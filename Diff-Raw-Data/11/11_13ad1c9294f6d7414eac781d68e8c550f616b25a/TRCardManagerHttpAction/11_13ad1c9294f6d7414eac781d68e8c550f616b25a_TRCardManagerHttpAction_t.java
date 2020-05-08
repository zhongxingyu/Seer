 package com.trcardmanager.http;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.cookie.BasicClientCookie;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.HTTP;
 import org.apache.http.util.EntityUtils;
 import org.jsoup.Connection.Response;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import android.util.Log;
 
 import com.trcardmanager.dao.CardDao;
 import com.trcardmanager.dao.MovementDao;
 import com.trcardmanager.dao.MovementsDao;
 import com.trcardmanager.dao.UserDao;
 import com.trcardmanager.exception.TRCardManagerDataException;
 import com.trcardmanager.exception.TRCardManagerLoginException;
 import com.trcardmanager.exception.TRCardManagerUpdateCardException;
 import com.trcardmanager.string.TRCardManagerStringHelper;
 
 public class TRCardManagerHttpAction {
 
 	private static final int TIMEOUT = 5000;
 	private static final String LOGIN_RESPONSE_OK = "2";
 	
 	private static final String COOKIE_NAME = "CWNWSESSION";
     private static final String URL_BASE = "https://ticketrestaurant.edenred.es/TRC/";
     private static final String URL_LOGIN = "checkUserLogin.php";
     private static final String URL_BALANCE = "consulta_tarjeta.html";
     private static final String URL_MY_ACCOUNT = "mi_cuenta.html";
     private static final String TYPE_PARAMETER = "trc";
     private static final String URL_UPDATE_CARD = "sendMyAccountCard.php";
     private static final String UPDATE_CARD_ID_PARAMETER = "updCard";
     private static final String UPDATE_CARD_PROFILE_PARAMETER = "TRCU";
     private static final String UPDATE_CARD_RESPONSE_OK = "OK";
     private static final String URL_PREPARE_UPDATE_CARD = "mi_cuenta.html";
     private static final String PREPARE_UPDATE_CARD_START_SEARCH = "<input type=\"hidden\" name=\"id\" value=\"";
     private static final String PREPARE_UPDATE_CARD_END_SEARCH = "\">";
     
     private static final String CLASS_TO_SEARH_ACTUAL_BALANCE = "result";
     private static final String ID_TO_SEARCH_CARD_NUMBER = "num_card";
     private static final String ATTRIBUTE_TO_GET_PROPERTY_VALUE = "value";
     
        
     public void getCookieLogin(UserDao user) throws TRCardManagerLoginException, 
     		ClientProtocolException, IOException{
 		Map<String, String> postMap = new HashMap<String, String>();
 			postMap.put("user", user.getEmail());
 			postMap.put("passwd", user.getPassword());
 			postMap.put("type", TYPE_PARAMETER);
 		
 		Response response = Jsoup.connect(URL_BASE+URL_LOGIN).data(postMap).execute();
 		if(response != null){
 			String responseString = response.body();
 	    	if(LOGIN_RESPONSE_OK.equals(responseString)){
 	    		 String cookieValue = response.cookie(COOKIE_NAME);
 	    		 user.setCookieValue(cookieValue);
 	    	}else{
 	    		throw new TRCardManagerLoginException();
 	    	}
 		}else{
 			throw new TRCardManagerLoginException();
 		}
     }
     
     public void getActualCard(UserDao user) throws ClientProtocolException,
     		IOException, TRCardManagerDataException{
         Document htmlDocument = getHttpPage(URL_MY_ACCOUNT,user.getCookieValue());
         Element elNumCard = htmlDocument.getElementById(ID_TO_SEARCH_CARD_NUMBER);
         String numCardValue = elNumCard.attr(ATTRIBUTE_TO_GET_PROPERTY_VALUE);
         Log.i("", "consulta tarjeta get: " + numCardValue);
         CardDao card = new CardDao(numCardValue);
         user.setActualCard(card);
     }
     
     public void getActualCardBalanceAndMovements(UserDao user) throws ClientProtocolException,
 			IOException, TRCardManagerDataException{  
     	Document htmlDocument = getHttpPage(URL_BALANCE,user.getCookieValue());
         
     	String actualBalance = getActualBalance(htmlDocument);
         user.getActualCard().setBalance(actualBalance);
         
         List<MovementDao> movementList = getMovementsList(htmlDocument);
         MovementsDao movements = new MovementsDao();
         	movements.setMovements(movementList);
         	
         getPaginationMovements(movements,htmlDocument);
         
         user.getActualCard().setMovementsData(movements);
     }
     
     private String getActualBalance(Document htmlDocument){
     	Elements elementsResult = htmlDocument.getElementsByClass(CLASS_TO_SEARH_ACTUAL_BALANCE);
         Element elBalance = elementsResult.first();
         String balance = elBalance.html();
         Log.i("", "consulta saldo get: " + balance);
         int substringposition = balance.indexOf(" ");
         return balance.substring(0,
         		substringposition>0?substringposition:balance.length()-1);
     }
     
     private void getPaginationMovements(MovementsDao movementsDao,Document htmlDocument) throws ClientProtocolException,
 			IOException, TRCardManagerDataException{
     	
     	//SEARCH dates fromdate todate
     	String fromDate = htmlDocument.getElementById("fromdate").attr("value");
     	String toDate = htmlDocument.getElementById("todate").attr("value");
     	movementsDao.setDateStart(fromDate);
     	movementsDao.setDateEnd(toDate);
     	
     	//SEARH NUMBER OF PAGES class="tab_menu" --> class="derecha" --> <a /> hasta title="Imprimir"
     	Element tabMenu = htmlDocument.getElementsByClass("tab_menu").get(0);
     	Element pagesNumbers = tabMenu.getElementsByClass("derecha").get(0);
     	Elements pagesLinks = pagesNumbers.getElementsByTag("a");
     	
     	//FILL LIST OF MOVEMENTS FROM PAGES
     	List<String> listUrlLinks = new ArrayList<String>();
     	for(Element pageLink : pagesLinks){
     		String title = pageLink.attr("title");
     		//avoid imprimir link
     		if(title==null || "".equals(title.trim())){
     			//call href value
     			String href = pageLink.attr("href");
     			listUrlLinks.add(href);
     		}
     	}
     	movementsDao.setPaginationLinks(listUrlLinks);
     	movementsDao.setActualPage(0);
     	movementsDao.setNumberOfPages(listUrlLinks.size());
     }
     
     
     public void getMoreMovements(UserDao user) throws IOException{
     	MovementsDao movementsData = user.getActualCard().getMovementsData();
     	List<String> paginationUrls = movementsData.getPaginationLinks();
    	for(int actual=0;actual<paginationUrls.size();actual++){
    		if(actual != movementsData.getActualPage()){
	    		String url = paginationUrls.get(actual);
	    		Document htmlDocument = getHttpPage(url,user.getCookieValue());
	        	List<MovementDao> pageMovements = getMovementsList(htmlDocument);
	        	movementsData.getMovements().addAll(pageMovements);
    		}
     	}
     }
     
     
     private Document getHttpPage(String httpPage, String cookieValue) throws IOException{
     	return Jsoup.connect(URL_BASE+httpPage).cookie(COOKIE_NAME,cookieValue).timeout(TIMEOUT).get();
     }
     
     public String getPrepareUpdateCard(UserDao user) throws ClientProtocolException,
 		IOException, TRCardManagerDataException{
 		
     	String id = "";
     	DefaultHttpClient httpClient = new DefaultHttpClient();
 		HttpGet get = new HttpGet(URL_BASE+URL_PREPARE_UPDATE_CARD);
 		get.addHeader("Cookie", COOKIE_NAME+"="+user.getCookieValue());
 		
 		HttpResponse response = httpClient.execute(get);
 		HttpEntity entity = response.getEntity();
 		
 		Log.i("","prepare update card get: " + response.getStatusLine());
 		if (entity != null) {
 			String html = EntityUtils.toString(entity,HTTP.UTF_8);
 			id = new TRCardManagerStringHelper(html)
 				.getStringBetwen(PREPARE_UPDATE_CARD_START_SEARCH, PREPARE_UPDATE_CARD_END_SEARCH);
 		    System.out.println("id: "+id);
 		}
 		return id;
 	}
     
     public void activateCard(UserDao user, CardDao card) throws IOException, TRCardManagerUpdateCardException, TRCardManagerDataException{
     /*
 		var f = document.getElementById('updCard');
 		var _id = f.id.value;
 		var _profile = f.profile.value;
         var _num_card = f.num_card.value; 			
 	    
         $.ajax({
            url: "sendMyAccountCard.php",
            type: "POST",
            dataType: "text",
            data: {swlang: swlang, id: "updCard", profile: "TRCU", num_card: _num_card},	           		
            error: function(req, err, obj) {
     	 */
     	//TRC4e1ad6e9cc5d0
     	
     	//Get 
     	//<input type="hidden" name="id" value="TRC4e1ad6e9cc5d0">
     	String id = getPrepareUpdateCard(user);
     	
     	HttpPost post = new HttpPost(URL_BASE+URL_UPDATE_CARD);
     	List<NameValuePair> params = createPostListParameters(new String[][]{
     			{"swlang",Locale.getDefault().getCountry()},
     			{"id",id},
     			{"profile",UPDATE_CARD_PROFILE_PARAMETER},
     			{"num_card",card.getCardNumber()}
     	});
     	
 		post.setEntity(new UrlEncodedFormEntity(params,HTTP.UTF_8));
 		//post.addHeader("Cookie", COOKIE_NAME+"="+user.getCookieValue());
 		DefaultHttpClient httpClient = new DefaultHttpClient();
 		BasicClientCookie cookie = new BasicClientCookie(COOKIE_NAME, user.getCookieValue());
 		cookie.setDomain("ticketrestaurant.edenred.es");
 		cookie.setPath("/");
 		httpClient.getCookieStore().addCookie(cookie);
 		HttpResponse response = httpClient.execute(post);
 		HttpEntity entity = response.getEntity();
 
 		System.out.println("update card post response: " + response.getStatusLine());
 		if (entity != null){
 			String html = EntityUtils.toString(entity,HTTP.UTF_8); 
 			if(!UPDATE_CARD_RESPONSE_OK.equals(html)){
 				throw new TRCardManagerUpdateCardException(); 
 			}
 		}else{
 			throw new TRCardManagerUpdateCardException();
 		}
     }
  
     
    
     private List<MovementDao> getMovementsList(Document htmlDocument) throws IOException{
     	List<MovementDao> movementsList = new ArrayList<MovementDao>();
     
     	Element table =  htmlDocument.getElementsByClass("tab_movs").first();
     	Elements tableRows = table.getElementsByTag("tr");
     	int firstDataRow = 2;
     	for(int cont=firstDataRow;cont<tableRows.size();cont++){
     		MovementDao movement = getMovement(tableRows.get(cont));
     		Log.i("", "Movement found: "+movement.getTrade()+" --> "+movement.getAmount());
     		movementsList.add(movement);
     	}
     	return movementsList;
     }
     
     private MovementDao getMovement(Element dataRow){
     	MovementDao movement = new MovementDao();
     	Elements dataCells = dataRow.getElementsByTag("td");
     	String date = dataCells.get(0).html();
     	String hour = dataCells.get(1).html();
     	String operationType = dataCells.get(3).html();
     	
     	String amount = dataCells.get(4).html();
     	String trade = dataCells.get(5).html();
     	String state = dataCells.get(6).html();
     	
     	movement.setDate(getAtFirstWhiteSpace(date));
     	movement.setHour(getAtFirstWhiteSpace(hour));
     	movement.setOperationType(operationType);
     	movement.setAmount(getAtFirstWhiteSpace(amount)+"€");
     	movement.setTrade(trade);
     	movement.setState(state);
     	
     	return movement;
     }
     
     private String getAtFirstWhiteSpace(String value){
     	int pos = value.indexOf(" ");
     	pos = pos == -1?value.length():pos;
     	return value.substring(0,pos);
     }
     
     
     private List<NameValuePair> createPostListParameters(String[][] nameVaules){
     	List<NameValuePair> params = new ArrayList<NameValuePair>();
     	for(String[] nameValue : nameVaules){
     		params.add(new BasicNameValuePair(nameValue[0], nameValue[1]));
     	}
 		return params;
     }
 }
