 /*******************************************************************************
  * Forwarding on Gates Simulator/Emulator
  * Copyright (C) 2012, Integrated Communication Systems Group, TU Ilmenau.
  * 
  * This program and the accompanying materials are dual-licensed under either
  * the terms of the Eclipse Public License v1.0 as published by the Eclipse
  * Foundation
  *  
  *   or (per the licensee's choosing)
  *  
  * under the terms of the GNU General Public License version 2 as published
  * by the Free Software Foundation.
  ******************************************************************************/
 package de.tuilmenau.ics.fog.application;
 
 import java.lang.reflect.Constructor;
 
 import de.tuilmenau.ics.fog.application.observer.ApplicationEventExit;
 import de.tuilmenau.ics.fog.application.observer.ApplicationObservable;
 import de.tuilmenau.ics.fog.application.observer.IApplicationEventObserver;
 import de.tuilmenau.ics.fog.exceptions.CreationException;
 import de.tuilmenau.ics.fog.exceptions.InvalidParameterException;
 import de.tuilmenau.ics.fog.facade.Description;
 import de.tuilmenau.ics.fog.facade.Host;
 import de.tuilmenau.ics.fog.facade.Identity;
 import de.tuilmenau.ics.fog.facade.Layer;
 import de.tuilmenau.ics.fog.ui.Viewable;
 import de.tuilmenau.ics.fog.util.Logger;
 
 
 /**
  * Application running on a host.
  * Process is implemented as parallel thread.
  */
 public abstract class Application
 {
 	/**
 	 * Creates an application for a given host using reflection.
 	 * The application is not started.
 	 * 
 	 * @param pName Name of the application, which is used as class name.
 	 * @param pHost Reference to the host, on which the application should run.
 	 * @param pIdentity Identity of the creator of the application.
 	 * @return Reference to the created application (!= null).
 	 * @throws CreationException On error.
 	 */
 	public static Application createApplication(String pName, Host pHost, Identity pIdentity) throws CreationException
 	{
 		Application app = null;
 		try {
 			Class<?> appClass = null;
 			
 			if(pName.indexOf('.') < 0 ) {
 				// no "." in string -> Add base class name
 				pName = Application.class.getPackage().getName() +"." +pName;
 			}
 			
 			// Fetch class object ...
 			appClass = Class.forName(pName);
 						
 			// ... get std constructor for class ...
 			Constructor<?> tConstructor = appClass.getConstructor(Host.class, Identity.class);
 			
 			// ... and generate object
 			app = (Application) tConstructor.newInstance(pHost, pIdentity);
 		}
 		catch(NoClassDefFoundError tExc) {
 			// Class does not exist (Name wrong)
 			throw new CreationException("Application " +pName +" not defined.", tExc);
 		}
 		catch(ClassNotFoundException tExc) {
 			// Class does not exist (Name wrong)
 			throw new CreationException("Application " +pName +" not found.", tExc);
 		}
 		catch(NoSuchMethodException tExc) {
 			// Constructor not found
 			throw new CreationException("Application " +pName +" does not have a valid constructor.", tExc);
 		}
 		catch(ClassCastException tExc) {
 			// Convert operation failed
 			// -> Name of a class with wrong base class
 			throw new CreationException("Application " +pName +" does not support scripting interface.", tExc);
 		}
 		catch(Exception tExc) {
 			throw new CreationException("Exception during application " +pName , tExc);
 		}
 
 		return app;
 	}
 	
 	/**
 	 * Default constructor is not allowed for this class.
 	 * Use the constructor with parameters.
 	 */
 	@SuppressWarnings("unused")
 	private Application()
 	{
 		throw new RuntimeException("Default constructor for Application not allowed.");
 	}
 	
 	protected Application(Host pHost, Identity pIdentity)
 	{
 		this(pHost, null, pIdentity);
 	}
 	
 	protected Application(Host pHost, Logger pParentLogger, Identity pIdentity)
 	{
 		mHost = pHost;
 		mParameters = null;
 		mAppObservable = new ApplicationObservable(this);
 
 		// use given identity or create own one
 		if(pIdentity != null) {
 			mIdentity = pIdentity;
 		} else {
 			mIdentity = mHost.getAuthenticationService().createIdentity(this.toString());
 		}
 
 		if(pParentLogger == null) pParentLogger = pHost.getLogger();
 		mLogger = new Logger(pParentLogger);
 	}
 	
 	/**
 	 * Non-blocking call to start the application. It is doing
 	 * statistical stuff and than calling {@link started}, where
 	 * derived application classes can do the real work.
 	 * 
 	 * It does not call {@link started} if the application is
 	 * already running but throws an runtime exception.
 	 */
 	public final void start()
 	{
 		if(!isRunning()) {
 			mLogger.trace(this, "Started");
 			mHost.registerApp(this);
 			started();
 		} else {
 			throw new RuntimeException(this +" - Application is already running.");
 		}
 	}
 	
 	/**
 	 * Non-blocking call, where derived classes can startup
 	 * the application. If they detect an error and terminate,
 	 * they have to call {@link terminated}.
 	 */
 	protected abstract void started();
 	
 	/**
 	 * Non-blocking call to inform the application that it
 	 * should exit.
 	 */
 	public abstract void exit();
 	
 	/**
 	 * @return If the application is currently running.
 	 */
 	public abstract boolean isRunning();
 	
 	/**
 	 * Blocking call, which waits until the application is
 	 * terminated.
 	 */
 	public void waitForExit()
 	{
 		while(isRunning()) {
 			synchronized(this) {
 				try {
 					wait();
 				} catch (InterruptedException tExc) {
 					mLogger.warn(this, "Can not wait for exit. Try to wait again.", tExc);
 				}
 			}
 		}
 	}
 	
 	public void addObserver(IApplicationEventObserver pObserver)
 	{
 		mLogger.log(this, "Register observer " + pObserver);
 		mAppObservable.addObserver(pObserver);
 	}
 	
 	public void deleteObserver(IApplicationEventObserver pObserver)
 	{
 		mLogger.log(this, "Unregister observer " + pObserver);
 		mAppObservable.deleteObserver(pObserver);
 	}
 	
 	public String toString()
 	{
 		return this.getClass().getSimpleName() +"@" +mHost;
 	}
 	
 	/**
 	 * Last method an application MUST call when it
 	 * terminates. {@link isRunning} MUST now return true.
 	 * 
 	 * @param pExc != null if the application terminates with an error.
 	 */
 	protected final void terminated(Exception pExc)
 	{
 		if(isRunning()) {
 			if(pExc != null){
 				throw new IllegalStateException(this +" - Application is terminated but still running. Application was terminated because of exception: " + pExc);
 			}else{
 				throw new IllegalStateException(this +" - Application is terminated but still running.");
 			}
 		}
 		
 		if(pExc != null) {
 			mLogger.warn(this, "Terminated with error.", pExc);
 		} else {
 			mLogger.trace(this, "Terminated");
 		}
 		
 		// inform observer about termination
 		mAppObservable.notifyObservers(new ApplicationEventExit());
 		mAppObservable.clear();
 		
 		mHost.unregisterApp(this);		
 		
 		// inform all waiting threads about the termination
 		synchronized (this) {
 			notifyAll();
 		}
 	}
 	
 	public Host getHost()
 	{
 		return mHost;
 	}
 	
 	public Layer getLayer()
 	{
 		return mHost.getLayer(null);
 	}
 	
 	public Logger getLogger()
 	{
 		return mLogger;
 	}
 	
 	public Identity getIdentity()
 	{
 		return mIdentity;
 	}
 	
 	/**
 	 * Method is called before the application is started. It sets the parameters
 	 * for the application execution. In the default implementation, the parameters
 	 * are accepted in any case and stored for later access.
 	 * 
 	 * @param pParameters Parameters for application. pParameters[0] is the application name.
 	 * @throws InvalidParameterException If the parameters are not acceptable
 	 */
 	public void setParameters(String[] pParameters) throws InvalidParameterException
 	{
 		mParameters = pParameters;
 	}
 	
 	/**
 	 * @return Parameter strings (!= null)
 	 */
 	public String[] getParameters()
 	{
 		if(mParameters == null) mParameters = new String[0];
 		
 		return mParameters;
 	}
 	
 	public Description getDescription()
 	{
 		return Description.createBE(true);
 	}
 	
 	/**
 	 * Host, on which the application is running on.
 	 * It is set by the constructor and always != null.
 	 */
 	protected Host mHost;
 	
 	/**
 	 * Logger for output messages of the application.
 	 * It is set by the constructor and always != null.
 	 */
 	protected Logger mLogger;
 	
 	private Identity mIdentity;
 	
 	/**
 	 * Parameters from the command line of this application instance
 	 */
 	@Viewable("Parameters")
 	private String[] mParameters;
 	
 	protected ApplicationObservable mAppObservable;
 }
