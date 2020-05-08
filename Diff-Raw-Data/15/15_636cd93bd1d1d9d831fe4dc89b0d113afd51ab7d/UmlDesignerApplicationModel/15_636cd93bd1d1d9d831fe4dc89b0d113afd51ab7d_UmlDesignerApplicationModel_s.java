 package ui;
 
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.ActionMap;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 import javax.swing.JToolBar;
 
 import org.jhotdraw.app.Application;
 import org.jhotdraw.app.DefaultApplicationModel;
 import org.jhotdraw.app.DefaultMenuBuilder;
 import org.jhotdraw.app.MenuBuilder;
 import org.jhotdraw.app.View;
 import org.jhotdraw.app.action.ActionUtil;
 import org.jhotdraw.app.action.file.ExportFileAction;
 import org.jhotdraw.app.action.view.ToggleViewPropertyAction;
 import org.jhotdraw.app.action.view.ViewPropertyAction;
 import org.jhotdraw.draw.AttributeKey;
 import org.jhotdraw.draw.AttributeKeys;
 import org.jhotdraw.draw.DefaultDrawingEditor;
 import org.jhotdraw.draw.Drawing;
 import org.jhotdraw.draw.DrawingEditor;
 import org.jhotdraw.draw.DrawingView;
 import org.jhotdraw.draw.Figure;
 import org.jhotdraw.draw.TextAreaFigure;
 import org.jhotdraw.draw.action.ButtonFactory;
 import org.jhotdraw.draw.io.DOMStorableInputOutputFormat;
 import org.jhotdraw.draw.tool.ConnectionTool;
 import org.jhotdraw.draw.tool.CreationTool;
 import org.jhotdraw.draw.tool.TextAreaCreationTool;
 import org.jhotdraw.draw.tool.Tool;
 import org.jhotdraw.util.ResourceBundleUtil;
 
 import domain.UmlDesignerFactory;
 
 import ui.UmlClass.AssociationFigure;
 import ui.UmlClass.UmlClassFigure;
 
 import edu.umd.cs.findbugs.annotations.Nullable;
 
 @SuppressWarnings("serial")
 public class UmlDesignerApplicationModel extends DefaultApplicationModel {
 	private final static double[] scaleFactors = { 5, 4, 3, 2, 1.5, 1.25, 1,
 			0.75, 0.5, 0.25, 0.10 };
 	private DefaultDrawingEditor sharedEditor;
 	private HashMap<String, Action> actions;
 
 	private static class ToolButtonListener implements ItemListener {
 		private Tool tool;
 		private DrawingEditor editor;
 
 		public ToolButtonListener(Tool t, DrawingEditor editor) {
 			this.tool = t;
 			this.editor = editor;
 		}
 
 		@Override
 		public void itemStateChanged(ItemEvent evt) {
 			if (evt.getStateChange() == ItemEvent.SELECTED) {
 				editor.setTool(tool);
 			}
 		}
 	}
 
 	public UmlDesignerApplicationModel() {
 	}
 
 	@Override
 	public void initView(Application a, @Nullable View v) {
 		if (a.isSharingToolsAmongViews()) {
 			((UmlDesignerView) v).setEditor(getSharedEditor());
 		}
 	}
 
 	@Override
 	public ActionMap createActionMap(Application a, @Nullable View v) {
 		ActionMap m = super.createActionMap(a, v);
 		ResourceBundleUtil drawLabels = ResourceBundleUtil
 				.getBundle("org.jhotdraw.draw.Labels");
 		ResourceBundleUtil ourLabels = ResourceBundleUtil
 				.getBundle("ui.UmlClass.Labels");
 		AbstractAction aa;
 		m.put(ExportFileAction.ID, new ExportFileAction(a, v));
 		m.put("view.toggleGrid", aa = new ToggleViewPropertyAction(a, v,
 				UmlDesignerView.GRID_VISIBLE_PROPERTY));
 		drawLabels.configureAction(aa, "view.toggleGrid");
 		for (double sf : scaleFactors) {
 			m.put((int) (sf * 100) + "%", aa = new ViewPropertyAction(a, v,
 					DrawingView.SCALE_FACTOR_PROPERTY, Double.TYPE, new Double(
 							sf)));
 			aa.putValue(Action.NAME, (int) (sf * 100) + " %");
 		}
 
 		m.put("edit.observerPattern", aa = new AbstractAction() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				//
 				DOMStorableInputOutputFormat dom = new DOMStorableInputOutputFormat(
 						new UmlDesignerFactory());
 				try {
 					dom.read(new File("observerPattern.xml"), sharedEditor
 							.getActiveView().getDrawing(), false);
 				} catch (IOException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		});
 		ourLabels.configureAction(aa, "edit.observerPattern");
 
 		m.put("edit.generateSkeletonCodeFile", aa = new AbstractAction() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
 				Drawing d = sharedEditor.getActiveView().getDrawing(); //get the current drawing
				int j = d.getChildCount();
 				
 				//for every figure in the drawing
				for(int i = 0; i < j - 1; i++){
 					//if it is a UmlClassFigure
					if(d.contains(typeof(UmlClassFigure))){
 						//output code
						String output = getModel().toString();
						String fileName = (model.getName() + ".java");
 
 						try {
 
 							PrintWriter outStream = new PrintWriter(fileName);
 
 							outStream.write(output);
 							outStream.close();
 						
 						} catch (FileNotFoundException e1) {
 							e1.printStackTrace();
 						}
 					}
 				}
 
 			}
 
 		});
 		ourLabels.configureAction(aa, "edit.generateSkeletonCodeFile");
 
 		return m;
 	}
 
 	@Override
 	public List<JToolBar> createToolBars(Application a, @Nullable View v) {
 		ResourceBundleUtil drawLabels = ResourceBundleUtil
 				.getBundle("org.jhotdraw.draw.Labels");
 		UmlDesignerView view = (UmlDesignerView) v;
 		DrawingEditor editor;
 		if (view == null) {
 			editor = getSharedEditor();
 		} else {
 			editor = view.getEditor();
 		}
 
 		LinkedList<JToolBar> list = new LinkedList<JToolBar>();
 		JToolBar tb = new JToolBar();
 		addCreationButtonsTo(tb, editor);
 		tb.setName(drawLabels.getString("window.drawToolBar.title"));
 		list.add(tb);
 		tb = new JToolBar();
 		// ButtonFactory.addAttributesButtonsTo(tb, editor);
 		// tb.setName(drawLabels.getString("window.attributesToolBar.title"));
 		// list.add(tb);
 		tb = new JToolBar();
 		ButtonFactory.addAlignmentButtonsTo(tb, editor);
 		tb.setName(drawLabels.getString("window.alignmentToolBar.title"));
 		list.add(tb);
 
 		return list;
 	}
 
 	private void addCreationButtonsTo(JToolBar tb, final DrawingEditor editor) {
 		// AttributeKeys for the entity sets
 		HashMap<AttributeKey, Object> attributes;
 
 		ResourceBundleUtil labels = ResourceBundleUtil
 				.getBundle("ui.UmlClass.Labels");
 		ResourceBundleUtil drawLabels = ResourceBundleUtil
 				.getBundle("org.jhotdraw.draw.Labels");
 
 		ButtonFactory.addSelectionToolTo(tb, editor);
 		tb.addSeparator();
 
 		attributes = new HashMap<AttributeKey, Object>();
 		attributes.put(AttributeKeys.FILL_COLOR, Color.white);
 		attributes.put(AttributeKeys.STROKE_COLOR, Color.black);
 		attributes.put(AttributeKeys.TEXT_COLOR, Color.black);
 		ButtonFactory.addToolTo(tb, editor, new CreationTool(
 				new UmlClassFigure(), attributes), "edit.createUmlClassFigure",
 				labels);
 		ButtonFactory.addToolTo(tb, editor, new ConnectionTool(
 				new AssociationFigure(), attributes), "edit.createAssociation",
 				labels);
 		attributes = new HashMap<AttributeKey, Object>();
 		attributes.put(AttributeKeys.STROKE_COLOR, new Color(0x000099));
 		tb.addSeparator();
 		ButtonFactory.addToolTo(tb, editor, new TextAreaCreationTool(
 				new TextAreaFigure()), "edit.createTextArea", drawLabels);
 
 		// ButtonFactory.addToolTo(tb, editor, new CreationTool(),
 		// "edit.observerPattern", drawLabels)
 	}
 
 	@Override
 	protected MenuBuilder createMenuBuilder() {
 		return new DefaultMenuBuilder() {
 
 			@Override
 			public void addOtherEditItems(JMenu m, Application app,
 					@Nullable View v) {
 				ActionMap am = app.getActionMap(v);
 				JMenuItem item = new JMenuItem(am.get("edit.observerPattern"));
 				item.setAction(am.get("edit.observerPattern"));
 				JMenuItem anotherItem = new JMenuItem(
 						am.get("edit.generateSkeletonCodeFile"));
 				anotherItem.setAction(am.get("edit.generateSkeletonCodeFile"));
 				m.add(item);
 				m.add(anotherItem);
 			}
 
 			@Override
 			public void addOtherViewItems(JMenu m, Application app,
 					@Nullable View v) {
 				ActionMap am = app.getActionMap(v);
 				JCheckBoxMenuItem cbmi;
 				cbmi = new JCheckBoxMenuItem(am.get("view.toggleGrid"));
 				ActionUtil.configureJCheckBoxMenuItem(cbmi,
 						am.get("view.toggleGrid"));
 				m.add(cbmi);
 				JMenu m2 = new JMenu("Zoom");
 				for (double sf : scaleFactors) {
 					String id = (int) (sf * 100) + "%";
 					cbmi = new JCheckBoxMenuItem(am.get(id));
 					ActionUtil.configureJCheckBoxMenuItem(cbmi, am.get(id));
 					m2.add(cbmi);
 				}
 				m.add(m2);
 			}
 		};
 	}
 
 	public DefaultDrawingEditor getSharedEditor() {
 		if (sharedEditor == null) {
 			sharedEditor = new DefaultDrawingEditor();
 		}
 		return sharedEditor;
 	}
 
 }
