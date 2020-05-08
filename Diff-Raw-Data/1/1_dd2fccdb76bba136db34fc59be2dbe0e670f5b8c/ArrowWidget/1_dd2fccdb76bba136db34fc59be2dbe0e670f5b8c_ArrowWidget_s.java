 package fr.hors.limite.photo.client.widget;
 
 import com.google.gwt.dom.client.ButtonElement;
 import com.google.gwt.dom.client.Document;
 import com.google.gwt.dom.client.ImageElement;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.HasClickHandlers;
 import com.google.gwt.event.dom.client.MouseOutEvent;
 import com.google.gwt.event.dom.client.MouseOutHandler;
 import com.google.gwt.event.dom.client.MouseOverEvent;
 import com.google.gwt.event.dom.client.MouseOverHandler;
 import com.google.gwt.event.shared.HandlerRegistration;
 import com.google.gwt.user.client.ui.ComplexPanel;
 
 public class ArrowWidget extends ComplexPanel implements HasClickHandlers,
 		MouseOutHandler, MouseOverHandler {
 
 	private static final String IMG_STYLE_SUFFIX_HOVER = "Hover";
 	private static final String IMG_STYLE_SUFFIX = "Img";
 	private final Position position;
 	private ImageElement imgElement;
 
 	public enum Position {
 		LEFT {
 			@Override
 			public String getValue() {
 				return "leftArrow";
 			}
 		},
 		RIGHT {
 			@Override
 			public String getValue() {
 				return "rightArrow";
 			}
 		};
 		public abstract String getValue();
 	}
 
 	public ArrowWidget(Position position) {
 		this.position = position;
 		ButtonElement pushButtonElement = Document.get()
 				.createPushButtonElement();
 		imgElement = Document.get().createImageElement();
 		pushButtonElement.appendChild(imgElement);
 		pushButtonElement.addClassName(position.getValue());
 		imgElement.setClassName(position.getValue() + IMG_STYLE_SUFFIX);
 		setElement(pushButtonElement);
 		this.addDomHandler(this, MouseOverEvent.getType());
 		this.addDomHandler(this, MouseOutEvent.getType());
 	}
 
 	@Override
 	public HandlerRegistration addClickHandler(ClickHandler handler) {
 		return this.addDomHandler(handler, ClickEvent.getType());
 	}
 
 	@Override
 	public void onMouseOver(MouseOverEvent event) {
 		imgElement.addClassName(position.getValue() + IMG_STYLE_SUFFIX_HOVER);
 	}
 
 	@Override
 	public void onMouseOut(MouseOutEvent event) {
 		imgElement
 				.removeClassName(position.getValue() + IMG_STYLE_SUFFIX_HOVER);
 	}
 
 }
