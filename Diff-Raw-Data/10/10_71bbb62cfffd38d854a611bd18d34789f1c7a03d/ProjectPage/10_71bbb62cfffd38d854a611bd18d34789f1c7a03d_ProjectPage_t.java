 package greenhouse.ui.wicket.page;
 
 import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.wicketstuff.annotation.mount.MountPath;
 
@MountPath("/projects/${project}")
 public class ProjectPage extends BaseProjectPage {
 
     public ProjectPage(PageParameters params) {
         super(params);
     }
 }
