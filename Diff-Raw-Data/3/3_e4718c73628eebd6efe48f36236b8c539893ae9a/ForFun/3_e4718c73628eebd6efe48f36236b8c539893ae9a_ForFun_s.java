 import java.io.* ;
 import java.awt.* ;
 import java.awt.event.* ;
 import java.awt.image.* ;
 
 public abstract class ForFun extends Frame {
   protected static class DrawArea extends Panel {
     private int width;
     private int height; 
 
     public BufferedImage screen_buffer;
 
     public DrawArea(int width, int height) {
       this.width = width;
       this.height = height;
       screen_buffer = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
     }
 
     public void paint(Graphics g) {
//      screen_buffer.getGraphics().drawLine(0,0,300,300);
       g.drawImage(screen_buffer,0,0, this);
     }
 
     public Dimension getPreferredSize() {
       return new Dimension(this.width,this.height) ;
     }
   }
 
   public ForFun(int width, int height) {
     super("ForFun");
       final ForFun frame = this;
      final DrawArea drawing = new DrawArea(width, height);
      Panel buttonPanel = new Panel() ;
      buttonPanel.setLayout(new BorderLayout()) ;
      Button quitButton = new Button("Quit") ;
      buttonPanel.add("Center",quitButton) ;
      frame.setLayout(new BorderLayout()) ;
      frame.add("Center",drawing) ;
      frame.add("South",buttonPanel) ;
      
      quitButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt)  {
          quit() ;
        }
      }) ;
 
      frame.addWindowListener(new WindowAdapter() {
        public void windowClosing(WindowEvent evt) {
          quit() ;
        }
      }) ;
 
      frame.pack() ;
      frame.setVisible(true) ;
       Thread updater = new Thread() {
         public void run() {
             try { Thread.sleep(100); 
           DataOutputStream outputStream = new DataOutputStream(new FileOutputStream("/dev/stdout"));
           while(true) {
             DataBufferInt buffer =((DataBufferInt)drawing.screen_buffer.getRaster().getDataBuffer());
             int[] pixels = buffer.getData();
             frame.process(pixels);
             for(int i = 0; i < pixels.length; i++) {
               int r = (pixels[i] >> 16) & 0xFF;
               int g = (pixels[i] >> 8) & 0xFF;
               int b = (pixels[i] >> 0) & 0xFF;
               outputStream.writeByte(r);
               outputStream.writeByte(g);
               outputStream.writeByte(b);
               buffer.setElem(i, pixels[i]);
             }
 
             outputStream.flush();
 
           }
         } catch(Exception ex) { ex.printStackTrace();}
         }
       };
       updater.start();
   } 
 
   public abstract void process(int[] pixels);
 
   private static void quit() {
     System.exit(0) ;
   }
   }
