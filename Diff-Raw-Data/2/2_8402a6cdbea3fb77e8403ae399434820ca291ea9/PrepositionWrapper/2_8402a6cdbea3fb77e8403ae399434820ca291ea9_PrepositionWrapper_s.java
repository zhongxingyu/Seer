 /*
  * Copyright (C) 2012 Fabian Hirschmann <fabian@hirschm.net>
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package com.github.fhirschmann.clozegen.lib.annotators.en;
 
 import com.github.fhirschmann.clozegen.lib.annotators.Wrapper;
 import com.github.fhirschmann.clozegen.lib.functions.CoveredTextFunction;
 import com.github.fhirschmann.clozegen.lib.generator.GapGenerator;
 import com.github.fhirschmann.clozegen.lib.generator.en.PrepositionGapGenerator;
 import com.github.fhirschmann.clozegen.lib.generator.en.PrepositionGapGeneratorModel;
 import com.github.fhirschmann.clozegen.lib.util.CollectionUtils;
 import com.github.fhirschmann.clozegen.lib.util.UIMAUtils;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.io.Resources;
 import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
 import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.PP;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.apache.uima.cas.ConstraintFactory;
 import org.apache.uima.cas.FSMatchConstraint;
 import org.apache.uima.cas.FSTypeConstraint;
 import org.apache.uima.jcas.tcas.Annotation;
 import org.apache.uima.resource.ResourceInitializationException;
 import org.apache.uima.resource.ResourceSpecifier;
 import org.uimafit.component.Resource_ImplBase;
 import org.uimafit.descriptor.ConfigurationParameter;
 
 /**
  * This annotator creates annotations for prepositions.
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public class PrepositionWrapper extends Resource_ImplBase implements Wrapper {
     /** The path to the preposition collocations. */
     public static final String PARAM_PATH = "Path";
     @ConfigurationParameter(name = PARAM_PATH, mandatory = true)
     private String path;
 
     private PrepositionGapGeneratorModel model;
 
     @Override
    public boolean initialize(ResourceSpecifier aSpecifier, Map aAdditionalParams)
             throws ResourceInitializationException {
         if (!super.initialize(aSpecifier, aAdditionalParams)) {
             return false;
         }
 
         model = new PrepositionGapGeneratorModel();
         try {
             model.load(Resources.getResource(path + "/trigrams.txt"),
                     Resources.getResource(path + "/after.txt"),
                     Resources.getResource(path + "/before.txt"));
             return true;
         } catch (IOException ex) {
             Logger.getLogger(PrepositionWrapper.class.getName()).
                     log(Level.SEVERE, null, ex);
             return false;
         }
     }
 
     @Override
     public FSMatchConstraint getConstraint() {
         FSTypeConstraint cons = ConstraintFactory.instance().createTypeConstraint();
         cons.add(PP.class.getName());
         return cons;
     }
 
     @Override
     public GapGenerator generator(
             final List<Annotation> annotationList, final int offset) {
         List<String> tokens = UIMAUtils.getAdjacentTokens(POS.class,
                 annotationList, offset, 1);
 
         return PrepositionGapGenerator.create(
                 tokens.get(0), tokens.get(1), tokens.get(2), model);
     }
 }
