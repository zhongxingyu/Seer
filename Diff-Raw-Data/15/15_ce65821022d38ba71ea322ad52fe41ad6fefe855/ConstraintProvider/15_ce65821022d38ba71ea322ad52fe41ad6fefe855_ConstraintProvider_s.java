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
 package com.github.fhirschmann.clozegen.lib.constraints.api;
 
 import org.apache.uima.cas.FSMatchConstraint;
 import org.apache.uima.jcas.JCas;
 
 /**
  * Implementing classes will provide a constraint.
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public interface ConstraintProvider {
     /**
     * A constraint, possibly <code>null</code>, which specifies on what occasion
     * {@link Adapter#generator(java.util.List, int)} should be called. If this
     * returns <code>null</code>, {@link Adapter#generator(java.util.List, int)} will
     * be called for each Annotation.
      *
      * <p>For example, if you want to only work on annotations of the type
      * {@link ART}, then this method should return <code>cons</code> like so:
      * <p><blockquote><pre>
      * {@code
      * FSTypeConstraint cons = ConstraintFactory.instance().createTypeConstraint();
      * cons.add(ART.class.getName());
      * }
      * </pre></blockquote>
      *
      * @param jcas the JCas
      * @return a new constraint
      */
     FSMatchConstraint getConstraint(JCas jcas);
 }
