 package com.undeadscythes.supergenes.service;
 
import com.undeadscythes.genebase.gedcom.GEDTag;
import com.undeadscythes.genebase.record.Individual;
import com.undeadscythes.genebase.structure.Date;
import com.undeadscythes.genebase.structure.Event;
import com.undeadscythes.metaturtle.exception.NoMetadataSetException;
import java.util.Calendar;
 
 /**
  * Calculate the age of an {@link Individual}.
  *
  * @author UndeadScythes
  */
 public class Age extends AncestryService {
     @Override
     public boolean run(final String[] args) {
         final Individual indi = getIndividual(args);
         if (indi.getUID().isNull()) {
             out.println("Cannot find individual.");
             return true;
         }
         int year = Calendar.getInstance().get(Calendar.YEAR);
         if (args.length == 2 && args[1].matches("[0-9]+")) {
             year = Integer.parseInt(args[1]);
         }
         try {
            final Event event = (Event)indi.getFirst(GEDTag.BIRT);
            final Date date = event.getDate();
            final int birth = date.year;
             out.println(indi.getFullName() + "'s age in " + year + " is ~" + (year - birth) + ".");
         } catch (NoMetadataSetException ex) {
             out.println("Unknown age, no birth year recorded.");
         }
         return true;
     }
 }
