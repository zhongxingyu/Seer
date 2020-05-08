 package org.bh.plugin.resultAnalysis;
 
 import info.clearthought.layout.TableLayout;
 import info.clearthought.layout.TableLayoutConstants;
 
 import java.awt.event.ActionListener;
 import java.util.Map;
 
 import javax.swing.JComboBox;
 import javax.swing.JPanel;
 
 import net.sourceforge.jeuclid.swing.JMathComponent;
 
 import org.bh.data.types.Calculable;
 import org.bh.gui.swing.BHDescriptionLabel;
 import org.bh.platform.formula.IFormula;
 /**
  * responsible for the visualisation of the formulas depending on selected dcf method
  * @version 1.0
  * @author Marco Hammel
  */
 public class BHFormulaPanel extends JPanel {
 
     JComboBox box;
     IFormula f;
     JMathComponent formula;
     JMathComponent value;
 
     public enum Keys {
 	PERIOD, FORMULA, VALUE;
 
 	@Override
 	public String toString() {
 	    return getClass().getName() + "." + super.toString();
 	}
     }
 
     public BHFormulaPanel(ActionListener al) {
	double border = 20;
	double size[][] = { { border, TableLayoutConstants.PREFERRED, border, 300, TableLayoutConstants.PREFERRED, border},// columns
 		{ border, TableLayoutConstants.PREFERRED, border, // rows
 			TableLayoutConstants.PREFERRED, border, TableLayoutConstants.PREFERRED, border, } };
 	setLayout(new TableLayout(size));
 
 	add(new BHDescriptionLabel(Keys.PERIOD), "1,1");
 
 	box = new JComboBox();
 	box.addActionListener(al);
 	add(box, "3,1");
 
 	add(new BHDescriptionLabel(Keys.FORMULA), "1,3");
 	add(new BHDescriptionLabel(Keys.VALUE), "1,5");
 
     }
 
     void setFormula(IFormula f) {
 	this.f = f;
 	if (formula != null) {
 	    remove(formula);
 	}
 	formula = f.getJMathComponent();
	add(formula, "3,3,4,3,left,center");
     }
 
     void setValues(Map<String, Calculable> values) {
 	if (value != null) {
 	    remove(value);
 	}
 	value = f.getJMathComponentForInputValues(values);
	add(value, "3,5,4,5,left,center");
     }
 
     void addEntry(String s) {
 	box.addItem(s);
     }
 
     void setSelectedIndex(int index) {
 	box.setSelectedIndex(index);
     }
 
 }
