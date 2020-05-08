 package nl.enovation.addressbook.cqrs.webui.contacts;
 
 import nl.enovation.addressbook.cqrs.command.CreateContactCommand;
 import nl.enovation.addressbook.cqrs.query.ContactEntry;
 import nl.enovation.addressbook.cqrs.query.repositories.ContactQueryRepositoryImpl;
 
 import org.axonframework.commandhandling.CommandBus;
 import org.axonframework.domain.StringAggregateIdentifier;
 import org.axonframework.domain.UUIDAggregateIdentifier;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.MockitoAnnotations;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Matchers.anyList;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.verify;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = { "classpath:/META-INF/spring/persistence-infrastructure-context.xml",
                                    "classpath:/META-INF/spring/cqrs-infrastructure-context.xml", "classpath:/META-INF/spring/contacts-context.xml" })
 public class ContactsControllerIntegrationTest {
     @Mock
     private BindingResult mockBindingResult;
 
     @Autowired
     private CommandBus commandBus;
 
     @Autowired
     private ContactsController controller;
 
     @Autowired
     private ContactQueryRepositoryImpl contactQueryRepositoryImpl;
 
     @Mock
     private Model model;
 
     private ContactEntry contactEntry;
 
     @After
     public void after() {
         contactQueryRepositoryImpl.deleteAll();
     }
 
     @Before
     public void setUp() {
         MockitoAnnotations.initMocks(this);
         Mockito.when(mockBindingResult.hasErrors()).thenReturn(false);
 
         controller = new ContactsController(contactQueryRepositoryImpl, commandBus);
 
         contactEntry = new ContactEntry();
         contactEntry.setFirstName("Foo");
         contactEntry.setLastName("Bar");
         contactEntry.setIdentifier(new UUIDAggregateIdentifier().asString());
     }
 
     @Test
     public void testDeleteCommand_failure() {
         Mockito.when(mockBindingResult.hasErrors()).thenReturn(true);
 
         String view = controller.formDelete(contactEntry, mockBindingResult);
 
         // Check that we're shown the delete page again
         assertEquals("contacts/delete", view);
     }
 
     @Test
     public void testDeleteCommand_success() {
         CreateContactCommand setupCommand = new CreateContactCommand(new StringAggregateIdentifier(contactEntry.getIdentifier()), contactEntry);
         commandBus.dispatch(setupCommand);
 
         // contactQueryRepositoryImpl.save(contactEntry);
         assertEquals("ContactEntry should be retrievable from repository", contactEntry, contactQueryRepositoryImpl.findOne(contactEntry.getIdentifier()));
 
         String view = controller.formDelete(contactEntry, mockBindingResult);
         assertEquals("ContactEntry should have been removed", null, contactQueryRepositoryImpl.findOne(contactEntry.getIdentifier()));
 
         // Check that we returned back to the contact list
         assertEquals("redirect:/contacts", view);
     }
 
     @Test
     public void testDeleteForm() {
         String view = controller.formDelete(contactEntry.getIdentifier(), model);
 
         // Check that we're shown the delete page again
         assertEquals("contacts/delete", view);
     }
 
     @Test
     public void testDetails() {
         contactQueryRepositoryImpl.save(contactEntry);
         String view = controller.details(contactEntry.getIdentifier(), model);
 
         verify(model).addAttribute(eq("contact"), eq(contactEntry));
 
         // Check that we're shown the contact list view
         assertEquals("contacts/details", view);
     }
 
     @Test
     public void testListContacts() {
         String view = controller.list(model);
 
         verify(model).addAttribute(eq("contacts"), anyList());
 
         // Check that we're shown the contact list view
         assertEquals("contacts/list", view);
     }
 
     @Test
     public void testNew() {
         String view = controller.formNew(model);
 
         // Check that we're shown the contact list view
         assertEquals("contacts/new", view);
     }
 
     @Test
     public void testNewCommand_failure() {
         Mockito.when(mockBindingResult.hasErrors()).thenReturn(true);
 
         String view = controller.formNewSubmit(contactEntry, mockBindingResult);
 
         // Check that we'reback to the original form
         assertEquals("contacts/new", view);
     }
 
     @Test
     public void testNewCommand_success() {
         String view = controller.formNewSubmit(contactEntry, mockBindingResult);
 
         ContactEntry contactFromDb = contactQueryRepositoryImpl.findOne(contactEntry.getIdentifier());
         assertEquals(contactEntry, contactFromDb);
 
         // Check that we're back to the overview
        assertEquals("redirect:/contacts/"+contactEntry.getIdentifier(), view);
     }
 
     @Test
     public void testUpdateCommand_failure() {
         Mockito.when(mockBindingResult.hasErrors()).thenReturn(true);
 
         contactQueryRepositoryImpl.save(contactEntry);
         assertEquals("ContactEntry should be retrievable from repository", contactEntry, contactQueryRepositoryImpl.findOne(contactEntry.getIdentifier()));
 
         String view = controller.formEditSubmit(contactEntry, mockBindingResult);
 
         // Check that we'reback to the original form
         assertEquals("contacts/edit", view);
     }
 
     @Test
     public void testUpdateCommand_success() {
         CreateContactCommand setupCommand = new CreateContactCommand(new StringAggregateIdentifier(contactEntry.getIdentifier()), contactEntry);
         commandBus.dispatch(setupCommand);
 
         // contactEntry = contactQueryRepositoryImpl.save(contactEntry);
         assertEquals("ContactEntry should be retrievable from repository", contactEntry, contactQueryRepositoryImpl.findOne(contactEntry.getIdentifier()));
 
         contactEntry.setFirstName("changedFirstName");
         String view = controller.formEditSubmit(contactEntry, mockBindingResult);
 
         ContactEntry contactFromDb = contactQueryRepositoryImpl.findOne(contactEntry.getIdentifier());
         assertEquals("changedFirstName", contactFromDb.getFirstName());
 
         // Check that we're back to the overview
        assertEquals("redirect:/contacts/"+contactEntry.getIdentifier(), view);
     }
 
     @Test
     public void testUpdateForm() {
         contactQueryRepositoryImpl.save(contactEntry);
         assertEquals("ContactEntry should be retrievable from repository", contactEntry, contactQueryRepositoryImpl.findOne(contactEntry.getIdentifier()));
 
         String view = controller.formEdit(contactEntry.getIdentifier(), model);
 
         // Check that we'reback to the original form
         assertEquals("contacts/edit", view);
     }
 }
