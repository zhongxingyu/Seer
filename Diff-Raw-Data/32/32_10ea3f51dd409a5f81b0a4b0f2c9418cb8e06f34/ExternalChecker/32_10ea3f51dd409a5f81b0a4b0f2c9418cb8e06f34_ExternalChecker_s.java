 package org.eclipse.dltk.validators.internal.core.externalchecker;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.validators.core.AbstractValidator;
 import org.eclipse.dltk.validators.core.IValidatorType;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.NodeList;
 
 
 public class ExternalChecker extends AbstractValidator {
 	
 	private String arguments;
 	private String commmand;
 	boolean initialized = false;
 	private static final String ARGUMENTS = "arguments";
 	private static final String COMMAND = "command";
 	private List rules = new ArrayList(); 
 
 
 	public void setCommand(String text){
 		this.commmand = text;
 	}
 	
 	public void  setRules(Vector list){
 		rules.clear();
 		for(int i=0; i<list.size(); i++){
 			rules.add(((Rule)list.get(i)));
 		}
 	}
 	
 	public String getCommand(){
 		return commmand;
 	}
 	
 	
 	private static class ExternalCheckerCodeModel {
 		private String[] codeLines;
 
 		private int[] codeLineLengths;
 
 		public ExternalCheckerCodeModel(String code) {
 			this.codeLines = code.split("\n");
 			int count = this.codeLines.length;
 
 			this.codeLineLengths = new int[count];
 
 			int sum = 0;
 			for (int i = 0; i < count; ++i) {
 				this.codeLineLengths[i] = sum;
 				sum += this.codeLines[i].length() + 1;
 			}
 		}
 
 		public int[] getBounds(int lineNumber) {
 			String codeLine = codeLines[lineNumber];
 			String trimmedCodeLine = codeLine.trim();
 
 			int start = codeLineLengths[lineNumber]
 					+ codeLine.indexOf(trimmedCodeLine);
 			int end = start + trimmedCodeLine.length();
 
 			return new int[] { start, end };
 		}
 	}
 	public ExternalCheckerProblem parseProblem(String problem) {
 	
 		for(int i = 0; i<rules.size(); i++){
 			Rule rule = (Rule)this.rules.get(i);
 			String wcard = rule.getDescription();
 			List tlist = null;
 			try{
 				tlist  = WildcardMatcher.match(wcard, problem);
 				
 			}
         		catch(Exception x){
 //        			System.out.println(x.toString());
         			return null;
 			}
 			return new ExternalCheckerProblem(rule.getType(), tlist);
 		}
 		return null;
 	}
 	
 	public ExternalChecker(String id, String name, IValidatorType type)	{
 		super(id, name, type);
 		this.arguments = "%f";
 		this.commmand = "";
 
 	}
 	protected ExternalChecker(String id, IValidatorType type) {
 		super(id, null, type);
 		this.arguments = "%f";
 		this.commmand = "";
 	}
 	
 	protected ExternalChecker(String id, Element element, IValidatorType type)
 	throws IOException {
 		super(id, null, type);
 		loadInfo(element);
 	}
 	
 	public void loadInfo(Element element) {
 		if (initialized) {
 			return;
 		}
 		super.loadFrom(element);
 		initialized = true;
 		this.commmand = new String(element.getAttribute(COMMAND));
 		this.arguments = element.getAttribute(ARGUMENTS);
 
 		NodeList nodes = element.getChildNodes();
 		rules.clear();
 		for(int i=0; i<nodes.getLength(); i++)
 		{
 			if(nodes.item(i).getNodeName()=="rule"){
 				NamedNodeMap map = nodes.item(i).getAttributes();
 				String ruletext = map.getNamedItem("TEXT").getNodeValue();
 				String ruletype = map.getNamedItem("TYPE").getNodeValue();
 				Rule r = new Rule(ruletext, ruletype);
 				rules.add(r);
 			}
 		}
 	}
 	public void storeTo(Document doc, Element element) {
 		super.storeTo(doc, element);
 		element.setAttribute(ARGUMENTS, this.arguments);
 		element.setAttribute(COMMAND, this.commmand);
 	
 		for (int i=0; i< rules.size(); i++)
 		{
 			Element elem = doc.createElement("rule");
 			elem.setAttribute("TEXT", ((Rule)rules.get(i)).getDescription());
 			elem.setAttribute("TYPE", ((Rule)rules.get(i)).getType());
 			element.appendChild(elem);
 		}
 		
 	}
 	
 	
 	protected static IMarker reportErrorProblem(IResource resource,
 			ExternalCheckerProblem problem, int start, int end)
 			throws CoreException {
 
 		return ExternalCheckerMarker.setMarker(resource, problem.getLineNumber(),
 				start, end, problem.getDescription(), IMarker.SEVERITY_ERROR,
 				IMarker.PRIORITY_NORMAL);
 	}
 
 	protected static IMarker reportWarningProblem(IResource resource,
 			ExternalCheckerProblem problem, int start, int end)
 			throws CoreException {
 
 		return ExternalCheckerMarker.setMarker(resource, problem.getLineNumber(),
 				start, end, problem.getDescription(), IMarker.SEVERITY_WARNING,
 				IMarker.PRIORITY_NORMAL);
 	}
 
 	public void setPath(String path) {
 		initialized = true;
 		String s = "";
 		
 		try {
 			byte b[] = path.getBytes("UTF8");
 			s = new String(b, "UTF8");
 		} catch (UnsupportedEncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 	}
 	
 	public void setArguments(String arguments) {
 		initialized = true;
 		this.arguments = arguments;
 	}
 	
 	public String getArguments() {
 		return arguments;
 	}
 	public IStatus validate(IResource resource, OutputStream console) {
 		return Status.OK_STATUS;
 	}
 	
 	public IStatus validate(ISourceModule module, OutputStream console) {
 		IResource resource = module.getResource();
 		if( resource == null ) {
 			return Status.CANCEL_STATUS;
 		}
 	
 		List lines = new ArrayList();
 		String filepath = resource.getLocation().makeAbsolute().toOSString();
 		String com = this.commmand;
 		String args= this.processArguments(resource);
 		
		StringBuilder sb = new StringBuilder();
 		sb.append(com);
 		sb.append(" ");
 		sb.append(args);
 		
 		String extcom = new String(sb);
 		
 		BufferedReader input = null;
 		OutputStreamWriter output = null;
 		Process process = null;
 		try {
 			try{
 				process = Runtime.getRuntime().exec(extcom);
 			}
 			catch(Exception e){
 				System.out.println(e.toString());
 			}
 			input = new BufferedReader(new InputStreamReader(process.getInputStream()));
 		
 			String line = null;
 			while ((line = input.readLine()) != null) {
 				if( console != null ) {
 					console.write((line+ "\n").getBytes());
 				}
 				lines.add(line);
 			}
 			
 			String content = "";
 			content = module.getSource();
 			ExternalCheckerCodeModel model = new ExternalCheckerCodeModel(content);
 			
 			for (Iterator iterator = lines.iterator(); iterator.hasNext();) {
 				String line1 = (String) iterator.next();
 				ExternalCheckerProblem problem = parseProblem(line1);
 				if (problem != null) {
 					int[] bounds = model.getBounds(problem.getLineNumber());
 					if(problem.getType().indexOf("Error")!= -1){
 						reportErrorProblem(resource, problem, bounds[0], bounds[1]);
 					}
 					else if(problem.getType().indexOf("Warning")!=- 1){
 						reportWarningProblem(resource, problem, bounds[0], bounds[1]);
 					}
 				}
 
 			}
 		}
 			
 		catch(Exception e){
 			System.out.println(e.toString());
 		}
 		return Status.OK_STATUS;
 	}
 	
 	public void setNewRule(Rule s){
 		rules.add(s);
 	}
 	
 	public  Rule getRule(int index){
 		if(index < rules.size())
 			return (Rule)rules.get(index);
 		return null;
 	}
 	
 	public int getNRules(){
 		return rules.size();
 	}
 	
 	private String processArguments(IResource resource) {
 		String path = resource.getLocation().makeAbsolute().toOSString();
 		String arguments = this.arguments;
 		String user = replaceSequence(arguments, 'f', path);
 		String result = "";
 		return result + " " + user;
 	}
 
 	private String replaceSequence(String from, char pattern, String value) {
 		boolean append_braces = false;
 		StringBuffer buffer = new StringBuffer();
 		if(pattern== 'f')
 			append_braces = true;
 		for( int i = 0; i < from.length(); ++i ) {
 			char c = from.charAt(i);
 			if( c == '%' && i < from.length() - 1 && from.charAt(i + 1) == pattern ) {
 				if(append_braces)
 					buffer.append("\"");
 				buffer.append(value);
 				if(append_braces)
 					buffer.append("\"");
 				i++;
 			}
 			else {
 				buffer.append(c);
 			}
 		}
 		return buffer.toString();
 	}
 		
 	public boolean isValidatorValid(){
 		IPath path = new Path(this.commmand);
 		File file = new File(path.toOSString());
 
 		if (!file.exists()) {
 			return false;
 		}
 
 		return true;
 	}
 	public void clean(ISourceModule module) {
 		clean(module.getResource());
 	}
 
 	public void clean(IResource resource) {
 		try {
 			ExternalCheckerMarker.clearMarkers(resource);
 		} catch (CoreException e) {
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
