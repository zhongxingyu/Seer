 package lorian.graph;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JSpinner;
 import javax.swing.SpinnerModel;
 import javax.swing.SpinnerNumberModel;
 import javax.swing.Spring;
 import javax.swing.SpringLayout;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 public class SettingsFrame extends JFrame implements ActionListener {
 	private static final long serialVersionUID = 3855842044783300106L;
 	private final String[] labels = {"Window", "Xmin:", "Xmax:", "Ymin:", "Ymax:", "Grid:", "Interface", "Look and Feel:" };
 	private final String[] GUIstyles = {"System default", "Cross-platform" };
 	//WindowSettings settings;
 	public SettingsFrame(Point point)
 	{
 		//this.settings = currentSettings;
 		this.addWindowListener(new WindowListener()
 		{
 			@Override
 			public void windowClosing(WindowEvent e) {
 				( (SettingsFrame) e.getSource()).Close();
 			}
 			@Override
 			public void windowDeactivated(WindowEvent e){}
 			@Override
 			public void windowDeiconified(WindowEvent e) {}
 			@Override
 			public void windowIconified(WindowEvent e){}
 			@Override			
 			public void windowOpened(WindowEvent e){}
 			@Override
 			public void windowActivated(WindowEvent e) {}
 			@Override
 			public void windowClosed(WindowEvent e) {}
 			
 		});
 		this.setLocationRelativeTo(null);
 		this.setLocation((int) point.getX() + 450, (int) point.getY() + 200);
 		this.setTitle("Settings");
 		this.setResizable(false);
 		
 		initUI();
 		Restore();
 	}
 	private void initUI()
 	{
 		JPanel panel = new JPanel();
 		SpringLayout layout = new SpringLayout();
 		panel.setLayout(layout);
 		
 		int height = 0;
 		
 		int i;
 		for(i=0;i<labels.length;i++)
 		{
 			JLabel label = new JLabel(labels[i]);
 			
 			if(i==0 || i==6)
 			{
 				label.setFont(label.getFont().deriveFont(13.0f).deriveFont(Font.BOLD));
 				label.setForeground(Color.BLACK);
 				panel.add(label);
 				SpringLayout.Constraints labelCons = layout.getConstraints(label);
 				labelCons.setX(Spring.constant(30));
 				if(i==5)
 					labelCons.setY(Spring.constant(35 + height * i));
 				else
 					labelCons.setY(Spring.constant(30 + height * i));
 				continue;
 			}
 			
 			else if(i>=1 && i<=4)
 			{
 				label.setFont(label.getFont().deriveFont(13.0f));
 				label.setForeground(Color.BLACK);
 				
 				SpinnerModel smodel;
 				
 				switch(i-1)
 				{
 				case 0:
					smodel = new SpinnerNumberModel(GraphFunctionsFrame.settings.getXmin(), Long.MIN_VALUE, Long.MAX_VALUE, 1);
 					smodel.addChangeListener(new ChangeListener(){
 						public void stateChanged(ChangeEvent e) {
 							SpinnerModel model = (SpinnerModel) e.getSource();
 							GraphFunctionsFrame.settings.setXmin( ((Double)model.getValue()).longValue());
 						}
 					});
 					break;
 				case 1:
					smodel = new SpinnerNumberModel(GraphFunctionsFrame.settings.getXmax(), Long.MIN_VALUE, Long.MAX_VALUE, 1);
 					smodel.addChangeListener(new ChangeListener(){
 						public void stateChanged(ChangeEvent e) {
 							SpinnerModel model = (SpinnerModel) e.getSource();
 							GraphFunctionsFrame.settings.setXmax(((Double)model.getValue()).longValue());
 						}
 					});
 					break;
 				case 2:
					smodel = new SpinnerNumberModel(GraphFunctionsFrame.settings.getYmin(), Long.MIN_VALUE, Long.MAX_VALUE, 1);
 					smodel.addChangeListener(new ChangeListener(){
 						public void stateChanged(ChangeEvent e) {
 							SpinnerModel model = (SpinnerModel) e.getSource();
 							GraphFunctionsFrame.settings.setYmin( ((Double)model.getValue()).longValue());
 						}
 					});
 					break;
 				case 3:
					smodel = new SpinnerNumberModel(GraphFunctionsFrame.settings.getYmax(),Long.MIN_VALUE, Long.MAX_VALUE, 1);
 					smodel.addChangeListener(new ChangeListener(){
 						public void stateChanged(ChangeEvent e) {
 							SpinnerModel model = (SpinnerModel) e.getSource();
 							GraphFunctionsFrame.settings.setYmax( ((Double)model.getValue()).longValue());
 						}
 					});
 					break;
 				default:
					smodel = new SpinnerNumberModel(0, Long.MIN_VALUE, Long.MAX_VALUE, 1);
 					break;
 				}
 				
 				JSpinner spinner = new JSpinner(smodel);
 				spinner.setFont(spinner.getFont().deriveFont(13.0f)); 
 				spinner.setPreferredSize(new Dimension(100, (int) spinner.getPreferredSize().getHeight()));
 				
 				height = (int) label.getPreferredSize().getHeight() + 10;
 				panel.add(label);
 				panel.add(spinner);
 				
 				
 				SpringLayout.Constraints labelCons = layout.getConstraints(label);
 				labelCons.setX(Spring.constant(30));
 				labelCons.setY(Spring.constant(30 + height * i + 2));
 				SpringLayout.Constraints spinnerCons = layout.getConstraints(spinner);
 				spinnerCons.setX(Spring.constant(130));
 				spinnerCons.setY(Spring.constant(30 + height * i));
 			}
 			else if(i==5)
 			{
 				label.setFont(label.getFont().deriveFont(13.0f));
 				label.setForeground(Color.BLACK);
 				JCheckBox checkbox = new JCheckBox();
 				checkbox.setSelected(GraphFunctionsFrame.settings.gridOn());
 				checkbox.addActionListener(this);
 				height = (int) label.getPreferredSize().getHeight() + 10;
 				panel.add(label);
 				panel.add(checkbox);
 				
 				SpringLayout.Constraints labelCons = layout.getConstraints(label);
 				labelCons.setX(Spring.constant(30));
 				labelCons.setY(Spring.constant(30 + height * i + 2));
 				SpringLayout.Constraints checkboxCons = layout.getConstraints(checkbox);
 				checkboxCons.setX(Spring.constant(130));
 				checkboxCons.setY(Spring.constant(30 + height * i));
 			}
 			else
 			{
 				label.setFont(label.getFont().deriveFont(13.0f));
 				label.setForeground(Color.BLACK);
 				JComboBox<String> GUIstyleComboBox = new JComboBox<String>(GUIstyles);
 				GUIstyleComboBox.addActionListener(this);
 				GUIstyleComboBox.setPreferredSize(new Dimension(140, (int) GUIstyleComboBox.getPreferredSize().getHeight())); 
 				height = (int) label.getPreferredSize().getHeight() + 10;
 				panel.add(label);
 				panel.add(GUIstyleComboBox);
 				
 				SpringLayout.Constraints labelCons = layout.getConstraints(label);
 				labelCons.setX(Spring.constant(30));
 				labelCons.setY(Spring.constant(30 + height * i + 2));
 				SpringLayout.Constraints comboboxCons = layout.getConstraints(GUIstyleComboBox);
 				comboboxCons.setX(Spring.constant(130));
 				comboboxCons.setY(Spring.constant(30 + height * i));
 				
 			}
 			
 		}
 		JButton button = new JButton("OK");
 		button.setPreferredSize(new Dimension(80, (int) button.getPreferredSize().getHeight()));
 		button.addActionListener(this);
 		panel.add(button);
 		SpringLayout.Constraints buttonCons = layout.getConstraints(button);
 		buttonCons.setX(Spring.constant(100));
 		buttonCons.setY(Spring.constant(40 + height * i));
 		this.add(panel);
 		this.setSize(300, i* height + 120);
 		
 	}
 	public void Restore()
 	{
 		this.setVisible(true);
 	}
 	public void Close()
 	{
 		this.setVisible(false);
 		this.dispose();
 	}
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if(e.getActionCommand()=="OK")
 		{
 			Close();
 			GraphFunctionsFrame.UpdateWindowSettings();
 		}
 		else if(e.getSource() instanceof JComboBox<?>)
 		{
 			@SuppressWarnings("unchecked")
 			String newval = (String) ((JComboBox<String>) e.getSource()).getSelectedItem();
 			
 			if(newval.equalsIgnoreCase(GUIstyles[0])) //System default
 			{
 				GraphFunctionsFrame.SetSystemLookAndFeel();
 				System.out.println("Changing GUI style to system default");
 			}
 			else if(newval.equalsIgnoreCase(GUIstyles[1])) //Cross-platform
 			{
 				GraphFunctionsFrame.SetJavaLookAndFeel();
 				System.out.println("Changing GUI style to cross-platform");
 			}
 		}
 		else if(e.getSource() instanceof JCheckBox)
 		{
 			GraphFunctionsFrame.settings.setGrid(((JCheckBox) e.getSource()).isSelected());
 		}
 
 		
 	}
 	
 }
