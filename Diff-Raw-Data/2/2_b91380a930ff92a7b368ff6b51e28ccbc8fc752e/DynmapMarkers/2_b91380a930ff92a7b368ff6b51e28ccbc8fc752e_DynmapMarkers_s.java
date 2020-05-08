 package com.hekta.chdynmap.functions;
 
 import java.util.Set;
 
 import com.laytonsmith.abstraction.MCLocation;
 import com.laytonsmith.abstraction.MCWorld;
 import com.laytonsmith.annotations.api;
 import com.laytonsmith.core.CHVersion;
 import com.laytonsmith.core.constructs.CArray;
 import com.laytonsmith.core.constructs.CBoolean;
 import com.laytonsmith.core.constructs.CInt;
 import com.laytonsmith.core.constructs.CDouble;
 import com.laytonsmith.core.constructs.CNull;
 import com.laytonsmith.core.constructs.Construct;
 import com.laytonsmith.core.constructs.CString;
 import com.laytonsmith.core.constructs.Target;
 import com.laytonsmith.core.constructs.CVoid;
 import com.laytonsmith.core.environments.Environment;
 import com.laytonsmith.core.exceptions.ConfigRuntimeException;
 import com.laytonsmith.core.functions.AbstractFunction;
 import com.laytonsmith.core.functions.Exceptions.ExceptionType;
 import com.laytonsmith.core.ObjectGenerator;
 import com.laytonsmith.core.Static;
 import com.laytonsmith.PureUtilities.StringUtils;
 import com.laytonsmith.PureUtilities.Version;
 
 import org.dynmap.markers.AreaMarker;
 import org.dynmap.markers.CircleMarker;
 import org.dynmap.markers.GenericMarker;
 import org.dynmap.markers.Marker;
 import org.dynmap.markers.MarkerDescription;
 import org.dynmap.markers.MarkerIcon;
 import org.dynmap.markers.MarkerSet;
 import org.dynmap.markers.PolyLineMarker;
 
 import static com.hekta.chdynmap.util.CHDynmapAPI.getDynmapAreaMarker;
 import static com.hekta.chdynmap.util.CHDynmapAPI.getDynmapCircleMarker;
 import static com.hekta.chdynmap.util.CHDynmapAPI.getDynmapIcon;
 import static com.hekta.chdynmap.util.CHDynmapAPI.getDynmapIconMarker;
 import static com.hekta.chdynmap.util.CHDynmapAPI.getDynmapMarker;
 import static com.hekta.chdynmap.util.CHDynmapAPI.getDynmapMarkerSet;
 import static com.hekta.chdynmap.util.CHDynmapAPI.getDynmapPolyLineMarker;
 import com.hekta.chdynmap.util.CHDynmapConverters;
 import com.hekta.chdynmap.util.CHDynmapMarkerType;
 
 /*
  *
  * @author Hekta
  */
 public class DynmapMarkers {
 
 	public static String docs() {
 		return "A class of functions to manage the Dynmap markers.";
 	}
 
 	@api
 	public static class dm_all_markers extends AbstractFunction {
 
 		public String getName() {
 			return "dm_all_markers";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{1, 2};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "array {setID, [type]} Returns an associative array containing the ID of all markers in the markerset."
 					+ " If the type is given, only the markers of this type are returne."
 					+ " Type can be one of " + StringUtils.Join(CHDynmapMarkerType.values(), ", ", ", or ", " or ") + ".";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			Static.checkPlugin("dynmap", t);
 			MarkerSet set = getDynmapMarkerSet(args[0].val(), t);
 			CHDynmapMarkerType type;
 			if ((args.length == 1) || (args[1] instanceof CNull)) {
 				type = null;
 			} else {
 				try {
 					type = CHDynmapMarkerType.valueOf(args[1].val().toUpperCase());
 				} catch (Exception exception) {
 					throw new ConfigRuntimeException("Invalid marker type.", ExceptionType.PluginInternalException, t);
 				}
 			}
 			CArray markerList = new CArray(t);
 			if ((type == null) || (type == CHDynmapMarkerType.AREA)) {
 				for (AreaMarker areaMarker : set.getAreaMarkers()) {
 					markerList.push(new CString(areaMarker.getMarkerID(), t));
 				}
 			}
 			if ((type == null) || (type == CHDynmapMarkerType.CIRCLE)) {
 				for (CircleMarker circleMarker : set.getCircleMarkers()) {
 					markerList.push(new CString(circleMarker.getMarkerID(), t));
 				}	
 			}
 			if ((type == null) || (type == CHDynmapMarkerType.ICON)) {
 				for (Marker iconMarker : set.getMarkers()) {
 					markerList.push(new CString(iconMarker.getMarkerID(), t));
 				}	
 			}
 			if ((type == null) || (type == CHDynmapMarkerType.POLYLINE)) {
 				for (PolyLineMarker polyLineMarker : set.getPolyLineMarkers()) {
 					markerList.push(new CString(polyLineMarker.getMarkerID(), t));
 				}	
 			}
 			return markerList;
 		}
 	}
 
 	@api
 	public static class dm_create_marker extends AbstractFunction {
 
 		public String getName() {
 			return "dm_create_marker";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{1, 2};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException, ExceptionType.CastException, ExceptionType.FormatException, ExceptionType.InvalidWorldException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "string {setID, [optionArray]} Creates a marker and returns its ID."
 					+ " ---- The option array is associative and not required, and all its keys are optional."
 					+ " <li>KEY - DEFAULT - DESCRIPTION - COMMENT</li>"
 					+ " <li>center - world spawn - the center of the marker - only for circle markers, world is ignored</li>"
 					+ " <li>corners - world spawn - the corners of the marker - only for area or polyline markers, world is ignored (and also y for area markers)</li>"
 					+ " <li>icon - null - the icon ID of the marker, null for the markerset default icon - only for icon markers</li>"
 					+ " <li>id - random - ID of the marker, must be unique within the set, if null or not given, an unique ID is generated</li>"
 					+ " <li>label - markerID - the label of the marker</li>"
 					+ " <li>label_is_html - false - sets if the label is processing as HTML</li>"
 					+ " <li>location - world spawn - the location of the marker - only for icon markers, world is ignored</li>"
 					+ " <li>persistent - false - sets if the label is persistent (saved and reloaded on restart), the markerset must be persistent - can not be changed later</li>"
 					+ " <li>radius - 0 0 - the radius of the marker - only for circle markers</li>"
 					+ " <li>type - ICON - the type of the marker, can be one of " + StringUtils.Join(CHDynmapMarkerType.values(), ", ", ", or ", " or ") + " - can not be changed later</li>"
 					+ " <li>world - first world - the world of the marker</li>";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			Static.checkPlugin("dynmap", t);
 			MarkerSet set = getDynmapMarkerSet(args[0].val(), t);
 			//create the option array
 			CArray optionArray;
 			if (args.length == 1) {
 				optionArray = new CArray(t);
 			} else {
 				optionArray = Static.getArray(args[1], t);
 			}
 			Set keys = optionArray.keySet();
 			//set optional values
 			//type
 			CHDynmapMarkerType type;
 			if ((!keys.contains("type")) || (optionArray.get("type", t) instanceof CNull)) {
 				type = CHDynmapMarkerType.ICON;
 			} else {
 				try {
 					type = CHDynmapMarkerType.valueOf(optionArray.get("type", t).val().toUpperCase());
 				} catch (Exception exception) {
 					throw new ConfigRuntimeException("Invalid marker type.", ExceptionType.PluginInternalException, t);
 				}
 			}
 			//id
 			String markerID;
 			if (keys.contains("id")) {
 				markerID = optionArray.get("id", t).val();
 				if ((set.findAreaMarker(markerID) != null) || (set.findCircleMarker(markerID) != null) || (set.findMarker(markerID) != null) || (set.findPolyLineMarker(markerID) != null)) {
 					throw new ConfigRuntimeException("\"" + markerID + "\" is already an existing marker.", ExceptionType.PluginInternalException, t);
 				}
 			} else {
 				markerID = null;
 			}
 			//world
 			MCWorld world;
 			if (keys.contains("world")) {
 				world = Static.getServer().getWorld(optionArray.get("world", t).val());
 			} else {
 				world = Static.getServer().getWorlds().get(0);
 			}
 			MCLocation spawnLocation = world.getSpawnLocation();
 			//label
 			String label;
 			if (keys.contains("label")) {
 				label = optionArray.get("label", t).val();
 			} else {
 				label = markerID;
 			}
 			//label_is_html
 			boolean labelIsHTML;
 			if (keys.contains("label_is_html")) {
 				labelIsHTML = Static.getBoolean(optionArray.get("label_is_html", t));
 			} else {
 				labelIsHTML = false;
 			}
 			//persistent
 			boolean isPersistent;
 			if (keys.contains("persistent")) {
 				isPersistent = Static.getBoolean(optionArray.get("persistent", t));
 			} else {
 				isPersistent = false;
 			}
 			//center
 			double centerX;
 			double centerY;
 			double centerZ;
 			MCLocation centerLocation;
 			if (type == CHDynmapMarkerType.CIRCLE) {
 				if (keys.contains("center")) {
 					centerLocation = ObjectGenerator.GetGenerator().location(optionArray.get("center", t), world, t);
 				} else {
 					centerLocation = spawnLocation;
 				}
 				centerX = centerLocation.getX();
 				centerY = centerLocation.getY();
 				centerZ = centerLocation.getZ();
 			} else {
 				centerX = 0;
 				centerY = 0;
 				centerZ = 0;
 			}
 			//corners
 			double[] cornersX;
 			double[] cornersY;
 			double[] cornersZ;
 			if (type == CHDynmapMarkerType.AREA) {
 				if (keys.contains("corners")) {
 					CArray givenCorners = Static.getArray(optionArray.get("corners", t), t);
 					if (givenCorners.inAssociativeMode()) {
 						throw new ConfigRuntimeException("The corners array must not be associative.", ExceptionType.CastException, t);
 					}
 					MCLocation cornerLocation;
 					int size = (int) givenCorners.size();
 					cornersX = new double[size];
 					cornersZ = new double[size];
 					int cornerCount = 0;
 					for (Construct corner : givenCorners.asList()) {
 						cornerLocation = ObjectGenerator.GetGenerator().location(corner, world, t);
 						cornersX[cornerCount] = cornerLocation.getX();
 						cornersZ[cornerCount] = cornerLocation.getZ();
 						cornerCount++;
 					}
 				} else {
 					cornersX = new double[1] ;
 					cornersZ = new double[1] ;
 					cornersX [0] = spawnLocation.getX();
 					cornersZ [0] = spawnLocation.getZ();
 				}
 				cornersY = null;
 			} else if (type == CHDynmapMarkerType.POLYLINE) {
 				if (keys.contains("corners")) {
 					CArray givenCorners = Static.getArray(optionArray.get("corners", t), t);
 					if (givenCorners.inAssociativeMode()) {
 						throw new ConfigRuntimeException("The corners array must not be associative.", ExceptionType.CastException, t);
 					}
 					MCLocation cornerLocation;
 					int size = (int) givenCorners.size();
 					cornersX = new double[size];
 					cornersY = new double[size];
 					cornersZ = new double[size];
 					int cornerCount = 0;
 					for (Construct corner : givenCorners.asList()) {
 						cornerLocation = ObjectGenerator.GetGenerator().location(corner, world, t);
 						cornersX[cornerCount] = cornerLocation.getX();
 						cornersZ[cornerCount] = cornerLocation.getY();
 						cornersZ[cornerCount] = cornerLocation.getZ();
 						cornerCount++;
 					}
 				} else {
 					cornersX = new double[1] ;
 					cornersY = new double[1] ;
 					cornersZ = new double[1] ;
 					cornersX [0] = spawnLocation.getX();
 					cornersY [0] = spawnLocation.getY();
 					cornersZ [0] = spawnLocation.getZ();
 				}
 			} else {
 				cornersX = null;
 				cornersY = null;
 				cornersZ = null;
 			}
 			//icon
 			MarkerIcon icon;
 			if (type == CHDynmapMarkerType.ICON) {
 				if (keys.contains("icon")) {
					icon = getDynmapIcon(optionArray.get("radius", t).val(), t);
 				} else {
 					icon = null;
 				}
 			} else {
 				icon = null;
 			}
 			//location
 			double locationX;
 			double locationY;
 			double locationZ;
 			MCLocation iconLocation;
 			if (type == CHDynmapMarkerType.ICON) {
 				if (keys.contains("location")) {
 					iconLocation = ObjectGenerator.GetGenerator().location(optionArray.get("location", t), world, t);
 				} else {
 					iconLocation = spawnLocation;
 				}
 				locationX = iconLocation.getX();
 				locationY = iconLocation.getY();
 				locationZ = iconLocation.getZ();
 			} else {
 				locationX = 0;
 				locationY = 0;
 				locationZ = 0;
 			}
 			//radius
 			double radiusX;
 			double radiusZ;
 			if (type == CHDynmapMarkerType.CIRCLE) {
 				if (keys.contains("radius")) {
 					CArray radius = Static.getArray(optionArray.get("radius", t), t);
 					radiusX = Static.getDouble(radius.get("x", t), t);
 					radiusZ = Static.getDouble(radius.get("z", t), t);
 				} else {
 					radiusX = 0;
 					radiusZ = 0;
 				}
 			} else {
 				radiusX = 0;
 				radiusZ = 0;
 			}
 			//create the marker
 			GenericMarker marker = null;
 			switch (type) {
 				case AREA:
 					marker = (GenericMarker) set.createAreaMarker(markerID, label, labelIsHTML, world.getName(), cornersX, cornersZ, isPersistent);
 					break;
 				case CIRCLE:
 					marker = (GenericMarker) set.createCircleMarker(markerID, label, labelIsHTML, world.getName(), centerX, centerY, centerZ, radiusX, radiusZ, isPersistent);
 					break;
 				case ICON:
 					marker = (GenericMarker) set.createMarker(markerID, label, labelIsHTML, world.getName(), locationX, locationY, locationZ, icon, isPersistent);
 					break;
 				case POLYLINE:
 					marker = (GenericMarker) set.createPolyLineMarker(markerID, label, labelIsHTML, world.getName(), cornersX, cornersY, cornersZ, isPersistent);
 					break;
 			}
 			if (marker == null) {
 				throw new ConfigRuntimeException("The marker creation failed.", ExceptionType.PluginInternalException, t);
 			}
 			return new CString(marker.getMarkerID(), t);
 		}
 	}
 
 	@api
 	public static class dm_delete_marker extends AbstractFunction {
 
 		public String getName() {
 			return "dm_delete_marker";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{2};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "void {setID, markerID} Deletes a marker in the set.";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			Static.checkPlugin("dynmap", t);
 			getDynmapMarker(args[0].val(), args[1].val(), t).deleteMarker();
 			return new CVoid(t);
 		}
 	}
 
 	@api
 	public static class dm_marker_boosted extends AbstractFunction {
 
 		public String getName() {
 			return "dm_marker_boosted";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{2};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "array {setID, markerID} Returns if the marker resolution is boosted. Only for area and circle markers.";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			Static.checkPlugin("dynmap", t);
 			GenericMarker marker = getDynmapMarker(args[0].val(), args[1].val(), t);
 			if (marker instanceof AreaMarker) {
 				return new CBoolean(((AreaMarker) marker).getBoostFlag(), t);
 			} else if (marker instanceof CircleMarker) {
 				return new CBoolean(((CircleMarker) marker).getBoostFlag(), t);
 			} else if (marker instanceof Marker) {
 				throw new ConfigRuntimeException("Icon markers are not valid here.", ExceptionType.PluginInternalException, t);
 			} else if (marker instanceof PolyLineMarker) {
 				throw new ConfigRuntimeException("Polyline markers are not valid here.", ExceptionType.PluginInternalException, t);
 			} else {
 				throw new ConfigRuntimeException("Unknown marker type! Is the extension up to date?", ExceptionType.PluginInternalException, t);
 			}
 		}
 	}
 
 	@api
 	public static class dm_set_marker_boosted extends AbstractFunction {
 
 		public String getName() {
 			return "dm_set_marker_boosted";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{3};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "void {setID, markerID, boolean} Sets if the marker resolution is boosted. Only for area and circle markers.";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			Static.checkPlugin("dynmap", t);
 			GenericMarker marker = getDynmapMarker(args[0].val(), args[1].val(), t);
 			if (marker instanceof AreaMarker) {
 				((AreaMarker) marker).setBoostFlag(Static.getBoolean(args[2]));
 			} else if (marker instanceof CircleMarker) {
 				((CircleMarker) marker).setBoostFlag(Static.getBoolean(args[2]));
 			} else if (marker instanceof Marker) {
 				throw new ConfigRuntimeException("Icon markers are not valid here.", ExceptionType.PluginInternalException, t);
 			} else if (marker instanceof PolyLineMarker) {
 				throw new ConfigRuntimeException("Polyline markers are not valid here.", ExceptionType.PluginInternalException, t);
 			} else {
 				throw new ConfigRuntimeException("Unknown marker type! Is the extension up to date?", ExceptionType.PluginInternalException, t);
 			}
 			return new CVoid(t);
 		}
 	}
 
 	@api
 	public static class dm_marker_center extends AbstractFunction {
 
 		public String getName() {
 			return "dm_marker_center";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{2};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "array {setID, markerID} Returns the location of the marker center. Only for circle markers.";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			Static.checkPlugin("dynmap", t);
 			CircleMarker marker = getDynmapCircleMarker(args[0].val(), args[1].val(), t);
 			return CHDynmapConverters.getLocationArray(marker.getCenterX(), marker.getCenterY(), marker.getCenterZ(), Static.getServer().getWorld(marker.getWorld()));
 		}
 	}
 
 	@api
 	public static class dm_set_marker_center extends AbstractFunction {
 
 		public String getName() {
 			return "dm_set_marker_center";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{3};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException, ExceptionType.CastException, ExceptionType.FormatException, ExceptionType.InvalidWorldException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "void {setID, markerID, locationArray} Sets the center of a marker. Only for circle markers.";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			Static.checkPlugin("dynmap", t);
 			CircleMarker marker = getDynmapCircleMarker(args[0].val(), args[1].val(), t);
 			MCLocation location = ObjectGenerator.GetGenerator().location(args[2], Static.getServer().getWorld(marker.getWorld()), t);
 			marker.setCenter(location.getWorld().getName(), location.getX(), location.getY(), location.getZ());
 			return new CVoid(t);
 		}
 	}
 
 	@api
 	public static class dm_marker_corners extends AbstractFunction {
 
 		public String getName() {
 			return "dm_marker_corners";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{2};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "array {setID, markerID} Returns the corners location of the marker. Only for area and polyline markers.";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			Static.checkPlugin("dynmap", t);
 			GenericMarker marker = getDynmapMarker(args[0].val(), args[1].val(), t);
 			if (marker instanceof AreaMarker) {
 				AreaMarker areaMarker = (AreaMarker) marker;
 				CArray corners = new CArray(t);
 				MCWorld world = Static.getServer().getWorld(marker.getWorld());
 				for (int cornerCount = 0; cornerCount<areaMarker.getCornerCount(); cornerCount++) {
 					corners.push(CHDynmapConverters.getLocationArray(areaMarker.getCornerX(cornerCount), 0, areaMarker.getCornerZ(cornerCount), world));
 				}
 				return corners;
 			} else if (marker instanceof CircleMarker) {
 				throw new ConfigRuntimeException("Circle markers are not valid here.", ExceptionType.PluginInternalException, t);
 			} else if (marker instanceof Marker) {
 				throw new ConfigRuntimeException("Icon markers are not valid here.", ExceptionType.PluginInternalException, t);
 			} else if (marker instanceof PolyLineMarker) {
 				PolyLineMarker polyLineMarker = (PolyLineMarker) marker;
 				CArray corners = new CArray(t);
 				MCWorld world = Static.getServer().getWorld(marker.getWorld());
 				for (int cornerCount = 0; cornerCount<polyLineMarker.getCornerCount(); cornerCount++) {
 					corners.push(CHDynmapConverters.getLocationArray(polyLineMarker.getCornerX(cornerCount), polyLineMarker.getCornerY(cornerCount), polyLineMarker.getCornerZ(cornerCount), world));
 				}
 				return corners;
 			} else {
 				throw new ConfigRuntimeException("Unknown marker type! Is the extension up to date?", ExceptionType.PluginInternalException, t);
 			}
 		}
 	}
 
 	@api
 	public static class dm_set_marker_corners extends AbstractFunction {
 
 		public String getName() {
 			return "dm_set_marker_corners";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{3};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException, ExceptionType.CastException, ExceptionType.FormatException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "void {setID, markerID, array} Sets the location of the marker corners (array of location arrays, world is ignored, and for area markers y is ignored). Only for area and polyline markers.";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			Static.checkPlugin("dynmap", t);
 			GenericMarker marker = getDynmapMarker(args[0].val(), args[1].val(), t);
 			if (marker instanceof AreaMarker) {
 				CArray givenLocations = Static.getArray(args[2], t);
 				if (givenLocations.inAssociativeMode()) {
 					throw new ConfigRuntimeException("The array must not be associative.", ExceptionType.CastException, t);
 				}
 				MCLocation location;
 				MCWorld world = Static.getServer().getWorld(marker.getWorld());
 				int size = (int) givenLocations.size();
 				double[] Xs = new double[size];
 				double[] Zs = new double[size];
 				int cornerCount = 0;
 				for (Construct corner : givenLocations.asList()) {
 					location = ObjectGenerator.GetGenerator().location(corner, world, t);
 					Xs[cornerCount] = location.getX();
 					Zs[cornerCount] = location.getZ();
 					cornerCount++;
 				}
 				((AreaMarker) marker).setCornerLocations(Xs, Zs);
 			} else if (marker instanceof CircleMarker) {
 				throw new ConfigRuntimeException("Circle markers are not valid here.", ExceptionType.PluginInternalException, t);
 			} else if (marker instanceof Marker) {
 				throw new ConfigRuntimeException("Icon markers are not valid here.", ExceptionType.PluginInternalException, t);
 			} else if (marker instanceof PolyLineMarker) {
 				CArray givenLocations = Static.getArray(args[2], t);
 				if (givenLocations.inAssociativeMode()) {
 					throw new ConfigRuntimeException("The array must not be associative.", ExceptionType.CastException, t);
 				}
 				MCLocation location;
 				MCWorld world = Static.getServer().getWorld(marker.getWorld());
 				int size = (int) givenLocations.size();
 				double[] Xs = new double[size];
 				double[] Ys = new double[size];
 				double[] Zs = new double[size];
 				int cornerCount = 0;
 				for (Construct corner : givenLocations.asList()) {
 					location = ObjectGenerator.GetGenerator().location(corner, world, t);
 					Xs[cornerCount] = location.getX();
 					Ys[cornerCount] = location.getY();
 					Zs[cornerCount] = location.getZ();
 					cornerCount++;
 				}
 				((PolyLineMarker) marker).setCornerLocations(Xs, Ys, Zs);
 			} else {
 				throw new ConfigRuntimeException("Unknown marker type! Is the extension up to date?", ExceptionType.PluginInternalException, t);
 			}
 			return new CVoid(t);
 		}
 	}
 
 	@api
 	public static class dm_marker_description extends AbstractFunction {
 
 		public String getName() {
 			return "dm_marker_label";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{2};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "string {setID, markerID} Returns the description of the marker.";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			Static.checkPlugin("dynmap", t);
 			GenericMarker marker = getDynmapMarker(args[0].val(), args[1].val(), t);
 			if (marker instanceof AreaMarker) {
 				return new CString(((AreaMarker) marker).getDescription(), t);
 			} else if (marker instanceof CircleMarker) {
 				return new CString(((CircleMarker) marker).getDescription(), t);
 			} else if (marker instanceof Marker) {
 				return new CString(((Marker) marker).getDescription(), t);
 			} else if (marker instanceof PolyLineMarker) {
 				return new CString(((PolyLineMarker) marker).getDescription(), t);
 			} else {
 				throw new ConfigRuntimeException("Unknown marker type! Is the extension up to date?", ExceptionType.PluginInternalException, t);
 			}
 		}
 	}
 
 	@api
 	public static class dm_set_marker_description extends AbstractFunction {
 
 		public String getName() {
 			return "dm_set_marker_description";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{3};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "void {setID, markerID, htmlDescription} Sets the description of the marker (in HTML).";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			Static.checkPlugin("dynmap", t);
 			GenericMarker marker = getDynmapMarker(args[0].val(), args[1].val(), t);
 			if (marker instanceof AreaMarker) {
 				((AreaMarker) marker).setDescription(args[2].val());
 			} else if (marker instanceof CircleMarker) {
 				((CircleMarker) marker).setDescription(args[2].val());
 			} else if (marker instanceof Marker) {
 				((Marker) marker).setDescription(args[2].val());
 			} else if (marker instanceof PolyLineMarker) {
 				((PolyLineMarker) marker).setDescription(args[2].val());
 			} else {
 				throw new ConfigRuntimeException("Unknown marker type! Is the extension up to date?", ExceptionType.PluginInternalException, t);
 			}
 			return new CVoid(t);
 		}
 	}
 
 	@api
 	public static class dm_marker_fill_style extends AbstractFunction {
 
 		public String getName() {
 			return "dm_marker_fill_style";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{2};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "array {setID, markerID} Returns the fill style array of the marker. Only for area and circle markers.";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			Static.checkPlugin("dynmap", t);
 			GenericMarker marker = getDynmapMarker(args[0].val(), args[1].val(), t);
 			if (marker instanceof AreaMarker) {
 				AreaMarker areaMarker = (AreaMarker) marker;
 				CArray fillStyle = new CArray(t);
 				fillStyle.set("color", CHDynmapConverters.getColorArray(areaMarker.getFillColor(), t), t);
 				fillStyle.set("opacity", new CDouble(areaMarker.getFillOpacity(), t), t);
 				return fillStyle;
 			} else if (marker instanceof CircleMarker) {
 				CircleMarker circleMarker = (CircleMarker) marker;
 				CArray fillStyle = new CArray(t);
 				fillStyle.set("color", CHDynmapConverters.getColorArray(circleMarker.getFillColor(), t), t);
 				fillStyle.set("opacity", new CDouble(circleMarker.getFillOpacity(), t), t);
 				return fillStyle;
 			} else if (marker instanceof Marker) {
 				throw new ConfigRuntimeException("Icon markers are not valid here.", ExceptionType.PluginInternalException, t);
 			} else if (marker instanceof PolyLineMarker) {
 				throw new ConfigRuntimeException("Polyline markers are not valid here.", ExceptionType.PluginInternalException, t);
 			} else {
 				throw new ConfigRuntimeException("Unknown marker type! Is the extension up to date?", ExceptionType.PluginInternalException, t);
 			}
 		}
 	}
 
 	@api
 	public static class dm_set_marker_fill_style extends AbstractFunction {
 
 		public String getName() {
 			return "dm_set_marker_fill_style";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{3};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException, ExceptionType.CastException, ExceptionType.FormatException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "void {setID, markerID, array} Sets the marker fill style (array with \"color\" and \"opacity\" optional keys, color is a color r g b array, and opacity a number between 0 and 1 inclusive)."
 					+ " Only for area and circle markers";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			Static.checkPlugin("dynmap", t);
 			GenericMarker marker = getDynmapMarker(args[0].val(), args[1].val(), t);
 			if (marker instanceof AreaMarker) {
 				AreaMarker areaMarker = (AreaMarker) marker;
 				CArray fillStyle = Static.getArray(args[2], t);
 				Set keys = fillStyle.keySet();
 				int color;
 				if (keys.contains("color")) {
 					color = CHDynmapConverters.getColorInt(fillStyle.get("color", t), t);
 				} else {
 					color = areaMarker.getFillColor();
 				}
 				double opacity;
 				if (keys.contains("opacity")) {
 					opacity = Static.getDouble(fillStyle.get("opacity", t), t);
 					if ((opacity < 0) || (opacity > 1)) {
 						throw new ConfigRuntimeException("Opacity must be between 0 and 1 inclusive.", ExceptionType.FormatException, t);
 					}
 				} else {
 					opacity = areaMarker.getFillOpacity();
 				}
 				areaMarker.setFillStyle(opacity, color);
 			} else if (marker instanceof CircleMarker) {
 				CircleMarker circleMarker = (CircleMarker) marker;
 				CArray fillStyle = Static.getArray(args[2], t);
 				Set keys = fillStyle.keySet();
 				int color;
 				if (keys.contains("color")) {
 					color = CHDynmapConverters.getColorInt(fillStyle.get("color", t), t);
 				} else {
 					color = circleMarker.getFillColor();
 				}
 				double opacity;
 				if (keys.contains("opacity")) {
 					opacity = Static.getDouble(fillStyle.get("opacity", t), t);
 					if ((opacity < 0) || (opacity > 1)) {
 						throw new ConfigRuntimeException("Opacity must be between 0 and 1 inclusive.", ExceptionType.FormatException, t);
 					}
 				} else {
 					opacity = circleMarker.getFillOpacity();
 				}
 				circleMarker.setFillStyle(opacity, color);
 			} else if (marker instanceof Marker) {
 				throw new ConfigRuntimeException("Icon markers are not valid here.", ExceptionType.PluginInternalException, t);
 			} else if (marker instanceof PolyLineMarker) {
 				throw new ConfigRuntimeException("Polyline markers are not valid here.", ExceptionType.PluginInternalException, t);
 			} else {
 				throw new ConfigRuntimeException("Unknown marker type! Is the extension up to date?", ExceptionType.PluginInternalException, t);
 			}
 			return new CVoid(t);
 		}
 	}
 
 	@api
 	public static class dm_marker_icon extends AbstractFunction {
 
 		public String getName() {
 			return "dm_marker_icon";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{2};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "string {setID, markerID} Returns the icon ID of the marker. Only for icon markers.";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			Static.checkPlugin("dynmap", t);
 			return new CString(getDynmapIconMarker(args[0].val(), args[1].val(), t).getMarkerIcon().getMarkerIconID(), t);
 		}
 	}
 
 	@api
 	public static class dm_set_marker_icon extends AbstractFunction {
 
 		public String getName() {
 			return "dm_set_marker_icon";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{3};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "void {setID, markerID, iconID} Sets the icon of a marker. Only for icon markers.";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			Static.checkPlugin("dynmap", t);
 			Marker marker = getDynmapIconMarker(args[0].val(), args[1].val(), t);
 			MarkerIcon icon = getDynmapIcon(args[2].val(), t);
 			if (marker.getMarkerSet().isAllowedMarkerIcon(icon)) {
 				marker.setMarkerIcon(icon);
 			} else {
 				throw new ConfigRuntimeException("The icon is not allowed for the markerset.", ExceptionType.PluginInternalException, t);
 			}
 			return new CVoid(t);
 		}
 	}
 
 	@api
 	public static class dm_marker_label extends AbstractFunction {
 
 		public String getName() {
 			return "dm_marker_label";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{2};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException, ExceptionType.CastException, ExceptionType.FormatException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "string {setID, markerID} Returns the label of the marker.";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			Static.checkPlugin("dynmap", t);
 			return new CString(getDynmapMarker(args[0].val(), args[1].val(), t).getLabel(), t);
 		}
 	}
 
 	@api
 	public static class dm_marker_label_is_html extends AbstractFunction {
 
 		public String getName() {
 			return "dm_marker_label_is_html";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{2};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException, ExceptionType.CastException, ExceptionType.FormatException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "boolean {setID, markerID} Returns if the label of the marker is processed as HTML.";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			Static.checkPlugin("dynmap", t);
 			return new CBoolean(getDynmapMarker(args[0].val(), args[1].val(), t).isLabelMarkup(), t);
 		}
 	}
 
 	@api
 	public static class dm_set_marker_label extends AbstractFunction {
 
 		public String getName() {
 			return "dm_set_marker_label";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{3, 4};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "void {setID, markerID, label, [isHTML]} Sets the label of the marker, isHTML is a boolean, if true, label will be processed as HTML.";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			Static.checkPlugin("dynmap", t);
 			GenericMarker marker = getDynmapMarker(args[0].val(), args[1].val(), t);
 			boolean isHTML;
 			if (args.length == 3) {
 				isHTML = false;
 			} else {
 				isHTML = Static.getBoolean(args[3]);
 			}
 			marker.setLabel(args[2].val(), isHTML);
 			return new CVoid(t);
 		}
 	}
 
 	@api
 	public static class dm_marker_line_style extends AbstractFunction {
 
 		public String getName() {
 			return "dm_marker_line_style";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{2};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "array {setID, markerID} Returns the line style array of the marker. Only for area, circle and polyline markers.";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			Static.checkPlugin("dynmap", t);
 			GenericMarker marker = getDynmapMarker(args[0].val(), args[1].val(), t);
 			if (marker instanceof AreaMarker) {
 				AreaMarker areaMarker = (AreaMarker) marker;
 				CArray lineStyle = new CArray(t);
 				lineStyle.set("color", CHDynmapConverters.getColorArray(areaMarker.getLineColor(), t), t);
 				lineStyle.set("opacity", new CDouble(areaMarker.getLineOpacity(), t), t);
 				lineStyle.set("weight", new CInt(areaMarker.getLineWeight(), t), t);
 				return lineStyle;
 			} else if (marker instanceof CircleMarker) {
 				CircleMarker circleMarker = (CircleMarker) marker;
 				CArray lineStyle = new CArray(t);
 				lineStyle.set("color", CHDynmapConverters.getColorArray(circleMarker.getLineColor(), t), t);
 				lineStyle.set("opacity", new CDouble(circleMarker.getLineOpacity(), t), t);
 				lineStyle.set("weight", new CInt(circleMarker.getLineWeight(), t), t);
 				return lineStyle;
 			} else if (marker instanceof Marker) {
 				throw new ConfigRuntimeException("Icon markers are not valid here.", ExceptionType.PluginInternalException, t);
 			} else if (marker instanceof PolyLineMarker) {
 				PolyLineMarker polyLineMarker = (PolyLineMarker) marker;
 				CArray lineStyle = new CArray(t);
 				lineStyle.set("color", CHDynmapConverters.getColorArray(polyLineMarker.getLineColor(), t), t);
 				lineStyle.set("opacity", new CDouble(polyLineMarker.getLineOpacity(), t), t);
 				lineStyle.set("weight", new CInt(polyLineMarker.getLineWeight(), t), t);
 				return lineStyle;
 			} else {
 				throw new ConfigRuntimeException("Unknown marker type! Is the extension up to date?", ExceptionType.PluginInternalException, t);
 			}
 		}
 	}
 
 	@api
 	public static class dm_set_marker_line_style extends AbstractFunction {
 
 		public String getName() {
 			return "dm_set_marker_line_style";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{3};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException, ExceptionType.CastException, ExceptionType.FormatException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "void {setID, markerID, array} Sets the marker line style (array with \"color\", \"opacity\" and \"weight\" optional keys, color is a color r g b array, opacity a number between 0 and 1 inclusive and weight is an integer)."
 					+ " Only for area, circle and polyline markers.";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			Static.checkPlugin("dynmap", t);
 			GenericMarker marker = getDynmapMarker(args[0].val(), args[1].val(), t);
 			if (marker instanceof AreaMarker) {
 				AreaMarker areaMarker = (AreaMarker) marker;
 				CArray lineStyle = Static.getArray(args[2], t);
 				Set keys = lineStyle.keySet();
 				int color;
 				if (keys.contains("color")) {
 					color = CHDynmapConverters.getColorInt(lineStyle.get("color", t), t);
 				} else {
 					color = areaMarker.getLineColor();
 				}
 				double opacity;
 				if (keys.contains("opacity")) {
 					opacity = Static.getDouble(lineStyle.get("opacity", t), t);
 					if ((opacity < 0) || (opacity > 1)) {
 						throw new ConfigRuntimeException("Opacity must be between 0 and 1 inclusive.", ExceptionType.FormatException, t);
 					}
 				} else {
 					opacity = areaMarker.getLineOpacity();
 				}
 				int weight;
 				if (keys.contains("weight")) {
 					weight = Static.getInt32(lineStyle.get("weight", t), t);
 				} else {
 					weight = areaMarker.getLineWeight();
 				}
 				areaMarker.setLineStyle(weight, opacity, color);
 			} else if (marker instanceof CircleMarker) {
 				CircleMarker circleMarker = (CircleMarker) marker;
 				CArray lineStyle = Static.getArray(args[2], t);
 				Set keys = lineStyle.keySet();
 				int color;
 				if (keys.contains("color")) {
 					color = CHDynmapConverters.getColorInt(lineStyle.get("color", t), t);
 				} else {
 					color = circleMarker.getLineColor();
 				}
 				double opacity;
 				if (keys.contains("opacity")) {
 					opacity = Static.getDouble(lineStyle.get("opacity", t), t);
 					if ((opacity < 0) || (opacity > 1)) {
 						throw new ConfigRuntimeException("Opacity must be between 0 and 1 inclusive.", ExceptionType.FormatException, t);
 					}
 				} else {
 					opacity = circleMarker.getLineOpacity();
 				}
 				int weight;
 				if (keys.contains("weight")) {
 					weight = Static.getInt32(lineStyle.get("weight", t), t);
 				} else {
 					weight = circleMarker.getLineWeight();
 				}
 				circleMarker.setLineStyle(weight, opacity, color);
 			} else if (marker instanceof Marker) {
 				throw new ConfigRuntimeException("Icon markers are not valid here.", ExceptionType.PluginInternalException, t);
 			} else if (marker instanceof PolyLineMarker) {
 				PolyLineMarker polyLineMarker = (PolyLineMarker) marker;
 				CArray lineStyle = Static.getArray(args[2], t);
 				Set keys = lineStyle.keySet();
 				int color;
 				if (keys.contains("color")) {
 					color = CHDynmapConverters.getColorInt(lineStyle.get("color", t), t);
 				} else {
 					color = polyLineMarker.getLineColor();
 				}
 				double opacity;
 				if (keys.contains("opacity")) {
 					opacity = Static.getDouble(lineStyle.get("opacity", t), t);
 					if ((opacity < 0) || (opacity > 1)) {
 						throw new ConfigRuntimeException("Opacity must be between 0 and 1 inclusive.", ExceptionType.FormatException, t);
 					}
 				} else {
 					opacity = polyLineMarker.getLineOpacity();
 				}
 				int weight;
 				if (keys.contains("weight")) {
 					weight = Static.getInt32(lineStyle.get("weight", t), t);
 				} else {
 					weight = polyLineMarker.getLineWeight();
 				}
 				polyLineMarker.setLineStyle(weight, opacity, color);
 			} else {
 				throw new ConfigRuntimeException("Unknown marker type! Is the extension up to date?", ExceptionType.PluginInternalException, t);
 			}
 			return new CVoid(t);
 		}
 	}
 
 	@api
 	public static class dm_marker_loc extends AbstractFunction {
 
 		public String getName() {
 			return "dm_marker_loc";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{2};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "string {setID, markerID} Returns the location of the marker. Only for icon markers.";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			Static.checkPlugin("dynmap", t);
 			Marker marker = getDynmapIconMarker(args[0].val(), args[1].val(), t);
 			return CHDynmapConverters.getLocationArray(marker.getX(), marker.getY(), marker.getZ(), Static.getServer().getWorld(marker.getWorld()));
 		}
 	}
 
 	@api
 	public static class dm_set_marker_loc extends AbstractFunction {
 
 		public String getName() {
 			return "dm_set_marker_loc";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{3};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException, ExceptionType.CastException, ExceptionType.FormatException, ExceptionType.InvalidWorldException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "void {setID, markerID, locationArray} Sets the icon location of a marker. Only for icon markers.";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			Static.checkPlugin("dynmap", t);
 			Marker marker = getDynmapIconMarker(args[0].val(), args[1].val(), t);
 			MCLocation location = ObjectGenerator.GetGenerator().location(args[2], Static.getServer().getWorld(marker.getWorld()), t);
 			marker.setLocation(location.getWorld().getName(), location.getX(), location.getY(), location.getZ());
 			return new CVoid(t);
 		}
 	}
 
 	@api
 	public static class dm_set_marker_markerset extends AbstractFunction {
 
 		public String getName() {
 			return "dm_set_marker_markerset";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{3};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "void {setID, markerID, newSetID} Changes the markerset of the marker.";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			Static.checkPlugin("dynmap", t);
 			GenericMarker marker = getDynmapMarker(args[0].val(), args[1].val(), t);
 			MarkerSet newSet = getDynmapMarkerSet(args[2].val(), t);
 			if (getDynmapMarker(newSet.getMarkerSetID(), marker.getMarkerID(), t) != null) {
 				throw new ConfigRuntimeException("An other marker with the same ID already exists in the new markerset.", ExceptionType.PluginInternalException, t);
 			}
 			marker.setMarkerSet(newSet);
 			return new CVoid(t);
 		}
 	}
 
 	@api
 	public static class dm_marker_normalized_world extends AbstractFunction {
 
 		public String getName() {
 			return "dm_marker_normalized_world";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{2};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "string {setID, markerID} Returns the normalized world of the marker (used for directory and URL names in Dynmap).";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			Static.checkPlugin("dynmap", t);
 			return new CString(getDynmapMarker(args[0].val(), args[1].val(), t).getNormalizedWorld(), t);
 		}
 	}
 
 	@api
 	public static class dm_marker_persistent extends AbstractFunction {
 
 		public String getName() {
 			return "dm_marker_persistent";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{2};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "boolean {setID, markerID} Returns if the marker is persistent.";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			Static.checkPlugin("dynmap", t);
 			return new CBoolean(getDynmapMarker(args[0].val(), args[1].val(), t).isPersistentMarker(), t);
 		}
 	}
 
 	@api
 	public static class dm_marker_radius extends AbstractFunction {
 
 		public String getName() {
 			return "dm_marker_radius";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{2};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "array {setID, markerID} Returns the radius of the marker. Only for circle markers.";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			Static.checkPlugin("dynmap", t);
 			CircleMarker marker = getDynmapCircleMarker(args[0].val(), args[1].val(), t);
 			CArray radius = new CArray(t);
 			radius.set("x", new CDouble(marker.getRadiusX(), t), t);
 			radius.set("z", new CDouble(marker.getRadiusZ(), t), t);
 			return radius;
 		}
 	}
 
 	@api
 	public static class dm_set_marker_radius extends AbstractFunction {
 
 		public String getName() {
 			return "dm_set_marker_radius";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{3};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException, ExceptionType.CastException, ExceptionType.FormatException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "void {setID, markerID, array} Sets the radius of the marker (array with \"x\" and \"z\" keys). Only for circle markers.";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			Static.checkPlugin("dynmap", t);
 			CircleMarker marker = getDynmapCircleMarker(args[0].val(), args[1].val(), t);
 			CArray radius = Static.getArray(args[2], t);
 			marker.setRadius(Static.getDouble(radius.get("x", t), t), Static.getDouble(radius.get("z", t), t));
 			return new CVoid(t);
 		}
 	}
 
 	@api
 	public static class dm_marker_range_height extends AbstractFunction {
 
 		public String getName() {
 			return "dm_marker_range_height";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{2};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "array {setID, markerID} Returns the range height of the marker. Only for area markers.";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			Static.checkPlugin("dynmap", t);
 			AreaMarker marker = getDynmapAreaMarker(args[0].val(), args[1].val(), t);
 			CArray range = new CArray(t);
 			range.set("bottom", new CDouble(marker.getBottomY(), t), t);
 			range.set("top", new CDouble(marker.getTopY(), t), t);
 			return range;
 		}
 	}
 
 	@api
 	public static class dm_set_marker_range_height extends AbstractFunction {
 
 		public String getName() {
 			return "dm_set_marker_range_height";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{3};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException, ExceptionType.CastException, ExceptionType.FormatException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "void {setID, markerID, array} Sets the range height of a marker (array with \"top\" and \"bottom\" keys). Only for area markers.";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			Static.checkPlugin("dynmap", t);
 			AreaMarker marker = getDynmapAreaMarker(args[0].val(), args[1].val(), t);
 			CArray range = Static.getArray(args[2], t);
 			marker.setRangeY(Static.getDouble(range.get("top", t), t), Static.getDouble(range.get("bottom", t), t));
 			return new CVoid(t);
 		}
 	}
 
 	@api
 	public static class dm_marker_type extends AbstractFunction {
 
 		public String getName() {
 			return "dm_marker_type";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{2};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "string {setID, markerID} Returns the type of the marker. Can be one of " + StringUtils.Join(CHDynmapMarkerType.values(), ", ", ", ", " or ") + ", or UNKNOWN.";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			Static.checkPlugin("dynmap", t);
 			GenericMarker marker = getDynmapMarker(args[0].val(), args[1].val(), t);
 			if (marker instanceof AreaMarker) {
 				return new CString("AREA", t);
 			} else if (marker instanceof CircleMarker) {
 				return new CString("CIRCLE", t);
 			} else if (marker instanceof Marker) {
 				return new CString("ICON", t);
 			} else if (marker instanceof PolyLineMarker) {
 				return new CString("POLYLINE", t);
 			} else {
 				return new CString("UNKNOWN", t);
 			}
 		}
 	}
 
 	@api
 	public static class dm_marker_world extends AbstractFunction {
 
 		public String getName() {
 			return "dm_marker_world";
 		}
 
 		public Integer[] numArgs() {
 			return new Integer[]{2};
 		}
 
 		public ExceptionType[] thrown() {
 			return new ExceptionType[]{ExceptionType.InvalidPluginException, ExceptionType.PluginInternalException};
 		}
 
 		public boolean isRestricted() {
 			return true;
 		}
 
 		public Boolean runAsync() {
 			return false;
 		}
 
 		public String docs() {
 			return "string {setID, markerID} Returns the world of the marker.";
 		}
 
 		public Version since() {
 			return CHVersion.V3_3_1;
 		}
 
 		public Construct exec(Target t, Environment environment, Construct... args) throws ConfigRuntimeException {
 			Static.checkPlugin("dynmap", t);
 			return new CString(Static.getServer().getWorld(getDynmapMarker(args[0].val(), args[1].val(), t).getWorld()).getName(), t);
 		}
 	}
 }
