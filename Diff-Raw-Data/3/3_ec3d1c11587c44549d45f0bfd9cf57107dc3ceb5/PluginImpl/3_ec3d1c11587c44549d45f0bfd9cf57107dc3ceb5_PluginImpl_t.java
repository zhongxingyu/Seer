 package hudson.plugins.buggame;
 
import hudson.Extension;
 import hudson.Plugin;
 import hudson.model.Hudson;
 
 /**
  * Entry point of the CI Game plugin.
  */
@Extension
 public class PluginImpl extends Plugin {
 
     public static final GameDescriptor GAME_PUBLISHER_DESCRIPTOR = new GameDescriptor();
     public static final UserScorePropertyDescriptor USER_SCORE_PROPERTY_DESCRIPTOR = new UserScorePropertyDescriptor();
 
     @Override
     public void start() throws Exception {
         //Publisher.PUBLISHERS.addNotifier(GAME_PUBLISHER_DESCRIPTOR);
         //UserProperties.LIST.add(USER_SCORE_PROPERTY_DESCRIPTOR);
         Hudson.getInstance().getActions().add(new LeaderBoardAction());
         /*
          * List<UserInfo> users = Hudson.getInstance().getPeople().users;
          * System.out.println("USERS = " + users.size()); for (UserInfo userInfo :
          * users) { UserScoreProperty property =
          * userInfo.getUser().getProperty(UserScoreProperty.class); if (property !=
          * null) { Hudson.getInstance().getActions().add(new
          * LeaderBoardAction()); break; } }
          */
     }
 }
