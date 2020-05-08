 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package pjwiki;
 
import javax.swing.JOptionPane;

 /**
  *
  * @author Patrick
  */
 public class PjWikiViewStateMachine
 {
     public PjWikiViewStateMachine(PjWikiView view)
     {
         this.view = view;
         currentState = new Viewing();
     }
     
     State currentState;
     PjWikiView view;
     
     public void transitionNavigate(WikiWordPageBase word) throws Exception
         { currentState = currentState.transitionNavigate(word); }
     public void transitionEdit(WikiWordPageBase word) throws Exception
         { currentState = currentState.transitionEdit(word);  }
     public void transitionCancel(WikiWordPageBase word) throws Exception
         { currentState = currentState.transitionCancel(word);  }
     public void transitionSave(WikiWordPageBase word) throws Exception
         { currentState = currentState.transitionSave(word);  }
     public void transitionPreview(WikiWordPageBase word) throws Exception
         { currentState = currentState.transitionPreview(word);  }
     public void transitionExit(WikiWordPageBase word) throws Exception
         { currentState = currentState.transitionExit(word);  }
     
     public abstract class State
     {
         public abstract State transitionNavigate(WikiWordPageBase word) throws Exception;
         public abstract State transitionEdit(WikiWordPageBase word) throws Exception;
         public abstract State transitionCancel(WikiWordPageBase word) throws Exception;
         public abstract State transitionSave(WikiWordPageBase word) throws Exception;
         public abstract State transitionPreview(WikiWordPageBase word) throws Exception;
         public abstract State transitionExit(WikiWordPageBase word) throws Exception;
     }
     
     public class Viewing extends State
     {
         public Viewing()
         {
             view.showViewing();
         }
 
         @Override
         public State transitionNavigate(WikiWordPageBase word) throws Exception {
             if(word.exists())
             {
                 view.setCurrentWikiWordPage(word);
                 return new Viewing();
             }
             else
             {
                 if(view.displayCreateNewWordDialog(word))
                 {
                     view.loadContent();
                     return new Editing();
                 }
                 else if(view.setCurrentWikiWordPageFromHistory(-1))
                 {
                     return this;
                 }
                 else
                 {
                     return transitionExit(null);
                 }
             }
         }
 
         @Override
         public State transitionEdit(WikiWordPageBase word) throws Exception {
             if(word.exists())
             {
                 view.loadContent();
                 return new Editing();
             }
             else
             {
                 return transitionExit(null);
             }
         }
 
         @Override
         public State transitionCancel(WikiWordPageBase word) throws Exception {
             return this;
         }
 
         @Override
         public State transitionSave(WikiWordPageBase word) throws Exception {
             return this;
         }
 
         @Override
         public State transitionPreview(WikiWordPageBase word) throws Exception {
             return this;
         }
 
         @Override
         public State transitionExit(WikiWordPageBase word) throws Exception {
             PjWikiApp.getApplication().exit();
             return null;
         }
     }
     
     public class Editing extends State
     {
         public Editing()
         {
             view.showEditing();
             System.out.println("Editing");
         }
 
         @Override
         public State transitionNavigate(WikiWordPageBase word) throws Exception {
            if(view.displaySaveChangesDialoge())
             {
                 return new Viewing(); // load and display current text
             }
             else
             {
                 return this;
             }
         }
 
         @Override
         public State transitionEdit(WikiWordPageBase word) throws Exception {
             return this;
         }
 
         @Override
         public State transitionCancel(WikiWordPageBase word) throws Exception {
             return new Viewing();
         }
 
         @Override
         public State transitionSave(WikiWordPageBase word) throws Exception {
             if(view.saveCurrentWord())
             {
                 return this;
             }
             else
             {
                 return new Viewing();
             }
             
         }
 
         @Override
         public State transitionPreview(WikiWordPageBase word) throws Exception {
             return new Previewing();
         }
 
         @Override
         public State transitionExit(WikiWordPageBase word) throws Exception {
            if(view.displaySaveChangesDialoge())
             {
                 return null;
             }
             else
             {
                 return this;
             }
         }
 
     }
 
     public class Previewing extends State
     {
         public Previewing()
         {
             view.showPreviewing();
             System.out.println("Previewing");
         }
 
         @Override
         public State transitionNavigate(WikiWordPageBase word) throws Exception {
             throw new UnsupportedOperationException("Not supported yet.");
         }
 
         @Override
         public State transitionEdit(WikiWordPageBase word) throws Exception {
             view.showEditing();
             return new Editing();
         }
 
         @Override
         public State transitionCancel(WikiWordPageBase word) throws Exception {
             return new Viewing();
         }
 
         @Override
         public State transitionSave(WikiWordPageBase word) throws Exception {
             if(view.saveCurrentWord())
             {
                 return new Viewing();
             }
             else
             {
                 return this;
             }
         }
 
         @Override
         public State transitionPreview(WikiWordPageBase word) throws Exception {
             return this;
         }
 
         @Override
         public State transitionExit(WikiWordPageBase word) throws Exception {
            if(view.displaySaveChangesDialoge())
             {
                 return null;
             }
             else
             {
                 return this;
             }
            
         }
 
     }
 
 }
