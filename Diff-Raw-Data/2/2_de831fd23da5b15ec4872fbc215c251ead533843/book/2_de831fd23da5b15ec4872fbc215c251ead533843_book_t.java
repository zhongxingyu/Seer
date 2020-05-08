 /*
  *  Copyright:
  *  2013 Darius Mewes
  */
 
 package de.dariusmewes.TimoliaCore.commands;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.meta.BookMeta;
 
 public class book extends TCommand {
 
 	public book(String name) {
 		super(name);
 		setIngame();
 		setMinArgs(2);
 		setUsage("/book <author/title> [args]");
 		setDesc("Change the author and title of a book");
 	}
 
 	public void perform(CommandSender sender, String[] args) {
 		Player p = (Player) sender;
 
 		// String name = args[1];
 		// String text = "";
 		// String author = args.length == 3 ? args[2] : p.getName();
 		//
 		// if (args[0].equalsIgnoreCase("load")) {
 		// try {
 		// File file = new File(TimoliaCore.dataFolder + File.separator +
 		// "books" + File.separator + TimoliaCore.getCorrectName(name) +
 		// ".txt");
 		// if (!file.exists()) {
 		// sender.sendMessage(_("invalidFile"));
 		// return;
 		// }
 		//
 		// BufferedReader input = new BufferedReader(new FileReader(file));
 		//
 		// String in;
 		// while ((in = input.readLine()) != null)
 		// text += in + "\n%%%X%%%";
 		//
 		// input.close();
 		// } catch (Exception e) {
 		// sender.sendMessage(_("errorLoadBook"));
 		// return;
 		// }
 		//
 		// ItemStack book = new ItemStack(Material.WRITTEN_BOOK, 1);
 		// BookMeta meta = (BookMeta) book.getItemMeta();
 		//
 		// text = ChatColor.translateAlternateColorCodes('&', text);
 		// String[] lines = text.split("%%%X%%%");
 		//
 		// int pageCount = lines.length / 13;
 		// int restLines = lines.length % 13;
 		//
 		// String page;
 		//
 		// for (int pageFactor = 0; pageFactor < pageCount; pageFactor++) {
 		// page = "";
 		// for (int line = 0; line < 13; line++)
 		// page += lines[pageFactor * 13 + line];
 		//
 		// meta.addPage(page);
 		// }
 		//
 		// if (restLines > 0) {
 		// page = "";
 		// for (int line = 0; line < restLines; line++)
 		// page += lines[pageCount * 13 + line];
 		//
 		// meta.addPage(page);
 		// }
 		//
 		// meta.setTitle(ChatColor.translateAlternateColorCodes('&', name));
 		// meta.setAuthor(ChatColor.translateAlternateColorCodes('&', author));
 		// book.setItemMeta(meta);
 		//
 		// p.getInventory().addItem(book);
 		// sender.sendMessage(_("gotBook"));
 		// }
 		//
 		// else if (args[0].equalsIgnoreCase("save")) {
 		// if (p.getItemInHand().getType() != Material.WRITTEN_BOOK) {
 		// sender.sendMessage(_("itemNotBook"));
 		// return;
 		// }
 		//
 		// BookMeta meta = (BookMeta) p.getItemInHand().getItemMeta();
 		// for (int i = 1; i <= meta.getPageCount(); i++)
 		// text += meta.getPage(i) + "\n";
 		//
 		// try {
 		// File file = new File(TimoliaCore.dataFolder + File.separator +
 		// "books" + File.separator + name + ".txt");
 		// if (file.exists())
 		// file.delete();
 		//
 		// BufferedWriter output = new BufferedWriter(new FileWriter(file));
 		// output.append(text.replaceAll("", "&"));
 		// output.close();
 		// } catch (Exception e) {
 		// sender.sendMessage(_("errorsavebook"));
 		// return;
 		// }
 		//
 		// sender.sendMessage(_("bookSaved"));
 		// }
 		//
 		// else
 
 		if (args[0].equalsIgnoreCase("title")) {
 			if (p.getItemInHand().getType() != Material.WRITTEN_BOOK) {
 				sender.sendMessage(_("itemnotbook"));
 				return;
 			}
 
 			String msg = "";
 			for (int i = 1; i < args.length; i++)
 				msg += args[i] + " ";
 
 			BookMeta meta = (BookMeta) p.getItemInHand().getItemMeta();
 			meta.setTitle(ChatColor.translateAlternateColorCodes('&', msg));
 			p.getItemInHand().setItemMeta(meta);
			sender.sendMessage(_("bookTitleChanged"));
 		}
 
 		else if (args[0].equalsIgnoreCase("author")) {
 			if (p.getItemInHand().getType() != Material.WRITTEN_BOOK) {
 				sender.sendMessage(_("itemnotbook"));
 				return;
 			}
 
 			String msg = "";
 			for (int i = 1; i < args.length; i++)
 				msg += args[i] + " ";
 
 			BookMeta meta = (BookMeta) p.getItemInHand().getItemMeta();
 			meta.setAuthor(msg);
 			p.getItemInHand().setItemMeta(meta);
 			sender.sendMessage(_("bookAuthorChanged"));
 		}
 	}
 
 }
