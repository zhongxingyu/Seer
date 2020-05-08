 package io.github.dector.lightmap.core;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author dector
  */
 public class LightMap {
 
	private static final boolean MEASURE_UPDATE = false;
 
 	private Map<Position, Light> staticLights;
 	private Map<Integer, Pair<Position, Light>> dynamicLights;
 	private int lastDynamicId = 0;
 
 	private float[][] staticLightsValues;
 	private float[][] dynamicLightsValues;
 	private float[][] lightValues;
 
 	private int width;
 	private int height;
 
 	private boolean staticDirty;
 	private boolean dynamicDirty;
 
 	public LightMap(int width, int height) {
 		this.width = width;
 		this.height = height;
 
 		lightValues = new float[width][height];
 		staticLightsValues = new float[width][height];
 		dynamicLightsValues = new float[width][height];
 
 		staticLights = new HashMap<Position, Light>();
 		dynamicLights = new HashMap<Integer, Pair<Position, Light>>();
 	}
 
 	public int getWidth() {
 		return width;
 	}
 
 	public int getHeight() {
 		return height;
 	}
 
 	public float getLightValueAt(int x, int y) {
 		return lightValues[x][y];
 	}
 
 	public float[][] getLightValues() {
 		return lightValues;
 	}
 
 	public void addStaticLight(Light light, int x, int y) {
 		addStaticLight(light, new Position(x, y));
 	}
 
 	public void addStaticLight(Light light, Position pos) {
 		staticLights.put(pos, light);
 
 		markStaticDirty();
 	}
 
 	public int addDynamicLight(Light light, Position pos) {
 		Pair<Position, Light> pair = new Pair<Position, Light>(pos, light);
 		dynamicLights.put(lastDynamicId, pair);
 
 		markDynamicDirty();
 
 		return lastDynamicId++;
 	}
 
 	public void removeStaticLightAt(Position p) {
 		staticLights.remove(p);
 
 		markStaticDirty();
 	}
 
 	public Position[] getStaticLightsPositions() {
 		Position[] pos = new Position[staticLights.size()];
 		staticLights.keySet().toArray(pos);
 		return pos;
 	}
 
 	public void changeStaticLightAt(Position p, int innerRadius, int outerRadius) {
 		removeStaticLightAt(p);
 		addStaticLight(new Light(innerRadius, outerRadius), p);
 	}
 
 	public boolean hasStaticLightAt(int x, int y) {
 		return hasStaticLightAt(new Position(x, y));
 	}
 
 	public boolean hasStaticLightAt(Position pos) {
 		boolean found = false;
 
 		for (Position p : getStaticLightsPositions()) {
 			if (p.equals(pos)) {
 				found = true;
 			}
 		}
 
 		return found;
 	}
 
 	public Light getStaticLightAt(Position p) {
 		if (staticLights.containsKey(p)) {
 			return staticLights.get(p);
 		} else {
 			return null;
 		}
 	}
 
 	public int getStaticLightsCount() {
 		return staticLights.size();
 	}
 
 	public void setDynamicLightTo(int id, int x, int y) {
 		if (! dynamicLights.containsKey(id)) return;
 
 		Pair<Position, Light> pair = dynamicLights.get(id);
 		pair.first.x = x;
 		pair.first.y = y;
 
 		markDynamicDirty();
 	}
 
 	public void moveDynamicLight(int id, int dx, int dy) {
 		if (! dynamicLights.containsKey(id)) return;
 
 		Pair<Position, Light> pair = dynamicLights.get(id);
 		pair.first.x += dx;
 		pair.first.y += dy;
 
 		markDynamicDirty();
 	}
 
 	private long measureStartTime;
 
 	public void step() {
 		if (MEASURE_UPDATE)
 			measureStartTime = System.currentTimeMillis();
 
 		boolean dirty = false;
 
 		if (staticDirty) {
 			recountStaticLights();
 
 			dirty = true;
 		}
 
 		if (dynamicDirty) {
 			recountDynamicLights();
 
 			dirty = true;
 		}
 
 		if (dirty) {
 			clearArray(lightValues);
 			applyStaticLights();
 			applyDynamicLights();
 			setMaxOneInArray(lightValues);
 		}
 
 		if (MEASURE_UPDATE) {
 			float measureTime = (float) (System.currentTimeMillis() - measureStartTime) / 1000;
 
 			if (measureTime >= 0.001f)
 				System.out.printf("Update time: %.3f s\n", measureTime);
 		}
 	}
 
 	private void markStaticDirty() {
 		staticDirty = true;
 	}
 
 	private void markDynamicDirty() {
 		dynamicDirty = true;
 	}
 
 	private void recountStaticLights() {
 		clearArray(staticLightsValues);
 
 		for (Position p : staticLights.keySet()) {
 			Light l = staticLights.get(p);
 
 			recountLight(staticLightsValues, p, l);
 		}
 
 		setMaxOneInArray(staticLightsValues);
 
 		staticDirty = false;
 	}
 
 	private void recountDynamicLights() {
 		clearArray(dynamicLightsValues);
 
 		for (int id : dynamicLights.keySet()) {
 			Pair<Position, Light> p = dynamicLights.get(id);
 
 			recountLight(dynamicLightsValues, p.first, p.second);
 		}
 
 		setMaxOneInArray(dynamicLightsValues);
 
 		dynamicDirty = false;
 	}
 
 	private void recountLight(float[][] lightValues, Position p, Light l) {
 		int x = p.x;
 		int y = p.y;
 
 		int inR = l.innerRadius;
 		int outR = l.outerRadius;
 		int fromX 	= Math.max(x - outR, 0);
 		int toX 	= Math.min(x + outR, width - 1);
 		int fromY 	= Math.max(y - outR, 0);
 		int toY 	= Math.min(y + outR, height - 1);
 
 		for (int i = fromX; i <= toX; i++) {
 			for (int j = fromY; j <= toY; j++) {
 				int dx = x - i;
 				int dy = y - j;
 
 				float dd = (float) Math.sqrt(dx * dx + dy * dy);
 
 				if (dd <= inR) {
 					lightValues[i][j] = 1;
 				} else if (dd <= outR) {
 					float lightVal = (float) Math.pow(1 - dd / outR, 1.4f);
 					lightValues[i][j] += lightVal;
 				}
 			}
 		}
 	}
 
 	private void applyStaticLights() {
 		for (int x = 0; x < width; x++) {
 			for (int y = 0; y < height; y++) {
 				lightValues[x][y] += staticLightsValues[x][y];
 			}
 		}
 	}
 
 	private void applyDynamicLights() {
 		for (int x = 0; x < width; x++) {
 			for (int y = 0; y < height; y++) {
 				lightValues[x][y] += dynamicLightsValues[x][y];
 			}
 		}
 	}
 
 	private void clearArray(float[][] a) {
 		for (int i = 0; i < a.length; i++) {
 			for (int j = 0; j < a[i].length; j++) {
 				a[i][j] = 0;
 			}
 		}
 	}
 
 	private void setMaxOneInArray(float[][] a) {
 		for (int i = 0; i < a.length; i++) {
 			for (int j = 0; j < a[i].length; j++) {
 				if (a[i][j] > 1) {
 					a[i][j] = 1;
 				}
 			}
 		}
 	}
 
 	public void print() {
 		StringBuilder sb = new StringBuilder();
 
 		for (int y = height - 1; y >= 0; y--) {
 			sb.append("| ");
 			for (int x = 0; x < width; x++) {
 				sb.append(String.format("%.2f | ", lightValues[x][y]));
 			}
 
 			sb.append("\n");
 		}
 
 		System.out.println(sb.toString());
 	}
 }
