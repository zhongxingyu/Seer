 package database;
 
 import utils.PathAndFileNames;
 
 public class Exporter {
 	public static void main (String[] args) {
 		Database database = new Database(PathAndFileNames.DATA_BASE_PATH);
 		
		database.exportArff("SELECT * FROM method_view", "methods");
 		
 		database.shutdown();
 		
 		System.out.println("Done!");
 	}
 }
