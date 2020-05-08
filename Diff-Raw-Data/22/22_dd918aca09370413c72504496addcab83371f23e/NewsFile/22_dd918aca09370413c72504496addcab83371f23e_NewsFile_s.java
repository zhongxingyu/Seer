 package me.KeybordPiano459.Newspaper;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Scanner;
 import org.bukkit.ChatColor;
 
 public class NewsFile {
    Newspaper plugin;
    public NewsFile(Newspaper plugin) {
        this.plugin = plugin;
    }
     
     private File newsFile = null;
     
     public void createNews() {
        newsFile = new File(plugin.getDataFolder(), "news.txt");
         if (!newsFile.exists()) {
             try {
                 newsFile.createNewFile();
                 FileWriter writer = new FileWriter(newsFile);
                 writer.write("&dThis is what will be in the newspaper!\n");
                 writer.write("&2Now with color symbol support!\n");
                 writer.write("=== NEW PAGE ===\n");
                 writer.write("&3That creates a new page!\n");
                 writer.write("&4Use the news.txt file to set the news!");
                 writer.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
     
     public void setNews(String news) {
         try {
            newsFile = new File(plugin.getDataFolder(), "news.txt");
             FileWriter writer = new FileWriter(newsFile);
             writer.write(news);
             writer.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
     
     public ArrayList<String> getNews() {
         ArrayList<String> news = new ArrayList<String>();
         String page = "";
         try {
            File file = new File(plugin.getDataFolder(), "news.txt");
             Scanner s = new Scanner(file);
             while (s.hasNextLine()) {
                 String line = ChatColor.translateAlternateColorCodes('&', s.nextLine()) + "\n";
                 if (line.contains("=== NEW PAGE ===")) {
                     news.add(page);
                     page = "";
                 } else {
                     page += line;
                     if (!s.hasNextLine()) news.add(page);
                 }
             }
             s.close();
         } catch (IOException e) {
             e.printStackTrace();
         }
         
         return news;
     }
 }
