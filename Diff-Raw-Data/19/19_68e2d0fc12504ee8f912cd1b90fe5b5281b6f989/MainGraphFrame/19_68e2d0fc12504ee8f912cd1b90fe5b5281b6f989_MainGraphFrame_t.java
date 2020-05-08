 import java.awt.Dimension;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionListener;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.AbstractList;
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.Scanner;
 
 import javax.imageio.ImageIO;
 import javax.naming.NoInitialContextException;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import com.sun.xml.internal.bind.v2.schemagen.xmlschema.List;
 
 public class MainGraphFrame extends JFrame implements ActionListener, ChangeListener,
 		MouseMotionListener {
 
 	/**
 	 * LinkedList
 	 */
 	private static final long serialVersionUID = 1L;
 
 	protected FourierFrame fFrame;
 
 	protected GraphPanel upperPanel;
 
 	protected InstrumentPanel buttons;
 
 	private FourierMenuBar menu;
 
 	public LabelPanel labels;
 
 	public int sampleRate;
 
 	private File curFileIn;
 	protected boolean isDancing;
 
 	protected boolean noFile;
 
 	protected AbstractList<Integer> listOfSampleValues;
 	protected ArrayList<Integer> listOfFourierValues;
 
 	public MainGraphFrame(File in) throws FileNotFoundException {
 		super("Fourier Transform Display - ComS 229");
 		// TODO get this out
 		in = new File("src/twoTones.txt");
 		Image cornerImage = null;
 		try {
 			cornerImage = ImageIO.read(new File("image.bmp"));
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 
 		setIconImage(cornerImage);
 
 		if (in == null) {
 			noFile = true;
 			curFileIn = null;
 		} else {
 			noFile = false;
 			curFileIn = in;
 		}
 		if (!noFile) {
 			listOfSampleValues = getListOfPointsFromFile(curFileIn);
 			listOfFourierValues = getListOfFourierPointsFromSamplePoints(
 					listOfSampleValues, 256);
 		}
 		isDancing = false;
 
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 		labels = new LabelPanel(this);
 		menu = new FourierMenuBar(this);
 		fFrame = new FourierFrame(this);
 		buttons = new InstrumentPanel(this);
 		upperPanel = new GraphPanel(this);
 		upperPanel.addMouseMotionListener(this);
 
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 
 		JPanel mainPanel = new JPanel();
 
 		setPreferredSize(new Dimension(1024, 730));
 
 		setJMenuBar(menu);
 		mainPanel.add(upperPanel);
 		mainPanel.add(buttons);
 		mainPanel.add(fFrame);
 		mainPanel.add(labels);
 
 		add(mainPanel);
 
 		pack();
 		// setResizable(false);
 		setVisible(true);
 	}
 
 	public static void main(String[] args) throws FileNotFoundException {
 		new MainGraphFrame(null);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getActionCommand().equals("exit")) {
 			System.exit(0);
 		} else if (e.getActionCommand().equals("openFile")) {
 			JFileChooser chooser = new JFileChooser();
 
 			int returnVal = chooser.showOpenDialog(null);
 			if (returnVal == JFileChooser.APPROVE_OPTION) {
 				curFileIn = chooser.getSelectedFile();
 			} else {
 				System.out.println("You chose not to save");
 				return;
 			}
 			listOfSampleValues = getListOfPointsFromFile(curFileIn);
 			listOfFourierValues = getListOfFourierPointsFromSamplePoints(
 					listOfSampleValues, 256);
 			noFile = false;
 			fFrame.repaint();
 			upperPanel.repaint();
 			labels.resetLabels(sampleRate);
 			buttons.resetSlider();
 		} else if (e.getActionCommand().equals("dance")) {
 			isDancing = !isDancing;
 		} else if (e.getActionCommand().equals("foward")) {
 			buttons.slide.setValue(buttons.slide.getValue()
 					+ buttons.setJumpVal.getValue());
 		} else if (e.getActionCommand().equals("back")) {
 			buttons.slide.setValue(buttons.slide.getValue()
 					- buttons.setJumpVal.getValue());
 		}
 
 		repaint();
 	}
 
 	private AbstractList<Integer> getListOfPointsFromFile(File fin) {
 		Scanner in = null;
 		try {
 			in = new Scanner(fin);
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		sampleRate = Integer.parseInt(in.nextLine().substring(1).trim());
 		in.nextLine();
 		ArrayList<Integer> ret = new ArrayList<Integer>();
 		while (in.hasNext()) {
 			int next = in.nextInt();
 			ret.add(next);
 		}
 
 		return ret;
 	}
 
 	public ArrayList<Integer> getListOfFourierPointsFromSamplePoints(
 			AbstractList<Integer> intsIn, int startWindow) {
 
 		ArrayList<Integer> ret = new ArrayList<Integer>();
 		int[] ret2 = new int[256];
 		for (int i = startWindow; i < startWindow + 256; i++) {
 			ret2[i - startWindow] = intsIn.get(i);
 		}
 
 		for (int i = 0; i < 128; i++) {
			ret.add(FourierProgram.fourier(ret2, i));
 		}
 
 		return ret;
 	}
 
 	public void setFourierVals(ArrayList<Integer> arr) {
 		listOfFourierValues = arr;
 	}
 
 	@Override
 	public void stateChanged(ChangeEvent e) {
 		if (buttons == null)
 			return;
 		if (e.getSource() == buttons.slide) {
 			fFrame.repaint();
 			upperPanel.repaint();
 		}
 		if (e.getSource() == buttons.setJumpVal) {
 			buttons.shiftViewBack.setText("Move Window Back "
 					+ buttons.setJumpVal.getValue());
 			buttons.shiftViewFoward.setText("Move Window Foward "
 					+ buttons.setJumpVal.getValue());
 			buttons.shiftViewBack.repaint();
 			buttons.shiftViewFoward.repaint();
 			fFrame.repaint();
 			upperPanel.repaint();
 		}
 
 	}
 
 	public int getSampleRate() {
 		return sampleRate;
 	}
 
 	public void mouseDragged(MouseEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void mouseMoved(MouseEvent e) {
 		upperPanel.setToolTipText(upperPanel.getToolTipText(e));
 		fFrame.setToolTipText(fFrame.getToolTipText(e));
 	}
 
 }
