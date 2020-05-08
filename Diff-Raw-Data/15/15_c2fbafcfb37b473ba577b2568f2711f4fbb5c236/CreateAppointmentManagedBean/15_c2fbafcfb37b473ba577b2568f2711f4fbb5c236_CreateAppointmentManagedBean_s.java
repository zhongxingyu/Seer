 package com.acme.hospital.web.managedbeans;
 
 import java.util.Date;
 import java.util.List;
 
 import javax.faces.application.FacesMessage;
 import javax.faces.application.FacesMessage.Severity;
 import javax.faces.bean.ManagedBean;
 import javax.faces.bean.ManagedProperty;
 import javax.faces.bean.SessionScoped;
 import javax.faces.context.FacesContext;
 
 import org.primefaces.context.RequestContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.acme.hospital.domain.Appointment;
 import com.acme.hospital.domain.Doctor;
 import com.acme.hospital.domain.Patient;
 import com.acme.hospital.exception.ReservedAppointmentException;
 import com.acme.hospital.dto.NeighborDates;
 import com.acme.hospital.service.AppointmentFacade;
 
 @ManagedBean(name = "createAppointmentMB")
 @SessionScoped
 public class CreateAppointmentManagedBean {
 
 	private static Logger logger = LoggerFactory
 			.getLogger(CreateAppointmentManagedBean.class);
 
 	@ManagedProperty(value = "#{simpleAppointmentFacade}")
 	private AppointmentFacade appointmentFacade;
 
 	@ManagedProperty(value = "#{LoginManagedBean}")
 	private LoginManagedBean loginManagedBean;
 
 	private Date date;
 	private Patient selectedPatient;
 	private Appointment selectedAppointment;
 	private List<Patient> allPatients;
 	private List<Appointment> doctorAppointments;
 	private Doctor loggedInDoctor;
 
 	private NeighborDates nd;
 
 	private String previousDateAsString;
 
 	private String nextDateAsString;
 
 	public void createAppointment() {
 		boolean result = false;
 		try {
 			if (!(date == null || selectedPatient == null)) {
 				String loggedInDoctorName = loginManagedBean.getLoggedInUser();
 				loggedInDoctor = appointmentFacade.getDoctorByName(loggedInDoctorName);
 				
 				result = appointmentFacade.createAppointment(loggedInDoctor,selectedPatient, date);
 			}
 			
 			generateMessage(result);
 		
 		} catch (ReservedAppointmentException e) {
 			logger.info("The appointment creation was unsuccessful. ReservedAppointmentException catched: "+e.getMessage());
 			writeMessage(FacesMessage.SEVERITY_ERROR,"Error","The appointment creation was unsuccessful. The patient already has an appointment in the near future!");
 		}
 	}
 
 
 	private void generateMessage(boolean result) {
 		if (result) {
 			writeMessage(FacesMessage.SEVERITY_INFO, "Info", "The appointment creation was successful!  Patient: " + selectedPatient.getName() + " || Date: " + date );
 			logger.info("The appointment creation was successful! For Doctor: " + loggedInDoctor.getName() + " || Patient: "	+ selectedPatient.getName() + " || Date: " + date);
			
			selectedPatient=null;
			date=null;
 		} else {
 			if (this.selectedPatient == null) {
 				writeMessage(FacesMessage.SEVERITY_ERROR, "Error", "The appointment creation was unsuccessful. No patient selected");
 				logger.info("The appointment creation was unsuccessful. No patient selected");
 			} else if (this.date == null) {
 				writeMessage(FacesMessage.SEVERITY_ERROR, "Error", "The appointment creation was unsuccessful. No date selected");
 				logger.info("The appointment creation was unsuccessful. No date selected");
 			} else if (result == false) {
 				writeMessage(FacesMessage.SEVERITY_ERROR, "Error", "The appointment creation was unsuccessful. The date is reserved!");
 				logger.info("The appointment creation was unsuccessful. The date is reserved!");
 				nd = appointmentFacade.getFreeNeighborDates(loggedInDoctor,
 						date);
 				previousDateAsString = nd.getPreviousDate().toString();
 				nextDateAsString = nd.getNextDate().toString();
 				RequestContext context = RequestContext.getCurrentInstance();
 				context.execute("dialogPac.show()");
 			}
 		}
 	}
 
 	private void writeMessage(Severity severity, String summary, String detail) {
 		FacesContext.getCurrentInstance().addMessage(null,new FacesMessage(severity,summary,detail));
 	}
 	
 	public List<Appointment> getDoctorAppointments() {
 		Doctor d = appointmentFacade.getDoctorByName(loginManagedBean
 				.getLoggedInUser());
 		doctorAppointments = (List<Appointment>) appointmentFacade
 				.getDoctorAllAppointments(d);
 		return doctorAppointments;
 	}
 
 	public void createAppointmentAtPrevoiusDate() {
 		date.setTime(nd.getPreviousDate().getTime());
 		createAppointment();
 		RequestContext context = RequestContext.getCurrentInstance();
 		context.execute("dialogPac.hide()");
 	}
 
 	public void createAppointmentAtNextDate() {
 		date.setTime(nd.getNextDate().getTime());
 		createAppointment();
 		RequestContext context = RequestContext.getCurrentInstance();
 		context.execute("dialogPac.hide()");
 	}
 
 	public String getPreviousDateAsString() {
 		return previousDateAsString;
 	}
 
 	public String getNextDateAsString() {
 		return nextDateAsString;
 	}
 
 	public Date getDate() {
 		return date;
 	}
 
 	public void setDate(Date date) {
 		this.date = date;
 	}
 
 	public List<Patient> getAllPatients() {
 		this.allPatients = (List<Patient>) appointmentFacade.getAllPatients();
 		return this.allPatients;
 	}
 
 	public Patient getSelectedPatient() {
 		return selectedPatient;
 	}
 
 	public void setSelectedPatient(Patient selectedPatient) {
 		this.selectedPatient = selectedPatient;
 	}
 
 	public LoginManagedBean getLoginManagedBean() {
 		return loginManagedBean;
 	}
 
 	public void setLoginManagedBean(LoginManagedBean loginManagedBean) {
 		this.loginManagedBean = loginManagedBean;
 	}
 
 	public Appointment getSelectedAppointment() {
 		return selectedAppointment;
 	}
 
 	public void setSelectedAppointment(Appointment selectedAppointment) {
 		this.selectedAppointment = selectedAppointment;
 	}
 
 	public AppointmentFacade getAppointmentFacade() {
 		return appointmentFacade;
 	}
 
 	public void setAppointmentFacade(AppointmentFacade appointmentFacade) {
 		this.appointmentFacade = appointmentFacade;
 	}
 }
