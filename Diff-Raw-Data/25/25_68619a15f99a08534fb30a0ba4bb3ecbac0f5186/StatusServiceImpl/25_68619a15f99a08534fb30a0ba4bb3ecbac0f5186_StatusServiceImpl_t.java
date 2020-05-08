 package thesmith.eventhorizon.service.impl;
 
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import thesmith.eventhorizon.model.Account;
 import thesmith.eventhorizon.model.Event;
 import thesmith.eventhorizon.model.Status;
 import thesmith.eventhorizon.service.EventService;
 import thesmith.eventhorizon.service.StatusService;
 
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.repackaged.com.google.common.collect.Lists;
 import com.google.appengine.repackaged.org.joda.time.DateTime;
 import com.google.appengine.repackaged.org.joda.time.Interval;
 import com.google.appengine.repackaged.org.joda.time.Period;
 
 /**
  * Implementation of StatusService
  * 
  * @author bens
  */
 @Transactional
 @Service
 public class StatusServiceImpl implements StatusService {
   @PersistenceContext
   private EntityManager em;
   private final Log logger = LogFactory.getLog(this.getClass());
 
   private final Map<String, EventService> eventServices;
 
   public StatusServiceImpl(Map<String, EventService> eventServices) {
     this.eventServices = eventServices;
   }
 
   @SuppressWarnings("unchecked")
   public void create(Status status) {
     if (null == status.getTitle() || null == status.getTitleUrl() || null == status.getCreated())
       throw new RuntimeException("Unable to create status without appropriate info: " + status);
 
     List<Status> statuses = em.createQuery(
         "select s from Status s where s.personId = :personId and s.domain = :domain and s.created = :from")
         .setParameter("personId", status.getPersonId()).setParameter("domain", status.getDomain()).setParameter("from",
             status.getCreated()).setMaxResults(1).getResultList();
     if (null == statuses || statuses.size() < 1) {
       em.persist(status);
      em.flush();
     } else {
       Status s = statuses.get(0);
       s.setTitle(status.getTitle());
       s.setTitleUrl(status.getTitleUrl());
       em.merge(s);
     }
   }
 
   public Status find(Account account, Date from) {
     return this.find(account, from, "<=", "desc");
   }
 
   public Status next(Account account, Date from) {
     return this.find(account, from, ">", "asc");
   }
 
   public Status previous(Account account, Date from) {
     return this.find(account, from, "<", "desc");
   }
 
   @SuppressWarnings("unchecked")
   private Status find(Account account, Date from, String created, String order) {
     if (logger.isDebugEnabled())
       logger.debug("Finding for " + account.getPersonId() + " on " + account.getDomain() + " from " + from + " as "
           + created + " by " + order);
 
     List<Status> statuses = em.createQuery(
         "select s from Status s where s.personId = :personId and s.domain = :domain and s.created " + created
             + " :from order by s.created " + order).setParameter("personId", account.getPersonId()).setParameter(
         "domain", account.getDomain()).setParameter("from", from).setMaxResults(1).getResultList();
     if (null != statuses && statuses.size() > 0) {
       Status status = statuses.get(0);
       return processStatus(status, from, account);
     }
     return null;
   }
 
   public List<Status> list(Account account, int page) {
     EventService service = eventServices.get(account.getDomain());
     if (service == null)
       throw new RuntimeException("Unable to process events from unknown domain: " + account.getDomain());
 
     List<Status> statuses = Lists.newArrayList();
     Date oldest = null;
     Date newest = null;
 
     for (Event event : service.events(account, page)) {
       Status status = new Status();
       status.setPersonId(account.getPersonId());
       status.setDomain(account.getDomain());
       status.setCreated(event.getCreated());
       status.setTitle(event.getTitle());
       status.setTitleUrl(event.getTitleUrl());
       statuses.add(status);
 
       if (null == oldest || (null != status.getCreated() && status.getCreated().before(oldest)))
         oldest = status.getCreated();
 
       if (null == newest || (null != status.getCreated() && status.getCreated().after(newest)))
         newest = status.getCreated();
     }
     if (logger.isInfoEnabled())
       logger.info("Retrieved " + statuses.size() + " statuses for " + account.getDomain() + ":" + account.getUserId()
           + " as page " + page + " ranging from " + oldest + " to " + newest);
 
     return statuses;
   }
   
   public Status find(Key key, Date from, Map<String, Account> accounts) {
     Status status = em.find(Status.class, key);
     return processStatus(status, from, accounts.get(status.getDomain()));
   }
   
   private Status processStatus(Status status, Date from, Account account) {
     Status returnStatus = new Status();
     returnStatus.setCreated(status.getCreated());
     returnStatus.setDomain(status.getDomain());
     returnStatus.setPersonId(status.getPersonId());
     returnStatus.setTitle(status.getTitle());
     returnStatus.setTitleUrl(status.getTitleUrl());
     if (null != from)
       returnStatus.setPeriod(this.period(from, status.getCreated()));
     if (null != account && null != account.getTemplate())
       returnStatus.setStatus(this.status(account, returnStatus));
 
     if (logger.isDebugEnabled())
       logger.debug("Retrieved: " + returnStatus);
     return returnStatus;
   }
 
   private String status(Account account, Status status) {
     if (null == status.getTitle() || null == status.getTitleUrl() || null == status.getCreated())
       throw new RuntimeException("Unable to create status without appropriate info: " + status);
 
     String text = account.getTemplate();
     text = text.replaceAll("\\{title\\}", Matcher.quoteReplacement(status.getTitle()));
     if (null != status.getTitleUrl())
       text = text.replaceAll("\\{titleUrl\\}", Matcher.quoteReplacement(status.getTitleUrl()));
     text = text.replaceAll("\\{domain\\}", Matcher.quoteReplacement(account.getDomain()));
     if (null != account.getDomainUrl())
       text = text.replaceAll("\\{domainUrl\\}", Matcher.quoteReplacement(account.getDomainUrl()));
     if (null != account.getUserUrl())
       text = text.replaceAll("\\{userUrl\\}", Matcher.quoteReplacement(account.getUserUrl()));
     text = text.replaceAll("\\{ago\\}", this.ago(status.getCreated()));
     if (logger.isDebugEnabled())
       logger.debug(text);
     return text;
   }
 
   private String period(Date from, Date created) {
     DateTime now = new DateTime(from.getTime());
     DateTime then = new DateTime(created.getTime());
     if (from.after(created)) {
       Period period = new Interval(then, now).toPeriod();
       if (period.getYears() > 0 || period.getMonths() > 0)
         return "yonks";
       else if (period.getWeeks() > 0)
         return "month";
       else if (period.getDays() > 0)
         return "week";
     }
     return "today";
   }
 
   private String ago(Date created) {
     if (null == created)
       return "some time ago";
 
     DateTime now = new DateTime();
     DateTime then = new DateTime(created.getTime());
     Period period = new Interval(then, now).toPeriod();
     int printed = 0;
 
     StringBuffer ago = new StringBuffer();
     printed = printPeriod(ago, printed, period.getYears(), "year");
     printed = printPeriod(ago, printed, period.getMonths(), "month");
     printed = printPeriod(ago, printed, period.getWeeks(), "week");
     printed = printPeriod(ago, printed, period.getDays(), "day");
     printed = printPeriod(ago, printed, period.getHours(), "hour");
     printed = printPeriod(ago, printed, period.getMinutes(), "minute");
 
     ago.append(" ago");
     return ago.toString();
   }
 
   private int printPeriod(StringBuffer ago, int printed, int value, String desc) {
     if (printed > 1) {
       return printed;
     }
 
     if (value > 1) {
       if (printed == 1)
         ago.append(" and ");
       ago.append(value + " " + desc + "s");
     } else if (value == 1) {
       if (printed == 1)
         ago.append(" and ");
       ago.append(value + " " + desc);
     } else {
       return printed;
     }
     return printed + 1;
   }
 }
