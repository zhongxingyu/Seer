 package asrsController;
 
import productInfo.Product;

 /**
  * Interface die gebruikt wordt voor de simulaties en de Arduino 
  * 
  * @author Mike
  *
  */
 public interface BinPacking {
 	/**
 	 * Pakt het product in.
 	 * 
 	 * @param binNumber
 	 * @param product
 	 */
 	public abstract void packProduct(Byte binNumber, Product product);
 }
