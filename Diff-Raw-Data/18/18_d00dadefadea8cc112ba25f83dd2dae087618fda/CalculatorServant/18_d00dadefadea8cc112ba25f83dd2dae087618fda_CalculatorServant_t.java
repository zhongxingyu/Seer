 package br.com.objective.training.hugeinteger.calc;
 
 import java.io.Closeable;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
import java.net.SocketException;
 
 import br.com.objective.training.foundation.IOUtils;
 import br.com.objective.training.hugeinteger.calc.protocol.Add;
 import br.com.objective.training.hugeinteger.calc.protocol.Operation;
 import br.com.objective.training.hugeinteger.calc.protocol.Compare;
 import br.com.objective.training.hugeinteger.calc.protocol.Subtract;
 
 class CalculatorServant implements Runnable, Closeable {
 
 	private final Calculator<String> delegate;
 
 	private final Socket socket;
 	private ObjectInputStream inputStream;
 	private ObjectOutputStream outputStream;
 
 	private boolean isAlive;
 
 	CalculatorServant(final Calculator<String> delegate, final Socket socket) throws IOException {
 		this.delegate = delegate;
 		this.socket = socket;
 		this.inputStream = new ObjectInputStream(socket.getInputStream());
 		this.outputStream = new ObjectOutputStream(socket.getOutputStream());
 		this.isAlive = true;
 	}
 
 	@Override
 	public void run() {
 		try {
 			while(isAlive) {
 				processRequest((Operation) inputStream.readObject());
 			}
 
		} catch(SocketException se) {
			System.out.println(">>> Calculator Servant closed.");
		} catch(Exception e) {
 			throw new RuntimeException(e);
 		} finally {
 			IOUtils.closeSilently(this);
 		}
 	}
 
 	private void processRequest(Operation operation) {
 		System.out.println("Incoming request: " + operation);
 		if (operation instanceof Add) {
			respondWith(delegate.add(operation.getLeftOperand(), operation.getRightOperand()));
 		} else if (operation instanceof Subtract) {
			respondWith(delegate.subtract(operation.getLeftOperand(), operation.getRightOperand()));
 		} else if (operation instanceof Compare) {
			respondWith(delegate.compare(operation.getLeftOperand(), operation.getRightOperand()));
 		} else {
 			respondWith("Invalid Operation");
 		}
 	}
 
 	private void respondWith(String result) {
 		System.out.println("Sending result: " + result);
 		try {
 			outputStream.writeObject(result);
			outputStream.flush();

 		} catch (IOException e) {
 			e.printStackTrace(System.err);
 		}
 	}
 
 	@Override
 	public void close() throws IOException {
 		this.isAlive = false;
 		this.socket.close();
 	}
 
 }
