 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package parcodb.database.objects;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import parcodb.database.DatabaseConnection;
 
 
 public class Paese extends Caratteristica {
 
     protected int abitanti;
     protected int CAP;
     
     public Paese(String nome, int abitanti, int CAP, Comune[] comuni) throws Exception {
         super(nome, comuni);
         this.abitanti = abitanti;
         this.CAP = CAP;
     }    
     
     private Paese(String nome, int abitanti, int CAP ) {
         super(nome);
         this.abitanti = abitanti;
         this.CAP = CAP;
     }
 
     public int getAbitanti() {
         return abitanti;
     }
 
     public int getCAP() {
         return CAP;
     }
 
     public void setAbitanti(int abitanti) {
         this.abitanti = abitanti;
     }
 
     public void setCAP(int CAP) {
         this.CAP = CAP;
     }
 
     @Override
     public void insertIntoDB(DatabaseConnection conn) throws SQLException {
         super.insertIntoDB(conn); 
         PreparedStatement insertStatement = conn.getConn().prepareStatement("INSERT INTO `bdati`.`Paese` (`nome`, `abitanti`, `cap`) VALUES ( ? , ? , ? );");
         
         insertStatement.setString(1, nome);
         insertStatement.setInt(2, abitanti);
         insertStatement.setInt(3, CAP);
         
         insertStatement.execute();
     }
     
     
     static public Paese[] getPaesi(DatabaseConnection conn) throws SQLException {
        PreparedStatement preparedStatement = conn.getConn().prepareStatement("SELECT `nome`, `abitanti`, `cap` FROM `bdati`.`Paese`");
         
         ResultSet result = preparedStatement.executeQuery();
         
         int DIM = DatabaseConnection.getResultDim(result);
         Paese[] paesi = new Paese[DIM];
         int i;
         for (i=0; result.next(); i++) {
             paesi[i] = new Paese(result.getString(1), result.getInt(2), result.getInt(3));
         }
         
         if (i != DIM)
             throw new SQLException("il numero di risultati di getPaese() è incongruo ("+i+','+DIM+')');
         
         return paesi;
     }
 }
