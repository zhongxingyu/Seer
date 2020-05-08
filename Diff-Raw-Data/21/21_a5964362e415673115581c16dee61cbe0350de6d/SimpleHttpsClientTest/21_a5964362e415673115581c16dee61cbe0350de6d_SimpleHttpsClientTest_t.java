 /*
  * Copyright (C) 2013 UniCoPA
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package unicopa.copa.client;
 
 import java.io.File;
 import java.io.FileReader;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Properties;
 import java.util.Set;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.TimeoutException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import unicopa.copa.base.com.request.AbstractRequest;
 import unicopa.copa.base.com.request.AbstractResponse;
 import unicopa.copa.base.com.request.GetSingleEventRequest;
 import unicopa.copa.base.com.request.GetSingleEventResponse;
 import unicopa.copa.base.com.exception.APIException;
 import unicopa.copa.base.com.exception.InternalErrorException;
 import unicopa.copa.base.com.exception.PermissionException;
 import unicopa.copa.base.com.exception.RequestNotPracticableException;
 import unicopa.copa.base.com.request.AddSingleEventRequest;
 import unicopa.copa.base.com.request.AddSingleEventResponse;
 import unicopa.copa.base.com.request.CancelSingleEventRequest;
 import unicopa.copa.base.com.request.GetCategoriesRequest;
 import unicopa.copa.base.com.request.GetCategoriesResponse;
 import unicopa.copa.base.com.request.GetCurrentSingleEventsRequest;
 import unicopa.copa.base.com.request.GetEventGroupRequest;
 import unicopa.copa.base.com.request.GetEventGroupsRequest;
 import unicopa.copa.base.com.request.GetEventGroupsResponse;
 import unicopa.copa.base.com.request.GetEventRequest;
 import unicopa.copa.base.event.SingleEvent;
 
 /**
  * Test the CoPA system by testing the requests from an automated client.
  * 
  * @author Felix Wiemuth
  */
 public class SimpleHttpsClientTest {
 
     private SimpleHttpsClient httpsClient;
     private SimpleDateFormat sdf = new SimpleDateFormat("yyy-MM-dd hh:mm");
 
     @Before
     public void setUp() throws Exception {
 	File propertiesFile = new File("settings.properties");
 	if (!propertiesFile.exists()) {
 	    throw new Exception(
 		    "Cannot run test: File \"settings.properties\" is needed.");
 	}
 	Properties p = new Properties();
 	p.load(new FileReader(propertiesFile));
 	Set<String> stringPropertyNames = p.stringPropertyNames();
 	httpsClient = new SimpleHttpsClient(p.getProperty("loginURL"),
 		p.getProperty("requestURL"), true);
 	httpsClient.start();
 	httpsClient.authenticate(p.getProperty("user"), p.getProperty("pwd"));
     }
 
     @After
     public void tearDown() {
        try {
            httpsClient.stop();
        } catch (Exception ex) {
            Logger.getLogger(SimpleHttpsClientTest.class.getName()).log(Level.SEVERE, null, ex);
        }
     }
 
     @Test
     public void testGetEventGroupRequest() {
 	doRequest(new GetEventGroupRequest(2));
     }
 
     @Test
     public void testGetEventRequest() {
 	doRequest(new GetEventRequest(2));
     }
 
     @Test
     public void testGetSingleEventRequest() {
 
 	doRequest(new GetSingleEventRequest(42));
 	doRequest(new GetSingleEventRequest(0));
 	doRequest(new GetSingleEventRequest(-3));
 
 	GetSingleEventResponse response = (GetSingleEventResponse) doRequest(new GetSingleEventRequest(
 		7));
 	SingleEvent sev = response.getSingleEvent();
 	System.out.println("Received SingleEvent: DATE="
 		+ (sev == null ? null : sev.getDate()) + " ROOM="
 		+ sev.getLocation());
     }
 
     @Test
     public void testGetCurrentSingleEventsRequest() {
 	doRequest(new GetCurrentSingleEventsRequest(3, new Date(2000)));
     }
 
     @Test
     public void testAddSingleEventRequest() throws ParseException {
 	System.out.println("Add a SingleEvent");
 	SingleEvent s = new SingleEvent(1, 2, "room A",
 		sdf.parse("2013-06-20 09:00"), "Supervisor X", 30);
 	AddSingleEventResponse response = (AddSingleEventResponse) doRequest(new AddSingleEventRequest(
 		s, "created by CopaTestClient"));
 	System.out.println("Resulting ID: " + response.getSingleEventID());
     }
 
     @Test
     public void testCancelSingleEventRequest() {
 	doRequest(new CancelSingleEventRequest(7, "cancelled by CopaTestClient"));
     }
 
     @Test
     public void testGetCategoriesRequest() {
 	GetCategoriesResponse response = (GetCategoriesResponse) doRequest(new GetCategoriesRequest());
     }
 
     @Test
     public void testGetEventGroupsRequest() {
 	GetEventGroupsResponse response = (GetEventGroupsResponse) doRequest(new GetEventGroupsRequest(
 		7, null));
     }
 
     public AbstractResponse doRequest(AbstractRequest request) {
 	try {
 	    return httpsClient.sendRequest(request);
 	} catch (InterruptedException ex) {
	    Logger.getLogger(SimpleHttpsClientTest.class.getName()).log(Level.SEVERE, null, ex);
 	} catch (TimeoutException ex) {
	    Logger.getLogger(SimpleHttpsClientTest.class.getName()).log(Level.SEVERE, null, ex);
 	} catch (ExecutionException ex) {
	    Logger.getLogger(SimpleHttpsClientTest.class.getName()).log(Level.SEVERE, null, ex);
 	} catch (APIException ex) {
 	    System.out.println("Exception from server: " + ex.getMessage());
 	} catch (PermissionException ex) {
 	    System.out.println("Exception from server: " + ex.getMessage());
 	} catch (RequestNotPracticableException ex) {
 	    System.out.println("Exception from server: " + ex.getMessage());
 	} catch (InternalErrorException ex) {
 	    System.out.println("Exception from server: " + ex.getMessage());
 	}
 	return null;
     }
 }
