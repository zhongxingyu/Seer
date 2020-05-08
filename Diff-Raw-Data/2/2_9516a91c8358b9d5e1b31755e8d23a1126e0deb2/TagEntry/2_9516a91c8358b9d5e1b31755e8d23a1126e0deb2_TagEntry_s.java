 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package MessageClasses;
 
 /**
  * Tag entry object. 
  * Contains name of the tag and indexes where this tag is present.
  * @author Stanislav Nepochatov <spoilt.exile@gmail.com>
  */
 public class TagEntry {
         
     /**
      * Name of the tag
      */
     public String NAME;
 
     /**
      * Indexes of messages which contains this tag
      */
     public java.util.ArrayList<String> INDEXES;
 
     /**
      * Default costructor
      * @param givenName name of new created tag
      */
     public TagEntry(String givenName) {
         NAME = givenName;
         INDEXES = new java.util.ArrayList<String>();
     }
 
     /**
      * Return csv form of tag
      * @return csv line with tag name and it's index
      */
     public String toCsv() {
        return this.NAME + Generic.CsvFormat.renderGroup(this.INDEXES.toArray(new String[this.INDEXES.size()]));
     }
 }
