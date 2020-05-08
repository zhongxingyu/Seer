 package brorlandi.client;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.Socket;
import java.net.SocketException;
 
 public class Client implements Runnable, ClientInterface{
 
 	private String mServerAddress; // 'localhost'
 	private int mPorta;
 	
 	private Socket mServer;
 	private BufferedReader mFromServerInput;
 	private BufferedWriter mToServerOutput;
 	
 	private ClientAbstractInput mInputThread;
 	private ClientCallbackInterface mClientCallback;
 	
 	private boolean mIsConnected; ///< Variavel para indicar se o cliente está conectado.
 	
 	public Client(String server, int porta, ClientCallbackInterface ccbi, ClientAbstractInput input){
 		mServerAddress = server;
 		mPorta = porta;
 		mClientCallback = ccbi;
 		mIsConnected = false;
 
 		Thread clientThread = new Thread(this); // Roda o cliente em uma Thread nova;
 		clientThread.start(); // inicia a Thread.
 		clientThread.setName("Client Thread"); // nome da Thread para debug
 		mInputThread = input;
 		if(mInputThread != null){
 			mInputThread.start(); // inicia a Thread
 			mInputThread.setName("Client Input Thread"); //nome da Thread para debug
 		}
 	}
 
 	public void run(){
 	
 		mServer = null;
 		try {
 			mServer = new Socket(mServerAddress, mPorta);
 			//System.out.println("Conectado ao Servidor em: "+ mServer.getInetAddress().getHostAddress() +":"+mServer.getPort());
 			mIsConnected = true;
 			mClientCallback.onClientConnected(this);
 		} catch (Exception e) {
 			mClientCallback.onException(e);
 			mIsConnected = false;
 			//System.out.println("ERRO: "+e.getMessage());
 			//System.exit(1);
 		}
         
         
 		// comunicação com o servidor
 		try{
 			mFromServerInput = new BufferedReader(new InputStreamReader(mServer.getInputStream()));
 			mToServerOutput = new BufferedWriter(new OutputStreamWriter(mServer.getOutputStream()));
 			String read;
 			
 			do{
 				read = mFromServerInput.readLine();
 				if(read != null){
 					mClientCallback.onMessageReceive(read);
 				}
 			}while(read != null && mIsConnected);
 			if(read == null){
 				mIsConnected = false;
 				mClientCallback.onServerDisconnected();
 			}
		}catch(SocketException se){
			mClientCallback.onServerDisconnected();
		} catch(Exception e){
 			mClientCallback.onException(e);
 		} finally {
             try {
     			mIsConnected = false;
             	mToServerOutput.close();
 	        	mFromServerInput.close();
 	            mServer.close();
             } catch (IOException e) {
             }
             //mServer.removeClientSession(this);
         }
 	}
 
 	@Override
 	public synchronized void sendMessage(String message) {
 		if(mIsConnected){
 			try {
 				mToServerOutput.write(message);
 				mToServerOutput.newLine();
 				mToServerOutput.flush();
 			} catch (IOException e) {
 				mClientCallback.onException(e);
 			}
 		}
 	}
 
 	@Override
 	public Socket getSocket(){
 		return mServer;
 	}
 
 	@Override
 	public boolean isClientConnected() {
 		return mIsConnected;
 	}
 
 	@Override
 	public void downClient() {
 		mIsConnected = false;
 		if(mInputThread != null)
 			mInputThread.closeClientInputThread();
 		try {
 			mServer.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 		/*
 		try{
 			mServerInput = new BufferedReader(new InputStreamReader(mServer.getInputStream()));
 			mServerOutput = new BufferedWriter(new OutputStreamWriter(mServer.getOutputStream()));
 
 			mensagemFeedback = "";
 			do{
 				System.out.println(mensagemFeedback);
 				mensagemFeedback = mServerInput.readLine();
 			}while(mensagemFeedback != null);
 			
 			mServerInput.close();
 			mServerOutput.close();
 			mServer.close();
 			if(mServer.isClosed() == true){
 				mInputThread.setConnected(false);
 				System.out.println("A conexao com o servidor foi fechada :(");
 			}
 		}catch(IOException e){
 			System.out.println("IOException ERRO: "+e.getMessage());
 			e.printStackTrace();
 		}catch(Exception e){
 			System.out.println("ERRO: "+e.getMessage());
 			e.printStackTrace();
 		} finally {
 
 			try {
 				mServerInput.close();
 				mServerOutput.close();
 				mServer.close();
 
 			} catch (IOException e) {
 			}
 
 		}
 	}
 
 	@Override
 	public void clientInput(String input) {
 		try {
 			mServerOutput.write(input);
 			mServerOutput.newLine();
 			mServerOutput.flush();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	*/
 }
