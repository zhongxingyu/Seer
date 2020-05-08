 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package catlite;
 
 import CoreMDELite.GProlog;
 import CoreMDELite.HomePath;
 
 /**
  *
  * @author dsb
  */
 public class SDB extends GProlog {
 
     @Override
     public String fileType() {
         return ".sdb.pl";
     }
 
     @Override
     public String partialFileType() {
         return ".sdb";
     }
 
     public SDB(String filename) {
         super(filename);
     }
 
     public SDB(String filename, String[] files) {
         super(filename, files);
     }
 
     /**
      * **************** methods/transformations of SDB objects ***********
      */
     public SDB project(String outputFilename, String plfile) {
         String[] list = {HomePath.homePath + "libpl/discontiguous.pl",
             this.fullName, HomePath.homePath + "libpl/sdb.schema.pl", plfile, HomePath.homePath + "libpl/print.pl"};
         SDB tmp = new SDB("tmp", list);
         SDB result = new SDB(outputFilename);
         tmp.executeProlog(result);
         tmp.delete();
         // remember: don't test conformance as it won't pass
         // reason: the result of this operatin is some part of an SDB database
         return result;
     }
 
     public SDB scalePosition(String extra) {
         String[] list = {HomePath.homePath + "libpl/discontiguous.pl",
                          HomePath.homePath + "libpl/print.pl", this.fullName, HomePath.homePath + "libpl/scalePosition.pl"};
         SDB tmp = new SDB("tmp", list);
         SDB result = new SDB(filename + extra);
         tmp.executeProlog(result);
         tmp.delete();
         // don't test conformance as nothing will have changed
         // remember: only tuples of the position table are updated
         // so no logical constraints have been violated
         return result;
     }
     
     public Violet toVioletpl() {
         return toViolet("");
     }
 
      public Violet toViolet(String extra) {
         String[] list = {HomePath.homePath + "libpl/discontiguous.pl",
             this.fullName, HomePath.homePath + "libpl/violet.schema.pl", HomePath.homePath + "libpl/sdb2violet.pl", HomePath.homePath + "libpl/print.pl", HomePath.homePath + "libpl/violet.run.pl"};
         Violet tmp = new Violet("tmp", list);
        Violet result = new Violet(filename + extra);
         tmp.executeProlog(result);
         result.conform();  // make sure that the db conforms to umlfpl schema
         tmp.delete();
         return result;
     } 
     
     @Override
     public void conform(){
         String[] list = {HomePath.homePath+"libpl/discontiguous.pl",
              fullName,HomePath.homePath + "libpl/conform.pl",HomePath.homePath + "libpl/sdb.conform.pl"};
         SDB tmpconform = new SDB("tmpconform", list);
         tmpconform.executeProlog();
         tmpconform.delete();     
     }
 }
