 package connectivity;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import main.WinkelApplication;
 import model.Category;
 import model.Klant;
 import model.Product;
 import view.ProductError;
 
 public class QueryManager {
 
     private final Dbmanager dbmanager;
 
     public QueryManager(Dbmanager dbmanager) {
         this.dbmanager = dbmanager;  // zorgt ervoor dat je de private DB HIER kan veranderen in de database die in de class zit waaruit je het aanroept. deze dbmanager kan dus ALLEEN door deze constructor veranderd worden.
     }
 
     public String getCategoryName(int categoryId) { //zorgt ervoor dat de naam van de category tevoorschijn komt van de ID die je meegeeft, maar doet er voor de rest niks mee (dus geen object/list)
         String categoryName = "";
         try {
             String sql = "SELECT naam FROM categorie WHERE categorie_id='" + categoryId + "'";
             ResultSet result = dbmanager.doQuery(sql);  //gebruikt doquery methode uit dbmanager class
             while (result.next()) {  
                 categoryName = result.getString("naam");
             }
         } catch (SQLException e) {
             System.out.println(Dbmanager.SQL_EXCEPTION + e.getMessage());
         }
         return categoryName;
     }
 
     public List<Category> getCategories() {  //haalt alle data van de category's uit de DB, en maakt van elke catagory een object met naam en omschrijving, en zet die in de arraylist
         List<Category> categories = new ArrayList<Category>();
         try {
             String sql = "SELECT * FROM categorie ORDER BY naam ASC";
             ResultSet result = dbmanager.doQuery(sql);
             while (result.next()) {
                 Category category = new Category(result.getInt("categorie_id"),
                         result.getString("naam"),
                         result.getString("omschrijving"));
                 categories.add(category);
             }
         } catch (SQLException e) {
             System.out.println(Dbmanager.SQL_EXCEPTION + e.getMessage());
         }
         return categories;
     }
 
     public Product getProduct(int productId) { //haalt de gegevens van het product op van het ID
         Product product = new Product();
         try {
             String sql = "SELECT * FROM product WHERE product_id='" + productId + "'";
             ResultSet result = dbmanager.doQuery(sql);
             if (result.next()) {
                 product = new Product(result.getInt("product_id"),
                         result.getInt("categorie_id"),
                         result.getString("naam"),
                         result.getString("omschrijving"),
                         result.getDouble("prijs"));
             }
         } catch (SQLException e) {
             System.out.println(Dbmanager.SQL_EXCEPTION + e.getMessage());
         }
         return product;
     }
 
     public Klant getKlant (int klantId) { // haalt klantgegevens op via klant ID
         Klant klant = new Klant ();
         try {
             String sql = "SELECT * FROM klant " +
                     "Where klant_id=' " + klantId + "'";
             ResultSet result = dbmanager.doQuery(sql);
             if (result.next()){
                 klant = new Klant (result.getInt("klant_id"),
                         result.getString("naam"),
                         result.getString("adres"),
                         result.getString("postcode"),
                         result.getString("woonplaats"));
             }
             }   catch (SQLException e) {
                     System.out.println(Dbmanager.SQL_EXCEPTION + e.getMessage());
             }
         return klant;
         }
     
     
     public List<Product> getProducts(int categoryId) { // haalt een lijst van producten op
         List<Product> products = new ArrayList<Product>();
         try {
            String sql = "SELECT * FROM product WHERE categorie_id='" + categoryId + "' AND zichtbaar = '1' ORDER BY naam ASC"; //haalt dus alle producten op met het meegegeven category ID
             ResultSet result = dbmanager.doQuery(sql);
             while (result.next()) {
                 products.add(new Product(result.getInt("product_id"),  //hetzelfde als categorylist, alleen maak je het object aan IN de add method
                         result.getInt("categorie_id"),
                         result.getString("naam"),
                         result.getString("omschrijving"),
                         result.getDouble("prijs")));
             }
         } catch (SQLException e) {
             System.out.println(Dbmanager.SQL_EXCEPTION + e.getMessage());
         }
         return products;
     }
 
     public void setOrder(model.Basket basket, String naam, String adres,  //plaatst bestelgegevens en de bestelling zelf in de DB
             String postcode, String woonplaats, String notes, String betaalmethode) {
         String SQL_order = "INSERT INTO `order` (naam, adres, postcode, woonplaats, notes, betaalmethode, datum)"
                 + " VALUES('" + naam + "', '" + adres + "', '" + postcode + "', '"
                 + woonplaats + "', '" + notes + "', '" + betaalmethode + "', CURDATE() )";
         int order_id = 0;   //defaultwaarde?
         try {
             ResultSet result = dbmanager.insertQuery(SQL_order);
             result.next();
             order_id = result.getInt(1);  //pakt de int uit de...eerste kolom?
         } catch (SQLException e) {
             System.out.println("connectivity.QueryManager.setOrder() Exception:" + e.getMessage());
         }
         List<Product> products = basket.getProducts();
         for (Product product : products) {    // wat doet een : in een for statement? stond volgens mij in java boek
             int product_id = product.getProductId();
             int aantal = basket.getProductAmount(product);
             String SQL_orderProduct = "INSERT INTO orderregel (product_id,order_id,aantal) VALUES (" + product_id + "," + order_id + "," + aantal + ")";
             dbmanager.insertQuery(SQL_orderProduct);
         }
     }
    public void addNewProduct(int categorie_id, String naam, double prijs, String omschrijving)
     {
        String SQL_newProduct = "INSERT INTO product (categorie_id, naam, prijs, omschrijving)"
                + " VALUES (' " + categorie_id + "', '" + naam + "', '" + prijs + "', '" + omschrijving + "')";
         try{
             ResultSet result = dbmanager.insertQuery(SQL_newProduct);
             result.next();
         } catch (SQLException e) {
             System.out.println("Toevoegen van nieuw product doet het niet :" + e.getMessage());
           
         }
     }
     
     public void deleteProduct(int product_id)
     {
        String SQL_deleteProduct = "UPDATE product SET zichtbaar = '0' WHERE product_id = '" + product_id + "'";
         try{
             ResultSet result = dbmanager.insertQuery(SQL_deleteProduct);
             result.next();
         } catch (SQLException e) {
             System.out.println("Verwijderen van product doet het niet :" + e.getMessage());
         }     
     }
     
     public void modifyProduct (int product_id, int categorie_id, String naam, double prijs, String omschrijving)
     {
         String SQL_modifyProduct = "UPDATE product SET categorie_id = '" + categorie_id + "', naam = '" + naam + "', prijs = '" + prijs + "', omschrijving = '"
                 + omschrijving + "' WHERE product_id = '" + product_id + "'";
         try{
             ResultSet result = dbmanager.insertQuery(SQL_modifyProduct);
             result.next();
         } catch (SQLException e) {
             System.out.println("wijzigen van product werkt niet: " + e.getMessage());
         }
     }
     
     public void addKlant (int klant_id, String naam, String adres, String postcode, String woonplaats )
     {
         String SQL_newKlant = "INSERT INTO klant (klant_id, naam,"
                 + " adres, postcode, woonplaats) VALUES ('" + klant_id + "', '"
                 + naam + "', '" + adres + "', '" + postcode +"', '" + woonplaats + "')";
         try {
             ResultSet result = dbmanager.insertQuery(SQL_newKlant);
             result.next();
         } catch (SQLException e) {
             System.out.println("Nieuwe klant toevoegen werkt niet: " + e.getMessage());
         }
     }
     
     public void deleteKlant (int klant_id)
     {
         String SQL_deleteKlant = "DELETE FROM klant WHERE klant_id = '" + klant_id + "'";
         try {
             ResultSet result = dbmanager.insertQuery(SQL_deleteKlant);
             result.next();
         } catch (SQLException e) {
             System.out.println("Klant verwijderen is niet gelukt: " + e.getLocalizedMessage());
         }
     }
     
     public void modifyKlant (int klant_id, String naam, String adres, String postcode, String woonplaats)
     {
         String SQL_modifyKlant = "UPDATE klant SET naam = '" + naam + "', adres = '" + adres + "', postcode = '" + postcode + "', woonplaats = '" +woonplaats + "'"
                 + "WHERE klant_id = '" + klant_id + "'";
         try {
             ResultSet result = dbmanager.insertQuery(SQL_modifyKlant);
             result.next();
         } catch (SQLException e) {
             System.out.println("Klant wijzigen is niet gelukt: " + e.getMessage());
         }
     }
     
     public List<Klant> getKlantList(){
         List<Klant> klanten = new ArrayList<Klant>();
         try{
             String sql ="SELECT * FROM klant";
             ResultSet result = dbmanager.doQuery(sql);
             while (result.next()){
                 klanten.add(new Klant(result.getInt("klant_id"),
                         result.getString("naam"), 
                         result.getString("adres"),
                         result.getString("postcode"), 
                         result.getString("woonplaats")));
             }
         } catch(SQLException e){
             System.out.println(Dbmanager.SQL_EXCEPTION + e.getMessage());
         }
         return klanten;
     }
     
        public int getCategoryId(String categoryName) {
        
         int id = -1;
         try {
             String SQL_getCategoryId = "SELECT categorie_id FROM categorie WHERE naam = '" + categoryName + "'";
             ResultSet result = dbmanager.doQuery(SQL_getCategoryId);
            if (result.next()) {
            id = result.getInt("categorie_id");
            }
         } catch (SQLException e) {
             System.out.println("Categorie id ophalen is niet gelukt" + e.getMessage());
         }
         return id;
     }
 }
