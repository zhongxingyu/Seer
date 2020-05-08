 package dme.alcor;
 
import java.awt.*;
import java.awt.event.*;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
import javax.swing.*;
 
 public class Alcor extends JFrame implements ActionListener {
 	
 	/* Zona de Dados */
 	static JMenuBar menu_bar;
 	static JMenu menu;
 	static JMenuItem item_op1, item_op2;
 	
 	static JPanel norte, centro, sul;
 	static JLabel lb;
 	static JTable table;
 	static JScrollPane scrollpane;
 	static JComboBox<String> ch_mes, ch_ano;
 	
 	static JButton bt_mostrar, bt_anterior, bt_proximo, bt_sair, bt_reset, bt_crescente;
 	
 	static String meses[] = new String[12]; 
 	static String anos[] = new String[100];
 	static String [] tituloData = { "Data", "Iluminao", "Fase" };
 	static String [][] data_tabela;
 	static JTable tabela;
 	JScrollPane scrollPane;
 	
 	static DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
 	static Date data = new Date();
     static String now = df.format(data);
 	
 	static int i=0;
 	static int k=0;
 	
 	static boolean crescente = true;
 	
 	String about = "<html>" +
 			"       <h3>Alcor</h3><hr>" +
 			"       <p>Calcula a percentagem de iluminao da Lua<br>" +
 			"       para uma determinada data.</p>" +
 			"       <p>O algoritmo de Conway  utilizado<br>" +
 			"       para o clculo, portanto a preciso <br>" +
 			"       baixa.</p>" +
 			"       <hr>" +
 			"       </<html>";
 	
 	private static final long serialVersionUID = -1148257910792573725L;
 
 	public Alcor() {
 		
 		super("Alcor");
 		
 		/* // System Look and Feel
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} catch (ClassNotFoundException e1) {} 
 		  catch (InstantiationException e1) {} 
 		  catch (IllegalAccessException e1) {} 
 		  catch (UnsupportedLookAndFeelException e1) {};
          */
 		
 		menu_bar = new JMenuBar();
 		menu = new JMenu("Opes");
 		item_op1 = new JMenuItem("Sobre");
 		item_op1.addActionListener(this);
 		menu.add(item_op1);
 		
 		menu.addSeparator();
 		
 		item_op2 = new JMenuItem("Sair");
 		item_op2.addActionListener(this);
 		menu.add(item_op2);
 		
 		menu_bar.add(menu);
 		setJMenuBar(menu_bar);
 		
 		norte = new JPanel( new GridLayout(2,3) );
 		
 		lb = new JLabel("[ Lua ]");
 		norte.add(lb);
 		
 		lb = new JLabel("");
 		norte.add(lb);
 		
 		lb = new JLabel(now);
 		norte.add(lb);
 		
 		lb = new JLabel(" Data: ");
 		norte.add(lb);
 		
 		
 		meses[0] = "Janeiro";
 		meses[1] = "Fevereiro";
 		meses[2] = "Maro";
 		meses[3] = "Abril";
 		meses[4] = "Maio";
 		meses[5] = "Junho";
 		meses[6] = "Julho";
 		meses[7] = "Agosto";
 		meses[8] = "Setembro";
 		meses[9] = "Outubro";
 		meses[10] = "Novembro";
 		meses[11] = "Dezembro";
 		ch_mes = new JComboBox<String>(meses);
 		ch_mes.setBackground(new Color(0xfdfdf8 ));
 		norte.add(ch_mes);
 		
 		for(i=2000;i<=2099;i++){
 			anos[i-2000]=""+i;
 		}
 		ch_ano = new JComboBox<String>(anos);
 		ch_ano.setBackground(new Color(0xfdfdf8 ));
 		norte.add(ch_ano);
 		
 		
 		
 		getContentPane().add(norte, BorderLayout.NORTH);
 		
 		centro = new JPanel( new GridLayout(1,1) );
 		
 		data_tabela = new String[31][3];
 		tabela = new JTable(data_tabela, tituloData);
 		scrollPane = new JScrollPane(tabela);
 		tabela.setShowGrid(false);
 		tabela.setBackground(new Color(0xfdfdf8 ));
 		
 		tabela.setFont( new Font("Arial", Font.BOLD, 11) );
 		centro.add(scrollPane);
 		getContentPane().add(centro, BorderLayout.CENTER);
 		
 		sul = new JPanel( new GridLayout(2,4) );
 			
 		bt_mostrar = new JButton("Mostrar Ms");
 		bt_mostrar.addActionListener(this);
 		bt_mostrar.setBackground(new Color(0xfdfdf8 ));
 		sul.add(bt_mostrar);
 			
 		lb = new JLabel("");
 		sul.add(lb);
 		
 		bt_anterior = new JButton("Ms Anterior");
 		bt_anterior.addActionListener(this);
 		bt_anterior.setBackground(new Color(0xfdfdf8 ));
 		sul.add(bt_anterior);
 		
 		bt_proximo = new JButton("Prximo Ms");
 		bt_proximo.addActionListener(this);
 		bt_proximo.setBackground(new Color(0xfdfdf8 ));
 		sul.add(bt_proximo);
 		
 		bt_crescente = new JButton("Lua Crescente");
 		bt_crescente.addActionListener(this);
 		bt_crescente.setBackground(new Color(0xfdfdf8 ));
 		sul.add(bt_crescente);
 		
 			
 		lb = new JLabel("");
 		sul.add(lb);
 
 		
 		bt_reset = new JButton("Reset");
 		bt_reset.addActionListener(this);
 		bt_reset.setBackground(new Color(0xfdfdf8 ));
 		sul.add(bt_reset);
 		
 		bt_sair = new JButton("Sair");
 		bt_sair.addActionListener(this);
 		bt_sair.setBackground(new Color(0xfdfdf8 ));
 		sul.add(bt_sair);
 		
 		getContentPane().add(sul, BorderLayout.SOUTH);
 		
 		reset();
 		showOut( Integer.parseInt( anos[ch_ano.getSelectedIndex()] ), (ch_mes.getSelectedIndex()) );
 		
 		pack();
 		setLocationRelativeTo(null);
 		setVisible(true);
 		
 	}
 	
 	public void actionPerformed(ActionEvent e){
 		
 		if( e.getSource() == item_op1 ){
 		
 			JOptionPane.showMessageDialog(this, about, "Sobre este programa", JOptionPane.INFORMATION_MESSAGE);
 		}
 		
 		if( e.getSource() == bt_sair || e.getSource() == item_op2 ){
 			System.exit(0);
 		}
 		if ( e.getSource() == bt_mostrar ){
 			
 			showOut( Integer.parseInt( anos[ch_ano.getSelectedIndex()] ), (ch_mes.getSelectedIndex()) );
 			
 		}
 		
 		if( e.getSource() == bt_proximo ){
 			
 				if( ch_mes.getSelectedIndex() == 11 ){
 					ch_mes.setSelectedIndex( 0 );
 					if( ch_ano.getSelectedIndex() != 99)
 						ch_ano.setSelectedIndex( ch_ano.getSelectedIndex()+1 );
 					else
 						ch_ano.setSelectedIndex( 0 );
 				}
 				else
 					ch_mes.setSelectedIndex( ch_mes.getSelectedIndex()+1 );
 				
 				showOut( Integer.parseInt( anos[ch_ano.getSelectedIndex()] ), (ch_mes.getSelectedIndex()) );
 			}
 			
 		if( e.getSource() == bt_anterior ){
 			
 			if( ch_mes.getSelectedIndex() == 0 ){
 				ch_mes.setSelectedIndex( 11 );
 				if( ch_ano.getSelectedIndex() != 0)
 					ch_ano.setSelectedIndex( ch_ano.getSelectedIndex()-1 );
 				else
 					ch_ano.setSelectedIndex( 99 );
 			}
 			else
 				ch_mes.setSelectedIndex( ch_mes.getSelectedIndex()-1 );
 			
 			showOut( Integer.parseInt( anos[ch_ano.getSelectedIndex()] ), (ch_mes.getSelectedIndex()) );
 		}
 		
 	   if( e.getSource() == bt_reset ){
 		   
 		   reset();
 		   
 	   }
 	   
 	   if( e.getSource() == bt_crescente){
 		   
 		   luaCrescente( Integer.parseInt( anos[ch_ano.getSelectedIndex()] ), (ch_mes.getSelectedIndex()) );
 		   //System.out.println(""+Integer.parseInt( anos[ch_ano.getSelectedIndex()] )+" "+(ch_mes.getSelectedIndex()));
 	   }
 		
 		
 		
 		
 	}
 	
 	static public void showOut(int year, int month){
 		
 		int lim=0;
 		switch (month) {
 			case 0: lim=31; break;
 			case 1: lim=28; break;
 			case 2: lim=31; break;
 			case 3: lim=30; break;
 			case 4: lim=31; break;
 			case 5: lim=30; break;
 			case 6: lim=31; break;
 			case 7: lim=31; break;
 			case 8: lim=30; break;
 			case 9: lim=31; break;
 			case 10: lim=30; break;
 			case 11: lim=31; break;
 		}
 
 		k = 0;             
 		for(i=1; i<=lim; i++){
 			tabela.setValueAt(""+i+" de "+meses[month]+" de "+year, k, 0);
 			tabela.setValueAt(""+ConwayPercentage( Conway(year, month+1, i) ), k, 1);
 			tabela.setValueAt(""+PhaseName(ConwayPercentage( Conway(year, month+1, i) ), isCrescentConway(year, month+1, i)), k, 2);
 			k++;
 		}
 		
 	}
 	
 	  static public double Conway(int year, int month, int day){	
 			double r = year % 100;
 			double res = 0;
 			
 			r %= 19;
 			if (r>9){ r -= 19;}
 			r = ((r * 11) % 30) + month + day;
 			if (month<3){r += 2;}
 			r -= ((year<2000) ? 4 : 8.3);
 			r = Math.floor(r+0.5)%30;
 			res = (r < 0) ? r+30 : r;		
 			return res;
 		}
 	  
 	  static public boolean isCrescentConway(int year, int month, int day){
 		  
 		  if( ConwayPercentage(Conway(year, month, day)) >= ConwayPercentage(Conway(year, month, day-1 )) ){
 			  //System.out.println(true);
 			  return true;  
 		  }
 		  else{
 			  //System.out.println(false);
 			  return false;
 		  }
 		  
 	  }
 		   
 	  static public double ConwayPercentage(double age){
 		       double age_per=0.0;
 		       double res=0.0;
 		       
 		       /* Converso para percentagem  */
 		       age_per = ( (age) / 14 )* 100;
 		       if(age>=15){
 		           age = 29 - age;
 		           age_per = ( (age) / 14 )* 100;
 		       } 
 
 		       /* Arredondamento do resultado */
 		       res = age_per * 100;
 		       res = Math.round(res);
 		       res = res / 100;
 		       
 		       return res;
 		       
 		   }
 		  
 	  static public String PhaseName(double percentage, boolean crescent){
 	       
 	       String name="";       
 	       if (percentage>=0 && percentage<3 && crescent) {name = "Lua Nova";}
 	       if (percentage>=3 && percentage<35 && crescent) {name = "Primeira Falcada";}
 	       if (percentage>=35 && percentage<66 && crescent) {name = "Quarto Crescente";}
 	       if (percentage>=66 && percentage<97 && crescent) {name = "Primeira giba";}
 	       if (percentage>=97 && percentage<=100 && crescent) {name = "Lua Cheia";}
 	       if (percentage<100 && percentage>96 && !crescent) {name = "Lua Cheia";}
 	       if (percentage<=96 && percentage>66 && !crescent) {name = "Segunda Giba";}
 	       if ( percentage<=65 && percentage>35 && !crescent ) {name = "Quarto Minguante";}
 	       if (percentage<=35 && percentage>3 && !crescent ) {name = "Segunda Falcada";}
 	       if (percentage<=3 && percentage>=0 && !crescent) {name = "Lua Nova";}
 	       
 	       return name;
 	   }
 	  
 	  static public void reset(){
 		  
 		  String mes = now.substring(3,5);
 		   String ano = now.substring(6);
 		   
 		   ch_mes.setSelectedIndex( Integer.parseInt(mes)-1 );
 		   ch_ano.setSelectedIndex( Integer.parseInt(ano)-2000 );
 		   showOut( (Integer.parseInt(ano)-2000), (Integer.parseInt(mes)-1) );
 		  
 	  }
 	  
     static public void luaCrescente(int year, int month ){
     	 
     	int lim=0;
 		switch (month) {
 			case 0: lim=31; break;
 			case 1: lim=28; break;
 			case 2: lim=31; break;
 			case 3: lim=30; break;
 			case 4: lim=31; break;
 			case 5: lim=30; break;
 			case 6: lim=31; break;
 			case 7: lim=31; break;
 			case 8: lim=30; break;
 			case 9: lim=31; break;
 			case 10: lim=30; break;
 			case 11: lim=31; break;
 		}
 		
 		for(i=0; i<31; i++) {
 
 				tabela.setValueAt("", i, 0);
 				tabela.setValueAt("", i, 1);
 				tabela.setValueAt("", i, 2);
 			}		
 
 		k = 0;         
 		for(i=1; i<=lim; i++)
 			if( ConwayPercentage(Conway(year, month, i)) >= 10.0 && ConwayPercentage(Conway(year, month, i)) < 60.0 && isCrescentConway(year, month+1, i)){
 
 				System.out.println(""+(i)+"  "+ isCrescentConway(year, month+1, i+1) );
 				
 				tabela.setValueAt(""+i+" de "+meses[month]+" de "+year, k, 0);
 				tabela.setValueAt(""+ConwayPercentage( Conway(year, month+1, i) )+" %", k, 1);
 				tabela.setValueAt(""+PhaseName(ConwayPercentage( Conway(year, month+1, i) ), isCrescentConway(year, month+1, i)), k, 2);
 				k++;
 			}
 		
 			
     	
     		
     	
     }
 	  
 	   
 	public static void main(String[] args) {
 		
 		Alcor app = new Alcor();
 		 
 		 app.addWindowListener(new WindowAdapter()           
 	   		{
 	   			public void windowClosing(WindowEvent e)
 	   			{
 	   				System.exit(0);
 	   			}
 		});
 		
 
 	}
 
 }
