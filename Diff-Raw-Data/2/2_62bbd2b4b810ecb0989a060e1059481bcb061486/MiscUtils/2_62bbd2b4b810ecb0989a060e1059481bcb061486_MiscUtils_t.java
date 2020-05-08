 package org.esupportail.opi.web.utils;
 
 import org.esupportail.opi.domain.DomainApoService;
 import org.esupportail.opi.domain.beans.user.indcursus.IndCursusScol;
 import org.esupportail.opi.web.beans.pojo.IndCursusScolPojo;
 import org.esupportail.opi.web.beans.pojo.IndividuPojo;
 import org.esupportail.opi.web.beans.utils.comparator.ComparatorString;
 import org.esupportail.wssi.services.remote.Etablissement;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 public final class MiscUtils {
     private MiscUtils() { throw new UnsupportedOperationException(); }
 
     public static void initIndCursusScolPojo(IndividuPojo iP, DomainApoService apoService) {
         final ArrayList<IndCursusScolPojo> cursusList = new ArrayList<>();
 		for (IndCursusScol iCur : iP.getIndividu().getCursusScol()) {
             final IndCursusScolPojo pojo = new IndCursusScolPojo(iCur);
             final Etablissement etablissement =
                     apoService.getEtablissement(iCur.getCodEtablissement());
            iCur.setCodTypeEtab(etablissement == null ? "" : etablissement.getCodTpe());
             pojo.setEtablissement(etablissement);
             cursusList.add(pojo);
         }
 		Collections.sort(cursusList, new ComparatorString(IndCursusScolPojo.class));
         iP.setIndCursusScolPojo(cursusList);
     }
 }
