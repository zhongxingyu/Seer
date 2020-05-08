 package dk.aau.cs.giraf.cars;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import android.os.Bundle;
 import android.app.Activity;
 import android.graphics.Color;
 import android.view.Menu;
 import dk.aau.cs.giraf.cars.gamecode.*;
 import dk.aau.cs.giraf.cars.gamecode.GameObjects.*;
 import dk.aau.cs.giraf.cars.sound.RecorderThread;
 
 public class GameActivity extends Activity {
 	GameView view;
 	List<GameObject> objectList;
 	GameThread gameThread;
 	RecorderThread recorderThread;
 	
 	private int color1 = GameInfo.color1;
 	private int[] color1_Ids = null;
 	private int color2 = GameInfo.color2;
 	private int[] color2_Ids = null;
 	private int color3 = GameInfo.color3;
 	private int[] color3_Ids = null;
 	private int[] carColors;
 	private int[] carBitmapIds;
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_game);
 		
 		objectList = new ArrayList<GameObject>();
 		
 		BitmapContainer.setResources(getResources());
 		
 		BitmapContainer.add(R.drawable.map);
 		BitmapContainer.add(R.drawable.garage_backwall);
 		BitmapContainer.add(R.drawable.garage_port_aaben);
 		BitmapContainer.add(R.drawable.garage_port_step1);
 		BitmapContainer.add(R.drawable.garage_port_step2);
 		BitmapContainer.add(R.drawable.garage_port_closed);
 		BitmapContainer.add(R.drawable.garage_frontwall);
 		BitmapContainer.add(R.drawable.garage_tag);
 		checkForGarageRecoloring();
 		BitmapContainer.add(R.drawable.ic_launcher);
 		BitmapContainer.add(R.drawable.rock);
 		BitmapContainer.add(R.drawable.barricade);
 		BitmapContainer.add(R.drawable.bump);
 		BitmapContainer.add(R.drawable.car);
 		checkForCarRecoloring();
 		BitmapContainer.add(R.drawable.cat);
 		BitmapContainer.add(R.drawable.trophy);
 		//tempGarageColoredId = BitmapContainer.add(R.drawable.garage_frontwall, 255, 0, 0);
 
 		view = new GameView(this, getResources());
 		setContentView(view);
 		
 		gameThread = new GameThread(objectList, getFragmentManager());
 		recorderThread = new RecorderThread();
 		
 		SetObjects();
 		
 		gameThread.start();
 		recorderThread.start();
 	}
 	private void checkForGarageRecoloring() {
 		checkForGarageColor1();
 		checkForGarageColor2();
 		checkForGarageColor3();
 	}
 	private void checkForGarageColor1() {
 		if (color1 != Color.WHITE) {
 			color1_Ids = new int[7];
 			
 			color1_Ids[0] = BitmapContainer.add(R.drawable.garage_backwall, color1);
 			color1_Ids[1] = BitmapContainer.add(R.drawable.garage_port_aaben, color1);
 			color1_Ids[2] = BitmapContainer.add(R.drawable.garage_port_step1, color1);
 			color1_Ids[3] = BitmapContainer.add(R.drawable.garage_port_step2, color1);
 			color1_Ids[4] = BitmapContainer.add(R.drawable.garage_port_closed, color1);
 			color1_Ids[5] = BitmapContainer.add(R.drawable.garage_frontwall, color1);
 			color1_Ids[6] = BitmapContainer.add(R.drawable.garage_tag, color1);
 		}
 	}
 	private void checkForGarageColor2() {
 		if (color2 != Color.WHITE) {
 			color2_Ids = new int[7];
 			
 			color2_Ids[0] = BitmapContainer.add(R.drawable.garage_backwall, color2);
 			color2_Ids[1] = BitmapContainer.add(R.drawable.garage_port_aaben, color2);
 			color2_Ids[2] = BitmapContainer.add(R.drawable.garage_port_step1, color2);
 			color2_Ids[3] = BitmapContainer.add(R.drawable.garage_port_step2, color2);
 			color2_Ids[4] = BitmapContainer.add(R.drawable.garage_port_closed, color2);
 			color2_Ids[5] = BitmapContainer.add(R.drawable.garage_frontwall, color2);
 			color2_Ids[6] = BitmapContainer.add(R.drawable.garage_tag, color2);
 		}
 	}	
 	private void checkForGarageColor3() {
 		if (color3 != Color.WHITE) {
 			color3_Ids = new int[7];
 			
 			color3_Ids[0] = BitmapContainer.add(R.drawable.garage_backwall, color3);
 			color3_Ids[1] = BitmapContainer.add(R.drawable.garage_port_aaben, color3);
 			color3_Ids[2] = BitmapContainer.add(R.drawable.garage_port_step1, color3);
 			color3_Ids[3] = BitmapContainer.add(R.drawable.garage_port_step2, color3);
 			color3_Ids[4] = BitmapContainer.add(R.drawable.garage_port_closed, color3);
 			color3_Ids[5] = BitmapContainer.add(R.drawable.garage_frontwall, color3);
 			color3_Ids[6] = BitmapContainer.add(R.drawable.garage_tag, color3);
 		}
 	}
 	private void checkForCarRecoloring() {
 		carColors = new int[3];
 		carBitmapIds = new int[3];
 		carColors[0] = color1;
 		if (color1 != Color.WHITE) {
 			carBitmapIds[0] = BitmapContainer.add(R.drawable.car, color1);
 		}
 		carColors[1] = color2;
 		if (color2 != Color.WHITE) {
 			carBitmapIds[1] = BitmapContainer.add(R.drawable.car, color2);
 		}
 		carColors[2] = color3;
 		if (color3 != Color.WHITE) {
 			carBitmapIds[2] = BitmapContainer.add(R.drawable.car, color3);
 		}
 	}
 	
 	@Override
 	protected void onDestroy() {
 		super.onDestroy();
 		gameThread.stopRunning();
 		recorderThread.recording = false;
 		GameInfo.win=false;
 		BitmapContainer.clear();
 	}
 	
 	public void AddObjects() { 
 		Random rand = new Random();
 		int i;
 		int numberOfObjects = GameInfo.numberOfObstacles;
 		int[][] roadObstacles;
 		roadObstacles = ObjectPlacement.objectPlacement(numberOfObjects);
 		for(i=0;i<numberOfObjects;i++){
 			int obstaclesNumber = Math.abs(rand.nextInt()%4);
 			if (obstaclesNumber==0){
 				objectList.add(new Bump(roadObstacles[i][0], roadObstacles[i][1]));
 			}
 			if (obstaclesNumber==1){
 				objectList.add(new Cat(roadObstacles[i][0], roadObstacles[i][1]));
 			}
 			if (obstaclesNumber==2){
 				objectList.add(new Barricade(roadObstacles[i][0], roadObstacles[i][1]));
 			}
 			if (obstaclesNumber==3){
 				objectList.add(new Rock(roadObstacles[i][0], roadObstacles[i][1]));
 			}
 		}
 		objectList.add(new Car(MapDivider.mapYStart + MapDivider.obstacleSpace + MapDivider.totalObstacleHeight, GameInfo.carSpeed, carColors, carBitmapIds));
 		
 		ArrayList<Integer> laneNumbers = new ArrayList<Integer>();
 		laneNumbers.add(1);
 		laneNumbers.add(2);
 		laneNumbers.add(3);
 		int arrayNumber = 0;
 		
 		arrayNumber = rand.nextInt(laneNumbers.size());
 		objectList.add(new Garage(laneNumbers.get(arrayNumber), 6, color1, color1_Ids));
 		laneNumbers.remove(arrayNumber);
 
 		arrayNumber = rand.nextInt(laneNumbers.size());
 		objectList.add(new Garage(laneNumbers.get(arrayNumber), 6, color2, color2_Ids));
 		laneNumbers.remove(arrayNumber);
 		
 		arrayNumber = rand.nextInt(laneNumbers.size());
 		objectList.add(new Garage(laneNumbers.get(arrayNumber), 6, color3, color3_Ids));
 		laneNumbers.remove(arrayNumber);
 		
 		SetObjects();
 	}
 	public void SetObjects() {
 		gameThread.SetObjects(objectList);
 		view.SetObjects(objectList);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		getMenuInflater().inflate(R.menu.game, menu);
 		return true;
 	}
 }
