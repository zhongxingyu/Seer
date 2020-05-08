 package com.cqlybest.common.auth;
 
 import java.util.Collection;
import java.util.Collections;
 
 import org.springframework.security.authentication.AbstractAuthenticationToken;
 import org.springframework.security.core.GrantedAuthority;
 
 import com.cqlybest.common.bean.LoginUser;
 
 public class QQAuthToken extends AbstractAuthenticationToken {
 
   private static final long serialVersionUID = -7795121296695826713L;
 
   private LoginUser user;
 
   public QQAuthToken(LoginUser user) {
    this(Collections.<GrantedAuthority>emptyList());
     this.user = user;
   }
 
   private QQAuthToken(Collection<? extends GrantedAuthority> authorities) {
     super(authorities);
     setAuthenticated(true);
   }
 
   @Override
   public Object getCredentials() {
     return null;
   }
 
   @Override
   public Object getPrincipal() {
     return user;
   }
 
 }
