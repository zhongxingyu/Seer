 /*
  * The MIT License
  * 
  * Copyright (c) 2013 IKEDA Yasuyuki
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 package jp.ikedam.jenkins.plugins.groovy_label_assignment;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import jenkins.model.Jenkins;
 
 import hudson.matrix.AxisList;
 import hudson.matrix.MatrixRun;
 import hudson.matrix.LabelAxis;
 import hudson.matrix.MatrixBuild;
 import hudson.matrix.MatrixProject;
 import hudson.matrix.TextAxis;
 import hudson.model.AbstractBuild;
 import hudson.model.FreeStyleBuild;
 import hudson.model.ParameterValue;
 import hudson.model.AbstractProject;
 import hudson.model.Cause;
 import hudson.model.FreeStyleProject;
 import hudson.model.Node;
 import hudson.model.ParametersAction;
 import hudson.model.ParametersDefinitionProperty;
 import hudson.model.StringParameterDefinition;
 import hudson.model.StringParameterValue;
 import hudson.model.labels.LabelExpression;
 import hudson.slaves.DumbSlave;
 import hudson.util.FormValidation;
 
 import org.apache.commons.lang.StringUtils;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.jvnet.hudson.test.JenkinsRule.WebClient;
 import org.jvnet.hudson.test.CaptureEnvironmentBuilder;
import org.xml.sax.SAXException;
 
 import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
 import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 import com.gargoylesoftware.htmlunit.html.HtmlTextArea;
 
 import static org.junit.Assert.*;
 
 /**
  * Tests for GroovyLabelAssignmentProperty, working with Jenkins.
  */
 public class GroovyLabelAssignmentPropertyJenkinsTest
 {
     private static final int BUILD_REPEAT = 2;
     private static final long BUILD_TIMEOUT = 5 * 1000;
     @Rule
     public GroovyLabelAssignmentJenkinsRule j = new GroovyLabelAssignmentJenkinsRule();
     
     private GroovyLabelAssignmentProperty.DescriptorImpl getDescriptor()
     {
         return (GroovyLabelAssignmentProperty.DescriptorImpl)Jenkins.getInstance().getDescriptor(GroovyLabelAssignmentProperty.class);
     }
     
     
     @SuppressWarnings("deprecation")
     private <P extends AbstractProject<P,R>,R extends AbstractBuild<P,R>> R scheduleBuildWithParameters(
             P project,
             ParameterValue ... parameters) throws InterruptedException, ExecutionException, TimeoutException
     {
         Future<R> future = project.scheduleBuild2(
                 project.getQuietPeriod(),
                 new Cause.LegacyCodeCause(),
                 new ParametersAction(parameters)
         );
         if(future == null)
         {
             return null;
         }
         return future.get(BUILD_TIMEOUT + project.getQuietPeriod() * 1000, TimeUnit.MILLISECONDS);
     }
     
     private void assertBuiltOn(Node node, AbstractBuild<?,?> build)
     {
         assertEquals(node.getNodeName(), build.getBuiltOn().getNodeName());
     }
     
     protected DumbSlave slave1;
     protected DumbSlave slave2;
     protected DumbSlave slave3;
     
     @Before
     public void setupSlaves() throws Exception
     {
         slave1 = j.createOnlineSlave("test1 common1");
         slave2 = j.createOnlineSlave("test2 common2");
         slave3 = j.createOnlineSlave("test3 common1 common2");
     }
     
     @Before
     public void setupQuietPeriod() throws IOException
     {
         Jenkins.getInstance().setQuietPeriod(new Integer(1));
     }
     
     @Test
     public void testFreeStyleProjectWithoutGroovyLabelAssignmentProperty() throws Exception
     {
         // Test the behavior without GroovyLabelAssignmentProperty.
         // This is a regression test and a test for test methods.
         
         String paramName = "PARAM1";
         String defaultParamValue = "VALUE";
         
         FreeStyleProject project = j.createFreeStyleProject();
         ParametersDefinitionProperty paramProp = new ParametersDefinitionProperty(
                 new StringParameterDefinition(paramName, defaultParamValue)
         );
         project.addProperty(paramProp);
         CaptureEnvironmentBuilder ceb = new CaptureEnvironmentBuilder();
         project.getBuildersList().add(ceb);
         
         // Build is done with specified node.
         {
             project.setAssignedLabel(LabelExpression.parseExpression("test2"));
             
             for(int i = 0; i < BUILD_REPEAT; ++i)
             {
                 String paramValue = "AnotherValue";
                 
                 FreeStyleBuild build = scheduleBuildWithParameters(
                         project,
                         new StringParameterValue(paramName, paramValue)
                 );
                 assertBuiltOn(slave2, build);
                 assertEquals(paramValue, ceb.getEnvVars().get(paramName));
             }
         }
         
         // Build is done with specified label combination.
         {
             project.setAssignedLabel(LabelExpression.parseExpression("common1&&common2"));
             
             for(int i = 0; i < BUILD_REPEAT; ++i)
             {
                 String paramValue = "AnotherValue";
                 
                 FreeStyleBuild build = scheduleBuildWithParameters(
                         project,
                         new StringParameterValue(paramName, paramValue)
                 );
                 assertBuiltOn(slave3, build);
                 assertEquals(paramValue, ceb.getEnvVars().get(paramName));
             }
         }
         
         // Build is not done with non-exist label
         {
             project.setAssignedLabel(LabelExpression.parseExpression("nosuchnode"));
             
             String paramValue = "AnotherValue";
             try
             {
                 scheduleBuildWithParameters(
                         project,
                         new StringParameterValue(paramName, paramValue)
                 );
                 fail("Build must be timed out!");
             }
             catch(TimeoutException e)
             {
                 assertTrue(Jenkins.getInstance().getQueue().cancel(project));
             }
         }
     }
     
     @Test
     public void testFreeStyleProjectWithGroovyLabelAssignmentProperty() throws Exception
     {
         String paramName = "PARAM1";
         String defaultParamValue = "VALUE";
         
         FreeStyleProject project = j.createFreeStyleProject();
         ParametersDefinitionProperty paramProp = new ParametersDefinitionProperty(
                 new StringParameterDefinition(paramName, defaultParamValue)
         );
         project.addProperty(paramProp);
         GroovyLabelAssignmentProperty prop = new GroovyLabelAssignmentProperty(null);
         project.addProperty(prop);
         CaptureEnvironmentBuilder ceb = new CaptureEnvironmentBuilder();
         project.getBuildersList().add(ceb);
         
         // Specify patterns static
         {
             project.setAssignedLabel(LabelExpression.parseExpression("master"));
             
             Map<String, Node> slaveMap = new HashMap<String, Node>();
             slaveMap.put(slave1.getNodeName(), slave1);
             slaveMap.put(slave2.getNodeName(), slave2);
             slaveMap.put(slave3.getNodeName(), slave3);
             slaveMap.put("test1", slave1);
             slaveMap.put("test2", slave2);
             slaveMap.put("test3", slave3);
             slaveMap.put("common1 && common2", slave3);
             slaveMap.put("", Jenkins.getInstance());
             slaveMap.put("  ", Jenkins.getInstance());
             
             for(Map.Entry<String,Node> entry: slaveMap.entrySet())
             {
                 for(int i = 0; i < BUILD_REPEAT; ++i)
                 {
                     String paramValue = "AnotherValue";
                     prop.setGroovyScript(String.format("return \"%s\";", entry.getKey()));
                     
                     FreeStyleBuild build = scheduleBuildWithParameters(
                             project,
                             new StringParameterValue(paramName, paramValue)
                     );
                     assertBuiltOn(entry.getValue(), build);
                 }
             }
         }
         
         // Using a parameter
         {
             project.setAssignedLabel(LabelExpression.parseExpression("test1"));
             prop.setGroovyScript(String.format("return %s;", paramName));
             
             Map<String, Node> slaveMap = new HashMap<String, Node>();
             slaveMap.put(slave1.getNodeName(), slave1);
             slaveMap.put(slave2.getNodeName(), slave2);
             slaveMap.put(slave3.getNodeName(), slave3);
             slaveMap.put("test1", slave1);
             slaveMap.put("test2", slave2);
             slaveMap.put("test3", slave3);
             slaveMap.put("common1 && common2", slave3);
             slaveMap.put("", slave1);
             slaveMap.put("  ", slave1);
             
             for(Map.Entry<String,Node> entry: slaveMap.entrySet())
             {
                 for(int i = 0; i < BUILD_REPEAT; ++i)
                 {
                     String paramValue = entry.getKey();
                     
                     FreeStyleBuild build = scheduleBuildWithParameters(
                             project,
                             new StringParameterValue(paramName, paramValue)
                     );
                     assertBuiltOn(entry.getValue(), build);
                 }
             }
         }
         
         // returning null
         {
             project.setAssignedLabel(LabelExpression.parseExpression("test2"));
             prop.setGroovyScript(String.format("return null;", paramName));
             
             for(int i = 0; i < BUILD_REPEAT; ++i)
             {
                 String paramValue = "AnotherValue1";
                 
                 FreeStyleBuild build = scheduleBuildWithParameters(
                         project,
                         new StringParameterValue(paramName, paramValue)
                 );
                 assertBuiltOn(slave2, build);
             }
         }
     }
     
     @Test
     public void testMatrixProjectWithoutGroovyLabelAssignmentProperty() throws Exception
     {
         // Test the behavior without GroovyLabelAssignmentProperty.
         // This is a regression test and a test for test methods.
         
         String paramName = "PARAM1";
         String defaultParamValue = "VALUE";
         
         MatrixProject project = j.createMatrixProject();
         AxisList axes = new AxisList();
         axes.add(new TextAxis("axisParam", "axis1","axis2"));
         axes.add(new LabelAxis("axisLabel", Arrays.asList("test1", "test2")));
         project.setAxes(axes);
         project.setCombinationFilter("(axisParam==\"axis1\" && axisLabel==\"test1\") || (axisParam==\"axis2\" && axisLabel==\"test2\")");
         
         ParametersDefinitionProperty paramProp = new ParametersDefinitionProperty(
                 new StringParameterDefinition(paramName, defaultParamValue)
         );
         project.addProperty(paramProp);
         
         // Build is done with specified node.
         {
             for(int i = 0; i < BUILD_REPEAT; ++i)
             {
                 String paramValue = "AnotherValue";
                 
                 MatrixBuild build = scheduleBuildWithParameters(
                         project,
                         new StringParameterValue(paramName, paramValue)
                 );
                 
                 for(MatrixRun child: build.getRuns())
                 {
                     String axisParam = child.getProject().getCombination().get("axisParam");
                     if("axis1".equals(axisParam))
                     {
                         assertBuiltOn(slave1, child);
                     }
                     else if("axis2".equals(axisParam))
                     {
                         assertBuiltOn(slave2, child);
                     }
                     else
                     {
                         fail(String.format("unknown combination: %s", child.getProject().getCombination().toString()));
                     }
                 }
             }
         }
     }
     
     @Test
     public void testMatrixProjectWithGroovyLabelAssignmentProperty() throws Exception
     {
         String paramName = "PARAM1";
         String defaultParamValue = "VALUE";
         
         MatrixProject project = j.createMatrixProject();
         AxisList axes = new AxisList();
         axes.add(new TextAxis("axisParam", "axis1","axis2"));
         axes.add(new LabelAxis("axisLabel", Arrays.asList("test1", "test2")));
         project.setAxes(axes);
         project.setCombinationFilter("(axisParam==\"axis1\" && axisLabel==\"test1\") || (axisParam==\"axis2\" && axisLabel==\"test2\")");
         
         ParametersDefinitionProperty paramProp = new ParametersDefinitionProperty(
                 new StringParameterDefinition(paramName, defaultParamValue)
         );
         project.addProperty(paramProp);
         
         GroovyLabelAssignmentProperty prop = new GroovyLabelAssignmentProperty(null);
         project.addProperty(prop);
         
         {
             prop.setGroovyScript(StringUtils.join(Arrays.asList(
                     "switch(binding.getVariables().get(\"axisParam\")){",
                     "case \"axis1\":",
                     "    return \"common1&&common2\";",
                     "case \"axis2\":",
                     "    return \"test1\";",
                     "}",
                     "return null;"
             ), "\n"));
             for(int i = 0; i < BUILD_REPEAT; ++i)
             {
                 String paramValue = "AnotherValue";
                 
                 MatrixBuild build = scheduleBuildWithParameters(
                         project,
                         new StringParameterValue(paramName, paramValue)
                 );
                 
                 for(MatrixRun child: build.getRuns())
                 {
                     String axisParam = child.getProject().getCombination().get("axisParam");
                     if("axis1".equals(axisParam))
                     {
                         assertBuiltOn(slave3, child);
                     }
                     else if("axis2".equals(axisParam))
                     {
                         assertBuiltOn(slave1, child);
                     }
                     else
                     {
                         fail(String.format("unknown combination: %s", child.getProject().getCombination().toString()));
                     }
                 }
             }
         }
     }
     
     @Test
     public void testGroovyLabelAssignmentPropertyError() throws Exception
     {
         String paramName = "PARAM1";
         String defaultParamValue = "VALUE";
         
         FreeStyleProject project = j.createFreeStyleProject();
         ParametersDefinitionProperty paramProp = new ParametersDefinitionProperty(
                 new StringParameterDefinition(paramName, defaultParamValue)
         );
         project.addProperty(paramProp);
         GroovyLabelAssignmentProperty prop = new GroovyLabelAssignmentProperty(null);
         project.addProperty(prop);
         CaptureEnvironmentBuilder ceb = new CaptureEnvironmentBuilder();
         project.getBuildersList().add(ceb);
         
         // script is null
         {
             project.setAssignedLabel(LabelExpression.parseExpression("test3"));
             prop.setGroovyScript(null);
             
             for(int i = 0; i < BUILD_REPEAT; ++i)
             {
                 String paramValue = "AnotherValue1";
                 
                 FreeStyleBuild build = scheduleBuildWithParameters(
                         project,
                         new StringParameterValue(paramName, paramValue)
                 );
                 assertNull(build);
             }
         }
         
         // script is blank
         {
             project.setAssignedLabel(LabelExpression.parseExpression("test1"));
             prop.setGroovyScript("  ");
             
             for(int i = 0; i < BUILD_REPEAT; ++i)
             {
                 String paramValue = "AnotherValue1";
                 
                 FreeStyleBuild build = scheduleBuildWithParameters(
                         project,
                         new StringParameterValue(paramName, paramValue)
                 );
                 assertNull(build);
             }
         }
         
         // script contains syntax error
         {
             project.setAssignedLabel(LabelExpression.parseExpression("test1"));
             prop.setGroovyScript("\"test1");
             
             for(int i = 0; i < BUILD_REPEAT; ++i)
             {
                 String paramValue = "AnotherValue1";
                 
                 FreeStyleBuild build = scheduleBuildWithParameters(
                         project,
                         new StringParameterValue(paramName, paramValue)
                 );
                 assertNull(build);
             }
         }
         
         
         // script contains runtime error
         {
             project.setAssignedLabel(LabelExpression.parseExpression("test1"));
             prop.setGroovyScript("return nosuchvariable;");
             
             for(int i = 0; i < BUILD_REPEAT; ++i)
             {
                 String paramValue = "AnotherValue1";
                 
                 FreeStyleBuild build = scheduleBuildWithParameters(
                         project,
                         new StringParameterValue(paramName, paramValue)
                 );
                 assertNull(build);
             }
         }
     }
     
     @Test
     public void testDescriptor_doCheckGroovyScript()
     {
         GroovyLabelAssignmentProperty.DescriptorImpl descriptor = getDescriptor();
         
         // ok for proper script
         {
             String script = "switch(p){case \"a\": return \"test1\"; case \"b\": return \"test2\";}; return \"\";";
             assertEquals(FormValidation.Kind.OK, descriptor.doCheckGroovyScript(script).kind);
         }
         
         // errors for null
         {
             String script = null;
             assertEquals(FormValidation.Kind.ERROR, descriptor.doCheckGroovyScript(script).kind);
         }
         
         // error for blank
         {
             String script = "  ";
             assertEquals(FormValidation.Kind.ERROR, descriptor.doCheckGroovyScript(script).kind);
         }
         
         // error for syntax error
         {
             String script = "{";
             assertEquals(FormValidation.Kind.ERROR, descriptor.doCheckGroovyScript(script).kind);
         }
     }
     
     @Test
     public void testConfiguration() throws Exception
     {
         WebClient wc = j.createWebClient();
         FreeStyleProject project = j.createFreeStyleProject();
         String name = project.getName();
         String groovyScript = "return null;";
         
         {
             HtmlPage page = wc.getPage(project, "configure");
             HtmlForm form = page.getFormByName("config");
             HtmlCheckBoxInput checkbox = form.getInputByName(GroovyLabelAssignmentProperty.PROPERTYNAME);
             page = (HtmlPage)checkbox.setChecked(true);
             
             form = page.getFormByName("config");
             HtmlTextArea textarea = form.getTextAreaByName("_.groovyScript");
             textarea.setText(groovyScript);
             j.submit(form);
         }
         
         project = (FreeStyleProject)Jenkins.getInstance().getItem(name);
         GroovyLabelAssignmentProperty prop = project.getProperty(GroovyLabelAssignmentProperty.class);
         assertNotNull(prop);
         assertEquals(groovyScript, prop.getGroovyScript());
     }
 }
