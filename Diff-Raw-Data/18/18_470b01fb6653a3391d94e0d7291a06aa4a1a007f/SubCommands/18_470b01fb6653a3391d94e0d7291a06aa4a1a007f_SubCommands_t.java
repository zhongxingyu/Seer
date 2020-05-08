 /*
  *  LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
  */
 package de.weltraumschaf.juberblog.cmd;
 
 import com.google.common.collect.Maps;
 import de.weltraumschaf.commons.IO;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.Map;
 import org.apache.commons.lang3.Validate;
 
 /**
  * Available sub commands.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public enum SubCommands {
 
     CREATE("create"),
     PUBLISH("publish"),
     INSTALL("install");
 
     private static final Map<String, SubCommands> LOOKUP = Maps.newHashMap();
     static {
         for (final SubCommands cmd : values()) {
             LOOKUP.put(cmd.getSubCommandName(), cmd);
         }
     }
     private final String subCommandName;
 
     SubCommands(final String subCommandName) {
         Validate.notEmpty(subCommandName, "Sub command name must not be null or empty!");
         this.subCommandName = subCommandName;
     }
 
     public String getSubCommandName() {
         return subCommandName;
     }
 
     public static SubCommands forSubCommandName(final String subCommandName) {
        final String normalizedName = subCommandName.trim().toLowerCase();

         if (LOOKUP.containsKey(normalizedName)) {
             return LOOKUP.get(normalizedName);
         }
 
         throw new IllegalArgumentException(String.format("Unknown command '%s'!", subCommandName));
     }
 
     public static SubCommand create(final SubCommands type, final IO io) throws IOException, URISyntaxException {
         switch (type) {
             case CREATE:
                 return new CreateSubCommand(io);
             case PUBLISH:
                 return new PublishSubCommand(io);
             case INSTALL:
                 return new InstallSubCommand(io);
             default:
                 throw new IllegalArgumentException(String.format("Unknown command type '%s'!", type));
         }
     }
 }
