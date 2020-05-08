 package controllers;
 
 import models.Category;
 import models.Product;
 import models.User;
 import org.apache.commons.mail.SimpleEmail;
 import play.data.binding.Binder;
 import play.db.Model;
 import play.exceptions.TemplateNotFoundException;
 import play.i18n.Messages;
 import play.jobs.Job;
 import play.libs.Mail;
 import play.mvc.*;
 
 import java.lang.reflect.Constructor;
 import java.util.List;
 
 @CRUD.For(Product.class)
 @With(Secure.class)
 public class Products extends CRUD {
     public static void blank() throws Exception {
         renderArgs.put("blank", true);
         ObjectType type = ObjectType.get(getControllerClass());
         notFoundIfNull(type);
         Constructor<?> constructor = type.entityClass.getDeclaredConstructor();
         constructor.setAccessible(true);
         Model object = (Model) constructor.newInstance();
         try {
             render(type, object);
         } catch (TemplateNotFoundException e) {
             render("CRUD/blank.html", type, object);
         }
     }
 
     // TODO: there shall be a way to pass id of new created Product to sendNotofications
     public static void create() throws Exception {
         ObjectType type = ObjectType.get(getControllerClass());
         notFoundIfNull(type);
         Constructor<?> constructor = type.entityClass.getDeclaredConstructor();
         constructor.setAccessible(true);
         Product object = (Product) constructor.newInstance();
         Binder.bind(object, "object", params.all());
         if (Security.isConnected()) {
             User user = User.find("byEmail", Security.connected()).first();
             object.setAuthor(user);
         }
         validation.valid(object);
         if (validation.hasErrors()) {
             renderArgs.put("error", Messages.get("crud.hasErrors"));
             try {
                 render(request.controller.replace(".", "/") + "/blank.html", type, object);
             } catch (TemplateNotFoundException e) {
                 render("CRUD/blank.html", type, object);
             }
         }
         object._save();
         flash.success(Messages.get("crud.created", type.modelName));
         sendNotifications(object._key().toString());
         if (params.get("_save") != null) {
             redirect(request.controller + ".list");
         }
         if (params.get("_saveAndAddAnother") != null) {
             redirect(request.controller + ".blank");
         }
         redirect(request.controller + ".show", object._key());
     }
 
     public static void save(String id) throws Exception {
         ObjectType type = ObjectType.get(getControllerClass());
         notFoundIfNull(type);
         Product object = (Product) type.findById(id);
         notFoundIfNull(object);
         User author = object.getAuthor();
        if (author == null)  {
            if (Security.isConnected()) {
                author = User.find("byEmail", Security.connected()).first();
            }
        }
        /*List<Upload> uploads = (List<Upload>) Http.Request.current().args.get("__UPLOADS");
        for (Upload upload : uploads) {
            if (upload.getFieldName().equals("object.photo") && upload.getSize() > 0) {
                System.out.println(upload.asBytes());
                System.out.println(upload.getFileName());
            }
        }*/
         Binder.bind(object, "object", params.all());
         object.setAuthor(author);
         validation.valid(object);
         if (validation.hasErrors()) {
             renderArgs.put("error", Messages.get("crud.hasErrors"));
             try {
                 render(request.controller.replace(".", "/") + "/show.html", type, object);
             } catch (TemplateNotFoundException e) {
                 render("CRUD/show.html", type, object);
             }
         }
         object._save();
         flash.success(Messages.get("crud.saved", type.modelName));
         if (params.get("_save") != null) {
             redirect(request.controller + ".list");
         }
         redirect(request.controller + ".show", object._key());
     }
 
     @After(only = {"save"})
     public static void sendNotifications(String id) throws Exception {
         Product product = Product.findById(Long.parseLong(id));
         notFoundIfNull(product);
         Category category = product.getCategory();
         if (category == null && product.getParent() != null)
             category = product.getParent().getCategory();
         if (category != null) {
             List<User> moderators = category.getModerators();
             if (moderators == null && category.getParent() != null)
                 moderators = category.getParent().getModerators();
             boolean isNewProduct = "create".equals(Http.Request.current.get().actionMethod);
             if (moderators != null && !moderators.isEmpty()) {
                 for (User user : moderators)
                     sendEmail(product, user, isNewProduct);
             }
 
         }
     }
 
     private static void sendEmail(final Product product, User user, boolean isNewProduct) throws Exception {
         final SimpleEmail email = new SimpleEmail();
         email.addTo(user.email);
         email.setFrom("noreply@likespot.ru", "Likespot");
         email.setSubject(isNewProduct ? "New product in a category you moderate has been created" :
                 "Product in a category you moderate has been updated");
         email.setCharset("UTF-8");
         new Job<Void>() {
             @Override
             public void doJob() throws Exception {
                 Mail.send(email.setMsg("Product " + product.getTitle() + "\n" + product.getDescription()));
             }
         }.now();
     }
 }
