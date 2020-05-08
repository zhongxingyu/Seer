 /*
  * Copyright (c) 2006-2015 DMDirc Developers
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
 
 package com.dmdirc.addons.debug.commands;
 
 import com.dmdirc.addons.debug.Debug;
 import com.dmdirc.addons.debug.DebugCommand;
 import com.dmdirc.commandparser.CommandArguments;
 import com.dmdirc.commandparser.commands.IntelligentCommand;
 import com.dmdirc.commandparser.commands.context.CommandContext;
 import com.dmdirc.interfaces.WindowModel;
 import com.dmdirc.ui.input.AdditionalTabTargets;
 
 import javax.annotation.Nonnull;
 import javax.inject.Inject;
 import javax.inject.Provider;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.slf4j.Marker;
 
 import static com.dmdirc.util.LogUtils.APP_ERROR;
 import static com.dmdirc.util.LogUtils.FATAL_APP_ERROR;
 import static com.dmdirc.util.LogUtils.FATAL_USER_ERROR;
 import static com.dmdirc.util.LogUtils.USER_ERROR;
 
 /**
  * Creates DMDirc errors with the specified parameters.
  */
 public class FakeError extends DebugCommand implements IntelligentCommand {
 
     private static final Logger LOG = LoggerFactory.getLogger(FakeError.class);
 
     @Inject
     public FakeError(final Provider<Debug> commandProvider) {
         super(commandProvider);
     }
 
     @Override
     public String getName() {
         return "error";
     }
 
     @Override
     public String getUsage() {
         return "<user|app> [<low|medium|high|fatal|unknown>] - Creates an error"
                 + " with the specified parameters, defaults to high priority.";
     }
 
     @Override
     public void execute(@Nonnull final WindowModel origin,
             final CommandArguments args, final CommandContext context) {
         if ((args.getArguments().length == 1
                 || args.getArguments().length == 2)
                 && "user".equals(args.getArguments()[0])) {
             raiseError(getLevel(args.getArguments()), false);
         } else if ((args.getArguments().length == 1
                 || args.getArguments().length == 2)
                 && "app".equals(args.getArguments()[0])) {
             raiseError(getLevel(args.getArguments()), true);
         } else {
             showUsage(origin, args.isSilent(), getName(), getUsage());
         }
     }
 
     private void raiseError(final String level, final boolean appError) {
         final Marker marker = appError ? APP_ERROR : USER_ERROR;
         switch (level.toUpperCase()) {
             case "FATAL":
                LOG.error(appError ? FATAL_APP_ERROR : FATAL_USER_ERROR, "Debug error message");
                 break;
             case "HIGH":
                LOG.error(marker, "Debug error message");
                 break;
             case "MEDIUM":
                LOG.warn(marker, "Debug error message");
                 break;
             case "INFO":
                LOG.info(marker, "Debug error message");
                 break;
             default:
                LOG.info(marker, "Debug error message");
         }
     }
 
     /**
      * Returns the error level specified by the provided arguments.
      *
      * @param args command arguments
      *
      * @return Error level
      */
     private String getLevel(final String... args) {
         if (args.length >= 2) {
             return args[1].toUpperCase();
         } else {
             return "HIGH";
         }
     }
 
     @Override
     public AdditionalTabTargets getSuggestions(final int arg,
             final IntelligentCommandContext context) {
         final AdditionalTabTargets res = new AdditionalTabTargets();
         res.excludeAll();
 
         if (arg == 1) {
             res.add("user");
             res.add("app");
         } else if (arg == 2) {
             res.add("low");
             res.add("medium");
             res.add("high");
             res.add("fatal");
         }
 
         return res;
     }
 
 }
