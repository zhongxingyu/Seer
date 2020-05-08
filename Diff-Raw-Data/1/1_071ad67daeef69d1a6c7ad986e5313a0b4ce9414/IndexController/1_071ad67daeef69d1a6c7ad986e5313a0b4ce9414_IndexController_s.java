 package thesmith.eventhorizon.controller;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.ModelMap;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import thesmith.eventhorizon.model.Status;
 
 import com.google.appengine.repackaged.com.google.common.collect.Lists;
 
 @Controller
 public class IndexController extends BaseController {
   public static final String FROM = "from";
   private static final DateFormat format = new SimpleDateFormat("yyyy/MM/dd kk:mm:ss");
   private static final DateFormat urlFormat = new SimpleDateFormat("yyyy/MM/dd/kk/mm/ss");
 
   @RequestMapping(value = "/{personId}/{year}/{month}/{day}/{hour}/{min}/{sec}", method = RequestMethod.GET)
   public String index(@PathVariable("personId") String personId, @PathVariable("year") int year,
       @PathVariable("month") int month, @PathVariable("day") int day, @PathVariable("hour") int hour,
       @PathVariable("min") int min, @PathVariable("sec") int sec, ModelMap model) {
 
     try {
       Date from = format.parse(String.format("%d/%d/%d %d:%d:%d", year, month, day, hour, min, sec));
       this.setModel(personId, from, model);
     } catch (ParseException e) {
       if (logger.isWarnEnabled())
         logger.warn(e);
       return "redirect:/error";
     }
     return "index/index";
   }
 
   @RequestMapping(value = "/{personId}/{year}/{month}/{day}/{hour}/{min}/{sec}/{domain}/previous", method = RequestMethod.GET)
   public String previous(@PathVariable("personId") String personId, @PathVariable("year") int year,
       @PathVariable("month") int month, @PathVariable("day") int day, @PathVariable("hour") int hour,
       @PathVariable("min") int min, @PathVariable("sec") int sec, @PathVariable("domain") String domain) {
     try {
       Date from = format.parse(String.format("%d/%d/%d %d:%d:%d", year, month, day, hour, min, sec));
       Status status = statusService.previous(personId, domain, from);
       if (null == status)
         status = statusService.find(personId, domain, from);
       return String.format("redirect:/%s/%s/", personId, urlFormat.format(status.getCreated()));
     } catch (ParseException e) {
       if (logger.isWarnEnabled())
         logger.warn(e);
       return "redirect:/error";
     }
   }
 
   @RequestMapping(value = "/{personId}/{year}/{month}/{day}/{hour}/{min}/{sec}/{domain}/next", method = RequestMethod.GET)
   public String next(@PathVariable("personId") String personId, @PathVariable("year") int year,
       @PathVariable("month") int month, @PathVariable("day") int day, @PathVariable("hour") int hour,
       @PathVariable("min") int min, @PathVariable("sec") int sec, @PathVariable("domain") String domain) {
     try {
       Date from = format.parse(String.format("%d/%d/%d %d:%d:%d", year, month, day, hour, min, sec));
       Status status = statusService.next(personId, domain, from);
       if (null == status)
         status = statusService.find(personId, domain, from);
       return String.format("redirect:/%s/%s/", personId, urlFormat.format(status.getCreated()));
     } catch (ParseException e) {
       if (logger.isWarnEnabled())
         logger.warn(e);
       return "redirect:/error";
     }
   }
 
   @RequestMapping(value = "/{personId}/{domain}/previous", method = RequestMethod.GET)
   public String previous(@PathVariable("personId") String personId, @PathVariable("domain") String domain,
       @RequestParam("from") String from, ModelMap model) {
     try {
       Status status = statusService.previous(personId, domain, this.parseDate(from));
       if (null != status)
         this.setModel(personId, status.getCreated(), model);
 
     } catch (ParseException e) {
       if (logger.isWarnEnabled())
         logger.warn(e);
       return "redirect:/error";
     }
     return "index/index";
   }
 
   @RequestMapping(value = "/{personId}/{domain}/next", method = RequestMethod.GET)
   public String next(@PathVariable("personId") String personId, @PathVariable("domain") String domain,
       @RequestParam("from") String from, ModelMap model) {
     try {
       Status status = statusService.next(personId, domain, this.parseDate(from));
       if (null != status)
         this.setModel(personId, status.getCreated(), model);
 
     } catch (ParseException e) {
       if (logger.isWarnEnabled())
         logger.warn(e);
       return "redirect:/error";
     }
     return "index/index";
   }
 
   @RequestMapping(value = "/{personId}/now", method = RequestMethod.GET)
   public String now(@PathVariable("personId") String personId, @RequestParam("from") String from, ModelMap model) {
     if (null != from && from.length() > 0) {
       try {
         this.setModel(personId, this.parseDate(from), model);
 
       } catch (ParseException e) {
         if (logger.isWarnEnabled())
           logger.warn(e);
         return "redirect:/error";
       }
     }
     return "index/index";
   }
 
   @RequestMapping(value = "/{personId}", method = RequestMethod.GET)
   public String start(@PathVariable("personId") String personId, ModelMap model) {
     this.setModel(personId, null, model);
 
     return "index/index";
   }
 
   @RequestMapping(value = "/error", method = RequestMethod.GET)
   public String error() {
     return "error";
   }
 
   private void setModel(String personId, Date from, ModelMap model) {
     List<String> domains = accountService.domains(personId);
     List<Status> statuses = Lists.newArrayList();
     if (null != from) {
       for (String domain : domains) {
         Status status = statusService.find(personId, domain, from);
         if (null == status) {
           status = defaultStatus(personId, domain, from);
         }
         statuses.add(status);
       }
     } else {
       from = new Date();
       for (String domain : domains) {
         statuses.add(defaultStatus(personId, domain, from));
       }
     }
     model.addAttribute("statuses", statuses);
     model.addAttribute("personId", personId);
     model.addAttribute("from", from);
     
     if (logger.isDebugEnabled())
       logger.debug("Setting the model: "+model);
   }
   
   private Status defaultStatus(String personId, String domain, Date from) {
     Status status = new Status();
     status.setDomain(domain);
     status.setPersonId(personId);
     status.setStatus("");
     status.setCreated(from);
     status.setPeriod("today");
     return status;
   }
 
   private Date parseDate(String from) throws ParseException {
     if (from.startsWith("/"))
       from = from.substring(1);
     if (from.endsWith("/"))
       from = from.substring(0, from.length());
 
     return urlFormat.parse(from);
   }
 }
