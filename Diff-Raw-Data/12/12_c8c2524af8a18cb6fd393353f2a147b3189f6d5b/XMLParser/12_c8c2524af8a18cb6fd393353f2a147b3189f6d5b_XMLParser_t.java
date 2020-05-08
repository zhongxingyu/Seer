 package spas.nhandling;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
import java.util.Collections;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.TimeZone;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import spas.XMLTools;
 import spas.nhandling.nelements.NCourse;
 import spas.nhandling.nelements.NCourseComparator;
 import spas.nhandling.nelements.NElement;
 import spas.nhandling.nelements.NElementFactory;
 import spas.nhandling.nelements.NElementType;
 import spas.nhandling.nelements.NEvent;
 
 /**
  * Parses XML into NElement objects. Needs InputSource to actually do something.
  * Can only parse XML-files as specified in <a href=
  * "https://wiki.aalto.fi/download/attachments/71895449/NoppaAPI_eng.pdf?version=2&modificationDate=1352361073000"
  * target="_blank">the Noppa API</a>. Usually works in tandem with
  * {@link NReader}.
  * 
  * @see NReader
  * @author Lauri Lavanti
  * @version 1.2.1
  * @since 0.1
  */
 public class XMLParser {
 
 	/**
 	 * Parses XML-file into NElement-objects. Gathers all the information
 	 * relevant to NElement-type into NElement, NCourse or NEvent
 	 * representation.
 	 * 
 	 * @param is
 	 *            InputSource for the XML-file to parse.
 	 * @return Objectified version of the XML-file, <code>List</code> with class
 	 *         that extends NElement, or an empty list if something went wrong.
 	 */
 	public static List<? extends NElement> parseFile(InputSource is) {
 
 		try {
 			// Turn XML-file into a Document.
 			Document doc = XMLTools.parse(is);
 
 			// Get root name.
 			String rootNode = doc.getDocumentElement().getNodeName();
 
 			// Send the Document to be handled in the correct method.
 			if (rootNode.equalsIgnoreCase("ORGANIZATIONS")) {
 				return parseOrganizations(doc);
 			} else if (rootNode.equalsIgnoreCase("DEPARTMENTS")) {
 				return parseDepartments(doc);
 			} else if (rootNode.equalsIgnoreCase("COURSES")) {
 				return parseCourses(doc);
 			} else if (rootNode.equalsIgnoreCase("OVERVIEW")) {
 				return parseCourse(doc);
 			} else if (rootNode.equalsIgnoreCase("LECTURES")) {
 				return parseLectures(doc);
 			} else if (rootNode.equalsIgnoreCase("EXERCISES")) {
 				return parseExercises(doc);
 			} else if (rootNode.equalsIgnoreCase("ASSIGNMENTS")) {
 				return parseAssignments(doc);
 			} else if (rootNode.equalsIgnoreCase("EVENTS")) {
 				return parseEvents(doc);
 			}
 		} catch (IOException | SAXException | ParserConfigurationException e) {
 			// These should never happen.
 		}
 		// In case of an error or wrong XML-file, return an empty list.
 		return new ArrayList<NElement>();
 	}
 
 	/**
 	 * Parses organizations correctly from Document.
 	 * 
 	 * @param doc
 	 *            Document to be parsed.
 	 * @return List of organizations contained in Document.
 	 */
 	private static List<NElement> parseOrganizations(Document doc) {
 		List<NElement> objs = new ArrayList<NElement>();
 
 		// Get the list of organizations and loop through it.
 		for (Element e : XMLTools.getElements(doc, "organization")) {
 
 			// Create the NElement object.
 			NElement organization = NElementFactory
 					.createNElement(NElementType.ORGANIZATION);
 
 			// Getting and setting the id for the organization.
 			organization.setId(XMLTools.getTagValue("org_id", e));
 
 			// Getting and setting the name for the organization.
 			organization.setName(XMLTools.getTagValue("name_fi", e));
 
 			// Add it to the list.
 			objs.add(organization);
 		} // end of for-loop.
 		return objs;
 	}
 
 	/**
 	 * Parses departments correctly from Document.
 	 * 
 	 * @param doc
 	 *            Document to be parsed.
 	 * @return List of departments contained in the Document.
 	 */
 	private static List<NElement> parseDepartments(Document doc) {
 		List<NElement> objs = new ArrayList<NElement>();
 
 		// Get the list of departments and loop through it.
 		for (Element e : XMLTools.getElements(doc, "department")) {
 
 			// Create the NElement object.
 			NElement department = NElementFactory
 					.createNElement(NElementType.DEPARTMENT);
 
 			// Getting and setting the id for the department.
 			department.setId(XMLTools.getTagValue("dept_id", e));
 
 			// Getting and setting the organization id for the department.
 			department.setOrgId(XMLTools.getTagValue("org_id", e));
 
 			// Getting and setting the name for the department.
 			department.setName(XMLTools.getTagValue("name_fi", e));
 
 			// Add it to the list.
 			objs.add(department);
 		} // end of for-loop.
 		return objs;
 	}
 
 	/**
 	 * Parses courses correctly from Document.
 	 * 
 	 * @param doc
 	 *            Document to be parsed.
 	 * @return List of courses contained in the Document.
 	 */
 	private static List<NCourse> parseCourses(Document doc) {
 		List<NCourse> objs = new ArrayList<NCourse>();
 
 		// Get the list of courses and loop through it.
 		for (Element e : XMLTools.getElements(doc, "course")) {
 
 			// Create the NCourse object.
 			NCourse course = (NCourse) NElementFactory
 					.createNElement(NElementType.COURSE);
 
 			// Getting and setting the id for the course.
 			course.setId(XMLTools.getTagValue("course_id", e));
 
 			// Getting and setting the name for the course.
 			course.setName(XMLTools.getTagValue("name", e));
 
 			// Add it to the list.
 			objs.add(course);
 		} // end of for-loop.
 
 		// Sort list before returning it.
 		Collections.sort(objs, new NCourseComparator());
 		return objs;
 	}
 
 	/**
 	 * Parses a course correctly from the Document. Gets more information than
 	 * {@link XMLParser#parseCourses(Document) parseCourses(doc)}. The reason
 	 * why this method returns a list as well even though it will always contain
 	 * one Object at most, is to enable calling of only one method in this
 	 * class.
 	 * 
 	 * @param doc
 	 *            Document to be parsed.
 	 * @return A list containing the one course parsed correctly from Document.
 	 */
 	private static List<NCourse> parseCourse(Document doc) {
 		List<NCourse> objs = new ArrayList<NCourse>();
 
 		// Create the NCourse object and get the Element-form presentation of
 		// the course.
 		NCourse course = (NCourse) NElementFactory
 				.createNElement(NElementType.COURSE);
 		Element OverviewElement = doc.getDocumentElement();
 
 		// Getting and setting the id for the course.
 		course.setId(XMLTools.getTagValue("course_id", OverviewElement));
 
 		// Getting and setting the credits for the course.
 		course.setCredits(parseHTML(XMLTools.getTagValue("credits",
 				OverviewElement)));
 
 		// Getting and setting the periods for the course.
 		course.setPeriods(parseHTML(XMLTools.getTagValue("teaching_period",
 				OverviewElement)));
 
 		// Getting and setting the content for the course.
 		course.setContent(parseHTML(XMLTools.getTagValue("content",
 				OverviewElement)));
 
 		// Add it to the list.
 		objs.add(course);
 
 		return objs;
 	}
 
 	/**
 	 * Parses lectures correctly from Document.
 	 * 
 	 * @param doc
 	 *            Document to be parsed.
 	 * @return List of lectures contained in the Document.
 	 */
 	private static List<NEvent> parseLectures(Document doc) {
 		List<NEvent> objs = new ArrayList<NEvent>();
 
 		// Create the Calendar-basis and add TimeZone to them. Also create
 		// appropriate String->Date formatter.
 		Calendar startDate = new GregorianCalendar();
 		startDate.setTimeZone(TimeZone.getTimeZone("Europe/Helsinki"));
 		Calendar endDate = (Calendar) startDate.clone();
 		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
 
 		// Get the list of lectures and loop through it.
 		for (Element e : XMLTools.getElements(doc, "lecture")) {
 
 			// Copy startdate and enddate to new Calendars.
 			startDate = (Calendar) startDate.clone();
 			endDate = (Calendar) endDate.clone();
 
 			// Create the NEvent object.
 			NEvent lecture = (NEvent) NElementFactory
 					.createNElement(NElementType.LECTURE);
 
 			// Getting the date for the Lecture.
 			String date = XMLTools.getTagValue("date", e);
 
 			// Setting the start and end date for the lecture.
 			try {
 				// Parse starting datetime.
 				startDate.setTime(formatter.parse(date + " "
 						+ XMLTools.getTagValue("start_time", e)));
 
 				// Parse ending datetime.
 				endDate.setTime(formatter.parse(date + " "
 						+ XMLTools.getTagValue("end_time", e)));
 
 				// Add datetimes to lecture.
 				lecture.setStartDate(startDate);
 				lecture.setEndDate(endDate);
 			} catch (ParseException ex) {
 				// This should not normally happen.
 			}
 
 			// Getting and setting the location for the lecture.
 			lecture.setLocation(XMLTools.getTagValue("location", e));
 
 			// Getting and setting the description for the lecture.
 			lecture.setDescription(XMLTools.getTagValue("content", e));
 
 			objs.add(lecture);
 		} // end of for-loop.
 		return objs;
 	}
 
 	/**
 	 * Parses exercises from Document.
 	 * 
 	 * @param doc
 	 *            Document to be parsed.
 	 * @return List of exercises contained in the Document.
 	 */
 	private static List<NEvent> parseExercises(Document doc) {
 		List<NEvent> objs = new ArrayList<NEvent>();
 
 		// Create the Calendar-basis and add TimeZone to them. Also create
 		// appropriate String-Date formatter.
 		Calendar startDate = new GregorianCalendar();
 		startDate.setTimeZone(TimeZone.getTimeZone("Europe/Helsinki"));
 		Calendar endDate = (Calendar) startDate.clone();
 		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
 
 		// Get the list of exercises and loop through it.
 		for (Element e : XMLTools.getElements(doc, "exercise")) {
 
 			// Copy startdate and enddate to new Calendars.
 			startDate = (Calendar) startDate.clone();
 			endDate = (Calendar) endDate.clone();
 
 			// Create the NEvent object.
 			NEvent exercise = (NEvent) NElementFactory
 					.createNElement(NElementType.EXERCISE);
 
 			// Getting and setting the group for the exercise.
 			exercise.setGroup(XMLTools.getTagValue("group", e));
 
 			// Getting and setting the location for the exercise.
 			exercise.setLocation(XMLTools.getTagValue("location", e));
 
 			// Setting the start and end date for the exercise.
 			try {
 				// Parse starting datetime.
 				startDate.setTime(formatter.parse(XMLTools.getTagValue(
 						"start_date", e)
 						+ " "
 						+ XMLTools.getTagValue("start_time", e)));
 
 				// Parse ending datetime.
 				endDate.setTime(formatter.parse(XMLTools.getTagValue(
 						"end_date", e)
 						+ " "
 						+ XMLTools.getTagValue("end_time", e)));
 
 				// Add datetimes to exercise.
 				exercise.setStartDate(startDate);
 				exercise.setEndDate(endDate);
 			} catch (ParseException ex) {
 				// This should not normally happen.
 			}
 
 			// Add it to the list.
 			objs.add(exercise);
 		} // end of for-loop.
 		return objs;
 	}
 
 	/**
 	 * Parses assignments from Document.
 	 * 
 	 * @param doc
 	 *            Document to be parsed.
 	 * @return List of assignments contained in Document.
 	 */
 	private static List<NEvent> parseAssignments(Document doc) {
 		List<NEvent> objs = new ArrayList<NEvent>();
 
 		// Create the Calendar-basis and set TimeZone to it. Also create
 		// appropriate String-Date formatter.
 		Calendar dueDate = new GregorianCalendar();
 		dueDate.setTimeZone(TimeZone.getTimeZone("Europe/Helsinki"));
 		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
 
 		// Get the list of assignments and loop through it.
 		for (Element e : XMLTools.getElements(doc, "assignment")) {
 
 			// Copy duedate to new Calendars.
 			dueDate = (Calendar) dueDate.clone();
 
 			// Create the NEvent object.
 			NEvent assignment = (NEvent) NElementFactory
 					.createNElement(NElementType.ASSIGNMENT);
 
 			// Getting and setting the deadline for the assignment.
 			try {
 				dueDate.setTime(formatter.parse(XMLTools.getTagValue(
 						"deadline", e)));
 				assignment.setEndDate(dueDate);
 			} catch (ParseException ex) {
 				// This should not normally happen.
 			}
 
 			// Getting and setting the description for the assignment.
 			assignment.setDescription(XMLTools.getTagValue("title", e));
 
 			// Add it to the list.
 			objs.add(assignment);
 		} // end of for-loop.
 		return objs;
 	}
 
 	/**
 	 * Parses events from Document.
 	 * 
 	 * @param doc
 	 *            Document to be parsed.
 	 * @return List of events contained in the Document.
 	 */
 	private static List<NEvent> parseEvents(Document doc) {
 		List<NEvent> objs = new ArrayList<NEvent>();
 
 		// Create the Calendar-basis and add TimeZone to them. Also create the
 		// appropriate String-Date formatter.
 		Calendar startDate = new GregorianCalendar();
 		startDate.setTimeZone(TimeZone.getTimeZone("Europe/Helsinki"));
 		Calendar endDate = (Calendar) startDate.clone();
 		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
 
 		// Get the list of events and loop through them.
 		for (Element e : XMLTools.getElements(doc, "event")) {
 
 			// Copy startdate and enddate to new Calendars.
 			startDate = (Calendar) startDate.clone();
 			endDate = (Calendar) endDate.clone();
 
 			// Create the NEvent object.
 			NEvent event = (NEvent) NElementFactory
 					.createNElement(NElementType.OTHER);
 
 			// Setting the description for the event.
 			event.setDescription(XMLTools.getTagValue("title", e) + ", "
 					+ XMLTools.getTagValue("type", e));
 
 			// Getting and setting the location for the event.
 			event.setLocation(XMLTools.getTagValue("location", e));
 
 			// Setting the start and end for the event.
 			try {
 				// Parse starting datetime.
 				startDate.setTime(formatter.parse(XMLTools.getTagValue(
 						"start_date", e)
 						+ " "
 						+ XMLTools.getTagValue("start_time", e)));
 
 				// Parse ending datetime.
 				endDate.setTime(formatter.parse(XMLTools.getTagValue(
 						"end_date", e)
 						+ " "
 						+ XMLTools.getTagValue("end_time", e)));
 
 				// Set datetimes to event.
 				event.setStartDate(startDate);
 				event.setEndDate(endDate);
 			} catch (ParseException ex) {
 				// This should not normally happen.
 			}
 
 			// Add it to the list.
 			objs.add(event);
 		} // end of for-loop.
 		return objs;
 	}
 
 	/**
 	 * Method to parse html-tags out of text given as a parameter.
 	 * 
 	 * @param html
 	 *            HTML-String to parse.
 	 * @return Given String without HTML-tags.
 	 */
 	private static String parseHTML(String html) {
 		// Continue editing given text as long as it contains a tag.
 		while (html.contains("<") && html.contains(">")) {
 
 			// Parse one tag out of it.
 			html = html.replace(
 					html.substring(html.indexOf("<"), html.indexOf(">") + 1),
 					"");
 		}
 		return html;
 	}
 }
