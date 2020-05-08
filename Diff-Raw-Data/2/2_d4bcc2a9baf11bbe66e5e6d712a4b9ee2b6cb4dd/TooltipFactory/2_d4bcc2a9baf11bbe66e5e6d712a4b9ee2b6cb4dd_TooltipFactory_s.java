 /**
  * 
  */
 package cz.cuni.mff.peckam.java.origamist.services;
 
 import java.awt.Color;
 import java.awt.event.KeyEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.Hashtable;
 import java.util.Locale;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 import javax.swing.BorderFactory;
 import javax.swing.KeyStroke;
 import javax.swing.ToolTipManager;
 import javax.swing.UIManager;
 
 import cz.cuni.mff.peckam.java.origamist.services.interfaces.ConfigurationManager;
 
 /**
  * A factory class for creating visually interesting HTML tooltips.
  * 
  * <b>Service dependencies</b>:
  * ConfigurationManager
  * 
  * @author Martin Pecka
  */
 public class TooltipFactory extends Service
 {
 
     /** The application resource bundle. */
     protected ResourceBundle            messages             = null;
 
     /** The names for the key modifiers. */
     protected Hashtable<String, String> modifierTranslations = new Hashtable<String, String>(7);
 
     /** The names for the custom keys. */
     protected Hashtable<String, String> keyTranslations      = new Hashtable<String, String>();
 
     /** The first word contained in the <code>os.name</code> system property. */
     protected final String              osName;
 
     public TooltipFactory()
     {
         String os = System.getProperty("os.name", "Windows").trim();
         int index = os.indexOf(' ');
         if (index > 0)
             os = os.substring(0, index);
         this.osName = os;
 
         PropertyChangeListener l = new PropertyChangeListener() {
             @Override
             public void propertyChange(PropertyChangeEvent evt)
             {
                 messages = ResourceBundle.getBundle("application", (Locale) evt.getNewValue());
 
                 keyTranslations.clear();
                 modifierTranslations.clear();
                 for (String mod : new String[] { "Ctrl", "Meta", "Alt", "AltGr", "Shift" }) {
                     modifierTranslations.put(mod, getKeyTranslation(mod));
                 }
             }
         };
         ServiceLocator.get(ConfigurationManager.class).get().addPropertyChangeListener("locale", l);
         l.propertyChange(new PropertyChangeEvent(this, "locale", null, ServiceLocator.get(ConfigurationManager.class)
                 .get().getLocale()));
 
         UIManager.put("ToolTip.border", BorderFactory.createLineBorder(Color.LIGHT_GRAY, 1));
         UIManager.put("ToolTip.background", new Color(231, 231, 184, 230));
         ToolTipManager.sharedInstance().setDismissDelay(30000);
     }
 
     /**
      * Return a default looking tooltip.
      * 
      * @param text The text to be displayed.
      * @return A default looking tooltip.
      */
     public String getPlain(String text)
     {
         return text;
     }
 
     /**
      * Return a default looking tooltip with the accelerator text added.
      * 
      * @param text The text to be displayed.
      * @param accelerator The accelerator to be appended to the given text.
      * @return A default looking tooltip.
      */
     public String getPlain(String text, KeyStroke accelerator)
     {
         return text
                 + (accelerator != null ? " " + messages.getString("accelerator") + ": "
                         + getPlainKeyStrokeText(accelerator) : "");
     }
 
     /**
      * Return a custom (and good-looking) tooltip.
      * 
      * @param text The text to be displayed. Shouldn't be <code>null</code>.
      * @return A custom (and good-looking) tooltip.
      */
     public String getDecorated(String text)
     {
         return getDecorated(text, null, null, null);
     }
 
     /**
      * Return a custom (and good-looking) tooltip.
      * 
      * @param text The text to be displayed. Shouldn't be <code>null</code>.
      * @param title The short title describing the tooltip. Can be <code>null</code>.
      * @param iconName The name of the icon file in <code>/resources/images/</code> to display with this tooltip. Can be
      *            <code>null</code>.
      * @param accelerator The accelerator to be displayed. Can be <code>null</code>.
      * 
      * @return A custom (and good-looking) tooltip.
      */
     public String getDecorated(String text, String title, String iconName, KeyStroke accelerator)
     {
         StringBuilder result = new StringBuilder();
         String innerText = text.trim();
         // encode HTML entities if the given text isn't HTML yet
        if (!text.substring(0, 6).toLowerCase().startsWith("<html>")) {
             innerText = innerText.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
         } else {
             innerText = innerText.substring(6).replaceAll("</html>$", "");
         }
 
         result.append("<html>");
 
         result.append("<head>");
         result.append("<style type=\"text/css\">");
 
         result.append("html {margin: 0px; padding: 0px;} ");
 
         result.append("body {background: transparent url('")
                 .append(getClass().getResource("/resources/images/tooltip-bg.png")).append("') repeat-x top left;} ");
 
         result.append("#content {background: transparent url('")
                 .append(getClass().getResource("/resources/images/lightbulb-24.png"))
                 .append("') no-repeat top right; margin: 5px 0px 10px 10px; padding: 5px 25px 0px 0px;} ");
 
         result.append("#icon {width: 24px; height: 24px; border: 0px none; margin: 3px;} ");
 
         result.append("#title {font-weight: bold; font-size: 120%; margin: 0px 4px 0px 4px;}");
 
         result.append(".accelerator {margin-left: 10px; padding: 2px; background-color: #eaeaca;}");
 
         result.append(".key {margin: 0px; padding: 1px; background-color: #888888;}");
         result.append(".key div {margin: 0px; padding: 1px; background-color: #aaaaaa;}");
         result.append(".key div div {padding: 1px 3px; background-color: #eeeeee;}");
 
         try {
             result.append(messages.getString("tooltip.additionalCSS"));
         } catch (MissingResourceException e) {}
 
         result.append("</style>");
         result.append("</head>");
 
         result.append("<body><div id=\"content\">");
 
         if (title != null || iconName != null || accelerator != null) {
             result.append("<table id=\"heading\"><tr>");
             if (iconName != null) {
                 result.append("<td><img src=\"").append(getClass().getResource("/resources/images/" + iconName))
                         .append("\" alt=\"").append((title != null ? title : "icon"))
                         .append("\" id=\"icon\" width=\"24\" height=\"24\"/></td>");
             }
             if (title != null) {
                 result.append("<td><span id=\"title\">").append(title).append("</span></td>");
             }
             if (accelerator != null) {
                 result.append("<td>").append(getDecoratedKeyStrokeText(accelerator)).append("</td>");
             }
             result.append("</tr></table>");
         }
 
         result.append("<div id=\"main\">").append(innerText).append("</div>");
 
         result.append("</div></body>");
         result.append("</html>");
 
         return result.toString();
     }
 
     /**
      * Return a string corresponding to <code>key</code> in the current locale and the given OS. If no translation is
      * found, the <code>key</code> is returned.
      * 
      * @param key The key to be translated, can be either <code>(Ctrl|Meta|Alt|AltGr|Shift)</code> or a
      *            <code>part after "VK_" in a KeyEvent constant</code>.
      * @return A string corresponding to <code>key</code> in the current locale and the given OS.
      */
     protected String getKeyTranslation(String key)
     {
         try {
             return messages.getString("key." + key + "." + osName);
         } catch (MissingResourceException e) {
             try {
                 return messages.getString("key." + key);
             } catch (MissingResourceException e2) {
                 return key;
             }
         }
     }
 
     /**
      * Return a string array of textual description of the keystroke.
      * 
      * If the length of the returned array is <code>n</code>, then items <code>0</code> to <code>n-2</code> contain the
      * modifier key descriptions, and item <code>n-1</code> contains the key description.
      * 
      * Modifier can be on of:
      * <ul>
      * <li>Ctrl</li>
      * <li>Meta</li>
      * <li>Alt</li>
      * <li>AltGr</li>
      * <li>Shift</li>
      * </ul>
      * The modifiers will always be returned in this order.
      * 
      * The key can be any part following <code>VK_</code> in {@link KeyEvent} constants.
      * 
      * @param stroke The {@link KeyStroke} to parse.
      * @return A string array of textual description of the keystroke.
      */
     protected String[] parseKeyStroke(KeyStroke stroke)
     {
         if (stroke == null)
             return new String[0];
 
         int mod = stroke.getModifiers();
         boolean isCtrl = (mod & KeyEvent.CTRL_DOWN_MASK) > 0;
         boolean isShift = (mod & KeyEvent.SHIFT_DOWN_MASK) > 0;
         boolean isMeta = (mod & KeyEvent.META_DOWN_MASK) > 0;
         boolean isAlt = (mod & KeyEvent.ALT_DOWN_MASK) > 0;
         boolean isAltGr = (mod & KeyEvent.ALT_GRAPH_DOWN_MASK) > 0;
 
         String[] result = new String[(isCtrl ? 1 : 0) + (isShift ? 1 : 0) + (isMeta ? 1 : 0) + (isAlt ? 1 : 0)
                 + (isAltGr ? 1 : 0) + 1];
 
         int i = 0;
         if (isCtrl)
             result[i++] = "Ctrl";
         if (isMeta)
             result[i++] = "Meta";
         if (isAlt)
             result[i++] = "Alt";
         if (isAltGr)
             result[i++] = "AltGr";
         if (isShift)
             result[i++] = "Shift";
 
         result[i] = KeyStroke.getKeyStroke(stroke.getKeyCode(), 0).toString().replaceAll("^[a-z]* ", "");
 
         return result;
     }
 
     /**
      * Localize the strings returned by {@link parseKeyStroke(KeyStroke)} according to the current locale and OS.
      * 
      * Eg. localizes <code>Meta</code> modifier on Windows in English to <code>Win</code>.
      * 
      * @param stroke The array of strings returned by {@link parseKeyStroke(KeyStroke)}.
      * @return The array of localized strings.
      */
     protected String[] localizeParsedKeyStroke(String[] stroke)
     {
         if (stroke == null || stroke.length == 0)
             return stroke;
 
         if (stroke.length > 1) {
             for (int i = 0; i < stroke.length - 1; i++) {
                 stroke[i] = modifierTranslations.get(stroke[i]);
             }
         }
 
         String key = stroke[stroke.length - 1];
         if (keyTranslations.get(key) == null) {
             String trans = getKeyTranslation(key);
             // we really want to compare the String addresses! If equal, it may mean the
             // getKeyTranslation() didn't find anything usable and we've still got a suffix from the
             // VK_ constant name.
             if (trans == key) {
                 trans = trans.replaceAll("_", " ").toLowerCase();
                 StringBuilder sb = new StringBuilder(trans.length());
                 for (String s : trans.split(" ")) {
                     sb.append(Character.toUpperCase(s.charAt(0)));
                     if (s.length() > 1)
                         sb.append(s.substring(1)).append(" ");
                 }
                 trans = sb.toString().trim();
             }
             keyTranslations.put(key, trans);
         }
         stroke[stroke.length - 1] = keyTranslations.get(key);
 
         return stroke;
     }
 
     /**
      * Return a plain-text representation of the given {@link KeyStroke}.
      * 
      * @param stroke The {@link KeyStroke} we want text representation of.
      * @return A plain-text representation of the given {@link KeyStroke}.
      */
     protected String getPlainKeyStrokeText(KeyStroke stroke)
     {
         StringBuffer sb = new StringBuffer();
         String[] parsed = localizeParsedKeyStroke(parseKeyStroke(stroke));
         for (int i = 0; i < parsed.length; i++) {
             sb.append(parsed[i]);
             if (i < parsed.length - 1)
                 sb.append("+");
         }
         return sb.toString();
     }
 
     /**
      * Return a HTML-decorated representation of the given {@link KeyStroke}.
      * 
      * @param stroke The {@link KeyStroke} we want text representation of.
      * @return A HTML-decorated representation of the given {@link KeyStroke}.
      */
     protected String getDecoratedKeyStrokeText(KeyStroke stroke)
     {
         StringBuffer sb = new StringBuffer("<table class=\"accelerator\"><tr>");
         String[] parsed = localizeParsedKeyStroke(parseKeyStroke(stroke));
         for (int i = 0; i < parsed.length; i++) {
             sb.append("<td class=\"key\"><div><div>").append(parsed[i]).append("</div></div></td>");
             if (i < parsed.length - 1)
                 sb.append("<td>+</td>");
         }
         return sb.append("</tr></table>").toString();
     }
 
     @Override
     protected Class<?>[] getDependecies()
     {
         return new Class<?>[] { ConfigurationManager.class };
     }
 }
