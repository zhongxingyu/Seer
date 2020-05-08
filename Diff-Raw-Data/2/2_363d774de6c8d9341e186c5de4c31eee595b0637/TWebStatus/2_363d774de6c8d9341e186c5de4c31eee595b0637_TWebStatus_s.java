 package org.rsbot.event.impl;
 
 import org.rsbot.event.listeners.TextPaintListener;
 import org.rsbot.service.WebQueue;
 import org.rsbot.util.StringUtil;
 
 import java.awt.*;
 
 /**
  * Draws the web cache and cache writer information.
  *
  * @author Timer
  */
 public class TWebStatus implements TextPaintListener {
 	public TWebStatus() {
 	}
 
 	public int drawLine(final Graphics render, int idx) {
		final String[] items = {"Web Queue", "Buffering: " + WebQueue.weAreBuffering + ", " + WebQueue.bufferingCount + " nodes.", "Speed Buffering: " + WebQueue.speedBuffer,
 				"Cache Writer", "Queue Size: " + WebQueue.queueSize(0), "Remove queue size: " + WebQueue.queueSize(1), "Removing queue size: " + WebQueue.queueSize(2)};
 		for (final String item : items) {
 			StringUtil.drawLine(render, idx++, item);
 		}
 		return idx;
 	}
 }
