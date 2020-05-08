 //
 // $Id$
 //
 // jupload - A file upload applet.
 //
 // Copyright 2007 The JUpload Team
 //
 // Created: 07.05.2007
 // Creator: felfert
 // Last modified: $Date$
 //
 // This program is free software; you can redistribute it and/or modify
 // it under the terms of the GNU General Public License as published by
 // the Free Software Foundation; either version 2 of the License, or
 // (at your option) any later version.
 //
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 // GNU General Public License for more details.
 //
 // You should have received a copy of the GNU General Public License
 // along with this program; if not, write to the Free Software
 // Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 
 package wjhk.jupload2.upload;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.ConnectException;
 import java.net.InetSocketAddress;
 import java.net.Proxy;
 import java.net.ProxySelector;
 import java.net.Socket;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.net.UnknownHostException;
 import java.security.KeyManagementException;
 import java.security.KeyStoreException;
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 import java.security.UnrecoverableKeyException;
 import java.security.cert.CertificateException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLSocket;
 
 import wjhk.jupload2.policies.UploadPolicy;
 
 /**
  * This class implements the task of connecting to a HTTP(S) url using a proxy.
  * 
  * @author felfert
  */
 public class HttpConnect {
 
     private UploadPolicy uploadPolicy;
 
     /**
      * Helper function for perforing a proxy CONNECT request.
      * 
      * @param proxy The proxy to use.
      * @param host The destination's hostname.
      * @param port The destination's port
      * @return An established socket connection to the proxy.
      * @throws ConnectException if the proxy response code is not 200
      * @throws UnknownHostException
      * @throws IOException
      */
     private Socket HttpProxyConnect(Proxy proxy, String host, int port)
             throws UnknownHostException, IOException, ConnectException {
         InetSocketAddress sa = (InetSocketAddress) proxy.address();
         String phost = (sa.isUnresolved()) ? sa.getHostName() : sa.getAddress()
                 .getHostAddress();
         int pport = sa.getPort();
         // 
         Socket proxysock = new Socket(phost, pport);
         String req = "CONNECT " + host + ":" + port + " HTTP/1.1\r\n\r\n";
         proxysock.getOutputStream().write(req.getBytes());
         BufferedReader proxyIn = new BufferedReader(new InputStreamReader(
                 proxysock.getInputStream()));
         // We expect exactly one line: the proxy response
         String line = proxyIn.readLine();
         if (!line.matches("^HTTP/\\d\\.\\d\\s200\\s.*"))
             throw new ConnectException("Proxy response: " + line);
         this.uploadPolicy.displayDebug("Proxy response: " + line, 40);
         proxyIn.readLine(); // eat the header delimiter
         // we now are connected ...
         return proxysock;
     }
 
     /**
      * Connects to a given URL.
      * 
      * @param url The URL to connect to
      * @param proxy The proxy to be used, may be null if direct connection is
      *            needed
      * @return A socket, connected to the specified URL. May be null if an error
      *         occurs.
      * @throws NoSuchAlgorithmException
      * @throws KeyManagementException
      * @throws IOException
      * @throws UnknownHostException
      * @throws ConnectException
      * @throws CertificateException
      * @throws KeyStoreException
      * @throws UnrecoverableKeyException
      * @throws IllegalArgumentException
      */
     public Socket Connect(URL url, Proxy proxy)
             throws NoSuchAlgorithmException, KeyManagementException,
             ConnectException, UnknownHostException, IOException,
             KeyStoreException, CertificateException, IllegalArgumentException,
             UnrecoverableKeyException {
         // Temporary socket for SOCKS support
         Socket tsock;
         Socket ret = null;
         String host = url.getHost();
         int port;
         boolean useProxy = ((proxy != null) && (proxy.type() != Proxy.Type.DIRECT));
 
         // Check if SSL connection is needed
         if (url.getProtocol().equals("https")) {
             port = (-1 == url.getPort()) ? 443 : url.getPort();
             SSLContext context = SSLContext.getInstance("SSL");
             // Allow all certificates
             InteractiveTrustManager tm = new InteractiveTrustManager(
                     this.uploadPolicy.getSslVerifyCert(), url.getHost(), null);
             context.init(tm.getKeyManagers(), tm.getTrustManagers(),
                     SecureRandom.getInstance("SHA1PRNG"));
             if (useProxy) {
                 if (proxy.type() == Proxy.Type.HTTP) {
                     // First establish a CONNECT, then do a normal SSL
                     // thru that connection.
                     this.uploadPolicy.displayDebug(
                             "Using SSL socket, via HTTP proxy", 20);
                     ret = context.getSocketFactory().createSocket(
                             HttpProxyConnect(proxy, host, port), host, port,
                             true);
                 } else if (proxy.type() == Proxy.Type.SOCKS) {
                     this.uploadPolicy.displayDebug(
                             "Using SSL socket, via SOCKS proxy", 20);
                     tsock = new Socket(proxy);
                     tsock.connect(new InetSocketAddress(host, port));
                     ret = context.getSocketFactory().createSocket(tsock, host,
                             port, true);
                 } else
                     throw new ConnectException("Unkown proxy type "
                             + proxy.type());
             } else {
                 // If port not specified then use default https port
                 // 443.
                 this.uploadPolicy.displayDebug(
                         "Using SSL socket, direct connection", 20);
                 ret = context.getSocketFactory().createSocket(host, port);
             }
         } else {
             // If we are not in SSL, just use the old code.
             port = (-1 == url.getPort()) ? 80 : url.getPort();
             if (useProxy) {
                 if (proxy.type() == Proxy.Type.HTTP) {
                     InetSocketAddress sa = (InetSocketAddress) proxy.address();
                     host = (sa.isUnresolved()) ? sa.getHostName() : sa
                             .getAddress().getHostAddress();
                     port = sa.getPort();
                     this.uploadPolicy.displayDebug(
                             "Using non SSL socket, proxy=" + host + ":" + port,
                             20);
                     ret = new Socket(host, port);
                 } else if (proxy.type() == Proxy.Type.SOCKS) {
                     this.uploadPolicy.displayDebug(
                             "Using non SSL socket, via SOCKS proxy", 20);
                     tsock = new Socket(proxy);
                     tsock.connect(new InetSocketAddress(host, port));
                     ret = tsock;
                 } else
                     throw new ConnectException("Unkown proxy type "
                             + proxy.type());
             } else {
                 this.uploadPolicy.displayDebug(
                         "Using non SSL socket, direct connection", 20);
                 ret = new Socket(host, port);
             }
         }
         return ret;
     }
 
     /**
      * Connects to a given URL automatically using a proxy.
      * 
      * @param url The URL to connect to
      * @return A socket, connected to the specified URL. May be null if an error
      *         occurs.
      * @throws NoSuchAlgorithmException
      * @throws KeyManagementException
      * @throws IOException
      * @throws UnknownHostException
      * @throws ConnectException
      * @throws URISyntaxException
      * @throws UnrecoverableKeyException
      * @throws CertificateException
      * @throws KeyStoreException
      * @throws UnrecoverableKeyException
      * @throws IllegalArgumentException
      */
     public Socket Connect(URL url) throws NoSuchAlgorithmException,
             KeyManagementException, ConnectException, UnknownHostException,
             IOException, URISyntaxException, KeyStoreException,
             CertificateException, IllegalArgumentException,
             UnrecoverableKeyException {
         Proxy proxy = ProxySelector.getDefault().select(url.toURI()).get(0);
         return Connect(url, proxy);
     }
 
     /**
      * Retrieve the protocol to be used for the postURL of the current policy.
      * This method issues a HEAD request to the postURL and then examines the
      * protocol version returned in the response.
      * 
      * @return The string, describing the protocol (e.g. "HTTP/1.1")
      * @throws URISyntaxException
      * @throws IOException
      * @throws UnrecoverableKeyException
      * @throws IllegalArgumentException
      * @throws CertificateException
      * @throws KeyStoreException
      * @throws UnknownHostException
      * @throws NoSuchAlgorithmException
      * @throws KeyManagementException
      */
     public String getProtocol() throws URISyntaxException,
             KeyManagementException, NoSuchAlgorithmException,
             UnknownHostException, KeyStoreException, CertificateException,
             IllegalArgumentException, UnrecoverableKeyException, IOException {
         URL url = new URL(this.uploadPolicy.getPostURL());
         Proxy proxy = ProxySelector.getDefault().select(url.toURI()).get(0);
         boolean useProxy = ((proxy != null) && (proxy.type() != Proxy.Type.DIRECT));
         boolean useSSL = url.getProtocol().equals("https");
         Socket s = Connect(url, proxy);
         BufferedReader in = new BufferedReader(new InputStreamReader(s
                 .getInputStream()));
         StringBuffer req = new StringBuffer();
         req.append("HEAD ");
         if (useProxy && (!useSSL)) {
             // with a proxy we need the absolute URL, but only if not
             // using SSL. (with SSL, we first use the proxy CONNECT method,
             // and then a plain request.)
             req.append(url.getProtocol()).append("://").append(url.getHost());
         }
         req.append(url.getPath());
         /*
          * if (null != url.getQuery() && !"".equals(url.getQuery()))
          * req.append("?").append(url.getQuery());
          */
         req.append(" ").append("HTTP/1.1").append("\r\n");
         req.append("Host: ").append(url.getHost()).append("\r\n");
         req.append("Connection: close\r\n\r\n");
         OutputStream os = s.getOutputStream();
         os.write(req.toString().getBytes());
        os.close();
         if (!(s instanceof SSLSocket))
             s.shutdownOutput();
         String line = in.readLine();
         s.close();
         if (null == line) {
             this.uploadPolicy.displayErr("EMPTY HEAD response");
             return "HTTP/1.1";
         }
         Matcher m = Pattern.compile("^(HTTP/\\d\\.\\d)\\s.*").matcher(line);
         if (!m.matches()) {
             this.uploadPolicy.displayErr("Unexpected HEAD response: '" + line + "'");
             return "HTTP/1.1";
         }
         this.uploadPolicy.displayDebug("HEAD response: " + line, 40);
         return m.group(1);
     }
 
     /**
      * Creates a new instance.
      * 
      * @param policy The UploadPolicy to be used for logging.
      */
     public HttpConnect(UploadPolicy policy) {
         this.uploadPolicy = policy;
     }
 }
