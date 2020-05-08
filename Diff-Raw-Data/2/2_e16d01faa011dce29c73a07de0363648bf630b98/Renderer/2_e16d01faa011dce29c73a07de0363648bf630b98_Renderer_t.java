 package net.avh4.framework.uilayer.mvc;
 
 import net.avh4.framework.uilayer.scene.FontMetricsService;
 import net.avh4.framework.uilayer.scene.GraphicsOperations;
 import net.avh4.math.geometry.Rect;
 
 public interface Renderer<VM> {
    void draw(VM data, Rect bounds, GraphicsOperations g, FontMetricsService fm);
 }
