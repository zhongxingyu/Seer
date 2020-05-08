 package org.sikora.ruler;
 
 import com.melloware.jintellitype.HotkeyListener;
 import com.melloware.jintellitype.JIntellitype;
 import org.sikora.ruler.context.Context;
 import org.sikora.ruler.context.ContextProvider;
 import org.sikora.ruler.context.WindowsContextProvider;
 import org.sikora.ruler.model.input.InputDriver;
 import org.sikora.ruler.model.input.InputDriver.Event;
 import org.sikora.ruler.task.*;
 import org.sikora.ruler.task.impl.BaseDefinitionRepository;
 import org.sikora.ruler.task.impl.DefinitionDraftFactory;
 import org.sikora.ruler.ui.awt.AwtInputDriver;
 import org.sikora.ruler.ui.awt.AwtResultWindow;
 
 import static org.sikora.ruler.model.input.InputDriver.Command.*;
 
 /**
  * Input driver handler. It provides context based hints and executes recognized tasks. It is activated by
  * global hot key.
  */
 public class Ruler implements InputDriver.Handler, HotkeyListener {
   private final InputDriver inputDriver;
   private final DraftFactory draftFactory;
   private final AwtResultWindow resultWindow;
   private final ContextProvider contextProvider;
   private Context currentContext;
 
   /**
    * Configures global key hooks and wires Ruler to it. It uses AwtInputDriver for receiving users input.
    *
    * @param args ignored
    */
   public static void main(final String[] args) {
     final UiConfiguration uiConfiguration = new UiConfiguration(new AwtInputDriver(), new AwtResultWindow());
     DefinitionRepository definitionRepository = new BaseDefinitionRepository();
     Ruler ruler = new Ruler(uiConfiguration, definitionRepository, new WindowsContextProvider());
     uiConfiguration.driver().addHandler(ruler);
 
     JIntellitype.setLibraryLocation("../lib/JIntellitype64.dll");
     JIntellitype hook = JIntellitype.getInstance();
     hook.addHotKeyListener(ruler);
     hook.registerHotKey(1, JIntellitype.MOD_CONTROL, (int) ' ');
     hook.registerHotKey(2, JIntellitype.MOD_CONTROL + JIntellitype.MOD_SHIFT, (int) ' ');
   }
 
   private Ruler(final UiConfiguration uiConfiguration, final DefinitionRepository definitionRepository, final ContextProvider contextProvider) {
     this.inputDriver = uiConfiguration.driver();
     this.draftFactory = new DefinitionDraftFactory(definitionRepository);
     this.resultWindow = uiConfiguration.resultWindow();
     this.contextProvider = contextProvider;
     this.currentContext = contextProvider.currentContext();
   }
 
   /**
    * Based on input driver event provides context based hints back to the driver.
    * It also executes recognized tasks.
    *
    * @param event input driver event
    */
   public void dispatch(final Event event) {
     final Draft draft = draftFactory.draftFrom(event, currentContext);
     switch (event.command()) {
       case SUBMIT_INPUT:
         if (draft.isTaskComplete()) {
           final Task task = draft.toTask();
           final Result result = task.performAction();
           result.showOn(resultWindow);
         }
         break;
       case CANCEL:
         event.driver().issue(HIDE_INPUT);
         event.driver().issue(RESET_INPUT);
         break;
     }
   }
 
   /**
    * Global hot key hook. It focuses input window or result window based on a hot key.
    *
    * @param hook hook id
    */
   public void onHotKey(final int hook) {
     switch (hook) {
       case 1:
         currentContext = contextProvider.currentContext();
         inputDriver.issue(FOCUS_INPUT);
         break;
       case 2:
         resultWindow.display();
         break;
       default:
         break;
     }
   }
 
  public static class UiConfiguration {
     private final InputDriver inputDriver;
     private final AwtResultWindow resultWindow;
 
     private UiConfiguration(final InputDriver inputDriver, final AwtResultWindow resultWindow) {
       this.inputDriver = inputDriver;
       this.resultWindow = resultWindow;
     }
 
     public InputDriver driver() {
       return inputDriver;
     }
 
     public AwtResultWindow resultWindow() {
       return resultWindow;
     }
   }
 }
 
