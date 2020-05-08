 package models;
 
 import au.com.bytecode.opencsv.CSVReader;
 
 import com.google.gson.Gson;
 
 import exceptions.BadCSVLineFormatException;
 
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 
 import org.hibernate.validator.Length;
 
 import play.data.validation.Equals;
 import play.data.validation.Required;
 import play.db.jpa.Model;
 import play.i18n.Messages;
 import play.modules.search.Field;
 import play.modules.search.Indexed;
 import play.modules.search.Search;
 import play.templates.JavaExtensions;
 import play.Logger;
 import java.util.Arrays;
 
 @Entity
 @Indexed
 public class Way extends Model {
 
     @ManyToOne
     @Field(joinField = "code")
     public Referential referential;
 
     @Column(length = 5)
     @Field
     @Required
     @Length(min=5, max=5)
     public String cityInseeCode;
 
     @Column(length = 32)
     @Field(sortable = true)
     @Required
     @Length(max=32)
     public String name;
 
     @Column(length = 10)
     @Length(min=10, max=10)
     public String rivoliCode;
 
     @Column(length = 8)
     @Length(min=8, max=8)
     public String matriculation;
 
     @Column(length = 8, nullable = true)
     @Length(min=8, max=8)
     public String synonymMatricualtion = null;
 
     @Column(length = 10, nullable = true)
     @Length(min=10, max=10)
     public String synonymRivoliCode = null;
 
     public Way() {
     }
 
     /*
      * CSV city import
      * 0: cityInseeCode (= 5 chars)
      * 1: matriculation (= 8 chars)
      * 2: rivoliCode (= 10 chars)
      * 3: name (<= 32 chars)
      * 4: synonymMatriculation (= 8 chars)
      * 5: synonymRivoliCode (=10 chars)
      */
     public Way(Referential referential, String[] CSVLine) throws BadCSVLineFormatException {
         this.referential = referential;
 
         cityInseeCode = CSVLine[0];
         matriculation = CSVLine[1];
         rivoliCode = CSVLine[2];
         name = CSVLine[3];
         synonymMatricualtion = CSVLine[4];
         synonymRivoliCode = CSVLine[5];
 
         if(cityInseeCode == null || cityInseeCode.isEmpty())
             badFormat("way.cityInseeCode.empty");
 
         if(cityInseeCode.length() != 5)
             badFormat("way.cityInseeCode.wrongLength");
 
         if(matriculation != null && !matriculation.trim().isEmpty() && matriculation.length() != 8)
             badFormat("way.matriculation.wrongLength");
         if (matriculation != null && matriculation.trim().isEmpty())
             matriculation = null;
 
         // FIXME : who has seen that a rivoli code has a length of 10 characters ??!
 //        if(rivoliCode != null && !rivoliCode.trim().isEmpty() && rivoliCode.length() != 10)
 //            badFormat("way.rivoliCode.wrongLength");
 
         if((matriculation == null || matriculation.length() == 0) && (rivoliCode == null || rivoliCode.length() == 0))
             badFormat("way.identifier.missing");
 
         if(name == null || name.length() == 0)
             badFormat("way.name.empty");
 
         if(name.length() > 32)
             badFormat("way.name.wrongLength");
 
         if(synonymMatricualtion != null && synonymMatricualtion.length() != 8 && synonymMatricualtion.length() != 0)
             badFormat("way.synonymMatriculation.wrongLength");
         if (synonymMatricualtion != null && synonymMatricualtion.length() == 0)
             synonymMatricualtion = null;
 
         if(synonymRivoliCode != null && synonymRivoliCode.length() != 10 && synonymRivoliCode.length() != 0)
             badFormat("way.synonymRivoliCode.wrongLength");
         if (synonymRivoliCode != null && synonymRivoliCode.length() == 0)
             synonymRivoliCode = null;
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
                 Way way = new Way(currentImport.referential, nextLine);
                 if(!exists(way)) {
                     way.save();
                 }
                 else {
                     currentImport.alreadyExists(way.toJson());
                 }
             } catch (BadCSVLineFormatException e) {
                 currentImport.badFormat(e.messageKey, e.jsonObject);
             }
         }
     }
 
     private static boolean exists(Way way) {
         if(way.matriculation != null) return find("referential = ? and matriculation = ?", way.referential, way.matriculation).first() != null;
         else return find("referential = ? and rivoliCode = ?", way.referential, way.rivoliCode).first() != null;
     }
 
     public static List<Way> search(String referentialCode, String city, String search) {
         if(referentialCode == null || referentialCode.length() == 0) {
             return new ArrayList<Way>();
         }
        String queryReferential = "referential:" + referentialCode;
         String cleanSearch = JavaExtensions.noAccents(search).toUpperCase().replace("'", " ").replace("-", " ").trim();
         if (cleanSearch.length() <= 1) {
             return new ArrayList<Way>();
         }
         String luceneQuery = "cityInseeCode:\"" + city + "\" AND (name:\"" + cleanSearch + "\"";
         List<String> clearnSearchTb = Arrays.asList(cleanSearch.split(" "));
         String wordsTokenized = "";
         boolean wildcardable = false;
         for (String word : clearnSearchTb) {
             if (word.length() > 1) {
                 if (wordsTokenized.length() > 0) {
                     wordsTokenized += " AND ";
                 }
                 wordsTokenized += "name:" + word;
                 wildcardable = true;
             }
         }
         if (wildcardable) {
             wordsTokenized += "*";
         }
         if (wordsTokenized.length() > 0) {
             if (clearnSearchTb.size() > 1) {
                 luceneQuery += " OR (" + wordsTokenized + ")";
             } else {
                 luceneQuery += " OR " + wordsTokenized;
             }
         }
         luceneQuery += ")";
         Logger.debug("%s", luceneQuery);
         List<Long> wayIds = Search.search(queryReferential + " AND (" + luceneQuery + ")", Way.class).page(0, 10).fetchIds();
         List<Way> ways = new ArrayList<Way>();
         if(wayIds.size() > 0) {
             ways = Way.find("id in (?1)", wayIds).fetch();
         }
         return ways;
     }
 
     public static String toJson(List<Way> cities) {
         List<String> jsonWays = new ArrayList<String>();
         for (Way way : cities) {
             jsonWays.add(way.toJson());
         }
         return jsonWays.toString();
     }
 
     public Map<String, Object> toJsonMap() {
         Map<String, Object> jsonMap = new HashMap<String, Object>();
         if (this.id != null) jsonMap.put("id", this.id);
         jsonMap.put("cityInseeCode", this.cityInseeCode);
         jsonMap.put("name", this.name);
         if (this.matriculation != null) jsonMap.put("matriculation", this.matriculation);
         if (this.rivoliCode != null) jsonMap.put("rivoliCode", this.rivoliCode);
         if (this.synonymMatricualtion != null) jsonMap.put("synonymMatriculation", this.synonymMatricualtion);
         if (this.synonymRivoliCode != null) jsonMap.put("synonymRivoliCode", this.synonymRivoliCode);
         return jsonMap;
     }
 
     public String toJson() {
         return new Gson().toJson(this.toJsonMap());
     }
 
     @Override
     public String toString() {
         return this.name + " " + this.matriculation + " ( city insee: " + this.cityInseeCode + " )";
     }
 
 }
