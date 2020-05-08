 //--------------------------------------
 //
 // ReadView.java
 // Since: 2009/04/27
 //
 //--------------------------------------
 package org.utgenome.gwt.utgb.server.app;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.utgenome.UTGBErrorCode;
 import org.utgenome.UTGBException;
 import org.utgenome.format.bed.BEDDatabase;
 import org.utgenome.format.sam.SAMReader;
 import org.utgenome.graphics.GenomeWindow;
 import org.utgenome.graphics.ReadCanvas;
 import org.utgenome.gwt.utgb.client.bio.ChrLoc;
 import org.utgenome.gwt.utgb.client.bio.DASLocation;
 import org.utgenome.gwt.utgb.client.bio.DASResult;
 import org.utgenome.gwt.utgb.client.bio.DASResult.DASFeature;
 import org.utgenome.gwt.utgb.client.bio.GenomeDB;
 import org.utgenome.gwt.utgb.client.bio.GenomeDB.DBType;
 import org.utgenome.gwt.utgb.client.bio.OnGenome;
 import org.utgenome.gwt.utgb.client.bio.ReadCoverage;
 import org.utgenome.gwt.utgb.client.bio.ReadQueryConfig;
 import org.utgenome.gwt.utgb.client.bio.ReadQueryConfig.Layout;
 import org.utgenome.gwt.utgb.server.WebTrackBase;
 import org.utgenome.gwt.utgb.server.util.WebApplicationResource;
 import org.xerial.silk.SilkWriter;
 import org.xerial.util.ObjectHandlerBase;
 import org.xerial.util.StopWatch;
 import org.xerial.util.log.Logger;
 
 /**
  * Web action for querying data in a specified window in a genome
  * 
  */
 public class ReadView extends WebTrackBase {
 	private static final long serialVersionUID = 1L;
 	private static Logger _logger = Logger.getLogger(ReadView.class);
 
 	public ReadView() {
 	}
 
 	public int start = -1;
 	public int end = -1;
 	public String species;
 	public String ref;
 	public String chr;
 	public int width = 700;
 	public boolean useCanvas = true;
 	public Layout layout = Layout.PILEUP;
 
 	// resource ID
 	public String path;
 
 	public static boolean isDescendant(String targetPath) {
 
 		String[] pathComponent = targetPath.split("(\\\\|/)");
 		if (pathComponent == null)
 			return false;
 
 		int level = 0;
 		for (String each : pathComponent) {
 			if ("..".equals(each)) {
 				level--;
 			}
 			else if (!".".equals(each)) {
 				level++;
 			}
 			if (level < 0)
 				return false;
 		}
 
 		return level > 0;
 	}
 
 	@Override
 	public void handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
 		// validating input
 		if (start == -1 || end == -1 || chr == null)
 			return;
 
 		String suffix = getActionSuffix(request);
 		_logger.info("suffix: " + suffix);
 		if ("png".equals(suffix)) {
 			response.setContentType("image/png");
 			ReadQueryConfig config = new ReadQueryConfig(width, useCanvas, layout);
 			config.maxmumNumberOfReadsToDisplay = Integer.MAX_VALUE;
 			List<OnGenome> result = overlapQuery(new GenomeDB(path, ref), new ChrLoc(chr, start, end), config);
 
 			ReadCanvas canvas = new ReadCanvas(width, 100, new GenomeWindow(start, end));
 			canvas.draw(result);
 			canvas.toPNG(response.getOutputStream());
 		}
 		else {
 			List<OnGenome> result = overlapQuery(new GenomeDB(path, ref), new ChrLoc(chr, start, end), new ReadQueryConfig(width, useCanvas, layout));
 
 			response.setContentType("text/html");
 
 			// output the result in Silk format
 			SilkWriter w = new SilkWriter(response.getWriter());
 			w.preamble();
 			for (OnGenome each : result) {
 				w.leafObject("entry", each);
 			}
 			w.endDocument();
 		}
 	}
 
 	public static List<OnGenome> overlapQuery(GenomeDB db, ChrLoc loc, ReadQueryConfig config) {
 
 		if (!isDescendant(db.path)) {
 			_logger.error("relative path must be used in the path parameter: " + db.path);
 			return new ArrayList<OnGenome>(0);
 		}
 		return overlapQuery(new File(WebTrackBase.getProjectRootPath()), db, loc, config);
 	}
 
 	public static List<OnGenome> overlapQuery(File baseDir, GenomeDB db, ChrLoc loc, ReadQueryConfig config) {
 
 		StopWatch sw = new StopWatch();
 		loc = loc.getLocForPositiveStrand();
 		List<OnGenome> result = new ArrayList<OnGenome>();
 		try {
 
 			DBType dbType = GenomeDB.resolveDBType(db.path);
 			if (dbType == null)
 				throw new UTGBException(UTGBErrorCode.UnknownDBType, "auto detection of DBType failed : " + db.path);
 
 			switch (dbType) {
 			case BAM: {
 				File bamFile = new File(db.path);
 				if (config.wigPath != null) {
 					config.wigPath = new File(baseDir, config.wigPath).getAbsolutePath();
					File wigDB = new File(config.wigPath.trim());
					if (!(wigDB.exists() && wigDB.isFile())) {
 						_logger.warn(String.format("wig database file %s is not found", config.wigPath));
 						config.wigPath = null;
 					}
 				}

 				if (config.layout == Layout.COVERAGE)
 					return SAMReader.depthCoverage(bamFile, loc, config.pixelWidth, config);
 				else
 					return SAMReader.overlapQuery(bamFile, loc, config.pixelWidth, config);
 			}
 			case BED: {
 				result = BEDDatabase.overlapQuery(new File(db.path), loc);
 				break;
 			}
 			case DAS: {
 				String dasType = null;
 				if (db instanceof DASLocation) {
 					dasType = ((DASLocation) db).dasType;
 				}
 				DASResult queryDAS = DASViewer.queryDAS(db.path, dasType, loc);
 				if (queryDAS != null) {
 					for (DASFeature each : queryDAS.segment.feature) {
 						result.add(each);
 					}
 				}
 				break;
 			}
 			default:
 				throw new UTGBException(UTGBErrorCode.INVALID_INPUT, "unknown db type: " + dbType);
 			}
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		_logger.debug("query done. " + sw.getElapsedTime() + " sec.");
 
 		if (config.layout == Layout.COVERAGE || result.size() > config.maxmumNumberOfReadsToDisplay) {
 			// compute coverage
 			ReadCoverage coverage = computeCoverage(result, loc.start, loc.end, config.pixelWidth);
 			result.clear();
 			result.add(coverage);
 		}
 
 		return result;
 	}
 
 	public static ReadCoverage computeCoverage(List<OnGenome> readList, int start, int end, int pixelWidth) {
 
 		int[] coverage = new int[pixelWidth];
 		for (int i = 0; i < coverage.length; ++i)
 			coverage[i] = 0;
 
 		GenomeWindow w = new GenomeWindow(start, end);
 
 		//  ------      ------
 		//    --------  ---
 		//      ----    --
 		// 0112233332200332111000 (coverage)
 		for (OnGenome eachRead : readList) {
 			int bucketStart = w.getXPosOnWindow(eachRead.getStart(), pixelWidth);
 			int bucketEnd = w.getXPosOnWindow(eachRead.getEnd(), pixelWidth);
 			if (bucketStart < 0)
 				bucketStart = 0;
 
 			if (bucketEnd - bucketStart <= 0)
 				bucketEnd = bucketStart + 1;
 
 			if (bucketEnd >= pixelWidth)
 				bucketEnd = pixelWidth - 1;
 
 			for (int i = bucketStart; i < bucketEnd; ++i)
 				coverage[i]++;
 		}
 
 		return new ReadCoverage(start, end, pixelWidth, coverage);
 	}
 
 	private static class OnGenomeDataRetriever<T extends OnGenome> extends ObjectHandlerBase<T> {
 		private ArrayList<OnGenome> geneList = new ArrayList<OnGenome>();
 
 		public OnGenomeDataRetriever() {
 		}
 
 		public ArrayList<OnGenome> getResult() {
 			return geneList;
 		}
 
 		public void handle(T bean) throws Exception {
 			geneList.add(bean);
 		}
 
 		public void handleException(Exception e) throws Exception {
 			_logger.error(e);
 		}
 	}
 
 	private static BufferedReader openURL(String url, ServletContext context) throws IOException {
 		BufferedReader in;
 		if (!url.startsWith("http://")) {
 			if (!url.startsWith("/"))
 				url = "/" + url;
 			_logger.debug("proxy request: " + url);
 			in = WebApplicationResource.openResource(context, url);
 		}
 		else {
 			URL address = new URL(url);
 			_logger.debug("proxy request: " + url);
 			in = new BufferedReader(new InputStreamReader(address.openStream()));
 		}
 		return in;
 	}
 
 }
