 import java.io.BufferedOutputStream;
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.rmi.*;
 import java.util.*;
 public class Cliente
 {
     public static ArrayList<String> listaServidores = new ArrayList<String>(10);//Array con la lista de servidores
     
     /**
      * Descarga la lista de servidores disponible.
      * Guardado en un fichero llamado 'servidores'. Cada línea del archivo contiene
      * una dirección ya formateada.
      * @throws Exception 
      */
     public static void servidores() throws Exception{
         java.io.BufferedInputStream in = new java.io.BufferedInputStream(new
         java.net.URL("https://dl.dropbox.com/s/u53c2jtce261g7r/servidores.txt?token_hash=AAEU65Li2X8QVSCHKIYZRl8a5nZdzhyICNgn6YrcxE-cuw&dl=1").openStream());
         java.io.FileOutputStream fos = new java.io.FileOutputStream("servidores.txt");
         java.io.BufferedOutputStream bout = new BufferedOutputStream(fos,1024);
         byte data[] = new byte[1024];
         while(in.read(data,0,1024)>=0){
             bout.write(data);
         }
         bout.close();
         in.close();
     }
     /**
      * Lee el archivo descargado de 'servidores' guardando en un ArrayList todos
      * las direcciones que haya. Decir que en ese archivo debe estar únicamente la dirección
      * IP. 
      * @throws Exception 
      */
     public static void leer() throws Exception{
         try{
             // Abre el archivo.
             FileInputStream fstream = new FileInputStream("servidores.txt");
             //Cogemos el objeto y lo abrimos.
             DataInputStream in = new DataInputStream(fstream);
             BufferedReader br = new BufferedReader(new InputStreamReader(in));
             String strLine;
             //Lee el archivo línea a línea.
             while ((strLine = br.readLine()) != null){
                 listaServidores.add(strLine);
             }
             //Eliminamos el último elemento porque en las pruebas realizadas es basura.
             //Es un problema al bajarse o algo parecido.
             listaServidores.remove(listaServidores.size()-1);
             System.out.println(listaServidores.size());
             System.out.println(listaServidores.toString());
             //Cerramos
             in.close();
         }catch (Exception e){//Catch exception if any
             System.err.println("Error: " + e.getMessage());
         }
     }
     /**
      * Muestra los servidores existentes en el archivo descargado
      * y deja que el cliente elija el correcto.
      * @return int Entero que representa el servidor elegido.
      */
     public static int elegirServidor(){
     	System.out.println("Elige el nuevo servidor: ");
     	for(int i = 0; i < listaServidores.size(); i++){
     		System.out.println((i+1)+": "+listaServidores.get(i));
     	}
     	Scanner T = new Scanner(System.in);
    	int t = T.nextInt()-1;
    	return t;
     }
     
     public static void main(String[] args){
         try{
             servidores();
             leer();
             int servidor = elegirServidor();
             InterfazServidor L = (InterfazServidor)Naming.lookup("rmi://"+listaServidores.get(servidor)+":2001/callback");
             InterfazCliente ObjetodeCallBack = new
             ImpInterfazCliente();
             System.out.println("objeto callback cliente creado...");
             L.setNumPlayers(2);//MODIFICAR
             //registro de callback
             L.RegistroCallBack(ObjetodeCallBack);
             System.out.println("CallBack registrado...");
             try{
                 Thread.sleep(1000);
             }catch (InterruptedException e){}
             //L.QuitarCallBack(ObjetodeCallBack);
             //System.out.println("CallBack eliminado de registro...");
         }catch (Exception e) {System.out.println("problema en cliente...");}
             System.out.println("Cliente finalizando...");
     }//main
 }
