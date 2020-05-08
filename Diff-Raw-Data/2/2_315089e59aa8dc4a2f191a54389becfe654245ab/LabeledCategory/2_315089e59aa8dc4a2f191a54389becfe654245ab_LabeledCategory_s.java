 package org.jgum.category;
 
 import java.util.Objects;
 
 import com.google.common.base.Function;
 import com.google.common.collect.FluentIterable;
 
 
 /**
  * A category uniquely identified by a label in a given categorization.
  * @author sergioc
  *
  * @param <T> the category label type.
  */
 public abstract class LabeledCategory<T> extends Category {
 	
 	public static <U> FluentIterable<U> labels(FluentIterable<? extends LabeledCategory<?>> path) {
 		return path.transform(new Function<LabeledCategory<?>, U>() {
 			@Override
 			public U apply(LabeledCategory<?> category) {
 				return (U)category.getLabel();
 			}
 		});
 	} 
 	
 	
 	private final T label; //the identifier of this category.
 	
 	/**
 	 * @param categorization the hierarchy where this category exists.
 	 * @param label a label identifying this category.
 	 */
 	public LabeledCategory(Categorization<?> categoryHierarchy, T label) {
 		super(categoryHierarchy);
 		this.label = label;
 	}
 	
 	
 	/**
 	 * 
 	 * @return the label of this category.
 	 */
 	public T getLabel() {
 		return label;
 	}
 	
 	@Override
 	public String toString() {
 		return "["+labelToString()+"]" + super.toString();
 	}
 	
 	protected String labelToString() {
 		return Objects.toString(label);
 	}
 	
 	/**
 	 * 
 	 * @param linearizationFunction is a linearization function.
 	 * @return An iterable of category labels, according to the given linearization function.
 	 */
 	public <U extends Category> FluentIterable<U> linearizeLabels(Function<U,FluentIterable<U>> linearizationFunction) {
 		return LabeledCategory.<U>labels((FluentIterable)linearize((Function)linearizationFunction));
 	}
 
 	/**
 	 * 
 	 * @return An iterable of category labels, according to the default bottom-up linearization function.
 	 */
	public <U extends Category> FluentIterable<U> bottomUpLabelsLabels() {
 		return (FluentIterable<U>)linearizeLabels(getCategorization().getBottomUpLinearizationFunction());
 	}
 	
 	/**
 	 * 
 	 * @return An iterable of category labels, according to the default top-down linearization function.
 	 */
 	public <U extends Category> FluentIterable<U> topDownLabels() {
 		return (FluentIterable<U>)linearizeLabels(getCategorization().getTopDownLinearizationFunction());
 	}
 
 }
