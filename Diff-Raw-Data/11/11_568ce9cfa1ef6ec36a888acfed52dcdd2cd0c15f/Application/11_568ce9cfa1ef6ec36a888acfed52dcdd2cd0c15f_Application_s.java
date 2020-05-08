 /*
  *
  * Copyright (C) 2011 SW 11 Inc.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  *
  *
  *
  *
  */
 package controllers;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import play.*;
 import play.mvc.*;
 
 import java.util.*;
 
 import models.*;
 import models.generate.Document;
 
 import models.generate.DocumentGenerator;
 import play.Play;
 import play.data.validation.Required;
 import play.data.validation.Validation;
 import play.mvc.results.Redirect;
 import utils.io.FileStringReader;
 
 public class Application extends Controller
 {
 
     public static void index()
     {

         render();
     }
 
     @Before
     static void setConnectedUser()
     {
         if (Security.isConnected())
         {
 
             User user = User.find("email_", Security.connected()).first();
             if (user != null)
             {
 
                 renderArgs.put("user", user);
             }
         }
     }
 
     public static void upload(String name, String tags, String description, File template, Boolean isHidden)
     {
 
         String upload = request.params.get("upload");
         Boolean success = false;
 
         String user = Security.connected();
 
 
 
         String hidden = request.params.get("isHidden");
 
         if (hidden == null)
         {
             isHidden = false;
         }
         else
         {
             isHidden = true;
         }
 
 
         if (upload != null)
         {
 
             validation.clear();
             validation.required(name).message("Please insert a name.");
             validation.required(template).message("Please select a file.");
 
 
             if (template != null && !Validation.hasErrors())
             {
                 String error = Template.upload(name, description, template, user, isHidden);
                 if (error == null)
                 {
                     tagIt(name, tags);
                     success = true;
 
                 }
                 else
                 {
                     Errors.displayInlineError(1, "Template has to be a plain-text file (encoded in UTF-8).", "../Application/upload");
                 }
             }
         }
 
         render(name, description, template, success);
 
     }
 
     public static void tagIt(String template, String tags)
     {
         List<String> tagList = null;
         tagList = createTags(tags);
         Template temp = Template.find("name_", template).first();
         temp.tagItWith(tagList).save();
         showSingleTemplate(temp.id);
     }
 
     public static List<String> createTags(String fullString)
     {
         //fullString = fullString.replace("^\\s*", "");
         //fullString = fullString.replace("\\s*$", "");
         fullString = fullString.replaceAll("\\s+", " ");
         String[] alltags = fullString.split(",");
 
         List<String> tagList = new ArrayList<String>();
         List<String> realTags = new ArrayList<String>();
         tagList.addAll(Arrays.asList(alltags));
 
         for (int index = 0; index < tagList.size(); index++)
         {
             //tagList.set(index, tagList.get(index).replace("^\\s*", ""));
             //tagList.set(index, tagList.get(index).replace("\\s*$", ""));
             tagList.set(index, tagList.get(index).trim());
             if ((tagList.get(index).length() > 0) && (tagList.get(index).equals(" ")) == false)
             {
                 realTags.add(tagList.get(index));
             }
         }
         return realTags;
     }
 
     public static void showAllTemplates()
     {
 
         // Template Test
 
         /*Template.deleteAll();       
         Template template = new Template("example", "example.txt", "niemand2", new Date(123456l), "Example config", 15000);
         template.calculateForm();      
         
         template.save();*/
 
         try
         {
 
             List<Template> all_templates = Template.findAll();
 
             render(all_templates);
         }
         catch (Exception e)
         {
 
             System.out.println(e.getMessage());
             render("Application/upload.html");
         }
 
     }
 
     public static void showSingleTemplate(long id)
     {
 
         Template template = Template.findById(id);
         List<Tag> tags = template.sortTags(template);
         render(template, tags);
     }
 
 //    public static void simpleLink()
 //    {
 //        String applicationPath = Play.applicationPath.getAbsolutePath();
 //        File templateFile = new File(applicationPath + "/data/test/SimpleDocument.txt");
 //        Map<String, String> replaceMap = new HashMap<String, String>();
 //        replaceMap.put("%name%", "John");
 //
 //
 //        DocumentGenerator generator = new DocumentGenerator(templateFile, replaceMap);
 //        models.generate.Document document = generator.create();
 //        String path = "/public/tmp/" + document.getFile().getName();
 //        render(path);
 //    }
     public static void selectedTemplate(Long id)
     {
 
         Template loadedTemplate = Template.findById(id);
         render(loadedTemplate);
     }
 
     public static void register()
     {
 
         render();
     }
 
     public static void insertionComplete() throws FileNotFoundException, IOException
     {
         //Template template = Template.findById(id);
 
         Map<String, String[]> map = request.params.all();
         Template template = Template.findById(Long.decode(map.get("ID")[0]));
 
         template.doMap(map);
 
         Iterator iterator = template.templates_.keySet().iterator();
 
         while (iterator.hasNext())
         {
             String key = (String) iterator.next();
 
         }
 
         DocumentGenerator generator = new DocumentGenerator(new File(Play.applicationPath.getAbsolutePath() + "/public/templates/" + template.filename_), template.getTemplates_());
         File document = generator.create();
 
         FileStringReader reader_temp = new FileStringReader(document);
 
 //      document.getFile().delete();
 
         String path1 = document.getAbsolutePath();
         String playPath = Play.applicationPath.getAbsolutePath();
 
 
 
         path1 = path1.replaceAll("\\\\", "/");
         playPath = playPath.replaceAll("\\\\", "/");
 
 
         //template.pathToFilledFile = document.getFile().getAbsolutePath().replaceAll(Play.applicationPath.getAbsolutePath(), "");
         template.pathToFilledFile = path1.replaceAll(playPath, "");
         //template.pathToFilledFile = "public/tmp/1305726987921/test123.txt";
 
 
 
         File file = new File(template.pathToFilledFile);
 
         String[] splitted = file.getName().split(".");
 
         for (int i = 0; i < splitted.length; i++)
         {
             System.out.println("X: " + splitted[i]);
         }
 
         if (splitted.length > 1 && splitted[splitted.length - 1] == "tex")
         {
             //System.out.println("TEX");
             //Helper.texToPdf(new File(Play.applicationPath.getAbsolutePath()+template.pathToFilledFile), new File(Play.applicationPath.getAbsolutePath()+template.pathToFilledFile));
         }
         else
         {
             FileStringReader reader = new FileStringReader(document);
 
 
             Helper.textToImage(reader.read(), new File(Play.applicationPath.getAbsolutePath() + template.pathToFilledFile));
 
         }
 
 
         render(template);
 
 
 
     }
 
     public static void generatePdf(String temp)
     {
         String path = Play.applicationPath.toString() + temp;
         path = path.replaceAll("\\\\", "/");
         File tex = new File(path);
         File dest = new File(tex.getParent());
         if (Helper.texToPdf(tex, dest))
         {
             String[] files = tex.getParent().split("tmp");
             System.out.println(files[1]);
             String name = tex.getName().substring(0, tex.getName().lastIndexOf("."));
 
             System.out.println("PDF successfully!");
             redirect("/public/tmp" + files[1] + "/" + name + ".pdf");
         }
         else
         {
             System.out.println("PDF createn failed!");
         }
     }
 
     public static void UserCP()
     {
         render("Application/usercp.html");
     }
 
     public static void editProfile()
     {
         User usercur = User.find("email_", Security.connected()).first();
         render("Application/editProfile.html", usercur);
     }
 
     public static void doEditProfile(String username, String password, String passwordagain,
             String firstname, String lastname, String question, String answer)
     {
 
         List<String> Errors = new ArrayList();
         boolean errorStatus = false;
         User user = User.find("email_", username).first();
         String email = user.email_;
 
 
 
         if ((firstname.length() == 0) || (lastname.length() == 0))
         {
             Errors.add("Please type your name");
             errorStatus = true;
         }
 
         if (password.length() < 6 && password.length() > 0)
         {
             Errors.add("Password too short. At least 6 characters");
             errorStatus = true;
 
         }
         else if (password.equals(passwordagain) == false)
         {
             Errors.add("Passwords don't match");
             errorStatus = true;
         }
 
 
         if (question.length() != 0 || answer.length() != 0)
         {
             if (answer.length() == 0 || question.length() == 0)
             {
                 Errors.add("Please type in your answer and question");
                 errorStatus = true;
             }
         }
 
 
         if (errorStatus == false)
         {
 
 
             user.edit(password, firstname, lastname, question, answer);
             user.save();
             Errors.add("User " + email + " successfull edited");
         }
         render(Errors, errorStatus);
     }
 
     public static void editTemplates(Boolean admin)
     {
 
 
         User user = User.find("email_", Security.connected()).first();
 
         List<Template> all_templates = Template.findAll();
 
         render(all_templates, user.email_, admin);
     }
 
     public static void editOtherProfile()
     {
         User user = User.find("email_", Security.connected()).first();
         if (user.admin_ == true)
         {
             List<Template> all_users = User.findAll();
 
             render(all_users);
         }
         else
         {
             Errors.displayInlineError(2, "You dont have Admin Rights", "../Application/index");
         }
     }
 
     public static void editOtherProfileNow(String username)
     {
 
         User user = User.find("email_", Security.connected()).first();
         if (user.admin_ == true)
         {
             User usercur = User.find("email_", username).first();
             render("Application/editProfile.html", usercur);
         }
         else
         {
             Errors.displayInlineError(2, "You dont have Admin Rights", "../Application/index");
         }
     }
 
     public static void deleteUser(String username)
     {
 
         User usercur = User.find("email_", Security.connected()).first();
         String mail = usercur.email_;
         User.delete("email_", username);
 
 
         if (mail.equals(username))
         {
             redirect("/Secure/logout");
         }
         else
         {
             render("Application/deleteUser.html", username);
 
         }
     }
 
     public static void AdminCP()
     {
         User user = User.find("email_", Security.connected()).first();
 
         if (user.admin_ == true)
         {
             render("Application/admincp.html");
         }
         else
         {
             Errors.displayInlineError(2, "You dont have Admin Rights", "../Application/index");
         }
     }
 
     public static void editAdmin(String username)
     {
 
         User usercur = User.find("email_", username).first();
 
         if (usercur.admin_ == true)
         {
             usercur.admin_ = false;
         }
         else
         {
             usercur.admin_ = true;
         }
 
         usercur.save();
 
 
         render("Application/editadmin.html");
     }
 
     public static void deleteTemplate(String tempname)
     {
 
         Template temp = Template.find("name_", tempname).first();
         temp.delete();
         render("Application/deleteTemplate.html", tempname);
     }
 }
