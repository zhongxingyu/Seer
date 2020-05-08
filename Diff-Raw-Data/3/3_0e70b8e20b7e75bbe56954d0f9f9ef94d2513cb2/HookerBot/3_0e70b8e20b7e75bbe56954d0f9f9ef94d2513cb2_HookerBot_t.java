 /*
  * HookerBot.java
  *
  * Copyright (C) 2012 Matthew Khouzam
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 
 package com.matthew.hookersandblackjack;
 
 import java.util.HashMap;
 import java.util.Random;
 
 import org.jibble.pircbot.DccChat;
 import org.jibble.pircbot.DccFileTransfer;
 import org.jibble.pircbot.PircBot;
 
 public class HookerBot extends PircBot {
 	String names[] = { "Alice", "Bertha", "Coraline", "Delphine", "Elanor",
 			"Fatima", "Gertrude", "Hildegarte", "Iris", "Julianna", "Karine",
 			"Latticia" };
 	Random rnd = new Random(System.nanoTime());
 
 	class LastAction {
 		public LastAction(String action) {
 			name = action;
 			cost = name.hashCode() % 80 + 20;
 		}
 
 		public int getPrice() {
 			return cost;
 		}
 
 		final String name;
 		final int cost;
 	}
 
 	public HookerBot() {
		this.setName("HookerBot_" + names[rnd.nextInt(names.length)]);
 	}
 
 	@Override
 	protected void onConnect() {
 		// TODO Auto-generated method stub
 		super.onConnect();
 	}
 
 	@Override
 	protected void onDisconnect() {
 		// TODO Auto-generated method stub
 		super.onDisconnect();
 	}
 
 	HashMap<String, LastAction> custList = new HashMap<String, LastAction>();
 	HMDB playerDB;
 
 	public HMDB getPlayerDB() {
 		return playerDB;
 	}
 
 	public void setPlayerDB(HMDB playerDB) {
 		this.playerDB = playerDB;
 	}
 
 	@Override
 	protected void onMessage(String channel, String sender, String login,
 			String hostname, String message) {
 		super.onMessage(channel, sender, login, hostname, message);
 		if (message.toLowerCase().startsWith("!price")) {
 			String action = message.substring(6);
 			LastAction la = new LastAction(action);
 			custList.put(sender, la);
 			sendMessage(channel, sender + ": That will cost " + la.getPrice()
 					+ " Lttng dollars");
 		} else if (message.toLowerCase().startsWith("!accept")) {
 			LastAction lastAction = custList.get(sender);
 			if (lastAction != null) {
 				Long currentCash = playerDB.get(sender);
 				if (currentCash > lastAction.getPrice()) {
 					sendMessage(channel, sender + ": Ok");
 					sendAction(channel, " does " + lastAction.name
 							+ " to " + sender);
 					playerDB.put(sender,
 							currentCash
 									- lastAction.getPrice());
 				} else {
 					sendMessage(channel, "Hey everyone, " + sender
 							+ " can't even afford a "
 							+ lastAction.name + "!");
 				}
 			} else {
 				sendMessage(channel, sender
 						+ ": You haven't told me what you want to do");
 			}
 		}
 	}
 
 	@Override
 	protected void onJoin(String channel, String sender, String login,
 			String hostname) {
 		// TODO Auto-generated method stub
 		super.onJoin(channel, sender, login, hostname);
 	}
 
 	@Override
 	protected void onKick(String channel, String kickerNick,
 			String kickerLogin, String kickerHostname, String recipientNick,
 			String reason) {
 		// TODO Auto-generated method stub
 		super.onKick(channel, kickerNick, kickerLogin, kickerHostname,
 				recipientNick, reason);
 	}
 
 	@Override
 	protected void onQuit(String sourceNick, String sourceLogin,
 			String sourceHostname, String reason) {
 		// TODO Auto-generated method stub
 		super.onQuit(sourceNick, sourceLogin, sourceHostname, reason);
 	}
 
 	@Override
 	protected void onOp(String channel, String sourceNick, String sourceLogin,
 			String sourceHostname, String recipient) {
 		// TODO Auto-generated method stub
 		super.onOp(channel, sourceNick, sourceLogin, sourceHostname, recipient);
 	}
 
 	@Override
 	protected void onDeop(String channel, String sourceNick,
 			String sourceLogin, String sourceHostname, String recipient) {
 		// TODO Auto-generated method stub
 		super.onDeop(channel, sourceNick, sourceLogin, sourceHostname,
 				recipient);
 	}
 
 	@Override
 	protected void onDeVoice(String channel, String sourceNick,
 			String sourceLogin, String sourceHostname, String recipient) {
 		// TODO Auto-generated method stub
 		super.onDeVoice(channel, sourceNick, sourceLogin, sourceHostname,
 				recipient);
 	}
 
 	@Override
 	protected void onInvite(String targetNick, String sourceNick,
 			String sourceLogin, String sourceHostname, String channel) {
 		// TODO Auto-generated method stub
 		super.onInvite(targetNick, sourceNick, sourceLogin, sourceHostname,
 				channel);
 	}
 
 	@Override
 	protected void onDccChatRequest(String sourceNick, String sourceLogin,
 			String sourceHostname, long address, int port) {
 		this.sendMessage(sourceNick,
 				"I don't think I can handle you in a one on one situation");
 	}
 
 	@Override
 	protected void onIncomingFileTransfer(DccFileTransfer transfer) {
 		// TODO Auto-generated method stub
 		super.onIncomingFileTransfer(transfer);
 	}
 
 	@Override
 	protected void onIncomingChatRequest(DccChat chat) {
 		// TODO Auto-generated method stub
 		super.onIncomingChatRequest(chat);
 	}
 
 	@Override
 	protected void onPing(String sourceNick, String sourceLogin,
 			String sourceHostname, String target, String pingValue) {
 		// TODO Auto-generated method stub
 		super.onPing(sourceNick, sourceLogin, sourceHostname, target, pingValue);
 	}
 
 	@Override
 	protected void onFinger(String sourceNick, String sourceLogin,
 			String sourceHostname, String target) {
 		sendAction(this.getChannels()[0], "slaps " + sourceNick);
 		super.onFinger(sourceNick, sourceLogin, sourceHostname, target);
 	}
 
 }
