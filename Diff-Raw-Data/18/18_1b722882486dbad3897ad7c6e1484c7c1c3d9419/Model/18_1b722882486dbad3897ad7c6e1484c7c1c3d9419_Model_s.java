 
 public class Model {
         
     // Constructor
     public Model() {
         XSIZE = 300;
         YSIZE = 300;
         XRES = 30;
         YRES = 30;
         // Create a gameMap with some buffer with zeroes.
         gameMap = new int[XSIZE + XRES][YSIZE + YRES];
         gameState = new int[XRES][YRES];
         initGameMap();
         // Set intial character pos
         currentPosX = (XSIZE + XRES)/2;
         currentPosY = (YSIZE + YRES)/2;
         gameMap[currentPosX][currentPosY] = 2;
         // Init some random walls on the map
         gameMap[125][125] = 1;
         gameMap[125][126] = 1;
         gameMap[125][127] = 1;
         gameMap[125][128] = 1;
         gameMap[125][129] = 1;
         gameMap[125][130] = 1;
         gameMap[125][131] = 1;
     }
 
     // Returns current state of the game
     public int[][] getGameState() {
         int x = currentPosX - XRES/2;
         int y = currentPosY - YRES/2;
         for (int i=0; i<XRES; i++) {
             for (int j=0; j<YRES; j++) {
                 gameState[i][j] = gameMap[x + i][y + j];
             }
         }
         return gameState;
     }
     
     // Changes the game state. Called by controller.
     public void processInput (String event) {
         switch (event) {
             case "w": updateCurrentPosition(currentPosX, currentPosY + 1);
             break;
             case "a": updateCurrentPosition(currentPosX - 1, currentPosY);
             break;
             case "s": updateCurrentPosition(currentPosX, currentPosY - 1);
             break;
             case "d": updateCurrentPosition(currentPosX + 1, currentPosY);
             break;
         }
     }
    
     
     
     private void initGameMap() {
         for (int i=0; i<XSIZE + XRES; i++) {
             for (int j=0; j<YSIZE + YRES; j++) {
                 gameMap[i][j]=0;
             }
         }
     }
     
     // Method to update current position of protagonist
     private void updateCurrentPosition (int newX, int newY) {
         // Check if x position is ok
         if ( (XSIZE - newX) >= XRES/2 && (newX >= XRES/2) ) {
             currentPosX = newX;
         }
         // Check if y position is ok
         if ( (YSIZE - newY) >= YRES/2 && (newY >= YRES/2) ) {
             currentPosY = newY;
         } 
     }
     
     // Constants that determine size of playing field in tiles.
     private int XSIZE;
     private int YSIZE;
     
     // Constants that determine VIEWING field in tiles.
     private int XRES;
     private int YRES;
     
     // Variables for storing gamestates
     private int[][] gameMap;
     private int[][] gameState;
     private int currentPosX;
     private int currentPosY;
 }
