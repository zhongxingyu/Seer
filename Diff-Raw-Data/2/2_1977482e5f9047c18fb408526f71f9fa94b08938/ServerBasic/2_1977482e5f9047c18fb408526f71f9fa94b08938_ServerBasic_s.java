 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package serverbasic;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import static java.lang.Thread.sleep;
 import java.net.Socket;
 import java.net.SocketException;
 import java.util.ArrayList;
 import java.util.NoSuchElementException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.imageio.ImageIO;
 
 /**
  *
  * @author Administrador
  */
 public class ServerBasic extends Thread{
     private static int puerto=5000;
 //    private static DataOutputStream dos_CR;
 //    private static DataInputStream dis_CR;
     private static ObjectInputStream entrada_imagen;
     private static ObjectOutputStream salida_imagen;
     private static LinkedListSocket linkedListSocket;
 //    private static ArrayList listaPeticioens;
 
     private static Socket sk_add;
     private static int idSesion=0;
 
 
     /**
      * @param args the command line arguments
      */
    public ServerBasic(Socket sk, int id,LinkedListSocket list, ArrayList listaPeti) throws IOException{
         super(""+id); // le damos un nombre al hilo
         System.out.println("Socket sk add: "+ sk_add);
         System.out.println("Nombre del hilo: "+getName ()+". Id del hilo: "+getId()); 
         this.sk_add = sk;  //Sino en cada hilo que genere me qdare con el ultimo socket
         this.idSesion = id;  // este es paso por valor no hay problema.
         this.linkedListSocket=list;
 //        this.listaPeticioens=listaPeti;
     }
 
     public synchronized void run() {
        try {
             Socket Sk_peticion = sk_add; // Guardamos el socket localmente para no sobre escribirlo mas adelante. :?
                          // canales de entrada y salida 
             DataInputStream dis_SC = new DataInputStream(Sk_peticion.getInputStream());    // Canal para recibir el tipo de cliente de peticion o de resolucion
             DataOutputStream dos_SC = new DataOutputStream(Sk_peticion.getOutputStream());  // Canal para responder a la peticion de Captcha
             String accion=dis_SC.readUTF();
             if (accion.equals("resolucion")){
                 //guardamos la conexion con dicho cliente en la lista enlazada.
                 linkedListSocket.addSocket(Sk_peticion);     
                 System.out.println("Socket guardado....");
             }else{
                 if(accion.equals("peticion")){     
 //                    listaPeticioens.add(idSesion, sk_add); // Guardamos la peticion en el id de sesion
                     System.out.println("peticion id: "+idSesion);
                     // aqui debemos asegurarnos de coger el socke correcto previamente guardado en el constructor.                      [Probando]
                     dos_SC.writeUTF("vamos mandame la imagen");// mandamos el foco al clientePetecion para que nos mande la imagen
                     entrada_imagen = new ObjectInputStream( Sk_peticion.getInputStream() );  // Para obtener la imagen"Cliente peticion".
                     leerImagenCliente();
                     //                            linkedListSocket.deleteNULL(); // borrarmos los socket null [ PROBANDO ] ... [KO]
                     boolean fallo;
                     do{
                         fallo=false;
                         try{
                             Socket sk_cr = linkedListSocket.getSocketFirst(); // obtenemos el cliente resolutor
                             System.out.println("Socket obgenido: "+ sk_cr);
                             DataOutputStream dos_CR = new DataOutputStream(sk_cr.getOutputStream());  // Canal para responder a la peticion de Captcha
                             DataInputStream dis_CR = new DataInputStream(sk_cr.getInputStream());
                             dos_CR.writeUTF("Captcha..."); // mandamos el foco al cliente resolutor y le mandamos la imagen
                             enviarImagen(sk_cr); // como el cliente resolutor ya tiene el foco le mandamos la imagen
                             String respuesta = dis_CR.readUTF(); // recibimos la respuesta del cliente resolutor
                             // ahora tenemos que recuperar la conexion con el cliente peticion.
                             System.out.println("socket Peticion : "+Sk_peticion);
                             dos_SC.writeUTF(respuesta); // aqui SC es el cliente peticion porque a sido la ultima conexion entrante con el sever SC
                         }catch(SocketException ex){
                             System.out.println(" << Fallo obteniendo Socket >> ...\t [KO]");
                             fallo=true;
                         }
                         catch(NoSuchElementException ex){
                             System.out.println(" << No hay conexiones >> ...\t [KO]");
                             fallo=true;
                             try {
                                 sleep(5000);
                             } catch (InterruptedException ex1) {
                                 Logger.getLogger(ServerBasic.class.getName()).log(Level.SEVERE, null, ex1);
                             }
                         }
                     } while(fallo);
                 }
             }
             System.out.println("Fin peticion id: "+idSesion);
             linkedListSocket.listSocket(); // test
         } catch (IOException ex) {
             Logger.getLogger(ServerBasic.class.getName()).log(Level.SEVERE, null, ex);
             System.out.println("excepcion... run server");
         }
     }
     public static void leerImagenCliente() throws IOException{
               try {
                 byte[] bytesImagen = (byte[]) entrada_imagen.readObject();
                 ByteArrayInputStream entradaImagen = new ByteArrayInputStream(bytesImagen);
                 BufferedImage bufferedImage = ImageIO.read(entradaImagen);
  
                 String nombreFichero="C:\\Documents and Settings\\Administrador\\Escritorio\\"+idSesion+".jpg";
                 System.out.println("Generando el fichero: "+nombreFichero );
                 FileOutputStream out = new FileOutputStream(nombreFichero);
                 // esbribe la imagen a fichero
                 ImageIO.write(bufferedImage, "jpg", out);
             }
  
             // atrapar problemas que pueden ocurrir al tratar de leer del cliente
             catch ( ClassNotFoundException excepcionClaseNoEncontrada ) {
                 System.out.println( "\nSe recibió un tipo de objeto desconocido" );
             }      
         System.out.println("Imagen gardada de cliente");
     }
     public static void enviarImagen(Socket sk_cr) throws IOException{
 
         try{
             salida_imagen = new ObjectOutputStream( sk_cr.getOutputStream() );
             BufferedImage bufferedImage = ImageIO.read(new File("C:\\Documents and Settings\\Administrador\\Escritorio\\"+idSesion+".jpg"));
             ByteArrayOutputStream salidaImagen = new ByteArrayOutputStream();
             ImageIO.write(bufferedImage, "jpg", salidaImagen);
             byte[] bytesImagen = salidaImagen.toByteArray();
             salida_imagen.writeObject( bytesImagen );
             salida_imagen.flush();
             System.out.println( "Se ha enviado la imagen desde el server" );
         // procesar los problemas que pueden ocurrir al enviar el objeto
         }catch ( IOException excepcionES ) {
             System.out.println( "\nError al escribir el objeto" );
         }    catch(Exception e){
             System.out.println("Mensage de error.. "+e.getMessage());
         }   
     }
     
 
 
 }
