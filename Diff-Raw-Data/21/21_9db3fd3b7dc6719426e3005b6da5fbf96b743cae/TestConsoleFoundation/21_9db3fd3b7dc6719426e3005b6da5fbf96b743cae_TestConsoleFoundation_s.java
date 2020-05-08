 // Copyright (C) 2008 - 2011 Philip Aston
 // All rights reserved.
 //
 // This file is part of The Grinder software distribution. Refer to
 // the file LICENSE which is part of The Grinder distribution for
 // licensing details. The Grinder distribution is available on the
 // Internet at http://grinder.sourceforge.net/
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 // FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 // COPYRIGHT HOLDERS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 // INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 // SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 // HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 // STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 // ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 // OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package net.grinder.console;
 
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.verifyNoMoreInteractions;
 import static org.mockito.Mockito.when;
 
 import java.io.File;
 import java.net.InetAddress;
 import java.net.ServerSocket;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.regex.Pattern;
 
 import net.grinder.common.Logger;
 import net.grinder.common.LoggerStubFactory;
 import net.grinder.common.Test;
 import net.grinder.communication.MessageDispatchRegistry;
 import net.grinder.communication.MessageDispatchRegistry.Handler;
 import net.grinder.console.ConsoleFoundation.UI;
 import net.grinder.console.client.ConsoleConnection;
 import net.grinder.console.client.ConsoleConnectionException;
 import net.grinder.console.client.ConsoleConnectionFactory;
 import net.grinder.console.common.ErrorHandler;
 import net.grinder.console.common.Resources;
 import net.grinder.console.common.StubResources;
 import net.grinder.console.communication.ConsoleCommunication;
 import net.grinder.console.communication.DistributionControl;
 import net.grinder.console.communication.ProcessControl;
 import net.grinder.console.communication.server.DispatchClientCommands;
 import net.grinder.console.distribution.FileDistribution;
 import net.grinder.console.model.ConsoleProperties;
 import net.grinder.console.model.SampleModel;
 import net.grinder.console.model.SampleModelViews;
 import net.grinder.messages.console.RegisterExpressionViewMessage;
 import net.grinder.messages.console.RegisterTestsMessage;
 import net.grinder.messages.console.ReportStatisticsMessage;
 import net.grinder.statistics.ExpressionView;
 import net.grinder.statistics.StatisticsServices;
 import net.grinder.statistics.StatisticsServicesImplementation;
 import net.grinder.statistics.TestStatisticsMap;
 import net.grinder.testutility.AbstractFileTestCase;
 import net.grinder.testutility.StubTimer;
 import net.grinder.util.Directory;
 
 import org.junit.Before;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Captor;
 import org.mockito.MockitoAnnotations;
 
 
 /**
  * Unit tests for {@link ConsoleFoundation}.
  *
  * @author Philip Aston
  * @version $Revision$
  */
 public class TestConsoleFoundation extends AbstractFileTestCase {
 
   private static TestConsoleFoundation s_instance;
 
   private static void setInstance(TestConsoleFoundation instance) {
     s_instance = instance;
   }
 
   @Captor private
   ArgumentCaptor<Handler<RegisterTestsMessage>> m_handlerCaptor1;
 
   @Captor private
   ArgumentCaptor<Handler<ReportStatisticsMessage>> m_handlerCaptor2;
 
   @Captor private
   ArgumentCaptor<Handler<RegisterExpressionViewMessage>> m_handlerCaptor3;
 
   @Before public void setUp() {
     MockitoAnnotations.initMocks(this);
   }
 
   private final Resources m_resources =
     new StubResources<Object>(new HashMap<String, Object>() {{
     }});
 
   private final LoggerStubFactory m_loggerStubFactory = new LoggerStubFactory();
   private final Logger m_logger = m_loggerStubFactory.getLogger();
 
   public void testConstruction() throws Exception {
 
     final ConsoleFoundation foundation =
       new ConsoleFoundation(m_resources, m_logger);
 
     setInstance(this);
 
     final MyUI ui = (MyUI)foundation.createUI(MyUI.class);
     assertNotNull(ui);
 
     final Thread runConsole = new Thread(new Runnable() {
       public void run() {
         try {
           foundation.run();
         }
         catch (Exception e) {
           // Ignore.
         }
       }
     });
 
     runConsole.start();
 
     foundation.shutdown();
 
     m_loggerStubFactory.assertNoMoreCalls();
   }
 
   public void testSimpleRun() throws Exception {
 
     final Timer timer = new Timer(true);
 
     final ConsoleProperties consoleProperties =
       new ConsoleProperties(m_resources, new File(getDirectory(), "props"));
 
    final ConsoleFoundation foundation =
      new ConsoleFoundation(m_resources, m_logger, timer, consoleProperties);

    m_loggerStubFactory.assertNoMoreCalls();

    final Thread runConsole = new Thread(new Runnable() {
      public void run() { foundation.run(); }
    });

     // Figure out a free local port.
 
     final ServerSocket serverSocket =
       new ServerSocket(0, 50, InetAddress.getLocalHost());
     final String hostName = serverSocket.getInetAddress().getHostName();
     final int port = serverSocket.getLocalPort();
     serverSocket.close();
     consoleProperties.setConsoleHost(hostName);
     consoleProperties.setConsolePort(port);
 
     runConsole.start();
 
     final ConsoleConnectionFactory ccf = new ConsoleConnectionFactory();
 
     final int retries = 3;
 
     for (int i = 0; i < retries; ++i) {
       try {
 	final ConsoleConnection client = ccf.connect(hostName, port);
 	assertEquals(0, client.getNumberOfAgents());
 	client.close();
       }
       catch (ConsoleConnectionException e) {
 	if (i == retries - 1) {
 	  throw e;
 	}
 
 	Thread.sleep(50);
       }
     }
 
     foundation.shutdown();
 
     runConsole.join();
   }
 
   public void testWireFileDistribution() throws Exception {
 
     final FileDistribution fileDistribution = mock(FileDistribution.class);
 
     final ConsoleProperties consoleProperties =
       new ConsoleProperties(m_resources, new File(getDirectory(), "props"));
 
     final StubTimer timer = new StubTimer();
 
     new ConsoleFoundation.WireFileDistribution(fileDistribution,
                                                consoleProperties,
                                                timer);
 
     assertEquals(6000, timer.getLastDelay());
     assertEquals(6000, timer.getLastPeriod());
 
     final TimerTask scanFileTask = timer.getLastScheduledTimerTask();
     scanFileTask.run();
     verify(fileDistribution).scanDistributionFiles();
 
     consoleProperties.setDistributionFileFilterExpression(".*");
 
     final ArgumentCaptor<Pattern> patternCaptor =
       ArgumentCaptor.forClass(Pattern.class);
 
     verify(fileDistribution).setFileFilterPattern(patternCaptor.capture());
     assertEquals(".*", patternCaptor.getValue().pattern());
 
     final ArgumentCaptor<Directory> directoryCaptor =
       ArgumentCaptor.forClass(Directory.class);
 
     final Directory directory = new Directory(new File(getDirectory(), "foo"));
     consoleProperties.setAndSaveDistributionDirectory(directory);
 
     verify(fileDistribution).setDirectory(directoryCaptor.capture());
     assertSame(directory, directoryCaptor.getValue());
 
     consoleProperties.setConsolePort(999);
 
     verifyNoMoreInteractions(fileDistribution);
   }
 
   public void testWireMessageDispatch() throws Exception {
     final MessageDispatchRegistry messageDispatchRegistry =
       mock(MessageDispatchRegistry.class);
 
     final ConsoleCommunication consoleCommunication =
       mock(ConsoleCommunication.class);
     when(consoleCommunication.getMessageDispatchRegistry())
       .thenReturn(messageDispatchRegistry);
 
     final SampleModel sampleModel = mock(SampleModel.class);
 
     final SampleModelViews sampleModelViews = mock(SampleModelViews.class);
 
     final DispatchClientCommands dispatchClientCommands =
       new DispatchClientCommands(null, null, null);
 
     new ConsoleFoundation.WireMessageDispatch(consoleCommunication,
                                               sampleModel,
                                               sampleModelViews,
                                               dispatchClientCommands);
 
     verify(consoleCommunication).getMessageDispatchRegistry();
 
     verify(messageDispatchRegistry).set(eq(RegisterTestsMessage.class),
                                         m_handlerCaptor1.capture());
 
     final Collection<Test> tests = Collections.emptySet();
     m_handlerCaptor1.getValue().handle(new RegisterTestsMessage(tests));
 
     verify(sampleModel).registerTests(tests);
 
     verify(messageDispatchRegistry).set(eq(ReportStatisticsMessage.class),
                                         m_handlerCaptor2.capture());
 
     final TestStatisticsMap delta = new TestStatisticsMap();
     m_handlerCaptor2.getValue().handle(new ReportStatisticsMessage(delta));
 
     verify(sampleModel).addTestReport(delta);
 
     verify(messageDispatchRegistry).set(
       eq(RegisterExpressionViewMessage.class), m_handlerCaptor3.capture());
 
     final ExpressionView expressionView =
       StatisticsServicesImplementation.getInstance()
       .getStatisticExpressionFactory().createExpressionView(
         "blah", "userLong0", false);
     m_handlerCaptor3.getValue().handle(
       new RegisterExpressionViewMessage(expressionView));
 
     verify(sampleModelViews).registerStatisticExpression(expressionView);
 
     verifyNoMoreInteractions(consoleCommunication,
                              sampleModel,
                              sampleModelViews);
   }
 
   public static class MyUI implements UI {
     public MyUI(Logger logger,
                 Resources resources,
                 ConsoleProperties properties,
                 StatisticsServices statisticsServices,
                 SampleModel model,
                 ConsoleCommunication consoleCommunication,
                 DistributionControl distributionControl,
                 FileDistribution fileDistribution,
                 ProcessControl processControl) {
       assertSame(s_instance.m_logger, logger);
       assertSame(s_instance.m_resources, resources);
       assertNotNull(properties);
       assertSame(StatisticsServicesImplementation.getInstance(),
                  statisticsServices);
       assertNotNull(model);
       assertNotNull(consoleCommunication);
       assertNotNull(distributionControl);
       assertNotNull(fileDistribution);
       assertNotNull(processControl);
     }
 
     public ErrorHandler getErrorHandler() {
       return null;
     }
   }
 }
