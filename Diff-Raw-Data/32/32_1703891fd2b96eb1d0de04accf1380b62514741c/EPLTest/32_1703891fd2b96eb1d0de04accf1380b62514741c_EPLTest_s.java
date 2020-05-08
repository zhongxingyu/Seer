 
 import ca.uhn.hl7v2.model.v25.datatype.CX;
 import ca.uhn.hl7v2.model.v25.group.OMP_O09_ORDER;
 import ca.uhn.hl7v2.model.v25.message.ACK;
 import ca.uhn.hl7v2.model.v25.message.ADT_A01;
 import ca.uhn.hl7v2.model.v25.message.ADT_A03;
 import ca.uhn.hl7v2.model.v25.message.OMP_O09;
 import ca.uhn.hl7v2.model.v25.segment.MSH;
 import ca.uhn.hl7v2.model.v25.segment.PID;
 import com.abada.epl.test.CustomEPL;
 import com.abada.esper.EsperLoader;
 import com.abada.esper.service.EsperService;
 import java.net.URL;
 
 /*
  * To change this template, choose Tools | Templates and open the template in
  * the editor.
  */
 /**
  *
  * @author mmartin
  */
 public class EPLTest {
 
     public static void main(String[] args) throws Exception {
 
         URL esper = new URL("file:/home/mmartin/NetBeansProjects/eva/trunk/eva-server/eva-rest/src/main/resources/META-INF/hl7.esper.config.cfg.xml");
         URL sta = new URL("file:/home/mmartin/NetBeansProjects/eva/trunk/eva-server/eva-rest/src/main/resources/META-INF/statement.esper.config.cfg.xml");
 
         EsperLoader el = new EsperLoader(esper);
 
        EsperService es = new EsperService(sta, el, null, null, null, 1, 1);
         CustomEPL c = new CustomEPL(System.currentTimeMillis());
 
 
         es.send(getMessageADT_A01());
         Thread.currentThread().sleep(2000);
         es.send(getMessageADT_A03());
         Thread.currentThread().sleep(2000);
         es.send(getOMP_009());
         Thread.currentThread().sleep(2000);
         es.send(getMessageADT_A01());
         Thread.currentThread().sleep(2000);
         es.send(getOMP_009());
 
 //        Thread.currentThread().sleep(5000);
 
 //        c.setTest("3");
 //        es.send(c);
 //        System.out.println("--------3--------\n");
 //        Thread.currentThread().sleep(5000);
 //
 //        c.setTest("4");
 //        es.send(c);
 //        System.out.println("--------4--------\n");
 //        Thread.currentThread().sleep(5000);
 //
 //        c.setTest("5");
 //        es.send(c);
 //        System.out.println("--------5--------\n");
 //        Thread.currentThread().sleep(5000);
 
     }
 
     public static ADT_A01 getMessageADT_A01() throws Exception {
 
         ADT_A01 adt = new ADT_A01();
 
         // Populate the MSH Segment          
         MSH mshSegment = adt.getMSH();
         mshSegment.getFieldSeparator().setValue("|");
         mshSegment.getEncodingCharacters().setValue("^~\\&");
         mshSegment.getDateTimeOfMessage().getTime().setValue("200701011539");
         mshSegment.getSendingApplication().getNamespaceID().setValue("TestSendingSystem");
         mshSegment.getSequenceNumber().setValue("123");
         mshSegment.getMessageType().getMessageCode().setValue("ADT");
         mshSegment.getMessageType().getTriggerEvent().setValue("A01");
         mshSegment.getMessageType().getMessageStructure().setValue("ADT_A01");
         mshSegment.getMsh12_VersionID().getVid1_VersionID().setValue("2.5");
         // Populate the PID Segment
         PID pid = adt.getPID();
         pid.getPatientName(0).getFamilyName().getSurname().setValue("Doe");
         pid.getPatientName(0).getGivenName().setValue("John");
 
         CX patientIdentifier = pid.insertPid3_PatientIdentifierList(0);
         patientIdentifier.getCx1_IDNumber().setValue(Integer.toString(1));
         patientIdentifier.getCx5_IdentifierTypeCode().setValue("PI8");
 
 
         //
 
 //         CX[] cxarray = pid.getPid3_PatientIdentifierList();
 //        if (cxarray != null && cxarray.length > 0) {
 //            for (CX cxx : cxarray) {
 //                addPatientId(patient, cxx.getCx1_IDNumber().getValue(), cxx.getCx5_IdentifierTypeCode().getValue());
 //            }
 //        }
 
         //pid.getPatientIdentifierList(0).get().setValue("123456")
 
         return adt;
     }
 
     public static OMP_O09 getOMP_009() throws Exception {
 
         OMP_O09 message = new OMP_O09();
 
         // Populate the MSH Segment          
         MSH mshSegment = message.getMSH();
         mshSegment.getFieldSeparator().setValue("|");
         mshSegment.getEncodingCharacters().setValue("^~\\&");
         mshSegment.getDateTimeOfMessage().getTime().setValue("200701011539");
         mshSegment.getSendingApplication().getNamespaceID().setValue("TestSendingSystem");
         mshSegment.getSequenceNumber().setValue("13343");
         mshSegment.getMessageType().getMessageCode().setValue("OMP");
         mshSegment.getMessageType().getTriggerEvent().setValue("O09");
         mshSegment.getMessageType().getMessageStructure().setValue("OMP_O09");
         mshSegment.getMsh12_VersionID().getVid1_VersionID().setValue("2.5");
         // Populate the PID Segment
         PID pid = message.getPATIENT().getPID();
         pid.getPatientName(0).getFamilyName().getSurname().setValue("Doe");
         pid.getPatientName(0).getGivenName().setValue("John");
 
         CX patientIdentifier = pid.insertPid3_PatientIdentifierList(0);
         patientIdentifier.getCx1_IDNumber().setValue(Integer.toString(1));
         patientIdentifier.getCx5_IdentifierTypeCode().setValue("PI");
         OMP_O09_ORDER orderSegment = message.getORDER();
         orderSegment.getORC().getOrc2_PlacerOrderNumber().getEi1_EntityIdentifier().setValue("2");
 
         return message;
     }
 
     public static ADT_A03 getMessageADT_A03() throws Exception {
 
         ADT_A03 adt = new ADT_A03();
 
         // Populate the MSH Segment          
         MSH mshSegment = adt.getMSH();
         mshSegment.getFieldSeparator().setValue("|");
         mshSegment.getEncodingCharacters().setValue("^~\\&");
         mshSegment.getDateTimeOfMessage().getTime().setValue("200701011539");
         mshSegment.getSendingApplication().getNamespaceID().setValue("TestSendingSystem");
         mshSegment.getSequenceNumber().setValue("123");
         mshSegment.getMessageType().getMessageCode().setValue("ADT");
         mshSegment.getMessageType().getTriggerEvent().setValue("A01");
         mshSegment.getMessageType().getMessageStructure().setValue("ADT_A01");
         mshSegment.getMsh12_VersionID().getVid1_VersionID().setValue("2.5");
         // Populate the PID Segment
         PID pid = adt.getPID();
         pid.getPatientName(0).getFamilyName().getSurname().setValue("Doe");
         pid.getPatientName(0).getGivenName().setValue("John");
 
         CX patientIdentifier = pid.insertPid3_PatientIdentifierList(0);
         patientIdentifier.getCx1_IDNumber().setValue(Integer.toString(1));
         patientIdentifier.getCx5_IdentifierTypeCode().setValue("PI");
 
 
         //
 
 //         CX[] cxarray = pid.getPid3_PatientIdentifierList();
 //        if (cxarray != null && cxarray.length > 0) {
 //            for (CX cxx : cxarray) {
 //                addPatientId(patient, cxx.getCx1_IDNumber().getValue(), cxx.getCx5_IdentifierTypeCode().getValue());
 //            }
 //        }
 
         //pid.getPatientIdentifierList(0).get().setValue("123456")
 
         return adt;
     }
 
     public static ACK getACK() throws Exception {
 
         return (ACK) getMessageADT_A01().generateACK();
     }
 }
