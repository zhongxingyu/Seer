 package prodoc;
 
 import java.io.*;
 import java.util.*;
 
 import static prodoc.StartDoclet.*;
 
 public class HTML_Template{
 	private final String template;
 	private final String templateFile;
 	private final List<String> templateParts = new ArrayList<String>();
 	
 	HTML_Template(String templateFile,List<TemplateTag> tags) throws IOException, MissingTagException{
 		this.templateFile = templateFile;
 		template = getTemplate(templateFile);
 		devideVorlage(tags);
 	}
 
 	private int devidePointer = 0;
 
 	/**
 	 * This method takes a Template and changes it in a String
 	 * @param templateUrl Url to the template that has to be read
 	 * @return the template as String
 	 * @throws IOException
 	 */
 	private String getTemplate(String templateFile) throws IOException{
 		StringBuffer templateBuffer = new StringBuffer();
 		FileReader in;
 		int c;
 		in = new FileReader(new File(templateFolder,templateFile));
 
 		while ((c = in.read()) != -1){
 			templateBuffer.append((char) c);
 		}
 
 		String templateString = templateBuffer.toString();
 		if (templateString.indexOf("startCell") == (-1)){
 			return templateString;
 		}else{
 			templateString = templateString.substring(templateString.indexOf("startCell") + 12, templateString.indexOf("<!--endCell"));
 			return templateString;
 		}
 	}
 
 	/**
 	 * This method seperates a substring from the template according to a given
 	 * devideword.
 	 * 
 	 * @param devideWord
 	 *            marks the position where the template has to be split
 	 * @return A seperated String from the template
 	 */
 	private String getHtmPart(TemplateTag tag){
 		String returnString;
 		returnString = template.substring(devidePointer, tag.startposition);
 		devidePointer = tag.endposition;
 		return returnString;
 	}
 
 	/**
 	 * This method devides the template at the given devideWords to insert the
 	 * content.
 	 * 
 	 * @param devideWord[]
 	 *            holds the words where the template has to be split
 	 * @return an Object containing the String befor and after the divideWord.
 	 * @throws MissingTagException 
 	 */
 	private void devideVorlage(List<TemplateTag> tags) throws MissingTagException{
 		TreeSet<TemplateTag> sortedTags = new TreeSet<TemplateTag>();
 		for (TemplateTag tag:tags){
 			int position = template.indexOf(tag.name);
 			if(position > -1){
 				tag.setPositions(position);
 				tag.found = true;
 				sortedTags.add(tag);
 			}else{
 				if(tag.required){
 					throw new MissingTagException(tag,templateFile);
 				}
 			}
 		}
 		devidePointer = 0;
 		for (TemplateTag tag:sortedTags){
 			templateParts.add(getHtmPart(tag));
 			tag.templatePosition = templateParts.size();
 			templateParts.add("");
 		}
 		templateParts.add(template.substring(devidePointer, template.length()));
 	}
 	
 	/**
 	 * Inserts the content for a tag into the template
 	 * @param tag
 	 */
 	void insertTagContent(TemplateTag tag){
 		if(tag.found){
 			templateParts.set(tag.templatePosition,tag.getContent());
 		}
 	}
 	
 	/**
 	 * Returns the the filled Template as String
 	 * @return
 	 */
 	String getTemplateContent(){
 		StringBuffer templateBuffer = new StringBuffer();
 		for(String templatePart:templateParts){
 			templateBuffer.append(templatePart);
 		}
 		return templateBuffer.toString();
 	}
 
 	/**
 	 * Saves the filled template
 	 * @param HtmFileName
 	 * @throws IOException
 	 */
 	void saveHtmFile(String htmFileName, String path) throws IOException
 	{
 		String filePath = path + "/" + htmFileName + ".html";
 		FileWriter out = new FileWriter( filePath );
 		out.write(getTemplateContent());
 		out.close();
 	}
 }
