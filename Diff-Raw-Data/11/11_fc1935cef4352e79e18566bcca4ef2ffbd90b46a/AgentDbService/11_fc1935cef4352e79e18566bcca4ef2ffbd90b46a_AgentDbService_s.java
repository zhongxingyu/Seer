 package com.razie.dist.db;
 
 import org.w3c.dom.Element;
 
import com.razie.pub.assets.AssetMgr;
 import com.razie.pub.agent.Agent;
 import com.razie.pub.agent.AgentFileService;
 import com.razie.pub.agent.AgentHttpService;
 import com.razie.pub.agent.AgentService;
import com.razie.pub.assets.Meta;
 import com.razie.pub.base.AttrAccess;
 import com.razie.pub.base.data.RiXmlUtils;
 import com.razie.pub.base.log.Log;
 import com.razie.pub.comms.AgentHandle;
 import com.razie.pub.comms.Agents;
 import com.razie.pub.events.PostOffice;
 
 /**
  * TODO 1-1 docs
  * 
  * TODO 1-1 make this nostatic - right now uses a static singleton !!!
  * 
  * @author razvanc
  */
 public class AgentDbService extends AgentService {
     public static int            curVersion = 3;
     public static AgentDbService singleton  = null;
 
     public AgentDbService() {
         if (singleton != null) {
             throw new IllegalStateException("ERR_SVC_INIT AgentDbSrevice already initialized!");
         }
 
        AssetMgr.instance().register(new Meta(AgentDb.META, "", AgentDb.class.getName(), DbInventory.class
                .getName()));
         AgentHttpService.registerSoaAsset(AgentDb.class);
     }
 
     public static boolean upgradeDb(Element e) {
         if (e.getNodeName().equals("db")) {
             int ver = e.hasAttribute("dbver") ? Integer.parseInt(e.getAttribute("dbver")) : 0;
             if (ver < curVersion || ver < 2) {
 
                 switch (ver) {
                 case 0:
                 case 1:
                     upgrade0(e); // no break
                 case 2:
                     upgrade2(e); // no break
                 default:
                     return true;
                 }
             }
         }
         return false;
     }
 
     /** add dbver and tstamp on all diffs */
     public static void upgrade0(Element e) {
         long l = System.currentTimeMillis();
 
         e.setAttribute("dbver", "2");
         for (Element diff : RiXmlUtils.getNodeList(e, "settings/diffs/diff", null)) {
             diff.setAttribute("tstamp", String.valueOf(l++));
         }
         Log.logThis("AGENTDB_UPGRADEDDB 0->2 ");
     }
 
     /** add sync attr to root */
     public static void upgrade2(Element e) {
         long l = System.currentTimeMillis();
 
         e.setAttribute("dbver", "3");
         e.setAttribute("sync", "true");
         e.setAttribute("target", "all");
 
         Log.logThis("AGENTDB_UPGRADEDDB 2->3 ");
     }
 
     protected void onStartup() {
 // TODO do i need somehing here?       
     }
 
     protected void onStartupx() {
         // upgrade all dbs
         // TODO optimize this with sax
         AttrAccess aa = AgentDb.listLocalDb();
         for (String a : aa.getPopulatedAttr()) {
             AgentDb db = AgentDb.db(a);
             db.close();
         }
 
         // sync all dbs on startup
         // TODO optimize this - idiotic way to sync all DBs on startup...
         aa = AgentDb.listLocalDb();
         for (String a : aa.getPopulatedAttr()) {
             if (!a.startsWith("testdb")) {
                 Agent.instance().notifyOthers(AgentDb.EVT_UPDATEDB, "dbname", a);
             }
         }
         Log.logThis("AGENTDB_NOTIFYONSTARTUP");
 
     }
 
     protected void onConnectToOtherAgent(AgentHandle remote) {
         // TODO optimize this - idiotic way to sync all DBs on startup...
         Log.logThis("AGENTDB_NOTIFYCONNETEDHOST " + remote.hostname);
 
         AttrAccess aa = AgentDb.listLocalDb();
         for (String a : aa.getPopulatedAttr()) {
             if (!a.startsWith("testdb")) {
                 Agent.instance().notifyOther(remote, AgentDb.EVT_UPDATEDB, "dbname", a);
             }
         }
     }
 
     /** main method to be notified about an event */
     public void eatThis(String srcID, String eventId, AttrAccess info) {
         // we only care about external events really
         if (srcID == null || srcID.length() <= 0 || Agents.me().name.equals(srcID)) {
             return;
         }
 
         if (AgentDb.EVT_NEWDB.equals(eventId)) {
             PostOffice.shout(null, AgentDb.EVT_NEWDB, "dbname", info.getAttr("dbname"));
         } else if (AgentDb.EVT_UPDATEDB.equals(eventId)) {
             // when sync() from notification, will not notify others...
             // TODO optimize - add ver to attrs...
             String dbname = (String) info.getAttr("dbname");
             AgentDb.sync(dbname, Agents.agent(srcID), dbname, false);
             PostOffice.shout(null, AgentDb.EVT_UPDATEDB, "dbname", info.getAttr("dbname"));
         } else if (AgentDb.EVT_REMOVEDB.equals(eventId)) {
             // when sync() from notification, will not notify others...
             String dbname = (String) info.getAttr("dbname");
             AgentDb.db(dbname).delete(false);
             PostOffice.shout(null, AgentDb.EVT_REMOVEDB, "dbname", info.getAttr("dbname"));
         }
     }
 
     /** @return the list of event types you're interested in or null/empty if interested in all */
     public String[] interestedIn() {
         return events;
     }
 
     static String[] events       = { AgentDb.EVT_NEWDB, AgentDb.EVT_REMOVEDB, AgentDb.EVT_UPDATEDB };
 
     //TODO implement service dependencies...
     static Class<?>[]  dependencies = { AgentFileService.class };
 }
