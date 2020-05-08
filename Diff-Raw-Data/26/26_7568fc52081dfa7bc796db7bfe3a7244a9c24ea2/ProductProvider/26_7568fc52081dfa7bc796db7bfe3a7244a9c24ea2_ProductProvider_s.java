 package excel;
 
import com.flashshop.Product;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Evgen
  * Date: 17.03.13
  * Time: 18:30
  * To change this template use File | Settings | File Templates.
  */
 public interface ProductProvider {
     public boolean hasNextProduct();
     public Product nextProduct();
 }
