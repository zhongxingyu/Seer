 package topshelf.gwt.editor.client;
 
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import topshelf.gwt.common.client.AbstractAsyncOracle;
 import topshelf.gwt.common.client.AbstractAsyncSelector;
 import topshelf.gwt.common.client.AbstractRFOracle;
 
 import com.google.gwt.editor.client.Editor;
 import com.google.gwt.editor.client.EditorDelegate;
 import com.google.gwt.editor.client.ValueAwareEditor;
 import com.google.gwt.event.dom.client.KeyCodes;
 import com.google.gwt.event.dom.client.KeyUpEvent;
 import com.google.gwt.event.dom.client.KeyUpHandler;
 import com.google.gwt.event.logical.shared.HasSelectionHandlers;
 import com.google.gwt.event.logical.shared.SelectionEvent;
 import com.google.gwt.event.logical.shared.SelectionHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.Command;
 import com.google.gwt.user.client.TakesValue;
 import com.google.gwt.user.client.ui.SuggestOracle.Request;
 import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
 import com.google.gwt.view.client.ProvidesKey;
 
 /**
  * This {@link Editor} is backed by an {@link AbstractAsyncSelector}.  The {@link AbstractRFOracle}
  * configured within the selector can be used to retrieve proxy objects from the server and
  * set within the selector.
  * 
  * @author bloo
  *
  * @param <T>
  */
 public class SelectorInputEditor<T> extends InputEditorBase<T>
 	implements ValueAwareEditor<T>, HasSelectionHandlers<Suggestion> {
 	
 	AbstractAsyncSelector<T> selector;
 	T previousValue = null;
 	ProvidesKey<T> keyProvider = null;
 	
 	public SelectorInputEditor(final AbstractAsyncSelector<T> selector) {
 		this(selector, null, null);
 	}
 	
 	public SelectorInputEditor(final AbstractAsyncSelector<T> selector, ProvidesKey<T> keyProvider) {
 		this(selector, null, keyProvider);
 	}
 
 	public SelectorInputEditor(final AbstractAsyncSelector<T> selector, final Command onEnterHandler) {
 		this(selector, onEnterHandler, null);
 	}
 	
 	public SelectorInputEditor(final AbstractAsyncSelector<T> selector, final Command onEnterHandler, ProvidesKey<T> keyProvider) {
 		super(selector);
 		this.keyProvider = keyProvider;
 		addStyleName("ts-SelectorInputEditor");
 		this.selector = selector;
 		if (null != onEnterHandler) {
 			selector.getSuggestBox().getValueBox().addKeyUpHandler(new KeyUpHandler() {
 				@Override
 				public void onKeyUp(KeyUpEvent event) {
 					event.stopPropagation();
 					if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
 						onEnterHandler.execute();
 					}
 				}
 			});
 		}
 	}
 
 	/**
 	 * Override InputEditorBase's impl where it casts input to ValueBoxBase
 	 * and instead delegate the readOnly toggle to the SuggestBox's
 	 * TextBox widget.
 	 */
 	@Override
 	public void onSetInputLock(boolean lock) {
 		selector.getSuggestBox().getValueBox().setReadOnly(lock);
 	}
 
 	@Override
 	public void flush() {}
 
 //	public String getCurrentQuery() {
 //		@SuppressWarnings("unchecked")
 //		AbstractAsyncSelector<T> sel = (AbstractAsyncSelector<T>)getInput();
 //		AbstractAsyncOracle<T> oracle = sel.getOracle();
 //		Request req = null == oracle ? null : oracle.getCurrentOracleRequest();
 //		String query = null == req ? null : req.getQuery();
 //		return query;		
 //	}
 	
 	public String getCurrentInput() {
 		return selector.getSuggestBox().getValue();
 	}
 
 	public void clearQuery() {
 		@SuppressWarnings("unchecked")
 		AbstractAsyncSelector<T> sel = (AbstractAsyncSelector<T>)getInput();
 		AbstractAsyncOracle<T> oracle = sel.getOracle();
 		Request req = null == oracle ? null : oracle.getCurrentOracleRequest();
 		if (null != req) req.setQuery(null);
 	}
 	
 	@Override
 	public void setValue(T value) {
 		previousValue = getValue();
 		if (null == value)
 			clearQuery();
 		super.setValue(value);
 	}
 	
 	@Override
 	public void setValue(T value, boolean fireEvents) {
 		previousValue = getValue();
 		if (null == value)
 			clearQuery();
 		super.setValue(value, fireEvents);
 	}
 	
 	/**
 	 * We have an invalid input if we have a query string inside but
 	 * it didn't resolve to an actual value.
 	 * 
 	 * @return
 	 */
 	public boolean hasInvalidInput() {
 		String input = getCurrentInput();
		return (null == getValue() && null != input && input.matches("[\\S]+"));
 	}
 	
 	@Override
 	public void setDelegate(EditorDelegate<T> delegate) {}
 
 	@Override
 	public void onPropertyChange(String... paths) {}
 
 	@Override
 	public HandlerRegistration addSelectionHandler(
 			SelectionHandler<Suggestion> handler) {
 		return selector.addSelectionHandler(handler);
 	}
 	
 	public T getPreviousValue() {
 		return previousValue;
 	}
 	
 	public T getSuggestedValue(Suggestion suggestion) {
 		return selector.getSuggestedValue(suggestion);
 	}
 	
 	public boolean isSuggestingNewValue(Suggestion suggestion) {
 		T orig = getPreviousValue();
 		T suggested = getSuggestedValue(suggestion);
 		Object origKey = null == keyProvider ? orig : keyProvider.getKey(orig);
 		Object suggestedKey = null == keyProvider ? suggested : keyProvider.getKey(suggested);
 		return ((null == origKey && null != suggestedKey) ||
 				(null != origKey && null == suggestedKey) ||
 				(null != origKey && null != suggestedKey && origKey != suggestedKey));
 	}
 	
 	List<TakesValue<?>> dependentEditors = new ArrayList<TakesValue<?>>();
 	
 	/**
 	 * add downstream dependent selectors that should clear their values if this selector's
 	 * value should change to a new one
 	 * 
 	 * @param editors
 	 */
 	public void addDependentEditors(TakesValue<?>... editors) {
 		if (dependentEditors.size() == 0 && editors.length > 0) {
 			this.addSelectionHandler(new SelectionHandler<Suggestion>() {
 				@Override
 				public void onSelection(SelectionEvent<Suggestion> event) {
 					if (isSuggestingNewValue(event.getSelectedItem())) {
 						for (TakesValue<?> tv : dependentEditors) {
 							tv.setValue(null);
 						}
 					}
 				}
 			});
 		}
 		dependentEditors.addAll(Arrays.asList(editors));
 	}
 }
