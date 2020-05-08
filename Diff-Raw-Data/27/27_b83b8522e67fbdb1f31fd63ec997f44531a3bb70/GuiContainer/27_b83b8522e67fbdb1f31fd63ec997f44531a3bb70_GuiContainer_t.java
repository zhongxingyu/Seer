 package me.hajo.editor.client;
 
 import me.hajo.editor.client.EntryPoint.StateStorage;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.uibinder.client.UiBinder;
 import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
 import com.google.gwt.user.client.ui.HTMLPanel;
 import com.google.gwt.user.client.ui.ResizeComposite;
 import com.google.gwt.user.client.ui.Widget;
 
 public class GuiContainer extends ResizeComposite {
 	private static GuiContainerUiBinder uiBinder = GWT.create(GuiContainerUiBinder.class);
 
 	interface GuiContainerUiBinder extends UiBinder<Widget, GuiContainer> {
 	}
 
	@UiField
	FlowPanel canvas;
 
	public GuiContainer(HTMLPanel icanvas, String image_upload_url, StateStorage stateStorage) {
 		initWidget(uiBinder.createAndBindUi(this));
		icanvas.removeFromParent();
		this.canvas.add(icanvas);
 	}
 
 }
