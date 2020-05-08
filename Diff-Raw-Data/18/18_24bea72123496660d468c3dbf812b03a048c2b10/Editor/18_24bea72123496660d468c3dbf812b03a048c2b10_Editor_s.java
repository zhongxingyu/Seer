 package suite.editor;
 
 /**
  * A boring editor.
  */
 public class Editor {
 
 	public static void main(String args[]) {
 		EditorController controller = new EditorController();
 
 		EditorView view = new EditorView();
 		view.setController(controller);
		view.run();
 	}
 
 }
