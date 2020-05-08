 package br.ufu.facom.network.dlontology.test;
 
 import br.ufu.facom.network.dlontology.FinSocket;
 
 public class Receiver {
 	public static void main(String[] args) {
 		FinSocket finSocket = new FinSocket();
 		
 		if(finSocket.open()){
 			try{
 				finSocket.register("Receiver");
 				finSocket.join("Trial");
				finSocket.read();
 			}finally{
 				finSocket.close();
 			}
 		}
 	}
 }
