 package org.test.streaming;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class ManualPush extends HttpServlet {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	protected static final Log log = LogFactory.getLog(ManualPush.class);
 
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 
 		req.getRequestDispatcher("manualPush.jsp").forward(req, resp);
 
 	}
 	
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		
 		
 		List<CachoRetrieval> requests = new ArrayList<CachoRetrieval>();
 		String videoId = req.getParameter("videoId");
 		String fileName = req.getParameter("fileName");
 		int current = 0;
 		while(true){
 			String ip = req.getParameter("ip-"+current);
 			String port = req.getParameter("port-"+current);
 			String from = req.getParameter("from-"+current);
 			String lenght = req.getParameter("lenght-"+current);
 			
 			CachoRetrieval cachoRetrieval = newCachoRetrieval(fileName, videoId, ip, port, from, lenght);
 			if(cachoRetrieval == null){
 				break;
 			}
 			requests.add(cachoRetrieval);
 			current++;
 		}
 		if(CollectionUtils.isEmpty(requests)){
 			log.error("Errores en el request de push manual, no se hace nada");
 			return;
 		}
 		Conf conf = new Conf();
 		ManualPushRetrievalPlan sharingPlan = new ManualPushRetrievalPlan(videoId, requests);
 		DefaultMovieSharingPlanInterpreter interpreter = new DefaultMovieSharingPlanInterpreter(conf);
 		interpreter.interpret(sharingPlan);
 	}
 
 	private CachoRetrieval newCachoRetrieval(String fileName, String videoId, String ip, String port, String from,
 			String lenght) {
 
 		if(StringUtils.isEmpty(fileName)){
 			log.error("fileName no puede estar vacio");
 			return null;
 		}
 		if(StringUtils.isEmpty(videoId)){
 			log.error("videoId no puede estar vacio");
 			return null;
 		}
 		if(StringUtils.isEmpty(ip)){
			log.error("port no puede estar vacio");
 			return null;
 		}
 		if(StringUtils.isEmpty(port)){
 			log.error("port no puede estar vacio");
 			return null;
 		}
 		if(StringUtils.isEmpty(from)){
 			log.error("from no puede estar vacio");
 			return null;
 		}
 		if(StringUtils.isEmpty(lenght)){
 			log.error("lenght no puede estar vacio");
 			return null;
 		}
 		int puerto = Integer.parseInt(port);
 		int desde = Integer.parseInt(from);
 		int duracion = Integer.parseInt(lenght);
 		
 		log.info("new cacho retrieval: ip: "+ip+" port: "+port+" from: "+from+" lenght: "+lenght);
 		
 		CachoRequest cachoRequest = new CachoRequest();
 		cachoRequest.setCacho(new MovieCacho(desde, duracion));
 		cachoRequest.setFileName(fileName);
 		cachoRequest.setMovieId(videoId);
 		cachoRequest.setDirection(CachoDirection.PUSH);
 		
 		return new CachoRetrieval(ip, puerto, cachoRequest);
 	}
 
 }
