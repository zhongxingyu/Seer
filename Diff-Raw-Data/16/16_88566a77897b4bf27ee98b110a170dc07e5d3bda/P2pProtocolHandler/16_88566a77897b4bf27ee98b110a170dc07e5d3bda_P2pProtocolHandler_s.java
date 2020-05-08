 import java.util.concurrent.*;
 import java.io.*;
 import java.net.*;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Manejador del protocolo p2p.
  */
 public class P2pProtocolHandler{
     private final int  NULL_HASHID       = 0xffffffff;
     private final int  APP_PORT          = 5947;
     // Estructuras de control
     private static HashMap<String,Song> SongDB;
     private static ArrayList<InetAddress> NodeDB;
     private static ConcurrentHashMap<Integer,String> ConsultDB;
     private static String host;
     private String id;
     
     /**
      * Constructor por defecto.
      */
     public P2pProtocolHandler() {
         SongDB = null;
         NodeDB = null;
         ConsultDB = null;
         host = null;
     }
     
     /**
      * Constructor.
      * @param knownNodesFilePath Localización de los nodos conocidos.
      * @param musicLib Localización de la librería.
      * @param id Identificador único del nodo.
      */
     public P2pProtocolHandler(String knownNodesFilePath, String musicLib,String id){
         ConsultDB = new ConcurrentHashMap<Integer,String>();
         NodeDB = parseKnownNodesFile(knownNodesFilePath);
         SongDB = parseSongFile(musicLib);
 	this.id = id;
 	try{
 	    host = InetAddress.getLocalHost().getHostAddress();
 	}
 	catch(UnknownHostException e){
 	    System.out.println("Error recuperando la Ip del servidor: ");
 	}
     }
     
      /**
      * 
      * Parsea la librería de música XSPF.
      * @param musicLib ruta a la biblioteca de música.
      * @return Un HashMap que mapea de "autor-título" a la estructura.
      * canción. La estructura canción contiene información adicional.
      */
     private HashMap<String,Song> parseSongFile(String musicLib){
         HashMap<String,Song> resp = ParseXSPF.parse(musicLib);
         return resp;
     }
     
     /**
      * Parsea el archivo de nodos conocidos.
      * @param knownNodesFilePath ruta al archivo con los nodos conocidos por 
      * este nodo.
      * @return lista enlazada de direcciones IP de los nodos conocidos por
      * este nodo.
      */
     private ArrayList<InetAddress> parseKnownNodesFile(String knownNodesFilePath){
         ArrayList<InetAddress> Nodes = new ArrayList<InetAddress>();
         try {
             BufferedReader nodeFile = new BufferedReader(new
                     FileReader(knownNodesFilePath));
             String line;
             while ((line = nodeFile.readLine()) != null) {
                 if (line.length() != 0)
                     Nodes.add(InetAddress.getByName(line));
             }
        
         }
         catch(FileNotFoundException fnf) {
             System.out.println("Error al abrir archivo: "+fnf);
         }
         catch(IOException e){
 	    System.out.println("I/O Error: "+e);
 	}
         return Nodes;
     }
     
     /**
      * Obtiene los datos del pedido a partir de una conexión con el cliente.
      * @param s Socket por donde se obtiene el pedido p2p.
      * @return Objeto que contiene los datos del pedido.
      */
     public P2pRequest getRequest(Socket s) {
         P2pRequest req = null;
         try {
             // Preparar para leer datos
             ObjectInputStream is = new ObjectInputStream(s.getInputStream());
             req = (P2pRequest) is.readObject();
         }
         catch (ClassNotFoundException csnf) {
             System.out.println("Error: "+csnf);
         }
         catch (IOException e ) {
             System.out.println("I/O Error: "+e);
 	}
         return req;
     }
     
     /**
      * Ejecuta un comando C.
      * @param req Parámetros del comando C.
      * @param cs Socket de comunicación.
      */
     public void makeConsult(P2pRequest req, Socket cs){
         // Crear comunicación con el cliente
         try {
             ObjectOutputStream os = new ObjectOutputStream(cs.getOutputStream());
             // Consulta repetida ?
             if (!ConsultDB.isEmpty() && ConsultDB.containsKey(req.hash_id)) {
                 // No atiendo la consulta porque ya lo hice en el pasado
                 String emptyString = "";
                 P2pRequest nulAnswer =
 		    new P2pRequest(NULL_HASHID,0,
 				   emptyString.getBytes());
                 os.writeObject(nulAnswer);
                 os.close();
                 return;
             }
             else {
                 String resultadoFinal = "";
                 // Agregar hash de consulta a mi base de datos
                 ConsultDB.put(req.hash_id, "");
                 // Verificar tipo de consulta: Autor, Titulo o todas
                 String tipoReq = new String(req.data);
                 String[] st = tipoReq.split("@@");
                 String expr = null;
                 if (st.length > 1)
                     expr = st[1].toLowerCase();
                 
                 if (st[0].compareTo("W") == 0) {
                     // Todas las canciones de la red
                     resultadoFinal = SongDbToString(this.id);
                 }
                 else if (st[0].compareTo("T") == 0) {
                     // Por título
                     Pattern regex = Pattern.compile(expr);
                     Matcher m;
                     Collection<Song> s = SongDB.values();
                     Iterator<Song> it = s.iterator();
                     while (it.hasNext()) {
                         Song sg = it.next();
                         m = regex.matcher(sg.title);
                         if (m.find()) { // Hubo match
                             resultadoFinal = resultadoFinal.concat
                                     (sg.toString()+"@@"+
                                     P2pProtocolHandler.host+"@@"+this.id+"##");
                         }
                         m.reset();
                     }
                 }
                 else if (st[0].compareTo("A") == 0) {
                     // Por autor
                     Pattern regex = Pattern.compile(expr);
                     Matcher m;
                     Collection<Song> s = SongDB.values();
                     Iterator<Song> it = s.iterator();
                     while (it.hasNext()) {
                         Song sg = it.next();
                         m = regex.matcher(sg.creator);
                         if (m.find()) { // Hubo match
                             resultadoFinal = resultadoFinal.concat
                                     (sg.toString()+"@@"+
                                     P2pProtocolHandler.host+"@@"+this.id+"##");
                         }
                         m.reset();
                     }
                 }
                 // Preparar estructura de respuestas
                 String[] respuesta = new String[NodeDB.size()];
                 // Hacer consulta a mis nodos vecinos.
                 // Arreglo de threads
                 ConsultThread[] ct = new ConsultThread[NodeDB.size()];
                 // Crear cada uno de los threads y ejecutarlos.
                 for(int i = 0; i < NodeDB.size(); i++) {
                     ct[i] = new ConsultThread(i, respuesta, NodeDB.get(i),
                             req, APP_PORT, this);
                     ct[i].start();
                 }
                 // Espero que todos los threads terminen su ejecución
                 for(int i = 0; i < NodeDB.size(); i++) {
                     ct[i].join();
                 }
                 // Colocar todos los resultados en un solo String
                 for(int i = 0; i < NodeDB.size(); i++) {
                     resultadoFinal = resultadoFinal.concat(respuesta[i]);
                 }
                 // Construir respuesta
                 P2pRequest respFinal = new P2pRequest(NULL_HASHID,0,
                         resultadoFinal.getBytes());
                 // Mandar respuesta
                 os.writeObject(respFinal);
                 os.close();
                 return;
             }
         }
         catch(IOException e) {
             System.out.println("Error I/O: "+e);
         }
         catch(InterruptedException ie) {
             System.out.println("Interrupted exception: "+ie);
         }
     }
     
     /**
      * Genera un string representativo de la base de datos de canciones.
      * @param nodeID Identificador único del nodo.
      * @return String representativo de la base de datos de canciones.
      */
     public String SongDbToString(String nodeID) {
         String resp = "";
         // Obtener todas las canciones de SongDB
         Collection<Song> s = SongDB.values();
         Iterator<Song> it = s.iterator();
         while (it.hasNext()) {
             Song se = it.next();
             resp = resp.concat(se.toString()+"@@"+
                     P2pProtocolHandler.host+"@@"+nodeID+"##");
         }
         return resp;
     }
     
     /**
      * Ejecuta un comando A.
      * @param req Parámetros del comando A.
      * @param cs Socket de comunicación.
      */
     public void makeReachable(P2pRequest req, Socket cs) {
         // Mandar respuesta al cliente
         String resp = "";
         try {
             ObjectOutputStream os = new ObjectOutputStream
                     (cs.getOutputStream());
             // Consulta repetida ?
             if (!ConsultDB.isEmpty() && ConsultDB.containsKey(req.hash_id)) {
                 // No atiendo la consulta porque ya lo hice en el pasado
                 String emptyString = "";
                 P2pRequest nulAnswer = new P2pRequest(NULL_HASHID,0,
                         emptyString.getBytes());
                 os.writeObject(nulAnswer);
                 os.close();
                 return;
             }
             else {
                 // Agregar hash de consulta a mi base de datos
                 ConsultDB.put(req.hash_id, "");
                 for(int i = 0; i < NodeDB.size(); i++) {
                     resp = resp.concat(NodeDB.get(i).getHostName()+"##");
                 }
                 // Preparar estructura de respuestas
                 String[] respuesta = new String[NodeDB.size()];
                 // Hacer consulta a mis nodos vecinos.
                 // Arreglo de threads
                 ConsultThread[] ct = new ConsultThread[NodeDB.size()];
                 // Crear cada uno de los threads y ejecutarlos.
                 for(int i = 0; i < NodeDB.size(); i++) {
                     ct[i] = new ConsultThread(i, respuesta, NodeDB.get(i),
                             req, APP_PORT, this);
                     ct[i].start();
                 }
                 // Espero que todos los threads terminen su ejecución
                 for(int i = 0; i < NodeDB.  size(); i++) {
                     ct[i].join();
                 }
                 // Colocar todos los resultados en un solo String
                 for(int i = 0; i < NodeDB.size(); i++) {
                     resp = resp.concat(respuesta[i]);
                 }
                 // Construir respuesta
                 P2pRequest ans = new P2pRequest(NULL_HASHID,0,resp.getBytes());
                 // Mandar respuesta
                 os.writeObject(ans);
                 os.close();
             }
         }
         catch(IOException e) {
             System.out.println("Error I/O: "+e);
         }
         catch(InterruptedException ie) {
             System.out.println("Interrupted exception: "+ie);
         }
         
     }
     
     /**
      * Ejecuta un comando D del lado del servidor.
      * @param req Parámetros del comando D del lado del servidor.
      * @param cs Socket de comunicación.
      */
     public void sendSong(P2pRequest req, Socket cs) {
         // Nombre de archivo ?
         String nombreMP3 = new String(req.data);
         // Buscar en SongDB
         String rutaArchivo = SongDB.get(nombreMP3).location;
         // Cargar archivo
         try {
             File cancion = new File(rutaArchivo);
             FileInputStream fin = new FileInputStream(cancion);
             byte contenidoMP3[] = new byte[(int) cancion.length()];
             fin.read(contenidoMP3);
             // Preparar P2pRequest con respuesta
             P2pRequest respuesta = new P2pRequest(NULL_HASHID,0,contenidoMP3);
             // Mandar respuesta al cliente
             ObjectOutputStream os = new ObjectOutputStream
                     (cs.getOutputStream());
             os.writeObject(respuesta);
             os.close();
             fin.close();
         }
         catch(FileNotFoundException fnf) {
             System.out.println("Error: "+fnf);
         }
         catch(NullPointerException nl) {}
         catch(IOException e) {
             System.out.println("Error I/O: "+e);
         }
     }
     
     /**
      * Permite descargar una canción desde un nodo de la red. 
      * @param req contiene información relevante sobre la conexión.
      * @param download_path ruta donde se guarda el archivo luego de descargado.
      * @param cs socket para establecer canal de comunicación con el servidor.
      * @return indica falla o éxito en la conexión.
      */
     public boolean requestSong(P2pRequest req, String download_path, Socket cs){
 	boolean result = true;
 
 	if(download_path == null){
 	    System.out.println("Path de descarga nulo");
 	    System.exit(1);
 	}
         try {
             // Construir salida hacia el servidor
             ObjectOutputStream os = new ObjectOutputStream
                     (cs.getOutputStream());
             // Mandar petición al servidor
             os.writeObject(req);
             // Ahora esperar respuesta con archivo
             ObjectInputStream is = new ObjectInputStream(cs.getInputStream());
             P2pRequest ans = (P2pRequest) is.readObject();
             // Extraer datos del archivo MP3
             FileOutputStream fos = new FileOutputStream
                     (download_path+"/"+new String(req.data)+".mp3");
             fos.write(ans.data);
             fos.close();
             os.close();
             is.close();
         }
         catch(ClassNotFoundException cnfe) {
             System.out.println("Class not found: "+cnfe);
 	    result = false;
         }
         catch(IOException e) {
             System.out.println("Error I/O: "+e);
 	    result = false;
         }
 
         return result;
     }
     
     /**
      * Envía una petición de consulta al nodo.
      * @param req contiene información relevante sobre la conexión.
      * @param cs socket para establecer canal de comunicación con el servidor.
      * @return Resultado de la consulta. Puede cantener las canciones de toda 
      * la red o las canciones que satisfacen el criterio de búsqueda 
      * especificado.
      */
     public String requestConsult(P2pRequest req, Socket cs) {
         String result = null;
         // Contruir salida hacia el servidor
         try {
             ObjectOutputStream os = new ObjectOutputStream(cs.getOutputStream());
             // Mandar petición al servidor
             os.writeObject(req);
             // Ahora esperar respuesta con string
             ObjectInputStream is = new ObjectInputStream(cs.getInputStream());
             P2pRequest ans = (P2pRequest) is.readObject();
             result = new String(ans.data);
         }
         catch(ClassNotFoundException cnfe) {
             System.out.println("Class not found: "+cnfe);
         }
         catch(IOException e) {
             System.out.println("Error I/O: "+e);
         }
         return result;
     }
     
     /**
      * Envía una petición de "Nodos alcanzables" a un nodo.
      * @param req contiene información relevante sobre la conexión.
      * @param cs socket para establecer canal de comunicación con el servidor.
      * @return Nodos alcanzables por el nodo al que se conecta este cliente.
      */
     public String requestReachable(P2pRequest req, Socket cs) {
         String result = null;
         // Construir salida hacia el servidor
         try {
             ObjectOutputStream os = new ObjectOutputStream
                     (cs.getOutputStream());
             // Mandar petición al servidor
             os.writeObject(req);
             // Ahora esperar respuesta con string
             ObjectInputStream is = new ObjectInputStream(cs.getInputStream());
             P2pRequest ans = (P2pRequest) is.readObject();
             result = new String(ans.data);
         }
         catch(ClassNotFoundException cnfe) {
             System.out.println("Class not found: "+cnfe);
         }
         catch(IOException e) {
             System.out.println("Error I/O: "+e);
         }
         return result;
     }
 }
