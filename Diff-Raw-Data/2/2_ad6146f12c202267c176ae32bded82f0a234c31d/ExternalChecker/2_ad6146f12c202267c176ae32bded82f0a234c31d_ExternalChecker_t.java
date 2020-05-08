 package org.eclipse.dltk.validators.internal.core.externalchecker;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.environment.EnvironmentManager;
 import org.eclipse.dltk.core.environment.IEnvironment;
 import org.eclipse.dltk.core.environment.IExecutionEnvironment;
 import org.eclipse.dltk.core.environment.IFileHandle;
 import org.eclipse.dltk.validators.core.AbstractValidator;
 import org.eclipse.dltk.validators.core.IValidatorType;
 import org.eclipse.dltk.validators.internal.core.ValidatorsCore;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class ExternalChecker extends AbstractValidator {
 
 	private static final String EXTENSIONS = "scriptPattrn"; //$NON-NLS-1$
 	private String arguments;
 	private Map paths;
 	boolean initialized = false;
 	private static final String ARGUMENTS = "arguments"; //$NON-NLS-1$
 	private static final String PATH_TAG = "path";
 	private static final String ENVIRONMENT_ATTR = "environment";
 	private static final String PATH_ATTR = "path";
 	private List rules = new ArrayList();
 	private String extensions;
 
 	public void setCommand(Map command) {
 		this.paths = command;
 	}
 
 	public void setRules(Vector list) {
 		rules.clear();
 		for (int i = 0; i < list.size(); i++) {
 			rules.add(list.get(i));
 		}
 	}
 
 	public Map getCommand() {
 		return paths;
 	}
 
 	private static class ExternalCheckerCodeModel {
 		private String[] codeLines;
 
 		private int[] codeLineLengths;
 
 		public ExternalCheckerCodeModel(String code) {
 			this.codeLines = code.split("\n"); //$NON-NLS-1$
 			int count = this.codeLines.length;
 
 			this.codeLineLengths = new int[count];
 
 			int sum = 0;
 			for (int i = 0; i < count; ++i) {
 				this.codeLineLengths[i] = sum;
 				sum += this.codeLines[i].length() + 1;
 			}
 		}
 
 		public int[] getBounds(int lineNumber) {
 			if (codeLines.length <= lineNumber) {
 				return new int[] { 0, 1 };
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
 				if (cproblem != null) {
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
 		this.arguments = "%f"; //$NON-NLS-1$
 		this.paths = newEmptyPath(); //$NON-NLS-1$
 		this.extensions = "*"; //$NON-NLS-1$
 	}
 
 	private Map newEmptyPath() {
 		Map paths = new HashMap();
 		IEnvironment[] environments = EnvironmentManager.getEnvironments();
 		for (int i = 0; i < environments.length; i++) {
 			paths.put(environments[i], "");
 		}
 		return paths;
 	}
 
 	protected ExternalChecker(String id, IValidatorType type) {
 		super(id, null, type);
 		this.arguments = "%f"; //$NON-NLS-1$
 		this.paths = newEmptyPath(); //$NON-NLS-1$
 	}
 
 	protected ExternalChecker(String id, Element element, IValidatorType type) {
 		super(id, null, type);
 		loadInfo(element);
 	}
 
 	public void loadInfo(Element element) {
 		if (initialized) {
 			return;
 		}
 		super.loadFrom(element);
 		initialized = true;
 		IEnvironment[] environments = EnvironmentManager.getEnvironments();
 		paths = newEmptyPath();
 		// this.path = new Path(element.getAttribute(PATHS_TAG));
 		NodeList childNodes = element.getChildNodes();
 		for (int i = 0; i < childNodes.getLength(); i++) {
 			Node item = childNodes.item(i);
 			if (item.getNodeType() == Node.ELEMENT_NODE) {
 				Element elementNode = (Element) item;
 				if (elementNode.getTagName().equalsIgnoreCase(PATH_TAG)) {
 					String environment = elementNode
 							.getAttribute(ENVIRONMENT_ATTR);
 					String path = elementNode.getAttribute(PATH_ATTR);
 					IEnvironment env = EnvironmentManager
 							.getEnvironmentById(environment);
 					if (env != null) {
 						this.paths.put(env, path);
 					}
 				}
 			}
 		}
 		this.arguments = element.getAttribute(ARGUMENTS);
 		this.extensions = element.getAttribute(EXTENSIONS);
 
 		NodeList nodes = element.getChildNodes();
 		rules.clear();
 		for (int i = 0; i < nodes.getLength(); i++) {
 			if (nodes.item(i).getNodeName() == "rule") { //$NON-NLS-1$
 				NamedNodeMap map = nodes.item(i).getAttributes();
 				String ruletext = map.getNamedItem("TEXT").getNodeValue(); //$NON-NLS-1$
 				String ruletype = map.getNamedItem("TYPE").getNodeValue(); //$NON-NLS-1$
 				Rule r = new Rule(ruletext, ruletype);
 				rules.add(r);
 			}
 		}
 	}
 
 	public void storeTo(Document doc, Element element) {
 		super.storeTo(doc, element);
 		element.setAttribute(ARGUMENTS, this.arguments);
 		element.setAttribute(EXTENSIONS, this.extensions);
 
 		for (int i = 0; i < rules.size(); i++) {
 			Element elem = doc.createElement("rule"); //$NON-NLS-1$
 			elem.setAttribute("TEXT", ((Rule) rules.get(i)).getDescription()); //$NON-NLS-1$
 			elem.setAttribute("TYPE", ((Rule) rules.get(i)).getType()); //$NON-NLS-1$
 			element.appendChild(elem);
 		}
 
 		for (Iterator iterator = paths.keySet().iterator(); iterator.hasNext();) {
 			IEnvironment env = (IEnvironment) iterator.next();
 			if (env != null) {
 				Element elem = doc.createElement(PATH_TAG);
 				elem.setAttribute(ENVIRONMENT_ATTR, env.getId());
 				elem.setAttribute(PATH_ATTR, (String) paths.get(env));
 				element.appendChild(elem);
 			}
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
 
 	public IStatus validate(IResource[] resources, OutputStream console,
 			IProgressMonitor monitor) {
 		return Status.CANCEL_STATUS;
 	}
 
 	public IStatus validate(ISourceModule[] modules, OutputStream console,
 			IProgressMonitor monitor) {
 		if (monitor == null)
 			monitor = new NullProgressMonitor();
 
 		monitor.beginTask(
 				Messages.ExternalChecker_checkingWithExternalExecutable,
 				modules.length);
 		try {
 			for (int i = 0; i < modules.length; i++) {
 				if (monitor.isCanceled())
 					return Status.CANCEL_STATUS;
 				validate(modules[i], console);
 				monitor.worked(1);
 			}
 		} finally {
 			monitor.done();
 		}
 		return Status.OK_STATUS;
 	}
 
 	public IStatus validate(ISourceModule module, OutputStream console) {
 
 		String elementName = module.getElementName();
 		String[] split = this.extensions.split(";"); //$NON-NLS-1$
 		boolean found = false;
 		for (int i = 0; i < split.length; i++) {
 			if (elementName.endsWith(split[i]) || split[i].equals("*")) { //$NON-NLS-1$
 				found = true;
 				break;
 			}
 		}
 		if (this.extensions.equals("")) { //$NON-NLS-1$
 			found = true;
 		}
 		if (!found) {
 			return Status.OK_STATUS;
 		}
 		IResource resource = module.getResource();
 		if (resource == null) {
 			return new Status(IStatus.ERROR, ValidatorsCore.PLUGIN_ID,
 					Messages.ExternalChecker_sourceModuleResourceIsNull);
 		}
 
 		try {
 			ExternalCheckerMarker.clearMarkers(resource);
 		} catch (CoreException e2) {
 			if (DLTKCore.DEBUG) {
 				e2.printStackTrace();
 			}
 		}
 		IEnvironment environment = EnvironmentManager.getEnvironment(module);
 		IExecutionEnvironment execEnvironment = (IExecutionEnvironment) environment
 				.getAdapter(IExecutionEnvironment.class);
 
 		List lines = new ArrayList();
 		// String filepath = resource.getLocation().makeAbsolute().toOSString();
 		String com = (String) this.paths.get(environment);
 		if (com == null || com.trim().length() == 0) {
 			return Status.CANCEL_STATUS;
 		}
 		String args = this.processArguments(resource, environment);
 		String[] sArgs = args.split("::"); //$NON-NLS-1$
 
 		List coms = new ArrayList();
 		coms.add(com);
 		for (int i = 0; i < sArgs.length; i++) {
 			coms.add(sArgs[i]);
 		}
 
 		String[] extcom = (String[]) coms.toArray(new String[coms.size()]);
 
 		BufferedReader input = null;
 		Process process = null;
 		try {
 			try {
 				process = execEnvironment.exec(extcom, null, null);
 			} catch (Throwable e) {
 				if (DLTKCore.DEBUG) {
 					System.out.println(e.toString());
 				}
 				return Status.CANCEL_STATUS;
 			}
 			input = new BufferedReader(new InputStreamReader(process
 					.getInputStream()));
 
 			String line = null;
 			while ((line = input.readLine()) != null) {
 				if (console != null) {
 					console.write((line + "\n").getBytes()); //$NON-NLS-1$
 				}
 				lines.add(line);
 			}
 
 			String content = ""; //$NON-NLS-1$
 			content = module.getSource();
 			ExternalCheckerCodeModel model = new ExternalCheckerCodeModel(
 					content);
 
 			for (Iterator iterator = lines.iterator(); iterator.hasNext();) {
 				String line1 = (String) iterator.next();
 				ExternalCheckerProblem problem = parseProblem(line1);
 				if (problem != null) {
 					int[] bounds = model.getBounds(problem.getLineNumber() - 1);
 					if (problem.getType().indexOf(
 							Messages.ExternalChecker_error) != -1) {
 						reportErrorProblem(resource, problem, bounds[0],
 								bounds[1]);
 					} else if (problem.getType().indexOf(
 							Messages.ExternalChecker_warning) != -1) {
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
 
 	private String processArguments(IResource resource, IEnvironment environment) {
 		String path = null;
 		if (resource.getLocation() != null) {
 			path = resource.getLocation().makeAbsolute().toOSString();
 		}
 		else {
 			URI uri = resource.getLocationURI();
 			IFileHandle file = environment.getFile(uri);
			path = file.toOSString();
 		}
 		String user = replaceSequence(arguments.replaceAll("\t", "::") //$NON-NLS-1$ //$NON-NLS-2$
 				.replaceAll(" ", "::"), 'f', path); //$NON-NLS-1$ //$NON-NLS-2$
 		return user;
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
 	
 	public boolean isValidatorValid(IEnvironment environment) {
 		String path = (String) this.paths.get(environment);
 		if( path == null || path.trim().length() == 0 ) {
 			return false;
 		}
 		IFileHandle file = environment.getFile(new Path(path));
 
 		if (!file.exists()) {
 			return false;
 		}
 
 		return true;
 	}
 
 	public void clean(ISourceModule[] modules) {
 		for (int i = 0; i < modules.length; i++) {
 			clean(modules[i].getResource());
 		}
 	}
 
 	public void clean(IResource[] resource) {
 		for (int i = 0; i < resource.length; i++) {
 			try {
 				ExternalCheckerMarker.clearMarkers(resource[i]);
 			} catch (CoreException e) {
 				if (DLTKCore.DEBUG) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	public String getExtensions() {
 		return extensions;
 	}
 
 	public void setExtensions(String scriptPattern) {
 		this.extensions = scriptPattern;
 	}
 
 	public void setProgressMonitor(IProgressMonitor monitor) {
 		// TODO Auto-generated method stub
 
 	}
 }
