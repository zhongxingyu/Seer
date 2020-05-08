 package org.kaoriha.flowerflower.compile;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.arnx.jsonic.JSON;
 
 import org.joda.time.DateTime;
 import org.joda.time.format.ISODateTimeFormat;
 import org.kaoriha.flowerflower.compile.document.Depot;
 import org.kaoriha.flowerflower.compile.document.DocumentHandler;
 import org.kaoriha.flowerflower.compile.document.Fragment;
 import org.kaoriha.flowerflower.compile.document.IndexEntry;
 
 import com.googlecode.jatl.Html;
 import com.ibm.icu.text.MessageFormat;
 
 public class Builder {
 	private final DocumentHandler doc;
 	private final TimeTable timeTable;
 	private final String lastReleasedSeparationId;
 	private final File outputDir;
 	private List<String> separationIdList;
 
 	public Builder(DocumentHandler doc, TimeTable timeTable, String lastReleasedSeparationId, File outputDir) {
 		this.doc = doc;
 		this.timeTable = timeTable;
 		this.lastReleasedSeparationId = lastReleasedSeparationId;
 		this.outputDir = outputDir;
 	}
 
 	public void build() throws IOException {
 		separationIdList = new ArrayList<String>();
 		for (Map.Entry<DateTime, String> e : timeTable.getMap().entrySet()) {
 			separationIdList.add(e.getValue());
 		}
 		boolean isAfter = (lastReleasedSeparationId == null);
 		for (int i = 0; i < separationIdList.size(); i++) {
 			if (isAfter) {
 				buildEach(separationIdList.get(i));
 			}
 			if (separationIdList.get(i).equals(lastReleasedSeparationId)) {
 				isAfter = true;
 			}
 		}
 	}
 
 	private void buildEach(String sid) throws IOException {
 		String dirName = timeTable.getDateTime(sid).toString(Constant.DATE_DIR_NAME_FORMAT);
 		File dir = new File(outputDir, dirName);
 		if (!dir.mkdir()) {
 			throw new IllegalArgumentException("outputDir is not clean.");
 		}
 		File authDir = new File(dir, Constant.AUTH_DIR_NAME);
 		if (!authDir.mkdir()) {
 			throw new IllegalArgumentException("outputDir/authDir is not clean.");
 		}
 		
 		buildEachHtml(authDir, sid);
 		buildEachDiffJson(authDir, sid);
 
 		File pubDir = new File(dir, Constant.PUBLIC_DIR_NAME);
 		if (!pubDir.mkdir()) {
 			throw new IllegalArgumentException("outputDir/pubDir is not clean.");
 		}
 		
 		buildLatestSeparationHtml(pubDir, sid);
 	}
 
 	private void buildLatestSeparationHtml(File dir, String sid) throws FileNotFoundException, IOException {
 		Depot depot = doc.getChronicle().getDepot(sid);
 		String indexStr = "<li><a href=\"" + Constant.LATEST_SEPARATION_HTML_FILENAME + "\">" + Constant.LATEST_SEPARATION_NAME + "</a></li>" + buildIndex(depot);
 
 		{
 			IndexEntry ie = new IndexEntry();
 			ie.setName(Constant.CHARACTER_NOTE_NAME);
 			ie.setStartFragment(depot.fromKey(Constant.CHARACTER_NOTE_INITIAL_KEY));
 			buildChapter(dir, ie, indexStr);
 		}
 
 		for (IndexEntry ie : depot.getIndexEntryList()) {
 			for (Fragment f = ie.getStartFragment(); f != null; f = f.getNext()) {
 				if (f.getKey().equals(sid)) {
 					FileOutputStream fos = null;
 					try {
 						MessageFormat mf = new MessageFormat(Constant.LatestSeparationHtml.FORMAT);
 						Map<String, String> vm = new HashMap<String, String>();
 						vm.put(Constant.LatestSeparationHtml.TITLE, ie.getName());
 
 						DateTime dt = timeTable.getDateTime(sid);
 						vm.put(Constant.LatestSeparationHtml.yyyy, Integer.toString(dt.getYear()));
 						vm.put(Constant.LatestSeparationHtml.MM, Integer.toString(dt.getMonthOfYear()));
 						vm.put(Constant.LatestSeparationHtml.dd, Integer.toString(dt.getDayOfMonth()));
 
 						vm.put(Constant.LatestSeparationHtml.INDEX, indexStr);
 
 						vm.put(Constant.LatestSeparationHtml.CONTENT, "<div class=\"separation\">" + f.getHtml() + "</div>");
						vm.put(Constant.LatestSeparationHtml.START_SID, "latest.html");
 
 						File file = new File(dir, Constant.LATEST_SEPARATION_HTML_FILENAME);
 						fos = new FileOutputStream(file, false);
 						OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
 						osw.write(mf.format(vm));
 						osw.close();
 					} finally {
 						if (fos != null) {
 							fos.close();
 						}
 					}
 
 					return;
 				}
 			}
 		}
 	}
 
 	private String buildIndex(Depot depot) {
 		StringWriter sw = new StringWriter();
 		Html index = new Html(sw);
 		for (IndexEntry ie : depot.getIndexEntryList()) {
 			index = index.li().a().href(ie.getStartFragment().getKey() + ".html").text(ie.getName()).end().end();
 		}
 		index = index.li().a().href(Constant.CHARACTER_NOTE_INITIAL_KEY + ".html").text(Constant.CHARACTER_NOTE_NAME).end().end();
 		return sw.getBuffer().toString();
 	}
 
 	private void buildEachHtml(File dir, String sid) throws FileNotFoundException, IOException {
 		Depot depot = doc.getChronicle().getDepot(sid);
 
 		String indexStr = buildIndex(depot);
 		for (IndexEntry ie : depot.getIndexEntryList()) {
 			buildChapter(dir, ie, indexStr);
 		}
 		
 		IndexEntry ie = new IndexEntry();
 		ie.setName(Constant.CHARACTER_NOTE_NAME);
 		ie.setStartFragment(depot.fromKey(Constant.CHARACTER_NOTE_INITIAL_KEY));
 		buildChapter(dir, ie, indexStr);
 	}
 
 	private void buildChapter(File dir, IndexEntry ie, String indexStr) throws IOException, FileNotFoundException {
 		FileOutputStream fos = null;
 		try {
 			MessageFormat mf = new MessageFormat(Constant.Html.FORMAT);
 			Map<String, String> vm = new HashMap<String, String>();
 			vm.put(Constant.Html.TITLE, ie.getName());
 			vm.put(Constant.Html.INDEX, indexStr);
			vm.put(Constant.Html.START_SID, ie.getStartFragment().getKey());
 
 			StringBuilder sb = new StringBuilder();
 			for (Fragment f = ie.getStartFragment(); f != null; f = f.getNext()) {
 				if (f.getHtml().length() == 0) {
 					continue;
 				}
 				sb.append("<div class=\"separation\" id=\"");
 				sb.append(f.getKey());
 				sb.append("\">");
 				sb.append(f.getHtml());
 				sb.append("</div>");
 			}
 			vm.put(Constant.Html.CONTENT, sb.toString());
 
 			File f = new File(dir, ie.getStartFragment().getKey() + ".html");
 			fos = new FileOutputStream(f, false);
 			OutputStreamWriter osw = new OutputStreamWriter(fos, "UTF-8");
 			osw.write(mf.format(vm));
 			osw.close();
 		} finally {
 			if (fos != null) {
 				fos.close();
 			}
 		}
 	}
 
 	private void buildEachDiffJson(File dir, String sid) throws IOException {
 		String distSid = sid;
 		Depot origDepot;
 		int distSidIndex = separationIdList.indexOf(distSid);
 		if (distSidIndex == 0) {
 			origDepot = new Depot();
 		} else {
 			origDepot = doc.getChronicle().getDepot(separationIdList.get(distSidIndex - 1));
 		}
 		Depot distDepot = doc.getChronicle().getDepot(distSid);
 
 		File df = new File(dir, distSid + ".json");
 		FileOutputStream fos = new FileOutputStream(df);
 		JSON.encode(distDepot.diff(origDepot), fos);
 		fos.close();
 
 		if ((distSidIndex % 10) == 9) {
 			String farOrigSid = separationIdList.get(distSidIndex - 9);
 			Depot farOrigDepot;
 			if (distSidIndex - 9 == 0) {
 				farOrigDepot = new Depot();
 			} else {
 				farOrigDepot = doc.getChronicle().getDepot(separationIdList.get(distSidIndex - 10));
 			}
 			File ef = new File(dir, farOrigSid + distSid + ".json");
 			fos = new FileOutputStream(ef);
 			JSON.encode(distDepot.diff(farOrigDepot), fos);
 			fos.close();
 		}
 
 		Map<String, Object> cm = new HashMap<String, Object>();
 		if (distSidIndex + 1 < separationIdList.size()) {
 			String nextSid = separationIdList.get(distSidIndex + 1);
 			cm.put(Constant.CATALOGUE_NEXT_RELEASE_SCHEDULE_KEY, timeTable.getDateTime(nextSid).toString(ISODateTimeFormat.dateTimeNoMillis()));
 		}
 		List<String> localList = new ArrayList<String>();
 		Map<String, String> expressMap = new HashMap<String, String>();
 		for (int i = 0; i <= distSidIndex; i++) {
 			localList.add(separationIdList.get(i));
 			if ((i % 10) == 9) {
 				expressMap.put(separationIdList.get(i - 9), separationIdList.get(i));
 			}
 		}
 		cm.put(Constant.CATALOGUE_LOCAL_KEY, localList);
 		cm.put(Constant.CATALOGUE_EXPRESS_KEY, expressMap);
 		String pm = doc.getPushMessageMap().get(separationIdList.get(distSidIndex));
 		if (pm != null) {
 			cm.put(Constant.CATALOGUE_PUSH_MESSAGE, pm);
 		}
 
 		File catalogue = new File(dir, Constant.CATALOGUE_FILENAME);
 		fos = new FileOutputStream(catalogue);
 		JSON.encode(cm, fos);
 		fos.close();
 	}
 }
