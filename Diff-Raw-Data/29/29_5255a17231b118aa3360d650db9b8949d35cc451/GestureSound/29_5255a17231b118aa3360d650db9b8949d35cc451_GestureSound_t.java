 package advanced.gestureSound;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.mt4j.components.MTComponent;
 import org.mt4j.input.IMTInputEventListener;
 import org.mt4j.input.inputData.AbstractCursorInputEvt;
 import org.mt4j.input.inputData.InputCursor;
 import org.mt4j.input.inputData.MTInputEvent;
 import org.mt4j.sceneManagement.AbstractScene;
 
 import processing.core.PApplet;
 import processing.core.PGraphics;
 import advanced.gestureSound.gestures.GestureEngine;
 import advanced.gestureSound.gestures.GestureEngine.ParamMap;
 import advanced.gestureSound.gestures.GestureEngine.Zone;
 import de.sciss.jcollider.Buffer;
 import de.sciss.jcollider.Synth;
 
 public class GestureSound extends MTComponent {
 
 	PApplet applet;
 	GestureEngine engine;
 	
 	SC sc;
 	List<InputCursor> ins;
 	
 	float lastRight1 = 0f;
 	float lastRight2 = 0f;
 	float lastLeft1 = 0f;
 	float lastLeft2 = 0f;
 
 	public GestureSound(PApplet applet, final AbstractScene scene) {
 		super(applet);
 		// TODO Auto-generated constructor stub
 		this.applet = applet;
 		this.engine = new GestureEngine(applet, scene);
 		this.ins = new ArrayList<InputCursor>();
 		
         scene.getCanvas().addInputListener(new IMTInputEventListener() {
         	//@Override
         	public boolean processInputEvent(MTInputEvent inEvt){
         		if(inEvt instanceof AbstractCursorInputEvt){
         			AbstractCursorInputEvt posEvt = (AbstractCursorInputEvt)inEvt;
         			if (posEvt.hasTarget() && posEvt.getTargetComponent().equals(scene.getCanvas())){
         				InputCursor m = posEvt.getCursor();
         				if (posEvt.getId() == AbstractCursorInputEvt.INPUT_ENDED) {
         					ins.remove(m);
         				}
         				else if (posEvt.getId() == AbstractCursorInputEvt.INPUT_DETECTED) {
         					ins.add(m);
         				}
         			}
         		}
         		return false;
         	}
 		});
 		
 		
 		sc = new SC();
 		sc.setupSupercollider();
 		
 		setupGestures();
 	}
 	
 	
 	void setupGestures() {
 		try {
 			File f = new File("data/sounds/amiu.aif");
 			System.out.println("Loading sample at: "+f.getAbsolutePath());
 			final Buffer b = Buffer.read(sc.server,f.getAbsolutePath());
 			Synth synth1 = new Synth("grannyyy", new String[] {"trigRate", "buffer", "dur"}, new float[] { 10, b.getBufNum(), 0.1f }, sc.grpAll);
 			engine.addToMap("curvature", synth1, "centerPos", 
					new ParamMap() { 
						public float pos = 0f;
						public float dur = (float) b.getDuration();
						public float map(float in) {
							if (dur != Float.NaN)
								dur = (float) b.getDuration();
							pos += in/20f;
							pos = Math.min(Math.max(0, pos), dur);
							System.out.println("Pos:"+pos+" In:"+in+" Dur:"+dur);
							return pos;
					}},
 					new Zone() { @Override public boolean in(InputCursor in) {return inQuadrant(in,1)||inQuadrant(in,4);}});
 			engine.addToMap("velocity", synth1, "trigRate", 
 					new ParamMap() { public float map(float in) {return in*4; }},
 					new Zone() { @Override public boolean in(InputCursor in) {return inQuadrant(in,1)||inQuadrant(in,4);}});
 			engine.addToMap("curvature", synth1, "dur", 
 					new ParamMap() { public float map(float in) {return (float)((0.3+in)*b.getDuration()); }},
 					new Zone() { @Override public boolean in(InputCursor in) {return inQuadrant(in,2)||inQuadrant(in,3);}});
 			engine.addToMap("velocity", synth1, "amp", 
 					new ParamMap() { public float map(float in) {return in/10; }},
 					new Zone() { @Override public boolean in(InputCursor in) {return inQuadrant(in,2)||inQuadrant(in,3);}});
 
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	
 	@Override
 	public void drawComponent(PGraphics g) {
 		g.stroke(100);
 		g.fill(150);
 		
 		for (InputCursor in : ins) {
 			if (in.getFirstEvent().getPosX() < this.applet.width/2) { //its on the left half of the screen
 				lastLeft1 = engine.getCurrentValue("curvature", in)*300;
 				lastLeft2 = engine.getCurrentValue("velocity", in)*2;
 				g.fill(100,100,0);
 			}
 			else {
 				lastRight1 = engine.getCurrentValue("curvature", in)*300;
 				lastRight2 = engine.getCurrentValue("velocity", in)*2;
 				g.fill(100,0,100);
 			}
 			
 			for (AbstractCursorInputEvt evt : in.getEvents()) {
 				g.rect(evt.getPosX(), evt.getPosY(), 10, 10);
 			}
 		}
 		g.stroke(100);
 		g.fill(150);
 
 		//top left bars
 		g.rect(30,50,30,lastLeft1);
 		g.rect(70,50,30,lastLeft2);
 
 		//top right bars
 		g.rect(this.applet.width-60,50,30,lastRight1);
 		g.rect(this.applet.width-100,50,30,lastRight2);
 		
 		//middle line
 		g.line(this.applet.width/2, 0, this.applet.width/2, this.applet.height);
 
 	}
 
 }
