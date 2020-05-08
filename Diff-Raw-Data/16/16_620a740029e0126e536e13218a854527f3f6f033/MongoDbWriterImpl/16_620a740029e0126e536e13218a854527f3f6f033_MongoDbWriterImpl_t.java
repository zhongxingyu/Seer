 package biz.c24.io.fixml.sample.storage;
 
 import biz.c24.io.api.data.ComplexDataObject;
 import biz.c24.io.api.presentation.JsonSink;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBCollection;
 import com.mongodb.MongoException;
 import com.mongodb.util.JSON;
 import com.mongodb.util.JSONParseException;
 
 import java.io.IOException;
 import java.io.StringWriter;
 
 public class MongoDbWriterImpl<T extends ComplexDataObject> implements MongoDbWriter<T> {
 
     private final JsonSink sink;
     private final DBCollection mongoDBCollection;
 
     public MongoDbWriterImpl(final DBCollection mongoDBCollection, final JsonSink sink) {
         this.mongoDBCollection = mongoDBCollection;
         this.sink = sink;
     }
 
     public T store(final T complexDataObject) {
         System.out.println("Storing...");
         try {
             StringWriter writer = new StringWriter();
             sink.setWriter(writer);
             sink.writeObject(complexDataObject);
             mongoDBCollection.save((BasicDBObject) JSON.parse(writer.toString()));
             return complexDataObject;
         } catch (JSONParseException e) {
             throw new IllegalStateException(e.getMessage(), e);
         } catch (MongoException.Network e) {
             throw new IllegalStateException("mongoDB not accessible - ensure that it's running and available for service.");
         } catch (IOException e) {
             throw new IllegalStateException(e.getMessage(), e);
         }
     }
 }
