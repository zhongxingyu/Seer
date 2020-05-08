 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 public class BottonPanel extends JPanel implements ActionListener {
 	
 	private boolean  translateT , scalate ;
 	JButton rotate, translate, scalation,addpoint ;
	JTextField rAngle, translateX, translateY , scalateX, scalateY ;
 	ArrayList <int []> listaPuntos = new ArrayList();
 	Principal p;
 	
 	public BottonPanel(Principal p){
 		this.p=p;
 		this.setLayout(new GridLayout(1,11)) ;
 		rotate = new JButton("ROTATION");
 		rotate.setActionCommand("ROTATE") ;
 		rotate.addActionListener(this) ;
 		
 		translate = new JButton("TRANSLATION");
 		translate.setActionCommand("TRASLATE") ;
 		translate.addActionListener(this) ;
 		
 		scalation = new JButton("SCALATION");
 		scalation.setActionCommand("SCALATE") ;
 		scalation.addActionListener(this) ;
 		
 		addpoint = new JButton("ADD POINT") ;
 		addpoint.setActionCommand("ADD") ;
 		addpoint.addActionListener(this) ;
 		
		rAngle = new JTextField();
		translateX = new JTextField();
		translateY = new JTextField();
		scalateX = new JTextField();
		scalateY = new JTextField();
		
 		add(addpoint);
 		add(translate) ;
 		add(scalation) ;
 		add(rotate) ;
//		add(rAngle) ;
 	}
 	public int [] transformar(int a [] ,double x , double y ){
 		if(translateT){
 			a [0] = (int) (a [0] + Math.round(x)) ;
 			a [1] = (int) (a[1] + Math.round(y)) ;
 			translateT = false ;
 			return a ;
 		}
 		if (scalate){
 			a [0] =(int) Math.round(x * a [0])  ;
 			a [1] = (int) Math.round(y * a [1]) ;
 			scalate = false ;
 			return a ;
 		}
 		return a ;
 	}
 	
 	public int [] transformar (int a [] , double angle){
 		a [0] = (int) Math.round(a[0] *Math.cos(angle) - (a[1] * Math.sin(angle))) ;
 		a [1] = (int) Math.round(a[0] *Math.sin(angle) + (a[1] * Math.cos(angle))) ;
 		return a ;
 	}
 	
 	  public void actionPerformed(ActionEvent e) {
 		    String comando = e.getActionCommand() ;
 		    int arreglo [] ;
 		    if (comando.equals("ROTATE")){
 		    	
 		    	try {
 		    		double x = Integer.parseInt(JOptionPane.showInputDialog("Ingrese el angulo de rotacin")) ;
 		    		for (int i = 0; i < listaPuntos.size(); i++) {
 		    			arreglo = transformar( listaPuntos.get(i), x) ;
 		    			listaPuntos.set(i, arreglo) ;
 					}
 		    		arreglo = null;
 		    	}catch (Exception excep) {
 		    		JOptionPane.showMessageDialog(null, "Esto tiene error", "ERROR", JOptionPane.ERROR_MESSAGE) ;
 		    	}
 		      }else if (comando.equals("TRASLATE")) {
 		    	  try {
 		    		  
 		    		  double x = Double.parseDouble(JOptionPane.showInputDialog("Ingrese el incremento en X")) ;
 		    		  double y = Double.parseDouble(JOptionPane.showInputDialog("Ingrese el incremento en Y")) ;
 		    		  for (int i = 0; i < listaPuntos.size(); i++) {
 		    			  	translateT = true ;
 							arreglo = transformar(listaPuntos.get(i), x, y) ;
 							listaPuntos.set(i, arreglo) ;
 		    		  }
 		    		  arreglo = null ;
 		    	  }catch (Exception excep){
 			    		JOptionPane.showMessageDialog(null, "valores erroneos, verifique los datos", "ERROR", JOptionPane.ERROR_MESSAGE) ;
 		    	  }
 		        }else if (comando.equals("SCALATE")){
 		        	  try {
 			    		  
 			    		  double x = Double.parseDouble(JOptionPane.showInputDialog("Ingrese el incremento en X")) ;
 			    		  double y = Double.parseDouble(JOptionPane.showInputDialog("Ingrese el incremento en Y")) ;
 			    		  for (int i = 0; i < listaPuntos.size(); i++) {
 			    			  	scalate = true ;
 								arreglo = transformar(listaPuntos.get(i), x, y) ;
 								listaPuntos.set(i, arreglo) ;
 			    		  }
 			    		  arreglo = null ;
 			    	  }catch (Exception excep){
 				    		JOptionPane.showMessageDialog(null, "valores erroneos, verifique los datos", "ERROR", JOptionPane.ERROR_MESSAGE) ;
 			    	  }
 		          }else if (comando.equals("ADD")){
 		            int arr [] = new int [3] ;
 						try {
 							arr[0] = Integer.parseInt(JOptionPane.showInputDialog("Ingrese la coordenada en X"));
 							arr[1] =Integer.parseInt(JOptionPane.showInputDialog("Ingrese la coordenada en Y"));
 						}catch(Exception ecep) {
 				    		JOptionPane.showMessageDialog(null, "valores erroneos, verifique los datos", "ERROR", JOptionPane.ERROR_MESSAGE) ;
 						}
 						arr[2] =1 ;
 						this.listaPuntos.add(arr) ;
 		          }
 			p.paintEverything(listaPuntos) ;
 	  }
 }
