 /**
  * palava - a java-php-bridge
  * Copyright (C) 2007-2010  CosmoCode GmbH
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  */
 
 package de.cosmocode.palava.jobs.form;
 
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
import org.apache.log4j.Logger;
 
 import de.cosmocode.json.JSON;
 import de.cosmocode.json.JSONRenderer;
 import de.cosmocode.palava.bridge.Content;
 import de.cosmocode.palava.bridge.call.Arguments;
 import de.cosmocode.palava.bridge.call.Call;
 import de.cosmocode.palava.bridge.command.Command;
 import de.cosmocode.palava.bridge.command.CommandException;
 import de.cosmocode.palava.bridge.content.JsonContent;
 
 public class parseDate implements Command {
 
    private static final Logger log = Logger.getLogger(parseDate.class); 
     
     @Override
     public Content execute(Call call) throws CommandException {
         final Arguments arguments = call.getArguments();
         arguments.require("pattern", "source");
         
         final String pattern = arguments.getString("pattern");
         final String source = arguments.getString("source");
         
         final DateFormat dateFormat = new SimpleDateFormat(pattern);
         dateFormat.setLenient(false);
         
         final JSONRenderer out = JSON.createJSONRenderer();
         
         Object returnValue = Boolean.FALSE;
         
         try {
             final Date date = dateFormat.parse(source);
             returnValue = date.getTime() / 1000;
         } catch (ParseException e) {
             log.info("Parsing date " + source + " with pattern " + pattern + " failed.");
         }
         
         out.object().
                 key("date").value(returnValue).
             endObject();
         
         return new JsonContent(out);
     }
 
 }
