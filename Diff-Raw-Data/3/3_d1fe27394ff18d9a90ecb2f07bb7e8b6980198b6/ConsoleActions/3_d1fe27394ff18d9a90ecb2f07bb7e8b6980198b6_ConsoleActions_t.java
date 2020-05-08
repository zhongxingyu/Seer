 package gui;
 
 import java.awt.event.ActionEvent;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 
 public class ConsoleActions {
 
	/** changed visibility to default */
	Console c;
 	int history = 0;
 
 	public ConsoleActions(Console console) {
 		this.c = console;
 	}
 
 	public Action newLine = new AbstractAction() {
 
 		public void actionPerformed(ActionEvent e) {
 			ConsoleActions.this.c.newLine();
 
 		}
 	};
 
 	public Action end = new AbstractAction() {
 
 		public void actionPerformed(ActionEvent e) {
 			ConsoleActions.this.c.print("Send signal to kill process");
 			ConsoleActions.this.newLine.actionPerformed(e);
 		}
 	};
 	
 	public Action clean = new AbstractAction() {
 
 		public void actionPerformed(ActionEvent e) {
 			ConsoleActions.this.c.clean();
 			ConsoleActions.this.newLine.actionPerformed(e);
 		}
 	};
 }
