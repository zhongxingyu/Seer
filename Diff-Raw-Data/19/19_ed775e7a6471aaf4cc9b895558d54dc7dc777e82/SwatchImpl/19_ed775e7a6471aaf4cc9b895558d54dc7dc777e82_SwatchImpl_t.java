 package com.socialcomputing.wps.server.persistence.hibernate;
 
 import java.io.Serializable;
 import java.io.StringReader;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.EmbeddedId;
 import javax.persistence.Entity;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.Transient;
 
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 
 import com.socialcomputing.wps.server.persistence.Dictionary;
 import com.socialcomputing.wps.server.persistence.Swatch;
 import com.socialcomputing.wps.server.swatchs.XSwatch;
 
 @Entity
 @Table(name = "swatchs")
 @org.hibernate.annotations.Table(appliesTo = "swatchs")
 @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
 public class SwatchImpl implements Serializable, Swatch {
 
     @EmbeddedId
     SwatchPk swatchPk;
     
     @ManyToOne(cascade = { CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH })
     @JoinColumn(name = "dictionaryName", unique = false, nullable = false, insertable = false, updatable = false)
     DictionaryImpl dictionary;
 
     @Column(name = "swatch", columnDefinition = "text")
     String swatch;
 
     @Transient
     private XSwatch m_Swatch = null;
 
     public SwatchImpl() {
         this.swatchPk = null;
         this.swatch = null;
     }
     
     public SwatchImpl(SwatchPk swatchPk, String swatch) {
         this.swatchPk = swatchPk;
         this.swatch = swatch;
     }
     
     @Override
    public String getName() {
        return swatchPk.getName();
    }
    
    @Override
     public String getDefinition() {
         return swatch;
     }
 
     @Override
     public void setDefinition(String definition) throws JDOMException {
         this.swatch = definition;
         this.m_Swatch = null;
     }
     
     public SwatchPk getSwatchPk() {
         return swatchPk;
     }
 
     public void setSwatchPk(SwatchPk swatchPk) {
         this.swatchPk = swatchPk;
     }
 
     @Override
     public XSwatch getSwatch() {
         try {
             if (m_Swatch == null) {
                 SAXBuilder builder = new SAXBuilder(false);
                 Document doc = builder.build(new StringReader(swatch));
                 Element root = doc.getRootElement();
                 m_Swatch = XSwatch.readObject(root);
             }
         }
         catch (Exception e) {
             e.printStackTrace();
         }
         return m_Swatch;
     }
 
 }
