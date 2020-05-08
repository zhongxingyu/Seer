 package com.bluesky.visualprogramming.core;
 
 import java.awt.Point;
 import java.awt.Rectangle;
 
 import org.apache.batik.dom.svg.SVGOMGElement;
 import org.apache.commons.lang.StringEscapeUtils;
 import org.apache.commons.lang.StringUtils;
 
 import com.bluesky.visualprogramming.core.value.StringValue;
 import com.bluesky.visualprogramming.ui.diagram.SVGDiagramPanel;
 import com.bluesky.visualprogramming.ui.svg.SvgScene;
 
 public class Field {
 
 	public String name;
 	public _Object target;
 
 	// public boolean owner;
 
 	private SelectedStatus selectedStatus = SelectedStatus.NotSelected;
 
 	/**
 	 * the max value of height and width is 1000;
 	 */
 	private Rectangle area = new Rectangle();
 
 	public Field(String name) {
 		this.name = name;
 		area = new Rectangle(0, 0, 100, 100);
 	}
 
 	public Field(_Object targetObject, String name) {
 		this.target = targetObject;
 		this.name = name;
 		// this.owner = owner;
 
 		area = new Rectangle(0, 0, 100, 100);
 	}
 
 	@Override
 	public String toString() {
 
 		return name;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public _Object getTarget() {
 		return target;
 	}
 
 	public void setTarget(_Object target) {
 		this.target = target;
 	}
 
 	public SelectedStatus getSelectedStatus() {
 		return selectedStatus;
 	}
 
 	public void setSelectedStatus(SelectedStatus selectedStatus) {
 		this.selectedStatus = selectedStatus;
 	}
 
 	public Rectangle getArea(double zoomRate) {
 		Rectangle r = new Rectangle((int) (area.x * zoomRate),
 				(int) (area.y * zoomRate), (int) (area.width * zoomRate),
 				(int) (area.height * zoomRate));
 		return r;
 	}
 
 	public Rectangle getArea() {
 		// if (area == null)
 		// this.area = (Rectangle)target.area.clone();
 
 		// if (area == null)
 		// this.area = new Rectangle(0,0,100,100);
 
 		return area;
 	}
 
 	public void setStartPosition(float x, float y) {
 		area.x = (int) x;
 		area.y = (int) y;
 
 	}
 
 	public boolean isSystemField() {
 		return name != null && name.startsWith("_");
 	}
 
 	/**
 	 * draw itself as an icon. (if it is big enough, then draw internal.)
 	 * 
 	 * @param diagramPanel
 	 * @param scene
 	 * @param canvasOffset
 	 * @param zoom
 	 * @param own
 	 */
 	public void draw(SVGDiagramPanel diagramPanel, SvgScene scene,
 			Point canvasOffset, double zoom, boolean own) {
 
 		// System.out.println("draw:"+getName());
 
 		// draw border
 		// g.setColor(borderColor);
 
 		int x = (int) (area.x * zoom) + canvasOffset.x;
 		int y = (int) (area.y * zoom) + canvasOffset.y;
 		int width = (int) (area.width * zoom);
 		int height = (int) (area.height * zoom);
 
 		long id = target.getId();
 		String value = target.getHumanReadableText();
 
 		SVGOMGElement ele = null;
 		if (target != null) {
 			_Object _graphic = target.getChild("_graphic");
 			if (_graphic != null && _graphic instanceof StringValue) {
 				StringValue sv = (StringValue) _graphic;
 				ele = scene.addObject(sv.getValue(), id, x, y, 0.2f);
 
 			} else
 				ele = scene.addObject(target.type, id, x, y, 0.2f);
 		}
 
 		scene.setName(id, name);
 
		String value2 = StringEscapeUtils.escapeXml(value);
 		int maxLength = 20;
		int length = value2.length() > maxLength ? maxLength : value2.length();
		value2 = value2.substring(0, length);
		scene.setDescription(id, value2);
 
 		scene.setBorderColor(id, target.borderColor);
 
 		int borderWidth = target.borderWidth;
 
 		ele.setUserData("field", this, null);
 		diagramPanel.addMouseListener(ele);
 
 		int finalBorderWidth = -1;
 		if (own)
 			finalBorderWidth = borderWidth;
 		else
 			finalBorderWidth = borderWidth / 2;
 
 	}
 }
