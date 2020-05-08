 package org.eclipse.uml2.diagram.common.draw2d;
 
 import org.eclipse.draw2d.BorderLayout;
 import org.eclipse.draw2d.Graphics;
 import org.eclipse.draw2d.Label;
 import org.eclipse.draw2d.MarginBorder;
 import org.eclipse.draw2d.RectangleFigure;
 import org.eclipse.draw2d.Shape;
 import org.eclipse.draw2d.StackLayout;
 import org.eclipse.draw2d.ToolbarLayout;
 import org.eclipse.draw2d.geometry.Rectangle;
 
 
 public class Cube3DFigure extends Shape {
 	public static final int DEFAULT_DEPTH_GAP = 10;
 	
 	private int myDepthGap = DEFAULT_DEPTH_GAP;
 	private final int[] myFillPath = new int[14];
 	private final RectangleFigure myLabelPane;
 	private final RectangleFigure myContentPane;
 	private final Label myNameLabel;
 	private final StereotypeLabel myTypeLabel; 
 	
 	public Cube3DFigure(){
 		setDepthGap(DEFAULT_DEPTH_GAP);
 		
 		setLayoutManager(new StackLayout());
 		RectangleFigure insides = new RectangleFigure();
 		insides.setBorder(new MarginBorder(1, 1, 1, 1));
 		add(insides);
 		insides.setLayoutManager(new BorderLayout());
 		 
 		myLabelPane = invisibleRectangle();
 		ToolbarLayout labelPaneLayout = new ToolbarLayout(false);
 		labelPaneLayout.setMinorAlignment(ToolbarLayout.ALIGN_CENTER);
 		labelPaneLayout.setSpacing(5);
 		myLabelPane.setLayoutManager(labelPaneLayout);
 		
 		myTypeLabel = stereoLabel();
 		myNameLabel = wrapLabel();
 		myLabelPane.add(myTypeLabel);
 		myLabelPane.add(myNameLabel);
 		
 		myContentPane = invisibleRectangle();
 		insides.add(myLabelPane, BorderLayout.TOP);
 		insides.add(myContentPane, BorderLayout.CENTER);
 	}
 	
 	public void setDepthGap(int depthGap) {
 		myDepthGap = depthGap;
 		setBorder(new MarginBorder(myDepthGap, 0, 0, myDepthGap));
 		repaint();
 	}
 	
 	public RectangleFigure getContentPane() {
 		return myContentPane;
 	}
 	
 	public RectangleFigure getNamePane() {
 		return myLabelPane;
 	}
 	
 	public Label getNameLabel() {
 		return myNameLabel;
 	}
 	
 	public StereotypeLabel getTypeLabel() {
 		return myTypeLabel;
 	}
 	
 	public void setTypeLabelVisible(boolean visible){
 		myTypeLabel.setVisible(visible);
 	}
 	
 	public void setTypeLabelText(String text){
 		myTypeLabel.setText(text);
 	}
 	
 	@Override
 	protected void outlineShape(Graphics graphics) {
 		Rectangle bounds = getBounds();
 		if (bounds.width < myDepthGap * 2 || bounds.height < myDepthGap * 2){
 			graphics.drawRectangle(bounds);
 			return;
 		}
 		
 		int x_min = bounds.x;
 		int y_min = bounds.y;
 		int x_max = bounds.x + bounds.width - 1;
 		int y_max = bounds.y + bounds.height - 1;
 		int delta = myDepthGap;
 		
 		graphics.drawLine(x_min, y_min + delta, x_min + delta, y_min);
 		graphics.drawLine(x_min + delta, y_min, x_max, y_min);
 		graphics.drawLine(x_max, y_min, x_max - delta, y_min + delta);
 		graphics.drawLine(x_max, y_min, x_max, y_max - delta);
 		graphics.drawLine(x_max, y_max - delta, x_max - delta, y_max);
 	}
 	
 	@Override
 	protected void fillShape(Graphics graphics) {
 		Rectangle bounds = getBounds();
 		if (bounds.width < myDepthGap * 2 || bounds.height < myDepthGap * 2){
 			graphics.fillRectangle(bounds);
 			return;
 		}
 		
 		int x_min = bounds.x;
 		int y_min = bounds.y;
 		int x_max = bounds.x + bounds.width - 1;
 		int y_max = bounds.y + bounds.height - 1;
 		int delta = myDepthGap;
 		
 		int i = 0;
 		myFillPath[i++] = x_min;
 		myFillPath[i++] = y_min + delta;
 		myFillPath[i++] = x_min + delta;
 		myFillPath[i++] = y_min;
 		myFillPath[i++] = x_max;
 		myFillPath[i++] = y_min;
 		myFillPath[i++] = x_max;
 		myFillPath[i++] = y_max - delta;
 		myFillPath[i++] = x_max - delta;
 		myFillPath[i++] = y_max;
 		myFillPath[i++] = x_max - delta;
 		myFillPath[i++] = y_min + delta;
 		myFillPath[i++] = x_min;
 		myFillPath[i++] = y_min + delta;
 		
 		graphics.fillPolygon(myFillPath);
 	}
 	
 	private static RectangleFigure invisibleRectangle(){
 		RectangleFigure result = new RectangleFigure();
 		result.setOutline(false);
 		return result;
 	}
 	
 	private static Label wrapLabel(){
 		Label result = new Label();
 		result.setBorder(new MarginBorder(0, 3, 0, 3));
 		return result;
 	}
 
 	private static StereotypeLabel stereoLabel(){
 		StereotypeLabel result = new StereotypeLabel();
 		result.setBorder(new MarginBorder(0, 3, 0, 3));
 		return result;
 	}
 
 }
