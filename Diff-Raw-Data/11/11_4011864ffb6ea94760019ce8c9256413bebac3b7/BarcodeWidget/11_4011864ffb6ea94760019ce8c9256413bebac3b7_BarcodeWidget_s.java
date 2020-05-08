 package view.components.widgets;
 
 import org.mt4j.components.TransformSpace;
 import org.mt4j.components.visibleComponents.font.FontManager;
 import org.mt4j.components.visibleComponents.font.IFont;
 import org.mt4j.components.visibleComponents.widgets.MTImage;
 import org.mt4j.components.visibleComponents.widgets.MTTextArea;
 import org.mt4j.input.inputProcessors.IGestureEventListener;
 import org.mt4j.input.inputProcessors.MTGestureEvent;
 import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapEvent;
 import org.mt4j.input.inputProcessors.componentProcessors.tapProcessor.TapProcessor;
 import org.mt4j.util.MTColor;
 import org.mt4j.util.math.Vector3D;
 
 import rfid.idtronic.evo.desktop.hf.EDHFReply;
 import view.components.MTAbstractWindow;
 import bookshelf.apis.libis.LibisBarcode;
 import controllers.SuggestableScene;
 
 public class BarcodeWidget extends MTAbstractWindow {
 	private EDHFReply tag;
 	
 	public BarcodeWidget(final SuggestableScene scene, float x, float y) {
 		super(scene.getMTApplication(), x, y, 575, 250, "Enter barcode from inside backcover");
 		
 		IFont font = FontManager.getInstance().createFont(scene.getMTApplication(), "fonts/Trebuchet MS.ttf", 
 				20, 	//Font size
 				new MTColor(255,255,255));	//Font color
 		
 		float w = 55;
 		float h = 35;
 		float sx = 5;
 		float sy = 5+h+5;
 		
 		final MTTextArea barcode = new MTTextArea(scene.getMTApplication(), 5, 5, 3*w, h, font);
 		barcode.setFillColor(new MTColor(0, 0, 0, 255));
 		barcode.setStrokeWeight(2.5f);
 		barcode.setStrokeColor(new MTColor(255, 255, 255, 150));
 		getContainer().addChild(barcode);
 		
 		MTTextArea buttons[] = new MTTextArea[12];
 		
 		for (int i=0; i<buttons.length; i++) {
 			buttons[i] = new MTTextArea(scene.getMTApplication(), sx+(i%3)*w, sy+((int) (i/3))*h, w, h, font);
 			buttons[i].setFillColor(new MTColor(0, 0, 0, 255));
 			buttons[i].setStrokeWeight(2.5f);
 			buttons[i].setStrokeColor(new MTColor(255, 255, 255, 150));
 		}
 		
 		for (int i=0; i<buttons.length; i++) {
 			getContainer().addChild(buttons[i]);
 			buttons[i].setText(Integer.toString(i+1));
 			if (i==9) {
 				buttons[i].setText("Corr");
 				buttons[i].removeAllGestureEventListeners();
 				buttons[i].setFillColor(new MTColor(185, 87, 89, 255));
 				buttons[i].registerInputProcessor(new TapProcessor(scene.getMTApplication()));
 				buttons[i].addGestureListener(TapProcessor.class, new IGestureEventListener() {
 					@Override
 					public boolean processGestureEvent(MTGestureEvent ge) {
 						TapEvent te = (TapEvent) ge;
 						
 						if (te.getTapID() == TapEvent.TAPPED) {
 							barcode.setText(barcode.getText().substring(0, barcode.getText().length()-1));
 							if (barcode.getText().length() == 9)
 								barcode.setFillColor(new MTColor(136, 185, 129, 255));
 							else
 								barcode.setFillColor(new MTColor(185, 87, 89, 255));
 						}
 						
 						return true;
 					}
 				});
 			} else if (i==10) {
 				buttons[i].setText("0");
 				buttons[i].removeAllGestureEventListeners();
 				buttons[i].registerInputProcessor(new TapProcessor(scene.getMTApplication()));
 				buttons[i].addGestureListener(TapProcessor.class, new IGestureEventListener() {
 					@Override
 					public boolean processGestureEvent(MTGestureEvent ge) {
 						TapEvent te = (TapEvent) ge;
 						
 						if (te.getTapID() == TapEvent.TAPPED) {
 							barcode.setText(barcode.getText()+"0");
 							
 							if (barcode.getText().length() == 9)
 								barcode.setFillColor(new MTColor(136, 185, 129, 255));
 							else {
 								barcode.setFillColor(new MTColor(185, 87, 89, 255));
 							}
 						}
 						
 						return true;
 					}
 				});
 			} else if (i==11) {
 				buttons[i].setText("Ok");
 				buttons[i].setFillColor(new MTColor(136, 185, 129, 255));
 				buttons[i].removeAllGestureEventListeners();
 				buttons[i].registerInputProcessor(new TapProcessor(scene.getMTApplication()));
 				buttons[i].addGestureListener(TapProcessor.class, new IGestureEventListener() {
 					@Override
 					public boolean processGestureEvent(MTGestureEvent ge) {
 						TapEvent te = (TapEvent) ge;
 						
 						if (te.getTapID() == TapEvent.TAPPED) {
 							if (barcode.getText().length() == 9)
 								barcode.setFillColor(new MTColor(0, 0, 0, 255));
 							else {
 								barcode.setFillColor(new MTColor(185, 87, 89, 255));
 								return true;
 							}
 							
 							if (tag != null)
 								scene.getTagController().addTag(getTag(), new LibisBarcode(barcode.getText()));
 							
							setVisible(false);
 							scene.processBarcode(new LibisBarcode(barcode.getText()), tag);
 						}
 						
 						return true;
 					}
 				});
 			} else {
 				final int number = i;
 				buttons[i].removeAllGestureEventListeners();
 				buttons[i].registerInputProcessor(new TapProcessor(scene.getMTApplication()));
 				buttons[i].addGestureListener(TapProcessor.class, new IGestureEventListener() {
 					@Override
 					public boolean processGestureEvent(MTGestureEvent ge) {
 						TapEvent te = (TapEvent) ge;
 						
 						if (te.getTapID() == TapEvent.TAPPED) {
 							barcode.setText(barcode.getText()+(number+1));
 							
 							if (barcode.getText().length() == 9)
 								barcode.setFillColor(new MTColor(136, 185, 129, 255));
 							else {
 								barcode.setFillColor(new MTColor(185, 87, 89, 255));
 								return true;
 							}
 						}
 						
 						return true;
 					}
 				});
 			}
 		}
 		
 		MTImage preview = new MTImage(scene.getMTApplication(), scene.getMTApplication().loadImage("data/images/barcode.png"));
 		getContainer().addChild(preview);
 		preview.setPositionRelativeToParent(new Vector3D(20+3*w+preview.getWidthXY(TransformSpace.LOCAL)/2,getContainer().getCenterPointLocal().getY()));
 	}
 
 	public void processTag(EDHFReply tag) {
 		this.tag = tag;
 		this.setVisible(true);
 	}
 	
 	private EDHFReply getTag() {
 		return this.tag;
 	}
 }
