 package RDFaAnnotator.main;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import baxtree.btr.LabelFinder;
 
 import com.hp.hpl.jena.query.Query;
 import com.hp.hpl.jena.query.QueryExecution;
 import com.hp.hpl.jena.query.QueryExecutionFactory;
 import com.hp.hpl.jena.query.QueryFactory;
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.hp.hpl.jena.rdf.model.Model;
 import com.hp.hpl.jena.rdf.model.ModelFactory;
 import com.hp.hpl.jena.rdf.model.RDFNode;
 import com.hp.hpl.jena.rdf.model.Resource;
 
 public class TemplateGenerater {
 
 	protected String template_str = "";
 	protected String endpoint_url = "";
 	protected String topic_uri;
 	protected Model model;
 	protected String img_formats = ".avi|.bmp|.emf|.gif|.jpg|.jpeg|.mov|.mpg|.mpeg|.png|.wmf|.xbm";
 	protected String page_formats = ".html|.htm|.shtml|.asp|.jsp|.cgi|.php";
 	
 	public TemplateGenerater(String endpoint_url, String topic_uri, String endpoint_name){
 		this.endpoint_url = endpoint_url;
 		this.topic_uri = topic_uri;
 	}
 	
 	public TemplateGenerater(Model model, String topic_uri){
 		this.model = model;
 		this.topic_uri = topic_uri;
 	}
 	
 	public TemplateGenerater(String rdf_url, String topic_uri){
 		this.model = ModelFactory.createDefaultModel();
 		this.model = RDFModelLoader.loadTriplesFromURL(rdf_url);
 		this.topic_uri = topic_uri;
 	}
 	
 	public String createTemplateFromContext(String template_name){
 //		String topic_local_name = this.model.getResource(this.topic_uri).getLocalName();
 //		if(topic_local_name != null && !topic_local_name.equals(""))
 //			template_str += "<div about=\"${topic.topicuri}\">about: <a href=\"${topic.topicuri}\">"+topic_local_name+"</a><br/>\r\n\r\n";
 //		else
 			template_str += "<div about=\"${topic.topicuri}\">about: <a href=\"${topic.topicuri}\" onclick=\"javascript:return false;\">${topic.topicuri}</a><br/>\r\n\r\n";
 		//topic ?p ?o
 		String preprouri = "";
 		String querystr = 	
 			"	SELECT ?p ?o" +
 			"	{" +
 			"		<"+this.topic_uri+"> ?p	?o." +
 			"	}" +
 			"	ORDER BY ?p";
 			Query query = QueryFactory.create(querystr);
 			QueryExecution qe = QueryExecutionFactory.create(query, model);
 			ResultSet results = qe.execSelect();
 			qe.close();	
 		for(;results.hasNext();){
 			QuerySolution qs = results.nextSolution();
 			Resource property = (Resource) qs.get("p");
 			if(property.getURI().equalsIgnoreCase(preprouri)){
 				continue;
 			}
 			else{
 				preprouri = property.getURI();
 				RDFNode object = qs.get("o");
 				String pro_local_name = property.getLocalName();
 				String pro_node_name = (model.getNsURIPrefix(property.getNameSpace())+"_"+pro_local_name + "rel").replace("-", "$dash$");;
 				String pro_curie_name = model.getNsURIPrefix(property.getNameSpace())+":"+pro_local_name;
 				if(object.isLiteral()){
 					template_str += "<#if topic."+ pro_node_name +"??>\r\n" +
 								"	<#list topic."+pro_node_name+"?keys as key>\r\n" +
 								"		<#if topic."+ pro_node_name +"[key].val??>\r\n" +
 								getLiteralStyle(pro_local_name, preprouri)+
 								"			<span class=\"rdfasnippet\"><span property=\""+ pro_curie_name +"\"<#if topic."+ pro_node_name +"[key].dat??> datatype=\"${topic."+ pro_node_name +"[key].dat}\"</#if><#if topic."+ pro_node_name +"[key].lan??> xml:lang=\"${topic."+ pro_node_name +"[key].lan}\"</#if>>${topic."+ pro_node_name +"[key].val}</span></span><br/>\r\n" +
 								"		</#if>\r\n" +
 								"	</#list>\r\n" +
 								"</#if>\r\n\r\n";
 				}
 				else if(object.isResource()){
 					String web_resource_tags = "";
 					Resource resource = (Resource)object;
 					if(object.isURIResource()){
 						String object_uri = resource.getURI();
 						String object_local_name = resource.getLocalName();
 						int index = 0;
 						if(object_uri.indexOf(".") != -1)
 							index = object_uri.lastIndexOf(".");
 						String expension_name = object_uri.substring(index);
						if(img_formats.indexOf(expension_name) != -1){
 								web_resource_tags = "<span class=\"rdfasnippet\"><img rev=\""+ pro_curie_name +"\" src=\"${topic."+ pro_node_name +"[key].uri}\" resource=\""+ this.topic_uri +"\" alt=\"\" width=\"200px\"></img></span><br/>\r\n";
 						}
						else if(page_formats.indexOf(expension_name) != -1){
 								web_resource_tags = "<span class=\"rdfasnippet\"><a rel=\""+ pro_curie_name +"\" href=\"${topic."+ pro_node_name +"[key].uri}\" onclick=\"javascript:return false;\">${topic."+ pro_node_name +"[key].uri}</a></span><br/>\r\n";
 						}
 						else if(pro_curie_name.indexOf("license") != -1 || pro_curie_name.indexOf("License") != -1 || pro_curie_name.indexOf("LICENSE") != -1){
 								web_resource_tags = "<span class=\"rdfasnippet\"><a rel=\""+ pro_curie_name +"\" href=\"${topic."+ pro_node_name +"[key].uri}\" onclick=\"javascript:return false;\">${topic."+ pro_node_name +"[key].uri}</a></span><br/>\r\n";
 						}
 						else{
 								web_resource_tags = "<span class=\"rdfasnippet\"><a rel=\""+ pro_curie_name +"\" href=\"${topic."+ pro_node_name +"[key].uri}\" onclick=\"javascript:return false;\">${topic."+ pro_node_name +"[key].uri}</a></span><br/>\r\n";
 						}
 					}
 					/*@TODO need test
 					else if(object.isAnon()){
 						web_resource_tags = "<a rel=\""+ pro_curie_name +"\">" +
 											"	<#if topic."+ pro_node_name +"[key].bnojbpro??>" +
 											"		<#if topic."+ pro_node_name +"[key].bnuri??>" +
 											"			<a rel=\"${topic."+ pro_node_name +"[key].bnojbpro}\" resource=\"${topic."+ pro_node_name +"[key].bnuri}\">${topic."+ pro_node_name +"[key].bnuri}</a><br/>\r\n" +
 											"		</#if>" +
 											"	</#if>" +
 											"	<#if topic."+ pro_node_name +"[key].bndatpro??>" +
 											"		<#if topic."+ pro_node_name +"[key].bnlex??>" +
 											"			<a property=\"${topic."+ pro_node_name +"[key].bndatpro}\"<#if topic."+ pro_node_name +"[key].bndat??> datatype=\"${topic."+ pro_node_name +"[key].bndat}\"</#if><#if topic."+ pro_node_name +"[key].bnlan??> xml:lang=\"${topic."+ pro_node_name +"[key].bnlan}\"</#if>></a><br/>\r\n" +
 											"		</#if>" +
 											"	</#if>";
 					}
 					*/
 					else{
 						web_resource_tags = "<span class=\"rdfasnippet\"><a rel=\""+ pro_curie_name +"\" href=\"${topic."+ pro_node_name +"[key].uri}\" onclick=\"javascript:return false;\">${topic."+ pro_node_name +"[key].uri}</a></span><br/>\r\n";
 					}
 					template_str += "<#if topic."+ pro_node_name +"??>\r\n" +
 								"	<#list topic."+pro_node_name+"?keys as key>\r\n" +
 								"		<#if topic."+ pro_node_name +"[key].uri??>\r\n" +
 								getResourceStyle(pro_local_name, preprouri, true) +
 								web_resource_tags +
 								"		</#if>\r\n" +
 								"	</#list>\r\n" +
 								"</#if>\r\n\r\n";
 				}
 			}
 		}
 		
 		preprouri = "";
 		
 		//?s ?p topic			copied and pasted codes here and it needs refactoring
 		String querystr2 = 	
 			"	SELECT ?s ?p" +
 			"	{" +
 			"		?s ?p <"+this.topic_uri+">." +
 			"	}" +
 			"	ORDER BY ?p";
 			Query query2 = QueryFactory.create(querystr2);
 			QueryExecution qe2 = QueryExecutionFactory.create(query2, model);
 			ResultSet results2 = qe2.execSelect();
 			qe2.close();	
 		for(;results2.hasNext();){
 			QuerySolution qs = results2.nextSolution();
 			Resource property = (Resource) qs.get("p");
 			if(property.getURI().equalsIgnoreCase(preprouri)){
 				continue;
 			}
 			else{
 				preprouri = property.getURI();
 				Resource subject = (Resource) qs.get("s");
 				String pro_local_name = property.getLocalName();
 				String pro_node_name = (model.getNsURIPrefix(property.getNameSpace())+"_"+pro_local_name + "rev").replace("-", "$dash$");
 				String pro_curie_name = model.getNsURIPrefix(property.getNameSpace())+":"+pro_local_name;
 				if(subject.isResource()){
 					Resource resource = subject;
 					String web_resource_tags = "";
 					if(subject.isURIResource()){
 						String object_uri = resource.getURI();
 						String object_local_name = resource.getLocalName();
 						int index = 0;
 						if(object_uri.indexOf(".") != -1)
 							index = object_uri.lastIndexOf(".");
 						String expension_name = object_uri.substring(index);
						if(img_formats.indexOf(expension_name) != -1){
 								web_resource_tags = "<span class=\"rdfasnippet\"><img rel=\""+ pro_curie_name +"\" src=\"${topic."+ pro_node_name +"[key].uri}\" resource=\""+ this.topic_uri +"\" alt=\"\" width=\"200px\"></img></span><br/>\r\n";
 						}
						else if(page_formats.indexOf(expension_name) != -1){
 								web_resource_tags = "<span class=\"rdfasnippet\"><a rev=\""+ pro_curie_name +"\" href=\"${topic."+ pro_node_name +"[key].uri}\" onclick=\"javascript:return false;\">${topic."+ pro_node_name +"[key].uri}</a></span><br/>\r\n";
 						}
 						else if(pro_curie_name.indexOf("license") != -1 || pro_curie_name.indexOf("License") != -1 || pro_curie_name.indexOf("LICENSE") != -1){
 								web_resource_tags = "<span class=\"rdfasnippet\"><a rev=\""+ pro_curie_name +"\" href=\"${topic."+ pro_node_name +"[key].uri}\" onclick=\"javascript:return false;\">${topic."+ pro_node_name +"[key].uri}</a></span><br/>\r\n";
 						}
 						else{
 								web_resource_tags = "<span class=\"rdfasnippet\"><a rev=\""+ pro_curie_name +"\" href=\"${topic."+ pro_node_name +"[key].uri}\" onclick=\"javascript:return false;\">${topic."+ pro_node_name +"[key].uri}</a></span><br/>\r\n";
 						}
 					}
 					else{
 							web_resource_tags = "<span class=\"rdfasnippet\"><a rev=\""+ pro_curie_name +"\" href=\"${topic."+ pro_node_name +"[key].uri}\" onclick=\"javascript:return false;\">${topic."+ pro_node_name +"[key].uri}</a></span><br/>\r\n";
 					}
 					template_str += "<#if topic."+ pro_node_name +"??>\r\n" +
 								"	<#list topic."+pro_node_name+"?keys as key>\r\n" +
 								"		<#if topic."+ pro_node_name +"[key].uri??>\r\n" +
 								getResourceStyle(pro_local_name, preprouri, false) +
 								web_resource_tags +
 								"		</#if>\r\n" +
 								"	</#list>\r\n" +
 								"</#if>\r\n\r\n";
 				}
 			}
 			
 		}
 //		System.out.println(template_str); 
 		template_str += "</div>\r\n" +
 						//"<iframe name=\"dereference\" style=\"display:none\"></iframe>\r\n" +
 						"\r\n";
 		return template_str;
 	}
 	
 	public String getLiteralStyle(String property_name, String property_uri){
 //		property_name = "[[<span style=\"color:blue\">"+property_name+"</span>]]";
 		property_name = "[<a href=\""+property_uri+"\" onclick=\"javascript:return false;\">" + property_name +"</a> &gt;&gt;] ";
 		return property_name;
 	}
 	
 	public String getResourceStyle(String property_name, String property_uri, boolean rel_flag){
 //		property_name = "{{<span style=\"color:green\">"+property_name+"</span>}}";
 		if(rel_flag)
 			property_name = "[<a href=\""+property_uri+"\" onclick=\"javascript:return false;\">" + property_name +"</a> &gt;&gt;] ";
 		else
 			property_name = "[<a href=\""+property_uri+"\" onclick=\"javascript:return false;\">" + property_name +"</a> &lt;&lt;] ";
 		return property_name;
 	}
 	
 	public static void main(String [] args){
 		Model model = ModelFactory.createDefaultModel();
 		String rdf_url = "http://richard.cyganiak.de/foaf.rdf";
 		String topic_uri = "http://richard.cyganiak.de/foaf.rdf#cygri";
 		try{
 			URL url = new URL(rdf_url);
 			HttpURLConnection con = (HttpURLConnection)url.openConnection();
 			con.setRequestProperty("Accept", "application/rdf+xml");
 			InputStream is = con.getInputStream();
 			InputStreamReader isr = new InputStreamReader(is, "UTF-8");
 			model.read(isr, rdf_url, null);
 		}
 		catch (MalformedURLException murle){
 			murle.printStackTrace();
 		}
 		catch(FileNotFoundException fnfe){
 			fnfe.printStackTrace();
 		}
 		catch(IOException ioe){
 			ioe.printStackTrace();
 		}
 		TemplateGenerater itg = new TemplateGenerater(model, topic_uri);
 		itg.createTemplateFromContext("foaftemplate");
 	}
 }
