 package pl.psnc.dl.wf4ever.portal.model;
 
 import java.net.URI;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.apache.wicket.model.PropertyModel;
 import org.purl.wf4ever.rosrs.client.Annotable;
 import org.purl.wf4ever.rosrs.client.AnnotationTriple;
 import org.purl.wf4ever.rosrs.client.exception.ROException;
 import org.purl.wf4ever.rosrs.client.exception.ROSRSException;
 
 import com.hp.hpl.jena.rdf.model.Property;
 
 /**
  * A model that is based on a quad (annotation + a triple inside its body). It acts as a model of string, which is the
  * object of the triple. When the model object is loaded, it searches for a value in the annotations. When the value is
  * updated, this model updates the annotations as well.
  * 
  * @author piotrekhol
  * 
  */
 public class AnnotationTripleModel implements IModel<AnnotationTriple> {
 
     /** id. */
     private static final long serialVersionUID = -3397270930648327359L;
     /** Logger. */
     private static final Logger LOG = Logger.getLogger(AnnotationTripleModel.class);
 
     /** The triple. */
     private AnnotationTriple triple;
 
     /** Model for updating only the triple value. */
     private final ValueModel valueModel = new ValueModel();
 
     /** The model holding the object being annotated. */
     private IModel<? extends Annotable> annotableModel;
 
 
     /**
      * Constructor.
      * 
      * @param triple
      *            annotation triple
      */
     public AnnotationTripleModel(AnnotationTriple triple) {
         this.annotableModel = new Model<>(triple.getSubject());
         this.triple = triple;
     }
 
 
     /**
      * Constructor.
      * 
      * @param annotableModel
      *            the annotated resource
      * @param property
      *            the immutable property
      * @param anyExisting
      *            should any existing value be searched
      */
     public AnnotationTripleModel(IModel<? extends Annotable> annotableModel, URI property, boolean anyExisting) {
         this.annotableModel = annotableModel;
         this.triple = new AnnotationTriple(null, annotableModel.getObject(), property, null, anyExisting);
     }
 
 
     /**
      * Constructor.
      * 
      * @param annotableModel
      *            the annotated resource
      * @param property
      *            the immutable property
      * @param anyExisting
      *            should any existing value be searched
      */
     public AnnotationTripleModel(IModel<? extends Annotable> annotableModel, Property property, boolean anyExisting) {
         this(annotableModel, URI.create(property.getURI()), anyExisting);
     }
 
 
     public IModel<? extends Annotable> getAnnotableModel() {
         return new PropertyModel<>(triple, "subject");
     }
 
 
     @Override
     public void detach() {
     }
 
 
     @Override
     public AnnotationTriple getObject() {
         checkAnnotableModel();
         return triple;
     }
 
 
     /**
      * Check if the annotable model object has not changed.
      */
     private void checkAnnotableModel() {
         if (triple.getSubject() == null || !triple.getSubject().equals(annotableModel.getObject())) {
             triple = new AnnotationTriple(triple.getAnnotation(), annotableModel.getObject(), triple.getProperty(),
                    null, triple.isMerge());
         }
     }
 
 
     /**
      * This method does nothing, you better use setPropertyAndValue.
      * 
      * @param newTriple
      *            ignored
      */
     @Override
     public void setObject(AnnotationTriple newTriple) {
     }
 
 
     /**
      * Update the property and value of the triple, making one update or delete request, as necessary.
      * 
      * @param property
      *            the new property
      * @param value
      *            the new value
      */
     public void setPropertyAndValue(URI property, String value) {
         checkAnnotableModel();
         if (property == null || value == null) {
             delete();
         } else {
             if (triple.getAnnotation() != null) {
                 try {
                     triple.updatePropertyValue(property, value);
                 } catch (ROSRSException e) {
                     LOG.error("Can't update annotation " + triple.getAnnotation(), e);
                 }
             } else {
                 try {
                     triple = triple.getSubject().createPropertyValue(property, value);
                 } catch (ROSRSException | ROException e) {
                     LOG.error("Can't create annotation " + triple.getAnnotation(), e);
                 }
             }
         }
     }
 
 
     /**
      * Delete this annotation triple.
      */
     public void delete() {
         checkAnnotableModel();
         if (triple.getAnnotation() != null) {
             try {
                 triple.delete();
             } catch (ROSRSException e) {
                 LOG.error("Can't delete/update annotation " + triple.getAnnotation(), e);
             }
         }
     }
 
 
     public ValueModel getValueModel() {
         return valueModel;
     }
 
 
     /**
      * A model for retrieving and updating only the triple value. Useful for simple components that allow to edit only
      * the value.
      * 
      * @author piotrekhol
      * 
      */
     class ValueModel implements IModel<String> {
 
         /** id. */
         private static final long serialVersionUID = -7572730556806614174L;
 
 
         @Override
         public void detach() {
         }
 
 
         @Override
         public String getObject() {
             checkAnnotableModel();
             if (triple.getValue() == null && triple.isMerge()) {
                 List<AnnotationTriple> triples = triple.getSubject().getPropertyValues(triple.getProperty(), true);
                 if (!triples.isEmpty()) {
                     triple = new AnnotationTriple(triples.get(0).getAnnotation(), triple.getSubject(),
                             triple.getProperty(), triples.get(0).getValue(), true);
                 }
             }
             return triple.getValue();
         }
 
 
         @Override
         public void setObject(String object) {
             checkAnnotableModel();
             if (triple.getAnnotation() == null) {
                 throw new IllegalStateException("Annotable object cannot be null to set a value");
             }
             if (object != null) {
                 if (!object.equals(triple.getValue())) {
                     if (triple.getAnnotation() == null) {
                         try {
                             triple = triple.getSubject().createPropertyValue(triple.getProperty(), object);
                         } catch (ROSRSException | ROException e) {
                             LOG.error("Can't create an annotation", e);
                         }
                     } else {
                         try {
                             triple.updateValue(object);
                         } catch (ROSRSException e) {
                             LOG.error("Can't update annotation " + triple.getAnnotation(), e);
                         }
                     }
                 }
             } else if (triple.getAnnotation() != null) {
                 try {
                     triple.delete();
                 } catch (ROSRSException e) {
                     LOG.error("Can't delete/update annotation " + triple.getValue(), e);
                 }
             }
         }
     }
 
 }
