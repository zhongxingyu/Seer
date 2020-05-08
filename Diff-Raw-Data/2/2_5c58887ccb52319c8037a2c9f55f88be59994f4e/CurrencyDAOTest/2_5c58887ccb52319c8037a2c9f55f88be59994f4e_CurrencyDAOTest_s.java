 package pl.jw.currency.exchange.kantor2008;
 
 import java.util.List;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
 
 import pl.jw.currency.exchange.api.CurrencyData;
 import pl.jw.currency.exchange.api.ICurrencyDAO;
 
@ContextConfiguration(locations = "classpath:application-context-dto-kantor.xml")
 public class CurrencyDAOTest extends AbstractJUnit4SpringContextTests {
 
 	@Autowired
 	private ICurrencyDAO currencyDAO;
 
 	public void setCurrencyDAO(ICurrencyDAO currencyDAO) {
 		this.currencyDAO = currencyDAO;
 	}
 
 	@Test
 	public void get() {
 		List<CurrencyData> list = currencyDAO.get();
 		Assert.assertEquals("", 12, list.size());
 	}
 
 }
