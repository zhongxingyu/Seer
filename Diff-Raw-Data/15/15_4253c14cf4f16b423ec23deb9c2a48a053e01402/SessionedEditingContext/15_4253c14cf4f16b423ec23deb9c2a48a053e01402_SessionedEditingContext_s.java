 //  SessionedEditingContext.java
 
 /*
  * Copyright (c) 2008, Gennady & Michael Kushnir
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without modification, are
  * permitted provided that the following conditions are met:
  * 
  * 	•	Redistributions of source code must retain the above copyright notice, this
  * 		list of conditions and the following disclaimer.
  * 	•	Redistributions in binary form must reproduce the above copyright notice,
  * 		this list of conditions and the following disclaimer in the documentation
  * 		and/or other materials provided with the distribution.
  * 	•	Neither the name of the RUJEL nor the names of its contributors may be used
  * 		to endorse or promote products derived from this software without specific 
  * 		prior written permission.
  * 		
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT
  * SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
  * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
  * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY
  * WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package net.rujel.reusables;
 
 import java.util.logging.Logger;
 
 import com.webobjects.eocontrol.*;
 import com.webobjects.appserver.*;
 import com.webobjects.foundation.NSMutableArray;
 
 public class SessionedEditingContext extends EOEditingContext {
 	protected WOSession session;
 	protected static Logger logger = Logger.getLogger("rujel.reusables");
 	
 	protected Counter failures = new Counter();
 	
 	public SessionedEditingContext (WOSession ses){
 			super((ses.objectForKey("objectStore")!=null)?
 					(EOObjectStore)ses.objectForKey("objectStore"):
 						EOObjectStoreCoordinator.defaultCoordinator());
 		if (ses == null) throw new 
 			NullPointerException (
 					"You should define a session to instantiate SessionedEditingContext");
 		session = ses;
 		if(ses instanceof MultiECLockManager.Session)
 			((MultiECLockManager.Session)ses).ecLockManager().registerEditingContext(this);
 	}
 	
 	public SessionedEditingContext (EOObjectStore parent,WOSession ses){
 		super(parent);
 		if (ses == null) throw new 
 			NullPointerException (
 					"You should define a session to instantiate SessionedEditingContext");
 		session = ses;
 		if(ses instanceof MultiECLockManager.Session)
 			((MultiECLockManager.Session)ses).ecLockManager().registerEditingContext(this);
 	}
 	
 	public WOSession session() {
 		return session;
 	}
 	
 	public void saveChanges() {
 		try {
 			super.saveChanges();
 			failures.nullify();
 		} catch (RuntimeException ex) {
 			failures.raise();
 			throw ex;
 		}
 	}
 	
 	public int failuresCount () {
 		return failures.value();
 	}
 	
 	protected void fin() {
 		if(session instanceof MultiECLockManager.Session)
 			((MultiECLockManager.Session)session).
 						ecLockManager().unregisterEditingContext(this);
 		if(_stackTraces.count() > 0)
 			logger.log(WOLogLevel.WARNING,"disposing locked editing context (" + 
 					_nameOfLockingThread + " : " + _stackTraces.count() + ')', new Object[] 
 					             {session, new Exception(), _stackTraces});		
 	}
 	public void dispose() {
 		fin();
 		super.dispose();
 	}
 	public void finalize() throws Throwable {
 		fin();
 		super.finalize();
 	}
 	
 	private String _nameOfLockingThread = null;
 	private NSMutableArray _stackTraces = new NSMutableArray();
 
 	   public void lock() {
 	       String nameOfCurrentThread = Thread.currentThread().getName();
 	       String trace = WOLogFormatter.formatTrowable(new Exception());
 	       if (_stackTraces.count() == 0) {
 	           _stackTraces.addObject(trace);
 	           _nameOfLockingThread = nameOfCurrentThread;
 	       } else {
 	           if (nameOfCurrentThread.equals(_nameOfLockingThread)) {
 	               _stackTraces.addObject(trace);
 	           } else {
 	               logger.log(WOLogLevel.INFO,
 	            		   "Attempting to lock editing context from " + nameOfCurrentThread
 	            		   + " that was previously locked in " + _nameOfLockingThread,
 	            		   new  Object[] {session,trace,_stackTraces});
 	           }
 	       }
 	       super.lock();
 	   }
 
 	   public void unlock() {
 	       super.unlock();
 	       if (_stackTraces.count() > 0)
 	           _stackTraces.removeLastObject();
 	       if (_stackTraces.count() == 0)
 	           _nameOfLockingThread = null;
 	   }
 	   
 	   public void insertObject(EOEnterpriseObject object) {
 		   super.insertObject(object);
 		   if(!globalIDForObject(object).isTemporary())
 			   logger.log(WOLogLevel.WARNING,"Inserting not new object",
					   new Object[] {session,object});
 	   }
 
 }
