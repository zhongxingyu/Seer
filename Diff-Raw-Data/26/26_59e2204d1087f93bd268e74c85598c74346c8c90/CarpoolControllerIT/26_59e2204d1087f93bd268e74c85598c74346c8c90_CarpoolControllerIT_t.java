 package smartpool.web;
 
 import org.junit.Test;
 import org.mockito.Mock;
 import org.springframework.ui.ModelMap;
 import smartpool.domain.Carpool;
 import smartpool.persistence.dao.*;
 import smartpool.service.*;
 import smartpool.web.form.CreateCarpoolFormValidator;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.List;
 
 import static org.junit.Assert.assertEquals;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 public class CarpoolControllerIT {
 
     @Mock
     HttpServletRequest request;
     @Mock
     MailService mailService;
 
     @Test
     public void shouldSearchForCarpool() {
         initMocks(this);
         CarpoolService carpoolService = new CarpoolService(
                 new CarpoolDao(), new BuddyDao(), new RouteDao(), new CarpoolBuddyDao());
         JoinRequestService joinRequestService = new JoinRequestService(
                 new JoinRequestDao(), new CarpoolBuddyDao(), mailService);
         BuddyService buddyService = new BuddyService(new BuddyDao());
         RouteService routeService = new RouteService(new RouteDao());
         CreateCarpoolFormValidator validator = new CreateCarpoolFormValidator();
         MailService mailService = new MailService(null);
        CarpoolBuddyService carpoolBuddyService = new CarpoolBuddyService(new CarpoolBuddyDao());
 
         CarpoolController carpoolController = new CarpoolController(
                 carpoolService,
                 joinRequestService,
                 buddyService,
                 routeService,
                 validator,
                mailService,
                carpoolBuddyService);
         ModelMap model = new ModelMap();
         request.setAttribute("query", "Sony Centre");
         carpoolController.searchByLocation(model, request);
         List<Carpool> searchResult = (List<Carpool>) model.get("searchResult");
         assertEquals(2, searchResult.size());
     }
 }
