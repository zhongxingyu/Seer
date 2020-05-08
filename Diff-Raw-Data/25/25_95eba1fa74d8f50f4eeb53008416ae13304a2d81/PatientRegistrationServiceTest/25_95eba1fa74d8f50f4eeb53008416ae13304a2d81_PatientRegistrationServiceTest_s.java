 package org.openmrs.module.patientregistration.service;
 
 import junit.framework.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.openmrs.Location;
 import org.openmrs.LocationAttribute;
 import org.openmrs.LocationAttributeType;
 import org.openmrs.Patient;
 import org.openmrs.module.emrapi.EmrApiProperties;
 import org.openmrs.module.emrapi.printer.Printer;
 import org.openmrs.module.emrapi.printer.PrinterService;
 import org.openmrs.module.emrapi.printer.UnableToPrintViaSocketException;
 import org.openmrs.module.paperrecord.PaperRecordService;
 import org.openmrs.module.paperrecord.UnableToPrintLabelException;
 import org.openmrs.module.patientregistration.PatientRegistrationUtil;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import java.util.UUID;
 
 import static org.mockito.Mockito.doNothing;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.spy;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.powermock.api.mockito.PowerMockito.mockStatic;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest(PatientRegistrationUtil.class)
 public class PatientRegistrationServiceTest {
 
     private PatientRegistrationServiceImpl patientRegistrationService;
 
     private PrinterService printerService;
 
     private EmrApiProperties emrApiProperties;
 
     private PaperRecordService paperRecordService;
 
     @Before
     public void setup() {
         paperRecordService = mock(PaperRecordService.class);
         paperRecordService.setPrinterService(printerService);
         patientRegistrationService = spy(new PatientRegistrationServiceImpl());
         printerService = mock(PrinterService.class);
         patientRegistrationService.setPrinterService(printerService);
         emrApiProperties = mock(EmrApiProperties.class);
         patientRegistrationService.setEmrApiProperties(emrApiProperties);
         patientRegistrationService.setPaperRecordService(paperRecordService);
     }
 
 
     @Test
     public void printIdCardLabel_shouldCallMethodToPrintIdCardLabel() throws UnableToPrintLabelException {
 
         Location location = new Location(1);
         Patient patient = new Patient(1);
         Printer printer = new Printer();
         printer.setId(1);
 
         patientRegistrationService.printIDCardLabel(patient, location);
 
         verify(paperRecordService).printIdCardLabel(patient, location);
     }
 
     @Test
    public void printIdCard_shouldCallMethodToPrintIdCard() throws UnableToPrintViaSocketException {
 
         Location location = new Location(1);
         Location medicalRecordLocation = new Location(2);
         Patient patient = new Patient(1);
         Printer printer = new Printer();
         printer.setId(1);
 
         mockStatic(PatientRegistrationUtil.class);
         when(PatientRegistrationUtil.getMedicalRecordLocationRecursivelyBasedOnTag(location)).thenReturn(medicalRecordLocation);
         when(printerService.getDefaultPrinter(location, Printer.Type.ID_CARD)).thenReturn(printer);
         doNothing().when(patientRegistrationService).printIdCardUsingEPCL(patient, printer, medicalRecordLocation);
 
         patientRegistrationService.printIDCard(patient, location);
 
         verify(patientRegistrationService).printIdCardUsingEPCL(patient, printer, medicalRecordLocation);
 
     }
 
     @Test
     public void printRegistrationLabel_shouldCallMethodToPrintRegistrationLabel() throws UnableToPrintLabelException {
 
         Location location = new Location(1);
         Patient patient = new Patient(1);
 
         patientRegistrationService.printRegistrationLabel(patient, location, 1);
 
         verify(paperRecordService).printPaperRecordLabels(patient, location, 1);
     }
 
     @Test
     public void getNameToPrintOnIdCardShouldReturnNameToPrintOnIdCard() {
 
         LocationAttributeType nameToPrintOnIdCardType = new LocationAttributeType();
         nameToPrintOnIdCardType.setId(1);
         nameToPrintOnIdCardType.setUuid(UUID.randomUUID().toString());
 
         Location location = new Location(1);
         location.setName("Regular name");
 
         LocationAttribute nameToPrintOnIdCard = new LocationAttribute();
         nameToPrintOnIdCard.setAttributeType(nameToPrintOnIdCardType);
         nameToPrintOnIdCard.setValue("Name to print");
         location.setAttribute(nameToPrintOnIdCard);
 
         when(emrApiProperties.getLocationAttributeTypeNameToPrintOnIdCard()).thenReturn(nameToPrintOnIdCardType);
 
         Assert.assertEquals("Name to print", patientRegistrationService.getNameToPrintOnIdCard(location));
     }
 
     @Test
     public void getNameToPrintOnIdCardShouldReturnDisplayNameIfNameToDisplayOnIdCardAttributeDefined() {
 
         LocationAttributeType nameToPrintOnIdCardType = new LocationAttributeType();
         nameToPrintOnIdCardType.setId(1);
         nameToPrintOnIdCardType.setUuid(UUID.randomUUID().toString());
 
         Location location = new Location(1);
         location.setName("Regular name");
 
         when(emrApiProperties.getLocationAttributeTypeNameToPrintOnIdCard()).thenReturn(nameToPrintOnIdCardType);
 
         Assert.assertEquals("Regular name", patientRegistrationService.getNameToPrintOnIdCard(location));
     }
 }
