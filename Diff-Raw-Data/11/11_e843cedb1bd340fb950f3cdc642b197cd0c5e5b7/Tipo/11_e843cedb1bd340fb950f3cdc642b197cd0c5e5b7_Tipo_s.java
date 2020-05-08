 
 import java.util.ArrayList;
 
 
 
 /**
  *
  * @author victor
  */
 public class Tipo {
     
     TipoLiteral tipo;
     int dimension;
     Tipo tipobase;
     tablaSimbolos tS;
     int numParam;
     boolean array;
     boolean indice;
     
     public Tipo(TipoLiteral _tipo){
 
         tipo = _tipo;
         dimension = 1;
         tipobase = null;
         tS = null;
         array = false;
         indice = false;
 		
     }
 
     public Tipo(TipoLiteral _tipo, int _dimension){
     	tipo = _tipo;
     	dimension = _dimension;
     	tipobase = null;
     	tS = null;
     	array = false;
     	indice = false;
     }
     
     public Tipo(TipoLiteral _tipo, boolean _indice){
         tipo = _tipo;
         dimension = 1;
         tipobase = null;
         indice = true;
     }
     
     
     public Tipo(TipoLiteral _tipo, Tipo _tipobase){
         tipo = TipoLiteral.array;
         dimension = -1;
         tipobase = _tipobase;
         array = true;
     }
     
     public Tipo(Tipo _anterior){
         tipo = _anterior.tipo;
         dimension = _anterior.dimension;
         tipobase = _anterior.tipobase;
     }
     
     public TipoLiteral getTipo(){
 		return tipo;
 	} 
 	
	public boolean isIndice(){
 		return indice;
 	}
 
 	public boolean esArray(){
 		return(tipo == TipoLiteral.array);
 	}
 	
     public Tipo getTipoBase(){
 		return tipobase;
 	}
 	
 	public TipoLiteral getTipoFinal(){
 		if(array){
 			return tipobase.getTipoFinal();
 		} else{
 			return tipo;
 		}
 	}
 	
     public int getDimension(){
 		return dimension;
 	}
 	
     public int getDimensionTotal(){
 		if(array){
 			return dimension*tipobase.getDimensionTotal();
 		}else{
 			return dimension;
 		}
 	}
 	
     public void setDimension(int _dim) throws Error_8{
 		if(_dim > 0){
 			dimension = _dim;
 		}else{
 			throw new Error_8(0,0);
 		}
 	}
     public void setDimension(String _dim) throws Error_8{
 		setDimension(Integer.parseInt(_dim));
 	}
 	
 	@Override
 	public String toString(){
 		if (array){
 			return tipobase.toStringSoloTipo() + "[]";
 			//return "[] -> " + tipobase.toString();
 		}else{
 			return tipo.toString();
 		}
 		
 	}
 	
 	private String toStringSoloTipo(){
 		if (array){
 			return tipobase.toStringSoloTipo();
 		}else{
 			return tipo.toString();
 		}
 		
 	}
 	
 }
