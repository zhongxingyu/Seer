 package gov.cida.sedmap.data;
 
 import gov.cida.sedmap.io.FileDownloadHandler;
 import gov.cida.sedmap.io.InputStreamWithFile;
 import gov.cida.sedmap.io.IoUtils;
 import gov.cida.sedmap.io.WriterWithFile;
 import gov.cida.sedmap.io.util.StrUtils;
 import gov.cida.sedmap.io.util.StringValueIterator;
 import gov.cida.sedmap.ogc.FilterWithViewParams;
 import gov.cida.sedmap.ogc.OgcUtils;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.net.URL;
 import java.net.URLConnection;
 import java.sql.SQLException;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.naming.Context;
 import javax.naming.NamingException;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import org.apache.log4j.Logger;
 import org.geotools.filter.AbstractFilter;
 import org.opengis.filter.PropertyIsGreaterThan;
 import org.opengis.filter.PropertyIsGreaterThanOrEqualTo;
 import org.opengis.filter.PropertyIsLessThan;
 import org.opengis.filter.PropertyIsLessThanOrEqualTo;
 
 public abstract class Fetcher {
 
 	public static final String SEDMAP_DS = "java:comp/env/jdbc/sedmapDS";
 
 	private static final Logger logger = Logger.getLogger(Fetcher.class);
 
	protected static String NWIS_URL = "http://waterservices.usgs.gov/nwis/dv/?format=_format_&sites=_sites_&startDT=_startDate_&endDT=_endDate_&statCd=00003&parameterCd=00060,80154,80155";
 
 	public static FetcherConfig conf;
         public static final int NUM_NWIS_TRIES = 3;
 
 	protected String getDataTable(String descriptor) {
 		return conf.DATA_TABLES.get(descriptor);
 	}
 	protected List<Column> getTableMetadata(String tableName) {
 		return conf.getTableMetadata(tableName);
 	}
 	protected Context getContext() throws NamingException {
 		return conf.getContext();
 	}
 
 
 	public abstract Fetcher initJndiJdbcStore(String jndiJdbc) throws IOException;
 
 	protected abstract InputStreamWithFile handleSiteData(String descriptor, FilterWithViewParams filter, Formatter formatter)
 			throws IOException, SQLException, NamingException;
 	protected abstract InputStreamWithFile handleDiscreteData(Iterator<String> sites, FilterWithViewParams filter, Formatter formatter)
 			throws IOException, SQLException, NamingException;
 
 	protected InputStreamWithFile handleNwisData(Iterator<String> sites, FilterWithViewParams filter, Formatter formatter)
 			throws IOException, SQLException, NamingException {
 		if ( !sites.hasNext() ) {
 			// return nothing if there are no sites
 			return null;
 		}
 
 		// NWIS offers RDB only that is compatible with sedmap needs
 		Formatter rdb = new RdbFormatter();
 		String format = "rdb";
 		String url = NWIS_URL.replace("_format_", format);
 
 		// extract expressions from the filter we can handle
 		String yr1 = filter.getViewParam("yr1");
 		String yr2 = filter.getViewParam("yr2");
 
 		// translate the filters to NWIS Web query params
 		String startDate = yr1 + "-01-01"; // Jan  1st of the given year
 		String endDate   = yr2 + "-12-31"; // Dec 31st of the given year
 
 		url = url.replace("_startDate_", startDate);
 		url = url.replace("_endDate_",   endDate);
 
 		// we need to include the second header line for rdb format
 		int headerLines    = formatter instanceof RdbFormatter ?2 :1;
 		boolean needHeader = true;
 		int readLineCountAfterComments = 0;
                 String sitesUrl = null;
 		// open temp file
 		WriterWithFile tmp = null;
 		try {
 			tmp = IoUtils.createTmpZipWriter("daily_data", formatter.getFileType());
                         tmp.write(formatter.fileHeader(HeaderType.DAILY));
 			while (sites.hasNext()) {
 				int batch = 0;
 				String sep = "";
 				StringBuilder siteList = new StringBuilder();
 
 				// site list should be in batches of 99 site IDs
 				while (++batch<99 && sites.hasNext()) {
 					siteList.append(sep).append(sites.next());
 					sep=",";
 				}
 				sitesUrl = url.replace("_sites_",   siteList);
 
 				// fetch the data from NWIS
 				BufferedReader nwis = null;
 				try {
 					nwis = fetchNwisData(sitesUrl);
 
 					String line;
 					while ((line = nwis.readLine()) != null) {
 						if (line.startsWith("#")) {
 							readLineCountAfterComments = 0;
 							continue;
 						}
 						if (readLineCountAfterComments++<2) {
 							if (needHeader) {
 								if (readLineCountAfterComments==1) {
 									line = reconditionLine(line);
 								}
 								needHeader = readLineCountAfterComments < headerLines;
 							} else {
 								continue;
 							}
 						}
 						// translate from NWIS RDB format to requested format
 						line = formatter.transform(line, rdb);
 						tmp.write(line);
 						tmp.write(IoUtils.LINE_SEPARATOR);
 					}
 				} finally {
 					IoUtils.quiteClose(nwis);
 				}
 			}
 		}
                 catch(Exception e){
                     if(null != tmp){
                         StringBuilder errMsgBuilder = new StringBuilder();
                         tmp.deleteFile();
                         tmp = IoUtils.createTmpZipWriter("daily_data", formatter.getFileType());
                         
                         errMsgBuilder.append("Error Retrieving Daily Data");
                        errMsgBuilder.append(IoUtils.LINE_SEPARATOR);
                         if(null != sitesUrl && sitesUrl.length() > 0 ){
                             errMsgBuilder.append("No data could be retrieved from the following url:");
                             errMsgBuilder.append(sitesUrl);
                         }
                         else{
                             errMsgBuilder.append("There was an error forming the url for the NWIS web query");
                         }
                        errMsgBuilder.append(IoUtils.LINE_SEPARATOR);
                         errMsgBuilder.append(e.getMessage());
                        errMsgBuilder.append(IoUtils.LINE_SEPARATOR);
                         
                         //get stack trace as a string
                         StringWriter sw = new StringWriter();
                         PrintWriter pw = new PrintWriter(sw);
                         e.printStackTrace(pw);
                         
                         errMsgBuilder.append(sw.toString());
                         String errMsg = errMsgBuilder.toString();
                         
                         tmp.write(errMsg);
                         logger.error(errMsg, e);
                     }
                 }
                 finally {
 			IoUtils.quiteClose(tmp);
 		}
 
 		return new InputStreamWithFile( IoUtils.createTmpZipStream( tmp.getFile() ), tmp.getFile());
 	}
 
 
 	protected String reconditionLine(String line) {
 		line = line.replaceAll("\\d\\d_00060_00003_cd", "DAILY_FLOW_QUAL");
 		line = line.replaceAll("\\d\\d_00060_00003",    "DAILY_FLOW");
 		line = line.replaceAll("\\d\\d_80155_00003_cd", "DAILY_SSL_QUAL");
 		line = line.replaceAll("\\d\\d_80155_00003",    "DAILY_SSL");
 		line = line.replaceAll("\\d\\d_80154_00003_cd", "DAILY_SSC_QUAL");
 		line = line.replaceAll("\\d\\d_80154_00003",    "DAILY_SSC");
 		return line;
 	}
 
 
 	protected BufferedReader fetchNwisData(String urlStr) throws IOException {
 		URL url = new URL(urlStr);
 		URLConnection cn = url.openConnection();
                final int timeout = 30000;//30sec
                 cn.setConnectTimeout(timeout);
                 cn.setReadTimeout(timeout);
                 
                 BufferedReader reader = null;
                 int nwisTriesCount = 0;
                 while(null == reader && nwisTriesCount < NUM_NWIS_TRIES){
                     try{
                         reader = new BufferedReader(new InputStreamReader(cn.getInputStream()));
                     }
                     catch(IOException e){
                         if(nwisTriesCount == NUM_NWIS_TRIES -1){
                             throw e;
                         }
                     }
                     nwisTriesCount++;
                 }
 		return reader;
 	}
 
 
 	public void doFetch(HttpServletRequest req, FileDownloadHandler handler)
 			throws ServletException, IOException {
 		logger.debug("doFetch");
 
 		String    dataTypes = getDataTypes(req);
 		Formatter formatter = getFormatter(req);
 		boolean   alsoFlow  = getDiscreteFlow(req);
 		List<String> sites  = new LinkedList<String>();
 
 		handler.beginWritingFiles(); // start writing files
 
 		for (String site  : conf.DATA_TYPES) { // check for daily and discrete
 			if ( ! dataTypes.contains(site) ) continue;
 
 			String    ogcXml = getFilter(req, site);
 			AbstractFilter aFilter = OgcUtils.ogcXmlToFilter(ogcXml);
 			@SuppressWarnings("unchecked") // fyi: for some reason this is not require for all jdks
 			String yr1 = OgcUtils.removeFilter(aFilter, "year", PropertyIsGreaterThanOrEqualTo.class, PropertyIsGreaterThan.class);
 			@SuppressWarnings("unchecked") // fyi: for some reason this is not require for all jdks
 			String yr2 = OgcUtils.removeFilter(aFilter, "year", PropertyIsLessThanOrEqualTo.class,    PropertyIsLessThan.class);
 			FilterWithViewParams filter = new FilterWithViewParams(aFilter);
 			filter.putViewParam("yr1", "1900", yr1);
 			filter.putViewParam("yr2", "2100", yr2);
 
 			for (String value : conf.DATA_VALUES) { // check for sites and data
 				if ( ! dataTypes.contains(value) ) continue;
 
 				StringBuilder   name = new StringBuilder();
 				String    descriptor = name.append(site).append('_').append(value).toString();
 				String      filename = descriptor + formatter.getFileType();
 
 				InputStreamWithFile fileData = null;
 				try {
 					// TODO this was originally going to be a single call but reality got in the way - could use a refactor
 					if ( "daily_data".equals(descriptor) ) {
 						fileData = handleNwisData(sites.iterator(), filter, formatter);
 					} else if ( "discrete_data".equals(descriptor) ) {
 						fileData = handleDiscreteData(sites.iterator(), filter, formatter);
 					} else if (descriptor.contains("sites") ) {
 						fileData = handleSiteData(descriptor, filter, formatter);
 						List<String> descriptorSites = makeSiteIterator(fileData, formatter);
 
 						if ( ! alsoFlow) { // if the user does not want discrete flow data
 							sites.clear(); // do not retain the discrete sites for NWIS daily data
 						}
 
 						if (sites.size() == 0) { // this is a short circuit to prevent an unneccessary loop
 							sites = descriptorSites;
 						} else {
 							// TODO refactor to use sorted set
 							for (String siteNo : descriptorSites) {
 								if ( ! sites.contains(siteNo) ) {
 									sites.add(siteNo);
 								}
 							}
 						}
 					}
 					// if we found some sites or data then append it to the collection
 					if ( fileData != null && !sites.isEmpty() ) {
 						handler.writeFile(formatter.getContentType(), filename, fileData);
 					}
 				} catch (Exception e) {
 					String msg = "failed to fetch from DB";
 					logger.error(msg);
 					logger.error(e);
 					throw new ServletException(msg, e);
 				} finally {
 					IoUtils.quiteClose(fileData);
 					if (fileData != null) {
 						fileData.deleteFile(); // TODO these files are not deleting. it must still be open?
 					}
 				}
 			}
 		}
 		handler.finishWritingFiles(); // done writing files
 
 	}
 
 
 	// TODO this needs a bit of finessing because we want to be able to read the site data twice.
 	// once for the use return download and again for the NWIS site list.
 	// right now, it assumes that the data comes from a cached file.
 	// it uses the formatter that created the file to know how to split the file
 	protected List<String> makeSiteIterator(InputStream fileData, Formatter formatter) throws IOException {
 
 		LinkedList<String> sites = new LinkedList<String>();
 
 		if (fileData instanceof InputStreamWithFile) {
 			File file = ((InputStreamWithFile) fileData).getFile(); // TODO change to zip for temp files
 			if (file==null) return StringValueIterator.EMPTY;
 			InputStream       fin = null;// IoUtils.createTmpZipStream(file) );
 			InputStreamReader rin = null;
 			BufferedReader reader = null;
 
 			try {
 				fin    = new FileInputStream(file);
 				rin    = new InputStreamReader(fin);
 				reader = new BufferedReader(rin);
 
 				String line;
 				while ( (line=reader.readLine()) != null ) {
 					int pos = line.indexOf( formatter.getSeparator() );
 					if (pos == -1) continue; // trap empty lines
 					String site = line.substring(0, pos );
 					// only preserve site numbers and not comments or header info
 					if (site.matches("^\\d+$")) {
 						sites.add(site);
 					}
 				}
 			} finally {
 				IoUtils.quiteClose(reader, rin, fin);
 			}
 		}
 
 		return sites;
 	}
 
 
 
 	protected String getFilter(HttpServletRequest req, String dataType) {
 		String ogcXml = req.getParameter(dataType+"Filter");
 
 		if (ogcXml == null) {
 			logger.warn("Failed to locate OGC 'filter' parameter - using default");
 			// TODO empty result
 			return "";
 		}
 
 		return ogcXml;
 	}
 
 
 
 	protected boolean getDiscreteFlow(HttpServletRequest req) {
 		String  flow     = req.getParameter("dataTypes");
 		boolean alsoFlow = ! StrUtils.isEmpty(flow) && flow.toLowerCase().contains("flow");
 		return alsoFlow;
 	}
 
 
 	protected String getDataTypes(HttpServletRequest req) {
 		String types = req.getParameter("dataTypes");
 
 		// expecting a string with terms "daily_discrete_sites_data" in it
 		// sites means site data - no samples
 		// data  means site data - no site info
 		// daily and discrete refer to samples
 		// - "daily_discrete_sites_data" means they want all data
 		// - "daily_sites" means they want all daily site info only
 
 		if (types == null) {
 			logger.warn("Failed to locate 'dataTypes' parameter - using default");
 			types = "daily_discrete_sites";
 		}
 
 		return types;
 	}
 
 
 
 	protected Formatter getFormatter(HttpServletRequest req) {
 		String format = req.getParameter("format");
 		format = conf.FILE_FORMATS.containsKey(format) ?format :"tsv";
 
 		Formatter formatter = null;
 		try {
 			formatter = conf.FILE_FORMATS.get(format).newInstance();
 		} catch (Exception e) {
 			logger.warn("Could not instantiate formatter for '" +format+"' with class "
 					+conf.FILE_FORMATS.get(format)+". Using RDB as fall-back.");
 			formatter = new RdbFormatter();
 		}
 
 		return formatter;
 	}
 
 }
