 /*****************************************************
  * jr generated file
  ****************************************************/
 import edu.ucdavis.jr.*;
 import edu.ucdavis.jr.jrx.*;
 import java.rmi.*;
 import java.io.Serializable;
 
 
 import edu.ucdavis.jr.*;
 import java.util.*;
 
 public abstract class Person extends java.lang.Object {
     { JRinit(); }
     private Door door;
     protected int numCalls;
     private String name;
     public Op_ext.JRProxyOp JRget_op_tellLastCallOrClosing_voidTovoid()
     {
         return op_tellLastCallOrClosing_voidTovoid;
     }
     
     public Op_ext.JRProxyOp op_tellLastCallOrClosing_voidTovoid;
     
     
     @Override()
     public String toString() {
         // Return
         return (name);
         // End Return
 
     }
     
     public Person(String name) {
         // Begin Expr2
         super();
         // Begin Expr2
         this.name = name;
         JRprocess();
     }
     
     public Person() {
         // Begin Expr2
         this("Unnamed");
         JRprocess();
     }
     
     public void say(String msg) {
         // Begin Expr2
        System.out.println(((MyTime) (new Cap_ext_(Global.clock.JRget_op_getTime_voidToMyTime(), "MyTime")).call(jrvm.getTimestamp(), (java.lang.Object[]) null)) + ": " + name + " says: " + msg);
     }
     
     private boolean enterDoor() {
         // Begin Expr2
         numCalls = ((Integer) (new Cap_ext_(Global.door.JRget_op_add_PersonToint(), "int")).call(jrvm.getTimestamp(), new java.lang.Object [] {this}));
         // Return
         return (numCalls < 2);
         // End Return
 
     }
     
     private void leaveBar() {
         // Begin JRCall
         // Begin Expr2
         (new Cap_ext_(Global.door.JRget_op_leave_PersonTovoid(), "void")).call(jrvm.getTimestamp(), new java.lang.Object [] {this});// End JRCall
         
     }
     
     public boolean knowsItsClosed() {
         // Return
         return (numCalls >= 2);
         // End Return
 
     }
     
     public boolean knowsLastCallHasBeen() {
         // Return
         return (numCalls >= 1);
         // End Return
 
     }
     
     protected abstract boolean shouldGoHome();
     
     protected abstract void oneIteration();
     public Op_ext.JRProxyOp JRget_op_personProcess_voidTovoid()
     {
         return op_personProcess_voidTovoid;
     }
     
     public Op_ext.JRProxyOp op_personProcess_voidTovoid;
     class ProcOp_voidTovoid_implpersonProcess extends ProcOp_ext_impl
     {
         Person thisarg;
         public ProcOp_voidTovoid_implpersonProcess(Person thisIn) throws RemoteException
         {
         thisarg = thisIn;
         }
         public java.lang.Object call(long JRtimestamp, java.lang.Object [] JRargs) throws RemoteException
         {
             jrvm.ariseAndReceive();  // from caller
             try    {
                 jrvm.setTimestamp(JRtimestamp);
                 thisarg.personProcessvoidTovoid(null, null, null, JRargs);
 return null;
             } finally {
                 jrvm.sendAndDie();    // to caller
             }
         }
         class sendThread implements Runnable
         {
             java.lang.Object [] JRargs;
             Op_ext.JRProxyOp retOp;
             Cap_ext fretOp;
             edu.ucdavis.jr.RemoteHandler handler;
 
             public sendThread(Op_ext.JRProxyOp retOp, edu.ucdavis.jr.RemoteHandler handler, java.lang.Object [] JRargs)
             {
                 this.JRargs = JRargs;
                 this.retOp = retOp;
                 this.handler = handler;
             }
             public sendThread(Op_ext.JRProxyOp retOp, Cap_ext fretOp,edu.ucdavis.jr.RemoteHandler handler, java.lang.Object [] JRargs)
             {
                 this.JRargs = JRargs;
                 this.retOp = retOp;
                 this.fretOp = fretOp;
                 this.handler = handler;
             }
             public void run()
             {
                 try    {
                     thisarg.personProcessvoidTovoid(this.retOp, this.fretOp, this.handler, this.JRargs);
                 } catch (Exception e) {/* should be safe to ignore this exception */}
                 jrvm.threadDeath();
             }
         }
         public void send(long JRtimestamp, java.lang.Object [] JRargs) throws RemoteException
         {
             this.send(JRtimestamp, null, null, null, JRargs);
         }
         public void send(long JRtimestamp, edu.ucdavis.jr.RemoteHandler handler, java.lang.Object [] JRargs) throws RemoteException
         {
             this.send(JRtimestamp, null, handler, null, JRargs);
         }
         public Cap_ext cosend(long JRtimestamp, java.lang.Object [] JRargs) throws RemoteException
         {
             return this.cosend(JRtimestamp, null, null, null, JRargs);
         }
         public Cap_ext cosend(long JRtimestamp, edu.ucdavis.jr.RemoteHandler handler, java.lang.Object [] JRargs) throws RemoteException
         {
             return this.cosend(JRtimestamp, null, handler, null, JRargs);
         }
         public void send(long JRtimestamp, Op_ext.JRProxyOp retOp, edu.ucdavis.jr.RemoteHandler handler, Exception thrown, java.lang.Object [] JRargs) throws RemoteException
         {
             jrvm.setTimestamp(JRtimestamp);
             jrvm.threadBirth();
             new Thread(new sendThread(retOp, handler, JRargs)).start();
         }
         public Cap_ext cosend(long JRtimestamp, Op_ext.JRProxyOp retOp, edu.ucdavis.jr.RemoteHandler handler, Exception thrown, java.lang.Object [] JRargs) throws RemoteException
         {
             jrvm.setTimestamp(JRtimestamp);
             jrvm.threadBirth();
             new Thread(new sendThread(null, handler, JRargs)).start();
             Op_ext.JRProxyOp myretOp = new Op_ext_.JRProxyOp(new InOp_ext_impl());
             myretOp.send(jrvm.getTimestamp(), (java.lang.Object []) null);
             return new Cap_ext_(myretOp, "void");
         }
         public Cap_ext cocall(long JRtimestamp, java.lang.Object [] JRargs) throws RemoteException
         {
             Op_ext.JRProxyOp retOp = new Op_ext_.JRProxyOp(new InOp_ext_impl(false));
             jrvm.setTimestamp(JRtimestamp);
             jrvm.threadBirth();
             new Thread(new sendThread(retOp, null, JRargs)).start();
             Cap_ext retCap = new Cap_ext_(retOp, "void");
             return retCap;
         }
         public Cap_ext cocall(long JRtimestamp, edu.ucdavis.jr.RemoteHandler handler, Cap_ext fretOp, java.lang.Object [] JRargs) throws RemoteException
         {
             Op_ext.JRProxyOp retOp = new Op_ext_.JRProxyOp(new InOp_ext_impl(false));
             jrvm.setTimestamp(JRtimestamp);
             jrvm.threadBirth();
             new Thread(new sendThread(retOp, fretOp, handler, JRargs)).start();
             Cap_ext retCap = new Cap_ext_(retOp, "void");
             return retCap;
         }
         public Recv_ext recv() throws RemoteException
         {
             /* This is an error */
             throw new jrRuntimeError("Receive invoked on an operation/operation capability associated with a method!");
         }
         public void deliverPendingMessages()
         {
             /* This is an error */
             throw new jrRuntimeError("Message delivery invoked on an operation associated with a method!");
         }
         public int length()
         {
             return 0;
         }
         public InOpIterator elements()
         {
             // This is an error
             throw new jrRuntimeError("Elements invoked on an operation / operation capability associated with a method!");
         }
         public InLock getLock()
         {
             // This is an error
             throw new jrRuntimeError("getLock invoked on an operation / operation capability associated with a method!");
         }
         public long getFirstTime()
         {
             // This is an error
             throw new jrRuntimeError("getFirstTime invoked on an operation / operation capability associated with a method!");
         }
         public boolean isRemote(String site)
         {
             // This is an error
             throw new jrRuntimeError("IsRemote invoked on an operation / operation capability associated with a method!");
         }
     }
     
     public void personProcessvoidTovoid(java.lang.Object [] JRargs) {
         ((Op_ext_.JRProxyOp)op_personProcess_voidTovoid).call(jrvm.getTimestamp(), JRargs);
     }
     private void personProcessvoidTovoid(Op_ext.JRProxyOp retOp, Cap_ext fretOp, edu.ucdavis.jr.RemoteHandler handler, java.lang.Object [] JRargs)
     {
         try    {
             {
                 // Begin Expr2
                 say("I\'m going in ...");
                 if (enterDoor()) {
                     // Begin Expr2
                     startLoop();
                     // Begin Expr2
                     say("I\'m going home ...");
                     // Begin Expr2
                     leaveBar();
                 }
                 // Return
                 { if (retOp != null)
                     retOp.send(jrvm.getTimestamp(), (edu.ucdavis.jr.RemoteHandler) null, null);
                 return ; }
                 // End Return
 
             }
         } catch (Exception JRe)    {
             if (retOp != null && fretOp == null)
             {
         	// if it is a forward cocall with handler
         	if ((handler != null) && !(JRe instanceof java.rmi.RemoteException))
         	    handler.JRhandler(JRe);
         	else
         	    // give preference to propagation through the call stack
         	    retOp.send(jrvm.getTimestamp(), JRe);
             }
             else if ((retOp != null) && (fretOp != null) && !(JRe instanceof java.rmi.RemoteException))
             {
         	// for COSTMT exception handling in operation
         	if (handler != null)
         	    handler.JRhandler(JRe);
         	fretOp.send(jrvm.getTimestamp(), handler, (java.lang.Object []) null);
             }
             else if ((handler != null) && !(JRe instanceof java.rmi.RemoteException))
             {
         	// this should only be for a send/forward
         	handler.JRhandler(JRe);
         	// can rethrow below just to get out of this method
             }
             // rethrow the proper type of exception
             // catch all
             throw new jrRuntimeError("Unhandled exception: " + JRe.toString()+ " at "+ jrRuntimeError.where(JRe));
         }
     }
     
     
     private void startLoop() {
         JRLoop19: while (!shouldGoHome()) {
             // Begin Expr2
             oneIteration();
         }
     }
     protected boolean JRcalled = false;
     protected JRPerson jrresref;
     public Object JRgetjrresref()
     { try {return jrresref.clone(); } catch (Exception e) {/* not gonna happen */ return null; } }
     protected void JRinit() {
     	if(this.JRcalled) return;
     	try {
     	op_personProcess_voidTovoid = new Op_ext_.JRProxyOp(new ProcOp_voidTovoid_implpersonProcess(this));
     	op_tellLastCallOrClosing_voidTovoid = 
     	  new Op_ext_.JRProxyOp(new InOp_ext_impl());
     	} catch (Exception JRe)
     	{ throw new jrRuntimeError(JRe.toString()); }
     	jrresref = new JRPerson(op_personProcess_voidTovoid, op_tellLastCallOrClosing_voidTovoid);
     	this.JRcalled = true;
     }
     private boolean JRproc = false;
     private void JRprocess() {
     	if (JRproc) return;
     	JRproc = true;
     	try {
     		op_personProcess_voidTovoid.send(jrvm.getTimestamp(), (java.lang.Object []) null);
     	} catch (Exception JRe) { throw new jrRuntimeError(JRe.toString()); }
     }
 }
