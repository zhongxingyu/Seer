 package org.hackystat.sensor.eclipse.event;
 
 import org.eclipse.core.resources.IResource;
 
 
 /**
  * Provides a event information (flag) and IResource instance if applicable. A client
  * of the Eclipse Sensor should not instantiate this instance. IResource instance is given at 
  * the resource place where the EclipseSensorEventListener is hooked.
  * 
  * <p>The IResource instance can be used when the following flag is contained.
  * <ul>
  * <li>PROJECT_OPEN - A project is opened. IResource can cast IProject.</li>
  * <li>PROJECT_CLOSE - A project is closed. IResource can cast IProject.</li>
  * <li>FILE_SAVE - A file is saved. IResource can cast IFile.</li>
 * 
  * After getting IResource instance, using <code>getResource()</code>, the client
  * might manipulate the project path and/or workspace path, and so forth.
  * 
  * <p>Otherwise the IResource instance is null.
  *
  * @author Takuya Yamashita
  * @version $Id: EclipseSensorEvent.java,v 1.1.1.1 2005/10/20 23:56:57 johnson Exp $
  * @see org.eclipse.core.resources.IResource
  */
 public class EclipseSensorEvent {
   /**
    * The flag when an event notify that project is opened.
    */
   public static final int PROJECT_OPEN = 0;
 
 
   /**
    * The flag when an event notify that project is closed.
    */
   public static final int PROJECT_CLOSE = 1;
 
 
   /**
    * The flag when an event notify that a file is saved.
    */
   public static final int FILE_SAVE = 2;
 
 
   /**
    * The IResource interface to hold either IContainer, IFile, IFolder, IProject, or IWorkspaceRoot.
    */
   private IResource resource;
 
 
   /**
    * The event information (flag). For example, that project is opened is 0.
    */
   private int event;
 
 
   /**
    * Constructor for the EclipseSensorEvent object. A client of the Eclipse sensor should
    * not instantiate this instance. Only EclipseSenserPlugin can instantiate this.
    *
    * @param resource IResource where the EclipseSensorEventListener is hooked.
    * @param event The event information when the EclipseSensor notify.
    */
   public EclipseSensorEvent(IResource resource, int event) {
     this.resource = resource;
     this.event = event;
   }
 
 
   /**
    * Gets the IResource implementing interface or class. After getting this. a client can cast
    * the IResource sub interface or sub class such as IProject, IFile, and so forth.
    *
    * @return The IResource interface.
    */
   public IResource getResource() {
     return this.resource;
   }
 
 
   /**
    * Gets the event information (flag). A client can know the event kind in the 
    * EclipseSensorEventListener implementing class when the class is notified.
    *
    * @return The event information (flag).
    */
   public int getEvent() {
     return this.event;
   }
 }
