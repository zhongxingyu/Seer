 package net.canang.cfi.biz.integration.springsecurity;
 
 import net.canang.cfi.core.so.dao.CfPrincipalDao;
 import net.canang.cfi.core.so.model.CfMetaState;
 import net.canang.cfi.core.so.model.CfPrincipalRole;
 import net.canang.cfi.core.so.model.CfUser;
 import org.apache.log4j.Logger;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.dao.DataAccessException;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.authority.SimpleGrantedAuthority;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UserDetailsService;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * @author rafizan.baharum
  * @since 9/11/13
  */
 @Service("userDetailService")
 @Transactional
 public class CfUserDetailService implements UserDetailsService {
 
     private static final Logger log = Logger.getLogger(CfUserDetailService.class);
 
     @Autowired
     protected SessionFactory sessionFactory;
 
     @Autowired
     private CfPrincipalDao principalDao;
 
     @Override
     public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException, DataAccessException {
         log.debug("loading username: " + s);
         CfUser user = null;
         Session session = sessionFactory.getCurrentSession();
         Query query = session.createQuery("select u from CfUser u where u.name = :username " +
                 "and u.metadata.state = :state");
         query.setString("username", s);
         query.setInteger("state", CfMetaState.ACTIVE.ordinal());
         user = (CfUser) query.uniqueResult();
         if (user == null)
             throw new UsernameNotFoundException("No such user");
        log.debug(user.getUsername() + " " + user.getPassword());
         return new CfUserDetails(user, loadGrantedAuthoritiesFor(user));
     }
 
     private Set<GrantedAuthority> loadGrantedAuthoritiesFor(CfUser user) {
         Set<GrantedAuthority> grantedAuthorities = new HashSet<GrantedAuthority>();
 //        try {
             //load all roles which ties to user
             for (CfPrincipalRole role : user.getRoles()) {
                 grantedAuthorities.add(new SimpleGrantedAuthority(role.getRoleType().name()));
             }
             log.info("load auth for " + user.getName() + "#" + user.getId());
          // XXX: will hook this up later
 //            grantedAuthorities.addAll(principalDao.loadEffectiveAuthorities(user));
 //        } catch (RecursiveGroupException e) {
 //            log.error(e.getMessage());
 //            grantedAuthorities.clear();
 //        }
         return grantedAuthorities;
     }
 
 
 }
