/* $Id: DBThread.java,v 1.17 2007/05/25 04:12:05 vpapad Exp $ */
 
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
 import niagara.logical.PrefetchSpec;
 
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
     
     private static final int ONE_DEMO_RUN = 4 * MIN_PER_HOUR * SECOND_PER_MIN * MILLISEC_PER_SEC;
     
     // the maximum prefetch allowed; in seconds;
     private static final long LOOK_AHEAD = 600; 
     
     // pane size of the db query; in seconds;
     private static final int PANE = 60;
     
     private static final double RATIO = 0.5;
        
     // weather similarity hack;
     private static final int MAX_HISTORY = 60;
     private static final int LOOKBACK = 30;
     private static final int WEATHER_OFFSET = 20; 
     private static final int TIME_COLUMN = 1;
 
	private final boolean DEBUG = false;
 	
 	// think these are optimization time??
 	public DBScanSpec dbScanSpec;
 	private Attribute[] variables;
 	private SimilaritySpec sSpec;
 	private PrefetchSpec pfSpec;
 	
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
 	
     // high watermark on the time predicate of db query;
     private long highWatermark = Long.MIN_VALUE;
 
 	// whether this is a first query got from punctQC? 
 	private int count = 0;
 	
 	// high watermark time monitored or stream and db;
 	private long streamTime=Long.MIN_VALUE, dbTime = Long.MIN_VALUE;
 	
 	private static long weather [][] = {
 		{1171980000, 18}, 
 		{1171983600, 8}, 
 		{1171987200, 0}, 
 		{1171990800, 0}, 
 		{1171994400, 0}};
 	
 	private class Query {
 		public Query (String query, long end) {
 			queryStr = query;
 			upperTS = end;
 		}
 		
 		String queryStr;
 		long upperTS;
 	}
 	
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
 		pfSpec = op.getPrefetchSpec();
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
 		dt.pfSpec = pfSpec;
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
 			
 			long roundtrip;
 			
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
 						
 					checkForSinkCtrlMsg(-1);
 					/*
 					 * if this is a continuous db scan
 					 */	
 					if (newQueries.size()==0) {
 						if (finish){
 							outputStream.endOfStream();
 							break;
 						} else {
 							//blocking call to get ctrl msg;
 							checkForSinkCtrlMsg(1*MILLISEC_PER_SEC);
 							
 							if (newQueries.size()==0)
 								continue;
 						}
 					}
 						
 					stmt = conn.createStatement();
 					if (DEBUG)
 						System.out.println("Attempting to execute query.");
 									
 					Query curQuery = (Query) newQueries.remove(0);
 					if (DEBUG)
 						System.err.println(curQuery.queryStr);
 					
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
 					
 					System.err.println("before execute: "+streamTime);
 					
 					roundtrip = streamTime;
 					
 					rs = stmt.executeQuery(curQuery.queryStr);
 					checkAllSinkCtrlMsg();
 					// Now do something with the ResultSet ....
 					int numAttrs = dbScanSpec.getNumAttrs();
 									
 					putResults(rs, numAttrs);
 					dbTime = curQuery.upperTS;
 					
 					roundtrip = streamTime - roundtrip;					
 
 					System.err.println("after execute: "+streamTime);
 					
 					System.err.println ("db time: "+dbTime);
 					
 					adjustPrefetch(roundtrip);
 					
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
 
 	/**
 	 * @param roundtrip
 	 */
 	private void adjustPrefetch(long roundtrip) 
 	throws java.lang.InterruptedException, ShutdownException, SQLException {
 		// if database data lags behind? (dbTime is smaller than streamTime)
 		
 		checkForSinkCtrlMsg(-1);
 		if (!intime ()) {
 			int size = pfSpec.getCoverage();
 			System.err.println("++++++++++++++++++roundtrip time: "+roundtrip);
 			System.err.println("db is beind");
 			// is database keeping up?
 			if (roundtrip > size) {
 				// increase db query size, which decreases db load
 				System.err.println("increase coverage: "+pfSpec.getCoverage()+ "by "+PANE);
 				pfSpec.setCoverage(size + PANE);
 			} else { 
 				// if database keeps up, and database data lags behind,
 				// we should increase prefetching 
 				int val = pfSpec.getPrefetchVal();
 				System.err.println("increase prefetch: "+pfSpec.getPrefetchVal()+ "by "+PANE);
 				pfSpec.setPrefetchVal(val + PANE);
 			}
 		} else if (farAhead ()) { // is database data far ahead of the stream? - this means we need to buffer too much
 			int size = pfSpec.getCoverage();
 			System.err.println("db is too far ahead");
 			if (roundtrip < size - PANE) {
 				System.err.println("decrease coverage: "+pfSpec.getCoverage()+ "by "+PANE);
 				pfSpec.setCoverage(size-PANE);
 			} else {
 				System.err.println("decrease prefetch: "+pfSpec.getPrefetchVal()+ "by "+PANE);
 				int val = pfSpec.getPrefetchVal();
 				pfSpec.setPrefetchVal(val-PANE);
 			}
 		}
 	}
 	
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
 	
     public void checkForSinkCtrlMsg(int timeout)
 	throws java.lang.InterruptedException, ShutdownException, SQLException {
 	// Loop over all sink streams, checking for control elements
     ArrayList ctrl;
 
     // Make sure the stream is not closed before checking is done
     if (!outputStream.isClosed()) {
     	ctrl = outputStream.getCtrlMsg(timeout);
     	//ctrl = outputStream.getCtrlMsg(1*MILLISEC_PER_SEC);
 	    
     	// If got a ctrl message, process it
     	if (ctrl != null) {
     		processCtrlMsgFromSink(ctrl);
     	}
     }
     }
     
     private void checkAllSinkCtrlMsg () 
     throws java.lang.InterruptedException, ShutdownException, SQLException{
 
         ArrayList ctrlMsgList = new ArrayList ();
 
         // Make sure the stream is not closed before checking is done
         if (!outputStream.isClosed()) {
             ArrayList ctrl;
         	do {
         	ctrl = outputStream.getCtrlMsg(-1);
     	    
         	// If got a ctrl message, process it
         	if (ctrl != null) {
         		ctrlMsgList.add(ctrl);
         	}
         	} while (ctrl != null);
         }
         for (Object ctrl : ctrlMsgList)
         	processCtrlMsgFromSink((ArrayList)ctrl);
     }
     
     public void processCtrlMsgFromSink(ArrayList ctrl) 
     	throws SQLException, ShutdownException, InterruptedException {
     	if (ctrl == null)
     		return;
     	
     	int ctrlFlag = (Integer)ctrl.get(0); 
     	String ctrlMsg = (String)ctrl.get(1);
     	switch (ctrlFlag) {
     	case CtrlFlags.CHANGE_QUERY:
     		String range = queryRange(ctrlMsg);
     		if (range != null)
     			newQueries.add(changeQuery(range));
     		streamTime = Long.valueOf(ctrlMsg);
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
     
     private String queryRange (String msg) {
     	
         long start, end, ts;
         
         ts = Long.valueOf (msg.trim());
         
         int prefetch = pfSpec.getPrefetchVal();
         
         if (highWatermark < ts+prefetch) {
             int queryCoverage = pfSpec.getCoverage();
 
             if (count == 0) {
             	start = ts;
             	//start = ts - sSpec.getNumOfMins()*60;
             	count++;
             } else
             	start = highWatermark;
 	        
             end = (start + queryCoverage);
 	        	
 	        String range = start + " " + end; //newQuery(start, end);
 	    
 	        System.err.println(range);
 	        highWatermark = end;
         	
         	return range;
     	}
         return null;
     }
     
     private Query changeQuery (String queryPara) 
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
     	
     	//System.err.println("********************hour: "+hour+"  epoch: "+epoch+"***********************");
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
     private Query newQuery (ArrayList <Long> date, long start, long end) {
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
 		return new Query (query.toString(), end/1000);
 
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
 			}
 
 			break;
 			
 		case SameDayOfWeek:
 			for (int i = 1; i <= numDays; i++) {				
 				calendar.setTimeInMillis(start);											
 				calendar.roll(Calendar.DAY_OF_YEAR, -i*DAYS_PER_WEEK);				
 
 				dates.add(calendar.getTimeInMillis());
 			}
 
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
 
 			}
 
 			break;
 			
 		default:
 			System.err.println("unsupported similarity type: "+sSpec.getSimilarityType().toString());
 		
 		}
 		return dates;
 	}
 
 	
 	/**
 	 * @param time
 	 * @param rainfall
 	 * @return the query string for retrieve date with similar weather
 	 */
 	private String similarWeather (long time, long rainfall) {
 		
 		int hour, dayOfWeek;
 		
 		String rain;
 		if (rainfall > 0)
 			rain = ">";
 		else
 			rain = "=";
 		
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
 		//queryString.append(" group by reporttime having avg(rainfall) >=" + (rainfall - WEATHER_OFFSET) + " and avg(rainfall) <=" + (rainfall + WEATHER_OFFSET));
 		queryString.append(" group by reporttime having avg(rainfall) " + rain + " 0");
 		queryString.append(" order by abs(avg(rainfall) - " + rainfall + ")");
 		
 		if (DEBUG)
 			System.err.println(queryString.toString());
 		
 		return queryString.toString();
 	}
 
 	/*private String similarWeather (long time, long rainfall) {
 		
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
 	}*/
 	
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
 	
 	private long getRainfall (long time) {
 		for (int i = 0; i < weather.length; i++) {
 			if (weather[i][0] <= time && time < weather[i][0] + MIN_PER_HOUR*SECOND_PER_MIN)
 				return weather[i][1];
 		}
 		
 		return 0;
 	}
 	
 	public String timePredicate (String upper, String lower) {
     	
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
 			stagelistElt.setAttribute("now", String.valueOf(streamTime));
 			for (int i = 0; i < stagelist.size(); i++) {
 				Element stageElt = doc.createElement ("stage"); 
 				Stage curr = stagelist.get(i);
 	            StringBuffer datelist = new StringBuffer();
 	            for (int j = 0; j < curr.dates.size(); j++) {
 	                datelist.append (String.valueOf(curr.dates.get(j))+ " ");
 	            }
 	            stageElt.setAttribute("dates", datelist.toString());
 
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
 		        
 		        boolean flag = false;
 		    	for (int j = 0; j < curr.activeActors.starts.size(); j++) {
 		    		actElt = doc.createElement("actor");
 		    		actElt.setAttribute("start", String.valueOf(curr.activeActors.starts.get(j)));
 		    		actElt.setAttribute("end", String.valueOf(curr.activeActors.ends.get(j)));
 		    		switch (curr.activeActors.status.get(j)) {
 		    		case PROGRESS:
 		    			actElt.setAttribute("status", "progress");
 		    			assert !flag: "progress stuff!";
 		    			flag = true;
 		    			break;
 		    		
 		    		case FUTURE:
 		    			actElt.setAttribute("status", "future");
 		    			break;
 		    		
 		    		default:
 		    			System.err.println ("supported actor status");
 		    		}
 		    		stageElt.appendChild(actElt);
 		    	}
 		    	stagelistElt.appendChild(stageElt);
 			}
 	    	instrumentationValues.add(stagelistElt);
     	}
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
     
     private boolean intime () {
     	// if streamTime or dbTime is not initialized, just return true,
     	// because we aren't ready to make any decision yet.
     	if (streamTime == Long.MIN_VALUE || dbTime == Long.MIN_VALUE) {
     		return true;
     	}
     	
     	return dbTime > streamTime;
     }
     
     private boolean farAhead () {
     	// if streamTime or dbTime is not initialized, just return true,
     	// because we aren't ready to make any decision yet.
     	if (streamTime == Long.MIN_VALUE || dbTime == Long.MIN_VALUE) {
     		return false;
     	}
     	
     	return dbTime > streamTime + LOOK_AHEAD;
 
     }
 }
