 package waba.io.impl;
 
 import java.util.Vector;
 import java.util.Properties;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileInputStream;
 
 public class SerialChoiceDialog extends java.awt.Dialog implements java.awt.event.ActionListener, java.awt.event.ItemListener{
 java.awt.Choice choice = null;
 	public SerialChoiceDialog(java.awt.Frame parent){
 		super(parent,"Choose Serial Port",false);
 		java.awt.Panel buttonPanel = new java.awt.Panel();
 		java.awt.Button cancel = new java.awt.Button("Cancel");
 		cancel.addActionListener(this);
 		buttonPanel.add(cancel);
 		java.awt.Button save = new java.awt.Button("Save");
 		save.addActionListener(this);
 		buttonPanel.add(save);
 		add(buttonPanel,java.awt.BorderLayout.SOUTH);
 		choice = new java.awt.Choice();
 		int index = -1;
 		for(java.util.Enumeration pe = SerialManager.getSerialPorts();pe.hasMoreElements();){
 			SerialPortDesc portDesc = (SerialPortDesc)pe.nextElement();
 			if(!portDesc.name.startsWith("Infrared") && !portDesc.name.startsWith("Internal")){
 				choice.add(portDesc.name);
 				index = portDesc.index;
 			}
 		}
 		if(choice.getItemCount() < 1) save.setEnabled(false);
 		System.out.println("choice.getItemCount() "+choice.getItemCount()+" index "+index);
 		if(index >=0 && index <=choice.getItemCount()){
 			choice.select(index);
 		}
 		
 		choice.addItemListener(this);
 		add(choice,java.awt.BorderLayout.CENTER);
 
		setResizable(false);
 		pack();
 		show();
 	}
 	
 	public void actionPerformed(java.awt.event.ActionEvent ae){
 		if(ae.getActionCommand().equals("Save")){
 			String choosenPortName = (String)choice.getSelectedItem();
 			for(java.util.Enumeration pe = SerialManager.getSerialPorts();pe.hasMoreElements();){
 				SerialPortDesc portDesc = (SerialPortDesc)pe.nextElement();
 				if(portDesc.name.equals(choosenPortName)){
 					SerialManager.assignZeroPort(portDesc);
 					break;
 				}
 			}
 		}
 		dispose();
 	}
 	public void itemStateChanged(java.awt.event.ItemEvent ie){}
 
 }
 
 
 
