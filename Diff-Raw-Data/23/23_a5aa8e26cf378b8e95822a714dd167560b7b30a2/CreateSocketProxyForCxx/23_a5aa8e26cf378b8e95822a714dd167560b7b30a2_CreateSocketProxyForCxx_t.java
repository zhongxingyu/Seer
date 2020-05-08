 package de.tum.ascodt.sidlcompiler.backend;
 
 
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.HashMap;
 import java.util.Stack;
 
 import org.eclipse.core.runtime.Assert;
 
 import de.tum.ascodt.plugin.utils.exceptions.ErrorWriterDevice;
 import de.tum.ascodt.plugin.utils.tracing.Trace;
 import de.tum.ascodt.sidlcompiler.astproperties.ExclusivelyInParameters;
 import de.tum.ascodt.sidlcompiler.astproperties.GetParameterList;
 import de.tum.ascodt.sidlcompiler.astproperties.GetProvidesAndUsesPortsOfComponent;
 import de.tum.ascodt.sidlcompiler.frontend.analysis.DepthFirstAdapter;
 import de.tum.ascodt.sidlcompiler.frontend.node.AClassPackageElement;
 import de.tum.ascodt.sidlcompiler.frontend.node.AInterfacePackageElement;
 import de.tum.ascodt.sidlcompiler.frontend.node.AOperation;
 import de.tum.ascodt.sidlcompiler.frontend.node.AUserDefinedType;
 import de.tum.ascodt.sidlcompiler.frontend.node.AUses;
 import de.tum.ascodt.sidlcompiler.frontend.node.PUserDefinedType;
 import de.tum.ascodt.sidlcompiler.symboltable.Scope;
 import de.tum.ascodt.sidlcompiler.symboltable.SymbolTable;
 import de.tum.ascodt.utils.TemplateFile;
 import de.tum.ascodt.utils.exceptions.ASCoDTException;
 
 
 public class CreateSocketProxyForCxx extends DepthFirstAdapter {
   private Trace _trace =
       new Trace(CreateSocketProxyForCxx.class.getCanonicalName());
 
   private Stack<TemplateFile> _templateFilesOfC2CxxProxyImplementation;
   private Stack<TemplateFile> _templateFilesOfC2CxxProxyHeader;
   private Stack<TemplateFile> _templateFilesProvidesPorts;
   private Stack<TemplateFile> _templateFilesUsesPortsForC2Cxx;
   private Stack<TemplateFile> _templateFilesOfTinyXMLHeader;
   private Stack<TemplateFile> _templateFilesOfTinyXMLImplementation;
 
   private Stack<String> _serverInvokers;
   private Stack<String> _clientInvokers;
   private Stack<String> _xmlInvokers;
   private Stack<String> _parallelWorkerInvokers;
   private Path _cxxDirectoryPath;
   private String[] _namespace;
   private SymbolTable _symbolTable;
   private String _fullyQualifiedComponentName;
   private boolean _generateProvidesMethods;
   private HashMap<String, Integer> _offset_map;
 
   // private String _providePortName;
 
   private String _fullQualifiedPortName;
 
   CreateSocketProxyForCxx(SymbolTable symbolTable,
                           Path componentsDirectoryPath,
                           String[] namespace,
                           HashMap<String, Integer> offset_map) {
     _templateFilesOfC2CxxProxyImplementation = new Stack<TemplateFile>();
     _templateFilesOfC2CxxProxyHeader = new Stack<TemplateFile>();
     _templateFilesProvidesPorts = new Stack<TemplateFile>();
     _templateFilesUsesPortsForC2Cxx = new Stack<TemplateFile>();
     _templateFilesOfTinyXMLHeader = new Stack<TemplateFile>();
     _templateFilesOfTinyXMLImplementation = new Stack<TemplateFile>();
     _serverInvokers = new Stack<String>();
     _clientInvokers = new Stack<String>();
     _xmlInvokers = new Stack<String>();
     _parallelWorkerInvokers = new Stack<String>();
     _cxxDirectoryPath = componentsDirectoryPath.resolve("c++");
     _namespace = namespace;
     _symbolTable = symbolTable;
     _offset_map = offset_map;
 
   }
 
   @Override
   public void inAClassPackageElement(AClassPackageElement node) {
     _trace.in("inAClassPackageElement(...)", "open new port interface");
 
     String componentName = node.getName().getText();
     _fullyQualifiedComponentName =
         _symbolTable.getScope(node).getFullyQualifiedName(componentName);
 
     _templateFilesOfC2CxxProxyImplementation.push(new TemplateFile(Paths.get("native-component-c2cxx-socket-implementation.template"),
                                                                    _cxxDirectoryPath.resolve(_fullyQualifiedComponentName.replaceAll("[.]",
                                                                                                                                      "/") + "C2CxxProxy.cpp"),
                                                                    _namespace,
                                                                    TemplateFile.getLanguageConfigurationForJNI(),
                                                                    true));
     _templateFilesOfC2CxxProxyHeader.push(new TemplateFile(Paths.get("native-component-c2cxx-socket-header.template"),
                                                            _cxxDirectoryPath.resolve(_fullyQualifiedComponentName.replaceAll("[.]",
                                                                                                                              "/") + "C2CxxProxy.h"),
                                                            _namespace,
                                                            TemplateFile.getLanguageConfigurationForJNI(),
                                                            true));
     _templateFilesOfTinyXMLHeader.push(new TemplateFile(Paths.get("tinyxml-header.template"),
                                                        _cxxDirectoryPath.resolve("tinyxml_ascodt.h"),
                                                         _namespace,
                                                         TemplateFile.getLanguageConfigurationForCPP(),
                                                         true));
     _templateFilesOfTinyXMLImplementation.push(new TemplateFile(Paths.get("tinyxml-cpp.template"),
                                                                _cxxDirectoryPath.resolve("tinyxml_ascodt.cpp"),
                                                                 _namespace,
                                                                 TemplateFile.getLanguageConfigurationForCPP(),
                                                                 true));
     _templateFilesOfC2CxxProxyImplementation.peek()
                                             .addMapping("__COMPONENT_NAME__",
                                                         componentName.toLowerCase());
     _templateFilesOfC2CxxProxyImplementation.peek()
                                             .addMapping("__COMPONENT_NAME_4WIN__",
                                                         componentName.toUpperCase());
     _templateFilesOfC2CxxProxyImplementation.peek()
                                             .addMapping("__COMPONENT_NAME_ENV__",
                                                         _fullyQualifiedComponentName.replaceAll("[.]",
                                                                                                 "_")
                                                                                     .toUpperCase());
     _templateFilesOfC2CxxProxyImplementation.peek()
                                             .addMapping("__CXX_FULL_QUALIFIED_NAME__",
                                                         _fullyQualifiedComponentName.replaceAll("[.]",
                                                                                                 "::"));
     _templateFilesOfC2CxxProxyImplementation.peek()
                                             .addMapping("__FULL_QUALIFIED_NAME__",
                                                         _fullyQualifiedComponentName);
     _templateFilesOfC2CxxProxyImplementation.peek()
                                             .addMapping("__PATH_FULL_QUALIFIED_NAME__",
                                                         _fullyQualifiedComponentName.replaceAll("[.]",
                                                                                                 "/"));
     _templateFilesOfC2CxxProxyHeader.peek()
                                     .addMapping("__PATH_FULL_QUALIFIED_NAME__",
                                                 _fullyQualifiedComponentName.replaceAll("[.]",
                                                                                         "/"));
     _templateFilesOfC2CxxProxyHeader.peek()
                                     .addMapping("__COMPONENT_NAME_ENV__",
                                                 _fullyQualifiedComponentName.replaceAll("[.]",
                                                                                         "_")
                                                                             .toUpperCase());
     _templateFilesOfC2CxxProxyHeader.peek().open();
     _templateFilesOfTinyXMLImplementation.peek().open();
     _templateFilesOfTinyXMLHeader.peek().open();
     _generateProvidesMethods = true;
 
     for (PUserDefinedType definedType : node.getProvides()) {
       definedType.apply(this);
 
     }
     _generateProvidesMethods = false;
 
     _trace.out("inAClassPackageElement(...)", "open new port interface");
   }
 
   @Override
   public void inAOperation(AOperation node) {
     Assert.isTrue(_generateProvidesMethods);
     try {
       _serverInvokers.push("invokers[" + _offset_map.get(_fullQualifiedPortName + node.getName()
                                                                                       .getText()) +
                            "]=invoker_" +
                            node.getName().getText() +
                            ";\n");
       _clientInvokers.push("invokers[" + _offset_map.get(_fullQualifiedPortName + node.getName()
                                                                                       .getText()) +
                            "]=invoker_" +
                            node.getName().getText() +
                            ";\n");
       _clientInvokers.push("invokers[" + _offset_map.get(_fullQualifiedPortName + node.getName()
                                                                                       .getText() +
                                                          "Parallel") +
                            "]=parallel_master_invoker_" +
                            node.getName().getText() +
                            ";\n");
       _parallelWorkerInvokers.push("parallel_worker_invokers[" + _offset_map.get(_fullQualifiedPortName + node.getName()
                                                                                                               .getText()) +
                                    "]=parallel_worker_invoker_" +
                                    node.getName().getText() +
                                    ";\n");
 
       TemplateFile c2CxxProxyImplementationTemplate =
           new TemplateFile(_templateFilesOfC2CxxProxyImplementation.peek(),
                            Paths.get("native-component-c2cxx-socket-implementation-provides-port.template"));
 
       ExclusivelyInParameters onlyInParameters = new ExclusivelyInParameters();
       node.apply(onlyInParameters);
 
       GetParameterList parameterList =
           new GetParameterList(_symbolTable.getScope(node));
       node.apply(parameterList);
       c2CxxProxyImplementationTemplate.addMapping("__SOCKET_PULL__",
                                                   parameterList.pullInFromSocketForCxx());
 
       c2CxxProxyImplementationTemplate.addMapping("__PARALLEL_WORKER_PULL__",
                                                   parameterList.pullInFromParallelWorkerForCxx());
 
       c2CxxProxyImplementationTemplate.addMapping("__PARALLEL_MASTER_PULL__",
                                                   parameterList.pullInFromParallelMasterForCxx());
 
       c2CxxProxyImplementationTemplate.addMapping("__SOCKET_PUSH__",
                                                   parameterList.pushOutToSocketForCxx());
       c2CxxProxyImplementationTemplate.addMapping("__OPERATION_PARAMETERS_LIST__",
                                                   parameterList.getParameterListInJNI(onlyInParameters.areAllParametersInParameters()));
       c2CxxProxyImplementationTemplate.addMapping("__OPERATION_NAME__",
                                                   node.getName().getText());
       c2CxxProxyImplementationTemplate.addMapping("__OPERATION_NAME_4WIN__",
                                                   node.getName()
                                                       .getText()
                                                       .toUpperCase());
 
       c2CxxProxyImplementationTemplate.addMapping("__OPERATION_PARAMETERS_LIST_C2F__",
                                                   parameterList.getParameterListInC2F());
       c2CxxProxyImplementationTemplate.addMapping("__FUNCTION_CALL_PARAMETERS_LIST__",
                                                   parameterList.getFunctionCallListInCxxSocket());
 
       _templateFilesProvidesPorts.add(c2CxxProxyImplementationTemplate);
 
     } catch (ASCoDTException e) {
       ErrorWriterDevice.getInstance().println(e);
     }
   }
 
   @Override
   public void inAUserDefinedType(AUserDefinedType node) {
     String fullQualifiedSymbol = Scope.getSymbol(node);
     AInterfacePackageElement interfaceNode =
         _symbolTable.getScope(node).getInterfaceDefinition(fullQualifiedSymbol);
     String portName = interfaceNode.getName().getText();
     _fullQualifiedPortName =
         _symbolTable.getScope(interfaceNode).getFullyQualifiedName(portName);
     if (_generateProvidesMethods) {
 
       if (interfaceNode != null) {
         interfaceNode.apply(this);
       }
     }
   }
 
   @Override
   public void inAUses(AUses node) {
     _trace.in("inAUses(AUses)", node.toString());
     GetProvidesAndUsesPortsOfComponent getPorts =
         new GetProvidesAndUsesPortsOfComponent();
     node.apply(getPorts);
     String fullQualifiedpPortTypeForC = getPorts.getUsesPorts("", "::");
     String fullQualifiedpPortType = getPorts.getUsesPorts("", ".");
     String fullQualifiedpPortTypeAsPath = getPorts.getUsesPorts("", "/");
     String portName = node.getAs().getText();
 
     try {
       TemplateFile templateForC2Cxx =
           new TemplateFile(_templateFilesOfC2CxxProxyImplementation.peek(),
                            Paths.get("native-component-c2cxx-socket-implementation-uses-ports.template"));
       templateForC2Cxx.addMapping("__USES_PORT_AS__", portName);
       templateForC2Cxx.addMapping("__USES_PORT_AS_4WIN__",
                                   portName.toUpperCase());
       templateForC2Cxx.addMapping("__CXX_FULL_QUALIFIED_PORT_TYPE__",
                                   fullQualifiedpPortTypeForC);
       templateForC2Cxx.addMapping("__PATH_FULL_QUALIFIED_PORT_TYPE__",
                                   fullQualifiedpPortTypeAsPath);
       _templateFilesUsesPortsForC2Cxx.push(templateForC2Cxx);
       _serverInvokers.push("invokers[" + _offset_map.get(fullQualifiedpPortType + "createPort") +
                            "]=invoker_create_client_port_for_" +
                            portName +
                            ";\n");
       _serverInvokers.push("invokers[" + _offset_map.get(fullQualifiedpPortType + "connectPort") +
                            "]=invoker_connect_client_dispatcher_" +
                            portName +
                            ";\n");
       _serverInvokers.push("invokers[" + _offset_map.get(fullQualifiedpPortType + "disconnectPort") +
                            "]=invoker_disconnect_client_dispatcher_" +
                            portName +
                            ";\n");
       _clientInvokers.push("invokers[" + _offset_map.get(fullQualifiedpPortType + "createPort") +
                            "]=invoker_create_client_port_for_" +
                            portName +
                            ";\n");
       _clientInvokers.push("invokers[" + _offset_map.get(fullQualifiedpPortType + "connectPort") +
                            "]=invoker_connect_client_dispatcher_" +
                            portName +
                            ";\n");
       _clientInvokers.push("invokers[" + _offset_map.get(fullQualifiedpPortType + "disconnectPort") +
                            "]=invoker_disconnect_client_dispatcher_" +
                            portName +
                            ";\n");
       _xmlInvokers.push("invokers[" + _offset_map.get(fullQualifiedpPortType + "createPort") +
                         "]=invoker_create_client_port_for_" +
                         portName +
                         ";\n");
       _xmlInvokers.push("invokers[" + _offset_map.get(fullQualifiedpPortType + "connectPort") +
                         "]=invoker_connect_client_dispatcher_" +
                         portName +
                         ";\n");
       _xmlInvokers.push("invokers[" + _offset_map.get(fullQualifiedpPortType + "disconnectPort") +
                         "]=invoker_disconnect_client_dispatcher_" +
                         portName +
                         ";\n");
     } catch (ASCoDTException e) {
       ErrorWriterDevice.getInstance().println(e);
     }
 
     _trace.out("inAUses(AUses)");
   }
 
   /**
    * Close the output streams.
    */
   @Override
   public void outAClassPackageElement(AClassPackageElement node) {
     Assert.isTrue(_templateFilesOfC2CxxProxyImplementation.size() == 1);
     Assert.isTrue(_templateFilesOfC2CxxProxyHeader.size() == 1);
     Assert.isTrue(_templateFilesOfTinyXMLImplementation.size() == 1);
     Assert.isTrue(_templateFilesOfTinyXMLHeader.size() == 1);
 
     String serverInvokers = "";
     String clientInvokers = "";
     String xmlInvokers = "";
 
     String parallelInvokers = "";
     while (!_serverInvokers.isEmpty()) {
       serverInvokers += _serverInvokers.peek();
       _serverInvokers.pop();
     }
     while (!_clientInvokers.isEmpty()) {
       clientInvokers += _clientInvokers.peek();
       _clientInvokers.pop();
     }
     while (!_xmlInvokers.isEmpty()) {
       xmlInvokers += _xmlInvokers.peek();
       _xmlInvokers.pop();
     }
     while (!_parallelWorkerInvokers.isEmpty()) {
       parallelInvokers += _parallelWorkerInvokers.peek();
       _parallelWorkerInvokers.pop();
     }
     _templateFilesOfC2CxxProxyImplementation.peek()
                                             .addMapping("__SET_SERVER_INVOKERS__",
                                                         serverInvokers);
     _templateFilesOfC2CxxProxyImplementation.peek()
                                             .addMapping("__SET_CLIENT_INVOKERS__",
                                                         clientInvokers);
     _templateFilesOfC2CxxProxyImplementation.peek()
                                             .addMapping("__SET_XML_INVOKERS__",
                                                         xmlInvokers);
     _templateFilesOfC2CxxProxyImplementation.peek()
                                             .addMapping("__SET_PARALLEL_WORKER_INVOKERS__",
                                                         parallelInvokers);
     _templateFilesOfC2CxxProxyImplementation.peek()
                                             .addMapping("__CLIENT_METHODS__",
                                                         "" + (2 + _offset_map.size()));
     _templateFilesOfC2CxxProxyImplementation.peek()
                                             .addMapping("__SERVER_METHODS__",
                                                         "" + (2 + _offset_map.size()));
     _templateFilesOfC2CxxProxyImplementation.peek().open();
     while (!_templateFilesUsesPortsForC2Cxx.isEmpty()) {
       _templateFilesUsesPortsForC2Cxx.peek().open();
       _templateFilesUsesPortsForC2Cxx.peek().close();
       _templateFilesUsesPortsForC2Cxx.pop();
     }
 
     while (!_templateFilesProvidesPorts.isEmpty()) {
       _templateFilesProvidesPorts.peek().open();
       _templateFilesProvidesPorts.peek().close();
       _templateFilesProvidesPorts.pop();
     }
 
     _templateFilesOfC2CxxProxyImplementation.peek().close();
     _templateFilesOfC2CxxProxyHeader.peek().close();
     _templateFilesOfTinyXMLImplementation.peek().close();
     _templateFilesOfTinyXMLHeader.peek().close();
     _templateFilesOfC2CxxProxyImplementation.pop();
     _templateFilesOfC2CxxProxyHeader.pop();
     _templateFilesOfTinyXMLImplementation.pop();
     _templateFilesOfTinyXMLHeader.pop();
   }
 }
