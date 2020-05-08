 package org.skullforge.asteroidpush.ui;
 
 import org.newdawn.slick.Color;
 import org.newdawn.slick.Font;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.geom.Rectangle;
 import org.skullforge.asteroidpush.designer.catalogue.ModuleCatalogue;
 import org.skullforge.asteroidpush.designer.data.ModuleData;
 
 public class ShipModuleList implements Widget {
 
    private final float marginX = 3.0f;
    private final float marginY = 3.0f;
 
    public ShipModuleList(ModuleCatalogue moduleCatalogue, Font font) {
       this.moduleCatalogue = moduleCatalogue;
       this.font = font;
    }
 
    @Override
    public void render(Graphics g, Rectangle frame) {
       g.setColor(Color.gray);
       g.drawRoundRect(frame.getX(), frame.getY(), frame.getWidth(), frame.getHeight(), 5);
 
       Font currentFont = g.getFont();
       g.setFont(font);
       g.setColor(Color.white);
       float top = frame.getY() + marginY;
       for (ModuleData module : this.moduleCatalogue.getModuleData())
       {
          g.drawString(module.getName(), frame.getX() + marginX, top);
          top += g.getFont().getLineHeight();
       }
       g.setFont(currentFont);
    }
 
    ModuleCatalogue moduleCatalogue;
    Font font;
 }
