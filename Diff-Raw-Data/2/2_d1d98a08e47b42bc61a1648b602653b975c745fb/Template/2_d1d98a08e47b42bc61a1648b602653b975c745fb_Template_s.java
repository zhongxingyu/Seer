 package jp.co.nttcom.camel.documentbuilder.xml.template;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import javax.xml.bind.JAXB;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.filefilter.FileFilterUtils;
 import org.apache.commons.io.filefilter.TrueFileFilter;
 
public class Template {
 
     private List<Plugin> plugins = new ArrayList<Plugin>();
 
     public static Template load(String dir) throws IOException {
         if (dir == null) {
             throw new IOException("Dir should not be null.");
         }
         
         File xmlDir = new File(dir);
         if (!xmlDir.exists()) {
             throw new FileNotFoundException("No directory found : " + dir);
         }
         if (!xmlDir.isDirectory()) {
             throw new IOException("Not directory : " + dir);
         }
         
         List<Plugin> plugins = new ArrayList<Plugin>();
         for (File file : getPluginXMLs(xmlDir)) {
             Plugin plugin = JAXB.unmarshal(file, Plugin.class);
             // 並び順を付与
             for (Extension extension : plugin.getExtensions()) {
                 int displayNo = 1;
                 for (StringProperty property : extension.getProperties().getStringProperties()) {
                     property.setDisplayNo(displayNo);
                     displayNo++;
                 }
             }
             plugins.add(plugin);
         }
         Template template = new Template();
         template.setPlugins(plugins);
         return template;
     }
 
     private Template() {
         // 
     }
         
     private static Collection<File> getPluginXMLs(File dir) {
         return FileUtils.listFiles(dir, FileFilterUtils.nameFileFilter("plugin.xml"), TrueFileFilter.TRUE);
     }    
     
     public List<Plugin> getPlugins() {
         return plugins;
     }
 
     public void setPlugins(List<Plugin> plugins) {
         this.plugins = plugins;
     }
    
     /**
      * コンポーネント情報取得
      * 
      * 指定したパレット名を持つコンポーネントをテンプレートから取得します。
      * 取得できない場合は、nullを返します。
      * 
      * @param paletteLabel パレット名
      * @return パレット名を持つテンプレートのコンポーネント。該当しない場合はnullを返す。
      */
     public Extension findExtension(String paletteLabel) {
         for (Plugin plugin : plugins) {
             Extension extension = plugin.findExtension(paletteLabel);
             if (extension != null) {
                 return extension;
             }
         }
         return null;
     }    
 }
