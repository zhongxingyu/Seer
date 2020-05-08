 package net.bodz.bas.i18n.dom;
 
 import java.util.Map.Entry;
 
 import net.bodz.bas.c.string.StringHtml;
 import net.bodz.bas.c.string.StringPart;
 import net.bodz.bas.i18n.LocaleColo;
 
 public class XDomainString
         extends XDomainNode<XDomainString, String>
         implements DomainString, Cloneable {
 
     public XDomainString() {
         super(null, null);
     }
 
     public XDomainString(String value) {
         super(null, value);
     }
 
     public XDomainString(String domain, String value) {
         super(domain, value);
     }
 
     public XDomainString(String domain, String value, XDomainString... follows) {
         super(domain, value, follows);
     }
 
     protected XDomainString(XDomainString other) {
         super(other);
     }
 
     public static XDomainString of(String plainString) {
         return new XDomainString(plainString);
     }
 
     @Override
     protected XDomainString createNode(String domain, String value) {
         return new XDomainString(domain, value);
     }
 
     @Override
     public XDomainString clone() {
         return new XDomainString(this);
     }
 
     /**
      * Get locale string.
      */
     @Override
     public String toString() {
         String path = LocaleColo.getInstance().getPath();
        String leaf = get(path);
         return leaf;
     }
 
     @Override
     public String toPlainText() {
         String str = toString();
         return StringHtml.toPlain(str);
     }
 
     /**
      * @see ParaLangUtil#parseParaLang(DomainString, String)
      */
     public static XDomainString parseParaLang(String plText) {
         XDomainString ds = new XDomainString();
         ParaLangUtil.parseParaLang(ds, plText);
         return ds;
     }
 
     @Override
     public String toParaLangString() {
         return toParaLangString("\n");
     }
 
     @Override
     public String toParaLangString(String separator) {
         return ParaLangUtil.formatParaLangString(this, separator);
     }
 
     /**
      * A multi-lang string is formatted as:
      * 
      * <pre>
      * "default-locale"
      * LOCALE1 "string for locale1"
      *         "more..."
      * LOCALE2 "string for locale2"
      *         "more..."
      * </pre>
      * 
      * @param mlstr
      *            multi-lang string to be parsed.
      * @return <code>null</code> iif <code>mlstr</code> is <code>null</code>.
      */
     public static XDomainString parseMultiLang(String mlstr) {
         if (mlstr == null)
             return null;
         MultiLangStringParser parser = new MultiLangStringParser();
         return parser.parse(mlstr);
     }
 
     @Override
     public String toMultiLangString() {
         return toMultiLangString("\n", null);
     }
 
     @Override
     public String toMultiLangString(String langSeparator, String lineSeparator) {
         MultiLangStringFormatter formatter = new MultiLangStringFormatter();
         formatter.setDomainSeparator(langSeparator);
         formatter.setLineSeparator(lineSeparator);
         return formatter.format(this);
     }
 
     @Override
     public DomainString append(DomainString other) {
         _join(other, false, this);
         return this;
     }
 
     @Override
     public XDomainString concat(DomainString other) {
         XDomainString out = clone();
         _join(other, false, out);
         return out;
     }
 
     @Override
     public XDomainString join(DomainString other) {
         XDomainString out = clone();
         _join(other, true, out);
         return out;
     }
 
     void _join(DomainString other, boolean bestFits, XDomainString output) {
         if (other == null)
             throw new NullPointerException("other");
 
         if (bestFits) { // find which domains are occurred in this only.
             for (Entry<String, XDomainString> entry : this) {
                 String d1 = entry.getKey();
                 XDomainString node1 = entry.getValue();
                 if (node1.value == null)
                     continue;
                 if (other.get(d1) != null)
                     continue;
 
                 String fallback2 = other.getNearest(d1);
                 if (fallback2 != null)
                     if (output == this)
                         node1.value += fallback2;
                     else
                         output.put(d1, node1.value + fallback2);
             }
         }
 
         for (Entry<String, String> entry : other.entrySet()) {
             String d2 = entry.getKey();
             String s2 = entry.getValue();
             if (s2 == null)
                 continue;
 
             String fallback1 = null;
             if (bestFits)
                 fallback1 = getNearest(d2);
 
             XDomainString outNode = output.create(d2, fallback1);
             if (outNode.value == null)
                 outNode.value = s2;
             else
                 outNode.value += s2;
         }
     }
 
     @Override
     public DomainString headPar() {
         return DomainStrings.headPar(this);
     }
 
     @Override
     public DomainString tailPar() {
         return DomainStrings.tailPar(this);
     }
 
     @Override
     public String getHeadPar() {
         String str = toString();
         return StringPart.getHeadPar(str);
     }
 
     @Override
     public String getTailPar() {
         String str = toString();
         return StringPart.getTailPar(str);
     }
 
 }
