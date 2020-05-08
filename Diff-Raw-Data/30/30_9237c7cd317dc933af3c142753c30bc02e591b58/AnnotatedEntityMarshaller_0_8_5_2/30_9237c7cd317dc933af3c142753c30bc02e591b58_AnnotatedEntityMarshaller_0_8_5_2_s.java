 /**
  * AnnotatedEntityMarshal_0_8_5_0.java
  *
  * 2012.01.31
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
 package uk.ac.ebi.io.core.marshal.versions;
 
 import com.google.common.collect.ListMultimap;
 import java.io.IOException;
 import org.apache.log4j.Logger;
 import uk.ac.ebi.annotation.util.AnnotationFactory;
 import uk.ac.ebi.caf.utility.version.Version;
 import uk.ac.ebi.core.StarRating;
 import uk.ac.ebi.interfaces.AnnotatedEntity;
 import uk.ac.ebi.interfaces.Annotation;
 import uk.ac.ebi.interfaces.Rating;
 import uk.ac.ebi.interfaces.io.ReconstructionInputStream;
 import uk.ac.ebi.interfaces.io.ReconstructionOutputStream;
 import uk.ac.ebi.interfaces.io.marshal.EntityMarshaller;
 import uk.ac.ebi.io.core.marshal.AbstractAnnotatedEntityMarshaller;
 
 
 /**
  *
  *          AnnotatedEntityMarshal_0_8_5_0 2012.01.31
  * @version $Rev$ : Last Changed $Date$
  * @author  johnmay
  * @author  $Author$ (this version)
  *
  *          Class description
  *
  */
 public class AnnotatedEntityMarshaller_0_8_5_2 extends AbstractAnnotatedEntityMarshaller {
 
     private static final Logger LOGGER = Logger.getLogger(AnnotatedEntityMarshaller_0_8_5_2.class);
 
 
     public AnnotatedEntityMarshaller_0_8_5_2() {
         super(new Version(0, 8, 5, 2));
     }
 
 
     public EntityMarshaller newInstance() {
         return new AnnotatedEntityMarshaller_0_8_5_2();
     }
 
 
     @Override
     public AnnotatedEntity read(ReconstructionInputStream in) throws IOException, ClassNotFoundException {
 
         AnnotatedEntity entity = getSuperclassMarshal().read(in);
 
 
 //        observations = new ObservationCollection();
 //        observations.readExternal(in, this);
         entity.setRating(StarRating.getRating(in.readByte()));
 
         int i = in.readInt();
 
         AnnotationFactory annotationFactory = AnnotationFactory.getInstance();
         while (entity.getAnnotations().size() < i) {
             int n = in.readInt();
             Byte index = in.readByte();
             for (int j = 0; j < n; j++) {
                 entity.addAnnotation(annotationFactory.readExternal(index, in));
             }
         }
 
         return entity;
 
     }
 
 
     @Override
     public void write(ReconstructionOutputStream out, AnnotatedEntity entity) throws IOException {
 
         getSuperclassMarshal().write(out, entity);
 
         out.writeByte(entity.getRating().getScore());
 
         //out.writeObject(rating);
 
         System.err.println("Observations are not currently written!");
         //observations.writeExternal(out);
 
         ListMultimap<Byte, Annotation> annotations = entity.getAnnotationMap();
 
         // total number of annotations
         out.writeInt(annotations.values().size());
 
         // write by index
         for (Byte index : annotations.keySet()) {
             out.writeInt(annotations.get(index).size());
             out.writeByte(index);
             try {
                 for (Annotation annotation : annotations.get(index)) {
                     annotation.writeExternal(out);
                 }
             } catch (IOException ex) {
                 // XXX
                 throw new IOException(
                        "Could not save, annotation on entity " + this + " : " + ex.getMessage() + " caused an error");
             }
         }
     }
 }
