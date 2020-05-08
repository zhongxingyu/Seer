 package controllers;
 
 import helper.UserHelper;
 
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import models.Course;
 import models.CourseMaterial;
 import models.DateUtility;
 import models.TimeInterval;
 import models.User;
 import models.DateUtility.LearningDayTime;
 import models.timetable.PersonalizedTimetable;
 import models.timetable.ScheduledTimetableEntryList;
 
 import org.joda.time.DateTime;
 
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;

 import play.mvc.With;
 import controllers.subtypes.ServiceSubscriptionPeriod;
 import dto.CourseDTO;
 import dto.CourseMaterialDTO;
 import dto.UserDTO;
 import exception.ExternalSynchronizationFailedException;
 
 /**
  * Main controller of the application. Handles requests to the all content
  * pages.
  * 
  */
 @With(Secure.class)
 public class Application extends BaseController {
 
 	public static final String SETTINGS_PERSONAL_DATA = "personalData";
 	public static final String SETTINGS_PAYMENT = "payment";
 
 	public Application() {
 
 	}
 
 	public static void index() {
 		redirect("Application.timetable");
 	}
 
 	public static void courses() {
 		List<Course> courses = getConnectedUser().courses;
 		List<CourseDTO> coursesList = new ArrayList<CourseDTO>();
 		if (courses != null) {
 			for (Course course : courses) {
 				CourseDTO courseDTO = new CourseDTO(course);
 				coursesList.add(courseDTO);
 			}
 		}
 
 		render("Application/courses.html", coursesList);
 	}
 
 	public static void course(Long id) {
 		Course course = findCourseForUser(id);
 		if (course == null) {
 			redirect("Application.courses");
 		} else {
 			CourseDTO courseDTO = new CourseDTO(course);
 			renderArgs.put("course", courseDTO);
 			render("Application/course-details.html");
 		}
 	}
 
 	public static void courseSettings(Long id) {
 		Course course = findCourseForUser(id);
 		if (course == null) {
 			redirect("Application.courses");
 		} else {
 			CourseDTO courseDTO = new CourseDTO(course);
 			renderArgs.put("course", courseDTO);
 			render("Application/course-settings.html");
 		}
 	}
 
 	public static void settings(String category) {
 		User user = getConnectedUser();
 		UserDTO userDto = new UserDTO(user);
 		renderArgs.put("userSettings", userDto);
 
 		if (SETTINGS_PAYMENT.equals(category)) {
 			render("Application/payment.html");
 		} else {
 			render("Application/personaldata.html");
 		}
 	}
 
 	public static void savePersonalData() {
 		User currentUser = getConnectedUser();
 		String name = params.get("name");
 		String email = params.get("email");
 		boolean sharedTimetable = getBooleanValue(params.get("sharedTimetable"));
 		boolean deadlineEmail = getBooleanValue(params.get("deadlineEmail"));
 		boolean learnSlotEmail = getBooleanValue(params.get("learnSlotEmail"));
 
 		String calendarURL = params.get("calendarURL");
 		String breakDurationValue = params.get("breakDuration");
 		String minSlotDurationValue = params.get("minSlotDuration");
 		String maxSlotDurationValue = params.get("maxSlotDuration");
 
 		String[] daysOfRestValue = params.getAll("daysOfRest");
 		String startTime1Value = params.get("starttime1");
 		String endTime1Value = params.get("endtime1");
 		String startTime2Value = params.get("starttime2");
 		String endTime2Value = params.get("endtime2");
 
 		validation.required(email);
 		validation.email(email);
 
 		float breakDuration = 0;
 		float minSlotDuration = 0;
 		float maxSlotDuration = 0;
 
 		try {
 			breakDuration = Float.parseFloat(breakDurationValue);
 		} catch (NumberFormatException e) {
 			validation.addError("breakDuration", "validation.settings.incorrect.float.value");
 		}
 
 		try {
 			minSlotDuration = Float.parseFloat(minSlotDurationValue);
 		} catch (NumberFormatException e) {
 			validation.addError("minSlotDuration", "validation.settings.incorrect.float.value");
 		}
 
 		try {
 			maxSlotDuration = Float.parseFloat(maxSlotDurationValue);
 		} catch (NumberFormatException e) {
 			validation.addError("maxSlotDuration", "validation.settings.incorrect.float.value");
 		}
 
 		// Checking the user with such an email address does not exist yet
 		if (!validation.hasErrors()) {
 			if (!currentUser.email.equals(email)) {
 				User existingUser = User.find("byEmail", email).first();
 				if (existingUser != null) {
 					validation.addError("email", "validation.user.exists");
 				}
 			}
 		}
 
 		UserDTO userDto = new UserDTO(currentUser);
 		userDto.setName(name);
 		userDto.setEmail(email);
 		userDto.setSharedTimetable(sharedTimetable);
 		userDto.setDeadlineEmail(deadlineEmail);
 		userDto.setLearnSlotEmail(learnSlotEmail);
 
 		userDto.setPrivateCalendarURL(calendarURL);
 		userDto.getPreferences().setBreakDuration(breakDuration);
 		userDto.getPreferences().setMinLearningSlotDuration(minSlotDuration);
 		userDto.getPreferences().setMaxLearningSlotDuration(maxSlotDuration);
 		userDto.setDaysOfRest(daysOfRestValue);
 		LearningDayTime learningDayTime = LearningDayTime.MORNING;
 		if(params.get("preferredLearningDayTime").equals("morning")){
 			learningDayTime = LearningDayTime.MORNING;
 		}
 		else if(params.get("preferredLearningDayTime").equals("afternoon")){
 			learningDayTime = LearningDayTime.AFTERNOON;
 		}
 		else if(params.get("preferredLearningDayTime").equals("evening")){
 			learningDayTime = LearningDayTime.EVENING;
 		}
 		else if(params.get("preferredLearningDayTime").equals("night")){
 			learningDayTime = LearningDayTime.NIGHT;
 		}
 		
 		userDto.getPreferences().setPreferredLearningDayTime(learningDayTime);
 		if (userDto.getPreferences().getTimeIntervalsOfRest() != null) {
 			userDto.getPreferences().getTimeIntervalsOfRest().clear();
 		}
 		userDto.addTimeIntervalOfRest(startTime1Value, endTime1Value);
 		userDto.addTimeIntervalOfRest(startTime2Value, endTime2Value);
 
 		if (validation.hasErrors()) {
 			// Showing the form again if the input was invalid
 			renderArgs.put("userSettings", userDto);
 
 			// Rendering settings page showing the errors
 			render("Application/personaldata.html");
 		} else {
 			currentUser.name = name;
 			currentUser.email = email;
 			currentUser.sharedTimetable = sharedTimetable;
 			currentUser.deadlineEmail = deadlineEmail;
 			currentUser.learnSlotEmail = learnSlotEmail;
 
 			currentUser.privateCalendarURL = userDto.getPrivateCalendarURL();
 			currentUser.preferences.setMaxLearningSlotDuration(maxSlotDuration);
 			currentUser.preferences.setMinLearningSlotDuration(minSlotDuration);
 			currentUser.preferences.setBreakDuration(breakDuration);
 			currentUser.preferences.setPreferredLearningDayTime(learningDayTime);
 
 			if (currentUser.preferences.getTimeIntervalsOfRest() != null) {
 				currentUser.preferences.getTimeIntervalsOfRest().clear();
 			} else {
 				currentUser.preferences.setTimeIntervalsOfRest(new ArrayList<TimeInterval>());
 			}
 
 			if (currentUser.preferences.getDaysOfRest() != null) {
 				currentUser.preferences.getDaysOfRest().clear();
 			} else {
 				currentUser.preferences.setDaysOfRest(new ArrayList<DateUtility.Day>());
 			}
 
 			currentUser.preferences.getTimeIntervalsOfRest().addAll(
 					userDto.getPreferences().getTimeIntervalsOfRest());
 			currentUser.preferences.getDaysOfRest().addAll(userDto.getPreferences().getDaysOfRest());
 
 			currentUser.save();
 
 			// Authenticating the user in the system
 			session.put(Security.SESSION_USERID, email);
 
 			// Redirecting to the settings page
 			redirect("Application.settings", SETTINGS_PERSONAL_DATA);
 		}
 	}
 
 	public static void saveCourseSettings() {
 
 		String idValue = params.get("id");
 		String title = params.get("title");
 		String remarks = params.get("remarks");
 		String difficultyValue = params.get("difficulty");
 		String priorityValue = params.get("priority");
 		String colorValue = params.get("color");
 		String deadlineValue = params.get("deadline");
 		String scriptPagesDoneValue = params.get("scriptPagesDone");
 		String scriptPagesToDoValue = params.get("scriptPagesToDo");
 		String workloadHoursExpectedValue = params.get("workloadHoursExpected");
 		boolean periodicExercisesNeeded = getBooleanValue(params.get("periodicExercisesNeeded"));
 		boolean considerExpectedHours = getBooleanValue(params.get("considerExpectedHours"));
 
 		Course course = null;
 		try {
 			long id = Long.parseLong(idValue);
 			course = findCourseForUser(id);
 			if (course == null) {
 				throw new Exception("No course with such ID exists");
 			}
 		} catch (Exception e) {
 			redirect("Application.courses");
 		}
 
 		validation.required(title);
 		Date deadline = new Date();
 		try {
 			deadline = CourseDTO.DATE_FORMAT.parse(deadlineValue);
 		} catch (Exception e) {
 			validation.addError("deadline", "validation.course.incorrect.deadline.format");
 		}
 
 		double difficulty = getDoubleValue(difficultyValue);
 		double priority = getDoubleValue(priorityValue);
 
 		Color color = Color.GRAY;
 		try {
 			color = Color.decode(colorValue);
 		} catch (Exception e) {
 			validation.addError("color", "validation.course.incorrect.color.value");
 		}
 
 		if (validation.hasErrors()) {
 			CourseMaterialDTO courseMaterial = new CourseMaterialDTO(getIntValue(scriptPagesToDoValue),
 					getIntValue(scriptPagesDoneValue));
 			courseMaterial.setConsiderExpectedHours(considerExpectedHours);
 			courseMaterial.setWorkloadHoursExpected((float) getDoubleValue(workloadHoursExpectedValue));
 
 			CourseDTO courseDto = new CourseDTO(course.getId(), title, remarks, difficulty, priority,
 					new DateTime(deadline.getTime()), courseMaterial, color, periodicExercisesNeeded);
 			renderArgs.put("course", courseDto);
 			render("Application/course-settings.html");
 		} else {
 			CourseMaterial courseMaterial = course.courseMaterial;
 			if (courseMaterial == null) {
 				courseMaterial = new CourseMaterial();
 			}
 
 			courseMaterial.scriptPagesDone = getIntValue(scriptPagesDoneValue);
 			courseMaterial.scriptPagesToDo = getIntValue(scriptPagesToDoValue);
 			courseMaterial.considerExpectedHours = considerExpectedHours;
 			courseMaterial.workloadHoursExpected = getDoubleValue(workloadHoursExpectedValue);
 			courseMaterial.save();
 
 			course.title = title;
 			course.remarks = remarks;
 			course.difficulty = difficulty;
 			course.priority = priority;
 			course.color = color.getRGB();
 			course.deadline = deadline;
 			course.courseMaterial = courseMaterial;
 			course.periodicExercisesNeeded = periodicExercisesNeeded;
 
 			course.save();
 			redirect("Application.courses");
 		}
 	}
 
 	public static void updatePaymentStatus() {
 		String paymentValue = params.get("payment");
 		int payment;
 		try {
 			payment = Integer.parseInt(paymentValue);
 		} catch (NumberFormatException e) {
 			payment = 0;
 		}
 
 		if (payment > 0) {
 			User currentUser = getConnectedUser();
 
 			currentUser.serviceSubscriptionPeriod = UserHelper.monthsToSubscriptionPeriod(payment);
 			if (!currentUser.serviceSubscriptionPeriod.equals(ServiceSubscriptionPeriod.FREE)) {
 				Date expirationDate = currentUser.subscriptionExpires;
 				Calendar calendar = Calendar.getInstance();
 
 				// Using current date if subscription is requested for
 				// the first time or it has already expired. Otherwise,
 				// extend it.
 				if (expirationDate != null && expirationDate.after(calendar.getTime())) {
 					calendar.setTime(expirationDate);
 				}
 				calendar.add(Calendar.MONTH, payment);
 				currentUser.subscriptionExpires = calendar.getTime();
 				currentUser.save();
 			}
 		}
 
 		redirect("Application.settings", SETTINGS_PAYMENT);
 	}
 
 	public static void timetable() {
 		User user = getConnectedUser();
 		List<Course> courses = user.courses;
 		List<CourseDTO> coursesList = new ArrayList<CourseDTO>();
 
 		if (courses != null) {
 			for (Course course : courses) {
 				CourseDTO courseDTO = new CourseDTO(course);
 				coursesList.add(courseDTO);
 			}
 		}
 
 		try {
 			PersonalizedTimetable timetable = new PersonalizedTimetable(user, coursesList);
 			ScheduledTimetableEntryList entryList = timetable.scheduleTimeSlots();
 			render("Application/timetable.html", entryList);
 		} catch (ExternalSynchronizationFailedException e) {
 			renderArgs.put(
 					"error",
 					String.format("Unable to connecto to the external calendar. %s: %s", e.getCause()
 							.getClass().getName(), e.getMessage()));
 			render("Application/timetable-error.html");
 		}
 	}
 
 	public static void statistics() {
 		User user = getConnectedUser();
 		List<Course> courses = user.courses;
 		List<CourseDTO> coursesList = new ArrayList<CourseDTO>();
 
 		if (courses != null) {
 			for (Course course : courses) {
 				CourseDTO courseDTO = new CourseDTO(course);
 				coursesList.add(courseDTO);
 			}
 		}
 		PersonalizedTimetable timetable = new PersonalizedTimetable(user, coursesList);
 		ScheduledTimetableEntryList entryList = timetable.scheduleTimeSlots();
 		render("Application/statistics.html", entryList, coursesList);
 	}
 
 	/**
 	 * Converts a submitted input parameter to a boolean value.
 	 */
 	private static boolean getBooleanValue(String value) {
 		if (value != null && !value.isEmpty()) {
 			return true;
 		}
 
 		return false;
 	}
 
 	/**
 	 * Converts a submitted input parameter to a double value.
 	 */
 	private static double getDoubleValue(String value) {
 		double d = 0;
 		try {
 			d = Double.parseDouble(value);
 		} catch (NumberFormatException e) {
 			d = 0;
 		}
 		return d;
 	}
 
 	/**
 	 * Converts a submitted input parameter to an integer value.
 	 */
 	private static int getIntValue(String value) {
 		int i = 0;
 		try {
 			i = Integer.parseInt(value);
 		} catch (NumberFormatException e) {
 			i = 0;
 		}
 		return i;
 	}
 
 	/**
 	 * Returns a course with the required id belonging to the current user. If
 	 * no course with such id belongs to the current user, a null object is
 	 * returned.
 	 * 
 	 * @param id
 	 *            Course id
 	 * @return Course object
 	 */
 	private static Course findCourseForUser(long id) {
 		Course course = null;
 		List<Course> courses = getConnectedUser().courses;
 		if (courses != null) {
 			for (Course userCourse : courses) {
 				if (userCourse.id == id) {
 					course = userCourse;
 					break;
 				}
 			}
 		}
 		return course;
 	}
 }
