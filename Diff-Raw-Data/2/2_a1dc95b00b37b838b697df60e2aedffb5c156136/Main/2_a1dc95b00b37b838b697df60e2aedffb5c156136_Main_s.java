 package org.min.packkmean;
 
 public class Main {
 	
 	public static void main(String[] args) {
 		//Cargamos el fichero Properties
 		LoadProperties loadP = new LoadProperties(args[0]);
 		loadP.load();
 		
 		//Cargamos los datos del fichero .arff
 		LoadData loadD = new LoadData(loadP.getData());
		ListaEntidades lista = loadD.CargarDatos();
 		
 		//Llamamos a k-means
 	}
 
 }
