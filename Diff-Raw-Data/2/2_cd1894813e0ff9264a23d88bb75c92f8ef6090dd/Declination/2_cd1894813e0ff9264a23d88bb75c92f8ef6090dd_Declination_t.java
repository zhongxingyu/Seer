 package de.planetes.catalogue.ri;
 
 import org.springframework.beans.factory.annotation.Configurable;
 
 import de.planetes.catalogue.IDeclination;
 
 /**
  * Default implementation of the {@link IDeclination} interface. <br>
  * To create a new instance of this class use the provided factory:
  * 
  * <pre>
  * <code>
  * 		IDeclination declination = Declination.createDeclination(23, 22, 57);
  * 	</code>
  * </pre>
  * 
  * @author Thorsten Kamann
  */
 @Configurable
 public class Declination extends AbstractValidatedCatalogue implements
 		IDeclination {
 	private int degree;
 	private int minutes;
 	private double seconds;
 
 	/**
 	 * Protected constructor. Please use {@link CatalogueFactory} to create a
 	 * new instance.
 	 * 
 	 * @param degree
 	 * @param minutes
 	 * @param seconds
 	 */
 	protected Declination(int degree, int minutes, double seconds) {
 		this.degree = degree;
 		this.minutes = minutes;
 		this.seconds = seconds;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.planetes.catalogue.IDeclination#getDegree()
 	 */
 	public int getDegree() {
 		return degree;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.planetes.catalogue.IDeclination#getMinutes()
 	 */
 	public int getMinutes() {
 		return minutes;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see de.planetes.catalogue.IDeclination#getSeconds()
 	 */
 	public double getSeconds() {
 		return seconds;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
		return degree + "° " + minutes + "' " + seconds + "\"";
 	}
 }
