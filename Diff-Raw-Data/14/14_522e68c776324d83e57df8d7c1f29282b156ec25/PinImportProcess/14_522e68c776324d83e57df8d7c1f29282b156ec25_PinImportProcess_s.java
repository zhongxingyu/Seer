 package is.ru.honn.rupin.process;
 
 import is.ru.honn.rupin.domain.Board;
 import is.ru.honn.rupin.domain.Pin;
 import is.ru.honn.rupin.domain.User;
 import is.ru.honn.rupin.feeds.FeedEntry;
 import is.ru.honn.rupin.feeds.FeedException;
 import is.ru.honn.rupin.feeds.FeedHandler;
 import is.ru.honn.rupin.feeds.FeedReader;
 import is.ru.honn.rupin.service.BoardNotFoundException;
 import is.ru.honn.rupin.service.PinService;
 import is.ru.honn.rupin.service.UserNotFoundException;
 import is.ru.honn.rupin.service.UsernameExistsException;
 import is.ruframework.process.RuAbstractProcess;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.MessageSource;
 import org.springframework.context.support.FileSystemXmlApplicationContext;
 
 import java.util.List;
 import java.util.Locale;
 import java.util.logging.Logger;
 
 /**
  * User: kristjant08@ru.is
  * Kristján Eldjárn Þóroddsson
  * Date: 15.10.2012
  * Time: 09:05
  */
 public class PinImportProcess extends RuAbstractProcess implements FeedHandler {
 
     // skoða F11+Process+Design+A.avi 34:53mín
     Logger log = Logger.getLogger(this.getClass().getName());
     PinService pinService;
     FeedReader reader;
     MessageSource msg;
     User user;
     Board board;
 
     public void beforeProcess() {
         ApplicationContext psx = new FileSystemXmlApplicationContext("process.xml");
 
         ApplicationContext ctx = new FileSystemXmlApplicationContext("app.xml");
         pinService = (PinService) ctx.getBean("contentService");
         reader = (FeedReader) ctx.getBean("feedReader");
         reader.setFeedHandler(this);
         msg = (MessageSource) ctx.getBean("messageSource");
         log.info(msg.getMessage("processbefore",
                 new Object[]{getProcessContext().getProcessName()},
                 Locale.getDefault()));
 
         user = (User) psx.getBean("userBean");
         try {
             pinService.signUpUser(user.getUsername(), user.getFirstName(), user.getLastName(),
                     user.getEmail(), user.getPassword(), user.getGender());
         } catch (UsernameExistsException e) {
             e.printStackTrace();
         }
 
         board = (Board) psx.getBean("boardBean");
         try {
             pinService.createBoard(user.getUsername(), board.getName(), board.getCategory());
         } catch (UserNotFoundException e) {
             e.printStackTrace();
         }
     }
 
     public void processEntry(FeedEntry entry) {
         try {
            pinService.createPin(user.getUsername(), board.getName(), "www.mbl.is", "description");
         } catch (BoardNotFoundException e) {
             e.printStackTrace();
         }
     }
 
     @Override
     public void startProcess() {
         log.info(msg.getMessage("processstart",
                 new Object[]{getProcessContext().getProcessName()},
                 Locale.getDefault()));
 
         reader.setSource(getProcessContext().getImportURL());//?????
         try {
             reader.read();
         } catch (FeedException e) {
            e.printStackTrace();
         }
 
         log.info(msg.getMessage("processstartdone",
                 new Object[]{pinService.getPinsOnBoard(user.getUsername(), board.getName()).size()},
                 Locale.getDefault()));
     }
 
     public void afterProcess() {
         List<Pin> pins = pinService.getPinsOnBoard(user.getUsername(), board.getName());
         for (Pin i : pins) {
            System.out.println(i);
         }
     }
 }
