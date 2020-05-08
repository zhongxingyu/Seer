 package com.wolvencraft.prison.mines.util.variables;
 
 import org.bukkit.ChatColor;
 
 import com.wolvencraft.prison.mines.mine.Mine;
 import com.wolvencraft.prison.mines.util.Message;
 
 public class TimeTriggerVariables implements BaseVar {
 
 	@Override
 	public String parse(Mine mine, String option) {
 		Mine curMine;
		if(mine.hasParent()) curMine = mine.getSuperParent();
 		else curMine = mine;
 		if(!mine.getAutomaticReset()) return "<...>";
 		
 		if(option.startsWith("p")) {
 			// Reset period variable calculations
 			int ptime = curMine.getResetPeriod();
 			
 			int phour = ptime / 3600;										// Unformatted variables.
 			int pmin = ptime / 60;											// Contain exact values for hour, minutes, seconds.
 			int psec = ptime;												// Used in further calculations.
 			
 			int phourFormatted = phour;										// Formatted variables.
 			if(option.equalsIgnoreCase("phour")) return phourFormatted + "";
 			int pminFormatted = pmin - phour * 60;							// Values of higher-level variables have been subtracted.
 			if(option.equalsIgnoreCase("pmin")) return pminFormatted + "";
 			int psecFormatted = psec - pmin * 60;							// Do not have a 0 in front if the value is < 10.
 			if(option.equalsIgnoreCase("psec")) return psecFormatted + "";
 			
 			String phourClock = phourFormatted + "";						// Extra-formatted variables.
 			if(phourFormatted < 10) phourClock = "0" + phourClock;			// Have an added 0 in front
 			String pminClock = pminFormatted + "";							// if the value of the variable
 			if(pminFormatted < 10) pminClock = "0" + pminClock;				// is single-digit.
 			String psecClock = psecFormatted + "";							// Used in the super-formatted variable.
 			if(psecFormatted < 10) psecClock = "0" + psecClock;
 			
 			String ptimeClock = pminClock + ":" + psecClock;				// Super-formatted variable.
 			if(phour != 0) ptimeClock = phourFormatted + ":" + ptimeClock;	// Displays time in HOUR:MINUTE:SECOND format.
 			return ptimeClock;
 		} else if(option.startsWith("n")) {
 			// Next reset variable calculations
 			int ntime = curMine.getResetsIn();
 				
 			int nhour = ntime / 3600;										// Unformatted variables.
 			int nmin = ntime / 60;											// Contain exact values for hour, minutes, seconds.
 			int nsec = ntime;												// Used in further calculations.
 			
 			int nhourFormatted = nhour;										// Formatted variables.
 			if(option.equalsIgnoreCase("nhour")) return nhourFormatted + "";
 			int nminFormatted = nmin - nhour * 60;							// Values of higher-level variables have been subtracted.
 			if(option.equalsIgnoreCase("nmin")) return nminFormatted + "";
 			int nsecFormatted = nsec - nmin * 60;							// Do not have a 0 in front if the value is < 10.
 			if(option.equalsIgnoreCase("nsec")) return nsecFormatted + "";
 			
 			String nhourClock = nhourFormatted + "";						// Extra-formatted variables.
 			if(nhourFormatted < 10) nhourClock = "0" + nhourClock;			// Have an added 0 in front
 			String nminClock = nminFormatted + "";							// if the value of the variable
 			if(nminFormatted < 10) nminClock = "0" + nminClock;				// is single-digit.
 			String nsecClock = nsecFormatted + "";							// Used in the super-formatted variable.
 			if(nsecFormatted < 10) nsecClock = "0" + nsecClock;
 			
 			String ntimeClock = nminClock + ":" + nsecClock;				// Super-formatted variable.
 			if(nhour != 0) ntimeClock = nhourFormatted + ":" + ntimeClock;	// Displays time in HOUR:MINUTE:SECOND format.
 			return ntimeClock;
 		} else return "<...>";
 	}
 
 	@Override
 	public void getHelp() {
 		Message.send("+ Period at which the mine resets");
 		Message.send("|- " + ChatColor.GOLD + "<PTIME> " + ChatColor.WHITE + "Time in HH:MM:SS format", false);
 		Message.send("|- " + ChatColor.GOLD + "<PHOUR> " + ChatColor.WHITE + "Hours portion of the time", false);
 		Message.send("|- " + ChatColor.GOLD + "<PMIN> " + ChatColor.WHITE + "Minutes portion of the time", false);
 		Message.send("|- " + ChatColor.GOLD + "<PSEC> " + ChatColor.WHITE + "Seconds portion of the time", false);
 		Message.send("");
 		Message.send("+ Time until the next reset");
 		Message.send("|- " + ChatColor.GOLD + "<NTIME> " + ChatColor.WHITE + "Time in HH:MM:SS format", false);
 		Message.send("|- " + ChatColor.GOLD + "<NHOUR> " + ChatColor.WHITE + "Hours portion of the time", false);
 		Message.send("|- " + ChatColor.GOLD + "<NMIN> " + ChatColor.WHITE + "Minutes portion of the time", false);
 		Message.send("|- " + ChatColor.GOLD + "<NSEC> " + ChatColor.WHITE + "Seconds portion of the time", false);
 		Message.send("");
 		
 	}
 
 }
