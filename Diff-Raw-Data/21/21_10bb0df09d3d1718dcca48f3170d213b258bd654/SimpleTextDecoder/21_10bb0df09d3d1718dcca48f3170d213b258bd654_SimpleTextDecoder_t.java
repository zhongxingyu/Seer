 package info.kyorohiro.helloworld.io;
 
 import info.kyorohiro.helloworld.text.KyoroString;
 import info.kyorohiro.helloworld.util.CharArrayBuilder;
 
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.nio.CharBuffer;
 import java.nio.charset.Charset;
 import java.nio.charset.CharsetDecoder;
 
 public class SimpleTextDecoder {
 
 	private MarkableReader mReader = null;
 	private CharArrayBuilder mBuffer = new CharArrayBuilder();
 	private Charset mCharset = null;
 	private BreakText mBreakText;
 
 	public SimpleTextDecoder(Charset _cs, MarkableReader reader, BreakText breakText) {
 		mCharset = _cs;
 		mReader = reader;
 		mBreakText = breakText;
 	}
 
 	public boolean isEOF() {
 		try {
 			if (mReader.getFilePointer() < mReader.length()) {
 				return false;
 			} else {
 				return true;
 			}
 		} catch (IOException e) {
 			return false;
 		}
 	}
 
 	public CharSequence decodeLine() throws IOException {
 		return decodeLine(null);
 	}
 
 	private CharsetDecoder mDecoder = null;
 	private ByteBuffer mByteBuffer = ByteBuffer.allocateDirect(32); // bb͏߂
 	private CharBuffer mCharBuffer = CharBuffer.allocate(16); // cb͏߂
 
 	public CharsetDecoder getCharsetDecoder() {
 		if (mDecoder == null) {
 			mDecoder = mCharset.newDecoder();
 		}
 		return mDecoder;
 	}
 
 
 	public synchronized CharSequence decodeLine(byte[] escape) throws IOException {
 		//long time1 = 0;
 		//long time2 = 0;
 		//time1 = System.currentTimeMillis();
 
 		mBuffer.clear();
 		mByteBuffer.clear();
 		mCharBuffer.clear();
 		CharsetDecoder decoder = getCharsetDecoder();
 		boolean end = false;
 
 		if (escape != null) {
 			mByteBuffer.put(escape);
 		}
 
 		long todoPrevPosition = mReader.getFilePointer();
 		
 		float[] ws = new float[5];
 		float textLength = 0;
 		int width = mBreakText.getWidth();
 		float textSize = mBreakText.getSimpleFont().getFontSize();
		android.util.Log.v("kiyo","textSize="+textSize+","+width);
 		int len = 0;
 
 		outside: do {
 			int d = mReader.read();
 			if (d >= 0) {
 				mByteBuffer.put((byte) d); // bbւ̏
 			} else {
 				break;
 			}
 			mByteBuffer.flip();
 			decoder.decode(mByteBuffer, mCharBuffer, end);
 
 			mCharBuffer.flip();
 			char c = ' ';
 			boolean added = false;
 			while (mCharBuffer.hasRemaining()) {
 				added = true;
 				mByteBuffer.clear();
 				c = mCharBuffer.get();
 				mBuffer.append(c);
 				{
 					// following code maby out of memor error , you must change
 					// kb
 					//int len = Integer.MAX_VALUE;// example -->1024*4;// force
 												// crlf 12kb
 					if (mBreakText != null) {
 //						len = mBreakText.breakText(mBuffer);
 						int size = mBuffer.getCurrentBufferedMojiSize();
 						ws[0] = 0;
 						mBreakText.getTextWidths(mBuffer.getAllBufferedMoji(), size-1, size, ws, textSize);
 						textLength += ws[0];
 						//if(c=='\t'){
 						//android.util.Log.v("kiyo","time2 ["+mBuffer.getAllBufferedMoji()[size-1]+"]"+c+"="+width+","+textLength +","+ ws[0]);
 						//}
 
 						if(textLength>width){
 							//android.util.Log.v("kiyo","dd1 s="+textLength+",w="+width+",l="+len+",s="+size);
 							len += 0;
 						} else {
 							//android.util.Log.v("kiyo","dd2 s="+textLength+",w="+width+",l="+len+",s="+size);
 							len +=1;
 						}
 					}
 					if (len < mBuffer.getCurrentBufferedMojiSize()) {
 						// ЂƂOŉs
 						mBuffer.removeLast();
 						mReader.seek(todoPrevPosition);
 						break outside;
 					} else {
 						todoPrevPosition = mReader.getFilePointer();
 					}
 				}
 			}
 			if (c == '\n') {
 				break;
 			}
 
 			if (!added) {
 				mByteBuffer.compact();
 			}
 			mCharBuffer.clear(); // cbݏԂɕύX
 		} while (!end);
 		KyoroString ret =new KyoroString(mBuffer.getAllBufferedMoji(),
 				mBuffer.getCurrentBufferedMojiSize());
 		//time2 = System.currentTimeMillis();
 		//android.util.Log.v("kiyo","time a="+(time2-time1));
 		return ret;
 	}
 }
