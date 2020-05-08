 package com.rockontrol.yaogan.dao;
 
 import java.util.List;
 
import org.apache.commons.lang.StringUtils;
 import org.hibernate.Query;
 import org.springframework.stereotype.Repository;
 
 import com.rockontrol.yaogan.model.Place;
 import com.rockontrol.yaogan.model.Shapefile;
 import com.rockontrol.yaogan.model.User;
 
 @Repository("shapefileDao")
 public class ShapefileDaoImpl extends BaseDaoImpl<Shapefile> implements IShapefileDao {
 
    @SuppressWarnings("unchecked")
    @Override
    public List<String> getAvailableTimesOfPlace(Long placeId) {
       Query query = getSession().getNamedQuery("Shapefile.getAvailableTimesOfPlace");
       query.setLong("placeId", placeId);
       return query.list();
    }
 
    @SuppressWarnings("unchecked")
    @Override
    public List<Place> getAvailablePlacesOfUser(Long userId, String time) {
       Query query = getSession().getNamedQuery("Shapefile.getAvailablePlacesOfUser");
       query.setLong("userId", userId);
       query.setString("time", time);
       return query.list();
    }
 
    @SuppressWarnings("unchecked")
    @Override
    public List<Place> getAvailablePlacesOfOrg(Long orgId, String time) {
       Query query = getSession().getNamedQuery("Shapefile.getAvailablePlacesOfOrg");
       query.setLong("orgId", orgId);
       query.setString("time", time);
       return query.list();
    }
 
    @SuppressWarnings("unchecked")
    @Override
    public List<String> getAvailableTimesOfOrg(Long orgId) {
       Query query = getSession().getNamedQuery("Shapefile.getAvailableTimesOfOrg");
       query.setLong("orgId", orgId);
       return query.list();
    }
 
    @SuppressWarnings("unchecked")
    @Override
    public List<String> getAvailableTimesOfUser(Long userId) {
       Query query = getSession().getNamedQuery("Shapefile.getAvailableTimesOfUser");
       query.setLong("userId", userId);
       return query.list();
    }
 
    @SuppressWarnings("unchecked")
    @Override
    public List<Shapefile> getShapefiles(Long placeId, String time) {
       Query query = getSession().getNamedQuery("Shapefile.getShapefilesByPlaceAndTime");
       query.setLong("placeId", placeId);
       query.setString("time", time);
       return query.list();
    }
 
    @Override
    public Shapefile getShapefile(Long placeId, String time, String category) {
       Query query = getSession().getNamedQuery("Shapefile.getShapefileByPTC");
       query.setLong("placeId", placeId);
       query.setString("time", time);
       query.setString("category", category);
       return (Shapefile) query.uniqueResult();
    }
 
    @Override
    public List<Shapefile> getAvailableFilesOfUser(Long userId) {
       String hql = "select file from com.rockontrol.yaogan.model.Shapefile as file,"
             + "com.rockontrol.yaogan.model.Place p,com.rockontrol.yaogan.model.UserPlace up "
             + "  where file.placeId=up.placeId and p.id=file.placeId and up.userId=:userId";
       Query query = getSession().createQuery(hql);
       query.setLong("userId", userId);
       List<Shapefile> list = query.list();
       return list;
    }
 
    @Override
    public List<Shapefile> getShapefilesOfOrg(Long orgId) {
       String hql = "select file from com.rockontrol.yaogan.model.Shapefile file "
             + ",com.rockontrol.yaogan.model.Place p where p.orgId=:orgId and file.placeId=p.id";
       Query query = getSession().createQuery(hql);
       query.setLong("orgId", orgId);
       return query.list();
    }
 
    @Override
    public long getShapefileCountOfOrg(Long orgId) {
       String hql = "select count(file) from com.rockontrol.yaogan.model.Shapefile file "
             + ",com.rockontrol.yaogan.model.Place p where p.orgId=:orgId and file.placeId=p.id";
       Query query = getSession().createQuery(hql);
       query.setLong("orgId", orgId);
       Long count = (Long) query.uniqueResult();
       return count;
    }
 
    @Override
    public List<Shapefile> getShapefilesOfOrg(Long orgId, Long placeId, String time) {
       String hql = "select file from com.rockontrol.yaogan.model.Shapefile file "
             + ",com.rockontrol.yaogan.model.Place p where file.placeId=:placeId and file.placeId=p.id and p.orgId=:orgId "
             + " and file.shootTime=:time order by file.placeId,file.shootTime desc";
       Query query = getSession().createQuery(hql);
       query.setLong("orgId", orgId);
       query.setString("time", time);
       query.setLong("placeId", placeId);
       return query.list();
    }
 
    private static final String FILTER_HQL_TEMPLATE = "select f from com.rockontrol.yaogan.model.Shapefile as f ";
 
    private String _genFilterHqlOfOrg(Long placeId, String time) {
       StringBuilder sb = new StringBuilder();
       sb.append(FILTER_HQL_TEMPLATE).append(",com.rockontrol.yaogan.model.Place as p ")
             .append(" where p.orgId=? ").append(" and f.placeId=p.id ");
       if (placeId != null) {
          sb.append(" and f.placeId =? ");
       }
      if (!StringUtils.isEmpty(time)) {
          sb.append(" and f.shootTime =? ");
       }
       return sb.toString();
    }
 
    private String _genFilterHqlOfUser(Long placeId, String time) {
       StringBuilder sb = new StringBuilder();
       sb.append(FILTER_HQL_TEMPLATE).append(",com.rockontrol.yaogan.model.Place as p ")
             .append(" ,com.rockontrol.yaogan.model.UserPlace as up ")
             .append(" where up.userId=? ").append(" and f.placeId=p.id ")
             .append(" and f.placeId=up.placeId ");
       if (placeId != null) {
          sb.append(" and f.placeId = ? ");
       }
      if (!StringUtils.isEmpty(time)) {
          sb.append(" and f.shootTime = ?");
       }
       return sb.toString();
    }
 
    private Object[] _genParams(Long id, Long placeId, String time) {
      if (placeId != null && !StringUtils.isEmpty(time)) {
          return new Object[] { id, placeId, time };
       }
       if (placeId != null) {
          return new Object[] { id, placeId };
       }
      if (!StringUtils.isEmpty(time)) {
          return new Object[] { id, time };
       }
       return null;
    }
 
    @Override
    public long getShapefileCountOfUser(User caller) {
       String hql = "select count(file) from com.rockontrol.yaogan.model.Shapefile as file,"
             + "com.rockontrol.yaogan.model.Place p,com.rockontrol.yaogan.model.UserPlace up "
             + "  where file.placeId=up.placeId and p.id=file.placeId and up.userId=:userId";
       Query query = getSession().createQuery(hql);
       query.setLong("userId", caller.getId());
       long count = (Long) query.uniqueResult();
       return count;
    }
 
    @Override
    public List<Shapefile> getShapefilesOfOrgByPage(Long orgId, int startIndex,
          int maxCount) {
       String hql = "select file from com.rockontrol.yaogan.model.Shapefile file "
             + ",com.rockontrol.yaogan.model.Place p where p.orgId=:orgId and file.placeId=p.id";
       Query query = getSession().createQuery(hql);
       query.setLong("orgId", orgId);
       query.setFirstResult(startIndex);
       query.setMaxResults(maxCount);
       return query.list();
    }
 
    @Override
    public List<Shapefile> getShapefilesOfUserByPage(Long userId, int startIndex,
          int maxCount) {
       String hql = "select file from com.rockontrol.yaogan.model.Shapefile as file,"
             + "com.rockontrol.yaogan.model.Place p,com.rockontrol.yaogan.model.UserPlace up "
             + "  where file.placeId=up.placeId and p.id=file.placeId and up.userId=:userId";
       Query query = getSession().createQuery(hql);
       query.setLong("userId", userId);
       query.setFirstResult(startIndex);
       query.setMaxResults(maxCount);
       return query.list();
    }
 
    @Override
    public List<Shapefile> filterShapefilesOfOrgByPage(Long orgId, Long placeId,
          String time, int startIndex, int maxCount) {
       String hql = "select file from com.rockontrol.yaogan.model.Shapefile file "
             + ",com.rockontrol.yaogan.model.Place p where file.placeId=:placeId and file.placeId=p.id and p.orgId=:orgId "
             + " and file.shootTime=:time order by file.placeId,file.shootTime desc";
       Query query = getSession().createQuery(hql);
       query.setLong("orgId", orgId);
       query.setString("time", time);
       query.setLong("placeId", placeId);
       query.setFirstResult(startIndex);
       query.setMaxResults(maxCount);
       return query.list();
    }
 
    @Override
    public List<Shapefile> filterShapefilesOfUserByPage(Long userId, Long placeId,
          String time, int startIndex, int maxCount) {
       String hql = "select file from com.rockontrol.yaogan.model.Shapefile as file,"
             + "com.rockontrol.yaogan.model.Place p,com.rockontrol.yaogan.model.UserPlace up "
             + "  where file.placeId=:placeId and file.placeId=up.placeId and p.id=file.placeId and up.userId=:userId and file.shootTime=:time "
             + "order by file.placeId,file.shootTime desc";
       Query query = getSession().createQuery(hql);
       query.setLong("userId", userId);
       query.setString("time", time);
       query.setLong("placeId", placeId);
       query.setFirstResult(startIndex);
       query.setMaxResults(maxCount);
       return query.list();
    }
 
    @Override
    public long filterCountOfOrg(Long orgId, Long placeId, String time) {
       String hql = _genFilterHqlOfOrg(placeId, time);
       Object[] params = _genParams(orgId, placeId, time);
       return findByHQL(hql, params).size();
    }
 
    @Override
    public long filterCountOfUser(Long userId, Long placeId, String time) {
       String hql = _genFilterHqlOfOrg(placeId, time);
       Object[] params = _genParams(userId, placeId, time);
       return findByHQL(hql, params).size();
    }
 
    @Override
    public List<Shapefile> filterOfOrgByPage(Long orgId, Long placeId, String time,
          int startIndex, int maxCount) {
       String hql = _genFilterHqlOfOrg(placeId, time);
       Object[] params = _genParams(orgId, placeId, time);
       return findByPage(hql, startIndex, maxCount, params);
    }
 
    @Override
    public List<Shapefile> filterOfUserByPage(Long UserId, Long placeId, String time,
          int startIndex, int maxCount) {
       String hql = _genFilterHqlOfOrg(placeId, time);
       Object[] params = _genParams(UserId, placeId, time);
       return findByPage(hql, startIndex, maxCount, params);
    }
 
 }
