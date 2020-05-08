 import java.lang.Integer;
 import java.util.List;
 import java.util.ArrayList;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import javax.swing.JFrame;
 import javax.swing.JTable;
 import javax.swing.JOptionPane;
 import javax.swing.JTextField;
 import javax.swing.JLabel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import java.util.Iterator;
 import java.awt.Dimension;
 /**
  * Write a description of class Estante here.
  * 
  * @author (your name) 
  * @version (a version number or a date)
  */
 public class Estante implements ActionListener
 {
     // instance variables - replace the example below with your own
     private int id;
     private String nome;
     private JFrame window;
     private JTextField campoNome;
     private JComboBox campoEstante; 
     private String mode;   
     private static MySQL mysql = new MySQL();
     /**
      * Constructor for objects of class Estante
      */
     public Estante()
     {
         // initialise instance variables
         this.id = -1;
         this.nome = "";
         this.mode = null;
     }
     public Estante(int id,String nome){
         this.id = id;
         this.nome = nome;
         this.mode = null;
     }
     public Estante(String id,String nome){
         this.id = Integer.parseInt(id);
         this.nome = nome;
         this.mode = null;
     }
 
     public void setID(int id){
         if(this.id == -1){
             this.id = id;
         }
     }
     public void setID(String id){
         if(this.id == -1){
             this.id = Integer.parseInt(id);
         }
     }
     public int getID(){
         return this.id;
     }
     public void setNome(String nome){
         this.nome = nome;
     }
     public String getNome(){
         return this.nome;
     }
     public boolean insere(){
         boolean deuCerto = false;
         if(this.getID()==-1){
             String sql = "INSERT INTO estante(nome) VALUES(\""+this.getNome()+"\");";
             deuCerto = mysql.executaInsert(sql);
             if(deuCerto){
                 sql = "SELECT id FROM estante WHERE nome=\""+this.getNome()+"\" ORDER BY id DESC LIMIT 1";
                 ConjuntoResultados lista = mysql.executaSelect(sql);
                 lista.next();
                 this.setID(lista.getString("id"));
             }
         }
         return deuCerto;
     }
     public boolean atualiza(){
         boolean deuCerto = false;
         if(this.getID() != -1){
             String sql = "UPDATE estante SET nome=\""+this.getNome()+"\" WHERE id="+this.getID()+";";
             deuCerto = mysql.executaUpdate(sql);
         }
         return deuCerto;
     }
     public int apaga(){
         int deuCerto = 0;
         if(this.getID() != -1){
             List<Livro> livros = Livro.getLivrosByEstante(this);
             if(livros.size()>0){
                 if(JOptionPane.showConfirmDialog(null,"Deseja apagar os livros desta estante? (se escolher 'no' eles sero migrados para outra Estante)", "Deseja apagar os livros desta estante? (se escolher 'no' eles sero migrados para outra Estante)",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE)==JOptionPane.YES_OPTION){
                     for(int c=0;c<livros.size();c++){
                         livros.get(c).apaga();
                     }
                 }
                 else if( Estante.getEstantes().size()>1){
                     if(this.mode != null){
                         return 0;
                     }
                     this.mode = "apagar";
                     this.window = new JFrame("Migrar Livros");
                     this.window.setLayout(null);
                     this.window.setSize(400,200);
                     this.window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                     JLabel rotulo = new JLabel("Selecione a estante para o qual os "+livros.size()+" livros devero ser migrados:");
                     rotulo.setBounds(10,10,380,30);
                     this.campoEstante = new JComboBox();
                     Iterator<Estante> estantes = Estante.getEstantes().iterator();
  
                     while(estantes.hasNext()){
                         Estante estante = estantes.next();
                         if(!estante.toString().equals(this.toString())){
                             this.campoEstante.addItem(estante);
                         }
                         
                     }
                     this.campoEstante.setBounds(10,50,380,30);
                    
                     JButton botao = new JButton("Migrar");
                     botao.setBounds(10,90,380,30);
                     botao.addActionListener(this);
                     this.window.add(rotulo);
                     this.window.add(campoEstante);
                     this.window.add(botao);
                     this.window.setVisible(true);
                     return 2;
                 }
                 else{
                     JOptionPane.showMessageDialog(null,"A migrao no  possvel pois no h mais estantes cadastradas! Cadastre novas estantes ou tente novamente e selecione a opo 'Sim'");
                 }
             }
             String sql = "DELETE FROM estante WHERE id="+this.getID()+";";
             if(mysql.executaDelete(sql)==true){
                 deuCerto = 1;
                 this.id = -1;
                 this.nome = "";
             }
             else{
                 deuCerto = 0;
             }
         }
         return deuCerto;
     }
     public boolean salvar(){
         if(this.id == -1){
             return this.insere();
         }
         else{
             return this.atualiza();
         }
     }
     public void excluir(){
         if(JOptionPane.showConfirmDialog(null,"Deseja realmente apagar este estante?", "Deseja realmente apagar este estante?",JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE)==JOptionPane.YES_OPTION){
             int deuCerto = this.apaga();
             if(deuCerto==1){
                 JOptionPane.showMessageDialog(null,"Estante apagada com sucesso!");
             }
             else if(deuCerto == 0){
                 JOptionPane.showMessageDialog(null,"Houve um erro durante o apagamento da estante!");
             }
         }
     }
     public String toString(){
         return this.getID()+" - "+this.getNome();
     }
     public JFrame editar(){
         if(this.mode != null){
             return null;
         }
         this.mode = "editar";
         this.window = new JFrame("Editar Estante "+this.getNome());
         this.window.setLayout(null);
         this.window.setSize(400,300);
         this.window.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
         
         //Campo ID:
         JLabel rotuloID = new JLabel("ID da Estante:");
         rotuloID.setBounds(10,10, 100, 30);
         JTextField campoID = new JTextField(""+this.getID());
         campoID.setEditable(false);
         campoID.setBounds(120,10, 200, 30);
         
         JLabel rotuloNome = new JLabel("Titulo da Estante:");
        rotuloNome.setBounds(120, 50, 100, 30);
         this.campoNome = new JTextField(this.getNome());
         this.campoNome.setBounds(120,50, 200, 30);
         
         JButton botaoEditar = new JButton("Editar");
         botaoEditar.setBounds(10,90,100,30);
         botaoEditar.addActionListener(this);
         
         JButton botaoApagar = new JButton("Apagar");
         botaoApagar.setBounds(120,90,200,30);
         botaoApagar.addActionListener(this);
         
         this.window.add(rotuloID);
         this.window.add(campoID);
         this.window.add(rotuloNome);
         this.window.add(this.campoNome);
         this.window.add(botaoEditar);
         this.window.add(botaoApagar);
         this.window.setVisible(true);
         return this.window;
     }
         
     public JFrame cadastrar(){
         if(this.mode != null || this.id != -1){
             return null;
         }
         this.mode = "cadastrar";
         this.window = new JFrame("Cadastrar Estante");
         this.window.setLayout(null);
         this.window.setSize(400,300);
         this.window.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
         
         
         JLabel rotuloNome = new JLabel("Titulo da Estante:");
         rotuloNome.setBounds(10, 10, 100, 30);
         this.campoNome = new JTextField(this.getNome());
         this.campoNome.setBounds(120,10, 200, 30);
         
         JButton botaoCadastrar = new JButton("Cadastrar");
         botaoCadastrar.setBounds(10,50,100,30);
         botaoCadastrar.addActionListener(this);
         
         this.window.add(rotuloNome);
         this.window.add(this.campoNome);
         this.window.add(botaoCadastrar);
         this.window.setVisible(true);
         return this.window;
     }
     public void actionPerformed(ActionEvent e) {
         
         if(this.mode == "cadastrar"){
             this.mode = null;
             //Cadastra
             this.setNome(this.campoNome.getText());
             if(this.salvar()){
                 JOptionPane.showMessageDialog(null,"Estante cadastrada com Sucesso!");
             }
             else{
                 JOptionPane.showMessageDialog(null,"Houve um erro durante o cadastro da estante");
             }
         }
         else if(this.mode == "editar"){
             this.mode = null;
             JButton botao = (JButton)e.getSource();
             if(botao.getText() == "Editar"){
                 this.setNome(this.campoNome.getText());
                 if(this.salvar()){
                     JOptionPane.showMessageDialog(null,"Estante editada com Sucesso!");
                 }
                 else{
                     JOptionPane.showMessageDialog(null,"Houve um erro durante a Edio da estante");
                 }
             }
             else{
                 this.excluir();
             }
         }
         else if(this.mode == "apagar"){
             this.mode = null;
             List<Livro> livros = Livro.getLivrosByEstante(this);
             for(int c=0;c<livros.size();c++){
                 Livro livro = livros.get(c);
                 livro.setEstante((Estante)this.campoEstante.getSelectedItem());
                 if(!livro.salvar()){
                     JOptionPane.showMessageDialog(null,"Houve um erro durante a transferncia do Item");
                 }
             }
             int deuCerto = this.apaga();
             if(deuCerto==1){
                 JOptionPane.showMessageDialog(null,"Livros migrados e Estante apagada com sucesso!");
             }
             else if(deuCerto == 0){
                 JOptionPane.showMessageDialog(null,"Houve um erro durante o apagamento da estante!");
             }
         }
         this.mode = null;
         this.window.setVisible(false);
     }
     public static List<Estante> getEstantes()
     {
         // put your code here
         List<Estante> estantes = new ArrayList();
         String sql = "SELECT * FROM estante";
         ConjuntoResultados lista = mysql.executaSelect(sql);
         while(lista.next()){
             Estante estante = new Estante(lista.getString("id"),lista.getString("nome"));
             estantes.add(estante);
         }
         return estantes;
     }
     public static Estante getEstante(int id){
         String sql = "SELECT * FROM estante WHERE id="+id+" LIMIT 1";
         ConjuntoResultados lista = mysql.executaSelect(sql);
         if(lista.next()){
             Estante estante = new Estante(lista.getString("id"),lista.getString("nome"));
             return estante;
         }
         else{
             return null;
         }
     }
     public static Estante getEstante(String id){
         String sql = "SELECT * FROM estante WHERE id="+id+" LIMIT 1";
         ConjuntoResultados lista = mysql.executaSelect(sql);
         if(lista.next()){
             Estante estante = new Estante(lista.getString("id"),lista.getString("nome"));
             return estante;
         }
         else{
             return null;
         }
     }
     public static JTable lista(List<Estante> estantes){
         Object[][] lista = new Object[estantes.size()][6];
         String[] columns = {"ID","Nome"};
         for(int c=0;c<estantes.size(); c++){
             Estante estante = estantes.get(c);
             lista[c][0] = estante.getID();
             lista[c][1] = estante.getNome();
         }
         JTable table = new JTable(lista, columns){  
             public boolean isCellEditable(int row,int column){  
                 return false;  
             }  
         };
         table.setPreferredScrollableViewportSize(new Dimension(500, 70));
         table.setFillsViewportHeight(true);
         return table;
     }
     public static List<Estante> procurar(String id, String nome){
         List<Estante> estantes = new ArrayList();
         String sql = "SELECT * FROM estante WHERE id=\""+id+"\" AND nome=\""+nome+"\"";
         ConjuntoResultados lista = mysql.executaSelect(sql);
         while(lista.next()){
             Estante estante = new Estante(lista.getString("id"),lista.getString("titulo"));
 
             estantes.add(estante);
         }
         return estantes;
     }
     public static JTable lista(){
         List<Estante> estantes = Estante.getEstantes();
         Object[][] lista = new Object[estantes.size()][2];
         String[] columns = {"ID","Nome"};
         for(int c=0;c<estantes.size(); c++){
             Estante estante = estantes.get(c);
             lista[c][0] = estante.getID();
             lista[c][1] = estante.getNome();
         }
         JTable table = new JTable(lista, columns){  
             public boolean isCellEditable(int row,int column){  
                 return false;  
             }  
         };
         table.setPreferredScrollableViewportSize(new Dimension(400, 800));
         table.setFillsViewportHeight(true);
         return table;
     }   
 }
