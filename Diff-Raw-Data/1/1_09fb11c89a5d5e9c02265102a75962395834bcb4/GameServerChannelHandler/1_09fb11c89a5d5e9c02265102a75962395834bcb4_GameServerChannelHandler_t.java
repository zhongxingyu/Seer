 /*
  * This file is part of aion-lightning <aion-lightning.com>.
  *
  *  aion-lightning is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  aion-lightning is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with aion-lightning.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.aionemu.loginserver.network.gameserver;
 
 import org.apache.log4j.Logger;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.jboss.netty.channel.ChannelStateEvent;
 
 import com.aionemu.commons.network.netty.handler.AbstractChannelHandler;
 import com.aionemu.commons.network.netty.handler.AbstractPacketHandlerFactory;
 import com.aionemu.loginserver.GameServerInfo;
 
 /**
  * @author lyahim
  * 
  */
 public class GameServerChannelHandler extends AbstractChannelHandler
 {
 	/**
 	 * Default logger
 	 */
 	private static final Logger	log				= Logger.getLogger(GameServerChannelHandler.class);
 	private GameServerInfo		gameServerInfo	= null;
 
 	public GameServerChannelHandler(AbstractPacketHandlerFactory<GameServerChannelHandler> aphf)
 	{
 		super(aphf);
 	}
 
 	@Override
 	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception
 	{
 		super.channelConnected(ctx, e);
 		log.info("GameServer connected from " + inetAddress.getHostAddress());
 	}
 
 	@Override
 	public void channelDisconnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception
 	{
 		super.channelDisconnected(ctx, e);
		gameServerInfo.setGscHandler(null);
 		log.info("GameServer disconnected! IP: " + inetAddress.getHostAddress());
 	}
 
 	/**
 	 * @return GameServerInfo for this GsConnection or null if this GsConnection
 	 *         is not authenticated yet.
 	 */
 	public GameServerInfo getGameServerInfo()
 	{
 		return gameServerInfo;
 	}
 
 	/**
 	 * @param gameServerInfo
 	 *            Set GameServerInfo for this GsConnection.
 	 */
 	public void setGameServerInfo(GameServerInfo gameServerInfo)
 	{
 		this.gameServerInfo = gameServerInfo;
 	}
 }
