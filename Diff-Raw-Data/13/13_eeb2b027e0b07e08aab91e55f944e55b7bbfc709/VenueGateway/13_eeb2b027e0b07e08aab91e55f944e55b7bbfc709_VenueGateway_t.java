 /**
  * @(#)Skil2_honn Nov 6, 2010 Thordur Arnarson
  *
  * Copyright (c) Thordur Arnarson
  */
 package is.ru.honn.rusquare.data.content;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
 
 import is.ruframework.data.RuGenericDataAccess;
 
 /**
  * @author Thordur Arnarson
  * @version 1.0 Nov 6, 2010
  */
 public class VenueGateway extends RuGenericDataAccess implements DataGateway{
 	public int add(ContentDTO cDTO) {
		SimpleJdbcInsert insertContent = new SimpleJdbcInsert(getDataSource()).withTableName("venues").usingGeneratedKeyColumns("id");
 		Map<String, Object> parameters = new HashMap<String, Object>(7);
 		parameters.put("name", ((VenueDTO) cDTO).getName());
 		parameters.put("street", ((VenueDTO) cDTO).getStreet());
 		parameters.put("city", ((VenueDTO) cDTO).getCity());
 		parameters.put("country", ((VenueDTO) cDTO).getCountry());
 		parameters.put("postalCode", ((VenueDTO) cDTO).getPostalCode());
		parameters.put("venuetype", ((VenueDTO) cDTO).getVenueType());
		parameters.put("totalcheckins", ((VenueDTO) cDTO).getTotalCheckins());
 		return insertContent.executeAndReturnKey(parameters).intValue();
 
 }
 
 	/* (non-Javadoc)
 	 * @see is.ru.honn.rusquare.data.content.DataGateway#getContent()
 	 */
 	@Override
 	public List getContent() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 }
