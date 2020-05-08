 import java.io.*;
 import java.util.*;
 
 public class ReadSource implements Runnable {
 
 	private BufferedReader _input;
 	private PrintStream _output;
 	private List _listeners = new ArrayList();
 
 	public ReadSource(BufferedReader input, PrintStream output) {
 		_input = input;
 		_output = output;
 	}
 
 	public void run() {
 		try {
 			while (!Thread.interrupted()) {
 				byte[] utf = _input.readLine().getBytes("UTF8");
 				_fireEvent(utf);
 			}
 		} catch (IOException ex) {
 			_output.println(ex);
 		}
 	}
 
 	public synchronized void addEventListener(ReadEventListener listener) {
 		_listeners.add(listener);
 	}
 
 	public synchronized void removeEventListener(ReadEventListener listener) {
 		_listeners.remove(listener);
 	}
 
 	private synchronized void _fireEvent(byte[] utf) {
 		ReadEvent event = new ReadEvent(this, utf);
 		Iterator i = _listeners.iterator();
		while(i.hasNext())	{
 			((ReadEventListener) i.next()).handleReadEvent(event);
 		}
 	}
 }
