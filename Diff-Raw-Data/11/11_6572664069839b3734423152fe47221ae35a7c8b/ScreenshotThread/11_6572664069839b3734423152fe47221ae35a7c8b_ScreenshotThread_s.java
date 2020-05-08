 package main.java.com.owfg.facade.bb.StoreManagement.app;
 
 import java.util.Hashtable;
 import java.util.Vector;
 
 import javax.microedition.media.MediaException;
 
 import main.java.com.google.zxing.BarcodeFormat;
 import main.java.com.google.zxing.BinaryBitmap;
 import main.java.com.google.zxing.DecodeHintType;
 import main.java.com.google.zxing.LuminanceSource;
 import main.java.com.google.zxing.Reader;
 import main.java.com.google.zxing.ReaderException;
 import main.java.com.google.zxing.Result;
 import main.java.com.google.zxing.common.GlobalHistogramBinarizer;
 import main.java.com.google.zxing.oned.MultiFormatOneDReader;
 import net.rim.device.api.system.Bitmap;
 import net.rim.device.api.system.Display;
 
 public class ScreenshotThread implements Runnable {
 
 	public void run() {
 		Result result;
 		Reader reader;
 		Hashtable hints = new Hashtable(1);
 		Vector readerHints = new Vector(4);
 		readerHints.addElement(BarcodeFormat.UPC_A);
 		readerHints.addElement(BarcodeFormat.UPC_E);
 		readerHints.addElement(BarcodeFormat.CODE_128);
 		readerHints.addElement(BarcodeFormat.ITF);
 		hints.put(DecodeHintType.POSSIBLE_FORMATS, readerHints);
 		reader = new MultiFormatOneDReader(hints);
 		Bitmap bitmap = new Bitmap(Display.getWidth(), Display.getHeight());
 		while (true) {
 			Display.screenshot(bitmap);
 			LuminanceSource source = new CustomBitmapLuminanceSource(bitmap);
 			BinaryBitmap bitmap1 = new BinaryBitmap(
 					new GlobalHistogramBinarizer(source));
 			try {
 				result = reader.decode(bitmap1);
 			} catch (ReaderException e) {
 				return;
 			}
 			if (result != null) {
 				MyApp.resultText = result.getText();
				try {
					MyApp.player.stop();
				} catch (Exception e) {
					e.printStackTrace();
				}
 				return;
 			} else {
 				continue;
 			}
 		}
 	}
 
 }
