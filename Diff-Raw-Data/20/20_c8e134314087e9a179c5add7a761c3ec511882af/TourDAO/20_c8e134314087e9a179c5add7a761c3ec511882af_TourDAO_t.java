 package kz.enu.epam.azimkhan.tour.dao;
 
 import kz.enu.epam.azimkhan.tour.connection.ConnectionPool;
 import kz.enu.epam.azimkhan.tour.entity.Tour;
 import kz.enu.epam.azimkhan.tour.entity.TourType;
 import kz.enu.epam.azimkhan.tour.entity.User;
 import kz.enu.epam.azimkhan.tour.exception.ConnectionPoolException;
 import kz.enu.epam.azimkhan.tour.exception.DAOLogicalException;
 import kz.enu.epam.azimkhan.tour.exception.DAOTechnicalException;
 import org.apache.log4j.Logger;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * Tour DAO
  */
 public class TourDAO extends AbstractDAO<Integer, Tour>{
 	private final String SELECT_ALL = "SELECT id, tourname, details, hot, price, regular_discount, type FROM tour";
 	private final String FIND_BY_ID = "SELECT id, tourname, details, hot, price, regular_discount, type FROM tour WHERE id = ?";
     private final String CREATE_TOUR = "INSERT INTO tour(tourname, details, hot, price, regular_discount, type) VALUES(?, ?, ?, ?, ?, ?)";
     private final String UPDATE_BY_ID = "UPDATE tour SET tourname=?, details=?, hot=?, price=?, regular_discount=?, type=? WHERE id=?";
 	private final String DELETE_TOUR_BY_ID = "DELETE FROM tour WHERE id = ?";
 
 	private static final Logger logger = Logger.getRootLogger();
 
     private TourDAO(){}
 
     private static TourDAO instance;
 
     public static TourDAO getInstance(){
         if (instance == null){
             instance = new TourDAO();
         }
         return instance;
     }
 	@Override
 	public List<Tour> findAll() throws DAOLogicalException, DAOTechnicalException {
 		ConnectionPool connectionPool = null;
 		try{
 			connectionPool = ConnectionPool.getInstance();
 		} catch (ConnectionPoolException e){
 			throw new DAOTechnicalException(e);
 		}
 
 		Connection connection = connectionPool.getConnection();
 		PreparedStatement statement = null;
 
 		LinkedList<Tour> tours= new LinkedList<Tour>();
 
 		if (connection != null){
 			try {
 				statement = connection.prepareStatement(SELECT_ALL);
 				ResultSet set = statement.executeQuery();
 
 				while(set.next()){
 					Tour tour = createEntity(set);
 					tours.add(tour);
 				}
 
 				return tours;
 			} catch (SQLException e) {
 				throw new DAOLogicalException(e);
 			} finally {
 				if (null != statement) {
 					try {
 						statement.close();
 					} catch (SQLException e) {
 						logger.error(e.getMessage());
 					}
 				}
 				connectionPool.release(connection);
 			}
 		} else{
 			throw new DAOTechnicalException();
 		}
 	}
 
     /**
      * Find tour by id
      * @param id
      * @return
      * @throws DAOLogicalException
      * @throws DAOTechnicalException
      */
 	@Override
 	public Tour findById(Integer id) throws DAOLogicalException, DAOTechnicalException {
 		if (id != null){
 			ConnectionPool pool = null;
 			try {
 				pool = ConnectionPool.getInstance();
 			} catch (ConnectionPoolException e) {
 				throw new DAOLogicalException(e);
 			}
 
 			Connection connection = pool.getConnection();
 			PreparedStatement statement = null;
 
 			if (connection != null){
 				try {
 					statement = connection.prepareStatement(FIND_BY_ID);
 				   	statement.setInt(1, id);
 
 					ResultSet set = statement.executeQuery();
 
 					if (set.next()){
 						return createEntity(set);
 					} else{
 						throw new DAOLogicalException();
 					}
 				} catch (SQLException e) {
 					throw new DAOTechnicalException(e);
 				} finally {
 					if (statement != null){
 						try {
 							statement.close();
 						} catch (SQLException e) {
 							logger.error(e.getMessage());
 						}
 						pool.release(connection);
 					}
 				}
 
 			} else{
 				throw new DAOTechnicalException(NO_CONNECTION_MESSAGE);
 			}
 		} else {
 			throw new DAOLogicalException();
 		}
 	}
 
     /**
      * Delete tour by id
      * @param id
      * @return
      * @throws DAOLogicalException
      * @throws DAOTechnicalException
      */
 	@Override
 	public boolean delete(Integer id) throws DAOLogicalException, DAOTechnicalException {
         if (null != id) {
 
             ConnectionPool connectionPool = null;
             try{
                 connectionPool = ConnectionPool.getInstance();
             } catch (ConnectionPoolException e){
                 throw new DAOTechnicalException(e);
             }
 
             Connection connection = connectionPool.getConnection();
             PreparedStatement statement = null;
             if (connection != null) {
                 try {
                     statement = connection.prepareStatement(DELETE_TOUR_BY_ID);
                     statement.setInt(1, id);
                     int affected = statement.executeUpdate();
                     if (affected > 0){
                         return true;
                     } else{
                         throw new DAOLogicalException("No such record in database");
                     }
 
                 } catch (SQLException e) {
                     throw new DAOTechnicalException();
                 } finally {
                     if (null != statement) {
                         try {
                             statement.close();
                         } catch (SQLException e) {
                             logger.error(e.getMessage());
                         }
                     }
                     connectionPool.release(connection);
                 }
             } else{
                 throw new DAOTechnicalException(NO_CONNECTION_MESSAGE);
             }
         }
 
         return false;
 	}
 
     /**
      * Delete tour
      * @param entity
      * @return
      * @throws DAOLogicalException
      * @throws DAOTechnicalException
      */
 	@Override
 	public boolean delete(Tour entity) throws DAOLogicalException, DAOTechnicalException {
 		return false;  
 	}
 
     /**
      * Create tour
      * @param entity
      * @return
      * @throws DAOLogicalException
      * @throws DAOTechnicalException
      */
 	@Override
 	public boolean create(Tour entity) throws DAOLogicalException, DAOTechnicalException {
         if (entity != null){
             ConnectionPool connectionPool = null;
             try{
                 connectionPool = ConnectionPool.getInstance();
             } catch (ConnectionPoolException e){
                 throw new DAOTechnicalException(e);
             }
 
             Connection connection = connectionPool.getConnection();
             PreparedStatement statement = null;
             if (connection != null){
                 try {
                     statement = connection.prepareStatement(CREATE_TOUR);
                     statement.setString(1, entity.getTourname());
                     statement.setString(2, entity.getDetails());
                     statement.setBoolean(3, entity.isHot());
                     statement.setInt(4, entity.getPrice());
                     statement.setInt(5, entity.getRegularDiscount());
                     statement.setInt(6, entity.getType().getId());
 
                     int affected = statement.executeUpdate();
                     return (affected > 0);
 
                 } catch (SQLException e) {
                     throw new DAOTechnicalException(e.getMessage());
 
                 } finally {
                     if (null != statement) {
                         try {
                             statement.close();
                         } catch (SQLException e) {
                             logger.error(e.getMessage());
                         }
                     }
                     connectionPool.release(connection);
                 }
             } else{
                 throw new DAOTechnicalException(NO_CONNECTION_MESSAGE);
             }
         }
 
         return false;
 	}
 
 	@Override
 	public boolean update(Tour entity) throws DAOLogicalException, DAOTechnicalException {
 		if (entity != null){
 			ConnectionPool connectionPool = null;
 			try{
 				connectionPool = ConnectionPool.getInstance();
 			} catch (ConnectionPoolException e){
 				throw new DAOTechnicalException(e);
 			}
 
 			Connection connection = connectionPool.getConnection();
 			PreparedStatement statement = null;
 			if (connection != null){
 				try {
 
 					statement = connection.prepareStatement(UPDATE_BY_ID);
 					statement.setString(1, entity.getTourname());
 					statement.setString(2, entity.getDetails());
 					statement.setBoolean(3, entity.isHot());
 					statement.setInt(4, entity.getPrice());
 					statement.setInt(5, entity.getRegularDiscount());
 					statement.setInt(6, entity.getType().getId());
 					statement.setInt(7, entity.getId());

 					int affected = statement.executeUpdate();
 					if (affected > 0){
 						return true;
 					} else{
 						throw new DAOLogicalException();
 					}
 
 				} catch (SQLException e) {
 					throw new DAOTechnicalException(e.getMessage());
 
 				} finally {
 					if (null != statement) {
 						try {
 							statement.close();
 						} catch (SQLException e) {
 							logger.error(e.getMessage());
 						}
 					}
 					connectionPool.release(connection);
 				}
 			} else{
 				throw new DAOTechnicalException(NO_CONNECTION_MESSAGE);
 			}
 		} else {
 			throw new DAOLogicalException();
 		}
 	}
 
 	@Override
 	public Tour createEntity(ResultSet set) throws SQLException{
 		Tour tour = new Tour();
 
 		tour.setId(set.getInt("id"));
 		tour.setDetails(set.getString("details"));
 
 		int hot = set.getInt("hot");
 		tour.setHot(hot == 1);
 
 		tour.setPrice(set.getInt("price"));
 		tour.setTourname(set.getString("tourname"));
 		tour.setRegularDiscount(set.getInt("regular_discount"));
 
 		TourType type = TourType.findById(set.getInt("type"));
 		tour.setType(type);
 
 		return tour;
 	}
 
 
 }
