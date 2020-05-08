 /* 
 ==================================================================
  JSR 272 Mobile TV API -Evaluation Pack-
 http://sourceforge.net/projects/jsr272/
  
  Copyright (c) 2007 Eric Bréchemier <jsr272@eric.brechemier.name>
  Licensed under BSD License and/or MIT License.
 ==================================================================
 
 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                          BSD License
                              ~~~
              http://creativecommons.org/licenses/BSD/
 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
                           MIT License
 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   Copyright (c) 2007 Eric Bréchemier <jsr272@eric.brechemier.name>
   
   Permission is hereby granted, free of charge, to any person
   obtaining a copy of this software and associated documentation
   files (the "Software"), to deal in the Software without
   restriction, including without limitation the rights to use,
   copy, modify, merge, publish, distribute, sublicense, and/or sell
   copies of the Software, and to permit persons to whom the
   Software is furnished to do so, subject to the following
   conditions:
 
   The above copyright notice and this permission notice shall be
   included in all copies or substantial portions of the Software.
 
   THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
   EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
   OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
   NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
   HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
   WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
   FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
   OTHER DEALINGS IN THE SOFTWARE.
 
 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
 */
 package net.sf.jsr272.test;
 
 import java.util.Date;
 
 import javax.microedition.broadcast.ServiceComponent;
 import javax.microedition.broadcast.ServiceContext;
 import javax.microedition.broadcast.ServiceContextListener;
 
 import javax.microedition.broadcast.esg.CommonMetadataSet;
 import javax.microedition.broadcast.esg.DataUnavailableException;
 import javax.microedition.broadcast.esg.ProgramEvent;
 import javax.microedition.broadcast.esg.QueryComposer;
 import javax.microedition.broadcast.esg.QueryException;
 import javax.microedition.broadcast.esg.Service;
 import javax.microedition.broadcast.esg.ServiceGuide;
 import javax.microedition.broadcast.esg.ServiceGuideData;
 import javax.microedition.broadcast.esg.ServiceGuideListener;
 
 import javax.microedition.broadcast.platform.PlatformProvider;
 import javax.microedition.broadcast.platform.PlatformProviderSelector;
 import javax.microedition.broadcast.platform.PlatformProviderSelectorListener;
 
 import junit.framework.Assert;
 
 import net.sf.jsr272.stub.ServiceContextStub;
 import net.sf.jsr272.stub.platform.PlatformProviderSelectorStub;
 
 import net.sf.jsr272.util.ServiceGuideUtil;
 import net.sf.jsr272.util.PlatformProviderSelectorUtil;
 
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 public class MainTest implements PlatformProviderSelectorListener, ServiceGuideListener, ServiceContextListener
 {
   public static final String TEST_LOGGER = "";
   public static final Level LOG_LEVEL = Level.INFO;
   protected static Logger _log;
   
   public static void main(String[] args)
   {
     _log = Logger.getLogger(TEST_LOGGER);
     Assert.assertNotNull("Logger failed to initialize.",_log);
     _log.setLevel(LOG_LEVEL);
     
     try
     {
       _log.info("Tests Started from command line, using PlatformProviderSelectorStub.");
       
       PlatformProviderSelectorStub.useSingleton();
       ServiceContextStub.useSingleton();
       
       MainTest starter = new MainTest(args);
       starter.runTest();
     }
     catch(Exception e)
     {
       e.printStackTrace();
       _log.error(e);
     }
   }
   
   protected int _platformProviderId;
   protected String _serviceProviderName;
   protected ServiceGuide _currentESG;
   protected Service _selectedChannel;
   protected boolean _zappingContextInitialized;
   
   protected boolean[] _checkPoint;
   
   public MainTest(String[] args)
   {
     if(args.length!=2)
       throw new IllegalArgumentException("Usage: PlatformProviderId ServiceProviderName");
     
     _platformProviderId = Integer.parseInt(args[0]);
     _serviceProviderName = args[1];
     
     _log.info("Starting tests with PlatformProviderId='"+_platformProviderId+"' ServiceProviderName='"+_serviceProviderName+"'");
     
     _currentESG = null;
     _selectedChannel = null;
     _zappingContextInitialized = false;
     _checkPoint = new boolean[]
       {
         false, //  0: startTest()
         false, //  1: update(*,*,*)
         false, //  2: update(*,*,false)
         false, //  3: serviceGuideUpdated(*,*,*)
         false, //  4: serviceGuideUpdated(SERVICE_GUIDE_UPDATE_COMPLETED,*,*)
         false, //  5: updateCompleted()
         false, //  6: contextUpdate(*,*,*)
         false, //  7: contextUpdate(ServiceContext.getDefaultContext(),*,*)
         false, //  8: contextInstanceInitialized(...)
         false, //  9: serviceSelectionInitiated(...)
         false, // 10: defaultserviceComponentsSelected(...)
         false, // 11: serviceComponentsSelectionCompleted(...)
         false, // 12: playersInitialized(...)
         false, // 13: playersStarted(...)
         false  // 14: contextStopped(...)
       };
      
    // TODO: check synchronization for checkpoint 12
    //       to fix checkpoint 12 was not reached.
   }
 
   
   protected void runTest()
   {
     // I create a separate thread for the test to catch unexecuted parts
     // e.g. due to missing callbacks after completion of this test thread.
     
     final long MS_TEST_TIME_OUT = 1000;
     final long MS_SLEEP_DURATION = 100;
     
     Thread testThread = new Thread
     (
       new Runnable()
       { 
         public void run()
         {
           startTest();
         }
       }
     );
     _log.trace("Created Test Thread");
     testThread.start();
     _log.trace("Test Thread Started");
     
     _log.trace("Test Thread Completed... "+Thread.activeCount()+" Thread(s) still running");
     
     long msTime = System.currentTimeMillis();
     boolean isTimeOut = false;
     while( Thread.activeCount()>1 && !isTimeOut )
     {
       isTimeOut = (System.currentTimeMillis()-msTime>MS_TEST_TIME_OUT);
       
       try
       {
         _log.trace("Awoken: "+Thread.activeCount()+" Thread(s) still running");
         Thread.sleep(MS_SLEEP_DURATION);
       }
       catch(InterruptedException ie)
       {
         Assert.fail( "Thread Interrupted: "+ie.getMessage() );
       }
     }
     
     _log.trace("Completed: only "+Thread.activeCount()+" Thread remains.");
     
     for (int i=0; i<_checkPoint.length; i++)
     {
       Assert.assertTrue("Failed to pass at check point ["+i+"]",_checkPoint[i]);
     }
     
     if (isTimeOut)
     {
       Assert.fail( "Test Thread should have completed under "+((float)MS_TEST_TIME_OUT)/1000+" seconds." );
     }
     
   }
   
   protected void startTest()
   {
     _checkPoint[0] = true;
     _log.trace("Check Point 0");
     
     // xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
     // 1. Bootstrap - Select the Operator or "Platform Provider"
     // xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
     
     // 1.1 Start discovery for Platform Providers, usually scanning UHF,VHF frequency bands
     
     PlatformProviderSelector.addListener(this);
     
     // asynchronous call
     PlatformProviderSelector.discover();
   }
   
   // <implements interface="PlatformProviderSelectorListener">
   
   public void update(int progress, int found, boolean continuing)
   {
     _checkPoint[1] = true;
     _log.trace("Check Point 1");
     _log.info("Plaform Discovery ["+progress+"% complete] - "+found+" Platform Providers found.");
     
     if (continuing)
       return;
     
     _checkPoint[2] = true;
     _log.trace("Check Point 2");
     
     PlatformProviderSelector.removeListener(this);
     
     // 1.2 Get the list of Platform Providers (e.g. as announced in ESG Provider List)
     PlatformProvider[] availableProvider = PlatformProviderSelector.getAvailableProviders();
     Assert.assertNotNull
       (
       "getAvailableProviders() must not return null.",
       availableProvider
       );
     Assert.assertTrue
       (
       "No platform provider found. Cannot continue the tests.",
       availableProvider.length>0
       );
     
     // 1.3 Iterate over Providers and select the one with expected id
     //     an alternative would be to ask the user to choose her Operator
     boolean providerFound = false;
     for (int i=0; i<availableProvider.length; i++)
     {
       if ( availableProvider[i].getID() == _platformProviderId )
       {
         providerFound = true;
         PlatformProviderSelector.select(availableProvider[i]);
       }
     }
     Assert.assertTrue
       (
         "Platform '"+_platformProviderId+"' could not be found. Choose one of ["
         +PlatformProviderSelectorUtil.getAvailableProviderListDescription()+"]"
         ,
         providerFound
       );
     
     // 1.4 Select the Electronic Service Guide corresponding to my operator.
     //     In multiple operator deployments with a common broadcaster,
     //     several service guides may be available.
     //     In this case, the common broacaster is "Platform Provider"
     //     and each Telecom Operator is "Service Provider" 
     //     with its own associated Electronic Service Guide (ESG).
     ServiceGuide[] availableESG = ServiceGuide.getAllServiceGuides();
     _currentESG = null;
     for (int i=0; i<availableESG.length; i++)
     {
       if ( _serviceProviderName.equals( availableESG[i].getServiceProvider() )  )
       {
         _currentESG = availableESG[i];
       }
     }
     Assert.assertNotNull
       (
         "Service Provider '"+_serviceProviderName+"' could not be found. Choose one of ["
         +ServiceGuideUtil.getAvailableESGListDescription()+"]"
         ,
         _currentESG
       );
     
     // xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
     // 2. Acquire/Update ESG metadata
     // xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
     
     try
     {
       _currentESG.addListener(this, QueryComposer.currentProgram() );
     }
     catch(QueryException qe)
     {
       qe.printStackTrace();
       Assert.fail( "currentProgram() incorrectly rejected as invalid filter: "+qe.getMessage() );
     }
       
     // asynchronous call
     // completion events will be received through ServiceGuideListener interface
     _currentESG.forceUpdate();
   } 
   
   // </implements>
   
   // <implements interface="ServiceGuideListener">
   
   public void serviceGuideUpdated(String event, ServiceGuideData serviceGuideData, Object eventData)
   {
     _checkPoint[3] = true;
     _log.trace("Check Point 3");
     
     if ( SERVICE_GUIDE_UPDATE_COMPLETED.equals(event) )
     {
       _checkPoint[4] = true;
       _log.trace("Check Point 4");
       _currentESG.removeListener(this);
       updateCompleted();
     }
     else if( ServiceGuideListener.SERVICE_GUIDE_UPDATE_FAILED.equals(event) )
     {
       _currentESG.removeListener(this);
       Assert.fail("ESG Update failed. "+eventData);
     }
   }
   
   // </implements>
   
   protected void updateCompleted()
   {
     _checkPoint[5] = true;
     _log.trace("Check Point 5");
     
     // xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
     // 3. Query the ESG 
     // xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
     
     // 3.1 Query the ESG for the Channel List
     Service[] channel = null;
     try
     {
       channel = _currentESG.findServices(null);
     }
     catch(QueryException qe)
     {
       qe.printStackTrace();
       Assert.fail( "null incorrectly rejected as invalid query: "+qe.getMessage() );
     }
     
     Assert.assertTrue
       (
         "No channel description found in current ESG.",
         channel.length >= 1
       );
     
     // 3.2 Iterate over Channels to display the list of Channel Information
     _log.info("Channel List ("+channel.length+" channels)");
     for (int i=0; i<channel.length; i++)
     {
       String channelName = null;
       String description = null;
       
       // Note: ouch... it hurts to catch this DataUnavailableException for each fiedl
       //       just in case the data is not present (optional field...)
       // See: ServiceGuideData
       // "If the attribute is applicable but there is no data for the given attribute 
       // then the accessor method throws an (sic) DataUnavailableException."
       try
       {
         channelName = channel[i].getStringValue(CommonMetadataSet.SERVICE_NAME);
       }
       catch(DataUnavailableException due)
       {
         Assert.fail( "Service Name attribute on channel "+i+" has no value: "+due.getMessage() );
       }
       catch(Exception e)
       {
         Assert.fail("Unexpected exception: "+e.getMessage() );
       }
       
       
       try
       {
         description = channel[i].getStringValue(CommonMetadataSet.SERVICE_DESCRIPTION);
       }
       catch(DataUnavailableException due)
       {
         _log.warn( "Service Description attribute on channel "+i+" has no value: "+due.getMessage() );
       }
       
       _log.info(i+": "+channelName+" - "+description);
     }
 
     // 3.3 Query the ESG for current programs
     ProgramEvent[] currentProgram = null;
     try
     {
       currentProgram = _currentESG.findPrograms( QueryComposer.currentProgram() );
     }
     catch (QueryException qe)
     {
       qe.printStackTrace();
       Assert.fail( "currentProgram() incorrectly rejected as invalid query: "+qe.getMessage() );
     }
     
     Assert.assertTrue
       (
         "No program description found in current ESG.",
         currentProgram.length >= 1
       );
     
     // 3.4 Iterate over Programs to display the current program information on all channels
     _log.info("Current Programs ("+currentProgram.length+" programs)");
     for (int i=0; i<currentProgram.length; i++)
     {
       String channelName = null;
       Date startTime = null;
       Date endTime = null;
       String title = null;
       String synopsis = null;
       
       try
       {
         channelName = currentProgram[i].getStringValue(CommonMetadataSet.SERVICE_NAME);
       }
       catch(DataUnavailableException due)
       {
         Assert.fail( "Channel Name attribute on program "+i+" has no value: "+due.getMessage() );
       }
       
       try
       {
         startTime = currentProgram[i].getDateValue(CommonMetadataSet.PROGRAM_START_TIME);
       }
       catch(DataUnavailableException due)
       {
         Assert.fail( "Start Time attribute on program "+i+" has no value: "+due.getMessage() );
       }
       
       try
       {
         endTime = currentProgram[i].getDateValue(CommonMetadataSet.PROGRAM_END_TIME);
       }
       catch(DataUnavailableException due)
       {
         Assert.fail( "End Time attribute on program "+i+" has no value: "+due.getMessage() );
       }
       
       try
       {
         title = currentProgram[i].getStringValue(CommonMetadataSet.PROGRAM_NAME);
       }
       catch(DataUnavailableException due)
       {
         Assert.fail( "Program Title attribute on program "+i+" has no value: "+due.getMessage() );
       }
       
       try
       {
         synopsis = currentProgram[i].getStringValue(CommonMetadataSet.PROGRAM_DESCRIPTION);
       }
       catch(DataUnavailableException due)
       {
         _log.warn( "Program Synopsis attribute on program "+i+" has no value: "+due.getMessage() );
       }
       
       _log.info(i+": "+channelName+" ["+startTime+"-"+endTime+"] "+title+" - "+synopsis);
     }
     
     // 3.5 Select channel to watch, fixed here to the first TV service.
     //    An alternative would be to ask the user to choose which channel to watch ;)
     
     boolean tvServiceFound = false;
     for (int i=0; i<channel.length && !tvServiceFound; i++)
     {
       String channelTypeEnum = null;
       try
       {
         channelTypeEnum = channel[i].getStringValue(CommonMetadataSet.SERVICE_TYPE_NAME);
       }
       catch(DataUnavailableException due)
       {
         Assert.fail("Service Type Name attribute is not available on channel "+i+".");
       }
       
       if (  "tv".equals( channelTypeEnum )  )
       {
         _selectedChannel = channel[i];
         tvServiceFound = true;
       }
     }
     Assert.assertTrue
       (
         "No TV Channel found in ESG.",
         tvServiceFound
       );
     
     _log.info("Select TV channel: "+_selectedChannel);
     
     // xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
     // 4. Zap to selected channel
     // xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
     
     // 4.1 Initialize the Service Context for Zapping
     ServiceContext zappingContext = null;
     try
     {
       zappingContext = ServiceContext.getDefaultContext();
     }
     catch(Exception e)
     {
       Assert.fail( "Unexpected exception: "+e.getMessage() );
     }
     Assert.assertNotNull
       (
         "Default Service Context must not be null.",
         zappingContext
       );
     
     // Note: there is an issue here because getDefaultContext is asynchronous
     //       and the listener cannot be registered before getting the result,
     //       which means that there is a high risk of missing notification
     //       of CONTEXT_STOPPED... Could turn around this issue using a timeout...
     //       but there should be a better way to register the listener,
     //       e.g. as a parameter of getDefaultContext() / createServiceContext()
     zappingContext.addListener(this);
     
     // in case media was already playing, or CONTEXT_STOPPED was missed:
     if ( zappingContext.getState()!=ServiceContext.STOPPED )
       zappingContext.stop();
   }  
   
   // <implements interface="ServiceContextListener">
   
   public void contextUpdate(ServiceContext context, String event, Object data)
   {
     _checkPoint[6] = true;
     _log.trace("Check Point 6");
     
     if ( !context.equals( ServiceContext.getDefaultContext() ) )
       return;
     
     _checkPoint[7] = true;
     _log.trace("Check Point 7");
     
     if ( CONTEXT_STOPPED.equals(event) )
     {
       if (!_zappingContextInitialized)
       {
         _zappingContextInitialized = true;
         contextInstanceInitialized(context);
       }
       else
       {
         contextStopped(context);
       }
     }
     else if ( SELECTION_INITIATED.equals(event) )
     {
       serviceSelectionInitiated(context);
     }
     else if ( COMPONENTS_IDENTIFIED.equals(event) )
     {
       ServiceComponent[] defaultServiceComponent = (ServiceComponent[])data;
       defaultserviceComponentsSelected(context, defaultServiceComponent);
     }
     else if ( COMPONENTS_SET.equals(event) )
     {
       serviceComponentsSelectionCompleted(context);
     }
     else if ( PLAYERS_REALIZED.equals(event) )
     {
       playersInitialized(context);
     }
     else if ( PRESENTATION_STARTED.equals(event) )
     {
       playersStarted(context);
     }
     
     else if ( CONTEXT_CLOSED.equals(event) )
     {
       contextClosed(context);
     }
   }
   
   // </implements>
   
   protected void contextInstanceInitialized(ServiceContext context)
   {
     _checkPoint[8] = true;
     _log.trace("Check Point 8");
     
     try
     {
       // 4.2 Apply Channel Selection
       // asynchronous call
       context.select(_selectedChannel);
     }
     catch(Exception e)
     {
       Assert.fail("Unexpected Error: "+e.getMessage());
     }
     
   }
   
   protected void serviceSelectionInitiated(ServiceContext context)
   {
     _checkPoint[9] = true;
     _log.trace("Check Point 9");
   }
   
   protected void defaultserviceComponentsSelected(ServiceContext context, ServiceComponent[] component)
   {
     _checkPoint[10] = true;
     _log.trace("Check Point 10");
     
     boolean videoComponentFound = false;
     boolean audioComponentFound = false;
     for (int i=0; i<component.length; i++)
     {
       switch( component[i].getType() )
       {
         case ServiceComponent.VIDEO:
           videoComponentFound = true;
           break;
           
         case ServiceComponent.AUDIO:
           audioComponentFound = true;
           break;
       }
     }
     Assert.assertTrue
       (
         "No Video Component found for TV Channel.",
         videoComponentFound
       );
     Assert.assertTrue
       (
         "No Audio Component found for TV Channel.",
         audioComponentFound
       );
     
     // A good place to modify selected components
   }
   
   protected void serviceComponentsSelectionCompleted(ServiceContext context)
   {
     _checkPoint[11] = true;
     _log.trace("Check Point 11");
     
     // A good place to modify selected components
   }
   
   protected void playersInitialized(ServiceContext context)
   {
     _checkPoint[12] = true;
     _log.trace("Check Point 12");
   }
   
   protected void playersStarted(ServiceContext context)
   {
     _checkPoint[13] = true;
     _log.trace("Check Point 13");
     
     // xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
     // 5. End the test by stopping the zappingContext
     // xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
     // asynchronous call
     context.stop();
   }
   
   protected void contextStopped(ServiceContext context)
   {
     _checkPoint[14] = true;
     _log.trace("Check Point 14");
     
     // Test completed
     _log.info("Test Completed Successfully.");
   }
   
   protected void contextClosed(ServiceContext context)
   {
     // Default Instance cannot be closed
     Assert.fail("unexpected close of zapping context.");
     context.removeListener(this);
   }
}
