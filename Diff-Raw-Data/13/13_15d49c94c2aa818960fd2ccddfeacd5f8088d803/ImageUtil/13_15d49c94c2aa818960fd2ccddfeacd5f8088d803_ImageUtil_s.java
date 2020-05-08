 /* 
  * @(#)ImageUtil.java
  */
 package framework.util;
 
 import java.awt.Graphics2D;
 import java.awt.Image;
 import java.awt.geom.AffineTransform;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 
 import javax.imageio.ImageIO;
 import javax.servlet.http.HttpServletResponse;
 import javax.swing.ImageIcon;
 
 import nl.captcha.Captcha;
 import nl.captcha.gimpy.RippleGimpyRenderer;
 import nl.captcha.servlet.CaptchaServletUtil;
 
 import com.google.zxing.BarcodeFormat;
 import com.google.zxing.client.j2se.MatrixToImageWriter;
 import com.google.zxing.common.BitMatrix;
 import com.google.zxing.qrcode.QRCodeWriter;
 
 /**
  * ̹  , ũ  ̿  ִ ƿƼ Ŭ̴.
  */
 public class ImageUtil {
 
 	/**
 	 * , ܺο ü νϽȭ    
 	 */
 	private ImageUtil() {
 	}
 
 	/**
 	 * ̹  Ѵ. 
 	 * ҽ ̹  width, height  ũⰡ ū   Ͽ  ä ̹ Ѵ.
 	 * @param srcPath ҽ ̹ 
 	 * @param destPath  ̹ 
 	 * @param width   
 	 * @param height   
 	 */
 	public static void resize(String srcPath, String destPath, int width, int height) {
 		File srcFile = new File(srcPath);
 		File destFile = new File(destPath);
 		resize(srcFile, destFile, width, height);
 	}
 
 	/**
 	 * ̹  Ѵ. 
 	 * ҽ ̹  width, height  ũⰡ ū   Ͽ  ä ̹ Ѵ.
 	 * @param srcFile ҽ ̹ 
 	 * @param destFile  ̹ 
 	 * @param width   
 	 * @param height   
 	 */
 	public static void resize(File srcFile, File destFile, int width, int height) {
 		Image image = new ImageIcon(srcFile.getAbsolutePath()).getImage();
 		if (image.getWidth(null) < 1 || image.getHeight(null) < 1) {
 			throw new IllegalArgumentException("  ʽϴ.");
 		}
 		double scale = _getScale(width, height, image.getWidth(null), image.getHeight(null));
 		int scaleWidth = (int) (scale * image.getWidth(null));
 		int scaleHeight = (int) (scale * image.getHeight(null));
 		BufferedImage bufImg = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_4BYTE_ABGR);
 		Graphics2D g2d = bufImg.createGraphics();
 		AffineTransform ax = new AffineTransform();
 		ax.setToScale(1, 1);
 		g2d.drawImage(image, ax, null);
 		Image resizedImg = bufImg.getScaledInstance(scaleWidth, scaleHeight, Image.SCALE_SMOOTH);
 		_writePNG(resizedImg, destFile);
 	}
 
 	/**
 	 * CAPTCHA ̹ 䰴ü ϰ,  ڿ Ѵ.
 	 * ⺻  200px,  50px Ѵ.
 	 * @param response captcha ̹  䰴ü
 	 * @return  ڿ
 	 */
 	public static String captcha(HttpServletResponse response) {
 		return captcha(response, 200, 50);
 	}
 
 	/**
 	 * CAPTCHA ̹ 䰴ü ϰ,  ڿ Ѵ.
 	 * @param response captcha ̹  䰴ü
 	 * @param width   ȼ
 	 * @param height   ȼ
 	 * @return  ڿ
 	 */
 	public static String captcha(HttpServletResponse response, int width, int height) {
 		response.reset();
 		Captcha captcha = new Captcha.Builder(width, height).addText().addBackground().gimp(new RippleGimpyRenderer()).build();
 		CaptchaServletUtil.writeImage(response, captcha.getImage());
 		return captcha.getAnswer();
 	}
 
 	/**
 	 * QRCode ̹ Ѵ.
 	 * @param url QRCode ĵ  ̵  URL
 	 * @param destPath QRCode ϸ
 	 * @param width QRCode ̹  
 	 */
 	public static void qrcode(String url, String destPath, int width) {
 		qrcode(url, new File(destPath), width);
 	}
 
 	/**
 	 * QRCode ̹ Ѵ.
 	 * @param url QRCode ĵ  ̵  URL
 	 * @param destFile QRCode ̹  ü
 	 * @param width QRCode ̹ 
 	 */
 	public static void qrcode(String url, File destFile, int width) {
 		try {
 			qrcode(url, new FileOutputStream(destFile), width);
 		} catch (FileNotFoundException e) {
			new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * QRCode ̹ Ѵ.
 	 * @param url QRCode ĵ  ̵  URL
 	 * @param response qrcode ̹  䰴ü
 	 * @param width QRCode ̹ 
 	 */
 	public static void qrcode(String url, HttpServletResponse response, int width) {
 		try {
 			response.reset();
 			response.setContentType("image/png");
 			qrcode(url, response.getOutputStream(), width);
 		} catch (Exception e) {
			new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * QRCode ̹ Ѵ.
 	 * @param url QRCode ĵ  ̵  URL
 	 * @param os  Ʈ
 	 * @param width QRCode ̹ 
 	 */
 	public static void qrcode(String url, OutputStream os, int width) {
 		QRCodeWriter l_qr_writer = new QRCodeWriter();
 		try {
 			String l_url = new String(url.getBytes("UTF-8"), "ISO-8859-1");
 			BitMatrix l_bit_matrix = l_qr_writer.encode(l_url, BarcodeFormat.QR_CODE, width, width);
 			MatrixToImageWriter.writeToStream(l_bit_matrix, "png", os);
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	//////////////////////////////////////////////////////////////////////////////////////////Private ޼ҵ
 
 	/**
 	 *  ̹    ̹   Ѵ. 
 	 * ũⰡ ū     Ѵ.
 	 * @param resizeWidth   
 	 * @param resizeHeight   
 	 * @param imageWidth  ̹  
 	 * @param imageHeight  ̹  
 	 * @return  
 	 */
 	private static double _getScale(int resizeWidth, int resizeHeight, int imageWidth, int imageHeight) {
 		double widthScale = (double) resizeWidth / imageWidth;
 		double heightScale = (double) resizeHeight / (double) imageHeight;
 		if (widthScale > heightScale) {
 			return heightScale;
 		} else {
 			return widthScale;
 		}
 	}
 
 	/**
 	 * ̹ PNG  Ѵ.
 	 * @param image  ̹ ü
 	 * @param destFile  ̹ 
 	 */
 	private static void _writePNG(Image image, File destFile) {
 		BufferedImage bufImg = new BufferedImage(image.getWidth(null), image.getHeight(null), BufferedImage.TYPE_4BYTE_ABGR);
 		Graphics2D g2d = bufImg.createGraphics();
 		g2d.drawImage(image, 0, 0, null);
 		try {
 			ImageIO.write(bufImg, "png", destFile);
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 }
