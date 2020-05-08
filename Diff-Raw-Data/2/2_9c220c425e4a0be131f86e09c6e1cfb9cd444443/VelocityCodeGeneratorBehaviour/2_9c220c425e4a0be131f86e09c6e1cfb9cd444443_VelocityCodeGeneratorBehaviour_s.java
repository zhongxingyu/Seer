 package org.jbehave.core.story.codegen.velocity;
 
 import java.io.File;
 
 import org.jbehave.core.mock.UsingMatchers;
 import org.jbehave.core.story.codegen.domain.ScenarioDetails;
 import org.jbehave.core.story.codegen.domain.StoryDetails;
 
 /**
  * 
  * @author Mauro Talevi
  */
 public class VelocityCodeGeneratorBehaviour extends UsingMatchers {
 
     public void shouldGenerateCodeForStoryWithFullScenario() throws Exception {
         // given
        StoryDetails story = new StoryDetails("Joe drinks vodka", "", "", "", "");
         ScenarioDetails scenario1 = new ScenarioDetails();
         scenario1.name = "Happy path";
         scenario1.context.givens.add("a bar downtown");
         scenario1.context.givens.add("a thirsty Joe");
         scenario1.event.name = "Joe asks for a Smirnov";
         scenario1.outcome.outcomes.add("bartender serves Joe");
         scenario1.outcome.outcomes.add("Joe is happy");
         story.addScenario(scenario1);
         ScenarioDetails scenario2 = new ScenarioDetails();
         scenario2.name = "Unhappy path";
         scenario2.context.givens.add("a pub uptown");
         scenario2.context.givens.add("an equally thirsty Joe");
         scenario2.event.name = "Joe asks for an Absolut";
         scenario2.outcome.outcomes.add("bartender tells Joe it is sold out");
         scenario2.outcome.outcomes.add("Joe is unhappy");
         story.addScenario(scenario2);
 
         // when
         String generatedSourceDirectory = "delete_me/generated-src";
         VelocityCodeGenerator generator = new VelocityCodeGenerator(generatedSourceDirectory);
         generator.generateStory(story);
 
         // then
         String[] generatedPaths = new String[]{
            "stories/JoeDrinksVodka.java",
            "scenarios/HappyPath.java",
            "scenarios/UnhappyPath.java",
            "events/JoeAsksForASmirnov.java",      
            "events/JoeAsksForAnAbsolut.java",      
            "givens/ABarDowntown.java",      
            "givens/APubUptown.java",      
            "givens/AThirstyJoe.java",      
            "givens/AnEquallyThirstyJoe.java",      
            "outcomes/BartenderServesJoe.java",      
            "outcomes/BartenderTellsJoeItIsSoldOut.java",      
            "outcomes/JoeIsHappy.java",      
            "outcomes/JoeIsUnhappy.java"
         };
         
         for ( int i = 0; i < generatedPaths.length; i++ ){
             ensureThat(new File(generatedSourceDirectory+File.separator+generatedPaths[i]).exists() );           
         }
     }
 
 }
