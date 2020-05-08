 package spas.taglib.printers;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.jsp.JspWriter;
 import javax.servlet.jsp.tagext.TagSupport;
 
import edu.emory.mathcs.backport.java.util.Collections;

 import spas.nhandling.NReader;
 import spas.nhandling.nelements.NCourse;
 import spas.nhandling.nelements.NEvent;
 import spas.usercontrol.UserCourseHandler;
 
 /**
  * Prints out course given as GET-parameters (name and id). If information was
  * not given, it redirects user to welcome.jsp, otherwise prints it out in a
  * table containing information about it. Also adds a remove/add button
  * accordingly and handles it. Now also adds edit form for course, if it's
  * added.
  * 
  * @author Lauri Lavanti
  * @version 1.2.1
  * @since 1.0
  * @see NReader
  * @see UserCourseHandler
  * 
  */
 public class PrintCourse extends TagSupport {
 
 	@Override
 	public int doStartTag() {
 		/*
 		 * A word of warning: this method is long, but I had no good way to
 		 * split it into better and smaller methods, or at least I couldn't
 		 * think of one.
 		 */
 
 		// Get request and writer.
 		HttpServletRequest request = (HttpServletRequest) pageContext
 				.getRequest();
 		JspWriter out = pageContext.getOut();
 
 		// Get given id and name.
 		String id = request.getParameter("id");
 		String name = request.getParameter("name");
 
 		// Get the course.
 		NCourse c = new NReader().getCourseOverview(id, name);
 
 		// In case of a new course, add id.
 		if (c.getId().equals("")) {
 			c.setId(id);
 		}
 
 		// In case of a new course, add name.
 		if (c.getName().equals("")) {
 			c.setName(name);
 		}
 
 		// Create course handler.
 		UserCourseHandler handler = new UserCourseHandler(pageContext
 				.getServletContext().getRealPath(
 						"/resources/users/"
 								+ pageContext.getSession().getAttribute(
 										"username") + ".xml"));
 
 		try {
 			// Build periods for course.
 			String periods = "";
 			for (String p : c.getPeriods()) {
 				if (!periods.equals("")) {
 					periods += ", ";
 				}
 				periods += p;
 			}
 			// Print out the information for the course.
 			out.println(c.getId() + ": " + c.getName() + "<br/>");
 			out.println("<table>");
 			out.println("<tr><td>Opintopisteet</td><td>" + c.getCredits()
 					+ "</td></tr>");
 			out.println("<tr><td>Opetusperiodi(t)</td><td>" + periods
 					+ "</td></tr>");
 			out.println("<tr><td>Sisältö</td><td>" + c.getContent()
 					+ "</td></tr>");
 			out.println("<table><br/><br/><br/>");
 
 			// If user pressed add, try to add course and print appropriately.
 			if (request.getParameter("add") != null) {
 				if (handler.addCourse(c)) {
 					out.println("<p class='success'>Kurssi lisättiin"
 							+ " onnistuneesti.</p>");
 				} else {
 					// Adding failed.
 					out.println("<p class='error'>Kurssin lisäyksessä "
 							+ "oli ongelma, kokeile myöhemmin "
 							+ "uudelleen.</p>");
 				}
 			}
 
 			/*
 			 * If user pressed remove, try to remove course and print
 			 * appropriately.
 			 */
 			if (request.getParameter("remove") != null) {
 				if (handler.removeCourse(id)) {
 					out.println("<p class='success'>Kurssi poistettiin "
 							+ "onnistuneesti.</p>");
 				} else {
 					// Removing failed.
 					out.println("<p class='error'>Kurssin poistamisessa "
 							+ "oli ongelma, kokeile myöhemmin "
 							+ "uudelleen.</p>");
 				}
 			}
 
 			// Get the link to the file using this tag..
 			String path = request.getRequestURL() + "?"
 					+ request.getQueryString();
 
 			if (handler.containsCourse(id)) {
 				// Get course from user's file.
 				for (NCourse co : handler.getCourses()) {
 					if (co.getId().equals(id)) {
 						// Add periods to new representation of course.
 						for (String period : c.getPeriods()) {
 							co.setPeriods(period);
 						}
 						co.setCredits(c.getCredits());
 						c = co;
 						break;
 					}
 				}
 
 				// Print out editing forms for active course.
 				out.println(getAddedCourse(c, path, handler));
 				out.println("<form action='" + path
 						+ "' method='post' onsubmit='confirmSubmit()'>");
 				out.println("<input type='submit' name='remove' "
 						+ "value='Poista' /></form>");
 			} else {
 				// Print adding form.
 				out.println("<form action='" + path + "' method='post'>");
 				out.println("<input type='submit' name='add' "
 						+ "value='Lisää' /></form>");
 			}
 		} catch (IOException e) {
 			// This should never happen.
 		}
 
 		return SKIP_BODY;
 	}
 
 	/**
 	 * Builds String representation in HTML for an added course's edit form.
 	 * 
 	 * @param c
 	 *            Course to be edited.
 	 * @param path
 	 *            Path to url using this tag.
 	 * @param handler
 	 *            Coursehandler for user.
 	 * @return HTML-representation of course.
 	 */
 	private String getAddedCourse(NCourse c, String path,
 			UserCourseHandler handler) {
 		String course = "";
 
 		// Get course's groups and insert them into a set to avoid duplicates.
 		List<NEvent> exercises = new NReader().getCourseExercises(c);
 		Collection<String> groups = new HashSet<String>();
 		for (NEvent e : exercises) {
 
 			// Make sure exercises actually have groups, before adding.
 			if (!e.getGroup().equals("")) {
 				groups.add(e.getGroup());
 			}
 		}
 		String groupmenu = "";
 		if (groups.size() > 0) {
 			// Add groups to List for sorting.
 			groups = new ArrayList<String>(groups);
 			Collections.sort((List<String>) groups);
 
 			/*
 			 * If course had groups, get chosen and then create a form to pick a
 			 * new group.
 			 */
 			String chosengroup = handler.getGroup(id);
 			groupmenu = "Ryhmä: <select name='group'>"
 					+ "<option value=' '>--</option>";
 
 			// Loop through groups and add them as options.
 			for (String group : groups) {
 				String selected = chosengroup.equals(group) ? "selected" : "";
 				groupmenu += "<option value='" + group + "' " + selected + ">"
 						+ group + "</option>";
 			}
 
 			// Build the rest of the group selection form.
 			groupmenu += "</select><br/>";
 
 		} // end of if-group-size-clause.
 
 		// Get periods and loop through them.
 		String periodmenu = "";
 		List<String> periods = c.getPeriods();
 		// Make sure it isn't empty.
 		if (periods.size() > 0) {
 			periodmenu = "Suoritusperiodi: <select name='period'>";
 			// Loop through periods and add them to menu.
 			for (String period : periods) {
 				String selected = period.equals(c.getExecperiod()) ? "selected"
 						: "";
 				periodmenu += "<option value='" + period + "' " + selected
 						+ ">" + period + "</option>";
 			}
 			periodmenu += "</select><br/>";
 		}
 
 		// Build year-selection menu.
 		int pyear = Calendar.getInstance().get(Calendar.YEAR);
 		int[] years = { pyear - 1, pyear, pyear + 1, pyear + 2, pyear + 3,
 				pyear + 4, pyear + 5 };
 		String yearmenu = "Suoritusvuosi: <select name='year'>";
 		// Loop through years and add them to menu.
 		for (int year : years) {
 			String selected = year == c.getExecyear() ? "selected" : "";
 			yearmenu += "<option value='" + year + "' " + selected + ">" + year
 					+ "</option>";
 		}
 		yearmenu += "</select><br />";
 
 		// Add forms for completing and deactivating course.
 		course += "<form action='" + path + "' method='post'><br/>" + groupmenu
 				+ periodmenu + yearmenu + "<input type='submit' name='edit' "
 				+ "value='Tallenna' /></form>";
 
 		return course;
 	}
 }
