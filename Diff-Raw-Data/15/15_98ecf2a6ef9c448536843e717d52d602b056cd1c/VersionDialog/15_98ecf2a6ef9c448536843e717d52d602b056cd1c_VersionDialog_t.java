 package org.cotrix.web.codelistmanager.client.codelists;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.uibinder.client.UiHandler;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.PopupPanel;
 import com.google.gwt.user.client.ui.TextBox;
 import com.google.gwt.user.client.ui.Widget;
 
 /**
  * @author "Federico De Faveri federico.defaveri@fao.org"
  *
  */
 public class VersionDialog extends PopupPanel {
 	
 	private static final Binder binder = GWT.create(Binder.class);
 	interface Binder extends UiBinder<Widget, VersionDialog> {}
 	
 	public interface VersionDialogListener {
 		public void onCreate(String id, String newVersion);
 	}
 	
 	@UiField Label name;
 	
	@UiField TextBox oldVersion;
 	
 	@UiField TextBox newVersion;
 	
 	protected String id;
 	protected VersionDialogListener listener;
 
 	public VersionDialog(VersionDialogListener listener) {
 		this.listener = listener;
 		setWidget(binder.createAndBindUi(this));
 		setAutoHideEnabled(true);
 	}
 	
 	public void setOldVersion(String id, String name, String oldVersion)
 	{
 		this.id = id;
 		this.name.setText(name);
 		this.oldVersion.setText(oldVersion);
 		this.newVersion.setText(oldVersion);
 	}
 	
 	@UiHandler("create")
 	protected void onCreate(ClickEvent clickEvent)
 	{
 		listener.onCreate(id, newVersion.getValue());
 		hide();
 	}
 
 }
