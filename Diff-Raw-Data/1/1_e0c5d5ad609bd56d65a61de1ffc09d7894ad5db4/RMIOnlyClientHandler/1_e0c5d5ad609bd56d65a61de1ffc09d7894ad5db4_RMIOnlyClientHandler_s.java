 package ch.hsr.objectCaching.rmiOnlyServer;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import ch.hsr.objectCaching.account.Account;
 import ch.hsr.objectCaching.account.AccountService;
 import ch.hsr.objectCaching.dto.MethodCall;
 import ch.hsr.objectCaching.dto.ReturnValue;
 import ch.hsr.objectCaching.interfaces.ClientHandler;
 import ch.hsr.objectCaching.interfaces.MethodCalledListener;
 
 public class RMIOnlyClientHandler extends ClientHandler {
 
 	protected RMIOnlySkeleton skeletonInUse;
 	private AccountSkeleton accountSkeleton;
 	private AccountServiceSkeleton accountServiceSkeleton;
 	protected String clientIpAdress;
 	protected ArrayList<MethodCalledListener> listeners;
 
 	public void setAccountSkeleton(AccountSkeleton skeleton) {
 		this.accountSkeleton = skeleton;
 	}
 
 	public RMIOnlySkeleton getSkeleton() {
 		return skeletonInUse;
 	}
 
 	@Override
 	public void run() {
 		try {
 			MethodCall methodCall;
 			while ((methodCall = readMethodCallfrom()) != null) {
 				methodCall.setClientIp(clientIpAdress);
 				notifiyListeners(methodCall);
 				processMethodCall(methodCall);
 			}
 			objectInputStream.close();
 			objectOutputStream.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	protected void notifiyListeners(MethodCall methodCall) {
 		for (MethodCalledListener listener : listeners) {
 			listener.methodCalled(methodCall.getMethodName(),
 					methodCall.getClientIp());
 		}
 	}
 
 	MethodCall readMethodCallfrom() throws IOException, ClassNotFoundException {
 		Object objectFromStream = null;
 		if ((objectFromStream = objectInputStream.readObject()) != null) {
 			MethodCall methodCall = (MethodCall) objectFromStream;
 			return methodCall;
 		}
 		return null;
 	}
 
 	protected void setSkeleton(MethodCall methodCall) {
 		if (methodCall.getClassName().equals(Account.class.getName())) {
 			skeletonInUse = accountSkeleton;
 		}
 		if (methodCall.getClassName().equals(AccountService.class.getName())) {
 			skeletonInUse = accountServiceSkeleton;
 		}
 	}
 
 	protected void processMethodCall(MethodCall methodCall) {
 		setSkeleton(methodCall);
 		ReturnValue returnValue = skeletonInUse.invokeMethod(methodCall);
 		send(returnValue);
 	}
 
 	void sendResponse(ReturnValue returnValue) {
 		try {
 			objectOutputStream.writeObject(returnValue);
 			objectOutputStream.flush();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void setAccountServiceSkeleton(
 			AccountServiceSkeleton accountServiceSkeleton) {
 		this.accountServiceSkeleton = accountServiceSkeleton;
 	}
 
 	@Override
 	public void setClientIpAddress(String clientIpAddress) {
 		this.clientIpAdress = clientIpAddress;
 	}
 	
 	@Override
 	public String getClientIpAddress() {
 		return clientIpAdress;
 	}
 
 	public void setMethodCalledListeners(
 			ArrayList<MethodCalledListener> listeners) {
 		this.listeners = listeners;
 	}
 }
