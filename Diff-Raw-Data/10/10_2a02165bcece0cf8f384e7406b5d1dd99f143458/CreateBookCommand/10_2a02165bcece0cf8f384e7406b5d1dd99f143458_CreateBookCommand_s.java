 /*
  * Copyright (C) 2012 p000ison
  * 
  * This work is licensed under the Creative Commons
  * Attribution-NonCommercial-NoDerivs 3.0 Unported License. To view a copy of
  * this license, visit http://creativecommons.org/licenses/by-nc-nd/3.0/ or send
  * a letter to Creative Commons, 171 Second Street, Suite 300, San Francisco,
  * California, 94105, USA.
  * 
  */
 package com.p000ison.dev.copybooks.commands;
 
 import com.p000ison.dev.copybooks.Book;
 import com.p000ison.dev.copybooks.CopyBooks;
 import com.p000ison.dev.copybooks.GenericCommand;
 import net.minecraft.server.Item;
 import org.bukkit.Material;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import java.util.ArrayList;
 
 /**
  * @author Max
  */
 public class CreateBookCommand extends GenericCommand {
 
     public CreateBookCommand(CopyBooks plugin, String name)
     {
         super(plugin, name);
         setPermissions("cb.command.create");
         setUsages("/cb create - Creates a book from a id");
         setArgumentRange(1, 2);
         setIdentifiers("create", "c");
     }
 
     @Override
     public void execute(CommandSender sender, String label, String[] args)
     {
         if (sender instanceof Player) {
             Player player = (Player) sender;
             ItemStack item = player.getItemInHand();
 
             if (item == null) {
                 return;
             }
 
             int amount = 1;
 
             if (args.length == 2) {
                 try {
                     amount = Integer.parseInt(args[1]);
                 } catch (NumberFormatException ex) {
                     player.sendMessage("Please enter a numberal amount!");
                     return;
                 }
             }
             int id;
             try {
                 id = Integer.parseInt(args[0]);
             } catch (NumberFormatException ex) {
                 player.sendMessage("Please enter a numberal id!");
                 return;
             }
 
             Book book = plugin.getStorageManager().retrieveBook(id);
 
             if (book == null) {
                 player.sendMessage("Book not found!");
                 return;
             }
 
             if (!Book.hasPermission(book, player)) {
                 player.sendMessage("You dont have permisson for this boook!");
                 return;
             }
 
            ItemStack bookItem = book.toItemStack(amount);
 
             player.getInventory().addItem(bookItem);
             player.updateInventory();
 
             sender.sendMessage(String.format("You got %s book with the id %s", amount, id));
         } else {
             sender.sendMessage(plugin.getTranslation("only.player"));
         }
     }
 }
