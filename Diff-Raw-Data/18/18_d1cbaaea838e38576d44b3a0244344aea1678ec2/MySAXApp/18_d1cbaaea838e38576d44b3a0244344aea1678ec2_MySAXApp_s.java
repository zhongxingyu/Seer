 package com.shawnhurley;
 
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 
 import javax.swing.JFormattedTextField;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 
 import org.xml.sax.Attributes;
 
 import org.xml.sax.helpers.DefaultHandler;
 public class MySAXApp extends DefaultHandler {
 	private JPanel testPanel;
 	private int yvalue;
 	private int xvalue;
 	private int countobjectfield = 0;
 	String elementName = null;
 	private GridBagConstraints c = new GridBagConstraints();
 	public MySAXApp(){
 		super();
 	}
     public void startDocument ()
     {
     	testPanel= new JPanel(new GridBagLayout());
     	yvalue = 0;
     	xvalue = 0;
     }
    
 
     public void startElement (String uri, String name, String qName, Attributes attributes)
     {
     	System.out.println(name + " "+ qName + " "+ attributes);
     	if (qName == "objectfield" && countobjectfield==0){
 			countobjectfield++; //counts the object field so that we can indent later ones
 		}
 		if(qName == "name"){
 			yvalue++;
 			elementName = qName;
 		}
 		if(qName == "value"){
 			elementName = qName;
 		}
 		if(qName == "fields"){
 			xvalue--;
 			elementName = qName;
 		}
 		if (qName == "objectfield" && countobjectfield >= 1){
 			elementName = qName;
 			yvalue++;
			
 		}
 		else{
 			elementName = qName;
 		}
 	}
 	
 
 
     public void endElement (String uri, String name, String qName)
     {
     	if(qName == "objectfield"){
    		yvalue--;
     	}
     }
 
 
     public void characters (char ch[], int start, int length)
     {
     	if(elementName == "intfield" || elementName=="stringfield"){
     		
     	}
     	if(elementName == "name"){
     		String thing = null;
     		//String thing = ch.toString();
     		StringBuffer sb = new StringBuffer();
     		for (int i = start; i < start+ length; i++) {
     			 	sb.append(ch[i]);
     		}
     		System.out.println();
     		c.fill = GridBagConstraints.HORIZONTAL;
     		c.gridx = xvalue;
     		c.gridy = yvalue;
     		JLabel testLabel = new JLabel(sb.toString());
     		testPanel.add(testLabel,c);
     	}
     	if(elementName == "value"){
     		xvalue--;
     		StringBuffer sb = new StringBuffer();
     		for (int i = start; i < start + length; i++) {
 				sb.append(ch[i]);
 			}
     		c.fill = GridBagConstraints.HORIZONTAL;
     		c.gridx=xvalue;
     		c.gridy=yvalue;
     		JFormattedTextField testField = new JFormattedTextField(sb.toString());
     		testPanel.add(testField,c);
     		xvalue++;
     	}
     }
     public JPanel getTestPanel(){
     	return testPanel;
     }
 }
