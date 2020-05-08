 package View;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.border.EmptyBorder;
 
 import Model.Airport;
 import Model.PhysicalRunway;
 import Model.Runway;
 import net.miginfocom.swing.MigLayout;
 
 
 @SuppressWarnings("serial")
 public class EditRunwayDialog extends JDialog {
 
 	private JPanel contentPane;
 	private JTextField LASDA;
 	private JTextField LTORA;
 	private JTextField LTODA;
 	private JTextField LLDA;
 	private JTextField txtl;
 	private JTextField RASDA;
 	private JTextField RTORA;
 	private JTextField RTODA;
 	private JTextField RLDA;
 	private JTextField txtr;
 	@SuppressWarnings("unused")
 	private Airport airport;
 	@SuppressWarnings("unused")
 	private JList physicalRunwayJList;
 	private JTextField LDT;
 	private JTextField RDT;
 
 	public EditRunwayDialog(Airport airport, JList physicalRunwayJList, boolean newRunway) {
 		this.airport = airport;
 		this.physicalRunwayJList = physicalRunwayJList;
 		
 		
 		setResizable(false);
 		setTitle("Edit Runway");
 		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		setBounds(100, 100, 490, 354);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 		
 		JPanel panel = new JPanel();
 		panel.setBounds(12, 50, 212, 185);
 		contentPane.add(panel);
 		panel.setLayout(new MigLayout("", "[37px][37px,grow]", "[15px][][][][]"));
 		
 		JLabel lblAsda = new JLabel("ASDA");
 		lblAsda.setToolTipText("Accelerate Stop Distance Available");
 		panel.add(lblAsda, "cell 0 0,alignx trailing,aligny top");
 		
 		LASDA = new JTextField();
 		panel.add(LASDA, "cell 1 0,growx");
 		LASDA.setColumns(10);
 		
 		JLabel lblToda = new JLabel("TORA");
 		lblToda.setToolTipText("Take-Off Run Available ");
 		panel.add(lblToda, "cell 0 1,alignx trailing,aligny top");
 		
 		LTORA = new JTextField();
 		lblToda.setLabelFor(LTORA);
 		panel.add(LTORA, "flowx,cell 1 1,growx");
 		LTORA.setColumns(10);
 		
 		JLabel lblToda_1 = new JLabel("TODA");
 		lblToda_1.setToolTipText("Take-Off Distance Available");
 		panel.add(lblToda_1, "cell 0 2,alignx trailing");
 		
 		LTODA = new JTextField();
 		lblToda_1.setLabelFor(LTODA);
 		panel.add(LTODA, "flowx,cell 1 2,growx");
 		LTODA.setColumns(10);
 		
 		JLabel lblLda = new JLabel("LDA");
 		lblLda.setToolTipText("Landing Distance Available");
 		panel.add(lblLda, "cell 0 3,alignx trailing");
 		
 		LLDA = new JTextField();
 		lblLda.setLabelFor(LLDA);
 		panel.add(LLDA, "flowx,cell 1 3,growx");
 		LLDA.setColumns(10);
 		
 		JLabel lblM = new JLabel("m");
 		panel.add(lblM, "cell 1 0");
 		
 		JLabel lblNewLabel_1 = new JLabel("m");
 		panel.add(lblNewLabel_1, "cell 1 1");
 		
 		JLabel lblNewLabel_2 = new JLabel("m");
 		panel.add(lblNewLabel_2, "cell 1 2");
 		
 		JLabel lblM_1 = new JLabel("m");
 		panel.add(lblM_1, "cell 1 3");
 		
		JLabel lblDisplacementThreshold = new JLabel("Displaced Threshold");
 		lblDisplacementThreshold.setToolTipText("Displacement Threshold");
 		panel.add(lblDisplacementThreshold, "cell 0 4,alignx trailing");
 		
 		LDT = new JTextField();
 		LDT.setColumns(10);
 		panel.add(LDT, "flowx,cell 1 4,growx");
 		
 		JLabel label_10 = new JLabel("m");
 		panel.add(label_10, "cell 1 4");
 		
 		JPanel panel_1 = new JPanel();
 		panel_1.setBounds(12, 12, 212, 34);
 		contentPane.add(panel_1);
 		panel_1.setLayout(new MigLayout("", "[68.00,grow][129.00,grow]", "[24px]"));
 		
 		JLabel lblNewLabel = new JLabel("Runway");
 		panel_1.add(lblNewLabel, "cell 0 0,alignx center,aligny center");
 		
 		txtl = new JTextField();	
 		panel_1.add(txtl, "cell 1 0");
 		txtl.setColumns(10);
 		
 		JPanel panel_2 = new JPanel();
 		panel_2.setBounds(12, 267, 463, 45);
 		contentPane.add(panel_2);
 		
 		
 		JPanel panel_3 = new JPanel();
 		panel_3.setBounds(246, 50, 212, 185);
 		contentPane.add(panel_3);
 		panel_3.setLayout(new MigLayout("", "[][grow]", "[][][][][]"));
 		
 		JLabel label = new JLabel("ASDA");
 		label.setToolTipText("Accelerate Stop Distance Available");
 		panel_3.add(label, "cell 0 0,alignx trailing");
 		
 		RASDA = new JTextField();
 		RASDA.setColumns(10);
 		panel_3.add(RASDA, "flowx,cell 1 0,growx");
 		
 		JLabel label_1 = new JLabel("m");
 		panel_3.add(label_1, "cell 1 0");
 		
 		JLabel label_2 = new JLabel("TORA");
 		label_2.setToolTipText("Take-Off Run Available ");
 		panel_3.add(label_2, "cell 0 1,alignx trailing");
 		
 		RTORA = new JTextField();
 		RTORA.setColumns(10);
 		panel_3.add(RTORA, "flowx,cell 1 1,growx");
 		
 		JLabel label_4 = new JLabel("TODA");
 		label_4.setToolTipText("Take-Off Distance Available");
 		panel_3.add(label_4, "cell 0 2,alignx trailing");
 		
 		RTODA = new JTextField();
 		RTODA.setColumns(10);
 		panel_3.add(RTODA, "flowx,cell 1 2,growx");
 		
 		JLabel label_3 = new JLabel("LDA");
 		label_3.setToolTipText("Landing Distance Available");
 		panel_3.add(label_3, "cell 0 3,alignx trailing");
 		
 		RLDA = new JTextField();
 		RLDA.setColumns(10);
 		panel_3.add(RLDA, "flowx,cell 1 3,growx");
 		
 		JLabel label_5 = new JLabel("m");
 		panel_3.add(label_5, "cell 1 1");
 		
 		JLabel label_6 = new JLabel("m");
 		panel_3.add(label_6, "cell 1 2");
 		
 		JLabel label_7 = new JLabel("m");
 		panel_3.add(label_7, "cell 1 3");
 		
 		JLabel lblDt = new JLabel("DT");
 		lblDt.setToolTipText("Displacement Threshold");
 		panel_3.add(lblDt, "cell 0 4,alignx trailing");
 		
 		RDT = new JTextField();
 		RDT.setColumns(10);
 		panel_3.add(RDT, "flowx,cell 1 4,growx");
 		
 		JLabel label_11 = new JLabel("m");
 		panel_3.add(label_11, "cell 1 4");
 		
 		JPanel panel_4 = new JPanel();
 		panel_4.setBounds(246, 12, 212, 34);
 		contentPane.add(panel_4);
 		panel_4.setLayout(new MigLayout("", "[74.00][grow]", "[]"));
 		
 		JLabel label_8 = new JLabel("Runway");
 		panel_4.add(label_8, "cell 0 0,alignx center");
 		
 		txtr = new JTextField();
 		txtr.setColumns(10);
 		panel_4.add(txtr, "cell 1 0");
 		
 		JButton btnNewButton = new JButton("Apply");
 		btnNewButton.setBounds(290, 11, 80, 23);
 		btnNewButton.addActionListener(new ERDokListener(airport, LASDA, LTORA, LTODA, 
 				LLDA, LDT, RASDA, RTORA, RTODA, RLDA, RDT, txtr, txtl, physicalRunwayJList, this, newRunway));
 		panel_2.setLayout(null);
 		panel_2.add(btnNewButton);
 		
 		JButton btnNewButton_1 = new JButton("Cancel");
 		btnNewButton_1.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				setVisible(false);
 			}
 		});
 		btnNewButton_1.setBounds(376, 11, 80, 23);
 		panel_2.add(btnNewButton_1);
 		
 		if(airport.runways().size() > 0 & !newRunway){			
 			int index = physicalRunwayJList.getSelectedIndex();
 			txtl.setText(airport.runways().get(index).getRunway(0).getName());
 			txtr.setText(airport.runways().get(index).getRunway(1).getName());
 			
 			LASDA.setText(Double.toString(airport.runways().get(index).getRunway(0).getASDA(0)));
 			LTORA.setText(Double.toString(airport.runways().get(index).getRunway(0).getTORA(0)));
 			LTODA.setText(Double.toString(airport.runways().get(index).getRunway(0).getTODA(0)));
 			LLDA.setText(Double.toString(airport.runways().get(index).getRunway(0).getLDA(0)));
 			LDT.setText(Double.toString(airport.runways().get(index).getRunway(0).getDisplacedThreshold(0)));
 			RASDA.setText(Double.toString(airport.runways().get(index).getRunway(1).getASDA(0)));
 			RTORA.setText(Double.toString(airport.runways().get(index).getRunway(1).getTORA(0)));
 			RTODA.setText(Double.toString(airport.runways().get(index).getRunway(1).getTODA(0)));
 			RLDA.setText(Double.toString(airport.runways().get(index).getRunway(1).getLDA(0)));
 			RDT.setText(Double.toString(airport.runways().get(index).getRunway(1).getDisplacedThreshold(0)));
 		}		
 		
 		setVisible(true);
 
 	}
 }
 
 class ERDokListener implements ActionListener{
 	Airport airport; 
 	JTextField LASDA; JTextField LTORA; JTextField LTODA; JTextField LLDA; JTextField LDT;
 	JTextField RASDA; JTextField RTORA; JTextField RTODA; JTextField RLDA; JTextField RDT;
 	JTextField RNAME; JTextField LNAME; 
 	JList physicalRunwayJList;
 	JDialog jd;
 	boolean newRunway;
 	public void actionPerformed(ActionEvent e) {
 		if(airport.runways().size() > 0 & !newRunway){ // get the physical runway and change the values
 			int index = physicalRunwayJList.getSelectedIndex();
 			// set the values to what's in the JTextFields
 			airport.runways().get(index).getRunway(0).setASDA(0, doubleParser.parse(LASDA.getText()));
 			airport.runways().get(index).getRunway(0).setTORA(0, doubleParser.parse(LTORA.getText()));
 			airport.runways().get(index).getRunway(0).setTODA(0, doubleParser.parse(LTODA.getText()));
 			airport.runways().get(index).getRunway(0).setLDA(0, doubleParser.parse(LLDA.getText()));
 			airport.runways().get(index).getRunway(0).setDisplacedThreshold(0, doubleParser.parse(LDT.getText()));
 			airport.runways().get(index).getRunway(1).setASDA(0, doubleParser.parse(RASDA.getText()));
 			airport.runways().get(index).getRunway(1).setTORA(0, doubleParser.parse(RTORA.getText()));
 			airport.runways().get(index).getRunway(1).setTODA(0, doubleParser.parse(RTODA.getText()));
 			airport.runways().get(index).getRunway(1).setLDA(0, doubleParser.parse(RLDA.getText()));
 			airport.runways().get(index).getRunway(1).setDisplacedThreshold(0, doubleParser.parse(RDT.getText()));
 //			System.out.println("rdt: " + airport.runways().get(index).getRunway(1).getDisplacedThreshold(0) + " " + RDT.getText());
 			airport.runways().get(index).getRunway(0).setName(LNAME.getText());
 			airport.runways().get(index).getRunway(1).setName(RNAME.getText());
 			
 			airport.runways().get(index).setId(LNAME.getText() + "/" + RNAME.getText());
 		} else { // add a new physical runway and assign the values
 			airport.addPhysicalRunway(new PhysicalRunway(RNAME.getText() + "/" + LNAME.getText(), 
 					new Runway(RNAME.getText(), doubleParser.parse(RTORA.getText()), doubleParser.parse(RASDA.getText()), 
 							doubleParser.parse(RTODA.getText()), doubleParser.parse(RLDA.getText()), doubleParser.parse(RDT.getText())), 
 					new Runway(LNAME.getText(), doubleParser.parse(LTORA.getText()), doubleParser.parse(LASDA.getText()), 
 							doubleParser.parse(LTODA.getText()), doubleParser.parse(LLDA.getText()), doubleParser.parse(LDT.getText()))));						
 		}
 		
 		ArrayList<String> physicalRunwayNames = new ArrayList<String>();
 		for(PhysicalRunway p : airport.runways()){
 			physicalRunwayNames.add(p.getId());
 		}
 		DefaultListModel pr = new DefaultListModel();
 		for(int i = 0; i < physicalRunwayNames.size(); i++){
 			pr.addElement(physicalRunwayNames.get(i));
 		}
 		physicalRunwayJList.setModel(pr);
 		physicalRunwayJList.setSelectedIndex(0);
 		jd.setVisible(false);
 	}
 	public ERDokListener(Airport airport, JTextField lASDA, JTextField lTORA,
 			JTextField lTODA, JTextField lLDA, JTextField lDT,
 			JTextField rASDA, JTextField rTORA, JTextField rTODA,
 			JTextField rLDA, JTextField rDT, JTextField rNAME,
 			JTextField lNAME, JList physicalRunwayJList, JDialog jd, boolean newRunway) {
 		this.airport = airport;
 		LASDA = lASDA;
 		LTORA = lTORA;
 		LTODA = lTODA;
 		LLDA = lLDA;
 		LDT = lDT;
 		RASDA = rASDA;
 		RTORA = rTORA;
 		RTODA = rTODA;
 		RLDA = rLDA;
 		RDT = rDT;
 		RNAME = rNAME;
 		LNAME = lNAME;
 		this.physicalRunwayJList = physicalRunwayJList;
 		this.jd = jd;
 		this.newRunway = newRunway;
 	}
 }
 
 class doubleParser{
 	static double parse(String s){
 		double d = 0;
 		try{
 			d = Double.parseDouble(s);
 		} catch (Exception e) {System.out.println(e);}
 		return d;
 	}
 }
