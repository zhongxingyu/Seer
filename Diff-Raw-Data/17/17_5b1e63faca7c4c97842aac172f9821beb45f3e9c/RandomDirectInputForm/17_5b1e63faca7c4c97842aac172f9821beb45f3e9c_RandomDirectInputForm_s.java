 package org.bh.plugin.directinput;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import org.bh.gui.swing.BHDescriptionLabel;
 import org.bh.gui.swing.BHValueLabel;
 
 import com.jgoodies.forms.layout.CellConstraints;
 import com.jgoodies.forms.layout.FormLayout;
 
 /**
  * Visual class ViewPeriodData1.
  * 
  * 
  */
 public class RandomDirectInputForm extends JPanel {
     private BHDescriptionLabel lfcf;
     private BHValueLabel tffcf;
     private BHDescriptionLabel lliabilities;
     private BHValueLabel tfliabilities;
     private BHDescriptionLabel lcurrency1;
     private BHDescriptionLabel lcurrency2;
 
     /**
      * Constructor.
      */
     public RandomDirectInputForm() {
 	
	String rowDef = "4px,p,4px,p,4px,p,4px";
	String colDef = "4px,right:pref,4px,150px:grow,4px,pref,4px";
 
 	FormLayout layout = new FormLayout(colDef, rowDef);
 	this.setLayout(layout);
 
 	CellConstraints cons = new CellConstraints();
 
	this.add(this.getLfcf(), cons.xywh(2, 4, 1, 1));
	this.add(this.getTffcf(), cons.xywh(4, 4, 1, 1));
	this.add(this.getLcurrency1(), cons.xywh(6, 4, 1, 1));
	this.add(this.getLliabilities(), cons.xywh(2, 6, 1, 1));
	this.add(this.getTfliabilities(), cons.xywh(4, 6, 1, 1));
	this.add(this.getLcurrency2(), cons.xywh(6, 6, 1, 1));
     }
 
 
     public BHDescriptionLabel getLcurrency1() {
 	if (lcurrency1 == null) {
 	    lcurrency1 = new BHDescriptionLabel("currency");
 	}
 	return lcurrency1;
     }
     
     public BHDescriptionLabel getLcurrency2() {
 	if (lcurrency2 == null) {
 	    lcurrency2 = new BHDescriptionLabel("currency");
 	}
 	return lcurrency2;
     }
 
     public BHDescriptionLabel getLfcf() {
 	if (lfcf == null) {
 	    lfcf = new BHDescriptionLabel(DTODirectInput.Key.FCF);
 	}
 	return lfcf;
     }
 
     public BHValueLabel getTffcf() {
 	if (tffcf == null) {
 	    tffcf = new BHValueLabel(DTODirectInput.Key.FCF);
 	}
 	return tffcf;
     }
 
     public BHDescriptionLabel getLliabilities() {
 	if (lliabilities == null) {
 	    lliabilities = new BHDescriptionLabel(DTODirectInput.Key.LIABILITIES);
 	}
 	return lliabilities;
     }
 
     public BHValueLabel getTfliabilities() {
 	if (tfliabilities == null) {
 	    tfliabilities = new BHValueLabel(DTODirectInput.Key.LIABILITIES);
 	}
 	return tfliabilities;
     }
     
     public static void main(String args[]) {
 
     	JFrame test = new JFrame("Test for ViewPeriodData1");
     	test.setContentPane(new RandomDirectInputForm());
     	test.addWindowListener(new WindowAdapter() {
     	    @Override
     		public void windowClosing(WindowEvent e) {
     		System.exit(0);
     	    }
     	});
     	test.pack();
     	test.show();
         }
 }
