 package sk.opendatanode.ui.results;
 
 import org.apache.wicket.markup.html.link.ExternalLink;
 import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.cycle.RequestCycle;
 
 public class ResultMenuPanel extends Panel {
 
     private static final long serialVersionUID = 1L;
     
     public ResultMenuPanel(String id) {
         super(id);
 
        ServletWebRequest request = (ServletWebRequest) RequestCycle.get().getRequest();
        String referrer = request.getHeader("referer");
        
        if(referrer != null && referrer.startsWith(request.getUrl().getProtocol() + "://" + request.getUrl().getHost())) {
	        add(new ExternalLink("backLink",
	        		  "javascript:history.go(-1)", 
	        		  getString("back")));
        } else {
        	add(new ExternalLink("backLink", "../", getString("back")));
        }
     }
     
 }
