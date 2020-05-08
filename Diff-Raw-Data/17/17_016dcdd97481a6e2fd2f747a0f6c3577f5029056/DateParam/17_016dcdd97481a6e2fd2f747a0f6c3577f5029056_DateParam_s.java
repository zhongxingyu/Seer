 package it.dariofabbri.accesscontrol.service.rest.param;
 
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import javax.ws.rs.WebApplicationException;
 
 public class DateParam extends AbstractParam<Date> {
 	
 	private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX");
 	
 	public DateParam(String param) throws WebApplicationException {
 		super(param);
 	}
 
 	@Override
 	protected Date parse(String param) throws Throwable {
 		return df.parse(param);
 	}
 }
