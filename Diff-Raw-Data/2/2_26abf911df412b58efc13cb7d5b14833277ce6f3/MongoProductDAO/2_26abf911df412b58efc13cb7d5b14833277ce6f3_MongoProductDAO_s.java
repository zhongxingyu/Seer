 package main.de.nordakademie.nakp.persistence;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 
 import main.de.nordakademie.nakp.business.Product;
 import main.de.nordakademie.nakp.business.ProductDAO;
 
 @Repository
 public class MongoProductDAO implements ProductDAO {
 
 	@Autowired
 	private MongodbFactory mongodb;
 
 	@Override
 	public List<Product> findAll() {
 		final List<Product> products = new ArrayList<>();
 		final DBCursor cursor = mongodb.getObject().getCollection("product")
 				.find();
 		while (cursor.hasNext()) {
 			final DBObject document = cursor.next();
			products.add(new Product((String) document.get("id")));
 		}
 		return products;
 	}
 
 }
