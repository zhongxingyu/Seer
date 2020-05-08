 package org.codehaus.xfire.picocontainer;
 
 import java.net.URL;
 import java.util.Map;
 
import javax.xml.namespace.QName;

 import org.codehaus.xfire.picocontainer.util.PicoObjectInvoker;
 import org.codehaus.xfire.service.Service;
 import org.codehaus.xfire.service.ServiceFactory;
 import org.codehaus.xfire.service.binding.BindingProvider;
 import org.codehaus.xfire.service.binding.ObjectServiceFactory;
 import org.codehaus.xfire.transport.TransportManager;
 import org.picocontainer.PicoContainer;
 import org.picocontainer.defaults.ObjectReference;
 import org.picocontainer.defaults.SimpleReference;
 
 /**
  * PicoContainer specialization of Java objects-specific implementation of the
  * {@link ServiceFactory} interface which uses a given picocontainer to pick
  * service objects instance when it's about to invoke them.
  * 
  * @author <a href="mailto:juze@intelli.biz">Jose Peleteiro</a>
  */
 public class PicoObjectServiceFactory
     extends ObjectServiceFactory
 {
     private ObjectReference picoReference;
 
     public PicoObjectServiceFactory(PicoContainer pico)
     {
         super();
         picoReference = new SimpleReference();
         picoReference.set(pico);
     }
 
     public PicoObjectServiceFactory(ObjectReference picoReference)
     {
         super();
         this.picoReference = picoReference;
     }
 
     public PicoObjectServiceFactory(PicoContainer pico, TransportManager transportManager,
             BindingProvider provider)
     {
         super(transportManager, provider);
         picoReference = new SimpleReference();
         picoReference.set(pico);
     }
 
     public PicoObjectServiceFactory(ObjectReference picoReference,
             TransportManager transportManager, BindingProvider provider)
     {
         super(transportManager, provider);
         this.picoReference = picoReference;
     }
 
     public Service create(Class clazz, String name, String namespace, Map properties)
     {
         return prepare(super.create(clazz, name, namespace, properties));
     }
 

    public Service create(Class clazz, Map properties)
    {
        return prepare(super.create(clazz, properties));
    }

    public Service create(Class clazz, QName name, URL wsdlUrl, Map properties)
     {
        return prepare(super.create(clazz, name, wsdlUrl, properties));
     }
 
     public Service create(Class clazz)
     {
         return prepare(super.create(clazz));
     }
 
     /**
      * Prepare an ObjectService to use pico.
      */
     protected Service prepare(Service endpoint)
     {
         endpoint.setInvoker(new PicoObjectInvoker(picoReference,
                 endpoint.getServiceInfo().getServiceClass()));
         return endpoint;
     }
 }
