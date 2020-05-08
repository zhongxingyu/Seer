 
 package budgetforce.model;
 
 // <editor-fold defaultstate="collapsed" desc="imports">
 import budgetforce.model.Budget;
 import budgetforce.model.Income;
 import budgetforce.model.Login;
 import budgetforce.model.Outgoing;
 import budgetforce.model.OutgoingHasTax;
 import budgetforce.model.Tax;
 
 import org.joda.time.Period;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.util.ArrayList;
 import java.sql.PreparedStatement;
 import java.sql.Types;  //for setting some columns null, if its necessary
 import java.sql.Timestamp;
 import javassist.bytecode.analysis.Type;
 // </editor-fold>
 
 /**
  * @author David König
  */
 /*
  * @sources
  * http://ohdevon.wordpress.com/2011/09/19/postgresql-to-netbeans-2/
  * http://www.postgresql.org/docs/9.1/static/
  */
 
 
 public class DatabaseManager {
        
     
     // <editor-fold defaultstate="collapsed" desc="constructor">
     private DatabaseManager()
     {
         this.establishConnection();
     }
     // </editor-fold>
    
     
     // <editor-fold defaultstate="collapsed" desc="destructor">
     //close connection when singleton will be destroyed
     @Override
     protected void finalize()
     {
         this.closeConnection();
     }
     // </editor-fold>
     
     
     // <editor-fold defaultstate="collapsed" desc="singleton">
     public static DatabaseManager getDatabaseManager()
     {
         if (s_SingletonManager == null)
         {
             s_SingletonManager = new DatabaseManager();
         }
         
         return s_SingletonManager;
     }
     // </editor-fold>
     
     
     // <editor-fold defaultstate="collapsed" desc="connection">
     private void establishConnection()
     {
         if (connection != null)
             return;
       
         try
         {
            Class.forName(s_Driver);
           
            connection = DriverManager.getConnection(s_Url, s_DatabaseUser, s_DatabasePw);
            
            if (connection != null) {
                System.out.println("Connected to the database");
            }
         } 
         catch(Exception e)
         {
             System.out.println("Problem when connecting to the database");
         }
     }
     
     
     private void closeConnection()
     {
         if (connection != null)
         {
             try
             {
                 connection.close();
             }
             catch(Exception e)
             {
                 System.out.println("Problem when closing the connection to the database");
             }
         }
     }
     // </editor-fold>
     
     
     // <editor-fold defaultstate="collapsed" desc="address">
     public Address getAddressByID(int _id)
     {  
         ResultSet rs = null;
         
         Address address = new Address();
         
         try
         {
             PreparedStatement st = connection.prepareStatement("SELECT * FROM address WHERE id = ?");
             st.setInt(1, _id);
             rs = st.executeQuery();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem when getting an address by id from database!");
         }
         
         try 
         {
             while(rs.next())
             {  
                 address.setId(rs.getInt("id"));
                 address.setStreetNmbr(rs.getString("streetNmbr"));
                 address.setCity(rs.getString("city"));
                 address.setZipCode(rs.getString("zipcode"));
                 address.setCountry(rs.getString("country"));
                 address.setPersondId(rs.getInt("personID"));
                 address.setAddressAddition(rs.getString("addressAddition"));
                 
                 String type = rs.getString("type");
                 type = type.toUpperCase();
                 
                 if(type.equals(address.getType().ADDITIONAL))
                 {
                     address.setType(Address.EAddressType.ADDITIONAL);
                 }
                 else if(type.equals(address.getType().BILLING))
                 {
                     address.setType(Address.EAddressType.BILLING);
                 }
                 else if(type.equals(address.getType().COMPANY))
                 {
                     address.setType(Address.EAddressType.COMPANY);
                 }
                 else if(type.equals(address.getType().PRIVATE))
                 {
                     address.setType(Address.EAddressType.PRIVATE);
                 }
             }
             rs.close();
         } 
         
         catch(Exception e)
         {
             System.out.println("Problem when mapping an address by id from database into an instance, result could be null!");
         }
         
         return address;
     }
     
     
     public ArrayList<Address> getAddressByPersonID(int _personID)
     {     
         ArrayList addressArray = new ArrayList<Address>();
         ResultSet rs = null;
         
         try
         {
             PreparedStatement st = connection.prepareStatement("SELECT * FROM address WHERE \"personID\" = ?");
             st.setInt(1, _personID);
             rs = st.executeQuery();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem in selecting an address from database by person id");
         }
         
         try 
         {
             while(rs.next())
             {  
                 Address address = new Address();
                 
                 address.setId(rs.getInt("id"));
                 address.setStreetNmbr(rs.getString("streetNmbr"));
                 address.setCity(rs.getString("city"));
                 address.setZipCode(rs.getString("zipcode"));
                 address.setCountry(rs.getString("country"));
                 address.setPersondId(rs.getInt("personID"));
                 address.setAddressAddition(rs.getString("addressAddition"));
                 
                 String type = rs.getString("type");
                 type.toUpperCase();
                 
                 if(type.equals(address.getType().ADDITIONAL))
                 {
                     address.setType(Address.EAddressType.ADDITIONAL);
                 }
                 else if(type.equals(address.getType().BILLING))
                 {
                     address.setType(Address.EAddressType.BILLING);
                 }
                 else if(type.equals(address.getType().COMPANY))
                 {
                     address.setType(Address.EAddressType.COMPANY);
                 }
                 else if(type.equals(address.getType().PRIVATE))
                 {
                     address.setType(Address.EAddressType.PRIVATE);
                 }
                 
                 addressArray.add(address);
             }
             rs.close();
         } 
         
         catch(Exception e)
         {
             System.out.println("Problem when mapping addresses selected by person id from database to a new instance, result could be null");
         }
         
         return addressArray;
     }
     
     
     public int insertAddress(Address _address)
     {        
         ResultSet rs = null;
         int id = 0;
         
         try
         {
             PreparedStatement st = connection.prepareStatement("INSERT INTO address(\"streetNmbr\","
                     + " city, zipcode, country, type, \"personID\", \"addressAddition\")"
                     + " VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
             st.setString(1, _address.getStreetNmbr());
             st.setString(2, _address.getCity());
             st.setString(3, _address.getZipCode());
             st.setString(4, _address.getCountry());
             st.setString(8, _address.getType().toString().toLowerCase()); 
             st.setInt(9, _address.getPersondId());
             st.setString(10, _address.getAddressAddition());
             
             st.executeUpdate();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem in inserting an address into database.");
         }
         
          //get ID
         try
         {
             PreparedStatement st = connection.prepareStatement("SELECT MAX(id) FROM address");
            
             rs = st.executeQuery();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem when selecting id from inserted address");
         }
         
         try 
         {
             while(rs.next())
             {  
                 id = rs.getInt(1);
             }
             rs.close();
         } 
         
         catch(Exception e)
         {
             System.out.println("Problem when mapping id from inserted address into int value");
         }
         
         return id;
     }
     
     
     public boolean updateAddress(Address _address)
     {       
         try
         {
             PreparedStatement st = connection.prepareStatement("UPDATE address SET"
                     + " \"streetNmbr\" = ?, city = ?, zipcode = ?, country = ?,"
                     + " type = ?, \"personID\" = ?, \"addressAddition\" = ?)"
                     + " WHERE id = ? ");
             
             st.setString(1, _address.getStreetNmbr());
             st.setString(2, _address.getCity());
             st.setString(3, _address.getZipCode());
             st.setString(4, _address.getCountry());
             st.setString(8, _address.getType().toString().toLowerCase()); 
             st.setInt(9, _address.getPersondId());
             st.setString(10, _address.getAddressAddition());
             st.setInt(11, _address.getId());
             
             st.executeUpdate();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem when updating address to the database.");
             return false;
         }
 
         return true;
     }
     
     
     public boolean deleteAddress(Address _address)
     {
         try
         {
             PreparedStatement st = connection.prepareStatement("DELETE FROM address WHERE id = ?");
             
             st.setInt(1, _address.getId());
             st.executeUpdate();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem when deleting an address from database, maybe id not found");
             return false;
         }
  
         return true;
     }
     // </editor-fold>
     
     
     // <editor-fold defaultstate="collapsed" desc="budget">
     public Budget getBudgetByID(int _id)
     {  
         Budget budget = new Budget();
         ResultSet rs = null;
         
         try
         {
             PreparedStatement st = connection.prepareStatement("SELECT * FROM budget WHERE id = ?");
             
             st.setInt(1, _id);
             rs = st.executeQuery();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem when selecting a budget by id from database");
         }
         
         try 
         {
             while(rs.next())
             {  
                 budget.setBudgetId(rs.getInt("id"));
                 budget.setCurrency(rs.getString("currency"));
                 budget.setName(rs.getString("name"));
                 budget.setPersonId(rs.getInt("personID"));
                 budget.setProjectId(rs.getInt("projectID"));
                 budget.setBudgetId(rs.getInt("budgetID"));
             }
             rs.close();
         } 
         
         catch(Exception e)
         {
             System.out.println("Problem mapping budget selected by id from database to instance, result could be null");
         }
          
         return budget;
     }
          
     
     public ArrayList<Budget> getBudgetByPersonID(int _personID)
     {
         ArrayList arrayBudget = new ArrayList<Budget>();
         ResultSet rs = null;
         
         try
         {
             PreparedStatement st = connection.prepareStatement("SELECT * FROM budget WHERE \"personID = ?");
             
             st.setInt(1, _personID);
             rs = st.executeQuery();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem when selecting budgets from a person by id from database");
         }
         
         try 
         {
             while(rs.next())
             {  
                 Budget budget = new Budget();
                 
                 budget.setBudgetId(rs.getInt("id"));
                 budget.setCurrency(rs.getString("currency"));
                 budget.setName(rs.getString("name"));
                 budget.setPersonId(rs.getInt("personID"));
                 budget.setProjectId(rs.getInt("projectID"));
                 budget.setBudgetId(rs.getInt("budgetID"));
                 
                 arrayBudget.add(budget);
             }
             rs.close();
         } 
         
         catch(Exception e)
         {
             System.out.println("Problem when mapping budget by person id from database, result could be null.");
         }
         
         
         return arrayBudget;
     }
     
         
     public ArrayList<Budget> getBudgetByProjectID(int _projectID)
     {
         ArrayList arrayBudget = new ArrayList<Budget>();
         ResultSet rs = null;
         
         try
         {
             PreparedStatement st = connection.prepareStatement("SELECT * FROM budget WHERE \"projectID = ?");
             
             st.setInt(1, _projectID);
             rs = st.executeQuery();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem selecting budgets by project id from database");
         }
         
         try 
         {
             while(rs.next())
             {  
                 Budget budget = new Budget();
                 
                 budget.setBudgetId(rs.getInt("id"));
                 budget.setCurrency(rs.getString("currency"));
                 budget.setName(rs.getString("name"));
                 budget.setPersonId(rs.getInt("personID"));
                 budget.setProjectId(rs.getInt("projectID"));
                 budget.setBudgetId(rs.getInt("budgetID"));
                 
                 arrayBudget.add(budget);
             }
             rs.close();
         } 
         
         catch(Exception e)
         {
             System.out.println("Problem when mapping budget by project id to instance, result could be null");
         }
         
         
         return arrayBudget;
     }
     
     
     public ArrayList<Budget> getBudgetByBudgetID(int _budgetID)
     {
         ArrayList arrayBudget = new ArrayList<Budget>();
         ResultSet rs = null;
         
         try
         {
             PreparedStatement st = connection.prepareStatement("SELECT * FROM budget WHERE \"budgetID = ?");
             
             st.setInt(1, _budgetID);
             rs = st.executeQuery();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem selecting subbudget from database");
         }
         
         try 
         {
             while(rs.next())
             {  
                 Budget budget = new Budget();
                 
                 budget.setBudgetId(rs.getInt("id"));
                 budget.setCurrency(rs.getString("currency"));
                 budget.setName(rs.getString("name"));
                 budget.setPersonId(rs.getInt("personID"));
                 budget.setProjectId(rs.getInt("projectID"));
                 budget.setBudgetId(rs.getInt("budgetID"));
                 
                 arrayBudget.add(budget);
             }
             rs.close();
         } 
         
         catch(Exception e)
         {
             System.out.println("Problem when mapping subbudget to database, result could be null");
         }
         
         return arrayBudget;
     }
     
     
     public int insertBudget(Budget _budget)
     {        
         ResultSet rs    = null;
         int id          = 0;
         
         try
         {
             PreparedStatement st = connection.prepareStatement("INSERT INTO budget(name,"
                     + " currency, \"personID\", \"projectID\", \"budgetID\")"
                     + " VALUES(?, ?, ?, ?, ?)");
             
             st.setString(1, _budget.getName());
             st.setString(2, _budget.getCurrency());
             st.setInt(3, _budget.getPersonId());
             st.setInt(4, _budget.getProjectId());
             st.setInt(5, _budget.getBudgetId());
             
             st.executeUpdate();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem when inserting budget into database");
         }
         
         //get ID
         try
         {
             PreparedStatement st = connection.prepareStatement("SELECT MAX(id) FROM budget");
            
             rs = st.executeQuery();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem when selecting inserted budget id from database ");
         }
         
         try 
         {
             while(rs.next())
             {  
                 id = rs.getInt(1);
             }
             rs.close();
         } 
         
         catch(Exception e)
         {
             System.out.println("Problem when mapping inserted budget id");
         }
         
         return id;
     }
     
     
     public boolean updateBudget(Budget _budget)
     { 
         try
         {
             PreparedStatement st = connection.prepareStatement("UPDATE budget SET"
                     + " name = ?, currency = ?, \"personID\" = ?, \"projectID\" = ?,"
                     + " \"budgetID\" = ? "
                     + " WHERE id = ?");
             
             st.setString(1, _budget.getName());
             st.setString(2, _budget.getCurrency());
             st.setInt(3, _budget.getPersonId());
             st.setInt(4, _budget.getProjectId());
             st.setInt(5, _budget.getBudgetId());
             st.setInt(6, _budget.getId());
             
             st.executeUpdate();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem when updating budget to database");
             return false;
         }
         
         return true;
     }
     
     
     public boolean deleteBudget(int _id)
     {
         try
         {
             PreparedStatement st = connection.prepareStatement("DELETE FROM budget WHERE id = ?");
             
             st.setInt(1, _id);
             st.executeUpdate();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem when deleting budget from database");
             return false;
         }
         
         return true;
     }
     // </editor-fold>
     
     
     // <editor-fold defaultstate="collapsed" desc="category">
     public Category getCategoryByID(int _id)
     {
         ResultSet rs = null;
         
         Category category = new Category();
         
         try
         {
             PreparedStatement st = connection.prepareStatement("SELECT * FROM category WHERE id = ?");
             st.setInt(1, _id);
             rs = st.executeQuery();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem when selecting category by id from database");
         }
         
         try {
             while(rs.next())
             {  
                 category.setM_Id(rs.getInt("id"));
                 category.setM_Name(rs.getString("name"));
             }
         } 
         
         catch(Exception e)
         {
             System.out.println("Problem when mapping category selected by id, to an instance");
         }
         
         return category;
     }
     
     
     public int insertCategory(Category _category)
     {
         ResultSet rs    = null;
         int             id = 0;
         
         try
         {
             PreparedStatement st = connection.prepareStatement("INSERT INTO category(name)"
                     + " VALUES(?)");
             
             st.setString(1, _category.getM_Name());
             
             st.executeUpdate();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem when inserting category into database");
         }
         
         //get ID
         try
         {
             PreparedStatement st = connection.prepareStatement("SELECT MAX(id) FROM category");
            
             rs = st.executeQuery();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem when selecting id from inserted category");
         }
         
         try 
         {
             while(rs.next())
             {  
                 id = rs.getInt(1);
             }
             rs.close();
         } 
         
         catch(Exception e)
         {
             System.out.println("Problem when mapping inserted category id");
         }
         
         
         return id;
     }
     
     
     public boolean updateCategory(Category _category)
     {  
         try
         {
             PreparedStatement st = connection.prepareStatement("UPDATE budget SET"
                     + " name = ? WHERE id = ?");
             
             st.setString(1, _category.getM_Name());
             st.setInt(2, _category.getM_Id());
             
             st.executeUpdate();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem when updating category to database");
             return false;
         }
         
         return true;
     }
      
     
     public boolean deleteCategory(int _id)
     {
         try
         {
             PreparedStatement st = connection.prepareStatement("DELETE FROM category WHERE id = ?");
             
             st.setInt(1, _id);
             st.executeUpdate();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem deleting category from database");
             return false;
         }
         
         return true;
     }
     // </editor-fold>
     
     
     // <editor-fold defaultstate="collapsed" desc="income">
     public Income getIncomeByID(int _id)
     {
         ResultSet rs = null;
         
         Income income = new Income();
         
         try
         {
             PreparedStatement st = connection.prepareStatement("SELECT * FROM income WHERE id = ?");
             st.setInt(1, _id);
             rs = st.executeQuery();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem selecting income by id from database");
         }
         
         try 
         {
             while(rs.next())
             {  
                 Object o = new Object();
                 
                 income.setId(rs.getInt("id"));
                 income.setName(rs.getString("name"));
                 income.setAmount(rs.getFloat("amount"));
                 income.setStart(rs.getTimestamp("start"));
                 income.setEnd(rs.getTimestamp("ende"));
                 income.setTimestamp(rs.getTimestamp("timestamp"));
                 income.setPersonID(rs.getInt("personID"));
                 income.setIncomeID(rs.getInt("incomeID"));
                 
                 //first map period from database to an Object and then map this Object to an joda Period instance
                 o = rs.getObject("period");
                 
                 try
                 { 
                     Period period = new Period(o);
                     //Period period = (Period) o; 
                     income.setPeriod(period);
                 }
                 
                 catch(Exception e)
                 {
                     System.out.println("Casting Object to Period failed");
                 }
             }
         } 
         
         catch(Exception e)
         {
             System.out.println("Problem when mapping income by id to instance, result could be null");
         }
 
         return income;
     }
     
     
     public ArrayList<Income> getIncomeByPersonID(int _personID)
     {
         ResultSet rs = null;
         
         ArrayList incomeArray = new ArrayList<Income>();
         
         try
         {
             PreparedStatement st = connection.prepareStatement("SELECT * FROM income WHERE \"personID\" =  ?");
             st.setInt(1, _personID);
             rs = st.executeQuery();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem when selecting income by person id from database");
         }
         
         try {
             while(rs.next())
             {         
                 Income tmpIncome       = new Income();
                 Object o               = new Object();
                 
                 tmpIncome.setId(rs.getInt("id"));
                 tmpIncome.setName(rs.getString("name"));
                 tmpIncome.setAmount(rs.getFloat("amount"));   
                 tmpIncome.setStart(rs.getTimestamp("start"));
                 tmpIncome.setEnd(rs.getTimestamp("ende"));
                 tmpIncome.setTimestamp(rs.getTimestamp("timestamp"));
                 tmpIncome.setPersonID(rs.getInt("personID"));
                 tmpIncome.setIncomeID(rs.getInt("incomeID"));
                 
                 //first map period from database to an Object and then map this Object to an joda Period instance
                 o = rs.getObject("period");
                 
                 try
                 { 
                     Period period = (Period) o; 
                     tmpIncome.setPeriod(period);
                 }
                 
                 catch(Exception e)
                 {
                     System.out.println("Casting Object to Period failed");
                 }
        
                 incomeArray.add(tmpIncome);       
             }  
         } 
         
         catch(Exception e)
         {
             System.out.println("Problem when mapping income by person id to instance, result could be null");
         }
 
         return incomeArray;
     }
      
     
     public ArrayList<Income> getIncomeByIncomeID(int _incomeID)
     {
         ResultSet rs = null;
         
         ArrayList incomeArray = new ArrayList<Income>();
         
         try
         {
             PreparedStatement st = connection.prepareStatement("SELECT * FROM income WHERE \"incomeID\" =  ?");
             st.setInt(1, _incomeID);
             rs = st.executeQuery();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem when selecting subincome database");
         }
         
         try {
             while(rs.next())
             {         
                 Income tmpIncome       = new Income();
                 Object o               = new Object();
                 
                 tmpIncome.setId(rs.getInt("id"));
                 tmpIncome.setName(rs.getString("name"));
                 tmpIncome.setAmount(rs.getFloat("amount"));
                 tmpIncome.setStart(rs.getTimestamp("start"));
                 tmpIncome.setEnd(rs.getTimestamp("ende"));
                 tmpIncome.setTimestamp(rs.getTimestamp("timestamp"));
                 tmpIncome.setPersonID(rs.getInt("personID"));
                 tmpIncome.setIncomeID(rs.getInt("incomeID"));
                 
                 //first map period from database to an Object and then map this Object to an joda Period instance
                 o = rs.getObject("period");
                 
                 try
                 { 
                     Period period = (Period) o; 
                     tmpIncome.setPeriod(period);
                 }
                 
                 catch(Exception e)
                 {
                     System.out.println("Casting Object to Period failed");
                 }
        
                 incomeArray.add(tmpIncome);       
             }  
         } 
         
         catch(Exception e)
         {
             System.out.println("Problem when mapping subincome, result could be null");
         }
  
         return incomeArray;
     }
     
     
     public int insertIncome(Income _income)
     {   
         ResultSet rs    = null;
         int id          = 0;
         
         try
         {
             PreparedStatement st = connection.prepareStatement("INSERT INTO income(name,"
                     + " amount, timestamp, period, start, ende, \"personID\", \"incomeID\")"
                     + " VALUES(?, ?, ?, ?, ?, ?, ?, ?)");
             
             st.setString(1, _income.getName());
             st.setFloat(2, _income.getAmount());
             st.setTimestamp(3, _income.getTimestamp());
             
             //Object o = (String) _income.getPeriod();
             //st.setObject(4, o, ); 
             
             st.setTimestamp(5, _income.getStart());
             st.setTimestamp(6, _income.getEnd());
             st.setInt(7, _income.getPersonID());
             //st.setInt(8, _income.getIncomeID());
             
             //if there isnt set any subincome, set the incomeID null, because if it isnt set it will be 0 and 0 != null
             if (_income.getIncomeID() == 0)     st.setNull(8, Types.BIGINT);  //java sql types, because you have to specify the sql type if you want to set the column null
             else                                st.setInt(8, _income.getIncomeID());
             
             st.executeUpdate();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem when inserting income into database");
         }
         
         //get ID
         try
         {
             PreparedStatement st = connection.prepareStatement("SELECT MAX(id) FROM income");
            
             rs = st.executeQuery();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem selecting income from inserted id from database");
         }
         
         try 
         {
             while(rs.next())
             {  
                 id = rs.getInt(1);
             }
             rs.close();
         } 
         
         catch(Exception e)
         {
             System.out.println("Problem when mapping inserted income id");
         }
 
         return id;
     }
     
     
     public boolean updateIncome(Income _income)
     {
         try
         {
             PreparedStatement st = connection.prepareStatement("UPDATE income"
                     + " SET name = ?, amount = ?, timestamp = ?, period = ?, start = ?,"
                     + " ende = ?, \"personID\" = ?, \"incomeID\" = ?  WHERE id = ?");
            
             st.setString(1, _income.getName());
             st.setFloat(2, _income.getAmount());
             st.setTimestamp(3, _income.getTimestamp());
             st.setObject(4, _income.getPeriod());
             st.setTimestamp(5, _income.getStart());
             st.setTimestamp(6, _income.getEnd());
             st.setInt(7, _income.getPersonID());
             st.setInt(8, _income.getIncomeID());
             st.setInt(9, _income.getId());
            
             st.executeUpdate();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem when updating income to database");
             return false;
         }
         
         return true;
     }
     
     
     public boolean deleteIncome(int _id)
     {
         try
         {
             PreparedStatement st = connection.prepareStatement("DELETE FROM income"
                     + " WHERE id = ?");
             
             st.setInt(1, _id);
             
             st.executeUpdate();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem deleting income from database");
             return false;
         }
 
         return true;
     }
     // </editor-fold>
        
     
     // <editor-fold defaultstate="collapsed" desc="login">
     public Login getLoginByUsername(String _username)
     {
         Login login = new Login();
         ResultSet rs = null;
         
         try
         {
             PreparedStatement st = connection.prepareStatement("SELECT * FROM login WHERE username = ?");
             
             st.setString(1, _username);
             rs = st.executeQuery();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem when selecting login by username from database");
         }
         
         try 
         {
             while(rs.next())
             {  
                 login.setUsername(rs.getString("username"));
                 login.setPassword(rs.getString("password"));
                 login.setSecurityQuestion(rs.getString("securityQuestion"));
                 login.setPersondId(rs.getInt("personID"));
                 
                 String type = rs.getString("type");
                 type.toUpperCase();
                 
                 if(type.equals(Login.ELoginType.COMPANY))
                 {
                     login.setType(Login.ELoginType.COMPANY);
                 }
                 else if(type.equals(Login.ELoginType.PRIVATE))
                 {
                     login.setType(Login.ELoginType.PRIVATE);
                 }
             }
             rs.close();
         } 
         
         catch(Exception e)
         {
             System.out.println("Problem mapping login selected by username, result could be null");
         }
 
         return login;
     }
     
     
     public Login getLoginByPersonID(int _personID)
     { 
         Login login = new Login();
         ResultSet rs = null;
         
         try
         {
             PreparedStatement st = connection.prepareStatement("SELECT * FROM login WHERE \"personID\" = ?");
             
             st.setInt(1, _personID);
             rs = st.executeQuery();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem selecting login by person id from database");
         }
         
         try 
         {
             while(rs.next())
             {  
                 login.setUsername(rs.getString("username"));
                 login.setPassword(rs.getString("password"));
                 login.setSecurityQuestion(rs.getString("securityQuestion"));
                 login.setPersondId(rs.getInt("personID"));
                 
                 String type = rs.getString("type");
                 type.toUpperCase();
                 
                 if(type.equals(Login.ELoginType.COMPANY))
                 {
                     login.setType(Login.ELoginType.COMPANY);
                 }
                 else if(type.equals(Login.ELoginType.PRIVATE))
                 {
                     login.setType(Login.ELoginType.PRIVATE);
                 }
             }
             rs.close();
         } 
         
         catch(Exception e)
         {
             System.out.println("Problem when mapping login selected by person id, result could be null");
         }
 
         return login;
     }
        
     
     public int insertLogin(Login _login)
     {
         ResultSet rs    = null;
         int id          = 0;
         
         try
         {
             PreparedStatement st = connection.prepareStatement("INSERT INTO login(username,"
                     + " password, \"securityQuestion\", \"personID\", type)"
                     + " VALUES(?, ?, ?, ?, ?)");
             
             st.setString(1, _login.getUsername());
             st.setString(2, _login.getPassword());
             st.setString(3, _login.getSecurityQuestion());
             st.setInt(4, _login.getPersondId());
             st.setString(5, _login.getType().toString().toLowerCase());
            
             st.executeUpdate();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem inserting login into database");
         }
         
         //get ID
         try
         {
             PreparedStatement st = connection.prepareStatement("SELECT MAX(id) FROM login");
            
             rs = st.executeQuery();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem when selecting id from inserted login from database");
         }
         
         try 
         {
             while(rs.next())
             {  
                 id = rs.getInt(1);
             }
             rs.close();
         } 
         
         catch(Exception e)
         {
             System.out.println("Problem when mapping id from inserted login");
         }
 
         return id;
     }
     
     
     public boolean updateLogin(Login _login) //username can not be changed 
     {
         try
         {
             PreparedStatement st = connection.prepareStatement("UPDATE login"
                     + " SET password = ?, \"securityQuestion\" = ?, \"personID\" = ?, type = ?"
                     + " WHERE username = ?");
         
             st.setString(1, _login.getPassword());
             st.setString(2, _login.getSecurityQuestion());
             st.setInt(3, _login.getPersondId());
             st.setString(4, _login.getType().toString().toLowerCase());
             st.setString(5, _login.getUsername());
            
             st.executeUpdate();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem when updatin login to database");
             return false;
         }
 
         return true;
     }
       
     
     public boolean updateLoginPassword(String _username, String _password)
     {
         try
         {
             PreparedStatement st = connection.prepareStatement("UPDATE login"
                     + " SET password = ? WHERE username = ?");
             
             st.setString(1, _password);
             st.setString(2, _username);
            
             st.executeUpdate();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem updating login password to database");
             return false;
         }
 
         return true;
     }
      
     
     public boolean updateLoginSecurityQuestion(String _username, String _securityQuestion)
     {
         try
         {
             PreparedStatement st = connection.prepareStatement("UPDATE login"
                     + " SET \"securityQuestion\" = ? WHERE username = ?");
             
             st.setString(1, _securityQuestion);
             st.setString(2, _username);
             
             st.executeUpdate();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem when updating security question from login to database");
             return false;
         }
 
         return true;
     }
       
     
     public boolean deleteLogin(String _username)
     {
         try
         {
             PreparedStatement st = connection.prepareStatement("DELETE FROM login"
                     + " WHERE username = ?");
             
             st.setString(1, _username);
             
             st.executeUpdate();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem when deleting login from database");
             return false;
         }
 
         return true;
     }
     // </editor-fold>
     
     
     // <editor-fold defaultstate="collapsed" desc="outgoing">
     public Outgoing getOutgoingByID(int _id)
     {
         Outgoing outgoing = new Outgoing();
         ResultSet rs = null;
         
         try
         {
             PreparedStatement st = connection.prepareStatement("SELECT * FROM outgoing WHERE id = ?");
             
             st.setInt(1, _id);
             rs = st.executeQuery();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem when selecting outgoing by id from database");
         }
         
         try 
         {
             while(rs.next())
             {  
                 outgoing.setId(rs.getInt("id"));
                 outgoing.setAmount(rs.getFloat("amount"));
                 outgoing.setPeriodId(rs.getInt("period_id"));
                 outgoing.setStart(rs.getTimestamp("start"));
                 outgoing.setEnd(rs.getTimestamp("ende"));
                 outgoing.setTimeStamp(rs.getTimestamp("timestamp"));
                 outgoing.setBudgetId(rs.getInt("budgetID"));
                 outgoing.setCategoryId(rs.getInt("categoryID"));
             }
             rs.close();
         } 
         
         catch(Exception e)
         {
             System.out.println("Problem mapping outgoing selected by id, result could be null");
         }
 
         return outgoing;
     }
     
     
     public ArrayList<Outgoing> getOutgoingByBudgetID(int _budgetID)
     {
         ArrayList arrayOutgoing = new ArrayList<Outgoing>();
         
         ResultSet rs = null;
         
         try
         {
             PreparedStatement st = connection.prepareStatement("SELECT * FROM outgoing WHERE id = ?");
             
             st.setInt(1, _budgetID);
             rs = st.executeQuery();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem selecting outgoings by budget from database");
         }
         
         try 
         {
             while(rs.next())
             {  
                 Outgoing outgoing = new Outgoing();
                 
                 outgoing.setId(rs.getInt("id"));
                 outgoing.setAmount(rs.getFloat("amount"));
                 outgoing.setPeriodId(rs.getInt("period_id"));
                 outgoing.setStart(rs.getTimestamp("start"));
                 outgoing.setEnd(rs.getTimestamp("ende"));
                 outgoing.setTimeStamp(rs.getTimestamp("timestamp"));
                 outgoing.setBudgetId(rs.getInt("budgetID"));
                 outgoing.setCategoryId(rs.getInt("categoryID"));
                 
                 arrayOutgoing.add(outgoing);
             }
             rs.close();
         } 
         
         catch(Exception e)
         {
             System.out.println("Problem mapping outgoings selected by a budget, result could be null");
         }
         
         return arrayOutgoing;
     }
     
     
     public ArrayList<Outgoing> getOutgoingByCategoryID(int _categoryID)
     {
         ArrayList arrayOutgoing = new ArrayList<Outgoing>();
         
         ResultSet rs = null;
         
         try
         {
             PreparedStatement st = connection.prepareStatement("SELECT * FROM outgoing WHERE id = ?");
             
             st.setInt(1, _categoryID);
             rs = st.executeQuery();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem selecting outgoings by category from database");
         }
         
         try 
         {
             while(rs.next())
             {  
                 Outgoing outgoing = new Outgoing();
                 
                 outgoing.setId(rs.getInt("id"));
                 outgoing.setAmount(rs.getFloat("amount"));
                 outgoing.setPeriodId(rs.getInt("period_id"));
                 outgoing.setStart(rs.getTimestamp("start"));
                 outgoing.setEnd(rs.getTimestamp("ende"));
                 outgoing.setTimeStamp(rs.getTimestamp("timestamp"));
                 outgoing.setBudgetId(rs.getInt("budgetID"));
                 outgoing.setCategoryId(rs.getInt("categoryID"));
                 
                 arrayOutgoing.add(outgoing);
             }
             rs.close();
         } 
         
         catch(Exception e)
         {
             System.out.println("Problem mapping outgoings selected by category, result could be null");
         }
 
         return arrayOutgoing;
     }
     
     
     public int insertOutgoing(Outgoing _outgoing)
     {
         ResultSet rs    = null;
         int id          = 0;
         
         try
         {
             PreparedStatement st = connection.prepareStatement("INSERT INTO outgoing(amount,"
                     + " period_id, start, ende, timestamp, \"budgetID\", \"categoryID\") "
                     + " VALUES(?, ?, ?, ?, ?, ?)");
            
            st.setFloat (1, _outgoing.getAmount());
            st.setInt   (2, _outgoing.getPeriodId());
            st.setDate  (3, new java.sql.Date(_outgoing.getStart().getTime()));
            st.setDate  (4, new java.sql.Date(_outgoing.getEnd().getTime()));
            st.setDate  (5, new java.sql.Date(_outgoing.getTimeStamp().getTime()));
            st.setInt   (6, _outgoing.getBudgetId());
            st.setInt   (7, _outgoing.getCategoryId());
            
             st.executeUpdate();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem when inserting outgoing into database");
         }
         
         //get ID
         try
         {
             PreparedStatement st = connection.prepareStatement("SELECT MAX(id) FROM outgoing");
            
             rs = st.executeQuery();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem selecting id from inserted outgoing from database");
         }
         
         try 
         {
             while(rs.next())
             {  
                 id = rs.getInt(1);
             }
             rs.close();
         } 
         
         catch(Exception e)
         {
             System.out.println("Problem mapping id from inserted outoging");
         }
 
         return id;
     }
     
     
     public boolean updateOutgoing(Outgoing _outgoing)
     {
         try
         {
             PreparedStatement st = connection.prepareStatement("UPDATE outgoing SET amount = ?, "
                     + " period_id = ?, start = ?, ende = ?, timestamp = ?, \"budgetID\" = ?,"
                     + " \"categoryID\" = ? WHERE id = ?");
            
             st.setFloat(1, _outgoing.getAmount());
             st.setInt(2, _outgoing.getPeriodId());
             st.setTimestamp(3, new java.sql.Timestamp(_outgoing.getStart().getTime()));
             st.setTimestamp(4, new java.sql.Timestamp(_outgoing.getEnd().getTime()));
             st.setTimestamp(5, new java.sql.Timestamp(_outgoing.getTimeStamp().getTime()));
             st.setInt(6, _outgoing.getBudgetId());
             st.setInt(7, _outgoing.getCategoryId());
             st.setInt(8, _outgoing.getId());
            
             st.executeUpdate();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem updating outgoing to database");
             return false;
         }
 
         return true;
     }
     
     
     public boolean deleteOutgoing(int _id)
     {
         try
         {
             PreparedStatement st = connection.prepareStatement("DELETE FROM outgoing"
                     + " WHERE id = ?");
             
             st.setInt(1, _id);
             
             st.executeUpdate();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem deleting outgoing from database");
             return false;
         }
         
         
         return true;
     }
     // </editor-fold>
     
     
     // <editor-fold defaultstate="collapsed" desc="outgoingHasTax">
     public OutgoingHasTax getOutgoingHasTaxByID(int _outgoingID, int _taxID)
     {
         OutgoingHasTax oht = new OutgoingHasTax();
         ResultSet rs = null;
         
         try
         {
             PreparedStatement st = connection.prepareStatement("SELECT * FROM outgoing_has_tax WHERE \"outgoingID\" = ?"
                     + " and \"taxID\" = ? ");
             
             st.setInt(1, _outgoingID);
             st.setInt(2, _taxID);
             rs = st.executeQuery();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem selecting outgoing has tax by id from database");
         }
         
         try 
         {
             while(rs.next())
             {  
                 oht.setOutgoingId(rs.getInt("outoginID"));
                 oht.setTaxId(rs.getInt("taxID"));
                 oht.setWriteOff(rs.getBoolean("write-off"));
             }
             rs.close();
         } 
         
         catch(Exception e)
         {
             System.out.println("Problem when mapping outgoing has tax selected by id to an instance, result could be null");
         }
 
         return oht;
     }
      
     
     public ArrayList<OutgoingHasTax> getOutgoingHasTaxByOutgoingID(int _outgoingID)
     {
         ArrayList arrayOht = new ArrayList<OutgoingHasTax>();
         
         ResultSet rs = null;
         
         try
         {
             PreparedStatement st = connection.prepareStatement("SELECT * FROM outgoing_has_tax WHERE \"outgoingID\" = ?");
             
             st.setInt(1, _outgoingID);
             rs = st.executeQuery();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem when selecting ougoing has tax by outgoing id from database");
         }
         
         try 
         {
             while(rs.next())
             {  
                 OutgoingHasTax oht = new OutgoingHasTax();
                 
                 oht.setOutgoingId(rs.getInt("outoginID"));
                 oht.setTaxId(rs.getInt("taxID"));
                 oht.setWriteOff(rs.getBoolean("write-off"));
                 
                 arrayOht.add(oht);
             }
             rs.close();
         } 
         
         catch(Exception e)
         {
             System.out.println("Problem when mapping ougoing has tax selected by outgoing id, result could be null");
         }
 
         return arrayOht;
     }
        
     
     public int insertOutgoingHasTaxByID(OutgoingHasTax _outgoingHasTax)
     {
         ResultSet rs    = null;
         int id          = 0;
         
         try
         {
             PreparedStatement st = connection.prepareStatement("INSERT INTO outgoing_has_tax"
                     + " (\"outgoingID\", \"taxID\", write-off) "
                     + " VALUES(?, ?, ?)");
            
             st.setInt(1, _outgoingHasTax.getOutgoingId());
             st.setInt(2, _outgoingHasTax.getTaxId());
             st.setBoolean(3, _outgoingHasTax.isWriteOff());
            
             st.executeUpdate();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem when inserting outgoing has tax into database");
         }
         
         //get ID
         try
         {
             PreparedStatement st = connection.prepareStatement("SELECT MAX(id) FROM outgoing_has_tax");
            
             rs = st.executeQuery();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem selecting id from inserted outgoing has tax from database");
         }
         
         try 
         {
             while(rs.next())
             {  
                 id = rs.getInt(1);
             }
             rs.close();
         } 
         
         catch(Exception e)
         {
             System.out.println("Problem when mapping id from inserted outgoing has tax");
         }
 
         return id;
     }
     
     
     public boolean updateOutgoingHasTax(OutgoingHasTax _outgoingHasTax)
     {
         try
         {
             PreparedStatement st = connection.prepareStatement("UPDATE outgoing_has_tax "
                     + " SET write-off = ? WHERE \"outgoingID\" = ? AND \"taxID\" = ?");
            
             st.setBoolean(1, _outgoingHasTax.isWriteOff());
             st.setInt(2, _outgoingHasTax.getOutgoingId());
             st.setInt(3, _outgoingHasTax.getTaxId());
             
            
             st.executeUpdate();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem when updating outgoing has tax to database");
             return false;
         }
 
         return true;
     }
     
     
     public boolean deleteOutgoingHasTaxByID(int _outgoingID, int _taxID)
     {
         try
         {
             PreparedStatement st = connection.prepareStatement("DELETE FROM person"
                     + " WHERE \"outgoingID\" = ? AND \"taxID\" = ?");
             
             st.setInt(2, _outgoingID);
             st.setInt(3, _taxID);
             
             st.executeUpdate();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem when deleting outgoing has tax from database");
             return false;
         }
 
         return true;
     }
     // </editor-fold>
     
     
     // <editor-fold defaultstate="collapsed" desc="person">
     public Person getPersonByID(int _id)
     {
         Person person = new Person();
         ResultSet rs = null;
         
         try
         {
             PreparedStatement st = connection.prepareStatement("SELECT * FROM person WHERE id = ?");
             
             st.setInt(1, _id);
             rs = st.executeQuery();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem selecting person by id from database");
         }
         
         try 
         {
             while(rs.next())
             {  
                 person.setId(rs.getInt("id"));
                 person.setFirstName(rs.getString("firstName"));
                 person.setLastName(rs.getString("lastName"));
                 person.setEmail(rs.getString("email"));
                 person.setPhone1(rs.getString("phone1"));
                 person.setPhone2(rs.getString("phone2"));
             }
             rs.close();
         } 
         
         catch(Exception e)
         {
             System.out.println("Problem mapping selected person by id, result could be null");
         }
 
         return person;
     }
       
     
     public int insertPerson(Person _person)     //returns the id of the inserted person
     {       
         ResultSet rs = null;
         int id = 0;
         
         try
         {
             PreparedStatement st = connection.prepareStatement("INSERT INTO person(\"firstName\","
                     + " \"lastName\", email, phone1, phone2) VALUES(?, ?, ?, ?, ?)");
            
             st.setString(1, _person.getFirstName());
             st.setString(2, _person.getLastName());
             st.setString(3, _person.getEmail());
             st.setString(4, _person.getPhone1());
             st.setString(5, _person.getPhone2());
            
             st.executeUpdate();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem inserting person into database");
         }
 
         //get ID
         try
         {
             PreparedStatement st = connection.prepareStatement("SELECT MAX(id) FROM person");
            
             rs = st.executeQuery();
         }
         
         catch(Exception e)
         {
             
             System.out.println("Problem selecting id from inserted person from database");
         }
         
         try 
         {
             while(rs.next())
             {  
                 id = rs.getInt(1);
             }
             rs.close();
         } 
         
         catch(Exception e)
         {
             System.out.println("Problem mapping id from inserted person");
         }
 
         return id;
     }
       
     
     public boolean updatePerson(Person _person)
     {
         try
         {
             PreparedStatement st = connection.prepareStatement("UPDATE person SET \"firstName\" = ?, "
                     + " \"lastName\" = ?, phone1 = ?, phone2 = ?, email = ? WHERE id = ?");
            
       
             st.setString(1, _person.getFirstName());
             st.setString(2, _person.getLastName());
             st.setString(3, _person.getPhone1());
             st.setString(4, _person.getPhone2());
             st.setString(5, _person.getEmail());
             st.setInt(6, _person.getId());
            
             st.executeUpdate();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem updating person to database");
             return false;
         }
  
         return true;
     }
        
     
     public boolean deletePerson(int _id)
     { 
         try
         {
             PreparedStatement st = connection.prepareStatement("DELETE FROM person"
                     + " WHERE id = ?");
             
             st.setInt(1, _id);
             
             int result = st.executeUpdate();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem deleting person from database");
             return false;
         }
 
         return true;
     }
      // </editor-fold>
     
     
     // <editor-fold defaultstate="collapsed" desc="project">
     public Project getProjectByID(int _id)
     {
         Project project = new Project();
         ResultSet rs = null;
         
         try
         {
             PreparedStatement st = connection.prepareStatement("SELECT * FROM project WHERE id = ?");
             
             st.setInt(1, _id);
             rs = st.executeQuery();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem selecting project by id from database");
         }
         
         try 
         {
             while(rs.next())
             {  
                 project.setId(rs.getInt("id"));
                 project.setName(rs.getString("name"));
             }
             rs.close();
         } 
         
         catch(Exception e)
         {
             System.out.println("Problem mapping selected project by id, result could be null");
         }
 
         return project;
     }
      
     
     public int insertProject(Project _project)
     {
         ResultSet rs    = null;
         int id          = 0;
         
         try
         {
             PreparedStatement st = connection.prepareStatement("INSERT INTO project(name) "
                     + " VALUES(?)");
            
             st.setString(1, _project.getName());
            
             st.executeUpdate();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem inserting project into database");
         }
         
         //get ID
         try
         {
             PreparedStatement st = connection.prepareStatement("SELECT MAX(id) FROM project");
            
             rs = st.executeQuery();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem selecting id from inserted project from database");
         }
         
         try 
         {
             while(rs.next())
             {  
                 id = rs.getInt(1);
             }
             rs.close();
         } 
         
         catch(Exception e)
         {
             System.out.println("Problem mapping id from inserted project");
         }
         
         return id;
     }
     
     
     public boolean updateProject(Project _project)
     {
         try
         {
             PreparedStatement st = connection.prepareStatement("UPDATE project SET name = ?, "
                     + " WHERE id = ?");
            
             st.setString(1, _project.getName());
            
             st.executeUpdate();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem updating project to database");
             return false;
         }
 
         return true;
     }
     
     
     public boolean deleteProject(int _id)
     {
         try
         {
             PreparedStatement st = connection.prepareStatement("DELETE FROM project"
                     + " WHERE id = ?");
             
             st.setInt(1, _id);
             
             st.executeUpdate();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem deleting project from database");
             return false;
         }
  
         return true;
     }
     // </editor-fold>
     
     
     // <editor-fold defaultstate="collapsed" desc="systemNotifcation">
     public SystemNotification getSystemNotificationByID(int _id)
     {
         SystemNotification notification = new SystemNotification();
         ResultSet rs = null;
         
         try
         {
             PreparedStatement st = connection.prepareStatement("SELECT * FROM system_notification WHERE id = ?");
             
             st.setInt(1, _id);
             rs = st.executeQuery();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem selecting system notification by id from database");
         }
         
         try 
         {
             while(rs.next())
             {  
                 notification.setMessage(rs.getString("message"));
                 
                 String type = rs.getString("type");
                 type.toUpperCase();
                 
                 if(type.equals(SystemNotification.ENotificationType.ERROR))
                 {
                     notification.setType(SystemNotification.ENotificationType.ERROR);
                 }
                 
                 else if(type.equals(SystemNotification.ENotificationType.NEUTRAL))
                 {
                     notification.setType(SystemNotification.ENotificationType.NEUTRAL);
                 }
                 
                 else if(type.equals(SystemNotification.ENotificationType.SUCCESS))
                 {
                     notification.setType(SystemNotification.ENotificationType.SUCCESS);
                 }
             }
             rs.close();
         } 
         
         catch(Exception e)
         {
             System.out.println("Problem mapping system notification selected by id, result could be null");
         }
 
         return notification;
     }
     
     
     public int insertSystemNotification(SystemNotification _systemNotification)
     {
         ResultSet rs    = null;
         int id          = 0;
         
         try
         {
             PreparedStatement st = connection.prepareStatement("INSERT INTO system_notification"
                     + " (message, type) VALUES(?, ?)");
            
             st.setString(1, _systemNotification.getMessage());
             st.setString(2, _systemNotification.getType().toString().toUpperCase());
            
             st.executeUpdate();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem inserting system notification into database");
         }
         
         //get ID
         try
         {
             PreparedStatement st = connection.prepareStatement("SELECT MAX(id) FROM system_notification");
            
             rs = st.executeQuery();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem selecting id from inserted system notification from database");
         }
         
         try 
         {
             while(rs.next())
             {  
                 id = rs.getInt(1);
             }
             rs.close();
         } 
         
         catch(Exception e)
         {
             System.out.println("Problem mapping selected id from inserted system notification");
         }
 
         return id;
     }
     
     
     public boolean updateSystemNotification(SystemNotification _systemNotification)
     {
         try
         {
             PreparedStatement st = connection.prepareStatement("UPDATE system_notification"
                     + " SET message = ?, type = ? WHERE id = ?");
            
             st.setString(1, _systemNotification.getMessage());
             st.setString(2, _systemNotification.getType().toString().toLowerCase());
             st.setInt(3, _systemNotification.getId());
            
             st.executeUpdate();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem updating system notification to database");
             return false;
         }
 
         return true;
     }
     
     
     public boolean deleteSystemNotification(int _id)
     {
         try
         {
             PreparedStatement st = connection.prepareStatement("DELETE FROM system_notification"
                     + " WHERE id = ?");
             
             st.setInt(1, _id);
             
             st.executeUpdate();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem deleting system notification from database");
             return false;
         }
 
         return true;
     }
     // </editor-fold>
     
     
     // <editor-fold defaultstate="collapsed" desc="Tax">
     public Tax getTaxByID(int _id)
     {
         Tax tax = new Tax();
         ResultSet rs = null;
         
         try
         {
             PreparedStatement st = connection.prepareStatement("SELECT * FROM tax WHERE id = ?");
             
             st.setInt(1, _id);
             rs = st.executeQuery();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem selecting tax by id from database");
         }
         
         try 
         {
             while(rs.next())
             {  
                 tax.setAmount(rs.getFloat("amount"));
                 tax.setType(rs.getString("type"));
                 tax.setSystemFlag(rs.getBoolean("systemFlag"));
             }
             rs.close();
         } 
         
         catch(Exception e)
         {
             System.out.println("Problem mapping selected tax by id, result could be null");
         }
 
         return tax;
     }
     
     
     public int insertTax(Tax _tax)
     {
         ResultSet rs    = null;
         int id          = 0;
                 
         try
         {
             PreparedStatement st = connection.prepareStatement("INSERT INTO tax"
                     + " (amount, type, \"systemFlag\") VALUES(?, ?, ?)");
            
             st.setFloat(1, _tax.getAmount());
             st.setString(2, _tax.getType());
             st.setBoolean(3, _tax.isSystemFlag());
            
             st.executeUpdate();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem when inserting tax into database");
         }
         
         //get ID
         try
         {
             PreparedStatement st = connection.prepareStatement("SELECT MAX(id) FROM tax");
            
             rs = st.executeQuery();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem selecting id from inserted tax from database");
         }
         
         try 
         {
             while(rs.next())
             {  
                 id = rs.getInt(1);
             }
             rs.close();
         } 
         
         catch(Exception e)
         {
             System.out.println("Problem mapping id from inserted tax");
         }
 
         return id;
     }
     
     
     public boolean updateTax(Tax _tax)
     {
         try
         {
             PreparedStatement st = connection.prepareStatement("UPDATE tax"
                     + " SET amount = ?, type = ?, \"systemFlag\" = ? WHERE id = ?");
            
             st.setFloat(1, _tax.getAmount());
             st.setString(2, _tax.getType());
             st.setBoolean(3, _tax.isSystemFlag());
             st.setInt(4, _tax.getId());
            
             st.executeUpdate();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem updating tax to database");
             return false;
         }
 
         return true;
     }
     
     
     public boolean deleteTax(int _id)
     {
         try
         {
             PreparedStatement st = connection.prepareStatement("DELETE FROM tax"
                     + " WHERE id = ?");
             
             st.setInt(1, _id);
             
             st.executeUpdate();
         }
         
         catch(Exception e)
         {
             System.out.println("Problem deleting tax from database");
             return false;
         }
 
         return true;
     }
     // </editor-fold>
     
     private Connection connection                       = null;
     private static final String s_DatabaseUser          = "postgres";
     private static final String s_DatabasePw            = "pgr4";
     private static final String s_Url                   = "jdbc:postgresql://localhost:5432/BudgetForce";
     private static final String s_Driver                = "org.postgresql.Driver";
     private static DatabaseManager s_SingletonManager   = null;  
 }
