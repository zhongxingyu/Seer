 /*
  * Copyright (C) 2014 Lord_Ralex
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.hoenn.pokebot.extensions.scrolls;
 
 import com.google.gson.JsonElement;
 import com.google.gson.JsonObject;
 import com.google.gson.JsonParser;
 import com.google.gson.JsonSyntaxException;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.logging.Level;
 import org.apache.commons.lang3.StringUtils;
 import org.hoenn.pokebot.PokeBot;
 import org.hoenn.pokebot.api.CommandExecutor;
 import org.hoenn.pokebot.api.events.CommandEvent;
 
 /**
  * @author Lord_Ralex
  */
 public class PriceCommand implements CommandExecutor {
 
     private final String url;
 
     public PriceCommand() {
         url = "http://a.scrollsguide.com/prices?name={name}&days={days}";
     }
 
     @Override
     public void runEvent(CommandEvent event) {
         if (event.getArgs().length == 0 || (event.getArgs()[0].equals("-d") && event.getArgs().length < 3)) {
             if (event.getChannel() == null) {
                 event.getUser().sendMessage("Usage: .price <-d days> [name]");
             } else {
                 event.getChannel().sendMessage(event.getUser().getNick() + ", usage: .price <-d days> [name]");
             }
             return;
         }
         int days = 2;
         String[] name = event.getArgs();
         if (event.getArgs()[0].equals("-d")) {
             name = new String[event.getArgs().length - 2];
             for (int i = 2; i < event.getArgs().length; i++) {
                 name[i - 2] = event.getArgs()[i];
             }
            days = Integer.parseInt(event.getArgs()[1]);
         }
         try {
             URL playerURL = new URL(url.replace("{days}", Integer.toString(days)).replace("{name}", StringUtils.join(name, "%20")));
             List<String> lines = new LinkedList<>();
             HttpURLConnection conn = (HttpURLConnection) playerURL.openConnection();
             conn.setRequestProperty("User-Agent", "PokeBot - " + PokeBot.VERSION);
             conn.connect();
             try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                 String line;
                 while ((line = reader.readLine()) != null) {
                     lines.add(line);
                 }
             }
 
             JsonParser parser = new JsonParser();
             JsonElement element = parser.parse(StringUtils.join(lines, "\n"));
             JsonObject obj = element.getAsJsonObject();
 
             String result = obj.get("msg").getAsString();
             if (!result.equalsIgnoreCase("success")) {
                 if (event.getChannel() == null) {
                     event.getUser().sendMessage("Scroll not found");
                 } else {
                     event.getChannel().sendMessage(event.getUser().getNick() + ", scroll not found");
                 }
                 return;
             }
             JsonObject dataObject = obj.get("data").getAsJsonArray().get(0).getAsJsonObject();
 
             StringBuilder builder = new StringBuilder();
 
             builder.append("Buy: ").append(dataObject.get("buy").getAsInt()).append(" Gold - ");
             builder.append("Sell: ").append(dataObject.get("sell").getAsInt()).append(" Gold - ");
             builder.append("Last seen: ").append(parseTime(dataObject.get("lastseen").getAsInt()));
             if (event.getChannel() == null) {
                 event.getUser().sendMessage(builder.toString().split("\n"));
             } else {
                 String[] message = builder.toString().split("\n");
                 for (String msg : message) {
                     event.getChannel().sendMessage(event.getUser().getNick() + ", " + msg);
                 }
             }
 
         } catch (IOException | JsonSyntaxException | IllegalStateException ex) {
             PokeBot.log(Level.SEVERE, "Error on getting scroll for Scrolls for '" + StringUtils.join(event.getArgs(), " ") + "'", ex);
             if (event.getChannel() == null) {
                 event.getUser().sendMessage("Error on getting scroll: " + ex.getLocalizedMessage());
             } else {
                 event.getChannel().sendMessage(event.getUser().getNick() + ", error on getting scroll: " + ex.getLocalizedMessage());
             }
         }
     }
 
     @Override
     public String[] getAliases() {
         return new String[]{"price"};
     }
 
     private String parseTime(int time) {
         if (time < 10) {
             return "Just now";
         }
         String[] strs = new String[]{"second", "minute", "hour", "day", "week", "month"};
 
         int[] duration = new int[]{1, 60, 3600, 86400, 604800, 2630880};
         double no = 0;
 
         int i;
         for (i = duration.length - 1; (i >= 0) && ((no = time / duration[i]) < 1); i--) {
 
         }
 
         int t = (int) Math.floor(no);
         return t + " " + strs[i] + ((t > 1) ? "s" : "") + " ago";
     }
 }
