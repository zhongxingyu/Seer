 /*
  * Copyright (c) 2006-2012 DMDirc Developers
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.nma;
 
 import com.dmdirc.FrameContainer;
 import com.dmdirc.commandparser.BaseCommandInfo;
 import com.dmdirc.commandparser.CommandArguments;
 import com.dmdirc.commandparser.CommandType;
 import com.dmdirc.commandparser.commands.Command;
 import com.dmdirc.commandparser.commands.context.CommandContext;
 
 import java.io.IOException;
 
 import lombok.Setter;
 import lombok.extern.slf4j.Slf4j;
 
 /**
  * Command to raise notifications with NotifyMyAndroid.
  */
 @Slf4j
 public class NotifyMyAndroidCommand extends Command {
 
     /** A command info object for this command. */
     public static final BaseCommandInfo INFO = new BaseCommandInfo(
             "notifymyandroid",
             "notifymyandroid <title> -- <body>"
                 + " - Sends notification to NotifyMyAndroid",
             CommandType.TYPE_GLOBAL);
 
     /** The configuration domain to retrieve settings from. */
     @Setter
     private String configDomain;
 
     /** {@inheritDoc} */
     @Override
     public void execute(final FrameContainer origin,
             final CommandArguments args, final CommandContext context) {
         final String[] parts = args.getArgumentsAsString().split("\\s+--\\s+", 2);
         log.trace("Split input: {}", parts);
 
         if (parts.length != 2) {
             showUsage(origin, args.isSilent(), INFO.getName(), INFO.getHelp());
         }
 
         log.trace("Retrieving settings from domain '{}'", configDomain);
         final NotifyMyAndroidClient client = new NotifyMyAndroidClient(
                 origin.getConfigManager().getOption(configDomain, "apikey"),
                 origin.getConfigManager().getOption(configDomain, "application"));
 
        try {
            client.notify(parts[0], parts[1]);
            sendLine(origin, args.isSilent(), FORMAT_OUTPUT, "Notification sent");
        } catch (IOException ex) {
            log.info("Exception when trying to notify NMA", ex);
            sendLine(origin, args.isSilent(), FORMAT_ERROR, "Unable to send: " + ex.getMessage());
        }
     }
 
 }
