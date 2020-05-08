 package whiteboard.app;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.geom.AffineTransform;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 
 import javax.swing.AbstractAction;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFileChooser;
 import javax.swing.JFormattedTextField;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 
 import net.miginfocom.swing.MigLayout;
 import whiteboard.SerialController;
 import whiteboard.WhiteboardPanel;
 import whiteboard.WhiteboardParser;
 import whiteboard.svgreader.SvgReader;
 
 public class SelectPanel extends JPanel {
     
     private final JFormattedTextField reelSpacingField = new JFormattedTextField(500.0);
     private final JFormattedTextField reelRadiusField = new JFormattedTextField(25.0);
     private final JFormattedTextField pulsesPerRotationField = new JFormattedTextField(282.0);
     private final JFormattedTextField pulleyRadiusField = new JFormattedTextField(0.0);
     private final JFormattedTextField startXField = new JFormattedTextField(250.0);
     private final JFormattedTextField startYField = new JFormattedTextField(220.0);
     private final JTextField portField = new JTextField("/dev/ttyUSB0");
     private final JTextField filenameField = new JTextField();
     private final JButton chooseFileButton 
             = new JButton(new AbstractAction("Choose file") {
         @Override
         public void actionPerformed(ActionEvent ev) {
             JFileChooser chooser = new JFileChooser();
             int ret = chooser.showOpenDialog(SelectPanel.this);
             if (ret == JFileChooser.APPROVE_OPTION)
                 filenameField.setText(chooser.getSelectedFile().getPath());
         }
     });
     private final JButton previewButton
             = new JButton(new AbstractAction("Preview") {
         @Override
         public void actionPerformed(ActionEvent ev) {
             try {
                 String uri = new File(filenameField.getText()).toURI().toASCIIString();
                 PreviewPanel preview = new PreviewPanel(new SvgReader(uri));
                 JDialog d = new JDialog(SwingUtilities.getWindowAncestor(SelectPanel.this));
                 d.setContentPane(preview);
                 d.pack();
                 d.setVisible(true);
             } catch (IOException e) {
                 e.printStackTrace(); // todo: show a dialog
             }
         }
     });
     private final JButton simulateButton 
             = new JButton(new AbstractAction("Simulate") {
         @Override
         public void actionPerformed(ActionEvent ev) {
             try {
                 byte[] data = createOutput();
                 
                 WhiteboardPanel p = new WhiteboardPanel(
                         (Double) reelRadiusField.getValue(),
                         (Double) reelSpacingField.getValue(),
                         1024
                 );
                 
                 JDialog d = new JDialog(SwingUtilities.getWindowAncestor(SelectPanel.this));
                 d.setContentPane(p);
                 d.setSize(600, 600);
                 d.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                 d.setVisible(true);
                 
                 new WhiteboardParser(p, data).start();
             } catch (IOException e) {
                 e.printStackTrace(); // todo: show a dialog
             }
         }
     });
     
     private final JButton runButton 
             = new JButton(new AbstractAction("Go!") {
         @Override
         public void actionPerformed(ActionEvent ev) {
             try {
                 final byte[] data = createOutput();
                 SerialController controller = new SerialController(portField.getText());
                 final RunPanel p = new RunPanel(controller);
                 JDialog d = new JDialog(SwingUtilities.getWindowAncestor(SelectPanel.this));
                 d.setContentPane(p);
                 d.pack();
                 d.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                 d.setVisible(true);
                 p.getStartButton().addActionListener(new ActionListener() {
                     @Override
                     public void actionPerformed(ActionEvent arg0) {
                         new WhiteboardParser(p, data).start();
                     }
                 });
             } catch (IOException e) {
                 e.printStackTrace(); // todo: show a dialog
             }
         }
         });
 
     public SelectPanel() {
         super(new MigLayout("", "[right]rel[300lp,grow,fill]"));
         
         add(new JLabel("Filename"));
         add(filenameField, "grow, span");
         add(chooseFileButton, "skip 1, wrap, grow 0");
         add(new JLabel("Reel radius"));
         add(reelRadiusField, "wrap");
        add(new JLabel("Pulses per rotation"));
         add(pulsesPerRotationField, "wrap");
         add(new JLabel("Pulley radius"));
         add(pulleyRadiusField, "wrap");
         add(new JLabel("Reel spacing"));
         add(reelSpacingField, "wrap");
         add(new JLabel("Start position left"));
         add(startXField, "wrap");
         add(new JLabel("Start position down"));
         add(startYField, "wrap");
         add(new JLabel("Port"));
         add(portField, "wrap");
         add(previewButton, "span, split 2");
         add(runButton, "wrap");
     }
     
     private byte[] createOutput() throws IOException {
         Converter c = new Converter();
         c.setReelRadius((Double) reelRadiusField.getValue());
         c.setReelSpacing((Double) reelSpacingField.getValue());
         c.setPulsesPerRotation((Double) pulsesPerRotationField.getValue());
         c.setStartX((Double) startXField.getValue());
         c.setStartY((Double) startYField.getValue());
         
         String uri = new File(filenameField.getText()).toURI().toASCIIString();
         SvgReader reader = new SvgReader(uri);
         double scale = c.getReelSpacing() / reader.getSize().getWidth();
         AffineTransform t = AffineTransform.getScaleInstance(scale, scale);
 
         ByteArrayOutputStream out = new ByteArrayOutputStream();
         c.convert(reader.getPathIterator(t), out);
         
         return out.toByteArray();
     }
 
     public static void main(String[] args) {
         try {
             UIManager.setLookAndFeel(
                     UIManager.getSystemLookAndFeelClassName());
         } catch (Exception e) {}
 
         JFrame f = new JFrame();
         f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         f.setContentPane(new SelectPanel());
         f.pack();
         f.setVisible(true);
     }
 }
