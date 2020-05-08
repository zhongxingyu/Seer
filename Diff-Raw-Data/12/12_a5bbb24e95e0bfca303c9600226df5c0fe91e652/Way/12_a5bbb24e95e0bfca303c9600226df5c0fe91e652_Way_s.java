 package de.uni.stuttgart.informatik.ToureNPlaner.UI.Overlays;
 
 import android.graphics.Path;
 import android.graphics.Point;
 import android.util.FloatMath;
 import de.uni.stuttgart.informatik.ToureNPlaner.Util.ArrayDeque;
 import org.mapsforge.core.Tile;
 
 import java.util.ArrayList;
 
 class Way {
 	private static int smallestLevelSize = 4;
 	// Must be factors of smallest size
 	static final int[] steps = {128, 128, 64, 64, 64, 64, 64, 32, 16, 8, 8, 4, 4, 2, 2, 1, 1, 1, 1, 1};
 
 	private final float[] way;
 	private final float[] cache;
 	private Level root;
 	private final ArrayDeque<Level> stack = new ArrayDeque<Level>();
 	private final ArrayList<Level> clipped = new ArrayList<Level>();
 
 	private byte lastZoomLevel = -1;
 
 	public Way(int[] p) {
 		way = new float[p.length];
 		cache = new float[p.length];
 		initWay(p);
 	}
 
 	private void initWay(int[] points) {
 		int length = points.length / 2;
 
 		int pointsLeft = length;
 		// round to the right size
 		ArrayList<Level> levels = new ArrayList<Level>((length + smallestLevelSize + 1) / smallestLevelSize);
 		int begin = 0, end;
 
 		while (pointsLeft > 0) {
 			end = Math.min(begin + smallestLevelSize, length);
 			int leftMin = Integer.MAX_VALUE, topMax = Integer.MIN_VALUE, rightMax = Integer.MIN_VALUE, bottomMin = Integer.MAX_VALUE;
 			// add overlap
 			int start = Math.max(0, begin - 1);
 			int finish = Math.min(end + 1, points.length / 2);
 			for (int i = start; i < finish; i++) {
 				leftMin = Math.min(leftMin, points[i * 2]);
 				topMax = Math.max(topMax, points[i * 2 + 1]);
 				rightMax = Math.max(rightMax, points[i * 2]);
 				bottomMin = Math.min(bottomMin, points[i * 2 + 1]);
 				way[i * 2] = (float) points[i * 2] / 1000000f;
 				way[i * 2 + 1] = (float) points[i * 2 + 1] / 1000000f;
 			}
 			levels.add(new Level(leftMin, topMax, rightMax, bottomMin, begin, end));
 			pointsLeft -= smallestLevelSize;
 			begin = end;
 		}
 
 		ArrayList<Level> upper;
 		do {
 			upper = new ArrayList<Level>(levels.size() / 2);
 			for (int i = 0; i + 1 < levels.size(); i += 2) {
 				Level first = levels.get(i), second = levels.get(i + 1);
 				upper.add(new Level(first, second));
 			}
 			if (levels.size() % 2 != 0) {
 				upper.add(levels.get(levels.size() - 1));
 			}
 			levels = upper;
 		} while (upper.size() != 1);
 		root = upper.get(0);
 	}
 
 	private void clip(int leftE6, int topE6, int rightE6, int bottomE6) {
 		stack.clear();
 		stack.push(root);
 		while (!stack.isEmpty()) {
 			Level level = stack.pop();
 			// reject
 			if (level.rightE6 < leftE6 ||
 					level.leftE6 > rightE6 ||
 					level.topE6 < bottomE6 ||
 					level.bottomE6 > topE6) {
 				continue;
 			}
 			// accept
 			if (level.rightE6 <= rightE6 &&
 					level.leftE6 >= leftE6 &&
 					level.topE6 <= topE6 &&
 					level.bottomE6 >= bottomE6) {
 				clipped.add(level);
 				continue;
 			}
 			if (level.leftChild != null) {
 				stack.push(level.rightChild);
 				stack.push(level.leftChild);
 				continue;
 			}
 			clipped.add(level);
 		}
 	}
 
 	private void updatePath(Path path, Point drawPosition, byte drawZoomLevel) {
 		final int step = steps[drawZoomLevel];
 		int begin = clipped.get(0).begin;
 		path.moveTo(cache[begin * 2] - drawPosition.x, cache[begin * 2 + 1] - drawPosition.y);
 		for (Level lvl : clipped) {
 			int i;
 			for (i = lvl.begin; i < lvl.end; i += step) {
 				path.lineTo(cache[i * 2] - drawPosition.x, cache[i * 2 + 1] - drawPosition.y);
 			}
 			if (i != lvl.end - 1) {
 				i = lvl.end - 1;
 				path.lineTo(cache[i * 2] - drawPosition.x, cache[i * 2 + 1] - drawPosition.y);
 			}
 		}
 	}
 
 	static final float pi = (float) Math.PI;
 	static final float pi180 = (pi / 180.0F);
 
 	private void updateCache(byte drawZoomLevel) {
 		if (drawZoomLevel != lastZoomLevel) {
 			invalidateCache();
 			lastZoomLevel = drawZoomLevel;
 		}
 		float f = (float) (Tile.TILE_SIZE << drawZoomLevel);
 		float pi4f = (1.f / (4.f * pi)) * f;
 		float f360 = 1.f / 360.f * f;
 		float f05 = 0.5f * f;
 		final int step = steps[drawZoomLevel];
 
 		// update the first before
 		for (Level lvl : clipped) {
 			if (lvl.zoomLevelCached != drawZoomLevel) {
 				lvl.zoomLevelCached = drawZoomLevel;
 				int i;
 				for (i = lvl.begin; i < lvl.end; i += step) {
 					updatePoint(i, pi4f, f360, f05);
 				}
 				if (i != lvl.end - 1) {
 					updatePoint(lvl.end - 1, pi4f, f360, f05);
 				}
 			}
 		}
 	}
 
 	private void updatePoint(int i, float pi4f, float f360, float f05) {
 		// longitudeToPixelX
 		cache[i * 2] = (way[i * 2] + 180.f) * f360;
 		float sinLatitude = FloatMath.sin(way[i * 2 + 1] * pi180);
 		float t = (1.f + sinLatitude) / (1.f - sinLatitude);
 		cache[i * 2 + 1] = f05 - ((float) Math.log(t)) * pi4f;
 	}
 
 	public void setupPath(Path path, Point drawPosition, byte drawZoomLevel, int leftE6, int topE6, int rightE6, int bottomE6) {
 		clipped.clear();
 
 		clip(leftE6, topE6, rightE6, bottomE6);
 
 		path.rewind();
 
 		if (clipped.isEmpty())
 			return;
 
 		updateCache(drawZoomLevel);
 
 		updatePath(path, drawPosition, drawZoomLevel);
 	}
 
 	private void invalidateCache() {
 		stack.clear();
 		stack.push(root);
 		while (!stack.isEmpty()) {
 			Level level = stack.pop();
 			level.zoomLevelCached = -1;
 			if (level.leftChild != null) {
 				stack.push(level.rightChild);
 				stack.push(level.leftChild);
 			}
 		}
 	}
 
 	private static float calcDistance(float x1, float y1, float x2, float y2, int x3, int y3) {
 		final float px = x2 - x1;
 		final float py = y2 - y1;
 
 		final float dist = px * px + py * py;
 
 		float u = ((x3 - x1) * px + (y3 - y1) * py) / dist;
 
 		if (u > 1.f)
 			u = 1.f;
 		else if (u < 0.f)
 			u = 0.f;
 
 		final float x = x1 + u * px;
 		final float y = y1 + u * py;
 
 		final float dx = x - x3;
 		final float dy = y - y3;
 
 		return dx * dx + dy * dy;
 	}
 
 	public float getDistance(Point p, Point drawPosition, byte zoomLevel) {
 		int step = steps[zoomLevel];
 		float minDistance = Float.MAX_VALUE;
 
 		for (Level lvl : clipped) {
 			int i;
 			for (i = lvl.begin; i < lvl.end - step; i += step) {
 				minDistance = Math.min(minDistance,
 						calcDistance(
 								cache[i * 2] - drawPosition.x, cache[i * 2 + 1] - drawPosition.y,
 								cache[(i + step) * 2] - drawPosition.x, cache[(i + step) * 2 + 1] - drawPosition.y,
 								p.x, p.y));
 			}
 			i -= step;
			minDistance = Math.min(minDistance,
					calcDistance(
							cache[i * 2] - drawPosition.x, cache[i * 2 + 1] - drawPosition.y,
							cache[(lvl.end - 1) * 2] - drawPosition.x, cache[(lvl.end - 1) * 2 + 1] - drawPosition.y,
							p.x, p.y));
 		}
 		return minDistance;
 	}
 }
