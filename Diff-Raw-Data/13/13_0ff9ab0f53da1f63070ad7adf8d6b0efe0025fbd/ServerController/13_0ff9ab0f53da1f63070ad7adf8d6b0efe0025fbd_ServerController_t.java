 import javax.net.ssl.SSLServerSocket;
 import javax.net.ssl.SSLServerSocketFactory;
 import javax.net.ssl.SSLSocket;
 import java.io.BufferedReader;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.BufferedWriter;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Properties;
 
 //import java.security.KeyStore;
 
 public class ServerController {
 	private SSLServerSocket _serverSocket;
 	private Model model;
 
 	public ServerController(int port, String password, String keyStoreName) {
 
 		//Instantiate a Model. Architecture subject to change
 		model = new Model(password.toCharArray());
 
 		//Use password based encryption as basis for SSL
 		Properties systemProps = System.getProperties();
 		systemProps.setProperty("javax.net.ssl.keyStore", keyStoreName);
 		systemProps.setProperty("javax.net.ssl.keyStorePassword",
 				password.toString());
 		System.setProperties(systemProps);
 
 		//Attempt to create a Socket Factory with the specified security parameters
 		SSLServerSocketFactory sslServerFactory = (SSLServerSocketFactory) SSLServerSocketFactory
 			.getDefault();
 		try {
 			_serverSocket = (SSLServerSocket) sslServerFactory
 				.createServerSocket(port);
 			_serverSocket
				.setEnabledCipherSuites(new String[] { "TLS_RSA_WITH_AES_256_CBC_SHA256" });
 			_serverSocket.setEnabledProtocols(new String[] { "TLSv1",
 				"TLSv1.1", "TLSv1.2" });
 
 			//Perpetually spawn a new socket and listen on it whenever one is accepted.
 			//Start a new thread to handle each connection.
 			for (;;) {
 				SSLSocket clientSocket = (SSLSocket) _serverSocket.accept();
 				Runnable connectionHandler = new ConnectionHandler(clientSocket);
 				new Thread(connectionHandler).start();
 			}
 
 		} catch (IOException e) {
 			System.err.println("Password Verification Failed");
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.err.println("Could not create socket on port " + port + ".");
 		}
 	}
 
 	public ServerController(String password) {
 		this(9999, password, "PrivKey");
 	}
 
 	private class ConnectionHandler implements Runnable {
 		private SSLSocket _sslSocket;
 		private String identity;
 		private String role;
 
 		public ConnectionHandler(SSLSocket sslSocket) {
 			_sslSocket = sslSocket;
 			identity = null;
 			role = null;
 		}
 
 		public void run() {
 			try {
 				InputStream inputStream = _sslSocket.getInputStream();
 				InputStreamReader inputStreamReader = new InputStreamReader(
 						inputStream);
 				BufferedReader bufferedReader = new BufferedReader(
 						inputStreamReader);
 
 				boolean forcePassChange=true;
 
 				while(true){
 					String command;
 					try {
 						command = bufferedReader.readLine();
 					} catch (Exception e) {
 						//e.printStackTrace();
 						break;
 					}
 
 					//Close politely when the client is done
 					if( command == null )
 						break;
 
 					// Always login first.
 					if( identity == null)
 						if (!command.equals("login"))
 							continue;
 
 					//Force a password change if one is needed.
 					if ( (identity!=null) && forcePassChange ){
 						try{
 							if(!command.equals("changePassword"))
 								throw new Exception("Client refused to change password");
 							forcePassChange = false;
 						} catch (Exception e){
 							//System.err.println(e.getMessage());
 							//Close Session
 							identity = null;
 							role = null;
 							return;
 						}
 					}
 
 
 					switch (command) {
 						case "login" :
 							forcePassChange = this.login(bufferedReader);
 							break;
 						case "sendFile":
 							//TODO Remove this and do not use it.
 							this.receiveFile(inputStream, bufferedReader);
 							break;
 						case "changePassword":
 							this.changePassword(bufferedReader);
 							break;
 						case "resetPassword":
 							this.resetPassword(bufferedReader);
 							break;
 						case "createUser":
 							if(!role.equals("admin"))
 								break;
 							this.createUser(bufferedReader);
 							break;
 						case "getCourseList":
 							this.getCourseList( );
 							break;
 						case "createCourse":
 							this.createCourse(bufferedReader);
 							break;
 						case "getCoursesForUser":
 							this.getCoursesForUser(bufferedReader);
 							break;
 						case "getCourseRole":
 							this.getCourseRole(bufferedReader);
 							break;
 						case "addUserToCourse":
 							this.addUserToCourse(bufferedReader);
 							break;
 						case "createAssignment":
 							this.createAssignment(bufferedReader);
 							break;
 						case "getAssignmentsForCourse":
 							this.getAssignmentsForCourse();
 							break;
 						case "getCourseHandouts":
 							this.getCourseHandouts(bufferedReader);
 							break;
 						case "addHandout":
 							this.addHandout(inputStream);
 							break;
 						case "getHandoutFile":
 							this.getHandoutFile();
 							break;
 						case "getAssignmentsForUser":
 							this.getAssignmentsForUser(bufferedReader);
 							break;
 						case "makeSubmission":
 							this.makeSubmission(inputStream);
 							break;
 						case "getSubmissionsForAssignment":
 							this.getSubmissionsForAssignment();
 							break;
 						case "getSubmissionFile":
 							this.getSubmissionFile();
 							break;
 						case "updateGradesForAssignment":
 							this.updateGradesForAssignment();
 							break;
 						default:
 							//System.out.println("Command not found");
 							// Above should throw an exception
 							break;
 					}
 				}
 
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 
 		private boolean login(BufferedReader bufferedReader) throws IOException {
 			String username = bufferedReader.readLine();
 			//char[] password = receiveChar();
 			String isOneTime = "f";
 
 			String [] login_output= {null, null, null};
 			String loginResult = "succeed";
 
 			try{
 				login_output = model.login(username, receiveChar());
 				identity = login_output[0];
 				role = login_output[1];
 				isOneTime = login_output[2];
 			} catch (Exception e) {
 				identity = null;
 				role = null;
 				loginResult = e.getMessage();
 				e.printStackTrace();
 			}
 
 			OutputStream outputStream = _sslSocket.getOutputStream();
 			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(
 					outputStream);
 			BufferedWriter bufferedWriter = new BufferedWriter(
 					outputStreamWriter);
 
 			bufferedWriter.write(identity + '\n');
 			bufferedWriter.write(role + '\n');
 			bufferedWriter.write(isOneTime + '\n');
 			bufferedWriter.write(loginResult + '\n');
 			bufferedWriter.flush();
 			return (isOneTime.equals("t"));
 
 		}
 
 		//TODO remove
 		private void receiveFile(InputStream inputStream,
 				BufferedReader bufferedReader) throws IOException {
 
 			//Get filename and size
 			String filename = bufferedReader.readLine() + ".out";
 			int filesize = Integer.parseInt(bufferedReader.readLine());
 
 			//Tell the model to save the file. It will automatically be encrypted.
 			model.saveFile(inputStream,filename,filesize, identity);
 			//model.decrypt(filename, filename+".decrypt",filesize); //Debug
 
 			//Get output stream to send an ack.
 			OutputStream outputStream = _sslSocket.getOutputStream();
 			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
 			BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
 
 			//Send an ack.
 			bufferedWriter.write("succeed"+'\n');
 			bufferedWriter.flush();
 		}
 
 		private void changePassword(BufferedReader bufferedReader)
 			throws Exception {
 
 			OutputStream os = _sslSocket.getOutputStream();
 			OutputStreamWriter osw = new OutputStreamWriter(os);
 			BufferedWriter bw = new BufferedWriter(osw);
 
 			try {
 				model.changePassword(identity, identity,
 						receiveChar(),
 						receiveChar());
 			} catch (Exception e) {
 				bw.write("fail"+'\n');
 				bw.write(e.getMessage()+'\n');
 				bw.flush();
 				return;
 			}
 
 			bw.write("succeed"+'\n'+role+'\n');
 			bw.flush();
 
 		}
 
 		private void resetPassword(BufferedReader bufferedReader)
 			throws Exception {
 			char [] newPassword=null;
 			String userName = receiveString();
 
 			try {
 				newPassword = model.resetPassword(identity,userName);								
 			} catch (Exception e) {
 				sendString("fail");
 			}
 			sendString("succeed");
 			sendChar(newPassword);
 		}
 
 		private void createUser(BufferedReader bufferedReader)
 			throws IOException {
 
 			String username = bufferedReader.readLine();
 			String role = bufferedReader.readLine();
 			char [] newPassword = null;
 
 			//Get output stream to send the password back
 			OutputStream outputStream = _sslSocket.getOutputStream();
 			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
 			BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
 
 			try{
 				newPassword = model.createUser(identity,username,role.toCharArray());
 			} catch (Exception e){
 				bufferedWriter.write(e.getMessage()+'\n');
 				bufferedWriter.flush();
 				return;
 			}
 
 			//Send the password back.
 			bufferedWriter.write(new String(newPassword)+'\n');
 			bufferedWriter.flush();
 
 		}
 
 		private void getCourseList()
 			throws Exception {
 
 			LinkedList<String> courseList = model.getCourseList();
 			sendObject(courseList);
 		}
 
 		//Assumes professor makes course. Otherwise just send an
 		//extra line in the client.
 		private void createCourse(BufferedReader bufR)
 			throws Exception {
 			//String professorName = bufR.readLine();
 			String professorName = identity;
 			String courseName = bufR.readLine();
 
 			model.createCourse(professorName,courseName);
 		}
 
 		private void getCoursesForUser(BufferedReader bufR)
 			throws Exception{
 			String username = bufR.readLine();
 			sendObject(model.coursesForUser(identity,username));
 		}
 
 		private void getCourseRole(BufferedReader bufR)
 			throws Exception{
 			String courseName = receiveString();
 			String userName = receiveString();
 			String role = model.getCourseRole(courseName,userName);
 			sendString(role);
 		}
 
 		private void addUserToCourse(BufferedReader bufR)
 			throws Exception{
 			String userName = receiveString();
 			String courseName = receiveString();
 			String courseRole = receiveString();			
 
 			model.addUserToCourse(identity,userName, courseName, courseRole);
 		}
 
 		private void createAssignment(BufferedReader bufR)
 			throws Exception{
 			String courseName = receiveString();
 			String assignmentName = receiveString();
 
 			model.createAssignment(identity, courseName, assignmentName);
 		}
 
 		private void getAssignmentsForCourse()
 			throws Exception{
 			String courseName = receiveString();
 			sendObject( model.getAssignmentsForCourse(identity,courseName) );
 		}
 
 		private void getCourseHandouts(BufferedReader bufR)
 			throws Exception{
 			String courseName = receiveString();
 
 			sendObject(model.getCourseHandouts(identity,courseName));
 		}
 
 		private void getAssignmentsForUser(BufferedReader bufR)
 			throws Exception{
 			String courseName = receiveString();
 			String userName = receiveString();
 
 			sendObject(model.getAssignmentsFor(identity,courseName, userName));
 		}
 
 		private void addHandout(InputStream inputStream)
 			throws Exception{
 			String courseName = receiveString();
 			String handoutName = receiveString();
 			int filesize = (int) receiveObject();
 			model.addHandout(identity,courseName,handoutName,inputStream,filesize);
 
 		}
 
 		private void getHandoutFile()
 			throws Exception{
 			String courseName = receiveString();
 			String handoutName = receiveString();
 			streamFile(
 					model.getHandoutStream(identity,courseName,handoutName));
 		}
 
 		private void makeSubmission(InputStream inputStream)
 			throws Exception{
 			String courseName = receiveString();
 			String username = receiveString();
 			String assignmentName = receiveString();
 			int filesize = (int) receiveObject();
 			model.makeSubmission(identity,courseName,username,assignmentName,inputStream,filesize);
 		}
 
 		private void getSubmissionsForAssignment()
 			throws Exception{
 			String courseName = receiveString();
 			String assignmentName = receiveString();
 			HashMap<String,Assignment> submissions =
 				model.getSubmissions(identity,courseName, assignmentName);
 			sendObject(submissions);
 		}
 
 		private void getSubmissionFile()
 			throws Exception{
 			String courseName = receiveString();
 			String assignmentName = receiveString();
 			String username = receiveString();
 			streamFile(
 					model.getSubmissionFile(identity,courseName,assignmentName,username));
 		}
 
 		private void updateGradesForAssignment()
 			throws Exception{
 			String courseName = receiveString();
 			String assignmentName = receiveString();
 			HashMap<String,Assignment> gradebook = 
 				(HashMap<String,Assignment>) receiveObject();
 			model.updateGradesFor(identity,courseName, assignmentName, gradebook);
 		}
 
 
 
 		////////////// Helper Methods to save typing
 
 		private void sendObject(Object obj)
 			throws Exception {
 			ObjectOutputStream oos = new ObjectOutputStream(
 					_sslSocket.getOutputStream());
 			oos.reset();
 			oos.writeObject(obj);
 			oos.flush();
 		}
 
 		private Object receiveObject()
 			throws Exception{
 			ObjectInputStream ois = new ObjectInputStream(
 					_sslSocket.getInputStream());
 			return ois.readObject();
 		}
 
 		private void sendString(String str)
 			throws Exception {
 			OutputStream outputStream = _sslSocket.getOutputStream();
 			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
 			BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
 
 			bufferedWriter.write(str+'\n');
 			bufferedWriter.flush();
 		}
 
 		private String receiveString()
 			throws Exception{
 			InputStream inputStream = _sslSocket.getInputStream();
 			InputStreamReader inputStreamReader = new InputStreamReader(
 					inputStream);
 			BufferedReader bufferedReader = new BufferedReader(
 					inputStreamReader);
 			return bufferedReader.readLine();
 		}
 		
 		private char[] receiveChar()
 			throws Exception{
 			InputStream inputStream = _sslSocket.getInputStream();
 			InputStreamReader inputStreamReader = new InputStreamReader(
 					inputStream);
 			BufferedReader bufferedReader = new BufferedReader(
 					inputStreamReader);
 			int size = Integer.parseInt(bufferedReader.readLine());
 			byte[] passwordBytes = new byte[size];
 			inputStream.read(passwordBytes, 0, size);
 			char[] password = new char[size/2];
 			for(int i = 0; i<size; i = i+2) {
 				password[i/2] = (char)((passwordBytes[i]<<8) | (passwordBytes[i+1]));
 			}
 			
 			return password;
 		}
 		
 		private void sendChar(char[] pass)
 			throws Exception{
 			OutputStream outputStream = _sslSocket.getOutputStream();
 			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
 			BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
 
 			int size = pass.length;
 			byte[] password = new byte[size*2];
 			for(int i=0; i<pass.length; i++) {
 					password[2*i] = (byte) ((pass[i]&0xFF00)>>8); 
 					password[2*i+1] = (byte) (pass[i]&0x00FF); 
 			}
 			bufferedWriter.write(Integer.toString(2*size)+'\n');
 			bufferedWriter.flush();
 			outputStream.write(password, 0, size*2);
 			outputStream.flush();
 		}
 
 		private void streamFile(InputStream is)
 			throws Exception{
 			int bytesRead;
 			byte[] buf = new byte[1024];
 			while( (bytesRead = is.read(buf)) != -1)
 				_sslSocket.getOutputStream().write(buf,0,bytesRead);
 			is.close();
 			byte[] done = { (byte) 0xde, (byte) 0xad, (byte) 0xbe, (byte) 0xef};
 			_sslSocket.getOutputStream().write(done,0,4);
 			_sslSocket.getOutputStream().flush();
 		}
 
 
 	}
 
 	///// Tests and normal run behavior.
 
 	public static void normalRun(){
 		System.out.println("Enter server password: ");
 		InputStreamReader inputStreamReader = new InputStreamReader(System.in);
 		BufferedReader reader = new BufferedReader(inputStreamReader);
 		String password = null;
 		try {
 			password = reader.readLine();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		new ServerController(password);
 	}
 
 	public static void main(String[] args) {
 		normalRun();
 
 	}
 
 }
