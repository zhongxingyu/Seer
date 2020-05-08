 package uk.co.codefoo.bukkit.saywhat.GameVariableToken;
 
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 public class InMyHand implements TokenExpander
 {
 	private Token token;
 	public InMyHand()
 	{
 		token = new Token(
 			"inmyhand"
 			,"The name of the item in the current player's hand"
 		);
 	}
 	@Override
 	public Token getToken()
 	{
 		return token;
 	}
 
 	@Override
 	public String getGameVariableValue(Player currentPlayer)
 	{
 		if (currentPlayer==null)
 		{
 			return "<console>";
 		}
 		ItemStack item = currentPlayer.getItemInHand();
 		String result = 
 			item.getItemMeta().hasDisplayName()
 				? item.getItemMeta().getDisplayName()
 				: item.getType().toString().replace("_", " ").toLowerCase();
 		return result;
 	}
 }
