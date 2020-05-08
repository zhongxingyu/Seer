 package Chapter.Eight;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.hibernate.Criteria;
 import org.hibernate.Hibernate;
 import org.hibernate.Query;
 import org.hibernate.SQLQuery;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.hibernate.criterion.Criterion;
 import org.hibernate.criterion.LogicalExpression;
 import org.hibernate.criterion.Restrictions;
 
 import Chapter.Seven.pojo.Product;
 import Chapter.Seven.pojo.Software;
 import Chapter.Seven.pojo.Supplier;
 import Chapter.Three.Cameron_Mckenzie.HibernateUtil;
 
 public class HQLExample {
 
   // *** Methods that use displayObjectList
 
 //  public void executeNamedQuery(Session session) {
 //
 //    Query query = session
 //        .getNamedQuery("com.hibernatebook.criteria.Product.HQLpricing");
 //    List results = query.list();
 //    displayObjectList(results);
 //
 //    query = session
 //        .getNamedQuery("com.hibernatebook.criteria.Product.SQLpricing");
 //    results = query.list();
 //    displayObjectList(results);
 //  }
 //
 //  public void executeScalarSQL(Session session) {
 //    String sql = "select avg(product.price) as avgPrice from Product product";
 //
 //    SQLQuery query = session.createSQLQuery(sql);
 //    query.addScalar("avgPrice", Hibernate.DOUBLE);
 //    List results = query.list();
 //    displayObjectList(results);
 //
 //  }
 
 //
 //  public void displayObjectList(List list) {
 //    Iterator iter = list.iterator();
 //    if (!iter.hasNext()) {
 //      System.out.println("No objects to display.");
 //      return;
 //    }
 //    while (iter.hasNext()) {
 //      Object obj = iter.next();
 //      System.out.println(obj.getClass().getName());
 //      System.out.println(obj);
 //    }
 //  }
   
   
   // *** Methods that use displayObjectsList
 
 //public void executeProjectionHQL(Session session) {
 //Query query = session
 //    .createQuery("select product.name, product.price from Product product");
 //List results = query.list();
 //displayObjectsList(results);
 //}
 
 //public void executeAssociationsHQL(Session session) {
 //String hql = "select s.name, p.name, p.price from Product p inner join p.supplier as s";
 //Query query = session.createQuery(hql);
 //List results = query.list();
 //displayObjectsList(results);
 //}
 
 //public void executeAssociationObjectsHQL(Session session) {
 //String hql = "from Product p inner join p.supplier as s";
 //Query query = session.createQuery(hql);
 //List results = query.list();
 //displayObjectsList(results);
 //}
 
 //public void executeCountHQL(Session session) {
 //String hql = "select min(product.price), max(product.price) from Product product";
 //Query query = session.createQuery(hql);
 //List results = query.list();
 //displayObjectsList(results);
 //}
 
   
 //  public void displayObjectsList(List list) {
 //    Iterator iter = list.iterator();
 //    if (!iter.hasNext()) {
 //      System.out.println("No objects to display.");
 //      return;
 //    }
 //    while (iter.hasNext()) {
 //      System.out.println("New object");
 //      Object[] obj = (Object[]) iter.next();
 //      for (int i = 0; i < obj.length; i++) {
 //        System.out.println(obj[i]);
 //      }
 //
 //    }
 //  }
   
   
   // *** Methods that use displayProductsList
   
 //public void executeSimpleHQL(Session session) {
 //Query query = session.createQuery("from Product");
 //List results = query.list();
 //displayProductsList(results);
 //}
 
//public void executeFullyQualifiedHQL(Session session) {
//Query query = session
//    .createQuery("from com.hibernatebook.criteria.Product");
//List results = query.list();
//displayProductsList(results);
//}
 
   public void executeDeleteHQL() {
     Session session = HibernateUtil.getSession();
     Transaction transaction = session.beginTransaction();
     String hql = "delete from Product where name = :name";
     Query query = session.createQuery(hql);
     query.setString("name", "Mouse");
     int rowCount = query.executeUpdate();
     System.out.println("Rows affected: " + rowCount);
     // See the results of the update
     query = session.createQuery("from Product");
     displayProductsList(query.list());
     transaction.commit();
   }
 
   public void executeCriteriaForRestrictions() {
     Session session = HibernateUtil.getSession();
     Transaction transaction = session.beginTransaction();
     Criterion price = Restrictions.gt("price", new Double(25.0));
     Criterion name = Restrictions.like("name", "Mou%");
     LogicalExpression orExp = Restrictions.or(price, name);
     Criteria criteria = session.createCriteria(Product.class);
     criteria.add(orExp);
     criteria.add(Restrictions.ilike("description", "blocks%"));
     displayProductsList(criteria.list());
     transaction.commit();
   }
 
   public void executeHQLForRestrictions() {
     Session session = HibernateUtil.getSession();
     Transaction transaction = session.beginTransaction();
     String hql = "from Product where price > 25.0 and name like 'Mou%'";
     Query query = session.createQuery(hql);
     displayProductsList(query.list());
     transaction.commit();
   }
 
   public void executeNamedParametersHQL() {
     Session session = HibernateUtil.getSession();
     Transaction transaction = session.beginTransaction();
     String hql = "from Product where price > :price";
     Query query = session.createQuery(hql);
     query.setDouble("price", 25.0);
     displayProductsList(query.list());
     transaction.commit();
   }
 
   public void executeObjectNamedParametersHQL() {
     Session session = HibernateUtil.getSession();
     Transaction transaction = session.beginTransaction();
     String supplierHQL = "from Supplier where name='MegaInc'";
     Query supplierQuery = session.createQuery(supplierHQL);
     Supplier supplier = (Supplier) supplierQuery.list().get(0);
     String hql = "from Product as product where product.supplier=:supplier";
     Query query = session.createQuery(hql);
     query.setEntity("supplier", supplier);
     displayProductsList(query.list());
     transaction.commit();
   }
 
   public void executePagingHQL() {
     Session session = HibernateUtil.getSession();
     Transaction transaction = session.beginTransaction();
     Query query = session.createQuery("from Product");
     query.setFirstResult(1);
     query.setMaxResults(2);
     displayProductsList(query.list());
     transaction.commit();
   }
 
   public void executeUniqueResultHQL() {
     Session session = HibernateUtil.getSession();
     Transaction transaction = session.beginTransaction();
     String hql = "from Product where price>25.0";
     Query query = session.createQuery(hql);
     query.setMaxResults(1);
     Product product = (Product) query.uniqueResult();
     // test for null here if needed
     List results = new ArrayList();
     results.add(product);
     displayProductsList(results);
     transaction.commit();
   }
 
   public void executeOrderHQL() {
     Session session = HibernateUtil.getSession();
     Transaction transaction = session.beginTransaction();
     String hql = "from Product p where p.price>25.0 order by p.price desc";
     Query query = session.createQuery(hql);
     displayProductsList(query.list());
     transaction.commit();
   }
 
   public void executeOrderTwoPropertiesHQL() {
     Session session = HibernateUtil.getSession();
     Transaction transaction = session.beginTransaction();
     String hql = "from Product p order by p.supplier.name asc, p.price asc";
     Query query = session.createQuery(hql);
     displayProductsList(query.list());
     transaction.commit();
   }
 
   private void displayProductsList(List list) {
     Iterator iterator = list.iterator();
 
     if (!iterator.hasNext()) {
       System.out.println("No products to display.");
       return;
     }
 
     while (iterator.hasNext()) {
       Product product = (Product) iterator.next();
       String msg = product.getSupplier().getName() + "\t";
       msg += product.getName() + "\t";
       msg += product.getPrice() + "\t";
       msg += product.getDescription();
       System.out.println(msg);
     }
   }
 
   // *** Methods that use displaySupplierList
 
   public void executeFetchAssociationsHQL() {
     Session session = HibernateUtil.getSession();
     Transaction transaction = session.beginTransaction();
     String hql = "from Supplier s inner join fetch s.products as p";
     Query query = session.createQuery(hql);
     displaySupplierList(query.list());
     transaction.commit();
   }
 
   public void executeSelectSQL() {
     Session session = HibernateUtil.getSession();
     Transaction transaction = session.beginTransaction();
     String sql = "select {supplier.*} from Supplier supplier";
     SQLQuery query = session.createSQLQuery(sql);
     query.addEntity("supplier", Supplier.class);
     displaySupplierList(query.list());
     transaction.commit();
   }
 
   public void executeUpdateHQL() {
     Session session = HibernateUtil.getSession();
     Transaction transaction = session.beginTransaction();
     String hql = "update Supplier set name = :newName where name = :name";
     Query query = session.createQuery(hql);
     query.setString("name", "SuperCorp");
     query.setString("newName", "MegaCorp");
     int rowCount = query.executeUpdate();
     System.out.println("Rows affected: " + rowCount);
     // See the results of the update
     query = session.createQuery("from Supplier");
     displaySupplierList(query.list());
     transaction.commit();
   }
 
   private void displaySupplierList(List list) {
     Iterator iterator = list.iterator();
 
     if (!iterator.hasNext()) {
       System.out.println("No suppliers to display.");
       return;
     }
 
     while (iterator.hasNext()) {
       Supplier supplier = (Supplier) iterator.next();
       System.out.println(supplier.getName());
     }
   }
 
   public void populate() {
     Supplier supplier = null;
     Product product = null;
     Software software = null;
 
     Session session = HibernateUtil.getSession();
     Transaction transaction = session.beginTransaction();
 
     supplier = new Supplier();
     supplier.setName("SuperCorp");
 
     product = new Product();
     product.setName("Mouse");
     product.setDescription("Optical Wheel Mouse");
     product.setPrice(20.0);
     product.setSupplier(supplier);
 
     supplier.addProduct(product);
 
     product = new Product();
     product.setName("Mouse");
     product.setDescription("One Button Mouse");
     product.setPrice(22.0);
     product.setSupplier(supplier);
 
     supplier.addProduct(product);
 
     software = new Software();
     software.setName("Web Browser");
     software.setDescription("Blocks Pop-ups");
     software.setVersion("2.0");
     software.setSupplier(supplier);
     supplier.addProduct(software);
 
     session.save(supplier);
 
     supplier = new Supplier();
     supplier.setName("MegaInc");
 
     product = new Product();
     product.setName("Keyboard");
     product.setDescription("101 Keys");
     product.setPrice(30.0);
     product.setSupplier(supplier);
 
     supplier.addProduct(product);
 
     software = new Software();
     software.setName("Email");
     software.setDescription("Blocks spam");
     software.setPrice(49.99);
     software.setVersion("4.1 RMX Edition");
     software.setSupplier(supplier);
 
     supplier.addProduct(software);
 
     session.save(supplier);
     transaction.commit();
   }
 
   public static void main(String args[]) {
     HQLExample example = new HQLExample();
 
     System.out.println("=== Populate Database ===");
     example.populate();
 
     // *** Methods that use displaySupplierList
     //System.out.println("=== Execute Update HQL ===");
     //example.executeUpdateHQL();
 
     //System.out.println("=== Execute Select SQL ===");
     //example.executeSelectSQL();
 
     //System.out.println("=== Execute Fetch Association HQL ===");
     //example.executeFetchAssociationsHQL();
 
     // *** Methods that use displayProductsList
     //System.out.println("=== Execute Order Two Properties HQL ===");
     //example.executeOrderTwoPropertiesHQL();
 
     //System.out.println("=== Execute Order HQL ===");
     //example.executeOrderHQL();
 
     //System.out.println("=== Execute Unique Result HQL ===");
     //example.executeUniqueResultHQL();
 
     //System.out.println("=== Execute Paging HQL ===");
     //example.executePagingHQL();
 
     //System.out.println("=== Execute Object Named Paramters HQL ===");
     //example.executeObjectNamedParametersHQL();
 
     //System.out.println("=== Execute Named Parameters HQL ===");
     //example.executeNamedParametersHQL();
 
     //System.out.println("=== Execute HQL for Restrictions ===");
     //example.executeHQLForRestrictions();
 
     //System.out.println("=== Execute Criteria for Restrictions ===");
     //example.executeCriteriaForRestrictions();
 
    System.out.println("=== Execute Delete HQL ===");
    example.executeDeleteHQL();
   }
 }
