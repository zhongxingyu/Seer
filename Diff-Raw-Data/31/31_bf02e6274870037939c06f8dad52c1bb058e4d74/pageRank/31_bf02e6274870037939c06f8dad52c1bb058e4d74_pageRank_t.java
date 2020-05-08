 
 
 public class pageRank {
     static final double epsilon = 0.001;
     static final double d = 0.95;
     // pageRank <fileName>
 	static CSV file;
 	public static void main(String args[]){
	    boolean isDirected = false;
	    
	    if(args.length == 1){
	        file = new CSV(args[0]);
	    }
	    else if(args.length == 2){
	        file = new CSV(args[0]);
	        isDirected = true;
	    }
	    
		Matrix.initMatrix(isDirected);
 	    
 	    file = new CSV("data/NCAA_football.csv");
 		
		//System.out.println(file.toString());
 		
		//System.out.println("size="+file.rows.size());
 		
 		int matrixSize = file.rows.size();
 		
 		for(int i = 0; i < file.rows.size(); i++){
 		    Row tempRow = new Row(file.rows.get(i),matrixSize);
 		    Matrix.addRow(tempRow);
 		}
 		
 		
 		for(int i = 0; i < file.data.size(); i++){
 		    Row tempRow = Matrix.getRowbyName(file.data.get(i).get(0));
		    Row tempRow2 = null;
 		    Link tempLink = new Link(Integer.parseInt(file.data.get(i).get(1)));
		    Link tempLink2 = null;
 		    tempRow.addValue(tempLink, Matrix.getRowbyName(file.data.get(i).get(2)).id);
		    if(isDirected){
		        tempRow2 = Matrix.getRowbyName(file.data.get(i).get(2));
		        tempLink2 = new Link(Integer.parseInt(file.data.get(i).get(3)));
		        tempRow2.addValue(tempLink2, Matrix.getRowbyName(file.data.get(i).get(0)).id);
		    }
		    
 		}
 		
 		
		
 	}
 }
