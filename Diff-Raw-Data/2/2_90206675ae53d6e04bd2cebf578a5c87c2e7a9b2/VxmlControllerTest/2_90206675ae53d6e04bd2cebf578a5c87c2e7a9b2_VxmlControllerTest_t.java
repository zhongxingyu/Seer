 /**
  * MOTECH PLATFORM OPENSOURCE LICENSE AGREEMENT
  *
  * Copyright (c) 2011 Grameen Foundation USA.  All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the documentation
  * and/or other materials provided with the distribution.
  *
  * 3. Neither the name of Grameen Foundation USA, nor its respective contributors
  * may be used to endorse or promote products derived from this software without
  * specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY GRAMEEN FOUNDATION USA AND ITS CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
  * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED.  IN NO EVENT SHALL GRAMEEN FOUNDATION USA OR ITS CONTRIBUTORS
  * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING
  * IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY
  * OF SUCH DAMAGE.
  */
 package org.motechproject.server.appointmentreminder.web;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.motechproject.appointmentreminder.dao.PatientDAO;
 import org.motechproject.appointmentreminder.model.Appointment;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.util.Date;
 
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Mockito.when;
 
 /**
  *
  */
 @RunWith(MockitoJUnitRunner.class)
 public class VxmlControllerTest {
 
 
     @InjectMocks
     VxmlController vxmlController = new VxmlController();
 
     @Mock
     private PatientDAO patientDAO;
 
     @Mock
     private HttpServletRequest request;
 
     @Mock
     HttpServletResponse response;
 
     @Before
     public void initMocks() {
 
         MockitoAnnotations.initMocks(this);
      }
 
     @Test
     public void testAppointmentReminder () {
 
         String appointmentId = "aID";
 
         Appointment appointment = new Appointment();
         Date appointmentDate = new Date(12345L);
         appointment.setReminderWindowEnd(appointmentDate);
 
         when(request.getParameter("a")).thenReturn(appointmentId);
         when(patientDAO.getAppointment(appointmentId)).thenReturn(appointment);
 
         ModelAndView modelAndView = vxmlController.ar(request, response);
 
         assertEquals("ar", modelAndView.getViewName());
 
         assertEquals(appointmentDate, modelAndView.getModelMap().get("appointmentDueDate"));
 
     }
 
     @Test
     public void testAppointmentReminderNoId () {
 
         when(request.getParameter("a")).thenReturn(null);
 
         ModelAndView modelAndView = vxmlController.ar(request, response);
 
         assertEquals("ar_default", modelAndView.getViewName());
 
     }
 
     @Test
     public void testAppointmentReminderNoAppointment () {
 
         String appointmentId = "nID";
 
         when(request.getParameter("a")).thenReturn(appointmentId);
         when(patientDAO.getAppointment(appointmentId)).thenReturn(null);
 
         ModelAndView modelAndView = vxmlController.ar(request, response);
 
         assertEquals("ar_error", modelAndView.getViewName());
 
     }
 
     @Test
     public void testAppointmentReminderGetAppointmentException () {
 
         String appointmentId = "eID";
 
         Appointment appointment = new Appointment();
 
        when(request.getParameter("a")).thenReturn(appointmentId);
         when(patientDAO.getAppointment(appointmentId)).thenThrow(new RuntimeException());
 
         ModelAndView modelAndView = vxmlController.ar(request, response);
 
         assertEquals("ar_default", modelAndView.getViewName());
 
     }
 
 
 
 }
