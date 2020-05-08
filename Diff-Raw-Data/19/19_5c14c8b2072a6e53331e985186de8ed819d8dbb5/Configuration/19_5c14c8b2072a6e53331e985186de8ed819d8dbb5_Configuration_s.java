 package com.sl.xmlparser.config;
 
 import org.kohsuke.args4j.Argument;
 import org.kohsuke.args4j.Option;
 
 public class Configuration {
     
     @Option(name = "--xml-main-element", usage = "Root XML element")
     private String mainXmlElementXPath = "/projekty/projekt";
     
     @Option(name = "--xml-project-name", usage = "Name XPath")
     private String projectNameXPath = "nazwa";
     
     @Option(name = "--xml-full-description", usage = "Full description XPath")
     private String fullDescriptionXPath = "opis";
     
     @Option(name = "--xml-short-description", usage = "Short description XPath")
     private String shortDescriptionXPath = "skrot";
     
     @Option(name = "--xml-category", usage = "Category XPath")
     private String categoryXPath = "kategoria";
     
     @Option(name = "--xml-price-with-vat", usage = "Prica with VAT XPath")
     private String priceWithVatXPath = "ceny/@brutto";
     
     @Option(name = "--xml-img-main", usage = "Main images XPath")
     private String imgMainXPath = "zdjecia//zdjecie[@nazwa='zdjecie']/@plik";
     
     @Option(name = "--xml-img-projection", usage = "Projection images XPath")
     private String imgProjectionXPath = "kondygnacje//kondygnacja/@rzut";
     
     @Option(name = "--xml-img-elevation", usage = "Elevation images XPath")
     private String imgElevationXPath = "elewacje//elewacja/@zdjecie";
     
     @Option(name = "--xml-img-location", usage = "Location images XPath")
     private String imgLocationXPath = "zdjecia/zdjecie[@nazwa='teren']/@plik";
     
     @Option(name = "--xml-usable-space", usage = "Usable space XPath")
     private String usableSpaceXPath = "powierzchnie/@uzytkowa";
     
     @Option(name = "--xml-floors", usage = "Floors XPath")
     private String floorsXPath = "concat(kondygnacje//kondygnacja[1]/@nazwa, ',', kondygnacje//kondygnacja[2]/@nazwa, ',', kondygnacje//kondygnacja[3]/@nazwa)";
     
     @Option(name = "--xml-garage", usage = "Garage XPath")
     private String garageXPath = "ilosc_stanowisk_garazowych";
     
     @Option(name = "--xml-technology", usage = "Technology XPath")
     private String technologyXPath = "rodzaj";
     
     @Option(name = "--xml-roof-type", usage = "Roof type XPath")
     private String roofTypeXPath = "rodzaj_dachu";
     
     @Option(name = "--xml-minimum-plot", usage = "Minimum plot XPath")
     private String minimumPlotXPath = "concat(wymiary/wymiar[@nazwa='min-szerokosc-dzialki'], ' x ', wymiary/wymiar[@nazwa='min-dlugosc-dzialki'], ' m')";
     
     @Option(name = "--xml-built-in-area", usage = "Build in are XPath")
     private String builtInAreaXPath = "concat(powierzchnie/powierzchnia[@nazwa='zabudowy'], ' m2')";
     
     @Option(name = "--xml-volume", usage = "Volume XPath")
     private String volumeXPath = "concat(kubatura/@calkowita, ' m3')";
     
     @Option(name = "--xml-building-height", usage = "Building height XPath")
     private String buildingHeightXPath = "concat(wymiary/wymiar[@nazwa='wysokosc'], ' m')";
     
     @Option(name = "--xml-slope-of-the-roof", usage = "Slope of the roof XPath")
     private String slopeOfTheRoofXPath = "concat(wymiary/wymiar[@nazwa='kat-dachu'], '°')";
     
     @Option(name = "--xml-basement", usage = "Basement XPath")
     private String basementXPath;
     
     @Option(name = "--constant-amount", usage = "Constant used for Amount element")
     private Integer amountConstant = 99;
     
     @Option(name = "--constant-active", usage = "Constant used for Active element")
     private Integer active = 1;
     
     @Option(name = "--constant-img-main-uri-template", usage = "Directory with main images")
     private String imgMainUriTemplateContant = "/home/slaskawiec/Downloads/xml/wizualizacje/%s";
     
     @Option(name = "--constant-img-location-uri-template", usage = "Directory with location images")
     private String imgLocationUriTemplateContant = "/home/slaskawiec/Downloads/xml/usytuowanie/%s";
 
     @Option(name = "--constant-img-projection-uri-template", usage = "Directory with projection images")
     private String imgProjectionUriTemplateContant = "/home/slaskawiec/Downloads/xml/rzuty/%s";
     
     @Option(name = "--constant-img-elevation-uri-template", usage = "Directory with elevation images")
     private String imgElevationUriTemplateContant = "/home/slaskawiec/Downloads/xml/elewacje/%s";
     
     @Option(name = "--constant-img-link-template", usage = "HTML template for images")
     private String imgLinkTemplateConstant = "<img src=\"%s\" alt=\"\" />";
     
     @Option(name = "--output-directory", usage = "Output directory")
     private String outputDirectory = "./test";
     
     @Option(name = "--output-csv-name", usage = "Output CSV file name")
     private String outputCsvName = "index.csv";
 
     @Option(name = "--output-img-width", usage = "Width of the resized image")
     private Integer outputImgWidth = 640;
     
     @Option(name = "--output-img-height", usage = "Height of the resized image")
     private Integer outputImgHeight = 480;
     
     @Option(name = "--output-img-compression-ratio", usage = "JPG Compression ratio")
     private Float outputImgCompressionRatio = 0.8f;
     
     @Argument(index = 0, required = true, usage = "Input XML URI")
     private String inputXmlFile;
     
     public Integer getOutputImgWidth() {
         return outputImgWidth;
     }
 
     public void setOutputImgWidth(Integer outputImgWidth) {
         this.outputImgWidth = outputImgWidth;
     }
 
     public Integer getOutputImgHeight() {
         return outputImgHeight;
     }
 
     public void setOutputImgHeight(Integer outputImgHeight) {
         this.outputImgHeight = outputImgHeight;
     }
     
     public String getInputXmlFile() {
         return inputXmlFile;
     }
 
     public void setInputXmlFile(String inputXmlFile) {
         this.inputXmlFile = inputXmlFile;
     }
     
     public String getOutputCsvName() {
         return outputCsvName;
     }
 
     public void setOutputCsvName(String outputCsvName) {
         this.outputCsvName = outputCsvName;
     }
 
     
     public String getOutputDirectory() {
         return outputDirectory;
     }
 
     public void setOutputDirectory(String outputDirectory) {
         this.outputDirectory = outputDirectory;
     }
 
     public String getImgLinkTemplateConstant() {
         return imgLinkTemplateConstant;
     }
 
     public void setImgLinkTemplateConstant(String imgLinkTemplateConstant) {
         this.imgLinkTemplateConstant = imgLinkTemplateConstant;
     }
     
     public String getMainXmlElementXPath() {
         return mainXmlElementXPath;
     }
 
     public void setMainXmlElementXPath(String mainXmlElementXPath) {
         this.mainXmlElementXPath = mainXmlElementXPath;
     }
 
     public String getProjectNameXPath() {
         return projectNameXPath;
     }
 
     public void setProjectNameXPath(String projectNameXPath) {
         this.projectNameXPath = projectNameXPath;
     }
 
     public String getFullDescriptionXPath() {
         return fullDescriptionXPath;
     }
 
     public void setFullDescriptionXPath(String fullDescriptionXPath) {
         this.fullDescriptionXPath = fullDescriptionXPath;
     }
 
     public String getShortDescriptionXPath() {
         return shortDescriptionXPath;
     }
 
     public void setShortDescriptionXPath(String shortDescriptionXPath) {
         this.shortDescriptionXPath = shortDescriptionXPath;
     }
 
     public String getCategoryXPath() {
         return categoryXPath;
     }
 
     public void setCategoryXPath(String categoryXPath) {
         this.categoryXPath = categoryXPath;
     }
 
     public String getPriceWithVatXPath() {
         return priceWithVatXPath;
     }
 
     public void setPriceWithVatXPath(String priceWithVatXPath) {
         this.priceWithVatXPath = priceWithVatXPath;
     }
 
     public String getImgMainXPath() {
         return imgMainXPath;
     }
 
     public void setImgMainXPath(String imgMainXPath) {
         this.imgMainXPath = imgMainXPath;
     }
 
     public String getImgProjectionXPath() {
         return imgProjectionXPath;
     }
 
     public void setImgProjectionXPath(String imgProjectionXPath) {
         this.imgProjectionXPath = imgProjectionXPath;
     }
 
     public String getImgElevationXPath() {
         return imgElevationXPath;
     }
 
     public void setImgElevationXPath(String imgElevationXPath) {
         this.imgElevationXPath = imgElevationXPath;
     }
 
     public String getImgLocationXPath() {
         return imgLocationXPath;
     }
 
     public void setImgLocationXPath(String imgLocationXPath) {
         this.imgLocationXPath = imgLocationXPath;
     }
 
     public String getUsableSpaceXPath() {
         return usableSpaceXPath;
     }
 
     public void setUsableSpaceXPath(String usableSpaceXPath) {
         this.usableSpaceXPath = usableSpaceXPath;
     }
 
     public String getFloorsXPath() {
         return floorsXPath;
     }
 
     public void setFloorsXPath(String floorsXPath) {
         this.floorsXPath = floorsXPath;
     }
 
     public String getGarageXPath() {
         return garageXPath;
     }
 
     public void setGarageXPath(String garageXPath) {
         this.garageXPath = garageXPath;
     }
 
     public String getTechnologyXPath() {
         return technologyXPath;
     }
 
     public void setTechnologyXPath(String technologyXPath) {
         this.technologyXPath = technologyXPath;
     }
 
     public String getRoofTypeXPath() {
         return roofTypeXPath;
     }
 
     public void setRoofTypeXPath(String roofTypeXPath) {
         this.roofTypeXPath = roofTypeXPath;
     }
 
     public String getMinimumPlotXPath() {
         return minimumPlotXPath;
     }
 
     public void setMinimumPlotXPath(String minimumPlotXPath) {
         this.minimumPlotXPath = minimumPlotXPath;
     }
 
     public String getBuiltInAreaXPath() {
         return builtInAreaXPath;
     }
 
     public void setBuiltInAreaXPath(String builtInAreaXPath) {
         this.builtInAreaXPath = builtInAreaXPath;
     }
 
     public String getVolumeXPath() {
         return volumeXPath;
     }
 
     public void setVolumeXPath(String volumeXPath) {
         this.volumeXPath = volumeXPath;
     }
 
     public String getBuildingHeightXPath() {
         return buildingHeightXPath;
     }
 
     public void setBuildingHeightXPath(String buildingHeightXPath) {
         this.buildingHeightXPath = buildingHeightXPath;
     }
 
     public String getSlopeOfTheRoofXPath() {
         return slopeOfTheRoofXPath;
     }
 
     public void setSlopeOfTheRoofXPath(String slopeOfTheRoofXPath) {
         this.slopeOfTheRoofXPath = slopeOfTheRoofXPath;
     }
 
     public String getBasementXPath() {
         return basementXPath;
     }
 
     public void setBasementXPath(String basementXPath) {
         this.basementXPath = basementXPath;
     }
 
     public Integer getAmountConstant() {
         return amountConstant;
     }
 
     public void setAmountConstant(Integer amountConstant) {
         this.amountConstant = amountConstant;
     }
 
     public Integer getActive() {
         return active;
     }
 
     public void setActive(Integer aktywny) {
         this.active = aktywny;
     }
 
     public String getImgMainUriTemplateContant() {
         return imgMainUriTemplateContant;
     }
 
     public void setImgMainUriTemplateContant(String imgMainUriTemplateContant) {
         this.imgMainUriTemplateContant = imgMainUriTemplateContant;
     }
 
     public String getImgLocationUriTemplateContant() {
         return imgLocationUriTemplateContant;
     }
 
     public void setImgLocationUriTemplateContant(String imgLocationUriTemplateContant) {
         this.imgLocationUriTemplateContant = imgLocationUriTemplateContant;
     }
 
     public String getImgProjectionUriTemplateContant() {
         return imgProjectionUriTemplateContant;
     }
 
     public void setImgProjectionUriTemplateContant(String imgProjectionUriTemplateContant) {
         this.imgProjectionUriTemplateContant = imgProjectionUriTemplateContant;
     }
 
     public String getImgElevationUriTemplateContant() {
         return imgElevationUriTemplateContant;
     }
 
     public void setImgElevationUriTemplateContant(String imgElevationUriTemplateContant) {
         this.imgElevationUriTemplateContant = imgElevationUriTemplateContant;
     }
     
     public Float getOutputImgCompressionRatio() {
         return outputImgCompressionRatio;
     }
 
     public void setOutputImgCompressionRatio(Float outputImgCompressionRatio) {
         this.outputImgCompressionRatio = outputImgCompressionRatio;
     }
 }
