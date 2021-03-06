 /*
  * Copyright (c) 2003, jMonkeyEngine - Mojo Monkey Coding
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without 
  * modification, are permitted provided that the following conditions are met:
  *
  * Redistributions of source code must retain the above copyright notice, this 
  * list of conditions and the following disclaimer. 
  * 
  * Redistributions in binary form must reproduce the above copyright notice, 
  * this list of conditions and the following disclaimer in the documentation 
  * and/or other materials provided with the distribution. 
  * 
  * Neither the name of the Mojo Monkey Coding, jME, jMonkey Engine, nor the 
  * names of its contributors may be used to endorse or promote products derived 
  * from this software without specific prior written permission. 
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE 
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
  * POSSIBILITY OF SUCH DAMAGE.
  *
  */
 package com.jme.scene;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.logging.Level;
 
 import com.jme.renderer.Renderer;
 import com.jme.util.LoggingSystem;
 
 /**
  * <code>Node</code> defines an internal node of a scene graph. The internal
  * node maintains a collection of children and handles merging said children 
  * into a single bound to allow for very fast culling of multiple nodes. 
  * Node allows for any number of children to be attached.
  * @author Mark Powell
 * @version $Id: Node.java,v 1.4 2003-12-10 01:05:36 mojomonkey Exp $
  */
 public class Node extends Spatial implements Serializable {
     //List to hold the children.
     private ArrayList children;
 
     /**
      * Constructor instantiates a new <code>Node</code> with a default empty
      * list for containing children.
      *
      */
     public Node() {
         super();
         children = new ArrayList();
         LoggingSystem.getLogger().log(Level.INFO, "Node created.");
     }
     
     /**
      * 
      * <code>getQuantity</code> returns the number of children this node 
      * maintains.
      * @return the number of children this node maintains.
      */
     public int getQuantity() {
         return children.size();
     }
     
     /**
      * 
      * <code>attachChild</code> attaches a child to this node. This node
      * becomes the child's parent. The current number of children maintained
      * is returned.
      * @param child the child to attach to this node.
      * @return the number of children maintained by this node.
      */
     public int attachChild(Spatial child) {
         if(child == null) {
             return children.size();
         }
         if(!children.contains(child)) {
             child.setParent(this);
             children.add(child);
         }
         LoggingSystem.getLogger().log(Level.INFO, "Child attached to this" +            " node");
         return children.size();
     }
     
     /**
      * 
      * <code>detachChild</code> removes a given child from the node's list.
      * This child will no longe be maintained.
      * @param child the child to remove.
      * @return the index the child was at. -1 if the child was not in the list.
      */
     public int detachChild(Spatial child) {
         int index = children.indexOf(child);
         if(index != -1) {
             children.remove(index);
             LoggingSystem.getLogger().log(Level.INFO, "Child removed.");
         }
         return index;
     }
     
     /**
      * 
      * <code>detachChildAt</code> removes a child at a given index. That 
      * child is returned for saving purposes.
      * @param index the index of the child to be removed.
      * @return the child at the supplied index.
      */
     public Spatial detachChildAt(int index) {
         LoggingSystem.getLogger().log(Level.INFO, "Child removed.");
         return (Spatial)children.remove(index);
     }
     
     /**
      * 
      * <code>setChild</code> places a child at a given index. If a child is
      * already set to that index the old child is returned.
      * @param i the index to set the child to.
      * @param child the child to attach.
      * @return the old child at the index.
      */
     public Spatial setChild(int i, Spatial child) {
         Spatial old = (Spatial)children.get(i);
         children.add(i,child);
         LoggingSystem.getLogger().log(Level.INFO, "Child attached to this" +
                     " node");
         return old;
     }
     
     /**
      * 
      * <code>getChild</code> returns a child at a given index.
      * @param i the index to retrieve the child from.
      * @return the child at a specified index.
      */
     public Spatial getChild(int i) {
         return (Spatial)children.get(i);
     }
 
     /**
      * <code>updateWorldData</code> updates all the children maintained by
      * this node.
      * @param time the frame time.
      */
     public void updateWorldData(float time) {
         super.updateWorldData(time);
 
         for (int i = 0; i < children.size(); i++) {
             Spatial child = (Spatial) children.get(i);
             if (child != null) {
                 child.updateGeometricState(time, false);
             }
         }
     }
    
     /**
      * <code>draw</code> calls the onDraw method for each child maintained
      * by this node.
      * @see com.jme.scene.Spatial#draw(com.jme.renderer.Renderer)
      * @param r the renderer to draw to.
      */
     public void draw(Renderer r) {
         for (int i = 0; i < children.size(); i++) {
             Spatial child = (Spatial) children.get(i);
             if (child != null)
                 child.onDraw(r);
         }
     }
 
     /**
      * <code>updateWorldBound</code> merges the bounds of all the children
      * maintained by this node. This will allow for faster culling operations.
      * @see com.jme.scene.Spatial#updateWorldBound()
      */
     public void updateWorldBound() {
         if(worldBound == null) {
             return;
         }
         boolean foundFirstBound = false;
         for (int i = 0; i < children.size(); i++) {
             Spatial child = (Spatial) children.get(i);
             if (child != null) {
                 if (foundFirstBound) {
                     // merge current world bound with child world bound
                    worldBound = worldBound.merge(child.getWorldBound());
                 } else {
                     // set world bound to first non-null child world bound
                     foundFirstBound = true;
                     worldBound = child.getWorldBound();
                 }
             }
         }
 
     }
 
 }
