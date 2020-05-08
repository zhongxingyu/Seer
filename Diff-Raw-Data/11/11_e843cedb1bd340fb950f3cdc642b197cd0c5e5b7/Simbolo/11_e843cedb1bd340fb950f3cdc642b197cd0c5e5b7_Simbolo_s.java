 
 
 /**
  *
  * @author victor
  */
 public class Simbolo {
 
     String nombre;
     int posicion_locals;
     Tipo tipo;
     Visibilidad visibilidad;
     TipoSimbolo tipo_simb;
     String nombreClase;
     
     
     public Simbolo(String _nombre, int _pos, Tipo _tipo, Visibilidad _vis, TipoSimbolo _tipoSimb) {
         nombre = _nombre;
         posicion_locals = _pos;
         tipo = _tipo;
         visibilidad = _vis;
         tipo_simb = _tipoSimb;
         nombreClase = null;
     }
 
     public Simbolo(String _nombre, int _pos, Tipo _tipo, Visibilidad _vis, TipoSimbolo _tipoSimb, String _nombreClase) {
         nombre = _nombre;
         posicion_locals = _pos;
         tipo = _tipo;
         visibilidad = _vis;
         tipo_simb = _tipoSimb;
         nombreClase = _nombreClase;
     }
     
     public Tipo getTipo(){
 		return tipo;
 	}
 
 	public TipoSimbolo getTipoSimbolo(){
 		return tipo_simb;
 	}
 
 	public String getNombre(){
 		return nombre;
 	}
     
     public String getNombreClase(){
         return nombreClase;
     }
 
     public TipoLiteral getTipoFinal(){
 		TipoLiteral aux = tipo.getTipo();
 		if(aux == TipoLiteral.array){
 			return tipo.getTipoFinal();
 		} else{
 			return aux;
 		}
 	}
 	
 	public boolean esArray(){
 		return tipo.esArray();
 	}
 	
 	public int getDimension(){
 		return tipo.getDimensionTotal();
 	}
     
     @Override
     public String toString(){
         String aux = nombreClase;
         if(aux == null)
             aux = "NOCLASE";
 		return visibilidad + " " + aux + " " + nombre + ":" + tipo + " @ " + posicion_locals + " (" + tipo_simb + ")";
 	}
 	
 	@Override
     public boolean equals(Object simb){
         Simbolo s = (Simbolo)simb;
         if(tipo_simb != TipoSimbolo.metodo && tipo_simb != TipoSimbolo.constructor)
         	return (nombre == null ? s.nombre == null : nombre.equals(s.nombre));
         else{
         	if(nombre == s.nombre){
         		if(getDimension() == s.getDimension())
         			return true;
         	}
         	return false;
 
         }
     }
 }
