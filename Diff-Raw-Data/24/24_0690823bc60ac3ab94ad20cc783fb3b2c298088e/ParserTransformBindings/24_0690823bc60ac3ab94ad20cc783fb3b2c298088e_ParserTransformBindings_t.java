 package cz.cuni.mff.peckam.java.origamist.jaxb;
 
 import java.util.List;
 
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.bind.util.JAXBResult;
 import javax.xml.parsers.SAXParser;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.sax.SAXResult;
 
 import org.xml.sax.ContentHandler;
 import org.xml.sax.SAXException;
 
 import cz.cuni.mff.peckam.java.origamist.jaxb.AdditionalTransforms.AdditionalTransform;
 
 /**
  * An implementation of Bindings that supports use of one or more XML transforms to convert an input XML document to
  * some other XML schema before unmarshalling.
  * <p>
  * 
  * This implementation uses a series of TransformerHandler classes to achieve the series of XML transforms; this was
  * found to be a more straightforward and "accessible" (i.e., less hidden resetting of error handlers, entity resolvers,
  * etc) than using XMLFilters as recommended by the Java Web Services tutorial.
  * <p>
  * 
  * This package is based on a XML Schema versioning system from http://www.funkypeople.biz/knowledge/JavaXml-v2.zip .
  * 
  * @author Sean Barnett
  * @author Martin Pecka
  * 
  * @param T The type of the unmarhalled object.
  */
 public class ParserTransformBindings<T> implements Bindings<T>
 {
 
     /** The definition of the transform(s) to use to get from the XML schema to the Java bindings schema. */
     protected TransformInfo                 transform;
 
     /** The unmarshaller used during the unmarshalling process. */
     protected Unmarshaller                  unmarshaller;
 
     /** This element will receive the final unmarshalled classes. */
     protected JAXBResult                    unmarshallerResult;
 
     /** The parser that parses the XML file. */
     protected SAXParser                     unmarshalParser;
 
     /** The array of content handlers joined in a line to convert the XML file. */
     protected SAXOutputtingContentHandler[] contentHandlers;
 
     /** The {@link BindingsManager} that controls this binding. */
     protected final BindingsManager         bindingsManager;
 
     /**
      * Simple constructor.
      * 
      * @param bindingsManager A reference to the bindings manager.
      * @param transform The definition of the transform(s) to use to get from the XML schema to the Java bindings
      *            schema.
      * @param start List of transforms to be added before the classic ones.
      * @param beforeUnmarshaller List of transforms to be added before the unmarshaller.
      * @param end List of transforms to be added after the unmarshaller.
      * 
      * @throws JAXBException
      */
     public ParserTransformBindings(BindingsManager bindingsManager, TransformInfo transform,
             List<AdditionalTransform> start, List<AdditionalTransform> beforeUnmarshaller, List<AdditionalTransform> end)
             throws JAXBException
     {
         this.bindingsManager = bindingsManager;
         this.transform = transform;
 
         // create unmarshaller - not validating as a validating parser will be used
         // to do validation of raw XML
         unmarshaller = bindingsManager.createUnmarshaller(transform.getToSchema());
 
         // last transformer handler writes to JAXB result
         unmarshallerResult = new JAXBResult(unmarshaller);
 
         try {
             // create the unmarshal transforms, and couple each to the next in sequence
             createHandlers(transform, start, beforeUnmarshaller, end);
 
             // index of the handler after which the unmarshalling should proceed
             int lastBeforeUnmarshallerIndex = start.size() + transform.getDepth() + beforeUnmarshaller.size() - 1;
 
             // set last unmarshal transform to point to JAXBResult
             contentHandlers[lastBeforeUnmarshallerIndex].setResult(unmarshallerResult);
 
             // set the remaining "tail" to be processed by the unmarshaller result
             if (end.size() > 0)
                 unmarshallerResult.setHandler(contentHandlers[lastBeforeUnmarshallerIndex + 1]);
         } catch (TransformerConfigurationException e) {
             throw new JAXBException(e.getMessage(), (e.getException() == null) ? e : e.getException());
         } catch (SAXException e) {
             throw new JAXBException(e.getMessage(), (e.getException() == null) ? e : e.getException());
         }
     }
 
     @Override
     public ContentHandler getContentHandler()
     {
         return contentHandlers[0];
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public T getResult() throws JAXBException, IllegalStateException, ClassCastException
     {
         Object result = unmarshallerResult.getResult();
 
         // the result can be wrapped inside a JAXBElement if the schema doesn't define a root element
         if (result instanceof JAXBElement<?>)
             result = ((JAXBElement<?>) result).getValue();
 
         return (T) result;
     }
 
     /**
      * Create the series of unmarshal transforms, and point each to the next in sequence as its target content handler.
      * 
      * @param transform The definition of the last transform in sequence.
      * @param start List of transforms to be added before the classic ones.
      * @param beforeUnmarshaller List of transforms to be added before the unmarshaller.
      * @param end List of transforms to be added after the unmarshaller.
      * 
      * @throws SAXException
      * @throws TransformerConfigurationException
      */
     protected void createHandlers(TransformInfo transform, List<AdditionalTransform> start,
             List<AdditionalTransform> beforeUnmarshaller, List<AdditionalTransform> end) throws SAXException,
             TransformerConfigurationException
     {
         contentHandlers = new SAXOutputtingContentHandler[start.size() + transform.getDepth()
                 + beforeUnmarshaller.size() + end.size()];
 
        int i = -1;
 
         for (AdditionalTransform tr : start) {
            i++;
             if (tr.getTransform().getTemplates() != null) {
                 contentHandlers[i] = bindingsManager.getTransformerFactory().newTransformerHandler(
                         tr.getTransform().getTemplates());
             } else {
                 contentHandlers[i] = tr.getTransform().getContentHandler();
             }
         }
 
         for (TransformInfo tr = transform; tr != null; tr = tr.getParent()) {
             int trDepth = tr.getDepth();
             if (tr.getTemplates() != null) {
                 contentHandlers[i + trDepth] = bindingsManager.getTransformerFactory().newTransformerHandler(
                         tr.getTemplates());
             } else {
                 contentHandlers[i + trDepth] = tr.getContentHandler();
             }
         }
         i += transform.getDepth();
 
         for (AdditionalTransform tr : beforeUnmarshaller) {
            i++;
             if (tr.getTransform().getTemplates() != null) {
                 contentHandlers[i] = bindingsManager.getTransformerFactory().newTransformerHandler(
                         tr.getTransform().getTemplates());
             } else {
                 contentHandlers[i] = tr.getTransform().getContentHandler();
             }
         }
 
         for (AdditionalTransform tr : end) {
            i++;
             if (tr.getTransform().getTemplates() != null) {
                 contentHandlers[i] = bindingsManager.getTransformerFactory().newTransformerHandler(
                         tr.getTransform().getTemplates());
             } else {
                 contentHandlers[i] = tr.getTransform().getContentHandler();
             }
         }
 
         // interconnect the handlers with setResult()
         for (i = 0; i < contentHandlers.length - 1; i++) {
             contentHandlers[i].setResult(new SAXResult(contentHandlers[i + 1]));
         }
     }
 }
