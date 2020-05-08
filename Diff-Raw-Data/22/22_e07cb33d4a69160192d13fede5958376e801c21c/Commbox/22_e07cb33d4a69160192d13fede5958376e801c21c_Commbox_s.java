 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package jm.device.v1;
 
 import java.io.IOException;
 import jm.device.ConnectorType;
 import jm.device.IComm;
 import jm.device.ProtocolType;
 
 /**
  *
  * @author Ogilvy
  */
 public class Commbox extends jm.device.Commbox {
     private Box _box;
     private Shared _shared;
     
     public Commbox() {
     }
     
     @Override
     public void open() throws IOException {
         _shared = new Shared();
         _box = new Box(getPort(), _shared);
         if (!_box.openComm())
             throw new IOException();
     }
 
     @Override
     public void close() throws IOException {
         if (!_box.closeComm())
             throw new IOException();
     }
 
     @Override
     public IComm<?> configure(ProtocolType type) throws IOException {
         if (type.equals(ProtocolType.ISO14230)) {
            return new Comm<ISO14230>(_box, new ISO14230(_box, _shared));
         } else if (type.equals(ProtocolType.ISO15765)) {
             ISO15765 p = new ISO15765(_box, _shared);
            IComm comm = new Comm<ISO15765>(_box, p);
             p.prepare();
             return comm;
         } else if (type.equals(ProtocolType.KWP1281)) {
             return new KWP1281Comm(_box, new KWP1281(_box, _shared));
         } else if (type.equals(ProtocolType.MIKUNI)) {
            return new Comm<Mikuni>(_box, new Mikuni(_box, _shared));
         }
         return null;
     }
 
     @Override
     public void setConnector(ConnectorType type) throws IOException {
         _shared.connector = type;
     }
     
 }
