 package com.ngdb.web.pages.article.game;
 
 import com.ngdb.entities.ActionLogger;
 import com.ngdb.entities.ArticleFactory;
 import com.ngdb.entities.article.Game;
 import com.ngdb.entities.article.element.File;
 import com.ngdb.entities.article.element.Picture;
 import com.ngdb.entities.reference.*;
 import com.ngdb.entities.user.User;
 import com.ngdb.web.model.BoxList;
 import com.ngdb.web.model.OriginList;
 import com.ngdb.web.model.PlatformList;
 import com.ngdb.web.model.PublisherList;
 import com.ngdb.web.services.infrastructure.CurrentUser;
 import com.ngdb.web.services.infrastructure.FileService;
 import com.ngdb.web.services.infrastructure.PictureService;
 import org.apache.shiro.authz.annotation.RequiresAuthentication;
 import org.apache.tapestry5.EventConstants;
 import org.apache.tapestry5.SelectModel;
 import org.apache.tapestry5.annotations.*;
 import org.apache.tapestry5.beaneditor.Validate;
 import org.apache.tapestry5.corelib.components.Zone;
 import org.apache.tapestry5.hibernate.annotations.CommitAfter;
 import org.apache.tapestry5.ioc.annotations.Inject;
 import org.apache.tapestry5.services.ajax.AjaxResponseRenderer;
 import org.apache.tapestry5.upload.services.UploadedFile;
 import org.got5.tapestry5.jquery.JQueryEventConstants;
 import org.hibernate.Session;
 import org.joda.time.DateTime;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 @RequiresAuthentication
 public class GameUpdate {
 
     @Property
     @Persist("entity")
     private Game game;
 
     @Property
     private UploadedFile mainPicture;
 
     @Inject
     private ReferenceService referenceService;
 
     @Inject
     private PictureService pictureService;
 
     @Inject
     private ArticleFactory articleFactory;
 
     @Property
     private String details;
 
     @Property
     private String ngh;
 
     @Property
     private String ean;
 
     @Property
     private String imdbId;
 
     @Property
     private String reference;
 
     @Property
     @Validate("required")
     private Origin origin;
 
     @Property
     @Validate("required")
     private Date releaseDate;
 
     @Property
     @Validate("required")
     private Publisher publisher;
 
     @Property
     @Validate("required")
     private Platform platform;
 
     @Property
     @Validate("required")
     private Long megaCount;
 
     @Property
     @Validate("required")
     private Box box;
 
     @Property
     @Validate("required,maxLength=255")
     private String title;
 
     @InjectPage
     private GameView gameView;
 
     @Inject
     private Session session;
 
     @Inject
     private CurrentUser currentUser;
 
     @Persist
     @Property
     private List<UploadedFile> pictures;
 
     @Persist
     @Property
     private Set<Picture> storedPictures;
 
     @Property
     private Picture picture;
 
     @Inject
     private ActionLogger actionLogger;
 
     @Inject
     private AjaxResponseRenderer ajaxResponseRenderer;
 
     @Inject
     private FileService fileService;
 
     public void onActivate(Game game) {
         this.game = game;
         if (pictures == null) {
             pictures = new ArrayList<UploadedFile>();
         }
     }
 
     Game onPassivate() {
         return game;
     }
 
     @SetupRender
     public void setup() {
         if (game == null) {
             this.publisher = null;
             this.platform = null;
             this.megaCount = null;
             this.box = null;
             this.details = null;
             this.origin = null;
             this.releaseDate = new DateTime().withYear(1990).toDate();
             this.details = null;
             this.title = null;
             this.ngh = null;
             this.ean = null;
             this.imdbId = null;
             this.reference = null;
         } else {
             this.publisher = game.getPublisher();
             this.platform = game.getPlatform();
             this.megaCount = game.getMegaCount();
             this.box = game.getBox();
             this.details = game.getDetails();
             this.origin = game.getOrigin();
             this.releaseDate = game.getReleaseDate();
             this.details = game.getDetails();
             this.title = game.getTitle();
             this.ngh = game.getNgh();
             this.ean = game.getUpc();
             this.imdbId = game.getImdbId();
             this.reference = game.getReference();
             this.storedPictures = game.getPictures().all();
         }
     }
 
     @OnEvent(component = "uploadImage", value = JQueryEventConstants.AJAX_UPLOAD)
     void onImageUpload(UploadedFile uploadedFile) {
         this.pictures.add(uploadedFile);
     }
 
     @CommitAfter
     @DiscardAfter
     public Object onSuccess() {
         Game game = new Game();
         if (isEditMode()) {
             game = this.game;
             game.updateModificationDate();
         }
         game.setDetails(details);
         game.setOrigin(origin);
         game.setReleaseDate(releaseDate);
         game.setTitle(title);
         game.setPublisher(publisher);
         game.setPlatform(platform);
         game.setMegaCount(megaCount);
         game.setBox(box);
         game.setNgh(ngh);
         game.setUpc(ean);
         game.setImdbId(imdbId);
         game.setReference(reference);
         game = (Game) session.merge(game);
         if (this.mainPicture != null) {
             Picture picture = pictureService.store(mainPicture, game);
             game.addPicture(picture);
             if (isEditMode()) {
                 session.merge(picture);
             }
         }
         if (pictures != null) {
             for (UploadedFile uploadedPicture : pictures) {
                 Picture picture = pictureService.store(uploadedPicture, game);
                 game.addPicture(picture);
                 if (isEditMode()) {
                     session.merge(picture);
                 }
             }
         }
         gameView.setGame(game);
         User user = currentUser.getUser();
         actionLogger.addEditAction(user, game);
         return gameView;
     }
 
     @CommitAfter
     Object onActionFromDeletePicture(Picture picture) {
         game.removePicture(picture);
         pictureService.delete(picture);
         this.storedPictures = game.getPictures().all();
         return this;
     }
 
     public String getSmallPictureUrl() {
         return picture.getUrl("small");
     }
 
     public boolean isEditMode() {
         return this.game != null;
     }
 
     public SelectModel getPlatforms() {
         return new PlatformList(referenceService.getPlatforms());
     }
 
     public SelectModel getPublishers() {
         return new PublisherList(referenceService.getPublishers());
     }
 
     public SelectModel getBoxes() {
         return new BoxList(referenceService.getBoxes());
     }
 
     public SelectModel getOrigins() {
         return new OriginList(referenceService.getOrigins());
     }
 
 }
