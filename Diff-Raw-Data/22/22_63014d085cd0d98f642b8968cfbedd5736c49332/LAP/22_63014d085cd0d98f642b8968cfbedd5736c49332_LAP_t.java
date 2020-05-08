 package LinearArcPlotEditor;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.Shape;
 import java.awt.Stroke;
 import java.awt.event.MouseListener;
 import java.awt.geom.Line2D;
 import java.util.List;
 
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
 import com.clcbio.api.clc.graphics.framework.RootDrawingNode;
 import com.clcbio.api.free.datatypes.bioinformatics.sequence.Sequence;
 import com.clcbio.api.free.gui.components.ObjectMoveable;
 import com.clcbio.api.free.gui.dialog.ClcMessages;
 import com.clcbio.api.free.gui.focus.ClcFocusPanel;
 import com.clcbio.api.free.gui.focus.ClcFocusScrollPane;
 
 public class LAP extends RootDrawingNode {
 
 	
 	private int [] pairings; 
 	private float [] reliabilities; 
 	private Color [] colors;
 	private int seqLength;
 	private int lengtht;
	//Used to specify height of size. 
	private int broadestPair;
 	private ColorGradientRectangle colorGradientRectangle; 
 	
 	
 	private Arc [] arcs; 
 	
 	private Baseline baseline;
 	
 	private String title;
 	private Sequence seq;
 
 	public LAP(Sequence seq, ColorGradientModel gradmodel, String title){
 		this.seq = seq;
 		seqLength = seq.getLength();
 		init();
 		
 		baseline = new Baseline(seqLength);
 		addChild(baseline);
 		
 		
 		int nr=0;
 		for(int i = 0;i<pairings.length; i++){
 			if(pairings[i]>i){
 				nr = nr+1;
 			}
 		}
 		
 		//Generate arcs
 		int cnt = 0;
 		if(arcs==null){
 			arcs = new Arc[nr];
 			for(int i = 0; i<pairings.length; i++){
 				if(pairings[i]>i){
					if(pairings[i]-i > broadestPair){
						broadestPair = pairings[i]-i;
					}
 					System.out.println(i + " i");
 					System.out.println(pairings[i] + " p[i]");
 					arcs[cnt] = new Arc(i,pairings[i],seqLength, reliabilities[i]);
 					addChild(arcs[cnt]);
 					cnt = cnt+1;
 				}
 			}
 		}
 		
 		setColors(gradmodel);
 		setSize();
 	}
 	
 	private void init(){
 		// Set scaleX and scaleY for zooming. 
 		setMaxScaleX(30);
 		setMaxScaleY(30);
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
     	System.out.println("Lngde: " + seq.getLength());
     	this.seqLength = seq.getLength();
 	}
 	
 	
 	public void setColors(ColorGradientModel gradmodel){  
 		int cnt = 0; 
 		for(int i = 0; i<pairings.length; i++){
 			if(pairings[i]>i){
 				arcs[cnt].setColor(gradmodel.getColor(reliabilities[i]));
 				cnt = cnt+1;
 			}
 		}
 		//colorGradientRectangle.setColors(gradmodel);
 	}
 	
 	@Override
 	protected void setSize() {
 		// TODO Auto-generated method stub
 		if(pairings != null){
 			System.out.println("seq length: " + pairings.length);
			setSize(0, pairings.length*getScaleX(), 0, broadestPair*getScaleY());
 		}
 		else{
 			System.out.println("Null");
 		}
 		//System.out.println(" width: " + seq.getLength()*getScaleX() + " ScaleX: " + getScaleX());
 		setSize(0,1200*getScaleX(),0,600);
 	}
 }
