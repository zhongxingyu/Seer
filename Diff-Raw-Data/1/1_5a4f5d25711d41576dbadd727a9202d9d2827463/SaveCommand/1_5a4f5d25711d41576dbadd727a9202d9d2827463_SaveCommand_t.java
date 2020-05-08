 package com.p000ison.dev.copybooks.commands;
 
 import com.p000ison.dev.copybooks.CopyBooks;
 import com.p000ison.dev.copybooks.api.InvalidBookException;
 import com.p000ison.dev.copybooks.objects.Book;
 import com.p000ison.dev.copybooks.objects.GenericCommand;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.spout.nbt.stream.NBTOutputStream;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import static com.p000ison.dev.copybooks.util.BookIO.*;
 
 /**
  * Represents a SaveCommand
  */
 public class SaveCommand extends GenericCommand {
 
 
     public SaveCommand(CopyBooks plugin, String name)
     {
         super(plugin, name);
         setArgumentRange(3, 4);
         setUsages("/cb save <file> <format> <iih/id> [id]");
         setPermissions("cb.commands.save");
        setIdentifiers("save");
     }
 
     @Override
     public void execute(CommandSender sender, String label, String[] args)
     {
         Book book = null;
         String mode = args[2];
 
         if (mode.equals("iih")) {
 
             if (sender instanceof Player) {
                 Player player = (Player) sender;
 
                 try {
                     book = new Book(player.getItemInHand(), player.getName());
                 } catch (InvalidBookException e) {
                     player.sendMessage(ChatColor.RED + "Failed to create book!");
                 }
             }
 
         } else if (mode.equals("id")) {
             long id;
 
             try {
                 id = Long.parseLong(args[3]);
             } catch (NumberFormatException e) {
                 sender.sendMessage(ChatColor.RED + "Failed to parse id!");
                 return;
             }
 
             book = plugin.getStorageManager().retrieveBook(id);
         }
 
         if (book == null) {
             return;
         }
 
         String fileName = args[0];
         String format = args[1];
 
         if (format.equalsIgnoreCase("nbt")) {
             File file = new File(new File(plugin.getDataFolder(), "saves"), fileName + ".book");
 
             if (file.isDirectory()) {
                 return;
             }
 
             if (file.exists()) {
                 return;
             }
 
             try {
                 writeNBTBook(book, file);
             } catch (IOException e) {
                 CopyBooks.debug(null, e);
             }
 
         } else if (format.equalsIgnoreCase("text")) {
             File file = new File(new File(plugin.getDataFolder(), "saves"), fileName + ".txt");
 
             if (file.isDirectory()) {
                 return;
             }
 
             if (file.exists()) {
                 return;
             }
 
             try {
                 writeBook(book.getPages(), file);
             } catch (IOException e) {
                 sender.sendMessage(ChatColor.RED + "Failed at saving file!");
             }
         }
     }
 }
