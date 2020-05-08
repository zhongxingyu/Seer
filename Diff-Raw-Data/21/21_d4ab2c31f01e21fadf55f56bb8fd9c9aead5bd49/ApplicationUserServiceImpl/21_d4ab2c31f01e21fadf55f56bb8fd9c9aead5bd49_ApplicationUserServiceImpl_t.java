 package com.tda.service.impl;
 
 import java.util.List;
 
 import org.springframework.dao.DataAccessException;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.tda.model.applicationuser.ApplicationUser;
 import com.tda.persistence.dao.ApplicationUserDAO;
 import com.tda.persistence.dao.AuthorityDAO;
 import com.tda.service.api.ApplicationUserService;
 
 public class ApplicationUserServiceImpl implements ApplicationUserService {
 	private ApplicationUserDAO applicationUserDAO;
 	private AuthorityDAO authorityDAO;
 
 	public ApplicationUserDAO getApplicationUserDAO() {
 		return applicationUserDAO;
 	}
 
 	public void setApplicationUserDAO(ApplicationUserDAO applicationUserDAO) {
 		this.applicationUserDAO = applicationUserDAO;
 	}
 
 	public AuthorityDAO getAuthorityDAO() {
 		return authorityDAO;
 	}
 
 	public void setAuthorityDAO(AuthorityDAO authorityDAO) {
 		this.authorityDAO = authorityDAO;
 	}
 
 	@Transactional(readOnly = true)
 	public UserDetails loadUserByUsername(String username)
 			throws UsernameNotFoundException, DataAccessException {
 
 		List<ApplicationUser> usersFound = applicationUserDAO
 				.findByUsername(username);
 
 		if (usersFound.size() == 0)
 			throw new UsernameNotFoundException("User: " + username
 					+ " not found");
 
 		return usersFound.get(0);
 	}
 
 	@Transactional
 	public void save(ApplicationUser applicationUser) {
 		applicationUserDAO.save(applicationUser);
 	}
 
 	@Transactional
 	public void delete(ApplicationUser applicationUser) {
 		applicationUserDAO.delete(applicationUser);
 	}
 
 	@Transactional
 	public void update(ApplicationUser applicationUser) {
 		applicationUserDAO.update(applicationUser);
 	}
 
 	@Transactional(readOnly = true)
 	public ApplicationUser findById(Long id) {
 		return applicationUserDAO.findById(id);
 	}
 
 	@Transactional(readOnly = true)
 	public List<ApplicationUser> findAll() {
 		return applicationUserDAO.findAll();
 	}
 
 	@Transactional
 	public void deleteById(Long id) {
 		applicationUserDAO.deleteById(id);
 	}
 
 	@Transactional(readOnly = true)
 	public int count() {
 		return applicationUserDAO.count();
 	}
 
 	@Transactional(readOnly = true)
 	public List<ApplicationUser> findByExample(ApplicationUser exampleObject) {
 		return applicationUserDAO.findByExample(exampleObject);
 	}
 }
