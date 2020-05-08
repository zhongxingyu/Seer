 #set( $symbol_pound = '#' )
 #set( $symbol_dollar = '$' )
 #set( $symbol_escape = '\' )
 package ${package};
 
 import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.test.test.config.ServletConfig;
 import org.junit.Before;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.MediaType;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.ContextHierarchy;
 import org.springframework.test.context.TestExecutionListeners;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
 import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
 import org.springframework.test.context.transaction.TransactionConfiguration;
 import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
 import org.springframework.test.context.web.WebAppConfiguration;
 import org.springframework.test.web.servlet.MockMvc;
 import org.springframework.test.web.servlet.setup.MockMvcBuilders;
 import org.springframework.web.context.WebApplicationContext;
 
 import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @WebAppConfiguration
 @TestExecutionListeners({ DependencyInjectionTestExecutionListener.class,
         DirtiesContextTestExecutionListener.class,
         TransactionalTestExecutionListener.class,
         DbUnitTestExecutionListener.class })
 @ContextHierarchy({
         @ContextConfiguration("classpath*:test-applicationContext.xml"),
         @ContextConfiguration(classes = {ServletConfig.class})
 })
 @TransactionConfiguration(defaultRollback = true)
 public abstract class AbstractWebTests {
 
     public static final String APPLICATION_JSON_UTF_8 = "application/json;charset=UTF-8";
 
     protected MockMvc mockMvc;
 
     @Autowired
     protected WebApplicationContext wac;
 
     @Before
     public void setup() {
         mockMvc = MockMvcBuilders.webAppContextSetup(wac)
                 .alwaysExpect(content().contentType(APPLICATION_JSON_UTF_8))
                 .build();
     }
 
 }
