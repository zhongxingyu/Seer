 package envJavaSpace;
 import java.awt.Container;
 import java.awt.Point;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.lang.reflect.InvocationTargetException;
 import java.net.MalformedURLException;
 import java.rmi.*; 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.concurrent.*; 
 
 import oopl.DistributedOOPL;
 import oopl.GUI.GUI;
 import tuplespace.*;
 import tuplespace.Prohibition;
 import apapl.Environment;
 import apapl.data.*;
 import aplprolog.prolog.Prolog;
 import aplprolog.prolog.builtins.ExternalActions;
 import aplprolog.prolog.builtins.ExternalTool;
 import net.jini.core.discovery.*;
 import net.jini.core.entry.*;
 import net.jini.core.event.RemoteEventListener;
 import net.jini.core.lease.Lease;
 import net.jini.core.lease.LeaseDeniedException;
 import net.jini.core.lookup.*;  
 import net.jini.core.transaction.Transaction;
 import net.jini.core.transaction.TransactionException;
 import net.jini.core.transaction.TransactionFactory;
 import net.jini.core.transaction.server.TransactionManager;
 import net.jini.lookup.ServiceDiscoveryManager;
 import net.jini.space.*;
 
 /*
  * Extends Environment to be compatible with 2APL and implements ExternalTool to 
  * be compatible with my Prolog engine. 
  */
 public class SpaceTest  extends Environment implements ExternalTool{
 	public static JavaSpace space; // shared data
 	public int clock = 0;
 	public DistributedOOPL oopl; // norm interpreter
 	public static String TYPE_STATUS="status", TYPE_PROHIBITION="prohibition", 
 		TYPE_OBLIGATION="obligation", TYPE_READINGREQ = "readingRequest",TYPE_READING = "reading",TYPE_INVESTIGATE = "investigate",TYPE_CARGO = "cargo",TYPE_COIN = "coin",TYPE_POINTS = "points",
 			TYPE_OBJECT="object", TYPE_INVENTORY="inventory", NULL="null"; // for matching string with class type
 	public int[] ar_true, ar_null, ar_state_change, ar_false; // precalculated IntProlog data 
 	public int INT_TUPLE=0, INT_POINT=0, INT_NULL=0;
 	public APAPLTermConverter converter; // Converts between IntProlog and 2APL
 	private static TransactionManager transManager;
 	//private Object leaseRenewalManager;
 	private ServiceDiscoveryManager sdm;
 	private Prolog2Java p2j;
 	
 	/*
 	 * Just for testing.
 	 */
     public static void main(String[] args){ 
 		SpaceTest st = new SpaceTest();
     }
     
     /*
      * A kickoff function to begin the system.
      */
 	public void initialize() throws RemoteException, UnusableEntryException, TimeoutException, InterruptedException { 
 		// Jini stuff
 		System.out.println("test.");  
 		System.setSecurityManager(new RMISecurityManager());
 		LookupLocator ll = null; 
 		try { 
 			ll = new LookupLocator("jini://kafka.cs.nott.ac.uk"); 
 		} catch (MalformedURLException e) { 
 			
 			e.printStackTrace(); 
 		} 
 		
 		System.out.println("Lookup locator: "+ll.toString());
 		//StreamServiceRegistrar sr = ll.getStreamRegistrar();
 		
 		ServiceRegistrar sr = null; 
 		try { 
 			sr = ll.getRegistrar(); 
 			System.out.println("Service Registrar: "+sr.getServiceID());
 		} catch (Exception e) { 
 			
 			e.printStackTrace(); 
 		} 
 		
 		 try {
 	            File file = new File("./log/space"+ System.currentTimeMillis() +".log");
 
 	            // Create file if it does not exist
 	            boolean success = file.createNewFile();
 	            if (success) {
 	                // File did not exist and was created
 	            } else {
 	                // File already exists
 	            }
 	            
 	            PrintStream printStream;
 	    		try {
 	    			printStream = new PrintStream(new FileOutputStream(file));
 	    			System.setOut(printStream);
 	    		} catch (FileNotFoundException e1) {
 	    			// TODO Auto-generated catch block
 	    			e1.printStackTrace();
 	    		}
 	        } catch (IOException e) {
 	        	
 	        }
 		 System.out.println("test2.");
 		
 		System.out.println("Service Registrar: "+sr.getServiceID()); 
 		ServiceTemplate template = new ServiceTemplate(null, new Class[] { JavaSpace.class }, null); 
 		ServiceMatches sms = null; 
 		try { 
 			sms = sr.lookup(template, 10); 
 		} catch (RemoteException e) { 
 			e.printStackTrace(); 
 		} 
 		if(0 < sms.items.length) { 
 			space = (JavaSpace) sms.items[0].service; 
 			System.out.println("Java Space found.");  
 		
 			ServiceTemplate trans = new ServiceTemplate(null, new Class[] { TransactionManager.class }, null);
 
 			ServiceMatches sms1 = null;
 			try {
 				sms1 = sr.lookup(trans, 10);
 			} catch (RemoteException e) {
 
 				e.printStackTrace();
 			}
 			if(0 < sms1.items.length) {
 			    transManager = (TransactionManager) sms1.items[0].service;
 			    System.out.println("TransactionManager found.");
 			   
 			   
 			} else {
 			    System.out.println("No TransactionManager found.");
 			}
 /*			try {
 				sdm = new ServiceDiscoveryManager(null,null);
 				leaseRenewalManager = sdm.getLeaseRenewalManager();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}*/
 
 			registerOrg();
 			p2j = new Prolog2Java();
 			// Starting the normative system:
 			oopl = new DistributedOOPL(); // Create interpreter object
 			GUI g = new GUI(oopl,"SpaceOrg.2opl","OOPL",null,6677); // Make a GUI for the interpreter
 			converter = new APAPLTermConverter(oopl.prolog); // Make a term converter (relies on Prolog engine for string storage)
 			INT_TUPLE =makeStringKnown("tuple"); // Because strings get integers, you need to add them to the engine, so you can precompute constructs.
 			INT_POINT =makeStringKnown("point");
 			//INT_POINT =makeStringKnown("cell");
 			//INT_POINT =makeStringKnown("position");
 			INT_NULL =makeStringKnown("null"); 
 			makeStringKnown("notifyAgent"); 
 			makeStringKnown("clock"); 
 			makeStringKnown("obligation"); 
 			makeStringKnown("prohibition"); 
 			makeStringKnown("position");
 			makeStringKnown("reading");
 			makeStringKnown("investigate");
 			makeStringKnown("cargo");
 			makeStringKnown("coin");
 			makeStringKnown("points");
 			makeStringKnown("read"); 
 			makeStringKnown("readIfExists"); 
 			makeStringKnown("snapshot"); 
 			makeStringKnown("take"); 
 			makeStringKnown("takeIfExists"); 
 			makeStringKnown("write"); 
 			registerActions(oopl.prolog); // Register the possible actions on this ExternalTool (such as @external(space,theAction(arg1,arg2),Result).)
 			// Precompute some data: ('true.', 'null.', 'tuple_space_changed.')
 			ar_true = oopl.prolog.mp.parseFact("true.", oopl.prolog.strStorage, false); 
 			ar_false = oopl.prolog.mp.parseFact("false.", oopl.prolog.strStorage, false); 
 			ar_null = oopl.prolog.mp.parseFact("null.", oopl.prolog.strStorage, false);
 			ar_state_change = oopl.prolog.mp.parseFact("tuple_space_changed.", oopl.prolog.strStorage, false);
 			// To create a IntProlog structure out of a string use the above lines (but replace the fact string such as "true.")
 			// Starting the clock 
 			//Thread t = new Thread(new ClockTicker(this));
 			//t.start(); 
 			//this.insertTestData();
 		} else { 
 			System.out.println("No Java Space found."); 
 		}
 	} 
 
 	/*
 	 * Both used for increasing or just reading the clock. 
 	 */
 	public int updateClock(int amount){
 		//if(amount>0)  oopl.handleEvent(ar_state_change, false); // clock ticked so deadlines can be passed, handleEvent causes the interpreter to check the norms
 		Time t = new Time();
 		Entry e = getLast(t);
 		//System.out.println(e.toString());
 		if (e != null)
 			return ((Time) e).clock;
 		return 0;
 	}
 	
 	/*
 	 * Constructor immediately initializes the space. 
 	 */
 	public SpaceTest(){
 		super();
 		try { initialize(); } catch (Exception e) { e.printStackTrace(); }
 	}
 	
 	//////////////////////// 2OPL TO JAVASPACE AND 2APL
 	/*
 	 * Make sure String s has an index in the Prolog engine.
 	 */
 	public int makeStringKnown(String s){
 		if(oopl.prolog.strStorage.getInt(s)==null) oopl.prolog.strStorage.add(s);
 		return oopl.prolog.strStorage.getInt(s);
 	}
 	/*
 	 * Make the possible external actions known to the Prolog engine. These will be the actions that
 	 * the organization can do.
 	 */
 	public void registerActions(Prolog p) { 
 		oopl.prolog.builtin.external.registerAction("read", this, ExternalActions.INTAR, ExternalActions.INTAR);
 		oopl.prolog.builtin.external.registerAction("readIfExists", this, ExternalActions.INTAR, ExternalActions.INTAR);
 		oopl.prolog.builtin.external.registerAction("snapshot", this, ExternalActions.INTAR, ExternalActions.INTAR);
 		oopl.prolog.builtin.external.registerAction("take", this, ExternalActions.INTAR, ExternalActions.INTAR);
 		oopl.prolog.builtin.external.registerAction("takeIfExists", this, ExternalActions.INTAR, ExternalActions.INTAR);
 		oopl.prolog.builtin.external.registerAction("write", this, ExternalActions.INTAR, ExternalActions.INTAR);
 		oopl.prolog.builtin.external.registerAction("notifyAgent", this, ExternalActions.INTAR, ExternalActions.INTAR);
 		oopl.prolog.builtin.external.registerAction("clock", this, ExternalActions.INTAR, ExternalActions.INTAR);
 	}
 
 	/*
 	 * Handle a call from the organization (actually: from Prolog). These calls are in IntProlog datatypes (int arrays). 
 	 * ExternalActions ea is a part of the Prolog engine which reads returns ea.intResult after the
 	 * external call.
 	 */
 	public void handleCall(int[] call, ExternalActions ea, int returnType) {  
 		/*
 		 * For JavaSpace calls: the integer array is first transformed to an Entry object, then passed
 		 * to the JavaSpaced using the appropriate method call, and then the result is converted back
 		 * to an integer array.
 		 */
 		if(call[1] == oopl.prolog.strStorage.getInt("read")){
 			try {
 				//
 				
 				//System.out.println("read hack 1");
 				//ea.intResult = ar_true;
 				/*Entry e = space.read(createEntry(call, true), null, Lease.FOREVER);
 				Entry e1 = space.read(createEntry(call, false), null, Lease.FOREVER);
 				if (e != null && e == e1)
 					ea.intResult = ar_true;
 				else
 					ea.intResult = ar_false;*/
 				Entry a = createEntry(call);
 				//System.out.println(a.toString());
 				Entry e = getLast(a);
 				//System.out.println(e.toString());
 				ea.intResult = entryToArray(e);
 			} catch (Exception e) {e.printStackTrace();}
 		} else if(call[1] == oopl.prolog.strStorage.getInt("readIfExists")){
 			try {
 				ea.intResult = entryToArray(space.readIfExists(createEntry(call), null, get_number(call,oopl.prolog.harvester.scanElement(call, 3, false, false)+1)));
 			} catch (Exception e) {e.printStackTrace();}
 		} else if(call[1] == oopl.prolog.strStorage.getInt("snapshot")){
 			ea.intResult = ar_true;
 		} else if(call[1] == oopl.prolog.strStorage.getInt("take")){
 			try {
 				ea.intResult = entryToArray(space.take(createEntry(call), null, get_number(call,oopl.prolog.harvester.scanElement(call, 3, false, false)+1)));
 			} catch (Exception e) {e.printStackTrace();}
 		} else if(call[1] == oopl.prolog.strStorage.getInt("takeIfExists")){
 			try {
 				ea.intResult = entryToArray(space.takeIfExists(createEntry(call), null, get_number(call,oopl.prolog.harvester.scanElement(call, 3, false, false)+1)));
 			} catch (Exception e) {e.printStackTrace();}
 		} else if(call[1] == oopl.prolog.strStorage.getInt("write")){
 			System.out.println("write");
 			try {
 				long lease = get_number(call,oopl.prolog.harvester.scanElement(call, 3, false, false)+1);
 				if(lease <= 0) lease = Lease.FOREVER;
 				
 				Entry e = createEntry(call);
 				space.write(e, null, lease);
 				//System.out.println(e+"  "+lease+"   "+Lease.FOREVER);
 				ea.intResult = ar_true;
 			} catch (Exception e) {e.printStackTrace();}
 	    /*
 	     * The next case throws towards the agent an event that its status is changed.
 	     */
 		} else if(call[1] == oopl.prolog.strStorage.getInt("notifyAgent")){ // notifyAgent(name,obligation(blabla)).
 			//System.out.println("notify agent THIS SHOULD NOT BE CALLED!!!!!!");
 /*			for (int i = 0;  i<call.length; i++){
 				
 				String recipient = oopl.prolog.strStorage.getString(call[i]);
 				System.out.println("create entry test "+ i + recipient);
 			}*/
 			String recipient = oopl.prolog.strStorage.getString(call[4]);
 			APLFunction event = (APLFunction)converter.get2APLTerm(Arrays.copyOfRange(call, 6, call.length));
 			//System.out.println("Sending event to "+recipient+": "+event);
 			try {
 				space.write(createEntry(recipient, event), null, Lease.FOREVER);
 			} catch (RemoteException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (TransactionException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			//throwEvent(event, new String[]{recipient});
 			ea.intResult = ar_true;
 		} else if(call[1] == oopl.prolog.strStorage.getInt("clock")){ // Read the clock
 			int[] r = new int[3];
 			addNumber(r, 0, updateClock(0)); // Use updateClock because of synchronization
 			ea.intResult = r;
 		}
 	}
 
 
 	public void handleCall(Object[] call, ExternalActions p, int returnType) { }
 	
 	/*
 	 * Create an entry object form an integer array. Perhaps we want to replace this with
 	 * something like createEntry(oopl.prolog.toPrologString(call)).
 	 */
 	public Entry createEntry(int[] call) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException{ // e.g.: read(tuple(name,point(2,4),20),0)
 		//System.out.println(oopl.prolog.arStr(call));
 		return p2j.parseTerm(call, oopl);
 		
 		
 	/*	
 		int type = call[4];
 		System.out.println("!!!!!!!!!!!!!!!!");
 		String tuple = oopl.prolog.strStorage.getString(call[4]);
 		System.out.println(call.toString());
 		if (tuple.startsWith("position")) {
 			//System.out.println("org reads position");
 			String name = null;
 			if(call[7]!=INT_NULL) name = oopl.prolog.strStorage.getString(call[7]);
 			Cell c = null;
 			if(call[10]!=INT_NULL) c = new Cell(get_number(call,13),get_number(call,16)); 
 			//c = new Cell(3,3); 
 			//System.out.println(c.toString());
 
 			return new Position(name,c);
 		}
 		else if (tuple.startsWith("cargo")) {
 			//System.out.println("org reads cargo");
 			return new Cargo();
 		}
 		else if (tuple.startsWith("coin")) {
 			//System.out.println("org reads coin");
 			int l = call.length;
 		
 			return new Coin();
 		}
 		else if (tuple.startsWith("reading")) {
 			//System.out.println("org reads reading");
 			
 			return new Reading();
 		}
 		else if (tuple.startsWith("investigate")) {
 			//System.out.println("org reads investigate");
 		
 			return new Investigate();
 		}
 		else if(tuple == ""){
 			
 		}
 		
 /*		for (int i = 0;  i<call.length; i++){
 			
 			String recipient = oopl.prolog.strStorage.getString(call[i]);
 			System.out.println("create entry test "+ i + recipient);
 		}
 		else if(type == INT_TUPLE){ // I have to make this more accessable 
 			String name = null;
 			if(call[7]!=INT_NULL) name = oopl.prolog.strStorage.getString(call[7]);
 			Point p = null;
 
 			if(call[10]!=INT_NULL) p = new Point(get_number(call,13),get_number(call,16)); 
 
 			Integer i = null;
 			int c = oopl.prolog.harvester.scanElement(call, 9, false, false);
 			if(call[c+1]!=INT_NULL) i = get_number(call,c+1);
 			return new Tuple(name,p,i);
 		}
 
 		return null;*/
 	}
 	
 	/*
 	public Entry fromProlog(String s) {
 		
 		String delims = "[(),]+";
 		String[] tokens = s.split(delims);
 		System.out.println("create entry test " + tokens.toString());
 		return null;
 		
 	}
 	
 	public String fromJava(Entry e) {
 		
 		
 		return null;
 		
 	}
 	*/
 
 	/*
 	 * Convert an entry to an array. Can also be done by calling the prolog compiler and give
 	 * it e.toPrologString() as an argument.
 	 */
 	public int[] entryToArray(Entry e){
 		if(e == null){
 			int[] r = new int[3];
 			addPredicate(r, 0, oopl.prolog.strStorage.getInt("null"), 0);
 			return r;
 				
 		} 
 		else {
 			return ((TimeEntry) e).toArray(oopl);
 		}
 		/*
 		else if(e instanceof Tuple){
 			Tuple t = (Tuple)e;
 			int[] r = new int[t.point==null?12:18];
 			addPredicate(r,0,oopl.prolog.strStorage.getInt("tuple"),3); // tuple/3
 			addPredicate(r,3,makeStringKnown(t.str==null?"null":t.str),0); // the name
 			int c = 9;
 			if(t.point == null) addPredicate(r, 6, oopl.prolog.strStorage.getInt("null"), 0);
 			else {
 				addPredicate(r, 6, oopl.prolog.strStorage.getInt("point"), 2);
 				addNumber(r, 9, t.point.x);
 				addNumber(r, 12, t.point.y);
 				c = 15;
 			} 
 			addNumber(r, c,t.i);
 			return r;
 		}
 		
 		
 		else if(e instanceof Position){
 			Position t = (Position)e;
 			int[] r = new int[t.cell==null?12:18];
 			addPredicate(r,0,oopl.prolog.strStorage.getInt("position"),2); // tuple/3
 			addPredicate(r,3,makeStringKnown(t.agent==null?"null":t.agent),0); // the name
 			int c = 9;
 			if(t.cell == null) addPredicate(r, 6, oopl.prolog.strStorage.getInt("null"), 0);
 			else {
 				addPredicate(r, 6, oopl.prolog.strStorage.getInt("cell"), 2);
 				addNumber(r, 9, t.cell.x);
 				addNumber(r, 12, t.cell.y);
 				c = 15;
 			} 
 			//System.out.println("to array: " + r.toString());
 			//addNumber(r, c,t.i);
 			return r;
 		}
 		else if(e instanceof Cargo){
 			Cargo t = (Cargo)e;
 			int[] r = new int[15];
 			addPredicate(r,0,oopl.prolog.strStorage.getInt("cargo"),2); // cargo/2
 	
 			
 				addPredicate(r, 3, oopl.prolog.strStorage.getInt("cell"), 2);
 				addNumber(r, 6, t.cell.x);
 				addNumber(r, 9, t.cell.y);
 				
 			addNumber(r,12,t.clock);
 			//addPredicate(r,3,makeStringKnown(t.agent==null?"null":t.agent),0); // the name
 			//for (int i = 0;  i<r.length; i++){
 			//	System.out.println("to array: " + oopl.prolog.strStorage.getString(r[i]));
 				
 			//}
 			
 			//addNumber(r, c,t.i);
 			return r;
 		}
 		else if(e instanceof Coin){
 			Coin t = (Coin)e;
 			int[] r = new int[18];
 			addPredicate(r,0,oopl.prolog.strStorage.getInt("coin"),3); // cargo/2
 	
 			
 				addPredicate(r, 3, oopl.prolog.strStorage.getInt("cell"), 2);
 				addNumber(r, 6, t.cell.x);
 				addNumber(r, 9, t.cell.y);
 				
 			addNumber(r,12,t.clock);
 			addPredicate(r,15, makeStringKnown(t.agent),0);
 			//addPredicate(r,3,makeStringKnown(t.agent==null?"null":t.agent),0); // the name
 			//for (int i = 0;  i<r.length; i++){
 			//	System.out.println("to array: " + oopl.prolog.strStorage.getString(r[i]));
 				
 			//}
 			
 			//addNumber(r, c,t.i);
 			return r;
 		}
 		else if(e instanceof Reading){
 			Reading t = (Reading)e;
 			int[] r = new int[21];
 			addPredicate(r,0,oopl.prolog.strStorage.getInt("reading"),4); // cargo/2
 	
 			
 			addPredicate(r, 3, oopl.prolog.strStorage.getInt("cell"), 2);
 			addNumber(r, 6, t.cell.x);
 			addNumber(r, 9, t.cell.y);
 			addNumber(r, 12, t.value.intValue());
 			addPredicate(r,15, makeStringKnown(t.agent),0);
 			addNumber(r,18,t.clock);
 			//addPredicate(r,3,makeStringKnown(t.agent==null?"null":t.agent),0); // the name
 			//for (int i = 0;  i<r.length; i++){
 			//	System.out.println("to array: " + oopl.prolog.strStorage.getString(r[i]));
 				
 			//}
 			
 			//addNumber(r, c,t.i);
 			return r;
 		}
 		else if(e instanceof Investigate){
 			Investigate t = (Investigate)e;
 			int[] r = new int[21];
 			addPredicate(r,0,oopl.prolog.strStorage.getInt("investigate"),4); // cargo/2
 	
 			
 			addPredicate(r, 3, oopl.prolog.strStorage.getInt("cell"), 2);
 			addNumber(r, 6, t.cell.x);
 			addNumber(r, 9, t.cell.y);
 			addNumber(r, 12, t.value.intValue());
 			addPredicate(r,15, makeStringKnown(t.agent),0);
 			addNumber(r,18,t.clock);
 			//addPredicate(r,3,makeStringKnown(t.agent==null?"null":t.agent),0); // the name
 			//for (int i = 0;  i<r.length; i++){
 			//	System.out.println("to array: " + oopl.prolog.strStorage.getString(r[i]));
 				
 			//}
 			
 			//addNumber(r, c,t.i);
 			return r;
 		
 		}
 		else if(e instanceof Points){
 			Points t = (Points)e;
 			int[] r = new int[12];
 			addPredicate(r,0,oopl.prolog.strStorage.getInt("points"),4); // points/2
 			
 			addPredicate(r,3,makeStringKnown(t.agent),0); // the name
 			addNumber(r,6,t.clock);
 			addNumber(r, 9, t.value);
 
 			return r;
 		}
 		
 		return null;
 		*/
 	}
 	/*
 	 * Gets the int value of a number out of an integer array.
 	 * Note that normally this is a double.
 	 */
 	public int get_number(int[] call, int cursor){
 		long l1 = ((long)call[cursor]<<32)>>>32;
 		long l2 = ((long)call[cursor+1]<<32)>>>32;
 		return (int)Double.longBitsToDouble((l1<<32)|l2);
 	} 
 	/*
 	 * Add predicate integers to an array.
 	 */
 	public void addPredicate(int[] array, int cursor, int name, int arity){
 		array[cursor] = oopl.prolog.harvester.PREDICATE;
 		array[cursor+1] = name;
 		array[cursor+2] = arity;
 	}
 	/*
 	 * Add a number to an array.
 	 */
 	public void addNumber(int[] array, int cursor, int number){
 		array[cursor] = oopl.prolog.harvester.NUMBER;
 		array[cursor+1] = getInt(number,true);
 		array[cursor+2] = getInt(number,false);
 	}
 	/*
 	 * Convert a regular integer to a Prolog store value. Each number is encoded with 
 	 * two integers (64 bit double format), so you can use getInt(i,true) and getInt(i,false) 
 	 * to get both i's number representation parts.
 	 */
 	public int getInt(int i, boolean a){
 		long l = Double.doubleToLongBits(i);
 		if(a) return (int)((l>>>32));
 		else return (int)((l<<32)>>>32);
 	}
 	
 	
 	//////////////////////// 2APL AND JAVASPACE
 
 	/**
 	 * Convert a Prolog predicate to a suitable JavaSpace datatype.
 	 * @param sAgent The agent that calls the method (important for the name in the status).
 	 * @param call The predicate from the call.
 	 * @return The entry representation of the predicate.
 	 */
 	public Entry createEntry(String sAgent, APLFunction call){ 
 		
		System.out.print("from/for agent " + sAgent + "  ");
 		System.out.println(call.toString());
 		if(call.getName().equals(TYPE_STATUS)){ // Prolog format: status(position(1,4),30) 
 			Cell c = null;
 			if(call.getParams().get(0) instanceof APLFunction){ // null is APLIdent  
 				APLFunction point = (APLFunction) call.getParams().get(0); // Get the point coordinations TODO: type check the arguments
 				int pointX = ((APLNum)point.getParams().get(0)).toInt(); // Get the position
 				int pointY = ((APLNum)point.getParams().get(1)).toInt();
 				c = new Cell(pointX,pointY);
 			}
 			Integer clock = null; // if health is null (which is ident) it stays also in java null
 			if(call.getParams().get(1) instanceof APLNum) clock = ((APLNum)call.getParams().get(1)).toInt(); // The health meter
 			
 			return new Position(sAgent,c,clock); // Create Tuple
 		}
 		else if(call.getName().equals(TYPE_READINGREQ)){ // Prolog format: readingRequest(position(X,Y))
 			Cell c = null;
 			if(call.getParams().get(0) instanceof APLFunction){ // null is APLIdent  
 				APLFunction point = (APLFunction) call.getParams().get(0); // Get the point coordinations TODO: type check the arguments
 				int pointX = ((APLNum)point.getParams().get(0)).toInt(); // Get the position
 				int pointY = ((APLNum)point.getParams().get(1)).toInt();
 				c = new Cell(pointX,pointY);
 			}
 			return new ActionRequest(sAgent,"reading",c); // Create Tuple
 		}
 		else if(call.getName().equals(TYPE_COIN)){ // Prolog format: coin(position(X,Y),Clock,Agent)
 			//System.out.println("create entry coin "+call.getParams().toString());
 			Cell c = null;
 			if(call.getParams().get(0) instanceof APLFunction){ // null is APLIdent  
 				APLFunction point = (APLFunction) call.getParams().get(0); // Get the point coordinations TODO: type check the arguments
 				int pointX = ((APLNum)point.getParams().get(0)).toInt(); // Get the position
 				int pointY = ((APLNum)point.getParams().get(1)).toInt();
 				c = new Cell(pointX,pointY);
 			}
 			Integer clock = null; // if health is null (which is ident) it stays also in java null
 			if(call.getParams().get(1) instanceof APLNum) clock = ((APLNum)call.getParams().get(1)).toInt(); // The health meter
 			String agent = null; // if health is null (which is ident) it stays also in java null
 			if(call.getParams().get(1) instanceof APLIdent) agent = ((APLIdent)call.getParams().get(1)).toString(); // The health meter
 			
 			return new Coin(c,agent,clock); // Create Tuple
 		}
 		else if(call.getName().equals(TYPE_CARGO)){ // Prolog format: cargo(position(X,Y),Clock)
 			//System.out.println("create entry cargo "+call.getParams().toString());
 			Cell c = null;
 			if(call.getParams().get(0) instanceof APLFunction){ // null is APLIdent  
 				APLFunction point = (APLFunction) call.getParams().get(0); // Get the point coordinations TODO: type check the arguments
 				int pointX = ((APLNum)point.getParams().get(0)).toInt(); // Get the position
 				int pointY = ((APLNum)point.getParams().get(1)).toInt();
 				c = new Cell(pointX,pointY);
 			}
 			Integer clock = null; // if health is null (which is ident) it stays also in java null
 			if(call.getParams().get(1) instanceof APLNum) clock = ((APLNum)call.getParams().get(1)).toInt(); // The health meter
 			
 			return new Cargo(c,clock); // Create Tuple
 		} 
 		else if(call.getName().equals(TYPE_POINTS)){ //points(Agent,Now,NewHealth)
 			//System.out.println("create entry points "+call.getParams().toString());
 			
 			Integer clock = null; // if health is null (which is ident) it stays also in java null
 			if(call.getParams().get(1) instanceof APLNum) clock = ((APLNum)call.getParams().get(1)).toInt(); // The health meter
 			Integer health = null; // if health is null (which is ident) it stays also in java null
 			if(call.getParams().get(2) instanceof APLNum) health = ((APLNum)call.getParams().get(2)).toInt(); // The health meter
 		
 			return new Points(sAgent,clock,health); // Create Tuple
 		}
 		else if(call.getName().equals(TYPE_PROHIBITION)){ // Prolog format: status(position(1,4),30) 
 			Prohibition p = null;
 			//System.out.println("create entry prohibition "+call.getParams().toString());
 			
 		
 			if(call.getParams().get(0) instanceof Term){ // null is APLIdent  
 				//APLFunction point = (APLFunction) call.getParams().get(0); // Get the point coordinations TODO: type check the arguments
 				String s1 = call.getParams().get(0).toString();// Get the position
 				String s2 = call.getParams().get(1).toString();
 				p = new Prohibition(sAgent, s1, s2, 1);
 			}
 			//Integer health = null; // if health is null (which is ident) it stays also in java null
 			//if(call.getParams().get(1) instanceof APLNum) health = ((APLNum)call.getParams().get(1)).toInt(); // The health meter
 			//System.out.println(call.toString());
 			//System.out.println(p.toString());
 			return p; // Create Tuple
 		} 
 		else if(call.getName().equals(TYPE_OBLIGATION)){ // Prolog format: status(position(1,4),30) 
 			Obligation o = null;
 			//System.out.println("create entry obligation "+call.getParams().toString());
 			
 		
 			if(call.getParams().get(0) instanceof Term){ // null is APLIdent  
 				//APLFunction point = (APLFunction) call.getParams().get(0); // Get the point coordinations TODO: type check the arguments
 				String s1 = call.getParams().get(0).toString();// Get the position
 				//String s2 = call.getParams().get(1).toString();
 				String s3 = call.getParams().get(2).toString();
 				
 				int deadline = ((APLNum)call.getParams().get(1)).toInt();
 				
 				o = new Obligation(sAgent, s1, s3, deadline, 1);
 				//System.out.println(s2);
 			}
 			//Integer health = null; // if health is null (which is ident) it stays also in java null
 			//if(call.getParams().get(1) instanceof APLNum) health = ((APLNum)call.getParams().get(1)).toInt(); // The health meter
 			//System.out.println(call.toString());
 			//System.out.println(o.toString());
 			return o; // Create Tuple
 		} 
 		else if(call.getName().equals(TYPE_OBJECT)){ // Prolog format: object(truck,position(30,20))
 			String name = ((APLIdent)call.getParams().get(0)).getName();
 			Point p = null;
 			if(call.getParams().get(1) instanceof APLFunction){ // null is APLIdent so 
 				APLFunction point = (APLFunction) call.getParams().get(1); // Get the point coordinations TODO: type check the arguments
 				int pointX = ((APLNum)point.getParams().get(0)).toInt(); // Get the position
 				int pointY = ((APLNum)point.getParams().get(1)).toInt();
 				p = new Point(pointX,pointY);
 			}
 			return new Tuple(name,p,null); // Create Tuple
 		}  
 		return null;
 	}
 	
 	//agent use
 	public Term entryToTerm(Entry entry){ 
 		
 		if(entry instanceof Points){ // in case of tuples return points(name,value,clock)
 			Points points = (Points) entry;   // cast to tuple
 			return new APLFunction("points", new Term[]{new APLIdent(points.agent),new APLNum(points.value),new APLNum(points.clock)}); // construct result
 		} 
 		else if(entry instanceof Reading){ // in case of tuples return reading(name,position(2,4),value,clock)
 			Reading reading = (Reading) entry;   // cast to tuple
 
 			Term posTerm = new APLFunction("position", new Term[]{new APLNum(reading.cell.x),new APLNum(reading.cell.y)}); // get position
 
 			return new APLFunction("tuple", new Term[]{new APLIdent(reading.agent),posTerm,new APLNum(reading.value.intValue()),new APLNum(reading.clock)}); // construct result
 			
 		} 
 		else if(entry instanceof Obligation){ // in case of tuples return tuple(name,position(2,4),48)
 			Obligation o = (Obligation) entry;   // cast to tuple
 			String name = o.agent;
 			if(name==null)name="null"; 
 			Term posTerm = new APLIdent("null");
 			Term posTerm1 = new APLIdent("null");
 			Term posTerm2 = new APLIdent("null");
 			//all possible obligations
 			String term;
 			int t = o.obligation.indexOf("(");
 			term = o.obligation.substring(1, t).trim();
 			if (term.startsWith("at"))
 			{
 				int i = o.obligation.indexOf(",");
 				int j = o.obligation.indexOf(",", i+1);
 				int x = Integer.parseInt(o.obligation.substring(term.length() + 2, i).trim());
 				int y = Integer.parseInt(o.obligation.substring(i+1, j).trim());
 				posTerm = new APLFunction("at", new Term[]{new APLNum(x),new APLNum(y), new APLIdent(name)}); // get position
 			}
 			else if (term.startsWith("coin")) { //[coin(X,Y,NewTime,A1)]
 				posTerm = coinTerm(o.obligation, term, o.agent);
 			}
 			else if (term.startsWith("cargo")) { //[cargo(X,Y,NewTime)]
 				posTerm = cargoTerm(o.obligation, term, o.agent);
 			}
 			else if (term.startsWith("reading")) { //[reading(X1,Y1,Value,Thing,Time)]
 				posTerm = readingTerm(o.obligation, term, o.agent);
 			}
 			else if (term.startsWith("investigate")) {
 				posTerm = investigateTerm(o.obligation, term, o.agent);
 			}
 			
 			if(o.deadline!=null){
 				posTerm1 = new APLNum(o.deadline);
 			}
 			if(o.sanction!=null){
 				int i = o.sanction.indexOf("(");
 				posTerm2 = new APLFunction(o.sanction.substring(1,i), new Term[]{new APLIdent(name)});
 			}
 			return new APLFunction("obligation", new Term[]{posTerm,posTerm1,posTerm2});
 			// TODO: other datatypes
 		}
 		else if(entry instanceof Prohibition){ // in case of tuples return tuple(name,position(2,4),48)
 			Prohibition o = (Prohibition) entry;   // cast to tuple
 			String name = o.agent;
 			if(name==null)name="null"; 
 			Term posTerm = new APLIdent("null");
 			Term posTerm2 = new APLIdent("null");
 			
 			String term;
 			int t = o.prohibition.indexOf("(");
 			term = o.prohibition.substring(1, t).trim();
 			if (term.startsWith("at"))
 			{
 				int i = o.prohibition.indexOf(",");
 				int j = o.prohibition.indexOf(",", i+1);
 				int x = Integer.parseInt(o.prohibition.substring(term.length() + 2, i).trim());
 				int y = Integer.parseInt(o.prohibition.substring(i+1, j).trim());
 				posTerm = new APLFunction("at", new Term[]{new APLNum(x),new APLNum(y), new APLIdent(name)}); // get position
 			}
 			else if (term.startsWith("coin")) { //[coin(X,Y,NewTime,A1)]
 				posTerm = coinTerm(o.prohibition, term, o.agent);
 			}
 			else if (term.startsWith("cargo")) { //[cargo(X,Y,NewTime)]
 				posTerm = cargoTerm(o.prohibition, term, o.agent);
 			}
 			else if (term.startsWith("reading")) { //[reading(X1,Y1,Value,Thing,Time)]
 				posTerm = readingTerm(o.prohibition, term, o.agent);
 			}
 			else if (term.startsWith("investigate")) {
 				posTerm = investigateTerm(o.prohibition, term, o.agent);
 			}
 			
 
 			if(o.sanction!=null){
 				int i = o.sanction.indexOf("(");
 				posTerm2 = new APLFunction(o.sanction.substring(1,i), new Term[]{new APLIdent(name)});
 			}
 			return new APLFunction("prohibition", new Term[]{posTerm,posTerm2});
 			// TODO: other datatypes
 		}
 		return new APLIdent("null");
 	}
 
 	private Term investigateTerm(String s, String term, String agent) {
 		int i = s.indexOf(",");
 		int j = s.indexOf(",", i+1);
 		int k = s.indexOf(",", j+1);
 		int l = s.indexOf(",", k+1);
 		int m = s.indexOf(")", l+1);
 		String x = s.substring(term.length() + 2, i).trim();
 		String y = s.substring(i+1, j).trim();
 		String z = s.substring(j+1, k).trim();
 		String w = s.substring(k+1, m).trim();
 		Term xt = numOrVar(x);
 		Term yt = numOrVar(y);
 		Term zt = numOrVar(z);
 		Term wt = numOrVar(w);
 		Term posTerm = new APLFunction("investigate", new Term[]{xt,yt,zt, new APLIdent(agent), wt});return posTerm;
 	}
 
 	private Term readingTerm(String s, String term, String agent) {
 		int i = s.indexOf(",");
 		int j = s.indexOf(",", i+1);
 		int k = s.indexOf(",", j+1);
 		int l = s.indexOf(",", k+1);
 		int m = s.indexOf(")", l+1);
 		String x = s.substring(term.length() + 2, i).trim();
 		String y = s.substring(i+1, j).trim();
 		String z = s.substring(j+1, k).trim();
 		String w = s.substring(k+1, m).trim();
 		Term xt = numOrVar(x);
 		Term yt = numOrVar(y);
 		Term zt = numOrVar(z);
 		Term wt = numOrVar(w);
 		Term posTerm = new APLFunction("reading", new Term[]{xt,yt,zt, new APLIdent(agent), wt});
 		return posTerm;
 	}
 
 	private Term cargoTerm(String s, String term, String agent) {
 		int i = s.indexOf(",");
 		int j = s.indexOf(",", i+1);
 		int k = s.indexOf(")", j+1);
 		String x = s.substring(term.length() + 2, i).trim();
 		String y = s.substring(i+1, j).trim();
 		String z = s.substring(j+1, k).trim();
 		Term xt = numOrVar(x);
 		Term yt = numOrVar(y);
 		Term zt = numOrVar(z);
 		Term posTerm = new APLFunction("cargo", new Term[]{xt,yt,zt, new APLIdent(agent)});
 		return posTerm;
 	}
 
 	private Term coinTerm(String s, String term, String name) {
 		int i = s.indexOf(",");
 		int j = s.indexOf(",", i+1);
 		int k = s.indexOf(",", j+1);
 		String x = s.substring(term.length() + 2, i).trim();
 		String y = s.substring(i+1, j).trim();
 		String z = s.substring(j+1, k).trim();
 		Term xt = numOrVar(x);
 		Term yt = numOrVar(y);
 		Term zt = numOrVar(z);
 		Term posTerm = new APLFunction("coin", new Term[]{xt,yt,zt, new APLIdent(name)});
 		return posTerm;
 	}
 
 	private Term numOrVar(String x) {
 		Term xt;
 		Integer ix = Integer.getInteger(x);
 		if (ix != null) {
 			xt = new APLNum(ix);
 		}
 		else {
 			xt = new APLVar(x);
 		}
 		return xt;
 	}
 
 	//from agent program
 	public Term read(String sAgent, APLFunction call, APLNum timeOut){
 	
 		try{ 
 			return entryToTerm(space.read(createEntry(sAgent,call), null, timeOut.toInt())); 
 		} catch(Exception e){ e.printStackTrace(); return new APLIdent("null"); }
 	}
 	
 	public Term readIfExists(String sAgent, APLFunction call, APLNum timeOut){
 		try{ 
 			return entryToTerm(space.readIfExists(createEntry(sAgent,call), null, timeOut.toInt())); 
 		} catch(Exception e){ e.printStackTrace(); return new APLIdent("null"); }
 	}
 	
 	public Term snapshot(String sAgent, APLFunction call){
 		return new APLIdent("true");
 	}
 	
 	public Term take(String sAgent, APLFunction call, APLNum timeOut){
 		try{ 
 			Term e =  entryToTerm(space.take(createEntry(sAgent,call), null, timeOut.toInt())); 
 			//oopl.handleEvent(ar_state_change, false); // check the norms
 			return e;
 		} catch(Exception e){ e.printStackTrace(); return new APLIdent("null"); }
 	}
 	
 	public Term takeIfExists(String sAgent, APLFunction call, APLNum timeOut){
 		try{ 
 			Term e =  entryToTerm(space.takeIfExists(createEntry(sAgent,call), null, timeOut.toInt())); 
 			//if(e!=null)oopl.handleEvent(ar_state_change, false); // check the norms
 			return e;
 		} catch(Exception e){ e.printStackTrace(); return new APLIdent("null"); }
 	}
 	
 	public Term write(String sAgent, APLFunction call, APLNum lease){ 
 		//System.out.println("write " + sAgent);
 		try{
 			long leaseVal = lease.toInt();
 			if(leaseVal < 0) leaseVal = Lease.FOREVER; 
 			space.write(createEntry(sAgent,call), null, leaseVal);
 			//oopl.handleEvent(ar_state_change, false); // check the norms
 			return new APLIdent("true");
 		}catch (Exception e){ e.printStackTrace(); return new APLIdent("null"); }
 	}
 	
 	public Term clock(String sAgent){
 		return new APLNum(updateClock(0));
 	}
 	public Term readingRequest(String sAgent, APLFunction call){
 		return new APLNum(50);
 		
 		//TODO hack
 	}
 	/*
 	 * ENVIRONMENT OVERRIDES
 	 */
 	public synchronized void addAgent(String sAgent) {
 		//System.out.println("register " + sAgent);
 		register(sAgent);
 	}
 	
 	private void register(String agent) {
 		
 		AgentHandler handler;
 		try {
 			handler = new AgentHandler(this, agent);
 			space.notify(new Prohibition(agent), null,
 			        handler,
 			        3000000,
 			        new MarshalledObject(new String("prohibition")));
 			space.notify(new Obligation(agent), null,
 			        handler,
 			        3000000,
 			        new MarshalledObject(new String("obligation")));
 			space.notify(new Points(agent), null,
 			        handler,
 			        3000000,
 			        new MarshalledObject(new String("points")));
 			space.notify(new Reading(agent), null,
 			        handler,
 			        3000000,
 			        new MarshalledObject(new String("reading")));
 
 		} catch (RemoteException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (TransactionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 /*			theManager.renewFor(myReg.getLease(), Lease.FOREVER,
                 30000, new DebugListener());*/
 	}
 	
 	public Obligation readObligation(String agent) {
 		Obligation obligation = new Obligation(agent);
 		ArrayList<TimeEntry> obligations = new ArrayList<TimeEntry>();
 		getAll(obligation, obligations);
 		if (obligations.size() > 0)
 			return (Obligation) obligations.get(0);
 		return null;
 	}
 	
 	public Prohibition readProhibition(String agent) {
 		Prohibition prohibition = new Prohibition(agent);
 		ArrayList<TimeEntry> prohibitions = new ArrayList<TimeEntry>();
 		getAll(prohibition, prohibitions);
 		if (prohibitions.size() > 0)
 			return (Prohibition) prohibitions.get(0);
 		return null;
 	}
 
 	public Coin readCoin(String agent) {
 		Coin coin = new Coin(agent);
 		ArrayList<TimeEntry> coins = new ArrayList<TimeEntry>();
 		getAll(coin, coins);
 		if (coins.size() > 0)
 			return (Coin) coins.get(0);
 		return null;
 	}
 	
 	public Reading readReading(String agent) {
 		Reading reading = new Reading(agent);
 		ArrayList<TimeEntry> readings = new ArrayList<TimeEntry>();
 		getAll(reading, readings);
 		if (readings.size() > 0)
 			return (Reading) readings.get(0);
 		return null;
 	}
 	
 	public Points readPoints(String agent) {
 		Points point = new Points(agent);
 		ArrayList<TimeEntry> points = new ArrayList<TimeEntry>();
 		getAll(point, points);
 		if (points.size() > 0)
 			return (Points) points.get(0);
 		return null;
 	}
 
 	
 	private void getAll(Object template, ArrayList<TimeEntry> result) {
 
 		TimeEntry entry;
 		try {
 			Transaction.Created trans = TransactionFactory.create(transManager, Lease.FOREVER);
 			//leaseRenewalManager.renewUntil(trans.lease, Lease.FOREVER, null);
 			Transaction txn = trans.transaction;
 			try {
 				while ((entry = (TimeEntry) space.take((Entry) template, txn, 200)) != null){
 					//System.out.println(entry.toString());
 					result.add(entry);
 				}
 				TimeEntry t = getLatest(result);
 				result = new ArrayList<TimeEntry>();
 				result.add(t);
 				System.out.println(result.toString());
 				txn.abort();
 				//leaseRenewalManager.cancel(trans.lease);
 			} catch (UnusableEntryException e) {
 				e.printStackTrace();
 			} catch (TransactionException e) {
 				e.printStackTrace();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			} 
 			
 		} catch (LeaseDeniedException e1) {
 			e1.printStackTrace();
 		} catch (RemoteException e1) {
 			e1.printStackTrace();
 		}
 	}
 	private Entry getLast(Entry a) {
 		TimeEntry entry;
 		try {
 			Transaction.Created trans = TransactionFactory.create(transManager, Lease.FOREVER);
 			//leaseRenewalManager.renewUntil(trans.lease, Lease.FOREVER, null);
 			Transaction txn = trans.transaction;
 			try {
 				ArrayList<TimeEntry> result = new ArrayList<TimeEntry>();
 				while ((entry = (TimeEntry) space.take(a, txn, 200)) != null){
 					//System.out.println(entry.toString());
 					result.add(entry);
 				}
 				TimeEntry e = getLatest(result);
 				//System.out.println(result.toString());
 				txn.abort();
 				//leaseRenewalManager.cancel(trans.lease);
 				return e;
 			} catch (UnusableEntryException e) {
 				e.printStackTrace();
 			} catch (TransactionException e) {
 				e.printStackTrace();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			} 
 			
 		} catch (LeaseDeniedException e1) {
 			e1.printStackTrace();
 		} catch (RemoteException e1) {
 			e1.printStackTrace();
 		}
 		return null;
 	}
 	private TimeEntry getLatest(ArrayList<TimeEntry> result) {
 		
 		if (result.size() > 1) {
 			//System.out.println("to be compared: "+result.toString());	
 		Collections.sort(result, new Comparator<TimeEntry>(){
 			  public int compare(TimeEntry t1, TimeEntry t2) {
 				  //TimeEntry t3 = (TimeEntry) t1;
 				 //TimeEntry t4 = (TimeEntry) t2;
 				if (t1.time != null && t1.time != null)  
 					return t1.time.compareTo(t2.time);
 				return 1;
 			  }
 			  
 			});
 		return result.get(result.size()-1);
 
 		}
 		else if (result.size() == 1) {
 			return result.get(0);
 		}
 
 		return null;
 		
 	}
 
 	public void notifyAgent(String agent, Entry o) {
 		Term t = entryToTerm(o);
 		throwEvent((APLFunction) t, new String[]{agent});
 		System.out.println(t.toString());
 	}
 
 	public void notifyOrg() {
 		System.out.println("org notified ");
 		oopl.handleEvent(ar_state_change, false);
 	}
 	
     private void registerOrg() {
 		
 		OrgHandler handler;
 		try {
 			handler = new OrgHandler(this);
 			space.notify(new Position(), null,
 			        handler,
 			        3000000,
 			        new MarshalledObject(new String("position")));
 			space.notify(new Coin(), null,
 			        handler,
 			        3000000,
 			        new MarshalledObject(new String("coin")));
 			space.notify(new Cargo(), null,
 			        handler,
 			        3000000,
 			        new MarshalledObject(new String("cargo")));
 			space.notify(new Reading(), null,
 			        handler,
 			        3000000,
 			        new MarshalledObject(new String("reading")));
 			space.notify(new Points(), null,
 			        handler,
 			        3000000,
 			        new MarshalledObject(new String("points")));
 		} catch (RemoteException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (TransactionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 /*			theManager.renewFor(myReg.getLease(), Lease.FOREVER,
                 30000, new DebugListener());*/
 	}
     
     private void insertTestData()
     {
     	Cargo cargo = new Cargo(5, new Cell(10,10), 1);
     	Points p1 = new Points("a1", 1000, 1);
     	Points p2 = new Points("a2", 1000, 1);
     	Points p3 = new Points("a3", 1000, 1);
     	Points p4 = new Points("c1", 1000, 1);
     	Points p5 = new Points("t1", 1000, 1);
     	Reading r1 = new Reading(11, "a1", new Cell(11,11), 1, 50);
     	Reading r2 = new Reading(12, "a2", new Cell(1,11), 1, 60);
     	Reading r3 = new Reading(13, "a3", new Cell(11,1), 1, 10);
     	Coin c1 = new Coin(10, new Cell(15,15), "a1", 1);
     	Coin c2 = new Coin(20, new Cell(1,15), "a2", 1);
     	Coin c3 = new Coin(30, new Cell(15,1), "a3", 1);
     	
     	try {
 			space.write(cargo, null, Lease.FOREVER);
 			space.write(p1, null, Lease.FOREVER);
 			space.write(p2, null, Lease.FOREVER);
 			space.write(p3, null, Lease.FOREVER);
 			space.write(p4, null, Lease.FOREVER);
 			space.write(p5, null, Lease.FOREVER);
 			space.write(r1, null, Lease.FOREVER);
 			space.write(r2, null, Lease.FOREVER);
 			space.write(r3, null, Lease.FOREVER);
 			space.write(c1, null, Lease.FOREVER);
 			space.write(c2, null, Lease.FOREVER);
 			space.write(c3, null, Lease.FOREVER);
 			
 		} catch (RemoteException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (TransactionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     }
 
 
 
 
 	
 }
