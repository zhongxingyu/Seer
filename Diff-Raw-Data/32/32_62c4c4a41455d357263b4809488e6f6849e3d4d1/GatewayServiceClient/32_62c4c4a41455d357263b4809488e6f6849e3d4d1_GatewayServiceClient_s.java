 // ========================================================================
 // Copyright (C) zeroth Project Team. All rights reserved.
 // GNU AFFERO GENERAL PUBLIC LICENSE Version 3, 19 November 2007
 // http://www.gnu.org/licenses/agpl-3.0.txt
 // ========================================================================
 package zeroth.actor.service.app.ws;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Map;
 import java.util.logging.Logger;
 import javax.inject.Inject;
 import javax.xml.namespace.QName;
 import javax.xml.ws.BindingProvider;
 import javax.xml.ws.Service;
 import javax.xml.ws.WebServiceException;
 import zeroth.framework.enterprise.shared.EnterpriseException;
 import zeroth.framework.service.app.GatewayService;
 import zeroth.framework.standard.app.GatewayModel;
 /**
  * Gateway service client.
  * @author nilcy
  */
 public final class GatewayServiceClient {
     /** ロガー */
     @Inject
     private Logger log;
     /** コンストラクタ */
     private GatewayServiceClient() {
         super();
     }
     /**
      * Submit SOAP method.
      * @param <T> application model type
      * @param aSoapRequest SOAP request
      * @return SOAP response
      * @throws EnterpriseException MalformedURLException or
      *             WebServiceException
      */
     @SuppressWarnings("unchecked")
     public <T> T submit(final GatewayModel aSoapRequest) throws EnterpriseException {
         URL url;
         try {
             url = new URL("http://localhost:8080/ReferenceService/CalcService?wsdl");
            final QName serviceName = new QName("http://kuzumeji.com/soap/", "ReferenceService");
             final Service service = Service.create(url, serviceName);
             log.fine(service.getWSDLDocumentLocation().toString());
             final GatewayService port = service.getPort(GatewayService.class);
             final Map<String, Object> context = ((BindingProvider) port).getRequestContext();
             log.fine(context.entrySet().toString());
             final GatewayModel returnedValue = port.submit(aSoapRequest);
             return (T) returnedValue.getBody();
         } catch (final MalformedURLException e) {
             log.warning(e.getLocalizedMessage());
             throw new EnterpriseException(e.getLocalizedMessage());
         } catch (final WebServiceException e) {
             log.warning(e.getLocalizedMessage());
             throw new EnterpriseException(e.getLocalizedMessage());
         }
     }
 }
