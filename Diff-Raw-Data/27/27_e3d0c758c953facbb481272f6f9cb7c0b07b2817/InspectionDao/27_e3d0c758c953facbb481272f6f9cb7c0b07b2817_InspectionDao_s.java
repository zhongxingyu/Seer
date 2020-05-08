 package de.hswt.hrm.inspection.dao.jdbc;
 
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkNotNull;
 import static com.google.common.base.Preconditions.checkState;
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 
 import javax.inject.Inject;
 
 import org.apache.commons.dbutils.DbUtils;
 
 import com.google.common.base.Optional;
 
 import de.hswt.hrm.common.database.DatabaseFactory;
 import de.hswt.hrm.common.database.JdbcUtil;
 import de.hswt.hrm.common.database.NamedParameterStatement;
 import de.hswt.hrm.common.database.SqlQueryBuilder;
 import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.common.database.exception.ElementNotFoundException;
 import de.hswt.hrm.common.database.exception.SaveException;
 import de.hswt.hrm.contact.model.Contact;
 import de.hswt.hrm.inspection.dao.core.IInspectionDao;
 import de.hswt.hrm.inspection.dao.core.ILayoutDao;
 import de.hswt.hrm.inspection.model.Inspection;
 import de.hswt.hrm.inspection.model.Layout;
 import de.hswt.hrm.plant.dao.core.IPlantDao;
 import de.hswt.hrm.plant.model.Plant;
 import de.hswt.hrm.contact.dao.core.IContactDao;
 import de.hswt.hrm.photo.model.Photo;
 import de.hswt.hrm.photo.dao.core.IPhotoDao;
 import de.hswt.hrm.scheme.dao.core.ISchemeDao;
 import de.hswt.hrm.scheme.model.Scheme;
 
 public class InspectionDao implements IInspectionDao {
    // FIXME Make dao injectable
    ILayoutDao layoutDao = new LayoutDao();
 
     private final IContactDao contactDao;
     private final IPlantDao plantDao;
     private final IPhotoDao photoDao;
     private final ISchemeDao schemeDao;
    
     @Inject
    public InspectionDao(IContactDao contactDao, IPlantDao plantDao, IPhotoDao photoDao, ISchemeDao schemeDao) {
         this.contactDao = contactDao;
         this.plantDao = plantDao;
         this.photoDao = photoDao;
         this.schemeDao = schemeDao;

     }
 
     @Override
     public Collection<Inspection> findAll() throws DatabaseException {
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.select(TABLE_NAME, Fields.ID, Fields.LAYOUT_FK, Fields.PLANT_FK,
                 Fields.REQUESTER_FK, Fields.CONTRACTOR_FK, Fields.CHECKER_FK,
                 Fields.INSPECTIONDATE, Fields.REPORTDATE, Fields.NEXTDATE, Fields.TEMPERATURE,
                 Fields.HUMIDITY, Fields.SUMMARY, Fields.TITEL, Fields.TEMPERATURERATING,
                 Fields.TEMPERATUREQUANTIFIER, Fields.HUMIDITYRATING, Fields.HUMIDITYQUANTIFIER,
                 Fields.FRONTPICTURE_FK, Fields.PLANTPICTURE_FK, Fields.TEMPERATURECOMMENT,
                 Fields.HUMIDITYCOMMENT, Fields.LEGIONELLA, Fields.LEGIONELLARATING,
                 Fields.LEGIONELLAQUANTIFIER, Fields.LEGIONELLACOMMENT, Fields.GERMS,
                 Fields.GERMSRATING, Fields.GERMSQUANTIFIER, Fields.GERMSCOMMENT, Fields.SCHEME);
 
         String query = builder.toString();
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
 
                 ResultSet rs = stmt.executeQuery();
                 Collection<Inspection> inspections = fromResultSet(rs,
                         Optional.<Inspection> absent());
                 rs.close();
 
                 return inspections;
             }
         }
         catch (SQLException e) {
             throw new DatabaseException("Unexpected error.", e);
         }
     }
    
     @Override
     public Scheme findScheme(Inspection inspection) throws DatabaseException {
    	checkNotNull(inspection, "Inspection must not be null.");
    	checkState(inspection.getId() >= 0, "ID must be non negative.");
 
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.select(TABLE_NAME, Fields.SCHEME);
         builder.where(Fields.ID);
        
         String query = builder.toString();
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 stmt.setParameter(Fields.ID, inspection.getId());
 
                 ResultSet rs = stmt.executeQuery();
                 rs.next();
                 int schemeId = JdbcUtil.getId(rs, Fields.SCHEME);
                 checkState(schemeId >= 0, "Invalid scheme ID returned from database.");
                 DbUtils.closeQuietly(rs);
                
                 return schemeDao.findById(schemeId);
             }
         }
         catch (SQLException e) {
             throw new DatabaseException("Unknown error.", e);
         }
     }
 
     @Override
     public Inspection findById(int id) throws DatabaseException, ElementNotFoundException {
         checkArgument(id >= 0, "ID must be non negative.");
 
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.select(TABLE_NAME, Fields.ID, Fields.LAYOUT_FK, Fields.PLANT_FK,
                 Fields.REQUESTER_FK, Fields.CONTRACTOR_FK, Fields.CHECKER_FK,
                 Fields.INSPECTIONDATE, Fields.REPORTDATE, Fields.NEXTDATE, Fields.TEMPERATURE,
                 Fields.HUMIDITY, Fields.SUMMARY, Fields.TITEL, Fields.TEMPERATURERATING,
                 Fields.TEMPERATUREQUANTIFIER, Fields.HUMIDITYRATING, Fields.HUMIDITYQUANTIFIER,
                 Fields.FRONTPICTURE_FK, Fields.PLANTPICTURE_FK, Fields.TEMPERATURECOMMENT,
                 Fields.HUMIDITYCOMMENT, Fields.LEGIONELLA, Fields.LEGIONELLARATING,
                 Fields.LEGIONELLAQUANTIFIER, Fields.LEGIONELLACOMMENT, Fields.GERMS,
                 Fields.GERMSRATING, Fields.GERMSQUANTIFIER, Fields.GERMSCOMMENT, Fields.SCHEME);
         builder.where(Fields.ID);
 
         String query = builder.toString();
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 stmt.setParameter(Fields.ID, id);
 
                 ResultSet rs = stmt.executeQuery();
                 Collection<Inspection> ratings = fromResultSet(rs, Optional.<Inspection> absent());
                 rs.close();
 
                 if (ratings.isEmpty()) {
                     throw new ElementNotFoundException();
                 }
                 else if (ratings.size() > 1) {
                     throw new DatabaseException("ID is not unique.");
                 }
 
                 return ratings.iterator().next();
             }
         }
         catch (SQLException e) {
             throw new DatabaseException("Unknown error.", e);
         }
     }
 
     @Override
     public Inspection insert(Inspection inspection) throws SaveException {
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.insert(TABLE_NAME, Fields.ID, Fields.LAYOUT_FK, Fields.PLANT_FK,
                 Fields.REQUESTER_FK, Fields.CONTRACTOR_FK, Fields.CHECKER_FK,
                 Fields.INSPECTIONDATE, Fields.REPORTDATE, Fields.NEXTDATE, Fields.TEMPERATURE,
                 Fields.HUMIDITY, Fields.SUMMARY, Fields.TITEL, Fields.TEMPERATURERATING,
                 Fields.TEMPERATUREQUANTIFIER, Fields.HUMIDITYRATING, Fields.HUMIDITYQUANTIFIER,
                 Fields.FRONTPICTURE_FK, Fields.PLANTPICTURE_FK, Fields.TEMPERATURECOMMENT,
                 Fields.HUMIDITYCOMMENT, Fields.LEGIONELLA, Fields.LEGIONELLARATING,
                 Fields.LEGIONELLAQUANTIFIER, Fields.LEGIONELLACOMMENT, Fields.GERMS,
                 Fields.GERMSRATING, Fields.GERMSQUANTIFIER, Fields.GERMSCOMMENT, Fields.SCHEME);
 
         final String query = builder.toString();
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 stmt.setParameter(Fields.LAYOUT_FK, inspection.getLayout().getId());
                 stmt.setParameter(Fields.PLANT_FK, inspection.getPlant().getId());
                 stmt.setParameter(Fields.REQUESTER_FK, inspection.getRequester().get().getId());
                 stmt.setParameter(Fields.CONTRACTOR_FK, inspection.getContractor().get().getId());
                 stmt.setParameter(Fields.CHECKER_FK, inspection.getChecker().get().getId());
                 stmt.setParameter(Fields.INSPECTIONDATE,
                         JdbcUtil.timestampFromCalendar(inspection.getInspectionDate()));
                 stmt.setParameter(Fields.REPORTDATE,
                         JdbcUtil.timestampFromCalendar(inspection.getReportDate()));
                 stmt.setParameter(Fields.NEXTDATE,
                         JdbcUtil.timestampFromCalendar(inspection.getNextInspectionDate()));
                 stmt.setParameter(Fields.TEMPERATURE, inspection.getTemperature().orNull());
                 stmt.setParameter(Fields.HUMIDITY, inspection.getHumidity().orNull());
                 stmt.setParameter(Fields.SUMMARY, inspection.getSummary().orNull());
                 stmt.setParameter(Fields.TITEL, inspection.getTitle());
                 stmt.setParameter(Fields.TEMPERATURERATING, inspection.getTemperatureRating()
                         .orNull());
                 stmt.setParameter(Fields.TEMPERATUREQUANTIFIER, inspection
                         .getTemperatureQuantifier().orNull());
                 stmt.setParameter(Fields.HUMIDITYRATING, inspection.getHumidityRating().orNull());
                 stmt.setParameter(Fields.HUMIDITYQUANTIFIER, inspection.getHumidityQuantifier()
                         .orNull());
                 stmt.setParameter(Fields.FRONTPICTURE_FK, inspection.getFrontpicture().get()
                         .getId());
                 stmt.setParameter(Fields.PLANTPICTURE_FK, inspection.getPlantpicture().get()
                         .getId());
                 stmt.setParameter(Fields.TEMPERATURECOMMENT, inspection.getAirtemperatureComment()
                         .orNull());
                 stmt.setParameter(Fields.HUMIDITYCOMMENT, inspection.getHumidityComment().orNull());
                 stmt.setParameter(Fields.LEGIONELLA, inspection.getLegionella().orNull());
                 stmt.setParameter(Fields.LEGIONELLARATING, inspection.getLegionellaRating()
                         .orNull());
                 stmt.setParameter(Fields.LEGIONELLAQUANTIFIER, inspection.getLegionellaQuantifier()
                         .orNull());
                 stmt.setParameter(Fields.LEGIONELLACOMMENT, inspection.getLegionellaComment()
                         .orNull());
                 stmt.setParameter(Fields.GERMS, inspection.getGerms().orNull());
                 stmt.setParameter(Fields.GERMSRATING, inspection.getGermsRating().orNull());
                 stmt.setParameter(Fields.GERMSQUANTIFIER, inspection.getGermsQuantifier().orNull());
                 stmt.setParameter(Fields.GERMSCOMMENT, inspection.getGermsComment().orNull());
                 stmt.setParameter(Fields.SCHEME, inspection.getScheme().getId());
 
                 int affectedRows = stmt.executeUpdate();
                 if (affectedRows != 1) {
                     throw new SaveException();
                 }
 
                 try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                     if (generatedKeys.next()) {
                         int id = generatedKeys.getInt(1);
 
                         // Create new Inspection with id
                         Inspection inserted = new Inspection(id, inspection.getReportDate(),
                                 inspection.getInspectionDate(), inspection.getNextInspectionDate(),
                                 inspection.getTitle(), inspection.getLayout(),
                                 inspection.getPlant(), inspection.getScheme());
 
                         inserted.setRequester(inspection.getRequester().orNull());
                         inserted.setContractor(inspection.getContractor().orNull());
                         inserted.setChecker(inspection.getChecker().orNull());
                         inserted.setTemperature(inspection.getTemperature().orNull());
                         inserted.setHumidity(inspection.getHumidity().orNull());
                         inserted.setSummary(inspection.getSummary().orNull());
                         inserted.setTemperatureRating(inspection.getTemperatureRating().orNull());
                         inserted.setTemperatureQuantifier(inspection.getTemperatureQuantifier()
                                 .orNull());
                         inserted.setHumidityRating(inspection.getHumidityRating().orNull());
                         inserted.setHumidityQuantifier(inspection.getHumidityQuantifier().orNull());
                         inserted.setFrontpicture(inspection.getFrontpicture().orNull());
                         inserted.setPlantpicture(inspection.getPlantpicture().orNull());
                         inserted.setAirtemperatureComment(inspection.getAirtemperatureComment()
                                 .orNull());
                         inserted.setHumidityComment(inspection.getHumidityComment().orNull());
                         inserted.setLegionella(inspection.getLegionella().orNull());
                         inserted.setLegionellaRating(inspection.getLegionellaRating().orNull());
                         inserted.setLegionellaQuantifier(inspection.getLegionellaQuantifier()
                                 .orNull());
                         inserted.setLegionellaComment(inspection.getLegionellaComment().orNull());
                         inserted.setGerms(inspection.getGerms().orNull());
                         inserted.setGermsRating(inspection.getGermsRating().orNull());
                         inserted.setGermsQuantifier(inspection.getGermsQuantifier().orNull());
                         inserted.setGermsComment(inspection.getGermsComment().orNull());
 
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
     public void update(Inspection inspection) throws ElementNotFoundException, SaveException {
         checkNotNull(inspection, "inspection must not be null.");
 
         if (inspection.getId() < 0) {
             throw new ElementNotFoundException("Element has no valid ID.");
         }
 
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.update(TABLE_NAME, Fields.ID, Fields.LAYOUT_FK, Fields.PLANT_FK,
                 Fields.REQUESTER_FK, Fields.CONTRACTOR_FK, Fields.CHECKER_FK,
                 Fields.INSPECTIONDATE, Fields.REPORTDATE, Fields.NEXTDATE, Fields.TEMPERATURE,
                 Fields.HUMIDITY, Fields.SUMMARY, Fields.TITEL, Fields.TEMPERATURERATING,
                 Fields.TEMPERATUREQUANTIFIER, Fields.HUMIDITYRATING, Fields.HUMIDITYQUANTIFIER,
                 Fields.FRONTPICTURE_FK, Fields.PLANTPICTURE_FK, Fields.TEMPERATURECOMMENT,
                 Fields.HUMIDITYCOMMENT, Fields.LEGIONELLA, Fields.LEGIONELLARATING,
                 Fields.LEGIONELLAQUANTIFIER, Fields.LEGIONELLACOMMENT, Fields.GERMS,
                 Fields.GERMSRATING, Fields.GERMSQUANTIFIER, Fields.GERMSCOMMENT, Fields.SCHEME);
         builder.where(Fields.ID);
 
         final String query = builder.toString();
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 stmt.setParameter(Fields.LAYOUT_FK, inspection.getLayout().getId());
                 stmt.setParameter(Fields.PLANT_FK, inspection.getPlant().getId());
                 stmt.setParameter(Fields.REQUESTER_FK, inspection.getRequester().get().getId());
                 stmt.setParameter(Fields.CONTRACTOR_FK, inspection.getContractor().get().getId());
                 stmt.setParameter(Fields.CHECKER_FK, inspection.getChecker().get().getId());
                 stmt.setParameter(Fields.INSPECTIONDATE,
                         JdbcUtil.timestampFromCalendar(inspection.getInspectionDate()));
                 stmt.setParameter(Fields.REPORTDATE,
                         JdbcUtil.timestampFromCalendar(inspection.getReportDate()));
                 stmt.setParameter(Fields.NEXTDATE,
                         JdbcUtil.timestampFromCalendar(inspection.getNextInspectionDate()));
                 stmt.setParameter(Fields.TEMPERATURE, inspection.getTemperature().orNull());
                 stmt.setParameter(Fields.HUMIDITY, inspection.getHumidity().orNull());
                 stmt.setParameter(Fields.SUMMARY, inspection.getSummary().orNull());
                 stmt.setParameter(Fields.TITEL, inspection.getTitle());
                 stmt.setParameter(Fields.TEMPERATURERATING, inspection.getTemperatureRating()
                         .orNull());
                 stmt.setParameter(Fields.TEMPERATUREQUANTIFIER, inspection
                         .getTemperatureQuantifier().orNull());
                 stmt.setParameter(Fields.HUMIDITYRATING, inspection.getHumidityRating().orNull());
                 stmt.setParameter(Fields.HUMIDITYQUANTIFIER, inspection.getHumidityQuantifier()
                         .orNull());
                 stmt.setParameter(Fields.FRONTPICTURE_FK, inspection.getFrontpicture().get()
                         .getId());
                 stmt.setParameter(Fields.PLANTPICTURE_FK, inspection.getPlantpicture().get()
                         .getId());
                 stmt.setParameter(Fields.TEMPERATURECOMMENT, inspection.getAirtemperatureComment()
                         .orNull());
                 stmt.setParameter(Fields.HUMIDITYCOMMENT, inspection.getHumidityComment().orNull());
                 stmt.setParameter(Fields.LEGIONELLA, inspection.getLegionella().orNull());
                 stmt.setParameter(Fields.LEGIONELLARATING, inspection.getLegionellaRating()
                         .orNull());
                 stmt.setParameter(Fields.LEGIONELLAQUANTIFIER, inspection.getLegionellaQuantifier()
                         .orNull());
                 stmt.setParameter(Fields.LEGIONELLACOMMENT, inspection.getLegionellaComment()
                         .orNull());
                 stmt.setParameter(Fields.GERMS, inspection.getGerms().orNull());
                 stmt.setParameter(Fields.GERMSRATING, inspection.getGermsRating().orNull());
                 stmt.setParameter(Fields.GERMSQUANTIFIER, inspection.getGermsQuantifier().orNull());
                 stmt.setParameter(Fields.GERMSCOMMENT, inspection.getGermsComment().orNull());
                 stmt.setParameter(Fields.SCHEME, inspection.getScheme().getId());
 
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
 
     private Collection<Inspection> fromResultSet(final ResultSet rs, Optional<Inspection> inspection)
             throws SQLException, ElementNotFoundException, DatabaseException {
 
         checkNotNull(rs, "Result must not be null.");
         Collection<Inspection> inspectionList = new ArrayList<>();
         while (rs.next()) {
             int id = rs.getInt(Fields.ID);
 
             // mandatory fk's
             int layoutId = JdbcUtil.getId(rs, Fields.LAYOUT_FK);
             checkArgument(layoutId >= 0, "Invalid layout key from database.");
             Layout layout = layoutDao.findById(layoutId);
             int plantId = JdbcUtil.getId(rs, Fields.PLANT_FK);
             checkArgument(plantId >= 0, "Invalid plant key from database.");
             Plant plant = plantDao.findById(plantId);
             int schemeId = JdbcUtil.getId(rs, Fields.SCHEME);
             checkArgument(schemeId >= 0, "Invalid scheme key from database");
             Scheme scheme = schemeDao.findById(schemeId);
 
             // optional fks's
             int requesterId = JdbcUtil.getId(rs, Fields.REQUESTER_FK);
             int contractorId = JdbcUtil.getId(rs, Fields.CONTRACTOR_FK);
             int checkerId = JdbcUtil.getId(rs, Fields.CHECKER_FK);
             int frontpictureId = JdbcUtil.getId(rs, Fields.FRONTPICTURE_FK);
             int plantpictureId = JdbcUtil.getId(rs, Fields.PLANTPICTURE_FK);
 
             // rest
             float temperature = rs.getFloat(Fields.TEMPERATURE);
             float humidity = rs.getFloat(Fields.HUMIDITY);
             String summary = rs.getString(Fields.SUMMARY);
             String title = rs.getString(Fields.TITEL);
             int temperatureRating = rs.getInt(Fields.TEMPERATURERATING);
             int temperatureQuantifier = rs.getInt(Fields.TEMPERATUREQUANTIFIER);
             int humidityRating = rs.getInt(Fields.HUMIDITYRATING);
             int humidityQuantifier = rs.getInt(Fields.HUMIDITYQUANTIFIER);
             String airtemperatureComment = rs.getString(Fields.TEMPERATURECOMMENT);
             String humidityComment = rs.getString(Fields.HUMIDITYCOMMENT);
             float legionella = rs.getFloat(Fields.LEGIONELLA);
             int legionellaRating = rs.getInt(Fields.LEGIONELLARATING);
             int legionellaQuantifier = rs.getInt(Fields.LEGIONELLAQUANTIFIER);
             String legionellaComment = rs.getString(Fields.LEGIONELLACOMMENT);
             float germs = rs.getFloat(Fields.GERMS);
             int germsRating = rs.getInt(Fields.GERMSRATING);
             int germsQuantifier = rs.getInt(Fields.GERMSQUANTIFIER);
             String germsComment = rs.getString(Fields.GERMSCOMMENT);
 
             // calendars
             Date date = rs.getDate(Fields.INSPECTIONDATE);
             checkNotNull(date, "Inspection date is mandatory.");
             Calendar inspectionDate = JdbcUtil.calendarFromDate(date);
 
             date = rs.getDate(Fields.REPORTDATE);
             checkNotNull(date, "Report date is mandatory.");
             Calendar reportDate = JdbcUtil.calendarFromDate(date);
 
             date = rs.getDate(Fields.NEXTDATE);
             checkNotNull(date, "Next date is mandatory.");
             Calendar nextInspectionDate = JdbcUtil.calendarFromDate(date);
 
             Inspection inserted = new Inspection(id, reportDate, inspectionDate,
                     nextInspectionDate, title, layout, plant, scheme);
 
             // optional fk's
             if (requesterId >= 0) {
                 Contact requester = contactDao.findById(requesterId);
                 inserted.setRequester(requester);
             }
             if (contractorId >= 0) {
                 Contact contractor = contactDao.findById(contractorId);
                 inserted.setContractor(contractor);
             }
             if (checkerId >= 0) {
                 Contact checker = contactDao.findById(checkerId);
                 inserted.setChecker(checker);
             }
             if (frontpictureId >= 0) {
                 Photo frontpicture = photoDao.findById(frontpictureId);
                 inserted.setFrontpicture(frontpicture);
             }
             if (plantpictureId >= 0) {
                 Photo plantpicture = photoDao.findById(plantpictureId);
                 inserted.setFrontpicture(plantpicture);
             }
 
             // rest
             inserted.setTemperature(temperature);
             inserted.setHumidity(humidity);
             inserted.setSummary(summary);
             inserted.setTemperatureRating(temperatureRating);
             inserted.setTemperatureQuantifier(temperatureQuantifier);
             inserted.setHumidityRating(humidityRating);
             inserted.setHumidityQuantifier(humidityQuantifier);
             inserted.setAirtemperatureComment(airtemperatureComment);
             inserted.setHumidityComment(humidityComment);
             inserted.setLegionella(legionella);
             inserted.setLegionellaRating(legionellaRating);
             inserted.setLegionellaQuantifier(legionellaQuantifier);
             inserted.setLegionellaComment(legionellaComment);
             inserted.setGerms(germs);
             inserted.setGermsRating(germsRating);
             inserted.setGermsQuantifier(germsQuantifier);
             inserted.setGermsComment(germsComment);
 
             inspectionList.add(inserted);
         }
 
         return inspectionList;
     }
 
     private static final String TABLE_NAME = "Report";
 
     private static final class Fields {
         public static final String ID = "Report_ID";
         public static final String LAYOUT_FK = "Report_Layout_FK";
         public static final String PLANT_FK = "Report_Plant_FK";
         public static final String REQUESTER_FK = "Report_Requester_FK";
         public static final String CONTRACTOR_FK = "Report_Contractor_FK";
         public static final String CHECKER_FK = "Report_Checker_FK";
         public static final String INSPECTIONDATE = "Report_Jobdate";
         public static final String REPORTDATE = "Report_Reportdate";
         public static final String NEXTDATE = "Report_Nextdate";
         public static final String TEMPERATURE = "Report_Airtemperature";
         public static final String HUMIDITY = "Report_Humidity";
         public static final String SUMMARY = "Report_Summary";
         public static final String TITEL = "Report_Titel";
         public static final String TEMPERATURERATING = "Report_Airtemperature_Rating";
         public static final String TEMPERATUREQUANTIFIER = "Report_Airtemperature_Quantifier";
         public static final String HUMIDITYRATING = "Report_Humidity_Rating";
         public static final String HUMIDITYQUANTIFIER = "Report_Humidity_Quantifier";
         public static final String FRONTPICTURE_FK = "Report_Frontpicture_FK";
         public static final String PLANTPICTURE_FK = "Report_Plantpicture_FK";
         public static final String TEMPERATURECOMMENT = "Report_Airtemperature_Comment";
         public static final String HUMIDITYCOMMENT = "Report_Humidity_Comment";
         public static final String LEGIONELLA = "Report_Legionella";
         public static final String LEGIONELLARATING = "Report_Legionella_Rating";
         public static final String LEGIONELLAQUANTIFIER = "Report_Legionella_Quantifier";
         public static final String LEGIONELLACOMMENT = "Report_Legionella_Comment";
         public static final String GERMS = "Report_Germs";
         public static final String GERMSRATING = "Report_Germs_Rating";
         public static final String GERMSQUANTIFIER = "Report_Germs_Quantifier";
         public static final String GERMSCOMMENT = "Report_Germs_Comment";
         public static final String SCHEME = "Report_Scheme_FK";
 
     }
 
 }
