 package ClassAdminFrontEnd;
 
 import java.awt.BasicStroke;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.HeadlessException;
 import java.awt.RenderingHints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.awt.image.BufferedImage;
 import java.awt.image.BufferedImageOp;
 import java.io.IOException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.LinkedList;
 
 import javax.imageio.ImageIO;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JEditorPane;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.Timer;
 
 import jxl.biff.drawing.ComboBox;
 
 import org.imgscalr.Scalr;
 import org.imgscalr.Scalr.Method;
 import org.imgscalr.Scalr.Mode;
 
 import ClassAdminBackEnd.AbsentException;
 import ClassAdminBackEnd.EntityType;
 import ClassAdminBackEnd.Project;
 import ClassAdminBackEnd.RapidAssessmentComponentType;
 import ClassAdminBackEnd.RapidAssessmentContainerType;
 import ClassAdminBackEnd.RapidAssessmentMarkType;
 import ClassAdminBackEnd.RapidAssessmentRectangleType;
 import ClassAdminBackEnd.SuperEntity;
 
 public class RapidAssessmentMarkingCanvas extends JFrame {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private Project project;
 	private BufferedImage backGround;
 	private BufferedImage resizedBackGround = null;
 	private ContentPanel contentPanel;
 	private JFrame parentFrame;
 	private JComponent parentRect;
 	private int focusedMark = 0;
 	private LinkedList<MyMark> markList = new LinkedList<RapidAssessmentMarkingCanvas.MyMark>();
 
 	private JTextField searchBox;
 	private JLabel searchLabel, loadAssessmentLabel;
 	private JComboBox studentChooser;
 	private JComboBox assessmentChooser;
 	private JLabel[] infoLabels;
 	private JComboBox loadCombo;
 	private JButton btnLoad;
 	private JLabel timerLabel;
 	private Timer timer;
 	private Date time;
 
 	public void refreshButtons() {
 		contentPanel.add(searchBox);
 		contentPanel.add(searchLabel);
 		contentPanel.add(studentChooser);
 		searchLabel.setBounds(parentFrame.getWidth() - 145, 20, 130, 30);
 		searchBox.setBounds(parentFrame.getWidth() - 145, 50, 130, 30);
 		studentChooser.setBounds(parentFrame.getWidth() - 145, 80, 130, 30);
 
		loadAssessmentLabel.setBounds(parentFrame.getWidth() - 145, 200, 130,
 				30);
 		contentPanel.add(loadAssessmentLabel);
		loadCombo.setBounds(parentFrame.getWidth() - 145, 230, 130, 30);
 		contentPanel.add(loadCombo);
		btnLoad.setBounds(parentFrame.getWidth() - 145, 260, 130, 30);
 		contentPanel.add(btnLoad);
 		contentPanel.add(timerLabel);
 		timerLabel.setBounds(parentFrame.getWidth() - 145, parentFrame.getHeight()-100, 130, 30);
 
 		for (int x = 0; x < infoLabels.length; ++x) {
			infoLabels[x].setBounds(parentFrame.getWidth() - 145, 110 + x * 35,
					130, 30);
 		}
 	}
 
 	public RapidAssessmentMarkingCanvas(RapidAssessmentContainerType head,
 			Project project) {
 		super();
 		setTitle("Marking: no assessment selected");
 		this.project = project;
 		parentFrame = this;
 		new ContentPanel();
 		this.setContentPane(contentPanel);
 		this.setSize(150, 400);
 		contentPanel.setVisible(true);
 		refreshButtons();
 		
 		this.addWindowListener(new WindowListener() {
 			
 			@Override
 			public void windowOpened(WindowEvent arg0) {
 				time = new Date(22*1000*60*60L);
 				timer = new Timer(1000, new ActionListener() {
 					
 					@Override
 					public void actionPerformed(ActionEvent arg0) {
 						time.setTime(time.getTime()+1000L);
 						timerLabel.setText(new SimpleDateFormat("HH:mm:ss").format(time));
 						
 					}
 				});
 				timer.start();
 				
 			}
 			
 			@Override
 			public void windowIconified(WindowEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void windowDeiconified(WindowEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void windowDeactivated(WindowEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void windowClosing(WindowEvent arg0) {
 				timer.stop();
 				RapidAssessmentMarkingCanvas.this.project.updateTables();
 				
 			}
 			
 			@Override
 			public void windowClosed(WindowEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 			
 			@Override
 			public void windowActivated(WindowEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 		});
 
 		if (head != null) {
 			createComponent(head);
 		}
 
 		this.addKeyListener(new KeyListener() {
 
 			@Override
 			public void keyTyped(KeyEvent arg0) {
 
 			}
 
 			@Override
 			public void keyReleased(KeyEvent arg0) {
 
 			}
 
 			@Override
 			public void keyPressed(KeyEvent arg0) {
 
 				switch (arg0.getKeyCode()) {
 				case 192:
 					markList.get(focusedMark).addChar('0');
 					break;
 				case KeyEvent.VK_ESCAPE:
 					searchBox.setFocusable(true);
 					searchBox.requestFocus();
 					searchBox.selectAll();
 					break;
 				case KeyEvent.VK_ENTER:
 					markList.get(focusedMark).next();
 					break;
 				case '1':
 				case '2':
 				case '3':
 				case '4':
 				case '5':
 				case '6':
 				case '7':
 				case '8':
 				case '9':
 				case '0':
 					markList.get(focusedMark).addChar(arg0.getKeyCode());
 
 					break;
 
 				default:
 					break;
 				}
 
 			}
 		});
 		this.setVisible(true);
 		this.repaint();
 	}
 
 	public class ContentPanel extends JPanel {
 		/**
 		 * 
 		 */
 		public ContentPanel() {
 			super();
 			contentPanel = this;
 			this.setLayout(null);
 			searchLabel = new JLabel("Search");
 			loadAssessmentLabel = new JLabel("Load Assessment");
 			loadAssessmentLabel.setFocusable(false);
 			studentChooser = new JComboBox();
 			timerLabel = new JLabel();
			infoLabels = new JLabel[3];
 			for (int x = 0; x < infoLabels.length; ++x) {
 				infoLabels[x] = new JLabel();
 				infoLabels[x].setVisible(true);
 				infoLabels[x].setFocusable(false);
 			}
 			studentChooser.addActionListener(new ActionListener() {
 
 				@Override
 				public void actionPerformed(ActionEvent arg0) {
 					if (studentChooser.getSelectedItem() != null && parentRect != null){
 						assess((SuperEntity) studentChooser.getSelectedItem());
 						
 					}
 
 				}
 			});
 			
 			
 			studentChooser.setVisible(true);
 			studentChooser.setFocusable(false);
 
 			searchLabel.setVisible(true);
 			loadAssessmentLabel.setVisible(true);
 
 			searchBox = new JTextField();
 
 			searchBox.setVisible(true);
 
 			searchBox.addKeyListener(new KeyListener() {
 
 				@Override
 				public void keyTyped(KeyEvent e) {
 
 				}
 
 				@Override
 				public void keyReleased(KeyEvent e) {
 					search(searchBox.getText());
 					if(e.getKeyCode() == KeyEvent.VK_ENTER && parentRect != null){
 						searchBox.setFocusable(false);
 						parentFrame.requestFocus();
 					}
 
 				}
 
 				@Override
 				public void keyPressed(KeyEvent e) {
 
 				}
 			});
 			loadCombo = new JComboBox();
 			loadCombo.setFocusable(false);
 			loadCombo.setVisible(true);
 			btnLoad = new JButton("Load");
 			btnLoad.setFocusable(false);
 			btnLoad.setVisible(true);
 			btnLoad.addActionListener(new ActionListener() {
 
 				@Override
 				public void actionPerformed(ActionEvent arg0) {
 					if (loadCombo.getSelectedItem() != null)
 						try {
 							createComponent((RapidAssessmentComponentType) loadCombo
 									.getSelectedItem());
 
 						} catch (ClassCastException e) {
 
 						}
 				}
 			});
 			refreshButtons();
 			refreshLoad();
 
 		}
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 
 		@Override
 		protected void paintComponent(Graphics g) {
 
 			super.paintComponent(g);
 			Graphics g2 = g.create();
 
 			if (resizedBackGround == null && backGround != null) {
 				resizedBackGround = Scalr.resize(backGround, Method.QUALITY,
 						Mode.FIT_EXACT, parentRect.getWidth(),
 						parentRect.getHeight(), Scalr.OP_ANTIALIAS);
 			}
 
 			g2.drawImage(resizedBackGround, 0, 0, null);
 			g2.dispose();
 		}
 	}
 
 	public void search(String str) {
 
 		studentChooser.removeAllItems();
 		LinkedList<SuperEntity> list = new LinkedList<SuperEntity>();
 		project.getHead().search(str.toLowerCase(), list);
 
 		for (int x = 0; x < list.size(); ++x) {
 			SuperEntity tmp = list.get(x);
 			while (tmp != project.getHead()
 					&& tmp.getParentEntity() != project.getHead()) {
 				tmp = tmp.getParentEntity();
 			}
 
 			if (tmp != project.getHead())
 				studentChooser.addItem(tmp);
 		}
 	}
 
 	public void refreshLoad() {
 		LinkedList<RapidAssessmentContainerType> containers = new LinkedList<RapidAssessmentContainerType>();
 		this.project.getHeadEntityType().findRapidAssessment(containers);
 		loadCombo.removeAllItems();
 		for (int x = 0; x < containers.size(); ++x)
 			loadCombo.addItem(containers.get(x));
 
 	}
 
 	public JComponent createComponent(RapidAssessmentComponentType node) {
 		MyComponent comp = null;
 		try {
 			RapidAssessmentMarkType n = ((RapidAssessmentMarkType) node);
 
 			comp = new MyMark(node, (int) (n.getX()), (int) (n.getY()));
 
 			return comp;
 		} catch (ClassCastException e) {
 
 		}
 		try {
 			RapidAssessmentContainerType n = ((RapidAssessmentContainerType) node);
 			contentPanel.removeAll();
 			backGround = n.getImage();
 			parentFrame.setTitle("Marking: " + node.getName());
 			parentFrame.setSize((int) (n.getW() + 100), (int) (n.getH() + 50));
 			for (int x = 0; x < n.getSubEntityType().size(); ++x) {
 				try {
 					JComponent j = createComponent((RapidAssessmentComponentType) n
 							.getSubEntityType().get(x));
 					contentPanel.add(j);
 					parentRect = j;
 
 				} catch (ClassCastException e) {
 				}
 			}
 			refreshButtons();
 			searchBox.requestFocus(true);
 			searchLabel.setFocusable(false);
 
 			contentPanel.repaint();
 
 			return contentPanel;
 		} catch (ClassCastException e) {
 
 		}
 		try {
 			RapidAssessmentRectangleType n = ((RapidAssessmentRectangleType) node);
 			comp = new MyRectangle(node, (int) (n.getX()), (int) (n.getY()),
 					(int) (n.getW()), (int) (n.getH()));
 			for (int x = 0; x < n.getSubEntityType().size(); ++x) {
 				try {
 					comp.add(createComponent((RapidAssessmentComponentType) n
 							.getSubEntityType().get(x)));
 
 				} catch (ClassCastException e) {
 				}
 			}
 			return comp;
 		} catch (ClassCastException e) {
 
 		}
 		return comp;
 	}
 
 	public class MyComponent extends JComponent {
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 		private SuperEntity entity;
 		private EntityType enType;
 
 		public MyComponent(EntityType enType, int x, int y, int w, int h) {
 			this.setLocation(x, y);
 			this.setSize(w, h);
 			this.enType = enType;
 			this.setVisible(true);
 
 		}
 
 		/**
 		 * @return the mark
 		 */
 		public double getMark() {
 			if (entity != null) {
 				try {
 					return entity.getMark();
 				} catch (AbsentException e) {
 					return 0;
 				}
 			} else
 				return 0;
 		}
 
 		/**
 		 * @param mark
 		 *            the mark to set
 		 */
 		public void setMark(double mark) {
 			if (entity != null)
 				entity.setMark(mark);
 		}
 
 		/**
 		 * @return the maxMark
 		 */
 		public double getMaxMark() {
 			return enType.getMaxValue();
 		}
 
 		/**
 		 * @param maxMark
 		 *            the maxMark to set
 		 */
 		public void setMaxMark(double maxMark) {
 			enType.setMaxValue(maxMark);
 		}
 
 		public void updateMark() {
 			double total = 0;
 			for (int x = 0; x < this.getComponentCount(); ++x) {
 				try {
 					total += ((MyComponent) this.getComponent(x)).getMark();
 				} catch (ClassCastException e) {
 
 				}
 
 			}
 			this.setMark(total);
 			try {
 				((MyComponent) this.getParent()).updateMark();
 			} catch (ClassCastException e) {
 
 			}
 		}
 
 		/**
 		 * @return the entity
 		 */
 		public SuperEntity getEntity() {
 			return entity;
 		}
 
 		/**
 		 * @param entity
 		 *            the entity to set
 		 */
 		public void setEntity(SuperEntity entity) {
 			this.entity = entity;
 		}
 
 		/**
 		 * @return the enType
 		 */
 		public EntityType getEnType() {
 			return enType;
 		}
 
 		/**
 		 * @param enType
 		 *            the enType to set
 		 */
 		public void setEnType(EntityType enType) {
 			this.enType = enType;
 		}
 	}
 
 	public class MyRectangle extends MyComponent {
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 
 		/**
 		 * @param maxMark
 		 * @param x
 		 * @param y
 		 * @param w
 		 * @param h
 		 */
 		public MyRectangle(EntityType enType, int x, int y, int w, int h) {
 			super(enType, x, y, w, h);
 			new MyMarkTotalComponent(this);
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
 		 */
 		@Override
 		protected void paintComponent(Graphics g) {
 			super.paintComponent(g);
 			Graphics2D g2 = (Graphics2D) g.create();
 			g2.setColor(Color.red);
 			g2.setStroke(new BasicStroke(3f));
 
 			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 					RenderingHints.VALUE_ANTIALIAS_ON);
 			g2.drawRoundRect(0, 0, this.getWidth() - 1, this.getHeight() - 1,
 					20, 20);
 
 			g2.dispose();
 		}
 
 	}
 
 	public class MyMark extends MyComponent {
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 		private String strValue = "";
 
 		/**
 		 * @param maxMark
 		 * @param x
 		 * @param y
 		 * @param w
 		 * @param h
 		 */
 		public MyMark(EntityType enType, int x, int y) {
 			super(enType, x, y, RapidAssessmentCanvas.MARK_SIZE * 3,
 					RapidAssessmentCanvas.MARK_SIZE);
 			markList.add(this);
 
 			this.addMouseListener(new MouseListener() {
 
 				@Override
 				public void mouseReleased(MouseEvent arg0) {
 
 				}
 
 				@Override
 				public void mousePressed(MouseEvent arg0) {
 					int old = focusedMark;
 					markList.get(old).next();
 					focusedMark = markList.indexOf(arg0.getSource());
 					markList.get(old).repaint();
 
 					markList.get(focusedMark).repaint();
 				}
 
 				@Override
 				public void mouseExited(MouseEvent arg0) {
 
 				}
 
 				@Override
 				public void mouseEntered(MouseEvent arg0) {
 
 				}
 
 				@Override
 				public void mouseClicked(MouseEvent arg0) {
 
 				}
 			});
 
 		}
 
 		public boolean addChar(int charVal) {
 			String newStr = strValue + (char) charVal;
 			try {
 				double newMark = Double.parseDouble(newStr);
 				if (newMark > this.getMaxMark())
 					return false;
 
 				this.setMark(newMark);
 				strValue = newStr;
 
 				if ((strValue.length()) * 10 > this.getMaxMark())
 					next();
 
 				this.repaint();
 				return true;
 			} catch (NumberFormatException e) {
 				return false;
 			}
 
 		}
 
 		public boolean backspace() {
 			if (strValue.length() > 0) {
 				strValue = strValue.substring(0, strValue.length() - 2);
 				this.setMark(Double.parseDouble(strValue));
 				return true;
 			} else
 				return false;
 
 		}
 
 		public void next() {
 			strValue = "";
 			MyMark old = markList.get(focusedMark);
 			focusedMark = ++focusedMark % markList.size();
 			if(focusedMark == 0){
 				searchBox.setFocusable(true);
 				searchBox.requestFocus();
 				searchBox.selectAll();
 			}
 			markList.get(focusedMark).repaint();
 			old.repaint();
 
 		}
 
 		@Override
 		protected void paintComponent(Graphics arg0) {
 
 			super.paintComponent(arg0);
 
 			Graphics2D g2 = (Graphics2D) arg0.create();
 
 			if (markList.get(focusedMark) == this) {
 				g2.setColor(Color.red.darker().darker());
 			} else {
 				g2.setColor(Color.red);
 			}
 			g2.setStroke(new BasicStroke(2f));
 			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 					RenderingHints.VALUE_ANTIALIAS_ON);
 			g2.drawRect(0, 0, this.getWidth() / 3 - 1, this.getHeight() - 1);
 			g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN,
 					RapidAssessmentCanvas.MARK_SIZE - 2));
 			String text1 = "" + this.getMark();
 			String text2 = "/" + this.getMaxMark();
 
 			g2.drawString(text1 + text2, this.getWidth() / 3 + 2,
 					this.getHeight() - 1);
 			g2.dispose();
 
 		}
 
 	}
 
 	public class MyMarkTotalComponent extends JComponent {
 
 		/**
 		 * 
 		 */
 		private static final long serialVersionUID = 1L;
 		private static final int WIDTH = RapidAssessmentCanvas.MARK_SIZE * 6;
 		private static final int HEIGHT = (int) (RapidAssessmentCanvas.MARK_SIZE * 1.5);
 
 		public MyMarkTotalComponent(MyComponent parent) {
 			this.setSize(WIDTH, HEIGHT);
 			this.setLocation((int) parent.getWidth() - WIDTH,
 					(int) parent.getHeight() - HEIGHT);
 
 			this.setLayout(null);
 			parent.add(this);
 
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
 		 */
 		@Override
 		protected void paintComponent(Graphics arg0) {
 			super.paintComponent(arg0);
 			Graphics2D g2 = (Graphics2D) arg0.create();
 			g2.setColor(Color.red);
 			g2.setStroke(new BasicStroke(2f));
 			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
 					RenderingHints.VALUE_ANTIALIAS_ON);
 			g2.drawRect(0, 0, this.getWidth() + 5, this.getHeight() + 5);
 			g2.setFont(new Font(Font.SANS_SERIF, Font.PLAIN,
 					this.getHeight() - 4));
 			g2.drawString(((MyComponent) this.getParent()).getMark() + " / "
 					+ ((MyComponent) this.getParent()).getMaxMark(), 1,
 					this.getHeight() - 3);
 			g2.dispose();
 		}
 
 	}
 
 	public void assess(SuperEntity entity) {
		System.out.println("assess");
 		load(entity, (MyComponent) (parentRect));
 		LinkedList<String> list = new LinkedList<String>();
 		entity.findThreeStrings(list);
 
		for (int x = 0; x < 3 && x < list.size(); ++x) {
			infoLabels[x].setText(list.get(x));
 		}
 		parentFrame.repaint();
 	}
 
 	public void load(SuperEntity entity, MyComponent comp) {
 		comp.setEntity(entity.findEntityOfType_Down(comp.getEnType()));
 		for (int x = 0; x < comp.getComponentCount(); ++x) {
 			try {
 				load(entity, (MyComponent) (comp.getComponent(x)));
 			} catch (ClassCastException e) {
 
 			}
 		}
 	}
 
 }
