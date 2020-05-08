 package org.softwareFm.displayCore.api;
 
 import java.util.Collections;
 import java.util.Map;
 
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.softwareFm.swtBasics.text.TitleAndTextField;
 
 public class BoundTitleAndTextField extends TitleAndTextField {
 
 	private String url;
 	private Map<String, Object> lastContext = Collections.<String, Object> emptyMap();
 
 	public BoundTitleAndTextField(Composite parent, final DisplayerContext displayerContext, final DisplayerDetails displayerDetails) {
 		super(displayerContext.configForTitleAnd, parent, displayerDetails.key);
 		addCrListener(new Listener() {
 			@Override
 			public void handleEvent(Event e) {
 				displayerContext.repository.modifyData(displayerDetails.entity, url, displayerDetails.key, getText(), lastContext);
 			}
 		});
 	}
 
 	public void setLastBindingContext(BindingContext bindingContext) {
 		this.url = bindingContext.url;
 		this.lastContext = bindingContext.context;
		setEditable(false);
 	}
 }
