 package at.ac.tuwien.dse.allocator.controller;
 
 import java.net.UnknownHostException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import at.ac.tuwien.dse.core.db.DAOFactory;
 import at.ac.tuwien.dse.core.db.DoctorDAO;
 import at.ac.tuwien.dse.core.db.HospitalDAO;
 import at.ac.tuwien.dse.core.db.HospitalEmployeeDAO;
 import at.ac.tuwien.dse.core.db.OperationTypeDAO;
 import at.ac.tuwien.dse.core.db.PatientDAO;
 import at.ac.tuwien.dse.core.db.TimeSlotDAO;
 import at.ac.tuwien.dse.core.model.Doctor;
 import at.ac.tuwien.dse.core.model.Hospital;
 import at.ac.tuwien.dse.core.model.HospitalEmployee;
 import at.ac.tuwien.dse.core.model.OperationType;
 import at.ac.tuwien.dse.core.model.Patient;
 import at.ac.tuwien.dse.core.model.TimeSlot;
 import at.ac.tuwien.dse.core.util.Config;
 
 import com.mongodb.DB;
 import com.mongodb.DBAddress;
 import com.mongodb.Mongo;
 
 
 public class Test_InsertTestData {
 	protected static DAOFactory fact;
 	protected static DB db;
 	
 	@BeforeClass
 	public static void setUp() throws ParseException {
 		try {
 			db = Mongo.connect(new DBAddress(Config.DB_HOST));
 		} catch (UnknownHostException e1) {
 			e1.printStackTrace();
 			return;
 		}
 		fact = new DAOFactory(db);
 		db.dropDatabase();
 		
 		PatientDAO pDao = fact.getPatientDAO();
 		Patient p1 = new Patient();
 		p1.setEmail("asdf");
 		p1.setLatitude(48.3175373);
 		p1.setLongitude(15.1228989);
 		p1.setName("Hr. Franz Meier");
 		p1.setPassword(getPassword("pwfm"));
 		p1.setSsn("1234");
 		pDao.persist(p1);
 		
 		Patient p2 = new Patient();
 		p2.setEmail("asdf1");
 		p2.setLatitude(48.21524);
 		p2.setLongitude(16.34799);
		p2.setName("Fr. Hermine Mller");
 		p2.setPassword(getPassword("pwhm"));
 		p2.setSsn("1233");
 		pDao.persist(p2);
 		
 		Patient p3 = new Patient();
 		p3.setEmail("asdf2");
 		p3.setLatitude(48.2222);
 		p3.setLongitude(16.38231);
 		p3.setName("Hr. Markus Moser");
 		p3.setPassword(getPassword("pwmm"));
 		p3.setSsn("1235");
 		pDao.persist(p3);
 		
 		Patient p4 = new Patient();
 		p4.setEmail("asdf3");
 		p4.setLatitude(48.2222);
 		p4.setLongitude(16.38231);
 		p4.setName("Fr. Beatrix Bauer");
 		p4.setPassword(getPassword("pwbb"));
 		p4.setSsn("1235");
 		pDao.persist(p4);
 		
 		Patient p5 = new Patient();
 		p5.setEmail("asdf4");
 		p5.setLatitude(48.2222);
 		p5.setLongitude(16.38231);
		p5.setName("Hr. Ben Bcker");
 		p5.setPassword(getPassword("pwbb"));
 		p5.setSsn("1235");
 		pDao.persist(p5);
 		
 		Patient p6 = new Patient();
 		p6.setEmail("asdf5");
 		p6.setLatitude(48.2222);
 		p6.setLongitude(16.38231);
 		p6.setName("Fr. Gloria Fasching");
 		p6.setPassword(getPassword("pwgf"));
 		p6.setSsn("1235");
 		pDao.persist(p6);
 		
 		DoctorDAO dDao = fact.getDoctorDAO();
 		Doctor d = new Doctor();
 		d.setEmail("dominik0012@gmx.net");
 		d.setName("Dr. Aufmesser");
 		d.setPassword(getPassword("dra"));
 		dDao.persist(d);
 		
 		Doctor d1 = new Doctor();
 		d1.setEmail("qwer1");
 		d1.setName("Dr. Gott");
 		d1.setPassword(getPassword("drg"));
 		dDao.persist(d1);
 		
 		Doctor d2 = new Doctor();
 		d2.setEmail("qwer2");
 		d2.setName("Dr. Gust-Fehler");
 		d2.setPassword(getPassword("drgf"));
 		dDao.persist(d2);
 		
 		Doctor d3 = new Doctor();
 		d3.setEmail("qwer3");
 		d3.setName("Dr. Augenblick");
 		d3.setPassword(getPassword("dra"));
 		dDao.persist(d3);
 		
 		OperationTypeDAO oDao = fact.getOperationTypeDAO();
 		OperationType o = new OperationType();
 		o.setName("Augenheilkunde");
 		oDao.persist(o);
 		
 		OperationType o1 = new OperationType();
		o1.setName("Orthopdie");
 		oDao.persist(o1);
 		
 		OperationType o2 = new OperationType();
 		o2.setName("HNO");
 		oDao.persist(o2);
 		
 		OperationType o3 = new OperationType();
 		o3.setName("Neurochirurgie");
 		oDao.persist(o3);
 		
 		OperationType o4 = new OperationType();
 		o4.setName("Kardiologie");
 		oDao.persist(o4);
 		
 		HospitalDAO hDao = fact.getHospitalDAO();
 		
 		Hospital h = new Hospital();
 		h.setLatitude(48.30768);
 		h.setLongitude(16.32373);
 		h.setName("LKH Klosterneuburg");
 		hDao.persist(h);
 		
 		Hospital h1 = new Hospital();
 		h1.setLatitude(48.21511);
 		h1.setLongitude(16.3804);
		h1.setName("Barmherzige Brder Wien");
 		hDao.persist(h1);
 		
 		Hospital h2 = new Hospital();
 		h2.setLatitude(48.20353);
 		h2.setLongitude(15.63817);
 		h2.setName("KH St. Poelten");
 		hDao.persist(h2);
 		
 		Hospital h3 = new Hospital();
 		h3.setLatitude(48.22058);
 		h3.setLongitude(16.34705);
 		h3.setName("AKH Wien");
 		hDao.persist(h3);
 		
 		Hospital h4 = new Hospital();
 		h4.setLatitude(48.30329);
 		h4.setLongitude(14.30547);
 		h4.setName("AKH Linz");
 		hDao.persist(h4);
 		
 		Hospital h5 = new Hospital();
 		h5.setLatitude(48.33158);
 		h5.setLongitude(16.06069);
 		h5.setName("LKH Tulln");
 		hDao.persist(h5);
 		
 		HospitalEmployeeDAO heDao = fact.getHospitalEmployeeDAO();
 		HospitalEmployee he = new HospitalEmployee();
 		he.setHospitalId(h.getId());
 		he.setName("empl1");
 		he.setPassword(getPassword("pwe1"));
 		he.setEmail("wert1");
 		heDao.persist(he);
 		
 		HospitalEmployee he1 = new HospitalEmployee();
 		he1.setHospitalId(h1.getId());
 		he1.setName("empl2");
 		he1.setPassword(getPassword("pwe2"));
 		he1.setEmail("wert2");
 		heDao.persist(he1);
 		
 		HospitalEmployee he2 = new HospitalEmployee();
 		he2.setHospitalId(h2.getId());
 		he2.setName("empl3");
 		he2.setPassword(getPassword("pwe3"));
 		he2.setEmail("wert3");
 		heDao.persist(he2);
 		
 		HospitalEmployee he3 = new HospitalEmployee();
 		he3.setHospitalId(h3.getId());
 		he3.setName("empl4");
 		he3.setPassword(getPassword("pwe4"));
 		he3.setEmail("wert4");
 		heDao.persist(he3);
 		
 		HospitalEmployee he4 = new HospitalEmployee();
 		he4.setHospitalId(h4.getId());
 		he4.setName("empl5");
 		he4.setPassword(getPassword("pwe5"));
 		he4.setEmail("wert5");
 		heDao.persist(he4);
 		
 		HospitalEmployee he5 = new HospitalEmployee();
 		he5.setHospitalId(h5.getId());
 		he5.setName("empl6");
 		he5.setPassword(getPassword("pwe6"));
 		he5.setEmail("wert6");
 		heDao.persist(he5);
 		
 		SimpleDateFormat sdf = new SimpleDateFormat("d.M.y H:m");
 		
 		TimeSlotDAO tsDao = fact.getTimeSlotDAO();
 		TimeSlot ts = new TimeSlot();
 		ts.setHospitalId(h1.getId());
 		ts.setOperationTypeId(o.getId());
 		ts.setStart(sdf.parse("30.06.2013 09:00"));
 		ts.setEnd(sdf.parse("30.06.2013 11:00"));
 		tsDao.persist(ts);
 		
 		TimeSlot ts1 = new TimeSlot();
 		ts1.setHospitalId(h2.getId());
 		ts1.setOperationTypeId(o.getId());
 		ts1.setStart(sdf.parse("29.06.2013 11:00"));
 		ts1.setEnd(sdf.parse("29.06.2013 13:00"));
 		tsDao.persist(ts1);
 		
 		TimeSlot ts2 = new TimeSlot();
 		ts2.setHospitalId(h3.getId());
 		ts2.setOperationTypeId(o.getId());
 		ts2.setStart(sdf.parse("29.06.2013 12:00"));
 		ts2.setEnd(sdf.parse("29.06.2013 15:00"));
 		tsDao.persist(ts2);
 		
 		TimeSlot ts3 = new TimeSlot();
 		ts3.setHospitalId(h4.getId());
 		ts3.setOperationTypeId(o.getId());
 		ts3.setStart(sdf.parse("29.06.2013 13:00"));
 		ts3.setEnd(sdf.parse("29.06.2013 17:00"));
 		tsDao.persist(ts3);
 		
 		TimeSlot ts4 = new TimeSlot();
 		ts4.setHospitalId(h.getId());
 		ts4.setOperationTypeId(o.getId());
 		ts4.setStart(sdf.parse("30.06.2013 09:00"));
 		ts4.setEnd(sdf.parse("30.06.2013 10:00"));
 		tsDao.persist(ts4);
 		
 	}
 	
 	@Test
 	public void test() {
 		
 	}
 	
 	private static byte[] getPassword(String pw) {
 		try {
 			return MessageDigest.getInstance("SHA-1").digest(pw.getBytes());
 		} catch (NoSuchAlgorithmException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return null;
 		}
 	}
 }
