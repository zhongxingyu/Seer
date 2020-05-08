 /*
  * The contents of this file are subject to the OpenMRS Public License
  * Version 1.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at
  * http://license.openmrs.org
  *
  * Software distributed under the License is distributed on an "AS IS"
  * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
  * License for the specific language governing rights and limitations
  * under the License.
  *
  * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
  */
 
 package org.openmrs.module.paperrecord;
 
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.openmrs.Location;
 import org.openmrs.Patient;
 import org.openmrs.Person;
 import org.openmrs.User;
 
 import java.util.Date;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 public class PaperRecordRequestTest {
 
     private String timeZoneCode;
 
     @Before
     public void setup() {
        timeZoneCode = new DateTime().getZone().getID();
     }
 
     @Test
     public void testUpdateStatusShouldUpdateStatusLastUpdated() throws InterruptedException {
 
         PaperRecordRequest request = new PaperRecordRequest();
 
         request.updateStatus(PaperRecordRequest.Status.OPEN);
         Date date = request.getDateStatusChanged();
         Assert.assertNotNull(date);
 
         Thread.sleep(1);
 
         request.updateStatus(PaperRecordRequest.Status.ASSIGNED_TO_CREATE);
         Date updatedDate = request.getDateStatusChanged();
         Assert.assertTrue(updatedDate.after(date));
 
     }
 
     @Test
     public void testToString() {
 
         PaperRecordRequest request = new PaperRecordRequest();
         request.setIdentifier("ABC");
         request.setId(1);
         request.setPatient(new Patient(1));
         request.setAssignee(new Person(1));
         User user = new User();
         user.setUsername("username");
         request.setCreator(user);
         request.setRecordLocation(new Location(1));
         request.setRequestLocation(new Location(2));
         request.updateStatus(PaperRecordRequest.Status.OPEN);
         request.setDateCreated(new DateTime(2012, 10, 10, 10, 10).toDate());
         request.setDateStatusChanged(new DateTime(2012, 9, 9, 9, 9).toDate());
 
         assertThat(request.toString(), is("Paper Record Request: [1 ABC Patient#1 1 2 OPEN Person(personId=1) username Wed Oct 10 10:10:00 "
                 + timeZoneCode + " 2012 Sun Sep 09 09:09:00 " + timeZoneCode +" 2012]"));
     }
 
     @Test
     public void testToStringWithNullValues() {
 
         PaperRecordRequest request = new PaperRecordRequest();
         request.updateStatus(null);
         request.setDateStatusChanged(null);
 
         assertThat(request.toString(), is("Paper Record Request: [(no id) null (no patient) (no record location) (no request location) (no status) (no assignee) (no creator) (no date created) (no date status changed)]"));
     }
 }
