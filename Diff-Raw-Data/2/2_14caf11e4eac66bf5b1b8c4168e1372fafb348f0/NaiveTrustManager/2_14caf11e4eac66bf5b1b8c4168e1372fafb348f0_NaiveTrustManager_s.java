 /*
  * $Id$
  */
package org.xins.util.ant.sourceforge;
 
 import java.security.cert.X509Certificate;
 import javax.net.ssl.X509TrustManager;
 
 /**
  * Trust manager that trusts all clients and all servers with any checks.
  *
  * @version $Revision$ $Date$
  * @author Ernst de Haan (<a href="mailto:znerd@FreeBSD.org">znerd@FreeBSD.org</a>)
  *
  * @since XINS 0.127
  */
 public final class NaiveTrustManager
 extends Object
 implements X509TrustManager {
 
    //-------------------------------------------------------------------------
    // Class fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Class functions
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------------------
 
    /**
     * Constructs a new <code>NaiveTrustManager</code>.
     */
    public NaiveTrustManager() {
       // empty
    }
 
 
    //-------------------------------------------------------------------------
    // Fields
    //-------------------------------------------------------------------------
 
    //-------------------------------------------------------------------------
    // Methods
    //-------------------------------------------------------------------------
 
    public X509Certificate[] getAcceptedIssuers() {
       return null;
    }
 
    public void checkClientTrusted(X509Certificate[] certs, String authType) {
       // empty
    }
 
    public void checkServerTrusted(X509Certificate[] certs, String authType) {
       // empty
    }
 }
