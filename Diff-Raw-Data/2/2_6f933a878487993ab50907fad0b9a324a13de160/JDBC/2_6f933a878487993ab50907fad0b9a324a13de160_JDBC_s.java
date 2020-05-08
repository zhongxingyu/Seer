 package banco;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import model.Campus;
 import model.Edital;
 import model.Eixo;
 import model.Projeto;
 
 /**
  * <h1>Classe de consultas no Banco de Dados</h1>
  *
  * <p>contem a conexão em modo privado para que possa ser feita a conexão
  * automatica na chamada de cada metodo de consulta</p>
  *
  * @author Caio Alexandre
  */
 public class JDBC {
 
     public String buscarEmailProfessor(int idProjeto) {
         String email = "";
         try {
             connect();
             ResultSet rs = con.createStatement().executeQuery("SELECT email FROM projeto,professor WHERE idprojeto = " + idProjeto + " and projeto.professor_idprofessor=professor.idprofessor;");
 
             if (rs.next()) {
                 email = rs.getString("email");
             }
             disconnect();
         } catch (ClassNotFoundException | SQLException ex) {
         }
         return email;
     }
 
     public void homologar(String sql) {
         try {
             connect();
             Statement com = con.createStatement();
             int rs = com.executeUpdate(sql);
             disconnect();
         } catch (ClassNotFoundException | SQLException ex) {
         }
     }
     //CONNECTION - conexão SQL usada para criar o Statement para o comando SQL
     protected Connection con = null;
     private ResultSet resultset = null;
     private Statement statement = null;
 
     /**
      * Conecta-se com o banco de dados, deve ser chamado no inicio de seu metodo
      * de consulta.
      *
      * @throws ClassNotFoundException - Deve ser tratado erro com Driver MySQL
      * @throws SQLException - Deve ser tratado erro de conexão com o banco
      */
     protected void connect() throws ClassNotFoundException, SQLException {
         Class.forName("org.gjt.mm.mysql.Driver");
         //(Augusto)-Alterei o banco de test para rpv e o final do ip de 3306 para 3307
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/rpv", "root", "");
     }
 
     /**
      * Desconecta-se do banco de dados
      */
     protected void disconnect() {
         if (con != null) {
             try {
                 con.close();
             } catch (Exception e) {
             }
             con = null;
         }
     }
 
     public ArrayList<String> listaDeCampus() {
         ArrayList<String> lista = new ArrayList<>();
 
         try {
             connect();
 
             ResultSet rs = con.createStatement().executeQuery("SELECT * FROM rpv.campus");
 
             while (rs.next()) {
                 lista.add(rs.getString("nome"));
             }
 
         } catch (Exception ex) {
             ex.printStackTrace();
         } finally {
             disconnect();
         }
 
         return lista;
     }
 
     public ArrayList<String> listaDeEixos() {
         ArrayList<String> lista = new ArrayList<>();
 
         try {
             connect();
 
             ResultSet rs = con.createStatement().executeQuery("SELECT * FROM rpv.eixo");
 
             while (rs.next()) {
                 lista.add(rs.getString("tipo"));
             }
 
         } catch (Exception ex) {
             ex.printStackTrace();
         } finally {
             disconnect();
         }
 
         return lista;
     }
 
     public ArrayList<String> listaDeMembros() {
         ArrayList<String> lista = new ArrayList<>();
 
         try {
             connect();
 
             ResultSet rs = con.createStatement().executeQuery("SELECT * FROM rpv.professor");
 
             while (rs.next()) {
                 lista.add(rs.getString("nome"));
             }
 
         } catch (Exception ex) {
             ex.printStackTrace();
         } finally {
             disconnect();
         }
 
         return lista;
     }
 
     /**
      * Testa se existe conexão com o Banco
      *
      * @return retorna <b>true</b> caso houver conexão e houver possibilidade de
      * executar comandos
      * @throws ClassNotFoundException - Caso o driver de conexão do MySQL com
      * java não esta presente
      * @throws SQLException - Caso haja algum erro SQL, tanto com conexão quanto
      * comandos
      */
     public boolean testarConexao() {
 
         try { //tenta conectar e preparar comando
 
             connect(); //Pode causar erros descritos no método (acima)
 
             //Testa a conexão preparando ela para um comando
             Statement com = con.createStatement();
 
 
 
             //Caso não ocorra nenhum erro em uma parte acima desconecta
             disconnect();
 
             //e retorna que a conexão foi estabelecida com sucesso
             return true;
 
             //caso não haja conexão nem sai do "con.createStatement();"
 
         } catch (Exception ex) { //caso haja erro antes do retorno
             disconnect();   //desconecta primeiro
             return false;   //retorna que conexão não foi bem sucedida
         }
     }
 
     public ArrayList<Projeto> buscarProjeto(String sql) throws ClassNotFoundException, SQLException {
 
         try {
             connect();
             Statement com = con.createStatement();
             ArrayList<Projeto> list = new ArrayList<Projeto>();
             ResultSet rs = com.executeQuery(sql);
             while (rs.next()) {
                 Projeto projeto = new Projeto();
                 projeto.setId(rs.getInt("idprojeto"));
                 projeto.setNome(rs.getString("nome"));
                 projeto.setTitulo(rs.getString("titulo"));
                 projeto.setEquipe(rs.getString("equipe"));
                 projeto.setPrazoDeExecucao(rs.getInt("p_execucao"));
                 projeto.setInicio(rs.getDate("d_inicio"));
                 projeto.setFim(rs.getDate("d_fim"));
                 projeto.setPalavrasChave(rs.getString("palavraschave").split(","));
                 projeto.setCampus(new Campus(rs.getInt("campus_idcampus"), ""));
                 projeto.setEixo(new Eixo(rs.getInt("eixo_ideixo"), ""));
                 projeto.setArquivoPDF(rs.getInt("arquivos_idarquivos"));
 
 
                 list.add(projeto);
             }
             disconnect();
             return list;
         } catch (SQLException | ClassNotFoundException ex) {
             disconnect();
             ex.printStackTrace();
         }
         return null;
 
     }
 
     public ResultSet listaProjetos(String sql) {
 
         try {
             connect();
             statement = con.createStatement();
             ResultSet result = statement.executeQuery(sql);
             return result;
 
         } catch (SQLException sqle) {
 
             System.out.println("SQL Inválido");
             sqle.printStackTrace();
             return null;
         } catch (ClassNotFoundException ex) {
         }
         /*try {
          statement = con.createStatement();
          resultset = statement.executeQuery("SELECT * FROM projetos");
          } catch (SQLException ex) {
          Logger.getLogger(JDBC.class.getName()).log(Level.SEVERE, null, ex);
          }
          */
         return resultset;
     }
        public ArrayList<Edital> buscarEdital(String sql) throws ClassNotFoundException, SQLException {
 
         try {
             connect();
             Statement com = con.createStatement();
             ArrayList<Edital> list = new ArrayList<Edital>();
             ResultSet rs = com.executeQuery(sql);
             while (rs.next()) {
                 Edital edital = new Edital();
                 edital.setId(rs.getInt("idprojeto"));
                 //edital.setNome(rs.getString("nome"));
                 edital.setNomeEdital(rs.getString("nome"));
                 edital.setInicio(rs.getDate("d_inicio"));
                 edital.setFim(rs.getDate("d_fim"));
                 //projeto.setPalavrasChaves(rs.getString("palavraschaves"));
                 //projeto.setCampus(com);
                 //projeto.setEixo(list);
                 edital.setArquivoPDF(rs.getInt("arquivos_idarquivos"));
 
 
                 list.add(edital);
             }
             disconnect();
             return list;
         } catch (SQLException | ClassNotFoundException ex) {
             disconnect();
             ex.printStackTrace();
         }
         return null;
 
     }
 
        
      /*    public void alterarProjeto(String sql) {
         try {
             connect();
             Statement com = con.createStatement();
             com.executeUpdate(sql);
             disconnect();
         } catch (ClassNotFoundException | SQLException ex) {
         }
     }*/
         
 //    ESTE É O TEMPLATE DO SEU MÉTODO, FAÇA USUFRUTO DELE COM SABEDORIA
 //    substitua os '<' '>' pelo dado correspondente
 //    public ArrayList<<Objeto>> obter<AlgumaCoisa>(<parametro>) throws ClassNotFoundException, SQLException {
 //        try {            
 //            connect();      //Conecta-se com o Banco de Dados
 //            
 //            Statement com = con.createStatement();    //Prepara para receber comando
 //            
 //            ArrayList<<Objeto>> list = new ArrayList<<Objeto>>(); //Prepara lista para retornar
 //    
 //            ResultSet rs = com.executeQuery("SELECT <colunas> FROM <banco.tabela> WHERE <coluna> = " + <parametro>); //Requisita tabela de resposta a consulta
 //    
 //            while(rs.next()) {   //Trabalha sobre a tabela retornada
 //                <Objeto> <objeto> = new <Objeto>();       //cria objeto temporario para armazenar na lista
 //                <objeto>.set<Atributo>(rs.getString("<NomeColuna>"));
 //                <objeto>.set<Atributo>(rs.getInt(<IdColuna>));
 //                list.add(<objeto>);       //Adiciona objetos a lista
 //            }
 //            
 //            disconnect();   //desconecta apos todos os resgates
 //            
 //            return list;    //e rerona a lista com oque foi desejado;
 //    
 //            //Caso deseja apenas o primeiro item use apenas 'if (next())' e 'else return null'
 //    
 //        } catch (SQLException|ClassNotFoundException ex) { //caso haja erro antes do retorno
 //            disconnect();   //desconecta primeiro
 //            return new <Objeto>Erro(ex);    //retorna classe respectiva com dados nulos e informação do erro
 //        }
 //    }
 }
