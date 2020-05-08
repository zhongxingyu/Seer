 package cytoscape.data.readers;
 
 import cytoscape.data.CyAttributes;
 import cytoscape.data.attr.MultiHashMapDefinition;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 public class CyAttributesReader
 {
 
   public static void loadAttributes(CyAttributes cyAttrs,
                                     Reader fileIn) throws IOException
   {
     final BufferedReader reader;
     if (fileIn instanceof BufferedReader) { reader = (BufferedReader) fileIn; }
     else { reader = new BufferedReader(fileIn); }
     final String attributeName;
     byte type = -1;
     {
       final String firstLine = reader.readLine();
       if (firstLine == null) { return; }
       final StringTokenizer tokens = new StringTokenizer(firstLine);
       attributeName = tokens.nextToken();
       if (tokens.hasMoreTokens()) {
         final String foo = tokens.nextToken();
         final String searchStr = "class=";
         final int inx = foo.indexOf(searchStr);
         if (inx >= 0) {
           String className = foo.substring(inx + searchStr.length()).trim();
           if (className.endsWith(")")) {
             className = className.substring(0, className.length() - 1); }
           if (className.equalsIgnoreCase("java.lang.String")) {
             type = MultiHashMapDefinition.TYPE_STRING; }
           else if (className.equalsIgnoreCase("java.lang.Boolean")) {
             type = MultiHashMapDefinition.TYPE_BOOLEAN; }
           else if (className.equalsIgnoreCase("java.lang.Integer")) {
             type = MultiHashMapDefinition.TYPE_INTEGER; }
           else if (className.equalsIgnoreCase("java.lang.Double")) {
             type = MultiHashMapDefinition.TYPE_FLOATING_POINT; } } }
     }
     boolean firstLine = true;
     boolean list = false;
     while (true) {
       final String line = reader.readLine();
       if (line == null) { break; }
       int inx = line.indexOf('=');
       final String key = line.substring(0, inx).trim();
       String val = line.substring(inx + 1).trim();
       if (firstLine && val.startsWith("(")) { list = true; }
       if (list) {
         // Chop away leading '(' and trailing ')'.
         val = val.substring(1).trim();
         val = val.substring(0, val.length() - 1).trim();
         final StringTokenizer elms = new StringTokenizer(val, ":");
         final ArrayList elmsBuff = new ArrayList();
         while (elms.hasMoreTokens()) {
           elmsBuff.add(elms.nextToken().trim()); }
         if (firstLine) {
           if (type < 0) {
             while (true) {
               try {
                 new Integer((String) elmsBuff.get(0));
                 type = MultiHashMapDefinition.TYPE_INTEGER;
                 break; }
               catch (Exception e) {}
               try {
                 new Double((String) elmsBuff.get(0));
                 type = MultiHashMapDefinition.TYPE_FLOATING_POINT;
                 break; }
               catch (Exception e) {}
              try {
                new Boolean((String) elmsBuff.get(0));
                type = MultiHashMapDefinition.TYPE_BOOLEAN;
                break; }
              catch (Exception e) {}
               type = MultiHashMapDefinition.TYPE_STRING;
               break; } }
           firstLine = false; }
         for (int i = 0; i < elmsBuff.size(); i++) {
           if (type == MultiHashMapDefinition.TYPE_INTEGER) {
             elmsBuff.set(i, new Integer((String) elmsBuff.get(i))); }
           else if (type == MultiHashMapDefinition.TYPE_BOOLEAN) {
             elmsBuff.set(i, new Boolean((String) elmsBuff.get(i))); }
           else if (type == MultiHashMapDefinition.TYPE_FLOATING_POINT) {
             elmsBuff.set(i, new Double((String) elmsBuff.get(i))); }
           else {
             // A string; do nothing.
           } }
         cyAttrs.setAttributeList(key, attributeName, elmsBuff); }
       else { // Not a list.
         if (firstLine) {
           if (type < 0) {
             while (true) {
               try {
                 new Integer(val);
                 type = MultiHashMapDefinition.TYPE_INTEGER;
                 break; }
               catch (Exception e) {}
               try {
                 new Double(val);
                 type = MultiHashMapDefinition.TYPE_FLOATING_POINT;
                 break; }
               catch (Exception e) {}
              try {
                new Boolean(val);
                type = MultiHashMapDefinition.TYPE_BOOLEAN;
                break; }
              catch (Exception e) {}
               type = MultiHashMapDefinition.TYPE_STRING;
               break; } }
           firstLine = false; }
         if (type == MultiHashMapDefinition.TYPE_INTEGER) {
           cyAttrs.setAttribute(key, attributeName, new Integer(val)); }
         else if (type == MultiHashMapDefinition.TYPE_BOOLEAN) {
           cyAttrs.setAttribute(key, attributeName, new Boolean(val)); }
         else if (type == MultiHashMapDefinition.TYPE_FLOATING_POINT) {
           cyAttrs.setAttribute(key, attributeName, new Double(val)); }
         else {
           cyAttrs.setAttribute(key, attributeName, val); } } }            
   }
 
 }
