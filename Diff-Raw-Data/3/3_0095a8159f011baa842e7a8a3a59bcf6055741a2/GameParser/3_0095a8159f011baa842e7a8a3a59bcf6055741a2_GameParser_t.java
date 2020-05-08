 package edu.pugetsound.vichar.ar;
 
 import java.util.Iterator;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class GameParser {
 	
 	private static final String TURRET_NAMESPACE = "turrets";
     private static final String TURRETBULLET_NAMESPACE = "turretsBullets";
     private static final String FIREBALL_NAMESPACE = "fireballs";
     private static final String MINION_NAMESPACE = "minions";
     private static final String BATTERY_NAMESPACE = "batteries";
     private static final String PLAYER_NAMESPACE = "player";
     private static final String EYEBALL_NAMESPACE = "eyeballs";
     private static final String PLATFORM_NAMESPACE = "platforms";
     private static final String POSITION_NAMESPACE = "position";
     private static final String ROTATION_NAMESPACE = "rotation";
     
   //JSON parsing
     
     protected static boolean updated = false;
     protected static final int OBJ_SIZE = 7; 	// the number of array positions to use to represent a game object.
     protected static float[] poseData;
     
     //Game Board
     static int xTiles = 9; //number of board tiles in the x direction // don't enter zeros!!!
     static int yTiles = 9;// number of tiles in the y direction // don't enter zeros!!!
     static boolean[][] board = new boolean[xTiles][yTiles];
     static int[] tileCoordinate = new int[2];
     private static final float[] THEIR_BOARD_DIMENSIONS = {2000.0f, 2000.0f}; // don't enter zeros!!!
     private static boolean freshBoard = false;
     
     /**
      * Parse the engineState JSONObject into a float array in ARGameRender.
      * 
      * @throws JSONException
      */
     protected static void parseEngineState(JSONObject engineState, String deviceUUID) throws JSONException
     {
     	poseData = new float[(xTiles * yTiles + 0)* OBJ_SIZE]; // ZERO IS IN THERE FOR TESTING.
     	int count;
     	if(freshBoard)
     	{
     		count = xTiles * yTiles * OBJ_SIZE;
     	}
     	else
     	{
         	count = 0; 
     	}
 //    	generateBoard();
 
     	JSONObject platforms = engineState.optJSONObject(PLATFORM_NAMESPACE);
     	if(platforms != null)
     	{
     		// TODO do something with the platforms
     		// like delete them from board.
     	}
     	if(freshBoard)
     	{
     		freshBoard = false;
     	}
     	else
     	{
     		count = loadBoard(board, count);
     	}
     	
     	//will opt returning null clear the objects?
     	JSONObject turrets = engineState.optJSONObject(TURRET_NAMESPACE);
     	if(turrets != null){
     		count = loadObject(turrets, 1.0f, count, deviceUUID, false);
     	}
     	// TODO change type indices
     	JSONObject turretBullets = engineState.optJSONObject(TURRETBULLET_NAMESPACE);
     	if(turretBullets != null){
     		count = loadObject(turretBullets, 2.0f, count, deviceUUID, false);
     	}
 
     	JSONObject fireballs = engineState.optJSONObject(FIREBALL_NAMESPACE);
     	if(fireballs != null){
     		count = loadObject(fireballs, 3.0f, count, deviceUUID, false);
     	}
 
     	JSONObject minions = engineState.optJSONObject(MINION_NAMESPACE);
     	if(minions != null){
     		count = loadObject(minions, 4.0f, count, deviceUUID, false);
     	}
 
     	JSONObject batteries = engineState.optJSONObject(BATTERY_NAMESPACE);
     	if(batteries != null){
     		count = loadObject(batteries, 5.0f, count, deviceUUID, true);
     	}
 
     	JSONObject player = engineState.optJSONObject(PLAYER_NAMESPACE);
     	// load player 
     	if(player != null)
     	{
     		DebugLog.LOGI(player.toString());
     	   	if( count + OBJ_SIZE >= poseData.length)
     	   	{
     	   		int newLen = poseData.length * 2;
         		resizeArray(newLen);
         	}
     	   	poseData[count++] = 6.0f; // TODO use enums to represent the types of gameobjects.
     		count = parsePosition(player.getJSONObject(POSITION_NAMESPACE), count);
     		try{
         		count = parseRotaion(player.getJSONObject(ROTATION_NAMESPACE), count);
     		}
     		catch(JSONException e)
     		{
     			DebugLog.LOGI("Object has no rotation. setting to 0,0,0.");
     			poseData[count++] = 0.0f;
     			poseData[count++] = 0.0f;
     			poseData[count ++] = 0.0f;    		}
     		updated = true;
 //    		DebugLog.LOGI( "Parse:" + player.toString());
     	}
     	else DebugLog.LOGW("No Player");
     	
 		JSONObject eyeballs = engineState.optJSONObject(EYEBALL_NAMESPACE);
     	if(eyeballs != null)
     	{
     		count = loadObject(eyeballs, 7.0f, count, deviceUUID, false);
     	}
     }
 
     
 	/**
      * A helper method to load the object in the array
      * @param type
      * @param typeIndex
      * @param i
      * @return
      * @throws JSONException
      */
     private static int loadObject(JSONObject type, float typeIndex, int i, String deviceUUID, boolean isBattery) throws JSONException
     {
     	Iterator<String> objItr = type.keys();
 
     	while( objItr.hasNext())
     	{
     		String thisEye = objItr.next();
     		JSONObject obj = type.getJSONObject(thisEye);
     		if (deviceUUID.equals(thisEye))
     		{
    			DebugLog.LOGI("Recognized own eye");
     			return i;
     		}
     		if( i + OBJ_SIZE >= poseData.length)
     		{
     			int newLen = poseData.length * 2;
     			//poseData = poseData.
     			resizeArray(newLen);
     		}
     		else
     		{
     			poseData[i++] = typeIndex; // TODO use enums to represent the types of gameobjects.
     			i = parsePosition(obj.getJSONObject(POSITION_NAMESPACE), i);
     			try
     			{
     				i = parseRotaion(obj.getJSONObject(ROTATION_NAMESPACE), i);
     			}
     			catch(JSONException e)
     			{
     				DebugLog.LOGW("Object has no rotation. setting to 0,0,0.");
         			poseData[i++] = 0.0f;
         			poseData[i++] = 0.0f;
         			poseData[i++] = 0.0f;
     			}
     			updated = true;
     		}
     	}
     	return i;
     }
     
     /**
      * loads position JSON data into poseData array.
      * @param xyz
      * @param i
      * @throws JSONException
      */
     private static int parsePosition(JSONObject xyz, int i) throws JSONException
     {
     	poseData[i++] = Float.parseFloat(xyz.getString("x"));
     	poseData[i++] = Float.parseFloat(xyz.getString("y"));
     	poseData[i++] = Float.parseFloat(xyz.getString("z"));
     	return i;
     }
     
     /**
      * loads rotation JSON data into poseData array. Designed to be called immediately after parsePosition.
      * @param xyz
      * @param i
      * @throws JSONException
      */
     private static int parseRotaion(JSONObject xyz, int i) throws JSONException
     {
     	poseData[i++] = Float.parseFloat(xyz.getString("x"));
     	poseData[i++] = Float.parseFloat(xyz.getString("y"));
     	poseData[i++] = Float.parseFloat(xyz.getString("z"));
     	return i;
     }
 
     private static void resizeArray (int newSize) 
     {
     	float[] tempArray = new float[newSize];
     	int preserveSize = Math.min(poseData.length, newSize);
     	for(int i=0; i<preserveSize; i++)
     	{
     		tempArray[i] = poseData[i];
     	}
     	poseData = new float[newSize];
     	for(int i=0; i<preserveSize; i++)
     	{
     		poseData[i] = tempArray[i];
     	} 
     }
     
     private static int loadBoard(boolean[][] currentBoard, int count) {
 		// TODO Auto-generated method stub
 		float tilesX = THEIR_BOARD_DIMENSIONS[0]/xTiles;
 		float tilesY = THEIR_BOARD_DIMENSIONS[1]/yTiles;
 		float xPos = (THEIR_BOARD_DIMENSIONS[0]/2) - tilesX / 2;
 		float yPos = (THEIR_BOARD_DIMENSIONS[1]/2) - tilesY / 2;
 		float tempY = yPos;
 		
 		if(count + (xTiles*yTiles*OBJ_SIZE) >= poseData.length)
 		{
 			int newLen = poseData.length * 2 + (xTiles*yTiles*OBJ_SIZE);
 			resizeArray(newLen);
 		}
 		for(int i=0; i < xTiles; i++)
 		{
 			for(int j = 0; j < yTiles; j++)
 			{
 				if(currentBoard[i][j])
 				{
 					poseData[count++] = 8.0f; //TODO use enums or change the hard coded id's
 					poseData[count++] = xPos;
 					poseData[count++] = tempY;
 					tempY -= tilesY;
 					poseData[count++] = 0.0f; // z position
 					poseData[count++] = 0.0f; // x rotation
 					poseData[count++] = 0.0f; // y rotation
 					poseData[count++] = 0.0f; // z rotation
 				}
 				else
 				{
 					tempY -= tilesY;
 				}
 			}
 			xPos -= tilesX;
 			tempY = yPos;
 		}
 		return count;
 	}
 
 	static void generateBoard() {
    	poseData = new float[(xTiles * yTiles + 1)* OBJ_SIZE]; // the hoard coded digit is space for game objects.
 		for(int i = 0; i < xTiles; i++)
 		{
 			for(int j = 0; j < yTiles; j++)
 			{
 				board[i][j] = true;
 			}
 		}
 		loadBoard(board, 0);
 		freshBoard = true;
 		updated = true;
 	}
 
 }
