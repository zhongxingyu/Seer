 package client;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.UnknownHostException;
 import java.nio.channels.SocketChannel;
 
 import base.common.AsyncActionBus;
 import base.game.GameHandler;
 import base.game.network.packets.LoginPacket;
 
 public class ClientGameHandler extends GameHandler {
 
 	public ClientGameHandler(AsyncActionBus bus, String clientName, String serverAddress) {
 		try {
 			this.networkHandler = new ClientNetworkHandler();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		LoginPacket lPacket = new LoginPacket(clientName, (byte) 0);
 
 		SocketChannel kkSocket = null;
 
 		try {
 			kkSocket = SocketChannel.open(new InetSocketAddress(serverAddress, 9999));
 			log.debug("Opening connection with {}", kkSocket.getRemoteAddress());
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		try {
 			Thread.sleep(2000);
 		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
 		}
 		send(lPacket, kkSocket);
 
 		while (true)
 			try {
				log.info("Still connected: {}", kkSocket.isConnected());
 				Thread.sleep(2000);
 			} catch (InterruptedException e) {
				e.printStackTrace();
 			}
 
 	}
 
 	private void send(LoginPacket lPacket, SocketChannel kkSocket) {
 
 		try {
 			log.debug("Wrote {} bytes", kkSocket.write(lPacket.getDataBuffer()));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void update() {
 
 	}
 
 }
