 /*
  * RequestWrapper.java criado em 27/09/2011
  * 
  * Propriedade de Objectos Fábrica de Software LTDA.
  * Reprodução parcial ou total proibida.
  */
 package br.com.objectos.comuns.sitebricks;
 
 import org.joda.time.LocalDate;
 
 import br.com.objectos.comuns.matematica.financeira.Percentual;
 import br.com.objectos.comuns.relational.search.Page;
 import br.com.objectos.comuns.sitebricks.relational.SearchString;
 
 /**
  * @author marcio.endo@objectos.com.br (Marcio Endo)
  */
 public interface RequestWrapper {
 
   boolean booleanParam(String param);

   <E extends Enum<E>> E enumParam(Class<E> enumClass, String param);
   String param(String param);
 
   LocalDate localDateParam(String param);
   LocalDate localDateParam(LocalDateFormat format, String param);
 
   Double doubleParam(String param);
   Integer integerParam(String param);
   Long longParam(String param);
 
   Percentual percentualParam(String param);
 
   Page getPage();
 
   SearchString getSearchString(String param);
 
 }
