 /*
  * Copyright luntsys (c) 2004-2005,
  * Date: 2004-8-12
  * Time: 11:17:45
  *
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met: 1.
  * Redistributions of source code must retain the above copyright notice, this
  * list of conditions and the following disclaimer. 2. Redistributions in
  * binary form must reproduce the above copyright notice, this list of
  * conditions and the following disclaimer in the documentation and/or other
  * materials provided with the distribution.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *  
  */
 
 package com.luntsys.luntbuild.utility;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.StringReader;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Set;
 import java.util.HashSet;
 import java.util.Iterator;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.tools.ant.BuildException;
 import org.apache.xerces.parsers.DOMParser;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.w3c.dom.Text;
 
 import com.luntsys.luntbuild.BuildGenerator;
 
 /**
  * Revision (or change log) manager for various version control systems.
  *
  * @author robin shine
  * @author Jason Archer
  */
 public class Revisions {
 	private static Log logger = LogFactory.getLog(Revisions.class);
 
 	/** Defines maximum change log entries suggest for retrieve */
 	public static final long MAX_ENTRIES = 1000;
 	/** List of raw change log lines retrieved from the one or more VCS */
 	private List changeLogs = new ArrayList();
 
 	/** Set of VCS logins that have changed the VCS content since the last build */
 	private Set changeLogins = new HashSet();
 
 	/**
 	 * Is there any file or directory modification, add or delete action inside these revisions?
 	 */
 	private boolean fileModified = false;
 
     /** DocumentBuilder to use when creating the document to start with. */
     private static DocumentBuilder builder = null;
     static {
         try {
             builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
         } catch (Exception exc) {
             throw new ExceptionInInitializerError(exc);
         }
     }
     /** The complete log document for this build. */
     protected Document doc = builder.newDocument();
     protected Element revisionElement = null;
     protected Element lastLog = null;
     protected Element lastEntry = null;
 
     /** XML element name for the root element. */
     private static final String REVISIONS_TAG = "revisions";
     /** XML attribute name for a time. */
     private static final String TIME_ATTR = "time";
     /** XML element name for a change log. */
     private static final String CHANGELOG_TAG = "changelog";
     /** XML element name for a log. */
     private static final String LOG_TAG = "log";
     /** XML element name for a VCS. */
     private static final String VCS_TAG = "vcs";
     /** XML attribute name for a VCS class. */
     private static final String VCS_CLASS_ATTR = "class";
     /** XML element name for a log entry. */
     private static final String ENTRY_TAG = "logentry";
     /** XML element name for a author. */
     private static final String AUTHOR_TAG = "author";
     /** XML element name for a date. */
     private static final String DATE_TAG = "date";
     /** XML element name for a task. */
     private static final String TASK_TAG = "task";
     /** XML element name for a task name. */
     private static final String TASK_NAME_TAG = "name";
     /** XML element name for a task description. */
     private static final String TASK_DESC_TAG = "description";
     /** XML element name for a path. */
     private static final String PATH_TAG = "path";
     /** XML element name for a msg. */
     private static final String MSG_TAG = "msg";
     /** XML attribute name for a revision. */
     private static final String REVISION_ATTR = "revision";
     /** XML attribute name for a task user/owner. */
     private static final String TASK_USER_ATTR = "user";
     /** XML attribute name for a task status. */
     private static final String TASK_STATUS_ATTR = "status";
     /** XML attribute name for a path action. */
     private static final String PATH_ACTION_ATTR = "action";
     /** XML attribute name for a path revision. */
     private static final String PATH_REVISION_ATTR = "revision";
 
 	/**
 	 * Constructor, creates a new revision manager.
 	 */
 	public Revisions() {
 		changeLogs.clear();
 		changeLogins.clear();
 		fileModified = false;
 		revisionElement = doc.createElement(REVISIONS_TAG);
 	}
 
 	/**
 	 * Sets the time attribute.
 	 * 
 	 * @param time the time to set
 	 */
 	public void setTimeAttribute(String time) {
 		revisionElement.setAttribute(TIME_ATTR, time);
 	}
 
 	/**
 	 * Gets the raw change log lines from the VCSs.
 	 * 
 	 * @return the raw change log lines
 	 */
 	public List getChangeLogs() {
 		return changeLogs;
 	}
 
 	/**
 	 * Gets the VCS logins that have changed the VCS content since the last build.
 	 * 
 	 * @return the VCS logins
 	 */
 	public Set getChangeLogins() {
 		return changeLogins;
 	}
 
 	/**
 	 * Checks if any files have been modified inside these revisions.
 	 * 
 	 * @return <code>true</code> if files have been modified
 	 */
 	public boolean isFileModified() {
 		return fileModified;
 	}
 
 	/**
 	 * Sets the modified status.
 	 * 
 	 * @param fileModified set <code>true</code> if files have been modified
 	 */
 	public void setFileModified(boolean fileModified) {
 		this.fileModified = fileModified;
 	}
 
     /**
      * Gets the log.
      * 
      * @return the log
      */
     public Node getLog() {
         Element log = (Element) revisionElement.cloneNode(true);
         Element changelogs = doc.createElement(CHANGELOG_TAG + "s");
         log.appendChild(changelogs);
 
         Iterator it = getChangeLogs().iterator();
         while (it.hasNext()) {
             String msg = (String) it.next();
 
             Element messageElement = doc.createElement(CHANGELOG_TAG);
 
             msg = Luntbuild.xmlEncodeEntities(msg);
             StringBuffer message = new StringBuffer();
             BufferedReader r = null;
             try {
                 r = new BufferedReader(new StringReader(msg));
                 String line = r.readLine();
                 boolean first = true;
                 while (line != null) {
                     if (!first) message.append("</br>");
                     first = false;
                     message.append(line);
                     line = r.readLine();
                 }
             } catch (IOException e) {
                 // shouldn't be possible
                 message.append(msg);
             } finally {
             	if (r != null) try {r.close();} catch (Exception e) {}
             }
 
             Text messageText = doc.createCDATASection(message.toString());
             messageElement.appendChild(messageText);
 
             changelogs.appendChild(messageElement);
         }
 
         return log;
     }
 
     /**
      * Reads the change logs from the specified build.
      * 
      * @param build the build
      * @return the logs
      */
     public static NodeList readLogs(com.luntsys.luntbuild.db.Build build) {
         File revisionLog = new File(build.getPublishDir() + File.separatorChar + BuildGenerator.REVISION_XML_LOG);
         try {
             DOMParser parser = new DOMParser();
             parser.parse(revisionLog.getAbsolutePath());
             return parser.getDocument().getElementsByTagName(LOG_TAG);
         } catch (Exception e) {
             logger.error("Unable to read revision log for " + build.getSchedule().getProject().getName()
                     + "/" + build.getSchedule().getName() + "/" + build.getVersion(), e);
             return null;
         }
     }
 
     /**
      * Reads the change logins from the specified build.
      * 
      * @param build the build
      * @return the change logins
      */
     public static Set readChangeLogins(com.luntsys.luntbuild.db.Build build) {
         File revisionLog = new File(build.getPublishDir() + File.separatorChar + BuildGenerator.REVISION_XML_LOG);
         try {
             DOMParser parser = new DOMParser();
             parser.parse(revisionLog.getAbsolutePath());
             NodeList authors = parser.getDocument().getElementsByTagName(AUTHOR_TAG);
             Set logins = new HashSet();
             for (int i = 0; i < authors.getLength(); i++) {
                 logins.add(Luntbuild.getTextContent(authors.item(i)));
             }
             return logins;
         } catch (Exception e) {
             logger.error("Unable to read revision log for " + build.getSchedule().getProject().getName()
                     + "/" + build.getSchedule().getName() + "/" + build.getVersion(), e);
             return new HashSet();
         }
     }
 
     /**
      * Merges the specified set of revisions with this object.
      * 
      * @param revisions the revisions to merge
      */
     public void merge(Revisions revisions) {
         setFileModified(isFileModified() || revisions.isFileModified());
         getChangeLogs().addAll(revisions.getChangeLogs());
         getChangeLogins().addAll(revisions.getChangeLogins());
         NodeList logs = revisions.getLog().getChildNodes();
         for (int i = 0; i < logs.getLength(); i++) {
             if (logs.item(i).getNodeName().equals(LOG_TAG)) {
                 Node newLogNode = doc.importNode(logs.item(i), true);
                 revisionElement.appendChild(newLogNode);
             }
         }
     }
 
     /**
      * Adds a log.
      * 
      * @param classname the name of the class the log is for
      * @param description the description of the VCS the log is for
      */
     public void addLog(String classname, String description) {
         lastLog = doc.createElement(LOG_TAG);
         lastLog.setAttribute(VCS_CLASS_ATTR, classname);
         Element vcsElement = doc.createElement(VCS_TAG);
         description = Luntbuild.xmlEncodeEntities(description);
         StringBuffer vcsBuffer = new StringBuffer();
         try {
             BufferedReader r = new BufferedReader(new StringReader(description));
             String line = r.readLine();
             boolean first = true;
             while (line != null) {
                 if (!first) vcsBuffer.append("</br>");
                 first = false;
                 vcsBuffer.append(line);
                 line = r.readLine();
             }
         } catch (IOException e) {
             // shouldn't be possible
             vcsBuffer.append(description);
         }
         Text vcsText = doc.createCDATASection(vcsBuffer.toString());
         vcsElement.appendChild(vcsText);
         lastLog.appendChild(vcsElement);
         lastEntry = null;
         
         revisionElement.appendChild(lastLog);
     }
 
     /**
      * Adds a log entry to the last log added.
      * 
      * @param revision the revision or version of the entry
      * @param author the author of the entry
      * @param date the date of the entry
      * @param message the message/comment of the entry
      */
     public void addEntryToLastLog(String revision, String author, Date date, String message) {
         if (lastLog == null)
             throw new BuildException("No log exists to add entry to.");
         lastEntry = this.doc.createElement(ENTRY_TAG);
        if (!Luntbuild.isEmpty(revision))
            lastEntry.setAttribute(REVISION_ATTR, revision);
         Element authorElement = doc.createElement(AUTHOR_TAG);
         if (!Luntbuild.isEmpty(author))
             authorElement.appendChild(doc.createCDATASection(author));
         else
             authorElement.appendChild(doc.createCDATASection("anonymous"));
         lastEntry.appendChild(authorElement);
         Element dateElement = doc.createElement(DATE_TAG);
         if (date != null)
             dateElement.appendChild(doc.createCDATASection(new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(date)));
         lastEntry.appendChild(dateElement);
         lastEntry.appendChild(doc.createElement("tasks"));
         lastEntry.appendChild(doc.createElement("paths"));
 
         Element messageElement = doc.createElement(MSG_TAG);
         message = Luntbuild.xmlEncodeEntities(message);
         StringBuffer messageBuffer = new StringBuffer();
         BufferedReader r = null;
         try {
             r = new BufferedReader(new StringReader(message));
             String line = r.readLine();
             boolean first = true;
             while (line != null) {
                 if (!first) messageBuffer.append("</br>");
                 first = false;
                 messageBuffer.append(line);
                 line = r.readLine();
             }
         } catch (IOException e) {
             // shouldn't be possible
             messageBuffer.append(message);
         } finally {
         	if (r != null) try {r.close();} catch (Exception e) {}
         }
         Text messageText = doc.createCDATASection(messageBuffer.toString());
         messageElement.appendChild(messageText);
         lastEntry.appendChild(messageElement);
         
         lastLog.appendChild(lastEntry);
     }
 
     /**
      * Adds a task to the last entry added.
      * 
      * @param name the name of the task
      * @param user the user/owner of the task
      * @param status the status of the task
      * @param description the description of the task
      */
     public void addTaskToLastEntry(String name, String user, String status, String description) {
         if (lastEntry == null)
             throw new BuildException("No entry exists to add task to.");
         Element taskElement = doc.createElement(TASK_TAG);
         Element tasks = (Element) lastEntry.getElementsByTagName("tasks").item(0);
        if (!Luntbuild.isEmpty(user))
            taskElement.setAttribute(TASK_USER_ATTR, user);
        if (!Luntbuild.isEmpty(status))
            taskElement.setAttribute(TASK_STATUS_ATTR, status);
         Element nameElement = doc.createElement(TASK_NAME_TAG);
         if (!Luntbuild.isEmpty(name))
             nameElement.appendChild(doc.createCDATASection(name));
         taskElement.appendChild(nameElement);
         Element descriptionElement = doc.createElement(TASK_DESC_TAG);
         if (!Luntbuild.isEmpty(description))
             descriptionElement.appendChild(doc.createCDATASection(description));
         taskElement.appendChild(descriptionElement);
         
         tasks.appendChild(taskElement);
     }
 
     /**
      * Adds a path to the last entry added.
      * 
      * @param path the path
      * @param action the action applied to the path
      * @param revision the revision or version of the path
      */
     public void addPathToLastEntry(String path, String action, String revision) {
         if (lastEntry == null)
             throw new BuildException("No entry exists to add path to.");
         Element pathElement = doc.createElement(PATH_TAG);
         Element paths = (Element) lastEntry.getElementsByTagName("paths").item(0);
        if (!Luntbuild.isEmpty(action))
            pathElement.setAttribute(PATH_ACTION_ATTR, action);
        if (!Luntbuild.isEmpty(revision))
            pathElement.setAttribute(PATH_REVISION_ATTR, revision);
         if (!Luntbuild.isEmpty(path))
             pathElement.appendChild(doc.createCDATASection(path));
         
         paths.appendChild(pathElement);
     }
 }
