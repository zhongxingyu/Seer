 package no.uib.ii.deliverandum.controllers;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletResponse;
 
 import no.uib.ii.deliverandum.beans.Assignment;
 import no.uib.ii.deliverandum.beans.Delivery;
 import no.uib.ii.deliverandum.beans.DeliveryState;
 import no.uib.ii.deliverandum.beans.User;
 import no.uib.ii.deliverandum.beans.command.AdminDeliveryCommand;
 import no.uib.ii.deliverandum.persistence.DeliveryDao;
 import no.uib.ii.deliverandum.persistence.DeliveryFileDao;
 import no.uib.ii.deliverandum.service.AdminManager;
 import no.uib.ii.deliverandum.service.mail.MailService;
 
 import org.apache.velocity.app.VelocityEngine;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.velocity.VelocityEngineUtils;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.multipart.MultipartFile;
 import org.springframework.web.servlet.ModelAndView;
 
 @Controller
 public class EditDeliveryController extends ControllerBase {
     
     private static final String PATH = "admin/deliveries/view";
     
     @Autowired private DeliveryDao deliveryDao;
     @Autowired private DeliveryFileDao deliveryFileDao;
     @Autowired private AdminManager adminManager;
     @Autowired private MailService mailService;
     @Autowired private VelocityEngine velocityEngine;
 
     
     @RequestMapping(value = "/{courseName}/" + PATH + "/{deliveryId}.html", method = RequestMethod.GET)
     public ModelAndView viewDelivery(
             @PathVariable String courseName,
             @PathVariable int deliveryId,
             HttpServletResponse res) {
         Delivery delivery = deliveryDao.getDelivery(deliveryId);
         if (delivery == null) {
             return resourceNotFound(res, "Ukjent innlevering");
         }
         return makeModelAndView(courseName, delivery, new AdminDeliveryCommand(delivery));
     }
 
     @RequestMapping(value = "/{courseName}/" + PATH + "/{deliveryId}.html", method = RequestMethod.POST)
     public ModelAndView saveDelivery(
             @PathVariable String courseName,
             @PathVariable int deliveryId,
             @ModelAttribute("command") AdminDeliveryCommand command,
             HttpServletResponse res) {
         Delivery delivery = deliveryDao.getDelivery(deliveryId);
         if (delivery == null) {
             return resourceNotFound(res, "Ukjent innlevering");
         }
         
         delivery.setGrade(command.getGrade());
         delivery.setGradeComment(command.getGradeComment());
         delivery.setState(command.isClosed() ? DeliveryState.EVALUATED : DeliveryState.RECEIVED);
         deliveryDao.persist(delivery);
         MultipartFile gradingAttachment = command.getGradingAttachment();
         if (gradingAttachment != null && !gradingAttachment.getOriginalFilename().isEmpty()) {
             InputStream in;
             try {
                 in = gradingAttachment.getInputStream();
             } catch (IOException e) {
                 e.printStackTrace();
                 return internalServerError(res, "Couldn't save grading attachment! Error: " + e.getMessage());
             }
             deliveryFileDao.persistGradingAttachment(in, gradingAttachment.getOriginalFilename(), delivery);
         }
         
         if (delivery.getState() == DeliveryState.EVALUATED) {
             Map<String, String> model = new HashMap<String, String>();
             Assignment assignment = delivery.getAssignment();
             model.put("assignment", assignment.getName());
             model.put("coursename", assignment.getCourseName());
             model.put("grade", delivery.getGrade());
             model.put("gradecomment", delivery.getGradeComment());
             String text = VelocityEngineUtils.mergeTemplateIntoString(velocityEngine, "no/uib/ii/deliverandum/grade-notification.vm", model);
            mailService.sendMail(delivery.getDeliveredBy().getEmail(), "Innlevering vurdert", text, delivery.getGradingAttachment());
         }
         
         ModelAndView mav = makeModelAndView(courseName, delivery, new AdminDeliveryCommand());
         mav.addObject("status", "Endringene ble lagret.");
         return mav;
     }
 
     private ModelAndView makeModelAndView(String courseName, Delivery delivery, AdminDeliveryCommand cmd) {
         User student = delivery.getDeliveredBy();
         ModelAndView mav = new ModelAndView(PATH);
         mav.addObject("courseName", courseName);
         mav.addObject("student", student);
         mav.addObject("delivery", delivery);
         mav.addObject("admins", adminManager.getAdmins());
        mav.addObject("existingAttachment", delivery.getGradingAttachment().getName());
         mav.addObject("command", cmd);
         return mav;
     }
 
 }
