 package org.ilrt.wf.facets.web.spring.controllers;
 
 import com.hp.hpl.jena.rdf.model.Property;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.ResIterator;
 import com.hp.hpl.jena.rdf.model.Resource;
 import com.hp.hpl.jena.rdf.model.ResourceFactory;
 import com.hp.hpl.jena.rdf.model.StmtIterator;
 import com.hp.hpl.jena.rdf.model.impl.ResourceImpl;
 import com.hp.hpl.jena.util.FileManager;
 import com.hp.hpl.jena.vocabulary.RDF;
 import freemarker.template.ObjectWrapper;
 import freemarker.template.SimpleCollection;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.apache.log4j.Logger;
 import org.ilrt.wf.facets.FacetView;
 import org.ilrt.wf.facets.FacetViewService;
 import org.ilrt.wf.facets.FacetViewServiceException;
 import org.ilrt.wf.facets.freemarker.FacetViewFreeMarkerWrapper;
 import org.ilrt.wf.facets.freemarker.ResourceHashModel;
 import org.ilrt.wf.facets.session.FacetViewSessionWrapper;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 import org.ilrt.wf.facets.FacetQueryService;
 import org.ilrt.wf.facets.freemarker.ExtendedSimpleCollection;
 import org.ilrt.wf.facets.freemarker.JenaObjectWrapper;
 
 /**
  * @author Mike Jones (mike.a.jones@bristol.ac.uk)
  */
 @Controller
 public class ResRevController extends AbstractController {
 
     @Autowired
     public ResRevController(final FacetViewService facetViewService, final FacetQueryService facetQueryService) {
         this.facetViewService = facetViewService;
         this.facetQueryService = facetQueryService;
     }
 
     // ---------- public methods that are mapped to URLs
     
     @RequestMapping(value = ITEM_PATH, method = RequestMethod.GET)
     public ModelAndView resourceView(HttpServletRequest request) throws FacetViewServiceException {
 
         // get the session object
         HttpSession session = request.getSession(true);
 
         String resource = request.getParameter(RESOURCE_PARAMETER);
 
         ModelAndView mav = displayResource(session, request, resource);
         
         mav.addObject("viewcontext", "resource");
         
         if (mav.getViewName().equalsIgnoreCase(PROFILE_VIEW_NAME))
         {
             Resource resourceObj = ((ResourceHashModel)(mav.getModel().get("resource"))).getResource();
             mav.addObject("publist", getListFromQuery("/queries/getPersonsPublications.rq", resourceObj));
         }
 
         if (mav.getViewName().equalsIgnoreCase(ORGANISATION_VIEW_NAME))
         {
             Resource resourceObj = ((ResourceHashModel)(mav.getModel().get("resource"))).getResource();
             mav.addObject("recentoutputs", getListFromQuery("/queries/getAllDeptOutputs.rq", resourceObj));
             mav.addObject("recentgrants", getListFromQuery("/queries/getAllDeptGrants.rq", resourceObj));
         }
         return mav;
     }
 
     @RequestMapping(value = VIEW_PATH, method = RequestMethod.GET)
     public ModelAndView mainView(HttpServletRequest request) throws FacetViewServiceException {
 
         // get the session object
         HttpSession session = request.getSession(true);
 
         // do a fresh query the service
         ModelAndView mav = createModelAndView(MAIN_VIEW_NAME, request);
         FacetView facetView = facetViewService.generate(request);
         FacetViewSessionWrapper wrapper = new FacetViewSessionWrapper(facetView);
         wrapper.setView(facetViewService.getViewType(request));
         session.setAttribute(FACETVIEW_SESSION, wrapper);
 
         // provide view context so that we can determine if resource is being viewed within a view
         mav.addObject(VIEW_KEY, wrapper.getView());
         mav.addObject(FACET_VIEW_KEY, new FacetViewFreeMarkerWrapper(facetView));
         mav.addObject("viewcontext", wrapper.getView());
         
         return mav;
     }
 
     @RequestMapping(value = HOME_PATH, method = RequestMethod.GET)
     public ModelAndView homeView(HttpServletRequest request) throws FacetViewServiceException {
 
         ModelAndView mav = createModelAndView(HOME_VIEW_NAME, request);
         mav.addObject(FACET_VIEW_KEY,this.facetViewService.listViews());
         mav.addObject("viewcontext", "home");
         
         int deptCount = facetQueryService.performSelect(getQuery("/queries/homeDepartmentCount.rq"), false).get(0).get("count").asLiteral().getInt();
         int peopleCount = facetQueryService.performSelect(getQuery("/queries/homePeopleCount.rq"), false).get(0).get("count").asLiteral().getInt();
         List<Map<String, RDFNode>> outputSummary = this.facetQueryService.performSelect(getQuery("/queries/homeOutputSummaries.rq"), false);
         List<Map<String, RDFNode>> grantSummary = this.facetQueryService.performSelect(getQuery("/queries/homeGrantsSummaries.rq"), false);
         
         mav.addObject("deptCount", deptCount);
         mav.addObject("peopleCount", peopleCount);
         
         int outputTotal = 0;
         for (Map<String, RDFNode> outputRow: outputSummary) {
             outputTotal += outputRow.get("scount").asLiteral().getInt();
         }
         
         int grantTotal = 0;
         for (Map<String, RDFNode> outputRow: grantSummary) {
             grantTotal += outputRow.get("gcount").asLiteral().getInt();
         }
         
         mav.addObject("grantSummary", new SimpleCollection(grantSummary, OBJECT_WRAPPER));
         mav.addObject("outputSummary", new SimpleCollection(outputSummary, OBJECT_WRAPPER));
         mav.addObject("outputTotal", outputTotal);
         mav.addObject("grantTotal", grantTotal);
         
         return mav;
     }
 
     @RequestMapping(value = ABOUT_PATH, method = RequestMethod.GET)
     public ModelAndView aboutView(HttpServletRequest request) throws FacetViewServiceException {
 
         ModelAndView mav = createModelAndView(ABOUT_VIEW_NAME, request);
         mav.addObject("viewcontext", "about");
         return mav;
     }
 
     @RequestMapping(value = CONTACT_PATH, method = RequestMethod.GET)
     public ModelAndView contactView(HttpServletRequest request) throws FacetViewServiceException {
 
         ModelAndView mav = createModelAndView(CONTACT_VIEW_NAME, request);
         mav.addObject("viewcontext", "contact");
         return mav;
     }
 
     @RequestMapping(value = RESEARCH_PATH, method = RequestMethod.GET)
     public ModelAndView researchView(HttpServletRequest request) throws FacetViewServiceException {
         String username = request.getRemoteUser();
 
         // get the session object
         HttpSession session = request.getSession(true);
 
         // do a fresh query the service
         ModelAndView mav = createModelAndView(RESEARCH_VIEW, request);
 
         Resource resource = facetQueryService.getInformationAboutIndirect(userNameProp, ResourceFactory.createPlainLiteral(username));
 
         mav.addObject("resource",  new ResourceHashModel(resource));
         
         mav.addObject("viewcontext", "research");
         
         return mav;
     }
 
     @RequestMapping(value = PROFILE_PATH, method = RequestMethod.GET)
     public ModelAndView profileView(HttpServletRequest request, HttpServletResponse response) throws FacetViewServiceException {
         String username = request.getRemoteUser();
         log.debug("Displaying results for " +  username);
         
         if (username == null || username.isEmpty())
         {
             return new ModelAndView("redirect:/");
         }
         
         // do a fresh query the service
         ModelAndView mav = createModelAndView(PROFILE_VIEW_NAME, request);
 
         Resource resource = facetQueryService.getInformationAboutIndirect(userNameProp, ResourceFactory.createPlainLiteral(username));
 
         mav.addObject("resource",  new ResourceHashModel(resource));
 
         // get users publication information
         mav.addObject("publist", getListFromQuery("/queries/getPersonsPublications.rq", resource));
         
         // add flag to allow proview view to differentiate between displaying regular users and current user's profile view
         mav.addObject("profileview",  "true");
         
         mav.addObject("viewcontext", "profile");
         
         return mav;
     }
     
     @RequestMapping(value = DEPARTMENT_PATH, method = RequestMethod.GET)
     public ModelAndView departmentView(HttpServletRequest request) throws FacetViewServiceException {
         String username = request.getRemoteUser();
         
         if (username == null || username.isEmpty())
         {
             return new ModelAndView("redirect:/");
         }
         
         // get the session object
         HttpSession session = request.getSession(true);
 
         // do a fresh query the service
         ModelAndView mav = createModelAndView(ORGANISATION_VIEW_NAME, request);
 
         Resource user = facetQueryService.getInformationAboutIndirect(userNameProp, ResourceFactory.createPlainLiteral(username));
         // TODO: some people are associated with more than one department
         ResIterator it = user.getModel().listResourcesWithProperty(memberProp, user);
         Resource dept = it.next();
         if (it.hasNext()) log.warn("User: " + username + " has more than one department");
         
         Resource department = facetQueryService.getInformationAbout(dept);
         
         mav.addObject("user",  new ResourceHashModel(user));
         mav.addObject("resource", new ResourceHashModel(department));
         mav.addObject("recentoutputs", getListFromQuery("/queries/getAllDeptOutputs.rq", dept));
         mav.addObject("recentgrants", getListFromQuery("/queries/getAllDeptGrants.rq", dept));
         
         mav.addObject("viewcontext", "department");
         
         return mav;
     }
 
     @RequestMapping(value = DEPT_LIST_PATH, method = RequestMethod.GET)
     public ModelAndView departmentAndGroupsView(HttpServletRequest request) throws FacetViewServiceException {
 
         // get the session object
         HttpSession session = request.getSession(true);
 
         // do a fresh query the service
         ModelAndView mav = createModelAndView(DEPT_LIST_VIEW_NAME, request);
         
         List<Map<String, RDFNode>> departmentSummary =  facetQueryService.performSelect(getQuery("/queries/getAllDepartments.rq"), false);
         mav.addObject("departmentList", new SimpleCollection(departmentSummary, OBJECT_WRAPPER));
        mav.addObject("viewcontext", "department");
         
         return mav;
     }
         
     // ---------- private methods
     private ModelAndView displayResourceOrFail(FacetViewSessionWrapper wrapper, String uri,
                                                HttpServletRequest request) {
 
         Resource resource = wrapper.get(uri);
 
         if (resource == null) {
             throw new NotFoundException("Unable to find the requested resource");
         } else {
             ModelAndView mav = createModelAndView(resolveViewForResource(resource), request);
             mav.addObject("resource", new ResourceHashModel(resource));
             return mav;
         }
     }
 
     private ModelAndView displayResourceOrFail(String uri, HttpServletRequest request) {
 
         Resource r = new ResourceImpl(uri);
         Resource resource = facetQueryService.getInformationAbout(r);
         if (resource == null) {
             throw new NotFoundException("Unable to find the requested resource");
         } else {
             ModelAndView mav = createModelAndView(resolveViewForResource(resource), request);
             mav.addObject("resource", new ResourceHashModel(resource));
             return mav;
         }
     }
 
 
 
     private ModelAndView displayResource(HttpSession session, HttpServletRequest request, String uri)
             throws FacetViewServiceException {
 
         log.debug("Requesting to view resource: " + uri);
 
         FacetViewSessionWrapper wrapper =
                 (FacetViewSessionWrapper) session.getAttribute(FACETVIEW_SESSION);
 
         if (wrapper != null) {
 
             log.debug("We have a FacetViewSessionWrapper instance on the session");
 
             Resource resource = wrapper.get(uri);
 
             if (resource != null) {
 
                 log.debug("Found resource");
 
                 ModelAndView mav = createModelAndView(resolveViewForResource(resource), request);
                 mav.addObject("resource", new ResourceHashModel(resource));
                 mav.addObject(VIEW_KEY, wrapper.getView());
                 return mav;
             } else {
 
                 log.debug("Cannot find the resource on the current session. Refreshing");
                 return displayResourceOrFail(uri, request);
             }
 
         } else {
 
             log.debug("We do not have a FacetViewSessionWrapper instance on the session. Refreshing");
             return displayResourceOrFail(uri, request);
         }
     }
 
     private String resolveViewForResource(Resource resource) {
 
         if (resource.hasProperty(RDF.type)) {
 
             StmtIterator iter = resource.listProperties(RDF.type);
             while (iter.hasNext())
             {
                 String type = iter.nextStatement().getResource().getURI();
 
                 if (type.equals("http://vocab.ouls.ox.ac.uk/projectfunding#Grant")) {
                     return GRANT_VIEW_NAME;
                 }
                 else if (type.equals("http://purl.org/vocab/aiiso/schema#Institution")) {
                     return ORGANISATION_VIEW_NAME;
                 }
                 else if (type.equals("http://xmlns.com/foaf/0.1/Person")) {
                     return PROFILE_VIEW_NAME;
                 }
                 else if(type.equals("http://purl.org/dc/terms/Publication")) {
                     return PUBLICATION_VIEW_NAME;
                 }
             }
 
         }
 
         return DEFAULT_VIEW;
     }
     
     private String getQuery(String queryFile) {
         String query = queryFileCache.get(queryFile);
         if (query == null) {
             query = FileManager.get().readWholeFileAsUTF8(queryFile);
             queryFileCache.put(queryFile, query);
         }
         return query;
     }
     
     private Object getListFromQuery(String queryFile, Resource resource) {
         String query = getQuery(queryFile);
         
         String completeQuery = String.format(query, resource.getURI());
         List<Map<String, RDFNode>> result = 
                 facetQueryService.performSelect(completeQuery, true);
                 
         // Reduce this down to a list<resource>
         List<Resource> hits = new ArrayList<Resource>(result.size());
         for (Map<String, RDFNode> soln: result) {
             hits.add((Resource) soln.get("s"));
         }
 
         // Return as a viewable thingy 
         return new ExtendedSimpleCollection(hits, OBJECT_WRAPPER);
     }
 
     private static ObjectWrapper OBJECT_WRAPPER = new JenaObjectWrapper();
     private static Map<String, String> queryFileCache = 
             new HashMap<String, String>();
     
     private FacetViewService facetViewService;
     private FacetQueryService facetQueryService;
 
     private final String FOAF = "http://xmlns.com/foaf/0.1/";
     private final Property userNameProp = ResourceFactory.createProperty(FOAF, "nick");
     private final Property memberProp = ResourceFactory.createProperty(FOAF, "member");
 
     public static String HOME_VIEW_NAME = "homeView";
     public static String MAIN_VIEW_NAME = "mainView";
     public static String ABOUT_VIEW_NAME = "aboutView";
     public static String CONTACT_VIEW_NAME = "contactView";
     public static String PROFILE_VIEW_NAME = "profileView";
     public static String GRANT_VIEW_NAME = "grantView";
     public static String ORGANISATION_VIEW_NAME = "orgView";
     public static String PUBLICATION_VIEW_NAME = "pubView";
     public static String DEFAULT_VIEW = "defaultView";
     public static String RESEARCH_VIEW = "researchView";
     public static String DEPT_LIST_VIEW_NAME = "deptListView";
 
     private final String FACETVIEW_SESSION = "facetView";
     private final String VIEW_KEY = "view";
     private final String RESOURCE_PARAMETER = "res";
 
     private final String HOME_PATH = "/";
     private final String VIEW_PATH = "/*";
     private final String ITEM_PATH = "/item";
     private final String ABOUT_PATH = "/about/";
     private final String CONTACT_PATH = "/contact/";
     private final String PROFILE_PATH = "/profile";
     private final String RESEARCH_PATH = "/research";
     private final String DEPARTMENT_PATH = "/mydepartment";
     private final String DEPT_LIST_PATH = "/organisations";
 
     private Logger log = Logger.getLogger(ResRevController.class);
 }
