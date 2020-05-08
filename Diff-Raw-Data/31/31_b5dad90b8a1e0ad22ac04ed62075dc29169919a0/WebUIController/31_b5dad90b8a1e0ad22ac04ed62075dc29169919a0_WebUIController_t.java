 package au.edu.uq.cmm.paul.servlet;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.NoResultException;
 import javax.persistence.TypedQuery;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 import org.joda.time.Days;
 import org.joda.time.Hours;
 import org.joda.time.Minutes;
 import org.joda.time.Months;
 import org.joda.time.Weeks;
 import org.joda.time.Years;
 import org.joda.time.base.BaseSingleFieldPeriod;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.ISODateTimeFormat;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import au.edu.uq.cmm.aclslib.proxy.AclsLoginException;
 import au.edu.uq.cmm.aclslib.proxy.AclsProxy;
 import au.edu.uq.cmm.aclslib.service.Service;
 import au.edu.uq.cmm.aclslib.service.Service.State;
 import au.edu.uq.cmm.paul.Paul;
 import au.edu.uq.cmm.paul.grabber.DatafileMetadata;
 import au.edu.uq.cmm.paul.grabber.DatasetMetadata;
 import au.edu.uq.cmm.paul.status.Facility;
 import au.edu.uq.cmm.paul.status.FacilityStatusManager;
 import au.edu.uq.cmm.paul.watcher.FileWatcher;
 
 /**
  * The MVC controller for Paul's web UI.  This supports the status and configuration
  * pages and also implements GET access to the files in the queue area.
  * 
  * @author scrawley
  */
 @Controller
 public class WebUIController {
     public enum Status {
         ON, OFF, TRANSITIONAL
     }
     
     private static final Logger LOG = Logger.getLogger(WebUIController.class);
 
     private static DateTimeFormatter[] FORMATS = new DateTimeFormatter[] {
         ISODateTimeFormat.dateHourMinuteSecond(),
         ISODateTimeFormat.localTimeParser(),
         ISODateTimeFormat.localDateOptionalTimeParser(),
         ISODateTimeFormat.dateTimeParser()
     };
     
     @Autowired(required=true)
     Paul services;
 
     @RequestMapping(value="/control", method=RequestMethod.GET)
     public String control(Model model) {
         addStateAndStatus(model);
         return "control";
     }
 
     @RequestMapping(value="/control", method=RequestMethod.POST)
     public String controlAction(Model model, HttpServletRequest request) {
         processStatusChange(getFileWatcher(), request.getParameter("watcher"));
         processStatusChange(getProxy(), request.getParameter("proxy"));
         addStateAndStatus(model);
         return "control";
     }
     
     private void processStatusChange(Service service, String param) {
         Service.State current = service.getState();
         if (param == null) {
             return;
         }
         Status target = Status.valueOf(param);
         if (target == stateToStatus(current) || 
                 stateToStatus(current) == Status.TRANSITIONAL) {
             return;
         }
         if (target == Status.ON) {
             service.startStartup();
         } else {
             service.startShutdown();
         }
     }
     
     private void addStateAndStatus(Model model) {
         State ws = getFileWatcher().getState();
         State ps = getProxy().getState();
         model.addAttribute("watcherState", ws);
         model.addAttribute("proxyState", ps);
         model.addAttribute("watcherStatus", stateToStatus(ws));
         model.addAttribute("proxyStatus", stateToStatus(ps));
     }
     
     private Status stateToStatus(State state) {
         switch (state) {
         case STARTED:
            return Status.ON;
         case FAILED:
         case STOPPED:
         case INITIAL:
             return Status.OFF;
         default:
             return Status.TRANSITIONAL;
         }
     }
 
     private AclsProxy getProxy() {
         return services.getProxy();
     }
     
     private FileWatcher getFileWatcher() {
         return services.getFileWatcher();
     }
     
     private Facility lookupFacilityByName(String facilityName) {
         return (Facility) 
                 services.getConfiguration().lookupFacilityByName(facilityName);
     }
     
     @RequestMapping(value="/sessions", method=RequestMethod.GET)
     public String status(Model model) {
         model.addAttribute("facilities", 
                 services.getFacilitySessionManager().getSnapshot());
         return "sessions";
     }
     
     @RequestMapping(value="/sessions/{sessionUuid:.+}", method=RequestMethod.POST, 
             params={"endSession"})
     public String endSession(@PathVariable String sessionUuid, Model model, 
             HttpServletResponse response, HttpServletRequest request) 
     throws IOException {
         services.getFacilitySessionManager().endSession(sessionUuid);
         response.sendRedirect(response.encodeRedirectURL(
                 request.getContextPath() + "/sessions"));
         return null;
     }
     
     @RequestMapping(value="/facilities/{facilityName:.+}", method=RequestMethod.GET)
     public String facilityConfig(@PathVariable String facilityName, Model model) {
         Facility facility = lookupFacilityByName(facilityName);
         model.addAttribute("facility", facility);
         return "facility";
     }
     
     @RequestMapping(value="/facilities/{facilityName:.+}", method=RequestMethod.POST, 
             params={"enableWatcher"})
     public String enableWatcher(@PathVariable String facilityName, Model model) {
         Facility facility = lookupFacilityByName(facilityName);
         if (facility != null) {
             getFileWatcher().startFileWatching(facility, true);
         }
         model.addAttribute("facility", facility);
         return "facility";
     }
     
     @RequestMapping(value="/facilities/{facilityName:.+}", method=RequestMethod.POST, 
             params={"disableWatcher"})
     public String disableWatcher(@PathVariable String facilityName, Model model) {
         Facility facility = lookupFacilityByName(facilityName);
         if (facility != null) {
             getFileWatcher().stopFileWatching(facility, true);
         }
         model.addAttribute("facility", facility);
         return "facility";
     }
     
     @RequestMapping(value="/facilities/{facilityName:.+}", method=RequestMethod.POST, 
             params={"pauseWatcher"})
     public String pauseWatcher(@PathVariable String facilityName, Model model) {
         Facility facility = lookupFacilityByName(facilityName);
         if (facility != null) {
             getFileWatcher().stopFileWatching(facility, false);
         }
         model.addAttribute("facility", facility);
         return "facility";
     }
     
     @RequestMapping(value="/facilities/{facilityName:.+}", method=RequestMethod.POST, 
             params={"startSession"})
     public String startSession(@PathVariable String facilityName, 
             @RequestParam(required=false) String userName, 
             @RequestParam(required=false) String password,
             @RequestParam(required=false) String account,
             Model model, HttpServletResponse response, HttpServletRequest request) 
     throws IOException {
         FacilityStatusManager fsm = services.getFacilitySessionManager();
         facilityName = tidy(facilityName);
         model.addAttribute("facilityName", facilityName);
         
         if ((userName = tidy(userName)).isEmpty() ||
                 (password = tidy(password)).isEmpty()) {
             // Phase 1 - user must fill in user name and password
             model.addAttribute("message", "Fill in username and password");
             return "facilityLoginForm";
         }
         if (account == null) {
             // Phase 2 - validate user credentials and get accounts list
             List<String> accounts = null;
             try {
                 LOG.debug("Attempting login");
                 accounts = fsm.login(facilityName, userName, password);
                 LOG.debug("Login succeeded");
             } catch (AclsLoginException ex) {
                 model.addAttribute("message", "Login failed: " + ex.getMessage());
             }
             // If there is only one account, select immediately.
            if (accounts != null) {
                try {
                    if (accounts.size() == 1) {
                        fsm.selectAccount(facilityName, userName, accounts.get(0));
                        LOG.debug("Account selection succeeded");
                        response.sendRedirect(response.encodeRedirectURL(
                                request.getContextPath() + "/sessions"));
                        return null;
                    } else {
                        model.addAttribute("accounts", accounts);
                        model.addAttribute("message", 
                                "Select an account to complete the login");
                    }
                } catch (AclsLoginException ex) {
                    model.addAttribute("message",
                            "Account selection failed: " + ex.getMessage());
                 }
             }
         } else {
             // Phase 3 - after user has selected an account
             try {
                 fsm.selectAccount(facilityName, userName, account);
                 LOG.debug("Account selection succeeded");
                 response.sendRedirect(response.encodeRedirectURL(
                         request.getContextPath() + "/sessions"));
                 return null;
             } catch (AclsLoginException ex) {
                 model.addAttribute("message", 
                         "Account selection failed: " + ex.getMessage());
             }
         }
         return "facilityLoginForm";
     }
     
     @RequestMapping(value="/config", method=RequestMethod.GET)
     public String config(Model model) {
         model.addAttribute("config", services.getConfiguration());
         return "config";
     }
     
     @RequestMapping(value="/queue", method=RequestMethod.GET)
     public String queue(Model model) {
         model.addAttribute("queue", services.getQueueManager().getSnapshot());
         return "queue";
     }
     
     @RequestMapping(value="/queue", method=RequestMethod.POST, 
             params={"deleteAll"})
     public String deleteAll(Model model, 
             @RequestParam(required=false) String mode, 
             @RequestParam(required=false) String confirmed) {
         if (confirmed == null) {
             return "queueDeleteConfirmation";
         }
         boolean discard = mode.equals("discard");
         int count = services.getQueueManager().deleteAll(discard);
         String verb = discard ? "deleted" : "archived";
         model.addAttribute("message",
                 (count == 0 ? "No queue entries " :
                     count == 1 ? "1 queue entry " :
                         (count + " queue entries ")) + verb);
         model.addAttribute("returnTo", "queue");
         return "ok";
     }
     
     @RequestMapping(value="/queue", method=RequestMethod.POST, 
             params={"expire"})
     public String expire(Model model, 
             @RequestParam(required=false) String mode, 
             @RequestParam(required=false) String confirmed,
             @RequestParam(required=false) String olderThan,
             @RequestParam(required=false) String period,
             @RequestParam(required=false) String unit) {
         Date cutoff = determineCutoff(model, tidy(olderThan), tidy(period), tidy(unit));
         if (cutoff == null || confirmed == null) {
             return "queueExpiryForm";
         }
         int count = services.getQueueManager().expire(mode.equals("discard"), cutoff);
         model.addAttribute("message",
                 count == 0 ? "No queue entries expired" :
                     count == 1 ? "1 queue entry expired" :
                         (count + " queue entries expired"));
         model.addAttribute("returnTo", "queue");
         return "ok";
     }
 
     @RequestMapping(value="/queue/{entry:.+}", method=RequestMethod.GET)
     public String queueEntry(@PathVariable String entry, Model model, 
             HttpServletResponse response) 
             throws IOException {
         long id;
         try {
             id = Long.parseLong(entry);
         } catch (NumberFormatException ex) {
             LOG.debug("Rejected request with bad entry id");
             response.sendError(HttpServletResponse.SC_BAD_REQUEST);
             return null;
         }
         DatasetMetadata metadata = fetchMetadata(id);
         if (metadata == null) {
             LOG.debug("Rejected request for unknown entry");
             response.sendError(HttpServletResponse.SC_NOT_FOUND);
             return null;
         }
         model.addAttribute("entry", metadata);
         return "queueEntry";
     }
     
     @RequestMapping(value="/queue/{entry:.+}", method=RequestMethod.POST, 
             params={"delete"})
     public String queueEntryDelete(@PathVariable String entry, Model model, 
             HttpServletResponse response,
             @RequestParam(required=false) String mode, 
             @RequestParam(required=false) String confirmed) 
             throws IOException {
         long id;
         try {
             id = Long.parseLong(entry);
         } catch (NumberFormatException ex) {
             LOG.debug("Rejected request with bad entry id");
             response.sendError(HttpServletResponse.SC_BAD_REQUEST);
             return null;
         }
         boolean discard = mode.equals("discard");
         services.getQueueManager().delete(id, discard);
         model.addAttribute("message",
                 "Queue entry " + (discard ? "deleted" : "archived"));
         model.addAttribute("returnTo", "../queue");
         return "ok";
     }
     
     @RequestMapping(value="/files/{fileName:.+}", method=RequestMethod.GET)
     public String file(@PathVariable String fileName, Model model, 
             HttpServletResponse response) 
             throws IOException {
         LOG.debug("Request to fetch file " + fileName);
         // This aims to prevent requests from reading files outside of the queue directory.
         // FIXME - this assumes that the directory for the queue is flat ...
         if (fileName.contains("/") || fileName.equals("..")) {
             LOG.debug("Rejected request for security reasons");
             response.sendError(HttpServletResponse.SC_BAD_REQUEST);
             return null;
         }
         File file = new File(services.getConfiguration().getCaptureDirectory(), fileName);
         DatafileMetadata metadata = fetchMetadata(file);
         if (metadata == null) {
             LOG.debug("No metadata for file " + fileName);
         } else {
             LOG.debug("Found metadata for file " + fileName);
         }
         model.addAttribute("file", file);
         model.addAttribute("contentType", 
                 metadata == null ? "application/octet-stream" : metadata.getMimeType());
         return "fileView";
     }
     
     private DatafileMetadata fetchMetadata(File file) {
         EntityManager entityManager = 
                 services.getEntityManagerFactory().createEntityManager();
         try {
             TypedQuery<DatafileMetadata> query = entityManager.createQuery(
                     "from DatafileMetadata d where d.capturedFilePathname = :pathName", 
                     DatafileMetadata.class);
             query.setParameter("pathName", file.getAbsolutePath());
             return query.getSingleResult();
         } catch (NoResultException ex) {
             return null;
         } finally {
             entityManager.close();
         }
     }
     
     private DatasetMetadata fetchMetadata(long id) {
         EntityManager entityManager = 
                 services.getEntityManagerFactory().createEntityManager();
         try {
             TypedQuery<DatasetMetadata> query = entityManager.createQuery(
                     "from DatasetMetadata d where d.id = :id", 
                     DatasetMetadata.class);
             query.setParameter("id", id);
             return query.getSingleResult();
         } catch (NoResultException ex) {
             return null;
         } finally {
             entityManager.close();
         }
     }
     
     private String tidy(String str) {
         return str == null ? "" : str.trim();
     }
     
     private Date determineCutoff(Model model, String olderThan, 
             String period, String unit) {
         if (olderThan.isEmpty() && period.isEmpty()) {
             model.addAttribute("message", 
                     "Either an expiry date or period must be supplied");
             return null;
         }
         DateTime cutoff;
         if (olderThan.isEmpty()) {
             int value;
             try {
                 value = Integer.parseInt(period);
             } catch (NumberFormatException ex) {
                 model.addAttribute("message", "Malformed period");
                 return null;
             }
             BaseSingleFieldPeriod p;
             switch (unit) {
             case "minute" : case "minutes" :
                 p = Minutes.minutes(value);
                 break;
             case "hour" : case "hours" :
                 p = Hours.hours(value);
                 break;
             case "day" : case "days" :
                 p = Days.days(value);
                 break;
             case "week" : case "weeks" :
                 p = Weeks.weeks(value);
                 break;
             case "month" : case "months" :
                 p = Months.months(value);
                 break;
             case "year" : case "years" :
                 p = Years.years(value);
                 break;
             default :
                 model.addAttribute("message", "Unrecognized unit");
                 return null;
             }
             cutoff = DateTime.now().minus(p);
         } else {
             cutoff = null;
             for (DateTimeFormatter format : FORMATS) {
                 try {
                     cutoff = format.parseDateTime(olderThan);
                     break;
                 } catch (IllegalArgumentException ex) {
                     continue;
                 }
             }
             if (cutoff == null) {
                 model.addAttribute("message", "Unrecognizable expiry date");
                 return null;
             }
         }
         if (cutoff.isAfter(new DateTime())) {
             model.addAttribute("message", "Expiry date is in the future");
             return null;
         }
         model.addAttribute("computedDate", FORMATS[0].print(cutoff));
         return cutoff.toDate();
     }
 }
