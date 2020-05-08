 package org.genericsystem.myadmin.beans;
 
 import java.util.HashMap;
 import java.util.Map;

 import javax.enterprise.context.RequestScoped;
 import javax.enterprise.event.Observes;
 import javax.inject.Named;
 
 @Named
 @RequestScoped
 public class PanelBean {
 
 	private Map<String, String> selectedValueMap = new HashMap<>();
 
 	public String getSelectedValue(String key) {
 		String value = selectedValueMap.get(key);
 		return value == null ? "" : value;
 	}
 
 	public void change(@Observes PanelTitleChangeEvent changeEvent) {
 		selectedValueMap.put(changeEvent.getPanelId(), changeEvent.getTitle());
 	}
 
 	public static class PanelTitleChangeEvent {
 		private final String panelId;
 		private final String title;
 
 		public PanelTitleChangeEvent(String panelId, String title) {
 			this.panelId = panelId;
 			this.title = title;
 		}
 
 		public String getPanelId() {
 			return panelId;
 		}
 
 		public String getTitle() {
 			return title;
 		}
 
 	}
 
 }
