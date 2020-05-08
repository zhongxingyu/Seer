 /**
  Trains Protocol: Middleware for Uniform and Totally Ordered Broadcasts
  Copyright: Copyright (C) 2010-2012
  Contact: michel.simatic@telecom-sudparis.eu
 
  This library is free software; you can redistribute it and/or
  modify it under the terms of the GNU Lesser General Public
  License as published by the Free Software Foundation; either
  version 3 of the License, or any later version.
 
  This library is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  Lesser General Public License for more details.
 
  You should have received a copy of the GNU Lesser General Public
  License along with this library; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
  USA
 
  Developer(s): Stephanie Ouillon
  */
 
 package trains;
 
 /**  
  * Main interface to access native methods of TrainsProtocol.
  * It contains methods wrappers for the native functions of interface.c and some additionnal
  * methods which are necessary to perform the trains protocol. 
  *
  * @author Stephanie Ouillon
  */
 
 public class Interface {
 		 
 	/** 
 	 * Maximum number of members in the protocol
 	 */
 	private static int max_memb;
 	
 	
 	/**
 	 * Address of the current process in the train protocol
 	 */
 	private static int myAddress;
 	
 	
 	/**
 	 * Native function, see {@link Interface#trInit(int, int, int, int, String, String) JtrInit()}
 	 * @param trainsNumber
 	 * @param wagonLength
 	 * @param waitNb
 	 * @param waitTime
 	 * @param callbackCircuitChange
 	 * @param callbackUtoDeliver
 	 * @return 0 upon successful completion, or -1 if an error occurred
 	 */
 	private native int trInit(int trainsNumber, int wagonLength, int waitNb, int waitTime,
 			String callbackCircuitChange, 
 			String callbackUtoDeliver);
 
 	
 	/**
 	 * Native function, see {@link #JtrError_at_line(int, int) JtrError_at_line()}
 	 * @param status
 	 * @param errnum
 	 */
 	private native void trError_at_line(int status, int errnum);//, const char *filename, 
 	//		unsigned int linenum, const char *format); 
 	
 	
 	/**
 	 * Native function, see {@link #JtrPerror(int) JtrPerror()}
 	 * @param errnum
 	 */
 	private native void trPerror(int errnum);
 	
 	
 	/**
 	 * Native function, see {@link #JtrTerminate() JtrTerminate()}
 	 * @return always 0
 	 */
 	private native int trTerminate();
 	
 	
 	/**
 	 * Native function, see {@link #Jnewmsg(int, byte[]) Jnewmsg()}.
 	 * @param payloadSize
 	 * @param payload
 	 * @return 0 on success
 	 */
 	private native int newmsg(int payloadSize, byte[] payload);
 	
 	
 	/**
 	 * Native function, see {@link #JutoBroadcast(Message) JutoBroadcast()}.
 	 * @param msg
 	 * @return 0 on success
 	 */
 	private native int utoBroadcast(Message msg);
 	
 	
 	/**
 	 * Native function, see {@link #JgetMAX_MEMB() JgetMAX_MEMB()}.
 	 * @return MAX_MEMB
 	 */
 	private static native int getMAX_MEMB();
 	
 	
 	/**
 	 * Native function, see {@link #JgetMyAddress() JgetMyAddress()}.
 	 * @return myAddress
 	 */
 	private static native int getMyAddress();
 	
 	
 	/**
 	 * Native static initializer for the MessageHeader class (caching IDs).
 	 * See {@link #initIDs() initIDs()}.  
 	 */
 	private static native void initIDsMessageHeader();
 	
 	
 	/**
 	 * Native static initializer for the Message class (caching IDs).
 	 * See {@link #initIDs() initIDs()}.  
 	 */
 	private static native void initIDsMessage();

 	public native void dumpCountersData(byte[] data);
 	
 	
 	/**
 	 * Native static initializer for the CircuitView class (caching IDs).
 	 * See {@link #initIDs() initIDs()}.  
 	 */
 	private static native void initIDsCircuitView();	
 
 	
 	/**
 	 * Loads the native library "trains" and performs some initializing stuff through
 	 * {@link #initIDs() initIDs()} and {@link #JgetMAX_MEMB() JgetMAX_MEMB}.
 	 * 
 	 * @return Interface
 	 */
 	public static Interface trainsInterface(){
 		System.loadLibrary("trains");
 		Interface trainsInterface = new Interface();
 		initIDs();
 		Interface.max_memb = JgetMAX_MEMB();
 		return trainsInterface;
 	}
 	
 	
 	/**
 	 * Calls native functions to cache field and method IDs which are repeatedly used.
 	 * One instance of MessageHeader, Message and CircuitView each is created to be used both
 	 * in Java and C.
 	 * 
 	 * These functions are declared static so that looking up the IDs and instantiating 
 	 * the objects happen only once when the trains library is loaded.
 	 * Using such static initializers have several advantages over caching IDs at the point of use.
 	 * There is no need to check whether an ID has already been cached or not. This avoids duplication 
 	 * caching and checking. Also, in case a class is unloaded and later reloaded, the cached IDs will 
 	 * automatically recalculated.
 	 * 
 	 * @return void
 	 */
 	public static void initIDs(){
 		initIDsMessageHeader();
 		initIDsMessage();
 		initIDsCircuitView();
 	}
 	
 	
 	/**
 	 * Returns the value of the native global variable MAX_MEMB, that will be
 	 * put in {@link #max_memb max_memb}.
 	 * 
 	 * @return the value of MAX_MEMB (native variable)
 	 */
 	public static int JgetMAX_MEMB(){
 		return getMAX_MEMB();
 	}
 	
 	
 	/**
 	 * Returns the value of the native global variable {@link #myAddress myAddress}.
 	 * 
 	 * @return the value of {@link #myAddress myAddress}
 	 */
 	public static int JgetMyAddress(){
 		return getMyAddress();
 	}
 	
 	
 	/**
 	 * Return the value of {@link #max_memb max_memb}.
 	 * This is a function to be used by the Java application to access this value.
 	 * 
 	 * @return the value of {@link #max_memb max_memb}
 	 */
 	public static int getMax_Memb(){
 		return Interface.max_memb;
 	}
 	
 	
 	/**
 	 * 
 	 * Initializes the trains protocol middleware. This method needs to be called before any other
 	 * actions in the trains protocol (sending or receiving messages). This method wraps the native function
 	 * trInit().
 	 * 
 	 * @param trainsNumber the number of trains on the circuit; when set to 0, uses the default value 1
 	 * @param wagonLength the length of the wagons in the train; when set to 0, uses the default value 32KB
 	 * @param waitNb the number of time to wait; when set to 0, uses the default value 10
 	 * @param waitTime time to wait (in microsecond); when set to 0, uses the default value 2
 	 * @param callbackCircuitChange name of the callback class to be called when there is a circuit change (Arrival or departure of a process)
 	 * @param callbackUtoDeliver name of the callback class be called when a message can be uto-delivered by the trains protocol
 	 * 
 	 * @return 0 upon successful completion, or -1 if an error occurred
 	 */
 	public int JtrInit(int trainsNumber, int wagonLength, int waitNb, int waitTime,
 			String callbackCircuitChange, 
 			String callbackUtoDeliver){
 		
 		int exitcode = this.trInit(trainsNumber, wagonLength, waitNb, waitTime,
 				callbackCircuitChange, callbackUtoDeliver);	
 
 		return exitcode;
 	}
 	
 	
 	/**
 	 * Never used.
 	 * 
 	 * @param status
 	 * @param errnum
 	 */	
 	public void JtrError_at_line(int status, int errnum){//, const char *filename, 
 		//	 unsigned int linenum, const char *format){
 		 
 		this.trError_at_line(status, errnum);//, const char *filename, 
 					//unsigned int linenum, const char *format);
 	}
 	
 	
 	/**
 	 * Never used.
 	 * 
 	 * @param errnum
 	 */
 	public void JtrPerror(int errnum){
 		this.trPerror(errnum);
 	}
 	
 	
 	/**
 	 * Should terminates the trains protocol but does nothing else than always returning 0.
 	 * Wrapping method of trTerminate. 
 	 * 
 	 * @return always 0
 	 */
 	public int JtrTerminate(){
 		int exitcode = this.trTerminate();
 		return exitcode;
 	}
 	
 	
 	/**
 	 * Allocates a new message to be sent.
 	 * 
 	 * @param payloadSize size of the content of the message
 	 * @param payload content of the message 
 	 * @return 0 on success
 	 */
 	public int Jnewmsg(int payloadSize, byte[] payload){
 		int exitcode = this.newmsg(payloadSize, payload);
 		return exitcode;
 	}
 	
 	
 	/**
 	 * Sends the message previously allocated on the train by newmsg.
 	 * 
 	 * @param msg message to be sent
 	 * @return 0 on success
 	 */
 	public int JutoBroadcast(Message msg){
 		int exitcode = this.utoBroadcast(msg);			
 		return exitcode;
 	}
 	
 	public void JdumpCountersData(byte[] data){
 		dumpCountersData(data);
 	}
 
 }
 	
