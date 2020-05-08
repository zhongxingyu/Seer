 package uebung5.aufgabe1;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.OutputStreamWriter;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.HashMap;
 import java.util.Map;
 
 public class ServerWorker extends BaseWorker implements Runnable {
 
 	final static String persistenceFileName = "counter.persistence";
 	int port;
 
 	public ServerWorker(int port) {
 		this.port = port;
 	}
 
 	/**
 	 * Lädt die Http-Header-Daten in eine Map-Datenstruktur
 	 * 
 	 * @param stream
 	 * @return
 	 * @throws IOException
 	 */
 	public Map<String, String> getHeaderFields(InputStream stream)
 			throws IOException {
 		BufferedReader requestStream = new BufferedReader(
 				new InputStreamReader(stream));
 
 		Map<String, String> map = new HashMap<String, String>();
 
 		// Gültiger Stream?
		if (requestStream != null) {
 
 			char[] buffer = new char[256];
 			// Liest 256-Bytes in den Buffer
 			// Verhindert, dass Angreifer mit Byte-Blob-Daten im Http-Header
 			// den Server in die Knie zwingen
 			requestStream.read(buffer, 0, buffer.length);
 
 			// Http-Header-Zeilen werden immer mit "\r\n"-abgeschloßen
 			String[] lines = new String(buffer).split("\r\n");
 
 			// Erste Zeile ist die HTTP-Anfrage
 			if (lines.length > 0 && lines[0] != null)
 				map.put(null, lines[0].trim());
 
 			// Http-Header Daten
 			// Fieldname : Fieldvalue
 			for (int i = 1; i < lines.length; ++i) {
 				int colonIndex = lines[i].indexOf(':');
 
 				if (colonIndex != -1) {
 					String fieldName = lines[i].substring(0, colonIndex).trim();
 					String fieldValue = lines[i].substring(colonIndex + 1)
 							.trim();
 
 					map.put(fieldName, fieldValue);
 				}
 
 			}
 		}
 		return map;
 	}
 
 	/**
 	 * Lädt eine serialisierte Int-Variable und setzt diese in ein HTML-Template
 	 * 
 	 * @param responseMsg
 	 * @return
 	 */
 	public Integer loadCounter() {
 
 		Integer counter = 0;
 		ObjectInputStream ois;
 		try {
 			ois = new ObjectInputStream(
 					new FileInputStream(persistenceFileName));
 			counter = ois.readInt();
 			ois.close();
 
 		} catch (FileNotFoundException e) {
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return counter;
 	}
 
 	private void saveCounter(Integer counter) {
 		ObjectOutputStream oos;
 		try {
 			oos = new ObjectOutputStream(new FileOutputStream(
 					persistenceFileName));
 			oos.writeInt(counter);
 			oos.close();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Liefert eine HTML-Website aus und speichert die User-Agents
 	 */
 	public void run() {
 		ServerSocket socket = null;
 		try {
 			socket = new ServerSocket(port);
 			Map<String, Integer> statistic = BaseWorker.loadStatistic();
 			Integer counter = loadCounter(); 
 
 			while (true) {
 				// Anfrage entgegennehmen
 				Socket connection = null;
 				try {
 					connection = socket.accept();
 					
 					// Timeout setzen, damit read() nicht für immer blocken kann.
 					// Dieser muss groß genug für Clients mit hohen Latenzen sein.
 					// Z.Zt. wird nur eine Verbindung zu einem Zeitpunkt behandelt.
 					// TODO: Klären ob ein Thread pro Verbindung sinnvoller ist.
 					connection.setSoTimeout(500);
 	
 					InputStream requestStream = connection.getInputStream();
 					
					// Now that we know, there's at least some payload
 					Map<String, String> fieldMap = this.getHeaderFields(requestStream);
 
 					final String responseMessage = (getFileContent("404.html"))
 						.replace("<!-- COUNTER -->", counter.toString());
 					saveCounter(++counter);
 
 					OutputStreamWriter responseStream = new OutputStreamWriter(
 							connection.getOutputStream());
 					// HTTP-Header
 					responseStream.append("HTTP/1.1 200 OK\r\n");
 					responseStream
 							.append("Content-Type: text/html;charset=utf-8\r\n");
 					responseStream.append("\r\n");
 					// HTML-Content
 					responseStream.append(responseMessage);
 					responseStream.close();
 					connection.close();
 	
 					// Ermittelt den User-Agent
 					String userAgent = stripUserAgent(fieldMap.get("User-Agent"));
 	
 					// Speichert den User-Agent zu Statistikzwecken ab
 					if (userAgent != null) {
 						if (statistic.containsKey(userAgent))
 							statistic.put(userAgent, statistic.get(userAgent) + 1);
 						else
 							statistic.put(userAgent, 1);
 	
 						BaseWorker.saveStatistic(statistic);
 					}
 				//u.a. SocketException, SocketTimeoutExcept
 				} catch (IOException e) {
 					if(connection != null)
 						connection.close();
 				}
 			}
 		} catch (Exception e) {
 			System.err.println("Unknown error: " + e.getMessage());
 			System.err.println("Trace:\n\n" + e.getStackTrace());
 		} finally {
 			try {
 				if (socket != null)
 					socket.close();
 			} catch (IOException e) {
 				System.err.println("Could not close socket");
 			}
 		}
 	}
 
 	/**
 	 * Formatiert die userAgent-Zeile in ein übersichtlicheres Format
 	 * 
 	 * @param userAgent
 	 * @return
 	 */
 	public String stripUserAgent(String userAgent) {
 		String[] commonUserAgents = { "Firefox", "MSIE", "Chrome", "Safari",
 				"Opera", "curl" };
 
 		if (userAgent != null) {
 			for (int i = 0; i < commonUserAgents.length; ++i)
 				if (userAgent.contains(commonUserAgents[i]))
 					return commonUserAgents[i];
 		}
 
 		return "Unknown";
 	}
 
 }
