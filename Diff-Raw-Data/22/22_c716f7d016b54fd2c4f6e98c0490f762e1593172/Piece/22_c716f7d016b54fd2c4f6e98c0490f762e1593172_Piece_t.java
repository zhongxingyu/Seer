 import java.util.Random;
 
 class Piece {
 	private int[][] points;
 	// should be 0-4
 	private int rotation; 
 	private String type;
 	
 	Random generator = new Random();
 	
 	// 0, 1, 2, 3, 4, 5, 6
 	// I, J, L, O, S, T, Z
 	public Piece() {
 		// picks a random piece type
		int type = generator.nextInt(7);
 		points = getPoints(type);
 		rotation = 0;
 	}
 	
 	public Piece(int n) {
 		points = getPoints(n);
 		rotation = 0;
 	}
 	
 	public boolean rotateR() {
 		// rotate 90 right
 		return false;
 	}
 	
 	public boolean rotateL() {
 		// TODO complete rotate code
 		rotateR();
 		rotateR();
 		rotateR();
 		return false;
 	}
 	
 	private int[][] getPoints(int type) {
 		int[][] value = new int[4][4];
 		switch (type) {
 			// line
 			case 0:
 			value[0][0]=1;
 			value[0][1]=1;
 			value[0][2]=1;
 			value[0][3]=1;
 			this.type="I";
 			break;
 			// J
 			case 1: 
 			value[1][0]=1;
 			value[1][1]=1;
 			value[1][2]=1;
 			value[0][2]=1;
 			this.type="J";
 			break;
 			// L
 			case 2:
 			value[0][0]=1;
 			value[0][1]=1;
 			value[0][2]=1;
 			value[1][2]=1;
 			this.type="L";
 			break;
 			// square
 			case 3:
 			value[0][0]=1;
 			value[0][1]=1;
 			value[1][0]=1;
 			value[1][1]=1;
 			this.type="O";
 			break;
 			// S
 			case 4:
 			value[1][0]=1;
 			value[2][0]=1;
 			value[1][1]=1;
 			value[0][1]=1;
 			this.type="S";
 			break;
 			// T
 			case 5:
 			value[0][0]=1;
 			value[1][0]=1;
 			value[2][0]=1;
 			value[1][1]=1;
 			this.type="T";
 			break;
 			// Z
 			case 6:
 			value[0][0]=1;
 			value[1][0]=1;
 			value[1][1]=1;
 			value[2][1]=1;
 			this.type="Z";
 			break;			
 		}
 		return value;
 	}
 	public String toString() {
 		String printString="";
 		for (int i=0;i<4 ;i++ ) {
 			for (int j=0;j<4 ;j++ ) {
 				if (points[i][j]==1) {
 					printString+=" "+this.type;
 				}
				else {
					printString+="  ";
				}
 			}
 			printString+="\n";
 		}
 		return printString;
 	}
 }
