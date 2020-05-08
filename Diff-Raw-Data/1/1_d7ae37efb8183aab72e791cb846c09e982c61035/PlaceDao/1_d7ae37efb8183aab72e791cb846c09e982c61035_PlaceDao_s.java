 package de.hswt.hrm.place.dao.jdbc;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 
 
 import de.hswt.hrm.common.database.DatabaseFactory;
 import de.hswt.hrm.common.database.NamedParameterStatement;
 import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.common.database.exception.ElementNotFoundException;
 import de.hswt.hrm.common.database.exception.SaveException;
 import de.hswt.hrm.common.exception.NotImplementedException;
 import de.hswt.hrm.place.model.Place;
 import de.hswt.hrm.place.dao.core.IPlaceDao;
 
  
 public class PlaceDao implements IPlaceDao {
     
     @Override
     public Collection<Place> findAll() throws DatabaseException {
         throw new NotImplementedException();
     }
 
     @Override
     public Place findById(int id) throws DatabaseException, ElementNotFoundException {
         throw new NotImplementedException();
         }
     
 
     /**
      * @see {@link IPlaceDao#insert(Place)}
      */
     @Override
     public Place insert(Place place) throws SaveException {
         final String query = "INSERT INTO Place (Place_Name, Place_Zip_Code, "
                 + "Place_City, Place_Street, Place_Street_Number, Place_Location, "
                 + "Place_Area) " 
                 + "VALUES (:placeName, :zipCode, :city, :street, :streetNumber, :location, "
                 + ":area);";
         
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 stmt.setParameter("placeName", place.getPlaceName());
                 stmt.setParameter("zipCode", place.getPostCode());
                 stmt.setParameter("city", place.getCity());
                 stmt.setParameter("street", place.getStreet());
                 stmt.setParameter("streetNumber", place.getStreetNo());
                 stmt.setParameter("location", place.getLocation());
                 stmt.setParameter("area", place.getArea());
 
                 
                 int affectedRows = stmt.executeUpdate();
                 if (affectedRows != 1) {
                     throw new SaveException();
                 }
                 
                 try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                     if (generatedKeys.next()) {
                         int id = generatedKeys.getInt(1);
                         
                         // Create new Place with id
                         Place inserted = new Place(id, place.getPlaceName(), 
                                 place.getPostCode(), place.getCity(), 
                                 place.getStreet(), place.getStreetNo(), place.getLocation(),
                                 place.getArea());
 
                         
                         return inserted;
                     }
                     else {
                         throw new SaveException("Could not retrieve generated ID.");    
                     }
                 }
             }
             
         }
         catch (SQLException|DatabaseException e) {
             throw new SaveException(e);
         }
     }
 
     @Override
     public void update(Place place) throws ElementNotFoundException, SaveException {
         throw new NotImplementedException();        
     }
 
     private Collection<Place> fromResultSet(ResultSet rs) throws SQLException {
         checkNotNull(rs, "Result must not be null.");
         Collection<Place> placeList = new ArrayList<>();
    
         while (rs.next()) {
             int id = rs.getInt("Place_ID");
             String placeName = rs.getString("Place_Name");
             String postCode = rs.getString("Place_Zip_Code"); 
             String city = rs.getString("Place_City");
             String street = rs.getString("Place_Street");
             String streetNo = rs.getString("Place_Street_Number");
             String location = rs.getString("Place_Location");
             String area = rs.getString("Place_Area");
             
             Place place = new Place(id, placeName, postCode, city, street, streetNo, location,
                     area);
 
             
             placeList.add(place);
         }
         
         return placeList;
     }
 }
