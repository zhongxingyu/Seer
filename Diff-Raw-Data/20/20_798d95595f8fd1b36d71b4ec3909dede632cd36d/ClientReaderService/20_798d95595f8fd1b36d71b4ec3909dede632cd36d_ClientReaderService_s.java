 package br.ufrj.tp.chat.client;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.Socket;
 
 public class ClientReaderService extends Thread {
 	
 	private BufferedReader serverToClient;
 	private String clientId;
 	private boolean active = true;
 	
 	public ClientReaderService(Socket socket, String clientId) throws IOException {
 		serverToClient = new BufferedReader( new InputStreamReader(socket.getInputStream()) );
 		this.clientId = clientId;
 	}
 	
 	@Override
 	public void run() {
 		Janela.escreve("Conectado como " + clientId);
 		try {
 			while (active) {
 				String message;
 				message = serverToClient.readLine();
				
				if ( !message.startsWith(clientId) ) {
					Janela.escreve(message);
				}
 				
 			}
 		} catch (IOException e) {
 			Janela.escreve("Erro ao ler mensagens do servidor.");
 			e.printStackTrace();
 		}
 	}
 	
 	public void shutdown() {
 		active = false;
 	}
 	
 }
