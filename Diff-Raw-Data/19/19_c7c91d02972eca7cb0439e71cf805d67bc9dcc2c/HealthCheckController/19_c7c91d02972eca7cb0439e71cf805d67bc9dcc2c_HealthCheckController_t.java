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
 
 import static nl.surfnet.bod.web.HealthCheckController.ServiceState.DISABLED;
 import static nl.surfnet.bod.web.HealthCheckController.ServiceState.FAILED;
 import static nl.surfnet.bod.web.HealthCheckController.ServiceState.SUCCEEDED;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.Callable;
 import java.util.concurrent.CancellationException;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletResponse;
 
 import com.google.common.collect.Lists;
 
 import nl.surfnet.bod.idd.IddClient;
 import nl.surfnet.bod.nbi.NbiClient;
 import nl.surfnet.bod.nbi.onecontrol.NotificationSubscriber;
 import nl.surfnet.bod.service.GroupService;
 import nl.surfnet.bod.service.InstituteService;
 import nl.surfnet.bod.service.VersReportingService;
 import nl.surfnet.bod.util.Environment;
 
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.http.client.methods.CloseableHttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.CloseableHttpClient;
 import org.apache.http.impl.client.HttpClients;
 import org.joda.time.Duration;
 import org.joda.time.Instant;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.EnvironmentAware;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 @Controller
 public class HealthCheckController implements InitializingBean, EnvironmentAware {
 
   private Logger logger = LoggerFactory.getLogger(HealthCheckController.class);
 
   @Resource private IddClient iddClient;
   @Resource private InstituteService instituteService;
   @Resource private NbiClient nbiClient;
   @Resource private GroupService openSocialGroupService;
   @Resource private GroupService sabGroupService;
   @Resource private VersReportingService verseReportingService;
   @Resource(name = "bodEnvironment") private Environment environment;
 
   @Autowired(required = false)
   private NotificationSubscriber notificationSubscriber; // only when in onecontrol mode
 
   @Value("${healthcheck.timeout.seconds}")
   private int timeoutInSeconds;
 
   private org.springframework.core.env.Environment springEnvironment;
   private List<ServiceCheck> checks;
 
   private final ServiceCheck oneControlNotificationsCheck = new ServiceCheck() {
     @Override
     public ServiceState healthy() throws Exception {
       if (notificationSubscriber == null) {
         return ServiceState.DISABLED;
       } else{
         return notificationSubscriber.isHealthy() ? ServiceState.SUCCEEDED : FAILED;
       }
     }
 
     @Override
     public String getName() {
       return "OneControl Notification Consumer";
     }
   };
 
   private final ServiceCheck iddServiceCheck = new ServiceCheck() {
     @Override
     public ServiceState healthy() {
       Instant lastUpdatedAt = instituteService.instituteslastUpdatedAt();
       Duration timeout = environment.getInstituteCacheMaxAge();
       return (lastUpdatedAt != null && lastUpdatedAt.plus(timeout.getMillis()).isAfterNow()) ? SUCCEEDED : FAILED;
     }
 
     @Override
     public String getName() {
       return "IDD (using " + iddClient.getClass().getSimpleName() + ")";
     }
   };
 
   private final ServiceCheck nbiServiceCheck = new ServiceCheck() {
     @Override
     public ServiceState healthy() {
       return nbiClient.getPhysicalPortsCount() > 0 ? SUCCEEDED : FAILED;
     }
 
     @Override
     public String getName() {
       return "NBI (using " + nbiClient.getClass().getSimpleName() + ")";
     }
   };
 
   private final ServiceCheck oAuthServerServiceCheck = new ServiceCheck() {
     @Override
     public ServiceState healthy() throws IOException {
       HttpGet request = new HttpGet(environment.getOauthServerUrl() + "/admin");
       try (CloseableHttpClient client = HttpClients.createDefault();
           CloseableHttpResponse response = client.execute(request)) {
         return response.getStatusLine().getStatusCode() == HttpStatus.SC_FORBIDDEN ? SUCCEEDED : FAILED;
       }
     }
 
     @Override
     public String getName() {
       return "OAuth server";
     }
   };
 
   private final ServiceCheck apiServiceCheck = new ServiceCheck() {
     private static final String PERSON_URI = "urn:collab:person:surfnet.nl:hanst";
 
     @Override
     public ServiceState healthy() throws IOException {
       return openSocialGroupService.getGroups(PERSON_URI).size() > 0 ? SUCCEEDED : FAILED;
     }
 
     @Override
     public String getName() {
       return "API (groupService)";
     }
   };
 
   private final ServiceCheck sabServiceCheck = new ServiceCheck() {
     private static final String PERSON_URI = "urn:collab:person:surfnet.nl:hanst";
 
     @Override
     public ServiceState healthy() throws IOException {
       if (environment.isSabEnabled()) {
         return sabGroupService.getGroups(PERSON_URI).size() > 0 ? SUCCEEDED : FAILED;
       } else {
         return DISABLED;
       }
     }
 
     @Override
     public String getName() {
       return "SAB (groupService)";
     }
   };
 
   private final ServiceCheck versCheck = new ServiceCheck() {
     @Override
     public ServiceState healthy() throws IOException {
       if (environment.isVersEnabled()) {
         return verseReportingService.isWsdlAvailable() ? SUCCEEDED : FAILED;
       } else {
         return DISABLED;
       }
     };
 
     @Override
     public String getName() {
       return "VERS (reporting service)";
     }
   };
 
   /**
    * Reports the healthstate of the application to a monitoring toolsuite (nagios), not intended for human eyeballs.
    * Is aware of the current application profile (e.g. will not check for onecontrol-dependencies when running in opendrac mode)
    * @param httpServletResponse will be set to "200 OK" when all is healthy, "417 Expectation Failed" otherwise
    */
   @RequestMapping(value = "/healthcheck/alive")
   public void alivePage(HttpServletResponse httpServletResponse) {
     // exclude the VERS- reportingservice check, which is not vital
     List<Callable<ServiceCheckResult>> tasks = new ArrayList<>();
     for (ServiceCheck check : checks) {
       if (check.getName().contains("VERS") == false) {
         tasks.add(callable(check));
       }
     }
     HealthCheckResult healthCheckResult = performTasks(tasks);
 
     if (healthCheckResult.isAllOk()) {
       httpServletResponse.setStatus(HttpServletResponse.SC_OK);
     } else{
       httpServletResponse.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
     }
   }
 
   private HealthCheckResult performTasks(final List<Callable<ServiceCheckResult>> tasks) {
     boolean everythingOk = true;
     ExecutorService threadPool = Executors.newFixedThreadPool(tasks.size());
     List<ServiceCheckResult> systems = new ArrayList<>();
     try {
       List<Future<ServiceCheckResult>> futures = threadPool.invokeAll(tasks, timeoutInSeconds, TimeUnit.SECONDS);
       for (int i = 0; i < futures.size(); ++i) {
         systems.add(toState(checks.get(i).getName(), futures.get(i)));
       }
     } catch (InterruptedException e) {
       everythingOk = false;
       logger.error("Error during calling healthchecks", e);
     } finally {
       threadPool.shutdown();
     }
     for (ServiceCheckResult result : systems) {
       everythingOk &= result.state != ServiceState.FAILED;
     }
     return new HealthCheckResult(everythingOk, systems);
   }
 
   @RequestMapping(value = "/healthcheck")
   public String index(Model model, HttpServletResponse response) {
     List<Callable<ServiceCheckResult>> tasks = new ArrayList<>();
     for (ServiceCheck check : checks) {
       tasks.add(callable(check));
     }
 
     HealthCheckResult healthCheckResult = performTasks(tasks);
     model.addAttribute("systems", healthCheckResult.getResults());
     response.setStatus(healthCheckResult.allOk ? HttpServletResponse.SC_OK : HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
 
     return "healthcheck";
   }
 
   private ServiceCheckResult toState(String name, Future<ServiceCheckResult> healthFuture) {
     try {
       return healthFuture.get();
     } catch (InterruptedException | ExecutionException | CancellationException e) {
       logger.error("Error during healthcheck of " + name, e);
       return new ServiceCheckResult(name, ServiceState.FAILED);
     }
 
   }
 
   private Callable<ServiceCheckResult> callable(final ServiceCheck serviceCheck) {
     return new Callable<ServiceCheckResult>() {
       @Override
       public ServiceCheckResult call() {
         return new ServiceCheckResult(serviceCheck.getName(), isServiceHealthy(serviceCheck));
       }
     };
   }
 
   public ServiceState isServiceHealthy(ServiceCheck check) {
     ServiceState result;
     try {
       result = check.healthy();
 
       if (result.failed()) {
         logger.error("HealthCheck for '{}' failed", check.getName());
       }
     } catch (Exception e) {
       logger.error("Healthcheck for " + check.getName() + " failed with an exception: ", e);
       result = ServiceState.FAILED;
     }
 
     return result;
   }
 
   protected void setLogger(Logger logger) {
     this.logger = logger;
   }
 
   public void setChecks(List<ServiceCheck> checks) {
     this.checks = checks;
   }
 
   @Override
   public void afterPropertiesSet() throws Exception {
     checks = Lists.newArrayList(
         iddServiceCheck,
         nbiServiceCheck,
         oAuthServerServiceCheck,
         apiServiceCheck,
         sabServiceCheck,
         versCheck);
 
     if (springEnvironment.acceptsProfiles("onecontrol")) {
       checks.add(oneControlNotificationsCheck);
     }
   }
 
   @Override
   public void setEnvironment(org.springframework.core.env.Environment environment) {
     this.springEnvironment = environment;
   }
 
  public void setTimeoutInSeconds(int timeoutInSeconds) {
    this.timeoutInSeconds = timeoutInSeconds;
  }

   public interface ServiceCheck {
     ServiceState healthy() throws Exception;
 
     String getName();
   }
 
   public enum ServiceState {
     FAILED, DISABLED, SUCCEEDED;
 
     public boolean failed() {
       return this == FAILED;
     }
   }
 
   public static class ServiceCheckResult {
     private String name;
     private ServiceState state;
 
     public ServiceCheckResult(String name, ServiceState state) {
       this.name = name;
       this.state = state;
     }
 
     public String getName() {
       return name;
     }
 
     public ServiceState getState() {
       return state;
     }
 
     @Override
     public int hashCode() {
       final int prime = 31;
       int result = 1;
       result = prime * result + ((name == null) ? 0 : name.hashCode());
       result = prime * result + ((state == null) ? 0 : state.hashCode());
       return result;
     }
 
     @Override
     public boolean equals(Object obj) {
       if (this == obj)
         return true;
       if (obj == null)
         return false;
       if (getClass() != obj.getClass())
         return false;
       ServiceCheckResult other = (ServiceCheckResult) obj;
       if (name == null) {
         if (other.name != null)
           return false;
       } else if (!name.equals(other.name))
         return false;
       if (state != other.state)
         return false;
       return true;
     }
   }
 
   private static class HealthCheckResult {
     private boolean allOk;
     private List<ServiceCheckResult> results;
 
     private HealthCheckResult(boolean allOk, List<ServiceCheckResult> results) {
       this.allOk = allOk;
       this.results = results;
     }
 
     private boolean isAllOk() {
       return allOk;
     }
 
     private List<ServiceCheckResult> getResults() {
       return results;
     }
   }
 }
