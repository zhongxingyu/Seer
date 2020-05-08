 package me.hajo.editor.client;
 
 import me.hajo.editor.client.EntryPoint.StateStorage;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.ResizeComposite;
 import com.google.gwt.user.client.ui.Widget;
 
 public class GuiContainer extends ResizeComposite {
 	private static GuiContainerUiBinder uiBinder = GWT.create(GuiContainerUiBinder.class);
 
 	interface GuiContainerUiBinder extends UiBinder<Widget, GuiContainer> {
 	}
 
	@UiField(provided = true)
	final HTMLPanel canvas;
 
	public GuiContainer(HTMLPanel canvas, String image_upload_url, StateStorage stateStorage) {
		this.canvas = canvas;
 		initWidget(uiBinder.createAndBindUi(this));
 	}
 
 }
