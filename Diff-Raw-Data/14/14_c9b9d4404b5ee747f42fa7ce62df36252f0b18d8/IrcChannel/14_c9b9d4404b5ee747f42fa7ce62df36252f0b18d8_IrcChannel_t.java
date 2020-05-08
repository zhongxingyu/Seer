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
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 import org.jdom.Element;
 
 /**
  * @author snert
  *
  */
 public class IrcChannel implements IDccPacketList, IConnectedState{
 	
 	private IrcServer ircServer;
 	private String channelName;
 	private Map<String, DccPacket> mapDccPackets;
 	private boolean autoconnect;
 	
 	public IrcChannel(){
 		this.mapDccPackets = new HashMap<String, DccPacket>();
 	}
 
 	public IrcChannel(IrcServer ircServer){
 		this();
 		this.ircServer = ircServer;
 	}
 	
 	public IrcChannel(IrcServer ircServer, String channelName){
 		this();
 		this.ircServer 	 = ircServer;
 		this.channelName = channelName;
 	}
 
 	public IrcServer getIrcServer() {
 		return ircServer;
 	}
 	
 	public void setIrcServer(IrcServer ircServer) {
 		this.ircServer = ircServer;
 	}
 
 	public String getChannelName() {
 		return channelName;
 	}
 	
 	public void setChannelName(String channelName) {
 		this.channelName = channelName;
 	}
 	
 	public Element makeXmlNode(){
 		Element nodeChannel = new Element("IRC_CHANNEL"); //$NON-NLS-1$
 		nodeChannel.setAttribute("channelName", StringUtils.defaultString(channelName)); //$NON-NLS-1$
 		return nodeChannel;
 	}
 	
 	public void connect(){
 		if(ircServer.isConnected()){
 			ircServer.getDccBot().joinChannel(channelName);
 		}else{
 			throw new RuntimeException("Server is not connected"); //$NON-NLS-1$
 		}
 	}
 	
 	public void disconnect(){
 		ircServer.getDccBot().partChannel(channelName);
		//remove precaution also from listchoinedchannels => because if server didnt answer channel in gui is still connected
		ircServer.getDccBot().removeChannelFromJoinedChannelList(channelName);
 	}
 
 	public boolean isConnected(){
 		return ircServer.getDccBot().isChannelJoined(channelName);
 	}
 
 	public void addDccPacket(DccPacket dccPacket) {
 		mapDccPackets.put(dccPacket.toString(), dccPacket);
 	}
 
 	@Override
 	public List<DccPacket> getListDccPackets() {
 		return new ArrayList<DccPacket>(mapDccPackets.values());
 	}
 
 	public void dccSendFile(String filePath, DccPacket dccPacket) {
 		ircServer.getDccBot().xdccSend(dccPacket, new File(filePath + dccPacket.getName()));
 	}
 
 	@Override
 	public boolean containsDccPacket(DccPacket dccPacket) {
 		return mapDccPackets.containsKey(dccPacket.toString());
 	}
 
 	public void setAutoconnect(boolean autoconnect) {
 		this.autoconnect = autoconnect;
 	}
 
 	public boolean isAutoconnect() {
 		return autoconnect;
 	}
 }
