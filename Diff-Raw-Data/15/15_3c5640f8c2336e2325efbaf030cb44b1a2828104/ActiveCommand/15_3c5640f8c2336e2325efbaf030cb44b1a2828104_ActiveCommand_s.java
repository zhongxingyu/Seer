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
 
 package com.dmdirc.addons.activewindow;
 
import com.dmdirc.FrameContainer;
 import com.dmdirc.addons.ui_swing.interfaces.ActiveFrameManager;
 import com.dmdirc.commandparser.BaseCommandInfo;
 import com.dmdirc.commandparser.CommandArguments;
 import com.dmdirc.commandparser.CommandInfo;
 import com.dmdirc.commandparser.CommandType;
 import com.dmdirc.commandparser.commands.Command;
 import com.dmdirc.commandparser.commands.IntelligentCommand;
 import com.dmdirc.commandparser.commands.context.CommandContext;
 import com.dmdirc.interfaces.CommandController;
 import com.dmdirc.interfaces.WindowModel;
 import com.dmdirc.ui.input.AdditionalTabTargets;
 import com.dmdirc.ui.input.TabCompleterUtils;
 
 import javax.annotation.Nonnull;
 import javax.inject.Inject;
 
 /**
  * Executes another command as if it were executed in the active window.
  */
 public class ActiveCommand extends Command implements IntelligentCommand {
 
     /** A command info object for this command. */
     public static final CommandInfo INFO = new BaseCommandInfo("active",
             "active <command> - executes the command as though it had been "
             + "executed in the active window", CommandType.TYPE_GLOBAL);
     /** Active frame manager. */
     private final ActiveFrameManager activeFrameManager;
     /** Tab-completer utilities. */
     private final TabCompleterUtils tabUtils;
 
     /**
      * Creates a new active command.
      *
      * @param controller          The controller to use for command information.
      * @param activeFrameManager The active window manager
      */
     @Inject
     public ActiveCommand(
             final CommandController controller,
             final ActiveFrameManager activeFrameManager,
             final TabCompleterUtils tabUtils) {
         super(controller);
 
         this.activeFrameManager = activeFrameManager;
         this.tabUtils = tabUtils;
     }
 
     @Override
     public void execute(@Nonnull final WindowModel origin,
             final CommandArguments args, final CommandContext context) {
         activeFrameManager.getActiveFrame().ifPresent(f -> f.getContainer().getCommandParser()
                .parseCommand((FrameContainer) f.getContainer(), args.getArgumentsAsString()));
     }
 
     @Override
     public AdditionalTabTargets getSuggestions(final int arg,
             final IntelligentCommandContext context) {
         return tabUtils.getIntelligentResults(arg, context, 0);
     }
 
 }
