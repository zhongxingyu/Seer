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
 
 import com.luntsys.luntbuild.ant.Commandline;
 import com.luntsys.luntbuild.db.Build;
 import com.luntsys.luntbuild.facades.lb12.BuilderFacade;
 import com.luntsys.luntbuild.utility.*;
 
 import ognl.Ognl;
 import ognl.OgnlException;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.types.Environment;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import java.io.*;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * The base class for all builders.
  *
  * @author robin shine
  */
 public abstract class Builder implements Serializable {
     /**
      * Keep tracks of version of this class, used when do serialization-deserialization
      */
     static final long serialVersionUID = 1L;
 
     public static final String ARTIFACTS_DIR = "artifacts";
 
     public static final String JUNIT_HTML_REPORT_DIR = "junit_html_report";
 
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
      * Get display name for current builders
      *
      * @return display name for current builders
      */
     public abstract String getDisplayName();
 
     /**
      * @return name of the icon for this version control system. Icon should be put into
      *         the images directory of the web application.
      */
     public abstract String getIconName();
 
     /**
      * Get properties of this builders. These properites will be shown to user and expect
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
 
     public abstract List getBuilderSpecificProperties();
 
     /**
      * Validates properties of this builders
      *
      * @throws com.luntsys.luntbuild.utility.ValidationException
      *
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
             }
         }
         if (!Luntbuild.isEmpty(getBuildSuccessCondition())) {
             try {
                 Ognl.parseExpression(getBuildSuccessCondition());
             } catch (OgnlException e) {
                 throw new ValidationException("Invalid build success condition: " + getBuildSuccessCondition() +
                         ", reason: " + e.getMessage());
             }
         }
     }
 
     /**
      * Get facade object of this builders
      *
      * @return facade object of this builders
      */
     public com.luntsys.luntbuild.facades.lb12.BuilderFacade getFacade() {
         com.luntsys.luntbuild.facades.lb12.BuilderFacade facade = constructFacade();
         facade.setName(getName());
         facade.setEnvironments(getEnvironments());
         facade.setBuildSuccessCondition(getBuildSuccessCondition());
         saveToFacade(facade);
         return facade;
     }
 
     /**
      * Construct builders facade object
      *
      * @return builders facade object
      */
     public abstract com.luntsys.luntbuild.facades.lb12.BuilderFacade constructFacade();
 
     /**
      * Load value from builders facade
      *
      * @param facade
      */
     public abstract void loadFromFacade(BuilderFacade facade);
 
     /**
      * Save value to builders facade
      *
      * @param facade
      */
     public abstract void saveToFacade(com.luntsys.luntbuild.facades.lb12.BuilderFacade facade);
 
     /**
      * Set facade object of this builders
      *
      * @param facade
      */
     public void setFacade(com.luntsys.luntbuild.facades.lb12.BuilderFacade facade) {
         setName(facade.getName());
         setEnvironments(facade.getEnvironments());
         setBuildSuccessCondition(facade.getBuildSuccessCondition());
         loadFromFacade(facade);
     }
 
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
      * Perform build for specified build object
      *
      * @throws Throwable
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
      * Constructs the command to run build
      *
      * @return the command to run build, should not be null
      */
     public abstract String constructBuildCmd(Build build) throws IOException;
 
     /**
      * Constructs the directory to run build command in
      *
      * @return the directory to run build command in. Null if do not care where to run build command
      */
     public abstract String constructBuildCmdDir(Build build);
 
     /**
      * Get build success condition for this builders
      *
      * @return build success condition for this builders, Null if not exist
      */
     public String getBuildSuccessCondition() {
         return buildSuccessCondition;
     }
 
     /**
      * Set build success condition for this builders
      *
      * @param buildSuccessCondition
      */
     public void setBuildSuccessCondition(String buildSuccessCondition) {
         this.buildSuccessCondition = buildSuccessCondition;
     }
 
     /**
      * Get environment settings for this builder
      * @return environment settings for this builder
      */
     public String getEnvironments() {
         return environments;
     }
 
     /**
      * Set environment settings for this builder
      * @param environments
      */
     public void setEnvironments(String environments) {
         this.environments = environments;
     }
 
     private boolean isBuildSuccess() {
         try {
             Boolean buildSuccessValue;
             if (!Luntbuild.isEmpty(buildSuccessCondition))
                 buildSuccessValue = (Boolean) Ognl.getValue(Ognl.parseExpression(buildSuccessCondition),
                     Ognl.createDefaultContext(this), this, Boolean.class);
             else
                 buildSuccessValue = (Boolean) Ognl.getValue(Ognl.parseExpression("result==0"),
                     Ognl.createDefaultContext(this), this, Boolean.class);
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
      * Get return code of execution of this builder
      * @return return code of execution of this builder
      */
     public int getResult() {
         return result;
     }
 
     /**
      * Checks if the builder log contains specified line pattern.
      * 
      * @param linePattern the linePattern to look for
      * @return <code>true</code> if the builder log contains specified line pattern
      * @throws RuntimeException if an error occurs while reading the log file
      */
     public boolean logContainsLine(String linePattern) {
     	try {
         	NodeList messages = build_log.getChildNodes();
         	for (int i = 0; i < messages.getLength(); i++) {
         		Node message = messages.item(i);
				if (message.getTextContent().matches(linePattern))
 					return true;
         	}
             return false;
     	} catch (Exception e) {
             throw new RuntimeException(e);
     	}
     }
 
     /**
      * Checks if the builder log contains specified line pattern from this builder.
      * 
      * @param linePattern the linePattern to look for
      * @return <code>true</code> if the builder log contains specified line pattern from this builder
      * @throws RuntimeException if an error occurs while reading the log file
      */
     public boolean builderLogContainsLine(String linePattern) {
     	try {
         	NodeList messages = build_log.getChildNodes();
         	for (int i = 0; i < messages.getLength(); i++) {
         		Node message = messages.item(i);
         		Node builder = message.getAttributes().getNamedItem("builder");
         		if (builder != null) {
        			if (builder.getTextContent().equals(getName())) {
        				if (message.getTextContent().matches(linePattern))
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
      * Get build object using this builder
      * @return build object using this builder
      */
     public Build getBuild() {
         return build;
     }
 
     /**
      * Get system object, this is mainly used for Ognl expression evaluation
      * @return system object
      */
     public OgnlHelper getSystem() {
         return new OgnlHelper();
     }
 
     /**
      * Get name of this builder
      * @return name of this builder
      */
     public String getName() {
         return name;
     }
 
     /**
      * Set name of this builder
      * @param name
      */
     public void setName(String name) {
         this.name = name;
     }
 
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
