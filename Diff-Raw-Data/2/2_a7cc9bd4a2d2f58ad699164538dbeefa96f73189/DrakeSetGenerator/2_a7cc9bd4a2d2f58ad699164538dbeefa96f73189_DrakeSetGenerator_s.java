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
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this software.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.jax.drakegenetics.gwtclientapp.client;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import org.jax.drakegenetics.shareddata.client.DiploidGenome;
 import org.jax.drakegenetics.shareddata.client.DrakeSpeciesSingleton;
 
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.rpc.AsyncCallback;
 import com.google.gwt.user.client.ui.Image;
 
 /**  This is a class solely for generating a demo set of drakes
  * for breeding and metabolism experiments.
  * 
  * @author <A HREF="mailto:dave.walton@jax.org">Dave Walton</A>
  */
 public class DrakeSetGenerator  {
 
     private Map<String, String> femalePhenome = new HashMap<String,String>();
     private Map<String, String> malePhenome = new HashMap<String,String>();
     private Map<String, String> sexRevPhenome = new HashMap<String,String>();
     private Map<String, String> diabeticPhenome = new HashMap<String,String>();
 
     private void setFemalePhenome(Map<String,String> phenome) {
         Set<String> keys = phenome.keySet();
         for (String key: keys) {
             femalePhenome.put(key,phenome.get(key));
         }
     }
     
     private void setMalePhenome(Map<String,String> phenome) {
         Set<String> keys = phenome.keySet();
         for (String key: keys) {
             malePhenome.put(key,phenome.get(key));
         }
     }
     
     private void setSexRevPhenome(Map<String,String> phenome) {
         Set<String> keys = phenome.keySet();
         for (String key: keys) {
             sexRevPhenome.put(key,phenome.get(key));
         }
     }
     
     private void setDiabeticPhenome(Map<String,String> phenome) {
         Set<String> keys = phenome.keySet();
         for (String key: keys) {
             diabeticPhenome.put(key,phenome.get(key));
         }
     }
     
     public Folder getTreeModel(DrakeGeneticsServiceAsync dgs) {
         Image f_small_example = new Image("images/eyes/SEF51311.jpg");
         Image f_large_example = new Image("images/eyes/LEF51311.jpg");
         Image m_small_example = new Image("images/eyes/SEM40100.jpg");
         Image m_large_example = new Image("images/eyes/LEM40100.jpg");
         Image d_small_example = new Image("images/eyes/SEF1bb2b.jpg");
         Image d_large_example = new Image("images/eyes/LEF1bb2b.jpg");
         Image b_small_example = new Image("images/eyes/SEM40100.jpg");
         Image b_large_example = new Image("images/eyes/LEM40100.jpg");
         
         DiploidGenome female_genome = new DiploidGenome("P1_M", "P1_P", true,
                 DrakeSpeciesSingleton.getInstance());
         DiploidGenome male_genome = new DiploidGenome("P2_M", "P2_P", false,
                 DrakeSpeciesSingleton.getInstance());
         DiploidGenome sex_rev_male_genome = new DiploidGenome("BOB_M", "P2_P", 
                true, DrakeSpeciesSingleton.getInstance());
         DiploidGenome db_female_genome = new DiploidGenome("DB_M", "DB_P", 
                 true, DrakeSpeciesSingleton.getInstance());
 
         dgs.getPhenome(female_genome,
                 new AsyncCallback<Map<String, String>>() {
                     public void onSuccess(Map<String, String> phenome) {
                         GWT.log("Have Female Phenotype!");
                         GWT.log(phenome.toString());
                         setFemalePhenome(phenome);
                     }
 
                     public void onFailure(Throwable caught) {
                         caught.printStackTrace();
                         GWT.log(caught.getMessage());
                     }
                 });
 
         dgs.getPhenome(male_genome,
                 new AsyncCallback<Map<String, String>>() {
                     public void onSuccess(Map<String, String> phenome) {
                         GWT.log("Have Male Phenotype!");
                         GWT.log(phenome.toString());
                         setMalePhenome(phenome);
                     }
 
                     public void onFailure(Throwable caught) {
                         caught.printStackTrace();
                         GWT.log(caught.getMessage());
                     }
                 });
         
         dgs.getPhenome(sex_rev_male_genome,
                 new AsyncCallback<Map<String, String>>() {
                     public void onSuccess(Map<String, String> phenome) {
                         GWT.log("Have Sex Rev Male Phenotype!");
                         GWT.log(phenome.toString());
                         setSexRevPhenome(phenome);
                     }
 
                     public void onFailure(Throwable caught) {
                         caught.printStackTrace();
                         GWT.log(caught.getMessage());
                     }
                 });
         
         dgs.getPhenome(db_female_genome,
                 new AsyncCallback<Map<String, String>>() {
                     public void onSuccess(Map<String, String> phenome) {
                         GWT.log("Have Diabetic Female Phenotype!");
                         GWT.log(phenome.toString());
                         setDiabeticPhenome(phenome);
                     }
 
                     public void onFailure(Throwable caught) {
                         caught.printStackTrace();
                         GWT.log(caught.getMessage());
                     }
                 });
         
         Folder[] folders = new Folder[] {
                 new Folder("Females", new Drake[] { new Drake("P1",
                         female_genome, femalePhenome,
                         f_small_example, f_large_example), 
                         new Drake("BOB", sex_rev_male_genome, sexRevPhenome,
                                 b_small_example, b_large_example),
                         new Drake("DB", db_female_genome, diabeticPhenome,
                                         d_small_example, d_large_example),}),
                 new Folder("Males", new Drake[] { new Drake("P2",
                         male_genome, malePhenome,
                         m_small_example, m_large_example), }) };
 
         Folder root = new Folder("root");
         for (int i = 0; i < folders.length; i++) {
             root.add((Folder) folders[i]);
         }
 
         return root;
     }
     
 }
