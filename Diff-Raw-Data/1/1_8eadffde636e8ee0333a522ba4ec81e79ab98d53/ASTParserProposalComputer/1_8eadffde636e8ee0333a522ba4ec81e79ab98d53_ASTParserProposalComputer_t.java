 package ms.gundam.astparser.plugin;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import ms.gundam.astparser.DB.DB;
 import ms.gundam.astparser.DB.Value;
 import ms.gundam.astparser.DB.ValuewithRanking;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IMethod;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.core.dom.AST;
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.ASTParser;
 import org.eclipse.jdt.core.dom.ASTVisitor;
 import org.eclipse.jdt.core.dom.ClassInstanceCreation;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.jdt.core.dom.Expression;
 import org.eclipse.jdt.core.dom.IBinding;
 import org.eclipse.jdt.core.dom.ITypeBinding;
 import org.eclipse.jdt.core.dom.MethodInvocation;
 import org.eclipse.jdt.core.dom.QualifiedName;
 import org.eclipse.jdt.core.dom.QualifiedType;
 import org.eclipse.jdt.core.dom.SimpleName;
 import org.eclipse.jdt.core.dom.SimpleType;
 import org.eclipse.jdt.core.dom.Type;
 import org.eclipse.jdt.ui.text.java.ContentAssistInvocationContext;
 import org.eclipse.jdt.ui.text.java.IJavaCompletionProposalComputer;
 import org.eclipse.jdt.ui.text.java.JavaContentAssistInvocationContext;
 import org.eclipse.jface.text.contentassist.CompletionProposal;
 import org.eclipse.jface.text.contentassist.ICompletionProposal;
 import org.eclipse.jface.text.contentassist.IContextInformation;
 
 public class ASTParserProposalComputer implements IJavaCompletionProposalComputer {
 	private String myClassname = null;
 	final private String directory = "/DB/mDB";
 	private DB db;
 
 	class MethodVisitor extends ASTVisitor {
     	private List<Value> beforestatementList = new ArrayList<Value>();
     	private List<Value> afterstatementList = new ArrayList<Value>();	
     	private int offset;
     
 		public MethodVisitor(int offset) {
 			super();
 			this.offset = offset;
 		}
 
 		public List<Value> getBeforeStatementList() {
 			return beforestatementList;
 		}
 
 		public List<Value> getAfterStatementList() {
 			return afterstatementList;
 		}
 
     	public boolean visit(ClassInstanceCreation node) {
 			String classname = "";
     		Type classtype = node.getType();
     		if (classtype != null) {
     			ITypeBinding type = classtype.resolveBinding();
     			if (type != null) {
 					if (type.isArray()) {
 						classname = DB.ARRAYNAME;
 					} else {
 						classname = type.getQualifiedName();
 					}
     			} else {
     				if (classtype.isSimpleType()) {
 						classname = ((SimpleType)classtype).getName().getFullyQualifiedName();
     				} else if (classtype.isQualifiedType()) {
 						classname = ((QualifiedType)classtype).getName().getFullyQualifiedName();
     				} else
     					;
     			}
     		} else {
     			System.err.println("cannot get type of new statement.");
     		}
     		if (offset < node.getStartPosition()) {
     			if (afterstatementList != null) {
     				afterstatementList.add(new Value(classname, "<init>"));
     			}
     		} else {
     			if (beforestatementList != null) {
     				beforestatementList.add(new Value(classname, "<init>"));
     			}
     		}
 			return super.visit(node);
     	}
 
     	/**
 		 * メソッド呼び出し文を出現順にリストに追加していく
 		 */
 		public boolean visit(MethodInvocation node) {
 			String classname = "";
     		Expression exp = node.getExpression();
     		if (exp != null) {
     			ITypeBinding type = exp.resolveTypeBinding();
     			if (type != null) {
 					if (type.isArray()) {
 						classname = DB.ARRAYNAME;
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
     		if (offset < node.getStartPosition()) {
     			if (afterstatementList != null) {
     				afterstatementList.add(new Value(classname, node.getName().toString()));
     			}
     		} else {
     			if (beforestatementList != null) {
     				beforestatementList.add(new Value(classname, node.getName().toString()));
     			}
     		}
 			return super.visit(node);
 		}
     }
 
 	@Override
 	public String getErrorMessage() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void sessionStarted() {
 		db = new DB();
 		db.open(new File(directory), true);
 	}
 
 	@Override
 	public void sessionEnded() {
 	}
 
 	private void makeRanking(Value key, Map<String, ValuewithRanking> proposalmap, boolean which) {
 		int ranking = 0;
 		int count = -1;
 
 		List<Value> result = db.get(key.getClassname(), key.getMethodname(), which);
 		Collections.sort(result);
 		long sum = 0;
 		for (Value v: result) {
 			sum += v.getCount();  
 		}
 System.out.println(key.getClassname()+key.getMethodname());			
 		/*
 		 * 出現数順にランキングを1位からつける．すでにマップにある場合はランキングを統合する
 		 */
 		for (Value v : result) {
 			if (count != v.getCount()) {
 				count = v.getCount();
 				ranking++;
 			}
 			int percentage = (int) (v.getCount() * 100 / sum);
 			if (percentage == 0)
 				continue;
 			ValuewithRanking newvalue = new ValuewithRanking(v);
 			newvalue.setPercentage(percentage);
 			newvalue.setRanking(ranking);
 
 			String keyString = newvalue.getClassname()+"#"+newvalue.getMethodname(); 
 System.out.println(newvalue.getRanking()+"("+percentage+"%):"+ keyString);			
 
 			if (proposalmap.containsKey(keyString)) {
 				ValuewithRanking value = proposalmap.get(keyString);
 				value.setRanking(value.getRanking() + ranking);
 				value.setPercentage(value.getPercentage() + percentage);
 				proposalmap.put(keyString, value);
 			} else {
 				proposalmap.put(keyString, newvalue);
 			}
 		}
 	}
 	
 	@Override
 	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context, IProgressMonitor monitor) {
 		final List<ICompletionProposal> list = new ArrayList<ICompletionProposal>();
 		final int offset = context.getInvocationOffset();
 		IMethod method = null;
 
 		final long start = System.nanoTime();
 
 		
 		try {
 			final IJavaElement element = ((JavaContentAssistInvocationContext)context).getCoreContext().getEnclosingElement();
 			if (element == null) {
 				return list;
 			}
 			method = (IMethod)element.getAncestor(IJavaElement.METHOD);
 			if (method == null) {
 				return list;
 			}
 			method.getSourceRange().getOffset();
 		} catch (UnsupportedOperationException e) {
 			return list;
 		} catch (JavaModelException e) {
 			e.printStackTrace();
 			return list;
 		}
 		
 		ICompilationUnit cu = method.getCompilationUnit();
 		ASTParser parser = ASTParser.newParser(AST.JLS4);
 		parser.setKind(ASTParser.K_COMPILATION_UNIT);
 		parser.setResolveBindings(true);
 		parser.setBindingsRecovery(true);
 		parser.setStatementsRecovery(true);
 		parser.setSource(cu);
 		CompilationUnit unit = (CompilationUnit) parser.createAST(null);
 		MethodVisitor visitor= new MethodVisitor(offset);
 		unit.accept(visitor);
 
 		Map<String, ValuewithRanking> proposalmap = new HashMap<String, ValuewithRanking>();
 		for (Value key : visitor.getBeforeStatementList()) { 
 			makeRanking(key, proposalmap, true);
 		}
 		for (Value key : visitor.getAfterStatementList()) { 
 			makeRanking(key, proposalmap, false);
 		}
 
 		List<ValuewithRanking> proposallist = new ArrayList<ValuewithRanking>();
 		for (ValuewithRanking v: proposalmap.values()) {
 			proposallist.add(v);
 		}
 		int rank = 1;
 		for (ValuewithRanking v : proposallist) {
 			if (v.getPercentage() != 0) {
 				String str = String.format("%3d(%3d) & %s.%s \\\\", rank++, v.getPercentage(), v.getClassname(),v.getMethodname());
 System.out.println(str);
 				list.add(new CompletionProposal(str, offset, 0, 0, null, str, null, "<pre>"+str+"</pre>"));
 			}
 		}
 
 		final long end = System.nanoTime();
 		final long elapsedTime = end - start;
 		System.out.println("elapsed time: " + elapsedTime / 100000 + " milli seconds.");
 		return list;
 	}
 
 	@Override
 	public List<IContextInformation> computeContextInformation(
 			ContentAssistInvocationContext arg0, IProgressMonitor arg1) {
 		return null;
 	}
 
 }
