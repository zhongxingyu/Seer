 /**
  * JDBCNetworkUnifier.java
  * 
  * allows access to all of the underlying networkDb interfaces from a single
  * class
  */
 package edu.sc.seis.sod.database.network;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import edu.iris.Fissures.Time;
 import edu.iris.Fissures.TimeRange;
 import edu.iris.Fissures.IfNetwork.Channel;
 import edu.iris.Fissures.IfNetwork.NetworkAccess;
 import edu.iris.Fissures.IfNetwork.NetworkAttr;
 import edu.iris.Fissures.IfNetwork.NetworkId;
 import edu.iris.Fissures.IfNetwork.NetworkNotFound;
 import edu.iris.Fissures.IfNetwork.Site;
 import edu.iris.Fissures.IfNetwork.Station;
 import edu.iris.Fissures.model.MicroSecondDate;
 import edu.iris.Fissures.network.NetworkIdUtil;
 import edu.sc.seis.fissuresUtil.cache.ProxyNetworkAccess;
 import edu.sc.seis.fissuresUtil.cache.ProxyNetworkDC;
 import edu.sc.seis.fissuresUtil.cache.VestingNetworkDC;
 import edu.sc.seis.fissuresUtil.database.ConnMgr;
 import edu.sc.seis.fissuresUtil.database.NotFound;
 import edu.sc.seis.fissuresUtil.database.network.JDBCChannel;
 import edu.sc.seis.fissuresUtil.database.network.JDBCNetwork;
 import edu.sc.seis.fissuresUtil.database.network.JDBCSite;
 import edu.sc.seis.fissuresUtil.database.network.JDBCStation;
 import edu.sc.seis.sod.database.ChannelDbObject;
 import edu.sc.seis.sod.database.NetworkDbObject;
 import edu.sc.seis.sod.database.SiteDbObject;
 import edu.sc.seis.sod.subsetter.network.NetworkEffectiveTimeOverlap;
 
 public class JDBCNetworkUnifier {
 
     public JDBCNetworkUnifier() throws SQLException {
         this(ConnMgr.createConnection());
     }
 
     public JDBCNetworkUnifier(Connection conn) throws SQLException {
         this.chanDb = new JDBCChannel(conn);
         this.netDb = chanDb.getNetworkTable();
         this.stationDb = chanDb.getStationTable();
         this.siteDb = chanDb.getSiteTable();
     }
 
     public ChannelDbObject getChannel(int chanDbId) throws NotFound,
             SQLException {
         return new ChannelDbObject(chanDbId, chanDb.get(chanDbId));
     }
 
     public int put(Channel chan) throws SQLException {
         return chanDb.put(chan);
     }
 
     public SiteDbObject getSite(int siteDbId) throws NotFound, SQLException {
         return new SiteDbObject(siteDbId, siteDb.get(siteDbId));
     }
 
     public int put(Site site) throws SQLException {
         return siteDb.put(site);
     }
 
     public Station getStation(int stationDbId) throws NotFound, SQLException {
         return stationDb.get(stationDbId);
     }
 
     public int put(Station station) throws SQLException {
         return stationDb.put(station);
     }
 
     public NetworkAttr getNet(int netDbId) throws NotFound, SQLException {
         return netDb.get(netDbId);
     }
 
     public NetworkDbObject getNet(int netDbId, ProxyNetworkDC ndc)
             throws NetworkNotFound, NotFound, SQLException {
         NetworkId id = netDb.get(netDbId).get_id();
         ProxyNetworkAccess na;
         synchronized(ndc) {
             try {
            	return new NetworkDbObject(netDbId, (ProxyNetworkAccess)ndc.a_finder().retrieve_by_id(id));
             } catch(NetworkNotFound e) {
                 // didn't get by id, try by code
                 logger.debug("Caught exception with retrieve_by_id  for "
                         + NetworkIdUtil.toString(id) + ", trying by code", e);
                 NetworkAccess[] netArray = ndc.a_finder()
                         .retrieve_by_code(id.network_code);
                if (netArray.length == 0) {
                	throw new NetworkNotFound("Cant retrieve_by_code for "
                            + NetworkIdUtil.toString(id) + " received "
                            + netArray.length + " nets.");
                }
                na = (ProxyNetworkAccess)netArray[0];
                 if(NetworkIdUtil.isTemporary(id)) {
                     // temp net codes are never reused in the same year, so
                     // find a network that overlaps the year we have in the id
                     // and hope it is right
                     // first make sure format is ok, just to make Charlie happy
                     MicroSecondDate formatCheck = new MicroSecondDate(id.begin_time);
                     String netYear = id.begin_time.date_time.substring(0, 4);
                     TimeRange year = new TimeRange(new Time(netYear
                             + "0101T00:00:01.000Z", -1), new Time(netYear
                             + "1231T23:59:59.000Z", -1));
                     NetworkEffectiveTimeOverlap yearOverlap = new NetworkEffectiveTimeOverlap(year);
                     NetworkAccess found = null;
                     for(int i = 0; i < netArray.length; i++) {
                         if(yearOverlap.overlaps(netArray[i].get_attributes().effective_time)) {
                             if(found != null) {
                                 // two overlap, just give up?
                                 throw new NetworkNotFound("No network matches id="
                                         + NetworkIdUtil.toString(id)
                                         + " and more than one overlaps year="
                                         + netYear);
                             }
                             found = netArray[i];
                         }
                     }
                    if (found != null) {
                    	// found exactly one match so return it
                    	return new NetworkDbObject(netDbId,  (ProxyNetworkAccess)found);
                    } else {
                    	throw new NetworkNotFound("Cant retrieve_by_code for "
                                + NetworkIdUtil.toString(id) + " no  year overlaps in "
                                + netArray.length + " nets.");
                    }
                 } else if(netArray.length != 1) {
                     throw new NetworkNotFound("Cant retrieve_by_code for "
                             + NetworkIdUtil.toString(id) + " received "
                             + netArray.length + " nets.");
                } else {
                	return new NetworkDbObject(netDbId, na);
                 }
             }
         }
     }
 
     public int put(NetworkAttr net) throws SQLException {
         return netDb.put(net);
     }
 
     public NetworkDbObject[] getAllNets(ProxyNetworkDC ndc)
             throws SQLException, NotFound, NetworkNotFound {
         int[] netIds = netDb.getAllNetworkDBIds();
         NetworkDbObject[] out = new NetworkDbObject[netIds.length];
         for(int i = 0; i < netIds.length; i++) {
             out[i] = getNet(netIds[i], ndc);
         }
         return out;
     }
 
     public JDBCNetwork getNetworkDb() {
         return netDb;
     }
 
     public JDBCChannel getChannelDb() {
         return chanDb;
     }
 
     public JDBCSite getSiteDb() {
         return siteDb;
     }
 
     public JDBCStation getStationDb() {
         return stationDb;
     }
 
     private JDBCNetwork netDb;
 
     private JDBCChannel chanDb;
 
     private JDBCSite siteDb;
 
     private JDBCStation stationDb;
     
     private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger
 			.getLogger(JDBCNetworkUnifier.class);
 }
