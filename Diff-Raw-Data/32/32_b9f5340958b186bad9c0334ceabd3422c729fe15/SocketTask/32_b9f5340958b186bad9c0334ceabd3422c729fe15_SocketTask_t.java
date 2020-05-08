 package egl.positioningsystem;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.InetSocketAddress;
 import java.net.Socket;
 import java.net.SocketAddress;
 
 import android.os.AsyncTask;
 import android.util.Log;
 
 /**
  * Classe para envio de dados via socket
  *
  */
 
 public abstract class SocketTask extends AsyncTask<String, String, Boolean> {
  
     private Socket socket;
     private InputStream is;
     private OutputStream os;
     private String host;
     private int port;
     private int timeout;
  
     /**
      * Construtor com host, porta e timeout
      *
      * @param host
      *            host para conexão
      * @param port
      *            porta para conexão
      * @param timeout
      *            timeout da conexão
      */
     public SocketTask(String host, int port, int timeout) {
         super();
         this.host = host;
         this.port = port;
         this.timeout = timeout;
     }
  
     /**
      * Envia dados adicionais se estiver conectado
      *
      * @param data
      *            dados addicionais
      * @throws IOException
      */
     public void sendData(String data) throws IOException {
         if (socket != null && socket.isConnected()) {
             os.write(data.getBytes());
         }
     }
  
     @Override
     protected Boolean doInBackground(String... params) {
         boolean result = false;
         try {
             SocketAddress sockaddr = new InetSocketAddress(host, port);
             socket = new Socket();
             socket.connect(sockaddr, timeout); // milisegundos
             if (socket.isConnected()) {
                 publishProgress("CONNECTED");
                 is = socket.getInputStream();
                 os = socket.getOutputStream();
                 for (String p : params) {
                     os.write(p.getBytes());
                 }
                 byte[] buff = new byte[2048];
                 int buffData = is.read(buff, 0, 2048);
                 while (buffData != -1) {
                     String response = new String(buff);
                     // Envia os dados
                     publishProgress(response);
                     buffData = is.read(buff, 0, 2048);
                 }
             } else {
                 publishProgress("CONNECT ERROR");
             }
         } catch (IOException e) {
             publishProgress("ERRO - DADOS NÃO ENVIADOS");
             Log.e("SocketAndroid", "Erro de entrada e saida", e);
             result = true;
         } catch (Exception e) {
            publishProgress("ERROR !");
             Log.e("SocketAndroid", "Erro generico", e);
             result = true;
         } finally {
             try {
                 if (is != null) {
                     is.close();
                 }
                 if (os != null) {
                     os.close();
                 }
                 if (socket != null) {
                     socket.close();
                 }
             } catch (Exception e) {
                 Log.e("SocketAndroid", "Erro ao fechar conexao", e);
             }
         }
         return result;
     }
 }
