 /*****************************************************************************
  * This source file is part of SBS (Screen Build System),                    *
  * which is a component of Screen Framework                                  *
  *                                                                           *
  * Copyright (c) 2008-2011 Ratouit Thomas                                    *
  *                                                                           *
  * This program is free software; you can redistribute it and/or modify it   *
  * under the terms of the GNU Lesser General Public License as published by  *
  * the Free Software Foundation; either version 3 of the License, or (at     *
  * your option) any later version.                                           *
  *                                                                           *
  * This program is distributed in the hope that it will be useful, but       *
  * WITHOUT ANY WARRANTY; without even the implied warranty of                *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser   *
  * General Public License for more details.                                  *
  *                                                                           *
  * You should have received a copy of the GNU Lesser General Public License  *
  * along with this program; if not, write to the Free Software Foundation,   *
  * Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA, or go to   *
  * http://www.gnu.org/copyleft/lesser.txt.                                   *
  *****************************************************************************/
 
 package screen.tools.sbs;
 
 import screen.tools.sbs.actions.ActionManager;
 import screen.tools.sbs.objects.ErrorList;
 import screen.tools.sbs.targets.Parameters;
 import screen.tools.sbs.targets.TargetManager;
 import screen.tools.sbs.utils.Logger;
 
 /**
  * Main class, entry point for sbs commands
  * 
  * @author Ratouit Thomas
  * 
  */
 public class Main {
 	/**
 	 * Logs registered errors and warnings.
 	 * 
 	 * @return has error during the process
 	 * 
 	 */
 	private static boolean checkErrors(){
 		if(ErrorList.instance.hasErrors()){
 			Logger.info("errors detected");
 			Logger.info("Logged errors (" + ErrorList.instance.getErrors().size() + ") :");
 			ErrorList.instance.displayErrors();
 			if(ErrorList.instance.hasWarnings()){
 				Logger.info("Logged warnings (" + ErrorList.instance.getWarnings().size() + ")");
 				ErrorList.instance.displayWarnings();
 			}
 			Logger.info("============== STOP ===============");
 			return false;
 		}else if(ErrorList.instance.hasWarnings()){
 			Logger.info("warnings detected");
 			Logger.info("Logged warnings (" + ErrorList.instance.getWarnings().size() + ")");
 			ErrorList.instance.displayWarnings();
 		}
 		return true;
 	}
 	
 	/**
 	 * Software entry / Main method.
 	 * 
 	 * @param args
 	 * @throws Exception
 	 * 
 	 */
 	public static void main(String[] args) throws Exception {
 		Logger.info("------------ begin SBS ------------");
 		
 		ActionManager actionManager = new ActionManager();
 		TargetManager targetManager = new TargetManager(actionManager);
 		Parameters parameters = new Parameters(args);
 		
 		//register actions for a given target
 		targetManager.call(parameters.getTargetCall(), parameters);
 		//verify that there is no error to resume the process
 		boolean hasNoError = checkErrors();
 		if(ErrorList.instance.isPrintUsage()){
 			//print help
 			targetManager.callUsage(ErrorList.instance.getTargetUsage());
 		}
 		else{
 			//don't process actions if there are some errors while target processing
 			if(hasNoError){
 				//process registered actions
 				actionManager.processActions();
 				//verify that there is no error to resume the process
 				hasNoError = checkErrors();
 				if(ErrorList.instance.isPrintUsage())
 					//print help
 					targetManager.callUsage(ErrorList.instance.getTargetUsage());
 			}
 		}
 		
 		Logger.info("------------- end SBS -------------");
 		Logger.info("");
 		Logger.info("-----------------------------------");
 		if(hasNoError)
 			Logger.info("        COMMAND SUCCESSFUL         ");
 		else
 			Logger.info("          COMMAND FAILED           ");
 		Logger.info("-----------------------------------");
 	}
 }
