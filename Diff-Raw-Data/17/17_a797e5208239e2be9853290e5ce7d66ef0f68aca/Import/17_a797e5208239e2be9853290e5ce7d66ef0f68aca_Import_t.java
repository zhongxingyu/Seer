 package kea.kme.pullpit.server.services;
 
 import java.io.IOException;
 import java.sql.SQLException;
 
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import kea.kme.pullpit.server.persistence.LogHandler;
 import kea.kme.pullpit.server.persistence.PodioObjectHandler;
 import kea.kme.pullpit.server.podio.PodioImporter;
 
 public class Import extends HttpServlet {
 	private static final long serialVersionUID = 4434374770986473879L;
 	boolean[] done;
 
 	public void doGet(HttpServletRequest req, HttpServletResponse res)
 			throws IOException {
 		String isCron = req.getParameter("cron");
		if (isCron!=null) {
			try {
				PodioObjectHandler.truncateAllPodioTables();
			} catch (SQLException e) {
				e.printStackTrace(System.err);
			}
		} else {
			res.getWriter().write("HTTP/1.0 200 OK");
			res.getWriter().close();
		}
 		createLog();
 		try {
 			pullShows();
 			pullBands();
 			pullVenues();
 			writeStaticObjects();
 		} catch (Exception e) {
 			e.printStackTrace(System.err);
 		}
 	}
 
 	public void pullShows() throws Exception {
 		PodioImporter importer = PodioImporter.getInstance();
 		updateLog("imp1", -1);
 		updateLog("imp1", importer.pullShows());
 	}
 
 	public void pullBands() throws Exception {
 		PodioImporter importer = PodioImporter.getInstance();
 		updateLog("imp2", -1);
 		updateLog("imp2", importer.pullBands());
 	}
 
 	public void pullVenues() throws Exception {
 		PodioImporter importer = PodioImporter.getInstance();
 		updateLog("imp3", -1);
 		updateLog("imp3", importer.pullVenues());
 	}
 
 	public void writeStaticObjects() throws Exception {
 		PodioImporter importer = PodioImporter.getInstance();
 		updateLog("imp4", -1);
 		updateLog("imp5", -1);
 		updateLog("imp6", -1);
 		updateLog("imp7", -1);
 		int[] results = importer.writeStaticObjects();
 		for (int i = 0; i < results.length; i++)
 			updateLog("imp" + (i + 4), results[i]);
 		updateLog("status", 0);
 	}
 
 	private void updateLog(String key, int value) {
 		try {
 			LogHandler.updateImportLog(key, value);
 		} catch (SQLException e) {
 			e.printStackTrace(System.err);
 		}
 	}
 
 	private void createLog() {
 		try {
 			LogHandler.createImportLog();
 		} catch (SQLException e) {
 			e.printStackTrace(System.err);
 		}
 	}
 
 }
