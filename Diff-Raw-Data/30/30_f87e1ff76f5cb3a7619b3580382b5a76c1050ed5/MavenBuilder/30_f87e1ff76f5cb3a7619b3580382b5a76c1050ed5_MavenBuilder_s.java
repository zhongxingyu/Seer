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
 import com.luntsys.luntbuild.facades.lb12.BuilderFacade;
 import com.luntsys.luntbuild.facades.lb12.MavenBuilderFacade;
 import com.luntsys.luntbuild.utility.DisplayProperty;
 import com.luntsys.luntbuild.utility.Luntbuild;
 import com.luntsys.luntbuild.utility.ValidationException;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Maven builder implementation.
  * 
  * @author robin shine
  */
 public class MavenBuilder extends Builder {
     /**
      * Keep tracks of version of this class, used when do serialization-deserialization
      */
     static final long serialVersionUID = 1L;
 
     /**
      * The command to run maven
      */
     private String command;
 
     /**
      * Directory to run maven in
      */
     private String dirToRunMaven;
 
     /**
      * Goals to build
      */
     private String goals;
 
     /**
      * Extra properties transfered into the build script
      */
     private String buildProperties =
             "buildVersion=\"${build.version}\"\n" +
             "artifactsDir=\"${build.artifactsDir}\"\n" +
             "buildDate=\"${build.startDate}\"\n" +
            "junitHtmlReportDir=\"${build.getReportUrl(\"JUnit\")}\"";
 
 	/**
 	 * Constructor, creates a new maven builder with default settings.
 	 */
     public MavenBuilder() {
         if (System.getProperty("os.name").startsWith("Windows")) {
             this.command = "\"C:\\Program Files\\Apache Software Foundation\\Maven 1.0.2\\bin\\maven.bat\"";
         } else {
             this.command = "/usr/local/bin/maven";
         }
         setBuildSuccessCondition("result==0 and builderLogContainsLine(\"BUILD SUCCESSFUL\")");
     }
 
 	/**
 	 * Gets the command to run maven.
 	 * 
 	 * @return the command to run maven
 	 */
     public String getCommand() {
         return command;
     }
 
 	/**
 	 * Sets the command to run maven.
 	 * 
 	 * @param command the command to run maven
 	 */
     public void setCommand(String command) {
         this.command = command;
     }
 
 	/**
 	 * Gets the directory to run maven in.
 	 * 
 	 * @return the directory to run maven in
 	 */
     public String getDirToRunMaven() {
         return dirToRunMaven;
     }
 
 	/**
 	 * Sets the directory to run maven in.
 	 * 
 	 * @param dirToRunMaven the directory to run maven in
 	 */
     public void setDirToRunMaven(String dirToRunMaven) {
         this.dirToRunMaven = dirToRunMaven;
     }
 
 	/**
 	 * Gets the goals.
 	 * 
 	 * @return the goals
 	 */
     public String getGoals() {
         return goals;
     }
 
 	/**
 	 * Sets the goals.
 	 * 
 	 * @param goals the goals
 	 */
     public void setGoals(String goals) {
         this.goals = goals;
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
         return "Maven builder";
     }
 
     /**
      * @inheritDoc
      */
     public String getIconName() {
         return "maven.png";
     }
 
     /**
      * @inheritDoc
      */
     public List getBuilderSpecificProperties() {
         List properties = new ArrayList();
         properties.add(new DisplayProperty() {
             public String getDisplayName() {
                 return "Command to run Maven";
             }
 
             public String getDescription() {
                 return "Specify command to run Maven (usually path to maven.bat or Maven shell script). For example: " +
                         "/usr/local/bin/maven. String enclosed by ${...} will be interpreted as OGNL expression, and it will be " +
                         "evaluated before execution. Refer to User's Guide for valid OGNL expressions in this context, as well as how to " +
                         "instruct Maven to use Luntbuild provided version number. NOTE. Single argument containing spaces should be " +
                         "quoted in order not be interpreted as multiple arguments.";
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
                 return "Directory to run Maven in";
             }
 
             public String getDescription() {
                 return "Specify the directory to run Maven in. If this path is not an absolute path, " +
                         "it is assumed to be relative " +
                         "to the schedule work directory. Defaults to schedule work " +
                         "directory if this property is left empty.";
             }
 
             public boolean isRequired() {
                 return false;
             }
 
             public String getValue() {
                 return getDirToRunMaven();
             }
 
             public void setValue(String value) {
                 setDirToRunMaven(value);
             }
         });
         properties.add(new DisplayProperty() {
             public String getDisplayName() {
                 return "Goals to build";
             }
 
             public String getDescription() {
                 return "Specify the goals to build. Use space to separate different goals (goal name " +
                         "containing spaces should be quoted in order not to be interpreted as multiple goals). " +
                         "You can also use ${...} to pass OGNL variables as the goal name. For example you " +
                         "can use ${build.schedule.name} to achieve different goals for different schedules. " +
                         "For valid OGNL expressions in this context, please refer to User's Guide.";
             }
 
             public boolean isRequired() {
                 return false;
             }
 
             public String getValue() {
                 return getGoals();
             }
 
             public void setValue(String value) {
                 setGoals(value);
             }
         });
         properties.add(new DisplayProperty() {
             public String getDisplayName() {
                 return "Build properties";
             }
 
             public String getDescription() {
                 return "Define build properties here to pass into the ant build script. For example:\n" +
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
             throw new ValidationException("Invalid command to run Maven: " + e.getMessage());
         }
         if (!Luntbuild.isEmpty(getGoals())) {
             try {
                 Luntbuild.validateExpression(getGoals());
             } catch (ValidationException e) {
                 throw new ValidationException("Invalid goals: " + e.getMessage());
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
         String mavenCmd = getCommand();
         mavenCmd = mavenCmd.replace('\n', ' ');
         mavenCmd = mavenCmd.replace('\r', ' ');
 
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
                         mavenCmd += " -D" + name + "=" + value;
                     }
                 }
             } catch (IOException e) {
                 // ignore
             } finally {
             	if (reader != null) try{reader.close();} catch (Exception e) {}
             }
         }
 
         // set maven log level based on project's log level if log level does not been explicitely specified in maven command
         if (!mavenCmd.matches(".*\\s(-X|--debug)($|\\s.*)") && !mavenCmd.matches(".*\\s(-q|--quiet)($|\\s.*)")) {
             if (build.getSchedule().getProject().getLogLevel() == com.luntsys.luntbuild.facades.Constants.LOG_LEVEL_BRIEF)
                 mavenCmd += " -q";
             else if (build.getSchedule().getProject().getLogLevel() == com.luntsys.luntbuild.facades.Constants.LOG_LEVEL_VERBOSE)
                 mavenCmd += " -X";
         }
 
         mavenCmd += " -d \"" + build.getSchedule().resolveAbsolutePath(getDirToRunMaven()) + "\"";
         if (!Luntbuild.isEmpty(getGoals()))
             mavenCmd += " " + getGoals();
 
         return mavenCmd;
     }
 
 	/**
      * @inheritDoc
 	 */
     public String constructBuildCmdDir(Build build) {
         return build.getSchedule().resolveAbsolutePath(getDirToRunMaven());
     }
 
     /**
      * @inheritDoc
      * @see MavenBuilderFacade
      */
     public BuilderFacade constructFacade() {
         return new MavenBuilderFacade();
     }
 
     /**
      * @inheritDoc
      * @throws RuntimeException if the facade is not an <code>MavenBuilderFacade</code>
      * @see MavenBuilderFacade
      */
     public void loadFromFacade(BuilderFacade facade) {
         if (!(facade instanceof MavenBuilderFacade))
             throw new RuntimeException("Invalid facade class: " + facade.getClass().getName());
         MavenBuilderFacade mavenBuilderFacade = (MavenBuilderFacade) facade;
         setCommand(mavenBuilderFacade.getCommand());
         setDirToRunMaven(mavenBuilderFacade.getDirToRunMaven());
         setGoals(mavenBuilderFacade.getGoals());
         setBuildProperties(mavenBuilderFacade.getBuildProperties());
     }
 
     /**
      * @inheritDoc
      * @throws RuntimeException if the facade is not an <code>MavenBuilderFacade</code>
      * @see MavenBuilderFacade
      */
     public void saveToFacade(BuilderFacade facade) {
         if (!(facade instanceof MavenBuilderFacade))
             throw new RuntimeException("Invalid facade class: " + facade.getClass().getName());
         MavenBuilderFacade mavenBuilderFacade = (MavenBuilderFacade) facade;
         mavenBuilderFacade.setCommand(getCommand());
         mavenBuilderFacade.setDirToRunMaven(getDirToRunMaven());
         mavenBuilderFacade.setGoals(getGoals());
         mavenBuilderFacade.setBuildProperties(getBuildProperties());
     }
 }
