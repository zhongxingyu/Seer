 // Copyright (C) 2012 Carles Muiños
 //
 // This file is part of TSAEnode.
 //
 // TSAEnode is free software: you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation, either version 3 of the License, or
 // (at your option) any later version.
 //
 // TSAEnode is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU General Public License for more details.
 //
 // You should have received a copy of the GNU General Public License
 // along with TSAEnode.  If not, see <http://www.gnu.org/licenses/>.
 
 package org.coderebels.tsaenode;
 
 import java.io.File;
 import java.io.Reader;
 import java.io.StringWriter;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.List;
 import java.util.Map;
 import java.util.Timer;
 
 import org.apache.logging.log4j.Logger;
 import org.apache.logging.log4j.LogManager;
 
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.Velocity;
 
 import org.coderebels.tsaenode.core.INode;
 import org.coderebels.tsaenode.core.TSAEnode;
 import org.coderebels.tsaenode.core.sync.Peer;
 
 
 /**
  * @author carles.ml.dev@gmail.com (Carles Muiños)
  */
 public class Main {
 
   private static Logger logger = LogManager.getLogger( Main.class.getName() );
 
   
   public static void main(String[] args) {
     new Main().start();
   }
 
 
   private Timer scheduler;
   private Simulation simulation;
 
 
  public Main() {
    scheduler = new Timer();
  }
 
 
   public void start() {
     // Register Node
     INode node = registerNode();
 
     // Read, Evaluate and Print
     repl( node );
 
     // Unregister Node and Exit
     unregisterNode( node );
   }
   
 
   private INode registerNode() {
     INode node = null;
 
     try {
       node = new TSAEnode();
       boolean done = node.connect();
 
       if (!done) {
         System.out.println( "An error occurred while registering the node." );
         System.exit( -1 );
       }
     } catch (Exception e) {
       logger.catching( e );
       System.out.println( "An error occurred while registering the node." );
       System.exit( -1 );
     }
 
     return node;
   }
 
   private void unregisterNode(INode node) {
     try {
       boolean done = node.disconnect();
 
       if (done) {
         System.out.println( "Bye bye!" );
         System.exit( 0 );
       } else {
         System.out.println( "An error occurred while unregistering the node." );
         System.exit( -1 );
       }
     } catch (Exception e) {
       logger.catching( e );
       System.out.println( "An error occurred while unregistering the node." );
       System.exit( -1 );
     }
   }
 
   private void repl(INode node) {
     boolean exit = false;
     int action = -1;
 
     try {
       StringWriter writer = prepareOptions();
 
       while (!exit) {
         printOptions( writer );
         action = readAction();
         exit = evaluateAction( action, node );
       }
     } catch (Exception e) {
       logger.catching( e );
       System.out.println( "An error occurred while interacting with the node." );
     }
   }
 
   private StringWriter prepareOptions() throws Exception {
     VelocityContext context = new VelocityContext();
     StringWriter writer = prepareTemplate( "options.vm", context );
 
     return writer;
   }
 
   private StringWriter prepareTemplate(String template, VelocityContext context) throws Exception {
     String templatePath = "templates" + File.separator + template;
     
     Reader reader = new InputStreamReader( getClass().getClassLoader().getResourceAsStream(templatePath) );
     StringWriter writer = new StringWriter();
     Velocity.evaluate( context, writer, "", reader );
 
     return writer;
   }
 
   private void printOptions(StringWriter writer) {
     System.out.println( writer.toString() );
   }
 
   private int readAction() throws Exception {
     int action = -1;
 
     try {
       action = Integer.parseInt( read() );
     } catch (Exception e) {}
 
     return action;
   }
 
   private String read() throws Exception {
     BufferedReader reader = new BufferedReader( new InputStreamReader(System.in) );
     return reader.readLine();
   }
 
   private String readFilename() throws Exception {
     System.out.println("Enter the absolute path of the file:");
     return read();
   }
 
   private boolean evaluateAction(int action, INode node) throws Exception {
     boolean exit = false;
 
     switch (action) {
       case 0: showProfile( node );
         break;
 
       case 1: addFile( node );
         break;
 
       case 2: removeFile( node );
         break;
 
       case 3: showFileIndex( node );
         break;
 
       case 4: showLog( node );
         break;
 
       case 5: showSummary( node );
         break;
 
       case 6: showAckSummary( node );
         break;
 
       case 7: runSyncSession( node );
         break;
 
       case 8: runSimulation( node );
         break;
 
       case 9: stopSimulation( node );
         break;
 
       case 10:
        scheduler.cancel();
         exit = true;
         break;
 
       default: System.out.println( "Unknown action. Try again." );
         break;
     }
 
     return exit;
   }
 
   private void showProfile(INode node) throws Exception {
     Peer profile = node.requestProfile();
 
     VelocityContext context = new VelocityContext();
     context.put( "profile", profile );
     StringWriter writer = prepareTemplate( "profile.vm", context );
 
     System.out.println( writer.toString() );
   }
 
   private void addFile(INode node) throws Exception {
     String file  = readFilename();
     boolean done = node.add( file );
 
     if (done) {
       System.out.println( "File added succesfully." );
     } else {
       System.out.println( "An error occurred while adding the file." );
     }
   }
 
   private void removeFile(INode node) throws Exception {
     String file  = readFilename();
     boolean done = node.remove( file );
 
     if (done) {
       System.out.println( "File removed succesfully." );
     } else {
       System.out.println( "An error occurred while removing the file." );
     }
   }
 
   private void showFileIndex(INode node) throws Exception {
     List files = node.requestFileIndex();
 
     VelocityContext context = new VelocityContext();
     context.put( "files", files );
     StringWriter writer = prepareTemplate( "fileIndex.vm", context );
 
     System.out.println( writer.toString() );
   }
 
   private void showLog(INode node) throws Exception {
     List ops = node.requestLog();
 
     VelocityContext context = new VelocityContext();
     context.put( "ops", ops );
     StringWriter writer = prepareTemplate( "log.vm", context );
 
     System.out.println( writer.toString() );
   }
 
   private void showSummary(INode node) throws Exception {
     Map summary = node.requestSummary();
 
     VelocityContext context = new VelocityContext();
     context.put( "summary", summary );
     StringWriter writer = prepareTemplate( "summary.vm", context );
 
     System.out.println( writer.toString() );
   }
 
   private void showAckSummary(INode node) throws Exception {
     Map ackSummary = node.requestAckSummary();
 
     VelocityContext context = new VelocityContext();
     context.put( "ackSummary", ackSummary );
     StringWriter writer = prepareTemplate( "ackSummary.vm", context );
 
     System.out.println( writer.toString() );
   }
 
   private void runSyncSession(INode node) throws Exception {
     boolean done = node.startTSAESession();
 
     if (done) {
       System.out.println( "Synchronization run succesfully." );
     } else {
       System.out.println( "An error occurred while running synchronization session." );
     }
   }
 
   private void runSimulation(INode node) throws Exception {
     if (simulation == null) {
       simulation = new Simulation( node );
       long delay  = 0;
       long period = 10000;
   
       scheduler.scheduleAtFixedRate( simulation, delay, period );
 
       System.out.println( "Starting simulation..." );
     } else {
       System.out.println( "Simulation is already running." );
     }
   }
 
   private void stopSimulation(INode node) throws Exception {
     if (simulation != null) {
       scheduler.cancel();
 
       VelocityContext context = new VelocityContext();
       context.put( "simulation", simulation );
       StringWriter writer = prepareTemplate( "simulation.vm", context );
 
       System.out.println( writer.toString() );
 
       simulation = null;
       if (!node.isConnected()) node.connect();
     } else {
       System.out.println( "No simulation running at the moment." );
     }
   }
 
 }
