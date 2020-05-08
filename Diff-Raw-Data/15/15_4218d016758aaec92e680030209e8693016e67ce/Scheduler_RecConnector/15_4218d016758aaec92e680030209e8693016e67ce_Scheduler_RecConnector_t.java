 /*
  * Copyright (c) 2009 - DHTMLX, All rights reserved
  */
 import java.sql.Connection;
 
 import com.dhtmlx.connector.ConnectorServlet;
 import com.dhtmlx.connector.SchedulerConnector;
 
 
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class SchedulerRecConnector.
  */
 public class Scheduler_RecConnector extends ConnectorServlet {
 
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

 	/* (non-Javadoc)
 	 * @see com.dhtmlx.connector.ConnectorServlet#configure()
 	 */
 	@Override
 	protected void configure() {
 		Connection conn= ( new DataBaseConnection()).getConnection();
 		
 		SchedulerConnector c = new SchedulerConnector(conn);
 		c.event.attach(new Scheduler_RecBehavior(c));
 		c.render_table("events_rec","event_id","start_date,end_date,text,rec_type,event_pid,event_length","","");
 
 	}
 
 }
