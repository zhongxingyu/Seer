 /*
  * Copyright luntsys (c) 2001-2004,
  * Date: 2004-12-16
  * Time: 21:16:37
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
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.Serializable;
 import java.io.StringReader;
 import java.util.Iterator;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import ognl.OgnlException;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.types.Environment;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import com.luntsys.luntbuild.ant.Commandline;
 import com.luntsys.luntbuild.db.Build;
 import com.luntsys.luntbuild.facades.lb12.BuilderFacade;
 import com.luntsys.luntbuild.utility.DisplayProperty;
 import com.luntsys.luntbuild.utility.Luntbuild;
 import com.luntsys.luntbuild.utility.LuntbuildLogger;
 import com.luntsys.luntbuild.utility.MyExecTask;
 import com.luntsys.luntbuild.utility.OgnlHelper;
 import com.luntsys.luntbuild.utility.ValidationException;
 
 /**
  * Base class for all builders.
  *
  * @author robin shine
  */
 public abstract class Builder implements Serializable {
     /**
      * Keep tracks of version of this class, used when do serialization-deserialization
      */
     static final long serialVersionUID = 1L;
 
     /** Name of the directory to store build artifacts in. */
     public static final String ARTIFACTS_DIR = "artifacts";
 
     /**
      * Name of the builder
      */
     private String name;
 
     /**
      * Currently running build
      */
     private transient Build build;
 
     /**
      * Result of builder execution
      */
     private transient int result;
 
     /**
      * Build log
      */
     private transient Node build_log;
 
     private String buildSuccessCondition;
     private String environments;
 
     /**
      * Gets the display name for this builder.
      *
      * @return the display name for this builder
      */
     public abstract String getDisplayName();
 
     /**
      * Gets the the name of the icon for this builder.
      * 
      * @return the name of the icon for this builder. Icon should be put into
      *         the images directory of the web application.
      */
     public abstract String getIconName();
 
     /**
      * Gets the common builder properties. These properites will be shown to user and expect
      * input from user.
      *
      * @return list of properties can be configured by user
      */
     public List getProperties() {
         List properties = getBuilderSpecificProperties();
         properties.add(new DisplayProperty() {
             public String getDisplayName() {
                 return "Environment variables";
             }
 
             public String getDescription() {
                 return "Environment variables to set before running this builder. For example:\n" +
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
                 return getEnvironments();
             }
 
             public void setValue(String value) {
                 setEnvironments(value);
             }
         });
         properties.add(new DisplayProperty() {
             public String getDisplayName() {
                 return "Build success condition";
             }
 
             public String getDescription() {
                 return "The build success condition is an OGNL expression used to determine if the build of the current project was successful. " +
                         "If left empty, the \"result==0\" value is assumed. Refer to the User's Guide for details.";
             }
 
             public boolean isRequired() {
                 return false;
             }
 
             public String getValue() {
                 return getBuildSuccessCondition();
             }
 
             public void setValue(String value) {
                 setBuildSuccessCondition(value);
             }
         });
         return properties;
     }
 
     /**
      * Gets the properties of this implementation of <code>Builder</code>. These properites will be shown to user and expect
      * input from user.
      *
      * @return list of properties can be configured by user
      */
     public abstract List getBuilderSpecificProperties();
 
     /**
      * Validates properties of this builder.
      *
      * @throws ValidationException if a property has an invalid value
      */
     public void validate() {
         Iterator it = getProperties().iterator();
         if (Luntbuild.isEmpty(getName())) {
             throw new ValidationException("Builder name should not be empty!");
         }
         setName(getName().trim());
         while (it.hasNext()) {
             DisplayProperty property = (DisplayProperty) it.next();
             if (property.isRequired() && (Luntbuild.isEmpty(property.getValue())))
                 throw new ValidationException("Property \"" + property.getDisplayName() + "\" can not be empty!");
             if (!property.isMultiLine() && !property.isSecret() && property.getValue() != null)
                 property.setValue(property.getValue().trim());
         }
         if (!Luntbuild.isEmpty(getEnvironments())) {
             BufferedReader reader = new BufferedReader(new StringReader(getEnvironments()));
             try {
                 String line;
                 while ((line = reader.readLine()) != null) {
                     if (line.trim().equals(""))
                         continue;
                     String name = Luntbuild.getAssignmentName(line);
                     String value = Luntbuild.getAssignmentValue(line);
                     if (Luntbuild.isEmpty(name) || Luntbuild.isEmpty(value))
                         throw new ValidationException("Invalid environment variable definition: " + line);
                 }
             } catch (IOException e) {
                 // ignores
             } finally {
             	if (reader != null) try{reader.close();} catch (Exception e) {}
             }
         }
         if (!Luntbuild.isEmpty(getBuildSuccessCondition())) {
             try {
             	Luntbuild.validateExpression(getBuildSuccessCondition());
             } catch (ValidationException e) {
                 throw new ValidationException("Invalid build success condition: " + getBuildSuccessCondition() +
                         ", reason: " + e.getMessage());
             }
         }
     }
 
     /**
      * Gets the facade object of this builder.
      *
      * @return facade object of this builder
      */
     public BuilderFacade getFacade() {
         BuilderFacade facade = constructFacade();
         facade.setName(getName());
         facade.setEnvironments(getEnvironments());
         facade.setBuildSuccessCondition(getBuildSuccessCondition());
         saveToFacade(facade);
         return facade;
     }
 
     /**
      * Constructs a blank builder facade object.
      *
      * @return the builder facade object
      */
     public abstract BuilderFacade constructFacade();
 
     /**
      * Loads this builder from a builder facade.
      *
      * @param facade the builder facade
      */
     public abstract void loadFromFacade(BuilderFacade facade);
 
     /**
      * Saves this builder to a builder facade.
      *
      * @param facade the builder facade
      */
     public abstract void saveToFacade(BuilderFacade facade);
 
     /**
      * Sets the facade object of this builder.
      *
      * @param facade the builder facade
      */
     public void setFacade(BuilderFacade facade) {
         setName(facade.getName());
         setEnvironments(facade.getEnvironments());
         setBuildSuccessCondition(facade.getBuildSuccessCondition());
         loadFromFacade(facade);
     }
 
 	/**
 	 * Creates and returns a copy of this object.
 	 * 
 	 * @return a clone of this instance
 	 * @throws CloneNotSupportedException if cloning is not supported
 	 * @throws RuntimeException if a clone can not be done
 	 */
     public Object clone() throws CloneNotSupportedException {
         try {
             Builder copy = (Builder) getClass().newInstance();
             copy.setName(getName());
             for (int i = 0; i < getProperties().size(); i++) {
                 DisplayProperty property = (DisplayProperty) getProperties().get(i);
                 DisplayProperty propertyCopy = (DisplayProperty) copy.getProperties().get(i);
                 propertyCopy.setValue(property.getValue());
             }
             return copy;
         } catch (InstantiationException e) {
             throw new RuntimeException(e);
         } catch (IllegalAccessException e) {
             throw new RuntimeException(e);
         }
     }
 
 	/**
 	 * Resolves OGNL variables in builder properties.
 	 * 
 	 * @param build the build
 	 * @param antProject the ant project
 	 * @throws Throwable from {@link Luntbuild#evaluateExpression(Object, String)}
 	 */
     public void resolveEmbeddedOgnlVariables(Build build, Project antProject) throws Throwable {
         this.build = build;
         OgnlHelper.setAntProject(antProject);
         OgnlHelper.setTestMode(false);
         for (int i = 0; i < getProperties().size(); i++) {
             DisplayProperty property = (DisplayProperty) getProperties().get(i);
             if (property.getValue() != null)
                 property.setValue(Luntbuild.evaluateExpression(this, property.getValue()));
             else
                 property.setValue("");
         }
     }
 
     /**
      * Performs the build for specified build object.
 	 * 
 	 * @param build the build
 	 * @param buildLogger the logger that will capture log messages
 	 * @throws BuildException if this builder fails
      * @throws Throwable if an exception occurs
      */
     public void build(Build build, LuntbuildLogger buildLogger) throws Throwable {
         this.build = build;
         this.build_log = buildLogger.getLog();
 
         // create a ant project to receive log
         Project antProject = Luntbuild.createAntProject();
         // log will be written without any filter or decoration
         buildLogger.setDirectMode(true);
         antProject.addBuildListener(buildLogger);
 
         String buildCmd = constructBuildCmd(build);
 
         OgnlHelper.setAntProject(antProject);
         OgnlHelper.setTestMode(false);
         Commandline cmdLine = Luntbuild.parseCmdLine(buildCmd);
 
         Environment env = new Environment();
         if (!Luntbuild.isEmpty(getEnvironments())) {
             BufferedReader reader = new BufferedReader(new StringReader(getEnvironments()));
             try {
                 String line;
                 while ((line = reader.readLine()) != null) {
                     if (line.trim().equals(""))
                         continue;
                     String assname = Luntbuild.getAssignmentName(line);
                     String value = Luntbuild.getAssignmentValue(line);
                     if (!Luntbuild.isEmpty(assname) && !Luntbuild.isEmpty(value)) {
                         Environment.Variable var = new Environment.Variable();
                         var.setKey(assname);
                         var.setValue(value);
                         env.addVariable(var);
                     }
                 }
             } catch (IOException e) {
                 // ignores
             } finally {
             	if (reader != null) try{reader.close();} catch (Exception e) {}
             }
         }
 
         MyExecTask exec =
             new MyExecTask(getDisplayName(), antProject, constructBuildCmdDir(build), cmdLine, env,
                 null, Project.MSG_INFO);
 
         boolean waitForFinish = !(this instanceof CommandBuilder && ((CommandBuilder)this).getWaitForFinish() != null && ((CommandBuilder)this).getWaitForFinish().equals("No"));
         result = exec.executeAndGetResult(waitForFinish);
 
         buildLogger.setDirectMode(false);
         if (!isBuildSuccess())
             throw new BuildException(getDisplayName() + " failed: build success condition not met!");
     }
 
     /**
      * Constructs the command to run this build.
      * 
      * @param build the build
      * @return the command to run this build
      */
     public abstract String constructBuildCmd(Build build);
 
     /**
      * Constructs the directory to run build command in.
      * 
      * @param build the build
      * @return the directory to run build command in, or <code>null</code> for the default directory
      */
     public abstract String constructBuildCmdDir(Build build);
 
     /**
      * Gets the build success condition for this builder.
      *
      * @return the build success condition for this builder
      */
     public String getBuildSuccessCondition() {
         return buildSuccessCondition;
     }
 
     /**
      * Sets the build success condition for this builder.
      *
      * @param buildSuccessCondition the build success condition for this builder
      */
     public void setBuildSuccessCondition(String buildSuccessCondition) {
         this.buildSuccessCondition = buildSuccessCondition;
     }
 
     /**
      * Gets the environment settings for this builder.
      * 
      * @return the environment settings for this builder
      */
     public String getEnvironments() {
         return environments;
     }
 
     /**
      * Sets the environment settings for this builder.
      * 
      * @param environments the environment settings for this builder
      */
     public void setEnvironments(String environments) {
         this.environments = environments;
     }
 
 	/**
 	 * Checks if this builder was successful according to <code>buildSuccessCondition</code>.
 	 * 
 	 * @return <code>true</code> if this builder was successful
 	 */
     private boolean isBuildSuccess() {
         try {
             Boolean buildSuccessValue;
             if (!Luntbuild.isEmpty(buildSuccessCondition))
             	buildSuccessValue = (Boolean)Luntbuild.evaluateExpression(this, buildSuccessCondition, Boolean.class);
             else
                	buildSuccessValue = (Boolean)Luntbuild.evaluateExpression(this, "result==0", Boolean.class);
             if (buildSuccessValue == null)
                 return false;
             else
                 return buildSuccessValue.booleanValue();
         } catch (ClassCastException e) {
             throw new RuntimeException(e);
         } catch (OgnlException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * Gets the return code of execution of this builder.
      * 
      * @return the return code of execution of this builder
      */
     public int getResult() {
         return result;
     }
 
     /**
      * Utility method to read and XML attribute as a string
      * 
      * @param element the XML element to read the attribute from
      * @param attrName the name of the attribute to read
      * @return the value of the attribute or null if the attribute does not
      *         exist
      */
     private String getAttribute(Node element, String attrName) {
         Node attrNode = element.getAttributes().getNamedItem(attrName);
         if (attrNode == null) {
             return null;
         } else {
             return attrNode.getNodeValue();
         }
     }
 
     /**
      * Checks if the builder log contains specified line pattern.
      * 
      * @param linePattern the line pattern to look for
      * @return <code>true</code> if the builder log contains specified line pattern
      * @throws RuntimeException if an error occurs while reading the log file
      */
     public boolean logContainsLine(String linePattern) {
     	try {
             Pattern regexp = Pattern.compile(linePattern);
         	NodeList messages = build_log.getChildNodes();
         	for (int i = 0; i < messages.getLength(); i++) {
         		Node message = messages.item(i);
                 if (regexp.matcher(Luntbuild.getTextContent(message)).matches()) {
 					return true;
                 }
         	}
             return false;
     	} catch (Exception e) {
             throw new RuntimeException(e);
     	}
     }
 
     /**
      * Checks if the builder log contains specified line pattern with the specified log level.
      * 
      * @param level the log level
      * @param linePattern the line pattern to look for
      * @return <code>true</code> if the builder log contains specified line pattern
      * @throws RuntimeException if an error occurs while reading the log file
      */
     public boolean logContainsLine(String level, String linePattern) {
         try {
             Pattern regexp = Pattern.compile(linePattern);
             NodeList messages = build_log.getChildNodes();
             for (int i = 0; i < messages.getLength(); i++) {
                 Node message = messages.item(i);
                 String priority = getAttribute(message, "priority");
                 if (level.equalsIgnoreCase(priority)) {
                     if (regexp.matcher(Luntbuild.getTextContent(message)).matches()) {
                         return true;
                     }
                 }
             }
             return false;
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * Checks if the builder log contains specified line pattern from this builder.
      * 
      * @param linePattern the line pattern to look for
      * @return <code>true</code> if the builder log contains specified line pattern from this builder
      * @throws RuntimeException if an error occurs while reading the log file
      */
     public boolean builderLogContainsLine(String linePattern) {
     	try {
             Pattern regexp = Pattern.compile(linePattern);
         	NodeList messages = build_log.getChildNodes();
         	for (int i = 0; i < messages.getLength(); i++) {
                 Node message = messages.item(i);
                 String builder = getAttribute(message, "builder");
                 if (getName().equals(builder)) {
                     if (regexp.matcher(Luntbuild.getTextContent(message)).matches()) {
                         return true;
                     }
                 }
         	}
             return false;
     	} catch (Exception e) {
             throw new RuntimeException(e);
     	}
     }
 
     /**
      * Checks if the builder log contains specified line pattern from this builder.
      * 
      * @param level the log level
      * @param linePattern the line pattern to look for
      * @return <code>true</code> if the builder log contains specified line pattern from this builder
      * @throws RuntimeException if an error occurs while reading the log file
      */
     public boolean builderLogContainsLine(String level, String linePattern) {
         try {
             Pattern regexp = Pattern.compile(linePattern);
             NodeList messages = build_log.getChildNodes();
             for (int i = 0; i < messages.getLength(); i++) {
                 Node message = messages.item(i);
                 String priority = getAttribute(message, "priority");
                 String builder = getAttribute(message, "builder");
                 if (getName().equals(builder) && level.equalsIgnoreCase(priority)) {
                     if (regexp.matcher(Luntbuild.getTextContent(message)).matches()) {
                             return true;
                     }
                 }
             }
             return false;
         } catch (Exception e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * Gets the build object using this builder.
      * 
      * @return the build object using this builder
      */
     public Build getBuild() {
         return build;
     }
 
     /**
      * Gets the system object, this is mainly used for OGNL expression evaluation.
      * 
      * @return the system object
      */
     public OgnlHelper getSystem() {
         return new OgnlHelper();
     }
 
     /**
      * Gets the name of this builder.
      * 
      * @return the name of this builder
      */
     public String getName() {
         return name;
     }
 
     /**
      * Sets the name of this builder.
      * 
      * @param name the name of this builder
      */
     public void setName(String name) {
         this.name = name;
     }
 
 	/**
 	 * Converts this builder to a string.
 	 * 
 	 * @return the string representation of this builder
 	 */
     public String toString() {
         String summary = "Builder name: " + getName() + "\n";
         summary += "Builder type: " + getDisplayName() + "\n";
         Iterator it = getProperties().iterator();
         while (it.hasNext()) {
             DisplayProperty property = (DisplayProperty) it.next();
             if (!property.isSecret())
                 summary += "    " + property.getDisplayName() + ": " + property.getValue() + "\n";
             else
                 summary += "    " + property.getDisplayName() + ":*****\n";
         }
         return summary;
     }
 }
