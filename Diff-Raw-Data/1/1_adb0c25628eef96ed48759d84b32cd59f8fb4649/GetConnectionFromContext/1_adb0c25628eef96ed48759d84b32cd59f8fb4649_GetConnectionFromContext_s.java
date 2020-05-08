 package com.ids.context;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 
 import javax.sql.DataSource;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
import mediautil.gen.Log;
 
 import org.springframework.context.ApplicationContext;
 
 import com.ids.controllers.MainController;
 
 public class GetConnectionFromContext {
 
 	private  ApplicationContext ctx;
 	private   DataSource source;
 	private Connection con;
   	private final static Logger logger = Logger.getLogger(GetConnectionFromContext.class.getName()); 
 	
 	//public Connection myConnection() 
 //	{
 		//   ctx = AppContext.getApplicationContext(); 
 
 		//	  ctx = AppContext.getApplicationContext();  
 		  //     source = (DataSource) ctx.getBean("DataSource");
 		 //       try {
 		//			con =       source.getConnection();
 		//		} catch (SQLException e) {
 					// TODO Auto-generated catch block
 			//		e.printStackTrace();
 		//			logger.log( Level.SEVERE,"Err", (Object) e );
 		//		}
 		//return con;
 	//}
 	
 	
 	public void closeCon() {
 		//try {
 	//		con.close();
 	//		logger.warning("WE HAVE ALSO CLOSED THIS CONNECTION ");
 	//	} catch (SQLException e) {
 			// TODO Auto-generated catch block
 	//		e.printStackTrace();
 	//		logger.log( Level.SEVERE,"Err", (Object) e );
 	//	}
 	}
 	
 }
