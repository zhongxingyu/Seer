 package nl.voorbeeldsite.formulieren.demo;
 
 import nl.voorbeeldsite.formulieren.Wizard;
 import nl.voorbeeldsite.formulieren.global.Persoon;
 import org.springframework.context.annotation.Scope;
 
 import javax.faces.context.FacesContext;
 import javax.inject.Named;
 import java.util.Date;
 
 @Named("formBean")
 @Scope("session")
 public class FormBean {
 
     private Persoon persoon;
     private Date ingangsDatum;
 
     public Persoon getPersoon() {
	    if (this.persoon == null) {
	    	return new Persoon();
	    } else {
         	return persoon;
	    }
     }
 
     public void setPersoon(Persoon persoon) {
         this.persoon = persoon;
     }
 
     public Date getIngangsDatum() {
        if (ingangsDatum == null) {
            return new Date();
        } else {
            return ingangsDatum;
        }
     }
 
     public void setIngangsDatum(Date ingangsDatum) {
         this.ingangsDatum = ingangsDatum;
     }
 
     public String next() {
         Wizard wizard = Wizard.makeWizard(FacesContext.getCurrentInstance());
         return wizard.nextStep();
     }
 
     public String previous() {
         Wizard wizard = Wizard.makeWizard(FacesContext.getCurrentInstance());
         return wizard.previousStep();
     }
 }
