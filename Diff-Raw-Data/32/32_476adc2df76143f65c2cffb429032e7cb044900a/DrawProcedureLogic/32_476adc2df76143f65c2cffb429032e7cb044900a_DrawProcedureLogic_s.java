 /*
  * Copyright (c) 2010-2012 Matthias Klass, Johannes Leimer,
  *               Rico Lieback, Sebastian Gabriel, Lothar Gesslein,
  *               Alexander Rampp, Kai Weidner
  *
  * This file is part of the Physalix Enrollment System
  *
  * Foobar is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Foobar is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package hsa.awp.scire.procedureLogic;
 
 import hsa.awp.campaign.model.*;
 import hsa.awp.common.exception.NoMatchingElementException;
 import hsa.awp.common.exception.ProgrammingErrorException;
 import hsa.awp.common.mail.IMail;
 import hsa.awp.common.model.TemplateDetail;
 import hsa.awp.common.model.TemplateType;
 import hsa.awp.event.model.Event;
 import hsa.awp.scire.exception.DuplicatePriorityListElementException;
 import hsa.awp.scire.procedureLogic.util.MailContent;
 import hsa.awp.scire.procedureLogic.util.PriorityListItemPrioritySorter;
 import hsa.awp.scire.procedureLogic.util.XmlDrawLogUtil;
 import hsa.awp.user.model.Group;
 import hsa.awp.user.model.SingleUser;
 import hsa.awp.user.model.User;
 import org.apache.velocity.Template;
 import org.apache.velocity.VelocityContext;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.web.util.HtmlUtils;
 
 import java.io.StringWriter;
 import java.util.*;
 
 import static hsa.awp.event.util.EventFormattingUtils.formatIdSubjectNameAndDetailInformation;
 
 /**
  * This is the class which implements the draw logic for the AWP Anmeldesystem.
  *
  * @author rico
  * @author klassm
  * @author johannes
  */
 public class DrawProcedureLogic extends AbstractProcedureLogic<DrawProcedure> implements IDrawProcedureLogic,
     IProcedureLogic<DrawProcedure> {
   /**
    * Boolean variable will be true when the underlying {@link DrawProcedure} has been drawn.
    */
   private boolean drawn = false;
 
   /**
    * A random generator.
    */
   private Random rand;
 
   /**
    * Text that is sent in mails.
    */
   private String drawMailtextNoLuck;
 
   private XmlDrawLogUtil xmlDrawLogUtil;
 
   /**
    * Default constructor.
    */
   public DrawProcedureLogic() {
 
     super(DrawProcedure.class);
     this.rand = new Random(System.nanoTime());
   }
 
   @Override
   @Transactional
   public synchronized void register(PriorityList list) {
 
     if (drawn) {
       throw new IllegalStateException("cannot register anymore : already drawn");
     } else if (list == null) {
       throw new IllegalArgumentException("no priorityList given (:null)");
     } else if (list.getInitiator() == null) {
       throw new IllegalArgumentException("no initiator given (:null)");
     } else if (list.getParticipant() == null) {
       throw new IllegalArgumentException("no participant given (:null)");
     }
 
     User participant = userFacade.getUserById(list.getParticipant());
 
     // check rules
     for (PriorityListItem item : list.getItems()) {
       Event event = eventFacade.getEventById(item.getEvent());
       this.checkRules(participant, event);
     }
 
     setProcedure(campaignFacade.getDrawProcedureById(getProcedure().getId()));
 
     if (list.getId() != 0L) {
       throw new ProgrammingErrorException("Priority list has not to be saved in the database!");
       // } else if (getProcedure().getPriorityLists().contains(list)) {
       // throw new ProgrammingErrorException("the priority list is not allowed to be assigned to the procedure");
     } else {
       campaignFacade.savePriorityList(list);
       getProcedure().addPriorityList(list);
       campaignFacade.updateProcedure(getProcedure());
     }
   }
 
   @Override
   public synchronized void register(Set<PriorityList> lists) {
 
     if (drawn) {
       throw new IllegalStateException("cannot register anymore : already drawn");
     } else if (lists == null || lists.size() == 0) {
       throw new IllegalArgumentException("no priorityList set given");
     }
 
     Long initiator = null;
     Long participant = null;
     for (PriorityList list : lists) {
       if (initiator == null) {
         if (list.getInitiator() == null) {
           throw new IllegalArgumentException("no initiator given (:null)");
         } else if (list.getParticipant() == null) {
           throw new IllegalArgumentException("no participant given (:null)");
         } else {
           initiator = list.getInitiator();
           participant = list.getParticipant();
         }
       } else if (initiator != list.getInitiator()) {
         throw new IllegalArgumentException("different initiators given");
       } else if (participant != list.getParticipant()) {
         throw new IllegalArgumentException("different participants given");
       }
     }
 
     // check whether an Event was added two times.
     LinkedList<Long> events = new LinkedList<Long>();
     for (PriorityList list : lists) {
       for (PriorityListItem item : getSortedPriorityListItemsOfPriorityList(list)) {
         if (events.contains(item.getEvent())) {
           throw new DuplicatePriorityListElementException(item.getEvent().toString());
         }
         events.add(item.getEvent());
       }
     }
 
     for (PriorityList list : lists) {
       register(list);
     }
   }
 
   private List<PriorityListItem> getSortedPriorityListItemsOfPriorityList(PriorityList list) {
     List<PriorityListItem> items = new ArrayList<PriorityListItem>(list.getItems());
     Collections.sort(items, new PriorityListItemPrioritySorter());
     return items;
   }
 
   @Override
   public synchronized void registerExamOnly(SingleUser initiator, User participant, Event event) {
 
     if (drawn) {
       throw new IllegalStateException("cannot register anymore : already drawn");
     }
     singleRegistration(event, participant, initiator, true);
   }
 
   @Override
   public boolean isDrawn() {
 
     return drawn;
   }
 
   @Override
   public synchronized void beforeActive() {
 
   }
 
   @Override
   public synchronized void afterActive() {
 
     logger.info("sending mails to registered participants");
     sendMails();
 
     /* priolists are deleted after mails are sent */
   }
 
   @Override
   public void whileActive() {
 
     logger.trace("whileActive of {} called", procedure.getName());
 
     if (!drawn && Calendar.getInstance().compareTo(procedure.getDrawDate()) >= 0) {
       draw();
     }
   }
 
   @Override
   public DrawProcedure getProcedure() {
 
     return procedure;
   }
 
   /**
    * this method starts the draw procedure and writes the resulting {@link ConfirmedRegistration}s as persistent objects.
    */
   @Transactional
   public synchronized void draw() {
 
     logger.info("Draw started");
     drawn = true;
 
     // get associated priority lists
     logger.info("using procedure '{}' with id '{}'", procedure.getName(), procedure.getId());
     procedure = campaignFacade.getDrawProcedureById(procedure.getId());
 
     List<PriorityList> allLists = new ArrayList<PriorityList>(procedure.getPriorityLists());
     logger.trace("Found '{}' prio lists to draw", allLists.size());
 
     int rounds = getProcedure().getMaximumPriorityListItems();
     logger.trace("Rounds to draw '{}'", rounds);
 
     // for every round work through the priority lists
     for (int round = 1; round <= rounds; round++) {
       logger.trace("Draw round {}", round);
 
       // seed randomness
       rand.setSeed(System.nanoTime());
 
       List<PriorityList> roundPool = new ArrayList<PriorityList>(allLists);
 
       while (roundPool.size() > 0) {
         PriorityList drawnList = randomPriorityList(roundPool);
         roundPool.remove(drawnList);
 
         // has this priority drawnList an item for this priority?
         PriorityListItem item = drawnList.getItem(round);
 
         if (item == null) {
           logger.trace("No item for this prio drawnList present. Skip this prio drawnList.");
           allLists.remove(drawnList);
         } else {
           Event event = eventFacade.getEventById(item.getEvent());
           logger.trace("Using item '{}' for event '{}' [{}]", new Object[]{item.getId(), event.getSubject().getName(),
               event.getEventId()});
 
           logger.trace("Event has '{}/{}' confirmed registrations", event.getConfirmedRegistrations().size(), event.getMaxParticipants());
 
           if (!event.hasPlaceLeft()) {
             logger.trace("No space left");
           } else {
             if (logger.isDebugEnabled()) {
               User user = userFacade.getUserById(item.getPriorityList().getParticipant());
               String name;
               if (user instanceof SingleUser) {
                 name = ((SingleUser) user).getName();
               } else {
                 name = ((Group) user).toString();
               }
               logger.debug("Creating registration for '{}' in event '{}'", name, event.getSubject().getName());
             }
 
             ConfirmedRegistration confirmedRegistration = ConfirmedRegistration.getInstance(item, procedure.getMandatorId());
             campaignFacade.saveConfirmedRegistration(confirmedRegistration);
 
             event.getConfirmedRegistrations().add(confirmedRegistration.getId());
             eventFacade.updateEvent(event);
 
             logger.trace("removing drawnList '{}' from draw pool", drawnList.getId());
             allLists.remove(drawnList);
           }
         }
         logger.debug("'{}' lists left", roundPool.size());
       }
     }
   }
 
   private PriorityList randomPriorityList(List<PriorityList> roundPool) {
 
     int num = rand.nextInt(roundPool.size());
 
     // get the item for this round, but update due to lazy loading
     PriorityList list = campaignFacade.updatePriorityList(roundPool.get(num));
 
     logger.trace("Drawed prio list of '{}' with id '{}'", list.getInitiator(), list.getId());
 
     return list;
   }
 
   // TODO All : Move to Notification Procedure
 
   /**
    * Sends dirty mails! (one user can get more mails when he gets more than one registration. The mails will be sent by a separate
    * thread.
    */
   private void sendMails() {
 
     Runnable task = new Runnable() {
       @Override
       public void run() {
 
         procedure = campaignFacade.getDrawProcedureById(procedure.getId());
         List<PriorityList> allLists = new ArrayList<PriorityList>(procedure.getPriorityLists());
         Map<Long, MailContent> mailPerUser = new HashMap<Long, MailContent>();
 
         for (PriorityList list : allLists) {
           MailContent mailContent = mailPerUser.get(list.getParticipant());
 
           if (mailContent == null) {
             mailContent = new MailContent(userFacade.getSingleUserById(list.getParticipant()));
             mailContent.setDrawProcedure(procedure);
             mailContent.setRegistrations(campaignFacade.findConfirmedRegistrationsByParticipantIdAndProcedure(list
                 .getParticipant(), procedure));
             mailPerUser.put(list.getParticipant(), mailContent);
           }
 
           mailContent.getPrioLists().add(list);
 
           mailPerUser.put(list.getParticipant(), mailContent);
         }
 
         for (MailContent mailContent : mailPerUser.values()) {
           sendMail(mailContent);
         }
 
         logger.info("send registrationLog");
         String drawLogAsXml = xmlDrawLogUtil.transformMailContentsToXml(mailPerUser.values());
         String correspondentEMail = procedure.getCampaign().getCorrespondentEMail();
 
         IMail mail = mailFactory.getInstance(correspondentEMail, "Registration Log", "see attachment.", "registration-log");
         mail.addByteArrayAsFileAttachment("drawLog.xml", drawLogAsXml.getBytes());
         mail.send();
 
         logger.info("remove associated priority lists");
        //campaignFacade.removePriorityListsAssociatedWithDrawProcedure(procedure);
       }
     };
 
     task.run();
   }
 
   protected void sendMail(MailContent content) {
 
     if (content == null) {
       throw new IllegalArgumentException("no mailContent was given");
     }
 
     User participant = content.getUser();
     List<Event> events = new LinkedList<Event>();
     for (ConfirmedRegistration confirmedRegistration : content.getRegistrations()) {
       try {
         events.add(eventFacade.getEventById(confirmedRegistration.getEventId()));
       } catch (NoMatchingElementException e) {
         throw new IllegalArgumentException("either event or participant id is invalid in the given confirmedRegistration");
       }
     }
 
     /** Write a mail to the user **/
     logger.trace("write a mail to the user");
 
     if (!(participant instanceof SingleUser)) {
       throw new UnsupportedOperationException("groups are not yet supported for mail sending");
     }
     SingleUser singleUser = (SingleUser) participant;
 
 
     String mailSubject = buildMailSubject();
 
     //-----------Begin edit
 
 
     Template template;
 
     VelocityContext context = new VelocityContext();
 
     context.put("name", singleUser.getName());
     context.put("campaign", procedure.getCampaign().getName());
     context.put("procedure", procedure.getName());
     context.put("priolists", convertToPrioListString(content.getPrioLists()));
 
     if (content.isDrawn()) {
       context.put("eventlist", convertToEventListString(content.getRegistrations()));
       template = templateService.loadVelocityTemplate(TemplateDetail.getInstance(procedure.getMandatorId(), TemplateType.DRAWN));
     } else {
       template = templateService.loadVelocityTemplate(TemplateDetail.getInstance(procedure.getMandatorId(), TemplateType.DRAWN_NO_LUCK));
     }
 
     StringWriter writer = new StringWriter();
 
     template.merge(context, writer);
 
     //-----------
 
     Campaign campaign = procedure.getCampaign();
 
     logger.trace("sending mail to {}", singleUser.getMail());
     IMail mail = mailFactory.getInstance(singleUser.getMail(), mailSubject, writer.toString(), campaign.getCorrespondentEMail());
     mail.send();
   }
 
   private String convertToPrioListString(List<PriorityList> lists) {
 
     StringBuffer sb = new StringBuffer();
 
     int i = 1;
     for (PriorityList list : lists) {
       sb.append("<table><tr><th>Wunschliste Kurs " + i + "</th></tr>\n");
       list = campaignFacade.getPriorityListById(list.getId());
 
       for (int priority = 1; priority <= list.getItems().size(); priority++) {
         PriorityListItem item = list.getItem(priority);
         sb.append("<tr><td>");
         Event event = eventFacade.getEventById(item.getEvent());
         sb.append(HtmlUtils.htmlEscape(formatIdSubjectNameAndDetailInformation(event)));
         sb.append("</td></tr>\n");
       }
       sb.append("</table>\n");
       i++;
     }
 
     return sb.toString();
   }
 
   private String convertToEventListString(List<ConfirmedRegistration> registrations) {
 
     StringBuffer sb = new StringBuffer();
 
     for (ConfirmedRegistration registration : registrations) {
       sb.append("<li>");
       Event event = eventFacade.getEventById(registration.getEventId());
       sb.append(formatIdSubjectNameAndDetailInformation(event));
       sb.append("</li> \n");
     }
 
     return sb.toString();
   }
 
   /**
    * @param drawMailtextNoLuck the drawMailtextNoLuck to set
    */
   public void setDrawMailtextNoLuck(String drawMailtextNoLuck) {
 
     this.drawMailtextNoLuck = drawMailtextNoLuck;
   }
 
   void setRandom(Random random) {
 
     this.rand = random;
   }
 
   public void setXmlDrawLogUtil(XmlDrawLogUtil xmlDrawLogUtil) {
     this.xmlDrawLogUtil = xmlDrawLogUtil;
   }
 }
