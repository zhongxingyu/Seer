 package org.sprimaudi.zkutil;
 
 import org.sprimaudi.zkspring.entity.Referensi;
 import org.sprimaudi.zkspring.service.ReferensiService;
 import org.springframework.stereotype.Component;
 import org.zkoss.zk.ui.select.annotation.VariableResolver;
 import org.zkoss.zul.Radio;
 import org.zkoss.zul.Radiogroup;
 
 import javax.inject.Inject;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: jote
  * Date: 7/21/12
  * Time: 6:17 PM
  * To change this template use File | Settings | File Templates.
  */
 @Component(value = "referensiUtil")
 public class ReferensiUtil {
     @Inject
     ReferensiService referensiService;
 
     private static final String RADIO_GRUP_KEY = "radio_grup_key";
 
     public Referensi fromRadioGrup(Radiogroup radioGrup) {
         String sgrup = (String) radioGrup.getAttribute(RADIO_GRUP_KEY);
         Long lgrup = (sgrup != null && !"".equalsIgnoreCase(sgrup)) ?
                 Long.parseLong(sgrup) : null;
         if (sgrup == null || radioGrup.getSelectedItem() == null) {
             return null;
         }
         return referensiService.byKodeGrup(
                 radioGrup.getSelectedItem().getValue(), lgrup);
     }
 
     public void toRadioGrup(Radiogroup radioGrup, Referensi referensi) {
         String sgrup = (String) radioGrup.getAttribute(RADIO_GRUP_KEY);
         Long lgrup = (sgrup != null && !"".equalsIgnoreCase(sgrup)) ?
                 Long.parseLong(sgrup) : null;
        if (sgrup == null || lgrup != referensi.getGrup()) {
             return;
         }
        List<Radio> rads = radioGrup.getItems();
        for (Iterator<Radio> rad = rads.iterator(); rad.hasNext(); ) {
            Radio next = rad.next();
//                if (StringUtil.nvl)
        }

     }
 }
