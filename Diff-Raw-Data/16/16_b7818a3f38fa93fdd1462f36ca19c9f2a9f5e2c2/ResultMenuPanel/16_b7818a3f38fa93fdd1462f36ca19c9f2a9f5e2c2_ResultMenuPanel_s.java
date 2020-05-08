 package sk.opendatanode.ui.results;
 
 import org.apache.wicket.markup.html.link.ExternalLink;
 import org.apache.wicket.markup.html.panel.Panel;
 
 public class ResultMenuPanel extends Panel {
 
     private static final long serialVersionUID = 1L;
     
     public ResultMenuPanel(String id) {
         super(id);
 
        add(new ExternalLink("backLink",
        		  "javascript:history.go(-1)", 
        		  getString("back")));
     }
     
 }
