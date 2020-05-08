 package me.galaran.mcsniffer;
 
 import java.nio.BufferUnderflowException;
 import java.nio.ByteBuffer;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import me.galaran.mcsniffer.packets.Packet;
 import me.galaran.mcsniffer.packets.PacketValidator;
 import me.galaran.mcsniffer.packets.Packet0BPlayerPosition;
 import me.galaran.mcsniffer.packets.Packet0CPlayerLook;
 import me.galaran.mcsniffer.packets.Packet0DPlayerPositionLook;
 import me.galaran.mcsniffer.packets.Packet33MapChunk;
 import me.galaran.mcsniffer.packets.Packet35BlockChange;
 
 /**
  * Packet format: http://mc.kev009.com/Protocol
  * @author Galaran
  */
 class TrafficHandler {
     
     private static final Logger log = Logger.getLogger("galaran.diamf.diamond_finder");
    private final String handlerType; // use for logging prefix
     
     private final PacketProcessor proc;
     private final Map<Byte, Class<? extends Packet>> targetPackets;
     private static final int MAIN_BUFFER_SIZE = 24 * 1024;
     
     private ByteBuffer mainBuffer = ByteBuffer.allocate(MAIN_BUFFER_SIZE);
 
     public TrafficHandler(PacketProcessor proc, boolean isServerPacketHandler) {
         this.proc = proc;
         this.targetPackets = (isServerPacketHandler ? serverPackets : clientPackets);
        this.handlerType = (isServerPacketHandler ? "[S] " : "[C] ");
     }
     
     public void handle(PcapPacketWrapper packet) {
         if (packet.isEmpty())
             return;
         
        log.log(Level.FINE, handlerType + "Incoming data chunk, size = " + packet.getPayload().length);
         mainBuffer.put(packet.getPayload());
         processBuffer();
     }
     
     private void processBuffer() {
         mainBuffer.flip();
         try {
             while (mainBuffer.position() < mainBuffer.limit() - 1) {
                 Packet probablyPacket = scanAndShiftPos();
                 if (probablyPacket != null) {
                    mainBuffer.compact(); // cut used bytes
                     log.log(Level.INFO, handlerType + "Complete packet {0}", probablyPacket.getClass().getName());
                     proc.processPacket(probablyPacket);
                 }
             }
            mainBuffer.compact(); // cut trash bytes
            log.log(Level.FINE, handlerType + "Buffer pos after compacting: " + mainBuffer.position());
         } catch (NeedMoreBytesException ex) {
             
             // need another fragment
             log.log(Level.INFO, handlerType + "Need another fragment! Shift pos to " + mainBuffer.limit());
             
            // preparing to append bytes to this buffer
             mainBuffer.position(mainBuffer.limit());
             mainBuffer.limit(mainBuffer.capacity());
         }
     }
 
     public Packet scanAndShiftPos() throws NeedMoreBytesException {
         mainBuffer.mark();
         try {
             if (!findAndValidate(mainBuffer))
                 return null;
         } catch (BufferUnderflowException ex) {
             mainBuffer.reset();
             throw new NeedMoreBytesException();
         }
     
         // its valid, construct packet
         mainBuffer.reset();
         Packet newPacket;
         byte code = mainBuffer.get();
         try {
             newPacket = targetPackets.get(code).newInstance();
         } catch (Exception ex) {
             log.log(Level.SEVERE, handlerType + "Error while creating packet instance");
             return null;
         }
         
         try {
             newPacket.readPacket(mainBuffer);
             newPacket.code = code;
         } catch (Exception ex) {
             log.log(Level.SEVERE,  handlerType + "Error while constructing packet " + newPacket.getClass().getName());
             
             newPacket = null;
             // change pos back to packet code + 1
             mainBuffer.reset();
             mainBuffer.get();
         }
         return newPacket;
     }
     
     private boolean findAndValidate(ByteBuffer buff) throws BufferUnderflowException {
         byte packetCode = buff.get();
         if (targetPackets.get(packetCode) == null) // no packet
             return false;
         
         PacketValidator val = validators.get(packetCode);
         return val.validate(buff);
     }
     
     private final static Map<Byte, Class<? extends Packet>> serverPackets = new HashMap<Byte, Class<? extends Packet>>();
     private final static Map<Byte, Class<? extends Packet>> clientPackets = new HashMap<Byte, Class<? extends Packet>>();
     private final static Map<Byte, Class<? extends Packet>> allPackets = new HashMap<Byte, Class<? extends Packet>>();
     
     private final static Map<Byte, PacketValidator> validators = new HashMap<Byte, PacketValidator>();
     
     static {
         serverPackets.put((byte)0x33, Packet33MapChunk.class);
         serverPackets.put((byte)0x35, Packet35BlockChange.class);
         
         clientPackets.put((byte)0x0B, Packet0BPlayerPosition.class);
         clientPackets.put((byte)0x0C, Packet0CPlayerLook.class);
         clientPackets.put((byte)0x0D, Packet0DPlayerPositionLook.class);
         
         allPackets.putAll(serverPackets);
         allPackets.putAll(clientPackets);
         
         for (Map.Entry<Byte, Class<? extends Packet>> entry : allPackets.entrySet()) {
             try {
                 validators.put(entry.getKey(), (PacketValidator) entry.getValue().newInstance());
             } catch (Exception ex) {
                 log.log(Level.SEVERE, "Error while constructing validators map");
                 ex.printStackTrace();
             }
         }
         
     }
     
     public static Map<Byte, Class<? extends Packet>> getPacketMap() {
         return allPackets;
     }
     
 }
