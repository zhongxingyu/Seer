 package net.doepner.ui.i18n;
 
 import net.doepner.i18n.L10nMapper;
 import net.doepner.ui.action.ActionId;
 
 import static net.doepner.lang.Language.DEUTSCH;
 import static net.doepner.lang.Language.ENGLISH;
 
 /**
  * Localized action descriptions
  */
 public class ActionDescriptions extends L10nMapper<ActionId> {
 
     {
         put(DEUTSCH, ActionId.SWITCH_BUFFER, "Textspeicher wechseln");
         put(DEUTSCH, ActionId.SWITCH_LANGUAGE, "Sprache wechseln");
         put(DEUTSCH, ActionId.SMALLER_FONT, "Schrift verkleinern");
         put(DEUTSCH, ActionId.BIGGER_FONT, "Schrift vergrößern");
         put(DEUTSCH, ActionId.SPEAK_WORD, "Wort vorlesen");
 
        put(ENGLISH, ActionId.SWITCH_BUFFER, "Switch buffer");
         put(ENGLISH, ActionId.SWITCH_LANGUAGE, "Switch language");
         put(ENGLISH, ActionId.SMALLER_FONT, "Decrease font size");
         put(ENGLISH, ActionId.BIGGER_FONT, "Increase font size");
         put(ENGLISH, ActionId.SPEAK_WORD, "Speak word");
     }
 }
