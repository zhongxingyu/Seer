 /*
  * ====================================================================
  * Copyright (c) 2004-2007 TMate Software Ltd.  All rights reserved.
  *
  * This software is licensed as described in the file COPYING, which
  * you should have received as part of this distribution.  The terms
  * are also available at http://svnkit.com/license.html.
  * If newer versions of this license are posted there, you may use a
  * newer version instead, at your option.
  * ====================================================================
  */
 package org.tmatesoft.svn.cli;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.text.MessageFormat;
 import java.util.Iterator;
 
 import org.tmatesoft.svn.cli.svn.SVNCommandEnvironment;
 import org.tmatesoft.svn.core.SVNErrorCode;
 import org.tmatesoft.svn.core.SVNErrorMessage;
 import org.tmatesoft.svn.core.SVNException;
 import org.tmatesoft.svn.core.internal.util.SVNFormatUtil;
 import org.tmatesoft.svn.core.internal.util.SVNPathUtil;
 import org.tmatesoft.svn.core.internal.wc.SVNErrorManager;
 import org.tmatesoft.svn.core.internal.wc.SVNFileUtil;
 import org.tmatesoft.svn.util.Version;
 
 
 /**
  * @version 1.1.2
  * @author  TMate Software Ltd.
  */
 public class SVNCommandUtil {
     
     public static String getLocalPath(String path) {
         path = path.replace('/', File.separatorChar);
         if ("".equals(path)) {
             path = ".";
         }
         return path;
     }
 
     public static boolean isURL(String pathOrUrl){
         return SVNPathUtil.isURL(pathOrUrl);
     }
     
     public static void mergeFileExternally(AbstractSVNCommandEnvironment env, String basePath, String repositoryPath, 
             String localPath, String mergeResultPath) throws SVNException {
         String[] testEnvironment = SVNFileUtil.getTestEnvironment();
         String mergeToolCommand = testEnvironment[1];
         if (testEnvironment[1] == null) {
             mergeToolCommand = SVNFileUtil.getEnvironmentVariable("SVN_MERGE");
             if (mergeToolCommand == null) {
                 mergeToolCommand = env.getClientManager().getOptions().getMergeTool();
             }
             testEnvironment = null;
         } else {
             mergeToolCommand = testEnvironment[1];
             testEnvironment = new String[] {"SVNTEST_EDITOR_FUNC=" + (testEnvironment[2] == null ? "" : testEnvironment[2])};
         }
 
         if (mergeToolCommand != null) {
             mergeToolCommand = mergeToolCommand.trim();
             if (mergeToolCommand.length() == 0) {
                 SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.CL_NO_EXTERNAL_MERGE_TOOL, 
                         "The SVN_MERGE environment variable is empty or consists solely of whitespace. Expected a shell command.");
                 SVNErrorManager.error(err);
             }
         } else {
             SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.CL_NO_EXTERNAL_MERGE_TOOL, 
                     "The environment variable SVN_MERGE and the merge-tool-cmd run-time configuration option were not set.");
             SVNErrorManager.error(err);
         }
         
         String result = null;
         if (SVNFileUtil.isWindows) {
             String merger = mergeToolCommand.toLowerCase();
             if (!(merger.endsWith(".exe") || merger.endsWith(".bat") || merger.endsWith(".cmd"))) {
                 result = SVNFileUtil.execCommand(new String[] { "cmd.exe", "/C", merger, basePath, repositoryPath, 
                         localPath, mergeResultPath }, testEnvironment, true);
             } else {
                 result = SVNFileUtil.execCommand(new String[] { merger, basePath, repositoryPath, localPath, 
                         mergeResultPath }, testEnvironment, true);
             }
         } else {
             result = SVNFileUtil.execCommand(new String[] { mergeToolCommand, basePath, repositoryPath, localPath, 
                     mergeResultPath }, testEnvironment, true);
         }
 
         if (result == null) {
             SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.EXTERNAL_PROGRAM, "Editor command '" + 
                     mergeToolCommand + " " + basePath + " " + repositoryPath + " " + localPath + " " + 
                     mergeResultPath +  "' failed.");
             SVNErrorManager.error(err);
         }
     }
 
     public static void editFileExternally(AbstractSVNCommandEnvironment env, String editorCommand, String path) throws SVNException {
         editorCommand = getEditorCommand(env, editorCommand);
         String testEnv[] = SVNFileUtil.getTestEnvironment();
         if (testEnv[0] != null) {
             testEnv = new String[] {"SVNTEST_EDITOR_FUNC=" + (testEnv[2] != null ? testEnv[2] : "")};
         }
         String result = null;
         if (SVNFileUtil.isWindows) {
             String editor = editorCommand.trim().toLowerCase();
             if (!(editor.endsWith(".exe") || editor.endsWith(".bat") || editor.endsWith(".cmd"))) {
                 result = SVNFileUtil.execCommand(new String[] {"cmd.exe", "/C", editorCommand, 
                         path}, testEnv, false);
             } else {
                 result = SVNFileUtil.execCommand(new String[] {editorCommand, path}, testEnv, false);
             }
         } else {
             result = SVNFileUtil.execCommand(new String[] {editorCommand, path}, testEnv, false);
         }
         
         if (result == null) {
             SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.EXTERNAL_PROGRAM, "Editor command '" + 
                     editorCommand + " " + path + "' failed.");
             SVNErrorManager.error(err);
         }
     }
     
     public static byte[] runEditor(AbstractSVNCommandEnvironment env, String editorCommand, byte[] existingValue, String prefix) throws SVNException {
         File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        File tmpFile = SVNFileUtil.createUniqueFile(tmpDir, prefix, ".tmp", true);
         OutputStream os = null;
         try {
             os = SVNFileUtil.openFileForWriting(tmpFile);
             os.write(existingValue);
         } catch (IOException e) {
             SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.IO_ERROR, e.getMessage());
             SVNErrorManager.error(err);
         } finally {
             SVNFileUtil.closeFile(os);
         }
         tmpFile.setLastModified(System.currentTimeMillis() - 2000);
         long timestamp = tmpFile.lastModified();
         editorCommand = getEditorCommand(env, editorCommand);
         String[] testEnv = SVNFileUtil.getTestEnvironment();
         if (testEnv[0] != null) {
             testEnv = new String[] {"SVNTEST_EDITOR_FUNC=" + (testEnv[2] != null ? testEnv[2] : "")};
         } else {
             testEnv = null;
         }
         try {
             String result = null;
             if (SVNFileUtil.isWindows) {
                 String editor = editorCommand.trim().toLowerCase();
                 if (!(editor.endsWith(".exe") || editor.endsWith(".bat") || editor.endsWith(".cmd"))) {
                     result = SVNFileUtil.execCommand(new String[] {"cmd.exe", "/C", editorCommand, tmpFile.getAbsolutePath()}, testEnv, false);
                 } else {
                     result = SVNFileUtil.execCommand(new String[] {editorCommand, tmpFile.getAbsolutePath()}, testEnv, false);
                 }
             } else {
                 result = SVNFileUtil.execCommand(new String[] {editorCommand, tmpFile.getAbsolutePath()}, testEnv, false);
             }
             if (result == null) {
                 SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.IO_ERROR, "Editor command '" + editorCommand + " " + tmpFile.getAbsolutePath() + "' failed.");
                 SVNErrorManager.error(err);
             }
             // now read from file.
             if (timestamp == tmpFile.lastModified()) {
                 return null;
             }
             InputStream is = null;
             ByteArrayOutputStream bos = new ByteArrayOutputStream();
             byte[] buffer = new byte[2048];
             try {
                 is = SVNFileUtil.openFileForReading(tmpFile);
                 while(true) {
                     int read = is.read(buffer);
                     if (read <= 0) {
                         break;
                     }
                     bos.write(buffer, 0, read);
                 }
             } catch (IOException e) {
                 SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.IO_ERROR, e.getMessage());
                 SVNErrorManager.error(err);
             } finally {
                 SVNFileUtil.closeFile(is);
             }
             return bos.toByteArray();
         } finally {
             SVNFileUtil.deleteFile(tmpFile);
         }
     }
     
     //TODO: password masking
     public static String prompt(String promptMessage, SVNCommandEnvironment env) throws SVNException {
         System.out.print(promptMessage);
         System.out.flush();
         String input = null;
         InputReader reader = new InputReader(System.in);
         Thread readerThread = new Thread(reader);
         readerThread.setDaemon(true);
         readerThread.start();
         while (true) {
             env.checkCancelled();
             if (reader.myIsFinished) {
                 input = reader.getReadInput();
                 break;
             }
             try {
                 Thread.sleep(1);
             } catch (InterruptedException e) {
             }
         }
         if (reader.getError() != null) {
             SVNErrorManager.error(reader.getError());
         }
         if (input == null) {
             SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.IO_ERROR, 
                         "Can't read stdin: End of file found");
                 SVNErrorManager.error(err);
         } 
         return input;
     }
 
     private static String getEditorCommand(AbstractSVNCommandEnvironment env, String editorCommand) throws SVNException {
         if (editorCommand != null) {
             return editorCommand;
         } 
         String[] testEnvironment = SVNFileUtil.getTestEnvironment();
         String command = testEnvironment[0];
         if (command == null) {
             command = SVNFileUtil.getEnvironmentVariable("SVN_EDITOR");
             if (command == null) {
                 command = env.getClientManager().getOptions().getEditor();
             }
             if (command == null) {
                 command = SVNFileUtil.getEnvironmentVariable("VISUAL");
             }
             if (command == null) {
                 command = SVNFileUtil.getEnvironmentVariable("EDITOR");
             }
         }
         String errorMessage = null;
         if (command == null) {
             errorMessage = 
                 "None of the environment variables SVN_EDITOR, VISUAL or EDITOR is " +
                 "set, and no 'editor-cmd' run-time configuration option was found"; 
         } else if ("".equals(command.trim())) {
             errorMessage = 
                 "The EDITOR, SVN_EDITOR or VISUAL environment variable or " +
                 "'editor-cmd' run-time configuration option is empty or " +
                 "consists solely of whitespace. Expected a shell command.";
         }
         if (errorMessage != null) {
             SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.CL_NO_EXTERNAL_EDITOR, errorMessage));
         }
         return command;
     }
 
     public static int getLinesCount(String str) {
         if ("".equals(str)) {
             return 1;
         }
         int count = 1;
         for (int i = 0; i < str.length(); i++) {
             if (str.charAt(i) == '\r') {
                 count++;
                 if (i < str.length() - 1 && str.charAt(i + 1) == '\n') {
                     i++;
                 }
             } else if (str.charAt(i) == '\n') {
                 count++;
             }
         }
         if (count == 0) {
             count++;
         }
         return count;
     }
 
     public static String getCommandHelp(AbstractSVNCommand command) {
         StringBuffer help = new StringBuffer();
         help.append(command.getName());
         if (command.getAliases().length > 0) {
             help.append(" (");
             for (int i = 0; i < command.getAliases().length; i++) {
                 help.append(command.getAliases()[i]);
                 if (i + 1 < command.getAliases().length) {
                     help.append(", ");
                 }
             }
             help.append(")");
         }
         if (!"".equals(command.getName())) {
             help.append(": ");
         }
         help.append(command.getDescription());
         help.append("\n");
         if (!command.getSupportedOptions().isEmpty()) {
             if (!command.getValidOptions().isEmpty()) {
                 help.append("\nValid Options:\n");
                 for (Iterator options = command.getValidOptions().iterator(); options.hasNext();) {
                     AbstractSVNOption option = (AbstractSVNOption) options.next();
                     help.append("  ");
                     String optionDesc = null;
                     if (option.getAlias() != null) {
                         optionDesc = "-" + option.getAlias() + " [--" + option.getName() + "]";
                     } else {
                         optionDesc = "--" + option.getName();
                     }
                     
                     if (!option.isUnary()) {
                         optionDesc += " ARG";
                     }
                 
                     help.append(SVNFormatUtil.formatString(optionDesc, 24, true));
                     help.append(" : ");
                     help.append(option.getDescription(command));
                     help.append("\n");
                 }
             }
 
             if (!command.getGlobalOptions().isEmpty()) {
                 help.append("\nGlobal Options:\n");
                 for (Iterator options = command.getGlobalOptions().iterator(); options.hasNext();) {
                     AbstractSVNOption option = (AbstractSVNOption) options.next();
                     help.append("  ");
                     String optionDesc = null;
                     if (option.getAlias() != null) {
                         optionDesc = "-" + option.getAlias() + " [--" + option.getName() + "]";
                     } else {
                         optionDesc = "--" + option.getName();
                     }
                     
                     if (!option.isUnary()) {
                         optionDesc += " ARG";
                     }
                 
                     help.append(SVNFormatUtil.formatString(optionDesc, 24, true));
                     help.append(" : ");
                     help.append(option.getDescription(command));
                     help.append("\n");
                 }
             }
         }
         return help.toString();
     }
 
     public static String getVersion(AbstractSVNCommandEnvironment env, boolean quiet) {
         String version = Version.getMajorVersion() + "." + Version.getMinorVersion() + "." + Version.getMicroVersion();
         String revNumber = Version.getRevisionNumber() < 0 ? "SNAPSHOT" : Long.toString(Version.getRevisionNumber());
         String message = MessageFormat.format(env.getProgramName() + ", version {0}\n", new Object[] {version + " (r" + revNumber + ")"});
         if (quiet) {
             message = version;
         }
         if (!quiet) {
             message += 
                 "\nCopyright (C) 2004-2007 TMate Software.\n" +
                 "SVNKit is open source (GPL) software, see http://svnkit.com/ for more information.\n" +
                 "SVNKit is pure Java (TM) version of Subversion, see http://subversion.tigris.org/";
         }
         return message;
     
     }
 
     public static String getGenericHelp(String programName, String header, String footer) {
         StringBuffer help = new StringBuffer();
         if (header != null) {
             String version = Version.getMajorVersion() + "." + Version.getMinorVersion() + "." + Version.getMicroVersion();
             header = MessageFormat.format(header, new Object[] {programName, version});
             help.append(header);
         }
         for (Iterator commands = AbstractSVNCommand.availableCommands(); commands.hasNext();) {
             AbstractSVNCommand command = (AbstractSVNCommand) commands.next();
             help.append("\n   ");
             help.append(command.getName());
             if (command.getAliases().length > 0) {
                 help.append(" (");
                 for (int i = 0; i < command.getAliases().length; i++) {
                     help.append(command.getAliases()[i]);
                     if (i + 1 < command.getAliases().length) {
                         help.append(", ");
                     }
                 }
                 help.append(")");
             }
         }
 
         help.append("\n\n");
         help.append(footer);
         return help.toString();
     }
  
     private static class InputReader implements Runnable {
         private BufferedReader myReader;
         private String myReadInput;
         private SVNErrorMessage myError;
         volatile boolean myIsFinished;
         
         public InputReader(InputStream is) {
             myReader = new BufferedReader(new InputStreamReader(is));
         }
         
         public void run() {
             myIsFinished = false;
             try {
                 myReadInput = myReader.readLine();
             } catch (IOException e) {
                 myError = SVNErrorMessage.create(SVNErrorCode.IO_ERROR, "Can''t read stdin: {0}", 
                         e.getLocalizedMessage());
             }
             myIsFinished = true;
         }
         
         public String getReadInput() {
             return myReadInput;
         }
         
         public SVNErrorMessage getError() {
             return myError;
         }
     }
 }
