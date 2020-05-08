 package ca.danielyule.mockingjay;
 
 import java.io.OutputStream;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.junit.rules.TestRule;
 import org.junit.runner.Description;
 import org.junit.runners.model.Statement;
 
 /**
  * <p>
  * A class for running a mock binary server with JUnit 4. This server will cause
  * a test to fail if unexpected data is sent. This server can also send data in
  * response to receiving the expected data.
  * </p>
  * 
  * <h3>Use</h3>
  * <p>
  * This server is designed to be used as a JUnit rule. In order to use this
  * server with your JUnit tests, simply add the following to your test suite:
  * </p>
  * 
  * <pre>
  * &#064;Rule
  * public MockServer mockServer = new MockServer(TEST_PORT);
  * </pre>
  * <p>
  * where TEST_PORT is an integer representing the port you expect to receive
  * data on.
  * </p>
  * 
  * <p>
  * To define what data you expect this server to receive, use the
  * {@link #expected()} {@link OutputStream}. Any data written to this output
  * stream will be expected to be received by the server. For example:
  * </p>
  * 
  * <pre>
  * mockServer.expected().write(new byte[] { 1, 2, 3, 4, 5 });
  * // put code to be tested here
  * </pre>
  * <p>
  * This will cause the server to expect the byte sequence [1, 2, 3, 4, 5] from
  * the socket connection it receives. Because the {@link #expected()} field
  * returns an <code>OutputStream</code>, you can use any component of the Java
  * IO library to write arbitrarily complex data. For example:
  * </p>
  * 
  * <pre>
  * Writer expectationWriter = new OutputStreamWriter(mockServer.expected());
  * expectationWriter.write(&quot;I do not like green eggs and ham!&quot;);
  * </pre>
  * <p>
  * To define what the server should respond when it receives data from the
  * client, use the {@link #response()} {@link OutputStream}. Any data written to
  * this <code>OutputStream</code> will be sent on the socket as soon as all
  * expected data up to this point has been sent. Essentially, you should write
  * expectation and response data to the mock server in the same order you expect
  * it to be sent along the socket. For example:
  * </p>
  * 
  * <pre>
  * mockServer.expected().write(new byte[] { 1, 2, 3, 4, 5 });
  * mockServer.response().write(new byte[] { 255, 254, 253, 252 });
  * mockServer.expected().write(new byte[] { 6, 7, 8, 9, 10 });
  * mockServer.response().write(new byte[] { 251, 250, 249, 248 });
  * // put code to be tested here
  * </pre>
  * <p>
  * This will cause the mock server to expect the sequence of bytes [1, 2, 3, 4,
  * 5], after it receives this, it will send the bytes [255, 254, 253, 252].
  * Then, the server will wait for the bytes [6, 7, 8, 9, 10], after which it
  * will send back [251, 250, 249, 248]. If at any point there is an IO problem,
  * or the data sent on the socket does not match what the server expects, the
  * test will fail, although not until the method exits.
  * 
  * <h3>Important Note</h3>
  * <h4>Problem</h4>
  * It is strongly recommended that your tests include both an expected and response component.  Because of the implementation of TCP/IP on modern operating systems, if you open a socket to the mock server, write your data and then immediately close, the data may or may not be sent, and the mock server will fail.  If you are getting random, inconsistent test failures, then this may be the cause.
  * <h4>Solution</h4>
  * Define a response for the mock server and block until you receive the response data, as shown below:
  * <pre>
  * //Define what we expect the server to send and receive. 
  * mockServer.expected().write(new byte[] { 1, 2, 3, 4, 5 });
  * mockServer.response().write(new byte[] { 255, 254, 253, 252 });
  * 
  * //Create a socket and connect on the local port
 * Socket socket = new Socket("localhost", TEST_PORT);
  * socket.connect();
  * 
  * //Send the data to the mock server
  * socket.getOutputStream.write(new byte[] { 1, 2, 3, 4, 5 });
  * 
  * byte response = new byte[4];
  * 
  * //block until the server responds
  * socket.getInputStream.read(response);
  * 
  * </pre>
  * 
  * @author Daniel Yule (daniel.yule@gmail.com)
  * 
  */
 public class MockServer implements TestRule {
 
 	/**
 	 * The port this {@link MockServer} will connect on.
 	 */
 	private int port;
 
 	/**
 	 * The connections that this server creates through the course of its
 	 * lifecycle, hashed on the map that they are created with. In the situation
 	 * where all tests run on the same thread, the object will be replaced each
 	 * test.
 	 */
 	private Map<Thread, MockServerConnection> connections = new HashMap<>();
 
 	@Override
 	public Statement apply(Statement base, Description description) {
 		if (base != null) {
 			return statement(base);
 		}
 		return base;
 	}
 
 	/**
 	 * Generates a new {@link Statement} which wraps the base statement in an
 	 * {@link #init() initialization} step before it is called and a
 	 * {@link #verify() verification} step afterwards
 	 * 
 	 * @param base
 	 *            The statement we will be wrapping
 	 * @return A {@link Statement} which will delegate a call to
 	 *         <code>base</code> after initialization and before verification.
 	 */
 	private Statement statement(final Statement base) {
 		return new Statement() {
 			@Override
 			public void evaluate() throws Throwable {
 				MockServerConnection connection = new MockServerConnection(port);
 				connections.put(Thread.currentThread(), connection);
 				base.evaluate();
 				connection.verify();
 
 			}
 		};
 	}
 
 	/**
 	 * Create a new MockServer that will listen on the given port.
 	 * 
 	 * @param port
 	 *            The port number to listen on.
 	 */
 	public MockServer(int port) {
 		this.port = port;
 	}
 
 	/**
 	 * An {@link OutputStream} representing the expectation for this server.
 	 * Anything written to this output stream will be expected to be received by
 	 * the server or the test will fail.
 	 * 
 	 * @return An {@link OutputStream} for writing expectation
 	 */
 	public OutputStream expected() {
 		return connections.get(Thread.currentThread()).expected();
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
 		return connections.get(Thread.currentThread()).response();
 	}
 
 }
