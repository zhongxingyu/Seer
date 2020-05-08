 package GUI.Box;
 
 import javax.swing.*;
 import javax.swing.event.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import javax.swing.table.*;
 import javax.swing.JDialog.*;
 import java.awt.Rectangle;
 
 import java.util.Properties;
 import org.omg.CORBA.ORB;
 import org.omg.PortableServer.POA;
 import org.omg.PortableServer.POAHelper;
 import org.omg.CosNaming.NameComponent;
 import org.omg.CosNaming.NamingContext;
 import org.omg.CosNaming.NamingContextHelper;
 import broker.radio.*;
 import broker.init.*;
 
 import java.lang.*;
 import javax.xml.parsers.*;
 import org.xml.sax.InputSource;
 import org.w3c.dom.*;
 
 public class BoxScreen extends Thread{
     private Integer i= new Integer(0);
     private Integer laps;
     private String id;
     private JFrame parent;
     // private JTextArea outArea;
 
     private JPanel outPanel;
     private JPanel meanPanel;
 
     private JScrollPane tablePanel;
 
     private JTable outTable;
 
     public DefaultTableModel model = new DefaultTableModel(); 
 
     private GridBagConstraints meanPanelGrid;
     private GridBagConstraints outPanelGrid;
 
     private JTextField textBoxGas = new JTextField("",15);
     private JTextField textBoxTyre = new JTextField("",15);
 
     private JLabel labelGas = new JLabel(" l / km ");
     private JLabel labelTyre = new JLabel(" % / km ");
     private JLabel labelGasExpl = new JLabel("Mean fuel consumption");
     private JLabel labelTyreExpl = new JLabel("Mean tyre usury");
     private JLabel labelInfo_1 = new JLabel("Team - , Competitor -");
     private JLabel labelInfo_2 = new JLabel("Max speed - , Max acceleration = -");
     private JLabel labelInfo_3 = new JLabel("Tank capacity = , Tyre mixture = -");
     private JLabel labelInfo_4 = new JLabel("Tyre usury = - , Fuel level = -");
     private JLabel labelInfo_6 = new JLabel("Laps to pitstop = - , Driving style -");
     private JLabel labelInfo_7 = new JLabel("Pitstop delay at lap - = -");
 
 
     private String boxCorbaLoc;
     private String monitorBoxCorbaLoc;
     private String configuratorCorbaLoc;
     private String monitorCorbaLoc;
     private ORB orb;
     private String ritorno = new String();
 
     private Double gasLevelValue;
     private Double tyreUsuryValue;
     private Integer sectorValue;
     private Integer lapsValue;
     private Double metresValue;
     private Double meanSpeedValue;
     private Double meanTyreUsuryValue;
     private Double meanGasConsumptionValue;
     private String styleValue;
     private String tyreValue;
     private Double gasLevelStrategyValue;
     private Integer pitstopLapValue;
     private float pitstopDelayValue;
 
 
     private String teamValue;
     private String firstnameValue;
     private String lastnameValue;
     private Double maxspeedValue;
     private Double maxaccelerationValue;
     private Double gastankcapacityValue;
     private String engineValue;
     private Double tyreUsuryCarValue;
     private Double gasLevelCarValue;
     private String mixtureValue;
     private String typetyreValue;
 
     public BoxScreen(String id_In, String xmlCompetitor){
 	id=id_In;
 	parent = new JFrame("BoxMonitor n° "+id_In);
 	readXmlCompetitor(xmlCompetitor);
 	// init();
     }
 
     public void createBoxOutput(){
 	outPanelGrid = new GridBagConstraints();
 	outPanel = new JPanel(new BorderLayout());
 	outPanel.setLayout(new GridBagLayout());
 	outPanel.setBorder(BorderFactory.createTitledBorder(null, "Box Output", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION));
 	// boxConfigurationGrid = new GridBagConstraints();
 	// outArea = new JTextArea(35,30);
 	// outArea = new JTextArea(20,35);
 	// outPanel.add(outArea);
 	outPanelGrid.fill = GridBagConstraints.HORIZONTAL;
 	outPanelGrid.gridx = 0;
 	outPanelGrid.gridy = 0;
 	outPanelGrid.ipady = 5;
 	outPanel.add(labelInfo_1, outPanelGrid);
 
 	outPanelGrid.fill = GridBagConstraints.HORIZONTAL;
 	outPanelGrid.gridx = 0;
 	outPanelGrid.gridy = 1;
 	outPanelGrid.ipady = 5;
 	outPanel.add(labelInfo_2, outPanelGrid);
 
 	outPanelGrid.fill = GridBagConstraints.HORIZONTAL;
 	outPanelGrid.gridx = 0;
 	outPanelGrid.gridy = 2;
 	outPanelGrid.ipady = 5;
 	outPanel.add(labelInfo_3, outPanelGrid);
 	outPanelGrid.fill = GridBagConstraints.HORIZONTAL;
 	outPanelGrid.gridx = 0;
 	outPanelGrid.gridy = 3;
 	outPanelGrid.ipady = 5;
 	outPanel.add(labelInfo_4, outPanelGrid);
 	outPanelGrid.fill = GridBagConstraints.HORIZONTAL;
 	outPanelGrid.gridx = 0;
 	outPanelGrid.gridy = 4;
 	outPanelGrid.ipady = 5;
 	outPanel.add(labelInfo_6, outPanelGrid);
 	outPanelGrid.fill = GridBagConstraints.HORIZONTAL;
 	outPanelGrid.gridx = 0;
 	outPanelGrid.gridy = 5;
 	outPanelGrid.ipady = 5;
 	outPanel.add(labelInfo_7, outPanelGrid);
 
     }
     public void createConsumptionMeans(){
 
 	meanPanelGrid = new GridBagConstraints();
 	meanPanel = new JPanel(new BorderLayout());
 	meanPanel.setLayout(new GridBagLayout());
 	meanPanel.setBorder(BorderFactory.createTitledBorder(null, "Means", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION));
 
 	meanPanelGrid.fill = GridBagConstraints.HORIZONTAL;
 	meanPanelGrid.gridx = 0;
 	meanPanelGrid.gridy = 0;
 	meanPanelGrid.ipady = 5;
 	meanPanel.add(labelGasExpl, meanPanelGrid);
 
 	meanPanelGrid.fill = GridBagConstraints.HORIZONTAL;
 	meanPanelGrid.gridx = 0;
 	meanPanelGrid.gridy = 1;
 	meanPanelGrid.ipady = 5;
 	meanPanel.add(textBoxGas, meanPanelGrid);
 	meanPanelGrid.fill = GridBagConstraints.HORIZONTAL;
 	meanPanelGrid.gridx = 1;
 	meanPanelGrid.gridy = 1;
 	meanPanelGrid.ipady = 5;
 	meanPanel.add(labelGas, meanPanelGrid);
 
 	meanPanelGrid.fill = GridBagConstraints.HORIZONTAL;
 	meanPanelGrid.gridx = 0;
 	meanPanelGrid.gridy = 2;
 	meanPanelGrid.ipady = 5;
 	meanPanel.add(labelTyreExpl, meanPanelGrid);
 
 	meanPanelGrid.fill = GridBagConstraints.HORIZONTAL;
 	meanPanelGrid.gridx = 0;
 	meanPanelGrid.gridy = 3;
 	meanPanelGrid.ipady = 5;
 	meanPanel.add(textBoxTyre, meanPanelGrid);
 	meanPanelGrid.fill = GridBagConstraints.HORIZONTAL;
 	meanPanelGrid.gridx = 1;
 	meanPanelGrid.gridy = 3;
 	meanPanelGrid.ipady = 5;
 	meanPanel.add(labelTyre, meanPanelGrid);
 
     }
 
     public void createTableOutput(){
 	model.addColumn("Lap");
 	model.addColumn("Sector");
 	model.addColumn("Fuel Level (l)");
 	model.addColumn("Tyre Usury (%)");
 	model.addColumn("Time (h:m:s:mll)");
 
 	outTable = new JTable(model);
 	TableColumn column = null;
 	column = outTable.getColumnModel().getColumn(0);
 	column.setPreferredWidth(20);
 	column = outTable.getColumnModel().getColumn(1);
 	column.setPreferredWidth(20);
 
 	tablePanel = new JScrollPane(outTable);
 	tablePanel.setPreferredSize(new Dimension(500, 250));
 
 	tablePanel.setVerticalScrollBar(new JScrollBar());
 	tablePanel.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 
     }
 
     public void run(){
 	createBoxOutput();
 	createConsumptionMeans();
 	createTableOutput();
 	parent.add(tablePanel, BorderLayout.EAST);
 	parent.add(outPanel,BorderLayout.SOUTH);
 	parent.add(meanPanel,BorderLayout.NORTH);
 
 	parent.pack();
 	parent.setVisible(true);
 	parent.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	try{
 	    org.omg.CORBA.Object obj = orb.string_to_object(configuratorCorbaLoc);
 	    // outArea.setText("Pre narrow");
 	    BoxConfigurator conf = BoxConfiguratorHelper.narrow(obj);
 	    // outArea.append("\nConf initialized, invoke configure");
 	    conf.Configure("boxConfig-"+id+".xml");
 	    // outArea.append("\nAfter configure");
 	    // outArea.append("\n pre string_to_object");
 	    org.omg.CORBA.Object obj_radio = orb.string_to_object(monitorBoxCorbaLoc);
 	    // outArea.append("\n pre narrow box_monitor");
 	    Box_Monitor_Radio comp_radio = Box_Monitor_RadioHelper.narrow(obj_radio);
 	    // outArea.append("\npre getupdate");
 	    short i=1;
 	    short qee=1;
 	    org.omg.CORBA.FloatHolder j=new org.omg.CORBA.FloatHolder(0);
 	    String temp;
 	    while(i<=(laps*3)){
		temp = comp_radio.GetUpdate(i,j);
 		readXml(temp);
 
 		double gas=(double)(gasLevelValue);
 		double tyre=(double)(tyreUsuryValue);
 		int decimal=1000; //3 cifre decimali
 		int  gasInt= (int)(decimal*gas);
 		int tyreInt = (int)(decimal*tyre);
 		double gasPrint=(double)gasInt/(double)decimal;
 		double tyrePrint=(double)tyreInt/(double)decimal;
 		String time = convert(j.value);
 		/*
 		  int ore = (int)(j.value/3600);
 		  int minuti = (int)(j.value/60);
 		  int secondi = (int)(j.value-(minuti*60+ore*3600));
 		  int millesimi = (int)((j.value - (minuti*60+ore*3600+secondi))*1000);
 		  String time;
 		  if(secondi <10){
 		  if(millesimi<10){
 		  time = new String(ore+":"+minuti+":0"+secondi+":00"+millesimi);}
 		  else if(millesimi<100){
 		  time = new String(ore+":"+minuti+":0"+secondi+":0"+millesimi);}
 		  else{
 		  time = new String(ore+":"+minuti+":0"+secondi+":"+millesimi);}
 		  }
 		  else{
 		  if(millesimi<10){
 		  time = new String(ore+":"+minuti+":"+secondi+":00"+millesimi);}
 		  else if(millesimi<100){
 		  time = new String(ore+":"+minuti+":"+secondi+":0"+millesimi);}
 		  else{
 		  time = new String(ore+":"+minuti+":"+secondi+":"+millesimi);}
 		  }*/
 		if( gas <= 0.0 || tyre >100.0 ){
 		    model.insertRow(0,new Object[]{lapsValue,sectorValue,"RITIRED","RITIRED",time});
 		    ListSelectionModel selectionModel = outTable.getSelectionModel();
 		    // outTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
 		    selectionModel.setSelectionInterval(0,0);
 		    i=(short)((laps*3)+1);
 		}
 		else{model.insertRow(0,new Object[]{lapsValue, sectorValue, gasPrint,tyrePrint, time});//j.value});
 		    ListSelectionModel selectionModel = outTable.getSelectionModel();
 		    // outTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
 		    selectionModel.setSelectionInterval(0,0);
 		    System.out.println(temp);
 		    textBoxGas.setText(meanGasConsumptionValue.toString());//aggiorno consumo medio
 		    textBoxTyre.setText(meanTyreUsuryValue.toString());//aggiorno velocità media
 		    if (pitstopLapValue !=null){
 			/*outArea.setText("Strategy:");
 			  outArea.append("\n-Laps to pitstop : "+pitstopLapValue.toString());
 			  outArea.append("\n-Style of guide :"+ styleValue);*/
 			labelInfo_6.setText("Laps to pitstop = "+pitstopLapValue.toString()+", Driving style "+styleValue);
 			if (pitstopLapValue == 0 && pitstopDelayValue !=-1.0){
 			    // JOptionPane.showMessageDialog(parent, "Total time for change tyre and refill fuel = "+convert(pitstopDelayValue), "Message from competition", JOptionPane.INFORMATION_MESSAGE);
 			    // outArea.append("\nPITSTOP --> tempo totale del pitstop : "+pitstopDelayValue+" secondi");
 			    labelInfo_7.setText("Total pitstop delay at lap "+lapsValue+" = "+pitstopDelayValue);
 			    pitstopDelayValue = (float)-1.0;
 			}
 
 			// outPanel.updateUI();
 			// outArea.append("\n----------\nInitial Configuration");
 			// outArea.append("\nTeam "+teamValue+", Competitor "+firstnameValue+" "+lastnameValue);
 			// outArea.append("\nmax speed = "+maxspeedValue.toString()+", max acceleration = "+maxaccelerationValue.toString()+"\ntank capacity = "+gastankcapacityValue.toString()+"style = "+engineValue+"\ntyre usury = "+tyreUsuryCarValue.toString()+" , fuel level = "+gasLevelCarValue.toString()+"\ntype tye ="+typetyreValue+", mixture = "+mixtureValue);
 
 			labelInfo_1.setText("Team "+teamValue+", Competitor "+firstnameValue+" "+lastnameValue);
 			labelInfo_2.setText("Max speed = "+maxspeedValue.toString()+", Max acceleration = "+maxaccelerationValue.toString());
 			labelInfo_3.setText("Tank capacity = "+gastankcapacityValue.toString()+ ", Tyre mixture = "+mixtureValue);
 			labelInfo_4.setText("Tyre usury = "+tyreUsuryCarValue.toString()+" , Fuel level = "+gasLevelCarValue.toString());
 			outPanel.updateUI();
 			/*
 			  private Double ;
 			  private Double ;
 			  private String ;
 			  private String ;*/
 		    }
 		    i=(short)(i+1);
 		    System.out.println("after run invoke");
 		    // this.sleep(500);
 		    // parent.repaint();
 		    // }
 		}
 	    }
 	    model.insertRow(0,new Object[]{"---", "---","---", "---", "---", "---", "---"});
 	    ListSelectionModel selectionModel = outTable.getSelectionModel();
 	    selectionModel.setSelectionInterval(0,0);
 
 	}
 	catch(NullPointerException e){
 // 	    e.printStackTrace();
 // 	    JOptionPane.showMessageDialog(parent, "Attention : NullPointerException", "Error", JOptionPane.ERROR_MESSAGE);
 	}
 	// catch()
 	catch (Exception e){
 // 	    System.out.println("Eccezione");
 // 	    JOptionPane.showMessageDialog(parent, "Attention : Exception", "Error", JOptionPane.ERROR_MESSAGE);
 // 	    e.printStackTrace();
 	}
     }
 
 
     public void init(String boxCorbaLocIn, String monitorBoxCorbaLocIn, String configuratorCorbaLocIn, String monitorCorbaLocIn, ORB orbIn, short lapsIn){
 	System.out.println("BoxMonitor.init");
 	boxCorbaLoc = boxCorbaLocIn;
 	monitorBoxCorbaLoc = monitorBoxCorbaLocIn;
 	configuratorCorbaLoc = configuratorCorbaLocIn;
 	monitorCorbaLoc = monitorCorbaLocIn;
 	orb = orbIn;
 	laps = new Integer(lapsIn);
     }
 
     public void readXml(String xmlRecords){
 	System.out.println("stringa da parsare : \n"+xmlRecords);
 	// String xmlRecords ="<?xml version=\"1.0\"?><update><gasLevel>35.58887482</gasLevel><!--prova--><tyreUsury>17.07367134</tyreUsury><lap>18</lap><sector>3</sector><metres>686.00000000</metres></update>";
 	try {
 	    DocumentBuilderFactory dbf =
 		DocumentBuilderFactory.newInstance();
 	    DocumentBuilder db = dbf.newDocumentBuilder();
 	    InputSource is = new InputSource();
 	    is.setCharacterStream(new StringReader(xmlRecords));
 
 	    Document doc = db.parse(is);
 	    //         NodeList nodes3 = doc.getElementsByTagName("update");
 	    NodeList nodes = doc.getElementsByTagName("status");
 	    // 	Element element = (Element) nodes.item(i);
 
 	    int i=0;
 	    Element element = (Element) nodes.item(i);
 	    NodeList gasLevel = element.getElementsByTagName("gasLevel");
 	    Element line = (Element) gasLevel.item(0);
 	    gasLevelValue = new Double(getCharacterDataFromElement(line));
 	    System.out.println("gaslevelvalue : "+gasLevelValue);
         
 	    NodeList tyreUsury = element.getElementsByTagName("tyreUsury");
 	    line = (Element) tyreUsury.item(0);
 	    tyreUsuryValue = new Double(getCharacterDataFromElement(line));
 	    System.out.println("tyre usury : "+tyreUsuryValue);
 
 	    NodeList lap = element.getElementsByTagName("lap");
 	    line = (Element) lap.item(0);
 	    System.out.println("lap: " + getCharacterDataFromElement(line));
 	    lapsValue = new Integer(getCharacterDataFromElement(line));
 	    System.out.println("laps : "+lapsValue);
 
 	    NodeList sector = element.getElementsByTagName("sector");
 	    line = (Element) sector.item(0);
 	    sectorValue = new Integer(getCharacterDataFromElement(line));
 	    System.out.println("sector : "+sectorValue);
 
	    NodeList metres = element.getElementsByTagName("metres");
	    line = (Element) metres.item(0);
	    metresValue = new Double(getCharacterDataFromElement(line));
	    System.out.println("metres : "+metresValue);
 
 	    NodeList meanTyreUsury = element.getElementsByTagName("meanTyreUsury");
 	    line = (Element) meanTyreUsury.item(0);
 	    meanTyreUsuryValue = new Double(getCharacterDataFromElement(line));
 	    System.out.println("meantyreusury : "+meanSpeedValue);
 
 	    NodeList meanGasConsumption = element.getElementsByTagName("meanGasConsumption");
 	    line = (Element) meanGasConsumption.item(0);
 	    meanGasConsumptionValue = new Double(getCharacterDataFromElement(line));
 	    System.out.println("meanGasConsumption :"+meanGasConsumptionValue);
 	    try{
 		System.out.println("nel try della parte della strategia");
 
 		NodeList nodes2 = doc.getElementsByTagName("strategy");
 		Element element2 = (Element) nodes2.item(i);
 
 		NodeList tyreType = element2.getElementsByTagName("tyreType");
 		line = (Element) tyreType.item(0);
 		tyreValue = new String(getCharacterDataFromElement(line));
 
 		NodeList style = element2.getElementsByTagName("style");
 		line = (Element) style.item(0);
 		styleValue = new String(getCharacterDataFromElement(line));
 
 		NodeList gasLevel2 = element2.getElementsByTagName("gasLevel");
 		line = (Element) gasLevel2.item(0);
 		gasLevelStrategyValue = new Double(getCharacterDataFromElement(line));
 
 		NodeList pitStopLaps = element2.getElementsByTagName("pitStopLaps");
 		line = (Element) pitStopLaps.item(0);
 		pitstopLapValue = new Integer(getCharacterDataFromElement(line));
 
 		NodeList pitStopDelay = element2.getElementsByTagName("pitStopDelay");
 		line = (Element) pitStopDelay.item(0);
 		pitstopDelayValue = new Float(getCharacterDataFromElement(line)).floatValue();
 	    }
 	    catch(Exception e){
 // 		System.out.println("strategia non presente");}
 	}
 	catch (Exception e) {
 // 	    e.printStackTrace();
  	    System.out.println("eccezione in readXml");
 	}
     }
 
     public void readXmlCompetitor(String xmlRecords){
 	try {
 	    DocumentBuilderFactory dbf =
 		DocumentBuilderFactory.newInstance();
 	    DocumentBuilder db = dbf.newDocumentBuilder();
 	    InputSource is = new InputSource();
 	    is.setCharacterStream(new StringReader(xmlRecords));
 
 	    Document doc = db.parse(is);
 	    //         NodeList nodes3 = doc.getElementsByTagName("update");
 	    NodeList nodes = doc.getElementsByTagName("driver");
 	    // nodes = doc.getElementsByTagName("car_driver");
 	    // 	Element element = (Element) nodes.item(i);
 
 	    int i=0;
 
 	    Element element = (Element) nodes.item(i);
 	    NodeList team = element.getElementsByTagName("team");
 	    Element line = (Element) team.item(0);
 	    teamValue = new String(getCharacterDataFromElement(line));
 	    System.out.println("team value "+ teamValue);
 
 	    element = (Element) nodes.item(i);
 	    NodeList firstname = element.getElementsByTagName("firstname");
 	    line = (Element) firstname.item(0);
 	    firstnameValue = new String(getCharacterDataFromElement(line));
 	    System.out.println("first "+firstnameValue);
 
 	    element = (Element) nodes.item(i);
 	    NodeList lastname = element.getElementsByTagName("lastname");
 	    line = (Element) lastname.item(0);
 	    lastnameValue = new String(getCharacterDataFromElement(line));
 
 	    nodes = doc.getElementsByTagName("car");
 	    System.out.println("in car");
 	    element = (Element) nodes.item(i);
 	    NodeList maxspeed = element.getElementsByTagName("maxspeed");
 	    line = (Element) maxspeed.item(0);
 	    maxspeedValue = new Double(getCharacterDataFromElement(line));
 
 	    element = (Element) nodes.item(i);
 	    NodeList maxacceleration = element.getElementsByTagName("maxacceleration");
 	    line = (Element) maxacceleration.item(0);
 	    maxaccelerationValue = new Double(getCharacterDataFromElement(line));
 
 	    element = (Element) nodes.item(i);
 	    NodeList gastank = element.getElementsByTagName("gastankcapacity");
 	    line = (Element) gastank.item(0);
 	    gastankcapacityValue = new Double(getCharacterDataFromElement(line));
 
 	    element = (Element) nodes.item(i);
 	    NodeList engine = element.getElementsByTagName("engine");
 	    line = (Element) engine.item(0);
 	    engineValue = new String(getCharacterDataFromElement(line));
 
 	    element = (Element) nodes.item(i);
 	    NodeList tyre = element.getElementsByTagName("tyreusury");
 	    line = (Element) tyre.item(0);
 	    tyreUsuryCarValue = new Double(getCharacterDataFromElement(line));
 
 	    element = (Element) nodes.item(i);
 	    NodeList level = element.getElementsByTagName("gasolinelevel");
 	    line = (Element) level.item(0);
 	    gasLevelCarValue = new Double(getCharacterDataFromElement(line));
 
 	    element = (Element) nodes.item(i);
 	    NodeList mixture = element.getElementsByTagName("mixture");
 	    line = (Element) mixture.item(0);
 	    mixtureValue = new String(getCharacterDataFromElement(line));
 
 	    element = (Element) nodes.item(i);
 	    NodeList type = element.getElementsByTagName("type_tyre");
 	    line = (Element) type.item(0);
 	    typetyreValue = new String(getCharacterDataFromElement(line));
 	}
 	catch (Exception e){
 // e.printStackTrace();
 }
 
     }
     public static String getCharacterDataFromElement(Element e) {
 	Node child = e.getFirstChild();
 	if (child instanceof CharacterData) {
 	    CharacterData cd = (CharacterData) child;
 	    return cd.getData();
 	}
 	return "-";
     }
     public String convert(float timeIn){
 
 	int ore = (int)(timeIn/3600);
 	int minuti = (int)(timeIn/60);
 	int secondi = (int)(timeIn-(minuti*60+ore*3600));
 	int millesimi = (int)((timeIn - (minuti*60+ore*3600+secondi))*1000);
 	String time;
 	if(minuti<10){
 	    if(secondi <10){
 		if(millesimi<10){
 		    time = new String("0"+ore+":0"+minuti+":0"+secondi+":00"+millesimi);}
 		else if(millesimi<100){
 		    time = new String("0"+ore+":0"+minuti+":0"+secondi+":0"+millesimi);}
 		else{
 		    time = new String("0"+ore+":0"+minuti+":0"+secondi+":"+millesimi);}
 	    }
 	    else{
 		if(millesimi<10){
 		    time = new String("0"+ore+":0"+minuti+":"+secondi+":00"+millesimi);}
 		else if(millesimi<100){
 		    time = new String("0"+ore+":0"+minuti+":"+secondi+":0"+millesimi);}
 		else{
 		    time = new String("0"+ore+":0"+minuti+":"+secondi+":"+millesimi);}
 	    }
 	}
 	else{
 	    if(secondi <10){
 		if(millesimi<10){
 		    time = new String("0"+ore+":"+minuti+":0"+secondi+":00"+millesimi);}
 		else if(millesimi<100){
 		    time = new String("0"+ore+":"+minuti+":0"+secondi+":0"+millesimi);}
 		else{
 		    time = new String("0"+ore+":"+minuti+":0"+secondi+":"+millesimi);}
 	    }
 	    else{
 		if(millesimi<10){
 		    time = new String("0"+ore+":"+minuti+":"+secondi+":00"+millesimi);}
 		else if(millesimi<100){
 		    time = new String("0"+ore+":"+minuti+":"+secondi+":0"+millesimi);}
 		else{
 		    time = new String("0"+ore+":"+minuti+":"+secondi+":"+millesimi);}
 	    }}
 
 	return time;
     }
 
     // public static void main(String[] args){
     // BoxMonitor b = new BoxMonitor(args[0]);
     // b.init();
     // }
 }
