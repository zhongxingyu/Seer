 /*
 * Copyright (c) 2010 The Jackson Laboratory
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software. If not, see <http://www.gnu.org/licenses/>.
 */
 package org.jax.drakegenetics.server;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  *
  * @author gbeane
  */
 public class GenotypeService {
 
     // map haplotype -> Gene Symbol -> Allele
     private Map<String, Map<String, String>> haplotypeToAlleles;
 
     public GenotypeService()
     {
         haplotypeToAlleles = new HashMap<String, Map<String, String>>();
 
         init();
     }
 
     private void init()
     {
         //XXX hard code in some haplotypes for testing
 
         Map<String, String> genotypeMap;
 
         //P1_M
         genotypeMap = new HashMap<String, String>();
         genotypeMap.put("Otc", "B");
         genotypeMap.put("Tyrp1", "Bog");
         genotypeMap.put("Myo5a", "D");
         genotypeMap.put("Ar", "Tr");
         genotypeMap.put("Dll3", "T");
         genotypeMap.put("M", "M");
         genotypeMap.put("Xdh","F");
         genotypeMap.put("Tyr", "C");
         genotypeMap.put("Pax6", "N");
         genotypeMap.put("Eda", "A1");
         genotypeMap.put("Dia", "Db");
         haplotypeToAlleles.put("P1_M", genotypeMap);
 
         //P1_P
         genotypeMap = new HashMap<String, String>();
         genotypeMap.put("Otc", "b");
         genotypeMap.put("Tyrp1", "bog");
         genotypeMap.put("Myo5a", "d");
         genotypeMap.put("Ar", "Tr");
         genotypeMap.put("Dll3", "t");
         genotypeMap.put("M", "m");
         genotypeMap.put("Xdh","f");
         genotypeMap.put("Tyr", "C");
         genotypeMap.put("Pax6", "n");
         genotypeMap.put("Eda", "A2");
         genotypeMap.put("Dia", "Db");
         haplotypeToAlleles.put("P1_P", genotypeMap);
 
         //P2_M
         genotypeMap = new HashMap<String, String>();
         genotypeMap.put("Otc", "b");
         genotypeMap.put("Tyrp1", "Bog");
         genotypeMap.put("Myo5a", "d");
         genotypeMap.put("Ar", "Tr");
         genotypeMap.put("Dll3", "t");
         genotypeMap.put("M", "m");
         genotypeMap.put("Xdh","f");
         genotypeMap.put("Tyr", "C");
         genotypeMap.put("Pax6", "n");
         genotypeMap.put("Eda", "A2");
         genotypeMap.put("Dia", "Db");
         haplotypeToAlleles.put("P2_M", genotypeMap);
 
         //P2_P
         genotypeMap = new HashMap<String, String>();
         genotypeMap.put("M", "m");
         genotypeMap.put("Xdh","f");
         genotypeMap.put("Tyr", "C");
         genotypeMap.put("Pax6", "n");
         genotypeMap.put("Eda", "A2");
         genotypeMap.put("Dia", "Db");
         haplotypeToAlleles.put("P2_P", genotypeMap);
         
         //BOB_M
         genotypeMap = new HashMap<String, String>();
         genotypeMap.put("Otc", "b");
         genotypeMap.put("Tyrp1", "Bog");
         genotypeMap.put("Myo5a", "d");
         genotypeMap.put("Ar", "tr");
         genotypeMap.put("Dll3", "t");
         genotypeMap.put("M", "m");
         genotypeMap.put("Xdh","f");
         genotypeMap.put("Tyr", "C");
         genotypeMap.put("Pax6", "n");
         genotypeMap.put("Eda", "A2");
         genotypeMap.put("Dia", "Db");
        haplotypeToAlleles.put("BOB_M", genotypeMap);
         

     }
 
     public String getAllele(String haplotype, String geneSymbol)
     {
         return haplotypeToAlleles.get(haplotype).get(geneSymbol);
     }
 
 }
