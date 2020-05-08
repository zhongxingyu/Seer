 package com.cnnic.whois.dao.query.cache;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.springframework.stereotype.Repository;
 
 import com.cnnic.whois.bean.QueryParam;
 import com.cnnic.whois.bean.QueryType;
 import com.cnnic.whois.execption.QueryException;
 import com.cnnic.whois.execption.RedirectExecption;
 @Repository("cacheEntityQueryDao")
 public class EntityQueryDao extends AbstractCacheQueryDao {
 	@Override
 	public Map<String, Object> query(QueryParam param) throws QueryException, RedirectExecption {
 		return dbQueryExecutor.query(QueryType.ENTITY, param);
 	}
	@Override
	protected List<String> getCacheKeySplits(QueryParam param) {
		List<String> keySplits = new ArrayList<String>();
		keySplits.add(QueryType.ENTITY.toString());
		keySplits.add("handle");
		keySplits.add(param.getQ());
		return keySplits;
	}
 
 	@Override
 	public QueryType getQueryType() {
 		return QueryType.ENTITY;
 	}
 
 	@Override
 	public boolean supportType(QueryType queryType) {
 		return QueryType.ENTITY.equals(queryType);
 	}
 
 	@Override
 	protected boolean needInitCache() {
 		return true;
 	}
 
 	@Override
 	protected void initCache() {
 		super.initCacheWithOneKey("$mul$entity", "Handle",QueryType.DNRENTITY);
 		super.initCacheWithOneKey("$mul$entity", "Handle",QueryType.RIRENTITY);
 	}
 }
