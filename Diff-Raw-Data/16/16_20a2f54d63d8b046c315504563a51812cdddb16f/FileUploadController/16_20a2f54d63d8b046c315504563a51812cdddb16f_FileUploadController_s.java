 package unisalento.fcorvino.controllers;
 
 import java.io.IOException;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.context.request.WebRequest;
 import org.springframework.web.multipart.MultipartFile;
 import unisalento.fcorvino.beans.EntModel;
 import unisalento.fcorvino.beans.models.ModelTable;
 import unisalento.fcorvino.model.ModelsBuilder;
 
 @Controller
 @RequestMapping("/fileupload")
 public class FileUploadController {
 
 	@ModelAttribute
 	public void ajaxAttribute(WebRequest request, Model model) {
             
 	}
 
 	@RequestMapping(method=RequestMethod.GET)
 	public void fileUploadForm() {
 	}
 
 	@RequestMapping(method=RequestMethod.POST)
 	public String processUpload(
                 @RequestParam MultipartFile file, 
                 @RequestParam(value="name") String name,
                 @RequestParam(value="table") String tableId,
                 @RequestParam(value="source") String source,
                 Model model) throws IOException {
             String message = "uploaded successfully";
             ModelsBuilder builder = new ModelsBuilder();            
             try {
                 builder.loadModel(name);
                 builder.loadTable(source, tableId, file);
             } catch(Exception e){
                 message = e.getMessage();
             }
             model.addAttribute("message", 
                     "File '" + file.getOriginalFilename() + "' " + message);
             model.addAttribute("model", builder.getModel());
            return "edit";
 	}
         
        @RequestMapping("/formUpload")
         public String requestForm(
                 @RequestParam(value="name") String name,
                 @ModelAttribute("model") EntModel entModel,
                 Model model){
             for(ModelTable table : entModel.getTypeModel().getTables()){
                 if(table.getName().equals(name)){
                    model.addAttribute("table", model);                
                     return "formupload";                    
                 }
             }
            return "error";
         }
 	
 }
