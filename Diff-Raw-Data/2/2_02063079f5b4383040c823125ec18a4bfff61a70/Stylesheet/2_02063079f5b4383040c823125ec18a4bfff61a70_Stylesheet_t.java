 package frostillicus.dxl;
 
 import java.io.*;
 import javax.xml.xpath.XPathExpressionException;
 import com.ibm.xsp.extlib.util.ExtLibUtil;
 import com.raidomatic.xml.*;
 
 import frostillicus.FNVUtil;
 import sun.misc.BASE64Encoder;
 import sun.misc.BASE64Decoder;
 import lotus.domino.*;
 //import org.apache.commons.codec.binary.Base64;
 
 public class Stylesheet extends AbstractDXLDesignNote {
 	private static final long serialVersionUID = -3543549758559295423L;
 	public Stylesheet(String databaseDocumentId, String designDocumentId) throws Exception {
 		super(databaseDocumentId, designDocumentId);
 	}
 
 	public String getContent() throws XPathExpressionException, UnsupportedEncodingException, IOException {
 		String fileData = this.getRootNode().selectSingleNode("/stylesheetresource/filedata").getTextContent();
 
 		//return Base64Coder.decodeString(fileData);
 		return new String(new BASE64Decoder().decodeBuffer(fileData), "UTF-8");
 		//return new String(Base64.decodeBase64(fileData), "UTF-8");
 	}
 	public void setContent(String content) throws XPathExpressionException {
 		XMLNode dataNode = this.getRootNode().selectSingleNode("/stylesheetresource/filedata");
		dataNode.setTextContent(new BASE64Encoder().encodeBuffer(content.getBytes()).replace("\r", ""));
 		//dataNode.setTextContent(Base64.encodeBase64String(content.getBytes()));
 	}
 
 	public static String create(String databaseDocumentId, String name) throws Exception {
 		DxlImporter importer = null;
 		try {
 			// Designer is case-sensitive too
 			if(!name.endsWith(".css")) {
 				name = name + ".css";
 			}
 
 			InputStream is = Stylesheet.class.getResourceAsStream("/frostillicus/dxl/stylesheet.xml");
 			BufferedReader reader = new BufferedReader(new InputStreamReader(is));
 			StringBuilder xmlBuilder = new StringBuilder();
 			while(reader.ready()) {
 				xmlBuilder.append(reader.readLine());
 				xmlBuilder.append("\n");
 			}
 			is.close();
 			String xml = xmlBuilder.toString().replace("name=\"\"", "name=\"" + FNVUtil.xmlEncode(name) + "\"");
 
 			importer = ExtLibUtil.getCurrentSession().createDxlImporter();
 			importer.setDesignImportOption(DxlImporter.DXLIMPORTOPTION_REPLACE_ELSE_CREATE);
 			importer.setReplicaRequiredForReplaceOrUpdate(false);
 			Document databaseDoc = ExtLibUtil.getCurrentDatabase().getDocumentByUNID(databaseDocumentId);
 			Database foreignDB = ExtLibUtil.getCurrentSessionAsSignerWithFullAccess().getDatabase(databaseDoc.getItemValueString("Server"), databaseDoc.getItemValueString("FilePath"));
 			importer.importDxl(xml, foreignDB);
 
 			Document importedDoc = foreignDB.getDocumentByID(importer.getFirstImportedNoteID());
 			return importedDoc.getUniversalID();
 		} catch(Exception e) {
 			e.printStackTrace();
 			if(importer != null) {
 				System.out.println(importer.getLog());
 			}
 		}
 		return null;
 	}
 }
