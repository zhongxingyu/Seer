 package GUI;
 
 import javax.swing.table.DefaultTableModel;
 
 public abstract class DataClosedZN {
 	
 	public static DefaultTableModel getKPModel(){
 		
 		return new DefaultTableModel(
 				new Object[][] {{null, null}},
 				new String[] {"K cr\u00EDtica", "P cr\u00EDtico"}){
 					private static final long serialVersionUID = 1L;
 				Class[] columnTypes = new Class[] {
 					Double.class, Double.class
 				};
 				public Class getColumnClass(int columnIndex) {
 					return columnTypes[columnIndex];
 				}
 				boolean[] columnEditables = new boolean[] {
 					false, false
 				};
 				public boolean isCellEditable(int row, int column) {
 					return columnEditables[column];
 				};
 			};
 	}
 	
 public static DefaultTableModel getTransientKPModel(double vK, double vP){
 		
 		return new DefaultTableModel(
 				new Object[][] {{vK, vP}},
 				new String[] {"K", "P"}){
 					private static final long serialVersionUID = 1L;
 				Class[] columnTypes = new Class[] {
 					Double.class, Double.class
 				};
 				public Class getColumnClass(int columnIndex) {
 					return columnTypes[columnIndex];
 				}
 				boolean[] columnEditables = new boolean[] {
 					false, false
 				};
 				public boolean isCellEditable(int row, int column) {
 					return columnEditables[column];
 				};
 			};
 	}
 	
 	public static DefaultTableModel getKPModel(double vK, double vP){
 		
 		return new DefaultTableModel(
 				new Object[][] {{vK, vP}},
 				new String[] {"K cr\u00EDtica", "P cr\u00EDtico"}){
 					private static final long serialVersionUID = 1L;
 				Class[] columnTypes = new Class[] {
 					Double.class, Double.class
 				};
 				public Class getColumnClass(int columnIndex) {
 					return columnTypes[columnIndex];
 				}
 				boolean[] columnEditables = new boolean[] {
 					false, false
 				};
 				public boolean isCellEditable(int row, int column) {
 					return columnEditables[column];
 				};
 			};
 	}
 	
 	public static DefaultTableModel getControllerModel(){
 		return new DefaultTableModel(
 				new Object[][] {{"P", "0.5 Kcr", null, null},
 								{"PI", "0.45 Kcr", "(1/1.2) Pcr", null},
 								{"PID", "0.6 Kcr", "0.5 Pcr", "0.125 Pcr"}},
 				new String[] {"Tipo de controlador", "Kc", "Ti", "Td"}){
 				
 				private static final long serialVersionUID = 1L;
 				
 				Class[] columnTypes = new Class[] {
 					String.class, String.class, String.class, String.class
 				};
 				public Class getColumnClass(int columnIndex) {
 					return columnTypes[columnIndex];
 				}
 				boolean[] columnEditables = new boolean[] {
 					false, false, false, false
 				};
 				public boolean isCellEditable(int row, int column) {
 					return columnEditables[column];
 				};
 			};
 	}
 	
 	public static DefaultTableModel getControllerModel(double criticK, double criticP){
 		
 		return new DefaultTableModel(
 				new Object[][] {
 						{"P", 0.5*criticK, null, null},
 						{"PI", 0.45*criticK, (1.0/1.2)*criticP, null},
 						{"PID", 0.6*criticK, 0.5*criticP, 0.125*criticP},
 				},
 				new String[] {"Tipo de controlador", "Kc", "Ti", "Td"}){
 				
 				private static final long serialVersionUID = 1L;
 				
 				Class[] columnTypes = new Class[] {
 					String.class, String.class, String.class, String.class
 				};
 				public Class getColumnClass(int columnIndex) {
 					return columnTypes[columnIndex];
 				}
 				boolean[] columnEditables = new boolean[] {
 					false, false, false, false
 				};
 				public boolean isCellEditable(int row, int column) {
 					return columnEditables[column];
 				};
 			};
 		
 	}
 	
 	private static String description = "Un procedimiento de sintonización a lazo cerrado  implica deshabilitar cualquier acción integral o derivativa del controlador./n"
 	+"Consiste en ir aumentando la ganancia del controlador hasta que la variable de proceso empiece a oscilar. La cantidad de ganancia "
 	+"necesaria para generar oscilaciones sostenidas (amplitud constante) es llamada “ultima ganancia” del proceso, mientras que el tiempo "
 	+"(periodo) entre cada pico de oscilación es llamado “último periodo” del proceso./n"
 	+"Ziegler y Nichols indican cómo se debe configurar la ganancia del controlador con un valor de la mitad de la última ganancia.  "
 	+"Kp = 0.5 Ku /n" 
 	+"Kp: Ganancia del controlador /n"
 	+"Ku: Ultima ganancia determinada incrementando la ganancia del controlador hasta lograr oscilación auto-sostenibles./n"
 	+"Una ganancia del controlador a la mitad de la “última ganancia” determinada experimentalmente, resulta una razonable "
 	+"respuesta rápida ante cambios de setpoint y cambios de carga./n" 
 	+"Las oscilaciones de la variable de proceso seguido de cambios de setpoint y de carga son atenuadas con cada pico de onda "
 	+"siendo esta aproximadamente un cuarto de la amplitud del pico anterior./n"  
	"Las reglas dadas por Ziegler y Nichols describen una relación real entre los parámetros de sintonización y las características "
 	+"operacionales del proceso. Para que el proceso oscile constantemente la ganancia del controlador debería ser alguna fracción de "
 	+"la ganancia necesaria y la constante de tiempo integral debería ser proporcional a la constante de tiempo del proceso.";
 		
 	public static String getDescription(){
 		return description;
 	}
 
 }
