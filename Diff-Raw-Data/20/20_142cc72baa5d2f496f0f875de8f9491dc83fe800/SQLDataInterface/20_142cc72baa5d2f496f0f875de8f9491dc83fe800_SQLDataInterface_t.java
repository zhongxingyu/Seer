 /*
  * This software is licensed under the GPLv3 license, included as
  * ./GPLv3-LICENSE.txt in the source distribution.
  *
  * Portions created by Brett Wilson are Copyright 2008 Brett Wilson.
  * All rights reserved.
  */
 
 package org.wwscc.storage;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author bwilson
  */
 public abstract class SQLDataInterface extends DataInterface
 {
 	private static Logger log = Logger.getLogger("org.wwscc.storage.SqliteDatabase");
 
 	public static class ResultRow extends HashMap<String,Object>
 	{
 		public boolean getBoolean(String key)
 		{
 			Object o = get(key);
 			if (o instanceof Boolean)
 				return (Boolean)o;
 			else if (o instanceof Integer)
 				return ((Integer)o) != 0;
 			else if (o instanceof String)
 			{
 				String b = (String)o;
 				return (b.equalsIgnoreCase("true") || b.equals("1"));
 			}
 			return false;
 		}
 
 		public int getInt(String key)
 		{
 			try
 			{
 				Object o = get(key);
 				if (o instanceof Integer)
 					return (Integer)o;
 				else if (o instanceof String)
 					return Integer.parseInt((String)o);
 			}
 			catch (NumberFormatException nfe)
 			{
 			}
 			return -1;
 		}
 
 		public long getLong(String key)
 		{
 			try
 			{
 				Object o = get(key);
 				if (o instanceof Long)
 					return (Long)o;
 				else if (o instanceof String)
 					return Long.parseLong((String)o);
 			}
 			catch (NumberFormatException nfe)
 			{
 			}
 			return -1;
 		}
 
 		public double getDouble(String key)
 		{
 			try
 			{
 				Object o = get(key);
 				if (o instanceof Double)
 					return (Double)o;
 				else if (o instanceof String)  // Function outputs come back as string
 					return Double.parseDouble((String)o);
 			}
 			catch (NumberFormatException nfe)
 			{
 			}
 			return -1;
 		}
 
 		public byte[] getBlob(String key)
 		{
 			Object o = get(key);
 			if (o instanceof byte[])
 				return (byte[])o;
 			else if (o instanceof String)
 				return ((String)o).getBytes();  // TODO, check decode from web data source?
 			return null;
 		}
 
 		public String getString(String key)
 		{
 			Object o = get(key);
 			if (o == null)
 				return "";
 			return get(key).toString();
 		}
 
 		public SADateTime getSADateTime(String key)
 		{
 			return new SADateTime(getString(key));
 		}
 		public SADateTime.SADate getSADate(String key)
 		{
 			return new SADateTime.SADate(getString(key));
 		}
 		public SADateTime.SATime getSATime(String key)
 		{
 			return new SADateTime.SATime(getString(key));
 		}
 	}
 
 	public static class ResultData extends ArrayList<ResultRow>
 	{
 	}
 
 	public abstract void start() throws IOException;
 	public abstract void commit() throws IOException;
 	public abstract void rollback();
 	public abstract int lastInsertId() throws IOException;
 	public abstract void executeUpdate(String sql, List<Object> args) throws IOException;
 	public abstract void executeGroupUpdate(String sql, List<List<Object>> args) throws IOException;
 	public abstract ResultData executeSelect(String sql, List<Object> args) throws IOException;
 
 	/**
 	 * Utility function to create a list for passing args
 	 * @param args list of objects to add to initially
 	 * @return the new List
 	 */
 	List<Object> newList(Object... args)
 	{
 		List<Object> l = new ArrayList<Object>();
 		for (Object o : args)
 			l.add(o);
 		return l;
 	}
 
 	int oppositeCourse()
 	{
 		if (currentCourse == 1) return 2;
 		return 1;
 	}
 
 	void logError(String f, Exception e)
 	{
		log.log(Level.SEVERE, f + " failed: " + e.getMessage() + "\nRefresh screen and try again", e);
 	}
 
 
 	byte[] serialize(Serializable o) throws IOException
 	{
 		ByteArrayOutputStream out = new ByteArrayOutputStream();
 		ObjectOutputStream writer = new ObjectOutputStream(out);
 		writer.writeObject(o);
 		return out.toByteArray();
 	}
 
 	Object unserialize(byte[] b) throws IOException, ClassNotFoundException
 	{
 		ByteArrayInputStream out = new ByteArrayInputStream(b);
 		ObjectInputStream reader = new ObjectInputStream(out);
 		Object o = reader.readObject();
 		return o;
 	}
 
 	@Override
 	public void clearChanges()
 	{
 		try {
 			executeUpdate("TRACKCLEAR", null);
 		} catch (Exception ioe) {
 			logError("getChanges", ioe);
 		}
 	}
 
 	public void trackChange(String type, Serializable o)
 	{
 		if (!trackRegChangesFlag)
 			return;
 		
 		try{
 			log.info("Track " + type + ", " + o);
 			executeUpdate("TRACK", newList(type, serialize(o)));
 		} catch (IOException ioe) {
 			logError("trackChange", ioe);
 		}
 	}
 
 	@Override
 	public List<Change> getChanges()
 	{
 		try
 		{
 			ResultData data = executeSelect("GETCHANGES", null);
 			List<Change> ret = new ArrayList<Change>();
 			for (ResultRow r : data)
 			{
 				Change c = new Change();
 				c.id = r.getInt("id");
 				c.type = r.getString("type");
 				c.arg = unserialize(r.getBlob("args"));
 				ret.add(c);
 			}
 			return ret;
 		}
 		catch (Exception ioe)
 		{
 			logError("getChanges", ioe);
 			return null;
 		}
 	}
 
 	
 	@Override
 	public void setCurrentEvent(Event e)
 	{
 		super.setCurrentEvent(e);
 		trackChange("SETEVENT", e);
 	}
 
 
 	@Override
 	public List<Event> getEvents()
 	{
 		try
 		{
 			ResultData data = executeSelect("GETEVENTS", null);
 			List<Event> ret = new ArrayList<Event>();
 			for (ResultRow r : data)
 			{
 				Event e = AUTO.loadEvent(r);
 				ret.add(e);
 			}
 			return ret;
 		}
 		catch (Exception ioe)
 		{
 			logError("getEvents", ioe);
 			return null;
 		}
 	}
 
 	@Override
 	public boolean updateEvent()
 	{
 		try
 		{
 			List<Object> vals = newList();
 			AUTO.addEventValues(currentEvent, vals);
 			vals.add(currentEvent.id);
 			executeUpdate("UPDATEEVENTS", vals);
 			return true;
 		}
 		catch (Exception ioe)
 		{
 			logError("updateEvent", ioe);
 			return false;
 		}
 	}
 
 	/**
 	 * Utility function for methods that loads entrants from driver/car
 	 * data as well as placing runs if they match.
 	 * @param d result data containing entrant info
 	 * @param r result data containing run info or null
 	 * @return a list of entrants
 	 * @throws java.io.IOException
 	 */
 	List<Entrant> loadEntrants(ResultData d, ResultData r) throws IOException
 	{
 		List<Entrant> ret = new ArrayList<Entrant>();
 		List<Run> runs = null;
 
 		if (r != null)
 		{
 			runs = new ArrayList<Run>();
 			for (ResultRow rrow : r)
 				runs.add(AUTO.loadRun(rrow));
 		}
 
 		for (ResultRow erow : d)
 		{
 			Entrant e = new Entrant();
 			e.car = AUTO.loadCar(erow);
 			e.firstname = erow.getString("firstname");
 			e.lastname = erow.getString("lastname");
 			e.index = getEffectiveIndex(e.car.classcode, e.car.indexcode);
 
 			if (runs != null)
 			{
 				for (Run rx : runs)
 				{
 					if (rx.getCarId() == e.getCarId())
 						e.runs.put(rx.run, rx);
 				}
 			}
 			
 			ret.add(e);
 		}
 
 		return ret;
 	}
 
 	
 	@Override
 	public List<Entrant> getEntrantsByEvent()
 	{
 		try
 		{
 			return loadEntrants(executeSelect("GETEVENTENTRANTS", newList(currentEvent.id)), null);
 		}
 		catch (Exception ioe)
 		{
 			logError("getEntrantsByEvent", ioe);
 			return null;
 		}
 	}
 
 
 	@Override
 	public List<Entrant> getRegisteredEntrants()
 	{
 		try
 		{
 			return loadEntrants(executeSelect("GETREGISTEREDENTRANTS", newList(currentEvent.id)), null);
 		}
 		catch (Exception ioe)
 		{
 			logError("getRegisteredEntrants", ioe);
 			return null;
 		}
 	}
 
 	/**
 	 * Gets all the entrants and their runs based on the current run order.  Ends up
 	 * being a lot faster (particular over a network) to load all of the runs for the run
 	 * group as one and then filter them to each entrant locally.
 	 * @return
 	 */
 	@Override
 	public List<Entrant> getEntrantsByRunOrder()
 	{
 		try
 		{
 			ResultData d = executeSelect("GETRUNORDERENTRANTS", newList(currentEvent.id, currentCourse, currentRunGroup));
 			if (d == null)
 				return new ArrayList<Entrant>();
 			ResultData runs = executeSelect("GETRUNSBYGROUP", newList(currentEvent.id, currentCourse, currentEvent.id, currentCourse, currentRunGroup));
 			return loadEntrants(d, runs);
 		}
 		catch (Exception ioe)
 		{
 			logError("getEntrantsByRunOrder", ioe);
 			return null;
 		}
 	}
 
 
 	protected Entrant loadEntrant(int course, int carid, boolean loadruns)
 	{
 		try
 		{
 			ResultData d = executeSelect("LOADENTRANT", newList(carid));
 			ResultData runs = null;
 			if (loadruns)
 				runs = executeSelect("GETRUNSBYCARID", newList(carid, currentEvent.id, course));
 			List<Entrant> e = loadEntrants(d, runs);
 			return e.get(0);
 		}
 		catch (Exception ioe)
 		{
 			logError("loadEntrant", ioe);
 			return null;
 		}
 	}
 
 	@Override
 	public Entrant loadEntrant(int carid, boolean loadruns)
 	{
 		return loadEntrant(currentCourse, carid, loadruns);
 	}
 
 	@Override
 	public Entrant loadEntrantOpposite(int carid, boolean loadruns)
 	{
 		return loadEntrant(oppositeCourse(), carid, loadruns);
 	}
 	
 
 	@Override
 	public Set<Integer> getCarIdsForCourse()
 	{
 		try
 		{
 			ResultData d = executeSelect("GETCARIDSFORCOURSE", newList(currentEvent.id, currentCourse));
 			HashSet<Integer> ret = new HashSet<Integer>();
 			for (ResultRow r : d)
 				ret.add(r.getInt("carid"));
 			return ret;
 		}
 		catch (Exception ioe)
 		{
 			logError("getCarIdsForCourse", ioe);
 			return null;
 		}
 	}
 
 	@Override
 	public List<Integer> getCarIdsForRunGroup()
 	{
 		try
 		{
 			ResultData d = executeSelect("GETCARIDSFORGROUP", newList(currentEvent.id, currentCourse, currentRunGroup));
 			List<Integer> ret = new ArrayList<Integer>();
 			for (ResultRow r : d)
 				ret.add(r.getInt("carid"));
 			return ret;
 		}
 		catch (Exception ioe)
 		{
 			logError("getCarIdsForRunGroup", ioe);
 			return null;
 		}
 	}
 
 	@Override
 	public void setRunOrder(List<Integer> carids) 
 	{
 		try
 		{
 			if (currentRunGroup <= 0) return; // Shouldn't be doing this if rungroup isn't valid
 
 			/* Start transaction */
 			start();
 
 			/* Delete the old */
 			List<Object> vals = newList(currentEvent.id, currentCourse, currentRunGroup);
 			executeUpdate("DELETERUNORDER", vals);
 
 			/* Reinsert all */
 			vals.clear();
 
 			List<List<Object>> lists = new ArrayList<List<Object>>(carids.size());
 
 			int ii = 0;
 			for (Integer carid : carids)
 			{
 				List<Object> items = new ArrayList<Object>(5);
 				items.add(currentEvent.id);
 				items.add(currentCourse);
 				items.add(currentRunGroup);
 				items.add(carid);
 				items.add(++ii);
 				lists.add(items);
 			}
 
 			executeGroupUpdate("INSERTRUNORDER", lists);
 			commit();
 		}
 		catch (Exception ioe)
 		{
 			rollback();
 			logError("setRunOrder", ioe);
 		}
 	}
 
 	//****************************************************/
 
 	protected List<Integer> loadRunOrder() throws IOException
 	{
 		List<Integer> ret = new ArrayList<Integer>();
 		ResultData d = executeSelect("LOADRUNORDER", newList(currentEvent.id, currentCourse, currentRunGroup));
 		for (ResultRow r : d)
 			ret.add(r.getInt("carid"));
 		return ret;
 	}
 
 
 	@Override
 	public void addToRunOrderOpposite(int carid)  // append carid to run order on opposite course
 	{
 		int save = currentCourse;
 		try
 		{
 			currentCourse = oppositeCourse();
 			List<Integer> list = loadRunOrder();
 			list.add(carid);
 			setRunOrder(list);
 		}
 		catch (Exception ioe)
 		{
 			logError("addToRunOrderOpposite", ioe);
 		}
 		currentCourse = save;
 	}
 
 
 	@Override
 	public void removeFromRunOrderOpposite(int carid) // remove carid from opposite course run order
 	{
 		int save = currentCourse;
 		try
 		{
 			currentCourse = oppositeCourse();
 			List<Integer> list = loadRunOrder();
 			list.remove(new Integer(carid));
 			setRunOrder(list);
 		}
 		catch (Exception ioe)
 		{
 			logError("removeFromRunOrderOpposite", ioe);
 		}
 		currentCourse = save;
 	}
 
 /*
 	protected void swapEntrant(int oldcarid, int newcarid, int course) // swap entrants
 	{
 		try
 		{
 			start();
 			registerCar(newcarid);
 			List<Object> vals = newList(newcarid, currentEvent.id, course, currentRunGroup, oldcarid);
 			executeUpdate("SWAPRUNORDER", vals);
 			vals.remove(3);
 			executeUpdate("SWAPRUNS", vals);
 			commit();
 		}
 		catch (Exception ioe)
 		{
 			rollback();
 			logError("swapEntrant", ioe);
 		}
 
 	}
 
 	@Override
 	public void swapEntrant(int oldcarid, int newcarid) // swap entrants
 	{
 		swapEntrant(oldcarid, newcarid, currentCourse);
 	}
 
 	@Override
 	public void swapEntrantOpposite(int oldcarid, int newcarid) // swap entrants on opposite course
 	{
 		swapEntrant(oldcarid, newcarid, oppositeCourse());
 	}
 */
 
 	
 	protected boolean hasRuns(int carid, int course)
 	{
 		try
 		{
 			List<Object> vals = newList(carid, currentEvent.id, course);
 			ResultData d = executeSelect("GETRUNCOUNT", vals);
 			return (d.get(0).getInt("count") > 0);
 		}
 		catch (Exception ioe)
 		{
 			logError("hasRuns", ioe);
 			return false;
 		}
 	}
 
 	@Override
 	public boolean hasRuns(int carid)  // return true if car has runs recorded
 	{
 		return hasRuns(carid, currentCourse);
 	}
 
 	@Override
 	public boolean hasRunsOpposite(int carid) // return true if car has runs recorded opposite
 	{
 		return hasRuns(carid, oppositeCourse());
 	}
 
 	@Override
 	public Map<String, Integer> getClass2RunGroupMapping() // return the class codes assigned to the current event
 	{
 		try
 		{
 			ResultData d = executeSelect("GETCLASSGROUPMAPPING", newList(currentEvent.id));
 			Map<String, Integer> ret = new HashMap<String, Integer>();
 			for (ResultRow r : d)
 				ret.put(r.getString("classcode"), r.getInt("rungroup"));
 			return ret;
 		}
 		catch (Exception ioe)
 		{
 			logError("getClass2RunGroupMapping", ioe);
 			return null;
 		}
 	}
 
 	@Override
 	public void setClass2RunGroupMapping(Map<String,Integer> list)  // set the classes/run group match ups for the current event
 	{
 		try
 		{
 			start();
 			executeUpdate("DELETECLASSGROUPMAPPING", newList(currentEvent.id));
 			List<Object> vals = newList(currentEvent.id, 0, 0);
 			for (String code : list.keySet())
 			{
 				vals.set(1, code);
 				vals.set(2, list.get(code));
 				executeUpdate("INSERTCLASSGROUPMAPPING", vals);
 			}
 			commit();
 		}
 		catch (Exception ioe)
 		{
 			rollback();
 			logError("setClass2RunGroupMapping", ioe);
 		}
 	}
 
 
 	@Override
 	public void newDriver(Driver d) throws IOException
 	{
 		List<Object> vals = newList();
 		AUTO.addDriverValues(d, vals);
 		executeUpdate("INSERTDRIVER", vals);
 		d.id = lastInsertId();
 		trackChange("INSERTDRIVER", d);
 	}
 
 	@Override
 	public void updateDriver(Driver d) throws IOException
 	{
 		List<Object> vals = new ArrayList<Object>();
 		AUTO.addDriverValues(d, vals);
 		vals.add(d.id);
 		executeUpdate("UPDATEDRIVER", vals);
 		trackChange("UPDATEDRIVER", d);
 	}
 
 	@Override
 	public void deleteDriver(Driver d) throws IOException
 	{
 		executeUpdate("DELETEDRIVER", newList(d.id));
 	}
 
 	@Override
 	public void deleteDrivers(Collection<Driver> list) throws IOException
 	{
 		try
 		{
 			start();
 			for (Driver d : list)
 				executeUpdate("DELETEDRIVER", newList(d.id));
 			commit();
 		}
 		catch (IOException ioe)
 		{
 			rollback();
 			throw ioe;
 		}
 	}
 
 	@Override
 	public List<Car> getCarsForDriver(int driverid)
 	{
 		try
 		{
 			ResultData d = executeSelect("LOADDRIVERCARS", newList(driverid));
 			List<Car> ret = new ArrayList<Car>();
 			for (ResultRow r : d)
 				ret.add(AUTO.loadCar(r));
 			return ret;
 		}
 		catch (Exception ioe)
 		{
 			logError("getCarsForDriver", ioe);
 			return null;
 		}
 	}
 
 	/** Subclasses override to deal with odd circumstances of getattr sql */
 	protected abstract ResultData getCarAttributesImpl(String attr) throws IOException;
 
 	@Override
 	public List<String> getCarAttributes(String attr) 
 	{
 		try
 		{
 			ResultData d = getCarAttributesImpl(attr);
 			List<String> ret = new ArrayList<String>();
 			for (ResultRow r : d)
 				ret.add(r.getString(attr));
 			return ret;
 		}
 		catch (Exception ioe)
 		{
 			logError("getCarAttributes", ioe);
 			return null;
 		}
 	}
 
 	@Override
 	public void registerCar(int carid) throws IOException
 	{
 		List<Object> vals = newList(currentEvent.id, carid);
 		executeUpdate("REGISTERCAR", vals);
 		trackChange("REGISTERCAR", carid);
 	}
 
 	@Override
 	public void unregisterCar(int carid) throws IOException
 	{
 		List<Object> vals = newList(currentEvent.id, carid);
 		executeUpdate("UNREGISTERCAR", vals);
 		trackChange("UNREGISTERCAR", carid);
 	}
 
 
 	@Override
 	public void newCar(Car c) throws IOException
 	{
 		List<Object> vals = newList();
 		AUTO.addCarValues(c, vals);
 		executeUpdate("INSERTCAR", vals);
 		c.id = lastInsertId();
 		trackChange("INSERTCAR", c);
 	}
 
 
 	@Override
 	public void updateCar(Car c) throws IOException
 	{
 		List<Object> vals = new ArrayList<Object>();
 		AUTO.addCarValues(c, vals);
 		vals.add(c.id);
 		executeUpdate("UPDATECAR", vals);
 		trackChange("UPDATECAR", c);
 	}
 
 	@Override
 	public void deleteCar(Car c) throws IOException
 	{
 		executeUpdate("DELETECAR", newList(c.id));
 	}
 
 	@Override
 	public void deleteCars(Collection<Car> list) throws IOException
 	{
 		try
 		{
 			start();
 			for (Car c : list)
 				executeUpdate("DELETECAR", newList(c.id));
 			commit();
 		}
 		catch (IOException ioe)
 		{
 			rollback();
 			throw ioe;
 		}
 	}
 
 	@Override
 	public boolean isRegistered(Car c)
 	{
 		try
 		{
 			ResultData cr = executeSelect("ISREGISTERED", newList(c.id, currentEvent.id));
 			return (cr.size() > 0);
 		}
 		catch (Exception ioe)
 		{
 			logError("isRegistered", ioe);
 			return false;
 		}
 	}
 	
 
 	@Override
 	public void insertRun(Run r)
 	{
 		try
 		{
 			List<Object> list = newList();
 			AUTO.addRunValues(r, list);
 			executeUpdate("INSERTRUN", list);
 			r.id = lastInsertId();
 		}
 		catch (Exception ioe)
 		{
 			logError("insertRun", ioe);
 		}
 	}
 
 
 	@Override
 	public void updateRun(Run r)
 	{
 		try
 		{
 			List<Object> vals = new ArrayList<Object>();
 			AUTO.addRunValues(r, vals);
 			vals.add(r.id);
 			executeUpdate("UPDATERUN", vals);
 		}
 		catch (Exception ioe)
 		{
 			logError("updateRun", ioe);
 		}
 	}
 
 	
 	@Override
 	public boolean setEntrantRuns(Car c, Collection<Run> runs)
 	{
 		try
 		{
 			for (Run r : runs)
 				if ((r.eventid != currentEvent.id) || (r.course != currentCourse))
 					throw new IllegalArgumentException(String.format("Invalid run id portion (%s,%s)", r.eventid, r.course));
 
 			start();
 			executeUpdate("DELETERUNSBYCOURSE", newList(c.id, currentCourse, currentEvent.id));
 			
 			List<List<Object>> lists = new ArrayList<List<Object>>();
 			for (Run r : runs)
 			{
 				List<Object> args = newList();
 				AUTO.addRunValues(r, args);
 				lists.add(args);
 			}
 
 			if (lists.size() > 0)
 				executeGroupUpdate("INSERTRUN", lists);
 
 			updateClassResults(c.classcode, c.id);
 			commit();
 			return true;
 		}
 		catch (Exception ioe)
 		{
 			logError("setEntrantRuns", ioe);
 			rollback();
 			return false;
 		}
 	}
 
 	protected void updateClassResults(String classcode, int carid) throws IOException
 	{
 		log.fine("Update event for class " + classcode);
 
 		/* Get a map of the current result update times */
 		List<Object> cevals = newList(classcode, currentEvent.id);
 		ResultData cr = executeSelect("GETUPDATED", cevals);
 
 		HashMap<Integer,SADateTime> updateMap = new HashMap<Integer,SADateTime>();
 		for (ResultRow r : cr)
 			updateMap.put(r.getInt("carid"), r.getSADateTime("updated"));
 		if (carid > 0)
 			updateMap.put(carid, new SADateTime());
 
 		/* Delete current event results for the same event/class, then query runs table for new results */
 		executeUpdate("DELETECLASSRESULTS", cevals);
 		ResultData results = executeSelect("GETCLASSRESULTS", cevals);
 
 		/* Now we will loop from 1st to last, calculating points and inserting new event results */
 		int position = 1;
 		boolean first = true;
 		double basis = 1;
 		double prev = 1;
 		int basecnt = 1;
 
 		ResultData setting = executeSelect("GETSETTING", newList("ppoints"));
 		ArrayList<Double> PPOINTS = new ArrayList<Double>();
 		for (String s : setting.get(0).getString("val").split(","))
 		{
 			try {
 				PPOINTS.add(Double.valueOf(s));
 			} catch (NumberFormatException nfe) {
 				log.warning("Failed to read ppoints from settings properly: " + nfe);
 			}
 		}
 
 
 		List<List<Object>> lists = new ArrayList<List<Object>>();
 		for (ResultRow r : results)
 		{
 			double sum = r.getDouble("sum");
 			int cnt = r.getInt("coursecnt");
 			if (first)
 			{
 				basis = sum;
 				prev = sum;
 				basecnt = cnt;
 				first = false;
 			}
 
 			List<Object> rvals = newList();
 			int insertcarid = r.getInt("carid");
 			rvals.add(currentEvent.id);
 			rvals.add(insertcarid);
 			rvals.add(classcode); // classcode doesn't change
 			rvals.add(position);
 			rvals.add(cnt);
 			rvals.add(sum);
 
 			if (cnt == basecnt)
 			{
 				rvals.add(sum-prev);
 				rvals.add(basis/sum*100);
 				if (position <= PPOINTS.size())
 					rvals.add(PPOINTS.get(position-1));
 				else
 					rvals.add(PPOINTS.get(PPOINTS.size()-1));
 			}
 			else
 			{ // This person ran less courses than the other people
 				rvals.add(999.999);
 				rvals.add(0.0);
 				rvals.add(0);
 			}
 
 			SADateTime update = updateMap.get(insertcarid);
 			rvals.add((update != null)? update: new SADateTime());
 			lists.add(rvals);
 			position++;
 			prev = sum;
 		}
 
 		executeGroupUpdate("INSERTCLASSRESULTS", lists);
 	}
 
 	@Override
 	public List<EventResult> getResultsForClass(String classcode) 
 	{
 		try
 		{
 			ResultData d = executeSelect("GETEVENTRESULTSBYCLASS", newList(classcode, currentEvent.id));
 			List<EventResult> ret = new ArrayList<EventResult>(d.size());
 			for (ResultRow r : d)
 				ret.add(AUTO.loadEventResult(r));
 			return ret;
 		}
 		catch (Exception ioe)
 		{
 			logError("getResultsForClass", ioe);
 			return null;
 		}
 	}
 
 	@Override
 	public Set<Integer> getCarIdsByChallenge(int challengeid)
 	{
 		try
 		{
 			
 			ResultData d = executeSelect("GETCARIDSBYCHALLENGE", newList(challengeid));
 			HashSet<Integer> ret = new HashSet<Integer>();
 			for (ResultRow r : d)
 			{
 				ret.add(r.getInt("car1id"));
 				ret.add(r.getInt("car2id"));
 			}
 			return ret;
 		}
 		catch (Exception ioe)
 		{
 			logError("getCarIdsByChallenge", ioe);
 			return null;
 		}
 	}
 
 	@Override
 	public void newChallenge(String name, int size, boolean bonus)
 	{
 		try
 		{
 			int rounds = size - 1;
 			int depth = (int)(Math.log(size)/Math.log(2));
 			start();
 
 			executeUpdate("INSERTCHALLENGE", newList(currentEvent.id, name, depth, bonus));
 			currentChallengeId = lastInsertId();
 
 			List<Object> rargs = newList(currentChallengeId, 0, false, -1, -1);
 			for (int ii = 0; ii <= rounds; ii++)
 			{
 				rargs.set(1, ii);
 				executeUpdate("INSERTBLANKCHALLENGEROUND", rargs);
 			}
 			rargs.set(1, 99);
 			executeUpdate("INSERTBLANKCHALLENGEROUND", rargs);
 
 			commit();
 		}
 		catch (Exception ioe)
 		{
 			logError("newChallenge", ioe);
 			rollback();
 		}
 	}
 
 	@Override
 	public List<Challenge> getChallengesForEvent()
 	{
 		try
 		{
 			ResultData d = executeSelect("GETCHALLENGESFOREVENT", newList(currentEvent.id));
 			List<Challenge> ret = new ArrayList<Challenge>();
 			for (ResultRow r : d)
 				ret.add(AUTO.loadChallenge(r));
 			return ret;
 		}
 		catch (Exception ioe)
 		{
 			logError("getChallengesForEvent", ioe);
 			return null;
 		}
 	}
 
 	@Override
 	public List<ChallengeRound> getRoundsForChallenge(int challengeid) 
 	{
 		try
 		{
 			ResultData d = executeSelect("GETROUNDSFORCHALLENGE", newList(challengeid));
 			List<ChallengeRound> ret = new ArrayList<ChallengeRound>();
 			for (ResultRow r : d)
 			{
 				/* Not a standard class to table layout so write the load manually */
 				ChallengeRound rnd = new ChallengeRound();
 				rnd.id = r.getInt("id");
 				rnd.challengeid = r.getInt("challengeid");
 				rnd.round = r.getInt("round");
 				rnd.car1 = new ChallengeRound.RoundEntrant();
 				rnd.car1.carid = r.getInt("car1id");
 				rnd.car1.dial = r.getDouble("car1dial");
 				rnd.car1.result = r.getDouble("car1result");
 				rnd.car1.newdial = r.getDouble("car1newdial");
 				rnd.car2 = new ChallengeRound.RoundEntrant();
 				rnd.car2.carid = r.getInt("car2id");
 				rnd.car2.dial = r.getDouble("car2dial");
 				rnd.car2.result = r.getDouble("car2result");
 				rnd.car2.newdial = r.getDouble("car2newdial");
 				ret.add(rnd);
 			}
 			return ret;
 		}
 		catch (Exception ioe)
 		{
 			logError("getRoundsForChallenge", ioe);
 			return null;
 		}
 	}
 
 	@Override
 	public List<ChallengeRun> getRunsForChallenge(int challengeid)
 	{
 		try
 		{
 			ResultData d = executeSelect("GETRUNSFORCHALLENGE", newList(challengeid));
 			List<ChallengeRun> ret = new ArrayList<ChallengeRun>();
 			for (ResultRow r : d)
 			{
 				ChallengeRun cr = AUTO.loadChallengeRun(r); cr.fixids();
 				ret.add(cr);
 			}
 			return ret;
 		}
 		catch (Exception ioe)
 		{
 			logError("getRunsForChallenge", ioe);
 			return null;
 		}
 	}
 
 	class Leader {  // I miss python
 		int carid; double basis; double net;
 		Leader(int i, double b, double n) { carid = i; basis = b; net = n; }
 	}
 	
 	@Override
 	public Dialins loadDialins() 
 	{
 		try
 		{
 			HashMap <String, Leader> leaders = new HashMap<String, Leader>();
 			ResultData d = executeSelect("GETDIALINS", newList(currentEvent.id, currentEvent.id));
 			Dialins ret = new Dialins();
 			for (ResultRow r : d)
 			{
 				String classcode = r.getString("classcode");
 				String indexcode = r.getString("indexcode");
 				int position = r.getInt("position");
 				int carid = r.getInt("carid");
 				double myraw = r.getDouble("myraw");
 				double mynet = r.getDouble("mynet");
 				double index = getEffectiveIndex(classcode, indexcode);
 
 				if (position == 1)
 					leaders.put(classcode, new Leader(carid, myraw * index, mynet));
 
 				if (!leaders.containsKey(classcode))
 				{
 					log.info("No leader for " + classcode + "?");
 					continue;
 				}
 
 				// Bonus dial is based on my best raw times
 				ret.bonusDial.put(carid, myraw/2.0);
 				// Class dial is based on the class leaders best time, need to apply indexing though
 				ret.classDial.put(carid, leaders.get(classcode).basis / index / 2.0);
 				ret.netTime.put(carid, mynet);
 				ret.classDiff.put(carid, mynet - leaders.get(classcode).net);
 
 				if (position == 2)
 				{
 					Leader lead = leaders.get(classcode);
 					ret.classDiff.put(lead.carid, lead.net - mynet);
 				}
 			}
 			return ret;
 		}
 		catch (Exception ioe)
 		{
 			logError("loadDialins", ioe);
 			return null;
 		}
 	}
 
 
 	@Override
 	public void updateChallengeRound(ChallengeRound r) 
 	{
 		try
 		{
 			List<Object> list = newList();
 			list.add(r.challengeid);
 			list.add(r.round);
 			list.add(r.swappedstart);
 			list.add(r.car1.carid);
 			list.add(r.car1.dial);
 			list.add(r.car1.result);
 			list.add(r.car1.newdial);
 			list.add(r.car2.carid);
 			list.add(r.car2.dial);
 			list.add(r.car2.result);
 			list.add(r.car2.newdial);
 			list.add(r.id);
 			executeUpdate("UPDATECHALLENGEROUND", list);
 		}
 		catch (Exception ioe)
 		{
 			logError("updateChallengeRound", ioe);
 		}
 	}
 
 
 	@Override
 	public List<Driver> getDriversLike(String first, String last)
 	{
 		if ((first == null) && (last == null))
 			return new ArrayList<Driver>();
 
 		ArrayList<Driver> ret = null;
 		try
 		{
 			ResultData d;
 			if (first == null)
 				d = executeSelect("GETDRIVERSBYLAST", newList(last+"%"));
 			else if (last == null)
 				d = executeSelect("GETDRIVERSBYFIRST", newList(first+"%"));
 			else
 				d = executeSelect("GETDRIVERSBY", newList(first+"%", last+"%"));
 
 			ret = new ArrayList<Driver>();
 			for (ResultRow r : d)
 				ret.add(AUTO.loadDriver(r));
 			return ret;
 		}
 		catch (Exception ioe)
 		{
 			logError("getDriversLike", ioe);
 			return null;
 		}
 	}
 
 	@Override
 	public Map<Integer, Driver> getAllDrivers()
 	{
 		Map<Integer, Driver> ret = null;
 		try
 		{
 			ResultData d = executeSelect("GETALLDRIVERS", null);
 			ret = new HashMap<Integer, Driver>();
 			for (ResultRow r : d)
 			{
 				Driver driver = AUTO.loadDriver(r);
 				ret.put(driver.id, driver);
 			}
 			return ret;
 		}
 		catch (Exception ioe)
 		{
 			logError("getAllDrivers", ioe);
 			return null;
 		}
 	}
 
 	
 	@Override
 	public Map<Integer, Car> getAllCars()
 	{
 		Map<Integer, Car> ret = null;
 		try
 		{
 			ResultData d = executeSelect("GETALLCARS", null);
 			ret = new HashMap<Integer, Car>();
 			for (ResultRow r : d)
 			{
 				Car car = AUTO.loadCar(r);
 				ret.put(car.id, car);
 			}
 			return ret;
 		}
 		catch (Exception ioe)
 		{
 			logError("getAllCars", ioe);
 			return null;
 		}
 	}
 
 	@Override
 	public Map<Integer, Run> getAllRuns()
 	{
 		Map<Integer, Run> ret = null;
 		try
 		{
 			ResultData d = executeSelect("GETALLRUNS", null);
 			ret = new HashMap<Integer, Run>();
 			for (ResultRow r : d)
 			{
 				Run run = AUTO.loadRun(r);
 				ret.put(run.id, run);
 			}
 			return ret;
 		}
 		catch (Exception ioe)
 		{
 			logError("getAllRuns", ioe);
 			return null;
 		}
 	}
 
 
 	@Override
 	public boolean isInOrder(int carid) 
 	{
 		try
 		{
 			ResultData d = executeSelect("GETRUNORDERROWS", newList(currentEvent.id, currentCourse, carid));
 			return !d.isEmpty();
 		}
 		catch (Exception ioe)
 		{
 			logError("isInOrder", ioe);
 			return false;
 		}
 	}
 
 	@Override
 	public ClassData getClassData()
 	{
 		try
 		{
 			ClassData ret = new ClassData();
 
 			ResultData cdata = executeSelect("GETCLASSES", null);
 			for (ResultRow r : cdata)
 				ret.add(AUTO.loadClass(r));
 
 			ResultData idata = executeSelect("GETINDEXES", null);
 			for (ResultRow r : idata)
 				ret.add(AUTO.loadIndex(r));
 
 			classCache = ret; // save for index lookups, user may preload cache for us
 			return ret;
 		}
 		catch (Exception ioe)
 		{
 			logError("getClassData", ioe);
 			return null;
 		}
 	}
 
 	//static HashMap<String,Double> cache = new HashMap<String,Double>();
 	ClassData classCache = null;
 	double getEffectiveIndex(String classcode, String indexcode)
 	{
 		if (classCache == null)
 			getClassData();
 
 		double indexVal = 1.0;
 		try
 		{
 			ClassData.Class classData = classCache.getClass(classcode);
 			ClassData.Index indexData;
 
 			if (classData == null)
 				throw new Exception("Invalid class: " + classcode);
 
 			/* Apply car index */
 			if (classData.carindexed)
 			{
 				if ((indexData = classCache.getIndex(indexcode)) != null)
 					indexVal *= indexData.getValue();
 			}
 
 			/* Apply class index (linked to index tables) */
 			if (classData.classindexed)
 			{
 				if ((indexData = classCache.getIndex(classcode)) != null)
 					indexVal *= indexData.getValue();
 			}
 
 			/* Apply special class multiplier (only < 1.000 for Tire class at this point) */
 			indexVal *= classData.classmultiplier;
 		}
 		catch (Exception ioe)
 		{
 			logError("getEffectiveIndex", ioe);
 		}
 
 		return indexVal;
 	}
 }
