 package models;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 import com.google.gson.Gson;
 
 import exceptions.BadCSVLineFormatException;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.RandomAccessFile;
 import java.io.Reader;
 import java.nio.channels.Channels;
 import java.nio.channels.FileChannel;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.PersistenceException;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 import javax.persistence.UniqueConstraint;
 
 import org.apache.commons.io.FileUtils;
 import org.hibernate.lob.ReaderInputStream;
 
 import play.Logger;
 import play.db.jpa.Model;
 import play.i18n.Messages;
 import play.modules.search.Field;
 import play.modules.search.Indexed;
 import play.modules.search.Search;
 import play.templates.JavaExtensions;
 
 @Entity
 @Table(uniqueConstraints = @UniqueConstraint(columnNames = {"referential_id", "inseeCode"}))
 @Indexed
 public class City extends Model {
 
     @ManyToOne(optional = false)
     @Field(joinField = "code")
     public Referential referential;
 
     @Column(length = 5)
     public String inseeCode;
 
     @Column(length = 5)
     @Field
     public String postalCode;
 
     @Column(length = 38)
     @Field(sortable = true)
     public String name;
 
     public City() {
     }
 
     /*
      * CSV city import
      * 0: inseeCode (= 5 chars)
      * 1: postalCode (= 5 chars)
      * 2: name (38 chars maximum)
      */
     public City(Referential referential, String[] CSVLine) throws BadCSVLineFormatException {
         this.referential = referential;
 
         inseeCode = CSVLine[0];
         name = CSVLine[1];
         postalCode = CSVLine[2];
 
         if(inseeCode == null || inseeCode.length() == 0)
             badFormat("city.inseeCode.empty");
 
         if(inseeCode.length() != 5)
             badFormat("city.inseeCode.wrongLength");
 
         if(name == null || name.length() == 0)
             badFormat("city.name.empty");
 
         if(name.length() > 38)
             badFormat("city.name.wrongLength");
 
         if(postalCode == null || postalCode.length() == 0)
             badFormat("city.postalCode.empty");
 
         if(postalCode.length() != 5)
             badFormat("city.postalCode.wrongLength");
     }
 
     public void badFormat(String messageKey) throws BadCSVLineFormatException {
         throw new BadCSVLineFormatException(messageKey, this.toJson());
     }
 
     public static void importCsv(Import currentImport) throws IOException {
         CSVReader referentialReader = new CSVReader(new FileReader(currentImport.file));
         String [] nextLine;
         for (int i = 0; (nextLine = referentialReader.readNext()) != null; i++) {
             if(i < currentImport.importLine) continue;
             if(i > 0 && (i % 100 == 0)) Import.em().flush();
             currentImport.importLine++;
             currentImport.save();
             try {
                 City city = new City(currentImport.referential, nextLine);
                 if(!City.exists(city)) {
                     city.save();
                 }
                 else {
                     currentImport.alreadyExists(city.toJson());
                 }
             } catch (BadCSVLineFormatException e) {
                 currentImport.badFormat(e.messageKey, e.jsonObject);
             }
         }
     }
 
     private static boolean exists(City city) {
         return find("referential = ? and inseeCode = ?", city.referential, city.inseeCode).first() != null;
     }
 
     public static List<City> search(String referentialCode, String search, Boolean postalCode) {
         if(referentialCode == null || referentialCode.length() == 0) {
             return new ArrayList<City>();
         }
        String queryReferential = "code:" + referentialCode;
         String cleanSearch = JavaExtensions.noAccents(search).toUpperCase().replace("'", " ").trim();
         if (cleanSearch.length() < 1) {
             return new ArrayList<City>();
         }
         if (postalCode) {
             return Search.search(queryReferential + " AND (postalCode:" + cleanSearch + " OR postalCode:" + cleanSearch + "*)", City.class).orderBy("postalCode").page(0, 10).fetch();
         } else {
             String luceneQuery = "name:\"" + cleanSearch + "\"";
             if (cleanSearch.length() > 0) {
                 String wordsTokenized = "";
                 Boolean wildcardable = false;
                 for (String word : cleanSearch.split(" ")) {
                     if (word.length() > 0) {
                         if (wordsTokenized.length() > 0) {
                             wordsTokenized += " AND ";
                         }
                         if (word.equals("SAINT")) {
                             wordsTokenized += "(name:ST OR name:SAINT)";
                             wildcardable = false;
                         } else if (word.equals("SAINTE")) {
                             wordsTokenized += "(name:STE OR name:SAINTE)";
                             wildcardable = false;
                         } else {
                             wordsTokenized += "name:" + word;
                             wildcardable = true;
                         }
                     }
                 }
                 if (wildcardable) {
                     wordsTokenized += "*";
                 }
                 if (wordsTokenized.length() > 0) {
                     luceneQuery += " OR (" + wordsTokenized + ")";
                 }
             }
             Logger.debug("%s", luceneQuery);
             List<Long> cityIds = Search.search(queryReferential + " AND (" + luceneQuery + ")", City.class).page(0, 10).fetchIds();
             List<City> cities = new ArrayList<City>();
             if(cityIds.size() > 0) {
                 cities = City.find("id in (?1) order by name", cityIds).fetch();
             }
             return cities;
         }
     }
 
     public static String toJson(List<City> cities) {
         List<String> jsonCities = new ArrayList<String>();
         for (City city : cities) {
             jsonCities.add(city.toJson());
         }
         return jsonCities.toString();
     }
 
     public Map<String, Object> toJsonMap() {
         Map<String, Object> jsonMap = new HashMap<String, Object>();
         jsonMap.put("name", this.name);
         jsonMap.put("inseeCode", this.inseeCode);
         jsonMap.put("postalCode", this.postalCode);
         return jsonMap;
     }
 
     public String toJson() {
         return new Gson().toJson(this.toJsonMap());
     }
 
     @Override
     public String toString() {
         return this.name + " " + this.postalCode + " ( insee: " + this.inseeCode + " )";
     }
 
 }
