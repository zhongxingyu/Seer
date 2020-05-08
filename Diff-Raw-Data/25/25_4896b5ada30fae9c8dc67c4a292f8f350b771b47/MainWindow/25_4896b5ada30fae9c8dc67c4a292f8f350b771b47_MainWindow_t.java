 package gui;
 
 import gui.IPropertyProvider.FieldInformation;
 import gui.PropertyPanel.IValueModificationEventHandler;
 
 import java.awt.*;
 import java.awt.event.*;
 
 import javax.swing.*;
 import javax.swing.UIManager.LookAndFeelInfo;
 import javax.swing.border.*;
 import javax.swing.event.*;
 
 import main.*;
 import net.miginfocom.swing.*;
 
 public class MainWindow extends JFrame implements ActionListener,
 		IValueModificationEventHandler {
 	private static final long serialVersionUID = 1L;
 	private JPanel contentPane;
 	private static MainFrame mainFrame;
 	private Canvas canvas;
 	private JPanel panel;
 	private JPanel panel_1;
 	private JPanel panel_2;
 	private JButton btnAdd;
 	private JButton btnRemove;
 	private JPanel panel_3;
 	private JList<UIEmitter> list;
 	private JScrollPane scrollPane;
 	private PropertyPanel propertyPanel;
 	private JPanel panel_4;
 	private DefaultListModel<UIEmitter> model;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		try {
 			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
 				if ("Nimbus".equals(info.getName())) {
 					UIManager.setLookAndFeel(info.getClassName());
 					break;
 				}
 			}
 		} catch (Exception e) {
 		}
 		EventQueue.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					MainWindow frame = new MainWindow();
 					frame.setExtendedState(Frame.MAXIMIZED_BOTH);
 					frame.setVisible(true);
 					frame.canvas.setFocusable(true);
 					frame.canvas.requestFocus();
 					frame.canvas.setIgnoreRepaint(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the frame.
 	 */
 	public MainWindow() {
 		setTitle("Particle Editor");
 		addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent arg0) {
 				EntryPoint.running = false;
 				arg0.getWindow().dispose();
 			}
 		});
 		addComponentListener(new ComponentAdapter() {
 			@Override
 			public void componentShown(ComponentEvent arg0) {
 				new Thread() {
 					@Override
 					public void run() {
 						new EntryPoint().init(canvas);
 					}
 				}.start();
 				while (MainWindow.mainFrame == null) {
 					try {
 						Thread.sleep(100);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 
 			@Override
 			public void componentResized(ComponentEvent arg0) {
 				if (mainFrame != null)
 					mainFrame.resized = true;
 			}
 		});
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		float ratio = 16f / 9f;
 		int width = 900;
 		int height = (int) (900f / ratio);
 		this.setSize(827, 624);
 		this.setSize(width, height);
 		this.setLocationRelativeTo(null);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(new BorderLayout(0, 0));
 
 		canvas = new Canvas();
 		contentPane.add(canvas, BorderLayout.CENTER);
 
 		panel = new JPanel();
 		contentPane.add(panel, BorderLayout.EAST);
 		panel.setLayout(new BorderLayout(0, 0));
 
 		panel_1 = new JPanel();
 		panel.add(panel_1, BorderLayout.NORTH);
 		panel_1.setLayout(new BorderLayout(0, 0));
 
 		panel_2 = new JPanel();
 		panel_1.add(panel_2, BorderLayout.SOUTH);
 
 		btnAdd = new JButton("Add");
 		btnAdd.addActionListener(this);
 		panel_2.add(btnAdd);
 
 		btnRemove = new JButton("Remove");
 		btnRemove.setEnabled(false);
 		btnRemove.addActionListener(this);
 		panel_2.add(btnRemove);
 
 		panel_3 = new JPanel();
 		panel_1.add(panel_3, BorderLayout.NORTH);
 		panel_3.setLayout(new MigLayout("", "[350px,grow]", "[200.00,grow]"));
 
 		model = new DefaultListModel<UIEmitter>();
 		list = new JList<UIEmitter>(model);
 		list.addListSelectionListener(new ListSelectionListener() {
 			@Override
 			public void valueChanged(ListSelectionEvent arg0) {
 				if (list.getSelectedValue() != null) {
 					btnRemove.setEnabled(true);
 					propertyPanel.setObject(list.getSelectedValue());
 				} else {
 					btnRemove.setEnabled(false);
 					propertyPanel.setObject(null);
 				}
 			}
 		});
 		list.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null,
 				null));
 		panel_3.add(list, "cell 0 0,grow");
 
 		panel_4 = new JPanel();
 		panel_4.setBorder(new TitledBorder(null, "Emitter Settings",
 				TitledBorder.LEADING, TitledBorder.TOP, null, null));
 		panel.add(panel_4, BorderLayout.CENTER);
 		panel_4.setLayout(new BorderLayout(0, 0));
 
		propertyPanel = new PropertyPanel() {
			private static final long serialVersionUID = 1L;

			@Override
			public void setPreferredSize(Dimension d) {
				int width = (int) (scrollPane.getViewport().getWidth() - scrollPane
						.getVerticalScrollBar().getSize().getWidth());
				super.setPreferredSize(new Dimension(width, (int) d.getHeight()));
				scrollPane.updateUI();
			}
		};
 		propertyPanel.addValueModificationEventHandler(this);
		scrollPane = new JScrollPane(propertyPanel,
				JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		panel_4.add(scrollPane);
 
 	}
 
 	public static void setMainFrame(MainFrame mainFrame) {
 		MainWindow.mainFrame = mainFrame;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		if (arg0.getSource().equals(btnAdd)) {
 			model.addElement(new UIEmitter());
 			Emitter[] emitters = new Emitter[model.size()];
 			for (int i = 0; i < emitters.length; i++) {
 				emitters[i] = model.get(i).getEmitter();
 			}
 			mainFrame.emitters = emitters;
 		}
 		if (arg0.getSource().equals(btnRemove)) {
 			model.removeElement(list.getSelectedValue());
 			Emitter[] emitters = new Emitter[model.size()];
 			for (int i = 0; i < emitters.length; i++) {
 				emitters[i] = model.get(i).getEmitter();
 			}
 			mainFrame.emitters = emitters;
 		}
 	}
 
 	private void updateEmitters() {
 		Emitter[] emitters = new Emitter[model.size()];
 		for (int i = 0; i < emitters.length; i++) {
 			emitters[i] = model.get(i).getEmitter();
 		}
 		mainFrame.emitters = emitters;
 	}
 
 	@Override
 	public void valueModified(FieldInformation fi, PropertyPanel sender) {
 		this.list.repaint();
 		updateEmitters();
 	}
 }
