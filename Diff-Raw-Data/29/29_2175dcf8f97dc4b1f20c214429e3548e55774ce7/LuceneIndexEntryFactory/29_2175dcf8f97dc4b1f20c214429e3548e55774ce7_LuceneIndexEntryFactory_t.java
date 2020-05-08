 package pt.inevo.encontra.lucene.index;
 
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.document.Fieldable;
 import pt.inevo.encontra.index.IndexEntryFactory;
 import pt.inevo.encontra.storage.Entry;
 import pt.inevo.encontra.storage.IEntry;
 
 import java.util.List;
 
 public class LuceneIndexEntryFactory<O extends IEntry> extends IndexEntryFactory<O, LuceneIndexEntry> {
 
     public LuceneIndexEntryFactory(Class indexedObjectClass) {
         super(LuceneIndexEntry.class, indexedObjectClass);
     }
 
     @Override
     protected LuceneIndexEntry setupIndexEntry(O object, LuceneIndexEntry entry) {
 
         Document doc = new Document();
         int i = 0;
         Object val = object.getValue();
         if (val instanceof IEntry[]) {
             IEntry[] objects = (IEntry[]) val;
             for (IEntry obj : objects) {
                 doc.add(new Field(obj.getId().toString(), obj.getValue().toString(), Field.Store.YES, Field.Index.NO));
             }
         } else {
             doc.add(new Field("VALUE", val.toString(), Field.Store.YES, Field.Index.NO));
         }
 
         doc.add(new Field("IDENTIFIER", object.getId().toString(), Field.Store.YES, Field.Index.NO));
         entry.setKey(object.getId().toString());
         entry.setValue(doc);
 
         return entry;
     }
 
     @Override
     protected O setupObject(LuceneIndexEntry entry, O object) {
         Document doc = entry.getValue();
         List<Fieldable> fields = doc.getFields();
 
         Fieldable idField = null;
         // EXTRACT ID
         for (Fieldable field : fields) {
             if (field.name().equals("IDENTIFIER")) {
                 object.setId(field.stringValue());
                 idField = field;
 
             }
         }
 
         // Remove this field
         fields.remove(idField);
 
         if (fields.size() == 1) { // Simple value
            if (fields.get(0) instanceof IEntry) {
                IEntry value = new Entry(fields.get(0).name(), fields.get(0).stringValue());
                object.setValue(value);
            } else {
                object.setValue(fields.get(0).stringValue());
            }

         } else {
             IEntry[] values = new IEntry[fields.size()];
             int i = 0;
             for (Fieldable field : fields) {
                 values[i++] = new Entry(field.name(), field.stringValue());
             }
             object.setValue(values);
         }
 
         return object;
     }
 }
