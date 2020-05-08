 package prodoc;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import com.sun.javadoc.Doc;
 import com.sun.javadoc.ExecutableMemberDoc;
 import com.sun.javadoc.ParamTag;
 import com.sun.javadoc.Parameter;
 import com.sun.javadoc.Tag;
 
 import static prodoc.StartDoclet.*;
 
 
 public abstract class BasicTemplate{
 	final HTML_Template fileTemplate;
 	
 	private final HTML_Template memberParameters;
 	
 	private final HTML_Template memberParameter;
 	
 	final TemplateTag TITLE_TAG = new TemplateTag("@title",true);
 	final TemplateTag LIBNAME_TAG = new TemplateTag("@libname",false);
 	final TemplateTag NAME_TAG = new TemplateTag("@name",true);
 	final TemplateTag EXAMPLE_TAG = new TemplateTag("@example",true);
 	final TemplateTag DESCRIPTION_TAG = new TemplateTag("@description",true);
 	final TemplateTag SYNTAX_TAG = new TemplateTag("@syntax",true);
 	final TemplateTag PARAMETERS_TAG = new TemplateTag("@parameters",true);
 	final TemplateTag PARAMETER_TAG = new TemplateTag("@parameter",true);
 	final TemplateTag PARAMETER_NAME_TAG = new TemplateTag("@name",true);
 	final TemplateTag PARAMETER_DESCRIPTION_TAG = new TemplateTag("@description",true);
 	final TemplateTag USAGE_TAG = new TemplateTag("@usage",true);
 	final TemplateTag RELATED_TAG = new TemplateTag("@related",true);
 	final TemplateTag RETURN_TAG = new TemplateTag("@return",true);
 	final TemplateTag MEMBER_RETURN_TAG = new TemplateTag("@return",true);
 	final TemplateTag LINKS_TAG = new TemplateTag("@links",true);
 	final TemplateTag FIELDS_TAG = new TemplateTag("@fields",false);
 	final TemplateTag METHODS_TAG = new TemplateTag("@methods",false);
 	final TemplateTag FIELD_TAG = new TemplateTag("@member",true);
 	final TemplateTag METHOD_TAG = new TemplateTag("@member",true);
 	final TemplateTag FIELD_LINK_TAG = new TemplateTag("@link",true);
 	final TemplateTag FIELD_NAME_TAG = new TemplateTag("@name",true);
 	final TemplateTag FIELD_DESCRIPTION_TAG = new TemplateTag("@description",true);
 	final TemplateTag METHOD_LINK_TAG = new TemplateTag("@link",true);
 	final TemplateTag METHOD_NAME_TAG = new TemplateTag("@name",true);
 	final TemplateTag METHOD_DESCRIPTION_TAG = new TemplateTag("@description",true);
 	final TemplateTag FIELD_KIND_TAG = new TemplateTag("@kind",true);
 	final TemplateTag METHOD_KIND_TAG = new TemplateTag("@kind",true);
 	
 	String fileName = "";
 	
 	BasicTemplate(String templateFileName) throws IOException, MissingTagException{
 		LIBNAME_TAG.setContent(libName);
 		fileTemplate = new HTML_Template(templateFileName,getFileTemplateDevideWords());
 		
 		List<TemplateTag> memberParametersDevideWords = new ArrayList<TemplateTag>();
 		memberParametersDevideWords.add(PARAMETER_TAG);
 		memberParameters = new HTML_Template("memberparameters.htm",memberParametersDevideWords);
 		
 		List<TemplateTag> memberParameterDevideWords = new ArrayList<TemplateTag>();
 		memberParameterDevideWords.add(PARAMETER_NAME_TAG);
 		memberParameterDevideWords.add(PARAMETER_DESCRIPTION_TAG);
 		memberParameter = new HTML_Template("memberparameter.htm",memberParameterDevideWords);
 	}
 	
 	void reset(){
 		TITLE_TAG.resetContent();
 		NAME_TAG.resetContent();
 		EXAMPLE_TAG.resetContent();
 		DESCRIPTION_TAG.resetContent();
 		SYNTAX_TAG.resetContent();
 		PARAMETERS_TAG.resetContent();
 		PARAMETER_TAG.resetContent();
 		PARAMETER_NAME_TAG.resetContent();
 		PARAMETER_DESCRIPTION_TAG.resetContent();
 		USAGE_TAG.resetContent();
 		RELATED_TAG.resetContent();
 		RETURN_TAG.resetContent();
 		LINKS_TAG.resetContent();
 		FIELDS_TAG.resetContent();
 		METHODS_TAG.resetContent();
 		FIELD_TAG.resetContent();
 		METHOD_TAG.resetContent();
 		METHOD_LINK_TAG.resetContent();
 		METHOD_NAME_TAG.resetContent();
 		METHOD_DESCRIPTION_TAG.resetContent();
 		FIELD_KIND_TAG.resetContent();
 		FIELD_LINK_TAG.resetContent();
 		FIELD_NAME_TAG.resetContent();
 		FIELD_DESCRIPTION_TAG.resetContent();
 		METHOD_KIND_TAG.resetContent();
 		fileName = "";
 	}
 	
 	abstract List<TemplateTag> getFileTemplateDevideWords();
 	abstract String getCompletedMember();
 	
 	/**
 	 * Saves the filled template as HTML File
 	 * @throws IOException
 	 */
 	void saveTemplate() throws IOException{
 		getCompletedMember();
 		fileTemplate.saveHtmFile(fileName,docFolder.getPath());
 		reset();
 	}
 
 	/**
 	 * Sets the description of the doc member
 	 * @param description, description that has to be add to the doc
 	 */
 	void setDescription(String description){
 		DESCRIPTION_TAG.setContent(description);
 	}
 
 	/**
	 * Reads the example file specified by the example tag of a doc member 
 	 * and adds it to the example section of the doc
 	 * @param doc
 	 * @throws IOException
 	 */
 	void setExample(Doc doc) throws IOException{
 		Tag[]exampleTag = doc.tags("@example");
 		if(exampleTag.length > 0){
 			StringBuffer exampleBuffer = new StringBuffer();
 			FileReader in;
 			int c;
 			String[] pathComponents = exampleTag[0].text().split( "/" );
 			String filePath = exampleTag[0].text() + "/" + pathComponents[ pathComponents.length - 1 ] + ".pde";
 			in = new FileReader(new File(exampleFolder,filePath));
 
 			while ((c = in.read()) != -1){
 				if((char)c=='<'){
 					exampleBuffer.append("&lt;");
 				}else{
 					exampleBuffer.append((char) c);
 				}
 			}
			
			in.close();
 
 			String exampleString = exampleBuffer.toString();
 			
 			EXAMPLE_TAG.setContent(exampleString);
 			
 		}else{
 			EXAMPLE_TAG.setContent("None available");
 		}
 	}
 
 	/**
 	 * Adds a methods signature to the Syntaxsection of the doc
 	 * @param memberSyntaxString Method Signature to add to the Syntaxsection
 	 */
 	private void addSyntax(String memberSyntaxString){
 		StringBuffer memberSyntaxBuffer = new StringBuffer();
 		memberSyntaxBuffer.append(memberSyntaxString);
 		memberSyntaxBuffer.append("\n");
 		SYNTAX_TAG.addContent(memberSyntaxBuffer.toString());
 	}
 	
 	/**
 	 * Adds a syntax to the syntaxsection
 	 * @param doc, doc item that has to be add to the syntax section
 	 */
 	void addSyntax(Doc doc){
 		if(doc.isConstructor() || doc.isMethod()){
 			StringBuffer syntaxBuffer = new StringBuffer();
 			for(Parameter parameter:((ExecutableMemberDoc) doc).parameters()){
 				syntaxBuffer.append(parameter.name());
 				syntaxBuffer.append(", ");
 			}
 			if(syntaxBuffer.length() > 2){
 				syntaxBuffer.delete(syntaxBuffer.length()-2,syntaxBuffer.length());
 			}
 
 			addSyntax(doc.name()+"("+syntaxBuffer.toString()+");");
 		}else if(doc.isField()){
 			addSyntax(doc.name()+";");
 		}
 	}
 
 	/**
 	 * Adds a parameter and its description to parametersection of the doc
 	 * @param parametername
 	 * @param parameterdescription
 	 */
 	private void addParameter(String parametername, String parameterdescription){
 		PARAMETER_NAME_TAG.setContent(parametername);
 		memberParameter.insertTagContent(PARAMETER_NAME_TAG);
 		PARAMETER_DESCRIPTION_TAG.setContent(parameterdescription);
 		memberParameter.insertTagContent(PARAMETER_DESCRIPTION_TAG);
 		PARAMETER_TAG.addContent(memberParameter.getTemplateContent());
 	}
 	
 	/**
 	 * Adds all parameters of an executable doc member to the documentation
 	 * @param doc
 	 */
 	void addParameters(Doc doc){
 		if(doc.isConstructor() || doc.isMethod()){
 			boolean hasParams = false;
 			for(ParamTag paramTag:((ExecutableMemberDoc) doc).paramTags()){
 				hasParams = true;
 				addParameter(paramTag.parameterName(),paramTag.parameterComment());
 			}
 			if(hasParams){
 				memberParameters.insertTagContent(PARAMETER_TAG);
 				PARAMETERS_TAG.setContent(memberParameters.getTemplateContent());
 			}
 		}
 	}
 	
 	/**
 	 * Sets the title for a doc item
 	 * @param title
 	 */
 	void setTitle(String memberTitle){
 		TITLE_TAG.setContent(memberTitle);
 	}
 
 	/**
 	 * Sets the usage for a doc item
 	 * @param doc
 	 */
 	void setUsage(Doc doc){
 		Tag[]usageTag = doc.tags("@usage");
 		if(usageTag.length > 0){
 			USAGE_TAG.setContent(usageTag[0].text());
 		}else{
 			USAGE_TAG.setContent("Web & Application");
 		}
 	}
 
 	/**
 	 * Adds all related members of a doc item to the related section of the doc
 	 * @param doc
 	 */
 	void addRelatedmember(Doc doc,HashMap<String,String> fieldLinks,HashMap<String,String> methodLinks){
 		Tag[] relatedTags = doc.tags("@related");
 		for (Tag relatedTag : relatedTags){
 			String link = "";
 			if(fieldLinks.containsKey(relatedTag.text())){
 				link = fieldLinks.get(relatedTag.text());
 			}else if(methodLinks.containsKey(relatedTag.text())){
 				link = methodLinks.get(relatedTag.text());
 			}else if(StartDoclet.classLinks.containsKey(relatedTag.text())){
 				link = StartDoclet.classLinks.get(relatedTag.text());
 			}else{
 				System.out.println("No related member found for @related "+relatedTag.text());
 			}
 			StringBuffer relatedBuffer = new StringBuffer();
 			relatedBuffer.append("<A href=\"");
 			relatedBuffer.append(link);
 			relatedBuffer.append(".html\">");
 			relatedBuffer.append(relatedTag.text());
 			relatedBuffer.append("</A><BR>\n");
 			RELATED_TAG.addContent(relatedBuffer.toString());
 		}
 	}
 	
 	void setName(String memberName){
 		NAME_TAG.setContent(memberName);
 	}
 	
 	/**
 	 * Build the filename for a doc object and returns it
 	 * @param doc
 	 * @param className
 	 * @return
 	 */
 	String buildFileName(Doc doc, String className){
 		String type;
 
 		if (doc.isMethod()){
 			type = "_method_";
 		}else if (doc.isClass()){
 			type = "_class_";
 		}else if (doc.isField()){
 			type = "_field_";
 		}else{
 			type = "_";
 		}
 		return className.toLowerCase() + type + doc.name().toLowerCase();
 	}
 	/**
 	 * Sets the file name for html docfile
 	 * @param doc
 	 * @param className
 	 */
 	void setFileName(Doc doc, String className){
 		fileName = buildFileName(doc,className);
 	}
 
 }
