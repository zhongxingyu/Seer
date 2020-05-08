 package LinearArcPlotEditor;
 
 import java.awt.BasicStroke;
 import java.awt.Canvas;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Rectangle;
 import java.awt.Shape;
 import java.awt.Stroke;
 import java.awt.event.MouseListener;
 import java.awt.geom.Line2D;
 import java.util.List;
 import java.util.PriorityQueue;
 
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import LinearArcPlotEditor.Arc;
 
 import LinearArcPlotEditor.ColorGradientRectangle;
 
 import com.clcbio.api.clc.datatypes.bioinformatics.structure.rnasecondary.RnaStructures;
 import com.clcbio.api.clc.datatypes.bioinformatics.structure.rnasecondary.annotation.RnaStructureAnnotation;
 import com.clcbio.api.clc.editors.graphics.components.ColorGradientModel;
 import com.clcbio.api.clc.graphics.DrawingContext;
 import com.clcbio.api.clc.graphics.components.ColorGradientManager;
 import com.clcbio.api.clc.graphics.framework.ChildDrawingNode;
 import com.clcbio.api.clc.graphics.framework.ClcCanvas;
 import com.clcbio.api.clc.graphics.framework.ClcScrollPane;
 import com.clcbio.api.clc.graphics.framework.RootDrawingNode;
 import com.clcbio.api.clc.graphics.framework.ViewBounds;
 import com.clcbio.api.free.datatypes.bioinformatics.sequence.Sequence;
 import com.clcbio.api.free.gui.components.ObjectMoveable;
 import com.clcbio.api.free.gui.dialog.ClcMessages;
 import com.clcbio.api.free.gui.focus.ClcFocusPanel;
 import com.clcbio.api.free.gui.focus.ClcFocusScrollPane;
 
 public class LAP extends RootDrawingNode {
 
 	
 	private int [] pairings; 
 	private float [] reliabilities; 
 	private int seqLength;
 	//Used to specify height of size. 
 	private int broadestPair;
 	private TitleText titleText;
 	private Arc [] arcs; 
 	private Baseline baseline;
 	private String TextForTitle;
 	private Sequence seq;
 	private PriorityQueue<Arc> MouseOverArcs;
 	private Arc mouseOverArc;
 	
 	public LAP(Sequence seq, ColorGradientModel gradmodel, String title){
 		this.seq = seq;
 		seqLength = seq.getLength();
 		if(seq.getLength() > 1000){
 			scaleX = 0.09;
 			scaleY = 0.1;
 		}
 		this.TextForTitle = title;
 		init();
 		
 		
 		int nr=0;
 		for(int i = 0;i<pairings.length; i++){
 			if(pairings[i]>i){
 				nr = nr+1;
 				if(pairings[i]-i > broadestPair){
 					broadestPair = (pairings[i]-i);
 				}
 			}
 		}
 		
 		//Generate arcs
 		int cnt = 0;
 		if(arcs==null){
 			arcs = new Arc[nr];
 			for(int i = 0; i<pairings.length; i++){
 				if(pairings[i]>i){
 					arcs[cnt] = new Arc(i,pairings[i],seqLength, reliabilities[i], this);
 					//System.out.println("Reliabilities first: " + reliabilities[i] + " reliabilities second " + reliabilities[pairings[i]]);
 					arcs[cnt].broadestPair = broadestPair;
 					addChild(arcs[cnt]);
 					cnt = cnt+1;
 				}
 			}
 		}
 
 		LAPFeatureView lv = new LAPFeatureView(seq,this);
 		for(LAPFeature l : lv.getFeatures()){
 			addChild(l);
 			System.out.println(l.getYAxis() + "YAxis of lapfeature l");
 			for(LAPFeatureInterval li : l.getIntervals()){
 				addChild(li);
 			}
 		}
 		baseline = new Baseline(seq, this);
 		addChild(baseline);
 
 		titleText = new TitleText(TextForTitle);
 		addChild(titleText);
 		
 		setColors(gradmodel);
 		setSize();
 	}
 	
 	private void init(){
 		// Set scaleX and scaleY for zooming. 
 		setMaxScaleX(12);
 		setMaxScaleY(12);
 		setMinScaleX(0.09);
 		setMinScaleX(0.1);
 		setMinScaleY(0.1);
 		setMinScaleRatio(1.0);
 		setMaxScaleRatio(1.0);
 		
 		//setup pairing and reliabilities.
 		pairings = RnaStructures.getStructures(
 				seq).getStructure(0).getPairing();
     	reliabilities = new float[seq.getLength()];
     	
     	//Our Rna structure
     	List<RnaStructureAnnotation> annotations = RnaStructures.getStructures(
 				seq).getStructure(0).getStructureAnnotations();
 		RnaStructureAnnotation probAnnotation = annotations.get(0);
 		
 		//Set reliability values
     	for(int i = 0; i<seq.getLength(); i++){
 			//get reliability of structure at that position
 			reliabilities[i] = (float)probAnnotation.getValue(i);
 		}
     	this.seqLength = seq.getLength();
     	
     	ArcComparator comp = new ArcComparator();
     	MouseOverArcs = new PriorityQueue<Arc>(20, comp);
 	}
 	
 	public void setTitle(String title){
 		titleText.setTitle(title);
 	}
 	
 	public void setColors(ColorGradientModel gradmodel){  
 		int cnt = 0; 
 		for(int i = 0; i<pairings.length; i++){
 			if(pairings[i]>i){
 				arcs[cnt].setColor(gradmodel.getColor(reliabilities[i]));
 				cnt = cnt+1;
 			}
 		}
 	}
 	
 	public void addArcToQueue(Arc arc){
 		this.MouseOverArcs.add(arc);
 	}
 	
 	public void removeArcFromQueue(Arc arc){
 		this.MouseOverArcs.remove(arc);
 	}
 	
 	public void iterateOverArcs(){
 		while(MouseOverArcs.size() != 0){
 			System.out.println("ct value " + MouseOverArcs.remove().getContainValue());
 		}
 	}
 	
 	
 	
 	public boolean canArcShow(){
 		return false;
 	}
 	
 	/*
 	 * Returns the y position of the visible screen in contrast to the entire canvas. 
 	 */
 	public int getYViewBounds(){
 		ClcScrollPane pane = getCanvas().getScrollPane();
 		List<ViewBounds> pV = pane.getVerticalViewBounds();
 		return (int) pV.get(0).getPosition();
 	}
 	
 	
 	/*
 	 * Returns the x position of the visible screen in contrast to the entire canvas. 
 	 */
 	public int getXViewBounds(){
 		ClcScrollPane pane = getCanvas().getScrollPane();
 		List<ViewBounds> pH = pane.getHorizontalViewBounds();
 		return (int)pH.get(0).getPosition();
 	}
 	
 	public int getViewPaneWidth(){
 		ClcScrollPane pane = getCanvas().getScrollPane();
 		return pane.getWidth();
 	}
 	
 	public int getViewPaneHeight(){
 		ClcScrollPane pane = getCanvas().getScrollPane();
 		return pane.getHeight();
 	}
 	
 	@Override
 	protected void setSize() {
 		ClcCanvas cv = getCanvas();
 		if(cv != null){
 			ClcScrollPane pane = cv.getScrollPane();
 			if(pane != null){
 				System.out.println("pane width " + pane.getViewWidth());
 				List<ViewBounds> pp = pane.getHorizontalViewBounds();
 				ViewBounds bb = pp.get(0);
 				List<ViewBounds> pV = pane.getVerticalViewBounds();
 				System.out.println("Viewbounds position x: " + bb.getPosition() + " y: " +  pV.get(0).getPosition());
 				Rectangle rg = pane.getVisibleRect();
 				System.out.println("rectangle x: " + rg.x + " rg.width " + rg.width + " center x " + rg.getCenterX());
 			}	
 		}
 		
 		if(pairings != null){
 			setSize(-110, pairings.length*getScaleX()+50, 0, 1000+(broadestPair/4)*getScaleY());
 		}
 		else{
 			setSize(0,1200*getScaleX(),0,600);
 		}
 	}
 	
 	public boolean canArcShowMouseOver(Arc arc){
 		if(mouseOverArc == null){
 			this.mouseOverArc = arc;
 			mouseOverArc.showAnnotation(true);
 			mouseOverArc.drawRect(true);
 			return true;
 		}
 		
 		double newArcValue = arc.getContainValue()-1;
 		double currentArcValue = mouseOverArc.getContainValue()-1;
 		
 		if(Math.abs(newArcValue) < Math.abs(currentArcValue)) {
			System.out.println("Under vrdien");
 			mouseOverArc.showAnnotation(false);
 			mouseOverArc = arc;
 			mouseOverArc.showAnnotation(true);
 			mouseOverArc.drawRect(true);
			//repaint();
 			return true;
 		}
 		if(arc == mouseOverArc) {
			System.out.println("Samme arc");
			mouseOverArc.showAnnotation(true);
			mouseOverArc.drawRect(true);
 			return true;
 		}
		System.out.println("false");
 		arc.showAnnotation(false);
 		return false;
 	}
 
 	public void refresh(ColorGradientModel colorGradientModel) {
 		setColors(colorGradientModel);
 	}
 	
 	public int GetLDHeight(){
 		return 500;
 	}
 	
 	public void setBaseLineText(boolean isBold, int textSize, String fontName){
 		baseline.setBold(isBold);
 		baseline.setFontSize(textSize);
 		baseline.setFontName(fontName);
 		baseline.updateFont();
 	}
 	
 	/*
 	 * This returns the Y position of the Base X-axis used for drawing. 
 	 */
 	public int getBaseXAxis(){
 		return (int)(100+(broadestPair/4)*getScaleY());
 	}
 	
 }
