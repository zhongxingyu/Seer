 package pt.ist.expenditureTrackingSystem.presentationTier.actions;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import myorg.presentationTier.Context;
 import myorg.presentationTier.LayoutContext;
 import myorg.presentationTier.actions.ContextBaseAction;
 
 import org.apache.commons.beanutils.BeanComparator;
 import org.apache.commons.collections.Predicate;
 import org.apache.commons.collections.comparators.ReverseComparator;
 import org.apache.commons.lang.StringUtils;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 import pt.ist.expenditureTrackingSystem.domain.announcements.Announcement;
 import pt.ist.expenditureTrackingSystem.domain.announcements.RCISTAnnouncement;
 import pt.ist.fenixWebFramework.struts.annotations.Mapping;
 import pt.utl.ist.fenix.tools.util.CollectionPager;
 
 @Mapping(path = "/viewRCISTAnnouncements")
 public class ViewRCISTAnnouncements extends ContextBaseAction {
 
     private static final int REQUESTS_PER_PAGE = 10;
    private static final String PUBLIC_LAYOUT = "rcistAnnouncements";
 
     public final ActionForward viewRCIST(final ActionMapping mapping, final ActionForm form, final HttpServletRequest request,
 	    final HttpServletResponse response) throws Exception {
 
 	ArrayList<RCISTAnnouncement> approvedList = new ArrayList<RCISTAnnouncement>();
 	approvedList.addAll(Announcement.getAnnouncements(RCISTAnnouncement.class, new Predicate() {
 
 	    @Override
 	    public boolean evaluate(Object arg0) {
 		RCISTAnnouncement announcement = (RCISTAnnouncement) arg0;
 		return announcement.getActive();
 	    }
 
 	}));
 
 	Collections.sort(approvedList, new ReverseComparator(new BeanComparator("creationDate")));
 
 	final CollectionPager<Announcement> pager = new CollectionPager<Announcement>((Collection) approvedList,
 		REQUESTS_PER_PAGE);
 
 	request.setAttribute("collectionPager", pager);
 	request.setAttribute("numberOfPages", Integer.valueOf(pager.getNumberOfPages()));
 
 	final String pageParameter = request.getParameter("pageNumber");
 	final Integer page = StringUtils.isEmpty(pageParameter) ? Integer.valueOf(1) : Integer.valueOf(pageParameter);
 	request.setAttribute("pageNumber", page);
 	request.setAttribute("announcements", pager.getPage(page));
 
 	return forward(request, "/public/viewRCISTAnnouncements.jsp");
     }
 
     @Override
     public Context createContext(final String contextPathString, HttpServletRequest request) {
 	LayoutContext layout = new LayoutContext(contextPathString);
 	layout.setLayout(PUBLIC_LAYOUT);
 	return layout;
     }
 }
