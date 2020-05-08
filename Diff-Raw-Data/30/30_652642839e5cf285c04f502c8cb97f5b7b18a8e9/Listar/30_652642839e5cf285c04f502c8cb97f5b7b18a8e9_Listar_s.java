 package cliente;
 
 import java.awt.ScrollPane;
 import javax.swing.JFrame;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableModel;
 
 /**
  *
  * @author Fabiano
  */
 public class Listar extends JFrame{
     
     Conexao conexao;
     int livrosListados=0;
     String livros[][];
     JTable tabela;
     public Listar(Conexao c){
         
         //String[] colunas = new String[]{"Titulo","Autor","Edicao","Qtde"};
         //String[][] linhas = new String[][]{{"Titulo","Autor","Edicao","Qtde"},
         //        {"Titulo","Autor","Edicao","Qtde"}};
         
        // JTable teste = new JTable(linhas,colunas);
         
         JScrollPane scroll = new JScrollPane();
         scroll.setViewportView(tabela);
         this.add(scroll);
         conexao=c;
         //getContentPane().setLayout(null); 
         //teste.setModel(new TableModel(new String[]{"Titulo","Autor","Edicao","Qtde"},5,4));
         //DefaultTableModel modelo = new DefaultTableModel(linhas,colunas);
         //teste.setModel(modelo);
 
         
         
         String resposta;
         String requisicao = "listar";
         System.out.println(requisicao);
         resposta = conexao.requisitar(requisicao);
         System.out.println("a resposta é: " + resposta);
        String aux[] = new String[2];
        aux = resposta.split("|",2);
         System.out.println("o aux 0 é: "+aux[0]);
        //livrosListados=Integer.parseInt(aux[0]);
         resposta = aux[1];
    
         String[][] linhas = new String[livrosListados][4];
         for(int i=0;i<livrosListados;i++){
             
             for(int j =0;j<4;j++){
                aux = resposta.split("|", 2);
                 linhas[i][j] = aux[0];
                resposta = aux[1];
                
             }
         }
         String[] colunas = new String[]{"Titulo","Autor","Edicao","Qtde"};
         tabela = new JTable(linhas,colunas);
         getContentPane().add(tabela);
         
         
         
         
        
        
        
        
         setSize(300, 300);
         setResizable(false);
         setTitle("Lista de Livros");
         //this.lista();
         
     }
     
     public void lista(){
         
         /*String resposta;
         String requisicao = "listar";
         System.out.println(requisicao);
         resposta = conexao.requisitar(requisicao);
         
         String[] aux = new String[2];
         aux = resposta.split("|", 2);
         System.out.println(aux[0]);
         //livrosListados=Integer.parseInt(aux[0]);
         resposta = aux[1];
     
         String[][] linhas = new String[livrosListados][4];
         for(int i=0;i<livrosListados;i++){
             
             for(int j =0;j<4;j++){
                 aux = resposta.split("|", 2);
                 linhas[i][j] = aux[0];
                 resposta = aux[1];
                 
             }
         }
         String[] colunas = new String[]{"Titulo","Autor","Edicao","Qtde"};
         tabela = new JTable(linhas,colunas);
         getContentPane().add(tabela);*/
     }
     
 }
