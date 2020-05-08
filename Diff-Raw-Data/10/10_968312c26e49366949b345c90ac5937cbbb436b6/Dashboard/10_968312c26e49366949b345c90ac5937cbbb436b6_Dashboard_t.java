 package controllers;
 
 import helpers.EImageSize;
 import helpers.EImageType;
 import helpers.ImageHelper;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 
 import jgravatar.Gravatar;
 import jgravatar.GravatarDefaultImage;
 import jgravatar.GravatarRating;
 import models.Dvd;
 import models.User;
 import net.coobird.thumbnailator.Thumbnails;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.avaje.ebean.Ebean;
 
 import play.Logger;
 import play.data.Form;
 import play.mvc.Controller;
 import play.mvc.Result;
 import play.mvc.Results;
 import play.mvc.Security;
 import views.html.dashboard.displaydvd;
 import views.html.dashboard.lendform;
 import views.html.dashboard.unlendform;
 import views.html.dashboard.deletedvd;
 import forms.ExternalImageForm;
 import forms.InfoDvd;
 import forms.LendForm;
 import forms.UnLendForm;
 
 @Security.Authenticated(Secured.class)
 public class Dashboard extends Controller {
 
   /**
    * Display the dvd and its informations
    * 
    * @param dvdId
    * @return
    */
   public static Result displayDvd(final Long dvdId) {
     final Dvd dvd = Dvd.find.byId(dvdId);
 
     if (dvd == null) {
       return Results.badRequest("Dvd with the given Id was not found");
     }
 
     final InfoDvd infoDvd = new InfoDvd(dvd);
 
     return Results.ok(displaydvd.render(infoDvd, Controller.request().username()));
   }
 
   /**
    * Displays the dialog content for lending a dvd to a another {@link User}
    * 
    * @return
    */
   public static Result lendDialogContent(final Long dvdId) {
     // check if the user may see the dvd
     final String userName = Secured.getUsername();
     final Dvd dvdForUser = Dvd.getDvdForUser(dvdId, userName);
     if (dvdForUser == null) {
       return Results.forbidden();
     }
 
     final List<Dvd> dvdForUserInSameHull = Dvd.getDvdUnBorrowedSameHull(dvdForUser);
 
     final Form<LendForm> form = Controller.form(LendForm.class);
     return Results.ok(lendform.render(form, dvdForUser, dvdForUserInSameHull));
   }
 
   /**
    * Displays the dialog content for unlending a dvd
    * 
    * @param dvdId
    * @return
    */
   public static Result unLendDialogContent(final Long dvdId) {
     // check if the user may see the dvd
     final String userName = Secured.getUsername();
     final Dvd dvdForUser = Dvd.getDvdForUser(dvdId, userName);
     if (dvdForUser == null) {
       return Results.forbidden();
     }
 
     if (dvdForUser.borrowDate == null) {
       final String message = "The Dvd: " + dvdForUser + " is not borrowed to anybody !";
       Logger.error(message);
       return Results.internalServerError(message);
     }
 
     final List<Dvd> dvdBorrowedSameHull = Dvd.getDvdBorrowedSameHull(dvdForUser);
 
     return Results.ok(unlendform.render(Controller.form(UnLendForm.class), dvdForUser, dvdBorrowedSameHull));
 
   }
 
   /**
    * This actually lends a dvd to a user this is called via ajax
    * 
    * @param dvdId
    * @return
    */
   public static Result lendDvd(final Long dvdId) {
 
     final Form<LendForm> form = Controller.form(LendForm.class).bindFromRequest();
 
     // check if the form is okay
     final LendForm lendForm = form.get();
     final String userName = StringUtils.trimToNull(lendForm.userName);
     final String freeName = StringUtils.trimToNull(lendForm.freeName);
 
     if ((StringUtils.isEmpty(userName) == true && StringUtils.isEmpty(freeName) == true)) {
       Logger.error("Could not lend dvd because no user or freename is given: " + "username: " + userName + " freename: " + freeName);
 
       return Results.internalServerError();
     }
 
     final String ownerName = Controller.ctx().session().get(Secured.AUTH_SESSION);
     Dvd.lendDvdToUser(dvdId, ownerName, userName, freeName, lendForm.alsoOthersInHull);
 
     return Results.ok();
   }
 
   /**
    * This actually lends a dvd to a user this is called via ajax
    * 
    * @param dvdId
    * @return
    */
   public static Result unlendDvd(final Long dvdId) {
 
     final Form<UnLendForm> form = Controller.form(UnLendForm.class).bindFromRequest();
 
     // check if the form is okay
     final UnLendForm unlendForm = form.get();
 
     final String ownerName = Controller.ctx().session().get(Secured.AUTH_SESSION);
     Dvd.unlendDvdToUser(dvdId, ownerName, unlendForm.alsoOthersInHull);
 
     return Results.ok();
   }
 
   /**
    * This renders the content for the delete dvd content dialog
    * 
    * @param dvdId
    * @return
    */
   public static Result deleteDialogContent(final Long dvdId) {
 
     final String userName = Secured.getUsername();
     final Dvd dvdForUser = Dvd.getDvdForUser(dvdId, userName);
     if (dvdForUser == null) {
       return Results.forbidden();
     }
 
     return Results.ok(deletedvd.render(dvdForUser));
   }
 
   /**
    * Actually deletes the dvd
    * 
    * @param dvdId
    * @return
    */
   public static Result deleteDvd(final Long dvdId) {
     final String userName = Secured.getUsername();
     final Dvd dvdForUser = Dvd.getDvdForUser(dvdId, userName);
     if (dvdForUser == null) {
       return Results.forbidden();
     }
 
     Ebean.deleteManyToManyAssociations(dvdForUser, "attributes");
     dvdForUser.delete();
 
     return Results.ok();
   }
 
   /**
    * Streams an image which is bundled with the given {@link Dvd}
    * 
    * @param dvdId
    * @param imgType
    * @param imgSize
    * @return
    */
   public static Result streamImage(final Long dvdId, final String imgType, final String imgSize) {
     final File file = ImageHelper.getImageFile(dvdId, EImageType.valueOf(imgType), EImageSize.valueOf(imgSize));
     if (file != null) {
       Controller.response().setContentType("image/png");
       return Results.ok(file);
     }
     return Results.ok();
   }
 
   /**
    * This opens an external image and resizes it when needed
    * 
    * @return
    */
   public static Result streamExternalImage() {
     final Form<ExternalImageForm> form = Controller.form(ExternalImageForm.class).bindFromRequest();
 
     if (form.hasErrors()) {
       return Results.badRequest("Failure");
     }
 
     final EImageSize imageSize = EImageSize.valueOf(form.get().imgSize);
 
     try {
       final BufferedImage asBufferedImage = Thumbnails.of(new URL(form.get().url)).size(imageSize.getWidth(), imageSize.getHeight()).asBufferedImage();
 
       final ByteArrayOutputStream os = new ByteArrayOutputStream();
       ImageIO.write(asBufferedImage, "png", os);
       final InputStream is = new ByteArrayInputStream(os.toByteArray());
 
       Controller.response().setContentType("image/png");
       return Results.ok(is);
     } catch (final IOException e) {
       Logger.error("Failure whiler creating external image:", e);
       return Results.badRequest("Failure");
     }
 
   }
 
   /**
    * Gets the gravatar for the user
    * 
    * @return
    */
   public static Result gravatar(final Integer size, final String userName) {
 
     final String ownerName = (userName == null) ? Controller.ctx().session().get(Secured.AUTH_SESSION) : userName;
     final User userByName = User.getUserByName(ownerName);
 
    final String gravatarEmail = (userByName == null) ? "" : userByName.email;
 
     final Gravatar gravatar = new Gravatar();
     gravatar.setSize(size);
     gravatar.setRating(GravatarRating.GENERAL_AUDIENCES);
    // TODO: make this configiable
    gravatar.setDefaultImage(GravatarDefaultImage.GRAVATAR_ICON);
    final byte[] jpg = gravatar.download(gravatarEmail);
 
     Controller.response().setContentType("image/png");
     return Results.ok(jpg);
 
   }
 
   /**
    * Displays the user profile mask
    * 
    * @return
    */
   public static Result profile() {
     return Results.TODO;
   }
 
 }
