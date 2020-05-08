 package org.skullforge.asteroidpush.ui;
 
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.geom.Rectangle;
 
 public class DesignerLayout extends BasicWidget {
 
    Widget catalogue;
    Widget blueprint;
    final float blueprintRatio = 0.75f;
 
    public DesignerLayout() {
       this.catalogue = null;
       this.blueprint = null;
    }
 
    public void setCatalogueWidget(Widget widget) {
       this.catalogue = widget;
    }
 
    public void setBlueprintWidget(Widget widget) {
       this.blueprint = widget;
    }
 
    @Override
    public void resize(Rectangle frame) {
       super.resize(frame);
 
       if (blueprint != null) {
          blueprint.resize(getBlueprintFrame(getFrame()));
       }
       if (catalogue != null) {
          catalogue.resize(getCatalogueFrame(getFrame()));
       }
    }
 
    @Override
    public void setHover(float x, float y) {
       super.setHover(x, y);
      if (blueprint != null) {
         if (getBlueprintFrame(getFrame()).contains(x, y)) {
            blueprint.setHover(x, y);
         } else {
            blueprint.resetHover();
         }
       }
      if (catalogue != null) {
         if (getCatalogueFrame(getFrame()).contains(x, y)) {
            catalogue.setHover(x, y);
         } else {
            catalogue.resetHover();
         }
       }
    }
 
    @Override
    public void resetHover() {
       super.resetHover();
       if (blueprint != null) {
          blueprint.resetHover();
       }
       if (catalogue != null) {
          catalogue.resetHover();
       }
    }
 
    @Override
    public void render(Graphics g) {
       if (blueprint != null) {
          blueprint.render(g);
       }
       if (catalogue != null) {
          catalogue.render(g);
       }
    }
 
    private Rectangle getCatalogueFrame(Rectangle frame) {
       return new Rectangle(frame.getWidth() * blueprintRatio, frame.getY(),
             frame.getWidth() * (1.0f - blueprintRatio), frame.getHeight());
    }
 
    private Rectangle getBlueprintFrame(Rectangle frame) {
       return new Rectangle(frame.getX(), frame.getY(), frame.getWidth()
             * blueprintRatio, frame.getHeight());
    }
 
 }
