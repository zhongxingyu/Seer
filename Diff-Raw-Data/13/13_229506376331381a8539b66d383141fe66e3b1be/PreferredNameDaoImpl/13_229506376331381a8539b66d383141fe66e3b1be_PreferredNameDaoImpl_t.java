 package edu.wisc.portlet.preferred.dao;
 
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.dao.support.DataAccessUtils;
 import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.googlecode.ehcache.annotations.Cacheable;
 import com.googlecode.ehcache.annotations.TriggersRemove;
 
 import edu.wisc.portlet.preferred.form.PreferredName;
 
 @Repository
 public class PreferredNameDaoImpl implements PreferredNameDao  {
 	protected final Logger logger = LoggerFactory.getLogger(getClass());
 	
     private NamedParameterJdbcOperations jdbcTemplate;
     private UpdatePreferredNameProcedure updatePreferredName;
     private DeletePreferredNameFunction deletePreferredNameAdmin;
 	
 	@Autowired
     public void setJdbcTemplate(@Qualifier("prefname") NamedParameterJdbcOperations jdbcTemplate) {
         this.jdbcTemplate = jdbcTemplate;
     }
 	
 	@Autowired
     public void setUpdatePreferredName(UpdatePreferredNameProcedure updatePrefNameProc) {
         this.updatePreferredName = updatePrefNameProc;
     }
 	
 	@Autowired
 	public void setDeletePreferredName(DeletePreferredNameFunction deletePrefNameProc) {
 		this.deletePreferredNameAdmin = deletePrefNameProc;
 	}
 
 	@Override
 	@Cacheable(cacheName = "prefname")
 	public PreferredName getPreferredName(String pvi) {
 		final Map<String, String> args = new LinkedHashMap<String, String>();
         args.put("pvi", pvi);
         
         List<PreferredName> query = jdbcTemplate.query("select first_name, middle_name, pvi from msnprefname.msn_preferred_name where pvi = :pvi", 
                 args, 
                 PreferredNameRowMapper.INTANCE);
         
         return DataAccessUtils.singleResult(query);
 	}
 
 	@Override
 	public boolean isPending() {
 		// TODO check if what is in the db is the same as what is currently in LDAP
 		return false;
 	}
 
 	@Override
 	@Transactional
 	@TriggersRemove(cacheName="prefname")
 	public void setPreferredName(PreferredName pn) {
 		updatePreferredName.updatePrefferedName(pn);
 	}
 
 	@Override
 	@Transactional
 	@TriggersRemove(cacheName="prefname")
 	public void deletePreferredName(String pvi) {
 		updatePreferredName.updatePrefferedName(new PreferredName(pvi));
 		
 	}
 	
 	@Override
 	@Transactional
 	@TriggersRemove(cacheName="prefname")
	public boolean deletePreferredNameAdmin(String pvi) {
		return deletePreferredNameAdmin.deletePreferredNameAdmin(pvi);
 	}
 
 }
