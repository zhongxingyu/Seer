 package cours.ulaval.glo4003.aspect;
 
 import javax.inject.Inject;
 
 import org.aspectj.lang.JoinPoint;
 import org.aspectj.lang.annotation.After;
 import org.aspectj.lang.annotation.Aspect;
 
 import cours.ulaval.glo4003.domain.Notification;
 import cours.ulaval.glo4003.domain.Schedule;
 import cours.ulaval.glo4003.domain.Section;
 import cours.ulaval.glo4003.domain.User;
 import cours.ulaval.glo4003.domain.repository.ScheduleRepository;
 import cours.ulaval.glo4003.domain.repository.UserRepository;
 
 @Aspect
 public class ModifiedSectionAspect {
 
 	@Inject
 	ScheduleRepository scheduleRepository;
 
 	@Inject
 	UserRepository userRepository;
 
 	@After("execution(* cours.ulaval.glo4003.controller.ScheduleController.updateSection(..))")
 	private void addNotificationOnModify(JoinPoint pjp) throws Exception {
 		String id = pjp.getArgs()[0].toString();
 		String nrc = pjp.getArgs()[1].toString();
 		Schedule schedule = scheduleRepository.findById(id);
 		Section section = schedule.getSections().get(nrc);
 
 		for (String teacherIdul : section.getTeachers()) {
			String path = "/schedulemanager/schedule/editsection/" + id + "/" + schedule.getYear() + "/" + schedule.getSemester().toString() + "/"
					+ nrc + "/calendar";
 			User user = userRepository.findByIdul(teacherIdul);
 			user.addNotification(new Notification(Notification.SECTION_MODIFIED, path));
 
 			userRepository.store(user);
 		}
 	}
 }
