 package com.zipsoft.suggestfield;
 
 import java.io.Serializable;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.zipsoft.suggestfield.client.SuggestFieldClientRpc;
 import com.zipsoft.suggestfield.client.SuggestFieldServerRpc;
 import com.zipsoft.suggestfield.client.SuggestFieldState;
 import com.zipsoft.suggestfield.shared.SuggestFieldSuggestion;
 import com.zipsoft.suggestfield.shared.SuggestFieldSuggestionImpl;
 import com.vaadin.event.FieldEvents.BlurEvent;
 import com.vaadin.event.FieldEvents.BlurListener;
 import com.vaadin.event.FieldEvents.BlurNotifier;
 import com.vaadin.event.FieldEvents.FocusAndBlurServerRpcImpl;
 import com.vaadin.event.FieldEvents.FocusEvent;
 import com.vaadin.event.FieldEvents.FocusListener;
 import com.vaadin.event.FieldEvents.FocusNotifier;
 import com.vaadin.ui.AbstractComponent;
 import com.vaadin.ui.Component;
 import com.vaadin.ui.Component.Focusable;
 
 /**
  * SuggestField - Vaadin implementation of GWT SuggestBox
  * 
  * @author marko
  * 
  */
 public class SuggestField extends AbstractComponent implements Focusable,
 		BlurNotifier, FocusNotifier {
 
 	private static final long serialVersionUID = -7457475157965163036L;
 
 	public interface SuggestionLoader extends Serializable {
 		public List<SuggestFieldSuggestionImpl> loadSuggestions(String query, int limit);
 		public SuggestFieldSuggestion convertToServerSideValue(SuggestFieldSuggestionImpl suggestion);
 		public SuggestFieldSuggestionImpl convertToClientSideValue(SuggestFieldSuggestion model);
 	}
 
 	private SuggestionLoader suggestionLoader;
 
 	private SuggestFieldSuggestion value;
 
 	/*
 	 * Slanje rezultata na klijentsku stranu
 	 */
 	private SuggestFieldServerRpc rpc = new SuggestFieldServerRpc() {
 
 		private static final long serialVersionUID = 6022726844231207600L;
 
 		@Override
 		public void searchSuggestionsOnServer(String query, int limit) {
 			if (suggestionLoader == null) {
 				getRpcProxy(SuggestFieldClientRpc.class).sendSuggestions(query,
 						new ArrayList<SuggestFieldSuggestionImpl>());
 			} else {
 				getRpcProxy(SuggestFieldClientRpc.class).sendSuggestions(
 						query,
 						new ArrayList<SuggestFieldSuggestionImpl>(suggestionLoader
 								.loadSuggestions(query, limit)));
 			}
 
 		}
 
 		@Override
 		public void valueChanged(SuggestFieldSuggestionImpl value) {
 //			setValueInternal(suggestionLoader.convertToModel(value));
 //			fireSuggestionSelected();
 //			markAsDirty();
 			if (suggestionLoader != null) {
 				setValue(suggestionLoader.convertToServerSideValue(value), true);
 			}
 		}
 	};
 
 	FocusAndBlurServerRpcImpl focusBlurRpc = new FocusAndBlurServerRpcImpl(this) {
 
 		private static final long serialVersionUID = -780524775769549747L;
 
 		@Override
 		protected void fireEvent(Event event) {
 			SuggestField.this.fireEvent(event);
 		}
 	};
 
 	public SuggestField() {
 		setValue(null);
 		registerRpc(rpc);
 		registerRpc(focusBlurRpc);
 	}
 
 	// We must override getState() to cast the state to SuggestFieldState
 	@Override
 	public SuggestFieldState getState() {
 		return (SuggestFieldState) super.getState();
 	}
 
 	/**
 	 * Gets the current input prompt.
 	 * 
 	 * @see #setInputPrompt(String)
 	 * @return the current input prompt, or null if not enabled
 	 */
 	public String getInputPrompt() {
 		return getState().inputPrompt;
 	}
 
 	/**
 	 * Sets the input prompt - a textual prompt that is displayed when the field
 	 * would otherwise be empty, to prompt the user for input.
 	 * 
	 * @param inputPrompt
 	 */
 	public void setInputPrompt(String inputPrompt) {
 		getState().inputPrompt = inputPrompt;
 		markAsDirty();
 	}
 
 	protected void setValueInternal(SuggestFieldSuggestion value) {
 		this.value = value;
 	}
 
 	public void setValue(SuggestFieldSuggestion newValue) {
 		setValue(newValue, false);		
 	}
 	
 	public void setValue(SuggestFieldSuggestion newValue, boolean fireEvent) {
 		setValueInternal(newValue);
 		if (suggestionLoader != null) {
 			getState().suggestion = suggestionLoader.convertToClientSideValue(this.value);
 		} else {
 			getState().suggestion = null;
 		}
 		markAsDirty();
 		if (fireEvent) {
 			fireSuggestionSelected();
 		}
 	}
 
 	public SuggestFieldSuggestion getValue() {
 		return this.value;
 	}
 	
 	/**
      * {@inheritDoc}
      */
     @Override
     public void focus() {
         super.focus();
     }
 
 	@Override
 	public void addFocusListener(FocusListener listener) {
 		addListener(FocusEvent.EVENT_ID, FocusEvent.class, listener,
 				FocusListener.focusMethod);
 
 	}
 
 	@Deprecated
 	@Override
 	public void addListener(FocusListener listener) {
 		addFocusListener(listener);
 
 	}
 
 	@Override
 	public void removeFocusListener(FocusListener listener) {
 		removeListener(FocusEvent.EVENT_ID, FocusEvent.class, listener);
 
 	}
 
 	@Deprecated
 	@Override
 	public void removeListener(FocusListener listener) {
 		removeFocusListener(listener);
 
 	}
 
 	@Override
 	public void addBlurListener(BlurListener listener) {
 		addListener(BlurEvent.EVENT_ID, BlurEvent.class, listener,
 				BlurListener.blurMethod);
 
 	}
 
 	@Deprecated
 	@Override
 	public void addListener(BlurListener listener) {
 		addBlurListener(listener);
 
 	}
 
 	@Override
 	public void removeBlurListener(BlurListener listener) {
 		removeListener(BlurEvent.EVENT_ID, BlurEvent.class, listener);
 
 	}
 
 	@Deprecated
 	@Override
 	public void removeListener(BlurListener listener) {
 		removeBlurListener(listener);
 
 	}
 
 	public boolean isDisplayStringHTML() {
 		return getState().isDisplayStringHTML;
 	}
 
 	public void setDisplayStringHTML(boolean isDisplayStringHTML) {
 		getState().isDisplayStringHTML = isDisplayStringHTML;
 		markAsDirty();
 	}
 
 	public SuggestionLoader getSuggestionLoader() {
 		return suggestionLoader;
 	}
 
 	public void setSuggestionLoader(SuggestionLoader suggestionLoader) {
 		this.suggestionLoader = suggestionLoader;
 	}
 
 	@Override
 	public int getTabIndex() {
 		return getState().tabIndex;
 	}
 
 	@Override
 	public void setTabIndex(int tabIndex) {
 		getState().tabIndex = tabIndex;
 
 	}
 
 	
 	
 	public static class SuggestionSelectedEvent extends Component.Event {
 
 		private static final long serialVersionUID = -6562989408413288373L;
 		
 		private final SuggestFieldSuggestion suggestion;
 		
 		public SuggestionSelectedEvent(Component source, SuggestFieldSuggestion suggestion) {
 			super(source);
 			this.suggestion = suggestion;
 		}		
 		
 		public SuggestFieldSuggestion getSuggestion() {
 			return suggestion;
 		}	
 	}
 	
 	public interface SuggestionSelectedListener extends Serializable {
 		public void onSuggestionSelected(SuggestionSelectedEvent event);
 	}
 	
 	/* Value change events */
 
 	private static final Method VALUE_CHANGE_METHOD;
 
 	static {
 		try {
 			VALUE_CHANGE_METHOD = SuggestionSelectedListener.class
 					.getDeclaredMethod("onSuggestionSelected",
 							new Class[] { SuggestionSelectedEvent.class });
 		} catch (final java.lang.NoSuchMethodException e) {
 			// This should never happen
 			throw new java.lang.RuntimeException(
 					"Internal error finding methods in AbstractField");
 		}
 	}
 
 	/**
 	 * Emits the value change event. The value contained in the field is
 	 * validated before the event is created.
 	 */
 	protected void fireSuggestionSelected() {
 		fireEvent(new SuggestionSelectedEvent(this, getValue()));		
 	}
 	
 	public void addSuggestionSelectedListener(SuggestionSelectedListener listener) {
 		addListener(SuggestionSelectedEvent.class, listener,
 				VALUE_CHANGE_METHOD);
 
 	}
 		
 	public void removeSuggestionSelectedListener(SuggestionSelectedListener listener) {
 		removeListener(SuggestionSelectedEvent.class, listener,
 				VALUE_CHANGE_METHOD);
 
 	}	
 
 }
