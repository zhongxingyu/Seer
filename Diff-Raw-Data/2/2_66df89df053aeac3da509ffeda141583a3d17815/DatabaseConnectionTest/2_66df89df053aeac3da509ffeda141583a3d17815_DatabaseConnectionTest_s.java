 import com.serveeasy.dao.DaoRepository;
 import com.serveeasy.dao.table.TableDao;
 import junit.framework.TestCase;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 /**
  * Created by IntelliJ IDEA.
  * User: cristian.popovici
  */
 public class DatabaseConnectionTest extends TestCase {
 
     public void testConnection() {
         ApplicationContext ctx =
                 new ClassPathXmlApplicationContext("com/serveeasy/context/applicationContext.xml");
 
 
         DaoRepository repository = (DaoRepository) ctx.getBean("daoRepository");
         TableDao dao = repository.getTableDao();
     }
 
 
 }
