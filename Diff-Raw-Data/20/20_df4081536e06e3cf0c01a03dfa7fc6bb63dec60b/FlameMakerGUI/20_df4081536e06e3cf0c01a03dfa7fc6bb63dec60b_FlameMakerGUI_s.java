 package ch.epfl.flamemaker.gui;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.TreeSet;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import ch.epfl.flamemaker.color.Color;
 import ch.epfl.flamemaker.color.InterpolatedPalette;
 import ch.epfl.flamemaker.color.Palette;
 import ch.epfl.flamemaker.flame.Flame;
 import ch.epfl.flamemaker.flame.FlameTransformation;
 import ch.epfl.flamemaker.geometry2d.AffineTransformation;
 import ch.epfl.flamemaker.geometry2d.Point;
 import ch.epfl.flamemaker.geometry2d.Rectangle;
 
 public class FlameMakerGUI {
 
 	private Flame.Builder flameBuilder;
 	private Color backgroundColor;
 	private Palette palette;
 	private Rectangle frame;
 	private int density;
 	
 	private int m_selectedTransformationId;
 	private Set<Listener> m_listeners = new TreeSet<Listener>();
 	
 	public FlameMakerGUI() {
 		// Tableau des transformations
 		ArrayList<FlameTransformation> transformations = new ArrayList<FlameTransformation>();
 
 		// Définition des transformations
 		transformations.add(new FlameTransformation(new AffineTransformation(
 				-0.4113504, -0.7124804, -0.4, 0.7124795, -0.4113508, 0.8),
 				new double[] { 1, 0.1, 0, 0, 0, 0 }));
 
 		transformations.add(new FlameTransformation(new AffineTransformation(
 				-0.3957339, 0, -1.6, 0, -0.3957337, 0.2), new double[] { 0, 0,
 				0, 0, 0.8, 1 }));
 
 		transformations.add(new FlameTransformation(new AffineTransformation(
 				0.4810169, 0, 1, 0, 0.4810169, 0.9), new double[] { 1, 0, 0, 0,
 				0, 0 }));
 		flameBuilder = new Flame.Builder(new Flame(transformations));
 		backgroundColor = Color.BLACK;
 		
 		ArrayList<Color> paletteColors = new ArrayList<Color>();
 		paletteColors.add(Color.RED);
 		paletteColors.add(Color.GREEN);
 		paletteColors.add(Color.BLUE);
 		palette = new InterpolatedPalette(paletteColors);
 		
 		frame = new Rectangle(new Point(-0.25, 0), 5, 4);
 		
 		density = 50;
 		
 	}
 	
 	public void start() {
 		final FlameMakerGUI self = this;
 		
 		JFrame window = new JFrame("FlameMaker");
 		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		window.getContentPane().setLayout(new BorderLayout());
 		
 		JPanel	upperPanel = new JPanel(), 
 				lowerPanel = new JPanel(),
 				transformationsEditPanel = new JPanel(),
 				transformationsPreviewPanel = new JPanel(), 
 				transformationsEditButtons = new JPanel(),
 				fractalPanel = new JPanel();
 		
 		final AffineTransformationsComponent affineTransformationComponent = new AffineTransformationsComponent(flameBuilder, frame);
 		
 		/* Upper panel */
 		window.getContentPane().add(upperPanel, BorderLayout.CENTER);
 		upperPanel.setLayout(new GridLayout(1, 2));
 		upperPanel.add(transformationsPreviewPanel);
 		upperPanel.add(fractalPanel);
 		
		transformationsPreviewPanel.setBackground(new java.awt.Color(9, 9, 9, 10));
 		transformationsPreviewPanel.setLayout(new BorderLayout());
 		transformationsPreviewPanel.setBorder(BorderFactory.createTitledBorder("Transformations"));
 		
 		this.addListener(new Listener(){
 
 			@Override
 			public void onSelectedTransformationIdChange(int id) {
 				System.out.println("Selected : "+id);
 				affineTransformationComponent.highlightedTransformationIndex(id);
 			}
 			
 		});
 		fractalPanel.setLayout(new BorderLayout());
 		fractalPanel.setBorder(BorderFactory.createTitledBorder("Fractale"));
 	
 		
 		transformationsPreviewPanel.add(affineTransformationComponent, BorderLayout.CENTER);
 		fractalPanel.add(new FlameBuilderPreviewComponent(flameBuilder, backgroundColor, palette, frame, density), BorderLayout.CENTER);
 		
 		/* Lower panel */
 		window.getContentPane().add(lowerPanel, BorderLayout.PAGE_END);
 		lowerPanel.setLayout(new BoxLayout(lowerPanel, BoxLayout.PAGE_AXIS));
 		
 		// Panneau d'édition des transformations
 		lowerPanel.add(transformationsEditPanel);
 		lowerPanel.add(transformationsEditButtons);
 		transformationsEditPanel.setLayout(new BorderLayout());
 		
 		flameBuilder.addTransformation(new FlameTransformation(new AffineTransformation(1, 0, 1, 0, 0.6, 0), new double[]{1, 0, 0, 0, 0.6, 0}));
 		
 		final TransformationsListModel listModel = new TransformationsListModel(flameBuilder);
		JList transformationsList = new JList(listModel);
 		transformationsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		transformationsList.setVisibleRowCount(3);
 		transformationsList.setSelectedIndex(0);
 		transformationsList.addListSelectionListener(new ListSelectionListener(){
 			@Override
 			public void valueChanged(ListSelectionEvent evt) {
				self.setSelectedTransformationId(evt.getFirstIndex());
 			}
 		});
 		JScrollPane transformationsPane = new JScrollPane(transformationsList);
 		
 		transformationsEditPanel.add(transformationsPane, BorderLayout.CENTER);
 		
 		transformationsEditButtons.setLayout(new GridLayout(1, 2));
 
 		final 
 		JButton deleteTransformationButton = new JButton("Supprimer");
 		deleteTransformationButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				int selectedIndex = self.getSelectedTransformationId();
 				if(selectedIndex != -1) {
 					System.out.println("Removing "+selectedIndex);
 					listModel.removeTransformation(selectedIndex);
 				}
 				if(self.flameBuilder.transformationsCount() == 1) {
 					deleteTransformationButton.setEnabled(false);
 				}
 			}
 		});
 		
 		JButton addTransformationButton = new JButton("Ajouter");
 		transformationsEditButtons.add(addTransformationButton);
 		transformationsEditButtons.add(deleteTransformationButton);
 		
 		
 		addTransformationButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				listModel.addTransformation(new FlameTransformation(
 						AffineTransformation.IDENTITY, 
 						new double[]{1, 0, 0, 0, 0, 0}						
 				));
 				self.setSelectedTransformationId(self.flameBuilder.transformationsCount()-1);
 				if(!deleteTransformationButton.isEnabled() && self.flameBuilder.transformationsCount() > 1) {
 					deleteTransformationButton.setEnabled(true);
 				}
 			}
 		});
 		
 		window.pack();
 		window.setVisible(true);
 	}
 	
 	/**
 	 * Gets the currently selected transformation id
 	 */
 	public int getSelectedTransformationId(){
 		return m_selectedTransformationId;
 	}
 	
 	/**
 	 * Sets the currently selected transformation id
 	 * @param id of the transformation
 	 */
 	public void setSelectedTransformationId(int id){
 		m_selectedTransformationId = id;
 
 		Iterator<Listener> it = m_listeners.iterator();
 		while(it.hasNext()){
 			it.next().onSelectedTransformationIdChange(id);
 		}
 	}
 	
 	/**
 	 * Adds a listener notified when a global GUI value change (ie. selectedTransformationId)
 	 * @param l listener to add
 	 * @see #removeListener
 	 */
 	public void addListener(Listener l){
 		m_listeners.add(l);
 	}
 	
 	/**
 	 * Removes a listener.
 	 * @param l listener to remove
 	 * @see #addListener
 	 */
 	public void removeListener(Listener l){
 		m_listeners.remove(l);
 	}
 	
 	/**
 	 *	Interface for GUI listeners.
 	 */
 	public interface Listener{
 		public void onSelectedTransformationIdChange(int id);
 	}
 }
