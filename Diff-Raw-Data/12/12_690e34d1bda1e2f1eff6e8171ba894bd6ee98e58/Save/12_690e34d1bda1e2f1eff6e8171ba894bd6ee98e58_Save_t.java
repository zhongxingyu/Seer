 package com.undeadscythes.supergenes.service;
 
import com.undeadscythes.genebase.gedcom.*;
import com.undeadscythes.genebase.record.*;
import com.undeadscythes.supergenes.*;

 /**
  * Save the given {@link com.undeadscythes.genebase.GeneBase} to file as a
  * {@link com.undeadscythes.genebase.gedcom.GEDCOM}.
  *
  * @author UndeadScythes
  */
 public class Save extends AncestryService {
     @Override
     public boolean run(final String[] args) {
         String output = geneBase.getUID() + "-new.ged";
         if (args.length > 0) {
             output = args[0] + ".ged";
         }
        final Header header = (Header)geneBase.getFirst("HEAD");
        geneBase.remove("HEAD");
        geneBase.add(new SuperGenesHeader(header));
         out.openFile(output);
         geneBase.save(out);
         out.closeFile();
        out.println("GeneBase saved to " + output + ".");
        geneBase.remove("HEAD");
        geneBase.add(header);
         return true;
     }
 }
