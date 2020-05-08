 /**
  * Copyright 2010 OpenEngSB Division, Vienna University of Technology
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.openengsb.opencit.core.config;
 
 import static org.mockito.Matchers.any;
 import static org.mockito.Matchers.anyString;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.io.File;
 
 import org.apache.commons.io.FileUtils;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.openengsb.core.common.Domain;
 import org.openengsb.core.common.context.ContextCurrentService;
 import org.openengsb.core.workflow.internal.WorkflowServiceImpl;
 import org.openengsb.core.workflow.internal.persistence.PersistenceRuleManager;
 import org.openengsb.domain.build.BuildDomain;
 import org.openengsb.domain.build.BuildStartEvent;
 import org.openengsb.domain.build.BuildSuccessEvent;
 import org.openengsb.domain.deploy.DeployDomain;
 import org.openengsb.domain.deploy.DeployFailEvent;
 import org.openengsb.domain.notification.NotificationDomain;
 import org.openengsb.domain.notification.model.Notification;
 import org.openengsb.domain.report.ReportDomain;
 import org.openengsb.domain.report.model.Report;
 import org.openengsb.domain.scm.ScmDomain;
 import org.openengsb.domain.test.TestDomain;
 import org.openengsb.domain.test.TestSuccessEvent;
 import org.openengsb.opencit.core.projectmanager.ProjectManager;
 import org.openengsb.opencit.core.projectmanager.model.Project;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
 
 public class FlowExecutionTest {
 
     @Before
     public void setUp() throws Exception {
         FileUtils.deleteDirectory(new File("data"));
     }
 
     @After
     public void tearDown() throws Exception {
         FileUtils.deleteDirectory(new File("data"));
     }
 
     private BundleContext bundleContext;
     private ReportDomain reportDomain;
     private NotificationDomain notificationDomain;
 
     @Test
     public void testExecuteWorkflow() throws Exception {
         PersistenceRuleManager directoryRuleSource = new PersistenceRuleManager(); /* FIXME */
         directoryRuleSource.init();
 
         WorkflowServiceImpl service = new WorkflowServiceImpl();
         service.setRulemanager(directoryRuleSource);
 
         ContextCurrentService contextService = mock(ContextCurrentService.class);
         when(contextService.getThreadLocalContext()).thenReturn("foo");
         service.setCurrentContextService(contextService);
 
         bundleContext = mock(BundleContext.class);
         service.setBundleContext(bundleContext);
 
         mockDomain(TestDomain.class, "test");
         reportDomain = mockDomain(ReportDomain.class, "report");
         when(reportDomain.generateReport(anyString(), anyString(), anyString())).thenReturn(new Report("testreport"));
         notificationDomain = mockDomain(NotificationDomain.class, "notification");
         mockDomain(DeployDomain.class, "deploy");
         mockDomain(BuildDomain.class, "build");
         mockDomain(ScmDomain.class, "scm");
         ProjectManager projectManager = mockOsgiService(ProjectManager.class,
             "(&(openengsb.service.type=workflow-service)(openengsb.workflow.globalid=projectManager))");
         Project projectMock = mock(Project.class);
         when(projectManager.getCurrentContextProject()).thenReturn(projectMock);
 
         OpenCitConfigurator configurator = new OpenCitConfigurator();
         configurator.setRuleManager(directoryRuleSource);
         configurator.init();
 
         long pid = service.startFlow("ci");
 
         service.processEvent(new BuildStartEvent(pid));
         service.processEvent(new BuildSuccessEvent(pid, "output"));
         service.processEvent(new TestSuccessEvent(pid, "testoutput"));
         service.processEvent(new DeployFailEvent(pid, "deployoutput"));
 
         service.waitForFlowToFinish(pid);
         verify(notificationDomain).notify(any(Notification.class));
     }
 
     private <T> T mockDomain(Class<T> domainInterface, String name) throws InvalidSyntaxException {
         String filter = String.format("(&(openengsb.service.type=domain)(id=domain.%s))", name);
         return mockOsgiService(domainInterface, filter, Domain.class);
     }
 
     private <T> T mockOsgiService(Class<T> resultClass, String filter, Class<?>... queryInterfaces)
         throws InvalidSyntaxException {
 
         ServiceReference serviceRefMock = mock(ServiceReference.class);
         if (queryInterfaces.length == 0) {
             when(bundleContext.getAllServiceReferences(resultClass.getName(), filter)).thenReturn(
                 new ServiceReference[]{ serviceRefMock, });
         }
         for (Class<?> c : queryInterfaces) {
             when(bundleContext.getAllServiceReferences(c.getName(), filter)).thenReturn(
                     new ServiceReference[]{ serviceRefMock, });
         }
 
         T domainMock = mock(resultClass);
         when(bundleContext.getService(serviceRefMock)).thenReturn(domainMock);
         return domainMock;
     }
 }
