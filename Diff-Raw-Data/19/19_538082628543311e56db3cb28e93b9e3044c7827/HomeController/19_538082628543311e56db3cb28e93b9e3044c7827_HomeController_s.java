 package com.pit.giandym;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.io.IOUtils;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.multipart.MaxUploadSizeExceededException;
 import org.springframework.web.servlet.HandlerExceptionResolver;
 import org.springframework.web.servlet.ModelAndView;
 
 @Controller
 
 public class HomeController implements HandlerExceptionResolver{
 
 	@RequestMapping(value = "/script", method = RequestMethod.GET)
 	public String selectScript(HttpServletRequest request,HttpServletResponse reponse) {
 		//Initialisation du userFile en session
 		request.getSession().setAttribute("userFile", new UserFileModel());
 		//Initialisation de la liste de fichier en session (liste des fichiers generess)
 		request.getSession().setAttribute("listeFichier", null);
 
		//Recuperation de la date courante
		//Date date = new Date();
		//DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG, locale);
		//String formattedDate = dateFormat.format(date);
		//model.addAttribute("serverTime", formattedDate );

 		//récuperation de la liste des sites Natura2000
 		Map<Integer,String> liste = ScriptModel.getSite();
 		//recuperation de la liste de script
 		List<String> script= ScriptModel.getAllScript();
 
 		//On les ajoute au model pour pouvoir les afficher
 		request.setAttribute("listeSite",liste);
 		request.setAttribute("listeScript",script);
 		request.setAttribute("listeOptionMeth",ScriptModel.getMethode());
 		request.setAttribute("listeOptionType",ScriptModel.getType());
 
 		return "script/choixScript";
 	}
 
 
 	@RequestMapping(value = "/script/choixScript", method = RequestMethod.POST)
 	public String selectScriptSite(HttpServletRequest request,HttpServletResponse reponse){	
 		String script=null;
 		String site=null;
 		String type=null;
 		String methode=null;
 		if(request.getParameter("script")!=null){
 			script = request.getParameter("script");
 			System.out.println("Sript no : "+script);
 			if(script.equals("1")){
 				if(request.getParameter("optionMeth")!=null && request.getParameter("optionType")!=null){
 					methode=request.getParameter("optionMeth");
 					type=request.getParameter("optionType");			
 				}else{
 					System.out.println("L'attribut optionMeth ou optionType est à null");
 					return "script/erreurScript";
 				}
 			}
 		}else{
 			System.out.println("L'attribut script est à null");
 			return "script/erreurScript";
 		}

		if(request.getParameter("site")!=null){
			site = request.getParameter("site");
		}else{
 			System.out.println("L'attribut site est à null");
 			return "script/erreurScript";
 		}

 		//On cree un userFile en l'initialisant avec le numero de script et de site
 		request.getSession().setAttribute("userFile", new UserFileModel(script, site, methode, type));
 
 		//recuperation des entetes et ajout dans la vue
 		List<String> entete= ScriptModel.getEnteteByScript(Integer.parseInt(request.getParameter("script")));
 		request.setAttribute("listeEntete", entete);
 
 		return "script/envoieFichier";
 	}
 
 
 	@RequestMapping(value = "/script/envoieFichier", method = RequestMethod.POST)
 	public String envoieFichier(HttpServletRequest request, @ModelAttribute(value="FORM") UserFileModel script,BindingResult result){
 		if(!result.hasErrors()){
 			//on recupere l'userFile de la session
 			UserFileModel ufm = (UserFileModel)request.getSession().getAttribute("userFile");
 			//on lui donne le script recupere
 			ufm.setFile(script.getFile());
 			//ainsi que le nom
 			String name = script.getFile().getOriginalFilename();
 			ufm.setName(name.substring(0, name.lastIndexOf('.')));
 			//on indique si les fichiers doivent etre conserves
 			String conservation = request.getParameter("conservation");
 			System.out.println("conserver ?  "+conservation);
 			ufm.setConserver(conservation.equals("1")?true:false);
 			//on met le nom de sortie donné
 			String outputName = request.getParameter("outputName");
 			System.out.println("outputName : "+outputName);
 			ufm.setOutputName(outputName);
 			
 			File f = new File (ufm.getPath());
 			f.mkdir();
 			System.out.println("creation du dossier : "+ufm.getPath() + ufm.getUnique());
 			String filePath = ufm.getPath() +"/"+ ufm.getFile().getOriginalFilename();
 			System.out.println("chemin de fichier envoyé : "+filePath);
 
 			try {
 				System.out.print("Enregistrement du fichier ........");
 				FileOutputStream outputStream = new FileOutputStream(new File(filePath));
 				outputStream.write(ufm.getFile().getFileItem().get());
 				outputStream.close();
 				System.out.println("CHECK");
 
 				Map<String,ArrayList<String>> map = ufm.execute();
 				if(map==null){
 					System.out.println("script/erreurScript");
 					return "script/erreurScript";
 				}
 				request.getSession().setAttribute("listeFichier", map);
 
 			} catch (Exception e) {
 				System.out.println("Error while saving file");
 				e.printStackTrace();
 				return "script/erreurScript";
 			}
 
 			return "script/execution";
 		}else{
 			System.out.println("Erreur HomeControleur at envoieFichier");
 			return "script/erreurScript";
 		}
 	}
 
 
 	@RequestMapping(value = "/script/execution", method = RequestMethod.POST)
 	public String resultScript(HttpServletRequest request,HttpServletResponse reponse){
 		request.setAttribute("listeFichier", request.getSession().getAttribute("listeFichier"));
 
 		return null;
 	}
 
 	@RequestMapping(value = "/script/getFile", method = RequestMethod.GET)
 	public String getFile(HttpServletRequest request,HttpServletResponse reponse){
 		System.out.println(request.getParameter("id"));
 		@SuppressWarnings("unchecked")
 		ArrayList<String> path = ((Map<String,ArrayList<String>>)request.getSession().getAttribute("listeFichier")).get(request.getParameter("id"));
 		for(int i=0;i<path.size();i++){
 			System.out.println("test : "+path.get(i));
 		}
 		System.out.println("path : "+path.get(0));
 
 		String name = path.get(1).substring(0, path.get(1).lastIndexOf('.'));
 		System.out.println("name : "+name);
 
 		String extention = path.get(1).substring(path.get(1).lastIndexOf('.')+1,path.get(1).length()); 
 		System.out.println("Extention : "+extention);
 		// Voir la pour plus d extention
 		// http://www.coderanch.com/t/422345/Servlets/java/response-setContentType-working-case-Excel
 		if(extention.equals("csv")){
 			reponse.setContentType("APPLICATION/OCTET-STREAM");  
 			reponse.setHeader("Content-Disposition", "attachment; filename="+name+".csv"); 
 		}else if(extention.equals("pdf")){
 			reponse.setContentType("application/pdf");  
 			reponse.setHeader("Content-Disposition", "attachment; filename="+name+".pdf");  
 		}
 		try {
 			InputStream is = new FileInputStream(path.get(0));
 			IOUtils.copy(is, reponse.getOutputStream());
 			reponse.getOutputStream().flush();
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	
 	@Override
 	public ModelAndView resolveException(HttpServletRequest arg0,HttpServletResponse arg1, Object arg2, Exception exception) {
 		Map<Object, Object> model = new HashMap<Object, Object>();
 		if (exception instanceof MaxUploadSizeExceededException){
 			model.put("errors", "File size should be less then "+
 					((MaxUploadSizeExceededException)exception).getMaxUploadSize()+" byte.");
 		} else{
 			model.put("errors", "Unexpected error: " + exception.getMessage());
 		}
 		model.put("FORM", new UserFileModel());
 		return new ModelAndView("/script/erreurScript", ((Map) model));
 	}
 }
