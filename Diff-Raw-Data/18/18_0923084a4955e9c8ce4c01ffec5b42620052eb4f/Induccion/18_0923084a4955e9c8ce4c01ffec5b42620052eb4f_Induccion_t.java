 package algoritmosdeinferencia;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Stack;
 
 /**
  *
  * @author Charlie Corner
  */
 public class Induccion extends AlgoritmoDeInferencia {
 
     private Stack<Regla> reglasPorDisparar;
     private Stack<List<String>> pilaDeObjetivos;
     private List<Regla> reglasDisparadas;
     private String objetivo;
     private boolean seEncontroObjetivo;
 
     public Induccion(List<Regla> conjuntoDeReglas, List<String> hechosDeInicio, String objetivo) {
         this.conjuntoDeReglas = conjuntoDeReglas;
         this.hechosDeInicio = hechosDeInicio;
         this.reglasDisparadas = new ArrayList<Regla>();
         this.hechosInferidos = new ArrayList<String>();
         this.hechosPreguntados = new ArrayList<String>();
         this.reglasPorDisparar = new Stack<Regla>();
         this.pilaDeObjetivos = new Stack<List<String>>();
         this.objetivo = objetivo;
         this.seEncontroObjetivo = false;
     }
 
     @Override
     public String correrAlgoritmo() {
         List<String> listaObjetivos = new ArrayList<String>();
         listaObjetivos.add(objetivo);
         pilaDeObjetivos.push(listaObjetivos);
 
         while (!pilaDeObjetivos.empty()) {
             listaObjetivos = pilaDeObjetivos.peek();
             boolean seCumpleRegla = true;
 
             for (int i = 0; i < listaObjetivos.size(); i++) {
                 String s = listaObjetivos.get(i);
 
                 // Si se encuentra el elemento en alguna de las listas
                 if (isElementoEnListas(s)) {
 
                     if (objetivo.equals(s)) {
                         seEncontroObjetivo = true;
                         pilaDeObjetivos.pop();
                         break;
 
                     } else {
 
                        if ((i+1) == listaObjetivos.size()) {
                             pilaDeObjetivos.pop();
                            Regla r = reglasPorDisparar.pop();
                            agregarAHechosInferidos(r.getProducidos());
                            reglasDisparadas.add(r);
                         } else {
                             continue;
                         }
                     }
                     // Si el elemento no se encuentra en las listas, buscamos en reglas o preguntamos
                 } else {
                     Regla reglaAComprobar = buscarReglaQueProduzca(s);
 
                     if (null == reglaAComprobar) {
 
                         if (preguntarAlUsuarioSiEsta(s)) {
                             agregarAHechosPreguntados(s);
                             continue;
                             
                         } else {
                             seCumpleRegla = false;
                             break;
                         }
 
                     } else {
                         pilaDeObjetivos.push(reglaAComprobar.getCausantes());
                         reglasPorDisparar.push(reglaAComprobar);
                         break;
                     }
                 }
             }
 
             if (!seCumpleRegla) {
                 break;
             }
         }
 
         resultado = crearTextoRespuesta();
         return resultado;
     }
 
     @Override
     protected String crearTextoRespuesta() {
         StringBuilder sb = new StringBuilder("***Resultados del algoritmo de inducción***\n\n");
         sb = sb.append("Explicación:\n");
         sb = sb.append("Objetivo: ").append(objetivo).append('\n');
 
         for (Regla r : reglasDisparadas) {
             sb = sb.append("Se disparó la regla ").append(r.getIndiceDeRegla()).append(" y se agregó: ").append(r.getProducidos());
             sb = sb.append('\n');
         }
         sb = sb.append('\n');
         sb = sb.append("Hechos de inicio: \n");
 
         for (String s : hechosDeInicio) {
             sb = sb.append(s).append('\n');
         }
 
         sb = sb.append('\n');
         sb = sb.append("Hechos preguntados: \n");
 
         for (String s : hechosPreguntados) {
             sb = sb.append(s).append('\n');
         }
 
         sb = sb.append('\n');
         sb = sb.append("Hechos inferidos: \n");
 
         for (String s : hechosInferidos) {
             sb = sb.append(s).append('\n');
         }
 
         sb = sb.append(seEncontroObjetivo ? "Se encontró el objetivo!\n"
                 : "No se encontró el objetivo :(\n");
         return sb.toString();
     }
 
     private Regla buscarReglaQueProduzca(String productoABuscar) {
         Regla reglaARegresar = null;
 
         for (Regla r : conjuntoDeReglas) {
             String productoDeRegla = r.getProducidos();
 
             if (productoABuscar.equals(productoDeRegla)) {
                 reglaARegresar = r;
                 break;
             }
         }
 
         return reglaARegresar;
     }
 
     public String getObjetivo() {
         return objetivo;
     }
 
     public Stack<List<String>> getPilaDeObjetivos() {
         return pilaDeObjetivos;
     }
 
     public List<Regla> getReglasDisparadas() {
         return reglasDisparadas;
     }
 
     public Stack<Regla> getReglasPorDisparar() {
         return reglasPorDisparar;
     }
 
     public boolean isSeEncontroObjetivo() {
         return seEncontroObjetivo;
     }
 }
