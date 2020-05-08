 /* ********************************************************************
     Licensed to Jasig under one or more contributor license
     agreements. See the NOTICE file distributed with this work
     for additional information regarding copyright ownership.
     Jasig licenses this file to you under the Apache License,
     Version 2.0 (the "License"); you may not use this file
     except in compliance with the License. You may obtain a
     copy of the License at:
 
     http://www.apache.org/licenses/LICENSE-2.0
 
     Unless required by applicable law or agreed to in writing,
     software distributed under the License is distributed on
     an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
     KIND, either express or implied. See the License for the
     specific language governing permissions and limitations
     under the License.
 */
 package org.bedework.synch;
 
 import org.bedework.synch.BaseSubscriptionInfo.CrudCts;
 import org.bedework.synch.Notification.NotificationItem;
 import org.bedework.synch.cnctrs.ConnectorInstance;
 import org.bedework.synch.cnctrs.ConnectorInstance.ItemInfo;
 import org.bedework.synch.cnctrs.ConnectorInstance.SynchItemsInfo;
 import org.bedework.synch.exception.SynchException;
 import org.bedework.synch.wsmessages.SubscribeResponseType;
 import org.bedework.synch.wsmessages.SynchDirectionType;
 import org.bedework.synch.wsmessages.SynchEndType;
 import org.bedework.synch.wsmessages.UnsubscribeRequestType;
 import org.bedework.synch.wsmessages.UnsubscribeResponseType;
 
 import edu.rpi.cmt.calendar.XcalUtil;
 import edu.rpi.cmt.calendar.diff.XmlIcalCompare;
 
 import org.apache.log4j.Logger;
 import org.oasis_open.docs.ns.wscal.calws_soap.AddItemResponseType;
 import org.oasis_open.docs.ns.wscal.calws_soap.ComponentSelectionType;
 import org.oasis_open.docs.ns.wscal.calws_soap.ErrorCodeType;
 import org.oasis_open.docs.ns.wscal.calws_soap.FetchItemResponseType;
 import org.oasis_open.docs.ns.wscal.calws_soap.StatusType;
 import org.oasis_open.docs.ns.wscal.calws_soap.TargetDoesNotExistType;
 import org.oasis_open.docs.ns.wscal.calws_soap.UpdateItemResponseType;
 import org.oasis_open.docs.ns.wscal.calws_soap.UpdateItemType;
 
 import ietf.params.xml.ns.icalendar_2.ArrayOfComponents;
 import ietf.params.xml.ns.icalendar_2.ArrayOfProperties;
 import ietf.params.xml.ns.icalendar_2.AttendeePropType;
 import ietf.params.xml.ns.icalendar_2.BaseComponentType;
 import ietf.params.xml.ns.icalendar_2.BasePropertyType;
 import ietf.params.xml.ns.icalendar_2.IcalendarType;
