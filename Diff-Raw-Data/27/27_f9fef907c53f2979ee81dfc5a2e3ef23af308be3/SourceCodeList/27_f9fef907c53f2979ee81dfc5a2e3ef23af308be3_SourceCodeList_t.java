 package net.daum.remotesync;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 
 import static net.daum.remotesync.PackUtil.*;
 
 
 /**
  * 원본 파일로 부터 계산한, 블럭별 해쉬코드 리스트로 ArrayList로 블럭별 {@link Signature}를 담고 있다.
  * 이 객체를 기준으로 {@link BuildCodeList}를 만들어낼 수 있고, BuildCodeList와 SourceFile이 있으면, 
  * 목표파일을 생성해낼 수 있다. 
  * <ol>
 * <li>SourceFile → SourceCodeList</li>
 * <li>SourceCodeList + TargetFile → {@link BuildCodeList}</li>
 * <li>{@link BuildCodeList} + SourceFile → TargetFile</li>
  * </ol>
  * 
  * <p>
  * 즉, 두 원격지간의 파일을 동기화할 때, 양끝단에서는 상대방의 파일내용을 모르는 상태에서, 최소한의 정보만 
  * 주고받으며 최신본의 파일을 만들어 낼 수 있다. 
  * </p>
  * 
  * <p>
  * 예를들어, machineA가 원본파일 srcFile를 보관하고 있고, machineB가 새로운 파일 newFile를 올리는 상황을 살펴보자. 
  * (누가 클라이언트이고 서버인지보다, 누가 최신본을 갖고있는지가 중요하다.)
  * </p>
  * 
  * 이때, 파일을 동기화하기 위한 절차는 다음과 같다. 
  * <ol>
 * <li>machineA가 srcFile로 부터 SourceCodeList를 생성 → machineB에게 전달한다. 
 * <li>machineB는 전달받은 SourceCodeList와 newFile의 내용을 바탕으로 {@link BuildCodeList}를 작성 → machineA에게 전달한다. 
  * <li>machineA는 machineB로부터 받은 {@link BuildCodeList}와 srcFile의 내용을 참고해 newFile을 생성해낸다. 
  * <li>이로써, machineA는 machineB의 newFile과 같은 내용의 파일을 갖게 된다. 
  * </ol>
  * 
  * <p>
  * 즉, machineA와 machineB가 주고받는 데이타는 SourceCodeList와 BuildCodeList뿐이며, srcFile과 newFile간
  * 내용의 일치부분이 많을 수록 해당데이터의 크기는 작아진다. 크기가 작으면 작을 수록, machineB가 newFile의 내용
  * 전체를 machineA에게 전송하는 경우에 비해 네트워크 송수신 데이타 크기가 작아진다. 
  * </p>
  * <p>
  * 네트워크상 송수신처리를 위해 {@link #pack}/{@link #unpack} 메소드를 구현했다. 
  * </p>
  * <p>
  * 해당 알고리즘은 Rsync의 논문을 토대로 그대로 구현했다.  
  * </p>
  * @author dante
  * @see Signature
  */
 
 public class SourceCodeList extends ArrayList<Signature> {
 	public static final int VERSION1 = 0x71;
 	
 	private static final long serialVersionUID = -5885331537042946256L;
 	private int blockSize = RemoteSync.DEFAULT_BLOCK_SIZE;
 	
 	private SourceCodeList() {}
 	
 	private SourceCodeList(int blockSize) {
 		this.blockSize = blockSize;
 	}
 	
 	/**
 	 * 원본파일로 부터 해쉬코드리스트를 생성한다. 원본파일을 지정된 블럭크기만큼 읽으며 
 	 * {@link Signature}를 생성해서 나열한다.  
 	 * @param in 원본파일의 InputStream
 	 * @param blockSize 블럭크기 (4096) 
 	 * @return 블럭별 {@link Signature}객체가 추가된 {@link SourceCodeList}
 	 * @throws IOException
 	 */
 	public static final SourceCodeList create(InputStream in, int blockSize) throws IOException {
 		SourceCodeList sc = new SourceCodeList(blockSize);
 		byte[] buf = new byte[blockSize];
 		int r = 0;
 		while ((r = in.read(buf)) > 0) {
 			if (r == blockSize) {
 				sc.add(new Signature(buf));
 			} 
 		}
 		return sc;		
 	}
 
 	/**
 	 * 해쉬코드를 생성하는데 사용된 블럭크기. 이후 작업의 기준점이 된다. 
 	 * @return 블럭크기 바이트 수
 	 */
 	public int getBlockSize() {
 		return blockSize;
 	}
 
 	
 	/**
 	 * 네트워크에서 수신한 binary data로 부터 SourceCodeList 만들기. 
 	 * @param in pack으로 저장된 binary data (아마도 네트워크로 수신했을 스트림)
 	 * @return 해당 데이타에서 추출된 SourceCodeList.
 	 * @throws IOException
 	 */
 	public static final SourceCodeList unpack(InputStream in) throws IOException {
 		int version = in.read();
 		if (version != VERSION1) {
 			throw new RuntimeException("SOURCE_CODES Version mismatch");
 		}
 		int blockSize = read16bit(in);
 		long count = read32bit(in);
 		SourceCodeList sc = new SourceCodeList(blockSize);
 		for (long i = 0; i < count; i++) {
 			sc.add(Signature.unpack(in));
 		}
 		return sc;
 	}
 	
 	/**
 	 * 네트워크로 송신하기 위한 binary data 만들기.
 	 * @param out 바이너리 데이타를 쓸 OutputStream (주로, 네트워크 아웃풋스트림)
 	 * @return 쓴 바이트 수 
 	 * @throws IOException
 	 */
 	public long pack(OutputStream out) throws IOException {
 		out.write(VERSION1);
 		write16bit(out, blockSize);
 		write32bit(out, this.size());
 		long written = 1 + 2 + 4;
 		for (Signature sign: this) {
 			write32bit(out, sign.getFast());
 			out.write(sign.getStrong());
 			written += 24;
 		}
 		return written;
 	}
 	
 	/**
 	 * 목표파일을 읽어서, 소스코드와의 차이점 분석.
 	 * @param newFileIn 목표파일의 InputStream
 	 * @param rawLimit 일치하지 않는 부분의 최대크기. 현재 binary 포맷의 제한으로, 최대 4MB미만까지 표현할 수 있다.  
 	 *                 최대크기이상의 부분이 발견되면, 두개이상의 빌드코드로 쪼개어 저장한다.
 	 * @return 생성된 {@link BuildCodeList}
 	 * @throws IOException
 	 */
 	public BuildCodeList generateBuildCodes(InputStream newFileIn, long rawLimit) throws IOException {
 		return BuildCodeList.create(this, newFileIn, rawLimit);
 	}
 	
 	/**
 	 * 목표파일을 읽어서, 소스코드와의 차이점 분석. 일치하지 않는 부분의 최대크기는 4MB - 1byte로 최대값 지정.
 	 * @param targetFileIn 목표파일 InputStream
 	 * @return 생성된 {@link BuildCodeList}
 	 * @throws IOException
 	 */
 	public BuildCodeList generateBuildCodes(InputStream targetFileIn) throws IOException {
 		return generateBuildCodes(targetFileIn, RemoteSync.DEFAULT_RAW_LIMIT);
 	}
 	
 }
