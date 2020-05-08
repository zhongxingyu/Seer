 package de.ifcore.argue.controller;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import de.ifcore.argue.domain.entities.JsonString;
 import de.ifcore.argue.domain.form.JsonStringForm;
 import de.ifcore.argue.services.JsonStorageService;
 
 @Controller
 public class JsonStorageController
 {
 	@Autowired
 	private JsonStorageService storageService;
 
	@RequestMapping(value = "/storage/{base36Id}", method = RequestMethod.GET)
 	@ResponseBody
 	public JsonString get(@PathVariable String base36Id)
 	{
 		JsonString jsonString = storageService.get(base36Id);
 		return jsonString;
 	}
 
 	@RequestMapping(value = "/storage", method = RequestMethod.POST)
 	@ResponseBody
 	public String get(JsonStringForm jsonStringForm)
 	{
 		String base36Id = storageService.store(jsonStringForm.getJson());
 		return base36Id;
 	}
 }
