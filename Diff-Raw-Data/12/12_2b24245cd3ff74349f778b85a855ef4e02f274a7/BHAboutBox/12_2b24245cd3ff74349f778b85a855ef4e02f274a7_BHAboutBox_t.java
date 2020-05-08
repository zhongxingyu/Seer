 package org.bh.gui.swing;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
import javax.swing.WindowConstants;
 
 import org.bh.platform.PlatformKey;
 import org.bh.platform.Services;
 import org.bh.platform.i18n.ITranslator;
 
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.FormLayout;
 
 public class BHAboutBox extends JDialog implements ActionListener {
 
	private JButton ok;
	private final ITranslator translator = Services.getTranslator();
 
 	public BHAboutBox(JFrame contentFrame) {
 		super(contentFrame, true);
 		this.initialize(contentFrame);
 	}
 
	private void initialize(JFrame frame) {
 		String rowDef = "2px,p,4px,p,8px,p,14px,p,4px";
 		String colDef = "2px,40px,p:grow,2px";
 
 		FormLayout layout = new FormLayout(colDef, rowDef);
 
 		CellConstraints cons = new CellConstraints();
 
 		this.setLayout(layout);
 		this.setTitle(this.translator.translate(PlatformKey.HELPINFO));
		this.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
 		this.ok = new JButton(this.translator.translate("Bokay"));
 		this.ok.addActionListener(this);
 		
 		ImageIcon image = new ImageIcon(BHAboutBox.class.getResource("/org/bh/images/AboutBox.jpg"));
 		int x = (frame.getWidth() - 480) / 2;
 		int y = (frame.getHeight() - 600) / 2;
 
 		this.add(new JLabel(image), cons.xywh(2, 2, 2, 1));
 		this.add(new JLabel("<html><body>" + translator.translate("website")
 				+ ": " + translator.translate("website_long")
 				+ "</body></html>"), cons.xy(3, 4));
 		this.add(
 				new JLabel("<html><body>" + translator.translate("email")
 						+ ": " + translator.translate("email_long")
 						+ "</body></html>"), cons.xy(3, 6));
 		this.add(this.ok, cons.xywh(2, 8, 2, 1, "center, center"));
 		this.setLocation(x, y);
 		this.setResizable(false);
 		this.pack();
 		this.setVisible(true);
 
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		this.dispose();
 	}
 }
