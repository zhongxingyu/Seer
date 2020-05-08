 package org.exoplatform.addons.populator.services;
 
 import org.apache.commons.lang.StringUtils;
 import org.exoplatform.social.core.identity.model.Identity;
 import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
 import org.exoplatform.social.core.manager.IdentityManager;
 import org.exoplatform.social.core.space.SpaceUtils;
 import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
 import org.exoplatform.social.core.space.model.Space;
 
 import javax.enterprise.context.RequestScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 import java.util.logging.Logger;
 
 @Named("spaceService")
 @RequestScoped
 public class SpaceService {
 
   org.exoplatform.social.core.space.spi.SpaceService spaceService_;
   IdentityManager identityManager_;
   Logger log = Logger.getLogger("SpaceService");
 
   @Inject
   public SpaceService(org.exoplatform.social.core.space.spi.SpaceService spaceService, IdentityManager identityManager)
   {
     spaceService_ = spaceService;
     identityManager_ = identityManager;
   }
 
   public void createSpaces()
   {
     log.info("1/5 : CREATE PUBLIC DISCUSSIONS SPACE");
     createSpace("Public Discussions", "public_discussions", "john");
     log.info("2/5 : CREATE BANK PROJECT SPACE");
     createSpace("Bank Project", "bank_project", "benjamin");
     log.info("3/5 : CREATE MARKETING ANALYTICS SPACE");
     createSpace("Marketing Analytics", "marketing_analytics", "benjamin");
     log.info("4/5 : CREATE HUMAN RESOURCES SPACE");
     createSpace("Human Resources", "human_resources", "benjamin");
     log.info("5/5 : CREATE HELP CENTER SPACE");
     createSpace("Help Center", "help_center", "john");
     log.info("CREATE SPACES DONE");
   }
 
   public void addSpacesAvatars()
   {
     log.info("1/5 : UPDATE PUBLIC DISCUSSIONS SPACE");
     createSpaceAvatar("Public Discussions", "john", "eXo-Space-Public-color.png");
     log.info("2/5 : UPDATE BANK PROJECT SPACE");
     createSpaceAvatar("Bank Project", "benjamin", "eXo-Space-Sales-color.png");
     log.info("3/5 : UPDATE MARKETING ANALYTICS SPACE");
     createSpaceAvatar("Marketing Analytics", "benjamin", "eXo-Space-Marketing-color.png");
     log.info("4/5 : UPDATE HUMAN RESOURCES SPACE");
     createSpaceAvatar("Human Resources", "benjamin", "eXo-Space-RH-color.png");
     log.info("5/5 : UPDATE HELP CENTER SPACE");
     createSpaceAvatar("Help Center", "john", "eXo-Space-Intranet-color.png");
     log.info("UPDATE SPACES DONE");
   }
 
   private void createSpaceAvatar(String name, String editor, String avatarFile)
   {
     Space space = spaceService_.getSpaceByDisplayName(name);
     if (space!=null)
     {
       try {
         space.setAvatarAttachment(Utils.getAvatarAttachment(avatarFile));
         spaceService_.updateSpace(space);
         space.setEditor(editor);
         spaceService_.updateSpaceAvatar(space);
       } catch (Exception e) {
         log.info(e.getMessage());
       }
     }
   }
 
   private void createSpace(String name, String prettyName, String creator)
   {
     Space target = spaceService_.getSpaceByDisplayName(name);
     if (target!=null)
     {
       spaceService_.deleteSpace(target);
     }
 
     Space space = new Space();
 //    space.setId(name);
     space.setDisplayName(name);
     space.setPrettyName(prettyName);
     space.setDescription(StringUtils.EMPTY);
     space.setGroupId("/spaces/" + space.getPrettyName());
     space.setRegistration(Space.OPEN);
    space.setVisibility(Space.PUBLIC);
     space.setPriority(Space.INTERMEDIATE_PRIORITY);
 
 
     Identity identity = identityManager_.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), true);
     if (identity != null) {
       space.setPrettyName(SpaceUtils.buildPrettyName(space));
     }
     space.setType(DefaultSpaceApplicationHandler.NAME);
 
 
     spaceService_.createSpace(space, creator);
     //SpaceUtils.endRequest();
 
   }
 
 }
