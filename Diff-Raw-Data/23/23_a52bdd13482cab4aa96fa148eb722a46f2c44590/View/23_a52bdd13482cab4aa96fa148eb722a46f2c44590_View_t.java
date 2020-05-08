 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 
 /**
  * TODO Put here a description of what this class does.
  *
  * @author schepedw.
  *         Created Apr 16, 2013.
  */
 public class View extends Command {
 	private String table;
 	private DatabaseConnector conn;
 	private String columnName;
 	
 	//Could be made more extendable by passing the column name as a parameter
 	public View (String table){
 		
 		this.table=table;
 		if (this.table.equals("Divergence"))
 			this.columnName="DivName";
 		else if (this.table.equals("Annotation"))
 			this.columnName="AnnoName";
 		else if (this.table.equals("Vcf"))
 			this.columnName="VcfName";
 		try {
 			this.conn=new DatabaseConnector();
 		} catch (Exception exception) {
 			exception.printStackTrace();
 		}
 	}
 	@Override
 	public String execute() {
 		String sql = "Select `"+this.columnName+"` from `" +this.table+"`";
		String result="";
 		ResultSet names;
 		try {
 			names=this.conn.executeQuery(sql);
 			while (names.next()){
				result+=names.getString(1)+"\n";
 				
 			}
 		} catch (Exception exception) {
 			exception.printStackTrace();
 		} 
 		
 		
		return result;
 	}
 
 	@Override
 	public void pipeOutput() {
 		// TODO Auto-generated method stub.
 
 	}
 
 }
