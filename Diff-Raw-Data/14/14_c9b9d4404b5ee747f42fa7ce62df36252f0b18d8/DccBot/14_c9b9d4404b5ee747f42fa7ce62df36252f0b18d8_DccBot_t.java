 /*
  * Project: xdccBee
  * Copyright (C) 2009 snert@snert-lab.de,
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package de.snertlab.xdccBee.irc;
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import org.jibble.pircbot.DccFileTransfer;
 import org.jibble.pircbot.PircBot;
 
 import de.snertlab.xdccBee.irc.listener.LogMessage;
 import de.snertlab.xdccBee.irc.listener.NotifyManagerConnectedState;
 import de.snertlab.xdccBee.irc.listener.NotifyManagerDccBotLogging;
 import de.snertlab.xdccBee.irc.listener.NotifyManagerDccDownload;
 import de.snertlab.xdccBee.irc.listener.NotifyManagerDccPacket;
 
 /**
  * @author snert
  *
  */
 public class DccBot extends PircBot{
 	
 	public List<String> listChannelsJoined;
 	private IrcServer ircServer;
 	
 	public DccBot(IrcServer ircServer, String botName, String botVersion) {
 		this.listChannelsJoined = new ArrayList<String>();
 		this.ircServer = ircServer;
 		setVersion(botVersion);
 		setLogin(botName);
 	}
 	
 	public void setNickname(String nickname){
 		this.setName(nickname);
 	}
 	
 	@Override
 	public void log(String message) {
 		log( new LogMessage("[" + getLogTime() + "]" + " " + message , LogMessage.LOG_COLOR_DCCBOT_MESSAGE)); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 	}
 		
 	@Override
 	protected void onMessage(String channel, String sender, String login, String hostname, String message) {
 		//TODO: Wenn sich ein bot deconnected dann dies in Tabelle anzeigen => onQuit
 		if( DccMessageParser.isDccMessage(message) ){
 			DccPacket dccPacket = DccMessageParser.buildDccPacket(sender, message);
 			dccPacket.setDccBot(this);
 			IrcChannel ircChannel = ircServer.getChannelByName(channel);
 			dccPacket.setIrcChannel(ircChannel);
 			if( ! ircServer.containsPacket(dccPacket) ){
 				ircServer.addDccPacket(dccPacket);
 				ircChannel.addDccPacket(dccPacket);
 				log(new LogMessage(dccPacket.getName(), LogMessage.LOG_COLOR_DCC_MESSAGE));
 				NotifyManagerDccPacket.getNotifyManager().notifyNewPackage(dccPacket);
 			}
 		}else{
 			log(new LogMessage("KEINE DCC MESSAGE: " + message, LogMessage.LOG_COLOR_NO_DCC_MESSAGE)); //$NON-NLS-1$
 		}
 	}
 	
 	private void log(LogMessage message) {
 		NotifyManagerDccBotLogging.getNotifyManager().notify(ircServer, message);
 	}
 	
 	@Override
 	protected void onJoin(String channel, String sender, String login, String hostname) {
 		if( this.getName().equals(sender) ){
 			listChannelsJoined.add(channel.toUpperCase());
 			NotifyManagerConnectedState.getNotifyManager().notify(ircServer.getChannelByName(channel));
 		}
 	}
 	
 	@Override
 	protected void onPart(String channel, String sender, String login, String hostname) {
 		if( this.getName().equals(sender) ){
			removeChannelFromJoinedChannelList(channel);
 			NotifyManagerConnectedState.getNotifyManager().notify(ircServer.getChannelByName(channel));
 		}
 	}
 	
 	protected void onConnect() {
 		super.onConnect();
 		List<IrcChannel> listIrcChannels = ircServer.getListChannels();
 		for (IrcChannel ircChannel : listIrcChannels) {
 			if(ircChannel.isAutoconnect()){
 				joinChannel(ircChannel.getChannelName()); //TODO: Nicht ganz sauber, aber ircChannel.connect() kann nicht aufgerufen werden
 				                                          //da IRC Server threadBotConnect noch laeuft
 			}
 		}
 		NotifyManagerConnectedState.getNotifyManager().notify(ircServer);
 	}
 	
 	@Override
 	protected void onDisconnect() {
 		ircServer.disconnect();
 		super.onDisconnect();
 		NotifyManagerConnectedState.getNotifyManager().notify(ircServer);
 	}
 	
 	//TODO: kick oder ban abfangen??
 	
 	public boolean isChannelJoined(String channel){
 		//TODO: Was passiert wenn es den Channel nicht gibt??
 		if( listChannelsJoined.contains(channel.toUpperCase()) ){
 			return true;
 		}
 		return false;
 	}
 	
 	@Override
 	protected void onIncomingFileTransfer(DccFileTransfer transfer) {
 		DccDownload dccDownload = DccDownloadQueue.getInstance().getDccDownload(transfer);
 		transfer.receive(dccDownload.getDestinationFile(), true);
 		dccDownload.setDccFileTransfer(transfer);
 		dccDownload.start();
 	}
 	
 	private String getLogTime(){
 		Calendar cal = Calendar.getInstance();
 	    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm"); //$NON-NLS-1$
 	    return sdf.format(cal.getTime());	
 	}
 
 	public void xdccSend(DccPacket dccPacket, File target) {
 		DccDownloadQueue downloadQueue = DccDownloadQueue.getInstance();
 		DccDownload dccDownload = new DccDownload(dccPacket, target);
 		if( downloadQueue.getDccDownload(dccDownload.getKey()) != null ){
 			sendCTCPCommand(dccPacket.getSender(), "xdcc send #" + dccPacket.getPacketNr());		 //$NON-NLS-1$
 			dccDownload = downloadQueue.getDccDownload(dccDownload.getKey());
 			dccDownload.setState(DccDownload.STATE_DOWNLOAD_WAITING);
 		}else{
 			downloadQueue.addToQueue(dccDownload);
 			sendCTCPCommand(dccPacket.getSender(), "xdcc send #" + dccPacket.getPacketNr());		 //$NON-NLS-1$
 			NotifyManagerDccDownload.getNotifyManager().notifyNewDccDownload(dccDownload);
 		}
 	}
 	
 	public IrcServer getIrcServer() {
 		return ircServer;
 	}
 
	public void removeChannelFromJoinedChannelList(String channelName) {
		listChannelsJoined.remove(channelName.toUpperCase());
	}

 }
