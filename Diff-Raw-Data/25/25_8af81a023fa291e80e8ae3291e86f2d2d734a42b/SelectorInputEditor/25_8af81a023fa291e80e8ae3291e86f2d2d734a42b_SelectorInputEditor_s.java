 package topshelf.gwt.editor.client;
 
 
 import topshelf.gwt.common.client.AbstractRFOracle;
 import topshelf.gwt.common.client.AbstractAsyncSelector;
 
 import com.google.gwt.editor.client.Editor;
 import com.google.gwt.editor.client.EditorDelegate;
 import com.google.gwt.editor.client.ValueAwareEditor;
 
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
 	implements ValueAwareEditor<T> {
 	
 	AbstractAsyncSelector<T> selector;
 	
 	public SelectorInputEditor(final AbstractAsyncSelector<T> selector) {
 		super(selector);
 //		super(selector, selector);
 		this.selector = selector;
 	}
 
 	/**
 	 * Override InputEditorBase's impl where it casts input to ValueBoxBase
 	 * and instead delegate the readOnly toggle to the SuggestBox's
 	 * TextBox widget.
 	 */
 	@Override
 	public void setInputLock(boolean lock) {
 		selector.getSuggestBox().getTextBox().setReadOnly(lock);
 	}
 	
 	/**
 	 * override and return a newly created value to queue up
 	 * the persist when the whole editor gets persisted
 	 * @return newly created value object
 	 */
 	protected T createNewValueOnFlush() {
 		return null;
 	}
 	
 	@Override
 	public void flush() {
 		if (null == super.getValue()) {
 			setValue(createNewValueOnFlush());
 		}
 	}
 
 	@Override
 	public void setDelegate(EditorDelegate<T> delegate) {}
 
 	@Override
 	public void onPropertyChange(String... paths) {}
 }
