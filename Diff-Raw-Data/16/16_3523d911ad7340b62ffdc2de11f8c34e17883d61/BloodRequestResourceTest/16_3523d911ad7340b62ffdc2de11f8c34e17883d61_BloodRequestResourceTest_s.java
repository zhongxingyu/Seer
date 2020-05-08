 package org.bloodtorrent.resources;
 
 import org.bloodtorrent.dto.BloodRequest;
 import org.bloodtorrent.repository.BloodRequestRepository;
 import org.junit.Assert;
 import org.junit.Test;
 import org.mockito.Mockito;
 
 import java.util.Date;
 
 /**
  * Created with IntelliJ IDEA.
  * User: sds
  * Date: 13. 3. 14
  * Time: 오후 1:56
  * To change this template use File | Settings | File Templates.
  */
 public class BloodRequestResourceTest {
     private final BloodRequestRepository repository = Mockito.mock(BloodRequestRepository.class);
     private final BloodRequestResource resource = new BloodRequestResource(repository);
 
     @Test
     public void resourceCallCheck() {
         Assert.assertNotNull(resource);
     }
 
     @Test
    public void createNewBloodReq(){
         BloodRequest bloodRequest = new BloodRequest();
 
         bloodRequest.setFirstName("BOWON");
         bloodRequest.setLastName("KIM");
         bloodRequest.setPhone("0118427020");
         bloodRequest.setEmail("bb@samsung.com");
         bloodRequest.setGender("M");
         bloodRequest.setBloodGroup("O+");
         bloodRequest.setBloodVolume("11");
         bloodRequest.setRequesterType("C");
         bloodRequest.setValidated("N");
         bloodRequest.setDate(new Date());
         bloodRequest.setState("texas");
         bloodRequest.setCity("seoul");
         bloodRequest.setHospitalAddress("gangnamgu 320-11");
         resource.requestForBlood(bloodRequest.getFirstName(), bloodRequest.getLastName(), bloodRequest.getHospitalAddress(),
                 bloodRequest.getCity(), bloodRequest.getState(), bloodRequest.getPhone(),
                 bloodRequest.getEmail(), bloodRequest.getGender(), "03-11-2012",
                 bloodRequest.getBloodGroup(), String.valueOf(bloodRequest.getBloodVolume()), bloodRequest.getRequesterType());
     }
 }
