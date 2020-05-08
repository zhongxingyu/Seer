 package com.github.dmalch;
 
 import com.github.dmalch.components.Editor;
 import com.github.dmalch.components.EditorImpl;
 import org.junit.Test;
 import org.netbeans.jemmy.ClassReference;
 import org.netbeans.jemmy.operators.JFrameOperator;
 
 import java.lang.reflect.InvocationTargetException;
 
 import static com.github.dmalch.components.EditorImpl.editor;
 import static org.hamcrest.Matchers.containsString;
 import static org.hamcrest.Matchers.isEmptyString;
 
 public class JEditTest {
 
     @Test
     public void testTypeText() throws Exception {
         final String expectedText = "sample text";
 
         openEditor()
                 .typeText(expectedText)
                 .then(editor(containsString(expectedText)));
     }
 
     @Test
     public void testUndoType() throws Exception {
         final String expectedText = "sample text";
 
         openEditor()
                 .typeText(expectedText)
                 .clickUndo()
                 .then(editor(isEmptyString()));
     }
 
     @Test
     public void testRedoType() throws Exception {
         final String expectedText = "sample text";
 
         openEditor()
                 .typeText(expectedText)
                 .clickUndo()
                 .clickRedo()
                 .then(editor(containsString(expectedText)));
     }
 
     private Editor openEditor() throws InvocationTargetException, NoSuchMethodException, ClassNotFoundException, InterruptedException {
        new ClassReference("org.gjt.sp.jedit.jEdit").startApplication(new String[]{"-noserver"});
         return new EditorImpl(new JFrameOperator());
     }
 }
