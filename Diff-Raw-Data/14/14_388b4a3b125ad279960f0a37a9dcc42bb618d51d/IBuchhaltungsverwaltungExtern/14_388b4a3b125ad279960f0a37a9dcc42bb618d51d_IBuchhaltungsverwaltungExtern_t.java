 package komponentenInterfaces.extern;
 
 import typClasses.RechnungTyp;
 
 import java.util.Date;
 
 /**
  *
  */
 public interface IBuchhaltungsverwaltungExtern {
     RechnungTyp erstelleRechnung(Date rechnungsDatum);
 
     RechnungTyp addAuftragZuRechnung(int rechnungsNr, int auftragsNr);
 }
