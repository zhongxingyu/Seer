 package net.unit8.sastruts.easyapi.client;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 import java.util.List;
 
 import net.unit8.sastruts.easyapi.EasyApiException;
import net.unit8.sastruts.easyapi.MessageFormat;
import net.unit8.sastruts.easyapi.XStreamFactory;
 import net.unit8.sastruts.easyapi.testapp.dto.MuchMoneyDto;
 import net.unit8.sastruts.easyapi.testapp.dto.PersonDto;
 import net.unit8.sastruts.easyapi.testapp.dto.UserDto;
import net.unit8.sastruts.easyapi.xstream.io.CsvStreamXmlDriver;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.seasar.extension.jdbc.IterationCallback;
 import org.seasar.extension.jdbc.IterationContext;
 import org.seasar.framework.beans.util.BeanMap;
 import org.seasar.framework.unit.Seasar2;
 import org.seasar.framework.unit.TestContext;
 
 @RunWith(Seasar2.class)
 public class EasyApiClientTest {
 	private TestContext ctx;
 	@Test
 	public void testPost() {
 		EasyApiClient client = ctx.getComponent(EasyApiClient.class);
 		MuchMoneyDto muchMoneyDto = new MuchMoneyDto();
 		try {
 			muchMoneyDto.amount = 10000;
 			client.post(muchMoneyDto).to("citiBank").execute();
 			assertThat(muchMoneyDto.tokenGift, is("towel"));
 			assertThat(muchMoneyDto.amount, is(10000));
 		} catch (EasyApiException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Test
 	public void testGet() throws EasyApiException {
 		EasyApiClient client = ctx.getComponent(EasyApiClient.class);
 		BeanMap query = new BeanMap();
 		query.put("id", "3");
 		query.put("name", "hogehoge");
 		UserDto user = client
 				.get(UserDto.class, query)
 				.from("familyRegister")
 				.getSingleResult();
 		assertThat(user.name, is("Yoshitaka Kawashima"));
 	}
 
 	@Test
 	public void testGetJson() throws EasyApiException {
 		EasyApiClient client = ctx.getComponent(EasyApiClient.class);
 		BeanMap query = new BeanMap();
 		query.put("id", "3");
 		query.put("name", "hogehoge");
 		UserDto user = client
 				.get(UserDto.class, query)
 				.from("familyRegisterJson")
 				.getSingleResult();
 		assertThat(user.name, is("Yoshitaka Kawashima"));
 	}
 
 	@Test
 	public void testGetCsv() throws EasyApiException {
 		EasyApiClient client = ctx.getComponent(EasyApiClient.class);
 		BeanMap query = new BeanMap();
 		query.put("id", "3");
 		query.put("name", "hogehoge");
 		List<UserDto> user = client
 				.get(UserDto.class, query)
 				.from("familyRegisterCsv")
 				.getResultList();
 		assertThat(user.size(), is(3));
 		assertThat(user.get(0).name, is("Yoshitaka Kawashima"));
 		assertThat(user.get(1).name, is("Jon \"Bon Jovi"));
 		assertThat(user.get(2).name, is("John,\nLennon"));
 	}
 
 	@Test
 	public void testGetCsvStream() throws EasyApiException {
 		EasyApiClient client = ctx.getComponent(EasyApiClient.class);
 		BeanMap query = new BeanMap();
 		query.put("id", "3");
 		query.put("name", "hogehoge");
 
 		int res = client
 				.get(PersonDto.class, query)
 				.from("people")
 				.iterate(new IterationCallback<PersonDto, Integer>() {
 					int i=0;
 					@Override
 					public Integer iterate(PersonDto person, IterationContext context) {
 						System.out.println((i++) + ":" + person.name);
 						return i;
 					}
 				});
 		assertThat(res, is(200));
 	}
 
 	@Test
 	public void testPostParam() {
 		EasyApiClient client = ctx.getComponent(EasyApiClient.class);
 		MuchMoneyDto muchMoneyDto = new MuchMoneyDto();
 
 		try {
 			muchMoneyDto.amount = 10000;
 			client.post(muchMoneyDto).to("paramPost").execute();
 		} catch (EasyApiException e) {
 			e.printStackTrace();
 		}
 	}
 }
