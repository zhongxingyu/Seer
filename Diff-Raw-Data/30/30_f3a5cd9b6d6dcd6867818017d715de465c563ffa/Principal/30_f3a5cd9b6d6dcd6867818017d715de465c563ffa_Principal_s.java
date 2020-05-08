import java.util.Scanner;
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 
 
 public class Principal extends JFrame implements ActionListener{
 
 	private Container layout;
 	private JTextField text = new JTextField("Numero de Regla",3);
 	private JRadioButton[] radio = new JRadioButton[2];
	private JButton button = new JButton("Generar Automata");
 
 	private void mostrar(){
 		layout = this.getContentPane();
 		layout.setLayout(new GridLayout(4,1));
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.setLocationRelativeTo(null);
 		this.add(text);
 		radio[0] = new JRadioButton("Random");
 		radio[1] = new JRadioButton("No Random");
 		//
 		radio[0].setActionCommand("0");
 		radio[1].setActionCommand("1");
 		ButtonGroup grupo = new ButtonGroup();
 		grupo.add(radio[0]);
 		grupo.add(radio[1]);
 		radio[0].addActionListener(this);
 		radio[1].addActionListener(this);
 		this.add(radio[0]);
 		this.add(radio[1]);
 		//
		//button.setActionCommand("PASAR");
		//button.addActionListener(this);
		this.add(button);
 		this.setSize(250,250);
 		this.setVisible(true);
 	}
 
     public void actionPerformed(ActionEvent e) {
      	try{
     		//if(e.getActionCommand().equals("PASAR")){
	    		int regla = Integer.parseInt(text.getText());
    			System.out.println("Text "+regla);
    			if(Integer.parseInt(e.getActionCommand()) == 0){
    				AutomataGui gui = new AutomataGui(regla,0);
					gui.mostrar();	
    			}
	    		if(Integer.parseInt(e.getActionCommand()) == 1){
    				AutomataGui gui = new AutomataGui(regla,1);
					gui.mostrar();	
    			}
    		//}	
     	}catch(NumberFormatException ne){
     		System.out.println("La regla no es un numero!");
     	}
     }
 
 	public static void main(String args[]){
 		Principal ventanita = new Principal();
 		ventanita.mostrar();
 	}
 
 }
