 package fr.ultimate.breakfast.web.pages;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.bind.ValidationException;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.tapestry5.EventConstants;
 import org.apache.tapestry5.PersistenceConstants;
 import org.apache.tapestry5.SelectModel;
 import org.apache.tapestry5.ValueEncoder;
 import org.apache.tapestry5.annotations.Import;
 import org.apache.tapestry5.annotations.InjectComponent;
 import org.apache.tapestry5.annotations.OnEvent;
 import org.apache.tapestry5.annotations.Persist;
 import org.apache.tapestry5.annotations.Property;
 import org.apache.tapestry5.corelib.components.Form;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.apache.tapestry5.services.SelectModelFactory;
 
 import fr.ultimate.breakfast.domain.business.BreakfastManager;
 import fr.ultimate.breakfast.domain.model.Eater;
 import fr.ultimate.breakfast.domain.model.Team;
 import fr.ultimate.breakfast.domain.security.UltimateBreakfastSecurityContext;
 import fr.ultimate.breakfast.web.services.encoder.SecureEaterEncoder;
 
 /**
  * Commit page
  */
 @Import(stylesheet = "${exanpe.asset-base}/css/exanpe-t5-lib-skin.css")
 public class Commit
 {
     @Property
    @Persist(PersistenceConstants.FLASH)
     private List<Eater> providers;
 
     @Property
    @Persist(PersistenceConstants.FLASH)
     private List<Eater> absents;
 
     @Property
     @Persist
     private SelectModel eaterModel;
 
     @Property
     @Persist
     private ValueEncoder<Eater> valueEncoder;
 
     @InjectComponent
     private Form form;
 
     @Inject
     private BreakfastManager breakfastManager;
 
     @Inject
     private UltimateBreakfastSecurityContext securityContext;
 
     @Inject
     private SelectModelFactory selectModelFactory;
 
     void onActivate()
     {
         // always need a fresh copy
         Team p = breakfastManager.find(securityContext.getTeam().getId());
 
         List<Eater> eaters = p.getEaters();
 
         if (CollectionUtils.isNotEmpty(eaters))
         {
             eaterModel = selectModelFactory.create(eaters, "name");
             valueEncoder = new SecureEaterEncoder(eaters);
         }
 
         if (absents == null)
         {
             absents = new ArrayList<Eater>();
         }
         if (providers == null)
         {
             providers = new ArrayList<Eater>();
         }
 
     }
 
     @OnEvent(value = EventConstants.VALIDATE, component = "form")
     boolean validate() throws ValidationException
     {
         if (CollectionUtils.isEmpty(providers))
         {
             form.recordError("Personne n'a fait le petit dej ?");
             return false;
         }
         return true;
     }
 
     @Property
     @Persist(PersistenceConstants.FLASH)
     private boolean commitOk;
 
     @OnEvent(value = EventConstants.SUCCESS, component = "form")
     void commit()
     {
         breakfastManager.commit(securityContext.getTeam(), providers, absents);
         commitOk = true;

        if (absents != null)
        {
            absents.clear();
        }
        if (providers != null)
        {
            providers.clear();
        }
     }
 }
