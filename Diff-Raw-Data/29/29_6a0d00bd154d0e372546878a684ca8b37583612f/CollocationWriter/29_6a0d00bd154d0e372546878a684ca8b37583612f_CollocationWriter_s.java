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
 package com.github.fhirschmann.clozegen.lib.components;
 
 import com.github.fhirschmann.clozegen.lib.components.api.ConstraintBasedConsumer;
 import com.github.fhirschmann.clozegen.lib.multiset.WriteMultisets;
 import com.github.fhirschmann.clozegen.lib.util.MiscUtils;
 import com.github.fhirschmann.clozegen.lib.util.UIMAUtils;
 import com.google.common.collect.LinkedHashMultiset;
 import com.google.common.collect.Multiset;
 import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.POS;
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import org.apache.uima.UimaContext;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.jcas.tcas.Annotation;
 import org.apache.uima.resource.ResourceInitializationException;
 import org.apache.uima.util.Level;
 import org.uimafit.descriptor.ConfigurationParameter;
 
 /**
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public class CollocationWriter extends ConstraintBasedConsumer {
     /** The path to the collocation file. */
     public static final String PARAM_OUTPUT_PATH = "OutputPath";
     @ConfigurationParameter(name = PARAM_OUTPUT_PATH, mandatory = true)
     private String path;
 
     /** The n in n-gram. */
     public static final String PARAM_N = "N";
     @ConfigurationParameter(name = PARAM_N, mandatory = false, defaultValue = "3")
     private int n;
 
 	public static final String PARAM_MIN_FREQUENCY = "minFrequency";
 	@ConfigurationParameter(name = PARAM_MIN_FREQUENCY, mandatory = false, defaultValue = "1")
 	private int minFrequency;
 
     /** The multiset we will write to a file. */
     private Multiset<String> ms;
 
     @Override
     public void initialize(final UimaContext context)
             throws ResourceInitializationException {
         super.initialize(context);
         ms = LinkedHashMultiset.create();
     }
 
     @Override
     public void process(final JCas jcas, final List<Annotation> annotationList,
             final int index) {
         List<String> tokens = UIMAUtils.getAdjacentTokens(POS.class,
                 annotationList, index, n);
         ms.add(MiscUtils.WS_JOINER.join(tokens));
     }
 
     @Override
     public void collectionProcessComplete() {
         try {
            WriteMultisets.writeMultiSet(ms, minFrequency, new File(path));
         } catch (IOException ex) {
             getContext().getLogger().log(Level.SEVERE, ex.getMessage());
         }
     }
 }
