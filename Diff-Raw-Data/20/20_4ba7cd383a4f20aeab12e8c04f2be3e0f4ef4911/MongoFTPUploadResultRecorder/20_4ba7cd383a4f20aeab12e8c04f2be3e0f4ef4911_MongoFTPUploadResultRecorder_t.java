 package org.atlasapi.feeds.radioplayer.upload;
 
 import com.metabroadcast.common.persistence.mongo.DatabasedMongo;
 import com.mongodb.DBCollection;
 
 public class MongoFTPUploadResultRecorder implements RadioPlayerFTPUploadResultRecorder {
 
    private final DBCollection recordings;
    private final RadioPlayerFTPUploadResultTranslator translator;
 
     public MongoFTPUploadResultRecorder(DatabasedMongo mongo) {
         this.recordings = mongo.collection("radioplayer");
         this.translator = new RadioPlayerFTPUploadResultTranslator();
     }
     
     @Override
     public void record(RadioPlayerFTPUploadResult result) {
    	this.recordings.save(translator.toDBObject(result));
     }
 }
