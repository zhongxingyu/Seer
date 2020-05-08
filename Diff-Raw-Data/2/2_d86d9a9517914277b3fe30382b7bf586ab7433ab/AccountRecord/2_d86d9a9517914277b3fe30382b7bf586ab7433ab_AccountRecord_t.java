 package name.richardson.james.bukkit.exchequer;
 
 import java.util.List;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 import javax.persistence.Table;
 
 import com.avaje.ebean.validation.NotNull;
 
 import name.richardson.james.bukkit.utilities.internals.Logger;
 
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class AccountRecord.
  */
 @Entity
 @Table(name = "exchequer_accounts")
 public class AccountRecord implements Balance {
 
   /** The logger for this class. */
   public static final Logger logger = new Logger(AccountRecord.class);
 
   /**
    * The id (primary key) of the account. Also referenced as the account number
    * and used in various commands.
    */
   @Id
   private int id;
 
   /** The current balance of the account. */
   @NotNull
   private double balance;
 
   /** If the account is hidden or not. */
   @NotNull
   private boolean hidden;
 
   /** If the account is personal or not. */
   @NotNull
   private boolean personal;
 
   /** A list of signatories on the account. */
   private List<PlayerRecord> signatories;
 
   /**
    * Gets all the signatories associated with the account.
    * 
    * @return the owners
    */
   @ManyToMany(mappedBy = "accounts")
   public List<PlayerRecord> getSignatories() {
     return this.signatories;
   }
 
   /**
    * Sets the signatories associated with this account.
    * 
    * @param owners the new owners
    */
   public void setSignatories(List<PlayerRecord> signatories) {
     this.signatories = signatories;
   }
 
   /*
    * (non-Javadoc)
    * @see name.richardson.james.bukkit.exchequer.Balance#add(double)
    */
   public double add(double amount) {
     double proposal = this.getBalance() + amount;
     if (proposal < 0)
       throw new IllegalArgumentException("Balances can not be negative.");
     this.setBalance(proposal);
     return this.getBalance();
   }
 
   /*
    * (non-Javadoc)
    * @see name.richardson.james.bukkit.exchequer.Balance#substract(double)
    */
   public double substract(double amount) {
     double proposal = this.getBalance() - amount;
     if (proposal < 0)
       throw new IllegalArgumentException("Balances can not be negative.");
     this.setBalance(proposal);
     return this.getBalance();
   }
 
   /*
    * (non-Javadoc)
    * @see name.richardson.james.bukkit.exchequer.Balance#divide(double)
    */
   public double divide(double amount) {
     this.setBalance(this.getBalance() / amount);
     return this.getBalance();
   }
 
   /*
    * (non-Javadoc)
    * @see name.richardson.james.bukkit.exchequer.Balance#multiply(double)
    */
   public double multiply(double amount) {
     this.setBalance(this.getBalance() * amount);
     return this.getBalance();
   }
 
   /*
    * (non-Javadoc)
    * @see name.richardson.james.bukkit.exchequer.Balance#contains(double)
    */
   public boolean contains(double amount) {
    if (this.getBalance() >= amount) {
       return true;
     } else {
       return false;
     }
   }
 
   /**
    * Gets the id (primary key) of the account.
    * 
    * @return the id
    */
   public int getId() {
     return id;
   }
 
   /**
    * Sets the id (primary key) of the account.
    * 
    * This should not normally be used.
    * 
    * @param id the new id
    */
   public void setId(int id) {
     this.id = id;
   }
 
   /**
    * Checks if the account is a personal account.
    * 
    * @return true, if it is a personal account.
    */
   public boolean isPersonal() {
     return personal;
   }
 
   /**
    * Sets if an account is a personal account or not.
    * 
    * @param value
    */
   public void setPersonal(boolean value) {
     this.personal = value;
   }
 
   /**
    * Checks if the account is hidden or not.
    * A hidden account should not be visible to players by default.
    * 
    * @return true, if it is hidden.
    */
   public boolean isHidden() {
     return hidden;
   }
 
   /**
    * Sets if an account is hidden or not.
    * 
    * @param value
    */
   public void setHidden(boolean value) {
     this.hidden = value;
   }
 
   /**
    * Gets the balance of the account.
    * 
    * @return the total available funds in this account.
    */
   public double getBalance() {
     return balance;
   }
 
   /**
    * Sets the balance of the account.
    * 
    * @param balance the new balance.
    */
   public void setBalance(double balance) {
     this.balance = balance;
   }
 
   /**
    * Find last created account.
    * 
    * @param database the database to use.
    * @return the AccountRecord found.
    */
   public static AccountRecord findLastCreatedAccount(DatabaseHandler database) {
     logger.debug(String.format("Attempting to return last created AccountRecord."));
     return database.getEbeanServer().find(AccountRecord.class).where().setOrderBy("id DESC").findList().get(0);
   }
 
   public static AccountRecord findAccountByID(DatabaseHandler database, int id) {
     logger.debug(String.format("Attempting to return AccountRecord maching account number: %d.", id));
     return database.getEbeanServer().find(AccountRecord.class).where().eq("id", id).findUnique();
   }
 
 }
