 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package vue.terminal.multiview;
 
 import controller.terminal.interfacesGUI.TerminalMainVue;
 import controller.terminal.interfacesGUI.TerminalPay;
 import controller.terminal.interfacesGUI.TerminalRent;
 import controller.terminal.interfacesGUI.TerminalRentSummary;
 import controller.terminal.interfacesGUI.TerminalReturn;
 import controller.terminal.interfacesGUI.TerminalReturnSummary;
 import controller.terminal.interfacesGUI.TerminalWelcome;
 import java.util.Collection;
 import java.util.HashSet;
import javax.swing.JOptionPane;
 
 /**
  *
  * @author Valentin SEITZ
  */
 public class TerminalMultiMainVue extends HashSet<TerminalMainVue> implements TerminalMainVue {
 	
 	public TerminalMultiMainVue() {
 	}
 
 	public TerminalMultiMainVue(Collection<? extends TerminalMainVue> clctn) {
 		super(clctn);
 	}
 
 	public TerminalMultiMainVue(int i, float f) {
 		super(i, f);
 	}
 
 	public TerminalMultiMainVue(int i) {
 		super(i);
 	}
 
 	@Override
 	public TerminalWelcome getTerminalWelcome() {
 		TerminalMultiWelcome multiWelcome = new TerminalMultiWelcome();
 		for (TerminalMainVue mainVueInstance : this) {
 			multiWelcome.add(mainVueInstance.getTerminalWelcome());
 		}
 		return multiWelcome;
 	}
 
 	@Override
 	public TerminalRent getTerminalRent() {
 		TerminalMultiRent multiRent = new TerminalMultiRent();
 		for (TerminalMainVue mainVueInstance : this) {
 			multiRent.add(mainVueInstance.getTerminalRent());
 		}
 		return multiRent;
 	}
 
 	@Override
 	public TerminalRentSummary getTerminalRentSummary() {
 		TerminalMultiRentSummary multiRentSummary = new TerminalMultiRentSummary();
 		for (TerminalMainVue mainVueInstance : this) {
 			multiRentSummary.add(mainVueInstance.getTerminalRentSummary());
 		}
 		return multiRentSummary;
 	}
 
 	@Override
 	public TerminalReturn getTerminalReturn() {
 		TerminalMultiReturn multiReturn = new TerminalMultiReturn();
 		for (TerminalMainVue mainVueInstance : this) {
 			multiReturn.add(mainVueInstance.getTerminalReturn());
 		}
 		return multiReturn;
 	}
 
 	@Override
 	public TerminalReturnSummary getTerminalReturnSummary() {
 		TerminalMultiReturnSummary multiReturnSummary = new TerminalMultiReturnSummary();
 		for (TerminalMainVue mainVueInstance : this) {
 			multiReturnSummary.add(mainVueInstance.getTerminalReturnSummary());
 		}
 		return multiReturnSummary;
 	}
 
 	@Override
 	public TerminalPay getTerminalPay() {
 		TerminalMultiPay multiPay = new TerminalMultiPay();
 		for (TerminalMainVue mainVueInstance : this) {
 			multiPay.add(mainVueInstance.getTerminalPay());
 		}
 		return multiPay;
 	}
 
 	@Override
 	public void displayTerminalWelcome() {
 		for (TerminalMainVue mainVueInstance : this) {
 			mainVueInstance.displayTerminalWelcome();
 		}
 	}
 
 	@Override
 	public void displayTerminalRent() {
 		for (TerminalMainVue mainVueInstance : this) {
 			mainVueInstance.displayTerminalRent();
 		}
 	}
 
 	@Override
 	public void displayTerminalRentSummary() {
 		for (TerminalMainVue mainVueInstance : this) {
 			mainVueInstance.displayTerminalRentSummary();
 		}
 	}
 
 	@Override
 	public void displayTerminalReturn() {
 		for (TerminalMainVue mainVueInstance : this) {
 			mainVueInstance.displayTerminalReturn();
 		}
 	}
 
 	@Override
 	public void displayTerminalReturnSummary() {
 		for (TerminalMainVue mainVueInstance : this) {
 			mainVueInstance.displayTerminalReturnSummary();
 		}
 	}
 
 	@Override
 	public void displayTerminalPay() {
 		for (TerminalMainVue mainVueInstance : this) {
 			mainVueInstance.displayTerminalPay();
 		}
 	}
 
 	@Override
 	public void showInformation(String msg) {
		JOptionPane.showMessageDialog(null, msg, "Information", JOptionPane.INFORMATION_MESSAGE);
 	}
 
 	@Override
	public void showError(String msg) {
		JOptionPane.showMessageDialog(null, msg, "Erreur", JOptionPane.ERROR_MESSAGE);
 	}
 
 	@Override
	public void showWarning(String msg) {
		JOptionPane.showMessageDialog(null, msg, "Avertissement", JOptionPane.WARNING_MESSAGE);
 	}
 
 	@Override
 	public void setVisible(boolean visible) {
 		for (TerminalMainVue mainVueInstance : this) {
 			mainVueInstance.setVisible(visible);
 		}
 	}
 }
