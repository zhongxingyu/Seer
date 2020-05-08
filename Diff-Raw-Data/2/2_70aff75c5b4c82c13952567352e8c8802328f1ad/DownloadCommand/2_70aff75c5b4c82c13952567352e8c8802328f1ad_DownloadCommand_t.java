 package com.p000ison.dev.copybooks.commands;
 
 import com.p000ison.dev.copybooks.CopyBooks;
 import com.p000ison.dev.copybooks.api.InvalidBookException;
 import com.p000ison.dev.copybooks.api.WrittenBook;
 import com.p000ison.dev.copybooks.objects.GenericCommand;
 import com.p000ison.dev.copybooks.util.Helper;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import java.io.IOException;
 
 /**
  * Represents a AcceptCommand
  */
 public class DownloadCommand extends GenericCommand {
 
     public DownloadCommand(CopyBooks plugin, String name)
     {
         super(plugin, name);
         setArgumentRange(2, 3);
         setIdentifiers("download");
         setUsages("/cb download <url> <title> [author]- Downloads a url from this page.");
     }
 
     @Override
     public void execute(CommandSender sender, String label, String[] args)
     {
         if (sender instanceof Player) {
             Player player = (Player) sender;
 
             String author = player.getName();
 
             if (args.length == 3) {
                 author = args[2];
             }
 
             WrittenBook book;
 
             try {
                book = Helper.createBookFromURL(Helper.formatURL(args[0]), args[1], author);
             } catch (IOException e) {
                 player.sendMessage("Invalid URL! [" + e.getMessage() + "]");
                 return;
             } catch (InvalidBookException e) {
                 player.sendMessage("Failed to create book!");
                 return;
             }
 
             try {
                 player.getInventory().addItem(book.toItemStack(1));
             } catch (InvalidBookException e) {
                 player.sendMessage("Failed to create book!");
                 return;
             }
 
             player.sendMessage("Book downloaded!");
         } else {
 
         }
     }
 }
