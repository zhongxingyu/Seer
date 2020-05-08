 package com.undeadscythes.supergenes.service;
 
import com.undeadscythes.genebase.gedcom.GEDTag;
import com.undeadscythes.genebase.gedcom.RecordType;
import com.undeadscythes.genebase.structure.Date;
import com.undeadscythes.genebase.structure.Event;
import com.undeadscythes.metaturtle.exception.NoMetadataSetException;
import com.undeadscythes.metaturtle.metadata.Metadata;
import com.undeadscythes.metaturtle.unique.UniqueMeta;
 
 /**
  * Convert {@link GEDTag#RESI residency} events to {@link GEDTag#CENS census}
  * events if the dates are a close enough match to actual censuses.
  *
  * @author UndeadScythes
  */
 public class ResToCen extends AncestryService {
     @Override
     public boolean run(final String[] args) {
         for (UniqueMeta unique : geneBase.getUniqueMeta(RecordType.INDI)) {
            for (Metadata meta : unique.getList(GEDTag.RESI)) {
                final Event event = ((Event)meta);
                try {
                     switch (event.getDate().year) {
                         case 1841:
                             event.setDate(new Date("6 6 1841"));
                             event.setTag(GEDTag.CENS);
                             break;
                         case 1851:
                             event.setDate(new Date("30 3 1851"));
                             event.setTag(GEDTag.CENS);
                             break;
                         case 1861:
                             event.setDate(new Date("7 4 1861"));
                             event.setTag(GEDTag.CENS);
                             break;
                         case 1871:
                             event.setDate(new Date("2 4 1871"));
                             event.setTag(GEDTag.CENS);
                             break;
                         case 1881:
                             event.setDate(new Date("3 4 1881"));
                             event.setTag(GEDTag.CENS);
                             break;
                         case 1891:
                             event.setDate(new Date("5 3 1891"));
                             event.setTag(GEDTag.CENS);
                             break;
                         case 1901:
                             event.setDate(new Date("31 3 1901"));
                             event.setTag(GEDTag.CENS);
                             break;
                         case 1911:
                             event.setDate(new Date("2 3 1911"));
                             event.setTag(GEDTag.CENS);
                             break;
                     }
                } catch (NoMetadataSetException ex) {}
            }
         }
         out.println("Converted all residence events to census events.");
         return true;
     }
 }
