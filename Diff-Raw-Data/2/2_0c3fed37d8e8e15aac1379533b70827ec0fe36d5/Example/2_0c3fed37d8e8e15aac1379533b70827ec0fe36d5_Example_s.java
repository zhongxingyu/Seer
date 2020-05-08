 /*
 
     ensembl-rest-client  Java client for the Ensembl REST API.
     Copyright (c) 2013 held jointly by the individual authors.
 
     This library is free software; you can redistribute it and/or modify it
     under the terms of the GNU Lesser General Public License as published
     by the Free Software Foundation; either version 3 of the License, or (at
     your option) any later version.
 
     This library is distributed in the hope that it will be useful, but WITHOUT
     ANY WARRANTY; with out even the implied warranty of MERCHANTABILITY or
     FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
     License for more details.
 
     You should have received a copy of the GNU Lesser General Public License
     along with this library;  if not, write to the Free Software Foundation,
     Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA.
 
     > http://www.fsf.org/licensing/licenses/lgpl.html
     > http://www.opensource.org/licenses/lgpl-license.php
 
 */
 package com.github.heuermh.ensemblrestclient.example;
 
 import java.util.List;
 
 import com.github.heuermh.ensemblrestclient.Allele;
 import com.github.heuermh.ensemblrestclient.EnsemblRestClientModule;
 import com.github.heuermh.ensemblrestclient.FeatureService;
 import com.github.heuermh.ensemblrestclient.Location;
 import com.github.heuermh.ensemblrestclient.Lookup;
 import com.github.heuermh.ensemblrestclient.LookupService;
 import com.github.heuermh.ensemblrestclient.Transcript;
 import com.github.heuermh.ensemblrestclient.Variation;
 import com.github.heuermh.ensemblrestclient.VariationService;
 import com.github.heuermh.ensemblrestclient.VariationConsequences;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 
 /**
  * Example.
  */
 public final class Example {
 
     public static void main(final String args[]) {
         Injector injector = Guice.createInjector(new EnsemblRestClientModule());
 
 
         FeatureService featureService = injector.getInstance(FeatureService.class);
 
        System.out.println("features, 7:140424943-140425943");
         for (Variation variation : featureService.variationFeatures("human", "7:140424943-140425943")) {
             System.out.println(variation.getId() + "\t" + variation.getReference() + "\t" + variation.getAlternate() + "\t" + variation.getLocation().getName() + "\t" + variation.getLocation().getStart() + "\t" + variation.getLocation().getEnd() + "\t" + variation.getLocation().getStrand());
         }
 
         LookupService lookupService = injector.getInstance(LookupService.class);
 
         System.out.println("\nlookup, ENSG00000157764");
         Lookup ensg00000157764 = lookupService.lookup("human", "ENSG00000157764");
         System.out.println(ensg00000157764.getId() + "\t" + ensg00000157764.getSpecies() + "\t" + ensg00000157764.getType() + "\t" + ensg00000157764.getDatabase() + "\t" + ensg00000157764.getLocation().getName() + "\t" + ensg00000157764.getLocation().getStart() + "\t" + ensg00000157764.getLocation().getEnd() + "\t" + ensg00000157764.getLocation().getStrand());
 
         VariationService variationService = injector.getInstance(VariationService.class);
 
         System.out.println("\nid search, COSM476");
         VariationConsequences cosm476 = variationService.consequences("human", "COSM476");
 
         for (Transcript transcript : cosm476.getTranscripts()) {
             for (Allele allele : transcript.getAlleles()) {
                 for (String consequenceTerm : allele.getConsequenceTerms()) {
                     Location location = cosm476.getLocation();
                     System.out.println(cosm476.getName() + "\t" + location.getName() + "\t" + location.getStart() + "\t" + location.getEnd() + "\t" + location.getStrand() + "\t" + transcript.getGeneId() + "\t" + transcript.getTranscriptId() + "\t" + allele.getAlleleString() + "\t" + consequenceTerm);
                 }
             }
         }
 
  
         System.out.println("\nregion search, 9:22125503-22125502:1");
         VariationConsequences region = variationService.consequences("human", "9:22125503-22125502:1", "C");
 
         for (Transcript transcript : region.getTranscripts()) {
             for (Allele allele : transcript.getAlleles()) {
                 for (String consequenceTerm : allele.getConsequenceTerms()) {
                     Location location = region.getLocation();
                     System.out.println(region.getName() + "\t" + location.getName() + "\t" + location.getStart() + "\t" + location.getEnd() + "\t" + location.getStrand() + "\t" + transcript.getGeneId() + "\t" + transcript.getTranscriptId() + "\t" + allele.getAlleleString() + "\t" + consequenceTerm);
                 }
             }
         }
     }
 }