import ietf.params.xml.ns.icalendar_2.MethodPropType;
 import ietf.params.xml.ns.icalendar_2.OrganizerPropType;
 import ietf.params.xml.ns.icalendar_2.ValarmType;
 import ietf.params.xml.ns.icalendar_2.VcalendarType;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.bind.JAXBElement;
 import javax.xml.ws.Holder;
 

 /** The synchling handles the processing of a single subscription when there is
  * some activity.
  *
  * <p>A synchling may be started to process a subscription as a the result of a
  * callback notification for example from exchange or because a synch period has
  * elapsed and it's time to refresh.
  *
  * @author Mike Douglass
  */
 public class Synchling {
   private boolean debug;
 
   protected transient Logger log;
 
   private static volatile Object synchlingIdLock = new Object();
 
   private static long lastSynchlingId;
 
   private long synchlingId;
 
   private SynchEngine syncher;
 
  private XmlIcalCompare diff;

   // Subscription id used when getting the diff object.
   private String diffSubid;
 
   /* Max number of items we fetch at a time */
   private final int getItemsBatchSize = 20;
 
   /** Constructor
    *
    * @param syncher
    * @throws SynchException
    */
   public Synchling(final SynchEngine syncher) throws SynchException {
     debug = getLogger().isDebugEnabled();
 
     this.syncher = syncher;
 
     synchronized (synchlingIdLock) {
       lastSynchlingId++;
       synchlingId = lastSynchlingId;
     }
   }
 
   /**
    * @return unique id
    */
   public long getSynchlingId() {
     return synchlingId;
   }
 
   /**
    * @param note
    * @return OK for all handled fine. ERROR - discard. WARN - retry.
    * @throws SynchException
    */
   public StatusType handleNotification(final Notification<NotificationItem> note) throws SynchException {
     StatusType st;
 
     for (NotificationItem ni: note.getNotifications()) {
 
       switch (ni.getAction()) {
       case FullSynch:
         st = reSynch(note);
         if (st != StatusType.OK) {
           return st;
         }
         continue;
 
       case CopiedEvent:
         break;
       case CreatedEvent:
         st = addItem(note, ni);
         if (st != StatusType.OK) {
           return st;
         }
         continue;
 
       case DeletedEvent:
         break;
       case ModifiedEvent:
         st = updateItem(note, ni);
         if (st != StatusType.OK) {
           return st;
         }
         continue;
 
       case MovedEvent:
         break;
       case NewMailEvent:
         break;
       case StatusEvent:
         break;
 
       case NewSubscription:
         ni.getSubResponse().setStatus(subscribe(note, ni));
         if (ni.getSubResponse().getStatus() != StatusType.OK) {
           return ni.getSubResponse().getStatus();
         }
 
         ni.getSubResponse().setSubscriptionId(note.getSubscriptionId());
 
         /* Now put it on the queue for a refresh */
         syncher.setConnectors(note.getSub());
 
         syncher.reschedule(note.getSub());
 
         continue;
 
       case Unsubscribe:
         st = unsubscribe(note, ni);
         if (st != StatusType.OK) {
           return st;
         }
         continue;
       }
 
       return StatusType.ERROR;
     }
 
     note.getSub().setErrorCt(0);
     return StatusType.OK;
   }
 
   /* ====================================================================
    *                        Notification methods
    * ==================================================================== */
 
 
   private StatusType subscribe(final Notification note,
                          final NotificationItem ni) throws SynchException {
     if (debug) {
       trace("new subscription " + note.getSub());
     }
 
     syncher.setConnectors(note.getSub());
 
     /* Try to subscribe to both ends */
     ConnectorInstance cinst = syncher.getConnectorInstance(note.getSub(),
                                                            SynchEndType.A);
 
     SubscribeResponseType sr = ni.getSubResponse();
 
     if (!cinst.subscribe(sr)) {
       return sr.getStatus();
     }
 
     cinst = syncher.getConnectorInstance(note.getSub(),
                                          SynchEndType.B);
     if (!cinst.subscribe(sr)) {
       return sr.getStatus();
     }
 
     syncher.addSubscription(note.getSub());
     return StatusType.OK;
   }
 
   private StatusType addItem(final Notification note,
                              final NotificationItem ni) throws SynchException {
     IcalendarType ical = ni.getIcal();
 
     if (ical == null) {
       if (debug) {
         trace("No item found");
       }
 
       return StatusType.ERROR;
     }
 
     AddItemResponseType air = getOtherCinst(note).addItem(ical);
     if (debug) {
       trace("Add: status=" + air.getStatus() +
             " msg=" + air.getMessage());
     }
 
     return air.getStatus();
   }
 
   private StatusType updateItem(final Notification note,
                                 final NotificationItem ni) throws SynchException {
     IcalendarType ical = ni.getIcal();
 
     if (ical == null) {
       if (debug) {
         trace("No item found");
       }
 
       return StatusType.ERROR;
     }
 
     ConnectorInstance cinst = getOtherCinst(note);
 
     FetchItemResponseType fresp = cinst.fetchItem(ni.getUid());
     if (debug) {
       trace("Update: status=" + fresp.getStatus() +
             " msg=" + fresp.getMessage());
     }
 
     if (fresp.getStatus() != StatusType.OK) {
       return fresp.getStatus();
     }
 
     IcalendarType targetIcal = fresp.getIcalendar();
 
     ComponentSelectionType cst = getDiffer(note).diff(ical, targetIcal);
 
     if (cst == null) {
       if (debug) {
         trace("No update needed for " + ni.getUid());
       }
 
       return StatusType.OK;
     }
 
     UpdateItemType ui = new UpdateItemType();
 
     ui.setHref(fresp.getHref());
     ui.setChangeToken(fresp.getChangeToken());
     ui.getSelect().add(cst);
 
     UpdateItemResponseType uir = cinst.updateItem(ui);
     if (debug) {
       trace("Update: status=" + uir.getStatus() +
             " msg=" + uir.getMessage());
     }
     return uir.getStatus();
   }
 
   private ConnectorInstance getOtherCinst(final Notification note) throws SynchException {
     SynchEndType otherEnd;
     if (note.getEnd() == SynchEndType.A) {
       otherEnd = SynchEndType.B;
     } else {
       otherEnd = SynchEndType.A;
     }
 
     return syncher.getConnectorInstance(note.getSub(),
                                         otherEnd);
   }
 
   /* ====================================================================
    *                        private methods
    * ==================================================================== */
 
   /**
    * @param note
    * @return status
    * @throws SynchException
    */
   private StatusType unsubscribe(final Notification note,
                                  final NotificationItem ni) throws SynchException {
     Subscription sub = note.getSub();
     if (sub == null){
       return StatusType.ERROR;
     }
 
     if (!checkAccess(sub)) {
       info("No access for subscription " + sub);
       return StatusType.NO_ACCESS;
     }
 
     syncher.setConnectors(sub);
 
     /* See if it's OK by the connector instances */
 
     ConnectorInstance cinst = syncher.getConnectorInstance(sub,
                                                            SynchEndType.A);
 
     UnsubscribeRequestType usreq = ni.getUnsubRequest();
     UnsubscribeResponseType usr = ni.getUnsubResponse();
 
     if (!cinst.unsubscribe(usreq, usr)) {
       return usr.getStatus();
     }
 
     cinst = syncher.getConnectorInstance(note.getSub(),
                                          SynchEndType.B);
     if (!cinst.unsubscribe(usreq, usr)) {
       return usr.getStatus();
     }
 
     // Unsubscribe request - call connector instance to carry out any required
     // action
     sub.setOutstandingSubscription(null);
     sub.setDeleted(true);
 
     syncher.deleteSubscription(sub);
 
     return StatusType.OK;
   }
 
   private static class SynchInfo {
     /** */
     public ItemInfo itemInfo;
 
     /* Fields set during the actual synch process */
 
     /** add to none, A or B */
     public SynchEndType addTo = SynchEndType.NONE;
 
     /** Update none, A or B */
     public SynchEndType updateEnd = SynchEndType.NONE;
 
     /** delete none, A or B */
     public SynchEndType deleteFrom = SynchEndType.NONE;
 
     /** both ends changed since last synch */
     public boolean conflict;
 
     /** Constructor
      *
      * @param itemInfo
      */
     public SynchInfo(final ItemInfo itemInfo) {
       this.itemInfo = itemInfo;
     }
 
     @Override
     public String toString() {
       StringBuilder sb = new StringBuilder("SynchInfo{");
 
       sb.append(itemInfo);
 
       sb.append("}");
 
       return sb.toString();
     }
   }
 
   /** Information and objects needed to process one end of a resynch
    */
   private static class ResynchInfo {
     Subscription sub;
     SynchEndType end;
     boolean trustLastmod;
     ConnectorInstance inst;
     Map<String, ItemInfo> items;
     CrudCts lastCts;
     CrudCts totalCts;
 
     Map<String, Object> stripMap = new HashMap<String, Object>();
 
     // True if our target is missing.
     boolean missingTarget;
 
     ResynchInfo(final Subscription sub,
                 final SynchEndType end,
                 final boolean trustLastmod,
                 final ConnectorInstance inst) throws SynchException {
       this.sub = sub;
       this.end = end;
       this.trustLastmod = trustLastmod;
       this.inst = inst;
 
       makeStripMap(stripMap, sub);
 
       lastCts = new CrudCts();
       inst.setLastCrudCts(lastCts);
       totalCts = inst.getTotalCrudCts();
     }
 
     void updateCts() throws SynchException {
       inst.setLastCrudCts(lastCts);
       inst.setTotalCrudCts(totalCts);
     }
   }
 
   private StatusType reSynch(final Notification note) throws SynchException {
     Subscription sub = note.getSub();
 
     try {
       /* The action here depends on which way we are synching.
        *
        * For A to B
        * If the item does not exist on the B system then add it to B.
        * If the lastmod on B is prior to the A one - update.
        * Otherwise ignore.
        * We should remove all B events that have no counterpart on A. (though
        * this may be a parameter)
        *
        * For B to A
        * Just the reverse of the above.
        *
        * For both ways:
        * This is essentially do both the above. The intent is to ensure that
        * we add appropriate events to either end and possibly remove from either
        * end till we are left with the full overlapping set. That's the easy bit.
        *
        * Updates are easy as long as the set of updated events from both ends
        * does not overlap. For the non-overlapping event just update the older
        * event from the newer event.
        *
        * For the rest, if one end is designated master, then  update the non-master
        * from the master. Otherwise we probably need to flag the event.
        *
        * Note that we need to have a last-synched stamp in each event to
        * determine if the event has changed since last synched. For conflict
        * detection we also need a reliable lastmod which we store in the
        * last synched.
        */
 
       boolean bothWays = sub.getDirection() == SynchDirectionType.BOTH_WAYS;
 
       ResynchInfo ainfo = new ResynchInfo(sub,
                                           SynchEndType.A,
                                           sub.getEndAConn().getTrustLastmod(),
                                           syncher.getConnectorInstance(sub,
                                                                        SynchEndType.A));
       ResynchInfo binfo = new ResynchInfo(sub,
                                           SynchEndType.B,
                                           sub.getEndBConn().getTrustLastmod(),
                                           syncher.getConnectorInstance(sub,
                                                                        SynchEndType.B));
 
       boolean aChanged = false;
       boolean bChanged = false;
 
       if ((sub.getDirection() == SynchDirectionType.A_TO_B) || bothWays) {
         aChanged = ainfo.inst.changed();
       }
 
       if ((sub.getDirection() == SynchDirectionType.B_TO_A) || bothWays) {
         bChanged = binfo.inst.changed();
       }
 
       if (!aChanged && !bChanged) {
         // Nothing to do. last refresh updated on the way out.
         return StatusType.OK;
       }
 
       sub.setMissingTarget(false);
 
       ainfo.items = getItemsMap(ainfo);
       if (ainfo.items == null) {
         if (ainfo.missingTarget) {
           sub.setMissingTarget(true);
         }
         return StatusType.ERROR;
       }
 
       binfo.items = getItemsMap(binfo);
       if (binfo.items == null) {
         if (binfo.missingTarget) {
           sub.setMissingTarget(true);
         }
         return StatusType.ERROR;
       }
 
       /* updateInfo is a list of changes we need to apply to one or both ends
        */
       List<SynchInfo> updateInfo = new ArrayList<SynchInfo>();
 
       /* First see what we need to transfer from A to B */
       if ((sub.getDirection() == SynchDirectionType.A_TO_B) || bothWays) {
         getResynchs(updateInfo, ainfo, binfo);
       }
 
       /* Now B to A */
       if ((sub.getDirection() == SynchDirectionType.B_TO_A) || bothWays) {
         getResynchs(updateInfo, binfo, ainfo);
       }
 
       if ((sub.getDirection() == SynchDirectionType.A_TO_B) || bothWays) {
         checkDeletes(updateInfo, binfo);
       }
 
       if ((sub.getDirection() == SynchDirectionType.B_TO_A) || bothWays) {
         checkDeletes(updateInfo, ainfo);
       }
 
       if (debug) {
         trace("---------------- update set ----------------");
         for (SynchInfo si: updateInfo) {
           trace(si.toString());
         }
         trace("---------------- end update set ----------------");
       }
 
       if (updateInfo.size() > 0) {
         Holder<List<SynchInfo>> unprocessedRes = new Holder<List<SynchInfo>>();
 
         /* Now update end A from end B.
          */
         if ((sub.getDirection() == SynchDirectionType.B_TO_A) || bothWays) {
           while ((updateInfo.size() > 0) &&
                  processUpdates(note, updateInfo, unprocessedRes,
                                 binfo, ainfo)) {
             updateInfo = unprocessedRes.value;
           }
 
           ainfo.updateCts();
         }
 
         /* Now update end B from end A.
          */
         if ((sub.getDirection() == SynchDirectionType.A_TO_B) || bothWays) {
           while ((updateInfo.size() > 0) &&
                  processUpdates(note, updateInfo, unprocessedRes,
                                 ainfo, binfo)) {
             updateInfo = unprocessedRes.value;
           }
 
           binfo.updateCts();
         }
       }
 
       /* -------------------- Deletions ------------------------ */
 
       if (((updateInfo.size() > 0) &&
           (sub.getDirection() == SynchDirectionType.B_TO_A)) || bothWays) {
         processDeletes(note, updateInfo, ainfo);
       }
 
       if (((updateInfo.size() > 0) &&
           (sub.getDirection() == SynchDirectionType.A_TO_B)) || bothWays) {
         processDeletes(note, updateInfo, binfo);
       }
 
       sub.setErrorCt(0);
 
       return StatusType.OK;
     } catch (SynchException se) {
       throw se;
     } catch (Throwable t) {
       throw new SynchException(t);
     } finally {
       sub.updateLastRefresh();
       syncher.updateSubscription(sub);
       syncher.reschedule(sub);
     }
   }
 
   private void getResynchs(final List<SynchInfo> updateInfo,
                            final ResynchInfo fromInfo,
                            final ResynchInfo toInfo) throws SynchException {
     boolean useLastmods = fromInfo.trustLastmod && toInfo.trustLastmod;
 
     for (ItemInfo fromIi: fromInfo.items.values()) {
       ItemInfo toIi = toInfo.items.get(fromIi.uid);
 
       if (toIi == null) {
         /* It's not in the to list - add to list to fetch from the from end */
         if (debug) {
           trace("Need to add to end " + toInfo.end + ": uid:" + fromIi.uid);
         }
 
         SynchInfo si = new SynchInfo(fromIi);
         si.addTo = toInfo.end;
         updateInfo.add(si);
         continue;
       }
 
       /* It is at the to end - mark as seen then compare to see if
        * we need to update
        */
       toIi.seen = true;
 
       boolean update = true;
 
       if (useLastmods) {
         update = cmpLastMods(toIi.lastMod, fromIi.lastMod) < 0;
       }
 
       if (!update) {
         if (debug) {
           trace("No need to update end " + toInfo.end + ": uid:" + fromIi.uid);
         }
       }
 
       if (debug) {
         trace("Need to update end " + toInfo.end + ": uid:" + fromIi.uid);
       }
 
       SynchInfo si = new SynchInfo(fromIi);
 
       si.updateEnd = toInfo.end;
       updateInfo.add(si);
     }
   }
 
   private void checkDeletes(final List<SynchInfo> updateInfo,
                             final ResynchInfo toInfo) throws SynchException {
     for (ItemInfo ii: toInfo.items.values()) {
       if (ii.seen) {
         continue;
       }
 
       SynchInfo si = new SynchInfo(ii);
       /* If the lastmod is later than the last synch and this is 2 way then
        * this one got added after we synched. Add it to end B.
        *
        * If the lastmod is previous to our last synch then this one needs to
        * be deleted.
        */
       si.deleteFrom = toInfo.end;
       updateInfo.add(si);
     }
   }
 
   /**
    * @param rinfo
    * @return map or null for error
    * @throws SynchException
    */
   private Map<String, ItemInfo> getItemsMap(final ResynchInfo rinfo) throws SynchException {
     /* Items is a table built from the endB calendar */
     Map<String, ItemInfo> items = new HashMap<String, ItemInfo>();
 
     SynchItemsInfo sii = rinfo.inst.getItemsInfo();
     if (sii.getStatus() != StatusType.OK) {
       if ((sii.getErrorResponse() != null) &&
           (sii.getErrorResponse().getError() != null)) {
         // More information
         ErrorCodeType ecode = sii.getErrorResponse().getError().getValue();
         if (ecode instanceof TargetDoesNotExistType) {
           // The target we are addressing is no longer available.
           rinfo.missingTarget = true;
         }
       }
       rinfo.sub.setErrorCt(rinfo.sub.getErrorCt() + 1);
       return null;
     }
 
     for (ItemInfo ii: sii.items) {
       if (debug) {
         trace(ii.toString());
       }
       ii.seen = false;
       items.put(ii.uid, ii);
     }
 
     return items;
   }
 
   /** Do the adds and updates for the end specified by toInfo.
    *
    * @param note
    * @param updateInfo
    * @param unprocessedRes
    * @param fromInfo
    * @param toInfo
    * @return true if there are unprocessed entries for this end
    * @throws SynchException
    */
   private boolean processUpdates(final Notification note,
                                  final List<SynchInfo> updateInfo,
                                  final Holder<List<SynchInfo>> unprocessedRes,
                                  final ResynchInfo fromInfo,
                                  final ResynchInfo toInfo) throws SynchException {
     boolean callAgain = false;
     List<SynchInfo> unProcessed = new ArrayList<SynchInfo>();
     unprocessedRes.value = unProcessed;
 
     List<String> uids = new ArrayList<String>();
     List<SynchInfo> sis = new ArrayList<SynchInfo>();
 
     int i = 0;
     /* First make a batch of items to fetch */
 
     while (i < updateInfo.size()) {
       SynchInfo si = updateInfo.get(i);
       i++;
 
       // Add to unprocessed if it's not one of ours
       if ((si.addTo != toInfo.end) && (si.updateEnd != toInfo.end)) {
         unProcessed.add(si);
         continue;
       }
 
       // Add to unprocessed if the batch is big enough
       if (uids.size() == getItemsBatchSize) {
         unProcessed.add(si);
         callAgain = true;
         continue;
       }
 
       uids.add(si.itemInfo.uid);
       sis.add(si);
     }
 
     if (uids.size() == 0) {
       // Nothing left to do
       return false;
     }
 
     List<FetchItemResponseType> firs = fromInfo.inst.fetchItems(uids);
 
     Iterator<SynchInfo> siit = sis.iterator();
     for (FetchItemResponseType fir: firs) {
       SynchInfo si = siit.next();
 
       if (si.addTo == toInfo.end) {
         IcalendarType stripped = stripIcal(toInfo.stripMap, fir.getIcalendar());
         AddItemResponseType air = toInfo.inst.addItem(stripped);
 
         toInfo.lastCts.created++;
         toInfo.totalCts.created++;
 
         if (debug) {
           trace("Add: status=" + air.getStatus() +
                 " msg=" + air.getMessage());
         }
 
         continue;
       }
 
       if (si.updateEnd == toInfo.end) {
         // Update the instance
         FetchItemResponseType toFir = toInfo.inst.fetchItem(si.itemInfo.uid);
 
         if (toFir.getStatus() != StatusType.OK) {
           warn("Unable to fetch destination entity for update: message was " +
                toFir.getMessage());
           continue;
         }
 
         ComponentSelectionType cst = getDiffer(note).diff(fir.getIcalendar(),
                                                           toFir.getIcalendar());
 
         if (cst == null) {
           if (debug) {
             trace("No update needed for " + si.itemInfo.uid);
           }
 
           continue;
         }
 
         if (debug) {
           trace("Update needed for " + si.itemInfo.uid);
         }
 
         UpdateItemType ui = new UpdateItemType();
 
         ui.setHref(toFir.getHref());
         ui.setChangeToken(toFir.getChangeToken());
         ui.getSelect().add(cst);
 
         UpdateItemResponseType uir = toInfo.inst.updateItem(ui);
 
         if (uir.getStatus() != StatusType.OK) {
           warn("Unable to update destination entity");
           continue;
         }
 
         toInfo.lastCts.updated++;
         toInfo.totalCts.updated++;
 
         continue;
       }
 
       warn("Should not get here");
     }
 
     return callAgain;
   }
 
   /* Remove all the properties or components we are not sending to the "to" end.
    */
   private IcalendarType stripIcal(final Map<String, Object> stripMap,
                                   final IcalendarType val) throws SynchException {
     try {
       if ((stripMap == null) || stripMap.isEmpty()) {
         return val;
       }
 
       IcalendarType res = new IcalendarType();
 
       for (VcalendarType vcal: val.getVcalendar()) {
         VcalendarType v = (VcalendarType)XcalUtil.cloneComponent(vcal);
         res.getVcalendar().add(v);
 
         v.setProperties(stripProps(stripMap, vcal));
         v.setComponents(stripComps(stripMap, vcal));
       }
 
       return res;
     } catch (SynchException se) {
       throw se;
     } catch (Throwable t) {
       throw new SynchException(t);
     }
   }
 
   @SuppressWarnings("unchecked")
   private ArrayOfComponents stripComps(final Map<String, Object> stripMap,
                                        final BaseComponentType val) throws SynchException {
     try {
       ArrayOfComponents comps = val.getComponents();
 
       if (comps == null) {
         return null;
       }
 
       ArrayOfComponents c = new ArrayOfComponents();
 
       boolean stripped = false;
 
       for (JAXBElement jaxbCcomp:
            comps.getBaseComponent()) {
         BaseComponentType jcomp = (BaseComponentType)jaxbCcomp.getValue();
 
         /* Skip if we don't want this component */
         if (stripMap.get(jcomp.getClass().getCanonicalName()) != null) {
           stripped = true;
           continue;
         }
 
         /* Possibly remove some properties */
         ArrayOfProperties p = stripProps(stripMap, jcomp);
 
         if (jcomp.getProperties() != p) {
           // Some properties were removed
           stripped = true;
 
           jcomp = XcalUtil.cloneComponent(jcomp);
           jcomp.setProperties(p);
 
           jaxbCcomp.setValue(jcomp);
         }
 
         /* Possibly remove some sub-components */
         ArrayOfComponents strippedComps = stripComps(stripMap, jcomp);
 
         if (jcomp.getComponents() != strippedComps) {
           // Some components were removed
           stripped = true;
 
           BaseComponentType comp = XcalUtil.cloneComponent(jcomp);
           comp.setProperties(jcomp.getProperties());
           comp.setComponents(strippedComps);
 
           jaxbCcomp.setValue(comp);
         }
 
         c.getBaseComponent().add(jaxbCcomp);
       }
 
       if (stripped) {
         return c;
       }
 
       return comps;
     } catch (SynchException se) {
       throw se;
     } catch (Throwable t) {
       throw new SynchException(t);
     }
   }
 
   private ArrayOfProperties stripProps(final Map<String, Object> stripMap,
                                        final BaseComponentType val) throws SynchException {
     ArrayOfProperties props = val.getProperties();
 
     if (props == null) {
       return null;
     }
 
     ArrayOfProperties p = new ArrayOfProperties();
 
     boolean stripped = false;
 
     for (JAXBElement<? extends BasePropertyType> jprop:
          props.getBasePropertyOrTzid()) {
       if (stripMap.get(jprop.getValue().getClass().getCanonicalName()) != null) {
         stripped = true;
         continue;
       }
 
       //XXX Should do params
 
       p.getBasePropertyOrTzid().add(jprop);
     }
 
     if (stripped) {
       return p;
     }
 
     return props;
   }
 
   /**
    * @param note
    * @param updateInfo
    * @param toInfo
    * @throws SynchException
    */
   private void processDeletes(final Notification note,
                               final List<SynchInfo> updateInfo,
                               final ResynchInfo toInfo) throws SynchException {
     for (SynchInfo si: updateInfo) {
       // Add to unprocessed if it's not one of ours
       if (si.deleteFrom != toInfo.end) {
         continue;
       }
 
       //DeleteItemResponseType dir = toInfo.inst.deleteItems(si.itemInfo.uid);
     }
   }
 
   private int cmpLastMods(final String calLmod, final String exLmod) {
     int exi = 0;
     if (calLmod == null) {
       return -1;
     }
 
     for (int i = 0; i < 16; i++) {
       char cal = calLmod.charAt(i);
       char ex = exLmod.charAt(exi);
 
       if (cal < ex) {
         return -1;
       }
 
       if (cal > ex) {
         return 1;
       }
 
       exi++;
 
       // yyyy-mm-ddThh:mm:ssZ
       //     4  7     12 15
       if ((exi == 4) || (exi == 7) || (exi == 12) || (exi == 15)) {
         exi++;
       }
     }
 
     return 0;
   }
 
   private boolean checkAccess(final Subscription sub) throws SynchException {
     /* Does this principal have the rights to (un)subscribe? */
     return true;
   }
 
   @SuppressWarnings("unchecked")
   private XmlIcalCompare getDiffer(final Notification note) throws SynchException {
     Subscription sub = note.getSub();
 
     if ((diff != null) &&
         (diffSubid != null) &&
         diffSubid.equals(sub.getSubscriptionId())) {
       return diff;
     }
 
     /* Make up diff lists - use a map to make them unique */
     Map<String, Object> skipMap = new HashMap<String, Object>();
 
     /* First the defaults */
     addSkips(skipMap, XmlIcalCompare.defaultSkipList);
    addSkip(skipMap, new MethodPropType());

     makeStripMap(skipMap, sub);
 
     addSkips(skipMap, sub.getEndAConn().getSkipList());
     addSkips(skipMap, sub.getEndBConn().getSkipList());
 
     List<Object> skipList = new ArrayList<Object>(skipMap.values());
 
     diffSubid = sub.getSubscriptionId();
    diff = new XmlIcalCompare(skipList, SynchEngine.getTzGetter());
     return diff;
   }
 
  /** This adds the properties we are going to strip out of an event we add to
   * either end. This depends upon the global subscription properties. These are
   * also properties we ignore when comparing entries.
   *
   * @param stripMap
   * @param sub
   * @throws SynchException
   */
   private static void makeStripMap(final Map<String, Object> stripMap,
                                    final Subscription sub) throws SynchException {
     if (sub.getInfo() == null) {
       return;
     }
 
    addSkip(stripMap, new MethodPropType());

     /* Any needed for stuff we skip */
     if (sub.getInfo().getStripAlarms()) {
       addSkip(stripMap, new ValarmType());
     }
 
     if (sub.getInfo().getStripScheduling()) {
       addSkip(stripMap, new OrganizerPropType());
       addSkip(stripMap, new AttendeePropType());
     }
   }
 
   private void addSkips(final Map<String, Object> skipMap,
                         final List<Object> skipList) {
     if (skipList == null) {
       return;
     }
 
     for (Object o: skipList) {
       addSkip(skipMap, o);
     }
   }
 
   private static void addSkip(final Map<String, Object> skipMap,
                               final Object o) {
     skipMap.put(o.getClass().getCanonicalName(), o);
   }
 
   private Logger getLogger() {
     if (log == null) {
       log = Logger.getLogger(this.getClass());
     }
 
     return log;
   }
 
   private void trace(final String msg) {
     getLogger().debug(msg);
   }
 
   private void warn(final String msg) {
     getLogger().warn(msg);
   }
 
   @SuppressWarnings("unused")
   private void error(final Throwable t) {
     getLogger().error(this, t);
   }
 
   private void info(final String msg) {
     getLogger().info(msg);
   }
 }
