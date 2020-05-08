 package se.bhg.photos.config;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.Import;
 import org.springframework.core.env.Environment;
 import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
 import org.springframework.security.config.annotation.web.builders.HttpSecurity;
 import org.springframework.security.config.annotation.web.builders.WebSecurity;
 import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
 import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
 
 import se.bhg.photos.util.PhpBB3AuthenticationProvider;
 
 @Configuration
 @EnableWebSecurity
 @Import({ApplicationConfiguration.class})
 public class BhgWebSecurityConfiguration extends WebSecurityConfigurerAdapter {
     private final static String PROD_ENV = "yankton";
     @Autowired
     private PhpBB3AuthenticationProvider phpBB3AuthenticationProvider;
    
    @Autowired
    private UserDetailsService userDetailsService;
 
     @Autowired
     private Environment env;
     
     @Value("${remember.me.key}") 
     private String key;
 
     @Override
     protected void registerAuthentication(AuthenticationManagerBuilder auth) throws Exception {
         if (env.getActiveProfiles().length == 1 && PROD_ENV.equalsIgnoreCase(env.getActiveProfiles()[0])) {
             auth.authenticationProvider(phpBB3AuthenticationProvider);
         } else {
             auth.inMemoryAuthentication().withUser("user").password("password").roles("USER");
         }
     }
 
     @Override
     public void configure(WebSecurity web) throws Exception {
       web
         .ignoring()
           .antMatchers("/assets/**"); // #3
     }
 
     @Override
     protected void configure(HttpSecurity http) throws Exception {
         http
           .csrf()
             .disable()
          .authorizeRequests()
           .anyRequest().authenticated() // 7
           .and()
       .formLogin()  // #8
           .loginPage("/login") // #9
           .permitAll()
       .and()
           .rememberMe()
              .key(key)
              .userDetailsService(userDetailsService);
     }
 }
