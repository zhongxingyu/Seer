 package com.logisticsalliance.shp;
 
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.Time;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 
 import org.apache.commons.net.ftp.FTPClient;
 import org.apache.log4j.Logger;
 
 import com.logisticsalliance.general.CommonConstants;
 import com.logisticsalliance.general.DsKey;
 import com.logisticsalliance.general.EMailEmergency;
 import com.logisticsalliance.general.ScheduledWorker;
 import com.logisticsalliance.general.ScheduledWorker.EmailSent;
 import com.logisticsalliance.sqla.ConnectFactory;
 import com.logisticsalliance.sqla.ConnectFactory1;
 import com.logisticsalliance.text.TBuilder;
 import com.logisticsalliance.util.SupportTime;
 
 /**
  * This class selects shipments for daily reports.
  * 
  * @author Val Pashnev
  * @version %I%,%G%
  * @since 1.0
  */
 public class ShipmentDb {
 
 	private static Logger log = Logger.getLogger(ShipmentDb.class);
 
 	private static final String
 		SQL_INS1 =
 		"INSERT INTO OS61LYDTA.OSPIFC1 (I1RQS,I1STS,I1ISRC,I1DIV,I1WHS,I1SRV,I1CUS,I1PSTS,I1TPGM," +
 		"I1WGT,I1PCS,I1VOL,I1HGT,I1SKD,I1FPKD,I1FDLD,I1CON,I1LOD,I1SHP$,I1ORD$,I1OTYP," +
 		"I1PSTP,I1DSTP,I1SHPI,I1CAR,I1SRVP,I1URGN,I1ORD#) " +
 		"VALUES ('*CREATE','91','02','LA','STR','*ANY','SHOPPERS','0','OSR1200',?,?,?,?,?,?," +
 		"?,?,?,?,?,?,?,?,?,?,?,?,?)",
 
 		SQL_UPD1 =
 		"UPDATE OS61LYDTA.OSPIFC1 SET " +
 		"I1RQS='*CREATE'," +
 		"I1STS='91'," +
 		"I1ISRC='02'," +
 		"I1DIV='LA'," +
 		"I1WHS='STR'," +
 		"I1SRV='*ANY'," +
 		"I1CUS='SHOPPERS'," +
 		"I1PSTS='0'," +
 		"I1TPGM='OSR1200'," +
 		"I1WGT=?," +
 		"I1PCS=?," +
 		"I1VOL=?," +
 		"I1HGT=?," +
 		"I1SKD=?," +
 		"I1FPKD=?," +
 		"I1FDLD=?," +
 		"I1CON=?," +
 		"I1LOD=?," +
 		"I1SHP$=?," +
 		"I1ORD$=?," +
 		"I1OTYP=?," +
 		"I1PSTP=?," +
 		"I1DSTP=?," +
 		"I1SHPI=?," +
 		"I1CAR=?," +
 		"I1SRVP=?," +
 		"I1URGN=? " +
 		"WHERE I1ORD#=?",
 
 		SQL_DEL1 =
 		"DELETE FROM OS61LYDTA.OSPIFC1 WHERE I1DIV='LA' AND I1CUS='SHOPPERS' AND I1FPKD=?",
 
 		SQL_INS2 =
 		"INSERT INTO OS61LYDTA.OSPIFC2 (I2CUS,I2CAR,I2SRV,I2HUB2,I2PIKD,I2DRPD,I2CCF,I2SCF," +
 		"I2PSTP,I2DSTP,I2SHPGRP,I2ORD#,I2SEQN) " +
 		"VALUES ('SHOPPERS',?,?,?,?,?,?,?,?,?,?,?,?)",
 
 		SQL_UPD2 =
 		"UPDATE OS61LYDTA.OSPIFC2 SET " +
 		"I2CUS='SHOPPERS'," +
 		"I2CAR=?," +
 		"I2SRV=?," +
 		"I2HUB2=?," +
 		"I2PIKD=?," +
 		"I2DRPD=?," +
 		"I2CCF=?," +
 		"I2SCF=?," +
 		"I2PSTP=?," +
 		"I2DSTP=?," +
 		"I2SHPGRP=?" +
 		"WHERE I2ORD#=? AND I2SEQN=?",
 
 		SQL_DEL2 =
 		"DELETE FROM OS61LYDTA.OSPIFC2 WHERE I2ORD# IN " +
 		"(SELECT I1ORD# FROM OS61LYDTA.OSPIFC1 WHERE I1DIV='LA' AND I1CUS='SHOPPERS' AND I1FPKD=?)",
 
 		SQL_INS9 =
 		"INSERT INTO OS61LYDTA.OSPIFC9 (I9CUS,I9ENT,I9ENTQ,I9REF#,I9ORD#,I9LEG#,I9QUAL) " +
 		"VALUES ('SHOPPERS','SHOPPERS','5',?,?,?,?)",
 
 		SQL_UPD9 =
 		"UPDATE OS61LYDTA.OSPIFC9 SET " +
 		"I9CUS='SHOPPERS'," +
 		"I9ENT='SHOPPERS'," +
 		"I9ENTQ='5'," +
 		"I9REF#=? " +
 		"WHERE I9ORD#=? AND I9LEG#=? AND I9QUAL=?",
 
 		SQL_DEL9 =
 		"DELETE FROM OS61LYDTA.OSPIFC9 WHERE I9ORD# IN " +
 		"(SELECT I1ORD# FROM OS61LYDTA.OSPIFC1 WHERE I1DIV='LA' AND I1CUS='SHOPPERS' AND I1FPKD=?)",
 
 		MATRIXDC = "MATRIXDC";
 
 	private static String
 		SQL_SEL_EQUIP_SIZE =
 		"SELECT min(equip_size),dc,route_n " +
 		"FROM la.hship_data " +
 		"WHERE ship_date=? " +
 		"GROUP BY dc,route_n " +
 		"ORDER BY dc,route_n",
 
 		SQL_SEL_SHP =
 		"SELECT " +
		"sd.store_n, sd.cmdty, sd.del_date, route_n, stop_n, sd.dc," +
 		"dc_depart_time, prev_distance, prev_travel_time, arrival_time, service_time," +
 		"total_service_time, total_travel_time," +
 		"order_n, pallets, units, weight, cube, del_time_from, del_time_to," +
 		"spec_instructs, lh_carrier_id, lh_service, del_carrier_id, del_service," +
 		"sd.first_user_file, sd.next_user_file, rno.first_user_file, rno.next_user_file," +
 		"sts.first_user_file, sts.next_user_file, sts.ship_date " +
 
 		"FROM " +
 		"la.hship_data sd LEFT JOIN la.hcarrier_schedule1 cs ON " +
 		"cs.store_n=sd.store_n AND ((sd.cmdty='DCX' OR sd.cmdty='EVT' OR sd.cmdty='EVT2') AND " +
 		"(cs.cmdty='DCB' OR cs.cmdty='DCV') OR sd.cmdty<>'DCX' AND sd.cmdty<>'EVT' AND " +
 		"sd.cmdty<>'EVT2' AND cs.cmdty=sd.cmdty) AND cs.del_day=DAYOFWEEK(sd.del_date)-1," +
 		"la.hrn_order rno,la.hstore_schedule sts,la.hstore_profile sp " +
 
 		"WHERE " +
 		"ship_n=sd.n AND sp.n=sd.store_n AND " +
 		"sts.store_n=sd.store_n AND sts.cmdty=sd.cmdty AND " +
 		"(sts.ship_date IS NOT NULL AND sts.ship_date=sd.ship_date OR " +
 		"sts.ship_date IS NULL AND sts.ship_day=DAYOFWEEK(sd.ship_date)-1) AND " +
 		"sd.ship_date=? AND rno.lw NOT IN (" +CommonConstants.RX_LW+") " +
 		(ScheduledWorker.shipQryCarriers == null ? "" : "AND cs.del_carrier_id IN (" +
 		ScheduledWorker.shipQryCarriers+") ") +
 
 		"ORDER BY " +
 		"1,6,4,2";
 
 	private static ConnectFactory connectFactoryI5;
 
 	private static HashSet<DsKey> carriersNotFound = new HashSet<DsKey>();
 
 	private static int trials = 0;
 
 	public static void setConnectFactoryI5(ConnectFactory cf) {
 		ConnectFactory cf1 = new ConnectFactory(cf.getDriver(),
 			"jdbc:as400:tmsodev.nulogx.com;prompt=false", cf.getUser(), cf.getPassword());
 		connectFactoryI5 = cf1;
 	}
 	public static void clearCarriersNotFound() {
 		carriersNotFound.clear();
 	}
 	public static int getTrials() {
 		return trials;
 	}
 	public static void process(final Date shipDate, final String ftpSrv,
 		EmailSent es) throws InterruptedException {
 		trials++;
 		Thread t = new Thread() {
 			@Override
 			public void run() {
 				process1(shipDate, ftpSrv);
 				trials = 0;
 			}
 		};
 		t.setDaemon(true);
 		t.start();
 		int i = 0;
 		while (trials > 0 && i++ != 480) {
 			Thread.sleep(5000);
 		}
 		if (trials > 0) {
 			log.error("Report incomplete shipments");
 			if (trials > 2) {
 				EMailEmergency.send(es, trials+" trials to process shipments failed");
 			}
 		}
 	}
 	private static void process1(Date date, String ftpSrv) {
 		int shpDate = Functions.toInt(date);
 		ArrayList<ShipmentData> al = new ArrayList<ShipmentData>(1024);
 		Connection con = null, con1 = null;
 		try {
 			con = ConnectFactory1.one().getConnection();
 			con1 = connectFactoryI5.getConnection();
 			con1.setAutoCommit(true);
 			PreparedStatement st = con.prepareStatement(SQL_SEL_SHP);
 			int count = select(st, new ShipmentSrvc.HubStatements(con1), date, shpDate, al);
 			st.close();
 			
 			int n;
 			PreparedStatement st1;
 			/*st1 = con1.prepareStatement("DELETE FROM OS61LYDTA.OSPIFC1");
 			n = st1.executeUpdate();
 			st1.close();
 			st1 = con1.prepareStatement("DELETE FROM OS61LYDTA.OSPIFC2");
 			n = st1.executeUpdate();
 			st1.close();
 			st1 = con1.prepareStatement("DELETE FROM OS61LYDTA.OSPIFC9");
 			n = st1.executeUpdate();
 			st1.close();*/
 			st1 = con1.prepareStatement(SQL_DEL9);
 			st1.setInt(1, shpDate);
 			n = st1.executeUpdate();
 			st1.close();
 			st1 = con1.prepareStatement(SQL_DEL2);
 			st1.setInt(1, shpDate);
 			n = st1.executeUpdate();
 			st1.close();
 			st1 = con1.prepareStatement(SQL_DEL1);
 			st1.setInt(1, shpDate);
 			n = st1.executeUpdate();
 			st1.close();
 			n = n+0;
 			update(con1.prepareStatement(SQL_INS1), con1.prepareStatement(SQL_UPD1),
 				con1.prepareStatement(SQL_INS2), con1.prepareStatement(SQL_UPD2),
 				con1.prepareStatement(SQL_INS9), con1.prepareStatement(SQL_UPD9),
 				al, shpDate);
 			con1.commit();
 			
 			if (al.size() != 0) {
 				log.debug("\r\n\r\nSHIPMENTS: "+SupportTime.dd_MM_yyyy_Format.format(date)+
 					"\r\n\r\n"+al+
 					"\r\n\r\nTotal:   "+al.size()+
 					"\r\n\r\nMissing: "+(al.size()-count)+"\r\n");
 			}
 			/*String v = ShpTest.test(con1, shpDate, al);
 			if (v.length() != 0) {
 				log.error("\r\n\r\nDifferences:\r\n"+v);
 			}*/
 			if (ftpSrv != null) {
 				FTPClient f = new FTPClient();
 				f.connect(ftpSrv);
 				f.login(connectFactoryI5.getUser(), connectFactoryI5.getPassword());
 				f.sendCommand("RCMD call OS61LYDTA/run1200 parm('OS61LY')");
 				log.debug(f.getReplyString());
 				f.disconnect();
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
 	private static StringBuilder insDigits(String v) {
 		StringBuilder b = new StringBuilder(4);
 		int n = 4-v.length();
 		if (n > 0) {
 			TBuilder.add(b, '0', n);
 		}
 		b.append(v);
 		return b;
 	}
 	private static String getOrdN(ShipmentData sd) {
 		StringBuilder b = insDigits(sd.routeN);
 		String sn = String.valueOf(sd.storeN);
 		b.append(insDigits(sn));
 		return sd.dc+b.toString();
 	}
 	private static HashMap<String,String> getEquipSize(Connection con, Date date) throws Exception {
 		HashMap<String,String> m = new HashMap<String,String>(1024, .5f);
 		PreparedStatement st = con.prepareStatement(SQL_SEL_EQUIP_SIZE);
 		st.setDate(1, date);
 		ResultSet rs = st.executeQuery();
 		while (rs.next()) {
 			String equipSize = rs.getString(1);
 			String dc = rs.getString(2);
 			StringBuilder b = insDigits(rs.getString(3));
 			b.insert(0, dc);
 			m.put(b.toString(), equipSize);
 		}
 		rs.close();
 		return m;
 	}
 	private static int select(PreparedStatement st, ShipmentSrvc.HubStatements hst,
 		Date date, int shpDate, ArrayList<ShipmentData> al) throws Exception {
 		HashMap<String,String> esm = getEquipSize(st.getConnection(), date);
 		st.setDate(1, date);
 		HashMap<String,ShipmentItem> m = new HashMap<String,ShipmentItem>(64, .5f);
 		ResultSet rs = st.executeQuery();
 		if (!rs.next()) {
 			rs.close(); return 0;
 		}
 		int storeN;
 		int[] count = {0};
 		String dc, routeN;
 		ShipmentData sd = null;
 		while (true) {
 			storeN = rs.getInt(1);
 			routeN = rs.getString(4);
 			dc = rs.getString(6);
 			if (sd == null) {
 				sd = newData(storeN, dc, routeN);
 			}
 			else if (storeN != sd.storeN || !dc.equals(sd.dc) || !routeN.equals(sd.routeN)) {
 				addData(al, sd, count);
 				m.clear();
 				sd = newData(storeN, dc, routeN);
 			}
 			ShipmentItem si = new ShipmentItem();
 			si.orderN = rs.getString(14);
 			si.dsShipDate = rs.getDate(32);
 			if (addItem(m, sd, si)) {
 				if (sd.ordN == null) {
 					sd.ordN = getOrdN(sd);
 					sd.equipSize = esm.get(sd.ordN.substring(0, 6));
 					if (sd.equipSize == null) {
 						log.error("Equip.size does not exist");
 						sd.equipSize = "";
 					}
 					sd.cmdty = rs.getString(2);
 					sd.delDate = rs.getDate(3);
 					sd.stopN = rs.getString(5);
 					sd.dcDepartTime = rs.getTime(7);
 					sd.prevDistance = rs.getInt(8);
 					if (sd.dc.equals("30")) { sd.prevDistance /= 10d;}
 					sd.prevTravelTime = rs.getTime(9);
 					sd.arrivalTime = rs.getTime(10);
 					sd.serviceTime = rs.getTime(11);
 					sd.totalServiceTime = rs.getTime(12);
 					sd.totalTravelTime = rs.getTime(13);
 					sd.delTimeFrom = rs.getTime(19);
 					sd.delTimeTo = rs.getTime(20);
 					sd.specInstructs = rs.getString(21);
 					if (sd.specInstructs != null) {
 						sd.specInstructs = sd.specInstructs.trim();
 					}
 					sd.firstUserFile = rs.getString(26);
 					String nuf = rs.getString(27);
 					if (!sd.firstUserFile.equals(nuf)) { sd.nextUserFile = nuf;}
 				}
 				if (sd.lhCarrier == null) {
 					sd.lhCarrier = rs.getString(22);
 					if (sd.lhCarrier != null) {
 						String srv = rs.getString(23);
 						srv = Functions.cut(srv, 4);
 						sd.lhService = srv.isEmpty() ? "TL" : srv;
 					}
 				}
 				if (sd.delCarrier == null) {
 					sd.delCarrier = rs.getString(24);
 					if (sd.delCarrier != null) {
 						ShipmentSrvc.setService(hst, sd, shpDate);
 						String srv = rs.getString(25);
 						if (srv != null && !srv.trim().isEmpty()) {
 							srv = sd.delService;
 						}
 						sd.delService = ShipmentSrvc.getDelService(sd, srv);
 					}
 				}
 				if (sd.lhCarrier != null && sd.delCarrier != null) {
 					ShipmentSrvc.setHub(sd);
 				}
 				if (!sd.dcx && rs.getString(2).equals("DCX")) {
 					sd.dcx = true;
 				}
 
 				si.lw = si.orderN.substring(2, 4);
 				si.pallets = rs.getDouble(15);
 				si.units = rs.getDouble(16);
 				si.weight = rs.getDouble(17);
 				si.cube = rs.getDouble(18);
 				si.firstUserFile = rs.getString(28);
 				String nuf = rs.getString(29);
 				if (!si.firstUserFile.equals(nuf)) { si.nextUserFile = nuf;}
 				si.dsFirstUserFile = rs.getString(30);
 				nuf = rs.getString(31);
 				if (!si.dsFirstUserFile.equals(nuf)) { si.dsNextUserFile = nuf;}
 			}
 			if (!rs.next()) {
 				rs.close();
 				addData(al, sd, count);
 				break;
 			}
 		}
 		return count[0];
 	}
 	private static ShipmentData newData(int storeN, String dc, String routeN) {
 		ShipmentData sd = new ShipmentData();
 		sd.storeN = storeN;
 		sd.dc = dc;
 		sd.routeN = routeN;
 		return sd;
 	}
 	private static boolean addItem(HashMap<String,ShipmentItem> m, ShipmentData sd, ShipmentItem si) {
 		ShipmentItem si2 = m.get(si.orderN);
 		if (si2 != null) {
 			if (si.dsShipDate == si2.dsShipDate || si.dsShipDate != null &&
 				si2.dsShipDate != null || si2.dsShipDate != null) {
 				return false;
 			}
 			else if (si.dsShipDate != null) {
 				sd.items.remove(si2);
 			}
 		}
 		m.put(si.orderN, si);
 		sd.items.add(si);
 		return true;
 	}
 	private static void addData(ArrayList<ShipmentData> al, ShipmentData sd, int[] count) {
 		if (sd.delCarrier == null/* && sd.lhCarrier == null*/) {
 			int dow = SupportTime.getDayOfWeek(sd.delDate);
 			DsKey k = new DsKey(sd.storeN, sd.cmdty, dow);
 			if (carriersNotFound.add(k)) {
 				String type = sd.items.get(0).dsShipDate == null ? "regular" : "holidays";
 				log.error("Carrier not found (" + type + "): "+k);
 			}
 			sd.missing = true;
 		}
 		else { count[0]++;}
 		if (sd.lhCarrier == null) {
 			sd.hub = "";
 		}
 		al.add(sd);
 	}
 	private static void update(PreparedStatement ins1, PreparedStatement upd1,
 		PreparedStatement ins2, PreparedStatement upd2, PreparedStatement ins9,
 		PreparedStatement upd9, ArrayList<ShipmentData> al, int shpDate) throws Exception {
 		ArrayList<Functions.Ref> refs = new ArrayList<Functions.Ref>(8);
 		for (Iterator<ShipmentData> it = al.iterator(); it.hasNext();) {
 			ShipmentData sd = it.next();
 			if (sd.missing) { continue;}
 			//setTable1(upd1, sd);
 			//if (upd1.executeUpdate() == 0) {
 				setTable1(ins1, sd, shpDate);
 				ins1.addBatch();
 			//}
 			//Functions.putRefs(refs, sd, 0);
 			//updateTable9(upd9, ins9, refs, sd.ordN, 0);
 		}
 		ins1.executeBatch();
 		ins9.executeBatch();
 
 		for (Iterator<ShipmentData> it = al.iterator(); it.hasNext();) {
 			ShipmentData sd = it.next();
 			if (sd.missing) { continue;}
 			int i = 1;
 			if (sd.lhCarrier != null) {
 				//setTable2(upd2, sd, sd.lhCarrier, sd.lhService, i);
 				//if (upd2.executeUpdate() == 0) {
 					setTable2(ins2, sd, sd.lhCarrier, sd.lhService, i, shpDate);
 					ins2.addBatch();
 				//}
 				Functions.putRefs(refs, sd, i);
 				updateTable9(upd9, ins9, refs, sd.ordN, i);
 				i++;
 			}
 			if (sd.delCarrier != null) {
 				//setTable2(upd2, sd, sd.delCarrier, sd.delService, i);
 				//if (upd2.executeUpdate() == 0) {
 					setTable2(ins2, sd, sd.delCarrier, sd.delService, i, shpDate);
 					ins2.addBatch();
 				//}
 				Functions.putRefs(refs, sd, i);
 				updateTable9(upd9, ins9, refs, sd.ordN, i);
 			}
 		}
 		ins2.executeBatch();
 		ins9.executeBatch();
 	}
 	private static void setTable1(PreparedStatement st, ShipmentData sd,
 		int shpDate) throws Exception {
 		st.setDouble(1, sd.getTotalWeight(true));
 		st.setDouble(2, 1);//sd.getTotalUnits(null));
 		st.setDouble(3, sd.getTotalCube(true));
 		st.setDouble(4, 0);//height
 		st.setDouble(5, sd.getTotalPallets());
 		st.setInt(6, shpDate);
 		st.setInt(7, Functions.toInt(sd.delDate));
 		st.setString(8, String.valueOf(sd.storeN));
 		st.setString(9, MATRIXDC+sd.dc);
 		if (sd.dc.equals("20")) {
 			st.setDouble(10, 0);
 			st.setDouble(11, 0);
 			st.setString(12, sd.dcx ? "CX" : "NR");
 		}
 		else {
 			st.setDouble(10, sd.prevDistance);
 			int t1 = Functions.toMins(sd.serviceTime);
 			Time t = new Time(sd.prevTravelTime.getTime()+t1*60000);
 			st.setDouble(11, Functions.toDouble(t));
 			st.setString(12, "NR");
 		}
 		st.setInt(13, 1);
 		st.setInt(14, Integer.parseInt(sd.stopN)+1);
 		st.setString(15, sd.specInstructs == null ? "" : sd.specInstructs);
 		st.setString(16, Functions.cut(sd.delCarrier, 8));
 		st.setString(17, sd.delService);
 		st.setString(18, sd.dc.equals("10") ? "Y" : "N");//Urgent flag
 		st.setString(19, sd.ordN);
 	}
 	private static void setTable2(PreparedStatement st, ShipmentData sd,
 		String carrier, String service, int leg, int shpDate) throws Exception {
 		st.setString(1, Functions.cut(carrier, 8));
 		st.setString(2, service);
 		if (sd.lhCarrier == null) {
 			st.setString(3, "");
 			st.setInt(4, shpDate);
 			st.setInt(5, Functions.toInt(sd.delDate));
 		}
 		else {
 			Date xDocDate = new Date(sd.delDate.getTime()-SupportTime.DAY);
 			if (leg == 1) {
 				st.setString(3, sd.hub);
 				st.setInt(4, shpDate);
 				st.setInt(5, Functions.toInt(xDocDate));
 			}
 			else {
 				st.setString(3, "");
 				st.setInt(4, Functions.toInt(xDocDate));
 				st.setInt(5, Functions.toInt(sd.delDate));
 			}
 		}
 		st.setString(6, "Y");//Carrier-Commit Flag
 		st.setString(7, "Y");//Service-Commit Flag
 		st.setInt(8, 1);//Pick Stop #
 		st.setInt(9, Integer.parseInt(sd.stopN)+1);//Drop Stop #
 		st.setString(10, Functions.getGroupID(sd, leg));//Shipment Group Id
 		st.setString(11, sd.ordN);
 		st.setDouble(12, leg);
 	}
 	private static void updateTable9(PreparedStatement upd, PreparedStatement ins,
 		ArrayList<Functions.Ref> refs, String ordN, int leg) throws Exception {
 		for (Iterator<Functions.Ref> it = refs.iterator(); it.hasNext();) {
 			Functions.Ref r = it.next();
 			//setTable9(upd, r, ordN, leg);
 			//if (upd.executeUpdate() == 0) {
 				setTable9(ins, r, ordN, leg);
 				ins.addBatch();
 			//}
 		}
 	}
 	private static void setTable9(PreparedStatement st, Functions.Ref r,
 		String ordN, int leg) throws Exception {
 		st.setString(1, r.value);//Reference #
 		st.setString(2, ordN);
 		st.setDouble(3, leg);
 		st.setString(4, r.name);//Reference Qualifier
 	}
 
 }
