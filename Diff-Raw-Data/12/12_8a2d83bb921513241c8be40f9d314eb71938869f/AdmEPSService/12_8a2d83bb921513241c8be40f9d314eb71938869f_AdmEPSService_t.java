 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package servicios;
 
 import controller.AppointmentCRUDUrl;
 import controller.CRUD_EPS;
 import controller.MedicalRecordCRUDUrl;
 import fachadews.Appointment;
 import fachadews.MedicalRecord;
 import fachadews.MedicalRecordCRUD;
 import java.io.Serializable;
 import java.util.List;
 import javax.jws.WebService;
 import javax.jws.WebMethod;
 import javax.jws.WebParam;
 import javax.ejb.Stateless;
 import javax.xml.ws.WebServiceRef;
 import model.EPS;
 
 /**
  *
  * @author Sebastian
  */
 @WebService(serviceName = "AdmEPSService")
 @Stateless()
 public class AdmEPSService implements Serializable{
     //-----------------------------------------------
     //Appointmens
     //----------------------------------------------
     @WebMethod(operationName = "addAppoinment")
     public void addAppointment(@WebParam(name = "idRecord") int idRecord, @WebParam(name = "appointment") Appointment appointment, @WebParam(name = "idEPS") int idEPS)
     {
         EPS eps = new CRUD_EPS().readByID(idEPS);
         new AppointmentCRUDUrl().addAppointment(idRecord, appointment, eps.getUrlAppointments());
     }
     
     @WebMethod(operationName = "deleteAppoinment")
     public void deleteAppointment(@WebParam(name = "idRecord") int idRecord,@WebParam(name = "idAppointment") int idAppointment, @WebParam(name = "idEPS") int idEPS)
     {
         EPS eps = new CRUD_EPS().readByID(idEPS);
         new AppointmentCRUDUrl().deleteAppointment(idRecord, idAppointment, eps.getUrlAppointments());
     }
     @WebMethod(operationName = "getAppointment")
     public void getAppointment(@WebParam(name = "idRecord") int idRecord,@WebParam(name = "idAppointment") int idAppointment, @WebParam(name = "idEPS") int idEPS)
     {
         EPS eps = new CRUD_EPS().readByID(idEPS);
         new AppointmentCRUDUrl().getAppointment(idRecord, idAppointment, eps.getUrlAppointments());
     }
     //-----------------------------------------------
     //EPS
     //----------------------------------------------
     @WebMethod(operationName = "createEPS")
     public void createEPS(  @WebParam(name = "idEPS") int id, 
                             @WebParam(name = "nombreEPS") String name, 
                             @WebParam(name = "IPandPort") String IP,
                             @WebParam(name = "password") String password)
     {        
         new CRUD_EPS().create(id, name, IP, password);
     }
     
     @WebMethod(operationName = "readOneEPS")
     public EPS readByID(@WebParam(name = "idEPS") int id)
     {        
         return new CRUD_EPS().readByID(new Integer(id));
     }
     
     @WebMethod(operationName = "readAllEPS")
     public List<EPS> readAll()
     {        
         return new CRUD_EPS().readAll();
     }
     
     @WebMethod(operationName = "updateEPS")
     public void updateEPS(  @WebParam(name = "idEPS") int id, 
                             @WebParam(name = "nombreEPS") String name, 
                             @WebParam(name = "urlMedicalRecord") String urlRecord, 
                             @WebParam(name = "urlAppointment") String urlAppointment,
                             @WebParam(name = "urlFinancial") String urlFinancial)
     {        
         new CRUD_EPS().update(id, name, urlRecord, urlAppointment, urlFinancial);
     }
     
     @WebMethod(operationName = "deleteEPS")
     public void deleteID(@WebParam(name = "idEPS") int id)
     {        
         new CRUD_EPS().delete(new Integer(id));
     }
     
     @WebMethod(operationName = "changeEPS")
     public void changeEPS(@WebParam(name = "idRecord") int idRecord, @WebParam(name = "fromEPS") EPS from, @WebParam(name = "toEPS") EPS to)
     {
         new MedicalRecordCRUDUrl().changeEPS(idRecord,from,to);
     }
     //-----------------------------------------------
     //Medical Records
     //----------------------------------------------
 
    @WebMethod(operationName = "createMedicalRecord")
     public void createMedicalRecord(@WebParam(name = "idRecord") int idMedicalRecord, @WebParam(name = "idEPS") int idEPS) {
         EPS eps = new CRUD_EPS().readByID(idEPS);
         new MedicalRecordCRUDUrl().createMRList(idMedicalRecord,new MedicalRecord(), eps.getUrlRecords());
     }
     
    @WebMethod(operationName = "readMedicalRecord")
     public MedicalRecord readMedicalRecord(@WebParam(name = "idRecord") int idMedicalRecord, @WebParam(name = "idEPS") int idEPS)
     {
         EPS eps = new CRUD_EPS().readByID(idEPS);
         return new MedicalRecordCRUDUrl().getMR(idMedicalRecord, eps.getUrlRecords());
     }
     
    @WebMethod(operationName = "updateMedicalRecord")
     public void updateMedicalRecord(@WebParam(name = "record") MedicalRecord mr,@WebParam(name = "idEPS") int idEPS)
     {
         EPS eps = new CRUD_EPS().readByID(idEPS);
         new MedicalRecordCRUDUrl().setMR(mr, eps.getUrlRecords());
     }
     
    @WebMethod(operationName = "deleteMedicalRecord")
     public void deleteMedicalRecord(@WebParam(name = "idRecord") int idMedicalRecord, @WebParam(name = "idEPS") int idEPS)
     {
         EPS eps = new CRUD_EPS().readByID(idEPS);
         new MedicalRecordCRUDUrl().deleteMR(idMedicalRecord, eps.getUrlRecords());
     }
 
    @WebMethod(operationName = "readAllMedicalRecord")
     public List<MedicalRecord> readAllMedicalRecord(@WebParam(name = "idEPS") int idEPS)
     {
         EPS eps = new CRUD_EPS().readByID(idEPS);
         return new MedicalRecordCRUDUrl().readAllMR(eps.getId());
     }
 }
