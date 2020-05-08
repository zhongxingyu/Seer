 package server.persistence;
 
 import java.sql.Time;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 
 import server.entities.Day;
 import server.entities.EmailAddress;
 import server.entities.Form;
 import server.entities.Lesson;
 import server.entities.Login;
 import server.entities.Replacement;
 import server.entities.Room;
 import server.entities.Subject;
 import server.entities.Teacher;
 import server.entities.TimetableLesson;
 import server.operations.PasswordEncryptor;
 
 /**
  * Contains methods for creating data.
  * 
  * @author dennis.markmann
  * @since JDK.1.7.0_25
  * @version 1.0
  */
 
 public class DataCreationHelper {
 
 	private final EntityManager em;
 
 	public DataCreationHelper(final EntityManager em) {
 		this.em = em;
 	}
 
 	Teacher createTeacher(final String firstName, final String lastName, final String shortName) {
 		final Teacher teacher = new Teacher();
 		teacher.setFirstname(firstName);
 		teacher.setName(lastName);
 		teacher.setShortName(shortName);
 		this.em.persist(teacher);
 		return teacher;
 	}
 
 	public Form createForm(final String description, final Teacher teacher) {
 		final Form form = new Form();
 		form.setDescription(description);
 		form.setTeacher(teacher);
 		this.em.persist(form);
 		return form;
 	}
 
 	Room createRoom(final String description) {
 		final Room room = new Room();
 		room.setDescription(description);
 		this.em.persist(room);
 		return room;
 	}
 
 	Lesson createLesson(final String timeFrom, final String timeTo) {
 		final Lesson lesson = new Lesson();
 		lesson.setTimeFrom(Time.valueOf(timeFrom));
 		lesson.setTimeTo(Time.valueOf(timeTo));
 		this.em.persist(lesson);
 		return lesson;
 	}
 
 	Subject createSubject(final String description, final String shortName) {
 		final Subject subject = new Subject();
 		subject.setDescription(description);
 		subject.setShortName(shortName);
 		this.em.persist(subject);
 		return subject;
 	}
 
 	Day createDay(final String description) {
 		final Day day = new Day();
 		day.setDescription(description);
 		this.em.persist(day);
 		return day;
 	}
 
 	TimetableLesson createTimeTableSession(final Form form, final Teacher teacher, final Room room, final Lesson lesson, final Day day,
 			final Subject subject, final List<TimetableLesson> timetableLessons) {
 
 		final TimetableLesson timetableLesson = new TimetableLesson();
 		timetableLesson.setForm(form);
 		timetableLesson.setTeacher(teacher);
 		timetableLesson.setRoom(room);
 		timetableLesson.setLesson(lesson);
 		timetableLesson.setDay(day);
 		timetableLesson.setSubject(subject);
 		timetableLessons.add(timetableLesson);
 		this.em.persist(timetableLesson);
 		return timetableLesson;
 	}
 
 	EmailAddress createEmailAddress(final String eMailAddress) {
 		final EmailAddress email = new EmailAddress();
 		email.setEMailAddress(eMailAddress);
 		this.em.persist(email);
 		return email;
 	}
 
 	void createLogin(final String userName, final String eMailAddress) {
 		final Login login = new Login();
 		login.setUser(userName);
 		login.setEmail(this.createEmailAddress(eMailAddress));
 		login.setPassword(new PasswordEncryptor().encryptPassword("test"));
 		this.em.persist(login);
 	}
 
	void createReplacement(final Date date, final Room room, final Teacher teacher, final Form form, final Subject subject, final String note,
			final TimetableLesson timetableLesson) {
 		final Replacement replacement = new Replacement();
 		replacement.setDate(date);
 		replacement.setRoom(room);
 		replacement.setTeacher(teacher);
 		replacement.setForm(form);
		replacement.setSubject(subject);
		replacement.setNote(note);

 		this.em.persist(replacement);
 		timetableLesson.setReplacement(replacement);
 		this.em.persist(timetableLesson);
 	}
 }
