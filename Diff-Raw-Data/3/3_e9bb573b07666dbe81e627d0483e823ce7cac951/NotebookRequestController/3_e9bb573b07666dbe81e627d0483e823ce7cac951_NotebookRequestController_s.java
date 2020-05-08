 /**
  * NewRequestController.java Created by jhoppmann on Mar 14, 2013
  */
 package com.dhbw_db.control;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import com.dhbw_db.model.beans.Notebook.NotebookCategory;
 import com.dhbw_db.model.beans.User;
 import com.dhbw_db.model.io.database.DataAccess;
 import com.dhbw_db.model.io.logging.LoggingService;
 import com.dhbw_db.model.io.logging.LoggingService.LogLevel;
 import com.dhbw_db.model.request.Request;
 import com.dhbw_db.view.PostButtonPage;
 import com.dhbw_db.view.student.NotebookRequest;
 import com.vaadin.data.Property.ValueChangeEvent;
 import com.vaadin.data.Property.ValueChangeListener;
 import com.vaadin.ui.Button.ClickEvent;
 import com.vaadin.ui.Button.ClickListener;
 
 /**
  * @author jhoppmann
  * @version 0.1
  * @since 0.1
  */
 public class NotebookRequestController implements ClickListener,
 		ValueChangeListener {
 
 	private static final long serialVersionUID = 1L;
 
 	private DataAccess dao;
 
 	private NotebookRequest controlledView;
 
 	public NotebookRequestController(NotebookRequest controlledView) {
 		this.dao = MainController.get()
 									.getDataAccess();
 		this.controlledView = controlledView;
 	}
 
 	/**
 	 * This will return the notebook categories.
 	 * 
 	 * @return A List with NotebookCategories.
 	 */
 	public Map<NotebookCategory, Integer> getNotebooks() {
 		return dao.getNotebookCount();
 	}
 
 	/**
 	 * This will get a Map of the available operating systems.
 	 * 
 	 * @return A Map with a string representations of operating systems as keys
 	 *         and their unique IDs as values
 	 */
 	public Map<Integer, String> getOperatingSystems() {
 		return dao.getOSs();
 	}
 
 	/**
 	 * This will get a Map of the available approvers.
 	 * 
 	 * @return A Map with a string representations of approvers as keys and
 	 *         their unique IDs as values
 	 */
 	public List<User> getApprovers() {
 		return dao.getLecturers();
 	}
 
 	@Override
 	public void buttonClick(ClickEvent event) {
 		if (event.getButton()
 					.getCaption()
 					.equals("Zurücksetzen")) {
 			controlledView.reset();
 		} else if (event.getButton()
 						.getCaption()
 						.equals("Beantragen")) {
 			Request r = controlledView.getRequest();
 
 			if (r == null) {
 				MainController.get()
 								.print("Nicht alle Pflichtfelder gefüllt");
 				return;
 			}
 
 			String headline = "Antrag erfolgreich angelegt!";
 			String subline = "Sie können nun über die Navigationsleiste zur "
 					+ "Startseite zurückkehren.";
 			try {
				dao.insertRequest(r);
 				LoggingService.getInstance()
 								.log(	"Request successfully created and "
 												+ "persisted",
 										LogLevel.INFO);
 				r.start();
 			} catch (Exception e) {
 				LoggingService.getInstance()
 								.log(e.getMessage(), LogLevel.ERROR);
 				e.printStackTrace();
 				headline = "Antrag nicht angelegt!";
 				subline = "Der Antrag konnte leider nicht angelegt werden. "
 						+ "Versuchen Sie es erneut. Sollte das Problem "
 						+ "weiterhin bestehen, kontaktieren Sie einen "
 						+ "Administrator.";
 			}
 			MainController.get()
 							.changeView(new PostButtonPage(	headline,
 															subline));
 
 		}
 
 	}
 
 	@Override
 	public void valueChange(ValueChangeEvent event) {
 		// this method is for setting right time bounds
 
 		Date start = controlledView.getStartDate();
 		Date end = controlledView.getEndDate();
 
 		if (end == null && start == null) {
 			// leave this method if no date is set.
 			return;
 		}
 
 		// assure end date after start date
 		if (end == null || !start.before(end)) {
 			Calendar cal = Calendar.getInstance();
 			cal.setTime(start);
 			cal.add(Calendar.DATE, 1);
 			Date later = cal.getTime();
 
 			controlledView.setEndDate(later);
 		}
 
 		NotebookCategory nbc = controlledView.getCategory();
 
 		if (nbc == null || end == null) {
 			return;
 		}
 		// assure the date doesn't violate the category lengths
 		Calendar cal = Calendar.getInstance();
 		cal.setTime(start);
 		cal.add(Calendar.WEEK_OF_YEAR, 1);
 		Date oneWeek = cal.getTime();
 
 		cal.setTime(start);
 		cal.add(Calendar.MONTH, 1);
 		Date oneMonth = cal.getTime();
 
 		cal.setTime(start);
 		cal.add(Calendar.MONTH, 3);
 		Date threeMonths = cal.getTime();
 
 		if (nbc == NotebookCategory.SHORT && end.after(oneWeek)) {
 			controlledView.setEndDate(oneWeek);
 		} else if (nbc == NotebookCategory.MEDIUM && end.after(oneMonth)) {
 			controlledView.setEndDate(oneMonth);
 		} else if (end.after(threeMonths)) {
 			controlledView.setEndDate(threeMonths);
 		}
 
 	}
 }
