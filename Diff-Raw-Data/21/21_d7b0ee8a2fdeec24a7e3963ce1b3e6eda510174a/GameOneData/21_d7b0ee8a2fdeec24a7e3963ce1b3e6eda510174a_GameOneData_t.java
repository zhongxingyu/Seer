 package lazygames.trainyoureye;
 
 import java.util.Random;
 
 import android.graphics.Color;
 import android.graphics.drawable.ShapeDrawable;
 import android.util.Log;
 
 public class GameOneData {
 
 	private static final int startLevel = 1;
 	private static final int alpha = 255;
 	private static int level;
 	private static int amountShapes;
 	//private int score;
	private int difficulty = 3;
 	private static int canvasWidth = 0;
 	private static int canvasHeight = 0;
 	private int leftColor;
 	private int rightColor;
	private int leftOrRight; // 0 = left, 1 = right
 	private int correctShape;
 	protected GameOneCanvas game_one_canvas;
 	
 	private RandomShape[] shapes;
 	private ShapeDrawable[] drawableShapes;
 
 //	private int timer;
 	
 	GameOneData() {	
 		level = startLevel;
 		amountShapes = 6;
		this.leftOrRight = 1;
 		//this.score = 0;
 		setup();
 	}
 	
 	GameOneData (int start_level) {
 		level = start_level;
 	}
 	public void set_game_level(int current_level) {
 		level = current_level;
 	}
 	public int get_game_level() {
 		return level;
 	}
 	public void setup() {
 		
 		int red = SettingManager.readRedValue();
 		int blue = SettingManager.readBlueValue();
 		int green = SettingManager.readGreenValue();
 		
 		if(this.leftOrRight == 0) {
 			leftColor = Color.argb(alpha, 0, blue, green);
 			rightColor = Color.argb(alpha, red, 0, 0);
 		}
 		else {
			leftColor = Color.argb(alpha, red, 0, 0);
			rightColor = Color.argb(alpha, 0, blue, green);
 		}
 	}
 
 	public RandomShape[] getShapes() {
 		return this.shapes;
 	}
 	public static void setCanvasWidth(int width) {
 		canvasWidth = width;
 	}
 	public static void setCanvasHeight(int height) {
 		canvasHeight = height;
 	}
 	public static int getCanvasWidth() {
 		return canvasWidth;
 	}
 	public static int getCanvasHeight() {
 		return canvasHeight;
 	}
 	
 	
 	public void createShapes() {
 		
 		Random r = new Random();
 		int width, height;
 		Log.e("canvaswidth", "canvaswidth" + canvasWidth);
 		Log.e("canvasheight", "canvasheight" + canvasHeight);
 		if(canvasWidth * 2 <= canvasHeight * 3){
 			width = canvasWidth / 2;
 			height = width;
 		} else {
 			height = canvasHeight /3;
 			width = height;
 		}
 		
 		shapes = new RandomShape[amountShapes];
 		drawableShapes = new ShapeDrawable[amountShapes];
 		
 		/*
 		shapes[0] = new RandomShape(0, 0, 100, 100, 4);
 		shapes[1] = new RandomShape(0, 100, 100, 100, 4);
 		shapes[2] = new RandomShape(100, 0, 100, 100, 4);
 		shapes[3] = new RandomShape(100, 100, 100, 100, 4);
 		shapes[4] = new RandomShape(200, 0, 100, 100, 4);
 		shapes[5] = new RandomShape(200, 100, 100, 100, 4);
 		*/
 	
 		for(int i = 1; i < amountShapes; i++)
 			shapes[i] = new RandomShape((i%2)*height, ((int)((double)i/2))*width, width, height, this.difficulty);
 		
 		this.correctShape = r.nextInt(5)+1; // shapes[1 tot 5]
 		shapes[0] = new RandomShape(0, 0, shapes[this.correctShape]);
 	
 		for(int i = 0; i < amountShapes; i++){
 			drawableShapes[i] = new ShapeDrawable(shapes[i].getShape());
			drawableShapes[i].getPaint().setColor(leftColor);
 			drawableShapes[i].setBounds(0, 0, canvasWidth, canvasHeight);		
 		}
		drawableShapes[0].getPaint().setColor(rightColor);
 	}
 	
 	public void setGameOneCanvas(GameOneCanvas game_one_canvas) {
 		this.game_one_canvas = game_one_canvas;
 	}
 	
 	public void gameLoop() {
 		createShapes();
 		this.game_one_canvas.setDrawableShapes(drawableShapes);
 	}
 	public void levelUp() {
 		this.difficulty += 2;
 	}
 	public void reset() {
 		this.difficulty = 3;
 	}
 	public void reDrawGameCanvas() {
 		this.game_one_canvas.invalidate();
 	}
 	public int getCorrectShape() {
 		return correctShape;
 	}
 	
 }
