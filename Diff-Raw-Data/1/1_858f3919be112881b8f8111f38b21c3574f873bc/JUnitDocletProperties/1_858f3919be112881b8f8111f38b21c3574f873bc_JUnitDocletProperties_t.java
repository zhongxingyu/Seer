 /*
     This file is part of  EasyTest CodeGen, a project to generate 
     JUnit test cases  from source code in EasyTest Template format and  helping to keep them in sync
     during refactoring.
  	EasyTest CodeGen, a tool provided by
 	EaseTech Organization Under Apache License 2.0 
 	http://www.apache.org/licenses/LICENSE-2.0.txt
 */
 
 package org.easetech.easytest.codegen;
 
 /**
 * An interface with all constants declared..
 *
 * @author Ravi Polampelli
 *
 */
 
 
 public interface JUnitDocletProperties {
 
     //
     public final static String LICENSE                         = "license";
     public final static String JUNIT_VERSION                   = "junit.version";
     public final static String TEMPLATE_NAME                   = "template.name";
     public final static String TESTSOURCE_INDENT_WIDTH         = "testsource.indent.width";
     
     //
     public final static String MARKER_IMPORT_BEGIN             = "marker.import.begin";
     public final static String MARKER_IMPORT_END               = "marker.import.end";
     public final static String MARKER_EXTENDS_IMPLEMENTS_BEGIN = "marker.extends_implements.begin";
     public final static String MARKER_EXTENDS_IMPLEMENTS_END   = "marker.extends_implements.end";
     public final static String MARKER_CLASS_BEGIN              = "marker.class.begin";
     public final static String MARKER_CLASS_END                = "marker.class.end";
     public final static String MARKER_METHOD_BEGIN             = "marker.method.begin";
     public final static String MARKER_METHOD_END               = "marker.method.end";
     public final static String MARKER_JAVADOC_CLASS_BEGIN      = "marker.javadoc_class.begin";
     public final static String MARKER_JAVADOC_CLASS_END        = "marker.javadoc_class.end";
     public final static String MARKER_JAVADOC_METHOD_BEGIN     = "marker.javadoc_method.begin";
     public final static String MARKER_JAVADOC_METHOD_END       = "marker.javadoc_method.end";
 
     //
     public final static String ACCESSOR_TESTS                  = "accessor.tests";
     public final static String ACCESSOR_NAME                   = "accessor.name";
     public final static String ACCESSOR_TYPE_NAME              = "accessor.type.name";
     public final static String ACCESSOR_SET_NAME               = "accessor.set.name";
     public final static String ACCESSOR_GET_NAME               = "accessor.get.name";
 
     //
     public final static String TESTSUITE_PACKAGE_NAME          = "testsuite.package.name";
     public final static String TESTSUITE_IMPORTS               = "testsuite.imports";
     public final static String TESTSUITE_CLASS_NAME            = "testsuite.class.name";
     public final static String TESTSUITE_INSTANCE_NAME         = "testsuite.instance.name";
     public final static String TESTSUITE_ADD_TESTCASES         = "testsuite.add.testcases";
     public final static String TESTSUITE_ADD_TESTSUITES        = "testsuite.add.testsuites";
 
     //
     public final static String TESTCASE_PACKAGE_NAME           = "testcase.package.name";
     public final static String TESTCASE_CLASS_NAME             = "testcase.class.name";
     public final static String TESTCASE_INSTANCE_NAME          = "testcase.instance.name";
     public final static String TESTCASE_INSTANCE_TYPE          = "testcase.instance.type";
     public final static String TESTCASE_TESTMETHODS            = "testcase.testmethods";
     public final static String TESTCASE_UNMATCHED              = "testcase.unmatched";
     public final static String TESTCASE_METHOD_UNMATCHED       = "testcase.method.unmatched";
     public final static String TESTCASE_DATA_FILE_PATH         = "testcase.data.file.path";
     public final static String TESTCASE_REGISTER_CONVERTERS    = "testcase.register.converters";
     public final static String TESTCASE_REGISTER_EDITORS       = "testcase.register.editors";
     public final static String TESTCASE_IMPORTS			       = "testcase.imports";
    public final static String TESTCASE_LOADER_TYPE			   = "testcase.loader.type";
    
 
     //
     public final static String ADD_TESTSUITE_TO_TESTSUITE      = "add.testsuite.to.testsuite";
     public final static String ADD_TESTCASE_TO_TESTSUITE       = "add.testcase.to.testsuite";
     public final static String ADD_IMPORT_TESTSUITE            = "add.import.testsuite";
     public final static String ADD_TESTSUITE_NAME              = "add.testsuite.name";
     public final static String ADD_TESTCASE_NAME               = "add.testcase.name";
     public final static String ADD_IMPORT_NAME                 = "add.import.name";
 
     //
     public final static String TESTMETHOD_NAME                 = "testmethod.name";
     
     public final static String FILTER_INCLUDE                 = "filter.include";
     public final static String FILTER_EXCLUDE                 = "filter.exclude";
     public final static String OVERWRITE_EXISTING_TEST_DATA   = "overwrite.existing.test.data";
     public final static String OVERWRITE_EXISTING_CONVERTERS   = "overwrite.existing.converters";
     public final static String TESTCASE_EXTENSION   = "testcase.extension";
     public final static String TESTSUITE_EXTENSION   = "testsuite.extension";
     
     // variables holding informations about the device under test. (usefull in javadoc)
     public final static String PACKAGE_NAME                    = "package.name";
     public final static String CLASS_NAME                      = "class.name";
     public final static String METHOD_NAME                     = "method.name";
     public final static String METHOD_SIGNATURE                = "method.signature";
     public final static String METHOD_RETURNTYPE               = "method.returntype";
     public final static String METHOD_PARAMETER_VALUES         = "method.parameter.values";
     public final static String PARAM_CLASS_TYPE			       = "param.class.type";
 
     // variables, not required to be defined
     public final static String VALUE_LICENSE =
             "/**\n"+
             " * Generated by JUnitDoclet, a tool provided by\n"+
             " * ObjectFab GmbH under LGPL.\n"+
             " * Please see www.junitdoclet.org, www.gnu.org\n"+
             " * and www.objectfab.de for informations about\n"+
             " * the tool, the licence and the the authors.\n"+
             " */\n";
 
     public final static String VALUE_MARKER_BEGIN                    = "// JUnitDoclet begin ";
     public final static String VALUE_MARKER_END                      = "// JUnitDoclet end ";
     public final static String VALUE_MARKER_IMPORT                   = "import";
     public final static String VALUE_MARKER_EXTENDS_IMPLEMENTS       = "extends_implements";
     public final static String VALUE_MARKER_CLASS                    = "class";
     public final static String VALUE_MARKER_METHOD                   = "method";
     public final static String VALUE_MARKER_JAVADOC_CLASS            = "javadoc_class";
     public final static String VALUE_MARKER_JAVADOC_METHOD           = "javadoc_method";
     public final static String VALUE_MARKER_IMPORT_BEGIN             = VALUE_MARKER_BEGIN + VALUE_MARKER_IMPORT;
     public final static String VALUE_MARKER_IMPORT_END               = VALUE_MARKER_END + VALUE_MARKER_IMPORT;
     public final static String VALUE_MARKER_EXTENDS_IMPLEMENTS_BEGIN = VALUE_MARKER_BEGIN + VALUE_MARKER_EXTENDS_IMPLEMENTS;
     public final static String VALUE_MARKER_EXTENDS_IMPLEMENTS_END   = VALUE_MARKER_END + VALUE_MARKER_EXTENDS_IMPLEMENTS;
     public final static String VALUE_MARKER_CLASS_BEGIN              = VALUE_MARKER_BEGIN + VALUE_MARKER_CLASS;
     public final static String VALUE_MARKER_CLASS_END                = VALUE_MARKER_END + VALUE_MARKER_CLASS;
     public final static String VALUE_MARKER_METHOD_BEGIN             = VALUE_MARKER_BEGIN + VALUE_MARKER_METHOD;
     public final static String VALUE_MARKER_METHOD_END               = VALUE_MARKER_END + VALUE_MARKER_METHOD;
     public final static String VALUE_MARKER_JAVADOC_CLASS_BEGIN      = VALUE_MARKER_BEGIN + VALUE_MARKER_JAVADOC_CLASS;
     public final static String VALUE_MARKER_JAVADOC_CLASS_END        = VALUE_MARKER_END + VALUE_MARKER_JAVADOC_CLASS;
     public final static String VALUE_MARKER_JAVADOC_METHOD_BEGIN     = VALUE_MARKER_BEGIN + VALUE_MARKER_JAVADOC_METHOD;
     public final static String VALUE_MARKER_JAVADOC_METHOD_END       = VALUE_MARKER_END + VALUE_MARKER_JAVADOC_METHOD;
     public final static String VALUE_METHOD_UNMATCHED_NAME           = "testVault";
     public final static String VALUE_METHOD_UNMATCHED_NAME_MARKER    = "testcase." + VALUE_METHOD_UNMATCHED_NAME;
 
     // constants used as attribute describing templates
     public final static String TEMPLATE_ATTRIBUTE_DEFAULT            = "default";
     public final static String TEMPLATE_ATTRIBUTE_EASYTEST            = "easytest";
     public final static String TEMPLATE_ATTRIBUTE_DEFAULT_LAST       = "last";
     public final static String TEMPLATE_ATTRIBUTE_ENUM               = "enum";
     public final static String TEMPLATE_ATTRIBUTE_ENUM_FIRST         = TEMPLATE_ATTRIBUTE_ENUM + ".first";
     public final static String TEMPLATE_ATTRIBUTE_ENUM_LAST          = TEMPLATE_ATTRIBUTE_ENUM + ".last";
     public final static String TEMPLATE_ATTRIBUTE_ARRAY              = "array";
     public final static String TEMPLATE_ATTRIBUTE_ACCESSOR           = "accessor";
     
     // variables holding informations about the converters under test. (usefull in javadoc)
     public final static String CONVERTER_CLASS_NAME                    = "converter.class.name";
     public final static String CONVERTER_INSTANCE_TYPE                 = "converter.instance.type";
     public final static String CONVERTER_INSTANCE_NAME                   = "converter.instance.name";
     public final static String CONVERTER_SETTERS                		 = "converter.setters";
     public final static String CONVERTER_INSTANCE_ATTRIBUTE_NAME         = "converter.instance.attribute.name";
     public final static String CONVERTER_INSTANCE_ATTRIBUTE_SETTER_NAME  = "converter.instance.attribute.setter.name";
     public final static String CONVERTER_INSTANCE_ATTRIBUTE_TYPE  = "converter.instance.attribute.type";
     public final static String CONVERTER_INSTANCE_ATTRIBUTE_TYPE_WRAPPER  = "converter.instance.attribute.type.wrapper";
     public final static String CONVERTER_CLASS_NAME_SUFFIX           = "Converter";
     public final static String CONVERTER_IMPORTS			       = "converter.imports";
     public final static String CONVERTER_INSTANCE_ATTRIBUTE_CONVERTER  = "converter.instance.attribute.converter";
     public final static String CONVERTER_INSTANCE_ATTRIBUTE_CONVERTERUTILMETHOD  = "converter.instance.attribute.converterutilmethod";
     
     
     // variables holding informations about the editors under test. (usefull in javadoc)
     public final static String EDITOR_CLASS_NAME                    = "editor.class.name";
     public final static String EDITOR_INSTANCE_TYPE                 = "editor.instance.type";
     public final static String EDITOR_INSTANCE_TYPE_FULLNAME        = "editor.instance.type.qualifiedname";
     public final static String EDITOR_SETVALUE                		 = "editor.setvalue";
     public final static String EDITOR_INSTANCE_ATTRIBUTE_NAME         = "editor.instance.attribute.name";
     public final static String EDITOR_INSTANCE_ATTRIBUTE_SETTER_NAME  = "editor.instance.attribute.setter.name";
     public final static String EDITOR_INSTANCE_ATTRIBUTE_TYPE  = "editor.instance.attribute.type";
     public final static String EDITOR_CLASS_NAME_SUFFIX           = "Editor";
 
 }
