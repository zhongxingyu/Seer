 package de.eonas.addressbook;
 
 import de.eonas.addressbook.genericmodel.LdapSelectableData;
 import de.eonas.addressbook.model.Person;
 import org.jetbrains.annotations.Nullable;
 import org.primefaces.model.SortOrder;
 import org.springframework.stereotype.Component;
 
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.TypedQuery;
 import javax.persistence.criteria.*;
 import java.util.List;
 import java.util.Map;
 
 @Component
 public class Dao<T extends LdapSelectableData> {
     @PersistenceContext(unitName = "Addressbook")
     EntityManager em;
     Class<T> clazz;
 
     public List<T> load() {
         return load(0, 50, null, null, null );
     }
 
     public Object getRowKey(T object) {
         return object.getDn();
     }
 
     public void save(T object) {
         em.merge(object);
     }
 
     public List<T> load(int first, int pageSize, @Nullable String sortField, @Nullable SortOrder sortOrder, @Nullable Map<String, String> filters) {
         CriteriaBuilder cb = em.getCriteriaBuilder();
         CriteriaQuery<T> cq = cb.createQuery(clazz);
         //Metamodel m = em.getMetamodel();
         //EntityType<T> et = m.entity(clazz);
         Root<T> all = cq.from(clazz);
 
         //noinspection PointlessBooleanExpression
         if (false && filters != null) {
             // untested !!!
             for (String filter : filters.keySet()) {
                 String value = filters.get(filter);
                 final Path<String> objectPath = all.get(filter);
                 cq.where(cb.like(objectPath, value + "%"));
             }
         }
 
         //noinspection PointlessBooleanExpression
         if (false && sortField != null) {
             // untested !!!
             Order order;
             final Path<String> stringPath = all.get(sortField);
             if ( SortOrder.ASCENDING == sortOrder) {
                 order = cb.asc(stringPath);
             } else {
                 order = cb.desc(stringPath);
             }
             cq.orderBy(order);
         }
 
         cq.select(all);
         TypedQuery<T> query = em.createQuery(cq);
         query.setFirstResult(first);
         query.setMaxResults(pageSize);
         return query.getResultList();
     }
 
     @SuppressWarnings("UnusedDeclaration")
     public Class<T> getClazz() {
         return clazz;
     }
 
     public void setClazz(Class<T> clazz) {
         this.clazz = clazz;
     }
 
     public void insertDemoDataTransacted() {
         Person p = new Person();
         p.setCn("Helmut Manck");
         p.setGivenName("Helmut");
         p.setSn("Manck");
         p.setStreet("Pascalstraße 10a");
         p.setDisplayName("Helmut Manck");
         p.setC("Germany");
        p.setO("eonas IT-Bertung");
         p.setL("Berlin");
         p.setPostalCode("10587");
         p.setDn("" + System.currentTimeMillis());
         em.persist(p);
     }
 }
