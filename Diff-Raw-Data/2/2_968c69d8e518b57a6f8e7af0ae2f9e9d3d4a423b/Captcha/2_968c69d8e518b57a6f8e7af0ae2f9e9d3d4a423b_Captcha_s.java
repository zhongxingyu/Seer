 /**
  * 
  */
 package org.cweili.wray.util;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.geom.AffineTransform;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.util.Random;
 
 /**
  * 生成验证码
  * 
  * @author Cweili
  * @version 2013-4-16 下午5:35:20
  * 
  */
 public class Captcha {
 
 	private static Random random = new Random();
 
 	public static String getRandomString(int length) {
 		StringBuilder sb = new StringBuilder();
 		for (int i = 0; i < length; ++i) {
 			sb.append((char) (random.nextInt(26) + 65));
 		}
 		return sb.toString();
 	}
 
 	public static Color getRandomColor() {
 		return getRandomColor(0, 255);
 	}
 
 	public static Color getRandomColor(boolean dark) {
 		return dark ? getRandomColor(0, 127) : getRandomColor(128, 255);
 	}
 
 	public static Color getRandomColor(int begin, int end) {
 		return new Color(random.nextInt(end - begin) + begin, random.nextInt(end - begin) + begin,
 				random.nextInt(end - begin) + begin);
 	}
 
 	public static BufferedImage out(String str, int width, int height) throws IOException {
 		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
 		Graphics2D g = (Graphics2D) bi.getGraphics();
 		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 
 		// 填充背景
 		g.setColor(getRandomColor(false));
 		g.fillRect(0, 0, width, height);
 
 		// 画随机线
		for (int i = 0; i < 250; ++i) {
 			g.setColor(getRandomColor());
 			int x = random.nextInt(width - 2) + 1;
 			int y = random.nextInt(height - 2) + 1;
 			int x2 = x + random.nextInt(20) + 1;
 			int y2 = random.nextInt(20) + 1;
 			y2 = random.nextInt(2) == 1 ? y - y2 : y + y2;
 			g.drawLine(x, y, x2, y2);
 		}
 
 		Font f = new Font("Tahoma", Font.BOLD, height * 3 / 4);
 		g.setFont(f);
 
 		// 画字符
 		for (int i = 0; i < str.length(); ++i) {
 			int x = width / (str.length() + 1) * i + width / 10;
 			int y = height * 4 / 5 - height * 1 / 6 + random.nextInt(height * 1 / 3);
 
 			// 旋转
 			AffineTransform at = new AffineTransform();
 			at.rotate(random.nextDouble() * 30 * Math.PI / (double) 180, x, 0);
 
 			// 缩放
 			float sc = random.nextFloat() + 0.8f;
 			sc = sc > 1f ? 1f : sc;
 			at.scale(1f, sc);
 			g.setTransform(at);
 
 			// 画阴影
 			g.setColor(Color.black);
 			g.drawString(str.charAt(i) + "", x + 1, y + 1);
 
 			// 画字符
 			g.setColor(getRandomColor());
 			g.drawString(str.charAt(i) + "", x, y);
 		}
 
 		g.dispose();
 		return bi;
 	}
 
 }
