 package org.makumba.parade.view.managers;
 
 import java.util.List;
 
 import org.makumba.parade.model.Row;
 import org.makumba.parade.model.RowMakumba;
 import org.makumba.parade.view.interfaces.ParadeView;
 
 import freemarker.template.SimpleHash;
 
 public class MakumbaViewManager implements ParadeView {
 
     public void setParadeViewHeader(List<String> headers) {
         headers.add("Makumba version");
     }
 
     public void setParadeView(SimpleHash rowInformation, Row r) {
         SimpleHash makModel = new SimpleHash();
         RowMakumba makdata = (RowMakumba) r.getRowdata().get("makumba");
 
         makModel.put("version", makdata.getVersion());
         makModel.put("database", makdata.getDb());
         rowInformation.put("mak", makModel);
     }
 
 }
