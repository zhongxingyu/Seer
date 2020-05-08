 package com.thoughtworks.twu.controller;
 
 import com.thoughtworks.twu.domain.Offer;
 import com.thoughtworks.twu.service.OfferService;
 import com.thoughtworks.twu.service.OfferServiceInterface;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.mock.web.MockHttpServletRequest;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.transaction.TransactionConfiguration;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpSession;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Mockito.*;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:applicationContext.xml"})
 @TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
 @Transactional
 public class CreateOfferControllerTest {
 
     @Autowired
     OfferServiceInterface offerServiceInterface;
 
     private OfferServiceInterface mockOfferService;
     private CreateOfferController createOfferController;
     private String title;
     private String category;
     private String description;
     private String owner;
     private MockHttpServletRequest request;
     private HttpSession session;
     private HomeController homeController;
 
     @Before
     public void setUp() {
         mockOfferService = mock(OfferService.class);
         createOfferController = new CreateOfferController(mockOfferService);
         title = "this is a title";
         category = "Cars";
         description = "there is some descriptions";
         owner = "Qiushi";
         homeController = new HomeController();
         request = new MockHttpServletRequest();
         session = request.getSession();
     }
 
     @Test
     public void shouldGoToCreateOffer() {
         String actualUrl = createOfferController.goToCreatingOfferPage();
         String expectedUrl = "home/createOffer";
 
         assertThat(expectedUrl, is(actualUrl));
     }
 
     @Test
     public void shouldSaveOfferCorrectly() {
         createAnOffer();
         Offer offer = new Offer(title, description, category, owner);
 
         verify(mockOfferService).saveOffer(offer);
     }
 
     @Test
     public void shouldReturnTheCorrectUrlToDisplayOfferPageAfterCreatingOffer() {
         createAnOffer();
         String actualUrl = createOfferController.viewAnOfferAfterCreating(new ModelMap());
         String expectedUrl = "home/viewAnOffer";
 
         assertThat(expectedUrl, is(actualUrl));
     }
 
     @Test
     public void shouldReturnTheCorrectUrlToDisplayOfferFromBrowse() {
         createAnOffer();
         String actualUrl = createOfferController.viewAnOfferFromBrowse(new ModelMap(), "");
         String expectedUrl = "home/viewAnOffer";
 
         assertThat(expectedUrl, is(actualUrl));
     }
 
     @Test
     public void shouldReturnTheCorrectAttributeFromModelMapAfterCreatingOffer() {
         createAnOffer();
         ModelMap modelMap = new ModelMap();
         createOfferController.viewAnOfferAfterCreating(modelMap);
 
         assertTrue(modelMap.containsAttribute("theOffer"));
     }
 
     @Test
     public void shouldReturnTheCorrectAttributeFromModelMapFromBrowse() {
         createAnOffer();
         ModelMap modelMap = new ModelMap();
         createOfferController.viewAnOfferAfterCreating(modelMap);
 
         assertTrue(modelMap.containsAttribute("theOffer"));
     }
 
     @Test
     public void shouldExposeAllOffersToBrowseView() throws Exception {
 
         OfferService offerService = mock(OfferService.class);
         CreateOfferController createOfferController = new CreateOfferController(offerService);
 
        List<Offer> expectedOffers = new ArrayList<>();
 
         Offer firstOffer = new Offer("Title 1", "Category 1", "Description 1", "Me");
         Offer secondOffer = new Offer("Title 2", "Category 2", "Description 2", "You");
         Offer thirdOffer = new Offer("Title 3", "Category 3", "Description 3", "Someone else");
 
         expectedOffers.add(thirdOffer);
         expectedOffers.add(secondOffer);
         expectedOffers.add(firstOffer);
 
         when(offerService.getAll()).thenReturn(expectedOffers);
         ModelAndView modelAndView = createOfferController.browse(request, session);
         Map<String, Object> model = modelAndView.getModel();
 
         List<Offer> actualOffers = (List<Offer>) model.get("allOffers");
 
         assertThat(expectedOffers, is(actualOffers));
     }
 
     @Test
     public void shouldSetUsernameCorrectly() {
         request.setRemoteUser("Fernando");
         homeController.goToHomepage(request, session);
         assertThat(session.getAttribute("username").toString(), is("Fernando"));
     }
 
     private void createAnOffer() {
         HttpSession session1 = mock(HttpSession.class);
         when(session1.getAttribute("username")).thenReturn("Qiushi");
         MockHttpServletRequest request1 = new MockHttpServletRequest();
         request1.setSession(session1);
 
         createOfferController.createOffer(title, category, description, request1);
     }
 
 }
