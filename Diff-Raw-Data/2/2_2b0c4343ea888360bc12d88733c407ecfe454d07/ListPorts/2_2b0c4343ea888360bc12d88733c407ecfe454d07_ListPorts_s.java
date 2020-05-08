 package com.dropoutdesign.ddf.test;
 
 import java.io.*;
 import java.util.*;
import gnu.io.*;
 
 public class ListPorts {
     static Enumeration portList;
     static CommPortIdentifier portId;
     static SerialPort serialPort;
 
     public static void main(String[] args) {
         portList = CommPortIdentifier.getPortIdentifiers();
 
         while (portList.hasMoreElements()) {
             portId = (CommPortIdentifier) portList.nextElement();
             if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
 				System.out.println(portId.getName());
             }
         }
     }
 }
