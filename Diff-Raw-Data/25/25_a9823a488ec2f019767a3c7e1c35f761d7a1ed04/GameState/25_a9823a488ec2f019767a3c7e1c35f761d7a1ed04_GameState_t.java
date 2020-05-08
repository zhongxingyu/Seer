 package in.liquidmetal.dubsteptetris;
 
 import android.opengl.GLES20;
 import android.util.Log;
 
 import java.util.Date;
 
 /**
  * Created by utkarsh on 26/5/13.
  */
 public class GameState {
     static final float ARENA_WIDTH = 720.0f;
     static final float ARENA_HEIGHT = 1280.0f;
 
     static final float BOARD_LEFT = 100;
     static final float BOARD_WIDTH = 500;
     static final float BOARD_HEIGHT = 1000;
     static final float BOARD_BOTTOM = 100;
 
     static final int TETRAMINO_EMPTY = -1;
     static final int TETRAMINO_I = 0;
     static final int TETRAMINO_L = 1;
     static final int TETRAMINO_J = 2;
     static final int TETRAMINO_O = 3;
     static final int TETRAMINO_T = 4;
     static final int TETRAMINO_S = 5;
     static final int TETRAMINO_Z = 6;
     static final int TETRAMINO_TOTAL = 7;
 
     static final int numCellsX = 10;
     static final int numCellsY = 20;
 
     static final int hiddenRowFirst = 21;
     static final int hiddenRowSecond = 20;
 
     private long lastRenderTime;
     private long lastDropTime;
 
     private long dropRate = 2500;          // Number of miliseconds to pass before dropping one level
     private int currentTetramino;
     private int nextTetramino;
     private int currentTetraminoY;
 
     private static int[] scoreTypes = {0, 40, 100, 300, 1200};
     private int score = 0;
     private int level = 1;
 
 
 
     private int[][] board_representation = new int[numCellsY+2][numCellsX];
     private AlignedRect[][] board = new AlignedRect[numCellsY][numCellsX];
     static float[][] tetraminoColors = new float[TETRAMINO_TOTAL][3];
 
     // Have a 4x4 grid to represent the next piece
     private AlignedRect[][] nextPiece = new AlignedRect[4][4];
 
     private static float[] colorNeutral = {0.1f, 0.1f, 0.9f};
     private static float[] colorDanger = {0.9f, 0.1f, 0.1f};
 
     private float[] backgroundColor;
 
     private int touchDownX, touchDownY;
     private int tetrDownX, tetrDownY;
     private int rotationStage = 0;      // Stages can be 0, 1, 2, 3
     private long lastRotateTimestamp = 0;
 
     // Each stage is specified -----> (not vvvv)
     private int[][][] stagesI = {{{0, 0, 0, 0}, {1, 1, 1, 1}, {0, 0, 0, 0}, {0, 0, 0, 0}},
                                  {{0, 0, 1, 0}, {0, 0, 1, 0}, {0, 0, 1, 0}, {0, 0, 1, 0}},
                                  {{0, 0, 0, 0}, {0, 0, 0, 0}, {1, 1, 1, 1}, {0, 0, 0, 0}},
                                  {{0, 1, 0, 0}, {0, 1, 0, 0}, {0, 1, 0, 0}, {0, 1, 0, 0}}};
 
     private int[][][] stagesL = {{{0, 0, 1}, {1, 1, 1}, {0, 0, 0}},
                                  {{0, 1, 0}, {0, 1, 0}, {0, 1, 1}},
                                  {{0, 0, 0}, {1, 1, 1}, {1, 0, 0}},
                                  {{1, 1, 0}, {0, 1, 0}, {0, 1, 0}}};
 
     private int[][][] stagesJ = {{{1, 0, 0}, {1, 1, 1}, {0, 0, 0}},
                                  {{0, 1, 1}, {0, 1, 0}, {0, 1, 0}},
                                  {{0, 0, 0}, {1, 1, 1}, {0, 0, 1}},
                                  {{0, 1, 0}, {0, 1, 0}, {1, 1, 0}}};
 
     private int[][][] stagesO = {{{1, 1}, {1, 1}},
                                  {{1, 1}, {1, 1}},
                                  {{1, 1}, {1, 1}},
                                  {{1, 1}, {1, 1}}};
 
     private int[][][] stagesT = {{{0, 1, 0}, {1, 1, 1}, {0, 0, 0}},
                                  {{0, 1, 0}, {0, 1, 1}, {0, 1, 0}},
                                  {{0, 0, 0}, {1, 1, 1}, {0, 1, 0}},
                                  {{0, 1, 0}, {1, 1, 0}, {0, 1, 0}}};
 
     private int[][][] stagesS = {{{0, 1, 1}, {1, 1, 0}, {0, 0, 0}},
                                  {{0, 1, 0}, {0, 1, 1}, {0, 0, 1}},
                                  {{0, 0, 0}, {0, 1, 1}, {1, 1, 0}},
                                  {{1, 0, 0}, {1, 1, 0}, {0, 1, 0}}};
 
     private int[][][] stagesZ = {{{1, 1, 0}, {0, 1, 1}, {0, 0, 0}},
                                  {{0, 0, 1}, {0, 1, 1}, {0, 1, 0}},
                                  {{0, 0, 0}, {1, 1, 0}, {0, 1, 1}},
                                  {{0, 1, 0}, {1, 1, 0}, {1, 0, 0}}};
 
     int[][][][] stages = {stagesI, stagesL, stagesJ, stagesO, stagesT, stagesS, stagesZ};
 
     private ColorAnimator animBgDanger;
 
     private GameSurfaceRenderer myRenderer;
    private boolean haltForceDrop = false;
 
     // Used for the true tetris random generator
     private boolean[] currentBag = new boolean[TETRAMINO_TOTAL];
 
     public void setRenderer(GameSurfaceRenderer renderer) {
         myRenderer = renderer;
     }
 
     public void initializeBoard() {
         initializeColors();
         float cellSize = BOARD_HEIGHT / numCellsY;
         for(int y=0;y<numCellsY;y++)
             for(int x=0;x<numCellsX;x++) {
                 board[y][x] = new AlignedRect();
                 board[y][x].setColor(0,0,0);
                 board[y][x].setPosition(BOARD_LEFT + x*cellSize - cellSize/2, BOARD_BOTTOM+y*cellSize - cellSize/2);
                 board[y][x].setScale(cellSize,cellSize);
                 board_representation[y][x] = TETRAMINO_EMPTY;
             }
 
         for(int y=numCellsY;y<numCellsY+2;y++)
             for(int x=0;x<numCellsX;x++) {
                 board_representation[y][x] = TETRAMINO_EMPTY;
             }
 
         for(int y=0;y<4;y++) {
             for(int x=0;x<4;x++) {
                 nextPiece[y][x] = new AlignedRect();
                 nextPiece[y][x].setColor(0,0,0);
                 nextPiece[y][x].setScale(32f, 32f);
                 nextPiece[y][x].setPosition(BOARD_LEFT+BOARD_WIDTH+x*32-34, BOARD_BOTTOM+BOARD_HEIGHT-256+y*32);
             }
         }
 
         /**** Initial blocks for testing around
         board_representation[0][2] = TETRAMINO_I;
         board_representation[0][8] = TETRAMINO_L;
 
         board_representation[3][0] = TETRAMINO_S;
         board_representation[3][1] = TETRAMINO_T;
         board_representation[3][2] = TETRAMINO_L;
         board_representation[3][3] = TETRAMINO_O;*/
 
         currentTetramino = 3;
         currentTetraminoY = 22;
         nextTetramino = (int)(Math.random()*7);
         score = 0;
         backgroundColor = colorNeutral.clone();
 
         animBgDanger = new ColorAnimator(2000, colorNeutral, colorDanger);
 
         // Nothing has appeared... yet
         for(int i=0;i<TETRAMINO_TOTAL;i++)
             currentBag[i] = false;
 
         createNewTetramino();
     }
 
     public long getScore() {
         return score;
     }
 
     private void increaseScore(int change) {
         score = score + change;
         myRenderer.OnScoreChange(change);
     }
 
     public void clearScreen() {
         GLES20.glClearColor(backgroundColor[0], backgroundColor[1], backgroundColor[2], 1.0f);
         GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
     }
 
     private void spawnTetramino() {
         switch(currentTetramino) {
             case TETRAMINO_I:
                 // Test pre-fall intersection
                 if(board_representation[hiddenRowSecond][3] != TETRAMINO_EMPTY ||
                         board_representation[hiddenRowSecond][4] != TETRAMINO_EMPTY ||
                         board_representation[hiddenRowSecond][5] != TETRAMINO_EMPTY ||
                         board_representation[hiddenRowSecond][6] != TETRAMINO_EMPTY) {
                     initializeBoard();
                     break;
                 }
 
 
                 board_representation[hiddenRowSecond][3] = TETRAMINO_I + TETRAMINO_TOTAL;
                 board_representation[hiddenRowSecond][4] = TETRAMINO_I + TETRAMINO_TOTAL;
                 board_representation[hiddenRowSecond][5] = TETRAMINO_I + TETRAMINO_TOTAL;
                 board_representation[hiddenRowSecond][6] = TETRAMINO_I + TETRAMINO_TOTAL;
                 break;
 
             case TETRAMINO_J:
                 if(board_representation[hiddenRowSecond][3] != TETRAMINO_EMPTY ||
                         board_representation[hiddenRowSecond][4] != TETRAMINO_EMPTY ||
                         board_representation[hiddenRowSecond][5] != TETRAMINO_EMPTY ||
                         board_representation[hiddenRowFirst][3] != TETRAMINO_EMPTY) {
                     initializeBoard();
                     break;
                 }
 
                 board_representation[hiddenRowSecond][3] = TETRAMINO_J + TETRAMINO_TOTAL;
                 board_representation[hiddenRowSecond][4] = TETRAMINO_J + TETRAMINO_TOTAL;
                 board_representation[hiddenRowSecond][5] = TETRAMINO_J + TETRAMINO_TOTAL;
                 board_representation[hiddenRowFirst][3] = TETRAMINO_J + TETRAMINO_TOTAL;
                 break;
 
             case TETRAMINO_L:
                 if(board_representation[hiddenRowSecond][3] != TETRAMINO_EMPTY ||
                         board_representation[hiddenRowSecond][4] != TETRAMINO_EMPTY ||
                         board_representation[hiddenRowSecond][5] != TETRAMINO_EMPTY ||
                         board_representation[hiddenRowFirst][5] != TETRAMINO_EMPTY) {
                     initializeBoard();
                     break;
                 }
 
                 board_representation[hiddenRowSecond][3] = TETRAMINO_L + TETRAMINO_TOTAL;
                 board_representation[hiddenRowSecond][4] = TETRAMINO_L + TETRAMINO_TOTAL;
                 board_representation[hiddenRowSecond][5] = TETRAMINO_L + TETRAMINO_TOTAL;
                 board_representation[hiddenRowFirst][5] = TETRAMINO_L + TETRAMINO_TOTAL;
                 break;
 
             case TETRAMINO_O:
                 if(board_representation[hiddenRowSecond][5] != TETRAMINO_EMPTY ||
                         board_representation[hiddenRowSecond][6] != TETRAMINO_EMPTY ||
                         board_representation[hiddenRowFirst][5] != TETRAMINO_EMPTY ||
                         board_representation[hiddenRowFirst][6] != TETRAMINO_EMPTY) {
                     initializeBoard();
                     break;
                 }
 
                 board_representation[hiddenRowFirst][5] = TETRAMINO_O + TETRAMINO_TOTAL;
                 board_representation[hiddenRowFirst][6] = TETRAMINO_O + TETRAMINO_TOTAL;
                 board_representation[hiddenRowSecond][5] = TETRAMINO_O + TETRAMINO_TOTAL;
                 board_representation[hiddenRowSecond][6] = TETRAMINO_O + TETRAMINO_TOTAL;
                 break;
 
             case TETRAMINO_S:
                 if(board_representation[hiddenRowFirst][5] != TETRAMINO_EMPTY ||
                         board_representation[hiddenRowFirst][4] != TETRAMINO_EMPTY ||
                         board_representation[hiddenRowSecond][4] != TETRAMINO_EMPTY ||
                         board_representation[hiddenRowSecond][3] != TETRAMINO_EMPTY) {
                     initializeBoard();
                     break;
                 }
 
                 board_representation[hiddenRowFirst][5] = TETRAMINO_S + TETRAMINO_TOTAL;
                 board_representation[hiddenRowFirst][4] = TETRAMINO_S + TETRAMINO_TOTAL;
                 board_representation[hiddenRowSecond][4] = TETRAMINO_S + TETRAMINO_TOTAL;
                 board_representation[hiddenRowSecond][3] = TETRAMINO_S + TETRAMINO_TOTAL;
                 break;
 
             case TETRAMINO_Z:
                 if(board_representation[hiddenRowFirst][3] != TETRAMINO_EMPTY ||
                         board_representation[hiddenRowFirst][4] != TETRAMINO_EMPTY ||
                         board_representation[hiddenRowSecond][4] != TETRAMINO_EMPTY ||
                         board_representation[hiddenRowSecond][5] != TETRAMINO_EMPTY) {
                     initializeBoard();
                     break;
                 }
 
                 board_representation[hiddenRowFirst][3] = TETRAMINO_Z + TETRAMINO_TOTAL;
                 board_representation[hiddenRowFirst][4] = TETRAMINO_Z + TETRAMINO_TOTAL;
                 board_representation[hiddenRowSecond][4] = TETRAMINO_Z + TETRAMINO_TOTAL;
                 board_representation[hiddenRowSecond][5] = TETRAMINO_Z + TETRAMINO_TOTAL;
                 break;
 
             case TETRAMINO_T:
                 if(board_representation[hiddenRowFirst][4] != TETRAMINO_EMPTY ||
                         board_representation[hiddenRowFirst][3] != TETRAMINO_EMPTY ||
                         board_representation[hiddenRowSecond][4] != TETRAMINO_EMPTY ||
                         board_representation[hiddenRowSecond][5] != TETRAMINO_EMPTY) {
                     initializeBoard();
                     break;
                 }
 
                 board_representation[hiddenRowFirst][4] = TETRAMINO_T + TETRAMINO_TOTAL;
                 board_representation[hiddenRowSecond][3] = TETRAMINO_T + TETRAMINO_TOTAL;
                 board_representation[hiddenRowSecond][4] = TETRAMINO_T + TETRAMINO_TOTAL;
                 board_representation[hiddenRowSecond][5] = TETRAMINO_T + TETRAMINO_TOTAL;
                 break;
         }
         rotationStage = 0;
     }
 
     public void createNewTetramino() {
         currentTetramino = nextTetramino;
         currentBag[currentTetramino] = true;
 
         // Do we need to switch to a new bag?
         boolean newBag = true;
         for(int i=0;i<TETRAMINO_TOTAL;i++) {
             if(!currentBag[i]) {
                 newBag = false;
                 break;
             }
         }
 
         // Generate an initial guess
         nextTetramino = (int)Math.floor(Math.random()*7);
         if(newBag) {
             // Mark everything as 'uncreated'
             for(int i=0;i<TETRAMINO_TOTAL;i++) {
                 currentBag[i] = false;
             }
 
         } else {
             // Keep iterating until we find something that works
             // with the current bag we have
             while(currentBag[nextTetramino]) {
                 nextTetramino = (int)Math.floor(Math.random()*7);
             }
         }
 
         updateNextTetraminoDisplay();
         spawnTetramino();
     }
 
     private void updateNextTetraminoDisplay() {
         // Black out everything
         for(int y=0;y<4;y++) {
             for(int x=0;x<4;x++) {
                 nextPiece[y][x].setColor(0,0,0);
             }
         }
 
         int[][] nextPieceArray = stages[nextTetramino][0];
         float[] color = tetraminoColors[nextTetramino];
 
         for(int y=0;y<nextPieceArray.length;y++) {
             for(int x=0;x<nextPieceArray.length;x++) {
                 if(nextPieceArray[nextPieceArray.length-y-1][x]==1)
                     nextPiece[y][x].setColor(color[0],color[1],color[2]);
             }
         }
     }
 
     public void drawBoard() {
         for(int y=0;y<numCellsY;y++)
             for(int x=0;x<numCellsX;x++)
                 board[y][x].draw();
     }
 
     public void drawNextPiece() {
         for(int y=0;y<4;y++) {
             for(int x=0;x<4;x++) {
                 nextPiece[y][x].draw();
             }
         }
     }
 
     public void calculateNextFrame() {
         long timeNow = new Date().getTime();
 
         if(timeNow - lastDropTime > dropRate) {
             dropTetramino();
         }
 
         // We tetraminos are above the danger line (2nd last line)
         // do the animation!
         boolean isEmpty = true;
         for(int x=0;x<numCellsX;x++) {
             long value = board_representation[16][x];
             if(value>TETRAMINO_EMPTY && value<TETRAMINO_TOTAL) {
                 animBgDanger.start();
                 isEmpty = false;
             }
         }
 
         if(isEmpty)
             animBgDanger.reverse();
 
         animBgDanger.update();
 
         backgroundColor = animBgDanger.getCurrentColor();
     }
 
     private void dropTetramino() {
         boolean canDrop = true;
         for(int y=1;y<numCellsY+2;y++)
             for(int x=0;x<numCellsX;x++) {
                 if(board_representation[y][x] >= TETRAMINO_TOTAL) {
                     if(board_representation[y-1][x] >= 0 && board_representation[y-1][x]<TETRAMINO_TOTAL)
                         canDrop = false;
                 }
             }
 
         if(canDrop) {
             boolean didFirstRowChanges = false;
             for(int y=1;y<numCellsY+2;y++)
                 for(int x=0;x<numCellsX;x++) {
                     if(board_representation[y][x] >= TETRAMINO_TOTAL) {
                         board_representation[y-1][x] = board_representation[y][x];
                         board_representation[y][x] = TETRAMINO_EMPTY;
                         if(y==1)
                             didFirstRowChanges = true;
                     }
                 }
 
             if(didFirstRowChanges) canDrop = false;
         }
 
         if(!canDrop) {
             for(int y=0;y<numCellsY;y++)
                 for(int x=0;x<numCellsX;x++)
                     if(board_representation[y][x]>=TETRAMINO_TOTAL) board_representation[y][x] -= TETRAMINO_TOTAL;
 
             checkLines();
             createNewTetramino();
            haltForceDrop = true;
         }
 
         syncRepresentationAndBoard();
 
         lastDropTime = new Date().getTime();
     }
 
     private void checkLines() {
         int totalLines = 0;
         for(int y=0;y<numCellsY;y++) {
             boolean isLineDone = true;
             for(int x=0;x<numCellsX;x++) {
                 if(board_representation[y][x] == TETRAMINO_EMPTY) {
                     isLineDone = false;
                     break;
                 }
             }
 
             if(isLineDone) {
                 for(int y2=y;y2<numCellsY;y2++) {
                     for(int x=0;x<numCellsX;x++) {
                         board_representation[y2][x] = board_representation[y2+1][x];
                     }
                 }
 
                 y--;
                 totalLines++;
             }
         }
 
         increaseScore(scoreTypes[totalLines]*level);
     }
 
     public void syncRepresentationAndBoard() {
         for(int y=0;y<numCellsY;y++)
             for(int x=0;x<numCellsX;x++) {
                 if(board_representation[y][x] == TETRAMINO_EMPTY) {
                     board[y][x].setColor(0, 0, 0);
                 } else {
                     int colorIndex = board_representation[y][x];
                     if(colorIndex>=TETRAMINO_TOTAL) colorIndex -= TETRAMINO_TOTAL;
 
                     float[] color = tetraminoColors[colorIndex];
                     board[y][x].setColor(color[0], color[1], color[2]);
                 }
             }
 
         generateGhost();
     }
 
     private void generateGhost() {
 
         int[][] currentStage = stages[currentTetramino][rotationStage];
 
         int trueBottom = 0, trueLeft = 0, trueRight = 0;
 
         for(int i=currentStage.length-1;i>=0;i--) {
             boolean isRowEmpty = true;
             for(int j=0;j<currentStage.length;j++) {
                 if(currentStage[i][j]==1) {
                     isRowEmpty = false;
                     trueBottom = i;
                     break;
                 }
             }
 
             if(isRowEmpty==false)
                 break;
         }
 
         for(int j=0;j<currentStage.length;j++) {
             boolean isRowEmpty = true;
             for(int i=0;i<currentStage.length;i++) {
                 if(currentStage[i][j]==1) {
                     isRowEmpty = false;
                     trueLeft = j;
                     break;
                 }
             }
 
             if(!isRowEmpty)
                 break;
         }
 
         for(int j=currentStage.length-1;j>=0;j--) {
             boolean isRowEmpty = true;
             for(int i=0;i<currentStage.length;i++) {
                 if(currentStage[i][j]==1) {
                     isRowEmpty = false;
                     trueRight = j;
                     break;
                 }
             }
 
             if(!isRowEmpty)
                 break;
         }
 
         //if(trueLeft==trueRight)
         //    trueLeft -= 1;
 
         // Template match the location
         outerBreak:for(int y=numCellsY+1;y>=currentStage.length;y--) {
             outerContinue:for(int x=0;x<numCellsX-(trueRight-trueLeft);x++) {
                 for(int i=0;i<currentStage.length;i++) {
                     for(int j=trueLeft;j<=trueRight;j++) {
                         if(currentStage[i][j] == 1 && board_representation[y-i][x+j-trueLeft]!=TETRAMINO_TOTAL+currentTetramino)
                             continue outerContinue;
 
                         if(currentStage[i][j] == 0 && board_representation[y-i][x+j-trueLeft]==TETRAMINO_TOTAL+currentTetramino)
                             continue outerContinue;
                     }
                 }
 
                 Log.d("INFO", "Just testing");
 
                 // If we get here, we've found a match!
                 // Start working our way down from here
                 boolean collides = false;
 
 
                 boolean hitTrueBottom = false;
                 while(!collides && y>=trueBottom) {
                     if(y==trueBottom) {
                         hitTrueBottom = true;
                     }
 
                     for(int i=0;i<currentStage.length && !collides;i++) {
                         if(y-i<0)
                             continue;
                         for(int j=trueLeft;j<=trueRight;j++) {
 
 
                             if(currentStage[i][j] == 1 && board_representation[y-i][x+j-trueLeft] > TETRAMINO_EMPTY && board_representation[y-i][x+j-trueLeft] < TETRAMINO_TOTAL) {
                                 collides = true;
                                 y++;
                                 break;
                             }
                         }
                     }
 
                     if(hitTrueBottom)
                         break;
 
                     if(!collides)
                         y--;
 
                 }
 
                 if(hitTrueBottom && !collides)
                     collides = true;
 
 
                 for(int i=0;i<currentStage.length;i++) {
                     for(int j=trueLeft;j<=trueRight;j++) {
                         if(currentStage[i][j] == 1) {
                             if(y>=numCellsY)
                                 continue;
 
                             // If we're at the original piece itself, no need to shadow it
                             if(board_representation[y-i][x+j-trueLeft] == TETRAMINO_TOTAL+currentTetramino)
                                 continue;
 
                             board[y-i][x+j-trueLeft].setColor(0.198f, 0.200f, 0.200f);
                         }
                     }
                 }
                 break outerBreak;
             }
         }
     }
 
     private void initializeColors() {
         // I - cyanish
         tetraminoColors[TETRAMINO_I][0] = 0.0f;
         tetraminoColors[TETRAMINO_I][1] = 1.0f;
         tetraminoColors[TETRAMINO_I][2] = 0.961f;
 
         // L - orange
         tetraminoColors[TETRAMINO_L][0] = 0.996f;
         tetraminoColors[TETRAMINO_L][1] = 0.82f;
         tetraminoColors[TETRAMINO_L][2] = 0.125f;
 
         // J - blue
         tetraminoColors[TETRAMINO_J][0] = 0.373f;
         tetraminoColors[TETRAMINO_J][1] = 0.714f;
         tetraminoColors[TETRAMINO_J][2] = 0.902f;
 
         // O - Yellow
         tetraminoColors[TETRAMINO_O][0] = 0.882f;
         tetraminoColors[TETRAMINO_O][1] = 1.0f;
         tetraminoColors[TETRAMINO_O][2] = 0.0f;
 
         // T - purple
         tetraminoColors[TETRAMINO_T][0] = 0.514f;
         tetraminoColors[TETRAMINO_T][1] = 0.318f;
         tetraminoColors[TETRAMINO_T][2] = 0.835f;
 
         // S - green
         tetraminoColors[TETRAMINO_S][0] = 0.02f;
         tetraminoColors[TETRAMINO_S][1] = 0.914f;
         tetraminoColors[TETRAMINO_S][2] = 0.733f;
 
         // Z - red
         tetraminoColors[TETRAMINO_Z][0] = 0.875f;
         tetraminoColors[TETRAMINO_Z][1] = 0.169f;
         tetraminoColors[TETRAMINO_Z][2] = 0.314f;
     }
 
     public void updateRenderTime() {
         lastRenderTime = new Date().getTime();
     }
 
     public int[] getTetraminoPosition() {
         // Identifies the currently active tetramino's average
         // X position
 
         int avg[] = new int[2];
         avg[0] = 0;
         avg[1] = 0;
         for(int y=0;y<numCellsY+2;y++)
             for(int x=0;x<numCellsX;x++) {
                 if(board_representation[y][x] >= TETRAMINO_TOTAL) {
                     avg[0] += x;
                     avg[1] += y;
                 }
             }
 
         avg[0] /= 4.0f;
         avg[1] /= 4.0f;
         return avg;
     }
 
     public void signalDown(int posX, int posY) {
         touchDownX = posX;
         touchDownY = posY;
         int[] pos = getTetraminoPosition();
 
         tetrDownX = pos[0];
         tetrDownY = pos[1];
     }
 
     private void handleTranslate(int positionX) {
         int[] newTetrPos = getTetraminoPosition();
 
         int pixelDifference = positionX - touchDownX;
         int cellDifference = (int)(pixelDifference / (ARENA_WIDTH/numCellsX));
 
         int expectedTetrX = tetrDownX + cellDifference;
 
         if(expectedTetrX<newTetrPos[0]) {
             int numShifts = newTetrPos[0] - expectedTetrX;
 
             // Move left
             for(int i=0;i<numShifts;i++) {
                 boolean canShiftLeft = true;
                 for(int y=0;y<numCellsY+2;y++)
                     for(int x=0;x<numCellsX;x++) {
                         if(board_representation[y][x]>=TETRAMINO_TOTAL) {
                             if(x==0) {
                                 canShiftLeft = false;
                                 break;
                             }
 
                             int nextCell = board_representation[y][x-1];
                             if(nextCell>TETRAMINO_EMPTY && nextCell<TETRAMINO_TOTAL) {
                                 canShiftLeft = false;
                                 break;
                             }
                         }
                     }
 
                 if(!canShiftLeft)
                     return;
 
                 for(int y=0;y<numCellsY+2;y++) {
                     for(int x=1;x<numCellsX;x++) {
                         if(board_representation[y][x]>=TETRAMINO_TOTAL) {
                             board_representation[y][x-1] = board_representation[y][x];
                             board_representation[y][x] = TETRAMINO_EMPTY;
                         }
                     }
                 }
 
                 syncRepresentationAndBoard();
             }
         } else if(expectedTetrX>newTetrPos[0]) {
             int numShifts = expectedTetrX - newTetrPos[0];
 
             for(int i=0;i<numShifts;i++) {
                 boolean canShiftRight = true;
                 for(int y=0;y<numCellsY+2;y++)
                     for(int x=0;x<numCellsX;x++) {
                         if(board_representation[y][x]>=TETRAMINO_TOTAL) {
                             if(x==numCellsX-1) {
                                 canShiftRight = false;
                                 break;
                             }
 
                             int nextCell = board_representation[y][x+1];
                             if(nextCell>TETRAMINO_EMPTY && nextCell<TETRAMINO_TOTAL) {
                                 canShiftRight = false;
                                 break;
                             }
                         }
                     }
 
                 if(!canShiftRight)
                     return;
 
                 for(int y=0;y<numCellsY+2;y++) {
                     for(int x=numCellsX-2;x>=0;x--) {
                         if(board_representation[y][x]>=TETRAMINO_TOTAL) {
                             board_representation[y][x+1] = board_representation[y][x];
                             board_representation[y][x] = TETRAMINO_EMPTY;
                         }
                     }
                 }
 
                 syncRepresentationAndBoard();
             }
         }
     }
 
     private void handleDown(int positionY) {
        if(touchDownY>0 && touchDownY-positionY < 20 && !haltForceDrop) {
             dropTetramino();
             syncRepresentationAndBoard();
         }
     }
 
    public void signalUp(int x, int y) {
        haltForceDrop = false;
    }

     private int[] getTrueBounds(int[][] currentStage) {
         int trueBottom = 0, trueLeft = 0, trueRight = 0;
 
         for(int i=currentStage.length-1;i>=0;i--) {
             boolean isRowEmpty = true;
             for(int j=0;j<currentStage.length;j++) {
                 if(currentStage[i][j]==1) {
                     isRowEmpty = false;
                     trueBottom = i;
                     break;
                 }
             }
 
             if(isRowEmpty==false)
                 break;
         }
 
         for(int j=0;j<currentStage.length;j++) {
             boolean isRowEmpty = true;
             for(int i=0;i<currentStage.length;i++) {
                 if(currentStage[i][j]==1) {
                     isRowEmpty = false;
                     trueLeft = j;
                     break;
                 }
             }
 
             if(!isRowEmpty)
                 break;
         }
 
         for(int j=currentStage.length-1;j>=0;j--) {
             boolean isRowEmpty = true;
             for(int i=0;i<currentStage.length;i++) {
                 if(currentStage[i][j]==1) {
                     isRowEmpty = false;
                     trueRight = j;
                     break;
                 }
             }
 
             if(!isRowEmpty)
                 break;
         }

        return new int[]{trueBottom, trueLeft, trueRight};
     }
 
     public void handleRotation(int positionX) {
         // Less than 10ms before the last rotate? Slow down!
         long newTimeStamp = (new Date()).getTime();
         if(newTimeStamp - lastRotateTimestamp<=10) {
             return;
         }
 
         lastRotateTimestamp = newTimeStamp;
 
         int[][][][] stages = {stagesI, stagesL, stagesJ, stagesO, stagesT, stagesS, stagesZ};
         int[][] oldStage = stages[currentTetramino][rotationStage];
         int oldRotationStage = rotationStage;
 
         if(positionX>2*ARENA_WIDTH/3)
             rotationStage += 1;
         else if(positionX<ARENA_WIDTH/3)
             rotationStage -= 1;
 
         if(rotationStage >= 4)
             rotationStage = 0;
 
         if(rotationStage <= -1)
             rotationStage = 3;
 
         int[][] newStage = stages[currentTetramino][rotationStage];
 
         int trueOldBottom = 0, trueOldLeft = 0, trueOldRight = 0;
         int trueNewBottom = 0, trueNewLeft = 0, trueNewRight = 0;
 
         int[] ret = getTrueBounds(oldStage);
         trueOldBottom = ret[0];
         trueOldLeft = ret[1];
         trueOldRight = ret[2];
 
         ret = getTrueBounds(newStage);
         trueNewBottom = ret[0];
         trueNewLeft = ret[1];
         trueNewRight = ret[2];
 
 
 
         // Step 1: Do a sort of template match on the grid to identify the
         // location of the pattern
 
         // Caveat: We'll have to flip the new and old stages vertically (you know why)
         boolean found=false;
         int foundx, foundy;
         outerBreak:for(int y=0;y<numCellsY-oldStage.length+3;y++) {
             outerContinue:for(int x=0;x<numCellsX-oldStage[0].length+1;x++) {
 
                 // First, identify the x,y cooredinates of the old stage
                 for(int i=0;i<oldStage.length;i++) {
                     for(int j=trueOldLeft;j<=trueOldRight;j++) {
                         if(oldStage[oldStage.length-i-1][j-trueOldLeft] == 1 && board_representation[y+i][x+j-trueOldLeft]!=TETRAMINO_TOTAL+currentTetramino)
                             continue outerContinue;
 
                         if(oldStage[oldStage.length-i-1][j-trueOldLeft] == 0 && board_representation[y+i][x+j-trueOldLeft]==TETRAMINO_TOTAL+currentTetramino)
                             continue outerContinue;
 
 
                     }
                 }
 
                 // Ascertain there are no collisions happening
                 boolean colliding = false;
                 for(int i=0;i<newStage.length;i++) {
                     for(int j=0;j<newStage[0].length;j++) {
                         if(newStage[newStage.length-i-1][j] == 1 && oldStage[oldStage.length-i-1][j] == 0 && board_representation[y+i][x+j] != TETRAMINO_EMPTY) {
                             colliding = true;
                             break;
                         }
                     }
                 }
 
                 if(colliding) {
                     // Nothing to do here
                     rotationStage = oldRotationStage;
                     return;
                 }
 
                 for(int i=0;i<oldStage.length;i++) {
                     for(int j=0;j<oldStage[0].length;j++) {
                         if(oldStage[oldStage.length-i-1][j] == 1)
                             board_representation[y+i][x+j] = TETRAMINO_EMPTY;
                     }
                 }
 
                 for(int i=0;i<newStage.length;i++) {
                     for(int j=0;j<newStage[0].length;j++) {
                         if(newStage[newStage.length-i-1][j] == 1)
                             board_representation[y+i][x+j] = TETRAMINO_TOTAL+currentTetramino;
                     }
                 }
                 syncRepresentationAndBoard();
                 break outerBreak;
             }
         }
     }
 
     public void signalMotion(int positionX, int positionY) {
         if(Math.abs(touchDownX-positionX) > Math.abs(touchDownY-positionY)) {
             handleTranslate(positionX);
         } else {
             handleDown(positionY);
         }
     }
 }
