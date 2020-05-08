 package org.jboss.tools.bpmn2.reddeer;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.eclipse.draw2d.IFigure;
 import org.eclipse.draw2d.geometry.Rectangle;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.gef.EditPart;
 import org.eclipse.gef.GraphicalEditPart;
 import org.eclipse.gef.GraphicalViewer;
 import org.eclipse.graphiti.mm.pictograms.Diagram;
 import org.eclipse.graphiti.mm.pictograms.PictogramLink;
 import org.eclipse.graphiti.mm.pictograms.Shape;
 import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
 import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditPart;
 import org.eclipse.swtbot.eclipse.gef.finder.widgets.SWTBotGefEditor;
 import org.eclipse.ui.IEditorPart;
 import org.hamcrest.Matcher;
 import org.jboss.reddeer.swt.condition.WaitCondition;
 import org.jboss.reddeer.swt.exception.SWTLayerException;
 import org.jboss.reddeer.swt.impl.button.CheckBox;
 import org.jboss.reddeer.swt.impl.button.PushButton;
 import org.jboss.reddeer.swt.impl.ctab.DefaultCTabItem;
 import org.jboss.reddeer.swt.impl.menu.ContextMenu;
 import org.jboss.reddeer.swt.impl.shell.DefaultShell;
 import org.jboss.reddeer.swt.impl.styledtext.DefaultStyledText;
 import org.jboss.reddeer.swt.util.Display;
 import org.jboss.reddeer.swt.util.ResultRunnable;
 import org.jboss.reddeer.swt.wait.WaitWhile;
 import org.jboss.tools.bpmn2.reddeer.editor.Element;
 import org.jboss.tools.bpmn2.reddeer.editor.ElementType;
 import org.jboss.tools.bpmn2.reddeer.editor.matcher.ConstructOfType;
 import org.jboss.tools.bpmn2.reddeer.editor.matcher.ConstructWithName;
 
 /**
  * 
  */
 public class ProcessEditorView extends SWTBotGefEditor {
 
 	private static final int SAVE_SLEEP_TIME = 4000;
 	
 	private static Logger log = Logger.getLogger(ProcessEditorView.class);
 	
 	private static SWTWorkbenchBot bot = new SWTWorkbenchBot();
 	
 	private DefaultPalette palette;
 	
 	/**
 	 * 
 	 */
 	public ProcessEditorView() {
 		super(bot.activeEditor().getReference(), bot);
 		palette = new DefaultPalette(getGraphicalViewer().getEditDomain().getPaletteViewer());
 	}
 	
 	/**
 	 * 
 	 * @param title
 	 */
 	public ProcessEditorView(String title) {
 		super(bot.editorByTitle(title).getReference(), bot);
 		palette = new DefaultPalette(getGraphicalViewer().getEditDomain().getPaletteViewer());
 	}
 	
 	/**
 	 * 
 	 * @param section
 	 * @param label
 	 */
 	public void activateTool(String section, String label) {
 		palette.activateTool(section, label);
 	}
 	
 	/**
 	 * Get the process being created in this editor without 
 	 * selecting it.
 	 * 
 	 * @return
 	 */
 	public Element getProcess() {
 		return getProcess(false);
 	}
 	
 	/**
 	 * 
 	 * @param select
 	 * @return
 	 */
 	public Element getProcess(boolean select) {
 		return new Element(null, ElementType.PROCESS, null, 0, select);
 	}
 	
 	/**
 	 * 
 	 * @param editPart
 	 * @param byLabel
 	 */
 	public void selectEditPart(SWTBotGefEditPart editPart) {
 		select(editPart);
 		setFocus();
 	}
 
 	/**
 	 * 
 	 * @return
 	 */
 	public List<SWTBotGefEditPart> getSelectedEditParts() {
 		return selectedEditParts();
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public SWTBotGefEditPart getSelectedEditPart() {
 		List<SWTBotGefEditPart> selectedParts = selectedEditParts();
 		if (!selectedParts.isEmpty()) {
 			return selectedParts.get(0);
 		}
 		return null;
 	}
 	
 	/**
 	 * 
 	 * @param matcher
 	 * @param index
 	 * @return
 	 */
 	public SWTBotGefEditPart getEditPart(Matcher<? extends EditPart> matcher, int index) {
 		List<SWTBotGefEditPart> parts = editParts(matcher);
 		return parts.isEmpty() ? null : parts.get(index);
 	}
 
 	/**
 	 * 
 	 * @param matcher
 	 * @return
 	 */
 	public List<SWTBotGefEditPart> getEditParts(Matcher<? extends EditPart> matcher) {
 		return editParts(matcher);
 	}
 	
 	/**
 	 * 
 	 * @param label
 	 */
 	public List<SWTBotGefEditPart> getEditParts(String label) {
 		return editParts(new ConstructWithName<EditPart>(label));
 	}
 	
 	/**
 	 * 
 	 * @param label
 	 */
 	public SWTBotGefEditPart getEditPart(String label) {
 		List<SWTBotGefEditPart> partList = getEditParts(label);
 		if (!partList.isEmpty()) {
 			return partList.get(0);
 		}
 		return null;
 	}
 
 	/**
 	 * 
 	 * @param editPart
 	 * @param matcher
 	 * @return
 	 */
 	public List<SWTBotGefEditPart> getEditParts(SWTBotGefEditPart editPart, Matcher<? extends EditPart> matcher) {
 		return editPart.descendants(matcher);
 	}
 	
 	/**
 	 * 
 	 * @param editPart
 	 * @return
 	 */
 	public Rectangle getBounds(SWTBotGefEditPart editPart) {
 		return getBounds((GraphicalEditPart) editPart.part());
 	}
 	
 	/**
 	 * 
 	 * @param part
 	 * @return
 	 */
 	public Rectangle getBounds(GraphicalEditPart part) {
 		IFigure figure = part.getFigure();
 		Rectangle bounds = figure.getBounds().getCopy();
 		figure.translateToAbsolute(bounds);
 		return bounds;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public String getName(SWTBotGefEditPart editPart) {
 		Object model = editPart.part().getModel();
 		if (model instanceof Diagram) {
 			return ((Diagram) model).getName();
 		} else if (model instanceof Shape) {
 			Shape shape = (Shape) model;
 			PictogramLink link = shape.getLink();
 			if (link != null) {
 				EList<EObject> objectList = link.getBusinessObjects();
 				for (EObject eo : objectList) {
 					try {
 						Method method = eo.getClass().getMethod("getName");
 						String name = method.invoke(eo).toString();
 						return name;
 					} catch (Exception e) {
 						throw new RuntimeException("Failed to resolve name for editPart '" + editPart + "'", e);
 					}
 				}
 			}
 			throw new RuntimeException("Failed to resolve name for editPart '" + editPart + "' - Pictogram link is null.");
 		} else {
 			throw new UnsupportedOperationException("Unsupported object type: " + editPart);
 		}
 	}
 	
 	/**
 	 * 
 	 * @param label
 	 */
 	public void selectConstruct(String label) {
 		SWTBotGefEditPart editPart = getEditPart(label);
 		if (editPart == null) {
 			throw new RuntimeException("Cannot find '" + label + "'");
 		}
 		selectEditPart(editPart);
 		log.info("Selected construct '" + label + "'");
 	}
 
 	/**
 	 * 
 	 * @param matcher
 	 * @param index
 	 */
 	public void selectConstruct(Matcher<? extends EditPart> matcher, int index) {
 		selectEditPart(getEditPart(matcher, index));
 	}
 	
 	/**
 	 * 
 	 * @param constructType
 	 * @return
 	 */
 	public List<Element> getConstructs(ElementType constructType) {
 		log.info("Searching for constructs of type '" + constructType.toToolName() + "'");
 		List<Element> constructList = new ArrayList<Element>();
 		List<SWTBotGefEditPart> editPartList = getEditParts(new ConstructOfType<EditPart>(constructType));
 		for (SWTBotGefEditPart editPart : editPartList) {
 			constructList.add(new Element(getName(editPart), constructType, null, 0, false));
 		}
 		return constructList;
 	}
 	
 	/**
 	 * 
 	 * @param connectionType
 	 * @return
 	 */
 	public Element getLastConstruct(ElementType constructType) {
 		List<Element> constructList = getConstructs(constructType);
 		if (!constructList.isEmpty()) {
 			return constructList.get(constructList.size() - 1);
 		}
 		return null;
 	}
 	
 	/**
 	 * @return xml source code of opened process
 	 */
 	public String getSourceText() {
 		new WaitWhile(new SourceCodeIsNotShown());
 		
 		DefaultStyledText styled = new DefaultStyledText();
 		String text = styled.getText();
 		
 		new DefaultCTabItem("Source").close();
 		
 		return text;
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	protected GraphicalViewer getGraphicalViewer() {
 		GraphicalViewer viewer = Display.syncExec(new ResultRunnable<GraphicalViewer>() {
 			@Override
 			public GraphicalViewer run() {
 				final IEditorPart editor = partReference.getEditor(true);
 				return (GraphicalViewer) editor.getAdapter(GraphicalViewer.class);
 			}
 		});
 		
 		return viewer;
 	}
 	
 	/**
 	 * Sometimes generated IDs are not unique. Fix it!
 	 */
 	protected void repairProcessModel() {
 		try {
 			new DefaultShell("Selection Needed").setFocus();
 			new PushButton("Select All").click();
 			new PushButton("OK").click();
 		} catch (SWTLayerException e) {
 			log.warn(e.getMessage());
 		}
 	}
 	
 	/**
 	 * 
 	 * @param on
 	 */
 	protected void projectNature(boolean on) {
 		try {
 			log.info("Handling 'BPMN2 Project Nature'. Value '" + on + "'");
 			Thread.sleep(SAVE_SLEEP_TIME);
 			new DefaultShell("Configure BPMN2 Project Nature").setFocus();
 			new CheckBox().click();
 			new PushButton(on ? "Yes": "No").click();
 		} catch (InterruptedException e) {
 			// ignore - no worries
 		} catch (SWTLayerException e) {
 			log.warn(e.getMessage());
 		}
 	}
 	
 	/**
 	 * @see org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor#save()
 	 */
 	@Override
 	public void save() {
 		bot.menu("File").menu("Save").click();
 		repairProcessModel();
 		projectNature(true);
 	}
 	
 	private class SourceCodeIsNotShown implements WaitCondition {
 
 		@Override
 		public boolean test() {
 			try{
				new ContextMenu("Show Source View").select();
 			} catch(SWTLayerException e) {
 				return true;
 			}
 			return false;
 		}
 
 		@Override
 		public String description() {
 			return "Wait while source code is not shown";
 		}
 		
 	}
 
 }
