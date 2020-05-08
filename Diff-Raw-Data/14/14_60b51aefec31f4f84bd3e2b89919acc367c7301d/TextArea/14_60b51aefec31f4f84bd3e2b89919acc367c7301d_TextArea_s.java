 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package libs.form.fields;
 
 import java.util.Map;
 import libs.form.Form;
 
 /**
  *
  * @author kevin
  */
 public class TextArea extends TextField {
     public TextArea(String name) {
         super(name);
 
         setAttr("cols", "");
         setAttr("rows", "");
     }
 
     /**
      * Change la valeur de l'attribut cols
      *
      * @param $nb_cols nouvelle valeur
      *
      * @return obj (le champ en question)
      */
     public TextArea cols(int nbCols) {
         setAttr("cols", String.valueOf(nbCols));
 
         return this;
     }
 
     /**
      * Change la valeur de l'attribut cols
      *
      * @param $nbCols nouvelle valeur
      *
      * @return obj (le champ en question)
      */
     public TextArea cols(String colsPercent) {
         int cols = Integer.parseInt(
                     colsPercent.endsWith("%")
                         ? colsPercent.substring(0, colsPercent.length()-1)
                         : colsPercent);
 
         cols(cols);
 
         return this;
     }
 
     /**
      * Change la valeur de l'attribut Rows
      *
      * @param $nbRows nouvelle valeur
      *
      * @return obj (le champ en question)
      */
     public TextArea rows(int nbRows) {
         setAttr("rows", String.valueOf(nbRows));
 
         return this;
     }
 
     /**
      * Change la valeur de l'attribut cols
      *
      * @param $nbRows nouvelle valeur
      *
      * @return obj (le champ en question)
      */
     public TextArea rows(String rowsPercent) {
        int cols = Integer.parseInt(
                     rowsPercent.endsWith("%")
                         ? rowsPercent.substring(0, rowsPercent.length()-1)
                         : rowsPercent);
 
        cols(cols);
 
         return this;
     }
 
     @Override
     public String toHTML() {
         StringBuilder sb = new StringBuilder();
         makeClass();
 
         Map<String, String> html_attrs = getAttrs();
         html_attrs.remove("value");
         html_attrs.remove("type");
 
         sb.append("<textarea ").append(Form.toStringAttrs(html_attrs))
           .append(">")
           .append(getValue())
           .append("</textarea>");
 
         return sb.toString();
     }
 }
