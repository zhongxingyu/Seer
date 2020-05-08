 package com.asu.edu.base.dao.impl;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.servlet.ServletContext;
 import javax.servlet.http.HttpSession;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.security.authentication.AnonymousAuthenticationToken;
 import org.springframework.security.authentication.AuthenticationProvider;
 import org.springframework.security.authentication.BadCredentialsException;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.AuthenticationException;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.GrantedAuthorityImpl;
 import org.springframework.security.core.context.SecurityContextHolder;
 
 import com.asu.edu.base.dao.BaseDAO;
 import com.asu.edu.base.dao.intrf.SecurityDAOImplInterface;
 import com.asu.edu.base.vo.ChangePasswordVO;
 import com.asu.edu.base.vo.PendingUsersVO;
 import com.asu.edu.base.vo.RegisterationVO;
 import com.asu.edu.base.vo.UserRegistrationServiceVO;
 import com.asu.edu.base.vo.UserVO;
 import com.asu.edu.constants.CommonConstants;
 import com.asu.edu.constants.SQLConstants;
 
 public class SecurityDAOImpl extends BaseDAO implements SecurityDAOImplInterface,AuthenticationProvider {
 
 	private static final String GET_USER_DEPARTMENTS = "getUserDepartments";
 
 	private static final String USER_ROLE = "userRole";
 
 	private static final String GET_DEPTARTMENTS = "getdeptartments";
 
 	private static final String AUTHENTICATE = "authenticate";
 	private static final String GET_LOGIN_ATTEMPTS = "loginattempts";
 	private boolean isCaptchaEnabled = false;
 	public boolean isCaptchaEnabled() {
 		return isCaptchaEnabled;
 	}
 	public void setCaptchaEnabled(boolean isCaptchaEnabled) {
 		this.isCaptchaEnabled = isCaptchaEnabled;
 	}
 	
 
 	@Autowired
 	private ShaPasswordEncoder passwordEncoder;
 	
 	String calledFunction;
 
 	@Override
 	protected Object toDataObject(ResultSet rs) throws SQLException {
 		
 		if(calledFunction==AUTHENTICATE)
 		{
 			System.out.println("inside authenticate to data object");
 			UserVO userVO = new UserVO();
 			
 			userVO.setId(rs.getInt("ID"));
 			userVO.setUserName(rs.getString("USER_NAME"));
 			userVO.setEmail(rs.getString("EMAIL"));
 			userVO.setFirstName(rs.getString("FIRST_NAME"));
 			userVO.setLastName(rs.getString("LAST_NAME"));
 			userVO.setIsApproved(rs.getInt("IS_APPROVED"));
 			userVO.setLoginAttempts(rs.getInt("LOGIN_ATTEMPTS"));
 			userVO.setRoleId(rs.getInt("ROLE_ID"));
 			
 			return userVO;
 		}
 		if(calledFunction==GET_USER_DEPARTMENTS){
 			return rs.getInt("DEPT_ID");
 		}
 		if(calledFunction==GET_LOGIN_ATTEMPTS){
 			return rs.getInt("LOGIN_ATTEMPTS");
 		}
 		if(calledFunction==USER_ROLE)
 		{
 			System.out.println("inside userRole to data object");
 			return rs.getString("DESC");
 		}
 		else if(calledFunction=="getEmailForUser")
 		{
 				String email_id= rs.getString("EMAIL");
 				return email_id;
 		}
 		if(calledFunction=="sendEmail")
 		{
 			System.out.println("inside sendEmail to data object");
 			ChangePasswordVO changePasswordVO = new ChangePasswordVO();
 			changePasswordVO.setUserName(rs.getString("USER_NAME"));
 			
 		}
 
 
 		return null;
 	}
 	public boolean isValidPassword(String userName, String password)
 	{		
 		calledFunction = AUTHENTICATE;
 		Object[] prepareParams = new Object[2];
 		prepareParams[0] = userName;
 		prepareParams[1] = passwordEncoder.encodePassword(password, userName);
 		UserVO userVO = (UserVO)this.getRowByCriteria(SQLConstants.USER_LOGIN, prepareParams);
 		if(userVO!=null)
 		{
 			return true;
 		}
 		return false;
 	}
 boolean verifyLoginAttempts(String userName)
 {
 	calledFunction = GET_LOGIN_ATTEMPTS;
 	Object[] param = new Object[1];
 	param[0] = userName;
 	Integer loginAttempts = (Integer) this.getRowByCriteria(SQLConstants.LOGIN_ATTEMPTS,param);
 	if(loginAttempts != null)
 	{
 		if(loginAttempts >= 3)
 		{
 			//EnableCaptcha
 			setCaptchaEnabled(true);
 		}
 		Object[] updateParam = new Object[2];
 		updateParam[0] = ++loginAttempts;
 		updateParam[1] = userName;
 		String sql = SQLConstants.UPDATE_LOGIN_ATTEMPTS;
 		this.preparedStatementUpdate(sql, updateParam, true);
 		return false;
 	}
 	return true;
 }
 	@Override
 	public Authentication authenticate(Authentication auth)
 			throws AuthenticationException {
 		calledFunction = AUTHENTICATE;
 		Object[] prepareParams = new Object[2];
 		prepareParams[0] = auth.getName();
 		prepareParams[1] = passwordEncoder.encodePassword((String)auth.getCredentials(),(String)auth.getName());
 		UserVO userVO = (UserVO)this.getRowByCriteria(SQLConstants.USER_LOGIN, prepareParams);
 		boolean isVerified = verifyLoginAttempts(auth.getName());
		if(userVO!=null && isVerified)
 		{
 			if(userVO.getUserName().equals(auth.getPrincipal()))
 			{
 				calledFunction = USER_ROLE;
 				Object[] param = new Object[1];
 				param[0] = userVO.getUserName();
 				String role = (String)this.getRowByCriteria(SQLConstants.USER_ROLE,param);
 				List<GrantedAuthority> authoritites = new ArrayList<GrantedAuthority>();
 				authoritites.add((new GrantedAuthorityImpl(role)));
 				UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(auth.getName(), auth.getCredentials(), authoritites);
 				return token;
 			}			
 		}
 		else
 		{
 			/*
 			calledFunction = GET_LOGIN_ATTEMPTS;
 			Object[] param = new Object[1];
 			param[0] = auth.getName();
 			Integer loginAttempts = (Integer) this.getRowByCriteria(SQLConstants.LOGIN_ATTEMPTS,param);
 			if(loginAttempts != null)
 			{
 				if(loginAttempts >= 2)
 				{
 					//EnableCaptcha
 					setCaptchaEnabled(true);
 				}
 				Object[] updateParam = new Object[2];
 				updateParam[0] = ++loginAttempts;
 				updateParam[1] = auth.getName();
 				String sql = SQLConstants.UPDATE_LOGIN_ATTEMPTS;
 				this.preparedStatementUpdate(sql, updateParam, true);
 			}*/
 			
 		}
 		throw new BadCredentialsException("Username/Password does not match for ");
 	}
 
 	
 	@Override
 	public boolean supports(Class<? extends Object> authentication) {
 		return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
 	}
 	
 	public UserVO getUserDetails(Authentication authentication){
 		
 		calledFunction = AUTHENTICATE;
 		Object[] prepareParams = new Object[2];
 		prepareParams[0] = authentication.getName();
 		prepareParams[1] = passwordEncoder.encodePassword((String)authentication.getCredentials(),(String)authentication.getName());
 		UserVO userVO = (UserVO)this.getRowByCriteria(SQLConstants.USER_LOGIN, prepareParams);
 		
 		calledFunction = GET_USER_DEPARTMENTS;
 		prepareParams = new Object[1];
 		prepareParams[0] = userVO.getId();
 		userVO.setDepartments((ArrayList<Integer>)getListByCriteria(SQLConstants.USER_DEPT, prepareParams));
 		
 		return userVO;
 		
 	}
 	
 	public boolean isLoggedIn() {
         Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
         return isAuthenticated(authentication);
     }
 	private boolean isAuthenticated(Authentication authentication) {
         return authentication != null && !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated();
     }
 
 	public ShaPasswordEncoder getPasswordEncoder() {
 		return passwordEncoder;
 	}
 
 	public void setPasswordEncoder(ShaPasswordEncoder passwordEncoder) {
 		this.passwordEncoder = passwordEncoder;
 	}
 	public String getEmailForUser(String userName) {
 		calledFunction = "getEmailForUser";
 		
 		Object[] param = new Object[1];
 		param[0] = userName;
 		String sql  = SQLConstants.GET_EMAIL_ID;
 		String result = (String)this.getRowByCriteria(sql, param);
 		return result;
 	}
 	public void setPasswordForUser(String userName, String passwd)
 	{
 		calledFunction = "setPasswordForUser";
 		Object[] param = new Object[2];
 		param[1]=userName;
 		param[0]=passwordEncoder.encodePassword(passwd, userName);
 		String sql = SQLConstants.UPDATE_PASSWORD;
 		preparedStatementUpdate(sql, param,true);
 		
 	}
 	public void unlockUser()
 	{
 		Object[] updateParam = new Object[2];
 		updateParam[0] = 0;
 		UserVO userVO = getUserDetails(SecurityContextHolder.getContext().getAuthentication());
 		if (userVO != null) 
 		{
 			updateParam[1] = userVO.getUserName();
 			String sql = SQLConstants.UPDATE_LOGIN_ATTEMPTS;
 			this.preparedStatementUpdate(sql, updateParam, true);
 			setCaptchaEnabled(false);	
 		}
 	}	
 }
