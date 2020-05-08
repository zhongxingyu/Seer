 /*
  *  The MIT License
  *
  *  Copyright 2011 Sony Ericsson Mobile Communications. All rights reserved.
  *
  *  Permission is hereby granted, free of charge, to any person obtaining a copy
  *  of this software and associated documentation files (the "Software"), to deal
  *  in the Software without restriction, including without limitation the rights
  *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  *  copies of the Software, and to permit persons to whom the Software is
  *  furnished to do so, subject to the following conditions:
  *
  *  The above copyright notice and this permission notice shall be included in
  *  all copies or substantial portions of the Software.
  *
  *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  *  THE SOFTWARE.
  */
 package com.sonyericsson.jenkins.plugins.externalresource.dispatcher;
 
 import com.sonyericsson.hudson.plugins.metadata.model.MetadataBuildAction;
 import com.sonyericsson.hudson.plugins.metadata.model.MetadataNodeProperty;
 import com.sonyericsson.hudson.plugins.metadata.model.values.MetadataValue;
 import com.sonyericsson.hudson.plugins.metadata.model.values.TreeStructureUtil;
 import com.sonyericsson.jenkins.plugins.externalresource.dispatcher.data.ExternalResource;
 import com.sonyericsson.jenkins.plugins.externalresource.dispatcher.selection.AbstractDeviceSelection;
 import com.sonyericsson.jenkins.plugins.externalresource.dispatcher.selection.StringDeviceSelection;
 import hudson.model.Cause;
 import hudson.model.FreeStyleBuild;
 import hudson.model.FreeStyleProject;
 import hudson.model.labels.LabelAtom;
 import hudson.slaves.DumbSlave;
 import hudson.tasks.Mailer;
 import hudson.tasks.Shell;
 import org.jvnet.hudson.test.HudsonTestCase;
 
 import java.util.Collections;
 import java.util.LinkedList;
 
 /**
  * Hudson Tests for {@link ReleaseRunListener}.
  *
  * @author Robert Sandell &lt;robert.sandell@sonyericsson.com&gt;
  */
 public class ReleaseRunListenerHudsonTest extends HudsonTestCase {
 
     //CS IGNORE MagicNumber FOR NEXT 200 LINES. REASON: Test data.
 
     private DumbSlave slave;
     private MetadataNodeProperty property;
     private ExternalResource resource;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         slave = this.createOnlineSlave(new LabelAtom("TEST"));
         property = new MetadataNodeProperty((new LinkedList<MetadataValue>()));
         slave.getNodeProperties().add(property);
         resource = new ExternalResource("TestDevice", "description", "1", true,
                 new LinkedList<MetadataValue>());
         TreeStructureUtil.addValue(resource, "yes", "description", "is", "matching");
         TreeStructureUtil.addValue(property, resource, "attached-devices", "test");
         Mailer.descriptor().setHudsonUrl(this.getURL().toString());
     }
 
     /**
      * Happy Test for {@link ReleaseRunListener#onCompleted(hudson.model.AbstractBuild, hudson.model.TaskListener)}.
      *
      * @throws Exception if so.
      */
     public void testOnCompleted() throws Exception {
         FreeStyleProject project = this.createFreeStyleProject("testProject");
         project.setAssignedLabel(new LabelAtom("TEST"));
         project.getBuildersList().add(new Shell("sleep 2"));
         AbstractDeviceSelection selection = new StringDeviceSelection("is.matching", "yes");
         project.addProperty(new SelectionCriteria(Collections.singletonList(selection)));
 
         FreeStyleBuild build = project.scheduleBuild2(0, new Cause.UserCause()).get();
         assertBuildStatusSuccess(build);
         MetadataBuildAction metadata = build.getAction(MetadataBuildAction.class);
         assertNotNull(metadata);
         ExternalResource buildResource = (ExternalResource)TreeStructureUtil.getPath(metadata,
                 Constants.BUILD_LOCKED_RESOURCE_PATH);
         assertNotNull(buildResource);
         assertNull(buildResource.getLocked());
         assertNull(resource.getLocked());
     }
 }
