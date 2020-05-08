 package org.commacq.http;
 
 import java.io.IOException;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import lombok.extern.slf4j.Slf4j;
 
 import org.commacq.CsvLineCallbackWriter;
 import org.commacq.layer.Layer;
 import org.eclipse.jetty.server.Request;
 import org.eclipse.jetty.server.handler.AbstractHandler;
 
 @Slf4j
 public class CsvHttpFileHandler extends AbstractHandler {
 
 	private Layer layer;
 
 	public CsvHttpFileHandler(Layer layer) {
 		this.layer = layer;
 	}
 
 	@Override
 	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
 
 		if(target.equals("/favicon.ico")) {
 			//Ignore requests for the favicon.ico from the browser
 			return;
 		}
 		
 		log.info("Received request for: {}", target);
 		
 		final String entityId = HttpUtils.getEntityStringFromTarget(target);
 		
		if(!layer.getEntityIds().contains(entityId)) {
 			HttpUtils.respondWithErrorMessage(layer, entityId, response, log);
 			return;
 		}
 		
 		response.setContentType("text/csv");
 		response.setHeader("Content-disposition", "attachment; filename=" + entityId + ".csv");
 		
 		//response.setHeader("Cache-Control", "must-revalidate");
 		//response.setHeader("Pragma", "must-revalidate");
 		//response.setContentType("application/vnd.ms-excel");
 		
 		//TODO - Keep track of the total content length of the cache at any one time
 		//so that we can indicate to the user how many percent of the way through
 		//the file download they are.
 		CsvLineCallbackWriter writer = new CsvLineCallbackWriter(response.getWriter(), layer.getColumnNamesCsv(entityId));
 		
 		layer.getAllCsvLines(entityId, writer);
 		
 		response.flushBuffer();
 	}
 }
