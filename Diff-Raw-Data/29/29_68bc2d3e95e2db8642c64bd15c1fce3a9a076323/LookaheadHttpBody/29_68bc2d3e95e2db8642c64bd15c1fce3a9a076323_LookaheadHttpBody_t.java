 package net.hetimatan.util.http;
 
 
 import java.io.IOException;
 import net.hetimatan.io.file.MarkableReader;
 import net.hetimatan.util.event.EventTaskRunner;
 import net.hetimatan.util.io.ByteArrayBuilder;
 
 @Deprecated
 public class LookaheadHttpBody {
 	private MarkableReader mCurrentReader = null;
 	private long mStart = 0;
 	private long mStartTime = 0;
 	private int mContentLength = 0;
 
 	public LookaheadHttpBody(MarkableReader reader, long startPoint, int contentLength) throws IOException {
 		mCurrentReader = reader;
 		mStart = startPoint;
 		mStartTime = System.currentTimeMillis();
 		mContentLength = contentLength;
 	}
 	
 	public long getElapsedTime() {
 		return System.currentTimeMillis()-mStartTime;
 	}
 
 	public boolean lookahead() throws IOException {
 		MarkableReader reader = mCurrentReader;
 		reader.setBlockOn(false);
 		ByteArrayBuilder buffer = EventTaskRunner.getByteArrayBuilder();
 		buffer.setBufferLength(5*1024);
 		buffer.clear();
 		byte[] buf = buffer.getBuffer();
 		int ret = reader.read(buf, 0, buf.length);
 		if(ret<0) {
 			return true;
 		}
 		return readable(reader);
 	}
 	
 	public boolean readable(MarkableReader reader) throws IOException {
		if((reader.length()-mStart)>=mContentLength) {
			System.out.println("reader.length()="+reader.length());
			System.out.println("mStart="+mStart);
			System.out.println("mContentLength="+mContentLength);
 			return true;
 		} else {
			System.out.println("reader.length()="+reader.length());
			System.out.println("mStart="+mStart);
			System.out.println("mContentLength="+mContentLength);

 			return false;
 		}
 	}
 }
