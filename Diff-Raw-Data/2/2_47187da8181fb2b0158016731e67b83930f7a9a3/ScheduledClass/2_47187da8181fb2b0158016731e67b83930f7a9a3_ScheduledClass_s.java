 package uk.co.crowderconsult.sch;
 
 import java.awt.*;
 import java.io.*;
 import java.util.*;
 
 import javax.swing.*;
 
 import uk.co.crowderconsult.ini4j.*;
 import uk.co.crowderconsult.ini4j.Ini.Section;
 
 
 
 /**
  *
  * @author Graham Allan
  * @version 1.0
  *
  */
 @Data
 public class ScheduledClass {
 
   private int valueA;
   private int valueB;
   private int valueC;
   private FirstLib lib = new FirstLib();
 
 	/**
 	 *
 	 * @param a First Value
 	 * @param b Second Value
 	 * @return Int addition of a and b
 	 */
 	public int add(int a, int b){
 		return a + b;
 	}
 
 
 	public static void main(String[] args) {
 		try {
 			ScheduledClass mc = new ScheduledClass();
 			StringBuffer sb = new StringBuffer();
 			sb.append(mc.add(10,23));
 			sb.append("\n");
 			sb.append(mc.add(10,23));
 			sb.append("\n");
 			sb.append(new VatCalc().calcualteVAT(mc.add(10,23)));
 			sb.append("\n");
 			sb.append(mc.lib.abs(345.634));
 			sb.append("\n");
 			VatCalc vc = new VatCalc();
 			vc.setVatRate(0.56);
 			sb.append("VAT Rate ");
 			sb.append(vc.getVatRate());
 			sb.append("\n");
 			sb.append("Type ");
 			sb.append(vc.getChart().getPlot().getPlotType());
 			sb.append("\n");
 			sb.append(String.format("%2$s.%1$s@foo.com\n", "allan","graham"));
 
 			Ini iniFile = new Ini();
 			Section iniSection = iniFile.add("MySection");
 			iniSection.put("Value1", (new Date()).toLocaleString());
 			iniSection.put("Value2","B");
 			iniSection.put("Value3","V");
 			try {
 				new File("./MyIniFile.ini").delete();
 				sb.append("Deleted\n");
 			} catch (Exception e1) {
 				sb.append("VAT Rate ");
 				sb.append(e1.getLocalizedMessage());
 				sb.append("\n");
 			}
 			try {
 				iniFile.store(new FileWriter(new File("./MyIniFile.ini")));
 				sb.append("Created\n");
 			} catch (IOException e) {
 				e.printStackTrace();
 				sb.append("VAT Rate ");
 				sb.append(e.getLocalizedMessage());
 				sb.append("\n");
 			}
 			JOptionPane.showMessageDialog(null, sb.toString());
 
 		} catch (HeadlessException e) {
 			JOptionPane.showMessageDialog(null, e.getLocalizedMessage());
 		}
 
 	}
 }
