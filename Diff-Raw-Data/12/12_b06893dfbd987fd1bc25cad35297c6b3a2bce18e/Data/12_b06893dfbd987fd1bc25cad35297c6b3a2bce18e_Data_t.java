 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Color;
 
 public class Data{
     public static final int TITLE = 0;
     public static final int ENDLESS = 1;
     public static final int SCORE_ATTACK = 2;
 	public static final int STAGE_CLEAR = 3;
     public static final int DEMO = 4;
 	public static final int RANKING = 5;
//	public static final int CONFIG = 6;
	public static final int EXIT = 6;
 
 	public static boolean mouseCansel;
 	public static boolean keyCansel;
 	public static int time;
 	
 	public static int zoom = 1;
 	
 	public static final int RANKING_NAME_X = 70;
 	public static final int RANKING_TIME_X = 350;
 	public static final int RANKING_SCORE_X = 210;
 	public static final int RANKING_MAX_CHAIN_X = 430;
 	public static final int RANKING_MAX_DELETE_X = 530;
 	public static final int RANKING_STAGE_TIME_X = 215;
 	public static final int RANKING_STAGE_SCORE_X = 300;
 	public static final int RANKING_TOP_Y = 120;
 	public static final int RANKING_DIFF_Y = 29;
 	
 	public static final int SCORE_OUTPUT = 0;
 	public static final int SCORE_INPUT = 1;
 	
 	public static final int MESSAGE_X_SIZE = 20;
 	public static final int MESSAGE_Y_SIZE = 20;
 	
 	public static final int TITLE_CURSOR_X = 310;
 	public static final int TITLE_CURSOR_Y = 190;
 	public static final int TITLE_DIFFERENCE = 48;
 	
 	public static int cursorMaxX;
 	public static int cursorMaxY;
 	public static int scrollOffset;
     public static final int PANEL_SIZE = 32;
     public static final int SCROLL_UNIT = 4;
     public static final int DELETE_RAG = 10;
     public static final int DELETE_TIME = 50;
     public static final int DELETE_DIFFERENCE_TIME = 10;
     public static final int MPF = PANEL_SIZE/2;
     public static int GRAVITY = 16;
     public static final int PANEL_NUMBER = 6;
     public static final int INIT_CURSOR_X = 2;
     public static final int INIT_CURSOR_Y = 8;
     public static final int WIDTH = 640;
     public static final int HEIGHT = 480;
     public static int frame;
     public static int lv;
     public static int chain;
     public static final int fps = 30;
     public static final int ROW = 10;
     public static final int COL = 6;
     public static final int FIELD_START_X = 178;
     public static final int FIELD_START_Y = 64;
     public static final int TIME_LIMIT = fps*120;
     public static final int CLEAR_LINE = 20;
 	
 	public static boolean mousePressed = false;
 	public static int pressedX;
 	public static int pressedY;
 
 	public static final int LV_X = 43;
 	public static final int LV_Y = 113;
 
 	public static final int REST_X = 447;
 	public static final int REST_Y = 113;
 
 	public static final int SCORE_X = 447;
 	public static final int SCORE_Y = 202;
 	
 	public static final int MAX_CHAIN_X = 447;
 	public static final int MAX_CHAIN_Y = 296;
 	
 	public static final int MAX_DELETE_X = 447;
 	public static final int MAX_DELETE_Y = 389;
 	
 	public static final int CHAIN_EFFECT = 0;
 	public static final int SAME_EFFECT = 1;
 	public static final int EFFECT_SIZE = 30;
 	
 	public static final int EFFECT_TIME = 20;
 	
     public static int maxChain;
     public static int score;
     public static int maxDelete;
     
     public static int gameStatus = TITLE;
     
     public static final ImageData image = new ImageData();
     
     private static int debugCount;
     
 	public static final int SCORE_FONT = 0;
 	public static final int MESSAGE_FONT = 1;
 	
 	public static void setFont(Graphics g, int fontNo){
 		switch(fontNo){
 		case SCORE_FONT:
 			g.setColor(Color.WHITE);
     		g.setFont(new Font("lr oSVbN",0, 24*zoom));
 			break;
 		case MESSAGE_FONT:
 			g.setColor(Color.WHITE);
 			g.setFont(new Font("lr oSVbN",0, 18*zoom));
 			break;
 		}
 	}
 	
 	
     public static void debugPrint(String s){
 		debugCount++;
 		System.out.println("frame " + frame + " : " + s);
     }
     
 }
