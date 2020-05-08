 package com.sirenian.hellbound.givens;
 
 import java.awt.Graphics;
 
 import org.jbehave.core.story.domain.CleansUpWorld;
 import org.jbehave.core.story.domain.World;
 import org.jbehave.threaded.swing.DefaultWindowWrapper;
import org.jbehave.threaded.swing.HeadlessChecker;
 
 import com.sirenian.hellbound.Hellbound;
 import com.sirenian.hellbound.engine.EngineQueue;
 import com.sirenian.hellbound.engine.GuiQueue;
 import com.sirenian.hellbound.engine.PseudoRandomGlyphFactory;
 import com.sirenian.hellbound.engine.ThreadedEngineQueue;
 import com.sirenian.hellbound.gui.PitPanel;
 import com.sirenian.hellbound.gui.RenderedPit;
 import com.sirenian.hellbound.gui.ThreadedSwingQueue;
 import com.sirenian.hellbound.stories.util.WorldKey;
 import com.sirenian.hellbound.util.Logger;
 
 public class HellboundIsRunning extends HellboundGiven implements CleansUpWorld {
 
 	public void setUpAnyTimeIn(World world) {
 		DefaultWindowWrapper hellboundFrameWrapper = new DefaultWindowWrapper("HellboundFrame");		
 		ForcedHeartbeat heartbeat = new ForcedHeartbeat();
 		RenderedPit graphics = new RenderedPit(Hellbound.SCALE, Hellbound.WIDTH, Hellbound.HEIGHT, Hellbound.COLORMAP);
         EngineQueue engineQueue = new ThreadedEngineQueue();
         GuiQueue guiQueue = new ThreadedSwingQueue();
         PseudoRandomGlyphFactory glyphFactory = new PseudoRandomGlyphFactory(42, 7, 13);
 		
         Hellbound hellbound = new Hellbound(engineQueue, guiQueue, heartbeat, createPitPanelWithDoublePaint(graphics), glyphFactory);
 		
 		world.put(WorldKey.HEARTBEAT, heartbeat);
 		world.put(WorldKey.WINDOW_WRAPPER, hellboundFrameWrapper);
 		world.put(WorldKey.PIT, graphics);
         world.put(WorldKey.ENGINE_QUEUE, engineQueue);
         world.put(WorldKey.GUI_QUEUE, guiQueue);
         world.put(WorldKey.HELLBOUND, hellbound);
 	}
 	
 	private PitPanel createPitPanelWithDoublePaint(final RenderedPit pg) {
 		PitPanel panel = new PitPanel(Hellbound.SCALE, Hellbound.WIDTH, Hellbound.HEIGHT, Hellbound.COLORMAP) {
 
 			private static final long serialVersionUID = 1L;
 
 			public void paint(Graphics g) {
 				super.paint(g);
 				super.paint(pg);
 			}
 		};
 		return panel;
 	}	
 
     public void cleanUp(World world) {
         Logger.debug(this, "tidyUp Given");
        if (!new HeadlessChecker().isHeadless()) {
            Hellbound hellbound = (Hellbound) world.get(WorldKey.HELLBOUND);
            hellbound.stopHellbound();
        }
     }
 }
