 package dao;
 
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.util.ArrayList;
 import java.util.List;
 
 import model.Category;
 import util.DbUtil;
 
 public class CategoryDao {
 	private Connection connection;
 
 	public CategoryDao() {
 		connection = DbUtil.getConnection();
 	}
 
 	/**
 	 * @param category
 	 * @return 0 if there was an error, 1 if the insertion was correct
 	 */
 	public int addCategory(Category category) {
 		int ret = 0;
 		try {
 			PreparedStatement preparedStatement = connection
 					.prepareStatement("INSERT INTO categories (name, description) VALUES (?, ?)");
 
 			preparedStatement.setString(1, category.getName());
 			preparedStatement.setString(2, category.getDescription());
 			ret = preparedStatement.executeUpdate();
 			// Close the Statement
 			preparedStatement.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return ret;
 	}
 
 	/**
 	 * @param category
 	 * @return 0 if there was an error, 1 if the update was correct
 	 */
 	public int updateCategory(Category category) {
 		int ret = 0;
 		try {
 			PreparedStatement preparedStatement = connection
 					.prepareStatement("UPDATE categories SET name=?, description=?"
 							+ "WHERE id=?");
 
 			preparedStatement.setString(1, category.getName());
 			preparedStatement.setString(2, category.getDescription());
			preparedStatement.setInt(3, category.getId());
 			ret = preparedStatement.executeUpdate();
 			// Close the Statement
 			preparedStatement.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return ret;
 	}
 
 	/**
 	 * @param void
 	 * @return List<Category> that contains all the categories in the DB
 	 */
 	public List<Category> getAllCategories() {
 		List<Category> categories = new ArrayList<Category>();
 		try {
 			Statement statement = connection.createStatement();
 			ResultSet rs = statement.executeQuery("SELECT * FROM categories");
 			while (rs.next()) {
 				Category category = new Category();
 				category.setId(rs.getInt("id"));
 				category.setName(rs.getString("name"));
 				category.setDescription(rs.getString("description"));
 				categories.add(category);
 			}
 			rs.close();
 			// Close the Statement
 			statement.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		return categories;
 	}
 
 	/**
 	 * @param id
 	 *            of the category to delete
 	 * @return if there was an error, 1 if the deletion was correct
 	 */
 	public int deleteCategory(int categoryId) {
 		int ret = 0;
 		try {
 			PreparedStatement preparedStatement = connection
 					.prepareStatement("DELETE FROM categories WHERE id=?");
 			// Parameters start with 1
 			preparedStatement.setInt(1, categoryId);
 			ret = preparedStatement.executeUpdate();
 			// Close the Statement
 			preparedStatement.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return ret;
 	}
 }
