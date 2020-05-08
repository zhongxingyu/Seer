 package com.gentics.cr.lucene;
 
 import java.io.IOException;
 import java.util.Hashtable;
 import java.util.Map.Entry;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 
 import com.gentics.cr.util.indexing.AbstractUpdateCheckerJob;
 import com.gentics.cr.util.indexing.IndexController;
 import com.gentics.cr.util.indexing.IndexJobQueue;
 import com.gentics.cr.util.indexing.IndexLocation;
 
 /**
  * Nagios Servlet for IndexJobServlet. 
  */
 public class IndexJobNagiosServlet extends HttpServlet {
 	
 	private static final long serialVersionUID = -1686996634146038875L;
 	
 	private static final Logger LOGGER = Logger.getLogger(IndexJobNagiosServlet.class);
 	protected IndexController indexer;
 	private static final String NAGIOS_PARAM = "nagios";
 	
 	public void init(final ServletConfig config) throws ServletException {
 		super.init(config);
 		this.indexer = initIndexController(config);
 	}
 	
 	/**
 	 * implemented as own method to change executed context.
 	 * 
 	 * @param config
 	 * @return indexController
 	 */
 	public IndexController initIndexController(final ServletConfig config) {
 		return new IndexController("indexer");
 	}
 	
 	@Override
 	public final void destroy() {
 	if (indexer != null) {
 			indexer.stop();
 		}
 	}
 	
 	public final void doService(final HttpServletRequest request, 
 			final HttpServletResponse response) throws IOException {
 		
 		LOGGER.debug("Request:" + request.getQueryString());
 		
 		String nagString = request.getParameter(NAGIOS_PARAM);
 		
 		String index = request.getParameter("idx");
 		response.setContentType("text/plain");
 		Hashtable<String, IndexLocation> indexTable = indexer.getIndexes();
 		for (Entry<String, IndexLocation> e : indexTable.entrySet()) {
 			if (e.getKey().equalsIgnoreCase(index)) {
 				IndexLocation loc = e.getValue();
 				IndexJobQueue queue = loc.getQueue();
 				if (queue != null && queue.isRunning()) {
 					response.getWriter().write("WorkerThread:OK\n");
 				} else {
 					response.getWriter().write("WorkerThread:NOK\n");
 				}
 				response.getWriter().write("ObjectsInIndex:" + loc.getDocCount() + "\n");
				if (queue != null) {
					AbstractUpdateCheckerJob j = queue.getCurrentJob();
					if (j != null) {
						response.getWriter().write("CurrentJobObjectsToIndex:"
								+ j.getObjectsToIndex() + "\n");
					}
 				}
				
 			}
 		}
 	}
 	
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		doService(req, resp);
 	}
 	
 	protected String getAction(HttpServletRequest request) {
 		return request.getParameter("action");
 	}
 
 }
