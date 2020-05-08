 package cliente;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.JFrame;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import org.apache.xmlrpc.XmlRpcException;
 import org.apache.xmlrpc.client.XmlRpcClient;
 
 public class Listar extends JFrame {
 
     int livrosListados = 0;
     String livros[][];
     JTable tabela;
     String aux[] = new String[2];
     XmlRpcClient xmlrpc;
     Object[] parametros;
 
     public Listar(XmlRpcClient xmlrpc) {
         JScrollPane scroll = new JScrollPane();
         scroll.setViewportView(tabela);
         this.add(scroll);
 
         String resposta = "";
         this.xmlrpc = xmlrpc;
         parametros = new Object[]{};
 
         try {
             resposta = xmlrpc.execute("listar", parametros).toString();
         } catch (XmlRpcException ex) {
             Logger.getLogger(Listar.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         System.out.println("a resposta é: " + resposta);
 
         aux = resposta.split("\\|", 2);
         System.out.println("o aux 0 é: " + aux[0]);
         System.out.println("o aux 1 é: " + aux[1]);
         livrosListados = Integer.parseInt(aux[0]);
         resposta = aux[1];
         System.out.println("depois da resposta");
         String[][] linhas = new String[livrosListados][4];
         int debug = 0;
         for (int i = 0; i < livrosListados; i++) {
 
             for (int j = 0; j < 4; j++) {
                 aux = resposta.split("\\|", 2);
                 linhas[i][j] = aux[0];
                //if (i < (livrosListados - 1)) {
                     resposta = aux[1];
                //}
             }
         }
         
         String[] colunas = new String[]{"Titulo", "Autor", "Edicao", "Qtde"};
         tabela = new JTable(linhas, colunas);
         getContentPane().add(tabela);
 
         setSize(300, 300);
         setResizable(false);
         setTitle("Lista de Livros");
     }
 }
