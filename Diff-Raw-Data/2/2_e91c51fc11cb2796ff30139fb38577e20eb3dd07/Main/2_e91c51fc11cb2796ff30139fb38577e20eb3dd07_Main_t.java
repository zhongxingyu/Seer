 public class Main {
 	public static void main(String[] args){
 		try {
 			int i,j;
 			for (i=0; i < args.length; i++){
				for(j = i+1; j < args.length; j++){
 					if (args[i].equals(args[j])){
 						throw new Exception("2 queried files are the same file");
 					}
 				}
 			}
 		    HistoryParser history = new HistoryParser(args);
 		    history.printHistoryInformation();
 	
 		}
 		catch (Exception e) {
 			System.out.println("ERROR: " + e.getMessage());
 		}
 	}
 
 }
