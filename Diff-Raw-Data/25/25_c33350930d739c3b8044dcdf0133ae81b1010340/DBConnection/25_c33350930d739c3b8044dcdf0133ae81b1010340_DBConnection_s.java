 package server;
 
 import java.sql.*;
 
 /**
  *
  * @author beretis
  */
 public class DBConnection {
 
     private Connection connect = null;
     private Statement statement = null;
     private ResultSet resultSet = null;
     String db_address;
     String[] arguments = null;
     ResultSetMetaData meta = null;
     int columncount;
     String sql;
 
     public DBConnection(String db_address) {
         this.db_address = db_address;
     }
 
     public String connect() {
         try {
             Class.forName("com.mysql.jdbc.Driver");
         } catch (Exception e) {
             System.err.println("Nepodarilo sa nacitat MySQL driver");
             return "MySQL driver failed!";
         }
         try {
             connect = DriverManager.getConnection(db_address);
             System.out.println("PODARILO SA PRIPOJIT");
             return "OK";
         } catch (Exception e) {
             System.err.println("Pripojenie zlyhalo");
             return "Connect to DB failed!";
         }
     }
 
     public void disConnect() {
         try {
             connect.close();
         } catch (SQLException ex) {
             System.err.println("Disconnect failed!");
         }
     }
 
     public String login(String id, String pass) {
         try {
             //metoda vraci ADMIN/MANAGER/HR/EMPLOYEE/KO
             int role = 0;
             String query = "SELECT role FROM Users WHERE id='" + id + "' and pass='" + pass + "'"; // vytviry SQL dotaz
             statement = connect.createStatement();
             resultSet = statement.executeQuery(query);
             while (resultSet.next()) //Vypise vysledok dotazu
             {
                 role = resultSet.getInt("role");
             }
             if (role == 0) //jestli je role prazdna, v tabulke neexistuje uzivatel so zadanym id a heslom
             {
                 System.out.println("Neplatne id nebo heslo");
             }
             switch (role) {
                 case 0: {
                     return "KO";
                 }
                 case 1: {
                     return "ADMIN";
                 }
                 case 2: {
                     return "MANAGER";
                 }
                 case 3: {
                     return "HR";
                 }
                 case 4: {
                     return "EMPLOYEE";
                 }
                 default:
                     return "KO";
             }
         } catch (SQLException ex) {
             ex.printStackTrace();
             System.out.println("Chyba v metode login v DBConnect");
             return "KO error in database";
         }
     }
 
 // metoda na pridavanie uzivatela ktore je pretazena, je mozne ju volat s menej parametrami
     public String addUser(int Groups_idGroups, String pass, String name, String lastname, String address, String city, String email, String phone, int role) {
         try {
             String query = "INSERT into Users (Groups_idGroups, pass, name, lastname, address, city, email, phone, role)  VALUES ('" + Groups_idGroups + "', '" + pass + "', '" + name
                     + "', '" + lastname + "', '" + address + "', '" + city + "', '" + email
                     + "', '" + phone + "', '" + role + "')";
             statement = connect.createStatement();
             int rows_effected = statement.executeUpdate(query);
            if (rows_effected != 0) {
                return "OK";
            } else {
                 return "KO nepodarilo sa pridat uzivatela";
             }
         } catch (Exception ex) {
             System.out.println("Chyba v metode addUser");
             return "KO error in database";
         }
     }
 
 // metoda ktora meni heslo uzivatela identifikovaneho pomocou ID
     public String changePass(String id, String oldpass, String newpass) {
         String query = "UPDATE Users SET pass = '" + newpass + "' WHERE id ='" + id + "' and pass ='" + oldpass + "'";
         try {
             statement = connect.createStatement();
             int rows_effected = statement.executeUpdate(query);
             if (rows_effected != 0) {
                 return "OK";
             } else {
                 return "KO id/heslo je nespravne";
             }
         } catch (Exception ex) {
             System.out.println("Chyba v metode addUser");
             return "KO chyba v metode";
         }
 
     }
 // metoda ktora uzavre resultset
 
     private void close() {
         try {
             if (resultSet != null) {
                 resultSet.close();
             }
 
             if (statement != null) {
                 statement.close();
             }
 
             if (connect != null) {
                 connect.close();
             }
         } catch (Exception e) {
         }
     }
 // potrebujem si vyjasnit niektore veci v tabulke
 
     public String deleteUser(String id) {
         try {
             String query = "DELETE from Users WHERE id = '" + id + "'";
             statement = connect.createStatement();
             int delete = statement.executeUpdate(query);
             if (delete == 0) {
                 return "KO User s id" + id + "neexistuje";
             }
             query = "DELETE from Teams_has_Users WHERE Users_id = '" + id + "'";
             int delete2 = statement.executeUpdate(query);
             if (delete2 == 0) {
                 System.out.println("User nieje clenom ziadneho teamu");
             }
             query = "DELETE from Users_has_Items WHERE Users_id = '" + id + "'";
             int delete3 = statement.executeUpdate(query);
             if (delete3 == 0) {
                 System.out.println("User nema ziadne Itemi");
             }
             return "OK";
         } catch (Exception ex) {
             System.out.println("chyba v metode deleteUser v triede DBConnection");
             return "KO";
         }
     }
 
     public String[] getArguments() {
         return arguments;
     }
 
     public Connection getConnect() {
         return connect;
     }
 
     public String getDb_address() {
         return db_address;
     }
 
     public ResultSet getResultSet() {
         return resultSet;
     }
 
     public Statement getStatement() {
         return statement;
     }
 
     public void setArguments(String[] arguments) {
         this.arguments = arguments;
     }
 
     public void setDb_address(String db_address) {
         this.db_address = db_address;
     }
 
     public String adminChangePass(String id, String newpass) {
         String query = "UPDATE Users SET pass = '" + newpass + "' WHERE id ='" + id + "'";
         try {
             statement = connect.createStatement();
             int rows_effected = statement.executeUpdate(query);
             if (rows_effected != 0) {
                 return "OK";
             } else {
                 return "KO id/heslo je nespravne";
             }
         } catch (Exception ex) {
             System.out.println("Chyba v metode addUser");
             return "KO chyba v metode";
         }
     }
 
     public String getInfo(String id) //1. verzia bez filtru vracia vsetky podstatne hodnoty
     {
         //metoda vraci atributy profilu KLIC HODNOTA;KLIC HODNOTA;...
         String query = "Select pass,name,lastname,address,city,email,phone,role,professia from Users where id =" + id;
         try {
             statement = connect.createStatement();
             resultSet = statement.executeQuery(query);
             meta = resultSet.getMetaData(); //ziska metadata o vysledku aby bolo mozne
             columncount = meta.getColumnCount();        // urcit pocet stlpcov
             String show_result = "";
             while (resultSet.next())//pre vsetky radky
             {
                 for (int i = 1; i < columncount; i++)//pre vsetky stlpce
                 {
                     show_result += meta.getColumnName(i) + ":" + resultSet.getString(i) + ";";
                 }
             }
             return show_result;
         } catch (Exception e) {
             System.out.println(e.getMessage());
             System.out.println("Chyba v metode execute_command");
             return "KO error in database";
         }
     }
 
     public String getGroups() {
         try {
             String result = "";
             String sql1 = "Select * from Groups";
             String sql2 = "Select Items_id from Groups_has_Items where Groups_idGroups=";
             Statement statement1 = connect.createStatement();
             Statement statement2 = connect.createStatement();
             ResultSet resultSet1 = statement1.executeQuery(sql1);
 
             while (resultSet1.next()) {
                 result += resultSet1.getInt(1) + " " + resultSet1.getString(2);
                 ResultSet resultSet2 = statement2.executeQuery(sql2 + resultSet1.getInt(1));
                 result += " " + resultSet2.getFetchSize();
                 while (resultSet2.next()) {
                     result += " " + resultSet2.getInt(1);
                 }
                 result += ";";
             }
             return result;
 
         } catch (SQLException ex) {
             System.out.println(ex.getMessage());
             System.out.println("Chyba v metode execute_command");
             return "KO error in database";
         }
 
     }
 
     String simpleShowAllFrom(String from)//metoda na vypisovanie celeho obsahu jednoduchych tabuliek
     {
         try {
             String result = "";
             sql = "Select * from " + from;
             statement = connect.createStatement();
             resultSet = statement.executeQuery(sql);
             meta = resultSet.getMetaData();
             columncount = meta.getColumnCount();
             while (resultSet.next()) {
                 result += resultSet.getInt(1) + " " + resultSet.getString(2) + ";";
             }
             return result;
 
         } catch (SQLException ex) {
             System.out.println(ex.getMessage());
             System.out.println("Chyba v metode execute_command");
             return "KO error in database";
         }
 
     }
 
     String executeSql(String sql) //metoda ktora vykona zadany SQL prikaz a vrati vysledok vo formate splnujucom protocol
     {
         String low_sql = sql.toLowerCase();
         if (low_sql.startsWith("select")) {
             try {
                 String result = "";
                 statement = connect.createStatement();
                 resultSet = statement.executeQuery(sql);
                 meta = resultSet.getMetaData();
                 columncount = meta.getColumnCount();
                 while (resultSet.next()) {
                     for (int i = 0; i < columncount; i++) {
                         result = meta.getColumnName(i) + ":" + resultSet.getNString(i) + ";";
                     }
                 }
                 return result;
             } catch (SQLException ex) {
                 System.out.println(ex.getMessage());
                 System.out.println("Chyba v metode execute_command");
                 return "KO error in database";
             }
         } else if (low_sql.startsWith("delete")) {
             try {
                 statement = connect.createStatement();
                 int delete = statement.executeUpdate(sql);
                 if (delete == 0) {
                     return "KO id does not exist in the database";
                 }
                 return "OK";
             } catch (Exception ex) {
                 System.out.println("chyba v metode deleteUser v triede DBConnection");
                 return "KO error in database";
             }
         } else if (low_sql.startsWith("insert") || low_sql.startsWith("replace")) {
             try {
                 statement = connect.createStatement();
                 int rows_effected = statement.executeUpdate(sql);
                 if (rows_effected != 0) {
                     return "OK";
                 } else {
                     return "KO wrong input";
                 }
             } catch (Exception ex) {
                 System.out.println("Chyba v metode addUser");
                 return "KO error in database";
             }
         } else if (low_sql.startsWith("update")) {
             try {
                 statement = connect.createStatement();
                 int rows_effected = statement.executeUpdate(sql);
                 if (rows_effected != 0) {
                     return "OK";
                 } else {
                     return "KO wrong input";
                 }
             } catch (Exception ex) {
                 System.out.println("Chyba v metode addUser");
                 return "KO error in database";
             }
         } else {
             System.out.println("Wrong sql");
             return "KO error in sql exeption";
         }
     }
 
     String getItems() {
         return simpleShowAllFrom("Items");
     }
 
     String getTeams() {
         return simpleShowAllFrom("Teams");
     }
 
     String getUserItems(String idUser) {
         sql = "Select * from Users_has_Items where id='" + idUser + "'";
         return executeSql(sql);
     }
 
     String getUserTeams(String idUser) {
         sql = "Select * from Teams_has_Users where id='" + idUser + "'";
         return executeSql(sql);
     }
 
     String addItem(String name) {
         sql = "Insert into Items (name) values('" + name + "')";
         return executeSql(sql);
     }
 
     String addTeam(String pm, String name, String project, String info, String goal)//co to je pm??
     {
         sql = "Insert into Teams (name,project,info,goal) values('" + name + "','" + project + "','" + info + "','" + goal + "')";
         return executeSql(sql);
     }
 
     String addGroup(String name, int[] idItems)// nebolo by lepsie vytvorit grupu len s nazvom a potom vkladat do tabulky Groups_has_Items?
     {
         sql = "Insert into Groups (name) values('" + name + "')";
         if (!executeSql(sql).equals("OK"))//to same ako is_ok = executeSql(sql) == "OK" ? true : false;
         {
             return "KO error while adding Group";
         }
         sql = "Select id from Gropus where name='" + name + "')";
         String id = executeSql(sql);
         String[] tokens = id.split(":");
         String input = "";
         id = tokens[1];
         id = id.substring(0, id.length() - 1);
         for (int i = 0; i < idItems.length; i++) {
             sql = "Replace into Groups_has_Items (Items_id) values('" + idItems[i] + "')";
             executeSql(sql);
         }
         return "OK";
     }
 
     String delTeam(String id) {
         sql = "Delete from Teams where id='" + id + "'";
         if (!executeSql(sql).equals("OK")) {
             return "KO error while adding Group";
         }
         sql = "Delete from Teams_has_Users where id='" + id + "'";
         if (!executeSql(sql).equals("OK")) {
             return "KO error while deleting Team";
         }
         return "OK";
     }
 
     String delGroup(String id) {
         sql = "Delete from Groups where id='" + id + "'";
         if (!executeSql(sql).equals("OK")) {
             return "KO error while adding Group";
         }
         sql = "Delete from Groups_has_Items where id='" + id + "'";
         if (!executeSql(sql).equals("OK")) {
             return "KO error while deleting Group";
         }
         return "OK";
     }
 
     String delItem(String id) {
         sql = "Delete from Items where id='" + id + "'";
         return executeSql(sql);
     }
 
     String updateUser(String id, String name, String lastname, String address, String city, String email, String phone, String professia) {
         String query = "Update Users set id = '" + id + "'," + "name = '" + name + "'," + "lastname = '" + lastname + "'," + "address = '" + address + "',"
                 + "city = '" + city + "'," + "email = '" + email + "'," + "phone = '" + phone + "'," + "professia = '" + professia + "'";
         return executeSql(sql);
     }
 
     String updateTeam(String id, String pm, String name, String project, String info, String goal)//
     {
         String query = "Update Teams set id = '" + id + "'," + "pm = '" + pm + "'," + "name = '" + name + "'," + "project = '" + project + "',"
                 + "info = '" + info + "'," + "goal = '" + goal + "'";
         return executeSql(sql);
     }
 
     String updateGroup(String id, String name, int[] idItems) //spytat sa co to ma presne robit
     {
         sql = "Update Groups set name='" + name + "' where idGroups='" + id + "'";
         if (!executeSql(sql).equals("OK")) {
             return "KO error while updating Group's name";
         }
         for (int i = 0; i < idItems.length; i++) {
             sql = "Replace Groups_has_Items (Items_id) values ('" + idItems[i] + "')";
             if (!executeSql(sql).equals("OK")) {
                 return "KO error while updating Group's Items";
             }
         }
         return "OK";
     }
 
     String updateItem(String id, String name) {
         sql = "Update Items set name='" + name + "' WHERE id='" + id + "'";
         return executeSql(sql);
     }
 
     String userInTeam(String idUser, String idTeam) {
         sql = "Replace into Teams_has_Users (Teams_id, Users_id) values('" + idTeam + "', '" + idUser + "')";
         return executeSql(sql);
     }
 
     String userOutTeam(String idUser, String idTeam) {
         sql = "Delete Teams_has_Users where Teams_id = '" + idTeam + "' and Users_id = '" + idUser + "'";
         return executeSql(sql);
     }
 
     String setTeamConfirmed(String idUser, String idTeam, boolean confirmed) {
         sql = "Update Teams_has_Users set confirmed='" + confirmed + "' WHERE Users_id='" + idUser + "' and Teams_id='" + idTeam + "'";
         return executeSql(sql);
     }
 
     String setItemState(String idUser, String idItem, boolean state) {
         sql = "Update Users_has_Items set state='" + state + "' WHERE Users_id='" + idUser + "' and Items_id='" + idItem + "'";
         return executeSql(sql);
     }
 
     String getUsers() {
         return "2;3;4;5;6;7;8;9;10;11;12;13;666;";
         //throw new UnsupportedOperationException("Not yet implemented");
     }
}
