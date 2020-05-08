 /**
  * CotizacionVehiculoEBSBindingStub.java
  *
  * This file was auto-generated from WSDL
  * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
  */
 
 package ar.com.allianz.xmlns.Core.EBS.CotizacionVehiculo;
 
 import javax.xml.soap.SOAPException;
 
 import org.apache.axis.message.SOAPHeaderElement;
 
 public class CotizacionVehiculoEBSBindingStub extends org.apache.axis.client.Stub implements ar.com.allianz.xmlns.Core.EBS.CotizacionVehiculo.CotizacionVehiculoEBS {
     private java.util.Vector cachedSerClasses = new java.util.Vector();
     private java.util.Vector cachedSerQNames = new java.util.Vector();
     private java.util.Vector cachedSerFactories = new java.util.Vector();
     private java.util.Vector cachedDeserFactories = new java.util.Vector();
 
     static org.apache.axis.description.OperationDesc [] _operations;
 
     static {
         _operations = new org.apache.axis.description.OperationDesc[3];
         _initOperationDesc1();
     }
 
     private static void _initOperationDesc1(){
         org.apache.axis.description.OperationDesc oper;
         org.apache.axis.description.ParameterDesc param;
         oper = new org.apache.axis.description.OperationDesc();
         oper.setName("SolicitarContacto");
         param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "SolicitarContactoEBM"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "SolicitarContactoEBMType"), ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.SolicitarContactoEBMType.class, false, false);
         oper.addParameter(param);
         oper.setReturnType(new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "SolicitarContactoResponseEBMType"));
         oper.setReturnClass(ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.SolicitarContactoResponseEBMType.class);
         oper.setReturnQName(new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "SolicitarContactoResponseEBM"));
         oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
         oper.setUse(org.apache.axis.constants.Use.LITERAL);
         _operations[0] = oper;
 
         oper = new org.apache.axis.description.OperationDesc();
         oper.setName("CalcularCotizacionVehiculo");
         param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "CalcularCotizacionVehiculoEBM"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "CalcularCotizacionVehiculoEBMType"), ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.CalcularCotizacionVehiculoEBMType.class, false, false);
         oper.addParameter(param);
         oper.setReturnType(new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "CalcularCotizacionVehiculoResponseEBMType"));
         oper.setReturnClass(ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.CalcularCotizacionVehiculoResponseEBMType.class);
         oper.setReturnQName(new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "CalcularCotizacionVehiculoResponseEBM"));
         oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
         oper.setUse(org.apache.axis.constants.Use.LITERAL);
         _operations[1] = oper;
 
         oper = new org.apache.axis.description.OperationDesc();
         oper.setName("CalcularCotizacionFullVehiculo");
         param = new org.apache.axis.description.ParameterDesc(new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "CalcularCotizacionFullVehiculoEBM"), org.apache.axis.description.ParameterDesc.IN, new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "CalcularCotizacionFullVehiculoEBMType"), ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.CalcularCotizacionFullVehiculoEBMType.class, false, false);
         oper.addParameter(param);
         oper.setReturnType(new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "CalcularCotizacionFullVehiculoResponseEBMType"));
         oper.setReturnClass(ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.CalcularCotizacionFullVehiculoResponseEBMType.class);
         oper.setReturnQName(new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "CalcularCotizacionFullVehiculoResponseEBM"));
         oper.setStyle(org.apache.axis.constants.Style.DOCUMENT);
         oper.setUse(org.apache.axis.constants.Use.LITERAL);
         _operations[2] = oper;
 
     }
 
     public CotizacionVehiculoEBSBindingStub() throws org.apache.axis.AxisFault {
          this(null);
     }
 
     public CotizacionVehiculoEBSBindingStub(java.net.URL endpointURL, javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
          this(service);
          super.cachedEndpoint = endpointURL;
     }
 
     public CotizacionVehiculoEBSBindingStub(javax.xml.rpc.Service service) throws org.apache.axis.AxisFault {
         if (service == null) {
             super.service = new org.apache.axis.client.Service();
         } else {
             super.service = service;
         }
         ((org.apache.axis.client.Service)super.service).setTypeMappingVersion("1.2");
             java.lang.Class cls;
             javax.xml.namespace.QName qName;
             javax.xml.namespace.QName qName2;
             java.lang.Class beansf = org.apache.axis.encoding.ser.BeanSerializerFactory.class;
             java.lang.Class beandf = org.apache.axis.encoding.ser.BeanDeserializerFactory.class;
             java.lang.Class enumsf = org.apache.axis.encoding.ser.EnumSerializerFactory.class;
             java.lang.Class enumdf = org.apache.axis.encoding.ser.EnumDeserializerFactory.class;
             java.lang.Class arraysf = org.apache.axis.encoding.ser.ArraySerializerFactory.class;
             java.lang.Class arraydf = org.apache.axis.encoding.ser.ArrayDeserializerFactory.class;
             java.lang.Class simplesf = org.apache.axis.encoding.ser.SimpleSerializerFactory.class;
             java.lang.Class simpledf = org.apache.axis.encoding.ser.SimpleDeserializerFactory.class;
             java.lang.Class simplelistsf = org.apache.axis.encoding.ser.SimpleListSerializerFactory.class;
             java.lang.Class simplelistdf = org.apache.axis.encoding.ser.SimpleListDeserializerFactory.class;
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/CommonCore/EBM", ">QualityOfServiceType>RetryCount");
             cachedSerQNames.add(qName);
             cls = java.math.BigInteger.class;
             cachedSerClasses.add(cls);
             cachedSerFactories.add(org.apache.axis.encoding.ser.BaseSerializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleSerializerFactory.class, cls, qName));
             cachedDeserFactories.add(org.apache.axis.encoding.ser.BaseDeserializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleDeserializerFactory.class, cls, qName));
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/CommonCore/EBM", ">QualityOfServiceType>RetryInterval");
             cachedSerQNames.add(qName);
             cls = java.math.BigInteger.class;
             cachedSerClasses.add(cls);
             cachedSerFactories.add(org.apache.axis.encoding.ser.BaseSerializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleSerializerFactory.class, cls, qName));
             cachedDeserFactories.add(org.apache.axis.encoding.ser.BaseDeserializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleDeserializerFactory.class, cls, qName));
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/CommonCore/EBM", "EBMHeaderType");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.CommonCore.EBM.EBMHeaderType.class;
             cachedSerClasses.add(cls);
             cachedSerFactories.add(beansf);
             cachedDeserFactories.add(beandf);
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/CommonCore/EBM", "EBMType");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.CommonCore.EBM.EBMType.class;
             cachedSerClasses.add(cls);
             cachedSerFactories.add(beansf);
             cachedDeserFactories.add(beandf);
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/CommonCore/EBM", "ErrorDetailType");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.CommonCore.EBM.ErrorDetailType.class;
             cachedSerClasses.add(cls);
             cachedSerFactories.add(beansf);
             cachedDeserFactories.add(beandf);
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/CommonCore/EBM", "QualityOfServiceType");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.CommonCore.EBM.QualityOfServiceType.class;
             cachedSerClasses.add(cls);
             cachedSerFactories.add(beansf);
             cachedDeserFactories.add(beandf);
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/CommonCore/EBM", "ResponseEBMHeaderType");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.CommonCore.EBM.ResponseEBMHeaderType.class;
             cachedSerClasses.add(cls);
             cachedSerFactories.add(beansf);
             cachedDeserFactories.add(beandf);
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/CommonCore/EBM", "SenderType");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.CommonCore.EBM.SenderType.class;
             cachedSerClasses.add(cls);
             cachedSerFactories.add(beansf);
             cachedDeserFactories.add(beandf);
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "AccesorioVehiculoType");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.AccesorioVehiculoType.class;
             cachedSerClasses.add(cls);
             cachedSerFactories.add(beansf);
             cachedDeserFactories.add(beandf);
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "AdicionalType");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.AdicionalType.class;
             cachedSerClasses.add(cls);
             cachedSerFactories.add(beansf);
             cachedDeserFactories.add(beandf);
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "CalcularCotizacionFullVehiculoDataAreaType");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.CalcularCotizacionFullVehiculoDataAreaType.class;
             cachedSerClasses.add(cls);
             cachedSerFactories.add(beansf);
             cachedDeserFactories.add(beandf);
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "CalcularCotizacionFullVehiculoEBMType");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.CalcularCotizacionFullVehiculoEBMType.class;
             cachedSerClasses.add(cls);
             cachedSerFactories.add(beansf);
             cachedDeserFactories.add(beandf);
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "CalcularCotizacionFullVehiculoResponseDataAreaType");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.CalcularCotizacionFullVehiculoResponseDataAreaType.class;
             cachedSerClasses.add(cls);
             cachedSerFactories.add(beansf);
             cachedDeserFactories.add(beandf);
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "CalcularCotizacionFullVehiculoResponseEBMType");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.CalcularCotizacionFullVehiculoResponseEBMType.class;
             cachedSerClasses.add(cls);
             cachedSerFactories.add(beansf);
             cachedDeserFactories.add(beandf);
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "CalcularCotizacionFullVehiculoResponseType");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.CalcularCotizacionFullVehiculoResponseType.class;
             cachedSerClasses.add(cls);
             cachedSerFactories.add(beansf);
             cachedDeserFactories.add(beandf);
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "CalcularCotizacionFullVehiculoType");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.CalcularCotizacionFullVehiculoType.class;
             cachedSerClasses.add(cls);
             cachedSerFactories.add(beansf);
             cachedDeserFactories.add(beandf);
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "CalcularCotizacionVehiculoDataAreaType");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.CalcularCotizacionVehiculoDataAreaType.class;
             cachedSerClasses.add(cls);
             cachedSerFactories.add(beansf);
             cachedDeserFactories.add(beandf);
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "CalcularCotizacionVehiculoEBMType");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.CalcularCotizacionVehiculoEBMType.class;
             cachedSerClasses.add(cls);
             cachedSerFactories.add(beansf);
             cachedDeserFactories.add(beandf);
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "CalcularCotizacionVehiculoResponseDataAreaType");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.CalcularCotizacionVehiculoResponseDataAreaType.class;
             cachedSerClasses.add(cls);
             cachedSerFactories.add(beansf);
             cachedDeserFactories.add(beandf);
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "CalcularCotizacionVehiculoResponseEBMType");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.CalcularCotizacionVehiculoResponseEBMType.class;
             cachedSerClasses.add(cls);
             cachedSerFactories.add(beansf);
             cachedDeserFactories.add(beandf);
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "CalcularCotizacionVehiculoResponseType");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.CalcularCotizacionVehiculoResponseType.class;
             cachedSerClasses.add(cls);
             cachedSerFactories.add(beansf);
             cachedDeserFactories.add(beandf);
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "CalcularCotizacionVehiculoType");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.CalcularCotizacionVehiculoType.class;
             cachedSerClasses.add(cls);
             cachedSerFactories.add(beansf);
             cachedDeserFactories.add(beandf);
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "ConfiguracionType");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.ConfiguracionType.class;
             cachedSerClasses.add(cls);
             cachedSerFactories.add(beansf);
             cachedDeserFactories.add(beandf);
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "decimal-or-empty");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.DecimalOrEmpty.class;
             cachedSerClasses.add(cls);
             cachedSerFactories.add(org.apache.axis.encoding.ser.BaseSerializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleSerializerFactory.class, cls, qName));
             cachedDeserFactories.add(org.apache.axis.encoding.ser.BaseDeserializerFactory.createFactory(org.apache.axis.encoding.ser.SimpleDeserializerFactory.class, cls, qName));
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "empty-string");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.EmptyString.class;
             cachedSerClasses.add(cls);
             cachedSerFactories.add(enumsf);
             cachedDeserFactories.add(enumdf);
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "EsquemaComercialType");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.EsquemaComercialType.class;
             cachedSerClasses.add(cls);
             cachedSerFactories.add(beansf);
             cachedDeserFactories.add(beandf);
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "ListaAccesoriosType");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.AccesorioVehiculoType[].class;
             cachedSerClasses.add(cls);
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "AccesorioVehiculo");
             qName2 = null;
             cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
             cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "ListaAdicionalesType");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.AdicionalType[].class;
             cachedSerClasses.add(cls);
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "Adicional");
             qName2 = null;
             cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
             cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "ListaConfiguracionType");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.ConfiguracionType[].class;
             cachedSerClasses.add(cls);
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "Configuracion");
             qName2 = null;
             cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
             cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "ListaCotizacionFullType");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.Core.EBO.Allianz.CotizacionFullVehiculo.CotizacionFullType[].class;
             cachedSerClasses.add(cls);
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBO/Allianz/CotizacionFullVehiculo", "CotizacionFull");
             qName2 = null;
             cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
             cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "ListaEsquemasComercialesType");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.EsquemaComercialType[].class;
             cachedSerClasses.add(cls);
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "EsquemaComercial");
             qName2 = null;
             cachedSerFactories.add(new org.apache.axis.encoding.ser.ArraySerializerFactory(qName, qName2));
             cachedDeserFactories.add(new org.apache.axis.encoding.ser.ArrayDeserializerFactory());
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "ProductoType");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.ProductoType.class;
             cachedSerClasses.add(cls);
             cachedSerFactories.add(beansf);
             cachedDeserFactories.add(beandf);
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "SolicitarContactoDataAreaType");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.SolicitarContactoDataAreaType.class;
             cachedSerClasses.add(cls);
             cachedSerFactories.add(beansf);
             cachedDeserFactories.add(beandf);
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "SolicitarContactoEBMType");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.SolicitarContactoEBMType.class;
             cachedSerClasses.add(cls);
             cachedSerFactories.add(beansf);
             cachedDeserFactories.add(beandf);
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "SolicitarContactoResponseDataAreaType");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.SolicitarContactoResponseDataAreaType.class;
             cachedSerClasses.add(cls);
             cachedSerFactories.add(beansf);
             cachedDeserFactories.add(beandf);
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "SolicitarContactoResponseEBMType");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.SolicitarContactoResponseEBMType.class;
             cachedSerClasses.add(cls);
             cachedSerFactories.add(beansf);
             cachedDeserFactories.add(beandf);
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "SolicitarContactoType");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.SolicitarContactoType.class;
             cachedSerClasses.add(cls);
             cachedSerFactories.add(beansf);
             cachedDeserFactories.add(beandf);
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "UbicacionDelRiesgoType");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.UbicacionDelRiesgoType.class;
             cachedSerClasses.add(cls);
             cachedSerFactories.add(beansf);
             cachedDeserFactories.add(beandf);
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBM/Vehiculo/CotizacionVehiculo", "VehiculoACotizarType");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.VehiculoACotizarType.class;
             cachedSerClasses.add(cls);
             cachedSerFactories.add(beansf);
             cachedDeserFactories.add(beandf);
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBO/Allianz/CondicionesContratacion", "CondicionesContratacionType");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.Core.EBO.Allianz.CondicionesContratacion.CondicionesContratacionType.class;
             cachedSerClasses.add(cls);
             cachedSerFactories.add(beansf);
             cachedDeserFactories.add(beandf);
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBO/Allianz/CotizacionFullVehiculo", "CotizacionFullType");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.Core.EBO.Allianz.CotizacionFullVehiculo.CotizacionFullType.class;
             cachedSerClasses.add(cls);
             cachedSerFactories.add(beansf);
             cachedDeserFactories.add(beandf);
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBO/Allianz/CotizacionVehiculo", "CotizacionType");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.Core.EBO.Allianz.CotizacionVehiculo.CotizacionType.class;
             cachedSerClasses.add(cls);
             cachedSerFactories.add(beansf);
             cachedDeserFactories.add(beandf);
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBO/Allianz/Impuesto", "ImpuestosCotizacionType");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.Core.EBO.Allianz.Impuesto.ImpuestosCotizacionType.class;
             cachedSerClasses.add(cls);
             cachedSerFactories.add(beansf);
             cachedDeserFactories.add(beandf);
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBO/Allianz/Premio", "PremioType");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.Core.EBO.Allianz.Premio.PremioType.class;
             cachedSerClasses.add(cls);
             cachedSerFactories.add(beansf);
             cachedDeserFactories.add(beandf);
 
             qName = new javax.xml.namespace.QName("http://xmlns.allianz.com.ar/Core/EBO/Allianz/Prima", "PrimaType");
             cachedSerQNames.add(qName);
             cls = ar.com.allianz.xmlns.Core.EBO.Allianz.Prima.PrimaType.class;
             cachedSerClasses.add(cls);
             cachedSerFactories.add(beansf);
             cachedDeserFactories.add(beandf);
 
     }
 
     protected org.apache.axis.client.Call createCall() throws java.rmi.RemoteException {
         try {
             org.apache.axis.client.Call _call = super._createCall();
             if (super.maintainSessionSet) {
                 _call.setMaintainSession(super.maintainSession);
             }
             if (super.cachedUsername != null) {
                 _call.setUsername(super.cachedUsername);
             }
             if (super.cachedPassword != null) {
                 _call.setPassword(super.cachedPassword);
             }
             if (super.cachedEndpoint != null) {
                 _call.setTargetEndpointAddress(super.cachedEndpoint);
             }
             if (super.cachedTimeout != null) {
                 _call.setTimeout(super.cachedTimeout);
             }
             if (super.cachedPortName != null) {
                 _call.setPortName(super.cachedPortName);
             }
             java.util.Enumeration keys = super.cachedProperties.keys();
             while (keys.hasMoreElements()) {
                 java.lang.String key = (java.lang.String) keys.nextElement();
                 _call.setProperty(key, super.cachedProperties.get(key));
             }
             // All the type mapping information is registered
             // when the first call is made.
             // The type mapping information is actually registered in
             // the TypeMappingRegistry of the service, which
             // is the reason why registration is only needed for the first call.
             synchronized (this) {
                 if (firstCall()) {
                     // must set encoding style before registering serializers
                     _call.setEncodingStyle(null);
                     for (int i = 0; i < cachedSerFactories.size(); ++i) {
                         java.lang.Class cls = (java.lang.Class) cachedSerClasses.get(i);
                         javax.xml.namespace.QName qName =
                                 (javax.xml.namespace.QName) cachedSerQNames.get(i);
                         java.lang.Object x = cachedSerFactories.get(i);
                         if (x instanceof Class) {
                             java.lang.Class sf = (java.lang.Class)
                                  cachedSerFactories.get(i);
                             java.lang.Class df = (java.lang.Class)
                                  cachedDeserFactories.get(i);
                             _call.registerTypeMapping(cls, qName, sf, df, false);
                         }
                         else if (x instanceof javax.xml.rpc.encoding.SerializerFactory) {
                             org.apache.axis.encoding.SerializerFactory sf = (org.apache.axis.encoding.SerializerFactory)
                                  cachedSerFactories.get(i);
                             org.apache.axis.encoding.DeserializerFactory df = (org.apache.axis.encoding.DeserializerFactory)
                                  cachedDeserFactories.get(i);
                             _call.registerTypeMapping(cls, qName, sf, df, false);
                         }
                     }
                 }
             }
             return _call;
         }
         catch (java.lang.Throwable _t) {
             throw new org.apache.axis.AxisFault("Failure trying to get the Call object", _t);
         }
     }
 
     public ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.SolicitarContactoResponseEBMType solicitarContacto(ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.SolicitarContactoEBMType solicitarContactoEBM) throws java.rmi.RemoteException {
         if (super.cachedEndpoint == null) {
             throw new org.apache.axis.NoEndPointException();
         }
         org.apache.axis.client.Call _call = createCall();
         _call.setOperation(_operations[0]);
         _call.setUseSOAPAction(true);
         _call.setSOAPActionURI("http://xmlns.allianz.com.ar/Core/EBS/Vehiculo/SolicitarContacto");
         _call.setEncodingStyle(null);
         _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
         _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
         _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
         _call.setOperationName(new javax.xml.namespace.QName("", "SolicitarContacto"));
 
         setRequestHeaders(_call);
         setAttachments(_call);
  try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {solicitarContactoEBM});
 
         if (_resp instanceof java.rmi.RemoteException) {
             throw (java.rmi.RemoteException)_resp;
         }
         else {
             extractAttachments(_call);
             try {
                 return (ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.SolicitarContactoResponseEBMType) _resp;
             } catch (java.lang.Exception _exception) {
                 return (ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.SolicitarContactoResponseEBMType) org.apache.axis.utils.JavaUtils.convert(_resp, ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.SolicitarContactoResponseEBMType.class);
             }
         }
   } catch (org.apache.axis.AxisFault axisFaultException) {
   throw axisFaultException;
 }
     }
 
     public ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.CalcularCotizacionVehiculoResponseEBMType calcularCotizacionVehiculo(ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.CalcularCotizacionVehiculoEBMType calcularCotizacionVehiculoEBM) throws java.rmi.RemoteException {
         if (super.cachedEndpoint == null) {
             throw new org.apache.axis.NoEndPointException();
         }
         org.apache.axis.client.Call _call = createCall();
         
         /*
          * Agregado para insertar autentificacion 
          */
     	String nullString = null;    	
     	try {
     		SOAPHeaderElement user = new SOAPHeaderElement(nullString,"user");    	
 			user.addTextNode("ntnlbrks");
 			_call.addHeader(user);
 			SOAPHeaderElement password = new SOAPHeaderElement(nullString,"pwd");
 			password.addTextNode("055c5d907cb9");
 			_call.addHeader(password);
 		} catch (SOAPException e) {
 			e.printStackTrace();
 		}
     	
         _call.setOperation(_operations[1]);
         _call.setUseSOAPAction(true);
         _call.setSOAPActionURI("http://xmlns.allianz.com.ar/Core/EBS/Vehiculo/CalcularCotizacionVehiculo");
         _call.setEncodingStyle(null);
         _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
         _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
         _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
         _call.setOperationName(new javax.xml.namespace.QName("", "CalcularCotizacionVehiculo"));
 
         setRequestHeaders(_call);
         setAttachments(_call);
  try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {calcularCotizacionVehiculoEBM});
 
         if (_resp instanceof java.rmi.RemoteException) {
             throw (java.rmi.RemoteException)_resp;
         }
         else {
             extractAttachments(_call);
             try {
                 return (ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.CalcularCotizacionVehiculoResponseEBMType) _resp;
             } catch (java.lang.Exception _exception) {
                 return (ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.CalcularCotizacionVehiculoResponseEBMType) org.apache.axis.utils.JavaUtils.convert(_resp, ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.CalcularCotizacionVehiculoResponseEBMType.class);
             }
         }
   } catch (org.apache.axis.AxisFault axisFaultException) {
   throw axisFaultException;
 }
     finally{
     	//TODO aca esta!
     	System.out.println("-> " +_call.getMessageContext().getRequestMessage().getSOAPPartAsString());
     	System.out.println("<- " +_call.getMessageContext().getResponseMessage().getSOAPPartAsString());
     }
     }
 
     public ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.CalcularCotizacionFullVehiculoResponseEBMType calcularCotizacionFullVehiculo(ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.CalcularCotizacionFullVehiculoEBMType calcularCotizacionFullVehiculoEBM) throws java.rmi.RemoteException {
         if (super.cachedEndpoint == null) {
             throw new org.apache.axis.NoEndPointException();
         }
         org.apache.axis.client.Call _call = createCall();
         
         /*
          * Agregado para insertar autentificacion 
          */
     	String nullString = null;    	
     	try {
     		SOAPHeaderElement user = new SOAPHeaderElement(nullString,"user");    	
 			user.addTextNode("ntnlbrks");
 			_call.addHeader(user);
 			SOAPHeaderElement password = new SOAPHeaderElement(nullString,"pwd");
 			password.addTextNode("055c5d907cb9");
 			_call.addHeader(password);
 		} catch (SOAPException e) {
 			e.printStackTrace();
 		}
         
         _call.setOperation(_operations[2]);
         _call.setUseSOAPAction(true);
         _call.setSOAPActionURI("http://xmlns.allianz.com.ar/Core/EBS/Vehiculo/CalcularCotizacionFullVehiculo");
         _call.setEncodingStyle(null);
         _call.setProperty(org.apache.axis.client.Call.SEND_TYPE_ATTR, Boolean.FALSE);
         _call.setProperty(org.apache.axis.AxisEngine.PROP_DOMULTIREFS, Boolean.FALSE);
         _call.setSOAPVersion(org.apache.axis.soap.SOAPConstants.SOAP11_CONSTANTS);
         _call.setOperationName(new javax.xml.namespace.QName("", "CalcularCotizacionFullVehiculo"));
 
         setRequestHeaders(_call);
         setAttachments(_call);
  try {        java.lang.Object _resp = _call.invoke(new java.lang.Object[] {calcularCotizacionFullVehiculoEBM});
 
         if (_resp instanceof java.rmi.RemoteException) {
             throw (java.rmi.RemoteException)_resp;
         }
         else {
             extractAttachments(_call);
             try {
                 return (ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.CalcularCotizacionFullVehiculoResponseEBMType) _resp;
             } catch (java.lang.Exception _exception) {
                 return (ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.CalcularCotizacionFullVehiculoResponseEBMType) org.apache.axis.utils.JavaUtils.convert(_resp, ar.com.allianz.xmlns.Core.EBM.Vehiculo.CotizacionVehiculo.CalcularCotizacionFullVehiculoResponseEBMType.class);
             }
         }
   } catch (org.apache.axis.AxisFault axisFaultException) {
   throw axisFaultException;
 	}finally{
 		//TODO aca esta!
		System.out.println("-> " +_call.getMessageContext().getRequestMessage().getSOAPPartAsString());
		System.out.println("<- " +_call.getMessageContext().getResponseMessage().getSOAPPartAsString());
 	}
     }
 
 }
