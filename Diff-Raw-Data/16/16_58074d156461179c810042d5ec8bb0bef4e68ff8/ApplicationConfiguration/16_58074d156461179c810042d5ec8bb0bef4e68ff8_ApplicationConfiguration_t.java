 package se.bhg.photos.config;
 
 import javax.sql.DataSource;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.ComponentScan;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.core.env.Environment;
 import org.springframework.core.io.ClassPathResource;
 import org.springframework.core.io.FileSystemResource;
 import org.springframework.core.io.Resource;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.datasource.DriverManagerDataSource;
 import org.springframework.web.multipart.MultipartResolver;
 import org.springframework.web.multipart.commons.CommonsMultipartResolver;
 import org.springframework.web.servlet.ViewResolver;
 import org.springframework.web.servlet.config.annotation.EnableWebMvc;
 import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
 import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
 import org.thymeleaf.spring3.SpringTemplateEngine;
 import org.thymeleaf.spring3.view.ThymeleafViewResolver;
 import org.thymeleaf.templateresolver.ServletContextTemplateResolver;
 
 @EnableWebMvc
 @ComponentScan(basePackages = { "se.bhg.photos" })
 @Configuration
 public class ApplicationConfiguration extends WebMvcConfigurerAdapter {
     private final static String PROD_ENV = "yankton";
     private final static String PROD_PROPERTIES_PATH = "/etc/bhg/bhg-photos.properties";
 
     @Autowired
     private Environment env;
     
     @Value("${phpbb3mysql.username}")
     private  String username;
 
     @Value("${phpbb3mysql.password}") 
     private String password;
 
     @Bean
     public static PropertyPlaceholderConfigurer properties(Environment env) {
         Resource resource;
         PropertyPlaceholderConfigurer ppc = new PropertyPlaceholderConfigurer();
         if ((env.getActiveProfiles().length == 1 && PROD_ENV.equalsIgnoreCase(env.getActiveProfiles()[0]))) {
             resource = new FileSystemResource(PROD_PROPERTIES_PATH);
         } else {
             resource = new ClassPathResource("photos-" + env.getProperty("spring.profiles.active") + ".properties");
         }
         ppc.setLocations(new Resource[] {resource});
         ppc.setIgnoreResourceNotFound(true);
         return ppc;
     }
 
     @Override
     public void addResourceHandlers(ResourceHandlerRegistry registry) {
         registry.addResourceHandler("/assets/**").addResourceLocations("/assets/");
     }
 
     @Bean
     public MultipartResolver multipartResolver() {
         CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();
         commonsMultipartResolver.setMaxUploadSize(100000000);
         return commonsMultipartResolver;
     }
 
     @Bean
     public ServletContextTemplateResolver templateResolver() {
         ServletContextTemplateResolver servletContextTemplateResolver = new ServletContextTemplateResolver();
         servletContextTemplateResolver.setPrefix("/WEB-INF/templates/");
         servletContextTemplateResolver.setSuffix(".html");
         servletContextTemplateResolver.setTemplateMode("HTML5");
         // Template cache is true by default. Set to false if you want
         // templates to be automatically updated when modified.
         servletContextTemplateResolver.setCacheable(false);
         servletContextTemplateResolver.setCharacterEncoding("UTF-8");
         return servletContextTemplateResolver;
     }
 
     @Bean
     public SpringTemplateEngine templateEngine() {
         SpringTemplateEngine springTemplateEngine = new SpringTemplateEngine();
         springTemplateEngine.setTemplateResolver(templateResolver());
         return springTemplateEngine;
     }
 
     @Bean
     public ViewResolver thymeleafViewResolver() {
         ThymeleafViewResolver thymeleafViewResolver = new ThymeleafViewResolver();
         thymeleafViewResolver.setTemplateEngine(templateEngine());
         thymeleafViewResolver.setCharacterEncoding("UTF-8");
         return thymeleafViewResolver;
     }
 
     @Bean
     public DataSource dataSource() {
         DriverManagerDataSource ds = new DriverManagerDataSource();
 
         try {
             ds.setDriverClassName("com.mysql.jdbc.Driver");
             ds.setUsername(username);
             ds.setPassword(password);
             ds.setUrl("jdbc:mysql://localhost/bhgforum");
 
         } catch (Exception e) {
             //e.getMessage();
         }
         return ds;
     }
 
     @Bean
     public DataSource dataSourceImages() {
         DriverManagerDataSource ds = new DriverManagerDataSource();
 
         try {
             ds.setDriverClassName("com.mysql.jdbc.Driver");
             ds.setUsername(username);
             ds.setPassword(password);
            ds.setUrl("jdbc:mysql://localhost/bhg&characterEncoding=latin1");
 
         } catch (Exception e) {
             //e.getMessage();
         }
         return ds;
     }
 
     @Bean
     public JdbcTemplate jdbcTemplate() {
         return new JdbcTemplate(dataSource());
     }
 
     @Bean
     public JdbcTemplate jdbcTemplateImages() {
         return new JdbcTemplate(dataSourceImages());
     }
 }
