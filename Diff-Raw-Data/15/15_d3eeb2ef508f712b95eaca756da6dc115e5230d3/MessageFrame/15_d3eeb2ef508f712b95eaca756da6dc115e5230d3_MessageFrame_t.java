 package interpret.views;
 
 import java.io.ByteArrayOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.PrintStream;
 
 import java.awt.*;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JTextArea;
 import javax.swing.SwingUtilities;
 import javax.swing.border.EtchedBorder;
 
 /**
  * 標準出力を表示するためのクラス */
 class MessageFrame extends JFrame {
 
   private static final long serialVersionUID = -3941456441208116678L;
 
   private JLabel title;
   private JTextArea msgArea;
 
   MessageFrame() {
     super("Message");
     setSize(500, 600);
     setResizable(false);
     setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
     title = new JLabel("メッセージ");
     title.setFont(new Font("Arial", Font.BOLD, 20));
     msgArea = new JTextArea();
     msgArea.setEditable(false);  // ReadOnly に
     msgArea.setPreferredSize(new Dimension(400, 550));
     msgArea.setBorder(new EtchedBorder(EtchedBorder.RAISED));
     JTextAreaStream stream = new JTextAreaStream(msgArea);
    PrintStream pStream = new PrintStream(stream, true); // true は AutoFlush の設定
    System.setOut(pStream);
    System.setErr(pStream);
 
     setLayout(new BorderLayout());
 
     add("North", title);
     add("South", msgArea);
 
     System.out.println("message test");
 
     setVisible(true);
   }
 
   public class JTextAreaStream extends OutputStream {
     private JTextArea _area;
     private ByteArrayOutputStream _buf;
 
     public JTextAreaStream(JTextArea area) {
       _area = area;
       _buf = new ByteArrayOutputStream();
     }
 
     @Override
     public void write(int b) throws IOException {
       _buf.write(b);
     }
 
     @Override
     public void flush() throws IOException {
       // Swing のイベントスレッドにのせる
       SwingUtilities.invokeLater(new Runnable() {
         public void run() {
           _area.append(_buf.toString());
           _buf.reset();
         }
       });
     }
   }
 }
