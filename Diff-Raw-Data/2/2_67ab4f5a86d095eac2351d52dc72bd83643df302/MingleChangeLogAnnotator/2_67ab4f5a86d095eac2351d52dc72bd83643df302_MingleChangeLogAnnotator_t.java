 package mingleplugin;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.lang.String;
 
 import hudson.Extension;
 import hudson.MarkupText;
 import hudson.Util;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.scm.ChangeLogAnnotator;
 import hudson.scm.ChangeLogSet.Entry;
 
 /**
  * {@link ChangeLogAnnotator} that picks up MingleCard numbers.
  */
 @Extension
 public class MingleChangeLogAnnotator extends ChangeLogAnnotator {
  private static final Logger LOGGER = Logger.getLogger(MingleChangeLogAnnotator.class.getName()); // Not working!
   
   @Override
   public void annotate(AbstractBuild<?,?> build, Entry change, MarkupText text) {
 
     LOGGER.info("Started ChangeLogAnnotator");
 
     MingleRestService serv = getMingleServiceForProject(build.getProject());
     if(serv==null) return;    // not configured with Mingle
 
     // if there's any recorded detail information, try to use that, too.
     MingleBuildAction a = build.getAction(MingleBuildAction.class);
 
     Set<MingleCard> cardToBeSaved = new HashSet<MingleCard>();
 
     Pattern pattern = serv.getCardPattern();
         
     if (LOGGER.isLoggable(Level.FINE)) {
       LOGGER.fine("Using card pattern: " + pattern);
     }
     
     String plainText = text.getText();
     
     Matcher m = pattern.matcher(plainText);
     
     while (m.find()) {
       if (m.groupCount() >= 1) {
         
         int id = Integer.parseInt(m.group(1));
         LOGGER.info("Annotating Card number: '" + id + "'");
           
         MingleCard card = null;
         if (a != null) {
           card = a.getCard(id); // try to get cached card
         }
 
         if (card == null) {
           try {
             card = serv.getCard(id);
             if (card != null) {
               cardToBeSaved.add(card); // cache the card
             }
           } catch (Exception e) {
             LOGGER.log(Level.WARNING, "Error getting remote card " + id, e);
           }
         }
         
         URL url;
         try {
           url = serv.getCardUrl(id);
         } catch (MalformedURLException e) {
           throw new AssertionError(e); // impossible
         }
         if(card==null) {
           text.addMarkup(m.start(1), m.end(1), "<a href='"+url+"'>", "</a>");
         } else {
           text.addMarkup(m.start(1), m.end(1),
           String.format("<a href='%s' tooltip='%s'>",url, Util.escape( card.getName() ) ), "</a>");
         }
         
       } else {
         LOGGER.log(Level.WARNING, "The pattern " + pattern + " doesn't define a capturing group!");
       }
     }
     
     if (!cardToBeSaved.isEmpty()) {
         saveCards(build, a, cardToBeSaved);
     }
   }
 
 
   private void saveCards(AbstractBuild<?, ?> build, MingleBuildAction a, Set<MingleCard> cardToBeSaved) {
     if (a != null) {
       a.addCard(cardToBeSaved);
     } else {
       MingleBuildAction action = new MingleBuildAction(build, cardToBeSaved);
       build.addAction(action);
     }
     
     try {
       build.save();
     } catch (final IOException e) {
       LOGGER.log(Level.WARNING, "Error saving updated build", e);
     }
   }
   MingleRestService getMingleServiceForProject(AbstractProject<?, ?> project) {
     return null; //MingleRestService.get(project);
   }
 
 }
