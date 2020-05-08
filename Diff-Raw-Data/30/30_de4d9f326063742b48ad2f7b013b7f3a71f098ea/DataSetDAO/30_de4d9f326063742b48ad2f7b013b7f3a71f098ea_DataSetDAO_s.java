 package de.peterspan.csv2db.domain.dao;
 
 import java.util.List;
 
 import org.hibernate.Criteria;
 import org.hibernate.Session;
 import org.hibernate.criterion.Disjunction;
 import org.hibernate.criterion.MatchMode;
 import org.hibernate.criterion.Restrictions;
 
 import de.peterspan.csv2db.domain.entities.DataSet;
 import de.peterspan.csv2db.domain.entities.Location;
 import de.peterspan.csv2db.domain.entities.MeasurementValues;
 
 /**
  * This class provides all default operations that are derived from the HEDL entity model
  * for type DataSet.
  *
  * Note: This class is generated. Any change will be overridden.
  */
 public class DataSetDAO {
 	
 	public final static String FIELD__ID = getField(DataSet.class, "id");
 	public final static String FIELD__ACQUISITIONDATE = getField(DataSet.class, "acquisitionDate");
 	public final static String FIELD__STANDINGYEAR = getField(DataSet.class, "standingYear");
 	public final static String FIELD__ROTATION = getField(DataSet.class, "rotation");
 	public final static String FIELD__CLONE = getField(DataSet.class, "clone");
 	public final static String FIELD__REPETITION = getField(DataSet.class, "repetition");
 	public final static String FIELD__TREENUMBER = getField(DataSet.class, "treeNumber");
 	public final static String FIELD__IDENTNUMBER = getField(DataSet.class, "identNumber");
 	public final static String FIELD__LOCATION = getField(DataSet.class, "location");
 	public final static String FIELD__MEASUREMENTVALUES = getField(DataSet.class, "measurementValues");
 	public final static String FIELD__REMARK = getField(DataSet.class, "remark");
 	
 	/**
 	 * Creates a DataSet using all read-only and all non-null properties.
 	 */
 	public DataSet create(Session session, java.util.Date acquisitionDate, int standingYear, int rotation, int clone, int repetition, int treeNumber, java.lang.String identNumber) {
 		DataSet newEntity = new DataSet(acquisitionDate, standingYear, rotation, clone, repetition, treeNumber, identNumber);
 		session.save(newEntity);
 		return newEntity;
 	}
 	
 	/**
 	 * Returns the DataSet with the given id.
 	 */
 	public DataSet get(Session session, int id) {
 		DataSet entity = (DataSet) session.get(DataSet.class, id);
 		return entity;
 	}
 	
 	/**
 	 * Returns the DataSets with the given location.
 	 */
 	public List<DataSet> getByLocation(Session session, Location location) {
 		Criteria criteria = session.createCriteria(DataSet.class);
 		criteria = criteria.add(Restrictions.eq(FIELD__LOCATION, location));
 		@SuppressWarnings("unchecked")
 		List<DataSet> list = (List<DataSet>) criteria.list();
 		return list;
 	}
 	
 	/**
 	 * Returns the DataSets with the given measurementValues.
 	 */
 	public List<DataSet> getByMeasurementValues(Session session, MeasurementValues measurementValues) {
 		Criteria criteria = session.createCriteria(DataSet.class);
 		criteria = criteria.add(Restrictions.eq(FIELD__MEASUREMENTVALUES, measurementValues));
 		@SuppressWarnings("unchecked")
 		List<DataSet> list = (List<DataSet>) criteria.list();
 		return list;
 	}
 	
 	/**
 	 * Returns all DataSets where acquisitionDate is set to a value before '_maxDate'.
 	 */
 	public List<DataSet> getAcquisitionDateBefore(Session session, java.util.Date _maxDate) {
 		Criteria criteria = session.createCriteria(DataSet.class);
 		criteria = criteria.add(Restrictions.le(FIELD__ACQUISITIONDATE, _maxDate));
 		List<?> list = criteria.list();
 		@SuppressWarnings("unchecked")
 		List<DataSet> entities = (List<DataSet>) list;
 		return entities;
 	}
 	
 	/**
 	 * Returns all DataSets where acquisitionDate is set to a value after '_minDate'.
 	 */
 	public List<DataSet> getAcquisitionDateAfter(Session session, java.util.Date _minDate) {
 		Criteria criteria = session.createCriteria(DataSet.class);
 		criteria = criteria.add(Restrictions.ge(FIELD__ACQUISITIONDATE, _minDate));
 		List<?> list = criteria.list();
 		@SuppressWarnings("unchecked")
 		List<DataSet> entities = (List<DataSet>) list;
 		return entities;
 	}
 	
 	/**
 	 * Returns all entities of type DataSet.
 	 */
 	public List<DataSet> getAll(Session session) {
 		Criteria criteria = session.createCriteria(DataSet.class);
 		@SuppressWarnings("unchecked")
 		List<DataSet> entities = (List<DataSet>) criteria.list();
 		return entities;
 	}
 	
 	/**
 	 * Searches for entities of type DataSet.
 	 */
 	public List<DataSet> search(Session _session, String _searchString, int _maxResults) {
 		Criteria criteria = _session.createCriteria(DataSet.class);
 		Disjunction disjunction = Restrictions.disjunction();
 		disjunction.add(Restrictions.like(FIELD__IDENTNUMBER, _searchString.trim(), MatchMode.ANYWHERE));
 		disjunction.add(Restrictions.like(FIELD__REMARK, _searchString.trim(), MatchMode.ANYWHERE));
 		criteria = criteria.add(disjunction);
 		criteria = criteria.setMaxResults(_maxResults);
 		@SuppressWarnings("unchecked")
 		List<DataSet> entities = (List<DataSet>) criteria.list();
 		return entities;
 	}
 	
 	/**
 	 * Searches for entities of type DataSet.
 	 */
 	public List<DataSet> searchWithLocation(Session _session, Location location, String _searchString, int _maxResults) {
 		Criteria criteria = _session.createCriteria(DataSet.class);
 		// restrict by the value of the unique property
 		criteria = criteria.add(Restrictions.eq(FIELD__LOCATION, location));
 		Disjunction disjunction = Restrictions.disjunction();
 		disjunction.add(Restrictions.like(FIELD__IDENTNUMBER, _searchString.trim(), MatchMode.ANYWHERE));
 		disjunction.add(Restrictions.like(FIELD__REMARK, _searchString.trim(), MatchMode.ANYWHERE));
 		criteria = criteria.add(disjunction);
 		criteria = criteria.setMaxResults(_maxResults);
 		@SuppressWarnings("unchecked")
 		List<DataSet> entities = (List<DataSet>) criteria.list();
 		return entities;
 	}
 	
 	/**
 	 * Searches for entities of type DataSet.
 	 */
 	public List<DataSet> searchWithMeasurementValues(Session _session, MeasurementValues measurementValues, String _searchString, int _maxResults) {
 		Criteria criteria = _session.createCriteria(DataSet.class);
 		// restrict by the value of the unique property
 		criteria = criteria.add(Restrictions.eq(FIELD__MEASUREMENTVALUES, measurementValues));
 		Disjunction disjunction = Restrictions.disjunction();
 		disjunction.add(Restrictions.like(FIELD__IDENTNUMBER, _searchString.trim(), MatchMode.ANYWHERE));
 		disjunction.add(Restrictions.like(FIELD__REMARK, _searchString.trim(), MatchMode.ANYWHERE));
 		criteria = criteria.add(disjunction);
 		criteria = criteria.setMaxResults(_maxResults);
 		@SuppressWarnings("unchecked")
 		List<DataSet> entities = (List<DataSet>) criteria.list();
 		return entities;
 	}
 	
 	/**
 	 * Deletes a DataSet.
 	 */
 	public void delete(Session session, DataSet entity) {
 		session.delete(entity);
 	}
 	
 	/**
 	 * Counts the number of DataSet entities.
 	 */
 	public int count(Session session) {
 		return ((Long) session.createQuery("select count(*) from DataSet").uniqueResult()).intValue();
 	}
 		
 	private static String getField(Class<?> clazz, String fieldName) {
 		try {
 			return clazz.getDeclaredField(fieldName).getName();
 		} catch (SecurityException e) {
 			throw new RuntimeException(e.getClass().getSimpleName() + ": " + e.getMessage());
 		} catch (NoSuchFieldException e) {
 			throw new RuntimeException(e.getClass().getSimpleName() + ": " + e.getMessage());
 		}
 	}
 }
