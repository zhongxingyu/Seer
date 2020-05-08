 /**
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Copyright (c) 2013 Andune (andune.alleria@gmail.com)
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  * 1. Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer
  * in the documentation and/or other materials provided with the
  * distribution.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  */
 package com.andune.minecraft.commonlib.log;
 
 import com.andune.minecraft.commonlib.LoggerFactory;
 
 import java.util.logging.FileHandler;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * @author andune
  */
 public class LogUtilJUL implements LogUtil {
     private boolean debugEnabled = false;
     private Level previousLevel = null;
     private Level previousRootLevel = null;
     private String baseLogger;
 
     @Override
    public void enableDebug(final String baseLogger) {
         if (!debugEnabled) {
             debugEnabled = true;
             this.baseLogger = baseLogger;
 
             previousLevel = Logger.getLogger(baseLogger).getLevel();
//            Logger.getLogger("com.andune.minecraft.hsp").setLevel(Level.ALL);
             Logger.getLogger(baseLogger).setLevel(Level.ALL);
 
             Handler handler = getRootFileHandler(Logger.getLogger("Minecraft"));
             previousRootLevel = handler.getLevel();
             handler.setLevel(Level.ALL);
 
             com.andune.minecraft.commonlib.Logger pluginLogger = LoggerFactory.getLogger(baseLogger);
             pluginLogger.debug("DEBUG ENABLED");
         }
     }
 
     @Override
     public void disableDebug() {
         if (debugEnabled) {
             debugEnabled = false;
             com.andune.minecraft.commonlib.Logger pluginLogger = LoggerFactory.getLogger(baseLogger);
             pluginLogger.debug("DEBUG DISABLED");
 
//            Logger.getLogger("com.andune.minecraft.hsp").setLevel(previousLevel);
             Logger.getLogger(baseLogger).setLevel(previousLevel);
             previousLevel = null;
 
             Handler handler = getRootFileHandler(Logger.getLogger(baseLogger));
             handler.setLevel(previousRootLevel);
             previousRootLevel = null;
         }
     }
 
     private static Handler getRootFileHandler(Logger log) {
         Handler handler = null;
 
         // recurse up to root logger right away
         Logger parent = log.getParent();
         if (parent != null)
             handler = getRootFileHandler(parent);
 
         // now from root logger on down, we look for the first
         // FileHandler we find
         if (handler == null) {
             Handler[] handlers = log.getHandlers();
             for (int i = 0; i < handlers.length; i++) {
                 if (handlers[i] instanceof FileHandler) {
                     handler = handlers[i];
                     break;
                 }
             }
         }
 
         return handler;
     }
 }
