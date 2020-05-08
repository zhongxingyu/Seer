 package com.epita.mti.plic.opensource.controlibserversample.view;
 
 import com.epita.mti.plic.opensource.controlibserver.qrcode.QrcodeGenerator;
 import com.epita.mti.plic.opensource.controlibserversample.ServerSample;
 import com.epita.mti.plic.opensource.controlibserversample.controller.SelectInterfaceAction;
 import com.google.zxing.WriterException;
 import java.awt.*;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.net.SocketException;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  *
  * @author Julien "Roulyo" Fraisse
  */
 public class QRCodeView extends Frame
 {
 
   private Image img;
   private HashMap<String, HashMap<String, String>> interfaces;
 
   public QRCodeView() throws SocketException, WriterException
   {
     super("QRCode");
 
     Choice selector = new Choice();
     Dimension dim = getToolkit().getScreenSize();
     MediaTracker mt = new MediaTracker(this);
 
     interfaces = ServerSample.getConnectionManager().getInterfaces();
     for (Map.Entry<String, HashMap<String, String>> entry : interfaces.entrySet())
     {
       String string = entry.getKey();
       selector.add(string);
     }
 
    String ip = ServerSample.getConnectionManager().getInterfaces().get(selector.getSelectedItem()).get("IPV4");
    img = QrcodeGenerator.generateQrcode(ip, ServerSample.PORT, 200, 200);
     mt.addImage(img, 0);
 
     setLayout(null);
     setSize(220, 245);
     setLocation((dim.width - getBounds().width) / 2, (dim.height - getBounds().height) / 2);
     setVisible(true);
 
     addWindowListener(new WindowAdapter()
     {
 
       @Override
       public void windowClosing(WindowEvent we)
       {
         dispose();
       }
     });
 
     selector.setSize(200, 20);
     selector.setLocation(10, 215);
     selector.addItemListener(new SelectInterfaceAction(this));
  
     add(selector);
   }
 
   @Override
   public void update(Graphics g)
   {
     paint(g);
   }
 
   @Override
   public void paint(Graphics g)
   {
     if (img != null)
     {
       g.drawImage(img, 10, 10, this);
     }
     else
     {
       g.clearRect(0, 0, getSize().width, getSize().height);
     }
   }
 
  public void setQrcode(String ninterface) throws WriterException, SocketException
   {
    String ip = ServerSample.getConnectionManager().getInterfaces().get(ninterface).get("IPV4");
    img = QrcodeGenerator.generateQrcode(ip, ServerSample.PORT, 200, 200);
     update(getGraphics());
   }
 }
