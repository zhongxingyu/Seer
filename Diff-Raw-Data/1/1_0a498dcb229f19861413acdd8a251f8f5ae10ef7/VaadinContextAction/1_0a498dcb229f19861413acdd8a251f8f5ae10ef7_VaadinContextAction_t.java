 package pt.ist.bennu.vaadin.actions;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import myorg.presentationTier.Context;
 import myorg.presentationTier.LayoutContext;
 import myorg.presentationTier.actions.ContextBaseAction;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 import pt.ist.fenixWebFramework.struts.annotations.Mapping;
 
 @Mapping(path = "/vaadinContext")
 public class VaadinContextAction extends ContextBaseAction {
 
     public static class VaadinLayoutContext extends LayoutContext {
 	public VaadinLayoutContext(String path) {
 	    super(path);
	    addHead("/layout/vaadinHead.jsp");
 	}
     }
 
     public static class FullVaadinLayoutContext extends Context {
 
 	@Override
 	public ActionForward forward(final String body) {
 	    return new ActionForward("/embedded/vaadin-embedded-full.jsp");
 	}
 
 	public FullVaadinLayoutContext() {
 	    super(null);
 	}
     }
 
     public final ActionForward forwardToVaadin(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
 	return forwardToVaadin(request, true);
     }
 
     public final ActionForward forwardToFullVaadin(final ActionMapping mapping, final ActionForm form,
 	    final HttpServletRequest request, final HttpServletResponse response) throws Exception {
 	return forwardToVaadin(request, false);
     }
 
     public static ActionForward forwardToVaadin(final HttpServletRequest request, final boolean useBennuLayout) {
 	final Context context = useBennuLayout ? new VaadinLayoutContext(getContext(request).getPath())
 		: new FullVaadinLayoutContext();
 	setContext(request, context);
 	return forward(request, "/embedded/vaadin-embedded.jsp");
     }
 }
