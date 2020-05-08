 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package pulsarhunter.bookkeepr.jreaper;
 
 import bookkeepr.xml.StringConvertable;
 import bookkeepr.xml.XMLAble;
 import bookkeepr.xml.XMLReader;
 import bookkeepr.xml.XMLWriter;
 import bookkeepr.xmlable.BookkeeprHost;
 import bookkeepr.xmlable.CandidateList;
 import bookkeepr.xmlable.CandidateListIndex;
 import bookkeepr.xmlable.CandidateListStub;
 import bookkeepr.xmlable.ClassifiedCandidate;
 import bookkeepr.xmlable.ClassifiedCandidateIndex;
 import bookkeepr.xmlable.Processing;
 import bookkeepr.xmlable.Psrxml;
 import bookkeepr.xmlable.RawCandidate;
 import bookkeepr.xmlable.RawCandidateMatched;
 import bookkeepr.xmlable.ViewedCandidates;
 import bookkeepr.xmlable.ViewedCandidatesIndex;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.zip.GZIPInputStream;
 import org.apache.http.Header;
 import org.apache.http.HttpException;
 import org.apache.http.HttpHost;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.conn.params.ConnRoutePNames;
 import org.apache.http.entity.InputStreamEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.xml.sax.SAXException;
 
 /**
  *
  * @author kei041
  */
 public class BookKeeprConnection {
 
     private HttpClient httpClient;
     private BookkeeprHost remoteHost = null;
     private String status = "Not Connected";
 
     public BookKeeprConnection() {
 
         httpClient = new DefaultHttpClient();
 
     }
 
     public void setProxy(String proxyHost, int proxyPort) {
         final HttpHost proxy =
                 new HttpHost(proxyHost, proxyPort, "http");
         httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
     }
 
     public BookkeeprHost getRemoteHost() {
         return remoteHost;
     }
 
     public void setUrl(String rootUrl) throws URISyntaxException, BookKeeprCommunicationException {
         URI uri = new URI(rootUrl + "/ident");
         if (!uri.getPath().equals("/ident")) {
             uri = new URI("http://" + uri.getHost() + ":" + uri.getPort() + "/ident");
         }
         if (uri.getHost() == null) {
             status = "Bad URI specified";
             throw new BookKeeprCommunicationException("Cannot have empty URI");
         }
 
 
         contact(uri.toString());
 
 
     }
 
     public void contact() throws BookKeeprCommunicationException {
         contact(remoteHost.getUrl() + "/ident");
     }
 
     private void contact(String uri) throws BookKeeprCommunicationException {
         try {
             URI hostUri=new URI(uri);
             synchronized (httpClient) {
                 HttpGet req = new HttpGet(hostUri);
                 HttpResponse resp = httpClient.execute(req);
                 if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                     try {
                         InputStream in = resp.getEntity().getContent();
                         XMLAble xmlable = XMLReader.read(in);
                         in.close();
 
                         if (xmlable instanceof BookkeeprHost) {
                             //uri = this.remoteHost.getUrl();
                             this.remoteHost = (BookkeeprHost) xmlable;
 
 
                             this.remoteHost.setUrl("http://"+hostUri.getHost()+":"+hostUri.getPort());
                             status = "Connected (" + this.remoteHost.getStatus() + ")";
                         } else {
                             status = "Server Error";
                             throw new BookKeeprCommunicationException("BookKeepr returned the wrong thing for /ident");
                         }
                     } catch (SAXException ex) {
                         Logger.getLogger(BookKeeprConnection.class.getName()).log(Level.WARNING, "Got a malformed message from the bookkeepr", ex);
                         status = "Server Error";
 
                         throw new BookKeeprCommunicationException(ex);
                     }
                 } else {
                     resp.getEntity().consumeContent();
                     status = "Server Error (" + resp.getStatusLine().getStatusCode() + ")";
                     throw new BookKeeprCommunicationException("Got a " + resp.getStatusLine().getStatusCode() + " from the BookKeepr");
 
                 }
 
             }
         } catch (IOException ex) {
             Logger.getLogger(BookKeeprConnection.class.getName()).log(Level.WARNING, null, ex);
             status = "Server Error (IO error)";
             throw new BookKeeprCommunicationException(ex);
         } catch (HttpException ex) {
             Logger.getLogger(BookKeeprConnection.class.getName()).log(Level.WARNING, null, ex);
             status = "Server Error (HTTP error)";
             throw new BookKeeprCommunicationException(ex);
         } catch (URISyntaxException ex) {
             throw new BookKeeprCommunicationException(ex);
 
         }
     }
 
     public String getStatus() {
         return status;
 
 
     }
 
     public CandidateList getCandidateList(long id) throws BookKeeprCommunicationException {
         try {
             synchronized (httpClient) {
 
                 HttpGet req = new HttpGet(remoteHost.getUrl() + "/cand/" + Long.toHexString(id));
                 req.setHeader("Accept-Encoding", "gzip");
 
                 HttpResponse resp = httpClient.execute(req);
 
                 if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                     try {
                         InputStream in = resp.getEntity().getContent();
                         Header hdr = resp.getFirstHeader("Content-Encoding");
                         String enc = "";
                         if (hdr != null) {
                             enc = resp.getFirstHeader("Content-Encoding").getValue();
                         }
                         if (enc.equals("gzip")) {
                             in = new GZIPInputStream(in);
                         }
                         XMLAble xmlable = XMLReader.read(in);
                         in.close();
                         if (xmlable instanceof CandidateList) {
                             CandidateList p = (CandidateList) xmlable;
                             return p;
                         } else {
                             resp.getEntity().consumeContent();
                             throw new BookKeeprCommunicationException("BookKeepr returned the wrong thing for CandidateListID");
                         }
                     } catch (SAXException ex) {
                         Logger.getLogger(BookKeeprConnection.class.getName()).log(Level.WARNING, "Got a malformed message from the bookkeepr", ex);
                         throw new BookKeeprCommunicationException(ex);
                     }
                 } else {

                     throw new BookKeeprCommunicationException("Got a " + resp.getStatusLine().getStatusCode() + " from the BookKeepr");
                 }
             }
         } catch (HttpException ex) {
             throw new BookKeeprCommunicationException(ex);
 
         } catch (IOException ex) {
             throw new BookKeeprCommunicationException(ex);
 
         } catch (URISyntaxException ex) {
             throw new BookKeeprCommunicationException(ex);
 
         }
     }
 
     public ClassifiedCandidateIndex getAllClassifiedCandidates() throws BookKeeprCommunicationException {
         try {
             synchronized (httpClient) {
 
                 HttpGet req = new HttpGet(remoteHost.getUrl() + "/cand/classified");
                 req.setHeader("Accept-Encoding", "gzip");
 
                 HttpResponse resp = httpClient.execute(req);
 
                 if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                     try {
                         InputStream in = resp.getEntity().getContent();
                         Header hdr = resp.getFirstHeader("Content-Encoding");
                         String enc = "";
                         if (hdr != null) {
                             enc = resp.getFirstHeader("Content-Encoding").getValue();
                         }
                         if (enc.equals("gzip")) {
                             in = new GZIPInputStream(in);
                         }
                         XMLAble xmlable = XMLReader.read(in);
                         in.close();
 
                         if (xmlable instanceof ClassifiedCandidateIndex) {
                             ClassifiedCandidateIndex p = (ClassifiedCandidateIndex) xmlable;
                             return p;
                         } else {
                             resp.getEntity().consumeContent();
                             throw new BookKeeprCommunicationException("BookKeepr returned the wrong thing for ClassifiedCandidateIndex");
                         }
                     } catch (SAXException ex) {
                         Logger.getLogger(BookKeeprConnection.class.getName()).log(Level.WARNING, "Got a malformed message from the bookkeepr", ex);
                         throw new BookKeeprCommunicationException(ex);
                     }
                 } else {
                     throw new BookKeeprCommunicationException("Got a " + resp.getStatusLine().getStatusCode() + " from the BookKeepr");
                 }
             }
         } catch (HttpException ex) {
             throw new BookKeeprCommunicationException(ex);
 
         } catch (IOException ex) {
             throw new BookKeeprCommunicationException(ex);
 
         } catch (URISyntaxException ex) {
             throw new BookKeeprCommunicationException(ex);
         }
     }
 
     public ClassifiedCandidate postClassifiedCandidate(ClassifiedCandidate cand) throws BookKeeprCommunicationException {
         try {
             synchronized (httpClient) {
                 HttpPost req = new HttpPost(remoteHost.getUrl() + "/cand/newclassified");
                 req.setHeader("Accept-Encoding", "gzip");
                 ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
                 XMLWriter.write(out, cand);
                 ByteArrayInputStream in2 = new ByteArrayInputStream(out.toByteArray());
                 req.setEntity(new InputStreamEntity(in2, -1));
                 HttpResponse resp = httpClient.execute(req);
 
                 if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                     try {
                         InputStream in = resp.getEntity().getContent();
                         Header hdr = resp.getFirstHeader("Content-Encoding");
                         String enc = "";
                         if (hdr != null) {
                             enc = resp.getFirstHeader("Content-Encoding").getValue();
                         }
                         if (enc.equals("gzip")) {
                             in = new GZIPInputStream(in);
                         }
                         XMLAble xmlable = XMLReader.read(in);
                         in.close();
                         if (xmlable instanceof ClassifiedCandidate) {
                             ClassifiedCandidate p = (ClassifiedCandidate) xmlable;
                             return p;
                         } else {
                             resp.getEntity().consumeContent();
                             throw new BookKeeprCommunicationException("BookKeepr returned the wrong thing for ClassifiedCandidate");
                         }
                     } catch (SAXException ex) {
                         Logger.getLogger(BookKeeprConnection.class.getName()).log(Level.WARNING, "Got a malformed message from the bookkeepr", ex);
                         throw new BookKeeprCommunicationException(ex);
                     }
                 } else {
                     throw new BookKeeprCommunicationException("Got a " + resp.getStatusLine().getStatusCode() + " from the BookKeepr");
                 }
             }
         } catch (HttpException ex) {
             throw new BookKeeprCommunicationException(ex);
 
         } catch (IOException ex) {
             throw new BookKeeprCommunicationException(ex);
 
         } catch (URISyntaxException ex) {
             throw new BookKeeprCommunicationException(ex);
         }
     }
 
     public ClassifiedCandidate postToClassifiedCandidate(ClassifiedCandidate cand, RawCandidateMatched basic) throws BookKeeprCommunicationException {
         try {
             synchronized (httpClient) {
 
                 HttpPost req = new HttpPost(remoteHost.getUrl() + "/cand/" + Long.toHexString(cand.getId()));
                 req.setHeader("Accept-Encoding", "gzip");
                 ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
                 XMLWriter.write(out, basic);
                 ByteArrayInputStream in2 = new ByteArrayInputStream(out.toByteArray());
                 req.setEntity(new InputStreamEntity(in2, -1));
                 HttpResponse resp = httpClient.execute(req);
 
                 if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                     try {
                         InputStream in = resp.getEntity().getContent();
                         Header hdr = resp.getFirstHeader("Content-Encoding");
                         String enc = "";
                         if (hdr != null) {
                             enc = resp.getFirstHeader("Content-Encoding").getValue();
                         }
                         if (enc.equals("gzip")) {
                             in = new GZIPInputStream(in);
                         }
                         XMLAble xmlable = XMLReader.read(in);
                         in.close();
                         if (xmlable instanceof ClassifiedCandidate) {
                             ClassifiedCandidate p = (ClassifiedCandidate) xmlable;
                             return p;
                         } else {
                             resp.getEntity().consumeContent();
                             throw new BookKeeprCommunicationException("BookKeepr returned the wrong thing for ClassifiedCandidate");
                         }
                     } catch (SAXException ex) {
                         Logger.getLogger(BookKeeprConnection.class.getName()).log(Level.WARNING, "Got a malformed message from the bookkeepr", ex);
                         throw new BookKeeprCommunicationException(ex);
                     }
                 } else {
                     throw new BookKeeprCommunicationException("Got a " + resp.getStatusLine().getStatusCode() + " from the BookKeepr");
                 }
             }
         } catch (HttpException ex) {
             throw new BookKeeprCommunicationException(ex);
 
         } catch (IOException ex) {
             throw new BookKeeprCommunicationException(ex);
 
         } catch (URISyntaxException ex) {
             throw new BookKeeprCommunicationException(ex);
         }
     }
 
     public ViewedCandidatesIndex getAllViewedCandidates() throws BookKeeprCommunicationException {
         try {
             synchronized (httpClient) {
 
                 HttpGet req = new HttpGet(remoteHost.getUrl() + "/cand/viewed");
                 req.setHeader("Accept-Encoding", "gzip");
 
                 HttpResponse resp = httpClient.execute(req);
 
                 if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                     try {
                         InputStream in = resp.getEntity().getContent();
                         Header hdr = resp.getFirstHeader("Content-Encoding");
                         String enc = "";
                         if (hdr != null) {
                             enc = resp.getFirstHeader("Content-Encoding").getValue();
                         }
                         if (enc.equals("gzip")) {
                             in = new GZIPInputStream(in);
                         }
                         XMLAble xmlable = XMLReader.read(in);
                         in.close();
 
                         if (xmlable instanceof ViewedCandidatesIndex) {
                             ViewedCandidatesIndex p = (ViewedCandidatesIndex) xmlable;
 
                             return p;
                         } else {
                             resp.getEntity().consumeContent();
                             throw new BookKeeprCommunicationException("BookKeepr returned the wrong thing for ViewedCandidatesIndex");
                         }
                     } catch (SAXException ex) {
                         Logger.getLogger(BookKeeprConnection.class.getName()).log(Level.WARNING, "Got a malformed message from the bookkeepr", ex);
                         throw new BookKeeprCommunicationException(ex);
                     }
                 } else {
                     throw new BookKeeprCommunicationException("Got a " + resp.getStatusLine().getStatusCode() + " from the BookKeepr");
                 }
             }
         } catch (HttpException ex) {
             throw new BookKeeprCommunicationException(ex);
 
         } catch (IOException ex) {
             throw new BookKeeprCommunicationException(ex);
 
         } catch (URISyntaxException ex) {
             throw new BookKeeprCommunicationException(ex);
         }
     }
 
     public ViewedCandidates postViewedCandidates(ViewedCandidates viewedCandidates) throws BookKeeprCommunicationException {
         try {
             synchronized (httpClient) {
                 HttpPost req = new HttpPost(remoteHost.getUrl() + "/cand/viewed");
                 req.setHeader("Accept-Encoding", "gzip");
                 ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
                 XMLWriter.write(out, viewedCandidates);
                 ByteArrayInputStream in2 = new ByteArrayInputStream(out.toByteArray());
                 req.setEntity(new InputStreamEntity(in2, -1));
                 HttpResponse resp = httpClient.execute(req);
 
                 if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                     try {
                         InputStream in = resp.getEntity().getContent();
                         Header hdr = resp.getFirstHeader("Content-Encoding");
                         String enc = "";
                         if (hdr != null) {
                             enc = resp.getFirstHeader("Content-Encoding").getValue();
                         }
                         if (enc.equals("gzip")) {
                             in = new GZIPInputStream(in);
                         }
                         XMLAble xmlable = XMLReader.read(in);
                         in.close();
                         if (xmlable instanceof ViewedCandidates) {
                             ViewedCandidates p = (ViewedCandidates) xmlable;
                             return p;
                         } else {
                             resp.getEntity().consumeContent();
                             throw new BookKeeprCommunicationException("BookKeepr returned the wrong thing for ViewedCandidates");
                         }
                     } catch (SAXException ex) {
                         Logger.getLogger(BookKeeprConnection.class.getName()).log(Level.WARNING, "Got a malformed message from the bookkeepr", ex);
                         throw new BookKeeprCommunicationException(ex);
                     }
                 } else {
                     throw new BookKeeprCommunicationException("Got a " + resp.getStatusLine().getStatusCode() + " from the BookKeepr");
                 }
             }
         } catch (HttpException ex) {
             throw new BookKeeprCommunicationException(ex);
 
         } catch (IOException ex) {
             throw new BookKeeprCommunicationException(ex);
 
         } catch (URISyntaxException ex) {
             throw new BookKeeprCommunicationException(ex);
         }
     }
 
     public Processing getProcess(long processId) throws BookKeeprCommunicationException {
         try {
             synchronized (httpClient) {
 
                 HttpGet req = new HttpGet(remoteHost.getUrl() + "/id/" + Long.toHexString(processId));
                 HttpResponse resp = httpClient.execute(req);
 
                 if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                     try {
                         XMLAble xmlable = XMLReader.read(resp.getEntity().getContent());
                         if (xmlable instanceof Processing) {
                             Processing p = (Processing) xmlable;
                             return p;
                         } else {
 
                             throw new BookKeeprCommunicationException("BookKeepr returned the wrong thing for pointingID");
                         }
                     } catch (SAXException ex) {
                         Logger.getLogger(BookKeeprConnection.class.getName()).log(Level.WARNING, "Got a malformed message from the bookkeepr", ex);
                         throw new BookKeeprCommunicationException(ex);
                     }
                 } else {
                     resp.getEntity().consumeContent();
                     throw new BookKeeprCommunicationException("Got a " + resp.getStatusLine().getStatusCode() + " from the BookKeepr");
                 }
             }
         } catch (HttpException ex) {
             throw new BookKeeprCommunicationException(ex);
 
         } catch (IOException ex) {
             throw new BookKeeprCommunicationException(ex);
 
         } catch (URISyntaxException ex) {
             throw new BookKeeprCommunicationException(ex);
 
         }
     }
 
     public RawCandidate getRawCandidate(long candId) throws BookKeeprCommunicationException {
         try {
             synchronized (httpClient) {
 
                 HttpGet req = new HttpGet(remoteHost.getUrl() + "/cand/" + Long.toHexString(candId));
                 req.setHeader("Accept-Encoding", "gzip");
 
                 HttpResponse resp = httpClient.execute(req);
 
                 if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                     try {
                         InputStream in = resp.getEntity().getContent();
                         Header hdr = resp.getFirstHeader("Content-Encoding");
                         String enc = "";
                         if (hdr != null) {
                             enc = resp.getFirstHeader("Content-Encoding").getValue();
                         }
                         if (enc.equals("gzip")) {
                             in = new GZIPInputStream(in);
                         }
                         XMLAble xmlable = XMLReader.read(in);
                         in.close();
                         if (xmlable instanceof RawCandidate) {
                             RawCandidate p = (RawCandidate) xmlable;
                             return p;
                         } else {
 
                             throw new BookKeeprCommunicationException("BookKeepr returned the wrong thing for candId");
                         }
                     } catch (SAXException ex) {
                         Logger.getLogger(BookKeeprConnection.class.getName()).log(Level.WARNING, "Got a malformed message from the bookkeepr", ex);
                         throw new BookKeeprCommunicationException(ex);
                     }
                 } else {
                     resp.getEntity().consumeContent();
                     throw new BookKeeprCommunicationException("Got a " + resp.getStatusLine().getStatusCode() + " from the BookKeepr");
                 }
             }
         } catch (HttpException ex) {
             throw new BookKeeprCommunicationException(ex);
 
         } catch (IOException ex) {
             throw new BookKeeprCommunicationException(ex);
 
         } catch (URISyntaxException ex) {
             throw new BookKeeprCommunicationException(ex);
 
         }
     }
 
     public List<CandidateListStub> getAllCandLists() throws BookKeeprCommunicationException {
         try {
             synchronized (httpClient) {
 
                 HttpGet req = new HttpGet(remoteHost.getUrl() + "/cand/lists");
                 HttpResponse resp = httpClient.execute(req);
 
                 if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                     try {
                         InputStream in = resp.getEntity().getContent();
                         XMLAble xmlable = XMLReader.read(in);
                         in.close();
                         if (xmlable instanceof CandidateListIndex) {
                             CandidateListIndex idx = (CandidateListIndex) xmlable;
                             return idx.getCandidateListStubList();
                         } else {
                             throw new BookKeeprCommunicationException("BookKeepr returned the wrong thing for /cand/lists");
                         }
                     } catch (SAXException ex) {
                         Logger.getLogger(BookKeeprConnection.class.getName()).log(Level.WARNING, "Got a malformed message from the bookkeepr", ex);
                         throw new BookKeeprCommunicationException(ex);
                     }
                 } else {
                     resp.getEntity().consumeContent();
                     throw new BookKeeprCommunicationException("Got a " + resp.getStatusLine().getStatusCode() + " from the BookKeepr");
                 }
             }
         } catch (HttpException ex) {
             throw new BookKeeprCommunicationException(ex);
 
         } catch (IOException ex) {
             throw new BookKeeprCommunicationException(ex);
 
         } catch (URISyntaxException ex) {
             throw new BookKeeprCommunicationException(ex);
 
         }
     }
     
     
     public Psrxml getPsrxmlForCandidateList(CandidateList clist) throws BookKeeprCommunicationException{
          try {
             synchronized (httpClient) {
 
                 
                 long psrxmlid = clist.getPsrxmlId();
                 HttpGet req = new HttpGet(remoteHost.getUrl() + "/id/"+StringConvertable.ID.toString(psrxmlid));
                 HttpResponse resp = httpClient.execute(req);
 
                 if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                     try {
                         InputStream in = resp.getEntity().getContent();
                         XMLAble xmlable = XMLReader.read(in);
                         in.close();
                         if (xmlable instanceof Psrxml) {
                             Psrxml psrxml = (Psrxml) xmlable;
                             return psrxml;
                         } else {
                             throw new BookKeeprCommunicationException("BookKeepr returned the wrong thing for psrxml id "+psrxmlid);
                         }
                     } catch (SAXException ex) {
                         Logger.getLogger(BookKeeprConnection.class.getName()).log(Level.WARNING, "Got a malformed message from the bookkeepr", ex);
                         throw new BookKeeprCommunicationException(ex);
                     }
                 } else {
                     resp.getEntity().consumeContent();
 
                     throw new BookKeeprCommunicationException("Got a " + resp.getStatusLine().getStatusCode() + " from the BookKeepr  ("+remoteHost.getUrl() + "/id/"+StringConvertable.ID+")");
                 }
             }
         } catch (HttpException ex) {
             throw new BookKeeprCommunicationException(ex);
 
         } catch (IOException ex) {
             throw new BookKeeprCommunicationException(ex);
 
         } catch (URISyntaxException ex) {
             throw new BookKeeprCommunicationException(ex);
 
         }
     }
 }
