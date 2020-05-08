 package net.canadensys.dataportal.vascan.dao.impl;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import net.canadensys.databaseutils.ScrollableResultsIteratorWrapper;
 import net.canadensys.dataportal.vascan.dao.TaxonDAO;
 import net.canadensys.dataportal.vascan.model.TaxonLookupModel;
 import net.canadensys.dataportal.vascan.model.TaxonModel;
 
 import org.apache.log4j.Logger;
 import org.hibernate.Criteria;
 import org.hibernate.HibernateException;
 import org.hibernate.Query;
 import org.hibernate.SQLQuery;
 import org.hibernate.ScrollMode;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.criterion.Conjunction;
 import org.hibernate.criterion.Criterion;
 import org.hibernate.criterion.Disjunction;
 import org.hibernate.criterion.MatchMode;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Projections;
 import org.hibernate.criterion.Restrictions;
 import org.hibernate.type.CalendarType;
 import org.hibernate.type.IntegerType;
 import org.hibernate.type.StringType;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 
 /**
  * Implementation for handling TaxonModel and TaxonLookupModel through Hibernate technology.
  * @author canadensys
  *
  */
 @Repository("taxonDAO")
 public class HibernateTaxonDAO implements TaxonDAO{
 	
 	private enum RegionCriterionOperatorEnum{OR,AND};
 
 	//get log4j handler
 	private static final Logger LOGGER = Logger.getLogger(HibernateTaxonDAO.class);
 	private static final String MANAGED_ID = "taxonId";
 	
 	//TODO should not be in that class
 	//includes Greenland and St-Pierre Miquelon
 	public static final String ALL_PROVINCES[] = {"AB","BC","GL","NL_N","NL_L","MB","NB","NT","NS","NU","ON","PE","QC","PM","SK","YT"};
 	public static final String CANADA_PROVINCES[] = {"AB","BC","NL_N","NL_L","MB","NB","NT","NS","NU","ON","PE","QC","SK","YT"};
 	public static final int STATUS_ACCEPTED = 1;
 	public static final int STATUS_SYNONYM = 2;
 	
 	@Autowired
 	private SessionFactory sessionFactory;
 	
 	@Override
 	public boolean saveTaxonLookup(TaxonLookupModel tlm) {
 		try{
 			sessionFactory.getCurrentSession().saveOrUpdate(tlm);
 		}
 		catch (HibernateException e) {
 			LOGGER.fatal("Couldn't save TaxonLookupModel", e);
 			e.printStackTrace();
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public TaxonLookupModel loadTaxonLookup(Integer taxonId) {
 		Criteria searchCriteria = sessionFactory.getCurrentSession().createCriteria(TaxonLookupModel.class);
 		searchCriteria.add(Restrictions.eq(MANAGED_ID, taxonId));
 		return (TaxonLookupModel)searchCriteria.uniqueResult();
 	}
 
 	@Override
 	public TaxonModel loadTaxon(Integer taxonId) {
 		try{
 			Criteria searchCriteria = sessionFactory.getCurrentSession().createCriteria(TaxonModel.class);
 			searchCriteria.add(Restrictions.eq("id", taxonId));
 			return (TaxonModel)searchCriteria.uniqueResult();
 		}
 		catch(HibernateException e){
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<TaxonModel> loadTaxonByName(String taxonCalculatedName) {
 		Criteria searchCriteria = sessionFactory.getCurrentSession().createCriteria(TaxonModel.class).createCriteria("lookup").add(Restrictions.like("calname", taxonCalculatedName));
 		return (List<TaxonModel>)searchCriteria.list();
 	}
 	
 	/**
 	 * @see TaxonDAO
 	 */
 	@Override
 	public Iterator<TaxonLookupModel> loadTaxonLookup(int limitResultsTo, String habitus, int taxonid, String combination, String[] region, String[] status, String[] rank, boolean includeHybrids, String sort){
 		Criteria searchCriteria = sessionFactory.getCurrentSession().createCriteria(TaxonLookupModel.class);
 
 		// don't go any further if no region & status
 		// Make sure this request is looking for something
 		if((status == null || region == null) && taxonid == 0 && habitus.equals("all")){
 			return null;
 		}
 		
 		if(taxonid > 0){
 			searchCriteria.add(getTaxonCriterion(taxonid));
 		}
 		
 		// filter by status in region
 		if(status != null && region != null && combination != null){
 			searchCriteria.add(getStatusRegionCriterion(combination, status, region));
 		}
 		
 		// filter by habitus
 		if(habitus != null && habitus!="" && !habitus.equals("all")){
 			searchCriteria.add(getHabitCriterion(habitus));
 		}
 		
 		// filter by rank
 		if(rank != null){
 			searchCriteria.add(getRankCriterion(rank));
 		}
 		
 		// filter by hybrids
 		if(!includeHybrids){
 			searchCriteria.add(getExcludeHybridCriterion());
 		}
 		
 		// order
 		addOrderBy(searchCriteria, sort);
 		
 		// limit
 		if(limitResultsTo > 0 ){
 			addLimitClause(searchCriteria, limitResultsTo);
 		}
 		
 		return new ScrollableResultsIteratorWrapper<TaxonLookupModel>(searchCriteria.scroll(ScrollMode.FORWARD_ONLY),sessionFactory.getCurrentSession());
 	}
 	
 	/**
 	 * @see TaxonDAO
 	 */
 	@Override
 	public Integer countTaxonLookup(String habitus, int taxonid, String combination, String[] region, String[] status, String[] rank, boolean includeHybrids){
 		Criteria searchCriteria = sessionFactory.getCurrentSession().createCriteria(TaxonLookupModel.class);
 
 		// don't go any further if no region & status
 		// Make sure this request is looking for something
 		if((status == null || region == null) && taxonid == 0 && habitus.equals("all")){
 			return null;
 		}
 		//we only want to count
 		searchCriteria.setProjection(Projections.count(MANAGED_ID));
 		
 		if(taxonid > 0){
 			searchCriteria.add(getTaxonCriterion(taxonid));
 		}
 		
 		// filter by status in region
 		if(status != null && region != null && combination != null){
 			searchCriteria.add(getStatusRegionCriterion(combination, status, region));
 		}
 		
 		// filter by habitus
 		if(habitus != null && habitus!="" && !habitus.equals("all")){
 			searchCriteria.add(getHabitCriterion(habitus));
 		}
 		
 		// filter by rank
 		if(rank != null){
 			searchCriteria.add(getRankCriterion(rank));
 		}
 		
 		// filter by hybrids
 		if(!includeHybrids){
 			searchCriteria.add(getExcludeHybridCriterion());
 		}
 
 		Long total_rows = (Long)searchCriteria.uniqueResult();
 		return total_rows.intValue();
 	}
 	
 	@Override
 	public List<Object[]> getAcceptedTaxon(int maximumRank){
 		Session hibernateSession = sessionFactory.getCurrentSession();
 		Query query = hibernateSession.createSQLQuery("SELECT taxon.id, lookup.calname, lookup.rank FROM taxon,lookup WHERE taxon.id = lookup.taxonid AND taxon.rankid <= :rankid AND taxon.statusid = :statusid  ORDER BY lookup.calname ASC")
 				.addScalar("id",IntegerType.INSTANCE)
 				.addScalar("calname",StringType.INSTANCE)
 				.addScalar("rank",StringType.INSTANCE)
 				.setParameter("rankid", maximumRank)
 				.setParameter("statusid", STATUS_ACCEPTED);
 		return (List<Object[]>)query.list();
 	}
 	
 	@Override
 	public List<Object[]> loadCompleteTaxonData(List<Integer> taxonIdList){
 		Query query = sessionFactory.getCurrentSession().createSQLQuery("SELECT taxon.id, taxon.mdate, lookup.status, taxonomy.parentid, reference.url, reference.reference, lookup.calnameauthor,taxon.author,lookup.rank, N.calnameauthor AS parentFSN, lookup.higherclassification, "+
 			"lookup.class, lookup._order,lookup.family,lookup.genus,lookup.subgenus,lookup.specificepithet,lookup.infraspecificepithet" +
 			" FROM taxon" +
 			" INNER JOIN lookup ON taxon.id = lookup.taxonid"+
 			" INNER JOIN reference ON taxon.referenceid = reference.id"+
 			" LEFT JOIN taxonomy ON taxonomy.childid = taxon.id"+
 			" LEFT JOIN taxon T ON taxonomy.parentid = T.id"+
 			" LEFT JOIN lookup N ON N.taxonid = T.id"+
 		    " WHERE taxon.id IN (:id)")
 		    .addScalar("id", IntegerType.INSTANCE)
 			.addScalar("mdate",CalendarType.INSTANCE)
 			.addScalar("status",StringType.INSTANCE)
 			.addScalar("parentid",IntegerType.INSTANCE)
 			.addScalar("url",StringType.INSTANCE)
 			.addScalar("reference",StringType.INSTANCE)
 			.addScalar("calnameauthor",StringType.INSTANCE)
 			.addScalar("author",StringType.INSTANCE)
 			.addScalar("rank",StringType.INSTANCE)
 			.addScalar("parentfsn",StringType.INSTANCE)
 			.addScalar("higherclassification",StringType.INSTANCE)
 			.addScalar("class",StringType.INSTANCE)
 			.addScalar("_order",StringType.INSTANCE)
 			.addScalar("family",StringType.INSTANCE)
 			.addScalar("genus",StringType.INSTANCE)
 			.addScalar("subgenus",StringType.INSTANCE)
 			.addScalar("specificepithet",StringType.INSTANCE)
 			.addScalar("infraspecificepithet",StringType.INSTANCE);
 		query.setParameterList("id", taxonIdList);
 		return query.list();
 	}
 	
 	/**
 	 * Add a limit to the provided Criteria
 	 * @param searchCriteria
 	 * @param limitResultsTo
 	 */
 	private void addLimitClause(Criteria searchCriteria, int limitResultsTo){
 		searchCriteria.setMaxResults(limitResultsTo);
 	}
 	
 	/**
 	 * TODO Issue 753
 	 * @param sort
 	 * @return
 	 */
 	private void addOrderBy(Criteria searchCriteria, String sort){
 		if(sort != null){
 			if(sort.equals("alphabetically")){
 				searchCriteria.addOrder(Order.asc("calname"));
 			}
 			else if(sort.equals("taxonomically")){
 				//nothing to do, it's the assumed default order. This is risky and should be fixed.
 			}
 			else{
 				
 			}
 		}
 	}
 	
 	/**
 	 * Returns a criterion to exclude hybrids
 	 * @return
 	 */
 	private Criterion getExcludeHybridCriterion(){
 		//warning, × is not the x character, × = &times;
 		return Restrictions.not(Restrictions.like("calname", "×", MatchMode.ANYWHERE));
 	}
 	
 	/**
 	 * 
 	 * Fills the idList with the entire accepted children, recursively.
 	 * 
 	 * @param taxonid
 	 * @param in
 	 */
 	private void getTaxonIdTree(int taxonid, List<Integer> idList){
 		Session hibernateSession = sessionFactory.getCurrentSession();
 		SQLQuery query;
 		try {
 			query = hibernateSession.createSQLQuery("SELECT childid FROM taxonomy,taxon WHERE parentid = :taxonid AND taxonomy.childid = taxon.id AND taxon.statusid = :statusid");
 			query.setParameter("taxonid", taxonid);
 			query.setParameter("statusid",STATUS_ACCEPTED);
 			query.addScalar("childid",IntegerType.INSTANCE);
 			
 			List<Integer> result = query.list();
 			if(result != null){
 				for(Integer currId : result){
 					idList.add(currId);
 					getTaxonIdTree(currId,idList);
 				}
 			}
 		} catch (HibernateException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * Returns a Criterion for a taxon and all its accepted children
 	 * @param taxonId
 	 * @return
 	 */
 	private Criterion getTaxonCriterion(int taxonId){
 		 List<Integer> idList = new ArrayList<Integer>();
 		getTaxonIdTree(taxonId, idList);
 		//add the taxon itself
 		idList.add(taxonId);
 		return Restrictions.in("taxonId", idList);
 	}
 	
 	/**
 	 * Returns a Criterion for one ore more rank(s)
 	 * @param rank a validated rank array
 	 * @return
 	 */	
 	private Criterion getRankCriterion(String[] rank){
 		Criterion fieldCriterion = null;
 		for(String r : rank){
 			if(fieldCriterion == null){
 				fieldCriterion = Restrictions.eq("rank", r.toLowerCase());
 			}
 			else{ //separate different values with an OR statement
 				fieldCriterion = Restrictions.or(fieldCriterion, Restrictions.eq("rank", r.toLowerCase()));
 			}
 		}
 		return fieldCriterion;
 	}
 
 	
 	/**
	 * Returns a Criterion for a habit
 	 * @return
 	 */
 	private Criterion getHabitCriterion(String habit){
		return Restrictions.eq("calhabit", habit.toLowerCase());
 	}
 	
 	/**
 	 * Returns a Criterion for a combination of status and region(provinces)
 	 * Combination definition : 
 	 * (allof), (native,ephemere), (qc,on): 
 	 * AND (p_ON = 'native' OR p_ON = 'ephemere') AND (p_QC = 'native' OR p_QC = 'ephemere') ...
 	 * 
 	 * (anyof), (native,ephemere), (qc,on): 
 	 * AND (p_ON = 'native' OR p_ON = 'ephemere' OR p_QC = 'native' OR p_QC = 'ephemere') ...
 	 * 
 	 * (only), (native,ephemere), (qc,on):
 	 * AND (p_ON = 'native' OR p_ON = 'ephemere') OR (p_QC = 'native' OR p_QC = 'ephemere') 
 	 * AND p_BC <> 'native' AND p_BC <> 'ephemere' AND p_PM <> 'native' AND p_PM <> 'ephemere' [and so on for all provinces & territories] ... 
 	 * 
 	 * (only_ca), (native,ephemere), (qc,on):
 	 * AND (p_ON = 'native' OR p_ON = 'ephemere') OR (p_QC = 'native' OR p_QC = 'ephemere') 
 	 * AND p_BC <> 'native' AND p_BC <> 'ephemere' AND p_PM <> 'native' AND p_MB <> 'ephemere' [and so on for all provinces & territories, except p_PM & p_GL] ...
 	 * 
 	 * @param combination
 	 * @param status
 	 * @param province
 	 * @return
 	 */
 	private Criterion getStatusRegionCriterion(String combination, String[] status, String[] region){
 		if(combination.equals("allof")){
 			return getAllRegionStatusCriterion(status, region,RegionCriterionOperatorEnum.AND);
 		}
 		else if(combination.equals("anyof")){
 			return getAnyRegionStatusCriterion(status, region);
 		}
 		else if(combination.equals("only")){
 			return Restrictions.and(getAllRegionStatusCriterion(status, region,RegionCriterionOperatorEnum.OR), 
 					getExclusionCriterion(status,region,ALL_PROVINCES));
 		}
 		else if(combination.equals("only_ca")){
 			// we are looking to query only canadian provinces, and ignore greenland and saint-pierre miquelon 
 			// (may or may not have a status that must be excluded in other canadian provinces)
 			return Restrictions.and(getAllRegionStatusCriterion(status, region,RegionCriterionOperatorEnum.OR), 
 					getExclusionCriterion(status,region,CANADA_PROVINCES));
 		}
 		return null;
 	}
 	
 	/**
 	 * Returns a Criterion to include all region with one of the status.
 	 * The operation determine if the have an OR or an AND between the regions
 	 * @param status
 	 * @param province
 	 * @param op
 	 * @return
 	 */
 	private Criterion getAllRegionStatusCriterion(String[] status, String[] region, RegionCriterionOperatorEnum op){
 		Criterion regionStatusCriterion = null;
 		for(String prov : region){
 			// match against all the statuses checked in the checklist builder
 			// fill an array list of clauses that will be joined into a string by an sql operator
 			Criterion fieldCriterion = null;
 			for(String currStatus: status){
 				if(fieldCriterion == null){
 					fieldCriterion = Restrictions.eq(prov, currStatus);
 				}
 				else{ //separate different values with an OR statement
 					fieldCriterion = Restrictions.or(fieldCriterion, Restrictions.eq(prov, currStatus));
 				}
 			}
 			if(regionStatusCriterion == null){
 				regionStatusCriterion = fieldCriterion;
 			}
 			else{
 				if(op == RegionCriterionOperatorEnum.AND){
 					regionStatusCriterion = Restrictions.and(regionStatusCriterion, fieldCriterion);
 				}
 				else if(op == RegionCriterionOperatorEnum.OR){
 					regionStatusCriterion = Restrictions.or(regionStatusCriterion, fieldCriterion);
 				}
 			}
 		}
 		return regionStatusCriterion;
 	}
 	
 	/**
 	 * Returns a Criterion to include any of the regions with any of the status.
 	 * @param status
 	 * @param province
 	 * @return
 	 */
 	private Criterion getAnyRegionStatusCriterion(String[] status, String[] regions){
 		Disjunction disjunction = Restrictions.disjunction();
 		for(String currRegion : regions){
 			for(String currStatus: status){
 				disjunction.add( Restrictions.eq(currRegion, currStatus));
 			}
 		}
 		return disjunction;
 	}
 	
 	/**
 	 * Returns a Criterion to exclude all regions with one of the provided status that are not in the provided region array.
 	 * @param status
 	 * @param province
 	 * @param allProvinces
 	 * @return
 	 */
 	private Criterion getExclusionCriterion(String[] status, String[] region, String[] allRegions){
 		Conjunction exclusionCriterion = Restrictions.conjunction();
 		for(String stat: status){
 			for(String excludeProvince : allRegions){
 				boolean exclude = true;
 				for(String prov : region){
 					if(prov.equals(excludeProvince)){
 						exclude = false;
 						break;
 					}
 				}
 				// if province is not checked in builder, exclude that province for that status
 				if(exclude){
 					exclusionCriterion.add(Restrictions.ne(excludeProvince, stat));
 				}
 			}
 		}
 		return exclusionCriterion;
 	}
 	
 	public SessionFactory getSessionFactory() {
 		return sessionFactory;
 	}
 	public void setSessionFactory(SessionFactory sessionFactory) {
 		this.sessionFactory = sessionFactory;
 	}
 }
 
 
