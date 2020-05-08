 // Copyright (c) 2012, Daniel Andersen (dani_ande@yahoo.dk)
 // All rights reserved.
 //
 // Redistribution and use in source and binary forms, with or without
 // modification, are permitted provided that the following conditions are met:
 //
 // 1. Redistributions of source code must retain the above copyright notice, this
 //    list of conditions and the following disclaimer.
 // 2. Redistributions in binary form must reproduce the above copyright notice,
 //    this list of conditions and the following disclaimer in the documentation
 //    and/or other materials provided with the distribution.
 // 3. The name of the author may not be used to endorse or promote products derived
 //    from this software without specific prior written permission.
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 // ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 // WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 // DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 // ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 // LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 // ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 // (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 // SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package com.trollsahead.qcumberless.gui.elements;
 
 import com.trollsahead.qcumberless.engine.DesignerEngine;
 import com.trollsahead.qcumberless.gui.Animation;
 import com.trollsahead.qcumberless.gui.ButtonBar;
 import com.trollsahead.qcumberless.gui.CumberlessMouseListener;
 import com.trollsahead.qcumberless.model.PlayResult;
 
 import java.awt.*;
 import java.io.File;
 import java.util.HashSet;
 import java.util.Set;
 
 public class RootElement extends Element {
     public static final int TYPE_ROOT = -1;
 
     public static final int PADDING_HORIZONTAL = 20;
 
     private static final Color COLOR_BG_NORMAL    = new Color(1.0f, 1.0f, 1.0f, 0.5f);
     private static final Color COLOR_BG_HIGHLIGHT = new Color(1.0f, 1.0f, 1.0f, 0.6f);
 
     private static final int SCROLLBAR_WIDTH = 10;
     private static final int SCROLLBAR_PADDING_HORIZONTAL = 3;
     private static final int SCROLLBAR_PADDING_VERTICAL = 5;
     private static final int SCROLLBAR_BOTTOM_PADDING = ButtonBar.BUTTONBAR_HEIGHT;
     private static final int SCROLLBAR_ROUNDING = 7;
 
     private int x = 0;
     private int y = 0;
     private int width = 100;
     private int height = 100;
 
     private int offsetY = 0;
 
     private int scrollbarX = 0;
     private int scrollbarY = 0;
     private int scrollbarHeight = 1;
     private int scrollbarMaxHeight = 1;
 
     private boolean isDraggingScrollbar = false;
 
     private int cachedGroupHeight = 1;
     public boolean isLoaded = false;
 
     private int verticalGap = 0;
 
     public RootElement() {
         animation.alphaAnimation.setAlpha(Animation.FADE_ALPHA_DEFAULT, Animation.FADE_SPEED_ENTRANCE);
         animation.moveAnimation.setRenderPosition(0, -100, Animation.MOVEMENT_SPEED_ENTRANCE);
         type = TYPE_ROOT;
         rootType = ROOT_NONE;
     }
 
     public void pushToPosition(int y) {
         verticalGap = y;
     }
     
    public void push(int y) {
        dragCanvas(y);
        updatePosition();
        stickChildrenToParentRenderPosition(true);
    }

     public void setBounds(int x, int y, int width, int height) {
         this.x = x;
         this.y = y;
         this.width = width;
         this.height = height - SCROLLBAR_BOTTOM_PADDING;
         updatePosition();
     }
 
     public void scroll(int deltaY) {
        if (offsetY > 0 && deltaY >= 0) {
            return;
        }
         if (isScrollbarTouched() || isDraggingScrollbar) {
             dragScrollbar(deltaY);
         } else {
             dragCanvas(deltaY);
         }
        offsetY = Math.max(Math.min(0, -(cachedGroupHeight - scrollbarMaxHeight)), Math.min(0, offsetY));
         updatePosition();
         stickChildrenToParentRenderPosition(true);
     }
 
     public void startDrag(boolean isControlDown) {
         if (isScrollbarTouched()) {
             dragScrollbar(0);
         }
     }
 
     public void endDrag() {
         isDraggingScrollbar = false;
     }
 
     private void dragCanvas(int deltaY) {
         isDraggingScrollbar = false;
         offsetY += deltaY;
     }
 
     private void dragScrollbar(int deltaY) {
         isDraggingScrollbar = true;
         int dragOffset = CumberlessMouseListener.mouseY - SCROLLBAR_PADDING_VERTICAL - scrollbarY;
         if (dragOffset < 0 || dragOffset >= scrollbarHeight) {
             scrollbarY = CumberlessMouseListener.mouseY - SCROLLBAR_PADDING_VERTICAL - (scrollbarHeight / 2);
         } else {
             scrollbarY += deltaY;
         }
         scrollbarY = Math.max(0, Math.min(scrollbarMaxHeight - scrollbarHeight, scrollbarY));
         offsetY = -(scrollbarY * Math.max(cachedGroupHeight - renderHeight, 0) / Math.max(scrollbarMaxHeight - scrollbarHeight, 1));
     }
 
     protected void updateSelf(long time) {
         if (animation.moveAnimation.isMoving()) {
             stickChildrenToParentRenderPosition(true);
         }
     }
 
     public String getTitle() {
         return "";
     }
 
     public void trashElement() {
     }
 
     public Element findGroup(int x, int y, int type) {
         for (Element child : children) {
             Element foundElement = child.findGroup(x, y, type);
             if (foundElement != null) {
                 return foundElement;
             }
         }
         return null;
     }
 
     public void fold() {
     }
 
     public void unfold() {
     }
 
     protected void renderBefore(Graphics2D g) {
     }
 
     protected void renderAfter(Graphics2D g) {
         if (this == DesignerEngine.cucumberRoot) {
             return;
         }
         renderScrollbar(g);
         cachedGroupHeight = groupHeight;
     }
 
     protected void renderHintsInternal(Graphics2D g) {
     }
 
     private void renderScrollbar(Graphics g) {
         scrollbarX = (int) animation.moveAnimation.renderX - PADDING_HORIZONTAL;
         if (this == DesignerEngine.featuresRoot) {
             scrollbarX += SCROLLBAR_PADDING_HORIZONTAL;
         } else {
             scrollbarX += renderWidth - SCROLLBAR_PADDING_HORIZONTAL - SCROLLBAR_WIDTH;
         }
 
         scrollbarMaxHeight = renderHeight - SCROLLBAR_PADDING_VERTICAL * 2 - 2;
         scrollbarHeight = scrollbarMaxHeight * renderHeight / Math.max(Math.max(renderHeight, cachedGroupHeight), 1);
 
         if (!isDraggingScrollbar) {
             if (offsetY < 0) {
                 scrollbarY = (scrollbarMaxHeight - scrollbarHeight) * (-offsetY) / Math.max(cachedGroupHeight - renderHeight, -offsetY);
             } else {
                 scrollbarY = 0;
             }
         }
 
         g.setColor(new Color(0.0f, 0.0f, 0.0f, 0.5f));
         g.fillRoundRect(scrollbarX, SCROLLBAR_PADDING_VERTICAL, SCROLLBAR_WIDTH, scrollbarMaxHeight, SCROLLBAR_ROUNDING, SCROLLBAR_ROUNDING);
 
         g.setColor(isScrollbarTouched() ? COLOR_BG_HIGHLIGHT : COLOR_BG_NORMAL);
         g.fillRoundRect(scrollbarX + 1, SCROLLBAR_PADDING_VERTICAL + scrollbarY + 1, SCROLLBAR_WIDTH - 2, scrollbarHeight - 2, SCROLLBAR_ROUNDING, SCROLLBAR_ROUNDING);
     }
 
     protected void calculateRenderPosition(Graphics2D g) {
         if (this == DesignerEngine.cucumberRoot) {
             return;
         }
         updatePosition();
         renderWidth = width;
         renderHeight = height;
         paddingHeight = 0;
         groupHeight = 0;
     }
 
     private void updatePosition() {
         int realX = x + PADDING_HORIZONTAL;
         int realY = offsetY + y + verticalGap;
         animation.moveAnimation.renderX += realX - animation.moveAnimation.realX;
         animation.moveAnimation.renderY += realY - animation.moveAnimation.realY;
         animation.moveAnimation.setRealPosition(realX, realY);
     }
 
     private boolean isScrollbarTouched() {
         return (this == DesignerEngine.featuresRoot && CumberlessMouseListener.mouseX <= scrollbarX + SCROLLBAR_WIDTH) ||
                (this == DesignerEngine.stepsRoot && CumberlessMouseListener.mouseX >= scrollbarX);
     }
 
     protected void foldFadeAnimation(float alpha, boolean animate) {
     }
 
     public void click(int clickCount) {
     }
 
     public boolean isDragable() {
         return true;
     }
 
     protected void applyDrag() {
         isDraggingScrollbar = false;
     }
 
     public void updateElementIndex(Element element, int index) {
     }
 
     public Element duplicate() {
         return new RootElement();
     }
 
     protected StringBuilder buildFeatureInternal(int addState, long time) {
         return new StringBuilder();
     }
 
     protected void updateStepsInternal() {
     }
 
     public boolean export(File directory) {
         boolean success = true;
         for (Element child : children) {
             success &= child.export(directory);
         }
         return success;
     }
 
     public boolean save() {
         boolean success = true;
         for (Element child : children) {
             success &= child.save();
         }
         return success;
     }
 
     public void scrollToTop() {
         scrollbarY = 0;
         offsetY = 0;
     }
 
     public Set<String> getTagsInternal() {
         return new HashSet<String>();
     }
 
     public void toggleColorSchemeInternal() {
     }
 
     public void setPlayResult(PlayResult.State playState) {
     }
 }
