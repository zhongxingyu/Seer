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
 public class PlayerCommand implements CommandExecutor {
 
     private final String url;
 
     public PlayerCommand() {
         url = "http://a.scrollsguide.com/player?name={name}&fields=all";
     }
 
     @Override
     public void runEvent(CommandEvent event) {
         if (event.getArgs().length != 1) {
             if (event.getChannel() == null) {
                 event.getUser().sendMessage("Usage: .player [name]");
             } else {
                 event.getChannel().sendMessage(event.getUser().getNick() + ", usage: .player [name]");
             }
             return;
         }
         try {
             URL playerURL = new URL(url.replace("{name}", event.getArgs()[0]));
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
             JsonObject dataObject = obj.get("data").getAsJsonObject();
 
             StringBuilder builder = new StringBuilder();
 
             builder.append(dataObject.get("name").getAsString()).append("'s stats - ");
             builder.append("Rating: ").append(dataObject.get("rating").getAsInt()).append(" - ");
             builder.append("Rank: ").append(dataObject.get("rank").getAsInt()).append(" - ");
             builder.append("Played: ").append(dataObject.get("played").getAsInt()).append(" - ");
             builder.append("Won: ").append(dataObject.get("won").getAsInt());
             builder.append(" (");
             builder.append((int) ((dataObject.get("won").getAsDouble() / dataObject.get("played").getAsDouble()) * 100));
            builder.append("%) -");
             builder.append("Judgement wins: ").append(dataObject.get("limitedwon").getAsInt()).append(" - ");
             builder.append("Ranked wins: ").append(dataObject.get("rankedwon").getAsInt()).append(" - ");
             builder.append("Last game played: ").append(parseTime(dataObject.get("lastgame").getAsInt()));
 
             if (event.getChannel() == null) {
                 event.getUser().sendMessage(builder.toString());
             } else {
                 event.getChannel().sendMessage(event.getUser().getNick() + ", " + builder.toString());
             }
 
         } catch (IOException | JsonSyntaxException ex) {
             PokeBot.log(Level.SEVERE, "Error on getting player stats for Scrolls for " + event.getArgs()[0], ex);
             if (event.getChannel() == null) {
                 event.getUser().sendMessage("Error on getting player stats: " + ex.getLocalizedMessage());
             } else {
                 event.getChannel().sendMessage(event.getUser().getNick() + ", error on getting player stats: " + ex.getLocalizedMessage());
             }
         }
     }
 
     @Override
     public String[] getAliases() {
         return new String[]{"player"};
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
