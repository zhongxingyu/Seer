 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 import java.io.*;
 
 public class Ajedrez extends JFrame{
 	private static final long serialVersionUID = 5;
 	
   private int turn;
   private int rbx, rby, rnx, rny; //Tracking de la posicion del Rey
   private boolean activo, enroque;  // Pieza seleccionada y si se esta ejecutando un enrroque
   
   private JPanel panel, info; // Panel = Tablero   Info = Opciones
   private JTextField turno; // Caja de texto que contiene el color de la pieza activa
   private JButton nuevo, guarda, carga, salir, otro;  // Botones Otro = Locura mia
   private Casilla casillas[][], tmp1, tmp2;
  
   /**
     casillas es la representacion interna del tablero, tmp1 y tmp2 son temporales para
     alojar las posiciones de las piezas mientras se realiza un movimiento
   */
     
     
     
 	public Ajedrez(){
     super("Ajedrez");
     
     casillas = new Casilla[8][8];
     makePanel();
     
    enroque = activo = false;
     turn = 1;
 	}
 	
 	/* makePanel()
 	  Coloca la piezas pertinentes en su sitio adecuado para el inico de una nueva partida
 	  Se debe llamar solo al inicio del programa, o al reiniciar una partida (nueva)
 	*/
 	
 	protected void makePanel(){
 		for (int i = 0; i<8; i++)
 		  for (int j = 0; j<8; j++)
 		      casillas[i][j] = new Casilla(i,j);
 		  
 		  for (int i = 0; i<8; i++){
 		    casillas[1][i].setPieza(new Peon(1,i,true));
 		    casillas[6][i].setPieza(new Peon(6,i,false));
 		  }
 		  
 		  casillas[0][0].setPieza(new Torre(0,0,true));
 		  casillas[0][7].setPieza(new Torre(0,7,true));
 		  casillas[7][0].setPieza(new Torre(7,0,false));
 		  casillas[7][7].setPieza(new Torre(7,7,false));
 		  
 		  casillas[0][1].setPieza(new Caballo(0,1,true));
 		  casillas[0][6].setPieza(new Caballo(0,6,true));
 		  casillas[7][1].setPieza(new Caballo(7,1,false));
 		  casillas[7][6].setPieza(new Caballo(7,6,false));
 		  
 		  casillas[0][2].setPieza(new Alfil(0,2,true));
 		  casillas[0][5].setPieza(new Alfil(0,5,true));
 		  casillas[7][2].setPieza(new Alfil(7,2,false));
 		  casillas[7][5].setPieza(new Alfil(7,5,false));
 		  
 		  
 		  setRey(0,3, true);
 		  casillas[0][3].setPieza(new Rey(0,3,true));
 		  casillas[0][4].setPieza(new Reina(0,4,true));
 		  
 		  setRey(7,3, false);
 		  casillas[7][3].setPieza(new Rey(7,3,false));
 		  casillas[7][4].setPieza(new Reina(7,4,false));
 	}
 	
 	
 	/* addListeners()
 	  Agrega los listeners necesarios para la correcta ejecucion del programa, se modulariza
 	  para soportar posteriormente la opcion de cargar una partida desde archivo...
 	*/
 	
 	protected void addListeners(){
 		nuevo.addMouseListener(new MouseAdapter(){
 		    @Override
 		    public void mouseClicked(MouseEvent e){
 		      dispose();
 		      new Ajedrez().showGUI();
 		    }
 		  });
 		  
 		carga.addMouseListener(new MouseAdapter(){
   		public void mouseClicked(MouseEvent e){
   			Ajedrez nvo = new Ajedrez();
   			if (nvo.cargaPartida()){
   				dispose();
   				nvo.showGUI();
   			}
   			
   			else
   				nvo = null;
   			
   		}
   	});
   	
   	guarda.addMouseListener(new MouseAdapter(){
   		public void mouseClicked(MouseEvent e){
   			guardaPartida();
   		}
   	});
   	
 		  
 		  salir.addMouseListener(new MouseAdapter(){
 		    @Override
 		    public void mouseClicked(MouseEvent e){
 		      int res = JOptionPane.showConfirmDialog(null, "Desea guardar la partida actual???");
 					
 					if (res == JOptionPane.OK_OPTION)
 						guardaPartida();
 					
 					else if (res == JOptionPane.NO_OPTION)
 						JOptionPane.showMessageDialog(null, "Chido pues!!!\nEmpiezele de cero a la siguiente...");
 						
 					if (res != JOptionPane.CANCEL_OPTION)
 						System.exit(0);
 		    }
 		  });
 		  
 		  for (int i = 0, j; i<8; i++)
 		    for (j = 0; j<8; j++){
 		    	casillas[i][j].addMouseListener(new MouseAdapter(){
 
 		        @Override
 		        public void mouseClicked(MouseEvent E){
 		          Pieza tmp = null;
 		          int x;
 		                  
 		          if (E.getButton() == MouseEvent.BUTTON3 && activo){
 		            tmp1.setBackground(null);
 		            activo = false;
 		            return;
 		          }
 		          
 		          if (!E.getComponent().isEnabled())
 		            return;
 		          
 		          if (((Casilla)E.getComponent()).getPieza() == null && 
 		                  !activo)
 		            return;
 		          
 		          
 		          if (activo){                     
 		            tmp2 = (Casilla)(E.getComponent());
 		            
 		            activo = false;
 		            tmp1.setBackground(null);
 		            
 		            if (tmp1.getPieza() instanceof Rey)
 		                enroque = enroq();
 		            
 		            if (enroque){
 		              //TODO > Mover la torre correspondiente
 		              //     > Checar jaques y devolver las piezas
 		              
 		              tmp1.getPieza().move();
 		              
 		              tmp2.setPieza(new Rey(tmp2.getx(), tmp2.gety(),
 		                      tmp1.getPieza().isBlanca()));
 		              
 		              tmp2.getPieza().setMov(tmp1.getPieza().getMov());
 		              
 		              
 		              setRey(tmp2.getx(), tmp2.gety(),
 		                      tmp2.getPieza().isBlanca());
 		              
 		              tmp1.setPieza(null);
 		              
 		              if (tmp2.gety() > tmp1.gety()){
 		                
 		                if(tmp2.getPieza().isBlanca()){                                   
 		                	casillas[0][4].setPieza(new Torre(0,4,true));
 		                  casillas[0][7].setPieza(null);                                        
 		                }
 		                
 		                else{                                    
 		                	casillas[7][4].setPieza(new Torre(7,4,false));
 		                  casillas[7][7].setPieza(null);
 		                }                                    
 		              }
 		              
 		              else{
 		                
 		                if(tmp2.getPieza().isBlanca()){                                   
 		                	casillas[0][2].setPieza(new Torre(0,2,true));
 		                  casillas[0][0].setPieza(null);                                        
 		                }
 		                
 		                else{                                    
 		                	casillas[7][2].setPieza(new Torre(7,2,false));
 		                  casillas[7][0].setPieza(null);
 		                }
 		                
 		              }
 		              
 		              cTurno();
 		              
 		              activo = false;
 		              enroque = false;
 		              
 		              return;
 		          } // Fin Enroque
 		            
 		          else if(tmp1.getPieza().move(tmp2.getx(),tmp2.gety(),casillas)){
 		              
 		              if (tmp2.getPieza() != null)
 		                tmp = tmp2.getPieza().getPieza();
 		              
 		              tmp2.setPieza(tmp1.getPieza().getPieza());
 		              
 		              tmp2.getPieza().setX(tmp2.getx());
 		              tmp2.getPieza().setY(tmp2.gety());
 		              
 		              if (tmp2.getPieza() instanceof Rey){
 		                setRey(tmp2.getx(), tmp2.gety(), tmp2.
 		                        getPieza().isBlanca());
 		              }
 		              
 		              tmp2.getPieza().move();
 		              
 		              tmp1.setPieza(null);
 		              
 		              x = jaque();
 		              
 		              if (x != 0 && x != turn){ //Esta en jaque, de regreso!                    
 		                tmp2.getPieza().deMove();
 		                
 		                tmp1.setPieza(tmp2.getPieza().getPieza());
 		                tmp1.getPieza().setMov(tmp2.getPieza().getMov());
 		                tmp1.getPieza().setX(tmp1.getx());
 		                tmp1.getPieza().setY(tmp1.gety());
 
 		                if (tmp1.getPieza() instanceof Rey){
 		                    setRey(tmp1.getx(), tmp1.gety(), tmp1.
 		                            getPieza().isBlanca());
 		                }
 
 		                if (tmp != null)
 		                    tmp2.setPieza(tmp.getPieza());
 		                        
 		                else
 		                    tmp2.setPieza(null);
 		                
 		            
 		                return;
 		              }
 		                                              
 		              
 		          }
 		            
 		          else{ // Pieza invalida
 		              //alerta(2);
 		              return;
 		          }
 		            
 		          cTurno();
 		              
 		        } //Fin Movimiento (if (activo))
 		          
 		          else if (E.getButton() != MouseEvent.BUTTON3){
 		            tmp1 = (Casilla)(E.getComponent());
 		            
 		            if ((turn == 2 && tmp1.getPieza().isBlanca()) ||
 		                    (turn == 1 && !tmp1.getPieza().isBlanca())){
 		              alerta(1);
 		              return;
 		            }
 		              
 		            tmp1.setBackground(Color.GREEN);
 		            activo = true;
 		          }
 		        }
 		      });
 		    }
 		  
 	}
 	
 	
 	protected void showGUI(){
     setLayout(new FlowLayout());
     
     info = new JPanel();
     turno = new JTextField(5);
     nuevo = new JButton("Nuevo");
     otro = new JButton();
     salir = new JButton("Salir");
     guarda = new JButton("Guardar");
     carga = new JButton("Cargar");
     
     turno.setEditable(false);
     
     if (turn == 1)
 	    turno.setText("Blancas");
 	   else 
 	   	turno.setText("Negras");
     
     info.setLayout(new GridLayout(6,1));
     
     
     //otro es para un icono extra, puede ser removido sin problemas
     otro.setIcon(new ImageIcon("lerolero.gif"));
     otro.setEnabled(false);
     otro.setDisabledIcon(new ImageIcon("lerolero.gif"));
     
     info.add(turno);
     info.add(nuevo);
     info.add(salir);
     info.add(carga);
     info.add(guarda);
     info.add(otro);
     
     panel = new JPanel();
     
     panel.setLayout(new GridLayout(8,8));
     
    addListeners();
    
     for (int i = 0; i<8; i++)
     	for (int j = 0; j<8; j++)
     		panel.add(casillas[i][j]);
 
     
     add(panel);
     add(info);
     
     setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     setResizable(false);
     pack();
     setVisible(true);
 	}   
 	
 	protected boolean cargaPartida(){
 		int res;
 		boolean status = true;
   	
   	JFileChooser archivo = new JFileChooser("Cargar partida guardada");
   	res = archivo.showOpenDialog(this);
   	
   	if (res == JFileChooser.APPROVE_OPTION){
   		File file = archivo.getSelectedFile();
   		
 			try{
 				ObjectInputStream entrada = new ObjectInputStream(new FileInputStream(file.getAbsolutePath()));
 				Object var;
 				
 				var = entrada.readObject();
 				
 				if (var instanceof Casilla[][])
 					casillas = (Casilla[][])var;
 				else{
 					JOptionPane.showMessageDialog(null, "Archivo no correspondiente al programa.\nIntenta de nuevo...\nCasillas");
 					status = false;
 				}
 				
 				var = entrada.readObject();	
 				if (var instanceof Integer)
 					turn = (Integer)var;
 				else{
 					JOptionPane.showMessageDialog(null, "Archivo no correspondiente al programa.\nIntenta de nuevo...\nturn");
 					status = false;
 				}
 				
 				entrada.close();
 			}
 			catch(Exception e){
 				JOptionPane.showMessageDialog(null, "No se pudo abrir el archivo");
 			}
 			
 			finally{
 				return res == JFileChooser.APPROVE_OPTION && status;
 			}
 			
   	}
   	
   	return false;
   }
 
 	
 	private boolean guardaPartida(){
   	int res;
   	boolean retval = true;
   	
   	JFileChooser archivo = new JFileChooser("Guardar partida actual");
   	res = archivo.showSaveDialog(this);
   	
   	if (res == JFileChooser.APPROVE_OPTION){
   		File file = archivo.getSelectedFile();
   		
 			try{
 				ObjectOutputStream salida = new ObjectOutputStream(new FileOutputStream(file.getAbsolutePath()));
 				salida.writeObject(casillas);
 				salida.writeObject(new Integer(turn));
 				salida.flush();
 				salida.close();
 			}
 			catch(Exception e){
 				JOptionPane.showMessageDialog(null, "No se pudo guardar el archivo");
 				e.printStackTrace();
 				retval = false;
 			}
 			
 			finally{
 				return res != JFileChooser.CANCEL_OPTION && retval;
 			}
 			
   	}
   	
   	return false;
   	
   }
     
   protected void alerta(int opc){
       String msg = "";
       
       switch (opc){
           case 1:
               msg = "No es tu pieza, selecciona otra";
               break;
               
           case 2:
               msg = "Movimiento invalido. \nVuelve a intentarlo";
               break;
               
           case 3:
               msg = "Casilla vacia, selecciona una pieza";
               break;
               
           case 4:
               msg = "Estas en jaque!!!\nMueve otra pieza";
               
       }
       
       JOptionPane.showMessageDialog(null, msg);
   }
   
   protected final void setRey(int x, int y, boolean c){
       
       if (c){
           rbx = x;
           rby = y;
       }
               
       else{
           rnx = x;
           rny = y;
       }
       
   }
   
   protected int jaque(){
       int st=0, i, j;
       Pieza temp;
       
       for (i = 0; i<8; i++){
           for (j = 0; j<8; j++){
               if (casillas[i][j].getPieza() == null)
                   continue;
               
               temp = casillas[i][j].getPieza();
               
               if (temp.isBlanca() && temp.move(rnx, rny, casillas))
                   st = 1;
 
               
               else if (!temp.isBlanca() && temp.move(rbx, rby, casillas))
                   st = 2;
 
           }
           
           if (st != turn && st != 0){
               alerta(4);
               break;
           }
           
       }
       
      return st; 
   }
     
   protected boolean enroq(){
       
     if (tmp2.getPieza() != null){
         return false;
     }
     
     else if (tmp1.getPieza().getMov() != 0){
         return false;
     }
     
     else if (tmp1.getx() - tmp2.getx() != 0){
         return false;
     }
     
     else if (Math.abs(tmp1.gety() - tmp2.gety()) != 2){
         return false;
     }
     
     for (int j = tmp1.gety(), i = tmp1.getx(), k = tmp2.gety();
             !(casillas[i][j].getPieza() instanceof Torre); ){
         if (casillas[i][j].getPieza() != null && j != tmp1.gety()){
             return false;
         }
         
         if (k>tmp1.gety())
             j++;
         else
             j--;
         
         if (j>7 || j<0){
             return false;
         }
     }
     
     return true;
   }
   
   protected void cTurno(){
       if (turn == 1){
           turn = 2;
           turno.setText("Negras");
       }
       
       else{
           turn = 1;
           turno.setText("Blancas");
       }
       
   }
   
   /*
   public void Revive(){
       final JFrame win = new JFrame("Revivir Pieza");
       final JComboBox sel = new JComboBox();
       JButton OK = new JButton("OK");
       boolean v = true;
 
       sel.addItem("Gatito");
       sel.addItem("Perrito");
 
       OK.addMouseListener(new MouseAdapter(){
 
           @Override
           public void mouseClicked(MouseEvent E){
               String p = "";
 
               p = (String) sel.getSelectedItem();
               System.out.println(p);
 
               if (p.equals("Gatito"))
                   tmp = new Reina(tmp2.getx(), tmp2.gety(), 
                           tmp2.getPieza().isBlanca());
               else
                   tmp = new Alfil(tmp2.getx(), tmp2.gety(), 
                           tmp2.getPieza().isBlanca());
 
               win.dispose();
           }
           
       });
 
       win.setBounds(400, 300, 250, 80);
       win.setLayout(new FlowLayout());
       win.add(sel);
       win.add(OK);
 
       win.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
       win.setVisible(true);
 
 
   }*/
   
   public static void main(String args[]){
       new Ajedrez().showGUI(); 
       
   }
     
 }
