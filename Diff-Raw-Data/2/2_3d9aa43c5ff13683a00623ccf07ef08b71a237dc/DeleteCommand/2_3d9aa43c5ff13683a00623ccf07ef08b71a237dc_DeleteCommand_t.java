 import java.sql.SQLException;
 
 /**
  * TODO Put here a description of what this class does.
  * 
  * @author schepedw. Created Apr 16, 2013.
  */
 public class DeleteCommand extends Command {
 
 	private String table;
 	private String delValue;
 	private String columnName;
 	private DatabaseConnector conn;
 
 	public DeleteCommand(String table, String delValue, String columnName) {
 		this.table = table;
 		this.delValue = delValue;
 		this.columnName=columnName;
 		try {
 			this.conn = new DatabaseConnector();
 		} catch (Exception exception) {
 			exception.printStackTrace();
 		}
 	}
 
 	@Override
 	public String execute() {
 		String sql = "DELETE FROM `" + this.table + "` where `"
				+ this.columnName + "`=" + this.delValue+";" ;
 		try {
 			if (this.conn.executeUpdate(sql)==1) {
 				System.out.printf("Succesfully deleted %s from %s", this.delValue,
 						this.table);
 
 				return "Success";
 			}
 		} catch (Exception exception) {
 			exception.printStackTrace();
 		} 
 		return "Fail!";
 	}
 
 	@Override
 	public void pipeOutput() {
 		// TODO Auto-generated method stub.
 
 	}
 
 }
