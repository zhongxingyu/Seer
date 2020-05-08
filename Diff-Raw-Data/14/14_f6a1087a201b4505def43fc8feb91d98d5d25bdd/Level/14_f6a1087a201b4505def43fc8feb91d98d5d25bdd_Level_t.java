 package uk.me.jumped.gentlemenfighter.levels;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Scanner;
 
 import uk.me.jumped.gentlemenfighter.Constants;
 import uk.me.jumped.gentlemenfighter.graphics.Frame;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 
 public class Level {
 
 	private static final String MARKER_BACKGROUND = "[background]",
 			MARKER_PLATFORM_DEF = "[platform_def]",
 			MARKER_PLATFORM_INST = "[platform_inst]", MARKER_MUSIC = "[music]";
 
 	public Frame[] background;
 	private HashMap<String, PlatformDef> platformDefs = new HashMap<String, PlatformDef>();
 	private ArrayList<PlatformInst> platformInstances = new ArrayList<PlatformInst>();
 	public String music;
 
 	public void addPlatformDef(PlatformDef platformDef) {
 		this.platformDefs.put(platformDef.name, platformDef);
 	}
 
 	public PlatformDef getPlatformDef(String name) {
 		return this.platformDefs.get(name);
 	}
 
 	public void addPlatformInst(PlatformInst platformInst) {
 		this.platformInstances.add(platformInst);
 	}
 
 	public PlatformInst[] getPlatforms() {
 		return this.platformInstances.toArray(new PlatformInst[0]);
 	}
 
 	public void clear() {
 		this.platformDefs.clear();
 		this.platformInstances.clear();
 	}
 
 	public static Level load(final String level) {
 		Level l = new Level();
 		Scanner s = new Scanner(Gdx.files.internal(level).read());
 		while (s.hasNext()) {
 			String marker = s.next();
 			Gdx.app.debug("Level loading", "Marker: " + marker);
 			if (marker.equals(MARKER_BACKGROUND)) {
 
 				l.background = readFrames(s,
 						Constants.Files.Graphics.BACKGROUNDS);
 
 			} else if (marker.equals(MARKER_PLATFORM_DEF)) {
 				PlatformDef def = new PlatformDef();
 
 				def.name = s.next();
 
 				def.frames = readFrames(s, Constants.Files.Graphics.BACKGROUNDS);
 
 				def.width = s.nextFloat();
 				def.height = s.nextFloat();
 
 				l.addPlatformDef(def);
 
 			} else if (marker.equals(MARKER_PLATFORM_INST)) {
 				PlatformInst inst = new PlatformInst();
 
 				String platDefName = s.next();
 
 				inst.def = l.getPlatformDef(platDefName);
 				if (inst.def == null) {
 					Gdx.app.error("Level loading",
 							"Unrecongised platform name: " + platDefName);
 					continue;
 				}
 
 				inst.x = s.nextFloat();
 				inst.y = s.nextFloat();
 
 				Gdx.app.debug("Level loading", "New inst: " + inst.def.name
 						+ ", Pos: " + inst.x + ", " + inst.y);
 
 				l.addPlatformInst(inst);
 
 			} else if (marker.equals(MARKER_MUSIC)) {
 				l.music = s.next();
 				Gdx.app.debug("Level loading", "Music: " + l.music);
 			} else {
 				Gdx.app.error("Level loading", "Unrecongised marker: " + marker);
 
 				// resync.
 				while (marker.equals(MARKER_BACKGROUND)
 						|| marker.equals(MARKER_PLATFORM_DEF)
 						|| marker.equals(MARKER_PLATFORM_INST)
 						|| marker.equals(MARKER_MUSIC) || !s.hasNext()) {
 					marker = s.next();
 				}
 
 			}
 		}
 		s.close();
 
 		return l;
 	}
 
 	private static Frame[] readFrames(Scanner s, final String path) {
 
 		final int frameCount = s.nextInt();
 		Gdx.app.debug("Level loading", "Frame(s) count: " + frameCount);
 		Frame[] frames = new Frame[frameCount];
 
 		for (int i = 0; i < frameCount; i++) {
 			Gdx.app.debug("Level loading", "Frame index: " + i);
 
 			Frame f = new Frame();
 
 			String textureName = path + s.next();
 
 			Gdx.app.debug("Level loading", "Frame texture: " + textureName);
 
 			Texture t = new Texture(Gdx.files.internal(textureName));
 
			int x = s.nextInt(), y = s.nextInt(), w = s.nextInt(), h = s
					.nextInt();
 
 			Gdx.app.debug("Level loading", "Frame texture region: " + x + " "
 					+ y + " " + w + " " + h);
 
 			TextureRegion tr = new TextureRegion(t, x, y, w, h);
 
 			f.Region = tr;
 
 			f.Length = s.nextFloat();
 
 			frames[i] = f;
 		}
 
 		return frames;
 
 	}
 
 	public static class PlatformDef {
 		public String name;
 		public float width, height;
 		public Frame[] frames;
 	}
 
 	public static class PlatformInst {
 		public PlatformDef def;
 		public float x, y;
 	}
 }
