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
 
 import com.dmdirc.ClientModule.GlobalConfig;
 import com.dmdirc.DMDircMBassador;
 import com.dmdirc.addons.debug.Debug;
 import com.dmdirc.addons.debug.DebugCommand;
 import com.dmdirc.commandparser.CommandArguments;
 import com.dmdirc.commandparser.commands.context.CommandContext;
 import com.dmdirc.interfaces.WindowModel;
 import com.dmdirc.interfaces.config.AggregateConfigProvider;
 import com.dmdirc.interfaces.config.IdentityController;
 import com.dmdirc.ui.messages.Styliser;
 import com.dmdirc.updater.UpdateChecker;
 import com.dmdirc.updater.manager.CachingUpdateManager;
 
 import javax.annotation.Nonnull;
 import javax.inject.Inject;
 import javax.inject.Provider;
 
 /**
  * Forces the client to check for an update.
  */
 public class ForceUpdate extends DebugCommand {
 
     /** The global configuration used to check if updates are enabled. */
     private final AggregateConfigProvider globalConfig;
     /** The controller to use to read/write settings for the updater. */
     private final IdentityController identityController;
     /** The update manager to use when forcing an update. */
     private final CachingUpdateManager updateManager;
     /** The event bus to post errors to. */
     private final DMDircMBassador eventBus;
 
     /**
      * Creates a new instance of the command.
      *
      * @param commandProvider    The provider to use to access the main debug command.
      * @param globalConfig       The global config to use to check if updates are enabled.
      * @param identityController The controller to use to read/write settings for the updater.
      * @param updateManager      The update manager to use when forcing an update.
      * @param eventBus           The event bus to post errors to
      */
     @Inject
     public ForceUpdate(
             final Provider<Debug> commandProvider,
             @GlobalConfig final AggregateConfigProvider globalConfig,
             final IdentityController identityController,
             final CachingUpdateManager updateManager,
             final DMDircMBassador eventBus) {
         super(commandProvider);
 
         this.globalConfig = globalConfig;
         this.identityController = identityController;
         this.updateManager = updateManager;
         this.eventBus = eventBus;
     }
 
     @Override
     public String getName() {
         return "forceupdate";
     }
 
     @Override
     public String getUsage() {
         return " - Forces a client update check";
     }
 
     @Override
     public void execute(@Nonnull final WindowModel origin,
             final CommandArguments args, final CommandContext context) {
         if (globalConfig.getOptionBool("updater", "enable")) {
            UpdateChecker.checkNow(updateManager, identityController, "Forced update checker");
         } else {
             sendLine(origin, args.isSilent(), FORMAT_ERROR, "Update checking is "
                     + "currently disabled.  You can enable it by typing:");
             sendLine(origin, args.isSilent(), FORMAT_ERROR, Styliser.CODE_FIXED
                     + "    /set updater enable true");
         }
     }
 
 }
