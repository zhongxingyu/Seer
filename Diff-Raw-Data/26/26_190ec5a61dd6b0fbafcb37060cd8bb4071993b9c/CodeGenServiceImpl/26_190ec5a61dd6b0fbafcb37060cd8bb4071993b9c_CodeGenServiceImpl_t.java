 package org.dhara.portal.web.codegenService;
 
 import freemarker.template.Configuration;
 import freemarker.template.Template;
 import freemarker.template.TemplateException;
 import org.apache.airavata.workflow.model.component.ws.WSComponentPort;
 import org.apache.airavata.workflow.model.wf.Workflow;
 import org.apache.airavata.workflow.model.wf.WorkflowInput;
 import org.dhara.portal.web.airavataService.AiravataClientAPIService;
 import org.dhara.portal.web.exception.PortalException;
 
 import java.io.*;
 import java.util.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: harsha
  * Date: 7/26/13
  * Time: 8:45 PM
  * To change this template use File | Settings | File Templates.
  */
 public class CodeGenServiceImpl implements CodeGenService{
     private AiravataClientAPIService airavataClientAPIService;
 
     /**
     * @see org.dhara.portal.web.codegenService.CodeGenService#getGeneratedClass(String) ()
     */
     public String getGeneratedClass(String workflowId) throws PortalException {
         Workflow workflow=airavataClientAPIService.getWorkflow(workflowId);
        List<WSComponentPort> workflowInputs = null;
         List<WSComponentPort> workflowOutputs = null;
 
         try {
            workflowInputs = workflow.getInputs();
             workflowOutputs = workflow.getOutputs();
         } catch (Exception e) {
             throw new PortalException("Error getting workflow with id="+workflowId,e);
         }
 
         List<String> inputIds=new ArrayList<String>();
         List<String> outputIds=new ArrayList<String>();
         Map<String,String> inputBindings=new HashMap<String, String>();
         Map<String,String> outputBindings=new HashMap<String, String>();
         ArrayList<CodeGenInOutAssociations> inputs=new ArrayList<CodeGenInOutAssociations>();
         ArrayList<CodeGenInOutAssociations> outputs=new ArrayList<CodeGenInOutAssociations>();
        for (WSComponentPort workflowInput : workflowInputs) {
             inputIds.add(workflowInput.getName());
             CodeGenInOutAssociations codeGenInOutAssociations=new CodeGenInOutAssociations();
            if("int".equalsIgnoreCase(workflowInput.getType().getLocalPart()) || "integer".equalsIgnoreCase(workflowInput.getType().getLocalPart()) || "IntegerParameterType".equalsIgnoreCase(workflowInput.getType().getLocalPart())) {
                 inputBindings.put(workflowInput.getName()+"_TypeClass", CodegenUtils.LITERAL_INT_BINDING);
                 codeGenInOutAssociations.setIdentifier(workflowInput.getName());
                 codeGenInOutAssociations.setMappingClass(CodegenUtils.LITERAL_INT_BINDING);
                 inputs.add(codeGenInOutAssociations);
            } else if ("string".equalsIgnoreCase(workflowInput.getType().getLocalPart()) || "StringParameterType".equalsIgnoreCase(workflowInput.getType().getLocalPart())) {
                 inputBindings.put(workflowInput.getName()+"_TypeClass", CodegenUtils.LITERAL_STRING_BINDING);
                 codeGenInOutAssociations.setIdentifier(workflowInput.getName());
                 codeGenInOutAssociations.setMappingClass(CodegenUtils.LITERAL_STRING_BINDING);
                 inputs.add(codeGenInOutAssociations);
            } else if ("short".equalsIgnoreCase(workflowInput.getType().getLocalPart()) || "ShortParameterType".equalsIgnoreCase(workflowInput.getType().getLocalPart())) {
                 inputBindings.put(workflowInput.getName()+"_TypeClass", CodegenUtils.LITERAL_SHORT_BINDING);
                 codeGenInOutAssociations.setIdentifier(workflowInput.getName());
                 codeGenInOutAssociations.setMappingClass(CodegenUtils.LITERAL_SHORT_BINDING);
                 inputs.add(codeGenInOutAssociations);
            } else if ("double".equalsIgnoreCase(workflowInput.getType().getLocalPart()) || "DoubleParameterType".equalsIgnoreCase(workflowInput.getType().getLocalPart())) {
                 inputBindings.put(workflowInput.getName()+"_TypeClass", CodegenUtils.LITERAL_DOUBLE_BINDING);
                 codeGenInOutAssociations.setIdentifier(workflowInput.getName());
                 codeGenInOutAssociations.setMappingClass(CodegenUtils.LITERAL_DOUBLE_BINDING);
                 inputs.add(codeGenInOutAssociations);
            } else if ("float".equalsIgnoreCase(workflowInput.getType().getLocalPart()) || "FloatParameterType".equalsIgnoreCase(workflowInput.getType().getLocalPart())) {
                 inputBindings.put(workflowInput.getName()+"_TypeClass", CodegenUtils.LITERAL_FLOAT_BINDING);
                 codeGenInOutAssociations.setIdentifier(workflowInput.getName());
                 codeGenInOutAssociations.setMappingClass(CodegenUtils.LITERAL_FLOAT_BINDING);
                 inputs.add(codeGenInOutAssociations);
            } else if ("boolean".equalsIgnoreCase(workflowInput.getType().getLocalPart()) || "BooleanParameterType".equalsIgnoreCase(workflowInput.getType().getLocalPart())) {
                 inputBindings.put(workflowInput.getName()+"_TypeClass", CodegenUtils.LITERAL_BOOLEAN_BINDING);
                 codeGenInOutAssociations.setIdentifier(workflowInput.getName());
                 codeGenInOutAssociations.setMappingClass(CodegenUtils.LITERAL_BOOLEAN_BINDING);
                 inputs.add(codeGenInOutAssociations);
             } else {
                 inputBindings.put(workflowInput.getName()+"_TypeClass", CodegenUtils.LITERAL_STRING_BINDING);
                 codeGenInOutAssociations.setIdentifier(workflowInput.getName());
                 codeGenInOutAssociations.setMappingClass(CodegenUtils.LITERAL_STRING_BINDING);
                 inputs.add(codeGenInOutAssociations);
             }
         }
 
         for (WSComponentPort workflowOutput : workflowOutputs) {
             CodeGenInOutAssociations codeGenInOutAssociations=new CodeGenInOutAssociations();
             outputIds.add(workflowOutput.getName());
             if("int".equalsIgnoreCase(workflowOutput.getType().getLocalPart()) || "integer".equalsIgnoreCase(workflowOutput.getType().getLocalPart()) || "IntegerParameterType".equalsIgnoreCase(workflowOutput.getType().getLocalPart())) {
                 outputBindings.put(workflowOutput.getName()+"_TypeClass", CodegenUtils.LITERAL_INT_BINDING);
                 codeGenInOutAssociations.setIdentifier(workflowOutput.getName());
                 codeGenInOutAssociations.setMappingClass(CodegenUtils.LITERAL_INT_BINDING);
                 outputs.add(codeGenInOutAssociations);
             } else if ("string".equalsIgnoreCase(workflowOutput.getType().getLocalPart()) || "StringParameterType".equalsIgnoreCase(workflowOutput.getType().getLocalPart())) {
                 outputBindings.put(workflowOutput.getName()+"_TypeClass", CodegenUtils.LITERAL_STRING_BINDING);
                 codeGenInOutAssociations.setIdentifier(workflowOutput.getName());
                 codeGenInOutAssociations.setMappingClass(CodegenUtils.LITERAL_STRING_BINDING);
                 outputs.add(codeGenInOutAssociations);
             } else if ("short".equalsIgnoreCase(workflowOutput.getType().getLocalPart()) || "ShortParameterType".equalsIgnoreCase(workflowOutput.getType().getLocalPart())) {
                 outputBindings.put(workflowOutput.getName()+"_TypeClass", CodegenUtils.LITERAL_SHORT_BINDING);
                 codeGenInOutAssociations.setIdentifier(workflowOutput.getName());
                 codeGenInOutAssociations.setMappingClass(CodegenUtils.LITERAL_SHORT_BINDING);
                 outputs.add(codeGenInOutAssociations);
             } else if ("double".equalsIgnoreCase(workflowOutput.getType().getLocalPart()) || "DoubleParameterType".equalsIgnoreCase(workflowOutput.getType().getLocalPart())) {
                 outputBindings.put(workflowOutput.getName()+"_TypeClass", CodegenUtils.LITERAL_DOUBLE_BINDING);
                 codeGenInOutAssociations.setIdentifier(workflowOutput.getName());
                 codeGenInOutAssociations.setMappingClass(CodegenUtils.LITERAL_DOUBLE_BINDING);
                 outputs.add(codeGenInOutAssociations);
             } else if ("float".equalsIgnoreCase(workflowOutput.getType().getLocalPart()) || "FloatParameterType".equalsIgnoreCase(workflowOutput.getType().getLocalPart())) {
                 outputBindings.put(workflowOutput.getName()+"_TypeClass", CodegenUtils.LITERAL_FLOAT_BINDING);
                 codeGenInOutAssociations.setIdentifier(workflowOutput.getName());
                 codeGenInOutAssociations.setMappingClass(CodegenUtils.LITERAL_FLOAT_BINDING);
                 outputs.add(codeGenInOutAssociations);
             } else if ("boolean".equalsIgnoreCase(workflowOutput.getType().getLocalPart()) || "BooleanParameterType".equalsIgnoreCase(workflowOutput.getType().getLocalPart())) {
                 outputBindings.put(workflowOutput.getName()+"_TypeClass", CodegenUtils.LITERAL_BOOLEAN_BINDING);
                 codeGenInOutAssociations.setIdentifier(workflowOutput.getName());
                 codeGenInOutAssociations.setMappingClass(CodegenUtils.LITERAL_BOOLEAN_BINDING);
                 outputs.add(codeGenInOutAssociations);
             } else {
                 outputBindings.put(workflowOutput.getName()+"_TypeClass", CodegenUtils.LITERAL_STRING_BINDING);
                 codeGenInOutAssociations.setIdentifier(workflowOutput.getName());
                 codeGenInOutAssociations.setMappingClass(CodegenUtils.LITERAL_STRING_BINDING);
                 outputs.add(codeGenInOutAssociations);
             }
         }
         String classContents=generateClassFromTemplate(inputIds,outputIds,workflow.getName(),CodegenUtils.defaultExtendingClass,inputBindings,outputBindings,inputs,outputs);
         return classContents;
     }
 
     /**
      * @see org.dhara.portal.web.codegenService.CodeGenService#getGeneratedClassForCustomDeployment(String, java.util.Map, java.util.Map, String) ()
      */
     public String getGeneratedClassForCustomDeployment(String workflowId, Map<String, String> inputsMapping, Map<String, String> outputsMapping, String extendingAlgorithm) throws PortalException {
         Workflow workflow=airavataClientAPIService.getWorkflow(workflowId);
         List<WorkflowInput> workflowInputs = null;
         List<WSComponentPort> workflowOutputs = null;
 
         try {
             workflowInputs = workflow.getWorkflowInputs();
             workflowOutputs = workflow.getOutputs();
         } catch (Exception e) {
             throw new PortalException("Error getting workflow with id="+workflowId,e);
         }
 
         List<String> inputIds=new ArrayList<String>();
         List<String> outputIds=new ArrayList<String>();
         Map<String,String> inputBindings=new HashMap<String, String>();
         Map<String,String> outputBindings=new HashMap<String, String>();
         ArrayList<CodeGenInOutAssociations> inputs=new ArrayList<CodeGenInOutAssociations>();
         ArrayList<CodeGenInOutAssociations> outputs=new ArrayList<CodeGenInOutAssociations>();
 
         for (WorkflowInput workflowInput : workflowInputs) {
             inputIds.add(workflowInput.getName());
             CodeGenInOutAssociations codeGenInOutAssociations=new CodeGenInOutAssociations();
             if("int".equalsIgnoreCase(inputsMapping.get(workflowInput.getName())) || "integer".equalsIgnoreCase(inputsMapping.get(workflowInput.getName())) || "IntegerParameterType".equalsIgnoreCase(inputsMapping.get(workflowInput.getName()))) {
                 inputBindings.put(workflowInput.getName()+"_TypeClass", CodegenUtils.LITERAL_INT_BINDING);
                 codeGenInOutAssociations.setIdentifier(workflowInput.getName());
                 codeGenInOutAssociations.setMappingClass(CodegenUtils.LITERAL_INT_BINDING);
                 inputs.add(codeGenInOutAssociations);
             } else if ("string".equalsIgnoreCase(inputsMapping.get(workflowInput.getName())) || "StringParameterType".equalsIgnoreCase(inputsMapping.get(workflowInput.getName()))) {
                 inputBindings.put(workflowInput.getName()+"_TypeClass", CodegenUtils.LITERAL_STRING_BINDING);
                 codeGenInOutAssociations.setIdentifier(workflowInput.getName());
                 codeGenInOutAssociations.setMappingClass(CodegenUtils.LITERAL_STRING_BINDING);
                 inputs.add(codeGenInOutAssociations);
             } else if ("short".equalsIgnoreCase(inputsMapping.get(workflowInput.getName())) || "ShortParameterType".equalsIgnoreCase(inputsMapping.get(workflowInput.getName()))) {
                 inputBindings.put(workflowInput.getName()+"_TypeClass", CodegenUtils.LITERAL_SHORT_BINDING);
                 codeGenInOutAssociations.setIdentifier(workflowInput.getName());
                 codeGenInOutAssociations.setMappingClass(CodegenUtils.LITERAL_SHORT_BINDING);
                 inputs.add(codeGenInOutAssociations);
             } else if ("double".equalsIgnoreCase(inputsMapping.get(workflowInput.getName())) || "DoubleParameterType".equalsIgnoreCase(inputsMapping.get(workflowInput.getName()))) {
                 inputBindings.put(workflowInput.getName()+"_TypeClass", CodegenUtils.LITERAL_DOUBLE_BINDING);
                 codeGenInOutAssociations.setIdentifier(workflowInput.getName());
                 codeGenInOutAssociations.setMappingClass(CodegenUtils.LITERAL_DOUBLE_BINDING);
                 inputs.add(codeGenInOutAssociations);
             } else if ("float".equalsIgnoreCase(inputsMapping.get(workflowInput.getName())) || "FloatParameterType".equalsIgnoreCase(inputsMapping.get(workflowInput.getName()))) {
                 inputBindings.put(workflowInput.getName()+"_TypeClass", CodegenUtils.LITERAL_FLOAT_BINDING);
                 codeGenInOutAssociations.setIdentifier(workflowInput.getName());
                 codeGenInOutAssociations.setMappingClass(CodegenUtils.LITERAL_FLOAT_BINDING);
                 inputs.add(codeGenInOutAssociations);
             } else if ("boolean".equalsIgnoreCase(inputsMapping.get(workflowInput.getName())) || "BooleanParameterType".equalsIgnoreCase(inputsMapping.get(workflowInput.getName()))) {
                 inputBindings.put(workflowInput.getName()+"_TypeClass", CodegenUtils.LITERAL_BOOLEAN_BINDING);
                 codeGenInOutAssociations.setIdentifier(workflowInput.getName());
                 codeGenInOutAssociations.setMappingClass(CodegenUtils.LITERAL_BOOLEAN_BINDING);
                 inputs.add(codeGenInOutAssociations);
             } else {
                 inputBindings.put(workflowInput.getName()+"_TypeClass", CodegenUtils.LITERAL_STRING_BINDING);
                 codeGenInOutAssociations.setIdentifier(workflowInput.getName());
                 codeGenInOutAssociations.setMappingClass(CodegenUtils.LITERAL_STRING_BINDING);
                 inputs.add(codeGenInOutAssociations);
             }
         }
 
         for (WSComponentPort workflowOutput : workflowOutputs) {
             CodeGenInOutAssociations codeGenInOutAssociations=new CodeGenInOutAssociations();
             outputIds.add(workflowOutput.getName());
             if("int".equalsIgnoreCase(inputsMapping.get(workflowOutput.getName())) || "integer".equalsIgnoreCase(inputsMapping.get(workflowOutput.getName())) || "IntegerParameterType".equalsIgnoreCase(inputsMapping.get(workflowOutput.getName()))) {
                 outputBindings.put(workflowOutput.getName()+"_TypeClass", CodegenUtils.LITERAL_INT_BINDING);
                 codeGenInOutAssociations.setIdentifier(workflowOutput.getName());
                 codeGenInOutAssociations.setMappingClass(CodegenUtils.LITERAL_INT_BINDING);
                 outputs.add(codeGenInOutAssociations);
             } else if ("string".equalsIgnoreCase(inputsMapping.get(workflowOutput.getName())) || "StringParameterType".equalsIgnoreCase(inputsMapping.get(workflowOutput.getName()))) {
                 outputBindings.put(workflowOutput.getName()+"_TypeClass", CodegenUtils.LITERAL_STRING_BINDING);
                 codeGenInOutAssociations.setIdentifier(workflowOutput.getName());
                 codeGenInOutAssociations.setMappingClass(CodegenUtils.LITERAL_STRING_BINDING);
                 outputs.add(codeGenInOutAssociations);
             } else if ("short".equalsIgnoreCase(inputsMapping.get(workflowOutput.getName())) || "ShortParameterType".equalsIgnoreCase(inputsMapping.get(workflowOutput.getName()))) {
                 outputBindings.put(workflowOutput.getName()+"_TypeClass", CodegenUtils.LITERAL_SHORT_BINDING);
                 codeGenInOutAssociations.setIdentifier(workflowOutput.getName());
                 codeGenInOutAssociations.setMappingClass(CodegenUtils.LITERAL_SHORT_BINDING);
                 outputs.add(codeGenInOutAssociations);
             } else if ("double".equalsIgnoreCase(inputsMapping.get(workflowOutput.getName())) || "DoubleParameterType".equalsIgnoreCase(inputsMapping.get(workflowOutput.getName()))) {
                 outputBindings.put(workflowOutput.getName()+"_TypeClass", CodegenUtils.LITERAL_DOUBLE_BINDING);
                 codeGenInOutAssociations.setIdentifier(workflowOutput.getName());
                 codeGenInOutAssociations.setMappingClass(CodegenUtils.LITERAL_DOUBLE_BINDING);
                 outputs.add(codeGenInOutAssociations);
             } else if ("float".equalsIgnoreCase(inputsMapping.get(workflowOutput.getName())) || "FloatParameterType".equalsIgnoreCase(inputsMapping.get(workflowOutput.getName()))) {
                 outputBindings.put(workflowOutput.getName()+"_TypeClass", CodegenUtils.LITERAL_FLOAT_BINDING);
                 codeGenInOutAssociations.setIdentifier(workflowOutput.getName());
                 codeGenInOutAssociations.setMappingClass(CodegenUtils.LITERAL_FLOAT_BINDING);
                 outputs.add(codeGenInOutAssociations);
             } else if ("boolean".equalsIgnoreCase(inputsMapping.get(workflowOutput.getName())) || "BooleanParameterType".equalsIgnoreCase(inputsMapping.get(workflowOutput.getName()))) {
                 outputBindings.put(workflowOutput.getName()+"_TypeClass", CodegenUtils.LITERAL_BOOLEAN_BINDING);
                 codeGenInOutAssociations.setIdentifier(workflowOutput.getName());
                 codeGenInOutAssociations.setMappingClass(CodegenUtils.LITERAL_BOOLEAN_BINDING);
                 outputs.add(codeGenInOutAssociations);
             } else {
                 outputBindings.put(workflowOutput.getName()+"_TypeClass", CodegenUtils.LITERAL_STRING_BINDING);
                 codeGenInOutAssociations.setIdentifier(workflowOutput.getName());
                 codeGenInOutAssociations.setMappingClass(CodegenUtils.LITERAL_STRING_BINDING);
                 outputs.add(codeGenInOutAssociations);
             }
         }
 
         String classContents=generateClassFromTemplate(inputIds,outputIds,workflow.getName(),extendingAlgorithm,inputBindings,outputBindings,inputs,outputs);
         return classContents;
     }
 
     private String generateClassFromTemplate(List<String> inputIdentifiersList,List<String> outputIdentifiersList, String className,String extendingClass, Map<String,String> inputBindingsList,
                                              Map<String,String> outputBindingsList, ArrayList<CodeGenInOutAssociations> inputMappings,ArrayList<CodeGenInOutAssociations> outputMappings) throws PortalException {
         Configuration cfg = new Configuration();
         try {
             //Load template from source folder
             //String current = new java.io.File( "." ).getCanonicalPath();
             //fg.setDirectoryForTemplateLoading(new File(
             //current));
             File file1=new File(System.getProperty("catalina.base")+File.separator+"webapps"+File.separator+"portal"+File.separator+"WEB-INF"+File.separator+"templates");
             cfg.setDirectoryForTemplateLoading(file1);
             Template template = cfg.getTemplate("wpstemplate.ftl");
 
             // Build the data-model
             Map<String, Object> data = new HashMap<String, Object>();
             data.put("package", CodegenUtils.defaultPackage);
             data.put("processName", className);
             data.put("extendingClass", extendingClass);
 
             //List parsing
             List<String> imports = CodegenUtils.getImports();
             List<String> inputIdentifiers = inputIdentifiersList;
             List<String> outputIdentifiers = outputIdentifiersList;
 
             data.put("imports", imports);
             data.put("inputIdentifiers", inputIdentifiers);
             data.put("outputIdentifiers", outputIdentifiers);
             data.put("inputMappings",inputMappings);
             data.put("outputMappings",outputMappings);
             data.put("executionServlet",CodegenUtils.EXECUTION_SERVLET);
 
             Map<String,String> inputBindings =inputBindingsList;
             Set<Map.Entry<String, String>> entriesIn = inputBindings.entrySet();
             for ( Map.Entry<String, String> entry : entriesIn ) {
                 if (!data.containsKey(entry.getKey())) {
                     data.put(entry.getKey(), entry.getValue());
                 }
             }
 
             Map<String,String> outputBindings =outputBindingsList;
             Set<Map.Entry<String, String>> entriesOut = inputBindings.entrySet();
             for ( Map.Entry<String, String> entry : entriesOut ) {
                 if (!data.containsKey(entry.getKey())) {
                     data.put( entry.getKey(), entry.getValue() );
                 }
             }
 
 
             Writer file = new FileWriter(new File(className+".java"));
             template.process(data, file);
             file.flush();
             file.close();
 
             StringBuilder builder=new StringBuilder();
             String currentLine;
             BufferedReader br;
             br = new BufferedReader(new FileReader(className+".java"));
             while ((currentLine = br.readLine()) != null) {
                 builder.append(currentLine);
                 builder.append(System.getProperty("line.separator"));
             }
                 return builder.toString();
 
         } catch (IOException e) {
             throw new PortalException("Error while file operations",e);
         } catch (TemplateException e) {
             throw new PortalException("Template exception when generating the template");
         }
     }
     public AiravataClientAPIService getAiravataClientAPIService() {
         return airavataClientAPIService;
     }
 
     public void setAiravataClientAPIService(AiravataClientAPIService airavataClientAPIService) {
         this.airavataClientAPIService = airavataClientAPIService;
     }
 }
