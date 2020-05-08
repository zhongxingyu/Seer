 package org.uli.viewscope;
 
 
 import java.util.Map;
 import java.util.HashMap;
 
import org.springframework.beans.factory.config.CustomScopeConfigurer;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.Lazy;
 import org.springframework.context.annotation.Scope;
 
 /**
  * @author X112640
  *
  */
 @Configuration
 @Lazy
 public class SpringConfig {
    @Bean(name="counterBean")
    @Scope("view")
    public CounterBean getCounterBean() {
       return new CounterBean();
    }
    @Bean
    public CustomScopeConfigurer getCustomScopeConfigurer() {
       CustomScopeConfigurer csc = new CustomScopeConfigurer();
       Map<String, Object> scopes = new HashMap<String, Object>();
       scopes.put("view", new ViewScope());
       csc.setScopes(scopes);
       return csc;
    }
 }
