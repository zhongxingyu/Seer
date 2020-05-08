 /*
  * Copyright (c) 2006-2014 DMDirc Developers
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
 
 package com.dmdirc.addons.parser_xmpp;
 
 import com.dmdirc.parser.common.BaseChannelInfo;
 import com.dmdirc.parser.common.ChannelListModeItem;
 import com.dmdirc.parser.interfaces.ChannelClientInfo;
 import com.dmdirc.parser.interfaces.ClientInfo;
 import com.dmdirc.parser.interfaces.Parser;
 import com.dmdirc.parser.interfaces.callbacks.ChannelNamesListener;
 
 import java.util.Collection;
 
 /**
  * A 'fake' local channel used to display buddy lists.
  */
 public class XmppFakeChannel extends BaseChannelInfo {
 
     /**
      * Creates a new fake channel belonging to the specified parser and with the given name.
      *
      * @param parser The XMPP parser that owns this channel
      * @param name   The name of the channel
      */
     public XmppFakeChannel(final Parser parser, final String name) {
         super(parser, name);
     }
 
     @Override
     public void setTopic(final String topic) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public String getTopic() {
         return "No topic yet!";
     }
 
     @Override
     public long getTopicTime() {
         return System.currentTimeMillis(); // TODO
     }
 
     @Override
     public String getTopicSetter() {
         return ""; // TODO
     }
 
     @Override
     public String getModes() {
         return ""; // TODO
     }
 
     @Override
     public String getMode(final char mode) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public Collection<ChannelListModeItem> getListMode(final char mode) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void part(final String reason) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void sendWho() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void alterMode(final boolean add, final Character mode, final String parameter) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void flushModes() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void requestListModes() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public ChannelClientInfo getChannelClient(final ClientInfo client) {
         return getClient(client.getNickname());
     }
 
     @Override
     public ChannelClientInfo getChannelClient(final String client, final boolean create) {
         final String[] parts = getParser().parseHostmask(client);
 
         if (create && getClient(parts[0]) == null) {
             return new XmppChannelClientInfo(this, getParser().getClient(client));
         }
 
         return getClient(parts[0]);
     }
 
     /**
      * Updates this channel with the specified contacts.
      *
      * @param clients The contacts to be added
      */
     public void updateContacts(final Collection<XmppClientInfo> clients) {
         for (XmppClientInfo client : clients) {
             addClient(client.getNickname(), new XmppChannelClientInfo(this, client));
         }
 
         // TODO: Delete old contacts, don't needlessly create new objects
        getParser().getCallbackManager().getCallbackType(ChannelNamesListener.class).call(this);
     }
 
 }
