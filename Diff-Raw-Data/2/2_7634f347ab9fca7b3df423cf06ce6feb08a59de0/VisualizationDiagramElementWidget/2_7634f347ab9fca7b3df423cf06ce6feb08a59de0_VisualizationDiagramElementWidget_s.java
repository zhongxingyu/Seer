 package snomed.visualization.vaadin.client.diagramelement;
 
 import snomed.visualization.vaadin.client.model.VisualizationModifyType;
 import snomed.visualization.vaadin.client.model.VisualizationDiagramElementModel;
 import snomed.visualization.vaadin.client.model.VisualizationDiagramElementModel.VisualizationComponentType;
 
 import com.google.gwt.canvas.client.Canvas;
 import com.google.gwt.canvas.dom.client.Context2d;
 import com.google.gwt.canvas.dom.client.CssColor;
 import com.google.gwt.dom.client.ImageElement;
 import com.google.gwt.event.dom.client.ClickEvent;
 import com.google.gwt.event.dom.client.ClickHandler;
 import com.google.gwt.event.dom.client.MouseOutEvent;
 import com.google.gwt.event.dom.client.MouseOutHandler;
 import com.google.gwt.event.dom.client.MouseOverEvent;
 import com.google.gwt.event.dom.client.MouseOverHandler;
 import com.google.gwt.user.client.ui.Composite;
 import com.google.gwt.user.client.ui.Image;
 import com.google.gwt.user.client.ui.Label;
 import com.google.gwt.user.client.ui.VerticalPanel;
 
 /**
  * Custom widget for the diagram elements.
  * 
  * @author rporcio
  */
 public class VisualizationDiagramElementWidget extends Composite {
 
 	public static final String CLASSNAME = "visualizationdiagramelement";
 	private VisualizationDiagramElementServerRpc rpc;
 
 	private Canvas canvas;
 	private Context2d context;
 
 	private int zoomSize;
 	private int componentHeight;
 	private int componentWidth;
 
 	private int x = 0;
 	private int y = 0;
 	private boolean isMouseOver;
 	private ImageElement deletionIconElement;
 	private ImageElement characteristicIconElement;
 
 	public VisualizationDiagramElementWidget() {
 
 		VerticalPanel panel = new VerticalPanel();
 
 		canvas = Canvas.createIfSupported();
 		if (null == canvas) {
 			panel.add(new Label("Sorry, your browser doesn't support the HTML5 Canvas element."));
 		} else {
 			panel.add(canvas);
 			context = canvas.getContext2d();
 		}
 		
 		isMouseOver = false;
 
 		initWidget(panel);
 
 		// setText("VisualizationComponent sets the text via VisualizationComponentConnector using VisualizationComponentState");
 		setStyleName(CLASSNAME);
 
 	}
 	
 	public void setRpc(VisualizationDiagramElementServerRpc rpc) {
 		this.rpc = rpc;
 	}
 
 	public void visualize(final VisualizationDiagramElementState state) {
 		if (null != canvas) {
 
 			final VisualizationDiagramElementModel diagramElement = state.getComponentModel();
 
 			zoomSize = diagramElement.getSize();
 			componentWidth = zoomSize * 2;
 			componentHeight = zoomSize / 2;
 
 			context.clearRect(0, 0, componentWidth, componentHeight);
 
 			if (diagramElement.getType().equals(VisualizationComponentType.GROUP)) {
 				int groupSize = zoomSize / 5 * 2;
 				canvas.getElement().setAttribute("width", "" + groupSize);
 				canvas.getElement().setAttribute("height", "" + groupSize);
 				canvas.setPixelSize(groupSize, groupSize);
 			} else if (diagramElement.getType().equals(VisualizationComponentType.CONJUCTION)) {
 				int conjuctionSize = 10;
 				canvas.getElement().setAttribute("width", "" + conjuctionSize);
 				canvas.getElement().setAttribute("height", "" + conjuctionSize);
 				canvas.setPixelSize(conjuctionSize, conjuctionSize);
 			} else {
 				canvas.getElement().setAttribute("width", "" + (componentWidth + 6));
 				canvas.getElement().setAttribute("height", "" + (componentHeight + 6));
 				canvas.setPixelSize(componentWidth + 6, componentHeight + 6);
 
 			}
 
 			context.setFont((zoomSize / 10 + 3) + "px arial");
 
 			if (diagramElement.getType().equals(VisualizationComponentType.CONCEPT)) {
 				if (diagramElement.isDefined()) {
 					drawDefiningConcept(3, 3, diagramElement.getTerm());
 				} else {
 					drawPrimitiveConcept(3, 3, diagramElement.getTerm());
 				}
 			} else if (diagramElement.getType().equals(VisualizationComponentType.RELATIONSHIP)) {
 				if (diagramElement.isDefined()) {
 					drawDefiningRelationship(3, 3, diagramElement.getTerm());
 				} else {
 					drawNonDefiningRelationship(3, 3, diagramElement.getTerm());
 				}
 			} else if (diagramElement.getType().equals(VisualizationComponentType.GROUP)) {
 				drawGroup(diagramElement.getTerm());
 			} else if (diagramElement.getType().equals(VisualizationComponentType.CONJUCTION)) {
 				drawConjuction();
 			}
 
 			Image deletionIcon = new Image(state.getDeletionIcon().getURL());
 			Image characteristicIcon = new Image(state.getCharacteristicIcon().getURL());
 			
 			deletionIconElement = ImageElement.as(deletionIcon.getElement());
 			characteristicIconElement = ImageElement.as(characteristicIcon.getElement());
 
 			if (zoomSize < 100) {
 				deletionIconElement.setWidth(characteristicIconElement.getWidth() * zoomSize / 100);
 				deletionIconElement.setHeight(characteristicIconElement.getHeight() * zoomSize / 100);
 				characteristicIconElement.setWidth(characteristicIconElement.getWidth() * zoomSize / 100);
 				characteristicIconElement.setHeight(characteristicIconElement.getHeight() * zoomSize / 100);
 			}
 			
 			if (isMouseOver) {
 				drawModifyImages();
 			}
 
 			canvas.addMouseOverHandler(new MouseOverHandler() {
 				@Override
 				public void onMouseOver(MouseOverEvent event) {
 					if (diagramElement.getType().equals(VisualizationComponentType.CONCEPT) || diagramElement.getType().equals(VisualizationComponentType.RELATIONSHIP)) {
 						isMouseOver = true;
 						drawModifyImages();
 					}
 				}
 			});
 
 			canvas.addMouseOutHandler(new MouseOutHandler() {
 				@Override
 				public void onMouseOut(MouseOutEvent event) {
 					if (diagramElement.getType().equals(VisualizationComponentType.CONCEPT) || diagramElement.getType().equals(VisualizationComponentType.RELATIONSHIP)) {
 						isMouseOver = false;
 						visualize(state);
 					}
 				}
 			});
 
 			canvas.addClickHandler(new ClickHandler() {
 				@Override
 				public void onClick(ClickEvent event) {
 					event.preventDefault();
 					if (x != event.getX() && y != event.getY()) {
 						x = event.getX();
 						y = event.getY();
 
 						if (x > componentWidth - characteristicIconElement.getWidth() && y > componentHeight - characteristicIconElement.getHeight()) {
 							rpc.handleModify(diagramElement.getType(), VisualizationModifyType.CHARACTERISTIC_TYPE, diagramElement.getId());
 						} else if (x > componentWidth - deletionIconElement.getWidth() && y > componentHeight - 2.25 * characteristicIconElement.getHeight()) {
 							rpc.handleModify(diagramElement.getType(), VisualizationModifyType.DELETION, diagramElement.getId());
 						}
 					}
 
 				}
 			});
 		}
 	}
 
 	private void drawModifyImages() {
 		context.save();
 		context.drawImage(deletionIconElement, componentWidth - deletionIconElement.getWidth(), componentHeight - 2.25 * characteristicIconElement.getHeight());
 		context.drawImage(characteristicIconElement, componentWidth - characteristicIconElement.getWidth(), componentHeight - characteristicIconElement.getHeight());
 		context.restore();
 	}
 
 	private void drawGroup(String term) {
 		context.save();
 		int groupSize = zoomSize / 5;
 		context.arc(groupSize, groupSize, groupSize, 0, Math.PI * 2.0);
 		context.stroke();
 		if (null != term) {
 			context.moveTo(groupSize - groupSize / 2, groupSize - groupSize / 4);
 			context.lineTo(groupSize + groupSize / 2, groupSize - groupSize / 4);
 			context.moveTo(groupSize - groupSize / 2, groupSize);
 			context.lineTo(groupSize + groupSize / 2, groupSize);
 			context.moveTo(groupSize - groupSize / 2, groupSize + groupSize / 4);
 			context.lineTo(groupSize + groupSize / 2, groupSize + groupSize / 4);
 			context.stroke();
 		}
 		context.restore();
 	}
 
 	private void drawConjuction() {
 		context.save();
 		int conjunctionSize = 5;
 		context.arc(conjunctionSize, conjunctionSize, conjunctionSize, 0, Math.PI * 2.0);
 		context.fill();
 		context.restore();
 	}
 
 	private void drawDefiningConcept(final int x, final int y, final String term) {
 		context.save();
 		context.setFillStyle(CssColor.make(204, 204, 255));
 		context.fillRect(x, y, componentWidth, componentHeight);
 		context.strokeRect(x, y, componentWidth, componentHeight);
 		context.strokeRect(x - 3, y - 3, componentWidth + 6, componentHeight + 6);
 		context.restore();
 
 		drawTerm(term);
 	}
 
 	private void drawPrimitiveConcept(final int x, final int y, final String term) {
 		context.save();
 		context.setFillStyle(CssColor.make(99, 204, 255));
 		context.fillRect(x, y, componentWidth, componentHeight);
 		context.strokeRect(x, y, componentWidth, componentHeight);
 		context.restore();
 
 		drawTerm(term);
 	}
 
 	private void drawDefiningRelationship(final int x, final int y, final String term) {
 		context.save();
 		roundRect(x - 3, y - 3, componentWidth + 6, componentHeight + 6);
 		context.stroke();
 		roundRect(x, y, componentWidth, componentHeight);
 		context.stroke();
 		context.setFillStyle(CssColor.make(255, 255, 204));
 		roundRect(x, y, componentWidth, componentHeight);
 		context.fill();
 		context.restore();
 
 		drawTerm(term);
 	}
 
 	private void drawNonDefiningRelationship(final int x, final int y, final String term) {
 		context.save();
		context.setFillStyle(CssColor.make(204, 204, 99));
 		roundRect(x, y, componentWidth, componentHeight);
 		context.stroke();
 		roundRect(x, y, componentWidth, componentHeight);
 		context.fill();
 		context.restore();
 
 		drawTerm(term);
 	}
 
 	private void roundRect(final int x, final int y, final int width, final int height) {
 		int radius = componentWidth / 8;
 
 		context.beginPath();
 		context.moveTo(x + radius, y);
 		context.lineTo(x + width - radius, y);
 		context.quadraticCurveTo(x + width, y, x + width, y + radius);
 		context.lineTo(x + width, y + height - radius);
 		context.quadraticCurveTo(x + width, y + height, x + width - radius, y + height);
 		context.lineTo(x + radius, y + height);
 		context.quadraticCurveTo(x, y + height, x, y + height - radius);
 		context.lineTo(x, y + radius);
 		context.quadraticCurveTo(x, y, x + radius, y);
 		context.closePath();
 	}
 
 	private void drawTerm(String term) {
 		context.save();
 
 		if (term.length() <= (zoomSize / 2 - 10)) {
 			double position = term.length() / 2 * 5.5;
 			context.fillText(term, 3 + componentWidth / 2 - position, 7 + componentHeight / 2);
 		} else {
 			double position = term.substring(0, term.length() / 2).length() / 2 * 5.5;
 			context.fillText(term.substring(0, term.length() / 2), 3 + componentWidth / 2 - position, 7 + componentHeight / 4);
 			position = term.substring(term.length() / 2).length() / 2 * 5.5;
 			context.fillText(term.substring(term.length() / 2), 3 + componentWidth / 2 - position, 7 + componentHeight / 1.5);
 		}
 		context.restore();
 	}
 
 }
