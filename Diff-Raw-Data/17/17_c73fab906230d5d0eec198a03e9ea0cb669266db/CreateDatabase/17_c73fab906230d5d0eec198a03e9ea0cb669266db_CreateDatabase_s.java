 package server.persistence;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.Query;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import server.entities.Day;
 import server.entities.Form;
 import server.entities.Lesson;
 import server.entities.Room;
 import server.entities.School;
 import server.entities.Subject;
 import server.entities.Teacher;
 import server.entities.Timetable;
 import server.entities.TimetableLesson;
 
 /**
  * database table creation and initial dataCreation.
  * 
  * @author dennis.markmann
  * @since JDK.1.7.0_25
  * @version 1.0
  */
 
 public class CreateDatabase {
 
 	private EntityManager em;
 	private DataCreationHelper helper;
 
 	@Before
 	public void init() {
 		this.em = HibernateUtil.getEntityManager();
 		this.helper = new DataCreationHelper(this.em);
 	}
 
 	@Test
 	public final void testDataCreation() {
 
 		this.em.getTransaction().begin();
 
 		// To stop creating duplicate data
 		@SuppressWarnings("unchecked")
 		final List<Teacher> teacher = this.em
 				.createNativeQuery("SELECT * FROM lehrer WHERE Vorname = 'Volker' and Name = 'Luehrssen'", Teacher.class).getResultList();
 		if (teacher.size() != 0) {
 			this.em.getTransaction().commit();
 			return;
 		}
 
 		final School school = new School();
 		school.setImage("/home.jpg");
 		school.setText("Staatliche Gewerbeschule G18 <br>" + "Informations- und Elektrotechnik, <br>" + "Chemie- und Automatisierungstechnik <br>"
 				+ "Dratelnstraße 26 21109 Hamburg <br>" + "<br>" + "Telefon: +49 40 428 79 - 02 <br>" + "Telefax: +49 40 428 79 - 450 <br>"
 				+ "E-Mail: g18(at)hibb.hamburg.de <br>" + "<br>"
 				+ "++++ Neuer Rekord: Die IT1a hat den besten Notenschnitt, den jemals eine Klasse erreicht hat ++++<br>"
 				+ "++++ Zeugnisse sind fertig ++++<br>" + "++++ Studie: Killerspiele fördern Aufmerksamkeit und Reaktionsvermögen ++++<br>"
 				+ "++++ Folgende Lehrer sind krank: ... ++++<br>" + "++++ Stundenausfall für die Klassen IT3C, CH3M, IN5T ++++<br>"
 				+ "++++ Studie: Lernortverlegung nach Zuhause führt zu effektiverem Lernverhalten ++++<br>");
 		this.em.persist(school);
 
 		final Teacher luehrssen = this.helper.createTeacher("Volker", "Luehrssen", "Lue");
 		final Teacher albers = this.helper.createTeacher("Kirsten", "Albers", "Al");
 		final Teacher herrmann = this.helper.createTeacher("Werner", "Herrmann", "Hr");
 		final Teacher bastians = this.helper.createTeacher("Thomas", "Bastians", "Ba");
 		final Teacher giera = this.helper.createTeacher("Heike", "Giera", "Ga");
 		final Teacher wehmeyer = this.helper.createTeacher("Markus", "Wehmeyer", "Wm");
 		final Teacher burg = this.helper.createTeacher("Andreas", "Burg", "Bu");
 
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
 
 		this.helper.createTimeTableSession(it1a, luehrssen, raum53, lesson3, montag, ae, timeTableLessons_it1a);
 		this.helper.createTimeTableSession(it1a, luehrssen, raum53, lesson4, montag, ae, timeTableLessons_it1a);
 		this.helper.createTimeTableSession(it1a, albers, raum32, lesson5, montag, suk, timeTableLessons_it1a);
 		this.helper.createTimeTableSession(it1a, albers, raum32, lesson6, montag, suk, timeTableLessons_it1a);
 		this.helper.createTimeTableSession(it1a, herrmann, raum32, lesson7, montag, itSyst, timeTableLessons_it1a);
 		// Lessons for TuesDay
 		this.helper.createTimeTableSession(it1a, bastians, raum81, lesson3, dienstag, wug, timeTableLessons_it1a);
 		this.helper.createTimeTableSession(it1a, bastians, raum81, lesson4, dienstag, wug, timeTableLessons_it1a);
 		this.helper.createTimeTableSession(it1a, luehrssen, raum53, lesson5, dienstag, ae, timeTableLessons_it1a);
 		this.helper.createTimeTableSession(it1a, luehrssen, raum53, lesson6, dienstag, ae, timeTableLessons_it1a);
 		// Lessons for Wednesday
 		this.helper.createTimeTableSession(it1a, giera, raum82, lesson3, mittwoch, orgaGp, timeTableLessons_it1a);
 		this.helper.createTimeTableSession(it1a, giera, raum82, lesson4, mittwoch, orgaGp, timeTableLessons_it1a);
 		this.helper.createTimeTableSession(it1a, herrmann, raum53, lesson5, mittwoch, itSyst, timeTableLessons_it1a);
 		this.helper.createTimeTableSession(it1a, herrmann, raum53, lesson6, mittwoch, itSyst, timeTableLessons_it1a);
 		this.helper.createTimeTableSession(it1a, burg, raum114, lesson7, mittwoch, fe, timeTableLessons_it1a);
 		this.helper.createTimeTableSession(it1a, burg, raum114, lesson8, mittwoch, fe, timeTableLessons_it1a);
 		// Lessons for Thursday
 		this.helper.createTimeTableSession(it1a, luehrssen, raum53, lesson1, donnerstag, ae, timeTableLessons_it1a);
 		this.helper.createTimeTableSession(it1a, luehrssen, raum53, lesson2, donnerstag, ae, timeTableLessons_it1a);
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
 		timetable_it1a.setForm(it1a);
 		this.em.persist(timetable_it1a);
 
 		// it1b
 		// Lessons for Monday
 		final List<TimetableLesson> timeTableLessons_it1b = new ArrayList<TimetableLesson>();
 
 		this.helper.createTimeTableSession(it1b, albers, raum32, lesson1, montag, suk, timeTableLessons_it1b);
 		this.helper.createTimeTableSession(it1b, albers, raum32, lesson2, montag, suk, timeTableLessons_it1b);
 		this.helper.createTimeTableSession(it1b, bastians, raum53, lesson5, montag, wug, timeTableLessons_it1b);
 		this.helper.createTimeTableSession(it1b, bastians, raum53, lesson6, montag, wug, timeTableLessons_it1b);
 		this.helper.createTimeTableSession(it1b, luehrssen, raum53, lesson7, montag, ae, timeTableLessons_it1b);
 		// Lessons for TuesDay
 		this.helper.createTimeTableSession(it1b, luehrssen, raum53, lesson1, dienstag, ae, timeTableLessons_it1b);
 		this.helper.createTimeTableSession(it1b, luehrssen, raum53, lesson2, dienstag, ae, timeTableLessons_it1b);
 		this.helper.createTimeTableSession(it1b, bastians, raum81, lesson3, dienstag, wug, timeTableLessons_it1b);
 		this.helper.createTimeTableSession(it1b, bastians, raum81, lesson4, dienstag, wug, timeTableLessons_it1b);
 		// Lessons for Wednesday
 		this.helper.createTimeTableSession(it1b, herrmann, raum114, lesson4, mittwoch, itSyst, timeTableLessons_it1b);
 		this.helper.createTimeTableSession(it1b, herrmann, raum114, lesson5, mittwoch, itSyst, timeTableLessons_it1b);
 		this.helper.createTimeTableSession(it1b, burg, raum82, lesson6, mittwoch, fe, timeTableLessons_it1b);
 		this.helper.createTimeTableSession(it1b, burg, raum82, lesson7, mittwoch, fe, timeTableLessons_it1b);
 		this.helper.createTimeTableSession(it1b, giera, raum53, lesson8, mittwoch, orgaGp, timeTableLessons_it1b);
 		this.helper.createTimeTableSession(it1b, giera, raum53, lesson9, mittwoch, orgaGp, timeTableLessons_it1b);
 		this.helper.createTimeTableSession(it1b, giera, raum53, lesson10, mittwoch, orgaGp, timeTableLessons_it1b);

 		// Lessons for Thursday
 		this.helper.createTimeTableSession(it1b, giera, raum81, lesson1, donnerstag, orgaGp, timeTableLessons_it1b);
 		this.helper.createTimeTableSession(it1b, giera, raum81, lesson2, donnerstag, orgaGp, timeTableLessons_it1b);
 		this.helper.createTimeTableSession(it1b, luehrssen, raum53, lesson3, donnerstag, ae, timeTableLessons_it1b);
 		this.helper.createTimeTableSession(it1b, luehrssen, raum53, lesson4, donnerstag, ae, timeTableLessons_it1b);
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
 		timetable_it1b.setLessons(timeTableLessons_it1b);
 		timetable_it1b.setForm(it1b);
 		this.em.persist(timetable_it1b);
 
 		this.helper.createLogin("Volker.Luehrssen", "VolkerLuehrssen@localhost");
 		this.helper.createLogin("Kirsten.Albers", "KirstenAlbers@localhost");
 		this.helper.createLogin("Werner.Herrmann", "WernerHerrmann@localhost");
 		this.helper.createLogin("Thomas.Bastians", "ThomasBastians@localhost");
 		this.helper.createLogin("Heike.Giera", "HeikeGiera@localhost");
 		this.helper.createLogin("Markus.Wehmeyer", "MarkusWehmeyer@localhost");
 		this.helper.createLogin("Andreas.Burg", "AndreasBurg@localhost");
 		this.helper.createLogin("test", "test@localhost");
 
		this.helper.createReplacement("2013-W36", montag, raum53, luehrssen,luehrssen, it1a, pro, lesson2, "Lehrer erkrankt", 0);
 		this.helper.createReplacement("2013-W36", montag, raum53, herrmann, wehmeyer, it1a, ae, lesson3, "Zeugniskonferenz", 0);
 		this.helper.createReplacement("2013-W36", montag, raum82, herrmann, wehmeyer, it1b, ae, lesson4, "Veranstaltung", 0);
		this.helper.createReplacement("2013-W36", donnerstag, raum53, luehrssen,luehrssen, it1b, ae, lesson3, "Fällt aus", 1);
		this.helper.createReplacement("2013-W36", donnerstag, raum53, luehrssen,luehrssen, it1b, ae, lesson4, "Fällt aus", 1);
 
 		this.helper.createNewsletter(it1a, "test@localhost");
 		this.helper.createNewsletter(it1a, "test3@localhost");
 
 		this.em.getTransaction().commit();
 
 		this.createTimetables();
 
 	}
 
 	private void createTimetables() {
 		this.em.getTransaction().begin();
 		this.createTeacherTimetables();
 		this.createRoomTimetables();
 		this.em.getTransaction().commit();
 
 	}
 
 	@SuppressWarnings("unchecked")
 	private void createRoomTimetables() {
 		final List<Room> rooms = this.em.createQuery("from Room", Room.class).getResultList();
 
 		for (final Room room : rooms) {
 			final String lessonSql = "select * from klasse_tag_stunde where idRaum = '" + room.getId() + "'";
 			final Query lessonQuery = HibernateUtil.getEntityManager().createNativeQuery(lessonSql, TimetableLesson.class);
 			final Timetable timetable = new Timetable();
 			timetable.setLessons(lessonQuery.getResultList());
 
 			final String roomSql = "select * from raum where idRaum = '" + room.getId() + "'";
 			final Query roomQuery = HibernateUtil.getEntityManager().createNativeQuery(roomSql, Room.class);
 			timetable.setRoom((Room) roomQuery.getResultList().get(0));
 
 			this.em.persist(timetable);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	private void createTeacherTimetables() {
 		final List<Teacher> teachers = this.em.createQuery("from Teacher", Teacher.class).getResultList();
 
 		for (final Teacher teacher : teachers) {
 			final String lessonSql = "select * from klasse_tag_stunde where idLehrer = '" + teacher.getId() + "'";
 			final Query lessonQuery = HibernateUtil.getEntityManager().createNativeQuery(lessonSql, TimetableLesson.class);
 			final Timetable timetable = new Timetable();
 			timetable.setLessons(lessonQuery.getResultList());
 
 			final String teacherSql = "select * from lehrer where idLehrer = '" + teacher.getId() + "'";
 			final Query teacherQuery = HibernateUtil.getEntityManager().createNativeQuery(teacherSql, Teacher.class);
 			timetable.setTeacher((Teacher) teacherQuery.getResultList().get(0));
 
 			this.em.persist(timetable);
 		}
 	}
 }
