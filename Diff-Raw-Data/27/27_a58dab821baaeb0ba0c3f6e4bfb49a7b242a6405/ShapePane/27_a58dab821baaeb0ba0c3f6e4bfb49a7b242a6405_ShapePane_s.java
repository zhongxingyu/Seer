 package org.pathwayeditor.visualeditor;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.RenderingHints;
 import java.awt.geom.AffineTransform;
 import java.util.Iterator;
 
 import javax.swing.JPanel;
 
 import org.pathwayeditor.figure.geometry.Envelope;
 import org.pathwayeditor.graphicsengine.CanvasDrawer;
 import org.pathwayeditor.graphicsengine.ICanvasDrawer;
 import org.pathwayeditor.visualeditor.controller.IViewControllerStore;
 import org.pathwayeditor.visualeditor.feedback.IFeedbackModel;
 import org.pathwayeditor.visualeditor.selection.ILinkSelection;
 import org.pathwayeditor.visualeditor.selection.INodeSelection;
 import org.pathwayeditor.visualeditor.selection.ISelectionRecord;
 
 public class ShapePane extends JPanel implements IShapePane {
 	private static final long serialVersionUID = -7580080598416351849L;
 
 	private final double PANE_BORDER = 20.0;
 	private final ISelectionRecord selections;
 	private final ICanvasDrawer canvasDrawer;
 	private final IFeedbackDrawer feedbackDrawer;
 	private AffineTransform lastTransform;
 
 	private Envelope canvasBounds;
 	
 	public ShapePane(IViewControllerStore geomFactory, ISelectionRecord selectionRecord, IFeedbackModel feedbackModel){
 		super();
 		this.selections = selectionRecord;
 		this.canvasDrawer = new CanvasDrawer(geomFactory);
 		this.feedbackDrawer = new FeedbackDrawer(feedbackModel);
 	}
 	
 	@Override
 	public void paintComponent(Graphics g){
 		super.paintComponent(g);
 		Graphics2D g2d = (Graphics2D)g;
 		Envelope bounds = this.canvasDrawer.getViewControllerStore().getCanvasBounds();
 		AffineTransform originalTransform = g2d.getTransform();
 		g2d.translate(-bounds.getOrigin().getX()+PANE_BORDER, -bounds.getOrigin().getY()+PANE_BORDER);
 		this.lastTransform = g2d.getTransform();
 		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
 		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
 		this.canvasDrawer.paint(g2d);
 		paintSelections(g2d);
 		this.feedbackDrawer.paint(g2d);
 		g2d.setTransform(originalTransform);
 	}
 
 	private void paintSelections(Graphics2D g2d) {
 		Iterator<INodeSelection> selectionIter = this.selections.selectedNodesIterator();
 		while(selectionIter.hasNext()){
 			INodeSelection node = selectionIter.next();
 			SelectionShape selection = new SelectionShape(node);
 			selection.paint(g2d);
 		}
 		Iterator<ILinkSelection> linkSelectionIter = this.selections.selectedLinksIterator();
 		while(linkSelectionIter.hasNext()){
 			ILinkSelection link = linkSelectionIter.next();
 			SelectionLinkDrawer selection = new SelectionLinkDrawer(link.getPrimitiveController());
 			selection.paint(g2d);
 		}
 	}
 
 
 	@Override
 	public ISelectionRecord getSelectionRecord() {
 		return this.selections;
 	}
 
 	@Override
 	public AffineTransform getLastUsedTransform(){
 		return this.lastTransform;
 	}
 
 
 	@Override
 	public IViewControllerStore getViewModel() {
 		return this.canvasDrawer.getViewControllerStore();
 	}
 
 
 
 	@Override
 	public IFeedbackModel getFeedbackModel() {
 		return this.feedbackDrawer.getFeedbackModel();
 	}
 
 	@Override
 	public void updateView() {
 		canvasBounds = canvasDrawer.getViewControllerStore().getCanvasBounds();
 		Dimension prefSize = new Dimension();
 		prefSize.setSize(canvasBounds.getDimension().getWidth()+2*PANE_BORDER, canvasBounds.getDimension().getHeight() + 2*PANE_BORDER);
 		this.setPreferredSize(prefSize);
 		revalidate();
 		repaint();
 	}
 }
