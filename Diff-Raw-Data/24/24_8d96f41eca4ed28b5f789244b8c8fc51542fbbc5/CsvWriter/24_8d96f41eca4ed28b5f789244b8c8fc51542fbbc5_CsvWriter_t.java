 package com.sl.xmlparser.csv;
 
 import com.sl.xmlparser.config.Configuration;
 import com.sl.xmlparser.model.ProjectModel;
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class CsvWriter {
     
     private static final Logger logger = Logger.getLogger(CsvWriter.class.getCanonicalName());
     private static final String HEADER = "\"Nazwa projektu\";\"Opis pełny\";\"Opis skrócony\";\"Kategorie\";\"Kategoria dla wyszukiwarki\";\"Cena z vat\";\"Ilość\";\"Obrazki główne\";\"Rzuty\";\"Elewacje\";\"Usytuowanie\";\"Aktywny (1)\";\"01.Powierzchnia użytkowa\";\"02.Powierzchnia użytkowa:\";\"03.Kondygnacje\";\"04.Kondygnacje\";\"05.Garaż\";\"06.Garaż\";\"07.Technologia\";\"08.Technologia\";\"09.Rodzaj dachu\";\"10.Rodzaj dachu\";\"11.Minimalne wymiary działki\";\"12.Minimalne wymiary działki\";\"13.Powierzchnia zabudowy\";\"14.Kubatura\";\"15.Wysokość budynku\";\"16.Kąt nachylenia dachu\"";
     private Configuration configuration;
     
     public CsvWriter(Configuration configuration) {
         this.configuration = configuration;
     }
     
     public void write(List<ProjectModel> modelsToBeWritten) throws IOException {
         
         File dst = new File(configuration.getOutputDirectory(), configuration.getOutputCsvName());
         
         logger.log(Level.INFO, "Writing model classes into CSV file: {0}", dst.getAbsolutePath());
         
         PrintStream out = null;
         try {
             out = new PrintStream(dst);
             StringBuilder sb = new StringBuilder();
             
             out.println(HEADER);
             
             for(int i = 0; i < modelsToBeWritten.size(); ++i) {
                 ProjectModel pm = modelsToBeWritten.get(i);
                 sb.setLength(0);
                 
                 sb.append(getAsStringEscaped(pm.getProjectName())).append(";");
                 sb.append(getAsStringEscaped(pm.getFullDescription())).append(";");
                 sb.append(getAsStringEscaped(pm.getShortDescription())).append(";");
                 sb.append(getAsStringEscaped(pm.getCategory())).append(";");
                 sb.append(getAsStringEscaped(pm.getCategoryForSearcher())).append(";");
                 sb.append(getAsStringEscaped(pm.getPriceWithVat())).append(";");
                 sb.append(getAsStringEscaped(pm.getAmount())).append(";");
                 
                 sb.append("\"");
                for (int j = 0; j < pm.getImgMain().size(); ++j) {
                    sb.append(String.format(configuration.getImgMainTemplateConstant(), configuration.getOutputDirectoryPrefix(), i, pm.getImgMain().get(j)));
                    if(j < pm.getImgMain().size() - 1) {
                        sb.append(",");
                    }
                 }
                 sb.append("\";");
                 
                 sb.append("\"");
                 for (String imgProjection : pm.getImgProjection()) {
                     sb.append(String.format(configuration.getImgLinkTemplateConstant(), configuration.getOutputDirectoryPrefix(), i, imgProjection)).append(" ");
                 }
                 sb.append("\";");
                 
                 sb.append("\"");
                 for (String imgElevation : pm.getImgElevation()) {
                     sb.append(String.format(configuration.getImgLinkTemplateConstant(), configuration.getOutputDirectoryPrefix(), i, imgElevation)).append(" ");
                 }
                 sb.append("\";");
                 
                 sb.append("\"");
                 for (String imgLocation : pm.getImgLocation()) {
                     sb.append(String.format(configuration.getImgLinkTemplateConstant(), configuration.getOutputDirectoryPrefix(), i, imgLocation)).append(" ");
                 }
                 sb.append("\";");
                 
                 sb.append(getAsStringEscaped(pm.getActive())).append(";");
                 sb.append(getAsStringEscaped(pm.getUsableSpace())).append(";");
                 sb.append(getAsStringEscaped(pm.getUsableSpace2())).append(";");
                 sb.append(getAsStringEscaped(pm.getFloors())).append(";");
                 sb.append(getAsStringEscaped(pm.getFloors2())).append(";");
                 sb.append(getAsStringEscaped(pm.getGarage())).append(";");
                 sb.append(getAsStringEscaped(pm.getGarage2())).append(";");
                 sb.append(getAsStringEscaped(pm.getTechnology())).append(";");
                 sb.append(getAsStringEscaped(pm.getTechnology2())).append(";");
                 sb.append(getAsStringEscaped(pm.getRoofType())).append(";");
                 sb.append(getAsStringEscaped(pm.getRoofType2())).append(";");
                 sb.append(getAsStringEscaped(pm.getMinimumPlot())).append(";");
                 sb.append(getAsStringEscaped(pm.getMinimumPlot2())).append(";");
                 sb.append(getAsStringEscaped(pm.getBuildInArea())).append(";");
                 sb.append(getAsStringEscaped(pm.getVolume())).append(";");
                 sb.append(getAsStringEscaped(pm.getBuildingHeight())).append(";");
                 sb.append(getAsStringEscaped(pm.getSlopeOfTheRoof())).append(";");
                 sb.append(getAsStringEscaped(pm.getBasement())).append(";");
                 
                 out.println(sb.toString());
             }
         } finally {
             if (out != null) {
                 out.close();
             }
         }
         
     }
     
     private String getAsStringEscaped(Object o) {
         return "\"" + String.valueOf(o) + "\"";
     }
 }
