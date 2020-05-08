 
 import java.util.ArrayList;
 
 
 
 /**
  *
  * @author victor
  */
 public class tablaSimbolos {
     
     ArrayList<Simbolo> lista;
     tablaSimbolos anterior;
     String nombre;
     
     public tablaSimbolos(){
         lista = new ArrayList<Simbolo>();
         anterior = null;
         nombre = "";
     }
     
     public tablaSimbolos(tablaSimbolos _anterior){
         lista = new ArrayList<Simbolo>();
         anterior = _anterior;
         nombre = _anterior.getNombre();
     }
 
     public tablaSimbolos(tablaSimbolos _anterior, String _nombre){
         lista = new ArrayList<Simbolo>();
         anterior = _anterior;
         nombre = _nombre;
     }
 
     public String getNombre(){
         return nombre;
     }
     
    public void add(String nombre, Tipo tipo, int posicion_max, Visibilidad _vis, TipoSimbolo tipoSimb) throws Error_1, Error_20{
         Simbolo item = new Simbolo(nombre,posicion_max, tipo,_vis, tipoSimb);
         if (!contiene(item,item.tipo.dimension)) {
             lista.add(item);
         }else{
             throw new Error_1(item.nombre, 0, 0);
         }
     }
     
    public int add(Simbolo item) throws Error_1, Error_20{
         if (!contiene(item,item.tipo.dimension)) {
             lista.add(item);
             return item.posicion_locals;
         }else{
             throw new Error_1(item.nombre, 0, 0);
         }
     }
     
     public Tipo getTipo(String nombre) throws Error_2 {
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
         throw new Error_2(nombre, 0, 0);
     }
     
     public Simbolo getSimbolo(String nombre) throws Error_2 {
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
         throw new Error_2(nombre, 0, 0);
     }
     
     public Simbolo getSimbolo(String nombre, int dimension) throws Error_2,Error_21 {
         boolean encontrado = false;
         for (Simbolo simbolo : lista) {
             if (simbolo.nombre.equals(nombre)){
                 if((simbolo.getTipoSimbolo() == TipoSimbolo.metodo || simbolo.getTipoSimbolo() == TipoSimbolo.constructor) && simbolo.tipo.dimension == dimension){
                     return simbolo;
                 }else if((simbolo.getTipoSimbolo() == TipoSimbolo.metodo || simbolo.getTipoSimbolo() == TipoSimbolo.constructor)){
                     encontrado = true;
                 }else{
                     return simbolo;
                 }
                 
             }
         }
         if(encontrado)
              throw new Error_21(getNombre(),nombre,dimension,0,0);
         // ha terminado la lista y no lo ha encontrado, llamamos a la lista anterior.
         if (anterior != null) {
             return anterior.getSimbolo(nombre,dimension);
         }
         // no se ha encontrado el símbolo
         throw new Error_2(nombre, 0, 0);
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
 
     public ArrayList<Simbolo> getListaSimbolos(){
         return lista;
     }
 
     
     public tablaSimbolos pop(){
         return anterior;
     }
 
    private boolean contieneTodo(Simbolo item) throws Error_20{
         if(anterior == null){
            return lista.contains(item); 
         }else{
             if(!lista.contains(item)) {
                return anterior.contiene(item,item.getDimension());
             }else{
                 return true;
             }
         }
     }
     
    private boolean contiene(Simbolo item, int dimension)throws Error_20 {
       /*  if(dimension == -1)// add
             return lista.contains(item);
 */
             // get
         for (Simbolo simb : lista) {
             if(simb.getNombre().equals(item.getNombre())){
                 // el nombre coincide y ambos son metodo/constructor
                 if((simb.getTipoSimbolo() == TipoSimbolo.metodo || simb.getTipoSimbolo() == TipoSimbolo.constructor)
                      && (item.getTipoSimbolo() == TipoSimbolo.metodo || item.getTipoSimbolo() == TipoSimbolo.constructor)){
                         // si coinciden los parametros, true
                     if(item.tipo.dimension == simb.tipo.dimension)
                        throw new Error_20(item.getNombreClase(),item.getNombre(),item.getDimension(),0,0);
                 } // el nombre coincide y ninguno es metodo/constructor
                 else if((simb.getTipoSimbolo() != TipoSimbolo.metodo && simb.getTipoSimbolo() != TipoSimbolo.constructor)
                      && (item.getTipoSimbolo() != TipoSimbolo.metodo && item.getTipoSimbolo() != TipoSimbolo.constructor)){
                     return true;
                 } // el nombre coincide y uno es metodo/constructor y el otro no
                 else{
                     return true;
                 }
             }
         }
         return false;
     }
 
     @Override
     public String toString(){
         String salida = "tabla de símbolos: " + "\n";
         for (Simbolo s : lista) {
             salida += s + "\n";
         }
         return salida;
     }
 
 }
