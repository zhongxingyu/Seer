 package com.atlassian.plugin.osgi.factory.transform;
 
 import junit.framework.TestCase;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import com.atlassian.plugin.osgi.hostcomponents.HostComponentRegistration;
 
 public class TestHostComponentSpringTransformer extends TestCase
 {
     public void testTransform() throws IOException
     {
         HostComponentSpringTransformer transformer = new HostComponentSpringTransformer();
 
         // host component with name
         Element pluginRoot = DocumentHelper.createDocument().addElement("atlassian-plugin");
         SpringTransformerTestHelper.transform(transformer, new ArrayList<HostComponentRegistration>(){{
             add(new StubHostComponentRegistration("foo", String.class));
        }}, pluginRoot, "osgi:reference[@id='foo' and @filter='(bean-name=foo)']/osgi:interfaces/beans:value/text()='java.lang.String'");
 
         // host component with name with # sign
         pluginRoot = DocumentHelper.createDocument().addElement("atlassian-plugin");
         SpringTransformerTestHelper.transform(transformer, new ArrayList<HostComponentRegistration>(){{
             add(new StubHostComponentRegistration("foo#1", String.class));
        }}, pluginRoot, "osgi:reference[@id='fooLB1' and @filter='(bean-name=foo#1)']/osgi:interfaces/beans:value/text()='java.lang.String'");
 
         // host component with no bean name
         pluginRoot = DocumentHelper.createDocument().addElement("atlassian-plugin");
         SpringTransformerTestHelper.transform(transformer, new ArrayList<HostComponentRegistration>(){{
             add(new StubHostComponentRegistration(String.class));
         }}, pluginRoot, "osgi:reference[@id='bean0' and not(@filter)]/osgi:interfaces/beans:value/text()='java.lang.String'");
     }
 }
