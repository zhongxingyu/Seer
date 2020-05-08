 package newgui.gui.display.primaryDisplay.loggerVizualizer;
 
 import java.awt.BorderLayout;
 import java.util.List;
 
 import javax.swing.JCheckBox;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import newgui.UIConstants;
 
 import logging.ConsensusTreeLogger;
 
 import gui.figure.TextElement;
 import gui.figure.treeFigure.DrawableNode;
 import gui.figure.treeFigure.DrawableTree;
 import gui.figure.treeFigure.SquareTree;
 import gui.figure.treeFigure.TreeElement;
 import gui.figure.treeFigure.TreeFigure;
 
 /**
  * Visualizer for consensus tree logger. Uses TreeFigure to draw a tree 
  * @author brendano
  *
  */
 public class ConsensusTreeViz extends AbstractLoggerViz {
 
 	@Override
 	public void initialize() {
 		this.remove(seriesFig);
 		treeLogger = (ConsensusTreeLogger)logger;
 		treeFig = new TreeFigure();
 		add(treeFig, BorderLayout.CENTER);
 		
 		burninMessage = new TextElement("(Burnin period not exceeded)", seriesFig);
 		burninMessage.setPosition(0.45, 0.5);
 		treeFig.addElement(burninMessage);
 		
 		final JCheckBox showErrorBarsBox = new JCheckBox("Error bars");
 		showErrorBarsBox.setFont(UIConstants.sansFont);
 		showErrorBarsBox.setSelected(true);
 		showErrorBarsBox.addChangeListener(new ChangeListener() {
 
 			@Override
 			public void stateChanged(ChangeEvent e) {
 				showErrorBars = showErrorBarsBox.isSelected();
 				update();
 			}
 		});
 		super.addOptionsComponent(showErrorBarsBox);
 		
 		
 		
 		final JCheckBox showSupportBox = new JCheckBox("Node support");
 		showSupportBox.setFont(UIConstants.sansFont);
 		showSupportBox.setSelected(false);
 		showSupportBox.addChangeListener(new ChangeListener() {
 
 			@Override
 			public void stateChanged(ChangeEvent e) {
 				showNodeSupport = showSupportBox.isSelected();
 				update();
 			}
 		});
 		super.addOptionsComponent(showSupportBox);
 	}
 
 	protected int getUpdateFrequency() {
 		return 1000;
 	}
 	
 	@Override
 	public String getDataString() {
 		List<DrawableTree> trees = treeFig.getTrees();
 		StringBuilder str = new StringBuilder();
 		String lineSep = System.getProperty("line.separator");
 		for(DrawableTree tree : trees) {
 			str.append(tree.getNewick() + lineSep);
 		}
 		return str.toString();
 	}
 	
 	@Override
 	public void update() {
 		if (burninMessage != null && logger.getBurninExceeded()) {
 			treeFig.removeElement(burninMessage);
 			burninMessage = null;
 		}
 		if (logger.getBurninExceeded() && treeLogger.getTreesTabulated()>0) {
 			String consNewick = treeLogger.getSummaryString();
 			
 			SquareTree drawableTree = new SquareTree(consNewick);
 			treeFig.removeAllTrees();
 			treeFig.addTree(drawableTree);
 			if (treeFig.getScaleType()==DrawableTree.NO_SCALE_BAR)
 				treeFig.setScaleType(DrawableTree.SCALE_AXIS);
 			
 			List<TreeElement> elems =  treeFig.getTreeElements();
 			for(TreeElement el : elems) {
 				el.getTreeDrawer().setShowErrorBars(showErrorBars);
 				
 				if (showNodeSupport) {
 					List<DrawableNode> internalNodes = drawableTree.getAllInternalDrawableNodes();
 					for(DrawableNode node : internalNodes) {
 						node.setLabelPosition(TreeFigure.UPPER_LEFT_POSITION);
 						String label = node.getAnnotationValue("support");
 						if (label != null)
 							label = label.substring(0, Math.min(label.length(), 4));
 						node.setCurrentLabel( label );
 					}
 					
 				}
 			}
 			treeFig.repaint();
 		}
 		repaint();
 	}
 
 	private TextElement burninMessage;
 	private ConsensusTreeLogger treeLogger;
 	private TreeFigure treeFig;
 	private boolean showErrorBars = true;
 	private boolean showNodeSupport = false;
 
 }
