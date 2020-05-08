 package org.rsbot.script.wrappers;
 
 import org.rsbot.script.methods.MethodContext;
 import org.rsbot.script.web.WebTile;
 
 import java.util.EnumSet;
 
 /**
  * A path consisting of a list of tile waypoints.
  *
  * @author Timer
  */
 public class RSWeb extends RSPath {
	private final WebTile[] tiles;

 	public RSWeb(MethodContext ctx, WebTile[] tiles) {
 		super(ctx);
		this.tiles = tiles;
	}

	public WebTile[] getTiles() {
		return tiles;
 	}
 
 	@Override
 	public boolean traverse(EnumSet<TraversalOption> options) {
 		return false;
 	}
 
 	@Override
 	public boolean isValid() {
 		return false;
 	}
 
 	@Override
 	public RSTile getNext() {
 		return null;
 	}
 
 	@Override
 	public RSTile getStart() {
 		return null;
 	}
 
 	@Override
 	public RSTile getEnd() {
 		return null;
 	}
 }
