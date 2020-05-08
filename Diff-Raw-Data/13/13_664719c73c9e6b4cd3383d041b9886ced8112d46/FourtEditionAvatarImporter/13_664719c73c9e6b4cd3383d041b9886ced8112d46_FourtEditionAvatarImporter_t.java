 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.sizzle.dd.core.importer;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import org.openide.util.Lookup;
 import org.openide.util.lookup.ServiceProvider;
 import org.sizzle.dd.core.Avatar;
 import org.sizzle.dd.core.AvatarRace;
 import org.sizzle.dd.core.modifier.InitialAbilityScoreModifier;
 import org.sizzle.dd.core.properties.AbilityModifierProperty;
 import org.sizzle.dd.core.properties.AbilityScoreProperty;
 import org.sizzle.dd.core.properties.RaceProperty;
 import org.sizzle.rpg.core.AbstractAvatar;
 import org.sizzle.rpg.core.IAvatar;
 import org.sizzle.rpg.core.importer.AvatarImporter;
 import org.sizzle.rpg.core.model.IModifier;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  *
  * @author Jason
  */
 @ServiceProvider(service = AvatarImporter.class)
 public class FourtEditionAvatarImporter extends AvatarImporter {
 
     @Override
     public void process(IAvatar avatar, Document document) {
 
         clearAbilities(Avatar.class.cast(avatar));
        
        RaceProperty raceProperty = avatar.find(RaceProperty.class);
        if(raceProperty!=null)
            avatar.removeProperty(raceProperty);
        
         setAbilityInitialScores(Avatar.class.cast(avatar), document);
         
         setRace(Avatar.class.cast(avatar), document);
     }
 
     @SuppressWarnings("unchecked")
     private void clearAbilities(AbstractAvatar avatar) {
         for (AbilityScoreProperty score : avatar.getLookup().lookupAll(AbilityScoreProperty.class)) {
             score.unsetValue();
             for (IModifier<?> mod : score.getLookup().lookupAll(IModifier.class)) {
                 score.removeModifier((IModifier<Integer>) mod);
             }
         }
         for (AbilityModifierProperty modifier : avatar.getLookup().lookupAll(AbilityModifierProperty.class)) {
             modifier.unsetValue();
             for (IModifier<?> mod : modifier.getLookup().lookupAll(IModifier.class)) {
                 modifier.removeModifier((IModifier<Integer>) mod);
             }
         }
     }
 
     private void setAbilityInitialScores(AbstractAvatar avatar, Document doc) {
         avatar.<Integer>find(AbilityScoreProperty.SLUG.STRENGTH_SCORE).addModifier(new InitialAbilityScoreModifier(Integer.parseInt(doc.getElementsByTagName("Strength").item(0).getAttributes().getNamedItem("score").getNodeValue())));
         avatar.<Integer>find(AbilityScoreProperty.SLUG.CONSTITUTION_SCORE).addModifier(new InitialAbilityScoreModifier(Integer.parseInt(doc.getElementsByTagName("Constitution").item(0).getAttributes().getNamedItem("score").getNodeValue())));
         avatar.<Integer>find(AbilityScoreProperty.SLUG.DEXTERITY_SCORE).addModifier(new InitialAbilityScoreModifier(Integer.parseInt(doc.getElementsByTagName("Dexterity").item(0).getAttributes().getNamedItem("score").getNodeValue())));
         avatar.<Integer>find(AbilityScoreProperty.SLUG.INTELLIGENCE_SCORE).addModifier(new InitialAbilityScoreModifier(Integer.parseInt(doc.getElementsByTagName("Intelligence").item(0).getAttributes().getNamedItem("score").getNodeValue())));
         avatar.<Integer>find(AbilityScoreProperty.SLUG.WISDOM_SCORE).addModifier(new InitialAbilityScoreModifier(Integer.parseInt(doc.getElementsByTagName("Wisdom").item(0).getAttributes().getNamedItem("score").getNodeValue())));
         avatar.<Integer>find(AbilityScoreProperty.SLUG.CHARISMA_SCORE).addModifier(new InitialAbilityScoreModifier(Integer.parseInt(doc.getElementsByTagName("Charisma").item(0).getAttributes().getNamedItem("score").getNodeValue())));
     }
 
     private void setRace(Avatar avatar, Document doc) {
         // Query the Rules elements
         NodeList ruleElementList = doc.getElementsByTagName("RulesElement");
         Node ruleElement;
         HashSet<String> raceNodes = new HashSet<>(0);
         for (int i = 0; i < ruleElementList.getLength(); i++) {
             ruleElement = ruleElementList.item(i);
             if(ruleElement.hasAttributes()) {
                 Node nameNode = ruleElement.getAttributes().getNamedItem("name");
                 Node typeNode = ruleElement.getAttributes().getNamedItem("type");
                 if (nameNode!=null 
                         && !nameNode.getNodeValue().isEmpty() 
                         && typeNode != null 
                         && typeNode.getNodeValue().matches("Race")
                         && !raceNodes.contains(nameNode.getNodeValue())) {
                     raceNodes.add(nameNode.getNodeValue());
                 }
             }
         }
         // Find race in global lookup
         Lookup.Result<AvatarRace> loadedRaceResults = Lookup.getDefault().lookupResult(AvatarRace.class);
         for (String race : raceNodes) {
             boolean raceFound = false;
             for (AvatarRace item : loadedRaceResults.allInstances()) {
                 if (item.name.contentEquals(race)) {
                     raceFound = true;
                     avatar.addProperty(new RaceProperty(item));
                 }
             }
             if (!raceFound) {
                 System.err.println("Race Not Loaded From Import: " + race);
             }
         }
     }
 }
