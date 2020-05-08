 /**
  * 
  */
 package resources.math;
 
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 
 /**
  * @author Jedabero
  * @since 0.4
  */
 public class Interval {
 	
 	private BigDecimal max;
 	private BigDecimal min;
 	
 	/**
 	 * Crea un intervalo cerrado de longitud finita
 	 * @param min el mnimo valor del intervalo
 	 * @param max el mximo valor del intervalo
 	 */
 	public Interval(BigDecimal min, BigDecimal max){
 		this.max = max;
 		this.min = min;
 	}
 	
 	/**
 	 * 
 	 * @return distancia entre el mximo y el mnimo
 	 */
 	public BigDecimal length(){
 		return max.subtract(min);
 	}
 	
 	/**
 	 * @return el valor del centro de este intervalo
 	 */
 	public BigDecimal centre(){
		return max.add(min).divide(BigDecimal.valueOf(2), 20, RoundingMode.HALF_UP);
 	}
 	
 	/**
 	 * @return el mnimo valor del intervalo
 	 */
 	public BigDecimal min(){
 		return min;
 	}
 	/**
 	 * @param m
 	 */
 	public void setMin(BigDecimal m){
 		this.min = m;
 	}
 	
 	/**
 	 * @return el mximo valor del intervalo
 	 */
 	public BigDecimal max(){
 		return max;
 	}
 	/**
 	 * @param m
 	 */
 	public void setMax(BigDecimal m){
 		this.max = m;
 	}
 	
 	public String toString(){
 		return "("+min+", "+max+")";
 	}
 	
 }
