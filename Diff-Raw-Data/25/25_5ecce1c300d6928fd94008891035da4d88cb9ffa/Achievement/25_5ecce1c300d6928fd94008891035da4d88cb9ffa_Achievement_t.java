 /*
  *  Wezzle
  *  Copyright (c) 2007-2008 Couchware Inc.  All rights reserved.
  */
 
 package ca.couchware.wezzle2d.manager;
 
 import ca.couchware.wezzle2d.Game;
 import ca.couchware.wezzle2d.ManagerHub;
 import ca.couchware.wezzle2d.Rule;
 import ca.couchware.wezzle2d.manager.Settings.Key;
 import ca.couchware.wezzle2d.tile.Tile;
 import ca.couchware.wezzle2d.tile.TileHelper;
 import ca.couchware.wezzle2d.tile.TileType;
 import ca.couchware.wezzle2d.util.IXMLizable;
 import ca.couchware.wezzle2d.util.Node;
 import ca.couchware.wezzle2d.util.StringUtil;
 import ca.couchware.wezzle2d.util.SuperCalendar;
 import java.awt.Color;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.EnumMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import org.jdom.Element;
 
 /**
  * An achievement will hold all the state information required for that 
  * achievement to be successfully completed. Each field will also hold whether
  * it is greater than, less than, or equal to the required value.
  * 
  * for example, an achievement can hold a value such as:  level &lt; 10, 
  * lines = 100, moves &lt; 15.
  *
  * achievemtns will be held within the achievement manager and take a game state
  * as a parameter to their update function. Every iteration of the game loop
  * will have the achievement compare its internal requirements to the game state
  * and evaluate whether or not it has been achieved. If it has been achieved,
  * the evaluate(gamestate) function will return true.
  * 
  * The achievement manager will simply iterate through the list of achievements
  * passing in the current game state and waiting for a response.
  * 
  * If the achievement has been successfully completed, it will be removed from
  * the achievement list and added to the completed list. This will dynamically
  * shrink the amount of searching necessary to check the achievements.
  * 
  * @author Kevin
  */
 public class Achievement implements IXMLizable
 {
    
     /** The levels of achievement difficulty. */
     public static enum Difficulty
     {
         BRONZE, 
         SILVER, 
         GOLD, 
         PLATINUM;
         
         /** The color map for the difficulties. */
         final private static Map<Difficulty, Color> difficultyColorMap
                 = new EnumMap<Difficulty, Color>(Difficulty.class);
 
         /**
          * Initializes the difficulty colour map.  Can only be called once
          * or it will raise an exception.
          * 
          * @param settingsMan
          */
         final public static void initializeDifficultyColorMap(SettingsManager settingsMan)
         {
             if (!difficultyColorMap.isEmpty())
             {
                 throw new IllegalStateException("Color map already created!");
             }
             
             Map<Difficulty, Color> map = difficultyColorMap;
             map.put(Difficulty.BRONZE,   settingsMan.getColor(Key.ACHIEVEMENT_COLOR_BRONZE));
             map.put(Difficulty.SILVER,   settingsMan.getColor(Key.ACHIEVEMENT_COLOR_SILVER));
             map.put(Difficulty.GOLD,     settingsMan.getColor(Key.ACHIEVEMENT_COLOR_GOLD));
             map.put(Difficulty.PLATINUM, settingsMan.getColor(Key.ACHIEVEMENT_COLOR_PLATINUM));             
         }
         
         /**
          * Returns the colour associated with the difficulty level.
          * 
          * @return
          */
         public Color getColor()
         {
             if (difficultyColorMap.isEmpty())
             {
                 throw new IllegalStateException("Color map has not been created!");
             }
             
             return difficultyColorMap.get(this);
         }
     }
 
     /** The rule list for this achievement. */
     private final List<Rule> ruleList;
 
     /** The title of this achievement. */
     private final String title;
 
     /** The formatted description for this acheivement. */
     private final String formattedDescription;
 
     /** The unformatted description. */
     private final String description;
 
     /** The difficulty level of the achievment. */
     private final Difficulty difficulty;
 
     /** The dete the achievement was completed, if any. */
     private SuperCalendar dateCompleted = null;    
 
     /**
      * The achievement is a list of rules which all have to be true for an
      * achievement to be achieved. As well as an associated text description 
      * and a difficulty type.
      * 
      * @param rules
      * @param description
      * @param difficulty
      */
     private Achievement(List<Rule> ruleList, 
             String title,
             String formattedDescription,
             String description, 
             Difficulty difficulty, 
             SuperCalendar dateCompleted)
     {
         this.ruleList             = ruleList;
         this.title                = title;
         this.formattedDescription = formattedDescription;
         this.description          = description;
         this.difficulty           = difficulty;
         this.dateCompleted        = dateCompleted;
     }
         
     public static Achievement newInstance(List<Rule> ruleList, 
             String title,
             String description, 
             Difficulty difficulty, 
             SuperCalendar dateCompleted)
     {
        return new Achievement(ruleList, 
                title, 
                description, 
                description, 
                difficulty, 
                dateCompleted);
     }
     
     public static Achievement newInstance(Achievement achievement, SuperCalendar dateCompleted)
     {
         return new Achievement(
                 achievement.ruleList, 
                 achievement.title,
                 achievement.formattedDescription,
                 achievement.description,
                 achievement.difficulty,
                 dateCompleted);
     }
 
     @SuppressWarnings("unchecked") 
     public static Achievement newInstanceFromXml(Element element)
     {
         // Get the name.
         String name = element.getAttributeValue("name");
                 
         // Get the description.
         Element descriptionElement = element.getChild("description");
         String formattedDescription = descriptionElement == null ? "" : descriptionElement.getText();         
         String description = descriptionElement.getTextTrim().replaceAll("\n +", "\n");
         
         // Get the difficulty.
         Difficulty difficulty = Difficulty.valueOf(element.getAttributeValue("difficulty"));        
         
         // Get the date.
         Element dateElement = element.getChild("date");
         SuperCalendar dateCompleted = dateElement != null
                 ? SuperCalendar.newInstanceFromXml(dateElement)
                 : null;                
         
         // Get all the rules.        
         List<Rule> rules = new ArrayList<Rule>();
         
         for ( Object o : element.getChildren("rule") )
         {
             Element rule = (Element) o;
             Rule.Type type = Rule.Type.valueOf(rule.getAttributeValue("type").toString());
 
             switch (type)
             {
                 case META:
                 
                     rules.add(createMetaRule(rule));
                     break;
 
                 case COLLISION:
                 
                     rules.add(createCollisionRule(rule));
                     break;
 
                 case RATE:
 
                     rules.add(createRateRule(rule));
                     break;
                 
                 default:
                     
                     rules.add(createSimpleRule(type, rule));
              
             } // end witch
         } // end while
         
         // Get the collisions.
         return new Achievement(rules, name, formattedDescription, description, difficulty, dateCompleted);
     }
 
     private static Rule createMetaRule(Element rule)
     {
         Element amount = rule.getChild("amount");
         Rule.Status metaType = Rule.Status.valueOf(amount.getAttributeValue("metatype").toString());
         int value = Integer.parseInt(amount.getAttributeValue("value").toString());
         Rule.Operation operation = Rule.Operation.valueOf(amount.getAttributeValue("operation"));
 
         Element achieve = rule.getChild("achievement");
         List<String> achievementNamesList = new ArrayList<String>();
 
         // If there are no specified achievement names...
         if (achieve == null)
         {
             return new Rule(Rule.Type.META, operation, value);
         }
         
         while (achieve != null)
         {
             achievementNamesList.add(achieve.getAttributeValue("name").toString());
             rule.removeChild("achievement");
             achieve = rule.getChild("achievement");
         }
 
         return new Rule(Rule.Type.META, operation, value, achievementNamesList, metaType);
     }
 
     @SuppressWarnings("unchecked")
     private static Rule createCollisionRule(Element rule)
     {
         // Get the operation.
         Rule.Operation operation = Rule.Operation.valueOf(rule.getAttributeValue("operation").toString());
 
         // Get the collisions.
         Node<Set<TileType>> tileTree = new Node<Set<TileType>>(null);
         List<Element> elementList = (List<Element>) rule.getChildren("item");
         Node<Set<TileType>> currentNode = tileTree;
         convertElementToInternalTree(currentNode, elementList);
 
         // Add the rule and continue to get the next rule.
         return new Rule(Rule.Type.COLLISION, operation, tileTree);
     }
 
 
     @SuppressWarnings("unchecked")
     private static Rule createRateRule(Element rule)
     {
         // The list of numerators (of which there may be many).
         List<Numerator> numeratorList = new ArrayList<Numerator>();
 
         // Get the numerator type.        
         for ( Object o : rule.getChildren("numerator") )
         {
             // The numerator element.
             Element numerator = (Element) o;
 
             Integer value = null;
             Rule.Operation operation = null;
             Rule.NumeratorType numeratorType = Rule.NumeratorType
                     .valueOf(numerator.getAttributeValue("type").toString());
 
             // Set the operation and value here if this is not a collision.
             // If it is a collision, the operation is set below.
             if (!numeratorType.equals(Rule.NumeratorType.COLLISION))
             {
                 operation = Rule.Operation.valueOf(numerator.getAttributeValue("operation"));
                 value = Integer.parseInt(numerator.getAttributeValue("value").toString());                
             }
 
             switch (numeratorType)
             {
                 case COLLISION:
 
                     for ( Object p : numerator.getChildren("item") )
                     {
                         Element item = (Element) p;
 
                        String modifiedType = item.getAttributeValue("type");
                        if ("*ITEM".equals(modifiedType))
                            modifiedType = "ITEMS";
                        else if ("*MULTIPLIER".equals(modifiedType))
                            modifiedType = "MULTIPLIERS";
                        
                         Rule.NumeratorSubType type = Rule.NumeratorSubType
                                .valueOf(modifiedType);
 
                         operation = Rule.Operation
                                 .valueOf(item.getAttributeValue("operation"));
 
                         value = Integer.parseInt(item
                                 .getAttributeValue("value").toString());
 
                         numeratorList.add(new Numerator(value, operation, numeratorType, type));                       
                     }
                     break;
                 
                 case LINES:
                 
                     numeratorList.add(new Numerator(value, operation, numeratorType,
                             Rule.NumeratorSubType.LINES));
                     break;
                 
                 case SCORE:
                 
                     numeratorList.add(new Numerator(value, operation, numeratorType,
                             Rule.NumeratorSubType.SCORE));
                     break;
                 
                 case ITEMS:
                 
                     numeratorList.add(new Numerator(value, operation, numeratorType,
                             Rule.NumeratorSubType.ITEMS));
                     break;
                 
                  case MULTIPLIERS:
                 
                     numeratorList.add(new Numerator(value, operation, numeratorType,
                             Rule.NumeratorSubType.MULTIPLIERS));
                     break;
                 
                 default: throw new IllegalArgumentException("Unrecognized numerator type.");
             }           
         }
 
         // Only one denominator for now.
         Element denominator = rule.getChild("denominator");
 
         Rule.DenominatorType denominatorType = Rule.DenominatorType
                 .valueOf(denominator.getAttributeValue("type").toString());
 
         Integer denominatorValue = Integer.parseInt(denominator
                 .getAttributeValue("value").toString());
 
         Rule.Operation denominatorOp = Rule.Operation.valueOf(denominator
                 .getAttributeValue("operation"));
 
         // Add the rule and continue to get the next rule.
         return new Rule(Rule.Type.RATE, numeratorList,
                 denominatorType, denominatorOp, denominatorValue);
     }
 
     private static Rule createSimpleRule(Rule.Type type, Element rule)
     {
         // Get the operation.
         Rule.Operation operation = Rule.Operation.valueOf(rule.getAttributeValue("operation").toString());
         int value = Integer.parseInt(rule.getAttributeValue("value").toString());
         return new Rule(type, operation, value);
     }
 
     /**
      * Transfer an XML element (and all it's children) to an internal tree node.
      * @param parentNode
      * @param elementList
      */
     @SuppressWarnings("unchecked") 
     private static void convertElementToInternalTree(
             Node<Set<TileType>> parentNode,
             List<Element> elementList)
     {
         for ( Element e : elementList )
         {
             String[] typeStrList = e.getAttributeValue("type").split(",");
             Set<TileType> typeSet = new HashSet<TileType>();
             for ( String typeStr : typeStrList )
             {
                 // Check for a pseudo-type.
                 if (typeStr.startsWith("*"))
                 {
                     if (typeStr.equals("*ITEM"))
                     {
                         typeSet.addAll(TileHelper.getItemTileTypeSet());
                     }
                     else if (typeStr.equals("*MULTIPLIER"))
                     {
                         typeSet.addAll(TileHelper.getMultiplierTileTypeSet());
                     }
                     else
                     {
                         throw new IllegalArgumentException("Unknown pseudo-tile: " + typeStr);
                     }
                 }
                 else
                 {
                     typeSet.add(TileType.valueOf(typeStr));
                 }
             }
             
             Node<Set<TileType>> node = parentNode.addChild(typeSet);
             convertElementToInternalTree(node, (List<Element>) e.getChildren("item"));
         }
     }
 
     private static void convertInternalTreeToElement(
             Element element,
             Node<Set<TileType>> parentNode)
     {
         for ( Node<Set<TileType>> child : parentNode.getChildren() )
         {
             Element e = new Element("item");
 
             if (child.getData().equals(TileHelper.getItemTileTypeSet()))
             {
                 e.setAttribute("type", "*ITEM");
             }
             else if (child.getData().equals(TileHelper.getMultiplierTileTypeSet()))
             {
                 e.setAttribute("type", "*MULTIPLIER");
             }
             else
             {
                 e.setAttribute("type", StringUtil.join(child.getData(), ","));
             }
             
             convertInternalTreeToElement(e, child);
             element.addContent(e);
         }
     }
     
     /**
      * A method to evaluate an achievement to check if it has been completed.
      * 
      * @param game The state of the game.
      * @return Whether or not the achievement has been completed.
      */
     public boolean evaluate(Game game, ManagerHub hub)
     {
         // Use the private helper method to test if all of the fields
         // meet the requirements. any null values are automatically
         // accepted.
 
      
         
         for (Rule rule : ruleList)
         {
            if (!rule.evaluate(game, hub))
                return false;
         }
        
         return true;       
     }   
     
     public boolean evaluateCollision(Node<Tile> tileTree)
     {
         // Use the private helper method to test if all of the fields
         // meet the requirements. any null values are automatically
         // accepted.
         
         for (Rule rule : ruleList)
         {
            if (!rule.evaluateCollision(tileTree))
                return false;
         }
        
         return true;       
     }
 
     public boolean evaluateMeta(AchievementManager achievementMan)
     {
         // Use the private helper method to test if all of the fields
         // meet the requirements. any null values are automatically
         // accepted.
 
         for (Rule rule : ruleList)
         {
            if (!rule.evaluateMeta(achievementMan))
                return false;
         }
 
         return true;
     }
 
     public Difficulty getDifficulty()
     {
         return difficulty;
     }   
     
     public String getTitle()
     {
         return title;
     }    
     
     /**
      * Get the date completed.
      * 
      * @return the date.
      */
     public Calendar getDateCompleted()
     {
         return dateCompleted;
     }
     
     /**
      * Get the description of the achievement.
      * 
      * @return The description.
      */
     public String getDescription()
     {
         return this.description;
     }
     
     @Override
     public String toString()
     {
         return "[" + this.title + " - " + this.difficulty + "] " + this.description;
     }    
     
     public Element toXmlElement()
     {
         Element element = new Element("achievement");
         element.setAttribute("name",  this.title);        
         element.setAttribute("difficulty", String.valueOf(this.difficulty));
        
         Element descriptionElement = new Element("description");
         descriptionElement.setText(this.formattedDescription);
         element.addContent(descriptionElement);
         
         // Date.
         if (dateCompleted != null)
         {                       
             element.addContent(dateCompleted.toXmlElement());
         }
         
         for ( Rule rule : this.ruleList )
         {            
             switch (rule.getType())
             {
                 case META:
                     element.addContent(createMetaXmlRule(rule));
                     break;
 
                 case COLLISION:
                     element.addContent(createCollisionXmlRule(rule));
                     break;
 
                 case RATE:
                     element.addContent(createRateXmlRule(rule));
                     break;
 
                 default:
                     element.addContent(createSimpleXmlRule(rule));
             }
         }
         
         return element;
     }
 
     private static Element createMetaXmlRule(Rule rule)
     {
         Element element = new Element("rule");
 
         // Set type.
         element.setAttribute("type", rule.getType().toString());
 
         // Set amount.
         Element amount = new Element("amount");
         amount.setAttribute("metatype", rule.getStatus().toString());        
         amount.setAttribute("operation", String.valueOf(rule.getOperation()));
         amount.setAttribute("value", String.valueOf(rule.getValue()));
         element.addContent(amount);
 
         List<String> achievementNamesList = rule.getAchievementNameList();
         if ( achievementNamesList != null )
         {
             for ( String achStr : achievementNamesList )
             {
                 Element achElement = new Element("achievement");
                 achElement.setAttribute("name", achStr);
                 element.addContent(achElement);
             }
         }
 
         return element;
     }
 
     /**
      * Create an xml representation of a Rate rule.
      *
      * @param rule
      * @return
      */
     private static Element createRateXmlRule(Rule rule)
     {
         Element element = new Element("rule");
         element.setAttribute("type", rule.getType().toString());
 
         List<Numerator> numerators = rule.getNumeratorList();
 
         // Do everything but the collisions.
         for(Iterator<Numerator> it = numerators.iterator(); it.hasNext();)
         {
             Numerator n = it.next();
            
             if(n.type.equals(Rule.NumeratorType.COLLISION))
                continue;
 
            Element numerator = new Element("numerator");
            numerator.setAttribute("type", n.type.toString());
            numerator.setAttribute("operation", n.operation.toString());
            numerator.setAttribute("value", String.valueOf(n.value));
 


            element.addContent(numerator);
 
            it.remove();
         }
 
         // We should only have the collisions left.
         if(numerators.size() > 0)
         {
             Element numerator = new Element("numerator");
             numerator.setAttribute("type", numerators.get(0).type.toString());
 
             for(Numerator n : numerators)
             {
                 Element item = new Element("item");
                 item.setAttribute("type", n.subType.toString());
                 item.setAttribute("operation", n.operation.toString());
                 item.setAttribute("value", String.valueOf(n.value));
 
                 numerator.addContent(item);
             }
 
             element.addContent(numerator);
         }
 
         Element denominator = new Element("denominator");
         denominator.setAttribute("type", rule.getDenominatorType().toString());
         denominator.setAttribute("operation", rule.getOperation().toString());
         denominator.setAttribute("value", String.valueOf(rule.getValue()));
 
         element.addContent(denominator);
 
         // We have a collision, build it up.
         return element;
 
     }
 
     private static Element createCollisionXmlRule(Rule rule)
     {
         Element element = new Element("rule");
 
         // Set type and operation.
         element.setAttribute("type", rule.getType().toString());
         element.setAttribute("operation", rule.getOperation().toString());
 
         // Create the collision rule.
         convertInternalTreeToElement(element, rule.getTileTree());
 
         return element;
     }
 
     private static Element createSimpleXmlRule(Rule rule)
     {
         Element element = new Element("rule");
 
         // Set type, operation and value
         element.setAttribute("type", rule.getType().toString());
         element.setAttribute("operation", rule.getOperation().toString());
         element.setAttribute("value", String.valueOf(rule.getValue()));
 
         return element;
     }
 
     /**
      * Read this carefully, as the Acheivement equivalence is probably different
      * to what you intuitively would think.
      * 
      * An Achievement is equal to another Achievement if they both have the same
      * title and description.  That's it.  If you take two achievements, and
      * one of them has a completed date, and the other one doesn't, they are
      * still considered to be the same achievement.
      * 
      * Basically, Achievement equivalence ignores whether or not the achievement
      * has been completed.
      * 
      * @param o
      * @return
      */
     @Override
     public boolean equals(Object o)
     {
         if (o == this)
         {
             return true;            
         }
         if (!(o instanceof Achievement))
         {
             return false;
         }
         
         Achievement achievement = (Achievement) o;
         return this.title.equals(achievement.title)
                 && this.description.equals(achievement.description);               
     }
     
     /**
      * Must override hashcode if you override <pre>equals()</pre>.
      * 
      * The method used here is taking from Effective Java (2nd Ed.) pp. 46-48.
      * 
      * @return
      */
     @Override
     public int hashCode()
     {
         int result = 17;
         result = 31 * result + title.hashCode();
         result = 31 * result + description.hashCode();        
         return result;
     }
 
     public static final class Numerator
     {
 
         public final int value;
         public final Rule.Operation operation;
         public final Rule.NumeratorType type;
         public final Rule.NumeratorSubType subType;
 
         public Numerator(
                 int value,
                 Rule.Operation operation,
                 Rule.NumeratorType type,
                 Rule.NumeratorSubType subType)
         {
             this.value = value;
             this.type = type;
             this.operation = operation;
             this.subType = subType;
         }
 
     }
     
 }
