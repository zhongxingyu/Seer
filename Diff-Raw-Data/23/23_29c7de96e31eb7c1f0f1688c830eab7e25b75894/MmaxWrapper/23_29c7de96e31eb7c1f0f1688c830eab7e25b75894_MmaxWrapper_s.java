 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.commons.lang3.StringEscapeUtils;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 public class MmaxWrapper {
 	
 	/**
 	 * Arguments
 	 */
 	static String importConll = "";
 	static String exportConll = "";
 	
 	static String mmaxPath = "";
 	static String project = "";
 	static String forNer = "";
 	
 	
 	static String MMAX_STRING = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
 			"<mmax_project>\n" +
 			"<words>%s_words.xml</words>\n" +
 			"<keyactions></keyactions>\n" +
 			"<gestures></gestures>\n" +
 			"</mmax_project>\n";
 	
 	static String WORDS_HEADER_STRING = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
 			"<!DOCTYPE words SYSTEM \"words.dtd\">\n" +
 			"<words>";
 	
 	static String MENTION_HEADER_STRING = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
 			"<!DOCTYPE markables SYSTEM \"markables.dtd\">\n" +
 			"<markables xmlns=\"www.eml.org/NameSpaces/coref\">\n";
 	
 	static String SENTENCE_HEADER_STRING = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
 			"<!DOCTYPE markables SYSTEM \"markables.dtd\">\n" +
 			"<markables xmlns=\"www.eml.org/NameSpaces/sentence\">\n";
 	
 
     public void createProjectFile(String fileName) throws IOException{
         BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName),"UTF-8"));
         writer.write(String.format(MMAX_STRING, project));
         writer.flush();
         writer.close();
     }
     
     public void importWords(Conll conll, String fileName) throws IOException {
      	BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName),"UTF-8"));
         Utils.toWriter(writer, WORDS_HEADER_STRING);
         for (int token_i = 0; token_i < conll.getSize(); token_i++) {
         	ConllToken token = conll.getToken(token_i);
         	String word = token.getField(1);
         	Utils.toWriter(writer, "<word id=\"word_"+(token_i+1)+"\">"+StringEscapeUtils.escapeXml(word) + "</word>\n");
         }
         Utils.toWriter(writer, "</words>");
         writer.flush();
         writer.close();
     }
     
     public void importMentions(Conll conll, String fileName) throws IOException {
      	BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName),"UTF-8"));
         Utils.toWriter(writer, MENTION_HEADER_STRING);
         
 //        for (Mention m : d.mentions) {
 //            String span = "";
 //            if (m.start == m.end) span = "word_"+(m.start+1);
 //            else span = "word_"+(m.start+1)+".."+"word_"+(m.end+1);
 //            String coref_class = "";
 //            if (d.corefClusters.get(m.corefClusterID).corefMentions.size() > 1) coref_class = "set_"+m.corefClusterID;
 //            else coref_class = "empty";
 //            String category = "other";
 //            if (m.category != null) {
 //                if (m.category.equals("ORG")) category = "organization";
 //                else if (m.category.equals("LOCATION")) category = "location";
 //                else if (m.category.equals("PERSON")) category = "person";
 //            }
 //            
 //            Utils.toWriter(writer, "<markable id=\"markable_"+(m.id+1)+"\" span=\""+span+"\" coref_class=\""+coref_class+"\" category=\""+category+"\" mmax_level=\"coref\"  rule=\"none\"  type=\"none\"/>\n");
 //        }
         Utils.toWriter(writer, "</markables>");
         writer.flush();
         writer.close();
     }
     
     public void createMarkableLevel(String fileName) throws IOException {
      	BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName),"UTF-8"));
         Utils.toWriter(writer, MENTION_HEADER_STRING);
         Utils.toWriter(writer, "</markables>");
         writer.flush();
         writer.close();
     }
     
     public void importSentences(Conll conll, String fileName) throws IOException {
      	BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName),"UTF-8"));
         Utils.toWriter(writer, SENTENCE_HEADER_STRING);
         
         int start = 0;
         int end;
         int sent_id = 0;
         for (int token_i = 0; token_i < conll.getSize(); token_i++) {
         	ConllToken token = conll.getToken(token_i);
         	if (token.isSentStart()) {
         		start = token.id+1;
         	}
         	if (token.isSentEnd()) {
         		end = token_i+1;
                 sent_id++;
                 String span = "word_"+start;
                 if (end != start) span += "..word_"+end;
                 start = end + 1;
                 Utils.toWriter(writer, "<markable mmax_level=\"sentence\" id=\"markable_"+sent_id+"\" span=\""+span+"\" />\n");
         	}
         }
         Utils.toWriter(writer, "</markables>");
         writer.flush();
         writer.close();
     }
     
     
     public void exportTokens(Conll conll, String fileName) throws IOException {
     	BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName),"UTF-8"));
     	for (int token_i = 0; token_i < conll.getSize(); token_i++) {
 	    	ConllToken token = conll.getToken(token_i);
 	    	if (token.isSentStart() && token.id != 0) {
 	    		writer.write("\n");
 	    	}
 	    	writer.write(Utils.implode(token.fields, "\t") + "\n");
     	}
     	writer.flush();
     	writer.close();
     }
     
     
     public void exportForNer(Conll conll, String fileName) throws IOException {
     	BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName),"UTF-8"));
    	String eol = System.getProperty("line.separator");
        StringBuilder s = new StringBuilder();
        ConllToken ct;
         for (int token_i = 0; token_i < conll.getSize(); token_i++) {
        	ct = conll.getToken(token_i);
             s.append(ct.fields.get(1)); s.append("\t");
            s.append(ct.fields.get(3).charAt(0)); s.append("\t");
             s.append(ct.fields.get(2)); s.append("\t");
            s.append(ct.fields.get(4)); s.append("\t");
            s.append(ct.fields.get(6)); s.append("\t");
             s.append(ct.fields.get(5)); s.append("\t");
             s.append(eol);
             if (ct.isSentEnd()) s.append(eol);
            writer.write(s.toString());	    	
     	}
     	writer.flush();
     	writer.close();
     }
     
     
     public void readWordLevel(Conll conll, String fileName) throws SAXException, IOException, ParserConfigurationException {
     	File mmax_file = new File(fileName);
         DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
 
         org.w3c.dom.Document doc = dBuilder.parse(mmax_file);
         NodeList markables = doc.getElementsByTagName("word");
         
         for (int i = 0; i < markables.getLength(); i++) {
             org.w3c.dom.Node markable = markables.item(i);            
             String word = markable.getFirstChild().getNodeValue();
             String ids = markable.getAttributes().getNamedItem("id").getNodeValue();
             int id = Integer.parseInt(ids.substring(5))-1;
             ConllToken ct = new ConllToken(conll, id);
             ct.addField(word);
             conll.addToken(ct);
         }
     }
     
     public void addSenteceLevel(Conll conll, String fileName) throws SAXException, IOException, ParserConfigurationException {
     	File mmax_file = new File(fileName);
         DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
 
         org.w3c.dom.Document doc = dBuilder.parse(mmax_file);
         NodeList markables = doc.getElementsByTagName("markable");
         
         for (int i = 0; i < markables.getLength(); i++) {
         	org.w3c.dom.Node markable = markables.item(i);   
         	String span = markable.getAttributes().getNamedItem("span").getNodeValue();
             
             String[] intervals = span.split(",");
             String[] interval = intervals[0].split("\\.\\.");
             int start = Integer.parseInt(interval[0].substring(5)) - 1 ;
             
             conll.getToken(start).setSentStart();
         }
     }
     
     public void addMarkableLevel(Conll conll, String fileName) throws ParserConfigurationException, SAXException, IOException {
     	File mmax_file = new File(fileName);
         DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
 
         org.w3c.dom.Document doc = dBuilder.parse(mmax_file);
         NodeList markables = doc.getElementsByTagName("markable");
         
         for (int i = 0; i < markables.getLength(); i++) {
         	org.w3c.dom.Node markable = markables.item(i);   
         	String span = markable.getAttributes().getNamedItem("span").getNodeValue();
         	String category = markable.getAttributes().getNamedItem("category").getNodeValue();
         	
         	String[] intervals = span.split(",");
             String[] interval = intervals[0].split("\\.\\.");
             int start = Integer.parseInt(interval[0].substring(5)) - 1 ;
             int end = start;
             if (interval.length > 1) {
                 end = Integer.parseInt(interval[1].substring(5)) - 1;
             }
             
             conll.setMention(start, end, category);
         }
         
         ConllToken ct;
         for (int i = 0; i < conll.getSize(); i++) {
         	ct = conll.getToken(i);
         	ct.addField(ct.category);
         	if (ct.cat_start) ct.addField("START");
         	else ct.addField("_");
         	if (ct.cat_end) ct.addField("END");
         	else ct.addField("_");
         }
     }
     
 	
 	public static void main(String[] args) throws Exception {
 		
 		for (int i = 0; i < args.length; i++) {			
 			if (args[i].equalsIgnoreCase("-conll")) importConll = Utils.attr(args[i+1], "-conll");
 			if (args[i].equalsIgnoreCase("-exportConll")) exportConll = Utils.attr(args[i+1], "-exportConll");
 			if (args[i].equalsIgnoreCase("-mmaxPath")) mmaxPath = Utils.attr(args[i+1], "-mmaxPath");
 			if (args[i].equalsIgnoreCase("-project")) project = Utils.attr(args[i+1], "-project");
 			if (args[i].equalsIgnoreCase("-ner")) forNer = Utils.attr("true", "-ner");
 			
 			if (args[i].equalsIgnoreCase("-h") || args[i].equalsIgnoreCase("--help") || args[i].equalsIgnoreCase("-?")) {
 				System.out.println("MMAX2 wrapper");
 				System.out.println("\n\t-cnll : base conll data");
 				System.out.println("\n\t-exportConll : added extra annotation columns");
 				System.out.println("\n\t-mmaxPath : path to mmax project");
 				System.out.println("\n\t-project : project name");
 				System.out.println("\n\t-ner : flag wheter to use function exportForNer()");
 				System.out.flush();
 				System.exit(0);
 			}
 		}
 		
 		MmaxWrapper mw = new MmaxWrapper();
 		if (exportConll.length() > 0) {			
 			String words = mmaxPath+project+"_words.xml";
 	        String coref_level = mmaxPath+project+"_coref_level.xml";
 	        String sentence_level = mmaxPath+project+"_sentence_level.xml";
 	        
 	        Conll conll = new Conll();
 	        if (importConll.length() > 0) {
 	        	conll.readConll(importConll);
 	        } else {
 	        	mw.readWordLevel(conll, words);
 		        mw.addSenteceLevel(conll, sentence_level);
 	        }
 	        mw.addMarkableLevel(conll, coref_level);
 	        if (forNer.length() > 0) {
 	        	mw.exportForNer(conll, exportConll);	        	
 	        } else {
 	        	mw.exportTokens(conll, exportConll);
 	        }
 		} else if (importConll.length() > 0) {
 	        String words = mmaxPath+project+"_words.xml";
 	        String coref_level = mmaxPath+project+"_coref_level.xml";
 	        String sentence_level = mmaxPath+project+"_sentence_level.xml";
 	        String project_file = mmaxPath+project+".mmax";
 	        
 	        Conll conll = new Conll();
 	        conll.readConll(importConll);
 	        mw.createProjectFile(project_file);
 	        mw.importWords(conll, words);
 	        mw.createMarkableLevel(coref_level);
 	        mw.importSentences(conll, sentence_level);
 		}      
     }
 }
