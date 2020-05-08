 package org.cloudlet.core.server;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.persistence.MappedSuperclass;
 import javax.persistence.NoResultException;
 import javax.persistence.Transient;
 import javax.persistence.TypedQuery;
 import javax.ws.rs.DefaultValue;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.UriBuilder;
 import javax.xml.bind.annotation.XmlTransient;
 
 @MappedSuperclass
 public abstract class Collection<E extends Item> extends Content {
 
   @QueryParam("start")
   @DefaultValue("0")
   @Transient
   protected int start;
 
   @QueryParam("limit")
   @DefaultValue("10")
   @Transient
   protected int limit;
 
   // sort=title|asc&sort=email|desc
   @QueryParam(Collection.SORT)
   @Transient
   protected List<String> sort;
 
   @QueryParam(Content.SEARCH)
   @Transient
   protected List<String> search;
 
   @Transient
   protected List<E> items;
 
   public static final String NEW = "new";
 
   public static final String QUERY_COUNT = "queryCount";
 
   public static final String ITEMS = "items";
 
   public static final String SORT = "sort";
 
   protected long total;
 
   @Transient
   protected long count;
 
   public static final char START_KEY = 's';
 
   public static final char LIMIT_KEY = 'l';
 
   @Override
   public void addJoin(StringBuilder sql) {
     sql.append(getEntryType().getName()).append(" e");
   }
 
   public void buildSearch(StringBuilder sql) {
     if (search != null && search.size() > 0) {
       sql.append(" and (");
       for (String s : search) {
         String[] split = s.split("\\|");
         sql.append("f.").append(split[0]).append(" like '").append(split[1]).append("%'").append(" or ");
       }
       sql.delete(sql.lastIndexOf(" or "), sql.length());
       sql.append(")");
     }
   }
 
   public void buildSort(StringBuilder sql) {
     if (sort != null && sort.size() > 0) {
       sql.append(" order by");
       for (String s : sort) {
         String[] split = s.split("\\|");
         sql.append(" f.").append(split[0]).append(" ").append(split[1]).append(",");
       }
       sql.deleteCharAt(sql.lastIndexOf(","));
     }
   }
 
   public long countItems() {
     StringBuilder sql = new StringBuilder("select count(e) from ");
     addJoin(sql);
     addWhere(sql);
     prepareQuery(sql);
     buildSearch(sql);
     TypedQuery<Long> query = em().createQuery(sql.toString(), Long.class);
     setParams(query);
     long count = query.getSingleResult().longValue();
     return count;
   }
 
   @Override
   public void doLoad() {
     items = findItems();
     count = countItems();
   }
 
   public List<E> findItems() {
     Class<E> entryClass = getEntryType();
     // CriteriaBuilder cb = em().getCriteriaBuilder();
     // CriteriaQuery<Book> cq = cb.createQuery(Book.class);
     // Root<Book> root_ = cq.from(Book.class);
     // Join<Book, Tag> join = root_.join(Book.COMMENTS,
     // JoinType.LEFT);
     // cq.select(root_);
 
     StringBuilder sql = new StringBuilder("select e from ");
     addJoin(sql);
     addWhere(sql);
     prepareQuery(sql);
     buildSearch(sql);
     buildSort(sql);
 
     TypedQuery<E> query = em().createQuery(sql.toString(), entryClass);
     if (start > 0) {
       query.setFirstResult(start);
     }
     if (limit > 0) {
       query.setMaxResults(limit);
     }
     setParams(query);
     return query.getResultList();
   }
 
   @Override
   @SuppressWarnings("unchecked")
   public E getChild(String path) {
     try {
       Class<E> entryClass = getEntryType();
       TypedQuery<E> query = em().createQuery("from " + entryClass.getName() + " e where e.parent=:parent and e.path=:path", entryClass);
       query.setParameter("parent", this);
       query.setParameter("path", path);
       return query.getSingleResult();
     } catch (NoResultException e) {
       return null;
     }
   }
 
   public long getCount() {
     return count;
   }
 
   @XmlTransient
   public abstract Class<E> getEntryType();
 
   public List<E> getItems() {
     return items;
   }
 
  public int getLimit() {
    return limit;
  }

   public String getNextPageUri() {
     if (isLastPage()) {
       return null;
     }
     Map<Character, Integer> params = getQueryPathMap();
     params.put(START_KEY, start + limit);
     return getUri(params);
   }
 
   public String getPreviousPageUri() {
     if (isFirstPage()) {
       return null;
     }
     Map<Character, Integer> params = getQueryPathMap();
     params.put(START_KEY, start - limit);
     return getUri(params);
   }
 
   @Override
   public Object getPropertyValue(String path) {
     return super.getPropertyValue(path);
   }
 
   public List<String> getSearch() {
     return search;
   }
 
   public List<String> getSort() {
     return sort;
   }
 
  public int getStart() {
    return start;
  }

   public long getTotal() {
     return total;
   }
 
   public boolean isFirstPage() {
     return start == 0;
   }
 
   public boolean isLastPage() {
     return start + limit >= count;
   }
 
   @Path(Collection.NEW)
   @GET
   public E newEntry() {
     return WebPlatform.get().getInstance(getEntryType());
   }
 
   public void prepareQuery(StringBuilder sql) {
   }
 
   public void setCount(Long count) {
     this.count = count;
   }
 
   public void setItems(List<E> items) {
     this.items = items;
   }
 
   public void setLimit(int limit) {
     this.limit = limit;
   }
 
   public void setLimit(Integer limit) {
     this.limit = limit;
   }
 
   @Override
   public void setParams(TypedQuery query) {
     super.setParams(query);
     query.setParameter("parent", this);
   }
 
   public void setSearch(List<String> search) {
     this.search = search;
   }
 
   public void setSort(List<String> sort) {
     this.sort = sort;
   }
 
   public void setStart(Integer start) {
     this.start = start;
   }
 
   public void setTotal(long total) {
     this.total = total;
   }
 
   protected E doCreate(E entry) {
     if (em().contains(entry)) {
       throw new RuntimeException("Content already created.");
     }
     total = total + 1;
     update();
     return super.doCreate(entry);
   }
 
   @Override
   protected Map<Character, Integer> getQueryPathMap() {
     Map<Character, Integer> params = super.getQueryPathMap();
     if (start != 0) {
       params.put(START_KEY, start);
     }
     if (limit != 10) {
       params.put(LIMIT_KEY, limit);
     }
     return params;
   }
 
   @Override
   protected void setQueryPath(Map<Character, Integer> params) {
     start = params.containsKey(START_KEY) ? params.get(START_KEY) : 0;
     limit = params.containsKey(LIMIT_KEY) ? params.get(LIMIT_KEY) : 10;
   }
 
   @Override
   protected void unsetQueryPath() {
     start = 0;
     limit = 10;
   }
 
 }
