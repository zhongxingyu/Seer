 package de.bdh.ks;
 
 import java.util.HashMap;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class KSLang 
 {
 	HashMap<String,HashMap<String,String>> lng = new HashMap<String,HashMap<String,String>>();;
 	String lang = "";
 	public KSLang()
 	{
 		HashMap<String,String> en = new HashMap<String,String>();
 		//HashMap<String,String> de = new HashMap<String,String>();
 		
 		en.put("usage", "USAGE: /auction SELL/BUY/REQUEST/DETAIL/LIST/LISTREQUESTS/COLLECT/ABORT/ABORTREQUEST");
 		en.put("usage_abort","USAGE: /auction abort ID - you can get the id by using list");
 		en.put("usage_request", "USAGE: /auction request (BLOCK) MAXPRICE AMOUNT");
 		en.put("usage_buy","USAGE: /auction buy (BLOCK) MAXPRICE AMOUNT");
 		en.put("usage_detail", "USAGE: /auction detail BLOCK");
 		en.put("usage_sell","USAGE: /auction sell BLOCK PRICEPERBLOCK (AMOUNT) OR /auction sell PRICE for Item in Hand");
 		en.put("usage_abortrequest","USAGE: /auction abortrequest ID - you can get the id by using list");
 		en.put("err_num","$1 must be numeric");
 		en.put("rem_success","Your auction has been cancelled. You can pick it up at the auction house");
 		en.put("err_invalid_id", "This ID was invalid or you dont have the permissions to do that");
 		en.put("rem_rec_success","Your request has been cancelled. You can pick it up at the auction house");
 		en.put("err_nosale","You don't have items for sale");
 		en.put("header_list","You've $1 $2. Page: $3 of $4");
 		en.put("err_noreq","You don't have items requested");
 		en.put("err_noperm","You're not allowed to do this");
 		en.put("err_to_ah","You've to go to an auction house to do this");
 		en.put("err_toohigh","$1 is too high");
 		en.put("err_nodeliver", "There is nothing for delivery");
 		en.put("err_block_404","Block not found");
 		en.put("err_block","Block is invalid");
 		en.put("err","Something went wrong");
 		en.put("err_nomoney","You don't have enough money");
 		en.put("err_nooffer","There is no offer which fulfills your options");
 		en.put("err_nomoney_fee", "You cannot afford the fee of $1 $a");
 		en.put("suc_offer", "Success. You're offering $1 Blocks for $2 $a");
 		en.put("suc_fee_paid","You've paid an auction-fee of $1 $a");
 		en.put("suc_bought","You've bought the amount you wanted");
 		en.put("suc_bought_part","You've bought $1 of $2");
 		en.put("suc_req", "You've requested $1 items for $2 $a");
 		en.put("req_info","Your request is valid for 14 days. If noone offers this item for your price, you'll get your money back");
 		
 		this.lng.put("en", en);
 		//this.lng.put("de", de);
 		
 		if(this.lng.get(configManager.lang) == null)
 			this.lang = "en";
 		else
 			this.lang = configManager.lang;
 	}
 	
 
 	public void msg(Player p, String el)
 	{
 		this.msg((Player)p, el, new Object[]{});
 	}
 	
 	public void msg(CommandSender p, String el)
 	{
 		this.msg((Player)p, el, new Object[]{});
 	}
 	
 	public void msg(CommandSender p, String el, Object[] args)
 	{
 		this.msg((Player)p, el, args);
 	}
 	
 	public void msg(Player p,String el, Object[] args)
 	{
 		String str = el;
 		if(this.lng.get(this.lang).get(el) != null)
 			str = this.lng.get(this.lang).get(el);
 		
		str = str.replace("$a", Main.econ.currencyNamePlural());
 		
 		if(args != null && args.length > 0)
 		{
 			for (int i = 1; i < args.length+1; i++) 
 			{
 				String tmp = "";
 				if(args[i-1] instanceof String)
 					tmp = (String)args[i-1];
 				else if(args[i-1] instanceof Integer)
 					tmp = new Integer((Integer)args[i-1]).toString();
 				
 				str = str.replace("$" + i, tmp);
 			}
 			str = str.replace("$$", "$");
 		}
 		
 		p.sendMessage(ChatColor.GOLD + str);
 	}
 }
