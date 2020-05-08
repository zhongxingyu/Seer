 /**
  * 
  */
 package de.fhb.mobile.ToDoListAndroidApp.communication;
 
 import java.io.IOException;
 import java.util.List;
 
 import org.apache.http.conn.ConnectTimeoutException;
 
 import de.fhb.mobile.ToDoListAndroidApp.models.Todo;
 
 
 /**
  * @author Patrick
  *
  */
 public interface IServerCommunicationREST {
 	
 	/**
 	 * authenticate the user on the server.
 	 * 
 	 * @param username
 	 * @param password
 	 * @return "1" if you are authenticated, 0 if you arent, -1 if ConnectTimeOutException, -2 if IOException.
 	 * @throws IOException 
 	 */
 	public int authentification(String username,String password);
 		
 	/**
 	 * synchronize local todolist with server-todolist
 	 * 
 	 * @param todoList
 	 * @return 
 	 */
	public boolean synchronize();
 	
 	/**
 	 * get all todos from Server.
 	 * 
 	 * @return all your own todos.
 	 */
 	public List<Todo> getAllTodo();
 
 	/**
 	 * add a new todo to your list.
 	 * 
 	 * @param todo
 	 */
 	public void addTodo(Todo todo);
 	
 	/**
 	 * delete a todo from your list.
 	 * 
 	 * @param todo
 	 */
 	public void deleteTodo(Todo todo);
 	
 }
