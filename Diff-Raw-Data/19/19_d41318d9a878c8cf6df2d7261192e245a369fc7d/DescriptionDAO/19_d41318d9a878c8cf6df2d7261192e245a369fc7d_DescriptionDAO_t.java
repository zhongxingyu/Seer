 package com.epam.lab.buyit.controller.dao.description;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import com.epam.lab.buyit.controller.dao.utils.DAOUtils;
 import com.epam.lab.buyit.controller.dao.utils.connection.ConnectionManager;
 import com.epam.lab.buyit.controller.dao.utils.transformers.DescriptionTransformer;
 import com.epam.lab.buyit.model.Description;
 
 public class DescriptionDAO implements DescriptionDAOInterface {
 	private static final Logger LOGGER = Logger.getLogger(DescriptionDAO.class);
 	private final static String GET_BY_PRODUCT_ID = "SELECT * FROM descriptions WHERE products_id = ?";
 	private final static String GET_ALL_DESCRIPTIONS = "SELECT * FROM descriptions";
 	private DescriptionTransformer transformer;
 
 	public DescriptionDAO() {
 		transformer = new DescriptionTransformer();
 	}
 
 	@Override
 	public int createElement(Description elem) {
 		Connection connection = ConnectionManager.getConnection();
 		PreparedStatement statement = null;
 		ResultSet generatedKeys = null;
 		try {
 			statement = transformer.fromObjectToCreatePS(elem, connection);
 			if (statement != null) {
 				statement.executeUpdate();
 				generatedKeys = statement.getGeneratedKeys();
 				generatedKeys.next();
 				return generatedKeys.getInt(1);
 			}
 		} catch (SQLException e) {
 			LOGGER.error(e);
 		} finally {
 			DAOUtils.close(generatedKeys, statement, connection);
 		}
 		return 0;
 	}
 
 	@Override
 	public Description getElementById(int id) {
		Description description = null;
		Connection connection = ConnectionManager.getConnection();
		PreparedStatement statement = null;
		ResultSet result = null;
		try {
			statement = connection.prepareStatement(GET_BY_PRODUCT_ID);
			statement.setInt(1, id);
			result = statement.executeQuery();
			if (result.next()) {
				description = transformer.fromRSToObject(result);
				return description;
			}
		} catch (SQLException e) {
			LOGGER.error(e);
		} finally {
			DAOUtils.close(result, statement, connection);
		}
		return description;
 	}
 
 	@Override
 	public void updateElement(Description elem) {
 		Connection connection = ConnectionManager.getConnection();
 		PreparedStatement statement = null;
 		try {
 			statement = transformer.fromObjectToUpdatePS(elem, connection);
 			if (statement != null) {
 				statement.executeUpdate();
 			}
 		} catch (SQLException e) {
 			LOGGER.error(e);
 		} finally {
 			DAOUtils.close(statement, connection);
 		}
 	}
 
 	@Override
 	public void deleteElementById(int id) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public List<Description> getAllDescriptions() {
 		List<Description> descriptions = new ArrayList<Description>();
 		Connection connection = ConnectionManager.getConnection();
 		PreparedStatement statement = null;
 		ResultSet result = null;
 		try {
 			statement = connection.prepareStatement(GET_ALL_DESCRIPTIONS);
 			result = statement.executeQuery();
 			while (result.next()) {
 				Description currentDescription = transformer
 						.fromRSToObject(result);
 				descriptions.add(currentDescription);
 			}
 		} catch (SQLException e) {
 			LOGGER.error(e);
 		} finally {
 			DAOUtils.close(result, statement, connection);
 		}
 		return descriptions;
 	}
 
 	@Override
 	public Description getDescriptionByProductId(int productId) {
 		Description description = null;
 		Connection connection = ConnectionManager.getConnection();
 		PreparedStatement statement = null;
 		ResultSet result = null;
 		try {
 			statement = connection.prepareStatement(GET_BY_PRODUCT_ID);
 			statement.setInt(1, productId);
 			result = statement.executeQuery();
 			if (result.next()) {
 				description = transformer.fromRSToObject(result);
 				return description;
 			}
 		} catch (SQLException e) {
 			LOGGER.error(e);
 		} finally {
 			DAOUtils.close(result, statement, connection);
 		}
 		return description;
 	}
 
 }
