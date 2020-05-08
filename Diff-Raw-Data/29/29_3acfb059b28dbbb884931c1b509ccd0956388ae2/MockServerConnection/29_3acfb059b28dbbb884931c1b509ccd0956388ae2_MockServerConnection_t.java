 package ca.danielyule.mockingjay;
 
 import static org.hamcrest.CoreMatchers.is;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.PipedInputStream;
 import java.io.PipedOutputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
import java.net.SocketException;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.Queue;
 
 import org.hamcrest.BaseMatcher;
 import org.hamcrest.Description;
 import org.junit.Assert;
 
 /**
  * Intended as a helper class for {@link MockServer}.
  * 
  * @author Daniel
  * 
  */
 public class MockServerConnection {
 
 	/**
 	 * The series of actions the thread will take. The thread will pop the top
 	 * off this queue and perform whatever type of action is indicated. For more
 	 * information, see {@link ReadAction}.
 	 * 
 	 * @see ReadAction
 	 */
 	private volatile Queue<ReadAction> readActions;
 	/**
 	 * The read action we are currently writing to. All writes to
 	 * {@link #expectedStream} and {@link #responseStream} are written here.
 	 * This is also at the back of the queue. Can be null if all
 	 * {@link ReadAction}s have been processed.
 	 */
 	private volatile ReadAction currentReadAction;
 
 	/**
 	 * An {@link InputStream} containing the current processed expected data;
 	 */
 	private InputStream expectedInputStream;
 	/**
 	 * The {@link ServerSocket} we will use to listen for connections from the
 	 * client. The socket stops listening after the first connection.
 	 */
 	private ServerSocket ss;
 	/**
 	 * The socket that the thread will be using to communicate with the client.
 	 */
 	private volatile Socket socket;
 
 	/**
 	 * The {@link OutputStream} owned by the socket.
 	 */
 	private OutputStream socketOutputStream;
 	/**
 	 * The thread that handles processing the client's input. This must be on a
 	 * thread, because the data from the client may be being sent
 	 * asynchronously.
 	 */
 	private Thread runner;
 	/**
 	 * The thread that will handle input from the client. This thread just waits
 	 * for new input and immediately passes it along to the runner.
 	 */
 	private Thread ioThread;
 	/**
 	 * The {@link PipedInputStream} that the io thread will use to send
 	 * information to the runner
 	 */
 	private PipedInputStream ioToRunnerInput;
 	/**
 	 * The {@link PipedOutputStream} the runner will read information from the
 	 * io thread
 	 */
 	private PipedOutputStream ioToRunnerOutput;
 
 	/**
 	 * The problem that caused the assertion to fail, or null if there were no
 	 * problems.
 	 */
 	private Throwable problem;
 	/**
 	 * Set to true if the thread should stop running.
 	 */
 	private volatile boolean stopping;
 
 	/**
 	 * Set to true after the IO thread stops running.
 	 */
 	private volatile boolean ioStopped = false;
 
 	/**
 	 * Set to true after the runner thread stops running.
 	 */
 	private volatile boolean runnerStopped = false;
 	/**
 	 * The {@link ProxyOutputStream} to which users write data they expect the
 	 * thread to receive.
 	 */
 	private OutputStream expectedStream;
 	/**
 	 * The {@link ProxyOutputStream} to which users write data they expect the
 	 * thread to send.
 	 */
 	private OutputStream responseStream;
 
 	/**
 	 * Closes everything down and checks to see if any errors were found. This
 	 * function should be called after all data has been sent and received. If
 	 * the data sent was incorrect, this function will cause a failed test.
 	 * 
 	 * @throws Throwable
 	 *             If an error of some type was encountered.
 	 */
 	public void verify() throws Throwable {
 		// Stop the runner thread from running
 		stop();
 
 		// Make sure to wait until all of the input is processed before
 		// checking
 		// for a failed assertion.
 
 		// We extract this to make sure it isn't being accessed from another
 		// thread and set to null between the check and the shutdown
 		final Socket localSocket = socket;
 		if (localSocket != null) {
			try{
				localSocket.shutdownInput();
			} catch (SocketException e) {
				//Ignore this, as this might occur if the other side closes the socket first.
			}
 		}
 
 		ioThread.interrupt();
 		ss.close();
 		if (localSocket != null) {
 			localSocket.close();
 		}
 
 		while (!ioStopped) {
 			synchronized (this) {
 				wait();
 			}
 			ioThread.interrupt();
 		}
 		synchronized (runner) {
 			runner.interrupt();
 		}
 
 		if (localSocket != null && runnerStopped) {
 			localSocket.close();
 		}
 		while (!runnerStopped) {
 			synchronized (this) {
 				wait();
 			}
 			synchronized (runner) {
 				runner.interrupt();
 			}
 			if (localSocket != null && runnerStopped) {
 				localSocket.close();
 			}
 		}
 
 		// To make sure that problem isn't affected while we do the null check,
 		// we extract it to a local variable
 		final Throwable localProblem = problem;
 		if (localProblem != null) {
 			throw localProblem;
 		}
 
 		// Gain control of the monitor that controls access to the problem and
 		// passed fields.
 
 		Assert.assertThat(ioToRunnerInput, new BaseMatcher<PipedInputStream>() {
 
 			@Override
 			public boolean matches(Object item) {
 				try {
 					if (item == null) {
 						return false;
 					}
 					return ((PipedInputStream) item).available() == 0;
 				} catch (IOException e) {
 					return true;
 				}
 			}
 
 			@Override
 			public void describeTo(Description description) {
 				if (description != null) {
 					description.appendText("An empty input stream");
 				}
 
 			}
 
 		});
 		if (expectedInputStream != null) {
 
 			Assert.assertThat(expectedInputStream, new BaseMatcher<InputStream>() {
 
 				@Override
 				public boolean matches(Object item) {
 					if (item == null) {
 						return false;
 					}
 					try {
 						return ((InputStream) item).available() == 0;
 					} catch (IOException e) {
 						return true;
 					}
 				}
 
 				@Override
 				public void describeTo(Description description) {
 					if (description != null) {
 						description.appendText("An empty input stream");
 					}
 
 				}
 
 			});
 		}
 
 		Assert.assertThat(readActions, new BaseMatcher<Queue<?>>() {
 
 			@Override
 			public boolean matches(Object item) {
 				if (item instanceof Queue<?>) {
 					return ((Queue<?>) item).isEmpty();
 				}
 				return false;
 			}
 
 			@Override
 			public void describeTo(Description description) {
 				if (description != null) {
 					description.appendText("A empty action queue");
 				}
 
 			}
 
 		});
 
 	}
 
 	/**
 	 * An {@link OutputStream} representing the expectation for this server.
 	 * Anything written to this output stream will be expected to be received by
 	 * the server or the test will fail.
 	 * 
 	 * @return An {@link OutputStream} for writing expectation
 	 */
 	public OutputStream expected() {
 		return expectedStream;
 	}
 
 	/**
 	 * An {@link OutputStream} representing the data this server should send
 	 * back to the client. The server will send this data as soon as anything
 	 * that has been written to the {@link #expected()} <code>OuputStream</code>
 	 * before this output stream has been written to.
 	 * 
 	 * @return An {@link OutputStream} for storing responses
 	 */
 	public OutputStream response() {
 		return responseStream;
 	}
 
 	/**
 	 * Creates a new {@link MockServerConnection} that will listen on the given
 	 * port.
 	 * 
 	 * @param port
 	 *            The port this server will listen on.
 	 * @throws IOException
 	 *             If the server is unable to listen on the given port.
 	 */
 	public MockServerConnection(int port) throws IOException {
 
 		ss = new ServerSocket(port);
 		readActions = new LinkedList<>();
 		currentReadAction = null;
 		problem = null;
 		stopping = false;
 		expectedStream = new ProxyOutputStream(ACTION_TYPE.EXPECTED);
 		responseStream = new ProxyOutputStream(ACTION_TYPE.RESPONSE);
 		ioToRunnerInput = new PipedInputStream();
 		ioToRunnerOutput = new PipedOutputStream(ioToRunnerInput);
 		expectedInputStream = null;
 
 		runner = new Thread() {
 
 			@Override
 			public synchronized void run() {
 				while (!stopping) {
 					try {
 						wait();
 					} catch (InterruptedException e) {
 						// Ignore it
 					}
 					try {
 						if (ioToRunnerInput.available() > 0) {
							InputStream localExpectedInputStream = expectedInputStream;
 							while (ioToRunnerInput.available() > 0
 									&& ((localExpectedInputStream != null && localExpectedInputStream
 											.available() > 0) || (!readActions.isEmpty() && readActions
 											.peek().actionType == ACTION_TYPE.EXPECTED))) {
 								if (localExpectedInputStream == null
 										|| localExpectedInputStream.available() == 0) {
 									byte[] expected = readActions.remove().toByteArray();
 									expectedInputStream = new ByteArrayInputStream(expected);
									localExpectedInputStream = expectedInputStream;
 								}
 								while (localExpectedInputStream != null
 										&& localExpectedInputStream.available() > 0
 										&& ioToRunnerInput.available() > 0) {
 									Assert.assertThat(ioToRunnerInput.read(),
 											is(localExpectedInputStream.read()));
 								}
 
 							}
 						}
 						final OutputStream localSocketOutputStream = socketOutputStream;
 						while (!readActions.isEmpty()
 								&& readActions.peek().actionType == ACTION_TYPE.RESPONSE
 								&& localSocketOutputStream != null) {
 
 							localSocketOutputStream.write(readActions.remove().toByteArray());
 
 						}
 					} catch (IOException e) {
 						// This should literally never happen
 						problem = e;
 						stopping = true;
 					} catch (AssertionError e) {
 						problem = e;
 						stopping = true;
 						while (!readActions.isEmpty()) {
 							if (readActions.peek().actionType == ACTION_TYPE.RESPONSE) {
 								try {
 									final OutputStream localSocketOutputStream = socketOutputStream;
 									if (localSocketOutputStream != null) {
 										localSocketOutputStream.write(readActions.remove()
 												.toByteArray());
 									}
 								} catch (IOException e1) {
 									// just keep trying to send stuff.
 								}
 							} else {
 								readActions.remove();
 							}
 
 						}
 					}
 				}
 				runnerStopped = true;
 				synchronized (MockServerConnection.this) {
 					MockServerConnection.this.notify();
 				}
 			}
 		};
 
 		ioThread = new Thread() {
 
 			@Override
 			public synchronized void run() {
 				try {
 					socket = ss.accept();
 					final Socket localSocket = socket;
 					if (localSocket == null) {
 						return;
 					}
 					localSocket.setSoLinger(true, 1000);
 					InputStream socketInputStream = localSocket.getInputStream();
 					socketOutputStream = localSocket.getOutputStream();
 					byte[] read = new byte[1024];
 					int readLen = 0;
 					while (readLen >= 0) {
 						// System.out.println("Waiting for data!");
 						readLen = socketInputStream.read(read);
 						if (readLen > 0) {
 							System.out.println("Got some data: "
 									+ new String(Arrays.copyOf(read, readLen)));
 							ioToRunnerOutput.write(read, 0, readLen);
 							synchronized (runner) {
								runner.notifyAll();
 							}
 						}
 
 					}
 					socketInputStream.close();
 				} catch (IOException e) {
 					// Guess we're all done
 				} finally {
 					synchronized (runner) {
 						stopping = true;
 						runner.notifyAll();
 					}
 				}
 				ioStopped = true;
 				synchronized (MockServerConnection.this) {
 					MockServerConnection.this.notify();
 				}
 			}
 
 		};
 
 		runner.start();
 		ioThread.start();
 
 	}
 
 	/**
 	 * Stop the thread from running. This will allow the thread to complete its
 	 * execution.
 	 */
 	private void stop() {
 
 		stopping = true;
 
 	}
 
 	/**
 	 * Used to delineate the two types of reading actions the thread can take:
 	 * 
 	 * EXPECTED: data that the thread expects to receive from the client
 	 * 
 	 * RESPONSE: data that the thread should send to the client.
 	 * 
 	 * @author Daniel Yule (daniel.yule@gmail.com)
 	 * 
 	 */
 	private enum ACTION_TYPE {
 
 		/**
 		 * Data the thread expects to receive from the client
 		 */
 		EXPECTED,
 
 		/**
 		 * Data the thread should send to the client
 		 */
 		RESPONSE;
 
 	}
 
 	/**
 	 * <p>
 	 * This class encapsulates some kind of action that the thread will take.
 	 * There are two types of action, indicated by {@link #isExpected}:
 	 * expecting and response. The action is <code>expecting</code> if
 	 * <code>isExpecting</code> is true and is <code>response</code> otherwise.
 	 * </p>
 	 * 
 	 * <p>
 	 * The action class is basically a writable byte array. When users write to
 	 * the {@link MockServerConnection#expectedStream} or
 	 * {@link MockServerConnection#responseStream}, those writes are dumped into
 	 * a readAction. As soon as the type of write changes(ie goes from a write
 	 * to expected to a write to responses) and a new <code>ReadAction</code> of
 	 * the appropriate type is added to the queue.
 	 * </p>
 	 * 
 	 * <p>
 	 * If the action is <code>expecting</code>, then the thread will read the
 	 * contents of the byte array and match it against what it has received. If
 	 * the <code>ReadAction</code> is of type <code>response</code> then the
 	 * thread will read its byte array and write that to the socket.
 	 * </p>
 	 * 
 	 * @author Daniel Yule (daniel.yule@gmail.com)
 	 * 
 	 */
 	private class ReadAction extends ByteArrayOutputStream {
 
 		/**
 		 * Determines the {@link ACTION_TYPE action type} of this
 		 * <code>ReadAction</code>.
 		 */
 		private ACTION_TYPE actionType;
 
 		/**
 		 * Creates a new {@link ReadAction} of the given type
 		 * 
 		 * @param actionType
 		 *            The {@link ACTION_TYPE} of this read action
 		 */
 		public ReadAction(ACTION_TYPE actionType) {
 			super();
 			this.actionType = actionType;
 		}
 
 		@Override
 		public synchronized String toString() {
 			return new String(this.toByteArray());
 		}
 
 	}
 
 	/**
 	 * For use with {@link MockServerConnection#expectedStream}. This is a
 	 * singleton class that operates as a proxy for a {@link ReadAction}.
 	 * Everything that is written to this output stream is passed along to the
 	 * {@link MockServerConnection#currentReadAction current ReadAction}. If the
 	 * current <code>ReadAction</code> doesn't match the type of this
 	 * <code>ProxyOutputStream</code>, then a new <code>ReadAction</code> is
 	 * created of the appropriate type and added to the queue.
 	 * 
 	 * @author Daniel Yule (daniel.yule@gmail.com)
 	 * 
 	 */
 
 	private class ProxyOutputStream extends OutputStream {
 
 		/**
 		 * The type of action this output stream is for. Can be either
 		 * <code>EXPECTED</code> or <code>RESPONSE</code>. If
 		 * {@link ACTION_TYPE#EXPECTED} then this is data that the thread should
 		 * expect from the client. If {@link ACTION_TYPE#RESPONSE} then this is
 		 * data that the thread should send to the client.
 		 */
 		private final ACTION_TYPE actionType;
 
 		/**
 		 * Create a new {@link ProxyOutputStream} of the given type.
 		 * 
 		 * @param type
 		 *            The {@link ACTION_TYPE} of this output stream
 		 */
 		public ProxyOutputStream(ACTION_TYPE type) {
 			this.actionType = type;
 		}
 
 		/**
 		 * Checks to make sure that we have the right type of action as the
 		 * current type. If the action doesn't match up, create a new one and
 		 * add it to the queue.
 		 */
 		private void checkCurrentReadAction() {
 			final ReadAction localReadAction = currentReadAction;
 
 			if (readActions.isEmpty() || localReadAction == null
 					|| localReadAction.actionType != actionType) {
 				currentReadAction = new ReadAction(actionType);
 				readActions.add(currentReadAction);
 
 			}
 
 		}
 
 		@Override
 		public void write(byte[] b, int off, int len) throws IOException {
 
 			// Make sure we have the correct type
 			checkCurrentReadAction();
 			final ReadAction localReadAction = currentReadAction;
 			if (localReadAction != null) {
 				// Proxy the write command
 				localReadAction.write(b, off, len);
 			}
 
 			// // Wake the thread up in case it was waiting for additional
 			// // ReadActions
 			synchronized (runner) {
 				runner.notifyAll();
 			}
 
 		}
 
 		@Override
 		public void write(int b) throws IOException {
 			// Identical to the above, but with a different proxied function
 			checkCurrentReadAction();
 			final ReadAction localReadAction = currentReadAction;
 			if (localReadAction != null) {
 				// Proxy the write command
 				localReadAction.write(b);
 			}
 
 			synchronized (runner) {
 				runner.notify();
 			}
 
 		}
 
 	}
 
 }
