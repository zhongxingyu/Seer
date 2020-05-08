 package com.orangeleap.tangerine.security;
 
 import org.springframework.security.Authentication;
 import org.springframework.security.context.SecurityContextHolder;
 import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
 
 import com.ibatis.sqlmap.client.SqlMapClient;
 import com.orangeleap.common.security.OrangeLeapAuthenticationProvider.AuthenticationHelper;
 import com.orangeleap.tangerine.dao.ibatis.IBatisSchemaDao;

 /*
  * Used for non-CAS auth to populate user details 
  */
 public class TangerineAuthenticationHelper implements AuthenticationHelper  {
 	
 	private SqlMapClient sqlMapClient;
 	private TangerineSessionInformationFilter tangerineSessionInformationFilter;
 	
 	public void setTangerineSessionInformationFilter(TangerineSessionInformationFilter tangerineSessionInformationFilter) {
 		this.tangerineSessionInformationFilter = tangerineSessionInformationFilter;
 	}
 	
 	@Override
 	public void postProcess(Authentication authentication) {
 		
 		if (!(authentication instanceof UsernamePasswordAuthenticationToken)) return;
 		
 		UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken)authentication;
 
 		// Needed by tangerineUserHelper
 		if (SecurityContextHolder.getContext().getAuthentication() == null) SecurityContextHolder.getContext().setAuthentication(token);
 		
 		if (token.getDetails() == null) {
 			token.setDetails(new TangerineAuthenticationDetails());
 		}
 
 
 		//
 		// let's switch schema's here since we know we have authenticated....
 		IBatisSchemaDao schema = new IBatisSchemaDao(sqlMapClient);
 		String username = SecurityContextHolder.getContext().getAuthentication().getName();
 		String sitename = username.substring(username.indexOf('@') + 1);
 		schema.use(sitename);
 
 		tangerineSessionInformationFilter.loadTangerineDetails(token);
 	}
 
 	public SqlMapClient getSqlMapClient() {
 		return sqlMapClient;
 	}
 
 	public void setSqlMapClient(SqlMapClient sqlMapClient) {
 		this.sqlMapClient = sqlMapClient;
 	}
 
 	
 }
