 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.wontonst.ghgformat.file;
 
 import com.wontonst.ghgformat.exceptions.IncompleteTopicException;
 import com.wontonst.ghgformat.parser.TopicBuilder;
 import com.wontonst.ghgrader.gui.RequirementGui;
 import com.wontonst.ghgrader.gui.TopicGui;
 import com.wontonst.util.BuildString;
 import java.util.ArrayList;
 import java.util.List;
 import javax.swing.JPanel;
 
 /**
  *
  * @author RoyZheng
  */
 public class Topic extends Component {
 
     int value = 0, input_value;
     List<Requirement> requirements;
 
     private Topic() {
     }
 
     public Topic(TopicBuilder builder) throws IncompleteTopicException {
         List<String> c = builder.check();
         if (c != null && !c.isEmpty()) {
             throw new IncompleteTopicException(builder.getTitle(), BuildString.Build(c, " "));
         }
         this.title = builder.getTitle();
         this.requirements = builder.getRequirements();
         this.value = builder.getValue();
     }
 
     @Override
     public void toString(Format f, StringBuilder b) {
         switch (f) {
             case MD:
             case HTML:
             case JEKYLL_HTML:
             case JEKYLL_MD:
                 b.append("###");
                 b.append(this.title);
                 b.append(" [");
                 b.append(this.value);
                 b.append(this.value == 1 ? " point" : " points");
                 b.append("]");
                 break;
             case GHG:
                 b.append(this.title);
                 break;
         }
         for (Requirement r : this.requirements) {
             b.append("\n");
             r.toString(f, b);
         }
     }
 
     public Topic deepClone() {
         Topic t = new Topic();
         t.value = this.value;
         t.input_value = this.input_value;
         List<Requirement> recs = new ArrayList<Requirement>();
         for (Requirement r : this.requirements) {
             recs.add(r.deepClone());
         }
         t.requirements = recs;
         return t;
     }
 
     @Override
     public TopicGui toPanel() {
         List<RequirementGui> rgui = new ArrayList<RequirementGui>();
         for (Requirement r : this.requirements) {
             rgui.add(r.toPanel());
         }
         return new TopicGui(this, rgui);
     }
 }
