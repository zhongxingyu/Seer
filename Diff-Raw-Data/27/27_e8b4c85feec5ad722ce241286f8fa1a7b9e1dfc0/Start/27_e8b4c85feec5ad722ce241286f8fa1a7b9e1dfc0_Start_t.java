 package ar.edu.it.itba.pdc;
 
 import ar.edu.it.itba.pdc.Implementations.proxy.TCPSelector;
 import ar.edu.it.itba.pdc.Implementations.proxy.client.ProxyClientSelectorProtocol;
 import ar.edu.it.itba.pdc.Implementations.proxy.client.TCPClientSelector;
 import ar.edu.it.itba.pdc.Implementations.proxy.server.ProxyServerSelectorProtocol;
 import ar.edu.it.itba.pdc.Implementations.proxy.server.TCPServerSelector;
 import ar.edu.it.itba.pdc.Implementations.proxy.utils.Worker;
 import ar.edu.it.itba.pdc.Interfaces.ProxyWorker;
 
 public class Start {
 
 	public static void main(String[] args) throws InterruptedException {
 		ProxyWorker work = new Worker();
 		TCPSelector serverSelector = new TCPServerSelector(work, 9090,
 				new ProxyServerSelectorProtocol());
 		TCPSelector clientSelector = new TCPClientSelector(work, 9091,
 				new ProxyClientSelectorProtocol());
		
 		work.setServer(serverSelector);
 		work.setClient(clientSelector);
 
 		Thread server = new Thread(serverSelector);
 		Thread client = new Thread(clientSelector);
 		Thread worker = new Thread(work);
 
 		worker.start();
 		server.start();
 		client.start();
 
 		worker.join();
 		server.join();
 		client.join();
 	}
 }
