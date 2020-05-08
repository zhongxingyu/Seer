 package de.hattrickorganizer.database;
 
 import java.sql.ResultSet;
 import java.sql.Types;
 import java.util.Vector;
 
 import de.hattrickorganizer.model.ScoutEintrag;
 import de.hattrickorganizer.tools.HOLogger;
 
 public final class ScoutTable extends AbstractTable {
 
 	/** tablename **/
 	public final static String TABLENAME = "SCOUT";
 	
 	protected ScoutTable(JDBCAdapter  adapter){
 		super(TABLENAME,adapter);
 	}
 
 	@Override
 	protected void initColumns() {
 		columns = new ColumnDescriptor[23];
 		columns[0]= new ColumnDescriptor("PlayerID",Types.INTEGER,false);
 		columns[1]= new ColumnDescriptor("Name",Types.VARCHAR,true,127);
 		columns[2]= new ColumnDescriptor("Info",Types.VARCHAR,false,256);
 		columns[3]= new ColumnDescriptor("Age",Types.INTEGER,false);
 		columns[4]= new ColumnDescriptor("Marktwert",Types.INTEGER,false);
 		//columns[5]= new ColumnDescriptor("Finanzberater",Types.VARCHAR,true,127);
 		columns[5]= new ColumnDescriptor("Speciality",Types.INTEGER,false);
 		columns[6]= new ColumnDescriptor("Kondition",Types.INTEGER,false);
 		columns[7]= new ColumnDescriptor("Erfahrung",Types.INTEGER,false);
 		columns[8]= new ColumnDescriptor("Form",Types.INTEGER,false);
 		columns[9]= new ColumnDescriptor("Torwart",Types.INTEGER,false);
 		columns[10]= new ColumnDescriptor("Verteidigung",Types.INTEGER,false);
 		columns[11]= new ColumnDescriptor("Spielaufbau",Types.INTEGER,false);
 		columns[12]= new ColumnDescriptor("Fluegel",Types.INTEGER,false);
 		columns[13]= new ColumnDescriptor("Torschuss",Types.INTEGER,false);
 		columns[14]= new ColumnDescriptor("Passpiel",Types.INTEGER,false);
 		columns[15]= new ColumnDescriptor("Standards",Types.INTEGER,false);
 		columns[16]= new ColumnDescriptor("Price",Types.INTEGER,false);
 		columns[17]= new ColumnDescriptor("Deadline",Types.TIMESTAMP,false);
 		columns[18]= new ColumnDescriptor("Wecker",Types.BOOLEAN,false);
 		columns[19]= new ColumnDescriptor("AgeDays",Types.INTEGER,false);
 		columns[20]= new ColumnDescriptor("Agreeability",Types.INTEGER,false);
 		columns[21]= new ColumnDescriptor("baseWage",Types.INTEGER,false);
 		columns[22]= new ColumnDescriptor("Nationality",Types.INTEGER,false);
 	}
 
 	/**
 	 * Save players from TransferScout
 	 *
 	 * @param hrfId TODO Missing Constructuor Parameter Documentation
 	 * @param list TODO Missing Constructuor Parameter Documentation
 	 */
 	protected void saveScoutList(Vector<ScoutEintrag> list) {
 		String sql = "";
 		String bool = "0";
 
 		// What should be done when list = null?? jailbird.
 		if (list != null) {
 			// Delete already existing list
 
 			delete( null,null );
 			for (int i = 0; i < list.size(); i++) {
 				final ScoutEintrag scout = list.elementAt(i);
 
 				if (scout.isWecker()) {
 					bool = "1";
 				} else {
 					bool = "0";
 				}
 
 				// Prepare insert statement
 				sql =
 					"INSERT INTO "+getTableName()+" (Name, Info, Age, AgeDays, Marktwert, Kondition, Erfahrung,  Form, Torwart, Verteidigung, Spielaufbau, Fluegel, Torschuss, Passpiel, Standards, Deadline, Wecker, PlayerID, Speciality, Price, Agreeability, baseWage, Nationality ) VALUES (";
 				sql
 					+= ("'"
 						+ de.hattrickorganizer.database.DBZugriff.insertEscapeSequences(scout.getName())
 						+ "','"
 						+ de.hattrickorganizer.database.DBZugriff.insertEscapeSequences(scout.getInfo())
 						+ "',"
 						+ scout.getAlter()
 						+ ","
 						+ scout.getAgeDays()
 						+ ","
 						+ scout.getTSI()
 						+ ","
 						+ scout.getKondition()
 						+ ","
 						+ scout.getErfahrung()
 						+ ","
 						+ scout.getForm()
 						+ ","
 						+ scout.getTorwart()
 						+ ","
 						+ scout.getVerteidigung()
 						+ ","
 						+ scout.getSpielaufbau()
 						+ ","
 						+ scout.getFluegelspiel()
 						+ ","
 						+ scout.getTorschuss()
 						+ ","
 						+ scout.getPasspiel()
 						+ ","
 						+ scout.getStandards()
 						+ ",'"
 						+ scout.getDeadline().toString()
 						+ "',"
 						+ bool
 						+ ", "
 						+ scout.getPlayerID()
 						+ ", "
 						+ scout.getSpeciality()
 						+ ", "
 						+ scout.getPrice()
 						+ ", "
 						+ scout.getAgreeability()
 						+ ", "
 						+ scout.getbaseWage()
 						+ ", "
 						+ scout.getNationality()
 						+ ")");
 
 				try {
 					adapter.executeUpdate(sql);
 				} catch (Exception e) {
 					HOLogger.instance().log(getClass(),"DBZugriff.deleteScoutTabelle: " + e);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Load player list for insertion into TransferScout
 	 *
 	 * @return TODO Missing Return Method Documentation
 	 */
 	public Vector<ScoutEintrag> getScoutList() {
 		final Vector<ScoutEintrag> ret = new Vector<ScoutEintrag>();
 
 		try {
 			final String sql =
				"SELECT PlayerID, Name, Info, Age, AgeDays, Marktwert, Speciality, Kondition, Erfahrung, Form, Torwart, Verteidigung, Spielaufbau, Fluegel, Torschuss, Passpiel, Standards, Price, Deadline, Wecker FROM "+getTableName();
 			final ResultSet rs = adapter.executeQuery(sql);
 			rs.beforeFirst();
 
 			while (rs.next()) {
 				final ScoutEintrag scout = new ScoutEintrag(rs);
 				ret.add(scout);
 			}
 		} catch (Exception e) {
 			HOLogger.instance().log(getClass(),"DBZugriff.getScoutList: " + e);
 		}
 
 		return ret;
 	}
 	
 }
