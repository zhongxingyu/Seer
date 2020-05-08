 package concretemanor.tools.teamview.actions;
 
 import net.sourceforge.stripes.action.ActionBean;
 import net.sourceforge.stripes.action.ForwardResolution;
 import net.sourceforge.stripes.action.Resolution;
 import net.sourceforge.stripes.action.UrlBinding;
 
@UrlBinding("/list.action")
 public class FullListActionBean extends ListActionBean implements ActionBean {
     public Resolution refresh() {
 	return new ForwardResolution("/main.jsp");
     }
 }
