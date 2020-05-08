 /*
  *  Copyright (C) 2013 caryoscelus
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  * 
  *  Additional permission under GNU GPL version 3 section 7:
  *  If you modify this Program, or any covered work, by linking or combining
  *  it with Clojure (or a modified version of that library), containing parts
  *  covered by the terms of EPL 1.0, the licensors of this Program grant you
  *  additional permission to convey the resulting work. {Corresponding Source
  *  for a non-source form of such a combination shall include the source code
  *  for the parts of Clojure used as well as that of the covered work.}
  */
 
 package chlorophytum.story.view;
 
 import chlorophytum.*;
 import chlorophytum.story.*;
 
 import com.badlogic.gdx.*;
 import com.badlogic.gdx.graphics.*;
 import com.badlogic.gdx.scenes.scene2d.*;
 import com.badlogic.gdx.scenes.scene2d.ui.*;
 import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
 
 /**
  * Stage for displaying story
  */
 public class StoryStage extends Stage {
     public boolean show = false;
     
     public StoryContext storyContext;
     
     /**
      * Stupid string parser.
      * Removes opening and closing and separate what was between them
      * and the rest
      * @param str String to parse
      * @param opening String to cut from
      * @param closing String to cut to
      * @return (parsed string, enclosed text)
      */
     protected String[] parse(String str, String opening, String closing) {
         String cut = null;
         String[] t = str.split("("+opening+"|"+closing+")");
         if (t.length > 1) {
             cut = t[1];
             if (t.length > 2) {
                 str = t[0] + t[2];
             } else {
                 str = t[0];
             }
         }
         
         String[] r = new String[2];
         r[0] = str;
         r[1] = cut;
         return r;
     }
     
     protected String[] parseAll (String labelText) {
         // remove superflous white space
         labelText = labelText.replace("\t", " ");
         labelText = labelText.replace("\n", " ");
         while (labelText.matches("  ")) {
             labelText = labelText.replace("  ", " ");
         }
         
         // now add some line-breaks for paragraphs
         labelText = labelText.replace("^", "\n");
         
         // now check if we should display a picture
         String[] t = parse(labelText, "<img:", ">");
         
         // at this point t = [labelText, img], and that's what we need for now!
         return t;
     }
     
     public void setContext (StoryContext context) {
         if (context == null) {
             Gdx.app.error("setContext", "context is null");
         }
         
         storyContext = context;
         if (context != null) {
             setupUi(context);
             show = true;
         }
     }
     
     /**
      * Setup storyStage from dialogue.
      * This is quite a mess, needs lots of refactoring
      */
     protected void setupUi (StoryContext piece) {
         final Skin skin = UiManager.instance().skin();
         final Table table = new Table();
         table.setFillParent(true);
         
         final Window winDialog = new Window("----", skin);
         table.add(winDialog).width(600).height(400);
         
         String[] parsed = parseAll(piece.getText());
         
         String labelText = parsed[0];
         String img = parsed[1];
         
         if (img != null) {
             final Image image = new Image(new Texture(Gdx.files.internal(img)));
             winDialog.add(image);
             winDialog.row();
         }
         
         final Label label = new Label(labelText, skin);
         label.setWrap(true);
         winDialog.add(label).space(6).pad(2).expand().fillX().top().left();
         
         // dialogue options
         for (StoryDialogLine line : piece.getLines()) {
             if (line.visible) {
                 final String text = line.text;
                 final StoryEvent event = line.event;
                 
                 final TextButton button = new TextButton(text, skin);
                 button.addListener(new ChangeListener() {
                     public void changed (ChangeEvent cevent, Actor actor) {
                         event.trigger(storyContext);
                     }
                 });
                 winDialog.row();
                winDialog.add(button).pad(2);
             }
         }
         
         winDialog.top();
         winDialog.pack();
         
         table.top();
         
         addActor(table);
     }
     
     public void act (float dt) {
         super.act(dt);
         
         if (storyContext == null) {
             show = false;
         } else if (storyContext.finished()) {
             show = false;
             storyContext = null;
         } else {
             if (!show) {
                 setupUi((StoryContext)storyContext);
                 show = true;
             }
         }
     }
 }
