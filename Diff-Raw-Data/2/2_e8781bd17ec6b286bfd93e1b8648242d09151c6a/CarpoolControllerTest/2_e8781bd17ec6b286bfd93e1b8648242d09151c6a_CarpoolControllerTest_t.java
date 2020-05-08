 package smartpool.web;
 
 import org.joda.time.LocalDate;
 import org.joda.time.LocalTime;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.springframework.ui.ModelMap;
 import org.springframework.validation.BindingResult;
 import smartpool.builder.CarpoolBuilder;
 import smartpool.domain.*;
 import smartpool.service.*;
 import smartpool.web.form.CreateCarpoolForm;
 import smartpool.web.form.CreateCarpoolFormValidator;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.core.IsEqual.equalTo;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertThat;
 import static org.junit.matchers.JUnitMatchers.hasItem;
 import static org.junit.matchers.JUnitMatchers.hasItems;
 import static org.mockito.Mockito.*;
 
 @RunWith(MockitoJUnitRunner.class)
 public class CarpoolControllerTest {
 
     private CarpoolController carpoolController;
     @Mock
     private CarpoolService carpoolService;
     @Mock
     private HttpServletRequest request;
     @Mock
     private BuddyService buddyService;
     @Mock
     private RouteService routeService;
     @Mock
     private CreateCarpoolFormValidator createCarpoolFormValidator;
     @Mock
     private CreateCarpoolForm createCarpoolForm;
     @Mock
     private BindingResult errors;
     @Mock
     private JoinRequestService joinRequestService;
 
     private ModelMap model;
 
 
 
     private Carpool expectedCarpool = CarpoolBuilder.CARPOOL_1;
     private ArrayList<Carpool> defaultCarpools;
 
 
     private final CarpoolBuddy testBuddy = new CarpoolBuddy(new Buddy("testBuddy"),"location",new LocalTime(10,30));
     private List<String> defaultRouteLocations;
 
     @Before
     public void setUp() throws Exception {
 
         carpoolController = new CarpoolController(carpoolService,joinRequestService,buddyService, routeService, createCarpoolFormValidator);
         when(carpoolService.getByName("carpool")).thenReturn(expectedCarpool);
         model = new ModelMap();
 
         defaultCarpools = new ArrayList<Carpool>() {{
             add(expectedCarpool);
         }};
         when(carpoolService.findAllCarpoolsByLocation("Diamond District")).thenReturn(defaultCarpools);
 
         when(buddyService.getCurrentBuddy(request)).thenReturn(testBuddy.getBuddy());
 
         when(request.getParameter("query")).thenReturn("Diamond District");
 
         when(createCarpoolForm.getDomainObject(testBuddy.getBuddy())).thenReturn(expectedCarpool);
 
         defaultRouteLocations = Arrays.asList("Diamond District");
     }
 
     @Test
     public void shouldRedirectToViewCarpool() throws Exception {
         assertEquals(carpoolController.viewCarpool("carpool", model, request), "carpool/view");
     }
 
     @Test
     public void shouldHaveCarpoolInstanceInModel() throws Exception {
         carpoolController.viewCarpool("carpool", model, request);
         Carpool carpoolActual = (Carpool) model.get("carpool");
 
         assertEquals(expectedCarpool, carpoolActual);
     }
 
     @Test
     public void shouldSearchForCarpool() {
         carpoolController.searchByLocation(model, request);
         List<Carpool> searchResult = (List<Carpool>) model.get("searchResult");
         assertThat(searchResult, hasItems(expectedCarpool));
     }
 
     @Test
     public void shouldRedirectToViewSearchCarpool() throws Exception {
         assertThat(carpoolController.searchByLocation(model, request), equalTo("carpool/search"));
     }
 
     @Test
     public void shouldRedirectToCreateCarpool() throws Exception {
         assertThat(carpoolController.create(), equalTo("carpool/create"));
     }
 
     @Test
     public void shouldRedirectToViewCarpoolWhenPostedOnCreate(){
         assertThat(carpoolController.create(new CreateCarpoolForm("from", "to", "15/06/2012", "pickupPoint", "9:00", "PERSONAL", "0", "10:00", "18:00", "Kormangla"), errors, model, request), equalTo("redirect:/carpool/from - to"));
     }
 
     @Test
     public void shouldInsertIntoDBWhenPostedOnCreate() throws Exception {
         ArrayList<CarpoolBuddy> carpoolBuddies = new ArrayList<CarpoolBuddy>();
         carpoolBuddies.add(new CarpoolBuddy(testBuddy.getBuddy(),"pickupPoint",new LocalTime(9,0)));
 
         ArrayList<String> routePoints = new ArrayList<String>();
         routePoints.add("Kormangla");
         routePoints.add("Domlur");
 
         Carpool carpool = new Carpool("from - to",new LocalDate(2012,6,15), CabType.PERSONAL,0,new LocalTime(10,0),new LocalTime(18,0),Status.NOT_STARTED, carpoolBuddies,0, routePoints);
         carpoolController.create(new CreateCarpoolForm("from", "to", "15/06/2012", "pickupPoint", "9:00", "PERSONAL", "0", "10:00", "18:00", "Kormangla, Domlur"), errors, model, request);
 
         verify(carpoolService).insert(carpool);
     }
 
     @Test
     public void shouldDisplayAllCarpoolsIfQueryIsNull() {
         when(carpoolService.findAllCarpoolsByLocation(null)).thenReturn(defaultCarpools);
         when(request.getParameter("query")).thenReturn(null);
         carpoolController.searchByLocation(model, request);
         assertThat((ArrayList<Carpool>) model.get("searchResult"), is(defaultCarpools));
     }
 
     @Test
     public void shouldAddCurrentBuddyToCarpoolWhileCreating() throws Exception {
         when(createCarpoolForm.getDomainObject(testBuddy.getBuddy())).thenReturn(new Carpool("testName"));
         carpoolController.create(createCarpoolForm, errors, model, request);
         verify(createCarpoolForm).getDomainObject(testBuddy.getBuddy());
     }
 
     @Test
     public void shouldValidateFormBeforeCreatingCarpool() throws Exception {
         carpoolController.create(createCarpoolForm,errors ,model,request);
         verify(createCarpoolFormValidator).validate(createCarpoolForm,errors);
     }
 
     @Test
     public void shouldRedirectToCreatePageIfValidationhasErrors() throws Exception {
         when(errors.hasErrors()).thenReturn(true);
         String redirectPage = carpoolController.create(createCarpoolForm,errors ,model,request);
         assertThat(redirectPage,is("carpool/create"));
     }
 
     @Test
     public void shouldGiveBackTheFormInModelWhenValidationHasErrors() throws Exception {
         when(errors.hasErrors()).thenReturn(true);
         carpoolController.create(createCarpoolForm, errors,model,request);
         assertThat((CreateCarpoolForm) model.get("createCarpoolForm"),is(createCarpoolForm));
     }
 
     @Test
     public void shouldGetAllRoutePoints() throws Exception {
         when(routeService.getAllLocation()).thenReturn(defaultRouteLocations);
         when(request.getParameter("query")).thenReturn(null);
         carpoolController.searchByLocation(model, request);
         assertThat((List<String>) model.get("routePoints"), is(defaultRouteLocations));
     }
 
     @Test
     public void shouldGetDashboardURL() throws Exception {
        assertThat(carpoolController.viewDashboard(model, request),is("admin/dashboard"));
     }
 
     @Test
     public void shouldGetCarpoolForDashboard() throws Exception {
         carpoolController.viewDashboard(model, request);
         assertThat((List<Carpool>)model.get("searchResult"),hasItem(expectedCarpool));
     }
 }
