 /**
  * IdentifierLoader.java
  *
  * 2011.09.15
  *
  * This file is part of the CheMet library
  *
  * The CheMet library is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * CheMet is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with CheMet.  If not, see <http://www.gnu.org/licenses/>.
  */
 package uk.ac.ebi.mdk.domain;
 
 import uk.ac.ebi.mdk.deprecated.MIR;
 import uk.ac.ebi.mdk.deprecated.MIRIAMEntry;
 import uk.ac.ebi.mdk.deprecated.MIRIAMLoader;
 import uk.ac.ebi.mdk.deprecated.Synonyms;
 import uk.ac.ebi.mdk.domain.identifier.AbstractIdentifier;
 import uk.ac.ebi.mdk.tool.MetaInfoLoader;
 
 import java.util.*;
 
 
 /**
  * IdentifierLoader – 2011.09.15 <br>
  * Class description
  *
  * @author johnmay
  * @author $Author$ (this version)
  * @version $Rev$ : Last Changed $Date$
  */
 public class IdentifierLoader
         extends DefaultLoader
         implements MetaInfoLoader {
 
     private static final MIRIAMLoader MIRIAM_LOADER = MIRIAMLoader.getInstance();
 
     private Map<Class, IdentifierMetaInfo> loaded = new HashMap<Class, IdentifierMetaInfo>(32);
 
     private IdentifierLoader() {
         super();
     }
 
 
     private static class IdentifierLoaderHolder {
         private static IdentifierLoader INSTANCE = new IdentifierLoader();
     }
 
 
     public static IdentifierLoader getInstance() {
         return IdentifierLoaderHolder.INSTANCE;
     }
 
 
     /**
      * Returns the MIRIAM MIR Identifier
      *
      * @param type
      *
      * @return
      */
     public int getMIR(Class<? extends AbstractIdentifier> type) {
 
         MIR miriam = type.getAnnotation(MIR.class);
 
         if (miriam != null) {
             return miriam.value();
         }
 
         return 0; // default entry
 
     }
 
 
     /**
      * Returns the miriam entry for this identifier class
      *
      * @param type
      *
      * @return
      */
     public MIRIAMEntry getEntry(Class type) {
         int mir = getMIR(type);
         return MIRIAM_LOADER.getEntry(mir);
     }
 
 
     /**
      * @inheritDoc
      */
     @Override
     public String getShortDescription(Class c) {
 
         int mir = getMIR(c);
 
         if (mir != 0) {
             MIRIAMEntry entry = MIRIAM_LOADER.getEntry(mir);
             return entry.getName();
         }
 
        return super.getLongDescription(c);
 
     }
 
 
     /**
      * @inheritDoc
      */
     @Override
     public String getLongDescription(Class c) {
 
         int mir = getMIR(c);
 
         if (mir != 0) {
             MIRIAMEntry entry = MIRIAM_LOADER.getEntry(mir);
             return entry.getDescription();
         }
 
         return super.getLongDescription(c);
 
     }
 
 
     /**
      * Access the synonyms for this identifier
      *
      * @param type
      *
      * @return
      */
     public Collection<String> getDatabaseSynonyms(Class type) {
 
         int mir = getMIR(type);
 
         Set<String> synonyms = new HashSet();
         if (mir != 0) {
             synonyms.addAll(MIRIAM_LOADER.getEntry(mir).getSynonyms());
         }
 
         Synonyms annotation = (Synonyms) type.getAnnotation(Synonyms.class);
         if (annotation != null) {
             synonyms.addAll(Arrays.asList(annotation.value()));
         }
 
         return synonyms;
     }
 
 
     public IdentifierMetaInfo load(Class c) {
         IdentifierMetaInfo metaInfo = getMetaInfo(c);
         loaded.put(c, metaInfo);
         return metaInfo;
     }
 
     private IdentifierMetaInfo loadMetaInfo(Class c) {
         IdentifierMetaInfo metaInfo = new IdentifierMetaInfo(super.getMetaInfo(c),
                                                              getEntry(c),
                                                              getDatabaseSynonyms(c));
         loaded.put(c, metaInfo);
         return metaInfo;
     }
 
     @Override
     public IdentifierMetaInfo getMetaInfo(Class c) {
         return loaded.containsKey(c) ? loaded.get(c) : loadMetaInfo(c);
     }
 
 
 }
