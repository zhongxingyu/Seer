 package domain.forgroundTile;
 
 import java.awt.Graphics;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.image.ImageObserver;
 
 public class Tree extends ForgroundTile {
 	private Image treeImg;
 	private int treeType;
 	private String name;
 	private int part;
 	private Image picTop;
 	private Image picBot;
 	private boolean top;
 
 	public Tree(int treeType, int part, Double prio, Boolean top) {
 		zvalue = (int) (140 + prio);
 		String uri = "./res/pic/treetop.png";
 		picTop = Toolkit.getDefaultToolkit().getImage(uri);
 		this.treeType = treeType;
 		name = "treeTop";
 		this.part = part;
 		uri = "./res/pic/trunk.png";
 		picBot = Toolkit.getDefaultToolkit().getImage(uri);
 		this.top = top;
 		if(!top)
 			zvalue--;
 
 	}
 
 	public boolean isSollid() {
 		return (part == 4);
 	}
 
 	public void drawItSelf(Graphics g, ImageObserver io, int x, int y,
 			int with, int higth, float zoom) {
 		int picX = 0;
 		int picY = 0;
 		if (top) {
 			if (treeType == 0) {
 				picX = 0;
 				picY = 0;
 			} else if (treeType == 1) {
 				picX = 94;
 				picY = 96;
 			} else if (treeType == 2) {
 				picX = 96;
 				picY = 0;
 			} else if (treeType == 3) {
 				picX = 94;
 				picY = 96;
 			}
 
 			if (part == 0) {
 
 			}
 			if (part == 1) {
 				picY += 32;
 			}
 			if (part == 2) {
 				picY += 64;
 			}
 			if (part == 3) {
 				picX += 32;
 			}
 			if (part == 4) {
 				picX += 32;
 				picY += 32;
 			}
 			if (part == 5) {
 				picX += 32;
 				picY += 64;
 			}
 			if (part == 6) {
 				picX += 64;
 			}
 			if (part == 7) {
 				picX += 64;
 				picY += 32;
 			}
 			if (part == 8) {
 				picY += 64;
 				picX += 64;
 			}
 			g.drawImage(picTop, x, y, x + (int) (with * zoom), y
 					+ (int) (higth * zoom), picX, picY, picX + 32, picY + 32,
 					io);
 
 		} else {
 			if (treeType == 0) {
 				picX = 0;
 				picY = 0;
 			} else if (treeType == 1 || treeType == 3) {
 				picX = 94;
 				picY = 0;
 			}
 			if (part == 0) {
 
 			}
 			if (part == 1) {
 				picY += 32;
 			}
 			if (part == 2) {
 				picY += 64;
 			}
 			if (part == 3) {
 				picX += 32;
 			}
 			if (part == 4) {
 				picX += 32;
 				picY += 32;
 			}
 			if (part == 5) {
 				picX += 32;
 				picY += 64;
 			}
 			if (part == 6) {
 				picX += 64;
 			}
 			if (part == 7) {
 				picX += 64;
 				picY += 32;
 			}
 			if (part == 8) {
 				picY += 64;
 				picX += 64;
 			}
 			g.drawImage(picBot, x, y, x + (int) (with * zoom), y
 					+ (int) (higth * zoom), picX, picY, picX + 32, picY + 32,
 					io);
 
 			// g.drawChars((part+"").toCharArray(), 0,
 			// (part+"").toCharArray().length, 0, 10);
 		}
 
 	}
 	// g.drawChars((getZValue()+"").toCharArray(), 0,
 	// (getZValue()+"").toCharArray().length, 0, 10);
 }
