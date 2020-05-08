 package Databank;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.Statement;
 import java.util.List;
 
 public class Connectie_Databank
 {
     //Eigenschappen databank
     private String connectieString = "";
     private Connection connectie = null;
     private PreparedStatement prepStatement = null;
    private Statement TEMP_Statement = null;    //Tijdelijk om dingen uit te testen
     private ResultSet inhoudQuery = null;
     
     //Inloggegevens PhpMyAdmin
     private String gebruikersnaam, wachtwoord;
     
     //Constructor met standaardinstellingen
     public Connectie_Databank()
     {
         connectieString = "jdbc:mysql://localhost/groep2_festivals";
         gebruikersnaam = "root";
         wachtwoord = "";
     }
     
     //Constructor met nieuwe data
     public Connectie_Databank(String connectionString, String gebruikersnaam, String wachtwoord)
     {
         this.connectieString = connectionString;
         this.gebruikersnaam = gebruikersnaam;
         this.wachtwoord = wachtwoord;
     }
     
     public void maakConnectie()
     {
         try
         {
             connectie = DriverManager.getConnection(connectieString, gebruikersnaam, wachtwoord);
         }
         catch(Exception e)
         {
             System.err.println(e.getMessage()); //Later verwijderen en gepaste meldingen en wijze van boodschap tonen
         }
     }
     
     public void voerQueryUit(String query, List<String> parameters)
     {
         try
         {
            //Reden preparedStatement: geen SQL-Injectie!
            prepStatement = connectie.prepareStatement(query);
 
            //Lijst met parameters uitlezen om de preparedStatement op te vullen
            for(int i=1; i<=parameters.size(); i++)
             {
               prepStatement.setString(i, parameters.get(i-1));
             }
            inhoudQuery = prepStatement.executeQuery();
         }
         catch(Exception e)
         {
             System.err.println(e.getMessage()); //Later verwijderen en gepaste meldingen en wijze van boodschap tonen
         }
     }
     
     public ResultSet haalResultSetOp()
     {
         return inhoudQuery;
     }
     
     public void sluitConnectie()
     {
         //ConnectieString leegmaken en alle objecten die te maken hebben met de connectie sluiten
         try
         {
             connectieString = "";
             if(connectie != null)
             {
                 connectie.close();
             }
             if(prepStatement != null)
             {
                 prepStatement.close();
             }
             if(inhoudQuery != null)
             {
                 inhoudQuery.close();
             }
         }
         catch(Exception e)
         {}
     }
 }
