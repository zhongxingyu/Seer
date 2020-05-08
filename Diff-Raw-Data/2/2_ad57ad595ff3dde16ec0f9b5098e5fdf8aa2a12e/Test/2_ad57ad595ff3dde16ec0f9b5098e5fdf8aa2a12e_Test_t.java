 package com.github.musikk.hex;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.util.Random;
 
 import javax.swing.JFrame;
 
 public class Test {
 
 	static Font font;
 
 	public static void main(String[] args) throws Exception {
 
 //		Font fnt = Font.createFont(Font.TRUETYPE_FONT, new FileInputStream(new File("ProFontWindows.ttf")));
 		Font fnt = new Font(Font.MONOSPACED, Font.PLAIN, 24);
 		font = fnt.deriveFont(24f);
 
 		JFrame f = new JFrame("foobar");
 		f.setSize(new Dimension(800, 600));
 		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
		f.add(new HexPanel(new ByteArrayDataProvider(randomCrap(1024))), BorderLayout.CENTER);
 
 		f.setVisible(true);
 
 	}
 
 	private static byte[] randomCrap(int amount) {
 		Random r = new Random();
 		byte[] bytes = new byte[amount];
 		r.nextBytes(bytes);
 		return bytes;
 	}
 
 	private static byte[] repeatingCrap(int amount) {
 		byte[] bs = new byte[amount];
 		for (int i = 0; i < amount; i++) {
 			bs[i] = (byte) ((i % ('z' - 'A')) + 'A');
 		}
 		return bs;
 	}
 
 }
