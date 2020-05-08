 /*
  * Copyright (C) 2011 Alasdair Morrison <amorri40@gmail.com>
  * Copyright (C) 2008, 2009 IsmAvatar <IsmAvatar@gmail.com>
  * 
  * This file is part of EnigmaJS Plugin.
  * 
  * EnigmaJS Plugin is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * EnigmaJS Plugin is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License (COPYING) for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.jsenigma.core;
 
 import static org.lateralgm.main.Util.deRef;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.Toolkit;
 import java.awt.image.BufferedImage;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.nio.ByteBuffer;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.EnumMap;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.Map;
 
 import javax.imageio.ImageIO;
 import javax.script.ScriptEngine;
 import javax.script.ScriptEngineManager;
 import javax.script.ScriptException;
 import javax.swing.JOptionPane;
 
 import org.lateralgm.components.impl.ResNode;
 import org.lateralgm.file.GmFile;
 import org.lateralgm.main.LGM;
 import org.lateralgm.resources.ResourceReference;
 import org.lateralgm.resources.Sprite;
 import org.lateralgm.resources.Background.PBackground;
 import org.lateralgm.resources.Font.PFont;
 import org.lateralgm.resources.GmObject.PGmObject;
 import org.lateralgm.resources.Path.PPath;
 import org.lateralgm.resources.Room.PRoom;
 import org.lateralgm.resources.Script.PScript;
 import org.lateralgm.resources.Sound.PSound;
 import org.lateralgm.resources.Sound.SoundKind;
 import org.lateralgm.resources.Sprite.PSprite;
 import org.lateralgm.resources.library.LibAction;
 import org.lateralgm.resources.sub.Action;
 import org.lateralgm.resources.sub.ActionContainer;
 import org.lateralgm.resources.sub.Argument;
 import org.lateralgm.resources.sub.Event;
 import org.lateralgm.resources.sub.BackgroundDef.PBackgroundDef;
 import org.lateralgm.resources.sub.Instance.PInstance;
 import org.lateralgm.resources.sub.Tile.PTile;
 import org.lateralgm.resources.sub.View.PView;
 
 
 public class JSEnigmaWriter {
 
 	protected static GmFile i;
 	public static ScriptEngineManager factory;
 	public static ScriptEngine engine;
 	private static String[] jsfiles = { "/Main/system.js","/Graphics/Canvas/Canvasmain.js", "/Graphics/Canvas/Canvasdrawing.js", "/Parser/parse_basics.js",
 			"/Parser/parse_system.js", "/Parser/parser_tgmg.js", "/Parser/parser.js", "/Platform/dialog.js", "/Main/object.js",
 			"/Universal/actions.js", "/Universal/math.js", "/Universal/input.js" };
 	private static String eventname;
 	private static String currentObject;
 
 	public static void initJavascript() throws FileNotFoundException
 			 {
 		factory = new ScriptEngineManager();
 		engine = factory.getEngineByName("JavaScript");
 		int i=0;
 		try {
 		for (i = 0; i < jsfiles.length; i++) {
 			engine.eval(new java.io.FileReader("./EnigmaJSLibrary"
 					+ jsfiles[i]));
 		}
 		} catch (ScriptException e) {
 			
 			System.out.println("File:"+jsfiles[i]+e.getMessage());
 			e.printStackTrace();
 		}
 	}
 
 	public static void writeResources() throws Exception {
 		i = LGM.currentFile;
 		root = LGM.root;
 		System.out.println("Starting convert to html5!");
 		numberOfErrors = 0;
 		initJavascript();
 		BufferedWriter loadingfile = new BufferedWriter(new FileWriter(
 				"./EnigmaJSLibrary/ide_edit/ideedit_loading.js"));
 		populateSprites(loadingfile);
 		populateSounds(loadingfile);
 		populateBackgrounds(loadingfile);
 		populatePaths(loadingfile);
 		populateScripts(loadingfile);
 		populateFonts(loadingfile);
 		populateTimelines(loadingfile);
 		populateObjects(loadingfile);
 		populateRooms(loadingfile);
 		loadingfile.close();
 		engine.eval("missing=getMissingFunctions();");
 		engine.eval("output=writeEnigmaJSOutputFile();");
 		System.out.println(engine.get("missing").toString());
 		
 		BufferedWriter outputfile = new BufferedWriter(new FileWriter(
 		"./enigmajsoutput.txt"));
 		outputfile.write(numberOfErrors+"\n");
 		outputfile.write(engine.get("output").toString());
 		outputfile.close();
 		
 		
 		if (numberOfErrors > 0)
 			System.out.println("Failed to convert with " + numberOfErrors
 					+ " errors");
 		else
 			System.out.println("Convert succesful");
 	}
 
 	private static void populateTimelines(BufferedWriter loadingfile)
 			throws IOException, ScriptException {
 		int size = i.timelines.size();
 
 		if (size == 0)
 			return;
 
 		org.lateralgm.resources.Timeline[] itl = i.timelines
 				.toArray(new org.lateralgm.resources.Timeline[0]);
 		for (int t = 0; t < size; t++) {
 			org.lateralgm.resources.Timeline it = itl[t];
 			loadingfile.write("timelineid_" + it.getId() + "=" + it.getName()
 					+ "=function() {");
 
 			int nomo = it.moments.size();
 			if (nomo == 0) {
 				loadingfile.write("};\n");
 				continue;
 			}
 			loadingfile.write(" switch(this.timeline_position) {");
 			for (int m = 0; m < nomo; m++) {
 				loadingfile.write(" case " + it.moments.get(m).stepNo + " :");
 				String code = getActionsCode(it.moments.get(m));
 				loadingfile.write(" {" + convertCode(code) + "} break;\n");
 			}
 			loadingfile.write("}};\n");
 		}
 	}
 
 	private static void populateFonts(BufferedWriter loadingfile)
 			throws IOException {
 		int size = i.fonts.size();
 
 		if (size == 0) {
 			loadingfile.write("var fontstructarray = {}; //no fonts \n");
 			return;
 		}
 		loadingfile.write("var fontstructarray = {");
 
 		org.lateralgm.resources.Font[] ifl = i.fonts
 				.toArray(new org.lateralgm.resources.Font[0]);
 		for (int f = 0; f < size; f++) {
 			org.lateralgm.resources.Font ifont = ifl[f];
 
 			if (f != 0)
 				loadingfile.write(",");
 
 			loadingfile.write(ifont.getId() + ":{name:\"" + ifont.getName()
 					+ "\",id:" + ifont.getId());
 			loadingfile.write(",fontName:\"" + ifont.get(PFont.FONT_NAME)
 					+ "\",size:" + ifont.get(PFont.SIZE) + ",bold:"
 					+ ifont.get(PFont.BOLD));
 			loadingfile.write(",italic:" + ifont.get(PFont.ITALIC)
 					+ ",rangeMin:" + ifont.get(PFont.RANGE_MIN) + ",rangeMax:"
 					+ ifont.get(PFont.RANGE_MAX));
 			loadingfile.write("}");
 		}
 		loadingfile.write("};\n");// end the fontstructarray
 	}
 
 	private static void populateScripts(BufferedWriter loadingfile)
 			throws IOException, ScriptException {
 
 		int size = i.scripts.size();
 
 		if (size == 0)
 			return;
 
 		org.lateralgm.resources.Script[] isl = i.scripts
 				.toArray(new org.lateralgm.resources.Script[0]);
 		for (int s = 0; s < isl.length; s++) {
 			org.lateralgm.resources.Script io = isl[s];
 			loadingfile.write("scriptid_" + io.getId() + "=" + io.getName()
 					+ "=function() {");
 			loadingfile.write(convertCode(io.get(PScript.CODE).toString()));
 			loadingfile.write("};\n");
 		}
 	}
 
 	private static void populatePaths(BufferedWriter loadingfile)
 			throws IOException {
 
 		int size = i.paths.size();
 
 		if (size == 0) {
 			loadingfile.write("var pathstructarray = {}; //no paths \n");
 			return;
 		}
 		loadingfile.write("var pathstructarray = {");
 
 		org.lateralgm.resources.Path[] ipl = i.paths
 				.toArray(new org.lateralgm.resources.Path[0]);
 		for (int p = 0; p < size; p++) {
 			org.lateralgm.resources.Path ip = ipl[p];
 
 			if (p != 0)
 				loadingfile.write(",");
 
 			loadingfile.write(ip.getId() + ":{name:\"" + ip.getName()
 					+ "\",id:" + ip.getId());
 			loadingfile.write(",smooth:" + ip.get(PPath.SMOOTH) + ",closed:"
 					+ ip.get(PPath.CLOSED) + ",precision:"
 					+ ip.get(PPath.PRECISION));
 			loadingfile.write(",points:[");
 
 			int points = ip.points.size();
 			if (points == 0) {
 				loadingfile.write("]}");
 				continue;
 			}
 
 			for (int pp = 0; pp < points; pp++) {
 				org.lateralgm.resources.sub.PathPoint ipp = ip.points.get(pp);
 				if (pp != 0)
 					loadingfile.write(",");
 				loadingfile.write("{x:" + ipp.getX() + ",y:" + ipp.getY()
 						+ ",speed:" + ipp.getSpeed() + "}");
 			}
 			loadingfile.write("]}");
 		}
 		loadingfile.write("};\n");// end the pathstructarray
 	}
 
 	private static void populateBackgrounds(BufferedWriter loadingfile)
 			throws IOException {
 
 		int size = i.backgrounds.size();
 		if (size == 0) {
 			loadingfile
 					.write("var backgroundstructarray = {}; //no backgrounds \n");
 			return;
 		}
 		loadingfile.write("var backgroundstructarray = {");
 
 		/*
 		 * Create folder if it doesn't exist
 		 */
 		File folder = new File("./EnigmaJSLibrary/res/backgrounds");
 		if (folder.exists()) deleteFolder(folder);
 			folder.mkdir();
 
 		org.lateralgm.resources.Background[] ibl = i.backgrounds
 				.toArray(new org.lateralgm.resources.Background[0]);
 		for (int s = 0; s < size; s++) {
 			org.lateralgm.resources.Background ib = ibl[s];
 
 			if (s != 0)
 				loadingfile.write(",");
 			loadingfile.write(ib.getId() + ":{name:\"" + ib.getName()
 					+ "\",id:" + ib.getId());
 			loadingfile.write(",transparent:" + ib.get(PBackground.TRANSPARENT)
 					+ ",smoothEdges:" + ib.get(PBackground.SMOOTH_EDGES)
 					+ ",preload:" + ib.get(PBackground.PRELOAD));
 
 			File out = new File("./EnigmaJSLibrary/res/backgrounds/" + ib.getName()
 					+ ".png");
 			BufferedImage img = ib.getBackgroundImage();
 
 			ImageIO.write(img, "PNG", out);
 
 			loadingfile.write("}");
 		}
 		loadingfile.write("};\n");// end the backgroundstructarray
 	}
 
 	private static void populateSounds(BufferedWriter loadingfile)
 			throws IOException {
 
 		int size = i.sounds.size();
 		if (size == 0) {
 			loadingfile.write("var soundstructarray = {}; //No sounds \n");
 			return;
 		}
 		loadingfile.write("var soundstructarray = {");
 
 		/*
 		 * Create folder if it doesn't exist
 		 */
 		File folder = new File("./EnigmaJSLibrary/res/sounds");
 		if (folder.exists()) deleteFolder(folder);
 			folder.mkdir();
 
 		org.lateralgm.resources.Sound[] isl = i.sounds
 				.toArray(new org.lateralgm.resources.Sound[0]);
 		for (int s = 0; s < size; s++) {
 			org.lateralgm.resources.Sound is = isl[s];
 			if (s != 0)
 				loadingfile.write(",");
 			loadingfile.write(is.getId() + ":{name:\"" + is.getName()
 					+ "\",id:" + is.getId());// +",kind:"+SOUND_CODE.get(is.get(PSound.KIND)));
 			loadingfile.write(",fileType:\"" + is.get(PSound.FILE_TYPE)
 					+ "\",fileName:\"" + is.get(PSound.FILE_NAME) + "\"");
 			loadingfile.write(",chorus:" + is.get(PSound.CHORUS) + ",echo:"
 					+ is.get(PSound.ECHO) + ",flanger:"
 					+ is.get(PSound.FLANGER));
 			loadingfile.write(",gargle:" + is.get(PSound.GARGLE) + ",reverb:"
 					+ is.get(PSound.GARGLE) + ",volume:"
 					+ is.get(PSound.VOLUME));
 			loadingfile.write(",pan:" + is.get(PSound.PAN) + ",preload:"
 					+ is.get(PSound.PRELOAD));
 
 			if (is.data == null || is.data.length == 0) {
 				continue;
 			}
 			FileOutputStream sndfile = new FileOutputStream(
 					"./EnigmaJSLibrary/res/sounds/" + is.getName()
 							+ is.get(PSound.FILE_TYPE));
 			sndfile.write(is.data);
 			sndfile.close();
 			loadingfile.write("}");
 		}
 		loadingfile.write("};\n");// end the soundstructarray
 	}
 
 	protected static ResNode root;
 
 	private static void populateRooms(BufferedWriter loadingfile)
 			throws IOException, ScriptException {
 		ArrayList<org.lateralgm.resources.Room> irooms = new ArrayList<org.lateralgm.resources.Room>();
 		Enumeration<?> e = root.preorderEnumeration();
 		while (e.hasMoreElements()) {
 			ResNode node = (ResNode) e.nextElement();
 			if (node.kind == org.lateralgm.resources.Resource.Kind.ROOM) {
 				org.lateralgm.resources.Room r = (org.lateralgm.resources.Room) deRef((ResourceReference<?>) node
 						.getRes());
 				if (r != null)
 					irooms.add(r); // is this null check even necessary?
 			}
 		}
 
 		int size = irooms.size();
 		if (size == 0)
 			return;
 
 		org.lateralgm.resources.Room[] irl = irooms
 				.toArray(new org.lateralgm.resources.Room[0]);
 		for (int s = 0; s < size; s++) {
 			org.lateralgm.resources.Room is = irl[s];
 			loadingfile.write("\nenigma.rooms." + is.getName()
 					+ " = function() {");
 			loadingfile.write("this.name=\"" + is.getName() + "\"; this.id="
 					+ is.getId() + "; this.caption=\"" + is.get(PRoom.CAPTION)
 					+ "\"; this.width=" + is.get(PRoom.WIDTH)
 					+ "; this.height=" + is.get(PRoom.HEIGHT) + ";");
 			loadingfile.write("this.speed="
 					+ is.get(PRoom.SPEED)
 					+ "; this.persistent="
 					+ is.get(PRoom.PERSISTENT)
 					+ "; this.backgroundColor="
 					+ ARGBtoRGBA(((Color) is.get(PRoom.BACKGROUND_COLOR))
 							.getRGB()) + ";");
 			loadingfile.write("this.drawBackgroundColor="
 					+ is.get(PRoom.DRAW_BACKGROUND_COLOR)
 					+ "; this.enableViews=" + is.get(PRoom.ENABLE_VIEWS)
 					+ ";\n");
 
 			loadingfile.write("\nthis.creationCode=function(){");
 			loadingfile.write(convertCode("" + is.get(PRoom.CREATION_CODE)));
 			loadingfile.write("}");
 
 			/*
 			 * or.backgroundDefCount = is.backgroundDefs.size(); if
 			 * (or.backgroundDefCount != 0) { or.backgroundDefs = new
 			 * BackgroundDef.ByReference(); BackgroundDef[] obdl =
 			 * (BackgroundDef[])
 			 * or.backgroundDefs.toArray(or.backgroundDefCount); for (int bd =
 			 * 0; bd < obdl.length; bd++) { BackgroundDef obd = obdl[bd];
 			 * org.lateralgm.resources.sub.BackgroundDef ibd =
 			 * is.backgroundDefs.get(bd);
 			 * 
 			 * obd.visible = ibd.properties.get(PBackgroundDef.VISIBLE);
 			 * obd.foreground = ibd.properties.get(PBackgroundDef.FOREGROUND);
 			 * obd.x = ibd.properties.get(PBackgroundDef.X); obd.y =
 			 * ibd.properties.get(PBackgroundDef.Y); obd.tileHoriz =
 			 * ibd.properties.get(PBackgroundDef.TILE_HORIZ); obd.tileVert =
 			 * ibd.properties.get(PBackgroundDef.TILE_VERT); obd.hSpeed =
 			 * ibd.properties.get(PBackgroundDef.H_SPEED); obd.vSpeed =
 			 * ibd.properties.get(PBackgroundDef.V_SPEED); obd.stretch =
 			 * ibd.properties.get(PBackgroundDef.STRETCH);
 			 * 
 			 * obd.backgroundId =
 			 * toId(ibd.properties.get(PBackgroundDef.BACKGROUND),-1); } }
 			 *//*
 				 * or.viewCount = is.views.size(); if (or.viewCount != 0) {
 				 * or.views = new View.ByReference(); View[] ovl = (View[])
 				 * or.views.toArray(or.viewCount); for (int v = 0; v <
 				 * ovl.length; v++) { View ov = ovl[v];
 				 * org.lateralgm.resources.sub.View iv = is.views.get(v);
 				 * 
 				 * ov.visible = iv.properties.get(PView.VISIBLE); ov.viewX =
 				 * iv.properties.get(PView.VIEW_X); ov.viewY =
 				 * iv.properties.get(PView.VIEW_Y); ov.viewW =
 				 * iv.properties.get(PView.VIEW_W); ov.viewH =
 				 * iv.properties.get(PView.VIEW_H); ov.portX =
 				 * iv.properties.get(PView.PORT_X); ov.portY =
 				 * iv.properties.get(PView.PORT_Y); ov.portW =
 				 * iv.properties.get(PView.PORT_W); ov.portH =
 				 * iv.properties.get(PView.PORT_H); ov.borderH =
 				 * iv.properties.get(PView.BORDER_H); ov.borderV =
 				 * iv.properties.get(PView.BORDER_V); ov.speedH =
 				 * iv.properties.get(PView.SPEED_H); ov.speedV =
 				 * iv.properties.get(PView.SPEED_V); ov.objectId =
 				 * toId(iv.properties.get(PView.OBJECT),-1); } }
 				 */
 
 			int instanceCount = is.instances.size();
 			if (instanceCount != 0) {
 				for (int i = 0; i < instanceCount; i++) {
 					org.lateralgm.resources.sub.Instance ii = is.instances
 							.get(i);
 					String instancename = ((ResourceReference<?>) ii.properties
 							.get(PInstance.OBJECT)).get().getName();
 					loadingfile.write("\n new enigma.objects." + instancename
 							+ "(" + ii.properties.get(PInstance.ID) + ","
 							+ toId(ii.properties.get(PInstance.OBJECT), -1)
 							+ "," + ii.properties.get(PInstance.X) + ","
 							+ ii.properties.get(PInstance.Y) + ");");
 					// oi.creationCode =
 					// ii.properties.get(PInstance.CREATION_CODE);
 				}
 			}
 			/*
 			 * or.tileCount = is.tiles.size(); if (or.tileCount != 0) { or.tiles
 			 * = new Tile.ByReference(); Tile[] otl = (Tile[])
 			 * or.tiles.toArray(or.tileCount); for (int t = 0; t < otl.length;
 			 * t++) { Tile ot = otl[t]; org.lateralgm.resources.sub.Tile it =
 			 * is.tiles.get(t);
 			 * 
 			 * ot.bgX = it.properties.get(PTile.BG_X); ot.bgY =
 			 * it.properties.get(PTile.BG_Y); ot.roomX =
 			 * it.properties.get(PTile.ROOM_X); ot.roomY =
 			 * it.properties.get(PTile.ROOM_Y); ot.width =
 			 * it.properties.get(PTile.WIDTH); ot.height =
 			 * it.properties.get(PTile.HEIGHT); ot.depth =
 			 * it.properties.get(PTile.DEPTH); ot.backgroundId =
 			 * toId(it.properties.get(PTile.BACKGROUND),-1); ot.id =
 			 * it.properties.get(PTile.ID); ot.locked =
 			 * it.properties.get(PTile.LOCKED); } // tile } // tiles
 			 */
 			loadingfile.write("};\n");
 		} // rooms
 		loadingfile.write("var room = new enigma.rooms."
 				+ irooms.get(0).getName() + "();"); // for now
 	}
 
 	private static void populateObjects(BufferedWriter loadingfile)
 			throws Exception {
 		int size = i.gmObjects.size();
 		if (size == 0)
 			return;
 
 		org.lateralgm.resources.GmObject[] iol = i.gmObjects
 				.toArray(new org.lateralgm.resources.GmObject[0]);
 		for (int s = 0; s < size; s++) {
 			org.lateralgm.resources.GmObject io = iol[s];
 
 			currentObject = io.getName();
 			loadingfile.write("\nenigma.objects." + io.getName()
 					+ " = function(id, oid, x, y) {");
 			loadingfile
 					.write("this.prototype = new enigma.objects.object_locals(id, oid, x, y);");
 			loadingfile.write(" this.id=id;	this.x=x; this.y=y;");
 			loadingfile.write("this.sprite_index = "
 					+ toId(io.get(PGmObject.SPRITE), -1) + "; this.visible = "
 					+ io.get(PGmObject.VISIBLE) + "; this.solid = "
 					+ io.get(PGmObject.SOLID) + "; this.persistent="
 					+ io.get(PGmObject.PERSISTENT) + "; this.parent="
 					+ toId(io.get(PGmObject.PARENT), -100) + ";this.mask="
 					+ toId(io.get(PGmObject.MASK), -1) + ";");
 
 			// Use this code instead to allow 0 main events
 			// and switch GmObject.mainEvents to MainEvent.ByReference
 			int numevents = io.mainEvents.size();
 			if (numevents == 0)
 				continue;
 
 			for (int me = 0; me < numevents; me++) {
 
 				ArrayList<org.lateralgm.resources.sub.Event> iel = io.mainEvents
 						.get(me).events;
 
 				// ome.id = me;
 				int eventCount = iel.size();
 				if (eventCount == 0)
 					continue;
 
 				for (int e = 0; e < eventCount; e++) {
 					org.lateralgm.resources.sub.Event ie = iel.get(e);
 					int id;
 					if (me == org.lateralgm.resources.sub.MainEvent.EV_COLLISION)
 						id = toId(ie.other, -1);
 					else
 						id = ie.id;
 					eventname = Event.eventName(ie.mainId, ie.id).replaceAll(
 							" ", "");
 
 					String code = getActionsCode(ie);
 					loadingfile.write("\nthis.event_" + ie.mainId + "_" + id
 							+ "=function(){");
 					loadingfile.write(convertCode(code));
 					loadingfile.write("};");
 					if (ie.mainId == 0) // create event?
 						loadingfile.write(" this.event_" + ie.mainId + "_" + id
 								+ "();");
 					else
 						// link in the event
 						loadingfile
 								.write("enigma.system.event_loop.link_event(this.id,"
 										+ ie.mainId
 										+ ","
 										+ id
 										+ ",this.event_"
 										+ ie.mainId + "_" + id + ",this);\n");
 				}
 			}
 			// write the draw event
 			loadingfile
 					.write("this.myevent_draw=function() {if (this.image_single!=-1) { this.image_speed = 0; this.image_index = this.image_single; } \n");
 			loadingfile
 					.write("if (this.visible && this.sprite_index != -1) enigma.global.draw_sprite_ext(this.sprite_index,this.image_index,this.x,this.y,this.image_xscale,this.image_yscale,this.image_angle,this.image_blend,this.image_alpha);};\n");
 
 			loadingfile.write("enigma.classes.depth(this, "
 					+ io.get(PGmObject.DEPTH) + ");"); // do this last after
 														// draw event
 			loadingfile.write("};\n");// end of object
 		}
 	}
 
 	private static String convertCode(String code) throws FileNotFoundException {
 		if (code.contains("lang=javascript"))
 			return code;
 
 		engine.put("code", code);
 		String output;
 		try {
 			output = engine
 					.eval(
 							"enigma.parser.parse_edl(code); this.output=enigma.parser.code_out; this.error=enigma.parser.err;")
 					.toString();
 		} catch (ScriptException e) {
 			JSEnigmaRunner.ef.ta.append(" \n<error> Exception with Code:"
 					+ code + "\n Message:" + e.getMessage() + "\n </error>");
 			e.printStackTrace();
 			return "/* Erroring code */";
 		}
 
 		if (!output.equals("No error")) {
 			code = code.replace("\r\n", " ").replace("\n", " ").replace("\"",
 					"\\\"");
 			System.out.println("<code>\n" + code + "</code>");
 			System.out.println("Error " + numberOfErrors + ":"
 					+ engine.get("error").toString() + " in " + currentObject
 					+ " event:" + eventname);
 			numberOfErrors++;
 			return "/* Erroring code */";
 		} else {
 			// return engine.get("output").toString();
 			// normally it would just return, but lets fix some errors
 			String fixed = engine.get("output").toString();
 			fixed = fixed.replace("this.true", "true").replace("this.false",
 					"false");
 			fixed = fixed.replace("var (", "(");
			//fixed = fixed.replace("{", ";{"); // remove this asap
 			fixed = fixed
 					.replace("this.argument_relative", "argument_relative");
 			return fixed;
 		}
 	}
 
 	public static void main(String[] args) {
 		try {
 			convertCode("room_speed = 10");
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private static boolean actionDemise = false; // when unsupported actions are
 													// encountered, only report
 													// 1 error
 	private static int numberOfBraces = 0; // gm ignores brace actions which are
 											// in the wrong place or missing
 	private static int numberOfIfs = 0; // gm allows multipe else actions after
 										// 1 if, so its important to track the
 										// number
 	private static int numberOfErrors = 0;
 
 	private static String getActionsCode(ActionContainer ac) {
 
 		final String nl = System.getProperty("line.separator"); //$NON-NLS-1$
 		StringBuilder code = new StringBuilder();
 
 		numberOfBraces = 0;
 		numberOfIfs = 0;
 
 		for (Action act : ac.actions) {
 			LibAction la = act.getLibAction();
 			if (la == null) {
 				if (!actionDemise) {
 					JOptionPane.showMessageDialog(null, "Unsupported action"); //$NON-NLS-1$
 					actionDemise = true;
 				}
 				continue;
 			}
 			List<Argument> args = act.getArguments();
 			switch (la.actionKind) {
 			case Action.ACT_BEGIN:
 				code.append('{');
 				numberOfBraces++;
 				break;
 			case Action.ACT_CODE:
 				// surround with brackets (e.g. for if conditions before it) and
 				// terminate dangling comments
 				//code.append('{').append(args.get(0).getVal()).append("/**/\n}").append(nl); //$NON-NLS-1$
 				code.append(args.get(0).getVal()); // EnigmaJS
 				break;
 			case Action.ACT_ELSE: {
 				if (numberOfIfs > 0) {
 					code.append("else "); //$NON-NLS-1$
 					numberOfIfs--;
 				}
 			}
 				break;
 			case Action.ACT_END:
 				if (numberOfBraces > 0) {
 					code.append('}');
 					numberOfBraces--;
 				}
 				break;
 			case Action.ACT_EXIT:
 				code.append("exit "); //$NON-NLS-1$
 				break;
 			case Action.ACT_REPEAT:
 				code
 						.append("repeat (").append(args.get(0).getVal()).append(") "); //$NON-NLS-1$ //$NON-NLS-2$
 				break;
 			case Action.ACT_VARIABLE:
 				if (act.isRelative())
 					code
 							.append(args.get(0).getVal())
 							.append(" += ").append(args.get(1).getVal()).append(nl); //$NON-NLS-1$
 				else
 					code
 							.append(args.get(0).getVal())
 							.append(" = ").append(args.get(1).getVal()).append(nl); //$NON-NLS-1$
 				break;
 			case Action.ACT_NORMAL: {
 				if (la.execType == Action.EXEC_NONE)
 					break;
 				ResourceReference<org.lateralgm.resources.GmObject> apto = act
 						.getAppliesTo();
 				if (apto != org.lateralgm.resources.GmObject.OBJECT_SELF) {
 					if (la.question) {
 						/* Question action using with statement */
 						if (apto == org.lateralgm.resources.GmObject.OBJECT_OTHER)
 							code.append("with (other) "); //$NON-NLS-1$
 						else if (apto.get() != null)
 							code
 									.append("with (").append(apto.get().getName()).append(") "); //$NON-NLS-1$ //$NON-NLS-2$
 						else
 							code.append("/*null with!*/"); //$NON-NLS-1$
 
 					} else {
 						if (apto == org.lateralgm.resources.GmObject.OBJECT_OTHER)
 							code.append("with (other) {"); //$NON-NLS-1$
 						else if (apto.get() != null)
 							code
 									.append("with (").append(apto.get().getName()).append(") {"); //$NON-NLS-1$ //$NON-NLS-2$
 						else
 							code.append("/*null with!*/{"); //$NON-NLS-1$
 					}
 				}
 				if (la.question) {
 					code.append("if "); //$NON-NLS-1$
 					numberOfIfs++;
 				}
 				if (act.isNot())
 					code.append('!');
 				if (la.allowRelative) {
 					if (la.question)
 						code
 								.append("(argument_relative = ").append(act.isRelative()).append(", "); //$NON-NLS-1$ //$NON-NLS-2$
 					else
 						code
 								.append("{argument_relative = ").append(act.isRelative()).append("; "); //$NON-NLS-1$ //$NON-NLS-2$
 				}
 				if (la.question && la.execType == Action.EXEC_CODE)
 					code
 							.append("lib").append(la.parentId).append("_action").append(la.id); //$NON-NLS-1$ //$NON-NLS-2$
 				else
 					code.append(la.execInfo);
 				if (la.execType == Action.EXEC_FUNCTION) {
 					code.append('(');
 					for (int i = 0; i < args.size(); i++) {
 						if ((toString(args.get(i)).equals("")
 								|| toString(args.get(i)).equals("  ") || toString(
 								args.get(i)).equals(" "))
 								&& args.size() > 7)
 							continue; // required with due to bug with CLI which
 										// thinks actions with no arguments have
 										// >7!
 						if (i != 0)
 							code.append(" , ");
 						if (toString(args.get(i)).equals("")
 								|| toString(args.get(i)).equals(" ")
 								|| toString(args.get(i)).equals("  "))
 							code.append("0");
 
 						code.append(toString(args.get(i)));
 					}
 					code.append(')');
 				}
 				if (la.allowRelative)
 					code.append(la.question ? ')' : "\n}"); //$NON-NLS-1$
 				code.append(nl);
 
 				if (apto != org.lateralgm.resources.GmObject.OBJECT_SELF
 						&& (!la.question))
 					code.append("\n}"); //$NON-NLS-1$
 			}
 				break;
 			}
 		}
 		if (numberOfBraces > 0) {
 			// someone forgot the closing block action
 			for (int i = 0; i < numberOfBraces; i++)
 				code.append("\n}"); //$NON-NLS-1$
 		}
 		return code.toString();
 	}
 
 	private static void populateSprites(BufferedWriter loadingfile)
 			throws IOException {
 
 		int size = i.sprites.size();
 		if (size == 0) {
 			loadingfile.write("var spritestructarray = {}; //no sprites \n");
 			return;
 		}
 		loadingfile.write("var spritestructarray = {");
 
 		/*
 		 * Create folder if it doesn't exist
 		 */
 		File folder = new File("./EnigmaJSLibrary/res/sprites");
 		if (folder.exists()) deleteFolder(folder);
 			folder.mkdir();
 			
 
 		org.lateralgm.resources.Sprite[] isl = i.sprites
 				.toArray(new org.lateralgm.resources.Sprite[0]);
 		for (int s = 0; s < size; s++) {
 			org.lateralgm.resources.Sprite is = isl[s];
 			if (s != 0)
 				loadingfile.write(",");
 			loadingfile.write(is.getId() + ":{name:\"" + is.getName()
 					+ "\",id:" + is.getId() + ",transparent:"
 					+ is.get(PSprite.TRANSPARENT));
 
 			loadingfile.write(",mask:" + is.get(PSprite.SEPARATE_MASK)
 					+ ",smooth:" + is.get(PSprite.SMOOTH_EDGES));
 
 			loadingfile.write(",originX:" + is.get(PSprite.ORIGIN_X)
 					+ ",originY:" + is.get(PSprite.ORIGIN_Y));
 			loadingfile.write(",bbLeft:" + is.get(PSprite.BB_LEFT)
 					+ ",bbRight:" + is.get(PSprite.BB_RIGHT) + ",bbTop:"
 					+ is.get(PSprite.BB_TOP) + ",bbBottom:"
 					+ is.get(PSprite.BB_BOTTOM));
 			loadingfile.write(",subImageCount:" + is.subImages.size());
 			int subimages = is.subImages.size();
 			if (subimages == 0)
 				continue;
 
 			/*
 			 * Create an offscreen canvas
 			 */
 			BufferedImage offscreenimage = new BufferedImage((is.subImages
 					.getWidth() * subimages), is.subImages.getHeight(),
 					BufferedImage.TYPE_INT_ARGB);
 			Graphics2D spritesheet = offscreenimage.createGraphics();
 
 			File out = new File("./EnigmaJSLibrary/res/sprites/" + is.getName()
 					+ ".png");
 			for (int i = 0; i < is.subImages.size(); i++) {
 				BufferedImage img = is.subImages.get(i);
 				spritesheet.drawImage(img, (is.subImages.getWidth() * i), 0,
 						null);
 
 			}
 			ImageIO.write(offscreenimage, "PNG", out);
 			spritesheet.dispose();
 			loadingfile.write("}");
 		}
 
 		loadingfile.write("};");
 		// end the spritestructarray
 	}
 
 	
 	public static void deleteFolder (File dirPath) {
 	    String [] files = dirPath.list ();
 
 	    for (int i = 0; i < files.length; i++) {
 	      File file = new File (dirPath, files[i]);
 	      if (file.isDirectory ())
 	    	  deleteFolder (file);
 	      file.delete ();
 	    }
 	  }
 	
 	public static int toId(Object obj, int def) {
 		ResourceReference<?> rr = (ResourceReference<?>) obj;
 		if (deRef(rr) != null)
 			return rr.get().getId();
 		return def;
 	}
 
 	public static String toString(Argument arg) {
 		String val = arg.getVal();
 		switch (arg.kind) {
 		case Argument.ARG_BOTH:
 			// treat as literal if starts with quote (")
 			if (val.startsWith("\"") || val.startsWith("'"))return val; //$NON-NLS-1$ //$NON-NLS-2$
 			// else fall through
 		case Argument.ARG_STRING:
 			return "\"" + val.replace("\\", "\\\\").replace("\"", "\"+'\"'+\"") + "\""; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
 		case Argument.ARG_BOOLEAN:
 			return Boolean.toString(!val.equals("0")); //$NON-NLS-1$
 		case Argument.ARG_MENU:
 			return val;
 		case Argument.ARG_COLOR:
 			try {
 				return String.format("$%06X", Integer.parseInt(val)); //$NON-NLS-1$
 			} catch (NumberFormatException e) {
 			}
 			return val;
 		default:
 			if (Argument.getResourceKind(arg.kind) == null)
 				return val;
 			try {
 				return arg.getRes().get().getName();
 			} catch (NullPointerException e) {
 				val = "-1"; //$NON-NLS-1$
 			}
 			return val;
 		}
 	}
 
 	public static int ARGBtoRGBA(int argb) {
 		return ((argb & 0x00FFFFFF) << 8) | (argb >>> 24);
 	}
 }
