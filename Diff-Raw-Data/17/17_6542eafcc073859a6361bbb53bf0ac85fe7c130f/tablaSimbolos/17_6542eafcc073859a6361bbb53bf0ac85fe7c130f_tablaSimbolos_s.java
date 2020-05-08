 
 import java.util.ArrayList;
 
 
 
 /**
  *
  * @author victor
  */
 public class tablaSimbolos {
     
     ArrayList<Simbolo> lista;
     tablaSimbolos anterior;
    int posicion_max;
     
     public tablaSimbolos(){
         lista = new ArrayList<Simbolo>();
         anterior = null;
        posicion_max = 1;
     }
     
     public tablaSimbolos(tablaSimbolos _anterior){
         lista = new ArrayList<Simbolo>();
         anterior = _anterior;
        posicion_max = _anterior.posicion_max;
     }
     
    public void add(String nombre, Tipo tipo) throws Sem_LexYaExiste{
         Simbolo item = new Simbolo(nombre,posicion_max, tipo);
         if (!contiene(item)) {
             lista.add(item);
            posicion_max++;
         }else{
             throw new Sem_LexYaExiste(item.nombre, 0, 0);
         }
     }
     
    public void add(Simbolo item) throws Sem_LexYaExiste{
		item.posicion_locals = posicion_max;
         if (!contiene(item)) {
             lista.add(item);
            posicion_max++;
         }else{
             throw new Sem_LexYaExiste(item.nombre, 0, 0);
         }
     }
     
     public Tipo getTipo(String nombre) throws Sem_LexNoDefinido {
         for (Simbolo simbolo : lista) {
             if (simbolo.nombre.equals(nombre)) {
                 return simbolo.tipo;
             }
         }
         // ha terminado la lista y no lo ha encontrado, llamamos a la lista anterior.
         if (anterior != null) {
             return anterior.getTipo(nombre);
         }
         // no se ha encontrado el símbolo
         throw new Sem_LexNoDefinido(nombre, 0, 0);
     }
     
     public Simbolo getSimbolo(String nombre) throws Sem_LexNoDefinido {
         for (Simbolo simbolo : lista) {
             if (simbolo.nombre.equals(nombre)) {
                 return simbolo;
             }
         }
         // ha terminado la lista y no lo ha encontrado, llamamos a la lista anterior.
         if (anterior != null) {
             return anterior.getSimbolo(nombre);
         }
         // no se ha encontrado el símbolo
         throw new Sem_LexNoDefinido(nombre, 0, 0);
     }
     
     public ArrayList<Simbolo> getAll(){
 		ArrayList<Simbolo> listado = new ArrayList<Simbolo>();
 		if (anterior != null)
 		{
 			listado.addAll(anterior.getAll());
 		}
 		listado.addAll(lista);
 		return listado;
 		
 	}
 
     
     public tablaSimbolos pop(){
         return anterior;
     }
 
     private boolean contieneTodo(Simbolo item) {
         if(anterior == null){
            return lista.contains(item); 
         }else{
             if(!lista.contains(item)) {
                 return anterior.contiene(item);
             }else{
                 return true;
             }
         }
     }
     
     private boolean contiene(Simbolo item) {
         return lista.contains(item);
     }
 
 }
