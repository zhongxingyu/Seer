 
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package database;
 
 import database.extra.Component;
 import database.extra.Ingredient;
 import database.extra.InvoiceItem;
 import database.models.ArticleModel;
 import database.models.BasicIngredientModel;
 import database.models.ContactModel;
 import database.models.InvoiceModel;
 import database.models.RecipeModel;
 import database.tables.Article;
 import database.tables.BasicIngredient;
 import database.tables.Contact;
 import database.tables.Invoice;
 import database.tables.Recipe;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import logging.MyLogger;
 import tools.Configuration;
 import tools.StringTools;
 
 /**
  *
  * @author Warkst
  */
 public class Database {
 
     private static Database driver;
     private Connection connection;
     private Statement statement;
     private Map<Integer, BasicIngredient> basicIngredientsById;
     private Map<Integer, Recipe> recipesById;
     private Map<Integer, Article> articlesById;
     private Map<Integer, Contact> contactsById;
     private Map<Integer, Invoice> invoicesById;
     private Map<Integer, Invoice> invoicesByNumber;
     private Map<String, BasicIngredient> basicIngredientsByName;
     private Map<String, Recipe> recipesByName;
     private Map<String, Integer> municipales;
     private Map<String, Article> articlesByName;
     private Map<String, Article> articlesByCode;
     private Map<String, Contact> contactsBySortKey;
     private Map<Integer, ArrayList<Invoice>> InvoicesbyYear;
 
     private Database() {
         try {
             basicIngredientsById = new TreeMap<Integer, BasicIngredient>();
             recipesById = new TreeMap<Integer, Recipe>();
             articlesById = new TreeMap<Integer, Article>();
             articlesByCode = new TreeMap<String, Article>();
             contactsById = new TreeMap<Integer, Contact>();
             invoicesById = new TreeMap<Integer, Invoice>();
             invoicesByNumber = new TreeMap<Integer, Invoice>();
             InvoicesbyYear = new TreeMap<Integer, ArrayList<Invoice>>();
 
             basicIngredientsByName = new TreeMap<String, BasicIngredient>(String.CASE_INSENSITIVE_ORDER);
             recipesByName = new TreeMap<String, Recipe>(String.CASE_INSENSITIVE_ORDER);
             articlesByName = new TreeMap<String, Article>(String.CASE_INSENSITIVE_ORDER);
             contactsBySortKey = new TreeMap<String, Contact>(String.CASE_INSENSITIVE_ORDER);
 
             municipales = new TreeMap<String, Integer>(String.CASE_INSENSITIVE_ORDER);
             // connect to db
             Class.forName("org.sqlite.JDBC");
             connection = DriverManager.getConnection(Configuration.center().getDB_URL());
             statement = connection.createStatement();
 
             // copy data
             loadContacts();
             loadBasicIngredients();
             loadRecipes();
             loadMunicipales();
             loadArticles();
             loadInvoices();
 
         } catch (Exception ex) {
             Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
             MyLogger.log(ex.getMessage());
         }
     }
 
     public Map<String, Article> getArticlesByCode() {
         return articlesByCode;
     }
 
     public static Database driver() {
         if (driver == null) {
             driver = new Database();
         }
         return driver;
     }
 
     public Connection getConnection() {
         return connection;
     }
 
     private void loadInvoices() throws SQLException {
         ResultSet rs = statement.executeQuery("SELECT * FROM " + Configuration.center().getDB_TABLE_INV() + " order by id");
         while (rs.next()) {
             try {
                 Invoice inv = Invoice.createStub(
                         rs.getInt("id"),
                         rs.getInt("number"),
                         rs.getString("date"),
                         contactsById.get(rs.getInt("clientid")),
                         rs.getString("pricecode"),
                         rs.getDouble("save"));
                 SimpleDateFormat formatter = new SimpleDateFormat("dd/mm/yyyy");
                 Date datum = formatter.parse(rs.getString("date"));
                 Calendar now = Calendar.getInstance();
                 now.setTime(datum);
                 int year = now.get(Calendar.YEAR);
                 if (!InvoicesbyYear.containsKey(year)) {
                     InvoicesbyYear.put(year, new ArrayList<Invoice>());
                 }
                 InvoicesbyYear.get(year).add(inv);
                 invoicesById.put(inv.getPrimaryKeyValue(), inv);
                 invoicesByNumber.put(inv.getNumber(), inv);
             } catch (ParseException ex) {
                 Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
 
         rs = statement.executeQuery("SELECT * FROM " + Configuration.center().getDB_TABLE_INV_ART());
         int links = 0;
         while (rs.next()) {
             int invoiceid = rs.getInt("invoiceid");
             int articleid = rs.getInt("articleid");
             double amount = rs.getDouble("amount");
             invoicesById.get(invoiceid).addItem(amount, articlesById.get(articleid));
             links++;
         }
         MyLogger.log("Database driver:: loaded " + invoicesById.size() + " invoices (linked " + links + " articles)!", MyLogger.LOW);
     }
 
     private void loadContacts() throws SQLException {
         ResultSet rs = statement.executeQuery("SELECT * FROM " + Configuration.center().getDB_TABLE_CON());
         while (rs.next()) {
             Contact contact = Contact.loadWithValues(
                     rs.getInt("id"),
                     rs.getString("sortkey"),
                     rs.getString("firm"),
                     rs.getString("contact"),
                     rs.getString("address"),
                     rs.getString("zipcode"),
                     rs.getString("municipality"),
                     rs.getString("telephone"),
                     rs.getString("telephone2"),
                     rs.getString("cellphone"),
                     rs.getString("fax"),
                     rs.getString("email"),
                     rs.getString("taxnr"),
                     rs.getString("pricecode"),
                     rs.getString("notes"),
                     rs.getString("type"));
             contactsById.put(contact.getPrimaryKeyValue(), contact);
             contactsBySortKey.put(contact.getSortKey().toLowerCase(), contact);
         }
         MyLogger.log("Database driver:: loaded " + contactsById.size() + " contacts (" + getSuppliersAlphabetically().size() + " suppliers and " + getClientsAlphabetically().size() + " clients)!", MyLogger.LOW);
     }
 
     public FunctionResult<Contact> addContact(ContactModel model) throws SQLException {
         if (contactsBySortKey.containsKey(model.getSortKey().toLowerCase())) {
             return new FunctionResult<Contact>(4, null, "Er bestaat al een contactpersoon met deze toonnaam.");
         }
 
         String msg = "";
         int code = 0;
         Contact newCon = null;
         String insertTableSQL = "INSERT INTO contacts"
                 + "(firm, sortkey, contact, address, zipcode, municipality, telephone, telephone2, cellphone, fax, email, taxnr, pricecode, notes, type) VALUES"
                 + "(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
         PreparedStatement preparedStatement = connection.prepareStatement(insertTableSQL);
         try {
             preparedStatement.setString(1, model.getFirm());
             preparedStatement.setString(2, model.getSortKey());
             preparedStatement.setString(3, model.getContact());
             preparedStatement.setString(4, model.getAddress());
             preparedStatement.setString(5, model.getZipcode());
             preparedStatement.setString(6, model.getMunicipality());
             preparedStatement.setString(7, model.getTelephone());
             preparedStatement.setString(8, model.getTelephone2());
             preparedStatement.setString(9, model.getCellphone());
             preparedStatement.setString(10, model.getFax());
             preparedStatement.setString(11, model.getEmail());
             preparedStatement.setString(12, model.getTaxnumber());
             preparedStatement.setString(13, model.getPricecode());
             preparedStatement.setString(14, model.getNotes());
             preparedStatement.setString(15, model.getType());
             preparedStatement.executeUpdate();
 
             ResultSet rs = statement.executeQuery("SELECT id FROM " + Configuration.center().getDB_TABLE_CON() + " WHERE sortkey=\"" + model.getSortKey() + "\"");
             if (rs.next()) {
                 newCon = Contact.createFromModel(rs.getInt("id"), model);
                 contactsById.put(newCon.getPrimaryKeyValue(), newCon);
                 contactsBySortKey.put(newCon.getSortKey().toLowerCase(), newCon);
             } else {
                 code = 2;
                 msg = "Het toevoegen is geslaagd maar er is een probleem opgetreden. Herstart het programma.";
             }
         } catch (SQLException ex) {
             Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
             code = 1;
             msg = ex.getMessage();
         } finally {
             preparedStatement.close();
         }
         return new FunctionResult<Contact>(code, newCon, msg);
     }
 
     public FunctionResult<Invoice> addInvoice(InvoiceModel model) throws SQLException {
         int code = 0;
         String msg = "";
         Invoice newInv = null;
         connection.setAutoCommit(false);
         String insertTableSQL = "INSERT INTO invoices"
                 + "(number, date, clientid, pricecode, save) VALUES"
                 + "(?,?,?,?,?)";
         String insertinvart = "INSERT INTO invoicesarticles"
                 + "(invoiceid, articleid, amount) VALUES"
                 + "(?,?,?)";
         PreparedStatement preparedStatement = connection.prepareStatement(insertTableSQL);
         try {
             preparedStatement.setInt(1, model.getNumber());
             preparedStatement.setString(2, model.getDate());
             preparedStatement.setInt(3, model.getClient().getPrimaryKeyValue());
             preparedStatement.setString(4, model.getPriceCode());
             preparedStatement.setDouble(5, model.getSave());
             preparedStatement.executeUpdate();
 
             ResultSet rs = statement.executeQuery("SELECT * FROM invoices WHERE number=\"" + model.getNumber() + "\"");
             if (rs.next()) {
                 newInv = Invoice.createFromModel(rs.getInt("id"), model);
                 invoicesById.put(newInv.getPrimaryKeyValue(), newInv);
                 invoicesByNumber.put(newInv.getNumber(), newInv);
                 SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                 Date datum = formatter.parse(rs.getString("date"));
                 Calendar now = Calendar.getInstance();
                 now.setTime(datum);
                 int year = now.get(Calendar.YEAR);
                 if (!InvoicesbyYear.containsKey(year)) {
                     InvoicesbyYear.put(year, new ArrayList<Invoice>());
                 }
                 InvoicesbyYear.get(year).add(newInv);
             } else {
                 code = 2;
                 msg = "Er is iets verkeerd gegaan. Herstart het programma.";
             }
 
             ArrayList<InvoiceItem> items = model.getItems();
             for (InvoiceItem item : items) {
                 Article art = item.getArticle();
                 preparedStatement = connection.prepareStatement(insertinvart);
                 preparedStatement.setInt(1, newInv.getPrimaryKeyValue());
                 preparedStatement.setInt(2, art.getPrimaryKeyValue());
                 preparedStatement.setDouble(3, item.getAmount());
                 preparedStatement.executeUpdate();
 		
 		newInv.addItem(item.getAmount(), item.getArticle());
             }
             connection.commit();
         } catch (ParseException ex) {
             Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
             connection.rollback();
         } catch (SQLException ex) {
             Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
             code = 1;
             msg = ex.getMessage();
             connection.rollback();
         } finally {
             preparedStatement.close();
             connection.setAutoCommit(true);
         }
         return new FunctionResult<Invoice>(code, newInv, msg);
     }
 
     public FunctionResult<Article> addArticle(ArticleModel model) throws SQLException {
         int code = 0;
         String msg = "";
         Article newArt = null;
         String insertTableSQL = "INSERT INTO articles"
                 + "(name, code, pricea, priceb, unit, taxes) VALUES"
                 + "(?,?,?,?,?,?)";
         PreparedStatement preparedStatement = connection.prepareStatement(insertTableSQL);
         try {
             preparedStatement.setString(1, model.getName());
             preparedStatement.setString(2, model.getCode());
             preparedStatement.setDouble(3, model.getPriceA());
             preparedStatement.setDouble(4, model.getPriceB());
             preparedStatement.setString(5, model.getUnit());
             preparedStatement.setDouble(6, model.getTaxes());
             preparedStatement.executeUpdate();
 
             ResultSet rs = statement.executeQuery("SELECT id FROM " + Configuration.center().getDB_TABLE_ART() + " WHERE name=\"" + model.getName() + "\"");
             if (rs.next()) {
                 newArt = Article.createFromModel(rs.getInt("id"), model);
                 articlesById.put(newArt.getPrimaryKeyValue(), newArt);
                 articlesByName.put(newArt.getName(), newArt);
                 articlesByCode.put(newArt.getCode(), newArt);
             } else {
                 code = 2;
                 msg = "Er is iets verkeerd gegaan. Herstart het programma.";
             }
         } catch (SQLException ex) {
             Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
             code = 1;
             msg = ex.getMessage();
         } finally {
             preparedStatement.close();
         }
         return new FunctionResult<Article>(code, newArt, msg);
     }
 
     private void loadBasicIngredients() throws SQLException {
         ResultSet rs = statement.executeQuery("SELECT * FROM " + Configuration.center().getDB_TABLE_INGR());
         while (rs.next()) {
             BasicIngredient b = BasicIngredient.loadWithValues(
                     rs.getInt("id"),
                     contactsById.get(rs.getInt("firmaid")),
                     rs.getString("merk"),
                     rs.getString("verpakking"),
                     rs.getDouble("prijsPerKilo"),
                     rs.getDouble("prijsPerVerpakking"),
                     rs.getDouble("gewichtPerVerpakking"),
                     rs.getDouble("verliespercentage"),
                     rs.getDouble("BTW"),
                     rs.getString("naam"),
                     rs.getString("datum"),
                     rs.getString("opmerking"));
             basicIngredientsById.put(b.getPrimaryKeyValue(), b);
             basicIngredientsByName.put(b.getName().toLowerCase(), b);
         }
 
 //        System.out.println("Database driver:: loaded " + basicIngredientsById.size() + " basic ingredients!");
         MyLogger.log("Database driver:: loaded " + basicIngredientsById.size() + " basic ingredients!", MyLogger.LOW);
     }
 
     public FunctionResult<BasicIngredient> addBasicIngredient(BasicIngredientModel ingredient) throws SQLException {
         if (basicIngredientsByName.containsKey(ingredient.getName().toLowerCase())) {
             return new FunctionResult<BasicIngredient>(4, null, "Er bestaat al een basisingrediënt met deze naam.");
         }
 
         int code = 0;
         String msg = "";
         BasicIngredient newBI = null;
         String insertTableSQL = "INSERT INTO ingredients"
                 + "(firmaid, naam, merk, verpakking, prijsPerKilo, prijsPerVerpakking, gewichtPerVerpakking, verliespercentage, BTW, datum, opmerking) VALUES"
                 + "(?,?,?,?,?,?,?,?,?,?,?)";
         PreparedStatement preparedStatement = connection.prepareStatement(insertTableSQL);
         try {
             preparedStatement.setInt(1, ingredient.getSupplier().getPrimaryKeyValue());
             preparedStatement.setString(2, ingredient.getName());
             preparedStatement.setString(3, ingredient.getBrand());
             preparedStatement.setString(4, ingredient.getPackaging());
             preparedStatement.setDouble(5, ingredient.getPricePerWeight());
             preparedStatement.setDouble(6, ingredient.getPricePerUnit());
             preparedStatement.setDouble(7, ingredient.getWeightPerUnit());
             preparedStatement.setDouble(8, ingredient.getLossPercent());
             preparedStatement.setDouble(9, ingredient.getTaxes());
             preparedStatement.setString(10, ingredient.getDate());
             preparedStatement.setString(11, ingredient.getNotes());
             preparedStatement.executeUpdate();
 
             ResultSet rs = statement.executeQuery("SELECT id FROM " + Configuration.center().getDB_TABLE_INGR() + " WHERE naam=\"" + ingredient.getName() + "\"");
             if (rs.next()) {
                 newBI = BasicIngredient.createFromModel(rs.getInt("id"), ingredient);
                 basicIngredientsById.put(newBI.getPrimaryKeyValue(), newBI);
                 basicIngredientsByName.put(newBI.getName(), newBI);
             } else {
                 code = 2;
                 msg = "Er is iets verkeerd gegaan. Herstart het programma.";
             }
         } catch (SQLException ex) {
             Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
             code = 1;
             msg = ex.getMessage();
         } finally {
             preparedStatement.close();
         }
         return new FunctionResult<BasicIngredient>(code, newBI, msg);
     }
 
     private void loadRecipes() throws SQLException {
         ResultSet rs = statement.executeQuery("SELECT * FROM " + Configuration.center().getDB_TABLE_REC());
         while (rs.next()) {
             Recipe r = Recipe.createStub(
                     rs.getInt("id"),
                     rs.getString("naam"),
                     rs.getString("datum"),
                     rs.getString("bereiding"),
                     rs.getDouble("nettogewicht"));
             recipesById.put(r.getPrimaryKeyValue(), r);
             recipesByName.put(r.getName().toLowerCase(), r);
         }
         // also copy all linked ingredients and recipes
         rs = statement.executeQuery("SELECT * FROM " + Configuration.center().getDB_TABLE_REC_INGR());
         int ingrLinks = 0;
         while (rs.next()) {
             int recipeName = rs.getInt("receptid");
             int ingredientName = rs.getInt("ingredientid");
             int rank = rs.getInt("rang");
             double quantity = rs.getDouble("quantiteit");
             recipesById.get(recipeName).addIngredient(basicIngredientsById.get(ingredientName), rank, quantity);
             ingrLinks++;
         }
         rs = statement.executeQuery("SELECT * FROM " + Configuration.center().getDB_TABLE_REC_REC());
         int recLinks = 0;
         while (rs.next()) {
             int recipeName = rs.getInt("receptid");
             int subrecipeName = rs.getInt("deelreceptid");
             int rank = rs.getInt("rang");
             double quantity = rs.getDouble("quantiteit");
             recipesById.get(recipeName).addIngredient(recipesById.get(subrecipeName), rank, quantity);
             recLinks++;
         }
 
 //        System.out.println("Database driver:: loaded " + recipesById.size() + " recipes (linked " + ingrLinks + " ingredients and " + recLinks + " recipes)!");
         MyLogger.log("Database driver:: loaded " + recipesById.size() + " recipes (linked " + ingrLinks + " ingredients and " + recLinks + " recipes)!", MyLogger.LOW);
     }
 
     public FunctionResult<Recipe> addRecipe(RecipeModel recipe) throws SQLException {
         if (recipesByName.containsKey(recipe.getName().toLowerCase())) {
             return new FunctionResult<Recipe>(4, null, "Er bestaat al een recept met deze naam");
         }
 
         boolean autoComm = connection.getAutoCommit();
         int code = 0;
         String msg = "";
         Recipe newRec = null;
         PreparedStatement stmt = null;
         try {
             connection.setAutoCommit(false);
             String insertrecipe = "INSERT INTO recipes"
                     + "(naam, bereiding, datum, nettogewicht) VALUES"
                     + "(?,?,?,?)";
             String insertrecrec = "INSERT INTO recipesrecipes"
                     + "(receptid, deelreceptid, rang, quantiteit) VALUES"
                     + "(?,?,?,?)";
             String insertrecing = "INSERT INTO recipesingredients"
                     + "(receptid, ingredientid, rang, quantiteit) VALUES"
                     + "(?,?,?,?)";
             stmt = connection.prepareStatement(insertrecipe);
             stmt.setString(1, recipe.getName());
             stmt.setString(2, recipe.getPreparation());
             stmt.setString(3, recipe.getDate());
             stmt.setDouble(4, recipe.getNetWeight());
             stmt.executeUpdate();
 //	    connection.commit();
 
             ResultSet rs = statement.executeQuery("SELECT id FROM " + Configuration.center().getDB_TABLE_REC() + " WHERE naam=\"" + recipe.getName() + "\"");
             if (rs.next()) {
                 newRec = Recipe.createFromModel(rs.getInt("id"), recipe);
                 recipesById.put(newRec.getPrimaryKeyValue(), newRec);
                 recipesByName.put(newRec.getName().toLowerCase(), newRec);
             } else {
                 code = 2;
                 msg = "Er is iets verkeerd gegaan. Herstart het programma.";
             }
 
             Map<Integer, Component> ings = recipe.getComponents();
             for (int i : ings.keySet()) {
                 Component comp = ings.get(i);
                 if (comp.getIngredient().isBasicIngredient()) {
                     stmt = connection.prepareStatement(insertrecing);
                 } else {
                     stmt = connection.prepareStatement(insertrecrec);
                 }
                 stmt.setInt(1, newRec.getPrimaryKeyValue());
                 stmt.setInt(2, comp.getIngredient().getPrimaryKeyValue());
                 stmt.setInt(3, comp.getRank());
                 stmt.setDouble(4, comp.getQuantity());
                 stmt.executeUpdate();
             }
             connection.commit();
 
         } catch (SQLException ex) {
             Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
             connection.rollback();
             code = 1;
             msg = ex.getMessage();
         } finally {
             connection.setAutoCommit(autoComm);
             if (stmt != null) {
                 stmt.close();
             }
         }
         return new FunctionResult<Recipe>(code, newRec, msg);
     }
 
     public FunctionResult<Invoice> updateInvoice(Invoice invoice) throws SQLException {
         boolean autoComm = connection.getAutoCommit();
         int code = 0;
         String msg = "";
         String insertinvart = "INSERT INTO invoicesarticles"
                 + "(invoiceid, articleid, amount) VALUES"
                 + "(?,?,?)";
         PreparedStatement preparedStatement = null;
         try {
             connection.setAutoCommit(false);
             String sql = "update invoices set number=?, date=?, clientid=?, pricecode=?, save=? where id=?";
             preparedStatement = connection.prepareStatement(sql);
             preparedStatement.setInt(1, invoice.getNumber());
             preparedStatement.setString(2, invoice.getDate());
             preparedStatement.setInt(3, invoice.getClient().getPrimaryKeyValue());
             preparedStatement.setString(4, invoice.getPriceCode());
             preparedStatement.setDouble(5, invoice.getSave());
             preparedStatement.setInt(6, invoice.getPrimaryKeyValue());
             preparedStatement.executeUpdate();
             Statement st = connection.createStatement();
             sql = "DELETE FROM invoicesarticles WHERE invoiceid = " + invoice.getPrimaryKeyValue();
             st.executeUpdate(sql);
             ArrayList<InvoiceItem> items = invoice.items();
 
             for (InvoiceItem item : items) {
                 Article art = item.getArticle();
                 preparedStatement = connection.prepareStatement(insertinvart);
                 preparedStatement.setInt(1, invoice.getPrimaryKeyValue());
                 preparedStatement.setInt(2, art.getPrimaryKeyValue());
                 preparedStatement.setDouble(3, item.getAmount());
                 preparedStatement.executeUpdate();
             }
             connection.commit();
         } catch (SQLException ex) {
             Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
             code = 1;
         } finally {
             connection.setAutoCommit(autoComm);
             if (preparedStatement != null) {
                 preparedStatement.close();
             }
         }
         return new FunctionResult<Invoice>(code, null, null);
     }
 
     public FunctionResult<Recipe> updateRecipe(Recipe recipe) throws SQLException {
         boolean autoComm = connection.getAutoCommit();
         int code = 0;
         String insertrecrec = "INSERT INTO recipesrecipes"
                 + "(receptid, deelreceptid, rang, quantiteit) VALUES"
                 + "(?,?,?,?)";
         String insertrecing = "INSERT INTO recipesingredients"
                 + "(receptid, ingredientid, rang, quantiteit) VALUES"
                 + "(?,?,?,?)";
         PreparedStatement stmt = null;
         try {
             connection.setAutoCommit(false);
             String sql = "update recipes set naam=?, bereiding=?, datum=?, nettogewicht=? where id=?";
             stmt = connection.prepareStatement(sql);
             stmt.setString(1, recipe.getName());
             stmt.setString(2, recipe.getPreparation());
             stmt.setString(3, recipe.getDate());
             stmt.setDouble(4, recipe.getNetWeight());
             stmt.setInt(5, recipe.getPrimaryKeyValue());
             stmt.executeUpdate();
             Statement st = connection.createStatement();
             sql = "DELETE FROM recipesrecipes WHERE receptid = " + recipe.getPrimaryKeyValue();
             st.executeUpdate(sql);
             sql = "DELETE FROM recipesingredients WHERE receptid = " + recipe.getPrimaryKeyValue();
             st.executeUpdate(sql);
             Map<Integer, Component> ings = recipe.getComponents();
 
             for (int i : ings.keySet()) {
                 Component comp = ings.get(i);
                 if (comp.getIngredient().isBasicIngredient()) {
                     stmt = connection.prepareStatement(insertrecing);
                 } else {
                     stmt = connection.prepareStatement(insertrecrec);
                 }
                 stmt.setInt(1, recipe.getPrimaryKeyValue());
                 stmt.setInt(2, comp.getIngredient().getPrimaryKeyValue());
                 stmt.setInt(3, comp.getRank());
                 stmt.setDouble(4, comp.getQuantity());
                 stmt.executeUpdate();
             }
             connection.commit();
         } catch (SQLException ex) {
             Logger.getLogger(Database.class.getName()).log(Level.SEVERE, null, ex);
             code = 1;
         } finally {
             connection.setAutoCommit(autoComm);
             if (stmt != null) {
                 stmt.close();
             }
         }
         return new FunctionResult<Recipe>(code, null, null);
     }
 
     private void loadMunicipales() throws SQLException {
         ResultSet rs = statement.executeQuery("SELECT * FROM " + Configuration.center().getDB_TABLE_MUNI());
         while (rs.next()) {
             int code = rs.getInt("code");
             String name = StringTools.capitalizeEach(rs.getString("name"));
             municipales.put(name, code);
         }
     }
 
     private void loadArticles() throws SQLException {
         ResultSet rs = statement.executeQuery("SELECT * FROM " + Configuration.center().getDB_TABLE_ART());
         while (rs.next()) {
             Article a = Article.loadWithValues(
                     rs.getInt("id"),
                     rs.getString("code"),
                     rs.getString("name"),
                     rs.getDouble("pricea"),
                     rs.getDouble("priceb"),
                     rs.getString("unit"),
                     rs.getDouble("taxes"));
             articlesById.put(a.getPrimaryKeyValue(), a);
             articlesByName.put(a.getName(), a);
             articlesByCode.put(a.getCode(), a);
         }
 
 //        System.out.println("Database driver:: loaded " + suppliersById.size() + " suppliers!");
         MyLogger.log("Database driver:: loaded " + articlesById.size() + " articles!", MyLogger.LOW);
     }
 //    
 //    public Map<Integer, Contact> getClients() {
 //	Map<Integer, Contact> clients = new TreeMap<Integer, Contact>();
 //	for (Contact contact : contactsById.values()) {
 //	    if (!contact.isSupplier()) {
 //		clients.put(contact.getPrimaryKeyValue(), contact);
 //	    }
 //	}
 //	return clients;
 //    }
 //    
 
     public Map<String, Contact> getClientsAlphabetically() {
         Map<String, Contact> clients = new TreeMap<String, Contact>();
         for (Contact contact : contactsBySortKey.values()) {
             if (!contact.isSupplier()) {
                 clients.put(contact.getSortKey().toLowerCase(), contact);
             }
         }
         return clients;
     }
 //
 //    public Map<Integer, Contact> getSuppliers() {
 //	Map<Integer, Contact> suppliers = new TreeMap<Integer, Contact>();
 //	for (Contact contact : contactsById.values()) {
 //	    if (contact.isSupplier()) {
 //		suppliers.put(contact.getPrimaryKeyValue(), contact);
 //	    }
 //	}
 //	return suppliers;
 //    }
 //    
 
     public Map<String, Contact> getSuppliersAlphabetically() {
         Map<String, Contact> suppliers = new TreeMap<String, Contact>();
         for (Contact contact : contactsBySortKey.values()) {
             if (contact.isSupplier()) {
                 suppliers.put(contact.getSortKey().toLowerCase(), contact);
             }
         }
         return suppliers;
     }
 
     public Map<Integer, BasicIngredient> getBasicIngredients() {
         return basicIngredientsById;
     }
 
     public Map<String, BasicIngredient> getBasicIngredientsAlphabetically() {
         return basicIngredientsByName;
     }
 
     public Map<Integer, Article> getArticles() {
         return articlesById;
     }
 
     public Map<String, Article> getArticlesAlphabetically() {
         return articlesByName;
     }
 
     public Map<Integer, Recipe> getRecipes() {
         return recipesById;
     }
 
     public Map<String, Recipe> getRecipesAlphabetically() {
         return recipesByName;
     }
 
     public Map<String, Integer> getMunicipales() {
         return municipales;
     }
 
     public Map<Integer, Invoice> getInvoicesById() {
         return invoicesById;
     }
 
     public Map<Integer, Invoice> getInvoicesByNumber() {
         return invoicesByNumber;
     }
 
     public boolean isArticleCodeUnique(String code) {
         Set<String> codes = new TreeSet<String>();
         for (Article article : articlesByName.values()) {
             if (codes.contains(code)) {
                 return false;
             } else {
                 codes.add(article.getCode());
             }
         }
         return true;
     }
 
     public boolean isArticleCodeUnique(String code, Article exclude) {
         Set<String> codes = new TreeSet<String>();
         for (Article article : articlesByName.values()) {
             if (codes.contains(code)) {
                 return false;
             } else {
                 if (article != exclude) {
                     codes.add(article.getCode());
                 }
             }
         }
         return true;
     }
 
     public List<Ingredient> getIngredients() {
         Map<String, Ingredient> sorted = new TreeMap<String, Ingredient>(String.CASE_INSENSITIVE_ORDER);
         sorted.putAll(basicIngredientsByName);
         sorted.putAll(recipesByName);
         ArrayList<Ingredient> me = new ArrayList<Ingredient>();
         me.addAll(sorted.values());
         return me;
     }
 
     public Map<String, Contact> getContactsAlphabetically() {
         return contactsBySortKey;
     }
 
     /**
      * Tries to send an update command to the database to update the record with
      * the specified id of the specified table to set the new values. Values is
      * expected to be of the format: "column_name = value, ...".
      *
      * @param table
      * @param primaryKey
      * @param primaryKeyValue
      * @param values
      * @return
      */
     public FunctionResult executeUpdate(String table, String primaryKey, int primaryKeyValue, String values) {
         try {
             statement.executeUpdate("UPDATE " + table + " SET " + values + " WHERE " + primaryKey + " = \"" + primaryKeyValue + "\"");
             System.out.println("DatabaseDriver::Executed update:\n"
                     + "UPDATE " + table + " SET " + values + " WHERE " + primaryKey + " = \"" + primaryKeyValue + "\" \nSUCCES!");
             return new FunctionResult(0, null, null);
         } catch (Exception e) {
             // do logging
             System.err.println("DatabaseDriver::Update command: \n"
                     + "UPDATE " + table + " SET " + values + " WHERE " + primaryKey + " = \"" + primaryKeyValue + "\" \nFAILED:\n" + e.getMessage());
 
             return new FunctionResult(1, null, StringTools.capitalize(e.getMessage()));
         }
     }
 
     public int getInvoiceNumber(Date date) {
         Calendar time = Calendar.getInstance();
         time.setTime(date);
         int year = time.get(Calendar.YEAR);
         if (!InvoicesbyYear.containsKey(year)) {
             return year % 100 * 10000 + 1;
         }
         ArrayList<Invoice> invoicesofyear = InvoicesbyYear.get(year);
         int lastindex = invoicesofyear.size() - 1;
         return invoicesofyear.get(lastindex).getNumber() + 1;
     }
 
     public FunctionResult deleteInvoice(Invoice i) {
         try {
             statement.executeUpdate("DELETE FROM invoices WHERE id= \"" + i.getPrimaryKeyValue() + "\"");
            System.out.println("DatabaseDriver::Executed delete:\n"
                     + "DELETE FROM invoices WHERE id= \"" + i.getPrimaryKeyValue() + "\"\nSUCCES!");
 
             invoicesById.remove(i.getPrimaryKeyValue());
             invoicesByNumber.remove(i.getNumber());
 
             return new FunctionResult(0, null, null);
         } catch (Exception e) {
             // do logging
             System.err.println("DatabaseDriver::Delete command: \n"
                     + "DELETE FROM invoices WHERE id= \"" + i.getPrimaryKeyValue() + "\"\nFAILED:\n" + e.getMessage());
 
             return new FunctionResult(1, null, StringTools.capitalize(e.getMessage()));
         }
     }
 
     /**
      * Tries to close the active database connection.
      *
      * @throws SQLException when the connection can't be closed.
      */
     public void shutdown() throws SQLException {
         connection.close();
     }
 }
