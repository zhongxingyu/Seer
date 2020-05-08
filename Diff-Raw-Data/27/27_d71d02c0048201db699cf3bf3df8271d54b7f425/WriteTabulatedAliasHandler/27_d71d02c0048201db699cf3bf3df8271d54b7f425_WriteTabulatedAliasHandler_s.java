 package uk.ac.susx.tag.wag;
 
 import com.google.common.collect.Lists;
 import org.supercsv.cellprocessor.CellProcessorAdaptor;
 import org.supercsv.cellprocessor.constraint.NotNull;
 import org.supercsv.cellprocessor.ift.CellProcessor;
 import org.supercsv.exception.SuperCsvCellProcessorException;
 import org.supercsv.io.CsvListWriter;
 import org.supercsv.io.ICsvListWriter;
 import org.supercsv.prefs.CsvPreference;
 import org.supercsv.util.CsvContext;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: hiam20
  * Date: 26/02/2013
  * Time: 12:03
  * To change this template use File | Settings | File Templates.
  */
public class WriteTabulatedAliasHandler implements AliasHandler {
 
 
     public enum Column {
         TYPE("type", new ParseAliasType()) {
             @Override
             public Object getValue(Alias alias) {
                 return alias.getType();
             }
         },
         SUBTYPE("subtype", new NotNull()) {
             @Override
             public Object getValue(Alias alias) {
                 return alias.getSubType();
             }
         },
         SOURCE("source", new NotNull()) {
             @Override
             public Object getValue(Alias alias) {
                 return alias.getSource();
             }
         },
         TARGET("target", new NotNull()) {
             @Override
             public Object getValue(Alias alias) {
                 return alias.getTarget();
             }
         };
 
         private final String header;
         private final CellProcessor processor;
 
         private Column(String header, CellProcessor processor) {
             this.header = header;
             this.processor = processor;
         }
 
         public String getHeader() {
             return header;
         }
 
         public CellProcessor getProcessor() {
             return processor;
         }
 
         public abstract Object getValue(Alias alias);
 
     }
 
 
     private final EnumSet<Column> selectedColumns;// = EnumSet.allOf(Column.class);
 
     private final ICsvListWriter listWriter;
 
     private WriteTabulatedAliasHandler(CsvListWriter listWriter, EnumSet<Column> selectedColumns, boolean writeHeaders)
             throws IOException {
         this.selectedColumns = selectedColumns;
         this.listWriter = listWriter;
         if (writeHeaders)
             this.listWriter.writeHeader(getHeaders());
     }
 
     public static WriteTabulatedAliasHandler newCsvInstance(Writer writer)
             throws IOException {
         return newCsvInstance(writer, EnumSet.allOf(Column.class));
     }
 
     public static WriteTabulatedAliasHandler newTsvInstance(Writer writer)
             throws IOException {
         return newTsvInstance(writer, EnumSet.allOf(Column.class));
     }
 
 
     public static WriteTabulatedAliasHandler newCsvInstance(Writer writer, EnumSet<Column> selectedColumns)
             throws IOException {
         CsvPreference csvPreferences = CsvPreference.STANDARD_PREFERENCE;
         final CsvListWriter listWriter = new CsvListWriter(writer, csvPreferences);
         return new WriteTabulatedAliasHandler(listWriter, selectedColumns, false);
     }
 
     public static WriteTabulatedAliasHandler newTsvInstance(Writer writer, EnumSet<Column> selectedColumns)
             throws IOException {
         CsvPreference csvPreferences = CsvPreference.TAB_PREFERENCE;
         final CsvListWriter listWriter = new CsvListWriter(writer, csvPreferences);
         return new WriteTabulatedAliasHandler(listWriter, selectedColumns, false);
     }
 
     @Override
     public void handle(Alias alias) {
         try {
             listWriter.write(Arrays.asList(getValues(alias)), getProcessors());
         } catch (IOException e) {
             // XXX: Do something more sensible with exceptions
             throw new RuntimeException(e);
         }
     }
 
