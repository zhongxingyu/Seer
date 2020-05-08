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
 package com.github.fhirschmann.clozegen.lib.register;
 
 import com.google.common.base.Objects;
 import com.google.common.base.Objects.ToStringHelper;
 import com.google.common.base.Predicate;
 import com.google.common.collect.*;
 import java.util.Collection;
 import java.util.Map;
 
 /**
  * A register of all available Gap Annotator Descriptions.
  *
  * @author Fabian Hirschmann <fabian@hirschm.net>
  */
 public class Register extends ForwardingCollection<RegisterEntry> {
 
    /** The map identifier → Entry. */
     private Map<String, RegisterEntry> register;
 
     /**
      * Creates a new empty Register.
      */
     public Register() {
         register = HashBiMap.create();
     }
 
     /**
      * Adds the specified {@link RegisterEntry} to the register.
      *
      * @param entry the {@link RegisterEntry} to add.
      * @return whether this collection changed
      */
     @Override
     public boolean add(final RegisterEntry entry) {
         boolean changed = !register.containsKey(entry.getIdentifier());
         register.put(entry.getIdentifier(), entry);
         return changed;
     }
 
     /**
      * Returns the {@link RegisterEntry} identified by <code>identifier</code>
      * or <code>null</code> if it doesn't exist.
      *
      * @param identifier the identifier of the entry in question
      * @return the entry identified by a given <code>identifier</code>
      */
     public RegisterEntry get(final String identifier) {
         return register.get(identifier);
     }
 
     /**
      * Returns a live view of the set of Descriptions which support
      * the given <code>language</code>.
      *
      * @param language the language(code)
      * @return live view of filtered descriptions
      */
     public Collection<RegisterEntry> getAnnotatorsForLanguage(final String language) {
         return Collections2.filter(register.values(), new Predicate<RegisterEntry>() {
             @Override
             public boolean apply(final RegisterEntry input) {
                 return input.getSupportedLanguages().contains(language);
             }
         });
     }
 
     @Override
     public void clear() {
         register.clear();
     }
 
     @Override
     protected Collection<RegisterEntry> delegate() {
         return register.values();
     }
 
     @Override
     public String toString() {
         final ToStringHelper str = Objects.toStringHelper(this);
         str.add("entries", register.values().toString());
         return str.toString();
     }
 }
