 package util;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 public class UtGeral {
 	
 	public boolean isNullOrEmpty(List<?> list){
 		return list == null || list.isEmpty();
 	}
 	
 	public List<String> converteParaLista(String textoSeparadoPorVirgula){
 		List<String> retornoList = new ArrayList<String>();
 		if (textoSeparadoPorVirgula != null && textoSeparadoPorVirgula.length() > 0){			
 			String[] arrayString = textoSeparadoPorVirgula.split(",");
 			for (int c=0;c<arrayString.length;c++){
 				retornoList.add(arrayString[c]);
 			}
 		}
 		
 		return retornoList;
 	}
 	
     private static String getDataGeral(String data, int opcao_formato) throws ParseException{
     	String formato = null;
     	switch (opcao_formato){
     		case 1: 
     			formato = "dd/MM/yyyy";
     			break;
     		case 2:
     			formato = "dd/MM/yyyy '&agrave;s' HH:mm";
     			break;
     	}
     	SimpleDateFormat dataSimples =  new SimpleDateFormat(formato);
         dataSimples.setLenient(false);
         return dataSimples.parse(data).toString();
 		
     }
 	
 	public static String getData(String data) throws ParseException{  
         return getDataGeral(data, 1);
     }
     
     public static String getDataComHora(String data) throws ParseException{  
     	return getDataGeral(data, 2); 
     }
 }
