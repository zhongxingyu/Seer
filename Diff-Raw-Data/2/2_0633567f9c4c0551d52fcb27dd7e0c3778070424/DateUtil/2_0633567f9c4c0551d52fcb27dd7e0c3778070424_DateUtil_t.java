 package br.com.sw2.gac.util;
 
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 
 /**
  * <b>Descrição: Classe utilitária para manipulação de datas.</b> <br>
  * .
  * @author: SW2
  * @version 1.0 Copyright 2012 SmartAngel.
  */
 public abstract class DateUtil {
 
     /**
      * Nome: getDataAtual Recupera o valor do atributo 'dataAtual'.
      * @return valor do atributo 'dataAtual'
      * @see
      */
     public static Date getDataAtual() {
         return new Date();
     }
 
     /**
      * Nome: getAnoAtual Recupera o valor do atributo 'anoAtual'.
      * @return valor do atributo 'anoAtual'
      * @see
      */
     public static int getAnoAtual() {
         Calendar data = new GregorianCalendar();
         return data.get(Calendar.YEAR);
     }
 
     /**
      * Nome: compareIgnoreTime Compara duas datas ignorando a hora, minuto e segundo.
      * @param date1 the date1
      * @param date2 the date2
      * @return int
      * @see
      */
     public static int compareIgnoreTime(Date date1, Date date2) {
         SimpleDateFormat sdf = new SimpleDateFormat("yyyMMdd");
         int retorno = 0;
         int strd1 = Integer.parseInt(sdf.format(date1));
         int strd2 = Integer.parseInt(sdf.format(date2));
 
         if (strd1 > strd2) {
             retorno = 1;
         } else if (strd1 < strd2) {
             retorno = -1;
         }
 
         return retorno;
     }
 
     /**
      * Nome: Retorna um objeto java.util.date com as valores informados.
      * @param ano the ano
      * @param mes the mes
      * @param dia the dia
      * @return valor do atributo 'date'
      * @see
      */
     public static Date getDate(int ano, int mes, int dia) {
         Calendar data = new GregorianCalendar();
        data.set(ano, mes - 1, dia);
         return data.getTime();
     }
 
     /**
      * Nome: getDia Retorna o dia do mês de um objeto java.util.date informado.
      * @param data the data
      * @return valor do atributo 'dia'
      * @see
      */
     public static int getDia(Date data) {
         int retorno = 0;
         if (null != data) {
             Calendar calendar = new GregorianCalendar();
             calendar.setTime(data);
             retorno = calendar.get(Calendar.DAY_OF_MONTH);
         }
         return retorno;
     }
 
     /**
      * Nome: getDia getDia Retorna o dia do mês de um objeto java.util.date informado.
      * @param object the object
      * @return valor do atributo 'dia'
      * @see
      */
     public static int getDia(Object object) {
         int retorno = 0;
         if (null != object && object instanceof Date) {
             retorno = getDia((Date) object);
         }
         return retorno;
     }
 
     /**
      * Nome: getPrimeiroDiaMes Retorna um objeto java.util.Date contendo o primeiro dia do mês informado
      * através de um java.util.date.
      * @param data the data
      * @return valor do atributo 'primeiroDiaMes'
      * @see
      */
     public static Date getPrimeiroDiaMes(Date data) {
         Date retorno = null;
         if (null != data) {
             Calendar calendar = new GregorianCalendar();
             calendar.setTime(data);
             retorno = getDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                     calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
         }
         return retorno;
     }
 
     /**
      * Nome: getUltimoDiaMes Retorna um objeto java.util.Date contendo o ultimo dia do mês informado
      * através de um java.util.date.
      * @param data the data
      * @return valor do atributo 'primeiroDiaMes'
      * @see
      */
     public static Date getUltimoDiaMes(Date data) {
         Date retorno = null;
         if (null != data) {
             Calendar calendar = new GregorianCalendar();
             calendar.setTime(data);
             retorno = getDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                     calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
         }
         return retorno;
     }
 
 }
