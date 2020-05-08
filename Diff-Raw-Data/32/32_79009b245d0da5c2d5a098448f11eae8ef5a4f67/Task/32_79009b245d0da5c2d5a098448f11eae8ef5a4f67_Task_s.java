 package models;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 public class Task{
 	public String id;
 	public String todo;
 	public String status;
 	public String owner;
 	public String details;
 	public Date due;
 	public Date createdAt;
 	public Date closedAt;
 	public String group;
 	public String author;
 	public Boolean done;
 	public Boolean active;
 	
 	
 	public static Task findById(Connection connection, String id) {
 
 		Task task = new Task();
 		  try{
 		  PreparedStatement ps = connection.prepareStatement("select id,owner,todo,status,details,due,createdAt,closedAt,author,tskgroup,done,active from task_dtl where id=? ");
 
 		   ps.setString(1, id);
 		 
 		  ps.execute();
 		  ResultSet rs =  ps.getResultSet();
 		  
 		 while(rs.next()){
 			
 			 task.id = rs.getString(1);
 			 task.owner  = rs.getString(2);
 			 task.todo = rs.getString(3);
 			 task.status = rs.getString(4);
 			 task.details= rs.getString(5);
 			 task.due = rs.getTimestamp(6);
 			 task.createdAt = rs.getTimestamp(7);
 			 task.closedAt = rs.getTimestamp(8);
 			 task.author = rs.getString(9);
 			 task.group = rs.getString(10);
 			 task.done = (rs.getString(11)!=null && rs.getString(11).equals("Y") )?Boolean.TRUE : Boolean.FALSE;
 			 task.active = (rs.getString(12)!=null && rs.getString(12).equals("Y") )?Boolean.TRUE : Boolean.FALSE;
 			 
 			 
 		 }
 		  System.out.println("executed for task select ["+id+"]" );
 		  
 		  }catch(Exception e){
 			  e.printStackTrace();
 		  }
 		  
 		  return task;
 	}
 
 	
 	
 	public static List<Task> findByOwner(Connection connection, String owner) {
 		List<Task> list = new ArrayList<Task>();
 		
 		  try{
 		  PreparedStatement ps = connection.prepareStatement("select id,owner,todo,status,details,due,createdAt,closedAt,author,tskgroup,done,active from task_dtl where owner=? and active=? order by createdAt ");
 
 		   ps.setString(1, owner);
 		   ps.setString(2, "Y");
 		 
 		  ps.execute();
 		  ResultSet rs =  ps.getResultSet();
 		  
 		 while(rs.next()){
 			 Task task = new Task();
 			 task.id = rs.getString(1);
 			 task.owner  = rs.getString(2);
 			 task.todo = rs.getString(3);
 			 task.status = rs.getString(4);
 			 task.details= rs.getString(5);
 			 task.due = rs.getTimestamp(6);
 			 task.createdAt = rs.getTimestamp(7);
 			 task.closedAt = rs.getTimestamp(8);
 			 task.author = rs.getString(9);
 			 task.group = rs.getString(10);
 			 task.done = (rs.getString(11)!=null && rs.getString(11).equals("Y") )?Boolean.TRUE : Boolean.FALSE;
 			 task.active = (rs.getString(12)!=null && rs.getString(12).equals("Y") )?Boolean.TRUE : Boolean.FALSE;
 			 list.add(task);
 		 }
 		  System.out.println("executed for task owner select ["+owner+"]" );
 		  
 		  }catch(Exception e){
 			  e.printStackTrace();
 		  }
 		  
 		  return list;
 	}
 	
 	
 	public static void create(Connection connection,Task task) {
 		  try{
		  PreparedStatement ps = connection.prepareStatement("insert into task_dtl(id,owner,todo,status,details,due,createdAt,closedAt,author,tskgroup,done,active) values (task_dtl_seq.nextval,?,?,?,?,?,?,?,?,?,?,?) ");
 		  ps.setString(1, task.owner);
 		  ps.setString(2, task.todo);
 		  ps.setString(3, task.status);
 		  ps.setString(4, task.details);
 		  if(task.due!=null){
 		  ps.setTimestamp(5, new Timestamp(task.due.getTime()));
 		  }else{
 		  ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));	  
 		  }
 		  ps.setTimestamp(6,new Timestamp(System.currentTimeMillis()));
 		  ps.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
 		  ps.setString(8, task.author);
 		  ps.setString(9, task.group);
 		  
 		  ps.setString(10, "N");
 		  ps.setString(11, "Y");
 
 		  ps.execute();
 		  System.out.println("executed for insert ["+task.owner+"]" );
 		  
 		  }catch(Exception e){
 			  e.printStackTrace();
 		  }
 	}
 	
 	
 	public static void update(Connection connection,Task task) {
 		  try{
 			  
 		  PreparedStatement ps = connection.prepareStatement("update task_dtl set owner=?,todo=?,status=?,details=?,due=?,closedAt=?,tskgroup=?,done=? where id=?  ");
 		  ps.setString(1, task.owner);
 		  ps.setString(2, task.todo);
 		  ps.setString(3, task.status);
 		  ps.setString(4, task.details);
 		  if(task.due!=null){
 		  ps.setTimestamp(5, new Timestamp(task.due.getTime()));
 		  }else{
 	      ps.setTimestamp(5, new Timestamp(System.currentTimeMillis()));  
 		  }
 		  ps.setTimestamp(6,new Timestamp(System.currentTimeMillis()));
 		  ps.setString(7, task.group);
 		  ps.setString(8, task.done?"Y":"N");
 
 		  ps.setString(9, task.id);
 
 		  ps.execute();
 		  System.out.println("executed for update ["+task.id+"]" );
 		  
 		  }catch(Exception e){
 			  e.printStackTrace();
 		  }
 	}
 	
 	
 	
 	public static void delete(Connection connection,String id) {
 		  try{
 			  
 		  PreparedStatement ps = connection.prepareStatement("update task_dtl set active=? where id=?  ");
 		  ps.setString(1, "N");
 		  ps.setString(2, id);
 
 		  ps.execute();
 		  System.out.println("executed for delete ["+id+"]" );
 		  
 		  }catch(Exception e){
 			  e.printStackTrace();
 		  }
 	}
 	
 }
