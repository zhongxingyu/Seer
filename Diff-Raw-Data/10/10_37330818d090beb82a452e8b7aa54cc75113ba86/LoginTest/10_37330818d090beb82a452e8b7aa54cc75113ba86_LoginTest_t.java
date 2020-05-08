 package test.dataportal;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.Statement;
 
 import junit.framework.TestCase;
 import net.sf.json.JSONObject;
 import net.sf.json.JSONSerializer;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.lang3.tuple.ImmutablePair;
 import org.apache.commons.lang3.tuple.Pair;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.webapp.WebAppContext;
 
 import test.dataportal.controllers.PersistenceUnitSingleton;
 
 /**
  * Unit test for login service at ciclope
  */
 public class LoginTest extends TestCase {
 
 	private static final String DB_PATH = "target/test";
 	private Server server;
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 
 		File dbFile = new File(DB_PATH + ".h2.db");
 		if (dbFile.exists()) {
 			dbFile.delete();
 		}
 
 		PersistenceUnitSingleton.setPersistenceUnit("functional-tests");
 
 		server = new Server(8080);
 		WebAppContext context = new WebAppContext();
 		context.setDescriptor("src/main/webapp/WEB-INF/web.xml");
 		context.setResourceBase("src/main/webapp/");
 		context.setContextPath("/dataportal");
 		context.setParentLoaderPriority(true);
 
 		server.setHandler(context);
 		server.start();
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		super.tearDown();
 		server.stop();
 	}
 
 	public void testLoginFailed() throws Exception {
 		String response = callService(new String[] { "request", "user",
 				"password" },
 				new String[] { "access", "wronguser", "wrongpass" });
 
 		JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(response);
 		assertFalse(jsonObject.getBoolean("success"));
 	}
 
 	public void testRegister() throws Exception {
 		String userName = "foo@foo.foo";
 		register(userName);
 		activate(userName);
 	}
 
 	public void testRegisterTwice() throws Exception {
 		// Register foo
 		String userName = "foo@foo.foo";
 		register(userName);
 
 		// Register foo again
 		registerFail(userName);
 
 		// Activate
 		activate(userName);
 
 		// Register foo again after foo is activated
 		registerFail(userName);
 	}
 
 	private void registerFail(String userName) throws IOException,
 			HttpException {
 		Pair<String, Integer> response = callServiceNoCheck(new String[] {
 				"request", "user", "password" }, new String[] { "register",
 				userName, "testpass" });
 		assertTrue(response.getRight() == 200);
		JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(response
				.getLeft());
 		assertFalse(jsonObject.getBoolean("success"));
 	}
 
 	private void activate(String userName) throws IOException, HttpException,
 			Exception {
 		int code = callServiceNoCheck(new String[] { "request", "hash" },
 				new String[] { "register", getUserHash(userName) }).getRight();
 
 		assertTrue(code == 302);// Redirect
 	}
 
 	private void register(String userName) throws IOException, HttpException {
 		String response = callService(new String[] { "request", "user",
 				"password" }, new String[] { "register", userName, "testpass" });
 
 		JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(response);
 		assertTrue(jsonObject.getBoolean("success"));
 	}
 
 	private String getUserHash(String userName) throws Exception {
 		Class.forName("org.h2.Driver");
 		Connection conn = DriverManager.getConnection("jdbc:h2:" + DB_PATH,
 				"sa", "");
 
 		Statement st = conn.createStatement();
 		ResultSet resultSet = st
 				.executeQuery("select * from \"users\" where \"id\"='"
 						+ userName + "'");
 		assertTrue(resultSet.first());
 
 		String hash = resultSet.getString("hash");
 
 		resultSet.close();
 		st.close();
 		conn.close();
 
 		return hash;
 	}
 
 	private String callService(String[] paramNames, String[] paramValues)
 			throws IOException, HttpException {
 		Pair<String, Integer> response = callServiceNoCheck(paramNames,
 				paramValues);
 		String content = response.getLeft();
 		int code = response.getRight();
 		assertTrue(code + "", code == 200);
 		return content;
 	}
 
 	private Pair<String, Integer> callServiceNoCheck(String[] paramNames,
 			String[] paramValues) throws IOException, HttpException {
 		HttpClient client = new HttpClient();
 		PostMethod post = new PostMethod(
 				"http://127.0.0.1:8080/dataportal/login");
 		for (int i = 0; i < paramValues.length; i++) {
 			post.addParameter(paramNames[i], paramValues[i]);
 		}
 
 		int code = client.executeMethod(post);
 		String ret = post.getResponseBodyAsString();
 		if (code != 200) {
 			System.err.println(ret);
 		}
 		post.releaseConnection();
 		Pair<String, Integer> response = new ImmutablePair<String, Integer>(
 				ret, code);
 		return response;
 	}
 }
