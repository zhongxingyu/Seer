 /**
  *<p>Title: </p>
  *<p>Description:  </p>
  *<p>Copyright: (c) Washington University, School of Medicine 2004</p>
  *<p>Company: Washington University, School of Medicine, St. Louis.</p>
  *@author Aarti Sharma
  *@version 1.0
  */
 
 package edu.common.dynamicextensions.util.global;
 
import java.util.Vector;
 
 /**
  * @author aarti_sharma
  *
  */
 public class Variables
 {
 
 	public static String dynamicExtensionsHome = new String();
	public static Vector databaseDefinitions = new Vector();
 	public static String databaseDriver = new String();
 	public static String[] databasenames;
 	public static boolean containerFlag = true;
 	public static String applicationCvsTag = new String();
 	public static String hibernateCfgFileName = "hibernate.cfg.xml";
 }
