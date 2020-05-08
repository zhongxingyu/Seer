 /*
  * Copyright luntsys (c) 2001-2004,
  * Date: 2004-12-16
  * Time: 21:44:26
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
 package com.luntsys.luntbuild.builders;
 
 import com.luntsys.luntbuild.db.Build;
 import com.luntsys.luntbuild.facades.Constants;
 import com.luntsys.luntbuild.facades.lb12.BuilderFacade;
 import com.luntsys.luntbuild.facades.lb12.RakeBuilderFacade;
 import com.luntsys.luntbuild.utility.DisplayProperty;
 import com.luntsys.luntbuild.utility.Luntbuild;
 import com.luntsys.luntbuild.utility.ValidationException;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Rake builder implementation.
  * 
  * @author lubosp
  */
 public class RakeBuilder extends Builder {
     /**
      * Keep tracks of version of this class, used when do serialization-deserialization.
      */
     static final long serialVersionUID = 1L;
 
     /**
      * The command to run rake.
      */
     private String command;
 
     /**
      * Path to rake build script.
      */
     private String buildScriptPath;
 
     /**
      * Targets to build.
      */
     private String targets;
 
     /**
      * Extra properties transfered into the build script.
      */
     private String buildProperties =
             "buildVersion=\"${build.version}\"\n" +
             "artifactsDir=\"${build.artifactsDir}\"\n" +
             "buildDate=\"${build.startDate}\"\n" +
            "junitHtmlReportDir=\"${build.getReportUrl(\"JUnit\")}\"";
 
 	/**
 	 * Constructor, creates a new rake builder with default settings.
 	 */
     public RakeBuilder() {
         if (System.getProperty("os.name").startsWith("Windows")) {
             this.command = "C:\\rake\\bin\\rake.bat ";
         } else {
             this.command = "/usr/local/bin/rake ";
         }
         setBuildSuccessCondition("result==0 and !builderLogContainsLine(\"Command failed with status\")");
     }
 
 	/**
 	 * Gets the command to run rake.
 	 * 
 	 * @return the command to run rake
 	 */
     public String getCommand() {
         return command;
     }
 
 	/**
 	 * Sets the command to run rake.
 	 * 
 	 * @param command the command to run rake
 	 */
     public void setCommand(String command) {
         this.command = command;
     }
 
 	/**
 	 * Gets the path to the build script.
 	 * 
 	 * @return the path to the build script
 	 */
     public String getBuildScriptPath() {
         return buildScriptPath;
     }
 
 	/**
 	 * Sets the path to the build script.
 	 * 
 	 * @param buildScriptPath the path to the build script
 	 */
     public void setBuildScriptPath(String buildScriptPath) {
         this.buildScriptPath = buildScriptPath;
     }
 
 	/**
 	 * Gets the targets to run.
 	 * 
 	 * @return the targets to run
 	 */
     public String getTargets() {
         return targets;
     }
 
 	/**
 	 * Sets the targets to run.
 	 * 
 	 * @param targets the targets to run
 	 */
     public void setTargets(String targets) {
         this.targets = targets;
     }
 
 	/**
 	 * Gets the build properties.
 	 * 
 	 * @return the build properties
 	 */
     public String getBuildProperties() {
         return buildProperties;
     }
 
 	/**
 	 * Sets the build properties.
 	 * 
 	 * @param buildProperties the build properties
 	 */
     public void setBuildProperties(String buildProperties) {
         this.buildProperties = buildProperties;
     }
 
     /**
      * @inheritDoc
      */
     public String getDisplayName() {
         return "Rake builder";
     }
 
     /**
      * @inheritDoc
      */
     public String getIconName() {
         return "rake.gif";
     }
 
     /**
      * @inheritDoc
      */
     public List getBuilderSpecificProperties() {
         List properties = new ArrayList();
         properties.add(new DisplayProperty() {
             public String getDisplayName() {
                 return "Command to run Rake";
             }
 
             public String getDescription() {
                 return "Specify the command to run Rake (normally path to rake.bat or rake shell script) here. For example: " +
                         "/path/to/rake buildVersion=\"${build.version}\" artifactsDir=\"${build.artifactsDir}\". String enclosed by ${...} will be interpreted " +
                         "as OGNL expression, and it be evaluated before execution. For valid OGNL expressions in this context, please refer to " +
                         "the User's Guide. NOTE. A single argument that includes spaces should be quoted in order not to be interpreted as multiple " +
                         "arguments.";
             }
 
             public boolean isMultiLine() {
                 return true;
             }
 
             public String getValue() {
                 return getCommand();
             }
 
             public void setValue(String value) {
                 setCommand(value);
             }
         });
         properties.add(new DisplayProperty() {
             public String getDisplayName() {
                 return "Build script path";
             }
 
             public String getDescription() {
                 return "The path for the Rake build script. If this path is not an absolute path, " +
                         "it is assumed that it is relative to the schedule work directory. " +
                         "Refer to the User's Guide " +
                         "for details about how to write a new Rake build file or how to modify your existing Rake build script.";
             }
 
             public String getValue() {
                 return getBuildScriptPath();
             }
 
             public void setValue(String value) {
                 setBuildScriptPath(value);
             }
         });
         properties.add(new DisplayProperty() {
             public String getDisplayName() {
                 return "Build targets";
             }
 
             public String getDescription() {
                 return "Specify the targets to build. Use space to separate different targets (target name " +
                         "containing spaces should be quoted in order not to be interpreted as multiple targets). " +
                         "You can also use ${...} to pass variables to the target name. For example you " +
                         "can use ${build.schedule.name} to achieve different targets for different schedules. " +
                         "For valid OGNL expressions in this context, please refer to the User's Guide.";
             }
 
             public boolean isRequired() {
                 return false;
             }
 
             public String getValue() {
                 return getTargets();
             }
 
             public void setValue(String value) {
                 setTargets(value);
             }
         });
         properties.add(new DisplayProperty() {
             public String getDisplayName() {
                 return "Build properties";
             }
 
             public String getDescription() {
                 return "Define build properties here to pass into the Rake build script. For example:\n" +
                         "buildVersion=${build.version}\n" +
                         "scheduleName=${build.schedule.name}\n" +
                         "You should set one variable per line. OGNL expression can be inserted to form the value provided they are " +
                         "enclosed by ${...}. For valid OGNL expressions in this context, please refer to the User's Guide.";
             }
 
             public boolean isRequired() {
                 return false;
             }
 
             public boolean isMultiLine() {
                 return true;
             }
 
             public String getValue() {
                 return getBuildProperties();
             }
 
             public void setValue(String value) {
                 setBuildProperties(value);
             }
         });
         return properties;
     }
 
     /**
      * @inheritDoc
      */
     public void validate() {
         super.validate();
         try {
             Luntbuild.validateExpression(getCommand());
         } catch (ValidationException e) {
             throw new ValidationException("Invalid command to run Rake: " + e.getMessage());
         }
         if (!Luntbuild.isEmpty(getTargets())) {
             try {
                 Luntbuild.validateExpression(getTargets());
             } catch (ValidationException e) {
                 throw new ValidationException("Invalid targets: " + e.getMessage());
             }
         }
         if (!Luntbuild.isEmpty(getBuildProperties())) {
             BufferedReader reader = new BufferedReader(new StringReader(getBuildProperties()));
             try {
                 String line;
                 while ((line = reader.readLine()) != null) {
                     if (line.trim().equals(""))
                         continue;
                     String name = Luntbuild.getAssignmentName(line);
                     String value = Luntbuild.getAssignmentValue(line);
                     if (Luntbuild.isEmpty(name) || Luntbuild.isEmpty(value))
                         throw new ValidationException("Invalid build property definition: " + line);
                 }
             } catch (IOException e) {
                 // ignores
             } finally {
             	if (reader != null) try{reader.close();} catch (Exception e) {}
             }
         }
     }
 
     /**
      * @inheritDoc
      */
     public String constructBuildCmd(Build build) {
         String rakeCmd = getCommand();
         rakeCmd = rakeCmd.replace('\n', ' ');
         rakeCmd = rakeCmd.replace('\r', ' ');
 
         if (!Luntbuild.isEmpty(getBuildProperties())) {
             BufferedReader reader = new BufferedReader(new StringReader(getBuildProperties()));
             try {
                 String line;
                 while ((line = reader.readLine()) != null) {
                     if (line.trim().equals(""))
                         continue;
                     String name = Luntbuild.getAssignmentName(line);
                     String value = Luntbuild.getAssignmentValue(line);
                     if (!Luntbuild.isEmpty(name) && !Luntbuild.isEmpty(value)) {
                         rakeCmd += " " + name + "=" + value;
                     }
                 }
             } catch (IOException e) {
                 // ignores
             } finally {
             	if (reader != null) try{reader.close();} catch (Exception e) {}
             }
         }
 
         // set rake log level based on project's log level if log level does not been explicitely
         // specified in rake command
         if (!rakeCmd.matches(".*\\s(-q|--quiet)($|\\s.*)") &&
                 !rakeCmd.matches(".*\\s(-v|-verbose)($|\\s.*)")) {
             if (build.getSchedule().getProject().getLogLevel() == Constants.LOG_LEVEL_BRIEF)
                 rakeCmd += " -q";
             else if (build.getSchedule().getProject().getLogLevel() == Constants.LOG_LEVEL_VERBOSE)
                 rakeCmd += " -v";
         }
 
         if (getBuildScriptPath() != null && getBuildScriptPath().length() > 0) {
             String buildScriptAbsolutePath = build.getSchedule().resolveAbsolutePath(getBuildScriptPath());
             rakeCmd += "  --rakefile \"" + buildScriptAbsolutePath + "\"";
         }
         if (!Luntbuild.isEmpty(getTargets()))
             rakeCmd += " " + getTargets();
 
         return rakeCmd;
     }
 
 	/**
      * @inheritDoc
 	 */
     public String constructBuildCmdDir(Build build) {
         String buildScriptAbsolutePath = build.getSchedule().resolveAbsolutePath(getBuildScriptPath());
         return new File(buildScriptAbsolutePath).getParent();
     }
 
     /**
      * @inheritDoc
      * @see RakeBuilderFacade
      */
     public BuilderFacade constructFacade() {
         return new RakeBuilderFacade();
     }
 
     /**
      * @inheritDoc
      * @throws RuntimeException if the facade is not an <code>RakeBuilderFacade</code>
      * @see RakeBuilderFacade
      */
     public void loadFromFacade(BuilderFacade facade) {
         if (!(facade instanceof RakeBuilderFacade))
             throw new RuntimeException("Invalid facade class: " + facade.getClass().getName());
         RakeBuilderFacade rakeBuilderFacade = (RakeBuilderFacade) facade;
         setCommand(rakeBuilderFacade.getCommand());
         setBuildScriptPath(rakeBuilderFacade.getBuildScriptPath());
         setTargets(rakeBuilderFacade.getBuildTargets());
         setBuildProperties(rakeBuilderFacade.getBuildProperties());
     }
 
     /**
      * @inheritDoc
      * @throws RuntimeException if the facade is not an <code>RakeBuilderFacade</code>
      * @see RakeBuilderFacade
      */
     public void saveToFacade(BuilderFacade facade) {
         if (!(facade instanceof com.luntsys.luntbuild.facades.lb12.RakeBuilderFacade))
             throw new RuntimeException("Invalid facade class: " + facade.getClass().getName());
         RakeBuilderFacade rakeBuilderFacade = (RakeBuilderFacade) facade;
         rakeBuilderFacade.setCommand(getCommand());
         rakeBuilderFacade.setBuildScriptPath(getBuildScriptPath());
         rakeBuilderFacade.setBuildTargets(getTargets());
         rakeBuilderFacade.setBuildProperties(getBuildProperties());
     }
 }
