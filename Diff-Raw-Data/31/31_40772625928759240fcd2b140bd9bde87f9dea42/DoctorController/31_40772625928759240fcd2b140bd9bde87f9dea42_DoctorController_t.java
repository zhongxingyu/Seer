 package controller;
 
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormat;
 import org.joda.time.format.DateTimeFormatter;
 import org.springframework.amqp.core.AmqpTemplate;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import dao.IUserInterfaceDAO;
 import dse_domain.DTO.ReservationCancelNotificationDTO;
 import dse_domain.DTO.ReservationDTO;
 import dse_domain.domain.OpSlot;
 import dse_domain.domain.Patient;
 import dse_domain.domain.Doctor;
 
 /**
  * Handles requests for the doctor page.
  */
 @Controller
 @RequestMapping(value = "/doctor")
 public class DoctorController extends SlotController {
 
 	@Autowired
 	AmqpTemplate amqpTemplate;
 
 	@Autowired
 	IUserInterfaceDAO uiDAO;
 
 	static final Logger logger = Logger.getLogger(DoctorController.class);
 
 	/**
 	 * Retrieve a doctor with a given id.
 	 * 
 	 * @param model
 	 * @param id
 	 * @return
 	 */
 	@RequestMapping(value = "", method = RequestMethod.GET)
 	public String doctor(Model model, @RequestParam("id") String id) {
 
 		Doctor doctor = uiDAO.findDoctor(id);
 		List<OpSlot> opSlots = uiDAO.findAllReservedOpSlotsWithDoctor(doctor);
 
 		for (OpSlot curr : opSlots) {
 			logger.debug("found op slot " + curr.getDateString() + " with reservation: "
 					+ curr.getReservation().getDoctor() + "/" + curr.getReservation().getPatient());
 		}
 
 		model.addAttribute("doctorID", id);
 		model.addAttribute("op_slots_this_doctor", opSlots);
 		model.addAttribute("sent_reservation", false);
 
 		addStandardOutputs(model);
 
 		return "doctor";
 	}
 
 	@RequestMapping(value = "", method = RequestMethod.POST)
 	public String doctorFiltered(Model model, @RequestParam("id") String id, @RequestParam("date") String date,
			@RequestParam("from") String from, @RequestParam("to") String to,
 			@RequestParam("hospital") String hospital, @RequestParam("type") String type) {
 
 		Doctor doctor = uiDAO.findDoctor(id);
 		List<OpSlot> opSlots = uiDAO.findAllReservedOpSlotsWithDoctor(doctor);
 
 		// Remove past slots before filtering
 		opSlots = removePastSlots(opSlots);
 
 		// Perform filtering
		opSlots = filterOpSlotList(opSlots, date, from, to, null, hospital, null, type);
 
 		// Sort the list of OpSlots according to their dates
 		Collections.sort(opSlots, new OpSlotComparator());
 
 		for (OpSlot curr : opSlots) {
 			logger.debug("found op slot " + curr.getDateString() + " with reservation: "
 					+ curr.getReservation().getDoctor() + "/" + curr.getReservation().getPatient());
 		}
 
 		model.addAttribute("doctorID", id);
 		model.addAttribute("op_slots_this_doctor", opSlots);
 		model.addAttribute("sent_reservation", false);
 
 		addStandardOutputs(model, opSlots);
 
 		return "doctor";
 	}
 
 	/**
 	 * Send a reservation request to an allocator, which is processed asynchronously.
 	 * 
 	 * @param model
 	 * @param dateStart
 	 * @param dateEnd
 	 * @param patientID
 	 * @param doctorID
 	 * @param type
 	 * @param minTime
 	 * @param maxDistance
 	 * @return
 	 */
 	@RequestMapping(value = "/reserve/", method = RequestMethod.POST)
 	public String doctorReserve(Model model, @RequestParam("date_start") String dateStart,
 			@RequestParam("date_end") String dateEnd, @RequestParam("patient_id") String patientID,
 			@RequestParam("doctor_id") String doctorID, @RequestParam("type") OpSlot.Type type,
 			@RequestParam("min_time") int minTime, @RequestParam("max_distance") int maxDistance) {
 
 		DateTimeFormatter fmt = DateTimeFormat.forPattern("dd.MM.yyyy");
 		DateTime startDate = DateTime.parse(dateStart, fmt);
 		DateTime endDate = DateTime.parse(dateEnd, fmt);
 
 		ReservationDTO res = new ReservationDTO(patientID, doctorID, startDate.toDate(), endDate.toDate(), type,
 				minTime, maxDistance);
 
 		logger.info("trying to reserve patientID: " + patientID + " (as doctorID: " + doctorID
 				+ "). sending message to allocator: " + res);
 
 		amqpTemplate.convertAndSend("allocator", res);
 
 		model.addAttribute("sent_reservation", true);
 		model.addAttribute("sent_dto", res);
 		addStandardOutputs(model);
 
 		return "doctor";
 	}
 
 	@RequestMapping(value = "/remove_reservation/", method = RequestMethod.GET)
 	public String doctorRemoveReservation(Model model, @RequestParam("opslot_id") String opslot_id) {
 
 		// TODO delete reservation (DAO)
 		// uiDAO.removeReservationFromOpSlot(opslot_id); // not tested yet TODO
 
 		ReservationCancelNotificationDTO cancel = new ReservationCancelNotificationDTO(opslot_id);
 		amqpTemplate.convertAndSend(cancel);
 
 		addStandardOutputs(model);
 
 		return "doctor";
 	}
 
 	private void addStandardOutputs(Model model) {
 		List<OpSlot> opSlots = uiDAO.findAllOpSlots();
 		model.addAttribute("opSlots", opSlots);
 		model.addAttribute("slotCount", opSlots.size());
 		List<Patient> patients = uiDAO.findAllPatients();
 		model.addAttribute("patients", patients);
 	}
 
 	private void addStandardOutputs(Model model, List<OpSlot> opSlots) {
 		model.addAttribute("opSlots", opSlots);
 		model.addAttribute("slotCount", opSlots.size());
 		List<Patient> patients = uiDAO.findAllPatients();
 		model.addAttribute("patients", patients);
 	}
 
 }
