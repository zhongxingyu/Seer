 package org.facttype.generator;
 
 import com.google.common.base.Objects;
 import com.google.common.collect.Iterables;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.TreeIterator;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.xtend2.lib.StringConcatenation;
 import org.eclipse.xtext.generator.IFileSystemAccess;
 import org.eclipse.xtext.xbase.lib.Conversions;
 import org.eclipse.xtext.xbase.lib.Functions.Function1;
 import org.eclipse.xtext.xbase.lib.IterableExtensions;
 import org.eclipse.xtext.xbase.lib.IteratorExtensions;
 import org.eclipse.xtext.xbase.lib.ListExtensions;
 import org.facttype.diagram.AbstractRule;
 import org.facttype.diagram.AfterBefore;
 import org.facttype.diagram.AlternativeKey;
 import org.facttype.diagram.Column;
 import org.facttype.diagram.Content;
 import org.facttype.diagram.Crud;
 import org.facttype.diagram.DerivationRule;
 import org.facttype.diagram.Derived;
 import org.facttype.diagram.Description;
 import org.facttype.diagram.Diagram;
 import org.facttype.diagram.EqualityRule;
 import org.facttype.diagram.EventRule;
 import org.facttype.diagram.ExclusionRule;
 import org.facttype.diagram.FactTypeDiagram;
 import org.facttype.diagram.GeneralConstraint;
 import org.facttype.diagram.Maximum;
 import org.facttype.diagram.Minimum;
 import org.facttype.diagram.NoOverlappingRule;
 import org.facttype.diagram.OccurrenceFrequencyRule;
 import org.facttype.diagram.PartialEqualityRule;
 import org.facttype.diagram.PrimaryKey;
 import org.facttype.diagram.SentenceTemplate;
 import org.facttype.diagram.SubsetRule;
 import org.facttype.diagram.Type;
 import org.facttype.diagram.Value;
 import org.facttype.diagram.ValueRule;
 import org.facttype.generator.ColumnNameComparator;
 import org.facttype.generator.Counter;
 import org.facttype.generator.DiagramComparator;
 import org.facttype.generator.EnterpriseArchitectUmlXmiGenerator;
 import org.facttype.generator.IGenerator2;
 import org.facttype.generator.Switch;
 
 /**
  * HTML generator to generate Fact Type Diagrams in HTML format.
  */
 @SuppressWarnings("all")
 public class DiagramGenerator implements IGenerator2 {
   public void doGenerate(final ResourceSet rs, final IFileSystemAccess fsa) {
     EList<Resource> _resources = rs.getResources();
     final Function1<Resource,Iterable<Diagram>> _function = new Function1<Resource,Iterable<Diagram>>() {
         public Iterable<Diagram> apply(final Resource r) {
           TreeIterator<EObject> _allContents = r.getAllContents();
           Iterable<EObject> _iterable = IteratorExtensions.<EObject>toIterable(_allContents);
           Iterable<Diagram> _filter = Iterables.<Diagram>filter(_iterable, Diagram.class);
           return _filter;
         }
       };
     List<Iterable<Diagram>> _map = ListExtensions.<Resource, Iterable<Diagram>>map(_resources, _function);
     final Iterable<Diagram> diagrams = Iterables.<Diagram>concat(_map);
     CharSequence _indexHtml = this.indexHtml(diagrams);
     fsa.generateFile("index.html", _indexHtml);
     CharSequence _glossaryHtml = this.glossaryHtml(diagrams);
     fsa.generateFile("glossary.html", _glossaryHtml);
     EnterpriseArchitectUmlXmiGenerator _enterpriseArchitectUmlXmiGenerator = new EnterpriseArchitectUmlXmiGenerator();
     final EnterpriseArchitectUmlXmiGenerator enterpriseArchitectUmlXmiGenerator = _enterpriseArchitectUmlXmiGenerator;
     CharSequence _generateXmlFile = enterpriseArchitectUmlXmiGenerator.generateXmlFile(diagrams);
     fsa.generateFile("enterpriseArchitect.xmi", _generateXmlFile);
     for (final Diagram diagram : diagrams) {
       String _relativeFileName = this.getRelativeFileName(diagram);
       Resource _eResource = diagram.eResource();
       EList<EObject> _contents = _eResource.getContents();
       EObject _head = IterableExtensions.<EObject>head(_contents);
       CharSequence _facttypeDiagramHtmlPage = this.facttypeDiagramHtmlPage(((Diagram) _head));
       fsa.generateFile(_relativeFileName, _facttypeDiagramHtmlPage);
     }
   }
   
   /**
    * Generate list of all columns
    * https://github.com/escay/Facttype/issues/27
    */
   public CharSequence glossaryHtml(final Iterable<Diagram> diagrams) {
     StringConcatenation _builder = new StringConcatenation();
     _builder.append("<!DOCTYPE html>");
     _builder.newLine();
     _builder.append("<html>");
     _builder.newLine();
     _builder.append("<head>");
     _builder.newLine();
     _builder.append("\t");
     _builder.append("<meta charset=\"utf-8\">");
     _builder.newLine();
     _builder.append("\t");
     _builder.append("<title>Facttype Diagrams</title>");
     _builder.newLine();
     _builder.append("\t");
     _builder.append("<link rel=\"stylesheet\" href=\"../css/styles.css\" type=\"text/css\"/>");
     _builder.newLine();
     _builder.append("\t");
     _builder.append("</head>");
     _builder.newLine();
     _builder.append("<body>");
     _builder.newLine();
     _builder.append("    ");
     _builder.append("<a href=\"index.html\">Facttype Diagrams</a>");
     _builder.newLine();
     _builder.append("\t");
     _builder.append("<h1>Columns</h1>");
     _builder.newLine();
     _builder.append("\t");
     _builder.append("<table class=\"index-data\">");
     _builder.newLine();
     _builder.append("\t\t");
     _builder.append("<tr>");
     _builder.newLine();
     _builder.append("\t\t\t");
     _builder.append("<th>Name</th>");
     _builder.newLine();
     _builder.append("\t\t\t");
     _builder.append("<th>Fast type diagram</th>");
     _builder.newLine();
     _builder.append("\t\t\t");
     _builder.append("<th>Description</th>");
     _builder.newLine();
     _builder.append("\t\t");
     _builder.append("</tr>");
     _builder.newLine();
     _builder.append("\t");
     _builder.append("<!-- ");
     ArrayList<Column> _arrayList = new ArrayList<Column>();
     ArrayList<Column> columns = _arrayList;
     _builder.append(" -->");
     _builder.newLineIfNotEmpty();
     {
       for(final Diagram diagram : diagrams) {
         {
           EList<FactTypeDiagram> _factTypeDiagrams = diagram.getFactTypeDiagrams();
           for(final FactTypeDiagram facttypeDiagram : _factTypeDiagrams) {
             {
               EList<Column> _columns = facttypeDiagram.getColumns();
               for(final Column column : _columns) {
                 _builder.append("\t");
                 _builder.append("<!-- ");
                 boolean _add = columns.add(column);
                 _builder.append(_add, "	");
                 _builder.append(" -->");
                 _builder.newLineIfNotEmpty();
               }
             }
           }
         }
       }
     }
     {
       ColumnNameComparator _columnNameComparator = new ColumnNameComparator();
       List<Column> _sort = IterableExtensions.<Column>sort(columns, _columnNameComparator);
       for(final Column column_1 : _sort) {
         _builder.append("\t");
         _builder.append("<tr>");
         _builder.newLine();
         _builder.append("\t");
         _builder.append("    ");
         _builder.append("<td>");
         _builder.newLine();
         _builder.append("\t");
         _builder.append("    \t");
         _builder.append("<em>");
         String _name = column_1.getName();
         _builder.append(_name, "	    	");
         _builder.append("</em>");
         _builder.newLineIfNotEmpty();
         _builder.append("\t");
         _builder.append("    ");
         _builder.append("</td>");
         _builder.newLine();
         _builder.append("\t");
         _builder.append("\t");
         _builder.append("<td>");
         _builder.newLine();
         _builder.append("\t");
         _builder.append("\t\t");
         EObject _eContainer = column_1.eContainer();
         FactTypeDiagram facttypeDiagram_1 = ((FactTypeDiagram) _eContainer);
         _builder.newLineIfNotEmpty();
         _builder.append("\t");
         _builder.append("\t\t");
         _builder.append("<a href=\"");
         EObject _eContainer_1 = facttypeDiagram_1.eContainer();
         String _relativeFileName = this.getRelativeFileName(((Diagram) _eContainer_1));
         _builder.append(_relativeFileName, "			");
         _builder.append("\">");
         String _name_1 = facttypeDiagram_1.getName();
         _builder.append(_name_1, "			");
         _builder.append("</a></td>");
         _builder.newLineIfNotEmpty();
         _builder.append("\t");
         _builder.append("\t");
         _builder.append("</td>");
         _builder.newLine();
         _builder.append("\t");
         _builder.append("\t");
         _builder.append("<td>");
         _builder.newLine();
         _builder.append("\t");
         _builder.append("\t\t");
         _builder.append("<em>");
         String _concept = column_1.getConcept();
         _builder.append(_concept, "			");
         _builder.append("</em>");
         _builder.newLineIfNotEmpty();
         _builder.append("\t");
         _builder.append("\t");
         _builder.append("</td>");
         _builder.newLine();
         _builder.append("\t");
         _builder.append("</tr>");
         _builder.newLine();
       }
     }
     _builder.append("</body>");
     _builder.newLine();
     _builder.append("</html>\t");
     _builder.newLine();
     return _builder;
   }
   
   public void doGenerate(final Resource resource, final IFileSystemAccess fsa) {
   }
   
   /**
    * Generates an index.html page with links to all facttype diagrams.
    */
   public CharSequence indexHtml(final Iterable<Diagram> diagrams) {
     StringConcatenation _builder = new StringConcatenation();
     _builder.append("<!DOCTYPE html>");
     _builder.newLine();
     _builder.append("<html>");
     _builder.newLine();
     _builder.append("<head>");
     _builder.newLine();
     _builder.append("\t");
     _builder.append("<meta charset=\"utf-8\">");
     _builder.newLine();
     _builder.append("\t");
     _builder.append("<title>Facttype Diagrams</title>");
     _builder.newLine();
     _builder.append("\t");
     _builder.append("<link rel=\"stylesheet\" href=\"../css/styles.css\" type=\"text/css\"/>");
     _builder.newLine();
     _builder.append("\t");
     _builder.append("</head>");
     _builder.newLine();
     _builder.append("<body>");
     _builder.newLine();
     _builder.append("\t");
     _builder.append("<a href=\"glossary.html\">Glossary</a>");
     _builder.newLine();
     _builder.append("\t");
     _builder.append("<h1>Facttype diagrams</h1>");
     _builder.newLine();
     _builder.append("\t");
     _builder.append("<table class=\"index-data\">");
     _builder.newLine();
     _builder.append("\t\t");
     _builder.append("<tr>");
     _builder.newLine();
     _builder.append("\t\t\t");
     _builder.append("<th>File</th>");
     _builder.newLine();
     _builder.append("\t\t\t");
     _builder.append("<th>Name</th>");
     _builder.newLine();
     _builder.append("\t\t\t");
     _builder.append("<th>Description</th>");
     _builder.newLine();
     _builder.append("\t\t");
     _builder.append("</tr>");
     _builder.newLine();
     {
       DiagramComparator _diagramComparator = new DiagramComparator();
       List<Diagram> _sort = IterableExtensions.<Diagram>sort(diagrams, _diagramComparator);
       for(final Diagram diagram : _sort) {
         {
           EList<FactTypeDiagram> _factTypeDiagrams = diagram.getFactTypeDiagrams();
           for(final FactTypeDiagram facttypeDiagram : _factTypeDiagrams) {
             _builder.append("\t");
             _builder.append("<tr>");
             _builder.newLine();
             _builder.append("\t");
             _builder.append("    ");
             _builder.append("<td>");
             String _packageNameAndFileName = this.getPackageNameAndFileName(diagram, "/");
             _builder.append(_packageNameAndFileName, "	    ");
             _builder.append("</td>");
             _builder.newLineIfNotEmpty();
             _builder.append("\t");
             _builder.append("\t");
             _builder.append("<td><a href=\"");
             EObject _eContainer = facttypeDiagram.eContainer();
             String _relativeFileName = this.getRelativeFileName(((Diagram) _eContainer));
             _builder.append(_relativeFileName, "		");
             _builder.append("\">");
             String _name = facttypeDiagram.getName();
             _builder.append(_name, "		");
             _builder.append("</a></td>");
             _builder.newLineIfNotEmpty();
             _builder.append("\t");
             _builder.append("\t");
             _builder.append("<td>");
             _builder.newLine();
             {
               Description _description = facttypeDiagram.getDescription();
               boolean _notEquals = (!Objects.equal(_description, null));
               if (_notEquals) {
                 _builder.append("\t");
                 _builder.append("\t");
                 _builder.append("<em>");
                 Description _description_1 = facttypeDiagram.getDescription();
                 String _text = _description_1.getText();
                 _builder.append(_text, "		");
                 _builder.append("</em>");
                 _builder.newLineIfNotEmpty();
               }
             }
             _builder.append("\t");
             _builder.append("\t");
             _builder.append("</td>");
             _builder.newLine();
             _builder.append("\t");
             _builder.append("</tr>");
             _builder.newLine();
           }
         }
       }
     }
     _builder.append("\t");
     _builder.append("</table>");
     _builder.newLine();
     _builder.append("</body>");
     _builder.newLine();
     _builder.append("</html>");
     _builder.newLine();
     return _builder;
   }
   
   public String className(final Resource res) {
     URI _uRI = res.getURI();
     String name = _uRI.lastSegment();
     int _indexOf = name.indexOf(".");
     return name.substring(0, _indexOf);
   }
   
   /**
    * Returns the ../../../ to get to the root directory.
    */
   public String getToRootDirectory(final AbstractRule rule) {
     EObject _eContainer = rule.eContainer();
     EObject _eContainer_1 = _eContainer.eContainer();
     return this.getToRootDirectory(((Diagram) _eContainer_1));
   }
   
   /**
    * Returns the package/subpackage name.
    */
   public String getPackageNameAndFileName(final Diagram diagram, final String separator) {
     Resource _eResource = diagram.eResource();
     URI _uRI = _eResource.getURI();
     String _path = _uRI.path();
     int index = _path.indexOf("/src");
     String result = "";
     int _minus = (-1);
     boolean _notEquals = (index != _minus);
     if (_notEquals) {
       int _plus = (index + 5);
       index = _plus;
       Resource _eResource_1 = diagram.eResource();
       URI _uRI_1 = _eResource_1.getURI();
       String _path_1 = _uRI_1.path();
       String substring = _path_1.substring(index);
       String[] results = substring.split("/");
       index = 0;
       final String[] _converted_results = (String[])results;
       int _size = ((List<String>)Conversions.doWrapArray(_converted_results)).size();
       boolean _lessThan = (index < _size);
       boolean _while = _lessThan;
       while (_while) {
         {
           String _plus_1 = (result + separator);
           final String[] _converted_results_1 = (String[])results;
           String _get = ((List<String>)Conversions.doWrapArray(_converted_results_1)).get(index);
           String _plus_2 = (_plus_1 + _get);
           result = _plus_2;
           int _plus_3 = (index + 1);
           index = _plus_3;
         }
         final String[] _converted_results_1 = (String[])results;
         int _size_1 = ((List<String>)Conversions.doWrapArray(_converted_results_1)).size();
         boolean _lessThan_1 = (index < _size_1);
         _while = _lessThan_1;
       }
     }
     return result;
   }
   
   /**
    * Returns the ../../../ to get to the root directory.
    */
   public String getToRootDirectory(final Diagram diagram) {
     Resource _eResource = diagram.eResource();
     URI _uRI = _eResource.getURI();
     String _path = _uRI.path();
     int index = _path.indexOf("/src");
     String result = "";
     int _minus = (-1);
     boolean _notEquals = (index != _minus);
     if (_notEquals) {
       int _plus = (index + 5);
       index = _plus;
       Resource _eResource_1 = diagram.eResource();
       URI _uRI_1 = _eResource_1.getURI();
       String _path_1 = _uRI_1.path();
       String substring = _path_1.substring(index);
       String[] results = substring.split("/");
       final String[] _converted_results = (String[])results;
       int _size = ((List<String>)Conversions.doWrapArray(_converted_results)).size();
       index = _size;
       boolean _greaterThan = (index > 1);
       boolean _while = _greaterThan;
       while (_while) {
         {
           String _plus_1 = (result + "../");
           result = _plus_1;
           int _minus_1 = (index - 1);
           index = _minus_1;
         }
         boolean _greaterThan_1 = (index > 1);
         _while = _greaterThan_1;
       }
     }
     boolean _equals = result.equals("");
     if (_equals) {
       result = "./";
     }
     return result;
   }
   
   /**
    * Returns the relative path, compared to /src
    * If /src is not found the className is returned
    */
   public String getRelativeFileName(final Diagram diagram) {
     Resource _eResource = diagram.eResource();
     URI _uRI = _eResource.getURI();
     String _path = _uRI.path();
     int index = _path.indexOf("/src");
     String result = "";
     int _minus = (-1);
     boolean _notEquals = (index != _minus);
     if (_notEquals) {
       int _plus = (index + 4);
       index = _plus;
       Resource _eResource_1 = diagram.eResource();
       URI _uRI_1 = _eResource_1.getURI();
       String _path_1 = _uRI_1.path();
       String _substring = _path_1.substring(index);
       result = _substring;
     } else {
       Resource _eResource_2 = diagram.eResource();
       String _className = this.className(_eResource_2);
       result = _className;
     }
     String _replace = result.replace(".ftd", ".html");
     result = _replace;
     return ("." + result);
   }
   
   /**
    * Generates the HTML page containing the facttype diagram.
    */
   public CharSequence facttypeDiagramHtmlPage(final Diagram diagram) {
     StringConcatenation _builder = new StringConcatenation();
     _builder.append("<!DOCTYPE html>");
     _builder.newLine();
     _builder.append("<html>");
     _builder.newLine();
     _builder.append("<head>");
     _builder.newLine();
     _builder.append("\t");
     _builder.append("<meta charset=\"utf-8\">");
     _builder.newLine();
     _builder.append("\t");
     _builder.append("<title>");
     Resource _eResource = diagram.eResource();
     String _className = this.className(_eResource);
     _builder.append(_className, "	");
     _builder.append("</title>");
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     _builder.append("<link rel=\"stylesheet\" href=\"");
     String _toRootDirectory = this.getToRootDirectory(diagram);
     _builder.append(_toRootDirectory, "	");
     _builder.append("../css/styles.css\" type=\"text/css\"/>");
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     _builder.append("</head>");
     _builder.newLine();
     _builder.append("<body>");
     _builder.newLine();
     {
       EList<FactTypeDiagram> _factTypeDiagrams = diagram.getFactTypeDiagrams();
       for(final FactTypeDiagram factTypeDiagram : _factTypeDiagrams) {
         _builder.append("    ");
         _builder.append("<div class=\"index\"><a href=\"");
         String _toRootDirectory_1 = this.getToRootDirectory(diagram);
         _builder.append(_toRootDirectory_1, "    ");
         _builder.append("index.html\">Back to index</a></div>");
         _builder.newLineIfNotEmpty();
         _builder.append("    ");
         _builder.append("<!-- Resource: ");
         String _relativeFileName = this.getRelativeFileName(diagram);
         _builder.append(_relativeFileName, "    ");
         _builder.append(" -->");
         _builder.newLineIfNotEmpty();
         _builder.append("    ");
         _builder.append("<!-- To root: ");
         String _toRootDirectory_2 = this.getToRootDirectory(diagram);
         _builder.append(_toRootDirectory_2, "    ");
         _builder.append(" -->");
         _builder.newLineIfNotEmpty();
         _builder.append("\t");
         _builder.append("<!-- Name -->");
         _builder.newLine();
         _builder.append("\t");
         _builder.append("<h1>");
         String _name = factTypeDiagram.getName();
         _builder.append(_name, "	");
         _builder.append("</h1>");
         _builder.newLineIfNotEmpty();
         _builder.append("\t");
         _builder.newLine();
         {
           Description _description = factTypeDiagram.getDescription();
           boolean _notEquals = (!Objects.equal(_description, null));
           if (_notEquals) {
             _builder.append("\t");
             _builder.append("<p>");
             Description _description_1 = factTypeDiagram.getDescription();
             String _text = _description_1.getText();
             _builder.append(_text, "	");
             _builder.append("</p>");
             _builder.newLineIfNotEmpty();
           }
         }
         _builder.newLine();
         _builder.append("\t");
         _builder.append("<!-- Column data-->");
         _builder.newLine();
         _builder.append("\t");
         CharSequence _htmlCode = this.toHtmlCode(factTypeDiagram, diagram);
         _builder.append(_htmlCode, "	");
         _builder.newLineIfNotEmpty();
       }
     }
     _builder.append("</body>");
     _builder.newLine();
     _builder.append("</html>");
     _builder.newLine();
     return _builder;
   }
   
   public CharSequence toHtmlCode(final FactTypeDiagram factTypeDiagram, final Diagram diagram) {
     StringConcatenation _builder = new StringConcatenation();
     _builder.append("<table class=\"column-data\">");
     _builder.newLine();
     _builder.append("\t");
     _builder.append("<tbody class=\"rules\">");
     _builder.newLine();
     {
       EList<AbstractRule> _rules = factTypeDiagram.getRules();
       for(final AbstractRule abstractRule : _rules) {
         _builder.append("\t");
         _builder.append("<tr><!-- Rules first row: name and image-->");
         _builder.newLine();
         _builder.append("\t");
         _builder.append("\t");
         Switch _switch = new Switch();
         Switch isSecondColumn = _switch;
         _builder.newLineIfNotEmpty();
         {
           EList<Column> _columns = factTypeDiagram.getColumns();
           for(final Column column : _columns) {
             {
               boolean _and = false;
               EList<Column> _columns_1 = abstractRule.getColumns();
               boolean _contains = _columns_1.contains(column);
               if (!_contains) {
                 _and = false;
               } else {
                 boolean _value = isSecondColumn.getValue();
                 boolean _not = (!_value);
                 _and = (_contains && _not);
               }
               if (_and) {
                 _builder.append("\t");
                 _builder.append("\t");
                 _builder.append("<td>");
                 _builder.newLine();
                 _builder.append("\t");
                 _builder.append("\t");
                 _builder.append("\t");
                 _builder.append("<span id=\"");
                 String _name = abstractRule.getName();
                 _builder.append(_name, "			");
                 _builder.append("\">");
                 _builder.newLineIfNotEmpty();
                 _builder.append("\t");
                 _builder.append("\t");
                 _builder.append("\t\t");
                 String _shortCode = this.getShortCode(abstractRule);
                 _builder.append(_shortCode, "				");
                 _builder.append("(");
                 String _name_1 = abstractRule.getName();
                 _builder.append(_name_1, "				");
                 _builder.append(")");
                 _builder.newLineIfNotEmpty();
                 _builder.append("\t");
                 _builder.append("\t");
                 _builder.append("\t\t");
                 String _icon = this.getIcon(abstractRule);
                 _builder.append(_icon, "				");
                 _builder.newLineIfNotEmpty();
                 _builder.append("\t");
                 _builder.append("\t");
                 _builder.append("\t\t");
                 CharSequence _htmlReferences = this.htmlReferences(abstractRule);
                 _builder.append(_htmlReferences, "				");
                 _builder.newLineIfNotEmpty();
                 _builder.append("\t");
                 _builder.append("\t");
                 _builder.append("\t");
                 _builder.append("</span>");
                 _builder.newLine();
                 _builder.append("\t");
                 _builder.append("\t");
                 _builder.append("</td>");
                 isSecondColumn.setTrue();
                 _builder.newLineIfNotEmpty();
               } else {
                 _builder.append("\t");
                 _builder.append("\t");
                 _builder.append("<td></td>");
                 _builder.newLine();
               }
             }
           }
         }
         _builder.append("\t");
         _builder.append("</tr>");
         _builder.newLine();
         _builder.append("\t");
         _builder.append("<tr><!-- Rules second row: draw line above columns-->");
         _builder.newLine();
         _builder.append("\t");
         _builder.append("\t");
         Counter _counter = new Counter();
         Counter ruleColumnCounter = _counter;
         _builder.newLineIfNotEmpty();
         {
           EList<Column> _columns_2 = factTypeDiagram.getColumns();
           for(final Column column_1 : _columns_2) {
             {
               EList<Column> _columns_3 = abstractRule.getColumns();
               boolean _contains_1 = _columns_3.contains(column_1);
               if (_contains_1) {
                 _builder.append("\t");
                 _builder.append("\t");
                 ruleColumnCounter.increase();
                 _builder.newLineIfNotEmpty();
                 {
                   boolean _and_1 = false;
                   int _value_1 = ruleColumnCounter.getValue();
                   boolean _equals = (_value_1 == 1);
                   if (!_equals) {
                     _and_1 = false;
                   } else {
                     EList<Column> _columns_4 = abstractRule.getColumns();
                     int _size = _columns_4.size();
                     boolean _equals_1 = (_size == 1);
                     _and_1 = (_equals && _equals_1);
                   }
                   if (_and_1) {
                     _builder.append("\t");
                     _builder.append("\t");
                     _builder.append("<td class=\"line line-single\"><span><span></span></span></td>");
                     _builder.newLine();
                   } else {
                     boolean _and_2 = false;
                     int _value_2 = ruleColumnCounter.getValue();
                     boolean _equals_2 = (_value_2 == 1);
                     if (!_equals_2) {
                       _and_2 = false;
                     } else {
                       EList<Column> _columns_5 = abstractRule.getColumns();
                       int _size_1 = _columns_5.size();
                       boolean _greaterThan = (_size_1 > 1);
                       _and_2 = (_equals_2 && _greaterThan);
                     }
                     if (_and_2) {
                       _builder.append("\t");
                       _builder.append("\t");
                       _builder.append("<td class=\"line line-left\"><span><span></span></span></td>");
                       _builder.newLine();
                     } else {
                       int _value_3 = ruleColumnCounter.getValue();
                       EList<Column> _columns_6 = abstractRule.getColumns();
                       int _size_2 = _columns_6.size();
                       boolean _equals_3 = (_value_3 == _size_2);
                       if (_equals_3) {
                         _builder.append("\t");
                         _builder.append("\t");
                         _builder.append("<td class=\"line line-right\"><span><span></span></span></td>");
                         _builder.newLine();
                       } else {
                         _builder.append("\t");
                         _builder.append("\t");
                         _builder.append("<td class=\"line line-center\"><span><span></span></span></td>");
                         _builder.newLine();
                       }
                     }
                   }
                 }
               } else {
                 {
                   boolean _and_3 = false;
                   int _value_4 = ruleColumnCounter.getValue();
                   boolean _greaterThan_1 = (_value_4 > 0);
                   if (!_greaterThan_1) {
                     _and_3 = false;
                   } else {
                     int _value_5 = ruleColumnCounter.getValue();
                     EList<Column> _columns_7 = abstractRule.getColumns();
                     int _size_3 = _columns_7.size();
                     boolean _lessThan = (_value_5 < _size_3);
                     _and_3 = (_greaterThan_1 && _lessThan);
                   }
                   if (_and_3) {
                     _builder.append("\t");
                     _builder.append("\t");
                     _builder.append("<td class=\"line line\"><span><span></span></span></td>");
                     _builder.newLine();
                   } else {
                     _builder.append("\t");
                     _builder.append("\t");
                     _builder.append("<td></td>");
                     _builder.newLine();
                   }
                 }
               }
             }
           }
         }
         _builder.append("\t");
         _builder.append("</tr>");
         _builder.newLine();
       }
     }
     _builder.append("\t");
     _builder.append("</tbody>");
     _builder.newLine();
     _builder.newLine();
     _builder.append("\t");
     _builder.append("<!-- Unique constraints -->");
     _builder.newLine();
     _builder.append("\t");
     _builder.append("<tbody class=\"keys\">");
     _builder.newLine();
     {
       EList<AlternativeKey> _alternativeKeys = factTypeDiagram.getAlternativeKeys();
       for(final AlternativeKey uc : _alternativeKeys) {
         _builder.append("\t");
         _builder.append("<tr><!-- First row: name -->");
         _builder.newLine();
         _builder.append("\t");
         _builder.append("\t");
         Switch _switch_1 = new Switch();
         Switch isSecondColumn_1 = _switch_1;
         _builder.newLineIfNotEmpty();
         {
           EList<Column> _columns_8 = factTypeDiagram.getColumns();
           for(final Column column_2 : _columns_8) {
             {
               boolean _and_4 = false;
               EList<Column> _columns_9 = uc.getColumns();
               boolean _contains_2 = _columns_9.contains(column_2);
               if (!_contains_2) {
                 _and_4 = false;
               } else {
                 boolean _value_6 = isSecondColumn_1.getValue();
                 boolean _not_1 = (!_value_6);
                 _and_4 = (_contains_2 && _not_1);
               }
               if (_and_4) {
                 _builder.append("\t");
                 _builder.append("\t");
                 _builder.append("<td style=\"text-align:center;\">");
                 _builder.newLine();
                 _builder.append("\t");
                 _builder.append("\t");
                 _builder.append("\t");
                 _builder.append("<span id=\"");
                 String _name_2 = uc.getName();
                 _builder.append(_name_2, "			");
                 _builder.append("\">alternative key (");
                 String _name_3 = uc.getName();
                 _builder.append(_name_3, "			");
                 _builder.append(")</span></td>");
                 _builder.newLineIfNotEmpty();
                 _builder.append("\t");
                 _builder.append("\t");
                 _builder.append("\t");
                 isSecondColumn_1.setTrue();
                 _builder.newLineIfNotEmpty();
               } else {
                 _builder.append("\t");
                 _builder.append("\t");
                 _builder.append("<td></td>");
                 _builder.newLine();
               }
             }
           }
         }
         _builder.append("\t");
         _builder.append("</tr>");
         _builder.newLine();
         _builder.append("\t");
         _builder.append("<tr>");
         _builder.newLine();
         _builder.append("\t");
         _builder.append("\t");
         Counter _counter_1 = new Counter();
         Counter ruleColumnCounter_1 = _counter_1;
         _builder.newLineIfNotEmpty();
         {
           EList<Column> _columns_10 = factTypeDiagram.getColumns();
           for(final Column column_3 : _columns_10) {
             {
               EList<Column> _columns_11 = uc.getColumns();
               boolean _contains_3 = _columns_11.contains(column_3);
               if (_contains_3) {
                 _builder.append("\t");
                 _builder.append("\t");
                 ruleColumnCounter_1.increase();
                 _builder.newLineIfNotEmpty();
                 {
                   boolean _and_5 = false;
                   int _value_7 = ruleColumnCounter_1.getValue();
                   boolean _equals_4 = (_value_7 == 1);
                   if (!_equals_4) {
                     _and_5 = false;
                   } else {
                     EList<Column> _columns_12 = uc.getColumns();
                     int _size_4 = _columns_12.size();
                     boolean _equals_5 = (_size_4 == 1);
                     _and_5 = (_equals_4 && _equals_5);
                   }
                   if (_and_5) {
                     _builder.append("\t");
                     _builder.append("\t");
                     _builder.append("<td class=\"line line-arrow-dotted\"><span><span></span></span></td>");
                     _builder.newLine();
                   } else {
                     boolean _and_6 = false;
                     int _value_8 = ruleColumnCounter_1.getValue();
                     boolean _equals_6 = (_value_8 == 1);
                     if (!_equals_6) {
                       _and_6 = false;
                     } else {
                       EList<Column> _columns_13 = uc.getColumns();
                       int _size_5 = _columns_13.size();
                       boolean _greaterThan_2 = (_size_5 > 1);
                       _and_6 = (_equals_6 && _greaterThan_2);
                     }
                     if (_and_6) {
                       _builder.append("\t");
                       _builder.append("\t");
                       _builder.append("<td class=\"line line-left-arrow-dotted\"><span><span></span></span></td>");
                       _builder.newLine();
                     } else {
                       int _value_9 = ruleColumnCounter_1.getValue();
                       EList<Column> _columns_14 = uc.getColumns();
                       int _size_6 = _columns_14.size();
                       boolean _equals_7 = (_value_9 == _size_6);
                       if (_equals_7) {
                         _builder.append("\t");
                         _builder.append("\t");
                         _builder.append("<td class=\"line line-right-arrow-dotted\"><span><span></span></span></td>");
                         _builder.newLine();
                       } else {
                         _builder.append("\t");
                         _builder.append("\t");
                         _builder.append("<td class=\"line line-dotted\"><span><span></span></span></td>");
                         _builder.newLine();
                       }
                     }
                   }
                 }
               } else {
                 _builder.append("\t");
                 _builder.append("\t");
                 _builder.append("<td></td>");
                 _builder.newLine();
               }
             }
           }
         }
         _builder.append("\t");
         _builder.append("</tr>");
         _builder.newLine();
       }
     }
     _builder.append("\t");
     _builder.newLine();
     {
       PrimaryKey _primaryKey = factTypeDiagram.getPrimaryKey();
       boolean _notEquals = (!Objects.equal(_primaryKey, null));
       if (_notEquals) {
         _builder.append("\t");
         _builder.append("<!-- Primary key -->");
         _builder.newLine();
         _builder.append("\t");
         _builder.append("<tr><!-- First row: name -->");
         _builder.newLine();
         _builder.append("\t");
         _builder.append("\t");
         Switch _switch_2 = new Switch();
         Switch isSecondColumn_2 = _switch_2;
         _builder.newLineIfNotEmpty();
         {
           EList<Column> _columns_15 = factTypeDiagram.getColumns();
           for(final Column column_4 : _columns_15) {
             {
               boolean _and_7 = false;
               PrimaryKey _primaryKey_1 = factTypeDiagram.getPrimaryKey();
               EList<Column> _columns_16 = _primaryKey_1.getColumns();
               boolean _contains_4 = _columns_16.contains(column_4);
               if (!_contains_4) {
                 _and_7 = false;
               } else {
                 boolean _value_10 = isSecondColumn_2.getValue();
                 boolean _not_2 = (!_value_10);
                 _and_7 = (_contains_4 && _not_2);
               }
               if (_and_7) {
                 _builder.append("\t");
                 _builder.append("\t");
                 _builder.append("<td style=\"text-align:center;\">");
                 _builder.newLine();
                 _builder.append("\t");
                 _builder.append("\t");
                 _builder.append("\t");
                 _builder.append("<span id=\"");
                 PrimaryKey _primaryKey_2 = factTypeDiagram.getPrimaryKey();
                 String _name_4 = _primaryKey_2.getName();
                 _builder.append(_name_4, "			");
                 _builder.append("\">primary key (");
                 PrimaryKey _primaryKey_3 = factTypeDiagram.getPrimaryKey();
                 String _name_5 = _primaryKey_3.getName();
                 _builder.append(_name_5, "			");
                 _builder.append(")</span></td>");
                 _builder.newLineIfNotEmpty();
                 _builder.append("\t");
                 _builder.append("\t");
                 _builder.append("\t");
                 isSecondColumn_2.setTrue();
                 _builder.newLineIfNotEmpty();
               } else {
                 _builder.append("\t");
                 _builder.append("\t");
                 _builder.append("<td></td>");
                 _builder.newLine();
               }
             }
           }
         }
         _builder.append("\t");
         _builder.append("</tr>");
         _builder.newLine();
         _builder.append("\t");
         _builder.append("<tr>");
         _builder.newLine();
         _builder.append("\t");
         _builder.append("\t");
         Counter _counter_2 = new Counter();
         Counter ruleColumnCounter_2 = _counter_2;
         _builder.newLineIfNotEmpty();
         {
           EList<Column> _columns_17 = factTypeDiagram.getColumns();
           for(final Column column_5 : _columns_17) {
             {
               PrimaryKey _primaryKey_4 = factTypeDiagram.getPrimaryKey();
               EList<Column> _columns_18 = _primaryKey_4.getColumns();
               boolean _contains_5 = _columns_18.contains(column_5);
               if (_contains_5) {
                 _builder.append("\t");
                 _builder.append("\t");
                 ruleColumnCounter_2.increase();
                 _builder.newLineIfNotEmpty();
                 {
                   boolean _and_8 = false;
                   int _value_11 = ruleColumnCounter_2.getValue();
                   boolean _equals_8 = (_value_11 == 1);
                   if (!_equals_8) {
                     _and_8 = false;
                   } else {
                     PrimaryKey _primaryKey_5 = factTypeDiagram.getPrimaryKey();
                     EList<Column> _columns_19 = _primaryKey_5.getColumns();
                     int _size_7 = _columns_19.size();
                     boolean _equals_9 = (_size_7 == 1);
                     _and_8 = (_equals_8 && _equals_9);
                   }
                   if (_and_8) {
                     _builder.append("\t");
                     _builder.append("\t");
                     _builder.append("<td class=\"line line-arrow\"><span><span></span></span></td>");
                     _builder.newLine();
                   } else {
                     boolean _and_9 = false;
                     int _value_12 = ruleColumnCounter_2.getValue();
                     boolean _equals_10 = (_value_12 == 1);
                     if (!_equals_10) {
                       _and_9 = false;
                     } else {
                       PrimaryKey _primaryKey_6 = factTypeDiagram.getPrimaryKey();
                       EList<Column> _columns_20 = _primaryKey_6.getColumns();
                       int _size_8 = _columns_20.size();
                       boolean _greaterThan_3 = (_size_8 > 1);
                       _and_9 = (_equals_10 && _greaterThan_3);
                     }
                     if (_and_9) {
                       _builder.append("\t");
                       _builder.append("\t");
                       _builder.append("<td class=\"line line-left-arrow\"><span><span></span></span></td>");
                       _builder.newLine();
                     } else {
                       int _value_13 = ruleColumnCounter_2.getValue();
                       PrimaryKey _primaryKey_7 = factTypeDiagram.getPrimaryKey();
                       EList<Column> _columns_21 = _primaryKey_7.getColumns();
                       int _size_9 = _columns_21.size();
                       boolean _equals_11 = (_value_13 == _size_9);
                       if (_equals_11) {
                         _builder.append("\t");
                         _builder.append("\t");
                         _builder.append("<td class=\"line line-right-arrow\"><span><span></span></span></td>");
                         _builder.newLine();
                       } else {
                         _builder.append("\t");
                         _builder.append("\t");
                         _builder.append("<td class=\"line line\"><span><span></span></span></td>");
                         _builder.newLine();
                       }
                     }
                   }
                 }
               } else {
                 _builder.append("\t");
                 _builder.append("\t");
                 _builder.append("<td></td>");
                 _builder.newLine();
               }
             }
           }
         }
         _builder.append("\t");
         _builder.append("</tr>");
         _builder.newLine();
       }
     }
     _builder.append("\t");
     _builder.append("<tr><!-- Empty row -->");
     _builder.newLine();
     {
       EList<Column> _columns_22 = factTypeDiagram.getColumns();
       for(final Column column_6 : _columns_22) {
         _builder.append("\t\t");
         _builder.append("<td>&nbsp;</td>");
         _builder.newLine();
       }
     }
     _builder.append("\t");
     _builder.append("</tr>");
     _builder.newLine();
     _builder.append("\t");
     _builder.append("</tbody>");
     _builder.newLine();
     _builder.append("  ");
     _builder.newLine();
     _builder.append("\t");
     _builder.append("<tbody class=\"data\">");
     _builder.newLine();
     _builder.append("\t");
     _builder.append("<tr><!-- Column names, optional or required -->");
     _builder.newLine();
     {
       EList<Column> _columns_23 = factTypeDiagram.getColumns();
       for(final Column column_7 : _columns_23) {
         {
           PrimaryKey _primaryKey_8 = factTypeDiagram.getPrimaryKey();
           EList<Column> _columns_24 = _primaryKey_8.getColumns();
           boolean _contains_6 = _columns_24.contains(column_7);
           if (_contains_6) {
             {
               Derived _derived = column_7.getDerived();
               boolean _equals_12 = Derived.SOMETIMES.equals(_derived);
               if (_equals_12) {
                 _builder.append("\t\t");
                 _builder.append("<th class=\"sometimesderived\">");
                 String _name_6 = column_7.getName();
                 _builder.append(_name_6, "		");
                 _builder.append("</th>");
                 _builder.newLineIfNotEmpty();
               } else {
                 Derived _derived_1 = column_7.getDerived();
                 boolean _equals_13 = Derived.ALWAYS.equals(_derived_1);
                 if (_equals_13) {
                   _builder.append("\t\t");
                   _builder.append("<th class=\"alwaysderived\">");
                   String _name_7 = column_7.getName();
                   _builder.append(_name_7, "		");
                   _builder.append("</th>");
                   _builder.newLineIfNotEmpty();
                 } else {
                   _builder.append("\t\t");
                   _builder.append("<!-- No \'required\' class, this is implicit for PrimaryKey columns -->");
                   _builder.newLine();
                   _builder.append("\t\t");
                   _builder.append("<th>");
                   String _name_8 = column_7.getName();
                   _builder.append(_name_8, "		");
                   _builder.append("</th>");
                   _builder.newLineIfNotEmpty();
                 }
               }
             }
           } else {
             {
               boolean _isNotEmpty = column_7.isNotEmpty();
               if (_isNotEmpty) {
                 {
                   Derived _derived_2 = column_7.getDerived();
                   boolean _equals_14 = Derived.SOMETIMES.equals(_derived_2);
                   if (_equals_14) {
                     _builder.append("\t\t");
                     _builder.append("<th class=\"requiredsometimesderived\">");
                     String _name_9 = column_7.getName();
                     _builder.append(_name_9, "		");
                     _builder.append("</th>");
                     _builder.newLineIfNotEmpty();
                   } else {
                     Derived _derived_3 = column_7.getDerived();
                     boolean _equals_15 = Derived.ALWAYS.equals(_derived_3);
                     if (_equals_15) {
                       _builder.append("\t\t");
                       _builder.append("<th class=\"requiredalwaysderived\">");
                       String _name_10 = column_7.getName();
                       _builder.append(_name_10, "		");
                       _builder.append("</th>");
                       _builder.newLineIfNotEmpty();
                     } else {
                       _builder.append("\t\t");
                       _builder.append("<th class=\"required\">");
                       String _name_11 = column_7.getName();
                       _builder.append(_name_11, "		");
                       _builder.append("</th>");
                       _builder.newLineIfNotEmpty();
                     }
                   }
                 }
               } else {
                 {
                   Derived _derived_4 = column_7.getDerived();
                   boolean _equals_16 = Derived.SOMETIMES.equals(_derived_4);
                   if (_equals_16) {
                     _builder.append("\t\t");
                     _builder.append("<th class=\"optionalsometimesderived\">");
                     String _name_12 = column_7.getName();
                     _builder.append(_name_12, "		");
                     _builder.append("</th>");
                     _builder.newLineIfNotEmpty();
                   } else {
                     Derived _derived_5 = column_7.getDerived();
                     boolean _equals_17 = Derived.ALWAYS.equals(_derived_5);
                     if (_equals_17) {
                       _builder.append("\t\t");
                       _builder.append("<th class=\"optionalalwaysderived\">");
                       String _name_13 = column_7.getName();
                       _builder.append(_name_13, "		");
                       _builder.append("</th>");
                       _builder.newLineIfNotEmpty();
                     } else {
                       _builder.append("\t\t");
                       _builder.append("<th class=\"optional\">");
                       String _name_14 = column_7.getName();
                       _builder.append(_name_14, "		");
                       _builder.append("</th>");
                       _builder.newLineIfNotEmpty();
                     }
                   }
                 }
               }
             }
           }
         }
       }
     }
     _builder.append("\t");
     _builder.append("</tr>");
     _builder.newLine();
     _builder.append("\t");
     _builder.append("<tr><!-- Column types -->");
     _builder.newLine();
     {
       EList<Column> _columns_25 = factTypeDiagram.getColumns();
       for(final Column column_8 : _columns_25) {
         _builder.append("\t\t");
         _builder.append("<td>");
         Type _type = column_8.getType();
         String _name_15 = _type.getName();
         _builder.append(_name_15, "		");
         _builder.append(" (");
         Type _type_1 = column_8.getType();
         int _length = _type_1.getLength();
         _builder.append(_length, "		");
         _builder.append(", ");
         Type _type_2 = column_8.getType();
         int _precision = _type_2.getPrecision();
         _builder.append(_precision, "		");
         _builder.append(")</td>");
         _builder.newLineIfNotEmpty();
       }
     }
     _builder.append("\t");
     _builder.append("</tr>");
     _builder.newLine();
     _builder.append("\t");
     _builder.newLine();
     _builder.append("\t");
     _builder.append("<!-- Example rows-->");
     _builder.newLine();
     {
       EList<Column> _columns_26 = factTypeDiagram.getColumns();
       Column _get = _columns_26.get(0);
       EList<Value> _values = _get.getValues();
       for(final Value value : _values) {
         _builder.append("\t");
         _builder.append("<tr>");
         _builder.newLine();
         {
           EList<Column> _columns_27 = factTypeDiagram.getColumns();
           for(final Column column_9 : _columns_27) {
             _builder.append("\t");
             _builder.append("\t");
             _builder.append("<td>");
             EList<Value> _values_1 = column_9.getValues();
             EList<Column> _columns_28 = factTypeDiagram.getColumns();
             Column _get_1 = _columns_28.get(0);
             EList<Value> _values_2 = _get_1.getValues();
             int _indexOf = _values_2.indexOf(value);
             Value _get_2 = _values_1.get(_indexOf);
             String _value_14 = _get_2.getValue();
             _builder.append(_value_14, "		");
             _builder.append("</td>");
             _builder.newLineIfNotEmpty();
           }
         }
         _builder.append("\t");
         _builder.append("</tr>");
         _builder.newLine();
       }
     }
     _builder.append("\t");
     _builder.append("</tbody>");
     _builder.newLine();
     _builder.append("</table>");
     _builder.newLine();
     _builder.newLine();
     _builder.append("<!-- Sentences and examples -->");
     _builder.newLine();
     _builder.append("<div class=\"list\">");
     _builder.newLine();
     _builder.append("<h2>ZS</h2>");
     _builder.newLine();
     _builder.append("<ol>");
     _builder.newLine();
     {
       EList<SentenceTemplate> _sentenceTemplates = factTypeDiagram.getSentenceTemplates();
       for(final SentenceTemplate sentenceTemplate : _sentenceTemplates) {
         _builder.append("\t");
         _builder.append("<li>");
         String _constructCompleteSentenceWithColumnNames = this.constructCompleteSentenceWithColumnNames(sentenceTemplate);
         _builder.append(_constructCompleteSentenceWithColumnNames, "	");
         _builder.append("</li>");
         _builder.newLineIfNotEmpty();
       }
     }
     _builder.append("</ol>");
     _builder.newLine();
     _builder.append("</div>");
     _builder.newLine();
     _builder.newLine();
     _builder.append("<div class=\"list\">");
     _builder.newLine();
     _builder.append("\t");
     _builder.append("<h2>ZS+POP</h2>");
     _builder.newLine();
     _builder.append("\t");
     _builder.append("<!-- Order example data as described in https://github.com/escay/Facttype/issues/5 -->");
     _builder.newLine();
     {
       EList<Column> _columns_29 = factTypeDiagram.getColumns();
       Column _get_3 = _columns_29.get(0);
       EList<Value> _values_3 = _get_3.getValues();
       for(final Value value_1 : _values_3) {
         _builder.append("\t");
         _builder.append("<ol>");
         _builder.newLine();
         {
           EList<SentenceTemplate> _sentenceTemplates_1 = factTypeDiagram.getSentenceTemplates();
           for(final SentenceTemplate sentenceTemplate_1 : _sentenceTemplates_1) {
             _builder.append("\t");
             EList<Column> _columns_30 = factTypeDiagram.getColumns();
             Column _get_4 = _columns_30.get(0);
             EList<Value> _values_4 = _get_4.getValues();
             int columnIndex = _values_4.indexOf(value_1);
             _builder.newLineIfNotEmpty();
             _builder.append("\t");
             _builder.append("\t");
             _builder.append("<li>");
             CharSequence _constructCompleteSentenceWithExampleValues = this.constructCompleteSentenceWithExampleValues(sentenceTemplate_1, columnIndex);
             _builder.append(_constructCompleteSentenceWithExampleValues, "		");
             _builder.append("</li>");
             _builder.newLineIfNotEmpty();
           }
         }
         _builder.append("\t");
         _builder.append("</ol>");
         _builder.newLine();
       }
     }
     _builder.append("</div>");
     _builder.newLine();
     _builder.newLine();
     _builder.append("<!-- Rule descriptions -->");
     _builder.newLine();
     _builder.append("<table class=\"rule-descriptions\">");
     _builder.newLine();
     _builder.append("\t");
     Counter _counter_3 = new Counter();
     Counter ruleDescriptionCounter = _counter_3;
     _builder.newLineIfNotEmpty();
     {
       EList<AbstractRule> _rules_1 = factTypeDiagram.getRules();
       for(final AbstractRule abstractRule_1 : _rules_1) {
         {
           boolean _or = false;
           boolean _or_1 = false;
           boolean _or_2 = false;
           boolean _or_3 = false;
           if ((abstractRule_1 instanceof GeneralConstraint)) {
             _or_3 = true;
           } else {
             _or_3 = ((abstractRule_1 instanceof GeneralConstraint) || 
               (abstractRule_1 instanceof ValueRule));
           }
           if (_or_3) {
             _or_2 = true;
           } else {
             _or_2 = (_or_3 || 
               (abstractRule_1 instanceof DerivationRule));
           }
           if (_or_2) {
             _or_1 = true;
           } else {
             _or_1 = (_or_2 || 
               (abstractRule_1 instanceof OccurrenceFrequencyRule));
           }
           if (_or_1) {
             _or = true;
           } else {
             _or = (_or_1 || 
               (abstractRule_1 instanceof EventRule));
           }
           if (_or) {
             _builder.append("\t");
             ruleDescriptionCounter.increase();
             _builder.newLineIfNotEmpty();
             {
               int _value_15 = ruleDescriptionCounter.getValue();
               boolean _equals_18 = (_value_15 == 1);
               if (_equals_18) {
                 _builder.append("\t");
                 _builder.append("<tr>");
                 _builder.newLine();
                 _builder.append("\t");
                 _builder.append("\t");
                 _builder.append("<th></th>");
                 _builder.newLine();
                 _builder.append("\t");
                 _builder.append("\t");
                 _builder.append("<th>Rule</th>");
                 _builder.newLine();
                 _builder.append("\t");
                 _builder.append("\t");
                 _builder.append("<th>Description</th>");
                 _builder.newLine();
                 _builder.append("\t");
                 _builder.append("</tr>");
                 _builder.newLine();
               }
             }
             _builder.append("\t");
             CharSequence _ruleDescription = this.ruleDescription(abstractRule_1);
             _builder.append(_ruleDescription, "	");
             _builder.newLineIfNotEmpty();
           }
         }
       }
     }
     _builder.append("</table>");
     _builder.newLine();
     _builder.newLine();
     _builder.append("<!-- legend -->");
     _builder.newLine();
     _builder.append("<ul class=\"legend\">");
     _builder.newLine();
     _builder.append("\t");
     _builder.append("<li><img src=\"");
     String _toRootDirectory = this.getToRootDirectory(diagram);
     _builder.append(_toRootDirectory, "	");
     _builder.append("../img/icon-derivation.png\" alt=\"Icon\"/> derivation</li>");
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     _builder.append("<li><img src=\"");
     String _toRootDirectory_1 = this.getToRootDirectory(diagram);
     _builder.append(_toRootDirectory_1, "	");
     _builder.append("../img/icon-equality.png\" alt=\"Icon\"/> (partial) equality</li>");
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     _builder.append("<li><img src=\"");
     String _toRootDirectory_2 = this.getToRootDirectory(diagram);
     _builder.append(_toRootDirectory_2, "	");
     _builder.append("../img/icon-event.png\" alt=\"Icon\"/> event</li>");
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     _builder.append("<li><img src=\"");
     String _toRootDirectory_3 = this.getToRootDirectory(diagram);
     _builder.append(_toRootDirectory_3, "	");
     _builder.append("../img/icon-exclusion.png\" alt=\"Icon\"/> exclusion</li>");
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     _builder.append("<li><img src=\"");
     String _toRootDirectory_4 = this.getToRootDirectory(diagram);
     _builder.append(_toRootDirectory_4, "	");
     _builder.append("../img/icon-generalconstraint.png\" alt=\"Icon\"/> general constraint</li>");
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     _builder.append("<li><img src=\"");
     String _toRootDirectory_5 = this.getToRootDirectory(diagram);
     _builder.append(_toRootDirectory_5, "	");
     _builder.append("../img/icon-nooverlapping.png\" alt=\"Icon\"/> no overlapping</li>");
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     _builder.append("<li><img src=\"");
     String _toRootDirectory_6 = this.getToRootDirectory(diagram);
     _builder.append(_toRootDirectory_6, "	");
     _builder.append("../img/icon-occurrencefrequency.png\" alt=\"Icon\"/> occurrence frequency</li>");
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     _builder.append("<li><img src=\"");
     String _toRootDirectory_7 = this.getToRootDirectory(diagram);
     _builder.append(_toRootDirectory_7, "	");
     _builder.append("../img/icon-subset-super.png\" alt=\"Icon\"/> super set</li>");
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     _builder.append("<li><img src=\"");
     String _toRootDirectory_8 = this.getToRootDirectory(diagram);
     _builder.append(_toRootDirectory_8, "	");
     _builder.append("../img/icon-subset-sub.png\" alt=\"Icon\"/> sub set</li>");
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     _builder.append("<li><img src=\"");
     String _toRootDirectory_9 = this.getToRootDirectory(diagram);
     _builder.append(_toRootDirectory_9, "	");
     _builder.append("../img/icon-value.png\" alt=\"Icon\"/> value</li>");
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     _builder.append("<li><img src=\"");
     String _toRootDirectory_10 = this.getToRootDirectory(diagram);
     _builder.append(_toRootDirectory_10, "	");
     _builder.append("../img/icon-required-legend.png\" alt=\"Icon\"/> required</li>");
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     _builder.append("<li><img src=\"");
     String _toRootDirectory_11 = this.getToRootDirectory(diagram);
     _builder.append(_toRootDirectory_11, "	");
     _builder.append("../img/icon-optional-legend.png\" alt=\"Icon\"/> optional</li>");
     _builder.newLineIfNotEmpty();
     _builder.append("</ul>");
     _builder.newLine();
     return _builder;
   }
   
   protected CharSequence _ruleDescription(final AbstractRule abstractRule) {
     StringConcatenation _builder = new StringConcatenation();
     _builder.append("<tr>");
     _builder.newLine();
     _builder.append("\t");
     _builder.append("<td>");
     String _icon = this.getIcon(abstractRule);
     _builder.append(_icon, "	");
     _builder.append("</td>");
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     _builder.append("<td><a href=\"#");
     String _name = abstractRule.getName();
     _builder.append(_name, "	");
     _builder.append("\">");
     String _name_1 = abstractRule.getName();
     _builder.append(_name_1, "	");
     _builder.append("</a></td>");
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     _builder.append("<td>");
     {
       Description _description = abstractRule.getDescription();
       boolean _notEquals = (!Objects.equal(_description, null));
       if (_notEquals) {
         Description _description_1 = abstractRule.getDescription();
         String _text = _description_1.getText();
         _builder.append(_text, "	");
       } else {
         _builder.append("(no description)");
       }
     }
     _builder.append("</td>");
     _builder.newLineIfNotEmpty();
     _builder.append("</tr>");
     _builder.newLine();
     return _builder;
   }
   
   protected CharSequence _ruleDescription(final EventRule eventRule) {
     StringConcatenation _builder = new StringConcatenation();
     _builder.append("<tr>");
     _builder.newLine();
     _builder.append("\t");
     _builder.append("<td>");
     String _icon = this.getIcon(eventRule);
     _builder.append(_icon, "	");
     _builder.append("</td>");
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     _builder.append("<td><a href=\"#");
     String _name = eventRule.getName();
     _builder.append(_name, "	");
     _builder.append("\">");
     String _name_1 = eventRule.getName();
     _builder.append(_name_1, "	");
     _builder.append("</a></td>");
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     _builder.append("<td>");
     _builder.newLine();
     _builder.append("\t\t");
     {
       Description _description = eventRule.getDescription();
       boolean _notEquals = (!Objects.equal(_description, null));
       if (_notEquals) {
         Description _description_1 = eventRule.getDescription();
         String _text = _description_1.getText();
         _builder.append(_text, "		");
       } else {
         _builder.append("(no description)");
       }
     }
     _builder.newLineIfNotEmpty();
     _builder.append("\t\t");
     String _name_2 = eventRule.getName();
     _builder.append(_name_2, "		");
     _builder.append(" is triggered <em>");
     AfterBefore _afterBefore = eventRule.getAfterBefore();
     _builder.append(_afterBefore, "		");
     _builder.append("</em>:");
     _builder.newLineIfNotEmpty();
     {
       EList<Crud> _crud = eventRule.getCrud();
       boolean _hasElements = false;
       for(final Crud crud : _crud) {
         if (!_hasElements) {
           _hasElements = true;
           _builder.append(" ", "		");
         } else {
           _builder.appendImmediate(",", "		");
         }
         _builder.append("\t\t");
         _builder.append(crud, "		");
         _builder.newLineIfNotEmpty();
       }
     }
     _builder.append("\t");
     _builder.append("</td>");
     _builder.newLine();
     _builder.append("</tr>");
     _builder.newLine();
     return _builder;
   }
   
   protected CharSequence _ruleDescription(final OccurrenceFrequencyRule occurrenceFrequencyRule) {
     StringConcatenation _builder = new StringConcatenation();
     _builder.append("<tr>");
     _builder.newLine();
     _builder.append("\t");
     _builder.append("<td>");
     String _icon = this.getIcon(occurrenceFrequencyRule);
     _builder.append(_icon, "	");
     _builder.append("</td>");
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     _builder.append("<td><a href=\"#");
     String _name = occurrenceFrequencyRule.getName();
     _builder.append(_name, "	");
     _builder.append("\">");
     String _name_1 = occurrenceFrequencyRule.getName();
     _builder.append(_name_1, "	");
     _builder.append("</a></td>");
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     _builder.append("<td>");
     _builder.newLine();
     _builder.append("\t\t");
     {
       Description _description = occurrenceFrequencyRule.getDescription();
       boolean _notEquals = (!Objects.equal(_description, null));
       if (_notEquals) {
         Description _description_1 = occurrenceFrequencyRule.getDescription();
         String _text = _description_1.getText();
         _builder.append(_text, "		");
       } else {
         _builder.append("(no description)");
       }
     }
     _builder.newLineIfNotEmpty();
     _builder.append("\t\t");
     {
       Minimum _minimum = occurrenceFrequencyRule.getMinimum();
       boolean _notEquals_1 = (!Objects.equal(_minimum, null));
       if (_notEquals_1) {
         _builder.append("Minimum: <em>");
         Minimum _minimum_1 = occurrenceFrequencyRule.getMinimum();
         int _value = _minimum_1.getValue();
         _builder.append(_value, "		");
         _builder.append("</em>");
       }
     }
     _builder.newLineIfNotEmpty();
     _builder.append("\t\t");
     {
       Maximum _maximum = occurrenceFrequencyRule.getMaximum();
       boolean _notEquals_2 = (!Objects.equal(_maximum, null));
       if (_notEquals_2) {
         _builder.append("Maximum: <em>");
         Maximum _maximum_1 = occurrenceFrequencyRule.getMaximum();
         int _value_1 = _maximum_1.getValue();
         _builder.append(_value_1, "		");
         _builder.append("</em>");
       }
     }
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     _builder.append("</td>");
     _builder.newLine();
     _builder.append("</tr>");
     _builder.newLine();
     return _builder;
   }
   
   protected CharSequence _ruleDescription(final ValueRule valueRule) {
     StringConcatenation _builder = new StringConcatenation();
     _builder.append("<tr>");
     _builder.newLine();
     _builder.append("\t");
     _builder.append("<td>");
     String _icon = this.getIcon(valueRule);
     _builder.append(_icon, "	");
     _builder.append("</td>");
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     _builder.append("<td><a href=\"#");
     String _name = valueRule.getName();
     _builder.append(_name, "	");
     _builder.append("\">");
     String _name_1 = valueRule.getName();
     _builder.append(_name_1, "	");
     _builder.append("</a></td>");
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     _builder.append("<td>");
     {
       Description _description = valueRule.getDescription();
       boolean _notEquals = (!Objects.equal(_description, null));
       if (_notEquals) {
         Description _description_1 = valueRule.getDescription();
         String _text = _description_1.getText();
         _builder.append(_text, "	");
       } else {
         _builder.append("(no description)");
       }
     }
     _builder.newLineIfNotEmpty();
     {
       EList<String> _value = valueRule.getValue();
       boolean _hasElements = false;
       for(final String value : _value) {
         if (!_hasElements) {
           _hasElements = true;
           _builder.append(" Permitted values:", "		");
         } else {
           _builder.appendImmediate(",", "		");
         }
         _builder.append("\t\t");
         _builder.append("<em>");
         byte[] _bytes = value.getBytes();
         String _string = new String(_bytes);
         _builder.append(_string, "		");
         _builder.append("</em>");
         _builder.newLineIfNotEmpty();
       }
     }
     _builder.append("\t");
     _builder.append("</td>");
     _builder.newLine();
     _builder.append("</tr>");
     _builder.newLine();
     return _builder;
   }
   
   public String htmlLink(final AbstractRule referencedRule, final AbstractRule originatingRule) {
     String _toRootDirectory = this.getToRootDirectory(originatingRule);
     String _plus = ("<a href=\"" + _toRootDirectory);
     EObject _eContainer = referencedRule.eContainer();
     EObject _eContainer_1 = _eContainer.eContainer();
     String _relativeFileName = this.getRelativeFileName(((Diagram) _eContainer_1));
     String _plus_1 = (_plus + _relativeFileName);
     String _plus_2 = (_plus_1 + 
       "#");
     String _name = referencedRule.getName();
     String _plus_3 = (_plus_2 + _name);
     String _plus_4 = (_plus_3 + "\">");
    String _shortCode = this.getShortCode(referencedRule);
    String _plus_5 = (_plus_4 + _shortCode);
    String _plus_6 = (_plus_5 + "(");
     String _name_1 = referencedRule.getName();
    String _plus_7 = (_plus_6 + _name_1);
    String _plus_8 = (_plus_7 + ")</a>");
    return _plus_8;
   }
   
   protected CharSequence _htmlReferences(final AbstractRule rule) {
     return null;
   }
   
   protected CharSequence _htmlReferences(final EventRule rule) {
     StringConcatenation _builder = new StringConcatenation();
     _builder.append("<br>triggers:");
     _builder.newLine();
     {
       EList<DerivationRule> _triggers = rule.getTriggers();
       boolean _hasElements = false;
       for(final DerivationRule trigger : _triggers) {
         if (!_hasElements) {
           _hasElements = true;
         } else {
           _builder.appendImmediate("&nbsp;", "");
         }
         String _htmlLink = this.htmlLink(trigger, rule);
         _builder.append(_htmlLink, "");
         _builder.newLineIfNotEmpty();
       }
     }
     return _builder;
   }
   
   protected CharSequence _htmlReferences(final SubsetRule rule) {
     StringConcatenation _builder = new StringConcatenation();
     _builder.append("<br>ref:");
     _builder.newLine();
     {
       EList<SubsetRule> _references = rule.getReferences();
       boolean _hasElements = false;
       for(final SubsetRule reference : _references) {
         if (!_hasElements) {
           _hasElements = true;
         } else {
           _builder.appendImmediate("&nbsp;", "");
         }
         String _htmlLink = this.htmlLink(reference, rule);
         _builder.append(_htmlLink, "");
         _builder.newLineIfNotEmpty();
       }
     }
     return _builder;
   }
   
   protected CharSequence _htmlReferences(final DerivationRule rule) {
     StringConcatenation _builder = new StringConcatenation();
     _builder.append("<br>ref:");
     _builder.newLine();
     {
       EList<EventRule> _references = rule.getReferences();
       boolean _hasElements = false;
       for(final EventRule reference : _references) {
         if (!_hasElements) {
           _hasElements = true;
         } else {
           _builder.appendImmediate("&nbsp;", "");
         }
         String _htmlLink = this.htmlLink(reference, rule);
         _builder.append(_htmlLink, "");
         _builder.newLineIfNotEmpty();
       }
     }
     return _builder;
   }
   
   protected CharSequence _htmlReferences(final EqualityRule rule) {
     StringConcatenation _builder = new StringConcatenation();
     _builder.append("<br>ref:");
     _builder.newLine();
     {
       EList<EqualityRule> _references = rule.getReferences();
       boolean _hasElements = false;
       for(final EqualityRule reference : _references) {
         if (!_hasElements) {
           _hasElements = true;
         } else {
           _builder.appendImmediate("&nbsp;", "");
         }
         String _htmlLink = this.htmlLink(reference, rule);
         _builder.append(_htmlLink, "");
         _builder.newLineIfNotEmpty();
       }
     }
     return _builder;
   }
   
   protected CharSequence _htmlReferences(final PartialEqualityRule rule) {
     StringConcatenation _builder = new StringConcatenation();
     _builder.append("<br>ref:");
     _builder.newLine();
     {
       EList<PartialEqualityRule> _references = rule.getReferences();
       boolean _hasElements = false;
       for(final PartialEqualityRule reference : _references) {
         if (!_hasElements) {
           _hasElements = true;
         } else {
           _builder.appendImmediate("&nbsp;", "");
         }
         String _htmlLink = this.htmlLink(reference, rule);
         _builder.append(_htmlLink, "");
         _builder.newLineIfNotEmpty();
       }
     }
     return _builder;
   }
   
   protected CharSequence _htmlReferences(final ExclusionRule rule) {
     StringConcatenation _builder = new StringConcatenation();
     _builder.append("<br>ref:");
     _builder.newLine();
     {
       EList<ExclusionRule> _references = rule.getReferences();
       boolean _hasElements = false;
       for(final ExclusionRule reference : _references) {
         if (!_hasElements) {
           _hasElements = true;
         } else {
           _builder.appendImmediate("&nbsp;", "");
         }
         String _htmlLink = this.htmlLink(reference, rule);
         _builder.append(_htmlLink, "");
         _builder.newLineIfNotEmpty();
       }
     }
     return _builder;
   }
   
   public String constructCompleteSentenceWithColumnNames(final SentenceTemplate sentenceTemplate) {
     String _xblockexpression = null;
     {
       String result = "";
       EList<Content> _contents = sentenceTemplate.getContents();
       for (final Content content : _contents) {
         {
           String _content = content.getContent();
           boolean _notEquals = (!Objects.equal(_content, null));
           if (_notEquals) {
             String _plus = (result + " ");
             String _content_1 = content.getContent();
             String _plus_1 = (_plus + _content_1);
             result = _plus_1;
           }
           Column _column = content.getColumn();
           boolean _notEquals_1 = (!Objects.equal(_column, null));
           if (_notEquals_1) {
             String _plus_2 = (result + " ");
             Column _column_1 = content.getColumn();
             String _columnNameInSentence = this.columnNameInSentence(_column_1);
             String _plus_3 = (_plus_2 + _columnNameInSentence);
             result = _plus_3;
           }
         }
       }
       String _contentEnd = sentenceTemplate.getContentEnd();
       boolean _notEquals = (!Objects.equal(_contentEnd, null));
       if (_notEquals) {
         String _plus = (result + " ");
         String _contentEnd_1 = sentenceTemplate.getContentEnd();
         String _plus_1 = (_plus + _contentEnd_1);
         result = _plus_1;
       }
       _xblockexpression = (result);
     }
     return _xblockexpression;
   }
   
   public String columnNameInSentence(final Column column) {
     String _name = column.getName();
     String _plus = ("<em>&lt;" + _name);
     String _plus_1 = (_plus + "&gt;</em>");
     return _plus_1;
   }
   
   public CharSequence constructCompleteSentenceWithExampleValues(final SentenceTemplate sentenceTemplate, final int index) {
     StringConcatenation _builder = new StringConcatenation();
     {
       EList<Content> _contents = sentenceTemplate.getContents();
       for(final Content content : _contents) {
         {
           String _content = content.getContent();
           boolean _notEquals = (!Objects.equal(_content, null));
           if (_notEquals) {
             String _content_1 = content.getContent();
             _builder.append(_content_1, "");
             _builder.newLineIfNotEmpty();
           }
         }
         {
           Column _column = content.getColumn();
           boolean _notEquals_1 = (!Objects.equal(_column, null));
           if (_notEquals_1) {
             Column _column_1 = content.getColumn();
             EList<Value> _values = _column_1.getValues();
             Value value = _values.get(index);
             _builder.newLineIfNotEmpty();
             _builder.append("<em>");
             String _value = value.getValue();
             _builder.append(_value, "");
             _builder.append("</em>");
             _builder.newLineIfNotEmpty();
           }
         }
       }
     }
     String _contentEnd = sentenceTemplate.getContentEnd();
     _builder.append(_contentEnd, "");
     _builder.newLineIfNotEmpty();
     return _builder;
   }
   
   /**
    * Shortcut name generation methods
    */
   protected String _getShortCode(final SubsetRule rule) {
     return "SS";
   }
   
   protected String _getShortCode(final EqualityRule rule) {
     return "EQ";
   }
   
   protected String _getShortCode(final PartialEqualityRule rule) {
     String _xifexpression = null;
     boolean _isExcluding = rule.isExcluding();
     if (_isExcluding) {
       _xifexpression = "PEX EQ";
     } else {
       _xifexpression = "P EQ";
     }
     return _xifexpression;
   }
   
   protected String _getShortCode(final OccurrenceFrequencyRule rule) {
     return "OF";
   }
   
   protected String _getShortCode(final NoOverlappingRule rule) {
     return "NO";
   }
   
   protected String _getShortCode(final ExclusionRule rule) {
     return "EX";
   }
   
   protected String _getShortCode(final GeneralConstraint rule) {
     return "GC";
   }
   
   protected String _getShortCode(final EventRule rule) {
     return "EV";
   }
   
   protected String _getShortCode(final DerivationRule rule) {
     return "DV";
   }
   
   protected String _getShortCode(final ValueRule rule) {
     return "VL";
   }
   
   /**
    * Get Icon html tag generation methods
    */
   protected String _getIcon(final SubsetRule rule) {
     String _xifexpression = null;
     boolean _isSuper = rule.isSuper();
     if (_isSuper) {
       String _toRootDirectory = this.getToRootDirectory(rule);
       String _plus = ("<img src=\"" + _toRootDirectory);
       String _plus_1 = (_plus + "../img/icon-subset-super.png\" alt=\"Icon\"/>");
       _xifexpression = _plus_1;
     } else {
       String _toRootDirectory_1 = this.getToRootDirectory(rule);
       String _plus_2 = ("<img src=\"" + _toRootDirectory_1);
       String _plus_3 = (_plus_2 + "../img/icon-subset-sub.png\" alt=\"Icon\"/>");
       _xifexpression = _plus_3;
     }
     return _xifexpression;
   }
   
   protected String _getIcon(final EqualityRule rule) {
     String _toRootDirectory = this.getToRootDirectory(rule);
     String _plus = ("<img src=\"" + _toRootDirectory);
     String _plus_1 = (_plus + "../img/icon-equality.png\" alt=\"Icon\"/>");
     return _plus_1;
   }
   
   protected String _getIcon(final PartialEqualityRule rule) {
     String _toRootDirectory = this.getToRootDirectory(rule);
     String _plus = ("<img src=\"" + _toRootDirectory);
     String _plus_1 = (_plus + "../img/icon-equality.png\" alt=\"Icon\"/>");
     return _plus_1;
   }
   
   protected String _getIcon(final OccurrenceFrequencyRule rule) {
     String _toRootDirectory = this.getToRootDirectory(rule);
     String _plus = ("<img src=\"" + _toRootDirectory);
     String _plus_1 = (_plus + "../img/icon-occurrencefrequency.png\" alt=\"Icon\"/>");
     return _plus_1;
   }
   
   protected String _getIcon(final NoOverlappingRule rule) {
     String _toRootDirectory = this.getToRootDirectory(rule);
     String _plus = ("<img src=\"" + _toRootDirectory);
     String _plus_1 = (_plus + "../img/icon-nooverlapping.png\" alt=\"Icon\"/>");
     return _plus_1;
   }
   
   protected String _getIcon(final ExclusionRule rule) {
     String _toRootDirectory = this.getToRootDirectory(rule);
     String _plus = ("<img src=\"" + _toRootDirectory);
     String _plus_1 = (_plus + "../img/icon-exclusion.png\" alt=\"Icon\"/>");
     return _plus_1;
   }
   
   protected String _getIcon(final GeneralConstraint rule) {
     String _toRootDirectory = this.getToRootDirectory(rule);
     String _plus = ("<img src=\"" + _toRootDirectory);
     String _plus_1 = (_plus + "../img/icon-generalconstraint.png\" alt=\"Icon\"/>");
     return _plus_1;
   }
   
   protected String _getIcon(final EventRule rule) {
     String _toRootDirectory = this.getToRootDirectory(rule);
     String _plus = ("<img src=\"" + _toRootDirectory);
     String _plus_1 = (_plus + "../img/icon-event.png\" alt=\"Icon\"/>");
     return _plus_1;
   }
   
   protected String _getIcon(final DerivationRule rule) {
     String _toRootDirectory = this.getToRootDirectory(rule);
     String _plus = ("<img src=\"" + _toRootDirectory);
     String _plus_1 = (_plus + "../img/icon-derivation.png\" alt=\"Icon\"/>");
     return _plus_1;
   }
   
   protected String _getIcon(final ValueRule rule) {
     String _toRootDirectory = this.getToRootDirectory(rule);
     String _plus = ("<img src=\"" + _toRootDirectory);
     String _plus_1 = (_plus + "../img/icon-value.png\" alt=\"Icon\"/>");
     return _plus_1;
   }
   
   public CharSequence ruleDescription(final AbstractRule eventRule) {
     if (eventRule instanceof EventRule) {
       return _ruleDescription((EventRule)eventRule);
     } else if (eventRule instanceof OccurrenceFrequencyRule) {
       return _ruleDescription((OccurrenceFrequencyRule)eventRule);
     } else if (eventRule instanceof ValueRule) {
       return _ruleDescription((ValueRule)eventRule);
     } else if (eventRule != null) {
       return _ruleDescription(eventRule);
     } else {
       throw new IllegalArgumentException("Unhandled parameter types: " +
         Arrays.<Object>asList(eventRule).toString());
     }
   }
   
   public CharSequence htmlReferences(final AbstractRule rule) {
     if (rule instanceof DerivationRule) {
       return _htmlReferences((DerivationRule)rule);
     } else if (rule instanceof EqualityRule) {
       return _htmlReferences((EqualityRule)rule);
     } else if (rule instanceof EventRule) {
       return _htmlReferences((EventRule)rule);
     } else if (rule instanceof ExclusionRule) {
       return _htmlReferences((ExclusionRule)rule);
     } else if (rule instanceof PartialEqualityRule) {
       return _htmlReferences((PartialEqualityRule)rule);
     } else if (rule instanceof SubsetRule) {
       return _htmlReferences((SubsetRule)rule);
     } else if (rule != null) {
       return _htmlReferences(rule);
     } else {
       throw new IllegalArgumentException("Unhandled parameter types: " +
         Arrays.<Object>asList(rule).toString());
     }
   }
   
   public String getShortCode(final AbstractRule rule) {
     if (rule instanceof DerivationRule) {
       return _getShortCode((DerivationRule)rule);
     } else if (rule instanceof EqualityRule) {
       return _getShortCode((EqualityRule)rule);
     } else if (rule instanceof EventRule) {
       return _getShortCode((EventRule)rule);
     } else if (rule instanceof ExclusionRule) {
       return _getShortCode((ExclusionRule)rule);
     } else if (rule instanceof GeneralConstraint) {
       return _getShortCode((GeneralConstraint)rule);
     } else if (rule instanceof NoOverlappingRule) {
       return _getShortCode((NoOverlappingRule)rule);
     } else if (rule instanceof OccurrenceFrequencyRule) {
       return _getShortCode((OccurrenceFrequencyRule)rule);
     } else if (rule instanceof PartialEqualityRule) {
       return _getShortCode((PartialEqualityRule)rule);
     } else if (rule instanceof SubsetRule) {
       return _getShortCode((SubsetRule)rule);
     } else if (rule instanceof ValueRule) {
       return _getShortCode((ValueRule)rule);
     } else {
       throw new IllegalArgumentException("Unhandled parameter types: " +
         Arrays.<Object>asList(rule).toString());
     }
   }
   
   public String getIcon(final AbstractRule rule) {
     if (rule instanceof DerivationRule) {
       return _getIcon((DerivationRule)rule);
     } else if (rule instanceof EqualityRule) {
       return _getIcon((EqualityRule)rule);
     } else if (rule instanceof EventRule) {
       return _getIcon((EventRule)rule);
     } else if (rule instanceof ExclusionRule) {
       return _getIcon((ExclusionRule)rule);
     } else if (rule instanceof GeneralConstraint) {
       return _getIcon((GeneralConstraint)rule);
     } else if (rule instanceof NoOverlappingRule) {
       return _getIcon((NoOverlappingRule)rule);
     } else if (rule instanceof OccurrenceFrequencyRule) {
       return _getIcon((OccurrenceFrequencyRule)rule);
     } else if (rule instanceof PartialEqualityRule) {
       return _getIcon((PartialEqualityRule)rule);
     } else if (rule instanceof SubsetRule) {
       return _getIcon((SubsetRule)rule);
     } else if (rule instanceof ValueRule) {
       return _getIcon((ValueRule)rule);
     } else {
       throw new IllegalArgumentException("Unhandled parameter types: " +
         Arrays.<Object>asList(rule).toString());
     }
   }
 }
