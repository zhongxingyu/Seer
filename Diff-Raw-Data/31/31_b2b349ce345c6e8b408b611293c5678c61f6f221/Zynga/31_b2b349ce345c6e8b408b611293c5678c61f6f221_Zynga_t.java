 package site.zynga;
 
 import java.awt.AWTException;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.Robot;
 import java.awt.Toolkit;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 
 import javax.imageio.ImageIO;
 
 import util.UniquePixelIn;
 
 public class Zynga {
 
 	private static final String IMAGE_PATH = "Numbers/";
 	private static final String DEALERCHAT = "DealerChat.png";
 	
 	private static final String TABLE_CARDS_PATH = "src/site/zynga/ZyngaTableCards.txt";
 	UniquePixelIn upi;
 	
 	private int[][] imageArray;
 	private Robot r;
 	
 	private int screenWidth, screenHeight;
 	private Rectangle screenRec;
 	private BufferedImage currentImage;
 	
 	private Point currentCardPoint;
 	
 	
 	public static void main(String[] args) {
 		Zynga z = new Zynga();
 		z.loop();
 	}
 	
 	public Zynga(){
 		
 		upi = new UniquePixelIn(TABLE_CARDS_PATH);
 		
 		screenWidth = Toolkit.getDefaultToolkit().getScreenSize().width;
 		screenHeight = Toolkit.getDefaultToolkit().getScreenSize().height;
 		screenRec = new Rectangle(screenWidth, screenHeight);
 		
 		try {
 			r = new Robot();
 		} catch (AWTException e) {
 			e.printStackTrace();
 		}
 		imageArray = getSel(DEALERCHAT);
 		
 		loop();
 	}
 	
 	private void loop(){
 		while(true){
 			System.out.println("Loop");
 			currentImage = getImage();
 			Point p = getLocation(currentImage, imageArray);
 			if(p.x !=0 && p.y !=0){
 				for(int i=0; i<5; i++){
					System.out.print(getCard2(new Point(p.x+(i*40), p.y)));
 				}
 				System.out.println();
 			}
 		}
 	}
 	
 	private BufferedImage getImage(){
 		return r.createScreenCapture(screenRec);
 	}
 	
 	private Point getLocation(BufferedImage bi, int[][] sel){
 		for(int i=0; i<bi.getWidth(); i++){
 			for(int j=0; j<bi.getHeight(); j++){
 				if(isMatch(bi, sel, i, j)){
 					return new Point(i, j);
 				}
 			}
 		}
 		return new Point(0, 0);
 	}
 	
 	private boolean isMatch(BufferedImage bi, int[][] sel, int x, int y){
 		for(int i=0; i<sel.length; i++){
 			for(int j=0; j<sel[0].length; j++){
 				if(bi.getWidth() <= i+x || bi.getHeight() <= j+y){
 					return false;
 				}
 				if(sel[i][j] != bi.getRGB(i+x, j+y)){
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 	
 	public int[][] getSel(String fileName){
 		BufferedImage img = null;
 		try {
 		    img = ImageIO.read(new File(IMAGE_PATH+fileName));
 		} catch (IOException e) {
 			System.out.println("Error");
 			e.printStackTrace();
 		}
 		int[][] sel = new int[img.getWidth()][img.getHeight()];
 		for(int i=0; i<img.getWidth(); i++){
 			for(int j=0; j<img.getHeight(); j++){
 				sel[i][j] = img.getRGB(i, j);
 			}
 		}
 		return sel;
 	}
 	
 	private char getCard(Point p){
 		p.x += (725-450);
 		p.y += (508-722);
 		currentCardPoint = p;
 		int col;
 		//ACE
 		col = getCardAux(4, 15);
 		if(col == -2841440 || col == -6645094){
 			return 'A';
 		}
 		//KING
 		col = getCardAux(4, 16);
 		if(col == -3569277 || col == -8684677){
 			return 'K';
 		}
 		//QUEEN
 		col = getCardAux(4, 10);
 		if(col == -3900298 || col == -9671572){
 			return 'Q';
 		}
 		//JACK
 		col = getCardAux(4, 11);
 		if(col == -1585203 || col == -3487030){
 			return 'J';
 		}
 		//TEN
 		col = getCardAux(3, 3);
 		if(col == -3371897 || col == -8487298){
 			return 'T';
 		}
 		//NINE
 		col = getCardAux(4, 5);
 		if(col == -5355971 || col == -13684945){
 			return '9';
 		}
 		//EIGHT
 		col = getCardAux(4, 4);
 		if(col == -1387825 || col == -3355444){
 			return '8';
 		}
 		//SEVEN
 		col = getCardAux(6, 13);
 		if(col == -1982018 || col == -4539718){
 			return '7';
 		}
 		//SIX
 		col = getCardAux(4, 6);
 		if(col == -5421764 || col == -13684945){
 			return '6';
 		}
 		//FIVE
 		col = getCardAux(4, 7);
 		if(col == -1453617 || col == -3421237){
 			return '5';
 		}
 		//FOUR
 		col = getCardAux(3, 11);
 		if(col == -791574 || col == -1447447){
 			return '4';
 		}
 		//THREE
 		col = getCardAux(3, 13);
 		if(col == -1982018 || col == -4539718){
 			return '3';
 		}
 		//TWO
 		col = getCardAux(4, 13);
 		if(col == -1982018 || col == -4539718){
 			return '2';
 		}
 		return 'X';
 	}
 	
 	private char getCard2(Point p){
		p.x += (725-450);
		p.y += (508-722);
 		int[][] cardRank = new int[20][20];
 		for(int x=0; x<20; x++){
 			for(int y=0; y<20; y++){
				cardRank[x][y] = currentImage.getRGB(p.x+x, p.y+y);
 			}
 		}
 		return upi.thisIs(cardRank);
 	}
 	
 	private int getCardAux(int x, int y){
 		return currentImage.getRGB(currentCardPoint.x+x, currentCardPoint.y+y);
 	}
 
 }
