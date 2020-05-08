 /*
  *  Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
  *
  *  Permission is hereby granted, free of charge, to any person obtaining a copy
  *  of this software and associated documentation files (the "Software"), to deal
  *  in the Software without restriction, including without limitation the rights
  *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  *  copies of the Software, and to permit persons to whom the Software is
  *  furnished to do so, subject to the following conditions:
  *
  *  The above copyright notice and this permission notice shall be included in
  *  all copies or substantial portions of the Software.
  *
  *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  *  SOFTWARE.
  */
 
 package com.dmdirc.addons.parser_twitter;
 
 import com.dmdirc.actions.ActionManager;
 import com.dmdirc.addons.parser_twitter.actions.TwitterActionComponents;
 import com.dmdirc.config.prefs.PluginPreferencesCategory;
 import com.dmdirc.config.prefs.PreferencesCategory;
 import com.dmdirc.config.prefs.PreferencesManager;
 import com.dmdirc.config.prefs.PreferencesSetting;
 import com.dmdirc.config.prefs.PreferencesType;
 import com.dmdirc.parser.common.MyInfo;
 import com.dmdirc.parser.interfaces.Parser;
 import com.dmdirc.parser.interfaces.ProtocolDescription;
 import com.dmdirc.plugins.Plugin;
 import java.net.URI;
 import java.util.ArrayList;
 
 /**
  *
  * @author shane
  */
 public class TwitterPlugin extends Plugin  {
     /** Are we currently unloading? */
     private static boolean unloading = false;
 
     /**
      * Create a TwitterPlugin
      */
     public TwitterPlugin() { }
 
     /** {@inheritDoc} */
     @Override
     public void onLoad() {
         ActionManager.registerActionComponents(TwitterActionComponents.values());
     }
 
     /** {@inheritDoc} */
     @Override
     public void onUnload() {
         unloading = true;
         for (Twitter parser : new ArrayList<Twitter>(Twitter.currentParsers)) {
             parser.disconnect("");
         }
     }
 
     /**
      * Get a Twitter parser instance.
      *
      * @param myInfo The client information to use
      * @param address The address of the server to connect to
      * @return An appropriately configured parser
      */
     public Parser getParser(final MyInfo myInfo, final URI address) {
         return (unloading) ? null : new Twitter(myInfo, address, this);
     }
 
     /**
      * Retrieves a protocol description for the twitter protocol.
      *
      * @return A description of the twitter protocol
      * @since 0.6.4
      */
     public ProtocolDescription getDescription() {
         return new TwitterProtocolDescription();
     }
 
     /** {@inheritDoc} */
     @Override
     public void showConfig(final PreferencesManager manager) {
         final PreferencesCategory category = new PluginPreferencesCategory(
                 getPluginInfo(), "Twitter Plugin", "Settings related to the twitter plugin",
                 "category-twitter");
         final PreferencesCategory advanced = new PluginPreferencesCategory(
                 getPluginInfo(), "Advanced", "Advanced Settings related to the twitter plugin",
                 "category-twitter");
 
        category.addSetting(new PreferencesSetting(PreferencesType.INTEGER, getDomain(), "statuscount", "Statuses to request", "How many statuses to request at a time.?"));
         category.addSetting(new PreferencesSetting(PreferencesType.INTEGER, getDomain(), "apicalls", "API Calls", "Aim to only use how many API Calls per hour? (Twitter has a max of 150)"));
         category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN, getDomain(), "saveLastIDs", "Remember shown items", "Should previously shown items not be shown again next time?"));
         category.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN, getDomain(), "getSentMessages", "Show own Direct Messages", "Should we try to show our own direct messages to people not just ones to us?"));
 
         advanced.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN, getDomain(), "autoAt", "Prepend nickanmes with @", "Should all nicknmaes be shown with an @ infront of them? (Makes tab competion easier)"));
         advanced.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN, getDomain(), "replaceOpeningNickname", "Replace opening nickame?", "Should nicknames at the start of the message be replaced? (eg Replace foo: with @foo)"));
         advanced.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN, getDomain(), "debugEnabled", "Debugging Enabled?", "Should more debugging be enabled on the twitter plugin?"));
         advanced.addSetting(new PreferencesSetting(PreferencesType.BOOLEAN, getDomain(), "hide500Errors", "Hide HTTP 50x Errors", "At times twitter gives a lot of 502/503 errors. Should this be hidden from you?"));
 
         category.addSubCategory(advanced);
 
         manager.getCategory("Plugins").addSubCategory(category);
     }
 }
