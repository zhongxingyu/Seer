 /*
  * PduFactory.java
  *
  * Created on July 16, 2007, 9:15 PM
  *
  * To change this template, choose Tools | Template Manager
  * and open the template in the editor.
  */
 
 package edu.nps.moves.disutil;
 
 import java.io.*;
 
 import edu.nps.moves.dis.*;
 import edu.nps.moves.disenum.PduType;
 
 /**
  * Simple factory for PDUs. When bytes are received on the wire, they're passed off to us
  * and the correct constructor called to return the correct PDU type.<p>
  *
  * @author mcgredo
  */
 public class PduFactory
 {
     private boolean useFastPdu = false;
     
     /** Creates a new instance of PduFactory */
     public PduFactory()
     {
         this(false);
     }
     
     /** 
      * Create a new PDU factory; if true is passed in, we use "fast PDUs",
      * which minimize the memory garbage generated at the cost of being
      * somewhat less pleasant to work with.
      */
     public PduFactory(boolean useFastPdu)
     {
         this.useFastPdu = useFastPdu;
     }
     
     /** 
      * PDU factory. Pass in an array of bytes, get the correct type of pdu back,
      * based on the PDU type field contained in the byte array.
      */
     public Pdu createPdu(byte data[])
     {
         // Promote a signed byte to an int, then do a bitwise AND to wipe out everthing but the 
         // first eight bits. This effectively lets us read this as an unsigned byte
         int pduType = 0x000000FF & (int)data[2]; // The pdu type is a one-byte, unsigned byte in the third byte position.
         
         // Do a lookup to get the enumeration instance that corresponds to this value. 
         PduType pduTypeEnum = PduType.lookup[pduType];
         
         DataInputStream dis = new DataInputStream(new ByteArrayInputStream(data));
         Pdu aPdu = null;
         
         switch(pduTypeEnum)
         {
             case ESPDU: 
                // if the user has created the factory requesting that he get fast espdus back, give him those.
                if(useFastEspdu == true)
                {
                   aPdu = new FastEntityStatePdu();
                }
                else
                {
                   aPdu = new EntityStatePdu();
                }

                 aPdu.unmarshal(dis);
                 break;
                 
             case FIRE_PDU: 
                 aPdu = new FirePdu();
                 aPdu.unmarshal(dis);
                 break;
                 
             case DETONATION_PDU:
                 aPdu = new DetonationPdu();
                 aPdu.unmarshal(dis);
                 break;
             
             case SERVICE_REQUEST_PDU:
                 aPdu = new ServiceRequestPdu();
                 aPdu.unmarshal(dis);
                 break;
                 
             case RESUPPLY_OFFER_PDU:
                 aPdu = new ResupplyOfferPdu();
                 aPdu.unmarshal(dis);
                 break;
                 
             case RESUPPLY_RECEIVED_PDU:
                 aPdu = new ResupplyReceivedPdu();
                 aPdu.unmarshal(dis);
                 break;
                 
             case RESUPPLY_CANCEL_PDU:
                 aPdu = new ResupplyCancelPdu();
                 aPdu.unmarshal(dis);
                 break;
                 
             case REPAIR_COMPLETE_PDU:
                 aPdu = new RepairCompletePdu();
                 aPdu.unmarshal(dis);
                 break;
                 
             case REPAIR_RESPONSE_PDU:
                 aPdu = new RepairResponsePdu();
                 aPdu.unmarshal(dis);
                 break;
                 
             case CREATE_ENTITY_PDU:
                 aPdu = new CreateEntityPdu();
                 aPdu.unmarshal(dis);
                 break;
                 
             case REMOVE_ENTITY_PDU:
                 aPdu = new RemoveEntityPdu();
                 aPdu.unmarshal(dis);
                 break;
                 
             case START_RESUME_PDU:
                 aPdu = new StartResumePdu();
                 aPdu.unmarshal(dis);
                 break;
                 
             case STOP_FREEZE_PDU:
                 aPdu = new StopFreezePdu();
                 aPdu.unmarshal(dis);
                 break;
                 
             case ACKNOWLEDGE_PDU:
                 aPdu = new AcknowledgePdu();
                 aPdu.unmarshal(dis);
                 break;
                 
             case ACTION_REQUEST_PDU:
                 aPdu = new ActionRequestPdu();
                 aPdu.unmarshal(dis);
                 break;
             
             default: 
                 System.out.println("PDU not implemented");
                 
         }
         
         return aPdu;
     }
     
 }
