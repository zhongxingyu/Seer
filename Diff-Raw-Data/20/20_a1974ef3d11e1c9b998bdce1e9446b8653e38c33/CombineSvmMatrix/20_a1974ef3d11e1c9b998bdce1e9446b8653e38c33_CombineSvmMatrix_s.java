 package PatternGame;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 
 public class CombineSvmMatrix {
 
 	/* stores a matrix like this
 	 * [--------A---------]
 	 * [--------A---------]
 	 * [--------B---------]
 	 * [--------B---------]
 	 * [--------C---------]
 	 * [--------C---------]
 	 */
 	double svm[][] = null;
 	
 	/* 
 	 * [------A------|------B------|------C------]
 	 * 
 	 * 1 = A
 	 * 2 = B
 	 * 3 = C
 	 * 
 	 */
 	double svmLabel[] = null;
 	
 	int row;
 	int col;
 	int totalrow;
 	
 	/**
 	 * @param args
 	 */
 	public CombineSvmMatrix (SvmMatrix svm1,SvmMatrix svm2,SvmMatrix svm3){
 		double currentMatrix[][];
 		int currentRow = 0;
 		
 		row = svm1.getArrayRow();
 		col = svm1.getArrayCol();
 		totalrow = svm1.getArrayRow() * 3;
 		
 		svm = new double[totalrow][col];
 		svmLabel = new double[totalrow];
 		
 		currentMatrix = svm1.getMatrix();
 		for (int i=0; i<row; i++) {
 			for (int j=0; j<col; j++) {
 				svm[currentRow][j] = currentMatrix[i][j]; 
 			}
 			svmLabel[currentRow] = 1;
 			currentRow++;
 		}
 		
 		currentMatrix = svm2.getMatrix();
 		for (int i=0; i<row; i++) {
 			for (int j=0; j<col; j++) {
 				svm[currentRow][j] = currentMatrix[i][j]; 
 			}
 			svmLabel[currentRow] = 2;
 			currentRow++;
 		}
 		
 		currentMatrix = svm3.getMatrix();
 		for (int i=0; i<row; i++) {
 			for (int j=0; j<col; j++) {
 				svm[currentRow][j] = currentMatrix[i][j]; 
 			}
 			svmLabel[currentRow] = 3;
 			currentRow++;
 		}
 		
 		
 	}
 	
 	public double[][] getSvmMatrix() {
 		return svm;
 	}
 	
 	public double[] getSvmLabel() {
 		return svmLabel;
 	}
 	
 	public void svmout(String filename) {
 
 		BufferedWriter writer = null;
 
 		try {
 			writer = new BufferedWriter(new FileWriter("Svm/SVM_Matrix_"+filename+".txt"));
 
 			for (int i=0; i<totalrow; i++) {
 				for (int j=0; j<col; j++) {
					writer.write(String.format("%.2f",svm[i][j]) + " "); 
 				}
 				writer.newLine();
 			}
 
 			writer.flush();
 			writer.close();
 		} catch (IOException e) {
 			System.err.println(e.getMessage());
 			System.exit(-1);
 		}
 	}
 	
 	public void svmlabelout(String filename) {
 
 		BufferedWriter writer = null;
 
 		try {
 			writer = new BufferedWriter(new FileWriter("Svm/SVM_Label_Matrix_"+filename+".txt"));
 
 			
 			for (int i=0; i<totalrow; i++) {
 				writer.write(svmLabel[i] + " "); 
 				writer.newLine();
 			}
 
 			writer.flush();
 			writer.close();
 		} catch (IOException e) {
 			System.err.println(e.getMessage());
 			System.exit(-1);
 		}
 	}
 }
