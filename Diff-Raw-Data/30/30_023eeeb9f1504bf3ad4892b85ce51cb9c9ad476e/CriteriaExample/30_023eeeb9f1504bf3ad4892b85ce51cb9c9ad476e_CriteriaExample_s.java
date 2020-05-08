 package Chapter.Seven;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.Transaction;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 
 import Chapter.Seven.pojo.Product;
 import Chapter.Seven.pojo.Software;
 import Chapter.Seven.pojo.Supplier;
 import Chapter.Three.Cameron_Mckenzie.HibernateUtil;
 
 public class CriteriaExample {
   // public void executeSimpleCriteria(Session session) {
   // Criteria crit = session.createCriteria(Product.class);
   // List results = crit.list();
   // displayProductsList(results);
   // }
   //
   // public void executeEqualsCriteria(Session session) {
   // Criteria crit = session.createCriteria(Product.class);
   // crit.add(Restrictions.eq("name", "Mouse"));
   // List results = crit.list();
   // displayProductsList(results);
   // }
   //
   // public void executeNotEqualsCriteria(Session session) {
   // Criteria crit = session.createCriteria(Product.class);
   // crit.add(Restrictions.ne("name", "Mouse"));
   // List results = crit.list();
   // displayProductsList(results);
   // }
   //
   // public void executeLikePatternCriteria(Session session) {
   // Criteria crit = session.createCriteria(Product.class);
   // crit.add(Restrictions.like("name", "Mou%"));
   // List results = crit.list();
   // displayProductsList(results);
   // }
   //
   // public void executeILikeMatchModeCriteria(Session session) {
   // Criteria crit = session.createCriteria(Product.class);
   // crit.add(Restrictions.ilike("name", "browser", MatchMode.END));
   // List results = crit.list();
   // displayProductsList(results);
   // }
   //
   // public void executeNullCriteria(Session session) {
   // Criteria crit = session.createCriteria(Product.class);
   // crit.add(Restrictions.isNull("name"));
   // List results = crit.list();
   // displayProductsList(results);
   // }
   //
   // public void executeGreaterThanCriteria(Session session) {
   // Criteria crit = session.createCriteria(Product.class);
   // crit.add(Restrictions.gt("price", new Double(25.0)));
   // List results = crit.list();
   // displayProductsList(results);
   // }
   //
   // public void executeAndCriteria(Session session) {
   // Criteria crit = session.createCriteria(Product.class);
   // crit.add(Restrictions.gt("price", new Double(25.0)));
   // crit.add(Restrictions.like("name", "K%"));
   // List results = crit.list();
   // displayProductsList(results);
   // }
   //
   // public void executeOrCriteria(Session session) {
   // Criteria crit = session.createCriteria(Product.class);
   // Criterion price = Restrictions.gt("price", new Double(25.0));
   // Criterion name = Restrictions.like("name", "Mou%");
   // LogicalExpression orExp = Restrictions.or(price, name);
   // crit.add(orExp);
   // List results = crit.list();
   // displayProductsList(results);
   // }
   //
   // public void executeAndOrCriteria(Session session) {
   // Criteria crit = session.createCriteria(Product.class);
   // Criterion price = Restrictions.gt("price", new Double(25.0));
   // Criterion name = Restrictions.like("name", "Mou%");
   // LogicalExpression orExp = Restrictions.or(price, name);
   // crit.add(orExp);
   // crit.add(Restrictions.ilike("description", "blocks%"));
   // List results = crit.list();
   // displayProductsList(results);
   // }
   //
   // public void executeDisjunctionCriteria(Session session) {
   // Criteria crit = session.createCriteria(Product.class);
   // Criterion price = Restrictions.gt("price", new Double(25.0));
   // Criterion name = Restrictions.like("name", "Mou%");
   // Criterion desc = Restrictions.ilike("description", "blocks%");
   // Disjunction disjunction = Restrictions.disjunction();
   // disjunction.add(price);
   // disjunction.add(name);
   // disjunction.add(desc);
   // crit.add(disjunction);
   // List results = crit.list();
   // displayProductsList(results);
   // }
   //
   // public void executeSQLCriteria(Session session) {
   // Criteria crit = session.createCriteria(Product.class);
   // crit.add(Restrictions.sqlRestriction("{alias}.name like 'Mou%'"));
   // List results = crit.list();
   // displayProductsList(results);
   // }
   //
   // public void executePagingCriteria(Session session) {
   // Criteria crit = session.createCriteria(Product.class);
   // crit.setFirstResult(1);
   // crit.setMaxResults(2);
   // List results = crit.list();
   // displayProductsList(results);
   // }
   //
   // public void executeUniqueResultCriteria(Session session) {
   // Criteria crit = session.createCriteria(Product.class);
   // Criterion price = Restrictions.gt("price", new Double(25.0));
   // crit.setMaxResults(1);
   // Product product = (Product) crit.uniqueResult();
   // // test for null here if needed
   //
   // List results = new ArrayList();
   // results.add(product);
   // displayProductsList(results);
   // }
   //
   // public void executeUniqueResultExceptionCriteria(Session session) {
   // Criteria crit = session.createCriteria(Product.class);
   // Criterion price = Restrictions.gt("price", new Double(25.0));
   // Product product = (Product) crit.uniqueResult();
   // // test for null here if needed
   //
   // List results = new ArrayList();
   // results.add(product);
   // displayProductsList(results);
   // }
   //
   // public void executeOrderCriteria(Session session) {
   // Criteria crit = session.createCriteria(Product.class);
   // crit.add(Restrictions.gt("price", new Double(25.0)));
   // crit.addOrder(Order.desc("price"));
   // List results = crit.list();
   // displayProductsList(results);
   // }
   //
   // public void executeManyToOneAssociationsCriteria(Session session) {
   // Criteria crit = session.createCriteria(Product.class);
   // Criteria suppCrit = crit.createCriteria("supplier");
   // suppCrit.add(Restrictions.eq("name", "MegaInc"));
   // List results = crit.list();
   //
   // displayProductsList(results);
   // }
   //
   // public void executeNotNullOrZeroQBECriteria(Session session) {
   // Criteria crit = session.createCriteria(Product.class);
   // Product exampleProduct = new Product();
   // exampleProduct.setName("Mouse");
   // Example example = Example.create(exampleProduct);
   // example.excludeZeroes();
   // crit.add(example);
   // List results = crit.list();
   //
   // displayProductsList(results);
   // }
   //
   // public void executeQBEAdvancedCriteria(Session session) {
   // Criteria prdCrit = session.createCriteria(Product.class);
   // Product product = new Product();
   // product.setName("M%");
   // Example prdExample = Example.create(product);
   // prdExample.excludeProperty("price");
   // prdExample.enableLike();
   // Criteria suppCrit = prdCrit.createCriteria("supplier");
   // Supplier supplier = new Supplier();
   // supplier.setName("SuperCorp");
   // suppCrit.add(Example.create(supplier));
   // prdCrit.add(prdExample);
   // List results = prdCrit.list();
   //
   // displayProductsList(results);
   // }
   //
   // public void executeRowCountCriteria(Session session) {
   // Criteria crit = session.createCriteria(Product.class);
   // crit.setProjection(Projections.rowCount());
   // List results = crit.list();
   // displayObjectList(results);
   // }
   //
   // public void executeAggregatesCriteria(Session session) {
   // Criteria crit = session.createCriteria(Product.class);
   // ProjectionList projList = Projections.projectionList();
   // projList.add(Projections.max("price"));
   // projList.add(Projections.min("price"));
   // projList.add(Projections.avg("price"));
   // projList.add(Projections.countDistinct("description"));
   // crit.setProjection(projList);
   // List results = crit.list();
   // displayObjectsList(results);
   // }
   //
   // public void executeProjectionCriteria(Session session) {
   // Criteria crit = session.createCriteria(Product.class);
   // ProjectionList projList = Projections.projectionList();
   // projList.add(Projections.property("name"));
   // projList.add(Projections.property("description"));
   // crit.setProjection(projList);
   // List results = crit.list();
   // displayObjectsList(results);
   // }
   //
   // public void executeGroupByCriteria(Session session) {
   // Criteria crit = session.createCriteria(Product.class);
   // ProjectionList projList = Projections.projectionList();
   // projList.add(Projections.groupProperty("name"));
   // projList.add(Projections.property("price"));
   // crit.setProjection(projList);
   // List results = crit.list();
   // displayObjectsList(results);
   // }
   //
   // public void executeDistinctCriteria(Session session) {
   // Criteria crit = session.createCriteria(Product.class);
   // crit.add(Restrictions.gt("price", new Double(25.0)));
   // crit.add(Restrictions.like("name", "K%"));
   // crit.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
   // List results = crit.list();
   // displayProductsList(results);
   // }
   //
   // public void displayObjectList(List list) {
   // Iterator iter = list.iterator();
   // if (!iter.hasNext()) {
   // System.out.println("No objects to display.");
   // return;
   // }
   // while (iter.hasNext()) {
   // Object obj = iter.next();
   //
   // System.out.println(obj);
   // }
   // }
   //
   // public void displayObjectsList(List list) {
   // Iterator iter = list.iterator();
   // if (!iter.hasNext()) {
   // System.out.println("No objects to display.");
   // return;
   // }
   // while (iter.hasNext()) {
   // System.out.println("New object");
   // Object[] obj = (Object[]) iter.next();
   // for (int i = 0; i < obj.length; i++) {
   // System.out.println(obj[i]);
   // }
   //
   // }
   // }
   //
   // public void displayProductsList(List list) {
   // Iterator iter = list.iterator();
   // if (!iter.hasNext()) {
   // System.out.println("No products to display.");
   // return;
   // }
   // while (iter.hasNext()) {
   // Product product = (Product) iter.next();
   // String msg = product.getSupplier().getName() + "\t";
   // msg += product.getName() + "\t";
   // msg += product.getPrice() + "\t";
   // msg += product.getDescription();
   // System.out.println(msg);
   // }
   // }
   //
   // public void displaySoftwareList(List list) {
   // Iterator iter = list.iterator();
   // if (!iter.hasNext()) {
   // System.out.println("No software to display.");
   // return;
   // }
   // while (iter.hasNext()) {
   // Software software = (Software) iter.next();
   // String msg = software.getSupplier().getName() + "\t";
   // msg += software.getName() + "\t";
   // msg += software.getPrice() + "\t";
   // msg += software.getDescription() + "\t";
   // msg += software.getVersion();
   // System.out.println(msg);
   // }
   // }
 
   public void executeOneToManyAssociationsCriteria() {
     Session session = HibernateUtil.getSession();
     Transaction transaction = session.beginTransaction();
 
     Criteria supplierCriteria = session.createCriteria(Supplier.class);
     Criteria productCriteria = supplierCriteria.createCriteria("products");
     productCriteria.add(Restrictions.gt("price", new Double(25.0)));
    
     displaySupplierList(supplierCriteria.list());
     transaction.commit();
   }
 
   public void executeAssociationsSortingCriteria() {
     Session session = HibernateUtil.getSession();
     Transaction transaction = session.beginTransaction();
 
     Criteria supplierCriteria = session.createCriteria(Supplier.class);
     supplierCriteria.addOrder(Order.desc("name"));
 
     Criteria productCriteria = supplierCriteria.createCriteria("products");
     productCriteria.add(Restrictions.gt("price", new Double(25.0)));
 
     displaySupplierList(productCriteria.list());
 
     transaction.commit();
   }
 
  // public void executeQBECriteria(Session session) {
  // Criteria crit = session.createCriteria(Supplier.class);
  // Supplier supplier = new Supplier();
  // supplier.setName("MegaInc");
  // crit.add(Example.create(supplier));
  // List results = crit.list();
  //
  // displaySupplierList(results);
  // }
 
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
 
   // Populate Database
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
     CriteriaExample example = new CriteriaExample();
 
     System.out.println("=== Populate Database ===");
     example.populate();
 
     System.out.println("=== Execute One-To-Many Associations Criteria ===");
     // example.executeOneToManyAssociationsCriteria(); // BUG: runs, but doesn't produce correct output
 
     System.out.println("=== Execute Association Sorting Criteria ===");
     // example.executeAssociationsSortingCriteria();  // BUG: runs, but doesn't produce correct output
 
     // --
 
     // trans.commit();
     // trans = session.beginTransaction();
     // example.executeSimpleCriteria(session);
     // example.executeDistinctCriteria(session);
   }
 }
