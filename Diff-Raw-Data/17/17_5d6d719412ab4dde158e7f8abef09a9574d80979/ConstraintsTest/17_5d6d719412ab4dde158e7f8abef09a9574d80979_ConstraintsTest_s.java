 package homework5.processing.test;
 
 import homework5.android.constraints.EqualConstraint;
 import homework5.android.constraints.GraphicalObjectHorizontalCenterConstraint;
 import homework5.android.constraints.GraphicalObjectProperty;
 import homework5.android.constraints.GraphicalObjectVerticalCenterConstraint;
 import homework5.android.constraints.IntProductConstraint;
 import homework5.android.constraints.IntVariable;
 import homework5.android.constraints.Variable;
 import homework5.processing.behavior.MoveXBehavior;
 import homework5.processing.core.InteractiveWindowGroup;
 import homework5.processing.graphicalobject.LayoutGroup;
 import homework5.processing.graphicalobject.OutlineRect;
 import homework5.processing.graphicalobject.SimpleGroup;
 import homework5.processing.graphicalobject.Slider;
 import homework5.processing.graphicalobject.Text;
 
 import java.awt.Color;
 
 import processing.core.PApplet;
 
 public class ConstraintsTest extends InteractiveWindowGroup {
 
 	@Override
 	public void setup() {
 		super.setup();
 		// make the UI
 		LayoutGroup root = new LayoutGroup(0, 0, width, height, LayoutGroup.VERTICAL , 5);
 		root.addChild(new Text("width:", 0, 0,createFont("Helvetica",24), 18, Color.BLACK));
 		
 		Slider s1 = new Slider(0, 0, width, 30, 30, Color.BLACK, 20);
 		root.addChild(s1);
 		m_behaviors.add(new MoveXBehavior(s1.getSlider()));
 		root.addChild(new Text("height:", 0, 0,createFont("Helvetica",24), 18, Color.BLACK));
 		Slider s2 = new Slider(0, 0, width, 30, 30, Color.BLACK, 20);
 		root.addChild(s2);
 		m_behaviors.add(new MoveXBehavior(s2.getSlider()));
 		root.addChild(new Text("x:", 0, 0,createFont("Helvetica",24), 18, Color.BLACK));
 		Slider s3 = new Slider(0, 0, width, 30, 30, Color.BLACK, 50);
 		root.addChild(s3);
 		m_behaviors.add(new MoveXBehavior(s3.getSlider()));
 		root.addChild(new Text("y:", 0, 0,createFont("Helvetica",24), 18, Color.BLACK));
 		Slider s4 = new Slider(0, 0, width, 30, 30, Color.BLACK, 20);
 		root.addChild(s4);
 		m_behaviors.add(new MoveXBehavior(s4.getSlider()));
 		
 		SimpleGroup sg = new SimpleGroup(0,0,width, height);
 		root.addChild(sg);
 		
 		OutlineRect blueRect = new OutlineRect (0, 0, 50, 80, Color.BLUE, 5);
 		OutlineRect redRect = new OutlineRect (100, 0, 50, 80, Color.RED, 1);
 		sg.addChild(redRect);
 		sg.addChild(blueRect);
 		
 		// Build the constraints
 		
 		// make red width, height = 2 * blue width
 		Variable<Integer> redWidth = new GraphicalObjectProperty<Integer>(redRect, "Width");
 		Variable<Integer> redHeight = new GraphicalObjectProperty<Integer>(redRect, "Height");
 		Variable<Integer> redY = new GraphicalObjectProperty<Integer>(redRect, "Y");
 		Variable<Integer> redX = new GraphicalObjectProperty<Integer>(redRect, "X");
 		Variable<Integer> blueWidth = new GraphicalObjectProperty<Integer>(blueRect, "Width");
 		Variable<Integer> blueHeight = new GraphicalObjectProperty<Integer>(blueRect, "Height");
 		
 		// make width of red 2 * width of blue
 		new IntProductConstraint(redWidth, blueWidth, new IntVariable(2) ).activate();
 		new IntProductConstraint(redHeight, blueHeight, new IntVariable(2) ).activate();
 		// center blue in red
		//new GraphicalObjectHorizontalCenterConstraint(blueRect, redRect).activate();
		new GraphicalObjectVerticalCenterConstraint(blueRect, redRect).activate();
 		// tie sliders to variable values 
 		new EqualConstraint(redX, new GraphicalObjectProperty<Integer>(s3, "Value")).activate();
 		new EqualConstraint(redY, new GraphicalObjectProperty<Integer>(s4, "Value")).activate();
 		new EqualConstraint(blueWidth, new GraphicalObjectProperty<Integer>(s1, "Value")).activate();
 		new EqualConstraint(blueHeight, new GraphicalObjectProperty<Integer>(s2, "Value")).activate();
 		addChild(root);
 		
 		println("adjust the sliders to change properties of the rectangles");
 		println("blue rectangle will always stay in the center of red rectangle");
 	}
 
 	public static void main(String args[]) {
 		PApplet.main(new String[] { "homework5.processing.test.ConstraintsTest" });
 	}
 	
 }
