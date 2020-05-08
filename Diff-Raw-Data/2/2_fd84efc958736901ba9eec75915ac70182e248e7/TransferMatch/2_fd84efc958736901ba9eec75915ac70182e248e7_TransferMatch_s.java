 package svm.logic.implementation.tranferobjects;
 
 import svm.domain.abstraction.modelInterfaces.IMatch;
 import svm.logic.abstraction.exception.IllegalGetInstanceException;
 import svm.logic.abstraction.transferobjects.*;
 import svm.logic.implementation.transferobjectcreator.TransferObjectCreator;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  * Projectteam: Team C
  * Date: 05.11.12
  */
 public class TransferMatch implements ITransferMatch, IHasModel<IMatch> {
 
     private IMatch match;
     private String name;
     private Date start;
     private Date end;
     private boolean cancelled;
     private ITransferContactDetails contactDetails;
     private ITransferMatchType matchType;
     private String description;
     private String remarks;
     private ITransferTeam home;
     private ITransferTeam away;
     private Integer resultHome;


     private Integer resultAway;
 
     @Override
     public String getName() {
         return name;
     }
 
     @Override
     public Date getStart() {
         return start;
     }
 
     @Override
     public Date getEnd() {
         return end;
     }
 
     @Override
     public boolean isCanceled() {
         return cancelled;
     }
 
     @Override
     public ITransferContactDetails getContactDetails() throws IllegalGetInstanceException {
         return contactDetails;
     }
 
     @Override
     public ITransferMatchType getMatchType() throws IllegalGetInstanceException {
         return matchType;
     }
 
     @Override
     public String getDescription() {
         return description;
     }
 
     @Override
     public String getRemarks() {
         return remarks;
     }
 
     @Override
     public void setObject(Object o) throws IllegalGetInstanceException {
         this.match = (IMatch) o;
         name = match.getName();
         start = match.getStart();
         end = match.getEnd();
         cancelled = match.getCancelled();
         contactDetails = (ITransferContactDetails) TransferObjectCreator.getInstance(TransferContactDetails.class, this.match.getContactDetails());
         if (!match.getMatchType().isNull()) {
             matchType = (ITransferMatchType) TransferObjectCreator.getInstance(TransferMatchType.class, this.match.getMatchType());
         }
         description = match.getDescription();
         remarks = match.getRemarks();
         home = getHomeTeamTemp();
         away = getAwayTeamTemp();
         resultHome = match.getHomeResult();
         resultAway = match.getAwayResult();
     }
 
     private ITransferTeam getAwayTeamTemp() throws IllegalGetInstanceException {
         if (!match.getAwayExternal().isNull()) {
             return (ITransferTeam) TransferObjectCreator.getInstance(TransferExternalTeam.class, match.getAwayExternal());
         } else {
             return (ITransferTeam) TransferObjectCreator.getInstance(TransferInternalTeam.class, match.getAwayInternal());
         }
     }
 
     private ITransferTeam getHomeTeamTemp() throws IllegalGetInstanceException {
         if (!match.getHomeExternal().isNull()) {
             return (ITransferTeam) TransferObjectCreator.getInstance(TransferExternalTeam.class, match.getHomeExternal());
         } else {
             return (ITransferTeam) TransferObjectCreator.getInstance(TransferInternalTeam.class, match.getHomeInternal());
         }
     }
 
     @Override
     public Integer getResultAway() {
         return resultAway;
     }
 
     @Override
     public ITransferTeam getHomeTeam() throws IllegalGetInstanceException {
         return home;
     }
 
     @Override
     public ITransferTeam getAwayTeam() throws IllegalGetInstanceException {
         return away;
     }
 
     @Override
     public Integer getResultHome() {
         return resultHome;
     }
 
 
     @Override
     public IMatch getModel() {
         return this.match;
     }
 
     @Override
     public String toString() {
 
         String format = "dd.MM.YY hh:mm";
         SimpleDateFormat sdf = new SimpleDateFormat(format);
 
         return this.match.getName() + " (" + sdf.format(this.match.getStart()) + ")";
     }
 
 
 }
