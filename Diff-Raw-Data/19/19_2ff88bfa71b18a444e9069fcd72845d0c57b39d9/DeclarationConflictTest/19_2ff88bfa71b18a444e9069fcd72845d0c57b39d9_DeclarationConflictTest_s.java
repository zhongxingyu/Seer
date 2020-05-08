 package zinara.tests;
 
 import zinara.Main;
 import zinara.exceptions.IdentifierAlreadyDeclaredException;
 import zinara.exceptions.IdentifierNotDeclaredException;
 
 import junit.framework.*;
 
 public class DeclarationConflictTest extends TestCase {
     private String STATIC_DC_DIR = "../test_files/declaration_conflicts/";
 
     public DeclarationConflictTest(String name) { super(name); }
 
     public void testDeclarationConflict1() {
 	try {
 	    Main.testStaticFail(STATIC_DC_DIR + "dc1.zn");
 	    fail("Should raise IdentifierNotDeclaredException");
 	}
 	catch (IdentifierNotDeclaredException success) {}
 	catch (Exception e) { // any other exception
 	    e.printStackTrace();
 	    fail("Should raise IdentifierNotDeclaredException");
 	}
     }
 
     public void testDeclarationConflict2() {
 	try {
 	    Main.testStaticFail(STATIC_DC_DIR + "dc2.zn");
 	    fail("Should raise IdentifierNotDeclaredException");
 	}
 	catch (IdentifierNotDeclaredException success) {}
 	catch (Exception e) { // any other exception
 	    e.printStackTrace();
 	    fail("Should raise IdentifierNotDeclaredException");
 	}
     }
 
     public void testDeclarationConflict3() {
 	try {
 	    Main.testStaticFail(STATIC_DC_DIR + "dc3.zn");
 	    fail("Should raise IdentifierNotDeclaredException");
 	}
 	catch (IdentifierNotDeclaredException success) {}
 	catch (Exception e) { // any other exception
 	    e.printStackTrace();
 	    fail("Should raise IdentifierNotDeclaredException");
 	}
     }
 
     public void testDeclarationConflict4() {
 	try {
 	    Main.testStaticFail(STATIC_DC_DIR + "dc4.zn");
 	    fail("Should raise IdentifierNotDeclaredException");
 	}
 	catch (IdentifierNotDeclaredException success) {}
 	catch (Exception e) { // any other exception
 	    e.printStackTrace();
 	    fail("Should raise IdentifierNotDeclaredException");
 	}
     }
 
     public void testDeclarationConflict5() {
 	try {
 	    Main.testStaticFail(STATIC_DC_DIR + "dc5.zn");
 	    fail("Should raise IdentifierNotDeclaredException");
 	}
 	catch (IdentifierNotDeclaredException success) {}
 	catch (Exception e) { // any other exception
 	    e.printStackTrace();
 	    fail("Should raise IdentifierNotDeclaredException");
 	}
     }
 
     public void testDeclarationConflict6() {
 	try {
 	    Main.testStaticFail(STATIC_DC_DIR + "dc6.zn");
 	    fail("Should raise IdentifierNotDeclaredException");
 	}
 	catch (IdentifierNotDeclaredException success) {}
 	catch (Exception e) { // any other exception
 	    e.printStackTrace();
 	    fail("Should raise IdentifierNotDeclaredException");
 	}
     }
 
     public void testDeclarationConflict7() {
 	try {
 	    Main.testStaticFail(STATIC_DC_DIR + "dc7.zn");
 	    fail("Should raise IdentifierNotDeclaredException");
 	}
 	catch (IdentifierNotDeclaredException success) {}
 	catch (Exception e) { // any other exception
 	    e.printStackTrace();
 	    fail("Should raise IdentifierNotDeclaredException");
 	}
     }
 
     public void testDeclarationConflict8() {
 	try {
 	    Main.testStaticFail(STATIC_DC_DIR + "dc8.zn");
 	    fail("Should raise IdentifierNotDeclaredException");
 	}
 	catch (IdentifierNotDeclaredException success) {}
 	catch (Exception e) { // any other exception
 	    e.printStackTrace();
 	    fail("Should raise IdentifierNotDeclaredException");
 	}
     }
 
     public void testDeclarationConflict9() {
 	try {
 	    Main.testStaticFail(STATIC_DC_DIR + "dc9.zn");
 	    fail("Should raise IdentifierAlreadyDeclaredException");
 	}
 	catch (IdentifierAlreadyDeclaredException success) {}
 	catch (Exception e) { // any other exception
 	    e.printStackTrace();
 	    fail("Should raise IdentifierAlreadyDeclaredException");
 	}
     }
 
     public void testDeclarationConflict10() {
 	try {
 	    Main.testStaticFail(STATIC_DC_DIR + "dc10.zn");
 	    fail("Should raise IdentifierAlreadyDeclaredException");
 	}
 	catch (IdentifierAlreadyDeclaredException success) {}
 	catch (Exception e) { // any other exception
 	    e.printStackTrace();
 	    fail("Should raise IdentifierAlreadyDeclaredException");
 	}
     }
 
     public void testDeclarationConflict11() {
 	try {
 	    Main.testStaticFail(STATIC_DC_DIR + "dc11.zn");
 	    fail("Should raise IdentifierAlreadyDeclaredException");
 	}
 	catch (IdentifierAlreadyDeclaredException success) {}
 	catch (Exception e) { // any other exception
 	    e.printStackTrace();
 	    fail("Should raise IdentifierAlreadyDeclaredException");
 	}
     }
 
     public void testDeclarationConflict12() {
 	try {
 	    Main.testStaticFail(STATIC_DC_DIR + "dc12.zn");
 	    fail("Should raise IdentifierAlreadyDeclaredException");
 	}
 	catch (IdentifierAlreadyDeclaredException success) {}
 	catch (Exception e) { // any other exception
 	    e.printStackTrace();
 	    fail("Should raise IdentifierAlreadyDeclaredException");
 	}
     }
 
     public void testDeclarationConflict13() {
 	try {
 	    Main.testStaticFail(STATIC_DC_DIR + "dc13.zn");
 	    fail("Should raise IdentifierAlreadyDeclaredException");
 	}
 	catch (IdentifierAlreadyDeclaredException success) {}
 	catch (Exception e) { // any other exception
 	    e.printStackTrace();
 	    fail("Should raise IdentifierAlreadyDeclaredException");
 	}
     }
 
     public void testDeclarationConflict14() {
 	try {
 	    Main.testStaticFail(STATIC_DC_DIR + "dc14.zn");
 	    fail("Should raise IdentifierNotDeclaredException");
 	}
 	catch (IdentifierNotDeclaredException success) {}
 	catch (Exception e) { // any other exception
 	    e.printStackTrace();
 	    fail("Should raise IdentifierNotDeclaredException");
 	}
     }
 }
