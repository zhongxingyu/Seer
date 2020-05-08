 package ngmep.osm.dao;
 /*
 	ngmep2osm - importador de datos de ngmep a openstreetmap
 	
 	Copyright (C) 2011-2012 Alberto Fernández <infjaf@gmail.com>
 	
 	This program is free software: you can redistribute it and/or modify
 	it under the terms of the GNU General Public License as published by
 	the Free Software Foundation, either version 3 of the License, or
 	(at your option) any later version.
 	
 	This program is distributed in the hope that it will be useful,
 	but WITHOUT ANY WARRANTY; without even the implied warranty of
 	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 	GNU General Public License for more details.
 	
 	You should have received a copy of the GNU General Public License
 	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.Map;
 
 import ngmep.osm.datamodel.User;
 
 public final class UserDAO {
     
     private final Map<Integer, User> cache = new HashMap<Integer, User>();
 
     private static UserDAO instance = new UserDAO();
     
     public static UserDAO getInstance() {
         return instance;
     }
     private UserDAO() {
         super();
     }
     public static final String QUERY = "select id, name from users where id = ?";
     public User getUser(final int userId) throws SQLException{
        if (cache.get(userId) == null){
             final User user = getUserFromBD(userId);
            cache.put(userId, user);
         }
        return cache.get(userId);
     }
     public User getUserFromBD(final int userId) throws SQLException{
     	User user = null;
     	try (PreparedStatement statement = Database.getConnection().prepareStatement(QUERY); ){
 	        statement.setInt(1, userId);
 	        try (ResultSet resultSet = statement.executeQuery();) {		       
 		        if (resultSet.next()){
 		            user = new User();
 		            user.setId(userId);
 		            user.setName(resultSet.getString("name"));
 		        }
 	        }
     	}    	
         return user;
     }
 }
