 /*
 *			Copyright Author
 * (USE & RESTRICTIONS - Please read COPYRIGHT file)
 
 * Version		: XX.XX
 * Date		: 4/20/11 1:09 PM
 */
 
 // Default Package
 package DWLProject;
 
 import java.awt.Color;
 import java.util.List;
 
 import model.modeling.message;
 import view.modeling.ViewableAtomic;
 import GenCol.Pair;
 import GenCol.Queue;
 import GenCol.entity;
 
 public class DW_Coord_0_0 extends ViewableAtomic{
 
 	private static final message NULL_MESSAGE = new message();
 	//in port
 	private static final String EXT_CAT_IN = "ExtCatIn";
 	private static final String LOAD = "load";
 	private static final String STOP = "stop";
 	private static final String WRITER_DONE = "WriterDone";
 	//out port
 	private static final String EXT_CAT_OUT = "ExtCatOut";
 	private static final String CAT_OUT = "CatOut";
 	private static final String HALT = "halt";
 	//phases
     private static final String PASSIVE = "passive";
 	private static final String HALTING = "halting";
 	private static final String QUEUEING = "QueueingCat";
 	private static final String RECEIVING_EXT_CAT = "ReceivingExtCat";
 	private static final String SEND_EXT_CAT = "SendingExtCat";
 	private static final String WRITERS_DONE = "WritersDone";
 	
 	private static final String START = "start";
 	/**
 	 * Takes 1 unit of time to queue an incoming Cat File 
 	 */
 	private double QUEUEING_TIME = 1;
     
 	private message haltMessage;
 	private boolean startReceived;
 	private message outputMessage;
     private CatFile currentCatFile;
 	private Queue writersQueue;
 	private Queue catFileQueue;
 	private Queue workingCatFileQueue;
 	private Queue completedCatFileQueue;
 	private ExtCatFile currentExtCatFile;
 	private Queue pendingExtCatFileQueue;
 	private Queue workingExtCatFileQueue;
 
     // Add Default Constructor
     public DW_Coord_0_0(){
         this("Coordinator");
     }
 
     // Add Parameterized Constructors
     public DW_Coord_0_0(String name){
         super(name);
 // Structure information start
         // Add input port names
         addInport(EXT_CAT_IN);
         addInport(LOAD);
         addInport(STOP);
         addInport(WRITER_DONE);
 
         // Add output port names
         addOutport(EXT_CAT_OUT);
         addOutport(CAT_OUT);
         addOutport(HALT);
 
 //add test input ports:
         addTestInput(EXT_CAT_IN, new ExtCatFile("ExtCat1", 100, "L1", 2011, 10, 1D));
         addTestInput(EXT_CAT_IN, new ExtCatFile("ExtCat2", 100, "L1", 2011, 10, 1D));
         addTestInput(EXT_CAT_IN, new ExtCatFile("ExtCat3", 100, "L1", 2011, 10, 1D));
         addTestInput(EXT_CAT_IN, new ExtCatFile("ExtCat4", 80, "L2", 2011, 8, 1D));
         addTestInput(EXT_CAT_IN, new ExtCatFile("ExtCat5", 20, "L3", 2011, 2, 1D));
         addTestInput(LOAD, new entity("start"));
 
 // Structure information end
         writersQueue = new Queue();
         initialize();
     }
 
     // Add initialize function
     public void initialize(){
         super.initialize();
         phase = PASSIVE;
         sigma = INFINITY;
         startReceived = false;
         outputMessage = null;
         currentCatFile = null;
         catFileQueue = new Queue();
         workingCatFileQueue = new Queue();
         completedCatFileQueue = new Queue();
         workingExtCatFileQueue = new Queue();
         pendingExtCatFileQueue = new Queue();
         currentExtCatFile = null;
     }
 
     // Add external transition function
     @SuppressWarnings("unchecked")
     @Override
 	public void deltext(double e, message x){
     	Continue(e);
     	for (int i = 0 ; i < x.getLength(); i++) {
     		checkForStop(x, i);
     	}
     	if (phaseIs(RECEIVING_EXT_CAT)) {
     		queueExtCatFiles(x, e);
     	} 
     	if (phaseIs(PASSIVE)) {
     		checkForExtCat(x);
     	}
     	for (int i = 0 ; i < x.getLength(); i++) {
     		checkForStartOnLoadPort(x, i);
     	}
 		for (int i = 0; i < x.size(); i++) {
 			if (messageOnPort(x, WRITER_DONE, i)) {
 				entity value = x.getValOnPort(WRITER_DONE, i);
 				Pair pair = (Pair) value;
 				writersQueue.add(pair.getKey());
 				ExtCatFile aCatFile = (ExtCatFile) pair.getValue();
 				CatFile parentCatFile = aCatFile.getParentCatFile();
 				if (parentCatFile.isCompleted()) {
 					if (!completedCatFileQueue.contains(parentCatFile)) {
 						completedCatFileQueue.add(parentCatFile);
 						currentCatFile = null;
 						outputMessage = new message();
 		        		Pair aPair = new Pair(parentCatFile.getName(), parentCatFile);
 		        		outputMessage.add(makeContent(CAT_OUT, aPair));
 					}
 				}
 				holdIn(SEND_EXT_CAT, 0);
 				this.setBackgroundColor(Color.MAGENTA);
 			}
 		}
 		
     	if ((phaseIs(PASSIVE) || phaseIs(RECEIVING_EXT_CAT))
     			&& pendingExtCatFileQueue.isEmpty()
     			&& startReceived) {
    		currentExtCatFile = null;
     		holdIn(SEND_EXT_CAT, 0);
 			this.setBackgroundColor(Color.MAGENTA);
     	}
     }
 
 	@SuppressWarnings("unchecked")
 	private void sendExtCatToWriters() {
 		if (outputMessage == null) {
 			outputMessage = new message();
 		}
		if (currentCatFile == null) {
			while (!workingCatFileQueue.isEmpty() && (workingExtCatFileQueue.size() < writersQueue.size())) {
				currentCatFile = (CatFile) workingCatFileQueue.remove();
 	    	    if (currentCatFile.getProcessedExtCatList().isEmpty()) {
 	    	    	workingExtCatFileQueue.addAll(currentCatFile.getExtCatList());
 	    	    }
 			}
 		}
 		while (!writersQueue.isEmpty() && !workingExtCatFileQueue.isEmpty()) {
 			Writer_0_0 aWriter = (Writer_0_0)writersQueue.remove();
 			ExtCatFile aCatFile = (ExtCatFile) workingExtCatFileQueue.remove();
 			Pair aPair = new Pair(aWriter.getName(), aCatFile);
 			outputMessage.add(makeContent(EXT_CAT_OUT, aPair));
 		}
 	}
 
 	/**
      * Checks if the loading of ExtCat should start
      * 
      * @param x
      * @param i
      */
     private void checkForStartOnLoadPort(message x, int i) {
     	if (messageOnPort(x, LOAD, i)) {
     		entity value = x.getValOnPort(LOAD, i);
     		if (value.getName().equals(START)) {
     			startReceived = true;
     		}
     	}
 	}
 
 	/**
      * Queues any <code>ExtCatFile</code> that comes
      * in while the <tt>DW</tt> is <code>BUSY</code>
      * @param x
      * @param e 
      */
     @SuppressWarnings("unchecked")
 	private void queueExtCatFiles(message x, double e) {
     	for (int i = 0; i < x.getLength(); i++) {
     		if (messageOnPort(x, EXT_CAT_IN, i)) {
     			entity value = x.getValOnPort(EXT_CAT_IN, i);
     			if (value instanceof ExtCatFile) {
     				ExtCatFile aCatFile = (ExtCatFile) value;
     				currentExtCatFile.updateTimeToRegister(e);
     				holdIn(QUEUEING, QUEUEING_TIME);
         			this.setBackgroundColor(Color.MAGENTA);
     				pendingExtCatFileQueue.add(aCatFile);
     			} else {
     				System.out.println("Not an Ext Cat File: " + value.getName());
     				holdIn(PASSIVE, INFINITY);
         			this.setBackgroundColor(Color.GRAY);
     			}
     		}
     	}
 	}
 
 	/**
      * Picks up all <code>ExtCatFile</code> that are on
      * input port <code>LOAD</code>
      * 
      * @param x
      */
     @SuppressWarnings("unchecked")
 	private void checkForExtCat(message x) {
     	for (int i = 0; i < x.getLength(); i++) {
     		if (messageOnPort(x, EXT_CAT_IN, i)) {
     			entity value = x.getValOnPort(EXT_CAT_IN, i);
     			if (value instanceof ExtCatFile) {
     				ExtCatFile aCatFile = (ExtCatFile) value;
     				holdIn(RECEIVING_EXT_CAT, aCatFile.getTimeToRegister());
         			this.setBackgroundColor(Color.MAGENTA);
     				CatFile parent = aCatFile.getParentCatFile();
     				if (!catFileQueue.contains(parent)) {
         				catFileQueue.add(parent);
         				workingCatFileQueue.add(parent);
     				}
     				currentExtCatFile = aCatFile;
     			}
     			else {
     				System.out.println("Not an Ext Cat File: " + value.getName());
     				holdIn(PASSIVE, INFINITY);
         			this.setBackgroundColor(Color.GRAY);
     			}
     		}
     	}
 	}
 
 	/**
      * Checks if there is a halt command from the Experimental Frame
      * @param x
      * @param i
      */
     private void checkForStop(message x, int i) {
     	if (messageOnPort(x, STOP, i)) {
     		entity value = x.getValOnPort(STOP, i);
     		if (value.getName().equals(STOP)) {
     			holdIn(HALTING, 1);
     			this.setBackgroundColor(Color.MAGENTA);
     			haltMessage = new message();
     			haltMessage.add(makeContent(HALT, new entity(HALT)));
     		}
     	}
 	}
 
 	// Add internal transition function
     @SuppressWarnings("unchecked")
     @Override
 	public void deltint(){
     	if (phaseIs(HALTING)) {
     		passivateIn(PASSIVE);
 			this.setBackgroundColor(Color.GRAY);
     	} else if (phaseIs(QUEUEING)) {
     		double timeLeftForRegistration = currentExtCatFile.getTimeToRegister();
     		if (timeLeftForRegistration > 0D) {
     			holdIn(RECEIVING_EXT_CAT, timeLeftForRegistration);
     			this.setBackgroundColor(Color.MAGENTA);
     		} else {
     			currentExtCatFile = null;
     			holdIn(PASSIVE, INFINITY);
     			this.setBackgroundColor(Color.GRAY);
     		}
 		} else if (phaseIs(RECEIVING_EXT_CAT)) {
 			if (pendingExtCatFileQueue.size() > 0) {
 				currentExtCatFile = (ExtCatFile) pendingExtCatFileQueue.remove();
 				holdIn(RECEIVING_EXT_CAT,
 						currentExtCatFile.getTimeToRegister());
 				CatFile parent = currentExtCatFile.getParentCatFile();
 				if (!catFileQueue.contains(parent)) {
 					catFileQueue.add(parent);
 					workingCatFileQueue.add(parent);
 				}
 				this.setBackgroundColor(Color.MAGENTA);
 			} else if (workingCatFileQueue.isEmpty()){
 				passivateIn(PASSIVE);
 				currentExtCatFile = null;
 				this.setBackgroundColor(Color.GRAY);
 			} else if (startReceived) {
 				holdIn(SEND_EXT_CAT, 0);
				currentExtCatFile = null;
 				this.setBackgroundColor(Color.MAGENTA);
 			}
 		} else if (phaseIs(WRITERS_DONE)) {
 			passivateIn(PASSIVE);
 			this.setBackgroundColor(Color.GRAY);
 		} else if (phaseIs(SEND_EXT_CAT)) {
 			if (catFileQueue.size() == completedCatFileQueue.size()) {
 				holdIn(WRITERS_DONE, 0);
 				this.setBackgroundColor(Color.MAGENTA);
 			} else {
 				holdIn(PASSIVE, INFINITY);
     			this.setBackgroundColor(Color.GRAY);
 			}
 		}
     }
 
     // Add confluent function
     @Override
     public void deltcon(double e, message x){
     	deltint();
     	deltext(0, x);
     }
 
     // Add output function
     @Override
     public message out(){
     	message theMessage = NULL_MESSAGE;
     	if (phaseIs(HALTING)) {
     		theMessage = haltMessage;
     	}
     	if (phaseIs(SEND_EXT_CAT)) {
     		sendExtCatToWriters();
     		theMessage = outputMessage;
     	}
     	outputMessage = null;
     	return theMessage;
     }
 
 	@Override
 	public void showState() {
 		super.showState();
 		System.out.println("The CatFile Queue has " + catFileQueue.size() + " elements");
 		System.out.println("The Queue contains: " + catFileQueue);
 		System.out.println("The Completed Cat Queue has " + completedCatFileQueue.size() + " elements");
 		System.out.println("The Queue contains: " + completedCatFileQueue);
 		System.out.println("The Writers Queue has " + writersQueue.size() + " elements");
 		System.out.println("The Queue contains: " + writersQueue);
 	}
 	
 	@Override
 	public String getTooltipText() {
 		StringBuilder myBuilder = new StringBuilder();
 		myBuilder.append("\n");
 		myBuilder.append("Registering ExtCatFile: ");
 		myBuilder.append(currentExtCatFile != null ? currentExtCatFile.getName() : "null");
 		myBuilder.append("\n");
 		myBuilder.append("Writers Queue: ");
 		myBuilder.append(writersQueue);
 		myBuilder.append("\n");
 		myBuilder.append("Cat File Queue: ");
 		myBuilder.append(catFileQueue);
 		myBuilder.append("\n");
 		myBuilder.append("Completed Cat File Queue: ");
 		myBuilder.append(completedCatFileQueue);
 		myBuilder.append("\n");
 		myBuilder.append("Ext Cat File Queue: ");
 		myBuilder.append(workingExtCatFileQueue);
 		return super.getTooltipText() + myBuilder.toString();
 	}
 
     /**
      * @return <code>CAT_OUT</code> port
      */
 	public static String getExtCatOut() {
 		return EXT_CAT_OUT;
 	}
 
 	public static String getWriterDone() {
 		return WRITER_DONE;
 	}
 
 	/**
 	 * Set the writers into the queue
 	 * @param writerList
 	 */
 	@SuppressWarnings("unchecked")
 	public void setWriters(List<Writer_0_0> writerList) {
 		writersQueue.addAll(writerList);
 	}
 }
