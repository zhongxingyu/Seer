 /**
  *
  */
 package org.promasi.network.tcp;
 import java.io.IOException;
 import java.net.*;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 import org.promasi.utilities.design.Observer;
 
 /**
  * @author m1cRo
  *
  */
 public class TcpServer extends Observer<ITcpServerListener>
 {
 	/**
 	 *
 	 */
 	private ServerSocket _serverSocket;
 
 	/**
 	 *
 	 */
 	private Thread _acceptThread;
 
 	/**
 	 * 
 	 */
 	private Thread _clientCheckThread;
 	
 	/**
 	 * 
 	 */
 	private List<TcpClient> _clients;
 	
 	/**
 	 *
 	 */
 	private boolean _listening;
 	
 	/**
 	 * 
 	 */
 	private Lock _lockObject;
 	
 	/**
 	 *
 	 */
 	public TcpServer()
 	{
 		_acceptThread=null;
 		_clientCheckThread=null;
 		_listening=false;
 		_clients=new LinkedList<TcpClient>();
 		_lockObject = new ReentrantLock();
 	}
 
 	/**
 	 * 
 	 * @param portNumber
 	 * @return
 	 */
 	public boolean start(int portNumber)
 	{
 		if(_listening){
 			return false;
 		}
 		
 		try{
 			_serverSocket=new ServerSocket(portNumber);
 			_listening=true;
 			_acceptThread=new Thread(new Runnable() {
 					
 				@Override
 				public void run() {
 					while(_listening){
 						try{
 							Socket clientSocket=_serverSocket.accept();
 							try{
 								_lockObject.lock();
 								TcpClient client=new TcpClient(clientSocket);
 								_clients.add(client);
 								for(ITcpServerListener listener : getListeners()){
 									listener.clientConnected(client);
 								}	
 							}finally{
 								_lockObject.unlock();
 							}
 						}catch(IOException e){
 							//Logger
 						}catch(NetworkException e){
 							//Logger
 						}	
 					}
 				}
 			});
 				
 			_acceptThread.start();
 			_clientCheckThread=new Thread(new Runnable() {
 					
 				@Override
 				public void run() {
 					while(_listening){
 						try{
 							_lockObject.lock();
 							List<TcpClient> clients=new LinkedList<TcpClient>();
 							for(TcpClient client : _clients){
 								if(client.isConnected()){
 									clients.add(client);
 								}
 							}
 								
 							for(TcpClient client : new LinkedList<>(_clients)){
 								if(!clients.contains(client)){
 									for(ITcpServerListener listener : getListeners()){
 										listener.clientDisconnected(client);
 									}
 								}
 							}
 							
 							_clients=clients;
							Thread.sleep(100);
						}catch( InterruptedException e){
							
 						}finally{
 							_lockObject.unlock();
 						}
 					}
 				}
 			});
 				
 			_clientCheckThread.start();
 			for(ITcpServerListener listener : getListeners()){
 				listener.serverStarted(portNumber);
 			}
 			
 		}catch(IOException e){
 			return false;
 		}
 
 		return true;	
 	}
 
 
 	/**
 	 * 
 	 * @return
 	 */
 	public boolean stop(){
 		try{
 			_lockObject.lock();
 			_serverSocket.close();
 			_listening=false;
 			while(_acceptThread.isAlive() || _clientCheckThread.isAlive()){
 				try{
 					Thread.sleep(100);
 				} catch (InterruptedException e){
 					e.printStackTrace();
 				}
 			}
 				
 			for(ITcpServerListener listener : getListeners()){
 				listener.serverStopped();
 			}
 				
 			_clients.clear();
 			_acceptThread=null;
 		}catch(IOException e){
 			return false;
 		}finally{
 			_lockObject.unlock();
 		}
 			
 		return true;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public boolean isRunning(){
 		boolean result = false;
 		
 		try{
 			_lockObject.lock();
 			result = _listening;
 		}finally{
 			_lockObject.unlock();
 		}
 		
 		return result;
 	}
 	
 }
