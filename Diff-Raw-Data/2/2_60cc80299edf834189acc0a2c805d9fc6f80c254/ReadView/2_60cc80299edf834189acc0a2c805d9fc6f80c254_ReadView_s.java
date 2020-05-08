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
 import org.utgenome.gwt.utgb.client.bio.ChrLoc;
 import org.utgenome.gwt.utgb.client.bio.DASLocation;
 import org.utgenome.gwt.utgb.client.bio.DASResult;
 import org.utgenome.gwt.utgb.client.bio.GenomeDB;
 import org.utgenome.gwt.utgb.client.bio.OnGenome;
 import org.utgenome.gwt.utgb.client.bio.Read;
 import org.utgenome.gwt.utgb.client.bio.ReadCoverage;
 import org.utgenome.gwt.utgb.client.bio.ReadQueryConfig;
 import org.utgenome.gwt.utgb.client.bio.DASResult.DASFeature;
 import org.utgenome.gwt.utgb.client.bio.GenomeDB.DBType;
 import org.utgenome.gwt.utgb.client.bio.Read.ReadType;
 import org.utgenome.gwt.utgb.client.bio.ReadQueryConfig.Layout;
 import org.utgenome.gwt.utgb.server.WebTrackBase;
 import org.utgenome.gwt.utgb.server.util.WebApplicationResource;
 import org.xerial.ObjectHandlerBase;
 import org.xerial.silk.SilkWriter;
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
 	public Read.ReadType type = ReadType.SAM;
 
 	@Override
 	public void handle(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
 		// validating input
 		if (start == -1 || end == -1 || chr == null)
 			return;
 
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
 
 	public static List<OnGenome> overlapQuery(GenomeDB db, ChrLoc loc, ReadQueryConfig config) {
 
 		List<OnGenome> result = new ArrayList<OnGenome>();
 		StopWatch sw = new StopWatch();
 		DBType dbType = db.resolveDBType();
 		loc = loc.getLocForPositiveStrand();
 
 		try {
 			if (dbType == null)
 				throw new UTGBException(UTGBErrorCode.UnknownDBType, "auto detection of DBType failed : " + db);
 
 			switch (dbType) {
 			case BAM: {
 				File bamFile = new File(WebTrackBase.getProjectRootPath(), db.path);
 				result = SAMReader.overlapQuery(bamFile, loc);
 			}
 				break;
 			case BED: {
 				result = BEDDatabase.overlapQuery(new File(getProjectRootPath(), db.path), loc);
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
 				//			case URI: {
 				//				String dataSourceFullURI = db.path;
 				//				dataSourceFullURI.replace("%start", Integer.toString(loc.start));
 				//				dataSourceFullURI.replace("%end", Integer.toString(loc.end));
 				//				dataSourceFullURI.replace("%chr", loc.chr);
 				//				dataSourceFullURI.replace("%ref", db.ref);
 				//
 				//				BufferedReader reader = openURL(dataSourceFullURI, context);
 				//				OnGenomeDataRetriever<Gene> geneRetriever = new OnGenomeDataRetriever<Gene>();
 				//				Lens.findFromJSON(reader, "gene", Gene.class, geneRetriever);
 				//				result = geneRetriever.getResult();
 				//				break;
 				//			}
 
 			}
 		}
 		catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		_logger.debug("query done. " + sw.getElapsedTime() + " sec.");
 
		if (config.layout == Layout.COVERAGE || result.size() > 3000) {
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
