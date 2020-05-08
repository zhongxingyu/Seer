 /*
  * Copyright (C) 2009  Lars Pötter <Lars_Poetter@gmx.de>
  * All Rights Reserved.
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License version 2
  * as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License along
  * with this program; if not, see <http://www.gnu.org/licenses/>
  *
  */
 
 package org.FriendsUnited.NetworkLayer;
 
 import org.FriendsUnited.FriendServer.FriendServerFactory;
 import org.FriendsUnited.Util.Option.BooleanOption;
 import org.FriendsUnited.Util.Option.OptionCollection;
 import org.FriendsUnited.Util.Option.StringOption;
 import org.FriendsUnited.Util.time.TimeOut;
 import org.apache.log4j.Logger;
 
 /**
  *
  * @author Lars P&ouml;tter
  * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
  */
 public class Supervisor extends Thread
 {
     private final Logger log = Logger.getLogger(this.getClass().getName());
     private final OptionCollection cfg;
     private final OptionCollection status;
     private final OptionCollection servers;
     private final OptionCollection vroot;
 
     private volatile BooleanOption shouldRun;
     private final NodeId OwnNodeId;
     private NodeDirectory NodeDir;
     private Router MyRouter;
     private PacketTransmitterFactory prf;
     private FriendServerFactory fsf;
     private ControlInterfaceFactory cif;
     private TimeOut timeout;
 
     /**
      * @author Lars P&ouml;tter
      * (<a href=mailto:Lars_Poetter@gmx.de>Lars_Poetter@gmx.de</a>)
      */
     public class RunWhenShuttingDown extends Thread
     {
         public final void run()
         {
             System.out.println("Shutting down...");
             shouldRun.newValue(false);
             try
             {
                 Thread.sleep(3000);
             }
             catch (final InterruptedException e)
             {
                 e.printStackTrace();
             }
         }
     }
 
     /**
      *
      */
     public Supervisor(final OptionCollection OptCol)
     {
         cfg = OptCol;
         status = new OptionCollection("status", cfg.getCfgRoot());
         servers = new OptionCollection("servers", cfg.getCfgRoot());
        vroot =  new OptionCollection("root", cfg.getCfgRoot());
         vroot.addSubSection(cfg);
         vroot.addSubSection(status);
         vroot.addSubSection(servers);
         setName("Supervisor");
         shouldRun = new BooleanOption("shouldRun", true);
         status.add(shouldRun);
         // To save the changed configuration
         Runtime.getRuntime().addShutdownHook(new RunWhenShuttingDown());
         // 1. Initialization
         // Own NodeID - could be generated based on Mac IP and Time
         //              as it changes with every startup
         OwnNodeId = new NodeId();
         log.info("Own Node ID : " + OwnNodeId.toString());
         final StringOption NodeIdOption = new StringOption("OwnNodeId", OwnNodeId.toString());
         status.add(NodeIdOption);
 
         // Get accepted Certificates
 
         // Create own Certificate / PrivateKey / Public Key
 
     }
 
     /**
      *
      */
     @Override
     public final void run()
     {
         // AnnouncementManager
         final AnnouncementManager AnMng = new AnnouncementManager();
 
         // TimeOut Machine
         timeout = new TimeOut();
         timeout.start();
 
         // Node Directory
         NodeDir = new NodeDirectory(
                 cfg.getSubSectionCreateIfAbsent("NodeDirectory"),
                 status.getSubSectionCreateIfAbsent("NodeDirectory"));
 
         // Router
         MyRouter = new Router(
                 cfg.getSubSectionCreateIfAbsent("Router"),
                 NodeDir,
                 OwnNodeId,
                 status.getSubSectionCreateIfAbsent("Router"),
                 AnMng);
         NodeDir.addRouter(MyRouter);
 
         // Packet Receiver
         prf = new PacketTransmitterFactory(
                 cfg.getSubSectionCreateIfAbsent("PacketReciever"),
                 NodeDir,
                 OwnNodeId,
                 MyRouter,
                 status.getSubSectionCreateIfAbsent("PacketReciever"),
                 AnMng);
         prf.produceAndActivate();
 
         // FriendServers
         fsf = new FriendServerFactory(
                 cfg.getSubSectionCreateIfAbsent("FriendServers"),
                 MyRouter,
                 status.getSubSectionCreateIfAbsent("FriendServers"),
                 servers,
                 timeout);
         MyRouter.addOwnServerFactory(fsf);
         MyRouter.start();
         fsf.produceAndActivate();
 
         // Control Interface
         cif = new ControlInterfaceFactory(
                 cfg.getSubSectionCreateIfAbsent("ControlInterface"),
                 vroot);
         cif.produceAndActivate();
 
         // End
         log.info("Startup Complete");
     // 2. Run
         while(true == shouldRun.getValue())
         {
         // LifeSign supervision - periodic Manager ?
             try
             {
                 Thread.sleep(100);
                 cif.checkIfWorkersAreStillAlive();
                 prf.checkIfWorkersAreStillAlive();
             }
             catch (final InterruptedException e)
             {
             }
         }
         log.info("Beginning shutdown");
     // 3. Shut Down
 
         // Control Interface
         cif.stopAndDestroy();
         // Packet Receiver
         // prf.stopAndDestroy();
         // FriendServers
         fsf.stopAndDestroy();
         // Timeout
         timeout.shutdown();
         // Router - nothing to do
         MyRouter.close();
         // Node Directory
         NodeDir.close();
         // Configuration
         cfg.save("lastUsedConfiguation.xml");
         // Status
         status.save("lastStatus.xml");
         // Servers
         servers.save("lastServers.xml");
         // Log4J
         log.info("Done!");
         // Server terminates
         System.exit(0);
     }
 }
