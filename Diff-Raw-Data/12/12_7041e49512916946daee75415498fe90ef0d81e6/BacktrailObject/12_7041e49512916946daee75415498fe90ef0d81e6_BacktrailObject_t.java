 package ro.finsiel.eunis.backtrail;
 
 /**
  * This is an single backtrail object. <br />
  * for example: EUNIS >> SPECIES >> NAMES >> RESULTS are 4 backtrail objects. each having the NAME, which is displayed
  * in HTML and the associated link.
  * @author finsiel
  */
 public class BacktrailObject {
   private String name = null;
   private String url = null;
 
   // for example can be: style='color: #FFFFFF; text-decoration: none;'
   private String cssStyle = null;
 
   /**
    * Constructs an new BacktrailObject.
    */
   public BacktrailObject() {
   }
 
   /**
    * Construct directly the object.
    * @param name Anchor name.
    * @param url Anchor link.
    */
   public BacktrailObject( final String name, final String url )
   {
     this.name = (null != name) ? name : "";
     this.url = (null != url) ? url : "";
   }
 
   /**
    * Getter for url property.
    * @return URL.
    */
   public String getUrl() {
     return (null != url) ? url : "";
   }
 
   /**
    * Setter for url property.
    * @param url URL.
    */
   public void setUrl( final String url) {
     this.url = url;
   }
 
   /**
    * Getter for name property.
    * @return Name.
    */
   public String getName() {
     return (null != name) ? name : "";
   }
 
   /**
    * Setter for name property.
    * @param name Name.
    */
   public void setName( final String name )
   {
     this.name = name;
   }
 
   /**
    * Getter for css property.
    * @return CSS.
    */
   public String getCssStyle() {
     return cssStyle;
   }
 
   /**
    * Setter for css property.
    * @param cssStyle CSS.
    */
   public void setCssStyle( final String cssStyle)
   {
     this.cssStyle = cssStyle;
   }
 
   /**
    * return < A href='url">name< /A> or "< FONT color='black">name< /FONT>" if url is null.
    * @return HTML anchor.
    */
   public String toURLString() {
     if (null == url)
     {
      if( cssStyle == null )
      {
        return name;
      }
      else
      {
        return "<span class=\"" + cssStyle + "\">" + name + "</span>";
      }
     }
     else
     {
       if (null == cssStyle)
       {
         return "<a href=\"" + url + "\">" + name + "</a>";
       }
       else
       {
        return "<a href=\"" + url + "\" class=\"" + cssStyle + "\" >" + name + "</a>";
       }
     }
   }
 
   /**
    * Overriden from Object.toString()
    * @return An String representation of this object.
    */
   public String toString() {
     return "{NAME=" + getName() + "}, " + "{URL=" + getUrl() + "}";
   }
 }
