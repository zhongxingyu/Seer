 package com.tekton.comicrate.home.admin;
 
 import java.io.BufferedInputStream;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 //import java.io.FileOutputStream;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Enumeration;
 import java.util.Locale;
 import java.sql.ResultSetMetaData;
 
 //import java.nio.channels.FileChannel;
 import java.nio.file.*;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 //import org.springframework.web.bind.ServletRequestUtils;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 //import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.ui.Model;
 import org.springframework.util.FileCopyUtils;
 
 import com.tekton.comicrate.SQLController;
 import com.tekton.comicrate.files.fileParse;
 import com.tekton.comicrate.forms.Comic;
 import com.tekton.comicrate.home.admin.HomeFiles;
 
 @Controller
 @SessionAttributes
 public class HomeFilesController extends SQLController {
 	
 	public String user;
 	
 	@RequestMapping(value="/home/files/", method=RequestMethod.GET)
 	public String init_parse(Model model, Locale locale) {
 		
 		//get the "home" directory for files
 		
 
 		//send that list to the model to show for clicking
 		
 		//add an "all" function
 		return "file_base";
 	}
 
 	@RequestMapping(value="/home/file/{id}", method=RequestMethod.GET)
 	public String init_parse(Model model, Locale locale, @PathVariable("id") String id) {
 		
 		//get the "home" directory for files
 		
 
 		//send that list to the model to show for clicking
 		
 		//add an "all" function
 		return "file_base";
 	}
 
 	
 	@RequestMapping(value="/home/process/", method=RequestMethod.POST)
 	public String parse_files(@ModelAttribute("HomeFiles") HomeFiles files, BindingResult result, Model model, Locale locale) {
 		
 		for(int i = 0; i < files.files.length; i++) {
 			@SuppressWarnings("unused")
 			fileParse file = new fileParse(files.files[i]);
 		}
 		
 		return "file_parsed_list";
 	}
 	
     /**
      * download
      */
 	@RequestMapping(value="/home/download/{id}", method=RequestMethod.GET)
     public ModelAndView download(HttpServletRequest request, HttpServletResponse response, @PathVariable("id") String id) throws Exception {
         
 		this.createConnection();
 
 		HomeFile h_file = new HomeFile(this.conn);
 		h_file.setId(id);
 		h_file.getFileFromDB();
 		
         //Files file = this.filesService.find(id);
 
         File file = new File(h_file.getPath());
         FileInputStream stream = new FileInputStream(file);
 		BufferedInputStream ibr = new BufferedInputStream(stream);
 		DataInputStream file_input_stream = new DataInputStream(ibr);
 		
 		byte[] f = new byte[(int) file.length()];
 		file_input_stream.readFully(f);
         
         response.setContentType(h_file.getType());
         response.setContentLength((int) file.length());
         
         String f_name = h_file.getComic() + " " + h_file.getNumber() + "." + h_file.getType();
         
         response.setHeader("Content-Disposition","attachment; filename=\"" + f_name +"\"");
 
         FileCopyUtils.copy(f, response.getOutputStream());
 
         this.closeConnection();
         
         return null;
     }	
 
 	/**
 	 * Eventually this will move to a JSON request most likely
 	 * 
 	 * TODO: add error handling
 	 * 
 	 * @param request Standard HTTP request
 	 * @param response Standard HTTP response
 	 * @param id The ID of the file
 	 * @return Nothing for now...
 	 * @throws Exception
 	 */
 	@RequestMapping(value="/home/transfer/{id}", method=RequestMethod.GET)
     public ModelAndView transfer(HttpServletRequest request, 
     		HttpServletResponse response, 
     		@PathVariable("id") String id) throws Exception {
         
 		this.createConnection();
 
 		HomeFile h_file = new HomeFile(this.conn);
 		h_file.setId(id);
 		h_file.getFileFromDB();
 
         File file = new File(h_file.getPath());
         String f_name = h_file.getComic() + " " + h_file.getNumber() + "." + h_file.getType();
         String base = "F:\\transfer\\"+f_name;
         File out_file = new File(base);
         
         Path in = file.toPath();
         Path out = out_file.toPath();
         
         Files.copy(in,out);
         
         //FileOutputStream dest = new FileOutputStream(out_file);
 
         this.closeConnection();
         
         return null;
     }
 	
 	@RequestMapping(value="/home/json/transfer/{id}", method=RequestMethod.GET, headers="Accept=application/xml, application/json")
     public String json_transfer(Model model, 
     		Locale locale, 
     		@PathVariable("id") String id) throws Exception {
         
 		this.createConnection();
 
 		HomeFile h_file = new HomeFile(this.conn);
 		h_file.setId(id);
 		h_file.getFileFromDB();
 
         File file = new File(h_file.getPath());
         String f_name = h_file.getComic() + " " + h_file.getNumber() + "." + h_file.getType();
         String base = "F:\\transfer\\"+f_name;
         File out_file = new File(base);
         
         this.closeConnection(); //shouldn't need the connection for anything anymore
 
         model.addAttribute("fileDestination", base);
         model.addAttribute("sourceDestination", f_name);
         
         if(out_file.exists()) {
         	model.addAttribute("success", "already exists");
         	return "json_transfer";
         } else {
 	        Path in = file.toPath();
 	        Path out = out_file.toPath();
 	        
 	        Files.copy(in,out);
 
 	        model.addAttribute("success", "true");
 	        
 	        return "json_transfer";
         }
     }
 	
 	@RequestMapping(value="/home/edit/{id}", method=RequestMethod.GET)
 	public String edit(Model model, 
 			Locale locale, 
 			@PathVariable("id") String id) {
 		this.createConnection();
 		HomeFile file = new HomeFile(this.conn);
 		
 		file.setId(id);
 		file.getFileFromDB();
 		
 		model.addAttribute("file", file);
 
 		if(file.parent_book_local != null) {
 			Comic comic = new Comic(this.conn);
 			comic.setId(file.parent_book_local);
 			comic.getComicFromDB();
 			model.addAttribute("comic", comic);
 		}
 		
 		this.closeConnection();
 		return "files_edit";
 	}
 	
 	/**
 	 * 
 	 * Get data from the DB based on the files at hand to attach a file to a comic; if done correctly this should allow for easier "clean up" of duplicates! 
 	 * 
 	 * @param model The model (M in MVC) which will have data added to it to show later
 	 * @param locale Internal information
 	 * @param id The ID of the file
 	 * @param c The possible parent book for the file
 	 * @return
 	 */
 	@RequestMapping(value="/home/edit/{id}/from/{c}", method=RequestMethod.GET)
 	public String edit_assign(Model model, 
 			Locale locale, 
 			@PathVariable("id") String id, 
 			@PathVariable("c") String c) {
 		this.createConnection();
 		HomeFile file = new HomeFile(this.conn);
 		
 		file.setId(id);
 		file.getFileFromDB();
 		
 		file.setParent_book_local(c);
 		
 		model.addAttribute("file", file);
 		model.addAttribute("x", c);
 		
 		this.closeConnection();
 		return "files_edit";
 	}
 
 	@RequestMapping(value="/home/h", method=RequestMethod.GET)
 	public String stuff(HttpServletRequest request) {
 		String headername = "";
 		for(Enumeration<?> e = request.getHeaderNames(); e.hasMoreElements();){
 			headername = (String)e.nextElement();
 			System.out.println("Header Name:: "+ headername +" -- "+request.getHeader(headername));
 		}
 		
 		return "home";
 	}
 	
 	@RequestMapping(value="/home/edit/{id}", method=RequestMethod.POST)
 	public String update_file(@ModelAttribute("HomeFile") HomeFile file,
 			BindingResult result,
 			Model model,
 			Locale locale,
 			@PathVariable("id") String id) {
 		this.createConnection();
 		
 		file.conn = this.conn;
 		file.setId(id);
 		//file.getFileFromDB();
 		file.updateInLocalDB();
 		
 		model.addAttribute("file", file);
 		
 		//show the book data
 		if(file.parent_book_local != null) {
 			Comic comic = new Comic(this.conn);
 			comic.setId(file.parent_book_local);
 			comic.getComicFromDB();
 			model.addAttribute("comic", comic);
 		}
 		
 		this.closeConnection();
 		
 		return "files_edit";
 	}
 	
 	
 	/**
 	 * To get all the confirmed files that haven't been read, this query works! Though sadly it's not all that useful in a multi-user environment...
 	 * 
 	 * @return
 	 */
 	@RequestMapping(value="/home/unread", method=RequestMethod.GET)
	public String all_unread() {
 		this.createConnection();
 		
 		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 		this.user = auth.getName(); //get logged in username; we'll be needing that to find the comic...
 		
 		String q = "select c.id, c.title, c.issue_number, cf.path, ur.overall, cf.id as file_id from comic as c "+
 				"left join comic_files as cf on c.id=cf.parent_book_local "+
 				"left join (select * from user_ratings where user = \""+this.user+"\") as ur on ur.comic = c.id "+//;//+
 				"having ur.overall IS NULL AND cf.path IS NOT NULL";
 		
 		try {
 			Statement  stmt = this.conn.createStatement();
 			ResultSet result = null;
 			
 			try {
 				System.out.println( "Query: " + q );
 				//stmt.executeUpdate( q );
 				result = stmt.executeQuery(q);
 			} catch(SQLException e) {
 				//basically "what went wrong this time?!"
 				System.out.println( "SQLException: " + e.getMessage() );
 				System.out.println( "SQLState:     " + e.getSQLState() );
 				System.out.println( "VendorError:  " + e.getErrorCode() );
 			}
 			
 
 			while( result.next() ) {
 				System.out.println(result.getString("title")+ " :: "+result.getString("file_id"));
 				
 				/* Debug Loop **/
 				ResultSetMetaData md = result.getMetaData ();
 				// Get the number of columns in the result set
 				int numCols = md.getColumnCount();
 				int i;
 				for (i=1; i<=numCols; i++) {
 					//if (i > 1) System.out.print(",");
 					//System.out.println(md.getColumnLabel(i));
 					//System.out.println(md.getColumnLabel(i)+" , "+result.getString(md.getColumnLabel(i)));
 				}
 				/* End Debug Loop **/
 				
 			}
 		} catch( Exception e ) {
 			System.out.println( "Something broke... sql_stuff in HomeFiles" );
 			System.out.println( "SQLException " + e.getMessage() );
 			e.printStackTrace();
 		}
 		
 		this.closeConnection();
 		return "unread";
 	}
 	
 	public String transfer_series_unread() {
 		
 		
 		
 		return "unread_series";
 	}
 	
 	/**
 	 * TODO: add in some things that make showing which files haven't been read; 
 	 * there's no end-all-be-all query that I can figure out given the way things 
 	 * are set up. However, the "overall" rating is captured for the user any time
 	 * the comic and/or file is accessed...so it would be trivial to add another 
 	 * javascript array to searches/series overviews
 	 * 
 	 * select c.id, c.title, c.issue_number, cf.path, ur.overall from comic as c 
 			left join comic_files as cf on c.id=cf.parent_book_local
 			left join (select * from user_ratings where user = "tekton") as ur on ur.comic = c.id 
 			where c.title = "Batman"
 			having ur.overall IS NULL;
 	 * 
 	 * That's the closest I've found for when it's based on a series...could remove the where to get all books...
 	 * 
 	 * ...would also need to make sure the book exists on the local drive:
 	 * 
 	 * select c.id, c.title, c.issue_number, cf.path, ur.overall from comic as c 
 			left join comic_files as cf on c.id=cf.parent_book_local
 			left join (select * from user_ratings where user = "tekton") as ur on ur.comic = c.id 
 			having ur.overall IS NULL AND cf.path IS NOT NULL;
 	 * 
 	 */
 	
 }
