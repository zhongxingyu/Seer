 package com.workhub.mt4j;
 
 import jade.core.AID;
 
 import java.awt.Image;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Locale;
 import java.util.Map.Entry;
 
 import javax.swing.ImageIcon;
 import javax.swing.JOptionPane;
 
 import org.mt4j.components.MTComponent;
 import org.mt4j.components.TransformSpace;
 import org.mt4j.components.visibleComponents.font.FontManager;
 import org.mt4j.components.visibleComponents.font.IFont;
 import org.mt4j.components.visibleComponents.shapes.MTRectangle;
 import org.mt4j.components.visibleComponents.widgets.MTColorPicker;
 import org.mt4j.components.visibleComponents.widgets.MTListCell;
 import org.mt4j.components.visibleComponents.widgets.MTTextField;
 import org.mt4j.input.IMTInputEventListener;
 import org.mt4j.input.inputData.AbstractCursorInputEvt;
 import org.mt4j.input.inputData.MTInputEvent;
 import org.mt4j.input.inputProcessors.IGestureEventListener;
 import org.mt4j.input.inputProcessors.MTGestureEvent;
 import org.mt4j.input.inputProcessors.componentProcessors.dragProcessor.DragProcessor;
 import org.mt4j.util.MTColor;
 import org.mt4j.util.math.Vector3D;
 
 import processing.core.PApplet;
 import processing.core.PImage;
 
 import com.workhub.model.ElementModel;
 import com.workhub.utils.Constants;
 import com.workhub.utils.PDFUtils;
 
 public class ContextButton extends MTListCell {
 	private MTTextField m_text;
 	private final MTComponent m_source;
 	private ContextMenu m_menu;
 
 	public ContextButton(final PApplet applet, final WorkHubScene scene, MTComponent source, ContextMenu menu, final String text) {
 		super(MT4JConstants.CONTEXT_BUTTON_WIDTH, MT4JConstants.CONTEXT_BUTTON_HEIGHT, applet);
 
 		IFont font = FontManager.getInstance().createFont(applet, "arial.ttf", 18);
 		m_source = source;
 		m_menu = menu;
 		m_text = new MTTextField(0, 0, MT4JConstants.CONTEXT_BUTTON_WIDTH, MT4JConstants.CONTEXT_BUTTON_HEIGHT, font, applet);
 		m_text.setFillColor(new MTColor(110, 200, 240, 255));
 		m_text.setStrokeColor(new MTColor(110, 170, 200, 255));	
 		m_text.setText(text);
 		addChild(m_text);
 
 		addInputListener(new IMTInputEventListener() {
 			@Override
 			public boolean processInputEvent(MTInputEvent inEvt) {
 				if (inEvt instanceof AbstractCursorInputEvt) {
 					AbstractCursorInputEvt cursorInputEvt = (AbstractCursorInputEvt) inEvt;
 					switch (cursorInputEvt.getId()) {
 					case AbstractCursorInputEvt.INPUT_DETECTED:
 						switch (text) {
 						case MT4JConstants.CONTEXT_BUTTON_CLOSE:
 							//just close the menu (instruction below)
 							break;
 						case MT4JConstants.CONTEXT_BUTTON_DELETE:
 							if(m_source instanceof AbstractElementView) {
 								AbstractElementView element = (AbstractElementView)m_source;
 								JadeInterface.getInstance().deleteElement(element.getModel().getAgent());
 							}
 							else if(m_source instanceof ElementGroupView) {
 								for(MTComponent comp : m_source.getChildren()) {
 									if(comp instanceof AbstractElementView) {
 										AbstractElementView element = (AbstractElementView)comp;
 										JadeInterface.getInstance().deleteElement(element.getModel().getAgent());
 									}
 									else {
 										// Normalement impossible
 									}
 								}
 							}
 							else if(m_source instanceof WorkHubButton) {
 								WorkHubButton button = (WorkHubButton)m_source;
 								scene.removeShortcut(button.getText());
 							}
 							else {
 								// Type d'element inconnu
 							}
 							m_source.destroy();
 							break;
 						case MT4JConstants.CONTEXT_BUTTON_CREATE_TEXT:
 							TextElementView textElement = new TextElementView(((MTRectangle) getParent().getParent()).getPosition(TransformSpace.GLOBAL).x,
 									((MTRectangle) getParent().getParent()).getPosition(TransformSpace.GLOBAL).y,
 									MT4JConstants.ELEMENT_DEFAULT_WIDTH, MT4JConstants.ELEMENT_DEFAULT_HEIGHT, applet, scene);
 							textElement.addLassoProcessor();
 							JadeInterface.getInstance().createElement(Constants.TYPE_ELEMENT_TEXT);
 							break;
 						case MT4JConstants.CONTEXT_BUTTON_CREATE_IMAGE:
 							String imagePath = applet.selectInput();
 							ImageElementView imageElement = new ImageElementView(imagePath, ((MTRectangle) getParent().getParent()).getPosition(TransformSpace.GLOBAL).x, 
 									((MTRectangle) getParent().getParent()).getPosition(TransformSpace.GLOBAL).y, 
 									MT4JConstants.ELEMENT_DEFAULT_WIDTH, MT4JConstants.ELEMENT_DEFAULT_HEIGHT, applet, scene);
 							imageElement.addLassoProcessor();
 							JadeInterface.getInstance().createElement(Constants.TYPE_ELEMENT_PICTURE);
 							break;
 						case MT4JConstants.CONTEXT_BUTTON_CREATE_LINK:
 							LinkElementView linkElement = new LinkElementView(((MTRectangle) getParent().getParent()).getPosition(TransformSpace.GLOBAL).x, 
 									((MTRectangle) getParent().getParent()).getPosition(TransformSpace.GLOBAL).y, 
 									MT4JConstants.ELEMENT_DEFAULT_WIDTH, MT4JConstants.ELEMENT_DEFAULT_HEIGHT, applet, scene);
 							linkElement.addLassoProcessor();
 							JadeInterface.getInstance().createElement(Constants.TYPE_ELEMENT_LINK);
 							break;
 						case MT4JConstants.CONTEXT_BUTTON_CREATE_FILE:
 							String filePath = applet.selectInput();
 							FileElementView fileElement = new FileElementView(filePath, ((MTRectangle) getParent().getParent()).getPosition(TransformSpace.GLOBAL).x, 
 									((MTRectangle) getParent().getParent()).getPosition(TransformSpace.GLOBAL).y, 
 									MT4JConstants.ELEMENT_DEFAULT_WIDTH, MT4JConstants.ELEMENT_DEFAULT_HEIGHT, applet, scene);
 							fileElement.addLassoProcessor();
 							JadeInterface.getInstance().createElement(Constants.TYPE_ELEMENT_FILE);
 							break;
 						case MT4JConstants.CONTEXT_BUTTON_VISUALIZE_ELEMENTS:
 							ContextMenu.elementViewLocation.add(getPosition(TransformSpace.GLOBAL));
 							JadeInterface.getInstance().getElementList();
 							break;
 						case MT4JConstants.CONTEXT_BUTTON_EDIT_TITLE:
 							((AbstractElementView) m_source).tryEditElementTitle();
 							break;
 						case MT4JConstants.CONTEXT_BUTTON_EDIT_CONTENT:
 							((AbstractElementView) m_source).tryEditElementContent();
 							break;
 						case MT4JConstants.CONTEXT_BUTTON_SHARE:
 							ContextMenu.exportLocation.add(new ExportData(m_source, cursorInputEvt.getPosition()));
 							JadeInterface.getInstance().getNeightbourgList();
 							break;
 						case MT4JConstants.CONTEXT_BUTTON_CHANGE_COLOR:
 							PImage colPick = applet.loadImage("Image/colorcircle.png");
 							final MTColorPicker colorWidget = new MTColorPicker(0, 0, colPick, applet);
 							int colPickX = (int)cursorInputEvt.getPosX();
 							int colPickY = (int)cursorInputEvt.getPosY();
 							colorWidget.setPositionGlobal(new Vector3D(colPickX, colPickY));
 							MT4JUtils.fixPosition(colorWidget, colPickX, colPickY, applet, PositionAnchor.CENTER);
 							colorWidget.setStrokeColor(MTColor.WHITE);
 							colorWidget.addGestureListener(DragProcessor.class, new IGestureEventListener() {
 								public boolean processGestureEvent(MTGestureEvent ge) {
 									if (ge.getId()== MTGestureEvent.GESTURE_ENDED){
 										colorWidget.destroy();
 										((AbstractElementView)m_source).saveModel();
 									}else{
 										((AbstractElementView)m_source).setFillColor(colorWidget.getSelectedColor());
 									}
 									return false;
 								}
 							});
 							getParent().getParent().getParent().addChild(colorWidget);
 							colorWidget.setVisible(true);
 							break;
 						case MT4JConstants.CONTEXT_BUTTON_EXPORT_PDF:
 							ArrayList<ElementModel> elements = new ArrayList<>();
 							if(m_source instanceof ElementGroupView) {
 								for(MTComponent comp : ((ElementGroupView)m_source).getChildren()) {
 									if(comp instanceof AbstractElementView) {
 										elements.add(((AbstractElementView)comp).getModel());
 									}
 								}
 							}
 							else if(m_source instanceof AbstractElementView) {
 								elements.add(((AbstractElementView)m_source).getModel());
 							} else {
 								// Impossible
 							}
 							
 							Locale locale = Locale.getDefault();
 							DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL, locale);
 							
 							File f = new File("");
 							f.mkdirs();
 							f = new File("export "+dateFormat.format(new Date())+".pdf");
 							try {
 								f.createNewFile();
 							} catch (IOException e) {
 								e.printStackTrace();
 							}
 							
 							try {
 								PDFUtils.createPDF("WorkHub_" + dateFormat.format(new Date()) + ".pdf",
 										JadeInterface.getInstance().getNickname(), elements, new FileOutputStream(f));
 							} catch (FileNotFoundException e) {
 								e.printStackTrace();
 							}
 							break;
 						case MT4JConstants.CONTEXT_BUTTON_HIDE:
							m_source.removeFromParent();
 							m_source.destroy();
 							break;
 						case MT4JConstants.CONTEXT_BUTTON_SPLIT_GROUP:
 							m_source.removeAllChildren();
 							m_source.destroy();
 							break;
 						case MT4JConstants.CONTEXT_BUTTON_EXIT:
 							Object[] options = {"Quitter",
 									"Annuler",};
 							ImageIcon icon = new ImageIcon("Image/logo.png");
 							Image img = icon.getImage();
 							Image newimg = img.getScaledInstance( 40, 40,  java.awt.Image.SCALE_SMOOTH );
 							icon.setImage(newimg);
 							int confirmation = JOptionPane.showOptionDialog(applet,
 									"Etes-vous sr de vouloir quitter ?",
 									"Quitter WorkHub",
 									JOptionPane.YES_NO_OPTION,
 									JOptionPane.QUESTION_MESSAGE,
 									icon, options, null);
 							if (0 == confirmation)
 								applet.exit();
 							break;
 						}
 						m_menu.destroy();
 						break;
 					case AbstractCursorInputEvt.INPUT_ENDED:
 						break;
 					case AbstractCursorInputEvt.INPUT_UPDATED:
 						break;
 					default:
 						break;
 					}
 				}
 				return false;
 			}
 		});
 	}
 
 	public ContextButton(PApplet applet, final WorkHubScene scene, ContextMenu menu, final MTComponent source, final Entry<AID, String> entry, int menuType) {
 		super(MT4JConstants.CONTEXT_BUTTON_WIDTH, MT4JConstants.CONTEXT_BUTTON_HEIGHT, applet);
 		IFont font = FontManager.getInstance().createFont(applet, "arial.ttf", 18);
 		m_source = source;
 		m_menu = menu;
 		m_text = new MTTextField(0, 0, MT4JConstants.CONTEXT_BUTTON_WIDTH, MT4JConstants.CONTEXT_BUTTON_HEIGHT, font, applet);
 		m_text.setFillColor(new MTColor(110, 200, 240, 255));
 		m_text.setText(entry.getValue());
 		addChild(m_text);
 
 		switch(menuType) {
 		
 		case MT4JConstants.CONTEXT_IMPORT_MENU :
 			addInputListener(new IMTInputEventListener() {
 				@Override
 				public boolean processInputEvent(MTInputEvent inEvt) {
 					if (inEvt instanceof AbstractCursorInputEvt) {
 						AbstractCursorInputEvt cursorInputEvt = (AbstractCursorInputEvt) inEvt;
 						if(cursorInputEvt.getId() == AbstractCursorInputEvt.INPUT_DETECTED) {
 							if(scene.getElement(entry.getKey()) == null) {
 								ContextMenu.importLocation.add(cursorInputEvt.getPosition());
 								JadeInterface.getInstance().getElement(entry.getKey());
 							}
 							else {
 								// La vue est deja presente
 							}
 						}
 					}
 					m_menu.destroy();
 					return false;
 				}
 			});
 			break;
 		
 		case MT4JConstants.CONTEXT_EXPORT_MENU :
 			addInputListener(new IMTInputEventListener() {
 				@Override
 				public boolean processInputEvent(MTInputEvent inEvt) {
 					if (inEvt instanceof AbstractCursorInputEvt) {
 						AbstractCursorInputEvt cursorInputEvt = (AbstractCursorInputEvt) inEvt;
 						if(cursorInputEvt.getId() == AbstractCursorInputEvt.INPUT_DETECTED) {
 							if(source instanceof AbstractElementView) {
 								AbstractElementView element = (AbstractElementView)source;
 								JadeInterface.getInstance().sendElement(entry.getKey(), element.getModel().getAgent());
 							}
 							else if(source instanceof ElementGroupView) {
 								ElementGroupView group = (ElementGroupView)source;
 								for(MTComponent comp : group.getChildren()) {
 									if(comp instanceof AbstractElementView) {
 										AbstractElementView element = (AbstractElementView)comp;
 										JadeInterface.getInstance().sendElement(entry.getKey(), element.getModel().getAgent());
 									}
 									else {
 										// Polygone de contour, ne rien faire
 									}
 								}
 							}
 						}
 					}
 					m_menu.destroy();
 					return false;
 				}
 			});
 			break;
 			
 		default:
 			break;
 		}
 	}
 }
