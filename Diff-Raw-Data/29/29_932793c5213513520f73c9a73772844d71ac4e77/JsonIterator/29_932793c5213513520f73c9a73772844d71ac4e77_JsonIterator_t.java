 package org.meeuw.json;
 
 import com.fasterxml.jackson.core.JsonParseException;
 import com.fasterxml.jackson.core.JsonParser;
 import com.fasterxml.jackson.core.JsonToken;
 
 import java.io.IOException;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 
 /**
  * @author Michiel Meeuwissen
  * @since 0.4
  */
 public class JsonIterator implements Iterator<ParseEvent> {
     int depth = 0;
     final Path path = new Path();
     ParseEvent next;
     final JsonParser jp;
 
     public JsonIterator(JsonParser jp) {
         this.jp = jp;
     }
 
     @Override
     public ParseEvent next() {
         findNext();
         if (next == null) throw new NoSuchElementException();
         ParseEvent result = next;
         next = null;
         return result;
     }
 
     @Override
     public boolean hasNext() {
         findNext();
         return next != null;
     }
 
     @Override
     public void remove() {
         throw new UnsupportedOperationException();
     }
 
     protected void findNext() {
         if (next == null) {
             try {
                 while (next == null) {
                     JsonToken token = jp.nextToken();
                     if (token == null) {
                         break;
                     }
                     switch (token) {
                         case START_OBJECT:
                             depth++;
                             break;
                         case START_ARRAY:
                             depth++;
                             path.add(new ArrayEntry());
                             break;
                         case END_OBJECT: {
                             depth--;
                             PathEntry prev = path.peekLast();
                             if (prev != null) {
                                 if (prev instanceof ArrayEntry) {
                                    path.addLast(((ArrayEntry) path.pollLast()).inc());
                                 } else {
                                     path.removeLast();
                                 }
                             }
                             break;
                         }
                         case END_ARRAY: {
                             PathEntry prev = path.pollLast();
                             depth--;
                             break;
                         }
                         case FIELD_NAME:
                             String fieldName = jp.getText();
                             path.add(new KeyEntry(fieldName));
                             break;
                     }
 
                     next = new ParseEvent(token, new Path(path), jp.getText());
 
                     switch (token) {
                         case VALUE_STRING:
                         case VALUE_NUMBER_INT:
                         case VALUE_NUMBER_FLOAT:
                         case VALUE_TRUE:
                         case VALUE_FALSE:
                         case VALUE_NULL:
                         case END_ARRAY:
                             PathEntry prev = path.peekLast();
                             if (prev instanceof ArrayEntry) {
                                path.addLast(((ArrayEntry) path.pollLast()).inc());
                             } else {
                                 path.removeLast();
                             }
                             break;
                     }
 
 
                 }
             } catch (JsonParseException e) {
                 throw new RuntimeException(e);
             } catch (IOException e) {
                 throw new RuntimeException(e);
             }
         }
     }
 
 }
