 /**
  * Created On: Apr 3, 2007 1:03:10 PM
  * $Id$
  */
 package com.thinkparity.ophelia.browser.platform.action.platform;
 
 import com.thinkparity.codebase.assertion.Assert;
 
 import com.thinkparity.codebase.model.util.http.Link;
 import com.thinkparity.codebase.model.util.http.LinkFactory;
 
 import com.thinkparity.ophelia.browser.BrowserException;
 import com.thinkparity.ophelia.browser.platform.Platform;
 import com.thinkparity.ophelia.browser.platform.action.AbstractAction;
 import com.thinkparity.ophelia.browser.platform.action.ActionId;
 import com.thinkparity.ophelia.browser.platform.action.Data;
 import com.thinkparity.ophelia.browser.util.jdic.DesktopUtil;
 
 import org.jdesktop.jdic.desktop.DesktopException;
 
 /**
  * @author rob_masako@shaw.ca
  * @version $Revision$
  */
 public class LearnMore extends AbstractAction {
 
     /** The relative link for "beta" learn more. */
     private static String BETA_LINK;
 
     /** The relative link for "privacy" learn more. */
     private static String PRIVACY_LINK;
 
     /** The relative link for "security" learn more. */
     private static String SECURITY_LINK;
 
     /** The relative link for "signup" learn more. */
     private static String SIGNUP_LINK;
 
     static {
         BETA_LINK = "contactUs";
        // TODO Fix the privacy link
        PRIVACY_LINK = "pricing";
         SECURITY_LINK = "solutions/index.php#security";
         SIGNUP_LINK = "pricing";
     }
 
     /** Create LearnMore. */
     public LearnMore(final Platform platform) {
         super(ActionId.PLATFORM_LEARN_MORE);
     }
 
     /**
      * @see com.thinkparity.ophelia.browser.platform.action.AbstractAction#invoke(com.thinkparity.ophelia.browser.platform.action.Data)
      */
     @Override
     public void invoke(final Data data) {
         final Topic topic = (Topic) data.get(DataKey.TOPIC);
         // TODO Finalize where links go.
         // TODO Beta topic will go away when beta is done.
         switch (topic) {
         case PRIVACY:
             browse(PRIVACY_LINK);
             break;
         case SECURITY:
             browse(SECURITY_LINK);
             break;
         case SIGNUP:
             browse(SIGNUP_LINK);
             break;
         case BETA:
             browse(BETA_LINK);
             break;
         default:
             Assert.assertUnreachable("Unknown learn more topic.");
         }
     }
 
     /**
      * Browser to the specified topic link.
      * 
      * @param topicLink
      *            A topic link <code>String</code>.
      * @throws BrowserException
      */
     private void browse(final String topicLink) {
         final Link learnMoreLink = LinkFactory.getInstance().create(topicLink);
         try {
             DesktopUtil.browse(learnMoreLink.toString());
         } catch (final DesktopException dx) {
             throw new BrowserException("Cannot open Learn More web page", dx);
         }
     }
 
     public enum DataKey { TOPIC }
     public enum Topic { BETA, PRIVACY, SECURITY, SIGNUP }
 }
