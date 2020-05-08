 /*
  * FakeRequestWrapper.java criado em 27/09/2011
  * 
  * Propriedade de Objectos Fábrica de Software LTDA.
  * Reprodução parcial ou total proibida.
  */
 package br.com.objectos.comuns.sitebricks;
 
 import static com.google.common.collect.Maps.newHashMap;
 
 import java.util.Map;
 
 import org.joda.time.LocalDate;
 
 import br.com.objectos.comuns.matematica.financeira.Percentual;
 import br.com.objectos.comuns.relational.search.Page;
 import br.com.objectos.comuns.relational.search.SimplePage;
 import br.com.objectos.comuns.sitebricks.relational.SearchString;
 
 /**
  * @author marcio.endo@objectos.com.br (Marcio Endo)
  */
 public class FakeRequestWrapper implements RequestWrapper {
 
   private final Map<String, Object> params = newHashMap();
 
   public Object put(String key, Object value) {
     return params.put(key, value);
   }
 
   protected Object get(Object key) {
     return params.get(key);
   }
 
   @Override
   public boolean booleanParam(String param) {
    Boolean object = (Boolean) params.get(param);
    return object != null ? object.booleanValue() : false;
   }
   @SuppressWarnings("unchecked")
   @Override
   public <E extends Enum<E>> E enumParam(Class<E> enumClass, String param) {
     return (E) params.get(param);
   }
 
   @Override
   public String param(String param) {
     return (String) params.get(param);
   }
 
   @Override
   public LocalDate localDateParam(String param) {
     return (LocalDate) params.get(param);
   }
 
   @Override
   public LocalDate localDateParam(LocalDateFormat format, String param) {
     return localDateParam(param);
   }
 
   @Override
   public Double doubleParam(String param) {
     return (Double) params.get(param);
   }
   @Override
   public Integer integerParam(String param) {
     return (Integer) params.get(param);
   }
   @Override
   public Long longParam(String param) {
     return (Long) params.get(param);
   }
 
   @Override
   public Percentual percentualParam(String param) {
     return (Percentual) params.get(param);
   }
 
   @Override
   public Page getPage() {
     Object page = params.get("page");
 
     if (page == null) {
       page = new SimplePage();
     }
 
     return (Page) page;
   }
 
   @Override
   public SearchString getSearchString(String param) {
     String string = param(param);
     return new SearchString(string);
   }
 
 }
