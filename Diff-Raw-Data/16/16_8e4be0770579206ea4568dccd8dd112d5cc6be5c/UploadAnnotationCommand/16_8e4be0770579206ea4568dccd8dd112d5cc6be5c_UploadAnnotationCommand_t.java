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
 public class UploadAnnotationCommand extends Command{
 	private File fileLocation;
 	private String options;
 	private String name;
 	public UploadAnnotationCommand(String fileLocation, String options, String name){
 		this.fileLocation=new File(fileLocation);
 		this.options=options;
 		this.name=name;
 		if (this.name==""){
 			this.name=getDate();
 		}
 	}
 	@Override
	public String execute() {
 	 try {
 		DatabaseConnector connection=new DatabaseConnector();
 		AnnotationParser parser=new AnnotationParser(this.fileLocation);
 		ArrayList<String[]> rowsToUpload=parser.parseFile();
 		for (String[] row : rowsToUpload){
 			String chrom =row[0];
 			String startPosition=row[1];
 			String endPosition=row[2];
 			String geneName=row[3];
 			String geneDirection=row[4];
 			connection.uploadAnnotation(this.name,chrom, Integer.valueOf(startPosition),Integer.valueOf(endPosition), geneName, geneDirection);
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
 	/**
 	 * TODO: implement
 	 */
 
 }
