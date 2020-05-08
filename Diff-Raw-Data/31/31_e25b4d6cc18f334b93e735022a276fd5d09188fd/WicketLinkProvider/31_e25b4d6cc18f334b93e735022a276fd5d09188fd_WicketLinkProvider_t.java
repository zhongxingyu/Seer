 package de.flower.rmt.ui.app;
 
 import de.flower.rmt.service.ILinkProvider;
 import org.apache.wicket.request.Url;
 import org.apache.wicket.request.cycle.RequestCycle;
 import org.apache.wicket.request.mapper.parameter.PageParameters;
 import org.springframework.stereotype.Component;
 
 /**
  * @author flowerrrr
  */
 @Component
 public class WicketLinkProvider implements ILinkProvider {
 
     @Override
     public String deepLinkEvent(final Long eventId) {
         String relativeUrl = urlForEvent(eventId).toString();
        String url = RequestCycle.get().getUrlRenderer().renderFullUrl(Url.parse(relativeUrl));
         return url;
     }
 
     private static CharSequence urlForEvent(Long eventId) {
         return RequestCycle.get().urlFor(de.flower.rmt.ui.page.event.player.EventPage.class, new PageParameters().set(de.flower.rmt.ui.page.event.player.EventPage.PARAM_EVENTID, eventId));
     }
 }
