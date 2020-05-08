 package ms.gundam.astparser;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.dom.AST;
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.ASTParser;
 import org.eclipse.jdt.core.dom.ASTVisitor;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.jdt.core.dom.Expression;
 import org.eclipse.jdt.core.dom.IBinding;
 import org.eclipse.jdt.core.dom.ITypeBinding;
 import org.eclipse.jdt.core.dom.MethodDeclaration;
 import org.eclipse.jdt.core.dom.MethodInvocation;
 import org.eclipse.jdt.core.dom.QualifiedName;
 import org.eclipse.jdt.core.dom.SimpleName;;
 
 /**
  *  
  * @author tetsuo
  *
  */
 public class SourceReader {
     //private List<AttributedToken> list = new ArrayList<AttributedToken>();
 	private String myClassname = null; 
 	private DB db;
 	private int index = 1;
 
     public void read(File file) {
 		StringBuffer sb = new StringBuffer();
 		String pathname = null;
 		String packagename = null;
 		final String separator = File.separator.equals("\\") ? "\\\\" : File.separator;
 		try {
 			BufferedReader br;
 			br = new BufferedReader(new InputStreamReader( new FileInputStream(file)));
 			String line;
 			Pattern p = Pattern.compile("package (.*);");
 			boolean matchPackage = false;
 			while ((line = br.readLine()) != null){
 				if (!matchPackage) {
 					Matcher m = p.matcher(line);
 					if (m.matches()) {
 						packagename = m.group(1);
 						Matcher match = Pattern.compile("\\.").matcher(packagename);
 						pathname = match.replaceAll(separator);
 						matchPackage = true;
 					}
 				}
 			  	sb.append(line+"\n");
 			}
 		} catch (FileNotFoundException e) {
 		    System.err.println("File " + file.getAbsolutePath() + " not found.");
 	    	System.exit(1);
 		} catch (IOException e) {
 		    System.err.println("File " + file.getAbsolutePath() + " I/O Error.");
 	    	System.exit(1);
 		}
 
 		ASTParser parser = ASTParser.newParser(AST.JLS4);
 		parser.setResolveBindings(true);
 		parser.setBindingsRecovery(true);
 		Matcher matchpath;
 		if (pathname != null) {
 			String quotepath = java.util.regex.Pattern.quote(pathname);
 			matchpath = Pattern.compile(quotepath+separator+file.getName()).matcher(file.getAbsolutePath());
 		} else {
 			matchpath = Pattern.compile(file.getName()).matcher(file.getAbsolutePath());
 		}
 		String sourcepath[]  = new String[1];
 		sourcepath[0] = matchpath.replaceAll("");
 
 		String classname[] = file.getName().split("\\.java");
 //		Pattern.compile("\\.java$").matcher(file.getName()).replaceFirst("");
 		if (packagename != null)
 			myClassname = packagename + "." +  classname[0];
 		else
 			myClassname = classname[0];
 
 		parser.setEnvironment(null, sourcepath, null, true);
 		parser.setUnitName(file.getName());
 		parser.setSource(sb.toString().toCharArray());
 
 		try {
 			CompilationUnit unit = (CompilationUnit) parser.createAST(new NullProgressMonitor());
 			unit.accept(new ASTVisitorImpl());
 		} catch (IllegalStateException e) {
 			e.printStackTrace();
 			return;
 		}
     }
 
     private void regist(final File file) {
 		// ディレクトリの場合
 		if (file.isDirectory()) {
 			File[] subfiles = file.listFiles();
 			for (int i = 0; i < subfiles.length; i++) {
 				regist(subfiles[i]);
 			}
 		}
 
 		// ファイルの場合
 		else if (file.isFile()) {
 			if (file.getName().endsWith(".java")) {
 				System.out.println(index + " Reading " + file.getAbsolutePath() + " . . .");
 				index++;
 				read(file);
 			}
 		}
 		// ディレクトリでもファイルでもない場合は不正
 		else {
 			System.err.println(file.getAbsolutePath() + " is invaild");
 			System.exit(0);
 		}
 	}
 
     public static void main(String args[]) {
     	if (args.length == 0) {
 	    	System.out.println("Specify a source file or directory.");
 	    	System.exit(1);
     	}
 		SourceReader sr = new SourceReader();
 		sr.db = new DB();
     	sr.db.open(new File(args[0]), false);
 		sr.regist(new File(args[1]));
     }
 
     /**
      * VisitorパターンでASTの内容を表示する
      */
     class ASTVisitorImpl extends ASTVisitor {
     	private List<Value> statementList = null;
     
 		public boolean visit(MethodInvocation node) {
 			String classname = "";
     		Expression exp = node.getExpression();
     		if (exp != null) {
     			ITypeBinding type = exp.resolveTypeBinding();
     			if (type != null) {
 					if (type.isArray()) {
 						classname = "!ARRAY";
 					} else {
 						classname = type.getQualifiedName();
 					}
     			} else {
     				if (exp.getNodeType() == ASTNode.SIMPLE_NAME) {
 						classname = ((SimpleName)exp).getIdentifier();
     					IBinding bind = ((SimpleName)exp).resolveBinding();
     					if (bind != null) {
     						System.out.print("@@@");
     					}
     				} else if (exp.getNodeType() == ASTNode.QUALIFIED_NAME) {
 						classname = ((QualifiedName)exp).getFullyQualifiedName();
     				} else
     					;
     			}
     		} else {
     			classname = myClassname;
     		}
     		if (statementList != null) {
     			statementList.add(new Value(classname, node.getName().toString()));
     		}
 			return super.visit(node);
 		}
 
 		@Override
     	public boolean visit(MethodDeclaration node) {
 /*
  	    	Block body = node.getBody();
 	    	ms.gundam.astparser.ASTParser parser = new ms.gundam.astparser.ASTParser();
 		    if (body != null) {
 				for (Object statement : body.statements()) {
 					parser.addStatement((Statement)statement, ATTRIBUTE.NORMAL);
 				}
 			}
 		    StringBuilder sourcestr = new StringBuilder(node.getReturnType2() == null ? "" :  node.getReturnType2() + " ");
 		    sourcestr.append(node.getName().getFullyQualifiedName());
 		    sourcestr.append("(){");
     		for (Token token : parser.getTokens()) {
     			sourcestr.append(token.getName());
     			if (token.dump().charAt(0) == 'M') {
     				if (sourcestr.charAt(sourcestr.length()-2) == ' ') {
     					sourcestr.deleteCharAt(sourcestr.length()-2);
     				}
     			} else {
     				sourcestr.append(" ");
     			}
     		}
     		sourcestr.append("}\n");
     		System.out.print(Formatter.format(sourcestr.toString()));
  */
 //			System.out.println(node.getName().getFullyQualifiedName() + "{");
 			statementList = new ArrayList<Value>();
 		    return super.visit(node);
         }
 
 		@Override
 		public void endVisit(MethodDeclaration node) {
 			String prevclassname = null;
 			String prevmethodname = null;
 
 			//			System.out.println("}");
			if (statementList == null)
				return;
 			for (Value statement : statementList) {
 				if (prevclassname != null) {
 					db.put(prevclassname, prevmethodname, statement.getClassname(), statement.getMethodname());
 				}
 				prevclassname = statement.getClassname();
 				prevmethodname = statement.getMethodname();
 			}
 			
 			statementList = null;
 			super.endVisit(node);
 		}
 		
     }
 }
