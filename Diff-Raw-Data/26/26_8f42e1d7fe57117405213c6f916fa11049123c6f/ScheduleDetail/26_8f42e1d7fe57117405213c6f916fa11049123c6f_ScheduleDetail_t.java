 /*
  * ScheduleDetail.java
  * 
  * Written by Kaiwen Xu (kevin).
  * Released under Apache License 2.0.
  */
 
 package net.kevxu.purdueassist.course;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import net.kevxu.purdueassist.course.elements.Predefined.Subject;
 import net.kevxu.purdueassist.course.elements.Predefined.Term;
 import net.kevxu.purdueassist.course.elements.Predefined.Type;
 import net.kevxu.purdueassist.course.elements.Seats;
 import net.kevxu.purdueassist.course.shared.CourseNotFoundException;
 import net.kevxu.purdueassist.course.shared.HtmlParseException;
 import net.kevxu.purdueassist.course.shared.RequestNotFinishedException;
 import net.kevxu.purdueassist.course.shared.ResultNotMatchException;
 import net.kevxu.purdueassist.course.shared.Utilities;
 import net.kevxu.purdueassist.shared.httpclient.BasicHttpClientAsync;
 import net.kevxu.purdueassist.shared.httpclient.BasicHttpClientAsync.HttpRequestListener;
 import net.kevxu.purdueassist.shared.httpclient.HttpClientAsync.HttpMethod;
 import net.kevxu.purdueassist.shared.httpclient.MethodNotPostException;
 
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.http.Header;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.message.BasicNameValuePair;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 /**
  * This is the class implementing "Schedule Detail" search described in the
  * document. It utilizes asynchronous function call for non-blocking calling
  * style. You have to provide callback method by implementing
  * ScheduleDetailListener.
  * <p>
  * Input: crn <br />
  * Input (optional): term
  * <p>
  * Output: <br />
  * name crn subject cnbr section term levels campus type credits seats
  * waitlistSeats restrictions prerequisites generalRequirements corequisites;
  * 
  * @author Kaiwen Xu (kevin)
  * @see ScheduleDetailListener
  */
 public class ScheduleDetail implements HttpRequestListener {
 
 	private static final String URL_HEAD = "https://selfservice.mypurdue.purdue.edu/prod/"
 			+ "bzwsrch.p_schedule_detail";
 
 	private Term term;
 	private int crn;
 
 	private ScheduleDetailListener mListener;
 	private BasicHttpClientAsync mHttpClient;
 
 	private boolean requestFinished;
 
 	/**
 	 * Callback methods you have to implement. Provide either
 	 * ScheduleDetailEntry object or exceptions.
 	 * 
 	 * @author Kaiwen Xu (kevin)
 	 */
 	public interface ScheduleDetailListener {
 		public void onScheduleDetailFinished(ScheduleDetailEntry entry);
 
 		public void onScheduleDetailFinished(IOException e);
 
 		public void onScheduleDetailFinished(HtmlParseException e);
 
 		public void onScheduleDetailFinished(CourseNotFoundException e,
 				Term term, int crn);
 
 		public void onScheduleDetailFinished(Exception e);
 	}
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param onScheduleDetailFinishedListener
 	 *            callback you have to implement.
 	 */
 	public ScheduleDetail(ScheduleDetailListener scheduleDetailListener) {
 		this.mListener = scheduleDetailListener;
 		this.requestFinished = true;
 	}
 
 	/**
 	 * Call this method to start retrieving and parsing data.
 	 * 
 	 * @param crn
 	 *            CRN number of course.
 	 * @throws RequestNotFinishedException
 	 *             If calling this method before previous request is finished,
 	 *             then will throw this exception.
 	 */
 	public void getResult(int crn) throws RequestNotFinishedException {
 		getResult(Term.CURRENT, crn);
 	}
 
 	/**
 	 * Call this method to start retrieving and parsing data.
 	 * 
 	 * @param term
 	 *            School term. If it's null, current school term will be used.
 	 * @param crn
 	 *            CRN number of course.
 	 * @throws RequestNotFinishedException
 	 *             If calling this method before previous request is finished,
 	 *             then will throw this exception.
 	 */
 	public void getResult(Term term, int crn)
 			throws RequestNotFinishedException {
 		if (!this.requestFinished)
 			throw new RequestNotFinishedException();
 
 		this.requestFinished = false;
 
 		if (term == null)
 			term = Term.CURRENT;
 
 		this.term = term;
 		this.crn = crn;
 
 		List<NameValuePair> parameters = new ArrayList<NameValuePair>();
 		parameters.add(new BasicNameValuePair("term", term.getLinkName()));
 		parameters.add(new BasicNameValuePair("crn", Integer.toString(crn)));
 
 		mHttpClient = new BasicHttpClientAsync(URL_HEAD, HttpMethod.POST, this);
 		try {
 			mHttpClient.setParameters(parameters);
 			mHttpClient.getResponse();
 		} catch (MethodNotPostException e) {
 			e.printStackTrace();
		} finally {
			this.requestFinished = true;
 		}
 	}
 
 	/**
 	 * Check whether previous request has been finished.
 	 * 
 	 * @return Return true if previous request has already finished.
 	 */
 	public boolean isRequestFinished() {
 		return this.requestFinished;
 	}
 
 	@Override
 	public void onRequestFinished(HttpResponse httpResponse) {
 		try {
 			InputStream stream = httpResponse.getEntity().getContent();
 			Header encoding = httpResponse.getEntity().getContentEncoding();
 			Document document;
 			if (encoding == null) {
 				document = Jsoup.parse(stream, null, URL_HEAD);
 			} else {
 				document = Jsoup.parse(stream, encoding.getValue(), URL_HEAD);
 			}
 			stream.close();
 			ScheduleDetailEntry entry = parseDocument(document);
 			mListener.onScheduleDetailFinished(entry);
 		} catch (IllegalStateException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			mListener.onScheduleDetailFinished(e);
 		} catch (HtmlParseException e) {
 			mListener.onScheduleDetailFinished(e);
 		} catch (CourseNotFoundException e) {
 			mListener.onScheduleDetailFinished(e, term, crn);
 		} catch (ResultNotMatchException e) {
 			mListener.onScheduleDetailFinished(new HtmlParseException(e
 					.getMessage()));
 		} catch (Exception e) {
 			mListener.onScheduleDetailFinished(e);
 		} finally {
 			this.requestFinished = true;
 		}
 	}
 
 	@Override
 	public void onRequestFinished(ClientProtocolException e) {
 		e.printStackTrace();
 		this.requestFinished = true;
 	}
 
 	@Override
 	public void onRequestFinished(IOException e) {
 		mListener.onScheduleDetailFinished(e);
 		this.requestFinished = true;
 	}
 
 	private ScheduleDetailEntry parseDocument(Document document)
 			throws HtmlParseException, CourseNotFoundException,
 			ResultNotMatchException {
 		ScheduleDetailEntry entry = new ScheduleDetailEntry(term, crn);
 		Elements tableElements = document
 				.getElementsByAttributeValue("summary",
 						"This table is used to present the detailed class information.");
 
 		if (!tableElements.isEmpty()) {
 			for (Element tableElement : tableElements) {
 				// get basic info for selected course
 				Element tableBasicInfoElement = tableElement
 						.getElementsByClass("ddlabel").first();
 				if (tableBasicInfoElement != null) {
 					setBasicInfo(entry, tableBasicInfoElement.text());
 				} else {
 					throw new HtmlParseException("Basic info element empty.");
 				}
 
 				// get detailed course info
 				Element tableDetailedInfoElement = tableElement
 						.getElementsByClass("dddefault").first();
 
 				if (tableDetailedInfoElement != null) {
 					// process seat info
 					Elements tableSeatDetailElements = tableDetailedInfoElement
 							.getElementsByAttributeValue("summary",
 									"This layout table is used to present the seating numbers.");
 					if (tableSeatDetailElements.size() == 1) {
 						Element tableSeatDetailElement = tableSeatDetailElements
 								.first();
 						Elements tableSeatDetailEntryElements = tableSeatDetailElement
 								.getElementsByTag("tbody").first().children();
 						if (tableSeatDetailEntryElements.size() == 3
 								|| tableSeatDetailEntryElements.size() == 4) {
 							setSeats(entry, tableSeatDetailEntryElements.get(1)
 									.text());
 							setWaitlistSeats(entry,
 									tableSeatDetailEntryElements.get(2).text());
 							if (tableSeatDetailEntryElements.size() == 4) {
 								setCrosslistSeats(entry,
 										tableSeatDetailEntryElements.get(3)
 												.text());
 							}
 						} else {
 							throw new HtmlParseException(
 									"Seat detail entry elements size not 3. We have "
 											+ tableSeatDetailEntryElements
 													.size() + ".");
 						}
 					} else {
 						throw new HtmlParseException(
 								"Seat detail elements size not 1. We have "
 										+ tableSeatDetailElements.size() + ".");
 					}
 					// remove the seat info from detailed info
 					tableSeatDetailElements.remove();
 
 					// remaining information
 					setRemainingInfo(entry, tableDetailedInfoElement.html());
 
 				} else {
 					throw new HtmlParseException("Detailed info element empty.");
 				}
 
 			}
 		} else {
 			// test empty
 			Elements informationElements = document
 					.getElementsByAttributeValue("summary",
 							"This layout table holds message information");
 			if (!informationElements.isEmpty()
 					&& informationElements.text().contains(
 							"No detailed class information found")) {
 				throw new CourseNotFoundException(informationElements.text());
 			} else {
 				throw new HtmlParseException(
 						"Course table not found, but page does not contain message stating no course found.");
 			}
 		}
 
 		return entry;
 	}
 
 	/**
 	 * Set course name, crn, subject - cnbr and section number based on the
 	 * string passed to this method.
 	 * 
 	 * @param entry
 	 *            ScheduleDetailEntry to be set.
 	 * @param basicInfo
 	 *            String contains course name, crn, subject - cnbr and section
 	 *            number.
 	 * @throws HtmlParseException
 	 * @throws ResultNotMatchException
 	 */
 	private void setBasicInfo(ScheduleDetailEntry entry, String basicInfo)
 			throws HtmlParseException, ResultNotMatchException {
 		String[] basicInfoes = basicInfo.split(" - ");
 		if (basicInfoes.length >= 4) {
 			entry.setCrn(Integer.valueOf(basicInfoes[basicInfoes.length - 3]));
 			if (entry.getCrn() != entry.getSearchCrn())
 				throw new ResultNotMatchException(
 						"Result not match with search option.");
 			entry.setSection(basicInfoes[basicInfoes.length - 1]);
 
 			String[] subjectCnbr = basicInfoes[basicInfoes.length - 2]
 					.split(" ");
 			if (subjectCnbr.length == 2) {
 				entry.setSubject(Subject.valueOf(subjectCnbr[0]));
 				entry.setCnbr(subjectCnbr[1]);
 			} else {
 				throw new HtmlParseException(
 						"Subject and CNBR cannot be split to 2. We have "
 								+ subjectCnbr.length + ".");
 			}
 
 			StringBuffer name = new StringBuffer(basicInfoes[0]);
 			for (int i = 1; i <= basicInfoes.length - 4; i++) {
 				name.append(" - " + basicInfoes[i]);
 			}
 			entry.setName(name.toString());
 		} else {
 			throw new HtmlParseException(
 					"Basic info cannot be split to equal or more than 4. We have "
 							+ basicInfoes.length + ".");
 		}
 	}
 
 	/**
 	 * Set seats information, which contains capacity, actual and remaining.
 	 * 
 	 * @param entry
 	 *            ScheduleDetailEntry to be set.
 	 * @param seatsInfo
 	 *            String contains capacity, actual and remaining.
 	 * @throws HtmlParseException
 	 */
 	private void setSeats(ScheduleDetailEntry entry, String seatsInfo)
 			throws HtmlParseException {
 		String[] seatsInfoes = seatsInfo.split(" ");
 		if (seatsInfoes.length == 4) {
 			entry.setSeats(new Seats(Integer.valueOf(seatsInfoes[1]), Integer
 					.valueOf(seatsInfoes[2]), Integer.valueOf(seatsInfoes[3])));
 		} else {
 			throw new HtmlParseException(
 					"Seats info cannot be split to 4. We have "
 							+ seatsInfoes.length + ".");
 		}
 	}
 
 	/**
 	 * Same as setSeats().
 	 * 
 	 * @param entry
 	 *            ScheduleDetailEntry to be set.
 	 * @param waitlistSeatsInfo
 	 * @throws HtmlParseException
 	 */
 	private void setWaitlistSeats(ScheduleDetailEntry entry,
 			String waitlistSeatsInfo) throws HtmlParseException {
 		String[] waitlistSeatsInfoes = waitlistSeatsInfo.split(" ");
 		if (waitlistSeatsInfoes.length == 5) {
 			entry.setWaitlistSeats(new Seats(Integer
 					.valueOf(waitlistSeatsInfoes[2]), Integer
 					.valueOf(waitlistSeatsInfoes[3]), Integer
 					.valueOf(waitlistSeatsInfoes[4])));
 		} else {
 			throw new HtmlParseException(
 					"Waitlist seats info cannot be split to 5. We have "
 							+ waitlistSeatsInfoes.length + ".");
 		}
 	}
 
 	private void setCrosslistSeats(ScheduleDetailEntry entry,
 			String crosslistSeatsInfo) throws HtmlParseException {
 		String[] crosslistSeatsInfoes = crosslistSeatsInfo.split(" ");
 		if (crosslistSeatsInfoes.length == 6) {
 			entry.setCrosslistSeats(new Seats(Integer
 					.valueOf(crosslistSeatsInfoes[3]), Integer
 					.valueOf(crosslistSeatsInfoes[4]), Integer
 					.valueOf(crosslistSeatsInfoes[5])));
 		} else {
 			throw new HtmlParseException(
 					"Crosslist seats info cannot be split to 6. We have "
 							+ crosslistSeatsInfoes.length + ".");
 		}
 	}
 
 	/**
 	 * Set term, levels, campus and etc. based on the html passed to this
 	 * method.
 	 * 
 	 * @param entry
 	 *            ScheduleDetailEntry to be set.
 	 * @param remainingInfoHtml
 	 *            Html String contains information about the term, levels,
 	 *            campus.
 	 */
 	private void setRemainingInfo(ScheduleDetailEntry entry,
 			String remainingInfoHtml) {
 		// TODO: handle cross list courses. i.e. crn 10248
 		final int NOT_RECORD = 0;
 		final int PREREQUISTES = 1;
 		final int RESTRICTIONS = 2;
 		final int GENERAL_REQUIREMENTS = 3;
 		final int COREQUISITES = 4;
 
 		int recordType = NOT_RECORD;
 
 		String prerequisitesString = null;
 		String restrictionsString = null;
 		String generalRequirementsString = null;
 		String corequisitesString = null;
 
 		String[] remainingInfoes = remainingInfoHtml.split("<br />");
 		for (String info : remainingInfoes) {
 			info = info.trim();
 
 			if (recordType == PREREQUISTES) {
 				if (prerequisitesString == null) {
 					prerequisitesString = "";
 				}
 
 				if (!info.contains("Restrictions:")
 						&& !info.contains("General Requirements:")
 						&& !info.contains("Corequisites:")) {
 					prerequisitesString += " "
 							+ Utilities.removeHtmlTags(info).trim();
 				}
 			}
 
 			if (recordType == RESTRICTIONS) {
 				if (restrictionsString == null) {
 					restrictionsString = "";
 				}
 
 				if (!info.contains("Prerequisites:")
 						&& !info.contains("General Requirements:")
 						&& !info.contains("Corequisites:")) {
 					restrictionsString += " "
 							+ Utilities.removeHtmlTags(
 									info.replace("&nbsp;", "")).trim();
 				}
 			}
 
 			if (recordType == GENERAL_REQUIREMENTS) {
 				if (generalRequirementsString == null) {
 					generalRequirementsString = "";
 				}
 
 				if (!info.contains("Prerequisites:")
 						&& !info.contains("Restrictions:")
 						&& !info.contains("Corequisites:")) {
 					generalRequirementsString += " "
 							+ Utilities.removeHtmlTags(
 									info.replace("&nbsp;", "")).trim();
 				}
 			}
 
 			if (recordType == COREQUISITES) {
 				if (corequisitesString == null) {
 					corequisitesString = "";
 				}
 
 				if (!info.contains("Prerequisites:")
 						&& !info.contains("Restrictions:")
 						&& !info.contains("General Requirements:")) {
 					corequisitesString += " "
 							+ Utilities.removeHtmlTags(
 									info.replace("&nbsp;", "")).trim();
 				}
 			}
 
 			if (recordType == NOT_RECORD) {
 
 				if (info.contains("Associated Term: ")) {
 					String termString = info.substring(info.indexOf("</span>")
 							+ "</span>".length());
 					entry.setTerm(Term.valueOf(termString.replace(" ", "")
 							.toUpperCase()));
 					continue;
 				} else if (info.contains("Levels: ")) {
 					String levelsString = info.substring(info
 							.indexOf("</span>") + "</span>".length());
 					entry.setLevels(new ArrayList<String>(Arrays
 							.asList(levelsString.split(", "))));
 					continue;
 				} else if (info.contains("Campus")) {
 					String campusString = info.substring(0,
 							info.indexOf("Campus")).trim();
 					entry.setCampus(campusString);
 					continue;
 				} else if (info.contains("Schedule Type")) {
 					String typeString = info.substring(0,
 							info.indexOf("Schedule Type")).trim();
 					entry.setType(Type.valueOf(typeString.replace(" ", "")));
 					continue;
 				} else if (info.contains("Credits")) {
 					// TODO: require better handling of credit string with OR or
 					// TO contained
 					String creditsString = "0";
 					if (!info.contains("TO") && !info.contains("OR")) {
 						creditsString = info.substring(0,
 								info.indexOf("Credits")).trim();
 					} else if (info.contains("TO")) {
 						creditsString = info.substring(info.indexOf("TO") + 2,
 								info.indexOf("Credits")).trim();
 					} else if (info.contains("OR")) {
 						creditsString = info.substring(info.indexOf("OR") + 2,
 								info.indexOf("Credits")).trim();
 					}
 					entry.setCredits(Double.valueOf(creditsString));
 					continue;
 				}
 
 			}
 
 			if (info.contains("Restrictions:")) {
 				recordType = RESTRICTIONS;
 				continue;
 			} else if (info.contains("Prerequisites:")) {
 				recordType = PREREQUISTES;
 				continue;
 			} else if (info.contains("General Requirements:")) {
 				recordType = GENERAL_REQUIREMENTS;
 				continue;
 			} else if (info.contains("Corequisites:")) {
 				recordType = COREQUISITES;
 				continue;
 			}
 		}
 
 		if (prerequisitesString != null) {
 			entry.setPrerequisites(prerequisitesString);
 		}
 
 		if (restrictionsString != null) {
 			entry.setRestrictions(restrictionsString);
 		}
 
 		if (generalRequirementsString != null) {
 			entry.setGeneralRequirements(generalRequirementsString);
 		}
 
 		if (corequisitesString != null) {
 			entry.setCorequisites(corequisitesString);
 		}
 
 	}
 
 	/**
 	 * This class contains information return by ScheduleDetail.
 	 * 
 	 * @author Kaiwen Xu (kevin)
 	 */
 	public class ScheduleDetailEntry {
 
 		private String name;
 		private int crn;
 		private Subject subject;
 		private String cnbr;
 		private String section;
 		private Term term;
 		private List<String> levels;
 		private String campus;
 		private Type type;
 		private double credits;
 		private Seats seats;
 		private Seats waitlistSeats;
 		private Seats crossistSeats;
 		private String restrictions;
 		private String prerequisites;
 		private String generalRequirements;
 		private String corequisites;
 
 		private int searchCrn;
 		private Term searchTerm;
 
 		public ScheduleDetailEntry(Term term, int crn) {
 			this.searchTerm = term;
 			this.searchCrn = crn;
 
 		}
 
 		public Term getSearchTerm() {
 			return searchTerm;
 		}
 
 		public int getSearchCrn() {
 			return searchCrn;
 		}
 
 		public String getName() {
 			return name;
 		}
 
 		public int getCrn() {
 			return crn;
 		}
 
 		public Subject getSubject() {
 			return subject;
 		}
 
 		public String getCnbr() {
 			return cnbr;
 		}
 
 		public String getSection() {
 			return section;
 		}
 
 		public Term getTerm() {
 			return term;
 		}
 
 		public List<String> getLevels() {
 			return levels;
 		}
 
 		public String getCampus() {
 			return campus;
 		}
 
 		public Type getType() {
 			return type;
 		}
 
 		public double getCredits() {
 			return credits;
 		}
 
 		public Seats getSeats() {
 			return seats;
 		}
 
 		public Seats getWaitlistSeats() {
 			return waitlistSeats;
 		}
 
 		public Seats getCrosslistSeats() {
 			return crossistSeats;
 		}
 
 		public String getPrerequisites() {
 			return prerequisites;
 		}
 
 		public String getRestrictions() {
 			return restrictions;
 		}
 
 		public String getGeneralRequirements() {
 			return generalRequirements;
 		}
 
 		public String getCorequisites() {
 			return corequisites;
 		}
 
 		private void setName(String name) {
 			this.name = StringEscapeUtils.unescapeHtml(name).trim();
 		}
 
 		private void setCrn(int crn) {
 			this.crn = crn;
 		}
 
 		private void setSubject(Subject subject) {
 			this.subject = subject;
 		}
 
 		private void setCnbr(String cnbr) {
 			this.cnbr = cnbr;
 		}
 
 		private void setSection(String section) {
 			this.section = StringEscapeUtils.unescapeHtml(section).trim();
 		}
 
 		private void setTerm(Term term) {
 			this.term = term;
 		}
 
 		private void setLevels(List<String> levels) {
 			this.levels = levels;
 		}
 
 		private void setCampus(String campus) {
 			this.campus = StringEscapeUtils.unescapeHtml(campus).trim();
 		}
 
 		private void setType(Type type) {
 			this.type = type;
 		}
 
 		private void setCredits(double credits) {
 			this.credits = credits;
 		}
 
 		private void setSeats(Seats seats) {
 			this.seats = seats;
 		}
 
 		private void setWaitlistSeats(Seats waitlistSeats) {
 			this.waitlistSeats = waitlistSeats;
 		}
 
 		private void setCrosslistSeats(Seats crosslistSeats) {
 			this.crossistSeats = crosslistSeats;
 		}
 
 		private void setPrerequisites(String prerequisites) {
 			this.prerequisites = StringEscapeUtils.unescapeHtml(prerequisites)
 					.trim();
 		}
 
 		private void setRestrictions(String restrictions) {
 			this.restrictions = StringEscapeUtils.unescapeHtml(restrictions)
 					.trim();
 		}
 
 		private void setGeneralRequirements(String generalRequirements) {
 			this.generalRequirements = Utilities
 					.shrinkContentInParentheses(StringEscapeUtils.unescapeHtml(
 							generalRequirements).trim());
 		}
 
 		private void setCorequisites(String corequisites) {
 			this.corequisites = corequisites.trim();
 		}
 
 		@Override
 		public String toString() {
 			return "Course Name: " + name + "\n" + "CRN: " + crn + "\n"
 					+ "Subject: " + subject + "\n" + "CNBR: " + cnbr + "\n"
 					+ "Section: " + section + "\n" + "Term: " + term + "\n"
 					+ "Levels: " + levels + "\n" + "Campus: " + campus + "\n"
 					+ "Type: " + type + "\n" + "Credits: " + credits + "\n"
 					+ "Seats: " + seats + "\n" + "Waitlist Seats: "
 					+ waitlistSeats + "\n" + "Crosslist Seats: "
 					+ crossistSeats + "\n" + "Restrictions: " + restrictions
 					+ "\n" + "Prerequisites: " + prerequisites + "\n"
 					+ "General Requirements: " + generalRequirements + "\n"
 					+ "Corequisites: " + corequisites + "\n";
 		}
 	}
 
 }
