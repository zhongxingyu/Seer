 package com.example.fsexplorer.fs;
 
 import com.google.common.collect.ImmutableList;
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import java.util.List;
 import java.util.Objects;
 
 /**
  * @author Andrey Tarashevsky
  *         Date: 04.02.13
  */
 @XmlRootElement(name = "fileType")
public final class FileType {
 
     private final String className;
     private final List<String> extensions;
     private final String handlerUrl;
 
     public FileType(String className, List<String> extensions, String handlerUrl) {
         this.className = className;
         this.extensions = ImmutableList.copyOf(extensions);
         this.handlerUrl = handlerUrl;
     }
 
     @SuppressWarnings("UnusedDeclaration")
     @XmlElement
     public boolean isExpandable() {
         return !handlerUrl.isEmpty();
     }
 
     @XmlElement
     public String getClassName() {
         return className;
     }
 
     public List<String> getExtensions() {
         return extensions;
     }
 
     @XmlElement
     public String getHandlerUrl() {
         return handlerUrl;
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) {
             return true;
         }
 
         if (!(o instanceof FileType)) {
             return false;
         }
 
         final FileType other = (FileType)o;
 
         return Objects.equals(className, other.className)
                 && Objects.equals(handlerUrl, other.handlerUrl)
                 && Objects.equals(extensions, other.extensions);
     }
 
     @Override
     public int hashCode() {
         return Objects.hash(className, handlerUrl, extensions);
     }
 }
