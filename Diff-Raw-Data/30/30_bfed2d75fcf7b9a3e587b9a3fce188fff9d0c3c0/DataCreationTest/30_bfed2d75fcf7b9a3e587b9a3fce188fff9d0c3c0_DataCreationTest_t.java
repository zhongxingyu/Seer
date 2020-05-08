 package server.persistence;
 
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import server.entities.Day;
 import server.entities.Form;
 import server.entities.Lesson;
 import server.entities.Room;
 import server.entities.Subject;
 import server.entities.Teacher;
 import server.entities.Timetable;
 import server.entities.TimetableLesson;
 
 /**
  * Test for the initial dataCreation.
  * 
  * @author dennis.markmann
  * @since JDK.1.7.0_25
  * @version 1.0
  */
 
 public class DataCreationTest {
 
 	private EntityManager em;
 	private DataCreationHelper helper;
 
 	@Before
 	public void init() {
 		this.em = HibernateUtil.getEntityManager();
 		this.helper = new DataCreationHelper(this.em);
 	}
 
 	@SuppressWarnings("unused")
 	@Test
 	public void testDataCreation() {
 
 		this.em.getTransaction().begin();
 
 		final Teacher lührssen = this.helper.createTeacher("Volker", "Lührssen", "Lü");
 		final Teacher albers = this.helper.createTeacher("Kirsten", "Albers", "Al");
 		final Teacher herrmann = this.helper.createTeacher("Werner", "Herrmann", "Hr");
 		final Teacher bastians = this.helper.createTeacher("Thomas", "Bastians", "Ba");
 		final Teacher giera = this.helper.createTeacher("Heike", "Giera", "Ga");
 		final Teacher wehmeyer = this.helper.createTeacher("Markus", "Wehmeyer", "Wm");
 		final Teacher burg = this.helper.createTeacher("Burg", "Burg", "Bu");
 
 		final Form it1a = this.helper.createForm("it1a", herrmann);
 		final Form it1b = this.helper.createForm("it1b", burg);
 
 		final Room raum32 = this.helper.createRoom("Raum 32");
 		final Room raum53 = this.helper.createRoom("Raum 53");
 		final Room raum81 = this.helper.createRoom("Raum 81");
 		final Room raum82 = this.helper.createRoom("Raum 82");
 		final Room raum114 = this.helper.createRoom("Raum 144");
 
 		final Lesson lesson1 = this.helper.createLesson("07:45:00", "08:30:00");
 		final Lesson lesson2 = this.helper.createLesson("08:30:00", "09:15:00");
 		final Lesson lesson3 = this.helper.createLesson("09:30:00", "10:15:00");
 		final Lesson lesson4 = this.helper.createLesson("10:15:00", "11:00:00");
 		final Lesson lesson5 = this.helper.createLesson("11:30:00", "12:15:00");
 		final Lesson lesson6 = this.helper.createLesson("12:15:00", "13:00:00");
 		final Lesson lesson7 = this.helper.createLesson("13:15:00", "14:00:00");
 		final Lesson lesson8 = this.helper.createLesson("14:00:00", "14:45:00");
 		final Lesson lesson9 = this.helper.createLesson("15:00:00", "15:45:00");
 		final Lesson lesson10 = this.helper.createLesson("15:45:00", "16:30:00");
 
 		final Subject ae = this.helper.createSubject("Anwendungsentwicklung", "AE");
 		final Subject suk = this.helper.createSubject("Sprache und Kommunikation", "SuK");
 		final Subject itSyst = this.helper.createSubject("IT-Systeme", "IT-Syst");
 		final Subject wug = this.helper.createSubject("Wirtschaft und Gesellschaft", "WuG");
 		final Subject orgaGp = this.helper.createSubject("Organisation und Geschäftsprozesse", "OrgaGp");
 		final Subject fe = this.helper.createSubject("Fachenglisch", "FE");
 		final Subject pro = this.helper.createSubject("Projekt", "Pro");
 
 		final Day montag = this.helper.createDay("Montag");
 		final Day dienstag = this.helper.createDay("Diestag");
 		final Day mittwoch = this.helper.createDay("Mittwoch");
 		final Day donnerstag = this.helper.createDay("Donnerstag");
 		final Day freitag = this.helper.createDay("Freitag");
 
 		// it1a
 		// Lessons for Monday
 		final List<TimetableLesson> timeTableLessons_it1a = new ArrayList<TimetableLesson>();
 
 		final TimetableLesson timetableLesson = this.helper
 				.createTimeTableSession(it1a, lührssen, raum53, lesson3, montag, ae, timeTableLessons_it1a);
 		final TimetableLesson timetableLesson2 = this.helper.createTimeTableSession(it1a, lührssen, raum53, lesson4, montag, ae,
 				timeTableLessons_it1a);
 		final TimetableLesson timetableLesson3 = this.helper
 				.createTimeTableSession(it1a, albers, raum32, lesson5, montag, suk, timeTableLessons_it1a);
 		this.helper.createTimeTableSession(it1a, albers, raum32, lesson6, montag, suk, timeTableLessons_it1a);
 		this.helper.createTimeTableSession(it1a, herrmann, raum32, lesson7, montag, itSyst, timeTableLessons_it1a);
 		// Lessons for TuesDay
 		this.helper.createTimeTableSession(it1a, bastians, raum81, lesson3, dienstag, wug, timeTableLessons_it1a);
 		this.helper.createTimeTableSession(it1a, bastians, raum81, lesson4, dienstag, wug, timeTableLessons_it1a);
 		this.helper.createTimeTableSession(it1a, lührssen, raum53, lesson5, dienstag, ae, timeTableLessons_it1a);
 		this.helper.createTimeTableSession(it1a, lührssen, raum53, lesson6, dienstag, ae, timeTableLessons_it1a);
 		// Lessons for Wednesday
 		this.helper.createTimeTableSession(it1a, giera, raum82, lesson3, mittwoch, orgaGp, timeTableLessons_it1a);
 		this.helper.createTimeTableSession(it1a, giera, raum82, lesson4, mittwoch, orgaGp, timeTableLessons_it1a);
 		this.helper.createTimeTableSession(it1a, herrmann, raum53, lesson5, mittwoch, itSyst, timeTableLessons_it1a);
 		this.helper.createTimeTableSession(it1a, herrmann, raum53, lesson6, mittwoch, itSyst, timeTableLessons_it1a);
 		this.helper.createTimeTableSession(it1a, burg, raum114, lesson7, mittwoch, fe, timeTableLessons_it1a);
 		this.helper.createTimeTableSession(it1a, burg, raum114, lesson8, mittwoch, fe, timeTableLessons_it1a);
 		// Lessons for Thursday
 		this.helper.createTimeTableSession(it1a, lührssen, raum53, lesson1, donnerstag, ae, timeTableLessons_it1a);
 		this.helper.createTimeTableSession(it1a, lührssen, raum53, lesson2, donnerstag, ae, timeTableLessons_it1a);
 		this.helper.createTimeTableSession(it1a, giera, raum81, lesson3, donnerstag, orgaGp, timeTableLessons_it1a);
 		this.helper.createTimeTableSession(it1a, giera, raum81, lesson4, donnerstag, orgaGp, timeTableLessons_it1a);
 		this.helper.createTimeTableSession(it1a, wehmeyer, raum53, lesson5, donnerstag, pro, timeTableLessons_it1a);
 		this.helper.createTimeTableSession(it1a, wehmeyer, raum53, lesson6, donnerstag, pro, timeTableLessons_it1a);
 		this.helper.createTimeTableSession(it1a, herrmann, raum53, lesson7, donnerstag, pro, timeTableLessons_it1a);
 		this.helper.createTimeTableSession(it1a, herrmann, raum53, lesson8, donnerstag, pro, timeTableLessons_it1a);
 		// Lessons for Friday
 		this.helper.createTimeTableSession(it1a, wehmeyer, raum53, lesson1, freitag, pro, timeTableLessons_it1a);
 		this.helper.createTimeTableSession(it1a, wehmeyer, raum53, lesson2, freitag, pro, timeTableLessons_it1a);
 		this.helper.createTimeTableSession(it1a, wehmeyer, raum53, lesson3, freitag, pro, timeTableLessons_it1a);
 		this.helper.createTimeTableSession(it1a, wehmeyer, raum53, lesson4, freitag, pro, timeTableLessons_it1a);
 		this.helper.createTimeTableSession(it1a, wehmeyer, raum53, lesson5, freitag, pro, timeTableLessons_it1a);
 		this.helper.createTimeTableSession(it1a, wehmeyer, raum53, lesson6, freitag, pro, timeTableLessons_it1a);
 
 		final Timetable timetable_it1a = new Timetable();
 		timetable_it1a.setLessons(timeTableLessons_it1a);
 		this.em.persist(timetable_it1a);
 
 		// it1b
 		// Lessons for Monday
 		final List<TimetableLesson> timeTableLessons_it1b = new ArrayList<TimetableLesson>();
 
 		this.helper.createTimeTableSession(it1b, albers, raum32, lesson1, montag, suk, timeTableLessons_it1b);
 		this.helper.createTimeTableSession(it1b, albers, raum32, lesson2, montag, suk, timeTableLessons_it1b);
 		this.helper.createTimeTableSession(it1b, bastians, raum53, lesson5, montag, wug, timeTableLessons_it1b);
 		this.helper.createTimeTableSession(it1b, bastians, raum53, lesson6, montag, wug, timeTableLessons_it1b);
 		this.helper.createTimeTableSession(it1b, lührssen, raum53, lesson7, montag, ae, timeTableLessons_it1b);
 		// Lessons for TuesDay
 		this.helper.createTimeTableSession(it1b, lührssen, raum53, lesson1, dienstag, ae, timeTableLessons_it1b);
 		this.helper.createTimeTableSession(it1b, lührssen, raum53, lesson2, dienstag, ae, timeTableLessons_it1b);
 		this.helper.createTimeTableSession(it1b, bastians, raum81, lesson3, dienstag, wug, timeTableLessons_it1b);
 		this.helper.createTimeTableSession(it1b, bastians, raum81, lesson4, dienstag, wug, timeTableLessons_it1b);
 		// Lessons for Wednesday
 		this.helper.createTimeTableSession(it1b, herrmann, raum114, lesson4, mittwoch, itSyst, timeTableLessons_it1b);
 		this.helper.createTimeTableSession(it1b, herrmann, raum114, lesson5, mittwoch, itSyst, timeTableLessons_it1b);
 		this.helper.createTimeTableSession(it1b, burg, raum82, lesson6, mittwoch, fe, timeTableLessons_it1b);
 		this.helper.createTimeTableSession(it1b, burg, raum82, lesson7, mittwoch, fe, timeTableLessons_it1b);
 		this.helper.createTimeTableSession(it1b, giera, raum53, lesson8, mittwoch, orgaGp, timeTableLessons_it1b);
 		this.helper.createTimeTableSession(it1b, giera, raum53, lesson9, mittwoch, orgaGp, timeTableLessons_it1b);
 		// Lessons for Thursday
 		this.helper.createTimeTableSession(it1b, giera, raum81, lesson1, donnerstag, orgaGp, timeTableLessons_it1b);
 		this.helper.createTimeTableSession(it1b, giera, raum81, lesson2, donnerstag, orgaGp, timeTableLessons_it1b);
 		this.helper.createTimeTableSession(it1b, lührssen, raum53, lesson3, donnerstag, ae, timeTableLessons_it1b);
 		this.helper.createTimeTableSession(it1b, lührssen, raum53, lesson4, donnerstag, ae, timeTableLessons_it1b);
 		this.helper.createTimeTableSession(it1b, herrmann, raum114, lesson5, donnerstag, pro, timeTableLessons_it1b);
 		this.helper.createTimeTableSession(it1b, herrmann, raum114, lesson6, donnerstag, pro, timeTableLessons_it1b);
 		this.helper.createTimeTableSession(it1b, wehmeyer, raum114, lesson7, donnerstag, pro, timeTableLessons_it1b);
 		this.helper.createTimeTableSession(it1b, wehmeyer, raum114, lesson8, donnerstag, pro, timeTableLessons_it1b);
 		// Lessons for Friday
 		this.helper.createTimeTableSession(it1b, burg, raum114, lesson1, freitag, pro, timeTableLessons_it1b);
 		this.helper.createTimeTableSession(it1b, burg, raum114, lesson2, freitag, pro, timeTableLessons_it1b);
 		this.helper.createTimeTableSession(it1b, burg, raum114, lesson3, freitag, pro, timeTableLessons_it1b);
 		this.helper.createTimeTableSession(it1b, burg, raum114, lesson4, freitag, pro, timeTableLessons_it1b);
 		this.helper.createTimeTableSession(it1b, burg, raum114, lesson5, freitag, pro, timeTableLessons_it1b);
 		this.helper.createTimeTableSession(it1b, burg, raum114, lesson6, freitag, pro, timeTableLessons_it1b);
 
 		final Timetable timetable_it1b = new Timetable();
 		timetable_it1b.setLessons(timeTableLessons_it1a);
 		this.em.persist(timetable_it1b);
 
 		this.helper.createLogin("Volker.Lürssen", "Volker.Lürssen@g18.de");
 		this.helper.createLogin("Kirsten.Albers", "Kirsten.Albers@g18.de");
 		this.helper.createLogin("Werner.Herrmann", "Werner.Herrmann@g18.de");
 		this.helper.createLogin("Thomas.Bastians", "Thomas.Bastians@g18.de");
 		this.helper.createLogin("Heike.Giera", "Heike.Giera@g18.de");
 		this.helper.createLogin("Markus.Wehmeyer", "Markus.Wehmeyer@g18.de");
 		this.helper.createLogin("Burg.Burg", "Burg.Burg@g18.de");
 		this.helper.createLogin("test", "test@g18.de");
 
		this.helper.createReplacement(Calendar.getInstance().getTime(), raum53, lührssen, it1a, pro, "Lehrer erkrankt", timetableLesson);
		this.helper.createReplacement(Calendar.getInstance().getTime(), raum53, wehmeyer, it1a, null, null, timetableLesson2);
		this.helper.createReplacement(Calendar.getInstance().getTime(), raum82, wehmeyer, it1b, null, "Lehrer kaputt", timetableLesson3);
 
 		this.em.getTransaction().commit();
 
 		@SuppressWarnings("unchecked")
 		final List<Form> list = this.em.createNativeQuery("select * from Klasse", Form.class).getResultList();
 		assertTrue(list.size() >= 2);
 	}
 
 }
