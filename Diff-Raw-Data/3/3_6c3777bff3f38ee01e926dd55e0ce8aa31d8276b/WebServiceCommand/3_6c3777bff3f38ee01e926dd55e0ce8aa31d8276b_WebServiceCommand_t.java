 package com.spartansoftwareinc.globalsight.gscli;
 
 import java.rmi.RemoteException;
 import java.util.Date;
 
 import javax.xml.stream.XMLInputFactory;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 
 @SuppressWarnings("static-access")
 public abstract class WebServiceCommand extends Command {
 
     private Profile profile;
     
     // Default implementation that actually wants a webservice.
     public void handle(CommandLine command, UserData userData) 
                                 throws Exception {
         if (command.hasOption(PROFILE)) {
             profile = userData.getProfiles()
                 .getProfile(command.getOptionValue(PROFILE));
            if (profile == null) {
                die("Not such profile: '" + command.getOptionValue(PROFILE) + "'");
            }
         }
         else {
             profile = userData.getProfiles().getDefaultProfile();
             if (profile != null) {
                 verbose("Using default profile '" + 
                         profile.getProfileName() + "'");
             }
             else {
                 die("No default profile set; must use --profile");
             }
         }
         String url = null;
         if (command.hasOption(URL)) {
             url = command.getOptionValue(URL);
         }
         else if (profile != null){
             url = profile.getUrl(); 
         }
         if (url == null) {
             die("No URL specified.  Specify with --url=<url>");
         }
         WebService ws = new WebService(url, XMLInputFactory.newInstance());
         // XXX Ugly
         // TODO: also, lazily calculate this -- right now we always do it
         // even if the command dies immediately
         String token = getAuthToken(userData, profile);
         try {
             execWithAuth(ws, userData, command, token, profile);
         }
         catch (RemoteException e) {
             ErrorParser parser = new ErrorParser();
             Error error = parser.parse(e.getMessage());
             if (error instanceof InvalidTokenError) {
                 try {
                     execWithAuth(ws, userData, command, null, profile);
                 }
                 catch (RemoteException e2) {
                     dieWithError(parser.parse(e2.getMessage()));
                 }
             }
             else {
                 dieWithError(error);
             }
         }
     }
 
     static final String PROFILE = "profile";
     static final Option PROFILE_OPT = OptionBuilder
         .withArgName(PROFILE)
         .hasArg()
         .withDescription("profile name")
         .create(PROFILE);
     
     @Override
     public Options getOptions() {
         Options options = super.getOptions();
         options.addOption(PROFILE_OPT);
         return options;
     }
     
     void execWithAuth(WebService ws, UserData userData, CommandLine command,
                       String token, Profile profile) throws Exception {
         if (token == null) {
             token = authorize(ws, userData);
             verbose("Received token " + token);
             // Update Session cache
             Sessions sessions = userData.getSessions();
             sessions.setSession(profile.getProfileName(), token);
             userData.setSessions(sessions);
         }
         else {
             verbose("Using cached auth token " + token);
         }
         ws.setToken(token);
         execute(command, userData, ws);
     }
     
     private void dieWithError(Error error) {
         die("Webservice error (" + error.getStatus() + "): " +
                 error.getError());
     }
     
     protected Profile getProfile() {
         return profile;
     }
     
     protected String authorize(WebService ws, UserData userData) 
                         throws Exception {
         String username = null, password = null;
         if (getProfile() != null) {
             username = getProfile().getUsername();
             password = getProfile().getPassword();
         }
         if (username == null) {
             die("No username specified.  Specify with --user=<username>");
         }
         if (password == null) {
             die("No password specified.  Specify with --password=<password>");
         }
         return ws.login(username, password);
     }
     
     protected String getAuthToken(UserData userData, Profile profile) throws Exception {
         if (profile == null) {
             return null;
         }
         Sessions sessions = userData.getSessions();
         Session session = sessions.getSession(profile.getProfileName());
         if (session == null) {
             return null;
         }
         // XXX Is this the right place to validate the timestamp?
         String token = session.getToken();
         if (token == null) {
             return null;
         }
         Date timestamp = session.getDate(), now = new Date();
         if (!timestamp.before(now)) {
             // Strange date corruption
             return null; 
         }
         long delta = (now.getTime() - timestamp.getTime());
         // Expire the token
         if (delta > ONE_HOUR) {
             return null;
         }
         return token;
     }
     
     public static final long ONE_HOUR = 1000 * 60 * 60;
     
     protected abstract void execute(CommandLine command, UserData userData, 
                 WebService webService) throws Exception;
     
 }
