 package advanced.gestureSound.gestures;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 
 import org.mt4j.input.IMTInputEventListener;
 import org.mt4j.input.inputData.AbstractCursorInputEvt;
 import org.mt4j.input.inputData.InputCursor;
 import org.mt4j.input.inputData.MTInputEvent;
 import org.mt4j.sceneManagement.AbstractScene;
 
 import processing.core.PApplet;
 import Jama.Matrix;
 import advanced.gestureSound.gestures.filters.KalmanFilter;
 import advanced.gestureSound.gestures.qualities.Curvature;
 import advanced.gestureSound.gestures.qualities.Quality;
 import advanced.gestureSound.gestures.qualities.Velocity;
 import de.sciss.jcollider.Synth;
 
 public class GestureEngine {
 	public static class SynthInfo {
 		public Synth synth;
 		public String parameter;
 		public ParamMap pMap;
 		public Zone zone;
 		public SynthInfo(Synth synth, String param, ParamMap pMap, Zone z) {
 			this.synth = synth; this.parameter = param; this.pMap = pMap; this.zone = z;
 		}
 	}
 	public static interface ParamMap {
 		/**
 		 * Map from the range [-1..1] to whatever the synth param takes
 		 * @param in
 		 * @return
 		 */
 		public float map(float in);
 	}
 	
 	public static class Zone {
 		public boolean in(InputCursor c) {return true;}
 		protected boolean inQuadrant(InputCursor c, int quad) {
 			float x = c.getFirstEvent().getPosX()/applet.width;
 			float y = c.getFirstEvent().getPosY()/applet.height;
 			if (quad == 1 && y<0.5 && x < 0.5) return true;
 			if (quad == 2 && y<0.5 && x >= 0.5) return true;
 			if (quad == 3 && y>=0.5 && x >= 0.5) return true;
			if (quad == 4 && y>=0.5 && x < 0.5) return true;
 			return false;
 		}
 	}
 	
 
 	public HashMap<String, ArrayList<SynthInfo>> map;
 	public HashMap<String, HashMap<InputCursor, Quality>> qualities;
 	public HashMap<InputCursor, KalmanFilter> filters;
 	public static PApplet applet;
 	
 	public GestureEngine(PApplet app, AbstractScene scene) {
 		applet = app;
 		map = new HashMap<String, ArrayList<SynthInfo>>();
 		qualities =  new HashMap<String, HashMap<InputCursor,Quality>>();
 		qualities.put(Curvature.name, new HashMap<InputCursor,Quality>()) ;
 		qualities.put(Velocity.name, new HashMap<InputCursor,Quality>()) ;
 		filters = new HashMap<InputCursor, KalmanFilter>();
 		setupCursorListener(scene);
 	}
 	
 	
 	public void addToMap(String quality, Synth synth, String param, ParamMap pMap) {
 		addToMap(quality,synth,param,pMap, new Zone());
 	}
 	
 	public void addToMap(String quality, Synth synth, String param, ParamMap pMap, Zone z) {
 		if (!map.containsKey(quality))
 			map.put(quality, new ArrayList<SynthInfo>());
 		map.get(quality).add(new SynthInfo(synth,param,pMap,z));
 	}
 
 	
 	public void setupCursorListener(final AbstractScene scene) {
         scene.getCanvas().addInputListener(new IMTInputEventListener() {
         	@Override
         	public boolean processInputEvent(MTInputEvent inEvt){
         		if(inEvt instanceof AbstractCursorInputEvt){
         			AbstractCursorInputEvt posEvt = (AbstractCursorInputEvt)inEvt;
         			if (posEvt.hasTarget() && posEvt.getTargetComponent().equals(scene.getCanvas())){
         				if (posEvt.getId() == AbstractCursorInputEvt.INPUT_ENDED) {
         					System.out.println("Input Ended!");
         					removeCursor(posEvt.getCursor());
         				}
         				else if (posEvt.getId() == AbstractCursorInputEvt.INPUT_DETECTED) {
         					System.out.println("Input Detected!");
         					addCursor(posEvt.getCursor());
         				}
         				else {
         					InputCursor m = posEvt.getCursor();
         					updateEngine(filter(m));
         				}
         			}
         		}
         		return false;
         	}
 		});
 	}
 	
 	public void removeCursor(InputCursor in) {
 		filters.remove(in);
 	}
 	public void addCursor(InputCursor in) {
 		addQualitiesForCursor(in);
		KalmanFilter f = KalmanFilter.buildKF2D(9, 1, 20); //magicparams, still don't know what they mean.
 		f.setX(new Matrix(new double[][]{{in.getCurrentEvtPosX()}, {in.getCurrentEvtPosY()}, {0.01}, {0.01} }));
 		f.predict();
 		filters.put(in, f);
 		
 	}
 	
 	public void addQualitiesForCursor(InputCursor in) {
 		qualities.get(Curvature.name).put(in, new Curvature(this));
 		qualities.get(Velocity.name).put(in, new Velocity(this));
 	}
 	
 	public InputCursor filter(InputCursor in) {
 		AbstractCursorInputEvt evt = in.getCurrentEvent();
 		KalmanFilter f = filters.get(in);
 		f.correct(new Matrix(new double[][]{{evt.getPosX(), evt.getPosY()}}).transpose());
 		f.predict();
 		evt.setPositionX((float) f.getX().get(0,0));  //I get it!
 		evt.setPositionY((float) f.getX().get(1,0));
 		return in;
 	}
 	
 	public void updateEngine(InputCursor in) {
 		for (HashMap<InputCursor,Quality> cursorAndQualities : qualities.values()) {
 			if (cursorAndQualities.containsKey(in))
 				cursorAndQualities.get(in).update(in);
 		}
 	}
 	
 	public float getCurrentValue(String name) {
 		for (Quality qual : qualities.get(name).values()) {
 			return qual.getCurrentValue();
 		}
 		return Float.NaN;
 	}
 	
 	public float getCurrentValue(String name, InputCursor cursor) {
 		if (qualities.get(name).containsKey(cursor)) {
 			return qualities.get(name).get(cursor).getCurrentValue();
 		}
 		return Float.NaN;
 	}
 
 	
 	
 
 
 	public void gestureQualityChange(String quality, float val, InputCursor in) {
 		for (SynthInfo info : map.get(quality) ) {
 			if (info.zone.in(in)) {
 				try {
 					info.synth.set(info.parameter, info.pMap.map(val));
 				} catch (IOException e) {
 					/**
 					 * oops. not our problem.
 					 */
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 	
 
 }
