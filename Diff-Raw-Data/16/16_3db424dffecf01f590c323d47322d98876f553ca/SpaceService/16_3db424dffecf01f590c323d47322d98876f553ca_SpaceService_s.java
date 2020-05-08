 package org.exoplatform.addons.populator.services;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.poi.util.IdentifierManager;
 import org.exoplatform.social.core.identity.model.Identity;
 import org.exoplatform.social.core.identity.provider.SpaceIdentityProvider;
 import org.exoplatform.social.core.manager.IdentityManager;
 import org.exoplatform.social.core.space.SpaceException;
 import org.exoplatform.social.core.space.SpaceUtils;
 import org.exoplatform.social.core.space.impl.DefaultSpaceApplicationHandler;
 import org.exoplatform.social.core.space.model.Space;
 
 import javax.enterprise.context.ApplicationScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 import java.util.logging.Logger;
 
 @Named("userService")
 @ApplicationScoped
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
     createSpace("Public Discussions", "public_discussions", "john", "eXo-Space-Public-color.png");
 //    createSpace("Sales Team", "benjamin", "eXo-Space-Sales-color.png");
 //    createSpace("Marketing Analytics", "benjamin", "eXo-Space-Marketing-color.png");
 //    createSpace("Human Resources", "benjamin", "eXo-Space-RH-color.png");
 //    createSpace("Help Center", "john", "eXo-Space-Intranet-color.png");
   }
 
   private void createSpace(String name, String prettyName, String creator, String avatarFile)
   {
     Space space = new Space();
     space.setDisplayName(name);
     space.setPrettyName(prettyName);
     space.setDescription(StringUtils.EMPTY);
 //    try {
 //      space.setAvatarAttachment(UserService.getAvatarAttachment(avatarFile));
 //    } catch (Exception e) {
 //      log.info(e.getMessage());
 //    }
     space.setGroupId("/spaces/" + space.getPrettyName());
     space.setRegistration(Space.OPEN);
    space.setType(DefaultSpaceApplicationHandler.NAME);
     space.setVisibility(Space.PUBLIC);
     space.setPriority(Space.INTERMEDIATE_PRIORITY);
 
    Space target = spaceService_.getSpaceByDisplayName(name);
    if (target!=null)
    {
      spaceService_.deleteSpace(target);
    }
 
     Identity identity = identityManager_.getOrCreateIdentity(SpaceIdentityProvider.NAME, space.getPrettyName(), true);
     if (identity != null) {
       space.setPrettyName(SpaceUtils.buildPrettyName(space));
     }
 
 
     spaceService_.createSpace(space, creator);
     SpaceUtils.endRequest();
 
   }
 
 }
