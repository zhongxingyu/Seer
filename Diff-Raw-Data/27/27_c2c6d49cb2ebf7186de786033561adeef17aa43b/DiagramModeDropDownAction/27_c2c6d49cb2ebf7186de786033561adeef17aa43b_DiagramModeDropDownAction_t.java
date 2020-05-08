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
	private static final String DESCRIPTION = "Class visualization modes";
 
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
			super("",Action.AS_RADIO_BUTTON);
			this.mode = mode;	
			
 			String modeName = "";
			switch (mode.ordinal()) {
 			case 0:
				modeName = "Only class name";
 				break;
 			case 1:
				modeName = "Class/Authors's name";
 				break;
 			case 2:
				modeName = "Modified fields/methods";
 				break;
 			case 3:
				modeName = "All fields/methods";
 				break;
 			}
 			setText(modeName);
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
