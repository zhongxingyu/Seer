 package org.openengsb.framework.edb.updateHook.internal;
 
 import java.util.List;
 
 import org.openengsb.core.api.edb.EDBObject;
 import org.openengsb.core.api.edb.EDBUpdateHook;
 import org.openengsb.similarity.Indexer;
 
 public class UpdateHook implements EDBUpdateHook {
 
     private Indexer indexer;
 
     @Override
     public void update(List<EDBObject> inserts, List<EDBObject> updates, List<EDBObject> deletes) {
         indexer.updateIndex(inserts, updates, deletes);
     }

    public void setIndexer(Indexer indexer) {
        this.indexer = indexer;
    }

 }
