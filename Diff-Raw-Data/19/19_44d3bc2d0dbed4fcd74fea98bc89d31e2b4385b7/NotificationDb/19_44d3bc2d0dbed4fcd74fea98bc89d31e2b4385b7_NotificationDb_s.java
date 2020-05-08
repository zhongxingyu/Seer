 package com.logisticsalliance.sn;
 
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.Time;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 
 import javax.mail.Session;
 
 import org.apache.log4j.Logger;
 
 import com.logisticsalliance.general.CommonConstants;
 import com.logisticsalliance.general.DsKey;
 import com.logisticsalliance.general.ScheduledWorker.EmailSent;
 import com.logisticsalliance.sqla.ConnectFactory;
 import com.logisticsalliance.sqla.ConnectFactory1;
 import com.logisticsalliance.sqla.SqlSupport;
 import com.logisticsalliance.util.SupportTime;
 
 /**
  * This class selects the deliveries that should be sent to stores by email.
  * 
  * @author Val Pashnev
  * @version %I%,%G%
  * @since 1.0
  */
 public class NotificationDb extends Notify1 {
 
 	private static Logger log = Logger.getLogger(NotificationDb.class);
 
 	private static String SQL_SEL_DELIVERIES =
 		"SELECT " +
		"sd.store_n, sd.cmdty, dc, sd.ship_date, sd.del_date, route_n, arrival_time, service_time," +
 		"add_key, order_n, pallets, del_time_from, del_time_to, province, del_carrier," +
 		"sd.first_user_file, sd.next_user_file, sd.n, rno.first_user_file, rno.next_user_file," +
 		"sts.first_user_file, sts.next_user_file, sts.ship_date " +
 		//",TIMESTAMP(sd.del_date,del_time_from) AS del_dateTime " +
 
 		"FROM " +
 		"la.hship_data sd LEFT JOIN la.hcarrier_schedule cs ON " +
 		"cs.store_n=sd.store_n AND cs.cmdty=sd.cmdty AND cs.del_day=DAYOFWEEK(sd.del_date)-1," +
 		"la.hrn_order rno,la.hstore_schedule sts,la.hstore_profile sp " +
 
 		"WHERE " +
 		"ship_n=sd.n AND sp.n=sd.store_n AND sd.unsent_note IS NOT NULL AND " +
 		"sts.store_n=sd.store_n AND sts.cmdty=sd.cmdty AND " +
 		"(sts.ship_date IS NOT NULL AND sts.ship_date=sd.ship_date OR " +
 		"sts.ship_date IS NULL AND sts.ship_day=DAYOFWEEK(sd.ship_date)-1) AND " +
 		"TIMESTAMP(sd.del_date,del_time_from)>=? AND TIMESTAMP(sd.del_date,del_time_from)<? AND " +
 		"rno.lw NOT IN (" +CommonConstants.RX_LW+") " +
 
 		"ORDER BY " +
 		"sd.store_n,sts.del_time_from,add_key,sd.cmdty";
 
 	private final static String
 		SQL_SEL_ENVR = "SELECT time_store_notified FROM la.henvr",
 		SQL_INS_ENVR = "INSERT INTO la.henvr (time_store_notified) VALUES (?)",
 		SQL_UPD_ENVR = "UPDATE la.henvr SET time_store_notified=?",
 		SQL_UPD_UNSENT = "UPDATE la.hship_data SET unsent_note=NULL WHERE n=?",
 				
 		CCS = "Canada Cartage Systems", TBD ="TBD";
 
 	private final static DeliveryNote.Cmdty DCV_CMDTY =
 		new DeliveryNote.Cmdty(CommonConstants.DCV, null, null, null);
 
 	private static Timestamp nextTime = new Timestamp(0);
 	private static HashSet<DsKey> carriersNotFound = new HashSet<DsKey>();
 
 	public static void clearCarriersNotFound() {
 		carriersNotFound.clear();
 	}
 	public static void process(String notifyStartingTime, String notifyEndingTime,
 		long timeAhead, EmailSent es, HashSet<Integer> storeSubset,
 		boolean onlyTestStoresToRpt, boolean sendDelayedNotesOff) throws Exception {
 		long timeAheadInMins = timeAhead/60000;
 		Timestamp t0 = null, t1 = null;
 		if (notifyStartingTime != null) {
 			t0 = SupportTime.parseDdMMyyyyHHmm(notifyStartingTime);
 		}
 		if (notifyEndingTime != null) {
 			t1 = SupportTime.parseDdMMyyyyHHmm(notifyEndingTime);
 		}
 		Session s = null;
 		Connection con = null, con1 = null;
 		try {
 			con = ConnectFactory1.one().getConnection();
 			con1 = ConnectFactory1.one().getConnection();
 			PreparedStatement selDelSt = con.prepareStatement(SQL_SEL_DELIVERIES),
 				updUnsent = con.prepareStatement(SQL_UPD_UNSENT);
 			if (t1 == null) {
 				t1 = SqlSupport.getDb2CurrentTime(con1);
 			}
 			while (true) {
 				t0 = getNextTime(con1, SQL_SEL_ENVR, SQL_INS_ENVR,
 					t0, t1, nextTime, 1800000, timeAheadInMins, log);
 				if (t0 == null) {
 					break;
 				}
 				// Select deliveries
 				Timestamp t2 = new Timestamp(t0.getTime()-1800000);// less 30 minutes
 				s = select(selDelSt, updUnsent, t2, t0, s, es,
 					storeSubset, onlyTestStoresToRpt, false);
 				if (!sendDelayedNotesOff) {
 					s = select(selDelSt, updUnsent,
 						new Timestamp(t2.getTime()-SupportTime.DAY),// less 24 hours
 						t2, s, es, storeSubset, onlyTestStoresToRpt, true);
 				}
 				if (notifyEndingTime == null) {
 					updateNotifyEndingTime(con1, SQL_UPD_ENVR, t0);
 				}
 			}
 			selDelSt.close();
 		}
 		catch (Exception ex) {
 			ex.printStackTrace();
 			log.error(ex);
 		}
 		finally {
 			ConnectFactory.close(con);
 			ConnectFactory.close(con1);
 		}
 	}
 	private static DeliveryNote getNote(int storeN, String addKey, Time delTimeFrom) {
 		DeliveryNote dn = new DeliveryNote();
 		dn.storeN = storeN;
 		dn.addKey = addKey;
 		dn.delTimeFrom = delTimeFrom;
 		return dn;
 	}
 	private static Session select(PreparedStatement st, PreparedStatement updUnsent,
 		Timestamp t0, Timestamp t, Session s, EmailSent es, HashSet<Integer> storeSubset,
 		boolean onlyTestStoresToRpt, boolean delay) throws Exception {
 		st.setTimestamp(1, t0);
 		st.setTimestamp(2, t);
 		HashMap<String,DeliveryItem> m = new HashMap<String,DeliveryItem>(64, .5f);
 		ResultSet rs = st.executeQuery();
 		if (!rs.next()) {
 			rs.close(); return s;
 		}
 		int storeN = 0, i = 0;
 		DeliveryNote dn = null;
 		DeliveryItem di = null;
 		ArrayList<DeliveryNote> al = new ArrayList<DeliveryNote>(128);
 		while (true) {
 			storeN = rs.getInt(1);
 			if (onlyTestStoresToRpt && !storeSubset.contains(storeN)) {
 				if (!rs.next()) {
 					rs.close();
 					if (dn != null) { add(al, dn);}
 					break;
 				}
 				continue;
 			}
 			String addKey = rs.getString(9).trim();
 			Time delTimeFrom = rs.getTime(12);
 			if (dn == null) {
 				dn = getNote(storeN, addKey, delTimeFrom);
 			}
 			else if (storeN != dn.storeN || !delTimeFrom.equals(dn.delTimeFrom) ||
 				!addKey.equals(dn.addKey)) {
 				add(al, dn);
 				m.clear();
 				dn = getNote(storeN, addKey, delTimeFrom);
 				di = null;
 			}
 			Date dsShipDate = rs.getDate(23);
 			Time arrivalTime = rs.getTime(7);
 			Time serviceTime = rs.getTime(8);
 			String cmdty = rs.getString(2).toUpperCase();
 			if (di == null || !cmdty.equals(di.cmdty)) {
 				dn.cmdtyList.add(new DeliveryNote.Cmdty(cmdty,
 					dsShipDate, arrivalTime, serviceTime));
 			}
 			di = new DeliveryItem();
 			di.cmdty = cmdty;
 			di.orderN = rs.getString(10);
 			di.dsShipDate = dsShipDate;
 			dn.ns.add(rs.getLong(18));
 
 			if (addItem(m, dn, di)) {
 				if (dn.shipDate == null) {
 					dn.id = "m"+(++i);
 					dn.delay= delay;
 					dn.shipDate = rs.getDate(4);
 					dn.delDate = rs.getDate(5);
 					dn.routeN = rs.getString(6);
 					dn.arrivalTime = arrivalTime;
 					dn.serviceTime = serviceTime;
 					dn.delTimeTo = rs.getTime(13);
 					dn.province = rs.getString(14);
 					dn.firstUserFile = rs.getString(16);
 					String nuf = rs.getString(17);
 					if (!dn.firstUserFile.equals(nuf)) { dn.nextUserFile = nuf;}
 				}
 				if (dn.delCarrier == null) {
 					dn.delCarrier = rs.getString(15);
 				}
 				di.pallets = rs.getDouble(11);
 				di.firstUserFile = rs.getString(19);
 				String nuf = rs.getString(20);
 				if (!di.firstUserFile.equals(nuf)) { di.nextUserFile = nuf;}
 				di.dsFirstUserFile = rs.getString(21);
 				nuf = rs.getString(22);
 				if (!di.dsFirstUserFile.equals(nuf)) { di.dsNextUserFile = nuf;}
 			}
 			if (!rs.next()) {
 				rs.close();
 				add(al, dn);
 				break;
 			}
 		}
 		if (al.size() != 0) {
 			String interval = SupportTime.dd_MM_yyyy_HH_mm_Format.format(t0)+" - "+
 				SupportTime.dd_MM_yyyy_HH_mm_Format.format(t);
 			log.debug("\r\n\r\nNOTIFICATIONS for "+interval+"\r\n\r\n"+al);
 			if (es.emailUnsent == null) {
 				Thread.sleep(2000);
 				s = NotificationMail.send(s, es, storeSubset, al, interval);
 			}
 			update(updUnsent, al);
 		}
 		return s;
 	}
 
 	private static boolean addItem(HashMap<String,DeliveryItem> m, DeliveryNote dn, DeliveryItem di) {
 		DeliveryItem di2 = m.get(di.orderN);
 		if (di2 != null) {
 			if (di.dsShipDate == null && di2.dsShipDate != null) {
 				return false;
 			}
 			else if (di2.dsShipDate == null && di.dsShipDate != null) {
 				dn.items.remove(di2);
 			}
 			else {
 				log.error("Duplicate order: "+di.orderN+", store "+dn.storeN);
 				return false;
 			}
 		}
 		m.put(di.orderN, di);
 		dn.items.add(di);
 		return true;
 	}
 	private static void add(ArrayList<DeliveryNote> al, DeliveryNote dn) {
 		if (dn.delCarrier == null) {
 			int dow = SupportTime.getDayOfWeek(dn.delDate);
 			for (Iterator<DeliveryNote.Cmdty> it = dn.cmdtyList.iterator(); it.hasNext();) {
 				DeliveryNote.Cmdty c = it.next();
 				DsKey k = new DsKey(dn.storeN, c.cmdty, dow);
 				if (!c.cmdty.equals(CommonConstants.DCX) && carriersNotFound.add(k)) {
 					String type = c.dsShipDate == null ? "regular" : "holidays";
 					log.error("Carrier not found (" + type + "): "+k);
 				}
 			}
 		}
 		dn.totalPallets = dn.getTotalPallets(null);
 		if (dn.totalPallets > 30) {
 			return;
 		}
 		if (dn.delCarrier == null) {
 			dn.delCarrier = TBD;
 		}
 		else { dn.delCarrier = dn.delCarrier.trim();}
 
 		if (dn.cmdtyList.size() == 1 && CommonConstants.DCF.equals(dn.cmdtyList.get(0).cmdty)) {
 			dn.arrivalTime = null;
 			dn.serviceTime = null;
 		}
 		else if (CCS.equalsIgnoreCase(dn.delCarrier)) {
 			int i = dn.cmdtyList.indexOf(DCV_CMDTY);
 			if (i == -1) {
 				dn.arrivalTime = new Time(dn.delTimeFrom.getTime()+SupportTime.HOUR);
 				dn.serviceTime = null;
 			}
 			else {
 				DeliveryNote.Cmdty c = dn.cmdtyList.get(i);
 				dn.arrivalTime = c.arrivalTime;
 				dn.serviceTime = c.serviceTime;
 			}
 		}
 		else {
 			dn.arrivalTime = new Time(dn.delTimeFrom.getTime()+SupportTime.HOUR);
 			dn.serviceTime = null;
 		}
 		al.add(dn);
 	}
 	private static void update(PreparedStatement updUnsent,
 		ArrayList<DeliveryNote> al) throws Exception {
 		for (Iterator<DeliveryNote> it = al.iterator(); it.hasNext();) {
 			DeliveryNote dn = it.next();
 			for (Iterator<Long> it2 = dn.ns.iterator(); it2.hasNext();) {
 				updUnsent.setLong(1, it2.next());
 				updUnsent.addBatch();
 			}
 		}
 		updUnsent.executeBatch();
 		updUnsent.getConnection().commit();
 	}
 
 }
