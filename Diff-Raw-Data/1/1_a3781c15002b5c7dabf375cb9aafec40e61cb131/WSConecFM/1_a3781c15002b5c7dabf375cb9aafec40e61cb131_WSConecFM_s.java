 /**
  * @author Benito Arango
  * @e-mail benito.arango@facturacionmoderna.com
  * @version 1.0
  */
 package wsconnectionfm;
 
 import com.sun.org.apache.xerces.internal.parsers.DOMParser;
 import java.io.*;
 import javax.xml.soap.*;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 
 public class WSConecFM {
   private String urlSOAP;
   private String userId;
   private String userPass;
   private String rfcEmisor;
   private String soapResponse;
   public boolean generarPDF;
   public boolean generarTXT;
   public boolean generarCBB;
   public boolean error;
   public String strPdf;
   public String strXml;
   public String strCbb;
   public String strTxt;
 
   /*  Constructor que inicializa las variables de conexión
    *  Los valores inicializados son para el ambiente de pruebas
    */
   public WSConecFM() {
     this.urlSOAP = "https://t1demo.facturacionmoderna.com/timbrado/soap";
     this.userId = "UsuarioPruebasWS";
     this.userPass = "b9ec2afa3361a59af4b4d102d3f704eabdf097d4";
     this.rfcEmisor = "ESI920427886";
     this.generarPDF = true;
     this.generarTXT = false;
     this.generarCBB = false;
     this.error = false;
   }
   
   // Metodos para configurar los parametros de conexión
   public void setUrlTimbrado(String url) {
     this.urlSOAP = url;
   }
   public void setUserId(String user) {
     this.userId = user;
   }
   public void setUserPass(String pass) {
     this.userPass = pass;
   }
   public void setRfcEmisor(String rfc) {
     this.rfcEmisor = rfc;
   }
   public void setGenerarPDF(boolean pdf) {
     this.generarPDF = pdf;
   }
   public void setGenerarTXT(boolean txt) {
     this.generarTXT = txt;
   }
   public void setGenerarCBB(boolean cbb) {
     this.generarCBB = cbb;
   }
 
   public String timbrar(String layout) {
     File f = null;
     String linea, strLayout="", layoutb64="", wsresponse="Timbrado Exitoso";
     SOAPMessage soapResp;
     f = new File(layout);
     if (f.exists()) {
       try {
         BufferedReader buffer = new BufferedReader(new InputStreamReader(new FileInputStream(layout), "utf-8"));
         while ((linea = buffer.readLine()) != null)   {
           strLayout = strLayout + linea;
         }
       } catch(Exception ex) {return "Error al leer el layout";}
       layout = strLayout;
     }
     // Convertir a base 64 el layout.
     try {
       String ly = layout;
       layoutb64 = new sun.misc.BASE64Encoder().encode(ly.getBytes("UTF-8"));
     }
     catch (Exception ex) { }
     
     // Inicia la petición SOAP
     try {
       SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
       SOAPConnection soapConnection = soapConnectionFactory.createConnection();
       // Enviar mensaje al servidor
       MessageFactory messageFactory = MessageFactory.newInstance();
       SOAPMessage soapMessage = messageFactory.createMessage();
       SOAPPart soapPart = soapMessage.getSOAPPart();
       SOAPEnvelope envelope = soapPart.getEnvelope();
       envelope.addNamespaceDeclaration("xmlns",this.urlSOAP);
       SOAPBody soapBody = envelope.getBody();
       SOAPElement soapBodyElem=soapBody.addChildElement("requestTimbrarCFDI","");
       SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("param0");
       SOAPElement soapBodyElem3 = soapBodyElem1.addChildElement("text2CFDI");
       soapBodyElem3.addTextNode(layoutb64);
       SOAPElement soapBodyElem4 = soapBodyElem1.addChildElement("UserID");
       soapBodyElem4.addTextNode(this.userId);
       SOAPElement soapBodyElem5 = soapBodyElem1.addChildElement("UserPass");
       soapBodyElem5.addTextNode(this.userPass);
       SOAPElement soapBodyElem6 = soapBodyElem1.addChildElement("emisorRFC");
       soapBodyElem6.addTextNode(this.rfcEmisor);
       SOAPElement soapBodyElem7 = soapBodyElem1.addChildElement("generarTXT");
       soapBodyElem7.addTextNode(Boolean.toString(this.generarTXT));
       SOAPElement soapBodyElem8 = soapBodyElem1.addChildElement("generarPDF");
       soapBodyElem8.addTextNode(Boolean.toString(this.generarPDF));
       SOAPElement soapBodyElem9 = soapBodyElem1.addChildElement("generarCBB");
       soapBodyElem9.addTextNode(Boolean.toString(this.generarCBB));
       MimeHeaders headers = soapMessage.getMimeHeaders();
       headers.addHeader("SOAPAction", "requestTimbrarCFDI");
       soapMessage.saveChanges();
 
       /* Dscomentar la siguiente linea si desea visualizar el request*/
       //this.soapRequest=this.soapMessageToString(soapMessage);
 
       soapResp = soapConnection.call(soapMessage, this.urlSOAP);
 
       this.soapResponse=this.soapMessageToString(soapResp);
       soapConnection.close();
 
 
       String cadenaXML = this.soapResponse;
 
       DOMParser parser = new DOMParser();
       parser.parse(new InputSource(new java.io.StringReader(cadenaXML)));
 
       Document doc = parser.getDocument();
 
       if (doc.getElementsByTagName("SOAP-ENV:Fault").getLength() > 0) {
         NodeList nodeLst = doc.getElementsByTagName("SOAP-ENV:Body");
         for (int i = 0; i < nodeLst.getLength(); i++) {
           Element ele = (Element) nodeLst.item(i);
 
           NodeList nlsCode = ele.getElementsByTagName("faultcode");
           Element eleCode = (Element) nlsCode.item(0);
           String strCode = eleCode.getFirstChild().getNodeValue();
 
           NodeList nlsMessage = ele.getElementsByTagName("faultstring");
           Element eleMsg = (Element) nlsMessage.item(0);
           String strMessage = eleMsg.getFirstChild().getNodeValue();
 
           wsresponse = "Error: " + strCode + "\n";
           wsresponse = wsresponse + "Mensaje: " + strMessage;
 
         }
       } else {
         if (this.generarCBB) {this.generarPDF = false;}
         NodeList nodeLst = doc.getElementsByTagName("ns1:requestTimbrarCFDIResponse");
         for (int i = 0; i < nodeLst.getLength(); i++) {
           Element ele = (Element) nodeLst.item(i);
 
           NodeList nlsxml = ele.getElementsByTagName("xml");
           Element elexml = (Element) nlsxml.item(0);
           this.strXml = elexml.getFirstChild().getNodeValue();
 
           if (this.generarPDF) {
             NodeList nlspdf = ele.getElementsByTagName("pdf");
             Element elepdf = (Element) nlspdf.item(0);
             this.strPdf = elepdf.getFirstChild().getNodeValue();
           }
           if (this.generarTXT) {
             NodeList nlstxt = ele.getElementsByTagName("txt");
             Element eletxt = (Element) nlstxt.item(0);
             this.strTxt = eletxt.getFirstChild().getNodeValue();
           }
           if (this.generarCBB) {
             NodeList nlscbb = ele.getElementsByTagName("png");
             Element elecbb = (Element) nlscbb.item(0);
             this.strCbb = elecbb.getFirstChild().getNodeValue();
           }
         }
       }
     }
     catch (Exception e) {
       this.error = true;
       wsresponse = e.getMessage().toString();
     }
     return wsresponse;
   }
 
   public String cancelar(String UUID) {
     String wsresponse="Cancelado Exitoso";
     SOAPMessage soapResp;
     // Inicia la petición SOAP
     try {
       SOAPConnectionFactory soapConnectionFactory = SOAPConnectionFactory.newInstance();
       SOAPConnection soapConnection = soapConnectionFactory.createConnection();
       // Enviar mensaje al servidor
       MessageFactory messageFactory = MessageFactory.newInstance();
       SOAPMessage soapMessage = messageFactory.createMessage();
       SOAPPart soapPart = soapMessage.getSOAPPart();
       SOAPEnvelope envelope = soapPart.getEnvelope();
       envelope.addNamespaceDeclaration("xmlns",this.urlSOAP);
       SOAPBody soapBody = envelope.getBody();
       SOAPElement soapBodyElem=soapBody.addChildElement("requestCancelarCFDI","");
       SOAPElement soapBodyElem1 = soapBodyElem.addChildElement("request");
       SOAPElement soapBodyElem4 = soapBodyElem1.addChildElement("UserID");
       soapBodyElem4.addTextNode(this.userId);
       SOAPElement soapBodyElem5 = soapBodyElem1.addChildElement("UserPass");
       soapBodyElem5.addTextNode(this.userPass);
       SOAPElement soapBodyElem6 = soapBodyElem1.addChildElement("emisorRFC");
       soapBodyElem6.addTextNode(this.rfcEmisor);
       SOAPElement soapBodyElem7 = soapBodyElem1.addChildElement("uuid");
       soapBodyElem7.addTextNode(UUID);
       MimeHeaders headers = soapMessage.getMimeHeaders();
       headers.addHeader("SOAPAction", "requestTimbrarCFDI");
       soapMessage.saveChanges();
 
       /* Dscomentar la siguiente linea si desea visualizar el request*/
       //this.soapRequest=this.soapMessageToString(soapMessage);
       soapResp = soapConnection.call(soapMessage, this.urlSOAP);
 
       this.soapResponse=this.soapMessageToString(soapResp);
       soapConnection.close();
 
       String cadenaXML = this.soapResponse;
 
       DOMParser parser = new DOMParser();
       parser.parse(new InputSource(new java.io.StringReader(cadenaXML)));
 
       Document doc = parser.getDocument();
 
       if (doc.getElementsByTagName("SOAP-ENV:Fault").getLength() > 0) {
         NodeList nodeLst = doc.getElementsByTagName("SOAP-ENV:Body");
         for (int i = 0; i < nodeLst.getLength(); i++) {
           Element ele = (Element) nodeLst.item(i);
 
           NodeList nlsCode = ele.getElementsByTagName("faultcode");
           Element eleCode = (Element) nlsCode.item(0);
           String strCode = eleCode.getFirstChild().getNodeValue();
 
           NodeList nlsMessage = ele.getElementsByTagName("faultstring");
           Element eleMsg = (Element) nlsMessage.item(0);
           String strMessage = eleMsg.getFirstChild().getNodeValue();
 
           wsresponse = "Error: " + strCode + "\n";
           wsresponse = wsresponse + "Mensaje: " + strMessage;
 
         }
       } else {
         NodeList nodeLst = doc.getElementsByTagName("ns1:requestCancelarCFDIResponse");
         for (int i = 0; i < nodeLst.getLength(); i++) {
           Element ele = (Element) nodeLst.item(i);
 
           NodeList nlsCode = ele.getElementsByTagName("Code");
           Element eleCode = (Element) nlsCode.item(0);
           String strCode = eleCode.getFirstChild().getNodeValue();
 
           NodeList nlsMessage = ele.getElementsByTagName("Message");
           Element eleMsg = (Element) nlsMessage.item(0);
           String strMessage = eleMsg.getFirstChild().getNodeValue();
 
           wsresponse = "Exito: " + strCode + "\n";
           wsresponse = wsresponse + "Mensaje: " + strMessage;
         }
       }
     }
     catch (Exception e) {
       this.error = true;
       wsresponse = e.getMessage().toString();
     }
     return wsresponse;
   }
 
   public String soapMessageToString(SOAPMessage message){
     String result = null;
     if(message != null){
       ByteArrayOutputStream baos = null;
       try{
         baos = new ByteArrayOutputStream();
         message.writeTo(baos);
         result = baos.toString();
       }catch(Exception e){
       }
       finally{
         if(baos != null){
           try{
              baos.close();
           }catch(IOException ioe){
           }
         }
       }
     }
     return result;
   }
 
 }
