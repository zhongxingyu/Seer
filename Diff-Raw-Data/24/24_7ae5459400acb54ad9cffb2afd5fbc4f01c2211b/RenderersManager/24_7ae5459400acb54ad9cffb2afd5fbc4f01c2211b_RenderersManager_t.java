 package com.redshape.renderer.managers;
 
 import java.util.Queue;
import java.util.Stack;
 import java.util.concurrent.LinkedBlockingQueue;
 
 /**
  * @author Cyril A. Karpenko <self@nikelin.ru>
  * @package com.redshape.renderer.managers
  * @date 2/22/12 {2:15 AM}
  */
 public final class RenderersManager {
     private static final RenderersManager instance = new RenderersManager();
     
     public static RenderersManager getInstance() {
         return instance;
     }
     
    private Stack<Object> invalid = new Stack<Object>();
     
     public boolean isValid( Object object ) {
         return this.invalid.contains(object);
     }
     
     public void addInvalid( Object object ) {
         this.invalid.add(object);
     }
 
    public Stack<?> getInvalid() {
         return this.invalid;
     }
     
     public void markValid( Object object ) {
         this.invalid.remove(object);
     }
     
     
 }
