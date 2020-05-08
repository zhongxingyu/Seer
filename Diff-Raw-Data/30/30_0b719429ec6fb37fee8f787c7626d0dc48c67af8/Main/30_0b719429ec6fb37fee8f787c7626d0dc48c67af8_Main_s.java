 package net.frontlinesms.messaging;
 
import java.io.*;
import java.util.*;

 import serial.*;
 
 /**
  * A commandline utility for detecting connected AT devices.
  * @author Alex Anderson alex@frontlinesms.com
  */
 public class Main {
 	public static void main(String[] args) {
		SerialClassFactory.init();
 		AllModemsDetector amd = new AllModemsDetector();
 		ATDeviceDetector[] detectors = amd.detectBlocking();
 		printReport(detectors);
 	}
 	
 	/** Prints a report to {@link System#out} detailing the devices that were detected. */
 	private static void printReport(ATDeviceDetector[] completedDetectors) {
 		// All detectors are finished, so print a report
 		for(ATDeviceDetector d : completedDetectors) {
 			System.out.println("---");
 			System.out.println("PORT   : " + d.getPortIdentifier().getName());
 			if(d.isDetected()) {
 				System.out.println("SERIAL : " + d.getSerial());
 				System.out.println("BAUD   : " + d.getMaxBaudRate());
 			} else {
 				System.out.println("DETECTION FAILED");
 				System.out.println("> " + d.getExceptionMessage());
 			}
 		}	
 	}
 }
