 package main.input;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.concurrent.ExecutorService;
 
 import com.google.common.base.Throwables;
 import com.google.inject.Inject;
 
 public class ConsoleControl {
 	private final BufferedReader in;
 	private final InputListener listener;
 	private final ExecutorService service;
 
 	public class InputListenerThread implements Runnable {
 		@Override
 		public synchronized void run() {
 			try {
 				boolean stop = listener.lineRead(in.readLine());
 				while (stop != false) {
 					stop = listener.lineRead(in.readLine());
 				}
 			} catch (final IOException e) {
 				Throwables.propagate(e);
 			}
 		}
 	}
 
 	@Inject
 	public ConsoleControl(InputStream in, InputListener listener, ExecutorService service) {
		final InputStreamReader converter = new InputStreamReader(in);
 		this.in = new BufferedReader(converter);
 		this.listener = listener;
 		this.service = service;
 	}
 
 	public void startListening() {
 		service.execute(new InputListenerThread());
 	}
 }
