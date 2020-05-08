 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Actions;
 
 import Clases.GestionUniversidad;
 import Clases.Usuario;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 /**
  *
  * @author jaescalante02
  */
 public class GestionAvanzada extends org.apache.struts.action.Action {
 
 
     /* forward name="success" path="" */
     private static final String SUCCESS = "success";
     private static final String FAIL = "failure";
     
     /**
      * This is the action called from the Struts framework.
      *
      * @param mapping The ActionMapping used to select this instance.
      * @param form The optional ActionForm bean for this request.
      * @param request The HTTP Request we are processing.
      * @param response The HTTP Response we are processing.
      * @throws java.lang.Exception
      * @return
      */
     @Override
     public ActionForward execute(ActionMapping mapping, ActionForm form,
             HttpServletRequest request, HttpServletResponse response)
             throws Exception {
         
         String[]  arrunis={"Universidad de Karlsruhe", "Universidad de Stuttgart" ,"Universidad de Siegen",
         "Universidad Nacional de Cuyo", "Universidad Nacional de Quilmes", "Universidad Nacional de San Martín",
         "Universidad Tecnológica de Swinburne",
         "Universidad Católica de Lovaina",
         "Universidad Católica Boliviana San Pablo", "Universidad Privada Santa Cruz de la Sierra",
         "PSML - BRA - Universidad de Sao Paulo", "PSML - BRA - Universidad Federal de Río de Janeiro", "Universidad de Sao Paulo ", "Universidad Estadual de Campinas",
         "Instituto de Tecnología de Ontario",
         "PSML - CHI - Pontifica Universidad Católica de Chile", "Pontificia Universidad Católica de Chile", "Pontificia Universidad Católica de Valparaíso", "Universidad Austral de Chile", "Universidad de Concepción", "Universidad de Talca", "Universidad de Tarapacá",
         "Universidad de Costa Rica ",    
         "Pontificia Universidad Bolivariana", "PSML - COL - Pontificia Universidad Javeriana" ,"Universidad Externado", "Pontificia Universidad Javeriana ", "Universidad de los Andes", "Universidad del Norte", "Universidad del Valle",
         "Escuela Superior Politécnica del Litoral",
         "Universidad de Granada", "Universidad de Zaragoza", "Universidad Politécnica de Valencia", "PSML - ESP - Universidad Politécnica de Cataluña", "PSML - ESP - Universidad Politécnica de Madrid", "Universidad Carlos III de Madrid", "Universidad de Barcelona", "Universidad de las Islas Baleares", "Universidad de Santiago de Compostela", "Universitat Oberta de Catalunya", "Universitat Politécnica de Catalunya" ,
         "Universidad de Nuevo México", "Universidad de Oklahoma",
         "Universidad Abo Akademi",
         "Arts et Métiers ParisTech", "EPF Escuela de Ingenieros", "Escuela de Minas de Nantes", "INSA – Lyon", "TELECOM – SudParis", "Universidad Paris Est Marne la Vallé", "Universidad Paris Ouest Nanterre", "Universidad Tecnológica de Compiègne",
         "Politécnico de Milano", "Politécnico de Torino", "Universidad Degli Studi Di Genova",
         "Universidad de Tohoku", "Universidad Tecnológica de Nagaoka",
         "Instituto Tecnológico Superior de Cajeme (ITESCA)", "ITESM Monterrey", "Universidad Veracruzana",  "ITESM Monterrey ", "Universidad Autónoma Metropolitana",
         "NTNU- Trondheim",
        "Universidad San Martín de Porres","Pontificia Universidad Católica del Perú ", "Universidad de Lima", "Universidad del Pacífico ", "Universidad Peruana Cayetano Heredia",       
        "PSML - POL - AGH Universidad de Ciencias y Tecnología",
         "PSML - POR - Universidad Técnica de Lisboa",
         "Universidad de Panamá ",
         "Universidad Católica de Nuestra Señora de la Asunción",
         "Universidad de Puerto Rico",
         "Universidad de Ulster",
         "PSML - RPC - Universidad Técnica Checa en Praga",
         "Instituto Tecnológico de Santo Domingo", "Pontificia Universidad Católica Madre Maestra",
         "Real Instituto de Estocolmo KTH", "Universidad de Lund", "Universidad de Uppsala",     
         "Universidad Católica del Uruguay",
         "Universidad Centroccidental Lisandro Alvarado", "Universidad Metropolitana"};
         
         
         
         String[] paises = {"Alemania","Alemania","Alemania",
         "Argentina","Argentina", "Argentina",
         "Australia", 
         "Bélgica",
         "Bolivia","Bolivia",
         "Brasil","Brasil","Brasil","Brasil",
         "Canadá",
         "Chile","Chile","Chile","Chile","Chile","Chile","Chile",
         "CostaRica",
         "Colombia","Colombia","Colombia","Colombia","Colombia","Colombia","Colombia",
         "Ecuador",
         "España","España","España","España","España","España","España","España","España","España","España",
         "EstadosUnidos","EstadosUnidos",
         "Finlandia",
         "Francia","Francia","Francia","Francia","Francia","Francia","Francia","Francia",
         "Italia","Italia","Italia",
         "Japón","Japón",
         "México","México","México","México","México",
         "Noruega",
         "Perú","Perú","Perú","Perú","Perú",
         "Polonia",
         "Portugal",
         "Panamá",
         "Paraguay",
         "PuertoRico",
         "Reinounido",
         "RepúblicaCheca",
         "RepúblicaDominicana","RepúblicaDominicana",
         "Suecia","Suecia","Suecia",
         "Uruaguay",
         "Venezuela","Venezuela"
         };
                 
         Usuario u = (Usuario) form;
         GestionUniversidad[] gestuniv = new GestionUniversidad[arrunis.length];
         for(int i=0; i<arrunis.length;i++){
         gestuniv[i] = DBMS.DBMS.getInstance().calcularavgunivGestion(u.getNombreusuario(), arrunis[i]);
            
         gestuniv[i].setpais(paises[i]);
         gestuniv[i].setuniv(arrunis[i]);
         }
         
         this.mergesortC(gestuniv,gestuniv.length);
         String[] mejorc = new String[10];
         String[] mejorg = new String[10];
         String[] paisc = new String[10];
         String[] paisg = new String[10];
         int j=0;
         
         while(j<10){
         
             mejorc[j]=gestuniv[j].getuniv();
             paisc[j]= gestuniv[j].getpais();
            
             j++;
         }
         j=0;
         
         this.mergesortG(gestuniv,gestuniv.length);
         
         while(j<10){
         
             mejorg[j]=gestuniv[j].getuniv();
            paisg[j]=gestuniv[j].getpais();
             j++;
         }
         
         request.setAttribute("mejorc", mejorc);
         request.setAttribute("mejorg", mejorg);
         request.setAttribute("paisc", paisc);
         request.setAttribute("paisg", paisg);        
         
         return mapping.findForward(SUCCESS);
     }
     
         public void mezclarG(GestionUniversidad[] arreglo, int izqi, int medi, int deri){
       
 
         GestionUniversidad[] aux = new GestionUniversidad[deri- izqi + 2]; // Arreglo auxiliar.
 	    int i= izqi; // Contador.
 	    int j= medi; // Contador.
 	    int k = 0; //Contador.
 
 	    /*
 	     * Se colocan en el arreglo auxiliar en orden los elementos de
 	     * lo 2 arreglos ordenados.
 	     */
 		while ((i != medi) && (j != deri)){
 		
 			/*
 			 * Se escoje el menor elemento entre los 2 elementos menores
 			 * de ambos arreglos ordenados.
 			 */
 			if (arreglo[i].mayorQueG(arreglo[j])){
 			
 				aux[k]= arreglo[i];
 				i= i+1;
 			} else {
 			
 				aux[k]= arreglo[j];
 				j= j+1;
 			}
 
 		    k= k+1;
 		}
 
 		/*
 		 * Ciclo para terminar de cargar en aux el arreglo 1.
 		 */
 		while (i != medi){
 		
 			aux[k]= arreglo[i];
 			i= i+1;
 			k= k+1;
 		}
 
 		/*
 		 * Ciclo para terminar de cargar el arreglo 2.
 		 */
 		while (j != deri){
 		
 			aux[k]= arreglo[j];
 			j= j+1;
 			k= k+1;
 		}
 
 		/*
 		 * Nota: Solo entrara en uno de los ultimos 2 ciclos.
 		 */
 		
 		k=0;
 			
 		/*
 		 * Se coloca el arreglo auxiliar ordenado, en su posicion 
 		 * del arreglo final.
 		 */
 		while (k<(deri-izqi)){
 		
 			arreglo[izqi+k]= aux[k];
 			k= k+1;
 		}
 
     }
 
 
     public void OrdenarG(GestionUniversidad[] arreglo, int izq, int der){
 
     	/*
     	 * Llamada recursiva a ordenar, si la seccion del arreglo es
     	 * de mas de 3 elementos. 
     	 */
 		if (2<=(der-izq)) {
 		
 			int med = (izq+der)/ 2;
 			OrdenarG(arreglo, izq, med);
 			OrdenarG(arreglo, med, der);
 			mezclarG(arreglo, izq, med, der);
 		}
   
     }
 
 
     public void mergesortG(GestionUniversidad[] arreglo, int n){
 
         // Llamada al algoritmo recursivo.
 	    OrdenarG(arreglo, 0, n);
 
     }
     
 /////////////////////////////C////////////////////
     
             public void mezclarC(GestionUniversidad[] arreglo, int izqi, int medi, int deri){
       
 
         GestionUniversidad[] aux = new GestionUniversidad[deri- izqi + 2]; // Arreglo auxiliar.
 	    int i= izqi; // Contador.
 	    int j= medi; // Contador.
 	    int k = 0; //Contador.
 
 	    /*
 	     * Se colocan en el arreglo auxiliar en orden los elementos de
 	     * lo 2 arreglos ordenados.
 	     */
 		while ((i != medi) && (j != deri)){
 		
 			/*
 			 * Se escoje el menor elemento entre los 2 elementos menores
 			 * de ambos arreglos ordenados.
 			 */
 			if (arreglo[i].mayorQueC(arreglo[j])){
 			
 				aux[k]= arreglo[i];
 				i= i+1;
 			} else {
 			
 				aux[k]= arreglo[j];
 				j= j+1;
 			}
 
 		    k= k+1;
 		}
 
 		/*
 		 * Ciclo para terminar de cargar en aux el arreglo 1.
 		 */
 		while (i != medi){
 		
 			aux[k]= arreglo[i];
 			i= i+1;
 			k= k+1;
 		}
 
 		/*
 		 * Ciclo para terminar de cargar el arreglo 2.
 		 */
 		while (j != deri){
 		
 			aux[k]= arreglo[j];
 			j= j+1;
 			k= k+1;
 		}
 
 		/*
 		 * Nota: Solo entrara en uno de los ultimos 2 ciclos.
 		 */
 		
 		k=0;
 			
 		/*
 		 * Se coloca el arreglo auxiliar ordenado, en su posicion 
 		 * del arreglo final.
 		 */
 		while (k<(deri-izqi)){
 		
 			arreglo[izqi+k]= aux[k];
 			k= k+1;
 		}
 
     }
 
 
     public void OrdenarC(GestionUniversidad[] arreglo, int izq, int der){
 
     	/*
     	 * Llamada recursiva a ordenar, si la seccion del arreglo es
     	 * de mas de 3 elementos. 
     	 */
 		if (2<=(der-izq)) {
 		
 			int med = (izq+der)/ 2;
 			OrdenarC(arreglo, izq, med);
 			OrdenarC(arreglo, med, der);
 			mezclarC(arreglo, izq, med, der);
 		}
   
     }
 
 
     public void mergesortC(GestionUniversidad[] arreglo, int n){
 
         // Llamada al algoritmo recursivo.
 	    OrdenarC(arreglo, 0, n);
 
     }
     
     
 }
