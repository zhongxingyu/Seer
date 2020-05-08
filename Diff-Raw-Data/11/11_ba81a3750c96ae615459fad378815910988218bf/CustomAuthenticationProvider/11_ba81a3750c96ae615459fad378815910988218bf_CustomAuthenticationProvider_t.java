 package com.gl.busk.web.security;
 
 import org.springframework.security.authentication.AuthenticationProvider;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.AuthenticationException;
 
 public class CustomAuthenticationProvider implements AuthenticationProvider {
 
 	@Override
 	public boolean supports(Class<? extends Object> arg0) {
 		return true;
 	}
 
 	@Override
 	public Authentication authenticate(Authentication auth)
 			throws AuthenticationException {
 //		String email = auth.getName();
 //		String password = (String) auth.getCredentials();
 //		User user = userServices.findUser(email, password);
 //		if( user == null )
 //		{
 //			throw new BadCredentialsException("Usuario o Password Invalido");
 //		} else {
 //			List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
 //			GrantedAuthority grantedAuthority = new GrantedAuthorityImpl("ROLE_USER");
 //			authorities.add( grantedAuthority  );
 //			UsernamePasswordAuthenticationToken authOk = new UsernamePasswordAuthenticationToken(auth.getPrincipal(), auth.getCredentials(),authorities);
 //			authOk.setDetails(user);
 //			return authOk;
 //		}
 		return null;
 	}
 
 
 }
