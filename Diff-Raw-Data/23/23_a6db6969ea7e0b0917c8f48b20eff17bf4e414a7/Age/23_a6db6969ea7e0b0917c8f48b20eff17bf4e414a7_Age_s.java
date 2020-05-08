 package com.undeadscythes.supergenes.service;
 
import com.undeadscythes.genebase.gedcom.*;
import com.undeadscythes.genebase.record.*;
import com.undeadscythes.genebase.structure.*;
import com.undeadscythes.metaturtle.exception.*;
import com.undeadscythes.supergenes.exception.*;
import java.util.*;
 
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
            final Event event = (Event)indi.getList(GEDTag.BIRT);
            final int birth = event.getDate().year;
            if (birth == 0) throw new TagNotSetException(GEDTag.BIRT);
             out.println(indi.getFullName() + "'s age in " + year + " is ~" + (year - birth) + ".");

        } catch (TagNotSetException ex) {
            out.println("Unknown age, no birth year recorded.");
         } catch (NoMetadataSetException ex) {
             out.println("Unknown age, no birth year recorded.");
         }
         return true;
     }
 }
