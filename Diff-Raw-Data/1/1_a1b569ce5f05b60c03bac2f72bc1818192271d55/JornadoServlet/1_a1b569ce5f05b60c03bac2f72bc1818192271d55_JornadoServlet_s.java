 package jornado;
 
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 import com.google.inject.name.Named;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.Cookie;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * TODO: encrypt user cookie with login date and IP address; validate on the way in
  * TODO: Maybe make this compatible with people just using web.xml (e.g. provide crappy no-arg constructor and crappy initializer. ugh.)
  * TODO: support app-specified 500 pages with a nice debug mode
  * TODO: I've gone to some pains to allow an app-specified Request object but didn't close the loop on this
  */
 public class JornadoServlet<R extends Request<U>, U extends WebUser> extends HttpServlet {
   private final Router<R> router;
   private final UserService<U> userService;
   private final SecureCookieService secureCookieService;
   private final RequestFactory<U, R> requestFactory;
   private final Config config;
   private final Injector injector;
   private final List<Class<Filter<R>>> filterClasses;
 
   @Inject
   @SuppressWarnings("unchecked")
   public JornadoServlet(Router router, @Named("filters") List filters, UserService userService, SecureCookieService secureCookieService, RequestFactory requestFactory, Config config, Injector injector) {
     this.router = router;
     this.filterClasses = filters;
     this.userService = userService;
     this.secureCookieService = secureCookieService;
     this.requestFactory = requestFactory;
     this.config = config;
     this.injector = injector;
   }
 
   @Override
   protected void service(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
     RequestProfile.clear();
 
     final ServletBackedRequest<U> servletBackedRequest = new ServletBackedRequest<U>(httpServletRequest, userService, secureCookieService);
     final R request = requestFactory.createRequest(servletBackedRequest);
 
     final RouteHandlerData<R> routeHandlerData = router.route(request);
 
     if (routeHandlerData != null) {
       final Class<? extends Handler<R>> handlerClass = routeHandlerData.getHandlerClass();
 
       servletBackedRequest.setRouteHandlerData(routeHandlerData);
       final Handler<R> handler = injector.getInstance(handlerClass);
 
       Filter.FilterChain<R> chain = new Filter.FilterChain<R>() {
         Response current;
         Iterator<Class<Filter<R>>> f = filterClasses.iterator();
         @Override
         public Response doFilter(R request) {
           if (f.hasNext()) {
             return injector.getInstance(f.next()).filter(request, handlerClass, this);
           } else {
             return handler.handle(request);
           }
         }
       };
 
       sendResponse(httpServletResponse, request, chain.doFilter(request));
 
       if (config.isDebug()) {
         RequestProfile.finish();
         PrintWriter writer = new PrintWriter(System.out);
         RequestProfile.render(writer);
         writer.flush();
       }
     } else {
       // TODO: support app-defined pages
       httpServletResponse.sendError(404);
     }
   }
 
   private void sendResponse(HttpServletResponse servletResponse, R request, Response response) throws IOException {
     final int statusCode = response.getStatus().getCode();
     final String reasonPhrase = response.getStatus().getReasonPhrase();
     if (reasonPhrase == null) {
       servletResponse.setStatus(statusCode);
     } else {
       servletResponse.sendError(statusCode, reasonPhrase);
     }
 
     for (HeaderOp op : response.getHeaderOps()) {
       op.execute(servletResponse);
     }
 
     if (request.isLoginCookieInvalid()) {
       final Cookie cookie = request.getCookie(Constants.LOGIN_COOKIE);
       if (cookie != null) {
         cookie.setMaxAge(0);
         servletResponse.addCookie(cookie);
       }
     }
 
     final Body body = response.getBody();
     if (body != null) {
       RenderService renderService = injector.getInstance(body.getRenderServiceClass());
       servletResponse.setContentType(body.getMediaType().toString());
       renderService.write(servletResponse.getWriter(), body);
     }
   }
 }
