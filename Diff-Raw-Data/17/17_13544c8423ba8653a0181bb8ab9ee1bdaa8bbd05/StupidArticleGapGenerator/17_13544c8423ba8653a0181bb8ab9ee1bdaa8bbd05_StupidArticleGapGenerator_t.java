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
 package com.github.fhirschmann.clozegen.lib.generators;
 
 import com.github.fhirschmann.clozegen.lib.generators.api.Gap;
 import com.github.fhirschmann.clozegen.lib.generators.api.SingleTokenInputGapGenerator;
import com.google.common.base.Optional;
 
 
 /**
  * This is a sample implementation of a generator for gaps for articles. This
  * is only for demonstration purposes!
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public class StupidArticleGapGenerator implements SingleTokenInputGapGenerator {
     /** The valid answer for this gap. */
     private String validAnswer;
 
     @Override
     public void initialize(String token) {
         this.validAnswer = token;
     }
 
     @Override
    public Optional<Gap> generate(final int count) {
        return Optional.of(Gap.with(validAnswer, "a", "an", "the"));
     }
 }
