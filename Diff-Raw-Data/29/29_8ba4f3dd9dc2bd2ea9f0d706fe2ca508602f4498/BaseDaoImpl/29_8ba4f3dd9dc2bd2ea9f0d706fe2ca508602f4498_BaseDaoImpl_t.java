 package com.igearbook.dao;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.hibernate.Criteria;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Projections;
 import org.hibernate.criterion.Restrictions;
 
 import com.google.common.collect.Maps;
 import com.igearbook.entities.PaginationData;
 import com.igearbook.entities.PaginationParams;
 
 public abstract class BaseDaoImpl<T> implements BaseDao<T> {
     private final Pattern SELECT_REGEX = Pattern.compile("select(.*?)from(.*)", Pattern.CASE_INSENSITIVE);
     private final Class<T> type;
 
     public Class<T> getMyType() {
         return this.type;
     }
 
     public BaseDaoImpl(final Class<T> type) {
         this.type = type;
     }
 
     protected SessionFactory sessionFactory;
 
     public void setSessionFactory(final SessionFactory sessionFactory) {
         this.sessionFactory = sessionFactory;
     }
 
     protected Session getSession() {
         return sessionFactory.getCurrentSession();
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public T get(final int id) {
         return (T) getSession().get(type, id);
     }
 
     @Override
     public void delete(final T obj) {
         final Session session = getSession();
         session.delete(obj);
         session.flush();
     }
 
     @Override
     public void add(final T obj) {
         final Session session = getSession();
         session.save(obj);
         session.flush();
     }
 
     @Override
     public void update(final T obj) {
         final Session session = getSession();
         session.update(obj);
         session.flush();
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public List<T> list() {
         return getSession().createCriteria(type).list();
     }
 
     @Override
     public PaginationData<T> doPagination(final PaginationParams params) {
         return this.doPagination(type, params);
     }
 
     protected <P> PaginationData<P> doPagination(final Class<P> theClass, final PaginationParams params) {
         final PaginationData<P> paginationData = new PaginationData<P>();
         enrichParams(paginationData, params);
         final Criteria criteria = getSession().createCriteria(theClass);
         for (final Entry<String, Object> entry : params.getQueryParams().entrySet()) {
             criteria.add(Restrictions.eq(entry.getKey(), entry.getValue()));
         }
 
         final BigDecimal recordsPerPage = BigDecimal.valueOf(params.getRecordsPerPage());
         final BigDecimal totalRecords = getTotalRecords(theClass, params);
         final int totalPages = totalRecords.divide(recordsPerPage, RoundingMode.CEILING).intValue();
         final int currentPage = BigDecimal.valueOf(params.getStart() + 1).divide(recordsPerPage, RoundingMode.CEILING).intValue();
         paginationData.setTotalRecords(totalRecords.intValue());
         paginationData.setRecordsPerPage(recordsPerPage.intValue());
         paginationData.setCurrentPage(currentPage);
         paginationData.setTotalPages(totalPages);
 
         criteria.setFirstResult(params.getStart());
         criteria.setMaxResults(params.getRecordsPerPage());
         @SuppressWarnings("unchecked")
         final List<P> list = criteria.list();
         paginationData.setList(list);
         return paginationData;
     }
 
     protected <P> PaginationData<P> doPagination(final String hql, final PaginationParams params) {
         final PaginationData<P> paginationData = new PaginationData<P>();
         enrichParams(paginationData, params);
         final Query query = getSession().createQuery(hql);
         for (final Entry<String, Object> entry : params.getQueryParams().entrySet()) {
             query.setParameter(entry.getKey(), entry.getValue());
         }
 
         final BigDecimal recordsPerPage = BigDecimal.valueOf(params.getRecordsPerPage());
         final BigDecimal totalRecords = getTotalRecords(hql, params);
         final int totalPages = totalRecords.divide(recordsPerPage, RoundingMode.CEILING).intValue();
         final int currentPage = BigDecimal.valueOf(params.getStart() + 1).divide(recordsPerPage, RoundingMode.CEILING).intValue();
         paginationData.setTotalRecords(totalRecords.intValue());
         paginationData.setRecordsPerPage(recordsPerPage.intValue());
         paginationData.setCurrentPage(currentPage);
         paginationData.setTotalPages(totalPages);
 
         query.setFirstResult(params.getStart());
         query.setMaxResults(params.getRecordsPerPage());
         @SuppressWarnings("unchecked")
         final List<P> list = query.list();
         paginationData.setList(list);
 
         return paginationData;
     }
 
     private <P> void enrichParams(final PaginationData<P> paginationData, final PaginationParams params) {
         final Map<String, String> webParams = Maps.newHashMap();
         for (final Entry<String, Object> entry : params.getWebParams().entrySet()) {
             final String key = entry.getKey();
             final Object value = entry.getValue();
             if (value instanceof Object[]) {
                 final Object[] valueArray = (Object[]) value;
                 if (valueArray.length == 1) {
                     webParams.put(key, valueArray[0].toString());
                 } else {
                     throw new UnsupportedOperationException();
                 }
             } else {
                 webParams.put(key, value.toString());
             }
         }
         paginationData.setWebParams(webParams);
     }
 
    protected BigDecimal getTotalRecords(final String hql, final PaginationParams params) {
         String countHql = hql;
         final Matcher selectMatcher = SELECT_REGEX.matcher(hql);
         if (selectMatcher.find()) {
             countHql = String.format("select count(%s) from%s", selectMatcher.group(1), selectMatcher.group(2));
         } else {
             countHql = "select count(*) " + hql;
         }
         final Query query = getSession().createQuery(countHql);
        if (params.getQueryParams() != null) {
            for (final Entry<String, Object> entry : params.getQueryParams().entrySet()) {
                query.setParameter(entry.getKey(), entry.getValue());
            }
         }

         final Long count = (Long) query.uniqueResult();
         return BigDecimal.valueOf(count);
     }
 
    protected <P> BigDecimal getTotalRecords(final Class<P> theClass, final PaginationParams params) {
         final Criteria criteria = getSession().createCriteria(theClass);
        if (params.getQueryParams() != null) {
            for (final Entry<String, Object> entry : params.getQueryParams().entrySet()) {
                criteria.add(Restrictions.eq(entry.getKey(), entry.getValue()));
            }
         }
         final Long count = (Long) criteria.setProjection(Projections.rowCount()).uniqueResult();
         return BigDecimal.valueOf(count);
     }
 
 }
