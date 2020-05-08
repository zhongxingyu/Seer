 /*
  * Copyright 2008 Ryan Berdeen.
  *
  * This file is part of Postal.
  *
  * Postal is free software: you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Postal is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
  * License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with Postal.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.ryanberdeen.postal.server;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 
 import org.apache.mina.core.service.IoAcceptor;
 import org.apache.mina.filter.codec.ProtocolCodecFilter;
 import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
 
 import com.ryanberdeen.postal.ConnectionManager;
 import com.ryanberdeen.postal.protocol.PostalProtocolCodecFactory;
 
 public class PostalServer {
 	private IoAcceptor ioAcceptor;
 	private int port;
 
 	public PostalServer(int port, ConnectionManager connectionManager) {
 		this.port = port;
 		ioAcceptor = new NioSocketAcceptor();
 		ioAcceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new PostalProtocolCodecFactory()));
 
 		ioAcceptor.setHandler(new PostalServerHandler(connectionManager));
 	}
 
 	/** Starts accepting connections.
 	 */
 	public void start() throws IOException {
 		ioAcceptor.bind(new InetSocketAddress(port));
 	}
 
 	/** Closes all active connections and stops accepting new ones.
 	 */
 	public void stop() {
 		ioAcceptor.unbind();
 	}
 }
