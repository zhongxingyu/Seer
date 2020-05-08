 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  * 
  * 
  */
 package models;
 
 import controllers.DoSearch;
 import helpers.Helpers;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.util.Date;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.persistence.*;
 import play.db.jpa.GenericModel;
 import javax.xml.transform.stream.StreamSource;
 import net.sf.saxon.s9api.Processor;
 import net.sf.saxon.s9api.Serializer;
 import net.sf.saxon.s9api.XsltCompiler;
 import net.sf.saxon.s9api.XsltExecutable;
 import net.sf.saxon.s9api.XsltTransformer;
 import play.db.jpa.JPABase;
 import play.modules.search.Field;
 import play.modules.search.Indexed;
 
 /**
  *
  *
  * This class holds all xml-files
  * Variants of originals are held in variant-counter, original is number 0
  * Name of xml-file is preserved as given when uploaded
  * Name of assets should be on type
  *
  * Enunmeration of asset-type is kept in string due to db-restrinctions on jpa-enums
  *
  */
 @Indexed
 @Entity
 public class Asset extends GenericModel {
 
     @Id
     @SequenceGenerator(name = "asset_id_seq_gen", sequenceName = "asset_id_seq")
     @GeneratedValue(generator = "asset_id_seq_gen")
     public long id;
     @Lob
     public String xml;
     @Lob
     public String html;
     @Field
     @Lob
     public String htmlAsText;
     @Lob
     public String comment;
     @Lob
     public String name;
     @Lob
     public String fileName;
     public int variant;
     public int pictureNumber = 0;
     @Column(name = "import_date")
     @Temporal(TemporalType.TIMESTAMP)
     public java.util.Date importDate;
     @Lob
     public String rootName;
     @Lob
     public String type;
     @Lob
     public String refs;
     
     /* no enum-support in db, unfornately */
     public static String imageType = "imageType";
     public static String countryImage = "countryuImage";
     public static String introType = "introType";
     public static String manusType = "manus";
     public static String rootType = "root";
     public static String variantType = "variant";
     public static String commentType = "comment";
     public static String mythType = "myth";
     public static String personType = "person";
     public static String placeType = "place";
     public static String veiledningType = "veiledning";
     public static String txrType = "txr";
     public static String varListType = "varList";
     public static String bibleType = "bible";
     public static String registranten = "registranten";
     public static String bookinventory = "bookinventory";
     public static String mapVej = "mapVej";
     public static String mapXml = "mapXml";
 
     /**
      * Used by images
      * images on form rootname_number.jpg
      */
     public Asset(String name, String fileName, String comment, String type) {
         this.name = name;
         this.fileName = fileName;
         this.comment = comment;
         this.type = type;
         this.rootName = fileName.replace(".jpg", "").replaceFirst("_\\d+$", "");
         System.out.println("Rootname: " + rootName);
     }
 
     public Asset(String name, String fileName, String html, String xml, String comment, int variant, String type, String ref) {
         System.out.println("***** Constructing new asset, type is: " + type);
         this.variant = variant;
         this.name = name;
         this.html = html;
         this.xml = xml;
         this.importDate = new java.util.Date();
         this.comment = comment;
         this.fileName = fileName;
         this.rootName = getRootName(fileName, type);
         this.type = type;
         this.refs = ref;
         System.out.println("Root-name: " + rootName);
     }
 
     
     /**
      * Always make html searchable as text before saving
      */
     @Override
     public <T extends JPABase> T save() {
         System.out.println("Hey - I am saved");
         htmlAsText = Helpers.stripHtml(html);
         return super.save();
     }
 
     /**
      * create teaser for search-list
      * 
      * @return html with lookfor highlighted
      */
     public String getTeaser(String lookfor) {
         return DoSearch.createTeaser(htmlAsText, lookfor);
     }
 
     
     /**
      * Get the id for an asset
      * Use the filename is a key
      *
      * @return database-id
      * 
      */
     public long getCorrespondingRootId() {
         System.out.println("Getting root for: " + this.fileName);
         Asset root = Asset.find("rootName = ? and type = ?", rootName, Asset.rootType).first();
         return root.id;
     }
 
     /**
      * The txt-file should have a corresponding intro-file
      * @return id of intro-file asset
      * 
      */
     public String getCorrespondingIntro() {
         System.out.println("Looking for rootname: " + rootName);
         Asset intro = Asset.find("rootName = ? and type = ?", rootName, Asset.introType).first();
         return (intro != null ? intro.html : "");
     }
 
     /**
      * The txt-file should have a corresponding txr-file
      * @return id of txt-file
      * 
      */
     public String getCorrespondingTxr() {
         Asset txr = Asset.find("rootName = ? and type = ?", rootName, Asset.txrType).first();
         System.out.println("Txr is: " + txr);
         return (txr != null ? txr.html : "");
     }
 
     /**
      * The txt-file should have a corresponding comment-file
      *
      * @return comment-content as String after preprocessing
      * 
      */
     public String getCorrespondingComment() {
         Asset comment = Asset.find("fileName = ? and type = ?", rootName + "_com.xml", Asset.commentType).first();
         // System.out.println("Corresponding comment: " + intro.html);
         return (comment != null ? comment.html : "");
     }
 
     /**
      * Get xml and references for this asset
      * Currently not in use
      */
     public String getHtmlAndReferences() {
         return xml + refs;
     }
 
     /**
      * Calculates root name of an asset, based on the syntax of the filename
      * The root-name is stored in the asset model
      * @return root-name of the asset
      */
     public static String getRootName(String fileNameIn, String assetType) {
         String fileName = fileNameIn;
         fileName = fileName.replaceFirst(".xml", "").replaceFirst("_intro", "").replaceFirst("_com", "").replaceFirst("_txt", "").replaceFirst("_varList", "").replaceFirst("_txr", "").replaceFirst("_fax", "");
         if (assetType.equals(Asset.rootType) ||
                 assetType.equals(Asset.commentType) ||
                 assetType.equals(Asset.introType) ||
                 assetType.equals(Asset.txrType) ||
                assetType.equals(Asset.veiledningType)) return fileName;
         
         Pattern pattern = Pattern.compile("(.*)_.*\\d+$");
         Matcher matcher = pattern.matcher(fileName);
         if (!matcher.matches()) {
             System.out.println("Setting root name to: " + fileName);
             return fileName;
         } else {
             System.out.println("Setting root name to (regexp match): " + matcher.group(1));
             return matcher.group(1);
         }
     }
 
     public static Asset uploadCountryImage(String name, String comment, File file) {
         String fileName = file.getName();
         Asset asset = new Asset(name, fileName, comment, Asset.countryImage);
         asset.importDate = new Date();
         String filePath = play.Play.applicationPath.getAbsolutePath() + File.separator + "public" + File.separator + "images" + File.separator + fileName;
         Helpers.copyfile(file.getAbsolutePath(), filePath); 
         asset.save();
         return asset;
     }
     
     /**
      * 
      * Handles upload of fix-image
      * Binary file is copied and kept in the application-path
      * Every picture has a number, this is calculated based on the file-name
      * Asset with type Asset.imageType is created
      * 
      */
     public static Asset uploadImage(String name, String comment, File file) {
         String fileName = file.getName();
         fileName = fileName.replaceFirst("_fax", "_").replaceFirst("_0+", "_");
         Asset asset;
         if (Asset.find("fileName = ?", fileName).first() != null) {
             asset = Asset.find("fileName = ?", fileName).first();
             asset.comment = comment;
             asset.name = name;
             asset.type = Asset.imageType;
         } else {
             asset = new Asset(name, fileName, comment, Asset.imageType);
         }
         asset.importDate = new Date();
         String filePath = play.Play.applicationPath.getAbsolutePath() + File.separator + "public" + File.separator + "images" + File.separator + fileName;
         Helpers.copyfile(file.getAbsolutePath(), filePath);
         String[] pictureNums = fileName.replace(".jpg", "").split("_");
         int pictureNum = Integer.parseInt(pictureNums[pictureNums.length - 1]);
         asset.pictureNumber = pictureNum;
         asset.save();
         return asset;
     }
 
     /**
      * Keep a local version of the uploaded xml
      * Might not be needed?
      * 
      */
     private static String copyXmlToXmlDirectory(File epub) {
         File newFile = new File(play.Play.applicationPath.getAbsolutePath() + File.separator + "public" + File.separator + "xml" + File.separator + epub.getName());
         Helpers.copyfile(epub.getAbsolutePath(), newFile.getAbsolutePath());
         return newFile.getAbsolutePath();
     }
     
     /**
      * 
      * Handle upload of xml-file
      * Calculate asset-type based on file-name
      * Copy and keep xml-file
      * Calculate and keep html based on the right xslt
      * 
      * Note:
      * variant = 0 means it is the original
      *
      *
      */
     public static Asset uploadXmlFile(String name, String comment, File epub) {
         int variant = 0;
         String type;
         System.out.println("Epub name: " + epub.getName());
         if (epub.getName().equals("map_vej.xml")) {
             type = Asset.mapVej;
         } else if (epub.getName().startsWith("map_")) {
             type = Asset.mapXml;
         } else if (epub.getName().contains("_vej")) {
             type = Asset.veiledningType;
         } else if (epub.getName().equals("place.xml")) {
             type = Asset.placeType;
         } else if (epub.getName().equals("bible.xml")) {
             type = Asset.bibleType;
         } else if (epub.getName().equals("regList.xml")) {
             type = Asset.registranten;
         } else if (epub.getName().equals("pers.xml")) {
             type = Asset.personType;
         } else if (epub.getName().equals("myth.xml")) {
             type = Asset.mythType;
         } else if (epub.getName().replace(".xml", "").endsWith("_com")) {
             type = Asset.commentType;
         } else if (epub.getName().contains("intro")) {
             type = Asset.introType;
         } else if (epub.getName().contains("bookInventory")) {
             type = Asset.bookinventory;
         } else if (epub.getAbsolutePath().matches(".*_ms[1-9]*.xml")) {
             System.out.println("Type is manustype!");
             Pattern pattern = Pattern.compile("ms(\\d+)");
             Matcher matcher = pattern.matcher(epub.getAbsolutePath());
             String found = "no match";
             if (matcher.find()) {
                 System.out.println("Manus number is: " + matcher.group(1));
                 found = matcher.group(1);
                 variant = Integer.parseInt(found);
             }
             type = Asset.manusType;
         } else if (epub.getName().contains("txr")) {
             type = Asset.txrType;
         } else if (epub.getName().contains("varList")) {
             System.out.println("Type is varList");
             type = Asset.varListType;
         } else {
             Pattern pattern = Pattern.compile("v(\\d+)");
             Matcher matcher = pattern.matcher(epub.getAbsolutePath());
             String found = "no match";
             if (matcher.find()) {
                 System.out.println("Variant is: " + matcher.group(1));
                 found = matcher.group(1);
                 variant = Integer.parseInt(found);
                 type = Asset.variantType;
             } else {
                 System.out.println("No variant found");
                 type = Asset.rootType;
             }
         }
         System.out.println("File: " + epub);
         System.out.println("File-type: " + type);
         String copiedFile = copyXmlToXmlDirectory(epub);
         System.out.println("Copied file: " + copiedFile);
         
         String html;
         
         // consider a hash :-)
         if (type.equals(Asset.mapVej) || type.equals(Asset.mapXml)) {
           html =  Asset.xmlRefToHtml(epub.getAbsolutePath(), "vejXSLT.xsl"); 
         } else if (type.equals(Asset.veiledningType)) {
           html = fixHtml(Asset.xmlRefToHtml(epub.getAbsolutePath(), "veiledning.xsl"));
         } else if (type.equals(Asset.placeType)) {
             html = fixHtml(Asset.xmlRefToHtml(epub.getAbsolutePath(), "placeXSLT.xsl"));
         } else if (type.equals(Asset.personType)) {
             html = Asset.xmlRefToHtml(epub.getAbsolutePath(), "persXSLT.xsl");
         } else if (type.equals(Asset.commentType)) {
             html = fixHtml(Asset.xmlRefToHtml(copiedFile, "comXSLT.xsl"));
         } else if (type.equals(Asset.introType)) {
             html = fixHtml(Asset.xmlToHtmlIntro(copiedFile));
         } else if (type.equals(Asset.variantType)) {
             html = fixHtml(Asset.xmlToHtmlVariant(copiedFile));
         } else if (type.equals(Asset.rootType)) {
             html = fixHtml(Asset.xmlToHtml(copiedFile));
         } else if (type.equals(Asset.manusType)) {
             html = Asset.xmlToHtmlManus(copiedFile);
         } else if (type.equals(Asset.mythType)) {
             html = Asset.xmlRefToHtml(epub.getAbsolutePath(), "mythXSLT.xsl");
         } else if (type.equals(Asset.bibleType)) {
             html = Asset.xmlRefToHtml(epub.getAbsolutePath(), "bibleXSLT.xsl");
         } else if (type.equals(Asset.registranten)) {
             html = Asset.xmlRefToHtml(epub.getAbsolutePath(), "varListXSLT.xsl");
         } else if (type.equals(Asset.txrType)) {
             html = fixHtml(Asset.xmlRefToHtml(copiedFile, "txrXSLT.xsl"));
         } else if (type.equals(Asset.varListType)) {
             html = Asset.xmlRefToHtml(copiedFile, "varListXSLT.xsl");
         } else if (type.equals(Asset.bookinventory)) {
             html = Asset.xmlRefToHtml(copiedFile, "bookInventoryXSLT.xsl");
         } else {
             html = "Not found: filetype unknown";
             throw new Error("No recognized filetype found");
         }
         if (html.startsWith("Error")) {
             throw new Error("Probably an error in xslt-stylesheet or xml: " + html);
         }
 
         // create or update asset
         String xml = "";
         try {
             xml = Helpers.readFile(epub.getAbsolutePath());
         } catch (IOException ex) {
             Logger.getLogger(Asset.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         // String refs = Helpers.getReferencesFromXml(xml, epub.getName().replaceFirst("", html));
         String references = "";
 
         Asset asset;
         System.out.println("Filename: " + epub.getName());
         if (Asset.find("filename", epub.getName()).fetch().size() > 0) {
             System.out.println("--- Updating asset with name: " + epub.getName());
             asset = Asset.find("filename", epub.getName()).first();
             asset.name = name;
             asset.html = html;
             asset.comment = comment;
             asset.variant = variant;
             asset.importDate = new Date();
             asset.type = type;
             asset.refs = references;
             asset.fileName = epub.getName();
             asset.rootName = getRootName(epub.getName(), asset.type);
         } else {
             asset = new Asset(name, epub.getName(), html, xml, comment, variant, type, references);
         }
         asset.save();
         System.out.println("Root-name: " + asset.rootName);
         System.out.println("Asset is saved, total assets: " + Asset.findAll().size());
         if (asset.type.equals(Asset.rootType)) {
             Chapter.createChaptersFromAsset(asset);
         }
         return asset;
     }
 
     // remove headers so html can be shown in div
     // consider fixing xslt later
     private static String fixHtml(String html) {
         // System.out.println("Fixing html - removing body");
         // html = html.replaceFirst("(?s).*<body.*?>", "<div class='simple' id='top'>");
         // System.out.println("Fixing html 2");
         // html = html.replaceFirst("(?s)</body.*", "</div>");
         // System.out.println("Fixing html 3");
         html = html.replaceAll("<div class='[^']+'/>", "").replaceAll("<div class=\"[^\"]+\"/>", "");
         System.out.println("Html if fixed :-)");
         return html;
     }
 
     /**
      * Get variants and manuses based on the root-asset
      * @return List of variants, manuses and varLists connected to this asset
      * 
      */
     public static List<Asset> getVariants(long assetId) {
 
         Asset rootAsset = Asset.findById(assetId);
         System.out.println("rootName: " + rootAsset.id);  
         List<Asset> variants = Asset.find("rootName = ? and (type = ?  or type =  ? or type = ?) order by type, variant", rootAsset.rootName, Asset.variantType, Asset.manusType, Asset.varListType).fetch();
         return variants;
     }
 
     
     /**
      * Get correspondig manus to an asset
      * @return List of manuses 
      */
     public static List<Asset> getManus(long assetId) {
         Asset rootAsset = Asset.findById(assetId);
         List<Asset> manus = Asset.find("rootName = ? and type = ? ", rootAsset.rootName, "manus").fetch();
         if (manus.isEmpty()) {
             manus = Asset.find("rootName = ? and type = ? ", rootAsset.rootName.replaceFirst("_1", ""), "manus").fetch();
         }
         return manus;
     }
 
     /**
      * Combine xml and xslt for ref-type assets
      */
     public static String xmlRefToHtml(String fileName, String xsltPath) {
         String filePath = play.Play.applicationPath.getAbsolutePath() + File.separator + "public" + File.separator + "xslt" + File.separator + xsltPath;
         return xmlToHtml(fileName, filePath);
     }
 
     /**
      * Combine xml and xslt for myth-type assets
      */
     public static String xmlToHtmlMyth(String fileName) {
         String filePath = play.Play.applicationPath.getAbsolutePath() + File.separator + "public" + File.separator + "xslt" + File.separator + "mythXSLT.xsl";
         return xmlToHtml(fileName, filePath);
     }
 
     /**
      * Combine xml and xslt for manus-type assets
      */
     public static String xmlToHtmlManus(String fileName) {
         String filePath = play.Play.applicationPath.getAbsolutePath() + File.separator + "public" + File.separator + "xslt" + File.separator + "msXSLT.xsl";
         return xmlToHtml(fileName, filePath);
     }
 
     /**
      * Combine xml and xslt for intro-type assets
      */
     public static String xmlToHtmlIntro(String fileName) {
         System.out.println("Uploading introduction");
         String filePath = play.Play.applicationPath.getAbsolutePath() + File.separator + "public" + File.separator + "xslt" + File.separator + "introXSLT.xsl";
         return xmlToHtml(fileName, filePath);
     }
 
     /**
      * Combine xml and xslt for var-type assets
      */
     public static String xmlToHtmlVariant(String fileName) {
         String filePath = play.Play.applicationPath.getAbsolutePath() + File.separator + "public" + File.separator + "xslt" + File.separator + "varXSLT.xsl";
         return xmlToHtml(fileName, filePath);
     }
 
     /**
      * Combine xml and xslt for root-type assets
      */
     public static String xmlToHtml(String fileName) {
         // String filePath = play.Play.applicationPath.getAbsolutePath() + File.separator + "public" + File.separator + "xslt" + File.separator + "xhtml2" + File.separator + "tei.xsl";
         String filePath = play.Play.applicationPath.getAbsolutePath() + File.separator + "public" + File.separator + "xslt" + File.separator + "txtXSLT.xsl";
         return xmlToHtml(fileName, filePath);
     }
 
     
     /**
      * Does the xslt-transformation of a xml-file and and a xslt-file
      * @params fileName - name of xml-file
      * @params fileName - name of xslt - file
      * 
      */
     public static String xmlToHtml(String fileName, String filePath) {
         System.out.println("User dir: " + System.getProperty("user.dir"));
         try {
             File xmlIn = new File(fileName);
             StreamSource source = new StreamSource(xmlIn);
             Processor proc = new Processor(false);
             XsltCompiler comp = proc.newXsltCompiler();
             System.out.println("Filepath: " + filePath);
             XsltExecutable exp = comp.compile(new StreamSource(new File(filePath)));
             Serializer out = new Serializer();
             ByteArrayOutputStream buf = new ByteArrayOutputStream();
             out.setOutputProperty(Serializer.Property.METHOD, "xhtml");
             out.setOutputProperty(Serializer.Property.OMIT_XML_DECLARATION, "yes");
             out.setOutputProperty(Serializer.Property.INDENT, "no");
             out.setOutputProperty(Serializer.Property.ENCODING, "utf-8");
             out.setOutputStream(buf);
             // out.setOutputFile(new File("tour.html"));
             XsltTransformer trans = exp.load();
             // trans.setInitialTemplate(new QName("main"));
             trans.setSource(source);
             trans.setDestination(out);
             trans.transform();
             // System.out.println("Output generated: " + buf.toString());
             return buf.toString("utf-8");
         } catch (Exception e) {
             e.printStackTrace();
             return ("Error: " + e.toString());
         }
 
     }
     
     public int getNumberOfPictures() {
         System.out.println("**** Looking for rootName: " + rootName);
         return Asset.find("rootName = ? and type = ?", rootName, Asset.imageType).fetch().size();
     }
 
     public String getFileNameWithoutXml() {
         return this.rootName;
     }
 
     
     
 }
