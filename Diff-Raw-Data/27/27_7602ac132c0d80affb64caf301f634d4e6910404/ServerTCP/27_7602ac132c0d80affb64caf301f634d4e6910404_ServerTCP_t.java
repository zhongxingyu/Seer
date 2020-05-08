 package alpv_ws1112.ub1.webradio.communication.tcp;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketTimeoutException;
 import java.util.ArrayList;
 import java.util.concurrent.BrokenBarrierException;
 import java.util.concurrent.CyclicBarrier;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import javax.sound.sampled.UnsupportedAudioFileException;
 
 import alpv_ws1112.ub1.webradio.communication.Server;
 import alpv_ws1112.ub1.webradio.communication.ServerStreamer;
 
 public class ServerTCP implements Server {
 
 	private ServerSocket _socket;
 	private boolean _close = false;
 	private ArrayList<ServerTCPWorker> _clients, _pendingClients; // Number of
 																	// connected
 																	// and
 																	// pending
 																	// clients
 
 	private CyclicBarrier _barrier; // Buffer sending barrier
 	private ServerStreamer _streamer;
 	private AtomicBoolean _currentlyResetingBarrier;
 	private AtomicBoolean _currentlyMergingClients;
 
 	public ServerTCP(int port) throws IOException {
 		// Create socket
 		_socket = new ServerSocket(port);
		_socket.setSoTimeout(500); // Only 500ms timeout
 
 		// Create connection queues
 		_clients = new ArrayList<ServerTCPWorker>();
 		_pendingClients = new ArrayList<ServerTCPWorker>();
 		_currentlyMergingClients = new AtomicBoolean();
 		_currentlyResetingBarrier = new AtomicBoolean();
 
 		System.out.println("Starting server using port \"" + port + "\".");
 	}
 
 	/**
 	 * Set the close flag
 	 */
 	public void close() {
 		_close = true;
 	}
 
 	/**
 	 * Play a specific song
 	 */
 	public void playSong(String path) throws MalformedURLException,
 			UnsupportedAudioFileException, IOException {
 
 		System.out.println("Play song: " + path);
 
 		try {
			
			
 			// Initialize the stream and start all clients
 			if (_streamer == null) {
 				_streamer = new ServerStreamer(this, path);
				// Initialize the barrier
				resetBarrier();
 				for (ServerTCPWorker client : _clients) {
 					startClient(client);
 				}
 			} else {
 				_streamer.changePath(path);
 			}
 
 		} catch (MalformedURLException e) {
 			System.err.println("Can't find audio file.");
 		} catch (UnsupportedAudioFileException e) {
 			System.err.println("Unsupported audio file.");
 		} catch (IOException e) {
 			System.err.println("Can't open audio file.");
 		}
 	}
 
 	/**
 	 * Start client playback
 	 */
 	public void startClient(ServerTCPWorker worker) {
 		worker.setAudioFormat(_streamer.getAudioFormat());
 		worker.play();
 
 		Thread thread = new Thread(worker);
 		thread.start();
 	}
 
 	/**
 	 * Handles client server socket connections
 	 */
 	public void run() {
 
 		System.out.println("Server started.");
 
 		while (!_close) {
 			try {
 				Socket client = _socket.accept();
 				ServerTCPWorker worker = new ServerTCPWorker(this,
 						client.getOutputStream());
 				addClient(worker);
 			} catch (SocketTimeoutException e) {
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 		try {
 
 			// Close connected clients
 			for (ServerTCPWorker client : _clients) {
 				client.close();
 			}
 
 			// Close pending clients
 			for (ServerTCPWorker pendingClient : _pendingClients) {
 				pendingClient.close();
 			}
 
 			// Close streamer
 			if (_streamer != null) {
 				_streamer.close();
 			}
 
 			// Close socket
 			_socket.close();
 
 		} catch (IOException e) {
 			System.err.println(e.getMessage());
 		}
 		System.out.println("Server closed.");
 	}
 
 	/**
 	 * Add a new client to the pending queue
 	 * 
 	 * @param worker
 	 */
 	private void addClient(ServerTCPWorker worker) {
 		while (_currentlyMergingClients.get())
 			;
 		_pendingClients.add(worker);
 		System.out.println("New pending client.");
 		
 		if(_barrier == null) {
 			resetBarrier();
 		}
 	}
 
 	/**
 	 * Returns the current buffer
 	 * 
 	 * @return
 	 */
 	public byte[] getBuffer() {
 		return _streamer.getBuffer();
 	}
 
 	/**
 	 * Refresh the current barrier and merge pending and connected clients
 	 */
 	public void resetBarrier() {
 		// Set locks
 		_currentlyResetingBarrier.set(true);
 		_currentlyMergingClients.set(true);
 		// Add pending workers to connected worker queue and start them if radio
 		// is already playing
 		for (ServerTCPWorker pendingClient : _pendingClients) {
 			_clients.add(pendingClient);
 			if (_streamer != null) {
 				startClient(pendingClient);
 			}
 			System.out.println("Client added.");
 		}
 		// Remove added workers from pending queue
 		_pendingClients.clear();
 		// Remove merging lock
 		_currentlyMergingClients.set(false);
		// Create a new barrier
 		int size = _clients.size();
 		if (size > 0) {
 			_barrier = new CyclicBarrier(size, _streamer);
 		}
 		// Remove reseting lock
 		_currentlyResetingBarrier.set(false);
 	}
 
 	/**
 	 * Wait at the current barrier
 	 */
 	public void awaitBarrier() {
		System.out.println(new java.util.Date());
		System.out.println("awaitBarrier: " + _barrier.getParties());
		System.out.println();
 		if (_barrier != null) {
 			while (_currentlyResetingBarrier.get())
 				;
 			try {
 				_barrier.await();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			} catch (BrokenBarrierException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
