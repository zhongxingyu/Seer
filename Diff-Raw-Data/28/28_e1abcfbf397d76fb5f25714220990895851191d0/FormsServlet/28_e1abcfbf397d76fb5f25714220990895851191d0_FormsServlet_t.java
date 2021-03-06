 package edu.iastate.music.marching.attendance.servlets;
 
 import java.io.IOException;
 import java.net.URLEncoder;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.TimeZone;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import edu.iastate.music.marching.attendance.App;
 import edu.iastate.music.marching.attendance.controllers.DataTrain;
 import edu.iastate.music.marching.attendance.controllers.FormController;
 import edu.iastate.music.marching.attendance.model.Absence;
 import edu.iastate.music.marching.attendance.model.Form;
 import edu.iastate.music.marching.attendance.model.User;
 import edu.iastate.music.marching.attendance.util.PageBuilder;
 import edu.iastate.music.marching.attendance.util.Util;
 import edu.iastate.music.marching.attendance.util.ValidationUtil;
 
 public class FormsServlet extends AbstractBaseServlet {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -4738485557840953303L;
 
 	private enum Page {
 		forma, formb, formc, formd, index, view, remove, messages;
 	}
 
 	private static final String SERVLET_PATH = "form";
 
 	private static final String SUCCESS_FORMA = "Submitted new Form A";
 	private static final String SUCCESS_FORMB = "Submitted new Form B";
 	private static final String SUCCESS_FORMC = "Submitted new Form C";
 	private static final String SUCCESS_FORMD = "Submitted new Form D";
 
 	@Override
 	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 
 		if (!isLoggedIn(req, resp)) {
 			resp.sendRedirect(AuthServlet.getLoginUrl(req));
 			return;
 		} else if (!isLoggedIn(req, resp, getServletUserTypes())) {
 			resp.sendRedirect(ErrorServlet.getLoginFailedUrl(req));
 			return;
 		}
 
 		Page page = parsePathInfo(req.getPathInfo(), Page.class);
 		if (page == null) {
 			show404(req, resp);
 			return;
 		}
 
 		switch (page) {
 		case forma:
 			postFormA(req, resp);
 			break;
 		case formb:
 			handleFormB(req, resp);
 			break;
 		case formc:
 			handleFormC(req, resp);
 			break;
 		case formd:
 			handleFormD(req, resp);
 			break;
 		case index:
 			showIndex(req, resp);
 			break;
 		case view:
 			viewForm(req, resp, new LinkedList<String>(), "");
 			break;
 		// case remove:
 		// removeForm(req, resp);
 		// break;
 		case messages:
 			break;
 		default:
 			ErrorServlet.showError(req, resp, 404);
 		}
 	}
 
 	private void viewForm(HttpServletRequest req, HttpServletResponse resp,
 			List<String> errors, String success_message)
 			throws ServletException, IOException {
 		DataTrain train = DataTrain.getAndStartTrain();
 		User currentUser = train.getAuthController().getCurrentUser(
 				req.getSession());
 		Form form = null;
 		try {
 			long id = Long.parseLong(req.getParameter("id"));
 			form = train.getFormsController().get(id);
 			PageBuilder page = new PageBuilder(Page.view, SERVLET_PATH);
 			page.setPageTitle("Form " + form.getType());
 			page.setAttribute("form", form);
 			page.setAttribute("day", form.getDayAsString());
 			page.setAttribute("isDirector", currentUser.getType().isDirector());
 			page.setAttribute("error_messages", errors);
 			page.setAttribute("success_message", success_message);
 			page.passOffToJsp(req, resp);
 		} catch (NumberFormatException nfe) {
 			ErrorServlet.showError(req, resp, 500);
 		}
 	}
 
 	@Override
 	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 
 		if (!isLoggedIn(req, resp)) {
 			resp.sendRedirect(AuthServlet.getLoginUrl());
 			return;
 		} else if (!isLoggedIn(req, resp, getServletUserTypes())) {
 			resp.sendRedirect(ErrorServlet.getLoginFailedUrl(req));
 			return;
 		}
 
 		Page page = parsePathInfo(req.getPathInfo(), Page.class);
 
 		if (page == null)
 			ErrorServlet.showError(req, resp, 404);
 		else
 			switch (page) {
 			case index:
 				showIndex(req, resp);
 				break;
 			case forma:
 				postFormA(req, resp);
 				break;
 			case formb:
 				handleFormB(req, resp);
 				break;
 			case formc:
 				handleFormC(req, resp);
 				break;
 			case formd:
 				handleFormD(req, resp);
 				break;
 			case messages:
 				break;
 			case view:
 				updateStatus(req, resp);
 				break;
 			default:
 				ErrorServlet.showError(req, resp, 404);
 			}
 	}
 
 	private void updateStatus(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		DataTrain train = DataTrain.getAndStartTrain();
 		FormController fc = train.getFormsController();
 
 		List<String> errors = new LinkedList<String>();
 		String success_message = "";
 		boolean validForm = true;
 
 		Form f = null;
 		Form.Status status = null;
 		long id = 0;
 		if (!ValidationUtil.isPost(req)) {
 			validForm = false;
 		}
 		if (validForm) {
 			try {
 				id = Long.parseLong(req.getParameter("id"));
 				f = fc.get(id);
 				String strStat = req.getParameter("status");
 				if (strStat.equalsIgnoreCase("Approved"))
 					status = Form.Status.Approved;
 				else if (strStat.equalsIgnoreCase("Pending"))
 					status = Form.Status.Pending;
 				else if (strStat.equalsIgnoreCase("Denied"))
 					status = Form.Status.Denied;
 				else {
 					validForm = false;
 					errors.add("Invalid form status.");
 				}
 
 			} catch (NumberFormatException e) {
 				errors.add("Unable to find form.");
 				validForm = false;
 			} catch (NullPointerException e) {
 				errors.add("Unable to find form.");
 				validForm = false;
 			}
 		}
 
 		if (validForm) {
 			// Send them back to their Form page
 			f.setStatus(status);
 			fc.update(f);
 			success_message = "Successfully updated form.";
 		}
 
 		viewForm(req, resp, errors, success_message);
 
 	}
 
 	private void postFormA(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		String reason = null;
 		Date date = null;
 
 		DataTrain train = DataTrain.getAndStartTrain();
 
 		// Parse out all data from form and validate
 		boolean validForm = true;
 		List<String> errors = new LinkedList<String>();
 
 		if (!ValidationUtil.isPost(req)) {
 			// This is not a post request to create a new form, no need to
 			// validate
 			validForm = false;
 		} else {
 			// Extract all basic parameters
 			reason = req.getParameter("Reason");
 
 			try {
 				date = Util.parseDate(req.getParameter("StartMonth"),
 						req.getParameter("StartDay"),
 						req.getParameter("StartYear"), "0", "AM", "0", train
 								.getAppDataController().get().getTimeZone());
 			} catch (IllegalArgumentException e) {
 				validForm = false;
 				errors.add("Invalid Input: The input date is invalid.");
 			}
 		}
 
 		if (validForm) {
 			// Store our new form to the data store
 			User student = train.getAuthController().getCurrentUser(
 					req.getSession());
 
 			Form form = null;
 			try {
 				form = train.getFormsController().createFormA(student, date,
 						reason);
 			} catch (IllegalArgumentException e) {
 				validForm = false;
 				errors.add(e.getMessage());
 			}
 
 			if (form == null) {
 				validForm = false;
				errors.add("Fix any errors above and try to resubmit. If you're still having issues, submit a bug report using the form at the bottom of the page.");
 			}
 		}
 		if (validForm) {
 			String url = getIndexURL() + "?success_message="
 					+ URLEncoder.encode(SUCCESS_FORMA, "UTF-8");
 			// url = resp.encodeRedirectURL(url);
 			resp.sendRedirect(url);
 		} else {
 			// Show form
 			PageBuilder page = new PageBuilder(Page.forma, SERVLET_PATH);
 
 			page.setPageTitle("Form A");
 
 			page.setAttribute("error_messages", errors);
 
 			page.setAttribute("cutoff", train.getAppDataController().get()
 					.getFormSubmissionCutoff());
 
 			page.setAttribute("Reason", reason);
 			setStartDate(date, page, train.getAppDataController().get()
 					.getTimeZone());
 
 			page.passOffToJsp(req, resp);
 		}
 	}
 
 	private void handleFormB(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		String department = null;
 		String course = null;
 		String section = null;
 		String building = null;
 		Date startDate = null;
 		Date endDate = null;
 		Date fromTime = null;
 		Date toTime = null;
 		int day = -1;
 		int minutesToOrFrom = 0;
 		String comments = null;
 		Absence.Type absenceType = null;
 
 		DataTrain train = DataTrain.getAndStartTrain();
 
 		// Parse out all data from form and validate
 		boolean validForm = true;
 		List<String> errors = new LinkedList<String>();
 
 		if (!ValidationUtil.isPost(req)) {
 			// This is not a post request to create a new form, no need to
 			// validate
 			validForm = false;
 		} else {
 			// Extract all basic parameters
 			department = req.getParameter("Department");
 			course = req.getParameter("Course");
 			section = req.getParameter("Section");
 			building = req.getParameter("Building");
 			comments = req.getParameter("Comments");
 
 			String minutestmp = req.getParameter("MinutesToOrFrom");
 
 			try {
 				minutesToOrFrom = Integer.parseInt(minutestmp);
 			} catch (NumberFormatException nfe) {
 				errors.add("Minutes to or from must be a whole number.");
 			}
 
 			String stype = req.getParameter("Type");
 			if (stype != null && !stype.equals("")) {
 				if (stype.equals(Absence.Type.Absence.getValue())) {
 					absenceType = Absence.Type.Absence;
 				} else if (stype.equals(Absence.Type.Tardy.getValue())) {
 					absenceType = Absence.Type.Tardy;
 				} else if (stype.equals(Absence.Type.EarlyCheckOut.getValue())) {
 					absenceType = Absence.Type.EarlyCheckOut;
 				}
 			} else {
 				errors.add("Invalid type.");
 			}
 
 			// this is one-based! Starting on Sunday.
 			
 			try {
 			day = Integer.parseInt(req.getParameter("DayOfWeek"));
 			
 			} catch (NumberFormatException nfe) {
 				errors.add("Weekday was invalid.");
 			}
 			
 			if (day < 1 || day > 7) {
 				errors.add("Value of " + day + " for day was not valid.");
 			}
 
 			try {
 				startDate = Util.parseDate(req.getParameter("StartMonth"),
 						req.getParameter("StartDay"),
 						req.getParameter("StartYear"), "0", "AM", "0", train
 								.getAppDataController().get().getTimeZone());
 			} catch (IllegalArgumentException e) {
 				validForm = false;
 				errors.add("The start date is invalid.");
 			}
 			try {
 				endDate = Util.parseDate(req.getParameter("EndMonth"),
 						req.getParameter("EndDay"),
 						req.getParameter("EndYear"), "0", "AM", "0", train
 								.getAppDataController().get().getTimeZone());
 			} catch (IllegalArgumentException e) {
 				validForm = false;
 				errors.add("The end date is invalid.");
 			}
 			try {
 				fromTime = Util.parseDate("1", "1", "1000",
 						req.getParameter("FromHour"),
 						req.getParameter("FromAMPM"),
 						req.getParameter("FromMinute"), train
 								.getAppDataController().get().getTimeZone());
 			} catch (IllegalArgumentException e) {
 				validForm = false;
 				errors.add("The start time is invalid.");
 			}
 			try {
 				toTime = Util.parseDate("1", "1", "1000",
 						req.getParameter("ToHour"), req.getParameter("ToAMPM"),
 						req.getParameter("ToMinute"), train
 								.getAppDataController().get().getTimeZone());
 			} catch (IllegalArgumentException e) {
 				validForm = false;
 				errors.add("The end time is invalid.");
 			}
 
 		}
 
 		if (validForm) {
 			// Store our new form to the datastore
 			User student = train.getAuthController().getCurrentUser(
 					req.getSession());
 
 			Form form = null;
 			try {
 				form = train.getFormsController().createFormB(student,
 						department, course, section, building, startDate,
 						endDate, day, fromTime, toTime, comments,
 						minutesToOrFrom, absenceType);
 			} catch (IllegalArgumentException e) {
 				validForm = false;
 				errors.add(e.getMessage());
 			}
 
 			if (form == null) {
 				validForm = false;
				errors.add("Fix any errors above and try to resubmit. If you're still having issues, submit a bug report using the form at the bottom of the page.");
 			}
 		}
 		if (validForm) {
 			String url = getIndexURL() + "?success_message="
 					+ URLEncoder.encode(SUCCESS_FORMB, "UTF-8");
 			url = resp.encodeRedirectURL(url);
 			resp.sendRedirect(url);
 		} else {
 			// Show form
 			PageBuilder page = new PageBuilder(Page.formb, SERVLET_PATH);
 
 			page.setPageTitle("Form B");
 			page.setAttribute("daysOfWeek", App.getDaysOfTheWeek());
 			page.setAttribute("error_messages", errors);
 			page.setAttribute("Department", department);
 			page.setAttribute("Course", course);
 			page.setAttribute("Section", section);
 			page.setAttribute("Building", building);
 			setStartDate(startDate, page, train.getAppDataController().get()
 					.getTimeZone());
 			setEndDate(endDate, page, train.getAppDataController().get()
 					.getTimeZone());
 			page.setAttribute("Type", Form.Type.B);
 			page.setAttribute("Comments", comments);
 			page.setAttribute("MinutesToOrFrom", minutesToOrFrom);
 			page.setAttribute("Type", absenceType);
 			page.setAttribute("types", Absence.Type.values());
			
			if (fromTime != null) {
				Calendar from = Calendar.getInstance();
				from.setTime(fromTime);
				page.setAttribute("FromHour", from.get(Calendar.HOUR));
				page.setAttribute("FromMinute", from.get(Calendar.MINUTE));
				page.setAttribute("FromAMPM", from.get(Calendar.AM_PM) == 0 ? "AM" : "PM");
			}

			if (toTime != null) {
				Calendar to = Calendar.getInstance();
				to.setTime(toTime);
				page.setAttribute("ToHour", to.get(Calendar.HOUR));
				page.setAttribute("ToMinute", to.get(Calendar.MINUTE));
				page.setAttribute("ToAMPM", to.get(Calendar.AM_PM) == 0 ? "AM" : "PM");
			}
 
 			page.passOffToJsp(req, resp);
 		}
 	}
 
 	private void handleFormC(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		Date date = null;
 		String reason = null;
 		Absence.Type type = null;
 
 		DataTrain train = DataTrain.getAndStartTrain();
 
 		// Parse out all data from form and validate
 		boolean validForm = true;
 		List<String> errors = new LinkedList<String>();
 
 		if (!ValidationUtil.isPost(req)) {
 			// This is not a post request to create a new form, no need to
 			// validate
 			validForm = false;
 		} else {
 			// Extract all basic parameters
 			reason = req.getParameter("Reason");
 
 			String stype = req.getParameter("Type");
 			if (stype != null && !stype.equals("")) {
 				if (stype.equals(Absence.Type.Absence.getValue())) {
 					type = Absence.Type.Absence;
 				} else if (stype.equals(Absence.Type.Tardy.getValue())) {
 					type = Absence.Type.Tardy;
 				} else if (stype.equals(Absence.Type.EarlyCheckOut.getValue())) {
 					type = Absence.Type.EarlyCheckOut;
 				}
 			} else {
 				errors.add("Invalid type.");
 			}
 
 			try {
 				if (type == Absence.Type.Absence) {
 					date = Util.parseDate(req.getParameter("Month"),
 							req.getParameter("Day"), req.getParameter("Year"),
 							"0", "AM", "0", train.getAppDataController().get()
 									.getTimeZone());
 
 				} else {
 					date = Util
 							.parseDate(req.getParameter("Month"),
 									req.getParameter("Day"),
 									req.getParameter("Year"),
 									req.getParameter("Hour"),
 									req.getParameter("AMPM"),
 									req.getParameter("Minute"), train
 											.getAppDataController().get()
 											.getTimeZone());
 				}
 			} catch (IllegalArgumentException e) {
 				validForm = false;
 				errors.add("The date is invalid.");
 			}
 		}
 
 		if (validForm) {
 			// Store our new form to the data store
 			User student = train.getAuthController().getCurrentUser(
 					req.getSession());
 
 			Form form = null;
 			try {
 				form = train.getFormsController().createFormC(student, date,
 						type, reason);
 			} catch (IllegalArgumentException e) {
 				validForm = false;
 				errors.add(e.getMessage());
 			}
 
 			if (form == null) {
 				validForm = false;
				errors.add("Fix any errors above and try to resubmit. If you're still having issues, submit a bug report using the form at the bottom of the page.");
 			}
 		}
 		if (validForm) {
 			String url = getIndexURL() + "?success_message="
 					+ URLEncoder.encode(SUCCESS_FORMC, "UTF-8");
 			url = resp.encodeRedirectURL(url);
 			resp.sendRedirect(url);
 		} else {
 			// Show form
 			PageBuilder page = new PageBuilder(Page.formc, SERVLET_PATH);
 
 			page.setPageTitle("Form C");
 
 			page.setAttribute("error_messages", errors);
 			page.setAttribute("types", Absence.Type.values());
 			setStartDate(date, page, train.getAppDataController().get()
 					.getTimeZone());
 			page.setAttribute("Reason", reason);
 
 			page.passOffToJsp(req, resp);
 		}
 	}
 
 	private void handleFormD(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		String email = null;
 		int minutes = 0;
 		Date date = null;
 		String details = null;
 
 		DataTrain train = DataTrain.getAndStartTrain();
 
 		// Parse out all data from form and validate
 		boolean validForm = true;
 		List<String> errors = new LinkedList<String>();
 
 		if (!ValidationUtil.isPost(req)) {
 			// This is not a post request to create a new form, no need to
 			// validate
 			validForm = false;
 		} else {
 			// Extract all basic parameters
 			email = req.getParameter("Email");
 			details = req.getParameter("Details");
 
 			try {
 				minutes = Integer.parseInt(req.getParameter("AmountWorked"));
 			} catch (NumberFormatException e) {
 				validForm = false;
 				errors.add("Invalid amount of time worked: " + e.getMessage());
 			}
 
 			try {
 				date = Util.parseDate(req.getParameter("StartMonth"),
 						req.getParameter("StartDay"),
 						req.getParameter("StartYear"), "0", "AM", "0", train
 								.getAppDataController().get().getTimeZone());
 			} catch (IllegalArgumentException e) {
 				validForm = false;
 				errors.add("Invalid Input: The input date is invalid.");
 			}
 		}
 		User student = train.getAuthController().getCurrentUser(
 				req.getSession());
 
 		if (validForm) {
 			// Store our new form to the data store
 
 			Form form = null;
 			try {
 				// hash the id for use in the accepting and declining forms
 				form = train.getFormsController().createFormD(student, email,
 						date, minutes, details);
 			} catch (IllegalArgumentException e) {
 				validForm = false;
 				errors.add(e.getMessage());
 			}
 
 			if (form == null) {
 				validForm = false;
				errors.add("Fix any errors above and try to resubmit. If you're still having issues, submit a bug report using the form at the bottom of the page.");
 			}
 		}
 		if (validForm) {
 			String url = getIndexURL() + "?success_message="
 					+ URLEncoder.encode(SUCCESS_FORMD, "UTF-8");
 			url = resp.encodeRedirectURL(url);
 			resp.sendRedirect(url);
 		} else {
 			// Show form
 			PageBuilder page = new PageBuilder(Page.formd, SERVLET_PATH);
 
 			page.setPageTitle("Form D");
 
 			page.setAttribute("error_messages", errors);
 
 			page.setAttribute("verifiers", train.getAppDataController().get()
 					.getTimeWorkedEmails());
 
 			page.setAttribute("Email", email);
 			page.setAttribute("AmountWorked", minutes);
 			setStartDate(date, page, train.getAppDataController().get()
 					.getTimeZone());
 			page.setAttribute("Details", details);
 
 			page.passOffToJsp(req, resp);
 		}
 	}
 
 	private void setEndDate(Date date, PageBuilder page, TimeZone timezone) {
 		if (date == null)
 			return;
 
 		Calendar c = Calendar.getInstance(timezone);
 		c.setTime(date);
 		page.setAttribute("EndYear", c.get(Calendar.YEAR));
 		page.setAttribute("EndMonth", c.get(Calendar.MONTH));
 		page.setAttribute("EndDay", c.get(Calendar.DATE));
 	}
 
 	private void setStartDate(Date date, PageBuilder page, TimeZone timezone) {
 		if (date == null)
 			return;
 
 		Calendar c = Calendar.getInstance(timezone);
 		c.setTime(date);
 		page.setAttribute("StartYear", c.get(Calendar.YEAR));
 		page.setAttribute("StartMonth", c.get(Calendar.MONTH));
 		page.setAttribute("StartDay", c.get(Calendar.DATE));
 		page.setAttribute("StartHour", c.get(Calendar.HOUR));
 		page.setAttribute("StartMinute", c.get(Calendar.MINUTE));
 		page.setAttribute("StartPeriod", c.get(Calendar.AM_PM));
 	}
 
 	private void showIndex(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 
 		DataTrain train = DataTrain.getAndStartTrain();
 		FormController fc = train.getFormsController();
 
 		PageBuilder page = new PageBuilder(Page.index, SERVLET_PATH);
 
 		// Pass through any success message in the url parameters sent from a
 		// new form being created or deleted
 		page.setAttribute("success_message",
 				req.getParameter("success_message"));
 
 		User currentUser = train.getAuthController().getCurrentUser(
 				req.getSession());
 
 		String removeid = req.getParameter("removeid");
 		if (removeid != null && removeid != "") {
 			try {
 				long id = Long.parseLong(removeid);
 				if (currentUser.getType() == User.Type.Student
 						&& fc.get(id).getStatus() != Form.Status.Pending) {
 					page.setAttribute("error_messages",
 							"Students cannot delete any non-pending form.");
 				} else {
 					if (fc.removeForm(id)) {
 						page.setAttribute("success_message",
 								"Form successfully deleted");
 					} else {
 						page.setAttribute("error_messages",
 								"Form not deleted. If the form was already approved then you can't delete it.");
 					}
 				}
 			} catch (NullPointerException e) {
 				page.setAttribute("error_messages", "The form does not exist.");
 			}
 		}
 
 		// Handle students and director differently
 		List<Form> forms = null;
 		if (getServletUserType() == User.Type.Student)
 			forms = fc.get(currentUser);
 		else if (getServletUserType() == User.Type.Director)
 			forms = fc.getAll();
 		page.setAttribute("forms", forms);
 
 		page.passOffToJsp(req, resp);
 	}
 
 	/**
 	 * This servlet can be used for multiple user types, this grabs the type of
 	 * user this specific servlet instance is for
 	 * 
 	 * @return
 	 */
 	private User.Type getServletUserType() {
 		String value = getInitParameter("userType");
 
 		return User.Type.valueOf(value);
 	}
 
 	/**
 	 * This servlet can be used for multiple user types, this grabs the type of
 	 * user this specific servlet instance can be accessed by
 	 * 
 	 * @return
 	 */
 	private User.Type[] getServletUserTypes() {
 		User.Type userType = getServletUserType();
 
 		// Since a TA is also a student, we also allow them access to their
 		// student forms
 		if (userType == User.Type.Student)
 			return new User.Type[] { User.Type.Student, User.Type.TA };
 		else
 			return new User.Type[] { userType };
 	}
 
 	/**
 	 * Have to use a method since this servlet can be mapped from different
 	 * paths
 	 */
 	private String getIndexURL() {
 		return pageToUrl(Page.index, getInitParameter("path"));
 	}
 
 }
