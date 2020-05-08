 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.verdis;
 
 import Sirius.navigator.connection.Connection;
 import Sirius.navigator.connection.ConnectionFactory;
 import Sirius.navigator.connection.ConnectionInfo;
 import Sirius.navigator.connection.ConnectionSession;
 import Sirius.navigator.connection.proxy.ConnectionProxy;
 
 import Sirius.server.middleware.types.MetaObject;
 
 import org.apache.commons.beanutils.BeanUtils;
 
 import de.cismet.tools.gui.log4jquickconfig.Log4JQuickConfig;
 
 import de.cismet.verdis.commons.constants.VerdisConstants;
 
 /**
  * DOCUMENT ME!
  *
  * @author   thorsten.hell@cismet.de
  * @version  $Revision$, $Date$
  */
 public class Test1 {
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new instance of Test1.
      */
     public Test1() {
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @param  args  DOCUMENT ME!
      */
     public static void main(final String[] args) {
         try {
             final String callServerURL = "http://localhost:9986/callserver/binary";
             final String connectionClass = "Sirius.navigator.connection.RESTfulConnection";
             Log4JQuickConfig.configure4LumbermillOnLocalhost();
 
 //            String callServerURL="rmi://localhost/callServer";
 //            String connectionClass="Sirius.navigator.connection.RMIConnection";
             final Connection connection = ConnectionFactory.getFactory()
                         .createConnection(connectionClass, callServerURL);
             ConnectionSession session = null;
             ConnectionProxy proxy = null;
             final ConnectionInfo connectionInfo = new ConnectionInfo();
             connectionInfo.setCallserverURL(callServerURL);
             connectionInfo.setPassword("sb");
             connectionInfo.setUserDomain(VerdisConstants.DOMAIN);
             connectionInfo.setUsergroup(VerdisConstants.DOMAIN);
             connectionInfo.setUsergroupDomain(VerdisConstants.DOMAIN);
             connectionInfo.setUsername("SteinbacherD102");
 
             session = ConnectionFactory.getFactory().createSession(connection, connectionInfo, true);
 
             System.out.println("session created");
             proxy = ConnectionFactory.getFactory()
                         .createProxy("Sirius.navigator.connection.proxy.DefaultConnectionProxyHandler", session);
 
             System.out.println("connection established");
             System.out.println("retrieve 6000467");
             final long l = System.currentTimeMillis();
             final MetaObject mo = proxy.getMetaObject(6000467, 11, VerdisConstants.DOMAIN);
 //            MetaObject mo = proxy.getMetaObject(6021737, 11, VerdisConstants.DOMAIN);
             System.out.println("dauer:" + (System.currentTimeMillis() - l));
             System.out.println("retrieved 6000467");
            System.out.println(mo.getBean().toJSONString());
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 }
