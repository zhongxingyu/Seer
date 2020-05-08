 package org.toj.dnd.irctoolkit.game.battle;
 
 import java.io.IOException;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 import org.toj.dnd.irctoolkit.engine.ToolkitEngine;
 import org.toj.dnd.irctoolkit.game.PC;
 import org.toj.dnd.irctoolkit.game.encounter.Encounter;
 import org.toj.dnd.irctoolkit.game.encounter.NPC;
 import org.toj.dnd.irctoolkit.io.file.GameStore;
 import org.toj.dnd.irctoolkit.util.AbbreviationUtil;
 import org.toj.dnd.irctoolkit.util.XmlUtil;
 
 public class Battle implements Cloneable {
 
     private LinkedList<Combatant> combatants;
     private Combatant current;
     private int round = -1;
     private Encounter encounter;
     private Map<String, NPC> npcs;
 
     public Battle() {
         this.combatants = new LinkedList<Combatant>();
         this.npcs = new HashMap<String, NPC>();
     }
 
     @SuppressWarnings("unchecked")
     public Battle(Element e, Map<String, PC> pcs) {
         this();
         this.round = Integer.valueOf(e.elementTextTrim("round"));
         if (e.element("encounterSettings") != null) {
             try {
                 this.encounter = GameStore.loadEncounter(e
                         .elementTextTrim("encounterSettings"));
             } catch (IOException e1) {
                 ToolkitEngine.getEngine().fireErrorMsgWindow(
                         "ȡϢʧܡȷ"
                                 + e.elementTextTrim("encounterSettings")
                                 + "Ƿڡ");
             }
         }
 
         if (e.element("combatants") != null) {
             Iterator<Element> i = e.element("combatants").elementIterator();
             while (i.hasNext()) {
                 Element combatant = i.next();
                 if (combatant.element("isPC") != null
                         && combatant.elementTextTrim("isPC").equals("true")) {
                     combatants.add(pcs.get(combatant.elementTextTrim("name")));
                 } else if (combatant.element("isNPC") != null
                         && combatant.elementTextTrim("isNPC").equals("true")) {
                     NPC npc = new NPC(combatant, encounter);
                     this.npcs.put(npc.getName(), npc);
                     combatants.add(npc);
                 } else {
                     combatants.add(new Combatant(combatant));
                 }
             }
 
             if (e.element("current") != null) {
                 this.current = combatants.get(Integer.parseInt(e
                         .elementTextTrim("current")));
             }
         }
     }
 
     public Battle(Encounter encounter) {
         this();
         this.encounter = encounter;
         for (String name : encounter.npcs.keySet()) {
             this.npcs.put(name, new NPC(name, encounter.npcs.get(name)));
         }
     }
 
     public Element toXmlElement() {
         Element e = DocumentHelper.createElement("battle");
         e.add(XmlUtil.textElement("round", String.valueOf(round)));
         if (current != null) {
             e.add(XmlUtil.textElement("current",
                     String.valueOf(combatants.indexOf(current))));
         }
         if (this.combatants != null && !this.combatants.isEmpty()) {
             Element combatants = e.addElement("combatants");
             for (Combatant ch : this.combatants) {
                 if (ch instanceof PC) {
                     Element ec = combatants.addElement("combatant");
                     ec.add(XmlUtil.textElement("isPC", "true"));
                     ec.add(XmlUtil.textElement("name", ch.getName()));
                 } else {
                     combatants.add(ch.toXmlElement());
                 }
             }
         }
         if (this.encounter != null) {
             e.add(XmlUtil.textElement("elementSettings", encounter.name));
         }
         if (this.combatants != null && !this.combatants.isEmpty()) {
             Element combatants = e.addElement("combatants");
             for (Combatant ch : this.combatants) {
                 if (ch instanceof PC) {
                     Element ec = combatants.addElement("combatant");
                     ec.add(XmlUtil.textElement("isPC", "true"));
                     ec.add(XmlUtil.textElement("name", ch.getName()));
                 } else {
                     combatants.add(ch.toXmlElement());
                 }
             }
         }
         return e;
     }
 
     public void addChar(String charName) {
         double init = 0;
         if (!combatants.isEmpty()) {
             init = combatants.getLast().getInit() - 1;
         }
         combatants.add(new Combatant(charName, init));
     }
 
     public void addCharByInit(String charName, double init) {
         Combatant ch = this.findCharByNameOrAbbre(charName);
         if (ch == null) {
             combatants.add(new Combatant(charName, init));
         } else {
             ch.setInit(init);
         }
         restoreOrder();
     }
 
     public void addChar(PC pc) {
         double init = 0;
         if (!combatants.isEmpty()) {
             init = combatants.getLast().getInit() - 1;
         }
         pc.setInit(init);
 
         Combatant ch = this.findCharByNameOrAbbre(pc.getName());
         if (ch == null) {
             combatants.add(pc);
         }
     }
 
     public void addCharByInit(PC pc, double init) {
         pc.setInit(init);
         Combatant ch = this.findCharByNameOrAbbre(pc.getName());
         if (ch == null) {
             combatants.add(pc);
         }
         restoreOrder();
     }
 
     public void startRound(int n) {
         round = n;
         if (round < 0) {
             current = null;
         } else {
             current = combatants.getFirst();
         }
     }
 
     public void go(String charName) {
         this.go(charName, round);
     }
 
     public void go(String charName, int round) {
         this.round = round;
         if (round < 0) {
             current = null;
         } else {
             current = findCharByNameOrAbbre(charName);
         }
     }
 
     public void pre() {
         if (current == null) {
             return;
         }
         if (current.equals(combatants.getFirst())) {
             current = combatants.getLast();
             round--;
             if (round < 0) {
                 current = null;
             }
         } else {
             current = combatants.get(combatants.indexOf(current) - 1);
         }
     }
 
     public void end() {
         if (current.equals(combatants.getLast())) {
             current = combatants.getFirst();
             round++;
         } else {
             current = combatants.get(combatants.indexOf(current) + 1);
         }
     }
 
     public void putCharBefore(String charName, String before) {
         int index = findIndexByNameOrAbbr(before);
         Combatant ch = findCharByNameOrAbbre(charName);
         if (ch == null || index == -1) {
             return;
         }
 
         if (findCharByNameOrAbbre(charName).equals(current)) {
             end();
         }
 
         if (index == 0) {
             ch.setInit(combatants.get(index).getInit() + 1);
         } else {
             ch.setInit((combatants.get(index).getInit() + combatants.get(
                     index - 1).getInit()) / 2);
         }
         restoreOrder();
     }
 
     public void putCharAfter(String charName, String after) {
         int index = findIndexByNameOrAbbr(after);
         Combatant ch = findCharByNameOrAbbre(charName);
         if (ch == null || index == -1) {
             return;
         }
 
         if (findCharByNameOrAbbre(charName).equals(current)) {
             end();
         }
 
         if (combatants.getLast().equals(combatants.get(index))) {
             ch.setInit(combatants.get(index).getInit() - 1);
         } else {
             ch.setInit((combatants.get(index).getInit() + combatants.get(
                     index + 1).getInit()) / 2);
         }
         restoreOrder();
     }
 
     public void removeChar(String charName) {
         if (findCharByNameOrAbbre(charName).equals(current)) {
             end();
         }
         combatants.remove(findCharByNameOrAbbre(charName));
     }
 
     public void renameChar(String charName, String newName) {
         Combatant ch = findCharByNameOrAbbre(charName);
         if (ch != null) {
             ch.rename(newName);
         }
     }
 
     public void heal(String charName, int healValue) {
         Combatant ch = findCharByNameOrAbbre(charName);
         if (ch != null) {
             ch.heal(healValue);
         }
     }
 
     public void damage(String charName, int dmg) {
         Combatant ch = findCharByNameOrAbbre(charName);
         if (ch != null) {
             ch.damage(dmg);
         }
     }
 
     public void addThp(String charName, int thp) {
         Combatant ch = findCharByNameOrAbbre(charName);
         if (ch != null) {
             ch.setThp(thp);
         }
     }
 
     public void applyState(String charName, String stateStr) {
         Combatant ch = findCharByNameOrAbbre(charName);
         if (ch != null) {
             State state = State.parseState(stateStr, round, current.getInit());
             ch.applyState(state);
         }
     }
 
     public void removeState(String charName, String stateStr) {
         Combatant ch = findCharByNameOrAbbre(charName);
         if (ch != null) {
             State state = State.parseState(stateStr, -1, -1);
             ch.removeState(ch.matchState(state));
         }
     }
 
     public Combatant getCurrent() {
         return current;
     }
 
     public void clear() {
         this.combatants = new LinkedList<Combatant>();
         this.current = null;
         this.round = -1;
     }
 
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder();
         if (round == 0) {
             sb.append("surprise: ");
         } else if (round > 0) {
             sb.append("r").append(round).append(": ");
         } else {
             sb.append("setup: ");
         }
         for (Combatant ch : combatants) {
             if (!ch.equals(combatants.getFirst())) {
                 sb.append(", ");
             }
             if (ch.equals(current)) {
                 sb.append(ch.toOnTurnString());
             } else {
                 sb.append(ch);
             }
         }
         return sb.toString();
         // return chars.toString().substring(1, chars.toString().length() - 1);
     }
 
     public void restoreOrder() {
         Collections.sort(combatants, new Comparator<Combatant>() {

             @Override
             public int compare(Combatant o1, Combatant o2) {
                 return (int) Math.signum(o2.getInit() - o1.getInit());
             }
         });
     }
 
     public int findIndexByNameOrAbbr(String charName) {
         int i = findIndexByName(charName);
         if (i != -1) {
             return i;
         } else {
             return findIndexByAbbr(charName);
         }
     }
 
     public int findIndexByAbbr(String charName) {
         int i = 0;
         for (Combatant c : combatants) {
             if (AbbreviationUtil.isPrefixAbbre(charName, c.getName())) {
                 return i;
             }
             i++;
         }
         return -1;
     }
 
     public int findIndexByName(String charName) {
         int i = 0;
         for (Combatant c : combatants) {
             if (c.getName().equalsIgnoreCase(charName)) {
                 return i;
             }
             i++;
         }
         return -1;
     }
 
     public Combatant findCharByNameOrAbbre(String charName) {
         Combatant c = findCharByName(charName);
         if (c != null) {
             return c;
         } else {
             return findCharByAbbr(charName);
         }
     }
 
     public Combatant findCharByName(String charName) {
         for (Combatant c : combatants) {
             if (c.getName().equalsIgnoreCase(charName)) {
                 return c;
             }
         }
         return null;
     }
 
     public Combatant findCharByAbbr(String charName) {
         for (Combatant c : combatants) {
             if (AbbreviationUtil.isPrefixAbbre(charName, c.getName())) {
                 return c;
             }
         }
         return null;
     }
 
     public List<String> onTurnStart() {
         return checkStatesOnCondition(true);
     }
 
     public List<String> onTurnEnd() {
         return checkStatesOnCondition(false);
     }
 
     private List<String> checkStatesOnCondition(boolean isTurnStart) {
         List<String> result = new LinkedList<String>();
         for (Combatant ch : combatants) {
             List<String> stateMsgsFromChar = ch.checkStateBehavior(this.round,
                     this.current, isTurnStart);
             if (stateMsgsFromChar != null && !stateMsgsFromChar.isEmpty()) {
                 result.addAll(stateMsgsFromChar);
             }
         }
         return result;
     }
 
     public Battle clone() {
         Battle clone = null;
         try {
             clone = (Battle) super.clone();
             if (current != null) {
                 clone.current = (Combatant) this.current.clone();
             }
             clone.combatants = new LinkedList<Combatant>();
             for (Combatant ch : combatants) {
                 if (ch != null) {
                     clone.combatants.add(ch.clone());
                 } else {
                     clone.combatants.add(null);
                 }
             }
         } catch (CloneNotSupportedException e) {
             e.printStackTrace();
         }
         return clone;
     }
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result
                 + ((combatants == null) ? 0 : combatants.hashCode());
         result = prime * result + ((current == null) ? 0 : current.hashCode());
         result = prime * result + round;
         return result;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (this == obj)
             return true;
         if (obj == null)
             return false;
         if (getClass() != obj.getClass())
             return false;
         Battle other = (Battle) obj;
         if (combatants == null) {
             if (other.combatants != null)
                 return false;
         } else if (!combatants.equals(other.combatants))
             return false;
         if (current == null) {
             if (other.current != null)
                 return false;
         } else if (!current.equals(other.current))
             return false;
         if (round != other.round)
             return false;
         return true;
     }
 
     public LinkedList<Combatant> getChars() {
         return combatants;
     }
 
     public int getRound() {
         return round;
     }
 
     public Map<String, NPC> getNpcs() {
         return npcs;
     }
 }
