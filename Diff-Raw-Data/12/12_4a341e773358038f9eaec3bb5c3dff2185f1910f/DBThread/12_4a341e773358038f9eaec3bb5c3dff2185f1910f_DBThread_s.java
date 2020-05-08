/* $Id: DBThread.java,v 1.11 2007/05/22 22:25:56 jinli Exp $ */
 
 package niagara.data_manager;
 
 /**
  * Niagra DataManager DBThread - an input thread to connect to a database,
  * execute a query and put the results into a stream of tuples that can be
  * processed by the database
  */
 
 import java.lang.reflect.Array;
 import org.w3c.dom.*;
 import javax.xml.parsers.*;
 
 import niagara.logical.DBScan;
 import niagara.logical.DBScanSpec;
 import niagara.optimizer.colombia.*;
 import niagara.query_engine.TupleSchema;
 import niagara.utils.*;
 import niagara.utils.BaseAttr.Type;
 import niagara.logical.SimilaritySpec;
 
 // for jdbc
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.Properties;
 import java.sql.Time;
 
 /**
  * DBThread provides a SourceThread compatible interface to fetch data from a
  * rdbms via jdbc
  */
 public class DBThread extends SourceThread {	
 	// some truth we all know;
 	public static final int SECOND_PER_MIN = 60;
 	public static final int MILLISEC_PER_SEC = 1000;
 	public static final int MIN_PER_HOUR = 60;
 	public static final int HOUR_PER_DAY = 24;
     public static final int DAYS_PER_WEEK = 7;
     
     private static final int ONE_DEMO_RUN = 2 * MIN_PER_HOUR * SECOND_PER_MIN * MILLISEC_PER_SEC;
     
     // weather similarity hack;
     private static final int MAX_HISTORY = 60;
     private static final int LOOKBACK = 30;
     private static final int WEATHER_OFFSET = 1; 
     private static final int TIME_COLUMN = 1;
 
 	private final boolean DEBUG = false;
 	
 	// think these are optimization time??
 	public DBScanSpec dbScanSpec;
 	private Attribute[] variables;
 	private SimilaritySpec sSpec;
 	
 	// database connection;
 	private Connection conn = null;
 	private Statement stmt = null;
 	
 	private ArrayList similarDate;
 	private long epoch = -1;
 
 	// these are run-time??
 	public SinkTupleStream outputStream;
 	public CPUTimer cpuTimer;
 	private ArrayList newQueries;
 	private boolean finish = false;
 	private boolean shutdown = false; 
 	
 	// whether this is a first query got from punctQC? 
 	private int count = 0;
 	
 	// intrumentation
 	boolean polled = false;
 	Document doc;
 	
 	private class Stage {
 		//for x-axis
 		long starttime, endtime;
 		// for y-axis
 		ArrayList<Long> dates;
 		
         Actors activeActors;
         Actors exeunt;
 	};
 	
 	private class Actors {
 		//ArrayList <Long> dates;
 		ArrayList <Long> starts;
 		ArrayList <Long> ends;
 		ArrayList <Status> status;
 	};
 	
 	enum Status {FUTURE, DONE, PROGRESS};
 
 	private ArrayList<Stage> stagelist;
 	//private Actors activeActors;
 	//private Actors exeunt;
 	
 	// object for synchronization;
 	private Object synch = new Object(); 
 	
 	public DBThread() {
 		newQueries = new ArrayList();
 	}
 	
 	/**
 	 * @see niagara.optimizer.colombia.PhysicalOp#initFrom(LogicalOp)
 	 */
 	public void opInitFrom(LogicalOp lop) {
 		DBScan op = (DBScan) lop;
 		dbScanSpec = op.getSpec();
 		variables = op.getVariables();
 		sSpec = op.getSimilaritySpec();
 	}
 
 	public void constructTupleSchema(TupleSchema[] inputSchemas) {
 		;
 	}
 
 	public TupleSchema getTupleSchema() {
 		TupleSchema ts = new TupleSchema();
 		ts.addMappings(new Attrs(variables, Array.getLength(variables)));
 		return ts;
 	}
 
 	/**
 	 * @see niagara.optimizer.colombia.PhysicalOp#FindLocalCost(ICatalog,
 	 *      LogicalProperty, LogicalProperty[])
 	 */
 	public Cost findLocalCost(ICatalog catalog, LogicalProperty[] InputLogProp) {
 		// XXX vpapad: totally bogus flat cost for stream scans
 		return new Cost(catalog.getDouble("stream_scan_cost"));
 	}
 
 	/**
 	 * @see java.lang.Object#hashCode()
 	 */
 	public int hashCode() {
 		// XXX vpapad: spec's hashCode is Object.hashCode()
 		return dbScanSpec.hashCode();
 	}
 
 	/**
 	 * @see java.lang.Object#equals(Object)
 	 */
 	public boolean equals(Object o) {
 		if (o == null || !(o instanceof DBThread))
 			return false;
 		if (o.getClass() != getClass())
 			return o.equals(this);
 		// XXX vpapad: Spec.equals is Object.equals
 		if (dbScanSpec.equals(((DBThread) o).dbScanSpec))
 			System.err.println("DBSCAN equals true");
 		return dbScanSpec.equals(((DBThread) o).dbScanSpec) ^ 
 				equalsNullsAllowed(sSpec, ((DBThread) o).sSpec);
 	}
 
 	/**
 	 * @see niagara.optimizer.colombia.Op#copy()
 	 */
 	public Op opCopy() {
 		DBThread dt = new DBThread();
 		dt.dbScanSpec = dbScanSpec;
 		dt.variables = variables;
 		dt.sSpec = sSpec;
 		return dt;
 	}
 
 	public void plugIn(SinkTupleStream outputStream, DataManager dm) {
 		this.outputStream = outputStream;
 	}
 
 	public void dumpAttributesInXML(StringBuffer sb) {
 	    sb.append(" var='");
 	    for (int i = 0; i < variables.length; i++) {
 		sb.append(variables[i].getName());
 		if (i != variables.length-1)
 		    sb.append(", ");
 	    }
 	    sb.append("'/>");
 	}
 
 	/**
 	 * Thread run method
 	 * 
 	 */
 	public void run() {
 
 		
 		try {
 			Class.forName("org.postgresql.Driver");
 	
 		} catch (ClassNotFoundException e) {
 			System.out.println("Class not found " + e.getMessage());
 			return;
 		}
 		if (niagara.connection_server.NiagraServer.RUNNING_NIPROF)
 			JProf.registerThreadName(this.getName());
 	
 		if (niagara.connection_server.NiagraServer.TIME_OPERATORS) {
 			this.cpuTimer = new CPUTimer();
 			this.cpuTimer.start();
 		}
 	
 		// steps
 		// connect to the database
 		// execute the query
 		// put the results into a tuple
 		ResultSet rs = null;
 
 		try {
 			System.out.println("Attempting to connect to server.");
 			//conn = DriverManager.getConnection("jdbc:postgresql://barista.cs.pdx.edu/latte", "tufte", "loopdata");
 			Properties properties = new Properties();
 			properties.setProperty("user", "tufte");
 			properties.setProperty("password", "loopdata");
 
 			conn = DriverManager.getConnection("jdbc:postgresql://barista.cs.pdx.edu/latte", properties);
 
 			System.out.println("Connected.");
 			
 			stmt = conn.createStatement();
 
 			stmt.execute("SET work_mem=100000");
 			String qs;
 			
 			// one time db query;
 			if (dbScanSpec.oneTime()) {
 				qs = dbScanSpec.getQueryString();
 				System.out.println("query string: "+qs);
 		
 				System.out.println("DB Query is:" + qs);
 				//stmt = conn.createStatement();
 				System.out.println("Attempting to execute query.");
 
 				rs = stmt.executeQuery(qs);
 				//System.out.println("Executed.");				
 		
 				// Now do something with the ResultSet ....
 				int numAttrs = dbScanSpec.getNumAttrs();
 				//System.out.println("Num Attrs: " + numAttrs);
 				
 				putResults(rs, numAttrs);
 
 				outputStream.endOfStream();
 			} else 
 				do { // continuous db scan
 					/*
 					 * response to query shutdown;
 					 */
 					if (shutdown) {
 						outputStream.putCtrlMsg(CtrlFlags.SHUTDOWN, "bouncing back SHUTDOWN msg");
 						
 						break;
 					}
 						
 					/*
 					 * if this is a continuous db scan
 					 */	
 					if (newQueries.size()==0) {
 						if (finish){
 							outputStream.endOfStream();
 							break;
 						} else {
 							//blocking call to get ctrl msg;
 							checkForSinkCtrlMsg();
 							
 							if (newQueries.size()==0)
 								continue;
 						}
 					}
 						
 					stmt = conn.createStatement();
 					System.out.println("Attempting to execute query.");
 					
 
 					
 					String queryString = (String) newQueries.remove(0);
 					if (DEBUG)
 						System.err.println(queryString);
 					
 					// an actor is in progress
 					synchronized (synch) {
 						//activeActors.status.set(0, Status.PROGRESS);
 						for (Stage curr: stagelist) {
 							if (curr.activeActors.status.size() != 0) {
 								curr.activeActors.status.set(0, Status.PROGRESS);
 								break;
 							}
 						}
 					}
 					
 					rs = stmt.executeQuery(queryString);
 				
 					// Now do something with the ResultSet ....
 					int numAttrs = dbScanSpec.getNumAttrs();
 									
 					putResults(rs, numAttrs);
 					
 					// the actor is done, moved to exeunt list;
 					synchronized (synch) {
 						//Stage curr = stagelist.get(stagelist.size() - 1);
 						for (Stage curr: stagelist) {
 							if (curr.activeActors.status.size() != 0) {
 								if (curr.exeunt == null) {
 									curr.exeunt = new Actors();
 									curr.exeunt.starts = new ArrayList <Long> ();
 									curr.exeunt.ends = new ArrayList <Long> ();
 									curr.exeunt.status = new ArrayList <Status> ();
 								}
 								curr.exeunt.starts.add(curr.activeActors.starts.remove(0));
 								curr.exeunt.ends.add(curr.activeActors.ends.remove(0));
 								curr.activeActors.status.remove(0);
 								curr.exeunt.status.add(Status.DONE);
 
 								break;		
 							}
 						}
 					}
 					
 					//ArrayList instrumentationNames = new ArrayList ();
 					//ArrayList instrumentationValues = new ArrayList ();
 					//getInstrumentationValues(instrumentationNames, instrumentationValues);
 					
 				} while (true);
 			
 		} catch (SQLException ex) {
 			// handle any errors
 			System.out.println("SQLException: " + ex.getMessage());
 			System.out.println("SQLState: " + ex.getSQLState());
 			System.out.println("VendorError: " + ex.getErrorCode());
 			try {
 				outputStream.putCtrlMsg(CtrlFlags.SHUTDOWN, ex.getMessage());
 			} catch (Exception e) {
 				// just ignore it
 			}
 		} catch(ShutdownException se) {
 			try {
 			//outputStream.endOfStream();
 			} catch (Exception e) {
 				//ignore it
 			}
 			// no need to send shutdown, we must have got it from the output stream
 		} catch(InterruptedException ie) {
 			try {
 				outputStream.putCtrlMsg(CtrlFlags.SHUTDOWN, ie.getMessage());
 			} catch (Exception e) {
 				// just ignore it
 			}
 		} finally {
 		 // it is a good idea to release
 		 // resources in a finally{} block
 		 // in reverse-order of their creation
 		 // if they are no-longer needed
 	
 		 if (rs != null) {
 		 try {
 			 rs.close();
 		 } catch (SQLException sqlEx) { // ignore
 			 rs = null;
 		 }
 		 }
 	
 		 if (stmt != null) {
 		 try {
 			 stmt.close();
 		 } catch (SQLException sqlEx) { // ignore
 			 stmt = null;
 		 }
 		 }
 	
 		 if (conn != null) {
 		 try {
 			 conn.close();
 		 } catch (SQLException sqlEx) { // ignore
 			 conn = null;
 		 }
 		 }
 	   }// end of finally
 	
 
 	}	// end of run method
 	
 	private void putResults (ResultSet rs, int numAttrs) 
 		throws SQLException, ShutdownException, InterruptedException {
 
 		while (rs.next()) {
 			Tuple newtuple = new Tuple(false, numAttrs);
 			for(int i = 1; i<= numAttrs; i++) {
 				// get attribut type
 				// then create the appropriate object
 				// finally, load attribute into the tuple
 				BaseAttr attr;
 				switch(dbScanSpec.getAttrType(i-1)) {
 				case Int:
 					attr = new IntegerAttr();
 					Integer iObj = new Integer(rs.getInt(i));
 					attr.loadFromObject(iObj);
 					break;
 				case Long:
 					attr = new LongAttr();
 					Long lObj = new Long(rs.getLong(i));
 					attr.loadFromObject(lObj);
 					break;
 				case TS:
 					attr = new TSAttr();
 					Long tObj = new Long(rs.getLong(i));
 					attr.loadFromObject(tObj);
 					break;
 				case String:
 					attr = new StringAttr();
 					attr.loadFromObject(rs.getString(i));
 					break;
 				case XML:
 					attr = new XMLAttr();
 					attr.loadFromObject(rs.getString(i));
 					break;
 				default:
 					throw new PEException("Invalid type " + dbScanSpec.getAttrType(i));
 				}
 				
 				newtuple.appendAttribute(attr);
 			}  // end for
 			ArrayList ctrl = outputStream.putTuple(newtuple);
 			while(ctrl != null) {
 				processCtrlMsgFromSink(ctrl);
 				ctrl = outputStream.flushBuffer();
 				//assert ctrlFlag == CtrlFlags.NULLFLAG: "bad ctrl flag ";
 			}
 			
 		} // end while				
 
 	}
 	
     public void checkForSinkCtrlMsg()
 	throws java.lang.InterruptedException, ShutdownException, SQLException {
 	// Loop over all sink streams, checking for control elements
     ArrayList ctrl;
 
     // Make sure the stream is not closed before checking is done
     if (!outputStream.isClosed()) {
     	ctrl = outputStream.getCtrlMsg(1*MILLISEC_PER_SEC);
 	    
     	// If got a ctrl message, process it
     	if (ctrl != null) {
     		processCtrlMsgFromSink(ctrl);
     	} else {
     		
     	}
     	}
     }
     
     public void processCtrlMsgFromSink(ArrayList ctrl) 
     	throws SQLException, ShutdownException, InterruptedException {
     	if (ctrl == null)
     		return;
     	
     	int ctrlFlag = (Integer)ctrl.get(0); 
     	String ctrlMsg = (String)ctrl.get(1);
     	switch (ctrlFlag) {
     	case CtrlFlags.CHANGE_QUERY:
     		newQueries.add(changeQuery(ctrlMsg));
     		break;
     	case CtrlFlags.READY_TO_FINISH:
     		System.err.println("live stream ends...");
     		finish = true;
     		break;
     	case CtrlFlags.SHUTDOWN: //handle query shutdown;
     		System.err.println("ready to shutdown at DBThread");
     		shutdown = true;
     		break;
     	default:
     		assert false : "KT unexpected control message from source " + 
 		    CtrlFlags.name[ctrlFlag];
     	}
     }
     
     private String changeQuery (String queryPara) 
     throws java.lang.InterruptedException, ShutdownException, SQLException {
     	String[] queryRange = queryPara.split("[ |\t]+");
     	assert queryRange.length == 2: "Jenny - range is more than a pair?!";
 
     	long realStart = Long.valueOf(queryRange[0]);;
 		
     	if (count == 0) {
 			realStart -= sSpec.getNumOfMins()*SECOND_PER_MIN;
 			count++;
 		}
     	
     	if (!sSpec.getWeather()) {
    	   		similarDate = getSimilarDate (MILLISEC_PER_SEC*realStart, 
    	   				MILLISEC_PER_SEC*Long.valueOf(queryRange[1]));
    	   		
    	   		synchronized (synch) {
    	   			if (stagelist == null) {
    	   				stagelist = new ArrayList ();
    	   				stagelist.add(setStage(similarDate, MILLISEC_PER_SEC*realStart));
    	   			}
    	   		}
     	} else {
     		if (nextEpoch(realStart*MILLISEC_PER_SEC)) {
     			ResultSet rs;
     			int num = 0; 
     			similarDate = new ArrayList ();
     			while (similarDate.size() < sSpec.getNumOfDays() && num < MAX_HISTORY/LOOKBACK) {
     				rs = stmt.executeQuery(similarWeather(
     						MILLISEC_PER_SEC*(realStart+num*LOOKBACK*HOUR_PER_DAY*MIN_PER_HOUR*SECOND_PER_MIN), 
     						getRainfall(realStart)));
     				extractSimilarDate (similarDate, rs);
     				num++;
     			}
     		} 
     		
     		synchronized (synch) {
 	    		if (stagelist == null) {
 	    			stagelist = new ArrayList ();
 	    			stagelist.add(setStage(similarDate, MILLISEC_PER_SEC*realStart));
 	    		} else {
 	    			// change stage
 	    			long startTS = stagelist.get(0).starttime;
 	    			stagelist.add(setStage(similarDate, startTS));
 	    		}
     		}
     		
     	}
 
     	// form query to retrieve archive data from db;
     	return newQuery (similarDate, MILLISEC_PER_SEC*realStart, MILLISEC_PER_SEC*Long.valueOf(queryRange[1]));
     }
     
     /**
      * 
      * @param start
      * @return
      */
     private boolean nextEpoch (long start) {
     	Calendar calendar = GregorianCalendar.getInstance();
     	
     	calendar.setTimeInMillis(start);
     	System.err.println(calendar.getTime().toString());
     	
     	int hour = calendar.get(Calendar.HOUR_OF_DAY);
     	
     	System.err.println("********************hour: "+hour+"  epoch: "+epoch+"***********************");
     	if (hour == epoch)
     		return false;
     	else {
     		epoch = hour;
     		return true;
     	}
     }
 
     
     /**
      * 
      * @param date An arrayList of dates with similar weather;
      * @return
      */
     private String newQuery (ArrayList <Long> date, long start, long end) {
     	assert (stagelist != null): "how can stagelist be null";
     	synchronized (synch) {
     		Stage curr = stagelist.get(stagelist.size() - 1);
     		
 			if (curr.activeActors == null) {
 				curr.activeActors = new Actors();
 				curr.activeActors.starts = new ArrayList <Long> ();
 				curr.activeActors.ends = new ArrayList <Long> ();
 				curr.activeActors.status = new ArrayList ();
 			}
 			//activeActors.dates = date;
 			curr.activeActors.starts.add(start);
 			curr.activeActors.ends.add(end);
 			curr.activeActors.status.add(Status.FUTURE);
 		}
 
 		
 
 		Calendar calendar = GregorianCalendar.getInstance();
 		Calendar past = GregorianCalendar.getInstance();
 		
 		//calendar.setTimeInMillis(start*1000);
 		//String startTS = calendar.getTime().toString();
 		//calendar.setTimeInMillis(end*1000);
 		//String endTS = calendar.getTime().toString();
 		int numDays = sSpec.getNumOfDays();
 		int numMins = sSpec.getNumOfMins();
 
 		StringBuffer query = new StringBuffer("(");
 
 		//queryString.replace ("NUMOFDAYS", "interval '1 day'");
 		//query.append(queryString);
 		String upper, lower;
 		int offset;
 		for (int i = 0; i < numDays; i++) {
 			if (i > 0)
 				query.append(" union all ");
 			
 			past.setTimeInMillis((Long)date.get(i));
 							
 			calendar.setTimeInMillis(start);
 			
 			// the offset (in number of days) between now and the picked date with similar weather 			
 			offset = calendar.get(Calendar.DAY_OF_YEAR) - past.get(Calendar.DAY_OF_YEAR); 
 			
 			
 			calendar.roll(Calendar.DAY_OF_YEAR, -offset);
 
 			lower = calendar.getTime().toString();
 				
 			calendar.setTimeInMillis(end);			
 			calendar.roll(Calendar.DAY_OF_YEAR, -offset);				
 			//calendar.add(Calendar.MINUTE, numMins);				
 			upper = calendar.getTime().toString();
 			
 			query.append(dbScanSpec.getQueryString().replace("NUMOFDAYS", " '" +offset+" days'").replace("TIMEPREDICATE", timePredicate(upper, lower)));
 				
 		}
 		query.append(") order by panetime");
 		return query.toString();
     }
 
 	/**
 	 * @param date
 	 * @param start
 	 */
 	private Stage setStage(ArrayList<Long> date, long start) {
 		 
 		Stage	stage = new Stage ();
 		
 		stage.starttime = start;
 		stage.endtime = stage.starttime + ONE_DEMO_RUN;
         stage.dates = new ArrayList <Long> ();
         stage.dates = date; 
         
         return stage;
 		/*stage.startdate = Long.MAX_VALUE;
 		stage.enddate = Long.MIN_VALUE;
 		
 		for ( Long time : date) {
 			if (time < stage.startdate)
 				stage.startdate = time;
 			if (time > stage.enddate)
 				stage.enddate = time;
 		}*/
 	}
     
 	/**
 	 * @param start
 	 * @param end
 	 * @return
 	 */
 	private ArrayList getSimilarDate(long start, long end) {
 		Calendar calendar = GregorianCalendar.getInstance();
 		
 		int numDays = sSpec.getNumOfDays();
 		int numMins = sSpec.getNumOfMins();
 
 		StringBuffer query = new StringBuffer("(");
 
 		ArrayList <Long> dates = new ArrayList();
 		String upper, lower;
 		switch (sSpec.getSimilarityType()) {
 		case AllDays:
 			for (int i = 1; i <= numDays; i++) {
 				calendar.setTimeInMillis(start);											
 				calendar.roll(Calendar.DAY_OF_YEAR, -i);				
 
 				dates.add(calendar.getTimeInMillis());
 
 				/*
 				if (i > 1)
 					query.append(" union all ");
 				
 				//query.append(queryString.replace("NUMOFDAYS", " '" +i+" days'"));
 				
 				calendar.setTimeInMillis(start);											
 				calendar.roll(Calendar.DAY_OF_YEAR, -i);				
 				//calendar.add(Calendar.MINUTE, -numMins);				
 				lower = calendar.getTime().toString();
 				
 				calendar.setTimeInMillis(end);			
 				calendar.roll(Calendar.DAY_OF_YEAR, -i);				
 				//calendar.add(Calendar.MINUTE, numMins);				
 				upper = calendar.getTime().toString();
 				
 				query.append(dbScanSpec.getQueryString().replace("NUMOFDAYS", " '" +i+" days'").replace("TIMEPREDICATE", timePredicate(upper, lower)));
 				*/
 			}
 			//query.append(") order by panetime");
 			break;
 			
 		case SameDayOfWeek:
 			for (int i = 1; i <= numDays; i++) {				
 				calendar.setTimeInMillis(start);											
 				calendar.roll(Calendar.DAY_OF_YEAR, -i*DAYS_PER_WEEK);				
 
 				dates.add(calendar.getTimeInMillis());
 
 				/*
 				calendar.setTimeInMillis(start);											
 				calendar.roll(Calendar.DAY_OF_YEAR, -i*DAYS_PER_WEEK);				
 				
 				lower = calendar.getTime().toString();
 				
 				calendar.setTimeInMillis(end);			
 				calendar.roll(Calendar.DAY_OF_YEAR, -i*DAYS_PER_WEEK);				
 				
 				upper = calendar.getTime().toString();
 				
 				if (i > 1)
 					query.append(" union all ");
 
 				query.append(dbScanSpec.getQueryString().replace("NUMOFDAYS", " '" +i*DAYS_PER_WEEK+" days'").replace("TIMEPREDICATE", timePredicate(upper, lower)));
 				*/
 			}
 			//query.append(") order by panetime");
 			break;
 	
 		case WeekDays:	
 			int i = 1, offset = 1, weekday;
 			while (i <= numDays) {
 				calendar.setTimeInMillis(start);											
 				calendar.roll(Calendar.DAY_OF_YEAR, -offset);
 				weekday = calendar.get(Calendar.DAY_OF_WEEK);
 				if (weekday == Calendar.SUNDAY || weekday == Calendar.SATURDAY) {
 					offset++;
 					continue;
 				}
 				i++;
 				offset++;
 				dates.add(calendar.getTimeInMillis());
 
 				/*
 				calendar.setTimeInMillis(start);											
 				calendar.roll(Calendar.DAY_OF_YEAR, -offset);
 				weekday = calendar.get(Calendar.DAY_OF_WEEK);
 				if (weekday == Calendar.SUNDAY || weekday == Calendar.SATURDAY) {
 					offset++;
 					continue;
 				}
 				//calendar.add(Calendar.MINUTE, -numMins);				
 				lower = calendar.getTime().toString();
 				
 				calendar.setTimeInMillis(end);			
 				calendar.roll(Calendar.DAY_OF_YEAR, -offset);
 				if (weekday == Calendar.SUNDAY || weekday == Calendar.SATURDAY) {
 					offset++;
 					continue;
 				}
 				//calendar.add(Calendar.MINUTE, numMins);				
 				upper = calendar.getTime().toString();
 
 				if (i > 1)
 					query.append(" union all ");
 
 				query.append(dbScanSpec.getQueryString().replace("NUMOFDAYS", " '" +offset+" days'").replace("TIMEPREDICATE", timePredicate(upper, lower)));
 				i++;
 				offset++;
 				*/
 			}
 			//query.append(") order by panetime");
 			break;
 			
 		default:
 			System.err.println("unsupported similarity type: "+sSpec.getSimilarityType().toString());
 		
 		}
 		return dates;
 		//return newQuery(dates, start, end);
 		//return query.toString();
 	}
 
 	
 	/**
 	 * @param time
 	 * @param rainfall
 	 * @return the query string for retrieve date with similar weather
 	 */
 	private String similarWeather (long time, int rainfall) {
 		
 		int hour, dayOfWeek;
 		StringBuffer queryString = new StringBuffer ("select reporttime, abs(avg(rainfall)-"+rainfall+") from hydra " );
 		Calendar calendar = GregorianCalendar.getInstance();
 		calendar.setTimeInMillis(time);
 		
 		hour = calendar.get(Calendar.HOUR_OF_DAY);
 		dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
 		
 		// look at the past month
 		queryString.append(" where reporttime < TIMESTAMP '"+calendar.getTime().toString() +"'");
 		calendar.roll(Calendar.DAY_OF_YEAR, -LOOKBACK);
 		queryString.append(" and reporttime >= TIMESTAMP '"+calendar.getTime().toString() + "'");
 		
 		// look at the same hour
 		queryString.append(" and extract (HOUR from reporttime)="+hour);
 		
 		switch (sSpec.getSimilarityType()) {
 		case AllDays:
 			break;
 		case SameDayOfWeek:
 			queryString.append(" and extract(DOW from reporttime)="+dayOfWeek);
 			break;
 		case WeekDays:
 			queryString.append(" extract(DOW from reporttime) <> 0 and extract(DOW from reporttime <> 6)");
 			break;
 		default:
 			System.err.println("unsupported similarity type"); 
 		}
 		queryString.append(" group by reporttime having avg(rainfall) >=" + (rainfall - WEATHER_OFFSET) + " and avg(rainfall) <=" + (rainfall + WEATHER_OFFSET));
 		queryString.append(" order by abs(avg(rainfall) - " + rainfall + ")");
 		
 		if (DEBUG)
 			System.err.println(queryString.toString());
 		
 		return queryString.toString();
 	}
 	
 	/**
 	 * get result from the db query to retrieve date with similar weather  
 	 * 
 	 * @param rs
 	 * @param numAttrs
 	 * @throws SQLException
 	 * @throws ShutdownException
 	 * @throws InterruptedException
 	 */
 	private ArrayList extractSimilarDate (ArrayList similarDate, ResultSet rs) 
 	throws SQLException, ShutdownException, InterruptedException {
 		long time;
 		int count = 0;
 		
 		while (rs.next() && count < sSpec.getNumOfDays()) {
 			time = rs.getDate(TIME_COLUMN).getTime();
 			similarDate.add(time);
 			count++;
 		}
 		return similarDate;
 	}
 	
 	private int getRainfall (long time) {
 		return 0;
 	}
 	
 	public String timePredicate (String upper, String lower) {
     	
     	//StringBuffer query = new StringBuffer(" " + timeFunct  + ">= '" + queryRange[0] + "' and "+ timeFunct + "<= '" +queryRange[1]+"' ");
     	//query.append(" and dbdetectorid= "+queryRange[2]);
     	String pred = " "+dbScanSpec.getTimeAttr()  + ">= " + "TIMESTAMP '"+lower + "' and "+ dbScanSpec.getTimeAttr() + "< " + "TIMESTAMP '"+upper+ "'";
 
     	return pred;
     }
 
 
 	/**
 	 * @see niagara.utils.SerializableToXML#dumpChildrenInXML(StringBuffer)
 	 */
 	public void dumpChildrenInXML(StringBuffer sb) {
 		;
 	}
 	
     public void getInstrumentationValues(ArrayList<String> instrumentationNames, ArrayList<Object> instrumentationValues) {
 
     	synchronized (synch) {
     		if (stagelist == null)
 	        	return; 
 	
 	    	try {
 	    	if (!polled) { 
 	            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 	            DocumentBuilder db = dbf.newDocumentBuilder();
 	            DOMImplementation di = db.getDOMImplementation();
 	
 	            doc = di.createDocument("http://www.cs.pdx.edu/~jinli/lattedemo", "lattedemo", null);
 	
 	    		polled = true;
 	        } 
 	    	}catch (ParserConfigurationException e) {
 	        	System.err.println("DocumentBuilder cannot be created which satisfies the configuration requested");
 	        }
 	        
 			// first set up the stage
 
 			instrumentationNames.add("stagelist");
 			Element stagelistElt = doc.createElement("stagelist");
 			for (int i = 0; i < stagelist.size(); i++) {
 				Element stageElt = doc.createElement ("stage"); 
 				Stage curr = stagelist.get(i);
 				stageElt.setAttribute("starttime", String.valueOf(curr.starttime));
 				stageElt.setAttribute("endtime", String.valueOf(curr.endtime));
 	            StringBuffer datelist = new StringBuffer();
 	            for (int j = 0; j < curr.dates.size(); j++) {
 	                datelist.append (String.valueOf(curr.dates.get(j))+ " ");
 	            }
 	            stageElt.setAttribute("dates", datelist.toString());
 				//stageElt.setAttribute("dates", String.valueOf(stage.startdate));
 				//stageElt.setAttribute("enddate", String.valueOf(stage.enddate));
 				//instrumentationValues.add(stageElt);
 				
 		        // send actors
 		        //instrumentationNames.add("actors");
 		        
 		        //Element actorsElt = doc.createElement("actors");
 		        Element actElt;
 		        if (curr.exeunt != null) {
 		        	for (int j = 0; j < curr.exeunt.starts.size(); j++) {
 		        		actElt = doc.createElement("actor");
 		        		actElt.setAttribute("start", String.valueOf(curr.exeunt.starts.get(j)));
 		        		actElt.setAttribute("end", String.valueOf(curr.exeunt.ends.get(j)));
 		        		actElt.setAttribute("status", "done");
 		        		//actorsElt.appendChild(actElt);
 		        		stageElt.appendChild(actElt);
 		        	}
 		        }
 		        
 		    	for (int j = 0; j < curr.activeActors.starts.size(); j++) {
 		    		actElt = doc.createElement("actor");
 		    		actElt.setAttribute("start", String.valueOf(curr.activeActors.starts.get(j)));
 		    		actElt.setAttribute("end", String.valueOf(curr.activeActors.ends.get(j)));
		    		switch (curr.activeActors.status.get(i)) {
 		    		case PROGRESS:
 		    			actElt.setAttribute("status", "progress");
 		    			break;
 		    		
 		    		case FUTURE:
 		    			actElt.setAttribute("status", "future");
 		    			break;
 		    		
 		    		default:
 		    			System.err.println ("supported actor status");
 		    		}
 		    		//actorsElt.appendChild(actElt);
 		    		stageElt.appendChild(actElt);
 		    	}
 		    	//stageElt.appendChild(actorsElt);
 		    	stagelistElt.appendChild(stageElt);
 			}
 	    	instrumentationValues.add(stagelistElt);
     	}
 
     	//printElt ( (Element) instrumentationValues.get(0));
     	//printElt ( (Element) instrumentationValues.get(1));
     	
     	//super.getInstrumentationValues(instrumentationNames, instrumentationValues);
     }
     
     private void printElt (Node elt) {
     	NamedNodeMap attrs = elt.getAttributes();
     	
     	if ( attrs.getLength() != 0) {
     		for (int i = 0; i < attrs.getLength(); i++) {
     			System.out.println( "attr name: " + attrs.item(i).getNodeName() + " attri value: " + attrs.item(i).getNodeValue());
     		}
     		return;
     	}
     	
     	NodeList nodes = elt.getChildNodes();
     	
     	for (int i = 0; i < nodes.getLength(); i++) {
     		printElt(nodes.item(i));
     	}
     }
 }
