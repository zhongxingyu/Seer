 package ru.bigbuzzy.monitor.task;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.auth.AuthScope;
 import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
 import org.apache.http.params.HttpConnectionParams;
 import org.apache.http.params.HttpParams;
 import org.apache.http.util.EntityUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.util.StopWatch;
 import ru.bigbuzzy.monitor.model.task.ResourceStatus;
 import ru.bigbuzzy.monitor.service.ConfigurationService;
 import ru.bigbuzzy.monitor.service.ResourceStatusService;
 import ru.bigbuzzy.monitor.service.mail.MailErrorCommand;
 import ru.bigbuzzy.monitor.service.mail.MailExceptionCommand;
 import ru.bigbuzzy.monitor.service.MailService;
 import ru.bigbuzzy.monitor.model.config.Accept;
 import ru.bigbuzzy.monitor.model.config.Resource;
 import ru.bigbuzzy.monitor.model.config.Url;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: volodko
  * Date: 05.12.11
  * Time: 14:37
  */
 public class HttpResourceMonitor {
     private static final Logger logger = LoggerFactory.getLogger(HttpResourceMonitor.class);
     @Autowired
     private MailService mailService;
     @Autowired
     private ConfigurationService configurationService;
     @Autowired
     private ResourceStatusService resourceStatusService;
 
     private ThreadSafeClientConnManager clientConnManager = new ThreadSafeClientConnManager();
 
     public void execute() {
         if (logger.isTraceEnabled()) {
             logger.trace("Method execute has started");
         }
 
         List<ResourceStatus> resourceStatuses = new ArrayList<ResourceStatus>();
 
         if (CollectionUtils.isEmpty(configurationService.getResources())) {
             return;
         }
         logger.trace("Resource size = {}", configurationService.getResources().size());
 
         for (Resource resource : configurationService.getResources()) {
             ResourceStatus resourceStatus = new ResourceStatus();
             resourceStatus.setCreateTime(new Date());
             resourceStatus.setResource(resource);
 
             DefaultHttpClient httpClient = new DefaultHttpClient(clientConnManager);
 
             try {
                 Accept accept = resource.getAccept();
 
                 HttpParams params = httpClient.getParams();
                 HttpConnectionParams.setConnectionTimeout(params, accept.getConnectionTimeout());
                 HttpConnectionParams.setSoTimeout(params, accept.getSocketTimeout());
 
                 Url url = resource.getUrl();
 
                 if (StringUtils.isNotBlank(url.getLogin())) {
                     httpClient.getCredentialsProvider().setCredentials(
                             new AuthScope(url.getHost(), url.getPort()),
                             new UsernamePasswordCredentials(url.getLogin(), url.getPassword()));
                 }
 
                 HttpGet httpget = new HttpGet(resource.getUrl().getPath());
 
                 logger.trace("Executing request {}", httpget.getURI());
 
                 StopWatch watch = new StopWatch();
                 watch.start();
                 HttpResponse response = httpClient.execute(httpget);
                 watch.stop();
                 logger.trace("Watch time: {}", watch.toString());
 
                 if (watch.getTotalTimeMillis() > accept.getConnectionTimeout()) {
                     mailService.send(new MailErrorCommand(resource,
                             MailErrorCommand.ErrorCode.ResponseTimeOut,
                             String.valueOf(watch.getTotalTimeMillis())));
                 }
                 resourceStatus.setResponseTimeOut(watch.getTotalTimeMillis());
 
                 if (response.getStatusLine() != null) {
                     int statusCode = response.getStatusLine().getStatusCode();
                     logger.trace("Response status code: {}", statusCode);
                     if (accept.getResponseCode() != -1 && statusCode != accept.getResponseCode()) {
                         logger.error("Accept error by statusCode {} not equal {}", accept.getResponseCode(), statusCode);
                         mailService.send(new MailErrorCommand(resource,
                                 MailErrorCommand.ErrorCode.ResponseCode,
                                 String.valueOf(statusCode)));
                     }
                     resourceStatus.setResponseCode(statusCode);
                 }
 
                 HttpEntity entity = response.getEntity();
                 if (entity != null) {
                     String responseBody = EntityUtils.toString(entity);
 //                    logger.trace("---------------- Response body start ------------------------");
 //                    logger.trace(responseBody);
 //                    logger.trace("---------------- Response body end --------------------------");
                     if (responseBody.length() <= accept.getResponseSize()) {
                         mailService.send(new MailErrorCommand(resource,
                                 MailErrorCommand.ErrorCode.ResponseSize,
                                 String.valueOf(responseBody.length())));
                     }
                     resourceStatus.setResponseSize(responseBody.length());
                 }
             } catch (Exception e) {
                 logger.error("Exception occurred: ", e);
                 mailService.send(new MailExceptionCommand(resource, e));
                 resourceStatus.setStatusException(true);
                 resourceStatus.setStatusMessage(e.getMessage());
             }
             resourceStatuses.add(resourceStatus);
         }
         resourceStatusService.save(resourceStatuses);
     }
 
     public void destroy() {
         if (clientConnManager != null) {
             clientConnManager.shutdown();
         }
     }
 }
