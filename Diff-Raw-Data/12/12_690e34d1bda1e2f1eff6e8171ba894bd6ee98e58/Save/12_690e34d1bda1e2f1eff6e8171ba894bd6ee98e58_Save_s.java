 package com.undeadscythes.supergenes.service;
 
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
         out.openFile(output);
         geneBase.save(out);
         out.closeFile();
         return true;
     }
 }
