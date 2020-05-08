 package com.madthrax.ridiculousRPG.event;
 
 import java.util.Map;
 import java.util.Map.Entry;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.files.FileHandle;
 import com.badlogic.gdx.graphics.g2d.tiled.TiledMap;
 import com.madthrax.ridiculousRPG.GameBase;
 import com.madthrax.ridiculousRPG.animation.TileAnimation;
 import com.madthrax.ridiculousRPG.event.handler.EventExecScriptAdapter;
 import com.madthrax.ridiculousRPG.event.handler.EventHandler;
 import com.madthrax.ridiculousRPG.movement.MovementHandler;
 import com.madthrax.ridiculousRPG.util.BlockingBehavior;
 import com.madthrax.ridiculousRPG.util.Speed;
 import com.madthrax.ridiculousRPG.util.TextureRegionLoader;
 import com.madthrax.ridiculousRPG.util.TextureRegionLoader.TextureRegionRef;
 
 public class EventFactory {
 
 	private static final char EVENT_CUSTOM_PROP_KZ = '$';
 	// the key is translated to lower case -> we are case insensitive
 	private static final String EVENT_PROP_ID = "id";
 	private static final String EVENT_PROP_DISPLAY = "display";
 	private static final String EVENT_PROP_HEIGHT = "height";
 	private static final String EVENT_PROP_OUTREACH = "outreach";
 	private static final String EVENT_PROP_ROTATION = "rotation";
 	private static final String EVENT_PROP_SCALEX = "scalex";
 	private static final String EVENT_PROP_SCALEY = "scaley";
 	private static final String EVENT_PROP_IMAGE = "image";
 	private static final String EVENT_PROP_EFFECT = "effect";
 	private static final String EVENT_PROP_EFFECTFRONT = "effectfront";
 	private static final String EVENT_PROP_EFFECTREAR = "effectrear";
 	private static final String EVENT_PROP_CENTERIMAGE = "centerimage";
 	private static final String EVENT_PROP_BLOCKING = "blocking";
 	private static final String EVENT_PROP_MOVEHANDLER = "movehandler";
 	private static final String EVENT_PROP_SPEED = "speed";
 	private static final String EVENT_PROP_ANIMATION = "animation";
 	private static final String EVENT_PROP_ESTIMATETOUCHBOUNDS = "estimatetouchbounds";
 	private static final String EVENT_PROP_HANDLER = "eventhandler";
 	// the following properties can not be mixed with an eventhandler
 	// which doesn't extend the EventExecScriptAdapter
 	private static final String EVENT_PROP_ONPUSH = "onpush";
 	private static final String EVENT_PROP_ONTOUCH = "ontouch";
 	private static final String EVENT_PROP_ONTIMER = "ontimer";
 	private static final String EVENT_PROP_ONCUSTOMEVENT = "oncustomevent";
 	private static final String EVENT_PROP_ONLOAD = "onload";
 
 	/**
 	 * Method to parse the object properties input.
 	 * 
 	 * @param ev
 	 * @param props
 	 */
 	public static void parseProperties(EventObject ev, Map<String, String> props) {
 		for (Entry<String, String> entry : props.entrySet()) {
 			String key = entry.getKey().trim();
			// Fix the behavior of the libgdx XmlReader
 			String val = entry.getValue().replace("&quot;", "\"").replace(
 					"&gt;", ">").replace("&lt;", "<").replace("&amp;", "&")
 					.trim();
 			if (key.length() == 0 || val.length() == 0)
 				continue;
 			if (key.charAt(0) == EVENT_CUSTOM_PROP_KZ) {
 				ev.properties.put(key, val);
 			} else {
 				parseSingleProperty(ev, key, val, props);
 			}
 		}
 	}
 
 	private static void parseSingleProperty(EventObject ev, String key,
 			String val, Map<String, String> props) {
 		// let's be case insensitive
 		key = key.toLowerCase();
 		try {
 			if (EVENT_PROP_ID.equals(key)) {
 				ev.id = toInt(val);
 			} else if (EVENT_PROP_HEIGHT.equals(key)) {
 				ev.z += toInt(val);
 			} else if (EVENT_PROP_BLOCKING.equals(key)) {
 				ev.blockingBehavior = BlockingBehavior.parse(val);
 			} else if (EVENT_PROP_SPEED.equals(key)) {
 				ev.setMoveSpeed(Speed.parse(val));
 			} else if (EVENT_PROP_MOVEHANDLER.equals(key)) {
 				Object evHandler = GameBase.$().eval(val);
 				if (evHandler instanceof Class<?>) {
 					@SuppressWarnings("unchecked")
 					Class<? extends MovementHandler> clazz = (Class<? extends MovementHandler>) evHandler;
 					evHandler = clazz.getMethod("$").invoke(null);
 				}
 				if (evHandler instanceof MovementHandler) {
 					ev.setMoveHandler((MovementHandler) evHandler);
 				}
 			} else if (EVENT_PROP_OUTREACH.equals(key)) {
 				ev.outreach = toInt(val);
 			} else if (EVENT_PROP_ROTATION.equals(key)) {
 				ev.rotation = toFloat(val);
 			} else if (EVENT_PROP_SCALEX.equals(key)) {
 				ev.scaleX = toFloat(val);
 			} else if (EVENT_PROP_SCALEY.equals(key)) {
 				ev.scaleY = toFloat(val);
 			} else if (EVENT_PROP_IMAGE.equals(key)) {
 				if (Gdx.files.internal(val).exists()) {
 					boolean estimateTouch = "true".equalsIgnoreCase(props
 							.get(EVENT_PROP_ESTIMATETOUCHBOUNDS));
 					ev.setImage(val, estimateTouch, !estimateTouch);
 					initVisibleEvent(ev, props);
 				}
 			} else if (EVENT_PROP_EFFECTFRONT.equals(key)) {
 				if (Gdx.files.internal(val).exists()) {
 					ev.setEffectFront(val);
 					if (ev.z == 0f && props.get(EVENT_PROP_HEIGHT) == null) {
 						ev.z = .1f;
 					}
 				}
 			} else if (EVENT_PROP_EFFECT.equals(key)) {
 				if (Gdx.files.internal(val).exists()) {
 					ev.setEffectFront(val);
 					ev.setEffectRear(val);
 					if (ev.z == 0f && props.get(EVENT_PROP_HEIGHT) == null) {
 						ev.z = .1f;
 					}
 				}
 			} else if (EVENT_PROP_EFFECTREAR.equals(key)) {
 				if (Gdx.files.internal(val).exists()) {
 					ev.setEffectRear(val);
 					if (ev.z == 0f && props.get(EVENT_PROP_HEIGHT) == null) {
 						ev.z = .1f;
 					}
 				}
 			} else if (EVENT_PROP_ANIMATION.equals(key)) {
 				FileHandle fh = Gdx.files.internal(val);
 				if (fh.exists()) {
 					TextureRegionRef t = TextureRegionLoader.load(val);
 					TileAnimation anim = new TileAnimation(val, t
 							.getRegionWidth() / 4, t.getRegionHeight() / 4, 4,
 							4);
 					t.dispose();
 					boolean estimateTouch = "true".equalsIgnoreCase(props
 							.get(EVENT_PROP_ESTIMATETOUCHBOUNDS));
 					ev.setAnimation(anim, estimateTouch, !estimateTouch);
 					initVisibleEvent(ev, props);
 				} else {
 					Object result = GameBase.$().eval(val);
 					if (result instanceof TileAnimation) {
 						boolean estimateTouch = "true".equalsIgnoreCase(props
 								.get(EVENT_PROP_ESTIMATETOUCHBOUNDS));
 						ev.setAnimation((TileAnimation) result, estimateTouch,
 								!estimateTouch);
 						initVisibleEvent(ev, props);
 					}
 				}
 			} else if (EVENT_PROP_HANDLER.equals(key)) {
 				Object evHandler = GameBase.$().eval(val);
 				if (evHandler instanceof Class<?>) {
 					@SuppressWarnings("unchecked")
 					Class<? extends EventHandler> clazz = (Class<? extends EventHandler>) evHandler;
 					evHandler = clazz.newInstance();
 				}
 
 				// merge both event handler
 				if (evHandler instanceof EventExecScriptAdapter
 						&& ev.getEventHandler() instanceof EventExecScriptAdapter) {
 					((EventExecScriptAdapter) evHandler)
 							.merge((EventExecScriptAdapter) ev
 									.getEventHandler());
 				} else if (evHandler instanceof EventHandler) {
 					ev.setEventHandler((EventHandler) evHandler);
 				}
 			} else if (key.startsWith(EVENT_PROP_ONPUSH)) {
 				ev.pushable = true;
 				if (ev.getEventHandler() == null) {
 					ev.setEventHandler(new EventExecScriptAdapter());
 				}
 				if (ev.getEventHandler() instanceof EventExecScriptAdapter) {
 					String index = key.substring(EVENT_PROP_ONPUSH.length())
 							.trim();
 					((EventExecScriptAdapter) ev.getEventHandler()).execOnPush(
 							val, index.length() == 0 ? -1 : toInt(index));
 				}
 			} else if (key.startsWith(EVENT_PROP_ONTOUCH)) {
 				ev.touchable = true;
 				if (ev.getEventHandler() == null) {
 					ev.setEventHandler(new EventExecScriptAdapter());
 				}
 				if (ev.getEventHandler() instanceof EventExecScriptAdapter) {
 					String index = key.substring(EVENT_PROP_ONTOUCH.length())
 							.trim();
 					((EventExecScriptAdapter) ev.getEventHandler())
 							.execOnTouch(val, index.length() == 0 ? -1
 									: toInt(index));
 				}
 			} else if (key.startsWith(EVENT_PROP_ONTIMER)) {
 				if (ev.getEventHandler() == null) {
 					ev.setEventHandler(new EventExecScriptAdapter());
 				}
 				if (ev.getEventHandler() instanceof EventExecScriptAdapter) {
 					String index = key.substring(EVENT_PROP_ONTIMER.length())
 							.trim();
 					((EventExecScriptAdapter) ev.getEventHandler())
 							.execOnTimer(val, index.length() == 0 ? -1
 									: toInt(index));
 				}
 			} else if (key.startsWith(EVENT_PROP_ONCUSTOMEVENT)) {
 				if (ev.getEventHandler() == null) {
 					ev.setEventHandler(new EventExecScriptAdapter());
 				}
 				if (ev.getEventHandler() instanceof EventExecScriptAdapter) {
 					String index = key.substring(
 							EVENT_PROP_ONCUSTOMEVENT.length()).trim();
 					((EventExecScriptAdapter) ev.getEventHandler())
 							.execOnCustomTrigger(val, index.length() == 0 ? -1
 									: toInt(index));
 				}
 			} else if (key.startsWith(EVENT_PROP_ONLOAD)) {
 				if (ev.getEventHandler() == null) {
 					ev.setEventHandler(new EventExecScriptAdapter());
 				}
 				if (ev.getEventHandler() instanceof EventExecScriptAdapter) {
 					String index = key.substring(EVENT_PROP_ONLOAD.length())
 							.trim();
 					((EventExecScriptAdapter) ev.getEventHandler()).execOnLoad(
 							val, index.length() == 0 ? -1 : toInt(index));
 				}
 			}
 		} catch (Exception e) {
 			GameBase.$error("TiledMap.createEvent",
 					"Could not parse property '" + key + "' for event '"
 							+ ev.name + "'", e);
 		}
 	}
 
 	private static void initVisibleEvent(EventObject ev,
 			Map<String, String> props) {
 		ev.visible = true;
 		if ("true".equalsIgnoreCase(props.get(EVENT_PROP_CENTERIMAGE)))
 			ev.centerDrawbound();
 		if (ev.z == 0f && props.get(EVENT_PROP_HEIGHT) == null) {
 			ev.z = .1f;
 		}
 	}
 
 	public static boolean isHidden(Map<String, String> properties) {
 		return "false".equalsIgnoreCase(properties.get(EVENT_PROP_DISPLAY))
 				|| "none".equalsIgnoreCase(properties.get(EVENT_PROP_DISPLAY));
 	}
 
 	public static int getZIndex(Map<String, String> properties) {
 		return toInt(properties.get(EVENT_PROP_HEIGHT));
 	}
 
 	public static int getZIndex(TiledMap map, int tile) {
 		return toInt(map.getTileProperty(tile, EVENT_PROP_HEIGHT));
 	}
 
 	private static int toInt(String prop) {
 		if (prop != null && prop.length() > 0) {
 			try {
 				return Integer.parseInt(prop);
 			} catch (NumberFormatException e) {
 			}
 		}
 		return 0;
 	}
 
 	private static float toFloat(String prop) {
 		if (prop != null && prop.length() > 0) {
 			try {
 				return Float.parseFloat(prop);
 			} catch (NumberFormatException e) {
 			}
 		}
 		return 0;
 	}
 }
