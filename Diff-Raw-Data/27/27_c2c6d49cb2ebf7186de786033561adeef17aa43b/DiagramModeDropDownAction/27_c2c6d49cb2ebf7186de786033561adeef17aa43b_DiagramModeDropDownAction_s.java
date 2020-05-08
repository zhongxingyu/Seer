 package edu.uci.lighthouse.ui.views.actions;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.eclipse.draw2d.Animation;
 import org.eclipse.jface.action.Action;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.eclipse.zest.core.widgets.GraphNode;
 import org.eclipse.zest.core.widgets.IContainer;
 
 import edu.uci.lighthouse.ui.LighthouseUIPlugin;
 import edu.uci.lighthouse.ui.figures.ILighthouseClassFigure;
 import edu.uci.lighthouse.ui.utils.GraphUtils;
 
 public class DiagramModeDropDownAction extends DropDownAction{
 
 	private static Logger logger = Logger.getLogger(DiagramModeDropDownAction.class);
 	
 	private static final String ICON = "/icons/class_mode.png";
	private static final String DESCRIPTION = "Class visualization mode";
 
 	public DiagramModeDropDownAction(IContainer container) {
 		super(container);
 	}
 	
 	@Override
 	protected List<IAction> createActions() {
 		List<IAction> result = new ArrayList<IAction>();
 		result.add(new DiagramModeAction(ILighthouseClassFigure.MODE.ONE));
 		result.add(new DiagramModeAction(ILighthouseClassFigure.MODE.TWO));
 		result.add(new DiagramModeAction(ILighthouseClassFigure.MODE.THREE));
 		result.add(new DiagramModeAction(ILighthouseClassFigure.MODE.FOUR));
 		return result;
 	}
 	
 	private final class DiagramModeAction extends Action {
 		ILighthouseClassFigure.MODE mode;
 		public DiagramModeAction(ILighthouseClassFigure.MODE mode){
			super("Mode " + (mode.ordinal()+1),Action.AS_RADIO_BUTTON);
 			String modeName = "";
			int key = mode.ordinal();
			switch (key ) {
 			case 0:
				modeName = "1. Only Class Name";
 				break;
 			case 1:
				modeName = "2. Class/Authors's Name";
 				break;
 			case 2:
				modeName = "3. Modified Fields/Methods";
 				break;
 			case 3:
				modeName = "4. All Fields/Methods";
 				break;
 			}
 			setText(modeName);
			this.mode = mode;			
 		}
 		@Override
 		public void run() {
 			if (isChecked()) {
 				logger.debug(getText() + " running...");
 				refreshAllFigures(mode);
 				selectedAction = this;
 			}
 		}
 	}
 
 	@Override
 	protected int getDefaultActionIndex() {
 		return 0;
 	}
 
 	@Override
 	protected void init() {
 		setToolTipText(DESCRIPTION);
 		setImageDescriptor(AbstractUIPlugin
 				.imageDescriptorFromPlugin(LighthouseUIPlugin.PLUGIN_ID, ICON));
 	}
 	
 	private void refreshAllFigures(ILighthouseClassFigure.MODE mode){
 		Animation.markBegin();
 		for (Iterator itNodes = container.getNodes().iterator(); itNodes.hasNext();) {
 			GraphNode node = (GraphNode) itNodes.next();
 			GraphUtils.changeFigureMode(node,mode);
 /*			if (node instanceof UmlClassNode){	
 				UmlClassNode classNode = (UmlClassNode) node;
 				classNode.setLevel(level);
 			}*/
 		}
 		Animation.run(150);
 	}
 }
