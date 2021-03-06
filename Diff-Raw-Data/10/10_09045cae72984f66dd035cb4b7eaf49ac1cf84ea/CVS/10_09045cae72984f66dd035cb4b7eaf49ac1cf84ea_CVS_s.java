 /*******************************************************************************
  * CruiseControl, a Continuous Integration Toolkit
  * Copyright (c) 2001, ThoughtWorks, Inc.
  * 651 W Washington Ave. Suite 500
  * Chicago, IL 60661 USA
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  *     + Redistributions of source code must retain the above copyright
  *       notice, this list of conditions and the following disclaimer.
  *
  *     + Redistributions in binary form must reproduce the above
  *       copyright notice, this list of conditions and the following
  *       disclaimer in the documentation and/or other materials provided
  *       with the distribution.
  *
  *     + Neither the name of ThoughtWorks, Inc., CruiseControl, nor the
  *       names of its contributors may be used to endorse or promote
  *       products derived from this software without specific prior
  *       written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  ******************************************************************************/
 package net.sourceforge.cruisecontrol.sourcecontrols;
 
 import net.sourceforge.cruisecontrol.CruiseControlException;
 import net.sourceforge.cruisecontrol.Modification;
 import net.sourceforge.cruisecontrol.SourceControl;
 import net.sourceforge.cruisecontrol.util.Commandline;
 import net.sourceforge.cruisecontrol.util.StreamPumper;
 import org.apache.log4j.Logger;
 
 import java.io.*;
 import java.lang.reflect.Method;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /**
  * This class implements the SourceControlElement methods for a CVS repository.
  * The call to CVS is assumed to work without any setup. This implies that if
  * the authentication type is pserver the call to cvs login should be done
  * prior to calling this class.
  *
  * @author  <a href="mailto:pj@thoughtworks.com">Paul Julius</a>
  * @author  Robert Watkins
  * @author  Frederic Lavigne
  * @author  <a href="mailto:jcyip@thoughtworks.com">Jason Yip</a>
  * @author  Marc Paquette
  * @author <a href="mailto:johnny.cass@epiuse.com">Johnny Cass</a>
  * @author <a href="mailto:m@loonsoft.com">McClain Looney</a>
  */
 public class CVS implements SourceControl {
 
     private Hashtable _properties = new Hashtable();
     private String _property;
     private String _propertyOnDelete;
 
     /**
      * CVS allows for mapping user names to email addresses.
      * If CVSROOT/users exists, it's contents will be parsed and stored in this
      * hashtable.
      */
     private Hashtable mailAliases = new Hashtable();
 
     /**
      * The caller must provide the CVSROOT to use when calling CVS.
      */
     private String cvsroot;
 
     /**
      * The caller must indicate where the local copy of the repository
      * exists.
      */
     private String local;
 
     /**
      * The CVS tag we are dealing with.
      */
     private String tag;
 
     /** enable logging for this class */
     private static Logger log = Logger.getLogger(CVS.class);
 
     /**
      *  This line delimits seperate files in the CVS log information.
      */
     private final static String CVS_FILE_DELIM =
             "=============================================================================";
 
     /**
      * This is the keyword that precedes the name of the RCS filename in the CVS
      * log information.
      */
     private final static String CVS_RCSFILE_LINE = "RCS file: ";
 
     /**
      * This is the keyword that precedes the name of the working filename in the
      * CVS log information.
      */
     private final static String CVS_WORKINGFILE_LINE = "Working file: ";
 
     /**
      * This line delimits the different revisions of a file in the CVS log
      * information.
      */
     private final static String CVS_REVISION_DELIM =
             "----------------------------";
 
     /**
      * This is the keyword that precedes the timestamp of a file revision in the
      * CVS log information.
      */
     private final static String CVS_REVISION_DATE = "date:";
 
     /**
      * This is the keyword that precedes the author of a file revision in the
      * CVS log information.
      */
     private final static String CVS_REVISION_AUTHOR = "author:";
 
     /**
      * This is the keyword that precedes the state keywords of a file revision
      * in the CVS log information.
      */
     private final static String CVS_REVISION_STATE = "state:";
 
     /**
      * This is the name of the tip of the main branch, which needs special handling with
      * the log entry parser
      */
     private final static String CVS_HEAD_TAG = "HEAD";
 
     /**
      * This is the keyword that precedes the revision as found in the
      * CVS log information.
      */
     private final static String CVS_REVISION_REVISION = "revision";
 
     /**
      * This is the keyword that tells us when we have reaced the ned of the
      * header as found in the CVS log information.
      */
     private final static String CVS_DESCRIPTION = "description:";
 
     /**
      * This is a state keyword which indicates that a revision to a file was not
      * relevant to the current branch, or the revision consisted of a deletion
      * of the file (removal from branch..).
      */
     private final static String CVS_REVISION_DEAD = "dead";
 
     /**
      * This is the log string set for files that are added on a different branch
      */
     private final static String CVS_BRANCH_ADDED =
             "was initially added on branch";
 
     /**
      * System dependent new line seperator.
      */
     private final static String NEW_LINE = System.getProperty("line.separator");
 
     /**
      * This is the date format required by commands passed to CVS.
      */
     final static SimpleDateFormat CVSDATE =
             new SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'GMT'");
 
     /**
      *  This is the date format returned in the log information from CVS.
      */
     final static SimpleDateFormat LOGDATE =
             new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");
 
     static {
         // The timezone is hard coded to GMT to prevent problems with it being
         // formatted as GMT+00:00. However, we still need to set the time zone
         // of the formatter so that it knows it's in GMT.
         CVSDATE.setTimeZone(java.util.TimeZone.getTimeZone("GMT"));
     }
 
     /**
      * Sets the CVSROOT for all calls to CVS.
      *
      *@param cvsroot CVSROOT to use.
      */
     public void setCvsRoot(String cvsroot) {
         this.cvsroot = cvsroot;
     }
 
     /**
      * Sets the local working copy to use when making calls to CVS.
      *
      *@param local String indicating the relative or absolute path to the local
      *      working copy of the module of which to find the log history.
      */
     public void setLocalWorkingCopy(String local) throws CruiseControlException {
         this.local = local;
         if (local != null && !new File(local).exists()) {
             throw new CruiseControlException(
                     "Local working copy \"" + local + "\" does not exist!");
         }
     }
 
     /**
      * Set the cvs tag.  Note this should work with names, numbers, and anything
      * else you can put on log -rTAG
      * @param tag the cvs tag
      */
     public void setTag(String tag) {
         this.tag = tag;
     }
 
     public void setProperty(String property) {
         _property = property;
     }
 
     public void setPropertyOnDelete(String propertyOnDelete) {
         _propertyOnDelete = propertyOnDelete;
     }
 
     public Hashtable getProperties() {
         return _properties;
     }
 
     public void validate() throws CruiseControlException {
         if(cvsroot == null)
             throw new CruiseControlException("'cvsroot' is a required attribute on CVS");
     }
 
     /**
      * Returns a List of Modifications detailing all the changes between the
      * last build and the latest revision at the repository
      *
      *@param lastBuild last build time
      *@return maybe empty, never null.
      */
     public List getModifications(Date lastBuild, Date now) {
         List mods = null;
         try {
             mods = execHistoryCommand(buildHistoryCommand(lastBuild));
         } catch (Exception e) {
             log.error("Log command failed to execute succesfully", e);
         }
 
         if (mods == null) {
             return new ArrayList();
         }
         return mods;
     }
 
     /**
      * Get CVS's idea of user/address mapping.
      *
      * @return a Hashtable containing the mapping defined in CVSROOT/users.
      * If CVSROOT/users doesn't exist, an empty Hashtable is returned.
      */
     private Hashtable getMailAliases() {
         if (mailAliases == null) {
             mailAliases = new Hashtable();
             Commandline commandLine = new Commandline();
             commandLine.setExecutable("cvs");
 
             if (cvsroot != null) {
                 commandLine.createArgument().setValue("-d");
                 commandLine.createArgument().setValue(cvsroot);
             }
 
             commandLine.createArgument().setLine("-q co -p CVSROOT/users");
             log.debug("Executing: " + commandLine);
 
             Process p = null;
             try {
                 p = Runtime.getRuntime().exec(commandLine.getCommandline());
                 logErrorStream(p);
                 InputStream is = p.getInputStream();
                 BufferedReader in = new BufferedReader(new InputStreamReader(is));
 
                 String line;
 
                 while ((line = in.readLine()) != null) {
                     log.debug("Mapping " + line);
                     int colon = line.indexOf(':');
                     if (colon < 1) {
                         // log an error
                     } else {
                         String user = line.substring(0, colon);
                         String address = line.substring(colon + 1);
                         mailAliases.put(user, address);
                     }
                 }
 
                 p.waitFor();
                 p.getInputStream().close();
                 p.getOutputStream().close();
                 p.getErrorStream().close();
             } catch (Exception e) {
                 log.error("Failed reading mail aliases", e);
             }
 
             if (p == null || p.exitValue() != 0) {
                 mailAliases = new Hashtable();
             }
         }
 
         return mailAliases;
     }
 
     /**
      *@param lastBuildTime
      *@return CommandLine for "cvs -d CVSROOT -q log -d ">lastbuildtime" "
      */
     public Commandline buildHistoryCommand(Date lastBuildTime) {
         Commandline commandLine = new Commandline();
         commandLine.setExecutable("cvs");
 
         if (cvsroot != null) {
             commandLine.createArgument().setValue("-d");
             commandLine.createArgument().setValue(cvsroot);
         }
         commandLine.createArgument().setValue("-q");
 
         commandLine.createArgument().setValue("log");
         String dateRange = ">" + formatCVSDate(lastBuildTime);
         commandLine.createArgument().setValue("-d" + dateRange);
 
         if (tag != null) {
             // add -b and -rTAG to list changes relative to the current branch,
             // not relative to the default branch, which is HEAD
 
             // note: -r cannot have a space between itself and the tag spec.
             commandLine.createArgument().setValue("-r" + tag);
         } else {
             // This is used to include the head only if a Tag is not specified.
             commandLine.createArgument().setValue("-b");
         }
 
         return commandLine;
     }
 
     public static String formatCVSDate(Date date) {
         return CVSDATE.format(date);
     }
 
     /**
      * Parses the input stream, which should be from the cvs log command. This
      * method will format the data found in the input stream into a List of
      * Modification instances.
      *
      *@param input InputStream to get log data from.
      *@return List of Modification elements, maybe empty never null.
      *@exception IOException
      */
     protected List parseStream(InputStream input) throws IOException {
         BufferedReader reader = new BufferedReader(new InputStreamReader(input));
 
         // Read to the first RCS file name. The first entry in the log
         // information will begin with this line. A CVS_FILE_DELIMITER is NOT
         // present. If no RCS file lines are found then there is nothing to do.
         String line = readToNotPast(reader, CVS_RCSFILE_LINE, null);
         ArrayList mods = new ArrayList();
 
         while (line != null) {
             // Parse the single file entry, which may include several
             // modifications.
             List returnList = parseEntry(reader);
 
             //Add all the modifications to the local list.
             mods.addAll(returnList);
 
             // Read to the next RCS file line. The CVS_FILE_DELIMITER may have
             // been consumed by the parseEntry method, so we cannot read to it.
             line = readToNotPast(reader, CVS_RCSFILE_LINE, null);
         }
 
         return mods;
     }
 
     private void getRidOfLeftoverData(InputStream stream) {
         StreamPumper outPumper = new StreamPumper(stream, null);
         new Thread(outPumper).start();
     }
 
     /**
      * This method encapsulates the strange behavior that the windows CVS client
      * wants relative paths to use the forward-slash character (/) rather than
      * the windows standard back-slash (\). This should work fine on *Nix
      * machines and windows machines.
      *
      *@return The relative path to the working copy using (/) characters as path
      *      separator.
      */
     private String getLocalPath() {
         return local.replace('\\', '/');
     }
 
     private boolean preJava13() {
         String javaVersion = System.getProperty("java.version");
         return javaVersion.startsWith("1.1") || javaVersion.startsWith("1.2");
     }
 
     private List execHistoryCommand(Commandline command) throws Exception {
         Process p = null;
 
         if (local != null) {
             if (System.getProperty("os.name").equalsIgnoreCase("Linux")
                     && !(preJava13())) {
                 log.debug("Executing: " + command + " in directory: "
                           + getLocalPath());
 
                 // Use reflection to call this JDK 1.3 method
                 //p = Runtime.getRuntime().exec(command.getCommandline(),
                 // null, new File(getLocalPath()));
 
                 Method execMethod = Runtime.class.getMethod(
                         "exec", new Class[]{String[].class, String[].class,
                                             File.class});
 
                 // envp is null to inherit parent env (for things like
                 // CVS_RSH=ssh etc.)
                 String[] envp = null;
                 Object[] args = new Object[]{command.getCommandline(),
                                              envp, new File(getLocalPath())};
 
                 p = (Process) execMethod.invoke(Runtime.getRuntime(), args);
             } else {
                 command.createArgument().setValue(getLocalPath());
             }
         }
 
         if (p == null) {
             log.debug("Executing: " + command);
             p = Runtime.getRuntime().exec(command.getCommandline());
         }
 
         logErrorStream(p);
         InputStream cvsLogStream = p.getInputStream();
         List mods = parseStream(cvsLogStream);
 
         getRidOfLeftoverData(cvsLogStream);
         p.waitFor();
         p.getInputStream().close();
         p.getOutputStream().close();
         p.getErrorStream().close();
 
         mailAliases = getMailAliases();
 
         return mods;
     }
 
     protected void setMailAliases(Hashtable mailAliases) {
         this.mailAliases = mailAliases;
     }
 
     private void logErrorStream(Process p) {
         StreamPumper errorPumper = new StreamPumper(p.getErrorStream(),
                                                     new PrintWriter(System.err, true));
         new Thread(errorPumper).start();
     }
 
     //(PENDING) Extract CVSEntryParser class
     /**
      * Parses a single file entry from the reader. This entry may contain zero or
      * more revisions. This method may consume the next CVS_FILE_DELIMITER line
      * from the reader, but no further.
      *
      *@param reader Reader to parse data from.
      *@return modifications found in this entry; maybe empty, never null.
      *@exception IOException
      */
     private List parseEntry(BufferedReader reader) throws IOException {
         ArrayList mods = new ArrayList();
 
         String nextLine = "";
 
         // Read to the working file name line to get the filename. It is ASSUMED
         // that a line will exist with the working file name on it.
         String workingFileLine = readToNotPast(reader, CVS_WORKINGFILE_LINE, null);
         String workingFileName = workingFileLine.substring(CVS_WORKINGFILE_LINE.length());
         String branchRevisionName = null;
 
         if (tag != null && !tag.equals(CVS_HEAD_TAG)) {
             // Look for the revision of the form "tag: *.(0.)y ". this doesn't work for HEAD
             // get line with branch revision on it.
 
             String branchRevisionLine = readToNotPast(reader, "\t"+tag+": ", CVS_DESCRIPTION);
 
             if(branchRevisionLine!=null) {
                 // Look for the revision of the form "tag: *.(0.)y "
                 branchRevisionName = branchRevisionLine.substring(tag.length()+3);
                 if(branchRevisionName.charAt(branchRevisionName.lastIndexOf(".")-1)=='0') {
                     branchRevisionName= branchRevisionName.substring(0,branchRevisionName.lastIndexOf(".")-2)+
                     branchRevisionName.substring(branchRevisionName.lastIndexOf("."));
                 }
             }
         }
 
        while (reader.ready() && nextLine != null && !nextLine.startsWith(CVS_FILE_DELIM)) {
             nextLine = readToNotPast(reader, "revision", CVS_FILE_DELIM);
             if (nextLine == null) {
                 //No more revisions for this file.
                 break;
             }
 
             nextLine.length();
             StringTokenizer tokens = new StringTokenizer(nextLine, " ");
             tokens.nextToken();
             String revision = tokens.nextToken();
             if(tag != null && !tag.equals(CVS_HEAD_TAG)) {
                 String itsBranchRevisionName = revision.substring(0,revision.lastIndexOf('.'));
                 if(!itsBranchRevisionName.equals(branchRevisionName)) {
                     break;
                 }
             }
 
             // Read to the revision date. It is ASSUMED that each revision
             // section will include this date information line.
             nextLine = readToNotPast(reader, CVS_REVISION_DATE, CVS_FILE_DELIM);
             if (nextLine == null) {
                 //No more revisions for this file.
                 break;
             }
 
             tokens = new StringTokenizer(nextLine, " \t\n\r\f;");
             // First token is the keyword for date, then the next two should be
             // the date and time stamps.
             tokens.nextToken();
             String dateStamp = tokens.nextToken();
             String timeStamp = tokens.nextToken();
 
             // The next token should be the author keyword, then the author name.
             tokens.nextToken();
             String authorName = tokens.nextToken();
 
             // The next token should be the state keyword, then the state name.
             tokens.nextToken();
             String stateKeyword = tokens.nextToken();
 
             // if no lines keyword then file is added
             boolean isAdded = false;
             try {
                 tokens.nextToken();
             } catch (NoSuchElementException noLinesFoundIgnore) {
                 isAdded = true;
             }
 
             // All the text from now to the next revision delimiter or working
             // file delimiter constitutes the messsage.
             String message = "";
             nextLine = reader.readLine();
             boolean multiLine = false;
 
             while (nextLine != null && !nextLine.startsWith(CVS_FILE_DELIM)
                     && !nextLine.startsWith(CVS_REVISION_DELIM)) {
 
                 if (multiLine) {
                     message += NEW_LINE;
                 } else {
                     multiLine = true;
                 }
                 message += nextLine;
 
                 //Go to the next line.
                 nextLine = reader.readLine();
             }
 
             Modification nextModification = new Modification();
 
             int lastSlashIndex = workingFileName.lastIndexOf("/");
             nextModification.fileName = workingFileName.substring(lastSlashIndex+1);
             if (lastSlashIndex != -1) {
                 nextModification.folderName = workingFileName.substring(0, lastSlashIndex);
             } else {
                 nextModification.folderName = "";
             }
 
             try {
                 nextModification.modifiedTime = LOGDATE.parse(dateStamp + " "
                                                               + timeStamp + " GMT");
             } catch (ParseException pe) {
                 log.error("Error parsing cvs log for date and time", pe);
                 return null;
             }
 
             nextModification.userName = authorName;
 
 
             String address = (String)mailAliases.get(authorName);
             if(address != null) {
                 nextModification.emailAddress = address;
             }
 
             nextModification.comment = (message != null ? message : "");
 
             if(stateKeyword.equalsIgnoreCase(CVS_REVISION_DEAD) && message.indexOf("was initially added on branch")!=-1) {
                 log.debug("skipping branch addition activity for " + nextModification);
                 //this prevents additions to a branch from showing up as action "deleted" from head
                 continue;
             }
 
             if (stateKeyword.equalsIgnoreCase(CVS_REVISION_DEAD)) {
                 nextModification.type = "deleted";
                  if( _propertyOnDelete != null ) {
                      _properties.put(_propertyOnDelete, "true");
                  }
             } else if (isAdded) {
                 nextModification.type = "added";
             } else {
                 nextModification.type = "modified";
             }
             if( _property != null ) {
                 _properties.put(_property, "true");
             }
             mods.add(nextModification);
         }
         return mods;
     }
 
     /**
      * This method will consume lines from the reader up to the line that begins
      * with the String specified but not past a line that begins with the
      * notPast String. If the line that begins with the beginsWith String is
      * found then it will be returned. Otherwise null is returned.
      *
      *@param reader Reader to read lines from.
      *@param beginsWith String to match to the beginning of a line.
      *@param notPast String which indicates that lines should stop being consumed,
      *      even if the begins with match has not been found. Pass null to this
      *      method to ignore this string.
      *@return String that begin as indicated, or null if none matched to the end
      *      of the reader or the notPast line was found.
      *@throws IOException
      */
     private String readToNotPast(BufferedReader reader, String beginsWith,
                                  String notPast) throws IOException {
         boolean checkingNotPast = notPast != null;
 
         String nextLine = reader.readLine();
         while (nextLine != null && !nextLine.startsWith(beginsWith)) {
             if (checkingNotPast && nextLine.startsWith(notPast)) {
                 return null;
             }
             nextLine = reader.readLine();
         }
 
         return nextLine;
     }
 
 }
