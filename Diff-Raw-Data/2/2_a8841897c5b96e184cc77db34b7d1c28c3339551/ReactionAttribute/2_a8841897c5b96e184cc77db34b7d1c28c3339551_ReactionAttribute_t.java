 /*
  * Copyright (C) 2012  John May and Pablo Moreno
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 
 package uk.ac.ebi.mdk.io.text.biocyc.attribute;
 
 /**
  * Attributes for BioCyc reactions.dat file
  * @author John May
  * @link http://bioinformatics.ai.sri.com/ptools/flatfile-format.html#compounds.dat
  */
 public enum ReactionAttribute implements Attribute {
     UNIQUE_ID,
     TYPES,
     COMMON_NAME,
     CITATIONS,
     COMMENT,
     DELTAG0,
     EC_NUMBER,
     ENZYMATIC_REACTION,
     IN_PATHWAY,
     LEFT,
    OFFICIAL_EC("OFFICIAL-EC?", "OFFICIAL-EC\\?"),
     ORPHAN("ORPHAN?", "ORPHAN\\?"),
     RIGHT,
     SIGNAL,
     SPECIES,
     SPONTANEOUS("SPONTANEOUS?", "SPONTANEOUS\\?"),
     SYNONYMS;
 
     private String name;
     private String pattern;
 
     ReactionAttribute() {
         this.name = name().replaceAll("_", "-");
         this.pattern = name;
     }
 
     ReactionAttribute(String name) {
         this(name, name);
     }
 
     ReactionAttribute(String name, String pattern) {
         this.name = name;
         this.pattern = pattern;
     }
 
 
     @Override
     public String getName() {
         return name;
     }
 
     @Override
     public String getPattern() {
         return pattern;
     }
 }
