 package com.logisticsalliance.tt;
 
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.Time;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 
 import org.apache.log4j.Logger;
 
 import com.logisticsalliance.general.CommonConstants;
 import com.logisticsalliance.general.DsKey;
 import com.logisticsalliance.general.EMailEmergency;
 import com.logisticsalliance.general.ScheduledWorker.EmailSent;
 import com.logisticsalliance.shp.OrderItem;
 import com.logisticsalliance.sqla.ConnectFactory;
 import com.logisticsalliance.sqla.ConnectFactory1;
 import com.logisticsalliance.util.SupportTime;
 
 /**
  * This class populates the deliveries in the table of track and trace project.
  * 
  * @author Val Pashnev
  * @version %I%,%G%
  * @since 1.0
  */
 public class TtTableDb {
 
 	private static Logger log = Logger.getLogger(TtTableDb.class);
 
 	private static final String
 		SQL_INS =
 		"INSERT INTO OS61LXDTA.OSPDLVS (dvsts,dvstsd,dvlsts,dvlstsd,dvstore#,dvcom," +
 		"dvdc,dvshpd,dvdlvd,dvdlvt,dvsrvtime,dvroute,dvstop#,dvpallets,dvetato,dvetatc," +
 		"dvcar) VALUES ('10','PLAN','10','PLAN',?,?,?,?,?,?,?,?,?,?,?,?,?)",
 
 		SQL_UPD =
 		"UPDATE OS61LXDTA.OSPDLVS SET dvdlvd=?,dvdlvt=?,dvsrvtime=?,dvroute=?,dvstop#=?," +
 		"dvpallets=?,dvetato=?,dvetatc=?,dvcar=? WHERE dvstore#=? AND dvcom=? AND " +
 		"dvdc=? AND dvshpd=?",
 
 		SQL_SEL_DELIVERIES =
 		"SELECT DISTINCT " +
		"sd.store_n, sd.cmdty, sd.del_date, route_n, stop_n, dc, arrival_time, service_time," +
 		"order_n, pallets, del_time_from, del_time_to, del_carrier_id, sd.first_user_file," +
 		"sd.next_user_file, rno.first_user_file, rno.next_user_file, sts.first_user_file," +
 		"sts.next_user_file, sts.ship_date " +
 
 		"FROM " +
 		"la.hship_data sd LEFT JOIN la.hcarrier_schedule1 cs ON " +
 		"cs.store_n=sd.store_n AND ((sd.cmdty='DCX' OR sd.cmdty='EVT' OR sd.cmdty='EVT2') AND " +
 		"(cs.cmdty='DCB' OR cs.cmdty='DCV') OR sd.cmdty<>'DCX' AND sd.cmdty<>'EVT' AND " +
 		"sd.cmdty<>'EVT2' AND cs.cmdty=sd.cmdty) AND cs.del_day=DAYOFWEEK(sd.del_date)-1," +
 		"la.hrn_order rno,la.hstore_schedule sts " +
 
 		"WHERE " +
 		"ship_n=sd.n AND " +
 		"sts.store_n=sd.store_n AND sts.cmdty=sd.cmdty AND " +
 		"(sts.ship_date IS NOT NULL AND sts.ship_date=sd.ship_date OR " +
 		"sts.ship_date IS NULL AND sts.ship_day=DAYOFWEEK(sd.ship_date)-1) AND " +
 		"sd.ship_date=? AND rno.lw NOT IN (" +CommonConstants.RX_LW+") "+
 
 		"ORDER BY " +
		"sd.store_n,sd.cmdty,dc";
 
 	private static ConnectFactory connectFactoryI5;
 
 	private static HashSet<DsKey> carriersNotFound = new HashSet<DsKey>();
 
 	private static int trials = 0;
 
 	public static void setConnectFactoryI5(ConnectFactory cf) {
 		connectFactoryI5 = cf;
 	}
 	public static void clearCarriersNotFound() {
 		carriersNotFound.clear();
 	}
 	public static int getTrials() {
 		return trials;
 	}
 	public static void process(final Date shipDate, EmailSent es) throws InterruptedException {
 		trials++;
 		Thread t = new Thread() {
 			@Override
 			public void run() {
 				process1(shipDate);
 				trials = 0;
 			}
 		};
 		t.setDaemon(true);
 		t.start();
 		int i = 0;
 		while (trials > 0 && i++ != 360) {
 			Thread.sleep(5000);
 		}
 		if (trials > 0) {
 			log.error("Report incomplete truck and trace table");
 			if (trials > 2) {
 				EMailEmergency.send(es, trials+" trials to populate truck and trace table failed");
 			}
 		}
 	}
 	private static void process1(Date shipDate) {
 		Connection con = null, con1 = null;
 		try {
 			con = ConnectFactory1.one().getConnection();
 			con1 = connectFactoryI5.getConnection();
 			con1.setAutoCommit(true);
 			PreparedStatement st = con.prepareStatement(SQL_SEL_DELIVERIES);
 			st.setDate(1, shipDate);
 			ResultSet rs = st.executeQuery();
 			ArrayList<DeliveryRow> al = new ArrayList<DeliveryRow>(1024);
 			int count = select(rs, al);
 			st.close();
 			/*st = con1.prepareStatement("DELETE FROM OS61LYDTA.OSPDLVS WHERE dvshpd=?");
 			st.setDate(1, shipDate);
 			int n = st.executeUpdate();
 			st.close();*/
 			update(con1.prepareStatement(SQL_INS), con1.prepareStatement(SQL_UPD), al, shipDate);
 			con1.commit();
 			if (al.size() != 0) {
 				log.debug("\r\n\r\nSHIPMENTS: "+SupportTime.dd_MM_yyyy_Format.format(shipDate)+
 					"\r\n\r\n"+al+
 					"\r\n\r\nTotal:   "+al.size()+
 					"\r\n\r\nMissing: "+(al.size()-count));
 			}
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
 	private static DeliveryRow newData(int storeN, String cmdty, String dc) {
 		DeliveryRow r = new DeliveryRow();
 		r.storeN = storeN;
 		r.cmdty = cmdty;
 		r.dc = dc;
 		return r;
 	}
 	private static int select(ResultSet rs, ArrayList<DeliveryRow> al) throws Exception {
 		HashMap<String,OrderItem> m = new HashMap<String,OrderItem>(64, .5f);
 		if (!rs.next()) {
 			rs.close(); return 0;
 		}
 		int storeN;
 		String cmdty, dc;
 		int[] count = {0};
 		DeliveryRow r = null;
 		while (true) {
 			storeN = rs.getInt(1);
 			cmdty = rs.getString(2);
 			dc = rs.getString(6);
 			if (r == null) {
 				r = newData(storeN, cmdty, dc);
 			}
 			else if (storeN != r.storeN || !cmdty.equals(r.cmdty) || !dc.equals(r.dc)) {
 				addRow(al, r, count);
 				m.clear();
 				r = newData(storeN, cmdty, dc);
 			}
 			OrderItem oi = new OrderItem();
 			oi.orderN = rs.getString(9);
 			oi.dsShipDate = rs.getDate(20);
 			if (addItem(m, r, oi)) {
 				if (r.delDate == null) {
 					r.delDate = rs.getDate(3);
 					r.routeN = rs.getString(4);
 					r.stopN = rs.getString(5);
 					r.arrivalTime = rs.getTime(7);
 					r.serviceTime = rs.getTime(8);
 					r.delTimeFrom = rs.getTime(11);
 					r.delTimeTo = rs.getTime(12);
 					r.delCarrier = rs.getString(13);
 					r.firstUserFile = rs.getString(14);
 					String nuf = rs.getString(15);
 					if (!r.firstUserFile.equals(nuf)) { r.nextUserFile = nuf;}
 				}
 				oi.lw = oi.orderN.substring(2, 4);
 				oi.pallets = rs.getDouble(10);
 				oi.firstUserFile = rs.getString(16);
 				String nuf = rs.getString(17);
 				if (!oi.firstUserFile.equals(nuf)) { oi.nextUserFile = nuf;}
 				oi.dsFirstUserFile = rs.getString(18);
 				nuf = rs.getString(19);
 				if (!oi.dsFirstUserFile.equals(nuf)) { oi.dsNextUserFile = nuf;}
 			}
 			if (!rs.next()) {
 				rs.close();
 				addRow(al, r, count);
 				break;
 			}
 		}
 		return count[0];
 	}
 	private static void addRow(ArrayList<DeliveryRow> al, DeliveryRow r, int[] count) {
 		r.pallets = r.getTotalPallets();
 		if (r.delCarrier == null) {
 			int dow = SupportTime.getDayOfWeek(r.delDate);
 			DsKey k = new DsKey(r.storeN, r.cmdty, dow);
 			if (carriersNotFound.add(k)) {
 				String type = r.items.get(0).dsShipDate == null ? "regular" : "holidays";
 				log.error("Carrier not found (" + type + "): "+k);
 			}
 			r.missing = true;
 		}
 		else { count[0]++;}
 		if (!CommonConstants.CCS.equalsIgnoreCase(r.delCarrier)) {
 			r.arrivalTime = new Time(r.delTimeFrom.getTime()+SupportTime.HOUR);
 		}
 		al.add(r);
 	}
 	private static boolean addItem(HashMap<String,OrderItem> m, DeliveryRow r, OrderItem oi) {
 		OrderItem oi2 = m.get(oi.orderN);
 		if (oi2 != null) {
 			if (oi.dsShipDate == oi2.dsShipDate || oi.dsShipDate != null &&
 				oi2.dsShipDate != null || oi2.dsShipDate != null) {
 				return false;
 			}
 			else if (oi.dsShipDate != null) {
 				r.items.remove(oi2);
 			}
 		}
 		m.put(oi.orderN, oi);
 		r.items.add(oi);
 		return true;
 	}
 	private static void update(PreparedStatement ins, PreparedStatement upd,
 		ArrayList<DeliveryRow> al, Date shipDate) throws Exception {
 		for (Iterator<DeliveryRow> it = al.iterator(); it.hasNext();) {
 			DeliveryRow r = it.next();
 			if (r.missing) { continue;}
 			upd.setDate(1, r.delDate);
 			upd.setInt(2, getTime(r.arrivalTime));
 			upd.setInt(3, getTime(r.serviceTime));
 			upd.setString(4, r.routeN);
 			upd.setString(5, r.stopN);
 			upd.setInt(6, r.pallets);
 			upd.setInt(7, getTime(r.delTimeFrom));
 			upd.setInt(8, getTime(r.delTimeTo));
 			if (r.delCarrier != null && r.delCarrier.length() > 8) {
 				r.delCarrier = r.delCarrier.substring(0, 8);
 			}
 			upd.setString(9, r.delCarrier);
 			upd.setInt(10, r.storeN);
 			upd.setString(11, r.cmdty);
 			upd.setString(12, r.dc);
 			upd.setDate(13, shipDate);
 			if (upd.executeUpdate() != 0) {
 				continue;
 			}
 			ins.setInt(1, r.storeN);
 			ins.setString(2, r.cmdty);
 			ins.setString(3, r.dc);
 			ins.setDate(4, shipDate);
 			ins.setDate(5, r.delDate);
 			ins.setInt(6, getTime(r.arrivalTime));
 			ins.setInt(7, getTime(r.serviceTime));
 			ins.setString(8, r.routeN);
 			ins.setString(9, r.stopN);
 			ins.setInt(10, r.pallets);
 			ins.setInt(11, getTime(r.delTimeFrom));
 			ins.setInt(12, getTime(r.delTimeTo));
 			ins.setString(13, r.delCarrier);
 			ins.addBatch();
 		}
 		ins.executeBatch();
 	}
 	private static int getTime(Time t) {
 		String v = SupportTime.Hmm_Format.format(t);
 		return Integer.parseInt(v);
 	}
 }
