 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 public class Peer implements Runnable, ReadEventListener {
 
 	protected InetAddress group;
 	protected int port;
 
 	public Peer(InetAddress group, int port) {
 		this.group = group;
 		this.port = port;
 		initIO();
 	}
 
 	protected BufferedReader input;
 	protected PrintStream output;
 
 	protected void initIO() {
 		input = new BufferedReader(new InputStreamReader(System.in));
 		output = System.out;
 	}
 
 	protected Thread listener;
 	protected Thread readSourceListener;
 
 	public synchronized void start() throws IOException {
 		if (listener == null) {
 			initNet();
 			listener = new Thread(this);
 			listener.start();
 			ReadSource readSource = new ReadSource(input, output);
 			readSourceListener = new Thread(readSource);
 			readSourceListener.start();
 			readSource.addEventListener(this);
 		}
 	}
 
 	protected MulticastSocket socket;
 	protected DatagramPacket outgoing, incoming;
 
 	protected void initNet() throws IOException {
 		socket = new MulticastSocket(port);
 		socket.setTimeToLive(5);
 		socket.joinGroup(group);
		outgoing = new DatagramPacket(new byte[1], 1, group, port);
		incoming = new DatagramPacket(new byte[65508], 65508);
 	}
 
	public synchronized void stop() throws IOException {
 		if (listener != null) {
 			listener.interrupt();
 			listener = null;
 			try {
 				socket.leaveGroup(group);
 			} finally {
 				socket.close();
 			}
 		}
 	}
 
	public synchronized void handleReadEvent(ReadEvent e) {
 		try {
 			byte[] utf = e.getReadInput();
 			outgoing.setData(utf);
 			outgoing.setLength(utf.length);
 			socket.send(outgoing);
 		} catch (IOException ex) {
 			handleIOException(ex);
 		}
 	}
 
 	public void run() {
 		try {
 			while (!Thread.interrupted()) {
 				incoming.setLength(incoming.getData().length);
 				socket.receive(incoming);
 				String message = new String(
 					incoming.getData(), 0, incoming.getLength(), "UTF8");
 				output.println(message);
 			}
 		} catch (IOException ex) {
 			handleIOException(ex);
 		}
 	}
 
 	protected synchronized void handleIOException (IOException ex) {
 		if (listener != null) {
 			output.println(ex);
 			if (listener != Thread.currentThread())
 				listener.interrupt();
 			listener = null;
 			try {
 				socket.leaveGroup(group);
 			} catch (IOException ignored) {}
 			socket.close();
 		}
 	}
 }
