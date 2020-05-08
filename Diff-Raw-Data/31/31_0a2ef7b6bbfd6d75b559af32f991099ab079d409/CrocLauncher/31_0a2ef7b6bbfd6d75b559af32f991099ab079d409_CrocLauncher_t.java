 /**
  * Bristlecone Test Tools for Databases
  * Copyright (C) 2011 Continuent Inc.
  * Contact: bristlecone@lists.forge.continuent.org
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of version 2 of the GNU General Public License as
  * published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA
  *
  * Initial developer(s): Robert Hodges
  * Contributor(s): Linas Virbalas
  */
 
 package com.continuent.bristlecone.croc;
 
 import org.apache.log4j.Logger;
 
 /**
  * Launcher for croc runs. This class parses command line arguments and invokes
  * croc.
  * 
  * @author <a href="mailto:robert.hodges@continuent.com">Robert Hodges</a>
  * @version 1.0
  */
 public class CrocLauncher
 {
     private static Logger logger = Logger.getLogger(CrocLauncher.class);
 
     /** Creates a new Benchmark instance. */
     public CrocLauncher()
     {
     }
 
     /** Main method to permit external invocation. */
     public static void main(String argv[]) throws Exception
     {
         String masterUrl = null;
         String masterUser = null;
         String masterPassword = null;
         String slaveUrl = null;
         String slaveUser = null;
         String slavePassword = null;
         String user = null;
         String password = null;
         String defaultSchema = null;
         boolean ddlReplication = true;
         boolean stageTables = false;
         boolean compare = true;
         int timeout = 60;
         String testList = null;
         boolean verbose = false;
 
         // Parse arguments.
         int argc = 0;
         while (argc < argv.length)
         {
             String nextArg = argv[argc];
             argc++;
 
             if ("-masterUrl".equals(nextArg))
             {
                 masterUrl = argv[argc++];
             }
             else if ("-masterUser".equals(nextArg))
             {
                 masterUser = argv[argc++];
             }
             else if ("-masterPassword".equals(nextArg))
             {
                 masterPassword = argv[argc++];
             }
             else if ("-slaveUrl".equals(nextArg))
             {
                 slaveUrl = argv[argc++];
             }
             else if ("-slaveUser".equals(nextArg))
             {
                 slaveUser = argv[argc++];
             }
             else if ("-slavePassword".equals(nextArg))
             {
                 slavePassword = argv[argc++];
             }
             else if ("-user".equals(nextArg))
             {
                 user = argv[argc++];
             }
             else if ("-password".equals(nextArg))
             {
                 password = argv[argc++];
             }
             else if ("-defaultSchema".equals(nextArg))
             {
                 defaultSchema = argv[argc++];
             }
             else if ("-ddlReplication".equals(nextArg))
             {
                 ddlReplication = Boolean.parseBoolean(argv[argc++]);
             }
             else if ("-stageTables".equals(nextArg))
             {
                 stageTables = true;
             }
             else if ("-compare".equals(nextArg))
             {
                 compare = Boolean.parseBoolean(argv[argc++]);
             }
             else if ("-timeout".equals(nextArg))
             {
                 timeout = Integer.parseInt(argv[argc++]);
             }
             else if ("-testList".equals(nextArg))
             {
                 testList = argv[argc++];
             }
             else if ("-verbose".equals(nextArg))
             {
                 verbose = true;
             }
             else if ("-help".equals(nextArg))
             {
                 usage();
                 return;
             }
             else
             {
                 String msg = "Unrecognized flag (try -help for usage): "
                         + nextArg;
                 println(msg);
                 exitWithFailure();
             }
         }
 
         // Run the test.
         try
         {
             logger.info("CROC - A REPLICATION TEST CROCODILE");
             logger.info("Initiating...");
             Croc croc = new Croc();
             croc.setMasterUrl(masterUrl);
             if (masterUser != null)
                 croc.setMasterUser(masterUser);
             if (masterPassword != null)
                 croc.setMasterPassword(masterPassword);
             croc.setSlaveUrl(slaveUrl);
             if (slaveUser != null)
                 croc.setSlaveUser(slaveUser);
             if (slavePassword != null)
                 croc.setSlavePassword(slavePassword);
             croc.setDdlReplication(ddlReplication);
             croc.setStageTables(stageTables);
             if (user != null)
                 croc.setUser(user);
             if (password != null)
                 croc.setPassword(password);
             if (defaultSchema != null)
                 croc.setDefaultSchema(defaultSchema);
             croc.setDdlReplication(ddlReplication);
             croc.setCompare(compare);
             croc.setTimeout(timeout);
             croc.setTestList(testList);
            croc.setVerbose(verbose);
 
             croc.run();
         }
         catch (CrocError e)
         {
             logger.fatal("ERROR: " + e.getMessage());
             if (verbose)
             {
                 e.printStackTrace();
             }
         }
         catch (Throwable t)
         {
             logger.fatal("Croc execution failed due to unexpected exception", t);
 
             // Catch and print the error that caused benchmark failure.
             println("Execution failed...See log for detailed stack trace(s)");
             println("EXCEPTION: " + t.getMessage());
 
             // Print out sub-exceptions as well.
             Throwable cause = t;
             while ((cause = cause.getCause()) != null)
             {
                 println("SUB-EXCEPTION: " + cause.getMessage());
             }
         }
     }
 
     /** Print to standard out. */
     protected static void println(String message)
     {
         System.out.println(message);
     }
 
     /** Print usage. */
     protected static void usage()
     {
         println("CROCODILE REPLICATOR TEST PROGRAM (\"croc\")");
         println("Usage: croc options");
         println("Options:");
         println("  -compare {true|false}         If true, compare tables (default=true)");
         println("  -ddlReplication {true|false}  If true, DDL replicates (default=true)");
         println("  -stageTables                  Create staging tables for test tables");
         println("  -masterPassword password      Master db password");
         println("  -masterUrl url                Master db url");
         println("  -masterUser user              Master db user");
         println("  -password pw                  Db password (overrides master and slave)");
         println("  -slavePassword password       Slave db password");
         println("  -slaveUrl url                 Slave db url");
         println("  -slaveUser user               Slave db user");
         println("  -testList file                File containing list of tests");
         println("  -timeout secs                 Time out to wait for replication (default=60)");
         println("  -user user                    Db user (overrides master and slave)");
         println("  -defaultSchema schema         Default schema name (required for PostgreSQL)");
         println("  -verbose                      Print verbose error output");
         println("  -help                         Print usage and exit");
         println("Notes:");
         println("  Test list is a set of croc Loader class names, one per line");
 
     }
 
     // Fail gloriously.
     protected static void exitWithFailure()
     {
         System.exit(1);
     }
 
     // Exit with a success code.
     protected static void exitWithSuccess1()
     {
         System.exit(0);
     }
 }
