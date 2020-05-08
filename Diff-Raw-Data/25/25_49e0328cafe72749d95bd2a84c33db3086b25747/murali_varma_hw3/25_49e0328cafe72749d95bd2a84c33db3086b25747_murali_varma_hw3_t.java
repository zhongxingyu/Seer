 import processing.core.*; 
 import processing.data.*; 
 import processing.event.*; 
 import processing.opengl.*; 
 
 import java.util.HashMap; 
 import java.util.ArrayList; 
 import java.io.BufferedReader; 
 import java.io.PrintWriter; 
 import java.io.InputStream; 
 import java.io.OutputStream; 
 import java.io.IOException; 
 
 public class murali_varma_hw3 extends PApplet {
 
 //constants
int NUM_HORIZONTAL_CELLS = 120;
int NUM_VERTICAL_CELLS = 120;
int CELL_HEIGHT = 5;
int CELL_WIDTH = 5;
 int CONTROLS_WIDTH = 200;
 
 float RU = 0.082f;
 float RV = 0.041f;
 
 int U_MODE = 0;
 int V_MODE = 1;
 float f = 0.035f;
 float k = 0.0625f;
float dt = 3;
 
 //variables
 float[][] u;
 float[][] v;
 int[][] neighbors = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
 
 int mode = U_MODE;
 
 float minColor;
 float maxColor;
 
 public void setup() {
 	// frameRate(100);
 	size(NUM_HORIZONTAL_CELLS * CELL_WIDTH + CONTROLS_WIDTH, NUM_VERTICAL_CELLS * CELL_HEIGHT);
 	background(0);
 	noStroke();
 
 	//initialize variables
 	u = new float[NUM_VERTICAL_CELLS][NUM_HORIZONTAL_CELLS];
 	v = new float[NUM_VERTICAL_CELLS][NUM_HORIZONTAL_CELLS];
 
 	initCells();
 }
 
 public void initCells() {
 	int horizontalOffset = 20;
 	int verticalOffset = 20;
 	int blockWidth = 10;
 	int blockHeight = 10;
 
 	for(int i = 0; i < NUM_VERTICAL_CELLS; i++) {
 		for(int j = 0; j < NUM_HORIZONTAL_CELLS; j++) {
 			if (i >= verticalOffset && i < verticalOffset + blockHeight && j >= horizontalOffset && j < horizontalOffset + blockWidth) {
 				u[i][j] = 0.5f;
 				v[i][j] = 0.25f;
 			}
 			else {
 				u[i][j] = 1.0f;
 				v[i][j] = 0.0f;
 			}
 		}
 	}
 }
 
 public void draw() {
 	// println("Before: " + u[19][20] + ", " + u[21][20] + ", " + u[20][19] + ", " + u[20][21] + " ::: " + u[20][20]);
 	doDiffusion();
	// println("After Diffusion: " + u[19][20] + ", " + u[21][20] + ", " + u[20][19] + ", " + u[20][21] + " ::: " + u[20][20]);
 	doReaction();
	// println("After Reaction: " + u[19][20] + ", " + u[21][20] + ", " + u[20][19] + ", " + u[20][21] + " ::: " + u[20][20]);
 	setColorRange();
	// minColor = 0;
	// maxColor = 1;
 	drawCells();
 	// println(minColor+ ", " + maxColor);
 }
 
 public void setColorRange() {
 	minColor = 10000;
 	maxColor = 0;
 	if (mode == U_MODE) {
 		for (int i = 0; i < NUM_VERTICAL_CELLS; i++) {
 			for (int j = 0; j < NUM_HORIZONTAL_CELLS; j++) {
 				minColor = min(minColor, u[i][j]);
 				maxColor = max(maxColor, u[i][j]);
 			}
 		}	
 	}
 	else {
 		for (int i = 0; i < NUM_VERTICAL_CELLS; i++) {
 			for (int j = 0; j < NUM_HORIZONTAL_CELLS; j++) {
 				minColor = min(minColor, v[i][j]);
 				maxColor = max(maxColor, v[i][j]);
 			}
 		}	
 	}
 }
 
 public void drawCells() {
 	float range = maxColor - minColor;
 	for (int i = 0; i < NUM_VERTICAL_CELLS; i++) {
 		for (int j = 0; j < NUM_HORIZONTAL_CELLS; j++) {
 			if (range == 0) {
 				drawCell(i, j, 0.5f);
 			}
 			else if (mode == U_MODE){
 				drawCell(i, j, (u[i][j] - minColor) / range);
 			}
 			else{
 				drawCell(i, j, (v[i][j] - minColor) / range);
 			}
 		}
 	}
 }
 
 public void drawCell(int i, int j, float col) {
 	fill(255 * col);
 	rect (CELL_WIDTH * j, CELL_HEIGHT * i, CELL_WIDTH, CELL_HEIGHT);
 }
 
 public void doDiffusion() {
 	float[][] u2 = new float[NUM_VERTICAL_CELLS][NUM_HORIZONTAL_CELLS];
 	float[][] v2 = new float[NUM_VERTICAL_CELLS][NUM_HORIZONTAL_CELLS];
 
 	for (int i = 0; i < NUM_VERTICAL_CELLS; i++) {
 		for (int j = 0; j < NUM_HORIZONTAL_CELLS; j++) {
 			float sumU = 0;
 			float sumV = 0;
 			for (int l = 0; l < neighbors.length; l++) {
 				int y = (i + neighbors[l][1] + NUM_VERTICAL_CELLS) % NUM_VERTICAL_CELLS;
 				int x = (j + neighbors[l][0] + NUM_HORIZONTAL_CELLS) % NUM_HORIZONTAL_CELLS;
 				sumU += u[y][x];
 				sumV += v[y][x];
 			}
 			u2[i][j] = u[i][j] + dt * RU * (sumU - 4 * u[i][j]);
 			v2[i][j] = v[i][j] + dt * RV * (sumV - 4 * v[i][j]);
 		}
 	}
 	//copy new array values into original array
 	for (int i = 0; i < NUM_VERTICAL_CELLS; i++) {
 		for (int j = 0; j < NUM_HORIZONTAL_CELLS; j++) {
 			u[i][j] = u2[i][j];
 			v[i][j] = v2[i][j];
 		}
 	}
 }
 
 public void doReaction() {
 	for (int i = 0; i < NUM_VERTICAL_CELLS; i++) {
 		for (int j = 0; j < NUM_HORIZONTAL_CELLS; j++) {
			float uvv = u[i][j] * v[i][j] * v[i][j];
			u[i][j] += dt * (-1 * uvv + f * (1 - u[i][j]));
			v[i][j] += dt * (uvv - v[i][j] * (f + k));
 		}
 	}
 }
   static public void main(String[] passedArgs) {
     String[] appletArgs = new String[] { "murali_varma_hw3" };
     if (passedArgs != null) {
       PApplet.main(concat(appletArgs, passedArgs));
     } else {
       PApplet.main(appletArgs);
     }
   }
 }
