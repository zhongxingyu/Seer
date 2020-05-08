 package org.kevoree.library.javase.basicGossiper;
 
import org.kevoree.annotation.DictionaryAttribute;
import org.kevoree.annotation.DictionaryType;

 import java.util.List;
 
@DictionaryType({
        @DictionaryAttribute(name = "interval", defaultValue = "30000", optional = true)
})
 public interface GossiperComponent {
 
 	public List<String> getAddresses(String remoteNodeName);
 
 	public int parsePortNumber(String nodeName);
 
 	public String getName();
 
 	public String getNodeName();
 
 	public void localNotification(Object data);
 }
