 package geoSenseMW;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.lang.reflect.InvocationTargetException;
 import java.rmi.*; 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.Date;
 
 import net.jini.core.transaction.TransactionException;
 import oopl.DistributedOOPL;
 import oopl.GUI.GUI;
 import tuplespace.*;
 import tuplespace.Prohibition;
 import apapl.Environment;
 import apapl.data.*;
 import aplprolog.prolog.IntHarvester;
 import aplprolog.prolog.Prolog;
 import aplprolog.prolog.builtins.ExternalActions;
 import aplprolog.prolog.builtins.ExternalTool;
 
 import org.openspaces.core.GigaSpace;
 import org.openspaces.core.space.UrlSpaceConfigurer;
 import com.j_spaces.core.IJSpace;
 import com.gigaspaces.events.DataEventSession;
 import com.gigaspaces.events.EventSessionConfig;
 import com.gigaspaces.events.EventSessionFactory;
 
 
 /*
  * Extends Environment to be compatible with 2APL and implements ExternalTool to 
  * be compatible with my Prolog engine. 
  */
 public class EnvGeoSense  extends Environment implements ExternalTool{
 	//public static JavaSpace space; // shared data
 	public int clock = 0;
 	public DistributedOOPL oopl; // norm interpreter
 	public static String TYPE_STATUS="status", TYPE_PROHIBITION="prohibition", 
 		TYPE_OBLIGATION="obligation", TYPE_READINGREQ = "readingRequest",TYPE_READING = "reading",TYPE_INVESTIGATE = "investigate",TYPE_CARGO = "cargo",TYPE_COIN = "coin",TYPE_POINTS = "points",
 			TYPE_OBJECT="object", TYPE_INVENTORY="inventory", NULL="null"; // for matching string with class type
 	public int[] ar_true, ar_null, ar_state_change, ar_false; // precalculated IntProlog data 
 	public int INT_TUPLE=0, INT_POINT=0, INT_NULL=0;
 	public APAPLTermConverter converter; // Converts between IntProlog and 2APL
 	private Prolog2Java p2j;
 	private GigaSpace space;
 	private DataEventSession session;
 	public static String[] agents = {"a1", "a2", "a3", "t1", "c1"};
 	
 	/*
 	 * Just for testing.
 	 */
     public static void main(String[] args){ 
 		EnvGeoSense st = new EnvGeoSense();
     }
     
     /*
      * A kickoff function to begin the system.
      */
     public void initializeGS() throws RemoteException {
     	try {
         	File file = new File("./log/"+ new Date(System.currentTimeMillis()) +".log");
 
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
     	//IJSpace ispace = new UrlSpaceConfigurer("jini://*/*/myGrid").space();
         // use gigaspace wrapper to for simpler API
         //this.space = new GigaSpaceConfigurer(ispace).gigaSpace();
         this.space=DataGridConnectionUtility.getSpace("myGrid");
         //space.clear(null);
         dumpGSdata();
         EventSessionConfig config = new EventSessionConfig();
         config.setFifo(true);
         //config.setBatch(100, 20);
         IJSpace ispace = new UrlSpaceConfigurer("jini://*/*/myGrid").space();
         EventSessionFactory factory = EventSessionFactory.getFactory(ispace);
         session = factory.newDataEventSession(config); 
     }
     
 	
 		
 		public void initializeOOPL() throws RemoteException {
 			registerOrg();
 			p2j = new Prolog2Java();
 			// Starting the normative system:
 			oopl = new DistributedOOPL(); // Create interpreter object
 			GUI g = new GUI(oopl,"SpaceOrg.2opl","OOPL",null,6677); // Make a GUI for the interpreter
 			converter = new APAPLTermConverter(oopl.prolog); // Make a term converter (relies on Prolog engine for string storage)
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
 			
 		
 	} 
 
 	/*
 	 * Both used for increasing or just reading the clock. 
 	 */
 	public synchronized int updateClock(int amount){
 		//if(amount>0)  oopl.handleEvent(ar_state_change, false); // clock ticked so deadlines can be passed, handleEvent causes the interpreter to check the norms
 		Time t = new Time();
 		TimeEntry e = getLast(t);
 		//System.out.println(e.toString());
 		if (e != null) {
 			
 		this.clock = ((Time) e).clock;
 		return ((Time) e).clock;
 		}
 		return 0;
 	}
 	
 	/*
 	 * Constructor immediately initializes the space. 
 	 */
 	public EnvGeoSense(){
 		super();
 		try { initializeGS(); initializeOOPL();} catch (Exception e) { e.printStackTrace(); }
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
 	@Override
 	public void registerActions(Prolog p) { 
 		oopl.prolog.builtin.external.registerAction("read", this, ExternalActions.INTAR, ExternalActions.INTAR);
 		oopl.prolog.builtin.external.registerAction("write", this, ExternalActions.INTAR, ExternalActions.INTAR);
 		oopl.prolog.builtin.external.registerAction("notifyAgent", this, ExternalActions.INTAR, ExternalActions.INTAR);
 		oopl.prolog.builtin.external.registerAction("clock", this, ExternalActions.INTAR, ExternalActions.INTAR);
 	}
 
 	/*
 	 * Handle a call from the organization (actually: from Prolog). These calls are in IntProlog datatypes (int arrays). 
 	 * ExternalActions ea is a part of the Prolog engine which reads returns ea.intResult after the
 	 * external call.
 	 */
 	@Override
 	public void handleCall(int[] call, ExternalActions ea, int returnType) {  
 		/*
 		 * For JavaSpace calls: the integer array is first transformed to an Entry object, then passed
 		 * to the JavaSpaced using the appropriate method call, and then the result is converted back
 		 * to an integer array.
 		 */
 		if(call[1] == oopl.prolog.strStorage.getInt("read")){
 			try {
 
 				TimeEntry a = createEntry(call);
 				//System.out.println(a.toString());
 				TimeEntry e = getLast(a);
 				//System.out.println(e.toString());
 				ea.intResult = entryToArray(e);
 			} catch (Exception e) {e.printStackTrace();}
 		} else if(call[1] == oopl.prolog.strStorage.getInt("write")){
			//System.out.println("write");
 			try {
 				long lease = get_number(call,oopl.prolog.harvester.scanElement(call, 3, false, false)+1);
 				//if(lease <= 0) lease = Lease.FOREVER;
 				
 				TimeEntry e = createEntry(call);
 				if (e.getTime() == null)
 					e.setTime();
 				if (e.getClock() == null) {
 					//updateClock(0);
 					e.setClock(clock);
 				}
 				System.out.println("Organization writes: "+e.toString());
 				space.write(e);
 				//System.out.println(e+"  "+lease+"   "+Lease.FOREVER);
 				ea.intResult = ar_true;
 			} catch (Exception e) {e.printStackTrace();}
 	    /*
 	     * The next case throws towards the agent an event that its status is changed.
 	     */
 		} else if(call[1] == oopl.prolog.strStorage.getInt("notifyAgent")){ // notifyAgent(name,obligation(blabla)).
 			
 			String recipient = oopl.prolog.strStorage.getString(call[4]);
 			APLFunction event = (APLFunction)converter.get2APLTerm(Arrays.copyOfRange(call, 6, call.length));
 			TimeEntry e = createEntry(recipient, event);
 			if (e.getTime() == null)
 				e.setTime();
 			if (e.getClock() == null) {
 				//updateClock(0);
 				e.setClock(clock);
 			}
 			System.out.println("Organization notifies agent (write): "+e.toString());
 			space.write(e);
 			
 			//throwEvent(event, new String[]{recipient});
 			ea.intResult = ar_true;
 		} else if(call[1] == oopl.prolog.strStorage.getInt("clock")){ // Read the clock
 			int[] r = new int[3];
 			addNumber(r, 0, updateClock(0)); // Use updateClock because of synchronization
 			ea.intResult = r;
 		}
 	}
 
 
 	@Override
 	public void handleCall(Object[] call, ExternalActions p, int returnType) { }
 	
 	/*
 	 * Create an entry object form an integer array. Perhaps we want to replace this with
 	 * something like createEntry(oopl.prolog.toPrologString(call)).
 	 */
 	public TimeEntry createEntry(int[] call) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, ClassNotFoundException, NoSuchMethodException{ // e.g.: read(tuple(name,point(2,4),20),0)
 		//System.out.println(oopl.prolog.arStr(call));
 		return p2j.parseTerm(call, converter, oopl);
 		
 	}
 	
 
 	/*
 	 * Convert an entry to an array. Can also be done by calling the prolog compiler and give
 	 * it e.toPrologString() as an argument.
 	 */
 	public int[] entryToArray(TimeEntry e){
 		if(e == null){
 			int[] r = new int[3];
 			addPredicate(r, 0, oopl.prolog.strStorage.getInt("null"), 0);
 			return r;
 				
 		} 
 		else {
 			return e.toIntArray(oopl);
 		}
 	
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
 		array[cursor] = IntHarvester.PREDICATE;
 		array[cursor+1] = name;
 		array[cursor+2] = arity;
 	}
 	/*
 	 * Add a number to an array.
 	 */
 	public void addNumber(int[] array, int cursor, int number){
 		array[cursor] = IntHarvester.NUMBER;
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
 	
 	
 	//////////////////////// 2APL/2OPL from APLFunction to TimeEntry AND JAVASPACE
 
 	/**
 	 * Convert a Prolog predicate to a suitable JavaSpace datatype.
 	 * @param sAgent The agent that calls the method (important for the name in the status).
 	 * @param call The predicate from the call.
 	 * @return The entry representation of the predicate.
 	 */
 	public TimeEntry createEntry(String sAgent, APLFunction call){ 
 		
 		//System.out.print("from/for agent " + sAgent + "  ");
 		//System.out.println(call.toString());
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
 			//System.out.print("from/for agent " + sAgent + "  ");
 			//System.out.println(call.toString());
 			return new ActionRequest(sAgent,"reading",c,clock); // Create Tuple
 		}
 		else if(call.getName().equals(TYPE_READING)){ // Prolog format: reading(position(X,Y))
 			Cell c = null;
 			if(call.getParams().get(0) instanceof APLFunction){ // null is APLIdent  
 				APLFunction point = (APLFunction) call.getParams().get(0); // Get the point coordinations TODO: type check the arguments
 				int pointX = ((APLNum)point.getParams().get(0)).toInt(); // Get the position
 				int pointY = ((APLNum)point.getParams().get(1)).toInt();
 				c = new Cell(pointX,pointY);
 			}
 			System.out.print("from/for agent " + sAgent + "  ");
 			System.out.println(call.toString());
 			System.out.println(new Reading(sAgent,c));
 			return new Reading(sAgent,c); // Create Tuple
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
 			if(call.getParams().get(2) instanceof APLIdent) agent = ((APLIdent)call.getParams().get(2)).toString(); // The health meter
 			
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
 			
 			//Integer clock = null; // if health is null (which is ident) it stays also in java null
 			//if(call.getParams().get(1) instanceof APLNum) clock = ((APLNum)call.getParams().get(1)).toInt(); // The health meter
 			//Integer health = null; // if health is null (which is ident) it stays also in java null
 			//if(call.getParams().get(2) instanceof APLNum) health = ((APLNum)call.getParams().get(2)).toInt(); // The health meter
 		
 			return new Points(sAgent); // Create Tuple
 		}
 		else if(call.getName().equals(TYPE_PROHIBITION)){ // Prolog format: status(position(1,4),30) 
 			Prohibition p = null;
 			//System.out.println("create entry prohibition "+call.getParams().toString());
 			
 		
 			if(call.getParams().get(0) instanceof Term){ // null is APLIdent  
 				//APLFunction point = (APLFunction) call.getParams().get(0); // Get the point coordinations 
 				String s1 = call.getParams().get(0).toString();// Get the position
 				String s2 = call.getParams().get(1).toString();
 				p = new Prohibition(sAgent, s1, s2, clock);
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
 				
 				o = new Obligation(sAgent, s1, s3, deadline, clock);
 				//System.out.println(s2);
 			}
 			return o; // Create Tuple
 		} 
 
 		return null;
 	}
 	
 	//agent use
 	//Possibly move to Tuple classes
 	public Term entryToTerm(TimeEntry timeEntry){ 
 		
 		if(timeEntry instanceof Points){ // points(Points)
 			Points points = (Points) timeEntry; 
 			return new APLFunction("points", new Term[]{new APLNum(points.value)}); // construct result
 		} 
 		else if(timeEntry instanceof Time){ // clock(Clock)
 			Time time = (Time) timeEntry; 
 			return new APLFunction("clock", new Term[]{new APLNum(time.clock)}); // construct result
 		} 
 		else if(timeEntry instanceof Reading){ // reading(at(X,Y),Value,Agent,Clock)
 			Reading reading = (Reading) timeEntry;
 			Term term = constructTerm("at("+reading.cell.x+","+reading.cell.y+")");
 			return new APLFunction("reading", new Term[]{term,new APLNum(reading.value.intValue()),new APLIdent(reading.agent),new APLNum(reading.clock)}); // construct result
 		} 
 		else if(timeEntry instanceof Obligation){ //obligation(Goal, Deadline, Sanction)
 			Obligation o = (Obligation) timeEntry; 
 			String name = o.agent;
 			if(name==null)name="null"; 
 			Term posTerm = new APLIdent("null");
 			Term posTerm1 = new APLIdent("null");
 			Term posTerm2 = new APLIdent("null");
 			//all possible obligations
 
 			posTerm = constructTerm(o.obligation);
 			
 			if(o.deadline!=null){
 				posTerm1 = new APLNum(o.deadline);
 			}
 			if(o.sanction!=null){
 				posTerm2 = constructTerm(o.sanction);
 			}
 			return new APLFunction("obligation", new Term[]{new APLList(posTerm),posTerm1,new APLList(posTerm2)});
 		}
 		else if(timeEntry instanceof Prohibition){ //prohibition(State,Sanction)
 			Prohibition o = (Prohibition) timeEntry; 
 			String name = o.agent;
 			if(name==null)name="null"; 
 			Term posTerm = new APLIdent("null");
 			Term posTerm2 = new APLIdent("null");
 
 			posTerm = constructTerm(o.prohibition);
 
 			if(o.sanction!=null){
 				posTerm2 = constructTerm(o.sanction);
 			}
 			return new APLFunction("prohibition", new Term[]{new APLList(posTerm),new APLList(posTerm2)});
 		}
 		return new APLIdent("null");
 	}
 	
 	private Term constructTerm(String term) {
 		term = term.replace("[","");
 		term.replace("]","");
 		
 		int tx = term.indexOf("(");
 		String s = term.substring(0, tx).trim();
 		
 		Term[] t = new Term[10];
 		int i = term.indexOf(",");
 		int index = 0;
 		if (i == -1) {
 			return new APLFunction(term);
 		}
 		else {
 			String x = term.substring(s.length() + 1, i).trim();
 			t[index] = numOrIdent(x);
 			index++;
 		}
 		while (term.indexOf(",", i+1) > 0) {
 			int j = term.indexOf(",", i+1);
 			String y = term.substring(i+1, j).trim();
 			t[index] = numOrIdent(y);
 			i=j;
 			index++;
 		}
 		int j = term.indexOf(")");
 		String y = term.substring(i+1, j).trim();
 		t[index] = numOrIdent(y);
 		Term posTerm = new APLFunction(s, t);
 		return posTerm;
 		
 	}
 
 
 	private Term numOrIdent(String x) {
 		Term xt;
 		Integer ix = Integer.getInteger(x);
 		if (ix != null) {
 			xt = new APLNum(ix);
 		}
 		else {
 			xt = new APLIdent(x);
 		}
 		return xt;
 	}
 
 
 	//from agent program
 	public Term read(String sAgent, APLFunction call, APLNum timeOut){
 	
 		try{ 
 			TimeEntry te = createEntry(sAgent,call);
 			System.out.println("Agent reads: "+te.toString());
 			TimeEntry te1 = space.read(te, 2000);
 			System.out.println("Agent finds: "+te1.toString());
 			return entryToTerm(te1); 
 		} catch(Exception e){ e.printStackTrace(); return new APLIdent("null"); }
 	}
 	
 	public Term write(String sAgent, APLFunction call, APLNum lease){ 
 		//System.out.println("write " + sAgent);
 		try{
 
 			TimeEntry e = createEntry(sAgent,call);
 			if (e.getTime() == null)
 				e.setTime();
 			if (e.getClock() == null) {
 				//updateClock(0);
 				e.setClock(clock);
 			}
 			//System.out.println("Agent writes: "+e.toString());
 			space.write(e);
 
 			return new APLIdent("true");
 		}catch (Exception e){ e.printStackTrace(); return new APLIdent("null"); }
 	}
 	
 
 	/*
 	 * ENVIRONMENT OVERRIDES
 	 */
 	@Override
 	public void addAgent(String sAgent) {
 		System.out.println("register " + sAgent);
 
 			register(sAgent);
 		
 	}
 	
 	private void register(String agent) {
 		
 		AgentHandler handler;
 		try {
 			handler = new AgentHandler(this, agent);
 			try {
 				session.addListener(new Prohibition(agent), handler);
 				session.addListener(new Obligation(agent), handler); 
 				session.addListener(new Points(agent), handler);
 				session.addListener(new Reading(), handler);
 				session.addListener(new Time(), handler);
 			} catch (TransactionException e) {
 				e.printStackTrace();
 			} 
 			
 		} catch (RemoteException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}  catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 	}
 	
 	private TimeEntry getLast(TimeEntry a) {
 		try {
 			TimeEntry[] result = space.readMultiple(a);
 				if (result.length > 0)
 					return getLatest(result);
 			} catch (Exception e) {
 				e.printStackTrace();
 			} 
 
 		return null;
 	}
 	
 	private TimeEntry getLatest(TimeEntry[] result) {
 		Arrays.sort(result, new Comparator<TimeEntry>(){
 			@Override
 			public int compare(TimeEntry t1, TimeEntry t2) {
 				return t1.getTime().compareTo(t2.getTime());
 			}
 
 		});
 		System.out.println("latest      "+result[result.length-1]);
 		return result[result.length-1];
 	}
 
 	public void notifyAgent(String agent, TimeEntry e) {
 		Term t = entryToTerm(e);
 		if (t.toString() == "null")
 			return;
 		throwEvent((APLFunction) t, new String[]{agent});
 		System.out.println("Event sent to agent      "+agent+ " " +t.toString());
 		
 	}
 	public void notifyAgent(String agent, ArrayList<TimeEntry> r) {
 		for (TimeEntry te : r) {
 			notifyAgent(agent,te);
 		}
 	}
 
 	public void notifyOrg(TimeEntry te) {
 		System.out.println("org notified found "+te.toString());
 		System.out.println("org notified sent "+te.toPrologString());
 		//int[] OOPLformat = te.toIntArray(oopl);
 		//oopl.handleEvent(ar_state_change, false);
 		oopl.handleEvent(oopl.getProlog().mp.parseFact(te.toPrologString(),oopl.getProlog().strStorage,false),false);
 	}
 	
 	 private void registerOrg() throws RemoteException {
 			
 			OrgHandler handler = new OrgHandler(this);
 			
 			try {
 				for (int i=0; i<agents.length;i++) {
 					session.addListener(new Position(agents[i]), handler); 
 				}
 				session.addListener(new Cargo(), handler); 
 				session.addListener(new Reading(), handler); 
 				session.addListener(new Time(), handler); 
 				session.addListener(new Coin(), handler); 
 			} catch (RemoteException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (TransactionException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} 
 	 }
     
    /* private void insertTestData()
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
     	Time t1 = new Time(0);
     	Prohibition px = new Prohibition("t1","[at(5, 5, t1)]", "[reduce_300(t1)]",0);
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
 			space.write(t1, null, Lease.FOREVER);
 			space.write(px, null, Lease.FOREVER);
 			
 		} catch (RemoteException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (TransactionException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
     	
     
     	
     }
 
 */
 
     private void dumpGSdata() {
     	TimeEntry entry;
         //Entry temp = new Time();
     	System.out.println("-------------------------last log tuples start--------------------------------");
     	ArrayList<TimeEntry> result = new ArrayList<TimeEntry>();
 		while ((entry = (TimeEntry) space.take(null)) != null){
 			System.out.println(entry.toString());
 			result.add(entry);
 		}
 System.out.println("-------------------------last log tuples end----------------------------------");
 return;
 	}
 
 	
 }
