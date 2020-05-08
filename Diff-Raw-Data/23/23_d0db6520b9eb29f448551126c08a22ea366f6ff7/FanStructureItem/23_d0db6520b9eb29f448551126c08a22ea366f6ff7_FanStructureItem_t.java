 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package net.colar.netbeans.fan.structure;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 import javax.swing.ImageIcon;
 import net.colar.netbeans.fan.FanParserResult;
 import org.antlr.runtime.CommonToken;
 import org.antlr.runtime.CommonTokenStream;
 import org.antlr.runtime.tree.CommonTree;
 import org.netbeans.modules.csl.api.ElementHandle;
 import org.netbeans.modules.csl.api.ElementKind;
 import org.netbeans.modules.csl.api.HtmlFormatter;
 import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.csl.api.OffsetRange;
 import org.netbeans.modules.csl.api.StructureItem;
 import org.netbeans.modules.csl.spi.ParserResult;
 
 /**
  *
  * @author thibautc
  */
 public class FanStructureItem implements StructureItem
 {
     private final CommonTree node;
     private final ParserResult result;
     private final FanElementHandle handle;
     private String name;
     private List<StructureItem> items=new ArrayList<StructureItem>();
     private String html;
     private final int start;
     private final int stop;
     
 
     public FanStructureItem(CommonTree node, ElementKind kind, ParserResult result)
     {
 	this.node = node;
 	//this.kind = node.getKind();
 	this.result = result;
 	//TODO: modifiers
 	this.name=node.getText();
	// node gives up index of 1st and last token part of this struct. item
	// then we finx those tokens by index in tokenStream (from lexer)
	// from that we can find start and end location of struct. text in source file.
 	CommonTokenStream tokenStream=((FanParserResult)result).getTokenStream();
 	CommonToken startToken=(CommonToken)tokenStream.get(node.getTokenStartIndex());
 	CommonToken endToken=(CommonToken)tokenStream.get(node.getTokenStopIndex());
 	this.start=startToken.getStartIndex();
 	this.stop=endToken.getStopIndex();
	OffsetRange range=new OffsetRange(start, stop);
	this.handle=new FanElementHandle(kind, (CommonToken)node.getToken(), result, range);
 	System.err.println(name+" ["+start+"-"+stop+"]");
     }
 
     public String getName()
     {
 	return name;
     }
 
     public String getSortText()
     {
 	return getName();
     }
 
     public String getHtml(HtmlFormatter arg0)
     {
 	return html!=null?html:getName();
     }
 
     public ElementHandle getElementHandle()
     {
 	return handle;
     }
 
     public ElementKind getKind()
     {
 	return handle.getKind();
     }
 
     public Set<Modifier> getModifiers()
     {
 	return handle.getModifiers();
     }
 
     public boolean isLeaf()
     {
 	return items.isEmpty();
     }
 
     public List<? extends StructureItem> getNestedItems()
     {
 	return items;
     }
 
     public long getPosition()
     {
 	return start;
     }
 
     public long getEndPosition()
     {
 	return stop;
     }
 
     public ImageIcon getCustomIcon()
     {
 	return null;
     }
 
     void setName(String text) {
 	name=text;
     }
 
     void addModifier(Modifier modifier) {
 	getModifiers().add(modifier);
     }
 
     void setNestedItems(List<StructureItem> subList) {
 	items=subList;
    }
 
     void setHtml(String string)
     {
 	html=string;
     }
 }
