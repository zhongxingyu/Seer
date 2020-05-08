 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.UnsupportedEncodingException;
 import java.net.Socket;
 import java.net.SocketTimeoutException;
 import java.net.UnknownHostException;
 import java.nio.ByteBuffer;
 import java.security.InvalidKeyException;
 import java.security.KeyFactory;
 import java.security.KeyPair;
 import java.security.KeyPairGenerator;
 import java.security.NoSuchAlgorithmException;
 import java.security.PrivateKey;
 import java.security.PublicKey;
 import java.security.spec.InvalidKeySpecException;
 import java.security.spec.X509EncodedKeySpec;
 import javax.crypto.BadPaddingException;
 import javax.crypto.Cipher;
 import javax.crypto.CipherOutputStream;
 import javax.crypto.IllegalBlockSizeException;
 import javax.crypto.NoSuchPaddingException;
 import javax.xml.bind.DatatypeConverter;
 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /**
  *
  * @author rft
  */
 public class SocketCliente implements Runnable{
     
     private MenuActionListener actionListener = new MenuActionListener();       // Variavel que vai conter a referência ao MenuActionListener
     private String endereco = new String();                                     // variavel que vai guardar o endereco do servidor
     private int porto;                                                          // variavel que vai guardar o porto do servidor
     private volatile String mensagem;                                           // variavel que vai receber a mensagem escrita pelo cliente
     private boolean stop = false;                                               // variavel booleana que define se o ciclo de escuta escrita se mantem em execução
     private String recebido;                                                    // variavel que armazena o conteudo do inputstream
     private PrivateKey chave_privada_c;                                         // variavel que vai guardar a chave privada da instancia desta classe
     private Cipher cipher_s;                                                    // cipher para desencriptar no servidor
     private Cipher cipher_c;                                                    // cipher para ecriptar para o cliente
     private PublicKey chave_publica_c;                                          // variavel que vai guardar a chave publica da instância desta classe
     private PublicKey chave_publica_s;                                          // variavel que vai guardar a chave publica enviada pelo servidor
     private byte[] encrypted = null;                                            // array de bytes que vai guardar os dados encriptados
     private byte[] decrypted = null;                                            // array de bytes que vai guardar os dados desencriptados
     private boolean first_con_envio = true;                                     // variavel que se for true envia a public key ao servidor como primeira mensagem
     private boolean first_con_recebido = true;                                  // variavel que se for true indica o recebimento da chave publica do servidor
     
     ////////////////////////////////////////////////////////////////////////////
     /////////////////////////Gets e Sets////////////////////////////////////////
     ////////////////////////////////////////////////////////////////////////////
     
     
     public synchronized void setMensagemCliente(String mensagem){               // metodo que recebe a mensagem a enviar pelo cliente
         this.mensagem = mensagem;
     }
     
     public synchronized String getMensagemCliente(){                            // metodo que retorna a mensagem armazenada para enviar
         return mensagem;
     }
     
     ////////////////////////////////////////////////////////////////////////////
     //////////////////Gerar as chaves publicas e privadas///////////////////////
     ////////////////////////////////////////////////////////////////////////////
     private void geraKeyPair(){
       try {
           KeyPairGenerator keyPg = KeyPairGenerator.getInstance("RSA");         // Gerador de chaves pubicas e privadas baseadas no algoritmo RSA
           keyPg.initialize(1024);                                               // Inicializa o KeyPair especificando o tamanho da chave
           KeyPair pair = keyPg.generateKeyPair();                               // Gera a chaves
           chave_privada_c = pair.getPrivate();                                  // cria uma instancia de PrivateKey através da chave privada gerada
           chave_publica_c = pair.getPublic();                                   // cria uma instância de PublicKey através da chave pública gerada  
       }catch(NoSuchAlgorithmException e){   
           System.out.println(e);
       }
     }
     
     ////////////////////////////////////////////////////////////////////////////
     //////////////////////////Encriptar dados///////////////////////////////////
     ////////////////////////////////////////////////////////////////////////////
     private byte[] encrypt(String mensagem){ 
         try {  
             byte[] descodificada = decodeUTF8(mensagem);  
             cipher_s = Cipher.getInstance("RSA/ECB/PKCS1Padding");                // Cria um cipher baseado no algoritmo "RSA"
             cipher_s.init(Cipher.ENCRYPT_MODE, chave_publica_s);                  // Inicializa o cipher com a chave publica do servidor     
             //System.out.println("nProvider is: " + cipher_c.getProvider().getInfo());// debug
         
             encrypted = cipher_s.doFinal(descodificada);                          // encripta a mensagem a enviar       
         }catch(NoSuchAlgorithmException e){
             System.out.println(e);
         }catch(NoSuchPaddingException e){
             System.out.println(e);
         }catch(InvalidKeyException e){
             System.out.println(e);
         }catch(IllegalBlockSizeException e){
             System.out.println(e);
         }catch(BadPaddingException e){
             System.out.println(e);
         }catch(IOException e){
             System.out.println(e); 
         }    
         if (chave_publica_s == null)
             System.out.println("A chave de encriptação do servidor ainda nao esta disponivel");
         System.out.println("C Mensagem encriptada: "+encrypted);                  // debug
         return encrypted;                                                         // retorna a mensagem encriptada
       
     }
     
     ////////////////////////////////////////////////////////////////////////////
     ////////////////////////Desencriptar dados//////////////////////////////////
     ////////////////////////////////////////////////////////////////////////////
     private String decrypt(byte[] mensagem){
         String output ="";
         try{
             cipher_c = Cipher.getInstance("RSA/ECB/PKCS1Padding");
             cipher_c.init(Cipher.DECRYPT_MODE, chave_privada_c);
             decrypted = cipher_c.doFinal(mensagem);
             output = encodeUTF8(decrypted);
         }catch(NoSuchAlgorithmException e){
             System.out.println(e);
         }catch(NoSuchPaddingException e){
             System.out.println(e);
         }catch(InvalidKeyException e){
             System.out.println(e);
         }catch(IllegalBlockSizeException e){
             System.out.println(e);
         }catch(BadPaddingException e){
             System.out.println(e);
            stop = true;                                                        // Para o ciclo de escuta
         }    
       
         return output;
    }
     
     ////////////////////////////////////////////////////////////////////////////
     ///////////////////Encode / Decoder de strings para Bytes UTF-8/////////////
     ////////////////////////////////////////////////////////////////////////////
     private static String encodeUTF8(byte[] bytes){
         String temp = null;
         try{
             temp = new String(bytes, "UTF-8");
         }catch(UnsupportedEncodingException e){
             System.out.println(e);
         }
         return temp;
     }
 
     private static byte[] decodeUTF8(String text) throws IOException{           // Método que recebe uma String e converte para bytes UTF8
        
         return text.getBytes("UTF-8");
     }
 
 
     ////////////////////////////////////////////////////////////////////////////
     //////////////////////////CONSTRUTORES//////////////////////////////////////
     ////////////////////////////////////////////////////////////////////////////
     
     public SocketCliente(MenuActionListener actionListener, String endereco, int porto, String mensagem){   // Construtor com parametros para receber a referencia do objeto MenuActionListener
         this.actionListener = actionListener;                                                               // o endereço do servidor, o porto e a mensagem a enviar
         this.endereco = endereco;
         this.porto = porto;
         this.mensagem = mensagem;
     }
     
     
     //*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*//
     ///////////////////////Programa começa a executar//////////////////////////
     //*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*/*//
     @Override
     public void run(){                                                          // Sobreposição do método run da interface Runnable
         geraKeyPair();                                                          // Invoca o método generateKeyPair
         
         //Conectar ao servidor.
         try{
             Socket socket_client = new Socket(endereco, porto);                 // Criar um novo Socket 
             socket_client.setSoTimeout(1000);                                   // Especifica um timeout para o inputstream, para nao ficar bloquado a espera de dados
             actionListener.setSocketClienteError("Conectado");                  // Envia um texto qualquer para a variavel socket_c_erro do MenuActionListener para este não ficar em null
             
             InputStream inputStream = socket_client.getInputStream();           // cria um canal de recebimento de dados
             OutputStream outputStream = socket_client.getOutputStream();        // cria um canal de envio de dados
      
             while(!stop){         
                 try{                                                            // uma vez que especificamos um timeout para o inputstream necessitamos do try/catch
                     byte[] tmp = new byte[128];
                     inputStream.read(tmp);
                     System.out.println("S mensagem do cliente encriptada"+ new String(tmp));
                     recebido = decrypt(tmp);
                     System.out.println("S mensagem do cliente desencriptada "+ recebido);
                 }catch(SocketTimeoutException sto){
                     System.out.println("C Timeout do inputstream");                 
                 }
                 System.out.println("C Socket cliente: "+ getMensagemCliente() );// debug
                     
                 if (recebido != null){                                          // Se recebeu informação no imputstream
                     System.out.println("C Mensagem que chegou ao cliente:"+ recebido); //debug
                     actionListener.appendInfo("Servidor: " + recebido);         // envia a informação para a JTextAreaInfo
                     recebido = null;                                            // Coloca a mensagem a null para nao voltar a repetir
                 }
   
                 if (first_con_envio){                                           // caso seja o primeiro contacto com o servidor
                     try{  
                         //////PARA ENVIAR E RECECER CHAVES PUBLICAS/////////////
                         System.out.println("C chave_publica: "+DatatypeConverter.printHexBinary(chave_publica_c.getEncoded())); //debug
                         ByteBuffer bb = ByteBuffer.allocate(4);                 // aloca um ByteBuffer de tamanho 4 á variavel bb          
                         bb.putInt(chave_publica_c.getEncoded().length);         // coloca no buffer o tamanho da chave_publica_c
                         socket_client.getOutputStream().write(bb.array());      // envia o byte array bb
                         socket_client.getOutputStream().write(chave_publica_c.getEncoded());  // envia a chave publica codificada
                         socket_client.getOutputStream().flush();                // força o envio dos dados no outputstream
                         
                         byte[] temp_chave_s = new byte[4];                      // cria um novo array de bytes com tamanho 4
                         socket_client.getInputStream().read(temp_chave_s,0,4);  // lê do input stream para o byte temp_chave_se 
                         ByteBuffer bb_s = ByteBuffer.wrap(temp_chave_s);        // cria um buffer de bytes com tamanho baseado no byte temp_chave_s
                         int tamanho = bb_s.getInt();                            // guarda o tamanho do bytebuffer
                         System.out.println(tamanho);                            // debug
                         byte[] temp_chave_s_bytes = new byte[tamanho];          // cria um novo array de bytes com o tamanho do bytebuffer 
                         socket_client.getInputStream().read(temp_chave_s_bytes);// recebe no inputStream para o array de bytes temp_cave_s_bytes
                         System.out.println(DatatypeConverter.printHexBinary(temp_chave_s_bytes));   // debug
                         X509EncodedKeySpec keySpec = new X509EncodedKeySpec(temp_chave_s_bytes);    // codifica a chave recebida
                         KeyFactory keyFactory = KeyFactory.getInstance("RSA");  // cria um novo keyFactory baseado no algoritmo RSA
                         chave_publica_s = keyFactory.generatePublic(keySpec);   // gera a chave publica através da chave codificada keySpec que foi recebida pelo servidor
                         System.out.println(DatatypeConverter.printHexBinary(chave_publica_s.getEncoded())); //debug
                         first_con_envio = false;                                // coloca a variavel first_con_envio em false
                     
                     }catch(NoSuchAlgorithmException e){
                         System.out.println(e);
                     }catch(InvalidKeySpecException e){
                         System.out.println(e);    
                     }     
                 }
                 else{
                     if (getMensagemCliente() != null && getMensagemCliente().length() > 0){ // caso haja uma mensagem nova a enviar
                             System.out.println("C Mensagem a enviar nao encriptada "+ getMensagemCliente()); // debug
                             byte[] mensagem = encrypt(getMensagemCliente());    // encripta a mensagem a enviar ao servidor
                             outputStream.write(mensagem, 0, mensagem.length );  // Envia os bytes UTF8 da mensagem.
                             outputStream.flush();                               // Força o envio dos bytes em buffer
                             setMensagemCliente(null);                           // coloca a string a null para nao repetir envio
                     }
                 }
                 System.out.println("C Fim de ciclo");                           //debug
             }                                                                   // Termina o ciclo While   
             inputStream.close();                                                // Fecha o canal de entrada de dados
             outputStream.close();                                               // Fecha o canal de saída de dados 
             socket_client.close();                                              // Fecha o socket.
 
         }catch(UnknownHostException e){
             actionListener.appendInfo("Host Desconhecido");                     // caso haja exceção coloca informação na janela info
             actionListener.setSocketClienteError("Host Desconhecido");          // invoca o método setSocketClienteError do menuActionLister
         }catch(IOException e){
             actionListener.appendInfo("Host selecionado não está disponível");  // caso haja exceção coloca informação na janela info
             actionListener.setSocketClienteError("Host não disponivel");        // invoca o método setSocketClienteError do menuActionLister
         }catch(IllegalArgumentException Ia){
             actionListener.appendInfo("Porto mal inserido");                    // caso haja exceção coloca informação na janela info
         }
      
     }
     
 }
