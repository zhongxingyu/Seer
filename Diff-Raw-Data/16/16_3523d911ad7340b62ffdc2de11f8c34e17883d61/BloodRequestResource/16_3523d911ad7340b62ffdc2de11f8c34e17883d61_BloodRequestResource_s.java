 package org.bloodtorrent.resources;
 
 import com.yammer.dropwizard.hibernate.UnitOfWork;
 import com.yammer.dropwizard.views.View;
 import org.bloodtorrent.IllegalDataException;
 import org.bloodtorrent.ResourceManager;
 import org.bloodtorrent.dto.BloodRequest;
 import org.bloodtorrent.dto.User;
 import org.bloodtorrent.repository.BloodRequestRepository;
 import org.bloodtorrent.view.BloodRequestView;
 import org.bloodtorrent.view.CommonView;
 import org.bloodtorrent.view.ResultView;
 
 import javax.validation.ConstraintViolation;
 import javax.validation.Validation;
 import javax.validation.Validator;
 import javax.validation.ValidatorFactory;
 import javax.ws.rs.*;
 import javax.ws.rs.core.MediaType;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: sds
  * Date: 13. 3. 19
  * Time: 오전 7:55
  * To change this template use File | Settings | File Templates.
  */
 @Path("/requestForBlood")
 @Produces(MediaType.TEXT_HTML)
 public class BloodRequestResource {
     private final BloodRequestRepository repository;
 
     public BloodRequestResource(BloodRequestRepository repository) {
         this.repository = repository;
     }
 
     @GET
     @UnitOfWork
     public BloodRequestView forwardBloodRequestForm() {
         return new BloodRequestView();
     }
 
     @POST
     @UnitOfWork
     public View requestForBlood(
             @FormParam("firstName") String firstName,
             @FormParam("lastName") String lastName,
             @FormParam("hospitalAddress") String hospitalAddress,
             @FormParam("city") String city,
             @FormParam("state") String state,
             @FormParam("phone") String phone,
             @FormParam("email") String email,
             @FormParam("gender") String gender,
             @FormParam("birthday") String birthday,
             @FormParam("bloodGroup") String bloodGroup,
             @FormParam("bloodVolume") String bloodVolume,
             @FormParam("requesterType") String requesterType) throws IllegalDataException {
         Calendar cal = null;
         BloodRequest bloodRequest = new BloodRequest();
         bloodRequest.setFirstName(firstName);
         bloodRequest.setLastName(lastName);
         bloodRequest.setHospitalAddress(hospitalAddress);
         bloodRequest.setCity(city);
         bloodRequest.setState(state);
         bloodRequest.setPhone(phone);
         bloodRequest.setEmail(email);
         bloodRequest.setGender(gender);
 
         setBirthday(birthday, bloodRequest);
 
         bloodRequest.setBloodGroup(bloodGroup);
         bloodRequest.setBloodVolume(bloodVolume);
         bloodRequest.setRequesterType(requesterType);
         bloodRequest.setDate(new Date());
         bloodRequest.setValidated("N");
         bloodRequest.setId("" + System.currentTimeMillis());
 
         ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
         Validator validator = factory.getValidator();
         Set<ConstraintViolation<BloodRequest>> constraintViolations = validator.validate(bloodRequest);
 
         if(constraintViolations.size() > 0){
             List<String > messages = new ArrayList<String>();
             for(ConstraintViolation constraintViolation :constraintViolations){
                 messages.add(constraintViolation.getMessage()) ;
             }
             return new ResultView("fail", messages);
         }else{
             createNewBloodRequest(bloodRequest);
             FindingMatchingDonorResource resource = ResourceManager.getInstance().find(FindingMatchingDonorResource.class).get();
            resource.findMatchingDonors(bloodRequest);
 
             return new CommonView("/ftl/thankyou.ftl");
         }
 
     }
 
     private void setBirthday(String birthday, BloodRequest bloodRequest) {
         Calendar cal;
         if (birthday != null && birthday.trim().length() > 0) {
             String[] birth = birthday.split("-");
             cal = Calendar.getInstance();
             cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(birth[0]));
             cal.set(Calendar.MONTH, Integer.parseInt(birth[1]));
             cal.set(Calendar.YEAR, Integer.parseInt(birth[2]));
             bloodRequest.setBirthday(cal.getTime());
         }
     }
 
     public void createNewBloodRequest(BloodRequest bloodRequest) {
         repository.insert(bloodRequest);
     }
 }
