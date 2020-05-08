 /**
  * 
  */
 package org.morganm.heimdall.event.handlers;
 
 import org.morganm.heimdall.engine.Engine;
 import org.morganm.heimdall.event.BlockChangeEvent;
 import org.morganm.heimdall.event.InventoryChangeEvent;
 import org.morganm.heimdall.event.PlayerEvent;
 
 /** Enricher which wraps an Engine and passes it events.
  * 
  * @author morganm
  *
  */
 public class EngineWrapper extends EventHandler {
 	private final Engine engine;
 	
 	public EngineWrapper(final Engine engine) {
 		this.engine = engine;
 	}
 
 	@Override
 	public void processEvent(BlockChangeEvent bc) {
 		engine.processBlockChange(bc);
 	}
 
 	@Override
 	public void processEvent(InventoryChangeEvent ice) {
 		engine.processInventoryChange(ice);
 	}
 	
 	@Override
 	public void processEvent(PlayerEvent pe) {
 		engine.processPlayerEvent(pe);
 	}
 }
