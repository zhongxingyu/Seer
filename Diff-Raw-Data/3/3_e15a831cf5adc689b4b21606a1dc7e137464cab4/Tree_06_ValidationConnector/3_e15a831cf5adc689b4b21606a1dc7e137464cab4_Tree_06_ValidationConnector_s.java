 /*
  * Copyright (c) 2009 - DHTMLX, All rights reserved
  */
 import java.sql.Connection;
 
 
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class TreeValidationConnector.
  */
 public class Tree_06_ValidationConnector extends ConnectorServlet {
 
 	/* (non-Javadoc)
 	 * @see com.dhtmlx.connector.ConnectorServlet#configure()
 	 */
 	@Override
 	protected void configure() {
 		Connection conn= ( new DataBaseConnection()).getConnection();
 		
 		TreeConnector c = new TreeConnector(conn);
 		c.event.attach(new Tree_06_ValidationBehavior());
 		c.render_table("tasks", "taskId", "taskName","","parentId");
 	}
 
 }
