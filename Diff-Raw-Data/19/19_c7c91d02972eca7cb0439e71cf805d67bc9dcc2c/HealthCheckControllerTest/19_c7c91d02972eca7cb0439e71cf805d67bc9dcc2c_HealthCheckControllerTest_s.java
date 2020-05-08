 /**
  * Copyright (c) 2012, 2013 SURFnet BV
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  *
  *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  *     disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided with the distribution.
  *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
  *     derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package nl.surfnet.bod.web;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.contains;
 import static org.hamcrest.Matchers.containsString;
 import static org.hamcrest.Matchers.hasItems;
 import static org.hamcrest.Matchers.is;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verifyZeroInteractions;
 import static org.mockito.Mockito.when;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import javax.servlet.http.HttpServletResponse;
 
 import nl.surfnet.bod.idd.IddClient;
 import nl.surfnet.bod.nbi.NbiClient;
 import nl.surfnet.bod.service.GroupService;
 import nl.surfnet.bod.service.InstituteService;
 import nl.surfnet.bod.service.VersReportingService;
 import nl.surfnet.bod.support.ModelStub;
 import nl.surfnet.bod.util.Environment;
 import nl.surfnet.bod.web.HealthCheckController.ServiceCheck;
 import nl.surfnet.bod.web.HealthCheckController.ServiceCheckResult;
 import nl.surfnet.bod.web.HealthCheckController.ServiceState;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.slf4j.helpers.MarkerIgnoringBase;
 import org.slf4j.helpers.MessageFormatter;
 import org.slf4j.helpers.NOPLoggerFactory;
 import org.springframework.mock.web.MockHttpServletResponse;
 
 @RunWith(MockitoJUnitRunner.class)
 public class HealthCheckControllerTest {
 
   @InjectMocks
   private HealthCheckController subject;
 
   @Mock private IddClient iddClientMock;
   @Mock private NbiClient nbiClientMock;
   @Mock private VersReportingService verseReportingService;
   @Mock private GroupService openSocialGroupService;
   @Mock private GroupService sabGroupService;
   @Mock private InstituteService instituteService;
   @Mock private Environment bodEnvironment;
 
   private org.springframework.core.env.Environment springEnvironment;
 
   @Before
   public void before() throws Exception {
     springEnvironment = mock(org.springframework.core.env.Environment.class);
    this.subject.setEnvironment(springEnvironment);
     subject.afterPropertiesSet();
   }
 
   private HttpServletResponse httpServletResponse = new MockHttpServletResponse();
 
   @SuppressWarnings("unchecked")
   @Test
   public void should_have_status_200_when_all_checks_pass() throws Exception {
     suppressErrorOutput();
     ModelStub model = new ModelStub();
     ServiceCheck check = mock(ServiceCheck.class);
     subject.setChecks(Collections.singletonList(check));
 
     when(check.getName()).thenReturn("mock check");
     when(check.healthy()).thenReturn(ServiceState.SUCCEEDED);
 
     subject.index(model, httpServletResponse);
 
     assertThat(httpServletResponse.getStatus(), is(HttpServletResponse.SC_OK));
     assertThat((Iterable<ServiceCheckResult>)model.asMap().get("systems"), contains(new ServiceCheckResult("mock check", ServiceState.SUCCEEDED)));
   }
 
   @SuppressWarnings("unchecked")
   @Test
   public void should_have_status_500_when_at_least_one_check_fails() throws Exception {
     suppressErrorOutput();
     ModelStub model = new ModelStub();
     ServiceCheck check = mock(ServiceCheck.class);
     subject.setChecks(Collections.singletonList(check));
 
     when(check.getName()).thenReturn("mock check");
     when(check.healthy()).thenReturn(ServiceState.FAILED);
 
     subject.index(model, httpServletResponse);
 
     assertThat(httpServletResponse.getStatus(), is(HttpServletResponse.SC_INTERNAL_SERVER_ERROR));
     assertThat((Iterable<ServiceCheckResult>)model.asMap().get("systems"), contains(new ServiceCheckResult("mock check", ServiceState.FAILED)));
   }
 
   @SuppressWarnings("unchecked")
   @Test
   public void whenHealthCheckFailsShouldLog() {
     ModelStub model = new ModelStub();
     ErrorLogger logger = new ErrorLogger();
     subject.setLogger(logger);
 
     when(bodEnvironment.isSabEnabled()).thenReturn(true);
 
     subject.index(model, httpServletResponse);
 
     assertThat(logger.getErrorMessages(), hasItems(
         containsString("IDD"),
         containsString("NBI"),
         containsString("OAuth"),
         containsString("API"),
         containsString("SAB")));
   }
 
   @Test
   public void nagiosCheckShouldIgnoreVers(){
     subject.alivePage(httpServletResponse);
     verifyZeroInteractions(verseReportingService);
   }
 
   private void suppressErrorOutput() {
     subject.setLogger(new NOPLoggerFactory().getLogger(""));
   }
 
   @SuppressWarnings("serial")
   private class ErrorLogger extends MarkerIgnoringBase {
 
     private final List<String> errorMessages = new ArrayList<>();
 
     public Collection<String> getErrorMessages() {
       return errorMessages;
     }
 
     @Override
     public synchronized void error(String format, Object... arguments) {
       errorMessages.add(MessageFormatter.format(format, arguments).getMessage());
     }
 
     @Override
     public synchronized void error(String format, Object arg) {
       errorMessages.add(MessageFormatter.format(format, arg).getMessage());
     }
 
     @Override
     public void error(String msg, Throwable t) {
       errorMessages.add(msg);
     }
 
     @Override
     public void error(String msg) {
     }
 
     @Override
     public void error(String format, Object arg1, Object arg2) {
     }
 
     @Override
     public boolean isTraceEnabled() {
       return false;
     }
 
     @Override
     public void trace(String msg) {
     }
 
     @Override
     public void trace(String format, Object arg) {
     }
 
     @Override
     public void trace(String format, Object arg1, Object arg2) {
     }
 
     @Override
     public void trace(String format, Object... arguments) {
     }
 
     @Override
     public void trace(String msg, Throwable t) {
     }
 
     @Override
     public boolean isDebugEnabled() {
       return false;
     }
 
     @Override
     public void debug(String msg) {
     }
 
     @Override
     public void debug(String format, Object arg) {
     }
 
     @Override
     public void debug(String format, Object arg1, Object arg2) {
     }
 
     @Override
     public void debug(String format, Object... arguments) {
     }
 
     @Override
     public void debug(String msg, Throwable t) {
     }
 
     @Override
     public boolean isInfoEnabled() {
       return false;
     }
 
     @Override
     public void info(String msg) {
     }
 
     @Override
     public void info(String format, Object arg) {
     }
 
     @Override
     public void info(String format, Object arg1, Object arg2) {
     }
 
     @Override
     public void info(String format, Object... arguments) {
     }
 
     @Override
     public void info(String msg, Throwable t) {
     }
 
     @Override
     public boolean isWarnEnabled() {
       return false;
     }
 
     @Override
     public void warn(String msg) {
     }
 
     @Override
     public void warn(String format, Object arg) {
     }
 
     @Override
     public void warn(String format, Object... arguments) {
     }
 
     @Override
     public void warn(String format, Object arg1, Object arg2) {
     }
 
     @Override
     public void warn(String msg, Throwable t) {
     }
 
     @Override
     public boolean isErrorEnabled() {
       return false;
     }
 
   }
 }
