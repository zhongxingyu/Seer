 public class Matrix{
 	
 	private int[][] matrix;
 	
 	public Matrix(int row, int column){
 		matrix = new int[row][column];
 		
 		for(int i=0 ; i<row ; i++){
 			for(int j=0 ; j<column ; j++){
 				matrix[i][j] = 1;
 			}
 		}
 	}
 	
 	public void setElement(int row, int column, int newValue){		// sets element at (row,column) to newValue
 		if(row<matrix.length && column<matrix[0].length){
 			matrix[row][column] = newValue;
 		}
 	}
 	
 	public void prettyPrint(){						// prints the matrix nicely
 		System.out.println("");
 		for(int i=0 ; i<matrix.length ; i++){
 			for(int j=0 ; j<matrix[0].length ; j++){
 				System.out.print("\t" + matrix[i][j]);
 			}
 			System.out.println("");
 		}
 		System.out.println("");
 	}
 	
 	public void setRow(int row, String value){				// sets given row to values presented in a string, separated by commas
 		if(row<matrix.length){ 				   		// valid row reference
 			String[] valueArray = value.split("\\,");
 			if(valueArray.length==matrix[0].length){   		// valid value string
 				for(int i=0 ; i<matrix[0].length ; i++){
 					matrix[row][i] = Integer.parseInt(valueArray[i]);
 				}
 			}
 		}
 	}
 	
 	public void setColumn(int column, String value){			// sets given column to values presented in a string, separated by commas
 		if(column<matrix[0].length){ 			   		// valid column reference
 			String[] valueArray = value.split("\\,");
 			if(valueArray.length==matrix.length){      		// valid value string
 				for(int i=0 ; i<matrix.length ; i++){
 					matrix[i][column] = Integer.parseInt(valueArray[i]);
 				}
 			}
 		}
 	}
 	
	public String toString(){						// converts matrix to string like "[1,2,3;5,6,7;8,9,10]"
 		String output = "[";
 		for(int i=0 ; i<matrix.length ; i++){
 			for(int j=0 ; j<matrix[0].length ; j++){
				output += matrix[i][j];
				if(j!=matrix[0].length-1){
					output += ",";
				}
				else if(i!=matrix.length-1){
					output += ";";
				}
				else{
					output += "]";
				}
 			}
 		}

 		return output;
 	}
 }
