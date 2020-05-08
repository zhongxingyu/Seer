 package ch.zhaw.minipc.presentation;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.JButton;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 
 import ch.zhaw.minipc.base.CPU;
 import ch.zhaw.minipc.base.ReturnValues;
 import ch.zhaw.minipc.base.RunModes;
 import ch.zhaw.minipc.commands.Command;
 import ch.zhaw.minipc.memory.MemoryCell;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 
 import javax.swing.JPanel;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableColumn;
 
 import java.awt.FlowLayout;
 import javax.swing.JComboBox;
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.JLabel;
 import java.awt.GridLayout;
 import java.awt.GridBagLayout;
 import java.awt.GridBagConstraints;
 import java.awt.Insets;
 import javax.swing.BoxLayout;
 import java.awt.Component;
 import javax.swing.GroupLayout;
 import javax.swing.GroupLayout.Alignment;
 import javax.swing.JCheckBox;
 
 public class EmulatorGUI implements Observer{
 
 	private JFrame frame;
 	private EmulatorGUI emuGui;
	private RunModes mode = RunModes.STEP;
 	private JTextField txtResultDez;
 	private Thread emuThread;
 	private CPU cpu;
 	
 	private JButton btnStep;
 	private JButton btnStart;
 	private JTextField txtCommandCounter;
 	private JPanel panelCommandTable;
 	private JTable tableCommandMemory;
 	private JComboBox comboBox;
 	private JPanel panelRegister;
 	private JLabel lblRegister;
 	private JTextField txtRegDez1;
 	private JTextField txtRegBin1;
 	private JTextField txtRegDez2;
 	private JTextField txtRegBin2;
 	private JLabel lblRegister_2;
 	private JTextField txtRegDez3;
 	private JTextField txtRegBin3;
 	private JLabel lblAkku;
 	private JScrollPane scrollPaneMemory;
 	private JTable tableDataMemory;
 	private JTextField txtResultBin;
 	private JLabel lblBefzhler;
 	private JTextField txtBefehlsZaehlerDez;
 	private JTextField txtBefehlsZaehlerBin;
 
 	/**
 	 * Create the application.
 	 */
 	public EmulatorGUI() {
 		initialize();
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frame = new JFrame();
 		frame.setBounds(100, 100, 920, 487);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.getContentPane().setLayout(new BorderLayout(0, 0));
        
         
         DefaultTableModel model = new DefaultTableModel();
         tableCommandMemory = new JTable(model);
         tableCommandMemory.setEnabled(false);
         tableCommandMemory.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
       
         model.addColumn("#");
         model.addColumn("Command");
         model.addColumn("Op-Code");
         
         TableColumn col = tableCommandMemory.getColumnModel().getColumn(0);
         col.setPreferredWidth(40);
         col = tableCommandMemory.getColumnModel().getColumn(1);
         col.setPreferredWidth(120);
         col = tableCommandMemory.getColumnModel().getColumn(2);
         col.setPreferredWidth(150);
         
 		JScrollPane scrollPaneCommand = new JScrollPane(tableCommandMemory);
 		scrollPaneCommand.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		scrollPaneCommand.setPreferredSize(new Dimension(310,150));
 		frame.getContentPane().add(scrollPaneCommand, BorderLayout.WEST);
 		
 		panelCommandTable = new JPanel();
 		panelCommandTable.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
 		frame.getContentPane().add(panelCommandTable, BorderLayout.NORTH);
 		
 		
 		comboBox = new JComboBox();
 		comboBox.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				JComboBox cb = (JComboBox)arg0.getSource();
 				mode = (RunModes) (cb.getSelectedItem());
 			}
 		});
 		comboBox.setModel(new DefaultComboBoxModel(RunModes.values()));
		this.mode = (RunModes) comboBox.getSelectedItem();
 		panelCommandTable.add(comboBox);
 		
 		btnStart = new JButton("Start");		
 		panelCommandTable.add(btnStart);
 		btnStart.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				emuGui.startEmulation();
 			}
 		});
 		btnStart.setEnabled(false);
 		
 		btnStep = new JButton("Step");
 		panelCommandTable.add(btnStep);
 		btnStep.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				synchronized (this) {
 					cpu.proceed();
 				}
 			}
 		});
		btnStep.setEnabled(true);
 		
 		panelRegister = new JPanel();
 		frame.getContentPane().add(panelRegister, BorderLayout.CENTER);
 		
 		lblRegister = new JLabel("Register 1");
 		lblRegister.setBounds(19, 11, 63, 16);
 		
 		txtRegDez1 = new JTextField();
 		txtRegDez1.setEditable(false);
 		txtRegDez1.setBounds(99, 5, 153, 28);
 		txtRegDez1.setColumns(10);
 		panelRegister.setLayout(null);
 		panelRegister.add(lblRegister);
 		panelRegister.add(txtRegDez1);
 		
 		txtRegBin1 = new JTextField();
 		txtRegBin1.setEditable(false);
 		txtRegBin1.setBounds(99, 34, 153, 28);
 		panelRegister.add(txtRegBin1);
 		txtRegBin1.setColumns(10);
 		
 		JLabel lblRegister_1 = new JLabel("Register 2");
 		lblRegister_1.setBounds(19, 82, 63, 16);
 		panelRegister.add(lblRegister_1);
 		
 		txtRegDez2 = new JTextField();
 		txtRegDez2.setEditable(false);
 		txtRegDez2.setBounds(99, 76, 153, 28);
 		panelRegister.add(txtRegDez2);
 		txtRegDez2.setColumns(10);
 		
 		txtRegBin2 = new JTextField();
 		txtRegBin2.setEditable(false);
 		txtRegBin2.setBounds(99, 105, 153, 28);
 		panelRegister.add(txtRegBin2);
 		txtRegBin2.setColumns(10);
 		
 		lblRegister_2 = new JLabel("Register 3");
 		lblRegister_2.setBounds(19, 154, 63, 16);
 		panelRegister.add(lblRegister_2);
 		
 		txtRegDez3 = new JTextField();
 		txtRegDez3.setEditable(false);
 		txtRegDez3.setBounds(99, 148, 153, 28);
 		panelRegister.add(txtRegDez3);
 		txtRegDez3.setColumns(10);
 		
 		txtRegBin3 = new JTextField();
 		txtRegBin3.setEditable(false);
 		txtRegBin3.setBounds(99, 177, 153, 28);
 		panelRegister.add(txtRegBin3);
 		txtRegBin3.setColumns(10);
 		
 		lblAkku = new JLabel("Akku");
 		lblAkku.setBounds(19, 224, 61, 16);
 		panelRegister.add(lblAkku);
 		
 		txtResultDez = new JTextField();
 		txtResultDez.setBounds(99, 218, 153, 28);
 		panelRegister.add(txtResultDez);
 		txtResultDez.setEditable(false);
 		txtResultDez.setColumns(10);
 		
 		txtResultBin = new JTextField();
 		txtResultBin.setEditable(false);
 		txtResultBin.setBounds(99, 248, 153, 28);
 		panelRegister.add(txtResultBin);
 		txtResultBin.setColumns(10);
 		
 		
 		txtCommandCounter = new JTextField();
 		txtCommandCounter.setBounds(99, 370, 153, 28);
 		panelRegister.add(txtCommandCounter);
 		txtCommandCounter.setEditable(false);
 		txtCommandCounter.setColumns(10);
 		
 		JLabel lblZhler = new JLabel("Steps\n");
 		lblZhler.setBounds(19, 376, 61, 16);
 		panelRegister.add(lblZhler);
 		
 		lblBefzhler = new JLabel("Bef.Z\u00E4hler");
 		lblBefzhler.setBounds(19, 291, 72, 16);
 		panelRegister.add(lblBefzhler);
 		
 		txtBefehlsZaehlerDez = new JTextField();
 		txtBefehlsZaehlerDez.setEditable(false);
 		txtBefehlsZaehlerDez.setBounds(99, 284, 153, 28);
 		panelRegister.add(txtBefehlsZaehlerDez);
 		txtBefehlsZaehlerDez.setColumns(10);
 		
 		txtBefehlsZaehlerBin = new JTextField();
 		txtBefehlsZaehlerBin.setEditable(false);
 		txtBefehlsZaehlerBin.setBounds(99, 314, 153, 28);
 		panelRegister.add(txtBefehlsZaehlerBin);
 		txtBefehlsZaehlerBin.setColumns(10);
 		
 		JLabel lblCarryFlag = new JLabel("Carry Flag");
 		lblCarryFlag.setBounds(19, 348, 72, 16);
 		panelRegister.add(lblCarryFlag);
 		
 		JCheckBox checkBox = new JCheckBox("");
 		checkBox.setEnabled(false);
 		checkBox.setBounds(99, 344, 28, 23);
 		panelRegister.add(checkBox);
 		
         DefaultTableModel modelMemory = new DefaultTableModel();
         tableDataMemory = new JTable(modelMemory);
         tableDataMemory.setEnabled(false);
         tableDataMemory.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
       
         modelMemory.addColumn("#");
         modelMemory.addColumn("Value Dez");
         modelMemory.addColumn("Value Bin");
         
         TableColumn colData = tableDataMemory.getColumnModel().getColumn(0);
         colData.setPreferredWidth(40);
         colData = tableDataMemory.getColumnModel().getColumn(1);
         colData.setPreferredWidth(80);
         colData = tableDataMemory.getColumnModel().getColumn(2);
         colData.setPreferredWidth(150);
         
 		JScrollPane scrollPaneMemory = new JScrollPane(tableDataMemory);
 		scrollPaneMemory.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		scrollPaneMemory.setPreferredSize(new Dimension(270,110));
 		frame.getContentPane().add(scrollPaneMemory, BorderLayout.EAST);
 		
 		JMenuBar menuBar = new JMenuBar();
 		frame.setJMenuBar(menuBar);
 		//Build the first menu.
 		JMenu menu = new JMenu("Emulator");
 		menuBar.add(menu);
 		
 		JMenuItem menuItem = new JMenuItem("Initialize");
 		menuItem.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				initCPU();	
 			}
 		});
 		menu.add(menuItem);
 		
 		//frame.pack();
 		this.emuGui = this;
         frame.setVisible(true);
 	}
 	
 	public void initCPU(){
 	    JFileChooser chooser = new JFileChooser("./data");
 	    int returnVal = chooser.showOpenDialog(this.frame);
 	    
 	    if(returnVal == JFileChooser.APPROVE_OPTION) {
 	    	this.cpu = new CPU(chooser.getSelectedFile().getPath());
 			cpu.addObserver(emuGui);
 			
 			btnStart.setEnabled(true);
 	    }
 	    ReturnValues returnSet = this.cpu.getInitialConifg();
 	    updateTable(returnSet);
 	    updateDataTable(returnSet);
 	    
 	}
 	
 	public void startEmulation(){
 		
 		cpu.setRunMode(mode);
 		
 		switch(mode){
 			case AUTO:
 				new Thread(cpu).start();
 				break;
 			case SLOW:
 				new Thread(cpu).start();
 				break;
 			case STEP:
 				this.emuThread = new Thread(cpu);
 				this.emuThread.start();
 				break;
 		}	
 	}
 	
 	private void updateTable(ReturnValues returnSet){
 		Map<Integer,Command> map = returnSet.getMemory().getCommandMemory();
 		DefaultTableModel model = (DefaultTableModel) this.tableCommandMemory.getModel();
 		for( Map.Entry<Integer,Command> entry : map.entrySet() )
 		{
 		  int cellNumber = entry.getKey();
 		  Command command = entry.getValue();
 		 
 		  model.addRow(new Object[]{cellNumber, command.getFullCommand(), command.getOpCode()});
 		}
 	}
 	
 	private void updateDataTable(ReturnValues returnSet){
 		Map<Integer,MemoryCell> mapMemory = returnSet.getMemory().getDataMemory();
 		DefaultTableModel modelData = (DefaultTableModel) this.tableDataMemory.getModel();
 		modelData.getDataVector().removeAllElements();
 		for( Map.Entry<Integer,MemoryCell> entry : mapMemory.entrySet() )
 		{
 		  int cellNumber = entry.getKey();
 		  MemoryCell cell = entry.getValue();
 		 
 		  modelData.addRow(new Object[]{cellNumber, cell.getDezValue(), cell.getBinValue()});
 		}
 	}
 	
 	private void selectRow(int position){
 		ListSelectionModel selectionModel = tableCommandMemory.getSelectionModel();
 		selectionModel.clearSelection();
 		selectionModel.addSelectionInterval(position,position);
 	}
 	
 	private void updateFields(ReturnValues returnSet){
 		String resultTextDez = Integer.toString(returnSet.getAkku().getDezValue());
 		this.txtResultDez.setText(resultTextDez);
 		String resultTextBin = returnSet.getAkku().getBinValue();
 		this.txtResultBin.setText(resultTextBin);
 
 		String resultTextDez1 = Integer.toString(returnSet.getRegisterList().get("R1").getDezValue());
 		this.txtRegDez1.setText(resultTextDez1);
 		String resultTextBin1 = returnSet.getRegisterList().get("R1").getBinValue();
 		this.txtRegBin1.setText(resultTextBin1);
 		
 		String resultTextDez2 = Integer.toString(returnSet.getRegisterList().get("R2").getDezValue());
 		this.txtRegDez2.setText(resultTextDez2);
 		String resultTextBin2 = returnSet.getRegisterList().get("R2").getBinValue();
 		this.txtRegBin2.setText(resultTextBin2);
 		
 		String resultTextDez3 = Integer.toString(returnSet.getRegisterList().get("R3").getDezValue());
 		this.txtRegDez3.setText(resultTextDez3);
 		String resultTextBin3 = returnSet.getRegisterList().get("R3").getBinValue();
 		this.txtRegBin3.setText(resultTextBin3);
 	}
 	
 	@Override
 	public void update(Observable arg0, Object arg1) {
 		ReturnValues returnSet = (ReturnValues)arg1;
 		this.updateFields(returnSet);
 		this.updateDataTable(returnSet);
 		this.selectRow(returnSet.getProgramCounter());
 		this.txtCommandCounter.setText(Integer.toString(returnSet.getProgramCounter()));
 		
 		if(mode==RunModes.AUTO){
 			
 		}else if(mode ==RunModes.SLOW){
 			//returnSet.getMemory().getCommandMemoryField()
 			
 		}
 		else if(mode == RunModes.STEP){
 			//this.selectRow(returnSet.getProgramCounter());
 			this.cpu.pause();
 		}
 		
 		this.frame.repaint();
 	}
 }
