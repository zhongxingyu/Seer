 package eu.inmite.glass.rest;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Date;
 import java.util.Map;
 
import org.codehaus.jackson.map.ObjectMapper;

 import com.google.appengine.api.urlfetch.HTTPResponse;
 import com.google.appengine.api.urlfetch.URLFetchService;
 import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
 import com.google.common.collect.ImmutableMap;
 
import eu.inmite.glass.banking.model.fio.AccountStatement;
 import eu.inmite.glass.banking.utils.TemplateReplace;
 import eu.inmite.glass.banking.view.model.AccountInformationVO;
 
 public class FioRESTClient {
 	
 	private static FioRESTClient inst = null;
 	private URLFetchService httpClient;
 	
 	private static String url = "https://www.fio.cz/ib_api/rest/periods/${TOKEN}/${DATE_FROM}/${DATE_TO}/transactions.json";
 	
 	private FioRESTClient() {
 		super();
 		httpClient = URLFetchServiceFactory.getURLFetchService();
 	}
 	
	public static FioRESTClient getInstance() {
 		if (inst == null) {
 			inst = new FioRESTClient();
 		}
 		return inst;
 	}
 	
 	public AccountInformationVO fetchAccountInformation(String token) {
 		return this.fetchAccountInformation(token, new Date(System.currentTimeMillis() - 30*24*60*60*1000), new Date());
 	}
 	
 	public AccountInformationVO fetchAccountInformation(String token, Date fromDate, Date toDate) {
 		AccountInformationVO accountInformation = new AccountInformationVO();
		Map<String, String> map = ImmutableMap.<String, String>of("TOKEN", token, "DATE_FROM", "2013-01-25", "DATE_TO", "2013-06-22");
 		
 		try {
			String fixedURL = TemplateReplace.templateReplace(url, map);
			HTTPResponse response = httpClient.fetch(new URL(fixedURL));
			AccountStatement statement = new ObjectMapper().readValue(response.getContent(), AccountStatement.class);
			System.out.println(statement.toString());
			return null;
 		} catch (MalformedURLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			return null;
 		}
 		return accountInformation;
 	}
 
 }
