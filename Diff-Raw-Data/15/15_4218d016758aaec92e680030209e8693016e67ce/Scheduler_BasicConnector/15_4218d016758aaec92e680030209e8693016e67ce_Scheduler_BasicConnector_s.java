 /*
  * Copyright (c) 2009 - DHTMLX, All rights reserved
  */
 import java.sql.Connection;
 
 import com.dhtmlx.connector.ConnectorServlet;
 import com.dhtmlx.connector.SchedulerConnector;
 
 
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class SchedulerBasicConnector.
  */
 public class Scheduler_BasicConnector extends ConnectorServlet {
 
 	/* (non-Javadoc)
 	 * @see com.dhtmlx.connector.ConnectorServlet#configure()
 	 */
 	@Override
 	protected void configure() {
 		Connection conn= ( new DataBaseConnection()).getConnection();
 		
 		SchedulerConnector c = new SchedulerConnector(conn);
 		c.render_table("events","event_id","start_date,end_date,event_name,details","","");
 	}
 
 }
