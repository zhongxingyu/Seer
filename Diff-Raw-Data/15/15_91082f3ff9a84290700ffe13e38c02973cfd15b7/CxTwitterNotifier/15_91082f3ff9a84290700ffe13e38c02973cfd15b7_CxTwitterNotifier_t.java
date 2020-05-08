 //
 package com.deloitte.acs;
 //
 import java.io.*;
 //
 import jetbrains.buildServer.serverSide.BuildServerAdapter;
 import jetbrains.buildServer.serverSide.SBuildServer;
 import jetbrains.buildServer.serverSide.SRunningBuild;
 import jetbrains.buildServer.messages.Status;
 //
 import com.intellij.openapi.diagnostic.Logger;
 //
 import twitter4j.Twitter;
 import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
 
 
 // Tweets build start / finish to the build account 
 public class CxTwitterNotifier extends BuildServerAdapter 
 {
    private final static Logger LOG = Logger.getInstance(CxTwitterNotifier.class.getName());
    
    
    // Constructor, register the listener ( this )
    public CxTwitterNotifier(SBuildServer aBuildServer) 
    {
        System.out.println( "*** Notifier adding listener=" + this );
        LOG.info("*** Notifierr adding listener=" + this);
 
        // Register with TC 
        aBuildServer.addListener(this);
    }
 
    
    // Build started, set the version number
    public void buildStarted(SRunningBuild build) 
    {
        String sTcBuildNumber = build.getBuildNumber();
        String sFullName = build.getFullName();
        System.out.println( "*** TwitterNotifier plugin, buildStarted, getting build number, default value=" +  sTcBuildNumber );
        LOG.info("*** TwitterNotifier plugin, buildStarted, getting build number, default value=" + sTcBuildNumber );
        //	
        sendTweet( "Build " + sFullName + ", " + sTcBuildNumber + " started on " + build.getAgentName() );
    }
 
    public void buildChangedStatus(SRunningBuild build, Status oldStatus, Status newStatus)
    {
 	   String sTcBuildNumber = build.getBuildNumber();
 	   String sFullName = build.getFullName();
        System.out.println( "*** TwitterNotifier plugin, buildChangedStatus");
        LOG.info( "*** TwitterNotifier plugin, buildChangedStatus" );
        //
        sendTweet( "Build " + sFullName + ", " + sTcBuildNumber + " status, " + newStatus.getText() );
    }
    
    public void buildFinished(SRunningBuild build)
    {
 	   String sTcBuildNumber = build.getBuildNumber();
 	   String sFullName = build.getFullName();
        System.out.println( "*** TwitterNotifier plugin, buildFinished");
        LOG.info( "*** TwitterNotifier plugin, buildFinished" );
        //
        sendTweet( "Build " + sFullName + ", " + sTcBuildNumber + " finished." );	   
    }
    
    // Updates the twitter account 
    public static void sendTweet( String psText )
    {
 	   try
 	   {
 			// Configuration 
 			ConfigurationBuilder confbuilder = new ConfigurationBuilder();
 			confbuilder.setOAuthAccessToken(CxGlobal.ACCESS_TOKEN_KEY)
 			.setOAuthAccessTokenSecret(CxGlobal.ACCESS_TOKEN_SECRET)
 			.setOAuthConsumerKey(CxGlobal.CONSUMER_KEY)
 			.setOAuthConsumerSecret(CxGlobal.CONSUMER_SECRET);
 						
 			// get the interaction object
 			Twitter twitter = new TwitterFactory(confbuilder.build()).getInstance();
 			// send 
 			twitter4j.Status status = twitter.updateStatus( psText );
 			//
 			//System.out.println("*** Response=" + status.getText() );
 	   }
 	   catch( Exception ex )
 	   {
 	       System.out.println( "***ERROR=" + stack2string( ex ));
 	       LOG.info( "***ERROR=" + stack2string( ex ) );
 	   }
    }
 
    public static String stack2string(Exception e) 
    {
       try 
       {
         StringWriter sw = new StringWriter();
         PrintWriter pw = new PrintWriter(sw);
         e.printStackTrace(pw);
         return "------\r\n" + sw.toString() + "------\r\n";
       }
       catch(Exception e2) 
       {
         return "bad stack2string";
       }
    }
 
 }  // EOC
