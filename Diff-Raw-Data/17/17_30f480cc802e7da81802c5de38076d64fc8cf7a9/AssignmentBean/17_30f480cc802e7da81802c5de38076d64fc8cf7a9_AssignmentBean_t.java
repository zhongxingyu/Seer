 package mybeans.mydb.assignments;
 
 import java.io.Serializable;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.sql.DataSource;
 
 import org.primefaces.event.data.PageEvent;
 
 import mybeans.mydb.assignments.model.Assignment;
 
 @ManagedBean(name="assignment")
 @SessionScoped
 
 public class AssignmentBean {
 	//resource injection
 	//@Resource(name="jdbc/projectdb")
 	private DataSource ds;
 	
 	private Connection con;
 	
 	private Assignment selectedAssignment;
 	
 
 	
 	public List<Assignment> list;
 	
 	public Assignment getSelectedAssignment() {
 		return selectedAssignment;
 	}
 
 	public void setSelectedAssignment(Assignment selectedAssignment) {
 		this.selectedAssignment = selectedAssignment;
 	}
 
 	public void downloadSwitch(int id, boolean checked) throws SQLException{
 		System.out.println("trigger switch");
 		openDownload(id,checked);
 	}
 	
 	//if resource injection is not support, you still can get it manually.
 	public AssignmentBean(){
 		try {
 			Context ctx = new InitialContext();
 			ds = (DataSource)ctx.lookup("java:comp/env/jdbc/projectdb");
 		} catch (NamingException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public boolean checkDupicateAssignment(String targetName)throws SQLException{
 		//return true if there's no such name in the DB
 		if(ds==null)
 			throw new SQLException("Can't get data source");
  
 		//get database connection
 		con = ds.getConnection();
  
 		if(con==null)
 			throw new SQLException("Can't get database connection");
 		
 		PreparedStatement ps 
 		= con.prepareStatement(
 		   "select count(*) from assignments where name=? and name!=''");
 		
 		ps.setString(1, targetName);
 		ResultSet result =  ps.executeQuery();
 		result.next();
 		int r = result.getInt(1);
 		con.close();
 		if(r==0)
 			return true;
 		else
 			return false;
 	}
 	
 	public List<Assignment> getAssignmentList()throws SQLException{
 		 
 		if(ds==null)
 			throw new SQLException("Can't get data source");
  
 		//get database connection
 		con = ds.getConnection();
  
 		if(con==null)
 			throw new SQLException("Can't get database connection");
  
 		PreparedStatement ps 
 			= con.prepareStatement(
 			   "select * from assignments order by a_id ASC"); 
  
 		//get student data from database
 		ResultSet result =  ps.executeQuery();
 		
 		list = new ArrayList<Assignment>();
 		
 		while(result.next()){
 			Assignment assign = new Assignment();
 			assign.setDescription(result.getString("description"));
 			String temp = assign.getDescription();
 			
 			if(temp.length()>50){
 				assign.setShortDescription(temp.substring(0, 49)+" ...");
 			}else
 				assign.setShortDescription(temp);
 			
 			
 			assign.setAssignmentID(result.getInt("a_id"));
 			assign.setAssignmentDirectory(result.getString("document_dict"));			
 		    //get screenshot directory
 			String tempSSD=result.getString("screenshot_dict");
 			if(tempSSD==null)
 				assign.setScreenDirectory("noScreenshot");
 			else if("".equals(tempSSD))
 				assign.setScreenDirectory("noScreenshot");
 			else
 				tempSSD="/"+tempSSD;
 			assign.setScreenDirectory(tempSSD);
 			
 			int downloadable = result.getInt("downloadable");
 			if(downloadable==1)
 				assign.setValue(true);
 			else
 				assign.setValue(false);
 			
 			assign.setPoint(result.getInt("point"));
 			assign.setRating(result.getInt("rating"));
 			//get assignment name
 //			String name;	
 //			if(count<10)
 //				name = "Assignment 0"+count;
 //			else
 //				name = "Assignment "+count;
 			assign.setAssignmentName(result.getString("name"));
 			assign.setAuthor(result.getString("author"));
 			//store all data into a List
 			list.add(assign);
 		}
 		con.close();
 		if(!list.isEmpty())
 			selectedAssignment = list.get(0);
 		return list;
 	}
 	
 	public Assignment getAssignmentByID(int a_id) throws SQLException{
 		Assignment assign = new Assignment();
 		if(ds==null)
 			throw new SQLException("Can't get data source");
  
 		//get database connection
 		con = ds.getConnection();
  
 		if(con==null)
 			throw new SQLException("Can't get database connection");
 		
 		PreparedStatement ps 
 		= con.prepareStatement(
 		   "select * from assignments where a_id=?;");
 		
 		ps.setInt(1, a_id);
 		ResultSet result =  ps.executeQuery();
 		
 		result.next();
 		assign.setAssignmentDirectory(result.getString("document_dict"));
 		assign.setDescription(result.getString("description"));
 		assign.setScreenDirectory(result.getString("screenshot_dict"));
 		assign.setPoint(result.getInt("point"));
 		assign.setRating(result.getInt("rating"));
 		assign.setAssignmentName(result.getString("name"));
 		assign.setAuthor(result.getString("author"));
 		
 		con.close();
 		return assign;
 	}
 	
 	public void addAssignment(String docu_dict, String description, String ss_dict, int point, int rating, String name, String author) throws SQLException{
 		if(ds==null)
 			throw new SQLException("Can't get data source");
  
 		//get database connection
 		con = ds.getConnection();
  
 		if(con==null)
 			throw new SQLException("Can't get database connection");
 		
 		PreparedStatement ps 
 		= con.prepareStatement(
 		   "select max(a_id) from assignments;");
 		
 		ResultSet result =  ps.executeQuery();
 		
 		result.next();
 		int current = result.getInt(1)+1;
 		
 
 		ps = con.prepareStatement("insert into assignments values (?,?,SYSDATE(),?,?,?,?,?,?,?)");
 		
 		ps.setInt(1, current);
 		ps.setString(2, docu_dict);
 		ps.setString(3,description);
 		ps.setString(4, ss_dict);
 		ps.setInt(5, point);
 		ps.setInt(6,rating);
 		ps.setString(7, author);
 		ps.setString(8, name);
 		ps.setInt(9, 0);
 		
 		int updated = ps.executeUpdate();
 		
 		if(updated==0)
 			throw new SQLException("Insert Error!");
 		
 		con.close();
 		
 	}
 
 	public void openDownload(int id, boolean checked) throws SQLException{
 		if(ds==null)
 			throw new SQLException("Can't get data source");
  
 		//get database connection
 		con = ds.getConnection();
  
 		if(con==null)
 			throw new SQLException("Can't get database connection");
 		
 		
 		PreparedStatement ps 
 		= con.prepareStatement(
 		   "update assignments set downloadable=abs(downloadable-1) where a_id=?;");
 		
 		ps.setInt(1, id);
 		
 		int updated = ps.executeUpdate();
 		
 		System.out.println("id:" +id);
 		
 		if(updated==0)
 			throw new SQLException("Update Error!");
 		
         String summary = checked ? "Set as downloadable" : "Set as undownloadable";  
         
         FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(summary));
 		
 		con.close();
 	}
 	
 	public void updateAssignment(String docu_dict,
 			int a_id, String description, String ss_dict, int point, int rating, String name, String author) throws SQLException {
 		// TODO Auto-generated method stub
 		
 		if(ds==null)
 			throw new SQLException("Can't get data source");
  
 		//get database connection
 		con = ds.getConnection();
  
 		if(con==null)
 			throw new SQLException("Can't get database connection");
  
 		
		PreparedStatement ps = con.prepareStatement("update assignments set document_dict=?, uploaded_date=SYSDATE(), description=?, screenshot_dict=?, point=?, rating=?,author=?, name=? where a_id=?");
 		
 		ps.setString(1, docu_dict);
 		ps.setString(2, description);
 		ps.setString(3, ss_dict);
 		ps.setInt(4,point);
 		ps.setInt(5, rating);
		
		ps.setString(6, author);
		ps.setString(7, name);
		ps.setInt(8, a_id);
		
 		
 		int updated = ps.executeUpdate();
 		
 		if(updated==0)
 			throw new SQLException("Update Error!");
 		
 		con.close();
 		
 	}
 	
 	public void deleteAssignment(int a_id) throws SQLException{
 		
 		if(ds==null)
 			throw new SQLException("Can't get data source");
  
 		//get database connection
 		con = ds.getConnection();
  
 		if(con==null)
 			throw new SQLException("Can't get database connection");
  
 		
 		PreparedStatement ps = con.prepareStatement("delete from assignments where a_id=?");
 		ps.setInt(1, a_id);	
 		int updated = ps.executeUpdate();
 		
 		if(updated==0)
 			throw new SQLException("Update Error!");
 		
 		con.close();
 		
 	}
 
 	public List<Assignment> getList() {
 		return list;
 	}
 
 	public void setList(List<Assignment> list) {
 		this.list = list;
 	}
 	
     public void pageClick(PageEvent event){
     	int current = event.getPage();
     	this.selectedAssignment = list.get(current*9);
     }
     
 
 }
