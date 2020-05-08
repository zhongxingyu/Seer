 // ShowCommand.java
 //
 // Informatics 122 Winter 2013
 // Project #2: Who's Gonna Ride Your Wild Horses? (Implementation)
 //
 // Implements the SHOW command
 
 package inf122.horses.console.commands;
 
 import inf122.horses.console.results.CommandResult;
 import inf122.horses.console.results.NoHorseFoundCommandResult;
 import inf122.horses.console.results.NoRaceFoundCommandResult;
 import inf122.horses.console.results.ReachedPostTimeCommandResult;
 import inf122.horses.console.results.ShowBetPlacedCommandResult;
 import inf122.horses.console.state.RacetrackState;
 
 import java.util.Set;
 
 import uci.inf122.assignment2HorseBetting.Race;
 
 
 public class ShowCommand extends SingleHorseBetCommand
 {
 	public ShowCommand(int raceNumber, int amountPerHorse, Set<String> horseNumbers)
 	{
 		super(raceNumber, amountPerHorse, horseNumbers);
 	}
 	
 	public CommandResult execute(RacetrackState state)
 	{
 		int raceID = getRaceNumber();
 		int betAmount = getAmountPerHorse();
 		Set<String> horses = getHorseNumbers();
 		Race race = state.getRace(raceID);
 		
 		// Inf122TBD: Return an actual result
 		if (state.doesRaceExist(raceID))
 		{
 			for (String next : horses)
 			{
 				if (!race.doesHorseExist(next))
 				{
 					return new NoHorseFoundCommandResult(raceID);
 				}
 			}
 			
 			if (race.getPostTime())
 			{
 				return new ReachedPostTimeCommandResult(raceID);
 			}
 			else
 			{
				String str = "\n";
 				
 				for (String next : horses)
 				{
 					int ticketID = state.placeBet(race, next, BetType.SHOW, betAmount);
					str = str + "Ticket ID#" + ticketID + ": Horse #" + next + " - Total Amount: $" + betAmount;
 					str += "\n";
 				}
 				return new ShowBetPlacedCommandResult(raceID, horses, betAmount, str);
 			}
 		}
 		else
 		{
 			return new NoRaceFoundCommandResult(getRaceNumber());
 		}
 	}
 }
