 /*
  * Copyright (c) 2006-2009 Chris Smith, Shane Mc Cormack, Gregory Holmes
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
 
 package com.dmdirc.addons.relaybot;
 
 import com.dmdirc.Channel;
 import com.dmdirc.ChannelEventHandler;
 import com.dmdirc.parser.common.CallbackManager;
 import com.dmdirc.parser.common.CallbackNotFoundException;
 import com.dmdirc.parser.common.CallbackObject;
 import com.dmdirc.parser.interfaces.Parser;
 import com.dmdirc.parser.interfaces.callbacks.CallbackInterface;
 import com.dmdirc.parser.interfaces.callbacks.ChannelMessageListener;
 import com.dmdirc.parser.interfaces.callbacks.SocketCloseListener;
 import com.dmdirc.parser.irc.IRCCallbackManager;
 import com.dmdirc.parser.irc.IRCParser;
 
 import java.lang.reflect.Field;
 import java.util.Date;
 import java.util.Map;
 
 /**
  * This is a callback manager that when created will replace the callback
  * manager of the given parser with itself.
  *
  * When a new callback is added it will be allowed unless the RelayBotPlugin
  * says otherwise.
  *
  * When the parser disconnects, the original CBM is restored.
  *
  * @author shane
  */
 public class RelayCallbackManager extends IRCCallbackManager implements SocketCloseListener {
 
     /** Pluign that created this callback manager. */
     private final RelayBotPlugin myPlugin;
 
     /** Original CallbackManager */
     private final IRCCallbackManager originalCBM;
 
     /**
      * Create a new RelayCallbackManager and replace
      *
      * @param myPlugin
      * @param parser
      */
     public RelayCallbackManager(final RelayBotPlugin myPlugin, final IRCParser parser) {
         super(parser);
 
         this.myPlugin = myPlugin;
         this.originalCBM = (IRCCallbackManager) parser.getCallbackManager();
         setCallbackManager(parser, this);
         addCallback(SocketCloseListener.class, this);
     }
 
     /** {@inheritDoc} */
     @Override
     public <T extends CallbackInterface> void addCallback(final Class<T> callback, final T o, final String target) throws CallbackNotFoundException {
         // Don't allow the core to give itself a ChannelMessageListener if we
         // already have a listener for this channel.
         if (o instanceof ChannelEventHandler && callback == ChannelMessageListener.class) {
             try {
                 // Get the old callback manager
                 final Field field = o.getClass().getDeclaredField("owner");
                 field.setAccessible(true);
                 final Channel channel = (Channel) field.get(o);
 
                 if (myPlugin.isListening(channel)) {
                     return;
                 }
             } catch (NoSuchFieldException ex) {
                 ex.printStackTrace();
             } catch (SecurityException ex) {
                 ex.printStackTrace();
             } catch (IllegalArgumentException ex) {
                 ex.printStackTrace();
             } catch (IllegalAccessException ex) {
                 ex.printStackTrace();
             }
         }
 
         // Unless the plugin says we are listening instead of this channel, then
         // we can add the callback.
         forceAddCallback(callback, o, target);
     }
 
     /**
      * Add a callback with a specific target.
      * This method will throw a CallbackNotFoundException if the callback does not exist.
      *
      * @param <T> The type of callback
      * @param callback Type of callback object.
      * @param o instance of ICallbackInterface to add.
      * @param target Parameter to specify that a callback should only fire for specific things
      * @throws CallbackNotFoundException If callback is not found.
      * @throws NullPointerException If 'o' is null
      */
     public <T extends CallbackInterface> void forceAddCallback(final Class<T> callback, final T o, final String target) throws CallbackNotFoundException {
         super.addCallback(callback, o, target);
     }
 
 
     /**
      * Set the Callback Manager of a given parser.
      *
      * @param parser
      * @param cbm
      */
     private void setCallbackManager(final IRCParser parser, final CallbackManager<IRCParser> cbm) {
         try {
             // Get the old callback manager
             final Field field = parser.getClass().getDeclaredField("myCallbackManager");
             field.setAccessible(true);
             @SuppressWarnings("unchecked")
             final CallbackManager<IRCParser> oldCBM = (CallbackManager<IRCParser>) field.get(parser);
 
             // Clone the known CallbackObjects list (horrible code ahoy!)
             // First get the old map of callbacks
             final Field cbField = CallbackManager.class.getDeclaredField("callbackHash");
             cbField.setAccessible(true);
             @SuppressWarnings("unchecked")
             final Map<Class<? extends CallbackInterface>, CallbackObject> oldCallbackHash = (Map<Class<? extends CallbackInterface>, CallbackObject>) cbField.get(oldCBM);
 
             // Clear my map of callbacks
             @SuppressWarnings("unchecked")
            final Map<Class<? extends CallbackInterface>, CallbackObject> myCallbackHash = (Map<Class<? extends CallbackInterface>, CallbackObject>) cbField.get(cbm);
             myCallbackHash.clear();
 
             // Now add them all to the new cbm.
             for (CallbackObject callback : oldCallbackHash.values()) {
                 // Change their manager to the new one.
                 final Field ownerField = CallbackObject.class.getDeclaredField("myManager");
                 ownerField.setAccessible(true);
                 ownerField.set(callback, cbm);
 
                 // And add them to the CBM
                 cbm.addCallbackType(callback);
             }
 
             // Replace the old one with the new one.
             field.set(parser, cbm);
         } catch (IllegalArgumentException ex) {
             ex.printStackTrace();
         } catch (IllegalAccessException ex) {
             ex.printStackTrace();
         } catch (NoSuchFieldException ex) {
             ex.printStackTrace();
         } catch (SecurityException ex) {
             ex.printStackTrace();
         }
     }
 
     /**
      * When the socket closes, reset the callback manager.
      *
      * @param parser
      */
     @Override
     public void onSocketClosed(final Parser parser, final Date date) {
         if (parser.getCallbackManager() instanceof RelayCallbackManager) {
             setCallbackManager((IRCParser)parser, originalCBM);
         }
     }
 }
