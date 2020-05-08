 package com.infectosaurus;
 
 import com.infectosaurus.tools.FixedSizeArray;
 import com.infectosaurus.tools.ObjectPool;
 import com.infectosaurus.tools.Vector2;
 
 import android.util.Log;
 
 
 public class RenderSystem {
 	private ObjectHandler<RenderElement>[] renderQueues;
 	private int queueIndex;
 	private ObjectPool<RenderElement> rElementPool;
 	
 	//TODO tie to max size of GOHandler!!
 	private final static int QUEUE_SIZE = 256;
 	private final static int DRAW_QUEUE_COUNT = 2;
     private final static int MAX_BG_TILES = 128;
 	
     public RenderSystem() {
         renderQueues = new ObjectHandler[DRAW_QUEUE_COUNT];
         for (int i = 0; i < DRAW_QUEUE_COUNT; i++) {
             renderQueues[i] = new ObjectHandler<RenderElement>(QUEUE_SIZE);
             renderQueues[i].objects.setComparator(RenderElement.comparer);
         }
         
         int poolSize = (QUEUE_SIZE+MAX_BG_TILES) * 6;
         rElementPool = new ObjectPool<RenderElement>(poolSize, RenderElement.class);
         
         queueIndex = 0;
     }
     
     public void scheduleForDraw(Drawable object, Vector2 pos) {
        RenderElement element = rElementPool.allocate();
         if(element == null) return;
        if(pos.x + object.getWidth() < Camera.pos.x) return;
    	if(pos.x > Camera.pos.x + Camera.screenWidth) return;
        if(pos.y + object.getHeight() < Camera.pos.y) return;
    	if(pos.y > Camera.pos.y + Camera.screenHeight) return;
         
         
         //Since this is done a lot, we want max speed, so we change
         //the public variables instead of calling set
         element.drawable = object;
         element.x = pos.x;
         element.y = pos.y;   	
     	renderQueues[queueIndex].add(element);
     }
     
     public void swap(RenderingThread renderer) {
         renderQueues[queueIndex].commitUpdates();
 
         // This code will block if the previous queue is still being executed.
         renderer.setDrawQueue(renderQueues[queueIndex]);
         
         final int lastQueue = (queueIndex == 0) ? DRAW_QUEUE_COUNT - 1 : queueIndex - 1;
         
         //Clear the old queues.
         FixedSizeArray<RenderElement> objects;
         objects = renderQueues[lastQueue].getObjects();
         clearQueue(objects, renderQueues);
         
         queueIndex = (queueIndex + 1) % DRAW_QUEUE_COUNT;
     }
 
 	private void clearQueue(FixedSizeArray<RenderElement> objects, ObjectHandler[] queue) {
 		final int count = objects.getCount();
 		RenderElement rElement;
 		
 		for (int i = count - 1; i >= 0; i--) {
 			rElement = (RenderElement) objects.removeLast();
 			rElementPool.release(rElement);
 		}
 	}
 }
