 package edu.umd.cs.guitar.event;
 
 import java.util.Hashtable;
 import java.util.List;
 
 import org.openqa.selenium.WebElement;
 
 import edu.umd.cs.guitar.model.GComponent;
 import edu.umd.cs.guitar.model.WebComponent;
 
 public class WebToggleCheckbox implements GEvent {
 	@Override
 	public boolean isSupportedBy(GComponent gComponent) {
 		if(gComponent instanceof WebComponent) {
 			WebElement e = ((WebComponent) gComponent).getElement();	
 			if("input".equals(e.getTagName().toLowerCase()) && e.isEnabled()) {
 				if ("checkbox".equals(e.getAttribute("type").toLowerCase()) ||
 						"radio".equals(e.getAttribute("type").toLowerCase())) {
 					return true;
 				}
 			}
 		}
 		
 		return false;
 	}
 
 	@Override
	public void perform(GComponent gComponent, List<String> parameters,
			Hashtable<String, List<String>> optionalData)  {
 		if(gComponent instanceof WebComponent) {
 			WebComponent we = (WebComponent) gComponent;
 			we.getElement().toggle();
 		}
 	}
 
 	@Override
 	public void perform(GComponent gComponent,
 			Hashtable<String, List<String>> optionalData) {
 		perform(gComponent, null, optionalData);
 	}
 
 }
