 package org.s23m.cell.communication.xml;
 
 import java.util.Arrays;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import org.eclipse.xtend2.lib.StringConcatenation;
 import org.eclipse.xtext.xbase.lib.IterableExtensions;
 import org.s23m.cell.communication.xml.NamespaceExtensions;
 import org.s23m.cell.communication.xml.StringUtils;
 import org.s23m.cell.communication.xml.model.dom.CompositeNode;
 import org.s23m.cell.communication.xml.model.dom.Namespace;
 import org.s23m.cell.communication.xml.model.dom.Node;
 import org.s23m.cell.communication.xml.model.schema.Schema;
 import org.s23m.cell.communication.xml.model.schemainstance.ArtifactSet;
 import org.s23m.cell.communication.xml.model.schemainstance.StringElement;
 
 @SuppressWarnings("all")
 public class XmlRendering {
   private static int INDENTATION = 2;
   
   private static String PREAMBLE = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
   
   public static String render(final Schema node) {
     CharSequence _doRender = XmlRendering.doRender(node);
     String _string = _doRender.toString();
     return _string;
   }
   
   public static String render(final ArtifactSet node) {
     CharSequence _doRender = XmlRendering.doRender(node);
     String _string = _doRender.toString();
     return _string;
   }
   
   private static CharSequence doRender(final CompositeNode node) {
     StringConcatenation _builder = new StringConcatenation();
     _builder.append(XmlRendering.PREAMBLE, "");
     _builder.newLineIfNotEmpty();
    String _render = XmlRendering.render(node, 0);
     _builder.append(_render, "");
     _builder.newLineIfNotEmpty();
     return _builder;
   }
   
  private static String _render(final CompositeNode node, final int level) {
     StringConcatenation _builder = new StringConcatenation();
     final Iterable<? extends Node> children = node.getChildren();
     _builder.newLineIfNotEmpty();
     String _renderPrefix = XmlRendering.renderPrefix(node, level);
     _builder.append(_renderPrefix, "");
     {
       boolean _isEmpty = IterableExtensions.isEmpty(children);
       if (_isEmpty) {
         _builder.append("/>");
         _builder.newLineIfNotEmpty();
       } else {
         _builder.append(">");
         _builder.newLineIfNotEmpty();
         {
           for(final Node n : children) {
             int _plus = (level + 1);
            String _render = XmlRendering.render(n, _plus);
             _builder.append(_render, "");
             _builder.newLineIfNotEmpty();
           }
         }
         String _renderSuffix = XmlRendering.renderSuffix(node, level);
         _builder.append(_renderSuffix, "");
         _builder.newLineIfNotEmpty();
       }
     }
    return _builder.toString();
   }
   
   private static String _render(final StringElement node, final int level) {
     String _renderPrefix = XmlRendering.renderPrefix(node, level);
     String _plus = (_renderPrefix + ">");
     String _text = node.getText();
     String _plus_1 = (_plus + _text);
     String _renderSuffix = XmlRendering.renderSuffix(node);
     String _plus_2 = (_plus_1 + _renderSuffix);
     return _plus_2;
   }
   
  private static String _render(final Node node, final int level) {
     String _renderPrefix = XmlRendering.renderPrefix(node, level);
     String _plus = (_renderPrefix + "/>");
     return _plus;
   }
   
   private static String renderPrefix(final Node node, final int level) {
     String _whitespace = XmlRendering.whitespace(level);
     String _plus = (_whitespace + "<");
     String _name = XmlRendering.name(node);
     String _plus_1 = (_plus + _name);
     String _renderAttributes = XmlRendering.renderAttributes(node);
     String _plus_2 = (_plus_1 + _renderAttributes);
     return _plus_2;
   }
   
   private static String renderSuffix(final Node node, final int level) {
     String _whitespace = XmlRendering.whitespace(level);
     String _renderSuffix = XmlRendering.renderSuffix(node);
     String _plus = (_whitespace + _renderSuffix);
     return _plus;
   }
   
   private static String renderSuffix(final Node node) {
     String _name = XmlRendering.name(node);
     String _plus = ("</" + _name);
     String _plus_1 = (_plus + ">");
     return _plus_1;
   }
   
   private static String whitespace(final int level) {
     int _multiply = (level * XmlRendering.INDENTATION);
     String _repeat = StringUtils.repeat(_multiply, " ");
     return _repeat;
   }
   
   private static String name(final Node node) {
     Namespace _namespace = node.getNamespace();
     String _prefix = _namespace.getPrefix();
     String _name = node.getName();
     String _qualifiedName = NamespaceExtensions.qualifiedName(_prefix, _name);
     return _qualifiedName;
   }
   
   private static String renderAttributes(final Node node) {
     String _xblockexpression = null;
     {
       StringBuilder _stringBuilder = new StringBuilder();
       final StringBuilder builder = _stringBuilder;
       Map<String,String> _attributes = node.getAttributes();
       final Set<Entry<String,String>> entrySet = _attributes.entrySet();
       for (final Entry<String,String> entry : entrySet) {
         {
           builder.append(" ");
           String _key = entry.getKey();
           builder.append(_key);
           builder.append("=\"");
           String _value = entry.getValue();
           builder.append(_value);
           builder.append("\"");
         }
       }
       String _string = builder.toString();
       _xblockexpression = (_string);
     }
     return _xblockexpression;
   }
   
  private static String render(final Node node, final int level) {
     if (node instanceof StringElement) {
       return _render((StringElement)node, level);
     } else if (node instanceof CompositeNode) {
       return _render((CompositeNode)node, level);
     } else if (node != null) {
       return _render(node, level);
     } else {
       throw new IllegalArgumentException("Unhandled parameter types: " +
         Arrays.<Object>asList(node, level).toString());
     }
   }
 }
