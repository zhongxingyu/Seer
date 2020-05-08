 import java.io.BufferedOutputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Iterator;
import java.util.logging.Logger;
 
 import paginaweb.PaginaWeb;
 
 
 
 public class GeneradorInformeHTML extends GeneradorInforme {
 
 	@Override
 	public boolean generar() {
 		// TODO Auto-generated method stub
 		paginaWeb = new PaginaWeb();
 		
 		this.setNomInforme("Informe - " + this.generateDateStamp());
 		paginaWeb.afegeixLiniaSeparacio();
 		paginaWeb.afegeixHeader(this.getNomInforme(), 1, true);
 		paginaWeb.afegeixLiniaSeparacio();
 		
 		Iterator<Taula> iterador = informe.getTaules().iterator();
 		Taula taula;
 		
 		while(iterador.hasNext()){
 			taula = (Taula) iterador.next();
 			paginaWeb.afegeixTaula(taula.getTaula(), true, true);
 		}
 		
 		ArrayList<Taula> taules = informe.getTaules();
		System.out.println("----- numero de taules : "+taules.size());
		//paginaWeb.afegeixTaula(taules.get(0).getTaula(), true, true);
		//paginaWeb.afegeixTaula(taules.get(1).getTaula(), true, true);
 		
 		//paginaWeb.afegeixTaula(informe.getTaulaProjectes().getTaula(), true, true);
 		
 		return this.guardarFitxer();
 	}
 
 
 		/**
 		 * @uml.property  name="paginaWeb"
 		 */
 		private PaginaWeb paginaWeb;
 
 
 		/**
 		 * Getter of the property <tt>paginaWeb</tt>
 		 * @return  Returns the paginaWeb.
 		 * @uml.property  name="paginaWeb"
 		 */
 		public PaginaWeb getPaginaWeb() {
 			return paginaWeb;
 		}
 
 
 		/**
 		 * Setter of the property <tt>paginaWeb</tt>
 		 * @param paginaWeb  The paginaWeb to set.
 		 * @uml.property  name="paginaWeb"
 		 */
 		public void setPaginaWeb(PaginaWeb paginaWeb) {
 			this.paginaWeb = paginaWeb;
 		}
 
 
 			
 			/**
 			 * Constructor de la classe, t com a parmetre un objecte de
 			 * la clase Informe. Aquest Informe l'estableix com a l'informe de la clase.
 			 */
 			public GeneradorInformeHTML(Informe informe){
 				this.setInforme(informe);
 			}
 
 
 				
 				/**
 				 * Guarda l'informe en format txt en un fitxer dins el directori /informes
 				 * @return Boolean True si s'ha guardat, False si hi ha hagut error
 				 */
 				public boolean guardarFitxer(){
 					/**
 					 * El mtode escriuPagina() de la clase PaginaWeb imprimeix per
 					 * pantalla la taula en format HTML, canviem la interfcie que acta com a out
 					 * per el fitxer on es guardar l'informe
 					 */
 					try {
 						//String nomFitxer = "informe-" + this.generateDateStamp() + ".html";
 						String nomFitxer = "informe.html";
 						String ruta = "";
 						PrintStream out = new PrintStream(
 								new BufferedOutputStream(new FileOutputStream(ruta + "" +nomFitxer)));
 						PrintStream outAux = System.out;
 						System.setOut(out);
 						paginaWeb.afegeixLiniaSeparacio();
 						paginaWeb.afegeixTextNormal("Time Tracker");
 						paginaWeb.escriuPagina();
 						out.close();
 						System.setOut(outAux);
 						return true;
 					}catch(FileNotFoundException e){
 						e.printStackTrace();
 						return false;
 					}
 				
 				}
 
 
 				/**
 				 * @uml.property  name="informe"
 				 */
 				private Informe informe;
 
 
 				/**
 				 * Getter of the property <tt>informe</tt>
 				 * @return  Returns the informe.
 				 * @uml.property  name="informe"
 				 */
 				public Informe getInforme() {
 					return informe;
 				}
 
 
 				/**
 				 * Setter of the property <tt>informe</tt>
 				 * @param informe  The informe to set.
 				 * @uml.property  name="informe"
 				 */
 				public void setInforme(Informe informe) {
 					this.informe = informe;
 				}
 }
