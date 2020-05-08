 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package net.colar.netbeans.fan.structure;
 
 import java.util.HashSet;
 import java.util.Set;
 import org.antlr.runtime.CommonToken;
 import org.netbeans.modules.csl.api.ElementHandle;
 import org.netbeans.modules.csl.api.ElementKind;
 import org.netbeans.modules.csl.api.Modifier;
 import org.netbeans.modules.csl.api.OffsetRange;
 import org.netbeans.modules.csl.spi.ParserResult;
 import org.netbeans.modules.parsing.api.Source;
 import org.openide.filesystems.FileObject;
 
 /**
  *
  * @author tcolar
  */
 public class FanElementHandle implements ElementHandle{
     private final ParserResult result;
     private final Source source;
     private final CommonToken token;
     private ElementKind kind;
     private OffsetRange offsetRange;
     private Set<Modifier> modifiers=new HashSet<Modifier>();
 
    public FanElementHandle(ElementKind kind, CommonToken token, ParserResult result)
     {
 	this.token=token;
 	this.result=result;
 	this.source=result.getSnapshot().getSource();
 	this.kind=kind;
	this.offsetRange=new OffsetRange(token.getStartIndex(), token.getStopIndex());
     }
 
     public FileObject getFileObject() {
 	return source.getFileObject();
     }
 
     public String getMimeType() {
 	return source.getMimeType();
     }
 
     public String getName() {
 	return token.getText();
     }
 
     public String getIn() {
 	return "";
     }
 
     public ElementKind getKind() {
 	return kind;
     }
 
     public Set<Modifier> getModifiers() {
 	return modifiers;
     }
 
     public void setModifiers(Set<Modifier> modifiers)
     {
 	this.modifiers=modifiers;
     }
 
     public boolean signatureEquals(ElementHandle arg0) {
 	return false;//TODO
     }
 
    public OffsetRange getOffsetRange(ParserResult arg0) {
 	return offsetRange;
     }
 
 }
