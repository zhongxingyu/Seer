 package asrsController;
 
<<<<<<< HEAD
import order.Product;
 /**
  * Interface die gebruikt wordt voor de simulaties en de Arduino 
  * 
  * @author Mike
  *
  */
=======
import productInfo.Product;

>>>>>>> 556b3cb96fe5164af1be4f4c8ca0308398ae6c51
 public interface BinPacking {
 	/**
 	 * Pakt het product in.
 	 * 
 	 * @param binNumber
 	 * @param product
 	 */
 	public abstract void packProduct(Byte binNumber, Product product);
 }
