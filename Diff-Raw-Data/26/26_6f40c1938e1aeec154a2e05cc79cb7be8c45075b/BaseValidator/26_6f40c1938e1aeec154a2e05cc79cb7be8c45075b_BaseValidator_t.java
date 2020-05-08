 /**
  * 
  */
 package com.snyder.review.shared.validator;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.snyder.state.list.HasListEvents;
 import com.snyder.state.list.IsEmptyState;
 import com.snyder.state.list.ListObserver;
 import com.snyder.state.list.ObservableForwardingList;
 import com.snyder.state.list.ObservableList;
 import com.snyder.state.nonnull.NonNullState;
 
 /**
  * A base implementation of {@link Validator}
  * 
 * @author SnyderGP
  */
 public class BaseValidator implements Validator
 {
 	
 	private final ObservableList<ValidationError> errors = 
 		new ObservableForwardingList<ValidationError>();
 	private final NonNullState<Boolean> validState = new IsEmptyState(errors);
 	private final List<Validator> children = new ArrayList<Validator>();
 	private final ChildObserver childObserver = new ChildObserver();
 
 	@Override
     public NonNullState<Boolean> isValidState()
     {
 	    return validState;
     }
 
 	@Override
 	public void addObserver(ListObserver<ValidationError> observer)
     {
 	    errors.addObserver(observer);
     }
 
 	@Override
 	public void addObserver(ListObserver<ValidationError> observer, boolean notifyInitial)
     {
 	    errors.addObserver(observer, notifyInitial);
     }
 
 	@Override
 	public void removeObserver(ListObserver<ValidationError> observer)
     {
 	    errors.removeObserver(observer);
     }
 
 	@Override
 	public List<ValidationError> getView()
     {
 	    return errors.getView();
     }
 	
 	protected ObservableList<ValidationError> getErrors()
 	{
 		return errors;
 	}
 
     public void addChildValidator(Validator validator)
     {
 	    children.add(validator);
 	    validator.addObserver(childObserver, true);
     }
 
     public void removeChildValidator(Validator validator)
     {
 	    children.remove(validator);
 	    validator.removeObserver(childObserver);
 	    errors.removeAll(validator.getView());
     }
 
 	/**
 	 * Maintains the error list with all child errors
 	 * 
	 * @author SnyderGP
 	 */
 	private class ChildObserver implements ListObserver<ValidationError>
 	{
 
 		@Override
         public void elementsAdded(HasListEvents<ValidationError> list,
             List<ValidationError> element, int index)
         {
 	        errors.addAll(element);
         }
 
 		@Override
         public void elementsRemoved(HasListEvents<ValidationError> list,
             List<ValidationError> element, int index)
         {
 	        errors.removeAll(element);
         }
 		
 	}
 	
 }
