 package com.hannah.swing.util;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.color.ColorSpace;
 import java.awt.image.BufferedImage;
 import java.awt.image.ColorConvertOp;
 import java.awt.image.ImageObserver;
 import java.awt.image.RenderedImage;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Random;
 
 import javax.imageio.ImageIO;
 import javax.swing.ImageIcon;
 
 import com.hannah.common.util.FileUtil;
 import com.hannah.common.util.StringUtil;
 
 /**
  * @author longrm
  * @date 2012-4-26
  */
 public class ImageUtil {
 
 	public static ImageIcon getImageIcon(String imagePath) {
 		return new ImageIcon(ImageUtil.class.getResource("/images" + imagePath));
 	}
 
 	/**
 	 * get image bytes
 	 * @param im
 	 * @param formatName
 	 * @return
 	 * @throws IOException
 	 */
 	public static byte[] getBytes(RenderedImage im, String formatName) throws IOException {
 		ByteArrayOutputStream output = new ByteArrayOutputStream();
 		ImageIO.write(im, formatName, output);
 		output.close();
 		return output.toByteArray();
 	}
 
 	/**
 	 * change ImageIcon to BufferedImage
 	 * @param imageIcon
 	 * @return
 	 */
 	public static BufferedImage getBufferedImage(ImageIcon imageIcon) {
 		int width = imageIcon.getIconWidth();
 		int height = imageIcon.getIconHeight();
 		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
 		Graphics gc = bufferedImage.createGraphics();
 		ImageObserver observer = imageIcon.getImageObserver();
 		gc.drawImage(imageIcon.getImage(), 0, 0, observer);
 		return bufferedImage;
 	}
 
 	public static BufferedImage createImage(String str, Font font) {
 		return createImage(str, font, Color.BLACK, Color.WHITE, 0.0);
 	}
 
 	public static BufferedImage createImage(String str, Font font, double theta) {
 		return createImage(str, font, Color.BLACK, Color.WHITE, theta);
 	}
 
 	public static BufferedImage createImage(String str, Font font, Color fontColor, Color backColor, double theta) {
 		return createImage(str.split("\\n"), font, fontColor, backColor, theta);
 	}
 
 	/**
 	 * create image to show string
 	 * @param strs
 	 * @param font
 	 * @param fontColor
 	 * @param backColor
 	 * @param theta
 	 * @return
 	 */
 	public static BufferedImage createImage(String[] strs, Font font, Color fontColor, Color backColor, double theta) {
 		int rotateLen = theta == 0 ? 0 : (int) (font.getSize() * theta / 3); // 旋转
 		int fWidth = font.getSize() / 2 + rotateLen;
 		int spaceWidth = font.getSize() / 3; // 左右边留空白
 		int fHeight = font.getSize();
 
 		// 计算字符所占总宽度，取最长的一行，中文字符占2位
 		int maxCount = 0;
 		for (String str : strs) {
 			int totalCount = 0;
 			for (int i = 0; i < str.length(); i++) {
 				if (StringUtil.isChinese(str.charAt(i) + ""))
 					totalCount += 2;
 				else
 					totalCount++;
 			}
 			if (maxCount < totalCount)
 				maxCount = totalCount;
 		}
 		int imageWidth = maxCount * fWidth + 2 * spaceWidth - maxCount * rotateLen / 3;
 		int imageHeight = strs.length * fHeight + font.getSize() / 4;
 
 		BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
 		Graphics2D g2D = (Graphics2D) image.getGraphics();
 		// 设置背景色
 		g2D.setColor(backColor);
 		// 画边框
 		g2D.fillRect(1, 1, imageWidth - 2, imageHeight - 2);
 		// 设置字体色
 		g2D.setColor(fontColor);
 		g2D.setFont(font);
 		// 绘画
 		int x = 0;
 		int y = 0;
 		for (String str : strs) {
 			x = spaceWidth;
 			y += fHeight - rotateLen;
 			// g2D.drawString(str, x, y);
 			// 画一行字符
 			for (int i = 0; i < str.length(); i++) {
 				// 旋转角度使字符弯曲 多行时有问题？？？???
 				if (theta != 0) {
 					double tmpTheta = x == spaceWidth ? theta : theta * 2 * (i % 2 == 0 ? 1 : -1);
 					g2D.rotate(tmpTheta, x, y);
 				}
 				String tmpStr = str.charAt(i) + "";
 				g2D.drawString(tmpStr, x, y);
 				if (StringUtil.isChinese(tmpStr))
 					x += fWidth * 2;
 				else
 					x += fWidth;
 				// 最后一个字符时恢复旋转的角度
 				if (i == str.length() - 1 && theta != 0) {
 					double tmpTheta = x == spaceWidth ? theta : theta * (i % 2 == 0 ? -1 : 1);
 					g2D.rotate(tmpTheta, x, y);
 				}
 			}
 		}
 		return image;
 	}
 
 	public static BufferedImage getInterfereImage(ImageIcon imageIcon, int lineCount) {
 		return getInterfereImage(imageIcon, lineCount, Color.BLACK);
 	}
 
 	/**
 	 * 随机产生干扰线，使图象中不易被其它程序探测到
 	 * @param imageIcon
 	 * @param lineCount
 	 * @param lineColor
 	 * @return
 	 */
 	public static BufferedImage getInterfereImage(ImageIcon imageIcon, int lineCount, Color lineColor) {
 		int imageWidth = imageIcon.getIconWidth();
 		int imageHeight = imageIcon.getIconHeight();
 		BufferedImage interfereImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_3BYTE_BGR);
 		Graphics2D g2D = (Graphics2D) interfereImage.getGraphics();
 		g2D.drawImage(imageIcon.getImage(), 0, 0, imageIcon.getImageObserver());
 		g2D.setColor(lineColor);
 		Random random = new Random(System.currentTimeMillis());
 		for (int i = 0; i < lineCount; i++) {
 			int x = random.nextInt(imageWidth);
 			int y = random.nextInt(imageHeight);
 			int xl = random.nextInt(imageWidth / 5);
 			int yl = random.nextInt(imageHeight / 5);
 			g2D.drawLine(x, y, x + xl, y + yl);
 		}
 		return interfereImage;
 	}
 
 	/**
 	 * 使用现成的类ColorConvertOp，它的作用就是将一个颜色模式的图片转换为另一个颜色模式的图片。
 	 * 颜色模式是诸如RGB颜色模式、灰度颜色模式等决定图片色彩的东西，
 	 * 比如一副RGB颜色模式的图片是彩色，但我们把它复制到一个灰度颜色模式的图片上时，图片就成灰色的了。
 	 * @param originalImage
 	 * @param colorspace
 	 * @return
 	 */
 	public static BufferedImage getFilterImage(BufferedImage originalImage, int colorspace) {
 		int imageWidth = originalImage.getWidth();
 		int imageHeight = originalImage.getHeight();
 		BufferedImage filterImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_3BYTE_BGR);
 		ColorConvertOp cco = new ColorConvertOp(ColorSpace.getInstance(colorspace), null);
 		cco.filter(originalImage, filterImage);
 		return filterImage;
 	}
 
 	/**
 	 * 改变图片像素Alpha值实现图片透明
 	 * @param imageIcon
 	 * @param alpha 1-255
 	 * @return alphaImage PNG format
 	 * @throws IOException
 	 */
 	public static BufferedImage getAlphaImage(ImageIcon imageIcon, int alpha) throws IOException {
 		int imageWidth = imageIcon.getIconWidth();
 		int imageHeight = imageIcon.getIconHeight();
 		BufferedImage alphaImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_4BYTE_ABGR);
 		Graphics2D g2D = (Graphics2D) alphaImage.getGraphics();
 		g2D.drawImage(imageIcon.getImage(), 0, 0, imageIcon.getImageObserver());
 		// 循环每一个像素点，改变像素点的Alpha值
 		for (int j1 = alphaImage.getMinY(); j1 < alphaImage.getHeight(); j1++) {
 			for (int j2 = alphaImage.getMinX(); j2 < alphaImage.getWidth(); j2++) {
 				int rgb = alphaImage.getRGB(j2, j1);
 				rgb = ((alpha + 1) << 24) | (rgb & 0x00ffffff);
 				alphaImage.setRGB(j2, j1, rgb);
 			}
 		}
 		// g2D.drawImage(alphaImage, 0, 0, imageIcon.getImageObserver());
 		return alphaImage;
 	}
 
	public static BufferedImage zoom(BufferedImage image, int width, int height) {
		return zoom(image, width, height, true);
	}

 	/**
 	 * 缩小放大图片尺寸
 	 * @param image
 	 * @param width
 	 * @param height
	 * @param keepHWRatio 保持高宽比
 	 * @return
 	 */
	public static BufferedImage zoom(BufferedImage image, int width, int height, boolean keepHWRatio) {
 		if (width < 1 || height < 1)
 			return null;
 
 		float oldWidth = image.getWidth(null);
 		float oldHeight = image.getHeight(null);
 		float xRatio = oldWidth / width;
 		float yRatio = oldHeight / height;
		if (keepHWRatio) {
			if (xRatio < yRatio) {
				xRatio = yRatio;
				width = (int) (oldWidth / xRatio);
			} else {
				yRatio = xRatio;
				height = (int) (oldHeight / yRatio);
			}
		}
 
 		BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
 		int x = 0, y = 0;
 		for (int i = 0; i < width; i++) {
 			for (int j = 0; j < height; j++) {
 				x = (int) (i * xRatio);
 				if (x > oldWidth)
 					x = (int) oldWidth;
 				y = (int) (j * yRatio);
 				if (y > oldHeight)
 					y = (int) oldHeight;
 				result.setRGB(i, j, image.getRGB(x, y));
 			}
 		}
 		return result;
 	}
 
 	public static void zoomWrite(File imageFile, double xRatio, double yRatio, File newImageFile) throws IOException {
 		zoomWrite(imageFile, xRatio, yRatio, FileUtil.getFileExtension(imageFile.getName()), newImageFile);
 	}
 
 	/**
 	 * 按比率输出图片文件
 	 * @param imageFile 原图片
 	 * @param xRatio 宽度比率
 	 * @param yRatio 高度比率
 	 * @param formatName 输出图片格式
 	 * @param newImageFile 新图片
 	 * @throws IOException
 	 */
 	public static void zoomWrite(File imageFile, double xRatio, double yRatio, String formatName, File newImageFile)
 			throws IOException {
 		// 先按比率创建BufferedImage
 		ImageIcon icon = new ImageIcon(imageFile.getAbsolutePath());
 		BufferedImage image = getBufferedImage(icon);
		image = zoom(image, (int) (image.getWidth() * xRatio), (int) (image.getHeight() * yRatio), false);
 		// 将BufferedImage输出
 		FileOutputStream output = new FileOutputStream(newImageFile);
 		ImageIO.write(image, formatName, output);
 	}
 
 }
