 import java.io.File;
 import java.io.FileNotFoundException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 
 public abstract class UploadCommand extends Command {
 	public abstract ArrayList<String[]> parseFile() throws FileNotFoundException;
 	public abstract String getSQLStatement(String[] row) ;
 	
 	public String upload(File file) throws ClassNotFoundException, SQLException, FileNotFoundException{
 		DatabaseConnector connection=new DatabaseConnector();
 		ArrayList<String[]> rowsToUpload=parseFile();
		
 		for (String[] row: rowsToUpload){
 			String sql= getSQLStatement(row);
 			connection.upload(sql);
 		}
		
 		return file.toString()+" Uploaded Successfully!";
 	}
 	
 }
