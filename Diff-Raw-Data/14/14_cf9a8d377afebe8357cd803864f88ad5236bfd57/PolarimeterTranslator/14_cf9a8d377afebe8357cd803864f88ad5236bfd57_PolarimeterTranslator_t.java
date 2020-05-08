 /*-
  * Copyright © 2009 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.jython.translator;
 
 import gda.factory.Finder;
 import gda.jython.JythonServer;
 import gda.jython.JythonServerFacade;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * A class to provide a translation facility for Polarimeter scripting syntax.
  * <p>
  * Anything entered by the user (command line or script) should be passed
  * through the translate method. If the syntax is recognised, it is converted
  * into 'real' jython so it may be passed to a Jython interpreter. Anything
  * unrecognised, or if an error occurs, then the string is returned untouched.
  */
 
 public class PolarimeterTranslator extends TranslatorBase
    implements Translator
 {
 	private static final Logger logger=LoggerFactory.getLogger(PolarimeterTranslator.class);
 	 
 	@Override
 	public String getHelpMessage() 
 	{
 		String helpString = new String("");
 
 	      helpString +=
 	         "Available console commands in addition to Jython syntax:\\n";
 	      helpString += "help\\t-\\tprints this message\\n";
 	      helpString +=
 	         "pos pseudoDevice\t-\tgets the current position of the object\\n";
 	      helpString +=
 	         "pos pseudoDevice new_position\t-\tmoves the object to the new position\\n";
 	      helpString +=
 	         "inc pseudoDevice increment\t-\tincremental move by the given amount\\n";
 	      helpString += "list\t-\tlist all the interfaces of existing objects\\n";
 	      helpString +=
 	         "list interfaceName\t-\tlists all the objects of the given type (interface).  E.g. OE, Detector\\n";
 	      helpString +=
 	         "list defaults\t-\tlists all objects which would be used in a scan by default\\n";
 	      helpString +=
 	         "add default pseudoDevice\t-\tadd an object to the defaults list\\n";
 	      helpString +=
 	         "remove default pseudoDevice\t-\tremove an object from the defaults list\\n";
 	      helpString +=
 	         "scan axis start stop step time [f] [[pinhole size] step]\t-\tscan of a single axis with data collected after each step, flux monitoring optional\\n";
 	      helpString +=
 	         "pscan innerAxis start stop step time outerAxis start stop step [f] [[pinhole size]\t-\ta 2 dimensional scan with optional flux monitoring\\n";
           helpString +=
 	         "tscan pseudoDevice [scannablename2] timeInterval [numberOfPoints]\t-\tTime scan in which the positions of a list of pseudoDevices are collected at regular intervals. If no numberOfPoints supplied then the scan will not stop until halted.\\n";
 	      helpString +=
 	         "pause\t-\tduring a script, checks to see if the pause/resume button has been pressed\\n";
 	      helpString +=
 	         "watch pseudoDevice\t-\tadds the scannable to the watch sub-panel in the terminal panel.\\n";
 	      helpString +=
 	         "level pseudoDevice [value]\t-\tif value is declared then sets move order (level) of the scannable, else returns the current level.\\n";
 	      helpString +=
 	         "add functionName\t-\twhere functionName is a function in the global namespace.  This dynamically adds a function to the extended syntax.\\n";
 	      helpString += "run scriptName\t-\truns the named script.\\n";
 	      helpString +=
 	         "record [on|off] \t-\tstarts/stops recording all terminal output to a file placed in the scripts directory\\n";
 
 	      return helpString;
 	}
 
 	/**
 	    * Translates a command identified by the translate() method. This ignores
 	    * any spaces at start or end of line - it assumes that translate() has
 	    * removed any significant \n or \t characters.
 	    * 
 	    * @param thisGroup String
 	    * @return String
 	    */
 	@Override
 	public String translateGroup(String thisGroup)
 	   {
 	      String originalGroup = thisGroup;
 	      try
 	      {
 	         // then remove all leading tabs and trailing spaces
 	         // thisGroup = thisGroup.trim();
 
 	         // tidy up []'s
 	         thisGroup = GeneralTranslator.tidyBrackets(thisGroup);
 
 	         // split rest of line by space
 	         String[] args = GeneralTranslator.splitGroup(thisGroup);
 	         // if nothing in the string then return
 	         if (args.length == 0)
 	         {
 	            return thisGroup;
 	         }
 
 	         // assume first element in 'args' is the method, second is the name of
 	         // the
 	         // object and rest are the arguments of the method
 
 	         // test() for mv
 	         if (args[0].startsWith("#"))
 	         {
 	            return thisGroup;
 	         }
 	         else if (args[0].compareTo("pos") == 0 && args.length > 2)
 	         {
 	            if (args.length > 3)
 	            {
 	               // this command moves several motors simultaneously
 	               // it assumes that calls is made in the format:
 	               // pos obj1 amount1 obj2 amount2 etc.
 	               // to do this, write a small script and run it separately.
 	               thisGroup = "";
 	               String postfix = ""; // to add on at the end
 	               String postpostfix = ""; // another to add on at the end
 	               // loop through further args, jumping two at a time
 	               for (int i = 1; i < args.length - 1; i += 2)
 	               {
 	                  thisGroup +=
 	                     args[i] + ".asynchronousMoveTo(" + args[i + 1] + ");";
 	                  // the postfix will make the interpreter pause until all
 	                  // the movements have been completed
 	                  postfix += "ScannableBase.waitForScannable(" + args[i] + ");";
 	                  postpostfix +=
 	                     "print \"" + args[i] + " now at: \"," + args[i]
 	                        + ".getPosition();";
 	               }
 	               thisGroup += postfix + postpostfix + "\n";
 	            }
 	            // else simply use the moveTo() method
 	            else
 	            {
 	               thisGroup = args[1] + ".asynchronousMoveTo(" + args[2] + ");";
 	               thisGroup += "ScannableBase.waitForScannable(" + args[1] + ");";
 	               thisGroup +=
 	                  "print \"" + args[1] + " now at: \"," + args[1]
 	                     + ".getPosition()";
 	            }
 	         }
 	         // test() for inc
 	         else if (args[0].compareTo("inc") == 0)
 	         {
 	            thisGroup = args[1] + ".moveBy(" + args[2] + ");";
 	            thisGroup +=
 	               "print \"" + args[1] + " now at: \"," + args[1]
 	                  + ".getPosition()";
 	         }
 	         // test for pos
 	         else if (args[0].compareTo("pos") == 0 && args.length == 2)
 	         {
 	            thisGroup = "print " + args[1] + ".toString()";
 	         }
 	         // test for basic scan
 	         else if (args[0].toLowerCase().compareTo("scan") == 0)
 	         {
 	            thisGroup = translateScan(thisGroup);
 	         }
 	         // test for Stokes scan
 	         else if (args[0].toLowerCase().compareTo("pscan") == 0)
 	         {
 	            thisGroup = translateStokesScan(thisGroup);
 	         }
 	         //test for time scan
 	         else if (args[0].toLowerCase().compareTo("tscan") == 0)
 	         {
 	            thisGroup = translateTScan(thisGroup);
 	         }
 	         //	 test for concurrent scan
 	         else if (args[0].toLowerCase().compareTo("cscan") == 0)
 	         {
 	            thisGroup = translateConcurrentScan(thisGroup);
 	         }
 	         // test for list
 	         else if (args.length > 1
 	            && args[0].toLowerCase().compareTo("list") == 0
 	            && args[1].toLowerCase().compareTo("defaults") == 0)
 	         {
 	            thisGroup =
 	               "print Finder.getInstance().find(JythonServer.SERVERNAME).getDefaultScannables()";
 	         }
 	         // test for list
 	         else if (args[0].toLowerCase().compareTo("add") == 0
 	            && args[1].toLowerCase().compareTo("default") == 0)
 	         {
 	            thisGroup =
 	               "Finder.getInstance().find(JythonServer.SERVERNAME).addDefault("
 	                  + args[2] + ")";
 	         }
 	         // test for list
 	         else if (args[0].toLowerCase().compareTo("remove") == 0
 	            && args[1].toLowerCase().compareTo("default") == 0)
 	         {
 	            thisGroup =
 	               "Finder.getInstance().find(JythonServer.SERVERNAME).removeDefault("
 	                  + args[2] + ")";
 	         }
 	         // test for list
 	         else if (args[0].toLowerCase().compareTo("list") == 0)
 	         {
 	            if (args.length == 1)
 	            {
 	               // list all interfaces, removing interfaces which would be
 	               // confusing to users in the output
 	               thisGroup =
 	                  "availableInterfaces=finder.listAllInterfaces();"
 	                     + "None=availableInterfaces.remove(\"Findable\");"
 	                     + "None=availableInterfaces.remove(\"MonitorListener\");"
 	                     + "None=availableInterfaces.remove(\"PutListener\");"
 	                     + "None=availableInterfaces.remove(\"GetListener\");"
 	                     + "None=availableInterfaces.remove(\"ConnectionListener\");"
 	                     + "None=availableInterfaces.remove(\"Serializable\");"
 	                     + "None=availableInterfaces.remove(\"Jython\");"
 	                     + "None=availableInterfaces.remove(\"Configurable\");"
 	                     + "None=availableInterfaces.remove(\"Localizable\");"
 	                     + "None=availableInterfaces.remove(\"JythonServer\");"
 	                     + "None=availableInterfaces.remove(\"Runnable\");"
 	                     + "None=availableInterfaces.remove(\"IObservable\");"
 	                     + "None=availableInterfaces.remove(\"Motor\");"
 	                     + "None=availableInterfaces.remove(\"IObserver\");"
 	                     + "None=availableInterfaces.remove(\"Scriptcontroller\");"
 	                     + "None=availableInterfaces.remove(\"Device\");"
 	                     + "None=availableInterfaces.remove(\"Finisher\");"
 	                     + "None=availableInterfaces.remove(\"Closeable\");"
 	                     + "None=availableInterfaces.remove(\"Flushable\");"
 	                     + "None=availableInterfaces.remove(\"RobotListener\");"
 	                     + "None=availableInterfaces.remove(\"UsesProperties\");"
 	                     + "None=availableInterfaces.remove(\"Observer\");"
 	                     + "ArrayListPrinter(availableInterfaces)";
 	            }
 	            // change all of the above if user has asked about scannables
 	            else if (args[1].compareTo("Scannable") == 0
 	               || args[1].compareTo("Scannables") == 0)
 	            {
 	               thisGroup =
 	                  "for ii in dir(): \n\tif isinstance(eval(ii),Scannable):\n\t\t print eval(ii).toString()\n\n";
 	            }
 	            else if (Finder.getInstance().listAllNames(args[1]).size() == 0)
 	            {
 	               thisGroup =
 	                  "for i in range(" + args[1]
 	                     + ".getDOFNames().__len__()):\n\tprint " + args[1]
 	                     + ".getDOFNames()[i]\n\n";
 	            }
 	            else if (args[1].compareTo("all") == 0)
 	            {
 	               thisGroup =
 	                  "ArrayListPrinter(finder.listAllNames(\"Findable\"))";
 	            }
 	            else
 	            {
 	               thisGroup =
 	                  "ArrayListPrinter(finder.listAllNames(\"" + args[1] + "\"))";
 	            }
 	         }
 	         // 'pos' on its own
 	         else if (args[0].compareTo("pos") == 0 && args.length == 1)
 	         {
 	            thisGroup =
 	               "for ii in dir(): \n\tif isinstance(eval(ii),Scannable):\n\t\tif not isinstance(eval(ii),OEAdapter):\n\t\t\tprint eval(ii).toString()\n\n";
 	         }
 	         // print out a list of available commands
 	         else if (args[0].toLowerCase().compareTo("help") == 0
 	            && args.length == 1)
 	         {
 	            thisGroup = "print\"" + this.getHelpMessage() + "\"";
 	         }
 	         else if (args[0].toLowerCase().compareTo("help") == 0
 	            && args.length == 2)
 	         {
 	            thisGroup = "_gdahelp(" + args[1] + ")";
 	         }
 	         // if in a script, see if a pause has been requested
 	         else if (args[0].toLowerCase().compareTo("pause") == 0)
 	         {
 	            thisGroup = "ScriptBase.checkForPauses()";
 	         }
 
 	         // change the level of a scannable
 	         else if (args[0].toLowerCase().compareTo("level") == 0)
 	         {
 	            if (args.length > 2)
 	            {
 	               thisGroup = args[1].toLowerCase() + ".setLevel(" + args[2] + ")";
 	            }
 	            else
 	            {
 	               thisGroup = args[1].toLowerCase() + ".getLevel()";
 	            }
 	         }
 	         // restart the JythonInterpreter - cleans up the namespace
 	         else if (args[0].toLowerCase().compareTo("reset") == 0
 	            && args[1].toLowerCase().compareTo("namespace") == 0)
 	         {
 	            JythonServer server =
 	               (JythonServer) Finder.getInstance().find(JythonServer.SERVERNAME);
 	            if (server != null)
 	               server.restart();
 	            thisGroup = "";
 	         }
 	         // dynamically add a function to the extended syntax
 	         else if (args[0].compareTo("alias") == 0)
 	         {
 	            // assume next arg is the name of the function
 	            if (args.length == 2)
 	            {
 	               aliases.add(args[1]);
 	               thisGroup = "";
 	            }
 	         }
 	         // prevent overwriting of scannables
 	         else if (thisGroup.contains("="))
 	         {
 	            // extract the string we are interested in
 	            String leftOfOperator =
 	               thisGroup.substring(0, thisGroup.indexOf("="));
 	            String test = "'" + leftOfOperator.trim() + "'";
 
 	            // get the Jython server and test if our object is in the namespace
 	            JythonServerFacade server = JythonServerFacade.getInstance();
 	            String eval = server.evaluateCommand("dir()");
 	            if (eval.contains(test))
 	            {
 	               // if it is, is it a Scannable?
 	               String result =
 	                  server.evaluateCommand("isinstance(" + leftOfOperator
 	                     + " ,Scannable)");
 
 	               // if so, the raise an error rather than completing the code
 	               if (result.compareTo("1") == 0)
 	               {
 	                  thisGroup =
 	                     "raise Exception(\"Trying to overwrite: "
 	                        + leftOfOperator.trim() + "\")";
 	               }
 	            }
 	            // else do nothing
 	         }
 	         // run a script
 	         else if (args[0].compareTo("run") == 0 && args.length == 2)
 	         {
 	            JythonServer jythonServer = (JythonServer)Finder.getInstance().find(JythonServer.SERVERNAME);
 	            String fullpath = jythonServer.getJythonScriptPaths().pathToScript(args[1]);
 	            // if the script didn't exist, there'll just be a syntax error here.
 	            thisGroup = "finder.find(JythonServer.SERVERNAME).runCommandSynchronously(\""
 	                        + fullpath + "\")";
 	         }
 
 	         // else test for a dynamically added command
 	         else
 	         {
 	            if (aliases.contains(args[0]) && args.length > 1)
 	            {
 	               thisGroup = args[0] + "(";
 
 	               for (int i = 1; i < args.length; i++)
 	               {
 	                  thisGroup += args[i];
 	                  if (i < args.length - 1)
 	                     thisGroup += ",";
 	               }
 	               thisGroup += ")";
 	            }
 	         }
 	      }
 	      catch (Exception e)
 	      {
 	         // if an error, return the original string
 	         // and let jython interpreter handle any syntax errors
 	         logger.debug("Error in Translator: {}", originalGroup);
 	         return originalGroup;
 	      }
 	      return thisGroup;
 	   }
 	
 	
 	   /*
 		 * Handles Stokes paramter type scanes with two nested scans
 		 * Assumes inner scan comes first with  start stop step and time parameters
 		 * floowed by the inner scan which contains start stop and step fields
 		 * followed by an optional "f" and flux monitor pinhole size fields  
 		 * 
 		 */
 		   private String translateStokesScan(String thisGroup) 
 		   {
 			   String command = thisGroup;
 			      try
 			      {
 			         // now split on spaces (there should not be any spaces inside []'s)
 			    	  String[] parts = command.split(" ");
 
 			          // now check to see if the scan has been appended with f xx
 			          // to give the pinhole to beused for flux monitoring
 			          String pinhole = null;
 			          if (parts[parts.length - 2].compareTo("f") == 0)
 			          {
 			        	  pinhole = parts[parts.length - 1];
 
 			             // now recreate parts[] without these last two parameters
 			             String[] copy = parts.clone();
 			             parts = new String[copy.length - 2];
 			             for (int i = 0; i < copy.length - 2; i++)
 			             {
 			                parts[i] = copy[i];
 			             }
 			          }
 			          else
 			          {
 			        	  pinhole = "0";
 			          }
 			         // build the command line (adding calls gda.scan.StepScan)
 			         command = "innerscan=MultiRegionScan();innerscan.addScan(PolarimeterGridScan(";
 			         int i = 1;
 			         command += parts[i] + ","; // Outer dof name
 			         i++;
 			         command += parts[i] + ","; // Outer start
 			         i++;
 			         command += parts[i] + ","; //Outer stop
 			         i++;
 			         command += parts[i] + ","; //Outer step;
 			         i++;
 			         command += Double.toString(Double.valueOf(parts[i]) *1000) + ","; //Inner time converted to msecs
 			         i++;
 			         command += pinhole + "));"; //Flux monitor
 			         
 			         command += "stepscan=MultiRegionScan();stepscan.addScan(PolarimeterGridScan(";
 			         
 			         command += parts[i] + ","; // Inner dof name
 			         i++;
 			         command += parts[i] + ","; // Inner start
 			         i++;
 			         command += parts[i] + ","; //Inner stop
 			         i++;
 			         command += parts[i] + ","; //Inner step
 			         i++;
 		             command += "innerscan))";
 			       //  command += pinhole + "))"; //Flux monitor
 			         command += ";stepscan.runScan();";
 			      }
 
 			      catch (Exception ex)
 			      {
 			         // if an error, return the original string
 			         // and let jython interpreter handle any syntax errors
 			         logger.debug(
 			            "Error in Translator.translateStokesScan: {}", thisGroup);
 			         command = thisGroup;
 			      }
 			      
 			      return command;
 			   }
 		   
 		   /*
 		    * Based on translateScan. @param original_command String @return String
 		    */
 		   private static String translateConcurrentScan(String original_command)
 		   {
 		      String command = original_command;
 		      try
 		      {
 		         // now split on spaces (there should not be any spaces inside []'s)
 		         String[] parts = command.split(" ");
 		         // now check to see if te scan has been appended with f xx
 		          // to give the pinhole to beused for flux monitoring
 		         String pinhole = null;
 		          if (parts[parts.length - 2].compareTo("f") == 0)
 		          {
 		        	  pinhole = parts[parts.length - 1];
 
 		             // now recreate parts[] without these last two parameters
 		             String[] copy = parts.clone();
 		             parts = new String[copy.length - 2];
 		             for (int i = 0; i < copy.length - 2; i++)
 		             {
 		                parts[i] = copy[i];
 		             }
 		          }
 		          else
 		          {
 		        	  pinhole = "0";
 		          }
 		         
 		         // build the command line (adding calls gda.scan.StepScan)
 		         command = "thisscan = PolarimeterConcurrentScan([";
 		         int i = 1;
 		         command += parts[i] + ","; // Outer dof name
 		         i++;
 		         command += parts[i] + ","; // Outer start
 		         i++;
 		         command += parts[i] + ","; //Outer stop
 		         i++;
 		         command += parts[i] + ","; //Outer step;
 		         i++;
 		         command += Double.toString(Double.valueOf(parts[i]) *1000) + ","; //Inner time converted to msecs
 		         i++;
 		         command += pinhole + ","; //Flux monitor
 		         i++;
 		         
 		         for (int j = i-1 ; j < parts.length - 1; ++j)
 		         {
 		            String newPart = parts[j].trim();
 		            if (!newPart.endsWith(","))
 		            {
 		               newPart += ",";
 		            }
 		            command += newPart;
 		         }
 		         command += parts[parts.length - 1] + "]);";
 		         command += "thisscan.runScan();";
 		      }
 
 		      catch (Exception ex)
 		      {
 		         // if an error, return the original string
 		         // and let jython interpreter handle any syntax errors
 		         logger.debug("Error in Translator.translateConcurrentScan: {} ",original_command);
 		         command = original_command;
 		      }
 		      return command;
 		   }
 	
 	/*
 	    * Translates scan command string for standard polaimeter scans with
 	    * or without optioanl flux monitoring. as idicated by f field
 	    * 
 	    *  @param original_command String 
 	    *  @return String
 	    */
 	   private static String translateScan(String original_command)
 	   {
 	      String command = original_command;
 	      try
 	      {
 	         // now split on spaces (there should not be any spaces inside []'s)
 	         String[] parts = command.split(" ");
 	         
 	         //	       now check to see if te scan has been appended with f xx
 	          // to give the pinhole to beused for flux monitoring
 	         String pinhole = null;
 	          if (parts[parts.length - 2].compareTo("f") == 0)
 	          {
 	        	  pinhole = parts[parts.length - 1];
 
 	             // now recreate parts[] without these last two parameters
 	             String[] copy = parts.clone();
 	             parts = new String[copy.length - 2];
 	             for (int i = 0; i < copy.length - 2; i++)
 	             {
 	                parts[i] = copy[i];
 	             }
 	          }
 	          else
 	          {
 	        	  pinhole = "0";
 	          }
 	          //Convert time to msecs
 	          parts[5]= Double.toString(Double.valueOf(parts[5]) *1000) + ","; 
 	          
 	         // build the command line (adding calls gda.scan.StepScan)
 	         command = "thisscan = PolarimeterGridScan(";
 	         for (int i = 1; i < parts.length ; ++i)
 	         {
 	            String newPart = parts[i].trim();
 	            if (!newPart.endsWith(","))
 	            {
 	               newPart += ",";
 	            }
 	            command += newPart;
 	         }
 	         command += pinhole + ");";
 	         command += "thisscan.runScan();";
 	      }
 
 	      catch (Exception ex)
 	      {
 	         // if an error, return the original string
 	         // and let jython interpreter handle any syntax errors
 	         logger.debug("Error in Translator.translateConcurrentScan: {}", original_command);
 	         command = original_command;
 	      }
 	      return command;
 	   }
 	   private String translateTScan(String thisGroup)
 	   {
 	      String command = "thisscan=PseudoDeviceTimeScan([";
 
 	      try
 	      {
 	         String[] parts = thisGroup.split(" ");
 
 	         JythonServerFacade server = JythonServerFacade.getInstance();
 
 	         boolean lastWasAScannable = false;
 	         boolean first = true;
 	         boolean numberOfPointsFound = false;
 
 	         for (int i = 1; i < parts.length; i++)
 	         {
 
 	            String eval =
 	               server.evaluateCommand("isinstance(" + parts[i] + ",Scannable)");
 
 	            if (eval.compareTo("1") == 0 && first)
 	            {
 	               command += parts[i];
 	               lastWasAScannable = true;
 	               first = false;
 	            }
 	            else if (eval.compareTo("1") == 0 && !first)
 	            {
 	               command += ",";
 	               command += parts[i];
 	               lastWasAScannable = true;
 	               first = false;
 	            }
 	            else if (eval.compareTo("1") != 0 && lastWasAScannable)
 	            {
 	               command += "],";
 	               command += parts[i];
 	               lastWasAScannable = false;
 	            }
 	            else if (eval.compareTo("1") != 0 && !lastWasAScannable)
 	            {
 	               command += ",";
 	               command += parts[i];
 	               lastWasAScannable = false;
 	               numberOfPointsFound = true;
 	            }
 	         }
 
 	         if (!numberOfPointsFound)
 	         {
 	            command += ",0";
 	         }
 
 	         command += ");thisscan.runScan();";
 
 	      }
 	      catch (Exception e)
 	      {
 	         logger.debug("Error in Translator.translateKScan: {}", thisGroup);
 	         command = thisGroup;
 	      }
 
 	      return command;
 	   }
 
 	@Override
 	public void addAliasedCommand(String commandName) {
 	}
 
 	@Override
 	public void addAliasedVarargCommand(String commandName) {
 	}
 	
 	@Override
 	public boolean ignoreRestOfLine(String thisGroup) {
		return thisGroup.trim().startsWith("#");
	}
 }
