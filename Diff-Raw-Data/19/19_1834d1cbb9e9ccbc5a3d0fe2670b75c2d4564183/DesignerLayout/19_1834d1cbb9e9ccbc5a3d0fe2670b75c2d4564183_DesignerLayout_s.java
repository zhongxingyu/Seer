 package org.skullforge.asteroidpush.ui;
 
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.geom.Rectangle;
 import org.newdawn.slick.geom.Vector2f;
 
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
    public void render(Graphics g, Rectangle frame) {
       Vector2f hoverPosition = getHover();
 
       if (blueprint != null) {
          Rectangle blueprintFrame = getBlueprintFrame(frame);
          if (hoverPosition != null
                && blueprintFrame.contains(hoverPosition.x, hoverPosition.y)) {
             blueprint.setHover(hoverPosition.x - blueprintFrame.getX(),
                                hoverPosition.y - blueprintFrame.getY());
          } else {
             blueprint.resetHover();
          }
          blueprint.render(g, blueprintFrame);
       }
 
       if (catalogue != null) {
          Rectangle catalogueFrame = getCatalogueFrame(frame);
          if (hoverPosition != null
                && catalogueFrame.contains(hoverPosition.x, hoverPosition.y)) {
             catalogue.setHover(hoverPosition.x - catalogueFrame.getX(),
                                hoverPosition.y - catalogueFrame.getY());
          } else {
             catalogue.resetHover();
          }
          catalogue.render(g, catalogueFrame);
       }
    }
 
    private Rectangle getCatalogueFrame(Rectangle frame) {
      return new Rectangle(frame.getX() * (1.0f - blueprintRatio),
             frame.getY(), frame.getWidth() * (1.0f - blueprintRatio),
             frame.getHeight());
    }
 
    private Rectangle getBlueprintFrame(Rectangle frame) {
       return new Rectangle(frame.getX(), frame.getY(), frame.getWidth()
             * blueprintRatio, frame.getHeight());
    }
 
 }
