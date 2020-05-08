 /*
  * Copyright (c) 2006-2011 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
 
 package com.dmdirc.addons.osd;
 
 import com.dmdirc.FrameContainer;
 import com.dmdirc.commandparser.CommandArguments;
 import com.dmdirc.commandparser.CommandInfo;
 import com.dmdirc.commandparser.CommandType;
 import com.dmdirc.commandparser.commands.Command;
 import com.dmdirc.commandparser.commands.IntelligentCommand;
 import com.dmdirc.commandparser.commands.context.CommandContext;
 import com.dmdirc.ui.input.AdditionalTabTargets;
 import com.dmdirc.ui.messages.Styliser;
 
 /**
  * The osd command shows an on screen message.
  */
 public final class OsdCommand extends Command implements
         IntelligentCommand, CommandInfo {
 
     /** The OSDManager that this command should use. */
     private final OsdManager osdManager;
 
     /**
      * Creates a new instance of OsdCommand.
      *
      * @param osdManager OSD Manager used to control OSD windows
      */
     public OsdCommand(final OsdManager osdManager) {
         super();
 
         this.osdManager = osdManager;
     }
 
     /**
      * Used to show a notification using this plugin.
      *
      * @param timeout Timeout for the OSD window. If negative then the value
      * from the config will be used
      * @param title Title of dialog if applicable
      * @param message Message to show
      *
      * @return True if the notification was shown.
      */
     public boolean showOSD(final int timeout, final String title,
             final String message) {
         osdManager.showWindow(timeout, Styliser.stipControlCodes(message));
         return true;
     }
 
     /** {@inheritDoc} */
     @Override
     public void execute(final FrameContainer<?> origin,
             final CommandArguments args, final CommandContext context) {
         if (args.getArguments().length > 0
                 && "--close".equalsIgnoreCase(args.getArguments()[0])) {
             osdManager.closeAll();
         } else if (args.getArguments().length > 0
                 && "--timeout".equalsIgnoreCase(args.getArguments()[0])) {
             if (args.getArguments().length < 2) {
                 sendLine(origin, args.isSilent(), FORMAT_ERROR, "You "
                         + "must specify a valid number for the OSD timeout.");
                 return;
             }
 
             try {
                 showOSD(Integer.parseInt(args.getArguments()[1]), null,
                         args.getArgumentsAsString(2));
             } catch (NumberFormatException ex) {
                 sendLine(origin, args.isSilent(), FORMAT_ERROR, "You "
                         + "must specify a valid number for the OSD timeout.");
             }
         } else {
             showOSD(-1 , null, args.getArgumentsAsString());
         }
     }
 
     /** {@inheritDoc}. */
     @Override
     public String getName() {
         return "osd";
     }
 
     /** {@inheritDoc}. */
     @Override
     public boolean showInHelp() {
         return true;
     }
 
     /** {@inheritDoc} */
     @Override
     public CommandType getType() {
         return CommandType.TYPE_GLOBAL;
     }
 
     /** {@inheritDoc}. */
     @Override
     public String getHelp() {
         return "osd --close - closes all OSD windows\n"
                + "osd [--timeout <delay in seconds>] <message> - show the "
                 + "specified message in an OSD window";
     }
 
     /** {@inheritDoc} */
     @Override
     public AdditionalTabTargets getSuggestions(final int arg,
             final IntelligentCommandContext context) {
         final AdditionalTabTargets res = new AdditionalTabTargets();
 
         if (arg == 0) {
             res.add("--close");
             res.add("--timeout");
         } else if (arg > 0 && context.getPreviousArgs().get(0)
                 .equals("--close")) {
             res.excludeAll();
         }
 
         return res;
     }
 
 }
