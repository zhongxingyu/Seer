 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package examples;
 
 import common.db.DBUtil;
 import common.utils.StringUtil;
 import java.io.File;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import paragraph.bean.Document;
 import paragraph.bean.Paragraph;
 import paragraph.db.DocumentTable;
 import paragraph.db.ParagraphTable;
 import taxonomy.Taxonomy;
 import taxonomy.bean.TaxonomyDescription;
 import taxonomy.bean.TaxonomyDiscussion;
 import taxonomy.bean.TaxonomyGenericElement;
 import taxonomy.bean.TaxonomyKeyFile;
 import taxonomy.bean.TaxonomyMeta;
 import taxonomy.bean.TaxonomyNomenclature;
 import taxonomy.bean.TaxonomyOtherInfo;
 import taxonomy.bean.TaxonomySynonym;
 import taxonomy.key.KeyTo;
 import taxonomy.key.bean.KeyDiscussion;
 import taxonomy.key.bean.KeyHeading;
 import taxonomy.key.bean.KeyStatement;
 
 /**
  *
  * @author iychoi
  */
 public class Parse_11_Mitchell_Apidae_1962 {
     
     private int documentID;
     private Document document;
     private List<Paragraph> paragraphs;
     
     private Document loadDocument(int documentID) throws IOException {
         Connection conn = DBUtil.getConnection();
         Document document = DocumentTable.getDocument(conn, documentID);
         try {
             conn.close();
         } catch (SQLException ex) {
             throw new IOException(ex.getMessage());
         }
         return document;
     }
     
     private List<Paragraph> loadParagraphs(Document document) throws IOException {
         Connection conn = DBUtil.getConnection();
         List<Paragraph> paragraphs = ParagraphTable.getParagraphs(conn, document.getDocumentID());
 
         try {
             conn.close();
         } catch (SQLException ex) {
             throw new IOException(ex.getMessage());
         }
         return paragraphs;
     }
     
     private String getTaxonName(String nameInfo) {
         String newTaxon = nameInfo;
         int idxBrace = nameInfo.indexOf("[");
         if(idxBrace > 0) {
             newTaxon = nameInfo.substring(0, idxBrace).trim();
         }
         
         int idxDot = nameInfo.indexOf(".");
         if(idxDot > 0) {
             newTaxon = newTaxon.substring(idxDot + 1).trim();
         }
         
         Pattern p1 = Pattern.compile("^(.+)?, \\d+$");
         Matcher mt1 = p1.matcher(newTaxon);
         if(mt1.find()) {
             return mt1.group(1);
         }
         
         return newTaxon;
     }
     
     private String getPureName(String name) {
         String authority = getAuthority(name);
         
         int idx = name.indexOf(authority);
         if(idx >= 0) {
             return name.substring(0, idx).trim();
         }
         
         System.out.println("No purename found - " + name);
         return null;
     }
     
     private String getAuthority(String name) {
         String name2 = name;
         if(name2.endsWith(", sp. nov.")) {
             int lastIdx = name2.indexOf(", sp. nov.");
             if(lastIdx >= 0) {
                 name2 = name2.substring(0, lastIdx);
             }
         }
         
         int startPos = name2.length()-1;
         
         int commaIdx = name2.indexOf(",");
         if(commaIdx >= 0) {
             startPos = Math.min(startPos, commaIdx - 1);
         }
         
         int etIdx = name2.indexOf(" et ");
         if(etIdx >= 0) {
             startPos = Math.min(startPos, etIdx - 1);
         }
         
         int spacePos = 0;
         for(int i=startPos;i>=0;i--) {
             if(name2.charAt(i) == ' ') {
                 if(i >= 1 && name2.charAt(i - 1) != '.') {
                     spacePos = i;
                     break;
                 }
             }
         }
 
         String authority = name2.substring(spacePos + 1).trim();
         if((authority.charAt(0) >= 'A' && authority.charAt(0) <= 'Z')
                 || authority.charAt(0) == '(') {
             return authority;
         } else {
             System.out.println("No authority found - " + name2);
             return null;
         }
     }
 
     private TaxonomyNomenclature genNewNomenclature(String taxon) {
         TaxonomyNomenclature nomenclature = new TaxonomyNomenclature();
         String taxonName = getTaxonName(taxon);
         System.out.println("taxonName : " + taxonName);
         nomenclature.setName(getPureName(taxonName));
         //System.out.println("name : " + getPureName(taxonName));
         nomenclature.setAuthority(getAuthority(taxonName));
         //System.out.println("authority : " + getAuthority(taxonName));
         nomenclature.setNameInfo(taxon);
         nomenclature.setHierarchy("FAMILY APIDAE; " + getPureName(taxonName) + " " + getAuthority(taxonName));
         nomenclature.setHierarchyClean("FAMILY APIDAE; " + getPureName(taxonName));
         nomenclature.setRank("Species");
         return nomenclature;
     }
     
     private TaxonomyNomenclature genNewNomenclatureForTitle(String taxon) {
         TaxonomyNomenclature nomenclature = new TaxonomyNomenclature();
         String taxonName = taxon;
        String pureTaxonName = "FAMILY APIDAE";
         System.out.println("taxonName : " + taxonName);
         nomenclature.setName(pureTaxonName);
         //System.out.println("name : " + getPureName(taxonName));
         nomenclature.setAuthority(null);
         //System.out.println("authority : " + getAuthority(taxonName));
         nomenclature.setNameInfo(taxonName);
         nomenclature.setHierarchy(taxonName);
         nomenclature.setHierarchyClean(taxonName);
         nomenclature.setRank("Family");
         return nomenclature;
     }
     
     private TaxonomyOtherInfo genNewOtherInfo(String otherinfo_str) {
         TaxonomyOtherInfo otherinfo = new TaxonomyOtherInfo();
         otherinfo.setOtherInfo(otherinfo_str);
         return otherinfo;
     }
     
     private TaxonomyGenericElement genNewElement(String title, String content) {
         TaxonomyGenericElement elem = new TaxonomyGenericElement();
         elem.setName(title);
         elem.setText(content);
         return elem;
     }
     
     private String[] splitTitleBody(String content) {
         int divPosition = 0;
         boolean braceStart = false;
         for(int i=0;i<content.length();i++) {
             if(content.charAt(i) == '(') {
                 braceStart = true;
             } else if(content.charAt(i) == ')') {
                 braceStart = false;
             } else if(content.charAt(i) == ':' || content.charAt(i) == '.' || content.charAt(i) == '-') {
                 // possible position
                 if(!braceStart) {
                     divPosition = i;
                     break;
                 }
             }
         }
 
         String[] split = new String[2];
         split[0] = content.substring(0, divPosition).trim();
         split[1] = content.substring(divPosition+1).trim();
         return split;
     }
     
     private String removeTrailingDot(String content) {
         int dotStart = content.length();
         for(int i=0;i<content.length();i++) {
             if(content.charAt(content.length() - 1 - i) == '.') {
                 dotStart = content.length() - 1 - i;
             } else {
                 break;
             }
         }
         
         return content.substring(0, dotStart).trim();
     }
     
     private String removeStartingDot(String content) {
         int dotEnd = 0;
         for(int i=0;i<content.length();i++) {
             if(content.charAt(i) == '.') {
                 dotEnd = i + 1;
             } else {
                 break;
             }
         }
         
         return content.substring(dotEnd).trim();
     }
     
     private String[] splitKeyStatement(String content) {
         String[] split1 = content.split("\\.{3,}");
         String first = removeTrailingDot(split1[0]);
         String second = removeStartingDot(split1[1]);
         
         String[] split = new String[3];
         
         Pattern p1 = Pattern.compile("^([-–]|\\d+)(\\.)?\\s(.+)$");
         Matcher mt1 = p1.matcher(first);
         if(mt1.find()) {
             split[0] = mt1.group(1).trim();
             split[1] = mt1.group(3).trim();
             split[2] = second;
         }
         
         return split;
     }
     
     private TaxonomySynonym genNewSynonym(String synonym) {
         TaxonomySynonym syn = new TaxonomySynonym();
         syn.setSynonym(synonym);
         return syn;
     }
     
     private KeyDiscussion genKeyDiscussion(String discussion) {
         KeyDiscussion diss = new KeyDiscussion();
         diss.setText(discussion);
         return diss;
     }
     
     private TaxonomyDiscussion genDiscussion(String discussion) {
         TaxonomyDiscussion diss = new TaxonomyDiscussion();
         diss.setText(discussion);
         return diss;
     }
     
     private TaxonomyDescription genNewDescription(String title, String content) {
         TaxonomyDescription desc = new TaxonomyDescription();
         desc.setType(TaxonomyDescription.TaxonomyDescriptionType.DESCRIPTION_GENERIC);
         desc.setTitle(title);
         desc.setDescription(content);
         return desc;
     }
     
     private KeyTo genKeyTo(String content) {
         KeyTo keyto = new KeyTo();
         KeyHeading heading = new KeyHeading();
         heading.setHeading(content);
         keyto.setHeading(heading);
         return keyto;
     }
     
     private KeyStatement genKeyStatement(String content) {
         KeyStatement statement = new KeyStatement();
         String[] split = splitKeyStatement(content);
         statement.setId(split[0]);
         statement.setStatement(split[1]);
         
         try {
             int nextStatementId = Integer.parseInt(split[2].substring(0, 1));
             statement.setNextStatementId(split[2]);
         } catch(Exception ex) {
             statement.setDetermination(split[2]);
         }
         
         return statement;
     }
     
     private String getFullTaxonName(String name, String authority) {
         String full = "";
         if(name != null) {
             full += name;
         }
         
         if(authority != null) {
             full += " " + authority;
         }
         
         return full.trim();
     }
     
     public void start(int documentID) throws IOException, Exception {
         this.documentID = documentID;
         this.document = loadDocument(this.documentID);
         this.paragraphs = loadParagraphs(document);
         
         List<Taxonomy> taxonomies = new ArrayList<Taxonomy>();
         List<KeyTo> keytos = new ArrayList<KeyTo>();
         
         Taxonomy taxonomy = null;
         KeyTo keyto = null;
         String prevTitle = null;
         for(Paragraph paragraph : paragraphs) {
             switch(paragraph.getType()) {
                 case PARAGRAPH_TAXONNAME:
                 {
                     taxonomy = new Taxonomy();
                     // set metadata
                     TaxonomyMeta meta = new TaxonomyMeta(document.getFilename());
                     taxonomy.setMeta(meta);
 
                     if(paragraph.getContent().startsWith("FAMILY APIDAE")) {
                         taxonomy.setNomenclture(genNewNomenclatureForTitle(paragraph.getContent()));
                     } else {
                         taxonomy.setNomenclture(genNewNomenclature(paragraph.getContent()));
                     }
                     // add to list
                     taxonomies.add(taxonomy);
                     break;
                 }
                 case PARAGRAPH_OTHERINFO:
                 {
                     if(taxonomy != null) {
                         taxonomy.getNomenclature().addOtherInfo(genNewOtherInfo(paragraph.getContent()));
                     }
                     break;
                 }
                 case PARAGRAPH_SYNONYM: 
                 {
                     if(taxonomy != null) {
                         taxonomy.addSynonym(genNewSynonym(paragraph.getContent()));
                     }
                     break;
                 }
                 case PARAGRAPH_DISCUSSION_NON_TITLED_BODY:
                 {
                     if(taxonomy != null) {
                         if(taxonomy.getDiscussion() == null) {
                             taxonomy.setDiscussion(new TaxonomyDiscussion());
                         }
 
                         taxonomy.addDiscussionNonTitled(genDiscussion(paragraph.getContent()));
                     }
                     break;
                 }
                 case PARAGRAPH_DISTRIBUTION_WITH_BODY:
                 {
                     if(taxonomy != null) {
                         String[] split = splitTitleBody(paragraph.getContent());
                         prevTitle = split[0];
 
                         taxonomy.addElement(genNewElement(prevTitle, split[1]));
                     }
                     break;
                 }
                 case PARAGRAPH_DESCRIPTION_WITH_BODY:
                 {
                     if(taxonomy != null) {
                         String[] split = splitTitleBody(paragraph.getContent());
                         prevTitle = split[0];
 
                         taxonomy.addDescription(genNewDescription(prevTitle, split[1]));
                     }
                     break;
                 }
                 case PARAGRAPH_SUBTITLE_WITH_BODY:
                 {
                     if(taxonomy != null) {
                         String[] split = splitTitleBody(paragraph.getContent());
                         prevTitle = split[0];
 
                         taxonomy.addElement(genNewElement(prevTitle, split[1]));
                     }
                     break;
                 }
                 case PARAGRAPH_KEY:
                 {
                     if(paragraph.getContent().startsWith("KEY TO SPECIES")) {
                         prevTitle = paragraph.getContent();
                     } else {
                         if(taxonomy != null) {
                             taxonomy.increaseKeyToTable();
                         }
                         
                         if(prevTitle == null) {
                             keyto = genKeyTo(paragraph.getContent());
                         } else {
                             keyto = genKeyTo(prevTitle + " - " + paragraph.getContent());
                         }
                         keytos.add(keyto);
                     }
                     break;
                 }
                 case PARAGRAPH_KEY_DISCUSSION:
                 {
                     if(keyto != null) {
                         keyto.addDiscussion(genKeyDiscussion(paragraph.getContent()));
                     }
                     break;
                 }
                 case PARAGRAPH_KEY_BODY:
                 {
                     if(keyto != null) {
                         keyto.addStatement(genKeyStatement(paragraph.getContent()));
                     }
                     break;
                 }
                 case PARAGRAPH_IGNORE:
                     break;
                 default:
                 {
                     System.err.println("Skipped - " + paragraph.getTypeString());
                     System.err.println(paragraph.getContent());
                     break;
                 }
             }
         }
         
         // to file
         File keyOutDir = new File("key");
         keyOutDir.mkdir();
         int keyfileIndex = 1;
         int allocKeyToTableNum = 0;
         for(KeyTo key : keytos) {
             File outKeyFile = new File(keyOutDir, StringUtil.getSafeFileName(keyfileIndex + ". " + key.getHeading().getHeading()) + ".xml");
             key.toXML(outKeyFile);
             keyfileIndex++;
             
             int sumKeyTo = 0;
             
             for(int i=0;i<taxonomies.size();i++) {
                 Taxonomy taxon = taxonomies.get(i);
                 int keyToNum =  taxon.getKeyToTableNumber();
                 sumKeyTo += keyToNum;
                 
                 if(allocKeyToTableNum < sumKeyTo) {
                     taxon.addKeyFile(new TaxonomyKeyFile(outKeyFile));
                     allocKeyToTableNum++;
                     break;
                 }
             }
         }
         
         File taxonOutDir = new File("taxon");
         taxonOutDir.mkdir();
         
         // taxon file
         int taxonfileIndex = 1;
         for(Taxonomy taxon : taxonomies) {
             TaxonomyNomenclature nomenclature = taxon.getNomenclature();
             if(nomenclature != null) {
                 File outTaxonFile = new File(taxonOutDir, StringUtil.getSafeFileName(taxonfileIndex + ". " + getFullTaxonName(nomenclature.getName(), nomenclature.getAuthority())) + ".xml");
                 taxon.toXML(outTaxonFile);
                 taxonfileIndex++;
             }
         }
     }
     
     public static void main(String[] args) throws Exception {
         Parse_11_Mitchell_Apidae_1962 obj = new Parse_11_Mitchell_Apidae_1962();
         obj.start(11);
     }
 }
