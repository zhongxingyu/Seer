 package br.com.locadora.principal;
 
 import java.io.ObjectInputStream.GetField;
 import java.util.HashMap;
 
 import br.com.locadora.controller.ControllerAluguel;
 import br.com.locadora.controller.ControllerCliente;
 import br.com.locadora.controller.ControllerDVD;
 
 public class Main {
 
 
 	public static void main(String[] args) {
 		
 		HashMap<String, String> parameters = parseArrayToHashMap(args);
 		
 		if(parameters.containsKey("comando")){			
 			
 			if(parameters.get("comando").equalsIgnoreCase("consultaDVD")){
 				ControllerDVD.listaDVDs(parameters);
 			}
 			//Visualizar os detalhes do DVD
 			//-comando selecionaDVD -c <cdigo do DVD>
 			else if(parameters.get("comando").equalsIgnoreCase("selecionaDVD")){
 				ControllerDVD.realizarBusca(parameters.get("c"));
 			}
 			//Alugar DVD
 			//-comando alugarDVD -d <cdigo do DVD> -c <cdigo do Cliente> -t <data do aluguel (Formato DD/MM/YYYY)> -v <valor pago>
 			else if(parameters.get("comando").equalsIgnoreCase("alugarDVD")){
 				//verificar parametros obrigatrios
 				ControllerAluguel.realizaAluguel(parameters);
 			}
 			//Devolver DVD
 			//-comando devolverDVD -d <cdigo do DVD> -c <cdigo do Cliente> -t <data do aluguel (Formato DD/MM/YYYY)> -v <valor pago>
 			else if(parameters.get("comando").equalsIgnoreCase("devolverDVD")){
 				ControllerAluguel.realizarDevolucao(parameters);
 			}
 			//Consulta de Cliente
 			//-comando consultaCliente -c <cdigo do cliente>
 			else if(parameters.get("comando").equalsIgnoreCase("consultaCliente")){
 				ControllerCliente.realizarBusca(parameters.get("c"));
 			}
 			
 		}
 		
 	}
 
 	public static HashMap<String, String> parseArrayToHashMap(String[] param){
 		HashMap<String, String> hashmap = new HashMap<String, String>(); 
 		StringBuilder allParams = new StringBuilder();
 		
 		for(String p : param){
 			allParams.append(p).append(":");
 		}
 		
 		String[] allParamsArray = allParams.toString().split("-");
 		
 		for(String str : allParamsArray){		
 			if(str.split(":").length >= 2){
				hashmap.put(str.split(":")[0], str.split(":")[1]);
 			}
 		}
 		
 		return hashmap;
 	}
 }
 
