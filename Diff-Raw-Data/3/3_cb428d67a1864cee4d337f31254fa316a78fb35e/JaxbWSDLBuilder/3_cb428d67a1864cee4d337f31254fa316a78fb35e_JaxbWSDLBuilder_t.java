 package org.codehaus.xfire.jaxb2;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.wsdl.Types;
 import javax.wsdl.WSDLException;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.SchemaOutputResolver;
 import javax.xml.transform.Result;
 import javax.xml.transform.dom.DOMResult;
 
 import org.codehaus.xfire.XFireRuntimeException;
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.service.binding.ObjectServiceFactory;
 import org.codehaus.xfire.transport.TransportManager;
 import org.codehaus.xfire.wsdl11.builder.WSDLBuilder;
 import org.jdom.Content;
 import org.jdom.Element;
 import org.jdom.input.DOMBuilder;
 import org.w3c.dom.Document;
 
 public class JaxbWSDLBuilder
     extends WSDLBuilder
 {
     private Set<Class> classes = new HashSet<Class>();
     private Set<JaxbType> types = new HashSet<JaxbType>();
     private Set<String> namespaces = new HashSet<String>();
     
     public JaxbWSDLBuilder(Service service, TransportManager transportManager) throws WSDLException
     {
         super(service, transportManager);
     }
 
     public void addDependency(org.codehaus.xfire.wsdl.SchemaType type)
     {
         if (!hasDependency(type))
         {
             if (type instanceof JaxbType)
             {
                 JaxbType jaxbType = (JaxbType) type;
 
                 if (types.contains(jaxbType)) return;
                 
                 classes.add(jaxbType.getActualTypeClass());
                 namespaces.add(jaxbType.getSchemaType().getNamespaceURI());
                 types.add(jaxbType);
             } 
             else 
             {
             	super.addDependency(type);
             }
         }
         else
         {
             super.addDependency(type);
         }
     }
     
     
     @Override
     protected void writeComplexTypes()
     {
         // Check to see if the user supplied schemas. If so, don't generate them.
         if (getService().getProperty(ObjectServiceFactory.SCHEMAS) == null)
         {
             generateJaxbSchemas();
         }
         
         super.writeComplexTypes();
     }
 
     private void generateJaxbSchemas()
     {
         try
         {
             JAXBContext context = JAXBContext.newInstance(classes.toArray(new Class[0]));
             final List<DOMResult> results = new ArrayList<DOMResult>();
        
             context.generateSchema(new SchemaOutputResolver() {
                 @Override
                 public Result createOutput(String ns, String file)
                     throws IOException
                 {
                     DOMResult result = new DOMResult();
                     result.setSystemId(file);
                     
                     results.add(result);
                     
                     return result;
                 }
             });
         
             Types types = getDefinition().getTypes();
             if (types == null)
             {
                 types = getDefinition().createTypes();
                 getDefinition().setTypes(types);
             }
             
             DOMBuilder domBuilder = new DOMBuilder();
             
             for (DOMResult result : results)
             {
                 Element schema = domBuilder.build(((Document)result.getNode()).getDocumentElement());
                 
                 // JAXB doesn't accept a Result with a null SystemId, which
                 // means it generats import statements for us. However, they're
                 // worthless imports as they contain the schemaLocation. So
                 // lets remove it.
                 removeImports(schema);
                 
                 String namespace = schema.getAttributeValue("targetNamespace");
                if (namespace == null) {
                    namespace = "";
                }
                 Element previousSchema = createSchemaType(namespace);
                 while (schema.getContentSize() > 0) {
                     Content child = schema.removeContent(0);
                     previousSchema.addContent(child);
                 }
             }
         }
         catch (JAXBException e)
         {
             throw new XFireRuntimeException("Couldn't generate a schema for the JAXB objects!", e);
         }
         catch (IOException e)
         {
             throw new XFireRuntimeException("Couldn't generate a schema for the JAXB objects!", e);
         }
     }
     
 }
