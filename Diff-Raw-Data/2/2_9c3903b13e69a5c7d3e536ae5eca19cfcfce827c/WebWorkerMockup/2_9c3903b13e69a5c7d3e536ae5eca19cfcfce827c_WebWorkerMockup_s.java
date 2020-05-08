 package webServer;
 
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 
 import webServer.messages.DropOfferRequest;
 import webServer.messages.GetProfileRequest;
 import webServer.messages.LaunchOfferRequest;
 import webServer.messages.LoginRequest;
 import webServer.messages.LogoutRequest;
 import webServer.messages.SetProfileRequest;
 
 import data.LoginCred;
 import data.UserProfile;
 
 /**
  * WebServiceWorker mockup implementation.
  * 
  * @author Paul Vlase <vlase.paul@gmail.com>
  */
 public class WebWorkerMockup implements Runnable {
 	public WebServerMockup	webServer;
 	public Socket			clientSocket;
 
 	public WebWorkerMockup(WebServerMockup webServer, Socket clientSocket) {
 		this.webServer = webServer;
 		this.clientSocket = clientSocket;
 	}
 
 	private void log(String str) {
 		System.out.println("[" + clientSocket.getInetAddress() + ":" + clientSocket.getPort() + "] " + str);
 	}
 
 	@Override
 	public void run() {
 		ObjectInputStream ois = null;
 		ObjectOutputStream oos = null;
 
 		System.out.println("[WebServiceWorkerMockup:run()] Started");
 		try {
 			ois = new ObjectInputStream(clientSocket.getInputStream());
 			oos = new ObjectOutputStream(clientSocket.getOutputStream());
 
 			Object requestObj = ois.readObject();
 			Object responseObj;
 			System.out.println("New conn");
 
			if (requestObj instanceof LoginCred) {
 				log("Login message");
 				responseObj = webServer.login((LoginRequest) requestObj);
 			} else if (requestObj instanceof LogoutRequest) {
 				log("Logout message");
 				responseObj = webServer.logout((LogoutRequest) requestObj);
 			} else if (requestObj instanceof LaunchOfferRequest) {
 				log("Launch offer message");
 				responseObj = webServer.launchOffer((LaunchOfferRequest) requestObj);
 			} else if (requestObj instanceof GetProfileRequest) {
 				responseObj = webServer.getProfile((GetProfileRequest) requestObj);
 			} else if (requestObj instanceof GetProfileRequest) {
 				responseObj = webServer.setProfile((SetProfileRequest) requestObj);
 			} else if (requestObj instanceof DropOfferRequest) {
 				log("Drop offermessage");
 				responseObj = webServer.dropOffer((DropOfferRequest) requestObj);
 			} else {
 				log("Unknow command");
 				responseObj = null;
 			}
 
 			oos.writeObject(responseObj);
 			oos.flush();
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				if (oos != null)
 					oos.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 			try {
 				if (ois != null)
 					ois.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 
 			try {
 				if (clientSocket != null)
 					clientSocket.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 }
