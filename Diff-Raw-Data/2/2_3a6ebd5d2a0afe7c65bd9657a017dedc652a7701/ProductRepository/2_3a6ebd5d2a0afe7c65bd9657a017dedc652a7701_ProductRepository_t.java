 package org.giavacms.catalogue.repository;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.ejb.LocalBean;
 import javax.ejb.Stateless;
 import javax.inject.Named;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.Query;
 
 import org.giavacms.base.common.util.HtmlUtils;
 import org.giavacms.base.model.Page;
 import org.giavacms.base.model.attachment.Document;
 import org.giavacms.base.model.attachment.Image;
 import org.giavacms.base.repository.AbstractPageRepository;
 import org.giavacms.catalogue.model.Category;
 import org.giavacms.catalogue.model.Product;
 import org.giavacms.common.model.Search;
 
 @Named
 @Stateless
 @LocalBean
 public class ProductRepository extends AbstractPageRepository<Product>
 {
 
    private static final long serialVersionUID = 1L;
 
    @PersistenceContext
    EntityManager em;
 
    @Override
    protected String getDefaultOrderBy()
    {
       return "code asc";
    }
 
    @Override
    public int getListSize(Search<Product> search)
    {
       // parameters map - the same in both getList() and getListSize() usage
       Map<String, Object> params = new HashMap<String, Object>();
       // a flag to drive native query construction
       boolean count = true;
       // a flag to tell native query whether to fetch all additional fields
       boolean completeFetch = false;
       // the native query
       StringBuffer string_query = getListNative(search, params, count, 0, 0, completeFetch);
       Query query = getEm().createNativeQuery(string_query.toString());
       // substituition of parameters
       for (String param : params.keySet())
       {
          query.setParameter(param, params.get(param));
       }
       // result extraction
       return ((BigInteger) query.getSingleResult()).intValue();
    }
 
    @Override
    public List<Product> getList(Search<Product> search, int startRow,
             int pageSize)
    {
       // parameters map - the same in both getList() and getListSize() usage
       Map<String, Object> params = new HashMap<String, Object>();
       // a flag to drive native query construction
       boolean count = false;
       // a flag to tell native query whether to fetch all additional fields
       boolean completeFetch = false;
       // the native query
       StringBuffer stringbuffer_query = getListNative(search, params, count, startRow, pageSize, completeFetch);
       Query query = getEm().createNativeQuery(stringbuffer_query.toString());
       // substituition of parameters
       for (String param : params.keySet())
       {
          query.setParameter(param, params.get(param));
       }
       // result extraction
       return extract(query.getResultList(), completeFetch);
    }
 
    /**
     * In case of a main table with one-to-many collections to fetch at once
     * 
     * we need an external query to read results and an internal query to apply parameters and paginate results
     * 
     * we need just the external query to apply parameters and count the overall distinct results
     */
    protected StringBuffer getListNative(Search<Product> search, Map<String, Object> params, boolean count,
             int startRow, int pageSize, boolean completeFetch)
    {
 
       // aliases to use in the external query
       String pageAlias = "P";
       String productAlias = "F";
       String categoryAlias = "FC";
       String categoryPageAlias = "FCP";
       String imageAlias = "I";
       String documentAlias = "D";
 
       // query string buffer
       StringBuffer sb = new StringBuffer(
                "SELECT ");
 
       if (count)
       {
          // we only count distinct results in case of count = true
          sb.append(" count( distinct ").append(pageAlias).append(".id ) ");
       }
       else
       {
          // we select a cartesian product of master/details rows in case of count = false
          sb.append(pageAlias).append(".id, ");
          sb.append(pageAlias).append(".title, ");
          sb.append(pageAlias).append(".description, ");
          sb.append(productAlias).append(".preview, ");
          sb.append(productAlias).append(".dimensions, ");
          sb.append(productAlias).append(".code, ");
          sb.append(productAlias).append(".category_id, ");
          sb.append(categoryPageAlias).append(".title AS categoryTitle, ");
          sb.append(imageAlias).append(".fileName AS image, ");
          sb.append(documentAlias).append(".fileName AS document ");
          if (completeFetch)
          {
             // additional fields to retrieve only when fetching
          }
       }
 
       // master-from clause is the same in both count = true and count = false
       sb.append(" FROM ").append(Product.TABLE_NAME).append(" AS ").append(productAlias);
       sb.append(" LEFT JOIN ").append(Category.TABLE_NAME).append(" AS ").append(categoryAlias)
                .append(" ON ( ").append(categoryAlias).append(".id = ").append(productAlias)
                .append(".category_id ) ");
       sb.append(" LEFT JOIN ").append(Page.TABLE_NAME).append(" as ").append(pageAlias).append(" on ( ")
                .append(productAlias).append(".id = ")
                .append(pageAlias).append(".id ) ");
       sb.append(" LEFT JOIN ").append(Page.TABLE_NAME).append(" as ").append(categoryPageAlias).append(" on ( ")
                .append(categoryAlias).append(".id = ")
                .append(categoryPageAlias).append(".id ) ");
 
       if (count)
       {
          // we optimize the count query by avoiding useless left joins
       }
       else
       {
          // we need details-from clause in case of count = false
          if (Product.HAS_DETAILS)
          {
             sb.append(" LEFT JOIN ").append(Product.TABLE_NAME).append("_").append(Document.TABLE_NAME)
                      .append(" AS RD ON ( RD.").append(Product.TABLE_NAME).append("_id = ")
                      .append(productAlias)
                      .append(".id ) ");
             sb.append(" LEFT JOIN ").append(Document.TABLE_NAME).append(" AS ").append(documentAlias)
                      .append(" ON ( RD.documents_id = ").append(documentAlias)
                      .append(".id ) ");
             sb.append(" LEFT JOIN ").append(Product.TABLE_NAME).append("_").append(Image.TABLE_NAME)
                      .append(" AS RI ON ( RI.").append(Product.TABLE_NAME).append("_id = ")
                      .append(productAlias)
                      .append(".id ) ");
             sb.append(" LEFT JOIN ").append(Image.TABLE_NAME).append(" as ").append(imageAlias)
                      .append(" on ( ").append(imageAlias)
                      .append(".id = RI.images_id ) ");
          }
       }
 
       String innerPageAlias = null;
       String innerProductAlias = null;
       String innerCategoryAlias = null;
       String innerCategoryPageAlias = null;
       if (count)
       {
          // we don't need an inner query in case of count = true (because we only need distinct id to count,
          // disregarding result pagination) so aliases stay the same
          innerPageAlias = pageAlias;
          innerProductAlias = productAlias;
          innerCategoryAlias = categoryAlias;
         innerCategoryPageAlias = categoryPageAlias;
       }
       else if (Product.HAS_DETAILS)
       {
          // we need different aliases for the inner query in case of count = false or multiple detail rows for each
          // master
          innerPageAlias = "P1";
          innerProductAlias = "D1";
          innerCategoryAlias = "C1";
          innerCategoryPageAlias = "CP1";
          // inner query comes as an inner join, because mysql does not support limit in subquerys
          sb.append(" INNER JOIN ");
 
          // this is the opening bracket for the internal query
          sb.append(" ( ");
 
          sb.append(" select distinct ").append(innerPageAlias)
                   .append(".id from ");
          sb.append(Product.TABLE_NAME).append(" AS ").append(innerProductAlias);
          sb.append(" LEFT JOIN ").append(Product.TABLE_NAME).append(" AS ").append(innerCategoryAlias)
                   .append(" ON ( ").append(innerCategoryAlias).append(".id = ").append(innerProductAlias)
                   .append(".category_id ) ");
          sb.append(" LEFT JOIN ").append(Page.TABLE_NAME).append(" as ").append(innerPageAlias).append(" on ( ")
                   .append(innerProductAlias).append(".id = ")
                   .append(innerPageAlias).append(".id ) ");
          sb.append(" LEFT JOIN ").append(Page.TABLE_NAME).append(" as ").append(innerCategoryPageAlias)
                   .append(" on ( ")
                   .append(innerCategoryAlias).append(".id = ")
                   .append(innerCategoryPageAlias).append(".id ) ");
       }
       else
       {
          // we also don't need an inner query in case of master-data that has no multiple details
          // so aliases can stay the same
          innerPageAlias = pageAlias;
          innerProductAlias = productAlias;
          innerCategoryAlias = categoryAlias;
          innerCategoryPageAlias = categoryPageAlias;
       }
 
       // we append filters right after the latest query, so that they apply to the external one in case count = true and
       // to the internal one in case count = false
       String separator = " where ";
       applyRestrictionsNative(search, innerPageAlias, innerProductAlias, innerCategoryAlias, innerCategoryPageAlias,
                separator, sb,
                params);
 
       if (count)
       {
          // if we only need to count distinct results, we are over!
       }
       else
       {
          // we need to sort internal results to apply pagination
          sb.append(" order by ").append(innerProductAlias).append(".code asc ");
          
          // we apply limit-clause only if we want pagination
          if (pageSize > 0)
          {
             sb.append(" limit ").append(startRow).append(", ").append(pageSize).toString();
          }
 
          if (Product.HAS_DETAILS)
          {
             // this is the closing bracket for the internal query
             sb.append(" ) ");
             // this is where external id selection applies
             sb.append(" as IN2 ON ").append(pageAlias).append(".ID = IN2.ID ");
             // we also need to sort external results to keep result order within each results page
             sb.append(" order by ").append(productAlias).append(".code asc ");
             sb.append(", ").append(imageAlias).append(".id asc ");
             sb.append(", ").append(documentAlias).append(".id asc ");
          }
 
       }
       return sb;
    }
 
    protected void applyRestrictionsNative(Search<Product> search, String pageAlias, String productAlias,
             String categoryAlias, String categoryPageAlias, String separator, StringBuffer sb,
             Map<String, Object> params)
    {
 
       if (true)
       {
          sb.append(separator).append(productAlias)
                   .append(".category_id is not null ");
          separator = " and ";
          sb.append(separator).append(categoryPageAlias)
                   .append(".active = :activeCategory");
          params.put("activeCategory", true);
          separator = " and ";
       }
 
       // CATEGORY NAME
       if (search.getObj().getCategory() != null
                && search.getObj().getCategory().getTitle() != null
                && search.getObj().getCategory().getTitle().trim().length() > 0)
       {
          sb.append(separator).append(categoryPageAlias)
                   .append(".title = :NAMECAT ");
          params.put("NAMECAT", search.getObj().getCategory().getTitle());
          separator = " and ";
       }
 
       // CATEGORY ID
       if (search.getObj().getCategory() != null
                && search.getObj().getCategory().getId() != null
                && search.getObj().getCategory().getId().trim().length() > 0)
       {
          sb.append(separator).append(categoryAlias).append(".id = :IDCAT ");
          params.put("IDCAT", search.getObj().getCategory().getId());
          separator = " and ";
       }
 
       // CODE
       if (search.getObj().getCode() != null
                && !search.getObj().getCode().isEmpty())
       {
          sb.append(separator).append(productAlias).append(".code = :CODE ");
          params.put("CODE", search.getObj().getCode());
          separator = " and ";
       }
 
       // NAME OR DESCRIPTION
       // TITLE --> ALSO SEARCH IN CUSTOM FIELDS
       String customLike = null;
       if (search.getObj().getTitle() != null
                && !search.getObj().getTitle().trim().isEmpty())
       {
          customLike = " upper ( " + pageAlias
                   + ".description ) like :LIKETEXTCUSTOM ";
          params.put("LIKETEXTCUSTOM", likeParam(search.getObj().getTitle().trim().toUpperCase()));
       }
       super.applyRestrictionsNative(search, pageAlias, separator, sb, params, customLike);
 
    }
 
    @Override
    protected Product prePersist(Product n)
    {
       n.setClone(true);
       n.setDescription(HtmlUtils.normalizeHtml(n.getDescription()));
       n = super.prePersist(n);
       return n;
    }
 
    @Override
    protected Product preUpdate(Product n)
    {
       n.setClone(true);
       n.setDescription(HtmlUtils.normalizeHtml(n.getDescription()));
       n = super.preUpdate(n);
       return n;
    }
 
    /**
     * sb.append(pageAlias).append(".id, "); sb.append(pageAlias).append(".title, ");
     * sb.append(pageAlias).append(".description, "); sb.append(productAlias).append(".preview, ");
     * sb.append(productAlias).append(".dimensions, "); sb.append(productAlias).append(".code, ");
     * sb.append(productAlias).append(".category_id, ");
     * sb.append(categoryPageAlias).append(".title AS categoryTitle, "); sb.append(" I.fileName AS image, ");
     * sb.append(" D.fileName AS document ");
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected List<Product> extract(List resultList, boolean completeFetch)
    {
       Product product = null;
       Map<String, Set<String>> imageNames = new HashMap<String, Set<String>>();
       Map<String, Set<String>> documentNames = new HashMap<String, Set<String>>();
       Map<String, Product> products = new LinkedHashMap<String, Product>();
 
       Iterator<Object[]> results = resultList.iterator();
       while (results.hasNext())
       {
          product = new Product();
          Object[] row = results.next();
          int i = 0;
          String id = (String) row[i];
          product.setId(id);
          i++;
          String title = (String) row[i];
          product.setTitle(title);
          i++;
          String description = (String) row[i];
          product.setDescription(description);
          i++;
          String preview = (String) row[i];
          product.setPreview(preview);
          i++;
          String dimensions = (String) row[i];
          product.setDimensions(dimensions);
          i++;
          String code = (String) row[i];
          product.setCode(code);
          i++;
          String category_id = (String) row[i];
          product.getCategory().setId(category_id);
          i++;
          String category_title = (String) row[i];
          product.getCategory().setTitle(category_title);
          i++;
          String imagefileName = (String) row[i];
          if (imagefileName != null && !imagefileName.isEmpty())
          {
             if (imageNames.containsKey(id))
             {
                HashSet<String> set = (HashSet<String>) imageNames.get(id);
                set.add(imagefileName);
             }
             else
             {
                HashSet<String> set = new HashSet<String>();
                set.add(imagefileName);
                imageNames.put(id, set);
             }
          }
          i++;
          String documentfileName = (String) row[i];
          if (documentfileName != null && !documentfileName.isEmpty())
          {
             if (documentNames.containsKey(id))
             {
                HashSet<String> set = (HashSet<String>) documentNames.get(id);
                set.add(documentfileName);
             }
             else
             {
                HashSet<String> set = new HashSet<String>();
                set.add(documentfileName);
                documentNames.put(id, set);
             }
          }
          i++;
          if (completeFetch)
          {
             // extract additional fields
          }
          if (!products.containsKey(id))
          {
             products.put(id, product);
          }
 
       }
       for (String id : documentNames.keySet())
       {
          Product prod = null;
          if (products.containsKey(id))
          {
             prod = products.get(id);
             Set<String> docs = documentNames.get(id);
             for (String docFileName : docs)
             {
                Document document = new Document();
                document.setFilename(docFileName);
                prod.addDocument(document);
             }
          }
          else
          {
             logger.error("ERROR - DOCS CYCLE cannot find id:" + id);
          }
 
       }
       for (String id : imageNames.keySet())
       {
          Product prod = null;
          if (products.containsKey(id))
          {
             prod = products.get(id);
             Set<String> docs = imageNames.get(id);
             for (String imgFileName : docs)
             {
                Image image = new Image();
                image.setFilename(imgFileName);
                prod.addImage(image);
             }
          }
          else
          {
             logger.error("ERROR - IMGS CYCLE cannot find id:" + id);
          }
 
       }
       return new ArrayList<Product>(products.values());
    }
 
    @Override
    public Product fetch(Object key)
    {
       try
       {
          Search<Product> sp = new Search<Product>(Product.class);
          sp.getObj().setId(key.toString());
          return getList(sp, 0, 1).get(0);
       }
       catch (Exception e)
       {
          logger.error(e.getMessage(), e);
          return null;
       }
    }
 }
