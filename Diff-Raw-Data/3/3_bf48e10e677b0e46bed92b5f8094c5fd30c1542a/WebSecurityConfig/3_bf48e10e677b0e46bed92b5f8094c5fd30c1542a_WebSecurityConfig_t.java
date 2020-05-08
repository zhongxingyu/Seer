 package com.ssm.llp.web.config;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
 import org.springframework.security.config.annotation.web.builders.HttpSecurity;
 import org.springframework.security.config.annotation.web.builders.WebSecurity;
 import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
 import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
 import org.springframework.security.core.userdetails.UserDetailsService;
 
 /**
  * @author rafizan.baharum
  * @since 9/14/13
  *        http://spring.io/blog/2013/07/03/spring-security-java-config-preview-web-security/
  */
 @Configuration
 @EnableWebSecurity
 public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
 
     @Autowired
     @Qualifier(value = "userDetailService")
     private UserDetailsService userDetailService;
 
     @Override
     public void configure(WebSecurity web) throws Exception {
         web
                 .ignoring()
                 .antMatchers("/resources/**");
     }
 
     @Override
     protected void configure(HttpSecurity http) throws Exception {
         http
                 .csrf().disable()
                 .authorizeRequests()
                 .antMatchers("/*").permitAll()
                 .antMatchers("/gate/**").permitAll()
                 .antMatchers("/validate/**").permitAll()
                 .antMatchers("/secure/**").hasRole("USER")
                 .anyRequest().authenticated()
                 .and()
                 .formLogin()
                 .loginProcessingUrl("/login")
                 .defaultSuccessUrl("/secure/dashboard")
                .failureUrl("/gate/in?login_error=1")
                .loginPage("/gate/in")
                 .permitAll()
                 .and()
                 .logout()
                 .logoutUrl("/logout")
                 .logoutSuccessUrl("/index")
                 .invalidateHttpSession(true);
     }
 
 
     @Override
     protected void registerAuthentication(AuthenticationManagerBuilder auth) throws Exception {
         auth.userDetailsService(userDetailService);
     }
 }
