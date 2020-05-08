 package ua.kiev.naiv.drinkit.springconfig;
 
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.EnableAspectJAutoProxy;
 import org.springframework.web.servlet.config.annotation.EnableWebMvc;
 
 /**
  * @author pkolmykov
  */
 @Configuration
@ComponentScan({
        "ua.kiev.naiv.drinkit.cocktail.controller",
        "ua.kiev.naiv.drinkit.cocktail.common"
})
 @EnableWebMvc
 @EnableAspectJAutoProxy
 public class RestConfig {
 }
