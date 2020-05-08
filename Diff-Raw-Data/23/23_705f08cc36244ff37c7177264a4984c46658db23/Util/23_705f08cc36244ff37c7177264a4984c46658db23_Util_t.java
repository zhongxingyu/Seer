 package org.cxt.lt;
 
 import java.awt.Dimension;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.image.BufferedImage;
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 import javax.imageio.ImageIO;
 
 import org.cxt.lt.util.ConfigManager;
 
 public class Util {
 
 	public static Point getScreenWorkingSize() {
 
 		Point windowSize = new Point();
 
 		Rectangle rect = java.awt.GraphicsEnvironment
 				.getLocalGraphicsEnvironment().getMaximumWindowBounds();
 
 		windowSize.x = rect.width;
 		windowSize.y = rect.height;
 
 		return windowSize;
 
 	}
 
 	public static Point getScreenFullSize() {
 		Dimension resolution = Toolkit.getDefaultToolkit().getScreenSize();
 		Rectangle rectangle = new Rectangle(resolution);
 
 		return new Point(rectangle.width, rectangle.height);
 	}
 
 	public static void saveImageToDefaultFile(BufferedImage aBufferedImage) {
 		saveImageToFile(aBufferedImage,
 				ConfigManager.getString("SHOT_IMAGE_TMP_FILE"));
 	}
 
 	public static void saveImageToFile(BufferedImage aBufferedImage,
 			String aFilePath) {
 
 		File file = new File(aFilePath);
 
 		file.getParentFile().mkdirs();
 
 		try {
 			ImageIO.write(aBufferedImage, "png", file);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static boolean compareImageBinary(BufferedImage image1,
 			BufferedImage image2) {
 
 		boolean ret = true;
 
 		int w1 = image1.getWidth(null);
 		int h1 = image1.getHeight(null);
 
 		int w2 = image2.getWidth(null);
 		int h2 = image2.getHeight(null);
 
 		if ((w1 == w2) && (h1 == h2)) {
 
 			BufferedImage imageB1 = new BufferedImage(w1, h1,
 					BufferedImage.TYPE_BYTE_BINARY);
 			BufferedImage imageB2 = new BufferedImage(w2, h2,
 					BufferedImage.TYPE_BYTE_BINARY);
 
 			for (int i = 0; i < w1; i += 2) {
 				for (int j = 0; j < h1; j += 2) {
 
 					int rgb1 = image1.getRGB(i, j);
 					int rgb2 = image2.getRGB(i, j);
 
 					imageB1.setRGB(i, j, rgb1);
 					imageB2.setRGB(i, j, rgb2);
 
 				}
 			}
 
 			for (int i = 0; i < w1; i += 2) {
 				for (int j = 0; j < h1; j += 2) {
 
 					if (imageB1.getRGB(i, j) == imageB2.getRGB(i, j)) {
 						continue;
 					} else {
 						ret = false;
 						return ret;
 					}
 				}
 			}
 
 		} else {
 			ret = false;
 		}
 
 		return ret;
 	}
 
 	public static void deleteFile(String path) {
 		File f = new File(path);
 		if (f.isDirectory()) {// 如果是目录，先递归删除
 			String[] list = f.list();
 			for (int i = 0; i < list.length; i++) {
 				deleteFile(path + File.separatorChar + list[i]);// 先删除目录下的文件
 			}
 		}
 		f.delete();
 	}
 
 	/**
 	 * copy 文件夹
 	 */
 	public static void copyFile(File source, File target) {// copy 文件
 		FileInputStream inFile = null;
 		FileOutputStream outFile = null;
 		try {
 			inFile = new FileInputStream(source);
 			outFile = new FileOutputStream(target);
 			byte[] buffer = new byte[1024];
 			int i = 0;
 			while ((i = inFile.read(buffer)) != -1) {
 				outFile.write(buffer, 0, i);
 			}
 			inFile.close();
 			outFile.close();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (inFile != null) {
 					inFile.close();
 				}
 				if (outFile != null) {
 					outFile.close();
 				}
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	// 处理目录
 	public static void copyFolder(File source, File target) {
 		File[] file = source.listFiles();// 得到源文件下的文件项目
 		for (int i = 0; i < file.length; i++) {
 			if (file[i].isFile()) {// 判断是文件
 				File sourceDemo = new File(source.getAbsolutePath()
 						+ File.separatorChar + file[i].getName());
 				File destDemo = new File(target.getAbsolutePath()
 						+ File.separatorChar + file[i].getName());
 				copyFile(sourceDemo, destDemo);
 			}
 			if (file[i].isDirectory()) {// 判断是文件夹
 				File sourceDemo = new File(source.getAbsolutePath()
 						+ File.separatorChar + file[i].getName());
 				File destDemo = new File(target.getAbsolutePath()
 						+ File.separatorChar + file[i].getName());
 				destDemo.mkdir();// 建立文件夹
 				copyFolder(sourceDemo, destDemo);
 			}
 		}// end copyDict
 
 	}
 
 	public static void copy(File source, File target) {
 		if (source.isFile()) {
 			copyFile(source, target);
 		} else {
 			copyFolder(source, target);
 		}
 	}
 
 	public static boolean compareImage(BufferedImage image1,
 			BufferedImage image2) {
 
 		boolean ret = true;
 
 		int w1 = image1.getWidth(null);
 		int h1 = image1.getHeight(null);
 
 		int w2 = image2.getWidth(null);
 		int h2 = image2.getHeight(null);
 
 		if ((w1 == w2) && (h1 == h2)) {
 			for (int i = 0; i < w1; i += 2) {
 				for (int j = 0; j < h1; j += 2) {
					
 					if (image1.getRGB(i, j) == image2.getRGB(i, j)) {
 						continue;
 					} else {
 						ret = false;
 						return ret;
 					}
 				}
 			}
 
 		} else {
 			ret = false;
 		}
 
 		return ret;
 	}
 
 	public static String exec(String string) throws IOException {
 		Process process = Runtime.getRuntime().exec(string);
 		InputStream iStream = process.getInputStream();
 
 //		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
 //
 //		String line = reader.readLine();
 		
 		return null;
 	}
 
 }
