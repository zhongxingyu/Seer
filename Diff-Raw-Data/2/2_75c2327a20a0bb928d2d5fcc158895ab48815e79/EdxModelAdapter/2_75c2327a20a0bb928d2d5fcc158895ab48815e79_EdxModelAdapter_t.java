 package classviewer.changes;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.security.KeyManagementException;
 import java.security.NoSuchAlgorithmException;
 import java.security.cert.X509Certificate;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 
 import javax.net.ssl.HttpsURLConnection;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLSocketFactory;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.X509TrustManager;
 
 import classviewer.model.CourseModel;
 import classviewer.model.CourseRec;
 import classviewer.model.OffRec;
 import classviewer.model.Status;
 
 /**
  * EdX html is weird. I could not find an off-the-shelf parser that would
  * convert it into DOM, so here is a hackish solution.
  * 
  * Only doing additions and modifications for universities and classes.
  * 
  * @author TK
  */
 public class EdxModelAdapter {
 
 	/** Class records bundled by class id */
 	private HashMap<String, ArrayList<EdxRecord>> records = new HashMap<String, ArrayList<EdxRecord>>();
 	private static SSLSocketFactory sslSocketFactory;
 
 	static {
 		// We will use this to deal with PKIX cert exceptions
 		makeAllTrustingManager();
 	}
 	
 	/** Read everything into a string buffer */
 	private StringBuffer readIntoBuffer(Reader reader) throws IOException {
 		StringBuffer b = new StringBuffer();
 		BufferedReader br = new BufferedReader(reader);
 		String s = br.readLine();
 		while (s != null) {
 			b.append(s + "\n"); // keep /n there for easier debugging
 			s = br.readLine();
 		}
 		return b;
 	}
 
 	/**
 	 * Find next article from the given offset. An article should have XML tag
 	 * "article" and "class" attribute "course". Return -1 if not found.
 	 */
 	private int findNextArticle(StringBuffer all, int offset)
 			throws IOException {
 		// Until we find something or run out of file
 //		while (offset < all.length()) {
 			int off = all.indexOf("course-tile", offset);
 			return off;
 //			if (off < 0)
 //				return -1;
 //			// Closing > after this
 //			int close = all.indexOf("</article>", off);
 //			if (close < 0)
 //				throw new IOException("Article tag at " + off
 //						+ " does not close. Broken file?");
 //			// Now look for class before the closing. Assume no spaces between
 //			// the attribute name and the value
 //			int cl = all.indexOf("<h1><span>", off);
 //			if (cl > 0 && cl < close)
 //				return off;
 //			// Otherwise move past close
 //			offset = close + 1;
 //		}
 //		// Ran out of file
 //		return -1;
 	}
 
 	/**
 	 * Extract course info from the chunk of the buffer between start and end.
 	 * 
 	 * @return
 	 */
 	private EdxRecord parseCourse(String all, String edxBase)
 			throws IOException {
 		// final String toID = "<article id=\"";
 		final String toUrl = "<a href=\"";
 		final String toNew = "<div class=\"new-course-ribbon\">";
 		final String toNumber = "<h2 class=\"title course-title\">";
 		final String toDesc = "course-subtitle copy-detail\">";
 		final String toDate = "Starts:</span>";
 		final String toUni = "<li><strong>";
 
 		int idx, end;
 
 		boolean isNew = all.indexOf(toNew) > 0;
 
 		idx = all.indexOf(toUrl);
 		if (idx < 0)
 			throw new IOException("No URL for course " + all);
 		end = all.indexOf("\"", idx + toUrl.length());
 		String home = all.substring(idx + toUrl.length(), end);
 
 		idx = all.indexOf(toNumber);
 		if (idx < 0)
 			throw new IOException("No number for course " + all);
 		end = all.indexOf("<", idx + toNumber.length());
 		if (end < 0)
 			throw new IOException("Tag at " + idx + " does not close " + all);
 		String courseId = all.substring(idx + toNumber.length(), end).trim();
 		if (courseId.endsWith(":"))
 			courseId = courseId.substring(0, courseId.length()-1);
 		// assuming it's </strong><a ...
 		idx = all.indexOf(">", end + 15);
 		end = all.indexOf("<", idx);
 		if (end < 0)
 			throw new IOException("No title end at " + idx + " in " + all);
 		String name = cleanStr(all.substring(idx + 1, end).trim());
 
 		idx = all.indexOf(toDesc);
 		if (idx < 0)
 			throw new IOException("No description for course " + all);
 //		idx = all.indexOf("<p>", idx);
 //		if (idx < 0)
 //			throw new IOException("No <p> in description for course " + all);
 		end = all.indexOf("<", idx+2);
 		if (end < 0)
 			throw new IOException("Tag at " + idx + " does not close " + all);
 		String descr = cleanStr(all.substring(idx + toDesc.length(), end).trim());
 
 		idx = all.indexOf(toDate);
 		if (idx < 0)
 			throw new IOException("No date for course " + all);
 		end = all.indexOf("</", idx + toDate.length() + 1);
 		if (end < 0)
 			throw new IOException("Tag at " + idx + " does not close " + all);
 		String dateStr = all.substring(idx + toDate.length(), end).trim();
 
 		idx = all.indexOf(toUni);
 		if (idx < 0)
 			throw new IOException("No university for course " + all);
 		end = all.indexOf("</", idx);
 		if (end < 0)
 			throw new IOException("Tag at " + idx + " does not close " + all);
 		String univer = all.substring(idx + toUni.length(), end).trim();
 		// Drop spaces in the university name. Otherwise we have problems
 		// forming space-separated attribute string. Also, get rid of
 		// "University of "
 		univer = univer.replace("University of ", "");
 		univer = univer.replace(" ", "");
 
 		// System.out.println(courseId + ", " + name + "\n\t" + descr + "\n\t"
 		// + univer + ", " + date + ", " + isNew);
 
 		Date start = EdxRecord.parseDate(dateStr);
 		int duration = 1;
 		// TODO they seem to have dropped end date
 //		if (home != null && start != null) {
 //			String endStr = extractEndDate(edxBase, home);
 //			Date endDate = EdxRecord.parseDate(endStr);
 //			if (endDate != null) {
 //				long mills = endDate.getTime() - start.getTime();
 //				duration = Math.round(mills / (1000 * 3600 * 24 * 7.0f));
 //			}
 //		}
 
 		return new EdxRecord(courseId, name, descr, univer, start, duration,
 				home, isNew);
 	}
 	private String cleanStr(String str) {
 		str = str.replace("&amp;", "&");
 		return str;
 	}
 
 /*
 	<article class="course-tile"><div class="left-col">
 	<div class="new-course-ribbon"></div><div class="top"><div class="title">
 	<h1><span>24.00x:</span> Introduction to Philosophy: God, Knowledge and Consciousness</h1>
 	</div>
 	<div class="subtitle">This course will focus on big questions. You will learn how to ask them and how to answer them. 
 		<a class="go-to-course" href="/course/mit/24-00x/introduction-philosophy-god/888">more</a>
 	</div></div><div class="bottom">
 	<div class="detail"><ul class="clearfix"><li>
 	<span class="bold-title">Starts:</span> <span class="date-display-single">1 Oct 2013</span>
 	</li><li> • </li><li><div class="instructor-list">
 	<span class="bold-title">Instructors:</span> Caspar Hare</div></li>
 	<li> • </li><li class="school-list">MITx</li></ul></div></div></div>
 	<div class="right-col">
 	<div class="image">
 	<img src="https://www.edx.org/sites/default/files/styles/course_tile_image/public/2400x_262x136.jpg?itok=y3n29SDS" width="277" height="136" alt="Introduction to Philosophy: God, Knowledge and Consciousness" />
 	</div><div class="actions clearfix">
 	<div class="action"><div class="iframe iframe-register action-register-course">
 	<iframe src="https://courses.edx.org/mktg/MITx/24.00x/2013_SOND"></iframe>
 	</div></div></div></div><div class="clearfix"></div>
 	<div style="hidden" class="course-link" href="/course/mit/24-00x/introduction-philosophy-god/888"></div>
 	</article>  
 	</div><div class="views-row views-row-2 views-row-even">
 	<!-- This is the template for every row in Courses Page -->
 */
 	
 	/** Assuming everything is in a string buffer, pick out classes */
 	private HashMap<String, ArrayList<EdxRecord>> readHtml(StringBuffer all,
 			String baseUrl) throws IOException {
 		int offset = 0;
 		final int END = all.length();
 		while (offset < END) {
 			// Start of the next course description, if any
 			int start = findNextArticle(all, offset);
 			if (start < 0)
 				break;
 			// End of this article, will miss the last big blue button
 			int end = all.indexOf("<div class=\"col-both-courses\">", offset);
 			if (end < 0) {
 				System.err.println("Article at " + offset + " is not closed");
 				end = END;
 			}
 			// Parse this particular course info
 			EdxRecord rec = parseCourse(all.substring(start, end), baseUrl);
 			ArrayList<EdxRecord> list = records.get(rec.getCourseId());			
 			if (list == null) {
 				list = new ArrayList<EdxRecord>();
 				records.put(rec.getCourseId(), list);
 			}
 			list.add(rec);
 			// Move past the end
 			offset = end + 10;
 		}
 		return records;
 	}
 
 	// Borrowed from https://code.google.com/p/misc-utils/wiki/JavaHttpsUrl
 	private static void makeAllTrustingManager() {
 		// Create a trust manager that does not validate certificate chains
 		final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
 			@Override
 			public void checkClientTrusted(final X509Certificate[] chain,
 					final String authType) {
 			}
 
 			@Override
 			public void checkServerTrusted(final X509Certificate[] chain,
 					final String authType) {
 			}
 
 			@Override
 			public X509Certificate[] getAcceptedIssuers() {
 				return null;
 			}
 		} };
 
 		// Install the all-trusting trust manager
 		try {
 			SSLContext sslContext = SSLContext.getInstance("SSL");
 			sslContext.init(null, trustAllCerts,
 					new java.security.SecureRandom());
 			// Create an ssl socket factory with our all-trusting manager
 			sslSocketFactory = sslContext.getSocketFactory();
 		} catch (NoSuchAlgorithmException | KeyManagementException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * This is the main read method called from the application. It puts data
 	 * into internal structure
 	 */
 	public void parse(String edxUrl, boolean ignoreSSL) throws IOException {
 		records.clear();
 		int page = 0;
 		while (true) {
 			URL url = new URL(edxUrl
 					+ "/course-list/allschools/allsubjects/allcourses"
 					+ ((page == 0) ? "" : ("?page=" + page)));
 			// All set up, we can get a resource through https now:
 			URLConnection urlCon = url.openConnection();
 			// Tell the url connection object to use our socket factory which
 			// bypasses security checks
 			if (ignoreSSL)
 				((HttpsURLConnection) urlCon)
 						.setSSLSocketFactory(sslSocketFactory);
 			InputStream stream = urlCon.getInputStream();
 			InputStreamReader reader = new InputStreamReader(stream);
 
 			StringBuffer buffer = readIntoBuffer(reader);
 			stream.close();
 			
 			if (buffer.indexOf("courses-no-result-title") > 0)
 				break;
 
 			int before = records.size();
 			readHtml(buffer, edxUrl);
 			if (records.size() == before) {
 				System.out.println("Found no records and no stop sign in page " + page);
 				break;
 			}
 			page++;
 		}
 	}
 
 	/**
 	 * Compare the internal structure produced by the parse method to the given
 	 * model and return the set of differences
 	 */
 	public ArrayList<Change> collectChanges(CourseModel courseModel, int tooOldInDays) {
 		// Build a date before which we do not remove offerings
 		Date tooOld = new Date( new Date().getTime() - 24*3600000l*tooOldInDays );
 		System.out.println("Will keep all offerings older than " + tooOldInDays + " days: " + tooOld);
 		
 		ArrayList<Change> res = new ArrayList<Change>();
 
 		// There are no categories for Edx, but there are universities. For
 		// categories we'll just set EdX, unless manually specified otherwise
 		HashSet<String> uniFromFile = new HashSet<String>();
 		// Just check the first record. Assume the list is not empty
 		for (ArrayList<EdxRecord> lr : records.values())
 			uniFromFile.add(lr.get(0).getUniversity());
 		for (String u : uniFromFile) {
 			if (courseModel.getUniversity(u) == null)
 				res.add(new DescChange(DescChange.UNIVERSITY, Change.ADD,
 						"EdX University", null, makeUniJsonForId(u)));
 		}
 
 		// EdX is not particularly consistent with naming, hence this hack.
 		// Note that this will only work for simplification of ids.
 		ArrayList<String> names = new ArrayList<String>(records.keySet());
 		for (String s : names) {
 			CourseRec oldRec = courseModel.getClassByShortName(s);
 			if (oldRec != null)
 				continue;
 			String s1 = s.replace(" ", "").replace("-", "");
 			oldRec = courseModel.getClassByShortName(s1);
 			if (oldRec != null) {
 				ArrayList<EdxRecord> list = records.remove(s);
 				for (EdxRecord r : list)
 					r.setCourseId(s1);
 				ArrayList<EdxRecord> list1 = records.get(s1);
 				if (list1 == null)
 					records.put(s1, list);
 				else
 					list1.addAll(list);
 			}
 		}
 		
 		// Go over all course bundles, pick those that don't yet exist
 		for (ArrayList<EdxRecord> list : records.values()) {
 			String courseId = list.get(0).getCourseId();
 			CourseRec oldRec = courseModel.getClassByShortName(courseId);
 			if (oldRec == null) {
 				res.add(new EdxCourseChange(Change.ADD, null, null, list,
 						courseModel));
 			} else {
 				diffCourse(list, oldRec, res, courseModel, tooOld);
 			}
 		}
 		return res;
 	}
 
 	private void diffCourse(ArrayList<EdxRecord> list, CourseRec oldRec,
 			ArrayList<Change> res, CourseModel model, Date tooOld) {
 		// The only things we have here are: name, description, offerings
 		String s1 = list.get(0).getName();
 		String s2 = oldRec.getName();
 		if (s1 == null && s2 != null || s1 != null && !s1.equals(s2))
 			res.add(new EdxCourseChange(Change.MODIFY, "Name", oldRec, list,
 					model));
 		s1 = list.get(0).getDescription();
 		s2 = oldRec.getDescription();
 		if (s1 == null && s2 != null || s1 != null && !s1.equals(s2))
 			res.add(new EdxCourseChange(Change.MODIFY, "Description", oldRec,
 					list, model));
 
 		// Compare offerings by date
 		ArrayList<Date> existing = new ArrayList<Date>();
 		ArrayList<Date> incoming = new ArrayList<Date>();
 		for (EdxRecord r : list)
 			if (r.getStart() != null)
 				incoming.add(r.getStart());
 			else
 				System.err.println("New EdX offering without start date: " + r);
 		for (OffRec r : oldRec.getOfferings())
 			if (r.getStart() != null)
 				existing.add(r.getStart());
 			else
 				System.err.println("Existing EdX offering without start date: "
 						+ r);
 
 		// Differences
 		ArrayList<Date> deleted = new ArrayList<Date>(existing);
 		deleted.removeAll(incoming);
 		ArrayList<Date> added = new ArrayList<Date>(incoming);
 		added.removeAll(existing);
 		
 		// Remove deletes that are too old
 		for (Iterator<Date> it = deleted.iterator(); it.hasNext(); ) {
 			Date d = it.next();
 			if (d.before(tooOld))
 				it.remove();
 		}
 		
 		// Possible date shift?
 		if (deleted.size() == 1 && added.size() == 1) {
 			OffRec rr = null;
 			for (OffRec r : oldRec.getOfferings())
				if (r.getStart() != null && r.getStart().equals(deleted.get(0))) {
 					rr = r;
 					break;
 				}
 			EdxRecord er = null;
 			for (EdxRecord r : list)
 				if (r.getStart().equals(added.get(0))) {
 					er  = r;
 					break;
 				}
 			// EdxOfferingChange(String type, CourseRec course, String field, OffRec offering, EdxRecord record
 			res.add(new EdxOfferingChange(Change.MODIFY, oldRec, "Start", rr, er));
 			deleted.clear();
 			added.clear();
 		}
 
 		// Prune deleted things that are actually done: never delete those records
 		for (OffRec r : oldRec.getOfferings())
 			if (deleted.contains(r.getStart())
 					&& (Status.DONE.equals(r.getStatus()) || Status.REGISTERED
 							.equals(r.getStatus()))) {
 				deleted.remove(r.getStart());
 			}		
 		
 		// Deleted
 		for (OffRec r : oldRec.getOfferings())
 			if (deleted.contains(r.getStart()))
 				res.add(new EdxOfferingChange(Change.DELETE, oldRec, null, r,
 						null));
 		// Added
 		for (EdxRecord r : list)
 			if (added.contains(r.getStart()))
 				res.add(new EdxOfferingChange(Change.ADD, oldRec, null, null, r));
 
 		// For the intersection check duration. No need to check the start date,
 		// since it's the key. TODO They no longer have duration
 //		diff = new ArrayList<Date>(existing);
 //		diff.retainAll(incoming);
 //		for (EdxRecord r : list)
 //			if (diff.contains(r.getStart())) {
 //				// Locate corresponding existing
 //				OffRec r1 = null;
 //				for (OffRec r2 : oldRec.getOfferings())
 //					if (r2.getStart().equals(r.getStart()))
 //						r1 = r2;
 //				assert (r1 != null);
 //
 //				if (r1.getDuration() != r.getDuration()) {
 //					res.add(new EdxOfferingChange(Change.MODIFY, oldRec,
 //							"Duration", r1, r));
 //				}
 //			}
 	}
 
 	private HashMap<String, Object> makeUniJsonForId(String u) {
 		HashMap<String, Object> res = new HashMap<String, Object>();
 		res.put("name", u);
 		res.put("short_name", u);
 		res.put("description", u + " on EdX");
 		return res;
 	}
 
 	public String extractEndDate(String baseUrl, String home) {
 		final String tag = "<span class=\"final-date\">";
 
 		String addr = baseUrl + home;
 		try {
 			URL url = new URL(addr);
 			InputStream stream = url.openStream();
 			InputStreamReader reader = new InputStreamReader(stream);
 
 			StringBuffer buffer = readIntoBuffer(reader);
 			stream.close();
 
 			int idx = buffer.indexOf(tag);
 			if (idx < 0)
 				return null;
 			int end = buffer.indexOf("</span>", idx);
 			return buffer.substring(idx + tag.length(), end).trim();
 		} catch (Exception e) {
 			System.err.println("Cannot get end date from " + addr);
 		}
 		return null;
 	}
 
 	public boolean loadClassDuration(String baseURL, OffRec off,
 			boolean ignoreSSL) throws IOException {
 		String tail = off.getLink();
 		if (tail == null)
 			return false;
 		URL url = new URL(baseURL + tail);
 		// All set up, we can get a resource through https now:
 		URLConnection urlCon = url.openConnection();
 		// Tell the url connection object to use our socket factory which
 		// bypasses security checks
 		if (ignoreSSL)
 			((HttpsURLConnection) urlCon).setSSLSocketFactory(sslSocketFactory);
 		InputStream stream = urlCon.getInputStream();
 		InputStreamReader reader = new InputStreamReader(stream);
 
 		StringBuffer buffer = readIntoBuffer(reader);
 		stream.close();
 
 		// We are looking for the number of weeks. This code is specific to the
 		// EdX format
 		final String startLabel = "Course Length:";
 		int start = buffer.indexOf(startLabel);
 		if (start < 0) {
 			return false;
 		}
 		start += startLabel.length();
 		int end = buffer.indexOf(" week", start);
 		if (end < 0) {
 			return false;
 		}
 
 		// This slice should end with the number we are looking for
 		String slice = buffer.substring(start, end).trim();
 		for (start = slice.length() - 1; start >= 0
 				&& Character.isDigit(slice.charAt(start)); start--)
 			;
 		int weeks = Integer.parseInt(slice.substring(start + 1));
 
 		off.setDuration(weeks);
 		return true;
 	}
 }
