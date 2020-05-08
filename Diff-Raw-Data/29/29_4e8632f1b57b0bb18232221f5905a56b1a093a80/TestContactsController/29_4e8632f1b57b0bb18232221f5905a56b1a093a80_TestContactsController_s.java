 package com.rolan.examples.spring.crudexample.controller;
 
 import com.rolan.examples.spring.crudexample.dao.ContactsDao;
 import com.rolan.examples.spring.crudexample.entity.Contact;
 import java.util.Collections;
 import java.util.List;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.web.servlet.MockMvc;
 import org.springframework.test.web.servlet.setup.MockMvcBuilders;
 
 import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
 import static org.mockito.Mockito.*;
 import static org.hamcrest.Matchers.*;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration
 public class TestContactsController {
     
     @Autowired
     private ContactsDao contactsDao;
     @Autowired
     private ContactsController contactsController;
     private MockMvc mockMvc;
     
     @Before
     public void setup() {
         mockMvc = MockMvcBuilders.standaloneSetup(contactsController).build();
     }
     
     @Test
     public void testDefaultPage() throws Exception {
         List<Contact> contactsList = Collections.<Contact>emptyList();
         when(contactsDao.getAllContacts()).thenReturn(contactsList);
         mockMvc.perform(post("/")).andExpect(status().isOk()).
                 andExpect(view().name("/contacts/showContacts")).
                 andExpect(model().attribute("contacts", contactsList));
     }
     
     @Test
     public void testViewAllContacts() throws Exception {
         List<Contact> contactsList = Collections.<Contact>emptyList();
         when(contactsDao.getAllContacts()).thenReturn(contactsList);
        mockMvc.perform(post("/")).andExpect(status().isOk()).
                 andExpect(view().name("/contacts/showContacts")).
                 andExpect(model().attribute("contacts", contactsList));
     }
     
     @Test
     public void testCreateContact() throws Exception {
         mockMvc.perform(get("/contacts/contactDetail")).
                 andExpect(status().isOk()).
                 andExpect(view().name("contacts/contactDetail")).
                 andExpect(model().attribute("contact", notNullValue()));
     }
     
     @Test
     public void testEditContact() throws Exception {
         Long contactId = 1L;
         Contact contact = new Contact();
         contact.setId(contactId);
         when(contactsDao.getContactById(contactId)).thenReturn(contact);
         mockMvc.perform(get("/contacts/contactDetail").param("id", Long.toString(contactId))).
                 andExpect(status().isOk()).
                 andExpect(view().name("contacts/contactDetail")).
                 andExpect(model().attribute("contact", contact));
     }
     
     @Test
    public void testEditedContact() throws Exception {
        Long contactId = 1L;
        Contact contact = new Contact();
        contact.setId(contactId);
        mockMvc.perform(post("/contacts/contactDetail").param("id", "1").param("name", "name")).
                 andExpect(status().isMovedTemporarily()).
                 andExpect(redirectedUrl("/contacts/viewAllContacts"));
     }
     
     @Configuration
     static class ContactsControllerTestConfiguration {
         
         @Bean
         public ContactsDao contactsDao() {
             return mock(ContactsDao.class);
         }
         
         @Bean
         ContactsController contactsController() throws NoSuchFieldException {
             return new ContactsController();
         }
     }
 }