     private Column[] getColumnsOrdered() {
         final Column[] columns = Column.values();
         final Column[] columnsOrdered = new Column[selectedColumns.size()];
         int i = 0, j = 0;
         while (j < columns.length) {
             if (selectedColumns.contains(columns[j]))
                 columnsOrdered[i++] = columns[j];
             j++;
         }
         return columnsOrdered;
     }
 
     private String[] getHeaders() {
         final Column[] columnsOrdered = getColumnsOrdered();
         final String[] headers = new String[columnsOrdered.length];
         for (int i = 0; i < headers.length; i++)
             headers[i] = columnsOrdered[i].getHeader();
         return headers;
     }
 
     private CellProcessor[] getProcessors() {
         final Column[] columnsOrdered = getColumnsOrdered();
         final CellProcessor[] processors = new CellProcessor[columnsOrdered.length];
         for (int i = 0; i < processors.length; i++)
             processors[i] = columnsOrdered[i].getProcessor();
         return processors;
     }
 
     private Object[] getValues(Alias alias) {
         final Column[] columnsOrdered = getColumnsOrdered();
         final Object[] values = new Object[columnsOrdered.length];
         for (int i = 0; i < values.length; i++)
             values[i] = columnsOrdered[i].getValue(alias);
         return values;
     }
 
 //    private static void writeWithCsvListWriter() throws Exception {
 //
 //        // create the customer Lists (CsvListWriter also accepts arrays!)
 //        final List<Object> john = Arrays.asList(new Object[]{"1", "John", "Dunbar",
 //                new GregorianCalendar(1945, Calendar.JUNE, 13).getTime(),
 //                "1600 Amphitheatre Parkway\nMountain View, CA 94043\nUnited States", null, null,
 //                "\"May the Force be with you.\" - Star Wars", "jdunbar@gmail.com", 0L});
 //
 //        final List<Object> bob = Arrays.asList(new Object[]{"2", "Bob", "Down",
 //                new GregorianCalendar(1919, Calendar.FEBRUARY, 25).getTime(),
 //                "1601 Willow Rd.\nMenlo Park, CA 94025\nUnited States", true, 0,
 //                "\"Frankly, my dear, I don't give a damn.\" - Gone With The Wind", "bobdown@hotmail.com", 123456L});
 //
 //        ICsvListWriter listWriter = null;
 //        try {
 //
 //            listWriter = new CsvListWriter(new FileWriter("target/writeWithCsvListWriter.csv"),
 //                    CsvPreference.STANDARD_PREFERENCE);
 //
 //            final CellProcessor[] processors = getProcessors();
 //            final String[] header = new String[]{"customerNo", "firstName", "lastName", "birthDate",
 //                    "mailingAddress", "married", "numberOfKids", "favouriteQuote", "email", "loyaltyPoints"};
 //
 //            // write the header
 //            listWriter.writeHeader(header);
 //
 //            // write the customer lists
 //            listWriter.write(john, processors);
 //            listWriter.write(bob, processors);
 //
 //        } finally {
 //            if (listWriter != null) {
 //                listWriter.close();
 //            }
 //        }
 //    }
 
 
     public static class ParseAliasType extends CellProcessorAdaptor {
 
         public ParseAliasType() {
             super();
         }
 
         public ParseAliasType(CellProcessor next) {
             // this constructor allows other processors to be chained after ParseAliasType
             super(next);
         }
 
         public Object execute(Object value, CsvContext context) {
 
             validateInputNotNull(value, context);  // throws an Exception if the input is null
 
            for (AliasType type : AliasType.values()){
                if (type.name().equalsIgnoreCase(value.toString())){
                     // passes the Day enum to the next processor in the chain
                     return next.execute(type, context);
                 }
             }
 
             throw new SuperCsvCellProcessorException(
                     String.format("Could not parse '%s' as a day", value), context, this);
         }
     }
 
 
 }
