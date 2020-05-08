 /*
  * AVRS - http://avrs.sourceforge.net/
  *
  * Copyright (C) 2011 John Gorkos, AB0OO
  *
  * AVRS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published
  * by the Free Software Foundation; either version 2 of the License,
  * or (at your option) any later version.
  *
  * AVRS is distributed in the hope that it will be useful, but
  * WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with AVRS; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  * USA
  */
 package net.ab0oo.aprs.wedjat;
 
 /**
  * @author johng
  *   This is a simple test class that connects to an APRS-IS server and parses the packets found
  *   there-in.
  */
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.ab0oo.aprs.clients.PacketListener;
 import net.ab0oo.aprs.parser.APRSPacket;
 import net.ab0oo.aprs.parser.DataExtension;
 import net.ab0oo.aprs.parser.Position;
 import net.ab0oo.aprs.parser.PositionPacket;
 import net.ab0oo.aprs.wedjat.models.MonitoredStation;
 import net.ab0oo.aprs.wedjat.models.Rule;
 import net.ab0oo.aprs.wedjat.models.Rule.RuleType;
 import net.ab0oo.aprs.wedjat.service.WedjatService;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 class WedjatClient implements PacketListener {
 
    protected static final Logger     log              = LogManager.getLogger("wedjat");
     static DecimalFormat              df               = new DecimalFormat("###.000000");
     static final String               FROM_ADDRESS     = "wedjat@www.aprs-alert.net";
     static final long                 UPDATE_INTERVAL  = 60 * 1 * 1000;                                     // 1
                                                                                                              // minute;
     static final int                  TRIGGER_DISTANCE = 75;
     private static NotificationThread nt               = new NotificationThread();
     // private static int packets = 0;
     private static long               lastUpdate       = 0;
    private static int                packets          = 0;
    private static int                positionPackets  = 0;
     private WedjatService             wedjatService;
     private Map<String, LastPosition> lastPositions    = new HashMap<String, LastPosition>();
     private List<NotificationInfo>    notificationList = new ArrayList<NotificationInfo>();
     private Object                    lockingObject    = new Object();
 
     /**
      * @param notificationList
      *            the notificationList to set
      */
     public final void setNotificationList(List<NotificationInfo> notificationList) {
         this.notificationList = notificationList;
     }
 
     /**
      * @param lockingObject
      *            the lockingObject to set
      */
     public final void setLockingObject(Object lockingObject) {
         this.lockingObject = lockingObject;
     }
 
     public WedjatClient() throws IOException {
         System.out.println("Starting wedjet client!");
         System.out.println("Started!");
     }
 
     public void init() {
         log.info("Initializing Wedjat Client");
         nt.setLockingObject(lockingObject);
         nt.setWedjatService(wedjatService);
         nt.setNotificationList(notificationList);
         Thread notificationThread = new Thread(nt);
         notificationThread.start();
         log.info("Wedjat Client initialized, sending startup mail");
         SendMail outboundMail = new SendMail("alert@aprs-alert.net", "jgorkos@gmail.com", "Wedjat Startup",
             "Wedjat has just initialized.  Just thought you'd like to know.");
         outboundMail.send();
         log.info("Mail sent, beginning normal operation");
 
     }
 
     @Override
     public void processPacket(APRSPacket packet) {
         packets++;
         if (packet.isAprs() && packet.getAprsInformation() instanceof PositionPacket) {
             PositionPacket pp = (PositionPacket) packet.getAprsInformation();
             DataExtension de = pp.getExtension();
             if (de != null) {
                 positionPackets++;
                 processAlertRule(packet.getSourceCall(), pp);
             }
         }
         if (System.currentTimeMillis() > lastUpdate + UPDATE_INTERVAL) {
             lastUpdate = System.currentTimeMillis();
             System.out.println(new Date() + ": Total Packets:  " + packets + ", Position Packets: " + positionPackets
                     + " Tracked Stations:  " + lastPositions.size());
             log.info("Total Packets: " + packets + ", Position Packets: " + positionPackets
                 + " Tracked Stations: " + lastPositions.size());
         }
 
     }
 
     public void processAlertRule(String sourceCall, PositionPacket pp) {
         // this call can be more expensive than we want to pay. need to cache it.
         List<MonitoredStation> ms = wedjatService.getMonitoredStationsList(sourceCall.toUpperCase());
         Position lastPosition = null;
         // this statement ensures that we will NEVER alert on a single packet
         // from a station; we require two packets from a station: the first
         // establishes a baseline position, and the second and subsequent packets
         // are tested for movement/incursions, etc
         // System.out.println("Processing packet for "+sourceCall);
         if (lastPositions.containsKey(sourceCall)) {
             lastPosition = lastPositions.get(sourceCall).getLastPosition();
             for (MonitoredStation oneCall : ms) {
                 List<Rule> rules = wedjatService.getActiveRulesByStationId(oneCall.getStationId());
                 if (rules.size() > 0) {
                     log.debug("There are " + rules.size() + " active rules for " + sourceCall);
                 }
                 for (Rule oneRule : rules) {
                     Position currentPosition = pp.getPosition();
                     if (oneRule.getRuleType() == RuleType.MOVEMENT) {
                         if (currentPosition.distance(lastPositions.get(sourceCall).getLastPosition()) > TRIGGER_DISTANCE) {
                             int updateCount = wedjatService.resetRuleTimer(oneRule.getRuleId());
                             if (updateCount > 0) {
                                 log.debug(updateCount + " rows updated for " + sourceCall + " rule "
                                         + oneRule.getRuleId());
                                 notify(sourceCall, oneRule, pp);
                             }
                         }
                     } else if (oneRule.getRuleType() == RuleType.INCURSION
                             && wedjatService.isIncursion(lastPosition, currentPosition, oneRule.getZoneId())) {
                         // trigger notifications for this rule & reset rule timer
                         int updateCount = wedjatService.resetRuleTimer(oneRule.getRuleId());
                         if (updateCount > 0) {
                             log.debug(updateCount+" rows updated for "+sourceCall+" incursion rule "+oneRule.getRuleId());
                             notify(sourceCall, oneRule, pp);
                         }
                     } else if (oneRule.getRuleType() == RuleType.EXCURSION
                             && wedjatService.isExcursion(lastPosition, currentPosition, oneRule.getZoneId())) {
                         // trigger notifications for this rule & reset rule timer
                         int updateCount = wedjatService.resetRuleTimer(oneRule.getRuleId());
                         if (updateCount > 0) {
                             log.debug(updateCount+" rows updated for "+sourceCall+" excursion rule "+oneRule.getRuleId());
                             notify(sourceCall, oneRule, pp);
                         }
                     }
                 }
 
             }
         }
         LastPosition lp = new LastPosition(pp.getPosition());
         lastPositions.put(sourceCall, lp);
         return;
     }
 
     public void notify(String sourceCall, Rule rule, PositionPacket position) {
         NotificationInfo ni = new NotificationInfo(sourceCall, rule, position);
         notificationList.add(ni);
         synchronized (lockingObject) {
             lockingObject.notify();
         }
     }
 
     /**
      * @param lastPositions
      *            the lastPositions to set
      */
     public final void setLastPositions(HashMap<String, LastPosition> lastPositions) {
         this.lastPositions = lastPositions;
     }
 
     /**
      * @param wedjatService
      *            the wedjatService to set
      */
     public final void setWedjatService(WedjatService wedjatService) {
         this.wedjatService = wedjatService;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see net.ab0oo.aprs.clients.PacketListener#setOutputStream(java.io.DataOutputStream)
      */
     @Override
     public void setOutputStream(DataOutputStream outToServer) {
         // TODO Auto-generated method stub
 
     }
 }
