 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package data;
 
 import exceptions.StorageException;
 import java.lang.reflect.Field;
 import java.util.*;
 
 /**
  *
  * @author christianlinde, delbertooo
  */
 public class Storage {
 
     /* should be thread safe too: http://www.theserverside.de/singleton-pattern-in-java/ */
     private static Storage instance = new Storage();
     private HashMap<DataType, HashMap<Integer, IStorageData>> data;
     private HashMap<DataType, Integer> autoInc;
     private HashMap<String, Coupon> coupons;
 
     private static class Data {
 
         public static final DataType USERS = new DataType(0);
         public static final DataType PRODUCTS = new DataType(1);
     }
 
     private void addDefaultData() throws StorageException {
         //add some users
         addUser(new User(
                 true,
                 "Admin",
                 "password",
                 "admin@example.com",
                 "Admin Street",
                 "Admin Town"));
 
         //add some products
         addProduct(new Product(
                 "Kindle Fire HD",
                 199f,
                 "17 cm (7 Zoll), Dualband -WLAN über zwei Antennen, "
                 + "HD-Display, Dolby-Audio-Technologie, 16 GB oder 32 GB",
                 "Amazon"));
         addProduct(new Product(
                 "Dell Latitude ST T11",
                 729.95f,
                 "25,7 cm (10,1 Zoll) Tablet-PC (Intel Atom Z670, 1,5GHz, "
                 + "2GB RAM, 32GB SSD, Intel 600, Win 7 Pro)",
                 "Dell"));
         addProduct(new Product(
                 "JAGUAR XF SPORTBRAKE",
                 48550f,
                 "3.0 Liter V6 Diesel S 275 PS (202kW): Kraftstoffverbrauch: "
                 + "innerorts 8,6 l/100km, außerorts 4,9 l/100km, kombiniert "
                 + "6,2 l/100km.CO2-Emission: 163 g/km; Effizienzklasse B",
                 "JAGUAR"));
 
     }
 
     private void addData(DataType type, IStorageData d) throws StorageException {
         synchronized (Storage.class) {
             int newId = autoInc.get(type) + 1;
             autoInc.put(type, newId);
             d.setId(newId);
         }
     }
 
     private void setData(DataType type, ICopyable d) {
         synchronized (Storage.class) {
             data.get(type).put(((IStorageData)d).getId(), (IStorageData)d.getCopy());
         }
     }
 
     private IStorageData getDataById(DataType type, int id) {
         synchronized (Storage.class) {
             ICopyable c = (ICopyable)data.get(type).get(id);
             return (IStorageData)c.getCopy();
         }
     }
 
     private void deleteDataById(DataType type, int id) {
         synchronized (Storage.class) {
             data.get(type).remove(id);
         }
     }
 
     public void addUser(User u) throws StorageException {
         addData(Storage.Data.USERS, u);
     }
 
     public void setUser(User u) {
         setData(Storage.Data.USERS, u);
     }
 
     public void addProduct(Product p) throws StorageException {
         addData(Storage.Data.PRODUCTS, p);
     }
 
     public void setProduct(Product p) {
         setData(Storage.Data.PRODUCTS, p);
     }
 
     public User getUserById(int id) {
         return (User) getDataById(Storage.Data.USERS, id);
     }
 
     public Product getProductById(int id) {
         return (Product) getDataById(Storage.Data.PRODUCTS, id);
     }
 
     public void deleteUserById(int id) {
         deleteDataById(Storage.Data.USERS, id);
     }
 
     public void deleteProductById(int id) {
         deleteDataById(Storage.Data.PRODUCTS, id);
     }
     
     public void addCoupon(Coupon c) {
         synchronized (Storage.class) {
            this.coupons.put(c.getCode(), c);
         }
     }
     
     public Coupon getCouponByCody(String code) {
         synchronized (Storage.class) {
             return (Coupon)this.coupons.get(code).getCopy();
         }
     }
 
     /**
      * Returns the IDs of users with a name starting with <param>name</param>.
      * This function is <b>not</b> case sensitive.
      *
      * @param name Exact name or the first characters of a name.
      * @return ArrayList of Integers (IDs).
      */
     public List<Integer> getUserIdsByName(String name) {
         List<Integer> res = new ArrayList<Integer>();
         synchronized (Storage.class) {
             for (Map.Entry<Integer, IStorageData> e : data.get(Storage.Data.USERS).entrySet()) {
                 User u = (User) e.getValue();
                 if (u.getName().toLowerCase().equals(name.toLowerCase())) {
                     res.add(e.getKey());
                 }
             }
         }
         return res;
     }
 
     public WishList getWishListForUser(int userId) {
         return this.getUserById(userId).getWishList();
     }
 
     public Cart getCartForUser(int userId) {
         return this.getUserById(userId).getCart();
     }
     
     public List<Comment> getCommentsForProduct(int productId) {
         return this.getProductById(productId).getComments();
     }
 
     private Storage() {
 
         autoInc = new HashMap<DataType, Integer>();
         coupons = new HashMap<String, Coupon>();
         Field[] fields = Storage.Data.class.getFields();
         for (Field f : fields) {
             try {
                 DataType key = (DataType) f.get(Storage.Data.class);
                 autoInc.put(key, 0);
                 data.put(key, new HashMap<Integer, IStorageData>());
             } catch (Exception ex) {
                 throw new RuntimeException("Something in Storage went completely wrong.\n" + ex.getMessage());
             }
         }
         try {
             addDefaultData();
         } catch (StorageException ex) {
             throw new RuntimeException("Failed to initialize Data.\n" + ex.getMessage());
         }
     }
 
     public static Storage getInstance() {
         return instance;
     }
 }
