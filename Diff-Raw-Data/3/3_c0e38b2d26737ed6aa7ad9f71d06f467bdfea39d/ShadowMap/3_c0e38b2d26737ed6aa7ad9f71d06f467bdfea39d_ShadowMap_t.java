 package net.fourbytes.shadow.map;
 
 import java.io.BufferedInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 import java.util.zip.GZIPInputStream;
 import java.util.zip.GZIPOutputStream;
 
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.utils.Array;
 import com.badlogic.gdx.utils.Json;
 import com.badlogic.gdx.utils.LongMap;
 import com.badlogic.gdx.utils.ObjectMap;
 
 import net.fourbytes.shadow.Block;
 import net.fourbytes.shadow.Coord;
 import net.fourbytes.shadow.Entity;
 import net.fourbytes.shadow.GameObject;
 import net.fourbytes.shadow.Garbage;
 import net.fourbytes.shadow.Layer;
 import net.fourbytes.shadow.Level;
 import net.fourbytes.shadow.Mob;
 import net.fourbytes.shadow.Player;
 import net.fourbytes.shadow.TypeBlock;
 import net.fourbytes.shadow.blocks.BlockType;
 import net.fourbytes.shadow.gdxutils.ByteMap;
 
 /**
  * An ShadowMap is an specially saved map. It mostly differs from the TilED maps by saving an "snapshot" of 
  * the current state of the level into individual {@link Chunk}s instead of saving an "initial state" of 
  * the level into one general map. 
  */
 public class ShadowMap {
 	
 	public LongMap<Chunk> chunks = new LongMap<Chunk>();
 	
 	public ShadowMap() {
 	}
 	
 	/**
 	 * Creates an fresh, "initial state" {@link GameObject}.
 	 * @param level Level to create the {@link GameObject} in.
 	 * @param x X position,
 	 * @param y Y position
 	 * @param ln Layer number
 	 * @param tid Tile ID (optional, use 0 by default)
 	 * @param type Type parameter ("block" or "entity") (optional)
 	 * @param subtype Subtype parameter ("Player" or "BlockDissolve.1")
 	 * @return {@link GameObject} "loaded" from map.
 	 */
 	public static GameObject convert(int x, int y, Layer layer, int tid, String type, String subtype) {
 		GameObject obj = null;
 		if (type == null || type.isEmpty()) {
 			if (subtype.toLowerCase().startsWith("block")) {
 				type = "block";
 			} else {
 				type = "entity";
 			}
 		}
 		if ("block".equals(type)) {
 			//System.out.println("tid: "+tid);
 			Block block = BlockType.getInstance(subtype, x, y, layer);
 			block.subtype = subtype;
 			obj = block;
 		} else if ("entity".equals(type)) {
 			if ("Player".equals(subtype)) {
 				Entity ent = new Player(new Vector2(x, y), layer);
 				obj = ent;
 			} else if (subtype.startsWith("Mob")) {
 				try {
 					Class clazz = ShadowMap.class.getClassLoader().loadClass("net.fourbytes.shadow."+subtype);
 					Mob mob = (Mob) clazz.getConstructor(Vector2.class, Layer.class).newInstance(new Vector2(x, y), layer);
 					obj = mob;
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		} else {
 			if (tid != 0) {
 				throw new IllegalStateException("unknown type "+type+" for block id "+tid);
 			}
 		}
 		return obj;
 	}
 	
 	/**
 	 * Creates an {@link MapObject} out of an {@link GameObject}.
 	 * @param go {@link GameObject} to convert
 	 * @return {@link MapObject} that can be converted back 
 	 * to create another {@link GameObject} <b> representing an SIMILAR (!)</b> {@link GameObject} to the original.
 	 */
 	public static MapObject convert(GameObject go) {
 		MapObject mo = new MapObject();
 		
 		mo.x = go.pos.x;
 		mo.y = go.pos.y;
 		
 		if (go instanceof Block) {
 			mo.type = "block";
 		} else if (go instanceof Entity) {
 			mo.type = "entity";
 		}
 		
 		if (go instanceof TypeBlock) {
 			mo.subtype = ((Block)go).subtype;
 		} else if (mo.subtype == null || mo.subtype.isEmpty()) {
 			mo.subtype = go.getClass().getSimpleName();
 		}
 		
 		for (ByteMap.Entry<Layer> entry : go.layer.level.layers.entries()) {
 			if (entry.value == go.layer) {
 				mo.layer = entry.key;
 				break;
 			}
 		}
 		
 		Object o = go;
 		if (go instanceof TypeBlock) {
 			o = ((TypeBlock)go).type;
 		}
 		
 		Field[] fields = o.getClass().getFields();
 		for (Field field : fields) {
 			Saveable saveable = field.getAnnotation(Saveable.class);
 			if (saveable != null) {
 				try {
 					mo.args.put(field.getName(), field.get(o));
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		
 		return mo;
 	}
 	
 	/**
 	 * Creates an {@link GameObject} out of an {@link MapObject} . 
 	 * @param mo {@link MapObject} to convert
 	 * @param level Level to allocate the GameObject to.
 	 * @return {@link GameObject} representing an closest-possible, 
 	 * thru-stream-sendable replica of the original {@link GameObject}.
 	 */
 	public static GameObject convert(MapObject mo, Level level) {
 		Layer layer = null;
 		if (level != null) {
 			layer = level.layers.get(mo.layer);
 			if (layer == null) {
 				level.fillLayer(mo.layer);
 				layer = level.layers.get(mo.layer);
 			}
 		}
 		int tid = 0;
 		GameObject go = convert((int)mo.x, (int)mo.y, layer, tid, mo.type, mo.subtype);
		if (go == null) {
			return null;
		}
 		go.pos.x = mo.x;
 		go.pos.y = mo.y;
 		
 		Object o = go;
 		if (o instanceof TypeBlock) {
 			o = ((TypeBlock)o).type;
 		}
 		
 		for (ObjectMap.Entry<String, Object> entry : mo.args.entries()) {
 			try {
 				o.getClass().getField(entry.key).set(o, entry.value);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		
 		return go;
 	}
 	
 	/**
 	 * Temporarily loads an ShadowMap into memory. Doesn't create an level.
 	 * @param file File to load map from.
 	 * @return ShadowMap containing {@link Chunk}s containing {@link MapObject}s NOT {@link GameObject}s, or null when failed.
 	 */
 	public static ShadowMap loadFile(FileHandle file) {
 		ShadowMap map = null;
 		try {
 			InputStream fis = file.read();
 			GZIPInputStream gis = new GZIPInputStream(fis);
 			map = Garbage.json.fromJson(ShadowMap.class, gis);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return map;
 	}
 	
 	/**
 	 * Converts the temporarily loaded ShadowMap to an level.
 	 * @param level Level to fill.
 	 */
 	public void fillLevel(Level level) {
 		for (Chunk chunk : chunks.values()) {
 			convert(chunk, level, true);
 		}
 	}
 	
 	/**
 	 * Converts the content of temporarily loaded chunk, binding it (<b>NOT</b> adding it when add == false) to an level.
 	 * @param chunk Chunk to convert.
 	 * @param level Level to fill.
 	 * @param add Add the result of conversion to level?
 	 * @return Result of conversion.
 	 */
 	public Array<GameObject> convert(Chunk chunk, Level level, boolean add) {
 		Array<GameObject> gos = new Array<GameObject>();
 		for (MapObject mo : chunk.objects) {
 			GameObject go = convert(mo, level);
 			if (go != null && add) {
 				go.layer.add(go);
 				if (level != null) {
 					if (go instanceof Player) {
 						level.player = (Player) go;
 					}
 				}
 			}
 			gos.add(go);
 		}
 		level.ftick = true;
 		level.tickid = 0;
 		return gos;
 	}
 	
 	/**
 	 * Creates an ShadowMap from an level to save afterwards.
 	 * @param level Level to get data from.
 	 * @return ShadowMap containing {@link Chunk}s containing {@link MapObject}s converted from 
 	 * {@link GameObject}s of the level.
 	 */
 	public static ShadowMap createFrom(Level level) {
 		ShadowMap map = new ShadowMap();
 		for (Layer layer: level.layers.values()) {
 			for (GameObject go : layer.blocks) {
 				add0(map, go);
 			}
 			for (GameObject go : layer.entities) {
 				add0(map, go);
 			}
 		}
 		return map;
 	}
 	
 	protected static void add0(ShadowMap map, GameObject go) {
 		MapObject mo = convert(go);
 		Chunk chunk = map.chunks.get(Coord.get((int)(mo.x / Chunk.size), (int)(mo.y / Chunk.size)));
 		if (chunk == null) {
 			chunk = new Chunk();
 			chunk.x = (int)(mo.x / Chunk.size);
 			chunk.y = (int)(mo.y / Chunk.size);
 			map.chunks.put(Coord.get((int)(mo.x / Chunk.size), (int)(mo.y / Chunk.size)), chunk);
 		}
 		chunk.objects.add(mo);
 	}
 	
 	/**
 	 * Saves the ShadowMap to the file to load afterwards.
 	 * @param file File to save the ShadowMap to.
 	 */
 	public void save(FileHandle file) {
 		try {
 			String json = Garbage.json.toJson(this);
 			OutputStream fos = file.write(false);
 			GZIPOutputStream gos = new GZIPOutputStream(fos);
 			gos.write(json.getBytes("UTF-8"));
 			gos.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 }
