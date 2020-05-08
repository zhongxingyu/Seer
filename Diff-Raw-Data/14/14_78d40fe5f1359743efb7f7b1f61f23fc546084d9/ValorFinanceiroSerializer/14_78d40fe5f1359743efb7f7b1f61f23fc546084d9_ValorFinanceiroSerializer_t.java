 /*
  * ValorFinanceiroDeserializer.java criado em 30/09/2011
  * 
  * Propriedade de Objectos Fábrica de Software LTDA.
  * Reprodução parcial ou total proibida.
  */
 package br.com.objectos.comuns.sitebricks.json;
 
 import java.io.IOException;
 
 import org.codehaus.jackson.JsonGenerator;
 import org.codehaus.jackson.JsonProcessingException;
 import org.codehaus.jackson.map.JsonSerializer;
 import org.codehaus.jackson.map.SerializerProvider;
 
 import br.com.objectos.comuns.matematica.financeira.ValorFinanceiro;
 
 /**
  * @author marcio.endo@objectos.com.br (Marcio Endo)
  */
 public class ValorFinanceiroSerializer extends JsonSerializer<ValorFinanceiro> {
 
   @Override
   public void serialize(ValorFinanceiro value, JsonGenerator jgen, SerializerProvider provider)
       throws IOException, JsonProcessingException {
 
     double doubleValue = value.doubleValue();
     String text = value.toString();
 
     jgen.writeStartObject();
    jgen.writeBooleanField("zero", value.isZero());
     jgen.writeObjectField("doubleValue", doubleValue);
     jgen.writeObjectField("text", text);
     jgen.writeEndObject();
 
   }
 
 }
