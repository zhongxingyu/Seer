 package org.facttype.generator;
 
 import java.util.List;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.xtext.generator.IFileSystemAccess;
 import org.eclipse.xtext.xbase.lib.BooleanExtensions;
 import org.eclipse.xtext.xbase.lib.ComparableExtensions;
 import org.eclipse.xtext.xbase.lib.Conversions;
 import org.eclipse.xtext.xbase.lib.Functions.Function1;
 import org.eclipse.xtext.xbase.lib.IntegerExtensions;
 import org.eclipse.xtext.xbase.lib.IterableExtensions;
 import org.eclipse.xtext.xbase.lib.ListExtensions;
 import org.eclipse.xtext.xbase.lib.ObjectExtensions;
 import org.eclipse.xtext.xbase.lib.StringExtensions;
 import org.eclipse.xtext.xtend2.lib.ResourceExtensions;
 import org.eclipse.xtext.xtend2.lib.StringConcatenation;
 import org.facttype.diagram.AbstractRule;
 import org.facttype.diagram.AfterBefore;
 import org.facttype.diagram.AlternativeKey;
 import org.facttype.diagram.Column;
 import org.facttype.diagram.Content;
 import org.facttype.diagram.Crud;
 import org.facttype.diagram.DerivationRule;
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
 import org.facttype.generator.Counter;
 import org.facttype.generator.DiagramComparator;
 import org.facttype.generator.IGenerator2;
 import org.facttype.generator.Switch;
 
 @SuppressWarnings("all")
 public class DiagramGenerator implements IGenerator2 {
   
   public void doGenerate(final ResourceSet rs, final IFileSystemAccess fsa) {
     {
       EList<Resource> _resources = rs.getResources();
       final Function1<Resource,Iterable<Diagram>> _function = new Function1<Resource,Iterable<Diagram>>() {
           public Iterable<Diagram> apply(final Resource r) {
             Iterable<EObject> _allContentsIterable = ResourceExtensions.allContentsIterable(r);
             Iterable<Diagram> _filter = IterableExtensions.<Diagram>filter(_allContentsIterable, org.facttype.diagram.Diagram.class);
             return _filter;
           }
         };
       List<Iterable<Diagram>> _map = ListExtensions.<Resource, Iterable<Diagram>>map(_resources, _function);
       Iterable<Diagram> _flatten = IterableExtensions.<Diagram>flatten(_map);
       final Iterable<Diagram> diagrams = _flatten;
       StringConcatenation _indexHtml = this.indexHtml(diagrams);
       fsa.generateFile("index.html", _indexHtml);
       for (Diagram diagram : diagrams) {
         String _relativeFileName = this.getRelativeFileName(diagram);
         Resource _eResource = diagram.eResource();
         EList<EObject> _contents = _eResource.getContents();
         EObject _head = IterableExtensions.<EObject>head(_contents);
         StringConcatenation _facttypeDiagramHtmlPage = this.facttypeDiagramHtmlPage(((Diagram) _head));
         fsa.generateFile(_relativeFileName, _facttypeDiagramHtmlPage);
       }
     }
   }
   
   public void doGenerate(final Resource resource, final IFileSystemAccess fsa) {
   }
   
   public StringConcatenation indexHtml(final Iterable<Diagram> diagrams) {
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
       for(Diagram diagram : _sort) {
         {
           EList<FactTypeDiagram> _factTypeDiagrams = diagram.getFactTypeDiagrams();
           for(FactTypeDiagram facttypeDiagram : _factTypeDiagrams) {
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
               boolean _operator_notEquals = ObjectExtensions.operator_notEquals(_description, null);
               if (_operator_notEquals) {
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
     {
       URI _uRI = res.getURI();
       String _lastSegment = _uRI.lastSegment();
       String name = _lastSegment;
       int _indexOf = name.indexOf(".");
       String _substring = name.substring(0, _indexOf);
       return _substring;
     }
   }
   
   public String getToRootDirectory(final AbstractRule rule) {
     EObject _eContainer = rule.eContainer();
     EObject _eContainer_1 = _eContainer.eContainer();
     String _rootDirectory = this.getToRootDirectory(((Diagram) _eContainer_1));
     return _rootDirectory;
   }
   
   public String getPackageNameAndFileName(final Diagram diagram, final String separator) {
     {
       Resource _eResource = diagram.eResource();
       URI _uRI = _eResource.getURI();
       String _path = _uRI.path();
       int _indexOf = _path.indexOf("/src");
       int index = _indexOf;
       String result = "";
       int _operator_minus = IntegerExtensions.operator_minus(1);
       boolean _operator_notEquals = ObjectExtensions.operator_notEquals(((Integer)index), ((Integer)_operator_minus));
       if (_operator_notEquals) {
         {
           int _operator_plus = IntegerExtensions.operator_plus(((Integer)index), ((Integer)5));
           index = _operator_plus;
           Resource _eResource_1 = diagram.eResource();
           URI _uRI_1 = _eResource_1.getURI();
           String _path_1 = _uRI_1.path();
           String _substring = _path_1.substring(index);
           String substring = _substring;
           String[] _split = substring.split("/");
           String[] results = _split;
           index = 0;
           final String[] typeConverted_results = (String[])results;
           int _size = ((List<String>)Conversions.doWrapArray(typeConverted_results)).size();
           boolean _operator_lessThan = ComparableExtensions.<Integer>operator_lessThan(((Integer)index), ((Integer)_size));
           Boolean _xwhileexpression = _operator_lessThan;
           while (_xwhileexpression) {
             {
               String _operator_plus_1 = StringExtensions.operator_plus(result, separator);
               final String[] typeConverted_results_1 = (String[])results;
               String _get = ((List<String>)Conversions.doWrapArray(typeConverted_results_1)).get(index);
               String _operator_plus_2 = StringExtensions.operator_plus(_operator_plus_1, _get);
               result = _operator_plus_2;
               int _operator_plus_3 = IntegerExtensions.operator_plus(((Integer)index), ((Integer)1));
               index = _operator_plus_3;
             }
             final String[] typeConverted_results_2 = (String[])results;
             int _size_1 = ((List<String>)Conversions.doWrapArray(typeConverted_results_2)).size();
             boolean _operator_lessThan_1 = ComparableExtensions.<Integer>operator_lessThan(((Integer)index), ((Integer)_size_1));
             _xwhileexpression = _operator_lessThan_1;
           }
         }
       }
       return result;
     }
   }
   
   public String getToRootDirectory(final Diagram diagram) {
     {
       Resource _eResource = diagram.eResource();
       URI _uRI = _eResource.getURI();
       String _path = _uRI.path();
       int _indexOf = _path.indexOf("/src");
       int index = _indexOf;
       String result = "";
       int _operator_minus = IntegerExtensions.operator_minus(1);
       boolean _operator_notEquals = ObjectExtensions.operator_notEquals(((Integer)index), ((Integer)_operator_minus));
       if (_operator_notEquals) {
         {
           int _operator_plus = IntegerExtensions.operator_plus(((Integer)index), ((Integer)5));
           index = _operator_plus;
           Resource _eResource_1 = diagram.eResource();
           URI _uRI_1 = _eResource_1.getURI();
           String _path_1 = _uRI_1.path();
           String _substring = _path_1.substring(index);
           String substring = _substring;
           String[] _split = substring.split("/");
           String[] results = _split;
           final String[] typeConverted_results = (String[])results;
           int _size = ((List<String>)Conversions.doWrapArray(typeConverted_results)).size();
           index = _size;
           boolean _operator_greaterThan = ComparableExtensions.<Integer>operator_greaterThan(((Integer)index), ((Integer)1));
           Boolean _xwhileexpression = _operator_greaterThan;
           while (_xwhileexpression) {
             {
               String _operator_plus_1 = StringExtensions.operator_plus(result, "../");
               result = _operator_plus_1;
               int _operator_minus_1 = IntegerExtensions.operator_minus(((Integer)index), ((Integer)1));
               index = _operator_minus_1;
             }
             boolean _operator_greaterThan_1 = ComparableExtensions.<Integer>operator_greaterThan(((Integer)index), ((Integer)1));
             _xwhileexpression = _operator_greaterThan_1;
           }
         }
       }
       boolean _equals = result.equals("");
       if (_equals) {
         result = "./";
       }
       return result;
     }
   }
   
   public String getRelativeFileName(final Diagram diagram) {
     {
       Resource _eResource = diagram.eResource();
       URI _uRI = _eResource.getURI();
       String _path = _uRI.path();
       int _indexOf = _path.indexOf("/src");
       int index = _indexOf;
       String result = "";
       int _operator_minus = IntegerExtensions.operator_minus(1);
       boolean _operator_notEquals = ObjectExtensions.operator_notEquals(((Integer)index), ((Integer)_operator_minus));
       if (_operator_notEquals) {
         {
           int _operator_plus = IntegerExtensions.operator_plus(((Integer)index), ((Integer)4));
           index = _operator_plus;
           Resource _eResource_1 = diagram.eResource();
           URI _uRI_1 = _eResource_1.getURI();
           String _path_1 = _uRI_1.path();
           String _substring = _path_1.substring(index);
           result = _substring;
         }
       } else {
         Resource _eResource_2 = diagram.eResource();
         String _className = this.className(_eResource_2);
         result = _className;
       }
       String _replace = result.replace(".ftd", ".html");
       result = _replace;
       String _operator_plus_1 = StringExtensions.operator_plus(".", result);
       return _operator_plus_1;
     }
   }
   
   public StringConcatenation facttypeDiagramHtmlPage(final Diagram diagram) {
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
     String _rootDirectory = this.getToRootDirectory(diagram);
     _builder.append(_rootDirectory, "	");
     _builder.append("../css/styles.css\" type=\"text/css\"/>");
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     _builder.append("</head>");
     _builder.newLine();
     _builder.append("<body>");
     _builder.newLine();
     {
       EList<FactTypeDiagram> _factTypeDiagrams = diagram.getFactTypeDiagrams();
       for(FactTypeDiagram factTypeDiagram : _factTypeDiagrams) {
         _builder.append("    ");
         _builder.append("<div class=\"index\"><a href=\"");
         String _rootDirectory_1 = this.getToRootDirectory(diagram);
         _builder.append(_rootDirectory_1, "    ");
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
         String _rootDirectory_2 = this.getToRootDirectory(diagram);
         _builder.append(_rootDirectory_2, "    ");
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
           boolean _operator_notEquals = ObjectExtensions.operator_notEquals(_description, null);
           if (_operator_notEquals) {
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
         StringConcatenation _htmlCode = this.toHtmlCode(factTypeDiagram, diagram);
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
   
   public StringConcatenation toHtmlCode(final FactTypeDiagram factTypeDiagram, final Diagram diagram) {
     StringConcatenation _builder = new StringConcatenation();
     _builder.append("<table class=\"column-data\">");
     _builder.newLine();
     _builder.append("\t");
     _builder.append("<tbody class=\"rules\">");
     _builder.newLine();
     {
       EList<AbstractRule> _rules = factTypeDiagram.getRules();
       for(AbstractRule abstractRule : _rules) {
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
           for(Column column : _columns) {
             {
               boolean _operator_and = false;
               EList<Column> _columns_1 = abstractRule.getColumns();
               boolean _contains = _columns_1.contains(column);
               if (!_contains) {
                 _operator_and = false;
               } else {
                 boolean _value = isSecondColumn.getValue();
                 boolean _operator_not = BooleanExtensions.operator_not(_value);
                 _operator_and = BooleanExtensions.operator_and(_contains, _operator_not);
               }
               if (_operator_and) {
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
                 StringConcatenation _htmlReferences = this.htmlReferences(abstractRule);
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
                 _builder.newLineIfNotEmpty();} else {
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
           for(Column column_1 : _columns_2) {
             {
               EList<Column> _columns_3 = abstractRule.getColumns();
               boolean _contains_1 = _columns_3.contains(column_1);
               if (_contains_1) {
                 _builder.append("\t");
                 _builder.append("\t");
                 ruleColumnCounter.increase();
                 _builder.newLineIfNotEmpty();
                 {
                   boolean _operator_and_1 = false;
                   int _value_1 = ruleColumnCounter.getValue();
                   boolean _operator_equals = ObjectExtensions.operator_equals(((Integer)_value_1), ((Integer)1));
                   if (!_operator_equals) {
                     _operator_and_1 = false;
                   } else {
                     EList<Column> _columns_4 = abstractRule.getColumns();
                     int _size = _columns_4.size();
                     boolean _operator_equals_1 = ObjectExtensions.operator_equals(((Integer)_size), ((Integer)1));
                     _operator_and_1 = BooleanExtensions.operator_and(_operator_equals, _operator_equals_1);
                   }
                   if (_operator_and_1) {
                     _builder.append("\t");
                     _builder.append("\t");
                     _builder.append("<td class=\"line line-single\"><span><span></span></span></td>");
                     _builder.newLine();} else {
                     boolean _operator_and_2 = false;
                     int _value_2 = ruleColumnCounter.getValue();
                     boolean _operator_equals_2 = ObjectExtensions.operator_equals(((Integer)_value_2), ((Integer)1));
                     if (!_operator_equals_2) {
                       _operator_and_2 = false;
                     } else {
                       EList<Column> _columns_5 = abstractRule.getColumns();
                       int _size_1 = _columns_5.size();
                       boolean _operator_greaterThan = ComparableExtensions.<Integer>operator_greaterThan(((Integer)_size_1), ((Integer)1));
                       _operator_and_2 = BooleanExtensions.operator_and(_operator_equals_2, _operator_greaterThan);
                     }
                     if (_operator_and_2) {
                       _builder.append("\t");
                       _builder.append("\t");
                       _builder.append("<td class=\"line line-left\"><span><span></span></span></td>");
                       _builder.newLine();} else {
                       int _value_3 = ruleColumnCounter.getValue();
                       EList<Column> _columns_6 = abstractRule.getColumns();
                       int _size_2 = _columns_6.size();
                       boolean _operator_equals_3 = ObjectExtensions.operator_equals(((Integer)_value_3), ((Integer)_size_2));
                       if (_operator_equals_3) {
                         _builder.append("\t");
                         _builder.append("\t");
                         _builder.append("<td class=\"line line-right\"><span><span></span></span></td>");
                         _builder.newLine();} else {
                         _builder.append("\t");
                         _builder.append("\t");
                         _builder.append("<td class=\"line line-center\"><span><span></span></span></td>");
                         _builder.newLine();
                       }
                     }
                   }
                 }} else {
                 {
                   boolean _operator_and_3 = false;
                   int _value_4 = ruleColumnCounter.getValue();
                   boolean _operator_greaterThan_1 = ComparableExtensions.<Integer>operator_greaterThan(((Integer)_value_4), ((Integer)0));
                   if (!_operator_greaterThan_1) {
                     _operator_and_3 = false;
                   } else {
                     int _value_5 = ruleColumnCounter.getValue();
                     EList<Column> _columns_7 = abstractRule.getColumns();
                     int _size_3 = _columns_7.size();
                     boolean _operator_lessThan = ComparableExtensions.<Integer>operator_lessThan(((Integer)_value_5), ((Integer)_size_3));
                     _operator_and_3 = BooleanExtensions.operator_and(_operator_greaterThan_1, _operator_lessThan);
                   }
                   if (_operator_and_3) {
                     _builder.append("\t");
                     _builder.append("\t");
                     _builder.append("<td class=\"line line\"><span><span></span></span></td>");
                     _builder.newLine();} else {
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
       for(AlternativeKey uc : _alternativeKeys) {
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
           for(Column column_2 : _columns_8) {
             {
               boolean _operator_and_4 = false;
               EList<Column> _columns_9 = uc.getColumns();
               boolean _contains_2 = _columns_9.contains(column_2);
               if (!_contains_2) {
                 _operator_and_4 = false;
               } else {
                 boolean _value_6 = isSecondColumn_1.getValue();
                 boolean _operator_not_1 = BooleanExtensions.operator_not(_value_6);
                 _operator_and_4 = BooleanExtensions.operator_and(_contains_2, _operator_not_1);
               }
               if (_operator_and_4) {
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
                 _builder.newLineIfNotEmpty();} else {
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
           for(Column column_3 : _columns_10) {
             {
               EList<Column> _columns_11 = uc.getColumns();
               boolean _contains_3 = _columns_11.contains(column_3);
               if (_contains_3) {
                 _builder.append("\t");
                 _builder.append("\t");
                 ruleColumnCounter_1.increase();
                 _builder.newLineIfNotEmpty();
                 {
                   boolean _operator_and_5 = false;
                   int _value_7 = ruleColumnCounter_1.getValue();
                   boolean _operator_equals_4 = ObjectExtensions.operator_equals(((Integer)_value_7), ((Integer)1));
                   if (!_operator_equals_4) {
                     _operator_and_5 = false;
                   } else {
                     EList<Column> _columns_12 = uc.getColumns();
                     int _size_4 = _columns_12.size();
                     boolean _operator_equals_5 = ObjectExtensions.operator_equals(((Integer)_size_4), ((Integer)1));
                     _operator_and_5 = BooleanExtensions.operator_and(_operator_equals_4, _operator_equals_5);
                   }
                   if (_operator_and_5) {
                     _builder.append("\t");
                     _builder.append("\t");
                     _builder.append("<td class=\"line line-arrow-dotted\"><span><span></span></span></td>");
                     _builder.newLine();} else {
                     boolean _operator_and_6 = false;
                     int _value_8 = ruleColumnCounter_1.getValue();
                     boolean _operator_equals_6 = ObjectExtensions.operator_equals(((Integer)_value_8), ((Integer)1));
                     if (!_operator_equals_6) {
                       _operator_and_6 = false;
                     } else {
                       EList<Column> _columns_13 = uc.getColumns();
                       int _size_5 = _columns_13.size();
                       boolean _operator_greaterThan_2 = ComparableExtensions.<Integer>operator_greaterThan(((Integer)_size_5), ((Integer)1));
                       _operator_and_6 = BooleanExtensions.operator_and(_operator_equals_6, _operator_greaterThan_2);
                     }
                     if (_operator_and_6) {
                       _builder.append("\t");
                       _builder.append("\t");
                       _builder.append("<td class=\"line line-left-arrow-dotted\"><span><span></span></span></td>");
                       _builder.newLine();} else {
                       int _value_9 = ruleColumnCounter_1.getValue();
                       EList<Column> _columns_14 = uc.getColumns();
                       int _size_6 = _columns_14.size();
                       boolean _operator_equals_7 = ObjectExtensions.operator_equals(((Integer)_value_9), ((Integer)_size_6));
                       if (_operator_equals_7) {
                         _builder.append("\t");
                         _builder.append("\t");
                         _builder.append("<td class=\"line line-right-arrow-dotted\"><span><span></span></span></td>");
                         _builder.newLine();} else {
                         _builder.append("\t");
                         _builder.append("\t");
                         _builder.append("<td class=\"line line-dotted\"><span><span></span></span></td>");
                         _builder.newLine();
                       }
                     }
                   }
                 }} else {
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
       boolean _operator_notEquals = ObjectExtensions.operator_notEquals(_primaryKey, null);
       if (_operator_notEquals) {
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
           for(Column column_4 : _columns_15) {
             {
               boolean _operator_and_7 = false;
               PrimaryKey _primaryKey_1 = factTypeDiagram.getPrimaryKey();
               EList<Column> _columns_16 = _primaryKey_1.getColumns();
               boolean _contains_4 = _columns_16.contains(column_4);
               if (!_contains_4) {
                 _operator_and_7 = false;
               } else {
                 boolean _value_10 = isSecondColumn_2.getValue();
                 boolean _operator_not_2 = BooleanExtensions.operator_not(_value_10);
                 _operator_and_7 = BooleanExtensions.operator_and(_contains_4, _operator_not_2);
               }
               if (_operator_and_7) {
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
                 _builder.newLineIfNotEmpty();} else {
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
           for(Column column_5 : _columns_17) {
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
                   boolean _operator_and_8 = false;
                   int _value_11 = ruleColumnCounter_2.getValue();
                   boolean _operator_equals_8 = ObjectExtensions.operator_equals(((Integer)_value_11), ((Integer)1));
                   if (!_operator_equals_8) {
                     _operator_and_8 = false;
                   } else {
                     PrimaryKey _primaryKey_5 = factTypeDiagram.getPrimaryKey();
                     EList<Column> _columns_19 = _primaryKey_5.getColumns();
                     int _size_7 = _columns_19.size();
                     boolean _operator_equals_9 = ObjectExtensions.operator_equals(((Integer)_size_7), ((Integer)1));
                     _operator_and_8 = BooleanExtensions.operator_and(_operator_equals_8, _operator_equals_9);
                   }
                   if (_operator_and_8) {
                     _builder.append("\t");
                     _builder.append("\t");
                     _builder.append("<td class=\"line line-arrow\"><span><span></span></span></td>");
                     _builder.newLine();} else {
                     boolean _operator_and_9 = false;
                     int _value_12 = ruleColumnCounter_2.getValue();
                     boolean _operator_equals_10 = ObjectExtensions.operator_equals(((Integer)_value_12), ((Integer)1));
                     if (!_operator_equals_10) {
                       _operator_and_9 = false;
                     } else {
                       PrimaryKey _primaryKey_6 = factTypeDiagram.getPrimaryKey();
                       EList<Column> _columns_20 = _primaryKey_6.getColumns();
                       int _size_8 = _columns_20.size();
                       boolean _operator_greaterThan_3 = ComparableExtensions.<Integer>operator_greaterThan(((Integer)_size_8), ((Integer)1));
                       _operator_and_9 = BooleanExtensions.operator_and(_operator_equals_10, _operator_greaterThan_3);
                     }
                     if (_operator_and_9) {
                       _builder.append("\t");
                       _builder.append("\t");
                       _builder.append("<td class=\"line line-left-arrow\"><span><span></span></span></td>");
                       _builder.newLine();} else {
                       int _value_13 = ruleColumnCounter_2.getValue();
                       PrimaryKey _primaryKey_7 = factTypeDiagram.getPrimaryKey();
                       EList<Column> _columns_21 = _primaryKey_7.getColumns();
                       int _size_9 = _columns_21.size();
                       boolean _operator_equals_11 = ObjectExtensions.operator_equals(((Integer)_value_13), ((Integer)_size_9));
                       if (_operator_equals_11) {
                         _builder.append("\t");
                         _builder.append("\t");
                         _builder.append("<td class=\"line line-right-arrow\"><span><span></span></span></td>");
                         _builder.newLine();} else {
                         _builder.append("\t");
                         _builder.append("\t");
                         _builder.append("<td class=\"line line\"><span><span></span></span></td>");
                         _builder.newLine();
                       }
                     }
                   }
                 }} else {
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
       for(Column column_6 : _columns_22) {
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
       for(Column column_7 : _columns_23) {
         {
           boolean _isNotEmpty = column_7.isNotEmpty();
           if (_isNotEmpty) {
             _builder.append("\t\t");
             _builder.append("<th class=\"required\">");
             String _name_6 = column_7.getName();
             _builder.append(_name_6, "		");
             _builder.append("</th>");
             _builder.newLineIfNotEmpty();} else {
             _builder.append("\t\t");
             _builder.append("<th class=\"optional\">");
             String _name_7 = column_7.getName();
             _builder.append(_name_7, "		");
             _builder.append("</th>");
             _builder.newLineIfNotEmpty();
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
       EList<Column> _columns_24 = factTypeDiagram.getColumns();
       for(Column column_8 : _columns_24) {
         _builder.append("\t\t");
         _builder.append("<td>");
         Type _type = column_8.getType();
         String _name_8 = _type.getName();
         _builder.append(_name_8, "		");
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
       EList<Column> _columns_25 = factTypeDiagram.getColumns();
       Column _get = _columns_25.get(0);
       EList<Value> _values = _get.getValues();
       for(Value value : _values) {
         _builder.append("\t");
         _builder.append("<tr>");
         _builder.newLine();
         {
           EList<Column> _columns_26 = factTypeDiagram.getColumns();
           for(Column column_9 : _columns_26) {
             _builder.append("\t");
             _builder.append("\t");
             _builder.append("<td>");
             EList<Value> _values_1 = column_9.getValues();
             EList<Column> _columns_27 = factTypeDiagram.getColumns();
             Column _get_1 = _columns_27.get(0);
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
       for(SentenceTemplate sentenceTemplate : _sentenceTemplates) {
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
       EList<Column> _columns_28 = factTypeDiagram.getColumns();
       Column _get_3 = _columns_28.get(0);
       EList<Value> _values_3 = _get_3.getValues();
       for(Value value_1 : _values_3) {
         _builder.append("\t");
         _builder.append("<ol>");
         _builder.newLine();
         {
           EList<SentenceTemplate> _sentenceTemplates_1 = factTypeDiagram.getSentenceTemplates();
           for(SentenceTemplate sentenceTemplate_1 : _sentenceTemplates_1) {
             _builder.append("\t");
             EList<Column> _columns_29 = factTypeDiagram.getColumns();
             Column _get_4 = _columns_29.get(0);
             EList<Value> _values_4 = _get_4.getValues();
             int _indexOf_1 = _values_4.indexOf(value_1);
             int columnIndex = _indexOf_1;
             _builder.newLineIfNotEmpty();
             _builder.append("\t");
             _builder.append("\t");
             _builder.append("<li>");
             StringConcatenation _constructCompleteSentenceWithExampleValues = this.constructCompleteSentenceWithExampleValues(sentenceTemplate_1, columnIndex);
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
       for(AbstractRule abstractRule_1 : _rules_1) {
         {
           boolean _operator_or = false;
           boolean _operator_or_1 = false;
           boolean _operator_or_2 = false;
           boolean _operator_or_3 = false;
           if ((abstractRule_1 instanceof org.facttype.diagram.GeneralConstraint)) {
             _operator_or_3 = true;
           } else {
             _operator_or_3 = BooleanExtensions.operator_or((abstractRule_1 instanceof org.facttype.diagram.GeneralConstraint), (abstractRule_1 instanceof org.facttype.diagram.ValueRule));
           }
           if (_operator_or_3) {
             _operator_or_2 = true;
           } else {
             _operator_or_2 = BooleanExtensions.operator_or(_operator_or_3, (abstractRule_1 instanceof org.facttype.diagram.DerivationRule));
           }
           if (_operator_or_2) {
             _operator_or_1 = true;
           } else {
             _operator_or_1 = BooleanExtensions.operator_or(_operator_or_2, (abstractRule_1 instanceof org.facttype.diagram.OccurrenceFrequencyRule));
           }
           if (_operator_or_1) {
             _operator_or = true;
           } else {
             _operator_or = BooleanExtensions.operator_or(_operator_or_1, (abstractRule_1 instanceof org.facttype.diagram.EventRule));
           }
           if (_operator_or) {
             _builder.append("\t");
             ruleDescriptionCounter.increase();
             _builder.newLineIfNotEmpty();
             {
               int _value_15 = ruleDescriptionCounter.getValue();
               boolean _operator_equals_12 = ObjectExtensions.operator_equals(((Integer)_value_15), ((Integer)1));
               if (_operator_equals_12) {
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
             StringConcatenation _ruleDescription = this.ruleDescription(abstractRule_1);
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
     String _rootDirectory = this.getToRootDirectory(diagram);
     _builder.append(_rootDirectory, "	");
     _builder.append("../img/icon-derivation.png\" alt=\"Icon\"/> derivation</li>");
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     _builder.append("<li><img src=\"");
     String _rootDirectory_1 = this.getToRootDirectory(diagram);
     _builder.append(_rootDirectory_1, "	");
     _builder.append("../img/icon-equality.png\" alt=\"Icon\"/> (partial) equality</li>");
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     _builder.append("<li><img src=\"");
     String _rootDirectory_2 = this.getToRootDirectory(diagram);
     _builder.append(_rootDirectory_2, "	");
     _builder.append("../img/icon-event.png\" alt=\"Icon\"/> event</li>");
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     _builder.append("<li><img src=\"");
     String _rootDirectory_3 = this.getToRootDirectory(diagram);
     _builder.append(_rootDirectory_3, "	");
     _builder.append("../img/icon-exclusion.png\" alt=\"Icon\"/> exclusion</li>");
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     _builder.append("<li><img src=\"");
     String _rootDirectory_4 = this.getToRootDirectory(diagram);
     _builder.append(_rootDirectory_4, "	");
     _builder.append("../img/icon-generalconstraint.png\" alt=\"Icon\"/> general constraint</li>");
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     _builder.append("<li><img src=\"");
     String _rootDirectory_5 = this.getToRootDirectory(diagram);
     _builder.append(_rootDirectory_5, "	");
     _builder.append("../img/icon-nooverlapping.png\" alt=\"Icon\"/> no overlapping</li>");
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     _builder.append("<li><img src=\"");
     String _rootDirectory_6 = this.getToRootDirectory(diagram);
     _builder.append(_rootDirectory_6, "	");
     _builder.append("../img/icon-occurrencefrequency.png\" alt=\"Icon\"/> occurrence frequency</li>");
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     _builder.append("<li><img src=\"");
     String _rootDirectory_7 = this.getToRootDirectory(diagram);
     _builder.append(_rootDirectory_7, "	");
     _builder.append("../img/icon-subset-super.png\" alt=\"Icon\"/> super set</li>");
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     _builder.append("<li><img src=\"");
     String _rootDirectory_8 = this.getToRootDirectory(diagram);
     _builder.append(_rootDirectory_8, "	");
     _builder.append("../img/icon-subset-sub.png\" alt=\"Icon\"/> sub set</li>");
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     _builder.append("<li><img src=\"");
     String _rootDirectory_9 = this.getToRootDirectory(diagram);
     _builder.append(_rootDirectory_9, "	");
     _builder.append("../img/icon-value.png\" alt=\"Icon\"/> value</li>");
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     _builder.append("<li><img src=\"");
     String _rootDirectory_10 = this.getToRootDirectory(diagram);
     _builder.append(_rootDirectory_10, "	");
     _builder.append("../img/icon-required-legend.png\" alt=\"Icon\"/> required</li>");
     _builder.newLineIfNotEmpty();
     _builder.append("\t");
     _builder.append("<li><img src=\"");
     String _rootDirectory_11 = this.getToRootDirectory(diagram);
     _builder.append(_rootDirectory_11, "	");
     _builder.append("../img/icon-optional-legend.png\" alt=\"Icon\"/> optional</li>");
     _builder.newLineIfNotEmpty();
     _builder.append("</ul>");
     _builder.newLine();
     return _builder;
   }
   
   protected StringConcatenation _ruleDescription(final AbstractRule abstractRule) {
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
       boolean _operator_notEquals = ObjectExtensions.operator_notEquals(_description, null);
       if (_operator_notEquals) {
         Description _description_1 = abstractRule.getDescription();
         String _text = _description_1.getText();
         _builder.append(_text, "	");} else {
         _builder.append("(no description)");
       }
     }
     _builder.append("</td>");
     _builder.newLineIfNotEmpty();
     _builder.append("</tr>");
     _builder.newLine();
     return _builder;
   }
   
   protected StringConcatenation _ruleDescription(final EventRule eventRule) {
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
       boolean _operator_notEquals = ObjectExtensions.operator_notEquals(_description, null);
       if (_operator_notEquals) {
         Description _description_1 = eventRule.getDescription();
         String _text = _description_1.getText();
         _builder.append(_text, "		");} else {
         _builder.append("(no description)");
       }
     }
     _builder.newLineIfNotEmpty();
     _builder.append("\t\t");
    _builder.append("Triggered <em>");
     AfterBefore _afterBefore = eventRule.getAfterBefore();
     _builder.append(_afterBefore, "		");
    _builder.append("</em> the rule.");
     _builder.newLineIfNotEmpty();
     {
       EList<Crud> _crud = eventRule.getCrud();
       boolean hasAnyElements = false;
       for(Crud crud : _crud) {
         if (!hasAnyElements) {
           hasAnyElements = true;
          _builder.append(" Triggers on: ", "		");
         } else {
           _builder.appendImmediate(",", "		");
         }
         _builder.append("\t\t");
        _builder.append("<em>");
         _builder.append(crud, "		");
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
   
   protected StringConcatenation _ruleDescription(final OccurrenceFrequencyRule occurrenceFrequencyRule) {
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
       boolean _operator_notEquals = ObjectExtensions.operator_notEquals(_description, null);
       if (_operator_notEquals) {
         Description _description_1 = occurrenceFrequencyRule.getDescription();
         String _text = _description_1.getText();
         _builder.append(_text, "		");} else {
         _builder.append("(no description)");
       }
     }
     _builder.newLineIfNotEmpty();
     _builder.append("\t\t");
     {
       Minimum _minimum = occurrenceFrequencyRule.getMinimum();
       boolean _operator_notEquals_1 = ObjectExtensions.operator_notEquals(_minimum, null);
       if (_operator_notEquals_1) {
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
       boolean _operator_notEquals_2 = ObjectExtensions.operator_notEquals(_maximum, null);
       if (_operator_notEquals_2) {
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
   
   protected StringConcatenation _ruleDescription(final ValueRule valueRule) {
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
       boolean _operator_notEquals = ObjectExtensions.operator_notEquals(_description, null);
       if (_operator_notEquals) {
         Description _description_1 = valueRule.getDescription();
         String _text = _description_1.getText();
         _builder.append(_text, "	");} else {
         _builder.append("(no description)");
       }
     }
     _builder.newLineIfNotEmpty();
     {
       EList<String> _value = valueRule.getValue();
       boolean hasAnyElements = false;
       for(String value : _value) {
         if (!hasAnyElements) {
           hasAnyElements = true;
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
     String _rootDirectory = this.getToRootDirectory(originatingRule);
     String _operator_plus = StringExtensions.operator_plus("<a href=\"", _rootDirectory);
     EObject _eContainer = referencedRule.eContainer();
     EObject _eContainer_1 = _eContainer.eContainer();
     String _relativeFileName = this.getRelativeFileName(((Diagram) _eContainer_1));
     String _operator_plus_1 = StringExtensions.operator_plus(_operator_plus, _relativeFileName);
     String _operator_plus_2 = StringExtensions.operator_plus(_operator_plus_1, "#");
     String _name = referencedRule.getName();
     String _operator_plus_3 = StringExtensions.operator_plus(_operator_plus_2, _name);
     String _operator_plus_4 = StringExtensions.operator_plus(_operator_plus_3, "\">");
     String _name_1 = referencedRule.getName();
     String _operator_plus_5 = StringExtensions.operator_plus(_operator_plus_4, _name_1);
     String _operator_plus_6 = StringExtensions.operator_plus(_operator_plus_5, "</a>");
     return _operator_plus_6;
   }
   
   protected StringConcatenation _htmlReferences(final AbstractRule rule) {
     return null;
   }
   
   protected StringConcatenation _htmlReferences(final EventRule rule) {
     StringConcatenation _builder = new StringConcatenation();
     _builder.append("<br>triggers:");
     _builder.newLine();
     {
       EList<DerivationRule> _triggers = rule.getTriggers();
       boolean hasAnyElements = false;
       for(DerivationRule trigger : _triggers) {
         if (!hasAnyElements) {
           hasAnyElements = true;
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
   
   protected StringConcatenation _htmlReferences(final SubsetRule rule) {
     StringConcatenation _builder = new StringConcatenation();
     _builder.append("<br>ref:");
     _builder.newLine();
     {
       EList<SubsetRule> _references = rule.getReferences();
       boolean hasAnyElements = false;
       for(SubsetRule reference : _references) {
         if (!hasAnyElements) {
           hasAnyElements = true;
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
   
   protected StringConcatenation _htmlReferences(final DerivationRule rule) {
     StringConcatenation _builder = new StringConcatenation();
     _builder.append("<br>ref:");
     _builder.newLine();
     {
       EList<EventRule> _references = rule.getReferences();
       boolean hasAnyElements = false;
       for(EventRule reference : _references) {
         if (!hasAnyElements) {
           hasAnyElements = true;
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
   
   protected StringConcatenation _htmlReferences(final EqualityRule rule) {
     StringConcatenation _builder = new StringConcatenation();
     _builder.append("<br>ref:");
     _builder.newLine();
     {
       EList<EqualityRule> _references = rule.getReferences();
       boolean hasAnyElements = false;
       for(EqualityRule reference : _references) {
         if (!hasAnyElements) {
           hasAnyElements = true;
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
   
   protected StringConcatenation _htmlReferences(final PartialEqualityRule rule) {
     StringConcatenation _builder = new StringConcatenation();
     _builder.append("<br>ref:");
     _builder.newLine();
     {
       EList<PartialEqualityRule> _references = rule.getReferences();
       boolean hasAnyElements = false;
       for(PartialEqualityRule reference : _references) {
         if (!hasAnyElements) {
           hasAnyElements = true;
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
   
   protected StringConcatenation _htmlReferences(final ExclusionRule rule) {
     StringConcatenation _builder = new StringConcatenation();
     _builder.append("<br>ref:");
     _builder.newLine();
     {
       EList<ExclusionRule> _references = rule.getReferences();
       boolean hasAnyElements = false;
       for(ExclusionRule reference : _references) {
         if (!hasAnyElements) {
           hasAnyElements = true;
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
       for (Content content : _contents) {
         {
           String _content = content.getContent();
           boolean _operator_notEquals = ObjectExtensions.operator_notEquals(_content, null);
           if (_operator_notEquals) {
             String _operator_plus = StringExtensions.operator_plus(result, " ");
             String _content_1 = content.getContent();
             String _operator_plus_1 = StringExtensions.operator_plus(_operator_plus, _content_1);
             result = _operator_plus_1;
           }
           Column _column = content.getColumn();
           boolean _operator_notEquals_1 = ObjectExtensions.operator_notEquals(_column, null);
           if (_operator_notEquals_1) {
             String _operator_plus_2 = StringExtensions.operator_plus(result, " ");
             Column _column_1 = content.getColumn();
             String _columnNameInSentence = this.columnNameInSentence(_column_1);
             String _operator_plus_3 = StringExtensions.operator_plus(_operator_plus_2, _columnNameInSentence);
             result = _operator_plus_3;
           }
         }
       }
       String _contentEnd = sentenceTemplate.getContentEnd();
       boolean _operator_notEquals_2 = ObjectExtensions.operator_notEquals(_contentEnd, null);
       if (_operator_notEquals_2) {
         String _operator_plus_4 = StringExtensions.operator_plus(result, " ");
         String _contentEnd_1 = sentenceTemplate.getContentEnd();
         String _operator_plus_5 = StringExtensions.operator_plus(_operator_plus_4, _contentEnd_1);
         result = _operator_plus_5;
       }
       _xblockexpression = (result);
     }
     return _xblockexpression;
   }
   
   public String columnNameInSentence(final Column column) {
     String _name = column.getName();
     String _operator_plus = StringExtensions.operator_plus("<em>&lt;", _name);
     String _operator_plus_1 = StringExtensions.operator_plus(_operator_plus, "&gt;</em>");
     return _operator_plus_1;
   }
   
   public StringConcatenation constructCompleteSentenceWithExampleValues(final SentenceTemplate sentenceTemplate, final int index) {
     StringConcatenation _builder = new StringConcatenation();
     {
       EList<Content> _contents = sentenceTemplate.getContents();
       for(Content content : _contents) {
         {
           String _content = content.getContent();
           boolean _operator_notEquals = ObjectExtensions.operator_notEquals(_content, null);
           if (_operator_notEquals) {
             String _content_1 = content.getContent();
             _builder.append(_content_1, "");
             _builder.newLineIfNotEmpty();
           }
         }
         {
           Column _column = content.getColumn();
           boolean _operator_notEquals_1 = ObjectExtensions.operator_notEquals(_column, null);
           if (_operator_notEquals_1) {
             Column _column_1 = content.getColumn();
             EList<Value> _values = _column_1.getValues();
             Value _get = _values.get(index);
             Value value = _get;
             _builder.newLineIfNotEmpty();
             String _value = value.getValue();
             _builder.append(_value, "");
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
   
   protected String _getIcon(final SubsetRule rule) {
     String _xifexpression = null;
     boolean _isSuper = rule.isSuper();
     if (_isSuper) {
       String _rootDirectory = this.getToRootDirectory(rule);
       String _operator_plus = StringExtensions.operator_plus("<img src=\"", _rootDirectory);
       String _operator_plus_1 = StringExtensions.operator_plus(_operator_plus, "../img/icon-subset-super.png\" alt=\"Icon\"/>");
       _xifexpression = _operator_plus_1;
     } else {
       String _rootDirectory_1 = this.getToRootDirectory(rule);
       String _operator_plus_2 = StringExtensions.operator_plus("<img src=\"", _rootDirectory_1);
       String _operator_plus_3 = StringExtensions.operator_plus(_operator_plus_2, "../img/icon-subset-sub.png\" alt=\"Icon\"/>");
       _xifexpression = _operator_plus_3;
     }
     return _xifexpression;
   }
   
   protected String _getIcon(final EqualityRule rule) {
     String _rootDirectory = this.getToRootDirectory(rule);
     String _operator_plus = StringExtensions.operator_plus("<img src=\"", _rootDirectory);
     String _operator_plus_1 = StringExtensions.operator_plus(_operator_plus, "../img/icon-equality.png\" alt=\"Icon\"/>");
     return _operator_plus_1;
   }
   
   protected String _getIcon(final PartialEqualityRule rule) {
     String _rootDirectory = this.getToRootDirectory(rule);
     String _operator_plus = StringExtensions.operator_plus("<img src=\"", _rootDirectory);
     String _operator_plus_1 = StringExtensions.operator_plus(_operator_plus, "../img/icon-equality.png\" alt=\"Icon\"/>");
     return _operator_plus_1;
   }
   
   protected String _getIcon(final OccurrenceFrequencyRule rule) {
     String _rootDirectory = this.getToRootDirectory(rule);
     String _operator_plus = StringExtensions.operator_plus("<img src=\"", _rootDirectory);
     String _operator_plus_1 = StringExtensions.operator_plus(_operator_plus, "../img/icon-occurrencefrequency.png\" alt=\"Icon\"/>");
     return _operator_plus_1;
   }
   
   protected String _getIcon(final NoOverlappingRule rule) {
     String _rootDirectory = this.getToRootDirectory(rule);
     String _operator_plus = StringExtensions.operator_plus("<img src=\"", _rootDirectory);
     String _operator_plus_1 = StringExtensions.operator_plus(_operator_plus, "../img/icon-nooverlapping.png\" alt=\"Icon\"/>");
     return _operator_plus_1;
   }
   
   protected String _getIcon(final ExclusionRule rule) {
     String _rootDirectory = this.getToRootDirectory(rule);
     String _operator_plus = StringExtensions.operator_plus("<img src=\"", _rootDirectory);
     String _operator_plus_1 = StringExtensions.operator_plus(_operator_plus, "../img/icon-exclusion.png\" alt=\"Icon\"/>");
     return _operator_plus_1;
   }
   
   protected String _getIcon(final GeneralConstraint rule) {
     String _rootDirectory = this.getToRootDirectory(rule);
     String _operator_plus = StringExtensions.operator_plus("<img src=\"", _rootDirectory);
     String _operator_plus_1 = StringExtensions.operator_plus(_operator_plus, "../img/icon-generalconstraint.png\" alt=\"Icon\"/>");
     return _operator_plus_1;
   }
   
   protected String _getIcon(final EventRule rule) {
     String _rootDirectory = this.getToRootDirectory(rule);
     String _operator_plus = StringExtensions.operator_plus("<img src=\"", _rootDirectory);
     String _operator_plus_1 = StringExtensions.operator_plus(_operator_plus, "../img/icon-event.png\" alt=\"Icon\"/>");
     return _operator_plus_1;
   }
   
   protected String _getIcon(final DerivationRule rule) {
     String _rootDirectory = this.getToRootDirectory(rule);
     String _operator_plus = StringExtensions.operator_plus("<img src=\"", _rootDirectory);
     String _operator_plus_1 = StringExtensions.operator_plus(_operator_plus, "../img/icon-derivation.png\" alt=\"Icon\"/>");
     return _operator_plus_1;
   }
   
   protected String _getIcon(final ValueRule rule) {
     String _rootDirectory = this.getToRootDirectory(rule);
     String _operator_plus = StringExtensions.operator_plus("<img src=\"", _rootDirectory);
     String _operator_plus_1 = StringExtensions.operator_plus(_operator_plus, "../img/icon-value.png\" alt=\"Icon\"/>");
     return _operator_plus_1;
   }
   
   public StringConcatenation ruleDescription(final AbstractRule eventRule) {
     if ((eventRule instanceof EventRule)) {
       return _ruleDescription((EventRule)eventRule);
     } else if ((eventRule instanceof OccurrenceFrequencyRule)) {
       return _ruleDescription((OccurrenceFrequencyRule)eventRule);
     } else if ((eventRule instanceof ValueRule)) {
       return _ruleDescription((ValueRule)eventRule);
     } else if ((eventRule instanceof AbstractRule)) {
       return _ruleDescription((AbstractRule)eventRule);
     } else {
       throw new IllegalArgumentException("Unhandled parameter types: " +
         java.util.Arrays.<Object>asList(eventRule).toString());
     }
   }
   
   public StringConcatenation htmlReferences(final AbstractRule rule) {
     if ((rule instanceof DerivationRule)) {
       return _htmlReferences((DerivationRule)rule);
     } else if ((rule instanceof EqualityRule)) {
       return _htmlReferences((EqualityRule)rule);
     } else if ((rule instanceof EventRule)) {
       return _htmlReferences((EventRule)rule);
     } else if ((rule instanceof ExclusionRule)) {
       return _htmlReferences((ExclusionRule)rule);
     } else if ((rule instanceof PartialEqualityRule)) {
       return _htmlReferences((PartialEqualityRule)rule);
     } else if ((rule instanceof SubsetRule)) {
       return _htmlReferences((SubsetRule)rule);
     } else if ((rule instanceof AbstractRule)) {
       return _htmlReferences((AbstractRule)rule);
     } else {
       throw new IllegalArgumentException("Unhandled parameter types: " +
         java.util.Arrays.<Object>asList(rule).toString());
     }
   }
   
   public String getShortCode(final AbstractRule rule) {
     if ((rule instanceof DerivationRule)) {
       return _getShortCode((DerivationRule)rule);
     } else if ((rule instanceof EqualityRule)) {
       return _getShortCode((EqualityRule)rule);
     } else if ((rule instanceof EventRule)) {
       return _getShortCode((EventRule)rule);
     } else if ((rule instanceof ExclusionRule)) {
       return _getShortCode((ExclusionRule)rule);
     } else if ((rule instanceof GeneralConstraint)) {
       return _getShortCode((GeneralConstraint)rule);
     } else if ((rule instanceof NoOverlappingRule)) {
       return _getShortCode((NoOverlappingRule)rule);
     } else if ((rule instanceof OccurrenceFrequencyRule)) {
       return _getShortCode((OccurrenceFrequencyRule)rule);
     } else if ((rule instanceof PartialEqualityRule)) {
       return _getShortCode((PartialEqualityRule)rule);
     } else if ((rule instanceof SubsetRule)) {
       return _getShortCode((SubsetRule)rule);
     } else if ((rule instanceof ValueRule)) {
       return _getShortCode((ValueRule)rule);
     } else {
       throw new IllegalArgumentException("Unhandled parameter types: " +
         java.util.Arrays.<Object>asList(rule).toString());
     }
   }
   
   public String getIcon(final AbstractRule rule) {
     if ((rule instanceof DerivationRule)) {
       return _getIcon((DerivationRule)rule);
     } else if ((rule instanceof EqualityRule)) {
       return _getIcon((EqualityRule)rule);
     } else if ((rule instanceof EventRule)) {
       return _getIcon((EventRule)rule);
     } else if ((rule instanceof ExclusionRule)) {
       return _getIcon((ExclusionRule)rule);
     } else if ((rule instanceof GeneralConstraint)) {
       return _getIcon((GeneralConstraint)rule);
     } else if ((rule instanceof NoOverlappingRule)) {
       return _getIcon((NoOverlappingRule)rule);
     } else if ((rule instanceof OccurrenceFrequencyRule)) {
       return _getIcon((OccurrenceFrequencyRule)rule);
     } else if ((rule instanceof PartialEqualityRule)) {
       return _getIcon((PartialEqualityRule)rule);
     } else if ((rule instanceof SubsetRule)) {
       return _getIcon((SubsetRule)rule);
     } else if ((rule instanceof ValueRule)) {
       return _getIcon((ValueRule)rule);
     } else {
       throw new IllegalArgumentException("Unhandled parameter types: " +
         java.util.Arrays.<Object>asList(rule).toString());
     }
   }
 }
