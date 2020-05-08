 /*
  *   gene View
  *   http://casmi.github.com/
  *   Copyright (C) 2011, Xcoo, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package jp.xcoo.casmi.geneview.view;
 
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import jp.xcoo.casmi.geneview.data.Exon;
 import jp.xcoo.casmi.geneview.data.Gene;
 import jp.xcoo.casmi.geneview.data.ViewScale;
 import jp.xcoo.casmi.geneview.net.GeneLoader;
 import casmi.Applet;
 import casmi.AppletRunner;
 import casmi.KeyEvent;
 import casmi.MouseButton;
 import casmi.MouseEvent;
import casmi.callback.MouseOverCallback;
import casmi.callback.MouseOverEventType;
 import casmi.graphics.color.ColorSet;
 import casmi.graphics.element.Element;
 import casmi.graphics.element.Line;
 import casmi.graphics.element.Text;
 import casmi.graphics.font.Font;
 import casmi.graphics.group.Group;
 
 /**
  * Class for Gene View
  * @author K. Nishimura
  *
  */
 public class GeneView extends Applet
 {
 	private static final String REMOTE_DATA_URL = "http://genome.ucsc.edu/cgi-bin/das/hg19/features?segment=1:500000,900000;type=refGene;type=knownGene;";
 
 	private static final double MIN_SCALE = 0.005;
 	private static final double SCALE_STEP = 0.0001;
 
 	private static final double GENE_ELEMENT_ORDER_STEP = 30;
 
 	private static final double SCALE_HEIGHT = 20;
 	private static final double HALF_SCALE_HEIGHT = SCALE_HEIGHT / 2;
 
 	private static final int MOUSE_SCALING_THRESHOLD = 10;
 
 	private static final double SCROLL_SPEED_EPS = 0.01;
 	private static final double MOUSE_SCROLL_SPEED_FACTOR = 10.0;
 	private static final double SCROLL_SPEED_DAMPING_FACTOR = 0.8;
 
 	private static final String TITLE = "Gene View";
 	private static final double FPS = 20.0;
 
 	private double scale = 0.01;
 
 	private double scroll = 0.0;
 	private double scrollSpeed = 0.0;
 
     private List<GeneElement> geneElements = new ArrayList<GeneElement>();
     private Line scaleMainLine;
     private List<Line> scaleLines = new ArrayList<Line>();
     private List<Text> scaleTexts = new ArrayList<Text>();
 
     private Text titleText;
     private String annotation = "Annotation";
     private Text annotationText;
 
 	private List<Gene> genes;
 	private List<Exon> exons;
 	private ViewScale viewScale;
 
 	private static int WIDTH = 1024;
 	private static int HEIGHT = 768;
 
 	private Group geneGroup;
 	private Group scaleGroup;
 
 	@Override
 	public void setup() {
 
 		// load gene info
 	    System.out.println("started to load");
 
 	    GeneLoader loader = new GeneLoader();
 		loader.load(REMOTE_DATA_URL);
 
 		System.out.println("finished loading");
 
 		genes = loader.getGenes();
 		exons = loader.getExons();
 		viewScale = loader.getViewScale();
 
 		setFPS(FPS);
 		setSize(WIDTH, HEIGHT);
 
 		setupRefGene();
         setupScale();
 
         {
             Font f = new Font("San-Serif");
             f.setSize(20);
 
             titleText = new Text(TITLE, f, -100, 330);
             titleText.setStrokeColor(ColorSet.WHITE);
         }
         titleText.setPosition(WIDTH / 2.0, HEIGHT - 40);
         addObject(titleText);
 
         {
             Font f = new Font("Sans-Serif");
             f.setSize(14);
 
             annotationText = new Text(annotation, f, 0, 10);
             annotationText.setStrokeColor(ColorSet.WHITE);
         }
         annotationText.setPosition(WIDTH / 2.0, HEIGHT / 2.0);
         addObject(annotationText);
 	}
 
 	@Override
     public void update() {
 		// scrolling
 		scroll += scrollSpeed / FPS;
 
 		scrollSpeed *= SCROLL_SPEED_DAMPING_FACTOR;
 		if( Math.abs(scrollSpeed) < SCROLL_SPEED_EPS ) {
 		    scrollSpeed = 0.0;
 		}
 
 		geneGroup.setX(scroll);
 		scaleGroup.setX(scroll);
 
 		GeneElement selected = null;
 
         for (GeneElement e : geneElements) {
             if (e.isSelected()) {
                 selected = e;
             }
         }
 
         if (selected != null) {
             annotationText.setText(selected.getName());
             annotationText.setPosition(selected.getX() + scroll, selected.getY() + HEIGHT / 2.0);
         } else {
             annotationText.setText("");
         }
 	}
 
     @Override
     public void exit() {
     }
 
 	private void setupRefGene(){
 	    geneGroup = new Group();
 
     	geneElements.clear();
 
     	for( Exon e: exons ){
     		GeneElement ge = new GeneElement(e, scale);
 
     		double x = ((e.getEnd() + e.getStart()) / 2.0 - viewScale.getStart() - viewScale.getLength() / 2.0) * scale;
     		double y = GENE_ELEMENT_ORDER_STEP * e.getOrder();
 
     		ge.setPosition(x, y);
 
     		geneGroup.add(ge);
 
     		geneElements.add(ge);
     	}
 
     	for( Gene g: this.genes ){
     		GeneElement ge = new GeneElement(g, scale);
 
     		double x = ((g.getEnd() + g.getStart())/2.0 - viewScale.getStart() - viewScale.getLength() / 2.0) * scale;
     		double y = GENE_ELEMENT_ORDER_STEP * g.getOrder();
 
     		ge.setPosition(x, y);
 
     		ge.addMouseEventCallback(new MouseOverCallback() {
 
                public void run(MouseOverEventType eventType, Element element) {
                     GeneElement e = (GeneElement) element;
 
                    switch(eventType) {
                     case ENTERED:
                         e.setSelected(true);
                         break;
 
                     case EXITED:
                         e.setSelected(false);
                         break;
                     default:
                         break;
                     }
                 }
             });
 
     		geneGroup.add(ge);
 
     		geneElements.add(ge);
     	}
 
    	geneGroup.setPosition(WIDTH / 2.0, HEIGHT / 3.0);
 
     	addObject(geneGroup);
 	}
 
     private void setupScale(){
         scaleGroup = new Group();
 
     	Font f = new Font("San-Serif");
         f.setSize(10);
 
         scaleLines.clear();
         scaleTexts.clear();
 
         final int length = viewScale.getLength();
         final int halfLength = length / 2;
     	final int digit = (int) Math.log10(length) + 1;
     	final double step = Math.pow(10, (digit - 2) );
     	final int numScales = (int) (length / step) + 1;
 
     	for( int i=0; i<numScales; i++ ) {
     		Line l = new Line( (i * step - halfLength ) * scale, HALF_SCALE_HEIGHT,
 					   		   (i * step - halfLength ) * scale, - HALF_SCALE_HEIGHT);
     		l.setStrokeColor(ColorSet.WHITE);
     		scaleLines.add(l);
     		scaleGroup.add(l);
 
     		Text t = new Text(Integer.toString((int)(i * step + viewScale.getStart())), f,
     						  (int)((i * step - halfLength ) * scale),
     						  (int)-SCALE_HEIGHT);
     		t.setStrokeColor(ColorSet.WHITE);
     		scaleTexts.add(t);
     		scaleGroup.add(t);
     	}
 
     	scaleMainLine = new Line(- scale * halfLength, 0, scale * halfLength, 0);
     	scaleMainLine.setStrokeColor(ColorSet.WHITE);
     	scaleGroup.add(scaleMainLine);
 
    	scaleGroup.setPosition(WIDTH / 2.0, HEIGHT / 3.0);
 
     	addObject(scaleGroup);
     }
 
     @Override
     public void mouseEvent(MouseEvent e, MouseButton b) {
         if (e == MouseEvent.DRAGGED) {
             double diffX = getMouseX() - getPrevMouseX();
             double diffY = getMouseY() - getPrevMouseY();
 
             if(Math.abs(diffY) > MOUSE_SCALING_THRESHOLD) { // scaling
 
                 if( (diffY) > 0){
                     scale += (Math.abs(diffY) - MOUSE_SCALING_THRESHOLD) * SCALE_STEP;
                 }else{
                     scale -= (Math.abs(diffY) - MOUSE_SCALING_THRESHOLD) * SCALE_STEP;
                 }
 
                 if(scale < MIN_SCALE){
                     scale = MIN_SCALE;
                 }
 
                 update();
 
             } else { // scrolling speed
                 scrollSpeed += diffX * MOUSE_SCROLL_SPEED_FACTOR;
             }
         }
     }
 
     @Override
     public void keyEvent(KeyEvent e) {
         if (e == KeyEvent.PRESSED) {
             if (getKeyCode() == 27) {
                 System.exit(0);
             }
         }
     }
 
     public static void main( String[] args )
     {
         AppletRunner.run("jp.xcoo.casmi.geneview.view.GeneView", "Gene View");
     }
 }
 
 
