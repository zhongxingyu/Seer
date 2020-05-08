 package ru.hh.spellcorrector.dto;
 
 import com.google.common.collect.Lists;
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import java.util.List;
 
 @XmlRootElement(name = "nigmer")
 public class NigmerDto {
 
   @XmlElement
  public List<WordDto> words = Lists.newArrayList();
 
 }
