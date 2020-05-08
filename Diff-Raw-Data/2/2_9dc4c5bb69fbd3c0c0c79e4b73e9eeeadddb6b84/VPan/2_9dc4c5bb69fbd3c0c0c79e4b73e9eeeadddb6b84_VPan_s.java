 /*
  * VPan.java.java
  *
  * Created on 01-03-2010 01:33:52 PM
  *
  * Copyright 2010 Jonathan Colt
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package colt.nicity.view.core;
 
 import colt.nicity.core.lang.ICallback;
 import colt.nicity.core.lang.UDouble;
 import colt.nicity.core.lang.UFloat;
 import colt.nicity.core.memory.struct.WH_F;
 import colt.nicity.core.memory.struct.XYWH_I;
 import colt.nicity.core.memory.struct.XY_I;
 import colt.nicity.view.border.ViewBorder;
 import colt.nicity.view.event.AInputEvent;
 import colt.nicity.view.event.AViewEvent;
 import colt.nicity.view.event.MouseDragged;
 import colt.nicity.view.event.MouseEntered;
 import colt.nicity.view.event.MouseExited;
 import colt.nicity.view.event.MouseMoved;
 import colt.nicity.view.event.MousePressed;
 import colt.nicity.view.event.MouseReleased;
 import colt.nicity.view.event.MouseWheel;
 import colt.nicity.view.flavor.ScrollFlavor;
 import colt.nicity.view.interfaces.ICanvas;
 import colt.nicity.view.interfaces.IDrop;
 import colt.nicity.view.interfaces.IDropMode;
 import colt.nicity.view.interfaces.IEvent;
 import colt.nicity.view.interfaces.IMouseEvents;
 import colt.nicity.view.interfaces.IMouseMotionEvents;
 import colt.nicity.view.interfaces.IMouseWheelEvents;
 import colt.nicity.view.interfaces.IView;
 import colt.nicity.view.paint.UPaint;
 import java.awt.Polygon;
 
 /**
  *
  * @author Administrator
  */
 public class VPan extends VClip implements IDrop, IMouseWheelEvents, IMouseEvents, IMouseMotionEvents {
 
     public static void main(String[] args) {
         ViewColor.onGray();
         UV.exitFrame(new VPan(new VChain(UV.cSN, new VButton("Button"), new VBox(800, 800) {
 
             @Override
             public void paintBorder(ICanvas g, int _x, int _y, int _w, int _h) {
                 UPaint.checked(g, _x, _y, _w, _h, AColor.lightGray, AColor.darkGray);
             }
         }), 400, 400), "Pan");
     }
     static ScrollFlavor flavor = new ScrollFlavor();
     float maxWBeforePan = -1;
     float maxHBeforePan = -1;
     float fixedW = -1;
     float fixedH = -1;
     int resize = 6;
     /**
      *
      */
     public int scrollBarSize = 16;
     /**
      *
      */
     public boolean operable = true;
     /**
      *
      */
     public ICallback dropCallback;
     /**
      *
      */
     public ICallback droppedCallback;
     /**
      *
      */
     public AColor barColor = ViewColor.cThemeScroll;
     /**
      *
      */
     public boolean resizeable = true;
     /**
      *
      */
     protected boolean resizingX = false;
     /**
      * 
      */
     protected boolean resizingY = false;
     /**
      *
      */
     protected boolean scrollingX = false;
     /**
      *
      */
     protected boolean scrollingY = false;
     private boolean paintYScrollbar = false;
     private boolean paintXScrollbar = false;
     private boolean paintXResizing = false;
     private boolean paintYResizing = false;
 
     /**
      *
      * @param _view
      * @param _w
      * @param _h
      * @param _autoCenterX
      * @param _autoCenterY
      */
     public VPan(
             IView _view,
             float _w, float _h,
             boolean _autoCenterX, boolean _autoCenterY) {
         this(_view, _w, _h);
         setAutoCenter(_autoCenterX, _autoCenterY);
     }
 
     /**
      *
      * @param _view
      * @param _w
      * @param _h
      * @param _autoCenter
      */
     public VPan(IView _view, float _w, float _h, boolean _autoCenter) {
         this(_view, _w, _h);
         setAutoCenter(_autoCenter);
     }
 
     /**
      *
      * @param _view
      * @param _w
      * @param _h
      */
     public VPan(IView _view, float _w, float _h) {
         super(_view, _w, _h);
         setBorder(new ViewBorder());
         overScroll = resize + scrollBarSize;
     }
 
     /**
      *
      * @param _barColor
      */
     public void setBarColor(AColor _barColor) {
         barColor = _barColor;
     }
 
     @Override
     public synchronized IView spans(int spanMasks) {
         return this;
     }
 
     @Override
     public void mend() {
         enableFlag(UV.cRepair);//??
         super.mend();
     }
 
     /**
      *
      * @param _autoCenter
      */
     public final void setAutoCenter(boolean _autoCenter) {
         autoCenterX = _autoCenter;
         autoCenterY = _autoCenter;
     }
 
     /**
      *
      * @param _autoCenterX
      * @param _autoCenterY
      */
     public final void setAutoCenter(boolean _autoCenterX, boolean _autoCenterY) {
         autoCenterX = _autoCenterX;
         autoCenterY = _autoCenterY;
     }
 
     /**
      *
      * @param _w
      * @param _h
      */
     public void setSizeBeforeScroll(int _w, int _h) {
         maxWBeforePan = _w;
         maxHBeforePan = _h;
     }
 
     /**
      *
      * @param _w
      * @param _h
      */
     public void fixedSize(int _w, int _h) {
         fixedW = _w;
         fixedH = _h;
         if (fixedW != -1) {
             w = fixedW;
         }
         if (fixedH != -1) {
             h = fixedH;
         }
     }
 
     @Override
     public void paintBorder(ICanvas _g, int _x, int _y, int _w, int _h) {
         super.paintBorder(_g, _x, _y, _w, _h);
 
         if (operable) {
             AColor c = barColor;
             if (alignY > -1 && view.getH() > getH() && !paintYResizing) {//paintYScrollbar
                 _g.setColor(barColor);
                 XYWH_I r = panY();
                 c = barColor;
                 _g.paintFlavor(flavor, _x + r.x + r.w, _y + r.y, resize, r.h, c);
 
                 if (scrollingY || paintYScrollbar) {
                     c = ViewColor.cThemeAccent;
 
                     _g.paintFlavor(flavor, _x + r.x, _y + r.y, r.w + resize, r.h, c);
 
                     _g.setColor(ViewColor.cThemeAccent.darker());
                     _g.line(_x + r.x + 3, _y + r.y + (r.h / 2) - 2, _x + r.x + r.w + resize - 6, _y + r.y + (r.h / 2) - 2);
                     _g.line(_x + r.x + 3, _y + r.y + (r.h / 2), _x + r.x + r.w + resize - 6, _y + r.y + (r.h / 2));
                     _g.line(_x + r.x + 3, _y + r.y + (r.h / 2) + 2, _x + r.x + r.w + resize - 6, _y + r.y + (r.h / 2) + 2);
 
 
                 }
 
             }
             if (alignX > -1 && view.getW() > getW() && !paintXResizing) {//paintXScrollbar
 
                 _g.setColor(barColor);
                 XYWH_I r = panX();
                 c = barColor;
                 _g.paintFlavor(flavor, _x + r.x, _y + r.y + r.h, r.w, resize, c);
 
                 if (scrollingX || paintXScrollbar) {
                     c = ViewColor.cThemeAccent;
 
                     _g.paintFlavor(flavor, _x + r.x, _y + r.y, r.w, r.h + resize, c);
 
                     _g.setColor(ViewColor.cThemeAccent.darker());
                     _g.line(_x + r.x + (r.w / 2) - 2, _y + r.y + 3, _x + r.x + (r.w / 2) - 2, _y + r.y + r.h + resize - 6);
                     _g.line(_x + r.x + (r.w / 2), _y + r.y + 3, _x + r.x + (r.w / 2), _y + r.y + r.h + resize - 6);
                     _g.line(_x + r.x + (r.w / 2) + 2, _y + r.y + 3, _x + r.x + (r.w / 2) + 2, _y + r.y + r.h + resize - 6);
                 }
 
 
             }
             if (resizeable) {//&& resizingX && resizingY) {
                 Polygon p = new Polygon();
                 p.addPoint(_x + _w - resize, _y + _h - resize);
                 p.addPoint(_x + _w - resize, _y + _h - scrollBarSize);
                 p.addPoint(_x + _w, _y + _h - scrollBarSize);
                 p.addPoint(_x + _w, _y + _h);
                 p.addPoint(_x + _w - scrollBarSize, _y + _h);
                 p.addPoint(_x + _w - scrollBarSize, _y + _h - resize);
                 p.addPoint(_x + _w - resize, _y + _h - resize);
 
                 _g.setColor(ViewColor.cThemeAccent);
                 _g.polygon(true, p.xpoints, p.ypoints, p.npoints);
                 _g.setColor(ViewColor.cThemeAccent.darker());
                 _g.polygon(false, p.xpoints, p.ypoints, p.npoints);
             }
 
 
             XYWH_I rx = resizeX();
             XYWH_I ry = resizeY();
 
             c = barColor;
             if (paintXResizing) {
                 //c = ViewColor.cThemeActive;
                 //_g.paintFlavor(flavor, rx.x, rx.y, rx.w, rx.h, c);
 
                 Polygon p = new Polygon();
                 p.addPoint(rx.x + (rx.w / 2), rx.y + rx.h - 30);
                 p.addPoint(rx.x + (rx.w / 2) - 8, rx.y + rx.h - 20);
                 p.addPoint(rx.x + (rx.w / 2) - 1, rx.y + rx.h - 20);
                 p.addPoint(rx.x + (rx.w / 2) - 1, rx.y + rx.h - 14);
                 p.addPoint(rx.x + (rx.w / 2) - 8, rx.y + rx.h - 14);
                 p.addPoint(rx.x + (rx.w / 2) - 1, rx.y + rx.h - 4);
                 p.addPoint(rx.x + (rx.w / 2) - 10, rx.y + rx.h - 4);
                 p.addPoint(rx.x + (rx.w / 2) - 10, rx.y + rx.h);
                 p.addPoint(rx.x + (rx.w / 2) + 10, rx.y + rx.h);
                 p.addPoint(rx.x + (rx.w / 2) + 10, rx.y + rx.h - 4);
                 p.addPoint(rx.x + (rx.w / 2) + 1, rx.y + rx.h - 4);
                 p.addPoint(rx.x + (rx.w / 2) + 8, rx.y + rx.h - 14);
                 p.addPoint(rx.x + (rx.w / 2) + 1, rx.y + rx.h - 14);
                 p.addPoint(rx.x + (rx.w / 2) + 1, rx.y + rx.h - 20);
                 p.addPoint(rx.x + (rx.w / 2) + 8, rx.y + rx.h - 20);
                 p.addPoint(rx.x + (rx.w / 2), rx.y + rx.h - 30);
 
                 _g.setColor(ViewColor.cThemeAccent);
                 _g.polygon(true, p.xpoints, p.ypoints, p.npoints);
                 _g.setColor(ViewColor.cThemeAccent.darker());
                 _g.polygon(false, p.xpoints, p.ypoints, p.npoints);
             }
             c = barColor;
             if (paintYResizing) {
                 //c = ViewColor.cThemeActive;
                 //_g.paintFlavor(flavor, ry.x, ry.y, ry.w, ry.h, c);
 
 
                 Polygon p = new Polygon();
                 p.addPoint(ry.x + ry.w - 30, ry.y + (ry.h / 2));
                 p.addPoint(ry.x + ry.w - 20, ry.y + (ry.h / 2) - 8);
                 p.addPoint(ry.x + ry.w - 20, ry.y + (ry.h / 2) - 1);
                 p.addPoint(ry.x + ry.w - 14, ry.y + (ry.h / 2) - 1);
                 p.addPoint(ry.x + ry.w - 14, ry.y + (ry.h / 2) - 8);
                 p.addPoint(ry.x + ry.w - 4, ry.y + (ry.h / 2) - 1);
                 p.addPoint(ry.x + ry.w - 4, ry.y + (ry.h / 2) - 10);
                 p.addPoint(ry.x + ry.w, ry.y + (ry.h / 2) - 10);
                 p.addPoint(ry.x + ry.w, ry.y + (ry.h / 2) + 10);
                 p.addPoint(ry.x + ry.w - 4, ry.y + (ry.h / 2) + 10);
                 p.addPoint(ry.x + ry.w - 4, ry.y + (ry.h / 2) + 1);
                 p.addPoint(ry.x + ry.w - 14, ry.y + (ry.h / 2) + 8);
                 p.addPoint(ry.x + ry.w - 14, ry.y + (ry.h / 2) + 1);
                 p.addPoint(ry.x + ry.w - 20, ry.y + (ry.h / 2) + 1);
                 p.addPoint(ry.x + ry.w - 20, ry.y + (ry.h / 2) + 8);
                 p.addPoint(ry.x + ry.w - 30, ry.y + (ry.h / 2));
 
                 _g.setColor(ViewColor.cThemeAccent);
                 _g.polygon(true, p.xpoints, p.ypoints, p.npoints);
                 _g.setColor(ViewColor.cThemeAccent.darker());
                 _g.polygon(false, p.xpoints, p.ypoints, p.npoints);
             }
 
         }
     }
 
     /**
      *
      * @return
      */
     public XYWH_I incUp() {
         XYWH_I p = panX();
         return new XYWH_I(p.x, p.y, p.w, p.h / 2);
     }
 
     /**
      *
      * @return
      */
     public XYWH_I incDown() {
         XYWH_I p = panX();
         return new XYWH_I(p.x, p.y + (p.h / 2), p.w, p.h / 2);
     }
 
     /**
      *
      * @return
      */
     public XYWH_I incRight() {
         XYWH_I p = panY();
         return new XYWH_I(p.x + (p.w / 2), p.y, p.w / 2, p.h);
     }
 
     /**
      *
      * @return
      */
     public XYWH_I incLeft() {
         XYWH_I p = panY();
         return new XYWH_I(p.x, p.y, p.w / 2, p.h);
     }
 
     /**
      *
      * @return
      */
     public XYWH_I resizeY() {
         return new XYWH_I((int) (getW() - resize), 0, resize, getH());
     }
 
     /**
      *
      * @return
      */
     public XYWH_I resizeX() {
         return new XYWH_I(0, (int) (getH() - resize), getW(), resize);
     }
 
     /**
      *
      * @return
      */
     public XYWH_I panX() {
         int r = resize;
         if (alignY < 0) {
             r = 0;
         }
         float _w = getW() - r;// top botton and resize areas = scrollBarSize*3
         int _x = (int) (alignX * _w);
         int pw = 0;
         if (wSlack != 0) {
             pw = (int) ((_w / (_w + wSlack)) * _w);
         }
         if (pw < scrollBarSize) {
             pw = scrollBarSize;
         }
         return new XYWH_I((int) (_x - (pw * alignX)), (int) (getH() - (scrollBarSize + r)), pw, scrollBarSize);
     }
 
     /**
      *
      * @return
      */
     public XYWH_I panY() {
         int r = resize;
         if (alignX < 0) {
             r = 0;
         }
         float _h = getH() - r;// top botton and resize areas = scrollBarSize*3
         int _y = (int) (alignY * _h);
         int ph = 0;
         if (hSlack != 0) {
             ph = (int) ((_h / (_h + hSlack)) * _h);
         }
         if (ph < scrollBarSize) {
             ph = scrollBarSize;
         }
         return new XYWH_I((int) (getW() - (scrollBarSize + r)), (int) (_y - (ph * alignY)), scrollBarSize, ph);
     }
 
     @Override
     public void paintBackground(ICanvas _g, int _x, int _y, int _w, int _h) {
         if (maxWBeforePan > -1 && maxWBeforePan > view.getW()) {
             //alignX = -1;
             w = view.getW();
             parent.layoutInterior();
             parent.repair();
             parent.flush();
         } else if (maxWBeforePan > -1 && alignX < 0) {
             w = maxWBeforePan;
             alignX = 0;
             parent.layoutInterior();
             parent.repair();
             parent.flush();
         }
         if (maxHBeforePan > -1 && maxHBeforePan > view.getH()) {
             //alignY = -1;
             h = view.getH();
             parent.layoutInterior();
             parent.repair();
             parent.flush();
         } else if (maxHBeforePan > -1 && alignY < 0) {
             h = maxHBeforePan;
             alignY = 0;
             parent.layoutInterior();
             parent.repair();
             parent.flush();
         }
         super.paintBackground(_g, _x, _y, _w, _h);
     }
 
     @Override
     public IView disbatchEvent(IView parent, AViewEvent event) {
         if (isPanEvent(event) || scrollingX || scrollingY || resizingX || resizingY) {
             return this;
         }
         if (event instanceof MouseMoved) {
             XY_I p = ((MouseMoved) event).getPoint();
             if ((alignX > -1 && panX().contains(p)) || (alignY > -1 && panY().contains(p))) {
                 if (!mouseIsDown) {
                     setPaintingScrollBars(p, isPanEvent(event));
                 }
                 return this;
 
             } else if ((p.x > getW() - resize) || (p.y > getH() - resize)) {
                 if (!mouseIsDown) {
                     setPaintingScrollBars(p, isPanEvent(event));
                 }
                 return this;
             } else if ((p.x > getW() - scrollBarSize) && (p.y > getH() - scrollBarSize)) {
                 if (!mouseIsDown) {
                     setPaintingScrollBars(p, isPanEvent(event));
                 }
                 return this;
             } else {
                 paintXScrollbar = false;
                 paintYScrollbar = false;
                 paintXResizing = false;
                 paintYResizing = false;
                 return super.disbatchEvent(parent, event);
             }
         } else if (event instanceof MousePressed) {
             XY_I p = ((MousePressed) event).getPoint();
             setModePoint(p, isPanEvent(event));
             if ((alignX > -1 && panX().contains(p)) || (alignY > -1 && panY().contains(p))) {
                 setPan(false);
                 return this;
             } else if ((p.x > getW() - resize) || (p.y > getH() - resize)) {
                 setPan(false);
                 return this;
             } else if ((p.x > getW() - scrollBarSize) && (p.y > getH() - scrollBarSize)) {
                 setPan(false);
                 return this;
             }
 
 
             if (scrollingX || scrollingY || resizingX || resizingY) {
                 return this;
             } else {
                 return super.disbatchEvent(parent, event);
             }
         } else if (event instanceof MouseReleased) {
             XY_I p = ((MouseReleased) event).getPoint();
             setModePoint(p, isPanEvent(event));
             if ((alignX > -1 && panX().contains(p)) || (alignY > -1 && panY().contains(p))) {
                 setPan(false);
                 return this;
             } else if ((p.x > getW() - resize) || (p.y > getH() - resize)) {
                 setPan(false);
                 return this;
             }
             if (scrollingX || scrollingY || resizingX || resizingY) {
                 return this;
             } else {
                 return super.disbatchEvent(parent, event);
             }
         } else if (event instanceof  MouseExited) {
             if (!mouseIsDown) {
                XY_I p = ((MouseReleased) event).getPoint();
                 setModePoint(p, isPanEvent(event));
             }
             return super.disbatchEvent(parent, event);
         } else {
             return super.disbatchEvent(parent, event);
         }
     }
 
     boolean isPanEvent(IEvent event) {
         if (event instanceof AInputEvent) {
             boolean isPanEvent = (((AInputEvent) event).isShiftDown() && ((AInputEvent) event).isControlDown());
             if (isPanEvent) {
                 return isPanEvent;
             }
         }
         lmp = null;
         return false;
     }
 
     // IMouseWheelEvents
     @Override
     public void mouseWheel(MouseWheel _e) {
         int rotation = _e.getWheelRotation();
         if (_e.isShiftDown()) {
             if (rotation < 0) {
                 for (int i = rotation; i < 0; i++) {
                     incRightScroll();
                 }
             } else {
                 for (int i = 0; i < rotation; i++) {
                     incLeftScroll();
                 }
             }
         } else {
             if (rotation < 0) {
                 for (int i = rotation; i < 0; i++) {
                     incUpScroll();
                 }
             } else {
                 for (int i = 0; i < rotation; i++) {
                     incDownScroll();
                 }
             }
         }
     }
 
     /**
      *
      */
     protected void incUpScroll() {
         float p = getAlignY() - (((float) getH() / 3) / ((float) getContent().getH()));
         setPositionY(UFloat.checkFloat(p, 0.0f));
         new Thread() {
 
             @Override
             public void run() {
                 while (mouseIsDown && !drug) {
                     try {
                         Thread.sleep(600);
                         if (!mouseIsDown) {
                             break;
                         }
                     } catch (InterruptedException ex) {
                     }
                     float p = getAlignY() - (((float) getH() / 3) / ((float) getContent().getH()));
                     setPositionY(UFloat.checkFloat(p, 0.0f));
                 }
             }
         }.start();
     }
 
     /**
      *
      */
     protected void incDownScroll() {
         float p = getAlignY() + (((float) getH() / 3) / ((float) getContent().getH()));
         setPositionY(UFloat.checkFloat(p, 1.0f));
         new Thread() {
 
             @Override
             public void run() {
                 while (mouseIsDown && !drug) {
                     try {
                         Thread.sleep(600);
                         if (!mouseIsDown) {
                             break;
                         }
                     } catch (InterruptedException ex) {
                     }
                     float p = getAlignY() + (((float) getH() / 3) / ((float) getContent().getH()));
                     setPositionY(UFloat.checkFloat(p, 1.0f));
                 }
             }
         }.start();
     }
 
     /**
      *
      */
     protected void incLeftScroll() {
         float p = getAlignX() - (((float) getW() / 3) / ((float) getContent().getW()));
         setPositionX(UFloat.checkFloat(p, 0.0f));
         new Thread() {
 
             @Override
             public void run() {
                 while (mouseIsDown && !drug) {
                     try {
                         Thread.sleep(600);
                         if (!mouseIsDown) {
                             break;
                         }
                     } catch (InterruptedException ex) {
                     }
                     float p = getAlignX() - (((float) getW() / 3) / ((float) getContent().getW()));
                     setPositionX(UFloat.checkFloat(p, 0.0f));
                 }
             }
         }.start();
     }
 
     /**
      *
      */
     protected void incRightScroll() {
         float p = getAlignX() + (((float) getW() / 3) / ((float) getContent().getW()));
         setPositionX(UFloat.checkFloat(p, 1.0f));
         new Thread() {
 
             @Override
             public void run() {
                 while (mouseIsDown && !drug) {
                     try {
                         Thread.sleep(600);
                         if (!mouseIsDown) {
                             break;
                         }
                     } catch (InterruptedException ex) {
                     }
                     float p = getAlignX() + (((float) getW() / 3) / ((float) getContent().getW()));
                     setPositionX(UFloat.checkFloat(p, 1.0f));
                 }
             }
         }.start();
     }
 
     /**
      *
      * @param _position
      */
     public void setPositionX(float _position) {
         if (_position < 0.0f) {
             _position = 0.0f;
         }
         if (_position > 1.0f) {
             _position = 1.0f;
         }
         setAlignX(_position, this);
         paint();
     }
 
     /**
      *
      * @param _position
      */
     public void setPositionY(float _position) {
         if (_position < 0.0f) {
             _position = 0.0f;
         }
         if (_position > 1.0f) {
             _position = 1.0f;
         }
         setAlignY(_position, this);
         paint();
     }
 
     /**
      *
      * @return
      */
     public float getPositionX() {
         return getAlignX();
     }
 
     /**
      *
      * @return
      */
     public float getPositionY() {
         return getAlignY();
     }
 
     /**
      *
      * @param _pan
      */
     public void setPan(boolean _pan) {
         //grabFocus();
         //getRootView().setMouseWheelFocus(0,this);//!!
     }
     boolean mouseIsDown = false;
     // IMouseEvents
 
     @Override
     public void mouseEntered(MouseEntered _e) {
         DragAndDrop.cDefault.mouseEntered(_e);
         mouseIsDown = false;
     }
 
     @Override
     public void mouseExited(MouseExited _e) {
         DragAndDrop.cDefault.mouseExited(_e);
         mouseIsDown = false;
     }
 
     /**
      *
      * @param _p
      * @param _panXY
      */
     public void setModePoint(XY_I _p, boolean _panXY) {
 
         if (_panXY) {
             scrollingX = true;
             scrollingY = true;
         } else if (resizeable && (_p.x < getW() && _p.y < getH()) && (_p.x > getW() - scrollBarSize && _p.y > getH() - scrollBarSize)) {
             resizingX = true;
             resizingY = true;
             scrollingX = false;
             scrollingY = false;
         } else if (resizeable && resizeY().contains(_p)) {
             resizingX = true;
             resizingY = false;
             scrollingX = false;
             scrollingY = false;
         } else if (resizeable && resizeX().contains(_p)) {
             resizingX = false;
             resizingY = true;
             scrollingX = false;
             scrollingY = false;
         } else if (panY().contains(_p) || incRight().contains(_p) || incLeft().contains(_p)) {
             resizingX = false;
             resizingY = false;
             scrollingX = false;
             scrollingY = true;
         } else if (panX().contains(_p) || incUp().contains(_p) || incDown().contains(_p)) {
             resizingX = false;
             resizingY = false;
             scrollingX = true;
             scrollingY = false;
         } else {
             resizingX = false;
             resizingY = false;
             scrollingX = false;
             scrollingY = false;
         }
     }
     WH_F mpwh = new WH_F();
     float mpxp = 0f;
     float mpyp = 0f;
 
     @Override
     public void mousePressed(MousePressed _e) {
         if (_e.getClickCount() > 0) {
             DragAndDrop.cDefault.mousePressed(_e);
         }
         mpwh = new WH_F(getW(), getH());
         mpxp = getPositionX();
         mpyp = getPositionY();
         mouseIsDown = true;
     }
 
     @Override
     public void mouseReleased(MouseReleased _e) {
         setModePoint(_e.getPoint(), isPanEvent(_e));
         if (incDown().contains(_e.getPoint())) {
             incDownScroll();
         }
         if (incLeft().contains(_e.getPoint())) {
             incLeftScroll();
         }
         if (incRight().contains(_e.getPoint())) {
             incRight();
         }
         if (incUp().contains(_e.getPoint())) {
             incUpScroll();
         }
 
         mouseIsDown = false;
         drug = false;
         PickupAndDrop.cDefault.event(_e);
         if (_e.getClickCount() > 0) {
             DragAndDrop.cDefault.mouseReleased(_e);
 
         }
         resizingX = false;
         resizingY = false;
         scrollingX = false;
         scrollingY = false;
         lmp = null;
     }
 
     // IMouseMotionEvents
     /**
      *
      * @param _p
      * @param _pan
      */
     public void setPaintingScrollBars(XY_I _p, boolean _pan) {
         if (mouseIsDown) {
             return;
         }
         if (resizeX().contains(_p)) {
             paintXResizing = true;
             paint();
             return;
         } else {
             paintXResizing = false;
         }
         if (resizeY().contains(_p)) {
             paintYResizing = true;
             paint();
             return;
         } else {
             paintYResizing = false;
         }
         if (panY().contains(_p) || _pan) {
             if (paintYScrollbar); else {
                 getRootView().setMouseWheelFocus(0, this);
                 paintYScrollbar = true;
                 paint();
             }
         } else {
             paintYScrollbar = false;
             paint();
         }
 
         if (panX().contains(_p) || _pan) {
             if (paintXScrollbar); else {
                 getRootView().setMouseWheelFocus(0, this);
                 paintXScrollbar = true;
                 paint();
             }
         } else {
             paintXScrollbar = false;
             paint();
         }
     }
     XY_I lmp = null;
 
     @Override
     public void mouseMoved(MouseMoved _e) {
         if (mouseIsDown) {
             return;
         }
         XY_I p = _e.getPoint();
         setPaintingScrollBars(p, isPanEvent(_e));
         if (isPanEvent(_e)) {
             if (lmp == null) {
                 lmp = _e.getPoint();
                 return;
             }
             XY_I mp = _e.getPoint();
             float yrate = (lmp.y - mp.y) / (getH() / 2);
             setPositionY(getPositionY() - yrate);
             float xrate = (lmp.x - mp.x) / (getW() / 2);
             setPositionX(getPositionX() - xrate);
 
             lmp = mp;
         }
     }
     boolean drug = false;
 
     @Override
     public void mouseDragged(MouseDragged _e) {
         DragAndDrop.cDefault.mouseDragged(_e);
         if (!operable) {
             return;
         }
         drug = true;
         if (resizeable && (resizingX || resizingY)) {
 
             if (fixedW == -1 && resizingX) {
                 w = mpwh.w + _e.getSumDeltaX();
             }
             if (fixedH == -1 && resizingY) {
                 h = mpwh.h + _e.getSumDeltaY();
             }
             layoutInterior();
             parent.layoutInterior();
             parent.paint();
         }
         if (scrollingY) {
             float s = getH() - resize;
             int p = 0;
             if (hSlack != 0) {
                 p = (int) ((s / (s + hSlack)) * s);
             }
 
             double delta = _e.getSumDeltaY() / (s - p);
             setPositionY((float) UDouble.clamp(mpyp + delta, 0, 1));
         }
         if (scrollingX) {
             float s = getW() - resize;
             int p = 0;
             if (wSlack != 0) {
                 p = (int) ((s / (s + wSlack)) * s);
             }
 
             double delta = _e.getSumDeltaX() / (s - p);
             setPositionX((float) UDouble.clamp(mpxp + delta, 0, 1));
         }
     }
 
     // IDrop
     @Override
     public IDropMode accepts(Object value, AInputEvent _e) {
         if (dropCallback == null) {
             return null;
         }
         return (IDropMode) dropCallback.callback(new Object[]{value, _e});
     }
 
     @Override
     public void dropParcel(final Object value, final IDropMode mode) {
         if (droppedCallback == null) {
             return;
         }
         new Thread() {// refactor add to drop callstack instead
 
             @Override
             public void run() {
                 droppedCallback.callback(new Object[]{value, mode});
             }
         }.start();
     }
 }
