 /**
  * ReactionEditor.java
  *
  * 2012.02.13
  *
  * This file is part of the CheMet library
  * 
  * The CheMet library is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * CheMet is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with CheMet.  If not, see <http://www.gnu.org/licenses/>.
  */
 package uk.ac.ebi.chemet.render.reaction;
 
 import java.awt.Window;
 import javax.swing.Box;
 import javax.swing.JComboBox;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import org.apache.log4j.Logger;
 import uk.ac.ebi.caf.component.factory.ComboBoxFactory;
import uk.ac.ebi.chemet.entities.reaction.DirectionImplementation;
 import uk.ac.ebi.core.DefaultEntityFactory;
 import uk.ac.ebi.interfaces.entities.MetabolicParticipant;
 import uk.ac.ebi.interfaces.entities.MetabolicReaction;
 import uk.ac.ebi.interfaces.reaction.Direction;
 
 
 /**
  *
  *          ReactionEditor 2012.02.13
  * @version $Rev$ : Last Changed $Date$
  * @author  johnmay
  * @author  $Author$ (this version)
  *
  *          Class description
  *
  */
 public class ReactionEditor {
 
     private static final Logger LOGGER = Logger.getLogger(ReactionEditor.class);
 
     private ReactionSideEditor left;
 
     private ReactionSideEditor right;
 
    private JComboBox direction = ComboBoxFactory.newComboBox(DirectionImplementation.values());
 
     private JComponent component;
 
     private MetabolicReaction rxn;
 
 
     public ReactionEditor(Window window) {
         left = new ReactionSideEditor(window);
         right = new ReactionSideEditor(window);
 
         component = Box.createHorizontalBox();
 
         component.add(left.getComponent());
         component.add(direction);
         component.add(right.getComponent());
 
         left.setSize(1);
         right.setSize(1);
 
     }
 
 
     public JComponent getComponent() {
         return component;
     }
 
 
     public void setReaction(MetabolicReaction rxn) {
         this.rxn = rxn;
         left.setParticipants(rxn.getReactants());
         right.setParticipants(rxn.getProducts());
     }
 
 
     public MetabolicReaction getReaction() {
 
         rxn = rxn == null ? DefaultEntityFactory.getInstance().newInstance(MetabolicReaction.class) : rxn;
 
         rxn.clear();
 
         for (MetabolicParticipant participant : left.getParticipants()) {
             rxn.addReactant(participant);
         }
 
         rxn.setDirection((Direction)direction.getSelectedItem());
 
         for (MetabolicParticipant participant : right.getParticipants()) {
             rxn.addProduct(participant);
         }
 
         return rxn;
 
     }
 
 
     public static void main(String[] args) {
         JFrame frame = new JFrame();
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         ReactionEditor editor = new ReactionEditor(frame);
         frame.add(editor.getComponent());
         frame.setVisible(true);
     }
 }
