 package it.planetgeeks.mclauncher.frames;
 
 import it.planetgeeks.mclauncher.Launcher;
 import it.planetgeeks.mclauncher.frames.utils.CustomKeyListener;
 import it.planetgeeks.mclauncher.frames.utils.CustomMouseListener;
 import it.planetgeeks.mclauncher.frames.utils.CustomWindowListener;
 import it.planetgeeks.mclauncher.utils.LanguageUtils;
 import it.planetgeeks.mclauncher.utils.Memory;
 import it.planetgeeks.mclauncher.utils.MemoryUtils;
 import it.planetgeeks.mclauncher.utils.Profile;
 
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.KeyEvent;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowEvent;
 
 import javax.swing.AbstractListModel;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JScrollPane;
 import javax.swing.JSeparator;
 import javax.swing.JSpinner;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 import javax.swing.WindowConstants;
 
 public class MemoryFrame extends JFrame
 {
 	private static final long serialVersionUID = 1L;
 
 	private int parentFrame;
 	private Object extra;
 	private Memory currentMem = null;
 	private int xSize;
 	private int ySize;
 
 	public MemoryFrame(int parentFrame, Object extra)
 	{
		this.extra = extra;
 		xSize = 256;
 		ySize = 340;
 		this.parentFrame = parentFrame;
 		initComponents();
 		this.setSize(xSize, ySize);
 		this.setResizable(false);
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		this.setLocation(screenSize.width / 2 - this.getWidth() / 2, screenSize.height / 2 - this.getHeight() / 2);
 	}
 
 	private void initComponents()
 	{
 
 		textField = new JTextField();
 		nameLbl = new JLabel();
 		sizeLbl = new JLabel();
 		spinnerSize = new JSpinner();
 		btn1 = new JButton();
 		btn2 = new JButton();
 		upBtn = new JButton();
 		downBtn = new JButton();
 		scrollpane = new JScrollPane();
 		list = new JList();
 		separator = new JSeparator();
 		this.setTitle(LanguageUtils.getTranslated("launcher.memory.title"));
 
 		setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
 
 		this.addWindowListener(new CustomWindowListener()
 		{
 			@Override
 			public void windowClosing(WindowEvent arg0)
 			{
 				if (parentFrame == 1)
 				{
 					Launcher.openProfileEditor((Profile) extra);
 				}
 			}
 		});
 
 		nameLbl.setHorizontalAlignment(SwingConstants.RIGHT);
 		nameLbl.setText(LanguageUtils.getTranslated("launcher.memory.namelbl") + " :");
 
 		sizeLbl.setHorizontalAlignment(SwingConstants.RIGHT);
 		sizeLbl.setText(LanguageUtils.getTranslated("launcher.memory.dimensionlbl") + " :");
 
 		btn1.setText(LanguageUtils.getTranslated("launcher.memory.createbtn"));
 
 		btn2.setText(LanguageUtils.getTranslated("launcher.memory.deletebtn"));
 
 		char ch = 8657;
 		upBtn.setText(String.valueOf(ch));
 
 		ch = 8659;
 		downBtn.setText(String.valueOf(ch));
 
 		scrollpane.setViewportView(list);
 
 		setList();
 
 		textField.addKeyListener(new CustomKeyListener()
 		{
 			@Override
 			public void keyReleased(KeyEvent arg0)
 			{
 				updateComponents(0);
 			}
 		});
 
 		list.addMouseListener(new CustomMouseListener()
 		{
 			@Override
 			public void mouseReleased(MouseEvent arg0)
 			{
 				updateComponents(1);
 			}
 		});
 
 		btn1.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent arg0)
 			{
 				if (currentMem != null)
 				{
 					MemoryUtils.modifyCustom(currentMem.name, String.valueOf(spinnerSize.getValue()));
 					updateComponents(2);
 				}
 				else
 				{
 					MemoryUtils.createCustom(textField.getText().trim(), String.valueOf(spinnerSize.getValue()));
 					updateComponents(2);
 				}
 			}
 		});
 
 		btn2.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent arg0)
 			{
 				if (currentMem != null)
 				{
 					MemoryUtils.deleteCustom(currentMem.name);
 					updateComponents(2);
 				}
 			}
 		});
 
 		upBtn.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent arg0)
 			{
 				if (list.getSelectedValue() != null)
 				{
 					int newPos = MemoryUtils.moveCustom(true, list.getSelectedIndex());
 					updateComponents(2);
 					list.setSelectedIndex(newPos);
 				}
 
 			}
 		});
 
 		downBtn.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent arg0)
 			{
 				if (list.getSelectedValue() != null)
 				{
 					int newPos = MemoryUtils.moveCustom(false, list.getSelectedIndex());
 					updateComponents(2);
 					list.setSelectedIndex(newPos);
 				}
 			}
 		});
 
 		btn2.setEnabled(false);
 
 		this.setLayout(null);
 
 		btn1.setBounds(15, this.ySize - 15 - 50, 100, 30);
 		btn2.setBounds(this.xSize - 100 - 22, this.ySize - 15 - 50, 100, 30);
 		scrollpane.setBounds(5, 5, 200, 165);
 		separator.setBounds(0, 173, this.xSize, 5);
 		textField.setBounds(100, 185, 135, 30);
 		nameLbl.setBounds(10, 185, 85, 30);
 		spinnerSize.setBounds(100, 225, 135, 30);
 		sizeLbl.setBounds(10, 225, 85, 30);
 		upBtn.setBounds(206, 5, 40, 30);
 		downBtn.setBounds(206, 40, 40, 30);
 
 		this.getContentPane().add(btn1);
 		this.getContentPane().add(btn2);
 		this.getContentPane().add(scrollpane);
 		this.getContentPane().add(separator);
 		this.getContentPane().add(textField);
 		this.getContentPane().add(nameLbl);
 		this.getContentPane().add(spinnerSize);
 		this.getContentPane().add(sizeLbl);
 		this.getContentPane().add(upBtn);
 		this.getContentPane().add(downBtn);
 
 		pack();
 	}
 
 	private void setList()
 	{
 		list.setModel(new AbstractListModel()
 		{
 
 			private static final long serialVersionUID = 1L;
 			String[] strings = MemoryUtils.getMemoryNames();
 
 			public int getSize()
 			{
 				return strings.length;
 			}
 
 			public Object getElementAt(int i)
 			{
 				return strings[i];
 			}
 		});
 	}
 
 	private void updateComponents(int Case)
 	{
 		if (Case == 0)
 		{
 			String[] memoryNames = MemoryUtils.getMemoryNames();
 
 			for (String mem : memoryNames)
 			{
 				if (mem.equals(textField.getText().trim()))
 				{
 					currentMem = MemoryUtils.getMem(mem);
 					btn1.setText(LanguageUtils.getTranslated("launcher.memory.modifybtn"));
 					btn2.setEnabled(true);
 					return;
 				}
 			}
 
 			currentMem = null;
 			btn1.setText(LanguageUtils.getTranslated("launcher.memory.createbtn"));
 			btn2.setEnabled(false);
 		}
 		else if (Case == 1)
 		{
 			currentMem = MemoryUtils.getMem((String) list.getSelectedValue());
 			if (currentMem != null)
 			{
 				textField.setText(currentMem.name);
 				spinnerSize.setValue(currentMem.size);
 				btn1.setText(LanguageUtils.getTranslated("launcher.memory.modifybtn"));
 				btn2.setEnabled(true);
 			}
 			else
 			{
 				btn1.setText(LanguageUtils.getTranslated("launcher.memory.createbtn"));
 				btn2.setEnabled(true);
 			}
 		}
 		else if (Case == 2)
 		{
 			setList();
 		}
 	}
 
 	private JButton btn1;
 	private JButton btn2;
 	private JButton upBtn;
 	private JButton downBtn;
 	private JLabel nameLbl;
 	private JLabel sizeLbl;
 	private JList list;
 	private JScrollPane scrollpane;
 	private JSeparator separator;
 	private JSpinner spinnerSize;
 	private JTextField textField;
 
 }
