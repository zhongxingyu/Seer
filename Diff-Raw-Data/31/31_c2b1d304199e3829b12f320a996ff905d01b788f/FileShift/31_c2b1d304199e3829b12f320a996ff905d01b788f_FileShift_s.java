 package de.splashfish.util;
 
 import java.io.File;
 
 public class FileShift {
 
 	public static final int FROM_FILE 	= 0;
 	public static final int TO_FILE		= 1;
 	
 	private File from;
 	private File to;
 	
 	public FileShift(File from, File to) {
 		this.from 	= from;
 		this.to 	= to;
 	}
 	
 	public File get(int i) {
 		switch(i) {
 			case 0: 	return from;	
 			case 1: 	return to;		
 			default: 	return null;
 		}
 	}
 	
	public boolean equals(FileShift shift) {
		return (from.equals(shift.get(FROM_FILE)) && to.equals(shift.get(TO_FILE)));
 	}
 }
