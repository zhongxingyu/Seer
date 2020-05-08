 package no.ebakke.studycaster.configuration;
 
 import java.text.MessageFormat;
 import java.util.EnumMap;
 import java.util.Map;
 import no.ebakke.studycaster.backend.StudyCasterException;
import org.apache.commons.lang3.StringEscapeUtils;
 import org.w3c.dom.Element;
 
 public class UIStrings {
   private final Map<UIStringKey,String>    strings;
   private final Map<UIStringKey,Character> mnemonics;
 
   public UIStrings(Element elm) throws StudyCasterException {
     strings   = new EnumMap<UIStringKey,String>(UIStringKey.class);
     mnemonics = new EnumMap<UIStringKey,Character>(UIStringKey.class);
     for (Element uiStringElm : ConfigurationUtil.getElements(elm, "uistring", false)) {
       String keyS = uiStringElm.getAttribute("key");
       UIStringKey key;
       try {
         key = UIStringKey.valueOf(keyS);
       } catch (IllegalArgumentException e) {
         throw new StudyCasterException("Unknown UI string key \"" + keyS + "\"");
       }
       String value = (key.isHtmlAllowed()) ? ConfigurationUtil.getSwingCaption(uiStringElm) : 
           ConfigurationUtil.getTextContent(uiStringElm);
       strings.put(key, value);
       String mnemonic = uiStringElm.getAttribute("mnemonic");
       if        ( key.usesMnemonic() && mnemonic.length() != 1) {
         throw new StudyCasterException("UI string with key " + key.name() +
             " requires a single-character mnemonic");
       } else if (!key.usesMnemonic() && mnemonic.length() >  0) {
         throw new StudyCasterException("Unnecessary mnemonic for UI string with key " + key.name());
       }
       if (key.usesMnemonic()) {
         if (mnemonic.length() != 1) {
           throw new StudyCasterException("UI string with key " + key.name() +
                       " requires a single-character mnemonic");
         }
         mnemonics.put(key, mnemonic.charAt(0));
       } else if (mnemonic.length() > 0) {
         throw new StudyCasterException("Unnecessary mnemonic for UI string with key " + key.name());
       }
 
     }
     for (UIStringKey key : UIStringKey.values()) {
       if (!strings.containsKey(key))
         throw new StudyCasterException("Missing UI string with key " + key.name());
     }
   }
 
   public String getString(UIStringKey key) {
     if (key.takesParameters())
       throw new IllegalArgumentException("UI string with key " + key + " takes parameters");
     return strings.get(key);
   }
 
   public String getString(UIStringKey key, Object parameters[]) {
     if (!key.takesParameters())
       throw new IllegalArgumentException("UI string with key " + key + " does not take parameters");
    final Object escapedParameters[];
    final String msg = strings.get(key);
    if (msg.startsWith("<html>")) {
      escapedParameters = new Object[parameters.length];
      for (int i = 0; i < parameters.length; i++) {
        escapedParameters[i] = StringEscapeUtils.escapeHtml3(
            parameters[i] == null ? "null" : parameters[i].toString());
      }
    } else {
      escapedParameters = parameters;
    }
    return new MessageFormat(msg).format(escapedParameters);
   }
 
   public char getMnemonic(UIStringKey key) {
     if (!key.usesMnemonic())
       throw new IllegalArgumentException("UI string with key " + key + "does not use a mnemonic");
     return mnemonics.get(key);
   }
 }
