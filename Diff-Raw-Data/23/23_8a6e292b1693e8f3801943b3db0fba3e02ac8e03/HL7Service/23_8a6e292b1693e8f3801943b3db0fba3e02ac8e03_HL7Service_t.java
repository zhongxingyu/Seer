 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.abada.eva.hl7.service;
 
 import ca.uhn.hl7v2.HL7Exception;
 import ca.uhn.hl7v2.model.Message;
 import ca.uhn.hl7v2.parser.GenericParser;
 import java.io.*;
 
 /**
  *
  * @author jesus
  */
 public class HL7Service {
     
     private GenericParser parser;
         
     public HL7Service(){
         this.parser = new GenericParser();
         parser.setXMLParserAsPrimary();
     }
     
     public Message buildMessage(String msg) throws HL7Exception{
         
         parser.setXMLParserAsPrimary();
         Message m = parser.parse(msg);
         
         return m;
 
     }
     
     public Message createAckComunicationPositive(Message msg) throws Exception{
         
         return  this.createAck(msg, "CA", null);
         
         
     }
     
     public Message createAckComunicationError(Message msg, HL7Exception e) throws Exception{
         return this.createAck(msg, "CE", e);
     }
     public Message createAckComunicationReject(Message msg, String error) throws Exception{
         return this.createAck(msg, "CR",new HL7Exception(error));
     }
     
     public String createAckComunicationRejectAsString(Message msg, String error) throws Exception{
         return parser.encode(this.createAckComunicationReject(msg, error));    
     }
     
    public String createAckComunicationErrorAsString(Message msg, HL7Exception e) throws Exception{
        return parser.encode(this.createAckComunicationError(msg, e));
    }
    
     public String createAckComunicationPositiveAsString(Message msg) throws Exception{
         return parser.encode(this.createAckComunicationPositive(msg));
     }
     
      public Message createAckPositive(Message msg) throws Exception{
         
         return  this.createAck(msg, null, null);
         
         
     }
      
     
     public String createAckPositiveAsString(Message msg) throws Exception{
         return parser.encode(this.createAckPositive(msg));
     }
     
     
     public Message createAck(Message msg, String code, HL7Exception e) throws Exception{
         
         Message ack = msg.generateACK(code, e);
         
         return ack;
     }
     
     public String createAckAsString(Message msg, String code, HL7Exception e) throws Exception{
         return parser.encode(this.createAck(msg, code, e));
     }
    //TODO remove
    /*public void prueba() throws HL7Exception, IOException, ClassNotFoundException {
 
         String msg = "MSH|^~\\&|HIS|System|Hosp|HL7 Genie|20071016055244||ACK^A01|A234242|P|2.5|\r\n" + "MSA|AA|234242|Message Received Successfully|";
         
         String xml = "<ADT_A01 xmlns=\"urn:hl7-org:v2xml\">"+
     "<MSH>"+
         "<MSH.1>|</MSH.1>"+
         "<MSH.2>^~\\&amp;</MSH.2>"+
         "<MSH.3>"+
             "<HD.1>TestSendingSystem</HD.1>"+
         "</MSH.3>"+
         "<MSH.7>"+
             "<TS.1>200701011539</TS.1>"+
         "</MSH.7>"+
         "<MSH.9>"+
             "<MSG.1>ADT</MSG.1>"+
             "<MSG.2>A01</MSG.2>"+
             "<MSG.3>ADT A01</MSG.3>"+
         "</MSH.9>"+
 	"<MSH.12>2.5</MSH.12>"+
         "<MSH.13>123</MSH.13>"+
     "</MSH>"+
     "<PID>"+
         "<PID.3>"+
             "<CX.1>123456</CX.1>"+
         "</PID.3>"+
         "<PID.5>"  +                                                                                                                                                                  
             "<XPN.1>"+                                                                                                                                                                
                " <FN.1>Doe</FN.1>"+                                                                                                                                                   
             "</XPN.1>"+                                                                                                                                                               
             "<XPN.2>John</XPN.2>"+                                                                                                                                                    
         "</PID.5>"+                                                                                                                                                                   
     "</PID>"+                                                                                                                                                                         
 "</ADT_A01>";
         
         GenericParser p = new GenericParser();
         p.setXMLParserAsPrimary();
         //EncodingDetector.is
         Message m = p.parse(msg);
        System.out.println(p.encode(m.generateACK()));
        System.out.println(m.generateACK().encode());
         //System.out.println("Esto es la salida: -----"+m);
       
     }
 
     private byte[] serialize(Object o) throws IOException {
 
         ByteArrayOutputStream bs = new ByteArrayOutputStream();
         ObjectOutputStream os = new ObjectOutputStream(bs);
         os.writeObject(o);  // this es de tipo DatoUdp
         os.close();
         return bs.toByteArray(); // devuelve byte[]
 
     }
 
     private Object unserialize(byte[] b) throws IOException, ClassNotFoundException {
 
         ByteArrayInputStream bs = new ByteArrayInputStream(b); // bytes es el byte[]
         ObjectInputStream is = new ObjectInputStream(bs);
         Object obj =  is.readObject();
         is.close();
         
         return obj;
         
    }*/
 }
