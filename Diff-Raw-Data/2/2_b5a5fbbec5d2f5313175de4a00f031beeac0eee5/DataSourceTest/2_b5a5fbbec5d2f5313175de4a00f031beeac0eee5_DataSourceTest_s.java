 package de.ololololo.test;
 
 import de.ololololo.*;
 import java.util.*;
 
 public class DataSourceTest {
 	private DataSource dataSource;
 	
 	public DataSourceTest() {
 		
 		
 	}
 	
 	public void main () {
dataSource = new DataSource();
 		
 		dataSource.printAllTasks();
 		dataSource.newTask("Test", new Date());
 		dataSource.newTask("neu 2", new Date());
 		dataSource.printAllTasks();
 	}
 }
