 // ========================================================================
 // Copyright (C) zeroth Project Team. All rights reserved.
 // GNU AFFERO GENERAL PUBLIC LICENSE Version 3, 19 November 2007
 // http://www.gnu.org/licenses/agpl-3.0.txt
 // ========================================================================
 package zeroth.framework.service.app;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.logging.Logger;
 import javax.inject.Inject;
 import javax.jws.WebParam;
 import javax.ws.rs.core.Response.Status;
 import zeroth.framework.standard.app.GatewayHeader;
 import zeroth.framework.standard.app.GatewayModel;
 import zeroth.framework.standard.app.GatewayUtil;
 /**
  * ゲートウェイサービス
  * @author nilcy
  */
 // @Stateless
 // @WebService(portName = "GatewayPort", serviceName = "GatewayService", name = "Gateway",
// targetNamespace = "http://zeroth.com/ws/gateway", endpointInterface =
// "com.zeroth.ws.gateway.GatewayService")
 public class GatewayServiceImpl implements GatewayService {
     /** ロガー */
     @Inject
     private Logger log;
     /** コンストラクタ */
     public GatewayServiceImpl() {
         super();
     }
     /** {@inheritDoc} */
     @Override
     // @WebMethod
     public GatewayModel submit(@WebParam(name = "request") final GatewayModel request) {
         try {
             final GatewayHeader header = request.getHeader();
             final Class<?> interfaceClass = Class.forName(header.getEndpoint());
             final Class<?> parameterClass = Class.forName(header.getParameter());
             final Object proxy = GatewayUtil.getProxy(header.getWsdlLocation(),
                 header.getNamespaceURI(), header.getLocalPart(), interfaceClass);
             final Method method = interfaceClass.getMethod(header.getOperation(),
                 new Class[] { parameterClass });
             final Object ret = method.invoke(proxy,
                 new Object[] { GatewayUtil.decode(request.getBody(), parameterClass) });
             request.setBody(GatewayUtil.encode(ret));
             request.setStatus(GatewayFactory.createStatus(Status.OK));
         } catch (final ClassNotFoundException e) {
             log.warning(e.getLocalizedMessage());
             request.setStatus(GatewayFactory.createStatus(Status.BAD_REQUEST,
                 e.getLocalizedMessage()));
         } catch (final SecurityException e) {
             log.warning(e.getLocalizedMessage());
             request
                 .setStatus(GatewayFactory.createStatus(Status.FORBIDDEN, e.getLocalizedMessage()));
         } catch (final NoSuchMethodException e) {
             log.warning(e.getLocalizedMessage());
             request.setStatus(GatewayFactory.createStatus(Status.BAD_REQUEST,
                 e.getLocalizedMessage()));
         } catch (final IllegalArgumentException e) {
             log.warning(e.getLocalizedMessage());
             request.setStatus(GatewayFactory.createStatus(Status.BAD_REQUEST,
                 e.getLocalizedMessage()));
         } catch (final IllegalAccessException e) {
             log.warning(e.getLocalizedMessage());
             request.setStatus(GatewayFactory.createStatus(Status.BAD_REQUEST,
                 e.getLocalizedMessage()));
         } catch (final InvocationTargetException e) {
             log.warning(e.getLocalizedMessage());
             request.setStatus(GatewayFactory.createStatus(Status.INTERNAL_SERVER_ERROR,
                 e.getLocalizedMessage()));
         }
         return request;
     }
 }
