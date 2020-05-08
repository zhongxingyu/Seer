 package com.bday.service;
 
 import java.lang.reflect.Type;
 import java.util.List;
 
 import javax.servlet.http.HttpSession;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import com.bday.model.UserModel;
 import com.bday.utils.Constants;
 import com.bday.view.InviteView;
 import com.bday.view.NewFamilyView;
 import com.google.gson.Gson;
 import com.google.gson.JsonArray;
 import com.google.gson.JsonObject;
 import com.google.gson.reflect.TypeToken;
 
 @Controller
 @RequestMapping("/invite")
 public class InviteService 
 {
 	@RequestMapping(method = RequestMethod.POST)
 	public String invite(@RequestBody String request, HttpSession session, Model model)
 	{
 		JsonObject jRequest = Constants.parse(request);
 		JsonArray jEmails = jRequest.get(Constants.ID_EMAILS).getAsJsonArray();
 		Type listOfEmails = new TypeToken<List<String>>(){}.getType();
 		Gson gson = new Gson();
 		List<String> emails = gson.fromJson(jEmails, listOfEmails);
 		UserModel user = (UserModel) session.getAttribute(Constants.USER);
 		
 		InviteView.invite(user, emails, model);
 		return "JSONView";
 	}
 }
