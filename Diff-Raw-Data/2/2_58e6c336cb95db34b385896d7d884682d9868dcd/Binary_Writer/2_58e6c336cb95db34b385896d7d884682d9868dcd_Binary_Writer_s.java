 package util;
 
 import play.*;
 import java.util.*;
 import java.io.*;
 import java.nio.*;
 import java.nio.channels.*;
 
 import com.fasterxml.jackson.core.*;
 
 // USAGE SAMPLE
 /*
 	File dssFile = new File("./layerData/some.dss");
 	Binary_Writer myWriter = new Binary_Writer(dssFile);
 	int width = 6150, height = 4557;
 	
 	ByteBuffer writeStream = myWriter.writeHeader(width, height); 
 	if (writeStream != null) {
 		
 		for (int y=0; y < height; y++) {
 				for (int x=0; x < width; x++) {
 					writeStream.putFloat(x*4, someDataYouHave[x]); // blah, 4 = size of float, ie 32bit
 				}
 				// contents of writeStream magically! written here...
 				myWriter.writeLine();
 				//	...and after write, the writeStream is set up accept more data on the next loop
 			}
 		}
 		myWriter.close();
 	}
 */
 
 // Reader class for our DSS files...
 //------------------------------------------------------------------------------
 public class Binary_Writer
 {
 	// FILE reading management related
 	private File mOutputFile;
 	private FileOutputStream mFileStream;
 	private WritableByteChannel mFileChannel;
 	private ByteBuffer mLineBuffer;
 	
 	// HEADER related
 	private final static int mBinaryWriteVersion = 1; // NOTE: update version for each new header version change
 	
 	private int mWidth, mHeight;
 	private float mCellSize, mCornerX, mCornerY;
 	private int mNoDataValue;
 
 	// CONSTRUCTOR
 	//--------------------------------------------------------------------------
 	public Binary_Writer(File dssFile, int width, int height) {
 		
 		mOutputFile = dssFile;
 		
 		// TODO: fixme...need a handy way to define these across the app...
 		mCornerX = -10062652.65061f;
 		mCornerY = 5249032.6922889f;
 		mCellSize = 30.0f;
 		mNoDataValue = -9999;
 		mWidth = width;
 		mHeight = height;
 	}
 	
 	//--------------------------------------------------------------------------
 	public int getWidth() {
 		return mWidth;
 	}
 	public int getHeight() {
 		return mHeight;
 	}
 	
 	// Opens binary DSS file and writes the header. Returns null if file does not exist or
 	//	some other problem occurs. Otherwise returns a ByteBuffer	
 	//--------------------------------------------------------------------------
 	public ByteBuffer writeHeader() {
 		
 		return writeHeader(4); // FIXME: size of int?
 	}
 
 	// Opens binary file and writes the header. Returns null if file does not exist or
 	//	some other problem occurs. Otherwise returns a ByteBuffer	
 	//--------------------------------------------------------------------------
 	public ByteBuffer writeHeader(int rasterElementSize) {
 		
 		try {
 			mFileStream = new FileOutputStream(mOutputFile);
 			mFileChannel = mFileStream.getChannel();
 			
 			ByteBuffer buf = ByteBuffer.allocateDirect(4); // FIXME: size of int (version)?
 			// write version type			
 			buf.putInt(mBinaryWriteVersion);
 			buf.flip();
 			mFileChannel.write(buf);
 			
 			buf = ByteBuffer.allocateDirect(6 * 4); // FIXME: header field ct * size of int
 			
 			buf.putInt(mWidth);
 			buf.putInt(mHeight);
 			buf.putFloat(mCornerX);
 			buf.putFloat(mCornerY);
 			buf.putFloat(mCellSize);
 			buf.putInt(mNoDataValue);
 			buf.flip();
 			mFileChannel.write(buf);
 		}
 		catch (Exception e) {
 			Logger.info(e.toString());
 			return null;
 		}
 		
 		mLineBuffer = ByteBuffer.allocateDirect(mWidth * rasterElementSize);
 		return mLineBuffer;
 	}
 	
 	// NOTE: you really have to use the mLineBuffer as the fill buffer...
 	//	and this is returned from writeHeader...no other way to get it...use it or lose it!
 	//--------------------------------------------------------------------------
 	public void writeLine() {
 
 		try {
 			// NOTE: Depending on how the buffer was filled, the position seems to not be
 			//	set at all (e.g., if using put relative, the position moves along
 			//	so that's good?). if you use put absolute, eg. put(index, value), the 
 			//	position does not get moved or set so writing that buffer will emit nothing
 			//	to file)?
 			// ALSO NOTE: there may be ways to simplify this...perhaps depending on usage
			//	patters. E.g., setting the position to the limit might only be needed 
 			//	because flip sets the limit to the position, then resets position to zero
 			//	I guess this signals the writer to emit all the data from the zero position
 			//	up to the limit. Perhaps simpler is to make sure the limit is set to the
 			//	buffer size (which it hopefully always is)...and just set the position to 0 
 			//	before writing? ie, maybe the flip isn't necessary..
 			mLineBuffer.position(mLineBuffer.limit());
 			mLineBuffer.flip();
 			
 			mFileChannel.write(mLineBuffer);
 			mLineBuffer.clear();
 		}
 		catch (Exception e) {
 			Logger.info(e.toString());
 		}
 	}
 	
 	//--------------------------------------------------------------------------
 	public void close() {
 
 		try {
 			mFileChannel.close();
 			mFileStream.close();
 			mFileStream = null;
 		}
 		catch (Exception e) {
 			Logger.info(e.toString());
 		}
 		finally {
 			if (mFileStream != null) {
 				try {
 					mFileStream.close();
 				}
 				catch (Exception e) {
 					Logger.info(e.toString());
 				}
 			}
 		}
 	}
 }
 
