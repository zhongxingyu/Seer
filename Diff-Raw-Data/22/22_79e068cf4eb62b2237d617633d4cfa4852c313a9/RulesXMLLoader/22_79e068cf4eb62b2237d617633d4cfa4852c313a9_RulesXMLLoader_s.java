 package vooga.towerdefense.gameeditor.gameloader.xmlloaders;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.w3c.dom.Element;
 
 import util.XMLTool;
 import vooga.towerdefense.factories.rulefactories.RuleFactory;
 import vooga.towerdefense.model.GameModel;
 import vooga.towerdefense.model.rules.Rule;
 
 public class RulesXMLLoader {
 
     private XMLTool myXMLTool;
 
     public RulesXMLLoader (XMLTool xmlTool) {
         myXMLTool = xmlTool;
     }
     
     public List<Rule> getRules(GameModel gameModel, Element rulesElement) {
         List<RuleFactory> ruleFactories = getRuleFactories(rulesElement);
         
         List<Rule> rules = new ArrayList<Rule>();
         for (RuleFactory rf : ruleFactories) {
             rules.add(rf.create(gameModel));
         }
         
         return rules;
     }
     
     public List<RuleFactory> getRuleFactories(Element rulesElement) {
         Map<String, Element> subElements = myXMLTool.getChildrenElementMap(rulesElement);
         
         List<RuleFactory> ruleFactories = new ArrayList<RuleFactory>();
         for (Element ruleElement : subElements.values()) {
             ruleFactories.add(getRuleFactory(ruleElement));
         }
         
         return ruleFactories;
     }
 
     @SuppressWarnings("rawtypes")
     private RuleFactory getRuleFactory (Element ruleNameElement) {
         List<String> constructorParams = getRuleFactoryConstructorParameters(ruleNameElement);
         
         try {
         	System.out.println("vooga.towerdefense.factories.rulefactories." + myXMLTool.getTagName(ruleNameElement));
             Class ruleFactoryClass = Class.forName("vooga.towerdefense.factories.rulefactories." + myXMLTool.getTagName(ruleNameElement)+ "Factory");
             // There only exists one rule constructor
             Constructor c = ruleFactoryClass.getConstructors()[0];
             RuleFactory ruleFactory =
                     (RuleFactory) c.newInstance(constructorParams.toArray());
             return ruleFactory;
         }
         catch (InstantiationException e) {
        	System.out.println("1");
             return null;
         }
         catch (IllegalAccessException e) {
        	System.out.println("2");
             return null;
         }
         catch (IllegalArgumentException e) {
        	System.out.println("3");
             return null;
         }
         catch (InvocationTargetException e) {
        	System.out.println("4");
             return null;
         }
         catch (ClassNotFoundException e) {
        	System.out.println("5");
             return null;
         } 
     }
 
     private List<String> getRuleFactoryConstructorParameters (Element ruleNameElement) {
         List<Element> subElements = myXMLTool.getChildrenList(ruleNameElement);
 
         List<String> ruleFactoryConstructorParameters = new ArrayList<String>();
         for (Element subElement : subElements) {
             ruleFactoryConstructorParameters.add(getRuleFactoryConstructorParameter(subElement));
         }
 
         return ruleFactoryConstructorParameters;
     }
 
     private String getRuleFactoryConstructorParameter (Element ruleParameterElement) {
         return myXMLTool.getContent(ruleParameterElement);
     }
 }
