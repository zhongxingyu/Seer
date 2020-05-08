 package org.atlasapi.feeds.radioplayer;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 import java.io.IOException;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import com.google.common.base.Function;
 import com.google.common.base.Strings;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableMap.Builder;
 import com.google.common.collect.Iterables;
 import com.metabroadcast.common.http.HttpStatusCode;
 import com.metabroadcast.common.media.MimeType;
 import com.metabroadcast.common.security.HttpBasicAuthChecker;
 import com.metabroadcast.common.security.UsernameAndPassword;
 import com.metabroadcast.common.webapp.health.HealthController;
 
 @Controller
 public class RadioPlayerHealthController {
 
     private final HealthController main;
     private final Map<String, Iterable<String>> slugs;
     private final HttpBasicAuthChecker checker;
     private final Iterable<String> serviceIds;
 
     public RadioPlayerHealthController(HealthController main, Iterable<String> serviceIds, String password) {
         if (!Strings.isNullOrEmpty(password)) {
             this.checker = new HttpBasicAuthChecker(ImmutableList.of(new UsernameAndPassword("bbc", password)));
         } else {
             this.checker = null;
         }
         this.main = checkNotNull(main);
         this.serviceIds = checkNotNull(serviceIds);
         slugs = createSlugMap();
     }
 
     private Map<String, Iterable<String>> createSlugMap() {
         Builder<String, Iterable<String>> slugMap = ImmutableMap.builder();
         for (String serviceId : serviceIds) {
             slugMap.put(serviceId, slugsFor(serviceId));
         }
         return slugMap.build();
     }
 
     private Iterable<String> slugsFor(final String serviceId) {
         return Iterables.concat(ImmutableList.of("ukrp-connect-"+serviceId), Iterables.transform(RadioPlayerServices.services, new Function<RadioPlayerService, String>() {
             @Override
             public String apply(RadioPlayerService service) {
                 return String.format("ukrp-%s-%s", serviceId, service.getName());
             }
         }));
     }
 
     @RequestMapping("feeds/ukradioplayer/health/{serviceId}")
     public String radioplayerHealth(HttpServletRequest request, HttpServletResponse response, @PathVariable("serviceId") String sid) throws IOException {
         if (checker == null) {
             response.setContentType(MimeType.TEXT_PLAIN.toString());
             response.getOutputStream().print("No password set up, health page cannot be viewed");
             return null;
         }
         boolean allowed = checker.check(request);
         if (allowed) {
             if(slugs.containsKey(sid)) {
                return main.showHealthPageForSlugs(response, slugs.get(sid));
             } else {
                 response.sendError(HttpStatusCode.NOT_FOUND.code());
             }
         }
         HttpBasicAuthChecker.requestAuth(response, "Heath Page");
         return null;
     }
 
 }
