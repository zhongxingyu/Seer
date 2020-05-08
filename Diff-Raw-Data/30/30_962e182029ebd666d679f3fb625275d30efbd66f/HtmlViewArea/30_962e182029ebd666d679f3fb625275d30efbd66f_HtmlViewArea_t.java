 package org.romaframework.aspect.view.html.area;
 
 import java.util.List;
 
 import org.romaframework.aspect.view.area.AreaComponent;
 
public interface HtmlViewArea extends AreaComponent, HtmlViewRenderable {
 
 	public List<HtmlViewRenderable> getComponents();

	public boolean isDirty();

	public void setDirty(boolean dirty);

 }
