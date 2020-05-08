 package model;
 
 import javax.xml.bind.annotation.XmlAnyElement;
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import com.google.common.base.Objects;
 
 @XmlRootElement(name = "cardProbability")
 public class Probability<T> implements Comparable<Probability<T>> {
 	private double cardProbability;
 	private T wrappedObject;
 
 	// For jackson json parsing
 	@SuppressWarnings("unused")
 	private Probability() {
 		cardProbability = 0.0;
 		wrappedObject = null;
 	}
 
 	public Probability(T object, double probability) {
 		this.wrappedObject = object;
 		this.cardProbability = probability;
 	}
 
 	@Override
 	public int compareTo(Probability<T> other) {
 		return Double.valueOf(cardProbability).compareTo(Double.valueOf(other.getCardProbability()));
 	}
 
 	@Override
 	public boolean equals(Object obj) {
		if (!this.getClass().equals(obj.getClass())) {
 			final Probability<?> other = (Probability<?>) obj;
 			return Objects.equal(other.cardProbability, this.cardProbability) && Objects.equal(wrappedObject, other.wrappedObject);
 		}
 		return false;
 	}
 
 	@XmlAttribute(name = "chance")
 	public double getCardProbability() {
 		return cardProbability;
 	}
 
 	@XmlAnyElement()
 	public T getWrappedObject() {
 		return wrappedObject;
 	}
 
 	@Override
 	public int hashCode() {
 		return Objects.hashCode(cardProbability, wrappedObject);
 	}
 
 	// For jackson json parsing
 	@SuppressWarnings("unused")
 	private void setCardProbability(double probability) {
 		this.cardProbability = probability;
 	}
 
 	// For jackson json parsing
 	@SuppressWarnings("unused")
 	private void setWrappedObject(T wrappedObject) {
 		this.wrappedObject = wrappedObject;
 	}
 }
