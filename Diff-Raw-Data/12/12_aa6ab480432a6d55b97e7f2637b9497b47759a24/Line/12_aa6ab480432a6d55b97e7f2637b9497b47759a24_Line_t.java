 package wu.geostory;
 
 import com.google.gwt.dom.client.Element;
 import com.google.gwt.dom.client.Style;
 import com.google.gwt.user.client.DOM;
 import com.google.gwt.user.client.ui.Widget;
 
 public class Line extends Widget{
 
 	public Line(){
		Element e = DOM.createElement("div");
 		Style s = e.getStyle();
 		s.setProperty("borderColor","#666");
 		s.setProperty("borderStyle","solid");
 		s.setProperty("borderWidth","0 0 0 1px");
 		s.setProperty("height","100%");
 		s.setProperty("top","0");
		s.setProperty("width","1px");
 		setElement(e);
 	}
 	
 }
