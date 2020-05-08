 /*
 * The MIT License
 *
 * Original work sponsored and donated by National Board of e-Health (NSI), Denmark (http://www.nsi.dk)
 *
 * Copyright (C) 2011 National Board of e-Health (NSI), Denmark (http://www.nsi.dk)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 * $HeadURL$
 * $Id$
 */
 package dk.nsi.minlog.config;
 
 import static org.mockito.Mockito.*;
 
 import javax.sql.DataSource;
 
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.context.annotation.Bean;
 import org.springframework.context.annotation.Configuration;
 import org.springframework.context.annotation.EnableAspectJAutoProxy;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.EnableTransactionManagement;
 
 import com.avaje.ebean.EbeanServer;
 import com.avaje.ebean.springsupport.factory.EbeanServerFactoryBean;
 
 /**
  * Simple test to see if spring is correctly setup, and all dependecies are met.
  *
  * Notice this demands some mocking as we do not want to setup alot of external resources.
  * 
  * @author kpi
  *
  */
 
 @RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes={ApplicationRootConfig.class, ConfigTest.DatabaseConfigConfigTest.class, ConfigTest.WSConfigTest.class})
 public class ConfigTest {
 
 	@BeforeClass
 	public static void setup(){
 		System.setProperty("sosi.production", "false");
 	}
 			
 	@Test
 	public void test() {
 		//Test is acually empty, because its a success if the setup does not blow up.
 	}
 	
 	@Configuration
 	@EnableTransactionManagement
 	public static class DatabaseConfigConfigTest extends DatabaseConfig{
 		
 		@Bean
 		@Override
 		public DataSource dataSource(){
 			return mock(DataSource.class);
 		}
 
 		
 		@Override
 		public EbeanServerFactoryBean ebeanServer(DataSource dataSource) throws Exception {
 			return mock(EbeanServerFactoryBean.class);
 		}
 	}
 
 	@Configuration
 	@EnableAspectJAutoProxy(proxyTargetClass = true)
 	public static class WSConfigTest extends WSConfig{
 
 		@Bean
 		public EbeanServer ebeanServer(){
 			return mock(EbeanServer.class);
 		}

 	}
 }
