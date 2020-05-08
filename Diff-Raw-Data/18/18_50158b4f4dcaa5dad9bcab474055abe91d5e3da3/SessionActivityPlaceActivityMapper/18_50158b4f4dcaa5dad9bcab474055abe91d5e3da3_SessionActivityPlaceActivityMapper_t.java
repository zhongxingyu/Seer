 package org.dhamma.translations.client;
 
 import javax.inject.Inject;
 
 import org.dhamma.translations.client.managed.ActivityFactory;
import org.dhamma.translations.client.places.ApplicationPlace;
 import org.dhamma.translations.client.places.LoginPlace;
 
 import com.google.gwt.activity.shared.Activity;
import com.google.gwt.core.client.GWT;
 import com.google.gwt.place.shared.Place;
 
 import de.mkristian.gwt.rails.Notice;
import de.mkristian.gwt.rails.places.RestfulActionEnum;
 import de.mkristian.gwt.rails.places.RestfulPlace;
 import de.mkristian.gwt.rails.session.Guard;
 import de.mkristian.gwt.rails.session.NeedsAuthorization;
 import de.mkristian.gwt.rails.session.NoAuthorization;
 
 public class SessionActivityPlaceActivityMapper extends ActivityPlaceActivityMapper {
 
     private final Guard guard;
 
     @Inject
     public SessionActivityPlaceActivityMapper(ActivityFactory factory, Guard guard, Notice notice) {
         super(factory, notice);
         this.guard = guard;
     }
 
     public Activity getActivity(Place place) {
         return pessimisticGetActivity(place);
     }
 
     /**
      * pessimistic in the sense that default is authorisation, only the places
      * which implements {@link NoAuthorization} will be omitted by the check.
      */
     protected Activity pessimisticGetActivity(Place place) {
         if (!(place instanceof NoAuthorization)) {
             if(guard.hasSession()){
                 if(!guard.isAllowed((RestfulPlace<?,?>)place)){
                     notice.warn("nothing to see");
                     return null;
                 }
                 //TODO move into a dispatch filter or callback filter
                 guard.resetCountDown();
             }
             else {
                 return LoginPlace.LOGIN.create(factory);
             }
         }
         return super.getActivity(place);
     }
 
     /**
      * optimistic in the sense that default is no authorisation, only the places
      * which implements {@link NeedsAuthorization} will be checked.
      */
     protected Activity optimisticGetActivity(Place place) {
         if (place instanceof NeedsAuthorization) {
             if(guard.hasSession()){
                 if(!guard.isAllowed((RestfulPlace<?,?>)place)){
                     notice.warn("nothing to see");
                     return null;
                 }
             }
             else {
                 return LoginPlace.LOGIN.create(factory);
             }
         }
         return super.getActivity(place);
     }
 }
