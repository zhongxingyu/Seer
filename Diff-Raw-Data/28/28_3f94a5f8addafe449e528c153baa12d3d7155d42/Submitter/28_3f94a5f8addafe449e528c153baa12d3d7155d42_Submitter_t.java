 /*==========================================================================*\
  |  $Id$
  |*-------------------------------------------------------------------------*|
  |  Copyright (C) 2006-2009 Virginia Tech
  |
  |  This file is part of Web-CAT Electronic Submitter.
  |
  |  Web-CAT is free software; you can redistribute it and/or modify
  |  it under the terms of the GNU General Public License as published by
  |  the Free Software Foundation; either version 2 of the License, or
  |  (at your option) any later version.
  |
  |  Web-CAT is distributed in the hope that it will be useful,
  |  but WITHOUT ANY WARRANTY; without even the implied warranty of
  |  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  |  GNU General Public License for more details.
  |
  |  You should have received a copy of the GNU General Public License along
  |  with Web-CAT; if not, see <http://www.gnu.org/licenses/>.
 \*==========================================================================*/
 
 package org.webcat.submitter;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.InvocationTargetException;
 import java.net.URI;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Map;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import org.w3c.dom.Document;
 import org.webcat.submitter.internal.DefaultLongRunningTaskManager;
 import org.webcat.submitter.internal.LongRunningTask;
 import org.webcat.submitter.internal.SubmissionParserErrorHandler;
 import org.webcat.submitter.internal.utility.FilePattern;
 import org.webcat.submitter.targets.AssignmentTarget;
 import org.webcat.submitter.targets.RootTarget;
 import org.webcat.submitter.targets.SubmissionTarget;
 import org.xml.sax.SAXException;
 
 //--------------------------------------------------------------------------
 /**
  * <p>
  * The primary class providing functionality for the electronic submitter.
  * Usage example:
  * </p>
  * <pre>
  * java.io.File folder = new java.io.File("path to what you want to submit");
  * SubmittableFile itemToSubmit = new SubmittableFile(folder);
  *
  * Submitter submitter = new Submitter();
  * URL targetsURL = new URL("http://yoursite.com/targets.xml");
  * submitter.readSubmissionTargets(targetsURL);
  * RootTarget root = submitter.getRoot();
  *
  * // Traverse the submission targets from root and get the assignment that you
  * // wish to submit to, perhaps via a user interface.
  * AssignmentTarget assignment = ...;
  *
  * SubmissionManifest manifest = new SubmissionManifest();
  * manifest.setSubmittableItems(itemToSubmit);
  * manifest.setAssignment(assignment);
  * manifest.setUsername("username");
  * manifest.setPassword("password");
  *
  * submitter.submit(manifest);
  * </pre>
  *
  * @author  Tony Allevato (Virginia Tech Computer Science)
  * @author  latest changes by: $Author$
  * @version $Revision$ $Date$
  */
 public class Submitter
 {
     //~ Constructors ..........................................................
 
     // ----------------------------------------------------------
     /**
      * Creates a new submitter.
      */
     public Submitter()
     {
         taskManager = new DefaultLongRunningTaskManager();
     }
 
 
     //~ Methods ...............................................................
 
     // ----------------------------------------------------------
     /**
      * Gets the long running task manager used by this submitter.
      *
      * @return the long running task manager used by this submitter
      */
     public ILongRunningTaskManager getLongRunningTaskManager()
     {
         return taskManager;
     }
 
 
     // ----------------------------------------------------------
     /**
      * <p>
      * Sets the long running task manager to be used by this submitter.
      * </p><p>
      * You may pass <code>null</code> to this method as a shortcut to revert
      * to the default long running task manager.
      * </p>
      *
      * @param manager the long running task manager to be used by this
      *     submitter
      */
     public void setLongRunningTaskManager(ILongRunningTaskManager manager)
     {
     	if (manager == null)
     	{
     		taskManager = new DefaultLongRunningTaskManager();
     	}
     	else
     	{
     		taskManager = manager;
     	}
     }
 
 
     // ----------------------------------------------------------
     /**
      * Gets the root object of the submission target tree.
      *
      * @return an ITargetRoot represented the root of the submission targets
      */
     public RootTarget getRoot()
     {
         return root;
     }
 
 
     // ----------------------------------------------------------
     /**
      * <p>
      * Gets the target at the specified path, where the path is a
      * slash-delimited sequence of target names. If a target name itself
      * contains a slash, it should be escaped as a double-slash (for example,
      * the path "Target A/Target//B" would search for a child of the
      * root named "Target A", then a child of that target named "Target/B".
      * </p><p>
      * If two children of a target have the same name, this method will only
      * find the first one. To avoid this, always use distinct names for the
      * immediate children of any particular node.
      * </p><p>
      * This method will not find hidden targets, even if the name matches the
      * path passed in. This is by design.
      * </p>
      *
      * @param path the path from the root
      * @return the target found at the specified path, or null if one was not
      *     found
      * @throws SubmissionTargetException if an exception occurs
      */
     public SubmissionTarget getTarget(String path)
     throws SubmissionTargetException
     {
         SubmissionTarget target = getRoot();
 
         String[] components = path.split("/(?!/)");
 
         for (String component : components)
         {
             component = component.replaceAll("//", "/");
 
             SubmissionTarget foundChild = null;
             SubmissionTarget[] children = target.getLogicalChildren();
 
             for (SubmissionTarget child : children)
             {
                 if (!child.isHidden() && component.equals(child.getName()))
                 {
                     foundChild = child;
                     break;
                 }
             }
 
             if (foundChild != null)
             {
                 target = foundChild;
             }
             else
             {
                 target = null;
                 break;
             }
         }
 
         return target;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Reads the submission target definitions from the specified URL.
      *
      * @param definitionsUrl a URL that points to the submission target
      *     definitions
      * @throws IOException if an I/O exception occurred
      * @throws SubmissionTargetException if some other exception occurred
      */
     public void readSubmissionTargets(URL definitionsUrl)
     throws IOException
     {
         InputStream stream = null;
 
         try
         {
             stream = definitionsUrl.openStream();
             readSubmissionTargets(stream);
         }
         finally
         {
             try
             {
                 if (stream != null)
                 {
                     stream.close();
                 }
             }
             catch (IOException e)
             {
                 // Do nothing.
             }
         }
     }
 
 
     // ----------------------------------------------------------
     /**
      * Reads the submission target definitions from the specified input stream.
      *
      * @param stream the InputStream from which to read the submission targets
      * @throws IOException if an I/O exception occurred
      * @throws SubmissionTargetException if some other exception occurred
      */
     public void readSubmissionTargets(final InputStream stream)
     throws IOException
     {
         LongRunningTask task = new LongRunningTask(
                 "Reading submission targets") {
             public void run() throws Exception
             {
                 beginSubtask(1);
 
                 try
                 {
                     DocumentBuilderFactory factory =
                         DocumentBuilderFactory.newInstance();
 
                     factory.setIgnoringComments(true);
                     factory.setCoalescing(false);
                     factory.setNamespaceAware(true);
                    factory.setValidating(false);
 
                     SubmissionParserErrorHandler errorHandler =
                             new SubmissionParserErrorHandler();
 
                     DocumentBuilder builder = factory.newDocumentBuilder();
                     builder.setErrorHandler(errorHandler);
 
                     Document document = builder.parse(stream);
                     TargetParseError[] errors = errorHandler.getErrors();
 
                     if (errors != null)
                     {
                         throw new TargetParseException(errors);
                     }
                     else
                     {
                         root = new RootTarget(taskManager);
                         root.parse(document.getDocumentElement(), this);
                     }
                 }
                 catch (ParserConfigurationException e)
                 {
                     throw new SubmissionTargetException(e);
                 }
                 catch (SAXException e)
                 {
                     throw new SubmissionTargetException(e);
                 }
                 finally
                 {
                     finishSubtask();
                 }
             }
         };
 
         try
         {
             taskManager.run(task);
         }
         catch (InvocationTargetException e)
         {
             throw (IOException) e.getCause();
         }
     }
 
 
     // ----------------------------------------------------------
     /**
      * Gets a value indicating whether the last submission generated a
      * response.
      *
      * @return true if the last submission generated a response; otherwise,
      *     false
      */
     public boolean hasResponse()
     {
         return hasResponse;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Gets the string contents of the response generated by the last
      * submission, if one was generated.
      *
      * @return the response from the last submission
      */
     public String getResponse()
     {
         return response;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Submits the resources described by the specified submission manifest.
      *
      * @param manifest a {@link SubmissionManifest} that describes the items to
      *     be submitted and where they should be sent
      * @throws IOException
      * @throws ProtocolNotRegisteredException
      * @throws RequiredItemsMissingException
      * @throws SubmissionTargetException
      */
     public void submit(final SubmissionManifest manifest)
     throws IOException, SubmissionTargetException
     {
         if (manifest.getSubmittableItems() == null ||
                 manifest.getSubmittableItems().length == 0)
         {
             throw new NoItemsToSubmitException();
         }
 
         hasResponse = false;
         response = null;
 
         String[] missingItemPatterns =
                 verifyRequiredItems(manifest.getAssignment(),
                         manifest.getSubmittableItems());
 
         if (missingItemPatterns != null)
         {
             throw new RequiredItemsMissingException(missingItemPatterns);
         }
 
         // It doesn't actually matter what encoder we use here, since we're
         // only getting the URI in order to strip off the scheme and determine
         // which transport protocol to use.
 
         URI transport = manifest.getResolvedTransport(null);
 
         final IProtocol protocol =
                 ProtocolRegistry.getInstance().createProtocolInstance(
                         transport.getScheme());
 
         if (protocol != null)
         {
             LongRunningTask task = new LongRunningTask(
                 "Packaging and submitting files") {
                 public void run() throws Exception
                 {
                     protocol.submit(manifest, this);
                 }
             };
 
             try
             {
                 taskManager.run(task);
             }
             catch (InvocationTargetException e)
             {
                 Throwable cause = e.getCause();
 
                 if (cause instanceof RuntimeException)
                 {
                     throw (RuntimeException) cause;
                 }
                 else if (cause instanceof IOException)
                 {
                     throw (IOException) cause;
                 }
                 else
                 {
                     cause.printStackTrace();
                 }
             }
 
             if (protocol.hasResponse())
             {
                 hasResponse = true;
                 response = protocol.getResponse();
             }
         }
         else
         {
             throw new ProtocolNotRegisteredException(transport.getScheme());
         }
     }
 
 
     // ----------------------------------------------------------
     /**
      * Verifies that all of the files that are denoted as required by the
      * target assignment exist in the set of submittable items being packaged.
      *
      * @param assignment the assignment to which the items are being submitted
      * @param items the items being submitted
      * @return an array of required file patterns that were not satisfied by
      *     the specified set of submittable items, or null if all required
      *     files were present
      */
     private String[] verifyRequiredItems(AssignmentTarget assignment,
             ISubmittableItem[] items) throws SubmissionTargetException
     {
         final Map<String, Boolean> requiredItemPatterns =
             new Hashtable<String, Boolean>();
 
         final String[] patterns = assignment.getAllRequiredFiles();
 
         for (String pattern : patterns)
         {
             requiredItemPatterns.put(pattern, false);
         }
 
         SubmittableItemVisitor visitor = new SubmittableItemVisitor()
         {
             protected void accept(ISubmittableItem item)
             {
                 if (item.getKind() == SubmittableItemKind.FILE)
                 {
                     for (String reqPattern : patterns)
                     {
                         FilePattern pattern = new FilePattern(reqPattern);
 
                         if (pattern.matches(item.getFilename()))
                         {
                             requiredItemPatterns.put(reqPattern, true);
                         }
                     }
                 }
             }
         };
 
         try
         {
             visitor.visit(items);
         }
         catch (InvocationTargetException e)
         {
             // Do nothing; no exceptions should be thrown.
         }
 
         List<String> missingItemPatterns = new ArrayList<String>();
         for (String requiredPattern : requiredItemPatterns.keySet())
         {
             if (requiredItemPatterns.get(requiredPattern) == false)
             {
                 missingItemPatterns.add(requiredPattern);
             }
         }
 
         if (missingItemPatterns.size() == 0)
         {
             return null;
         }
         else
         {
             String[] array = new String[missingItemPatterns.size()];
             missingItemPatterns.toArray(array);
             return array;
         }
     }
 
 
     //~ Static/instance variables .............................................
 
     /* The root of the submission target object tree. */
     private RootTarget root;
 
     /* Indicates whether the submission generated a response. */
     private boolean hasResponse;
 
     /* Contains the response generated by the submission, if any. */
     private String response;
 
     /* Used to manage progress notifications for long-running tasks. */
     private ILongRunningTaskManager taskManager;
 }
