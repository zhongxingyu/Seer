 package catalogos;
 
 import java.awt.Container;
 import java.awt.GraphicsEnvironment;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JLayeredPane;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableModel;
 
 import objetos.Obj_fuente_sodas_rh;
 
 import SQL.Connexion;
 
 @SuppressWarnings("serial")
 public class Cat_Comprobar_Fuente_Sodas_RH extends JDialog{
 	
 	Container cont = getContentPane();
 	JLayeredPane panel = new JLayeredPane();
 	
 	DefaultTableModel modelRh = new DefaultTableModel(0,3){
 		public boolean isCellEditable(int fila, int columna){
 			if(columna < 0)
 				return true;
 			return false;
 		}
 	};
 	JTable tablaRh = new JTable(modelRh);
 	JScrollPane scrollRh = new JScrollPane(tablaRh);
 	
 	DefaultTableModel modelAx = new DefaultTableModel(0,3){
 		public boolean isCellEditable(int fila, int columna){
 			if(columna < 0)
 				return true;
 			return false;
 		}
 	};
 	JTable tablaAx = new JTable(modelAx);
 	JScrollPane scrollAx = new JScrollPane(tablaAx);
 	
 	DefaultTableModel modelTotalRH = new DefaultTableModel(0,3){
 		public boolean isCellEditable(int fila, int columna){
 			if(columna < 0)
 				return true;
 			return false;
 		}
 	};
 	JTable tablaTotalRH = new JTable(modelTotalRH);
 	JScrollPane scrollTotalRH = new JScrollPane(tablaTotalRH);
 	
 	DefaultTableModel modelTotalAX = new DefaultTableModel(0,3){
 		public boolean isCellEditable(int fila, int columna){
 			if(columna < 0)
 				return true;
 			return false;
 		}
 	};
 	JTable tablaTotalAX = new JTable(modelTotalAX);
 	JScrollPane scrollTotalAX = new JScrollPane(tablaTotalAX);
 	
 	JLabel lblTotalRH = new JLabel();
 	JLabel lblTotalAX = new JLabel();
 	
 	JButton btnAceptar = new JButton(new ImageIcon("imagen/Aplicar.png"));
 	JButton btnActualizar = new JButton(new ImageIcon("imagen/Actualizar.png"));
 	
 	public Cat_Comprobar_Fuente_Sodas_RH(){
 		this.setTitle("Comparacin Fuente de Sodas RRHH");
 		
 		Etiqueta();
 		
 		panel.add(new JLabel("Tabla Recursos Humanos")).setBounds(210,10,200,20);
 		panel.add(lblTotalRH).setBounds(470,10,200,20);
 		panel.add(scrollRh).setBounds(210,35,320,290);
 		panel.add(new JLabel("Tabla de Diferencia RRHH")).setBounds(210,335,200,20);
 		panel.add(scrollTotalRH).setBounds(210,360,320,290);
 
 		panel.add(new JLabel("Tabla Auxiliar")).setBounds(650,10,200,20);
 		panel.add(lblTotalAX).setBounds(910,10,200,20);
 		panel.add(scrollAx).setBounds(650,35,320,290);
 		panel.add(new JLabel("Tabla de Diferencia Auxiliar de Operaciones")).setBounds(650,335,250,20);
 		panel.add(scrollTotalAX).setBounds(650,360,320,290);
 		
 		panel.add(btnActualizar).setBounds(555,5,32,20);
 		panel.add(btnAceptar).setBounds(595,5,32,20);

 		
 		btnActualizar.addActionListener(opActualizar);
 		btnAceptar.addActionListener(opAceptar);
 		
 		String[][] TablaRH = getMatrizRH();
 		Object[] filaRH = new Object[tablaRh.getColumnCount()]; 
 		for(int i=0; i<TablaRH.length; i++){
 			modelRh.addRow(filaRH); 
 			for(int j=0; j<3; j++){
 				modelRh.setValueAt(TablaRH[i][j]+"", i,j);
 			}
 		}
 		
 		String[][] TablaAux = getMatrizAux();
 		Object[] filaAux = new Object[tablaAx.getColumnCount()]; 
 		for(int i=0; i<TablaAux.length; i++){
 			modelAx.addRow(filaAux); 
 			for(int j=0; j<3; j++){
 				modelAx.setValueAt(TablaAux[i][j]+"", i,j);
 			}
 		}
 		
 		String[][] TablaDifRH = getMatrizDifRH();
 		Object[] filaDifRH = new Object[tablaTotalRH.getColumnCount()]; 
 		for(int i=0; i<TablaDifRH.length; i++){
 			modelTotalRH.addRow(filaDifRH); 
 			for(int j=0; j<3; j++){
 				modelTotalRH.setValueAt(TablaDifRH[i][j]+"", i,j);
 			}
 		}
 		
 		String[][] TablaDifAX = getMatrizDifAX();
 		Object[] filaDifAX = new Object[tablaTotalAX.getColumnCount()]; 
 		for(int i=0; i<TablaDifAX.length; i++){
 			modelTotalAX.addRow(filaDifAX); 
 			for(int j=0; j<3; j++){
 				modelTotalAX.setValueAt(TablaDifAX[i][j]+"", i,j);
 			}
 		}
 		sumaRH();
 		
 		sumaAX();
 		
 		if(TablaDifAX.length == 0 && TablaDifRH.length == 0){
 			btnAceptar.setEnabled(true);
 		}else{
 			btnAceptar.setEnabled(false);
 		}
 		cont.add(panel);
 		agregar(tablaRh);
 		this.setResizable(true);
 		this.setLocationRelativeTo(null);
 		this.setBounds(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
 	}
 	
 	private void agregar(final JTable tbl) {
         tbl.addMouseListener(new java.awt.event.MouseAdapter() {
 	        public void mouseClicked(MouseEvent e) {
 	        	if(e.getClickCount() == 2){
 	        		String[][] TablaDifRH = getMatrizDifRH();
 	    			String[][] TablaDifAX = getMatrizDifAX();
 	    			if(TablaDifAX.length == 0 && TablaDifRH.length == 0){
 	    				JOptionPane.showMessageDialog(null, "La Lista est correcta! \n Haga click en la palomita para terminar","Aviso",JOptionPane.INFORMATION_MESSAGE);
 	    			}else{
 	    				if(tablaRh.rowAtPoint(e.getPoint())+1 == tablaRh.getRowCount() ){
 	    					return;
 	    				}else{
 	    					int fila = tablaRh.getSelectedRow();
 	    					Object folio =  tablaRh.getValueAt(fila, 0);
 	    					new Cat_Fue_Soda_Rh(folio+"").setVisible(true);
 	    				}
 	    			}    			
 	        	}
 	        }
         });
     }
 	
 	ActionListener opAceptar = new ActionListener(){
 		public void actionPerformed(ActionEvent arg0){
 			Obj_fuente_sodas_rh fuente_soda = new Obj_fuente_sodas_rh();
 			System.out.println(fuente_soda.actualizar_status_ticket());
 			Actualizar();
 			JOptionPane.showMessageDialog(null, "Se Comprobaron con exito","Aviso",JOptionPane.INFORMATION_MESSAGE);
 		}
 	};
 	
 	ActionListener opActualizar = new ActionListener(){
 		public void actionPerformed(ActionEvent arg0){
 			Actualizar();
 			String[][] TablaDifRH = getMatrizDifRH();
 			String[][] TablaDifAX = getMatrizDifAX();
 			if(TablaDifAX.length == 0 && TablaDifRH.length == 0){
 				btnAceptar.setEnabled(true);
 			}else{
				btnAceptar.setEnabled(false);		
 			}
 		}	
 	};
 	
 	public void Actualizar(){
 		int numeroRH = tablaRh.getRowCount();
 		while(numeroRH > 0){
 			modelRh.removeRow(0);
 			numeroRH --;
 		}
 		
 		int numeroAx = tablaAx.getRowCount();
 		while(numeroAx > 0){
 			modelAx.removeRow(0);
 			numeroAx --;
 		}
 		
 		int total_numeroRh = tablaTotalRH.getRowCount();
 		while(total_numeroRh > 0){
 			modelTotalRH.removeRow(0);
 			total_numeroRh --;
 		}
 		
 		int total_numeroAx = tablaTotalAX.getRowCount();
 		while(total_numeroAx > 0){
 			modelTotalAX.removeRow(0);
 			total_numeroAx --;
 		}
 		
 		String[][] TablaRH = getMatrizRH();
 		Object[] filaRH = new Object[tablaRh.getColumnCount()]; 
 		for(int i=0; i<TablaRH.length; i++){
 			modelRh.addRow(filaRH); 
 			for(int j=0; j<3; j++){
 				modelRh.setValueAt(TablaRH[i][j]+"", i,j);
 			}
 		}
 		
 		String[][] TablaAux = getMatrizAux();
 		Object[] filaAux = new Object[tablaAx.getColumnCount()]; 
 		for(int i=0; i<TablaAux.length; i++){
 			modelAx.addRow(filaAux); 
 			for(int j=0; j<3; j++){
 				modelAx.setValueAt(TablaAux[i][j]+"", i,j);
 			}
 		}
 		
 		String[][] TablaDifRH = getMatrizDifRH();
 		Object[] filaDifRH = new Object[tablaTotalRH.getColumnCount()]; 
 		for(int i=0; i<TablaDifRH.length; i++){
 			modelTotalRH.addRow(filaDifRH); 
 			for(int j=0; j<3; j++){
 				modelTotalRH.setValueAt(TablaDifRH[i][j]+"", i,j);
 			}
			sumaRH();
			
			sumaAX();

 		}
 		
 		String[][] TablaDifAX = getMatrizDifAX();
 		Object[] filaDifAX = new Object[tablaTotalAX.getColumnCount()]; 
 		for(int i=0; i<TablaDifAX.length; i++){
 			modelTotalAX.addRow(filaDifAX); 
 			for(int j=0; j<3; j++){
 				modelTotalAX.setValueAt(TablaDifAX[i][j]+"", i,j);
 			}
 		}
 		sumaRH();
 		
 		sumaAX();
 	}
 	public void Etiqueta(){
 		int fila=0;
 		tablaRh.getColumnModel().getColumn(fila).setHeaderValue("Folio");
 		tablaRh.getColumnModel().getColumn(fila).setMaxWidth(50);
 		tablaRh.getColumnModel().getColumn(fila).setMinWidth(50);
 		tablaRh.getColumnModel().getColumn(fila+=1).setHeaderValue("Nombre");
 		tablaRh.getColumnModel().getColumn(fila).setMaxWidth(200);
 		tablaRh.getColumnModel().getColumn(fila).setMinWidth(200);
 		tablaRh.getColumnModel().getColumn(fila+=1).setHeaderValue("Totales");
 		tablaRh.getColumnModel().getColumn(fila).setMaxWidth(70);
 		tablaRh.getColumnModel().getColumn(fila).setMinWidth(70);
 	
 		tablaAx.getColumnModel().getColumn(fila=0).setHeaderValue("Folio");
 		tablaAx.getColumnModel().getColumn(fila).setMaxWidth(50);
 		tablaAx.getColumnModel().getColumn(fila).setMinWidth(50);
 		tablaAx.getColumnModel().getColumn(fila+=1).setHeaderValue("Nombre");
 		tablaAx.getColumnModel().getColumn(fila).setMaxWidth(200);
 		tablaAx.getColumnModel().getColumn(fila).setMinWidth(200);
 		tablaAx.getColumnModel().getColumn(fila+=1).setHeaderValue("Totales");
 		tablaAx.getColumnModel().getColumn(fila).setMaxWidth(70);
 		tablaAx.getColumnModel().getColumn(fila).setMinWidth(70);
 		
 		tablaTotalRH.getColumnModel().getColumn(fila=0).setHeaderValue("Folio");
 		tablaTotalRH.getColumnModel().getColumn(fila).setMaxWidth(50);
 		tablaTotalRH.getColumnModel().getColumn(fila).setMinWidth(50);
 		tablaTotalRH.getColumnModel().getColumn(fila+=1).setHeaderValue("Nombre");
 		tablaTotalRH.getColumnModel().getColumn(fila).setMaxWidth(200);
 		tablaTotalRH.getColumnModel().getColumn(fila).setMinWidth(200);
 		tablaTotalRH.getColumnModel().getColumn(fila+=1).setHeaderValue("Totales");
 		tablaTotalRH.getColumnModel().getColumn(fila).setMaxWidth(70);
 		tablaTotalRH.getColumnModel().getColumn(fila).setMinWidth(70);
 		
 		tablaTotalAX.getColumnModel().getColumn(fila=0).setHeaderValue("Folio");
 		tablaTotalAX.getColumnModel().getColumn(fila).setMaxWidth(50);
 		tablaTotalAX.getColumnModel().getColumn(fila).setMinWidth(50);
 		tablaTotalAX.getColumnModel().getColumn(fila+=1).setHeaderValue("Nombre");
 		tablaTotalAX.getColumnModel().getColumn(fila).setMaxWidth(200);
 		tablaTotalAX.getColumnModel().getColumn(fila).setMinWidth(200);
 		tablaTotalAX.getColumnModel().getColumn(fila+=1).setHeaderValue("Totales");
 		tablaTotalAX.getColumnModel().getColumn(fila).setMaxWidth(70);
 		tablaTotalAX.getColumnModel().getColumn(fila).setMinWidth(70);
 		
 	}
 	
 	public String[][] getMatrizRH(){
 		String qry = "select tb_fuente_sodas_rh.folio_empleado as Num," + 
         					"tb_fuente_sodas_rh.nombre_completo as Nombre," +
         					"sum(tb_fuente_sodas_rh.cantidad)as Total " +
         					"from tb_fuente_sodas_rh " +
         				"where tb_fuente_sodas_rh.status ='1' and tb_fuente_sodas_rh.status_ticket='0' " +
         					"group by tb_fuente_sodas_rh.nombre_completo,tb_fuente_sodas_rh.folio_empleado "+
         					"order by tb_fuente_sodas_rh.folio_empleado";
 		
 		String[][] Matriz = new String[getFilas(qry)][3];
 		Connection conn = Connexion.conexion();
 		Statement s;
 		ResultSet rs;
 		try {
 			s = conn.createStatement();
 			rs = s.executeQuery(qry);
 			int i=0;
 			while(rs.next()){
 				Matriz[i][0] = rs.getString(1).trim();
 				Matriz[i][1] = rs.getString(2).trim();
 				Matriz[i][2] = rs.getString(3).trim();
 
 				i++;
 			}
 			
 		} catch (SQLException e1) {
 			e1.printStackTrace();
 		}
 	    return Matriz; 
 	}
 	
 	public String[][] getMatrizAux(){
 		String qry = " select tb_fuente_sodas_auxf.folio_empleado as Num,"+
         					"tb_fuente_sodas_auxf.nombre_completo as Nombre,"+
         					"sum(tb_fuente_sodas_auxf.cantidad)as Total "+
         					"from tb_fuente_sodas_auxf "+
         				"where tb_fuente_sodas_auxf.status ='1' and tb_fuente_sodas_auxf.status_ticket='0' "+
         					"group by tb_fuente_sodas_auxf.nombre_completo,tb_fuente_sodas_auxf.folio_empleado "+
         					"order by tb_fuente_sodas_auxf.folio_empleado";
 		
 		String[][] Matriz = new String[getFilas(qry)][3];
 		Connection conn = Connexion.conexion();
 		Statement s;
 		ResultSet rs;
 		try {
 			s = conn.createStatement();
 			rs = s.executeQuery(qry);
 			int i=0;
 			while(rs.next()){
 				Matriz[i][0] = rs.getString(1).trim();
 				Matriz[i][1] = rs.getString(2).trim();
 				Matriz[i][2] = rs.getString(3).trim();
 
 				i++;
 			}
 			
 		} catch (SQLException e1) {
 			e1.printStackTrace();
 		}
 	    return Matriz; 
 	}
 	
 	public String[][] getMatrizDifRH(){
 		String qry = "((select tb_fuente_sodas_rh.folio_empleado as Num,"+ 
           					"tb_fuente_sodas_rh.nombre_completo as Personal_Con_Diferencia,"+
           					"sum(tb_fuente_sodas_rh.cantidad)as Total "+
           						"from tb_fuente_sodas_rh "+
           					"where tb_fuente_sodas_rh.status ='1' and tb_fuente_sodas_rh.status_ticket='0' "+
           						"group by tb_fuente_sodas_rh.nombre_completo,tb_fuente_sodas_rh.folio_empleado) "+
           					"except "+
           						"(select tb_fuente_sodas_auxf.folio_empleado as Num,"+
           						"tb_fuente_sodas_auxf.nombre_completo as Personal_Con_Diferencia,"+
           						"sum(tb_fuente_sodas_auxf.cantidad)as Total "+
           						"from tb_fuente_sodas_auxf "+
           					"where tb_fuente_sodas_auxf.status ='1' and tb_fuente_sodas_auxf.status_ticket='0' "+
           						"group by tb_fuente_sodas_auxf.nombre_completo,tb_fuente_sodas_auxf.folio_empleado))";
 		
 		String[][] Matriz = new String[getFilas(qry)][3];
 		Connection conn = Connexion.conexion();
 		Statement s;
 		ResultSet rs;
 		try {
 			s = conn.createStatement();
 			rs = s.executeQuery(qry);
 			int i=0;
 			while(rs.next()){
 				Matriz[i][0] = rs.getString(1).trim();
 				Matriz[i][1] = rs.getString(2).trim();
 				Matriz[i][2] = rs.getString(3).trim();
 
 				i++;
 			}
 			
 		} catch (SQLException e1) {
 			e1.printStackTrace();
 		}
 	    return Matriz; 
 	}
 	
 	public String[][] getMatrizDifAX(){
 		String qry = "((select tb_fuente_sodas_auxf.folio_empleado as Num,"+
         					"tb_fuente_sodas_auxf.nombre_completo as Personal_Con_Diferencia,"+
         					"sum(tb_fuente_sodas_auxf.cantidad)as Total "+
         				"from tb_fuente_sodas_auxf "+
         				"where tb_fuente_sodas_auxf.status ='1' and tb_fuente_sodas_auxf.status_ticket='0' "+
         					"group by tb_fuente_sodas_auxf.nombre_completo,tb_fuente_sodas_auxf.folio_empleado) "+
         				"except "+
         					"(select tb_fuente_sodas_rh.folio_empleado as Num,"+
         					"tb_fuente_sodas_rh.nombre_completo as Personal_Con_Diferencia,"+
         					"sum(tb_fuente_sodas_rh.cantidad)as Total "+
         				"from tb_fuente_sodas_rh "+
         				"where tb_fuente_sodas_rh.status ='1' and tb_fuente_sodas_rh.status_ticket='0' "+
         					"group by tb_fuente_sodas_rh.nombre_completo,tb_fuente_sodas_rh.folio_empleado))";
 		
 		String[][] Matriz = new String[getFilas(qry)][3];
 		Connection conn = Connexion.conexion();
 		Statement s;
 		ResultSet rs;
 		try {
 			s = conn.createStatement();
 			rs = s.executeQuery(qry);
 			int i=0;
 			while(rs.next()){
 				Matriz[i][0] = rs.getString(1).trim();
 				Matriz[i][1] = rs.getString(2).trim();
 				Matriz[i][2] = rs.getString(3).trim();
 
 				i++;
 			}
 			
 		} catch (SQLException e1) {
 			e1.printStackTrace();
 		}
 	    return Matriz; 
 	}
 	
 	public static int getFilas(String qry){
 		int filas=0;
 		try {
 			Connection conn = Connexion.conexion();
 			Statement s = conn.createStatement();
 			ResultSet rs = s.executeQuery(qry);
 			while(rs.next()){
 				filas++;
 			}
 			
 		} catch (SQLException e1) {
 			e1.printStackTrace();
 		}
 		return filas;
 	}	
 	
 	public void sumaRH(){
 		float suma = 0;
 		
 		for(int i=0;i<modelRh.getRowCount(); i++) {
 			float datos= Float.parseFloat(modelRh.getValueAt(i,2).toString());
 			suma=(suma+datos); 
 		} 
 		Object[] filaSuma = new Object[tablaRh.getColumnCount()]; 
 		filaSuma[0]=".....";
 		filaSuma[1]="Total General";
 		filaSuma[2]=suma;
 		
 		modelRh.addRow(filaSuma);
 	}
 	
 	public void sumaAX(){
 		float suma = 0;
 		int cont = 0;
 			for(int i=0;i<modelAx.getRowCount(); i++) {
 				float datos= Float.parseFloat(modelAx.getValueAt(i,2).toString());
 				suma=(suma+datos); 
 				cont ++;
 			} 
 		Object[] filaSuma = new Object[tablaAx.getColumnCount()]; 
 			filaSuma[0]=".....";
 			filaSuma[1]="Total General";
 			filaSuma[2]=suma;
 			
 			modelAx.addRow(filaSuma);
 			
 	}
 	
 			
 }
