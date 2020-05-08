 package umk.neural.network.identify.numbers.gui;
 
 import java.awt.BorderLayout;
 import java.awt.FileDialog;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import umk.neural.network.identify.numbers.activators.Activator;
 import umk.neural.network.identify.numbers.activators.IdentityActivator;
 import umk.neural.network.identify.numbers.perceptrons.BasicPerceptron;
 import umk.neural.network.identify.numbers.perceptrons.Perceptron;
 
 public class MainWindow extends JFrame {
 
 	private static final long serialVersionUID = 44202514566339254L;
 
 	private JPanel patternPresenterContainer = new JPanel();
 	private JPanel controlPanelContainer = new JPanel();
 	private Patterns patterns = new Patterns();
 	private JList patternsList = new JList(patterns);
 	private JPatternPresenter patternPresenter;
 
 	private List<Perceptron> perceptrons;
 	private Random r = new Random();
 
 	public static void main(String[] args) {
 		new MainWindow();
 	}
 	
 	private ActionListener click = new ActionListener() {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			/**
 			 * Check if perceptrons is null
 			 */
 			if(perceptrons == null)
 				return;
 			int index = check();
 			String name = patterns.get(index).getName();
 			System.out.println("Detected pattern #" + index + " " + name);
 		}
 	};
 
 	public MainWindow() {
 		super("Neurony");
 		buildUI();
 		pack();
 		setSize(640, 480);
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		setVisible(true);
 	}
 
 	private void buildUI() {
 		setLayout(new BorderLayout());
 		add(patternPresenterContainer);
 		patternPresenterContainer.setLayout(new BorderLayout());
 		replacePatternPresenter(new PixelPattern<Boolean>(5, 8));
 		controlPanelContainer.setLayout(new BoxLayout(controlPanelContainer, BoxLayout.Y_AXIS));
 		add(controlPanelContainer, BorderLayout.EAST);
 		buildControlPanel();
 	}
 
 	private void buildControlPanel() {
 		JScrollPane patternsScroller = new JScrollPane(patternsList);
 		patternsList.addListSelectionListener(new ListSelectionListener() {
 			@Override
 			public void valueChanged(ListSelectionEvent e) {
 				try {
 					PixelPattern<Boolean> pattern = (PixelPattern<Boolean>) patternsList.getSelectedValue();
 					if (pattern != null)
 						replacePatternPresenter(pattern);
 				} catch (IndexOutOfBoundsException ex) {
 				}
 			}
 		});
 		controlPanelContainer.add(patternsScroller);
 		JButton newPatern = new JButton("New patern");
 		newPatern.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				replacePatternPresenter(new PixelPattern<Boolean>(5, 8));
 			}
 		});
 		controlPanelContainer.add(newPatern);
 		JButton addPattern = new JButton("Add pattern");
 		addPattern.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				PixelPattern<Boolean> pattern = patternPresenter.getPattern();
 				if (patterns.hasPattern(pattern)) {
 					JOptionPane.showMessageDialog(MainWindow.this, "Pattern already on the list");
 				} else {
 					String name = JOptionPane.showInputDialog(MainWindow.this, "Enter new pattern name");
 					if (name == null) {
 						return;
 					}
 					pattern.setName(name);
 					patterns.addPattern(pattern);
 				}
 			}
 		});
 		controlPanelContainer.add(addPattern);
 		JButton delPattern = new JButton("Del pattern");
 		delPattern.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				int index = patternsList.getSelectedIndex();
 				if (index == -1) {
 					return;
 				}
 				patterns.delPattern(index);
 			}
 		});
 		controlPanelContainer.add(delPattern);
 
 		JButton learn = new JButton("Learn");
 		learn.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				String input = JOptionPane.showInputDialog(MainWindow.this, "Enter number of iterations");
 				learnAll(Integer.parseInt(input));
 			}
 		});
 		controlPanelContainer.add(learn);
 		JButton check = new JButton("Check");
 		check.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				int index = check();
 				String name = patterns.get(index).getName();
 				JOptionPane.showMessageDialog(MainWindow.this, "Detected pattern #" + index + " " + name);
 			}
 		});
 		controlPanelContainer.add(check);
 
 /*		JButton save = new JButton("Save");
 		save.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				try {
 					FileDialog saveDialog = new FileDialog(MainWindow.this, "Save patterns", FileDialog.SAVE);
 					saveDialog.setVisible(true);
 					File file = new File(saveDialog.getFile());
 					FileOutputStream fos = new FileOutputStream(file);
 					ObjectOutputStream oos = new ObjectOutputStream(fos);
 					oos.writeObject(patterns);
 					oos.flush();
 					oos.close();
 				} catch (Exception ex) {
 					ex.printStackTrace();
 				}
 			}
 		});
 		controlPanelContainer.add(save);
 
 		JButton load = new JButton("Load");
 		load.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				try {
 					FileDialog loadDialog = new FileDialog(MainWindow.this, "Save patterns", FileDialog.LOAD);
 					loadDialog.setVisible(true);
 					File file = new File(loadDialog.getFile());
 					FileInputStream fis = new FileInputStream(file);
 					ObjectInputStream ois = new ObjectInputStream(fis);
 					patterns = (Patterns) ois.readObject();
 					patternsList.setModel(patterns);
 					ois.close();
 				} catch (Exception ex) {
 					ex.printStackTrace();
 				}
 			}
 		});
 		controlPanelContainer.add(load);*/
 	}
 
 	private void replacePatternPresenter(PixelPattern<Boolean> pixelPattern) {
 		patternPresenter = new JPatternPresenter(pixelPattern, click);
 		patternPresenterContainer.removeAll();
 		patternPresenterContainer.add(patternPresenter, BorderLayout.CENTER);
 		patternPresenterContainer.validate();
 	}
 
 	public void learnAll(int iterations) {
 		Activator activator = new IdentityActivator();
 
 		double[] weights = new double[5 * 8];
 		for (int i = 0; i < 5 * 8; i++) {
			//weights[i] = 0.5;
 			weights[i] = 0.5;
 		}
 		/**
 		 * Create list of perceptrons 
 		 */
 		perceptrons = new ArrayList<Perceptron>();
 		for (int p = 0; p < patterns.getSize(); p++) {
 			Perceptron perceptron = new BasicPerceptron();
 			perceptron.setActivator(activator);
 			perceptron.setWeights(weights);
 			perceptrons.add(perceptron);
 		}
 
 		for (int p = 0; p < patterns.getSize(); p++) {
 			Perceptron perceptron = perceptrons.get(p);
 			PixelPattern<Boolean> pattern = patterns.get(p);
 			double score = eval(perceptron, pattern);// calc first score
 			for (int i = 0; i < iterations; i++) {
 				score = learn(perceptron, pattern, score);
 			}
 		}
 	}
 
 	private double learn(Perceptron p, PixelPattern<Boolean> pattern, double oldScore) {
 		double[] oldWeights = p.getWeights();
 		
 		/**
 		 * 33% probability of modify in pattern
		 * 10%
 		 */
 		PixelPattern<Boolean> modifiedPattern = pattern.copy();
		if(r.nextInt(100) < 10) {
 			int index = r.nextInt(modifiedPattern.getPixels().size());
 			Boolean pixel = modifiedPattern.getPixels().get(index);
 			modifiedPattern.getPixels().set(index, !pixel);
 		}
 
 		double[] newWeights = new double[modifiedPattern.getPixels().size()];
 		for (int i = 0; i < modifiedPattern.getPixels().size(); i++) {
 			newWeights[i] = r.nextDouble();
 		}
 		p.setWeights(newWeights);
 		double newScore = eval(p, modifiedPattern);
 		if (newScore > oldScore) {
 			return newScore;
 		} else {
 			p.setWeights(oldWeights);
 			return oldScore;
 		}
 	}
 
 	public double eval(Perceptron p, PixelPattern<Boolean> pattern) {
 		double[] in = new double[pattern.getPixels().size()];
 		for (int i = 0; i < pattern.getPixels().size(); i++) {
 			Boolean b = pattern.getPixel(i);
 			if (Boolean.TRUE.equals(b)) {
 				in[i] = 1d;
 			} else {
 				in[i] = -1d;
 			}
 		}
 		return p.eval(in);
 	}
 
 	public int check() {
 		PixelPattern<Boolean> pattern = patternPresenter.getPattern();
 		Perceptron perceptron = perceptrons.get(0);
 		
 		double score = eval(perceptron, pattern);
 		int index = 0;
 		System.out.println("No. " + index + " -> " + score);
 		for (int i = 1; i < perceptrons.size(); i++) {
 			
 			double newScore = eval(perceptrons.get(i), pattern);
 			System.out.println("No. " + i + " -> " + newScore);
 			if (newScore > score) {
 				score = newScore;
 				index = i;
 			}
 		}
 		return index;
 	}
 }
