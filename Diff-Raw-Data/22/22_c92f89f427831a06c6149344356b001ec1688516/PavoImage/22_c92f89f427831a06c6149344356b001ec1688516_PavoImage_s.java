 /**
  * 
  */
 package com.jpii.navalbattle.pavo.io;
 
 import java.awt.image.BufferedImage;
 import java.awt.image.ColorModel;
 import java.awt.image.IndexColorModel;
 import java.awt.image.WritableRaster;
 import java.io.Serializable;
 import java.util.Hashtable;
 
 /**
  * @author maximusvladimir
  *
  */
 public class PavoImage extends BufferedImage implements Serializable {
 
 	/**
 	 * @param arg0
 	 * @param arg1
 	 * @param arg2
 	 */
 	public PavoImage(int arg0, int arg1, int arg2) {
 		super(arg0, arg1, arg2);
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * @param arg0
 	 * @param arg1
 	 * @param arg2
 	 * @param arg3
 	 */
 	public PavoImage(int arg0, int arg1, int arg2, IndexColorModel arg3) {
 		super(arg0, arg1, arg2, arg3);
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * @param arg0
 	 * @param arg1
 	 * @param arg2
 	 * @param arg3
 	 */
 	public PavoImage(ColorModel arg0, WritableRaster arg1, boolean arg2,
 			Hashtable<?, ?> arg3) {
 		super(arg0, arg1, arg2, arg3);
 		// TODO Auto-generated constructor stub
 	}
 
 }
