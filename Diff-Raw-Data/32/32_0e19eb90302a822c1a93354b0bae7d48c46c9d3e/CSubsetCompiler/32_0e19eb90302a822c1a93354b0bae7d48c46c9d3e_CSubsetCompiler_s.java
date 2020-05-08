 package de.haw.cip;
 
 import javax.swing.UIManager;
 import de.haw.cip.gui.CIPWindow;
 import de.mk.exception.ExceptionHandler;
 
 /**
 * <p>�berschrift: Compiler & Interpreter Praktikum</p>
 * <p>Beschreibung: Compiler f�r Subset von C</p>
  * <p>Copyright: Copyright (c) 2002</p>
  * <p>Organisation: </p>
  * @author Martin Koose & Marco Behnke
  *
  * This Program can compile source files written in a subset of the
  * c programming language. To understand what commands can be used, look at <i>C Subset Grammar.doc</i>.
  *
  * Compiled programs can be run on a 68K machine.
  */
 public class CSubsetCompiler {
 
 	/**
 	 * Main executble method
 	 */
 	public static void main(String[] args) {
 		CIPWindow cip;
 		try {
 			cip = new CIPWindow();
 			cip.hide();
 			cip.setBounds(25, 25, 800, 600);
 			cip.show();
 		} catch (Exception e) {
 			System.out.println("STARTER_ERROR");
 			e.printStackTrace();
 			ExceptionHandler.instance(e);
 		}
 	}
 }
