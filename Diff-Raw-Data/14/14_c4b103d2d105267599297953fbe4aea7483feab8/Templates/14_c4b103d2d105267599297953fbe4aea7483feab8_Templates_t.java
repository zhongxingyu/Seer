 package controllers;
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import models.DataTag;
 import models.TagTyp;
 import models.Template;
 import play.mvc.Controller;
 
 import static org.junit.Assert.*;
 
 import java.io.*;
 import java.util.*;
 import java.util.zip.*;
 
 import play.test.*;
 import org.junit.*;
 
 import models.*;
 import Support.*;
 
 public class Templates extends Controller {
 
 	// get all tags out of file and save them into a list
 	public static ArrayList<DataTag> searchDataTags(String file_content) {
 		ArrayList<DataTag> data_tags = new ArrayList<DataTag>();
 
 		// init tags and offsets
 		String MINDSHARE_BEGIN_TAG = "<mindshare:";
 		String MINDSHARE_END_TAG = "</mindshare:";
 		String MINDSHARE_CLOSE_TAG = ">";
 		Integer offset_start_begin_tag = 0;
 		Integer offset_start_end_tag = 0;
 		Integer offset_close_begin_tag = 0;
 		Integer offset_close_end_tag = 0;
 
 		offset_start_begin_tag = file_content.indexOf(MINDSHARE_BEGIN_TAG,
 				offset_start_end_tag);
 		offset_start_end_tag = file_content.indexOf(MINDSHARE_CLOSE_TAG,
 				offset_start_begin_tag);
 		offset_close_begin_tag = file_content.indexOf(MINDSHARE_END_TAG,
 				offset_close_end_tag);
 		offset_close_end_tag = file_content.indexOf(MINDSHARE_CLOSE_TAG,
 				offset_close_begin_tag);
 
 		// run through the whole string and search for tags
 		while (offset_start_begin_tag != -1
 				&& (offset_close_begin_tag > offset_start_end_tag)) {
 			String start_tag_type = file_content.substring(
 					offset_start_begin_tag + MINDSHARE_BEGIN_TAG.length(),
 					offset_start_end_tag);
 			while (start_tag_type.length() <= 0) {
 
 				offset_start_begin_tag = file_content.indexOf(
 						MINDSHARE_BEGIN_TAG, offset_start_end_tag);
 				offset_start_end_tag = file_content.indexOf(
 						MINDSHARE_CLOSE_TAG, offset_start_begin_tag);
 				start_tag_type = file_content.substring(offset_start_begin_tag
 						+ MINDSHARE_BEGIN_TAG.length(), offset_start_end_tag);
 			}
 
 			String end_tag_type = file_content.substring(offset_close_begin_tag
 					+ MINDSHARE_END_TAG.length(), offset_close_end_tag);
 			String description = file_content.substring(
 					offset_start_end_tag + 1, offset_close_begin_tag);
 
 
 			// create tags and save them into an array list
 			if ((start_tag_type.equals(end_tag_type))) {
 
 				if (start_tag_type.equals("content")) {
 					DataTag actual_tag = new DataTag(TagTyp.CONTENT,
 							description);
 
 					if (!checkDuplicateDataTags(actual_tag, data_tags)) {
 						data_tags.add(actual_tag);
 					}
 
 				} else if (start_tag_type.equals("date")) {
 					DataTag actual_tag = new DataTag(TagTyp.DATE, description);
 
 					if (!checkDuplicateDataTags(actual_tag, data_tags)) {
 						data_tags.add(actual_tag);
 					}
 				} else if (start_tag_type.equals("text_short")) {
 					DataTag actual_tag = new DataTag(TagTyp.TEXT_SHORT,
 							description);
 
 					if (!checkDuplicateDataTags(actual_tag, data_tags)) {
 						data_tags.add(actual_tag);
 					}
 
 				} else if (start_tag_type.equals("text_long")) {
 					DataTag actual_tag = new DataTag(TagTyp.TEXT_LONG,
 							description);
 
 					if (!checkDuplicateDataTags(actual_tag, data_tags)) {
 						data_tags.add(actual_tag);
 					}
 				}
 			}
 
 			// increment offset for tags
 			offset_start_begin_tag = file_content.indexOf(MINDSHARE_BEGIN_TAG,
 					offset_close_end_tag);
 			offset_start_end_tag = file_content.indexOf(MINDSHARE_CLOSE_TAG,
 					offset_start_begin_tag);
 			offset_close_begin_tag = file_content.indexOf(MINDSHARE_END_TAG,
 					offset_close_end_tag);
 			offset_close_end_tag = file_content.indexOf(MINDSHARE_CLOSE_TAG,
 					offset_close_begin_tag);
 		}
 
 		return data_tags;
 	}
 
 	// call html page for specific template
 	public static void show(Long id) {
 
 		Template selected_template = Template.find("byId", id).first();
 		String file_content;
 		String name = selected_template.name;
 		try 
 		{
 			file_content = selected_template.getTemplate();
 			ArrayList<DataTag> data_tags = new ArrayList<DataTag>();
 			data_tags = searchDataTags(file_content);
 			render(id, name, data_tags);
 		} 
 		catch (IOException e) {
 
 			render(e.getMessage());
 		}
     }
	
 	// generates a user uploaded file which has been filled
 	public static void generateFileUpload(String content_template, ArrayList<String> data_tags){
 		String file_content = content_template;
 
 		// search data tags in user template
 		ArrayList<DataTag> result_data_tags = new ArrayList<DataTag>();
 		result_data_tags = searchDataTags(file_content);
 		
 		// fill data tags object with content of input fields
 		for(int count = 0; count < data_tags.size(); count++)
 		{
 			result_data_tags.get(count).setContent(data_tags.get(count));
 		}
 		
 		// generate output file
 		DocumentGenerator doc_gen = new DocumentGenerator(file_content, result_data_tags);
 		String result = doc_gen.getResult();
 		
 		// send user a output file with .txt extension
 		InputStream file_stream = new ByteArrayInputStream(result.getBytes());
 		response.setContentTypeIfNotSet("text/plain; charset=utf-8");
 		renderBinary(file_stream, "own_template.txt");
 	}
 	
	// Determines which button has been pressed and calls the right method
 	public static void submitDuplexer(Long id, ArrayList<String> data_tags, String submitType)
 	{
 		// generates a file
		if (submitType.equals("Download"))
 		{
 			generateFile(id, data_tags);
 		}
 		// exports all filled input fields
 		else if(submitType.equals("Export"))
 		{
 			exportInput(id, data_tags);
		}
 		
 	}
 	
 	// generates a .csv file with all input data from a given template
 	public static void exportInput(Long id, ArrayList<String> data_tags){
 		Template selected_template = Template.find("byId", id).first();
 		String file_content;
 		try 
 		{
 			file_content = selected_template.getTemplate();
 			ArrayList<DataTag> result_data_tags = new ArrayList<DataTag>();
 	        result_data_tags = searchDataTags(file_content);  
 	        String content = "";
 			
 	        // generate a comma seperated file with input field and content
 			for(int count = 0; count < data_tags.size(); count++)
 			{
 				content += result_data_tags.get(count).getDescription() + ";" +
 					data_tags.get(count) + "\n";
 			}
 			
 			// send user a output file with .csv extension
 		    InputStream file_stream = new ByteArrayInputStream(content.getBytes());
 			response.setContentTypeIfNotSet("text/plain; charset=utf-8");
 			renderBinary(file_stream, "UserInput.csv"); 			
 			
 		} 
 		catch (IOException e) 
 		{
 			render(e.getMessage());
 		}
 	}
 	
 	// generate a output file which represents a user filled template
 	public static void generateFile(Long id, ArrayList<String> data_tags){

 		Template selected_template = Template.find("byId", id).first();
 		String file_content;
 		try 
 		{
 			file_content = selected_template.getTemplate();
 			ArrayList<DataTag> result_data_tags = new ArrayList<DataTag>();
 			result_data_tags = searchDataTags(file_content);
 
 			for (int count = 0; count < data_tags.size(); count++) 
 			{
 				result_data_tags.get(count).setContent(data_tags.get(count));
 				// if there is no user input
 				if(data_tags.get(count).isEmpty())
 				{
 					result_data_tags.get(count).setContent("-");
 				}
 			}
 
 			DocumentGenerator doc_gen = new DocumentGenerator(file_content,
 					result_data_tags);
 			String result = doc_gen.getResult();
 
 			// check for multifile input
 			if (selected_template.isMultifile()) 
 			{
 				ArrayList<NamedString> named_strings = new ArrayList<NamedString>();
 				int index = 0;
 				
 				// split filestring into seperate files 
 				while (result.indexOf("?mindshare|fileend", index) > -1) 
 				{
 					int new_index = result
 							.indexOf("?mindshare|fileend*", index);
 
 					int name_index = result.indexOf("*", new_index) + 1;
 					int name_end = result.indexOf("*", name_index);
 
 					String name = result.substring(name_index, name_end);
 
 					named_strings.add(new NamedString(name, result.substring(
 							index, new_index)));
 
 					// To prevent endless loops
 					new_index++;
 					index = name_end + 2;
 				}
 
 				// generate zip-file out of all files
 				ZipFactory.Generate(named_strings, selected_template.filename,
 						"application/zip", false);
 			} 
 			else // just one file
 			{
 				InputStream file_stream = new ByteArrayInputStream(
 						result.getBytes());
 				response.setContentTypeIfNotSet("text/plain; charset=utf-8");
 				renderBinary(file_stream, selected_template.filename);
 			}
 		} 
 		catch (IOException e) 
 		{
 			render(e.getMessage());
 		}
 	}
 
 	// just download an empty template
 	public static void download(Long id) {
 		Template selected_template = Template.find("byId", id).first();
 		String file_content;
 
 		try 
 		{
 			file_content = selected_template.getTemplate();
 			InputStream file_stream = new ByteArrayInputStream(
 					file_content.getBytes());
 
 			response.setContentTypeIfNotSet("text/plain");
 			renderBinary(file_stream, selected_template.filename);
 		} 
 		catch (IOException e) 
 		{
 
 			render(e.getMessage());
 		}
 
 	}
 
 	// check if actual data tag already is in the data tags list
 	public static boolean checkDuplicateDataTags(DataTag actual_tag,
 			ArrayList<DataTag> data_tags) {
 		for (int i = 0; i < data_tags.size(); i++) 
 		{
 			if (actual_tag.getDescription().equals(
 					data_tags.get(i).getDescription())) 
 			{
 				return true;
 			}
 		}
 		return false;
 	}
 }
