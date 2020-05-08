 import java.io.*; //include Java's standard Input and Output routines
 import java.sql.*;
 import java.util.*;
 import javax.swing.*;
 import java.lang.ClassLoader;
 import java.lang.Throwable;
 import java.awt.*;
 import java.awt.event.*;
 import java.awt.print.PrinterJob;
 import javax.print.*;
 import javax.print.attribute.*;
 import javax.print.attribute.standard.*;
 import net.sf.jasperreports.engine.JRException;
 import net.sf.jasperreports.engine.export.JRPrintServiceExporter;
 import net.sf.jasperreports.engine.export.JRPrintServiceExporterParameter;
 import net.sf.jasperreports.engine.JRExporterParameter;
 import net.sf.jasperreports.engine.JasperCompileManager;
 import net.sf.jasperreports.engine.JasperExportManager;
 import net.sf.jasperreports.engine.JasperFillManager;
 import net.sf.jasperreports.engine.JasperPrint;
 import net.sf.jasperreports.engine.JasperReport;
 import net.sf.jasperreports.engine.util.JRLoader;
 import net.sf.jasperreports.view.JasperViewer;
 import net.sf.jasperreports.engine.data.JRXmlDataSource;
 
 
 
 public class enebooreports {
  
 public static String ficheroTemp;
 public static String build = "Build " + jrversion.eReports();
 public static String versionJR = jrversion.jasper();
 public static JRPrintServiceExporter exporter = new JRPrintServiceExporter();
 
                 public static void main(String[] args) throws IOException {
                 	    
 			try {
 			    String ficheroTemp;
                             String impresora;
                             String changelog = "";
 			    Boolean pdf,impDirecta, guardaTemporal;
 			    int nCopias, nParametrosJasper;
 			    long start;
 			    Class.forName(args[0]);
 			    //JOptionPane.showMessageDialog(null, "Init iniciado" , "Eneboo Reports", 1);
 			    InputStream is=enebooreports.class.getClass().getResourceAsStream("/otros/init.jasper");
 			    JasperReport creditos = (JasperReport) JRLoader.loadObject(is);
 			    JasperReport report = creditos;
 
 			    // Cargamos changelog
 			    InputStream chl = enebooreports.class.getResourceAsStream("/otros/changelog");
      			    BufferedReader br2 = new BufferedReader(new InputStreamReader(chl));
      			    String line= null;
      			    String listadoCompleto = "";
      			    while (null != (line = br2.readLine())) {
         							 listadoCompleto = listadoCompleto + line + "<br>";
       				        			    }  
 
    
                             Connection conn = DriverManager.getConnection(args[1],args[2],args[3]);
                             //JOptionPane.showMessageDialog(null, "Init finalizado" , "Eneboo Reports", 1);
 			    do
                               {
                             BufferedReader stdin = new BufferedReader (new InputStreamReader(System.in));
 			    System.out.flush();// empties buffer, before you input text
 			    ficheroTemp =""; //Nombre fichero Temporal
           		    ficheroTemp = stdin.readLine();
 			    if (ficheroTemp == null ) break;
 			    enebooreports.ficheroTemp = ficheroTemp;
 			    start = System.currentTimeMillis(); /* Para controlar el tiempo */					
                             guardaTemporal = false; //bool que indica si se borra o no el temp al finalizar de usarlo.
                             guardaTemporal = Boolean.parseBoolean(stdin.readLine());
 		            pdf = false; // exporta a pdf
                             pdf = Boolean.parseBoolean(stdin.readLine());
 		            nCopias = 0; // Número de copias
                             nCopias = Integer.parseInt(stdin.readLine());
 			    impresora =""; // nombre de impresora
                             impresora = stdin.readLine();
 			    impDirecta = false; // impresión directa
                             impDirecta = Boolean.parseBoolean(stdin.readLine());
                             nParametrosJasper = 0; // Número de parametros que vienen (Pareja Nombre-Valor)
                             nParametrosJasper = Integer.parseInt(stdin.readLine());
                             String[] parametroNombre = new String[nParametrosJasper]; 
                             String[] parametroValor = new String[nParametrosJasper];
                           				  for(int i = 0; i < nParametrosJasper; i++ ) {
 	 									parametroNombre[i] = stdin.readLine();
 	 									parametroValor[i] = stdin.readLine();  
 													}
 
                         java.util.Map<String, Object> hm = new HashMap<String,Object>(); //INICIALIZO MAPA    				
 			if (ficheroTemp.equals( "version" )) 
 						{
 						report = creditos;
 						guardaTemporal = true;//Para no intentar borrar luego un fichero que no existe
 						hm.put("VERSION", enebooreports.build);
 						hm.put("CHANGELOG",listadoCompleto);
 						hm.put("VERSIONJR",enebooreports.versionJR);						
 						}
 					else {     
                           	       		if (ficheroTemp.equals( "Repetir" ))//Solo compilar si no se llama repetir.
                                        				guardaTemporal = true; //Para no intentar borrar luego un fichero que no existe
                                        			 		 else 
                                         		 	report = JasperCompileManager.compileReport(ficheroTemp);
 
 	                                       for(int j = 0; j < nParametrosJasper; j++ )
                          				  if(!parametroValor[j].equals( "") && !parametroNombre[j].equals( ""))
 	 						 		hm.put(parametroNombre[j], parametroValor[j]); //Seteamos Parametros en mapa
 					      }
 						 
 					JasperPrint print = JasperFillManager.fillReport(report, hm, conn); //Rellenamos el report compilado
 					if (impDirecta) impresionDirecta( impresora, nCopias, print );
 							else
 					 		if(pdf) JasperExportManager.exportReportToPdfFile(print, impresora); // Exporta el informe a PDF
 								          else mostrarVisor( print, build, start);
 							  	       
 				if (!guardaTemporal)
 						{
                                    		File ficheroT = new File(ficheroTemp);
                                     		if (!ficheroT.delete())
                                          		JOptionPane.showMessageDialog(null, "El fichero Temporal " + ficheroTemp + " no se puede borrar." , "Eneboo Reports", 1);
 						}
 					  
 					
  				} while (!ficheroTemp.equals( "version" ));
                                         
 						
 			}
 			catch (Exception e) {
 			 crearLogError(e);
 			       		
 		       }  
 		
 	}
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	
 	public static void mostrarVisor(JasperPrint print, String build, long start) 
 									{
 							try {
 									JasperViewer viewer = new JasperViewer(print, false);
                                     					viewer.setTitle(print.getName() + " - Eneboo Reports"); 
 									viewer.setIconImage(new javax.swing.ImageIcon(enebooreports.class.getClass().getResource("/otros/logo32.gif")).getImage());
 									viewer.setVisible(true);
 									viewer.setAlwaysOnTop(true);
 									viewer.setModalExclusionType(Dialog.ModalExclusionType.APPLICATION_EXCLUDE);
 									viewer.setAlwaysOnTop(false); //Ahora no interesa estar siempre 
 								    	}
 					 catch (Exception e) {  
             JOptionPane.showMessageDialog(null, "mostrarVisor :: Se ha producido un error (Exception) : \n " + e.toString() , "Eneboo Reports", 1);
 	    e.printStackTrace();
 		crearLogError(e);
 		       }
 		       }  
 	
 	
 				
 					
 					
 	public static void impresionDirecta(String impresora, int nCopias, JasperPrint print) {
 	
 		try
 		{
 		
 		//Aqui imprimimos directamente en var impresora...
 		//JasperPrint print = JasperFillManager.fillReport( this.class.getResource("/classpath/yourReport.jasper").getPath(), new HashMap(), new yourReportDataSource());
 		PrinterJob job = PrinterJob.getPrinterJob();
 		/* Create an array of PrintServices */
 		PrintService[] services = PrintServiceLookup.lookupPrintServices(null, null);
 		int selectedService = -1;
 		String listadoImpresorasDisponibles = "";
 		/* Scan found services to see if anyone suits our needs */
 		for(int i = 0; i < services.length;i++){
 				if(services[i].getName().contains(impresora))
 						{
 						job.setPrintService(services[i]);
 						selectedService = i;	
 						}
 				listadoImpresorasDisponibles += services[i].getName() + "\n";
 							}
 		if (listadoImpresorasDisponibles.equals("")) listadoImpresorasDisponibles = "¡¡ Opppps !! . No se han detectado impresoras en el sistema";
 		if ( selectedService > -1) 
 			{
 			PrintRequestAttributeSet printRequestAttributeSet = new HashPrintRequestAttributeSet();
 			//MediaSizeName mediaSizeName = MediaSize.findMedia(4,4,MediaPrintableArea.INCH);
 			//printRequestAttributeSet.add(mediaSizeName);
 			printRequestAttributeSet.add(new Copies(nCopias)); // *************** Numero de copias
 			exporter.setParameter(JRExporterParameter.JASPER_PRINT, print);
 			/* We set the selected service and pass it as a paramenter */
 			exporter.setParameter(JRPrintServiceExporterParameter.PRINT_SERVICE, services[selectedService]);
 			exporter.setParameter(JRPrintServiceExporterParameter.PRINT_SERVICE_ATTRIBUTE_SET, services[selectedService].getAttributes());
 			exporter.setParameter(JRPrintServiceExporterParameter.PRINT_REQUEST_ATTRIBUTE_SET, printRequestAttributeSet);
 			exporter.setParameter(JRPrintServiceExporterParameter.DISPLAY_PAGE_DIALOG, Boolean.FALSE);
 			exporter.setParameter(JRPrintServiceExporterParameter.DISPLAY_PRINT_DIALOG, Boolean.FALSE);
 					
 			exporter.exportReport();
 			} else JOptionPane.showMessageDialog(null, "Eneboo Reports :: impresionDirecta :: No existe la impresora especificaca : ( " + impresora + " ).\n\nEspecifique alguna de las siguientes impresoras :\n" + listadoImpresorasDisponibles , "Eneboo Reports", 1);
 						
 		}catch (Exception e) {  
             JOptionPane.showMessageDialog(null, "impresionDirecta :: Se ha producido un error (Exception) : \n " + e.toString(), "Eneboo Reports", 1);
 	    e.printStackTrace();
 		       }  									
 		       
 		  }
 		  
 	public static void crearLogError(Exception error) {
 		try
 		{
 			String cabecera;
 			String ficheroError = enebooreports.ficheroTemp+ "_error.txt";
 			
 			///Generamos cabecera
 			cabecera = "\nEneboo Reports :\n\n* " + enebooreports.build + ".\n* Versión Jasper Reports " + enebooreports.versionJR + ".\n* Nombre fichero " + enebooreports.ficheroTemp + ".\n\n\n* StackTrace Java :\n";  	
 
 	
 			
 			FileOutputStream fos = new FileOutputStream(ficheroError);
 			PrintStream ps = new PrintStream(fos);
 			ps.printf(cabecera);
 			error.printStackTrace(ps);
 			ps.close();  
 			JOptionPane.showMessageDialog(null, "Se ha producido un error : \n" + error.toString() + "\n\nConsulte " + ficheroError + " para más información \n ", "Eneboo Reports", 1);
 
 	
 	}catch (Exception e) {  
             JOptionPane.showMessageDialog(null, "crearLogError :: Se ha producido un error (Exception) : \n " + e.toString(), "Eneboo Reports", 1);
 	    e.printStackTrace();
 			     }  		
 	}
 }
