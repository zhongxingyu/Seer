 package com.eagerlogic.cubee.client.styles;
 
 import com.google.gwt.dom.client.Element;
 
 /**
  *
  * @author dipacs
  */
 public class LinearGradient extends ABackground {
 	
 	private final double angle;
 	private final ColorStop[] stops;
 
 	public LinearGradient(double angle, ColorStop... stops) {
 		this.angle = angle;
 		this.stops = stops;
 	}
 	
 	
 	
 //	background: #ff0000; /* Old browsers */
 //	background: -moz-linear-gradient(-45deg, #ff0000 0%, #00ff00 100%); /* FF3.6+ */
 //	background: -webkit-gradient(linear, left top, right bottom, color-stop(0%,#ff0000), color-stop(100%,#00ff00)); /* Chrome,Safari4+ */
 //	background: -webkit-linear-gradient(-45deg, #ff0000 0%,#00ff00 100%); /* Chrome10+,Safari5.1+ */
 //	background: -o-linear-gradient(-45deg, #ff0000 0%,#00ff00 100%); /* Opera 11.10+ */
 //	background: -ms-linear-gradient(-45deg, #ff0000 0%,#00ff00 100%); /* IE10+ */
 //	background: linear-gradient(135deg, #ff0000 0%,#00ff00 100%); /* W3C */
 //	filter: progid:DXImageTransform.Microsoft.gradient( startColorstr='#ff0000', endColorstr='#00ff00',GradientType=1 ); /* IE6-9 fallback on horizontal gradient */
 
 	@Override
 	public void apply(Element element) {
 		element.getStyle().setProperty("bkgrnd", "almafa");
 		applyMozilla(element);
 		applyWebkitOld(element);
 		applyWebkitNew(element);
 		applyOpera(element);
 		applyMs(element);
 		applyStandard(element);
 	}
 	
 	public void applyMozilla(Element element) {
 		String style = "-moz-linear-gradient(" + ((angle - 0.75) * 360) + "deg";
 //		String style = "-moz-linear-gradient(" + ((int)(startX * 100)) + "% " 
 //				+ ((int)(startY * 100)) + "%, " + ((int)(endX * 100)) + "% " 
 //				+ ((int)(endY * 100)) + "%";
 		for (ColorStop stop : stops) {
			style += ", " + stop.getColor().toCSS() + "," + ((int)stop.getPosition() * 100) + "%";
 		}
 		style += ")";
 		element.getStyle().setProperty("background", style);
 	}
 	
 	public void applyWebkitOld(Element element) {
 		String style = "-webkit-gradient(linear, " + ((angle - 0.75) * 360) + "deg";
 		// background: -webkit-gradient(linear, left top, right bottom, color-stop(0%,#ff0000), color-stop(100%,#00ff00)); /* Chrome,Safari4+ */
 //		String style = "-webkit-gradient(linear, " + ((int)(startX * 100)) + "% " 
 //				+ ((int)(startY * 100)) + "%, " + ((int)(endX * 100)) + "% " 
 //				+ ((int)(endY * 100)) + "%";
 		for (ColorStop stop : stops) {
			style += ", color-stop(" + ((int)stop.getPosition() * 100) + "%," + stop.getColor().toCSS() + ")";
 		}
 		style += ")";
 		element.getStyle().setProperty("background", style);
 	}
 	
 	public void applyWebkitNew(Element element) {
 		String style = "-webkit-linear-gradient(" + ((angle - 0.75) * 360) + "deg";
 //		String style = "-webkit-linear-gradient(" + ((int)(startX * 100)) + "% " 
 //				+ ((int)(startY * 100)) + "%, " + ((int)(endX * 100)) + "% " 
 //				+ ((int)(endY * 100)) + "%";
 		for (ColorStop stop : stops) {
			style += ", " + stop.getColor().toCSS() + "," + ((int)stop.getPosition() * 100) + "%";
 		}
 		style += ")";
 		element.getStyle().setProperty("background", style);
 	}
 	
 	public void applyOpera(Element element) {
 		String style = "-o-linear-gradient(" + ((angle - 0.75) * 360) + "deg";
 //		String style = "-o-linear-gradient(" + ((int)(startX * 100)) + "% " 
 //				+ ((int)(startY * 100)) + "%, " + ((int)(endX * 100)) + "% " 
 //				+ ((int)(endY * 100)) + "%";
 		for (ColorStop stop : stops) {
			style += ", " + stop.getColor().toCSS() + "," + ((int)stop.getPosition() * 100) + "%";
 		}
 		style += ")";
 		element.getStyle().setProperty("background", style);
 	}
 	
 	public void applyMs(Element element) {
 		String style = "-ms-linear-gradient(" + ((angle - 0.75) * 360) + "deg";
 //		String style = "-ms-linear-gradient(" + ((int)(startX * 100)) + "% " 
 //				+ ((int)(startY * 100)) + "%, " + ((int)(endX * 100)) + "% " 
 //				+ ((int)(endY * 100)) + "%";
 		for (ColorStop stop : stops) {
			style += ", " + stop.getColor().toCSS() + "," + ((int)stop.getPosition() * 100) + "%";
 		}
 		style += ")";
 		element.getStyle().setProperty("background", style);
 	}
 	
 	public void applyStandard(Element element) {
 		String style = "linear-gradient(" + ((angle - 0.5) * 360) + "deg";
 //		String style = "linear-gradient(" + ((int)(startX * 100)) + "% " 
 //				+ ((int)(startY * 100)) + "%, " + ((int)(endX * 100)) + "% " 
 //				+ ((int)(endY * 100)) + "%";
 		for (ColorStop stop : stops) {
			style += ", " + stop.getColor().toCSS() + " " + ((int)stop.getPosition() * 100) + "%";
 		}
 		style += ")";
 		element.getStyle().setProperty("background", style);
 	}
 	
 	public void applyFilter(Element element) {
 		// TODO implement
 	}
 
 }
