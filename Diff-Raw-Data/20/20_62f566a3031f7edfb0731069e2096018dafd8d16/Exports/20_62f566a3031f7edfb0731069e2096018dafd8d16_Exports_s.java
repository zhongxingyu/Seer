 package controllers.admin;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import models.City;
 import models.Referential;
 import models.Way;
 
 import au.com.bytecode.opencsv.CSVWriter;
 import play.Logger;
 import play.Play;
 import play.libs.Codec;
 import play.mvc.Controller;
 
 public class Exports extends Admin {
 
     public static void citiesCSV(String referentialCode) throws IOException {
        Logger.debug("regerential : %s", referentialCode);
         Referential referential = Referential.find("code=?", referentialCode.toUpperCase()).first();
         notFoundIfNull(referential);
         File tmpFile = Play.getFile("data/tmp/"+Codec.UUID());
         if(!tmpFile.exists()) tmpFile.createNewFile();
         CSVWriter writer = new CSVWriter(new FileWriter(tmpFile));
         List<City> cities = City.find("referential=?", referential).fetch();
         Logger.debug("%d cities", cities.size());
         for(City city : cities) {
             String[] row = {
                 city.inseeCode,
                 city.name,
                 city.postalCode
             };
             writer.writeNext(row);
         }
         writer.close();
         response.contentType = "text/csv";
         renderBinary(tmpFile, "cities-" + referentialCode + ".csv");
     }
 
     public static void waysCSV(String referentialCode) throws IOException {
        Logger.debug("regerential : %s", referentialCode);
         Referential referential = Referential.find("code=?", referentialCode.toUpperCase()).first();
         notFoundIfNull(referential);
         File tmpFile = Play.getFile("data/tmp/"+Codec.UUID());
         if(!tmpFile.exists()) tmpFile.createNewFile();
         CSVWriter writer = new CSVWriter(new FileWriter(tmpFile));
         List<Way> ways = Way.find("referential=?", referential).fetch();
         Logger.debug("%d ways", ways.size());
         for(Way way : ways) {
             String[] row = {
                 way.cityInseeCode,
                 way.matriculation,
                 way.rivoliCode,
                 way.name,
                 way.synonymMatricualtion,
                 way.synonymRivoliCode
             };
             writer.writeNext(row);
         }
         writer.close();
         response.contentType = "text/csv";
         renderBinary(tmpFile, "ways-" + referentialCode + ".csv");
     }
 }
