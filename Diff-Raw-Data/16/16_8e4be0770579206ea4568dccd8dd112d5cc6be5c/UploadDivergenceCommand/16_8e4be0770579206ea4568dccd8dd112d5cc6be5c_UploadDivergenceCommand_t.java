 import java.io.File;
 import java.io.FileNotFoundException;
 import java.sql.SQLException;
 import java.util.ArrayList;
 
 
 /**
  * TODO Put here a description of what this class does.
  *
  * @author schepedw.
  *         Created Mar 14, 2013.
  */
 public class UploadDivergenceCommand extends Command{
 	private File fileLocation;
 	private String options;
 	private String name;
 	
 	public UploadDivergenceCommand(String fileLocation, String options, String name){
 		this.fileLocation=new File(fileLocation);
 		this.options=options;
 		this.name=name;
 		if (this.name==""){
 			this.name=getDate();
 		}
 	}
 	/**
 	 * TODO Put here a description of what this constructor does.
 	 *
 	 * @param fileLocation2
 	 * @param options2
	 * @return 
 	 */
 
 	@Override
	public String execute() {
 		try {
 			DatabaseConnector connection=new DatabaseConnector();
 			DivergenceParser parser=new DivergenceParser(this.fileLocation);
 			ArrayList<String[]> rowsToUpload=parser.parseFile();
 			for (String[] row : rowsToUpload){
 				String chromosome=row[0];
 				String position=row[1];
 				String divValue=row[2];
 				connection.uploadDivergence(this.name,chromosome, Integer.valueOf(position),Integer.valueOf(divValue));
 			}			
 		} catch (ClassNotFoundException exception) {
 			// TODO Auto-generated catch-block stub.
 			exception.printStackTrace();
 		} catch (SQLException exception) {
 			// TODO Auto-generated catch-block stub.
 			exception.printStackTrace();
 		} catch (FileNotFoundException exception) {
 			// TODO Auto-generated catch-block stub.
 			exception.printStackTrace();
 		}
		return "Upload Successful";
		
 		
 		
 	}
 
 	@Override
 	public void pipeOutput() {
 		// TODO Auto-generated method stub.
 		
 	}
 
 }
