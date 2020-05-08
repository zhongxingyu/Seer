 package edu.utdallas.gamegenerator.LearningObjective.Screen;
 
 import edu.utdallas.gamegenerator.LearningObjective.Character.LearningObjectiveCharacter;
 import edu.utdallas.gamegenerator.LearningObjective.Prop.GameButton;
 import edu.utdallas.gamegenerator.LearningObjective.Prop.GameText;
 import edu.utdallas.gamegenerator.Theme.ThemeStoryScreenIntro;
 import edu.utdallas.gamegenerator.Theme.ThemeStoryScreenOutro;
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlElementWrapper;
 import javax.xml.bind.annotation.XmlSeeAlso;
 import javax.xml.bind.annotation.XmlTransient;
 import java.util.List;
 
 /**
  * User: clocke
  * Date: 2/17/13
  * Time: 3:46 PM
  */
 @XmlTransient
 @XmlSeeAlso({LearningObjectiveFailure.class, ThemeStoryScreenIntro.class, ThemeStoryScreenOutro.class})
 public abstract class LearningObjectiveScreen implements Cloneable {
     private List<GameText> informationBoxes;
     private List<GameButton> buttons;
     private List<LearningObjectiveCharacter> characters;
 
     public List<GameText> getInformationBoxes() {
         return informationBoxes;
     }
 
     @XmlElementWrapper(name = "InformationBoxes")
     @XmlElement(name = "InformationBox")
     public void setInformationBoxes(List<GameText> informationBoxes) {
         this.informationBoxes = informationBoxes;
     }
 
     public List<GameButton> getButtons() {
         return buttons;
     }
 
     @XmlElementWrapper(name = "Buttons")
     @XmlElement(name = "Button")
     public void setButtons(List<GameButton> buttons) {
         this.buttons = buttons;
     }
 
     public List<LearningObjectiveCharacter> getCharacters() {
         return characters;
     }
 
    @XmlElementWrapper(name = "LOCharacters")
    @XmlElement(name = "Character")
     public void setCharacters(List<LearningObjectiveCharacter> characters) {
         this.characters = characters;
     }
 
     public abstract ScreenType getType();
 
     public LearningObjectiveScreen clone() {
         try {
             return (LearningObjectiveScreen) super.clone();
         } catch (CloneNotSupportedException e) {
 
         }
         return null;
     }
 }
