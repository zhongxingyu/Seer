 package org.dawnsci.jexl.internal;
 
 import org.apache.commons.jexl2.JexlArithmetic;
 
 import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
 import uk.ac.diamond.scisoft.analysis.dataset.Maths;
 
 /**
  * DatasetArithmetic class overriding the plus, minus, multiplication and division operators
  * of the JexlArithmetic object to work with Abstract datasets.
  */
 public class DatasetArithmetic extends JexlArithmetic {
 
 	public DatasetArithmetic(boolean lenient) {
 		super(lenient);
 	}
 	
 	/**
 	 * This function adds two objects, including AbstractDatasets
 	 * 
 	 * @param lhs Object on the left hand side of the equation
 	 * @param rhs Object on the right hand side of the equation
 	 * @return Object containing result (either Abstract dataset or whatever super.add returns)
 	 */
 	@Override
 	public Object add(Object lhs, Object rhs) {
 		
 		if (rhs instanceof AbstractDataset &&
 			lhs instanceof AbstractDataset) {
 			
 			return Maths.add((AbstractDataset)lhs, (AbstractDataset)rhs);
 		}
 		
 		if (lhs instanceof AbstractDataset &&
 				!(rhs instanceof AbstractDataset)) {
 			
 			return Maths.add((AbstractDataset)lhs, rhs);
 		}
 		
 		if (!(lhs instanceof AbstractDataset) &&
 				rhs instanceof AbstractDataset) {
 			
 			return Maths.add((AbstractDataset)rhs, lhs);
 		}
 		return super.add(lhs, rhs);
 	}
 	
 	/**
 	 * This function subtracts two objects, including AbstractDatasets
 	 * 
 	 * @param lhs Object on the left hand side of the equation
 	 * @param rhs Object on the right hand side of the equation
 	 * @return Object containing result (either Abstract dataset or whatever super.subtract returns)
 	 */
 	@Override
 	public Object subtract(Object lhs, Object rhs) {
 		
 		if (rhs instanceof AbstractDataset ||
 				lhs instanceof AbstractDataset) {
 				return Maths.subtract(lhs, rhs);
 			}
 		return super.subtract(lhs, rhs);
 	}
 	
 	/**
 	 * This function multiplies two objects, including AbstractDatasets
 	 * 
 	 * @param lhs Object on the left hand side of the equation
 	 * @param rhs Object on the right hand side of the equation
 	 * @return Object containing result (either Abstract dataset or whatever super.multipy returns
 	 */
 	@Override
 	public Object multiply(Object lhs, Object rhs) {
 
 		if (rhs instanceof AbstractDataset &&
 				lhs instanceof AbstractDataset) {
 			return Maths.multiply((AbstractDataset)lhs, (AbstractDataset)rhs);
 		}
 
 		if (lhs instanceof AbstractDataset &&
 				!(rhs instanceof AbstractDataset)) {
 			return Maths.multiply((AbstractDataset)lhs, rhs);
 		}
 
 		if (!(lhs instanceof AbstractDataset) &&
 				rhs instanceof AbstractDataset) {
 			return Maths.multiply((AbstractDataset)rhs, lhs);
 		}
 		return super.multiply(lhs, rhs);
 	}
 	
 	/**
 	 * This function divides two objects, including AbstractDatasets
 	 * 
 	 * @param lhs Object on the left hand side of the equation
 	 * @param rhs Object on the right hand side of the equation
 	 * @return Object containing result (either Abstract dataset or whatever super.divide returns)
 	 */
 	@Override
 	public Object divide(Object lhs, Object rhs) {
 		
 		if (rhs instanceof AbstractDataset ||
 				lhs instanceof AbstractDataset) {
 				
 				return Maths.divide(lhs, rhs);
 			}
 		return super.divide(lhs, rhs);
 	}
 	
 	/**
 	 * This function returns the negative of the supplied Object, including AbstractDatasets
 	 * 
 	 * @param ob Object to the right of negative sign
 	 * @return Object containing result (either Abstract dataset or whatever super.negate returns)
 	 */
 	@Override
 	public Object negate(Object ob) {
 		
 		if (ob instanceof AbstractDataset) {
 
 			return Maths.multiply((AbstractDataset)ob,-1);
 		}
 		return super.negate(ob);
 	}
 
 }
