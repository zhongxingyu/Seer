 package org.rsbot.script.web;
 
 import org.rsbot.script.Random;
 import org.rsbot.script.Script;
 import org.rsbot.script.methods.MethodContext;
 import org.rsbot.script.methods.MethodProvider;
 import org.rsbot.script.randoms.LoginBot;
 import org.rsbot.script.wrappers.RSPath;
 import org.rsbot.script.wrappers.RSTile;
 
 import java.util.Collections;
 
 public class RouteStep extends MethodProvider {
 	private final Type type;
 	private RSTile[] path = null;
 	private RSPath rspath = null;
 	private Teleport teleport = null;
 
 	public static enum Type {
 		PATH, TELEPORT
 	}
 
 	public RouteStep(final MethodContext ctx, final Object step) {
 		super(ctx);
 		if (step instanceof Teleport) {
 			this.type = Type.TELEPORT;
 			this.teleport = (Teleport) step;
 		} else if (step instanceof RSTile[]) {
 			this.type = Type.PATH;
 			this.path = (RSTile[]) step;
 		} else if (step instanceof RSTile) {
 			this.type = Type.PATH;
 			this.path = new RSTile[]{(RSTile) step};
 		} else {
 			throw new IllegalArgumentException("Step is of an invalid type!");
 		}
 	}
 
 	public boolean execute() {
 		try {
 			for (final Script checkScript : Collections.unmodifiableCollection(methods.bot.getScriptHandler().getRunningScripts().values())) {
 				if (!checkScript.isActive() || !checkScript.isRunning()) {
 					return false;
 				}
 				if (checkScript.isPaused()) {
 					sleep(500);
 					return true;
 				}
 			}
 			if (methods.bot.getScriptHandler().getRunningScripts().size() == 0) {
 				return false;
 			}
 			switch (type) {
 				case PATH:
 					if (path == null || inSomeRandom()) {//Recalculation says path is a no-go (or in a random).
 						return false;
 					}
 					if (rspath == null) {
 						rspath = methods.walking.newTilePath(path);
 					}
 					if (methods.calc.distanceTo(rspath.getEnd()) < 5) {
 						rspath = null;
 						path = null;
 						return true;
 					}
 					sleep(random(50, 150));
 					return !inSomeRandom() && rspath.traverse();
 				case TELEPORT:
 					if (inSomeRandom()) {
 						return false;
 					}
 					if (teleport != null && teleport.perform()) {
 						teleport = null;
 						return true;
 					}
 					return false;
 			}
 		} catch (Exception e) {
 		}
 		return false;
 	}
 
 	public boolean finished() {
 		return path == null && teleport == null;
 	}
 
 	public Teleport getTeleport() {
 		return teleport;
 	}
 
 	public RSTile[] getPath() {
 		return path;
 	}
 
 	private boolean inSomeRandom() {
 		if (methods.bot.disableRandoms) {
 			return false;
 		}
 		for (final Random random : methods.bot.getScriptHandler().getRandoms()) {
 			if (random.isEnabled() && !(methods.bot.disableAutoLogin && random instanceof LoginBot)) {
 				if (random.activateCondition()) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	void update() {
 		if (path != null && path.length > 1) {
 			RSTile startTile = path[0];
 			RSTile endTile = path[path.length - 1];
			path = methods.web.generateTilePath(startTile, endTile);
 			rspath = null;
 		}
 	}
 }
