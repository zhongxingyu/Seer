 /*
  * Copyright 2011 Objectos, Fábrica de Software LTDA.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not
  * use this file except in compliance with the License. You may obtain a copy of
  * the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
  * License for the specific language governing permissions and limitations under
  * the License.
  */
 package br.com.objectos.comuns.io.xls;
 
 import static com.google.common.collect.Lists.transform;
 
 import java.util.List;
 import java.util.Map;
 
 import org.apache.poi.ss.usermodel.Cell;
 import org.apache.poi.ss.usermodel.Row;
 
 import br.com.objectos.comuns.io.Column;
 import br.com.objectos.comuns.io.ColumnKey;
 import br.com.objectos.comuns.io.Line;
 
 import com.google.common.base.Function;
 import com.google.common.collect.ImmutableList;
 
 /**
  * @author marcio.endo@objectos.com.br (Marcio Endo)
  */
 class XlsLine implements Line {
 
   private final Map<ColumnKey<?>, XlsConverter<?>> converterMap;
 
   private final int number;
 
   private final Row row;
 
   public XlsLine(Map<ColumnKey<?>, XlsConverter<?>> converterMap, int number, Row row) {
     this.converterMap = converterMap;
     this.number = number;
     this.row = row;
   }
 
   @Override
   public int getNumber() {
     return number;
   }
 
   @Override
   public Column column(int indice) {
    Cell cell = row.getCell(indice);
     return new XlsColumn(converterMap, cell);
   }
 
   @Override
   public String getText() {
     List<Cell> cells = ImmutableList.copyOf(row);
     List<String> strings = transform(cells, new CellToString());
     return strings.toString();
   }
 
   @Override
   public String toString() {
     return String.format("%05d: %s", number, getText());
   }
 
   private static class CellToString implements Function<Cell, String> {
     @Override
     public String apply(Cell input) {
       String result = null;
 
       if (input != null) {
         result = input.toString();
         result = result.trim();
       }
 
       return result;
     }
   }
 
 }
