 package net.daum.remotesync;
 
 import static net.daum.remotesync.PackUtil.read16bit;
 import static net.daum.remotesync.PackUtil.read32bit;
 import static net.daum.remotesync.PackUtil.write16bit;
 import static net.daum.remotesync.PackUtil.write32bit;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PushbackInputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 
 /**
  * {@link SourceCodeList}와 목표파일(newFileIn)을 토대로 생성한 빌드코드 리스트. 
  * 이 객체와 원본파일의 내용을 합쳐서, 전체내용을 전송받지 않고도, 목표파일(newFile)을 
  * 만들어 낼 수 있다.
  * 
 * <li>{@link SourceCodeList} + newFile => BuildCodeList</li>
 * <li>{@link BuildCodeList} + srcFile => newFile<li>
  *
  * @see SourceCodeList
  * @author dante
  *
  */
 public class BuildCodeList extends ArrayList<BuildCode> {
 
 	public static final int VERSION1 = 0x81;
 	
 	private static final long serialVersionUID = 9134363866140763474L;
 	
 	
 	private int blockSize = RemoteSync.DEFAULT_BLOCK_SIZE;
 	private long rawLimit = RemoteSync.DEFAULT_RAW_LIMIT;
 	
 	private BuildCodeList() {}
 	
 	private BuildCodeList(int blockSize, long rawLimit) {
 		this.blockSize = blockSize;
 		this.rawLimit = rawLimit;
 	}
 	
 
 	/**
 	 * 블럭크기. SourceCodeList의 블럭크기와 같아야한다. 
 	 */
 	public int getBlockSize() {
 		return blockSize;
 	}
 
 	
 	private static Map<String, Integer> generateRefTable(SourceCodeList sc) {
 		Map<String, Integer> table = new HashMap<String, Integer>();
 		int idx = 0;
 		for (Signature sign: sc) {
 			table.put(new String(sign.getStrong()), idx++);
 		}
 		return table;
 	}
 	
 	private void processRawCodeIfNeeded(ByteArrayOutputStream rawbuf) {
 		if (rawbuf.size() > 0) {
 			add(BuildCode.createRawCode(rawbuf.toByteArray()));
 			rawbuf.reset();
 		}
 	}
 	
 	private void addByteToRawBuf(ByteArrayOutputStream rawbuf, byte c) {
 		if (rawbuf.size() >= rawLimit) {
 			processRawCodeIfNeeded(rawbuf);
 		}		
 		rawbuf.write(c);
 	}
 	
 
 	/**
 	 * {@link SourceCodeList}와 목표파일(newFileIn)을 이용해 BuildCodeList 생성하기. 
 	 * @param sourceCodeList 
 	 * @param newFileIn 목표파일의 InputStream
 	 * @param rawLimit 바이너리 포맷을 위해 제한된 값. 최대 (4M-1)의 기본값을 사용할 수 있다. 
 	 * @return 생성된 BulidCodeList
 	 * @throws IOException
 	 */
 	static final BuildCodeList create(SourceCodeList sourceCodeList, InputStream newFileIn, long rawLimit) throws IOException {
 		final int blockSize = sourceCodeList.getBlockSize();
 		BuildCodeList bc = new BuildCodeList(blockSize, rawLimit);
 		
 		Map<String, Integer> table = generateRefTable(sourceCodeList);
 		
 		PushbackInputStream pin = new PushbackInputStream(newFileIn, blockSize);
 		byte[] buf = new byte[blockSize];
 		int r = 0;
 		ByteArrayOutputStream rawbuf = new ByteArrayOutputStream();
 		while ((r = pin.read(buf)) > 0) {
 			if (r < blockSize) {
 				for (int i = 0; i < r; i++) {
 					bc.addByteToRawBuf(rawbuf, buf[i]);
 				}
 			} else {
 				Signature sign = new Signature(buf);
 				Integer idx = table.get(new String(sign.getStrong()));
 				if (idx != null) {
 					bc.processRawCodeIfNeeded(rawbuf);
 					bc.add(BuildCode.createRefCode(idx));
 				} else {
 					bc.addByteToRawBuf(rawbuf, buf[0]);
 					pin.unread(buf, 1, r - 1);
 				}
 			}
 		}
 		bc.processRawCodeIfNeeded(rawbuf);
 		return bc;
 	}
 
 	
 	public long patch(SourceFileAccess src, OutputStream newFileOut) throws IOException {
 		long written = 0;
 		for (BuildCode bc: this) {
 			written += bc.patch(src, blockSize, newFileOut);
 		}
 		return written;
 	}
 	
 	private long estimatedLength() {
 		long len = 0;
 		for (BuildCode code: this) {
 			len += code.length();
 		}
 		return len;
 	}
 
 	/** 
 	 * 네트워크로 보내기 위해 Binary포맷으로 변환하기. 
 	 * @param netOut 변환한 데이타를 저장할 네트워크 출력스트림.
 	 * @return 출력한 바이트 수
 	 * @throws IOException
 	 */
 	public long pack(OutputStream netOut) throws IOException {
 		netOut.write(VERSION1);
 		write16bit(netOut, blockSize);
 		write32bit(netOut, this.size());
 		write32bit(netOut, estimatedLength());
 		long written = 1 + 2 + 4 + 4;
 		
 		for (BuildCode code: this) {
 			written += code.pack(netOut);
 		}
 		return written;
 	}
 
 	/**
 	 * 네트워크로 수신한 Binary 포맷으로부터 BuildCodeList만들어 내기.
 	 * @param netIn 바이너리 포맷이 담겨있는 네트워크 입력스트림.
 	 * @return 분석해낸 BuildCodeList
 	 * @throws IOException
 	 */
 	public static final BuildCodeList unpack(InputStream netIn) throws IOException {
 		int header = netIn.read();
 		if (header != VERSION1) {
 			throw new RuntimeException("unknown buildcode version");
 		}
 		int blockSize = read16bit(netIn);
 		long count = read32bit(netIn);
 		read32bit(netIn); // length는 무시해도 됨. 
 		BuildCodeList bc = new BuildCodeList(blockSize, RemoteSync.DEFAULT_RAW_LIMIT);
 		while (count-- > 0) {
 			bc.add(BuildCode.unpack(netIn));
 		}		
 		return bc;
 	}
 	
 }
