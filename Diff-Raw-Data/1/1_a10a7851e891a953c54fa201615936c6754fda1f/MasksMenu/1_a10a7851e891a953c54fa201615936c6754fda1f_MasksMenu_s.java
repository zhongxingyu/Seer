 package sturla.atitp.frontend.menus.masksMenu;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JMenu;
 import javax.swing.JMenuItem;
 
 import sturla.atitp.frontend.MainFrame;
 import sturla.atitp.frontend.imageops.masks.FourMaskA;
 import sturla.atitp.frontend.imageops.masks.FourMaskC;
 import sturla.atitp.frontend.imageops.masks.FourMaskKirsh;
 import sturla.atitp.frontend.imageops.masks.GaussianFilterOperation;
 import sturla.atitp.frontend.imageops.masks.HighPassOperation;
 import sturla.atitp.frontend.imageops.masks.LaplaceMask;
 import sturla.atitp.frontend.imageops.masks.LogMask;
 import sturla.atitp.frontend.imageops.masks.LowPassOperation;
 import sturla.atitp.frontend.imageops.masks.MaskA;
 import sturla.atitp.frontend.imageops.masks.MaskC;
 import sturla.atitp.frontend.imageops.masks.MaskD;
 import sturla.atitp.frontend.imageops.masks.MaskKirsh;
 import sturla.atitp.frontend.imageops.masks.MedianPassOperation;
 
 public class MasksMenu extends JMenu {
 
 	private static final long serialVersionUID = 1L;
 	private MainFrame mainFrame;
 
 	public MasksMenu(MainFrame parent) {
 		super("Masks");
 		
 		JMenuItem highPassMask = new JMenuItem("HighPass");
 		highPassMask.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				mainFrame.currOperation = new HighPassOperation();
 				mainFrame.hideSliders();
 				mainFrame.displayTextFields(true);
 			}
 		});
 		JMenuItem lowPassMask = new JMenuItem("LowPass");
 		lowPassMask.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				mainFrame.currOperation = new LowPassOperation();
 				mainFrame.hideSliders();
 				mainFrame.displayTextFields(true);
 			}
 		});
 		JMenuItem medianPassMask = new JMenuItem("MedianPass");
 		medianPassMask.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				mainFrame.currOperation = new MedianPassOperation();
 				mainFrame.hideSliders();
 				mainFrame.displayTextFields(true);
 			}
 		});
 		
 		JMenuItem gaussianFilterMask = new JMenuItem("Gaussian Filter");
 		gaussianFilterMask.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				mainFrame.currOperation = new GaussianFilterOperation();
 				mainFrame.hideSliders();
 				mainFrame.displayTextFields(true);
 				mainFrame.value2.setVisible(true);
 			}
 		});
 		JMenuItem maskA = new JMenuItem("Mask A");
 		maskA.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				mainFrame.currOperation = new MaskA();
 				mainFrame.hideSliders();
 				mainFrame.displayTextFields(true);
 			}
 		});
 		JMenuItem maskA4 = new JMenuItem("Mask A: 4 dirs");
 		maskA4.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				mainFrame.currOperation = new FourMaskA();
 				mainFrame.hideSliders();
 				mainFrame.displayTextFields(false);
 				mainFrame.rectangle.setVisible(false);
 			}
 		});
 		JMenuItem maskKirsh = new JMenuItem("Mask Kirsh");
 		maskKirsh.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				mainFrame.currOperation = new MaskKirsh();
 				mainFrame.hideSliders();
 				mainFrame.displayTextFields(true);
 			}
 		});
 		JMenuItem maskKirsh4 = new JMenuItem("Mask Kirsh: 4 dirs");
 		maskKirsh4.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				mainFrame.currOperation = new FourMaskKirsh();
 				mainFrame.hideSliders();
 				mainFrame.displayTextFields(false);
 				mainFrame.rectangle.setVisible(false);
 			}
 		});
 		JMenuItem maskC = new JMenuItem("Mask C");
 		maskC.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				mainFrame.currOperation = new MaskC();
 				mainFrame.hideSliders();
 				mainFrame.displayTextFields(true);
 			}
 		});
 		JMenuItem maskC4 = new JMenuItem("Mask C: 4 dirs");
 		maskC4.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				mainFrame.currOperation = new FourMaskC();
 				mainFrame.hideSliders();
 				mainFrame.displayTextFields(false);
 				mainFrame.rectangle.setVisible(false);
 			}
 		});
 		JMenuItem maskD = new JMenuItem("Mask D");
 		maskD.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				mainFrame.currOperation = new MaskD();
 				mainFrame.hideSliders();
 				mainFrame.displayTextFields(true);
 			}
 		});
 		
 		JMenuItem maskD4 = new JMenuItem("Mask D: 4 dirs");
 		maskD4.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				mainFrame.currOperation = new FourMaskC();
 				mainFrame.hideSliders();
 				mainFrame.displayTextFields(false);
 				mainFrame.rectangle.setVisible(false);
 			}
 		});
 		
 		JMenuItem lmask = new JMenuItem("Mask Laplace");
 		lmask.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				mainFrame.currOperation = new LaplaceMask();
 				mainFrame.hideSliders();
 				mainFrame.displayTextFields(true);
 				mainFrame.value2.setVisible(true);
 			}
 		});
 		JMenuItem logmask = new JMenuItem("Mask LOG");
 		logmask.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent event) {
 				mainFrame.currOperation = new LogMask();
 				mainFrame.hideSliders();
 				mainFrame.displayTextFields(true);
 				mainFrame.value1.setVisible(true);
 				mainFrame.value2.setVisible(true);
 			}
 		});
 		
 		this.add(highPassMask);
 		this.add(lowPassMask);
 		this.add(medianPassMask);
 		this.add(gaussianFilterMask);
 		this.add(maskA);
 		this.add(maskA4);
 		this.add(maskKirsh);
 		this.add(maskKirsh4);
 		this.add(maskC);
 		this.add(maskC4);
 		this.add(maskD);
 		this.add(maskD4);
 		this.add(lmask);
 		this.add(logmask);
 		
 	}
 }
