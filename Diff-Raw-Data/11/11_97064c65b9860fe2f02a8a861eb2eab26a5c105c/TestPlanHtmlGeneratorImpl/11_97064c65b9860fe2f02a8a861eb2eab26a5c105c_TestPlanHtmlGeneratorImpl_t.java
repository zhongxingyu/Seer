 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership. The ASF licenses this file to you under the Apache License, Version
  * 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions
  * and limitations under the License.
  */
 package com.github.testplandoclet.html;
 
import java.io.FileOutputStream;
 import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
 
 import org.apache.velocity.Template;
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.Velocity;
 import org.apache.velocity.runtime.RuntimeConstants;
 import org.apache.velocity.runtime.log.Log4JLogChute;
 import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
 import org.apache.velocity.runtime.resource.loader.FileResourceLoader;
 
 import com.github.testplandoclet.Configuration;
 import com.github.testplandoclet.plan.TestPlan;
 
 /**
  * Implementation of the interface {@link TestPlanHtmlGenerator}.
  * @author Julien Giovaresco
  */
 public class TestPlanHtmlGeneratorImpl implements TestPlanHtmlGenerator
 {
    // ------------------------- private constants -------------------------
 
    /** The directory containing the templates. */
    private static final String TEMPLATES_DIR = "templates/";
 
    /** The defaut Velocity's template to user. */
    private static final String DEFAULT_TEMPLATE_FILE_NAME = "html_testplan.vm";
 
    // ------------------------- private members -------------------------
 
    /** The Velocity's template to use to generate the test plan. */
    private Template m_template;
 
    // ------------------------- constructors -------------------------
 
    /**
     * Constructor for {@link TestPlanHtmlGeneratorImpl}.
     * <p>
     * Initialize {@link Velocity} with the classpath resource loader.
     * </p>
     */
    public TestPlanHtmlGeneratorImpl()
    {
       Velocity.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, Log4JLogChute.class.getName());
       Velocity.setProperty(Log4JLogChute.RUNTIME_LOG_LOG4J_LOGGER, "velocity");
 
       Velocity.setProperty(Velocity.RESOURCE_LOADER, "classpath");
       Velocity.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
       Velocity.init();
 
       m_template = Velocity.getTemplate(TEMPLATES_DIR + DEFAULT_TEMPLATE_FILE_NAME, "UTF-8");
    }
 
    /**
     * Constructor for {@link TestPlanHtmlGeneratorImpl}.
     * <p>
     * Initialize {@link Velocity} with the file resource loader. </>
     */
    public TestPlanHtmlGeneratorImpl(String p_FilePath)
    {
 
       Velocity.setProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, Log4JLogChute.class.getName());
       Velocity.setProperty(Log4JLogChute.RUNTIME_LOG_LOG4J_LOGGER, "velocity");
 
       Velocity.setProperty(Velocity.RESOURCE_LOADER, "file");
       Velocity.setProperty("file.resource.loader.class", FileResourceLoader.class.getName());
       Velocity.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH, getTemplateDirFromTemplateFilePath(p_FilePath));
       Velocity.init();
 
       m_template = Velocity.getTemplate(getFileName(p_FilePath), "UTF-8");
    }
 
    // ------------------------- public methods-------------------------
 
    /**
     * {@inheritDoc}
     * @see fr.egiov.testplandoclet.html.TestPlanHtmlGenerator#generate(java.lang.String, fr.egiov.testplandoclet.plan.TestPlan)
     */
    @Override
    public void generate(String p_applicationName, TestPlan p_testplan)
    {
       VelocityContext context = null;
      Writer writer = null;
 
       context = new VelocityContext();
       context.put("applicatioName", p_applicationName);
       context.put("testplan", p_testplan);
 
       try
       {
         writer = new OutputStreamWriter(new FileOutputStream(Configuration.getFileName()), "UTF-8");
          m_template.merge(context, writer);
          writer.flush();
       }
       catch (IOException e)
       {
          e.printStackTrace();
       }
    }
 
    // ------------------------- protected methods-------------------------
 
    /**
     * Returns the template files dir from the filepath passed in parameter.
     * @param p_FilePath The template filepath.
     * @return The template files dir from the filepath passed in parameter.
     */
    protected static String getTemplateDirFromTemplateFilePath(String p_FilePath)
    {
       String dir = null;
       int index = -1;
 
       index = p_FilePath.lastIndexOf('/');
       if (index > 0)
       {
          dir = p_FilePath.substring(0, index);
       }
       else
       {
          dir = ".";
       }
       return dir;
    }
 
    /**
     * Returns the filename from the filepath passed in parameter.
     * @param p_FilePath The template filepath.
     * @return The filename from the filepath passed in parameter.
     */
    protected static String getFileName(String p_FilePath)
    {
       String dir = null;
       int index = -1;
 
       index = p_FilePath.lastIndexOf('/');
       if (index > 0)
       {
          dir = p_FilePath.substring(index + 1, p_FilePath.length());
       }
       else
       {
          dir = p_FilePath;
       }
       return dir;
    }
 }
