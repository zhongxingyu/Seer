 /******************************************************************************
  * ClientSOAPService.java
  * ****************************************************************************/
 
 /* J_LZ_COPYRIGHT_BEGIN *******************************************************
 * Copyright 2001-2008 Laszlo Systems, Inc.  All Rights Reserved.              *
 * Use is subject to license terms.                                            *
 * J_LZ_COPYRIGHT_END *********************************************************/
 
 package org.openlaszlo.remote.json.soap;
 
 import java.io.*;
 import java.util.*;
 import javax.xml.namespace.QName;
 import org.openlaszlo.sc.ScriptCompiler;
 import org.openlaszlo.utils.*;
 import org.apache.log4j.Logger;
 
 
 public class ClientSOAPService 
 {
     public static Logger mLogger = Logger.getLogger(ClientSOAPService.class);
     StringBuffer body = new StringBuffer();
 
     public ClientSOAPService () { }
 
     public String createObjectProgram(LZSOAPService service) {
 
         // object.__LZstubload
         //------------------------------------------------------------
         // Tells client data returned is an object 
         // this.__LZstubload = true
         body.append("{");
         body.append("\"__LZstubload\": true,");
 
         // SOAP specific information
         // object.stubinfo
         body.append("\"stubinfo\": ");
         {
             body.append("{");
             //------------------------------------------------------------
             // this.service = service.getServiceName()
             body.append("\"service\":" + ScriptCompiler.quote(service.getServiceName()) + ", ");
 
             //------------------------------------------------------------
             // this.port = service.getPort()
             body.append("\"port\": "+ ScriptCompiler.quote(service.getPort()) +",");
 
             //------------------------------------------------------------
             // this.wsdl = service.getWSDL()
             body.append("\"wsdl\": "+ ScriptCompiler.quote(service.getWSDL()) + ",");
 
             //------------------------------------------------------------
             // this.ctypes = <complexTypeInfoObject>
             body.append("\"__LZctypes\": ");
             pushComplexTypeInfo(service.getSchemaComplexTypes());
             body.append(", ");
 
             //------------------------------------------------------------
             // this._namespace = namespace /* target namespace */
             body.append("\"__LZnamespace\": " + 
                         ScriptCompiler.quote(service.getTargetNS()));
 
             body.append("},");
         }
 
         // object.stub
         body.append("\"stub\": ");
         body.append("{");
             
         int count = 0;
         //------------------------------------------------------------
         // Create client-side service operations. Assuming that 
         // operations won't be null.
         //------------------------------------------------------------
         Map operations = service.getOperations();
         Iterator iter = operations.keySet().iterator();
         while (iter.hasNext()) {
 
             String opName = (String)iter.next();
             LZSOAPOperation op = (LZSOAPOperation)operations.get(opName);
 
             if (mLogger.isDebugEnabled()) {
                 mLogger.debug(
                     /* (non-Javadoc)
                      * @i18n.test
                      * @org-mes="adding operation: " + p[0]
                      */
                     org.openlaszlo.i18n.LaszloMessages.getMessage(
                         ClientSOAPService.class.getName(),"051018-99", new Object[] {opName})
                               );
             }
 
             //------------------------------------------------------------
             //
             if (count++ > 0) { body.append(","); }
 
             body.append("\""+opName+"\": ");
 
             //"Multiply": function ()
             // {var args = arguments.callee.args;
             //return LzSOAPService.invoke(arguments[1],
             //arguments[0],
             //args.superclass.requestheaders,
             //{"parts": [["tns:Multiply",
             //               null]],
             // "opstyle": "document",
             // "operation": "Multiply",
             // "port": "MathServiceSoap",
             // "service": "MathService",
             // "wsdl":
             //              "http://www.dotnetjunkies.com/quickstart..../cs/mathservice.asmx?WSDL"},
             // args.superclass.secure,
             //args.superclass.secureport);}
             body.append("{");
             // 6. argument array of parameter type tuples like:
             //     [
             //       [ name1, element1, type1(qname) ], 
             //       [ name2, element2, type2(qname) ]
             body.append("\"parts\": ");
             //     ]
             pushParts(op.getInputMessage(), op.getStyle());
             body.append(",");
             // 5. operation type
             body.append("\"opstyle\": ");
             body.append(ScriptCompiler.quote(op.getStyle()));
             body.append(",");
                         
             // 4. operation name
             body.append("\"operation\": ");
             body.append(ScriptCompiler.quote(opName));
             body.append(",");
 
             // 3. SOAP port
             body.append("\"port\": ");
             body.append(ScriptCompiler.quote(service.getPort()));
             body.append(",");
 
             // 2. SOAP service
             body.append("\"service\": ");
             body.append(ScriptCompiler.quote(service.getServiceName()));
             body.append(",");
 
             // 1. SOAP wsdl
             body.append("\"wsdl\": ");
             body.append(ScriptCompiler.quote(service.getWSDL()));
             body.append("}");
         }
         body.append("}");
         body.append("}");
         return body.toString();
     }
 
     public String pushQName(QName qname) {
         if (qname == null) {
             return "null";
         }
         // this can be optimized
        //return ("new QName("+ScriptCompiler.quote(qname.getLocalPart())+","
        //+ ScriptCompiler.quote(qname.getNamespaceURI())+")");
        return ("[" + ScriptCompiler.quote(qname.getLocalPart())
                + ", " + ScriptCompiler.quote(qname.getNamespaceURI()) +
                "]");
     }
 
 
     void pushParts(LZSOAPMessage inMesg, String style) {
         if (inMesg == null) {
             body.append("null");
             return;
         }
 
         body.append("[");
         List parts = inMesg.getParts();
         int item = 0;
         for (int i = 0; i < parts.size(); i++) {
             if (item++ > 0) {
                 body.append(",");
             }
 
             LZSOAPPart part = (LZSOAPPart)parts.get(i);
             String name = part.getName();
             String element = part.getElement();
             ComplexType type = part.getType();
             QName typeName = ( type != null ? type.getName() : null );
             body.append("[");
             if (style.equals("rpc")) {
                 // rpc calls use the name of the part as the name of element.
                 body.append(ScriptCompiler.quote(name));
             } else {
                 // documents use element name
                 body.append(ScriptCompiler.quote(element));
             }
             body.append(",");
             body.append(pushQName(typeName));
             body.append("]");
         }
         body.append("]");
     }
 
     /**
      * @param program Program
      * @param ctm complex type map
      */
     public  void pushComplexTypeInfo(Map ctm) {
         if (ctm == null) {
             body.append("null");
             return;
         }
 
         Iterator iter = ctm.entrySet().iterator();
         if (! iter.hasNext()) {
             body.append("null");
             return;
         }
 
         body.append("{");
         int count  = 0;
         while (iter.hasNext()) {
             Map.Entry entry = (Map.Entry)iter.next();
             ComplexType ct = (ComplexType)entry.getValue();
             if (count > 0) {
                 body.append(", ");
             }
             if ((ct.getName().getLocalPart()) != null &&
                 !((ct.getName().getLocalPart()).equals(""))) {
 
                 mLogger.debug("pushComplexTypeInfo ct: "+ct.getName().getLocalPart()+" ,"
                               + ct.getName() + ", "+ct);
 
                 count++;
                 body.append(ScriptCompiler.quote(ct.getName().getLocalPart())+": ");
                 body.append("{");
                 // namespace
                 body.append("\"ns\": ");
                 body.append(ScriptCompiler.quote(ct.getName().getNamespaceURI()));
                 body.append(",");
 
                 // type is one of simple, complex, array
                 body.append("\"type\": ");
                 body.append(ScriptCompiler.quote(ct.getTypeString()));
                 body.append(",");
 
                 // QName for array type; null if complex type is not an array
                body.append("\"typeQ\": ");
                 body.append(pushQName(ct.getArrayItemTypeQName()));
                 body.append(",");
 
                 // push members
                body.append("\"members\": ");
                 pushMembers(ct);
                 body.append(",");
 
                 // push base
                 ComplexType base = ct.getBase();
                 QName baseQName = (base != null ? base.getArrayItemTypeQName() : null);
                body.append("\"base\": ");
                 body.append(pushQName(baseQName));
 
                 body.append("}");
             } 
         }
         body.append("}");
 
     }
 
 
     /** 
      * Push members of a complex type. 
      */
      void pushMembers(ComplexType ct) {
         Map members = ct.getMembers();
         if (members == null) {
             body.append("null");
             return;
         }
 
         Iterator iter = members.entrySet().iterator();
         if (! iter.hasNext()) {
             body.append("null");
             return;
         }
 
         int count = 0;
         body.append("{");
         while (iter.hasNext()) {
             if (count > 0) {
                 body.append(",");
             }
             Map.Entry entry = (Map.Entry)iter.next();
             String key = (String)entry.getKey();
             QName value = (QName)entry.getValue();
             body.append(ScriptCompiler.quote(key)+": ");
             body.append(pushQName(value));
             count++;
         }
 
         ComplexType baseType = ct.getBase();
         if (baseType != null) {
             count += pushBaseMembers(baseType);
         }
         body.append("}");
 
     }
 
 
     /** 
      * Push base members and returns count of members added. Helper method for
      * pushMembers().
      * @return count of members pushed
      */
      int pushBaseMembers(ComplexType ct) {
         
         Map members = ct.getMembers();
         if (members == null) return 0;
 
         Iterator iter = members.entrySet().iterator();
         if (! iter.hasNext()) return 0;
 
         int count = 0;
         while (iter.hasNext()) {
             if (count > 0) {
                 body.append(",");
             }
             Map.Entry entry = (Map.Entry)iter.next();
             String key = (String)entry.getKey();
             QName value = (QName)entry.getValue();
             body.append(key+": ");
             body.append(pushQName(value));
             count++;
         }
 
         ComplexType baseType = ct.getBase();
         if (baseType != null) {
             count += pushBaseMembers(baseType);
         }
         return count;
     }
     
 
     /**
      */
     public  String createObjectFile(LZSOAPService service)
     {
         // Create FlashFile object nd include action bytes
         body = new StringBuffer();
         createObjectProgram(service);
         return body.toString();
     }
 
     /**
      */
     public static byte[] createObject(LZSOAPService service)
     {
         ClientSOAPService cobj = new ClientSOAPService();
         String json = cobj.createObjectFile(service);
         mLogger.debug("createObjectFile: "+json);
         try {
             byte[] buf = json.getBytes("UTF8");
             return buf;
         }
         catch (UnsupportedEncodingException e) {
             throw new ChainedException(e);
         }
     }
 
 }
