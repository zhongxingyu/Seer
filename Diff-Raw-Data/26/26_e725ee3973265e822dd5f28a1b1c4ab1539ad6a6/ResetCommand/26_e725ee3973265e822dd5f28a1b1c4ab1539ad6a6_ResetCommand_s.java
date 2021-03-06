 package net.aufdemrand.denizen.commands.core;
 
 import net.aufdemrand.denizen.commands.AbstractCommand;
 import net.aufdemrand.denizen.scripts.ScriptEntry;
 import net.citizensnpcs.command.exception.CommandException;
 
 /**
  * 
  *  
  * @author Jeremy Schroeder
  */
 
 public class ResetCommand extends AbstractCommand {
 
 	/* RESET ('Name of Script') [FINISHES|FAILS]  or  RESET [FLAG:[NAME]]
 
 	/* Arguments: [] - Required, () - Optional 
 	 * [FINISHED|FAILED|FLAG:[NAME]] 
 	 * (['Name of Script']) - Required when using FINISHES/FAILS
 	 * 
 	 * Modifiers: 
 	 * None.
 	 * 
 	 * Example usages:
 	 * 
 	 * RESET FAILS
 	 * RESET 'Example Script 6' FINISHES
 	 * RESET 'FLAG:MAGICSHOPITEM'
 	 * 
 	 */
 
 
 	private enum ResetType {
 		FINISH, FAIL, FLAG
 	}
 
 	@Override
 	public boolean execute(ScriptEntry theCommand) throws CommandException {
 
 		/* Set variables */
 		String theScript = theCommand.getScript();
 		ResetType resetType = null;
 		String theFlag = null;
 
 		if (theCommand.arguments() == null)
 			throw new CommandException("...not enough arguments! Use RESET ('Name of Script') [FINISHES|FAILS]  or  RESET [FLAG:[NAME]]");
 
 		/* Check arguments */
 		for (String thisArg : theCommand.arguments()) {
 
			if (thisArg.equals("FINISHES") || thisArg.equals("FINISHED") || thisArg.equals("FINISH")) {
 				resetType = ResetType.FINISH;
 				aH.echoDebug("...will reset FINISHED.");
 			}
 
			else if (thisArg.equals("FAILS") || thisArg.equals("FAIL") || thisArg.equals("FAILED")) {
 				resetType = ResetType.FAIL;
 				aH.echoDebug("...will reset FAILED.");
 			}
 
 			else if (thisArg.toUpperCase().contains("FLAG:")) {
				theFlag = aH.getStringModifier(thisArg);
 				resetType = ResetType.FLAG;
 				aH.echoDebug("...will reset '%s'.", thisArg);
 			}
 
 			else if (aH.matchesScript(thisArg)) {
 				theScript = aH.getStringModifier(thisArg);
 				aH.echoDebug("...script affected is now '%s'.", thisArg);
 			}
			
 			else { 
 				theScript = thisArg;
 				aH.echoDebug("...script affected is now '%s'.", thisArg);
 			}
 		}
 
 
 		/* Reset! */
 		if (resetType != null) {
 			switch (resetType) {
 
 			case FINISH:
 				plugin.getSaves().set("Players." + theCommand.getPlayer().getName() + "." + theScript + "." + "Completed", null);
 				break;
 
 			case FAIL:
 				plugin.getSaves().set("Players." + theCommand.getPlayer().getName() + "." + theScript + "." + "Failed", null);
 				break;
 
 			case FLAG:
				plugin.getSaves().set("Players." + theCommand.getPlayer().getName() + ".Flags." + theFlag, null);
 				break;
 			}
			
 			plugin.saveSaves();
 			return true;
 		}
 
 		return false;
 	}
 
 
 }
