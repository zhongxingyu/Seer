 package de.raion.xmppbot.plugin;
 /*
  * #%L
  * XmppBot Core
  * %%
  * Copyright (C) 2012 Bernd Kiefer
  * %%
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
 *
  *      http://www.apache.org/licenses/LICENSE-2.0
 *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  * #L%
  */
 
 import static de.raion.xmppbot.util.PacketUtils.getFromAddress;
 
 import java.io.PrintWriter;
 
 import org.jivesoftware.smack.Chat;
 import org.jivesoftware.smack.PacketListener;
 import org.jivesoftware.smack.filter.PacketFilter;
 import org.jivesoftware.smack.packet.Message;
 import org.jivesoftware.smack.packet.Packet;
 import org.jivesoftware.smackx.muc.MultiUserChat;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.raion.xmppbot.XmppBot;
 import de.raion.xmppbot.XmppContext;
import de.raion.xmppbot.io.ChatWriter;
 import de.raion.xmppbot.io.MultiUserChatWriter;
 
 /**
  * base class for MessageListenerPlugins. the class provides to correct
  * context for the subclasses.
  */
 public abstract class AbstractMessageListenerPlugin<T> implements PacketListener {
 	// static variables
 	/** default logger */
 	private static Logger log = LoggerFactory.getLogger(AbstractMessageListenerPlugin.class);
 	private XmppBot xmppBot;
 
 	/**
 	 * @param aXmppBot reference to the bot
 	 */
 	public AbstractMessageListenerPlugin(XmppBot aXmppBot) {
 		this.xmppBot = aXmppBot;
 	}
 
 	public void processPacket(Packet packet) {
 
 		if (packet instanceof Message) {
 
			log.debug("packet from '{}'", packet.getFrom());

 			final Message message = (Message) packet;
 
 			final XmppBot bot = xmppBot;
 			final MultiUserChat muc = xmppBot.getMultiUserChat(getFromAddress(packet));
			final Chat chat = xmppBot.getChat(packet.getFrom().trim());
 
 			Runnable runnable = new Runnable() {
 
 				@SuppressWarnings("synthetic-access")
 				public void run() {
 
 					Thread.currentThread().setName("Message: " + message.getBody());
 
 					PrintWriter threadPrintWriter = null;
					if(muc != null) {
						threadPrintWriter = new PrintWriter(new MultiUserChatWriter(muc));
					} else {
						threadPrintWriter = new PrintWriter(new ChatWriter(chat));
					}
 
 					bot.getContext().setMultiUserChat(muc);
 					bot.getContext().setPrintWriter(threadPrintWriter);
 					bot.getContext().setMessage(message);
 
 					log.debug("muc from context" + bot.getContext().getMultiUserChat());
 
 					if (muc != null) {
 						processMessage(bot.getContext(), muc, message);
 					} else {
 						processMessage(bot.getContext(), chat, message);
 					}
 
 					bot.getContext().removeMultiUserChat();
 					bot.getContext().removePrintWriter();
 				}
 			};
 
 			Thread t = new Thread(runnable);
 			t.start();
 		}
 	}
 
 
 	/**
 	 * @return context the plugin is running with
 	 */
 	public XmppContext getContext() { return xmppBot.getContext(); }
 
 	/**
 	 * @return the filter which defines what messages the listener consumes
 	 */
 	public abstract PacketFilter getAcceptFilter();
 
 	/**
 	 * processes a message
 	 *
 	 * @param xmppContext context the listener is running
 	 * @param chat
 	 *            the chat the message came from
 	 * @param message
 	 *            message to process
 	 */
 	public abstract void processMessage(XmppContext xmppContext, Chat chat, Message message);
 
 	/**
 	 * processes a message
 	 *
 	 * @param xmppContext context the listener is running
 	 * @param muc
 	 *            multiuserchat the message came from
 	 * @param message
 	 *            message to process
 	 */
 	public abstract void processMessage(XmppContext xmppContext, MultiUserChat muc, Message message);
 
 }
