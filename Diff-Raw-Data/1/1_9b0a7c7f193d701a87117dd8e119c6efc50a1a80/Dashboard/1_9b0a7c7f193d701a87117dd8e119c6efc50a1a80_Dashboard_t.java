 package controllers;
 
 import com.avaje.ebean.Ebean;
 import com.timgroup.jgravatar.Gravatar;
 import com.timgroup.jgravatar.GravatarDefaultImage;
 import com.timgroup.jgravatar.GravatarRating;
 import forms.ExternalImageForm;
 import forms.LendForm;
 import forms.UnLendForm;
 import forms.dvd.DvdSearchFrom;
 import forms.dvd.objects.InfoDvd;
 import forms.dvd.objects.PrevNextCopies;
 import helpers.*;
 import models.CopyReservation;
 import models.Dvd;
 import models.User;
 import models.ViewedCopy;
 import net.coobird.thumbnailator.Thumbnails;
 import objects.shoppingcart.CacheShoppingCart;
 import org.apache.commons.lang.StringUtils;
 import play.Logger;
 import play.data.Form;
 import play.mvc.Controller;
 import play.mvc.Result;
 import play.mvc.Results;
 import play.mvc.Security;
 import plugins.jsAnnotations.JSRoute;
 import plugins.s3.S3Plugin;
 import views.html.dashboard.*;
 
 import javax.imageio.ImageIO;
 import java.awt.image.BufferedImage;
 import java.io.*;
 import java.net.URL;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 @Security.Authenticated(Secured.class)
 public class Dashboard extends Controller {
 
   /**
    * Display the dvd and its informations in a popup
    *
    * @param dvdId
    * @return
    */
   @JSRoute
   public static Result displayDvd(final Long dvdId) {
     return getInfoDvd(dvdId, true);
   }
 
 
   /**
    * Display the dvd and its informations on a pag
    *
    * @param dvdId
    * @return
    */
   @JSRoute
   public static Result displayCopyOnPage(final Long dvdId) {
     return getInfoDvd(dvdId, false);
   }
 
   /**
    * Gets the {@link InfoDvd} for the given id
    *
    * @param copyId
    * @return
    */
   private static Status getInfoDvd(final Long copyId, final boolean popup) {
     final Dvd copy = Dvd.find.byId(copyId);
 
     if (copy == null) {
       if (Logger.isErrorEnabled() == true) {
         Logger.error("Could not find copy with id: " + copyId);
       }
       return Results.badRequest("Copy with the given id was not found");
     }
 
 
     final InfoDvd infoDvd = new InfoDvd(copy);
 
     final DvdSearchFrom currentSearchForm = DvdSearchFrom.getCurrentSearchForm();
     final PrevNextCopies nextAndPrev = Dvd.getNextAndPrev(copy, currentSearchForm);
 
     final CacheShoppingCart shoppingCartFromCache = ShoppingCartController.getShoppingCartFromCache();
     final Set<Long> bookmarkedCopyIds = BookmarksController.getBookmarkedCopyIds();
     final List<ViewedCopy> copyViewed = ViewedCopy.getCopyViewed(copy);
 
 
     if (popup == true) {
       return Results.ok(displaydvdPopup.render(infoDvd, Secured.getUsername()));
     } else {
       return Results.ok(displaydvd.render(infoDvd, Secured.getUsername(), nextAndPrev,shoppingCartFromCache,bookmarkedCopyIds,copyViewed));
     }
   }
 
   /**
    * Displays the dialog content for lending a dvd to a another {@link User}
    *
    * @return
    */
   @JSRoute
   public static Result lendDialogContent(final Long dvdId) {
     // check if the user may see the dvd
     final String userName = Secured.getUsername();
     final Dvd dvdForUser = Dvd.getDvdForUser(dvdId, userName);
     if (dvdForUser == null) {
       return Results.forbidden();
     }
 
     final List<Dvd> dvdForUserInSameHull = Dvd.getDvdUnBorrowedSameHull(dvdForUser);
     final Map<String, String> reservationsForCopy = CopyReservation.getReservationsForCopy(dvdId);
 
     final Form<LendForm> form = Form.form(LendForm.class);
     return Results.ok(lendform.render(form, dvdForUser, dvdForUserInSameHull, reservationsForCopy, User.getOtherUserNames()));
   }
 
   /**
    * Displays the dialog content for unlending a dvd
    *
    * @param dvdId
    * @return
    */
   @JSRoute
   public static Result unLendDialogContent(final Long dvdId) {
     // check if the user may see the dvd
     final String userName = Secured.getUsername();
     final Dvd dvdForUser = Dvd.getDvdForUser(dvdId, userName, true);
     if (dvdForUser == null) {
       return Results.forbidden();
     }
 
     if (dvdForUser.borrowDate == null) {
       final String message = "The Dvd: " + dvdForUser + " is not borrowed to anybody !";
       Logger.error(message);
       return Results.internalServerError(message);
     }
 
     final List<Dvd> dvdBorrowedSameHull = Dvd.getDvdBorrowedSameHull(dvdForUser);
 
     return Results.ok(unlendform.render(Form.form(UnLendForm.class), dvdForUser, dvdBorrowedSameHull));
 
   }
 
   /**
    * This actually lends a dvd to a user this is called via ajax
    *
    * @param dvdId
    * @return
    */
   @JSRoute
   public static Result lendDvd(final Long dvdId) {
 
     final Form<LendForm> form = Form.form(LendForm.class).bindFromRequest();
 
     // check if the form is okay
     final LendForm lendForm = form.get();
     String userName = StringUtils.trimToNull(lendForm.userName);
     final String freeName = StringUtils.trimToNull(lendForm.freeName);
     final String reservation = StringUtils.trimToNull(lendForm.reservation);
 
     if (StringUtils.isEmpty(userName) == true && StringUtils.isEmpty(freeName) == true && StringUtils.isEmpty(reservation) == true) {
       Logger.error("Could not lend dvd because no user, reservation or freename is given ");
 
       return Results.internalServerError();
     }
 
     if (StringUtils.isEmpty(reservation) == false && StringUtils.isNumeric(reservation) == true) {
       final String reservationBorrowerName = CopyReservation.getReservationBorrowerName(Long.valueOf(reservation));
       if (StringUtils.isEmpty(reservationBorrowerName) == false) {
         userName = reservationBorrowerName;
       }
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
   @JSRoute
   public static Result unlendDvd(final Long dvdId) {
 
     final Form<UnLendForm> form = Form.form(UnLendForm.class).bindFromRequest();
 
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
   @JSRoute
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
   @JSRoute
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
    * @param copyId
    * @param imgType
    * @param imgSize
    * @return
    */
   @JSRoute
   public static Result streamImage(final Long copyId, final String imgType, final String imgSize) {
     final String url = ImageHelper.getImageFile(copyId, EImageType.valueOf(imgType), EImageSize.valueOf(imgSize));
     if (StringUtils.isEmpty(url) == true) {
       return Results.notFound();
     }
 
     if (S3Plugin.pluginEnabled() == false) {
 
       final File file = new File(url);
 
 
       final String etag = ETagHelper.getEtag(file);
       final String nonMatch = request().getHeader(IF_NONE_MATCH);
       if (etag.equals(nonMatch) == true) {
         return status(304);
       }
 
       response().setHeader(ETAG, etag);
       response().setContentType("image/png");
       response().setHeader("Content-Length", String.valueOf(file.length()));
       return Results.ok(file);
 
 
     } else {
       return redirect(url);
     }
   }
 
   /**
    * This opens an external image and resizes it when needed
    *
    * @return
    */
   public static Result streamExternalImage() {
     final Form<ExternalImageForm> form = Form.form(ExternalImageForm.class).bindFromRequest();
 
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
       Logger.error("Failure while creating external image:", e);
       return Results.badRequest("Failure");
     }
 
   }
 
   /**
    * Gets the gravatar for the user
    *
    * @return
    */
   public static Result gravatar(final Integer size, final String userName) {
 
     final String ownerName = (userName == null) ? Secured.getUsername() : userName;
     final User userByName = User.getUserByName(ownerName);
 
     final String gravatarEmail = (userByName == null) ? "" : userByName.email;
 
     final String etag = ETagHelper.getEtag(ECacheObjectName.GRAVATAR_IMAGES + gravatarEmail + size);
     final String nonMatch = request().getHeader(IF_NONE_MATCH);
     if (etag != null && etag.equals(nonMatch) == true) {
       return status(304);
     }
 
     byte[] gravatarBytes = CacheHelper.getObject(ECacheObjectName.GRAVATAR_IMAGES, gravatarEmail + size);
     if (gravatarBytes == null) {
       final Gravatar gravatar = new Gravatar();
       gravatar.setSize(size);
       gravatar.setRating(GravatarRating.GENERAL_AUDIENCES);
       gravatar.setDefaultImage(GravatarDefaultImage.GRAVATAR_ICON);
       gravatarBytes = gravatar.download(gravatarEmail);
       CacheHelper.setObject(ECacheObjectName.GRAVATAR_IMAGES, gravatarEmail + size, gravatarBytes);
       ETagHelper.removeEtag(ECacheObjectName.GRAVATAR_IMAGES + gravatarEmail + size);
       ETagHelper.createEtag(ECacheObjectName.GRAVATAR_IMAGES + gravatarEmail + size, gravatarBytes);
     }
 
     response().setHeader(ETAG, ETagHelper.getEtag(ECacheObjectName.GRAVATAR_IMAGES + gravatarEmail + size));
     Controller.response().setContentType("image/png");
     return Results.ok(gravatarBytes);
 
   }
 
 
 }
