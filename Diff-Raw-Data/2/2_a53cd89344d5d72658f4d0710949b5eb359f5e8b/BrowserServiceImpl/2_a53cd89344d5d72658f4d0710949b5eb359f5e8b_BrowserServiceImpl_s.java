 /*--------------------------------------------------------------------------
  *  Copyright 2007 utgenome.org
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *--------------------------------------------------------------------------*/
 //--------------------------------------
 // GenomeBrowser Project
 //
 // BrowserServiceImpl.java
 // Since: Apr 20, 2007
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.gwt.utgb.server;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpSession;
 
 import net.sf.samtools.SAMFileReader;
 import net.sf.samtools.SAMRecord;
 
 import org.utgenome.UTGBException;
 import org.utgenome.format.fasta.CompactFASTA;
 import org.utgenome.format.keyword.KeywordDB;
 import org.utgenome.format.sam.SAM2SilkReader;
 import org.utgenome.format.wig.WIGDatabaseReader;
 import org.utgenome.graphics.GenomeWindow;
 import org.utgenome.gwt.utgb.client.BrowserService;
 import org.utgenome.gwt.utgb.client.UTGBClientErrorCode;
 import org.utgenome.gwt.utgb.client.UTGBClientException;
 import org.utgenome.gwt.utgb.client.bean.DatabaseEntry;
 import org.utgenome.gwt.utgb.client.bean.track.TrackDescription;
 import org.utgenome.gwt.utgb.client.bio.AlignmentResult;
 import org.utgenome.gwt.utgb.client.bio.ChrLoc;
 import org.utgenome.gwt.utgb.client.bio.ChrRange;
 import org.utgenome.gwt.utgb.client.bio.CompactWIGData;
 import org.utgenome.gwt.utgb.client.bio.Gene;
 import org.utgenome.gwt.utgb.client.bio.GenomeDB;
 import org.utgenome.gwt.utgb.client.bio.Interval;
 import org.utgenome.gwt.utgb.client.bio.KeywordSearchResult;
 import org.utgenome.gwt.utgb.client.bio.OnGenomeDataSet;
 import org.utgenome.gwt.utgb.client.bio.ReadQueryConfig;
 import org.utgenome.gwt.utgb.client.bio.SAMRead;
 import org.utgenome.gwt.utgb.client.bio.WigGraphData;
 import org.utgenome.gwt.utgb.client.track.bean.TrackBean;
 import org.utgenome.gwt.utgb.client.view.TrackView;
 import org.utgenome.gwt.utgb.server.app.ChromosomeMap;
 import org.utgenome.gwt.utgb.server.app.ReadView;
 import org.utgenome.gwt.utgb.server.util.WebApplicationResource;
 import org.xerial.ObjectHandlerBase;
 import org.xerial.core.XerialException;
 import org.xerial.db.DBException;
 import org.xerial.db.sql.SQLExpression;
 import org.xerial.db.sql.sqlite.SQLiteAccess;
 import org.xerial.db.sql.sqlite.SQLiteCatalog;
 import org.xerial.json.JSONArray;
 import org.xerial.json.JSONObject;
 import org.xerial.lens.Lens;
 import org.xerial.util.StopWatch;
 import org.xerial.util.bean.BeanUtil;
 import org.xerial.util.log.Logger;
 
 import com.google.gwt.rpc.server.RpcServlet;
 
 /**
  * 
  * {"species":{"type":0, "target":"human"}, "genome":{"type":0}, "chromosome":{"type":0}}
  * 
  * 
  * @author leo
  * 
  */
 public class BrowserServiceImpl extends RpcServlet implements BrowserService {
 
 	static Logger _logger = Logger.getLogger(BrowserServiceImpl.class);
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	public BrowserServiceImpl() throws DBException {
 
 	}
 
 	private static String sanitizeViewName(String name) {
 		return name.replaceAll("\\.\\.", "");
 	}
 
 	public TrackView createTrackView(String silk) throws UTGBClientException {
 		try {
 			TrackView v = Lens.loadSilk(TrackView.class, new StringReader(silk));
 			return v;
 		}
 		catch (Exception e) {
 			throw new UTGBClientException(UTGBClientErrorCode.PARSE_ERROR, "parse error: " + e.getMessage());
 		}
 	}
 
 	public TrackView getTrackView(String viewName) throws UTGBClientException {
 
 		if (viewName.startsWith("http://")) {
 			_logger.info(String.format("loading view: ", viewName));
 			String view = getHTTPContent(viewName);
 			try {
 				TrackView v = Lens.loadSilk(TrackView.class, new BufferedReader(new InputStreamReader(new URL(viewName).openStream())));
 				return v;
 			}
 			catch (IOException e) {
 				throw new UTGBClientException(UTGBClientErrorCode.MISSING_FILES, "failed to retrieve view from " + viewName);
 			}
 			catch (XerialException e) {
 				throw new UTGBClientException(UTGBClientErrorCode.PARSE_ERROR, "parse error: " + e.getMessage());
 			}
 		}
 		else {
 			File viewFile = new File("config/view", sanitizeViewName(viewName) + ".silk");
 			try {
 
 				FileReader f = null;
 				try {
 					File viewFilePath = new File(UTGBMaster.getProjectRootFolder(), viewFile.getPath());
 					_logger.info(String.format("loading view:" + viewFile));
 					if (!viewFilePath.exists())
 						throw new UTGBClientException(UTGBClientErrorCode.MISSING_FILES, String.format("%s is not found", viewFile));
 
 					f = new FileReader(viewFilePath);
 					TrackView v = Lens.loadSilk(TrackView.class, f);
 					return v;
 				}
 				catch (UTGBException e) {
 					throw new UTGBClientException(UTGBClientErrorCode.NOT_IN_PROJECT_ROOT, String.format("not in the project root"));
 				}
 				catch (XerialException e) {
 					throw new UTGBClientException(UTGBClientErrorCode.PARSE_ERROR, String.format("parse error (%s): ", e));
 				}
 				finally {
 					if (f != null)
 						f.close();
 				}
 			}
 			catch (IOException e) {
 				throw new UTGBClientException(UTGBClientErrorCode.IO_ERROR, String.format("failed to close vilew file %s: %s", viewFile, e));
 			}
 		}
 	}
 
 	public String getDataModelParameter(String amoebaQuery) {
 		return null;
 	}
 
 	public String getHTTPContent(String url) {
 		try {
 
 			BufferedReader in = openURL(url);
 
 			StringWriter buffer = new StringWriter();
 			BufferedWriter out = new BufferedWriter(buffer);
 			String line;
 			while ((line = in.readLine()) != null) {
 				out.append(line);
 				out.newLine();
 			}
 			in.close();
 			out.flush();
 			return buffer.toString();
 		}
 		catch (IOException e) {
 			_logger.error(e);
 			return "";
 		}
 	}
 
 	public String getDatabaseCatalog(String jdbcAddress) {
 		try {
 			// TODO connection pool
 			SQLiteAccess dbAccess = new SQLiteAccess(jdbcAddress);
 			SQLiteCatalog catalog = dbAccess.getCatalog();
 			String json = catalog.toJSON();
 			_logger.debug("catalog: " + json);
 			return json;
 		}
 		catch (DBException e) {
 			_logger.error(e);
 			return "";
 		}
 	}
 
 	public String getTableData(String jdbcAddress, String tableName) {
 		try {
 			SQLiteAccess dbAccess = new SQLiteAccess(jdbcAddress);
 			String sql = SQLExpression.fillTemplate("select * from $1", tableName);
 			List<JSONObject> result = dbAccess.jsonQuery(sql);
 			JSONObject resultObj = new JSONObject();
 			JSONArray resultArray = new JSONArray();
 			for (JSONObject row : result) {
 				resultArray.add(row);
 			}
 			resultObj.put("data", resultArray);
 
 			String json = resultObj.toJSONString();
 			_logger.debug("query result: " + json);
 			return json;
 		}
 		catch (DBException e) {
 			_logger.error(e);
 			return "";
 		}
 	}
 
 	public List<TrackBean> getTrackList(int entriesPerPage, int page) {
 		ArrayList<TrackBean> trackList = new ArrayList<TrackBean>();
 		ArrayList<String> trackXMLFileList = WebApplicationResource.find(this.getServletContext(), "/tracks/", "(.*)\\.xml$", true);
 		for (String xmlPath : trackXMLFileList) {
 			try {
 				trackList.add(loadTrackInfo(xmlPath));
 			}
 			catch (UTGBException e) {
 				_logger.error(e);
 			}
 		}
 
 		return trackList;
 	}
 
 	private TrackBean loadTrackInfo(String trackXMLResourcePath) throws UTGBException {
 		try {
 			BufferedReader xmlReader = WebApplicationResource.openResource(this.getServletContext(), trackXMLResourcePath);
 			TrackBean trackInfo = BeanUtil.createXMLBean(TrackBean.class, xmlReader);
 			return trackInfo;
 		}
 		catch (XerialException e) {
 			throw new UTGBException(e);
 		}
 		catch (IOException e) {
 			throw new UTGBException(e);
 		}
 	}
 
 	public List<TrackBean> getTrackList(String prefix, int entriesPerPage, int page) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public int numHitsOfTracks(String prefix) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	//	public SearchResult keywordSearch(String species, String revision, String keyword, int numEntriesPerPage, int page) {
 	//
 	//		String keywordSearchURL = "http://utgenome.org/service/utgb-keyword/search";
 	//		keywordSearchURL += String.format("?revision=%s&keyword=%s&page=%d&pagewidth=%d", revision, keyword, page, numEntriesPerPage);
 	//		if (species != null)
 	//			keywordSearchURL += "&species=" + species;
 	//
 	//		_logger.debug(keywordSearchURL);
 	//
 	//		boolean isScaffoldSearch = keyword.toLowerCase().contains("scaffold");
 	//
 	//		try {
 	//			URL url = new URL(keywordSearchURL);
 	//			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
 	//			SearchResult searchResult = new SearchResult();
 	//			BeanUtil.populateBeanWithJSON(searchResult, in);
 	//
 	//			return searchResult;
 	//		}
 	//		catch (MalformedURLException e) {
 	//			_logger.error(e);
 	//		}
 	//		catch (IOException e) {
 	//			_logger.error(e);
 	//		}
 	//		catch (XerialException e) {
 	//			_logger.error(e);
 	//		}
 	//
 	//		return null;
 	//	}
 
 	public KeywordSearchResult keywordSearch(String species, String revision, String keyword, int numEntriesPerPage, int page) throws UTGBClientException {
 
 		File keywordDBFile = new File(WebTrackBase.getProjectRootPath(), "db/keyword.sqlite");
 		KeywordDB db = null;
 		try {
 			db = new KeywordDB(keywordDBFile);
 			return db.query(revision, keyword, page, numEntriesPerPage);
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 			throw new UTGBClientException(e.getMessage());
 		}
 		finally {
 			if (db != null)
 				try {
 					db.close();
 				}
 				catch (DBException e) {
 					throw new UTGBClientException(e.getMessage());
 				}
 		}
 
 	}
 
 	public void saveView(String viewXML) {
 		HttpServletRequest request = this.getThreadLocalRequest();
 		HttpSession session = request.getSession(true);
 		session.setAttribute("view", viewXML);
 	}
 
 	public String getCurrentView() {
 		HttpServletRequest request = this.getThreadLocalRequest();
 		HttpSession session = request.getSession(true);
 		_logger.debug("session ID = " + session.getId());
 		String viewXML = (String) session.getAttribute("view");
 		if (viewXML == null || (viewXML != null && viewXML.length() == 0)) {
 			try {
 				String xml = WebApplicationResource.getContent(this.getServletContext(), "/view/standard.xml");
 				return xml;
 			}
 			catch (IOException e) {
 				_logger.error(e);
 				return null;
 			}
 		}
 		return viewXML;
 	}
 
 	public TrackDescription getTrackDescription(String url) {
 
 		try {
 			BufferedReader in = openURL(url);
 			return BeanUtil.createXMLBean(TrackDescription.class, in);
 		}
 		catch (IOException e) {
 			_logger.error(e);
 		}
 		catch (XerialException e) {
 			_logger.error(e);
 		}
 
 		return null;
 	}
 
 	private BufferedReader openURL(String url) throws IOException {
 		BufferedReader in;
 		if (!url.startsWith("http://")) {
 			if (!url.startsWith("/"))
 				url = "/" + url;
 			_logger.debug("proxy request: " + url);
 			in = WebApplicationResource.openResource(this.getServletContext(), url);
 		}
 		else {
 			URL address = new URL(url);
 			_logger.debug("proxy request: " + url);
 			in = new BufferedReader(new InputStreamReader(address.openStream()));
 		}
 		return in;
 	}
 
 	static class BeanRetriever<T> extends ObjectHandlerBase<T> {
 		private ArrayList<T> geneList = new ArrayList<T>();
 
 		public BeanRetriever() {
 		}
 
 		public ArrayList<T> getResult() {
 			return geneList;
 		}
 
 		public void handle(T bean) throws Exception {
 			geneList.add(bean);
 		}
 
 		public void handleException(Exception e) throws Exception {
 			_logger.error(e);
 		}
 	}
 
 	static class RefseqGeneRetriever extends BeanRetriever<Gene> {
 
 		@Override
 		public void handle(Gene g) throws Exception {
 			g.adjustToOneOrigin();
 			super.handle(g);
 		}
 
 	}
 
 	public List<Gene> getGeneList(String serviceURI) {
 
 		StopWatch sw = new StopWatch();
 		try {
 			BufferedReader reader = openURL(serviceURI);
 
 			RefseqGeneRetriever geneRetriever = new RefseqGeneRetriever();
 			Lens.findFromJSON(reader, "gene", Gene.class, geneRetriever);
 			return geneRetriever.getResult();
 		}
 		catch (Exception e) {
 			_logger.error(e);
 		}
 		finally {
 			_logger.debug("proxy request: done " + sw.getElapsedTime() + " sec");
 		}
 		return new ArrayList<Gene>(); // no result
 	}
 
 	public AlignmentResult getAlignment(String serviceURI, String target, String sequence) {
 		AlignmentResult result = new AlignmentResult();
 		try {
 
 			BufferedReader reader = openURL(serviceURI);
 			BeanUtil.populateBeanWithJSON(result, reader);
 		}
 		catch (Exception e) {
 			_logger.error(e);
 		}
 		return result;
 	}
 
 	public ChrRange getChrRegion(String species, String revision) {
 		return ChromosomeMap.getChrRegion(species, revision);
 	}
 
 	public List<Interval> getLocusList(String dbGroup, String dbName, ChrLoc location) {
 
 		String bssFolder = UTGBMaster.getUTGBConfig().getProperty("utgb.db.folder", WebTrackBase.getProjectRootPath() + "/db");
 		File dbFile = new File(bssFolder, String.format("%s/%s", dbGroup, dbName));
 
 		if (dbFile.exists()) {
 			try {
 				SQLiteAccess dbAccess = new SQLiteAccess(dbFile.getAbsolutePath());
 				String sql = WebTrackBase
 						.createSQLStatement(
 								"select rowid, queryID as name, targetStart as start, targetEnd as end, strand from alignment where target = '$1' and start between $2 - $4 and $3 + $4 and (start <= $3 or end >= $2) order by start, end-start desc",
 								location.chr, location.start, location.end, 1000);
 				//_logger.info(sql);
 
 				List<Interval> result = dbAccess.query(sql, Interval.class);
 				//_logger.info(Lens.toJSON(result));
 				return result;
 			}
 			catch (Exception e) {
 				_logger.error(e);
 			}
 		}
 
 		return new ArrayList<Interval>();
 	}
 
 	public List<String> getChildDBGroups(String parentDBGroup) {
 
 		String dbFolder = UTGBMaster.getUTGBConfig().getProperty("utgb.db.folder", WebTrackBase.getProjectRootPath() + "/db");
 		File parentFolder = new File(dbFolder, parentDBGroup);
 
 		ArrayList<String> result = new ArrayList<String>();
 		if (parentFolder.exists()) {
 			File[] ls = parentFolder.listFiles();
 			if (ls != null) {
 				for (File each : ls) {
 					if (each.isDirectory()) {
 						if (each.getName().startsWith("."))
 							continue;
 						result.add(parentDBGroup + "/" + each.getName());
 					}
 				}
 			}
 		}
 
 		return result;
 	}
 
 	public List<String> getDBNames(String dbGroup) {
 
 		String dbFolder = UTGBMaster.getUTGBConfig().getProperty("utgb.db.folder", WebTrackBase.getProjectRootPath() + "/db");
 		File groupFolder = new File(dbFolder, dbGroup);
 
 		ArrayList<String> result = new ArrayList<String>();
 		if (groupFolder.exists()) {
 			File[] ls = groupFolder.listFiles();
 			if (ls != null) {
 				for (File each : ls) {
 					if (each.isFile()) {
 						if (each.getName().startsWith("."))
 							continue;
 						result.add(each.getName());
 					}
 				}
 			}
 		}
 
 		return result;
 	}
 
 	public List<DatabaseEntry> getDBEntry(String dbGroup) {
 
 		String dbFolder = UTGBMaster.getUTGBConfig().getProperty("utgb.db.folder", WebTrackBase.getProjectRootPath() + "/db");
 		File groupFolder = new File(dbFolder, dbGroup);
 
 		ArrayList<DatabaseEntry> result = new ArrayList<DatabaseEntry>();
 		if (groupFolder.exists()) {
 			File[] ls = groupFolder.listFiles();
 			if (ls != null) {
 				for (File each : ls) {
 					if (each.getName().startsWith("."))
 						continue;
 					if (each.isFile()) {
 						result.add(DatabaseEntry.newFile(each.getName()));
 					}
 					else if (each.isDirectory()) {
 						result.add(DatabaseEntry.newFolder(each.getName()));
 					}
 				}
 			}
 		}
 
 		return result;
 	}
 
 	public List<WigGraphData> getWigDataList(String fileName, int windowWidth, ChrLoc location) {
 		ArrayList<WigGraphData> wigDataList = null;
 
 		try {
 			WIGDatabaseReader reader = new WIGDatabaseReader(WebTrackBase.getProjectRootPath() + "/" + fileName);
 			wigDataList = reader.getWigDataList(windowWidth, location.chr, location.start, location.end);
 			reader.close();
 		}
 		catch (Exception e) {
 			_logger.error(e);
 			e.printStackTrace(System.err);
 		}
 
 		return wigDataList;
 	}
 
 	public static CompactWIGData convertResolution(WigGraphData w, ChrLoc location, int windowWidth) {
 		CompactWIGData cwig = new CompactWIGData();
 		cwig.setTrack(w.getTrack());
 		cwig.setMaxValue(w.getMaxValue());
 		cwig.setMinValue(w.getMinValue());
 		cwig.setBrowser(w.getBrowser());
 		cwig.setTrack_id(w.getTrack_id());
 		cwig.setStart(location.start < location.end ? location.start : location.end);
 		int span = 1;
 		if (w.getTrack().containsKey("span")) {
 			span = Integer.parseInt(w.getTrack().get("span"));
 			cwig.setSpan(span);
 		}
 
 		GenomeWindow window = new GenomeWindow(location.start, location.end);
 
 		float[] pixelWiseGraphData = new float[windowWidth];
 
 		Map<Integer, Float> data = w.getData();
 		for (Map.Entry<Integer, Float> each : data.entrySet()) {
 			int xOnGenome = each.getKey();
 			float val = each.getValue();
 
 			int x1 = window.getXPosOnWindow(xOnGenome, windowWidth);
 			int x2 = window.getXPosOnWindow(xOnGenome + span, windowWidth);
 			if (x1 == x2)
 				x2 = x1 + 1;
 
 			if (x1 < 0)
 				x1 = 0;
 
 			for (int i = x1; i < x2 && i < windowWidth; ++i) {
 				float current = pixelWiseGraphData[i];
 				float abs = Math.abs(val);
 				if (current < abs) {
 					pixelWiseGraphData[i] = val; // take the max (or min for negative value)
 				}
 			}
 		}
 
 		cwig.setData(pixelWiseGraphData);
 		return cwig;
 	}
 
 	public List<CompactWIGData> getCompactWigDataList(String path, int windowWidth, ChrLoc location) {
 
 		List<WigGraphData> wig = getWigDataList(path, windowWidth, location);
 		ArrayList<CompactWIGData> cWig = new ArrayList<CompactWIGData>();
 
 		for (WigGraphData w : wig) {
 			cWig.add(convertResolution(w, location, windowWidth));
 		}
 
 		return cWig;
 	}
 
 	public List<SAMRead> getSAMReadList(String readFileName, String refSeqFileName) {
 		final ArrayList<SAMRead> readDataList = new ArrayList<SAMRead>();
 
 		try {
 			_logger.info(WebTrackBase.getProjectRootPath() + "/" + readFileName);
 			_logger.info(WebTrackBase.getProjectRootPath() + "/" + refSeqFileName);
 			final CompactFASTA cf = new CompactFASTA(WebTrackBase.getProjectRootPath() + "/" + refSeqFileName);
 
 			Lens.findFromSilk(new SAM2SilkReader(new FileReader(new File(WebTrackBase.getProjectRootPath() + "/" + readFileName))), "record", SAMRead.class,
 					new ObjectHandlerBase<SAMRead>() {
 						public void handle(SAMRead input) throws Exception {
 							input.refSeq = cf.getSequence(input.rname, input.getStart() - 1, input.getEnd()).toString();
 							_logger.info(Lens.toSilk(input));
 							readDataList.add(input);
 						}
 					});
 		}
 		catch (Exception e) {
 			_logger.error(e);
 		}
 		return readDataList;
 	}
 
 	public List<SAMRead> querySAMReadList(String bamFileName, String indexFileName, String refSeqFileName, String rname, int start, int end) {
 		final ArrayList<SAMRead> readDataList = new ArrayList<SAMRead>();
 
 		try {
 			_logger.info(WebTrackBase.getProjectRootPath() + "/" + bamFileName);
 			_logger.info(WebTrackBase.getProjectRootPath() + "/" + indexFileName);
 			_logger.info(WebTrackBase.getProjectRootPath() + "/" + refSeqFileName);
 			final CompactFASTA cf = new CompactFASTA(WebTrackBase.getProjectRootPath() + "/" + refSeqFileName);
 
 			SAMFileReader reader = new SAMFileReader(new File(WebTrackBase.getProjectRootPath() + "/" + bamFileName), new File(WebTrackBase
 					.getProjectRootPath()
 					+ "/" + indexFileName));
 
 			StopWatch st1 = new StopWatch();
 			Iterator<SAMRecord> iterator = reader.query(rname, start, end, true);
 			_logger.info("st1:" + st1.getElapsedTime());
 
 			while (iterator.hasNext()) {
 				SAMRecord record = iterator.next();
 				_logger.info(record.format());
 
 				// convert SAMRecord to SAMRead
 
				SAMRead read = ReadView.convertToSAMRead(record);
 				// get refseq
 				read.refSeq = cf.getSequence(read.rname, read.getStart() - 1, read.getEnd()).toString();
 
 				readDataList.add(read);
 			}
 		}
 		catch (Exception e) {
 			_logger.error(e);
 		}
 		return readDataList;
 	}
 
 	public String getRefSeq(String refSeqFileName, String rname, int start, int end) {
 		_logger.info(rname + ":" + start + "-" + end);
 		String refSeq = null;
 		try {
 			_logger.info(WebTrackBase.getProjectRootPath() + "/" + refSeqFileName);
 			final CompactFASTA cf = new CompactFASTA(WebTrackBase.getProjectRootPath() + "/" + refSeqFileName);
 
 			if (end - start > 10000)
 				end = start + 10000;
 
 			refSeq = cf.getSequence(rname, start - 1, end).toString();
 			_logger.info(refSeq);
 		}
 		catch (Exception e) {
 			_logger.error(e);
 		}
 		return refSeq;
 	}
 
 	public OnGenomeDataSet getOnGenomeData(GenomeDB db, ChrLoc range, ReadQueryConfig config) {
 		return ReadView.overlapQuery(db, range, config);
 	}
 
 }
