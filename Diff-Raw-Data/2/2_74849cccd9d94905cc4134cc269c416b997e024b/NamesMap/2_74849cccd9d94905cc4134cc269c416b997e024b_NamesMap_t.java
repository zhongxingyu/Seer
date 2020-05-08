 package util;
 
 import java.util.HashMap;
 
 public class NamesMap {
 
 	public static final int DEPUTADO = 0;
 	public static final int SENADOR = 1;
 
 	private static HashMap<String, String> namesDeputados = new HashMap<String, String>();
 	private static HashMap<String, String> namesSenadores = new HashMap<String, String>();
 	
 	//initialization
 	static {
 		namesDeputados.put("ASSINATURA DE PUBLICAÇÕES","Assinatura de publicações");
 		namesDeputados.put("COMBUSTÍVEIS E LUBRIFICANTES","Combustível");
 		namesDeputados.put("CONSULTORIAS, PESQUISAS E TRABALHOS TÉCNICOS","Consultorias");
 		namesDeputados.put("DIVULGAÇÃO DA ATIVIDADE PARLAMENTAR","Divulgação");
 		namesDeputados.put("EMISSÃO DE BILHETE AÉREO","Passagens aéreas");
 		namesDeputados.put("FORNECIMENTO DE ALIMENTAÇÃO DO PARLAMENTAR","Alimentação");
 		namesDeputados.put("HOSPEDAGEM ,EXCETO DO PARLAMENTAR NO DISTRITO FEDERAL.","Hospedagem");
 		namesDeputados.put("LOCAÇÃO DE VEÍCULOS AUTOMOTORES OU FRETAMENTO DE EMBARCAÇÕES","Locação de veículos");
 		namesDeputados.put("MANUTENÇÃO DE ESCRITÓRIO DE APOIO A ATIVIDADE PARLAMENTAR","Manutenção de escritório");
 		namesDeputados.put("PASSAGENS AÉREAS E FRETAMENTO DE AERONAVES","Passagens aéreas e fretamentos");
		namesDeputados.put("SERVIÇO DE SEGURANÇA PRESTADO POR EMPRESA ESPECIALIZADA","Segurança");
 		namesDeputados.put("SERVIÇOS POSTAIS","Serviços postais");
 		namesDeputados.put("TELEFONIA","Telefonia");
 		
 		namesSenadores.put("Aluguel de imóveis para escritório político, compreendendo despesas concernentes a eles","Imóveis");
 		namesSenadores.put("Aquisição de material de consumo para uso no escritório político, inclusive aquisição ou locação de software, despesas postais, aquisição de publicações, locação de móveis e de equipamentos","Material de Consumo");
 		namesSenadores.put("Contratação de consultorias, assessorias, pesquisas, trabalhos técnicos e outros serviços de apoio ao exercício do mandato parlamentar","Consultorias");
 		namesSenadores.put("Divulgação da atividade parlamentar","Divulgação");
 		namesSenadores.put("Locomoção, hospedagem, alimentação, combustíveis e lubrificantes","Combustível e Hospedagem");
 		namesSenadores.put("Passagens aéreas, aquáticas e terrestes nacionais","Passagens");
 		namesSenadores.put("Serviços de Segurança Privada","Segurança");
 	}
 	
 	public static String getShortName(int nameType, String name){
 		if (nameType == DEPUTADO){
 			return namesDeputados.get(name);
 		} else if (nameType == SENADOR){
 			return namesSenadores.get(name);
 		} else {
 			return null;
 		}
 	}
 	
 }
 
 
