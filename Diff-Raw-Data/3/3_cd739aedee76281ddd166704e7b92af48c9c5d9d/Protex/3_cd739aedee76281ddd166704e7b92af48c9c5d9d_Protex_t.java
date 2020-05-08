 package biochem;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.DefaultListModel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import molGenExp.ColorModel;
 import molGenExp.CombinedColorPanel;
 import molGenExp.MolGenExp;
 import molGenExp.Organism;
 
 
 public class Protex extends JPanel {
 
 	private FoldingWindow upperFoldingWindow;
 	private FoldingWindow lowerFoldingWindow;
 	private ProteinHistoryList proteinHistoryList;
 	private JScrollPane histListScrollPane;
 	private ProteinHistListControlPanel proteinHistListControlPanel;
 	private CombinedColorPanel combinedColorPanel;
 	
 	ColorModel colorModel;
 
 	ProteinPrinter printer;
 
 	File outFile;
 
 	private MolGenExp mge;
 
 	public Protex(MolGenExp mge) {
 		super();
 		this.mge = mge;
 		colorModel = mge.getOverallColorModel();
 		printer = new ProteinPrinter();
 		outFile = null;
 		setupUI();
 	}
 
 
 	class ApplicationCloser extends WindowAdapter {
 		public void windowClosing(WindowEvent e) {
 			System.exit(0);
 		}
 	}
 
 	private void setupUI() {
 
 		JPanel leftPanel = new JPanel();
 		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
 		leftPanel.add(Box.createRigidArea(new Dimension(200,1)));
 		JPanel aapPanel = new JPanel();
		aapPanel.setLayout(new BoxLayout(aapPanel, BoxLayout.X_AXIS));
 		aapPanel.setBorder(BorderFactory.createTitledBorder("Amino acids"));
 		AminoAcidPalette aaPalette 
 		= new AminoAcidPalette(180, 225, 5, 4, colorModel);
 		aapPanel.setMaximumSize(new Dimension(200, 250));
		aapPanel.add(Box.createRigidArea(new Dimension(1,225)));
 		aapPanel.add(aaPalette);
 
 		JPanel histListPanel = new JPanel();
 		histListPanel.setBorder(
 				BorderFactory.createTitledBorder("History List"));
 		histListPanel.setLayout(new BoxLayout(histListPanel, BoxLayout.Y_AXIS));
 		proteinHistListControlPanel = new ProteinHistListControlPanel(this);
 		histListPanel.add(proteinHistListControlPanel);
 		proteinHistoryList = new ProteinHistoryList(
 				new DefaultListModel(), mge);
 		histListScrollPane = new JScrollPane(proteinHistoryList);
 //		histListScrollPane.setPreferredSize(new Dimension(200,1000));
 		histListPanel.add(histListScrollPane);
 
 		leftPanel.add(aapPanel);
 		leftPanel.add(histListPanel);
 
 		JPanel rightPanel = new JPanel();
 		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
 		upperFoldingWindow = new FoldingWindow("Upper Folding Window", this, colorModel);
 		lowerFoldingWindow = new FoldingWindow("Lower Folding Window", this, colorModel);
 		rightPanel.add(upperFoldingWindow);
 		combinedColorPanel = new CombinedColorPanel();
 		rightPanel.add(combinedColorPanel);
 		rightPanel.add(lowerFoldingWindow);
 
 		setButtonsEnabled(false);
 
 		JPanel mainPanel = new JPanel();
 
 		mainPanel.setLayout(
 				new BoxLayout(mainPanel, BoxLayout.X_AXIS));
 		mainPanel.add(leftPanel);
 		mainPanel.add(rightPanel);
 
 		setLayout(new BorderLayout());
 		add(mainPanel, BorderLayout.CENTER);
 	}
 	
 	public FoldingWindow getUpperFoldingWindow() {
 		return upperFoldingWindow;
 	}
 	
 	public FoldingWindow getLowerFoldingWindow() {
 		return lowerFoldingWindow;
 	}
 
 	public void addFoldedToHistList(FoldedPolypeptide fp) {
 		proteinHistoryList.add(fp);
 		histListScrollPane.revalidate();
 		histListScrollPane.repaint();
 		updateCombinedColor();
 	}
 
 	public void updateCombinedColor() {
 		Color u = upperFoldingWindow.getColor();
 		Color l = lowerFoldingWindow.getColor();
 		Color combined = colorModel.mixTwoColors(u, l);
 		combinedColorPanel.setCombinedColor(combined);
 	}
 
 	public void sendSelectedFPtoUP() {
 		if (proteinHistoryList.getSelectedValue() != null) {
 			FoldedPolypeptide fp =
 				(FoldedPolypeptide) proteinHistoryList.getSelectedValue();
 			upperFoldingWindow.setFoldedPolypeptide(fp);
 		}
 	}
 
 	public void sendSelectedFPtoLP() {
 		if (proteinHistoryList.getSelectedValue() != null){
 			FoldedPolypeptide fp =
 				(FoldedPolypeptide) proteinHistoryList.getSelectedValue();
 			lowerFoldingWindow.setFoldedPolypeptide(fp);
 		}
 	}
 
 
 	
 	public void loadOrganism(Organism o) {
 		upperFoldingWindow.setFoldedPolypeptide(
 				o.getGene1().getFoldedPolypeptide());
 		lowerFoldingWindow.setFoldedPolypeptide(
 				o.getGene2().getFoldedPolypeptide());
 	}
 
 	public void setButtonsEnabled(boolean b) {
 		proteinHistListControlPanel.setButtonsEnabled(b);
 	}
 }
