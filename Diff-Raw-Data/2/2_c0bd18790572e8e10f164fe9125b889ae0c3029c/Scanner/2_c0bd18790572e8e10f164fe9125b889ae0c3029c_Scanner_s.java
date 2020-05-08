 package net.fluxo.updater.processor;
 
 import net.fluxo.updater.DatabaseManager;
 import net.fluxo.updater.dbo.BrisData;
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 import org.joda.time.Period;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Attribute;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import java.io.IOException;
 import java.net.SocketTimeoutException;
 import java.sql.SQLException;
 import java.util.List;
 import java.util.concurrent.Callable;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Ronald Kurniawan (viper)
  * Date: 28/06/13
  * Time: 11:43 AM
  * <p/>
  * This class implements Runnable and is the class to be processed by the ThreadPool
  * implementation. For testing reasons, we're not going to touch Runnable stuff at first...
  */
 public class Scanner implements Callable<String> {
 
 	private BrisData _obj;
 	private String _fileURLToDownload;
 	private String _dateUpdated;
 	private char _updateFreq;
 	private Logger _logger = Logger.getLogger("net.fluxo");
 	private boolean _tryReconnect = false;
 	private int _retries = 1;
 	private int _retrySleepInMinutes = 1;
 
 	public Scanner(BrisData object, int retries, int retrySleepInMinutes) {
 		_obj = object;
 		_retries = retries;
 		_retrySleepInMinutes = retrySleepInMinutes;
 	}
 
 	public void processPage() {
 		try {
 			Document doc = Jsoup.connect(_obj.getDatasetURL()).get();
 			// get the url to download
 			Elements anchors = doc.getElementsByTag("a");
 			for (Element anc : anchors) {
 				boolean isURL = isFileURL(anc, _obj.getFile());
 				if (isURL) {
 					_logger.info("Connecting to: " + _fileURLToDownload);
 					break;
 				}
 			}
 			// We are going to check for last updated and created date
 			String strLastUpdated = null;
 			String strDateCreated = null;
 			Elements tr = doc.getElementsByAttributeValue("scope", "row");
 			for (Element e : tr) {
 				if (e.text().indexOf("webstore last updated") >= 0) {
 					Element valueElement = e.nextElementSibling();
 					strLastUpdated = valueElement.text();
 				} else if (e.text().indexOf("created") >= 0) {
 					Element valueElement = e.nextElementSibling();
 					strDateCreated = valueElement.text();
 				}
 			}
 			DateTimeFormatter formatter = DateTimeFormat.forPattern("MMMM dd, yyyy");
 			DateTime dtLastUpdated = null;
 			DateTime dtDateCreated = null;
 			if (strLastUpdated != null) {
 				dtLastUpdated = formatter.parseDateTime(strLastUpdated);
 				if (dtLastUpdated.isAfter(_obj.getLastChecked())) {
 					_obj.setLastChecked(dtLastUpdated.getMillis());
 				}
 			}
 			if (strDateCreated != null) {
 				dtDateCreated = formatter.parseDateTime(strDateCreated);
 				if (dtDateCreated.isAfter(_obj.getDatePublished())) {
 					_obj.setDatePublished(dtDateCreated.getMillis());
 				}
 			}
 
 			_logger.info(_obj.getDumpTable() + ": UPDATING qld_data on record id " + _obj.getId());
 			DatabaseManager.updateBrisDataObject(_obj);
 			processFile();
 		} catch (SocketTimeoutException ste) {
 			Period p = new Period(0, _retrySleepInMinutes, 0, 0);
 			try {
 				_logger.info("Scanner: Sleeping for " + _retrySleepInMinutes + " minutes...");
 				Thread.sleep(p.getMillis());
 				_retries--;
 				_logger.info("Scanner: " + _retries + " retries left...");
 				processPage();
 			} catch (InterruptedException ie) {
 			}
 		} catch (IOException ioe) {
 			ioe.printStackTrace();
 			_logger.error("Scanner/ERROR: " + ioe.getMessage() + " caused by " + ioe.getCause().getMessage());
 
 		} catch (SQLException sqle) {
 			sqle.printStackTrace();
 			_logger.error(_obj.getDumpTable() + ": ERROR CLOSING DB: " + sqle.getMessage() + " caused by " +
 					sqle.getCause().getMessage());
 		}
 	}
 
 	public void processRss() {
 		processFile();
 	}
 
 	/**
 	 * Checks if a given element contains our file URL to download, and sets the variable for file URL.
 	 *
 	 * @param element - given HTML element
 	 * @param fileURL - our fileURL
 	 * @return true if it is our file URL
 	 */
 	private boolean isFileURL(Element element, String fileURL) {
 		List<Attribute> attrs = element.attributes().asList();
 		for (Attribute a : attrs) {
 			if (a.getKey().equals("href") && a.getValue().endsWith(fileURL) &&
 					(a.getValue().indexOf("http") >= 0)) {
 				_fileURLToDownload = a.getValue();
 				return true;
 			}
 		}
 		return false;
 	}
 
 	private void processFile() {
 		// here we go...
 		if (_fileURLToDownload == null) {
 			_logger.error("Scanner/ERROR: File URL empty for object: " + _obj.getId() + "(" + _obj.getDatasetURL() + ")");
 			return;
 		}
 		if (_fileURLToDownload.endsWith(".csv")) {
 			ProcCSV pcsv = new ProcCSV(_obj, _fileURLToDownload);
 			pcsv.scan();
		} else if (_fileURLToDownload.endsWith(".rss")) {
 			ProcRSS prss = new ProcRSS(_obj, _fileURLToDownload);
 			prss.scan();
 		} else if (_fileURLToDownload.endsWith(".kml")) {
 			ProcKML pkml = new ProcKML(_obj, _fileURLToDownload);
 			pkml.scan();
 		}
 	}
 
 	@Override
 	public String call() {
 		if (_obj.getDatasetURL().endsWith(".rss") || _obj.getDatasetURL().indexOf(".rss") > -1) {
 			_fileURLToDownload = _obj.getDatasetURL();
 			processRss();
 		} else {
 			processPage();
 		}
 		return "OK";
 	}
 }
