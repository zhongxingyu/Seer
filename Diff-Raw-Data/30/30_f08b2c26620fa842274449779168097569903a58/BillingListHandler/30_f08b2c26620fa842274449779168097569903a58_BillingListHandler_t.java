 package de.uni.stuttgart.informatik.ToureNPlaner.Net.Handler;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 import de.uni.stuttgart.informatik.ToureNPlaner.Data.BillingItem;
 import de.uni.stuttgart.informatik.ToureNPlaner.Net.JacksonManager;
 import de.uni.stuttgart.informatik.ToureNPlaner.Net.Observer;
 import de.uni.stuttgart.informatik.ToureNPlaner.Net.Session;
 import de.uni.stuttgart.informatik.ToureNPlaner.Net.JacksonManager.ContentType;
 import de.uni.stuttgart.informatik.ToureNPlaner.UI.Activities.BillingScreen;
 
 public class BillingListHandler extends ConnectionHandler{
private String limitString = "limit=50";
 private String offsetString = "offset=0";
 private Integer offset = 0;
 	public BillingListHandler(Observer listener, Session session) {
 		super(listener, session);
 	
 	}
 	public void setLimit(int i){
 		i+=50;
 //		if(i >= 150){
 //			limitString = "limit="+Integer.valueOf(i);
 //		
 //			offset +=75;
 //		setOffset(i-50);
 //		}
 			limitString = "limit="+Integer.valueOf(i);
 	}
 	public void setOffset(int i){
 		offsetString = "offset="+Integer.valueOf(i);
 	}
 	@Override
 	protected String getSuffix() {
 		return "/listrequests?" + limitString + "&" + offsetString;
 	}
 	@Override
 	protected boolean isPost() {
 		return false;
 	}
 
 	@Override
 	protected Object handleInput(ContentType type, InputStream inputStream)throws Exception {
 		ObjectMapper mapper = JacksonManager.getMapper(type);
 
 		BillingScreen.billinglist = ((BillingItem.parse(mapper.readValue(inputStream, JsonNode.class))));
 	    return true;
 	}
 
 
 }
