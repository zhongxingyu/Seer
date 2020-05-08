 
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import javax.swing.JFrame;
 
 public class Menu extends JFrame{
   
   
   // Declaração de  variaveis
   private MenuActionListener actionListener;                                    // reserva de memória para um objeto da classe MainActionListener
   private MenuBar menuBar;                                                      // reserva de memória para um objeto da classe menuBar
   private Thread trd_s;                                                         // reserva de memória para a Thread que vai ser associada ao socket server
   private SocketServidor servidor;                                              // reserva de memória para o Socket
   
   ////////////////////////////////////////////////////////////////////////////// //recebe duas informações, porto novo, caso se especifique um novo porto
   ///////////////////Método para criar o socket Servidor//////////////////////// // e porto antigo que foi o porto usado anteriormente de modo a poder fechar o socket
   //////////Socket servidor é criado quando o programa arranca//////////////////
   public void criarSocketServidor(int portoNovo, int portoAntigo){  
     actionListener.setPortoLabel("A receber no porto: " + portoNovo);           // envia para o MenuActionListener a informação sobre o porto de escuta
    
     try{
     if (trd_s.isAlive()){                                                       
        servidor.stopThread();                                                   // caso já exista uma thread a correr, destroi para criar uma nova  
        criarSocketCliente("127.0.0.1", portoAntigo , "");                       // envia uma mensagem em branco para desblockear o "socket_servidor.accept();" e continuar com o fecho da thread
        actionListener.appendInfo("A parar threads activas...");
                          }
     }catch(NullPointerException Npe){
         actionListener.appendInfo("Não existem threads activas...");
     }
     
     // Inicialização da Thread para o Socket servidor
     servidor = new SocketServidor(actionListener, portoNovo);                   // Cria um novo SocketServidor e envia a referência do MenuActionListener, o novo porto
     trd_s = new Thread(servidor);
     trd_s.start();
     actionListener.setSocketServidor(servidor);                                 // Envia para o actionListener a referencia do socketServidor
                                             }
   
   
   //////////////////////////////////////////////////////////////////////////////
   ///////////////////Método para criar o socket cliente/////////////////////////
   //////////Socket cliente é criado quando se carrega no botão enviar///////////
   public SocketCliente criarSocketCliente(String endereco, int porto, String mensagem){
                                                                                           
     // Inicialização da Thread para o Socket do cliente
     Runnable cliente = new SocketCliente(actionListener, endereco, porto, mensagem);
     Thread trd_c = new Thread(cliente);
     trd_c.start(); 
     return (SocketCliente)cliente;
                                                                              }
   
   
   
 
   
   Menu() {
     addWindowListener(new MyFinishWindow());                                    // Habilita o fecho da janela principal
     initComponents();
    
     //configuração do JFrame Menu
     setLayout(null);
     setTitle("Sockets SID");                                                    // especifica o titulo do JFrame
     setSize(600, 520);                                                          // especifica o tamanho da JFrame
     setResizable(false);                                                        // impede que se possa fazer resize ao JFrame
     setLocation(100,100);                                                       // especifica a ocalização de inicialização do JFrame
     setVisible(true);                                                           // activa a visibilidade do JFrame
       
     
    criarSocketServidor(4558, 4444);                                            // invoca o método que cria o Socket Server com a porta 4444 por defeito
    /*
     // Inicialização da Thread para o Socket do servidor
     Runnable servidor = new SocketServidor(actionListener);
     Thread trd_s = new Thread(servidor);
     trd_s.start();
     */
     
   }
 
   private void initComponents() {
    actionListener = new MenuActionListener(this);                               // Instancia um objeto da classe MenuActionListener envia a referencia deste JFrame
    menuBar = new MenuBar(actionListener);                                       // instancia um objeto da classe MenuBar e envia a referencia de memória da actionListener
    setJMenuBar(menuBar);
   }
   
 
 
   public static void main(String[] argv) {
 
     new Menu();                                                                 // cria uma nova instancia self
     
   }
 }
