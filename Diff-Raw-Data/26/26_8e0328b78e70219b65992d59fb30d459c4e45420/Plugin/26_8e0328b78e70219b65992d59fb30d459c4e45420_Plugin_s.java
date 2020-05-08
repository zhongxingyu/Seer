 package jp.co.nttcom.camel.documentbuilder.xml.template;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 @XmlAccessorType(XmlAccessType.NONE)
 @XmlRootElement(name = "plugin")
 public class Plugin {
 
     /**
      * プラグインが提供するコンポーネント。
      */
    List<Extension> extensions = new ArrayList<Extension>();
 
     @XmlElement(name = "extension")
     public List<Extension> getExtensions() {
         return extensions;
     }
 
     public void setExtensions(List<Extension> extensions) {
         this.extensions = extensions;
     }
 
     /**
      * コンポーネント検索処理
      * 
      * 指定したパレット上のラベルを持つコンポーネントを取得します。
      * 存在しない場合はnullを返します。
      * 
      * @param paletteLabel パレット上のラベル
      * @return コンポーネント。存在しない場合はnullを返す。
      */
     public Extension findExtension(String paletteLabel) {
         for (Extension extension : extensions) {
             Properties properties = extension.getProperties();
             if (properties.getPaletteLabel().equals(paletteLabel)) {
                 return extension;
             }
         }
         return null;
     }
 }
