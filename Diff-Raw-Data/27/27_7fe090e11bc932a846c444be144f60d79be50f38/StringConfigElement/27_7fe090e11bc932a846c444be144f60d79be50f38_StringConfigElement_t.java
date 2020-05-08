 package evaluation.simulator.gui.customElements.configElements;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.StringTokenizer;
 
 import javax.swing.BorderFactory;
 import javax.swing.DefaultListModel;
 import javax.swing.ImageIcon;
 import javax.swing.JComboBox;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.ListModel;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import net.miginfocom.swing.MigLayout;
 import evaluation.simulator.annotations.property.StringProp;
 import evaluation.simulator.gui.pluginRegistry.SimPropRegistry;
 
 @SuppressWarnings("serial")
 public class StringConfigElement extends JPanel implements ActionListener, Observer {
 
 	StringProp property;
 	JTextField textfield;
 	JComboBox<String> jComboBox;
 	JScrollPane jScrollPane;
 	JList<String> jList;
 	
 	SimPropRegistry simPropRegistry;
 	private Component component;
 	List<JTextArea> messages;
 	Map<Component, Component> icons;
 	
 	int listSize;
 	
 	public StringConfigElement(StringProp s) {
 		
 		simPropRegistry = SimPropRegistry.getInstance();
 		
 		this.property = s;
 		simPropRegistry.registerGuiElement(this, property.getPropertyID());
 		
 		this.messages = new LinkedList<JTextArea>();
 		this.icons = new HashMap<Component, Component>();
 		this.listSize = 0;
 		
 		MigLayout migLayout = new MigLayout("","[grow]","");
 		this.setLayout(migLayout);
 		
 		this.setBorder(BorderFactory.createTitledBorder(property.getName()));
 		
 		if ( !this.property.getPossibleValues().equals("") ){
 			StringTokenizer st = new StringTokenizer(this.property.getPossibleValues(), ",");
 			if (this.property.getMultiSelection()){
 				this.jScrollPane = new JScrollPane();
 				DefaultListModel<String> listModel = new DefaultListModel<String>();
 				this.jList = new JList<String>(listModel);
 				this.jList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
 				while (st.hasMoreTokens()) {
 					this.listSize++;
 					listModel.addElement(st.nextToken());
 				}
 				
 				this.jList.addMouseListener( new MouseAdapter() {
 					public void mousePressed(MouseEvent evt) {
 						String tmp = "";
 						for (String str : jList.getSelectedValuesList() ) {
 							tmp = tmp + str + ",";
 						}
 						simPropRegistry.setValue(property.getPropertyID(), tmp);
 		            }
 				});
 				
 				this.jList.setToolTipText(property.getTooltip());
//				jList.setPreferredSize(new Dimension(10, 25));
				jScrollPane.setPreferredSize( new Dimension(150, 220));
 				jScrollPane.setViewportView(jList);
				this.add( jScrollPane, "growx, growy, wmin 10" );
 				this.component = this.jScrollPane;
 			}else{
 				this.jComboBox = new JComboBox<String>();
 				while (st.hasMoreTokens()) {
 					jComboBox.addItem(st.nextToken());
 				}
 				this.jComboBox.addActionListener(this);
 				this.jComboBox.setToolTipText(property.getTooltip());
				this.add( jComboBox, "growx, push, wmin 10" );
 				this.component = this.jComboBox;
 			}
 		}else {
 			this.textfield = new JTextField();
 			this.textfield.addActionListener(this);
 			this.textfield.setToolTipText(property.getTooltip());
			this.add( textfield, "growx, push, wmin 10" );
 			this.component = this.textfield;
 		}
 		
 		if (!property.getInfo().equals("")){
 			JTextArea textarea = new JTextArea("Info: " + property.getInfo());
 			textarea.setEditable(false);
 			textarea.setLineWrap(true);
 			textarea.setWrapStyleWord(true);
 			textarea.setPreferredSize( new Dimension(10, 25) );
 			this.add( textarea, "growx, growy, wmin 10" );
 		}
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent event) {
 		if ( event.getSource() == this.textfield ){
 			simPropRegistry.setValue(this.property.getPropertyID(), this.textfield.getText());
 		}
 		if ( event.getSource() == this.jComboBox ){
 			simPropRegistry.setValue(this.property.getPropertyID(), (String) this.jComboBox.getSelectedItem());
 		}
 	}
 	
 	// Called when simprop has changed
 	@Override
 	public void update(Observable observable, Object o) {
 		
 		for (Component message : this.messages){
 			this.remove(message);
 		}
 		for (Component icon : this.icons.values()){
 			this.remove(icon);
 		}
 		
 		this.messages.clear();
 		this.icons.clear();
 		
 		// Load properties on update
 		if ( !this.property.getPossibleValues().equals("") ) {
 			if ( this.property.getMultiSelection() ){
 				List<Integer> indices = new LinkedList<Integer>();
 				
 				int index = 0;
 				StringTokenizer possibleValueStrings = new StringTokenizer(this.property.getPossibleValues(), ",");
 				while (possibleValueStrings.hasMoreTokens()) {
 					String possible = possibleValueStrings.nextToken();
 					StringTokenizer configValues = new StringTokenizer((String) simPropRegistry.getValue(property.getPropertyID()).getValue(), ",");
 					while (configValues.hasMoreTokens()) {
 						if ( configValues.nextToken().equals(possible) ){
 							indices.add(index);
 							continue;
 						}
 					}
 					index++;
 				}
 				
 				// JAVA SUCKS HARD! THE STL IS TOTALY OVERLOADED BUT...
 				// int[] != Integer[] ... no nice conversion?!?!
 				// Yes, we have to do it this way!!!
 				int[] intIndices = new int[indices.size()];
 				for (int k = 0; k < intIndices.length; k++)
 					intIndices[k] = indices.get(k).intValue();
 				
 				this.jList.setSelectedIndices( intIndices );
 			}else{
 				this.jComboBox.setSelectedItem((String) simPropRegistry.getValue(property.getPropertyID()).getValue());
 			}
 		}else{
 			this.textfield.setText((String) simPropRegistry.getValue( property.getPropertyID()).getValue());
 		}
 		
 		if (property.getWarnings() != null && property.getWarnings().size() > 0){
 			for (String each : property.getWarnings()){
 				JTextArea text = new JTextArea(each);
 				text.setEditable(false);
 				text.setLineWrap(true);
 				text.setWrapStyleWord(true);
 				text.setPreferredSize( new Dimension(10,25));
 				this.messages.add( text );
 				JLabel warning = new JLabel(new ImageIcon("etc/img/icons/warning/warning_16.png"));
 				this.icons.put( text, warning);
 			}
 		}
 		
 		if (property.getErrors() != null && property.getErrors().size() > 0){
 			for (String each : property.getErrors()){
 				JTextArea text = new JTextArea(each);
 				text.setEditable(false);
 				text.setLineWrap(true);
 				text.setWrapStyleWord(true);
 				text.setPreferredSize( new Dimension(10,25));
 				this.messages.add( text );
 				JLabel error = new JLabel(new ImageIcon("etc/img/icons/error/error_16.png"));
 				this.icons.put(text, error);
 			}
 		}
 		
 		for (JTextArea message : this.messages){
 			this.add(this.icons.get(message) , "push, wmin 16, wrap");
 			this.add(message, "growx, growy, push, wmin 10" );
 		}
 		
 		updateUI();
 	}
 		
 }
