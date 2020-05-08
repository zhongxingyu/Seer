 package ch.cern.atlas.apvs.client.validation;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import com.github.gwtbootstrap.client.ui.ListBox;
 import com.google.gwt.event.dom.client.BlurEvent;
 import com.google.gwt.event.dom.client.BlurHandler;
 import com.google.gwt.event.dom.client.ChangeEvent;
 import com.google.gwt.event.dom.client.ChangeHandler;
 import com.google.gwt.event.dom.client.FocusEvent;
 import com.google.gwt.event.dom.client.FocusHandler;
 
 public class ListBoxField extends ValidationField {
 
 	private ListBox box;
 	private Map<String, Integer> ids;
 	
 	public ListBoxField(String fieldLabel, Validator validator) {
 		super(fieldLabel, validator);
 	    box = new ListBox();
 		
 		setField(box);
 		
 		ids = new HashMap<String, Integer>();
 		
 		box.addBlurHandler(new BlurHandler() {
 			
 			@Override
 			public void onBlur(BlurEvent event) {
 				validate(true);
 			}
 		});
 		
 		box.addFocusHandler(new FocusHandler() {
 			
 			@Override
 			public void onFocus(FocusEvent event) {
 				validate(true);
 			}
 		});
 		
 		box.addChangeHandler(new ChangeHandler() {
 			
 			@Override
 			public void onChange(ChangeEvent event) {
 				validate(true);
 			}
 		});
 	}
 	
 	public ListBoxField(String fieldLabel) {
 		this(fieldLabel, null);
 	}
 	
 	public String getValue() {
 		return box.getValue();
 	}
 	
 	public Integer getId() {
 		return ids.get(box.getValue());
 	}
 
 	public void addItem(String item, int id) {
 		box.addItem(item);
 		ids.put(item, id);
 	}
 }
