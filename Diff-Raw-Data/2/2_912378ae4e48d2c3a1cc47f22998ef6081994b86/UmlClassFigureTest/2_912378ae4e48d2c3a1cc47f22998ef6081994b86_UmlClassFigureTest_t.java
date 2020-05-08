 package edu.uci.lighthouse.ui.graph;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.GridData;
 import org.eclipse.draw2d.GridLayout;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.Label;
 import org.eclipse.draw2d.MarginBorder;
 import org.eclipse.draw2d.Panel;
 import org.eclipse.draw2d.PositionConstants;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.jdt.internal.ui.JavaPlugin;
 import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
 import org.eclipse.jdt.ui.ISharedImages;
 import org.eclipse.jdt.ui.JavaElementImageDescriptor;
 import org.eclipse.jdt.ui.JavaUI;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.DecorationOverlayIcon;
 import org.eclipse.jface.viewers.IDecoration;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 
 import edu.uci.lighthouse.model.LighthouseClass;
 import edu.uci.lighthouse.model.LighthouseEntity;
 import edu.uci.lighthouse.model.LighthouseEvent;
 import edu.uci.lighthouse.model.LighthouseField;
 import edu.uci.lighthouse.model.LighthouseMethod;
 import edu.uci.lighthouse.model.LighthouseModel;
 import edu.uci.lighthouse.model.LighthouseModelManager;
 import edu.uci.lighthouse.model.util.UtilModifiers;
 import edu.uci.lighthouse.ui.LighthouseUIPlugin;
 import edu.uci.lighthouse.ui.graph.IUmlClass.LEVEL;
 import edu.uci.lighthouse.ui.swt.util.ColorFactory;
 import edu.uci.lighthouse.ui.swt.util.FontFactory;
 import edu.uci.lighthouse.ui.swt.util.Icons;
 import edu.uci.lighthouse.ui.swt.util.ImageFactory;
 import edu.uci.lighthouse.ui.views.FilterManager;
 import edu.uci.lighthouse.views.filters.IClassFilter;
 
 public class UmlClassFigureTest extends Panel{
 	
 	private LighthouseClass umlClass;
 	private List<IFigure> separators = new LinkedList<IFigure>();
 	private List<IFigure> separatorEvents = new LinkedList<IFigure>();
 	private HashMap<IFigure,LighthouseEntity> fig2entityMap = new HashMap<IFigure,LighthouseEntity>();
 	private static LEVEL currentLevel = LEVEL.ONE;
 
 	private final int NUM_COLUMNS = 2;
 	//private static final SharedImages jdtImg = new SharedImages();
 	
 	//public static enum LEVEL {ONE, TWO, THREE, FOUR};
 
 	public UmlClassFigureTest(LighthouseClass umlClass){		
 		GridLayout layout = new GridLayout();
 		layout.horizontalSpacing = 0;
 		layout.verticalSpacing = 0;
 		layout.numColumns = NUM_COLUMNS;			
 		layout.marginHeight = 0;
 		layout.marginWidth = 0; 
 		
 		setLayoutManager(layout);
 		setBorder(new UmlClassBorderTest(ColorFactory.classBorder));
 		
 		this.umlClass = umlClass;		
 		populate(currentLevel);
 	}
 	
 	protected void paintFigure(Graphics g) {
 		super.paintFigure(g);
 		
 		g.setForegroundColor(ColorFactory.classGradientBackground);
 		Rectangle r = new Rectangle(getBounds());
 		r.setSize(r.getSize().width, 20);
 		g.fillGradient(r, true);
 
 	}
 	
 	public void populate(LEVEL level) {
 		// Remove all figures from the container
 		removeAll();
 		
 		currentLevel = level;
 				
 		// Create the class title
 		Label classLabel = new Label(umlClass.getShortName(), ImageFactory.getImage(Icons.CLASS));
 		classLabel.setFont(FontFactory.classTitleBold);
 		classLabel.setTextAlignment(PositionConstants.BOTTOM);
 		classLabel.setIconAlignment(PositionConstants.BOTTOM);
 		classLabel.setBorder(new MarginBorder(6,2,0,2));
 		add(classLabel);
 		fig2entityMap.put(classLabel, umlClass);
 			
 		if (level != LEVEL.ONE) {
 			LighthouseModel model = LighthouseModel.getInstance();
 
 			// Populate class events
 			populateEvents(this, model.getEvents(umlClass));
 
 			if (level == LEVEL.FOUR || level == LEVEL.THREE) {
 				// Insert class separator
 				insertSeparator(this);
 				
 				LinkedList<LighthouseField> fields = new LinkedList<LighthouseField>();
 				LinkedList<LighthouseMethod> methods = new LinkedList<LighthouseMethod>();
 				
 				Collection<LighthouseEntity> ma = model.getMethodsAndAttributesFromClass(umlClass);
 				for (LighthouseEntity e : ma) {
 					if (e instanceof LighthouseField) {
 						fields.add((LighthouseField)e);
 					} else if (e instanceof LighthouseMethod){
 						methods.add((LighthouseMethod)e);
 					}
 				}
 				
 				
 				// Populate class fields
 				//if (fields != null) {
 					populateEntity(this, fields,level);
 				//}
 
 				// Insert class separator
 				insertSeparator(this);
 
 				// Populate methods
 				//if (methods != null) {
 					populateEntity(this, methods,level);
 				//}
 			}
 		}
 	}
 	
 	public LighthouseEntity findLighthouseEntityAt(int x, int y){
 		LighthouseEntity result = null;
 		IFigure fig = findFigureAt(x, y);
 		if (fig != null){
 			result = fig2entityMap.get(fig);
 		}
 		return result;
 	}
 
 	
 	private void insertSeparator(IFigure fig) {
 		Label label = null;
 		for (int i=0; i<NUM_COLUMNS; i++) {
 			GridData data = new GridData();
 			data.heightHint = 10;
 			label = new Label();
 			fig.getLayoutManager().setConstraint(label, data);
 			fig.add(label);			
 		}
 		if (label != null){
 			separators.add(label);
 		}
 	}
 
 	private void populateEntity(IFigure fig,
 			Collection<? extends LighthouseEntity> entities, LEVEL level) {
 		boolean entered = false;
 		LighthouseEntity[] e = entities.toArray(new LighthouseEntity[0]);
 		LighthouseModel model = LighthouseModel.getInstance();
 		for (int i = 0; i < e.length; i++) {
 			if (!filterEntity(e[i])) {
 				Collection<LighthouseEvent> events = model.getEvents(e[i]);
 				if (level == LEVEL.FOUR
 						|| (level == LEVEL.THREE && events.size() > 0)) {
 
 					// Image label
 					String caption = e[i].getShortName();
					if (caption.trim().contains("<init>")){
 						caption = caption.trim().replace("<init>", umlClass.getShortName());
 					}
 					StyledLabel label = new StyledLabel(caption,
 							getEntityIcon(e[i]));
 					if (isRemoved(e[i])) {
 						label.setForegroundColor(ColorFactory.gray);
 						label.setStrikeout(true);
 					}
 
 					add(label);
 					fig2entityMap.put(label, e[i]);
 					populateEvents(this, events);
 
 					// Events Separator
 					if (level == LEVEL.FOUR) {
 						if (events.size() > 0) {
 							entered = true;
 							if (i != 0)
 								separatorEvents.add(label);
 						} else if (entered) {
 							entered = false;
 							separatorEvents.add(label);
 						}
 					}
 				}
 
 			}
 		}
 	}
 	
 	private boolean isRemoved(LighthouseEntity e) {
 		boolean result = false;
 		Collection<LighthouseEvent> events = LighthouseModel.getInstance().getEvents(e);
 		if (events.size() > 0) {
 			for (LighthouseEvent evt : events) {
 				if (evt.getType() == LighthouseEvent.TYPE.REMOVE) {
 					result = true;
 				} else {
 					result = false;
 				}				
 				/*else if (evt.getType() != LighthouseEvent.TYPE.OK_TO_REMOVE) {
 					result = false;
 				}*/
 			}
 		}
 		return result;
 	}
 
 	private void populateEvents(IFigure fig, Collection<LighthouseEvent> events) {
 		if (events.size() > 0) {
 			LighthouseEvent[] evts = events.toArray(new LighthouseEvent[0]);
 			Label label = null;
 
 			for (int i = 0; i < evts.length; i++) {
 				if (!filterEvent(evts[i])) {
 					label = new Label(evts[i].getAuthor().getName(),
 							getEventIcon(evts[i]));
 					label.setBorder(new MarginBorder(0, 4, 0, 2));
 					fig.add(label);
 
 					label = new Label();
 					fig.add(label);
 				}
 			}
 			if (label != null){
 				fig.remove(label);
 			}
 		} else {
 			fig.add(new Label());
 		}
 	}
 	
 	private Image getEntityIcon(LighthouseEntity e){
 		ImageDescriptor descriptor = null;
 		UtilModifiers modifier = UtilModifiers.getVisibility(e);
 		if (e instanceof LighthouseField) {
 			if (modifier == UtilModifiers.PUBLIC) {
 				descriptor = JavaUI.getSharedImages().getImageDescriptor(
 						ISharedImages.IMG_FIELD_PUBLIC);
 			} else if (modifier == UtilModifiers.PRIVATE) {
 				descriptor = JavaUI.getSharedImages().getImageDescriptor(
 						ISharedImages.IMG_FIELD_PRIVATE);
 			} else if (modifier == UtilModifiers.PROTECTED) {
 				descriptor = JavaUI.getSharedImages().getImageDescriptor(
 						ISharedImages.IMG_FIELD_PROTECTED);
 			} else if (modifier == UtilModifiers.DEFAULT) {
 				descriptor = JavaUI.getSharedImages().getImageDescriptor(
 						ISharedImages.IMG_FIELD_DEFAULT);
 			}
 		} else if (e instanceof LighthouseMethod) {
 			if (modifier == UtilModifiers.PUBLIC) {
 				descriptor = JavaUI.getSharedImages().getImageDescriptor(
 						ISharedImages.IMG_OBJS_PUBLIC);
 			} else if (modifier == UtilModifiers.PRIVATE) {
 				descriptor = JavaUI.getSharedImages().getImageDescriptor(
 						ISharedImages.IMG_OBJS_PRIVATE);
 			} else if (modifier == UtilModifiers.PROTECTED) {
 				descriptor = JavaUI.getSharedImages().getImageDescriptor(
 						ISharedImages.IMG_OBJS_PROTECTED);
 			} else if (modifier == UtilModifiers.DEFAULT) {
 				descriptor = JavaUI.getSharedImages().getImageDescriptor(
 						ISharedImages.IMG_OBJS_DEFAULT);
 			}
 		}
 		
 		Collection<UtilModifiers> modifiers = UtilModifiers.getModifiers(e);
 		
 		int flags = 0;
 		if (modifiers.contains(UtilModifiers.STATIC)){
 			flags |= JavaElementImageDescriptor.STATIC;			
 		}
 		if (modifiers.contains(UtilModifiers.SYNCHRONIZED)){
 			flags |= JavaElementImageDescriptor.SYNCHRONIZED;
 		}
 		if (modifiers.contains(UtilModifiers.FINAL)){
 			flags |= JavaElementImageDescriptor.FINAL;
 		}
 		descriptor= new JavaElementImageDescriptor(descriptor, flags, JavaElementImageProvider.BIG_SIZE);
 		return JavaPlugin.getImageDescriptorRegistry().get(descriptor);
 		
 //		ImageDescriptor descriptor = null;
 //		LighthouseModelManager manager = new LighthouseModelManager(LighthouseModel.getInstance());
 //		if (e instanceof LighthouseField) {
 //			if (manager.isPublic(e)) {
 //				descriptor = JavaUI.getSharedImages().getImageDescriptor(ISharedImages.IMG_FIELD_PUBLIC);
 //			} else if (manager.isPrivate(e)) {
 //				descriptor = JavaUI.getSharedImages().getImageDescriptor(ISharedImages.IMG_FIELD_PRIVATE);
 //			} else if (manager.isProtected(e)) {
 //				descriptor = JavaUI.getSharedImages().getImageDescriptor(ISharedImages.IMG_FIELD_PROTECTED);
 //			} else {
 //				descriptor = JavaUI.getSharedImages().getImageDescriptor(ISharedImages.IMG_FIELD_DEFAULT);
 //			}
 //		} else if (e instanceof LighthouseMethod) {
 //			if (manager.isPublic(e)) {
 //				descriptor = JavaUI.getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_PUBLIC);
 //			} else if (manager.isPrivate(e)) {
 //				descriptor = JavaUI.getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_PRIVATE);
 //			} else if (manager.isProtected(e)) {
 //				descriptor = JavaUI.getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_PROTECTED);
 //			} else {
 //				descriptor = JavaUI.getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_DEFAULT);
 //			}
 //		}
 //		int flags = 0;
 //		if (manager.isStatic(e)){
 //			flags |= JavaElementImageDescriptor.STATIC;			
 //		}
 //		if (manager.isSynchronized(e)){
 //			flags |= JavaElementImageDescriptor.SYNCHRONIZED;
 //		}
 //		if (manager.isFinal(e)){
 //			flags |= JavaElementImageDescriptor.FINAL;
 //		}
 //		descriptor= new JavaElementImageDescriptor(descriptor, flags, JavaElementImageProvider.BIG_SIZE);
 //		return JavaPlugin.getImageDescriptorRegistry().get(descriptor);
 	}
 	
 	private Image getEventIcon(LighthouseEvent ev){
 		Image result = null;
 		switch (ev.getType()){
 		case ADD:
 			result = ImageFactory.getImage(Icons.EVENT_ADD);
 			break;
 		case REMOVE:
 			result = ImageFactory.getImage(Icons.EVENT_REMOVE);
 			break;
 		case MODIFY:
 			result = ImageFactory.getImage(Icons.EVENT_MODIFY);
 			break;	
 		//case CUSTOM:
 //			if (ev instanceof InfluenceEvent){
 //				InfluenceEvent ievt = (InfluenceEvent) ev;
 //				result = ImageFactory.getImage(ievt.getIcon());
 //			}
 			//break;
 		}
 
 		if (ev.isCommitted()){
 			ImageDescriptor desc = AbstractUIPlugin.imageDescriptorFromPlugin(LighthouseUIPlugin.PLUGIN_ID, "/icons/commit.gif");
 			DecorationOverlayIcon overlayIcon = new DecorationOverlayIcon(result,desc,IDecoration.BOTTOM_RIGHT);
 			result = overlayIcon.createImage();
 		}
 		
 		return result;
 	}
 	
 	protected boolean filterEntity(LighthouseEntity entity) {
 		// TODO Optimize the algorithm
 		LighthouseModel model = LighthouseModel.getInstance();
 		IClassFilter[] filters = FilterManager.getInstance().getClassFilters();
 		boolean selected = false;
 		if (filters.length > 0) {
 			for (IClassFilter filter : filters) {
 				selected |= filter.select(model, entity);
 			}
 			if (!selected) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	protected boolean filterEvent(LighthouseEvent event) {
 		// TODO Optimize the algorithm
 		LighthouseModel model = LighthouseModel.getInstance();
 		IClassFilter[] filters = FilterManager.getInstance().getClassFilters();
 		boolean selected = false;
 		if (filters.length > 0) {
 			for (IClassFilter filter : filters) {
 				selected |= filter.select(model, event);
 			}
 			if (!selected) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.draw2d.Figure#removeAll()
 	 */
 	@Override
 	public void removeAll() {
 		super.removeAll();
 		separators.clear();
 		separatorEvents.clear();
 		fig2entityMap.clear();
 	}
 
 	/**
 	 * @return the separators
 	 */
 	Collection<IFigure> getSeparators() {
 		return separators;
 	}
 	
 	/**
 	 * @return the separators
 	 */
 	Collection<IFigure> getSeparatorEvents() {
 		return separatorEvents;
 	}
 
 	/**
 	 * @return the currentLevel
 	 */
 	public LEVEL getCurrentLevel() {
 		return currentLevel;
 	}
 }
