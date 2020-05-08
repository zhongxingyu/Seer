 /**
  * Copyright (c) 2011-2013 Robert Maupin
  * 
  * This software is provided 'as-is', without any express or implied
  * warranty. In no event will the authors be held liable for any damages
  * arising from the use of this software.
  * 
  * Permission is granted to anyone to use this software for any purpose,
  * including commercial applications, and to alter it and redistribute it
  * freely, subject to the following restrictions:
  * 
  *    1. The origin of this software must not be misrepresented; you must not
  *    claim that you wrote the original software. If you use this software
  *    in a product, an acknowledgment in the product documentation would be
  *    appreciated but is not required.
  * 
  *    2. Altered source versions must be plainly marked as such, and must not be
  *    misrepresented as being the original software.
  * 
  *    3. This notice may not be removed or altered from any source
  *    distribution.
  */
 package org.csdgn.fxm;
 
 import java.util.Locale;
 
 import org.csdgn.fxm.model.World;
 import org.csdgn.fxm.net.ServerPipelineFactory;
 
 import io.netty.bootstrap.ServerBootstrap;
 import io.netty.channel.EventLoopGroup;
 import io.netty.channel.nio.NioEventLoopGroup;
 import io.netty.channel.socket.nio.NioServerSocketChannel;
 
 public class Server {
 	public static void main(String[] args) throws Exception {
 		//Set default locale to something sane.
 		Locale.setDefault(Locale.US);
 		
 		//No Javassist, whatever that is.
 		//This prevents an error being thrown.
 		System.setProperty("io.netty.noJavassist", "true");
 		
 		//Load Configuration
 		System.out.println("Loading configuration.");
 		Config.loadConfiguration();
		Config.createMissingFolders();
 		
 		//Load world
 		System.out.println("Loading world.");
 		World.instance.loadWorld();
 		
 		int port;
 		if (args.length > 0) {
 			port = Integer.parseInt(args[0]);
 		} else {
 			port = 4000;
 		}
 		
 		System.out.println(String.format("FxMud running on port %d.", port));
 		
 		new Server(port).run();
 	}
 	
 	private final int port;
 
 	public Server(int port) {
 		this.port = port;
 	}
 	
 	public void run() throws Exception {
 		EventLoopGroup owner = new NioEventLoopGroup();
 		EventLoopGroup worker = new NioEventLoopGroup();
 		try {
 			ServerBootstrap b = new ServerBootstrap()
 				.group(owner, worker)
 				.channel(NioServerSocketChannel.class)
 				.childHandler(new ServerPipelineFactory());
 
 			b.bind(port)
 				.sync()
 				.channel()
 				.closeFuture()
 				.sync();
 		} finally {
 			owner.shutdownGracefully();
 			worker.shutdownGracefully();
 		}
 	}
 }
