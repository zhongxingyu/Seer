 package cours.ulaval.glo4003.controller.model;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import cours.ulaval.glo4003.controller.model.utils.TimeSlotComparator;
 import cours.ulaval.glo4003.domain.Schedule;
 import cours.ulaval.glo4003.domain.Section;
 import cours.ulaval.glo4003.domain.Time;
 import cours.ulaval.glo4003.domain.TimeSlot;
 import cours.ulaval.glo4003.domain.TimeSlot.DayOfWeek;
 import cours.ulaval.glo4003.domain.conflictdetection.conflict.Conflict;
 import cours.ulaval.glo4003.domain.conflictdetection.conflict.UnavailableTeacherConflict;
 
 public class CalendarModel {
 
 	private ScheduleInformationModel scheduleInfo;
 
 	private List<CourseSlotModel> monday = new ArrayList<CourseSlotModel>();
 	private List<CourseSlotModel> tuesday = new ArrayList<CourseSlotModel>();
 	private List<CourseSlotModel> wednesday = new ArrayList<CourseSlotModel>();
 	private List<CourseSlotModel> thursday = new ArrayList<CourseSlotModel>();
 	private List<CourseSlotModel> friday = new ArrayList<CourseSlotModel>();
 
 	public CalendarModel(Schedule schedule) {
 		scheduleInfo = new ScheduleInformationModel(schedule);
 
 		for (Section section : schedule.getSectionsList()) {
 			for (TimeSlot timeSlot : section.getCoursesAndLabTimeSlots()) {
 				addToList(section, timeSlot, false);
 			}
 			if (section.getLabTimeSlot() != null) {
 				addToList(section, section.getLabTimeSlot(), true);
 			}
 		}
 
 		sortCoursesByTime();
 		
		//Temporaire pour tests uniquement
		schedule.add(new UnavailableTeacherConflict("90111", "Thierry Eude", new TimeSlot(new Time(8,30), 3, DayOfWeek.MONDAY)));
		
 		associateConflictsToACourseSlot(schedule.getConflicts());
 	}
 
 	private void associateConflictsToACourseSlot(List<Conflict> conflicts) {
 
 		for (Conflict conflict : conflicts) {
 			setConflictToACourseSlot(conflict, conflict.getFirstNrc(), conflict.getFirstTimeSlot());
 
 			TimeSlot secondTimeSlot = conflict.getSecondTimeSlot();
 
 			if (secondTimeSlot != null) {
 				setConflictToACourseSlot(conflict, conflict.getSecondNrc(), conflict.getSecondTimeSlot());
 			}
 		}
 	}
 
 	private void setConflictToACourseSlot(Conflict conflict, String nrc, TimeSlot time) {
 		CourseSlotModel courseSlot = findCourseSlotAccordingToDay(time, nrc);
 		if (courseSlot != null) {
 			courseSlot.addConflict(conflict);
 		}
 	}
 
 	private CourseSlotModel findCourseSlotAccordingToDay(TimeSlot slot, String nrc) {
 		CourseSlotModel model = null;
 		switch (slot.getDayOfWeek()) {
 		case MONDAY:
 			model = findCourseSlotAccordingToHours(slot, nrc, monday);
 			break;
 		case TUESDAY:
 			model = findCourseSlotAccordingToHours(slot, nrc, tuesday);
 			break;
 		case WEDNESDAY:
 			model = findCourseSlotAccordingToHours(slot, nrc, wednesday);
 			break;
 		case THURSDAY:
 			model = findCourseSlotAccordingToHours(slot, nrc, thursday);
 			break;
 		case FRIDAY:
 			model = findCourseSlotAccordingToHours(slot, nrc, friday);
 			break;
 		default:
 			break;
 		}
 
 		return model;
 	}
 
 	private CourseSlotModel findCourseSlotAccordingToHours(TimeSlot slot, String nrc, List<CourseSlotModel> models) {
 		CourseSlotModel modelToReturn = null;
 		for (CourseSlotModel model : models) {
 			if (isSameCourse(slot, nrc, model)) {
 				modelToReturn = model;
 				break;
 			}
 		}
 
 		return modelToReturn;
 	}
 
 	private boolean isSameCourse(TimeSlot time, String nrc, CourseSlotModel model) {
 
 		if (!model.getNrc().equals(nrc)) {
 			return false;
 		}
 		if (!model.getTimeSlotStart().equals(time.getStartTime().toString())) {
 			return false;
 		}
 		if (!model.getTimeSlotEnd().equals(time.getEndTime().toString())) {
 			return false;
 		}
 
 		return true;
 	}
 
 	private void sortCoursesByTime() {
 		Collections.sort(monday, new TimeSlotComparator());
 		Collections.sort(tuesday, new TimeSlotComparator());
 		Collections.sort(wednesday, new TimeSlotComparator());
 		Collections.sort(thursday, new TimeSlotComparator());
 		Collections.sort(friday, new TimeSlotComparator());
 	}
 
 	private void addToList(Section section, TimeSlot timeSlot, boolean isLab) {
 		switch (timeSlot.getDayOfWeek()) {
 		case MONDAY:
 			monday.add(new CourseSlotModel(section, timeSlot, isLab));
 			break;
 		case TUESDAY:
 			tuesday.add(new CourseSlotModel(section, timeSlot, isLab));
 			break;
 		case WEDNESDAY:
 			wednesday.add(new CourseSlotModel(section, timeSlot, isLab));
 			break;
 		case THURSDAY:
 			thursday.add(new CourseSlotModel(section, timeSlot, isLab));
 			break;
 		case FRIDAY:
 			friday.add(new CourseSlotModel(section, timeSlot, isLab));
 			break;
 		default:
 			break;
 		}
 	}
 
 	public ScheduleInformationModel getScheduleInfo() {
 		return scheduleInfo;
 	}
 
 	public void setScheduleInfo(ScheduleInformationModel scheduleInfo) {
 		this.scheduleInfo = scheduleInfo;
 	}
 
 	public List<CourseSlotModel> getMonday() {
 		return monday;
 	}
 
 	public void setMonday(List<CourseSlotModel> monday) {
 		this.monday = monday;
 	}
 
 	public List<CourseSlotModel> getTuesday() {
 		return tuesday;
 	}
 
 	public void setTuesday(List<CourseSlotModel> tuesday) {
 		this.tuesday = tuesday;
 	}
 
 	public List<CourseSlotModel> getWednesday() {
 		return wednesday;
 	}
 
 	public void setWednesday(List<CourseSlotModel> wednesday) {
 		this.wednesday = wednesday;
 	}
 
 	public List<CourseSlotModel> getThursday() {
 		return thursday;
 	}
 
 	public void setThursday(List<CourseSlotModel> thursday) {
 		this.thursday = thursday;
 	}
 
 	public List<CourseSlotModel> getFriday() {
 		return friday;
 	}
 
 	public void setFriday(List<CourseSlotModel> friday) {
 		this.friday = friday;
 	}
 
 }
