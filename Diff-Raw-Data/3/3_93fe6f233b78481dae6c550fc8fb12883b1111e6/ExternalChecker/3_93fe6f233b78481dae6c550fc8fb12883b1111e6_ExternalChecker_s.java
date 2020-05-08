 package org.eclipse.dltk.validators.internal.core.externalchecker;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
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
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.validators.core.AbstractValidator;
 import org.eclipse.dltk.validators.core.IValidatorType;
 import org.eclipse.dltk.validators.internal.core.ValidatorsCore;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.NodeList;
 
 public class ExternalChecker extends AbstractValidator {
 
 	private static final String EXTENSIONS = "scriptPattrn";
 	private String arguments;
 	private IPath commmand;
 	boolean initialized = false;
 	private static final String ARGUMENTS = "arguments";
 	private static final String COMMAND = "command";
 	private List rules = new ArrayList();
 	private String extensions;
 
 	public void setCommand(String text) {
 		this.commmand = new Path(text);
 	}
 
 	public void setRules(Vector list) {
 		rules.clear();
 		for (int i = 0; i < list.size(); i++) {
 			rules.add(((Rule) list.get(i)));
 		}
 	}
 
 	public IPath getCommand() {
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
 			if( codeLines.length <= lineNumber ) {
 				return new int[] {0, 1};
 			}
 			String codeLine = codeLines[lineNumber];
 			String trimmedCodeLine = codeLine.trim();
 
 			int start = codeLineLengths[lineNumber]
 					+ codeLine.indexOf(trimmedCodeLine);
 			int end = start + trimmedCodeLine.length();
 
 			return new int[] { start, end };
 		}
 	}
 
 	public ExternalCheckerProblem parseProblem(String problem) {
 		List wlist = ExternalCheckerWildcardManager.loadCustomWildcards();
 		for (int i = 0; i < rules.size(); i++) {
 			Rule rule = (Rule) this.rules.get(i);
 			// String wcard = rule.getDescription();
 // List tlist = null;
 			try {
 				WildcardMatcher wmatcher = new WildcardMatcher(wlist);
 				ExternalCheckerProblem cproblem = wmatcher.match(rule, problem);
 				if(cproblem != null) {
 					return cproblem;
 				}
 			} catch (Exception x) {
 				if (DLTKCore.DEBUG) {
 					System.out.println(x.toString());
 				}
 				continue;
 			}
 		}
 		return null;
 	}
 
 	public ExternalChecker(String id, String name, IValidatorType type) {
 		super(id, name, type);
 		this.arguments = "%f";
 		this.commmand = new Path("");
 		this.extensions = "*";
 	}
 
 	protected ExternalChecker(String id, IValidatorType type) {
 		super(id, null, type);
 		this.arguments = "%f";
 		this.commmand = new Path("");
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
 		this.commmand = new Path((element.getAttribute(COMMAND)));
 		this.arguments = element.getAttribute(ARGUMENTS);
 		this.extensions = element.getAttribute(EXTENSIONS);
 
 		NodeList nodes = element.getChildNodes();
 		rules.clear();
 		for (int i = 0; i < nodes.getLength(); i++) {
 			if (nodes.item(i).getNodeName() == "rule") {
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
 		element.setAttribute(COMMAND, this.commmand.toOSString());
 		element.setAttribute(EXTENSIONS, this.extensions);
 
 		for (int i = 0; i < rules.size(); i++) {
 			Element elem = doc.createElement("rule");
 			elem.setAttribute("TEXT", ((Rule) rules.get(i)).getDescription());
 			elem.setAttribute("TYPE", ((Rule) rules.get(i)).getType());
 			element.appendChild(elem);
 		}
 
 	}
 
 	protected static IMarker reportErrorProblem(IResource resource,
 			ExternalCheckerProblem problem, int start, int end)
 			throws CoreException {
 
 		return ExternalCheckerMarker.setMarker(resource, problem
 				.getLineNumber(), start, end, problem.getDescription(),
 				IMarker.SEVERITY_ERROR, IMarker.PRIORITY_NORMAL);
 	}
 
 	protected static IMarker reportWarningProblem(IResource resource,
 			ExternalCheckerProblem problem, int start, int end)
 			throws CoreException {
 
 		return ExternalCheckerMarker.setMarker(resource, problem
 				.getLineNumber(), start, end, problem.getDescription(),
 				IMarker.SEVERITY_WARNING, IMarker.PRIORITY_NORMAL);
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
 		
 		String elementName = module.getElementName();
 		String[] split = this.extensions.split(";");
 		boolean found = false;
 		for (int i = 0; i < split.length; i++) {
 			if( elementName.endsWith(split[i]) || split[i].equals("*") ) {
 				found = true;
 				break;
 			}
 		}
 		if( this.extensions.equals("")) {
 			found = true;
 		}
 		if( !found ) {
 			return Status.OK_STATUS;
 		}
 		IResource resource = module.getResource();
 		if (resource == null) {
 			return new Status(IStatus.ERROR, ValidatorsCore.PLUGIN_ID, "SourceModule resource is null");
 		}
 		
 		try {
 			ExternalCheckerMarker.clearMarkers(resource);
 		} catch (CoreException e2) {
 			if( DLTKCore.DEBUG ) {
 				e2.printStackTrace();
 			}
 		}
 
 		List lines = new ArrayList();
 		// String filepath = resource.getLocation().makeAbsolute().toOSString();
 		String com = this.commmand.toOSString();
 		String args = this.processArguments(resource);
 		String[] sArgs = args.split("::");
 
 		List coms = new ArrayList();
 		coms.add(com);
 		for (int i = 0; i < sArgs.length; i++) {
 			coms.add(sArgs[i]);
 		}
 
 		// StringBuilder sb = new StringBuilder();
 		// /sb.append(com);
 		// sb.append(" ");
 		// sb.append(args);
 
 		// String extcom1 = new String(sb);
 		String[] extcom = (String[]) coms.toArray(new String[coms.size()]);
 
 		BufferedReader input = null;
 // OutputStreamWriter output = null;
 		Process process = null;
 		try {
 			try {
 				// process = Runtime.getRuntime().exec(extcom);
 				process = DebugPlugin.exec(extcom, null);
 			} catch (Throwable e) {
 				if (DLTKCore.DEBUG) {
 					System.out.println(e.toString());
 				}
 			}
 			input = new BufferedReader(new InputStreamReader(process
 					.getInputStream()));
 
 			String line = null;
 			while ((line = input.readLine()) != null) {
 				if (console != null) {
 					console.write((line + "\n").getBytes());
 				}
 				lines.add(line);
 			}
 
 			String content = "";
 			content = module.getSource();
 			ExternalCheckerCodeModel model = new ExternalCheckerCodeModel(
 					content);
 
 			for (Iterator iterator = lines.iterator(); iterator.hasNext();) {
 				String line1 = (String) iterator.next();
 				ExternalCheckerProblem problem = parseProblem(line1);
 				if (problem != null) {
 					int[] bounds = model.getBounds(problem.getLineNumber()-1);
 					if (problem.getType().indexOf("Error") != -1) {
 						reportErrorProblem(resource, problem, bounds[0],
 								bounds[1]);
 					} else if (problem.getType().indexOf("Warning") != -1) {
 						reportWarningProblem(resource, problem, bounds[0],
 								bounds[1]);
 					}
 				}
 			}
 		}
 
 		catch (Exception e) {
 			if (DLTKCore.DEBUG) {
 				System.out.println(e.toString());
 			}
 		}
 		return Status.OK_STATUS;
 	}
 
 	public void setNewRule(Rule s) {
 		rules.add(s);
 	}
 
 	public Rule getRule(int index) {
 		if (index < rules.size())
 			return (Rule) rules.get(index);
 		return null;
 	}
 
 	public int getNRules() {
 		return rules.size();
 	}
 
 	private String processArguments(IResource resource) {
 		String path = resource.getLocation().makeAbsolute().toOSString();
 		String arguments = this.arguments;
 
 		String user = replaceSequence(arguments.replaceAll("\t", "::")
 				.replaceAll(" ", "::"), 'f', path);
		String result = "";
		return result + "::" + user;
 	}
 
 	private String replaceSequence(String from, char pattern, String value) {
 		StringBuffer buffer = new StringBuffer();
 		for (int i = 0; i < from.length(); ++i) {
 			char c = from.charAt(i);
 			if (c == '%' && i < from.length() - 1
 					&& from.charAt(i + 1) == pattern) {
 				buffer.append(value);
 				i++;
 			} else {
 				buffer.append(c);
 			}
 		}
 		return buffer.toString();
 	}
 
 	public boolean isValidatorValid() {
 		IPath path = this.commmand;
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
 
 	public String getExtensions() {
 		return extensions;
 	}
 
 	public void setExtensions(String scriptPattern) {
 		this.extensions = scriptPattern;
 	}
 }
