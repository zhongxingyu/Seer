 package com.socialcomputing.wps.server.persistence;
 
 import org.jdom.JDOMException;
 
 import com.socialcomputing.wps.server.persistence.hibernate.SwatchPk;
 import com.socialcomputing.wps.server.swatchs.XSwatch;
 
 /**
  * Title: Swatch Loader Description: Copyright: Copyright (c) 2000 Company:
  * VOYEZ VOUS
  * 
  * @author Franck Valetas
  * @version 1.0
  */
 
 public interface Swatch {
    public String getName();
 
     public XSwatch getSwatch() throws JDOMException;
 
     public void setDefinition(String definition) throws JDOMException;
     
     public SwatchPk getSwatchPk();
     
     public void setSwatchPk(SwatchPk swatchPk);
     
     public String getDefinition();
 }
