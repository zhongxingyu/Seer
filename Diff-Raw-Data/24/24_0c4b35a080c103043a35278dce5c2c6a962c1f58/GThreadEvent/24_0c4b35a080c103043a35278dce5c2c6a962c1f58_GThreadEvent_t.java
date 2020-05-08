 /*	
  *  Copyright (c) 2009-@year@. The GUITAR group at the University of Maryland. Names of owners of this group may
  *  be obtained by sending an e-mail to atif@cs.umd.edu
  * 
  *  Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated 
  *  documentation files (the "Software"), to deal in the Software without restriction, including without 
  *  limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
  *	the Software, and to permit persons to whom the Software is furnished to do so, subject to the following 
  *	conditions:
  * 
  *	The above copyright notice and this permission notice shall be included in all copies or substantial 
  *	portions of the Software.
  *
  *	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT 
  *	LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO 
  *	EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER 
  *	IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR 
  *	THE USE OR OTHER DEALINGS IN THE SOFTWARE. 
  */
 package edu.umd.cs.guitar.event;
 
 import java.util.Hashtable;
 import java.util.List;
 
 import edu.umd.cs.guitar.model.GComponent;
 import edu.umd.cs.guitar.util.GUITARLog;
 
 /**
  * Abstract class for all GUITAR events requiring to run in a separate thread.
  * All subclasses must implement the <code> actionPerformImp </code> methods.
  * 
  * <p>
  * 
  * @author <a href="mailto:baonn@cs.umd.edu"> Bao Nguyen </a>
  */
 public abstract class GThreadEvent implements GEvent {
 
 	/**
      * 
      */
 	public GThreadEvent() {
 		super();
 		if (threadGroup == null) {
 			threadGroup = new DispatchThreadGroup("GThreadEvent group");
 		}
 	}
 
 	static DispatchThreadGroup threadGroup;
 
 	@Override
	public void perform(GComponent gComponent, List<String>  parameters,
 			Hashtable<String, List<String>> optionalData) {
 		Thread t = new Thread(threadGroup, new DispatchThread(gComponent,
 				parameters, optionalData));
 
 		t.start();
 
 	}
 
 	@Override
 	public void perform(GComponent gComponent,
 			Hashtable<String, List<String>> optionalData) {
 		Thread t = new Thread(threadGroup, new DispatchThread(gComponent,
 				optionalData));
 		t.start();
 	}
 
 	// /*
 	// * (non-Javadoc)
 	// *
 	// * @see
 	// *
 	// edu.umd.cs.guitar.event.GEvent#perform(edu.umd.cs.guitar.model.GComponent
 	// * , java.lang.Object, java.lang.Object)
 	// */
 	// @Override
 	// public void perform(GComponent gComponent, Object parameters,
 	// Object optionalData) {
 	// Thread t = new Thread(threadGroup, new DispatchThread(gComponent));
 	// // Thread t = new Thread(new DispatchThread(gComponent));
 	// t.start();
 	// }
 
 	/**
 	 * The actual implementation of the event without parameters
 	 * 
 	 * <p>
 	 * 
 	 * @param gComponent
 	 */
 	protected abstract void performImpl(GComponent gComponent,
 			Hashtable<String, List<String>> optionalData);
 
 	/**
 	 * 
 	 * The actual implementation of the event with parameters
 	 * 
 	 * @param gComponent
 	 * @param parameters
 	 */
 	protected abstract void performImpl(GComponent gComponent,
 			Object parameters, Hashtable<String, List<String>> optionalData);
 
 	/**
 	 * A helper class to group all event dispatching threads
 	 * 
 	 * <p>
 	 * 
 	 * @author <a href="mailto:baonn@cs.umd.edu"> Bao N. Nguyen </a>
 	 * 
 	 */
 	class DispatchThreadGroup extends ThreadGroup {
 
 		/**
 		 * @param name
 		 */
 		public DispatchThreadGroup(String name) {
 			super(name);
 		}
 
 		@Override
 		public void uncaughtException(Thread t, Throwable e) {
 
 			GUITARLog.log.error(this.getName() + " uncaught Exception!!!", e);
 			throw (RuntimeException) e;
 		}
 	}
 
 	/**
 	 * A helper class to run action in a separate thread.
 	 * 
 	 * <p>
 	 * 
 	 * @author <a href="mailto:baonn@cs.umd.edu"> Bao Nguyen </a>
 	 */
 	// private class DispatchThread extends Thread {
 	private class DispatchThread implements Runnable {
 
 		GComponent gComponent;
 		Object parameters = null;
 		Hashtable<String, List<String>> optionalData = null;
 
 		/**
 		 * @param gComponent
 		 * @param optionalData
 		 */
 		public DispatchThread(GComponent gComponent,
 				Hashtable<String, List<String>> optionalData) {
 			super();
 			this.gComponent = gComponent;
 			this.optionalData = optionalData;
 		}
 
 		/**
 		 * @param gComponent
 		 * @param parameters
 		 * @param optionalData
 		 */
 		public DispatchThread(GComponent gComponent, Object parameters,
 				Hashtable<String, List<String>> optionalData) {
 			super();
 			this.gComponent = gComponent;
 			this.parameters = parameters;
 			this.optionalData = optionalData;
 		}
 
 		@Override
 		public void run() {
 			synchronized (gComponent) {
 
 				if (parameters == null)
 					performImpl(gComponent, optionalData);
 				else
 					performImpl(gComponent, parameters, optionalData);
 
 			}
 		}
 	}
 }
