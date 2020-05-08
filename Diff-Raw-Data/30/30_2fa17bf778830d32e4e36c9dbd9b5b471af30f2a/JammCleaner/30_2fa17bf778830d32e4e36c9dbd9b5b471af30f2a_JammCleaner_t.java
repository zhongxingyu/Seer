 /*
  * Jamm
  * Copyright (C) 2002 Dave Dribin and Keith Garner
  *  
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  */
 
 package jamm.tools;
 
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.MissingOptionException;
import org.apache.commons.cli.MissingArgumentException;
 
 /**
  * This is the application that cleans up after jamm.
  */
 public final class JammCleaner
     implements JammCleanerDefines
 {
     /**
      * Print out the help message
      *
      * @param opt the options for this application
      */
     private static void printHelp(Options opt)
     {
         HelpFormatter hf = new HelpFormatter();
         hf.printHelp("Usage:  JammCleaner [options]" , opt);
     }
 
     /**
      * Loads up the options object
      *
      * @return a loaded Options object
      */
     private static Options getOptions()
     {
         Options opts = new Options();
         opts.addOption('v', "verbose", NO_ARGS, "verbose");
         opts.addOption('y', "yes", NO_ARGS, "Assume yes for all questions");
         opts.addOption('h', HAS_ARGS, "ldap host  (default: localhost)");
        opts.addOption('p', HAS_ARGS, "ldap port  (default: 389)");
         opts.addOption('w', "password", HAS_ARGS,
                        "password to connect to LDAP with");
         opts.addOption('n', "no-execute", NO_ARGS,
                        "Print would be executed, but do not execute.");
         opts.addOption('D', "dn", HAS_ARGS, "the dn to bind with",
                        IS_REQUIRED);
         opts.addOption('b', "base", HAS_ARGS, "base dn to search from",
                        IS_REQUIRED);
         opts.addOption('?', "help", NO_ARGS, "prints help message");
 
         return opts;
     }
 
     /**
      * Parse the command line and do what is needed.
      *
      * @param opts The options
      * @param cmd the command line/parsed options
      */
     private static void parseArgs(Options opts, CommandLine cmd)
     {
         if (cmd.hasOption('?'))
         {
             printHelp(opts);
             System.exit(0);
         }
         if (cmd.hasOption('v'))
         {
             JammCleanerOptions.setVerbose(true);
         }
         if (cmd.hasOption('y'))
         {
             JammCleanerOptions.setAssumeYes(true);
         }
         if (cmd.hasOption('h'))
         {
             JammCleanerOptions.setHost(cmd.getOptionValue('h', "localhost"));
         }
         if (cmd.hasOption('p'))
         {
             String value = cmd.getOptionValue('p', "389");
             JammCleanerOptions.setPort(Integer.parseInt(value));
         }
         if (cmd.hasOption('w'))
         {
             JammCleanerOptions.setPassword(cmd.getOptionValue('w'));
         }
         if (cmd.hasOption('D'))
         {
             JammCleanerOptions.setBindDn(cmd.getOptionValue('D'));
         }
         if (cmd.hasOption('b'))
         {
             JammCleanerOptions.setBaseDn(cmd.getOptionValue('b'));
         }
         if (cmd.hasOption('n'))
         {
             JammCleanerOptions.setNonDestructive(true);
         }
     }
     
     /**
      * Our main method
      *
      * @param argv command line arguments
      * @exception Exception if an error occurs
      */
     public static final void main(String argv[])
         throws Exception
     {
         Options opts = getOptions();
        CommandLine cmdl = null;
        try
        {
            cmdl = opts.parse(argv);
        }
        catch (MissingOptionException e)
        {
            System.out.println("Missing required options.");
            // System.out.println(e.toString());
            printHelp(opts);
            System.exit(1);
        }
        catch (MissingArgumentException e)
        {
            System.out.println("Missing required Arguments.");
            // System.out.println(e.toString());
            printHelp(opts);
            System.exit(1);
        }
        
         parseArgs(opts, cmdl);
         
        // System.out.println(JammCleanerOptions.argDump());
 
         AccountCleaner ac = new AccountCleaner();
         ac.cleanUp();
     }
 }
