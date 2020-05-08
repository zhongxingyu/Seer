 /*
  * Copyright (C) 2012 MineStar.de 
  * 
  * This file is part of VinciCode.
  * 
  * VinciCode is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * VinciCode is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with VinciCode.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package de.minestar.vincicode.formatter;
 
 import java.util.Date;
 
 import org.bukkit.ChatColor;
 
 import de.minestar.minestarlibrary.messages.Message;
 import de.minestar.vincicode.util.BookHelper;
 
 public class OfficialFormat implements MessageFormat {
 
     @Override
     public String formatHead(Message message) {
         StringBuilder stringBuilder = new StringBuilder(BookHelper.CHARS_PER_PAGE);
 
        BookHelper.appendColoredText(stringBuilder, ChatColor.DARK_RED, "++Offiziell++");
         BookHelper.newLine(stringBuilder);
 
         // append sender
         BookHelper.appendColoredText(stringBuilder, ChatColor.DARK_GREEN, "Absender: ");
         stringBuilder.append(message.getSender());
         BookHelper.newLine(stringBuilder);
 
         // append date
         BookHelper.appendColoredText(stringBuilder, ChatColor.DARK_GREEN, "Datum: ");
         stringBuilder.append(BookHelper.DATE.format(new Date(message.getTimestamp())));
         BookHelper.newLine(stringBuilder);
 
         // append time
         BookHelper.appendColoredText(stringBuilder, ChatColor.DARK_GREEN, "Uhrzeit: ");
         stringBuilder.append(BookHelper.TIME.format(new Date(message.getTimestamp())));
         BookHelper.newLine(stringBuilder);
 
         // append empty line
         BookHelper.emptyLine(stringBuilder);
 
         return stringBuilder.toString();
     }
 
     @Override
     public String formatBody(Message message) {
         return message.getMessage();
     }
 
 }
