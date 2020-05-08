 package com.dunnkers.pathmaker.util;
 
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * 
  * @author Dunnkers
  */
 public enum CodeFormat {
 	OSBOT("OSBot", true, WorldMap.OLD_SCHOOL), 
 	TRIBOT_OLD_SCHOOL("TRiBot", true, WorldMap.OLD_SCHOOL),
 	TRIBOT_RECENT("TRiBot", true, WorldMap.RECENT), 
 	RSBOT("RSBot", true, WorldMap.RECENT);
 
 	private final String name;
 	private final List<WorldMap> worldMaps;
 	private final boolean enabled;
 
 	private static final String DEFAULT_TEXT = "Not supported yet!";
 
 	private CodeFormat(String name, boolean enabled,
 			final WorldMap... worldMaps) {
 		this.name = name;
 		this.worldMaps = Arrays.asList(worldMaps);
 		this.enabled = enabled;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public boolean isEnabled() {
 		return enabled;
 	}
 
 	public String getCode(final ArrayList<Point> tileArray,
 			final TileMode tileMode) {
 		StringBuilder output = new StringBuilder(200);
 		/*
 		 * TODO convert to tile here, and store tile array as mouse points: more
 		 * efficient in drawing paint
 		 */
 		switch (tileMode) {
 		case PATH:
 			output.append(getPath());
 			output.append(getFormattedTiles(tileArray));
			output.append("\t};");
 			break;
 		case AREA:
 			output.append(getArea(tileArray));
 			switch (this) {
 			default:
 				output.append(getFormattedTiles(tileArray));
 				break;
 			}
 			output.append("\t);");
 			break;
 		default:
 			output.append(DEFAULT_TEXT);
 			break;
 		}
 		return output.toString();
 	}
 
 	private String getPath() {
 		switch (this) {
 		case RSBOT:
 			return "\tprivate final Tile[] path = new Tile[] {\n";
 		case OSBOT:
 			return "\tprivate final Position[] path = new Position[] {\n";
 		case TRIBOT_OLD_SCHOOL:
 			return "\tprivate final RSTile[] path = new RSTile[] {\n";
 		default:
 			return DEFAULT_TEXT;
 		}
 	}
 
 	private String getArea(final ArrayList<Point> tileArray) {
 		switch (this) {
 		case RSBOT:
 		case OSBOT:
 			return "\tprivate final Area area = new Area(\n";
 		case TRIBOT_OLD_SCHOOL:
 			return "\tprivate final RSArea area = new RSArea(\n";
 		default:
 			return DEFAULT_TEXT;
 		}
 	}
 
 	private String getTile(final Point point) {
 		return String.format(getTileFormat(), point.x, point.y);
 	}
 	
 	private String getTileFormat() {
 		switch (this) {
 		case RSBOT:
 			return "new Tile(%s, %s, 0)";
 		case OSBOT:
 			return "new Position(%s, %s, 0)";
 		case TRIBOT_OLD_SCHOOL:
 			return "new RSTile(%s, %s, 0)";
 		default:
 			return DEFAULT_TEXT;
 		}
 	}
 	
 	private String getFormattedTiles(final ArrayList<Point> tileArray) {
 		StringBuilder output = new StringBuilder(200);
 		for (int i = 0; i < tileArray.size(); i++) {
 			final boolean lastTile = tileArray.size() - 1 == i;
 			output.append("\t\t\t" + getTile(tileArray.get(i))
 					+ (lastTile ? "" : ",") + "\n");
 		}
 		return output.toString();
 	}
 
 	public List<WorldMap> getWorldMaps() {
 		return worldMaps;
 	}
 }
