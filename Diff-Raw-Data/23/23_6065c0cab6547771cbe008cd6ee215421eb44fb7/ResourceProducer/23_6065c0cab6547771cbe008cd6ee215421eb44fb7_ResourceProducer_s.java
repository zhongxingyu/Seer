 package cz.muni.fi.pv243.et.util;
 
 
 import cz.muni.fi.pv243.et.model.Currency;
 import cz.muni.fi.pv243.et.model.Person;
 import cz.muni.fi.pv243.et.model.PersonWrapper;
 import org.picketlink.Identity;
 
 import javax.enterprise.context.RequestScoped;
 import javax.enterprise.context.SessionScoped;
 import javax.enterprise.inject.Produces;
 import javax.faces.context.FacesContext;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 public class ResourceProducer {
 
     @Inject
     private Identity identity;
 
     @Produces
     @RequestScoped
     public FacesContext produceFacesContext() {
         return FacesContext.getCurrentInstance();
     }
 
     @Produces
     @Named("currencies")
     public Currency[] getCurrencies() {
         return Currency.values();
     }
 
    @Produces
    @RequestScoped
    @CurrentPerson
    public PersonWrapper getCurrentPerson() {
        return new PersonWrapper(identity.getUser().<Person>getAttribute("person").getValue());
    }

 }
