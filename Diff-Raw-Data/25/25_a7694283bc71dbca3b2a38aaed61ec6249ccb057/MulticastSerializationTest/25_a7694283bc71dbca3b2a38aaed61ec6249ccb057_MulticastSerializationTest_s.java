 package experimentos;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
import java.io.Serializable;
 import java.net.DatagramPacket;
 import java.net.InetAddress;
 import java.net.MulticastSocket;
 import java.net.UnknownHostException;
 
 /**
  * 
  * 
  * MulticastSerializationTest.java:
  * 
  * Test de envio y recepcin de objetos serializables a traves de un socket
  * Multicast.
  * 
  * @author Jon Ander Hernndez & Javier Mediavilla
  * 
  */
 
 public class MulticastSerializationTest {
 
	public class Paquete implements Serializable {
		private static final long serialVersionUID = 1L;
		String contenido = "Hello!";
	}

 	/**
 	 * This is the multicast group, we are listening to for multicast DNS
 	 * messages.
 	 */
 	private InetAddress group;
 
 	/**
 	 * This is our multicast socket.
 	 */
 	private MulticastSocket socket;
 
 	private InetAddress addr;
 	private String name;
 
 	/*
 	 * constantes
 	 */
 	public final static String MDNS_GROUP = "224.0.0.251";
 	public final static String MDNS_GROUP_IPV6 = "FF02::FB";
 	// public final static int MDNS_PORT = 5353;
 	// Modificado para que no haya troubles con el mDNS 'real' 0:-)
 	public final static int MDNS_PORT = 5454;
 	public final static int DNS_PORT = 53;
 
 	/**
 	 * 
 	 */
 	public MulticastSerializationTest() {
 
 		/*
 		 * Obtenemos la info de la red "local"
 		 */
 		try {
 			addr = InetAddress.getLocalHost();
 			name = addr.getHostAddress();
 			if (addr.isLoopbackAddress())
 				addr = null;
 		} catch (final IOException e) {
 			addr = null;
 			name = "local";
 		}
 
 		/*
 		 * Creamos el socket Multicast
 		 */
 		try {
 			group = InetAddress.getByName(MDNS_GROUP);
 
 			socket = new MulticastSocket(MDNS_PORT);
 
 			socket.joinGroup(group);
 
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 
 		} catch (IOException e) {
 			e.printStackTrace();
 
 		}
 	}
 
 	public void close() {
 		try {
 			socket.leaveGroup(group);
 
 			socket.close();
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public static void main(String[] args) {
 
 		final MulticastSerializationTest zeroconf = new MulticastSerializationTest();
 		final MulticastSerializationTest zeroconfArray[] = new MulticastSerializationTest[4];
 		Thread threadPool[] = new Thread[4];
 
 		System.out.println(zeroconf.addr);
 		System.out.println(zeroconf.name);
 
 		/*
 		 * Creamos un hilo que manda un mensaje.
 		 */
 		Thread thread = new Thread(new Runnable() {
 
 			@Override
 			public void run() {
 				System.out.println("enviando Hello!!!");
				Paquete msg = zeroconf.new Paquete();
 
 				ByteArrayOutputStream baos = new ByteArrayOutputStream();
 				ObjectOutputStream oos;
 
 				try {
 
 					oos = new ObjectOutputStream(baos);
 					oos.writeObject(msg);
 					oos.close();
 
 				} catch (IOException e1) {
 					e1.printStackTrace();
 				}
 				byte[] buffer = baos.toByteArray();
 
 				DatagramPacket hi = new DatagramPacket(buffer, buffer.length,
 						zeroconf.group, MulticastSocketTest.MDNS_PORT);
 				try {
 					zeroconf.socket.send(hi);
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 				System.out.println("enviado!!");
 			}
 
 		});
 
 		/*
 		 * Creamos una serie de hilos que leen.
 		 */
 		for (int i = 0; i < 4; i++) {
 			zeroconfArray[i] = new MulticastSerializationTest();
 			final int ii = i;
 			threadPool[i] = new Thread(new Runnable() {
 
 				@Override
 				public void run() {
 					System.out.println(ii + " recibiendo!!!");
 					byte[] buf = new byte[1000];
 					DatagramPacket recv = new DatagramPacket(buf, buf.length);
 					try {
 						zeroconfArray[ii].socket.receive(recv);
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 
 					ByteArrayInputStream bais = new ByteArrayInputStream(recv
 							.getData(), 0, recv.getData().length);
 					ObjectInputStream ois;
 
 					Paquete paquete;
 
 					try {
 
 						ois = new ObjectInputStream(bais);
 						paquete = (Paquete) ois.readObject();
 						ois.close();
 
 						System.out.println(ii + " recibido!!! : -> ["
 								+ paquete.contenido + "]");
 
 					} catch (IOException e1) {
 						e1.printStackTrace();
 					} catch (ClassNotFoundException e) {
 						e.printStackTrace();
 					}
 				}
 			});
 			threadPool[i].start();
 		}
 
 		try {
 			Thread.sleep(1);
 		} catch (InterruptedException e1) {
 			e1.printStackTrace();
 		}
 
 		thread.start();
 
 		try {
 			thread.join();
 			zeroconf.close();
 			for (int i = 0; i < 4; i++) {
 				threadPool[i].join();
 				zeroconfArray[i].close();
 			}
 
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
