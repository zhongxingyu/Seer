 package kz.enu.epam.azimkhan.tour.dao;
 
 import kz.enu.epam.azimkhan.tour.connection.ConnectionPool;
 import kz.enu.epam.azimkhan.tour.entity.Order;
 import kz.enu.epam.azimkhan.tour.entity.Tour;
 import kz.enu.epam.azimkhan.tour.entity.User;
 import kz.enu.epam.azimkhan.tour.exception.ConnectionPoolException;
 import kz.enu.epam.azimkhan.tour.exception.DAOLogicalException;
 import kz.enu.epam.azimkhan.tour.exception.DAOTechnicalException;
 import org.apache.log4j.Logger;
 
 import java.sql.*;
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * order dao
  */
 public class OrderDAO extends AbstractDAO<Integer, Order>{
 
     private static final String SELECT_USER_ORDERS = "SELECT order.id, order.paid, user.username, tour.tourname, order.amount, order.purchase_date FROM `tour_purchase` `order` JOIN `tour` ON order.tour_id = tour.id JOIN user ON user.id = order.client_id WHERE order.client_id = ?";
     private static final String EMPTY_USER = "EMPTY USER";
     private static final String UPDATE_ORDER = "UPDATE tour_purchase SET paid=? WHERE id =?";
 
     private final String CREATE_ORDER = "INSERT INTO tour_purchase (tour_id, client_id, amount, purchase_date) VALUES (?, ?, ?, ?)";
     private final String SELECT_ORDERS = "SELECT order.id, order.paid, user.username, tour.tourname, order.amount, order.purchase_date FROM `tour_purchase` `order` JOIN `tour` ON order.tour_id = tour.id JOIN user ON user.id = order.client_id";
 
     private Logger logger = Logger.getRootLogger();
 
     private OrderDAO(){}
 
     private static OrderDAO instance;
 
     public static OrderDAO getInstance(){
         if (instance == null){
             instance = new OrderDAO();
         }
         return instance;
     }
 
 
     /**
      * find all orders
      * @return
      * @throws DAOLogicalException
      * @throws DAOTechnicalException
      */
     @Override
     public List<Order> findAll() throws DAOLogicalException, DAOTechnicalException {
         ConnectionPool connectionPool = null;
         try{
             connectionPool = ConnectionPool.getInstance();
         } catch (ConnectionPoolException e){
             throw new DAOTechnicalException(e);
         }
 
         Connection connection = connectionPool.getConnection();
         PreparedStatement statement = null;
 
         LinkedList<Order> orders = new LinkedList<Order>();
 
         if (connection != null){
             try {
                 statement = connection.prepareStatement(SELECT_ORDERS);
 
 
                 ResultSet set = statement.executeQuery();
 
                 while(set.next()){
 
                     Order order = createOrder(set);
 
                     orders.add(order);
                 }
 
                 return orders;
             } catch (SQLException e) {
                 throw new DAOTechnicalException(e);
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
             throw new DAOTechnicalException(NO_CONNECTION);
         }
     }
 
     /**
      * find orders for user
      * @param user
      * @return
      * @throws DAOLogicalException
      * @throws DAOTechnicalException
      */
     public List<Order> findOrdersForUser(User user) throws DAOLogicalException, DAOTechnicalException{
         if (user != null){
             ConnectionPool connectionPool = null;
             try{
                 connectionPool = ConnectionPool.getInstance();
             } catch (ConnectionPoolException e){
                 throw new DAOTechnicalException(e);
             }
 
             Connection connection = connectionPool.getConnection();
             PreparedStatement statement = null;
 
             LinkedList<Order> orders = new LinkedList<Order>();
 
             if (connection != null){
                 try {
                     statement = connection.prepareStatement(SELECT_USER_ORDERS);
                     statement.setInt(1, user.getId());
 
                     ResultSet set = statement.executeQuery();
 
                     while(set.next()){
 
                         Order order = createOrder(set);
 
                         orders.add(order);
                     }
 
                     return orders;
                 } catch (SQLException e) {
                     throw new DAOTechnicalException(e);
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
                 throw new DAOTechnicalException(NO_CONNECTION);
             }
         } else {
             throw new DAOLogicalException(EMPTY_USER);
         }
 
     }
 
     /**
      * create order
      * @param set
      * @return
      * @throws SQLException
      */
     private Order createOrder(ResultSet set) throws SQLException{
         Order order = new Order();
         order.setId(set.getInt("id"));
         order.setAmount(set.getDouble("amount"));
         order.setDateTime(set.getTimestamp("purchase_date"));
 
         Tour tour = new Tour();
         tour.setTourname(set.getString("tourname"));
         order.setTour(tour);
 
         order.setPaid(set.getBoolean("paid"));
         User user = new User();
         user.setUsername(set.getString("username"));
         order.setUser(user);
 
         return order;
     }
 
     /**
      * create order
      * @param entity
      * @return
      * @throws DAOLogicalException
      * @throws DAOTechnicalException
      */
     @Override
     public boolean create(Order entity) throws DAOLogicalException, DAOTechnicalException {
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
                     statement = connection.prepareStatement(CREATE_ORDER);
                     statement.setInt(1, entity.getTour().getId());
                     statement.setInt(2, entity.getUser().getId());
                     statement.setDouble(3, entity.getAmount());
                    statement.setDate(4, new Date(entity.getDateTime().getTime()));
 
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
                 throw new DAOTechnicalException(NO_CONNECTION);
             }
         }
 
         return false;
     }
 
     public void updatePaid(Integer id, boolean paid) throws  DAOLogicalException, DAOTechnicalException{
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
                 statement = connection.prepareStatement(UPDATE_ORDER);
 
                 statement.setBoolean(1, paid);
                 statement.setInt(2, id);
 
                 int affected = statement.executeUpdate();
 
                 if (affected < 0){
                     throw new DAOLogicalException(NO_ROWS_AFFECTED);
                 } else{
                     return;
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
             throw new DAOTechnicalException(NO_CONNECTION);
         }
     }
 
     /**
      * update order
      * @param entity
      * @return
      * @throws DAOLogicalException
      * @throws DAOTechnicalException
      */
     @Override
     public boolean update(Order entity) throws DAOLogicalException, DAOTechnicalException {
         return false;
     }
 
     /**
      * find order by id
      * @param id
      * @return
      * @throws DAOLogicalException
      * @throws DAOTechnicalException
      */
     @Override
     public Order findById(Integer id) throws DAOLogicalException, DAOTechnicalException {
         return null;
     }
 
     @Override
     public boolean delete(Integer id) throws DAOLogicalException, DAOTechnicalException {
         return false;
     }
 
     @Override
     public boolean delete(Order entity) throws DAOLogicalException, DAOTechnicalException {
         return false;
     }
 
     /**
      * create entity
      * @param set
      * @return
      * @throws SQLException
      */
     @Override
     public Order createEntity(ResultSet set) throws SQLException {
         Order order = new Order();
         order.setId(set.getInt("id"));
         order.setAmount(set.getDouble("amount"));
         order.setDateTime(set.getTimestamp("purchase_date"));
         order.setPaid(set.getBoolean("paid"));
 
         return order;
     }
 }
