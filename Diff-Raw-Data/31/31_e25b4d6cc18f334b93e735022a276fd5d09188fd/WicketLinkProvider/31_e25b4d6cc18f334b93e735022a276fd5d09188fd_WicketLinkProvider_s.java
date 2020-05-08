 package de.flower.rmt.ui.app;
 
 import de.flower.rmt.service.ILinkProvider;
import org.apache.wicket.protocol.http.RequestUtils;
 import org.apache.wicket.request.Url;
 import org.apache.wicket.request.cycle.RequestCycle;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Component;
 
 /**
  * @author flowerrrr
  */
 @Component
 public class WicketLinkProvider implements ILinkProvider {
 
    private final static Logger log = LoggerFactory.getLogger(WicketLinkProvider.class);

     @Override
     public String deepLinkEvent(final Long eventId) {
         String relativeUrl = urlForEvent(eventId).toString();
        String url = toAbsoluteUrl(relativeUrl);
         return url;
     }
 
     private static CharSequence urlForEvent(Long eventId) {
         return RequestCycle.get().urlFor(de.flower.rmt.ui.page.event.player.EventPage.class, new PageParameters().set(de.flower.rmt.ui.page.event.player.EventPage.PARAM_EVENTID, eventId));
     }

    public static String toAbsoluteUrl(String relativeUrl) {
        String requestUrl = RequestCycle.get().getRequest().getUrl().toString();
        String url = RequestUtils.toAbsolutePath(requestUrl, relativeUrl);
        return RequestCycle.get().getUrlRenderer().renderFullUrl(Url.parse(url));
    }
 }
