 package org.openmrs.module.addresshierarchy.db.hibernate;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.Criteria;
 import org.hibernate.Hibernate;
 import org.hibernate.SQLQuery;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Expression;
 import org.hibernate.criterion.Projections;
 import org.hibernate.criterion.Restrictions;
 import org.openmrs.Patient;
 import org.openmrs.PersonAddress;
 import org.openmrs.api.db.DAOException;
 import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
 import org.openmrs.module.addresshierarchy.AddressHierarchyLevel;
 import org.openmrs.module.addresshierarchy.AddressToEntryMap;
 import org.openmrs.module.addresshierarchy.db.AddressHierarchyDAO;
 import org.openmrs.module.addresshierarchy.exception.AddressHierarchyModuleException;
 
 /**
  * The Class HibernateAddressHierarchyDAO which links to the tables address_hierarchy,
  * address_hierarchy_type and person_address. This class does the functions of storing and
  * retrieving addresses.
  */
 public class HibernateAddressHierarchyDAO implements AddressHierarchyDAO {
 	
 	protected final Log log = LogFactory.getLog(getClass());
 	
 	/**
 	 * Hibernate session factory
 	 */
 	private SessionFactory sessionFactory;
 	
 	public void setSessionFactory(SessionFactory sessionFactory) {
 		this.sessionFactory = sessionFactory;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public int getAddressHierarchyEntryCount() {
 		int x = 0;
 		Session session = sessionFactory.getCurrentSession();
 		Criteria c = session.createCriteria(AddressHierarchyEntry.class);
 		List<Number> rows = c.setProjection((Projections.rowCount())).list();
 		if (rows.size() > 0) {
 			x = rows.get(0).intValue();
 		}
 		return x;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public int getAddressHierarchyEntryCountByLevel(AddressHierarchyLevel level) {
 		int x = 0;
 		Session session = sessionFactory.getCurrentSession();
 		Criteria criteria = session.createCriteria(AddressHierarchyEntry.class);
 		criteria.createCriteria("level").add(Restrictions.eq("levelId", level.getId()));
 		List<Number> rows = criteria.setProjection((Projections.rowCount())).list();
 		if (rows.size() > 0) {
 			x = rows.get(0).intValue();
 		}
 		return x;	
 	}
 	
 	public AddressHierarchyEntry getAddressHierarchyEntry(int addressHierarchyEntryId) {
 		Session session = sessionFactory.getCurrentSession();
 		AddressHierarchyEntry ah = (AddressHierarchyEntry) session.load(AddressHierarchyEntry.class, addressHierarchyEntryId);
 		return ah;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public AddressHierarchyEntry getAddressHierarchyEntryByUserGenId(String userGeneratedId) {
 		AddressHierarchyEntry ah = null;
 		Session session = sessionFactory.getCurrentSession();
 		Criteria criteria = session.createCriteria(AddressHierarchyEntry.class);
 		
 		List<AddressHierarchyEntry> list = criteria.add(Restrictions.eq("userGeneratedId", userGeneratedId)).list();
 		if (list != null && list.size() > 0) {
 			ah = list.get(0);
 		}
 		return ah;
 	}
 	
 	@SuppressWarnings("unchecked")
     public List<AddressHierarchyEntry> getAddressHierarchyEntriesByLevel(AddressHierarchyLevel addressHierarchyLevel) {
 		Session session = sessionFactory.getCurrentSession();
 		Criteria criteria = session.createCriteria(AddressHierarchyEntry.class);
 		criteria.createCriteria("level").add(Restrictions.eq("levelId", addressHierarchyLevel.getId()));
 		return criteria.list();
 	}
 	
 	@SuppressWarnings("unchecked")
     public List<AddressHierarchyEntry> getAddressHierarchyEntriesByLevelAndName(AddressHierarchyLevel addressHierarchyLevel, String name) {
 		Session session = sessionFactory.getCurrentSession();
 		Criteria criteria = session.createCriteria(AddressHierarchyEntry.class);
 		criteria.createCriteria("level").add(Restrictions.eq("levelId", addressHierarchyLevel.getId()));
 		criteria.add(Restrictions.eq("name", name).ignoreCase());
 		return criteria.list();
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<AddressHierarchyEntry> getAddressHierarchyEntriesByLevelAndNameAndParent(AddressHierarchyLevel addressHierarchyLevel, String name, AddressHierarchyEntry parent) {
 		Session session = sessionFactory.getCurrentSession();
 		Criteria criteria = session.createCriteria(AddressHierarchyEntry.class);
 		criteria.createCriteria("level").add(Restrictions.eq("levelId", addressHierarchyLevel.getId()));
 		criteria.createCriteria("parent").add(Restrictions.eq("addressHierarchyEntryId", parent.getId()));
 		criteria.add(Restrictions.eq("name", name).ignoreCase());
 		return criteria.list();
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<AddressHierarchyEntry> getChildAddressHierarchyEntries(AddressHierarchyEntry entry) {
 		Session session = sessionFactory.getCurrentSession();
 		Criteria criteria = session.createCriteria(AddressHierarchyEntry.class);
 		List<AddressHierarchyEntry> list = criteria.createCriteria("parent").add(
 		    Restrictions.eq("addressHierarchyEntryId", entry.getId())).list();
 		return list;
 	}
 	
 	public AddressHierarchyEntry getChildAddressHierarchyEntryByName(AddressHierarchyEntry entry, String childName) {
 		Session session = sessionFactory.getCurrentSession();
 		Criteria criteria = session.createCriteria(AddressHierarchyEntry.class);
 		criteria.createCriteria("parent").add(Restrictions.eq("addressHierarchyEntryId", entry.getId()));
 		criteria.add(Restrictions.eq("name", childName).ignoreCase());  // do a case-insensitive match
 		// this will throw an exception if we don't get a unique result--entries should always be unique on parent and name
 		return (AddressHierarchyEntry) criteria.uniqueResult();    
 	}
 	
 	public void saveAddressHierarchyEntry(AddressHierarchyEntry ah) {
 		try {
 			Session session = sessionFactory.getCurrentSession();
 			session.saveOrUpdate(ah);
 		}
 		catch (Throwable t) {
 			throw new DAOException(t);
 		}
 	}
 	
 	public void deleteAllAddressHierarchyEntries() {
 		Session session = sessionFactory.getCurrentSession();
 		
 		// cycle through all the top-level entries and delete them; the rest should be deleted via cascade
 		// note that I haven't been able to figure out how to have this cascade work on the hibernate level,
 		// so I have defined it at the database level in mysql; therefore, the unit test for this doesn't work
 		
		for (AddressHierarchyEntry entry : getAddressHierarchyEntriesByLevel(getTopAddressHierarchyLevel())) {
			session.delete(entry);
		}
 		
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<AddressHierarchyLevel> getAddressHierarchyLevels() {
 		Session session = sessionFactory.getCurrentSession();
 		Criteria criteria = session.createCriteria(AddressHierarchyLevel.class);
 		return criteria.list();
 	}
 	
 	public AddressHierarchyLevel getTopAddressHierarchyLevel() {
 		Session session = sessionFactory.getCurrentSession();
 		Criteria criteria = session.createCriteria(AddressHierarchyLevel.class);
 		criteria.add(Restrictions.isNull("parent"));
 		
 		AddressHierarchyLevel topLevel = null;
 		
 		try {
 			topLevel = (AddressHierarchyLevel) criteria.uniqueResult();
 		}
 		catch (Exception e) {
 			throw new AddressHierarchyModuleException("Unable to fetch top level address hierarchy type", e);
 		}
 		
 		return topLevel;
 	}
 	
 	public AddressHierarchyLevel getAddressHierarchyLevel(int levelId) {
 		Session session = sessionFactory.getCurrentSession();
 		AddressHierarchyLevel type = (AddressHierarchyLevel) session.load(AddressHierarchyLevel.class, levelId);
 		return type;
 	}
 	
     public AddressHierarchyLevel getAddressHierarchyLevelByParent(AddressHierarchyLevel parent) {
     	Session session = sessionFactory.getCurrentSession();
     	Criteria criteria = session.createCriteria(AddressHierarchyLevel.class);
     	criteria.add(Restrictions.eq("parent", parent));
     	
     	AddressHierarchyLevel child = null;
 		
 		try {
 			child = (AddressHierarchyLevel) criteria.uniqueResult();
 		}
 		catch (Exception e) {
 			throw new AddressHierarchyModuleException("Unable to fetch child address hierarchy type", e);
 		}
 		
 		return child;
     }
 	
 	public void saveAddressHierarchyLevel(AddressHierarchyLevel level) {
 		try {
 			Session session = sessionFactory.getCurrentSession();
 			session.saveOrUpdate(level);
 		}
 		catch (Throwable t) {
 			throw new DAOException(t);
 		}
 	}
 	
 
     public void deleteAddressHierarchyLevel(AddressHierarchyLevel level) {
     	try {
     		Session session = sessionFactory.getCurrentSession();
 			session.delete(level);
 		}
 		catch (Throwable t) {
 			throw new DAOException(t);
 		}
     }
 	
 	public AddressToEntryMap getAddressToEntryMap(int id) {
 		Session session = sessionFactory.getCurrentSession();
 		AddressToEntryMap result = (AddressToEntryMap) session.load(AddressToEntryMap.class, id);
 		return result;
     }
 
 	public void saveAddressToEntryMap(AddressToEntryMap addressToEntryMap) {
 		try {
 			Session session = sessionFactory.getCurrentSession();
 			session.saveOrUpdate(addressToEntryMap);
 		}
 		catch (Throwable t) {
 			throw new DAOException(t);
 		}
     }
 	
 	public void deleteAddressToEntryMap(AddressToEntryMap addressToEntryMap) {
 		try {
 			Session session = sessionFactory.getCurrentSession();
 			session.delete(addressToEntryMap);
 		}
 		catch (Throwable t) {
 			throw new DAOException(t);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
     public List<AddressToEntryMap> getAddressToEntryMapByPersonAddress(PersonAddress address) {
 		Session session = sessionFactory.getCurrentSession();
     	Criteria criteria = session.createCriteria(AddressToEntryMap.class);
 		criteria.createCriteria("address").add(Restrictions.eq("personAddressId", address.getId()));
     	return criteria.list();
     }
 	
 	@SuppressWarnings("unchecked")
     public List<Patient> findAllPatientsWithDateChangedAfter(Date date) {
 		Session session = sessionFactory.getCurrentSession();
     	Criteria criteria = session.createCriteria(Patient.class);
     	criteria.add(Expression.ge("dateChanged",date) );
     	criteria.add(Expression.eq("voided", false));
 	    return criteria.list();
     }
 
 	
 	/**
 	 * The following methods are deprecated and just exist to provide backwards compatibility to
 	 * Rwanda Address Hierarchy module
 	 */
 	
 	@Deprecated
 	public void associateCoordinates(AddressHierarchyEntry ah, double latitude, double longitude) {
 		ah.setLatitude(latitude);
 		ah.setLongitude(longitude);
 		Session session = sessionFactory.getCurrentSession();
 		session.update(ah);
 	}
 	
 	
 	@Deprecated
 	public List<AddressHierarchyEntry> getLeafNodes(AddressHierarchyEntry ah) {
 		List<AddressHierarchyEntry> leafList = new ArrayList<AddressHierarchyEntry>();
 		getLowestLevel(ah, leafList);
 		return leafList;
 	}
 	
 	// Recursively finds leaf nodes of ah 
 	@Deprecated
 	private List<AddressHierarchyEntry> getLowestLevel(AddressHierarchyEntry ah, List<AddressHierarchyEntry> leafList) {
 		List<AddressHierarchyEntry> children = getChildAddressHierarchyEntries(ah);
 		if (children.size() > 0) {
 			for (AddressHierarchyEntry addressHierarchy : children) {
 				getLowestLevel(addressHierarchy, leafList);
 			}
 		} else {
 			leafList.add(ah);
 		}
 		return children;
 	}
 	
 	@Deprecated
 	public void initializeRwandaHierarchyTables() {
 		
 		// TODO: make this generic...
 		// ie, change this function to initializeRwandaHierarchyTables, and make it deprecated
 		
 		Session session = sessionFactory.getCurrentSession();
 		
 		AddressHierarchyLevel country = new AddressHierarchyLevel();
 		country.setName("Country");
 		
 		AddressHierarchyLevel province = new AddressHierarchyLevel();
 		province.setName("Province");
 		
 		AddressHierarchyLevel district = new AddressHierarchyLevel();
 		district.setName("District");
 		
 		AddressHierarchyLevel sector = new AddressHierarchyLevel();
 		sector.setName("Sector");
 		
 		AddressHierarchyLevel cell = new AddressHierarchyLevel();
 		cell.setName("Cell");
 		
 		AddressHierarchyLevel umudugudu = new AddressHierarchyLevel();
 		umudugudu.setName("Umudugudu");
 		
 		session.save(country);
 		session.save(province);
 		session.save(country);
 		session.save(district);
 		session.save(sector);
 		session.save(cell);
 		session.save(umudugudu);
 		
 		province.setParent(country);
 		district.setParent(province);
 		sector.setParent(district);
 		cell.setParent(sector);
 		umudugudu.setParent(cell);
 		
 	}
 	
 	// TODO: remove "page" parameter?
 	// TODO: deprecate this whole method, or redo it so that it doesn't rely on custom query/hierarchy level\
 	// TODO: or, just change the SQL statement so that it dynamically maps based on the AddressHierarchyLevel field
 	
 	@SuppressWarnings("unchecked")
 	@Deprecated
 	public int getUnstructuredCount(int page) {
 		
 		String INVALID_ADDRESS_COUNT = "select count(*) "
 		        + " from person_address "
 		        + " left join patient_identifier on patient_identifier.patient_id = person_address.person_id "
 		        + " left join patient_program on patient_program.patient_id = person_address.person_id "
 		        + " left join patient_state on patient_program.patient_program_id = patient_state.patient_program_id "
 		        + " left join program_workflow_state on patient_state.state = program_workflow_state.program_workflow_state_id "
 		        + " left join concept_name on concept_name.concept_id = program_workflow_state.concept_id "
 		        + " left join person_name on person_name.person_id = person_address.person_id "
 		        + " where person_address.voided = 0 AND "
 		        + " patient_identifier.preferred = 1 AND "
 		        + " person_name.preferred = 1 AND "
 		        + " patient_program.voided = 0 AND "
 		        + " patient_program.date_completed is null AND "
 		        + " (person_address.country not in (select name from address_hierarchy where type_id = 1) "
 		        + " OR person_address.state_province not in (select name from address_hierarchy where type_id = 2 and parent_id in (select address_hierarchy_id from address_hierarchy where name = person_address.country and type_id = 1)) "
 		        + " OR person_address.county_district not in (select name from address_hierarchy where type_id = 3 and parent_id in (select address_hierarchy_id from address_hierarchy where name = person_address.state_province and type_id = 2))"
 		        + " OR person_address.city_village not in (select name from address_hierarchy where type_id = 4 and parent_id in (select address_hierarchy_id from address_hierarchy where name = person_address.county_district and type_id = 3))"
 		        + " OR person_address.neighborhood_cell not in (select name from address_hierarchy where type_id = 5 and parent_id in (select address_hierarchy_id from address_hierarchy where name = person_address.city_village and type_id = 4))"
 		        
 		        + " OR person_address.address1 not in (select name from address_hierarchy where type_id = 6 and parent_id in (select address_hierarchy_id from address_hierarchy where name = person_address.neighborhood_cell and type_id = 5)))";
 		
 		SQLQuery sqlQuery = sessionFactory.getCurrentSession().createSQLQuery(INVALID_ADDRESS_COUNT);
 		List<Integer> unstructuredCount = sqlQuery.list();
 		int count = 0;
 		if (unstructuredCount.size() > 0) {
 			count = unstructuredCount.get(0);
 		}
 		return count;
 	}
 	
 	// TODO: this won't work in the latest version of openmrs because of the change in table names
 	
 	@SuppressWarnings("unchecked")
 	@Deprecated
 	public List<Object[]> findUnstructuredAddresses(int page, int locationId) {
 		int startIndex = 0;
 		if (page > 0) {
 			startIndex = page * 100 - 100;
 		}
 		
 		String CELL_UMU = "select x.state_province, x.county_district, x.city_village, x.neighborhood_cell, x.address1, pi.patient_id,pi.identifier, location.name from (select identifier,location_id, patient_id, patient_identifier_id from patient_identifier where preferred = 1) pi left join (select address1,state_province, county_district, city_village, neighborhood_cell, date_created,person_id,person_address_id from person_address pa left join address_hierarchy on pa.address1 = address_hierarchy.name inner join address_hierarchy ah2 on pa.neighborhood_cell = ah2.name and address_hierarchy.parent_id = ah2.address_hierarchy_id and ah2.type_id=(select location_attribute_type_id from address_hierarchy_type where name='Cell') where voided=0) x on pi.patient_id = x.person_id inner join location on location.location_id = pi.location_id where location.location_id = ? and x.person_id is null order by x.date_created desc";
 		
 		SQLQuery sqlQuery = sessionFactory.getCurrentSession().createSQLQuery(CELL_UMU);
 		sqlQuery.addScalar("patient_id", Hibernate.INTEGER).addScalar("identifier", Hibernate.STRING).addScalar("name",
 		    Hibernate.STRING).addScalar("state_province", Hibernate.STRING).addScalar("county_district", Hibernate.STRING)
 		        .addScalar("city_village", Hibernate.STRING).addScalar("neighborhood_cell", Hibernate.STRING).addScalar(
 		            "address1", Hibernate.STRING);
 		sqlQuery.setInteger(0, locationId);
 		
 		sqlQuery.setMaxResults(100);
 		sqlQuery.setFirstResult(startIndex);
 		
 		List<Object[]> unstructuredPersonAddressIds = sqlQuery.list();
 		
 		return unstructuredPersonAddressIds;
 	}
 	
 	// TODO: figure out where this needs to go... probably will deprecate this?
 	
 	@SuppressWarnings("unchecked")
 	@Deprecated
 	public List<Object[]> getLocationAddressBreakdown(int locationId) {
 		
 		String LOCATION_BREAKDOWN = "select pa.county_district,pa.city_village, count(*) from(select identifier,location_id, patient_id, patient_identifier_id from patient_identifier where preferred = 1)pi inner join location on location.location_id = pi.location_id and location.location_id = ? inner join (select country,state_province,county_district,city_village, person_id from person_address where voided = 0 and preferred = 1) pa on pi.patient_id = pa.person_id group by pa.country, pa.state_province, pa.county_district, pa.city_village";
 		
 		SQLQuery sqlQuery = sessionFactory.getCurrentSession().createSQLQuery(LOCATION_BREAKDOWN);
 		sqlQuery.addScalar("city_village", Hibernate.STRING).addScalar("count(*)", Hibernate.INTEGER).setInteger(0,
 		    locationId);
 		
 		return sqlQuery.list();
 	}
 	
 	// TODO: fix this to work with the new address model? genericize this?
 	@SuppressWarnings("unchecked")
 	@Deprecated
 	public List<Object[]> getAllAddresses(int page) {
 		
 		int startIndex = 0;
 		if (page > 0) {
 			startIndex = page * 400 - 400;
 		}
 		
 		String ALL_ADDRESSES = "select * from (select max(date_created), patient_id from patient_program group by patient_id) pp inner join  person_address on pp.patient_id = person_address.person_id where person_address.voided = 0  order by person_address.date_created desc";
 		
 		SQLQuery sqlQuery = sessionFactory.getCurrentSession().createSQLQuery(ALL_ADDRESSES);
 		sqlQuery.addScalar("patient_id", Hibernate.INTEGER).addScalar("country", Hibernate.STRING).addScalar(
 		    "person_address.state_province", Hibernate.STRING).addScalar("person_address.county_district", Hibernate.STRING)
 		        .addScalar("person_address.city_village", Hibernate.STRING).addScalar("person_address.neighborhood_cell",
 		            Hibernate.STRING).addScalar("person_address.address1", Hibernate.STRING);
 		
 		sqlQuery.setMaxResults(100);
 		sqlQuery.setFirstResult(startIndex);
 		
 		List<Object[]> allAddresses = sqlQuery.list();
 		//List<PersonAddress> pas = convertToPersonAddresses(allAddresseses);
 		
 		return allAddresses;
 	}
 
 
 
 }
