 package de.hswt.hrm.inspection.dao.jdbc;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.base.Preconditions.checkState;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import javax.inject.Inject;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.hswt.hrm.common.database.DatabaseFactory;
 import de.hswt.hrm.common.database.DatabaseUtil;
 import de.hswt.hrm.common.database.JdbcUtil;
 import de.hswt.hrm.common.database.NamedParameterStatement;
 import de.hswt.hrm.common.database.SqlQueryBuilder;
 import de.hswt.hrm.common.database.exception.DatabaseException;
 import de.hswt.hrm.common.database.exception.ElementNotFoundException;
 import de.hswt.hrm.common.database.exception.SaveException;
 import de.hswt.hrm.inspection.dao.core.IBiologicalRatingDao;
 import de.hswt.hrm.inspection.dao.core.IInspectionDao;
 import de.hswt.hrm.inspection.model.BiologicalRating;
 import de.hswt.hrm.inspection.model.Inspection;
 import de.hswt.hrm.inspection.model.SamplingPointType;
 import de.hswt.hrm.scheme.dao.core.ISchemeComponentDao;
 import de.hswt.hrm.scheme.model.SchemeComponent;
 
 public class BiologicalRatingDao implements IBiologicalRatingDao {
     private final static Logger LOG = LoggerFactory.getLogger(BiologicalRatingDao.class);
     private final IInspectionDao inspectionDao;
     private final ISchemeComponentDao schemeComponentDao;
 
     @Inject
     public BiologicalRatingDao(final IInspectionDao inspectionDao,
             final ISchemeComponentDao schemeComponentDao) {
 
         checkNotNull(inspectionDao, "InspectionDao not properly injected to BiologicalRatingDao");
         checkNotNull(schemeComponentDao,
                 "SchemeComponentDao not properly injected to BiologicalRatingDao");
 
         this.inspectionDao = inspectionDao;
         LOG.debug("InspectionDao injected into BiologicalRatingDao.");
         this.schemeComponentDao = schemeComponentDao;
         LOG.debug("SchemeComponentDao injected into BiologicalRatingDao.");
     }
 
     @Override
     public Collection<BiologicalRating> findAll() throws DatabaseException {
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.select(TABLE_NAME, Fields.ID, Fields.BACTERIA, Fields.RATING, Fields.QUANTIFIER,
                 Fields.COMMENT, Fields.FK_COMPONENT, Fields.FK_REPORT, Fields.FLAG,
                 Fields.SAMPLINGPOINTTYPE);
 
         String query = builder.toString();
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
 
                 ResultSet rs = stmt.executeQuery();
                 Collection<BiologicalRating> ratings = fromResultSet(rs);
                 rs.close();
 
                 return ratings;
             }
         }
         catch (SQLException e) {
             throw new DatabaseException("Unexpected error.", e);
         }
     }
 
     @Override
     public BiologicalRating findById(final int id) throws DatabaseException,
             ElementNotFoundException {
 
         checkArgument(id >= 0, "ID must be non negative.");
 
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.select(TABLE_NAME, Fields.ID, Fields.BACTERIA, Fields.RATING, Fields.QUANTIFIER,
                 Fields.COMMENT, Fields.FK_COMPONENT, Fields.FK_REPORT, Fields.FLAG,
                 Fields.SAMPLINGPOINTTYPE);
 
         String query = builder.toString();
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 stmt.setParameter(Fields.ID, id);
 
                 ResultSet rs = stmt.executeQuery();
                 Collection<BiologicalRating> ratings = fromResultSet(rs);
                 rs.close();
 
                 if (ratings.isEmpty()) {
                     throw new ElementNotFoundException();
                 }
                 else if (ratings.size() > 1) {
                     throw new DatabaseException("ID '" + id + "' is not unique.");
                 }
 
                 return ratings.iterator().next();
             }
         }
         catch (SQLException e) {
             throw new DatabaseException("Unexpected error.", e);
         }
     }
 
     @Override
     public Collection<BiologicalRating> findByInspection(final Inspection inspection)
             throws DatabaseException {
 
         checkNotNull(inspection, "Inspection is mandatory.");
         checkArgument(inspection.getId() >= 0, "Inspection must have a valid ID.");
 
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.select(TABLE_NAME, Fields.ID, Fields.BACTERIA, Fields.RATING, Fields.QUANTIFIER,
                 Fields.COMMENT, Fields.FK_COMPONENT, Fields.FK_REPORT, Fields.FLAG,
                 Fields.SAMPLINGPOINTTYPE);
         builder.where(Fields.FK_REPORT);
 
         String query = builder.toString();
        
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
                 stmt.setParameter(Fields.FK_REPORT, inspection.getId());
 
                 ResultSet rs = stmt.executeQuery();
                 Collection<BiologicalRating> ratings = fromResultSet(rs);
                 rs.close();
                 return ratings;
             }
         }
         catch (SQLException e) {
             throw new DatabaseException("Unexpected error.", e);
         }
     }
 
     @Override
     public BiologicalRating insert(BiologicalRating biological) throws DatabaseException {
         checkNotNull(biological, "BiologicalRating must not be null.");
         checkState(biological.isValid(), "Biological Rating is invalid");
         checkState(biological.getComponent().getId() >= 0, "Component must have a valid ID.");
         checkState(biological.getInspection().getId() >= 0, "Inspection must have a valid ID.");
 
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.insert(TABLE_NAME, Fields.BACTERIA, Fields.RATING, Fields.QUANTIFIER,
                 Fields.COMMENT, Fields.FK_COMPONENT, Fields.FK_REPORT, Fields.FLAG,
                 Fields.SAMPLINGPOINTTYPE);
 
         String query = builder.toString();
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {
 
                 stmt.setParameter(Fields.BACTERIA, biological.getBacteriaCount());
                 stmt.setParameter(Fields.RATING, biological.getRating());
                 stmt.setParameter(Fields.QUANTIFIER, biological.getQuantifier());
                 stmt.setParameter(Fields.COMMENT, biological.getComment().orNull());
                 stmt.setParameter(Fields.FK_COMPONENT, biological.getComponent().getId());
                 stmt.setParameter(Fields.FK_REPORT, biological.getInspection().getId());
                 stmt.setParameter(Fields.FLAG, biological.getFlag().orNull());
                 if (biological.getSamplingPointType().isPresent()) {
                     stmt.setParameter(Fields.SAMPLINGPOINTTYPE, biological.getSamplingPointType()
                             .get().ordinal());
                 }
                 else {
                     stmt.setParameterNull(Fields.SAMPLINGPOINTTYPE);
                 }
 
                 int affectedRows = stmt.executeUpdate();
                 if (affectedRows != 1) {
                     throw new SaveException();
                 }
 
                 try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                     if (generatedKeys.next()) {
                         int id = generatedKeys.getInt(1);
 
                         // Create new biological rating with id
                         BiologicalRating inserted = new BiologicalRating(id,
                                 biological.getInspection(), biological.getComponent(),
                                 biological.getBacteriaCount(), biological.getRating(),
                                 biological.getQuantifier(), biological.getComment().orNull(),
                                 biological.getFlag().orNull());
                         inserted.setSamplingPointType(biological.getSamplingPointType().orNull());
                         
                         LOG.info("BiologicalRating inserted:\n"
                         		+ id + ", " 
                         		+ biological.getComponent().getComponent().getName() + ", "
                         		+ biological.getRating() + ", "
                         		+ biological.getQuantifier() + ", "
                         		+ biological.getFlag().or("NO FLAG SET!"));
                         
                         return inserted;
                     }
                     else {
                         throw new SaveException("Could not retrieve generated ID.");
                     }
                 }
             }
         }
         catch (SQLException e) {
             throw new DatabaseException("Unexpected error.", e);
         }
     }
 
     @Override
     public void update(BiologicalRating biological) 
             throws ElementNotFoundException, SaveException, DatabaseException {
         checkNotNull(biological, "Physical Rating must not be null.");
         checkState(biological.isValid(), "Biological Rating is invalid");
 
         if (biological.getId() < 0) {
             throw new ElementNotFoundException("Element has no valid ID.");
         }
 
         SqlQueryBuilder builder = new SqlQueryBuilder();
         builder.update(TABLE_NAME, Fields.BACTERIA, Fields.RATING, Fields.QUANTIFIER,
                 Fields.COMMENT, Fields.FK_COMPONENT, Fields.FK_REPORT, Fields.FLAG,
                 Fields.SAMPLINGPOINTTYPE);
         builder.where(Fields.ID);
 
         final String query = builder.toString();
 
         try (Connection con = DatabaseFactory.getConnection()) {
             try (NamedParameterStatement stmt = NamedParameterStatement.fromConnection(con, query)) {

                 stmt.setParameter(Fields.BACTERIA, biological.getBacteriaCount());
                 stmt.setParameter(Fields.RATING, biological.getRating());
                 stmt.setParameter(Fields.QUANTIFIER, biological.getQuantifier());
                 stmt.setParameter(Fields.COMMENT, biological.getComment().orNull());
                 stmt.setParameter(Fields.FK_COMPONENT, biological.getComponent().getId());
                 stmt.setParameter(Fields.FK_REPORT, biological.getInspection().getId());
                 stmt.setParameter(Fields.FLAG, biological.getFlag().orNull());
                 if (biological.getSamplingPointType().isPresent()) {
                     stmt.setParameter(Fields.SAMPLINGPOINTTYPE, biological.getSamplingPointType()
                             .get().ordinal());
                 }
                 else {
                     stmt.setParameterNull(Fields.SAMPLINGPOINTTYPE);
                 }
                 int affectedRows = stmt.executeUpdate();
                 if (affectedRows != 1) {
                     throw new SaveException();
                 }
                 
                 LOG.info("BiologicalRating updated:\n"
                 		+ biological.getId() + ", " 
                 		+ biological.getComponent().getComponent().getName() + ", "
                 		+ biological.getRating() + ", "
                 		+ biological.getQuantifier() + ", "
                 		+ biological.getFlag().or("NO FLAG SET!"));
             }
         }
         catch (SQLException e) {
             throw DatabaseUtil.createUnexpectedException(e);
         }
 
     }
 
     /**
      * All statements should join the flag table to be able to parse it correctly here...
      * 
      * @param rs
      * @return
      * @throws SQLException
      * @throws DatabaseException
      * @throws ElementNotFoundException
      */
     private Collection<BiologicalRating> fromResultSet(final ResultSet rs) throws SQLException,
             ElementNotFoundException, DatabaseException {
 
         checkNotNull(rs, "ResultSet must not be null.");
 
         Collection<BiologicalRating> ratingList = new ArrayList<>();
 
         while (rs.next()) {
             int id = rs.getInt(Fields.ID);
             int bacteria = rs.getInt(Fields.BACTERIA);
             int rating = rs.getInt(Fields.RATING);
             int quantifier = rs.getInt(Fields.QUANTIFIER);
             String comment = rs.getString(Fields.COMMENT);
             int componentId = JdbcUtil.getId(rs, Fields.FK_COMPONENT);
             checkState(componentId >= 0, "Invalid component ID returned from database");
             SchemeComponent component = schemeComponentDao.findById(componentId);
             int inspectionId = JdbcUtil.getId(rs, Fields.FK_REPORT);
             checkState(inspectionId >= 0, "Invalid report ID returned from database.");
             Inspection inspection = inspectionDao.findById(inspectionId);
             String flag = rs.getString(Fields.FLAG);
             SamplingPointType samplingPointType = SamplingPointType.values()[rs
                     .getInt(Fields.SAMPLINGPOINTTYPE)];
 
             BiologicalRating biological = new BiologicalRating(id, inspection, component, bacteria,
                     rating, quantifier, comment, flag);
             biological.setSamplingPointType(samplingPointType);
             ratingList.add(biological);
         }
 
         return ratingList;
     }
 
     private static final String TABLE_NAME = "Biological_Rating";
 
     private static final class Fields {
         public static final String ID = "Biological_Rating_ID";
         public static final String BACTERIA = "Biological_Rating_Bacteria_Count";
         public static final String RATING = "Biological_Rating_Rating";
         public static final String QUANTIFIER = "Biological_Rating_Quantifier";
         public static final String COMMENT = "Biological_Rating_Comment";
         public static final String FK_COMPONENT = "Biological_Rating_Component_FK";
         public static final String FK_REPORT = "Biological_Rating_Report_FK";
         public static final String FLAG = "Biological_Rating_Flag";
         public static final String SAMPLINGPOINTTYPE = "Biological_Rating_Sampling_TYPE_POINT";
     }
 }
