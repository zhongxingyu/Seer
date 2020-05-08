 package org.oobium.persist.migrate.controllers;
 
 import org.oobium.app.controllers.HttpController;
 import org.oobium.persist.migrate.MigratorService;
 
 public class RedoController extends HttpController {
 
 	public void handleRequest() throws Exception {
 		if(hasParam("log")) {
 			System.setProperty("org.oobium.persist.db.logging", param("log"));
 		}
 		try {
 			MigratorService service = MigratorService.instance();
 			String response;
 			try {
				response = service.migrateRedo("all".equals(param("step")) ? -1 : param("step", 1));
 				logger.info(response);
 			} catch(Exception e) {
 				response = e.getLocalizedMessage();
 				logger.info(response);
 			}
 			render(response);
 		} finally {
 			System.clearProperty("org.oobium.persist.db.logging");
 		}
 	};
 	
 }
