 /*
  * Created On: October 6, 2006, 1:31 PM
  */
 package com.thinkparity.ophelia.browser.application.browser.display.avatar.tab;
 
 import com.thinkparity.ophelia.browser.application.browser.component.PopupItemFactory;
 import com.thinkparity.ophelia.browser.application.browser.display.avatar.AvatarId;
 import com.thinkparity.ophelia.browser.application.browser.display.provider.ContentProvider;
 import com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar;
 import com.thinkparity.ophelia.browser.platform.util.State;
 
 /**
  * @author raymond@thinkparity.com
  * @version 1.1.2.1
  */
 public abstract class TabAvatar<T extends TabModel> extends Avatar {
     
     /** The avatar's accompanying <code>TabModel</code>.*/
     protected final T model;
 
     /** A menu item factory. */
     protected final PopupItemFactory menuItemFactory;
 
     /** The avatar's id. */
     private final AvatarId id;
 
     /** Creates new form TabAvatar */
     public TabAvatar(final AvatarId id, final T model) {
         // HACK This null check of an avatar id was done to allow the tab
         // plugin extension framework to create an avatar.  It needs to be
         // refactored in order to work "correctly".
         super(null == id ? "" : id.toString());
         this.id = id;
         this.menuItemFactory = PopupItemFactory.getInstance();
         this.model = model;
         installResizer();
     }
 
     /**
 	 * @see com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar#getId()
 	 */
 	@Override
 	public final AvatarId getId() {
 		return id;
 	}
 
     /**
 	 * @see com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar#getState()
 	 */
 	@Override
 	public final State getState() {
 		return null;
 	}
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar#reload()
      */
     @Override
     public void reload() {
         model.reload();
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar#setContentProvider(com.thinkparity.ophelia.browser.application.browser.display.provider.ContentProvider)
      */
     @Override
     public final void setContentProvider(final ContentProvider contentProvider) {
         model.setContentProvider(contentProvider);
     }
 
 	/**
      * @see com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar#setInput(java.lang.Object)
      */
     @Override
     public final void setInput(final Object input) {
         model.setInput(input);
         super.setInput(input);
     }
 
 	/**
 	 * @see com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar#setState(com.thinkparity.ophelia.browser.platform.util.State)
 	 */
 	@Override
 	public final void setState(final State state) {}

    /**
     * @see com.thinkparity.ophelia.browser.platform.application.display.avatar.Avatar#isAvatarBackgroundImage()
     */
    @Override
    public Boolean isAvatarBackgroundImage() {
        // Default avatar background image is not required for tab avatars.
        return Boolean.FALSE;
    }
 }
