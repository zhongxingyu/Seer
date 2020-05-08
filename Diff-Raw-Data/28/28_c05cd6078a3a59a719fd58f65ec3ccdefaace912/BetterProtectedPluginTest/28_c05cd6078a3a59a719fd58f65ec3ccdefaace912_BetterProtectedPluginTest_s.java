 package tmc.BetterProtected;
 
 import org.hibernate.SessionFactory;
 import org.junit.Test;
 
 import static org.hamcrest.CoreMatchers.notNullValue;
 import static org.hamcrest.CoreMatchers.nullValue;
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 public class BetterProtectedPluginTest {
 
     @Test
     public void testGetHibernateSession() throws Exception {
         BetterProtectedPlugin plugin = new BetterProtectedPlugin();
 
         SessionFactory sessionFactory = plugin.getSessionFactory();
         assertThat(sessionFactory, is(nullValue()));
 
         plugin.onEnable();
         assertThat(sessionFactory, is(notNullValue()));
 
         plugin.onDisable();
         assertThat(sessionFactory.isClosed(), is(true));
     }
 }
