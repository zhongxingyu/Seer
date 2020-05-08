 package level;
 
 /**
  * @author mauriceschleusinger
  * 
  */
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.Random;
 
 public class Generator {
 	/**
 	 * adds a filewriter
 	 */
 	FileWriter fw;
 	/**
 	 * adds a file
 	 */
 	File file;
 	/**
 	 * var for the height of the field
 	 */
 	int fieldheight;
 	/**
 	 * var for the with of the field
 	 */
 	int fieldwith;
 	/**
 	 * char-array for the generation of a map
 	 */
 	char maparray[][];
 
 	/**
 	 * generates a map based on the given fieldheight and fieldwith
 	 * 
 	 * @param fieldheight
 	 * @param fieldwith
 	 */
 	public void generateMap(int fieldheight, int fieldwith) {
 		this.fieldheight = fieldheight;
 		this.fieldwith = fieldwith;
 		this.maparray = new char[this.fieldheight][this.fieldwith];
 		this.file = new File("src/ressources/maps/genMap");
 		try {
 			this.fw = new FileWriter(this.file);
 
 			// create map-frame
 			for (int j = 0; j < this.fieldheight; j++) {
 				this.maparray[j][0] = '2';
 				if ((j >= (this.fieldheight - 1)) || (j == 0)) {
 					for (int i = 1; i < (this.fieldwith - 1); i++) {
 						this.maparray[j][i] = '2';
 					}
 				} else {
 					for (int i = 1; i < (this.fieldwith - 1); i++) {
 						this.maparray[j][i] = '1';
 					}
 				}
 				this.maparray[j][this.fieldwith - 1] = '2';
 			}
 			// create indestructible blocks
 			for (int j = 1; j < (this.fieldheight - 2); j += 3) {
 				for (int i = 1; i < (this.fieldwith - 2); i += 3) {
 					int rnd = new Random().nextInt(13);
 					if (rnd <= 1) {
 						// single block
 						this.maparray[j + rnd + 1][i + rnd + 1] = '2';
 					} else if (rnd <= 3) {
 						// L
 						this.maparray[j + 1][i + 1] = '2';
 						this.maparray[j + 2][i + 1] = '2';
 						this.maparray[j + 2][i + 2] = '2';
 					} else if (rnd <= 5) {
 						// L
 						this.maparray[j + 1][i + 2] = '2';
 						this.maparray[j + 1][i + 1] = '2';
 						this.maparray[j + 2][i + 2] = '2';
 					} else if (rnd <= 7) {
 						// diagonal
 						this.maparray[j + 1][i + 1] = '2';
 						this.maparray[j + 2][i + 2] = '2';
 					} else if (rnd <= 9) {
 						this.maparray[j + 1][i + 2] = '2';
 						this.maparray[j + 2][i + 1] = '2';
 					} else {
 						// square
 						this.maparray[j + 1][i + 1] = '2';
 						this.maparray[j + 1][i + 2] = '2';
 						this.maparray[j + 2][i + 1] = '2';
 						this.maparray[j + 2][i + 2] = '2';
 					}
 				}
 			}
 			// create the players
			int rnd = new Random().nextInt((this.fieldheight - 3)) + 1;
 			this.maparray[1][rnd - 1] = '3';
 			this.maparray[this.fieldheight - 2][rnd - 1] = '3';
 			// create some space around the players
 			this.maparray[1][rnd] = '0';
 			this.maparray[2][rnd - 1] = '0';
 			this.maparray[this.fieldheight - 2][rnd] = '0';
 			this.maparray[this.fieldheight - 3][rnd - 1] = '0';
 			// create a goal
			int rnd2 = new Random().nextInt(this.fieldwith - 3) + 1;
 			this.maparray[this.fieldheight / 2][rnd2] = '5';
 			// write the generated map
 			for (int i = 0; i < (this.fieldheight); i++) {
 				for (int j = 0; j < (this.fieldwith); j++) {
 					this.fw.write(this.maparray[i][j]);
 				}
 				if (!(i >= (this.fieldheight - 1))) {
 					this.fw.write(System.getProperty("line.separator"));
 				}
 			}
 			this.fw.flush();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
