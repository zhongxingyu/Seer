 package ch.bfh.bti7081.s2013.blue.service;
 
 import java.sql.Date;
 import java.util.Calendar;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import javax.persistence.TypedQuery;
 
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 import ch.bfh.bti7081.s2013.blue.entities.MedicalDrug;
 import ch.bfh.bti7081.s2013.blue.entities.Patient;
 import ch.bfh.bti7081.s2013.blue.entities.Prescription;
 import ch.bfh.bti7081.s2013.blue.entities.PrescriptionItem;
 
 /**
  * This class tests the PrescriptionService method getDailyPrescriptions
  * @author andef4
  */
 public class PrescriptionServiceTest {
 
     private Patient patient = null;
     private EntityManager em = null;
 
     /**
      * some example drugs
      */
     private static String[] DRUGS = { "Fluconax 200 mg, Kapseln",
             "Dafalgan, Brausetabletten 500 mg", "Azaimun 50 mg, Tabletten" };
 
     /**
      * default constructor, initializes the EntityManager em.
      */
     public PrescriptionServiceTest() {
         EntityManagerFactory factory = Persistence
                 .createEntityManagerFactory("mhc-pms");
         em = factory.createEntityManager();
     }
 
     /**
      * This method runs before every test and creates a test user, which is
      * deleted after every test.
      */
     @Before
     public void setUp() {
         em.getTransaction().begin();
        Patient patient = new Patient();
         patient.setFirstName("Fitz");
         patient.setLastName("Berger");
         Calendar calendar = Calendar.getInstance();
         calendar.set(Calendar.YEAR, 1976);
         calendar.set(Calendar.MONTH, 2);
         calendar.set(Calendar.DAY_OF_MONTH, 22);
         patient.setBirthday(new Date(calendar.getTime().getTime()));
         em.persist(patient);
         em.getTransaction().commit();
     }
 
     /**
      * This method tests the getDailyPrescription method with multiple
      * overlapping prescriptionItems. Every prescription Item has the same drug
      * and the same amount (morning: 1, noon: 2, evening: 3, night: 4). The
      * following table shows the distribution of the items. The numbers are the
      * days: [1 2 3 4 ] [ 2 3 4 5 ] [ 4 5 6 7 ] So on day 2 the patient takes 2
      * pills, on day 4 3 pills and on day 6 only 1 in the morning.
      */
     @Test
     public void getDailyPrescriptions() {
         TypedQuery<MedicalDrug> query = em.createQuery(
                 "SELECT drug FROM MedicalDrug drug WHERE drug.name=:drug",
                 MedicalDrug.class);
         query.setParameter("drug", DRUGS[0]);
         MedicalDrug medicalDrug = query.getSingleResult();
 
         Prescription prescription = null;
         PrescriptionItem item1 = null;
         PrescriptionItem item2 = null;
         PrescriptionItem item3 = null;
 
         try {
             em.getTransaction().begin();
 
             prescription = new Prescription();
             prescription.setPatient(patient);
             prescription.setDate(new Date(Calendar.getInstance().getTime()
                     .getTime()));
             prescription.setDoctor("Dr. Who");
             em.persist(prescription);
 
             Calendar startDate = Calendar.getInstance();
             Calendar endDate = Calendar.getInstance();
 
             endDate.add(Calendar.DAY_OF_MONTH, 3);
            item1 = getPrescriptionItem(medicalDrug, startDate, endDate);
             em.persist(item1);
 
             startDate.add(Calendar.DAY_OF_MONTH, 1);
             endDate.add(Calendar.DAY_OF_MONTH, 1);
            item2 = getPrescriptionItem(medicalDrug, startDate, endDate);
             em.persist(item2);
 
             startDate.add(Calendar.DAY_OF_MONTH, 2);
             endDate.add(Calendar.DAY_OF_MONTH, 2);
            item3 = getPrescriptionItem(medicalDrug, startDate, endDate);
             em.persist(item3);
 
             em.getTransaction().commit();
 
             List<DailyPrescription> dailyPrescriptions = PrescriptionService
                     .getInstance().getDailyPrescriptions(patient);
 
             assertDailyPrescription(dailyPrescriptions.get(0), medicalDrug, 1);
             assertDailyPrescription(dailyPrescriptions.get(1), medicalDrug, 2);
             assertDailyPrescription(dailyPrescriptions.get(2), medicalDrug, 2);
             assertDailyPrescription(dailyPrescriptions.get(3), medicalDrug, 3);
             assertDailyPrescription(dailyPrescriptions.get(4), medicalDrug, 2);
             assertDailyPrescription(dailyPrescriptions.get(5), medicalDrug, 1);
             assertDailyPrescription(dailyPrescriptions.get(6), medicalDrug, 1);
 
         } finally {
             // clean up database
             if (prescription != null && prescription.getId() != 0) {
                 em.remove(prescription);
             }
             if (item1 != null && item1.getId() != 0) {
                 em.remove(item1);
             }
             if (item2 != null && item2.getId() != 0) {
                 em.remove(item2);
             }
             if (item3 != null && item3.getId() != 0) {
                 em.remove(item3);
             }
         }
 
     }
 
     /**
      * creates a PrescriptionItem object with default values
      * @param drug The drug of the PrescriptionItem
      * @param startDate of the PrescriptionItem
      * @param endDate of the PrescriptionItem
      * @return a new (not persisted) PrescriptionItem
      */
     private PrescriptionItem getPrescriptionItem(MedicalDrug drug,
            Calendar startDate, Calendar endDate) {
         PrescriptionItem item1 = new PrescriptionItem();
         item1.setStartDate(new Date(startDate.getTime().getTime()));
         item1.setEndDate(new Date(endDate.getTime().getTime()));
         item1.setMedicalDrug(drug);
         item1.setMorning(1);
         item1.setNoon(2);
         item1.setEvening(3);
         item1.setNight(4);
         return item1;
     }
 
     /**
      * Test if a dailyPresciption has the correct amount of drugs for a single
      * MedicalDrug.
      * 
      * @param dailyPrescription
      *            The prescription to assert
      * @param ratio
      *            How many prescriptions are overlapping on this day
      */
     private void assertDailyPrescription(DailyPrescription dailyPrescription,
             MedicalDrug drug, int ratio) {
         Assert.assertEquals(
                 (int) dailyPrescription.getMorningDrugs().get(drug), 1 * ratio);
         Assert.assertEquals((int) dailyPrescription.getNoonDrugs().get(drug),
                 2 * ratio);
         Assert.assertEquals(
                 (int) dailyPrescription.getMorningDrugs().get(drug), 3 * ratio);
         Assert.assertEquals(
                 (int) dailyPrescription.getMorningDrugs().get(drug), 4 * ratio);
     }
 
     /**
      * Run after every test. Deletes the uesr created in setUp().
      */
     @After
     public void tearDown() {
         em.remove(patient);
     }
 }
