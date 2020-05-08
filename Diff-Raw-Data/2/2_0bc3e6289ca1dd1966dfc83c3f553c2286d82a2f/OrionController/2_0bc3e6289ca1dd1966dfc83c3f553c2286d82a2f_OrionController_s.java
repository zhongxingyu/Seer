 package polly.rx.httpv2;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeSet;
 
 import polly.rx.MSG;
 import polly.rx.core.AZEntryManager;
 import polly.rx.core.orion.Orion;
 import polly.rx.core.orion.PathPlanner;
 import polly.rx.core.orion.PathPlanner.RouteOptions;
 import polly.rx.core.orion.PathPlanner.UniversePath;
 import polly.rx.core.orion.QuadrantProvider;
 import polly.rx.core.orion.QuadrantProviderDecorator;
 import polly.rx.core.orion.WormholeProvider;
 import polly.rx.core.orion.WormholeProviderDecorator;
 import polly.rx.core.orion.model.Quadrant;
 import polly.rx.core.orion.model.QuadrantDecorator;
 import polly.rx.core.orion.model.Sector;
 import polly.rx.core.orion.model.SectorDecorator;
 import polly.rx.core.orion.model.SectorType;
 import polly.rx.core.orion.model.Wormhole;
 import polly.rx.core.orion.model.WormholeDecorator;
 import polly.rx.parsing.ParseException;
 import polly.rx.parsing.QuadrantCnPParser;
 import de.skuzzle.polly.http.annotations.Get;
 import de.skuzzle.polly.http.annotations.OnRegister;
 import de.skuzzle.polly.http.annotations.Param;
 import de.skuzzle.polly.http.annotations.Post;
 import de.skuzzle.polly.http.api.AlternativeAnswerException;
 import de.skuzzle.polly.http.api.Controller;
 import de.skuzzle.polly.http.api.HttpSession;
 import de.skuzzle.polly.http.api.answers.HttpAnswer;
 import de.skuzzle.polly.http.api.answers.HttpAnswers;
 import de.skuzzle.polly.sdk.MyPolly;
 import de.skuzzle.polly.sdk.Types;
 import de.skuzzle.polly.sdk.Types.TimespanType;
 import de.skuzzle.polly.sdk.httpv2.GsonHttpAnswer;
 import de.skuzzle.polly.sdk.httpv2.PollyController;
 import de.skuzzle.polly.sdk.httpv2.SuccessResult;
 import de.skuzzle.polly.sdk.httpv2.WebinterfaceManager;
 import de.skuzzle.polly.sdk.httpv2.html.HTMLTools;
 
 
 public class OrionController extends PollyController {
     
     public final static String VIEW_ORION_PREMISSION = "polly.permission.VIEW_ORION"; //$NON-NLS-1$
     public final static String WRITE_ORION_PREMISSION = "polly.permission.WRITE_ORION"; //$NON-NLS-1$
     public final static String ROUTE_ORION_PREMISSION = "polly.permission.ROUTE_ORION"; //$NON-NLS-1$
     
     
     public final static String PAGE_ORION = "/pages/orion"; //$NON-NLS-1$
     public final static String PAGE_QUAD_LAYOUT = "/pages/orion/quadlayout"; //$NON-NLS-1$
     
     public final static String API_GET_QUADRANT = "/api/orion/quadrant"; //$NON-NLS-1$
     public final static String API_GET_SECTOR_INFO = "/api/orion/sector"; //$NON-NLS-1$
     public final static String API_SET_ROUTE_TO = "/api/orion/routeTo"; //$NON-NLS-1$
     public final static String API_SET_ROUTE_FROM = "/api/orion/routeFrom"; //$NON-NLS-1$
     public final static String API_GET_ROUTE = "/api/orion/getRoute"; //$NON-NLS-1$
     private final static String API_POST_LAYOUT = "/api/orion/postLayout"; //$NON-NLS-1$
     
     private final static String CONTENT_QUAD_LAYOUT = "/polly/rx/httpv2/view/orion/quadlayout.html"; //$NON-NLS-1$
     private final static String CONTENT_QUADRANT = "/polly/rx/httpv2/view/orion/quadrant.html"; //$NON-NLS-1$
     private final static String CONTENT_SECTOR_INFO = "/polly/rx/httpv2/view/orion/sec_info.html"; //$NON-NLS-1$
     private final static String CONTENT_ORION = "/polly/rx/httpv2/view/orion/orion.html"; //$NON-NLS-1$
     private final static String CONTENT_ROUTE = "/polly/rx/httpv2/view/orion/route.html"; //$NON-NLS-1$
     
     private final static String REVORIX_CATEGORY_KEY = "httpRxCategory"; //$NON-NLS-1$
     private final static String ORION_NAME_KEY = "htmlOrionName"; //$NON-NLS-1$
     private final static String ORION_DESC_KEY = "htmlOrionDesc"; //$NON-NLS-1$
     
     private final static String ROUTE_FROM_KEY = "routeFrom"; //$NON-NLS-1$
     private final static String ROUTE_TO_KEY = "routeTo"; //$NON-NLS-1$
     
     
     
     public final static class DisplaySector extends SectorDecorator {
 
         private DisplaySector(Sector wrapped) {
             super(wrapped);
         }
         
         public String getQuadId() {
             return this.getQuadName().replace(" ", "_"); //$NON-NLS-1$ //$NON-NLS-2$
         }
     }
     
 
     
     public final static class DisplayQuadrant extends QuadrantDecorator {
 
         private DisplayQuadrant(Quadrant wrapped) {
             super(wrapped);
         }
         
         
         public String getQuadId() {
             return this.getName().replace(" ", "_"); //$NON-NLS-1$ //$NON-NLS-2$
         }
         
         @Override
         public Sector getSector(int x, int y) {
             return new DisplaySector(super.getSector(x, y));
         }
     }
     
     
     
     public final static class DisplayWormholeProvider extends WormholeProviderDecorator {
 
         public DisplayWormholeProvider(WormholeProvider wrapped) {
             super(wrapped);
         }
         
         private List<Wormhole> decorate(List<Wormhole> holes) {
             final List<Wormhole> result = new ArrayList<>(holes.size());
             for (final Wormhole hole : holes) {
                 result.add(new WormholeDecorator(hole) { 
                     @Override
                     public Sector getSource() {
                         return new DisplaySector(super.getSource());
                     }
                     
                     @Override
                     public Sector getTarget() {
                         return new DisplaySector(super.getTarget());
                     }
                 });
             }
             return result;
         }
         
         @Override
         public List<Wormhole> getWormholes(Quadrant quadrant, QuadrantProvider quads) {
             return this.decorate(super.getWormholes(quadrant, quads));
         }
 
         @Override
         public List<Wormhole> getWormholes(Sector sector, QuadrantProvider quads) {
             return this.decorate(super.getWormholes(sector, quads));
         }
     }
     
     
     
     public final static class DisplayQuadrantProvider extends QuadrantProviderDecorator {
 
         public DisplayQuadrantProvider(QuadrantProvider wrapped) {
             super(wrapped);
         }
         
         private Collection<Quadrant> decorate(Collection<Quadrant> quadrants) {
             final List<Quadrant> result = new ArrayList<>();
             for (final Quadrant quadrant : quadrants) {
                 result.add(new DisplayQuadrant(quadrant));
             }
             return result;
         }
         
         @Override
         public Collection<Quadrant> getAllQuadrants() {
             return this.decorate(super.getAllQuadrants());
         }
         
         @Override
         public Quadrant getQuadrant(Sector sector) {
             return new DisplayQuadrant(super.getQuadrant(sector));
         }
         
         @Override
         public Quadrant getQuadrant(String name) {
             name = name.replace('_', ' ');
             return new DisplayQuadrant(super.getQuadrant(name));
         }
     }
     
     
     
     private final QuadrantProvider quadProvider;
     private final WormholeProvider holeProvider;
     private final PathPlanner pathPlanner;
     private final AZEntryManager azManager;
     
     
     public OrionController(MyPolly myPolly, AZEntryManager azManager) {
         this(myPolly, 
                 new DisplayQuadrantProvider(Orion.INSTANCE.createQuadrantProvider()),
                 new DisplayWormholeProvider(Orion.INSTANCE.createWormholeProvider()),
                 Orion.INSTANCE.getPathPlanner(),
                 azManager);
     }
     
     
     
     /**
      * Copy Constructor for {@link #createInstance()}
      * @param myPolly
      * @param quadProvider
      * @param holeProvider
      * @param planner
      * @param azManager
      */
     private OrionController(MyPolly myPolly, QuadrantProvider quadProvider, 
             WormholeProvider holeProvider, PathPlanner planner, AZEntryManager azManager) {
         super(myPolly);
         this.quadProvider = quadProvider;
         this.holeProvider = holeProvider;
         this.pathPlanner = planner;
         this.azManager = azManager;
     }
     
     
 
     @Override
     protected Controller createInstance() {
         return new OrionController(this.getMyPolly(), this.quadProvider, 
                 this.holeProvider, this.pathPlanner, this.azManager);
     }
     
     
     
     @Override
     protected Map<String, Object> createContext(String content) {
         final Map<String, Object> c = super.createContext(content);
         HTMLTools.gainFieldAccess(c, MSG.class, "MSG"); //$NON-NLS-1$
         return c;
     }
     
     
     
     @Get(value = PAGE_ORION, name = ORION_NAME_KEY)
     @OnRegister({ 
         WebinterfaceManager.ADD_MENU_ENTRY, 
         MSG.FAMILY,
         REVORIX_CATEGORY_KEY, 
         ORION_DESC_KEY,
         VIEW_ORION_PREMISSION })
     public HttpAnswer orion() throws AlternativeAnswerException {
         this.requirePermissions(VIEW_ORION_PREMISSION);
         
         final Map<String, Object> c = this.createContext(CONTENT_ORION);
         
         final Collection<Quadrant> allQuads = new TreeSet<>(new Comparator<Quadrant>() {
                     @Override
                     public int compare(Quadrant o1, Quadrant o2) {
                        return 0;
                     }
                 });
         allQuads.addAll(this.quadProvider.getAllQuadrants());
         c.put("allQuads", allQuads); //$NON-NLS-1$
         c.put("routeStart", this.getSession().getAttached(ROUTE_FROM_KEY)); //$NON-NLS-1$
         c.put("routeTarget", this.getSession().getAttached(ROUTE_TO_KEY)); //$NON-NLS-1$
         c.put("entries", this.azManager.getEntries(this.getSessionUser().getId())); //$NON-NLS-1$
         return this.makeAnswer(c);
     }
     
     
     
     @Get(PAGE_QUAD_LAYOUT)
     public HttpAnswer quadLayout() throws AlternativeAnswerException {
         this.requirePermissions(OrionController.WRITE_ORION_PREMISSION);
         return this.makeAnswer(this.createContext(CONTENT_QUAD_LAYOUT));
     }
     
     
     
     @Post(API_POST_LAYOUT)
     public HttpAnswer postQuadLayout(@Param("quadName") String quadName, 
             @Param("paste") String paste) {
         
         try {
             final Collection<Sector> sectors = QuadrantCnPParser.parse(paste, quadName);
             for (final Sector sector : sectors) {
                 Orion.INSTANCE.createQuadrantUpdater().updateSectorInformation(sector);
             }
         } catch (ParseException e) {
             e.printStackTrace();
         }
         
         return HttpAnswers.newRedirectAnswer(PAGE_QUAD_LAYOUT);
     }
     
     
     
     private void fillQuadrantContext(String quadName, Map<String, Object> c, 
             boolean showInfo) {
         final Quadrant q = this.quadProvider.getQuadrant(quadName);
         
         if (showInfo) {
             final List<Wormhole> holes = this.holeProvider.getWormholes(
                     q, this.quadProvider);
             c.put("holes", holes); //$NON-NLS-1$
         }
         c.put("showQuadInfo", showInfo); //$NON-NLS-1$
         c.put("quad", q); //$NON-NLS-1$
     }
     
     
     
     @Get(API_GET_QUADRANT)
     public HttpAnswer quadrant(@Param("quadName") String name) {
         
         if (!this.getMyPolly().roles().hasPermission(this.getSessionUser(), 
                 VIEW_ORION_PREMISSION)) {
             return HttpAnswers.newStringAnswer(403, MSG.httpNoPermission);
         }
 
         final Map<String, Object> c = this.createContext(""); //$NON-NLS-1$
         this.fillQuadrantContext(name, c, true);
         return HttpAnswers.newTemplateAnswer(CONTENT_QUADRANT, c);
     }
     
     
     
     @Get(API_GET_QUADRANT)
     public HttpAnswer quadrant(
             @Param("quadName") String name, 
             @Param("hlX") int hlX, 
             @Param("hlY") int hlY) {
         
         if (!this.getMyPolly().roles().hasPermission(this.getSessionUser(), 
                 VIEW_ORION_PREMISSION)) {
             return HttpAnswers.newStringAnswer(403, MSG.httpNoPermission);
         }
         
         final Map<String, Object> c = this.createContext(""); //$NON-NLS-1$
         this.fillQuadrantContext(name, c, true);
         c.put("hlX", hlX); //$NON-NLS-1$
         c.put("hlY", hlY); //$NON-NLS-1$
         return HttpAnswers.newTemplateAnswer(CONTENT_QUADRANT, c);
     }
     
     
     
     @Get(API_GET_SECTOR_INFO)
     public HttpAnswer sectorInfo(
             @Param("quadrant") String quadrant, 
             @Param("x") int x,
             @Param("y") int y) {
         
         if (!this.getMyPolly().roles().hasPermission(this.getSessionUser(), 
                 VIEW_ORION_PREMISSION)) {
             return HttpAnswers.newStringAnswer(403, MSG.httpNoPermission);
         }
         
         final Sector sector = this.quadProvider.getQuadrant(quadrant).getSector(x, y);
         final Map<String, Object> c = this.createContext(CONTENT_SECTOR_INFO);
         c.put("sector", sector); //$NON-NLS-1$
         if (sector != null) {
             final List<Wormhole> holes = this.holeProvider.getWormholes(
                     sector, this.quadProvider);
             c.put("holes", holes); //$NON-NLS-1$
         }
         
         return HttpAnswers.newTemplateAnswer(CONTENT_SECTOR_INFO, c);
     }
 
     
     
     public class SectorResult extends SuccessResult {
         public final String quadName;
         public final int x;
         public final int y;
         
         public SectorResult(Sector sector) {
             super(true, ""); //$NON-NLS-1$
             this.quadName = sector.getQuadName();
             this.x = sector.getX();
             this.y = sector.getY();
         }
     }
     
     
     
     private HttpAnswer updateRouteInformation(String quadrant, int x, int y, String key) {
         if (!this.getMyPolly().roles().hasPermission(this.getSessionUser(), 
                 ROUTE_ORION_PREMISSION)) {
             return new GsonHttpAnswer(403, 
                     new SuccessResult(false, MSG.httpNoPermission));
         }
         final Quadrant q = this.quadProvider.getQuadrant(quadrant);
         final Sector sector = q.getSector(x, y);
         if (sector != null) {
             this.getSession().set(key, sector);
             return new GsonHttpAnswer(200, 
                 new SectorResult(sector));
         }
         this.getSession().set(key, sector);
         return new GsonHttpAnswer(200, 
             new SuccessResult(false, "")); //$NON-NLS-1$
     }
     
     
     
     @Get(API_SET_ROUTE_FROM)
     public HttpAnswer setRouteFrom(
             @Param("quadrant") String quadrant, 
             @Param("x") int x,
             @Param("y") int y) {
         
         return this.updateRouteInformation(quadrant, x, y, ROUTE_FROM_KEY);
     }
     
     
     
     @Get(API_SET_ROUTE_TO)
     public HttpAnswer setRouteTo(
             @Param("quadrant") String quadrant, 
             @Param("x") int x,
             @Param("y") int y) {
         
         return this.updateRouteInformation(quadrant, x, y, ROUTE_TO_KEY);
     }
     
     
     
     @Get(API_GET_ROUTE)
     public HttpAnswer getRoute(
             @Param("fleetId") int fleetId,
             @Param(value = "jt", optional = true) String jt,
             @Param(value = "cjt", optional = true) String cjt,
             @Param(value ="awh", optional = true, defaultValue = "false") boolean avoidWormholes) {
         
         if (!this.getMyPolly().roles().hasPermission(this.getSessionUser(), 
                 ROUTE_ORION_PREMISSION)) {
             return HttpAnswers.newStringAnswer(403, MSG.httpNoPermission);
         }
         
         final Map<String, Object> c = this.createContext(""); //$NON-NLS-1$
         final HttpSession s = this.getSession();
         
         if (!s.isSet(ROUTE_FROM_KEY) || !s.isSet(ROUTE_TO_KEY)) {
             return HttpAnswers.newStringAnswer(""); //$NON-NLS-1$
         }
         final Sector start = (Sector) s.getAttached(ROUTE_FROM_KEY);
         final Sector target = (Sector) s.getAttached(ROUTE_TO_KEY);
         
         final TimespanType jumpTime;
         if (fleetId == -1) {
             jumpTime = this.parse(jt, new TimespanType(0));
         } else {
             jumpTime = this.azManager.getJumpTime(fleetId, this.getSessionUser());
         }
         final TimespanType currentJumpTime = this.parse(cjt, jumpTime);
         final RouteOptions options = new RouteOptions(jumpTime, currentJumpTime, 
                 avoidWormholes);
         final UniversePath path = this.pathPlanner.findShortestPath(start, target, 
                 options);
         c.put("path", path); //$NON-NLS-1$
         c.put("legend", SectorType.HIGHLIGHTS); //$NON-NLS-1$
         return HttpAnswers.newTemplateAnswer(CONTENT_ROUTE, c);
     }
     
     
     
     private TimespanType parse(String jumpTime, TimespanType alternative) {
         final Types types = this.getMyPolly().parse(jumpTime);
         if (types == null || !(types instanceof TimespanType)) {
             return alternative;
         }
         return (TimespanType) types;
     }
 }
