 package org.ruhlendavis.mc.abacus;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 /**
  * Command handler/dispatcher. Checks to see if the command issued is an
  * Abacus command and calls the appropriate methods.
  * 
  * @author Feaelin (Iain E. Davis) <iain@ruhlendavis.org>
  */
 public class AbacusCommandExecutor implements CommandExecutor
 {
 	/**
 	 * The onCommand method is called by Bukkit during a command event.
 	 * 
 	 * @param sender		CommandSender object referencing either the player or the
 	 *                  console.
 	 * @param command   Command object representing the command matched by the
 	 *                  label.
 	 * @param label     String containing the actual command typed.
 	 * @param arguments Array of Strings containing everything typed after the
 	 *                  command.
 	 * @return False if we want Bukkit to display the 'usage' message.
 	 */
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label,
 	                         String[] arguments)
 	{
 		if (command.getName().equalsIgnoreCase("abacus"))
 		{
 			if (arguments.length > 0)
 			{
				if (arguments[1].contains("wood") && arguments[1].contains("stair"))
 				{
					sender.sendMessage(ChatColor.GREEN + computeMaterials(arguments[0], 134));
 				}
 				else
 				{	
 					try 
 					{
 						Parser parser = new Parser(arguments);
 						sender.sendMessage(ChatColor.GREEN + "Result: " + parser.getResult());
 					}
 					catch (ParserMathException e)
 					{
 						sender.sendMessage(ChatColor.RED + e.getMessage());
 					}
 				}
 			}
 			else
 			{
 				sender.sendMessage(ChatColor.RED + "Provide an expression to evaluate.");
 			}
 		}
 		return true;
 	}
 	
 	/**
 	 * Experimental Method to compute the required materials needed for a given
 	 * item.
 	 * 
 	 * @param sQuantity	String containing the quantity to produce.
 	 * @param type integer representing the Bukkit item_type ID top produce.
 	 * @return String containing the required amounts.
 	 */
 	private String computeMaterials(String sQuantity, int type)
 	{
 		long quantity;
 		try 
 		{
 			quantity = Long.parseLong(sQuantity);
 		}
 		catch(NumberFormatException e)
 		{
 			return ChatColor.RED + "'" + sQuantity + "' is not a number.";
 		}
 		
 		if (quantity < 1)
 		{
 			return ChatColor.RED + "'" + sQuantity + "' is an invalid amount.";
 		}
 		
 		switch(type)
 		{
 			case 134:
 			case 135:
 			case 136:
 				long planks = roundUpPositive(quantity, 4) * 6;
 				return quantity + " wooden stairs requires " + planks
 						 + " planks or " + roundUpPositive(planks, 4) + " logs.";
 			default:
 				return "";
 		}
 	}
 	
 	/**
 	 *  Utility function to perform integer division that rounds up. This one is
 	 *  safe to use with negative numbers.
 	 * 
 	 * @param number			long integer, number to be divided.
 	 * @param divisor			long integer, number to divide by.
 	 * @return						long integer result.
 	 * @see #roundUpPositive
 	 */
 	private long roundUpAny(long number, long divisor)
 	{
     int sign = (number > 0 ? 1 : -1) * (divisor > 0 ? 1 : -1);
     return sign * (Math.abs(number) + Math.abs(divisor) - 1) / Math.abs(divisor);
 	}
 	
 	/**
 	 *  Utility function to perform integer division that rounds up.
 	 *  NOTE: Will not operate correctly on negative numbers, but is possibly
 	 *  faster than roundUpAny()
 	 * 
 	 * @param number			long integer, number to be divided.
 	 * @param divisor			long integer, number to divide by.
 	 * @return						long integer result.
 	 * @see #roundUpAny
 	 */
 	private long roundUpPositive(long number, long divisor)
 	{
     return (number + divisor - 1) / divisor;
 	}	
 }
