 /*==========================================================================*\
  |  $Id$
  |*-------------------------------------------------------------------------*|
  |  Copyright (C) 2006-2009 Virginia Tech
  |
  |  This file is part of Web-CAT.
  |
  |  Web-CAT is free software; you can redistribute it and/or modify
  |  it under the terms of the GNU Affero General Public License as published
  |  by the Free Software Foundation; either version 3 of the License, or
  |  (at your option) any later version.
  |
  |  Web-CAT is distributed in the hope that it will be useful,
  |  but WITHOUT ANY WARRANTY; without even the implied warranty of
  |  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  |  GNU General Public License for more details.
  |
  |  You should have received a copy of the GNU Affero General Public License
  |  along with Web-CAT; if not, see <http://www.gnu.org/licenses/>.
 \*==========================================================================*/
 
 package net.sf.webcat.plugintester;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Date;
 import java.util.Properties;
 import net.sf.webcat.plugintester.util.PluginConfiguration;
 import net.sf.webcat.plugintester.util.WebCATConfiguration;
 
 //-------------------------------------------------------------------------
 /**
  * Performs the task of running the plugins based on the settings specified by
  * the user.
  * 
  * @author Tony Allevato
  * @version $Id$
  */
 public class PluginRunner
 {
     //~ Constructors ..........................................................
 
     // ----------------------------------------------------------
     /**
      * Initializes a new PluginRunner with the specified application
      * configuration and current settings.
      * 
      * @param wcConfig the Web-CAT application configuration
      * @param settings the current settings specified by the user
      */
     public PluginRunner(WebCATConfiguration wcConfig, Properties settings)
     {
         webcatConfig = wcConfig;
         currentSettings = settings;
     }
     
 
     //~ Methods ...............................................................
 
     // ----------------------------------------------------------
     /**
      * Prepares the result area and runs the plugins.
      */
     public void run()
     {
         createResultsDirectory();
 
         String[] envp = webcatConfig.envp();
         System.out.println("Running plug-ins with the following environment:");
         for (String env : envp)
         {
             System.out.println("  " + env);
         }
 
         String pluginsString = currentSettings.getProperty(
                 AppConstants.PROP_LAST_PLUGIN_PATHS);
         String[] plugins = pluginsString.split(File.pathSeparator);
 
         int index = 1;
         for (String plugin : plugins)
         {
             // Write properties that are plugin-specific before each one runs.
             
             File gradingPropsFile = new File(resultsDir, "grading.properties");
             Properties gradingProps = loadProperties(gradingPropsFile);
             gradingProps.setProperty("scriptHome", plugin);
             gradingProps.setProperty("timeout", "30");
             saveProperties(gradingProps, gradingPropsFile);
 
             // Run the plugin.
 
             runPlugin(index, plugin);
             index++;
         }
     }
 
     
     // ----------------------------------------------------------
     /**
      * Runs the plugin in the specified folder.
      * 
      * @param plugin the path to the plugin to execute
      */
     private void runPlugin(int index, String plugin)
     {
         String command = getCommandLine(index, plugin);
         String[] envp = webcatConfig.envp();
 
         String[] cmdArray = null;
         Process process = null;
 
         // Tack on the command shell prefix to the beginning, quoting the
         // whole argument sequence if necessary.
 
         String shell = cmdShell();
         if (shell != null && shell.length() > 0)
         {
             if (shell.charAt(shell.length() - 1) == '"')
             {
                 cmdArray = shell.split("\\s+");
                 cmdArray[cmdArray.length - 1] = command;
             }
             else
             {
                 command = shell + command;
             }
         }
 
         try
         {
             if (cmdArray != null)
             {
                 process = Runtime.getRuntime().exec(cmdArray, envp, resultsDir);
             }
             else
             {
                 process = Runtime.getRuntime().exec(command, envp, resultsDir);
             }
 
             process.waitFor();
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
         catch (InterruptedException e)
         {
             e.printStackTrace();
         }
     }
 
 
     // ----------------------------------------------------------
     /**
      * Gets the command shell that should be used to execute the grading
      * plugins.
      * 
      * @return the command shell
      */
     private String cmdShell()
     {
         if (cmdShellCache == null)
         {
             String os = System.getProperty("os.name");
     
             if (os != null && os.indexOf("Windows") >= 0)
             {
                 cmdShellCache = "cmd /c";
             }
             else
             {
                 cmdShellCache = "sh -c \"";
             }
     
             int len = cmdShellCache.length();
             if (len > 0
                    && cmdShellCache.charAt(len - 1) != ' '
                    && cmdShellCache.charAt(len - 1) != '"')
             {
                 cmdShellCache += " ";
             }
         }
         
         return cmdShellCache;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Constructs the command line that should be used to execute the specified
      * plugin, based on the values in the plugin's config.plist file.
      * 
      * @param pluginDir the directory that contains the plugin
      * @return the command line to use to execute the plugin
      */
     private String getCommandLine(int index, String pluginDir)
     {
         PluginConfiguration config =
             new PluginConfiguration(new File(pluginDir));
 
        String executable =
            pluginDir + "/" + config.getRootProperty("executable");
         
         String interpreterPrefix =
             config.getRootProperty("interpreter..prefix");
 
         if (interpreterPrefix != null)
         {
             executable = interpreterPrefix + " " + executable;
         }
 
         String cmdLine;
         cmdLine = substituteApplicationProperties(executable);
         cmdLine += " ";
        cmdLine += new File(resultsDir, "grading.properties").getAbsolutePath();
         
         File stdoutFile = new File(resultsDir, "" + index + "-stdout.txt");
         File stderrFile = new File(resultsDir, "" + index + "-stderr.txt");
 
        cmdLine += " 1> " + stdoutFile.getAbsolutePath();
        cmdLine += " 2> " + stderrFile.getAbsolutePath();
 
         return cmdLine;
     }
 
 
     // ----------------------------------------------------------
     /**
      * Creates the "Results" directory that will contain the plugins' output
      * (or clears it if it already exists).
      */
     private void createResultsDirectory()
     {
         String subPath = currentSettings.getProperty(
                 AppConstants.PROP_LAST_SUBMISSION_PATH);
 
         File subDir = new File(subPath);
         File parentDir = subDir.getParentFile();
         resultsDir = new File(parentDir, "Results");
 
         if (!resultsDir.exists())
         {
             resultsDir.mkdirs();
         }
         else
         {
             deleteContents(resultsDir);
         }
 
         // Create the grading.properties for the submission by first using
         // hard-coded default values, then overriding those with values
         // specified by the user's test.properties (located in the parent
         // directory of the submission files), and then finally by those values
         // entered directly into the user interface.
 
         Properties gradingProps = new Properties();
         
         initializeDefaultGradingProperties(gradingProps);
         
         Properties testProps = loadProperties(
                 new File(parentDir, "test.properties"));
         
         gradingProps.putAll(testProps);
 
         Properties userProps = getPropertiesFromString(
                 currentSettings.getProperty(
                         AppConstants.PROP_USER_GRADING_PROPERTIES));
 
         gradingProps.putAll(userProps);
 
         saveProperties(gradingProps,
                 new File(resultsDir, "grading.properties"));
     }
 
     
     // ----------------------------------------------------------
     /**
      * Initializes the specified properties objects with hard-coded default
      * values for the grading.properties file.
      * 
      * @param props the Properties object to initialize
      */
     private void initializeDefaultGradingProperties(Properties props)
     {
         props.setProperty("numReports", "0");
         props.setProperty("max.score.correctness", "50");
         props.setProperty("max.score.tools", "50");
         props.setProperty("userName", "dummyUser");
 
         String subPath = currentSettings.getProperty(
                 AppConstants.PROP_LAST_SUBMISSION_PATH);
 
         File dataDir = new File(new File(subPath).getParentFile(),
                 "ScriptData");
 
         props.setProperty("workingDir", subPath);
         props.setProperty("resultDir", resultsDir.getAbsolutePath());
         props.setProperty("scriptData", dataDir.getAbsolutePath());
         props.setProperty("course", "DummyCourse");
         props.setProperty("CRN", "DummyCRN");
         props.setProperty("assignment", "DummyAssignment");
         
         Date now = new Date();
         props.setProperty("dueDateTimestamp",
                 Long.toString(now.getTime() + 86400000));
         props.setProperty("submissionTimestamp",
                 Long.toString(now.getTime()));
         props.setProperty("submissionNo", "1");
         
         String webcatHome = currentSettings.getProperty(
                 AppConstants.PROP_WEBCAT_HOME);
 
         props.setProperty("frameworksBaseURL", new File(webcatHome,
                 "WEB-INF/Web-CAT.woa/Contents/Library/Frameworks").
                 getAbsolutePath());
     }
 
 
     // ----------------------------------------------------------
     /**
      * Deletes the contents of the specified directory (recursively deleting
      * any child directories as well). The directory itself will <b>not</b> be
      * deleted; it will be empty after this operation.
      * 
      * @param dir the directory that will be emptied
      */
     private void deleteContents(File dir)
     {
         File[] entries = dir.listFiles();
         
         for(File entry : entries)
         {
             if (entry.isDirectory())
             {
                 deleteContents(entry);
             }
 
             entry.delete();
         }
     }
 
 
     // ----------------------------------------------------------
     /**
      * Substitutes values from the Web-CAT application configuration into
      * plugin properties that use the ${var} syntax for variable substitution.
      * 
      * @param value the value that will have values substituted into it
      * @return the new value with substitutions made
      */
     private String substituteApplicationProperties(String value)
     {
         final String REFERENCE_START = "${";
         final String REFERENCE_END = "}";
 
         // Get the index of the first constant, if any.
         
         StringBuffer buffer = new StringBuffer(value.length());
         int beginIndex = 0;
         int startName = value.indexOf(REFERENCE_START, beginIndex);
 
         while (startName >= 0)
         {
             int endName = value.indexOf(REFERENCE_END, startName);
             if (endName == -1)
             {
                 // Terminating symbol not found; Return the value as is.
                 break;
             }
 
             if (startName > beginIndex)
             {
                 buffer.append(value.substring(beginIndex, startName));
                 beginIndex = startName;
             }
 
             String constName  =
                 value.substring(startName + REFERENCE_START.length(), endName);
             String constValue =
                 webcatConfig.applicationProperties().getProperty(constName);
 
             if (constValue == null)
             {
                 // Property name not found.
                 buffer.append(value.substring(beginIndex,
                               endName + REFERENCE_END.length()));
             }
             else
             {
                 // Insert the constant value into the original property value.
                 buffer.append(constValue);
             }
 
             beginIndex = endName + REFERENCE_END.length();
             
             // Look for the next constant
             startName = value.indexOf(REFERENCE_START, beginIndex);
         }
 
         buffer.append(value.substring(beginIndex, value.length()));
         return buffer.toString();
     }
 
 
     // ----------------------------------------------------------
     /**
      * Creates a Properties object that contains the key-value pairs in the
      * specified string.
      * 
      * @param propString a String containing the textual form of an ASCII
      *     properties file
      * @return the Properties object containing the properties
      */
     private Properties getPropertiesFromString(String propString)
     {
         Properties props = new Properties();
         
         if (propString != null)
         {
             ByteArrayInputStream stream = new ByteArrayInputStream(
                     propString.getBytes());
             
             try
             {
                 props.load(stream);
             }
             catch (IOException e)
             {
                 // Do nothing.
             }
         }
 
         return props;
     }
 
 
     // ----------------------------------------------------------
     /**
      * A helper method to load a Properties object from a file, ignoring
      * exceptions.
      * 
      * @param file the file from which to load the properties
      * @return a Properties object containing the properties from the file, or
      *     empty if there was an error
      */
     private Properties loadProperties(File file)
     {
         Properties props = new Properties();
 
         FileInputStream stream = null;
 
         try
         {
             stream = new FileInputStream(file);
             props.load(stream);
         }
         catch (IOException e)
         {
             // Do nothing.
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
             catch (IOException e2)
             {
                 // Do nothing.
             }
         }
         
         return props;
     }
     
     
     // ----------------------------------------------------------
     /**
      * A helper method to save a Properties object to a file, ignoring
      * exceptions.
      * 
      * @param props the Properties object to save
      * @param file the file to which to save the properties
      */
     private void saveProperties(Properties props, File file)
     {
         FileOutputStream stream = null;
 
         try
         {
             stream = new FileOutputStream(file);
             props.store(stream, null);
         }
         catch (IOException e)
         {
             // Do nothing.
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
             catch (IOException e2)
             {
                 // Do nothing.
             }
         }
     }
 
 
     //~ Instance/static variables .............................................
 
     private WebCATConfiguration webcatConfig;
     private Properties currentSettings;
     private String cmdShellCache;
     private File resultsDir;
 }
