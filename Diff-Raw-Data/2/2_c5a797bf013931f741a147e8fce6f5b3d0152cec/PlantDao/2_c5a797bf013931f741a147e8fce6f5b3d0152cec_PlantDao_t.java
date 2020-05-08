 package de.hswt.hrm.plant.dao.jdbc;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import javax.inject.Inject;
 
 import org.apache.commons.dbutils.DbUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import static com.google.common.base.Preconditions.*;
 
 import de.hswt.hrm.common.database.DatabaseFactory;
 import de.hswt.hrm.common.database.NamedParameterStatement;
 import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.common.database.exception.ElementNotFoundException;
 import de.hswt.hrm.common.database.exception.SaveException;
 import de.hswt.hrm.place.dao.core.IPlaceDao;
 import de.hswt.hrm.place.model.Place;
 import de.hswt.hrm.plant.model.Plant;
 import de.hswt.hrm.plant.dao.core.IPlantDao;
 
 public class PlantDao implements IPlantDao {
     private final static Logger LOG = LoggerFactory.getLogger(PlantDao.class);
     private final IPlaceDao placeDao;
 
     @Inject
     public PlantDao(final IPlaceDao placeDao) {
         checkNotNull(placeDao, "PlaceDao not injected properly.");
 
         this.placeDao = placeDao;
         LOG.debug("PlaceDao injected into PlantDao.");
     }
 
     @Override
     public Collection<Plant> findAll() throws DatabaseException {
 
         final String query = "SELECT Plant_ID, Plant_Place_FK,"
                 + "Plant_Manufacturer, Plant_Year_Of_Construction, Plant_Type, "
                 + "Plant_Airperformance, Plant_Motorpower, Plant_Motor_Rpm, Plant_Ventilatorperformance, "
                 + "Plant_Current, Plant_Voltage, Plant_Note, Plant_Description, Plant_Area, Plant_Location FROM Plant ;";
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 ResultSet result = stmt.executeQuery();
 
                 Collection<Plant> plants = fromResultSet(result);
                 DbUtils.closeQuietly(result);
                 return plants;
             }
         }
         catch (SQLException e) {
             throw new DatabaseException(e);
         }
     }
 
     @Override
     public Plant findById(int id) throws DatabaseException, ElementNotFoundException {
         checkArgument(id >= 0, "Id must not be negative.");
 
         final String query = "SELECT Plant_ID, Plant_Place_FK, "
                 + "Plant_Manufacturer, Plant_Year_Of_Construction, Plant_Type, "
                 + "Plant_Airperformance, Plant_Motorpower, Plant_Motor_Rpm, Plant_Ventilatorperformance, "
                 + "Plant_Current, Plant_Voltage, Plant_Note, Plant_Description, Plant_Area, Plant_Location FROM Plant "
                 + "WHERE Plant_ID =:id;";
         ;
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 stmt.setParameter("id", id);
                 ResultSet result = stmt.executeQuery();
 
                 Collection<Plant> plants = fromResultSet(result);
                 DbUtils.closeQuietly(result);
 
                 if (plants.size() < 1) {
                     throw new ElementNotFoundException();
                 }
                 else if (plants.size() > 1) {
                     throw new DatabaseException("ID '" + id + "' is not unique.");
                 }
 
                 return plants.iterator().next();
             }
         }
         catch (SQLException e) {
             throw new DatabaseException(e);
         }
     }
 
     /**
      * @see {@link IPlantDao#insert(Plant)}
      */
     @Override
     public Plant insert(Plant plant) throws SaveException {
         // FIXME: we must add a transaction here!
         // Handle dependency
         insertPlaceIfNecessary(plant);
 
         final String query = "INSERT INTO Plant (Plant_Place_FK, "
                 + "Plant_Manufacturer, Plant_Year_Of_Construction, Plant_Type, "
                 + "Plant_Airperformance, Plant_Motorpower, Plant_Motor_Rpm, Plant_Ventilatorperformance, "
                 + "Plant_Current, Plant_Voltage, Plant_Note, Plant_Description, Plant_Area, Plant_Location) "
                 + "VALUES (:plantPlaceFk, :manufactor, :constructionYear, :type, "
                 + ":airPerformance, :motorPower, :motorRpm, :ventilatorPerformance, :current, :voltage, "
                 + ":note, :description, :area, :location);";
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 stmt.setParameter("description", plant.getDescription());
                 stmt.setParameter("plantPlaceFk", plant.getPlace().get().getId());
                 stmt.setParameter("constructionYear", plant.getConstructionYear().orNull());
                 stmt.setParameter("manufactor", plant.getManufactor().orNull());
                 stmt.setParameter("type", plant.getType().orNull());
                 stmt.setParameter("airPerformance", plant.getAirPerformance().orNull());
                 stmt.setParameter("motorPower", plant.getMotorPower().orNull());
                 stmt.setParameter("motorRpm", plant.getMotorRpm().orNull());
                 stmt.setParameter("ventilatorPerformance", plant.getVentilatorPerformance()
                         .orNull());
                 stmt.setParameter("current", plant.getCurrent().orNull());
                 stmt.setParameter("voltage", plant.getVoltage().orNull());
                 stmt.setParameter("note", plant.getNote().orNull());
                 stmt.setParameter("area", plant.getArea());
                 stmt.setParameter("location", plant.getLocation());
 
                 int affectedRows = stmt.executeUpdate();
                 if (affectedRows != 1) {
                     throw new SaveException();
                 }
 
                 try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                     if (generatedKeys.next()) {
                         int id = generatedKeys.getInt(1);
 
                         // Create new plant with id
                         Plant inserted = new Plant(id, plant.getDescription(), plant.getArea(), plant.getLocation());
 
                         inserted.setConstructionYear(plant.getConstructionYear().orNull());
                         inserted.setManufactor(plant.getManufactor().orNull());
                         inserted.setType(plant.getType().orNull());
                         inserted.setAirPerformance(plant.getAirPerformance().orNull());
                         inserted.setMotorPower(plant.getMotorPower().orNull());
                         inserted.setMotorRpm(plant.getMotorRpm().orNull());
                         inserted.setVentilatorPerformance(plant.getVentilatorPerformance().orNull());
                         inserted.setCurrent(plant.getCurrent().orNull());
                         inserted.setVoltage(plant.getVoltage().orNull());
                         inserted.setNote(plant.getNote().orNull());
                         inserted.setPlace(plant.getPlace().orNull());
 
                         return inserted;
                     }
                     else {
                         throw new SaveException("Could not retrieve generated ID.");
                     }
                 }
             }
 
         }
         catch (SQLException | DatabaseException e) {
             throw new SaveException(e);
         }
     }
 
     @Override
     public void update(Plant plant) throws ElementNotFoundException, SaveException {
         checkNotNull(plant, "Plant must not be null.");
 
         if (plant.getId() < 0) {
             throw new ElementNotFoundException("Element has no valid ID.");
         }
 
         // FIXME: we must add a transaction here!
         // Handle dependency
         insertPlaceIfNecessary(plant);
 
         final String query = "UPDATE Plant SET " + "Plant_Place_FK = :plantPlaceFk, "
                 + "Plant_Manufacturer = :manufactor, "
                 + "Plant_Year_Of_Construction = :constructionYear, " + "Plant_Type = :type, "
                 + "Plant_Airperformance = :airPerformance, " + "Plant_Motorpower = :motorPower, "
                 + "Plant_Motor_Rpm = :motorRpm, "
                 + "Plant_Ventilatorperformance = :ventilatorPerformance, "
                 + "Plant_Current = :current, " + "Plant_Voltage = :voltage, "
                + "Plant_Note = :note, " + "Plant_Description = :description, "
                 + "Plant_Area = :area, " + "Plant_Location = :location "
                 + "WHERE Plant_ID = :id;";
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 stmt.setParameter("id", plant.getId());
                 stmt.setParameter("description", plant.getDescription());
                 stmt.setParameter("plantPlaceFk", plant.getPlace().get().getId());
                 stmt.setParameter("constructionYear", plant.getConstructionYear().orNull());
                 stmt.setParameter("manufactor", plant.getManufactor().orNull());
                 stmt.setParameter("type", plant.getType().orNull());
                 stmt.setParameter("airPerformance", plant.getAirPerformance().orNull());
                 stmt.setParameter("motorPower", plant.getMotorPower().orNull());
                 stmt.setParameter("motorRpm", plant.getMotorRpm().orNull());
                 stmt.setParameter("ventilatorPerformance", plant.getVentilatorPerformance()
                         .orNull());
                 stmt.setParameter("current", plant.getCurrent().orNull());
                 stmt.setParameter("voltage", plant.getVoltage().orNull());
                 stmt.setParameter("note", plant.getNote().orNull());
                 stmt.setParameter("area", plant.getArea());
                 stmt.setParameter("location", plant.getLocation());
 
                 int affectedRows = stmt.executeUpdate();
                 if (affectedRows != 1) {
                     throw new SaveException();
                 }
             }
         }
         catch (SQLException | DatabaseException e) {
             throw new SaveException(e);
         }
     }
 
     private void insertPlaceIfNecessary(final Plant plant) throws SaveException {
         if (plant.getPlace().isPresent() && plant.getPlace().get().getId() < 0) {
             Place place = placeDao.insert(plant.getPlace().get());
             plant.setPlace(place);
         }
     }
 
     private Collection<Plant> fromResultSet(ResultSet rs) throws SQLException,
             ElementNotFoundException, DatabaseException {
 
         checkNotNull(rs, "Result must not be null.");
         Collection<Plant> plantList = new ArrayList<>();
 
         while (rs.next()) {
             // "SELECT Plant_ID, Plant_Place_FK, Plant_Inspection_Interval, "
             // + "Plant_Manufacturer, Plant_Year_Of_Construction, Plant_Type"
             // +
             // "Plant_Airperformance, Plant_Motorpower, Plant_Motor_Rpm, Plant_Ventilatorperformance, "
             // + "Plant_Current, Plant_Voltage, Plant_Note, Plant_Description FROM Plant ;";
 
             int id = rs.getInt("plant_ID");
             String description = rs.getString("Plant_Description");
             String area = rs.getString("Plant_Area");
             String location = rs.getString("Plant_Location");
 
             Plant plant = new Plant(id, description, area, location);
             plant.setConstructionYear(rs.getInt("Plant_Year_Of_Construction"));
             plant.setManufactor(rs.getString("Plant_Manufacturer"));
             plant.setType(rs.getString("Plant_Type"));
             plant.setAirPerformance(rs.getString("Plant_Airperformance"));
             plant.setMotorPower(rs.getString("Plant_Motorpower"));
             plant.setMotorRpm(rs.getString("Plant_Motor_Rpm"));
             plant.setVentilatorPerformance(rs.getString("Plant_Ventilatorperformance"));
             plant.setCurrent(rs.getString("Plant_Current"));
             plant.setVoltage(rs.getString("Plant_Voltage"));
             plant.setNote(rs.getString("Plant_Note"));
 
             // handle dependency
             int placeId = rs.getInt("Plant_Place_FK");
             Place place = null;
             if (placeId >= 0) {
                 place = placeDao.findById(placeId);
             }
             plant.setPlace(place);
 
             plantList.add(plant);
         }
 
         return plantList;
 
     }
 }
