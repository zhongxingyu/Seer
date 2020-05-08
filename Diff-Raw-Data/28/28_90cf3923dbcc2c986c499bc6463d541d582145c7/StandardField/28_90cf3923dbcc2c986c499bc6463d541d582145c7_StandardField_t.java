 package se.fnord.rt.core.internal;
 
 import java.io.Serializable;
 
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlElementWrapper;
 import javax.xml.bind.annotation.XmlValue;
 
 public class StandardField implements Field, Serializable {
     private static final long serialVersionUID = 735974018454589190L;
 
     @XmlAttribute(name = "name")
     private final String rtId;
     @XmlAttribute
     private final String label;
 
     @XmlElement
    private final String description;
     @XmlAttribute
     private final String kind;
     @XmlAttribute
     private final String type;
     @XmlAttribute
     private final boolean readOnly;
     @XmlAttribute
     private final String mylynId;
     @XmlAttribute(name = "mapper")
     private final String translatorName;
 
     public StandardField(final String mylynId, final String rtId, final String label, final String description, final String kind,
             final String type, final String translatorName, final boolean readOnly) {
         this.mylynId = mylynId;
         this.rtId = rtId;
         this.label = label;
        this.description = description;
         this.kind = kind;
         this.type = type;
         this.translatorName = translatorName;
         this.readOnly = readOnly;
     }
 
     public StandardField() {
         this.mylynId = null;
         this.rtId = null;
         this.label = null;
         this.description = null;
         this.kind = null;
         this.type = null;
         this.translatorName = null;
         this.readOnly = true;
     }
 
     @Override
     public String getRTId() {
         return rtId;
     }
 
     @Override
     public String getLabel() {
         return (label == null)?getRTId():label;
     }
 
     @Override
     public String getDescription() {
        return description;
     }
 
     @Override
     public String getKind() {
         return "task.common.kind." + kind;
     }
 
     @Override
     public String getType() {
         return type;
     }
 
     @Override
     public boolean isReadOnly() {
         return readOnly;
     }
 
     public String getMylynId() {
         return mylynId;
     }
 
     @Override
     public String getTranslatorName() {
         return translatorName;
     }
 
 }
