 package net.hetimatan.net.http.request;
 
 import java.io.IOException;
 import net.hetimatan.io.file.KyoroFileForKyoroSocket;
 import net.hetimatan.io.file.MarkableFileReader;
 import net.hetimatan.io.filen.CashKyoroFile;
 import net.hetimatan.io.net.KyoroSocket;
 import net.hetimatan.util.http.LookaheadHttpBody;
 import net.hetimatan.util.http.LookaheadHttpHeader;
 import net.hetimatan.util.http.HttpRequestHeader;
 import net.hetimatan.util.http.HttpResponse;
 
 public class HttpGetResponse {
 	private CashKyoroFile mVF = null;
 	private int mVfOffset = 0;
 	private KyoroSocket mSocket = null;
 	private LookaheadHttpHeader mHeaderChunk = null;
 	private LookaheadHttpBody mBodyChunk = null;
 	private KyoroFileForKyoroSocket mBase = null;
 	private MarkableFileReader mReader = new MarkableFileReader(mBase, 512);
 	private long mContentLength = Integer.MAX_VALUE;
 	private HttpResponse mResponse = null;
 	private long mBodyStart = 0;
 
 	public HttpGetResponse(KyoroSocket socket) throws IOException {
 		mSocket = socket;
 		mBase = new KyoroFileForKyoroSocket(mSocket, 512*30);
 		mReader = new MarkableFileReader(mBase, 512*30);
 		mVfOffset = 0;
 	}
 	
 	public void todo_setSocket(KyoroSocket socket) {
 		mSocket =socket;
 	}
 
 	public int getVFOffset() {
 		return mVfOffset;
 	}
 
 	public CashKyoroFile getVF() {
 		return mVF;
 	}
 
 	public HttpResponse getHttpResponse() throws IOException {
 		return mResponse;
 	}
 
 	public void close() {
 		try { mVF.close();
 		} catch (IOException e) { e.printStackTrace();
 		}
 	}
 	
 	public boolean headerIsReadable() throws IOException {		
 		if(mHeaderChunk == null) {
 			mHeaderChunk = new LookaheadHttpHeader(mReader, Integer.MAX_VALUE);
 		}
 		return LookaheadHttpHeader.readByEndOfHeader(mHeaderChunk, mReader);
 	}
 
 	
 	public boolean bodyIsReadable() throws IOException {
 		if(mBodyChunk == null) {
 			mBodyChunk = new LookaheadHttpBody(mReader, mBodyStart, (int)mContentLength);
 		}
 		return mBodyChunk.lookahead();
 	}
 
 	
 	public void readHeader() throws IOException, InterruptedException {
 		try {
 			mReader.seek(0);
 			mResponse = HttpResponse.decode(mReader, false);
 			mBodyStart = mReader.getFilePointer();
 			mVfOffset = (int)mReader.getFilePointer();
 			mContentLength = mResponse.getContentSizeFromHeader();
 			{
 				mReader.seek(0);
 				byte[] bu = new byte[mVfOffset];
 				mReader.read(bu);
				System.out.println("++++"+new String(bu)+"#####");
 				mReader.seek(mVfOffset);
 			}
 			for(HttpRequestHeader h :mResponse.getHeader()) {
 				System.out.print("[##]"+h.getKey()+","+h.getValue());
 			}
 		} catch(Exception e) {
 			e.printStackTrace();
 			mVfOffset = (int)mReader.length();
 			{
 				mReader.seek(0);
 				byte[] bu = new byte[mVfOffset];
 				mReader.read(bu);
 				System.out.println("+DEBUG++"+new String(bu)+"#####");
 				mReader.seek(mVfOffset);
 			}
 		}
 	}
 
 	
 	public void readBody() throws IOException, InterruptedException {
 		try {
 			int datam = 0;
 			int num=0;
 			do {
 				datam = mReader.read();
 				if(datam < 0) {
 					break;
 				}
 				num++;
 				Thread.yield();
 			} while((num<mContentLength));
 		} finally {
 			mReader.baseFileIsClosedWhenCallClose(true);
 			mBase.baseFileIsClosedWhenCallClose(false);
 			mReader.close();
 		}
 		mVF = mBase.getVF();		
 	}
 }
