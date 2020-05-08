 /**
  * 
  */
 package org.cotrix.web.codelistmanager.client.codelist;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.uibinder.client.UiTemplate;
 import com.google.gwt.user.client.ui.Button;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.ToggleButton;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class CodelistToolbarImpl extends Composite implements CodelistToolbar {
 	
 	@UiTemplate("CodelistToolbar.ui.xml")
 	interface CodelistToolbarUiBinder extends UiBinder<Widget, CodelistToolbarImpl> {}
 	private static CodelistToolbarUiBinder uiBinder = GWT.create(CodelistToolbarUiBinder.class);
 	
 	@UiField Button allColumns;
 	@UiField Button allNormals;
 	
 	@UiField ToggleButton lock;
	@UiField Button finalize;
 	
 	protected ToolBarListener listener;
 	
 	public CodelistToolbarImpl() {
 		initWidget(uiBinder.createAndBindUi(this));
 	}
 	
 	@UiHandler("allColumns")
 	protected void onAllColumnsClick(ClickEvent event) {
 		listener.onAction(Action.ALL_COLUMN);
 	}
 	
 	@UiHandler("allNormals")
 	protected void onAllNormalsClick(ClickEvent event) {
 		listener.onAction(Action.ALL_NORMAL);
 	}
 	
 	@UiHandler("lock")
 	protected void onLockClick(ClickEvent event) {
 		if (lock.isDown()) listener.onAction(Action.LOCK);
 		else listener.onAction(Action.UNLOCK);
 	}
 	
	@UiHandler("finalize")
 	protected void onSealClick(ClickEvent event) {
 		listener.onAction(Action.FINALIZE);
 	}
 
 	@Override
 	public void setListener(ToolBarListener listener) {
 		this.listener = listener;
 	}
 
 	@Override
 	public void setEnabled(Action action, boolean enabled) {
 		switch (action) {
 			case ALL_COLUMN: allColumns.setEnabled(enabled); break;
 			case ALL_NORMAL: allNormals.setEnabled(enabled); break;
			case FINALIZE: finalize.setEnabled(enabled); break;
 			case LOCK: if (!enabled) lock.setDown(true); break;
 			case UNLOCK: if (!enabled) lock.setDown(false); break;
 		}
 	}
 
 }
