 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.net.InetAddress;
 import java.net.SocketTimeoutException;
 import java.net.UnknownHostException;
 
 class ActiveThread extends Thread {
 	private final boolean PUSH_MODE = true;
 	private final boolean PULL_MODE = false;
 
 	private boolean running = true;
 	private final DatagramSocket sock;
 	private final View view;
 
 	public ActiveThread(DatagramSocket sock, View view) {
 		this.sock = sock;
 		this.view = view;
 
 		System.out.println("ActiveThread constructed.");
 	}
 
 	@Override
 	public void run() {
 		while (this.running) {
 			// get random node
 			Node n = this.view.selectNode();
 			// if no nodes exist yet
 			if (n == null) {
 				// find node via broadcast on local network
 				ServiceLocator loc = new ServiceLocator(
 						Application.BROADCAST_ADDR, Application.BROADCAST_PORT,
 						this.sock);
 				n = loc.locateAnnouncer();
 			}
 
 			try {
 				if (this.PUSH_MODE) {
 					View buf_out = this.view.getBuffer(this.sock.getPort());
					SendView.sendData(this.sock, InetAddress.getByName(n
							.getAddress()), n.getPort(), buf_out);
 				} else {
 					// send empty view to trigger response
					SendView.sendData(this.sock, InetAddress.getByName(n
							.getAddress()), Application.PORT, null);
 				}
 				if (this.PULL_MODE) {
 					// + 1 because of the ":" delimiter
 					byte[] buf_in = new byte[Application.MAX_CAP
 							* Application.MAX_BYTES + 1];
 					DatagramPacket p = new DatagramPacket(buf_in, buf_in.length);
 					this.sock.receive(p);
 					View v = SendView.unpackData(p);
 
					// TODO: give more parameters H, c, S, ...
 					this.view.mergeViews(v, Application.H, Application.S,
 							Application.C);
 					this.view.age();
 				}
 
 			} catch (UnknownHostException e) {
 				e.printStackTrace();
 			} catch (SocketTimeoutException e) {
 				continue;
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void endThread() {
 		this.running = false;
 	}
 }
