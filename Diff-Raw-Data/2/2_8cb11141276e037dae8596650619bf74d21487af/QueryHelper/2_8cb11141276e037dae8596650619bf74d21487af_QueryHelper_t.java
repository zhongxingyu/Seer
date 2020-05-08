 package query;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.persistence.Query;
 
 import models.UsageLog;
 import play.Logger;
 import play.db.jpa.JPA;
 import util.Utils;
 
 /**
  * Helper class defining DB finder methods used in the application 
  * 
  * @author Paolo Di Tommaso
  * 
  *
  */
 public class QueryHelper {
 
 	/**
 	 * Find the minimum and maximum creation dates in the usage log
 	 * 
 	 * @return a <min, max> dates pair using a {@link MinMaxDate} instance or null if no data is available
 	 */
 	public static MinMaxDate findUsageMinMaxDate() { 
 		Query query = JPA.em().createQuery(
 				"select " +
 				"  new query.MinMaxDate( min(log.creation), max(log.creation) )" +
 				"from " +
 				"  UsageLog log " );
 				
 		return (MinMaxDate) query.getSingleResult();
 	}
 	
 	/**
 	 * Find the minimum creation date in the usage log
 	 * 
 	 * @return the min @{link Date} instance or null if no data is available
 	 */
 	public static Date findUsageMinDate() { 
 		MinMaxDate result = findUsageMinMaxDate();
 		return result != null  ? result.min : null;
 	}
 	
 	/**
 	 * Find the maximum creation date in the usage log
 	 * 
 	 * @return the min @{link Date} instance or null if no data is available
 	 */
 	public static Date findUsageMaxDate() { 
 		MinMaxDate result = findUsageMinMaxDate();
 		return result != null  ? result.max : null;
 	}
 	
 	/**
 	 * Fetch the dataset to be shoed in the usage grid control 
 	 * 
 	 * @param filter 
 	 * @param page the page index when pagination is used, the first page starts from 1
 	 * @param size the maximum number of records that a page can contain 
 	 * @param sortfield
 	 * @param sortorder
 	 * @param qvalue
 	 * @param qfield
 	 * @return
 	 */
 	public static GridResult findUsageGridData( UsageFilter filter, int page, int size, String sortfield, String sortorder, String qvalue, String qfield ) { 
 		
 		int skip = (page>0 && size>0 ) ? (page-1) * size : -1;
 		
 		QueryBuilder where =  new QueryBuilder(UsageLog.class);
 		
 		/* 
 		 * 1. counting 
 		 */
 
 		/* apply filter restrictions */
 		if( filter!=null && Utils.isNotEmpty(filter.bundle)) { 
 			where.and( "bundle", "like", filter.bundle );
 		}
 
 		if( filter!=null && Utils.isNotEmpty(filter.service)) { 
 			where.and( "service", "like", filter.service );
 		}
 
 		if( filter!=null && Utils.isNotEmpty(filter.status)) { 
 			where.and( "status", "=", filter.status);
 		}
 
 		if( filter!=null && filter.since!=null) { 
 			where.and( "creation", ">=", filter.since);
 		}
 
 		if( filter!=null && filter.until!=null) { 
 			Date end = new Date( filter.until.getTime() + 24L * 3600 * 1000 );
 			where.and( "creation", "<", end);
 		}
 		
 
 		/* 
 		 * more restrictions specified by the grid control
 		 */
 		if( "bundle".equals(qfield) && Utils.isNotEmpty(qvalue) ) { 
 			where.and( "bundle", "like", qvalue );
 		}
 
 		else if( "service".equals(qfield) && Utils.isNotEmpty(qvalue) ) { 
 			where.and( "service", "like", qvalue );
 		}
 
 		else if( "creation".equals(qfield) && Utils.isNotEmpty(qvalue) ) { 
 			Date date = Utils.parseDate(qvalue, null);
 			if( date != null ) { 
 				where.and( "creation", "=", date );
 			}
 			else { 
 				Logger.warn("Invalid date format: %s", qvalue);
 			}
 		}
 
 		else if( "ip".equals(qfield) && Utils.isNotEmpty(qvalue) ) { 
 			where.and( "ip", "like", qvalue );
 		}
 	
 		else if( "rid".equals(qfield) && Utils.isNotEmpty(qvalue) ) { 
 			where.and( "requestId", "like", qvalue );
 		}
 
 		else if( "email".equals(qfield) && Utils.isNotEmpty(qvalue) ) { 
 			where.and( "email", "like", qvalue );
 		}
 
 		else if( "source".equals(qfield) && Utils.isNotEmpty(qvalue) ) { 
			where.and( "source", "like", qvalue );
 		}
 
 		else if( "status".equals(qfield) && Utils.isNotEmpty(qvalue) ) { 
 			where.and( "status", "=", qvalue );
 		}	
 		
 		/* 
 		 * counting the total result items 
 		 */
 		GridResult result = new GridResult();
 		Query query = JPA.em().createQuery("select count(*) " + where.toString());
 		// append the params
 		where.setParams(query);
 		
 		// execute the query and get the COUNT
 		result.total = (Long) query.getSingleResult();
 		
 		/* 
 		 * 2. fetch result set 
 		 * define the ordering 
 		 */
 		if( Utils.isNotEmpty(sortfield) && !"undefined".equals(sortfield) && !"null".equals(sortfield) ) { 
 			if( sortfield.equals("duration") ) { // 'duration' is the formatted version of the field 'elapsed'
 				sortfield = "elapsed";
 			}
 			where.order(sortfield, !"desc".equals(sortorder) );
 		}
 		
 
 		/*
 		 * find the rows 
 		 */
 		query = JPA.em().createQuery(where.toString());
 		if( skip > 0 ) { 
 			query.setFirstResult(skip);
 		}
 		if( size > 0 ) { 
 			query.setMaxResults(size);
 		}
 
 		where.setParams(query);
 
 		result.rows = query.getResultList();
 		
 		return result;
 	}
 	
 
 	public static List<Object[]> findUsageAggregation(UsageFilter filter) { 
 
 		String select = 				
 			"select " +
 			"  count(*), " +
 			"  log.bundle, " +
 			"  log.service," +
 			"  log.status," +
 			"  PARSEDATETIME( FORMATDATETIME(log.creation,'yyyy-MM-dd'), 'yyyy-MM-dd' ) _creation ";
 
 		
 		QueryBuilder where = new QueryBuilder( "from USAGE_LOG log " );
 		
 		/* apply filter restrictions */
 		if( filter!=null && Utils.isNotEmpty(filter.bundle)) { 
 			where.and( "log.bundle", "like", filter.bundle );
 		}
 
 		if( filter!=null && Utils.isNotEmpty(filter.service)) { 
 			where.and( "log.service", "like", filter.service );
 		}
 
 		if( filter!=null && Utils.isNotEmpty(filter.status)) { 
 			where.and( "log.status", "=", filter.status);
 		}
 
 		if( filter!=null && filter.since!=null) { 
 			where.and( "log.creation", ">=", filter.since);
 		}
 
 		if( filter!=null && filter.until!=null) { 
 			Date end = new Date( filter.until.getTime() + 24L * 3600 * 1000 );
 			where.and( "log.creation", "<", end);
 		}
 		
 		
 		/* group by condition */
 		String groupby = 				
 			" group by " +
 			" log.bundle, log.service, log.status, _creation ";
 
 		
 		Query query = JPA.em().createNativeQuery( select + where + groupby );
 		// set the restrictions params 
 		where.setParams(query);
 		
 		/* return the result */
 		return query.getResultList();
 	}
 	
 	
 	/* helper class to add 'and' restriction */ 
 	static class QueryBuilder  { 
 		StringBuilder string;
 		boolean first=true;
 		List<Object> params;
 		
 		public QueryBuilder( Class clazz ) { 
 			params = new ArrayList<Object>();
 			
 			string = new StringBuilder("from ");
 			string. append( clazz.getSimpleName() );
 		}
 		
 		public QueryBuilder( String from ) { 
 			params = new ArrayList<Object>();
 			
 			string = new StringBuilder(from);
 		}
 		
 		QueryBuilder and( String field, String condition, Object value ) { 
 			if( Utils.isEmpty(field)) { 
 				return this;
 			}
 			
 			/* 
 			 * add the valid query separator 'and' or 'where'
 			 */
  			if( first ) { 
 				first = false;
 				string.append( " where ");
 			}
 			else { 
 				string.append( " and " );
 			}
 
  			/* appent the restriction */
 			string.append(field) .append(" ") .append(condition) .append(" ") .append("?");
 			
 			/* save the param value */
 			params.add(value);
 			
 			return this;
 		}
 		
 		public  QueryBuilder order( String field, boolean asc ) { 
 			if( Utils.isEmpty(field) ) { return this; }
 			
 			string 
 			    .append(" ") .append("order by ")
 				.append( field ) .append( asc ? " asc" : " desc" );
 			
 			return this;
 		}
 		
 		public String toString() { 
 			return string.toString();
 		}
 
 		public void setParams(Query query) {
 			for( int i=0; i<params.size(); i++ ) { 
 				query.setParameter(i+1, params.get(i));
 			}
 		}
 		
 	}	
 	
 	/**
 	 * Fetchs the usage count for all bundle/service in the applicaton log 
 	 * 
 	 * @return a list of {@link BundleServiceCount}
 	 */
 	public static List<BundleServiceCount> findUsageBundleServiceCounts ()  { 
 		String sQuery = 
 			"select new query.BundleServiceCount(" +
 			"  log.bundle, " +
 			"  log.service, " +
 			"  count(*) " +
 			") " +
 			"from " +
 			"  UsageLog log " +
 			"group by " +
 			" log.bundle, log.service ";
 
 		List<BundleServiceCount> result = JPA.em().createQuery(sQuery) .getResultList();
 		return result != null ? result : Collections.EMPTY_LIST;
 	}
 	
 	/** 
 	 * @return a map containing the available services for each bundle in the usage log 
 	 */
 	public static Map<String,List<String>> findUsageServiceMap() { 
 		List<BundleServiceCount> list = findUsageBundleServiceCounts();
 		return findUsageServiceMap(list);
 	}
 	
 	static Map<String,List<String>> findUsageServiceMap(List<BundleServiceCount> list) { 
 		
 		/* 
 		 * 1. group by bundle name 
 		 */
 		Map<String,List<String>> aggregate = new HashMap<String, List<String>>();
 		for( BundleServiceCount item : list ) {
 			List<String> services = aggregate.get(item.bundle);
 			if( services == null ) { 
 				services = new ArrayList<String>();
 				aggregate.put(item.bundle,services);
 			}
 			if( !services.contains(item.service) ) { 
 				services.add(item.service);
 			}
 		}
 		
 		return aggregate;
 	}
 }
 
 
