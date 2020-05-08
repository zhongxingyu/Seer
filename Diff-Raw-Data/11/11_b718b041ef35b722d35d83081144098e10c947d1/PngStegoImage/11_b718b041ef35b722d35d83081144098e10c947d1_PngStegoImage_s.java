 package hack.pwn.gadaffi.steganography;
 
 import hack.pwn.gadaffi.Constants;
 import hack.pwn.gadaffi.exceptions.DecodingException;
 import hack.pwn.gadaffi.exceptions.EncodingException;
 
 import java.io.ByteArrayOutputStream;
 import java.nio.ByteBuffer;
 import java.util.LinkedList;
 import java.util.List;
 
 import android.graphics.Bitmap;
 import android.graphics.Bitmap.CompressFormat;
 import android.graphics.Bitmap.Config;
 import android.graphics.BitmapFactory;
 
 /**
  * This class decodes and encodes PNG images.
  *
  */
 public class PngStegoImage extends AStegoImage{
 	protected static final String TAG = "PngStegoImage";
 	/**
 	 * This is the ratio of actual embedded bits per pixel
 	 */
 	public static final double RATIO = 0.09375;
 	
 	private static final int R_FLAG = 0x00010000;
 	private static final int G_FLAG = 0x00000100;
 	private static final int B_FLAG = 0x00000001;
 	
 	private static final int STATE_R = 0;
 	private static final int STATE_G = 1;
 	private static final int STATE_B = 2;
 	
 
 	/**
 	 * Takes a image bytes (should be a png file) and gets
 	 * the bytes out of the embedded data inside the png file,
 	 * should check for the magic value, and cancel processing
 	 */
 	@Override
 	public void decode() throws DecodingException{
 		if(getImageBytes() == null) {
 			throw new DecodingException("Decode called without image bytes");
 		}
 		
 		//
 		// Given just bytes from an image, reconstuct it into a Bitmap
 		//
 		Bitmap b = BitmapFactory.decodeByteArray(getImageBytes(), 0, getImageBytes().length);
 		setImageBitmap(b);
 
 		boolean gotByte = true;
 		
 		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
 		LinkedList<Boolean> bitBuffer = getBitBuffer();
 		
 		int currentState = STATE_START;
 		int length = 0;
 		double totalPixels = b.getHeight() * b.getWidth();
 		int maxLength   = (int) Math.floor((totalPixels * RATIO) - Constants.STEGO_HEADER_LENGTH);
 		for(int x = 0; x < b.getHeight(); x++) {
 			for(int y = 0; y < b.getWidth(); y++) {
 				int pixel = b.getPixel(x, y);
 				boolean firstBit  = (pixel & R_FLAG) != 0;
 				boolean secondBit = (pixel & G_FLAG) != 0;
 				boolean thirdBit  = (pixel & B_FLAG) != 0;
 				
 				Byte bite = null;
 				
 				bite = pushBit(firstBit, bitBuffer);
 				gotByte = handleByte(bite, outputStream, bitBuffer, gotByte);
 				
 				bite = pushBit(secondBit, bitBuffer);
 				gotByte = handleByte(bite, outputStream, bitBuffer, gotByte);
 				
 				bite = pushBit(thirdBit, bitBuffer);
 				gotByte = handleByte(bite, outputStream, bitBuffer, gotByte);
 				
 				if(gotByte) {
 					switch(currentState) {
 					case STATE_START:
 						if(outputStream.size() == Constants.MAGIC_VALUE_LENGTH) {
 							int val = 
 									ByteBuffer
 									.wrap(outputStream.toByteArray())
 									.order(Constants.BYTE_ORDER)
 									.getInt();
 							
 							if(val == Constants.MAGIC_VALUE) {
 								currentState = STATE_GOT_MAGIC;
 							}
 							else {
 								throw new DecodingException("Bad magic value: " + val);
 							}
 						}
 						break;
 					case STATE_GOT_MAGIC:
 						if(outputStream.size() == Constants.STEGO_HEADER_LENGTH) {
 							length = ((ByteBuffer) ByteBuffer
 											.wrap(outputStream.toByteArray())
 											.order(Constants.BYTE_ORDER)
 											.position(Constants.MAGIC_VALUE_LENGTH))
 											.getShort();
 							if(length > 0 &&
 							   length <= maxLength) {
 								currentState = STATE_GOT_LENGTH;
 								outputStream = new ByteArrayOutputStream(length);
 							}
 							else {
 								throw new DecodingException(String.format("Bad length: %d, Max is: %d", length, maxLength));
 							}
 						}
 						break;
 					case STATE_GOT_LENGTH:
 						if(outputStream.size() == length){
 							setEmbeddedData(outputStream.toByteArray());
 							return;
 						}
 						break;
 					}
 				}
 			}
 		}
 
 		//
 		// if we got here, theres an exception.
 		//
 		
 		throw new DecodingException("Never retrieved enough bytes.");
 	}
 
 	private boolean handleByte(Byte bite, ByteArrayOutputStream outputStream,
 			List<Boolean> bitBuffer, boolean gotByte) {
 		if(bite != null) {
 			outputStream.write(bite);
 			bitBuffer.clear();
 			return true;
 		}
 		else {
 			return gotByte;
 		}
 	}
 
 	@Override
 	/**
 	 * Takes a image bitmap (cover image), and embedded data
 	 * and produced image bytes containing the embedded data inside
 	 * the cover image, in png format.
 	 */
 	public void encode() throws EncodingException {
 		
 		if(getImageBitmap() == null) {
 			throw new EncodingException("Encode called without image bitmap.");
 		}
 		if(getEmbeddedData() == null) {
 			throw new EncodingException("Encode called without embeded data.");
 		}
 		
 		
 		List<Boolean> bits = getBitsForData(getEmbeddedData());
 		
 		Bitmap mutableBitmap = getImageBitmap().copy(Config.ARGB_8888, true);
 		mutableBitmap.setHasAlpha(true);
 		/*
 		 * Here's our stego algo.
 		 * 
 		 * Given our bit array, we set each pixels RGB values
 		 * individually even or odd depending if the value is odd or even
 		 * 
 		 * Set = odd,
 		 * Not set = even
 		 * 
 		 * We get 3 bits per pixel.
 		 */
 		int x = 0;
 		int y = 0;
 		int currentPixel = -1;
 		int currentState = STATE_R;
 		
 		for(boolean bit : bits) {
 			switch(currentState) {
 			case STATE_R:
 				if(x >= getImageBitmap().getHeight()) {
 					throw new EncodingException("Too many bytes given.");
 				}
 				
 				currentPixel = mutableBitmap.getPixel(x, y);
 				if(currentPixel == 0) currentPixel = Constants.PIXEL_BLACK_WITH_ALPHA;
 				
 				currentPixel = setByte(currentPixel, R_FLAG, bit);
 				currentState = STATE_G;
 				break;
 			case STATE_G:
 				currentPixel = setByte(currentPixel, G_FLAG, bit);
 				currentState = STATE_B;
 				break;
 			case STATE_B:
 				currentPixel = setByte(currentPixel, B_FLAG, bit);
 				mutableBitmap.setPixel(x, y, currentPixel);
 				y++;
 				if(y >= mutableBitmap.getWidth()) {
 					y = 0;
 					x++;
 				}
 				currentState = STATE_R;
 				break;
 			}
 		}
 		
 		//
 		// Handle the case where we have only set 2 or 1 bits in a given pixel
 		// before we reach the end of our data.
 		//
 		if(currentState != STATE_R) {
 			mutableBitmap.setPixel(x, y, currentPixel);
 		}
 		
 		//
 		// Encode our image into a set of bytes.
 		//
 		ByteArrayOutputStream bos = new ByteArrayOutputStream();
 		mutableBitmap.compress(CompressFormat.PNG, 100, bos);
 		setImageBytes(bos.toByteArray());
 		
 	}
 	
 	public int setByte(int pixel, int flag, boolean bit) {
 		if(bit) {
			return setByteEven(pixel, flag);
 		}
 		else {
			return setByteOdd(pixel, flag);
 		}
 	}
 	
	public int setByteEven(int pixel, int flag) {
 	    pixel = zeroBit(pixel, flag);
 		return pixel ^ flag;
 	}
 	
 	private int zeroBit(int pixel, int flag) {
 		return pixel & (0xFFFFFFFF ^ flag);
 	}
 
	public int setByteOdd(int pixel, int flag) {
 		return zeroBit(pixel, flag);
 	}
 
 
 }
