 package protex;
 
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
 import javax.swing.JApplet;
import javax.swing.JPanel;
 
 public class ProtexApplet extends JApplet {

 	private FoldingWindow foldingWindow;
 	private Protex protex;

 	public void init() {
 		protex = new Protex(true);
		foldingWindow = new FoldingWindow("Folding Window", protex);
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridLayout(1,2));

		JPanel aapPanel = new JPanel();
		aapPanel.setBorder(BorderFactory.createTitledBorder("Amino acids"));
		aapPanel.setLayout(new BoxLayout(aapPanel, BoxLayout.X_AXIS));
		AminoAcidPalette aaPalette = new AminoAcidPalette(225, 180, 4, 5, false);
		aapPanel.setMaximumSize(new Dimension(250, 200));
		aapPanel.add(aaPalette);
		aapPanel.add(Box.createRigidArea(new Dimension(1,180)));
		mainPanel.add(aapPanel);
		mainPanel.add(foldingWindow);

		getContentPane().add(mainPanel);
 	}
 
 }
