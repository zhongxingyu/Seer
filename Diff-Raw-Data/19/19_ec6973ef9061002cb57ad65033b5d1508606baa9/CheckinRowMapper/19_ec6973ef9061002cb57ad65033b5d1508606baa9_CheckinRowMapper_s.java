 /**
  * @Skilaverkefni2 Nov 7, 2010 Arnr Barkarson
  *
  * Copyright (c) Arnr Barkarson
  */
 package is.ru.honn.rusquare.data.content;
 
 import is.ruframework.data.RuAbstractRowMapper;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 /**
  * @author Arnr Barkarson
  *
  * @version 1.0, Nov 7, 2010
  */
 public class CheckinRowMapper extends RuAbstractRowMapper{
 
 	/* (non-Javadoc)
 	 * @see is.ruframework.data.RuAbstractRowMapper#mapRow(java.sql.ResultSet, int)
 	 */
 	@Override
 	public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
 		
 		Object checkin = new CheckinDTO( 	rs.getInt(1),
 											rs.getString(2), 
 									  		rs.getDate(3), 
 									  		rs.getString(4), 
 									  		rs.getInt(5));
 		add(checkin);
 		return checkin;
 	}

 }
