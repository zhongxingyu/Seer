 package org.ruhlendavis.mc.abacus;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.ruhlendavis.utility.NumberTools;
 
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
 			// Make sure we have some arguments.
 			if (arguments.length > 0)
 			{
 				// If the first argument is one of 'material/materia/materi/.../mat'
 				// then it is the material calculator call.
				if (arguments[0].substring(0,3).equalsIgnoreCase("mat"))
 				{
 					if (arguments.length == 3)
 					{
 						if (arguments[2].contains("wood") && arguments[2].contains("stair"))
 						{
 							sender.sendMessage(ChatColor.GREEN + computeMaterials(arguments[1], 134));
						}						
 					}
 					else
 					{
 						sender.sendMessage(ChatColor.RED + "Usage: /abacus material <quantity> <material>");
 					}
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
 				long planks = NumberTools.divideRoundUpPositive(quantity, 4) * 6;
 				return quantity + " wooden stairs requires " + planks
 						 + " planks or " + NumberTools.divideRoundUpPositive(planks, 4) + " logs.";
 			default:
 				return "";
 		}
 	}
 }
