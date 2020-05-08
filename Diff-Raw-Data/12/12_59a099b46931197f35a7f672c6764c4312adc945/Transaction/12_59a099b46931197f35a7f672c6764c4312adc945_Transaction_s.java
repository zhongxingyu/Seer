 package az.his.persist;
 
 import az.his.DBManager;
 import az.his.DBUtil;
 import org.hibernate.Query;
 import org.hibernate.type.DateType;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import javax.persistence.*;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 /**
  * Transaction to track.
  */
 @Entity(name = "transaction")
 public class Transaction implements DBListener {
     private int id;
     private int amount;
     private Date timestmp;
     private String comment;
     private User actor;
     private Account account;
     private TransactionCategory category;
     private boolean common;
     private boolean commonReady = false;
     private boolean amountReady = false;
 
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     public int getId() {
         return id;
     }
 
     public void setId(int id) {
         this.id = id;
     }
 
     public int getAmount() {
         return amount;
     }
 
     public void setAmount(int amount) {
         if (amountReady && isPersistent()) {
         if (!common) {
             long val = getAccount().getValue() - this.amount + amount;
             getAccount().setValue(val);
         }
         }
         if (!amountReady && isPersistent()) amountReady = true;
         this.amount = amount;
     }
 
     @Temporal(TemporalType.TIMESTAMP)
     public Date getTimestmp() {
         return timestmp;
     }
 
     public void setTimestmp(Date timestmp) {
         this.timestmp = timestmp;
     }
 
     public String getComment() {
         return comment;
     }
 
     public void setComment(String comment) {
         this.comment = comment;
     }
 
     @Basic
     public boolean isCommon() {
         return common;
     }
 
     public void setCommon(boolean common) {
         if (commonReady && isPersistent()) {
             if (!this.common && common) {
                 // Do not include in acc sum
                 getAccount().setValue(getAccount().getValue() - amount);
             } else if (this.common && !common) {
                 getAccount().setValue(getAccount().getValue() + amount);
             }
         }
         if (!commonReady && isPersistent()) commonReady = true;
         this.common = common;
     }
 
     @Transient
     private boolean isPersistent() {
         return (getId() != 0);
     }
 
     @ManyToOne
     public User getActor() {
         return actor;
     }
 
     public void setActor(User actor) {
         this.actor = actor;
     }
 
     @ManyToOne
     public Account getAccount() {
         return account;
     }
 
     public void setAccount(Account account) {
         this.account = account;
     }
 
     @ManyToOne
     public TransactionCategory getCategory() {
         return category;
     }
 
     public void setCategory(TransactionCategory category) {
         this.category = category;
     }
 
     @Transient
     public JSONObject getJson() throws JSONException {
         JSONObject ret = new JSONObject();
         DateFormat df = new SimpleDateFormat("dd.MM.yyyy");
 
         ret.put("id", getId());
        ret.put("amount", DBUtil.formatCurrency(getAmount()));
         ret.put("timestamp", df.format(getTimestmp()));
         ret.put("actor_id", getActor().getId());
         ret.put("actor_name", getActor().getName());
         ret.put("category_name", getCategory().getName());
         ret.put("comment", getComment());
 
         String type;
         int catId = getCategory().getId();
         if (catId == TransactionCategory.CAT_DONATE) type = "D";
         else if (catId == TransactionCategory.CAT_REFUND) type = "R";
         else type = "E";
 
         ret.put("type", type);
 
         return ret;
     }
 
     @Transient
     @SuppressWarnings("unchecked")
     public static List<Transaction> getFiltered(DBManager dbman, Date from, Date to, int category) {
         String q = "from az.his.persist.Transaction where timestmp >= :from and timestmp <= :to"
                 + ((category > 0) ? " and category = :cat" : "");
         Query query = dbman.getSession().createQuery(q)
                 .setParameter("from", from, DateType.INSTANCE)
                 .setParameter("to", to, DateType.INSTANCE);
         if (category > 0) {
             query.setParameter("cat", dbman.get(TransactionCategory.class, category));
         }
         return (List<Transaction>) query.list();
     }
 
     public void setAmount(double amount) {
         setAmount((int) Math.round(amount * 100));
     }
 
     @Override
     public void beforeDelete(DBManager dbman) {
         // Fix account sum
         if (!common)
             getAccount().setValue(getAccount().getValue() - amount);
 
         // Delete empty category
         if (getCategory().getType() != TransactionCategory.CatType.NONE) {
             Long res = (Long) dbman.getSession().createQuery("select count(id) from transaction where category = ?")
                     .setEntity(0, getCategory())
                     .uniqueResult();
             if (res <= 1) dbman.getSession().delete(getCategory());
         }
     }
 
     @Override
     public void beforeInsert(DBManager dbman) {
         if (!common)
             getAccount().setValue(getAccount().getValue() + amount);
     }
 }
