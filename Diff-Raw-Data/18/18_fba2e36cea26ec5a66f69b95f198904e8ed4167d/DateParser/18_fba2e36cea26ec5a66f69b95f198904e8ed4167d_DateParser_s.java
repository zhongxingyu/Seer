 package br.com.hrstatus.utils;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.Locale;
 
 import org.apache.log4j.Logger;
 
 /*
  * @Author spolti - spolti@ohsnap.com.br
  */
 
 public class DateParser {
 
 	public Date parser (String data){
 		
 		SimpleDateFormat formatador = null;
 		Date date1 = null;
 		
 		if(!data.equals(null) || !data.equals("") || data != ""){
 			//Substituindo espaços duplicados
 			data = data.replace("  ", " ");
 			//Substituindo quebra de linhas
 			data = data.replace("\n", "");
 		};
 		
 		Logger.getLogger(getClass()).debug("Data recebida para parse: " + data);
 		
 		//Formatos Aceitos
 		SimpleDateFormat formato1 = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy"); // padrao Mon Mar 04 16:09:05 BRT 2013
		String patternFormato1 = "(Sun|Mon|Tue|Wed|Thu|Fri|Sat)\\s(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Out|Nov|Dec)\\s\\d{1,}\\s\\d{2}:\\d{2}:\\d{2}\\s(BRA|[A-Z]{3,})\\s\\d{4}";
 		
 		SimpleDateFormat formato2 = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy",new Locale("pt", "BR")); // Seg Mar 04 16:09:05 BRT 2013
 		String patternFormato2 = "(Dom|Seg|Ter|Qua|Qui|Sex|Sab)\\s(Jan|Fev|Mar|Abr|Mai|Jun|Jul|Ago|Set|Out|Nov|Dez)\\s\\d{1,}\\s\\d{2}:\\d{2}:\\d{2}\\s([A-Z]{3,})\\s\\d{4}";
 		
 		SimpleDateFormat formato3 = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy"); // padrao Mon Mar 04 16:08:49 2013
 		String patternFormato3 = "(Sun|Mon|Tue|Wed|Thu|Fri|Sat)\\s(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Out|Nov|Dec)\\s\\d{1,}\\s\\d{2}:\\d{2}:\\d{2}\\s\\d{4}";
 		
 		SimpleDateFormat formato4 = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy",new Locale("pt", "BR")); // padrao Seg Mar 04 16:08:49 2013
 		String patternFormato4 = "(Dom|Seg|Ter|Qua|Qui|Sex|Sab)\\s(Jan|Fev|Mar|Abr|Mai|Jun|Jul|Ago|Set|Out|Nov|Dez)\\s\\d{1,}\\s\\d{2}:\\d{2}:\\d{2}\\s\\d{4}";
 		
 		SimpleDateFormat formato5 = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy",new Locale("pt", "BR")); // padrão segunda-feira,  4 de março de 2013 16h13min27s BRT
 		
 		SimpleDateFormat formato6 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 		String patternFormato6 = "\\d{4}-\\d{2}-\\d{2}\\s\\d{2}:\\d{2}:\\d{2}";
 		//verificando se o Timezone é nativo AIX GRNLNDST e GRNLNDDT e GRNLNDST3, se for ignora timezone.
 		if (data.contains("GRNLNDST") || data.contains("GRNLNDDT") || data.contains("GRNLNDST3")){
 			Logger.getLogger(getClass()).debug("Padrao AIX reconhecido, ignorando timezone.");
 			if (data.contains("GRNLNDST")){
 				data = data.replace("GRNLNDST", "");
 			}else if(data.contains("GRNLNDDT")){
 				data = data.replace("GRNLNDDT","");
 			}else if (data.contains("GRNLNDST3")){
 				data =  data.replace("GRNLNDST3","");
 			}
 			formatador = formato3;
 
 		}else if(data.equals(null) || data.equals("") || data == ""){
 			Logger.getLogger(getClass()).error("Nenhuma data foi recebida, setando valor default.");
 			data = ("Tue Mar 01 00:00:00 BRT 1950");
 			formatador = formato1;
 		//validando primeiro padrão
 		}else if(data.matches(patternFormato1)){
 			Logger.getLogger(getClass()).debug("Padrão reconhecido EEE MMM dd HH:mm:ss z yyyy");
 			data = data.replace("BRA", "BRT");
 			formatador = formato1;
 		//validando segundo padrão	
 		}else if (data.matches(patternFormato2)){
 			Logger.getLogger(getClass()).debug("Padrão reconhecido EEE MMM dd HH:mm:ss z yyyy Locale pt_BR");
 			formatador = formato2;
 		//validando terceiro padrão
 		}else if (data.matches(patternFormato3)){
 			Logger.getLogger(getClass()).debug("Padrão reconhecido EEE MMM dd HH:mm:ss yyyy ");
 			formatador = formato3;
 		//validando quarto padrão
 		}else if (data.matches(patternFormato4)){
 			Logger.getLogger(getClass()).debug("Padrão reconhecido EEE MMM dd HH:mm:ss yyyy Locale pt_BR");
 			formatador = formato4;
 		//validando quinto padrão
 		}else if (data.toLowerCase().startsWith("domingo") || data.toLowerCase().startsWith("segunda")
 				|| data.toLowerCase().startsWith("terça") || data.toLowerCase().startsWith("terca")
 				|| data.toLowerCase().startsWith("quarta") || data.toLowerCase().startsWith("quinta")
 				|| data.toLowerCase().startsWith("sexta") || data.toLowerCase().startsWith("sabado")
 				|| data.toLowerCase().startsWith("sábado")) {
 			
 			Logger.getLogger(getClass()).info("Padrão reconhecido: Data completa por extenso");
 			
 			String dataPorExtenso[] = data.split(" ");
 			
 			//Obtendo dia da semana
 			String diaSemana = null;
 			if (dataPorExtenso[0].endsWith(",")){
 				dataPorExtenso[0] = dataPorExtenso[0].substring(0,dataPorExtenso[0].length() -1); 
 			}
 			String diaSemanaVal = dataPorExtenso[0].toLowerCase();
 			
 			if (diaSemanaVal.equals("domingo")){
 				diaSemana = "Dom";
 			}else if(diaSemanaVal.equals("segunda-feira")){
 				diaSemana = "Seg";
 			}else if(diaSemanaVal.equals("segunda")){
 				diaSemana = "Seg";
 			}else if(diaSemanaVal.equals("terça-feira")){
 				diaSemana = "Ter";
 			}else if(diaSemanaVal.equals("terça")){
 				diaSemana = "Ter";
 			}else if(diaSemanaVal.equals("terca-feira")){
 				diaSemana = "Ter";
 			}else if(diaSemanaVal.equals("terca")){
 				diaSemana = "Ter";
 			}else if(diaSemanaVal.equals("quarta-feira")){
 				diaSemana = "Qua";
 			}else if(diaSemanaVal.equals("quarta")){
 				diaSemana = "Qua";
 			}else if(diaSemanaVal.equals("quinta-feira")){
 				diaSemana = "Qui";
 			}else if(diaSemanaVal.equals("quinta")){
 				diaSemana = "Qui";
 			}else if(diaSemanaVal.equals("sexta-feira")){
 				diaSemana = "Sex";
 			}else if(diaSemanaVal.equals("sexta")){
 				diaSemana = "Sex";
 			}else if(diaSemanaVal.equals("sabado")){
 				diaSemana = "Sab";
 			}else if(diaSemanaVal.equals("sábado")){
 				diaSemana = "Sab";
 			}
 			
 			//Obtendo dia do mês
 			String diaMes = dataPorExtenso[1];
 			
 			//obtendo mes do ano
 			String mesAno = null;
 			String mesAnoVal = dataPorExtenso[3].toLowerCase();
 			
 			if (mesAnoVal.equals("janeiro")){
 				mesAno = "Jan";
 			}else if(mesAnoVal.equals("fevereiro")){
 				mesAno = "Fev";
 			}else if(mesAnoVal.equals("março")){
 				mesAno = "Mar";
 			}else if(mesAnoVal.equals("marco")){
 				mesAno = "Mar";
 			}else if(mesAnoVal.equals("abril")){
 				mesAno = "Abr";
 			}else if(mesAnoVal.equals("maio")){
 				mesAno = "Mai";
 			}else if(mesAnoVal.equals("junho")){
 				mesAno = "Jun";
 			}else if(mesAnoVal.equals("julho")){
 				mesAno = "Jul";
 			}else if(mesAnoVal.equals("agosto")){
 				mesAno = "Ago";
 			}else if(mesAnoVal.equals("setembro")){
 				mesAno = "Set";
 			}else if(mesAnoVal.equals("outubro")){
 				mesAno = "Out";
 			}else if(mesAnoVal.equals("novembro")){
 				mesAno = "Nov";
 			}else if(mesAnoVal.equals("dezembro")){
 				mesAno = "Dez";
 			}
 			
 			//obtendo hora
 			String[] horaTemp = dataPorExtenso[6].split("h");
 			String hora = horaTemp[0];
 			
 			//obtendo minuto
 			String[] minF = dataPorExtenso[6].split("min");
 			String minTemp[] = minF[0].split("h");
 			String min = minTemp[1];
 			
 			//obtendo segundo
 			String seg = minF[1].replace("s", "");
 			
 			//concatenando a hora completa
 			String horaFinal = hora + ":" + min + ":" + seg;
 			
 			//Obtendo ano
 			String ano = dataPorExtenso[5];
 			
 			//obtendo Locale
 			String locale = dataPorExtenso[7];
 			
 			//Concatenando data no padrão EEE MMM dd HH:mm:ss yyyy Locale pt_BR"
 			String dataFinal = diaSemana + " " + mesAno + " " + diaMes + " " + horaFinal + " " + locale + " " + ano;
 			data = dataFinal;
 			formatador = formato5;
 	
 		//validando padrão 6
 		}else if (data.matches(patternFormato6)){
 			Logger.getLogger(getClass()).debug("Padrão reconhecido yyy-dd-mm HH:mm:ss");
 			System.out.println("Padrão reconhecido yyy-dd-mm HH:mm:ss");
 			formatador = formato6;
 		}
 		
 		else{
 			Logger.getLogger(getClass()).error("Nenhum padrão reconhecido");
 			Logger.getLogger(getClass()).error("Unparsable date: " + data);
 			data = ("Tue Mar 01 00:00:00 BRT 1950");
 			formatador = formato1;
 		}
 		try {
 			date1 = formatador.parse(data);
 		} catch (ParseException e) {	
 			e.printStackTrace();
 		}
 		return date1;
 	}
 	
	public static void main(String args[]){
		
		DateParser test = new DateParser();
		Date NewFormat = test.parser("");
		
		System.out.println("Data formadata: " + NewFormat);			
	}	
 }
