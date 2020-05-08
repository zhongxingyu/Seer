 package puf.m2.hms.view;
 
 import java.util.Date;
 
 import org.easymock.EasyMock;
 import org.junit.Assert;
 import org.junit.Test;
 
 import puf.m2.hms.exception.MedicalRecordException;
 import puf.m2.hms.model.MedicalRecord;
 import puf.m2.hms.model.Patient;
 
 public class InsertDiagnosisTest {
 
 	@Test
 	public void testIsDuplicateMedicalRecord() {
 		Patient patient = EasyMock.createMock(Patient.class);
 		InsertDiagnosis mock = EasyMock.createMock(InsertDiagnosis.class);
 		Date newDate = new Date();
 		String newDetail = "abc";
 		Assert.assertEquals(false,
 				mock.isDuplicateMedicalRecord(patient, newDate, newDetail));
 
 	}
 
	@SuppressWarnings("deprecation")
 	@Test
 	public void testIsDuplicateMedicalRecord2() {
 		MedicalRecord medicalRecord_mock = EasyMock
 				.createMock(MedicalRecord.class);
 		medicalRecord_mock.setDateAffect(new Date(2012, 02, 02));
 		medicalRecord_mock.setDetail("abc");
 		try {
 			medicalRecord_mock.save();
 			EasyMock.expectLastCall();
 		} catch (MedicalRecordException e) {
 			e.printStackTrace();
 		}
 
 		Patient patient = medicalRecord_mock.getPatient();
 		InsertDiagnosis mock = EasyMock.createMock(InsertDiagnosis.class);
 		Date newDate = new Date(2012, 02, 02);
 		String newDetail = "abc";
 		Assert.assertEquals(false,
 				mock.isDuplicateMedicalRecord(patient, newDate, newDetail));
 	}
 
 	@Test
 	public void testIsValidFields_invalid() {
 		String detail = "";
 		InsertDiagnosis mock = EasyMock.createMock(InsertDiagnosis.class);
 		Assert.assertEquals(false, mock.isValidFields(detail));
 	}
 
 	@Test
 	public void testIsValidFields_valid() {
 		String detail = "abc";
 		InsertDiagnosis mock = EasyMock.createMock(InsertDiagnosis.class);
 		Assert.assertEquals(false, mock.isValidFields(detail));
 	}
 
 }
