 package it.italiangrid.portal.dirac.model;
 
 import it.italiangrid.portal.dirac.admin.DiracAdminUtil;
 import it.italiangrid.portal.dirac.db.domain.JobJdls;
 import it.italiangrid.portal.dirac.exception.DiracException;
 import it.italiangrid.portal.dirac.util.DiracConfig;
 import it.italiangrid.portal.dirac.util.DiracUtil;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.nio.CharBuffer;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 import org.apache.log4j.Logger;
 
 import com.liferay.portal.kernel.util.FileUtil;
 
 public class Jdl {
 	
 	private static final Logger log = Logger.getLogger(Jdl.class);
 
 	private String path;
 	private String jobName;
 	private String executable;
 	private String arguments;
 	private List<String> inputSandbox;
 	private String outputSandboxRequest;
 	private List<String> outputSandbox;
 	private List<String> outputSandboxDestUri;
 	private String stdOutput;
 	private String stdError;
 	private List<String> inputData;
 	private String outputSE;
 	private List<List<String>> outputData;
 	private String outputPath;
 	private String parameters;
 	private String parameterStart;
 	private String parameterStep;
 	private String cpuNumber;
 	private String hostNumber;
 	private String wholeNodes;
 	private String smpGranularity;
 	private String vo;
 	private String requirements;
 	private String myProxyServer;
 	private String site;
 	private List<String> parameterNames = Arrays.asList(new String[]{ "jobName", "executable", "arguments",
 		"inputSandbox", "outputSandboxRequest", "outputSandbox", "outputSandboxDestUri", "stdOutput", "stdError",
 		"inputData", "outputSE", "outputData", "outputPath", "parameters", "parameterStart",
 		"parameterStep", "cpuNumber", "hostNumber", "wholeNodes", "smpGranularity", "vo", "requirements", "myProxyServer", "path", "site"});
 	private List<String> parameterJDLNames = Arrays.asList(new String[]{ "JobName", "Executable", "Arguments",
 			"InputSandbox", "OutputSandboxRequest", "OutputSandbox", "OutputSandboxDestUri", "StdOutput", "StdError",
 			"InputData", "OutputSE", "OutputData", "OutputPath", "Parameters", "ParameterStart",
 			"ParameterStep", "CPUNumber", "HostNumber", "WholeNodes", "SMPGranularity", "VirtualOrganization", "Requirements", "MyProxyServer", "path", "Site"});
 
 	/**
 	 * Default constructor.
 	 */
 	public Jdl() {
 		this.jobName = "Portal_Job";
 		this.executable = "/bin/ls";
 		this.arguments = "-latr";
 		this.stdOutput = "StdOut";
 		this.stdError = "StdErr";
 		this.path = "";
 	}
 
 	/**
 	 * 
 	 * @param jobName
 	 * @param executable
 	 * @param arguments
 	 * @param inputSandbox
 	 * @param outputSandbox
 	 * @param outputSandboxDestUri
 	 * @param stdOutput
 	 * @param stdError
 	 * @param inputData
 	 * @param outputSE
 	 * @param outputData
 	 * @param outputPath
 	 * @param parameters
 	 * @param paramenterStart
 	 * @param parameterStep
 	 * @param cpuNumber
 	 * @param hostNumber
 	 * @param wholeNodes
 	 * @param smpGranularity
 	 */
 	public Jdl(String jobName, String executable, String arguments,
 			List<String> inputSandbox, List<String> outputSandbox,
 			List<String> outputSandboxDestUri, String stdOutput,
 			String stdError, List<String> inputData, String outputSE,
 			List<List<String>> outputData, String outputPath,
 			String parameters, String paramenterStart,
 			String parameterStep, String cpuNumber, String hostNumber,
 			String wholeNodes, String smpGranularity, String vo) {
 		super();
 		this.jobName = jobName;
 		this.executable = executable;
 		this.arguments = arguments;
 		this.inputSandbox = inputSandbox;
 		this.outputSandbox = outputSandbox;
 		this.outputSandboxDestUri = outputSandboxDestUri;
 		this.stdOutput = stdOutput;
 		this.stdError = stdError;
 		this.inputData = inputData;
 		this.outputSE = outputSE;
 		this.outputData = outputData;
 		this.outputPath = outputPath;
 		this.parameters = parameters;
 		this.parameterStart = paramenterStart;
 		this.parameterStep = parameterStep;
 		this.cpuNumber = cpuNumber;
 		this.hostNumber = hostNumber;
 		this.wholeNodes = wholeNodes;
 		this.smpGranularity = smpGranularity;
 		this.vo = vo;
 	}
 
 	/**
 	 * @return the path
 	 */
 	public String getPath() {
 		return path;
 	}
 
 	/**
 	 * @param path the path to set
 	 */
 	public void setPath(String path) {
 		this.path = path;
 	}
 
 	/**
 	 * @return the jobName
 	 */
 	public String getJobName() {
 		return jobName;
 	}
 
 	/**
 	 * @param jobName
 	 *            the jobName to set
 	 */
 	public void setJobName(String jobName) {
 		this.jobName = jobName;
 	}
 
 	/**
 	 * @return the executable
 	 */
 	public String getExecutable() {
 		return executable;
 	}
 
 	/**
 	 * @param executable
 	 *            the executable to set
 	 */
 	public void setExecutable(String executable) {
 		this.executable = executable;
 	}
 
 	/**
 	 * @return the arguments
 	 */
 	public String getArguments() {
 		return arguments;
 	}
 
 	/**
 	 * @param arguments
 	 *            the arguments to set
 	 */
 	public void setArguments(String arguments) {
 		this.arguments = arguments;
 	}
 
 	/**
 	 * @return the inputSandbox
 	 */
 	public List<String> getInputSandbox() {
 		return inputSandbox;
 	}
 
 	/**
 	 * @param inputSandbox
 	 *            the inputSandbox to set
 	 */
 	public void setInputSandbox(List<String> inputSandbox) {
 		this.inputSandbox = inputSandbox;
 	}
 
 	/**
 	 * @return the outputSandboxRequest
 	 */
 	public String getOutputSandboxRequest() {
 		return outputSandboxRequest;
 	}
 
 	/**
 	 * @param outputSandboxRequest
 	 *            the outputSandboxRequest to set
 	 */
 	public void setOutputSandboxRequest(String outputSandboxRequest) {
 		this.outputSandboxRequest = outputSandboxRequest;
 	}
 
 	/**
 	 * @return the outputSandbox
 	 */
 	public List<String> getOutputSandbox() {
 		return outputSandbox;
 	}
 
 	/**
 	 * @param outputSandbox
 	 *            the outputSandbox to set
 	 */
 	public void setOutputSandbox(List<String> outputSandbox) {
 		this.outputSandbox = outputSandbox;
 	}
 
 	/**
 	 * @return the outputSandboxDestUri
 	 */
 	public List<String> getOutputSandboxDestUri() {
 		return outputSandboxDestUri;
 	}
 
 	/**
 	 * @param outputSandboxDestUri
 	 *            the outputSandboxDestUri to set
 	 */
 	public void setOutputSandboxDestUri(List<String> outputSandboxDestUri) {
 		this.outputSandboxDestUri = outputSandboxDestUri;
 	}
 
 	/**
 	 * @return the stdOutput
 	 */
 	public String getStdOutput() {
 		return stdOutput;
 	}
 
 	/**
 	 * @param stdOutput
 	 *            the stdOutput to set
 	 */
 	public void setStdOutput(String stdOutput) {
 		this.stdOutput = stdOutput;
 	}
 
 	/**
 	 * @return the stdError
 	 */
 	public String getStdError() {
 		return stdError;
 	}
 
 	/**
 	 * @param stdError
 	 *            the stdError to set
 	 */
 	public void setStdError(String stdError) {
 		this.stdError = stdError;
 	}
 
 	/**
 	 * @return the inputData
 	 */
 	public List<String> getInputData() {
 		return inputData;
 	}
 
 	/**
 	 * @param inputData
 	 *            the inputData to set
 	 */
 	public void setInputData(List<String> inputData) {
 		this.inputData = inputData;
 	}
 
 	/**
 	 * @return the outputSE
 	 */
 	public String getOutputSE() {
 		return outputSE;
 	}
 
 	/**
 	 * @param outputSE
 	 *            the outputSE to set
 	 */
 	public void setOutputSE(String outputSE) {
 		this.outputSE = outputSE;
 	}
 
 	/**
 	 * @return the outputData
 	 */
 	public List<List<String>> getOutputData() {
 		return outputData;
 	}
 
 	/**
 	 * @param outputData
 	 *            the outputData to set
 	 */
 	public void setOutputData(List<List<String>> outputData) {
 		this.outputData = outputData;
 	}
 
 	/**
 	 * @return the outputPath
 	 */
 	public String getOutputPath() {
 		return outputPath;
 	}
 
 	/**
 	 * @param outputPath
 	 *            the outputPath to set
 	 */
 	public void setOutputPath(String outputPath) {
 		this.outputPath = outputPath;
 	}
 
 	/**
 	 * @return the parameters
 	 */
 	public String getParameters() {
 		return parameters;
 	}
 
 	/**
 	 * @param parameters
 	 *            the parameters to set
 	 */
 	public void setParameters(String parameters) {
 		this.parameters = parameters;
 	}
 
 	/**
 	 * @return the parameterStart
 	 */
 	public String getParameterStart() {
 		return parameterStart;
 	}
 
 	/**
 	 * @param parameterStart
 	 *            the parameterStart to set
 	 */
 	public void setParameterStart(String parameterStart) {
 		this.parameterStart = parameterStart;
 	}
 
 	/**
 	 * @return the parameterStep
 	 */
 	public String getParameterStep() {
 		return parameterStep;
 	}
 
 	/**
 	 * @param parameterStep
 	 *            the parameterStep to set
 	 */
 	public void setParameterStep(String parameterStep) {
 		this.parameterStep = parameterStep;
 	}
 
 	/**
 	 * @return the cpuNumber
 	 */
 	public String getCpuNumber() {
 		return cpuNumber;
 	}
 
 	/**
 	 * @param cpuNumber the cpuNumber to set
 	 */
 	public void setCpuNumber(String cpuNumber) {
 		this.cpuNumber = cpuNumber;
 	}
 
 	/**
 	 * @return the hostNumber
 	 */
 	public String getHostNumber() {
 		return hostNumber;
 	}
 
 	/**
 	 * @param hostNumber the hostNumber to set
 	 */
 	public void setHostNumber(String hostNumber) {
 		this.hostNumber = hostNumber;
 	}
 
 	/**
 	 * @return the wholeNode
 	 */
 	public String getWholeNodes() {
 		return wholeNodes;
 	}
 
 	/**
 	 * @param wholeNode the wholeNode to set
 	 */
 	public void setWholeNodes(String wholeNodes) {
 		this.wholeNodes = wholeNodes;
 	}
 
 	/**
 	 * @return the smpGranularity
 	 */
 	public String getSmpGranularity() {
 		return smpGranularity;
 	}
 
 	/**
 	 * @param smpGranularity the smpGranularity to set
 	 */
 	public void setSmpGranularity(String smpGranularity) {
 		this.smpGranularity = smpGranularity;
 	}
 
 	/**
 	 * @return the vo
 	 */
 	public String getVo() {
 		return vo;
 	}
 
 	/**
 	 * @param vo the vo to set
 	 */
 	public void setVo(String vo) {
 		this.vo = vo;
 	}
 
 	/**
 	 * @return the requirements
 	 */
 	public String getRequirements() {
 		return requirements;
 	}
 
 	/**
 	 * @param requirements the requirements to set
 	 */
 	public void setRequirements(String requirements) {
 		this.requirements = requirements;
 	}
 
 	/**
 	 * @return the myProxyServer
 	 */
 	public String getMyProxyServer() {
 		return myProxyServer;
 	}
 
 	/**
 	 * @param myProxyServer the myProxyServer to set
 	 */
 	public void setMyProxyServer(String myProxyServer) {
 		this.myProxyServer = myProxyServer;
 	}
 
 	/**
 	 * @return the site
 	 */
 	public String getSite() {
 		return site;
 	}
 
 	/**
 	 * @param site the site to set
 	 */
 	public void setSite(String site) {
 		this.site = site;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		String string = "\n";
 		
 //		if (parameters != null) {
 //			string += "JobType = \"Parametric\";\n";
 //		}else{
 //			string += "JobType = \"Normal\";\n";
 //		}
 		if (jobName != null) {
 			string += "JobName = \"" + jobName + "\";\n";
 		}
 		if (executable != null) {
 			string += "Executable = \"" + executable + "\";\n";
 		}
 		if (arguments != null && !arguments.isEmpty()) {
 			string += "Arguments = \"" + arguments + "\";\n";
 		}
 		if (vo != null && !vo.isEmpty()) {
 			string += "VirtualOrganization = \"" + vo + "\";\n";
 		}
 		if (stdOutput != null) {
 			string += "StdOutput = \"" + stdOutput + "\";\n";
 		}
 		if (stdError != null) {
 			string += "StdError = \"" + stdError + "\";\n";
 		}
 		if (inputSandbox != null) {
 			string += "InputSandbox = {";
 			for (String s : inputSandbox) {
 				string += "\"" + s + "\",";
 			}
 
 			string = string.substring(0, string.length() - 1);
 
 			string += "};\n";
 		}
 		if (outputSandbox != null) {
 			string += "OutputSandbox = {";
 			for (String s : outputSandbox) {
 				string += "\"" + s + "\",";
 			}
 
 			string = string.substring(0, string.length() - 1);
 
 			string += "};\n";
 		}
 		if (outputSandboxDestUri != null) {
 			string += "OutputSandboxDestURI = {";
 			for (String s : outputSandboxDestUri) {
 				string += "\"" + s + "\", ";
 			}
 
 			string = string.substring(0, string.length() - 1);
 
 			string += "};\n";
 		}
 		if (inputData != null) {
 			string += "InputData = {";
 			for (String s : inputData) {
 				string += "\"" + s + "\",";
 			}
 
 			string = string.substring(0, string.length() - 2);
 
 			string += "};\n";
 		}
 		if (outputSE != null && !outputSE.isEmpty() && !outputSE.contains("null")) {
 			string += "OutputSE = \"" + outputSE + "\";\n";
 		}
 		if (outputData != null) {
 			string += "OutputData = {";
 			for (List<String> list : outputData) {
 				string += "[";
 				if (!list.get(0).equals("noData")) {
 					string += "OutputFile = " + list.get(0) + ";";
 				}
 				if (!list.get(1).equals("noData")) {
 					string += "StorageElement = " + list.get(1) + ";";
 				}
 				if (!list.get(2).equals("noData")) {
 					string += "LogicalFileName = " + list.get(2) + ";";
 				}
 				string = string.substring(0, string.length() - 2);
 				string += "],";
 			}
 
 			string = string.substring(0, string.length() - 2);
 
 			string += "};\n";
 		}
 		if (parameters != null) {
 			if (parameters.matches("((-|\\+)?[0-9]+(\\.[0-9]+)?)+")) {
 				string += "Parameters = " + parameters + ";\n";
 				if (parameterStart != null) {
 					string += "ParameterStart = " + parameterStart + ";\n";
 				}
 				if (parameterStep != null) {
 					string += "ParameterStep = " + parameterStep + ";\n";
 				}
 			} else {
 				if(!parameters.isEmpty()){
 					string += "Parameters = {";
 					for (String s : parameters.split(";")) {
 						string += "\"" + s + "\",";
 					}
 	
 					string = string.substring(0, string.length() - 2);
 	
 					string += "};\n";
 				}
 			}
 			string += "OutputSE = \"" + outputSE + "\";\n";
 		}
 		if (cpuNumber != null && !cpuNumber.isEmpty()) {
 			string += "CPUNumber = " + cpuNumber + ";\n";
 		}
 		if (hostNumber != null && !hostNumber.isEmpty()) {
 			string += "HostNumber = " + hostNumber + ";\n";
 		}
 		if (wholeNodes != null && !wholeNodes.isEmpty()) {
 			string += "WholeNodes = " + wholeNodes + ";\n";
 		}
 		if (smpGranularity != null && !smpGranularity.isEmpty()) {
 			string += "SMPGranularity = " + smpGranularity + ";\n";
 		}
 		if (requirements != null && !requirements.isEmpty()) {
 			string += "Requirements = " + requirements + ";\n";
 		}
 		if (myProxyServer != null && !myProxyServer.isEmpty()) {
 			string += "MyProxyServer = \"" + myProxyServer + "\";\n";
 		}
		if (site != null && !site.isEmpty() && !site.equals("LCG.ANY.it")) {
 			string += "Site = \"" + site + "\";\n";
 		}
 		string+="\n";
 		return string;
 	}
 
 	
 	public void setParameter(String parameter, Object value) {
 		int index = parameterNames.indexOf(parameter);
 		switch(index){
 		case 0: this.jobName = (String) value; break;
 		case 1: this.executable = (String) value; break;
 		case 2: this.arguments = (String) value; break;
 //		case 3: this.inputSandbox = (List<String>) value; break;
 		case 4: this.outputSandboxRequest = (String) value; break;
 //		case 5: this.outputSandbox = (List<String>) value; break;
 //		case 6: this.outputSandboxDestUri = (List<String>) value; break;
 		case 7: this.stdOutput = (String) value; break;
 		case 8: this.stdError = (String) value; break;
 //		case 9: this.inputData = (List<String>) value; break;
 		case 10: this.outputSE = (String) value; break;
 //		case 11: this.outputData = (List<List<String>>) value; break;
 		case 12: this.outputPath = (String) value; break;
 		case 13: this.parameters = (String) value; break;
 		case 14: this.parameterStart = (String) value; break;
 		case 15: this.parameterStep = (String) value; break;
 		case 16: this.cpuNumber = (String) value; break;
 		case 17: this.hostNumber = (String) value; break;
 		case 18: this.wholeNodes = (String) value; break;
 		case 19: this.smpGranularity = (String) value; break;
 		case 20: this.vo = (String) value; break;
 		case 21: this.requirements = (String) value; break;
 		case 22: this.myProxyServer = (String) value; break;
 		case 23: this.path = (String) value; break;
 		case 24: this.site = (String) value; break;
 		}
 	}
 	public void copyJob(JobJdls diracJdl, long userId) throws DiracException, IOException {
 		String diracJdlString = new String(diracJdl.getJdl());
 		copyJob(diracJdlString, userId, false, null);
 	}
 
 	public void copyJob(String diracJdlString, long userId, boolean isTemplate, String templatePath) throws DiracException, IOException {
 	
 		if(isTemplate){
 			diracJdlString = loadFromFile(templatePath);
 		}
 		
 		log.info(diracJdlString);
 		
 		this.jobName = "Portal_Job";
 		this.executable = "";
 		this.arguments = "";
 		this.stdOutput = "";
 		this.stdError = "";
 		this.path = "";
 		
 		String newJdl = "";
 		boolean parentesi = false;
 		int deep = 0;
 		
 		diracJdlString = diracJdlString.replaceAll(" {2,}", " ");
 		
 		for(int i = 1; i < diracJdlString.length()-1; i++){
 			
 			if(diracJdlString.charAt(i) == '{' || diracJdlString.charAt(i) == '['){
 				parentesi = true;
 				deep++;
 				
 				if(newJdl.charAt(newJdl.length()-1) == '\n')
 						newJdl = newJdl.substring(0, newJdl.length()-2);
 			}
 			
 			if(!(diracJdlString.charAt(i) == '\n' && parentesi))
 				newJdl += diracJdlString.charAt(i);
 			
 			if(diracJdlString.charAt(i) == '}' || diracJdlString.charAt(i) == ']'){
 				deep--;
 				if(deep == 0)
 					parentesi = false;
 			}
 			if(diracJdlString.charAt(i) == '\n' && (diracJdlString.charAt(i+1) == ' '))
 				i++;
 		}
 		
 		newJdl = newJdl.replaceAll(" {2,}", " ");
 		
 		log.info(newJdl);
 		
 		CharBuffer cb = CharBuffer.allocate(newJdl.length());
 		
 		for(int i = 0; i < newJdl.length(); i++){
 			cb.put(newJdl.charAt(i));
 		}
 		
 		cb.flip();
 		
 		String row = null;
 		String[] values;
 		boolean haveWrapper = false;
 		
 		for (String key : parameterJDLNames) {
 			
 			row = grep(cb, key);
 			if(row != null){
 				if(!row.contains("JobRequirements")){
 
 					log.info(key + " fuounded with value : " + row);
 					String value = row.replaceAll(key, "");
 					
 					value = value.substring(value.indexOf("=")+2, value.length()-2);
 					
 					switch(parameterJDLNames.indexOf(key)){
 					
 					case 5: /* OutputSanbox */
 						value= value.replaceAll("\"", "");
 						value= value.replace("{", "");
 						value= value.replace("}", "");
 						values = value.split(",");
 						this.outputSandbox = Arrays.asList(values);
 						break;
 						
 					case 3: /* InputSandbox */
 						value= value.replaceAll("\"", "");
 						value= value.replace("{", "");
 						value= value.replace("}", "");
 						values = value.split(",");
 						List<String> inputs = Arrays.asList(values);
 						
 						
 						this.inputSandbox = new ArrayList<String>();
 						
 						for (String input : inputs) {
 							if(!input.startsWith("SB:ProductionSandboxSE|"))
 								this.inputSandbox.add(input);
 						}
 						
 						break;
 					case 1: /* Executable */
 						value= value.replaceAll("\"", "");
 						if(value.equals(DiracConfig.getProperties("Dirac.properties", "dirac.wrapper.script"))){
 							haveWrapper = true;
 						}
 					default:
 						if(value.contains("{")&&value.contains("}")){
 							value= value.replaceAll("\"", "");
 							value= value.replace("{", "");
 							value= value.replace("}", "");
 						}else{
 							value= value.replaceAll("\"", "");
 						}
 						setParameter(parameterNames.get(parameterJDLNames.indexOf(key)), value);
 					}
 				
 					log.info("Value : " + value);
 					
 					
 				}
 			}
 		}
 		
 		
 		
 		if(haveWrapper){
 			this.executable = this.inputSandbox.get(1).substring(this.inputSandbox.get(1).lastIndexOf("/")+1, this.inputSandbox.get(1).length());
 			String[] oldArguments = this.arguments.split(" ");
 			String newArguments = "";
 			for (int i = 1; i < oldArguments.length; i++) {
 				newArguments += oldArguments[i] + " ";
 			}
 			this.arguments = newArguments;
 			this.inputSandbox.remove(1);
 			this.inputSandbox.remove(0);
 			
 			if(this.inputSandbox.size()==0){
 				this.inputSandbox = null;
 			}
 		}
 		
 		if(isTemplate){
 			getInputTemplate(userId, templatePath);
 		}else{
 			row = grep(cb, "JobID");
 			String jobId = row.substring(row.indexOf("=")+2, row.length()-2);
 			getInputSandboxFile(userId, jobId);
 		}
 		
 		this.outputSandboxRequest = "";
 		for(int i = 0; i < this.outputSandbox.size(); i++){
 			if(!this.outputSandbox.get(i).equals(this.stdOutput)&&!this.outputSandbox.get(i).equals(this.stdError)){
 				this.outputSandboxRequest += this.outputSandbox.get(i);
 				if(i < (this.outputSandbox.size()-1))
 					this.outputSandboxRequest += ";";
 			}
 		}
 		
 		log.info("OutputSanbox: " + this.outputSandbox);
 		log.info("OutputSanboxRequest: " + this.outputSandboxRequest);
 		
 		if(this.parameterStart!=null && !this.parameterStart.isEmpty() && this.parameters==null)
 			this.parameterStart = null;
 		
 	}
 	
 	
 	
 	private String loadFromFile(String path) throws FileNotFoundException, IOException {
 		
 		String jdlFileName = path.split("@")[0];
 		jdlFileName = jdlFileName.substring(jdlFileName.lastIndexOf("/")+1, jdlFileName.length());
 		
 		
 		File jdlFile = new File(path + "/" + jdlFileName + ".jdl");
 		if(!jdlFile.exists()){
 			jdlFileName = jdlFileName.substring(0, jdlFileName.lastIndexOf("_"));
 		}
 		
 		log.info("JDL name: " + jdlFileName);
 		log.info("JDL path: " + path + "/" + jdlFileName + ".jdl");
 		log.info("Exist? " + jdlFile.exists());
 		
 		jdlFile = new File(path + "/" + jdlFileName + ".jdl");
 		
 		BufferedReader br = new BufferedReader(new FileReader(jdlFile));
 	    try {
 	        StringBuilder sb = new StringBuilder();
 	        String line = br.readLine();
 
 	        while (line != null) {
 	            sb.append(line);
 	            sb.append("\n");
 	            line = br.readLine();
 	        }
 	        return sb.toString();
 	    } finally {
 	        br.close();
 	    }
 		
 	}
 
 	private void getInputTemplate(long userId, String templatePath) throws IOException {
 		/*
 		 * Create folder, and save path into field
 		 */
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
 		Calendar cal = new GregorianCalendar();
 		Date now = cal.getTime();
 		
 		
 		String tmpDir = "JDL_"+sdf.format(now);
 		String userPath = System.getProperty("java.io.tmpdir") + "/users/"+userId;
 		String path = userPath + "/DIRAC/jdls/"+tmpDir;
 		
 		File jdlFolder = new File(path);
 		jdlFolder.mkdirs();
 		
 		this.path = path;
 		
 		/*
 		 * Move file into created folder
 		 */
 		
 		File originFolder = new File(templatePath);
 		FileUtil.copyDirectory(originFolder, jdlFolder);
 		
 	}
 
 	private void getInputSandboxFile(long userId, String jobId) throws DiracException {
 		/*
 		 * Create folder, and save path into field
 		 */
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
 		Calendar cal = new GregorianCalendar();
 		Date now = cal.getTime();
 		
 		
 		String tmpDir = "JDL_"+sdf.format(now);
 		String userPath = System.getProperty("java.io.tmpdir") + "/users/"+userId;
 		String path = userPath + "/DIRAC/jdls/"+tmpDir;
 		
 		File jdlFolder = new File(path);
 		jdlFolder.mkdirs();
 		
 		this.path = path;
 		
 		/*
 		 * Get inputs file from dirac
 		 */
 		
 		DiracAdminUtil util = new DiracAdminUtil();
 		util.getInputSandbox(path, jobId);
 		
 		/*
 		 * Move file into created folder
 		 */
 		
 		File originFolder = new File(path+"/InputSandbox"+jobId);
 		
 		DiracUtil.mv(originFolder, path);
 		
 		
 	}
 
 	// Pattern used to parse lines
     private static Pattern linePattern = Pattern.compile(".*\r?\n");
 
 	private String grep(CharBuffer cb, String key) {
 		
 		Pattern pattern = null;
 		
 		try {
 		    pattern = Pattern.compile(key);
 		} catch (PatternSyntaxException x) {
 		    x.printStackTrace();
 		    return null;
 		}
 		
 		Matcher lm = linePattern.matcher(cb);	// Line matcher
 		Matcher pm = null;			// Pattern matcher
 		
 		while (lm.find()) {
 		    CharSequence cs = lm.group(); 	// The current line
 		    if (pm == null)
 		    	pm = pattern.matcher(cs);
 		    else
 		    	pm.reset(cs);
 		    if (pm.find())
 		    	return cs.toString();
 		    if (lm.end() == cb.limit())
 		    	break;
 		}
 		
 		return null;
 	}
 
 }
