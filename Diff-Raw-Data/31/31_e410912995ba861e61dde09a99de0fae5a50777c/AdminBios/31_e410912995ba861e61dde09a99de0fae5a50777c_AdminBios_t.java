 package controllers;
 
 import models.Bio;
 import play.data.Form;
 import play.mvc.*;
 
 import views.html.index;
 import views.html.admin.*;
 
 public class AdminBios extends SamsaraController
 {
   public static Result bios()
   {
     if (!isSessionAdmin())
     {
       return badRequest();
     }
     return ok(bios.render());
   }
 
   public static String shortBlurb(models.Bio bio)
   {
    return shortString(bio.blurb);
   }
 
   public static class EditBio
   {
     public String name;
     public String blurb;
     public EditBio()
     { }
 
     public EditBio(Bio b)
     {
       name = b.name;
       blurb = b.blurb;
     }
 
     public String validate()
     {
       return null;
     }
   }
 
   public static Result bio(String nameUrlPart)
   {
     if (!isSessionAdmin())
     {
       return badRequest();
     }
     if (!Bio.exists(nameUrlPart))
     {
       return renderBioIdNotFound(nameUrlPart);
     }
 
     Bio u = Bio.get(nameUrlPart);
     EditBio user = new EditBio(u);
     return ok(bio_single.render(u, form(EditBio.class).fill(user)));
   }
 
   public static Result updateBio(String nameUrlPart)
   {
     if (!isSessionAdmin())
     {
       return badRequest();
     }
     if (!Bio.exists(nameUrlPart))
     {
       return renderBioIdNotFound(nameUrlPart);
     }
 
     Bio b = Bio.get(nameUrlPart);
     Form<EditBio> filledForm = form(EditBio.class).bindFromRequest();
     if (filledForm.hasErrors())
     {
       return badRequest(bio_single.render(b, filledForm));
     }
 
     EditBio editBio = filledForm.get();
     b.name = nullForEmptyString(editBio.name);
     b.blurb = nullForEmptyString(editBio.blurb);
     b.save();
 
     return redirect(routes.AdminBios.bios());
   }
 
   public static Result renderBioIdNotFound(String nameUrlPart)
   {
     if (!isSessionAdmin())
     {
       return badRequest();
     }
     //TODO: Make a nice 404 page.
     return notFound(index.render("Bio with ID '" + nameUrlPart + "' not found."));
   }
 }
