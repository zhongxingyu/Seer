 /**
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package edu.dfci.cccb.mev.dataset.rest.assembly.json;
 
 import static edu.dfci.cccb.mev.dataset.domain.contract.Dimension.Type.COLUMN;
 import static edu.dfci.cccb.mev.dataset.domain.contract.Dimension.Type.ROW;
 import static java.lang.Double.MAX_VALUE;
 import static java.lang.Double.NaN;
 
 import java.io.IOException;
 import java.util.List;
 
 import lombok.SneakyThrows;
 import lombok.ToString;
 
 import com.fasterxml.jackson.core.JsonGenerator;
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.JsonSerializer;
 import com.fasterxml.jackson.databind.SerializerProvider;
 
 import edu.dfci.cccb.mev.dataset.domain.contract.Dataset;
 import edu.dfci.cccb.mev.dataset.domain.contract.Dimension;
 import edu.dfci.cccb.mev.dataset.domain.contract.InvalidCoordinateException;
 import edu.dfci.cccb.mev.dataset.domain.contract.InvalidDimensionTypeException;
 import edu.dfci.cccb.mev.dataset.domain.contract.Values;
 
 /**
  * @author levk
  * 
  */
 @ToString
 public class DatasetJsonSerializer extends JsonSerializer<Dataset> {
 
   /* (non-Javadoc)
    * @see com.fasterxml.jackson.databind.JsonSerializer#handledType() */
   @Override
   public Class<Dataset> handledType () {
     return Dataset.class;
   }
 
   /* (non-Javadoc)
    * @see
    * com.fasterxml.jackson.databind.JsonSerializer#serialize(java.lang.Object,
    * com.fasterxml.jackson.core.JsonGenerator,
    * com.fasterxml.jackson.databind.SerializerProvider) */
   @Override
   @SneakyThrows ({ InvalidDimensionTypeException.class, InvalidCoordinateException.class })
   public void serialize (Dataset value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
                                                                                         JsonProcessingException {
     jgen.writeStartObject ();
     writeDimension (jgen, value.dimension (ROW));
     writeDimension (jgen, value.dimension (COLUMN));
     writeValues (jgen, value.values (), value.dimension (ROW).keys (), value.dimension (COLUMN).keys ());
     jgen.writeEndObject ();
   }
 
   private void writeDimension (JsonGenerator jgen, Dimension dimension) throws IOException, JsonProcessingException {
     jgen.writeArrayFieldStart (dimension.type ().name ().toLowerCase ());
     for (String item : dimension.keys ())
       jgen.writeString (item);
     jgen.writeEndArray ();
   }
 
   private void writeValues (JsonGenerator jgen, Values values, List<String> rows, List<String> columns) throws IOException,
                                                                                                        JsonProcessingException,
                                                                                                        InvalidCoordinateException {
     double min = MAX_VALUE, max = -MAX_VALUE, sum = .0;
     int count = 0;
     jgen.writeArrayFieldStart ("values");
     for (String row : rows)
       for (String column : columns) {
         double value = values.get (row, column);
         min = min > value ? value : min;
         max = max < value ? value : max;
         sum += value;
         count++;
         jgen.writeStartObject ();
         jgen.writeStringField ("row", row);
         jgen.writeStringField ("column", column);
        jgen.writeNumberField ("value", values.get (row, column));
         jgen.writeEndObject ();
       }
     jgen.writeEndArray ();
     jgen.writeNumberField ("min", min);
     jgen.writeNumberField ("max", max);
     jgen.writeNumberField ("avg", count == 0 ? NaN : (sum / count));
   }
 }
