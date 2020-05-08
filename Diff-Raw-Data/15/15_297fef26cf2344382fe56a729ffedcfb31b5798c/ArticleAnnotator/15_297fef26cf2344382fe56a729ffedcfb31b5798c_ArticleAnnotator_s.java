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
 package com.github.fhirschmann.clozegen.lib.annotators;
 
import de.tudarmstadt.ukp.dkpro.core.api.lexmorph.type.pos.ART;
 import java.util.Arrays;
 import org.apache.uima.jcas.tcas.Annotation;
 
 /**
 * Annotates articles for use in cloze tests.
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public class ArticleAnnotator extends GapAnnotator {
    public final static String[] articles = new String[] {"a", "an", "the"};
 
     @Override
     public String[] getWordClasses() {
         return new String[] {"DT"};
     }
 
     @Override
     public Gap generate(final Annotation subject) {
        if (Arrays.asList(articles).contains(subject.getCoveredText())) {
            Gap gap = new Gap();
 
             gap.getValidAnswers().add(subject.getCoveredText());
             gap.getInvalidAnswers().add("a");
             gap.getInvalidAnswers().add("an");
             gap.getInvalidAnswers().add("the");
 
             return gap;
         }
         return null;
     }
 }
