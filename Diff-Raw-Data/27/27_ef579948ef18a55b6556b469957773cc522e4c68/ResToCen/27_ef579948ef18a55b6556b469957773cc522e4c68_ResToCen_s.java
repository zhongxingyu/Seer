 package com.undeadscythes.supergenes.service;
 
 import com.undeadscythes.genebase.gedcom.*;
 import com.undeadscythes.genebase.structure.*;
 import com.undeadscythes.metaturtle.exception.*;
 import com.undeadscythes.metaturtle.metadata.*;
 import com.undeadscythes.metaturtle.unique.*;
 
 /**
  * @author UndeadScythes
  */
 public class ResToCen extends AncestryService {
     @Override
     public boolean run(final String[] args) {
         for (UniqueMeta unique : geneBase.getUniqueMeta(RecordType.INDI)) {
             try {
                 for (Metadata meta : unique.getList(GEDTag.RESI)) {
                     final Event event = ((Event)meta);
                     switch (event.getDate().year) {
                         case 1841:
                            event.setDate(new Date(1841, 6, 6));
                             event.setTag(GEDTag.CENS);
                             break;
                         case 1851:
                            event.setDate(new Date(1851, 3, 30));
                             event.setTag(GEDTag.CENS);
                             break;
                         case 1861:
                            event.setDate(new Date(1861, 4, 7));
                             event.setTag(GEDTag.CENS);
                             break;
                         case 1871:
                            event.setDate(new Date(1871, 4, 2));
                             event.setTag(GEDTag.CENS);
                             break;
                         case 1881:
                            event.setDate(new Date(1881, 4, 3));
                             event.setTag(GEDTag.CENS);
                             break;
                         case 1891:
                            event.setDate(new Date(1891, 3, 5));
                             event.setTag(GEDTag.CENS);
                             break;
                         case 1901:
                            event.setDate(new Date(1901, 3, 31));
                             event.setTag(GEDTag.CENS);
                             break;
                         case 1911:
                            event.setDate(new Date(1911, 3, 2));
                             event.setTag(GEDTag.CENS);
                             break;
                     }
                 }
             } catch (NoMetadataSetException ex) {}
         }
         out.println("Converted all residence events to census events.");
         return true;
     }
 }
